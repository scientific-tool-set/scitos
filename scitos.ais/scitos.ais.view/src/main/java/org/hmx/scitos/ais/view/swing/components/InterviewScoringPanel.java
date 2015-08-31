/*
   Copyright (C) 2015 HermeneutiX.org

   This file is part of SciToS.

   SciToS is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   SciToS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with SciToS. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hmx.scitos.ais.view.swing.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.KeyStroke;

import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.UndoManager;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.view.swing.IUndoManagedView;
import org.hmx.scitos.view.swing.MessageHandler;

/**
 * Extension of the interview displaying {@link InterviewPanel}, allowing the scoring (i.e. detail category assignments) for the contained text
 * tokens.
 */
public final class InterviewScoringPanel extends InterviewPanel implements IUndoManagedView {

    /** The interview view (tab) containing this panel. */
    final InterviewView parentView;
    /**
     * The manager for handling previous states of the represented interview, enabling the un-do and re-do for a finite number of model change
     * actions.
     */
    private final UndoManager<Interview> undoManager;
    /** The MouseListener/MouseMotionListener implementation handling all mouse related selections. */
    private final MouseDragListener dragHandler;
    /** Flag indicating that an un-do or re-do operation is currently in progress. */
    private boolean undoInProgress = false;

    /**
     * Main constructor.
     *
     * @param parentView
     *            the InterviewView (tab) containing this panel
     */
    public InterviewScoringPanel(final InterviewView parentView) {
        super(parentView, parentView.getProject().getModelHandler());
        this.parentView = parentView;
        this.undoManager = new UndoManager<Interview>(parentView.getModel());
        this.dragHandler = new MouseDragListener();
        this.getViewPortView().setFocusable(true);
        this.getViewPortView().addMouseListener(this.dragHandler);
        this.getViewPortView().addMouseMotionListener(this.dragHandler);
        this.getViewPortView().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(final KeyEvent event) {
                final int keyCode = event.getKeyCode();
                final KeyStroke typedKey = KeyStroke.getKeyStroke(keyCode, event.getModifiers(), true);
                for (final DetailCategory category : parentView.getProject().getModelObject().provide()) {
                    if (typedKey.equals(category.getShortCut())) {
                        InterviewScoringPanel.this.assignDetailCategoryToSelectedTokens(category);
                        return;
                    }
                }
                if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                    InterviewScoringPanel.this.assignDetailCategoryToSelectedTokens(null);
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    InterviewScoringPanel.this.moveSelection(true, (event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0);
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    InterviewScoringPanel.this.moveSelection(false, (event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0);
                }
            }
        });
    }

    @Override
    public void modelChanged(final ModelEvent<?> event) {
        // ignore events triggered by own undo/redo method
        if (!this.undoInProgress) {
            final Interview model = this.getModel();
            if (event.getTarget() == model) {
                this.undoManager.undoableEditHappened(model);
            } else if (event.getTarget() instanceof AisProject) {
                this.undoManager.reset(model);
            }
        }
        super.modelChanged(event);
    }

    @Override
    public void refresh() {
        this.dragHandler.clear();
        super.refresh();
        this.parentView.handleToolBarOptions();
    }

    @Override
    protected TextTokenComponent createTextTokenComponent(final TextToken token) {
        final TextTokenComponent component = super.createTextTokenComponent(token);
        component.addMouseListener(this.dragHandler);
        component.addMouseMotionListener(this.dragHandler);
        return component;
    }

    @Override
    public void submitChangesToModel() {
        // nothing to do, since all changes are immediately transferred to the model
    }

    /**
     * Trigger the assignment (i.e. scoring) of the given detail category to all currently selected text tokens.
     *
     * @param category
     *            detail category to assign (can be <code>null</code> to remove any current category assignments)
     */
    void assignDetailCategoryToSelectedTokens(final DetailCategory category) {
        try {
            this.parentView.getProject().getModelHandler().assignDetailCategory(this.getModel(), this.getSelectedTokens(), category);
        } catch (final HmxException expected) {
            MessageHandler.showException(expected);
        }
        // ensure the scoring panel (still) holds the focus for consecutive keyboard events, that might contain a short cut for assigning a
        // category
        this.getViewPortView().requestFocusInWindow();
    }

    /**
     * Move/extend the currently selected token range by one step.
     *
     * @param moveLeft
     *            move/extend to the left, otherwise to the right
     * @param replaceCurrentSelection
     *            if the current selection should be discarded, else the selected token range will be extended by one token in the indicated direction
     */
    void moveSelection(final boolean moveLeft, final boolean replaceCurrentSelection) {
        final List<TextTokenComponent> selection = this.getSelection();
        if (selection.isEmpty()) {
            return;
        }
        final TextTokenComponent target;
        if (moveLeft && (replaceCurrentSelection || selection.size() == 1)) {
            final TextTokenComponent firstSelectedToken = selection.get(0);
            final Container paragraphWrapper = firstSelectedToken.getParent();
            final int firstSelectedIndex = Arrays.asList(paragraphWrapper.getComponents()).indexOf(firstSelectedToken);
            if (firstSelectedIndex == 0) {
                target = firstSelectedToken;
            } else {
                target = (TextTokenComponent) paragraphWrapper.getComponent(firstSelectedIndex - 1);
            }
        } else if (moveLeft) {
            target = selection.get(selection.size() - 1);
        } else {
            final TextTokenComponent lastSelectedToken = selection.get(selection.size() - 1);
            final Container paragraphWrapper = lastSelectedToken.getParent();
            final int lastSelectedIndex = Arrays.asList(paragraphWrapper.getComponents()).indexOf(lastSelectedToken);
            if (lastSelectedIndex + 1 == paragraphWrapper.getComponentCount()) {
                target = lastSelectedToken;
            } else {
                target = (TextTokenComponent) paragraphWrapper.getComponent(lastSelectedIndex + 1);
            }
        }
        target.setSelected(!target.isSelected());
        if (replaceCurrentSelection) {
            for (final TextTokenComponent selectedToken : selection) {
                selectedToken.setSelected(false);
            }
        }
    }

    @Override
    public boolean canUndo() {
        return this.undoManager.canUndo();
    }

    @Override
    public boolean canRedo() {
        return this.undoManager.canRedo();
    }

    @Override
    public void undo() {
        this.undoInProgress = true;
        try {
            this.parentView.getProject().getModelHandler().reset(this.getModel(), this.undoManager.undo());
        } finally {
            this.undoInProgress = false;
        }
    }

    @Override
    public void redo() {
        this.undoInProgress = true;
        try {
            this.parentView.getProject().getModelHandler().reset(this.getModel(), this.undoManager.redo());
        } finally {
            this.undoInProgress = false;
        }
    }

    /** MouseListener/MouseMotionListener implementation handling all mouse related selections. */
    private final class MouseDragListener extends MouseAdapter {

        /** The modifier key mask for adding interleaved selections, if pressed while mouse clicks on tokens occur. */
        private final int menuShortcutMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        /** All text token ui components in the currently focused paragraph. */
        private List<Component> textTokenComponents;
        /**
         * The selected text token ui components at the beginning of mouse click/drag events. The selected state is being preserved, if the menu short
         * cut modifier was pressed when initial event was triggered.
         */
        private List<TextTokenComponent> previousSelection;
        /** Text token index in the currently focused paragraph the currently active mouse drag started on. */
        private int dragStartIndex;
        /** Text token index in the currently focused paragraph the last mouse drag event has been processed for. */
        private int dragPositionIndex;
        /**
         * If the current mouse click/drag events were initially triggered on an already selected token – thereby selectively removing tokens from the
         * selection instead of adding them. Drag events are preserving the {@link #previousSelection} if the previously selected tokens are outside
         * of the drag affected range.
         */
        private boolean invertSelection;

        /**
         * Main constructor.
         */
        public MouseDragListener() {
            // nothing to initialize
        }

        /** Clear the internal references after an event (chain) has ended. */
        void clear() {
            this.dragStartIndex = -1;
            this.dragPositionIndex = -1;
            this.textTokenComponents = null;
            this.previousSelection = null;
            InterviewScoringPanel.this.parentView.handleToolBarOptions();
        }

        @Override
        public void mouseDragged(final MouseEvent event) {
            if (this.dragStartIndex == -1) {
                // no valid mousePressed() preceded this event
                return;
            }
            final int currentPositionIndex = this.getTextTokenComponentIndex(event);
            if (currentPositionIndex == -1 || this.dragPositionIndex == currentPositionIndex) {
                // outside of this panel's scope or nothing changed to previous call: ignore and wait for (valid) follow up event
                return;
            }
            if (currentPositionIndex > this.dragPositionIndex) {
                this.mouseDraggedToTheRight(currentPositionIndex);
            } else {
                this.mouseDraggedToTheLeft(currentPositionIndex);
            }
            this.dragPositionIndex = currentPositionIndex;
        }

        /**
         * Handle the mouse being dragged to a higher token index (to the right or down).
         *
         * @param currentPositionIndex
         *            new position of the dragged mouse cursor, as the token's index
         */
        private void mouseDraggedToTheRight(final int currentPositionIndex) {
            if (this.dragStartIndex <= this.dragPositionIndex && this.dragPositionIndex < currentPositionIndex) {
                // selection from left to right: cursor moved to the right - selecting additional tokens
                this.setTextTokenRangeSelected(this.dragPositionIndex + 1, currentPositionIndex + 1, !this.invertSelection);
            } else if (this.dragPositionIndex < currentPositionIndex && currentPositionIndex <= this.dragStartIndex) {
                // selection from right to left: cursor moved to the right - unselecting excess tokens
                this.setTextTokenRangeSelected(this.dragPositionIndex, currentPositionIndex, false);
            } else {
                // if (this.dragPositionIndex < this.dragStartIndex && this.dragStartIndex < currentPositionIndex) {
                // selection from right to left: cursor moved right from start - switching to left to right selection
                this.setTextTokenRangeSelected(this.dragPositionIndex, this.dragStartIndex, false);
                this.setTextTokenRangeSelected(this.dragStartIndex + 1, currentPositionIndex + 1, !this.invertSelection);
            }
        }

        /**
         * Handle the mouse being dragged to a lower token index (to the left or up).
         *
         * @param currentPositionIndex
         *            new position of the dragged mouse cursor, as the token's index
         */
        private void mouseDraggedToTheLeft(final int currentPositionIndex) {
            if (this.dragStartIndex <= currentPositionIndex && currentPositionIndex < this.dragPositionIndex) {
                // selection from left to right: cursor moved to the left - unselecting excess tokens
                this.setTextTokenRangeSelected(currentPositionIndex + 1, this.dragPositionIndex + 1, false);
            } else if (this.dragStartIndex <= this.dragPositionIndex && currentPositionIndex < this.dragStartIndex) {
                // selection from left to right: cursor moved left from start - switching to right to left selection
                this.setTextTokenRangeSelected(this.dragStartIndex + 1, this.dragPositionIndex + 1, false);
                this.setTextTokenRangeSelected(currentPositionIndex, this.dragStartIndex, !this.invertSelection);
            } else {
                // if (this.dragPositionIndex < this.dragStartIndex && currentPositionIndex < this.dragPositionIndex) {
                // selection from right to left: cursor moved to the left - selecting additional tokens
                this.setTextTokenRangeSelected(currentPositionIndex, this.dragPositionIndex, !this.invertSelection);
            }
        }

        @Override
        public void mousePressed(final MouseEvent event) {
            if (event.isPopupTrigger()) {
                return;
            }
            if ((event.getModifiers() & (this.menuShortcutMask | InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) == 0) {
                // unselect all previous selections
                for (final Component singleParagraph : InterviewScoringPanel.this.getViewPortView().getComponents()) {
                    for (final Component singleToken : ((Container) singleParagraph).getComponents()) {
                        ((TextTokenComponent) singleToken).setSelected(false);
                    }
                }
            }
            // request the focus in order to receive keyboard events, that might be contain a short cut for assigning a category
            InterviewScoringPanel.this.getViewPortView().requestFocusInWindow();
            Component eventSource = (Component) event.getSource();
            // ignore clicks on the main panel or a paragraph wrapper
            if (eventSource != InterviewScoringPanel.this.getViewPortView()
                    && eventSource.getParent() != InterviewScoringPanel.this.getViewPortView()) {
                // get the paragraph wrapper panel containing the event's source
                while (eventSource.getParent() != InterviewScoringPanel.this.getViewPortView()) {
                    eventSource = eventSource.getParent();
                }
                this.textTokenComponents = Arrays.asList(((Container) eventSource).getComponents());
                this.dragStartIndex = this.getTextTokenComponentIndex(event);
                this.dragPositionIndex = this.dragStartIndex;
                if (this.dragStartIndex == -1) {
                    this.textTokenComponents = null;
                    this.previousSelection = null;
                } else {
                    final List<TextTokenComponent> currentSelection = InterviewScoringPanel.this.getSelection();
                    if ((event.getModifiers() & (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) != 0 && !currentSelection.isEmpty()
                            && ComparisonUtil.containsInstance(this.textTokenComponents, currentSelection.get(0))) {
                        // remember selection state while dragging to preserve old selected tokens
                        this.previousSelection = currentSelection;
                        // SHIFT modifier was pressed and something was already selected
                        this.invertSelection = false;
                        final int firstSelectionIndex = ComparisonUtil.indexOfInstance(this.textTokenComponents, currentSelection.get(0));
                        if (this.dragStartIndex < firstSelectionIndex) {
                            this.dragPositionIndex = this.dragStartIndex;
                            this.dragStartIndex = firstSelectionIndex;
                        } else {
                            final int lastSelectionIndex =
                                    ComparisonUtil.indexOfInstance(this.textTokenComponents, currentSelection.get(currentSelection.size() - 1));
                            this.dragPositionIndex = this.dragStartIndex;
                            this.dragStartIndex = lastSelectionIndex;
                        }
                        final int rangeStart = Math.min(this.dragStartIndex, this.dragPositionIndex);
                        final int rangeEnd = Math.max(this.dragStartIndex, this.dragPositionIndex) + 1;
                        for (int index = rangeStart; index < rangeEnd; index++) {
                            ((TextTokenComponent) this.textTokenComponents.get(index)).setSelected(true);
                        }
                    } else {
                        final TextTokenComponent target = (TextTokenComponent) this.textTokenComponents.get(this.dragStartIndex);
                        this.invertSelection = target.isSelected();
                        target.setSelected(!this.invertSelection);
                    }
                }
            }
        }

        @Override
        public void mouseReleased(final MouseEvent event) {
            if (!event.isPopupTrigger()) {
                this.clear();
            }
        }

        /**
         * Set the text tokens in the currently focused paragraph between the given fromIndex (inclusive) and the given toIndex (exclusive).
         *
         * @param fromIndex
         *            low index endpoint of the tokens in the currently focused paragraph to be affected (inclusive)
         * @param toIndex
         *            high index endpoint of the tokens in the currently focused paragraph to be affected (exclusive)
         * @param selected
         *            if the affected tokens should be selected, otherwise any selection state is being removed – unless they are part of the
         *            {@link #previousSelection}
         */
        private void setTextTokenRangeSelected(final int fromIndex, final int toIndex, final boolean selected) {
            if (fromIndex < toIndex) {
                for (final Component singleToken : this.textTokenComponents.subList(fromIndex, toIndex)) {
                    ((TextTokenComponent) singleToken).setSelected(selected
                            || ComparisonUtil.containsInstance(this.previousSelection, singleToken));
                }
            }
        }

        /**
         * Determine the index of text token ui component in the currently focused paragraph, that the given mouse event location refers to.
         *
         * @param event
         *            occurred event to extract the associated text token ui component for
         * @return (can be <code>-1</code> if the event's location does not relate to any token.
         */
        private int getTextTokenComponentIndex(final MouseEvent event) {
            Component eventSource = (Component) event.getSource();
            // ignore clicks on the main panel or a paragraph wrapper
            if (eventSource == InterviewScoringPanel.this.getViewPortView()
                    || eventSource.getParent() == InterviewScoringPanel.this.getViewPortView()) {
                return -1;
            }
            // get the paragraph wrapper panel containing the event's source
            while (eventSource.getParent() != InterviewScoringPanel.this.getViewPortView()) {
                eventSource = eventSource.getParent();
            }
            final Point paragraphWrapperOnScreen = eventSource.getLocationOnScreen();
            final Point cursorLocation = event.getLocationOnScreen();
            cursorLocation.x -= paragraphWrapperOnScreen.x;
            cursorLocation.y -= paragraphWrapperOnScreen.y;
            final Component origin = eventSource.getComponentAt(cursorLocation);
            if (origin == null) {
                return -1;
            }
            return ComparisonUtil.indexOfInstance(this.textTokenComponents, origin);
        }
    }
}

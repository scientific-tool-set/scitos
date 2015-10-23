package org.hmx.scitos.hmx.view.swing.elements;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.hmx.view.ContextMenuFactory;
import org.hmx.scitos.hmx.view.IPericopeView;
import org.hmx.scitos.hmx.view.swing.components.SynAnalysisPanel;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.swing.ContextMenuPopupBuilder;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.util.VTextIcon;

/**
 * extension of the {@link AbstractProposition} with a {@link SynItem}s in the top right area to display the contained {@link ClauseItem}s and their
 * included origin text.
 */
public final class SynProposition extends AbstractProposition {

    final IPericopeView viewReference;
    /**
     * label displaying the syntactical indentation function.
     */
    private final JLabel functionLabel = new ScaledLabel(" ");
    /**
     * view representation of the contained clause items.
     */
    private final List<SynItem> items = new LinkedList<SynItem>();

    /**
     * creates a {@link SynProposition} by setting all values regarding to the represented {@link Proposition} and the language and {@link Font} of
     * the origin text.
     *
     * @param synArea
     *            syntactical analysis view to be contained in
     * @param commentHandler
     *            handling entity for comments on view elements
     * @param modelHandler
     *            the responsible model handler implementation realizing any model changes
     * @param represented
     *            represented {@link Proposition} to set
     */
    private SynProposition(final IPericopeView viewReference, final Proposition represented) {
        super(viewReference.getModelHandler(), represented);
        this.viewReference = viewReference;
        this.init();
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                viewReference.handleSelectedCommentable(SynProposition.this);
                this.mouseReleased(event);
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (event.isPopupTrigger()) {
                    final ContextMenuBuilder contextMenu;
                    if (SynProposition.this.getRepresented().getPartBeforeArrow() == null) {
                        contextMenu = ContextMenuFactory.createSynPropositionPopup(viewReference, represented);
                    } else {
                        contextMenu = ContextMenuFactory.createSynPropositionAfterArrowPopup(viewReference, represented);
                    }
                    ContextMenuPopupBuilder.buildSwingPopupMenu(contextMenu).show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });
    }

    /**
     * creates a {@link SynProposition} by setting all values regarding to the represented {@link Proposition} and its indentation referring to the
     * given level; transmitting the chosen language and {@link Font} of the origin text to the {@link SynItem}s contained.
     *
     * @param synPanel
     *            syntactical analysis view to be contained in
     * @param represented
     *            represented {@link Proposition} to set
     * @param level
     *            number of indentations to set
     * @return created {@link SynProposition}
     */
    public static SynProposition createSynPropositionByLevel(final IPericopeView viewReference, final Proposition represented, final int level) {
        final SynProposition created = new SynProposition(viewReference, represented);
        if (level > 0) {
            created.getIndentationArea().setPreferredSize(AbstractProposition.createIndentation(level));
        }
        return created;
    }

    /**
     * creates a {@link SynProposition} by setting all values regarding to the represented {@link Proposition} and its indentation equivalent to the
     * width of the view representation of the <code>partBeforeArrow</code>; transmitting the chosen language and {@link Font} of the origin text to
     * the {@link SynItem}s contained.
     *
     * @param synPanel
     *            syntactical analysis view to be contained in
     * @param represented
     *            represented {@link Proposition} to set
     * @param partBeforeArrow
     *            view representation of the <code>partBeforeArrow</code>
     * @return created {@link SynProposition}
     */
    public static SynProposition createSynPropositionByPartBeforeArrow(final IPericopeView viewReference, final Proposition represented,
            final SynProposition partBeforeArrow) {
        final SynProposition created = new SynProposition(viewReference, represented);
        created.getIndentationArea().setPreferredSize(SynProposition.createIndentationAfterArrow(partBeforeArrow));
        return created;
    }

    /**
     * initializes the {@link JLabel} in the indentation area for displaying its indentation function and an expanding {@link JPanel} on the right to
     * make sure it is always at the left side of its containing {@link SynAnalysisPanel} and the explicit syntactical functionality by inserting the
     * contained {@link SynItem}s, adding a listener to the translation field and activating the comment listener.
     */
    private void init() {
        final SyntacticalFunction function = this.getRepresented().getFunction();
        if (this.getRepresented().getParent() instanceof Proposition && function != null) {
            this.refreshFunction();
            final GridBagConstraints spacing = new GridBagConstraints();
            spacing.fill = GridBagConstraints.HORIZONTAL;
            spacing.weightx = 1;
            this.getIndentationArea().add(new JPanel(), spacing);
            final int border = (AbstractProposition.createIndentation(1).width - this.functionLabel.getPreferredSize().width) / 2;
            final GridBagConstraints verticalSpan = new GridBagConstraints();
            verticalSpan.fill = GridBagConstraints.VERTICAL;
            verticalSpan.weighty = 1;
            verticalSpan.insets = new Insets(0, border, 0, border);
            this.getIndentationArea().add(this.functionLabel, verticalSpan);
        }

        // create expanding panel on the right side of the proposition
        final GridBagConstraints rightSpacing = new GridBagConstraints();
        rightSpacing.fill = GridBagConstraints.HORIZONTAL;
        rightSpacing.weightx = 1;
        rightSpacing.gridx = 1;
        rightSpacing.gridy = 0;
        this.add(new JPanel(), rightSpacing);

        this.getTranslationField().addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent event) {
                final String translationText = SynProposition.this.getTranslationField().getText();
                if (!ComparisonUtil.isNullOrEmptyAwareEqual(translationText, SynProposition.this.getRepresented().getSynTranslation())) {
                    // only transfer if necessary
                    SynProposition.this.getModelHandler().setSynTranslation(SynProposition.this.getRepresented(), translationText);
                }
            }
        });
        for (final ClauseItem singleItem : this.getRepresented()) {
            this.insertItem(singleItem);
        }
        this.refreshComment();
    }

    /**
     * creates a {@link Dimension} for the indentation by settings its width regarding to the width of the view representation of the
     * <code>partBeforeArrow</code>
     *
     * @param partBeforeArrow
     *            view representation of the <code>partBeforeArrow</code>
     * @return {@link Dimension} representing the indentation
     */
    private static Dimension createIndentationAfterArrow(final SynProposition partBeforeArrow) {
        return new Dimension(partBeforeArrow.getIndentationArea().getPreferredSize().width
                + partBeforeArrow.getLeftArrows().getPreferredSize().width + partBeforeArrow.getItemArea().getPreferredSize().width, 1);
    }

    /**
     * inserts the specified {@link ClauseItem} at the end of displayed {@link SynItem}s
     *
     * @param item
     *            {@link ClauseItem} to insert
     */
    public void insertItem(final ClauseItem item) {
        final SynItem singleItem = new SynItem(this.viewReference, item);
        this.items.add(singleItem);
        this.getItemArea().add(singleItem);
    }

    /**
     * removes the specified {@link SynItem} from the displayed item area
     *
     * @param target
     *            {@link SynItem} to remove
     */
    public void removeItem(final SynItem target) {
        target.setVisible(false);
        this.items.remove(target);
        this.getItemArea().remove(target);
    }

    /**
     * @return displayed {@link SynItem}s
     */
    public SynItem[] getItems() {
        final SynItem[] val = new SynItem[this.items.size()];
        for (int i = 0; i < val.length; i++) {
            val[i] = this.items.get(i);
        }
        return val;
    }

    /**
     * sets the function (related to parent {@link Proposition} it is indented to) regarding the represented {@link Proposition}.
     *
     * @return if a <code>repaint()</code> is neccessary
     */
    public boolean refreshFunction() {
        final SyntacticalFunction function = this.getRepresented().getFunction();
        if (!(this.getRepresented().getParent() instanceof Proposition) || function == null) {
            return false;
        }
        if (this.functionLabel.getIcon() == null) {
            this.functionLabel.setIcon(new VTextIcon(this.functionLabel, function.getCode(), VTextIcon.Rotate.NONE));
        } else {
            ((VTextIcon) this.functionLabel.getIcon()).setLabel(function.getCode());
        }
        return true;
    }

    /**
     * sets the syntactical translation text regarding the stored translation in the represented {@link Proposition}.
     *
     * @see AbstractProposition#refreshTranslation()
     */
    @Override
    public void refreshTranslation() {
        this.getTranslationField().setText(this.getRepresented().getSynTranslation());
        super.refreshTranslation();
    }

    /**
     * resets the tool tip info containing the comment text regarding its value in the represented {@link Proposition}
     */
    public void refreshComment() {
        final String comment = this.getRepresented().getComment();
        if (comment == null || comment.isEmpty()) {
            this.setToolTipText(null);
        } else {
            this.setToolTipText(comment);
        }
    }

    @Override
    public void setDefaultBorder() {
        super.setDefaultBorder();
        if (this.items != null) {
            this.getItemArea().revalidate();
            for (final SynItem singleItem : this.getItems()) {
                singleItem.revalidate();
            }
        }
    }
}

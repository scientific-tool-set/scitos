/*
   Copyright (C) 2016 HermeneutiX.org

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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.hmx.scitos.ais.core.AisModelHandler;
import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.domain.IProvider;
import org.hmx.scitos.domain.ModelChangeListener;
import org.hmx.scitos.domain.ModelEvent;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.view.swing.util.WrapLayout;

/** Component displaying a single interview's text, including the assigned detail categories. */
public class InterviewPanel extends JScrollPane implements ModelChangeListener {

    /** Provider of the displayed interview. Will be called repeatedly if relevant model events occur. */
    private final IProvider<Interview> displayedInterviewProvider;
    /** The actual panel containing the interview's text, which is wrapped by this scrollable component. */
    private final JPanel viewPortView;

    /**
     * Main constructor.
     *
     * @param displayedInterviewProvider
     *            the provider of the displayed {@link Interview}
     * @param modelHandler
     *            the model handler responsible for the displayed interview, to listen for model change events on
     */
    public InterviewPanel(final IProvider<Interview> displayedInterviewProvider, final AisModelHandler modelHandler) {
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.displayedInterviewProvider = displayedInterviewProvider;
        this.setBorder(null);
        this.viewPortView = new JPanel(new GridBagLayout()) {

            @Override
            public void updateUI() {
                super.updateUI();
                final Color background = UIManager.getColor("TextPane.background");
                this.setBackground(background == null ? Color.WHITE : new Color(background.getRGB()));
            }
        };
        this.addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorAdded(final AncestorEvent event) {
                modelHandler.addModelChangeListener(InterviewPanel.this);
            }

            @Override
            public void ancestorRemoved(final AncestorEvent event) {
                modelHandler.removeModelChangeListener(InterviewPanel.this);
            }

            @Override
            public void ancestorMoved(final AncestorEvent event) {
                // ignore move events
            }

        });
        // start the initialization separately to allow potential sub classes to prepare themselves
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                InterviewPanel.this.init();
            }
        });
    }

    /** Initialize the interview contents and prepare to refresh the view whenever it is reloaded (i.e. the containing tab is made visible). */
    protected void init() {
        this.refresh();
        this.setViewportView(this.viewPortView);
        this.validate();
        this.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(final HierarchyEvent event) {
                if (InterviewPanel.this.isShowing()) {
                    InterviewPanel.this.refresh();
                } else {
                    // discard selection
                    for (final TextTokenComponent selectedToken : InterviewPanel.this.getSelection()) {
                        selectedToken.setSelected(false);
                    }
                }
            }
        });
    }

    @Override
    public void modelChanged(final ModelEvent<?> event) {
        if (this.isShowing() && (event.getTarget() instanceof AisProject || event.getTarget() == this.getModel())) {
            // refresh the whole view
            this.refresh();
        }
    }

    /**
     * Getter for the actual panel containing the view contents.
     *
     * @return the scrolled view content pane
     */
    protected final JPanel getViewPortView() {
        return this.viewPortView;
    }

    /**
     * Getter for the currently displayed interview's model element.
     *
     * @return displayed model element
     */
    protected final Interview getModel() {
        return this.displayedInterviewProvider.provide();
    }

    /** Discard all currently displayed contents and rebuild from current model state. */
    protected void refresh() {
        final List<TextToken> selection = this.getSelectedTokens();
        this.getViewPortView().removeAll();
        boolean isFirstParagraph = true;
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        for (final TextToken paragraphStart : this.getModel().getText()) {
            if (isFirstParagraph) {
                isFirstParagraph = false;
            } else {
                this.getViewPortView().add(new JSeparator(), constraints);
                constraints.gridy++;
            }
            final JPanel paragraphWrapper = new JPanel(new WrapLayout(FlowLayout.LEADING, 0, 5), true);
            paragraphWrapper.setOpaque(false);
            TextToken currentToken = paragraphStart;
            do {
                final TextTokenComponent tokenComponent = this.createTextTokenComponent(currentToken);
                tokenComponent.setSelected(CollectionUtil.containsInstance(selection, currentToken));
                paragraphWrapper.add(tokenComponent);
                currentToken = currentToken.getFollowingToken();
            } while (currentToken != null);
            this.getViewPortView().add(paragraphWrapper, constraints);
            constraints.gridy++;
        }
        constraints.weighty = 1;
        final JPanel spacing = new JPanel();
        spacing.setOpaque(false);
        spacing.setMinimumSize(new Dimension(1, 1));
        this.getViewPortView().add(spacing, constraints);
        this.revalidate();
    }

    /**
     * Create a single text token component. Sub classes should override this method in order to enable editing the interview (i.e. assigning detail
     * categories to tokens).
     *
     * @param token
     *            single text token to represent in an ui component
     * @return ui component for the given token
     */
    protected TextTokenComponent createTextTokenComponent(final TextToken token) {
        return new TextTokenComponent(token);
    }

    /**
     * Check if a range of tokens is currently selected, that is valid to get a detail category assigned.
     *
     * @return if at least one token is currently selected (the rest is sorted out in the {@link AisModelHandler model handler})
     */
    final boolean containsValidSelection() {
        for (final Component singleParagraph : this.getViewPortView().getComponents()) {
            for (final Component singleToken : ((Container) singleParagraph).getComponents()) {
                if (((TextTokenComponent) singleToken).isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Collect the text token ui components, that are currently in selected state. In order to expect anything else but an empty list from this
     * method, sub classes need to allow the setting of the text token ui components' selected state.
     *
     * @return selected text token ui components
     */
    final List<TextTokenComponent> getSelection() {
        final List<TextTokenComponent> selection = new LinkedList<TextTokenComponent>();
        for (final Component singleParagraph : this.getViewPortView().getComponents()) {
            for (final Component singleToken : ((Container) singleParagraph).getComponents()) {
                if (((TextTokenComponent) singleToken).isSelected()) {
                    selection.add((TextTokenComponent) singleToken);
                }
            }
        }
        return selection;
    }

    /**
     * Collect the text tokens whose ui components are currently in selected state.
     *
     * @return selected text token components
     * @see #getSelection()
     */
    final List<TextToken> getSelectedTokens() {
        final List<TextTokenComponent> components = this.getSelection();
        final List<TextToken> selection = new ArrayList<TextToken>(components.size());
        for (final TextTokenComponent selectedComponent : components) {
            selection.add(selectedComponent.getModel());
        }
        return selection;
    }
}

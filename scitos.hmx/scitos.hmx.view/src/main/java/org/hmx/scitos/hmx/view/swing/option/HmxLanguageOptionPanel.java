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

package org.hmx.scitos.hmx.view.swing.option;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import net.java.dev.designgridlayout.DesignGridLayout;
import net.java.dev.designgridlayout.IHideable;

import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.core.option.HmxLanguageOption;
import org.hmx.scitos.hmx.domain.model.LanguageModel;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.option.AbstractOptionPanel;

/**
 * Option panel representing the {@link HmxLanguageOption} and thereby offering the configuration of available {@link LanguageModel}s.
 */
public final class HmxLanguageOptionPanel extends AbstractOptionPanel {

    /** The global preferences handler being represented by this view component. */
    private final HmxLanguageOption options;
    /** The UI component for adding/updating/removing the language models. */
    final LanguageConfigPanel languagePanel;
    /** The UI component for adding/updating/removing syntactical functions of a language model. */
    final SynFunctionConfigPanel functionPanel = new SynFunctionConfigPanel();

    /**
     * Constructor.
     * 
     * @param options
     *            the global preferences handler being represented by this view component
     */
    public HmxLanguageOptionPanel(final HmxLanguageOption options) {
        super(null, HmxMessage.PREFERENCES_LANGUAGE);
        this.options = options;
        this.setBorder(BorderFactory.createEmptyBorder(5, 15, 0, 0));
        this.languagePanel = new LanguageConfigPanel(options);
        final DesignGridLayout layout = new DesignGridLayout(this);
        final IHideable languagePanelRow = layout.row().center().add(this.languagePanel).fill().withOwnRowWidth();
        final JButton switchToLanguagesButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_EDIT_LANGUAGES.get(), ScitosIcon.CATEGORY.create());
        final JButton switchToFunctionsButton = new JButton(HmxMessage.PREFERENCES_LANGUAGE_EDIT_FUNCTIONS.get(), ScitosIcon.CATEGORY.create());
        final IHideable switchToLanguagesRow = layout.row().center().add(switchToLanguagesButton);
        final IHideable switchToFunctionsRow = layout.row().center().add(switchToFunctionsButton);
        final IHideable functionPanelRow = layout.row().center().add(this.functionPanel).fill().withOwnRowWidth();
        switchToLanguagesRow.hide();
        functionPanelRow.hide();
        switchToFunctionsButton.setEnabled(false);
        switchToFunctionsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                languagePanelRow.hide();
                switchToLanguagesRow.forceShow();
                HmxLanguageOptionPanel.this.functionPanel.reset(HmxLanguageOptionPanel.this.languagePanel.getSelectedModel());
                switchToFunctionsRow.hide();
                functionPanelRow.forceShow();
            }
        });
        switchToLanguagesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                languagePanelRow.forceShow();
                switchToLanguagesRow.hide();
                HmxLanguageOptionPanel.this.languagePanel.getSelectedModel().reset(HmxLanguageOptionPanel.this.functionPanel.provideFunctions());
                HmxLanguageOptionPanel.this.languagePanel.fireSelectedModelRowUpdated();
                switchToFunctionsRow.forceShow();
                functionPanelRow.hide();
            }
        });
        this.languagePanel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                switchToFunctionsButton.setEnabled(!HmxLanguageOptionPanel.this.languagePanel.isInEditMode()
                        && HmxLanguageOptionPanel.this.languagePanel.isSelectedModelUserDefined());
            }
        });
    }

    @Override
    protected boolean isResizeCapable() {
        // take care of individual scrolling internally
        return true;
    }

    @Override
    protected void validateInput() {
        // nothing to do here, as the user has to specifically press the appropriate buttons to apply/discard any changes
    }

    @Override
    public boolean areChosenSettingsValid() {
        return !this.languagePanel.isInEditMode() && !this.functionPanel.isInEditMode();
    }

    @Override
    public void submitChosenSettings() {
        final List<LanguageModel> configuredModels = this.languagePanel.getUserModels();
        if (!this.options.getUserModels().equals(configuredModels)) {
            this.options.setUserModels(configuredModels);
            this.options.persistChanges();
        }
    }
}

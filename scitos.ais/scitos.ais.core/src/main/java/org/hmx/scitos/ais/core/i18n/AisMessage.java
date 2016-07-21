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

package org.hmx.scitos.ais.core.i18n;

import org.hmx.scitos.core.i18n.ILocalizableMessage;
import org.hmx.scitos.core.i18n.Translator;

/**
 * Utility class for internationalization of the components on the AIS module's graphical user interface.
 */
public enum AisMessage implements ILocalizableMessage {
    // general project actions
    PROJECT_NEW("Ais.Project.New"),
    PROJECT_EXPORT_HTML("Ais.Project.Export.Html"),
    PROJECT_EXPORT_ODS("Ais.Project.Export.Ods"),
    PROJECT_CHANGE_CATEGORIES("Ais.Project.ChangeDetailCategories"),
    // the matching dialog for detail category changes on an active project
    PROJECT_CHANGE_CATEGORIES_MATCHING("Ais.Project.ChangeDetailCategories.MatchOldToNew"),
    PROJECT_CHANGE_CATEGORIES_MATCHING_DESCRIPTION("Ais.Project.ChangeDetailCategories.MatchOldToNew.Description"),
    PROJECT_CHANGE_CATEGORIES_AFFECTED_TOKENS("Ais.Project.ChangeDetailCategories.AffectedTokens"),
    PROJECT_CHANGE_CATEGORIES_OLD("Ais.Project.ChangeDetailCategories.OldCategory"),
    PROJECT_CHANGE_CATEGORIES_NEW("Ais.Project.ChangeDetailCategories.ReplacingCategory"),
    // creating, modifying (participant, order), and deleting an interview
    INTERVIEW_NEW("Ais.Interview.New"),
    INTERVIEW_NEW_PARTICIPANTID("Ais.Interview.New.ParticipantId"),
    INTERVIEW_CHANGE_PARTICIPANTID("Ais.Interview.Modify.ParticipantId"),
    INTERVIEW_CHANGE_PARTICIPANTID_DESCRIPTION("Ais.Interview.Modify.ParticipantId.Description"),
    INTERVIEW_CHANGE_ORDER("Ais.Interview.Modify.Order"),
    INTERVIEW_DELETE("Ais.Interview.Delete"),
    INTERVIEW_DELETE_WARNING("Ais.Interview.Delete.Warning"),
    // hints and buttons for the actual usage (inserting and scoring an interview)
    INTERVIEW_TEXTINPUT_HINT("Ais.Interview.InputHint"),
    INTERVIEW_START_SCORING("Ais.Interview.StartScoring"),
    SCORE_REMOVE("Ais.Interview.Score.Remove"),
    SCORE_REMOVE_TOOLTIP("Ais.Interview.Score.RemoveToolTip"),
    ERROR_AIS_SELECTION_INVALID("Ais.Interview.Score.SelectionInvalid"),
    // the pattern analysis (i.e. summary) on the project's main tab
    ANALYSIS_SUMMARY("Ais.Analysis.Summary"),
    ANALYSIS_SEQUENCE("Ais.Analysis.Sequence"),
    ANALYSIS_PATTERN("Ais.Analysis.Pattern"),
    ANALYSIS_TABLE_HEADER_INTERVIEW("Ais.Analysis.TableHeader.Interview"),
    ANALYSIS_TABLE_HEADER_TOKENCOUNT("Ais.Analysis.TableHeader.TokenCount"),
    ANALYSIS_NOT_SCORED("Ais.Analysis.TableContent.NoDetailsScored"),
    ANALYSIS_EXPORT("Ais.Analysis.ExportToSpreadsheet"),
    ERROR_EXPORT_FAILED("Ais.Analysis.ExportToSpreadSheet.Failure"),
    // modification of a detail category model
    DETAIL_CATEGORIES_INVALID("Ais.DetailCategories.Invalid"),
    DETAIL_CATEGORY_ADD_ROOT("Ais.DetailCategory.AddRootCategory"),
    DETAIL_CATEGORY_ADD_CHILD("Ais.DetailCategory.AddChildCategory"),
    DETAIL_CATEGORY_CODE("Ais.DetailCategory.Code"),
    DETAIL_CATEGORY_CODE_DUPLICATE("Ais.DetailCategory.Code.NotUnique"),
    DETAIL_CATEGORY_COLOR("Ais.DetailCategory.Color"),
    DETAIL_CATEGORY_DELETE("Ais.DetailCategory.Delete"),
    DETAIL_CATEGORY_NAME("Ais.DetailCategory.Name"),
    DETAIL_CATEGORY_SHORTCUT("Ais.DetailCategory.ShortCut"),
    DETAIL_CATEGORIES_AS_DEFAULT("Ais.DetailCategories.SetAsDefault"),
    // application preferences entry
    PREFERENCES("Ais.Client.Preferences.NodeLabel"),
    PREFERENCES_DETAIL_CATEGORIES("Ais.Client.Preferences.DetailCategories.Default");

    /** The static translator instance to use for all messages of this type. */
    private static final Translator<AisMessage> TRANSLATOR = new Translator<AisMessage>(AisMessage.class);

    /** The attribute's key as it can be found in the actual language files. */
    private final String key;

    /**
     * Main constructor.
     *
     * @param messageKey
     *            actual key in the language file
     */
    private AisMessage(final String messageKey) {
        this.key = messageKey;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String get() {
        return AisMessage.TRANSLATOR.getLocalizedMessage(this);
    }

    @Override
    public String toString() {
        return this.get();
    }
}

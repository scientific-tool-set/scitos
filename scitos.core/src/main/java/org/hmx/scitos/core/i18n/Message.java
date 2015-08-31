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

package org.hmx.scitos.core.i18n;

import java.util.Arrays;
import java.util.Locale;

/**
 * Utility class for internationalization of the components on the graphical user interface.
 */
public enum Message implements ILocalizableMessage {
    CLIENT_TITLE_SCITOS("Client.Title.SciToS"), TOOLBAR_TITLE("Client.ToolBar.Title"),
    // basic messages
    OK("Ok"),
    CANCEL("Cancel"),
    TAB_CLOSE("Tab.Close"),
    TAB_CLOSE_ALL("Tab.Close.All"),
    TAB_CLOSE_OTHERS("Tab.Close.Others"),
    ERROR("Error"),
    ERROR_UNKNOWN("Error.Unknown"),
    // file menu and its menu items
    MENUBAR_FILE("Client.MenuBar.File"),
    MENUBAR_FILE_NEW("Client.MenuBar.File.New"),
    ERROR_FILE_NEW_PROJECTNAME_INVALID("Client.MenuBar.File.New.ProjectNameInvalid"),
    MENUBAR_FILE_OPEN("Client.MenuBar.File.Open"),
    ERROR_FILE_INVALID("Client.MenuBar.File.Open.InvalidXmlDocument"),
    ERROR_FILE_TYPE_NOT_RECOGNIZED("Client.MenuBar.File.Open.FileTypeNotRecognized"),
    MENUBAR_FILE_OPEN_ALREADY("Client.MenuBar.File.Open.ProjectAlreadyOpen"),
    MENUBAR_FILE_SAVE("Client.MenuBar.File.Save"),
    ERROR_SAVE_FAILED("Client.MenuBar.File.Save.Failed"),
    MENUBAR_FILE_SAVEAS("Client.MenuBar.File.SaveAs"),
    MENUBAR_FILE_TYPE_AIS("Client.MenuBar.File.Types.AIS"),
    MENUBAR_PREFERENCES("Client.MenuBar.Preferences"),
    MENUBAR_QUIT("Client.MenuBar.Quit"),
    // edit menu and its menu items
    MENUBAR_EDIT("Client.MenuBar.Edit"),
    MENUBAR_EDIT_UNDO("Client.MenuBar.Edit.Undo"),
    MENUBAR_EDIT_REDO("Client.MenuBar.Edit.Redo"),
    // the about menu entry
    MENUBAR_ABOUT("Client.MenuBar.About"),
    // the preferences dialog
    PREFERENCES_GENERAL("Client.Preferences.General"),
    PREFERENCES_GENERAL_LOOK_AND_FEEL("Client.Preferences.General.LookAndFeel"),
    PREFERENCES_GENERAL_UNDO("Client.Preferences.General.UndoLimit"),
    // the default welcome tab displayed in an empty opened client
    TAB_WELCOME_TITLE("Client.Tab.Welcome.Title"),
    TAB_WELCOME_TEXT("Client.Tab.Welcome.Text"),
    // the dialog for confirming the closing of a project
    PROJECT_CLOSE("Client.Project.Close"),
    PROJECT_CLOSE_QUESTION("Client.Project.Close.SaveChangesBeforeClosingQuestion"),
    // AIS specific texts
    AIS_PROJECT_NEW("Ais.Project.New"),
    AIS_PROJECT_CHANGE_CATEGORIES("Ais.Project.ChangeDetailCategories"),
    AIS_PROJECT_CHANGE_CATEGORIES_MATCHING("Ais.Project.ChangeDetailCategories.MatchOldToNew"),
    AIS_PROJECT_CHANGE_CATEGORIES_MATCHING_DESCRIPTION("Ais.Project.ChangeDetailCategories.MatchOldToNew.Description"),
    AIS_PROJECT_CHANGE_CATEGORIES_AFFECTED_TOKENS("Ais.Project.ChangeDetailCategories.AffectedTokens"),
    AIS_PROJECT_CHANGE_CATEGORIES_OLD("Ais.Project.ChangeDetailCategories.OldCategory"),
    AIS_PROJECT_CHANGE_CATEGORIES_NEW("Ais.Project.ChangeDetailCategories.ReplacingCategory"),
    AIS_INTERVIEW_NEW("Ais.Interview.New"),
    AIS_INTERVIEW_NEW_PARTICIPANTID("Ais.Interview.New.ParticipantId"),
    AIS_INTERVIEW_CHANGE_PARTICIPANTID("Ais.Interview.Modify.ParticipantId"),
    AIS_INTERVIEW_CHANGE_PARTICIPANTID_DESCRIPTION("Ais.Interview.Modify.ParticipantId.Description"),
    AIS_INTERVIEW_CHANGE_PARTICIPANTID_ALREADY_EXISTS("Ais.Interview.Modify.ParticipantId.AlreadyExists"),
    AIS_INTERVIEW_CHANGE_ORDER("Ais.Interview.Modify.Order"),
    AIS_INTERVIEW_DELETE("Ais.Interview.Delete"),
    AIS_INTERVIEW_DELETE_WARNING("Ais.Interview.Delete.Warning"),
    AIS_INTERVIEW_TEXTINPUT_HINT("Ais.Interview.InputHint"),
    AIS_INTERVIEW_START_SCORING("Ais.Interview.StartScoring"),
    AIS_SCORE_REMOVE("Ais.Interview.Score.Remove"),
    AIS_SCORE_REMOVE_TOOLTIP("Ais.Interview.Score.RemoveToolTip"),
    ERROR_AIS_SELECTION_INVALID("Ais.Interview.Score.SelectionInvalid"),
    AIS_ANALYSIS_SUMMARY("Ais.Analysis.Summary"),
    AIS_ANALYSIS_SEQUENCE("Ais.Analysis.Sequence"),
    AIS_ANALYSIS_PATTERN("Ais.Analysis.Pattern"),
    AIS_ANALYSIS_TABLE_HEADER_INTERVIEW("Ais.Analysis.TableHeader.Interview"),
    AIS_ANALYSIS_TABLE_HEADER_TOKENCOUNT("Ais.Analysis.TableHeader.TokenCount"),
    AIS_ANALYSIS_NOT_SCORED("Ais.Analysis.TableContent.NoDetailsScored"),
    AIS_ANALYSIS_EXPORT("Ais.Analysis.ExportToSpreadsheet"),
    ERROR_EXPORT_FAILED("Ais.Analysis.ExportToSpreadSheet.Failure"),
    AIS_PREFERENCES("Ais.Client.Preferences.NodeLabel"),
    AIS_PREFERENCES_DETAIL_CATEGORIES("Ais.Client.Preferences.DetailCategories.Default"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_INVALID("Ais.Client.Preferences.DetailCategories.Invalid"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_CODE("Ais.Client.Preferences.DetailCategories.Code"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_CODE_DUPLICATE("Ais.Client.Preferences.DetailCategories.Code.NotUnique"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_NAME("Ais.Client.Preferences.DetailCategories.Name"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_COLOR("Ais.Client.Preferences.DetailCategories.Color"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_SHORTCUT("Ais.Client.Preferences.DetailCategories.ShortCut"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_MATCH("Ais.Client.Preferences.DetailCategories.MatchOldCategory"),
    AIS_PREFERENCES_DETAIL_CATEGORY_ADD_ROOT("Ais.Client.Preferences.DetailCategories.AddRootCategory"),
    AIS_PREFERENCES_DETAIL_CATEGORY_ADD_CHILD("Ais.Client.Preferences.DetailCategories.AddChildCategory"),
    AIS_PREFERENCES_DETAIL_CATEGORY_DELETE("Ais.Client.Preferences.DetailCategories.DeleteCategory"),
    AIS_PREFERENCES_DETAIL_CATEGORIES_AS_DEFAULT("Ais.Client.Preferences.DetailCategories.SetAsDefault");

    /** The static translator instance to use for all messages of this type. */
    private static final Translator<Message> TRANSLATOR = new Translator<Message>(Arrays.asList(Message.values()));

    /** The attribute's key as it can be found in the actual language files. */
    final String key;

    /**
     * Main constructor.
     *
     * @param messageKey
     *            actual key in the language file
     */
    private Message(final String messageKey) {
        this.key = messageKey;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String get() {
        return this.get(Locale.getDefault());
    }

    @Override
    public String get(final Locale locale) {
        return Message.TRANSLATOR.getLocalizedMessage(this, locale);
    }

    @Override
    public String toString() {
        return this.get();
    }
}

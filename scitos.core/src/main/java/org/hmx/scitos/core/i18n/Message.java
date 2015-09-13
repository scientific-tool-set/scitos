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

import java.util.List;
import java.util.Locale;

/**
 * Utility class for internationalization of the components on the graphical user interface.
 */
public enum Message implements ILocalizableMessage {
    CLIENT_TITLE_SCITOS("Client.Title.SciToS"),
    TOOLBAR_TITLE("Client.ToolBar.Title"),
    OK("Ok"),
    CANCEL("Cancel"),
    TAB_CLOSE("Tab.Close"),
    TAB_CLOSE_ALL("Tab.Close.All"),
    TAB_CLOSE_OTHERS("Tab.Close.Others"),
    ERROR("Error"),
    ERROR_UNKNOWN("Error.Unknown"),
    // file menu and its menu items
    MENUBAR_FILE("Client.MenuBar.File"),
    MENUBAR_FILE_EXPORT("Client.MenuBar.File.Export"),
    MENUBAR_FILE_NEW("Client.MenuBar.File.New"),
    MENUBAR_FILE_OPEN("Client.MenuBar.File.Open"),
    ERROR_FILE_INVALID("Client.MenuBar.File.Open.InvalidXmlDocument"),
    ERROR_FILE_TYPE_NOT_RECOGNIZED("Client.MenuBar.File.Open.FileTypeNotRecognized"),
    MENUBAR_FILE_OPEN_ALREADY("Client.MenuBar.File.Open.ProjectAlreadyOpen"),
    MENUBAR_FILE_SAVE("Client.MenuBar.File.Save"),
    ERROR_SAVE_FAILED("Client.MenuBar.File.Save.Failed"),
    MENUBAR_FILE_SAVEAS("Client.MenuBar.File.SaveAs"),
    MENUBAR_FILE_TYPE_AIS("Client.MenuBar.File.Types.AIS"),
    // the about menu entry
    MENUBAR_ABOUT("Client.MenuBar.About"),
    MENUBAR_PREFERENCES("Client.MenuBar.Preferences"),
    MENUBAR_QUIT("Client.MenuBar.Quit"),
    // edit menu and its menu items
    MENUBAR_EDIT("Client.MenuBar.Edit"),
    MENUBAR_EDIT_UNDO("Client.MenuBar.Edit.Undo"),
    MENUBAR_EDIT_REDO("Client.MenuBar.Edit.Redo"),
    // view menu and its menu items
    MENUBAR_VIEW("Client.MenuBar.View"),
    MENUBAR_VIEW_FONT_SCALE_UP("Client.MenuBar.View.Font.ScaleUp"),
    MENUBAR_VIEW_FONT_SCALE_DOWN("Client.MenuBar.View.Font.ScaleDown"),
    MENUBAR_VIEW_TOGGLE_PROJECT_TREE("Client.MenuBar.View.ProjectTreeVisibility"),
    // the preferences dialog
    PREFERENCES_GENERAL("Client.Preferences.General"),
    PREFERENCES_GENERAL_LOOK_AND_FEEL("Client.Preferences.General.LookAndFeel"),
    PREFERENCES_GENERAL_TRANSLATION("Client.Preferences.General.Translation"),
    PREFERENCES_GENERAL_TRANSLATION_SYSTEM_DEFAULT("Client.Preferences.General.Translation.SystemDefault"),
    PREFERENCES_GENERAL_UNDO("Client.Preferences.General.UndoLimit"),
    PREFERENCES_RESTART_REQUIRED("Client.Preferences.RestartRequired"),
    // the default welcome tab displayed in an empty opened client
    TAB_WELCOME_TITLE("Client.Tab.Welcome.Title"),
    TAB_WELCOME_TEXT("Client.Tab.Welcome.Text"),
    // the dialog for confirming the closing of a project
    PROJECT_CLOSE("Client.Project.Close"),
    PROJECT_CLOSE_QUESTION("Client.Project.Close.SaveChangesBeforeClosingQuestion");

    /** The static translator instance to use for all messages of this type. */
    private static final Translator<Message> TRANSLATOR = new Translator<Message>(Message.class);

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
        return Message.TRANSLATOR.getLocalizedMessage(this);
    }

    /**
     * Determine the Locales for which message files are provided. This excludes the default translation.
     * 
     * @return Locales with explicit translations
     */
    public static List<Locale> getAvailableLocales() {
        return Translator.getAvailableLocales(Message.class);
    }

    @Override
    public String toString() {
        return this.get();
    }
}

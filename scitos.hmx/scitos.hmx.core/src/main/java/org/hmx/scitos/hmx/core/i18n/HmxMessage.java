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

package org.hmx.scitos.hmx.core.i18n;

import org.hmx.scitos.core.i18n.ILocalizableMessage;
import org.hmx.scitos.core.i18n.Translator;

/** Enum to avoid hard coded language file keys in the acting classes. */
public enum HmxMessage implements ILocalizableMessage {

    PROJECT_NEW("Hmx.Project.New"),
    /** menubar entry: File -&gt; Export to -&gt; SVG. */
    EXPORT_TYPE_SVG("Export.SVG"),
    EXPORT_CONTENT("Export.Contents"),
    EXPORT_CONTENT_SYNTACTICAL("Export.Contents.SyntacticalAnalysis"),
    EXPORT_CONTENT_SEMANTICAL("Export.Contents.SemanticalAnalysis"),
    EXPORT_SETTINGS("Export.Settings"),
    EXPORT_SETTINGS_INCLUDE_COMMENTS("Export.Settings.IncludeComments"),
    EXPORT_CONTENT_COMMENTS("Export.Contents.Comments"),
    EXPORT_COMMENTS_WHICH_ALL("Export.Contents.Comments.Which.All"),
    EXPORT_COMMENTS_WHICH_SYNTACTICAL("Export.Contents.Comments.Which.Syntactical"),
    EXPORT_COMMENTS_WHICH_SEMANTICAL("Export.Contents.Comments.Which.Semantical"),
    EXPORT_START("Export.Start"),
    EXPORT_SUCCESS("Export.Success"),

    /** menubar entry: Edit -&gt; Project Informations. */
    MENUBAR_PROJECTINFO("Client.MenuBar.Edit.ProjectInfo"),
    /** menubar entry: Edit -&gt; Add new Propositions. */
    MENUBAR_ORIGINTEXT_ADD("Client.MenuBar.Edit.OriginText.Add"),
    /** menubar entry: Edit -&gt; Remove Propositions. */
    MENUBAR_ORIGINTEXT_REMOVE("Client.MenuBar.Edit.OriginText.Remove"),
    /** confirmation question before removing propositions. */
    MENUBAR_ORIGINTEXT_REMOVE_CONFIRM("Client.MenuBar.Edit.OriginText.Remove.Confirm"),
    /** menubar entry: Edit -&gt; Merge With Other Project. */
    MENUBAR_PROJECT_MERGE("Client.MenuBar.Edit.MergeProjects"),
    /** question where to insert the content of project to merge with. */
    MENUBAR_PROJECT_MERGE_POSITION("Client.MenuBar.Edit.MergeProjects.PositionQuestion"),
    /** answer: insert content of project to merge with in front of current text. */
    MENUBAR_PROJECT_MERGE_INFRONT("Client.MenuBar.Edit.MergeProjects.InFront"),
    /** answer: insert content of project to merge with behind current text. */
    MENUBAR_PROJECT_MERGE_BEHIND("Client.MenuBar.Edit.MergeProjects.Behind"),

    /** in the new-project-setup: the hint label over the main text input. */
    TEXTINPUT_TOPIC("TextInput.Topic"),
    /** alternative topic over main text input when adding more text to project. */
    TEXTINPUT_TOPIC_ADD_PROPOSITIONS("TextInput.Topic.AddPropositions"),
    /** in the new-project-setup: button to finish the setup process. */
    TEXTINPUT_START_BUTTON("TextInput.StartAnalysis"),
    /** in the new-project-setup: button to hide the setup area on the right. */
    TEXTINPUT_HIDE_BUTTON("TextInput.Settings.Hide"),
    /** in the new-project-setup: button to show the setup area on the right. */
    TEXTINPUT_SHOW_BUTTON("TextInput.Settings.Show"),
    /** in the new-project-setup: label for language selection. */
    TEXTINPUT_LANGUAGE("TextInput.Settings.OriginLanguage"),
    /** in the new-project-setup: additional hints on the right. */
    TEXTINPUT_HINT("TextInput.Hint"),
    /** in the new-project-setup: additional warnings on the right. */
    TEXTINPUT_WARNING("TextInput.Warning"),
    /** text input view: button to add new propositions in front of current text. */
    TEXTINPUT_BEFORE_BUTTON("TextInput.AddText.Before"),
    /** text input view: button to add new propositions behind current text. */
    TEXTINPUT_BEHIND_BUTTON("TextInput.AddText.Behind"),
    /** confirmation question: proceed closing of new project in setup mode. */
    TEXTINPUT_QUIT_QUESTION("TextInput.Quit.DiscardInputQuestion"),
    /**
     * title of the {@link #TEXTINPUT_QUIT_QUESTION} dialog.
     */
    TEXTINPUT_QUIT_TITLE("TextInput.Quit.DiscardInputTitle"),

    /**
     * title of the popup behind the {@link #MENUBAR_PROJECTINFO} entry.
     */
    PROJECTINFO_FRAME_TITLE("ProjectInfo.FrameTitle"),
    /**
     * topic of the popup behind the {@link #MENUBAR_PROJECTINFO} entry.
     */
    PROJECTINFO_TITLE("ProjectInfo.Title"),
    /**
     * author label in the {@link #MENUBAR_PROJECTINFO} popup.
     */
    PROJECTINFO_AUTHOR("ProjectInfo.Author"),
    /**
     * comment label in the {@link #MENUBAR_PROJECTINFO} popup.
     */
    PROJECTINFO_COMMENT("ProjectInfo.Comment"),

    /** label over the comment area in the analysis mode. */
    ANALYSIS_COMMENT_TOPIC("Analysis.CommentTopic"),

    /** button to switch to the syntactical analysis view. */
    ANALYSIS_SYNTACTICAL_BUTTON("Analysis.Syntactical"),
    /** syntactical context menu entry: change clause item function. */
    MENU_CHANGE_ITEM_FUNCTION("Analysis.Syntactical.ChangeItemFunction"),
    /** syntactical context menu entry: change indentation function. */
    MENU_CHANGE_PROP_FUNCTION("Analysis.Syntactical.ChangePropositionFunction"),
    /** syntactical context menu entry: merge clause item with its prior. */
    MENU_MERGE_ITEM_PRIOR("Analysis.Syntactical.MergeWithPriorItem"),
    /** syntactical context menu entry: merge clause item with its follower. */
    MENU_MERGE_ITEM_FOLLOWER("Analysis.Syntactical.MergeWithFollowingItem"),
    /** syntactical context menu entry: split clause item. */
    MENU_SPLIT_ITEM_AFTER("Analysis.Syntactical.SplitClauseItemAfter"),
    /** syntactical context menu entry: set clause item function style. */
    MENU_HIGHLIGHT_ITEM("Analysis.Syntactical.HighlightItem"),
    /** clause item function style sub menu: set to plain. */
    MENU_HIGHLIGHT_ITEM_PLAIN("Analysis.Syntactical.HighlightItem.Plain"),
    /** clause item function style sub menu: set to bold. */
    MENU_HIGHLIGHT_ITEM_BOLD("Analysis.Syntactical.HighlightItem.Bold"),
    /** clause item function style sub menu: set to italic. */
    MENU_HIGHLIGHT_ITEM_ITALIC("Analysis.Syntactical.HighlightItem.Italic"),
    /** clause item function style sub menu: set to bold and italic. */
    MENU_HIGHLIGHT_ITEM_BOLD_ITALIC("Analysis.Syntactical.HighlightItem.BoldAndItalic"),
    /** syntactical context menu entry: split proposition. */
    MENU_SPLIT_PROP("Analysis.Syntactical.SplitProposition"),
    /** syntactical context menu entry: merge propositions. */
    MENU_MERGE_CHECKED_PROP("Analysis.Syntactical.MergeCheckedPropositions"),
    /** syntactical context menu entry: indent proposition. */
    MENU_INDENT_PROP("Analysis.Syntactical.IndentProposition"),
    /** syntactical context menu entry: unindent proposition. */
    MENU_UNINDENT_PROP("Analysis.Syntactical.RemoveOneIndentation"),
    /** hint: the desired unindentation affects the indentation of others. */
    MENU_UNINDENT_PROP_COLATERAL("Analysis.Syntactical.RemoveOneIndentation.AffectsOthers"),
    /**
     * title of the {@link #MENU_UNINDENT_PROP_COLATERAL} hint message.
     */
    MENU_UNINDENT_PROP_COLATERAL_TITLE("Analysis.Syntactical.RemoveOneIndentation.AffectsOthers.Title"),
    /** syntactical context menu entry: split part-after-arrow from other part. */
    MENU_RESET_PROP_PART("Analysis.Syntactical.ResetStandaloneProposition"),

    /** button to switch to the syntactical analysis view. */
    ANALYSIS_SEMANTICAL_BUTTON("Analysis.Semantical"),
    /** semantical context menu entry: create relation. */
    MENU_CREATE_RELATION("Analysis.Semantical.CreateRelation"),
    /** semantical context menu entry: rotate roles of relation parts. */
    MENU_ROTATE_RELATION_ROLES("Analysis.Semantical.RotateAssociateRoles"),
    /** semantical context menu entry: change relation type. */
    MENU_ALTER_RELATION_TYPE("Analysis.Semantical.AlterRelationType"),
    /** semantical context menu entry: remove relation. */
    MENU_KILL_RELATION("Analysis.Semantical.RemoveRelation"),
    /** semantical analysis view check box tooltip: fold relations on single level. */
    MENU_FOLD_RELATION_LEVEL("Analysis.Semantical.RelationFolding.FoldLevel"),
    /** semantical analysis view check box tooltip: unfold relations on single level. */
    MENU_UNFOLD_RELATION_LEVEL("Analysis.Semantical.RelationFolding.UnfoldLevel"),
    /** semantical analysis view button: fold all relations. */
    MENU_FOLD_RELATION_ALL("Analysis.Semantical.RelationFolding.FoldAll"),
    /** semantical analysis view button: unfold all relations. */
    MENU_UNFOLD_RELATION_ALL("Analysis.Semantical.RelationFolding.UnfoldAll"),

    /** export related labels. */
    EXPORT_TITLE("Export.Title"),
    EXPORT_TITLE_AUTHOR("Export.Title.Author"),

    EXPORT_HEIGHT("Export.Height"),
    EXPORT_WIDTH("Export.Width"),

    /** error messages. */
    ERROR_INDENT("Error.Indentation.Create"),
    ERROR_INDENT_UNDER_OWN_CHILD("Error.Indentation.CreateUnderOwnChild"),
    ERROR_UNINDENT_PERICOPE("Error.Indentation.Remove.PericopeReached"),
    ERROR_UNINDENT_ENCLOSED("Error.Indentation.Remove.Enclosed"),
    ERROR_CHECK_MORE_PROPS_TO_INDENT("Error.IndentProposition.AtLeastOneOtherChecked"),
    ERROR_CHECK_LESS_PROPS("Error.TooManyChecked"),
    ERROR_MERGE_PROPS("Error.MergePropositions"),
    ERROR_CHECK_MORE_PROPS_TO_MERGE("Error.MergeCheckedPropositions.AtLeastOneOtherChecked"),
    ERROR_SPLIT_PROP("Error.SplitProposition"),
    ERROR_MERGE_ITEMS_NO_PRIOR("Error.MergeClauseItems.NoPrior"),
    ERROR_MERGE_ITEMS_NO_FOLLOWER("Error.MergeClauseItems.NoFollower"),
    ERROR_RELATION_CHECK_MORE("Error.CreateRelation.AtLeastTwoChecked"),
    ERROR_RELATION_CHECK_LESS("Error.CreateRelation.MoreThenTwoChecked"),
    ERROR_RELATION_UNCONNECTED("Error.CreateRelation.NotConnected"),
    ERROR_MERGE_NOT_A_PERICOPE("Error.MergePericopes.FileContainsNoPericope"),
    ERROR_MERGE_PERICOPES_LANGUAGE_CONFLICT("Error.MergePericopes.LanguageConflict"),
    ERROR_PROPOSITIONS_DELETE_ALL_SELECTED("Error.DeletePropositions.AllSelected"),
    ERROR_PROPOSITIONS_DELETE_NONE_SELECTED("Error.DeletePropositions.NoneSelected"),
    ERROR_PROPOSITIONS_DELETE_CONDITIONS_NOT_MET("Error.DeletePropositions.ConditionsNotMet"),
    ERROR_PREFERENCES_RELATION_HIGHWEIGHT_ROLE("Error.Preferences.Relation.MandatoryHighWeightRole"),
    ERROR_PREFERENCES_RELATION_ROLES("Error.Preferences.Relation.MandatoryRoles"),

    /** Setting: label for font (type) selection. */
    SETTING_FONT_TYPE("Setting.Font.Type"),
    /** Setting: label for font size selection. */
    SETTING_FONT_SIZE("Setting.Font.Size"),
    /** The sample text to display the current selection on when setting a font. */
    FONT_SAMPLE_TEXT("Setting.Font.SampleText"),
    SETTING_CHANGE_COLOR("Preferences.View.Color.Change"),
    SETTING_SET_TRANSPARENT("Preferences.View.Color.Transparent"),
    /** user preferences. */
    PREFERENCES_GENERAL("Preferences.General"),
    PREFERENCES_GENERAL_ARROW_COLOR("Preferences.View.Color.Arrow"),
    PREFERENCES_GENERAL_RELATION_COLOR("Preferences.View.Color.Relation"),
    PREFERENCES_GENERAL_COMMENTED_BORDER_COLOR("Preferences.View.Color.CommentedBorder"),
    PREFERENCES_GENERAL_INDENTATION("Preferences.View.IndentationWidth"),
    PREFERENCES_GENERAL_INPUT("Preferences.View.TextInput"),
    PREFERENCES_GENERAL_INPUT_SHOW_SETTINGS("Preferences.View.TextInput.ShowSettings"),
    PREFERENCES_GENERAL_AUTHOR("Preferences.Analysis.ProjectInfo.DefaultAuthor"),
    PREFERENCES_GENERAL_LANGUAGE("Preferences.Analysis.TextInput.DefaultOriginLanguage"),

    PREFERENCES_EXPORT("Preferences.Export"),
    PREFERENCES_EXPORT_ELEMENTCOLOR("Preferences.Export.Color"),
    PREFERENCES_EXPORT_FONTCOLOR("Preferences.Export.FontColor"),
    PREFERENCES_EXPORT_ORIGINTEXT_COLOR("Preferences.Export.OriginText.FontColor"),
    PREFERENCES_EXPORT_TRANSLATION_COLOR("Preferences.Export.Translation.FontColor"),
    PREFERENCES_EXPORT_LABEL_COLOR("Preferences.Export.Label.FontColor"),
    PREFERENCES_EXPORT_SEMROLE_COLOR("Preferences.Export.SemRole.FontColor"),
    PREFERENCES_EXPORT_SYNFUNCTION_PLAIN_COLOR("Preferences.Export.SynFunction.Plain.FontColor"),
    PREFERENCES_EXPORT_SYNFUNCTION_BOLD_COLOR("Preferences.Export.SynFunction.Bold.FontColor"),
    PREFERENCES_EXPORT_SYNFUNCTION_BOLDITALIC_COLOR("Preferences.Export.SynFunction.BoldItalic.FontColor"),
    PREFERENCES_EXPORT_SYNFUNCTION_ITALIC_COLOR("Preferences.Export.SynFunction.Italic.FontColor"),
    PREFERENCES_EXPORT_PROPOSITION_BORDER("Preferences.Export.Proposition.BorderColor"),
    PREFERENCES_EXPORT_PROPOSITION_BACKGROUND("Preferences.Export.Proposition.BackgroundColor"),
    PREFERENCES_EXPORT_FONT("Preferences.Export.Font"),

    PREFERENCES_RELATION("Preferences.Relation"),
    PREFERENCES_RELATION_LONG("Preferences.Relation.Long"),
    PREFERENCES_RELATION_ADD("Preferences.Relation.Add"),
    PREFERENCES_RELATION_ADD_GROUP("Preferences.Relation.AddGroup"),
    PREFERENCES_RELATION_DEFAULTROLE_HIGHWEIGHT("Preferences.Relation.Add.DefaultRole.HighWeight"),
    PREFERENCES_RELATION_DEFAULTROLE_LOWWEIGHT("Preferences.Relation.Add.DefaultRole.LowWeight"),
    PREFERENCES_RELATION_MOVE_UP("Preferences.Relation.MoveUp"),
    PREFERENCES_RELATION_MOVE_DOWN("Preferences.Relation.MoveDown"),
    PREFERENCES_RELATION_REMOVE("Preferences.Relation.Remove"),
    PREFERENCES_RELATION_REMOVE_GROUP("Preferences.Relation.RemoveGroup"),
    PREFERENCES_RELATION_REMOVE_GROUP_CONFIRM("Preferences.Relation.RemoveGroup.Confirm"),
    PREFERENCES_RELATION_EDIT("Preferences.Relation.EditTemplate"),
    PREFERENCES_RELATION_APPLY("Preferences.Relation.ApplyChanges"),
    PREFERENCES_RELATION_DISCARD("Preferences.Relation.DiscardChanges"),
    PREFERENCES_RELATION_HIGH_WEIGHT_ROLE("Preferences.Relation.HighWeightRole"),
    PREFERENCES_RELATION_HIGH_WEIGHT_ONLY("Preferences.Relation.HighWeightOnly"),
    PREFERENCES_RELATION_HIGH_WEIGHT_POSITION("Preferences.Relation.HighWeightPosition"),
    PREFERENCES_RELATION_HIGH_WEIGHT_FIRST("Preferences.Relation.HighWeightPosition.First"),
    PREFERENCES_RELATION_HIGH_WEIGHT_LAST("Preferences.Relation.HighWeightPosition.Last"),
    PREFERENCES_RELATION_LOW_WEIGHT_ROLE("Preferences.Relation.LowWeightRole"),
    PREFERENCES_RELATION_LOW_WEIGHT_REPEAT("Preferences.Relation.LowWeightRepeatable"),
    PREFERENCES_RELATION_TOOLTIP("Preferences.Relation.Description"),
    
    PREFERENCES_LANGUAGE("Preferences.OriginLanguage"),
    PREFERENCES_LANGUAGE_EDIT("Preferences.OriginLanguage.EditEntry"),
    PREFERENCES_LANGUAGE_APPLY("Preferences.OriginLanguage.ApplyChanges"),
    PREFERENCES_LANGUAGE_DISCARD("Preferences.OriginLanguage.DiscardChanges"),
    PREFERENCES_LANGUAGE_EDIT_LANGUAGES("Preferences.OriginLanguage.EditLanguages"),
    PREFERENCES_LANGUAGE_NAME("Preferences.OriginLanguage.LanguageName"),
    PREFERENCES_LANGUAGE_NAME_MANDATORY("Preferences.OriginLanguage.LanguageName.Mandatory"),
    PREFERENCES_LANGUAGE_ORIENTATION("Preferences.OriginLanguage.TextOrientation"),
    PREFERENCES_LANGUAGE_ORIENTATION_LTR("Preferences.OriginLanguage.TextOrientation.LeftToRight"),
    PREFERENCES_LANGUAGE_ORIENTATION_RTL("Preferences.OriginLanguage.TextOrientation.RightToLeft"),
    PREFERENCES_LANGUAGE_FONTS("Preferences.OriginLanguage.RecommendedFonts"),
    PREFERENCES_LANGUAGE_SYSTEMMODEL("Preferences.OriginLanguage.SystemModel"),
    PREFERENCES_LANGUAGE_FUNCTION_COUNT("Preferences.OriginLanguage.FunctionCount"),
    PREFERENCES_LANGUAGE_CLONE_LANGUAGE("Preferences.OriginLanguage.CloneLanguage"),
    PREFERENCES_LANGUAGE_DELETE_LANGUAGE("Preferences.OriginLanguage.DeleteLanguage"),
    PREFERENCES_LANGUAGE_EDIT_FUNCTIONS("Preferences.OriginLanguage.EditFunctions"),
    PREFERENCES_LANGUAGEFUNCTIONS_ADD_FUNCTION("Preferences.OriginLanguageFunctions.Add"),
    PREFERENCES_LANGUAGEFUNCTIONS_ADD_GROUP("Preferences.OriginLanguageFunctions.AddGroup"),
    PREFERENCES_LANGUAGEFUNCTIONS_ADD_TOPLEVEL_GROUP("Preferences.OriginLanguageFunctions.AddTopLevelGroup"),
    PREFERENCES_LANGUAGEFUNCTIONS_MOVE_UP("Preferences.OriginLanguageFunctions.MoveUp"),
    PREFERENCES_LANGUAGEFUNCTIONS_MOVE_DOWN("Preferences.OriginLanguageFunctions.MoveDown"),
    PREFERENCES_LANGUAGEFUNCTIONS_REMOVE("Preferences.OriginLanguageFunctions.Remove"),
    PREFERENCES_LANGUAGEFUNCTIONS_REMOVE_GROUP("Preferences.OriginLanguageFunctions.RemoveGroup"),
    PREFERENCES_LANGUAGEFUNCTIONS_NAME("Preferences.OriginLanguageFunctions.Name"),
    PREFERENCES_LANGUAGEFUNCTIONS_NAME_MANDATORY("Preferences.OriginLanguageFunctions.Name.Mandatory"),
    PREFERENCES_LANGUAGEFUNCTIONS_CODE("Preferences.OriginLanguageFunctions.Code"),
    PREFERENCES_LANGUAGEFUNCTIONS_CODE_MANDATORY("Preferences.OriginLanguageFunctions.Code.Mandatory"),
    PREFERENCES_LANGUAGEFUNCTIONS_UNDERLINE("Preferences.OriginLanguageFunctions.Underline"),
    PREFERENCES_LANGUAGEFUNCTIONS_DESCRIPTION("Preferences.OriginLanguageFunctions.Description");

    /** The static translator instance to use for all messages of this type. */
    private static final Translator<HmxMessage> TRANSLATOR = new Translator<HmxMessage>(HmxMessage.class);

    /** The attribute's key as it can be found in the actual language files. */
    final String key;

    /**
     * Main constructor.
     *
     * @param messageKey
     *            actual key in the language file
     */
    private HmxMessage(final String messageKey) {
        this.key = messageKey;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String get() {
        return HmxMessage.TRANSLATOR.getLocalizedMessage(this);
    }

    @Override
    public String toString() {
        return this.get();
    }
}

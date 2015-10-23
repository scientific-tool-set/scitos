package org.hmx.scitos.hmx.view;

import java.awt.Font;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ICanHaveSyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.AbstractConnectable;
import org.hmx.scitos.hmx.domain.model.ClauseItem;
import org.hmx.scitos.hmx.domain.model.ClauseItem.Style;
import org.hmx.scitos.hmx.domain.model.Pericope;
import org.hmx.scitos.hmx.domain.model.Proposition;
import org.hmx.scitos.hmx.domain.model.Relation;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.hmx.domain.model.SyntacticalFunction;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.ContextMenuBuilder.CMenu;
import org.hmx.scitos.view.ContextMenuBuilder.CMenuItem;
import org.hmx.scitos.view.ContextMenuBuilder.CMenuItemAction;
import org.hmx.scitos.view.swing.MessageHandler;
import org.hmx.scitos.view.swing.MessageHandler.Choice;

/**
 * swing utility class creating all {@link ContextMenuBuilder}s for user interactions on the analysis elements
 */
public final class ContextMenuFactory {

    /** hidden constructor due to only static methods */
    private ContextMenuFactory() {
        // never called
    }

    /**
     * creates a {@link ContextMenuBuilder} with all entries related to the specified {@link ClauseItem} and the {@link Proposition} it is contained
     * in
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedItem
     *            designated popup owner
     * @return created popup
     */
    public static ContextMenuBuilder createSynItemPopup(final IPericopeView viewReference, final ClauseItem selectedItem) {
        final ContextMenuBuilder created = new ContextMenuBuilder();
        ContextMenuFactory.addFunctionChangeEntry(created, viewReference, HmxMessage.MENU_CHANGE_ITEM_FUNCTION, selectedItem, false);
        created.addSeparator();
        ContextMenuFactory.addSynItemEntries(created, viewReference, selectedItem);
        ContextMenuFactory.addSynPropositionEntries(created, viewReference, selectedItem.getParent());
        return created;
    }

    /**
     * creates a {@link ContextMenuBuilder} with all entries related to the specified {@link ClauseItem} and the option to reset standalone state of
     * the {@link Proposition} part
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedItem
     *            designated popup owner
     * @return created popup
     */
    public static ContextMenuBuilder createSynItemAfterArrowPopup(final IPericopeView viewReference, final ClauseItem selectedItem) {
        final ContextMenuBuilder created = new ContextMenuBuilder();
        ContextMenuFactory.addFunctionChangeEntry(created, viewReference, HmxMessage.MENU_CHANGE_ITEM_FUNCTION, selectedItem, false);
        created.addSeparator();
        ContextMenuFactory.addSynItemEntries(created, viewReference, selectedItem);
        created.addSeparator();
        ContextMenuFactory.addResetStandalonePropositionEntry(created, viewReference, selectedItem.getParent());
        return created;
    }

    /**
     * creates a {@link ContextMenuBuilder} with all entries related to the specified {@link Proposition}
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedProposition
     *            designated popup owner
     * @return created popup
     */
    public static ContextMenuBuilder createSynPropositionPopup(final IPericopeView viewReference, final Proposition selectedProposition) {
        final ContextMenuBuilder created = new ContextMenuBuilder();
        ContextMenuFactory.addSynPropositionEntries(created, viewReference, selectedProposition);
        return created;
    }

    /**
     * creates a {@link ContextMenuBuilder} with only one entry to reset the standalone state of the {@link Proposition} part
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedProposition
     *            designated popup owner
     * @return created popup
     */
    public static ContextMenuBuilder createSynPropositionAfterArrowPopup(final IPericopeView viewReference, final Proposition selectedProposition) {
        final ContextMenuBuilder created = new ContextMenuBuilder();
        ContextMenuFactory.addResetStandalonePropositionEntry(created, viewReference, selectedProposition);
        return created;
    }

    /**
     * creates a {@link ContextMenuBuilder} with only one entry to create a {@link Relation} over the clicked {@link Proposition}
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedConnectable
     *            designated popup owner
     * @return created popup
     */
    public static ContextMenuBuilder createSemPropositionPopup(final IPericopeView viewReference, final AbstractConnectable selectedConnectable) {
        final ContextMenuBuilder created = new ContextMenuBuilder();
        ContextMenuFactory.addCreateOrAlterRelationEntry(created, viewReference, selectedConnectable, false);
        return created;
    }

    /**
     * creates a {@link ContextMenuBuilder} with four entries to create a {@link Relation} over the clicked {@link Relation}, rotate its roles, change
     * its type or to delete it
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedRelation
     *            designated popup owner
     * @return created popup
     */
    public static ContextMenuBuilder createSemRelationPopup(final IPericopeView viewReference, final Relation selectedRelation) {
        final ContextMenuBuilder created = new ContextMenuBuilder();
        ContextMenuFactory.addCreateOrAlterRelationEntry(created, viewReference, selectedRelation, false);
        created.addItem(HmxMessage.MENU_ROTATE_RELATION_ROLES.get(), new CMenuItemAction() {

            @Override
            public void processSelectEvent() throws HmxException {
                viewReference.submitChangesToModel();
                viewReference.getModelHandler().rotateAssociateRoles(selectedRelation);
            }
        });
        ContextMenuFactory.addCreateOrAlterRelationEntry(created, viewReference, selectedRelation, true);
        created.addSeparator();
        created.addItem(HmxMessage.MENU_KILL_RELATION.get(), new CMenuItemAction() {

            @Override
            public void processSelectEvent() throws HmxException {
                viewReference.submitChangesToModel();
                viewReference.getModelHandler().removeRelation(selectedRelation);
            }
        });
        return created;
    }

    /**
     * adds the sub menu for changing the {@link ClauseItem}s (or {@link Proposition}s) function to the designated {@link ContextMenuBuilder};
     * <p>
     * MODE1: <code>(target != null)</code> sets the function of the targeted clause item;<br>
     * MODE2: <code>(target == null)</code> indents the represented synProposition and sets its function
     *
     * @param popupMenu
     *            popup to contain the new sub menu
     * @param menuTextKey
     *            message key for the name to set for the new sub menu
     * @param target
     *            {@link ClauseItem} of which the function should be changed
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param indentProp
     *            if the proposition should be indented under checked
     */
    private static void addFunctionChangeEntry(final ContextMenuBuilder popupMenu, final IPericopeView viewReference,
            final HmxMessage menuTextKey, final ICanHaveSyntacticalFunction target, final boolean indentProp) {
        final CMenu functionSubMenu = popupMenu.addMenu(menuTextKey.get());
        boolean addSeparator = false;
        for (final List<SyntacticalFunction> singleGroup : viewReference.getModelHandler().getModel().provideFunctions()) {
            if (addSeparator) {
                functionSubMenu.addSeparator();
            } else {
                addSeparator = true;
            }
            ContextMenuFactory.addSyntacticalFunctionEntries(functionSubMenu, target, singleGroup, viewReference, indentProp);
        }
    }

    /**
     * adds multiple entries for different syntactical functions to the popup sub menu for setting the functions
     *
     * @param changeFunctionMenu
     *            sub menu to contain the entries
     * @param target
     *            item of which the function should be changed
     * @param functions
     *            map of all functions to add with their translations, short forms and help texts
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param indentProp
     *            if the proposition should be indented under checked
     */
    private static void addSyntacticalFunctionEntries(final CMenu changeFunctionMenu, final ICanHaveSyntacticalFunction target,
            final List<SyntacticalFunction> functions, final IPericopeView viewReference, final boolean indentProp) {
        for (final SyntacticalFunction singleFunction : functions) {
            final String description = singleFunction.getDescription();
            if (singleFunction.isSelectable()) {
                final CMenuItem menuEntry = changeFunctionMenu.addItem(singleFunction.getName(), new CMenuItemAction() {

                    @Override
                    public void processSelectEvent() throws HmxException {
                        viewReference.submitChangesToModel();
                        if (target instanceof Proposition && indentProp) {
                            ContextMenuFactory.indentProposition(viewReference, (Proposition) target, singleFunction);
                        } else {
                            viewReference.getModelHandler().setSyntacticalFunction(target, singleFunction);
                        }
                    }
                });
                if (description != null && !description.isEmpty()) {
                    menuEntry.setToolTip(description);
                }
            } else {
                final CMenu subMenu = changeFunctionMenu.addMenu(singleFunction.getName());
                ContextMenuFactory.addSyntacticalFunctionEntries(subMenu, target, singleFunction.getSubFunctions(), viewReference, indentProp);
                if (description != null && !description.isEmpty()) {
                    subMenu.setToolTip(description);
                }
            }
        }
    }

    /**
     * adds the {@link ClauseItem} related entries to the designated {@link ContextMenuBuilder}
     *
     * @param popupMenu
     *            popup to contain the new entries
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param item
     *            {@link ClauseItem} to be changed
     */
    private static void addSynItemEntries(final ContextMenuBuilder popupMenu, final IPericopeView viewReference, final ClauseItem item) {
        final List<ClauseItem> itemList = item.getParent().getItems();
        final int itemIndex = ComparisonUtil.indexOfInstance(itemList, item);
        // merge specified clause item with its prior clause item
        if (itemIndex > 0) {
            popupMenu.addItem(HmxMessage.MENU_MERGE_ITEM_PRIOR.get(), new CMenuItemAction() {

                @Override
                public void processSelectEvent() throws HmxException {
                    viewReference.submitChangesToModel();
                    viewReference.getModelHandler().mergeClauseItemWithPrior(item);
                }
            });
        }
        final boolean isNotLastItemInParent = itemIndex + 1 < itemList.size();
        if (isNotLastItemInParent) {
            // merge specified clause item with its following clause item
            popupMenu.addItem(HmxMessage.MENU_MERGE_ITEM_FOLLOWER.get(), new CMenuItemAction() {

                @Override
                public void processSelectEvent() throws HmxException {
                    viewReference.submitChangesToModel();
                    viewReference.getModelHandler().mergeClauseItemWithFollower(item);
                }
            });
        }

        // split specified clause item after designated origin text part
        final String[] originText = item.getOriginText().trim().split("[ ]");
        // check if the clause item is splittable at all
        if (originText.length > 1) {
            final Font originTextFont = viewReference.getModelHandler().getModel().getFont();
            final CMenu splitItemMenu = popupMenu.addMenu(HmxMessage.MENU_SPLIT_ITEM_AFTER.get());
            // collect all possible split positions
            final StringBuffer partBuffer = new StringBuffer();
            for (int i = 1; i < originText.length; i++) {
                partBuffer.append(originText[i - 1]);
                final String possiblePart = partBuffer.toString();
                // add a single possible split to the splitItemMenu
                final CMenuItem possibleSplitEntry = splitItemMenu.addItem(possiblePart, new CMenuItemAction() {

                    @Override
                    public void processSelectEvent() throws HmxException {
                        viewReference.submitChangesToModel();
                        viewReference.getModelHandler().splitClauseItem(item, possiblePart);
                    }
                });
                possibleSplitEntry.setFont(originTextFont);
                // add whitespace before next part
                partBuffer.append(' ');
            }
        }
        ContextMenuFactory.addHighlightSynItemEntry(popupMenu, viewReference, item);
        popupMenu.addSeparator();

        if (isNotLastItemInParent) {
            // split proposition after specified clause item
            popupMenu.addItem(HmxMessage.MENU_SPLIT_PROP.get(), new CMenuItemAction() {

                @Override
                public void processSelectEvent() throws HmxException {
                    viewReference.submitChangesToModel();
                    viewReference.getModelHandler().splitProposition(item.getParent(), item);
                }
            });
        }
    }

    /**
     * adds the sub menu for changing the {@link ClauseItem}s {@link Font} style (PLAIN, BOLD, ITALIC, BOLD and ITALIC)
     *
     * @param popupMenu
     *            popup to contain the new sub menu
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param item
     *            {@link ClauseItem} to be highlighted
     */
    private static void addHighlightSynItemEntry(final ContextMenuBuilder popupMenu, final IPericopeView viewReference, final ClauseItem item) {
        final CMenu highlightEntry = popupMenu.addMenu(HmxMessage.MENU_HIGHLIGHT_ITEM.get());
        // set PLAIN
        final Map<Style, HmxMessage> styles = new LinkedHashMap<Style, HmxMessage>();
        styles.put(Style.PLAIN, HmxMessage.MENU_HIGHLIGHT_ITEM_PLAIN);
        styles.put(Style.BOLD, HmxMessage.MENU_HIGHLIGHT_ITEM_BOLD);
        styles.put(Style.ITALIC, HmxMessage.MENU_HIGHLIGHT_ITEM_ITALIC);
        styles.put(Style.BOLD_ITALIC, HmxMessage.MENU_HIGHLIGHT_ITEM_BOLD_ITALIC);
        for (final Entry<Style, HmxMessage> singleStyle : styles.entrySet()) {
            final Style clauseItemStyle = singleStyle.getKey();
            final CMenuItem menuItem = highlightEntry.addItem(styles.get(clauseItemStyle).get(), new CMenuItemAction() {

                @Override
                public void processSelectEvent() throws HmxException {
                    viewReference.submitChangesToModel();
                    viewReference.getModelHandler().setClauseItemFontStyle(item, clauseItemStyle);
                }
            });
            menuItem.setFont(ContextMenuFactory.getFontStyleValue(clauseItemStyle));
        }
    }

    /**
     * adds all entries to the {@link ContextMenuBuilder} that deals with the specified {@link Proposition}
     *
     * @param popupMenu
     *            popup to contain the new entries
     * @param viewReference
     *            viewReference containing the {@link Proposition}
     * @param proposition
     *            view represenation of the {@link Proposition} to be changed
     */
    private static void addSynPropositionEntries(final ContextMenuBuilder popupMenu, final IPericopeView viewReference,
            final Proposition proposition) {
        // merge propositions
        popupMenu.addItem(HmxMessage.MENU_MERGE_CHECKED_PROP.get(), new CMenuItemAction() {

            @Override
            public void processSelectEvent() throws HmxException {
                viewReference.submitChangesToModel();
                ContextMenuFactory.mergeSynPropositions(viewReference, proposition);
            }
        });
        // indent proposition under parent
        ContextMenuFactory.addFunctionChangeEntry(popupMenu, viewReference, HmxMessage.MENU_INDENT_PROP, proposition, true);
        // change function of indented proposition
        if (proposition.getParent() instanceof Proposition) {
            popupMenu.addSeparator();
            ContextMenuFactory.addFunctionChangeEntry(popupMenu, viewReference, HmxMessage.MENU_CHANGE_PROP_FUNCTION, proposition, false);
            // remove an indentation
            popupMenu.addItem(HmxMessage.MENU_UNINDENT_PROP.get(), new CMenuItemAction() {

                @Override
                public void processSelectEvent() throws HmxException {
                    viewReference.submitChangesToModel();
                    ContextMenuFactory.removeOneIndentation(viewReference, proposition);
                }
            });
        }
    }

    /**
     * adds a single entry for resetting the standalone state of the {@link Proposition} part
     *
     * @param popup
     *            popup to contain the new entry
     * @param viewReference
     *            viewReference containing the {@link Proposition}
     * @param proposition
     *            view representation of the {@link Proposition} part to reset
     */
    private static void addResetStandalonePropositionEntry(final ContextMenuBuilder popup, final IPericopeView viewReference,
            final Proposition proposition) {
        popup.addItem(HmxMessage.MENU_RESET_PROP_PART.get(), new CMenuItemAction() {

            @Override
            public void processSelectEvent() throws HmxException {
                viewReference.submitChangesToModel();
                viewReference.getModelHandler().resetStandaloneStateOfPartAfterArrow(proposition);
            }
        });
    }

    /**
     * tries to merge the selected {@link Proposition}s in the syntactical analysis view
     *
     * @param viewReference
     *            panel representing the syntactical analysis
     * @param selectedProposition
     *            view representation of the {@link Proposition} to merge
     * @throws HmxException
     *             invalid user input
     */
    static void mergeSynPropositions(final IPericopeView viewReference, final Proposition selectedProposition) throws HmxException {
        // get all checked propositions on the syntactical analysis panel
        final List<Proposition> checked = viewReference.getSelectedPropositions(selectedProposition);
        final int size = checked.size();
        if (size == 2) {
            // the clicked proposition and only one other are checked
            viewReference.getModelHandler().mergePropositions(checked.get(0), checked.get(1));
        } else if (size < 2) {
            // no proposition (or only the clicked one) checked
            throw new HmxException(HmxMessage.ERROR_CHECK_MORE_PROPS_TO_MERGE);
        } else {
            // more than one other proposition checked
            throw new HmxException(HmxMessage.ERROR_CHECK_LESS_PROPS);
        }
    }

    /**
     * tries to indent the specified {@link Proposition} in the syntactical analysis view under the checked one
     *
     * @param viewReference
     *            panel representing the syntactical analysis
     * @param selectedProposition
     *            view representation of the {@link Proposition} to subordinate
     * @param function
     *            indentation function to set for clicked {@link Proposition}
     * @throws HmxException
     *             invalid user input
     */
    static void indentProposition(final IPericopeView viewReference, final Proposition selectedProposition, final SyntacticalFunction function)
            throws HmxException {
        // get all checked propositions on the syntactical analysis panel
        final List<Proposition> checked = viewReference.getSelectedPropositions(selectedProposition);
        final int size = checked.size();
        if (size == 2) {
            // the clicked proposition and only one other are checked
            Proposition target;
            if (checked.get(0) == selectedProposition) {
                target = checked.get(1);
            } else {
                target = checked.get(0);
            }
            viewReference.getModelHandler().indentPropositionUnderParent(target, selectedProposition, function);
        } else if (size < 2) {
            // no proposition (or only the clicked one) checked
            throw new HmxException(HmxMessage.ERROR_CHECK_MORE_PROPS_TO_INDENT);
        } else {
            // more than one other proposition checked
            throw new HmxException(HmxMessage.ERROR_CHECK_LESS_PROPS);
        }
    }

    /**
     * removes one indentation of the specified {@link Proposition} in the viewReference, if the user agrees to possible effects on other propositions
     *
     * @param viewReference
     *            viewReference containing the {@link Proposition}
     * @param proposition
     *            view representation of the {@link Proposition} of which one indentation should be removed
     * @throws HmxException
     *             invalid user input
     */
    static void removeOneIndentation(final IPericopeView viewReference, final Proposition proposition) throws HmxException {
        if (proposition.getParent() instanceof Pericope) {
            throw new HmxException(HmxMessage.ERROR_UNINDENT_PERICOPE);
        }
        Proposition parent = ((Proposition) proposition.getParent()).getFirstPart();
        while (parent.getPartAfterArrow() != null) {
            List<Proposition> enclosed = parent.getLaterChildren();
            if (enclosed != null && ComparisonUtil.containsInstance(enclosed, proposition)) {
                throw new HmxException(HmxMessage.ERROR_UNINDENT_ENCLOSED);
            }
            parent = parent.getPartAfterArrow();
            enclosed = parent.getPriorChildren();
            if (enclosed != null && ComparisonUtil.containsInstance(enclosed, proposition)) {
                throw new HmxException(HmxMessage.ERROR_UNINDENT_ENCLOSED);
            }
        }
        if (!viewReference.getModelHandler().removeOneIndentationAffectsOthers(proposition)
                || Choice.YES == MessageHandler.showYesNoCancelDialog(HmxMessage.MENU_UNINDENT_PROP_COLATERAL.get(),
                        HmxMessage.MENU_UNINDENT_PROP_COLATERAL_TITLE.get())) {
            viewReference.getModelHandler().removeOneIndentation(proposition);
        }
    }

    private static void addCreateOrAlterRelationEntry(final ContextMenuBuilder popup, final IPericopeView viewReference,
            final AbstractConnectable selectedConnectable, final boolean onlyAlterType) {
        final String menuLabel = onlyAlterType ? HmxMessage.MENU_ALTER_RELATION_TYPE.get() : HmxMessage.MENU_CREATE_RELATION.get();
        final CMenu createOrAlterSubMenu = popup.addMenu(menuLabel);
        final int associateCount = onlyAlterType ? ((Relation) selectedConnectable).getAssociates().size() : -1;
        boolean insertSeparator = false;
        final Locale localeForStringConversion = Option.TRANSLATION.getValueAsLocale();
        for (final List<RelationTemplate> singleGroup : viewReference.provideRelationTemplates()) {
            for (final RelationTemplate singleRelation : singleGroup) {
                if (associateCount > 2 && !singleRelation.canHaveMoreThanTwoAssociates()) {
                    continue;
                }
                if (insertSeparator) {
                    createOrAlterSubMenu.addSeparator();
                    insertSeparator = false;
                }
                final int roleCount;
                if (onlyAlterType) {
                    roleCount = associateCount;
                } else if (singleRelation.canHaveMoreThanTwoAssociates()) {
                    roleCount = 3;
                } else {
                    roleCount = 2;
                }
                final List<AssociateRole> roles = singleRelation.getAssociateRoles(roleCount);
                final String itemLabel = ContextMenuFactory.buildRelationLabel(roles, onlyAlterType, localeForStringConversion);
                final CMenuItem singleRelationEntry = createOrAlterSubMenu.addItem(itemLabel, new CMenuItemAction() {

                    @Override
                    public void processSelectEvent() throws HmxException {
                        viewReference.submitChangesToModel();
                        if (onlyAlterType) {
                            ContextMenuFactory.alterRelationType(viewReference, (Relation) selectedConnectable, singleRelation);
                        } else {
                            ContextMenuFactory.createRelation(viewReference, selectedConnectable, singleRelation);
                        }
                    }
                });
                final String helpText = singleRelation.getDescription();
                if (helpText != null && !helpText.isEmpty()) {
                    singleRelationEntry.setToolTip(helpText);
                }
            }
            // insert a separator after each group (but avoid multiple separators because of empty groups)
            insertSeparator = true;
        }
    }

    private static String buildRelationLabel(final List<AssociateRole> roles, final boolean onlyAlterType, final Locale localeForStringConversion) {
        final Map<AssociateRole, AtomicInteger> occurrences = ComparisonUtil.countOccurrences(roles);
        final StringBuilder itemLabel = new StringBuilder();
        if (!onlyAlterType && occurrences.size() == 1) {
            String roleText = roles.get(0).getRole();
            if (roles.get(0).isHighWeight()) {
                roleText = roleText.toUpperCase(localeForStringConversion);
            }
            // the relation components got the same weights
            itemLabel.append(roleText).append(1).append(" - ").append(roleText).append(2);
            if (roles.size() > 2) {
                itemLabel.append(" - ...");
            }
        } else {
            final Map<AssociateRole, Integer> indices = new HashMap<AssociateRole, Integer>();
            for (final AssociateRole singleRole : roles) {
                Integer index = indices.get(singleRole);
                if (itemLabel.length() > 0) {
                    itemLabel.append(" - ");
                }
                if (onlyAlterType || index == null) {
                    if (singleRole.isHighWeight()) {
                        itemLabel.append(singleRole.getRole().toUpperCase(localeForStringConversion));
                    } else {
                        itemLabel.append(singleRole.getRole());
                    }
                    if (occurrences.get(singleRole).intValue() > 1) {
                        if (index == null) {
                            index = 1;
                        } else {
                            index = index + 1;
                        }
                        itemLabel.append(index);
                        indices.put(singleRole, index);
                    }
                } else if (index == 1) {
                    itemLabel.append("...");
                    indices.put(singleRole, 2);
                }
            }
        }
        return itemLabel.toString();
    }

    /**
     * create a new {@link Relation} of the specified kind over the selected {@link AbstractConnectable}s
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedConnectable
     *            designated popup owner
     * @param template
     *            the template (associate roles and weights) to apply
     * @throws HmxException
     *             invalid user input
     */
    static void createRelation(final IPericopeView viewReference, final AbstractConnectable selectedConnectable, final RelationTemplate template)
            throws HmxException {
        final List<? extends AbstractConnectable> associates = viewReference.getSelectedConnectables(selectedConnectable);
        final int associateCount = associates.size();
        if (associateCount < 2) {
            throw new HmxException(HmxMessage.ERROR_RELATION_CHECK_MORE);
        }
        if (associateCount > 2 && !template.canHaveMoreThanTwoAssociates()) {
            throw new HmxException(HmxMessage.ERROR_RELATION_CHECK_LESS);
        }
        viewReference.getModelHandler().createRelation(associates, template);
    }

    /**
     * changes the type of the designated {@link Relation} to the chosen one represented by the language file key
     *
     * @param viewReference
     *            the view the request for this context menu originated from
     * @param selectedRelation
     *            {@link Relation} to change the type of
     * @param template
     *            the template (associate roles and weights) to apply
     */
    static void alterRelationType(final IPericopeView viewReference, final Relation selectedRelation, final RelationTemplate template) {
        viewReference.getModelHandler().alterRelationType(selectedRelation, template);
    }

    /**
     * Get the matching {@link Font} style value for the given style option.
     *
     * @param style
     *            the option to get the {@link Font} style value for
     * @return matching {@link Font} style value
     */
    public static int getFontStyleValue(final Style style) {
        switch (style) {
        case BOLD:
            return Font.BOLD;
        case ITALIC:
            return Font.ITALIC;
        case BOLD_ITALIC:
            return Font.BOLD | Font.ITALIC;
        default:
            return Font.PLAIN;
        }
    }
}

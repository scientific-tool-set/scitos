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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.tree.TreePath;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISyntacticalFunctionProvider;
import org.hmx.scitos.hmx.domain.model.originlanguage.AbstractSyntacticalElement;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunction;
import org.hmx.scitos.hmx.domain.model.originlanguage.SyntacticalFunctionGroup;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 * TreeTable data model of a {@link ISyntacticalFunctionProvider}. It allows the adding/updating/removing of {@link SyntacticalFunctionGroup}s and
 * contained {@link SyntacticalFunction}s.
 * <ol start=0>
 * Manages data and buttons in a number of columns:
 * <li>Name</li>
 * <li>Code</li>
 * <li>Description</li>
 * <li>Button: add function to group (empty for function entry)</li>
 * <li>Button: add group to group (empty for function entry)</li>
 * <li>Button: move function/group up</li>
 * <li>Button: move function/group down</li>
 * <li>Button: remove function/group</li>
 * </ol>
 */
final class SynFunctionTreeModel extends AbstractTreeTableModel implements ISyntacticalFunctionProvider {

    /** Default initial state for a new syntactical function. */
    private static final SyntacticalFunction DEFAULT_NEW_FUNCTION = new SyntacticalFunction("", "", false, "");
    /** Default initial state for a new function group. */
    private static final SyntacticalFunctionGroup DEFAULT_NEW_GROUP = new SyntacticalFunctionGroup("", "",
            Collections.<AbstractSyntacticalElement>emptyList());
    /**
     * Representation of the lists of {@link AbstractSyntacticalElement}s on the top level.
     */
    private final Map<UUID, List<UUID>> topLevelGrouping = new LinkedHashMap<UUID, List<UUID>>();
    /**
     * Collection of all {@link AbstractSyntacticalElement}s represented here - regardless of their positions in the hierarchy.
     */
    private final Map<UUID, TreeTableRow> rowMapping = new HashMap<UUID, TreeTableRow>();

    /** Constructor. */
    public SynFunctionTreeModel() {
        super(new Object());
    }

    /**
     * Reset the currently displayed syntactical functions for the ones contained in the given provider.
     * 
     * @param provider
     *            the provider for the syntactical functions/functions groups to represent
     */
    public void reset(final ISyntacticalFunctionProvider provider) {
        this.topLevelGrouping.clear();
        this.rowMapping.clear();
        for (final List<AbstractSyntacticalElement> singleTopLevelGroup : provider.provideFunctions()) {
            final UUID reference = UUID.randomUUID();
            final List<UUID> topLevelChildren = new ArrayList<UUID>(singleTopLevelGroup.size());
            for (final AbstractSyntacticalElement singleTopLevelChild : singleTopLevelGroup) {
                topLevelChildren.add(this.addTreeTableRow(reference, singleTopLevelChild));
            }
            this.topLevelGrouping.put(reference, topLevelChildren);
        }
        this.modelSupport.fireTreeStructureChanged(new TreePath(this.getRoot()));
    }

    /**
     * Add a row to the internal mapping that is equivalent to the given model element.
     * 
     * @param parent
     *            reference to the super ordinated row or group
     * @param modelElement
     *            the {@link SyntacticalFunction} or {@link SyntacticalFunctionGroup} to represent
     * @return the internal reference to the added row
     */
    private UUID addTreeTableRow(final UUID parent, final AbstractSyntacticalElement modelElement) {
        final UUID reference = UUID.randomUUID();
        final TreeTableRow row;
        if (modelElement instanceof SyntacticalFunction) {
            row = new SynFunctionRow(parent, (SyntacticalFunction) modelElement);
        } else {
            row = new GroupRow(parent, (SyntacticalFunctionGroup) modelElement);
            for (final AbstractSyntacticalElement singleChild : ((SyntacticalFunctionGroup) modelElement).getSubFunctions()) {
                ((GroupRow) row).subEntries.add(this.addTreeTableRow(reference, singleChild));
            }
        }
        this.rowMapping.put(reference, row);
        return reference;
    }

    @Override
    public List<List<AbstractSyntacticalElement>> provideFunctions() {
        final List<List<AbstractSyntacticalElement>> result;
        result = new ArrayList<List<AbstractSyntacticalElement>>(this.topLevelGrouping.size());
        for (final List<UUID> singleGroup : this.topLevelGrouping.values()) {
            final List<AbstractSyntacticalElement> topLevelList = new ArrayList<AbstractSyntacticalElement>(singleGroup.size());
            for (final UUID singleTopLevelReference : singleGroup) {
                topLevelList.add(this.deriveModelInstance(singleTopLevelReference));
            }
            result.add(topLevelList);
        }
        return result;
    }

    /**
     * Recursive conversion of mutable tree table rows to their immutable model equivalents.
     * 
     * @param rowReference
     *            the reference to the row to be transfered into an equivalent {@link SyntacticalFunction} or {@link SyntacticalFunctionGroup}
     * @return equivalent model element
     */
    private AbstractSyntacticalElement deriveModelInstance(final UUID rowReference) {
        final TreeTableRow row = this.rowMapping.get(rowReference);
        if (row instanceof GroupRow) {
            final List<AbstractSyntacticalElement> subFunctions;
            subFunctions = new ArrayList<AbstractSyntacticalElement>(((GroupRow) row).subEntries.size());
            for (final UUID singleSubEntry : ((GroupRow) row).subEntries) {
                subFunctions.add(this.deriveModelInstance(singleSubEntry));
            }
            return new SyntacticalFunctionGroup(row.getName(), row.getDescription(), subFunctions);
        }
        final SynFunctionRow functionRow = (SynFunctionRow) row;
        return new SyntacticalFunction(functionRow.getCode(), functionRow.getName(), functionRow.isUnderlined(), functionRow.getDescription());
    }

    @Override
    public int getColumnCount() {
        return 8;
    }

    @Override
    public boolean isCellEditable(final Object reference, final int columnIndex) {
        switch (columnIndex) {
        case 3:
            // Button: add function to group (empty for function entry)
            return !(this.rowMapping.get(reference) instanceof SynFunctionRow);
        case 4:
            // Button: add group to group (empty for function entry)
            return !(this.rowMapping.get(reference) instanceof SynFunctionRow);
        case 5:
            // Button: move function/group up
            return this.getChildren(this.getParent(reference)).indexOf(reference) > 0;
        case 6:
            // Button: move function/group down
            final List<UUID> siblings = this.getChildren(this.getParent(reference));
            return siblings.indexOf(reference) + 1 < siblings.size();
        case 7:
            // all functions/function groups/top level groups can be removed
            return true;
        default:
            return false;
        }
    }

    @Override
    public Object getValueAt(final Object reference, final int columnIndex) {
        if (reference == this.getRoot()) {
            return null;
        }
        if (this.topLevelGrouping.containsKey(reference)) {
            return this.getTopLevelGroupValueAt(reference, columnIndex);
        }
        if (this.rowMapping.containsKey(reference)) {
            return this.getEntryValueAt(reference, columnIndex);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Getter for a cell value on a row representing the referenced top level group.
     * 
     * @param reference
     *            the reference to the top level group a cell value should be looked up for
     * @param columnIndex
     *            the column's index whose value is to be returned
     * @return cell value to display in the designated column
     */
    private Object getTopLevelGroupValueAt(final Object reference, final int columnIndex) {
        switch (columnIndex) {
        case 0:
            final int topLevelIndex = this.getIndexOfChild(this.getRoot(), reference);
            return String.valueOf(topLevelIndex + 1) + '.';
        case 1:
            return "";
        case 2:
            return "";
        case 3:
            return HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_ADD_FUNCTION.get();
        case 4:
            return HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_ADD_GROUP.get();
        case 5:
            return this.isCellEditable(reference, columnIndex) ? HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_MOVE_UP.get() : null;
        case 6:
            return this.isCellEditable(reference, columnIndex) ? HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_MOVE_DOWN.get() : null;
        case 7:
            return HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE_GROUP.get();
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Getter for a cell value on a row representing the referenced function or function group.
     * 
     * @param reference
     *            the reference to the function or function group a cell value should be looked up for
     * @param columnIndex
     *            the column's index whose value is to be returned
     * @return cell value to display in the designated column
     */
    private Object getEntryValueAt(final Object reference, final int columnIndex) {
        final TreeTableRow row = this.rowMapping.get(reference);
        switch (columnIndex) {
        case 0:
            return row.getName();
        case 1:
            return row instanceof SynFunctionRow ? ((SynFunctionRow) row).getCode() : "";
        case 2:
            return row.getDescription();
        case 3:
            return this.isCellEditable(reference, columnIndex) ? HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_ADD_FUNCTION.get() : null;
        case 4:
            return this.isCellEditable(reference, columnIndex) ? HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_ADD_GROUP.get() : null;
        case 5:
            return this.isCellEditable(reference, columnIndex) ? HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_MOVE_UP.get() : null;
        case 6:
            return this.isCellEditable(reference, columnIndex) ? HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_MOVE_DOWN.get() : null;
        case 7:
            return row instanceof GroupRow ? HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE_GROUP.get()
                    : HmxMessage.PREFERENCES_LANGUAGEFUNCTIONS_REMOVE.get();
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public UUID getChild(final Object parentReference, final int childIndex) {
        return this.getChildren(parentReference).get(childIndex);
    }

    @Override
    public int getChildCount(final Object parentReference) {
        if (this.rowMapping.get(parentReference) instanceof SynFunctionRow) {
            return 0;
        }
        return this.getChildren(parentReference).size();
    }

    @Override
    public int getIndexOfChild(final Object parentReference, final Object childReference) {
        return this.getChildren(parentReference).indexOf(childReference);
    }

    /**
     * Look up the list of child references for the parent row with the given reference.
     * 
     * @param parentReference
     *            UUID referring to the parent row to return the list of references to its children for
     * @return references to the designated row's children
     */
    private List<UUID> getChildren(final Object parentReference) {
        final List<UUID> children;
        if (this.rowMapping.containsKey(parentReference)) {
            children = ((GroupRow) this.rowMapping.get(parentReference)).subEntries;
        } else if (this.topLevelGrouping.containsKey(parentReference)) {
            children = this.topLevelGrouping.get(parentReference);
        } else if (parentReference == this.getRoot()) {
            // make it unmodifiable to ensure that an exception is being thrown if this case is not treated specifically
            children = Collections.unmodifiableList(new ArrayList<UUID>(this.topLevelGrouping.keySet()));
        } else {
            throw new IllegalArgumentException();
        }
        return children;
    }

    /**
     * Getter for the referenced row's parent.
     * 
     * @param reference
     *            UUID referring to the row to look-up the parent for
     * @return the referenced row's parent
     */
    private Object getParent(final Object reference) {
        final Object parentReference;
        if (this.rowMapping.containsKey(reference)) {
            final TreeTableRow row = this.rowMapping.get(reference);
            parentReference = row.getParent();
        } else {
            parentReference = this.getRoot();
        }
        return parentReference;
    }

    /**
     * Add a row representing a {@link SyntacticalFunction} under the referenced parent group.
     * 
     * @param parentPath
     *            path to the group the new function should be added to
     * @return path to the new row
     */
    public TreePath addSynFunctionRow(final TreePath parentPath) {
        return this.addRow(parentPath, new SynFunctionRow((UUID) parentPath.getLastPathComponent(), DEFAULT_NEW_FUNCTION));
    }

    /**
     * Add a row representing a {@link SyntacticalFunctionGroup} under the referenced parent group.
     * 
     * @param parentPath
     *            path to the group the new function should be added to
     * @return path to the new row
     */
    public TreePath addGroupRow(final TreePath parentPath) {
        return this.addRow(parentPath, new GroupRow((UUID) parentPath.getLastPathComponent(), DEFAULT_NEW_GROUP));
    }

    /**
     * Add the given row under the referenced parent group.
     * 
     * @param parentPath
     *            path to the group the new function should be added to
     * @param newRow
     *            the function or group row to be added
     * @return path to the new row
     */
    private TreePath addRow(final TreePath parentPath, final TreeTableRow newRow) {
        final UUID childReference = UUID.randomUUID();
        final List<UUID> siblings = this.getChildren(parentPath.getLastPathComponent());
        siblings.add(childReference);
        this.rowMapping.put(childReference, newRow);
        this.modelSupport.fireChildAdded(parentPath, siblings.size() - 1, childReference);
        return parentPath.pathByAddingChild(childReference);
    }

    /**
     * Add a row representing an emptying grouping that can potentially hold top level elements ({@link SyntacticalFunction} or
     * {@link SyntacticalFunctionGroup}).
     * 
     * @return path to the new row
     */
    public TreePath addTopLevelGroup() {
        final UUID childReference = UUID.randomUUID();
        this.topLevelGrouping.put(childReference, new ArrayList<UUID>(3));
        final TreePath rootPath = new TreePath(this.getRoot());
        this.modelSupport.fireChildAdded(rootPath, this.topLevelGrouping.size() - 1, childReference);
        return rootPath.pathByAddingChild(childReference);
    }

    /**
     * Move a single function, function group, or top level group up or down by one step.
     * 
     * @param targetPath
     *            path to the designated entry to be moved
     * @param moveDown
     *            if the designated entry should be moved down; otherwise moved up
     */
    public void moveEntry(final TreePath targetPath, final boolean moveDown) {
        final UUID entry = (UUID) targetPath.getLastPathComponent();
        final TreePath parentPath = targetPath.getParentPath();
        if (this.topLevelGrouping.containsKey(entry)) {
            // the designated path refers to a top level group
            CollectionUtil.moveEntryInInsertSortedMap(this.topLevelGrouping, entry, moveDown);
        } else {
            // the designated path refers to a function or function group
            final List<UUID> entrySiblings = this.getChildren(parentPath.getLastPathComponent());
            final int index = entrySiblings.indexOf(entry);
            final int indexToSwitchWith;
            if (moveDown) {
                indexToSwitchWith = index + 1;
            } else {
                indexToSwitchWith = index - 1;
            }
            final UUID entryToSwitchWith = entrySiblings.set(indexToSwitchWith, entry);
            entrySiblings.set(index, entryToSwitchWith);
        }
        this.modelSupport.fireTreeStructureChanged(parentPath);
    }

    /**
     * Delete the function, function group, or top level group at the given path.
     * 
     * @param targetPath
     *            path to the entry to be deleted
     */
    public void removeEntry(final TreePath targetPath) {
        final Object entry = targetPath.getLastPathComponent();
        final TreePath parentPath = targetPath.getParentPath();
        final List<UUID> siblings = this.getChildren(parentPath.getLastPathComponent());
        final int index = siblings.indexOf(entry);
        if (targetPath.getPathCount() == 2) {
            // path refers to a top level group - remove all its contained functions and function groups with it
            this.removeSubEntries(entry);
            this.topLevelGrouping.remove(entry);
        } else {
            // path refers to a function or function group
            if (this.rowMapping.get(entry) instanceof GroupRow) {
                // remove the function group's entries with it
                this.removeSubEntries(entry);
            }
            this.rowMapping.remove(entry);
            siblings.remove(entry);
        }
        this.modelSupport.fireChildRemoved(parentPath, index, entry);
    }

    /**
     * Recursively delete the functions and function groups under the designated parent.
     * 
     * @param parentReference
     *            reference to the parent for which all children should be deleted
     */
    private void removeSubEntries(final Object parentReference) {
        final List<UUID> children = this.getChildren(parentReference);
        for (final UUID childReference : children) {
            final TreeTableRow childRow = this.rowMapping.get(childReference);
            if (childRow instanceof GroupRow) {
                this.removeSubEntries(childReference);
            }
            this.rowMapping.remove(childReference);
        }
        children.clear();
    }

    /**
     * Return the internal, mutable row representation at the given path - if that path refers to a function or function group.
     * 
     * @param path
     *            the path to look up the internal row object for
     * @return internal, mutable row representation (or {@code null})
     */
    TreeTableRow getFunctionOrGroupRowAtPath(final TreePath path) {
        if (path == null || path.getPathCount() < 3) {
            return null;
        }
        return this.rowMapping.get(path.getLastPathComponent());
    }

    /**
     * Notify all listeners that the row at the given path has been updated, i.e. its values might have changed.
     * 
     * @param updatedPath
     *            path to the updated row
     */
    public void updatedRow(final TreePath updatedPath) {
        final TreePath parentPath = updatedPath.getParentPath();
        final List<UUID> siblingReferences = this.getChildren(parentPath.getLastPathComponent());
        final Object rowReference = updatedPath.getLastPathComponent();
        final int index = siblingReferences.indexOf(rowReference);
        this.modelSupport.fireChildChanged(parentPath, index, rowReference);
    }

    /** Mutable representation of a syntactical function or function group. */
    abstract static class TreeTableRow {

        /** Reference to the super ordinated row or group. */
        private UUID parent;
        /** This function/group entry's representation in the context menu to select from. */
        private String name;
        /** The additional description to display as tooltip for this function/group entry. */
        private String description;

        /**
         * Constructor.
         * 
         * @param parent
         *            reference to the super ordinated row or group
         * @param modelElement
         *            the syntactical function or function group to represent
         */
        TreeTableRow(final UUID parent, final AbstractSyntacticalElement modelElement) {
            this.parent = parent;
            this.name = modelElement.getName();
            this.description = modelElement.getDescription();
        }

        /**
         * Getter for the reference to the super ordinated row or group.
         * 
         * @return the parent reference
         */
        public UUID getParent() {
            return this.parent;
        }

        /**
         * Getter for this function/group entry's representation in the context menu to select from.
         * 
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Setter for this function/group entry's representation in the context menu to select from.
         *
         * @param name
         *            the value to set
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Getter for the additional description to display as tooltip for this function/group entry.
         * 
         * @return the description
         */
        public String getDescription() {
            return this.description;
        }

        /**
         * Setter for the additional description to display as tooltip for this function/group entry.
         *
         * @param description
         *            the value to set
         */
        public void setDescription(final String description) {
            this.description = description;
        }
    }

    /** Mutable representation of a function group. */
    static class GroupRow extends TreeTableRow {

        /** The references to sub ordinated rows. */
        final List<UUID> subEntries = new ArrayList<UUID>(3);

        /**
         * Constructor.
         * 
         * @param parent
         *            reference to the super ordinated row or group
         * @param modelElement
         *            the function group to represent
         */
        GroupRow(final UUID parent, final SyntacticalFunctionGroup modelElement) {
            super(parent, modelElement);
        }
    }

    /** Mutable representation of a syntactical function. */
    static class SynFunctionRow extends TreeTableRow {

        /** The short representation for this function entry to be displayed when selected. */
        private String code;
        /** Indicator whether the represented syntactical function should be displayed as underlined. */
        private boolean underlined;

        /**
         * Constructor.
         * 
         * @param parent
         *            reference to the super ordinated row or group
         * @param modelElement
         *            the syntactical function to represent
         */
        SynFunctionRow(final UUID parent, final SyntacticalFunction modelElement) {
            super(parent, modelElement);
            this.code = modelElement.getCode();
            this.underlined = modelElement.isUnderlined();
        }

        /**
         * Getter for the short representation for this function entry to be displayed when selected.
         * 
         * @return the code
         */
        public String getCode() {
            return this.code;
        }

        /**
         * Setter for the short representation for this function entry to be displayed when selected.
         *
         * @param code
         *            the value to set
         */
        public void setCode(final String code) {
            this.code = code;
        }

        /**
         * Getter for the indicator whether the represented syntactical function should be displayed as underlined.
         * 
         * @return the flag's value
         */
        public boolean isUnderlined() {
            return this.underlined;
        }

        /**
         * Setter for the indicator whether the represented syntactical function should be displayed as underlined.
         *
         * @param underlined
         *            the value to set
         */
        public void setUnderlined(final boolean underlined) {
            this.underlined = underlined;
        }
    }
}

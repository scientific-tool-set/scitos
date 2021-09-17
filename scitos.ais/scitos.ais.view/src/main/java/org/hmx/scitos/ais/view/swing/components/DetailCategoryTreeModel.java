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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.hmx.scitos.ais.core.i18n.AisMessage;
import org.hmx.scitos.ais.domain.IDetailCategoryProvider;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 * TreeTable data model of a DetailCategory model. It allows the modification of the hierarchical structure as well as the individual category
 * attributes like code, name, color, and short cut.
 */
final class DetailCategoryTreeModel extends AbstractTreeTableModel {

    /** The detail category rows without any parent categories. */
    private final List<DetailCategoryRow> rootCategories;
    /** Mapping of parent category rows to their subordinated (i.e. child) category rows. */
    private final Map<DetailCategoryRow, List<DetailCategoryRow>> categoryChildren;

    /**
     * Main constructor.
     *
     * @param categoryProvider
     *            initial provider of the detail categories to represent
     */
    DetailCategoryTreeModel(final IDetailCategoryProvider categoryProvider) {
        super(new Object());
        final MutableDetailCategoryModel model = new MutableDetailCategoryModel();
        model.addAll(categoryProvider.provide());
        this.rootCategories = new ArrayList<>();
        this.categoryChildren = new HashMap<>();
        for (final DetailCategory singleRoot : model.getRootCategories()) {
            this.rootCategories.add(this.addCategoryTreeToMap(model, singleRoot));
        }
    }

    /**
     * Create the equivalent row object for the given detail category. Recursively add it with all its subordinated (i.e. child) categories to the
     * internal mapping. The specified model is used to find the subordinated categories in the hierarchy.
     *
     * @param model
     *            full detail category model containing the given category, used to determine subordinated categories
     * @param detail
     *            the detail category to include in internal mapping
     * @return row object representing the given category
     */
    private DetailCategoryRow addCategoryTreeToMap(final MutableDetailCategoryModel model, final DetailCategory detail) {
        final List<DetailCategoryRow> children = new ArrayList<>();
        for (final DetailCategory singleChild : model.getChildCategories(detail)) {
            children.add(this.addCategoryTreeToMap(model, singleChild));
        }
        final DetailCategoryRow detailRow = new DetailCategoryRow(detail);
        if (!children.isEmpty()) {
            this.categoryChildren.put(detailRow, children);
        }
        return detailRow;
    }

    /**
     * Add a new (blank) category row under the given path. If the path contains only the (invisible) root element, this creates a new root category
     * (without a parent).
     *
     * @param parent
     *            path containing all super ordinated elements (first path element is the single root element)
     * @return path to the newly created detail category row
     */
    TreePath addChildCategoryRow(final TreePath parent) {
        final DetailCategoryRow newRow = new DetailCategoryRow("", "", Color.BLACK, null, null);
        final List<DetailCategoryRow> parentsChildren;
        if (parent.getPathCount() == 1) {
            parentsChildren = this.rootCategories;
        } else {
            final Object parentRow = parent.getLastPathComponent();
            if (!this.categoryChildren.containsKey(parentRow)) {
                this.categoryChildren.put((DetailCategoryRow) parentRow, new ArrayList<>());
            }
            parentsChildren = this.categoryChildren.get(parentRow);
            // inherit color from parent
            newRow.highlightColor = ((DetailCategoryRow) parentRow).highlightColor;
        }
        parentsChildren.add(newRow);
        this.modelSupport.fireChildAdded(parent, parentsChildren.size() - 1, newRow);
        return parent.pathByAddingChild(newRow);
    }

    /**
     * Remove the category row at the given path.
     *
     * @param target
     *            full path to the element that is supposed to be removed from the model
     */
    void removeCategoryRow(final TreePath target) {
        final TreePath targetParent = target.getParentPath();
        if (targetParent.getPathCount() == 1) {
            this.rootCategories.remove(target.getLastPathComponent());
        } else {
            final Object targetParentRow = targetParent.getLastPathComponent();
            final List<DetailCategoryRow> targetSiblings = this.categoryChildren.get(targetParentRow);
            targetSiblings.remove(target.getLastPathComponent());
            if (targetSiblings.isEmpty()) {
                this.categoryChildren.remove(targetParentRow);
            }
        }
        this.modelSupport.fireTreeStructureChanged(targetParent);
    }

    @Override
    public DetailCategoryRow getChild(final Object parent, final int index) {
        if (parent instanceof DetailCategoryRow && this.categoryChildren.containsKey(parent)) {
            return this.categoryChildren.get(parent).get(index);
        }
        return this.rootCategories.get(index);
    }

    @Override
    public int getChildCount(final Object parent) {
        final int childCount;
        if (this.isLeaf(parent)) {
            childCount = 0;
        } else if (this.categoryChildren.containsKey(parent)) {
            childCount = this.categoryChildren.get(parent).size();
        } else {
            childCount = this.rootCategories.size();
        }
        return childCount;
    }

    @Override
    public boolean isLeaf(final Object node) {
        return node instanceof DetailCategoryRow && !this.categoryChildren.containsKey(node);
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        if (parent instanceof DetailCategoryRow && this.categoryChildren.containsKey(parent)) {
            return this.categoryChildren.get(parent).indexOf(child);
        }
        return this.rootCategories.indexOf(child);
    }

    /**
     * Determine the maximum number of hierarchy steps from the (invisible) root element to any of its descendants (i.e. detail categories).
     *
     * @return depth of the hierarchical structure
     */
    public int getTreeDepth() {
        int depth = 0;
        for (final DetailCategoryRow singleRootCategory : this.rootCategories) {
            depth = Math.max(depth, this.getSubTreeDepth(singleRootCategory));
        }
        return depth;
    }

    /**
     * Recursively determine the maximum number of hierarchy steps from the given detail category row to any of its descendants (i.e. subordinated
     * detail categories).
     *
     * @param subTreeRoot
     *            detail category row to determine the number of subordinated hierarchy levels for
     * @return depth of this part of the hierarchical structure
     */
    private int getSubTreeDepth(final DetailCategoryRow subTreeRoot) {
        int maxChildTreeDepth = 0;
        if (this.categoryChildren.containsKey(subTreeRoot)) {
            for (final DetailCategoryRow singleChild : this.categoryChildren.get(subTreeRoot)) {
                maxChildTreeDepth = Math.max(maxChildTreeDepth, this.getSubTreeDepth(singleChild));
            }
        }
        return maxChildTreeDepth + 1;
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        switch (columnIndex) {
        case 0:
            return AisMessage.DETAIL_CATEGORY_CODE.get();
        case 1:
            return AisMessage.DETAIL_CATEGORY_NAME.get();
        case 2:
            return AisMessage.DETAIL_CATEGORY_COLOR.get();
        case 3:
            return AisMessage.DETAIL_CATEGORY_SHORTCUT.get();
        default:
            // no column header for the buttons
            return "";
        }
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        final Class<?> clazz;
        if (columnIndex == 2) {
            // highlight color
            clazz = Color.class;
        } else if (columnIndex == 3) {
            // short cut
            clazz = KeyStroke.class;
        } else {
            // code, name, matched old category, and button tool tips
            clazz = String.class;
        }
        return clazz;
    }

    @Override
    public boolean isCellEditable(final Object node, final int columnIndex) {
        if (!(node instanceof DetailCategoryRow)) {
            return false;
        }
        return this.isLeaf(node) || columnIndex < 5 && columnIndex != 2 && columnIndex != 3;
    }

    @Override
    public Object getValueAt(final Object node, final int column) {
        if (this.isCellEditable(node, column)) {
            final DetailCategoryRow row = (DetailCategoryRow) node;
            switch (column) {
            case 0:
                return row.code;
            case 1:
                return row.name;
            case 2:
                return row.highlightColor;
            case 3:
                return row.shortCut;
            case 4:
                return AisMessage.DETAIL_CATEGORY_ADD_CHILD.get();
            case 5:
                return AisMessage.DETAIL_CATEGORY_DELETE.get();
            default:
                throw new IllegalArgumentException();
            }
        }
        return null;
    }

    @Override
    public void setValueAt(final Object value, final Object node, final int column) {
        if (!(node instanceof DetailCategoryRow) || column > 4) {
            return;
        }
        final DetailCategoryRow row = (DetailCategoryRow) node;
        switch (column) {
        case 0:
            row.code = (String) value;
            break;
        case 1:
            row.name = (String) value;
            break;
        case 2:
            row.highlightColor = (Color) value;
            break;
        case 3:
            row.shortCut = (KeyStroke) value;
            if (value != null) {
                // remove short cut from other category to avoid double binding
                for (final DetailCategoryRow otherNode : this.getFlatCategoryList()) {
                    if (otherNode != node && value.equals(otherNode.shortCut)) {
                        this.setValueAt(null, otherNode, column);
                        break;
                    }
                }
            }
            break;
        default:
            return;
        }
        this.modelSupport.firePathChanged(this.buildPathToRow(row));
    }

    /**
     * Provide unsorted list of all detail categories currently contained in the model - regardless of any hierarchical structure.
     *
     * @return all detail categories in unsorted list
     */
    private List<DetailCategoryRow> getFlatCategoryList() {
        final List<DetailCategoryRow> categories = new ArrayList<>(this.rootCategories);
        for (final List<DetailCategoryRow> childCategories : this.categoryChildren.values()) {
            categories.addAll(childCategories);
        }
        return categories;
    }

    /**
     * Create the full hierarchical path (starting at the root element) for the given detail category row (therefore setting it as the last path
     * component.
     *
     * @param row
     *            detail category row to build the tree path for
     * @return full tree path from root to given category row
     */
    private TreePath buildPathToRow(final DetailCategoryRow row) {
        final Deque<Object> pathElements = new LinkedList<>();
        DetailCategoryRow currentElement = row;
        // traverse the tree path from the given leaf upwards
        outer: do {
            pathElements.addFirst(currentElement);
            for (final Entry<DetailCategoryRow, List<DetailCategoryRow>> childrenSet : this.categoryChildren.entrySet()) {
                if (CollectionUtil.containsInstance(childrenSet.getValue(), currentElement)) {
                    currentElement = childrenSet.getKey();
                    continue outer;
                }
            }
            break;
        } while (currentElement != null);
        // don't forget the (invisible) root element
        pathElements.addFirst(this.getRoot());
        return new TreePath(pathElements.toArray());
    }

    /**
     * Check if the current model state is deemed valid – i.e. all categories have a unique, non-empty code assigned to them.
     *
     * @return all categories have unique, non-empty codes
     */
    boolean isValid() {
        final Set<String> codesInUse = new HashSet<>();
        for (final DetailCategoryRow singleRoot : this.rootCategories) {
            if (singleRoot.code.isEmpty() || codesInUse.contains(singleRoot.code)) {
                return false;
            }
            codesInUse.add(singleRoot.code);
        }
        for (final List<DetailCategoryRow> children : this.categoryChildren.values()) {
            for (final DetailCategoryRow singleChild : children) {
                if (singleChild.code.isEmpty() || codesInUse.contains(singleChild.code)) {
                    return false;
                }
                codesInUse.add(singleChild.code);
            }
        }
        return true;
    }

    /**
     * Translate internal detail category rows to actual detail category model. Additionally provide a mapping from old detail categories (from
     * initial setup) to their new counter parts, if any rows from the initial model still exist.
     *
     * @return represented detail category model and mapping from old to new categories
     */
    SimpleEntry<MutableDetailCategoryModel, Map<DetailCategory, DetailCategory>> toModelWithMapping() {
        final MutableDetailCategoryModel model = new MutableDetailCategoryModel();
        final Map<DetailCategory, DetailCategory> mapping = new HashMap<>();
        for (final DetailCategoryRow singleRoot : this.rootCategories) {
            this.addDetailCategoryToModel(null, singleRoot, model, mapping);
        }
        return new SimpleEntry<>(model, mapping);
    }

    /**
     * Recursively fill the given model and old-to-new category mapping with the specified row and its subordinated (i.e. child category) rows.
     *
     * @param parent
     *            super ordinated (i.e. parent) detail category row (can be {@code null} if directly under root without parent)
     * @param row
     *            detail category to add to the model
     * @param model
     *            receiving model for the detail category build form the given row
     * @param mapping
     *            mapping from old detail categories (from initial setup) to their new counter parts, if the given row already existed in the inital
     *            model
     */
    private void addDetailCategoryToModel(final DetailCategoryRow parent, final DetailCategoryRow row, final MutableDetailCategoryModel model,
            final Map<DetailCategory, DetailCategory> mapping) {
        final DetailCategory parentCategory;
        if (parent == null) {
            parentCategory = null;
        } else {
            parentCategory = model.getDetailByCode(parent.code);
        }
        final boolean hasChildren = this.categoryChildren.containsKey(row);
        final DetailCategory target = new DetailCategory(parentCategory, row.code, row.name, !hasChildren, row.highlightColor, row.shortCut);
        model.add(target);
        if (hasChildren) {
            for (final DetailCategoryRow singleChild : this.categoryChildren.get(row)) {
                this.addDetailCategoryToModel(row, singleChild, model, mapping);
            }
        } else if (row.oldCategory != null) {
            mapping.put(row.oldCategory, target);
        }
    }

    /** View model element representing a detail category in the form of a single tree jtable row. */
    private static class DetailCategoryRow {

        /** The short name of the represented category. */
        String code;
        /** The full name of the represented category. */
        String name;
        /** The color to show over tokens, with the represented category assigned. */
        Color highlightColor;
        /** The short cut associated with the represented category, to assign it to selected tokens in the scoring view. */
        KeyStroke shortCut;
        /** The old category getting replaced by the represented one. */
        DetailCategory oldCategory;

        /**
         * Constructor.
         *
         * @param represented
         *            represented category
         */
        DetailCategoryRow(final DetailCategory represented) {
            this(represented.getCode(), represented.getName(), represented.getColor(), represented.getShortCut(), represented);
        }

        /**
         * Constructor.
         *
         * @param code
         *            short name of the represented category
         * @param name
         *            full name of the represented category
         * @param highlightColor
         *            color to show over tokens, with the represented category assigned
         * @param shortCut
         *            short cut associated with the represented category, to assign it to selected tokens in the scoring view
         * @param oldCategory
         *            the initial detail category this row is derived from (can be {@code null} for a new row)
         */
        DetailCategoryRow(final String code, final String name, final Color highlightColor, final KeyStroke shortCut,
                final DetailCategory oldCategory) {
            this.code = code;
            this.name = name;
            this.highlightColor = highlightColor;
            this.shortCut = shortCut;
            this.oldCategory = oldCategory;
        }
    }
}
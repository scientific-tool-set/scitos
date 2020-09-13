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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.swing.tree.TreePath;

import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.hmx.core.i18n.HmxMessage;
import org.hmx.scitos.hmx.domain.ISemanticalRelationProvider;
import org.hmx.scitos.hmx.domain.model.RelationModel;
import org.hmx.scitos.hmx.domain.model.RelationTemplate;
import org.hmx.scitos.hmx.domain.model.RelationTemplate.AssociateRole;
import org.hmx.scitos.hmx.view.ContextMenuFactory;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

/**
 * TreeTable data model of a {@link RelationModel}. It allows the adding and removing of groups and the contained {@link RelationTemplate}s.
 * <ol start=0>
 * Manages data and buttons in a number of columns:
 * <li>Index</li>
 * <li>Defined {@link RelationTemplate} as presented in the context menu in the semantical analysis view (empty for group)</li>
 * <li>Button: add template to group (empty for template entry)</li>
 * <li>Button: move template/group up</li>
 * <li>Button: move template/group down</li>
 * <li>Button: remove template/group</li>
 * </ol>
 */
final class RelationTreeModel extends AbstractTreeTableModel implements ISemanticalRelationProvider {

    /**
     * The groups of relation templates – directly under the root of the tree structure - identified by a UUID and mapping to the list of contained
     * relation templates' UUIDs.
     */
    private final Map<UUID, List<UUID>> relationGroups = new LinkedHashMap<>();
    /**
     * The individual relation template associated with their respective UUID.
     */
    private final Map<UUID, RelationTemplate> relationTemplates = new HashMap<>();

    /**
     * Constructor.
     *
     * @param relationProvider
     *            initial provider of the {@link RelationTemplate}s to represent
     */
    RelationTreeModel(final ISemanticalRelationProvider relationProvider) {
        super(new Object());
        // setup delegate structure (as the TreeModel cannot handle the mutable data objects as immediate nodes)
        for (final List<RelationTemplate> singleGroup : relationProvider.provideRelationTemplates()) {
            final List<UUID> relationTemplatesInGroup = new LinkedList<>();
            for (final RelationTemplate singleTemplate : singleGroup) {
                // map each relation template to a random UUID
                final UUID templateId = UUID.randomUUID();
                relationTemplatesInGroup.add(templateId);
                this.relationTemplates.put(templateId, singleTemplate);
            }
            // map the relation templates in one group to another random UUID
            this.relationGroups.put(UUID.randomUUID(), relationTemplatesInGroup);
        }
    }

    @Override
    public List<List<RelationTemplate>> provideRelationTemplates() {
        final List<List<RelationTemplate>> result = new ArrayList<>(this.relationGroups.size());
        for (final List<UUID> templateIdsInSingleGroup : this.relationGroups.values()) {
            final List<RelationTemplate> resultGroup = new ArrayList<>(templateIdsInSingleGroup.size());
            for (final UUID singleTemplateId : templateIdsInSingleGroup) {
                resultGroup.add(this.relationTemplates.get(singleTemplateId));
            }
            result.add(resultGroup);
        }
        return result;
    }

    @Override
    public Object getChild(final Object parent, final int index) {
        if (parent instanceof UUID) {
            // the parent is a relation template group, return the relation template at the given index
            return this.relationGroups.get(parent).get(index);
        }
        // the parent is the root, return the relation template group's id at the given index
        return new ArrayList<>(this.relationGroups.keySet()).get(index);
    }

    @Override
    public int getChildCount(final Object parent) {
        if (parent instanceof UUID) {
            // the parent is a group of relation templates, return the number of relation templates in this group
            return this.relationGroups.get(parent).size();
        }
        // the parent is the root, return the number of relation template groups
        return this.relationGroups.size();
    }

    @Override
    public boolean isLeaf(final Object node) {
        if (node instanceof UUID) {
            // the node is either a group or a single relation template
            return !this.relationGroups.containsKey(node) || this.relationGroups.get(node).isEmpty();
        }
        // the node is the root and can only be a leaf if there are no relation template groups
        return this.relationGroups.isEmpty();
    }

    @Override
    public int getIndexOfChild(final Object parent, final Object child) {
        if (parent instanceof UUID) {
            // the parent is a group of relation templates, find the index of the given relation template id
            return this.relationGroups.get(parent).indexOf(child);
        }
        // the parent is the root, find the index of the designated relation template group
        return new ArrayList<>(this.relationGroups.keySet()).indexOf(child);
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return "";
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(final Object node, final int columnIndex) {
        if (!(node instanceof UUID)) {
            // the root node is supposed to be invisible
            return false;
        }
        final List<UUID> siblings;
        switch (columnIndex) {
        case 2:
            // only groups offer an Add button for subordinated templates
            return this.relationGroups.containsKey(node);
        case 3:
            // all but the first entry can be moved up
            siblings = this.getSiblings(node);
            return siblings != null && siblings.indexOf(node) > 0;
        case 4:
            // all but the last entry can be moved down
            siblings = this.getSiblings(node);
            return siblings != null && siblings.indexOf(node) + 1 < siblings.size();
        case 5:
            // all lines can be removed
            return true;
        default:
            // Index and Summary columns are read-only
            return false;
        }
    }

    /**
     * Get the super-ordinated list containing the given element.
     * 
     * @param node
     *            template group or entry to return the containing parent list for
     * @return list of all template groups, if the given node is a group/list of templates; the template entry's group, if the given node is a single
     *         template entry
     */
    private List<UUID> getSiblings(final Object node) {
        List<UUID> result = null;
        if (this.relationGroups.containsKey(node)) {
            // the node is a relation template group, return list of all group ids
            result = new ArrayList<>(this.relationGroups.keySet());
        } else {
            // the node is a relation template, return list of all relation template ids in the same group
            for (final List<UUID> singleGroup : this.relationGroups.values()) {
                if (singleGroup.contains(node)) {
                    result = singleGroup;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String getValueAt(final Object node, final int columnIndex) {
        if (!(node instanceof UUID)) {
            // the root node is supposed to be invisible
            return null;
        }
        switch (columnIndex) {
        case 0:
            // Index
            final UUID groupId = this.getGroupId((UUID) node);
            final StringBuilder indexBuilder = new StringBuilder(10);
            indexBuilder.append(this.getSiblings(groupId).indexOf(groupId) + 1).append('.');
            if (groupId != node) {
                indexBuilder.append(this.getSiblings(node).indexOf(node) + 1).append('.');
            }
            return indexBuilder.append(' ').toString();
        case 1:
            // Defined RelationTemplate as presented in the context menu in the semantical analysis view (empty for group)
            return this.relationTemplates.containsKey(node) ? ContextMenuFactory.buildRelationLabel(this.relationTemplates.get(node)) : "";
        case 2:
            // Button: add template to group (empty for template entry)
            return this.isCellEditable(node, columnIndex) ? HmxMessage.PREFERENCES_RELATION_ADD.get() : null;
        case 3:
            // Button: move template/group up (all but the first entry can be moved up)
            return this.isCellEditable(node, columnIndex) ? HmxMessage.PREFERENCES_RELATION_MOVE_UP.get() : null;
        case 4:
            // Button: move template/group down (all but the last entry can be moved down)
            return this.isCellEditable(node, columnIndex) ? HmxMessage.PREFERENCES_RELATION_MOVE_DOWN.get() : null;
        case 5:
            // Button: remove group/template
            if (this.relationGroups.containsKey(node)) {
                return HmxMessage.PREFERENCES_RELATION_REMOVE_GROUP.get();
            }
            return HmxMessage.PREFERENCES_RELATION_REMOVE.get();
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get the id of the associated relation template group. Returns the given id if it already corresponds to a group. Otherwise, returns the id of
     * the group containing the relation template identified by the given id.
     * 
     * @param node
     *            id of a relation template or group
     * @return id of the associated relation template group
     */
    private UUID getGroupId(final UUID node) {
        UUID result = node;
        if (this.relationTemplates.containsKey(node)) {
            for (final Entry<UUID, List<UUID>> singleGroup : this.relationGroups.entrySet()) {
                if (singleGroup.getValue().contains(node)) {
                    result = singleGroup.getKey();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * The actual editing is done via a separate form not via the represented table. Only the buttons for adding/moving/deleting templates and groups
     * are available here.
     */
    @Override
    public void setValueAt(final Object value, final Object node, final int column) {
        // nothing to do here
    }

    /**
     * Add a new relation template group at the bottom of the list.
     * 
     * @return path to the newly created group entry
     */
    public TreePath addGroupEntry() {
        final UUID groupId = UUID.randomUUID();
        this.relationGroups.put(groupId, new LinkedList<>());
        final TreePath rootPath = new TreePath(this.getRoot());
        this.modelSupport.fireChildAdded(rootPath, this.relationGroups.size() - 1, groupId);
        return rootPath.pathByAddingChild(groupId);
    }

    /**
     * Getter for the current state of a {@link RelationTemplate} associated with the given path.
     * 
     * @param path
     *            target path to retrieve the associated relation template for
     * @return associated relation template (or {@code null} if the path doesn't correspond to any relation template)
     */
    public RelationTemplate getTemplateAtPath(final TreePath path) {
        return this.relationTemplates.get(path.getLastPathComponent());
    }

    /**
     * Add template to group at the given path.
     * 
     * @param groupPath
     *            path to the group the new relation template entry should be added
     * @return path to the newly create template entry
     */
    public TreePath addTemplateEntryToGroup(final TreePath groupPath) {
        final AssociateRole defaultHighWeight = new AssociateRole(HmxMessage.PREFERENCES_RELATION_DEFAULTROLE_HIGHWEIGHT.get(), true);
        final AssociateRole defaultLowWeight = new AssociateRole(HmxMessage.PREFERENCES_RELATION_DEFAULTROLE_LOWWEIGHT.get(), false);
        final RelationTemplate newTemplate = new RelationTemplate(defaultHighWeight, null, defaultLowWeight, "");
        final List<UUID> parentGroup = this.relationGroups.get(groupPath.getLastPathComponent());
        final UUID templateId = UUID.randomUUID();
        parentGroup.add(templateId);
        this.relationTemplates.put(templateId, newTemplate);
        this.modelSupport.fireChildAdded(groupPath, parentGroup.size() - 1, templateId);
        return groupPath.pathByAddingChild(templateId);
    }

    /**
     * Update template entry at the given path.
     * 
     * @param targetPath
     *            path to the relation template entry to be updated
     * @param newState
     *            the updated relation template to be stored internally
     */
    public void updateTemplateEntry(final TreePath targetPath, final RelationTemplate newState) {
        final UUID templateId = (UUID) targetPath.getLastPathComponent();
        this.relationTemplates.put(templateId, newState);
        final int templateIndex = this.getSiblings(templateId).indexOf(templateId);
        this.modelSupport.fireChildChanged(targetPath.getParentPath(), templateIndex, templateId);
    }

    /**
     * Move a single template group or entry up or down by one step.
     * 
     * @param targetPath
     *            path to the designated template group or entry to be moved
     * @param moveDown
     *            if the designated template group or entry should be moved down; otherwise moved up
     */
    public void moveEntry(final TreePath targetPath, final boolean moveDown) {
        final UUID entry = (UUID) targetPath.getLastPathComponent();
        if (this.relationGroups.containsKey(entry)) {
            // the designated path refers to a template group
            CollectionUtil.moveEntryInInsertSortedMap(this.relationGroups, entry, moveDown);
        } else {
            // the designated path refers to a single relation template (and the list returned from getSiblings feeds back to the original)
            final List<UUID> entrySiblings = this.getSiblings(entry);
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
        this.modelSupport.fireTreeStructureChanged(targetPath.getParentPath());
    }

    /**
     * Delete the template group or entry at the given path.
     * 
     * @param targetPath
     *            path to the template group or entry to be deleted
     */
    public void removeEntry(final TreePath targetPath) {
        final Object entry = targetPath.getLastPathComponent();
        final Object parent = targetPath.getPathComponent(targetPath.getPathCount() - 2);
        final int index = this.getIndexOfChild(parent, entry);
        if (this.relationGroups.containsKey(entry)) {
            // the designated path refers to a template group, remove all its relation templates with it
            for (final UUID singleRelationId : this.relationGroups.remove(entry)) {
                this.relationTemplates.remove(singleRelationId);
            }
        } else {
            // the designated path refers to a single relation template, remove it from the parent's list and the global relation template map
            this.relationGroups.get(parent).remove(entry);
            this.relationTemplates.remove(entry);
        }
        this.modelSupport.fireChildRemoved(targetPath.getParentPath(), index, entry);
    }
}
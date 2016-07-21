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

package org.hmx.scitos.view.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.domain.IMultiObjectModel;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.hmx.scitos.domain.util.ComparisonUtil;
import org.hmx.scitos.view.ContextMenuBuilder;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.components.ScaledLabel;
import org.hmx.scitos.view.swing.util.ButtonTabComponent;

/**
 * The single main view of the ScitosClient (except the menu bar and tool bar, which are handled by the client directly).
 */
public class MainView extends JPanel {

    /** The client instance containing this view. */
    final ScitosClient client;
    /** The currently open view projects. */
    private final List<IViewProject<?>> openProjects = new LinkedList<IViewProject<?>>();

    /** The divided pane containing the project tree on the left and the tab stack on the right. */
    private final JSplitPane splitPane;
    /** The (invisible) root of the project tree on the left side. */
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(null, true);
    /** The project tree on the left side, offering the selection of the open projects and their respective model elements. */
    private final JTree projectTree = new JTree(new DefaultTreeModel(this.rootNode, true));
    /** Wrapper of the project tree allowing it to be scrollable (and maintaining the correctly colored border). */
    private final JScrollPane scrollableTree = new JScrollPane(this.projectTree);
    /** The tab area holding the actual views for the open projects and their model elements. */
    private final JTabbedPane tabStack = new JTabbedPane(SwingConstants.TOP);

    /**
     * Main constructor.
     *
     * @param client
     *            {@link ScitosClient} instance containing this view
     * @param createWelcomeTab
     *            if a generic tab should be created in order to view an empty tab folder on startup
     */
    public MainView(final ScitosClient client, final boolean createWelcomeTab) {
        super(new BorderLayout());
        this.client = client;
        this.setBorder(null);
        this.initProjectTree(this.projectTree);
        // set the initial size (height can be ignored)
        this.scrollableTree.setPreferredSize(new Dimension(200, 1));
        this.scrollableTree.setMinimumSize(new Dimension(120, 1));

        this.initTabStack(this.tabStack, createWelcomeTab);
        final JPanel wrappedTabStack = new JPanel(new BorderLayout());
        wrappedTabStack.add(this.tabStack);
        // initialize the content area
        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        this.splitPane.setLeftComponent(this.scrollableTree);
        this.splitPane.setRightComponent(wrappedTabStack);
        this.splitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.splitPane.setDividerSize(5);
        this.add(this.splitPane);
        this.applyLookAndFeelDependentProjectTreeStyling();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (this.projectTree != null) {
            this.applyLookAndFeelDependentProjectTreeStyling();
        }
    }

    /**
     * Ensure the correct rendering regarding the currently applied look and feel, where look and feel colors have been re-used manually.
     */
    private void applyLookAndFeelDependentProjectTreeStyling() {
        final Color color = UIManager.getColor("Panel.background");
        this.scrollableTree.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(5, 5, 5, 0, color == null ? null : new Color(color.getRGB())),
                BorderFactory.createLoweredBevelBorder()));
        this.projectTree.setCellRenderer(new ScitosTreeNodeRenderer());
    }

    /**
     * Initialize the project tree's view attributes and behavior.
     *
     * @param tree
     *            actual tree component to initialize
     */
    private void initProjectTree(final JTree tree) {
        tree.setBorder(null);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setExpandsSelectedPaths(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // enable the activation of the user object in the selected node
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                MainView.this.applyTreeSelection();
            }
        });
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent event) {
                this.mouseReleased(event);
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                final TreePath path = tree.getPathForLocation(event.getX(), event.getY());
                if (event.isPopupTrigger() && path != null && path.getPathCount() > 1) {
                    final ScitosTreeNode clickedNode = (ScitosTreeNode) path.getLastPathComponent();
                    final ContextMenuBuilder contextMenu =
                            MainView.this.client.getProjectViewProvider().createContextMenu(clickedNode.getProject(), clickedNode.getUserObject());
                    if (contextMenu != null) {
                        ContextMenuPopupBuilder.buildSwingPopupMenu(contextMenu).show(tree, event.getX(), event.getY());
                    }
                }
            }
        });
    }

    /**
     * Initialize the tab area holding the views for the open projects and their model elements.
     *
     * @param tabPane
     *            actual tab pane to initialize
     * @param createWelcomeTab
     *            if a generic tab should be created in order to view an empty tab folder on startup
     */
    private void initTabStack(final JTabbedPane tabPane, final boolean createWelcomeTab) {
        tabPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        if (createWelcomeTab) {
            // display default start view
            this.createWelcomeTab();
        }
        // react on changed tabs
        tabPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(final ChangeEvent event) {
                MainView.this.selectNodeForCurrentTab();
                MainView.this.revalidateClient(true);
            }
        });
    }

    /** Hide/show the project tree to allow the maximum space to be available for the actual open project/file. */
    public void toggleProjectTreeVisibility() {
        if (this.getComponent(0) == this.splitPane) {
            this.removeAll();
            this.add(this.tabStack);
            this.revalidate();
        } else {
            this.removeAll();
            final JPanel rightWrapper = (JPanel) this.splitPane.getRightComponent();
            // avoid inconsistencies
            rightWrapper.removeAll();
            // make sure the tabbed pane is contained in the right component
            rightWrapper.add(this.tabStack);
            this.add(this.splitPane);
            this.revalidate();
            this.scrollableTree.repaint();
            this.splitPane.repaint();
        }
    }

    /**
     * Refresh the view state: i.e. the frame title, displayed contents of the current view, availability of general menu bar and tool bar items.
     *
     * @param refreshToolBarItems
     *            if the view specific tool bar items should be replaced as well
     */
    public void revalidateClient(final boolean refreshToolBarItems) {
        this.client.revalidate();
        if (refreshToolBarItems) {
            this.client.validateProjectMenuEntries();
            final AbstractProjectView<?, ?> currentTab = this.getActiveTab();
            if (currentTab == null) {
                this.client.setEditMenuItems(Collections.<JMenuItem>emptyList());
                this.client.setViewMenuItems(Collections.<JMenuItem>emptyList());
                this.client.setToolBarItems(Collections.<Component>emptyList());
            } else {
                this.client.setEditMenuItems(currentTab.createEditMenuItems());
                this.client.setViewMenuItems(currentTab.createViewMenuItems());
                this.client.setToolBarItems(currentTab.createToolBarItems());
            }
        }
    }

    /**
     * Getter for the currently active (selected) project/group/model tab.
     *
     * @return currently active (selected) project/group/model tab; returns {@code null} if none or another kind of tab is active
     */
    AbstractProjectView<?, ?> getActiveTab() {
        final Component tab = this.tabStack.getSelectedComponent();
        if (tab instanceof AbstractProjectView<?, ?>) {
            return (AbstractProjectView<?, ?>) tab;
        }
        return null;
    }

    /**
     * Clear the tree structure and rebuild it with the currently open projects. This re-applies the single selected node.
     */
    public void resetTreeStructure() {
        final List<IViewProject<?>> expandedProjectNodes = new LinkedList<IViewProject<?>>();
        final Map<IViewProject<?>, Set<String>> expandedModelGroupNodes = new HashMap<IViewProject<?>, Set<String>>();
        final List<IModel<?>> expandedSubModelNodes = new LinkedList<IModel<?>>();
        this.collectExpandedNodeObjects(expandedProjectNodes, expandedModelGroupNodes, expandedSubModelNodes);
        this.rootNode.removeAllChildren();
        // iterate all currently open projects
        for (final IViewProject<? extends IModel<?>> singleProject : this.openProjects) {
            // create a top level project node
            final ScitosTreeNode projectNode = new ScitosTreeNode(singleProject);
            // check if there are sub models to be represented in the tree
            final IModel<?> projectModel = singleProject.getModelObject();
            if (projectModel instanceof IMultiObjectModel<?, ?>) {
                // iterate all grouped sub models
                for (final Entry<String, List<? extends IModel<?>>> subModelGroup : new TreeMap<String, List<? extends IModel<?>>>(
                        ((IMultiObjectModel<?, ?>) projectModel).getSubModelObjects()).entrySet()) {
                    final List<? extends IModel<?>> subModels = subModelGroup.getValue();
                    // avoid group node if there is only a single sub model
                    if (subModels.size() == 1) {
                        final IModel<?> singleModel = subModels.get(0);
                        // create a model node directly under the project node
                        projectNode.add(new ScitosTreeNode(singleModel));
                    } else if (subModels.size() > 1) {
                        // create a group node under the project node
                        final String groupTitle = subModelGroup.getKey();
                        final ScitosTreeNode groupNode = new ScitosTreeNode(groupTitle);
                        // create individual model nodes under the group node
                        for (final IModel<?> singleSubModel : subModels) {
                            groupNode.add(new ScitosTreeNode(singleSubModel));
                        }
                        // add group node directly under project node
                        projectNode.add(groupNode);
                    }
                }
            }
            // add project node directly under (invisible) root
            this.rootNode.add(projectNode);
        }
        // notify tree model to update displayed tree
        ((DefaultTreeModel) this.projectTree.getModel()).reload();
        // re-apply expanded paths
        this.expandObjectNodes(expandedProjectNodes, expandedModelGroupNodes, expandedSubModelNodes);
        // reset the tree selection to match the currently open tab
        this.selectNodeForCurrentTab();
    }

    /**
     * Determine which view projects, and respective model elements are currently in expanded state in the project tree – in order to be able to
     * restore this state after resetting the project tree's contents.
     *
     * @param expandedProjectNodes
     *            editable list of view projects, to be filled with expanded view projects
     * @param expandedModelGroupNodes
     *            editable map of multi model groups, to be filled with expanded view projects and their expanded model groups
     * @param expandedSubModelNodes
     *            editable list of model elements, to be filled with expanded model elements
     */
    private void collectExpandedNodeObjects(final List<IViewProject<?>> expandedProjectNodes,
            final Map<IViewProject<?>, Set<String>> expandedModelGroupNodes, final List<IModel<?>> expandedSubModelNodes) {
        final Enumeration<TreePath> expandedNodes = this.projectTree.getExpandedDescendants(new TreePath(this.rootNode));
        if (expandedNodes != null) {
            while (expandedNodes.hasMoreElements()) {
                final Object[] singlePath = expandedNodes.nextElement().getPath();
                if (singlePath.length < 2) {
                    continue;
                }
                final IViewProject<?> project = ((ScitosTreeNode) singlePath[1]).getProject();
                if (!CollectionUtil.containsInstance(expandedProjectNodes, project)) {
                    expandedProjectNodes.add(project);
                }
                if (singlePath.length > 2 && ((DefaultMutableTreeNode) singlePath[2]).getUserObject() instanceof String) {
                    final Set<String> modelGroupsInProject;
                    if (expandedModelGroupNodes.containsKey(project)) {
                        modelGroupsInProject = expandedModelGroupNodes.get(project);
                    } else {
                        modelGroupsInProject = new HashSet<String>();
                        expandedModelGroupNodes.put(project, modelGroupsInProject);
                    }
                    expandedModelGroupNodes.get(project).add((String) ((DefaultMutableTreeNode) singlePath[2]).getUserObject());
                }
            }
        }
    }

    /**
     * Expand the nodes in the project tree associated with the given view projects, model groups, and model elements – if (still) existent.
     *
     * @param projects
     *            view projects to be expanded/viewable
     * @param modelGroups
     *            view projects and their model groups to be expanded/viewable
     * @param subModels
     *            model elements to be expanded/viewable
     * @see #collectExpandedNodeObjects(List, Map, List)
     */
    private void expandObjectNodes(final List<IViewProject<?>> projects, final Map<IViewProject<?>, Set<String>> modelGroups,
            final List<IModel<?>> subModels) {
        final List<ScitosTreeNode> nodes = new LinkedList<ScitosTreeNode>();
        for (final IViewProject<?> singleProject : projects) {
            nodes.add(this.getProjectNode(singleProject));
        }
        for (final Entry<IViewProject<?>, Set<String>> singleProject : modelGroups.entrySet()) {
            for (final String singleModelGroup : singleProject.getValue()) {
                nodes.add(this.getModelGroupTreeNode(singleProject.getKey(), singleModelGroup));
            }
        }
        for (final IModel<?> singleModel : subModels) {
            nodes.add(this.getModelTreeNode(singleModel));
        }
        for (final ScitosTreeNode singleNode : nodes) {
            if (singleNode != null) {
                this.projectTree.expandPath(new TreePath(singleNode.getPath()));
            }
        }
    }

    /**
     * Select the associated node in the project tree for the currently active tab view.
     */
    void selectNodeForCurrentTab() {
        final AbstractProjectView<?, ?> currentTab = this.getActiveTab();
        if (currentTab != null && currentTab.getModel() instanceof IViewProject<?>) {
            this.selectProjectTreeNode((IViewProject<?>) currentTab.getModel());
        } else if (currentTab != null && currentTab.getModel() instanceof IModel<?>) {
            this.selectModelTreeNode((IModel<?>) currentTab.getModel());
        } else if (currentTab != null && currentTab.getModel() instanceof String) {
            this.selectModelGroupTreeNode(currentTab.getProject(), (String) currentTab.getModel());
        } else {
            this.selectProjectTreeNode(null);
        }
    }

    /**
     * Refresh everything (tree label, tab title, tab content) associated with the given node.
     *
     * @param node
     *            tree node to refresh
     */
    void refreshElement(final ScitosTreeNode node) {
        final List<IViewProject<?>> expandedProjectNodes = new LinkedList<IViewProject<?>>();
        final Map<IViewProject<?>, Set<String>> expandedModelGroupNodes = new HashMap<IViewProject<?>, Set<String>>();
        final List<IModel<?>> expandedSubModelNodes = new LinkedList<IModel<?>>();
        this.collectExpandedNodeObjects(expandedProjectNodes, expandedModelGroupNodes, expandedSubModelNodes);
        ((DefaultTreeModel) this.projectTree.getModel()).reload(node);
        this.expandObjectNodes(expandedProjectNodes, expandedModelGroupNodes, expandedSubModelNodes);
        this.selectNodeForCurrentTab();
        // check if a tab for the given node is currently open
        final AbstractProjectView<?, ?> tabForNode = this.getExistingTabForNode(node);
        if (tabForNode != null) {
            // refresh tab title and its content pane
            this.tabStack.setTitleAt(this.tabStack.indexOfComponent(tabForNode), node.toString());
            tabForNode.refresh();
        }
        this.tabStack.repaint();
        this.revalidateClient(true);
    }

    /**
     * Discard the current selection in the project tree and select the given project's node.
     *
     * @param project
     *            project to select
     */
    private void selectProjectTreeNode(final IViewProject<?> project) {
        this.selectTreeNode(this.getProjectNode(project));
    }

    /**
     * Getter for the node in the project tree representing the given view project.
     *
     * @param project
     *            view project to find the associated project tree node for
     * @return associated project tree node
     */
    ScitosTreeNode getProjectNode(final IViewProject<?> project) {
        if (project != null) {
            final Enumeration<?> projectNodes = this.rootNode.children();
            while (projectNodes.hasMoreElements()) {
                final ScitosTreeNode singleProjectNode = (ScitosTreeNode) projectNodes.nextElement();
                if (singleProjectNode.getProject() == project) {
                    return singleProjectNode;
                }
            }
        }
        return null;
    }

    /**
     * Discard the current selection in the project tree and select the given project's model group's node.
     *
     * @param project
     *            project containing the targeted model group
     * @param modelGroup
     *            title of the model group to select
     */
    private void selectModelGroupTreeNode(final IViewProject<?> project, final String modelGroup) {
        this.selectTreeNode(this.getModelGroupTreeNode(project, modelGroup));
    }

    /**
     * Getter for the node in the project tree representing the specified model group in the given view project.
     *
     * @param project
     *            view project the specified model group belongs to
     * @param modelGroup
     *            title of the model group to find the associated project tree node for
     * @return associated project tree node
     */
    ScitosTreeNode getModelGroupTreeNode(final IViewProject<?> project, final String modelGroup) {
        final ScitosTreeNode projectNode = this.getProjectNode(project);
        if (projectNode != null && modelGroup != null) {
            final Enumeration<?> modelAndModelGroupNodes = projectNode.children();
            while (modelAndModelGroupNodes.hasMoreElements()) {
                final ScitosTreeNode singleModelOrModelGroupNode = (ScitosTreeNode) modelAndModelGroupNodes.nextElement();
                if (singleModelOrModelGroupNode.getUserObject() instanceof String
                        && modelGroup.equals(singleModelOrModelGroupNode.getUserObject())) {
                    return singleModelOrModelGroupNode;
                }
            }
        }
        return null;
    }

    /**
     * Discard the current selection in the project tree and select the given model's node.
     *
     * @param model
     *            represented model element to select the tree node for
     */
    public void selectModelTreeNode(final IModel<?> model) {
        this.selectTreeNode(this.getModelTreeNode(model));
    }

    /**
     * Getter for the project tree node representing the given model element.
     *
     * @param model
     *            model element to find the associated project tree node for
     * @return associated project tree node
     */
    private ScitosTreeNode getModelTreeNode(final IModel<?> model) {
        if (model != null && this.rootNode.getChildCount() > 0) {
            ScitosTreeNode singleTreeLeaf = (ScitosTreeNode) this.rootNode.getFirstLeaf();
            do {
                if (singleTreeLeaf.getUserObject() == model) {
                    return singleTreeLeaf;
                }
                singleTreeLeaf = (ScitosTreeNode) singleTreeLeaf.getNextLeaf();
            } while (singleTreeLeaf != null);
        }
        return null;
    }

    /**
     * Discard any current selection in the project tree, select the given node and ensure that the associated tab view is displayed.
     *
     * @param node
     *            project tree node to select (can be {@code null} if the selection should only be cleared)
     */
    private void selectTreeNode(final ScitosTreeNode node) {
        if (node == null) {
            this.projectTree.clearSelection();
        } else {
            final TreePath nodePath = new TreePath(node.getPath());
            final TreePath[] selected = this.projectTree.getSelectionPaths();
            // only apply requested selection if it is not already the single selected node in the tree
            if (selected == null || selected.length != 1 || !nodePath.equals(selected[0])) {
                this.projectTree.clearSelection();
                this.applyTreeSelection();
                this.projectTree.setSelectionPath(nodePath);
                this.applyTreeSelection();
            }
        }
    }

    /**
     * Ensure that the tab view associated with the currently selected project tree node is displayed. This creates the tab view if necessary.
     */
    void applyTreeSelection() {
        // ignore empty selection or multiple nodes here
        if (this.projectTree.getSelectionCount() == 1) {
            final ScitosTreeNode selectedNode = (ScitosTreeNode) this.projectTree.getSelectionPath().getLastPathComponent();
            // check open tabs, if one of them already represents the currently selected tree node
            AbstractProjectView<?, ?> tabForSelectedNode = this.getExistingTabForNode(selectedNode);
            if (tabForSelectedNode == null) {
                // we have to create a new tab for the selected tree node
                tabForSelectedNode = this.createTabForNode(selectedNode);
            }
            // select the tab
            this.tabStack.setSelectedComponent(tabForSelectedNode);
            this.refreshElement(selectedNode);
        }
    }

    /**
     * Getter for tab view associated with the given project tree node.
     *
     * @param node
     *            project tree node to find the associated tab view for
     * @return associated tab view (is {@code null} if no such tab view exists at the moment)
     */
    private AbstractProjectView<?, ?> getExistingTabForNode(final ScitosTreeNode node) {
        final Object nodeObject = node.getUserObject();
        final IViewProject<?> nodeProject = node.getProject();
        // iterate all tabs
        final Component[] tabStackComponents = this.tabStack.getComponents();
        AbstractProjectView<?, ?> nodeTab = null;
        for (final Component singleTabComponent : tabStackComponents) {
            if (singleTabComponent instanceof AbstractProjectView<?, ?>) {
                final AbstractProjectView<?, ?> singleTab = (AbstractProjectView<?, ?>) singleTabComponent;
                if (nodeProject == singleTab.getProject()) {
                    final boolean nodeObjectMatches;
                    if (nodeObject == nodeProject) {
                        nodeObjectMatches = nodeProject.getModelObject() == singleTab.getModel();
                    } else {
                        nodeObjectMatches = nodeObject.equals(singleTab.getModel());
                    }
                    if (nodeObjectMatches) {
                        // tab for the selected tree node exists
                        nodeTab = singleTab;
                        break;
                    }
                }
            }
        }
        return nodeTab;
    }

    /**
     * Create a tab view associated with the given node.
     *
     * @param node
     *            project tree node to create the tab view for
     * @return created tab view
     */
    private AbstractProjectView<?, ?> createTabForNode(final ScitosTreeNode node) {
        final Object nodeObject = node.getUserObject();
        final IViewProject<?> nodeProject = node.getProject();
        final AbstractProjectView<?, ?> newTab;
        if (nodeObject instanceof IViewProject<?>) {
            // the selected node represents the project node
            newTab = this.client.getProjectViewProvider().createProjectView((IViewProject<?>) nodeObject);
        } else if (nodeObject instanceof IModel<?>) {
            // the selected node represents a model node
            newTab = this.client.getProjectViewProvider().createModelView(nodeProject, (IModel<?>) nodeObject);
        } else {
            // the selected node is (assumed to be) a model group node
            newTab = this.client.getProjectViewProvider().createModelGroupView(nodeProject, (String) nodeObject);
        }
        // add a new tab containing the newly created view
        this.tabStack.add(node.toString(), newTab);
        // add a 'x' button for closing the tab
        this.tabStack.setTabComponentAt(this.tabStack.indexOfComponent(newTab), new ButtonTabComponent(this.tabStack));
        newTab.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(final HierarchyEvent event) {
                if (!newTab.isShowing()) {
                    // ensure no data is lost when a tab is closed
                    newTab.submitChangesToModel();
                }
            }
        });
        return newTab;
    }

    /**
     * Create and display the default startup tab, in order to avoid an empty tab area.
     */
    private void createWelcomeTab() {
        final JPanel welcomeTab = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;
        welcomeTab.add(new ScaledLabel(Message.TAB_WELCOME_TEXT.get()), constraints);
        constraints.weighty = 1;
        constraints.gridy++;
        welcomeTab.add(new JPanel(), constraints);
        constraints.weighty = 0;
        constraints.gridy++;
        welcomeTab.add(new JLabel(ScitosIcon.APPLICATION.create()), constraints);
        constraints.weighty = 2;
        constraints.gridy++;
        welcomeTab.add(new JPanel(), constraints);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.PAGE_END;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.weighty = 0;
        for (final Entry<FileType, ActionListener> singleFileType : this.client.createNewFileActions().entrySet()) {
            final JButton createFileButton =
                    new JButton(Message.MENUBAR_FILE_NEW.get() + " : " + singleFileType.getKey().getLocalizableName().get(),
                            ScitosIcon.NEW_FILE.create());
            createFileButton.addActionListener(singleFileType.getValue());
            constraints.gridy++;
            welcomeTab.add(createFileButton, constraints);
        }
        constraints.gridx = 1;
        constraints.gridy = 4;
        final JButton openFileButton = new JButton(Message.MENUBAR_FILE_OPEN.get(), ScitosIcon.FOLDER_OPEN.create());
        openFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                MainView.this.client.open();
            }
        });
        welcomeTab.add(openFileButton, constraints);
        // add a new tab containing the newly created view
        this.tabStack.add(Message.TAB_WELCOME_TITLE.get(), welcomeTab);
        // add a 'x' button for closing the tab
        this.tabStack.setTabComponentAt(this.tabStack.indexOfComponent(welcomeTab), new ButtonTabComponent(this.tabStack));
    }

    /**
     * Add the given {@link IViewProject} to the tree and select it.
     *
     * @param project
     *            project to add
     */
    void addProject(final IViewProject<?> project) {
        for (final IViewProject<?> singleProject : this.openProjects) {
            if (ComparisonUtil.isNullAwareEqual(singleProject.getSavePath(), project.getSavePath())) {
                MessageHandler.showMessage(Message.MENUBAR_FILE_OPEN_ALREADY.get(), "", MessageHandler.MessageType.WARN);
                return;
            }
        }
        this.openProjects.add(project);
        this.resetTreeStructure();
        if (project.getOpenTabElements().isEmpty() || !(project.getModelObject() instanceof IMultiObjectModel<?, ?>)) {
            this.selectProjectTreeNode(project);
        } else {
            for (final Object singleOpenTab : project.getOpenTabElements()) {
                if (singleOpenTab instanceof IMultiObjectModel<?, ?>) {
                    this.selectProjectTreeNode(project);
                } else if (singleOpenTab instanceof IModel<?>) {
                    this.selectModelTreeNode((IModel<?>) singleOpenTab);
                } else if (singleOpenTab instanceof String) {
                    this.selectModelGroupTreeNode(project, (String) singleOpenTab);
                }
            }
        }
    }

    /**
     * Getter for the currently active view project – i.e. the view project the currently active tab belongs to.
     *
     * @return current active {@link IViewProject}
     */
    IViewProject<?> getActiveProject() {
        final IViewProject<?> project;
        if (this.openProjects.isEmpty()) {
            // no active project
            project = null;
        } else {
            final AbstractProjectView<?, ?> activeTab = this.getActiveTab();
            if (activeTab != null) {
                project = activeTab.getProject();
            } else {
                final TreePath[] selectionInTree = this.projectTree.getSelectionPaths();
                if (selectionInTree == null || selectionInTree.length == 0) {
                    project = null;
                } else if (selectionInTree.length == 1) {
                    project = ((ScitosTreeNode) selectionInTree[0].getLastPathComponent()).getProject();
                } else {
                    IViewProject<?> projectOfAllSelections = ((ScitosTreeNode) selectionInTree[0].getLastPathComponent()).getProject();
                    for (int index = 1; index < selectionInTree.length; index++) {
                        if (projectOfAllSelections != ((ScitosTreeNode) selectionInTree[index].getLastPathComponent()).getProject()) {
                            projectOfAllSelections = null;
                            break;
                        }
                    }
                    project = projectOfAllSelections;
                }
            }
        }
        return project;
    }

    /**
     * Requests refreshing of everything (tree label, tab title, tab content) associated with the given project's node. This method is safe to use
     * even from outside the UI thread, since it is synchronized with it before executing the refresh action.
     *
     * @param project
     *            project to refresh the displayed information for
     */
    void invokeRepresentationRefresh(final IViewProject<?> project) {
        // make sure the public method does not cause trouble in the gui thread
        SwingUtilities.invokeLater(new Thread("Refresh") {

            @Override
            public void run() {
                MainView.this.refreshElement(MainView.this.getProjectNode(project));
            }
        });
    }

    /**
     * Ensure any open tab associated with the given project is still valid - closing any invalid tabs and refreshing the labels of the valid ones.
     *
     * @param project
     *            the project in need of validation
     */
    public void validateTabsForProject(final IViewProject<?> project) {
        for (final AbstractProjectView<?, ?> singleTab : this.getOpenTabsForProject(project)) {
            final Object modelObject = singleTab.getModel();
            if (project.isValidElement(modelObject)) {
                final int tabIndex = this.tabStack.indexOfComponent(singleTab);
                this.tabStack.setTitleAt(tabIndex, project.getLabel(modelObject));
                ((JComponent) this.tabStack.getTabComponentAt(tabIndex)).revalidate();
            } else {
                this.tabStack.remove(singleTab);
            }
        }
        this.tabStack.revalidate();
    }

    /**
     * Close the specified project.
     *
     * @param project
     *            {@link IViewProject} to end
     * @return successfully closed
     */
    boolean closeProject(final IViewProject<?> project) {
        if (!CollectionUtil.containsInstance(this.openProjects, project)) {
            // already closed
            return true;
        }
        // hand over currently open tabs to allow them to be saved as well
        this.updateOpenTabElementsForProject(project);
        // make sure no unsaved changes get lost
        if (!project.prepareForClosing()) {
            // user aborted closing
            return false;
        }
        // close all tabs associated with this project
        for (final AbstractProjectView<?, ?> singleTab : this.getOpenTabsForProject(project)) {
            this.tabStack.remove(singleTab);
        }
        // remove from tree
        this.openProjects.remove(project);
        this.resetTreeStructure();
        this.revalidateClient(true);
        return true;
    }

    /**
     * Collect all open tab views that belong to the given view project.
     *
     * @param project
     *            view project to find the tab views for
     * @return open tab views belonging to the given project
     */
    private List<AbstractProjectView<?, ?>> getOpenTabsForProject(final IViewProject<?> project) {
        if (!CollectionUtil.containsInstance(this.openProjects, project)) {
            return Collections.emptyList();
        }
        final List<AbstractProjectView<?, ?>> openTabs = new LinkedList<AbstractProjectView<?, ?>>();
        for (final Component singleTab : this.tabStack.getComponents()) {
            if (singleTab instanceof AbstractProjectView<?, ?>) {
                final AbstractProjectView<?, ?> associatedTab = (AbstractProjectView<?, ?>) singleTab;
                if (project == associatedTab.getProject()) {
                    openTabs.add(associatedTab);
                }
            }
        }
        return openTabs;
    }

    /**
     * Update the project's internal list of open tabs, that are associated with it, in order to allow it to possibly save it as part of its project
     * file. Thereby allowing the reopening of all previously opened tabs in the next session.
     *
     * @param project
     *            project to update the internal tab element list in
     */
    void updateOpenTabElementsForProject(final IViewProject<?> project) {
        if (project != null) {
            final List<Object> tabElements = new LinkedList<Object>();
            for (final AbstractProjectView<?, ?> singleTab : this.getOpenTabsForProject(project)) {
                singleTab.submitChangesToModel();
                tabElements.add(singleTab.getModel());
            }
            project.setOpenTabElements(tabElements);
        }
    }

    /**
     * Close all open projects. Ask the user to confirm saving or discarding pending changes.
     *
     * @return all projects successfully closed
     */
    boolean closeAllProjects() {
        // first close actually active projects (with open tabs)
        while (this.getActiveProject() != null) {
            if (!this.closeProject(this.getActiveProject())) {
                // user aborted the closing process
                return false;
            }
        }
        // close all other projects
        for (final IViewProject<?> singleProject : new ArrayList<IViewProject<?>>(this.openProjects)) {
            if (!this.closeProject(singleProject)) {
                // user aborted the closing process
                return false;
            }
        }
        return true;
    }

    /**
     * Wrapper for user objects in tree nodes. The JTree is calling the toString() method of its nodes' user objects in order to display the label
     * texts.
     */
    public final class ScitosTreeNode extends DefaultMutableTreeNode {

        /**
         * Constructor: for a node representing the sub tree root node of a view project.
         *
         * @param project
         *            represented project to store as user object
         * @see #getUserObject()
         */
        ScitosTreeNode(final IViewProject<?> project) {
            super(project, project.getModelObject() instanceof IMultiObjectModel<?, ?>
                    && !((IMultiObjectModel<?, ?>) project.getModelObject()).getSubModelObjects().isEmpty());
        }

        /**
         * Constructor: for a node representing a grouping of models in a multi model project.
         *
         * @param modelGroupTitle
         *            the group name to store as user object
         * @see #getUserObject()
         */
        ScitosTreeNode(final String modelGroupTitle) {
            super(modelGroupTitle, true);
        }

        /**
         * Constructor: for a node representing a single model (always a leaf in the tree).
         *
         * @param model
         *            represented model to store as user object
         * @see #getUserObject()
         */
        ScitosTreeNode(final IModel<?> model) {
            super(model, false);
        }

        /**
         * Getter for the view project associated with this node.
         *
         * @return associated view project
         */
        public IViewProject<?> getProject() {
            // the top level node (directly under the non-ScitosTreeNode root) contains the view project instance
            if (this.parent instanceof ScitosTreeNode) {
                return ((ScitosTreeNode) this.parent).getProject();
            }
            return (IViewProject<?>) this.getUserObject();
        }

        @Override
        public String toString() {
            return this.getProject().getLabel(this.getUserObject());
        }
    }

    /**
     * Implementation/Extension of the DefaultTreeCellRenderer in order to provide non-standard icons for each project tree node depending on its
     * respective type and status.
     */
    private final class ScitosTreeNodeRenderer extends DefaultTreeCellRenderer {

        /** Icon to display for main project node in expanded state. */
        private final Icon multiModelProjectOpenIcon = ScitosIcon.PROJECT_OPEN.create();
        /** Icon to display for main project node in collapsed state. */
        private final Icon multiModelProjectClosedIcon = ScitosIcon.PROJECT_CLOSED.create();
        /** Icon to display for model group node in expanded state. */
        private final Icon multiModelGroupOpenIcon = ScitosIcon.FOLDER_OPEN.create();
        /** Icon to display for main model group node in collapsed state. */
        private final Icon multiModelGroupClosedIcon = ScitosIcon.FOLDER_CLOSED.create();
        /** Icon to display for model element. */
        private final Icon modelIcon = ScitosIcon.MODEL_ELEMENT.create();

        /** Main constructor, initializes look and feel coloring associated with text panes (instead of trees). */
        ScitosTreeNodeRenderer() {
            final Color selectionBackground = UIManager.getColor("TextPane.selectionBackground");
            this.setBackgroundSelectionColor(selectionBackground == null ? Color.BLUE : new Color(selectionBackground.getRGB()));
            final Color selectionForeground = UIManager.getColor("TextPane.selectionForeground");
            this.setTextSelectionColor(selectionForeground == null ? Color.WHITE : new Color(selectionForeground.getRGB()));
            final Color background = UIManager.getColor("TextPane.background");
            this.setBackgroundNonSelectionColor(background == null ? Color.WHITE : new Color(background.getRGB()));
            final Color foreground = UIManager.getColor("TextPane.foreground");
            this.setTextNonSelectionColor(foreground == null ? Color.BLACK : new Color(foreground.getRGB()));
        }

        @Override
        public Component getTreeCellRendererComponent(final JTree tree, final Object node, final boolean isSelected, final boolean isExpanded,
                final boolean isLeaf, final int rowIndex, final boolean cellHasFocus) {
            final JLabel renderLabel =
                    (JLabel) super.getTreeCellRendererComponent(tree, node, isSelected, isExpanded, isLeaf, rowIndex, cellHasFocus);
            if (node instanceof ScitosTreeNode) {
                final Object nodeUserObject = ((ScitosTreeNode) node).getUserObject();
                if (nodeUserObject instanceof IViewProject<?>
                        && ((IViewProject<?>) nodeUserObject).getModelObject() instanceof IMultiObjectModel<?, ?>) {
                    if (isExpanded) {
                        renderLabel.setIcon(this.multiModelProjectOpenIcon);
                    } else {
                        renderLabel.setIcon(this.multiModelProjectClosedIcon);
                    }
                } else if (nodeUserObject instanceof String) {
                    if (isExpanded) {
                        renderLabel.setIcon(this.multiModelGroupOpenIcon);
                    } else {
                        renderLabel.setIcon(this.multiModelGroupClosedIcon);
                    }
                } else {
                    renderLabel.setIcon(this.modelIcon);
                }
            }
            return renderLabel;
        }
    }
}

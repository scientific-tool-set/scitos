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

package org.hmx.scitos.view.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.hmx.scitos.core.ExportOption;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.core.option.OptionHandler;
import org.hmx.scitos.domain.IModel;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.IViewProject;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.service.IModelParseServiceProvider;
import org.hmx.scitos.view.service.IOptionPanelServiceProvider;
import org.hmx.scitos.view.service.IProjectViewServiceProvider;
import org.hmx.scitos.view.swing.option.OptionView;
import org.hmx.scitos.view.swing.util.ViewUtil;
import org.hmx.scitos.view.swing.util.WrapLayout;

/**
 * Main swing client user interface.
 */
@Singleton
public final class ScitosClient {

    /** What to display in the frame title, when current project is not saved. */
    private static final char NOT_SAVED_FLAG = '*';
    /** The modifier used for menu short cuts, in order to be flexible on META-MASK for mac and CTRL-MASK for others. */
    private static final int SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    /** Files to load at application start instead of the default welcome tab. */
    private static List<File> toLoadAtStart = new ArrayList<File>(0);

    /** The ProjectViewServiceProvider responsible for creating view projects and their respective views. */
    private final IProjectViewServiceProvider projectViewProvider;
    /** The ModelParseServiceProvider responsible for creating and persisting model objects. */
    private final IModelParseServiceProvider modelParseProvider;
    /** The OptionPanelServiceProvider responsible for creating panels in the preferences dialog. */
    final IOptionPanelServiceProvider optionPanelProvider;

    /** The (main) window of the user interface. */
    private final JFrame frame = new JFrame() {

        /**
         * lets the <code>processWindowEvent()</code> of the {@link JFrame} deal with all {@link WindowEvent}s except the WINDOW_CLOSING. In this case
         * it attempts to close all projects and exiting the application afterwards. if the user aborted the closing of one of the open projects the
         * closing of the application is stopped too
         */
        @Override
        public void processWindowEvent(final WindowEvent event) {
            if (event.getID() != WindowEvent.WINDOW_CLOSING || ScitosClient.this.quit()) {
                // no WINDOW_CLOSING or closing of projects not aborted
                super.processWindowEvent(event);
            }
        }
    };

    /** Main Menu Bar - File Menu - Item: Save. */
    private JMenuItem saveMenuItem;
    /** Main Menu Bar - File Menu - Item: Save As. */
    private JMenuItem saveAsMenuItem;
    /** Main Menu Bar - File Menu - Sub-Menu: Export. */
    private JMenu exportMenu;
    /** Main Menu Bar - Edit Menu. */
    private JMenu editMenu;
    /** Main Menu Bar - Edit Menu - Item: Un-Do. */
    private JMenuItem undoMenuItem;
    /** Main Menu Bar - Edit Menu - Item: Re-Do. */
    private JMenuItem redoMenuItem;
    /** Number of generic application entries of the Main Menu Bar - Edit Menu. */
    private final int fixedEditMenuItemCount;
    /** Main Tool Bar. */
    private final JToolBar toolBar;
    /** Main Tool Bar - Item: Save. */
    private JButton saveToolItem;
    /** Main Tool Bar - Item: Un-Do. */
    private JButton undoToolItem;
    /** Main Tool Bar - Item: Re-Do. */
    private JButton redoToolItem;
    /** Number of generic application entries of the Main Tool Bar. */
    private final int fixedToolItemCount;

    /** The actual frame content besides the Menu Bar and Tool Bar. */
    final MainView mainView;

    /**
     * Main constructor.
     *
     * @param projectViewProvider
     *            the ProjectViewServiceProvider responsible for creating view projects and their respective views
     * @param modelParseProvider
     *            the ModelParseServiceProviderImpl responsible for creating and persisting model objects
     * @param optionPanelProvider
     *            instance containing all registered instances of option panel providers, that should be invoked when displaying the application's
     *            preferences dialog
     */
    @Inject
    public ScitosClient(final IProjectViewServiceProvider projectViewProvider, final IModelParseServiceProvider modelParseProvider,
            final IOptionPanelServiceProvider optionPanelProvider) {
        this.projectViewProvider = projectViewProvider;
        this.modelParseProvider = modelParseProvider;
        this.optionPanelProvider = optionPanelProvider;
        // handle the close operations separately
        this.frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.setMinimumSize(new Dimension(600, 400));
        if (!ScitosApp.IS_MAC) {
            this.frame.setIconImage(ScitosIcon.APPLICATION.create().getImage());
        }
        this.frame.setJMenuBar(this.createMenuBar());
        this.fixedEditMenuItemCount = this.editMenu.getMenuComponentCount();
        final JPanel contentPane = new JPanel(new BorderLayout(0, 5));
        contentPane.setBorder(null);
        this.mainView = new MainView(this, ScitosClient.toLoadAtStart.isEmpty());
        contentPane.add(this.mainView);
        this.toolBar = this.createToolBar();
        this.fixedToolItemCount = this.toolBar.getComponentCount();
        contentPane.add(this.toolBar, BorderLayout.PAGE_START);
        this.frame.setContentPane(contentPane);
        for (final File singleFile : ScitosClient.toLoadAtStart) {
            this.openFile(singleFile);
        }
        ScitosClient.toLoadAtStart.clear();
        // set the title regarding the start view
        this.refreshTitle();
        this.manageMenuOptions();
        this.frame.setSize(Option.WINDOW_WIDTH.getValueAsInteger(), Option.WINDOW_HEIGHT.getValueAsInteger());
        this.setLocation(Option.WINDOW_X_LOCATION.getValueAsInteger(), Option.WINDOW_Y_LOCATION.getValueAsInteger());
        this.frame.setVisible(true);
    }

    /**
     * Create the {@link JMenuBar} including all entries and their functionalities in this {@link ScitosClient}.
     *
     * @return fully initialized {@link JMenuBar}
     */
    private JMenuBar createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        // create the "file" menu
        menuBar.add(this.createFileMenu());
        // create the "edit" menu
        menuBar.add(this.createEditMenu());
        return menuBar;
    }

    /**
     * Create the File menu in the menu bar containing options for create, open, and save projects.
     *
     * @return created File menu
     */
    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu(Message.MENUBAR_FILE.get());
        // create the "new" entry in the "file" menu
        final Map<FileType, ActionListener> newFileActions = this.createNewFileActions();
        if (newFileActions.size() == 1) {
            final JMenuItem newItem = new JMenuItem(Message.MENUBAR_FILE_NEW.get(), ScitosIcon.NEW_FILE.create());
            newItem.addActionListener(newFileActions.values().iterator().next());
            fileMenu.add(newItem);
        } else {
            final JMenu newSubMenu = new JMenu(Message.MENUBAR_FILE_NEW + "...");
            newSubMenu.setIcon(ScitosIcon.NEW_FILE.create());
            for (final Entry<FileType, ActionListener> singleType : newFileActions.entrySet()) {
                final JMenuItem newItem = new JMenuItem(singleType.getKey().getLocalizableName().get());
                newItem.addActionListener(singleType.getValue());
                newSubMenu.add(newItem);
            }
            fileMenu.add(newSubMenu);
        }

        // create the "open" entry in the "file" menu
        final JMenuItem openItem = new JMenuItem(Message.MENUBAR_FILE_OPEN.get(), ScitosIcon.FOLDER_OPEN.create());
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ScitosClient.SHORTCUT_MASK));
        openItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.open();
            }
        });
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        // create the "save" entry in the "file" menu
        this.saveMenuItem = new JMenuItem(Message.MENUBAR_FILE_SAVE.get(), ScitosIcon.SAVE_FILE.create());
        this.saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ScitosClient.SHORTCUT_MASK));
        this.saveMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.save();
            }
        });
        fileMenu.add(this.saveMenuItem);
        // create the "save to" entry in the "file" menu
        this.saveAsMenuItem = new JMenuItem(Message.MENUBAR_FILE_SAVEAS.get(), ScitosIcon.SAVEAS_FILE.create());
        this.saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | ScitosClient.SHORTCUT_MASK));
        this.saveAsMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.saveAs();
            }
        });
        fileMenu.add(this.saveAsMenuItem);
        this.exportMenu = new JMenu(Message.MENUBAR_FILE_EXPORT.get());
        this.exportMenu.setIcon(ScitosIcon.EXPORT_FILE.create());
        fileMenu.add(this.exportMenu);

        // if we are NOT on Mac OS X, we have to add the preferences and quit entries here as well
        if (!ScitosApp.IS_MAC) {
            fileMenu.addSeparator();
            final JMenuItem aboutItem = new JMenuItem(Message.MENUBAR_ABOUT.get());
            aboutItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    new AboutDialog(ScitosClient.this.getFrame()).setVisible(true);
                }
            });
            fileMenu.add(aboutItem);
            // create the "preferences" entry in the "file" menu
            final JMenuItem preferencesItem = new JMenuItem(Message.MENUBAR_PREFERENCES.get(), ScitosIcon.CONFIG.create());
            preferencesItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    OptionView.showPreferenceDialog(ScitosClient.this, ScitosClient.this.optionPanelProvider);
                }
            });
            fileMenu.add(preferencesItem);
            fileMenu.addSeparator();
            // create the "exit" entry in the "file" menu
            final JMenuItem exitItem = new JMenuItem(Message.MENUBAR_QUIT.get());
            exitItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    ScitosClient.this.quit();
                }
            });
            fileMenu.add(exitItem);
        }
        return fileMenu;
    }

    /**
     * Create the Edit menu in the menu bar offering the undo- and redo-option for the currently active tab view.
     *
     * @return created Edit menu
     */
    private JMenu createEditMenu() {
        this.editMenu = new JMenu(Message.MENUBAR_EDIT.get());
        this.undoMenuItem = this.editMenu.add(new JMenuItem(Message.MENUBAR_EDIT_UNDO.get(), ScitosIcon.UNDO_EDIT.create()));
        this.undoMenuItem.setEnabled(false);
        this.undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ScitosClient.SHORTCUT_MASK));
        this.undoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.undo();
            }
        });
        this.redoMenuItem = this.editMenu.add(new JMenuItem(Message.MENUBAR_EDIT_REDO.get(), ScitosIcon.REDO_EDIT.create()));
        this.redoMenuItem.setEnabled(false);
        this.redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ScitosClient.SHORTCUT_MASK));
        this.redoMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.redo();
            }
        });
        return this.editMenu;
    }

    /**
     * Create the main tool bar including the (default) application level items. This is used as basis to let the repectively active tab view add
     * their own items.
     *
     * @return create tool bar
     */
    private JToolBar createToolBar() {
        final JToolBar bar = new JToolBar(Message.TOOLBAR_TITLE.get());
        bar.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        bar.setRollover(true);
        final WrapLayout toolBarLayout = new WrapLayout(FlowLayout.LEADING, 2, 0);
        toolBarLayout.setAlignOnBaseline(true);
        bar.setLayout(toolBarLayout);
        final Map<FileType, ActionListener> newFileActions = this.createNewFileActions();
        final JButton newFileButton = new JButton(ScitosIcon.NEW_FILE.create());
        newFileButton.setToolTipText(Message.MENUBAR_FILE_NEW.get());
        newFileButton.setFocusable(false);
        if (newFileActions.size() == 1) {
            newFileButton.addActionListener(newFileActions.values().iterator().next());
        } else {
            final JPopupMenu menu = new JPopupMenu();
            for (final Entry<FileType, ActionListener> singleType : newFileActions.entrySet()) {
                final JMenuItem newFileItem = new JMenuItem(singleType.getKey().getLocalizableName().get());
                newFileItem.addActionListener(singleType.getValue());
                menu.add(newFileItem);
            }
            menu.addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(final PopupMenuEvent event) {
                    newFileButton.setSelected(true);
                }

                @Override
                public void popupMenuWillBecomeInvisible(final PopupMenuEvent event) {
                    newFileButton.setSelected(false);
                }

                @Override
                public void popupMenuCanceled(final PopupMenuEvent event) {
                    // nothing to do
                }
            });
            newFileButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    menu.show(newFileButton, 0, newFileButton.getHeight());
                }
            });
        }
        bar.add(newFileButton);
        this.saveToolItem = new JButton(ScitosIcon.SAVE_FILE.create());
        this.saveToolItem.setToolTipText(Message.MENUBAR_FILE_SAVE.get());
        this.saveToolItem.setFocusable(false);
        this.saveToolItem.setEnabled(false);
        this.saveToolItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.save();
            }
        });
        bar.add(this.saveToolItem);
        bar.addSeparator();
        this.undoToolItem = new JButton(ScitosIcon.UNDO_EDIT.create());
        this.undoToolItem.setToolTipText(Message.MENUBAR_EDIT_UNDO.get());
        this.undoToolItem.setFocusable(false);
        this.undoToolItem.setEnabled(false);
        this.undoToolItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.undo();
            }
        });
        bar.add(this.undoToolItem);
        this.redoToolItem = new JButton(ScitosIcon.REDO_EDIT.create());
        this.redoToolItem.setToolTipText(Message.MENUBAR_EDIT_REDO.get());
        this.redoToolItem.setFocusable(false);
        this.redoToolItem.setEnabled(false);
        this.redoToolItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                ScitosClient.this.redo();
            }
        });
        bar.add(this.redoToolItem);
        return bar;
    }

    /** Execute un-do action. */
    void undo() {
        // the menu item is only available if there is an active tab
        this.mainView.getActiveTab().undo();
        this.revalidate();
    }

    /**
     * Execute re-do action for previously applied {@link #undo()}.
     */
    void redo() {
        // the menu item is only available if there is an active tab
        this.mainView.getActiveTab().redo();
        this.revalidate();
    }

    /**
     * Create generic new file actions for all supported file types.
     *
     * @return supported file types and associated new file actions
     */
    Map<FileType, ActionListener> createNewFileActions() {
        final Map<FileType, ActionListener> result = new LinkedHashMap<FileType, ActionListener>();
        for (final FileType singleType : FileType.values()) {
            result.put(singleType, new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent event) {
                    final Class<? extends IModel<?>> modelClass = ScitosClient.this.getModelParseProvider().getModelClassForFileType(singleType);
                    final IViewProject<?> newProject = ScitosClient.this.getProjectViewProvider().createEmptyProject(modelClass);
                    if (newProject != null) {
                        ScitosClient.this.mainView.addProject(newProject);
                    }
                }
            });
        }
        return result;
    }

    /**
     * Requests refreshing of everything (tree label, tab title, tab content) associated with the given project's node. This method is safe to use
     * even from outside the UI thread, since it is synchronized with it before executing the refresh action.
     *
     * @param project
     *            project to refresh the displayed information for
     */
    public void invokeRepresentationRefresh(final IViewProject<?> project) {
        this.mainView.invokeRepresentationRefresh(project);
    }

    /** Set the title regarding to the current active projects title and save location. */
    public void refreshTitle() {
        // make sure the public method does not cause trouble in the gui thread
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final StringBuilder frameTitle = new StringBuilder(Message.CLIENT_TITLE_SCITOS.get());
                final IViewProject<?> project = ScitosClient.this.mainView.getActiveProject();
                if (project != null) {
                    if (!project.isSaved()) {
                        frameTitle.insert(0, ScitosClient.NOT_SAVED_FLAG);
                    }
                    // show save path
                    final String savePath = project.getSavePath();
                    if (savePath != null) {
                        frameTitle.append(" - ").append(savePath);
                    }
                }
                ScitosClient.this.getFrame().setTitle(frameTitle.toString());
            }
        });
    }

    /**
     * Getter for the view provider instance of this client, responsible for creating view projects and their views.
     *
     * @return this client's view provider
     */
    public IProjectViewServiceProvider getProjectViewProvider() {
        return this.projectViewProvider;
    }

    /**
     * Getter for the provider instance of this client, responsible for creating and persisting model objects.
     *
     * @return this client's input/output handler
     */
    public IModelParseServiceProvider getModelParseProvider() {
        return this.modelParseProvider;
    }

    /**
     * Getter for the provider instance of this client, responsible for creating panels in the preferences dialog.
     *
     * @return the client's option panel provider
     */
    public IOptionPanelServiceProvider getOptionPanelProvider() {
        return this.optionPanelProvider;
    }

    /**
     * Getter for the window containing the graphical user interface.
     *
     * @return the actual {@link JFrame}
     */
    public JFrame getFrame() {
        return this.frame;
    }

    /**
     * Getter for the content displaying view.
     *
     * @return the main view
     */
    public MainView getMainView() {
        return this.mainView;
    }

    /**
     * Set the view specific menu bar - edit menu items, after removing any potentially view specific tool bar items that are currently displayed.
     *
     * @param editMenuItems
     *            components (e.g. buttons) associated with this view to be added to the tool bar (contained <code>null</code> elements are
     *            interpreted as separators)
     */
    public void setEditMenuItems(final Collection<JMenuItem> editMenuItems) {
        if (this.editMenu.getMenuComponentCount() > this.fixedEditMenuItemCount) {
            // remove old view specific edit menu items
            final List<Component> oldEditMenuItems = Arrays.asList(this.editMenu.getMenuComponents());
            for (final Component oldEditMenuItem : oldEditMenuItems.subList(this.fixedEditMenuItemCount, oldEditMenuItems.size())) {
                this.editMenu.remove(oldEditMenuItem);
            }
        }
        if (!editMenuItems.isEmpty()) {
            this.editMenu.addSeparator();
            for (final JMenuItem newEditMenuItem : editMenuItems) {
                if (newEditMenuItem == null) {
                    this.editMenu.addSeparator();
                } else {
                    this.editMenu.add(newEditMenuItem);
                }
            }
        }
    }

    /**
     * Set the view specific tool bar items, after removing any potentially view specific tool bar items currently displayed.
     *
     * @param toolBarItems
     *            components (e.g. buttons) associated with this view to be added to the tool bar (contained <code>null</code> elements are
     *            interpreted as separators)
     */
    public void setToolBarItems(final Collection<Component> toolBarItems) {
        if (this.toolBar.getComponentCount() > this.fixedToolItemCount) {
            // remove old view specific tool bar items
            final List<Component> oldToolBarItems = Arrays.asList(this.toolBar.getComponents());
            for (final Component oldToolBarItem : oldToolBarItems.subList(this.fixedToolItemCount, oldToolBarItems.size())) {
                this.toolBar.remove(oldToolBarItem);
            }
        }
        if (!toolBarItems.isEmpty()) {
            this.toolBar.addSeparator();
            for (final Component newToolBarItem : toolBarItems) {
                if (newToolBarItem == null) {
                    this.toolBar.addSeparator();
                } else {
                    this.toolBar.add(newToolBarItem);
                }
            }
        }
    }

    /**
     * Display a file dialog in order to open the selected files.
     *
     * @param rememberDirectory
     *            if the directory of the chosen file should be remembered for the next occasion
     * @return selected files (empty if user aborted the dialog)
     */
    File[] getFilesForOpening(final boolean rememberDirectory) {
        // use FileDialog instead of JFileChooser because it looks more native
        final FileDialog dialog = new FileDialog(this.getFrame());
        dialog.setMode(FileDialog.LOAD);
        dialog.setDirectory(Option.WORKDIR.getValue());
        ViewUtil.centerOnParent(dialog);
        dialog.setVisible(true);
        final String selection = dialog.getFile();
        if (selection == null) {
            // user aborted the file choosing dialog
            return new File[0];
        }
        final File result = new File(dialog.getDirectory(), selection);
        if (rememberDirectory) {
            // store the default work directory
            Option.WORKDIR.setValue(result.getParentFile().getAbsolutePath());
        }
        // open the selected file
        return new File[] { result };
    }

    /** Opening files the user selects in the displayed file dialog. */
    void open() {
        final File[] selection = this.getFilesForOpening(true);
        for (final File singleFile : selection) {
            this.openFile(singleFile);
        }
    }

    /**
     * Open the specified {@link File}, add it to the list of open projects, and activate (i.e. show) it.
     *
     * @param selected
     *            {@link File} to open
     */
    public void openFile(final File selected) {
        IViewProject<?> project = null;
        try {
            final Entry<? extends IModel<?>, List<Object>> model = ScitosClient.this.getModelParseProvider().open(selected);
            project = ScitosClient.this.getProjectViewProvider().createProject(model.getKey(), selected);
            project.setOpenTabElements(model.getValue());
            ScitosClient.this.mainView.addProject(project);
        } catch (final HmxException ex) {
            MessageHandler.showException(ex);
        }
    }

    /**
     * Open a save dialog and asks the user for the path, where to save.
     *
     * @param fileExtension
     *            associated file type extension
     * @param title
     *            title of the file dialog
     * @return save path
     */
    public File getSaveDestination(final String fileExtension, final String title) {
        // use FileDialog instead of JFileChooser because it looks more native
        final FileDialog dialog = new FileDialog(this.getFrame());
        dialog.setMode(FileDialog.SAVE);
        dialog.setTitle(title);
        dialog.setDirectory(Option.WORKDIR.getValue());
        dialog.setFilenameFilter(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return new File(dir, name).isDirectory() || name.endsWith(fileExtension);
            }
        });
        ViewUtil.centerOnParent(dialog);
        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            // store the default work directory
            Option.WORKDIR.setValue(dialog.getDirectory());
            // make sure the target has the given extension
            final String path = dialog.getDirectory() + dialog.getFile();
            if (path.matches(".*\\" + fileExtension)) {
                // the FileDialog already asked to replace an existing file with the same name
                return new File(path);
            }
            // we have to append the file extension
            File targetFile = new File(path + fileExtension);
            // avoid replacing an existing file - since the dialog is already closed, we cannot ask the user again
            for (int i = 1; targetFile.exists(); i++) {
                // add a counter to the end of the file name
                final StringBuilder pathBuilder = new StringBuilder(path).append('(').append(i).append(')').append(fileExtension);
                targetFile = new File(pathBuilder.toString());
            }
            return targetFile;
        }
        return null;
    }

    /** Save the currently active project to its last used save path. */
    void save() {
        final IViewProject<?> project = this.mainView.getActiveProject();
        // hand over currently open tabs to allow them to be saved as well
        this.mainView.updateOpenTabElementsForProject(project);
        try {
            if (!project.save()) {
                this.saveAs();
            }
        } catch (final HmxException ex) {
            MessageHandler.showException(ex);
        }
    }

    /** Save the current project by asking the user for the path, where to save. */
    void saveAs() {
        final IViewProject<?> activeProject = this.mainView.getActiveProject();
        // hand over currently open tabs to allow them to be saved as well
        this.mainView.updateOpenTabElementsForProject(activeProject);
        final File path = this.getSaveDestination(activeProject.getFileType().getFileExtension(), Message.MENUBAR_FILE_SAVE.get());
        if (path != null) {
            this.frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            try {
                activeProject.saveAs(path);
            } catch (final HmxException ex) {
                MessageHandler.showException(ex);
            } finally {
                this.frame.setCursor(null);
            }
        }
    }

    /** Enable or disable the menu entries for 'Save', 'Save To', 'Undo', and 'Redo' depending on the currently open tab's state. */
    public void manageMenuOptions() {
        final AbstractProjectView<?, ?> activeTab = this.mainView.getActiveTab();
        final boolean activeState;
        if (activeTab == null) {
            activeState = false;
            this.undoMenuItem.setEnabled(false);
            this.undoToolItem.setEnabled(false);
            this.redoMenuItem.setEnabled(false);
            this.redoToolItem.setEnabled(false);
        } else {
            activeState = true;
            final boolean undoAvailable = activeTab.canUndo();
            this.undoMenuItem.setEnabled(undoAvailable);
            this.undoToolItem.setEnabled(undoAvailable);
            final boolean redoAvailable = activeTab.canRedo();
            this.redoMenuItem.setEnabled(redoAvailable);
            this.redoToolItem.setEnabled(redoAvailable);
        }
        this.saveMenuItem.setEnabled(activeState);
        this.saveToolItem.setEnabled(activeState);
        this.saveAsMenuItem.setEnabled(activeState);
        this.exportMenu.setEnabled(activeState);
    }

    /**
     * Initialize any project dependent menu entries. In the current state, this regards the available export options.
     */
    public void validateProjectMenuEntries() {
        this.exportMenu.removeAll();
        final IViewProject<?> project = this.getMainView().getActiveProject();
        if (project != null) {
            final List<ExportOption> options = this.getModelParseProvider().getSupportedExports(project.getModelObject());
            for (final ExportOption exportOption : options) {
                final ImageIcon icon;
                switch (exportOption.getTargetFileType()) {
                case HTML:
                    icon = ScitosIcon.FILE_HTML.create();
                    break;
                case ODS:
                    icon = ScitosIcon.FILE_ODS.create();
                    break;
                default:
                    icon = null;
                }
                final JMenuItem singleOptionItem = new JMenuItem(exportOption.getMenuEntry().get(), icon);
                singleOptionItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        final File target =
                                ScitosClient.this.getSaveDestination(exportOption.getTargetFileType().getExtension(),
                                        Message.MENUBAR_FILE_EXPORT.get());
                        try {
                            ScitosClient.this.getModelParseProvider().export(ScitosClient.this.getMainView().getActiveProject().getModelObject(),
                                    exportOption.getStylesheetPath(), target);
                        } catch (final HmxException ex) {
                            MessageHandler.showException(ex);
                        }
                    }
                });
                this.exportMenu.add(singleOptionItem);
            }
        }
    }

    /**
     * Close the given project. Ask to save current changes before closing it, if there are any.
     *
     * @param project
     *            view project to close
     * @return successfully closed (no unsaved changes, or user confirmed saving-discarding of them)
     */
    public boolean closeProject(final IViewProject<?> project) {
        return this.mainView.closeProject(project);
    }

    /**
     * Realize the <code>Quit</code> option in the menu bar; first asking for last save, storing the window size and position for the next start, and
     * disposing the applications frame at last.
     *
     * @return executed successfully
     */
    public boolean quit() {
        if (!this.mainView.closeAllProjects()) {
            return false;
        }
        // user had no objections to closing all open projects
        this.storeWindowSettings();
        // close application
        System.exit(0);
        return true;
    }

    /**
     * Set the location of the {@link ScitosClient} to the targeted position, relocate it if the targeted position is out of the current screen's
     * bounds.
     *
     * @param posX
     *            distance from the left screen border
     * @param posY
     *            distance from the upper screen border
     */
    public void setLocation(final int posX, final int posY) {
        this.frame.setLocation(posX, posY);
        ViewUtil.moveInsideScreen(this.frame);
    }

    /** Refresh the view state: i.e. the frame title, displayed contents of the current view, availability of general menu bar and tool bar items. */
    public void revalidate() {
        this.refreshTitle();
        this.manageMenuOptions();
        this.mainView.repaint();
        ((JComponent) this.frame.getContentPane()).revalidate();
        this.frame.getContentPane().repaint();
    }

    /** Save the current window size and position for next application start. */
    private void storeWindowSettings() {
        final Rectangle bounds = this.frame.getBounds();
        Option.WINDOW_WIDTH.setValue(String.valueOf(bounds.width));
        Option.WINDOW_HEIGHT.setValue(String.valueOf(bounds.height));
        Option.WINDOW_X_LOCATION.setValue(String.valueOf(bounds.x));
        Option.WINDOW_Y_LOCATION.setValue(String.valueOf(bounds.y));
        OptionHandler.getInstance(Option.class).persistChanges();
    }

    /**
     * Add the {@link File} to load after a new {@link ScitosClient} is initialized.
     *
     * @param val
     *            {@link File} to add for opening at startup
     */
    public static void addFileToLoadAtStart(final File val) {
        synchronized (ScitosClient.toLoadAtStart) {
            ScitosClient.toLoadAtStart.add(val);
        }
    }
}

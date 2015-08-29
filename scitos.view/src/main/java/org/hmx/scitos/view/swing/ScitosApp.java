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

import java.awt.EventQueue;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.UIManager;

import org.hmx.scitos.core.option.Option;
import org.hmx.scitos.view.FileType;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.option.OptionView;
import org.hmx.scitos.view.swing.util.MacAppEventAdapter;

import dagger.ObjectGraph;

/**
 * The central App housing the main method invoking the swing view.
 */
public final class ScitosApp {

    static final boolean IS_MAC = "Mac OS X".equalsIgnoreCase(System.getProperty("os.name"));

    /** The active view client instance. */
    private static ScitosClient client = null;

    /** Constructor: hidden due to only static methods. */
    private ScitosApp() {
        // never called
    }

    /**
     * Main method invoking the swing view.
     *
     * @param args
     *            arguments that are currently ignored
     */
    public static void main(final String[] args) {
        if (IS_MAC) {
            // set the application name in screen menu bar
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SciToS");
            // transfer the frame menu bar to screen menu bar
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // initialize apple event listeners for screen menu bar
            try {
                MacAppEventAdapter.setAboutHandler(null, ScitosApp.class.getDeclaredMethod("showAbout"));
                MacAppEventAdapter.setFileHandler(null, ScitosApp.class.getDeclaredMethod("openFile", File.class));
                MacAppEventAdapter.setPreferencesHandler(null, ScitosApp.class.getDeclaredMethod("showPreferences"));
                MacAppEventAdapter.setQuitHandler(null, ScitosApp.class.getDeclaredMethod("quit"));
                MacAppEventAdapter.setDockIconImage(ScitosIcon.APPLICATION.getResourcePath());
            } catch (SecurityException sec) {
                // ignore reflection error
            } catch (NoSuchMethodException nsm) {
                // ignore reflection error
            }
        }
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    // default: NATIVE Look and Feel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    /*
                     * set the look and feel regarding to the options file entry; may throw an exception, when there is no valid entry
                     */
                    final String chosenLaF = Option.LOOK_AND_FEEL.getValue();
                    if (chosenLaF != null) {
                        UIManager.setLookAndFeel(chosenLaF);
                    }
                } catch (final Exception expected) {
                    // ignore
                }
                try {
                    loadModulesAndShowClient();
                } catch (final Exception ex) {
                    MessageHandler.showException(ex);
                }
            }
        });
    }

    /**
     * Invoke the dependency injected module loading and initialize the main client afterwards.
     * 
     * @throws ClassNotFoundException
     *             invalid file type / module definition
     * @throws InstantiationException
     *             invalid defined module class
     * @throws IllegalAccessException
     *             invalid defined module class
     */
    static void loadModulesAndShowClient() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // collect dependency injection modules
        final List<Object> modules = new LinkedList<Object>();
        final List<Class<?>> initializerClasses = new LinkedList<Class<?>>();
        // add main module
        modules.add(new ScitosModule());
        // add all sub modules
        for (FileType singleType : FileType.values()) {
            final Class<?> moduleClass = Class.forName(singleType.getModuleClassName());
            final Class<?> moduleInitializerClass = Class.forName(singleType.getModuleInitializerClassName());
            modules.add(moduleClass.newInstance());
            initializerClasses.add(moduleInitializerClass);
        }
        // create dependency injection graph from combined modules
        final ObjectGraph objectGraph = ObjectGraph.create(modules.toArray());
        // invoke initializers
        for (Class<?> singleInitializer : initializerClasses) {
            objectGraph.get(singleInitializer);
        }
        // create actual view client instance
        ScitosApp.client = objectGraph.get(ScitosClient.class);
    }

    /**
     * Getter for the main application client (user interface).
     *
     * @return view instance
     */
    public static ScitosClient getClient() {
        return ScitosApp.client;
    }

    /**
     * Execute open file request either in the already initialized client or storing the given file to be opened after the client's initialization.
     * 
     * @param target
     *            file to open
     */
    public static void openFile(final File target) {
        if (client != null) {
            client.openFile(target);
        } else {
            ScitosClient.addFileToLoadAtStart(target);
        }
    }

    /** Display the application's name, version, and license title. */
    public static void showAbout() {
        if (client != null) {
            new AboutDialog(client.getFrame()).setVisible(true);
        }
    }

    /** Display application's preferences dialog. */
    public static void showPreferences() {
        if (client != null) {
            OptionView.showPreferenceDialog(client, client.getOptionPanelProvider());
        }
    }

    /**
     * Execute application quit request.
     * 
     * @return if the quit request was successfully executed
     */
    public static boolean quit() {
        return client == null || client.quit();
    }
}

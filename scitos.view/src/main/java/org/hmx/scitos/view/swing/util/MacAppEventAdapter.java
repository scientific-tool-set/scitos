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

package org.hmx.scitos.view.swing.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** Based on the OSXAdapter (Version: 2.0 – provided by Apple, Inc.). */
public class MacAppEventAdapter implements InvocationHandler {

    /** The object to call the targetMethod on. */
    final Object targetObject;
    /** The actual event handling method to execute. */
    final Method targetMethod;
    /** The name of the designated application event handler to be. */
    final String proxySignature;
    /**
     * The actual {@code com.apple.eawt.Application} instance to register event handlers on.
     */
    static Object macOSXApplication;

    /**
     * Pass this method an Object and Method equipped to perform application shutdown logic. The method passed should return a boolean stating whether
     * or not the quit should occur.
     *
     * @param target
     *            instance to invoke the quitHandler method on, for a handleQuit request (can be {@code null} if the quitHandler method is
     *            {@code static})
     * @param quitHandler
     *            actual method to invoke for a handleQuit request (returning a {@code boolean})
     */
    public static void setQuitHandler(final Object target, final Method quitHandler) {
        MacAppEventAdapter.setHandler(new MacAppEventAdapter("handleQuit", target, quitHandler));
    }

    /**
     * Pass this method an Object and Method equipped to display application info. They will be called when the About menu item is selected from the
     * application menu.
     *
     * @param target
     *            instance to invoke the aboutHandler method on, for a handleAbout request (can be {@code null} if the aboutHandler method is
     *            {@code static})
     * @param aboutHandler
     *            actual method to invoke for a handleAbout request
     */
    public static void setAboutHandler(final Object target, final Method aboutHandler) {
        MacAppEventAdapter.setHandler(new MacAppEventAdapter("handleAbout", target, aboutHandler));
        try {
            final Method enableAboutMethod =
                    MacAppEventAdapter.macOSXApplication.getClass().getDeclaredMethod("setEnabledAboutMenu", new Class[] { boolean.class });
            enableAboutMethod.invoke(MacAppEventAdapter.macOSXApplication, new Object[] { Boolean.valueOf(true) });
        } catch (final Exception ex) {
            System.err.println("MacAppEventAdapter could not access the About Menu");
            ex.printStackTrace();
        }
    }

    /**
     * Pass this method an Object and a Method equipped to display application options. They will be called when the Preferences menu item is selected
     * from the application menu
     *
     * @param target
     *            instance to invoke the prefsHandler method on, for a handlePreferences request (can be {@code null} if the prefsHandler method is
     *            {@code static})
     * @param prefsHandler
     *            actual method to invoke for a handlePreferences request
     */
    public static void setPreferencesHandler(final Object target, final Method prefsHandler) {
        MacAppEventAdapter.setHandler(new MacAppEventAdapter("handlePreferences", target, prefsHandler));
        try {
            final Method enablePrefsMethod =
                    MacAppEventAdapter.macOSXApplication.getClass().getDeclaredMethod("setEnabledPreferencesMenu", new Class[] { boolean.class });
            enablePrefsMethod.invoke(MacAppEventAdapter.macOSXApplication, new Object[] { Boolean.valueOf(true) });
        } catch (final Exception ex) {
            System.err.println("MacAppEventAdapter could not access the About Menu");
            ex.printStackTrace();
        }
    }

    /**
     * Pass this method an Object and a Method equipped to handle document events from the Finder. Documents are registered with the Finder via the
     * CFBundleDocumentTypes dictionary in the application bundle's Info.plist.
     *
     * @param target
     *            instance to invoke the fileHandler method on, for a handleOpenFile request (can be {@code null} if the fileHandler method is
     *            {@code static})
     * @param fileHandler
     *            actual method to invoke for a handleOpenFile request
     */
    public static void setFileHandler(final Object target, final Method fileHandler) {
        MacAppEventAdapter.setHandler(new MacAppEventAdapter("handleOpenFile", target, fileHandler) {

            // Override MacAppEventAdapter.callTarget to send information on the file to be opened
            @Override
            public boolean callTarget(final Object appleEvent) {
                if (appleEvent != null) {
                    try {
                        final Method getFilenameMethod = appleEvent.getClass().getDeclaredMethod("getFilename");
                        final String filename = (String) getFilenameMethod.invoke(appleEvent);
                        this.targetMethod.invoke(this.targetObject, new Object[] { new File(filename) });
                    } catch (final Exception ex) {
                        // ignore exceptions
                    }
                }
                return true;
            }
        });
    }

    /**
     * Pass this method the classpath URL for the image to use the application's icon in the Dock.
     *
     * @param imageLocationInClassPath
     *            path to the targeted image on the classpath
     */
    public static void setDockIconImage(final String imageLocationInClassPath) {
        try {
            final Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            if (MacAppEventAdapter.macOSXApplication == null) {
                MacAppEventAdapter.macOSXApplication = applicationClass.getDeclaredMethod("getApplication").invoke(null);
            }
            final Image icon = Toolkit.getDefaultToolkit().createImage(MacAppEventAdapter.class.getResource(imageLocationInClassPath));
            applicationClass.getDeclaredMethod("setDockIconImage", Image.class).invoke(MacAppEventAdapter.macOSXApplication, icon);
        } catch (final ClassNotFoundException cnfe) {
            System.err.println("This version of Mac OS X does not support the Apple EAWT. Dock Icon has not been set (" + cnfe + ")");
        } catch (final Exception ex) { // Likely a NoSuchMethodException or an IllegalAccessException loading/invoking eawt.Application methods
            System.err.println("Mac OS X Adapter could not talk to EAWT:");
            ex.printStackTrace();
        }
    }

    /**
     * Create a Proxy object from the passed MacAppEventAdapter and add it as an ApplicationListener.
     *
     * @param adapter
     *            instance to register
     */
    public static void setHandler(final MacAppEventAdapter adapter) {
        try {
            final Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
            if (MacAppEventAdapter.macOSXApplication == null) {
                MacAppEventAdapter.macOSXApplication = applicationClass.getDeclaredMethod("getApplication").invoke(null);
            }
            final Class<?> applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
            final Method addListenerMethod =
                    applicationClass.getDeclaredMethod("addApplicationListener", new Class[] { applicationListenerClass });
            // Create a proxy object around this handler that can be reflectively added as an Apple ApplicationListener
            final Object osxAdapterProxy =
                    Proxy.newProxyInstance(MacAppEventAdapter.class.getClassLoader(), new Class[] { applicationListenerClass }, adapter);
            addListenerMethod.invoke(MacAppEventAdapter.macOSXApplication, new Object[] { osxAdapterProxy });
        } catch (final ClassNotFoundException cnfe) {
            System.err.println("This version of Mac OS X does not support the Apple EAWT. ApplicationEvent handling has been disabled (" + cnfe
                    + ")");
        } catch (final Exception ex) { // Likely a NoSuchMethodException or an IllegalAccessException loading/invoking eawt.Application methods
            System.err.println("Mac OS X Adapter could not talk to EAWT:");
            ex.printStackTrace();
        }
    }

    /**
     * Each MacAppEventAdapter has the name of the EAWT method it intends to listen for (handleAbout, for example), the Object that will ultimately
     * perform the task, and the Method to be called on that Object Main constructor.
     *
     * @param proxySignature
     *            name of the handled application event method (i.e. handleXXX)
     * @param target
     *            instance to invoke the handler method on, for a handleXXX request (can be {@code null} if the handler method is {@code static})
     * @param handler
     *            actual method to invoke for a handleXXX request
     */
    protected MacAppEventAdapter(final String proxySignature, final Object target, final Method handler) {
        this.proxySignature = proxySignature;
        this.targetObject = target;
        this.targetMethod = handler;
    }

    /**
     * Override this method to perform any operations on the event that comes with the various callbacks. See setFileHandler above for an example.
     *
     * @param appleEvent
     *            application event to handle
     * @return if the declared handler method returned {@code null} (incl. it being {@code void}) or not empty/{@code false}
     * @throws InvocationTargetException
     *             error in the invoked handler method
     * @throws IllegalAccessException
     *             declared handler method was not accessible from this class
     */
    public boolean callTarget(final Object appleEvent) throws InvocationTargetException, IllegalAccessException {
        final Object result = this.targetMethod.invoke(this.targetObject);
        return result == null || Boolean.valueOf(result.toString()).booleanValue();
    }

    /**
     * InvocationHandler implementation - This is the entry point for our proxy object; it is called every time an ApplicationListener method is
     * invoked.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if ((this.targetMethod != null && this.proxySignature.equals(method.getName()) && args.length == 1)) {
            final Object event = args[0];
            final boolean handled = this.callTarget(event);
            if (event != null) {
                try {
                    final Method setHandledMethod = event.getClass().getDeclaredMethod("setHandled", new Class[] { boolean.class });
                    // If the target method returns a boolean, use that as a hint
                    setHandledMethod.invoke(event, new Object[] { Boolean.valueOf(handled) });
                } catch (final Exception ex) {
                    System.err.println("MacAppEventAdapter was unable to handle an ApplicationEvent: " + event);
                    ex.printStackTrace();
                }
            }
        }
        // All of the ApplicationListener methods are void; return null regardless of what happens
        return null;
    }
}
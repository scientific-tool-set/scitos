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

import java.lang.reflect.InvocationTargetException;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.ScitosIcon;

/**
 * Generates uniformed message dialogs.
 */
public final class MessageHandler {

    /**
     * The available message types for displaying dialogs.
     */
    public enum MessageType {
        /** No icon to display. */
        PLAIN(JOptionPane.PLAIN_MESSAGE, ScitosIcon.APPLICATION_INFO),
        /** Used for information messages. */
        INFO(JOptionPane.INFORMATION_MESSAGE, ScitosIcon.APPLICATION_INFO),
        /** Used for warning messages. */
        WARN(JOptionPane.WARNING_MESSAGE, ScitosIcon.APPLICATION_WARN),
        /** Used for error messages. */
        ERROR(JOptionPane.ERROR_MESSAGE, ScitosIcon.APPLICATION_ERROR);

        /**
         * The equivalent {@link JOptionPane} constant.
         */
        private final int type;
        /** The image to display in the message dialog. */
        private final ScitosIcon icon;

        /**
         * Main constructor.
         * 
         * @param type
         *            the wrapped {@link JOptionPane} constant
         * @param icon
         *            the image to display in the message dialog
         */
        private MessageType(final int type, final ScitosIcon icon) {
            this.type = type;
            this.icon = icon;
        }

        /**
         * Getter for the equivalent {@link JOptionPane} constant.
         * 
         * @return the represented {@link JOptionPane} message type
         */
        int getType() {
            return this.type;
        }

        /**
         * Getter for the image to display in the message dialog.
         * 
         * @return the message icon
         */
        public Icon getIcon() {
            return this.icon.create();
        }
    }

    /**
     * The possible return values of simple option dialogs.
     */
    public enum Choice {
        /** Chosen option: yes. */
        YES,
        /** Chosen option: no. */
        NO,
        /** Chosen option: cancel; or: closed dialog without option selection. */
        CANCEL;
    }

    /** Hidden constructor due to only static methods. */
    private MessageHandler() {
        // nothing to do
    }

    /**
     * Shows a message dialog.
     *
     * @param message
     *            message contained in the dialog
     * @param messageTitle
     *            title of the dialog
     * @param messageType
     *            style of the dialog (Error, Warning, Information)
     */
    public static void showMessage(final String message, final String messageTitle, final MessageType messageType) {
        // make sure the message is displayed in the main GUI thread
        final Runnable showMessageInUiThread = new Runnable() {

            @Override
            public void run() {
                final JFrame parentFrame;
                final ScitosClient client = ScitosApp.getClient();
                if (client == null) {
                    // client has not been initialized properly (yet)
                    parentFrame = null;
                } else {
                    parentFrame = client.getFrame();
                }
                JOptionPane.showMessageDialog(parentFrame, MessageHandler.prepareMessage(message), messageTitle, messageType.getType(),
                        messageType.getIcon());
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            showMessageInUiThread.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(showMessageInUiThread);
            } catch (final InterruptedException iex) {
                // ignore it
            } catch (final InvocationTargetException itex) {
                // ignore it
            }
        }
    }

    /**
     * Shows a question dialog with YES, NO, and CANCEL option.
     *
     * @param message
     *            question contained in the dialog
     * @param messageTitle
     *            title of the dialog
     * @return chosen option (YES, NO, CANCEL)
     */
    public static Choice showYesNoCancelDialog(final String message, final String messageTitle) {
        switch (JOptionPane.showConfirmDialog(ScitosApp.getClient().getFrame(), MessageHandler.prepareMessage(message), messageTitle,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, MessageType.WARN.getIcon())) {
        case JOptionPane.YES_OPTION:
            return Choice.YES;
        case JOptionPane.NO_OPTION:
            return Choice.NO;
        default:
            return Choice.CANCEL;
        }
    }

    /**
     * Shows a warning dialog with OK and CANCEL option.
     *
     * @param message
     *            warning message contained in the dialog
     * @param messageTitle
     *            title of the dialog
     * @return chosen option (YES, CANCEL)
     */
    public static Choice showConfirmDialog(final String message, final String messageTitle) {
        switch (JOptionPane.showConfirmDialog(ScitosApp.getClient().getFrame(), MessageHandler.prepareMessage(message), messageTitle,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, MessageType.WARN.getIcon())) {
        case JOptionPane.OK_OPTION:
            return Choice.YES;
        default:
            return Choice.CANCEL;
        }
    }

    /**
     * Shows a multiple choice dialog, where the initial choice is determined by the initialIndex parameter, selecting one of the given options.
     *
     * @param message
     *            question contained in the dialog
     * @param messageTitle
     *            title of the dialog
     * @param options
     *            available options
     * @param initialIndex
     *            index of the option to be preselected by default
     * @return index of the chosen option
     */
    public static int showOptionDialog(final String message, final String messageTitle, final String[] options, final int initialIndex) {
        return JOptionPane.showOptionDialog(ScitosApp.getClient().getFrame(), MessageHandler.prepareMessage(message), messageTitle,
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, MessageType.INFO.getIcon(), options, options[initialIndex]);
    }

    /**
     * shows a question dialog, where the user can type in a custom answer.
     *
     * @param question
     *            question contained in the dialog
     * @param messageTitle
     *            title of the dialog
     * @param initialValue
     *            value to preset when displaying the dialog
     * @return inserted answer
     */
    public static String showInputDialog(final Message question, final String messageTitle, final String initialValue) {
        return (String) JOptionPane.showInputDialog(ScitosApp.getClient().getFrame(), MessageHandler.prepareMessage(question.get()),
                messageTitle, JOptionPane.QUESTION_MESSAGE, MessageType.INFO.getIcon(), null, initialValue);
    }

    /**
     * Show the error message contained in the given {@link Exception}.
     *
     * @param exception
     *            exception to display
     */
    public static void showException(final Exception exception) {
        String localizedMessage;
        if (exception instanceof HmxException) {
            localizedMessage = exception.getLocalizedMessage();
            if (exception.getCause() != null) {
                exception.getCause().printStackTrace();
                if (((HmxException) exception).isUnknown()) {
                    localizedMessage = localizedMessage + "\n\n" + MessageHandler.extractStackTrace(exception);
                }
            }
        } else {
            exception.printStackTrace();
            localizedMessage = Message.ERROR_UNKNOWN.get() + "\n\n" + MessageHandler.extractStackTrace(exception);
        }
        MessageHandler.showMessage(localizedMessage, Message.ERROR.get(), MessageType.ERROR);
    }

    /**
     * Extract the contained error message and the full stack trace form the given error.
     *
     * @param error
     *            error or exception to get the stack trace information from
     * @return full stack trace information
     */
    private static String extractStackTrace(final Throwable error) {
        final StringBuilder traceBuffer = new StringBuilder();
        traceBuffer.append(error.getLocalizedMessage());
        for (final StackTraceElement singleElement : error.getStackTrace()) {
            traceBuffer.append('\n');
            traceBuffer.append(singleElement.toString());
        }
        return traceBuffer.toString();
    }

    /**
     * Ensure there is no empty line in the given message, which might lead to unexpected results when displayed in a dialog.
     * 
     * @param message
     *            text to display
     * @return modified text, fit for being displayed in a dialog
     */
    static String prepareMessage(final String message) {
        if (message == null) {
            return "";
        }
        return message.replaceAll("[\n\r][\n\r]", "\n \n");
    }
}

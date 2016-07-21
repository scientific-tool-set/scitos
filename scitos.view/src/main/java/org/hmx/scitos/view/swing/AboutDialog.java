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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.java.dev.designgridlayout.DesignGridLayout;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.util.ViewUtil;

/** The application's about dialog. */
public final class AboutDialog extends JDialog {

    /**
     * Constructor.
     *
     * @param parent
     *            parent frame
     */
    public AboutDialog(final JFrame parent) {
        super(parent, Message.MENUBAR_ABOUT.get(), true);
        final JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));
        final DesignGridLayout layout = new DesignGridLayout(contentPane).withoutConsistentWidthAcrossNonGridRows();
        layout.row().center().add(new JLabel(ScitosIcon.APPLICATION.createScaled(-1, 128)));
        layout.row().center().add(new JLabel("SciToS – the Scientific Tool Set"));
        layout.row().center().add(new JLabel('v' + AboutDialog.class.getPackage().getImplementationVersion()));
        layout.row().center().add(new JLabel("by " + AboutDialog.class.getPackage().getImplementationVendor()));
        layout.row().center().add(new JLabel("GNU General Public License version 3 (GPLv3)"));
        final JButton okButton = new JButton(Message.OK.get());
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                AboutDialog.this.dispose();
            }
        });
        layout.row().center().add(okButton).fill();
        this.setContentPane(contentPane);
        this.pack();
        ViewUtil.centerOnParent(this);
    }
}

package org.hmx.scitos.view.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.hmx.scitos.core.i18n.Message;
import org.hmx.scitos.view.ScitosIcon;
import org.hmx.scitos.view.swing.util.ViewUtil;

/**
 * The application's about dialog.
 */
public final class AboutDialog extends JDialog {

    /**
     * Main constructor.
     *
     * @param parent
     *            parent frame
     */
    public AboutDialog(final JFrame parent) {
        super(parent, Message.MENUBAR_ABOUT.get(), true);
        final JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        contentPane.add(new JLabel(ScitosIcon.APPLICATION.createScaled(-1, 128)), constraints);
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.gridy++;
        contentPane.add(new JLabel("SciToS – the Scientific Tool Set"), constraints);
        constraints.gridy++;
        contentPane.add(new JLabel('v' + AboutDialog.class.getPackage().getImplementationVersion()), constraints);
        constraints.gridy++;
        contentPane.add(new JLabel("by " + AboutDialog.class.getPackage().getImplementationVendor()), constraints);
        constraints.gridy++;
        contentPane.add(new JLabel("GNU General Public License version 3 (GPLv3)"), constraints);
        final JButton okButton = new JButton(Message.OK.get());
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                AboutDialog.this.dispose();
            }
        });
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy++;
        contentPane.add(okButton, constraints);
        this.setContentPane(contentPane);
        this.pack();
        ViewUtil.centerOnParent(this);
    }
}

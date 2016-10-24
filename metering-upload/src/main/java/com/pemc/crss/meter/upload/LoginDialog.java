package com.pemc.crss.meter.upload;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.util.logging.Level.SEVERE;
import javax.swing.BorderFactory;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;

public class LoginDialog extends JDialog {

    public static final int RET_CANCEL = 0;

    public static final int RET_OK = 1;

    public LoginDialog(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(parent);

        // Close the dialog when Esc is pressed
        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doClose(RET_CANCEL);
            }
        });
    }

    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        GridBagConstraints gridBagConstraints;

        fieldPanel = new JPanel();
        lblUsername = new JLabel();
        txtUsername = new JTextField();
        lblPassword = new JLabel();
        txtPassword = new JPasswordField();
        buttonPanel = new JPanel();
        btnOK = new JButton();
        btnCancel = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Login");
        setAlwaysOnTop(true);
        setResizable(false);
        setType(Type.POPUP);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });

        fieldPanel.setBorder(BorderFactory.createEtchedBorder());
        fieldPanel.setLayout(new GridBagLayout());

        lblUsername.setText("Username:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fieldPanel.add(lblUsername, gridBagConstraints);

        txtUsername.setMinimumSize(new Dimension(200, 26));
        txtUsername.setPreferredSize(new Dimension(200, 26));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fieldPanel.add(txtUsername, gridBagConstraints);

        lblPassword.setText("Password:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fieldPanel.add(lblPassword, gridBagConstraints);

        txtPassword.setText("jPasswordField1");
        txtPassword.setMinimumSize(new Dimension(200, 26));
        txtPassword.setPreferredSize(new Dimension(200, 26));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fieldPanel.add(txtPassword, gridBagConstraints);

        getContentPane().add(fieldPanel, BorderLayout.CENTER);

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        btnOK.setText("OK");
        btnOK.setPreferredSize(new Dimension(100, 29));
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okActionPerformed(evt);
            }
        });
        buttonPanel.add(btnOK);
        getRootPane().setDefaultButton(btnOK);

        btnCancel.setText("Cancel");
        btnCancel.setPreferredSize(new Dimension(100, 29));
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });
        buttonPanel.add(btnCancel);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

    private void okActionPerformed(ActionEvent evt) {//GEN-FIRST:event_okActionPerformed
        MeterDataUploader dataUploader = (MeterDataUploader) this.getParent();
        dataUploader.login(txtUsername.getText(), new String(txtPassword.getPassword()));

        doClose(RET_OK);
    }//GEN-LAST:event_okActionPerformed

    private void cancelActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelActionPerformed

    private void closeDialog(WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnCancel;
    private JButton btnOK;
    private JPanel buttonPanel;
    private JPanel fieldPanel;
    private JLabel lblPassword;
    private JLabel lblUsername;
    private JPasswordField txtPassword;
    private JTextField txtUsername;
    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;
}

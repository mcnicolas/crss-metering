package com.pemc.crss.meter.upload;

import com.pemc.crss.meter.upload.http.HeaderStatus;
import com.pemc.crss.meter.upload.http.UploadStatus;
import com.pemc.crss.meter.upload.util.FileNameFilter;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.pemc.crss.meter.upload.LoginDialog.RET_OK;
import static com.pemc.crss.meter.upload.SelectedFileUtils.retrieveFileListing;
import static com.pemc.crss.meter.upload.SettingsDialog.RET_SAVE;
import static com.pemc.crss.meter.upload.http.UploadStatus.STALE;
import static com.pemc.crss.meter.upload.http.UploadStatus.SUCCESS;
import static java.awt.Color.BLUE;
import static java.awt.Color.RED;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_AND_DIRECTORIES;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class HeaderPanel extends JPanel {

    private FileFilter fileFilterXLS = new FileNameFilter("Excel Files (xls, xlsx)", "xls", "xlsx");
    private FileFilter fileFilterMDEF = new FileNameFilter("MDEF Files (mde)", "mde");
    private FileFilter fileFilterCSV = new FileNameFilter("CSV Files (csv)", "csv");

    private MeterDataUploader parent;
    private String selectedFileExtension = "";
    private ParticipantName participant;

    int interval;

    public HeaderPanel() {
        initComponents();
    }

    public void configureComponents(MeterDataUploader parent) {
        this.parent = parent;

        btnLogout.setVisible(false);
        uploadProcessStatus.setVisible(false);

        cboCategory.addItem(new ComboBoxItem("DAILY", "Daily"));
        cboCategory.addItem(new ComboBoxItem("MONTHLY", "Monthly"));
        cboCategory.addItem(new ComboBoxItem("CORRECTED_DAILY", "Corrected Meter Data (Daily)"));
        cboCategory.addItem(new ComboBoxItem("CORRECTED_MONTHLY", "Corrected Meter Data (Monthly)"));
    }

    public void configureServices(List<ComboBoxItem> mspListing, int interval) {
        populateMSPComboBox(mspListing);
        initRadioButtons(interval);
    }

    private void populateMSPComboBox(List<ComboBoxItem> mspListing) {
        DefaultComboBoxModel<ComboBoxItem> model = (DefaultComboBoxModel<ComboBoxItem>) cboMSP.getModel();
        model.removeAllElements();

        cboMSP.addItem(new ComboBoxItem("", ""));

        for (ComboBoxItem comboBoxItem : mspListing) {
            cboMSP.addItem(comboBoxItem);
        }

        participant = parent.getParticipant();
        if (participant != null) {
            updateSelectedMSP(parent.getParticipant().getShortName());
            cboMSP.setEnabled(false);
        }
    }

    private void initRadioButtons(int interval) {
        this.interval = interval;
        if (interval == 5) {
            radioUploadDataAsIs.setSelected(true);
            radioUploadDataAsIs.setEnabled(true);
            radioConvert.setEnabled(true);
        } else {
            radioUploadDataAsIs.setEnabled(false);
            radioConvert.setEnabled(false);
        }
    }

    public void updateSelectedMSP(String shortName) {
        DefaultComboBoxModel<ComboBoxItem> model = (DefaultComboBoxModel<ComboBoxItem>) cboMSP.getModel();

        for (int i = 0; i < model.getSize(); i++) {
            ComboBoxItem item = model.getElementAt(i);
            if (equalsIgnoreCase(item.getValue(), shortName)) {
                cboMSP.setSelectedIndex(i);
            }
        }
    }

    public void updateTransactionID(String transactionID) {
        txtTransactionID.setText(transactionID);
    }

    public void updateProcessDuration(String duration) {
        processDuration.setText(duration);
    }

    public void toggleProcessComplete(UploadStatus status) {
        if (status == SUCCESS) {
            uploadProcessStatus.setText("Finished Processing");
            uploadProcessStatus.setBackground(BLUE);
        } else if (status == STALE) {
            uploadProcessStatus.setText("Error Processing");
            uploadProcessStatus.setBackground(RED);
        } else {
        }

        uploadProcessStatus.setVisible(status != null);
    }

    public void updateProcessDisplay(HeaderStatus headerStatus, LocalDateTime uploadStartTime) {
        LocalDateTime to = LocalDateTime.now();

        if (headerStatus.getUploadStatus() == SUCCESS) {
            uploadProcessStatus.setText("Finished Processing");
            uploadProcessStatus.setBackground(BLUE);
        } else if (headerStatus.getUploadStatus() == STALE) {
            uploadProcessStatus.setText("Error Processing");
            uploadProcessStatus.setBackground(RED);
        }

        Duration diff = Duration.between(uploadStartTime, to);

        LocalTime localTime = LocalTime.MIDNIGHT.plus(diff);
        String processDuration = DateTimeFormatter.ofPattern("mm:ss").format(localTime);

        updateProcessDuration(processDuration);
        toggleProcessComplete(headerStatus.getUploadStatus());
        readyToUploadToolbar();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        GridBagConstraints gridBagConstraints;

        convertGroup = new ButtonGroup();
        toolbarPanel = new JPanel();
        btnSelectFiles = new JButton();
        btnClearTable = new JButton();
        btnUpload = new JButton();
        btnSettings = new JButton();
        btnLogout = new JButton();
        btnLogin = new JButton();
        fieldPanel = new JPanel();
        lblCategory = new JLabel();
        cboCategory = new JComboBox<>();
        lblMSP = new JLabel();
        cboMSP = new JComboBox<>();
        uploadStatusPanel = new JPanel();
        transactionPanel = new JPanel();
        lblTransactionID = new JLabel();
        txtTransactionID = new JTextField();
        lblProcessDuration = new JLabel();
        processDuration = new JLabel();
        uploadProcessStatus = new JLabel();
        convertPanel = new JPanel();
        radioUploadDataAsIs = new JRadioButton();
        radioConvert = new JRadioButton();

        setLayout(new BorderLayout());

        btnSelectFiles.setIcon(new ImageIcon(getClass().getResource("/images/Transaction List Filled-50.png"))); // NOI18N
        btnSelectFiles.setToolTipText("Select Files");
        btnSelectFiles.setEnabled(false);
        btnSelectFiles.setFocusable(false);
        btnSelectFiles.setHorizontalTextPosition(SwingConstants.CENTER);
        btnSelectFiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectFilesActionPerformed(evt);
            }
        });
        toolbarPanel.add(btnSelectFiles);

        btnClearTable.setIcon(new ImageIcon(getClass().getResource("/images/Broom-48.png"))); // NOI18N
        btnClearTable.setToolTipText("Clear Selection");
        btnClearTable.setEnabled(false);
        btnClearTable.setFocusable(false);
        btnClearTable.setHorizontalTextPosition(SwingConstants.CENTER);
        btnClearTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearSelectionActionPerformed(evt);
            }
        });
        toolbarPanel.add(btnClearTable);

        btnUpload.setIcon(new ImageIcon(getClass().getResource("/images/Upload to the Cloud-50.png"))); // NOI18N
        btnUpload.setToolTipText("Upload Files");
        btnUpload.setEnabled(false);
        btnUpload.setFocusable(false);
        btnUpload.setHorizontalTextPosition(SwingConstants.CENTER);
        btnUpload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                uploadActionPerformed(evt);
            }
        });
        toolbarPanel.add(btnUpload);

        btnSettings.setIcon(new ImageIcon(getClass().getResource("/images/Vertical Settings Mixer-50.png"))); // NOI18N
        btnSettings.setToolTipText("Settings");
        btnSettings.setFocusable(false);
        btnSettings.setHorizontalTextPosition(SwingConstants.CENTER);
        btnSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                settingsActionPerformed(evt);
            }
        });
        toolbarPanel.add(btnSettings);

        btnLogout.setIcon(new ImageIcon(getClass().getResource("/images/Export-48.png"))); // NOI18N
        btnLogout.setToolTipText("Logout");
        btnLogout.setEnabled(false);
        btnLogout.setFocusable(false);
        btnLogout.setHorizontalTextPosition(SwingConstants.CENTER);
        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                logoutActionPerformed(evt);
            }
        });
        toolbarPanel.add(btnLogout);

        btnLogin.setIcon(new ImageIcon(getClass().getResource("/images/Key-48.png"))); // NOI18N
        btnLogin.setToolTipText("Login");
        btnLogin.setFocusable(false);
        btnLogin.setHorizontalTextPosition(SwingConstants.CENTER);
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loginActionPerformed(evt);
            }
        });
        toolbarPanel.add(btnLogin);

        add(toolbarPanel, BorderLayout.WEST);

        fieldPanel.setLayout(new GridBagLayout());

        lblCategory.setText("Category:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        fieldPanel.add(lblCategory, gridBagConstraints);

        cboCategory.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
        fieldPanel.add(cboCategory, gridBagConstraints);

        lblMSP.setText("MSP:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        fieldPanel.add(lblMSP, gridBagConstraints);

        cboMSP.setEnabled(false);
        cboMSP.setPreferredSize(new Dimension(350, 27));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fieldPanel.add(cboMSP, gridBagConstraints);

        add(fieldPanel, BorderLayout.EAST);

        uploadStatusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        transactionPanel.setLayout(new GridBagLayout());

        lblTransactionID.setText("Transaction ID:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 0);
        transactionPanel.add(lblTransactionID, gridBagConstraints);

        txtTransactionID.setEditable(false);
        txtTransactionID.setHorizontalAlignment(JTextField.CENTER);
        txtTransactionID.setMaximumSize(new Dimension(300, 26));
        txtTransactionID.setMinimumSize(new Dimension(300, 26));
        txtTransactionID.setPreferredSize(new Dimension(300, 26));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 0, 5, 10);
        transactionPanel.add(txtTransactionID, gridBagConstraints);

        lblProcessDuration.setText("Process Duration:");
        lblProcessDuration.setMaximumSize(new Dimension(120, 16));
        lblProcessDuration.setMinimumSize(new Dimension(120, 16));
        lblProcessDuration.setPreferredSize(new Dimension(120, 16));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 10, 5, 0);
        transactionPanel.add(lblProcessDuration, gridBagConstraints);

        processDuration.setMaximumSize(new Dimension(60, 16));
        processDuration.setMinimumSize(new Dimension(60, 16));
        processDuration.setPreferredSize(new Dimension(60, 16));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        transactionPanel.add(processDuration, gridBagConstraints);

        uploadProcessStatus.setBackground(Color.blue);
        uploadProcessStatus.setForeground(new Color(255, 255, 255));
        uploadProcessStatus.setHorizontalAlignment(SwingConstants.CENTER);
        uploadProcessStatus.setText("Finished Processing");
        uploadProcessStatus.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        uploadProcessStatus.setMaximumSize(new Dimension(150, 26));
        uploadProcessStatus.setMinimumSize(new Dimension(150, 26));
        uploadProcessStatus.setOpaque(true);
        uploadProcessStatus.setPreferredSize(new Dimension(150, 26));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(5, 20, 5, 5);
        transactionPanel.add(uploadProcessStatus, gridBagConstraints);

        uploadStatusPanel.add(transactionPanel);

        convertGroup.add(radioUploadDataAsIs);
        radioUploadDataAsIs.setText("Upload Data As Is");
        convertPanel.add(radioUploadDataAsIs);

        convertGroup.add(radioConvert);
        radioConvert.setText("Convert To 5-Min");
        convertPanel.add(radioConvert);

        uploadStatusPanel.add(convertPanel);

        add(uploadStatusPanel, BorderLayout.SOUTH);
    }//GEN-END:initComponents

    private void selectFilesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_selectFilesActionPerformed
        String path = parent.getFilePath();
        JFileChooser fileChooser = new JFileChooser(path);

        String selectedCategory = ((ComboBoxItem) cboCategory.getSelectedItem()).getValue();
        if (equalsAnyIgnoreCase(selectedCategory, "DAILY", "MONTHLY")) {
            fileChooser.addChoosableFileFilter(fileFilterMDEF);
        }

//        fileChooser.addChoosableFileFilter(fileFilterXLS);
        fileChooser.addChoosableFileFilter(fileFilterCSV);
        fileChooser.setAcceptAllFileFilterUsed(false);

        fileChooser.setFileHidingEnabled(true);
        fileChooser.setFileSelectionMode(FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);
        int action = fileChooser.showOpenDialog(this);

        if (action == APPROVE_OPTION) {
            FileNameFilter fileFilter = (FileNameFilter) fileChooser.getFileFilter();

            updateFilePath(fileChooser.getSelectedFiles());
            List<FileBean> selectedFiles = retrieveFileListing(fileChooser.getSelectedFiles(), fileFilter.getExtensions());

            if (selectedFiles.isEmpty()) {
                showMessageDialog(parent, "No files found matching " + fileFilter.getDescription(),
                        "Missing Files", WARNING_MESSAGE);
            } else if (selectedFiles.size() > 10000) {
                showMessageDialog(parent, "Number of files selected is greater than 10,000",
                        "Too Many Files Selected", WARNING_MESSAGE);
            } else {
                FileBean fileBean = selectedFiles.get(0);
                selectedFileExtension = getExtension(fileBean.getPath().getFileName().toString());

                parent.updateTableDisplay(selectedFiles);
                btnSelectFiles.setEnabled(false);
                btnClearTable.setEnabled(true);
                btnUpload.setEnabled(true);

                if (fileFilter != fileFilterCSV) {
                    radioUploadDataAsIs.setEnabled(false);
                    radioConvert.setEnabled(false);
                    convertGroup.clearSelection();
                }
            }
        }
    }//GEN-LAST:event_selectFilesActionPerformed

    private void updateFilePath(File[] selectedFiles) {
        parent.updateFilePath(selectedFiles[0].getParent());
    }

    private void uploadActionPerformed(ActionEvent evt) {//GEN-FIRST:event_uploadActionPerformed
        String category = ((ComboBoxItem) cboCategory.getSelectedItem()).getValue();
        boolean convertToFiveMin = radioConvert.isSelected();

        if (!equalsAnyIgnoreCase(category, "daily", "monthly") && equalsIgnoreCase(selectedFileExtension, "mde")) {
            showMessageDialog(parent, "MDEF files can only be uploaded for Daily category.", "File Validation Error",
                    ERROR_MESSAGE);
            cboCategory.requestFocus();

            return;
        }

        // Validate MSP
        String mspShortName = ((ComboBoxItem) cboMSP.getSelectedItem()).getValue();

        if (isBlank(mspShortName)) {
            showMessageDialog(parent, "Please select an MSP", "Blank MSP Error", ERROR_MESSAGE);
            cboMSP.requestFocus();

            return;
        }

        uploadingToolbar();
        parent.uploadData(category, mspShortName, convertToFiveMin);
    }//GEN-LAST:event_uploadActionPerformed

    private void settingsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_settingsActionPerformed
        SettingsDialog settingsDialog = new SettingsDialog(parent, true);
        settingsDialog.configureComponents();
        settingsDialog.setVisible(true);

        if (settingsDialog.getReturnStatus() == RET_SAVE) {
            parent.saveSettings(settingsDialog.getServerURL(), settingsDialog.getDigiCertPath(), settingsDialog.getDigiCertPassPhrase());
        }
    }//GEN-LAST:event_settingsActionPerformed

    private void clearSelectionActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearSelectionActionPerformed
        parent.clearSelectedFiles();

        btnSelectFiles.setEnabled(true);
        btnClearTable.setEnabled(false);
        btnUpload.setEnabled(false);

        toggleProcessComplete(null);
        updateTransactionID("");
        updateProcessDuration("");

        selectedFileExtension = "";

        initRadioButtons(interval);
    }//GEN-LAST:event_clearSelectionActionPerformed

    private void logoutActionPerformed(ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
        parent.logout();
        loggedOutToolbar();
    }//GEN-LAST:event_logoutActionPerformed

    private void loginActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loginActionPerformed
        LoginDialog loginDialog = new LoginDialog(parent, true);
        loginDialog.setVisible(true);

        if (loginDialog.getReturnStatus() == RET_OK) {
            parent.login(loginDialog.getUsername(), loginDialog.getPassword());
        }
    }//GEN-LAST:event_loginActionPerformed

    // TODO: Find a more elegant way of enabling/disabling buttons
    public void resetStatus() {
        processDuration.setText("00:00");
        uploadProcessStatus.setText("");
        uploadProcessStatus.setVisible(false);
    }

    public void uploadingToolbar() {
        btnSelectFiles.setEnabled(false);
        btnClearTable.setEnabled(false);
        btnUpload.setEnabled(false);

        btnLogin.setEnabled(false);
        btnLogin.setVisible(false);

        btnSettings.setEnabled(false);

        btnLogout.setEnabled(false);
        btnLogout.setVisible(true);

        cboCategory.setEnabled(false);
        cboMSP.setEnabled(false);

        radioUploadDataAsIs.setEnabled(false);
        radioConvert.setEnabled(false);
    }

    public void readyToUploadToolbar() {
        btnSelectFiles.setEnabled(false);
        btnClearTable.setEnabled(true);
        btnUpload.setEnabled(true);

        btnLogin.setEnabled(false);
        btnLogin.setVisible(false);

        btnSettings.setEnabled(false);

        btnLogout.setEnabled(true);
        btnLogout.setVisible(true);

        cboCategory.setEnabled(true);

        if (participant != null) {
            cboMSP.setEnabled(false);
        } else {
            cboMSP.setEnabled(true);
        }

        initRadioButtons(interval);
    }

    public void disableAllToolbar() {
        btnSelectFiles.setEnabled(false);
        btnClearTable.setEnabled(false);
        btnUpload.setEnabled(false);

        btnLogin.setEnabled(false);
        btnLogin.setVisible(true);

        btnSettings.setEnabled(false);

        btnLogout.setEnabled(false);
        btnLogout.setVisible(false);

        cboCategory.setEnabled(false);
        cboMSP.setEnabled(false);

        radioUploadDataAsIs.setEnabled(false);
        radioConvert.setEnabled(false);
    }

    public void loggedInToolbar() {
        btnSelectFiles.setEnabled(true);
        btnClearTable.setEnabled(false);
        btnUpload.setEnabled(false);

        btnLogin.setEnabled(false);
        btnLogin.setVisible(false);

        btnSettings.setEnabled(false);

        btnLogout.setEnabled(true);
        btnLogout.setVisible(true);

        cboCategory.setEnabled(true);

        if (participant != null) {
            cboMSP.setEnabled(false);
        } else {
            cboMSP.setEnabled(true);
        }
    }

    public void loggedOutToolbar() {
        btnSelectFiles.setEnabled(false);
        btnClearTable.setEnabled(false);
        btnUpload.setEnabled(false);

        btnLogin.setEnabled(true);
        btnLogin.setVisible(true);

        btnSettings.setEnabled(true);

        btnLogout.setEnabled(false);
        btnLogout.setVisible(false);

        cboCategory.setEnabled(false);
        cboMSP.setEnabled(false);

        radioUploadDataAsIs.setEnabled(false);
        radioConvert.setEnabled(false);

        updateTransactionID("");
        updateProcessDuration("");
        toggleProcessComplete(null);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btnClearTable;
    private JButton btnLogin;
    private JButton btnLogout;
    private JButton btnSelectFiles;
    private JButton btnSettings;
    private JButton btnUpload;
    private JComboBox<ComboBoxItem> cboCategory;
    private JComboBox<ComboBoxItem> cboMSP;
    private ButtonGroup convertGroup;
    private JPanel convertPanel;
    private JPanel fieldPanel;
    private JLabel lblCategory;
    private JLabel lblMSP;
    private JLabel lblProcessDuration;
    private JLabel lblTransactionID;
    private JLabel processDuration;
    private JRadioButton radioConvert;
    private JRadioButton radioUploadDataAsIs;
    private JPanel toolbarPanel;
    private JPanel transactionPanel;
    private JTextField txtTransactionID;
    private JLabel uploadProcessStatus;
    private JPanel uploadStatusPanel;
    // End of variables declaration//GEN-END:variables

}

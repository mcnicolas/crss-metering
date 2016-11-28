package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Slf4j
public class MeterDataUploader extends JFrame {

    private String token;
    private String username;
    private String fullName;
    private String userType;
    private ParticipantName participant;
    private Properties properties;

    public MeterDataUploader() {
        initComponents();
        initProperties();
    }

    private void initProperties() {
        properties = new Properties();

        File config = new File("config.properties");
        if (!config.exists()) {
            saveSettings("http://localhost:8080");
        }

        try (Reader reader = new FileReader("config.properties")) {
            properties.load(reader);
            RestUtil.setBaseURL(properties.getProperty("URL"));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void configureComponents() {
//        centerOnScreen();

        headerPanel.configureComponents(this);
        tablePanel.configureComponents(this);
    }

    public void configureServices() {
        headerPanel.configureServices();
    }

    public void updateTableDisplay(List<FileBean> selectedFiles) {
        fileCount.setText(String.valueOf(selectedFiles.size()));

        long totalSize = selectedFiles.stream()
                .map(FileBean::getSize)
                .mapToLong(Long::longValue)
                .sum();

        totalFileSize.setText(byteCountToDisplaySize(totalSize));

        ((CardLayout)statusBarPanel.getLayout()).show(statusBarPanel, "Status");

        tablePanel.updateTableDisplay(selectedFiles);
    }

    public void uploadData(String category, String mspShortName) {
        ((CardLayout)statusBarPanel.getLayout()).show(statusBarPanel, "Upload");

        String transactionID = UUID.randomUUID().toString();
        List<FileBean> selectedFiles = tablePanel.getSelectedFiles();
        uploadProgressBar.setValue(0);
        uploadProgressBar.setMinimum(0);
        uploadProgressBar.setMaximum(selectedFiles.size() + 2);

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {

                int counter = 0;
                long headerID = -1;
                if (token != null) {
                    headerID = RestUtil.sendHeader(transactionID, username, selectedFiles.size(), category, token);
                    setProgress(++counter);
                }

                for (FileBean selectedFile : selectedFiles) {
                    RestUtil.sendFile(headerID, transactionID, selectedFile, category, mspShortName, token);
                    setProgress(++counter);

                    log.debug("Uploading file:{}", selectedFile.getPath().getFileName().toString());
                }

                RestUtil.sendTrailer(transactionID, token);
                setProgress(++counter);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage(), e);

                    String errorMessage = e.getMessage().substring(e.getMessage().indexOf(":")).trim();
                    showMessageDialog(MeterDataUploader.this, errorMessage, "Upload Error", ERROR_MESSAGE);
                }

                // TODO: Change status bar to a different card
                System.out.println("Done uploading files");
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if (equalsIgnoreCase(evt.getPropertyName(), "progress")) {
                uploadProgressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();
    }

    public void login(String username, String password) {
        // TODO: Use SwingWorker

        try {
            token = RestUtil.login(username, password);

            if (token == null) {
                showMessageDialog(this, "Invalid login", "Error", ERROR_MESSAGE);
            } else {
                log.debug("Logged in with token: {}", token);

                List<String> userData = RestUtil.getUserType(token);

                if (userData.size() > 1 &&
                        (!equalsIgnoreCase(userData.get(1), "PEMC") && !equalsIgnoreCase(userData.get(1), "MSP"))) {
                    showMessageDialog(this, "Invalid login", "Error", ERROR_MESSAGE);
                    token = null;

                    return;
                }

                this.username = username;

                userFullName1.setText(userData.get(0));
                userFullName2.setText(userData.get(0));
                ((CardLayout)statusBarPanel.getLayout()).show(statusBarPanel, "LoggedIn");

                userType = userData.get(1);

                // TODO: Avoid redundant rest call
                if (equalsIgnoreCase(userType, "MSP")) {
                    participant = RestUtil.getParticipant(token);
                }

                log.debug("User type: {}", userType);

                headerPanel.enableToolbar();
            }
        } catch (LoginException e) {
            log.error(e.getMessage(), e);

            showMessageDialog(this, e.getMessage(), "Error", ERROR_MESSAGE);
        }
    }

    public void logout() {
        token = null;
        username = null;
        userType = null;
        participant = null;

        tablePanel.clearSelectedFiles();

        ((CardLayout)statusBarPanel.getLayout()).show(statusBarPanel, "blank");
    }

    public void clearSelectedFiles() {
        ((CardLayout)statusBarPanel.getLayout()).show(statusBarPanel, "LoggedIn");

        // TODO: Clear status

        tablePanel.clearSelectedFiles();
    }

    public List<ComboBoxItem> getMSPListing() {
        return RestUtil.getMSPListing(token);
    }

    public ParticipantName getParticipant() {
        return participant;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void saveSettings(String serverURL) {
        properties.put("URL", serverURL);
        RestUtil.setBaseURL(properties.getProperty("URL"));

        try (Writer writer = new FileWriter("config.properties")) {
            properties.store(writer, "Saving updated server URL");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void centerOnScreen() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
        int height = graphicsDevices[0].getDisplayMode().getHeight();
        int width = graphicsDevices[0].getDisplayMode().getWidth();
        Dimension screen = new Dimension(width, height);

        Dimension size = new Dimension(this.getWidth(), this.getHeight());

        setSize(size);

        setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents
        GridBagConstraints gridBagConstraints;

        headerPanel = new HeaderPanel();
        tablePanel = new TablePanel();
        statusBarPanel = new JPanel();
        blankPanel = new JPanel();
        loggedinPanel = new JPanel();
        centerStatusPanel1 = new JPanel();
        userStatusPanel1 = new JPanel();
        lblUser1 = new JLabel();
        userFullName1 = new JLabel();
        statusPanel = new JPanel();
        centerStatusPanel2 = new JPanel();
        fileStatsPanel = new JPanel();
        lblTotalFileSize = new JLabel();
        totalFileSize = new JLabel();
        lblFileCount = new JLabel();
        fileCount = new JLabel();
        userStatusPanel2 = new JPanel();
        lblUser2 = new JLabel();
        userFullName2 = new JLabel();
        uploadProgressPanel = new JPanel();
        leftUploadPanel = new JPanel();
        lblTime = new JLabel();
        uploadTimer = new JLabel();
        centerUploadPanel = new JPanel();
        uploadProgressBar = new JProgressBar();
        rightUploadPanel = new JPanel();
        lblUploadSpeed = new JLabel();
        uploadSpeed = new JLabel();
        lblTimeRemaining = new JLabel();
        timeRemaining = new JLabel();
        initializeProgressPanel = new JPanel();
        lblUploadStatus = new JLabel();
        initializeProgressBar = new JProgressBar();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Meter Quantity Uploader");
        setMinimumSize(new Dimension(700, 550));
        setResizable(false);
        getContentPane().add(headerPanel, BorderLayout.NORTH);

        tablePanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
        getContentPane().add(tablePanel, BorderLayout.CENTER);

        statusBarPanel.setLayout(new CardLayout());

        blankPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        statusBarPanel.add(blankPanel, "blank");

        loggedinPanel.setLayout(new BorderLayout());

        centerStatusPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        loggedinPanel.add(centerStatusPanel1, BorderLayout.CENTER);

        userStatusPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));

        lblUser1.setText("User:");
        userStatusPanel1.add(lblUser1);

        userFullName1.setText("Firstname A. Lastname");
        userFullName1.setMaximumSize(new Dimension(175, 16));
        userFullName1.setMinimumSize(new Dimension(175, 16));
        userFullName1.setPreferredSize(new Dimension(175, 16));
        userStatusPanel1.add(userFullName1);

        loggedinPanel.add(userStatusPanel1, BorderLayout.EAST);

        statusBarPanel.add(loggedinPanel, "LoggedIn");

        statusPanel.setLayout(new BorderLayout());

        centerStatusPanel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        centerStatusPanel2.setLayout(new BorderLayout());

        fileStatsPanel.setLayout(new GridBagLayout());

        lblTotalFileSize.setText("Total Size:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        fileStatsPanel.add(lblTotalFileSize, gridBagConstraints);

        totalFileSize.setText("50 MB");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 15);
        fileStatsPanel.add(totalFileSize, gridBagConstraints);

        lblFileCount.setText("File Count:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        fileStatsPanel.add(lblFileCount, gridBagConstraints);

        fileCount.setText("100");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        fileStatsPanel.add(fileCount, gridBagConstraints);

        centerStatusPanel2.add(fileStatsPanel, BorderLayout.WEST);

        statusPanel.add(centerStatusPanel2, BorderLayout.CENTER);

        userStatusPanel2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));

        lblUser2.setText("User:");
        userStatusPanel2.add(lblUser2);

        userFullName2.setText("Firstname A. Lastname");
        userFullName2.setMaximumSize(new Dimension(175, 16));
        userFullName2.setMinimumSize(new Dimension(175, 16));
        userFullName2.setPreferredSize(new Dimension(175, 16));
        userStatusPanel2.add(userFullName2);

        statusPanel.add(userStatusPanel2, BorderLayout.EAST);

        statusBarPanel.add(statusPanel, "Status");

        uploadProgressPanel.setLayout(new BorderLayout());

        leftUploadPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        leftUploadPanel.add(lblTime);

        uploadTimer.setHorizontalAlignment(SwingConstants.RIGHT);
        uploadTimer.setMaximumSize(new Dimension(40, 16));
        uploadTimer.setMinimumSize(new Dimension(40, 16));
        uploadTimer.setPreferredSize(new Dimension(40, 16));
        leftUploadPanel.add(uploadTimer);

        uploadProgressPanel.add(leftUploadPanel, BorderLayout.WEST);

        centerUploadPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        centerUploadPanel.setLayout(new GridBagLayout());

        uploadProgressBar.setDoubleBuffered(true);
        uploadProgressBar.setMaximumSize(new Dimension(400, 20));
        uploadProgressBar.setMinimumSize(new Dimension(400, 20));
        uploadProgressBar.setPreferredSize(new Dimension(400, 20));
        uploadProgressBar.setStringPainted(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        centerUploadPanel.add(uploadProgressBar, gridBagConstraints);

        uploadProgressPanel.add(centerUploadPanel, BorderLayout.CENTER);

        rightUploadPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 3), new SoftBevelBorder(BevelBorder.LOWERED)));

        lblUploadSpeed.setMaximumSize(new Dimension(90, 16));
        lblUploadSpeed.setMinimumSize(new Dimension(90, 16));
        lblUploadSpeed.setPreferredSize(new Dimension(90, 16));
        rightUploadPanel.add(lblUploadSpeed);

        uploadSpeed.setMaximumSize(new Dimension(60, 16));
        uploadSpeed.setMinimumSize(new Dimension(60, 16));
        uploadSpeed.setPreferredSize(new Dimension(60, 16));
        rightUploadPanel.add(uploadSpeed);

        lblTimeRemaining.setMaximumSize(new Dimension(110, 16));
        lblTimeRemaining.setMinimumSize(new Dimension(110, 16));
        lblTimeRemaining.setPreferredSize(new Dimension(110, 16));
        rightUploadPanel.add(lblTimeRemaining);

        timeRemaining.setMaximumSize(new Dimension(40, 16));
        timeRemaining.setMinimumSize(new Dimension(40, 16));
        timeRemaining.setPreferredSize(new Dimension(40, 16));
        rightUploadPanel.add(timeRemaining);

        uploadProgressPanel.add(rightUploadPanel, BorderLayout.EAST);

        statusBarPanel.add(uploadProgressPanel, "Upload");

        initializeProgressPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3), new SoftBevelBorder(BevelBorder.LOWERED)));
        initializeProgressPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        lblUploadStatus.setText("Loading MSP Data");
        initializeProgressPanel.add(lblUploadStatus);

        initializeProgressBar.setPreferredSize(new Dimension(300, 20));
        initializeProgressPanel.add(initializeProgressBar);

        statusBarPanel.add(initializeProgressPanel, "Initialize");

        getContentPane().add(statusBarPanel, BorderLayout.SOUTH);

        setSize(new Dimension(960, 505));
        setLocationRelativeTo(null);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel blankPanel;
    private JPanel centerStatusPanel1;
    private JPanel centerStatusPanel2;
    private JPanel centerUploadPanel;
    private JLabel fileCount;
    private JPanel fileStatsPanel;
    private HeaderPanel headerPanel;
    private JProgressBar initializeProgressBar;
    private JPanel initializeProgressPanel;
    private JLabel lblFileCount;
    private JLabel lblTime;
    private JLabel lblTimeRemaining;
    private JLabel lblTotalFileSize;
    private JLabel lblUploadSpeed;
    private JLabel lblUploadStatus;
    private JLabel lblUser1;
    private JLabel lblUser2;
    private JPanel leftUploadPanel;
    private JPanel loggedinPanel;
    private JPanel rightUploadPanel;
    private JPanel statusBarPanel;
    private JPanel statusPanel;
    private TablePanel tablePanel;
    private JLabel timeRemaining;
    private JLabel totalFileSize;
    private JProgressBar uploadProgressBar;
    private JPanel uploadProgressPanel;
    private JLabel uploadSpeed;
    private JLabel uploadTimer;
    private JLabel userFullName1;
    private JLabel userFullName2;
    private JPanel userStatusPanel1;
    private JPanel userStatusPanel2;
    // End of variables declaration//GEN-END:variables

}

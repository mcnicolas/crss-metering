package com.pemc.crss.meter.upload;

import com.pemc.crss.meter.upload.http.FileStatus;
import com.pemc.crss.meter.upload.http.HeaderStatus;
import com.pemc.crss.meter.upload.table.UploadData;
import com.pemc.crss.meter.upload.table.UploadType;
import lombok.extern.slf4j.Slf4j;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static com.pemc.crss.meter.upload.ErrorDialog.showErrorDialog;
import static com.pemc.crss.meter.upload.http.UploadStatus.INPROGRESS;
import static com.pemc.crss.meter.upload.http.UploadStatus.STALE;
import static com.pemc.crss.meter.upload.http.UploadStatus.SUCCESS;
import static com.pemc.crss.meter.upload.table.UploadType.FILE;
import static com.pemc.crss.meter.upload.table.UploadType.HEADER;
import static com.pemc.crss.meter.upload.util.ErrorParserUtil.parseErrorMessage;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.LocalDateTime.now;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Slf4j
public class MeterDataUploader extends JFrame {

    public static final int ONE_SECOND = 1000;
    public static final int STALE_UPLOAD_TIMEOUT = 60000;

    private HttpHandler httpHandler;
    private String username;
    private String fullName;
    private String userType;
    private ParticipantName participant;
    private List<ComboBoxItem> mspListing;
    private Properties properties;
    private Timer processTimer;
    private LocalDateTime uploadStartTime;

    public MeterDataUploader(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;

        processTimer = new Timer(53, new ProcessTimerListener());
        processTimer.setInitialDelay(0);

        initComponents();
        initProperties();

        httpHandler.initialize(properties.getProperty("URL"));
    }

    private void initProperties() {
        properties = new Properties();

        File config = new File("config.properties");
        if (!config.exists()) {
            saveSettings("http://localhost:8080");
        }

        try (Reader reader = new FileReader("config.properties")) {
            properties.load(reader);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void updateFilePath(String path) {
        try {
            properties.put("path", path);
            properties.store(new FileWriter("config.properties"), "Updated file path");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getFilePath() {
        return (String) properties.get("path");
    }

    public void configureComponents() {
//        centerOnScreen();

        headerPanel.configureComponents(this);
        tablePanel.configureComponents(this);
    }

    public void configureServices() {
        headerPanel.configureServices(mspListing);
    }

    public void updateTableDisplay(List<FileBean> selectedFiles) {
        fileCount1.setText(String.valueOf(selectedFiles.size()));
        fileCount2.setText(String.valueOf(selectedFiles.size()));

        long totalSize = selectedFiles.stream()
                .map(FileBean::getSize)
                .mapToLong(Long::longValue)
                .sum();

        totalFileSize1.setText(byteCountToDisplaySize(totalSize));
        totalFileSize2.setText(byteCountToDisplaySize(totalSize));

        ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "Status");

        tablePanel.updateTableDisplay(selectedFiles);
    }

    public void triggerStatusCheck(Long headerID, LocalDateTime uploadStartTime) {
        processTimer.start();

        int recordCount = tablePanel.getSelectedFiles().size();

        StaleStatusMonitor monitor = new StaleStatusMonitor();

        SwingWorker<HeaderStatus, List<FileStatus>> statusWorker = new SwingWorker<HeaderStatus, List<FileStatus>>() {
            private boolean isStale(List<FileStatus> fileStatusList) {
                boolean retVal = false;
                Date now = new Date();

                if (fileStatusList.hashCode() == monitor.getChecksum()) {
                    Date lastUpdateTime = monitor.getUpdateTimestamp();
                    long timeDiff = now.getTime() - lastUpdateTime.getTime();

                    if (timeDiff > STALE_UPLOAD_TIMEOUT) {
                        retVal = true;
                    }
                } else {
                    monitor.setChecksum(fileStatusList.hashCode());
                    monitor.setUpdateTimestamp(now);

                    retVal = false;
                }

                return retVal;
            }

            private HeaderStatus getStatus(int runningFileCount) {
                HeaderStatus retVal = new HeaderStatus();
                retVal.setUploadStatus(INPROGRESS);

                if (runningFileCount == recordCount) {
                    retVal = httpHandler.getHeader(headerID);

                    if (equalsIgnoreCase(retVal.getNotificationSent(), "Y")) {
                        log.debug("Finished Processing {} files", runningFileCount);
                        retVal.setUploadStatus(SUCCESS);
                    }
                }

                return retVal;
            }

            @Override
            protected HeaderStatus doInBackground() throws Exception {
                log.debug("Status check triggered with headerID:{}", headerID);

                int runningFileCount = 0;
                HeaderStatus status = getStatus(runningFileCount);

                while (status.getUploadStatus() == INPROGRESS) {
                    List<FileStatus> fileStatusList = httpHandler.checkStatus(headerID);

                    if (isStale(fileStatusList)) {
                        log.debug("Upload is stale headerID:{}", headerID);

                        status.setUploadStatus(STALE);
                        break;
                    }

                    if (isNotEmpty(fileStatusList)) {
                        publish(fileStatusList);
                    }

                    Thread.yield();
                    Thread.sleep(ONE_SECOND);

                    runningFileCount = fileStatusList.size();
                    status = getStatus(runningFileCount);
                }

                return status;
            }

            @Override
            protected void process(List<List<FileStatus>> chunks) {
                tablePanel.updateRecordStatus(chunks.get(0));
            }

            @Override
            protected void done() {
                try {
                    HeaderStatus headerStatus = get();

                    processTimer.stop();

                    headerPanel.updateProcessDisplay(headerStatus, uploadStartTime);
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage(), e);
                }
            }
        };

        statusWorker.execute();
    }

    public void uploadData(String category, String mspShortName) {
        resetUploadStatus();

        List<FileBean> selectedFiles = tablePanel.getSelectedFiles();

        FileBucketFactory fileBucketFactory = new FileBucketFactory();
        FileBucket fileBucket = fileBucketFactory.createBucket(selectedFiles);

        uploadStartTime = now();

        uploadProgressBar.setValue(0);
        uploadProgressBar.setMinimum(0);
        uploadProgressBar.setMaximum(100);

        int totalProgress = selectedFiles.size() + 2;

        SwingWorker<String, UploadData> uploadWorker = new SwingWorker<String, UploadData>() {
            @Override
            protected String doInBackground() throws Exception {

                int counter = 0;

                long headerID = httpHandler.sendHeader(selectedFiles.size(), category, mspShortName);
                UploadData uploadData = new UploadData();
                uploadData.setUploadType(HEADER);
                uploadData.setHeaderID(headerID);
                uploadData.setUploadStartTime(now());
                publish(uploadData);

                int progressValue = (100 * ++counter) / totalProgress;
                setProgress(progressValue);

                while (fileBucket.hasMoreElements()) {
                    List<FileBean> fileList = fileBucket.getFiles();
                    httpHandler.sendFiles(headerID, fileList);

                    uploadData = new UploadData();
                    uploadData.setUploadType(UploadType.FILE);
                    uploadData.setFileList(fileList);
                    publish(uploadData);

                    counter = counter + fileList.size();
                    progressValue = (100 * counter) / totalProgress;
                    setProgress(progressValue);
                }

                String transactionID = httpHandler.sendTrailer(headerID);
                progressValue = (100 * ++counter) / totalProgress;
                setProgress(progressValue);

                return transactionID;
            }

            @Override
            protected void process(List<UploadData> chunks) {
                UploadData uploadData = chunks.get(0);

                if (uploadData.getUploadType() == HEADER) {
                    triggerStatusCheck(uploadData.getHeaderID(), uploadData.getUploadStartTime());
                } else if (uploadData.getUploadType() == FILE) {
                    tablePanel.updateInitialRecordStatus(uploadData.getFileList());
                }
            }

            @Override
            protected void done() {
                try {
                    String transactionID = get();

                    log.info("Done uploading files.");

                    headerPanel.updateTransactionID(transactionID);
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage(), e);
                    showErrorDialog(e, parseErrorMessage(e.getMessage()), WARNING_MESSAGE, MeterDataUploader.this);

                    uploadProgressBar.setValue(0);
                }

                // TODO: Display process upload progress bar

                headerPanel.readyToUploadToolbar();
            }
        };

        uploadWorker.addPropertyChangeListener(evt -> {
            if (equalsIgnoreCase(evt.getPropertyName(), "progress")) {
                uploadProgressBar.setValue((Integer) evt.getNewValue());
            }
        });

        uploadWorker.execute();
    }

    private void resetUploadStatus() {
        headerPanel.resetStatus();
        tablePanel.resetStatus();

        ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "Upload");
    }

    public void login(String username, String password) {
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {

                publish("Authenticating " + username);
                setProgress(10);

                httpHandler.login(username, password);
                setProgress(35);

                publish("Loading user credentials");
                List<String> userData = httpHandler.getUserType();
                setProgress(80);

                if (userData.size() > 1 &&
                        (!equalsIgnoreCase(userData.get(1), "PEMC") && !equalsIgnoreCase(userData.get(1), "MSP"))) {

                    return FALSE;
                }

                MeterDataUploader.this.username = username;
                fullName = userData.get(0);
                userType = userData.get(1);

                // TODO: Avoid redundant rest call
                if (equalsIgnoreCase(userType, "MSP")) {
                    participant = httpHandler.getParticipant();
                }

                publish("Loading MSP Listing");
                mspListing = httpHandler.getMSPListing();

                configureServices();
                setProgress(100);

                Thread.sleep(100);

                return TRUE;
            }

            @Override
            protected void process(List<String> fileBeans) {
                initializeProgressBar.setString(fileBeans.get(0));
            }

            @Override
            protected void done() {
                try {
                    Boolean retVal = get();

                    if (retVal == FALSE) {
                        showMessageDialog(MeterDataUploader.this, "Invalid Login", "Login Error",
                                ERROR_MESSAGE);

                        headerPanel.loggedOutToolbar();
                        ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "blank");
                    } else {
                        userFullName1.setText(fullName);
                        userFullName2.setText(fullName);
                        userFullName3.setText(fullName);

                        ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "LoggedIn");

                        headerPanel.loggedInToolbar();
                    }

                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage(), e);

                    showErrorDialog(e, parseErrorMessage(e.getMessage()), WARNING_MESSAGE, MeterDataUploader.this);

                    headerPanel.loggedOutToolbar();
                    ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "blank");
                }
            }
        };

        worker.addPropertyChangeListener(evt -> {
            if (equalsIgnoreCase(evt.getPropertyName(), "progress")) {
                initializeProgressBar.setValue((Integer) evt.getNewValue());
            }
        });

        ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "Initialize");
        headerPanel.disableAllToolbar();

        worker.execute();
    }

    public void logout() {
        log.debug("Logging out user:{}", username);

        username = null;
        userType = null;
        participant = null;

        tablePanel.clearSelectedFiles();

        ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "blank");
    }

    public void clearSelectedFiles() {
        ((CardLayout) statusBarPanel.getLayout()).show(statusBarPanel, "LoggedIn");

        // TODO: Clear status

        tablePanel.clearSelectedFiles();
    }

    public ParticipantName getParticipant() {
        return participant;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void saveSettings(String serverURL) {
        properties.put("URL", serverURL);

        try (Writer writer = new FileWriter("config.properties")) {
            properties.store(writer, "Saving updated server URL");

            httpHandler.initialize(serverURL);
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
        fileStatsPanel = new JPanel();
        lblTotalFileSize1 = new JLabel();
        totalFileSize1 = new JLabel();
        lblFileCount1 = new JLabel();
        fileCount1 = new JLabel();
        centerBlankPanel = new JPanel();
        userStatusPanel2 = new JPanel();
        lblUser2 = new JLabel();
        userFullName2 = new JLabel();
        uploadProgressPanel = new JPanel();
        fileStatsPanel1 = new JPanel();
        lblTotalFileSize2 = new JLabel();
        totalFileSize2 = new JLabel();
        lblFileCount2 = new JLabel();
        fileCount2 = new JLabel();
        centerUploadPanel = new JPanel();
        uploadProgressBar = new JProgressBar();
        userStatusPanel3 = new JPanel();
        lblUser3 = new JLabel();
        userFullName3 = new JLabel();
        initializeProgressPanel = new JPanel();
        initializeProgressBar = new JProgressBar();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Meter Quantity Uploader");
        setResizable(false);
        setSize(new Dimension(800, 505));
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
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

        fileStatsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        fileStatsPanel.setLayout(new GridBagLayout());

        lblTotalFileSize1.setText("Total Size:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        fileStatsPanel.add(lblTotalFileSize1, gridBagConstraints);

        totalFileSize1.setText("50 MB");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 15);
        fileStatsPanel.add(totalFileSize1, gridBagConstraints);

        lblFileCount1.setText("File Count:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        fileStatsPanel.add(lblFileCount1, gridBagConstraints);

        fileCount1.setText("100");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        fileStatsPanel.add(fileCount1, gridBagConstraints);

        statusPanel.add(fileStatsPanel, BorderLayout.WEST);

        centerBlankPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        statusPanel.add(centerBlankPanel, BorderLayout.CENTER);

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

        fileStatsPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        fileStatsPanel1.setLayout(new GridBagLayout());

        lblTotalFileSize2.setText("Total Size:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        fileStatsPanel1.add(lblTotalFileSize2, gridBagConstraints);

        totalFileSize2.setText("50 MB");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 15);
        fileStatsPanel1.add(totalFileSize2, gridBagConstraints);

        lblFileCount2.setText("File Count:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        fileStatsPanel1.add(lblFileCount2, gridBagConstraints);

        fileCount2.setText("100");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        fileStatsPanel1.add(fileCount2, gridBagConstraints);

        uploadProgressPanel.add(fileStatsPanel1, BorderLayout.WEST);

        centerUploadPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        centerUploadPanel.setLayout(new GridBagLayout());

        uploadProgressBar.setDoubleBuffered(true);
        uploadProgressBar.setMaximumSize(new Dimension(350, 20));
        uploadProgressBar.setMinimumSize(new Dimension(350, 20));
        uploadProgressBar.setPreferredSize(new Dimension(350, 20));
        uploadProgressBar.setStringPainted(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        centerUploadPanel.add(uploadProgressBar, gridBagConstraints);

        uploadProgressPanel.add(centerUploadPanel, BorderLayout.CENTER);

        userStatusPanel3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));

        lblUser3.setText("User:");
        userStatusPanel3.add(lblUser3);

        userFullName3.setText("Firstname A. Lastname");
        userFullName3.setMaximumSize(new Dimension(175, 16));
        userFullName3.setMinimumSize(new Dimension(175, 16));
        userFullName3.setPreferredSize(new Dimension(175, 16));
        userStatusPanel3.add(userFullName3);

        uploadProgressPanel.add(userStatusPanel3, BorderLayout.EAST);

        statusBarPanel.add(uploadProgressPanel, "Upload");

        initializeProgressPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3), new SoftBevelBorder(BevelBorder.LOWERED)));

        initializeProgressBar.setMaximumSize(new Dimension(350, 20));
        initializeProgressBar.setMinimumSize(new Dimension(350, 20));
        initializeProgressBar.setPreferredSize(new Dimension(350, 20));
        initializeProgressBar.setString("");
        initializeProgressBar.setStringPainted(true);
        initializeProgressPanel.add(initializeProgressBar);

        statusBarPanel.add(initializeProgressPanel, "Initialize");

        getContentPane().add(statusBarPanel, BorderLayout.SOUTH);

        setSize(new Dimension(1000, 505));
        setLocationRelativeTo(null);
    }//GEN-END:initComponents

    private void formWindowClosing(WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int action = showConfirmDialog(this,
                "Are you sure you want to exit Meter Quantity Uploader?",
                "Confirm Exit", OK_CANCEL_OPTION, INFORMATION_MESSAGE);
        if (action == OK_OPTION) {
            log.info("Closing down Meter Data Uploader");

            try {
                httpHandler.shutdown();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel blankPanel;
    private JPanel centerBlankPanel;
    private JPanel centerStatusPanel1;
    private JPanel centerUploadPanel;
    private JLabel fileCount1;
    private JLabel fileCount2;
    private JPanel fileStatsPanel;
    private JPanel fileStatsPanel1;
    private HeaderPanel headerPanel;
    private JProgressBar initializeProgressBar;
    private JPanel initializeProgressPanel;
    private JLabel lblFileCount1;
    private JLabel lblFileCount2;
    private JLabel lblTotalFileSize1;
    private JLabel lblTotalFileSize2;
    private JLabel lblUser1;
    private JLabel lblUser2;
    private JLabel lblUser3;
    private JPanel loggedinPanel;
    private JPanel statusBarPanel;
    private JPanel statusPanel;
    private TablePanel tablePanel;
    private JLabel totalFileSize1;
    private JLabel totalFileSize2;
    private JProgressBar uploadProgressBar;
    private JPanel uploadProgressPanel;
    private JLabel userFullName1;
    private JLabel userFullName2;
    private JLabel userFullName3;
    private JPanel userStatusPanel1;
    private JPanel userStatusPanel2;
    private JPanel userStatusPanel3;
    // End of variables declaration//GEN-END:variables

    private class ProcessTimerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Duration diff = Duration.between(uploadStartTime, now());

            LocalTime localTime = LocalTime.MIDNIGHT.plus(diff);
            String duration = DateTimeFormatter.ofPattern("mm:ss").format(localTime);
            headerPanel.updateProcessDuration(duration);
        }
    }

}

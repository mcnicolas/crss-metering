package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
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
        tablePanel.updateTableDisplay(selectedFiles);
    }

    // TODO:
    // 1. Display progress bar
    public void uploadData(String category, int mspID) {
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
                if (token != null) {
                    // TODO: Add error handling. When an error is encountered throw an exception.
                    RestUtil.sendHeader(transactionID, username, selectedFiles.size(), category, mspID, token);
                    publish("Sending header record");
                    setProgress(++counter);
                }

                for (FileBean selectedFile : selectedFiles) {
                    RestUtil.sendFile(transactionID, selectedFile, category, token);
                    publish(selectedFile.getPath().getFileName().toString());
                    setProgress(++counter);

                    log.debug("Uploading file:{}", selectedFile.getPath().getFileName().toString());
                }

                RestUtil.sendTrailer(transactionID, token);
                publish("Sending trailer record");
                setProgress(++counter);

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                uploadFilename.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
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
        try {
            token = RestUtil.login(username, password);

            if (token == null) {
                JOptionPane.showMessageDialog(this, "Invalid login", "Error", ERROR_MESSAGE);
            } else {
                log.debug("Logged in with token: {}", token);

                this.username = username;

                // TODO: Validate user
                // 1. User should be a valid user in the system (non-expired, non-locked, etc)
                // 2. User should be a PEMC User or a Trading Participant with an MSP registration category
                // 3. If PEMC User, it should belong to the metering department
                List<String> userData = RestUtil.getUserType(token);

                fullName = userData.get(0);
                jLabel14.setText(fullName);
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

            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", ERROR_MESSAGE);
        }
    }

    public void logout() {
        token = null;
        username = null;
        userType = null;
        participant = null;

        tablePanel.clearSelectedFiles();

        // TODO: Reset status bar
    }

    public void clearSelectedFiles() {
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
        leftStatusPanel1 = new JPanel();
        jLabel13 = new JLabel();
        jLabel14 = new JLabel();
        centerStatusPanel1 = new JPanel();
        statusPanel = new JPanel();
        leftStatusPanel = new JPanel();
        jLabel1 = new JLabel();
        jLabel4 = new JLabel();
        centerStatusPanel = new JPanel();
        jLabel12 = new JLabel();
        rightStatusPanel = new JPanel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        uploadProgressPanel = new JPanel();
        leftUploadPanel = new JPanel();
        jLabel7 = new JLabel();
        jLabel6 = new JLabel();
        centerUploadPanel = new JPanel();
        uploadProgressBar = new JProgressBar();
        lblUploading = new JLabel();
        uploadFilename = new JLabel();
        rightUploadPanel = new JPanel();
        jLabel8 = new JLabel();
        jLabel9 = new JLabel();
        jLabel10 = new JLabel();
        jLabel11 = new JLabel();
        initializeProgressPanel = new JPanel();
        lblUploadStatus = new JLabel();
        initializeProgressBar = new JProgressBar();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Meter Quantity Uploader");
        setMinimumSize(new Dimension(700, 550));
        setPreferredSize(new Dimension(700, 550));
        setResizable(false);
        getContentPane().add(headerPanel, BorderLayout.NORTH);

        tablePanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
        getContentPane().add(tablePanel, BorderLayout.CENTER);

        statusBarPanel.setLayout(new CardLayout());

        blankPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 3), new SoftBevelBorder(BevelBorder.LOWERED)));
        statusBarPanel.add(blankPanel, "blank");

        loggedinPanel.setLayout(new BorderLayout());

        leftStatusPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));

        jLabel13.setText("Logged in user:");
        leftStatusPanel1.add(jLabel13);

        jLabel14.setText("Chris A. Lim");
        leftStatusPanel1.add(jLabel14);

        loggedinPanel.add(leftStatusPanel1, BorderLayout.WEST);

        centerStatusPanel1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        loggedinPanel.add(centerStatusPanel1, BorderLayout.CENTER);

        statusBarPanel.add(loggedinPanel, "LoggedIn");

        statusPanel.setLayout(new BorderLayout());

        leftStatusPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));

        jLabel1.setText("Total Size:");
        leftStatusPanel.add(jLabel1);

        jLabel4.setText("10MB");
        leftStatusPanel.add(jLabel4);

        statusPanel.add(leftStatusPanel, BorderLayout.WEST);

        centerStatusPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));

        jLabel12.setText("jLabel12");
        centerStatusPanel.add(jLabel12);

        statusPanel.add(centerStatusPanel, BorderLayout.CENTER);

        rightStatusPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 3), new SoftBevelBorder(BevelBorder.LOWERED)));

        jLabel2.setText("File Count:");
        rightStatusPanel.add(jLabel2);

        jLabel3.setText("100");
        rightStatusPanel.add(jLabel3);

        statusPanel.add(rightStatusPanel, BorderLayout.EAST);

        statusBarPanel.add(statusPanel, "Status");

        uploadProgressPanel.setLayout(new BorderLayout());

        leftUploadPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 3, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));

        jLabel7.setText("Time:");
        leftUploadPanel.add(jLabel7);

        jLabel6.setText("5:47");
        leftUploadPanel.add(jLabel6);

        uploadProgressPanel.add(leftUploadPanel, BorderLayout.WEST);

        centerUploadPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 1), new SoftBevelBorder(BevelBorder.LOWERED)));
        centerUploadPanel.setLayout(new GridBagLayout());

        uploadProgressBar.setDoubleBuffered(true);
        uploadProgressBar.setMinimumSize(new Dimension(300, 20));
        uploadProgressBar.setPreferredSize(new Dimension(300, 20));
        uploadProgressBar.setStringPainted(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        centerUploadPanel.add(uploadProgressBar, gridBagConstraints);

        lblUploading.setText("Uploading:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        centerUploadPanel.add(lblUploading, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        centerUploadPanel.add(uploadFilename, gridBagConstraints);

        uploadProgressPanel.add(centerUploadPanel, BorderLayout.CENTER);

        rightUploadPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 3, 3), new SoftBevelBorder(BevelBorder.LOWERED)));

        jLabel8.setText("Upload Speed:");
        rightUploadPanel.add(jLabel8);

        jLabel9.setText("750 KB/s");
        rightUploadPanel.add(jLabel9);

        jLabel10.setText("Time Remaining:");
        rightUploadPanel.add(jLabel10);

        jLabel11.setText("5:34");
        rightUploadPanel.add(jLabel11);

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
    private JPanel centerStatusPanel;
    private JPanel centerStatusPanel1;
    private JPanel centerUploadPanel;
    private HeaderPanel headerPanel;
    private JProgressBar initializeProgressBar;
    private JPanel initializeProgressPanel;
    private JLabel jLabel1;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel14;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JLabel lblUploadStatus;
    private JLabel lblUploading;
    private JPanel leftStatusPanel;
    private JPanel leftStatusPanel1;
    private JPanel leftUploadPanel;
    private JPanel loggedinPanel;
    private JPanel rightStatusPanel;
    private JPanel rightUploadPanel;
    private JPanel statusBarPanel;
    private JPanel statusPanel;
    private TablePanel tablePanel;
    private JLabel uploadFilename;
    private JProgressBar uploadProgressBar;
    private JPanel uploadProgressPanel;
    // End of variables declaration//GEN-END:variables

}

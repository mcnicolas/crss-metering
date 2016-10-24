package com.pemc.crss.meter.upload;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class ApplicationLoader {

    public static void main(String[] args) {
        // TODO:
        // 1. Show splash screen
        // 2. Display
        // 3. Initialize components

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MeterDataUploader.class.getName()).log(SEVERE, null, ex);
        }

        MeterDataUploader dataUploader = new MeterDataUploader();

        dataUploader.configureComponents();
        dataUploader.configureServices();
        dataUploader.setVisible(true);

        LoginDialog loginDialog = new LoginDialog(dataUploader, true);
        loginDialog.setVisible(true);
    }

}

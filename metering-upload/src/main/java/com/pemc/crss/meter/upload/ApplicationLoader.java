package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Logger;

import static com.pemc.crss.meter.upload.LoginDialog.RET_OK;
import static java.util.logging.Level.SEVERE;

// TODO: Convert to spring boot
@Slf4j
public class ApplicationLoader {

    public static void main(String[] args) {
        HttpHandler httpHandler = new HttpHandler();

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

        MeterDataUploader dataUploader = new MeterDataUploader(httpHandler);
        dataUploader.configureComponents();

        log.debug("Ready to render UI");

        dataUploader.setVisible(true);

        LoginDialog loginDialog = new LoginDialog(dataUploader, true);
        loginDialog.setVisible(true);

        if (loginDialog.getReturnStatus() == RET_OK) {
            dataUploader.login(loginDialog.getUsername(), loginDialog.getPassword());

            dataUploader.configureServices();
        }
    }

}

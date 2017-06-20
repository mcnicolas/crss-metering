package com.pemc.crss.meter.upload;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javax.swing.SwingUtilities.invokeLater;

public class ErrorDialog extends JOptionPane {

    public ErrorDialog(Object[] message, int messageType, int defaultOption) {
        super(message, messageType, defaultOption, null);
    }

    public static void showErrorDialog(Throwable error, String textMessage, int messageType, Component parentComponent) {

        if (textMessage == null) {
            textMessage = "";
        }

        JDialog dialog;
        String detailedMessage = "";

        if (error != null) {
            detailedMessage += "<h2>Message</h2>";
            detailedMessage += error.getMessage();
            detailedMessage += "<h2>Stack trace</h2>";
            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));
            detailedMessage += sw.toString()
                    .replaceAll("(at |Caused by:)", "<strong>$1</strong> ")
                    .replaceAll("(\\([A-Za-z]+\\.java:[0-9])+\\)", "<strong>$1</strong>");
        }

        JEditorPane messageArea = new JEditorPane("text/html", detailedMessage);
        messageArea.setEditable(false);
        messageArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane stackTracePane = new JScrollPane(messageArea);
        stackTracePane.setVisible(false);

        Box messageBox = Box.createHorizontalBox();
        messageBox.add(new JLabel(textMessage.startsWith("<html>") ?
                textMessage : "<html><body>" +
                textMessage.replaceAll("\n", "<br>") + "</body></html>"));
        messageBox.add(Box.createHorizontalStrut(5));
        final JToggleButton toggleButton = new JToggleButton();
        toggleButton.setToolTipText("Show the error stack trace");
        toggleButton.setMargin(new Insets(2, 4, 2, 2));
//        messageBox.add(toggleButton);
        messageBox.add(Box.createHorizontalGlue());

        Object[] message = {messageBox, stackTracePane};

        ErrorDialog pane = new ErrorDialog(message, messageType, DEFAULT_OPTION);
        dialog = pane.createDialog(parentComponent, "MQ Uploader Error");
        dialog.setResizable(true);

        toggleButton.setAction(new AbstractAction("Details") {
            public void actionPerformed(ActionEvent e) {
                invokeLater(() -> {
                    stackTracePane.setVisible(toggleButton.isSelected());
                    if (toggleButton.isSelected()) {
                        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                        dialog.setBounds((screenSize.width - 700) / 2, (screenSize.height - 500) / 2, 700, 500);
                        messageArea.setCaretPosition(0);
                    } else {
                        dialog.pack();
                    }
                });
            }
        });

        dialog.pack();
        dialog.setVisible(true);
    }

}

package com.pemc.crss.meter.upload;

import com.pemc.crss.meter.upload.http.FileStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.text.WordUtils;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.List;

import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.RIGHT;

@Slf4j
public class TablePanel extends JPanel {

    private MeterDataUploader parent;

    public TablePanel() {
        initComponents();
    }

    public void configureComponents(MeterDataUploader parent) {
        this.parent = parent;

        DefaultTableCellRenderer rightAlignRenderer = new DefaultTableCellRenderer();
        rightAlignRenderer.setHorizontalAlignment(RIGHT);

        DefaultTableCellRenderer centerAlignRenderer = new DefaultTableCellRenderer();
        centerAlignRenderer.setHorizontalAlignment(CENTER);

        TableColumnModel columnModel = fileTable.getColumnModel();
        TableColumn column = columnModel.getColumn(0);
        column.setCellRenderer(rightAlignRenderer);
        column.setHeaderValue("");
        column.setPreferredWidth(50);
        column.setMinWidth(50);
        column.setResizable(false);

        column = columnModel.getColumn(1);
        column.setHeaderValue("Filename");
        column.setPreferredWidth(350);
        column.setMinWidth(350);
        column.setResizable(true);

        column = columnModel.getColumn(2);
        column.setCellRenderer(rightAlignRenderer);
        column.setHeaderValue("Size");
        column.setPreferredWidth(70);
        column.setMinWidth(70);
        column.setResizable(false);

        column = columnModel.getColumn(3);
        column.setHeaderValue("Status");
        column.setPreferredWidth(150);
        column.setMinWidth(150);
        column.setResizable(false);

        column = columnModel.getColumn(4);
        column.setHeaderValue("Error Details");
        column.setPreferredWidth(400);
        column.setMinWidth(400);
        column.setResizable(true);
    }

    public void updateTableDisplay(List<FileBean> selectedFiles) {
        SwingUtilities.invokeLater(() -> {
            FileTableModel tableModel = (FileTableModel) fileTable.getModel();
            tableModel.setFileList(selectedFiles);

            int width = 0;
            for (int i = 0; i < fileTable.getRowCount(); i++) {
                TableCellRenderer renderer = fileTable.getCellRenderer(i, 4);
                Component component = fileTable.prepareRenderer(renderer, i, 4);
                width = Math.max(component.getPreferredSize().width + 10, width);
            }

            TableColumnModel columnModel = fileTable.getColumnModel();
            columnModel.getColumn(4).setPreferredWidth(width);
        });
    }

    public List<FileBean> getSelectedFiles() {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();

        return tableModel.getFileList();
    }

    public void clearSelectedFiles() {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();
        tableModel.clearFileList();
    }

    public void resetStatus() {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();

        tableModel.resetUploadedStatus();

        Rectangle rectangle = fileTable.getCellRect(0, 0, true);
        fileTable.scrollRectToVisible(rectangle);
    }

    public void updateRecordStatus(List<FileStatus> fileList) {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();

        for (FileStatus fileStatus : fileList) {
            long fileID = fileStatus.getFileID();
            String fileName = fileStatus.getFileName();
            String status = WordUtils.capitalizeFully(fileStatus.getStatus());
            String errorDetail = fileStatus.getErrorDetails();

            tableModel.updateStatus(fileID, fileName, status, errorDetail);
        }
    }

    public void updateInitialRecordStatus(List<FileBean> fileList) {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();

        for (FileBean fileBean : fileList) {
            int key = fileBean.getKey();
            tableModel.updateStatus(key, "Queued For Processing");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {//GEN-BEGIN:initComponents

        scrollPane = new JScrollPane();
        fileTable = new JTable();

        setLayout(new BorderLayout());

        fileTable.setModel(new FileTableModel());
        fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        fileTable.setIntercellSpacing(new Dimension(5, 1));
        fileTable.setRowSelectionAllowed(false);
        scrollPane.setViewportView(fileTable);

        add(scrollPane, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTable fileTable;
    private JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}

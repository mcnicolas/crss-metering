package com.pemc.crss.meter.upload;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.util.List;

public class TablePanel extends JPanel {

    private MeterDataUploader parent;

    public TablePanel() {
        initComponents();
    }

    public void configureComponents(MeterDataUploader parent) {
        this.parent = parent;

        TableColumnModel columnModel = fileTable.getColumnModel();
        TableColumn column = columnModel.getColumn( 0 );
        column.setHeaderValue("");
        column.setPreferredWidth(50);
        column.setMinWidth(50);
        column.setResizable(false);

        column = columnModel.getColumn(1);
        column.setHeaderValue("Filename");
        column.setPreferredWidth(350);
        column.setMinWidth(350);
        column.setResizable(false);

        column = columnModel.getColumn(2);
        column.setHeaderValue("Timestamp");
        column.setPreferredWidth(150);
        column.setMinWidth(150);
        column.setResizable(true);

        column = columnModel.getColumn(3);
        column.setHeaderValue("Size");
        column.setPreferredWidth(100);
        column.setMinWidth(100);
        column.setResizable(true);

        column = columnModel.getColumn(4);
        column.setHeaderValue("Checksum");
        column.setPreferredWidth(300);
        column.setMinWidth(250);
        column.setResizable(true);
    }

    public void updateTableDisplay(List<FileBean> selectedFiles) {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();

        tableModel.setFileList(selectedFiles);
    }

    public List<FileBean> getSelectedFiles() {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();

        return tableModel.getFileList();
    }

    public void clearSelectedFiles() {
        FileTableModel tableModel = (FileTableModel) fileTable.getModel();
        tableModel.clearFileList();
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
        scrollPane.setViewportView(fileTable);

        add(scrollPane, BorderLayout.CENTER);
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTable fileTable;
    private JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

}

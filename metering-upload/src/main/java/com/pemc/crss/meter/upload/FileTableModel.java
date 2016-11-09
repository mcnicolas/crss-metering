package com.pemc.crss.meter.upload;

import javax.swing.table.AbstractTableModel;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileTableModel extends AbstractTableModel {

    private List<FileBean> fileList;

    @Override
    public int getRowCount() {
        if (fileList == null) {
            return 0;
        } else {
            return fileList.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileBean bean = fileList.get(rowIndex);

        String retVal;
        switch (columnIndex) {
            case 0:
                return String.valueOf(rowIndex + 1);
            case 1:
                Path path = bean.getPath();
                return path.getFileName();
            case 2:
                // TODO: optimize for performance
                FileTime lastModifiedDate = bean.getLastModified();
                Date x = new Date(lastModifiedDate.toMillis());
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

                return dateFormat.format(x);
            case 3:
                return org.apache.commons.io.FileUtils.byteCountToDisplaySize(bean.getSize());
            case 4:
                return bean.getChecksum();
            default:
                retVal = "";
        }

        return retVal;
    }

    public void setFileList(List<FileBean> fileList) {
        // TODO: Dirty code. Need to optimize further.
        this.fileList = fileList;

        fireTableDataChanged();
    }

    public void clearFileList() {
        this.fileList = new ArrayList<>();

        fireTableDataChanged();
    }

    public List<FileBean> getFileList() {
        return this.fileList;
    }

}

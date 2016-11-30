package com.pemc.crss.meter.upload;

import javax.swing.table.AbstractTableModel;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

public class FileTableModel extends AbstractTableModel {

    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private List<FileBean> fileList;
    private Map<Integer, FileBean> map;

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
        return 6;
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
                return bean.getKey();
            case 1:
                Path path = bean.getPath();
                return path.getFileName();
            case 2:
                FileTime lastModifiedDate = bean.getLastModified();
                return dateFormat.format(new Date(lastModifiedDate.toMillis()));
            case 3:
                return byteCountToDisplaySize(bean.getSize());
            case 4:
                return bean.getChecksum();
            case 5:
                return bean.getStatus();
            default:
                retVal = "";
        }

        return retVal;
    }

    public void setFileList(List<FileBean> fileBeans) {
        this.fileList = new ArrayList<>();
        this.map = new HashMap<>();

        for (int i = 0; i < fileBeans.size(); i++) {
            FileBean bean = fileBeans.get(i);

            int key = i + 1;
            bean.setKey(key);

            fileList.add(bean);
            map.put(key, bean);
        }

        fireTableDataChanged();
    }

    public void clearFileList() {
        this.fileList = new ArrayList<>();

        fireTableDataChanged();
    }

    public List<FileBean> getFileList() {
        return this.fileList;
    }

    public void updateUploadedStatus(int key) {
        FileBean bean = map.get(key);
        bean.setStatus("Uploaded");

        fireTableRowsUpdated(key - 1, key + 1);
    }

    public void resetUploadedStatus() {
        for (FileBean fileBean : fileList) {
            fileBean.setStatus("");
        }

        fireTableDataChanged();
    }

}

package com.pemc.crss.meter.upload;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

@Slf4j
public class FileTableModel extends AbstractTableModel {

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
        return 5;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Object value = getValueAt(0, columnIndex);
        if (value != null) {
            return value.getClass();
        } else {
            return String.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FileBean bean = fileList.get(rowIndex);

        String retVal;
        switch (columnIndex) {
            case 0:
                return bean.getKey();
            case 1:
                return bean.getPath().getFileName();
            case 2:
                return byteCountToDisplaySize(bean.getSize());
            case 3:
                return bean.getStatus();
            case 4:
                return bean.getErrorDetails();
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

    public void updateStatus(int key, String status) {
        FileBean bean = map.get(key);
        bean.setStatus(status);
        bean.setErrorDetails("");

        fireTableRowsUpdated(key - 1, key + 1);
    }

    public void updateStatus(long fileID, String fileName, String status, String errorDetail) {
        FileBean fileBean = findByFileID(fileID);

        if (fileBean != null) {
            fileBean.setStatus(status);
            fileBean.setErrorDetails(errorDetail);
        } else {
            fileBean = findByFileName(fileName);
            fileBean.setFileID(fileID);
            fileBean.setStatus(status);
            fileBean.setErrorDetails(errorDetail);
        }

        fireTableDataChanged();
    }

    private FileBean findByFileName(String fileName) {
        FileBean retVal = null;

        for (FileBean fileBean : fileList) {
            if (StringUtils.equalsIgnoreCase(fileBean.getPath().getFileName().toString(), fileName)) {
                retVal = fileBean;
                break;
            }
        }

        return retVal;
    }

    private FileBean findByFileID(long fileID) {
        FileBean retVal = null;

        for (int i = 0; i < fileList.size(); i++) {
            FileBean bean = fileList.get(i);

            if (bean.getFileID() == fileID) {
                retVal = bean;
                break;
            }
        }

        return retVal;
    }

    public void resetUploadedStatus() {
        for (FileBean fileBean : fileList) {
            fileBean.setStatus("");
        }

        fireTableDataChanged();
    }

}

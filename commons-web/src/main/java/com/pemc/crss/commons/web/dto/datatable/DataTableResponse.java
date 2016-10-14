package com.pemc.crss.commons.web.dto.datatable;

import java.util.List;

public class DataTableResponse<T> implements java.io.Serializable {

    private List<T> data;
    private int draw;
    private Long recordsTotal;
    private Long recordsFiltered;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public Long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public Long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public DataTableResponse<T> withData(List<T> data) {
        this.data = data;
        return this;
    }

    public DataTableResponse<T> withDraw(int draw) {
        this.draw = draw;
        return this;
    }

    public DataTableResponse<T> withRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
        return this;
    }

    public DataTableResponse<T> withRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
        return this;
    }

}

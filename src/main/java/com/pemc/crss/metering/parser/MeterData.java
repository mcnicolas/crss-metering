package com.pemc.crss.metering.parser;

import java.util.ArrayList;
import java.util.List;

public class MeterData {

    private List<Header> headers = new ArrayList<>();
    private TrailerRecord trailerRecord;
    private String error;

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public TrailerRecord getTrailerRecord() {
        return trailerRecord;
    }

    public void setTrailerRecord(TrailerRecord trailerRecord) {
        this.trailerRecord = trailerRecord;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    private void addMeterHeader(Header header) {
        headers.add(header);
    }

}

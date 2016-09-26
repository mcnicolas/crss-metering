package com.pemc.crss.metering.parser;

import java.util.ArrayList;
import java.util.List;

public class MeterData {

    private List<MeterHeader> meterHeaders = new ArrayList<>();
    private TrailerRecord trailerRecord;
    private String error;

    public List<MeterHeader> getMeterHeaders() {
        return meterHeaders;
    }

    public void setMeterHeaders(List<MeterHeader> meterHeaders) {
        this.meterHeaders = meterHeaders;
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

    private void addMeterHeader(MeterHeader meterHeader) {
        meterHeaders.add(meterHeader);
    }

}

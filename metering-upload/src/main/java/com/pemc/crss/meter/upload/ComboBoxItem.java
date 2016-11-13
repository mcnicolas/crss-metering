package com.pemc.crss.meter.upload;

import java.io.Serializable;

public class ComboBoxItem implements Serializable {

    private String value;
    private String display;

    public ComboBoxItem(String value, String display) {
        this.value = value;
        this.display = display;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }

}

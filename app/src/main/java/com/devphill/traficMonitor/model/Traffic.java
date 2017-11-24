package com.devphill.traficMonitor.model;

public class Traffic{

    private float mobileData;
    private float wiFiData;
    private float allData;

    public float getMobileData() {
        return mobileData;
    }

    public void setMobileData(float mobileData) {
        this.mobileData = mobileData;
    }

    public float getWiFiData() {
        return wiFiData;
    }

    public void setWiFiData(float wiFiData) {
        this.wiFiData = wiFiData;
    }

    public float getAllData() {
        return allData;
    }

    public void setAllData(float allData) {
        this.allData = allData;
    }
}
package com.devphill.traficMonitor.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class Package {

    private String name;
    private String version;
    private String packageName;
    private Drawable icon;
    private long mobileTx;
    private long mobileRx;
    private long wiFiTx;
    private long wiFiRx;

    private float mobileData;
    private float wiFiData;

    public boolean isUseTraffic(){

        if(mobileData > 0 || wiFiData > 0){

            return true;
        }
        else{

            return false;
        }

    }

    public void setMobileData(float mobileData) {
        this.mobileData = mobileData;
    }

    public void setWiFiData(float wiFiData) {
        this.wiFiData = wiFiData;
    }

    public float getMobileData(){

        return mobileData;
    }

    public float getWiFiData(){
        return wiFiData;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public long getMobileTx() {
        return mobileTx;
    }

    public void setMobileTx(long mobileTx) {
        this.mobileTx = mobileTx;
    }

    public long getMobileRx() {
        return mobileRx;
    }

    public void setMobileRx(long mobileRx) {
        this.mobileRx = mobileRx;
    }

    public long getWiFiTx() {
        return wiFiTx;
    }

    public void setWiFiTx(long wiFiTx) {
        this.wiFiTx = wiFiTx;
    }

    public long getWiFiRx() {
        return wiFiRx;
    }

    public void setWiFiRx(long wiFiRx) {
        this.wiFiRx = wiFiRx;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

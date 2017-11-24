package com.devphill.traficMonitor.ui.fragments.test_speed.helper;

/**
 * Created by Valera on 22.11.2017.
 */

public class DownloadUpload {

    private float percent;

    private float speed;

    private int status;

    public DownloadUpload(float percent, float speed,int status) {
        this.percent = percent;
        this.speed = speed;
        this.status = status;
    }

    public float getSpeed() {
        return speed;
    }

    public float getPercent() {
        return percent;
    }
    public int getStatus() {
        return status;
    }

}

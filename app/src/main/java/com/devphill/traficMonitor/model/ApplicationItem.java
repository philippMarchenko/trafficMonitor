package com.devphill.traficMonitor.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.util.Log;

import com.devphill.traficMonitor.service.TrafficService;


public class ApplicationItem {
    private long tx = 0;
    private long rx = 0;

    private long wifi_tx = 0;
    private long wifi_rx = 0;

    private long mobil_tx = 0;
    private long mobil_rx = 0;



    private float mobil_data = 0;

    private long current_tx = 0;
    private long current_rx = 0;

    private transient ApplicationInfo app;

    private boolean isMobil = false;
    private boolean isWiFi = false;

    static Context mContex;

    public ApplicationItem(ApplicationInfo _app) {
        app = _app;
        update();
    }

    public ApplicationItem() {

    }

    public void update() {
        long delta_tx = TrafficStats.getUidTxBytes(app.uid) - tx;
        long delta_rx = TrafficStats.getUidRxBytes(app.uid) - rx;

        tx = TrafficStats.getUidTxBytes(app.uid);
        rx = TrafficStats.getUidRxBytes(app.uid);

        current_tx = current_tx + delta_tx;
        current_rx = current_rx + delta_rx;

        if(isMobil == true) {
            mobil_tx = mobil_tx + delta_tx;
            mobil_rx = mobil_rx + delta_rx;
        } else if(isWiFi) {
            wifi_tx = wifi_tx + delta_tx;
            wifi_rx = wifi_rx + delta_rx;
        }

        String label = getApplicationLabel(mContex.getPackageManager());

    }

    public static ApplicationItem create(ApplicationInfo _app,Context context){
        long _tx = TrafficStats.getUidTxBytes(_app.uid);
        long _rx = TrafficStats.getUidRxBytes(_app.uid);
        mContex = context;

        ApplicationItem applicationItem = new ApplicationItem(_app);

        if((_tx + _rx) > 0) {
            PackageManager pm = mContex.getPackageManager();

            Log.i("appTrafficLogs","Create app item. AppLabel = " + pm.getApplicationLabel(_app).toString());
            Log.i("appTrafficLogs","_tx + _rx = " + (_tx + _rx));
            return applicationItem;
        }
        else
            return null;

    }
    public int getRebootAction() {

        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
        return mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION, 0);
    }
    public int getTotalUsageKb() {
        return Math.round((tx + rx)/ 1024);
    }
    public float getMobileMb() {

        float traffic = (float) (mobil_tx + mobil_rx) / ((float)1024*(float)1024);
        return Math.round(traffic * (float) 10.0) / (float) 10.0;
    }
    public float getMobileUsageMb() {

        float traffic;

        if(getRebootAction() == 1){
            traffic = (float) (mobil_tx + mobil_rx) / ((float)1024*(float)1024) + getTrafficAppsForReboot();
            return Math.round(traffic * (float) 10.0) / (float) 10.0;
        }
        else{
            traffic = ((float) (mobil_tx + mobil_rx) / ((float)1024*(float)1024));
            //traffic = traffic - getTrafficApps();
            traffic = Math.round(traffic * (float) 10.0) / (float) 10.0;

            if(traffic >= 0)
                return traffic;
            else return  0;
        }
    }
    public float getWiFiUsageMb() {

        float traffic;

        if(getRebootAction() == 1){
            traffic = (float) (wifi_rx + wifi_tx) / ((float)1024*(float)1024) + getTrafficAppsForRebootWiFi();
            return Math.round(traffic * (float) 10.0) / (float) 10.0;
        }
        else{
            traffic = ((float) ((float) (wifi_rx + wifi_tx)) / ((float)1024*(float)1024));
            // traffic = traffic - getTrafficAppsWiFi();

            traffic = Math.round(traffic * (float) 10.0) / (float) 10.0;

            if(traffic >= 0)
                return traffic;
            else return  0;
        }

    }
    public float getWiFiMb() {
        float traffic = ((float) (wifi_rx + wifi_tx)) / ((float)1024*(float)1024);
        return Math.round(traffic * (float) 10.0) / (float) 10.0;
    }
    public float getTrafficApps(){        //достаем траффик приложения по окончанию учетного периода
        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);
        return mySharedPreferences.getFloat(mContex.getPackageManager().getApplicationLabel(app).toString(),0);
    }   //??
    public float getTrafficAppsWiFi(){        //достаем траффик приложения по окончанию учетного периода
        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_WIFI, Context.MODE_PRIVATE);
        return mySharedPreferences.getFloat(mContex.getPackageManager().getApplicationLabel(app).toString(),0);
    }  //??
    public float getTrafficAppsForReboot(){ //достаем траффик приложения если была перезагрузка
        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT, Context.MODE_PRIVATE);
        return mySharedPreferences.getFloat(mContex.getPackageManager().getApplicationLabel(app).toString(),0);

    }
    public float getTrafficAppsForRebootWiFi(){ //достаем траффик приложения если была перезагрузка
        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT_WIFI, Context.MODE_PRIVATE);

        return mySharedPreferences.getFloat(mContex.getPackageManager().getApplicationLabel(app).toString(),0);

    }
    public String getApplicationLabel(PackageManager _packageManager) {
        return _packageManager.getApplicationLabel(app).toString();
    }
    public void setMobil_data(float mobil_data) {
        this.mobil_data = mobil_data;
    }
    public Drawable getIcon(PackageManager _packageManager) {
        return _packageManager.getApplicationIcon(app);
    }
    public void setMobilTraffic(boolean _isMobil) {
        isMobil = _isMobil;
    }
    public void setWiFiTraffic(boolean _iswifi) {
        isWiFi = _iswifi;
    }


}
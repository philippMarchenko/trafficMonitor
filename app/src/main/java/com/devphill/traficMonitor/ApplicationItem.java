package com.devphill.traficMonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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
        } else {
            wifi_tx = wifi_tx + delta_tx;
            wifi_rx = wifi_rx + delta_rx;
        }

        String label = getApplicationLabel(mContex.getPackageManager());
        /*Log.i("appTrafficLogs", "delta_tx " + delta_tx + " delta_rx " + delta_rx);
        Log.i("appTrafficLogs", "tx " + tx + " rx " + rx);
        Log.i("appTrafficLogs", "labelApp " + label);
        Log.i("appTrafficLogs", "mobil_rx " + mobil_rx + " mobil_tx " + mobil_tx);
        Log.i("appTrafficLogs", "wifi_rx " + wifi_rx + " wifi_tx " + wifi_tx);*/

    }

    public static ApplicationItem create(ApplicationInfo _app,Context context){
        long _tx = TrafficStats.getUidTxBytes(_app.uid);
        long _rx = TrafficStats.getUidRxBytes(_app.uid);
        mContex = context;

        if((_tx + _rx) > 0) return new ApplicationItem(_app);
        return null;
    }
    public int getRebootAction() {

        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
      // Log.i("appTrafficLogs", "getRebootAction " + mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION, 0));
        return mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION, 0);
    }
/*    public int getUsageKb() {
       *//* Log.i("appTrafficLogs", "getUsageKb");

        Log.i("appTrafficLogs","Name app = " + mContex.getPackageManager().getApplicationLabel(app).toString());
        Log.i("appTrafficLogs","Tx = " + tx);
        Log.i("appTrafficLogs","Rx = " +  rx);

        Log.i("appTrafficLogs","tx + rx /1024" +  ((tx + rx))/1024);*//*
        if(getRebootAction() == 1){
           *//* float traffic = ((float) (tx + rx) + (float)getTrafficAppsForReboot())/ ((float)1024*(float)1024);
            Log.i("appTrafficLogs","getRebootAction() = 1 " + getTrafficAppsForReboot());
            return Math.round(traffic * (float) 10.0) / (float) 10.0;*//*
            return Math.round((tx + rx)/ 1024)+ getTrafficAppsForReboot();
        }
        else{
          *//*  float traffic = ((float) (tx + rx) - (float)getTrafficAppsForReboot())/ ((float)1024*(float)1024);
            Log.i("appTrafficLogs","getRebootAction() = 0", "" + getTrafficAppsForReboot());
            return Math.round(traffic * (float) 10.0) / (float) 10.0;*//*
            return Math.round((tx + rx)/ 1024)- getTrafficApps();
        }
    }*/
/*    public float getUsageKbInt() {
        if(getRebootAction() == 1){
             return Math.round((tx + rx)/ 1024)+ getTrafficAppsForReboot();
        }
        else{
             return Math.round((tx + rx)/ 1024)- getTrafficApps();
        }
    }*/
    public int getTotalUsageKb() {
        return Math.round((tx + rx)/ 1024);
    }
    public float getMobileMb() {

        float traffic = (float) (mobil_tx + mobil_rx) / ((float)1024*(float)1024);
        return Math.round(traffic * (float) 10.0) / (float) 10.0;
    }
    public float getMobileUsageKb() {

        float traffic;

        if(getRebootAction() == 1){
            traffic = (float) (mobil_tx + mobil_rx) / ((float)1024*(float)1024) + getTrafficAppsForReboot();
            //Log.i("appTrafficLogs","getRebootAction() = 1 " + getTrafficAppsForReboot());
            return Math.round(traffic * (float) 10.0) / (float) 10.0;
        }
        else{
            traffic = ((float) (mobil_tx + mobil_rx) / ((float)1024*(float)1024));
          //  Log.i("appTrafficLogs","traffic" + traffic);
            traffic = Math.round(traffic * (float) 10.0) / (float) 10.0;
           // Log.i("appTrafficLogs","traffic" + traffic);
            traffic = traffic - getTrafficApps();
           // Log.i("appTrafficLogs","traffic" + traffic);

             Log.i("appTrafficLogs","getTrafficApps. appLabel = " + getApplicationLabel(mContex.getPackageManager()) +
                    " traffic last = " + getTrafficApps());
            Log.i("appTrafficLogs","getTrafficApps. appLabel = " + getApplicationLabel(mContex.getPackageManager()) +
                    " allTrafficApp = " +  ((float) (mobil_tx + mobil_rx) / ((float)1024*(float)1024)));


            if(traffic >= 0)
              return traffic;
            else return  0;
        }

        //Log.i("appTrafficLogs","getRebootAction() = 1 " + getTrafficAppsForReboot());
       // Log.i("appTrafficLogs","traffic = " + traffic);
      //  return Math.round(traffic * (float) 10.0) / (float) 10.0;
        //return Math.round((mobil_tx + mobil_rx)/ 1024);
    }
    public float getWiFiUsageKb() {
        float traffic = ((float) (wifi_rx + wifi_tx)) / ((float)1024*(float)1024);
        return Math.round(traffic * (float) 10.0) / (float) 10.0;
        //return Math.round((wifi_rx + wifi_tx)/ 1024);
    }
    public float getTrafficApps(){        //достаем траффик приложения по окончанию учетного периода
        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);

        return mySharedPreferences.getFloat(mContex.getPackageManager().getApplicationLabel(app).toString(),0);

    }
    public float getTrafficAppsForReboot(){ //достаем траффик приложения если была перезагрузка
        SharedPreferences mySharedPreferences = mContex.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT, Context.MODE_PRIVATE);

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


}

package com.devphill.traficMonitor.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.devphill.traficMonitor.service.TrafficService;

public class Util{


    public static boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public static int getRebootAction(Context context) {
        try{
            SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
            // Log.i("appTrafficLogs", "getRebootAction " + mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION, 0));
            return mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION, 0);
        }
        catch (Exception e) {
            return 0;
        }

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String getNetworkType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getTypeName();
        }
        return null;
    }
}
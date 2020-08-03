package com.devphill.traficMonitor.service.helper;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.devphill.traficMonitor.App;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.model.ApplicationItem;
import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.ui.MainActivity;
import com.devphill.traficMonitor.ui.fragments.app_traffic.FragmentTrafficApps;

import java.util.ArrayList;
import java.util.Date;

public class TrafficHelper{

    public static long mobile_trafficTXToday, mobile_trafficRXToday, mobile_trafficTXYesterday, mobile_trafficRXYesterday,allTrafficMobile;

    public static ArrayList<ApplicationItem> appList = new ArrayList<ApplicationItem>();	//список приложений для вівода на дисплей
    public static ArrayList<ApplicationItem> appListLocal = new ArrayList<ApplicationItem>();	//список приложений для отслеживания траффика

    public static boolean isWifiEnabled = false;
    public static boolean isMobilEnabled = false;

    public static  void dbWriteTraffic(Context context,String idsim) {

        DBHelper dbHelper = new DBHelper(context);

        long realtime = System.currentTimeMillis();
        Date date = new Date(realtime);
        // создаем объект для данных

        ContentValues cv = new ContentValues();
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c = db.query(idsim, null, null, null, null, null, null);

        allTrafficMobile = mobile_trafficTXToday + mobile_trafficRXToday;
        allTrafficMobile = allTrafficMobile / 1024;       //для Кб


        if (allTrafficMobile > 0 || TrafficService.firstWriteDB) {

            cv.put("mobile_trafficTXToday", mobile_trafficTXToday);
            cv.put("mobile_trafficRXToday", mobile_trafficRXToday);
            cv.put("mobile_trafficTXYesterday", mobile_trafficTXYesterday);
            cv.put("mobile_trafficRXYesterday", mobile_trafficRXYesterday);
            cv.put("time", realtime);
            cv.put("allTrafficMobile", allTrafficMobile);
            cv.put("lastDay", date.getDate());
            //	cv.put("reboot_device", 1);


            // вставляем запись и получаем ее ID
            long rowID = db.insert("" + idsim + "", null, cv);
            Log.d("TrafficHelper", "row inserted, ID = " + rowID);

            TrafficService.firstWriteDB = false;
        }
        c.close();


    }		//записть данных в бд

    public static void updateData (Context context) {

       // final Intent intent = new Intent(MainFragmentAdapter.BROADCAST_ACTION);

    //    intent.putExtra(MainFragmentAdapter.UPDATE_DATA, 1);    //обновили граффик,

      /*  Intent intent2 = new Intent(FragmentTrafficApps.UPDATE_TRAFFIC_APPS);
        intent2.putExtra("allTrafficMobile", allTrafficMobile);
        context.sendBroadcast(intent2);

        try {
            context.sendBroadcast(intent);            //послали интент фрагменту
        } catch (Error e) {
            e.printStackTrace();
        }*/

    }

   public static void updateAppTrafficList(Context context) {

        updateNetworkState(context);


        for (int i = 0; i < appListLocal.size(); i++) {

            ApplicationItem appInfolocal = appListLocal.get(i);

            appInfolocal.setMobilTraffic(isMobilEnabled);
            appInfolocal.setWiFiTraffic(isWifiEnabled);
            appInfolocal.update();

            if(appInfolocal.getWiFiUsageMb() > 0 || appInfolocal.getMobileUsageMb() > 0){

                if(!appList.contains(appInfolocal)) {
                    appList.add(appListLocal.get(i));
                }
            }
        }
    }

    public static void updateNetworkState(Context context) {
        isWifiEnabled = isConnectedWifi(context);
        isMobilEnabled = isConnectedMobile(context);
    }

    public static boolean isConnectedWifi(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedMobile(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static void updateNoty (Context context){

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.noty);
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        float trafficFloat = (float)allTrafficMobile/1024;
        trafficFloat = Math.round(trafficFloat*(float)10.0)/(float)10.0;

        float trafficTxFloat = (float)mobile_trafficTXToday/1048576;
        trafficTxFloat = Math.round(trafficTxFloat*(float)10.0)/(float)10.0;  //округляем до сотых


        float trafficRxFloat = (float)mobile_trafficRXToday/1048576;
        trafficRxFloat = Math.round(trafficRxFloat*(float)10.0)/(float)10.0;  //округляем до сотых

        float procent = trafficFloat /  (float)App.dataManager.getStopLevel()*100;
        procent = Math.round(procent*(float)10.0/(float)10.0);

        contentView.setImageViewResource(R.id.image, R.drawable.bittorrent);
        contentView.setProgressBar(R.id.usageData, App.dataManager.getStopLevel(), (int) (allTrafficMobile / 1024), false);
        contentView.setImageViewResource(R.id.imSendData, R.drawable.arrowup);
        contentView.setImageViewResource(R.id.imDownloadData, R.drawable.arrowdown);
        if((int)trafficFloat < 100) {
            contentView.setTextViewText(R.id.title, context.getString(R.string.used) + " " + trafficFloat + " " + context.getString(R.string.mb));
            contentView.setTextViewText(R.id.tvDownloadData,Float.toString(trafficRxFloat));
            contentView.setTextViewText(R.id.tvSendData,Float.toString(trafficTxFloat));
        }
        else {
            contentView.setTextViewText(R.id.title, context.getString(R.string.used) + " " + (int)trafficFloat + " " + context.getString(R.string.mb));
            contentView.setTextViewText(R.id.tvDownloadData,"" + (int)trafficRxFloat);
            contentView.setTextViewText(R.id.tvSendData,"" + (int)trafficTxFloat);
        }

        contentView.setTextViewText(R.id.procentData,(int)procent + " %");

        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        mBuilder.setContentIntent(intent);


        mBuilder.setSmallIcon(R.drawable.bittorrent);
        mBuilder.setContent(contentView);



        notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        //notification.defaults |= Notification.DEFAULT_SOUND;
        //notification.defaults |= Notification.DEFAULT_VIBRATE;
        mNotificationManager.notify(1, notification);

    }

    public static void saveTrafficAppsForReboot(Context context){

        SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        if(appList != null) {
            for (int i = 0; i < appList.size(); i++) {
                ApplicationItem item = appList.get(i);
                //Log.i(LOG_TAG, " saveTrafficAppsForReboot app = " + item.getApplicationLabel(getBaseContext().getPackageManager()) + "\n " +
                //	item.getMobileUsageKb());
                editor.putFloat(item.getApplicationLabel(context.getPackageManager()),item.getMobileMb());
            }
        }

        editor.apply();


    } //ложим траффик приложения если была перезагрузка

    public static void saveTrafficAppsForRebootWiFi(Context context){

        SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT_WIFI, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        if(appList != null) {
            for (int i = 0; i < appList.size(); i++) {
                ApplicationItem item = appList.get(i);
                //Log.i(LOG_TAG, " saveTrafficAppsForReboot app = " + item.getApplicationLabel(getBaseContext().getPackageManager()) + "\n " +
                //	item.getMobileUsageKb());
                editor.putFloat(item.getApplicationLabel(context.getPackageManager()),item.getWiFiMb());
            }
        }

        editor.apply();


    } //ложим траффик приложения если была перезагрузка

    public static void saveTotalTraffic(Context context) {

        SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        long totalTraffic = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();

        editor.putLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC, totalTraffic);

        editor.apply();
    }

    public static void saveTotalTrafficReboot(Context context) {

        SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC_REBOOT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        long totalTraffic = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();

        editor.putLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC, totalTraffic);

        editor.apply();
    }

    public static void resetTraffic() {

        mobile_trafficTXYesterday = TrafficStats.getMobileTxBytes();
        mobile_trafficRXYesterday = TrafficStats.getMobileRxBytes();

        mobile_trafficTXToday = 0;
        mobile_trafficRXToday = 0;

    }	//перезаписываем теперешний траффик в переменные для вчерашнего

    public static long getAllTrafficMobile() {
        return allTrafficMobile;
    }

    public static void resetTrafficReboot(Context context, String idsim) {

        DBHelper dbHelper = new DBHelper(context);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Cursor c = db.query(idsim, null, null, null, null, null, null);

        long realtime = System.currentTimeMillis();
        Date date = new Date(realtime);

        if (c.moveToLast()) {
            int mobile_trafficTXYesterdayColIndex = c.getColumnIndex("mobile_trafficTXYesterday");
            int mobile_trafficRXYesterdayColIndex = c.getColumnIndex("mobile_trafficRXYesterday");
            int mobile_trafficTXTodayColIndex = c.getColumnIndex("mobile_trafficTXToday");
            int mobile_trafficRXTodayColIndex = c.getColumnIndex("mobile_trafficRXToday");


            mobile_trafficTXYesterday = c.getLong(mobile_trafficTXTodayColIndex); //Переданные через мобильный интерфейс
            mobile_trafficRXYesterday = c.getLong(mobile_trafficRXTodayColIndex); //Принятые через мобильный интерфейс

        }

        cv.put("mobile_trafficTXToday", mobile_trafficTXToday);
        cv.put("mobile_trafficRXToday", mobile_trafficRXToday);
        cv.put("mobile_trafficTXYesterday", mobile_trafficTXYesterday);
        cv.put("mobile_trafficRXYesterday", mobile_trafficRXYesterday);
        //cv.put("time", realtime);
        cv.put("allTrafficMobile", allTrafficMobile);
        cv.put("lastDay", date.getDate());
        cv.put("reboot_device", 1);


        // вставляем запись и получаем ее ID
        long rowID = db.insert("" + idsim + "", null, cv);

        c.close();


    }	//в переменные для вчерашнего запишем траффик до перезагрузки

    public static void initAppTrafficList(Context context) {

        int count = 0;

        appListLocal.clear();
        appList.clear();

        //clearMobileTrafficApps();
        clearMobileTrafficAppsForReboot(context);
        //	clearWiFiTrafficApps();
        clearWiFiTrafficAppsForReboot(context);

        for (ApplicationInfo app : context.getPackageManager().getInstalledApplications(0)) {

            ApplicationItem item = ApplicationItem.create(app, context);
            if (item != null) {
                appListLocal.add(item);
                count++;
            }
        }

    }

    public static void clearMobileTrafficAppsForReboot(Context context){		//очистить сохраненный мобильный траффик приложений

        SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        if(appList != null) {
            for (int i = 0; i < appList.size(); i++) {
                ApplicationItem item = appList.get(i);
                editor.putFloat(item.getApplicationLabel(context.getPackageManager()),0);
            }
        }
        editor.apply();
    }

    public static void clearWiFiTrafficAppsForReboot(Context context){		//очистить сохраненный WiFi траффик приложений

        SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS_REBOOT_WIFI, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        if(appList != null) {
            for (int i = 0; i < appList.size(); i++) {
                ApplicationItem item = appList.get(i);
                editor.putFloat(item.getApplicationLabel(context.getPackageManager()),0);
            }
        }
        editor.apply();
    }
}
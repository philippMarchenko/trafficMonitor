package com.devphill.traficMonitor.service.helper;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.NetworkStatsManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.devphill.traficMonitor.App;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.networkStats.NetworkStatsHelper;
import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.ui.MainActivity;
import com.devphill.traficMonitor.ui.Widget;
import com.devphill.traficMonitor.ui.fragments.main.MainFragment;

import java.util.Date;


import static com.devphill.traficMonitor.ui.fragments.main.MainFragment.UPDATE_DATA;

public class TrafficMHelper{


    final String LOG_TAG = "TrafficMHelper";

    float trafficFloat;
    float trafficTxFloat;
    float trafficRxFloat ;

    public static long getAllTrafficMobile() {
        return allTrafficMobile;
    }

    public static long mobile_trafficTXToday, mobile_trafficRXToday, mobile_trafficTXYesterday, mobile_trafficRXYesterday,allTrafficMobile;

    public static long getRxData(Context context){

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager);
        return networkStatsHelper.getAllRxBytesMobile(context);
    }

    public static long getTxData(Context context){

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager);
        return networkStatsHelper.getAllTxBytesMobile(context);
    }

    public static void dbWriteTraffic(Context context,String idsim) {

        try{
            DBHelper dbHelper = new DBHelper(context);

            long realtime = System.currentTimeMillis();
            Date date = new Date(realtime);
            // создаем объект для данных

            ContentValues cv = new ContentValues();
            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // делаем запрос всех данных из таблицы mytable, получаем Cursor
            Cursor c = db.query(idsim, null, null, null, null, null, null);

            if (allTrafficMobile > 0 || TrafficService.firstWriteDB) {

                cv.put("mobile_trafficTXToday", mobile_trafficTXToday);
                cv.put("mobile_trafficRXToday", mobile_trafficRXToday);
                cv.put("mobile_trafficTXYesterday", mobile_trafficTXYesterday);
                cv.put("mobile_trafficRXYesterday", mobile_trafficRXYesterday);
                cv.put("time", realtime);
                cv.put("allTrafficMobile", allTrafficMobile);
                cv.put("lastDay", date.getDate());

                // вставляем запись и получаем ее ID
                long rowID = db.insert("" + idsim + "", null, cv);
                Log.d("TrafficMHelper", "row inserted, ID = " + rowID);

                TrafficService.firstWriteDB = false;
            }
            c.close();
        }
        catch(Exception e){

        }

    }		//записть данных в бд

    public static void updateNoty (Context context,String packageName){

        RemoteViews contentView = new RemoteViews(packageName, R.layout.noty);
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(context);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        float trafficFloat = (float)allTrafficMobile/1024;
        trafficFloat = Math.round(trafficFloat*(float)10.0)/(float)10.0;

        float trafficTxFloat = (float)mobile_trafficTXToday/1048576;
        trafficTxFloat = Math.round(trafficTxFloat*(float)10.0)/(float)10.0;  //округляем до сотых


        float trafficRxFloat = (float)mobile_trafficRXToday/1048576;
        trafficRxFloat = Math.round(trafficRxFloat*(float)10.0)/(float)10.0;  //округляем до сотых

        float procent = trafficFloat /  (float) App.dataManager.getStopLevel() * 100;
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

    public static void refreshWidget(Context context) {

        Widget widget = new Widget();

        mobile_trafficTXToday = getTxData(context); //Переданные через мобильный интерфейс
        mobile_trafficRXToday = getRxData(context); //Принятые через мобильный интерфейс


        allTrafficMobile = mobile_trafficTXToday + mobile_trafficRXToday;
        allTrafficMobile = allTrafficMobile / 1024;       //для Кб


        float trafficFloat = (float)allTrafficMobile/1024;
        trafficFloat = Math.round(trafficFloat*(float)100.0)/(float)100.0;

        float trafficTxFloat = (float)mobile_trafficTXToday/1048576;
        trafficTxFloat = Math.round(trafficTxFloat*(float)100.0)/(float)100.0;  //округляем до сотых

        float trafficRxFloat = (float)mobile_trafficRXToday/1048576;
        trafficRxFloat = Math.round(trafficRxFloat*(float)100.0)/(float)100.0;  //округляем до сотых

        Intent i = new Intent(context,Widget.class);
        i.setAction(Widget.FORCE_WIDGET_UPDATE);

        i.putExtra("trafficFloat",trafficFloat);
        i.putExtra("trafficTxFloat",trafficTxFloat);
        i.putExtra("trafficRxFloat",trafficRxFloat);
        i.putExtra("stopLevel", App.dataManager.getStopLevel());

        widget.onReceive(context,i);


        Intent intent = new Intent(MainFragment.UPDATE_DATA_ACTION);

        intent.putExtra(UPDATE_DATA,1);    //обновили граффик,
        intent.putExtra("trafficFloat",trafficFloat);
        intent.putExtra("trafficTxFloat",trafficTxFloat);
        intent.putExtra("trafficRxFloat",trafficRxFloat);

        try
        {
            context.sendBroadcast(intent);            //послали интент фрагменту

        }
        catch(Error e)
        {
            e.printStackTrace();
        }

    }

    public static void updateData (Context context){



    }			//посылем сообщения фрагментам для обновления данных



}
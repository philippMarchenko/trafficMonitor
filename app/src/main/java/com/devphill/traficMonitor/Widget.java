package com.devphill.traficMonitor;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

public class Widget extends AppWidgetProvider {
    public static String FORCE_WIDGET_UPDATE = "com.devphill.traficMonitor.FORCE_WIDGET_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("Widget_log", "onUpdate");
       // ..startService(context);
    }
    @Override
    public void onReceive(Context context, Intent intent){
        super.onReceive(context, intent);
        Log.d("Widget_log", "onReceive");
        if (FORCE_WIDGET_UPDATE.equals(intent.getAction()))
            updateWidget(context,intent);
    }
    private void updateWidget(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, Widget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        drawWidget(context, appWidgetManager, appWidgetIds,intent);
    }
    private void drawWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds,Intent intent) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            float traffic = intent.getFloatExtra("trafficFloat",0);
            float trafficTxFloat = intent.getFloatExtra("trafficTxFloat",0);
            float trafficRxFloat = intent.getFloatExtra("trafficRxFloat",0);


            if(traffic < 100){
                views.setTextViewText(R.id.titleW, "Использовано " + traffic + " Мб");
                views.setTextViewText(R.id.tvSendDataW, Float.toString(trafficTxFloat));
                views.setTextViewText(R.id.tvDownloadDataW, Float.toString(trafficRxFloat));
            }
            else{
                views.setTextViewText(R.id.titleW, "Использовано " + (int)traffic + " Мб");
                views.setTextViewText(R.id.tvSendDataW, "" + (int)trafficTxFloat);
                views.setTextViewText(R.id.tvDownloadDataW, "" + (int)trafficRxFloat);
            }

            views.setProgressBar(R.id.usageDataW, intent.getIntExtra("stopLevel",0),(int)traffic,false);
            views.setImageViewResource(R.id.imSendDataW,R.drawable.arrowup);
            views.setImageViewResource(R.id.imDownloadDataW,R.drawable.arrowdown);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d("Widget_log", "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d("Widget_log", "onDisabled");
    }
}
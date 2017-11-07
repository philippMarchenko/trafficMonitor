package com.devphill.traficMonitor.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.devphill.traficMonitor.service.TrafficService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, TrafficService.class);
        i.putExtra("task",TrafficService.ALARM_ACTION);
        context.startService(i);
        Log.i("alarmReceiver", "onReceive");

    }

}
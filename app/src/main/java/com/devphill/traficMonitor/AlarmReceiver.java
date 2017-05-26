package com.devphill.traficMonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.devphill.traficMonitor.TrafficService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, TrafficService.class);
        i.putExtra("task",TrafficService.ALARM_ACTION);
        context.startService(i);
        Log.i("alarmReceiver", "onReceive");





    }

}
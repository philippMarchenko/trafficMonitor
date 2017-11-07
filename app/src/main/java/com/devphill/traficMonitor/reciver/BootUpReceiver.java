package com.devphill.traficMonitor.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.devphill.traficMonitor.service.TrafficService;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
       /* Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);*/

        Intent i = new Intent(context, TrafficService.class);
        i.putExtra("task",TrafficService.SET_REBOOT_ACTION);
     //   i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        context.startService(i);
       // TrafficService.setRebootAction(context,1);
        Log.i("bootUpLog", "BootUpReceiver");
    }

}
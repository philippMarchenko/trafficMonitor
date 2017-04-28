package com.devphill.traficMonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d("NetworkCheckReceiver", "NetworkCheckReceiver invoked...");


            boolean noConnectivity = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            Intent mIntent = new Intent(context, TrafficService.class);
            if (!noConnectivity) {
                Log.d("NetworkCheckReceiver", "connected");
           //     context.startService(mIntent);
            }
            else
            {
                Log.d("NetworkCheckReceiver", "disconnected");
             //   context.stopService(mIntent);
            }
        }
    }
}

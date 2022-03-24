package com.devphill.traficMonitor.premmision;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import timber.log.Timber;

/**
 * Created by Valera on 24.11.2017.
 */

public class Permission {

    public static final int READ_PHONE_STATE_REQUEST = 37;

    Context context;
    Activity activity;

    public Permission(Context context, Activity activity) {

        this.context = context;
        this.activity = activity;


    }


    public void requestPermissionPhoneStateStats() {
        if (!hasPermissionToReadPhoneStats()) {
            requestPhoneStateStats();
        }
    }

    public void requestPhoneStateStats() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST);
    }

    public boolean hasPermissionToReadPhoneStats() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Timber.d("hasPermissionToReadPhoneStats  PackageManager.PERMISSION_GRANTED");
            return true;
        } else {
            Timber.d("hasPermissionToReadPhoneStats  PackageManager. false");

            return false;
        }
    }

    public boolean hasPermissionToReadNetworkHistory() {

        final AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }

        return false;
    }

    public void requestReadNetworkHistoryAccess() {

        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

package com.devphill.traficMonitor;

import android.app.Application;

import com.devphill.traficMonitor.ui.DataManager;

import timber.log.Timber;

public class App extends Application {


    public static DataManager dataManager;

    @Override public void onCreate() {
        super.onCreate();

        dataManager = new DataManager(getApplicationContext());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
         //   Timber.plant(new CrashReportingTree());
        }
    }


}
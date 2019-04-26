package com.devphill.traficMonitor;

import android.app.Application;

import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.ui.DataManager;
import com.devphill.traficMonitor.ui.TimeUtil;

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


        if(dataManager.isAppLaunchFirstTime()){
            dataManager.setStopLevel(100);
            dataManager.setAlertLevel(80);
            dataManager.setDate(TimeUtil.Companion.getTodayByFormat("yyyy-MM-dd"));

            dataManager.setPeriod(TrafficService.PERIOD_DAY);
            dataManager.setIsAppLaunchFirstTime(false);
        }

    }


}
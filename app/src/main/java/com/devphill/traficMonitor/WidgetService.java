package com.devphill.traficMonitor;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;



public class WidgetService extends Service {

    Timer timerW;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("Widget_log", "onCreate widget service");


       // task();
}
    public void task() {

        final Handler uiHandler = new Handler();
        timerW.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {//функция для длительных задач(например работа с сетью)

                refreshWidget();
                uiHandler.post(new Runnable() { //здесь можна выводить на экран и работать с основным потоком
                    @Override
                    public void run() {

                    }
                });

            }

            ;

        }, 0L, 3L * 1000); // интервал - 8000 миллисекунд, 0 миллисекунд до первого запуска.
    }
    private void refreshWidget() {

       // float trafficFloat = (float)allTrafficMobile/1024;
       // trafficFloat = Math.round(trafficFloat*(float)100.0)/(float)100.0;

        Intent i = new Intent(getBaseContext(),Widget.class);
        i.setAction(Widget.FORCE_WIDGET_UPDATE);

        i.putExtra("trafficFloat",36.58);

        //getBaseContext().is

        sendBroadcast(i);
        Log.d("Widget_log", "sendBroadcast");
    }
    @Override
    public void onDestroy()
    {
        Log.d("Widget_log", "onDestroy widget service");
    }


}
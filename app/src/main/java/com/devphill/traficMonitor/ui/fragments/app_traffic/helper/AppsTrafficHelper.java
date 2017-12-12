package com.devphill.traficMonitor.ui.fragments.app_traffic.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.TrafficStats;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.model.ApplicationItem;
import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.service.helper.TrafficHelper;
import com.devphill.traficMonitor.ui.fragments.app_traffic.FragmentTrafficApps;
import com.devphill.traficMonitor.util.Util;

import java.util.Comparator;

public class AppsTrafficHelper{

    public  String LOG_TAG = "AppsTrafficHelper";

    private Context context;

    private TextView tvDataUsageWiFi;
    private TextView tvDataUsageMobile;
    private TextView tvDataUsageTotal;

    ArrayAdapter<ApplicationItem> adapterApplications ;

    public AppsTrafficHelper(Context context, TextView tvDataUsageWiFi, TextView tvDataUsageMobile, TextView tvDataUsageTotal) {

        this.context = context;
        this.tvDataUsageWiFi = tvDataUsageWiFi;
        this.tvDataUsageMobile = tvDataUsageMobile;
        this.tvDataUsageTotal = tvDataUsageTotal;
    }

    public void initAdapter() {

        adapterApplications = new ArrayAdapter<ApplicationItem>(context, R.layout.item_application) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // ApplicationItem app = TrafficService.appList.get(position);
                ApplicationItem app = getItem(position);
                final View result;
                if (convertView == null) {
                    result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_application, parent, false);
                } else {
                    result = convertView;
                }

                TextView tvAppName = (TextView) result.findViewById(R.id.tvAppName);
                TextView mobileUsageApp = (TextView) result.findViewById(R.id.mobileUsageApp);
                TextView wiFiusageApp  = (TextView) result.findViewById(R.id.wiFiusageApp);
                ImageView imageApp = (ImageView) result.findViewById(R.id.imageApp);


                mobileUsageApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!FragmentTrafficApps.sortMobile) {
                            Toast.makeText(getContext(), "Сортировка по мобильному траффику.", Toast.LENGTH_SHORT).show();
                            FragmentTrafficApps.sortMobile = true;
                            updateAdapter();
                        }
                    }
                });

                wiFiusageApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(FragmentTrafficApps.sortMobile){
                            Toast.makeText(getContext(), "Сортировка по WiFi.", Toast.LENGTH_SHORT).show();
                            FragmentTrafficApps.sortMobile = false;
                            updateAdapter();
                        }


                    }
                });
                // TODO: resize once
                final int iconSize = Math.round(48 * context.getResources().getDisplayMetrics().density);
                imageApp.setImageDrawable(new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(
                        ((BitmapDrawable) app.getIcon(context.getPackageManager())).getBitmap(), iconSize, iconSize, true)
                ));

                tvAppName.setText(app.getApplicationLabel(context.getPackageManager()));

                if(app.getMobileUsageMb() >= 0)
                    mobileUsageApp.setText(Float.toString(app.getMobileUsageMb()) + " Mb");
                else
                    Log.i(LOG_TAG, "Траффик почемуто меньше ноля((" + app.getMobileUsageMb());

                wiFiusageApp.setText(Float.toString(app.getWiFiUsageMb()) + " Mb");

                return result;
            }
            @Override
            public int getCount() {
                return super.getCount();
            }

            @Override
            public Filter getFilter() {
                return super.getFilter();
            }
            @Override
            public void notifyDataSetChanged() {
                //do your sorting here

                super.notifyDataSetChanged();
            }
        };

        initAppList();

    }

    public void initAppList() {

        int count = 0;

        Log.i(LOG_TAG, "TrafficService.appList.size() " + TrafficHelper.appList.size());
        for (int i = 0; i < TrafficHelper.appList.size(); i++) {
            ApplicationItem app = TrafficHelper.appList.get(i);
            if(app != null) {
                adapterApplications.add(app);
            }
            Log.i(LOG_TAG, "count = " + ++count);
        }
        Log.i(LOG_TAG, "Fragment2 initAppList");
    }

    public void updateAdapter() {

        for (int i = 0, l = adapterApplications.getCount(); i < l; i++) {
            ApplicationItem app = adapterApplications.getItem(i);
            app.setMobilTraffic(FragmentTrafficApps.sortMobile);
            app.update();
        }

        if(FragmentTrafficApps.sortMobile) {
            adapterApplications.sort(new Comparator<ApplicationItem>() {
                @Override
                public int compare(ApplicationItem lhs, ApplicationItem rhs) {
                    return (int) ((rhs.getMobileUsageMb() - lhs.getMobileUsageMb()) * 10);
                }
            });
        }
        else{
            adapterApplications.sort(new Comparator<ApplicationItem>() {
                @Override
                public int compare(ApplicationItem lhs, ApplicationItem rhs) {
                    return (int) ((rhs.getWiFiUsageMb() - lhs.getWiFiUsageMb()) * 10);
                }
            });
        }

        adapterApplications.notifyDataSetChanged();

    }

    public long getTotalTraffic(){        //достаем полный траффик(wiFi + mobile) приложения по окончанию учетного периода

        try{
            SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);
            //Log.i(LOG_TAG, "Fragment2 getTotalTraffic " + mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0));
            return mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0);
        }
        catch(Exception e){
            return  0;
        }
    }

    public long getAllTrafficReboot(){ //достаем траффик приложения если была перезагрузка
        SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC_REBOOT, Context.MODE_PRIVATE);
        return mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0);

    }

    public void showAllTraffic(long traffic){

        long trafficWiFi = 0;
        long trafficTotal = 0;

        float trafficMobileFloat = (float) traffic / 1024;
        trafficMobileFloat = Math.round(trafficMobileFloat * (float) 10.0) / (float) 10.0;
        tvDataUsageMobile.setText(trafficMobileFloat + " Mb");


        if(Util.getRebootAction(context) == 1) {
            trafficTotal = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) + getAllTrafficReboot();
        }
        else{
            trafficTotal = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) - getTotalTraffic();
        }

        float trafficTotalFloat = ((float)trafficTotal) / ((float)1024*(float)1024);
        trafficTotalFloat = Math.round(trafficTotalFloat * (float) 10.0) / (float) 10.0;
        tvDataUsageTotal.setText(Float.toString(trafficTotalFloat) + " Mb");

        trafficWiFi = trafficTotal - (traffic*1024);
        float trafficWiFiFloat = ((float)trafficWiFi) / ((float)1024*(float)1024);
        trafficWiFiFloat = Math.round(trafficWiFiFloat * (float) 10.0) / (float) 10.0;
        if(trafficWiFiFloat < 0)
            trafficWiFiFloat = 0;

        tvDataUsageWiFi.setText(Float.toString(trafficWiFiFloat) + " Mb");

    }
}
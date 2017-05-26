package com.devphill.traficMonitor.fragments;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.internal.ParcelableSparseArray;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.devphill.traficMonitor.ApplicationItem;
import com.devphill.traficMonitor.DBHelper;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.StateSavingArrayAdapter;
import com.devphill.traficMonitor.TrafficService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//implements CompoundButton.OnCheckedChangeListener
public class FragmentTrafficApps extends Fragment {


    public  String LOG_TAG = "fragmentTrafficApps";

    DBHelper dbHelper;
    /// создаем объект для данных базы
    ContentValues cv = new ContentValues();
    Timer myTimer = new Timer(); // Создаем таймер

    TextView three_g_kb;
    TextView tvDataUsageWiFi;
    TextView tvDataUsageMobile;
    TextView tvDataUsageTotal;
    ListView lvApplications;


    static ArrayAdapter<ApplicationItem> adapterApplications ;

    public static BroadcastReceiver brAppsTrafficFragment;
    public static final String UPDATE_TRAFFIC_APPS = "update_traffic_apps";

    private boolean isWifiEnabled = false;
    private boolean isMobilEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_traff, container, false);
      //  if(isMyServiceRunning(TrafficService.class)) {
            initAdapter();
      //  }

        tvDataUsageMobile = (TextView) view.findViewById(R.id.tvDataUsageMobile);
        tvDataUsageWiFi = (TextView) view.findViewById(R.id.tvDataUsageWiFi);
        tvDataUsageTotal = (TextView) view.findViewById(R.id.tvDataUsageTotal);

        lvApplications = (ListView) view.findViewById(R.id.lvInstallApplication);



        if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED && TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED) {

            lvApplications.setAdapter(adapterApplications);

        } else {
            //  tvSupported.setVisibility(View.VISIBLE);
        }
        brAppsTrafficFragment = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //   Log.i(LOG_TAG, "onReceive brAppsTrafficFragment ");
                updateAdapter();

                long trafficMobile = intent.getLongExtra("allTrafficMobile", 0);
                long trafficWiFi = 0;
                long trafficTotal = 0;

                float trafficMobileFloat = (float) trafficMobile / 1024;
                trafficMobileFloat = Math.round(trafficMobileFloat * (float) 10.0) / (float) 10.0;
                tvDataUsageMobile.setText(trafficMobileFloat + " Mb");
                //if(adapterApplications != null) {
                    //if (adapterApplications.getItem(1).getRebootAction() == 1) {
                    //    trafficTotal = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) + getTotalTraffic();
                  //  } else {

                if(getRebootAction() == 1) {
                    trafficTotal = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) + getTotalTraffic();
                }
                else{
                    trafficTotal = (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()) - getTotalTraffic();
                }
                    // }
               // }
                float trafficTotalFloat = ((float)trafficTotal) / ((float)1024*(float)1024);
                trafficTotalFloat = Math.round(trafficTotalFloat * (float) 10.0) / (float) 10.0;
                tvDataUsageTotal.setText(Float.toString(trafficTotalFloat) + " Mb");

                trafficWiFi = trafficTotal - (trafficMobile*1024);
                float trafficWiFiFloat = ((float)trafficWiFi) / ((float)1024*(float)1024);
                trafficWiFiFloat = Math.round(trafficWiFiFloat * (float) 10.0) / (float) 10.0;
                if(trafficWiFiFloat < 0)
                    trafficWiFiFloat = 0;
                tvDataUsageWiFi.setText(Float.toString(trafficWiFiFloat) + " Mb");
                Log.i(LOG_TAG, "updateAdapter, allTrafficMobile " + trafficMobileFloat + " trafficTotal " + trafficTotal);

            }
        };


        Log.i(LOG_TAG, "onCreateView fragment2 ");

        return view;

    }
    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void initAdapter() {

        adapterApplications = new ArrayAdapter<ApplicationItem>(getActivity(), R.layout.item_install_application) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
               // ApplicationItem app = TrafficService.appList.get(position);
                ApplicationItem app = getItem(position);
                final View result;
                if (convertView == null) {
                    result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_install_application, parent, false);
                } else {
                    result = convertView;
                }

                TextView tvAppName = (TextView) result.findViewById(R.id.tvAppName);
                TextView mobileUsageApp = (TextView) result.findViewById(R.id.mobileUsageApp);
                TextView wiFiusageApp  = (TextView) result.findViewById(R.id.wiFiusageApp);
                ImageView imageApp = (ImageView) result.findViewById(R.id.imageApp);

                // TODO: resize once
                final int iconSize = Math.round(48 * getResources().getDisplayMetrics().density);
                imageApp.setImageDrawable(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                        ((BitmapDrawable) app.getIcon(getActivity().getPackageManager())).getBitmap(), iconSize, iconSize, true)
                ));

                tvAppName.setText(app.getApplicationLabel(getActivity().getPackageManager()));

                if(app.getMobileUsageKb() >= 0)
                    mobileUsageApp.setText(Float.toString(app.getMobileUsageKb()) + " Mb");
                else
                    Log.i(LOG_TAG, "Траффик почемуто меньше ноля((" + app.getMobileUsageKb());

                wiFiusageApp.setText(Float.toString(app.getWiFiUsageKb()) + " Mb");

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

        SharedPreferences mySharedPreferences = getContext().getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);

                int count = 0;
                for (int i = 0; i < TrafficService.appList.size(); i++) {
                    ApplicationItem app = TrafficService.appList.get(i);
                    if(app != null) {
                        adapterApplications.add(app);
                    }
                    Log.i(LOG_TAG, "count = " + count++);
                }
        Log.i(LOG_TAG, "Fragment2 initAppList");
    }
    public void updateAdapter() {
   /*     updateNetworkState();
        for (int i = 0; i < TrafficService.appList.size(); i++) {
            ApplicationItem app = TrafficService.appList.get(i);
            app.setMobilTraffic(isMobilEnabled);
            app.update();
        }*/

        for (int i = 0, l = adapterApplications.getCount(); i < l; i++) {
            ApplicationItem app = adapterApplications.getItem(i);
            app.setMobilTraffic(isMobilEnabled);
            app.update();
        }
        adapterApplications.sort(new Comparator<ApplicationItem>() {
            @Override
            public int compare(ApplicationItem lhs, ApplicationItem rhs) {
              //  Log.i(LOG_TAG, "Fragment2 sort lhs " + lhs.getMobileUsageKb());
                //Log.i(LOG_TAG, "Fragment2 sort rhs " + rhs.getMobileUsageKb());

              //  Log.i(LOG_TAG, "Fragment2 sort " +(int)((rhs.getMobileUsageKb() - lhs.getMobileUsageKb())*10));
                return (int)((rhs.getMobileUsageKb() - lhs.getMobileUsageKb())*10);
            }
        });
        adapterApplications.notifyDataSetChanged();

    }
    private void updateNetworkState() {
        isWifiEnabled = isConnectedWifi();
        isMobilEnabled = isConnectedMobile();
    }
    public boolean isConnectedWifi(){
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }
    public boolean isConnectedMobile(){
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
    public long getTotalTraffic(){        //достаем полный траффик(wiFi + mobile) приложения по окончанию учетного периода
        SharedPreferences mySharedPreferences = getContext().getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);
        //Log.i(LOG_TAG, "Fragment2 getTotalTraffic " + mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0));
        return mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0);

    }
    public int getRebootAction() {
            try{
                SharedPreferences mySharedPreferences = getContext().getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
                // Log.i("appTrafficLogs", "getRebootAction " + mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION, 0));
                return mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_REBOOT_ACTION, 0);
            }
            catch (Exception e) {
                Log.i(LOG_TAG, "Ошибка запроса событие перезагрузки " + e.getMessage());
                return 0;
            }

    }
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(brAppsTrafficFragment, new IntentFilter(FragmentTrafficApps.UPDATE_TRAFFIC_APPS));
        Log.i(LOG_TAG, "Fragment2 onResume");
    }
    @Override
    public void onPause() {
        super.onPause();

        try {
            getActivity().unregisterReceiver(brAppsTrafficFragment);
        }catch (IllegalArgumentException e){
            Log.d(LOG_TAG, "Error unregisterReceiver in Fragment2" + e.getMessage());
        }

        Log.i(LOG_TAG, "Fragment2 onPause");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //saveAppTraffic();
        Log.i(LOG_TAG, "Fragment2 onDestroy");
    }
}

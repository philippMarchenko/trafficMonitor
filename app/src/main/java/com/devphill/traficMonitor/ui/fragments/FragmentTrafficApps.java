package com.devphill.traficMonitor.ui.fragments;


import android.app.ActivityManager;
import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devphill.traficMonitor.model.ApplicationItem;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.model.Package;
import com.devphill.traficMonitor.networkStats.LoadPackageList;
import com.devphill.traficMonitor.networkStats.LoadPackageListSharedPref;
import com.devphill.traficMonitor.networkStats.NetworkStatsHelper;
import com.devphill.traficMonitor.networkStats.PackageManagerHelper;
import com.devphill.traficMonitor.service.TrafficService;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentTrafficApps extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        LoadPackageList.ILoadPackageListListener{


    public  String LOG_TAG = "fragmentTrafficApps";

    DBHelper dbHelper;
    /// создаем объект для данных базы
    ContentValues cv = new ContentValues();
    Timer myTimer = new Timer(); // Создаем таймер

    TextView tvDataUsageWiFi;
    TextView tvDataUsageMobile;
    TextView tvDataUsageTotal;
    ListView lvApplications;


    static ArrayAdapter<ApplicationItem> adapterApplications ;
    static ArrayAdapter<Package> adapterApplicationsM ;

    private SwipeRefreshLayout swipeRefreshLayout;

    public static BroadcastReceiver brAppsTrafficFragment;
    public static final String UPDATE_TRAFFIC_APPS = "update_traffic_apps";

    private boolean isWifiEnabled = false;
    private boolean isMobilEnabled = false;

    private boolean sortMobile = true;

    public static final String APP_PREFERENCES_TRAFFIC_APPS_M = "TrafficAppsM";
   // public static final String APP_PREFERENCES_TRAFFIC_APPS_WIFI_M = "TrafficAppsWiFiM";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_traff, container, false);

        initAdapterM();

        dbHelper = new DBHelper(getContext());  // создаем объект для создания и управления версиями БД


        tvDataUsageMobile = (TextView) view.findViewById(R.id.tvDataUsageMobile);
        tvDataUsageWiFi = (TextView) view.findViewById(R.id.tvDataUsageWiFi);
        tvDataUsageTotal = (TextView) view.findViewById(R.id.tvDataUsageTotal);

        lvApplications = (ListView) view.findViewById(R.id.lvInstallApplication);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(R.color.accent);

        lvApplications.setAdapter(adapterApplicationsM);


        swipeRefreshLayout.setOnRefreshListener(this);
        if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED && TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED) {

            lvApplications.setAdapter(adapterApplicationsM);

        } else {

        }
        brAppsTrafficFragment = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //   Log.i(LOG_TAG, "onReceive brAppsTrafficFragment ");
               // updateAdapterM();

                long trafficMobile = intent.getLongExtra("allTrafficMobile", 0);
               // getAllTraffic(trafficMobile);
                showAllTrafficM();

            }
        };

        startUpdateAppTraffic();

        Log.i(LOG_TAG, "onCreateView FragmentTrafficApps ");

        return view;

    }


    public void showAllTraffic(long traffic){

        long trafficWiFi = 0;
        long trafficTotal = 0;

        float trafficMobileFloat = (float) traffic / 1024;
        trafficMobileFloat = Math.round(trafficMobileFloat * (float) 10.0) / (float) 10.0;
        tvDataUsageMobile.setText(trafficMobileFloat + " Mb");


        if(getRebootAction() == 1) {
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

    public void showAllTrafficM(){

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getContext().getSystemService(Context.NETWORK_STATS_SERVICE);

        NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager);

        float allData = ((float) (networkStatsHelper.getAllRxBytesMobile(getContext())
                + networkStatsHelper.getAllTxBytesMobile(getContext())
                + networkStatsHelper.getAllRxBytesWifi()
                + networkStatsHelper.getAllTxBytesWifi()) / ((float)1024*(float)1024));

        allData = Math.round(allData * (float) 10.0) / (float) 10.0;

        tvDataUsageTotal.setText(Float.toString(allData) + " Mb");

        float mobileData = ((float) (networkStatsHelper.getAllRxBytesMobile(getContext())
                + networkStatsHelper.getAllTxBytesMobile(getContext())) / ((float)1024*(float)1024));

        mobileData = Math.round(mobileData * (float) 10.0) / (float) 10.0;

        tvDataUsageMobile.setText(Float.toString(mobileData) + " Mb");

        float wiFiData = ((float) (networkStatsHelper.getAllRxBytesWifi()
                + networkStatsHelper.getAllTxBytesWifi()) / ((float)1024*(float)1024));

        wiFiData = Math.round(wiFiData * (float) 10.0) / (float) 10.0;

        tvDataUsageWiFi.setText(Float.toString(wiFiData) + " Mb");

    }

    public long getAllTrafficReboot(){ //достаем траффик приложения если была перезагрузка
        SharedPreferences mySharedPreferences = getContext().getSharedPreferences(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC_REBOOT, Context.MODE_PRIVATE);
        return mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0);

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


                mobileUsageApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!sortMobile) {
                            Toast.makeText(getContext(), "Сортировка по мобильному траффику.", Toast.LENGTH_SHORT).show();
                            sortMobile = true;
                            updateAdapter();
                        }
                    }
                });

                wiFiusageApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(sortMobile){
                            Toast.makeText(getContext(), "Сортировка по WiFi.", Toast.LENGTH_SHORT).show();
                            sortMobile = false;
                            updateAdapter();
                        }


                    }
                });
                // TODO: resize once
                final int iconSize = Math.round(48 * getResources().getDisplayMetrics().density);
                imageApp.setImageDrawable(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                        ((BitmapDrawable) app.getIcon(getActivity().getPackageManager())).getBitmap(), iconSize, iconSize, true)
                ));

                tvAppName.setText(app.getApplicationLabel(getActivity().getPackageManager()));

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

        Log.i(LOG_TAG, "TrafficService.appList.size() " + TrafficService.appList.size());
                for (int i = 0; i < TrafficService.appList.size(); i++) {
                    ApplicationItem app = TrafficService.appList.get(i);
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
            app.setMobilTraffic(isMobilEnabled);
            app.update();
        }

        if(sortMobile) {
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

    public void initAdapterM(){

        adapterApplicationsM = new ArrayAdapter<Package>(getActivity(), R.layout.item_install_application) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // ApplicationItem app = TrafficService.appList.get(position);
                Package mPackage = getItem(position);
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


                mobileUsageApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!sortMobile) {
                            Toast.makeText(getContext(), "Сортировка по мобильному траффику.", Toast.LENGTH_SHORT).show();
                            sortMobile = true;
                            updateAdapterM();
                        }
                    }
                });

                wiFiusageApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(sortMobile){
                            Toast.makeText(getContext(), "Сортировка по WiFi.", Toast.LENGTH_SHORT).show();
                            sortMobile = false;
                            updateAdapterM();
                        }


                    }
                });
                // TODO: resize once
                final int iconSize = Math.round(48 * getResources().getDisplayMetrics().density);
                imageApp.setImageDrawable(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                        mPackage.getIcon(), iconSize, iconSize, true)
                ));

                tvAppName.setText(mPackage.getName());

                mobileUsageApp.setText(mPackage.getMobileData() + " mB");
                wiFiusageApp.setText(mPackage.getWiFiData() + " mB");

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


        initAppListM();



    }

    public void initAppListM(){

        Log.i(LOG_TAG, "initAppListM  ");

        LoadPackageList loadPackageList = new LoadPackageList(getContext(),this);
        loadPackageList.execute();

    }

    public void startUpdateAppTraffic() {

        final Handler uiHandler = new Handler();
        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {//функция для длительных задач(например работа с сетью)
                if(TrafficService.runTimer) {

                    initAppListM();
                }
                uiHandler.post(new Runnable() { //здесь можна выводить на экран и работать с основным потоком
                    @Override
                    public void run() {
                        if (TrafficService.runTimer) {


                        }
                    }
                });

            }
        }, 0L, 15L * 1000); // интервал - 3000 миллисекунд, 0 миллисекунд до первого запуска.
    }

    public void updateAdapterM(){

       // String s =


        if(sortMobile) {
            adapterApplicationsM.sort(new Comparator<Package>() {
                @Override
                public int compare(Package lhs, Package rhs) {
                    return (int) ((Float.parseFloat(rhs.getMobileData()) - Float.parseFloat(lhs.getMobileData())) * 10);
                }
            });
        }
        else{
            adapterApplicationsM.sort(new Comparator<Package>() {
                @Override
                public int compare(Package lhs, Package rhs) {
                    return (int) ((Float.parseFloat(rhs.getWiFiData()) - Float.parseFloat(lhs.getWiFiData())) * 10);
                }
            });
        }

        adapterApplicationsM.notifyDataSetChanged();
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

      try{
          SharedPreferences mySharedPreferences = getContext().getSharedPreferences(TrafficService.APP_PREFERENCES_TRAFFIC_APPS, Context.MODE_PRIVATE);
          //Log.i(LOG_TAG, "Fragment2 getTotalTraffic " + mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0));
          return mySharedPreferences.getLong(TrafficService.APP_PREFERENCES_TOTAL_TRAFFIC,0);
      }
       catch(Exception e){
            return  0;
        }
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

    public void saveTrafficAppsMobile(){

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Cursor c = db.query("traffic_app", null, null, null, null, null, null);

        if(adapterApplicationsM != null) {
            for (int i = 0; i < adapterApplicationsM.getCount(); i++) {
                Package item = adapterApplicationsM.getItem(i);

                if(!doesStationExist(db,item.getPackageName())){

                    cv.put("package_name", item.getPackageName());
                    cv.put("mobile_traffic", item.getMobileData());
                    cv.put("wifi_traffic", item.getWiFiData());

                    long rowID = db.insert("traffic_app", null, cv);
                    Log.d(LOG_TAG, "row inserted in traffic_app, ID = " + rowID);
                }
                else{

                    String strSQLupdate = "UPDATE traffic_app SET mobile_traffic = " + item.getMobileData() + " WHERE package_name = " +
                            "'" + item.getPackageName() + "'";

                    db.execSQL(strSQLupdate);
                }

                if (c.moveToFirst()) {

                    int package_nameColIndex = c.getColumnIndex("package_name");
                    int mobile_trafficColIndex = c.getColumnIndex("mobile_traffic");
                    int wifi_trafficColIndex = c.getColumnIndex("wifi_traffic");

                    do {

                        Log.d(LOG_TAG,"package_name = " + c.getString(package_nameColIndex) +
                                    ", mobile_traffic = " + c.getString(mobile_trafficColIndex) +
                                    ", wifi_traffic = " + c.getString(wifi_trafficColIndex));


                    } while (c.moveToNext());
                }
            }
        }

        c.close();

    } //сохраним траффик приложения

    public boolean doesStationExist(SQLiteDatabase db,String packageName){
      /*  final String query = "SELECT stationName FROM traffic_app WHERE packageName='"+packageName + "'";

            Cursor c = db.rawQuery(query, null);
            return c.moveToFirst();*/

            Cursor cursor = null;
            String checkQuery = "SELECT id FROM traffic_app  WHERE package_name" + "= '" + packageName + "'";
            cursor= db.rawQuery(checkQuery,null);
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            return exists;

    }

    public List<PackageInfo> getListApp (){

        ArrayList<PackageInfo> packageInfos = new ArrayList<PackageInfo>();


        final List<PackageInfo> apps = getContext().getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo packageInfo : apps) {

            if (packageInfo.requestedPermissions == null)
                continue;

            for (String permission : packageInfo.requestedPermissions) {

                if (TextUtils.equals(permission, android.Manifest.permission.INTERNET)) {
                    packageInfos.add(packageInfo);
                    break;
                }
            }
        }
        return packageInfos;
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

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);

        adapterApplicationsM.clear();
        initAppListM();

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onGetPackage(Package p) {


        if(p.isUseTraffic()){
            Log.i(LOG_TAG, "onGetPackage " +  p.getPackageName());

            adapterApplicationsM.add(p);
           // adapterApplicationsM.notifyDataSetChanged();
            updateAdapterM();
        }

    }

    @Override
    public void onFinishLoadpackage() {

        Log.i(LOG_TAG, "onFinishLoadpackage ");


        saveTrafficAppsMobile();

    }




}

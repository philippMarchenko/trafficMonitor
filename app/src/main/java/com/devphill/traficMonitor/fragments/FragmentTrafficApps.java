package com.devphill.traficMonitor.fragments;


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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;

import com.devphill.traficMonitor.ApplicationItem;
import com.devphill.traficMonitor.DBHelper;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.TrafficService;

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

    boolean runTimer = true;

    TextView kb;
    TextView tvDataUsageWiFi;
    TextView tvDataUsageMobile;
    TextView tvDataUsageTotal;
    ListView lvApplications;

    private long dataUsageTotalLast = 0;

    static ArrayAdapter<ApplicationItem> adapterApplications;

    public static BroadcastReceiver brAppsTrafficFragment;
    public static final String UPDATE_TRAFFIC_APPS = "update_traffic_apps";

    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, container, false);

        kb = (TextView) view.findViewById(R.id.kb);
        /*
        tvDataUsageWiFi = (TextView) view.findViewById(R.id.tvDataUsageWiFi);
        tvDataUsageMobile = (TextView) view.findViewById(R.id.tvDataUsageMobile);
        tvDataUsageTotal = (TextView) view.findViewById(R.id.tvDataUsageTotal);*/

        lvApplications = (ListView) view.findViewById(R.id.lvInstallApplication);

        //UpdateAdapter updateAdapter = new UpdateAdapter();
        //  updateAdapter.execute();

        initAdapter();


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

                long traffic = intent.getLongExtra("allTrafficMobile", 0);
                float trafficFloat = (float) traffic / 1024;
                trafficFloat = Math.round(trafficFloat * (float) 10.0) / (float) 10.0;
                kb.setText(trafficFloat + " Mb");
                Log.i(LOG_TAG, "updateAdapter, allTrafficMobile " + traffic);

            }
        };


        Log.i(LOG_TAG, "onCreateView fragment2 ");

        return view;

    }

    public void initAdapter() {

        adapterApplications = new ArrayAdapter<ApplicationItem>(getActivity(), R.layout.item_install_application) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ApplicationItem app = getItem(position);

                final View result;
                if (convertView == null) {
                    result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_install_application, parent, false);
                } else {
                    result = convertView;
                }

                TextView tvAppName = (TextView) result.findViewById(R.id.tvAppName);
                TextView tvAppTraffic = (TextView) result.findViewById(R.id.tvAppTraffic);

                // TODO: resize once
                final int iconSize = Math.round(48 * getResources().getDisplayMetrics().density);
                tvAppName.setCompoundDrawablesWithIntrinsicBounds(
                        //app.icon,
                        new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                                ((BitmapDrawable) app.getIcon(getActivity().getPackageManager())).getBitmap(), iconSize, iconSize, true)
                        ),
                        null, null, null
                );
                tvAppName.setText(app.getApplicationLabel(getActivity().getPackageManager()));
                if(app.getUsageKb() > 0)
                     tvAppTraffic.setText(Integer.toString(app.getUsageKb()) + " Kb");
                else
                    Log.i(LOG_TAG, "Траффик почемуто меньше ноля((");


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
        };


            int count = 0;
       // TODO: resize icon once
        for (ApplicationInfo app : getActivity().getPackageManager().getInstalledApplications(0)) {
            ApplicationItem item = ApplicationItem.create(app,getContext());
            if(item != null) {
                adapterApplications.add(item);
            }
            Log.i(LOG_TAG, "count = " + count++);
           }

    /*    for(int i = 0; adapterApplications.getCount() < 15; i ++){
            ApplicationItem item = ApplicationItem.create(listApp.get(i),getContext());
            if(item != null) {
                adapterApplications.add(item);
            }
        }*/
        Log.i(LOG_TAG, "adapterApplications.getCount = " + adapterApplications.getCount());
    }
    public boolean isConnectedMobile(){
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
    public void updateAdapter() {
        for (int i = 0, l = adapterApplications.getCount(); i < l; i++) {
            ApplicationItem app = adapterApplications.getItem(i);
            app.setMobilTraffic(isConnectedMobile());
            app.update();
        }

        adapterApplications.sort(new Comparator<ApplicationItem>() {
            @Override
            public int compare(ApplicationItem lhs, ApplicationItem rhs) {
                return (int)(rhs.getUsageKbInt() - lhs.getUsageKbInt());
            }
        });
        adapterApplications.notifyDataSetChanged();

    }
    public void onResume() {
        super.onResume();

        runTimer = true;
        getActivity().registerReceiver(brAppsTrafficFragment, new IntentFilter(FragmentTrafficApps.UPDATE_TRAFFIC_APPS));
        Log.i(LOG_TAG, "Fragment2 onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        runTimer = false;
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
        runTimer = false;
        Log.i(LOG_TAG, "Fragment2 onDestroy");
    }

 /*   @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final PackageInfoData app = (PackageInfoData) buttonView.getTag();
        if (app != null) {
            switch (buttonView.getId()) {

                case R.id.itemcheck_3g:
                    if (app.selected_3g != isChecked) {
                        app.selected_3g = isChecked;
                        MainActivity.dirty = true;
                        notifyDataSetChanged();
                    }
                    break;

            }
        }

    }*/

    /**
     * Small structure to hold an application info
     */
    public static final class PackageInfoData {
        /**
         * linux user id
         */
        public int uid;
        /**
         * application names belonging to this user id
         */
        public List<String> names;
        /**
         * rules saving & load
         **/
        public String pkgName;
        /**
         * indicates if this application is selected for wifi
         */
        public boolean selected_wifi;
        /**
         * indicates if this application is selected for 3g
         */
        public boolean selected_3g;
        /**
         * indicates if this application is selected for roam
         */
        public boolean selected_roam;
        /**
         * indicates if this application is selected for vpn
         */
        public boolean selected_vpn;
        /**
         * indicates if this application is selected for lan
         */
        public boolean selected_lan;
        /**
         * toString cache
         */
        public String tostr;
        /**
         * application info
         */
        public ApplicationInfo appinfo;
        /**
         * cached application icon
         */
        public Drawable cached_icon;
        /**
         * indicates if the icon has been loaded already
         */
        public boolean icon_loaded;

        /* install time */
        public long installTime;

        /**
         * first time seen?
         */
        public boolean firstseen;

        public PackageInfoData() {
        }

        public PackageInfoData(int uid, String name, String pkgNameStr) {
            this.uid = uid;
            this.names = new ArrayList<String>();
            this.names.add(name);
            this.pkgName = pkgNameStr;
        }

        public PackageInfoData(String user, String name, String pkgNameStr) {
            this(android.os.Process.getUidForName(user), name, pkgNameStr);
        }

        /**
         * Screen representation of this application
         */
        @Override
        public String toString() {
            if (tostr == null) {
                StringBuilder s = new StringBuilder();
                //if (uid > 0) s.append(uid + ": ");
                for (int i = 0; i < names.size(); i++) {
                    if (i != 0) s.append(", ");
                    s.append(names.get(i));
                }
                s.append("\n");
                tostr = s.toString();
            }
            return tostr;
        }

        public String toStringWithUID() {
            if (tostr == null) {
                StringBuilder s = new StringBuilder();
                s.append(uid + ": ");
                for (int i = 0; i < names.size(); i++) {
                    if (i != 0) s.append(", ");
                    s.append(names.get(i));
                }
                s.append("\n");
                tostr = s.toString();
            }
            return tostr;
        }

    }

}

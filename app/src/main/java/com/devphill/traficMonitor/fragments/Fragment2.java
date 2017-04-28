package com.devphill.traficMonitor.fragments;


import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//implements CompoundButton.OnCheckedChangeListener
public class Fragment2 extends Fragment {


    public  String LOG_TAG = "myLogs";

    DBHelper dbHelper;
    /// создаем объект для данных базы
    ContentValues cv = new ContentValues();
    Timer myTimer = new Timer(); // Создаем таймер

    boolean runTimer = true;

    TextView tvSupported;
    TextView tvDataUsageWiFi;
    TextView tvDataUsageMobile;
    TextView tvDataUsageTotal;
    ListView lvApplications;

    int year;
    int month;
    int day;

    private long dataUsageTotalLast = 0;

    static ArrayAdapter<ApplicationItem> adapterApplications;


    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, container, false);


        tvSupported = (TextView) view.findViewById(R.id.tvSupported);
        tvDataUsageWiFi = (TextView) view.findViewById(R.id.tvDataUsageWiFi);
        tvDataUsageMobile = (TextView) view.findViewById(R.id.tvDataUsageMobile);
        tvDataUsageTotal = (TextView) view.findViewById(R.id.tvDataUsageTotal);



   /*     if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED && TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED) {
            // handler.postDelayed(runnable, 0);
            task();

            initAdapter();
            lvApplications = (ListView) view.findViewById(R.id.lvInstallApplication);
            lvApplications.setAdapter(adapterApplications);
        } else {
            tvSupported.setVisibility(View.VISIBLE);
        }*/



            Log.i(LOG_TAG, "onCreateView fragment ");
         return view;

    }
    public void task (){
        final Handler uiHandler = new Handler();
        myTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {//функция для длительных задач(например работа с сетью)
                if(runTimer){

                }
                uiHandler.post(new Runnable() { //здесь можна выводить на экран и работать с основным потоком
                    @Override
                    public void run() {
                        if(runTimer){

                         Log.d(LOG_TAG, "Функция  iHandler.post");
                            long mobile = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
                            long total = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
                            tvDataUsageWiFi.setText("" + (total - mobile) / 1024 + " Kb");
                            tvDataUsageMobile.setText("" + mobile / 1024 + " Kb");
                            tvDataUsageTotal.setText("" + total / 1024 + " Kb");
                            if (dataUsageTotalLast != total) {
                                dataUsageTotalLast = total;
                                updateAdapter();
                            }

                            Calendar calendar = Calendar.getInstance();
                            year = calendar.get(Calendar.YEAR);
                            month= calendar.get(Calendar.MONTH);
                            day = calendar.get(Calendar.DAY_OF_MONTH);

                     }
                    }
                });

            };

        }, 0L, 8L * 1000); // интервал - 8000 миллисекунд, 0 миллисекунд до первого запуска.
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
                final int iconSize = Math.round(32 * getResources().getDisplayMetrics().density);
                tvAppName.setCompoundDrawablesWithIntrinsicBounds(
                        //app.icon,
                        new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(
                                ((BitmapDrawable) app.getIcon(getActivity().getPackageManager())).getBitmap(), iconSize, iconSize, true)
                        ),
                        null, null, null
                );
                tvAppName.setText(app.getApplicationLabel(getActivity().getPackageManager()));
                tvAppTraffic.setText(Integer.toString(app.getTotalUsageKb()) + " Kb");

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

        // TODO: resize icon once
        for (ApplicationInfo app : getActivity().getPackageManager().getInstalledApplications(0)) {
            ApplicationItem item = ApplicationItem.create(app);
            if(item != null) {
                adapterApplications.add(item);
            }
        }
    }
    public void updateAdapter() {
        for (int i = 0, l = adapterApplications.getCount(); i < l; i++) {
            ApplicationItem app = adapterApplications.getItem(i);
            app.update();
        }

        adapterApplications.sort(new Comparator<ApplicationItem>() {
            @Override
            public int compare(ApplicationItem lhs, ApplicationItem rhs) {
                return (int)(rhs.getTotalUsageKb() - lhs.getTotalUsageKb());
            }
        });
        adapterApplications.notifyDataSetChanged();
    }
    public void onResume() {
        super.onResume();
        runTimer = true;

        Log.i(LOG_TAG, "Fragment2 onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        runTimer = false;
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

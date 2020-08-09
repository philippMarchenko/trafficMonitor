package com.devphill.traficMonitor.ui.fragments.app_traffic;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.devphill.traficMonitor.model.ApplicationItem;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.model.Package;
import com.devphill.traficMonitor.networkStats.LoadPackageList;
import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.ui.fragments.app_traffic.helper.AppsTrafficHelper;
import com.devphill.traficMonitor.ui.fragments.app_traffic.helper.AppsTrafficHelperM;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentTrafficApps extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoadPackageList.ILoadPackageListListener{


    public  String LOG_TAG = "fragmentTrafficApps";

    @BindView(R.id.tvDataUsageWiFi)
    TextView tvDataUsageWiFi;
    @BindView(R.id.tvDataUsageMobile)
    TextView tvDataUsageMobile;
    @BindView(R.id.tvDataUsageTotal)
    TextView tvDataUsageTotal;

    RecyclerView recyclerViewApplications;


    static ArrayAdapter<ApplicationItem> adapterApplications ;

    private SwipeRefreshLayout swipeRefreshLayout;

    public static BroadcastReceiver brAppsTrafficFragment;
    public static final String UPDATE_TRAFFIC_APPS = "update_traffic_apps";

    public static boolean sortMobile = true;

    AppsTrafficHelper appsTrafficHelper;
    AppsTrafficHelperM appsTrafficHelperM;
    LoadPackageList loadPackageList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_traff, container, false);

        ButterKnife.bind(this,view);

        appsTrafficHelper = new AppsTrafficHelper(getContext(),tvDataUsageWiFi,tvDataUsageMobile,tvDataUsageTotal);

        recyclerViewApplications = (RecyclerView) view.findViewById(R.id.rvInstallApplication);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerViewApplications.setLayoutManager(mLayoutManager);
        recyclerViewApplications.setItemAnimator(new DefaultItemAnimator());

        appsTrafficHelperM = new AppsTrafficHelperM(getContext(),tvDataUsageWiFi,tvDataUsageMobile,tvDataUsageTotal);

        recyclerViewApplications.setAdapter(appsTrafficHelperM.getAppTrafficAdapter());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(),R.color.accent));

        swipeRefreshLayout.setOnRefreshListener(this);


        appsTrafficHelperM.showAllTrafficM();


        initAppList();
        return view;

    }


    public void initAppList(){

        if(TrafficService.packageList.isEmpty()){
            swipeRefreshLayout.setRefreshing(true);
            loadPackageList = new LoadPackageList(requireContext(),this);
            loadPackageList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else{
            appsTrafficHelperM.initAppListM(null);
        }


    } //обновляем траффик в БД по приложениям


    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(brAppsTrafficFragment, new IntentFilter(FragmentTrafficApps.UPDATE_TRAFFIC_APPS));
    }
    @Override

    public void onPause() {
        super.onPause();

        try {
            getActivity().unregisterReceiver(brAppsTrafficFragment);
        }catch (IllegalArgumentException e){
            Log.d(LOG_TAG, "Error unregisterReceiver in Fragment2" + e.getMessage());
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //saveAppTraffic();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    public void onRefresh() {

        swipeRefreshLayout.setRefreshing(true);
        Log.i(LOG_TAG, "onRefresh ");
        TrafficService.packageList.clear();
        loadPackageList = new LoadPackageList(requireContext(),this);
        loadPackageList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void allPackagesLoaded() {
        Log.i(LOG_TAG, "allPackagesLoaded ");

        appsTrafficHelperM.initAppListM(loadPackageList.getPackages());
        swipeRefreshLayout.setRefreshing(false);
    }
}

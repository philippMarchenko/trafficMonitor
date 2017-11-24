package com.devphill.traficMonitor.ui.fragments.app_traffic.helper;

import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.devphill.traficMonitor.adapter.AppTrafficAdapter;
import com.devphill.traficMonitor.model.Package;
import com.devphill.traficMonitor.model.Traffic;
import com.devphill.traficMonitor.networkStats.NetworkStatsHelper;
import com.devphill.traficMonitor.service.TrafficService;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class AppsTrafficHelperM{

    public  String LOG_TAG = "AppsTrafficHelperM";

    private Context context;

    private TextView tvDataUsageWiFi;
    private TextView tvDataUsageMobile;
    private TextView tvDataUsageTotal;



    private AppTrafficAdapter appTrafficAdapter;
    private List<Package> packageList = new ArrayList<>();

    public AppsTrafficHelperM(Context context, TextView tvDataUsageWiFi, TextView tvDataUsageMobile, TextView tvDataUsageTotal) {

        this.context = context;
        this.tvDataUsageWiFi = tvDataUsageWiFi;
        this.tvDataUsageMobile = tvDataUsageMobile;
        this.tvDataUsageTotal = tvDataUsageTotal;

        appTrafficAdapter = new AppTrafficAdapter(context,packageList);
    }

    public void showAllTrafficM(){

        getObrervableTraffic().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Traffic>() {
                    @Override
                    public void onNext(Traffic traffic) {

                        Log.d(LOG_TAG,"onNext  getObrervableTraffic ");

                        tvDataUsageTotal.setText(Float.toString(traffic.getAllData()) + " Mb");



                        tvDataUsageMobile.setText(Float.toString(traffic.getMobileData()) + " Mb");


                        tvDataUsageWiFi.setText(Float.toString(traffic.getWiFiData()) + " Mb");

                    }

                    @Override
                    public void onError(Throwable e) {
                        //Handle error
                    }

                    @Override
                    public void onComplete() {
                        Log.d(LOG_TAG,"Load file finish! ");

                    }
                });




    }

    public void initAppListM(){

        packageList.clear();
        packageList.addAll(TrafficService.packageList);
        appTrafficAdapter.updateAdapter();
        appTrafficAdapter.notifyDataSetChanged();


    } //обновляем траффик в БД по приложениям

    public Observable getObrervableTraffic(){

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);

        final NetworkStatsHelper networkStatsHelper = new NetworkStatsHelper(networkStatsManager);

        Observable obrervableTraffic = Observable.create(new ObservableOnSubscribe<Traffic>() {
            @Override
            public void subscribe(final ObservableEmitter<Traffic> emitter) throws Exception {

                Log.d(LOG_TAG,"init obrervableTraffic... ");

                Traffic traffic = new Traffic();

                float allData = ((float) (networkStatsHelper.getAllRxBytesMobile(context)
                        + networkStatsHelper.getAllTxBytesMobile(context)
                        + networkStatsHelper.getAllRxBytesWifi()
                        + networkStatsHelper.getAllTxBytesWifi()) / ((float)1024*(float)1024));

                allData = Math.round(allData * (float) 10.0) / (float) 10.0;

                float mobileData = ((float) (networkStatsHelper.getAllRxBytesMobile(context)
                        + networkStatsHelper.getAllTxBytesMobile(context)) / ((float)1024*(float)1024));

                mobileData = Math.round(mobileData * (float) 10.0) / (float) 10.0;

                float wiFiData = ((float) (networkStatsHelper.getAllRxBytesWifi()
                        + networkStatsHelper.getAllTxBytesWifi()) / ((float)1024*(float)1024));

                wiFiData = Math.round(wiFiData * (float) 10.0) / (float) 10.0;


                traffic.setAllData(allData);

                traffic.setWiFiData(wiFiData);

                traffic.setMobileData(mobileData);

             //   emitter.onComplete();
                emitter.onNext(traffic);
                 //   emitter.onError();

            }
        });



        return obrervableTraffic;
    }

    public AppTrafficAdapter getAppTrafficAdapter() {
        return appTrafficAdapter;
    }
}
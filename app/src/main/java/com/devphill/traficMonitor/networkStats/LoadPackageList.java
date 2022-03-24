package com.devphill.traficMonitor.networkStats;


import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.devphill.traficMonitor.model.Package;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class LoadPackageList extends AsyncTask<Void, Package,Void> {

    private Context mContext;
    private ILoadPackageListListener mILoadPackageListListener;
    public List<Package> packageList = new ArrayList<>();

    public interface ILoadPackageListListener {
        void allPackagesLoaded();
    }

    public LoadPackageList (Context context,ILoadPackageListListener iLoadPackageListListener){

        mILoadPackageListListener = iLoadPackageListListener;
        mContext = context;
    }


    public  List<Package> getPackages(){
        return packageList;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        Timber.d("doInBackground  " );

     //   try{

            PackageManager packageManager = mContext.getPackageManager();

            List<PackageInfo> packageInfoList = getListApp(packageManager);     //получаем список приложений с интернетом

            NetworkStatsManager networkStatsManager = (NetworkStatsManager) mContext.getSystemService(Context.NETWORK_STATS_SERVICE);

            NetworkStatsHelper networkStatsHelper;

            for (int i = 0;  i < packageInfoList.size(); i++) {

                Package packageItem = new Package();
                PackageInfo packageInfo = packageInfoList.get(i);

                int uid = PackageManagerHelper.getPackageUid(mContext, packageInfo.packageName);
                networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid,packageInfo.packageName);

                if(networkStatsHelper.getDataMobile(mContext) > 1048576){                              //more than 1 MB
                    Timber.d("add  packageItem");             // 253 мс

                    packageItem.setVersion(packageInfo.versionName);
                    packageItem.setPackageName(packageInfo.packageName);    //1240 мс

                    ApplicationInfo ai = null;
                    try {
                        ai = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                    } catch (PackageManager.NameNotFoundException e) {
                        Timber.d("PackageManager.NameNotFoundException e " + e.getMessage());             // 253 мс

                        e.printStackTrace();
                    }
                    if (ai == null) {
                        continue;
                    }
                    CharSequence appName = packageManager.getApplicationLabel(ai);
                    if (appName != null) {
                        packageItem.setName(appName.toString());
                    }                                                               //2950 мс

                    try {
                        packageItem.setIcon(packageManager.getApplicationIcon(packageInfo.packageName));
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    float trafficWiFi = (float) (networkStatsHelper.getDataWiFi(mContext) / 1048576);
                    trafficWiFi = Math.round(trafficWiFi * (float) 10.0) / (float) 10.0;

                    packageItem.setWiFiData(trafficWiFi);

                    float trafficMobile = (float) (networkStatsHelper.getDataMobile(mContext) / 1048576) ;
                    trafficMobile = Math.round(trafficMobile * (float) 10.0) / (float) 10.0;
                    Timber.d("networkStatsHelper.getDataMobile(mContext)  " + networkStatsHelper.getDataMobile(mContext));

                    packageItem.setMobileData(trafficMobile);


                    if(packageItem.isUseTraffic()){
                        Timber.d("add  " + packageInfo.packageName);

                        packageList.add(packageItem);
                    }
                }


                Timber.d("packageList  " + packageList.size());


            }
            publishProgress();

    /*    }
        catch (Exception e){
            Timber.d("Exception  " + e.getMessage());

        }*/

        return null;
    }

    @Override
    protected void onProgressUpdate(Package... p) {
        super.onProgressUpdate(p);
        Timber.d("onProgressUpdate");
        mILoadPackageListListener.allPackagesLoaded();

    }

    public List<PackageInfo> getListApp (PackageManager packageManager){

        ArrayList<PackageInfo> packageInfos = new ArrayList<PackageInfo>();


        final List<PackageInfo> apps = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

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

}
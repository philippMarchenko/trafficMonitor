package com.devphill.traficMonitor.networkStats;


import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.devphill.traficMonitor.model.Package;

import java.util.ArrayList;
import java.util.List;

public class LoadPackageList extends AsyncTask<Void, Package,Void> {

    public  String LOG_TAG = "LoadPackageListTag";
    private Context mContext;
    private ILoadPackageListListener mILoadPackageListListener;
    List<Package> packageList = new ArrayList<>();

    public interface ILoadPackageListListener {
        public void onGetPackage(Package p);
       // public void onFinishLoadpackage(List<Package> packageList);
    }

    public LoadPackageList (Context context,ILoadPackageListListener iLoadPackageListListener){

        mILoadPackageListListener = iLoadPackageListListener;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try{

            PackageManager packageManager = mContext.getPackageManager();

            List<PackageInfo> packageInfoList = getListApp(packageManager);     //получаем список приложений с интернетом

            NetworkStatsManager networkStatsManager = (NetworkStatsManager) mContext.getSystemService(Context.NETWORK_STATS_SERVICE);

            NetworkStatsHelper networkStatsHelper;

            for (int i = 0;  i < packageInfoList.size(); i++) {

                Package packageItem = new Package();
                PackageInfo packageInfo = packageInfoList.get(i);

                int uid = PackageManagerHelper.getPackageUid(mContext, packageInfo.packageName);
                networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid,packageInfo.packageName);

                Log.i(LOG_TAG, "add  packageItem");             // 253 мс

                packageItem.setVersion(packageInfo.versionName);
                packageItem.setPackageName(packageInfo.packageName);    //1240 мс

                ApplicationInfo ai = null;
                try {
                    ai = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
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

                    packageItem.setIcon(((BitmapDrawable)packageManager.getApplicationIcon(packageInfo.packageName)).getBitmap());
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                float trafficWiFi = (float) (networkStatsHelper.getDataWiFi(mContext) / ((float)1024*(float)1024));
                trafficWiFi = Math.round(trafficWiFi * (float) 10.0) / (float) 10.0;

                packageItem.setWiFiData(Float.toString(trafficWiFi));

                float trafficMobile = (float) (networkStatsHelper.getDataMobile(mContext) /((float)1024*(float)1024)) ;
                trafficMobile = Math.round(trafficMobile * (float) 10.0) / (float) 10.0;

                packageItem.setMobileData(Float.toString(trafficMobile));
               //  packageItem.setMobileData("1458");

                Log.i(LOG_TAG, "setWiFiData  " + Float.toString(trafficWiFi));
               // Log.i(LOG_TAG, "setMobileData  " + Float.toString(trafficMobile));
                Log.i(LOG_TAG, "packageName  " + packageInfo.packageName);

                publishProgress(packageItem);

                packageList.add(packageItem);

            }
          //  mILoadPackageListListener.onFinishLoadpackage(packageList);

        }
        catch (Exception e){

        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Package... p) {
        super.onProgressUpdate(p);

        mILoadPackageListListener.onGetPackage(p[0]);
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
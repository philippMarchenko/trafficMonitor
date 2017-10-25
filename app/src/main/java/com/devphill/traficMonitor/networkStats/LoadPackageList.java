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

    public interface ILoadPackageListListener {
        public void onGetPackage(Package p);
    }

    public LoadPackageList (Context context,ILoadPackageListListener iLoadPackageListListener){

        mILoadPackageListListener = iLoadPackageListListener;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Bitmap bitmapIcon;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bitmapIcon =  Bitmap.createBitmap(100, 100, conf); // this creates a MUTABLE bitmap;

        PackageManager packageManager = mContext.getPackageManager();

        List<PackageInfo> packageInfoList = getListApp(packageManager);

      //  List<Package> packageList = new ArrayList<>(packageInfoList.size());
        NetworkStatsManager networkStatsManager = (NetworkStatsManager) mContext.getSystemService(Context.NETWORK_STATS_SERVICE);

        NetworkStatsHelper networkStatsHelper;

        for (int i = 0;  i < packageInfoList.size(); i++) {

            Package packageItem = new Package();
            PackageInfo packageInfo = packageInfoList.get(i);

            int uid = PackageManagerHelper.getPackageUid(mContext, packageInfo.packageName);
            networkStatsHelper = new NetworkStatsHelper(networkStatsManager, uid);

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
                bitmapIcon = ((BitmapDrawable)packageManager.getApplicationIcon(packageInfo.packageName)).getBitmap();

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            packageItem.setIcon(bitmapIcon);                //4079 мс

            packageItem.setWiFiRx(networkStatsHelper.getPackageRxBytesWifi());
        //    packageItem.setWiFiTx(networkStatsHelper.getPackageTxBytesWifi());

          //  packageItem.setMobileRx(networkStatsHelper.getPackageRxBytesMobile(mContext));
            //packageItem.setMobileTx(networkStatsHelper.getPackageTxBytesMobile(mContext));  //47000



            Log.i(LOG_TAG, "packageName  " + packageInfo.packageName);
            Log.i(LOG_TAG, "MobileRx  " + networkStatsHelper.getPackageRxBytesMobile(mContext));

            publishProgress(packageItem);

         //   packageList.add(packageItem);

        }

        //Log.i(LOG_TAG, "size " + packageList.size());

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
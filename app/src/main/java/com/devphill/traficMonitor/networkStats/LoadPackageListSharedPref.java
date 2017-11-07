package com.devphill.traficMonitor.networkStats;


import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.devphill.traficMonitor.model.Package;
import com.devphill.traficMonitor.ui.fragments.FragmentTrafficApps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadPackageListSharedPref extends AsyncTask<Void, Package,Void> {

    public  String LOG_TAG = "LoadPackageListTag";
    private Context mContext;
    private ILoadPackageListSharedPrefListener mILoadPackageListListener;

    public interface ILoadPackageListSharedPrefListener {
        public void onGetPackageSharedPref(Package p);
        public void onFinishLoadpackageSharedPref();
    }

    public LoadPackageListSharedPref (Context context,ILoadPackageListSharedPrefListener iLoadPackageListListener){

        mILoadPackageListListener = iLoadPackageListListener;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Bitmap bitmapIcon;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bitmapIcon =  Bitmap.createBitmap(100, 100, conf); // this creates a MUTABLE bitmap;

        PackageManager packageManager = mContext.getPackageManager();

        SharedPreferences mySharedPreferencesMobile = mContext.getSharedPreferences(FragmentTrafficApps.APP_PREFERENCES_TRAFFIC_APPS_M, Context.MODE_PRIVATE);

        Map<String, ?> allEntries = mySharedPreferencesMobile.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {

            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());

            Package packageItem = new Package();

            String packageName = entry.getKey();

            if(packageName.equals("_mobile")){

                packageName = packageName.replaceAll("_mobile", "");
            }
            else{
                continue;
            }



            String mobileData =  entry.getValue().toString();
            String nameApp = "";

            packageItem.setMobileData(mobileData);
            packageItem.setWiFiData("888888");
            ApplicationInfo ai = null;
            try {
                ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (ai == null) {
                continue;
            }
            CharSequence appName = packageManager.getApplicationLabel(ai);
            if (appName != null) {
                packageItem.setName(appName.toString());
            }

            try {
                bitmapIcon = ((BitmapDrawable)packageManager.getApplicationIcon(packageName)).getBitmap();

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            packageItem.setIcon(bitmapIcon);


            publishProgress(packageItem);

        }

        for (int i = 0;  i < allEntries.size(); i++) {


          //  String packageName = allEntries.entrySet();



            Log.i(LOG_TAG, "add  packageItem");             // 253 мс




           /* packageItem.setVersion(packageInfo.versionName);
            packageItem.setPackageName(mySharedPreferences.getpackageName);    //1240 мс*/


                                                                     //2950 мс






          //  packageItem.setMobileData(mySharedPreferences.getFloat(packageInfoList.get(i).packageName,0));

            float data = (float) 123.888;

           // packageItem.setWiFiData(data);

            // packageItem.setWiFiRx(networkStatsHelper.getPackageRxBytesWifi());
           // packageItem.setWiFiTx(networkStatsHelper.getPackageTxBytesWifi());

           // packageItem.setMobileRx(networkStatsHelper.getPackageRxBytesMobile(mContext));
           // packageItem.setMobileTx(networkStatsHelper.getPackageTxBytesMobile(mContext));  //47000



            //   packageList.add(packageItem);

        }
        mILoadPackageListListener.onFinishLoadpackageSharedPref();
        //Log.i(LOG_TAG, "size " + packageList.size());

        return null;
    }

    @Override
    protected void onProgressUpdate(Package... p) {
        super.onProgressUpdate(p);

        mILoadPackageListListener.onGetPackageSharedPref(p[0]);
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
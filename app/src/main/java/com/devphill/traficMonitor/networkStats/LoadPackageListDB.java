package com.devphill.traficMonitor.networkStats;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.model.Package;

public class LoadPackageListDB extends AsyncTask<Void, Package,Void> {

    public  String LOG_TAG = "LoadPackageListTag";
    private Context mContext;
    private ILoadPackageListDB iLoadPackageListDB;
    DBHelper dbHelper;

    public interface ILoadPackageListDB {
         void onGetPackageDB(Package p);
         void onFinishLoadpackageDB();
    }

    public LoadPackageListDB(Context context, ILoadPackageListDB iLoadPackageListDB){

        dbHelper = new DBHelper(context);
        this.iLoadPackageListDB = iLoadPackageListDB;
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        PackageManager packageManager = mContext.getPackageManager();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("traffic_app", null, null, null, null, null, null);

        if (c.moveToFirst()) {

            int package_nameColIndex = c.getColumnIndex("package_name");
            int mobile_trafficColIndex = c.getColumnIndex("mobile_traffic");
            int wifi_trafficColIndex = c.getColumnIndex("wifi_traffic");

            do {

                Log.d(LOG_TAG, "package_name = " + c.getString(package_nameColIndex) +
                        ", mobile_traffic = " + c.getString(mobile_trafficColIndex) +
                        ", wifi_traffic = " + c.getString(wifi_trafficColIndex));

                Package packageItem = new Package();

                packageItem.setPackageName(c.getString(package_nameColIndex));
                packageItem.setMobileData(c.getString(mobile_trafficColIndex));
                packageItem.setWiFiData(c.getString(wifi_trafficColIndex));

                ApplicationInfo ai = null;
                try {
                    ai = packageManager.getApplicationInfo(c.getString(package_nameColIndex), PackageManager.GET_META_DATA);
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

                    packageItem.setIcon(((BitmapDrawable) packageManager.getApplicationIcon(c.getString(package_nameColIndex))).getBitmap());

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }


                publishProgress(packageItem);

            } while (c.moveToNext());
        }

        c.close();

        iLoadPackageListDB.onFinishLoadpackageDB();

        return null;
    }

    @Override
    protected void onProgressUpdate(Package... p) {
        super.onProgressUpdate(p);

        iLoadPackageListDB.onGetPackageDB(p[0]);
    }

}
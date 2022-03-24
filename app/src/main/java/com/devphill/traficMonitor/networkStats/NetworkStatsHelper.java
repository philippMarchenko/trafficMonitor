package com.devphill.traficMonitor.networkStats;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Robert Zagórski on 2016-09-09.
 */
public class NetworkStatsHelper {

    NetworkStatsManager networkStatsManager;
    int packageUid;
    public String LOG_TAG = "NetworkStatsHelperTag";

    Date date;
    Date currentdate;
    Calendar night = Calendar.getInstance();

    String packageName;

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager) {

        this.networkStatsManager = networkStatsManager;
        currentdate = new Date();
        date = new Date();
        date.setDate(currentdate.getDate());
        date.setMonth(currentdate.getMonth());
        date.setYear(currentdate.getYear());
        date.setHours(0);
        date.setMinutes(0);

    }

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager, int packageUid, String packageName) {

        this.networkStatsManager = networkStatsManager;
        this.packageUid = packageUid;
        currentdate = new Date();
        date = new Date();
        date.setDate(currentdate.getDate());
        date.setMonth(currentdate.getMonth());
        date.setYear(currentdate.getYear());
        date.setHours(0);
        date.setMinutes(0);

        night.setTime(date);

        this.packageName = packageName;
    }

    public long getAllRxBytesMobile(Context context) {
        NetworkStats.Bucket bucket = null;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context),
                    date.getTime(),
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e) {
            Log.i(LOG_TAG, "Не удалось получить данные траффика " + e.getMessage());
        }
        return bucket.getRxBytes();
    }

    public long getAllTxBytesMobile(Context context) {
        NetworkStats.Bucket bucket = null;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context),
                    date.getTime(),
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e) {
            Log.i(LOG_TAG, "Не удалось получить данные траффика " + e.getMessage());
        }
        return bucket.getTxBytes();
    }

    public long getAllRxBytesWifi() {


        NetworkStats.Bucket bucket = null;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                    "",
                    date.getTime(),
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e) {
            Log.i(LOG_TAG, "Не удалось получить данные траффика " + e.getMessage());
        }
        return bucket.getRxBytes();
    }

    public long getAllTxBytesWifi() {

        NetworkStats.Bucket bucket = null;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI,
                    "",
                    date.getTime(),
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        } catch (NullPointerException e) {
            Log.i(LOG_TAG, "Не удалось получить данные траффика " + e.getMessage());
        }
        return bucket.getTxBytes();
    }

    public String getSubscriberId(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            else{
                return telephonyManager.getSubscriberId();
            }
        } else {
            return null;
        }
    }

    public long getDataMobile(Context context){

        return getMobileUsage(ConnectivityManager.TYPE_MOBILE,getSubscriberId(context));
    }

    private long getMobileUsage(int networkType, String subScriberId) {
        NetworkStats networkStatsByApp;
        long currentUsage = 0L;
        try {
            networkStatsByApp = networkStatsManager.querySummary(networkType, subScriberId, date.getTime(), System.currentTimeMillis());
            do {
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                networkStatsByApp.getNextBucket(bucket);
                if (bucket.getUid() == packageUid) {
                    currentUsage = (bucket.getRxBytes() + bucket.getTxBytes());
                }
            } while (networkStatsByApp.hasNextBucket());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        catch(NullPointerException e){
            Log.i(LOG_TAG, "Не удалось получить данные траффика " +  e.getMessage());
        }

        return currentUsage;
    }

    public long getDataWiFi(Context context){

        return getWiFiUsage(ConnectivityManager.TYPE_WIFI,getSubscriberId(context));
    }

    private long getWiFiUsage(int networkType, String subScriberId) {
        NetworkStats networkStatsByApp;
        long currentUsage = 0L;
        try {
            networkStatsByApp = networkStatsManager.querySummary(networkType, subScriberId, date.getTime(), System.currentTimeMillis());
            do {
                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                networkStatsByApp.getNextBucket(bucket);
                if (bucket.getUid() == packageUid) {
                    currentUsage = (bucket.getRxBytes() + bucket.getTxBytes());
                }
            } while (networkStatsByApp.hasNextBucket());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        catch(NullPointerException e){
            Log.i(LOG_TAG, "Не удалось получить данные траффика " +  e.getMessage());
        }

        return currentUsage;
    }

}

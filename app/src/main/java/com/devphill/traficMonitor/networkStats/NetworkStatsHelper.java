package com.devphill.traficMonitor.networkStats;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Robert Zagórski on 2016-09-09.
 */
@TargetApi(Build.VERSION_CODES.M)
public class NetworkStatsHelper {

    NetworkStatsManager networkStatsManager;
    int packageUid;
    public  String LOG_TAG = "NetworkStatsHelperTag";

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

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager, int packageUid,String packageName) {

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
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    date.getTime(),
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        catch(NullPointerException e){
            Log.i(LOG_TAG, "Не удалось получить данные траффика " +  e.getMessage());
        }
        return bucket.getRxBytes();
    }

    public long getAllTxBytesMobile(Context context) {
        NetworkStats.Bucket bucket = null;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    date.getTime(),
                    System.currentTimeMillis());
        } catch (RemoteException e) {
            return -1;
        }
        catch(NullPointerException e){
            Log.i(LOG_TAG, "Не удалось получить данные траффика " +  e.getMessage());
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
        }
        catch(NullPointerException e){
            Log.i(LOG_TAG, "Не удалось получить данные траффика " +  e.getMessage());
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
        }
        catch(NullPointerException e){
            Log.i(LOG_TAG, "Не удалось получить данные траффика " +  e.getMessage());
        }
        return bucket.getTxBytes();
    }

 /*   public long getPackageRxBytesMobile(Context context) {

        Date currentdate = new Date(System.currentTimeMillis());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

        String strTime = simpleDateFormat.format(date.getTime());
        Log.i(LOG_TAG, "start date " +  strTime);
        Log.i(LOG_TAG, "start ms " +  date.getTime());
        strTime = simpleDateFormat.format(currentdate.getTime());
        Log.i(LOG_TAG, "end date " + strTime);
        Log.i(LOG_TAG, "end ms " + currentdate.getTime());
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    night.getTimeInMillis(),
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        catch(NullPointerException e){
            Log.i(LOG_TAG, "Не удалось получить данные траффика " +  e.getMessage());
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);

        strTime = simpleDateFormat.format(bucket.getStartTimeStamp());
        Log.i(LOG_TAG, "getStartTimeStamp " +  strTime);
        strTime = simpleDateFormat.format(bucket.getEndTimeStamp());

        Log.i(LOG_TAG, "getEndTimeStamp " + strTime);
        return bucket.getRxBytes();
    }*/
/*
    public long getPackageTxBytesMobile(Context context) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    night.getTimeInMillis(),
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }*/

   /* public long getPackageRxBytesWifi() {

        Date currentdate = new Date(System.currentTimeMillis());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

        Log.i(LOG_TAG, "getPackageRxBytesWifi  ");
        Log.i(LOG_TAG, "packageName " +  packageName);


        String strTime = simpleDateFormat.format( night.getTimeInMillis());
        Log.i(LOG_TAG, "start date " +  strTime);
        Log.i(LOG_TAG, "start ms " +   night.getTimeInMillis());
        strTime = simpleDateFormat.format(currentdate.getTime());
        Log.i(LOG_TAG, "end date " + strTime);
        Log.i(LOG_TAG, "end ms " + currentdate.getTime());

        NetworkStats networkStats = null;

        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_WIFI,
                    "",
                    night.getTimeInMillis(),
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);

        Log.i(LOG_TAG, "getRxBytes " + bucket.getRxBytes());
        return bucket.getRxBytes();
    }*/

/*    public long getPackageTxBytesWifi() {

        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_WIFI,
                    "",
                    night.getTimeInMillis(),
                    System.currentTimeMillis(),
                    packageUid);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }*/

    private String getSubscriberId(Context context, int networkType) {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }

    public long getDataMobile(Context context){

        return getMobileUsage(ConnectivityManager.TYPE_MOBILE,getSubscriberId(context,ConnectivityManager.TYPE_MOBILE));
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

        return getWiFiUsage(ConnectivityManager.TYPE_WIFI,getSubscriberId(context,ConnectivityManager.TYPE_MOBILE));
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

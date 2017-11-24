package com.devphill.traficMonitor.ui.fragments.test_speed.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.devphill.traficMonitor.util.Util;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class StartTestPing  {


    private Ping ping;
    private Context context;

    public StartTestPing(Context context){

        this.context = context;
    }


    public Observable getPing(){

        Observable obrervablePing = Observable.create(new ObservableOnSubscribe<Ping>() {
            @Override
            public void subscribe(final ObservableEmitter<Ping> emitter) throws Exception {

                ping =  getPing(new URL("https://www.google.com:443/"), context);

                emitter.onNext(ping);

            }
        });

        return obrervablePing;
    }



    public  Ping getPing (URL url, Context ctx) {
            Ping r = new Ping();
            if (Util.isNetworkConnected(ctx)) {
                r.net = Util.getNetworkType(ctx);
                try {
                    String hostAddress;
                    long start = System.currentTimeMillis();
                    hostAddress = InetAddress.getByName(url.getHost()).getHostAddress();
                    long dnsResolved = System.currentTimeMillis();
                    Socket socket = new Socket(hostAddress, url.getPort());
                    socket.close();
                    long probeFinish = System.currentTimeMillis();
                    r.dns = (int) (dnsResolved - start);
                    r.cnt = (int) (probeFinish - dnsResolved);
                    r.host = url.getHost();
                    r.ip = hostAddress;
                }
                catch (Exception e) {
                }
            }
            return r;
        }


}

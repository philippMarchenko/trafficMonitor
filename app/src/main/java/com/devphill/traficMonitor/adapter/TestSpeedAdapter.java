package com.devphill.traficMonitor.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devphill.traficMonitor.R;
import com.github.anastr.speedviewlib.base.Gauge;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class TestSpeedAdapter extends RecyclerView.Adapter<TestSpeedAdapter.ViewHolder> {

    private String[] mDataSet;
    private int[] mDataSetTypes;
    private Context mContext;
    private Activity myActivity;

    boolean testSpeed = false;

    public  String LOG_TAG = "myLogsTestSpeed";

    public static final int CARD_1 = 0;
    public static final int CARD_2 = 1;
    public static final int CARD_3 = 2;
    public static final int CARD_4 = 3;

    public static final int STATUS_DOWNLOAD = 1;
    public static final int STATUS_UPLOAD = 2;
    public static final int STATUS_FINISH = 0;

    public int status_du = 0;
    public int count = 0;
    Handler handlerViewProgress;
    Handler handlerPing;
    Handler handlerDownloadSpeed;
    Handler handlerUploadSpeed;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public TestSpeedAdapter(Context context, Activity activity, String[]dataSet, int[] dataSetTypes) {
        mContext = context;
        mDataSet = dataSet;
        mDataSetTypes = dataSetTypes;
        myActivity = activity;

        Log.i(LOG_TAG, "TestSpeedAdapter");
    }
    public class PingHolder extends ViewHolder {

        TextView pingView;

        public PingHolder(View v) {
            super(v);
            this.pingView = (TextView) v.findViewById(R.id.pingView);

           handlerPing = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    Bundle bundle = msg.getData();
                       if(bundle.getInt("ping") > 0)
                          pingView.setText(Integer.toString(bundle.getInt("ping")));

                }
            };
        }

    }
    public class TestDownloadHolder extends ViewHolder {

        TextView downloadSpeed;

        public TestDownloadHolder(View v) {
            super(v);
            this.downloadSpeed = (TextView) v.findViewById(R.id.downloadSpeed);

            handlerDownloadSpeed = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    float speedF = Math.round(bundle.getFloat("speed")/1000000*(float)100.0)/(float)100.0;
                    downloadSpeed.setText(Float.toString(speedF));

                }
            };
        }
    }
    public class TestUploadHolder extends ViewHolder {

        TextView uploadSpeed;

        public TestUploadHolder(View v) {
            super(v);
            this.uploadSpeed = (TextView) v.findViewById(R.id.uploadSpeed);

            handlerUploadSpeed = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Bundle bundle = msg.getData();
                    float speedF = Math.round(bundle.getFloat("speed")/1000000*(float)100.0)/(float)100.0;
                    uploadSpeed.setText(Float.toString(speedF));

                }
            };
        }
    }
    public class ViewProgressTestHolder extends ViewHolder {

        Ping ping;
        StartDownload startDownload;
        StartUpload startUpload;
        StartTestPing startTestPing;
        SpeedTestSocket speedTestSocket;
        ProgressBar progress,processTest;
        TextView progressText,processDU;
        ObjectAnimator anim;
        Button start;
        Gauge speedView;

        String netType;

        public ViewProgressTestHolder(View v) {
            super(v);
            this.progressText = (TextView) v.findViewById(R.id.progressText);
            this.processDU = (TextView) v.findViewById(R.id.processDU);
            this.start = (Button) v.findViewById(R.id.start);
            this.progress = (ProgressBar) v.findViewById(R.id.progress);
            this.processTest = (ProgressBar) v.findViewById(R.id.processTest);
            this.speedView = (Gauge) v.findViewById(R.id.speedView);

            speedTestSocket = new SpeedTestSocket();



            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    netType = getNetworkType(mContext);

                    if(netType == null)
                        Toast.makeText(myActivity, "Подключение к сети отсутствует!", Toast.LENGTH_LONG).show();

                    if(status_du == STATUS_FINISH && netType != null) {    //если проверка закончилась и есть конект
                        startTestPing = new StartTestPing();
                        startTestPing.execute();

                        startDownload = new StartDownload();    //запускаем следующую
                        startDownload.execute();
                        testSpeed = true;

                    }

                }
            });

            handlerViewProgress = new Handler() {
                @Override
                public void handleMessage(Message msg) {

                    Bundle bundle = msg.getData();
                    float speedF = Math.round(bundle.getFloat("speed")/1000000*(float)100.0)/(float)100.0;

                    progressText.setText(Math.round(bundle.getFloat("percent")) + " %");
                    progress.setProgress(Math.round(bundle.getFloat("percent")));
                    speedView.speedTo(speedF);

                    if (status_du == STATUS_DOWNLOAD) {
                        processDU.setText("Downloading...");
                        processTest.setVisibility(View.VISIBLE);
                    }
                    else if(status_du == STATUS_UPLOAD)
                        processDU.setText("Uploading...");
                    else if(status_du == STATUS_FINISH) {
                        processDU.setText("Finish!");
                        processTest.setVisibility(View.INVISIBLE);
                        speedView.speedTo(0);
                    }

                }
            };

            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onDownloadFinished(SpeedTestReport report) {
                    // called when download is finished
                    System.out.println("[DL FINISHED] rate in octet/s : " + report.getTransferRateOctet());
                    System.out.println("[DL FINISHED] rate in bit/s   : " + report.getTransferRateBit());



                    Message msg = handlerDownloadSpeed.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("speed", report.getTransferRateBit().floatValue());
                    msg.setData(bundle);
                    handlerDownloadSpeed.sendMessage(msg);


                    startUpload = new StartUpload();    //запускаем следующую
                    startUpload.execute();
                }

                @Override
                public void onDownloadError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download error occur
                }

                @Override
                public void onUploadFinished(SpeedTestReport report) {
                    // called when an upload is finished
                    System.out.println("[UL FINISHED] rate in octet/s : " + report.getTransferRateOctet());
                    System.out.println("[UL FINISHED] rate in bit/s   : " + report.getTransferRateBit());

                    status_du = STATUS_FINISH;

                    Message msg = handlerUploadSpeed.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("speed", report.getTransferRateBit().floatValue());
                    msg.setData(bundle);
                    handlerUploadSpeed.sendMessage(msg);

                    msg = handlerViewProgress.obtainMessage();
                    bundle = new Bundle();
                    bundle.putFloat("speed", report.getTransferRateBit().floatValue());
                    msg.setData(bundle);
                    handlerViewProgress.sendMessage(msg);
                }

                @Override
                public void onUploadError(SpeedTestError speedTestError, String errorMessage) {
                    // called when an upload error occur
                }

                @Override
                public void onDownloadProgress(float percent, SpeedTestReport report) {
                    // called to notify download progress
                    System.out.println("[DL PROGRESS] progress : " + percent + "%");
                    System.out.println("[DL PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                    System.out.println("[DL PROGRESS] rate in bit/s   : " + report.getTransferRateBit());

                    status_du = STATUS_DOWNLOAD;

                    Message msg = handlerViewProgress.obtainMessage();
                    Bundle bundle = new Bundle();

                    bundle.putFloat("percent", percent);
                    bundle.putFloat("speed", report.getTransferRateBit().floatValue());
                    msg.setData(bundle);
                    handlerViewProgress.sendMessage(msg);
                }

                @Override
                public void onUploadProgress(float percent, SpeedTestReport report) {
                    // called to notify upload progress
                    System.out.println("[UL PROGRESS] progress : " + percent + "%");
                    System.out.println("[UL PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                    System.out.println("[UL PROGRESS] rate in bit/s   : " + report.getTransferRateBit());

                    status_du = STATUS_UPLOAD;

                    Message msg = handlerViewProgress.obtainMessage();
                    Bundle bundle = new Bundle();

                    bundle.putFloat("percent", percent);
                    bundle.putFloat("speed", report.getTransferRateBit().floatValue());
                    msg.setData(bundle);
                    handlerViewProgress.sendMessage(msg);

                }

                @Override
                public void onInterruption() {
                    // triggered when forceStopTask is called
                }
            });
        }
        private class StartTestPing extends AsyncTask<Void, Void, Integer> {


            protected Integer doInBackground(Void... params) {
                try {

                    ping =  ping(new URL("https://www.google.com:443/"), mContext);
                    Log.i(LOG_TAG, " Ping Test \n Host " + ping.host +
                            " Net " + ping.net +
                            " DNS " + ping.dns +
                            " CNT " + ping.cnt +
                            " IP " + ping.ip);

                    Message msg = handlerPing.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("ping", ping.cnt);
                    msg.setData(bundle);
                    handlerPing.sendMessage(msg);



                } catch (Exception e) {
                    Log.i(LOG_TAG, "Ошибка запроса на пинг " + e.getMessage());
                }

                return null;
            }
        }
        public class Ping {
            public String net = "NO_CONNECTION";
            public String host;
            public String ip;
            public int dns = Integer.MAX_VALUE;
            public int cnt = Integer.MAX_VALUE;
        }
        public  Ping ping(URL url, Context ctx) {
            Ping r = new Ping();
            if (isNetworkConnected(ctx)) {
                r.net = getNetworkType(ctx);
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
                    Log.i(LOG_TAG, "Exception ping = " + e.getMessage());
                }
            }
            return r;
        }
        public boolean isNetworkConnected(Context context) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        public String getNetworkType(Context context) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                return activeNetwork.getTypeName();
            }
            return null;
        }
        private class StartDownload extends AsyncTask<Void, Void, Integer> {


            protected Integer doInBackground(Void... params) {
                try {

                    speedTestSocket.startDownload("2.testdebit.info", "/fichiers/1Mo.dat");

                } catch (Exception e) {
                    Log.i(LOG_TAG, "ошибка запроса на загрузку файла " + e.getMessage());
                }

                return null;
            }
        }
        private class StartUpload extends AsyncTask<Void, Void, Integer> {


            protected Integer doInBackground(Void... params) {
                try {

                    speedTestSocket.startUpload("2.testdebit.info", "/", 1000000);

                } catch (Exception e) {
                    Log.i(LOG_TAG, "ошибка запроса на выгрузку файла " + e.getMessage());
                }

                return null;
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Log.i(LOG_TAG, "TestSpeedAdapter onCreateViewHolder");
        View v;
        if (viewType == CARD_1) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.ping, viewGroup, false);
            return new PingHolder(v);
        } else if (viewType == CARD_2){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.download_speed, viewGroup, false);
            return new TestDownloadHolder(v);
        }
        else if (viewType == CARD_3){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.upload_speed, viewGroup, false);
            return new TestUploadHolder(v);
        }
        else if (viewType == CARD_4){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.progress_speed, viewGroup, false);
            return new ViewProgressTestHolder(v);
        }
        return null;
    }
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == CARD_1) {
            final PingHolder pingHolder = (PingHolder) viewHolder;

        }
        else if (viewHolder.getItemViewType() == CARD_2) {
            TestDownloadHolder testDownloadHolder = (TestDownloadHolder) viewHolder;
        }
        else if (viewHolder.getItemViewType() == CARD_3) {

            TestUploadHolder testUploadHolder = (TestUploadHolder) viewHolder;

        }
        else if (viewHolder.getItemViewType() == CARD_4) {
            final ViewProgressTestHolder viewProgressTestHolder = (ViewProgressTestHolder) viewHolder;

        }
    }
    @Override
    public int getItemCount() {
        return mDataSet.length;
    }
    @Override
    public int getItemViewType(int position) {
        return mDataSetTypes[position];
    }
}
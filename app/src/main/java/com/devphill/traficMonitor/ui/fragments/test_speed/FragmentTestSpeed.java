package com.devphill.traficMonitor.ui.fragments.test_speed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
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
import com.devphill.traficMonitor.adapter.TestSpeedAdapter;
import com.devphill.traficMonitor.ui.fragments.test_speed.helper.DownloadUpload;
import com.devphill.traficMonitor.ui.fragments.test_speed.helper.Ping;
import com.devphill.traficMonitor.ui.fragments.test_speed.helper.SpeedTest;
import com.devphill.traficMonitor.ui.fragments.test_speed.helper.StartTestPing;
import com.devphill.traficMonitor.util.Util;
import com.github.anastr.speedviewlib.base.Gauge;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class FragmentTestSpeed extends Fragment  {

    public static String LOG_TAG = "FragmentTestSpeed";

    public int status_du = 0;
    public static final int STATUS_DOWNLOAD = 1;
    public static final int STATUS_UPLOAD = 2;
    public static final int STATUS_FINISH = 0;

    @BindView(R.id.start)
    Button start;

    @BindView(R.id.pingView)
    TextView pingView;

    @BindView(R.id.uploadSpeed)
    TextView uploadSpeed;

    @BindView(R.id.downloadSpeed)
    TextView downloadSpeed;


    @BindView(R.id.progressText)
    TextView progressText;

    @BindView(R.id.processDU)
    TextView processDU;

    @BindView(R.id.progress)
    ProgressBar progress;

    @BindView(R.id.processTest)
    ProgressBar processTest;

    @BindView(R.id.speedView)
    Gauge speedView;
   // this.progressText = (TextView) v.findViewById(R.id.progressText);
   // this.processDU = (TextView) v.findViewById(R.id.processDU);
  //  this.start = (Button) v.findViewById(R.id.start);
 //   this.progress = (ProgressBar) v.findViewById(R.id.progress);
   // this.processTest = (ProgressBar) v.findViewById(R.id.processTest);
   // this.speedView = (Gauge) v.findViewById(R.id.speedView);

    StartTestPing startTestPing;
    ISpeedTestListener iSpeedTestListener;

    SpeedTest speedTest;

    boolean runTesting = false;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View roorview = inflater.inflate(R.layout.test_speed_fragment2, container, false);

        ButterKnife.bind(this,roorview);


        startTestPing = new StartTestPing(getContext());
      //  initSpeedtwstListener();
        speedTest = new SpeedTest();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String netType;
                netType = Util.getNetworkType(getContext());

                if(netType == null)
                    Toast.makeText(getActivity(), "Подключение к сети отсутствует!", Toast.LENGTH_LONG).show();
                else if(!runTesting){
                    startTestPing.getPing().subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableObserver<Ping>() {
                                @Override
                                public void onNext(Ping ping) {
                                    pingView.setText(Integer.toString(ping.cnt));
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {


                                }
                            });

                    speedTest.getDownloadObservable().subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableObserver<DownloadUpload>() {
                                @Override
                                public void onNext(DownloadUpload downloadUpload) {

                                    showProgressDU(downloadUpload.getPercent(),downloadUpload.getSpeed(),downloadUpload.getStatus());
                                    Log.i(LOG_TAG, "speed " + downloadUpload.getSpeed());

                                }

                                @Override
                                public void onError(Throwable e) {
                                    //Handle error
                                }

                                @Override
                                public void onComplete() {
                                    Log.i(LOG_TAG, "onComplete download ");
                                    speedTest.getUploadObservable().subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeWith(new DisposableObserver<DownloadUpload>() {
                                                @Override
                                                public void onNext(DownloadUpload downloadUpload) {
                                                    showProgressDU(downloadUpload.getPercent(),downloadUpload.getSpeed(),downloadUpload.getStatus());
                                                }

                                                @Override
                                                public void onError(Throwable e) {}

                                                @Override
                                                public void onComplete() {
                                                    Log.i(LOG_TAG, "onComplete upload ");
                                                    showProgressDU(0,0,STATUS_FINISH);
                                                    runTesting = false;

                                                }
                                            });
                                }
                            });
                    runTesting = true;
                }

            }
        });

        Log.i(LOG_TAG, "onCreateView ");

        return roorview;
    }


    public void showProgressDU(float percent,float speed, int status) {
        Log.i(LOG_TAG, "showProgressDU ");

        float speedF = speed/1000000;
        speedF = Math.round(speedF*(float)100.0)/(float)100.0;

        Log.i(LOG_TAG, "speedF " + speedF);

        progressText.setText(Math.round(percent) + " %");
        progress.setProgress(Math.round(percent));
        speedView.speedTo(speedF);

        if (status == STATUS_DOWNLOAD) {
            processDU.setText("Downloading...");
            processTest.setVisibility(View.VISIBLE);
            downloadSpeed.setText(Float.toString(speedF));
        }
        else if(status == STATUS_UPLOAD){
            processDU.setText("Uploading...");
            uploadSpeed.setText(Float.toString(speedF));
        }
        else if(status_du == STATUS_FINISH) {
            processDU.setText("Finish!");
            processTest.setVisibility(View.INVISIBLE);
            speedView.speedTo(0);
        }

    }
}

package com.devphill.traficMonitor.ui.fragments.test_speed.helper;

import android.util.Log;

import com.devphill.traficMonitor.model.Traffic;
import com.devphill.traficMonitor.ui.fragments.test_speed.FragmentTestSpeed;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SpeedTest {

    SpeedTestSocket speedTestSocket;
    SpeedTestListener speedTestListener;


    public interface SpeedTestListener {

        void showProgressDU(float percent, int status);
    }

    public SpeedTest() {

        this.speedTestListener = speedTestListener;

        speedTestSocket = new SpeedTestSocket();
    }


    public Observable getDownloadObservable() {

        return Observable.create(new ObservableOnSubscribe<DownloadUpload>() {
            @Override
            public void subscribe(final ObservableEmitter<DownloadUpload> e) throws Exception {

                speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
                    @Override
                    public void onCompletion(SpeedTestReport report) {
                        e.onComplete();
                    }

                    @Override
                    public void onProgress(float percent, SpeedTestReport report) {
                        e.onNext(new DownloadUpload(percent, report.getTransferRateBit().floatValue(), FragmentTestSpeed.STATUS_DOWNLOAD));
                    }

                    @Override
                    public void onError(SpeedTestError speedTestError, String errorMessage) {

                    }
                });

                speedTestSocket.startDownload("http://2.testdebit.info/fichiers/1Mo.dat");
            }
        });
    }

    public Observable getUploadObservable() {

        return Observable.create(new ObservableOnSubscribe<DownloadUpload>() {
            @Override
            public void subscribe(final ObservableEmitter<DownloadUpload> e) throws Exception {

                speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {
                    @Override
                    public void onCompletion(SpeedTestReport report) {
                        e.onComplete();
                        e.onNext(new DownloadUpload(0, report.getTransferRateBit().floatValue(), FragmentTestSpeed.STATUS_FINISH));

                    }
                    @Override
                    public void onProgress(float percent, SpeedTestReport report) {
                        e.onNext(new DownloadUpload(percent, report.getTransferRateBit().floatValue(), FragmentTestSpeed.STATUS_UPLOAD));
                    }

                    @Override
                    public void onError(SpeedTestError speedTestError, String errorMessage) {

                    }
                });

                speedTestSocket.startUpload("http://2.testdebit.info/", 1000000);
            }
        });
    }
}
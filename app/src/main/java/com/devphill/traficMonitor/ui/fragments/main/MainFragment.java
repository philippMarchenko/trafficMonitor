package com.devphill.traficMonitor.ui.fragments.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.ui.fragments.main.helper.LineChartHelper;
import com.devphill.traficMonitor.ui.fragments.main.helper.PieChartHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment {

    public  String LOG_TAG = "mainFragmentTag";

    public final static String UPDATE_DATA_ACTION = "update_data_action";

    public final static String UPDATE_CHART = "update_chart";
    public final static String UPDATE_DATA = "update_data";

    @BindView(R.id.linechart)
    LineChart lineChart;

    @BindView(R.id.selectPeriod)
    Spinner selectPeriod;                                           //период,месяц или день

    @BindView(R.id.downloadData)
    TextView download_data;

    @BindView(R.id.uploadData)
    TextView uploadData;

    @BindView(R.id.pieChart)
    PieChart pieChart;

    DBHelper dbHelper;

    LineChartHelper lineChartHelper;
    PieChartHelper pieChartHelper;

    private static BroadcastReceiver br;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment2, container, false);

        ButterKnife.bind(this,view);


        dbHelper = new DBHelper(getContext());
        lineChartHelper = new LineChartHelper(lineChart,selectPeriod,dbHelper,getContext());
        pieChartHelper = new PieChartHelper(pieChart,getContext());

        lineChartHelper.initChart();
        lineChartHelper.init_spiner();
        pieChartHelper.initPieChart();
        pieChartHelper.updateDataPie(0);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                lineChartHelper.getDataInTable();
                // getDataTestTable();
            }
        });t.start();

        lineChartHelper.updateChart();

        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {     //приемник сообщений из сервиса
                if (intent.getIntExtra(UPDATE_CHART, 0) == 1) {
                    Log.i(LOG_TAG, "Сообщение из сервиса");

                    lineChartHelper.clearChart();
                    lineChartHelper.getDataInTable();
                    lineChartHelper.updateChart();

                }
                if (intent.getIntExtra(UPDATE_DATA, 0) == 1) {
                    Log.i(LOG_TAG, "Сообщение для обновления");
                    lineChartHelper.getDataInTable();
                    lineChartHelper.updateChart();

                    uploadData.setText(Float.toString(intent.getFloatExtra("trafficTxFloat",0)) + " Мб");
                    download_data.setText(Float.toString(intent.getFloatExtra("trafficRxFloat",0)) + " Мб");

                    pieChartHelper.updateDataPie(intent.getFloatExtra("trafficFloat",0));

                }
            }
        };


        Log.i(LOG_TAG, "MainFragment onCreateView");

        return view;
    }


    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "MainFragment onResume");
        if(TrafficService.brRegistered == false) {
            getActivity().registerReceiver(br, new IntentFilter(UPDATE_DATA_ACTION));
            TrafficService.brRegistered = true;
        }

     /*   GridLayoutManager manager = new GridLayoutManager(getContext(), 2, GridLayout.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){

            @Override
            public int getSpanSize(int position) {
                return (position % 3 == 0 ? 2 : 1);
            }
        });
        // recyclerView.setHasFixedSize(true);
        mainFragmentAdapter = new MainFragmentAdapter(getContext(), getActivity(), mDataset, mDatasetTypes);
        // RecyclerView.LayoutManager mLayoutManager = new MyLayoutManager();
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mainFragmentAdapter);*/

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(LOG_TAG, "MainFragment onPause");

        if( TrafficService.brRegistered){
            try {
                getActivity().unregisterReceiver(br);
                TrafficService.brRegistered = false;
            }catch (IllegalArgumentException e){
                Log.d(LOG_TAG, "Error unregisterReceiver in MainFragment" + e.getMessage());
            }
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "MainFragment onDestroy");
        if (TrafficService.brRegistered == true) {
            try {
                //   getActivity().unregisterReceiver(MainFragmentAdapter.br);
                TrafficService.brRegistered = false;
            } catch (IllegalArgumentException e) {
                Log.d(LOG_TAG, "Error unregisterReceiver" + e.getMessage());
            }
        }
    }

}

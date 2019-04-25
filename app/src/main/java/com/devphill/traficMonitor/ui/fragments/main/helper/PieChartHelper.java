package com.devphill.traficMonitor.ui.fragments.main.helper;

import android.content.Context;
import android.graphics.Color;

import com.devphill.traficMonitor.App;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.helper.MyValueFormatterPie;
import com.devphill.traficMonitor.service.TrafficService;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;

public class PieChartHelper{

    private PieChart pieChart;
    private Context context;

    private ArrayList<Entry> entriesPie = new ArrayList<>();
    private ArrayList<String> labelsPie = new ArrayList<String>();

    public PieChartHelper(PieChart pieChart,Context context) {

        this.pieChart = pieChart;
        this.context = context;
    }


    public void updateDataPie (float trafficFloat){


            PieData d = pieChart.getData();
            pieChart.clear();
            entriesPie.clear();
            labelsPie.clear();

            d.addEntry(new Entry((App.dataManager.getStopLevel() - trafficFloat),0),0);
            d.addEntry(new Entry(trafficFloat,0),0);

            labelsPie.add(context.getResources().getString(R.string.have_left));
            labelsPie.add(context.getResources().getString(R.string.used_traffic));

            initPieChart();

            pieChart.notifyDataSetChanged();
            pieChart.invalidate();
        }

    public  void initPieChart(){

        final int[] MY_COLORS = {
                context.getResources().getColor(R.color.primary_dynamic),
                context.getResources().getColor(R.color.accent),
                // Color.YELLOW,
        };


        // adding colors
        ArrayList<Integer> colors = new ArrayList<Integer>();

        PieDataSet dataset = new PieDataSet(entriesPie, "");

        PieData data = new PieData(labelsPie, dataset);
        // Added My Own colors
        for (int c : MY_COLORS)
            colors.add(c);

        dataset.setColors(colors);

        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);


        data.setValueFormatter(new MyValueFormatterPie(context));

        pieChart.setDrawSliceText(false);
        pieChart.setDescription("");
        //  pieChart.setHoleColor(Color.BLACK);
        //  pieChart.setUsePercentValues(true);
        // pieChart.setCenterText("Использовано от лимита");
        pieChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        pieChart.animateY(0);
        pieChart.getLegend().setTextColor(Color.WHITE);
        // enable hole and configure
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColorTransparent(true);
        pieChart.setHoleRadius(20);
        pieChart.setTransparentCircleRadius(30);

        // enable rotation of the chart by touch
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);

        pieChart.setData(data);

        }
}

package com.devphill.traficMonitor.ui.fragments.main.helper;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.helper.CustomMarkerView;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.helper.MyValueFormatter;
import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.util.Util;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LineChartHelper{

    public  String LOG_TAG = "FileChartHelper";



    private LineChart lineChart;
    private Spinner selectPeriod;                                           //период,месяц или день

    private DBHelper dbHelper;
    private Context context;

    private ArrayList<Entry> entriesLineChart = new ArrayList<>();          //список для точек
    private ArrayList<String> labelsLineChart = new ArrayList<>();          //список для информации о времени на оси x
    private ArrayList<String> labelsLineChartDate = new ArrayList<>();      //список для даты времени на графике в точке

    private LineData dataLineChart;

    private long currentTime,lastTime;

    private boolean firstStartGraph = true;

    private int periodChart = TrafficService.PERIOD_DAY;
    private int periodChartOffset = 29;         //через сколько точек из таблицы отображать на граффике
    private int countPointsCharts = 1000;       //всего точек на граффике
    private int countEntrys = 1;                //количество точек на графике в данный момент

    private boolean firstRunSpiner = true;


    public LineChartHelper(LineChart lineChart, Spinner selectPeriod,DBHelper dbHelper,Context context){

        this.lineChart = lineChart;
        this.dbHelper = dbHelper;
        this.context = context;
        this.selectPeriod = selectPeriod;
    }


    public void initChart(){

        recoverPeriodChart();

        entriesLineChart.add(new Entry(0, 0));


        LineDataSet dataset = new LineDataSet(entriesLineChart, "Суточное потребление трафика");


        labelsLineChart.add("0");
        labelsLineChartDate.add("0");


        dataLineChart = new LineData(labelsLineChart,dataset);


        // dataset.setColors(ColorTemplate.LIBERTY_COLORS); //
        dataset.setDrawCubic(true);
        //       Log.d(LOG_TAG_FR1, "getCubicIntensity " + dataset.getCubicIntensity());

        dataset.setDrawFilled(true);
        dataset.setDrawValues(false);
        dataset.setDrawCircles(false);
        dataset.setColor(Color.GREEN);


        lineChart.setData(dataLineChart);
        lineChart.getLegend().setEnabled(false);
        //lineChart.setDescription("Суточное потребление трафика");
        lineChart.setDescriptionTextSize(12);
        lineChart.setDescriptionColor(Color.WHITE);
        lineChart.setTouchEnabled(true);
        lineChart.setBackgroundColor(Color.rgb(201,124,223));
        lineChart.setBackgroundColor(context.getResources().getColor(R.color.cardview_color));
        lineChart.setGridBackgroundColor(context.getResources().getColor(R.color.accent));

        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);

        lineChart.setDrawMarkerViews(true);
        CustomMarkerView customMarkerView = new CustomMarkerView(context, R.layout.custom_marker_view_layout,labelsLineChartDate);

        // customMarkerView.set
        lineChart.setMarkerView(customMarkerView);

        LimitLine upper_limit = new LimitLine(TrafficService.stopLevel, "Лимит траффика");
        upper_limit.setLineWidth(2f);
        upper_limit.enableDashedLine(10f, 0f, 0f);
        upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper_limit.setTextSize(10f);
        upper_limit.setTextColor(Color.WHITE);
        upper_limit.setLineColor(Color.RED);

        LimitLine allert_level = new LimitLine(TrafficService.allertLevel, "Уровень предупреждения");
        allert_level.setLineWidth(2f);
        allert_level.enableDashedLine(10f, 0f, 0f);
        allert_level.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        allert_level.setTextSize(10f);
        allert_level.setTextColor(Color.WHITE);
        allert_level.setLineColor(Color.YELLOW);


        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setValueFormatter(new MyValueFormatter(context));
        yAxis.addLimitLine(upper_limit);
        yAxis.addLimitLine(allert_level);
        //  yAxis.setAxisMaxValue(TrafficService.stopLevel + 5);

        lineChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                       /* MainActivity.viewPager.setPagingEnabled(false);
                        if(event.getAction() == MotionEvent.ACTION_UP)
                            MainActivity.viewPager.setPagingEnabled(true);*/
                ViewParent parent = v.getParent();
                // or get a reference to the ViewPager and cast it to ViewParent

                parent.requestDisallowInterceptTouchEvent(true);

                // let this view deal with the event or
                return false;
            }
        });
    }

    public void updateChart() {
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void getDataInTable() {

        if (Util.isMyServiceRunning(TrafficService.class,context) && TrafficService.idsim != null) {

            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query(TrafficService.idsim, null, null, null, null, null, null);

            if (c.moveToFirst()) {
                // определяем номера столбцов по имени в выборке
                int timeColIndex = c.getColumnIndex("time");
                int mobile_trafficTXTodayColIndex = c.getColumnIndex("mobile_trafficTXToday");
                int mobile_trafficRXTodayColIndex = c.getColumnIndex("mobile_trafficRXToday");
                int mobile_trafficTXYesterdayColIndex = c.getColumnIndex("mobile_trafficTXYesterday");
                int mobile_trafficRXYesterdayColIndex = c.getColumnIndex("mobile_trafficRXYesterday");
                int allTrafficMobileColIndex = c.getColumnIndex("allTrafficMobile");
                int lastDayColIndex = c.getColumnIndex("lastDay");
                int idColIndex = c.getColumnIndex("id");

                do {
                    currentTime = c.getLong(timeColIndex);

                    if (currentTime > lastTime || firstStartGraph) { // получаем значения по номерам столбцов и пишем все в лог


                        firstStartGraph = false;
                        Log.d(LOG_TAG, " \n Есть новые запси в базе! " + c.getLong(timeColIndex) +
                                ", id = " + c.getInt(idColIndex) +
                                ", mobile_trafficTXToday = " + c.getLong(mobile_trafficTXTodayColIndex) +
                                ", mobile_trafficRXToday = " + c.getLong(mobile_trafficRXTodayColIndex) +
                                ", mobile_trafficTXYesterday = " + c.getLong(mobile_trafficTXYesterdayColIndex) +
                                ", mobile_trafficRXYesterday = " + c.getLong(mobile_trafficRXYesterdayColIndex) +
                                ", allTrafficMobile = " + c.getLong(allTrafficMobileColIndex) +
                                " lastDay = " + c.getInt(lastDayColIndex));

                        float trafficFloat = (float) c.getLong(allTrafficMobileColIndex) / 1024;
                        trafficFloat = Math.round(trafficFloat * (float) 100.0) / (float) 100.0;  //округляем до сотых

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                        String strTime = simpleDateFormat.format(new Date(currentTime));

                        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                        String strTimeDate = simpleDateFormatDate.format(new Date(currentTime));
                        labelsLineChartDate.add(countEntrys, strTimeDate);


                        LineData data = lineChart.getData();
                        if (data != null) {

                            data.addXValue(strTime);

                            data.addEntry(new Entry(trafficFloat, countEntrys), 0);  // добавляем значение трафика по оси Y, по Х id из таблицы

                            lineChart.setDescription(context.getResources().getString(R.string.used) + " " + trafficFloat + " " + context.getResources().getString(R.string.mb));
                            countEntrys++;
                        }
                    }

                    // переход на следующую строку
                    // а если следующей нет (текущая - последняя), то false - выходим из цикла
                } while (c.move(periodChartOffset));
            }

            float y = c.getCount() / (float) periodChartOffset;
            if (y % 1 != 0) { //не целое

                labelsLineChart.remove(countEntrys - 1);  //удаляем последнюю точку
                entriesLineChart.remove(countEntrys - 1);
                labelsLineChartDate.remove(countEntrys - 1);

                countEntrys--;
                Log.d(LOG_TAG, " Не целое значание, y = " + y);

                c.moveToLast();                         //курсор на послнднюю

                int allTrafficMobileColIndex = c.getColumnIndex("allTrafficMobile");
                int timeColIndex = c.getColumnIndex("time");
                float trafficFloat = (float) c.getLong(allTrafficMobileColIndex) / 1024;
                trafficFloat = Math.round(trafficFloat * (float) 100.0) / (float) 100.0;  //округляем до сотых

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                String strTime = simpleDateFormat.format(new Date((c.getLong(timeColIndex))));

                SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                String strTimeDate = simpleDateFormatDate.format(new Date(c.getLong(timeColIndex)));
                labelsLineChartDate.add(countEntrys, strTimeDate);


                LineData data = lineChart.getData();
                if (data != null) {

                    data.addXValue(strTime);
                    data.addEntry(new Entry(trafficFloat, countEntrys), 0);  // добавляем значение трафика по оси Y, по Х id из таблицы
                    lineChart.setDescription(context.getResources().getString(R.string.used) + " " + trafficFloat + context.getResources().getString(R.string.mb));
                    countEntrys++;
                }
            }

            c.close();

            lastTime = currentTime;
        }
    }

    public void clearChart(){
        lastTime = 0;
        countEntrys = 1;
        lineChart.clear();

        labelsLineChart.clear();
        entriesLineChart.clear();
        labelsLineChartDate.clear();

        initChart();
        updateChart();
    }

    public void recoverPeriodChart(){

        if(Util.isMyServiceRunning(TrafficService.class,context)) {
            SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
            periodChartOffset = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_PERIOD_CHART,1200);
            Log.d(LOG_TAG,
                    " recoverPeriodChart = " + periodChartOffset);
        }

    }

    public void init_spiner(){

        String[] dataspiner = {context.getResources().getString(R.string.day), context.getResources().getString(R.string.month)};


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spiner_item, dataspiner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        selectPeriod.setAdapter(adapter);
        if(periodChartOffset == 29)     //восстановление позиции спинера
            selectPeriod.setSelection(0);
        else if (periodChartOffset == 1200)
            selectPeriod.setSelection(1);

        selectPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
                if(!firstRunSpiner) {
                    if (position == 0)
                        periodChartOffset = 29;
                        // periodChart = TrafficService.PERIOD_DAY;
                    else if (position == 1)
                        periodChartOffset = 1200;
                    // periodChart = TrafficService.PERIOD_MOUNTH;
                    // Toast.makeText(mContext, "periodChartOffset = " + periodChartOffset, Toast.LENGATH_SHORT).show();
                    Log.i(LOG_TAG, "onItemSelected spiner, periodChartOffset " + periodChartOffset);
                    updatePeriodChart(periodChartOffset);

                    clearChart();
                    //getDataTestTable();
                    getDataInTable();
                    updateChart();
                }
                firstRunSpiner = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }
    public void updatePeriodChart(int period){

        if(Util.isMyServiceRunning(TrafficService.class,context)) {

            SharedPreferences mySharedPreferences = context.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putInt(TrafficService.APP_PREFERENCES_PERIOD_CHART, period);
            editor.apply();
            Log.d(LOG_TAG,
                    " periodChartOffset is update = " + periodChartOffset);
        }

    }
}
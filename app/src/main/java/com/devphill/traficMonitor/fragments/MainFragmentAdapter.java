package com.devphill.traficMonitor.fragments;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.devphill.traficMonitor.CustomMarkerView;
import com.devphill.traficMonitor.DBHelper;
import com.devphill.traficMonitor.MainActivity;
import com.devphill.traficMonitor.MyValueFormatter;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.TrafficService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

public class MainFragmentAdapter extends RecyclerView.Adapter<MainFragmentAdapter.ViewHolder> {

    private String[] mDataSet;
    private int[] mDataSetTypes;
    private Context mContext;
    private Activity myActivity;
   // private CustomViewPager mPager;

    public final static String BROADCAST_ACTION = "myFilter";
    public final static String UPDATE_DATA_ACTION = "update_data_action";

    public final static String UPDATE_CHART = "update_chart";
    public final static String UPDATE_DATA = "update_data";

    public  String LOG_TAG = "mainFragmentTag";

    public static final int CARD_1 = 0;
    public static final int CARD_2 = 1;
    public static final int CARD_3 = 2;
    public static final int CARD_4 = 3;

    public static final int UPDATE_DATA_INFO = 1;

    Handler handlerUploadData = new Handler();
    Handler handlerDownloadData = new Handler();
    Handler handlerPieChart = new Handler();
    Timer myTimer = new Timer(); // Создаем таймер



    public static boolean firstStartGraph = true;

    public static BroadcastReceiver br;

    long currentTime,lastTime;

    int periodChart = TrafficService.PERIOD_DAY;
    int periodChartOffset = 29;         //через сколько точек из таблицы отображать на граффике
    int countPointsCharts = 1000;       //всего точек на граффике
    int countEntrys = 1;                //количество точек на графике в данный момент

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }
    public class LineChartHolder extends ViewHolder {

        boolean firstRunSpiner = true;

        DBHelper dbHelper = new DBHelper(mContext);

        LineChart lineChart;
        ArrayList<Entry> entriesLineChart = new ArrayList<>();           //список для точек
        ArrayList<String> labelsLineChart = new ArrayList<>();          //список для информации о времени на оси x
        ArrayList<String> labelsLineChartDate = new ArrayList<>();      //список для даты времени на графике в точке
        LineData dataLineChart;
        Spinner selectPeriod;                                           //период,месяц или день

        String[] dataspiner = {mContext.getResources().getString(R.string.day), mContext.getResources().getString(R.string.month)};

        public LineChartHolder(View v) {
            super(v);
            this.lineChart = (LineChart) v.findViewById(R.id.linechart);
            this.selectPeriod = (Spinner) v.findViewById(R.id.selectPeriod);


            initChart();

            Thread t = new Thread(new Runnable() {
                @Override
               public void run() {
                    getDataInTable();
                   // getDataTestTable();
                }
            });t.start();

            updateChart();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, R.layout.spiner_item, dataspiner);
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

            br = new BroadcastReceiver() {
                // действия при получении сообщений
                public void onReceive(Context context, Intent intent) {     //приемник сообщений из сервиса
                    if (intent.getIntExtra(UPDATE_CHART, 0) == 1) {
                        Log.i(LOG_TAG, "Сообщение из сервиса");

                        clearChart();
                        getDataInTable();
                        updateChart();

                    }
                    if (intent.getIntExtra(UPDATE_DATA, 0) == 1) {
                        Log.i(LOG_TAG, "Сообщение для обновления");
                        getDataInTable();
                        //  clearChart();
                        //  getDataTestTable();
                        updateChart();
                        handlerUploadData.sendEmptyMessage(UPDATE_DATA_INFO);
                        handlerDownloadData.sendEmptyMessage(UPDATE_DATA_INFO);
                        handlerPieChart.sendEmptyMessage(UPDATE_DATA_INFO);
                    }
                }
            };

                if(!TrafficService.brRegistered) {  //регистрируем приемник после его создания
                    myActivity.registerReceiver(br, new IntentFilter(MainFragmentAdapter.BROADCAST_ACTION));
                    TrafficService.brRegistered = true;
                }
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
            lineChart.setBackgroundColor(mContext.getResources().getColor(R.color.cardview_color));
            lineChart.setGridBackgroundColor(mContext.getResources().getColor(R.color.accent));





            lineChart.getAxisRight().setEnabled(false);
            lineChart.setDoubleTapToZoomEnabled(false);

            lineChart.setDrawMarkerViews(true);
            CustomMarkerView customMarkerView = new CustomMarkerView(mContext, R.layout.custom_marker_view_layout,labelsLineChartDate);

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
            yAxis.setValueFormatter(new MyValueFormatter(mContext));
            yAxis.addLimitLine(upper_limit);
            yAxis.addLimitLine(allert_level);
          //  yAxis.setAxisMaxValue(TrafficService.stopLevel + 5);
        }
        public void updateChart() {
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
        public void getDataInTable() {

            if (isMyServiceRunning(TrafficService.class) && TrafficService.idsim != null) {

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

                                lineChart.setDescription(mContext.getResources().getString(R.string.used) + " " + trafficFloat + mContext.getResources().getString(R.string.mb));
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
                        lineChart.setDescription(mContext.getResources().getString(R.string.used) + " " + trafficFloat + mContext.getResources().getString(R.string.mb));
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
        public boolean isMyServiceRunning(Class<?> serviceClass) {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }
        public void updatePeriodChart(int period){

            if(isMyServiceRunning(TrafficService.class)) {

                SharedPreferences mySharedPreferences = mContext.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.putInt(TrafficService.APP_PREFERENCES_PERIOD_CHART, period);
                editor.apply();
                Log.d(LOG_TAG,
                        " periodChartOffset is update = " + periodChartOffset);
            }

        }
        public void recoverPeriodChart(){

            if(isMyServiceRunning(TrafficService.class)) {
                SharedPreferences mySharedPreferences = mContext.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
                periodChartOffset = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_PERIOD_CHART,1200);
                Log.d(LOG_TAG,
                        " recoverPeriodChart = " + periodChartOffset);
            }

        }
        public void getDataTestTable(){
            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query("testDay", null, null, null, null, null, null);


            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            if (c.moveToFirst()) {
                int allTrafficMobileColIndex = c.getColumnIndex("allTrafficMobile");
                int idColIndex  = c.getColumnIndex("id");

                do {

                        Log.d(LOG_TAG, ", id = " + c.getInt(idColIndex) +
                                ", allTrafficMobile = " + c.getLong(allTrafficMobileColIndex));


                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                        String strTime = simpleDateFormat.format(new Date(currentTime));

                        LineData data = lineChart.getData();
                        if (data != null) {

                            data.addXValue(strTime);


                            data.addEntry(new Entry(c.getLong(allTrafficMobileColIndex), countEntrys),0);  // добавляем значение трафика по оси Y, по Х id из таблицы
                            //data.addXValue(labels.get(labels.size()));                                    //метка для значение по оси Х - это время
                            // entriesLineChart.add(new Entry(trafficFloat, xIndex));
                            lineChart.setDescription("Суточное потребление трафика " + c.getLong(allTrafficMobileColIndex) + " Мб");
                            countEntrys++;
                        }


                    // переход на следующую строку
                    // а если следующей нет (текущая - последняя), то false - выходим из цикла
                } while (c.move(periodChartOffset));
            }

            c.close();
        }
    }
    public class UploadDataHolder extends ViewHolder {

        DBHelper dbHelper = new DBHelper(mContext);
        TextView uploadData;

        public UploadDataHolder(View v) {
            super(v);
            this.uploadData = (TextView) v.findViewById(R.id.uploadData);

            updateUploadData();

            handlerUploadData = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == UPDATE_DATA_INFO){
                        updateUploadData();
                    }
                }
            };
        }


       public void updateUploadData(){
           // подключаемся к БД
           SQLiteDatabase db = dbHelper.getWritableDatabase();
           Cursor c = db.query(TrafficService.idsim, null, null, null, null, null, null);
           //Cursor c = db.query("n8938003992134961737f", null, null, null, null, null, null);

           // ставим позицию курсора на первую строку выборки
           // если в выборке нет строк, вернется false
           if (c.moveToLast()) {
               int mobile_trafficTXTodayColIndex = c.getColumnIndex("mobile_trafficTXToday");
              // int mobile_trafficRXTodayColIndex = c.getColumnIndex("mobile_trafficRXToday");

               long TrafficTx = c.getLong(mobile_trafficTXTodayColIndex)/1024;
               float trafficTxFloat = (float)TrafficTx/1024;
               trafficTxFloat = Math.round(trafficTxFloat*(float)100.0)/(float)100.0;  //округляем до сотых
               uploadData.setText(Float.toString(trafficTxFloat) + " Мб");
           }
           dbHelper.close();
           c.close();

       }

    }
    public class DownloadDataHolder extends ViewHolder {

        TextView downloadData;
        DBHelper dbHelper = new DBHelper(mContext);
        Timer timerDown = new Timer(); // Создаем таймер

        public DownloadDataHolder(View v) {
            super(v);
            this.downloadData = (TextView) v.findViewById(R.id.downloadData);

            updateDownloadData();

            handlerDownloadData = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == UPDATE_DATA_INFO){
                        updateDownloadData();
                    }
                }
            };
        }
        public void updateDownloadData(){
            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query(TrafficService.idsim, null, null, null, null, null, null);
            //Cursor c = db.query("n8938003992134961737f", null, null, null, null, null, null);

            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            if (c.moveToLast()) {
                //int mobile_trafficTXTodayColIndex = c.getColumnIndex("mobile_trafficTXToday");
                 int mobile_trafficRXTodayColIndex = c.getColumnIndex("mobile_trafficRXToday");

                long TrafficRx = c.getLong(mobile_trafficRXTodayColIndex)/1024;
                float trafficRxFloat = (float)TrafficRx/1024;
                trafficRxFloat = Math.round(trafficRxFloat*(float)100.0)/(float)100.0;  //округляем до сотых
                downloadData.setText(Float.toString(trafficRxFloat) + " Мб");


            }

            dbHelper.close();
            c.close();
        }

    }
    public class PieChartHolder extends ViewHolder {

        DBHelper dbHelper = new DBHelper(mContext);
        Button addData;
        Timer timerPie = new Timer(); // Создаем таймер
        PieChart pieChart;
        ArrayList<Entry> entriesPie = new ArrayList<>();
        ArrayList<String> labelsPie = new ArrayList<String>();

        float x = 0;
        float limittraffic = 100;



        public PieChartHolder(View v) {
            super(v);
            this.pieChart = (PieChart) v.findViewById(R.id.pieChart);

            initPieChart();
            updateDataPie();

            handlerPieChart = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == UPDATE_DATA_INFO){
                        updateDataPie();
                    }
                }
            };
        }

        public  void initPieChart(){

            final int[] MY_COLORS = {
                    mContext.getResources().getColor(R.color.primary_dynamic),
                    mContext.getResources().getColor(R.color.accent),
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


            data.setValueFormatter(new MyValueFormatterPie(mContext));

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

        public  void updateDataPie (){

            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query(TrafficService.idsim, null, null, null, null, null, null);
            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            if (c.moveToLast()) {
                // определяем номера столбцов по имени в выборке

                int allTrafficMobileColIndex = c.getColumnIndex("allTrafficMobile");
                float trafficFloat = (float)c.getLong(allTrafficMobileColIndex)/1024;
                trafficFloat = Math.round(trafficFloat*(float)100.0)/(float)100.0;  //округляем до сотых

                PieData d = pieChart.getData();
                pieChart.clear();
                entriesPie.clear();
                labelsPie.clear();

                d.addEntry(new Entry((TrafficService.stopLevel - trafficFloat),0),0);
                d.addEntry(new Entry(trafficFloat,0),0);

                labelsPie.add(mContext.getResources().getString(R.string.have_left));
                labelsPie.add(mContext.getResources().getString(R.string.used_traffic));

                initPieChart();

                pieChart.notifyDataSetChanged();
                pieChart.invalidate();
            }

            dbHelper.close();
            c.close();
        }
    }
    public MainFragmentAdapter(Context context, Activity activity, String[]dataSet, int[] dataSetTypes) {
        mContext = context;
        mDataSet = dataSet;
        mDataSetTypes = dataSetTypes;
        myActivity = activity;
        Log.i(LOG_TAG, "MainFragmentAdapter");
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Log.i(LOG_TAG, "MainFragmentAdapter onCreateViewHolder");
        View v;
        if (viewType == CARD_1) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.linechart, viewGroup, false);
            return new LineChartHolder(v);
        } else if (viewType == CARD_2){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.download_data, viewGroup, false);
            return new DownloadDataHolder(v);
        }
        else if (viewType == CARD_3){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.upload_data, viewGroup, false);
            return new UploadDataHolder(v);
        }
        else if (viewType == CARD_4){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.piechart, viewGroup, false);
            return new PieChartHolder(v);
        }
        return null;
    }
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == CARD_1) {
            final LineChartHolder lineChartHolder = (LineChartHolder) viewHolder;

        }
        else if (viewHolder.getItemViewType() == CARD_2) {
            DownloadDataHolder downloadDataHolder = (DownloadDataHolder) viewHolder;
        }
        else if (viewHolder.getItemViewType() == CARD_3) {

            UploadDataHolder uploadDataHolder = (UploadDataHolder) viewHolder;

        }
        else if (viewHolder.getItemViewType() == CARD_4) {
            final PieChartHolder pieChartHolder = (PieChartHolder) viewHolder;

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
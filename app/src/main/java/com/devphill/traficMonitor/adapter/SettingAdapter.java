package com.devphill.traficMonitor.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.devphill.traficMonitor.BuildConfig;
import com.devphill.traficMonitor.helper.DBHelper;
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.service.TrafficService;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;


public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {

    private static final String LOG_TAG = "setTag";


    private String[] mDataSet;
    private int[] mDataSetTypes;
    private Context mContext;
    private Activity myActivity;



    public static final int SET_1 = 0;
    public static final int SET_2 = 1;
    public static final int SET_3 = 2;
    public static final int SET_4 = 3;

    double procent;

    Handler handlerSet1;
    Handler handlerSet2;
    Handler handlerSet3;


    public static final int RECOVER_SETTINGS = 1;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View v) {
            super(v);
        }
    }

    public class Set1Holder extends ViewHolder {
        public TextView set_period;
        public RadioGroup radioGroup;
        public RadioButton button_month,button_day;

        public Set1Holder(View v) {
            super(v);
            this.set_period = (TextView) v.findViewById(R.id.set_period);
            this.radioGroup = (RadioGroup) v.findViewById(R.id.radioGroup1);
            this.button_month = (RadioButton) v.findViewById(R.id.button_month);
            this.button_day = (RadioButton) v.findViewById(R.id.button_day);


          //  handlerSet1 = new Handler(){
              //  @Override
                //public void handleMessage(Message msg) {
                 //   if(msg.what == RECOVER_SETTINGS){

                        if(TrafficService.period == TrafficService.PERIOD_DAY) {
                            button_month.setChecked(false);
                            button_day.setChecked(true);
                        }
                        else if(TrafficService.period == TrafficService.PERIOD_MOUNTH){
                            button_month.setChecked(true);
                            button_day.setChecked(false);
                        }
             //       }
            //    }
          //  };

        }

    }
    public class Set2Holder extends ViewHolder {

        public TextInputLayout inputDataLayout;
        public EditText inputDataStop,inputDataAllert;
        public RangeBar rangeBar;
        public TextView procent;
        public SwitchCompat stopDataSwitch,allertSwitch;

        public Set2Holder(View v) {
            super(v);
            this.rangeBar = (RangeBar) v.findViewById(R.id.rangebar);
            this.inputDataStop = (EditText) v.findViewById(R.id.inputDataStop);
            this.inputDataAllert = (EditText) v.findViewById(R.id.inputDataAllert);
            this.procent = (TextView) v.findViewById(R.id.procent);
            this.stopDataSwitch = (SwitchCompat) v.findViewById(R.id.stopDataSwitch);
            this.allertSwitch = (SwitchCompat) v.findViewById(R.id.allertSwitch);

           // handlerSet2 = new Handler() {
              //  @Override
              //  public void handleMessage(Message msg) {
                    //if(msg.what == RECOVER_SETTINGS){
                        inputDataStop.setText(Integer.toString(TrafficService.stopLevel));
                        inputDataAllert.setText(Integer.toString(TrafficService.allertLevel));
                       if(TrafficService.disable_internet == 1)
                           allertSwitch.setChecked(true);
                       else
                          allertSwitch.setChecked(false);
                       if(TrafficService.show_allert == 1)
                           stopDataSwitch.setChecked(true);
                       else
                           stopDataSwitch.setChecked(false);

                   // }
             //   }
          //  };
        }
    }
    public class Set3Holder extends ViewHolder {

        public Button setupDate;
        public TextView viewDate;
        DBHelper dbHelper = new DBHelper(mContext);

        public Set3Holder(View v) {
            super(v);
            this.setupDate = (Button) v.findViewById(R.id.setupDate);
            this.viewDate = (TextView) v.findViewById(R.id.viewDate);

            //handlerSet3 = new Handler() {
               // @Override
                //public void handleMessage(Message msg) {
                   // if(msg.what == RECOVER_SETTINGS){
                        viewDate.setText(TrafficService.day + "." + TrafficService.month + "." + TrafficService.mYear);
                  //  }
              //  }
           // };
            //recoverSettings();
        }
/*        public void recoverSettings(){

            SharedPreferences mySharedPreferences = mContext.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);

            TrafficService.period = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_PERIOD,TrafficService.period);
            TrafficService.stopLevel = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_STOPL_EVEL,TrafficService.stopLevel);
            TrafficService.allertLevel = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_ALLERT_LEVEL,TrafficService.allertLevel);
            TrafficService.disable_internet = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_DISABLE_INTERNET,TrafficService.disable_internet);
            TrafficService.show_allert = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_SHOW_ALLERT,TrafficService.show_allert);
            TrafficService.day = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_DAY,TrafficService.day);
            TrafficService.month = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_MONTH,TrafficService.month);
            TrafficService.mYear = mySharedPreferences.getInt(TrafficService.APP_PREFERENCES_YEAR,TrafficService.mYear);

            handlerSet1.sendEmptyMessage(RECOVER_SETTINGS);
            handlerSet2.sendEmptyMessage(RECOVER_SETTINGS);
            handlerSet3.sendEmptyMessage(RECOVER_SETTINGS);

       *//*     // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // делаем запрос всех данных из таблицы mytable, получаем Cursor
            Cursor c = db.query("set" + TrafficService.idsim, null, null, null, null, null, null);

            if (c.moveToFirst()) {

                int periodColIndex = c.getColumnIndex("period");
                int stopLevelColIndex = c.getColumnIndex("stopLevel");
                int allertLevelColIndex = c.getColumnIndex("allertLevel");
                int disable_internetColIndex = c.getColumnIndex("disable_internet");
                int show_allertColIndex = c.getColumnIndex("show_allert");
                int dayColIndex = c.getColumnIndex("day");
                int monthColIndex = c.getColumnIndex("month");
                int yearColIndex  = c.getColumnIndex("year");

                do {
                    // получаем значения по номерам столбцов и пишем все в лог
                    Log.d(LOG_TAG,
                            " period = " + c.getInt(periodColIndex) +
                                    ", \n stopLevel = " + c.getInt(stopLevelColIndex) +
                                    ", \n allertLevel = " + c.getInt(allertLevelColIndex) +
                                    ", \n disable_internet = " + c.getInt(disable_internetColIndex) +
                                    ", \n show_allert = " + c.getInt(show_allertColIndex) +
                                    ", \n day = " + c.getInt(dayColIndex) +
                                    ", \n month = " + c.getInt(monthColIndex) +
                                    ", \n year = " + c.getInt(yearColIndex));
                    // переход на следующую строку
                    // а если следующей нет (текущая - последняя), то false - выходим из цикла
                    TrafficService.period = c.getInt(periodColIndex);
                    TrafficService.stopLevel = c.getInt(stopLevelColIndex);
                    TrafficService.allertLevel = c.getInt(allertLevelColIndex);
                    TrafficService.disable_internet = c.getInt(disable_internetColIndex);
                    TrafficService.show_allert = c.getInt(show_allertColIndex);
                    TrafficService.day = c.getInt(dayColIndex);
                    TrafficService.month = c.getInt(monthColIndex);
                    TrafficService.mYear = c.getInt(yearColIndex);

                    handlerSet1.sendEmptyMessage(RECOVER_SETTINGS);
                    handlerSet2.sendEmptyMessage(RECOVER_SETTINGS);
                    handlerSet3.sendEmptyMessage(RECOVER_SETTINGS);

                } while (c.moveToNext());
            } else
                Log.d(LOG_TAG, "0 rows");
            c.close();
            // закрываем подключение к БД
            dbHelper.close();*//*

        }*/
    }
    public class Set4Holder extends ViewHolder {

        public Button zapros,newDay,newMonth;
        TextView versionApp;

        public Set4Holder(View v) throws PackageManager.NameNotFoundException {
            super(v);
            this.newDay = (Button) v.findViewById(R.id.newDay);
            this.versionApp = (TextView) v.findViewById(R.id.versionApp);

            newDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* Intent intent = new Intent(mContext,TrafficService.class);
                    intent.putExtra("task",TrafficService.CLEAN_TABLE);
                    mContext.startService(intent);*/

                    TrafficService.newDay = true;
                    TrafficService.newMonth = true;

                }
            });


        //    PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            versionApp.setText("Версия приложения " + BuildConfig.VERSION_NAME);

           //;



        }


    }
    public void updateSet(int setId){
       /* DBHelper dbHelper = new DBHelper(mContext);
        // создаем объект для данных
        ContentValues cv = new ContentValues();
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c = db.query("set" + TrafficService.idsim, null, null, null, null, null, null);*/

        SharedPreferences mySharedPreferences = mContext.getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();



        if(setId == SET_1){
              editor.putInt(TrafficService.APP_PREFERENCES_PERIOD, TrafficService.period);
          /*  cv.put("period",TrafficService.period);
            db.update("set" + TrafficService.idsim, cv, null,null);*/
        }
        else if(setId == SET_2){
            editor.putInt(TrafficService.APP_PREFERENCES_ALLERT_LEVEL, TrafficService.allertLevel);
            editor.putInt(TrafficService.APP_PREFERENCES_STOPL_EVEL, TrafficService.stopLevel);
            editor.putInt(TrafficService.APP_PREFERENCES_SHOW_ALLERT, TrafficService.show_allert);
            editor.putInt(TrafficService.APP_PREFERENCES_DISABLE_INTERNET, TrafficService.disable_internet);
            /*cv.put("allertLevel",TrafficService.allertLevel);
            cv.put("stopLevel",TrafficService.stopLevel);
            db.update("set" + TrafficService.idsim, cv, null,null);*/
        }
        else if(setId == SET_3){
            editor.putInt(TrafficService.APP_PREFERENCES_DAY, TrafficService.day);
            editor.putInt(TrafficService.APP_PREFERENCES_MONTH, TrafficService.month);
            editor.putInt(TrafficService.APP_PREFERENCES_YEAR, TrafficService.mYear);

           /* cv.put("day",TrafficService.day);
            cv.put("month",TrafficService.month);
            cv.put("year",TrafficService.mYear);
            db.update("set" + TrafficService.idsim, cv, null,null);*/
        }

        editor.apply();
       /* c.close();
        // закрываем подключение к БД
        dbHelper.close();*/

    }
    public SettingAdapter(Context context, Activity activity, String[]dataSet, int[] dataSetTypes) {
        mContext = context;
        mDataSet = dataSet;
        mDataSetTypes = dataSetTypes;
        myActivity = activity;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == SET_1) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.fragment_settings_1, viewGroup, false);
                      return new Set1Holder(v);
        } else if (viewType == SET_2){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.fragment_settings_2, viewGroup, false);
                     return new Set2Holder(v);
        }
         else if (viewType == SET_3){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.fragment_settings_3, viewGroup, false);
            return new Set3Holder(v);
        }
        else if (viewType == SET_4){
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.fragment_settings_4, viewGroup, false);
            try {
                return new Set4Holder(v);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if (viewHolder.getItemViewType() == SET_1) {
            Set1Holder holder = (Set1Holder) viewHolder;

            holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case -1:
                            //Toast.makeText(mContext, "Нихуя", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.button_day:
                            Toast.makeText(mContext, "Выбран день", Toast.LENGTH_SHORT).show();
                            TrafficService.period = TrafficService.PERIOD_DAY;
                            break;
                        case R.id.button_month:
                           // Toast.makeText(mContext, "Выбран месяц", Toast.LENGTH_SHORT).show();
                            TrafficService.period = TrafficService.PERIOD_MOUNTH;
                            break;
                    }
                    updateSet(SET_1);
                }
            });

        }
        else if (viewHolder.getItemViewType() == SET_2) {
            final Set2Holder holder = (Set2Holder) viewHolder;

            holder.inputDataStop.setOnKeyListener(new View.OnKeyListener()
                                      {
                                          public boolean onKey(View v, int keyCode, KeyEvent event)
                                          {
                                              if(event.getAction() == KeyEvent.ACTION_DOWN &&
                                                      (keyCode == KeyEvent.KEYCODE_ENTER))
                                              {
                                                  Toast.makeText(mContext, "Введено " +  holder.inputDataStop.getText().toString(), Toast.LENGTH_SHORT).show();

                                                  if(holder.inputDataStop.getText().toString().length() > 0)
                                                      TrafficService.stopLevel = Integer.parseInt(holder.inputDataStop.getText().toString());

                                                  InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                  imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                  holder.inputDataStop.clearFocus();
                                                  //holder.rangeBar.setTickEnd(Integer.parseInt(holder.inputDataStop.getText().toString()));
                                                  updateSet(SET_2);
                                                  return true;
                                              }
                                              return false;
                                          }
                                      }
            );
            holder.inputDataAllert.setOnKeyListener(new View.OnKeyListener()
                                    {
                                        public boolean onKey(View v, int keyCode, KeyEvent event)
                                        {
                                            if(event.getAction() == KeyEvent.ACTION_DOWN &&
                                                    (keyCode == KeyEvent.KEYCODE_ENTER) && holder.inputDataAllert.getText().toString() != null)
                                            {
                                                Toast.makeText(mContext, "Введено " +  holder.inputDataAllert.getText().toString(), Toast.LENGTH_SHORT).show();

                                                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                holder.inputDataAllert.clearFocus();

                                                if(holder.inputDataStop.getText().toString().length() > 0 && TrafficService.stopLevel < 1000 && TrafficService.stopLevel > 0) {
                                                    TrafficService.allertLevel = Integer.parseInt(holder.inputDataAllert.getText().toString());
                                                    int procent2 = 100;
                                                    if(TrafficService.stopLevel > TrafficService.allertLevel)
                                                        procent2 = TrafficService.allertLevel * 100 / TrafficService.stopLevel;
                                                    holder.rangeBar.setSeekPinByIndex(procent2);
                                                    Log.i("myLogs", "allertLevel " + TrafficService.allertLevel + " procent2 " + procent2);
                                                }
                                                updateSet(SET_2);
                                                return true;
                                            }
                                            return false;
                                        }
                                    }
                                    );


            holder.rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
                @Override
                public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                                  int rightPinIndex,
                                                  String leftPinValue, String rightPinValue) {
                    procent = ((double)rightPinIndex*0.01);
                    TrafficService.allertLevel = (int)(TrafficService.stopLevel*procent);

                    holder.inputDataAllert.setText(Integer.toString(TrafficService.allertLevel));
                    holder.procent.setText(rightPinIndex + "%");
                    Log.i("myLogs","procent " + procent + " rightPinIndex " + rightPinIndex);
                    updateSet(SET_2);
                }
            });

            holder.stopDataSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked)
                        TrafficService.disable_internet = 1;
                    else
                        TrafficService.disable_internet = 0;

                    updateSet(SET_2);
                }
            });
            holder.allertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked)
                        TrafficService.show_allert = 1;
                    else
                        TrafficService.show_allert = 0;

                    updateSet(SET_2);
                }
            });
        }
        else if(viewHolder.getItemViewType() == SET_3){
            final Set3Holder holder = (Set3Holder) viewHolder;

            holder.setupDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                            holder.viewDate.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);
                            TrafficService.day = dayOfMonth;
                            TrafficService.month = monthOfYear + 1;
                            TrafficService.mYear = year;
                            updateSet(SET_3);
                        }
                    };

                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            onDateSetListener,
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );

                    dpd.setAccentColor(Color.parseColor("#3643f4"));

                    dpd.show(((AppCompatActivity) myActivity).getFragmentManager(), "Datepickerdialog");

                }
            });
         }
        else if(viewHolder.getItemViewType() == SET_4){
            final Set4Holder holder = (Set4Holder) viewHolder;

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
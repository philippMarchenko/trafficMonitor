package com.devphill.traficMonitor.ui.fragments.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.service.TrafficService;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FragmentSettings extends Fragment {

    public String LOG_TAG = "FragmentSettings";

    public static final int SET_1 = 0;
    public static final int SET_2 = 1;
    public static final int SET_3 = 2;
    public static final int SET_4 = 3;

    @BindView(R.id.set_period)
    TextView set_period;

    @BindView(R.id.radioGroup1)
    RadioGroup radioGroup1;

    @BindView(R.id.button_month)
    RadioButton button_month;

    @BindView(R.id.button_day)
    RadioButton button_day;

    @BindView(R.id.rangebar)
    RangeBar rangebar;

    @BindView(R.id.inputDataStop)
    EditText inputDataStop;

    @BindView(R.id.inputDataAllert)
    EditText inputDataAllert;

    @BindView(R.id.procent)
    TextView procentTV;

    @BindView(R.id.stopDataSwitch)
    SwitchCompat stopDataSwitch;

    @BindView(R.id.allertSwitch)
    SwitchCompat allertSwitch;

    @BindView(R.id.setupDate)
    Button setupDate;

    @BindView(R.id.viewDate)
    TextView viewDate;

    @BindView(R.id.newDay)
    Button newDay;

    @BindView(R.id.versionApp)
    TextView versionApp;

    double procent;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
           View rootview = inflater.inflate(R.layout.fragment_settings, container, false);

           ButterKnife.bind(this,rootview);

            if(TrafficService.period == TrafficService.PERIOD_DAY) {
                button_month.setChecked(false);
                button_day.setChecked(true);
            }
            else if(TrafficService.period == TrafficService.PERIOD_MOUNTH){
                button_month.setChecked(true);
                button_day.setChecked(false);
            }


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

            viewDate.setText(TrafficService.day + "." + TrafficService.month + "." + TrafficService.mYear);

            float procentView = ((float)TrafficService.allertLevel*100)/(float)TrafficService.stopLevel;
            procentView = Math.round(procentView);
            procentTV.setText(procentView + "%");

        try{
            rangebar.setSeekPinByValue(procentView);
        }
        catch(IllegalArgumentException e){

        }

            newDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TrafficService.newDay = true;
                    TrafficService.newMonth = true;

                }
            });

            versionApp.setText("Версия приложения " + BuildConfig.VERSION_NAME);





        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case -1:
                        //Toast.makeText(mContext, "Нихуя", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.button_day:
                       // Toast.makeText(getContext(), "Выбран день", Toast.LENGTH_SHORT).show();
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


        inputDataStop.setOnKeyListener(new View.OnKeyListener()
                                       {
                                           public boolean onKey(View v, int keyCode, KeyEvent event)
                                           {
                                               if(event.getAction() == KeyEvent.ACTION_DOWN &&
                                                       (keyCode == KeyEvent.KEYCODE_ENTER))
                                               {
                                                   Toast.makeText(getContext(), "Введено " +  inputDataStop.getText().toString(), Toast.LENGTH_SHORT).show();

                                                   if(inputDataStop.getText().toString().length() > 0)
                                                       TrafficService.stopLevel = Integer.parseInt(inputDataStop.getText().toString());

                                                   InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                   imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                   inputDataStop.clearFocus();
                                                   //holder.rangeBar.setTickEnd(Integer.parseInt(holder.inputDataStop.getText().toString()));
                                                   updateSet(SET_2);
                                                   return true;
                                               }
                                               return false;
                                           }
                                       }
        );

        inputDataAllert.setOnKeyListener(new View.OnKeyListener()
                                                {
                                                    public boolean onKey(View v, int keyCode, KeyEvent event)
                                                    {
                                                        if(event.getAction() == KeyEvent.ACTION_DOWN &&
                                                                (keyCode == KeyEvent.KEYCODE_ENTER) && inputDataAllert.getText().toString() != null)
                                                        {
                                                            Toast.makeText(getContext(), "Введено " +  inputDataAllert.getText().toString(), Toast.LENGTH_SHORT).show();

                                                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                                            inputDataAllert.clearFocus();

                                                            if(inputDataStop.getText().toString().length() > 0 && TrafficService.stopLevel < 1000 && TrafficService.stopLevel > 0) {
                                                                TrafficService.allertLevel = Integer.parseInt(inputDataAllert.getText().toString());
                                                                int procent2 = 100;
                                                                if(TrafficService.stopLevel > TrafficService.allertLevel)
                                                                    procent2 = TrafficService.allertLevel * 100 / TrafficService.stopLevel;
                                                                rangebar.setSeekPinByIndex(procent2);
                                                                Log.i("myLogs", "allertLevel " + TrafficService.allertLevel + " procent2 " + procent2);
                                                            }
                                                            updateSet(SET_2);
                                                            return true;
                                                        }
                                                        return false;
                                                    }
                                                }
        );


        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                procent = ((double)rightPinIndex*0.01);
                TrafficService.allertLevel = (int)(TrafficService.stopLevel*procent);

                inputDataAllert.setText(Integer.toString(TrafficService.allertLevel));
                procentTV.setText(rightPinIndex + "%");
                Log.i("myLogs","procent " + procent + " rightPinIndex " + rightPinIndex);
                updateSet(SET_2);
            }
        });

        stopDataSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                    TrafficService.disable_internet = 1;
                else
                    TrafficService.disable_internet = 0;

               // updateSet(SET_2);
                Log.i(LOG_TAG,"stopDataSwitch" + TrafficService.disable_internet);
                updateSetValue(TrafficService.APP_PREFERENCES_DISABLE_INTERNET,TrafficService.disable_internet);

            }
        });
        allertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                    TrafficService.show_allert = 1;
                else
                    TrafficService.show_allert = 0;

               // updateSet(SET_2);
                Log.i(LOG_TAG,"allertSwitch" + TrafficService.show_allert);

                updateSetValue(TrafficService.APP_PREFERENCES_SHOW_ALLERT,TrafficService.show_allert);
            }
        });

        setupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        viewDate.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);
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

                dpd.show(((AppCompatActivity) getActivity()).getFragmentManager(), "Datepickerdialog");

            }
        });

           Log.i(LOG_TAG,"onCreateView Fragment set");

           return  rootview;
       }

    public void updateSet(int setId){

        SharedPreferences mySharedPreferences = getContext().getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        if(setId == SET_1){
            editor.putInt(TrafficService.APP_PREFERENCES_PERIOD, TrafficService.period);
        }
        else if(setId == SET_2){
            editor.putInt(TrafficService.APP_PREFERENCES_ALLERT_LEVEL, TrafficService.allertLevel);
            editor.putInt(TrafficService.APP_PREFERENCES_STOPL_EVEL, TrafficService.stopLevel);
            editor.putInt(TrafficService.APP_PREFERENCES_SHOW_ALLERT, TrafficService.show_allert);
            editor.putInt(TrafficService.APP_PREFERENCES_DISABLE_INTERNET, TrafficService.disable_internet);
        }
        else if(setId == SET_3){
            editor.putInt(TrafficService.APP_PREFERENCES_DAY, TrafficService.day);
            editor.putInt(TrafficService.APP_PREFERENCES_MONTH, TrafficService.month);
            editor.putInt(TrafficService.APP_PREFERENCES_YEAR, TrafficService.mYear);
        }

        editor.apply();


    }

    public void updateSetValue(String key,int value){

        Log.i(LOG_TAG,"updateSetValue key " + key + " value" + value);


        SharedPreferences mySharedPreferences = getContext().getSharedPreferences(TrafficService.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();

        editor.putInt(key, value);

        editor.apply();


    }
}

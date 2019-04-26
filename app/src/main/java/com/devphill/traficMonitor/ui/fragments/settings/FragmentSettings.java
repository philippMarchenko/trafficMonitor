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
import com.devphill.traficMonitor.App;
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


            if(App.dataManager.getPeriod() == TrafficService.PERIOD_DAY) {
                button_month.setChecked(false);
                button_day.setChecked(true);
            }
            else if(App.dataManager.getPeriod() == TrafficService.PERIOD_MOUNTH){
                button_month.setChecked(true);
                button_day.setChecked(false);
            }


            inputDataStop.setText(App.dataManager.getStopLevel().toString());
            inputDataAllert.setText(App.dataManager.getAlertLevel().toString());

            allertSwitch.setChecked(App.dataManager.isShowAlert());
            stopDataSwitch.setChecked(App.dataManager.isDisableConectionWhenLimit());

            viewDate.setText(App.dataManager.getDate());

            float procentView = ( (float) App.dataManager.getAlertLevel() * 100) / (float) App.dataManager.getStopLevel();
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
                        break;
                    case R.id.button_day:
                        App.dataManager.setPeriod(TrafficService.PERIOD_DAY);
                        break;
                    case R.id.button_month:
                        App.dataManager.setPeriod(TrafficService.PERIOD_MOUNTH);
                        break;
                }
            }
        });


        inputDataStop.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if(event.getAction() == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    if(inputDataStop.getText().toString().length() > 0){
                        App.dataManager.setStopLevel(Integer.parseInt(inputDataStop.getText().toString()));
                    }

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    inputDataStop.clearFocus();
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
                 //   Toast.makeText(getContext(), "Введено " +  inputDataAllert.getText().toString(), Toast.LENGTH_SHORT).show();

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    inputDataAllert.clearFocus();

                    int stopLevel = App.dataManager.getStopLevel();

                    if(inputDataStop.getText().toString().length() > 0 && stopLevel < 1000 && stopLevel > 0) {
                        int alertLevel = Integer.parseInt(inputDataAllert.getText().toString());

                        App.dataManager.setAlertLevel(alertLevel);
                        int procent2 = 100;
                        if(stopLevel > alertLevel)
                            procent2 = alertLevel * 100 / stopLevel;
                        rangebar.setSeekPinByIndex(procent2);
                       // Log.i("myLogs", "allertLevel " + TrafficService.allertLevel + " procent2 " + procent2);
                    }
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
                int alertLevel = (int)(App.dataManager.getStopLevel() * procent);

                App.dataManager.setAlertLevel(alertLevel);

                inputDataAllert.setText(Integer.toString(alertLevel));
                procentTV.setText(rightPinIndex + "%");

                Log.i("myLogs","procent " + procent + " rightPinIndex " + rightPinIndex);
            }
        });

        stopDataSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                App.dataManager.setDisableConectionWhenLimit(isChecked);

            }
        });
        allertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                App.dataManager.setIsShowAlert(isChecked);
            }
        });

        setupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        viewDate.setText(year + "-" + (monthOfYear + 1) + dayOfMonth);
                        App.dataManager.setDate(viewDate.getText().toString());
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


}

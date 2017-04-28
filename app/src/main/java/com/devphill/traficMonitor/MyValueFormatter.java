package com.devphill.traficMonitor;

import android.content.Context;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;

public class MyValueFormatter implements YAxisValueFormatter {

    private DecimalFormat mFormat;
    Context mContext;

    public MyValueFormatter(Context context) {
        mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        mContext = context;
    }



    @Override
    public String getFormattedValue(float value, YAxis yAxis) {
        return mFormat.format(value) + " " + mContext.getResources().getString(R.string.mb); // e.g. append a dollar-sign;
    }
}
package com.devphill.traficMonitor.helper;

import android.content.Context;

import com.devphill.traficMonitor.R;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

public class MyValueFormatterPie implements ValueFormatter {

    private DecimalFormat mFormat;
    Context mContext;

    public MyValueFormatterPie(Context context) {

        mFormat = new DecimalFormat("###,###,##0.00"); // use two decimal if needed
        mContext = context;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        // write your logic here
        return mFormat.format(value) + " " + mContext.getResources().getString(R.string.mb); // e.g. append a dollar-sign
    }
}
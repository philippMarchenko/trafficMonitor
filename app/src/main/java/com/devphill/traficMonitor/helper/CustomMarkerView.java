package com.devphill.traficMonitor.helper;

import android.content.Context;
import android.widget.TextView;

import com.devphill.traficMonitor.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.ArrayList;

public class CustomMarkerView extends MarkerView {

    private TextView tvData,tvDateTime;
    ArrayList<String> mlabelsLineChartDate;
    Context mContex;
    Entry mEntry;
    Context mContext;
    public CustomMarkerView (Context context, int layoutResource,ArrayList<String> labelsLineChartDate) {
        super(context, layoutResource);
        // this markerview only displays a textview
        tvData = (TextView) findViewById(R.id.tvData);
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        mlabelsLineChartDate = labelsLineChartDate;
        mContext = context;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        tvData.setText("" + e.getVal() + " " +  mContext.getResources().getString(R.string.mb)); // set the entry-value as the display text
        tvDateTime.setText("" + mlabelsLineChartDate.get(e.getXIndex())); // set the entry-value as the display text
        mEntry = e;
      //  Object o = e.getData();
       // highlight.ge
    }

    @Override
    public int getXOffset() {

        if(mEntry.getXIndex() == (mlabelsLineChartDate.size() - 1))
            return -(getWidth());
        else
            return 0;
    }


    @Override
    public int getYOffset() {
        if(mEntry.getXIndex() < 5)
            return -(getHeight());
        else
            return 0;
    }

 }
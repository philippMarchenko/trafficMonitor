package com.devphill.traficMonitor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devphill.traficMonitor.R;

public class FragmentSettings extends Fragment {

    public static final int SET_1 = 0;
    public static final int SET_2 = 1;
    public static final int SET_3 = 2;
    public static final int SET_4 = 3;



        public String LOG_TAG = "myLogs";


        private SettingAdapter settingAdapter;
        private RecyclerView recyclerView;


    private int mDatasetTypes[] = {SET_1, SET_2,SET_3,SET_4}; //view types

    private String[] mDataset = {"1", "2", "3","4"};

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
           View rootview = inflater.inflate(R.layout.fragment_settings, container, false);

           recyclerView = (RecyclerView) rootview.findViewById(R.id.recycler_view);
          // recyclerView.setHasFixedSize(true);
           settingAdapter = new SettingAdapter(getContext(),getActivity(),mDataset,mDatasetTypes);
           RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
           recyclerView.setLayoutManager(mLayoutManager);
           recyclerView.setItemAnimator(new DefaultItemAnimator());
           recyclerView.setAdapter(settingAdapter);

           Log.i(LOG_TAG,"onCreateView Fragment set");

           return  rootview;
       }


}

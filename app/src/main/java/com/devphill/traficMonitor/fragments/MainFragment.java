package com.devphill.traficMonitor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.TrafficService;
import com.devphill.traficMonitor.Widget;

public class MainFragment extends Fragment {

    public static final int CARD_1 = 0;
    public static final int CARD_2 = 1;
    public static final int CARD_3 = 2;
    public static final int CARD_4 = 3;

    public  String LOG_TAG = "mainFragmentTag";

    private int mDatasetTypes[] = {CARD_1, CARD_2,CARD_3,CARD_4}; //view types
    private String[] mDataset = {"1", "2", "3","4"};

    private MainFragmentAdapter mainFragmentAdapter;
    private RecyclerView recyclerView;

    Widget widget = new Widget();

   // static CustomViewPager mPager;

    static boolean runTimerFragment2 = true;
    private int currentPosition;
/*    public MainFragment(CustomViewPager viewPager){
        mPager = viewPager;
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);



        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_main_fragment);


        Log.i(LOG_TAG, "MainFragment onCreateView");

        return view;
    }


    public void onResume() {
        super.onResume();
        runTimerFragment2 = true;
        Log.i(LOG_TAG, "MainFragment onResume");
       /* if(TrafficService.brRegistered == false) {
            getActivity().registerReceiver(MainFragmentAdapter.br, new IntentFilter(MainFragmentAdapter.BROADCAST_ACTION));
            TrafficService.brRegistered = true;
        }*/

        GridLayoutManager manager = new GridLayoutManager(getContext(), 2, GridLayout.VERTICAL, false);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){

            @Override
            public int getSpanSize(int position) {
                return (position % 3 == 0 ? 2 : 1);
            }
        });
        // recyclerView.setHasFixedSize(true);
        mainFragmentAdapter = new MainFragmentAdapter(getContext(), getActivity(), mDataset, mDatasetTypes);
        // RecyclerView.LayoutManager mLayoutManager = new MyLayoutManager();
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mainFragmentAdapter);

    }

    @Override
    public void onPause() {
        super.onPause();

        runTimerFragment2 = false;
        Log.i(LOG_TAG, "MainFragment onPause");

        if( TrafficService.brRegistered){
               try {
           getActivity().unregisterReceiver(MainFragmentAdapter.br);
           TrafficService.brRegistered = false;
        }catch (IllegalArgumentException e){
            Log.d(LOG_TAG, "Error unregisterReceiver in MainFragment" + e.getMessage());
        }
        }

    }
    @Override
    public void onDestroy() {
        runTimerFragment2 = false;
        super.onDestroy();
        Log.i(LOG_TAG, "MainFragment onDestroy");
        if (TrafficService.brRegistered == true) {
            try {
                getActivity().unregisterReceiver(MainFragmentAdapter.br);
                TrafficService.brRegistered = false;
            } catch (IllegalArgumentException e) {
                Log.d(LOG_TAG, "Error unregisterReceiver" + e.getMessage());
            }
        }
    }

}

package com.devphill.traficMonitor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devphill.traficMonitor.R;


public class Fragment3 extends Fragment {

    public static String LOG_TAG = "myLogs";

    private TestSpeedAdapter testSpeedAdapter;
    private RecyclerView recyclerView;

    public static final int CARD_1 = 0;
    public static final int CARD_2 = 1;
    public static final int CARD_3 = 2;
    public static final int CARD_4 = 3;

    private int mDatasetTypes[] = {CARD_1, CARD_2,CARD_3,CARD_4}; //view types
    private String[] mDataset = {"1", "2", "3","4"};

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View roorview = inflater.inflate(R.layout.test_speed_fragment, container, false);

        recyclerView = (RecyclerView) roorview.findViewById(R.id.recycler_view_test_speed);
        testSpeedAdapter = new TestSpeedAdapter(getContext(),getActivity(),mDataset,mDatasetTypes);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(testSpeedAdapter);

      /*

        stopIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setMobileDataEnabled(getContext(),false);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });
*/
        return roorview;
    }




}

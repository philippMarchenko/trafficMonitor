package com.devphill.traficMonitor;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class StateSavingArrayAdapter<T extends Parcelable> extends
        ArrayAdapter<T> {

    private static final String KEY_ADAPTER_STATE = "StateSavingArrayAdapter.KEY_ADAPTER_STATE";

    /**
     * Saves the instance state of this {@link ArrayAdapter}. It saves the array
     * of currently managed by this adapter
     *
     * @param outState
     *          The bundle into which the state is saved
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_ADAPTER_STATE, getAllItems());
    }

    /**
     * Restore the instance state of the {@link ArrayAdapter}. It re-initializes
     * the array of objects being managed by this adapter with the state retrieved
     * from {@code savedInstanceState}
     *
     * @param savedInstanceState
     *          The bundle containing the previously saved state
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_ADAPTER_STATE)) {
            ArrayList<T> objects = savedInstanceState
                    .getParcelableArrayList(KEY_ADAPTER_STATE);
            setItems(objects);
        }
    }

    /**
     * Gets all the items in this adapter as an {@code ArrayList}
     */
    public ArrayList<T> getAllItems(){
        ArrayList<T> objects = new ArrayList<T>(getCount());
        for (int i = 0; i < getCount(); i++) {
            objects.add(getItem(i));
        }
        return objects;
    }

    /*
     * Replaces the items in the adapter with those in this list
     * @param items The items to set into the adapter.
     */
    public void setItems(ArrayList<T> items){
        clear();
        addAll(items);
    }

  /* Constructors and other boilerplate */

    public StateSavingArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public StateSavingArrayAdapter(Context context, int resource,
                                   int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public StateSavingArrayAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    public StateSavingArrayAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    public StateSavingArrayAdapter(Context context, int resource,
                                   int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public StateSavingArrayAdapter(Context context, int resource,
                                   int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

}
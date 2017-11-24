package com.devphill.traficMonitor.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.model.Package;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppTrafficAdapter extends RecyclerView.Adapter<AppTrafficAdapter.MyViewHolder> {

    private List<Package> packageList;
    private Context mContext;
    private boolean sortMobile = true;


    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView wiFiusageApp,mobileUsageApp,tvAppName;
        private ImageView image;

        public MyViewHolder(View v) {
            super(v);
            this.wiFiusageApp = (TextView) v.findViewById(R.id.wiFiusageApp);
            this.mobileUsageApp = (TextView) v.findViewById(R.id.mobileUsageApp);
            this.tvAppName = (TextView) v.findViewById(R.id.tvAppName);
            this.image = (ImageView) v.findViewById(R.id.imageApp);

        }
    }

    public AppTrafficAdapter(Context context, List<Package> list) {

        mContext = context;
        packageList = list;
    }

        @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_application, parent, false);
            return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Package mPackage = packageList.get(position);

        holder.mobileUsageApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sortMobile) {
                    Toast.makeText(mContext, "Сортировка по мобильному траффику.", Toast.LENGTH_SHORT).show();
                    sortMobile = true;
                    updateAdapter();
                }
            }
        });

        holder.wiFiusageApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sortMobile){
                    Toast.makeText(mContext, "Сортировка по WiFi.", Toast.LENGTH_SHORT).show();
                    sortMobile = false;
                    updateAdapter();
                }


            }
        });

        final int iconSize = Math.round(48 * mContext.getResources().getDisplayMetrics().density);
        holder.image.setImageDrawable(new BitmapDrawable(mContext.getResources(), Bitmap.createScaledBitmap(
                 mPackage.getIcon(), iconSize, iconSize, true)
           ));

        holder.tvAppName.setText(mPackage.getName());

        holder.mobileUsageApp.setText(mPackage.getMobileData() + " mB");
        holder.wiFiusageApp.setText(mPackage.getWiFiData() + " mB");
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    public void updateAdapter(){

        if(sortMobile) {
            Collections.sort(packageList, new Comparator<Package>() {
                @Override
                public int compare(Package lhs, Package rhs) {

                    return (int) ((Float.parseFloat(rhs.getMobileData()) - Float.parseFloat(lhs.getMobileData())) * 10);
                }
            });
        }
        else{

            Collections.sort(packageList, new Comparator<Package>() {
                @Override
                public int compare(Package lhs, Package rhs) {

                    return (int) ((Float.parseFloat(rhs.getWiFiData()) - Float.parseFloat(lhs.getWiFiData())) * 10);
                }
            });

        }

        notifyDataSetChanged();
    }

    public void addItem(Package p){
        packageList.add(p);
        notifyItemInserted(packageList.size() - 1);
    }

}


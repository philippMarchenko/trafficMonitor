package com.devphill.traficMonitor.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.premmision.Premission;
import com.devphill.traficMonitor.service.TrafficService;
import com.devphill.traficMonitor.helper.CustomViewPager;
import com.devphill.traficMonitor.ui.fragments.test_speed.FragmentTestSpeed;
import com.devphill.traficMonitor.ui.fragments.app_traffic.FragmentTrafficApps;
import com.devphill.traficMonitor.ui.fragments.settings.FragmentSettings;
import com.devphill.traficMonitor.ui.fragments.main.MainFragment;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public  String LOG_TAG = "MainActivity";


    private Drawer.Result drawerResult = null;
    private AccountHeader.Result headerResult = null;
    public String string;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    public static CustomViewPager viewPager;


    AlertDialog dialog;
    AlertDialog.Builder builder;
    Premission premission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Для Activity с боковым меню ставьте эту тему,
        // для Activity без бокового меню ставьте тему AppThemeNonDrawer (она прописана по умолчанию в манифесте кстати)
        // иначе будет "сползать" ActionBar
        // Темы находятся в styles.xml
      //  setTheme(R.style.AppThemeNonDrawer);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        premission = new Premission(getBaseContext(),this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().hide();

        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            //noinspection ConstantConditions
            TextView tv=(TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab_tv,null);
            Typeface typefaceR = Typeface.createFromAsset(getBaseContext().getAssets(),
                    "fonts/UbuntuMono-R.ttf");
            tv.setTypeface(typefaceR);
            tabLayout.getTabAt(i).setCustomView(tv);

        }
        Log.i(LOG_TAG, "onCreate " );

    }
    public void showQueryPremission(){

        builder = new AlertDialog.Builder(this,R.style.MyDialogTheme);
        builder.setTitle("Необходимо разрешение!");
        builder.setMessage("Этому приложению необходимо предоставить разрешение на просмотр данных сети");

        builder.setPositiveButton("Предоставить!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        premission.requestReadNetworkHistoryAccess();
                    }
                });

        builder.setNegativeButton("Отмена!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });


            dialog = builder.create();
            dialog.show();
        Log.i(LOG_TAG, "showQueryPremission" );

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart " );

        premission.requestPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause " );
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onResume() {
        Log.i(LOG_TAG, "onResume " );

        super.onResume();
        if (!premission.hasPermissions()) {
            Log.i(LOG_TAG, "return " );

            if (dialog == null) {
                showQueryPremission();
            }

            return;
        }
        
        setupViewPager(viewPager);

        if(!isMyServiceRunning(TrafficService.class)) {
            Intent intent = new Intent(this, TrafficService.class);
            startService(intent);
        }

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void setupViewPager(CustomViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainFragment(), getResources().getString(R.string.tab_1));
        adapter.addFragment(new FragmentTrafficApps(),getResources().getString(R.string.app_data));
        adapter.addFragment(new FragmentTestSpeed(), getResources().getString(R.string.tab_2));
        adapter.addFragment(new FragmentSettings(),getResources().getString(R.string.tab_3));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}

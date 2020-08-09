package com.devphill.traficMonitor.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.devphill.traficMonitor.R;
import com.devphill.traficMonitor.premmision.Permission;
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


    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        bottomNavigationView =  findViewById(R.id.bottom_navigation);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {

        super.onResume();
        initUI();

    }

    private void initUI(){
        setupViewPager(viewPager);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_all_traffic:{
                                viewPager.setCurrentItem(0);
                                return true;
                            }

                            case R.id.action_apps_traffic:{
                                viewPager.setCurrentItem(1);
                                return true;
                            }

                            case R.id.action_speed_test:{
                                viewPager.setCurrentItem(2);
                                return true;
                            }

                            case R.id.action_settings:{
                                viewPager.setCurrentItem(3);
                                return true;
                            }

                        }
                        return true;
                    }
                });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {

                switch (position){
                    case 0:{
                        bottomNavigationView.getMenu().findItem(R.id.action_all_traffic).setChecked(true);
                        break;
                    }
                    case 1:{
                        bottomNavigationView.getMenu().findItem(R.id.action_apps_traffic).setChecked(true);
                        break;
                    }
                    case 2:{
                        bottomNavigationView.getMenu().findItem(R.id.action_speed_test).setChecked(true);
                        break;
                    }
                    case 3:{
                        bottomNavigationView.getMenu().findItem(R.id.action_settings).setChecked(true);
                        break;
                    }
                }
            }
        });

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
        viewPager.setOffscreenPageLimit(4);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
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

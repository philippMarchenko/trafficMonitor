package com.devphill.traficMonitor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.devphill.traficMonitor.fragments.CustomViewPager;
import com.devphill.traficMonitor.fragments.FragmentTestSpeed;
import com.devphill.traficMonitor.fragments.FragmentTrafficApps;
import com.devphill.traficMonitor.fragments.FragmentTestSpeed;
import com.devphill.traficMonitor.fragments.FragmentSettings;
import com.devphill.traficMonitor.fragments.MainFragment;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private Drawer.Result drawerResult = null;
    private AccountHeader.Result headerResult = null;
    public String string;


    private Toolbar toolbar;
    private TabLayout tabLayout;
    public static CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Для Activity с боковым меню ставьте эту тему,
        // для Activity без бокового меню ставьте тему AppThemeNonDrawer (она прописана по умолчанию в манифесте кстати)
        // иначе будет "сползать" ActionBar
        // Темы находятся в styles.xml
        setTheme(R.style.AppThemeNonDrawer);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().hide();

        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if(!isMyServiceRunning(TrafficService.class)){
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
      //  adapter.addFragment(new FragmentTrafficApps(),getResources().getString(R.string.app_data));
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

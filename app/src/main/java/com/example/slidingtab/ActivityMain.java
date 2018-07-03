package com.example.slidingtab;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;

public class ActivityMain extends AppCompatActivity implements MainFragmentCalendar.OnFragmentInteractionListener, MainFragmentOwnPlants.OnFragmentInteractionListener{

    private static String ALARM = "ALARM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText("Kalender"));//MainFragmentCalendar
        //tabLayout.addTab(tabLayout.newTab().setText("Suche")); //MainFragmentSearch
        tabLayout.addTab(tabLayout.newTab().setText("Deine Pflanzen"));  //Your Plants
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        boolean isAlarm = (PendingIntent.getBroadcast(this, 0, new Intent(ALARM), PendingIntent.FLAG_NO_CREATE) == null);

        if(isAlarm){
            Intent IAlarm = new Intent(ALARM);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, IAlarm, 0);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.SECOND, 3);
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60000, pendingIntent);
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final MainPagerAdapter adpater = new MainPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adpater);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

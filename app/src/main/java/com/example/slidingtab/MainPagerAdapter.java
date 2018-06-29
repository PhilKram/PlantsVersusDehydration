package com.example.slidingtab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MainPagerAdapter extends FragmentStatePagerAdapter {

    int mNumberOfTabs;

    public MainPagerAdapter(FragmentManager fm, int NumberOfTabs){
        super(fm);
        this.mNumberOfTabs = NumberOfTabs;
    }

    @Override
    public Fragment getItem (int position){
        switch(position){
            case 0:
                MainFragmentCalendar mainFragmentCalendar = new MainFragmentCalendar();
                return mainFragmentCalendar;
            case 1:
                MainFragmentOwnPlants mainFragmentOwnPlants = new MainFragmentOwnPlants();
                return mainFragmentOwnPlants;
            default:
                return null;

        }

    }

    @Override
    public int getCount(){
        return mNumberOfTabs;
    }
}

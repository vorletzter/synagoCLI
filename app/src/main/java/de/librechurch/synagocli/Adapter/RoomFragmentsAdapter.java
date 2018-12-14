package de.librechurch.synagocli.Adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

import de.librechurch.synagocli.Fragments.RoomFragment;


// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
//public class RoomListsAdapter extends FragmentStatePagerAdapter {
public class RoomFragmentsAdapter extends FragmentPagerAdapter {

    private int numberOfPages;
    private ArrayList sessions;

    public RoomFragmentsAdapter(FragmentManager fm, ArrayList sessions) {
        super(fm);
        this.numberOfPages = sessions.size();
        this.sessions = sessions;
    }

    @Override
    public Fragment getItem(int position) {
        RoomFragment fragment = RoomFragment.newInstance(this.sessions.get(position).toString());
        return fragment;
    }


    @Override
    public int getCount() {
        return this.numberOfPages;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return sessions.get(position).toString();
    }
}

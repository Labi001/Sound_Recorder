package com.labinot.sound_recorder.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.labinot.sound_recorder.fragments.FileViewerFragment;
import com.labinot.sound_recorder.fragments.RecordFragment;

import java.util.ArrayList;
import java.util.List;

public class MyViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();

    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragments) {

        mFragmentList.add(fragments);
    }

    public Fragment getFragment(int position) {
        return mFragmentList.get(position);
    }
}

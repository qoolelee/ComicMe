package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ComicFragmentStateAdapter extends FragmentStateAdapter {
    public static Integer NUM_TABS = 2;

    public ComicFragmentStateAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int)
        Fragment fragment = AllComicFiltersFragment.newInstance("","");

        switch (position){
            case 0:
                break;
            default: // 1
                fragment = HistoricalComicFragment.newInstance("","");
                break;
        }

        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }

}

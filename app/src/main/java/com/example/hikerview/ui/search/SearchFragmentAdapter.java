package com.example.hikerview.ui.search;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hdy on 2017/5/23.
 */

public class SearchFragmentAdapter extends FragmentPagerAdapter {
    private int fragmentSize;
    private FragmentManager fragmentManager;
    private Fragment[] fragments = {new SearchFragment(), new SearchFragment(), new SearchFragment()};
    private Map<Integer, Integer> fragmentPosMap = new HashMap<>();

    public SearchFragmentAdapter(FragmentManager fm, int sizeAll) {
        super(fm);
        this.fragmentSize = sizeAll;
        this.fragmentManager = fm;
        fragmentPosMap.put(0, 0);
        fragmentPosMap.put(1, 1);
        fragmentPosMap.put(2, 2);
        for (int i = 3; i < sizeAll; i++) {
            fragmentPosMap.put(i, -1);
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return fragments[0];
        } else if (position == fragmentSize - 1) {
            return fragments[2];
        }
        return fragments[1];
    }

    @Override
    public int getCount() {
        return fragmentSize;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (fragmentPosMap.get(position) == -1) {
            //todo 复用
        }
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragmentManager.beginTransaction().show(fragment).commit();
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //                super.destroyItem(container, position, object);
        Fragment fragment = fragments[position];
        fragmentManager.beginTransaction().hide(fragment).commit();
    }

}
package com.example.bloothcontroler.ui.notifications;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * @author : WangHao
 * @date : 2020.4.15
 * @desc :
 * @version:
 */
public class MyPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;
    private List<Fragment> list;

    public MyPagerAdapter(Context context, List<Fragment> list, @NonNull FragmentManager fm) {
        super(fm);
        this.context = context;
        this.list = list;
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }
}

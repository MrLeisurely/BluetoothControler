package com.example.bloothcontroler.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * @author : WangHao
 * @date : 2020.4.15
 * @desc :
 * @version:
 */
public abstract class LazyFragment extends Fragment {

    /**
     * 懒加载
     */
    private boolean createView = false;
    private boolean activityCreated = false;
    private boolean lazy = false;
    /**
     * 这个Fragment是不是tab页面的第一个页面
     */
    private boolean isFirstTab = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        createView = true;
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activityCreated = true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && createView && activityCreated && !lazy) {
            //不是第一个Tab的Fragment 进行懒加载请求数据
            lazy = true;
            lazyLoad();
        } else if (isVisibleToUser && !createView && !activityCreated && !lazy) {
            //这个Fragment是多个Tab中的第一个
            isFirstTab = true;
        } else {
            //对用户可见时，是否需要重新刷新数据
            if (isVisibleToUser) {
                visibleToUser();
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
        } else {
            visibleToUser();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * 第一个Tab页面懒加载
         */
        if (isFirstTab && !lazy) {
            lazy = true;
            lazyLoad();
        }
    }

    /**
     * 每次在Fragment与用户可见状态且不是第一次对用户可见
     * 使用场景：使用add hide() show()方法切换fragment,fragment结合viewpager使用
     */
    public abstract void visibleToUser();

    /**
     * 懒加载，只有在Fragment第一次创建且第一次对用户可见
     */
    public abstract void lazyLoad();

}

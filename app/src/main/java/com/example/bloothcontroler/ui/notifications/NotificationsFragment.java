package com.example.bloothcontroler.ui.notifications;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.FragmentNotificationsBinding;
import com.example.bloothcontroler.databinding.FragmentNotificationsBinding;
import com.example.bloothcontroler.service.DataMessage;
import com.example.bloothcontroler.ui.widget.FontTabItem;
import com.example.bloothcontroler.ui.widget.YWLoadingDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import com.example.bloothcontroler.service.OrderCreater;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private String[] titles = new String[]{"PV1", "PV2", "PV3", "PV4"};
    private int currentIndex = 0;
    private List<Fragment> fragmentList = new ArrayList<>();
    private FragmentNotificationsBinding binding;
    private YWLoadingDialog dialog;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false);
        notificationsViewModel.getText().observe(this, new Observer<DataMessage>() {
            @Override
            public void onChanged(@Nullable DataMessage s) {
                handleMessage(s);
            }
        });
//        notificationsViewModel.startCover(OrderCreater.generalReadOrder(OrderCreater.Voc_of_String, 6),2000);
//        notificationsViewModel.startCover(OrderCreater.setDefault(),3000);
        return binding.getRoot();
    }

    private void handleMessage(DataMessage message) {
        if (null != message) {
            switch (message.what) {
                case DataMessage.RECEVED_IV_PV1_DATA:
                case DataMessage.RECEVED_IV_PV2_DATA:
                case DataMessage.RECEVED_IV_PV3_DATA:
                case DataMessage.RECEVED_IV_PV4_DATA:
                    if (dialog.isShowing()){
                        dialog.dismiss();
                    }
                    break;
                case DataMessage.RECEVED_IV_DATA:
                    int[] data = message.getData();
                    if (data.length > 5) {
                        binding.tvVoc.setText(String.valueOf(notificationsViewModel.getValue(data[0])));
                        binding.tvIrr.setText(String.valueOf(notificationsViewModel.getValue(data[1])));
                        binding.tvTemp.setText(String.valueOf(notificationsViewModel.getValue(data[2])));
                        binding.edPm.setText(String.valueOf(notificationsViewModel.getValue(data[3], 0.01)));
                        binding.edVoc.setText(String.valueOf(notificationsViewModel.getValue(data[4], 0.01)));
                        binding.edIsc.setText(String.valueOf(notificationsViewModel.getValue(data[5], 0.01)));
                    }
                    break;

            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dialog = new YWLoadingDialog(getContext());
        binding.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notificationsViewModel.isBTConnected()) {
                    if (TextUtils.isEmpty(binding.edPm.getText())
                            || TextUtils.isEmpty(binding.edVoc.getText())
                            || TextUtils.isEmpty(binding.edIsc.getText())
                    ) {
                        showMsg("请输入数据");
                        return;
                    }
                    double pamx = Double.parseDouble(binding.edPm.getText().toString());
                    double vmp = Double.parseDouble(binding.edVoc.getText().toString());
                    double imp = Double.parseDouble(binding.edIsc.getText().toString());

                    int p = (int) (pamx * 10);
                    int v = (int) (vmp * 10);
                    int i = (int) (imp * 10);
                    byte[] editOrder = OrderCreater.getWriteDataOrder(OrderCreater.T_of_Pm, 3,
                            p,
                            v,
                            i
                    );
                    notificationsViewModel.sendOrder(editOrder);
                }
            }
        });
        binding.imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notificationsViewModel.isBTConnected()){
                    notificationsViewModel.sendOrder(OrderCreater.generalReadOrder(OrderCreater.Voc_of_String, 6));
                }
            }
        });
        for (int i = 0; i < titles.length;i++) {
            int index = DataMessage.RECEVED_IV_PV1_DATA;
            if (i==0){
                index = DataMessage.RECEVED_IV_PV1_DATA;
            } else if (i == 1){
                index = DataMessage.RECEVED_IV_PV2_DATA;
            }else if (i == 2){
                index = DataMessage.RECEVED_IV_PV3_DATA;
            }else if (i == 3){
                index = DataMessage.RECEVED_IV_PV4_DATA;
            }
            fragmentList.add(LineChartFragment.getInstance(titles[i],index));
            String title = titles[i];
            TabLayout.Tab tab = binding.tabLayout.newTab();
            FontTabItem tv_title = new FontTabItem(getContext());
            if (i==0){
                tv_title.checked();
            } else {
                tv_title.unCheck();
            }
            tv_title.setTitle(title);
            tab.setCustomView(tv_title);
            binding.tabLayout.addTab(tab);
        }
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getContext(), fragmentList, getChildFragmentManager());
        binding.viewPager.setAdapter(pagerAdapter);
//        for (String title : titles) {
//            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
//        }

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = binding.tabLayout.getTabAt(position);
                if (null!=tab)
                    tab.select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
                currentIndex = tab.getPosition();
                notificationsViewModel.setSplitOrder(OrderCreater.getSplitOrder(tab.getPosition()));
                if (currentIndex==0){
                    notificationsViewModel.setPageTag(DataMessage.PAGE_IV_PV1_DATA);
                }
                else if (currentIndex==1){
                    notificationsViewModel.setPageTag(DataMessage.PAGE_IV_PV2_DATA);
                }
                else if (currentIndex==2){
                    notificationsViewModel.setPageTag(DataMessage.PAGE_IV_PV3_DATA);
                }
                else if (currentIndex==3){
                    notificationsViewModel.setPageTag(DataMessage.PAGE_IV_PV4_DATA);
                }
                dialog.show();

                FontTabItem cu = (FontTabItem) tab.getCustomView();
                if (null!=cu){
                    cu.checked();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                FontTabItem cu = (FontTabItem) tab.getCustomView();
                if (null!=cu){
                    cu.unCheck();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }



    private void showMsg(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        notificationsViewModel.setReady(true);
        notificationsViewModel.sendOrder(OrderCreater.getPVData(currentIndex));
    }

    @Override
    public void onStop() {
        super.onStop();
        notificationsViewModel.setReady(false);
    }
}
package com.example.bloothcontroler.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.FragmentHomeBinding;
import com.example.bloothcontroler.le.DeviceScanActivity;
import com.example.bloothcontroler.service.BluetoothDataIOServer;
import com.example.bloothcontroler.service.DataMessage;
import com.example.bloothcontroler.service.OrderCreater;
import com.example.bloothcontroler.ui.BluetoothActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false);
        homeViewModel.getText().observe(this, new Observer<DataMessage>() {
            @Override
            public void onChanged(@Nullable DataMessage s) {
                handleMessage(s);
            }
        });
        homeViewModel.startCover(OrderCreater.readStatus(),2000);
        return binding.getRoot();
    }

    private void handleMessage(DataMessage message) {
        if (null != message) {
            switch (message.what) {
                case DataMessage.CONNECT_STATUS:
                    if (homeViewModel.isBTConnected()) {
                        binding.txBluetooth.setText(getString(R.string.app_status_connected));
                    } else {
                        binding.txBluetooth.setText(getString(R.string.app_status_unconnected));
                    }
                    break;
                case DataMessage.RECEVED_STATUS_DATA:
                    int[] data = message.getData();
                    if (data.length > 16){
                        setDeviceStatus(data[0]);
                        setPVStatus(binding.tvPV1Status,data[1]);
                        setPVStatus(binding.tvPV2Status,data[2]);
                        setPVStatus(binding.tvPV3Status,data[3]);
                        setPVStatus(binding.tvPV4Status,data[4]);
                        setPVValue("PV1 VS PV2：",binding.tvBefore1,data[5]);
                        setPVValue("PV2 VS PV3：",binding.tvBefore2,data[6]);
                        setPVValue("PV3 VS PV4：",binding.tvBefore3,data[7]);
                        setPVValue("PV4 VS PV1：",binding.tvBefore4,data[8]);
                        setPVValue("PV1 VS PV2：",binding.tvAfter1,data[9]);
                        setPVValue("PV2 VS PV3：",binding.tvAfter2,data[10]);
                        setPVValue("PV3 VS PV4：",binding.tvAfter3,data[11]);
                        setPVValue("PV4 VS PV1：",binding.tvAfter4,data[12]);
                        setPVValue("PV1：",binding.tvResult1,data[13]);
                        setPVValue("PV2：",binding.tvResult2,data[14]);
                        setPVValue("PV3：",binding.tvResult3,data[15]);
                        setPVValue("PV4：",binding.tvResult4,data[16]);
                    }
                    break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.w(TAG,"onPause");
        homeViewModel.setReady(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.w(TAG,"onResume");
        homeViewModel.setReady(true);
    }

    private void setPVStatus(TextView pv, int status){
        if (isAdded()){
            if (status == 0x0000){
                pv.setText(getString(R.string.app_status_unrecoverd));
            }
            else if (status == 0x0001){
                pv.setText(getString(R.string.app_status_recovering));
            }
            else if (status == 0x0002){
                pv.setText(getString(R.string.app_status_recoverd));
            }
        }
    }

    private void setPVValue(String str,TextView pv,int value){
        if (isAdded()){
            BigDecimal b = new BigDecimal(value).multiply(new BigDecimal(0.1)).setScale(1, RoundingMode.HALF_UP);
            pv.setText(str + b.doubleValue() + "%");
        }
    }

    private void setDeviceStatus(int status){
        if (isAdded()){
            if (status == 0x0000){
                binding.imgStatusIcon.setImageResource(R.drawable.circle_red);
            }
            else if (status == 0x0100){
                binding.imgStatusIcon.setImageResource(R.drawable.circle_green);
            }
            else if (status >= 0x0001 && status <= 0x0010){
                binding.imgStatusIcon.setImageResource(R.drawable.circle_yellow);
            }
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.txBluetooth.setOnClickListener(this);
        binding.tvDefault.setOnClickListener(this);
        binding.tvPVModel.setOnClickListener(this);
        binding.tvStop.setOnClickListener(this);
    }

    private boolean isStart;

    private void showSetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_setlanguage, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        ListView listView = view.findViewById(R.id.language_content_list);

        final String[] languages;

        languages = new String[]{"PV1","PV2","PV3","PV4"};


        listView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, languages));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                binding.tvPVModel.setText(languages[position]);
                homeViewModel.sendOrder(OrderCreater.startCircle(position));
                dialog.dismiss();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvDefault:
                homeViewModel.sendOrder(OrderCreater.setDefault());
                break;
            case R.id.tvPVModel:
                showSetDialog();
                break;
            case R.id.tvStop:
                homeViewModel.sendOrder(OrderCreater.startOrStop(isStart));
                if (isStart){
                    isStart = false;
                    binding.tvStop.setText("Stop");
                } else {
                    isStart = true;
                    binding.tvStop.setText("Start");
                }
                break;
            case R.id.txBluetooth:
//                Intent intent = new Intent(getContext(), BluetoothActivity.class);
                Intent intent = new Intent(getContext(), DeviceScanActivity.class);
                startActivity(intent);
                break;
        }
    }
}
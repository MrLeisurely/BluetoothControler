package com.example.bloothcontroler.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.base.BaseRcvAdapter;
import com.example.bloothcontroler.databinding.ActivityChooseDeviceBinding;
import com.example.bloothcontroler.le.BluetoothLeService;
import com.example.bloothcontroler.ui.adapter.DeviceListAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hanwenhao
 * @date 2020/5/15
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class ChooseDeviceActivity extends Activity {
    private String TAG = ChooseDeviceActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_choose_device);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_choose_device);
        int height = ScreenUtil.getScreenHeight(this);
        int width = ScreenUtil.getScreenWidth(this);
        getWindow().setLayout((int) (width * 0.9), height*2/3);
        getWindow().setGravity(Gravity.CENTER);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,filter);

        binding.rcvMatch.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvFound.setLayoutManager(new LinearLayoutManager(this));
        foundDevices = new ArrayList<>();
        matchedDevices = new ArrayList<>();
        deviceSet = new HashSet<>();


        foundAdapter = new DeviceListAdapter(this);
        matchAdapter = new DeviceListAdapter(this);
        binding.rcvFound.setAdapter(foundAdapter);
        binding.rcvMatch.setAdapter(matchAdapter);
        foundAdapter.setData(foundDevices);
        matchAdapter.setData(matchedDevices);
        foundAdapter.setOnItemClickListener(new BaseRcvAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!isSearching){
                    connect(foundDevices.get(position).getAddress());
                }
            }
        });
        matchAdapter.setOnItemClickListener(new BaseRcvAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!isSearching){
                    connect(matchedDevices.get(position).getAddress());
                }
            }
        });

        // 获取BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth

        } else {

        }

        binding.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSearching){
                    stopSearch();
                    binding.cbSearch.setChecked(false);
                }
                foundDevices.clear();
                foundAdapter.notifyDataSetChanged();
            }
        });

        binding.cbSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        if (!bluetoothAdapter.isEnabled()) {
                            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(intent, REQUEST_ENABLE_BT);

                            //Toast.makeText(this, "开启蓝牙成功", Toast.LENGTH_SHORT).show();
                        } else {// 蓝牙已开启
                            startSearch();
                        }
                    } else {
                        stopSearch();
                    }


            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        boolean service = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "Try to bindService=" + service);
    }

    private void connect(String address){
        if (mBluetoothLeService.connect(address)){
            finish();
        } else {
            showMsg("Connect Fail");
        }
    }

    private BluetoothLeService mBluetoothLeService;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.e(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private List<BluetoothDevice> foundDevices;
    private HashSet<BluetoothDevice> deviceSet;
    private List<BluetoothDevice> matchedDevices;
    private DeviceListAdapter foundAdapter;
    private DeviceListAdapter matchAdapter;
    private ActivityChooseDeviceBinding binding;
    int REQUEST_ENABLE_BT = 1;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();                   // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show  in a ListView
                if (null != device){
                    Log.w(TAG,"device found : " + device.getName() + " address :" + device.getAddress());
                    if (deviceSet.add(device)){
                        foundDevices.add(device);
                        foundAdapter.notifyDataSetChanged();
//                        tvFoundNum.setText("可用设备(" + foundDevices.size() + ")");
                    }
                }
            }
        }
    };



    @Override
    protected void onDestroy() {
        if (isSearching){
            stopSearch();
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_ENABLE_BT){
//                noticeView.setText("开启蓝牙成功");
                startSearch();
            }
        }
    }

    private boolean isSearching = false;
    private BluetoothAdapter bluetoothAdapter = null;

    private void startSearch(){
        foundDevices.clear();
        deviceSet.clear();
        matchedDevices.clear();
        isSearching = true;
        // 查询配对设备
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        matchedDevices.addAll(bondedDevices);
        matchAdapter.notifyDataSetChanged();
//            tvMatchNum.setText("已配对的设备(" + matchedDevices.size() + ")");
        bluetoothAdapter.startDiscovery();
    }


    private void stopSearch(){
            isSearching = false;
            bluetoothAdapter.cancelDiscovery();
    }

    private void showMsg(String msg){
        if (!TextUtils.isEmpty(msg)){
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

}

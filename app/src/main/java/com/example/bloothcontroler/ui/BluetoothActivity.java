package com.example.bloothcontroler.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.base.BaseRcvAdapter;
import com.example.bloothcontroler.service.BluetoothDataIOServer;
import com.example.bloothcontroler.ui.adapter.DeviceListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private String  TAG = "BluetoothActivity";
    public static final int RECV_VIEW = 0;
    public static final int NOTICE_VIEW = 1;

    private BluetoothAdapter bluetoothAdapter = null;

    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;
    private AcceptThread acceptThread = null;

    private TextView noticeView = null;
    private Button turnOnOff = null;
    private TextView led0, led1, led2, led3, led4;
    NestedScrollView scrollView = null;
    private TextView recvView = null;
    private Button clearRecvView = null;
    private EditText sendText = null;
    private Button send = null;
    private Button btnOptions;
    private ProgressBar loading;
    private Button btnScan;
    private RecyclerView rcvFound;
    private RecyclerView rcvMatch;
    private TextView tvMatchNum;
    private TextView tvFoundNum;


    private List<BluetoothDevice> foundDevices;
    private List<BluetoothDevice> matchedDevices;
    private DeviceListAdapter foundAdapter;
    private DeviceListAdapter matchAdapter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();                   // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show  in a ListView
                if (null != device){
                    Log.w(TAG,"device found : " + device.getName() + " address :" + device.getAddress());
                    foundDevices.add(device);
                    foundAdapter.notifyDataSetChanged();
                    tvFoundNum.setText("可用设备(" + foundDevices.size() + ")");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_ENABLE_BT){
                noticeView.setText("开启蓝牙成功");
                doSearchOrCancel();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver,filter);

        // 注册监听事件
        noticeView = (TextView) findViewById(R.id.notice_view);
        turnOnOff = (Button) findViewById(R.id.turn_on_off);

        led0 = (TextView) findViewById(R.id.led0);
        led1 = (TextView) findViewById(R.id.led1);
        led2 = (TextView) findViewById(R.id.led2);
        led3 = (TextView) findViewById(R.id.led3);
        led4 = (TextView) findViewById(R.id.led4);
        scrollView = findViewById(R.id.scroll_view);
        recvView = (TextView) findViewById(R.id.recv_view);
        clearRecvView = (Button) findViewById(R.id.clear_recv_view);
        sendText = (EditText) findViewById(R.id.send_text);
        send = (Button) findViewById(R.id.send);
        btnOptions = (Button) findViewById(R.id.btn_options);
        loading = findViewById(R.id.pb_loading);
        rcvFound = findViewById(R.id.rcvFound);
        rcvMatch = findViewById(R.id.rcvMatch);
        btnScan = findViewById(R.id.btnScan);
        tvMatchNum = findViewById(R.id.tvMatchNum);
        tvFoundNum = findViewById(R.id.tvFoundNum);

        btnScan.setOnClickListener(this);
        turnOnOff.setOnClickListener(this);
        clearRecvView.setOnClickListener(this);
        send.setOnClickListener(this);
        btnOptions.setOnClickListener(this);


        rcvMatch.setLayoutManager(new LinearLayoutManager(this));
        rcvFound.setLayoutManager(new LinearLayoutManager(this));
        foundDevices = new ArrayList<>();
        matchedDevices = new ArrayList<>();

        foundAdapter = new DeviceListAdapter(this);
        matchAdapter = new DeviceListAdapter(this);
        rcvFound.setAdapter(foundAdapter);
        rcvMatch.setAdapter(matchAdapter);
        foundAdapter.setData(foundDevices);
        matchAdapter.setData(matchedDevices);
        foundAdapter.setOnItemClickListener(new BaseRcvAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!isSearching){
                    if (connectThread == null){
                        connectThread = new ConnectThread(foundDevices.get(position));
                        connectThread.start();
                    } else {

                    }
                }
            }
        });
        matchAdapter.setOnItemClickListener(new BaseRcvAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!isSearching){
                    if (connectThread == null){
                        connectThread = new ConnectThread(matchedDevices.get(position));
                        connectThread.start();
                    } else {

                    }
                }
            }
        });

        // 获取BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            btnScan.setVisibility(View.GONE);
            noticeView.setText("不支持蓝牙");
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                noticeView.setText("蓝牙未开启");
            }
            else {
                noticeView.setText("蓝牙已开启");
            }
        }

        noticeView.setBackgroundColor(Color.GRAY);
        led0.setBackgroundColor(Color.GRAY);
        led1.setBackgroundColor(Color.GRAY);
        led2.setBackgroundColor(Color.GRAY);
        led3.setBackgroundColor(Color.GRAY);
        led4.setBackgroundColor(Color.GRAY);
    }

    private boolean isOn = false;
    private boolean isSearching = false;
    private boolean asServer = false;
    int REQUEST_ENABLE_BT = 1;

    private void doSearchOrCancel(){
        if (isSearching){
            btnScan.setText("扫描");
            isSearching = false;
            loading.setVisibility(View.GONE);
            bluetoothAdapter.cancelDiscovery();
        } else {
            foundDevices.clear();
            matchedDevices.clear();
            btnScan.setText("停止");
            isSearching = true;
            loading.setVisibility(View.VISIBLE);
            // 查询配对设备
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            matchedDevices.addAll(bondedDevices);
            matchAdapter.notifyDataSetChanged();
            tvMatchNum.setText("已配对的设备(" + matchedDevices.size() + ")");
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnScan:

                if (!bluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);

                    //Toast.makeText(this, "开启蓝牙成功", Toast.LENGTH_SHORT).show();
                } else {// 蓝牙已开启
                    doSearchOrCancel();
                }
                break;
            case R.id.btn_options:
//                //创建弹出式菜单对象（最低版本11）
//                PopupMenu popup = new PopupMenu(this, view);//第二个参数是绑定的那个view
//                //获取菜单填充器
//                MenuInflater inflater = popup.getMenuInflater();
//                //填充菜单
//                inflater.inflate(R.menu.menu_main, popup.getMenu());
//                //绑定菜单项的点击事件
//                popup.setOnMenuItemClickListener(this);
//                //显示(这一行代码不要忘记了)
//                popup.show();
                if (bluetoothAdapter.isEnabled() && !isSearching && !asServer){
                    asServer = true;
                    btnScan.setVisibility(View.GONE);
                    if (acceptThread == null){
                        acceptThread = new AcceptThread();
                        acceptThread.start();
                    }
                }
                break;
            case R.id.turn_on_off: // 发送'0'或者'1'都可以
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (connectedThread == null) {
                    Toast.makeText(this, "未连接设备", Toast.LENGTH_SHORT).show();
                    break;
                }

                String turn_string = "1@#";
                connectedThread.write(turn_string.getBytes());
                if (isOn == false) {
                    isOn = true; // 打开了
                    turnOnOff.setText("OFF");
                    led1.setText("");
                    led2.setText("");
                    led3.setText("");
                    led4.setText("");
                }
                else {
                    isOn = false; // 关闭了
                    turnOnOff.setText("ON");
                    led1.setText("LED1");
                    led2.setText("LED2");
                    led3.setText("LED3");
                    led4.setText("LED4");
                }
                break;

            case R.id.clear_recv_view: // 清空接收框
                recvView.setText("");
                break;

            case R.id.send: // 发送数据，默认以"@#"结尾
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (connectedThread == null) {
                    Toast.makeText(this, "未连接设备", Toast.LENGTH_SHORT).show();
                    break;
                }
                String inputText = sendText.getText().toString(); // 发送给单片机数据
                //Toast.makeText(MainActivity.this, inputText, Toast.LENGTH_SHORT).show();
                connectedThread.write(inputText.getBytes());
                break;

            default:
                break;
        }
    }

    private android.os.Handler handler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = null;
            switch (msg.what) {
                case RECV_VIEW:
                    if (isOn == false) {
                        isOn = true;
                        turnOnOff.setText("OFF");
                    }
                    bundle = msg.getData();
                    String recv = bundle.getString("recv");
                    recvView.append(recv + "\n");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN); // 滚动到底部


                    if (recv.isEmpty() || recv.contains(" ") || recv.contains("#")) {
                        break;
                    }
                    int num = Integer.valueOf(recv) / 2; // 0-60s
                    if (num <= 20) {
                        led1.setText("");
                        led2.setText("");
                        led3.setText("");
                        led4.setText("");
                        led1.setBackgroundColor(Color.RED);
                        led2.setBackgroundColor(Color.RED);
                        led3.setBackgroundColor(Color.GREEN);
                        led4.setBackgroundColor(Color.GREEN);
                    }
                    else if (num < 30) {
                        int n = 30 - num;
                        led1.setText("" + n);
                        led2.setText("" + n);
                        if (num < 28) {
                            led3.setBackgroundColor(Color.GREEN);
                            led4.setBackgroundColor(Color.GREEN);
                        }
                        else {
                            led3.setBackgroundColor(Color.YELLOW);
                            led4.setBackgroundColor(Color.YELLOW);
                        }
                    }
                    else if (num <= 50) {
                        led1.setText("");
                        led2.setText("");
                        led3.setText("");
                        led4.setText("");
                        led1.setBackgroundColor(Color.GREEN);
                        led2.setBackgroundColor(Color.GREEN);
                        led3.setBackgroundColor(Color.RED);
                        led4.setBackgroundColor(Color.RED);
                    }
                    else {
                        int n = 60 - num;
                        led3.setText("" + n);
                        led4.setText("" + n);

                        if (num < 58) {
                            led1.setBackgroundColor(Color.GREEN);
                            led2.setBackgroundColor(Color.GREEN);
                        }
                        else {
                            led1.setBackgroundColor(Color.YELLOW);
                            led2.setBackgroundColor(Color.YELLOW);
                        }
                    }
                    break;

                case NOTICE_VIEW:
                    bundle = msg.getData();
                    String notice = bundle.getString("notice");
                    noticeView.setText(notice);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.start_bluetooth) {
            if (bluetoothAdapter != null) {
                // 开启蓝牙
                int REQUEST_ENABLE_BT = 1;
                if (!bluetoothAdapter.isEnabled()) {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    noticeView.setText("开启蓝牙成功");
                    //Toast.makeText(this, "开启蓝牙成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
                }
            }

            return true;
        }
        else if (id == R.id.show_devices) {
            if (bluetoothAdapter != null) {
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // 查询配对设备
                List<String> devices = new ArrayList<String>();
                Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : bondedDevices) {
                    devices.add(device.getName() + "-" + device.getAddress());
                }
                StringBuilder text = new StringBuilder();
                for (String device : devices) {
                    text.append(device + "\n");
                }
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        else if (id == R.id.find_devices) {
            Toast.makeText(this, "该功能暂时不可用", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.connect_devices) {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                return true;
            }

            // 查询配对设备 建立连接，只能连接第一个配对的设备
            List<String> devices = new ArrayList<String>();
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                connectThread = new ConnectThread(device);
                connectThread.start();
                //Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    //        private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final String MY_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    /**
     * 作为服务端接收链接
     */
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BCServer", UUID.fromString(MY_UUID));
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
//                     A connection was accepted. Perform work associated with
                    Log.e(TAG, "A connection was accepted. Perform work associated");
                    // the connection in a separate thread.
//                    manageMyConnectedSocket(socket);
                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.socket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
//                connectedThread = new ConnectedThread(socket);
//                connectedThread.start();
                BluetoothDataIOServer.getInstance().initWithSocket(socket);
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
                return;
            }
            //manageConnectedSocket(socket);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 客户端与服务器建立连接成功后，用ConnectedThread收发数据
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream input = null;
            OutputStream output = null;

            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = input;
            this.outputStream = output;
        }

        public void run() {
            StringBuilder recvText = new StringBuilder();
            byte[] buff = new byte[1024];
            int bytes;

            Bundle tmpBundle = new Bundle();
            Message tmpMessage = new Message();
            tmpBundle.putString("notice", "连接成功");
            tmpMessage.what = NOTICE_VIEW;
            tmpMessage.setData(tmpBundle);
            handler.sendMessage(tmpMessage);
            while (true) {
                try {
                    bytes = inputStream.read(buff);
                    if (bytes == 0){
                        return;
                    }
                    String str = new String(buff, "ISO-8859-1");
                    str = str.substring(0, bytes);

                    // 收到数据，单片机发送上来的数据以"#"结束，这样手机知道一条数据发送结束
                    //Log.e("read", str);
                    if (!str.endsWith("#")) {
                        recvText.append(str);
                        continue;
                    }
                    recvText.append(str.substring(0, str.length() - 1)); // 去除'#'

                    Bundle bundle = new Bundle();
                    Message message = new Message();

                    bundle.putString("recv", recvText.toString());
                    message.what = RECV_VIEW;
                    message.setData(bundle);
                    handler.sendMessage(message);
                    recvText.replace(0, recvText.length(), "");
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
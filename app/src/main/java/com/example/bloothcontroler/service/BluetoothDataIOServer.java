package com.example.bloothcontroler.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hanwenhao
 * @date 2020/4/10
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class BluetoothDataIOServer extends MutableLiveData<DataMessage> {
    private static final String TAG = BluetoothDataIOServer.class.getSimpleName();
    private boolean isConnected;
    private int pageTag;
    private int lastRegAddress;
    private int lastOrderAddress;
    private int lastOrderType;
    private boolean setTimeOK;
    public boolean isConnected() {
        return isConnected;
    }
    public boolean isSetTimeOK(){
        return setTimeOK;
    }
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    public void setPageTag(int pageTag){
        this.pageTag = pageTag;
    }
    private BluetoothDataIOServer(){

    }

    private static class SingleHolder{
        static BluetoothDataIOServer server = new BluetoothDataIOServer();
    }

    public static BluetoothDataIOServer getInstance(){
        return SingleHolder.server;
    }

    public void initWithSocket(BluetoothSocket socket){
        if (null != socket){
            if (bluetoothIOThread == null){
                bluetoothIOThread = new BluetoothIOThread(socket);
                bluetoothIOThread.start();
            } else {
                bluetoothIOThread.cancel();
                bluetoothIOThread.init(socket);
            }
        }
    }

    public void init(BluetoothGatt mBluetoothGatt,BluetoothGattCharacteristic mNotifyCharacteristic){
        this.mBluetoothGatt = mBluetoothGatt;
        this.mNotifyCharacteristic = mNotifyCharacteristic;
        setConnected(true);
    }

    /**
     * 处理接受到的数据
     * @param data
     */
    public void onDataRecv(byte[] data){
        try {
            DataMessage message = new DataMessage();
            // 校验数据
            if (CRCUtil.checkCRC(data) == 0){
                Log.w(TAG,"data check ok");
                int orderAddress = data[0];
                int orderType = data[1];
                if (orderAddress != lastOrderAddress || orderType != lastOrderType){
                    return;
                }

                int datasize = data[2];
                byte[] receivedData = new byte[datasize];
                System.arraycopy(data, 3, receivedData, 0, datasize);
                if (isDealingSplitOrder){
                    recvData.add(receivedData);
                    if (splitIndex == splitOrders.size() - 1){
                        isDealingSplitOrder = false;
                        message.setData(getRecvData());
                        recvData.clear();
                        splitOrders.clear();
                    } else {
                        splitIndex ++;
                        sendOrder(splitOrders.get(splitIndex),false,true);
                        return;
                    }
                } else {
                    message.setData(receivedData);
                }

                if (pageTag == DataMessage.PAGE_STATUS){
                    if (lastRegAddress == OrderCreater.DEVICE_STATUS){
                        message.what = DataMessage.RECEVED_STATUS_DATA;
                        postValue(message);
                    }
                    else if (lastRegAddress == OrderCreater.SET_TIME){
                        setTimeOK = true;
                    }
                }
                else if (pageTag == DataMessage.PAGE_SETTING){
                    if (lastRegAddress == OrderCreater.Pamx){
                        message.what = DataMessage.RECEVED_SETTING_DATA;
                        postValue(message);
                    }
                }
                else if (pageTag == DataMessage.PAGE_IV){
                    if (lastRegAddress == OrderCreater.Voc_of_String){
                        message.what = DataMessage.RECEVED_IV_DATA;
                        postValue(message);
                    }
                }
                else if (pageTag == DataMessage.PAGE_IV_PV1_DATA){
                    message.what = DataMessage.RECEVED_IV_PV1_DATA;
                    postValue(message);
                }
                else if (pageTag == DataMessage.PAGE_IV_PV2_DATA){
                    message.what = DataMessage.RECEVED_IV_PV2_DATA;
                    postValue(message);
                }
                else if (pageTag == DataMessage.PAGE_IV_PV3_DATA){
                    message.what = DataMessage.RECEVED_IV_PV3_DATA;
                    postValue(message);
                }
                else if (pageTag == DataMessage.PAGE_IV_PV4_DATA){
                    message.what = DataMessage.RECEVED_IV_PV4_DATA;
                    postValue(message);
                }
                else if (pageTag == DataMessage.PAGE_DEBUG){
                    message.what = DataMessage.RECEVED_DEBUG_DATA;
                    postValue(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disConnect(){
        if (mBluetoothGatt != null){
            try {
                mBluetoothGatt.disconnect();
            }catch (Exception e){

            }
        }
        setConnected(false);
    }

    public void close(){
        if (mBluetoothGatt != null){
            try {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }catch (Exception e){

            }
        }
        setConnected(false);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (null != bluetoothIOThread){
            bluetoothIOThread.cancel();
        }
    }


    public void sendOrder(String order){
        if (bluetoothIOThread != null && order != null){
            bluetoothIOThread.write(order.getBytes());
        }
    }

    private boolean isDealingOrder;
    private byte[] cacheOrder;

    private List<byte[]> splitOrders = new ArrayList<>();
    private List<byte[]> recvData = new ArrayList<>();
    private int splitIndex;
    private boolean isDealingSplitOrder;

    private byte[] getRecvData(){
        List<Byte> resultdata = new ArrayList<>();
        if (null != recvData && recvData.size() > 0){
            for (int i = 0; i < recvData.size(); i++) {
                byte[] data = recvData.get(i);
                for (int j = 0; j < data.length; j++) {
                    resultdata.add(data[j]);
                }
            }
        }
        byte[] result = new byte[resultdata.size()];
        for (int i = 0; i < resultdata.size(); i++) {
            result[i] = resultdata.get(i);
        }
        return result;
    }

    public void sendSplitOrder(List<byte[]> splitOrders){
        if (!isDealingSplitOrder && null!=splitOrders && splitOrders.size() > 0){
            this.splitOrders = splitOrders;
            isDealingSplitOrder = true;
            splitIndex = 0;
            sendOrder(splitOrders.get(0),false,true);
        }
    }

    /**
     * 发送控制指令，1秒内只会发送一条，带缓存功能，目前最多缓存一条指令
     * @param order 待发送指令
     * @param needCache 标记是否需要缓存此条指令
     */
    public synchronized void sendOrder(byte[] order,boolean needCache,boolean isSplit){
        if (order != null){
            if (isSplit){
                Log.d(TAG,"sendSplitOrder,splitNum = "+splitIndex);
            } else {
                Log.d(TAG,"sendOrder,isDealingOrder = "+isDealingOrder);
                if (isDealingOrder){
                    if (needCache){
                        cacheOrder = order;
                    }
                    return;
                }
                isDealingOrder = true;
            }

            Log.d(TAG,"order = "+ Arrays.toString(order));

            if (order.length > 4){
                int high = (order[2] & 0xFF) << 8;
                int low = order[3];
                lastRegAddress = high + low;
                lastOrderAddress = order[0];
                lastOrderType = order[1];
            }
            if (bluetoothIOThread != null){
                bluetoothIOThread.write(order);
            }
            if (mBluetoothGatt != null && mNotifyCharacteristic != null){
                try {
                    mNotifyCharacteristic.setValue(order);
                    mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
                }
                catch (Exception e){
                    Log.d(TAG,"send Data error,"+e.getMessage());
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isDealingOrder = false;
//                    if (cacheOrder != null){
//                        sendOrder(cacheOrder,true);
//                        cacheOrder = null;
//                    }
                    onDataRecv(create(10));
                }
            }).start();

        }
    }

    private byte[] create(int datanum){
        byte[] order = new byte[3 + datanum * 2];
        order[0] = 0x01;//从机地址 默认0x01
        order[1] = 0x04;
        order[2] = (byte) (datanum * 2);
        int[] nums = new int[datanum];

        for (int i = 0;i< datanum;i++){
            int num1 = (int) (100 * Math.random());
            nums[i] = num1;
            byte highRA = (byte) ((num1 & 0xFF00) >> 8);
            byte lowRA = (byte) (num1 & 0x00FF);
            order[3 + 2*i] = highRA;
            order[3 + 2*i + 1] = lowRA;
        }
        System.out.println(Arrays.toString(nums));
        return OrderCreater.getOrder(order);
    }

    private void setConnected(boolean isConnected){
        this.isConnected = isConnected;
        DataMessage message = new DataMessage();
        message.what = DataMessage.CONNECT_STATUS;
        postValue(message);
    }

    private BluetoothIOThread bluetoothIOThread;


    private class BluetoothIOThread extends Thread {
        private BluetoothSocket socket;
        private InputStream inputStream;
        private OutputStream outputStream;
        String TAG = "BluetoothIOThread";
        BluetoothIOThread(BluetoothSocket socket) {
            init(socket);
        }

        void init(BluetoothSocket socket){
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
//            StringBuilder recvText = new StringBuilder();
            byte[] buff = new byte[1024];
            int bytes;
            Bundle tmpBundle = new Bundle();
            DataMessage message = new DataMessage();
            tmpBundle.putString("notice", "连接成功");
            message.what = DataMessage.CONNECT_STATUS;
            isConnected = true;
            postValue(message);
            while (true) {
                try {
                    bytes = inputStream.read(buff);
                    if (bytes == 0){
                        continue;
                    }
                    byte[] data = new byte[bytes];
                    System.arraycopy(buff, 0, data, 0, bytes);
                    onDataRecv(data);
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
            isConnected = false;
        }
    }
}

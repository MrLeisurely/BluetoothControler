package com.example.bloothcontroler.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private int currentPv;
    private String password;
    private int currentType;
    private int currentGlass;
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

    public int getCurrentType() {
        return currentType;
    }

    public void setCurrentType(int currentType) {
        this.currentType = currentType;
    }

    public int getCurrentGlass() {
        return currentGlass;
    }

    public void setCurrentGlass(int currentGlass) {
        this.currentGlass = currentGlass;
    }

    public int getCurrentPv() {
        return currentPv;
    }

    public void setCurrentPv(int currentPv) {
        this.currentPv = currentPv;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    private void onRcvCopyDataFail(DataMessage message){
        if (currentCopyTime == copyTime){//重试次数用完
            if (isHandShake){
                message.what = DataMessage.HAND_SHAKE_FAIL;
            }
            else {
                message.what = DataMessage.GET_COPY_DATA_FAIL;
            }
            Log.w(TAG,"接收copydata失败，重试次数用完");
            postValue(message);
            resetCopy();
        }
        else {
            currentCopyTime ++;//重试计数+1
            Log.w(TAG,"接收copydata失败，重试：" +currentCopyTime);
            if (isHandShake){
                message.setCurrentCopyTime(currentCopyTime);
                message.setRepeatTime(currentHandshakeTime);
                message.what = DataMessage.HAND_SHAKE_RETRY_FAIL;
                postValue(message);
            }
            sendCopyOrder(cacheCopyOrder);
        }
    }

    /**
     * 处理接受到的数据
     * @param data
     */
    public synchronized void onDataRecv(byte[] data){
        try {
            DataMessage message = new DataMessage();
            // 校验数据
            if (CRCUtil.checkCRC(data) == 0){
                hasRcvData = true;
                Log.w(TAG,"data check ok");
                if (isCopy) {
                    if (Arrays.equals(data,cacheCopyOrder)){
                        currentCopyTime = 0;//重试次数归零
                        if (isHandShake){
                            if (currentHandshakeTime == handshakeTime - 1){//达到指定握手次数
                                currentHandshakeTime = 0;
                                isHandShake = false;
                                message.what = DataMessage.HAND_SHAKE_SUCCESS;
                                postValue(message);
                            }
                            else {
                                currentHandshakeTime ++;
                                message.what = DataMessage.UPDATE_HAND_SHAKING;//更新握手次数
                                message.setRepeatTime(currentHandshakeTime);
                                postValue(message);
                                sendCopyOrder(cacheCopyOrder);
                            }
                        }
                        else {
                            message.what = DataMessage.GET_COPY_DATA;//成功收到同样的回复
                            cacheIndex ++;
                            message.setRepeatTime(cacheIndex);
                            Log.w(TAG,"copyData rcv:" + cacheIndex);
                            postValue(message);
                        }
                    }
                    else {
                        onRcvCopyDataFail(message);
                    }
                    return;
                }
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
                        message.setData(getMergeData());
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

    private void onRcvTimeout(){
        DataMessage message = new DataMessage();
        message.what = DataMessage.RECEVED_OVER_TIME;
        postValue(message);
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
    private byte[] cacheCopyOrder;

    private List<byte[]> splitOrders = new ArrayList<>();
    private List<byte[]> recvData = new ArrayList<>();
    private int splitIndex;
    private boolean isDealingSplitOrder;
    private static long SPLIT_TIME_OUT = 5000;

    private int copyTime = 10;//失败重试的次数
    private int handshakeTime;//握手次数
    private int cacheIndex;
    private int currentCopyTime;
    private int currentHandshakeTime;
    private boolean isCopy;
    private boolean isHandShake;

    public void resetCacheIndex() {
        cacheIndex = 0;
    }

    public void resetCopy(){
        isCopy = false;
        currentCopyTime = 0;
        resetCacheIndex();
    }

    public void setCopyTime(int copyTime) {
        this.copyTime = copyTime;
    }

    public void setHandshakeTime(int handshakeTime) {
        this.handshakeTime = handshakeTime;
    }

    private byte[] getMergeData(){
        List<Byte> resultData = new ArrayList<>();
        if (null != recvData && recvData.size() > 0){
            for (int i = 0; i < recvData.size(); i++) {
                byte[] data = recvData.get(i);
                for (int j = 0; j < data.length; j++) {
                    resultData.add(data[j]);
                }
            }
        }
        byte[] result = new byte[resultData.size()];
        for (int i = 0; i < resultData.size(); i++) {
            result[i] = resultData.get(i);
        }
        return result;
    }

    public void sendSplitOrder(List<byte[]> orders){
        if (!isDealingSplitOrder && null!=orders && orders.size() > 0){
            this.splitOrders = orders;
            isDealingSplitOrder = true;
            splitIndex = 0;
            sendOrder(orders.get(0),false,true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(SPLIT_TIME_OUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isDealingSplitOrder){
                        onRcvTimeout();
                        isDealingSplitOrder = false;
                        splitIndex = 0;
                        splitOrders.clear();
                        recvData.clear();
                    }
                }
            }).start();
        }
    }


    public void sendOrder(byte[] order,boolean needCache,boolean isSplit){
        sendOrderImp(order,needCache,isSplit);
    }

    /**
     * 这类指令发送后下位机会回复相同的内容
     * @param order
     */
    public void sendCopyOrder(byte[] order){
        isCopy = true;
        cacheCopyOrder = order;
        sendOrderImp(order,false,false);
    }

    public void sendHandShakeOrder(byte[] order){
        isHandShake = true;
        sendCopyOrder(order);
    }

    public String toHex(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        int n = 0;
        for (byte b:bytes){
            if (n % 0x10 == 0){
                builder.append(String.format("%1$05x:",n));
            }
            builder.append(String.format("%1$02x ",b));
            n++;
            if (n % 0x10 == 0){
                builder.append("\n");
            }
        }
        builder.append("\n");
        return builder.toString().toUpperCase();
    }

    /**
     * 发送控制指令，1秒内只会发送一条，带缓存功能，目前最多缓存一条指令
     * @param order 待发送指令
     * @param needCache 标记是否需要缓存此条指令
     */
    private synchronized void sendOrderImp(final byte[] order, boolean needCache, boolean isSplit){
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

            Log.w(TAG,"order = "+ toHex(order));

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
                    Log.e(TAG,"send Data error,"+e.getMessage());
                }
            }
            flagId = Math.random();
            resetRcvDataFlag(flagId);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isDealingOrder = false;
                    if (cacheOrder != null){
                        sendOrder(cacheOrder,true,false);
                        cacheOrder = null;
                    }
//                    onDataRecv(order);
//                    onDataRecv(create(4));
                }
            }).start();

        }
    }

    private class RcvHandler implements Runnable{
        private final double localId;
        public RcvHandler(double localId) {
            this.localId = localId;
        }

        public double getLocalId() {
            return localId;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!hasRcvData && localId == flagId && isCopy){
                onRcvCopyDataFail(new DataMessage());
            }
        }
    }

    private volatile boolean hasRcvData;
    private double flagId;

    private void resetRcvDataFlag(double id){
        hasRcvData = false;
        new Thread(new RcvHandler(id)).start();
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

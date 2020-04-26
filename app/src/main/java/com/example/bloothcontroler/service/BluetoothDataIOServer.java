package com.example.bloothcontroler.service;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * @author Hanwenhao
 * @date 2020/4/10
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class BluetoothDataIOServer extends MutableLiveData<DataMessage> {

    private boolean isConnected;
    private int pageTag;
    private int lastAddress;
    public boolean isConnected() {
        return isConnected;
    }

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

    public synchronized void sendOrder(byte[] order){
        if (bluetoothIOThread != null && order != null){
            bluetoothIOThread.write(order);
            if (order.length > 4){
                int high = (order[2] & 0xFF) << 8;
                int low = order[3];
                lastAddress = high + low;
            }
        }
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
                    // 校验数据
                    if (CRCUtil.checkCRC(data) == 0){
                        Log.w(TAG,"data check ok");
                        if (pageTag == DataMessage.PAGE_STATUS){
                            if (lastAddress == OrderCreater.DEVICE_STATUS){
                                int datasize = data[2];
                                byte[] receivedData = new byte[datasize];
                                System.arraycopy(data, 3, receivedData, 0, datasize);
                                message.setData(receivedData);
                                message.what = DataMessage.RECEVED_STATUS_DATA;
                                postValue(message);
                            }
                        }
                        else if (pageTag == DataMessage.PAGE_SETTING){
                            if (lastAddress == OrderCreater.Pamx){
                                int datasize = data[2];
                                byte[] receivedData = new byte[datasize];
                                System.arraycopy(data, 3, receivedData, 0, datasize);
                                message.setData(receivedData);
                                message.what = DataMessage.RECEVED_SETTING_DATA;
                                postValue(message);
                            }
                        }
                        else if (pageTag == DataMessage.PAGE_IV){
                            if (lastAddress == OrderCreater.Voc_of_String){
                                int datasize = data[2];
                                byte[] receivedData = new byte[datasize];
                                System.arraycopy(data, 3, receivedData, 0, datasize);
                                message.setData(receivedData);
                                message.what = DataMessage.RECEVED_IV_DATA;
                                postValue(message);
                            }
                        }
                    }
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

package com.example.bloothcontroler.service;

/**
 * @author Hanwenhao
 * @date 2020/4/13
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class DataMessage {
    public static final int CONNECT_STATUS = 1;
    public static final int RECEVED_STATUS_DATA = 2;
    public static final int RECEVED_SETTING_DATA = 3;
    public static final int RECEVED_IV_DATA = 4;
    public static final int RECEVED_IV_PV1_DATA = 5;
    public static final int RECEVED_IV_PV2_DATA = 6;
    public static final int RECEVED_IV_PV3_DATA = 7;
    public static final int RECEVED_IV_PV4_DATA = 8;
    public static final int RECEVED_DEBUG_DATA = 9;
    public static final int RECEVED_OVER_TIME = 10;
    public static final int UPDATE_HAND_SHAKING = 11;
    public static final int HAND_SHAKE_SUCCESS = 12;
    public static final int GET_COPY_DATA = 13;
    public static final int GET_COPY_DATA_FAIL = 14;
    public static final int HAND_SHAKE_FAIL = 15;
    public static final int HAND_SHAKE_RETRY_FAIL = 16;

    public static final int PAGE_STATUS = 5;
    public static final int PAGE_SETTING = 6;
    public static final int PAGE_IV = 7;
    public static final int PAGE_MORE = 8;
    public static final int PAGE_DEBUG = 9;
    public static final int PAGE_IV_PV1_DATA = 10;
    public static final int PAGE_IV_PV2_DATA = 11;
    public static final int PAGE_IV_PV3_DATA = 12;
    public static final int PAGE_IV_PV4_DATA = 13;

    public int what;
    private byte[] data;
    private int dataSize;
    private int registerAddress;
    private int repeatTime;
    private int currentCopyTime;

    public int getCurrentCopyTime() {
        return currentCopyTime;
    }

    public void setCurrentCopyTime(int currentCopyTime) {
        this.currentCopyTime = currentCopyTime;
    }

    public int getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(int repeatTime) {
        this.repeatTime = repeatTime;
    }

    public int[] getData() {
        int[] mdata = new int[data.length/2];
        for (int i = 0;i < data.length - 1;i += 2){
            int high = (data[i] & 0x00FF) << 8;
            int low = data[i + 1] & 0x00FF;
            if (high + low >= 0x8000){
                mdata[i/2] = high + low - 0x10000;
            } else {
                mdata[i/2] = high + low;
            }
        }
        return mdata;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }

    public int getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(int registerAddress) {
        this.registerAddress = registerAddress;
    }
}

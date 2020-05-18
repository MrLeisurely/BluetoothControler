package com.example.bloothcontroler.service;

import java.util.Calendar;

/**
 * @author Hanwenhao
 * @date 2020/4/10
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class OrderCreater {

    public static final byte READ = 0x04;
    public static final byte DEBUG = 0x05;
    public static final byte WRITE = 0x10;

    /**
     * 运行信息读寄存器地址定义
     */
    public static final int DEVICE_STATUS = 30001;
    public static final int STATUS_PV1 = 30002;
    public static final int STATUS_PV2 = 30003;
    public static final int STATUS_PV3 = 30004;
    public static final int STATUS_PV4 = 30005;

    public static final int BEFORE_PV1_VS_PV2 = 30006;
    public static final int BEFORE_PV2_VS_PV3 = 30007;
    public static final int BEFORE_PV3_VS_PV4 = 30008;
    public static final int BEFORE_PV4_VS_PV1 = 30009;

    public static final int AFTER_PV1_VS_PV2 = 30010;
    public static final int AFTER_PV2_VS_PV3 = 30011;
    public static final int AFTER_PV3_VS_PV4 = 30012;
    public static final int AFTER_PV4_VS_PV1 = 30013;

    public static final int REPAIR_RESULT_PV1 = 30014;
    public static final int REPAIR_RESULT_PV2 = 30015;
    public static final int REPAIR_RESULT_PV3 = 30016;
    public static final int REPAIR_RESULT_PV4 = 30017;

    /**
     * 运行信息写单个寄存器地址定义
     */
    public static final int DEFAULT = 40001;
    public static final int START_PV1 = 40002;
    public static final int START_PV2 = 40003;
    public static final int START_PV3 = 40004;
    public static final int START_PV4 = 40005;
    public static final int STOP_OR_START = 40006;

    /**
     * 手机与设备对时
     */
    public static final int YEAR = 70001;
    public static final int MONTH = 70002;
    public static final int DAY = 70003;
    public static final int HOUR = 70004;
    public static final int MINUTE = 70005;
    public static final int SECOND = 70006;

    /**
     * 设置
     */
    public static final int Pamx = 50001;
    public static final int Vmp = 50002;
    public static final int Imp = 50003;
    public static final int Voc = 50004;
    public static final int Isc = 50005;
    public static final int Type = 50006;
    public static final int Glass = 50007;
    public static final int Cells = 50008;
    public static final int Years = 50009;
    public static final int Moduls = 50010;

    /**
     * IV
     */
    public static final int Voc_of_String = 60001;
    public static final int Irradiation = 60002;
    public static final int Temperation = 60003;
    public static final int T_of_Pm = 60004;
    public static final int T_of_Voc = 60005;
    public static final int T_of_Isc = 60006;
    public static final int PV1_Pmax_stc = 60007;
    public static final int PV1_Difference = 60008;
    public static final int PV1_128IVP_I = 60009;
    public static final int PV1_128IVP_V = 60137;
    public static final int PV2_Pmax_stc = 60265;
    public static final int PV2_Difference = 60266;
    public static final int PV2_128IVP_I = 60267;
    public static final int PV2_128IVP_V = 60395;
    public static final int PV3_Pmax_stc = 60523;
    public static final int PV3_Difference = 60524;
    public static final int PV3_128IVP_I = 60525;
    public static final int PV3_128IVP_V = 60653;
    public static final int PV4_Pmax_stc = 60781;
    public static final int PV4_Difference = 60782;
    public static final int PV4_128IVP_I = 60783;
    public static final int PV4_128IVP_V = 60911;

    public static final int SET_TIME = 63000;


    /**
     * 添加校验位
     * @param bytes
     * @return
     */
    public static byte[] getOrder(byte[] bytes){
        int crc = CRCUtil.getCRC(bytes);
        byte low = (byte) (crc & 0x00FF);
        byte high = (byte) ((crc & 0xFF00) >> 8);
        byte[] order = new byte[bytes.length + 2];
        System.arraycopy(bytes,0,order,0,bytes.length);
        order[order.length - 2] = high;
        order[order.length - 1] = low;
        return order;
    }

    public static byte[] getWriteDataOrder(int registerAddress,int registerNum,int... writeData){
        int length = 7 + writeData.length * 2;
        byte[] order = new byte[length];
        order[0] = 0x01;//从机地址 默认0x01
        order[1] = WRITE;
        byte highRA = (byte) ((registerAddress & 0xFF00) >> 8);
        byte lowRA = (byte) (registerAddress & 0x00FF);
        order[2] = highRA;
        order[3] = lowRA;
        byte highRN = (byte) ((registerNum & 0xFF00) >> 8);
        byte lowRN = (byte) (registerNum & 0x00FF);
        order[4] = highRN;
        order[5] = lowRN;
        order[6] = (byte) (writeData.length * 2);
        int[] wdata = writeData.clone();
        for (int i = 0;i < wdata.length;i ++){
            int num = wdata[i];
            byte high = (byte) ((num & 0xFF00) >> 8);
            byte low = (byte) (num & 0x00FF);
            order[6 + i*2 + 1] = high;
            order[6 + i*2 + 2] = low;
        }
        return getOrder(order);
    }

    public static byte[] debugOrder(){
        return generalReadOrder(DEBUG,DEVICE_STATUS,33);
    }

    /**
     * 通用读指令
     * @param registerAddress
     * @param registerNum
     * @return
     */
    public static byte[] generalReadOrder(int registerAddress, int registerNum){
        return generalReadOrder(READ,registerAddress,registerNum);
    }

    /**
     * 通用读指令
     * @param registerAddress
     * @param registerNum
     * @return
     */
    public static byte[] generalReadOrder(byte funType,int registerAddress, int registerNum){
        byte[] order = new byte[6];
        order[0] = 0x01;//从机地址 默认0x01
        order[1] = funType;
        byte highRA = (byte) ((registerAddress & 0xFF00) >> 8);
        byte lowRA = (byte) (registerAddress & 0x00FF);
        order[2] = highRA;
        order[3] = lowRA;
        byte highRN = (byte) ((registerNum & 0xFF00) >> 8);
        byte lowRN = (byte) (registerNum & 0x00FF);
        order[4] = highRN;
        order[5] = lowRN;
        return getOrder(order);
    }

    /**
     * 通用写指令
     * @param registerAddress
     * @param registerNum
     * @param writeData
     * @return
     */
    private static byte[] generalWriteOrder(int registerAddress,int registerNum,int writeData){
        byte[] order = new byte[11];
        order[0] = 0x01;//从机地址 默认0x01
        order[1] = WRITE;
        byte highRA = (byte) ((registerAddress & 0xFF00) >> 8);
        byte lowRA = (byte) (registerAddress & 0x00FF);
        order[2] = highRA;
        order[3] = lowRA;
        byte highRN = (byte) ((registerNum & 0xFF00) >> 8);
        byte lowRN = (byte) (registerNum & 0x00FF);
        order[4] = highRN;
        order[5] = lowRN;
        order[6] = 0x04;
        order[7] = (byte) ((writeData & 0xFF000000) >> 12);
        order[8] = (byte) ((writeData & 0x00FF0000) >> 8);
        order[9] = (byte) ((writeData & 0x0000FF00) >> 4);
        order[10] = (byte) (writeData & 0x000000FF);
        return getOrder(order);
    }

    public static byte[] setTimeOrder(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        return getWriteDataOrder(SET_TIME,6,year,month,day,hour,min,sec);
    }

    public static byte[] setDefault(){
        return getWriteDataOrder(DEFAULT,1,1);
    }

    public static byte[] startCircle(int pos){
        if (pos == 0){
            return getWriteDataOrder(START_PV1,1,1);
        }
        if (pos == 1){
            return getWriteDataOrder(START_PV2,1,2);
        }
        if (pos == 2){
            return getWriteDataOrder(START_PV3,1,3);
        }
        else return getWriteDataOrder(START_PV4,1,4);
    }

    public static byte[] getPVData(int pos){
        if (pos == 0){
            return generalReadOrder(PV1_Pmax_stc,258);
        }
        if (pos == 1){
            return generalReadOrder(PV2_Pmax_stc,258);
        }
        if (pos == 2){
            return generalReadOrder(PV3_Pmax_stc,258);
        }
        else return generalReadOrder(PV4_Pmax_stc,258);
    }

    public static byte[] startOrStop(boolean isStart){
        if (isStart){
            return getWriteDataOrder(STOP_OR_START,1,1);
        } else {
            return getWriteDataOrder(STOP_OR_START,1,2);
        }
    }

    public static byte[] readStatus(){
        return generalReadOrder(DEVICE_STATUS,23);
    }
}

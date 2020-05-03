package com.example.bloothcontroler.service;

/**
 * @author Hanwenhao
 * @date 2020/4/10
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class CRCUtil {
    public static int getCRC(byte[] bytes){
        int CRC = 0xFFFF;
        int POLYNOMIAL = 0xA001;//CRC 16 MODBUS

        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ( bytes[i] & 0xff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x01) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        //高低位转换，看情况使用
        CRC = ( (CRC & 0x0000FF00) >> 8) | ( (CRC & 0x000000FF ) << 8);
        return CRC;
    }

    /**
     *
     * @param bytes
     * @return 0 校验通过
     */
    public static int checkCRC(byte[] bytes){
        if (bytes.length < 2){
            return -1;
        } else {
            int high = (bytes[bytes.length - 2] & 0x00FF) << 8;
            int low = bytes[bytes.length - 1] & 0x00FF;
            int datacrc = high + low;
            int CRC = 0xFFFF;
            int POLYNOMIAL = 0xA001;//CRC 16 MODBUS

            int i, j;
            for (i = 0; i < bytes.length - 2; i++) {
                CRC ^= ( bytes[i] & 0xff);
                for (j = 0; j < 8; j++) {
                    if ((CRC & 0x01) != 0) {
                        CRC >>= 1;
                        CRC ^= POLYNOMIAL;
                    } else {
                        CRC >>= 1;
                    }
                }
            }
            CRC = ( (CRC & 0x0000FF00) >> 8) | ( (CRC & 0x000000FF ) << 8);
            return CRC - datacrc;
        }
    }
}

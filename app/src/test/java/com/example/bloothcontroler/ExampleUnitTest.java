package com.example.bloothcontroler;

import com.example.bloothcontroler.service.OrderCreater;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testCRC(){
//        byte[] bytes = new byte[]{0x01,0x04,0x30,0x01,0x00,0x01};
//        System.out.println(Integer.toHexString(CRCUtil.getCRC(bytes)));
//        byte[] bytes2 = OrderCreater.generalReadOrder(30001,23);
//        int high = (bytes2[2] & 0xFFFF) << 8;
//        int low = bytes2[3];
//        int lastAddress = high + low;
//        System.out.println(Arrays.toString(bytes2));
//        System.out.println(high);
//        System.out.println(low);
//        System.out.println(lastAddress);

//        byte[] order = new byte[7];
//        order[0] = 0x01;//从机地址 默认0x01
//        order[1] = 0x04;
//        order[2] = 4;
//        int num1 = 12;
//        int num2 = -23;
//        byte highRA = (byte) ((num1 & 0xFF00) >> 8);
//        byte lowRA = (byte) (num1 & 0x00FF);
//        order[3] = highRA;
//        order[4] = lowRA;
//        byte highRN = (byte) ((num2 & 0xFF00) >> 8);
//        byte lowRN = (byte) (num2 & 0x00FF);
//        order[5] = highRN;
//        order[6] = lowRN;

        byte[] data = OrderCreater.getWriteDataOrder(OrderCreater.DEFAULT,1,1);
        System.out.println(Arrays.toString(data));
        byte[] data1 = OrderCreater.setTimeOrder();
        System.out.println(Arrays.toString(data1));
        // 校验数据
//        if (CRCUtil.checkCRC(data) == 0){
//                    int datasize = data[2];
//                    byte[] receivedData = new byte[datasize];
//                    System.arraycopy(data, 3, receivedData, 0, datasize);
//                    DataMessage message = new DataMessage();
//                    message.setData(receivedData);
//                    message.setDataSize(datasize);
//            System.out.println(Arrays.toString(message.getData()));
//        }
//
////        BigDecimal b = new BigDecimal(-8897).multiply(new BigDecimal(0.1)).setScale(1, RoundingMode.HALF_UP);
////        System.out.println(b.doubleValue() + "%");
//        System.out.println(Arrays.toString(OrderCreater.getWriteDataOrder(OrderCreater.Pamx,2,12,-23)));
    }
    @Test
    public void test3(){
        byte[] order = OrderCreater.getPVData(2);
        int lastAddress = 0;
        if (order.length > 4){
            int high = (order[2] & 0xFF) << 8;
            int low = order[3];
            lastAddress = high + low;
        }
        System.out.println(Arrays.toString(order));
        System.out.println(lastAddress);
    }

    @Test
    public void testgetData(){
        byte[] data = {0x12,0x00,0x13,0x10};
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
        System.out.println(Arrays.toString(data));
        System.out.println(Arrays.toString(mdata));
    }


    @Test
    public void test2(){
        int datasize = 3;
//        int[] xs = created(datasize,true);
//        int[] ys = created(datasize,false);

        int[] pvData = created3(datasize);

        int cdataSize = (pvData.length - 2) / 2;

        int[] xs = new int[cdataSize];
        int[] ys = new int[cdataSize];
        System.arraycopy(pvData, 2, xs, 0, cdataSize);
        System.arraycopy(pvData, 2 + cdataSize, ys, 0, cdataSize);
        System.out.println(Arrays.toString(xs));
        System.out.println(Arrays.toString(ys));
        for (int i = 0;i < cdataSize ;i++){
            for (int j = i + 1 ; j < cdataSize ; j ++){
                if (xs[j] < xs[i]){
                    int c = xs[j];
                    xs[j] = xs[i];
                    xs[i] = c;

                    int cy = ys[j];
                    ys[j] = ys[i];
                    ys[i] = cy;
                }
            }
        }
        System.out.println(Arrays.toString(xs));
        System.out.println(Arrays.toString(ys));

    }

    private int[] created3(int datanum){
        int[] order = new int[2 + 2 * datanum];

        order[0] = 120;
        order[1] = 121;
//        order[2 + 2 * datanum] = 998;
//        order[2 + 2 * datanum + 1] = 999;
        for (int i = 0;i< datanum;i++){
            int num1;
            num1 = (int) (100 * Math.random());

            order[i + 2] = num1;
            order[i + datanum + 2] = i;
        }
        System.out.println(Arrays.toString(order));
        return order;
    }

    private int[] created(int datanum,boolean isradom){
        int[] order = new int[datanum];
        for (int i = 0;i< datanum;i++){
            int num1;
            if (isradom){
                num1 = (int) (100 * Math.random());
            } else {
                num1 = i;
            }
            order[i] = num1;
        }
        System.out.println(Arrays.toString(order));
        return order;
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
}
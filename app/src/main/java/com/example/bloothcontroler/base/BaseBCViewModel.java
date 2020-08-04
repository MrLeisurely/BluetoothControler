package com.example.bloothcontroler.base;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.bloothcontroler.service.BluetoothDataIOServer;
import com.example.bloothcontroler.service.DataMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Hanwenhao
 * @date 2020/4/10
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public abstract class BaseBCViewModel extends ViewModel {
    private BluetoothDataIOServer mText;
    private String TAG = "BaseBCViewModel";
    public boolean isBTConnected (){
        return mText.isConnected();
    }
    public boolean isSetTimeOK (){
        return mText.isSetTimeOK();
    }
    protected BaseBCViewModel(){
        mText = BluetoothDataIOServer.getInstance();
    }

    public LiveData<DataMessage> getText() {
        return mText;
    }

    /**
     * 一般指令，指用户手动发出，默认缓存
     * @param order
     */
    public void sendOrder(byte[] order){
        if (null != mText){
            mText.sendOrder(order,true,false);
        }
    }

    /**
     * 轮询指令，默认不缓存
     * @param order
     */
    public void sendUnCacheOrder(byte[] order){
        if (null != mText){
            mText.sendOrder(order,false,false);
        }
    }

    public void setSplitOrder(List<byte[]> splitOrders){
        if (null != mText){
            mText.sendSplitOrder(splitOrders);
        }
    }

    public void setPageTag(int pageTag){
        if (null != mText){
            mText.setPageTag(pageTag);
        }
    }

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (isReadyToSend && order.length > 0
                    && isBTConnected()
//                    && isSetTimeOK()
            ){
                Log.w(TAG,"sendCover:" + Arrays.toString(order));
                sendUnCacheOrder(order);
            }
        }
    };

    private boolean isReadyToSend;

    public void setReady(boolean isReadyToSend){
        this.isReadyToSend = isReadyToSend;
    }


    private Timer timer;
    private byte[] order;
    private boolean hasStarted;
    public void startCover(byte[] order,long period){
        this.order = order;
        if (timer == null){
            timer = new Timer();
        }
        if (!hasStarted){
            hasStarted = true;
            timer.schedule(timerTask,0,period);
        }
    }



    public double getValue(int data){
        return getValue(data,0.1);
    }

    public String getValueStr(int data){
        return String.valueOf(getValue(data));
    }

    public double getValue(int data,double rate){
        BigDecimal b = new BigDecimal(data).multiply(new BigDecimal(rate)).setScale(1, RoundingMode.HALF_UP);
        return b.doubleValue();
    }
}

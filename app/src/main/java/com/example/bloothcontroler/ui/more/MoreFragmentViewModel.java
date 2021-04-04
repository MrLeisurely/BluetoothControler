package com.example.bloothcontroler.ui.more;

import com.example.bloothcontroler.base.BaseBCViewModel;
import com.example.bloothcontroler.service.OrderCreater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hanwenhao
 * @date 2020/3/31
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class MoreFragmentViewModel extends BaseBCViewModel {
    public void startCPUHandShaking(){
        getIOServer().setHandshakeTime(10);
        getIOServer().sendHandShakeOrder(OrderCreater.createCPUHandShakeOrder(cpuIndex));
    }

    public void startUpdate(){
        getIOServer().resetCacheIndex();
        update(0);
    }

    public void update(int index){
//        int size = fileData.size();
//        Log.w("BluetoothDataIOServer","updating: fileData.size = " + size +",index = " + index);
        if (null != fileData && fileData.size() > 0 && index >= 0 && index < fileData.size()){
            byte[] order = fileData.get(index);
//            Log.w("BluetoothDataIOServer","getorder = " + getIOServer().toHex(order));
            getIOServer().sendCopyOrder(fileData.get(index));
        }
    }

    public void resetCopy(){
        getIOServer().resetCopy();
    }

    public byte cpuIndex;
    public List<byte[]> fileData = new ArrayList<>();

    public void readFile(String filePath,byte cpuIndex){
//        String filePath = "D:\\文档\\BlueTooth\\导出\\PVRecovery_cpu2_V4.hex";
        BufferedReader bufferedInputStream = null;
        fileData.clear();
        this.cpuIndex = cpuIndex;
        try {
            File file = new File(filePath);
//            System.out.println("getTotalSpace:" + file.getTotalSpace() + " getFreeSpace" + file.getFreeSpace());
            FileReader fileInputStream;
            fileInputStream = new FileReader(file);
            bufferedInputStream = new BufferedReader(fileInputStream);
            String line;
            while ((line = bufferedInputStream.readLine())!= null){
                byte[] order = createLineDataOrder(line);
                if (null != order){
                    fileData.add(order);
                }
            }
            bufferedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] createLineDataOrder(String lineDataStr){
        if (null != lineDataStr && lineDataStr.startsWith(":")){
            String realData = lineDataStr.substring(1);
            byte[] lineData = hex2Bytes(realData);
            return OrderCreater.createLineDataFrame(cpuIndex,lineData);
        }
        return null;
    }

    private static byte[] hex2Bytes(String src){
        byte[] res = new byte[src.length()/2];
        char[] chs = src.toCharArray();
        int[] b = new int[2];

        for(int i=0,c=0; i<chs.length; i+=2,c++){
            for(int j=0; j<2; j++){
                if(chs[i+j]>='0' && chs[i+j]<='9'){
                    b[j] = (chs[i+j]-'0');
                }else if(chs[i+j]>='A' && chs[i+j]<='F'){
                    b[j] = (chs[i+j]-'A'+10);
                }else if(chs[i+j]>='a' && chs[i+j]<='f'){
                    b[j] = (chs[i+j]-'a'+10);
                }
            }

            b[0] = (b[0]&0x0f)<<4;
            b[1] = (b[1]&0x0f);
            res[c] = (byte) (b[0] | b[1]);
        }

        return res;
    }
}

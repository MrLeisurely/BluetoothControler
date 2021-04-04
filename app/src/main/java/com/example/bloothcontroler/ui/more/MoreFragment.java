package com.example.bloothcontroler.ui.more;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.BuildConfig;
import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.FragmentMoreBinding;
import com.example.bloothcontroler.service.BluetoothDataIOServer;
import com.example.bloothcontroler.service.DataMessage;
import com.example.bloothcontroler.ui.ActivityDebug;
import com.example.bloothcontroler.ui.ChooseDeviceActivity;
import com.example.bloothcontroler.ui.ScreenUtil;
import com.example.bloothcontroler.ui.dialog.MssageDialog;
import com.example.bloothcontroler.ui.dialog.UpdateDialog;
import com.example.bloothcontroler.ui.widget.FileSelectionFragment;
import com.example.bloothcontroler.ui.widget.ObtainFilesAddress;
import com.example.bloothcontroler.ui.widget.YWLoadingDialog;
import com.example.bloothcontroler.ui.widget.entity.Document;

import org.jetbrains.annotations.NotNull;

/**
 * @author Hanwenhao
 * @date 2020/3/31
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class MoreFragment extends Fragment implements View.OnClickListener {
    private MoreFragmentViewModel notificationsViewModel;
    private FragmentMoreBinding binding;
    private YWLoadingDialog processDialog;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(MoreFragmentViewModel.class);
//        View root = inflater.inflate(R.layout.fragment_more, container, false);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_more,container,false);
        binding.blank.post(new Runnable() {
            @Override
            public void run() {
                int contenheight = binding.llContent.getHeight();
                int screenHeight = ScreenUtil.getScreenHeight(getContext());
                ViewGroup.LayoutParams layoutParams = binding.blank.getLayoutParams();
                if (contenheight < screenHeight){
                    layoutParams.height = screenHeight - contenheight;
                }
                binding.blank.setLayoutParams(layoutParams);
            }
        });
        notificationsViewModel.getText().observe(this, new Observer<DataMessage>() {
            @Override
            public void onChanged(DataMessage dataMessage) {
                Log.w("BluetoothDataIOServer","getting msg:" + dataMessage.what + "," + dataMessage.getRepeatTime());
                if (isAdded()){
                    handleMessage(dataMessage);
                }
            }
        });
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.tvFile.setOnClickListener(this);
        binding.tvStart.setOnClickListener(this);
        binding.tvOpen.setOnClickListener(this);
        binding.tvDebug.setOnClickListener(this);
        binding.tvAppVersion.setText("V" + BuildConfig.VERSION_NAME);
    }

    private void showPasswordDialog(){
        MssageDialog dialog = MssageDialog.getPasswordInstance();
        dialog.setListener(new MssageDialog.onPasscheckListener() {
            @Override
            public void onPassCheckOk() {
                BluetoothDataIOServer server = BluetoothDataIOServer.getInstance();
//                if (server.isConnected()){
                    server.setPageTag(DataMessage.PAGE_DEBUG);
                    Intent intent = new Intent(getContext(), ActivityDebug.class);
                    startActivity(intent);
//                } else {
//                    showMsg(getString(R.string.app_device_hint));
//                }

            }

            @Override
            public void onPassCheckFail(String msg) {
                showMsg(msg);
            }
        });
        dialog.show(getChildFragmentManager(),"pass");
    }

    private void showMsgDialog(String msg, String okBtn, final int tag){
        MssageDialog dialog = MssageDialog.getInstance(msg,okBtn);
//        dialog.setCancelable(false);
        dialog.setBtnClickListener(new MssageDialog.onBtnClickListener() {
            @Override
            public void onOkClick() {
                if (tag == DataMessage.HAND_SHAKE_SUCCESS){
                    needRestartHandshake = true;
                    goToReconnect();
                }
            }

            @Override
            public void onCancelClick() {

            }
        });
        dialog.show(getChildFragmentManager(),"message");
    }

    private void showMsgDialog(String msg){
        showMsgDialog(msg,null,0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null == processDialog){
            processDialog = new YWLoadingDialog(getContext());
        }
        BluetoothDataIOServer server = BluetoothDataIOServer.getInstance();
//        if (server.isConnected()){
            server.setPageTag(DataMessage.PAGE_MORE);
//        }
//        if (server.isConnected()){
            checkIfNeedRestartHandshake();
//        }
    }

    private boolean restartHandShakeFlag;
    private boolean needRestartHandshake;
    private void checkIfNeedRestartHandshake(){
        if (!restartHandShakeFlag && needRestartHandshake){
            restartHandShakeFlag = true;
            processDialog.showWithMsg("HandShaking");
            notificationsViewModel.startCPUHandShaking();
        }
    }

    private void handleMessage(DataMessage message){
        if (null == processDialog){
            return;
        }
        if (null != message){
            switch (message.what){
                case DataMessage.CONNECT_STATUS:
                    if (notificationsViewModel.getIOServer().isConnected()){
                        checkIfNeedRestartHandshake();
                    }
                    break;
                case DataMessage.GET_COPY_DATA_FAIL:
                    processDialog.dismiss();
                    showMsgDialog("更新失败");
                    break;
                case DataMessage.GET_COPY_DATA:
                    processDialog.updateValue("Updating:" + message.getRepeatTime() + "/" + notificationsViewModel.fileData.size());
                    if (message.getRepeatTime() == notificationsViewModel.fileData.size()){
                        processDialog.dismiss();
                        showMsgDialog("更新成功");
                        notificationsViewModel.resetCopy();
                    }
                    else {
                        notificationsViewModel.update(message.getRepeatTime());
                    }
                    break;
                case DataMessage.HAND_SHAKE_FAIL:
                    processDialog.dismiss();
                    showMsgDialog("CPU握手失败");
                    break;
                case DataMessage.HAND_SHAKE_SUCCESS:
                    processDialog.dismiss();
                    if (needRestartHandshake){
                        needRestartHandshake = false;
                        processDialog.showWithMsg("Updating");
                        notificationsViewModel.startUpdate();
                    }
                    else {
                        showMsgDialog("CPU握手完成,请等待设备重启","重启好了",DataMessage.HAND_SHAKE_SUCCESS);
                    }
                    break;
                case DataMessage.UPDATE_HAND_SHAKING:
                    processDialog.updateValue("HandShaking:" + message.getRepeatTime());
                    break;
            }
        }
    }

    private void goToReconnect(){
        Intent intent = new Intent(getContext(), ChooseDeviceActivity.class);
        startActivity(intent);
    }

    private void showUpdateDialog(String fileName, final String filePath){
        final UpdateDialog dialog = UpdateDialog.getInstance(fileName);
        dialog.setListener(new UpdateDialog.OnCPUChooseListener() {
            @Override
            public void onCPUChoose(byte cpuIndex) {
                notificationsViewModel.readFile(filePath,cpuIndex);
                notificationsViewModel.startCPUHandShaking();
                processDialog.showWithMsg("HandShaking");
            }
        });
        dialog.show(getChildFragmentManager(),"update");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvStart:
                break;
            case R.id.tvOpen:
                break;
            case R.id.tvDebug:
                showPasswordDialog();
                break;
            case R.id.tvFile:
                FileSelectionFragment fragment = FileSelectionFragment.getInstance("hex");
                fragment.setObtainFilesAddress(new ObtainFilesAddress() {
                    @Override
                    public void fileUrls(@NotNull Document fiels) {
                        Log.e("FileSelection"," name:" + fiels.name + " path:" + fiels.path);
                        showUpdateDialog(fiels.name,fiels.path);
                    }
                });
                fragment.show(getChildFragmentManager(),"FileSelection");
                break;
        }
    }

    private void showMsg(String msg){
        if (!TextUtils.isEmpty(msg)){
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}

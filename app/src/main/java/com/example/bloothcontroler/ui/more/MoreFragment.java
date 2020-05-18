package com.example.bloothcontroler.ui.more;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.FragmentMoreBinding;
import com.example.bloothcontroler.service.BluetoothDataIOServer;
import com.example.bloothcontroler.service.DataMessage;
import com.example.bloothcontroler.ui.ActivityDebug;
import com.example.bloothcontroler.ui.ChooseDeviceActivity;
import com.example.bloothcontroler.ui.ScreenUtil;
import com.example.bloothcontroler.ui.dialog.MssageDialog;
import com.example.bloothcontroler.ui.notifications.NotificationsViewModel;

/**
 * @author Hanwenhao
 * @date 2020/3/31
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class MoreFragment extends Fragment implements View.OnClickListener {
    private MoreFragmentViewModel notificationsViewModel;
    private FragmentMoreBinding binding;
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
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.tvFile.setOnClickListener(this);
        binding.tvStart.setOnClickListener(this);
        binding.tvOpen.setOnClickListener(this);
        binding.tvDebug.setOnClickListener(this);
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

    @Override
    public void onResume() {
        super.onResume();
        BluetoothDataIOServer server = BluetoothDataIOServer.getInstance();
//        if (server.isConnected()){
            server.setPageTag(DataMessage.PAGE_MORE);
//        }
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
                break;
        }
    }

    private void showMsg(String msg){
        if (!TextUtils.isEmpty(msg)){
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
}

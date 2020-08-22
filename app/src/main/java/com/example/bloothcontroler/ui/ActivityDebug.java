package com.example.bloothcontroler.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.ActivityDebugBinding;
import com.example.bloothcontroler.service.DataMessage;
import com.example.bloothcontroler.service.OrderCreater;
import com.example.bloothcontroler.ui.dashboard.DashboardViewModel;

public class ActivityDebug extends AppCompatActivity {
    private static final String TAG = "ActivityDebug";
    private ActivityDebugBinding binding;
    private DebugViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel =
                ViewModelProviders.of(this).get(DebugViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_debug);
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewModel.getText().observe(this, new Observer<DataMessage>() {
            @Override
            public void onChanged(@Nullable DataMessage s) {
                handleMessage(s);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume,startCover");
        viewModel.setReady(true);
        viewModel.startCover(OrderCreater.debugOrder(),2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onResume,stopCover");
        viewModel.setReady(false);
    }

    private void handleMessage(DataMessage message) {
        if (null != message) {
            switch (message.what) {
                case DataMessage.RECEVED_DEBUG_DATA:
                    int[] data = message.getData();
                    if (data.length > 43){
                        binding.tvVocs1.setText(viewModel.getValueStr(data[0]));
                        binding.tvIscs1.setText(viewModel.getValueStr(data[1]));
                        binding.tvIrrs1.setText(viewModel.getValueStr(data[2]));
                        binding.tvTemps1.setText(viewModel.getValueStr(data[3]));
                        binding.tvVmps1.setText(viewModel.getValueStr(data[4]));
                        binding.tvImps1.setText(viewModel.getValueStr(data[5]));
                        binding.tvVocs2.setText(viewModel.getValueStr(data[6]));
                        binding.tvIscs2.setText(viewModel.getValueStr(data[7]));
                        binding.tvIrrs2.setText(viewModel.getValueStr(data[8]));
                        binding.tvTemps2.setText(viewModel.getValueStr(data[9]));
                        binding.tvVmps2.setText(viewModel.getValueStr(data[10]));
                        binding.tvImps2.setText(viewModel.getValueStr(data[11]));
                        binding.tvVocs3.setText(viewModel.getValueStr(data[12]));
                        binding.tvIscs3.setText(viewModel.getValueStr(data[13]));
                        binding.tvIrrs3.setText(viewModel.getValueStr(data[14]));
                        binding.tvTemps3.setText(viewModel.getValueStr(data[15]));
                        binding.tvVmps3.setText(viewModel.getValueStr(data[16]));
                        binding.tvImps3.setText(viewModel.getValueStr(data[17]));
                        binding.tvVocs4.setText(viewModel.getValueStr(data[18]));
                        binding.tvIscs4.setText(viewModel.getValueStr(data[19]));
                        binding.tvIrrs4.setText(viewModel.getValueStr(data[20]));
                        binding.tvTemps4.setText(viewModel.getValueStr(data[21]));
                        binding.tvVmps4.setText(viewModel.getValueStr(data[22]));
                        binding.tvImps4.setText(viewModel.getValueStr(data[23]));
                        binding.tvVbus.setText(viewModel.getValueStr(data[24]));
                        binding.tvIO.setText(viewModel.getValueStr(data[25]));
                        binding.tvVki.setText(viewModel.getValueStr(data[26]));
                        binding.tvVbuslkp.setText(viewModel.getValueStr(data[27]));
                        binding.tvVbuslki.setText(viewModel.getValueStr(data[28]));
                        binding.tvIscstc.setText(viewModel.getValueStr(data[29]));
                        binding.tvVkp.setText(viewModel.getValueStr(data[30]));
                        binding.tvIki.setText(viewModel.getValueStr(data[31]));
                        binding.tvVbuslki2.setText(viewModel.getValueStr(data[32]));
                        binding.res1.setText(viewModel.getValueStr(data[33]));
                        binding.res2.setText(viewModel.getValueStr(data[34]));
                        binding.res3.setText(viewModel.getValueStr(data[35]));
                        binding.res4.setText(viewModel.getValueStr(data[36]));
                        binding.res5.setText(viewModel.getValueStr(data[37]));
                        binding.res6.setText(viewModel.getValueStr(data[38]));
                        binding.res7.setText(viewModel.getValueStr(data[39]));
                        binding.res8.setText(viewModel.getValueStr(data[40]));
                        binding.res9.setText(viewModel.getValueStr(data[41]));
                        binding.res10.setText(viewModel.getValueStr(data[42]));
                        binding.res11.setText(viewModel.getValueStr(data[43]));
                    }
                    break;
            }
        }
    }
}

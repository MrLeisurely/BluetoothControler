package com.example.bloothcontroler.ui.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.FragmentDashboardBinding;
import com.example.bloothcontroler.service.DataMessage;
import com.example.bloothcontroler.service.OrderCreater;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_dashboard,container,false);
        binding.setModel(dashboardViewModel);
        dashboardViewModel.getText().observe(this, new Observer<DataMessage>() {
            @Override
            public void onChanged(@Nullable DataMessage s) {
                handleMessage(s);
            }
        });
        return binding.getRoot();
    }

    private void handleMessage(DataMessage message) {
        if (null != message) {
            switch (message.what) {
                case DataMessage.RECEVED_SETTING_DATA:
                    int[] data = message.getData();
                    if (data.length > 9){
                        binding.edPamx.setText(String.valueOf(dashboardViewModel.getValue(data[0])));
                        binding.edVmp.setText(String.valueOf(dashboardViewModel.getValue(data[1])));
                        binding.edImp.setText(String.valueOf(dashboardViewModel.getValue(data[2])));
                        binding.edVoc.setText(String.valueOf(dashboardViewModel.getValue(data[3])));
                        binding.edIsc.setText(String.valueOf(dashboardViewModel.getValue(data[4])));
                        if (data[5] == 1){
                            binding.tvType.setText("Mono");
                        } else {
                            binding.tvType.setText("Poly");
                        }
                        if (data[6] == 1){
                            binding.tvGlass.setText("Double");
                        } else {
                            binding.tvGlass.setText("Single");
                        }
                        binding.edCells.setText(String.valueOf(data[7]));
                        binding.edYear.setText(String.valueOf(dashboardViewModel.getValue(data[8])));
                        binding.edModuls.setText(String.valueOf(data[9]));
                        showMsg("Successful!");
                    }
                    break;
            }
        }
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.tvType.setOnClickListener(this);
        binding.tvGlass.setOnClickListener(this);
        binding.imageView.setOnClickListener(this);
        binding.imgEdit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageView:
                if (dashboardViewModel.isBTConnected()){
                    dashboardViewModel.sendOrder(OrderCreater.generalReadOrder(OrderCreater.Pamx,10));

                }
                break;
            case R.id.imgEdit:
                if (dashboardViewModel.isBTConnected()){
                    if (checkDataOK()){
                        dashboardViewModel.sendOrder(editOrder);
                        showMsg("Successful!");
                    }
                }
                break;
            case R.id.tvType:
                showSetDialog(TYPE);
                break;
            case R.id.tvGlass:
                showSetDialog(GLASS);
                break;
        }
    }

    private byte[] editOrder;

    private boolean checkDataOK(){
        try {
            double pamx = Double.parseDouble(binding.edPamx.getText().toString());
            double vmp = Double.parseDouble(binding.edVmp.getText().toString());
            double imp = Double.parseDouble(binding.edImp.getText().toString());
            double voc = Double.parseDouble(binding.edVoc.getText().toString());
            double isc = Double.parseDouble(binding.edIsc.getText().toString());
            double cells = Double.parseDouble(binding.edCells.getText().toString());
            double years = Double.parseDouble(binding.edYear.getText().toString());
            double moduls = Double.parseDouble(binding.edModuls.getText().toString());
            if (pamx > 1000 || pamx < 0){
                showMsg("Pamx 取值为 0-1000W");
                return false;
            }
            if (vmp > 110 || vmp < 0){
                showMsg("Vmp 取值为 0-110V");
                return false;
            }
            if (imp > 5 || imp < 0){
                showMsg("Imp 取值为 0-5A");
                return false;
            }
            int p = (int) (pamx * 10);
            int v = (int) (vmp * 10);
            int i = (int) (imp * 10);
//            if (p * 10 !=  v * i){
//                showMsg("Pamx != Vmp * Imp,请检查输入数据");
//                return false;
//            }
            if (voc > 110 || voc < -110){
                showMsg("Voc 取值为 +-110V");
                return false;
            }
            if (isc > 5 || isc < 0){
                showMsg("Isc 取值为 0-5A");
                return false;
            }
            if (cells > 200 || cells < 0){
                showMsg("Cells 取值为 0-200");
                return false;
            }
            if (years > 5 || years < 0){
                showMsg("Year 取值为 0-5");
                return false;
            }
            if (moduls > 50 || moduls < 0){
                showMsg("Moduls 取值为 0-50");
                return false;
            }
            editOrder = OrderCreater.getWriteDataOrder(OrderCreater.Pamx,10,
                    p,
                    v,
                    i,
                    (int) (voc * 10),
                    (int) (isc * 10),
                    typeValue,
                    glassValue,
                    (int) (cells),
                    (int) (years * 10),
                    (int) (moduls)
            );
        } catch (Exception e){
            showMsg("读取错误，请输入正确数值");
            return false;
        }
        return true;
    }

    private void showMsg(String msg){
        if (!TextUtils.isEmpty(msg)){
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    private int typeValue = 0;
    private int glassValue = 0;

    private static final int TYPE = 1;
    private static final int GLASS = 2;
    private void showSetDialog(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_setlanguage, null);
        builder.setView(view);
        final AlertDialog dialog = builder.show();
        ListView listView = view.findViewById(R.id.language_content_list);

        final String[] languages;
        if (type == TYPE){
            languages = new String[]{"Poly","Mono"};
        } else {
            languages = new String[]{"Single","Double"};
        }

        listView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, languages));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type == TYPE){
                    dashboardViewModel.mType.set(languages[position]);
                    typeValue = position;
                } else {
                    dashboardViewModel.mGlass.set(languages[position]);
                    glassValue = position;
                }
                dialog.dismiss();
            }
        });

    }
}
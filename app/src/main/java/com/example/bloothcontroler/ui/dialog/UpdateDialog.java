package com.example.bloothcontroler.ui.dialog;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.UpdateDialogBinding;
import com.example.bloothcontroler.service.OrderCreater;

/**
 * @author Hanwenhao
 * @date 2021/4/4
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class UpdateDialog extends DialogFragment {
    public static UpdateDialog getInstance(String fileName) {
        Bundle bundle = new Bundle();
        bundle.putString("fileName", fileName);
        UpdateDialog dialog = new UpdateDialog();
        dialog.setArguments(bundle);
        return dialog;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Mdialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            getDialog().getWindow().setLayout((int) (0.8 * displayMetrics.widthPixels), ViewGroup.LayoutParams.WRAP_CONTENT);
        } catch (Exception e) {

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final UpdateDialogBinding  binding = DataBindingUtil.inflate(inflater,R.layout.update_dialog,container,false);
        Bundle bundle = getArguments();
        if (null != bundle){
            String fileName = bundle.getString("fileName","");
            binding.tvFileName.setText(fileName);
            binding.btncpu01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener){
                        listener.onCPUChoose(OrderCreater.CPU01);
                    }
                    dismiss();
                }
            });
            binding.btncpu02.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener){
                        listener.onCPUChoose(OrderCreater.CPU02);
                    }
                    dismiss();
                }
            });
        }

        return binding.getRoot();
    }

    public void setListener(OnCPUChooseListener listener) {
        this.listener = listener;
    }

    private OnCPUChooseListener listener;

    public interface OnCPUChooseListener {
        void onCPUChoose(byte cpuIndex);
    }
}

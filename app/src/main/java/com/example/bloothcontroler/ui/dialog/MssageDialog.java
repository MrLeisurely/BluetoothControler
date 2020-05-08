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
import com.example.bloothcontroler.databinding.AppDialogBinding;

/**
 * @author Hanwenhao
 * @date 2020/5/8
 * @Description
 * @update [序号][日期YYYY-MM-DD] [更改人姓名][变更描述]
 */
public class MssageDialog extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppDialogBinding binding = DataBindingUtil.inflate(inflater, R.layout.app_dialog,container,false);
        binding.btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Bundle bundle = getArguments();
        if (null != bundle){
            String msg = bundle.getString("msg","");
            binding.tvContent.setText(msg);
        }
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,R.style.Mdialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            getDialog().getWindow().setLayout((int) (0.8 * displayMetrics.widthPixels),ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        catch (Exception e){

        }
    }

    public static MssageDialog getInstance(String msg){
        Bundle bundle = new Bundle();
        bundle.putString("msg",msg);
        MssageDialog dialog = new MssageDialog();
        dialog.setArguments(bundle);
        return dialog;
    }
}

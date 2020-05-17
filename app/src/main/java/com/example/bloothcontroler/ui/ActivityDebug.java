package com.example.bloothcontroler.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.example.bloothcontroler.R;
import com.example.bloothcontroler.databinding.ActivityDebugBinding;
import com.example.bloothcontroler.ui.dashboard.DashboardViewModel;

public class ActivityDebug extends AppCompatActivity {
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
    }
}

package com.example.bloothcontroler.ui.dashboard;

import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bloothcontroler.base.BaseBCViewModel;
import com.example.bloothcontroler.service.BluetoothDataIOServer;

public class DashboardViewModel extends BaseBCViewModel {

    public ObservableField<String> mType;
    public ObservableField<String> mGlass;
    public DashboardViewModel() {
//        mType = new ObservableField<>("Poly");
//        mGlass = new ObservableField<>("Single");
//        mText.setValue("This is dashboard fragment");
    }
}
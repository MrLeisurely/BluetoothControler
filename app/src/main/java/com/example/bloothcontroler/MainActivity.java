package com.example.bloothcontroler;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.bloothcontroler.service.BluetoothDataIOServer;
import com.example.bloothcontroler.service.DataMessage;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tencent.bugly.crashreport.CrashReport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CrashReport.initCrashReport(this,"0d554cdaf6",true);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                BluetoothDataIOServer server = BluetoothDataIOServer.getInstance();
                switch (destination.getId()){
                    case R.id.navigation_home:
                        Log.w("MainActivity","navigation_status");
                        if (server.isConnected()){
                            server.setPageTag(DataMessage.PAGE_STATUS);
                        }
                        break;
                    case R.id.navigation_dashboard:
                        Log.w("MainActivity","navigation_setting");
                        if (server.isConnected()){
                            server.setPageTag(DataMessage.PAGE_SETTING);
                        } else {
                            showMsg("请先连接设备");
                        }
                        break;
                    case R.id.navigation_notifications:
                        Log.w("MainActivity","navigation_iv");
                        if (server.isConnected()){
                            server.setPageTag(DataMessage.PAGE_IV);
                        } else {
                            showMsg("请先连接设备");
                        }
                        break;
                    case R.id.navigation_more:
                        Log.w("MainActivity","navigation_more");
                        if (server.isConnected()){
                            server.setPageTag(DataMessage.PAGE_MORE);
                        }
                        break;

                }
            }
        });
        BluetoothDataIOServer.getInstance().setPageTag(DataMessage.PAGE_STATUS);
        NavigationUI.setupWithNavController(navView, navController);
        //test create
    }

    private void showMsg(String msg){
        if (!TextUtils.isEmpty(msg)){
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}

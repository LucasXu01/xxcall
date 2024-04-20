package com.lucas.xxcall;

import static com.lucas.xxcall.ui.dashboard.DashboardFragment.PICK_FILE_REQUEST_CODE;
import static com.lucas.xxcall.ui.dashboard.DashboardFragment.STORAGE_PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.blankj.utilcode.util.ScreenUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.lucas.xxcall.databinding.ActivityMainmainBinding;
import com.lucas.xxcall.event.MessageEvent2;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private ActivityMainmainBinding binding;
    //拨号请求码
    public static final int REQUEST_CALL_PERMISSION = 10111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.getSupportActionBar().hide();
        EventBus.getDefault().register(this);

        binding = ActivityMainmainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);


        // 设置状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }



        // 检查权限
        checkStoragePermission();
        checkReadPermission2(Manifest.permission.CALL_PHONE, REQUEST_CALL_PERMISSION);
    }


    //打电话申请权限，
    public boolean checkReadPermission2(String string_permission, int request_code) {
        boolean flag = false;
//已有权限
        if (ContextCompat.checkSelfPermission(this, string_permission) == PackageManager.PERMISSION_GRANTED) {
            flag = true;
        } else {
//申请权限
            ActivityCompat.requestPermissions(this, new String[]{string_permission}, request_code);
        }
        return flag;
    }


    // 检查存储权限
    private void checkStoragePermission() {
        // 如果Android版本在Marshmallow以上，需要动态请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                // 如果权限没有被授予，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE},
                        STORAGE_PERMISSION_REQUEST_CODE);
            } else {
                // 权限已经被授予
                // 在这里处理您的逻辑
            }
        } else {
            // 如果Android版本在Marshmallow以下，无需请求权限
            // 在这里处理您的逻辑
        }


    }

    // 启动文件选择器
    private void pickExcelFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.ms-excel");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Excel File"), PICK_FILE_REQUEST_CODE);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent2 event) {
       pickExcelFile();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        EventBus.getDefault().post(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
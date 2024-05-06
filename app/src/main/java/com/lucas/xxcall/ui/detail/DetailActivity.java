package com.lucas.xxcall.ui.detail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.lucas.xxcall.MainActivity;
import com.lucas.xxcall.R;
import com.lucas.xxcall.bean.BookBean;
import com.lucas.xxcall.event.MessageEvent;
import com.lucas.xxcall.login.LoginActivity;
import com.lucas.xxcall.widgets.CustomDialog;
import com.lucas.xxcall.widgets.CustomDialog2;
import com.lucas.xxcall.widgets.IntervalInputDialog;
import com.lucas.xxcall.widgets.ModifyPhoneInputDialog;
import com.lucas.xxcall.widgets.PhineNumberInputDialog;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import razerdp.basepopup.QuickPopupBuilder;
import razerdp.basepopup.QuickPopupConfig;
import razerdp.widget.QuickPopup;

public class DetailActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    //拨号请求码
    public static final int REQUEST_CALL_PERMISSION = 10111;


    private Button button_dual_sim_settings;
    private Button button_interval_duration;
    private Button button_start_auto_dial;
    private RecyclerView recyclerView;
    private PhoneAdapter adapter;
    private List<PhoneBean> phoneList = new ArrayList<>();
    private int 间隔时间 = 3;
    private int 拨打卡号 = 0; // 0为sim1   1为sim2
    private int preSim = 0;

    Button uncalledButton;
    Button calledButton;

    public int Position = 0;
    public BookBean bookBean;
    public View imgnodata;

    public boolean 是否在循环播号 = false;
    public ImageView imgmore;
    QuickPopupBuilder quickPopupBuilder;
    TextView tvTitle;

    QuickPopup quickPopup;
    QuickPopup quickPopup3;
    QuickPopup quickPopup2;
    ImageView img_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        EventBus.getDefault().register(this);

        // 设置状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        this.getSupportActionBar().hide();
        findViewById(R.id.img_back).setOnClickListener(view -> onBackPressed());
        imgnodata = findViewById(R.id.imgnodata);


        Long bookid = getIntent().getLongExtra("bookid", 1l);
        bookBean = LitePal.where("bookid = ?", String.valueOf(bookid)).findFirst(BookBean.class, true);
        bookBean.phoneBeans = LitePal.where("bookid = ?", String.valueOf(bookid)).find(PhoneBean.class);


        img_add = findViewById(R.id.img_add);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(bookBean.bookName);

        imgmore = findViewById(R.id.imgmore);

        button_dual_sim_settings = findViewById(R.id.button_dual_sim_settings);
        button_start_auto_dial = findViewById(R.id.button_start_auto_dial);
        button_interval_duration = findViewById(R.id.button_interval_duration);

        button_interval_duration.setOnClickListener(v -> {
            IntervalInputDialog dialog = new IntervalInputDialog(this);
            dialog.setOnIntervalSetListener(new IntervalInputDialog.OnIntervalSetListener() {
                @Override
                public void onIntervalSet(int interval) {
                    // 在这里处理用户输入的间隔参数
                    // interval 即为用户输入的间隔参数
                    间隔时间 = interval;
//                    ToastUtils.showShort("已设置间隔为" + interval + "秒");
                    button_interval_duration.setText("间隔时长（" + interval + "s）");

                }
            });
            dialog.show();

        });

        button_start_auto_dial.setOnClickListener(v -> {
            是否在循环播号 = !是否在循环播号;
            gotoCallPhone();
            if (是否在循环播号) {
                button_start_auto_dial.setText("停止自动拨号");
            }else {
                button_start_auto_dial.setText("开始自动拨号");
            }

        });

//        设置拨打的sim卡
        button_dual_sim_settings.setOnClickListener(V->{
            if ( 拨打卡号 == 0) {
                拨打卡号 = 1;
                button_dual_sim_settings.setText("双卡设置（卡2）");
                return;
            }

            if ( 拨打卡号 == 1) {
                拨打卡号 = 2;
                button_dual_sim_settings.setText("双卡设置（卡12轮播）");
                return;
            }

            if ( 拨打卡号 == 2) {
                拨打卡号 = 0;
                button_dual_sim_settings.setText("双卡设置（卡1）");
                return;
            }

        });


        // 检查权限
        checkStoragePermission();
        checkReadPermission2(Manifest.permission.CALL_PHONE, REQUEST_CALL_PERMISSION);


        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhoneAdapter(phoneList);
        adapter.setOnPhoneItemClickListener((position, view) -> showItemPopup( view, phoneList.get(position)));
        recyclerView.setAdapter(adapter);

        uncalledButton = findViewById(R.id.uncalledButton);
        uncalledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Position = 0 ;
                updateList(false);
                calledButton.setAlpha(0.5f);
                uncalledButton.setAlpha(1f);
            }
        });

        calledButton = findViewById(R.id.calledButton);
        calledButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Position = 1 ;
                updateList(true);
                calledButton.setAlpha(1f);
                uncalledButton.setAlpha(0.5f);
            }
        });



        refreshUI();
        for (PhoneBean phoneBean: bookBean.phoneBeans) {
            if (phoneBean.isCalled == false) {
                phoneList.add(phoneBean);
            }
        }
        adapter.notifyDataSetChanged();


        calledButton.setAlpha(0.5f);
        uncalledButton.setAlpha(1f);


        imgmore.setOnClickListener(view -> {
            showPopup(view);
        });

        img_add.setOnClickListener(view -> {
            showPopup3(view);
        });


    }

    @SuppressLint("SuspiciousIndentation")
    private void showPopup(View anchorView) {
        quickPopupBuilder = QuickPopupBuilder.with(DetailActivity.this)
                .contentView(R.layout.popup_detail_more)
                .config(new QuickPopupConfig()
                        .gravity(Gravity.LEFT | Gravity.BOTTOM)
                        .withClick(R.id.tx_1, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new XPopup.Builder(DetailActivity.this).asInputConfirm("重命名号码库", "请输入内容。",
                                                new OnInputConfirmListener() {
                                                    @Override
                                                    public void onConfirm(String text) {
                                                        bookBean.bookName = text;
                                                        bookBean.save();
                                                        tvTitle.setText(bookBean.bookName);
                                                        quickPopup.dismiss();
                                                    }
                                                })
                                        .show();
                            }
                        })
                        .withClick(R.id.tx_2, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bookBean.delete();
                                finish();
                                ToastUtils.showShort("已删除该号码库");
                                quickPopup.dismiss();
                            }
                        })

                );
        quickPopup = quickPopupBuilder.build();
        quickPopup.showPopupWindow(anchorView);
    }

    @SuppressLint("SuspiciousIndentation")
    private void showPopup3(View anchorView) {
        quickPopupBuilder = QuickPopupBuilder.with(DetailActivity.this)
                .contentView(R.layout.popup_add_phone)
                .config(new QuickPopupConfig()
                        .gravity(Gravity.LEFT | Gravity.TOP)
                        .withClick(R.id.button_select_file, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pickExcelFile();
                                quickPopup3.dismiss();
                            }
                        })
                        .withClick(R.id.img, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CustomDialog dialog = new CustomDialog(DetailActivity.this);
                                dialog.show();
                            }
                        })
                        .withClick(R.id.button_add_from_clipboard, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 导入剪切板
                                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
                                    CharSequence clipboardText = clipboardManager.getPrimaryClip().getItemAt(0).getText();
                                    if (!TextUtils.isEmpty(clipboardText)) {
                                        List<PhoneBean> clipboardList = parseClipboardText(clipboardText.toString());

                                        //添加到列表中
                                        if (clipboardList != null && clipboardList.size() >0){
                                            List<PhoneBean> rawList = new ArrayList<>();
                                            rawList.addAll(phoneList);
                                            rawList.addAll(0, clipboardList);
                                            refreshData(rawList);
                                        }
                                    }
                                }
                                quickPopup3.dismiss();
                            }
                        })
                        .withClick(R.id.img2, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CustomDialog2 dialog = new CustomDialog2(DetailActivity.this);
                                dialog.show();
                            }
                        })
                        .withClick(R.id.button_add_from_input, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                PhineNumberInputDialog dialog = new PhineNumberInputDialog(DetailActivity.this);
                                dialog.setOnIntervalSetListener(new PhineNumberInputDialog.OnModifySetListener() {
                                    @Override
                                    public void onModify(String text) {

                                        CharSequence clipboardText = text;
                                        if (!TextUtils.isEmpty(clipboardText)) {
                                            List<PhoneBean> clipboardList = parseClipboardText(clipboardText.toString());

                                            //添加到列表中
                                            if (clipboardList != null && clipboardList.size() > 0) {
                                                List<PhoneBean> rawList = new ArrayList<>();
                                                rawList.addAll(phoneList);
                                                rawList.addAll(0, clipboardList);
                                                refreshData(rawList);
                                            }
                                        }

                                    }
                                });
                                dialog.show();


//                                new XPopup.Builder(DetailActivity.this).asInputConfirm("手动录入", "规则同剪切板（每一行为：李雷 156712345657）",
//                                                "\n\n\n\n\n\n\n",
//                                                new OnInputConfirmListener() {
//                                                    @Override
//                                                    public void onConfirm(String text) {
//
//                                                        CharSequence clipboardText = text;
//                                                        if (!TextUtils.isEmpty(clipboardText)) {
//                                                            List<PhoneBean> clipboardList = parseClipboardText(clipboardText.toString());
//
//                                                            //添加到列表中
//                                                            if (clipboardList != null && clipboardList.size() >0){
//                                                                List<PhoneBean> rawList = new ArrayList<>();
//                                                                rawList.addAll(phoneList);
//                                                                rawList.addAll(0, clipboardList);
//                                                                refreshData(rawList);
//                                                            }
//                                                        }
//
//                                                        quickPopup3.dismiss();
//                                                    }
//                                                })
//                                        .show();
                            }
                        })


                );
        quickPopup3 = quickPopupBuilder.build();
        quickPopup3.showPopupWindow(anchorView);
    }

    @SuppressLint("SuspiciousIndentation")
    private void showItemPopup(View anchorView, PhoneBean phoneBean) {
        quickPopupBuilder = QuickPopupBuilder.with(DetailActivity.this)
                .contentView(R.layout.popup_detail_item_more)
                .config(new QuickPopupConfig()
                        .gravity(Gravity.LEFT | Gravity.BOTTOM)
                        .withClick(R.id.tx_1, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 呼叫
                                callPhoneNumber(phoneBean.Phone, 0);
                                quickPopup.dismiss();
                            }
                        })
                        .withClick(R.id.tx_2, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new XPopup.Builder(DetailActivity.this).asConfirm("确认弹窗", "是否删除号码",
                                                new OnConfirmListener() {
                                                    @Override
                                                    public void onConfirm() {
                                                        phoneBean.delete();
                                                        phoneList.remove(phoneBean);
                                                        adapter.notifyDataSetChanged();
                                                        quickPopup.dismiss();
                                                    }
                                                })
                                        .show();
                            }
                        })
                        .withClick(R.id.tx_3, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 添加备注
                                new XPopup.Builder(DetailActivity.this).asInputConfirm("添加备注", "请输入内容。",
                                                new OnInputConfirmListener() {
                                                    @Override
                                                    public void onConfirm(String text) {
                                                        phoneBean.beizhu = text;
                                                        quickPopup.dismiss();
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                })
                                        .show();
                            }
                        })
                        .withClick(R.id.tx_4, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 发短信
                                // 替换成你想要发送短信的手机号
                                String phoneNumber = phoneBean.Phone;
                                // 创建发送短信的Intent
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                // 设置短信发送的目标号码
                                intent.setData(Uri.parse("smsto:" + phoneNumber));
                                // 启动发短信页面
                                startActivity(intent);
                                quickPopup.dismiss();
                            }
                        })
                                .withClick(R.id.tx_5, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // 修改名字
                                        ModifyPhoneInputDialog dialog = new ModifyPhoneInputDialog(DetailActivity.this, phoneBean.Phone);
                                        dialog.setOnIntervalSetListener(new ModifyPhoneInputDialog.OnModifySetListener() {
                                            @Override
                                            public void onModify(String newPhone) {
                                                phoneBean.Phone = newPhone;
                                                phoneBean.save();
                                                adapter.notifyDataSetChanged();
                                                quickPopup.dismiss();
                                            }
                                        });
                                        dialog.show();
                                    }
                                })



                );
        quickPopup = quickPopupBuilder.build();
        quickPopup.showPopupWindow(anchorView);
    }

    public void refreshUI() {
        if (bookBean == null || bookBean.phoneBeans ==null || bookBean.phoneBeans.size()<1) {
            imgnodata.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }else {
            imgnodata.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    private List<PhoneBean> parseClipboardText(String clipboardText) {
        List<PhoneBean> phoneList = new ArrayList<>();
        String[] lines = clipboardText.split("\n");
        for (String line : lines) {
            String[] parts = line.split(" ");
            if (parts.length == 2) {
                String name = parts[0];
                String phone = parts[1];
                PhoneBean phoneBean = new PhoneBean(name, phone);
                phoneList.add(phoneBean);
            }
        }
        return phoneList;
    }

    // 根据isCalled字段更新列表显示
    private void updateList(boolean isCalled) {
        List<PhoneBean> filteredList = new ArrayList<>();
        for (PhoneBean phone : bookBean.phoneBeans) {
            if (phone.isCalled == isCalled) {
                filteredList.add(phone);
            }
        }
        adapter = new PhoneAdapter(filteredList);
        adapter.setOnPhoneItemClickListener((position, view) -> showItemPopup( view, phoneList.get(position)));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        readExcelFile(UriUtils.uri2File(uri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 读取Excel文件并将第一列和第二列的所有内容存储到两个列表中
    public void readExcelFile(File file) {
        List<PhoneBean> rawList = new ArrayList<>();

        try {
            // 创建工作簿对象
            Workbook workbook = Workbook.getWorkbook(file);

            // 获取第一个工作表
            Sheet sheet = workbook.getSheet(0);

            // 读取每一行，将第一列和第二列的值存储到列表中
            for (int i = 1; i < sheet.getRows(); i++) {
                Cell[] rowCells = sheet.getRow(i);
                if (rowCells.length >= 2) {
                    PhoneBean phoneBean = new PhoneBean(rowCells[0].getContents(), rowCells[1].getContents());
                    rawList.add(phoneBean);
                }
            }

            // 关闭工作簿
            workbook.close();

           refreshData(rawList);
            calledButton.setAlpha(0.5f);

        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }


    public void refreshData(List<PhoneBean> rawList) {
        if (rawList == null || rawList.size() < 1) return;
        phoneList.clear();
        phoneList.addAll(rawList);
        adapter.notifyDataSetChanged();
        rawList.clear();

        List<PhoneBean> phoneBeansTemp = LitePal.where("bookid = ?", String.valueOf(bookBean.bookId)).find(PhoneBean.class);
        for (PhoneBean phoneBean : phoneBeansTemp) {
            phoneBean.delete();
        }

        for (PhoneBean phoneBean : phoneList) {
            phoneBean.bookid = bookBean.bookId;
            phoneBean.save();
        }
        bookBean.phoneBeans = phoneList;
        bookBean.save();
        refreshUI();
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


    private final static String simSlotName[] = {
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx"};



    public void callPhone (int position) {
        if (拨打卡号 == 0) {
            callPhoneDetail(position, 0);
            preSim = 0;
            return;
        }

        if (拨打卡号 == 1) {
            callPhoneDetail(position, 1);
            preSim = 1;
            return;
        }

        if (拨打卡号 == 2) {
            if (preSim == 1){
                callPhoneDetail(position, 0);
                preSim = 0;
            }else {
                callPhoneDetail(position, 1);
                preSim = 1;
            }
            return;
        }
    }
    /**
     * 拨打电话（拨号权限自行处理）

     * @param simIndex ：sim卡的位置 0代表sim卡1，1代表sim卡2

     */

    public void callPhoneDetail(int position, int simIndex) {

        PhoneBean phoneBean = phoneList.get(position);
        phoneList.get(position).isCalled = true;
        phoneList.get(position).save();
        String phoneNum = phoneBean.Phone;

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        //Add all slots here, according to device.. (different device require different key so put all together)
        for (String s : simSlotName)
            intent.putExtra(s, simIndex); //0 or 1 according to sim.......
        //works only for API >= 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(simIndex));
            } catch (Exception e) {
                e.printStackTrace();
                //writeLog("No Sim card? at slot " + simNumber+"\n\n"+e.getMessage(), this);
            }
        }
        startActivity(intent);
    }


    public void callPhoneNumber(String phone, int simIndex) {

        String phoneNum = phone;

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNum));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("com.android.phone.force.slot", true);
        intent.putExtra("Cdma_Supp", true);
        //Add all slots here, according to device.. (different device require different key so put all together)
        for (String s : simSlotName)
            intent.putExtra(s, simIndex); //0 or 1 according to sim.......
        //works only for API >= 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                List<PhoneAccountHandle> phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(simIndex));
            } catch (Exception e) {
                e.printStackTrace();
                //writeLog("No Sim card? at slot " + simNumber+"\n\n"+e.getMessage(), this);
            }
        }
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        // 处理事件
        button_dual_sim_settings.postDelayed(() -> {
            gotoCallPhone();
        }, 1000 * 间隔时间);

        button_dual_sim_settings.postDelayed(() -> {
            if (Position == 0) {
                updateList(false);
            }
            if (Position == 1) {
                updateList(true);
            }
        }, 250);
    }


    public void gotoCallPhone() {
        Log.e("xujinjin", "gotoCallPhone:是否在循环播号 "+ 是否在循环播号);
        if (!是否在循环播号) return;
        if (phoneList.size()<1) {
            ToastUtils.showShort("还未导入xls文件");
            return;
        }

        boolean allCalled = true;
        if (phoneList.size()>1) {
            for (PhoneBean phoneBean: phoneList) {
                if (!phoneBean.isCalled) {
                    allCalled = false;
                }
            }
            if (allCalled) {
                ToastUtils.showShort("列表已经全部拨打过电话！");
                return;
            }

        }


        for (int i = 0 ; i < phoneList.size(); i ++) {
            if (!phoneList.get(i).isCalled) {
                ToastUtils.showShort("开始拨打未拨打的电话号码");
                int finalI = i;
                button_dual_sim_settings.postDelayed(()->{
                    callPhone(finalI);
                }, 1000);

                break;
            }
        }
    }
}
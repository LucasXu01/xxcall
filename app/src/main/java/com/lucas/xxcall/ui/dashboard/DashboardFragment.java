package com.lucas.xxcall.ui.dashboard;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UriUtils;
import com.lucas.xxcall.widgets.CustomDialog;
import com.lucas.xxcall.widgets.CustomDialog2;
import com.lucas.xxcall.widgets.IntervalInputDialog;
import com.lucas.xxcall.event.MessageEvent;
import com.lucas.xxcall.event.MessageEvent2;
import com.lucas.xxcall.widgets.ModifyPhoneInputDialog;
import com.lucas.xxcall.PhoneAdapter;
import com.lucas.xxcall.PhoneBean;
import com.lucas.xxcall.R;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import razerdp.basepopup.QuickPopupBuilder;
import razerdp.basepopup.QuickPopupConfig;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardFragment extends Fragment {

    public static final int PICK_FILE_REQUEST_CODE = 1;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private BookListAdapter adapter;
    private List<PhoneBean> phoneList = new ArrayList<>();
    private PopupWindow popupWindow;

    ImageView addbook;
    ImageView imgnodata;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_home_book, container, false);


        addbook = rootView.findViewById(R.id.addbook);
        imgnodata = rootView.findViewById(R.id.imgnodata);

//        recyclerView = rootView.findViewById(R.id.recyclerView);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        recyclerView.setLayoutManager(layoutManager);
//        adapter = new BookListAdapter(phoneList);
//        recyclerView.setAdapter(adapter);
//
//
//        adapter.setOnPhoneItemClickListener(new BookListAdapter.OnPhoneItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//
//                ModifyPhoneInputDialog dialog = new ModifyPhoneInputDialog(getActivity(), phoneList.get(position).Phone);
//                dialog.setOnIntervalSetListener(new ModifyPhoneInputDialog.OnModifySetListener() {
//                    @Override
//                    public void onModify(String newPhone) {
//                        phoneList.get(position).Phone = newPhone;
//                        adapter.notifyItemChanged(position);
//                    }
//                });
//                dialog.show();
//
//
//            }
//        });




        initPopupWindow();
        // 点击按钮显示PopupWindow
        addbook.setOnClickListener(v -> showPopup(v));



        return rootView;
    }


    private void showPopup(View anchorView) {
        QuickPopupBuilder.with(getContext())
                .contentView(R.layout.popup_normal)
                .config(new QuickPopupConfig()
                        .gravity(Gravity.LEFT | Gravity.BOTTOM)
                        .withClick(R.id.tx_1, v -> new XPopup.Builder(getContext()).asInputConfirm("新建号码库", "请输入内容。",
                                        text -> {

                                        })
                                .show())
                        .withClick(R.id.tx_2, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getContext(), "clicked", Toast.LENGTH_LONG).show();
                            }
                        })

                )
                .show(anchorView);
    }

    private void initPopupWindow(){
        // 初始化PopupWindow中的数据
        List<String> options = new ArrayList<>();
        options.add("新建号码库");
        options.add("删除号码库");

        // 初始化PopupWindow
        LayoutInflater inflater2 = LayoutInflater.from(getActivity());
        View popupView = inflater2.inflate(R.layout.popup_window, null);
        ListView listView = popupView.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        // 创建PopupWindow
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // 点击选项的回调
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                Toast.makeText(getActivity(), "You selected: " + selectedItem, Toast.LENGTH_SHORT).show();

                popupWindow.dismiss();
            }
        });
    }

}
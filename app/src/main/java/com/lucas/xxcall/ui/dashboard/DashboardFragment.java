package com.lucas.xxcall.ui.dashboard;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lucas.xxcall.bean.BookBean;
import com.lucas.xxcall.widgets.ModifyPhoneInputDialog;
import com.lucas.xxcall.PhoneBean;
import com.lucas.xxcall.R;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import razerdp.basepopup.QuickPopupBuilder;
import razerdp.basepopup.QuickPopupConfig;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class DashboardFragment extends Fragment {

    public static final int PICK_FILE_REQUEST_CODE = 1;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private RecyclerView recyclerView;
    private BookListAdapter adapter;
    private List<BookBean> bookList = new ArrayList<>();
    private PopupWindow popupWindow;
    QuickPopupBuilder quickPopupBuilder;

    ImageView addbook;
    ImageView imgnodata;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_home_book, container, false);


        addbook = rootView.findViewById(R.id.addbook);
        imgnodata = rootView.findViewById(R.id.imgnodata);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new BookListAdapter(bookList);
        recyclerView.setAdapter(adapter);
        adapter.setOnPhoneItemClickListener(position -> {

        });

        initBooks();
        initPopupWindow();
        // 点击按钮显示PopupWindow
        addbook.setOnClickListener(v -> showPopup(v));

        return rootView;
    }


    private void initBooks() {
        List<BookBean> listTemp = LitePal.findAll(BookBean.class);
        if (listTemp.size() > 0) {
            bookList.clear();
            bookList.addAll(listTemp);
            adapter.notifyDataSetChanged();
            imgnodata.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }else  {
            imgnodata.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }


    private void showPopup(View anchorView) {
        quickPopupBuilder = QuickPopupBuilder.with(getContext())
                .contentView(R.layout.popup_normal)
                .config(new QuickPopupConfig()
                        .gravity(Gravity.LEFT | Gravity.BOTTOM)
                        .withClick(R.id.tx_1, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new XPopup.Builder(getContext()).asInputConfirm("新建号码库", "请输入内容。",
                                                new OnInputConfirmListener() {
                                                    @Override
                                                    public void onConfirm(String text) {
                                                        BookBean bookBean = new BookBean();
                                                        bookBean.bookName = text;
                                                        bookBean.save();
                                                        initBooks();
                                                    }
                                                })
                                        .show();
                            }
                        })
                        .withClick(R.id.tx_2, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getContext(), "clicked", Toast.LENGTH_LONG).show();
                            }
                        })

                );
                quickPopupBuilder.show(anchorView);
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
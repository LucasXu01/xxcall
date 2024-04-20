package com.lucas.xxcall.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lucas.xxcall.PhoneBean;
import com.lucas.xxcall.R;
import com.lucas.xxcall.bean.BookBean;

import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.PhoneViewHolder> {
    private List<BookBean> bookList;

    public BookListAdapter(List<BookBean> phoneList) {
        this.bookList = phoneList;
    }

    private OnPhoneItemClickListener onPhoneItemClickListener;



    public void setOnPhoneItemClickListener(OnPhoneItemClickListener listener) {
        this.onPhoneItemClickListener = listener;
    }

    @NonNull
    @Override
    public PhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new PhoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneViewHolder holder, int position) {
        BookBean bookBean = bookList.get(position);
        holder.book.setText(bookBean.bookName);

        holder.xiugai.setOnClickListener(v->{
            if (position != RecyclerView.NO_POSITION) {
                onPhoneItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class PhoneViewHolder extends RecyclerView.ViewHolder {
        TextView book;
        TextView xiugai;

        public PhoneViewHolder(@NonNull View itemView) {
            super(itemView);
            book = itemView.findViewById(R.id.book);
            xiugai = itemView.findViewById(R.id.xiugai);
        }
    }

    public interface OnPhoneItemClickListener {
        void onItemClick(int position);
    }

}

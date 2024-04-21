package com.lucas.xxcall.bean;


import com.lucas.xxcall.Utils;
import com.lucas.xxcall.ui.detail.PhoneBean;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class BookBean extends LitePalSupport implements Serializable {




    @Column(unique = true, defaultValue = "unknown")
    public Long bookId;
    public String bookName;
    public List<PhoneBean> phoneBeans;

    public BookBean() {
        // 生成随机的 UUID 并将其作为 id
        this.bookId = Utils.getUUID();
    }

}




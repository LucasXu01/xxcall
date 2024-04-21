package com.lucas.xxcall.ui.detail;

import com.lucas.xxcall.Utils;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class PhoneBean extends LitePalSupport implements Serializable {

    @Column(unique = true, defaultValue = "unknown")
    public Long phoneId;

    public Long bookid;

    public String Phone;
    public String Name;
    public boolean isCalled;

    public PhoneBean (String contents, String contents1) {
        this.Phone = contents;
        this.Name = contents1;
        this.phoneId = Utils.getUUID();
    }
}

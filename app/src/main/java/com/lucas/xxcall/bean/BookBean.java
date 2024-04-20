package com.lucas.xxcall.bean;


import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class BookBean extends LitePalSupport implements Serializable {




    @Column(unique = true, defaultValue = "unknown")
    private String id;

}




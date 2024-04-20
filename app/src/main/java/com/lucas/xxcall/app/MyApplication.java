package com.lucas.xxcall.app;

import android.app.Application;

import org.litepal.LitePal;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        /**   数据库  */
        LitePal.initialize(this);

    }

}

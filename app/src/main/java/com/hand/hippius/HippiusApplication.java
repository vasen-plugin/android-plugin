package com.hand.hippius;

import android.content.Context;
import com.hand.baselibrary.BaseApplication;
import com.hand.baselibrary.config.Constants;
import com.hand.baselibrary.utils.LogUtils;
import androidx.multidex.MultiDex;

public class HippiusApplication extends BaseApplication{
    @Override
    public void onCreate() {
        Constants.BASE_URL = "http://dev-gateway.vasen.com";
        super.onCreate();
        onPostCreated();
        LogUtils.DEBUG = true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}

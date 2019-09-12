package com.xungengbang.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LocationUtils;
import com.blankj.utilcode.util.SPUtils;
import com.xungengbang.AppInit;
import com.xungengbang.R;
import com.xungengbang.Tool.NetInfo;

public class SplashActivity extends BaseActivity {

    SPUtils config = SPUtils.getInstance("config");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_splash);
        if (config.getBoolean("firstStart", true)) {
            config.put("daid", new NetInfo().getMacId());
//            config.put("daid", "00025");
            config.put("ServerId",AppInit.getConfig().getServerId());
            config.put("firstStart",false);
//            ActivityUtils.startActivity(getPackageName(), getPackageName() +".Activity.StartActivity");
        }
        ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage() + ".LoginActivity");

        finish();
    }
}

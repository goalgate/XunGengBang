package com.xungengbang.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.xungengbang.AppInit;
import com.xungengbang.R;
import com.xungengbang.Tool.DESX;


import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends BaseActivity {

    SPUtils config = SPUtils.getInstance("config");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (config.getBoolean("firstStart", true)) {
            config.put("daid", "00025");
            ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage() + ".LoginActivity");
        } else {
            ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage() + ".LoginActivity");
        }
        finish();
    }
}

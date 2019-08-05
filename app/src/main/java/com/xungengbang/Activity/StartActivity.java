package com.xungengbang.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.EditText;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xungengbang.AppInit;
import com.xungengbang.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends Activity {

    SPUtils config = SPUtils.getInstance("config");

    @BindView(R.id.et_daid)
    EditText et_daid;

    @OnClick(R.id.btn_next) void next(){
        if(TextUtils.isEmpty(et_daid.getText().toString())){
            ToastUtils.showLong("设备ID号输入为空");
        }else {
            config.put("daid", et_daid.getText().toString());
            config.put("firstStart",false);
            ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage() + ".LoginActivity");
            this.finish();
        }
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.hideStatusBar(this);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
    }
}

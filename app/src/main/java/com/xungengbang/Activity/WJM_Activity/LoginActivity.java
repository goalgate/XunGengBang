package com.xungengbang.Activity.WJM_Activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xungengbang.Activity.BaseActivity;
import com.xungengbang.AppInit;
import com.xungengbang.Connect.RetrofitGenerator;
import com.xungengbang.R;
import com.xungengbang.Tool.Alarm;
import com.xungengbang.Tool.MD5;
import com.xungengbang.Tool.MyObserver;
import com.xungengbang.Tool.ServerConnectionUtil;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class LoginActivity extends BaseActivity {

    SPUtils config = SPUtils.getInstance("config");

    SPUtils login = SPUtils.getInstance("login");

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };


    @OnClick(R.id.btn_login)
    void login() {
        if (TextUtils.isEmpty(et_username.getText().toString()) || TextUtils.isEmpty(et_password.getText().toString())) {
            ToastUtils.showLong("账号密码信息录入不全");
//            ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage()+AppInit.getConfig().getMainActivity());
        } else {
//            if (SPUtils.getInstance("config").getBoolean("firstStart", true)) {
//                SPUtils.getInstance("config").put("firstStart", false);
//            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", et_username.getText().toString());
                jsonObject.put("password", MD5.parseStrToMd5L32(et_password.getText().toString()).toUpperCase());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            login(jsonObject.toString());
        }
    }

    @BindView(R.id.et_username)
    EditText et_username;

    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.tv_daid)
    TextView tv_daid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppInit.getConfig().stick()) {
            setContentView(R.layout.activity_login);
        } else {
            setContentView(R.layout.activity_login2);
        }
        ButterKnife.bind(this);
        et_username.setOnEditorActionListener((TextView textView, int i, KeyEvent keyEvent)-> keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
        try {
            et_username.setText(login.getString("username"));
            et_password.setText(login.getString("password"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        tv_daid.setText("当前设备ID号为" + config.getString("daid"));

        requestRunPermisssion(permissions, new PermissionListener() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(List<String> deniedPermission) {

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
//        new ServerConnectionUtil().download("http://124.172.232.89:8050/daServer/updateADA.do?ver=" +AppUtils.getAppVersionName() + "&daid=" + config.getString("daid"), new ServerConnectionUtil.Callback() {
//            @Override
//            public void onResponse(String response) {
//                if (response != null) {
//                    if (response.equals("true")) {
//                        if(Build.VERSION.SDK_INT>=24) {//判读版本是否在7.0以上
//                            Uri apkUri = FileProvider.getUriForFile(LoginActivity.this, "com.xungengbang.fileprovider", new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"));
//                            Intent install = new Intent(Intent.ACTION_VIEW);
//                            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
//                            startActivity(install);
//                        }else{
//                            AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"), "application/vnd.android.package-archive");
//                        }
//                    }
//                }
//            }
//        });
    }

    public void login(String jsonData) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
        RetrofitGenerator.getConnectApi().login(config.getString("daid"), body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonData = new JSONObject(responseBody.string());
                            if (jsonData.getInt("code") == 1) {
                                JSONObject data = new JSONObject(jsonData.getString("data"));
                                Bundle bundle = new Bundle();
                                bundle.putString("token", data.getString("token"));
                                bundle.putString("pName", data.getString("compName"));
                                bundle.putString("compId", data.getString("compId"));
                                bundle.putString("compCode", data.getString("compCode"));

                                login.put("username", et_username.getText().toString());
                                login.put("password", et_password.getText().toString());
                                ActivityUtils.startActivity(bundle, getPackageName(), getPackageName() + AppInit.getConfig().getPackage() + AppInit.getConfig().getMainActivity());
                                LoginActivity.this.finish();
                            } else if (jsonData.getInt("code") == 2) {
                                ToastUtils.showLong(jsonData.getString("info"));
                            } else if (jsonData.getInt("code") == 99) {
                                ToastUtils.showLong("系统找不到该账号");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Alarm.getInstance(this).release();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            try {
                ToastUtils.showLong("尚未登录人员信息，无法登记巡更记录");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}

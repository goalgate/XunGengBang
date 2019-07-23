package com.xungengbang.Activity.ZJ_Activity;

import android.Manifest;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.EditText;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.SPUtils;
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

    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @OnClick(R.id.btn_login)
    void login() {
        if (TextUtils.isEmpty(et_username.getText().toString()) || TextUtils.isEmpty(et_password.getText().toString())) {
//            ToastUtils.showLong("账号密码信息录入不全");
            ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage()+".MainActivity");
        } else {
            if (SPUtils.getInstance("config").getBoolean("firstStart", true)) {
                SPUtils.getInstance("config").put("firstStart", false);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", et_username.getText().toString());
                jsonObject.put("password", MD5.parseStrToMd5L32(et_password.getText().toString()));
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
//        et_username.setText("yydw");
//        et_password.setText("88888");
        requestRunPermisssion(permissions, new PermissionListener() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(List<String> deniedPermission) {

            }
        });
    }
    private void autoUpdate() {
        new ServerConnectionUtil().download("http://124.172.232.89:8050/daServer/updateADA.do?ver=" + AppUtils.getAppVersionName() + "&daid=" + config.getString("daid"), new ServerConnectionUtil.Callback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    if (response.equals("true")) {
                        AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"), "application/vnd.android.package-archive");
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoUpdate();
    }

    public void login(String jsonData) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
        RetrofitGenerator.getTestApi().login(config.getString("daid"), body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonData = new JSONObject(responseBody.string());
                            if (jsonData.getString("code") == "1") {
                                JSONObject data = new JSONObject(jsonData.getString("data"));
                                Bundle bundle = new Bundle();
                                bundle.putString("token", data.getString("token"));
                                bundle.putString("userRealName", data.getString("userRealName"));
                                ActivityUtils.startActivity(bundle, getPackageName(), getPackageName() + AppInit.getConfig().getPackage()+".MainActivity");
                            } else if (jsonData.getString("code") == "2") {
                                ToastUtils.showLong(jsonData.getString("info"));
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
    public void onBackPressed() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
//            byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            try {
//                String result = bytesToHexString2(myNFCID);
                ToastUtils.showLong("尚未登录人员信息，无法登记巡更记录");
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}

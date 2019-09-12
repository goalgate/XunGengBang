package com.xungengbang.Activity.ZheJiangActivity;

import android.Manifest;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xungengbang.Activity.BaseActivity;
import com.xungengbang.AppInit;
import com.xungengbang.Connect.RetrofitGenerator;
import com.xungengbang.R;
import com.xungengbang.Tool.Alarm;
import com.xungengbang.Tool.MD5;
import com.xungengbang.Tool.MyObserver;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    SPUtils config = SPUtils.getInstance("config");

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
            //            ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig()
            // .getPackage()+AppInit.getConfig().getMainActivity());
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
            login1(et_username.getText().toString(), et_password.getText().toString());
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
//        et_username.setText("slj");
//        et_password.setText("88888");
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


    public void login(String jsonData) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
        RetrofitGenerator.getZheJiangApi().login(config.getString("daid"), body)
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
                                bundle.putString("userRealName", data.getString("userRealName"));
                                bundle.putString("pID", data.getString("pID"));
                                ActivityUtils.startActivity(bundle, getPackageName(),
                                        getPackageName() + AppInit.getConfig().getPackage() + AppInit.getConfig().getMainActivity());
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


    String mUriLocation;
    String st;

    public void login1(String username, String password) {
        Map<String, Object> paras = new HashMap<>(4);
        paras.put("username", username);
        paras.put("password", password);
        paras.put("authtype", "0");
        paras.put("authcode", "111");
        try {
             RetrofitGenerator.getZheJiangApi().login1(paras).subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Response<ResponseBody>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Response<ResponseBody> value) {
                            mUriLocation = value.headers().get("Location");

                        }

                        @Override
                        public void onError(Throwable e) {
                            paras.clear();
                        }

                        @Override
                        public void onComplete() {
                            Map<String, Object> paras1 = new HashMap<>(1);
                            paras1.put("service", AppInit.getConfig().getChildServerId() + "pac4j-cas?client_name" +
                                    "=CasClient");
                            RetrofitGenerator.getZheJiangApi().login2(mUriLocation, paras1)
                                    .subscribeOn(Schedulers.io())
                                    .unsubscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<Response<ResponseBody>>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onNext(Response<ResponseBody> value) {
                                            try {
                                                st = value.body().string();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            ToastUtils.showLong("无法连接服务器,请检查网络");
                                        }

                                        @Override
                                        public void onComplete() {
                                            paras1.put("ticket", st);
                                            RetrofitGenerator.getZheJiangApi().login3(paras1)
                                                    .subscribeOn(Schedulers.io())
                                                    .unsubscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Observer<Response<ResponseBody>>() {
                                                        @Override
                                                        public void onSubscribe(Disposable d) {

                                                        }

                                                        @Override
                                                        public void onNext(Response<ResponseBody> value) {
                                                            try {
                                                                StringReader sr = new StringReader(value.body().string());
                                                                InputSource is = new InputSource(sr);
                                                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                                                DocumentBuilder builder=factory.newDocumentBuilder();
                                                                Document doc = builder.parse(is);
                                                                Element root = doc.getDocumentElement();
                                                                Element authentication = (Element) root.getElementsByTagName("cas:authentication").item(0);
                                                                Element real_name = (Element) root.getElementsByTagName("cas:real_name").item(0);
                                                                Element user_id = (Element) root.getElementsByTagName("cas:user_id").item(0);
                                                                Element comp_id = (Element) root.getElementsByTagName("cas:comp_id").item(0);
                                                                Log.e("SDsd",authentication.getFirstChild().getNodeValue());
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("token", authentication.getFirstChild().getNodeValue());
                                                                bundle.putString("pID", user_id.getFirstChild().getNodeValue());
                                                                bundle.putString("userRealName", real_name.getFirstChild().getNodeValue());
                                                                bundle.putString("comp_id", comp_id.getFirstChild().getNodeValue());
                                                                ActivityUtils.startActivity(bundle, getPackageName(),
                                                                        getPackageName() + AppInit.getConfig().getPackage() + AppInit.getConfig().getMainActivity());

                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onError(Throwable e) {
                                                            Log.e("SDsd",e.toString());

                                                            ToastUtils.showLong("无法连接服务器,请检查网络");
                                                        }

                                                        @Override
                                                        public void onComplete() {


                                                        }
                                                    });


                                        }
                                    });
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
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

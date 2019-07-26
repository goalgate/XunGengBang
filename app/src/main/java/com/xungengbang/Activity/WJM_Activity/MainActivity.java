package com.xungengbang.Activity.WJM_Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LocationUtils;
import com.blankj.utilcode.util.SPUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.xungengbang.Activity.BaseActivity;
import com.xungengbang.AppInit;
import com.xungengbang.Bean.ReUploadBean;
import com.xungengbang.Camera.mvp.presenter.PhotoPresenter;
import com.xungengbang.Camera.mvp.view.IPhotoView;
import com.xungengbang.Connect.RetrofitGenerator;
import com.xungengbang.Light.presenter.LightPresenter;
import com.xungengbang.R;
import com.xungengbang.Tool.FileUtils;
import com.xungengbang.Tool.MyObserver;
import com.xungengbang.greendao.DaoSession;
import com.xungengbang.greendao.ReUploadBeanDao;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class MainActivity extends BaseActivity implements IPhotoView {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String TAG = MainActivity.class.getSimpleName();

    LightPresenter light = LightPresenter.getInstance();

    PhotoPresenter pp = PhotoPresenter.getInstance();

    SPUtils config = SPUtils.getInstance("config");

    String pName = "支冉";

    String token;

    boolean status = false;

    DaoSession daoSession = AppInit.getInstance().getDaoSession();

    ReUploadBeanDao reUploadBeanDao = daoSession.getReUploadBeanDao();

    Disposable disposableTips;

    Location mlocation;

    @BindView(R.id.tv_user)
    TextView tv_user;

    @BindView(R.id.tv_info)
    TextView tv_info;

    @BindView(R.id.tv_upload)
    TextView tv_upload;

    @BindView(R.id.btn_BigWhite)
    Button btn_BigWhite;

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;

    @OnClick(R.id.btn_BigWhite)
    void btn_BigWhite() {
        status = !status;
        if (status) {
            btn_BigWhite.setBackground(getDrawable(R.drawable.dd_1));
        } else {
            btn_BigWhite.setBackground(getDrawable(R.drawable.dd));
        }
        light.Bigwhite(status);
    }

    @OnClick(R.id.btn_change)
    void change() {
        ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage() + ".LoginActivity");
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        disposableTips = RxTextView.textChanges(tv_info)
                .debounce(10, TimeUnit.SECONDS)
                .switchMap(new Function<CharSequence, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull CharSequence charSequence) throws Exception {
                        return Observable.just("请将巡更棒靠近巡更点处进行巡更操作");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        tv_info.setText(s);
                    }
                });
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        if (list.size() > 0) {
            tv_upload.setVisibility(View.VISIBLE);
        }
        pp.Init(surfaceView, PhotoPresenter.MyOrientation.landscape);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());
        reUpload();
        try {
            token = getIntent().getExtras().getString("token");
            tv_user.setText(pName + ",欢迎您！");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        LocationUtils.register(3000, 100, new LocationUtils.OnLocationChangeListener() {
//            @Override
//            public void getLastKnownLocation(Location location) {
//                mlocation = location;
//                tv_info.setText(String.valueOf(location.getLatitude()));
//
//            }
//
//            @Override
//            public void onLocationChanged(Location location) {
//                mlocation = location;
//                tv_info.setText(String.valueOf(location.getLatitude()));
//
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//        });
    }


    @Override
    public void onGetPhoto(Bitmap bmp) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", "sdasd");
            jsonObject.put("daid", config.getString("daid"));
            jsonObject.put("pID", "441302199308100538");
            jsonObject.put("pName", pName);
            jsonObject.put("infoType", "正常");
            jsonObject.put("x", mlocation != null ? mlocation.getLatitude() : 0.00);
            jsonObject.put("y", mlocation != null ? mlocation.getLongitude() : 0.00);
            jsonObject.put("info", "sadsdsd");
            jsonObject.put("photo", FileUtils.bitmapToBase64(bmp));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        LocationUtils.unregister();

    }

    @Override
    public void onCaremaText(String s) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposableTips.dispose();
        light.Bigwhite(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
//            tv_info.setText(bytesToHexString2(myNFCID));
            try {
                String result = bytesToHexString2(myNFCID);
                //设置结果显示框的显示数值
                Log.e("result", result);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("xgsbBianhao", config.getString("daid"));
                jsonObject.put("xgdBianhao", result.toUpperCase());
                jsonObject.put("xgTime", formatter.format(new Date(System.currentTimeMillis())));
                upData(jsonObject.toString());
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void upData(final String jsonData) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData);
        RetrofitGenerator.getConnectApi().updata(token, body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<ResponseBody>(this) {

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonData = new JSONObject(responseBody.string());
                            if (jsonData.getString("code") == "0") {
                                tv_info.setText(jsonData.getString("info"));
                                light.red(true);
                                Observable.timer(1, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long aLong) throws Exception {
                                                light.red(false);
                                                Observable.timer(1, TimeUnit.SECONDS)
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Consumer<Long>() {
                                                            @Override
                                                            public void accept(Long aLong) throws Exception {
                                                                light.Bigwhite(status);
                                                            }
                                                        });
                                            }
                                        });
                            } else if (jsonData.getString("code") == "1") {
                                light.green(true);
                                Observable.timer(1, TimeUnit.SECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long aLong) throws Exception {
                                                light.green(false);
                                                Observable.timer(1, TimeUnit.SECONDS)
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Consumer<Long>() {
                                                            @Override
                                                            public void accept(Long aLong) throws Exception {
                                                                light.Bigwhite(status);
                                                            }
                                                        });
                                            }
                                        });
                                tv_info.setText("巡更成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        light.red(true);
                        Observable.timer(1, TimeUnit.SECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        light.red(false);
                                        Observable.timer(1, TimeUnit.SECONDS)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Consumer<Long>() {
                                                    @Override
                                                    public void accept(Long aLong) throws Exception {
                                                        light.Bigwhite(status);
                                                    }
                                                });
                                    }
                                });
                        daoSession.insert(new ReUploadBean(null, "upData", jsonData));
                        tv_upload.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
                        if (list.size() > 0) {
                            tv_upload.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }


    private void reUpload() {
        Observable.interval(2, 7200, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
                        for (final ReUploadBean bean : list) {
                            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bean.getContent());
                            RetrofitGenerator.getConnectApi().updata(token, body)
                                    .subscribeOn(Schedulers.single())
                                    .unsubscribeOn(Schedulers.single())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Observer<ResponseBody>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onNext(ResponseBody responseBody) {
                                            reUploadBeanDao.delete(bean);

                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
                                            if (list.size() == 0) {
                                                tv_upload.setVisibility(View.GONE);
                                            }
                                        }
                                    });

                        }
                    }
                });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 27) {
            pp.getOneShut();
        }

        return super.onKeyDown(keyCode, event);
    }
}

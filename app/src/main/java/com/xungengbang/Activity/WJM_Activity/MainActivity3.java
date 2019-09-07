package com.xungengbang.Activity.WJM_Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LocationUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.xungengbang.Activity.BaseActivity;
import com.xungengbang.AppInit;
import com.xungengbang.Bean.ReUploadBean;
import com.xungengbang.Camera.mvp.presenter.PhotoPresenter;
import com.xungengbang.Camera.mvp.view.IPhotoView;
import com.xungengbang.Connect.RetrofitGenerator;
import com.xungengbang.R;
import com.xungengbang.Tool.MyObserver;
import com.xungengbang.greendao.DaoSession;
import com.xungengbang.greendao.ReUploadBeanDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class MainActivity3 extends BaseActivity implements IPhotoView {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    SimpleDateFormat File_formatter = new SimpleDateFormat("yyyyMMddHHmmss");


    private String TAG = MainActivity.class.getSimpleName();

    PhotoPresenter pp = PhotoPresenter.getInstance();

    SPUtils config = SPUtils.getInstance("config");

    String pName;

    String token;

    String compId;

    boolean light_status = false;

    DaoSession daoSession = AppInit.getInstance().getDaoSession();

    ReUploadBeanDao reUploadBeanDao = daoSession.getReUploadBeanDao();

    Disposable disposableTips;

    Location mlocation;

    boolean xungengStatus = false;

    @BindView(R.id.iv_photo)
    ImageView iv_photo;

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

    @BindView(R.id.layour_fun)
    RelativeLayout layout_fun;

    @OnClick(R.id.btn_BigWhite)
    void btn_BigWhite() {
        light_status = !light_status;
        if (light_status) {
            btn_BigWhite.setBackground(getDrawable(R.drawable.dd_1));
        } else {
            btn_BigWhite.setBackground(getDrawable(R.drawable.dd));
        }
        pp.Flash(light_status);
    }

    @OnClick(R.id.btn_change)
    void change() {
        ActivityUtils.startActivity(getPackageName(), getPackageName() + AppInit.getConfig().getPackage() + ".LoginActivity");
        this.finish();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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
                .subscribe((s) -> tv_info.setText(s));
        List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
        if (list.size() > 0) {
            tv_upload.setVisibility(View.VISIBLE);
        }
        pp.Init(surfaceView, PhotoPresenter.MyOrientation.landscape);
        viewInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pp.PhotoPresenterSetView(this);
        pp.setDisplay(surfaceView.getHolder());
        reUpload();
        try {
            if (!LocationUtils.isGpsEnabled()) {
                LocationUtils.openGpsSettings();
            }
            if (LocationUtils.isLocationEnabled()) {
                LocationUtils.register(3000, 1000, new LocationUtils.OnLocationChangeListener() {
                    @Override
                    public void getLastKnownLocation(Location location) {
                        mlocation = location;
                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        mlocation = location;
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                });
            }
            token = getIntent().getExtras().getString("token");
            compId = getIntent().getExtras().getString("compId");
            pName = getIntent().getExtras().getString("pName");
            tv_user.setText(pName + ",欢迎您！");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int bmpMax = 5;
    int bmpcount = 0;
    List<File> FileList = new ArrayList<File>();
    List<String> fjsList = new ArrayList<String>();

    @Override
    public void onGetPhoto(Bitmap bmp) {
        Bitmap senceBitmap = scaleBitmap(rotate(bmp, 90), (float) 0.5);

        String dir_name = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "XunGengPic";
        File dir = new File(dir_name);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String pic_name = dir.getAbsolutePath() + File.separator + File_formatter.format(new Date()) + ".png";
        File pic = new File(pic_name);
        if (!pic.exists()) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(pic);
                pic.createNewFile();
                senceBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                FileList.add(pic);
                bmpcount++;
                if (bmpcount < bmpMax) {
                    ToastUtils.showLong("第" + String.valueOf(bmpcount) + "张照片保存成功,您还可以继续拍摄" + String.valueOf(bmpMax - bmpcount) + "张照片,您也可以点击返回键直接上传信息");
                } else {
                    ToastUtils.showLong("照片收集完毕");
                    layout_fun.setVisibility(View.VISIBLE);
                    updataWithPic();
                    xungengStatus= false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void onCaremaText(String s) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposableTips.dispose();
    }

    String ICCardID = "";

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            ICCardID = bytesToHexString2(myNFCID).toUpperCase();
            if (!xungengStatus) {
                xungengStatus =true;
                bmpcount = 0;
                FileList.clear();
                alertView.show();
            } else {
                ToastUtils.showLong("您上一次的巡更操作尚未完成");
            }

        }
    }

    AlertView alertView;

    private void viewInit() {
        alertView = new AlertView("是否需要拍照上传", null, "不需要", new String[]{"需要"}, null, MainActivity3.this,
                AlertView.Style.Alert, (o, position) -> {
            try {
                if (position == -1) {
                    upData();
                } else if (position == 0) {
                    ToastUtils.showLong("点击屏幕下方蓝色按钮拍摄照片");
                    layout_fun.setVisibility(View.INVISIBLE);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 301) {
            pp.getOneShut();
        } else if (keyCode == 4) {
            if (xungengStatus) {
                updataWithPic();
                layout_fun.setVisibility(View.VISIBLE);
                xungengStatus = false;
            }
        } else if (keyCode == 303 || keyCode == 302) {
            ViewGroup extView2 = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.info_form, null);
            final EditText et_devid = (EditText) extView2.findViewById(R.id.info_input);
            new AlertView("巡更预警上传", null, "取消", new String[]{"确定"}, null, MainActivity3.this, AlertView.Style.Alert, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 0) {
                        if (TextUtils.isEmpty(et_devid.getText().toString())) {
                            alarm("有异常情况");
                        } else {
                            alarm(et_devid.getText().toString());
                        }
                    }
                }
            }).addExtView(extView2).show();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
    }

    private void upData() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("xgsbBianhao", config.getString("daid"));
            jsonObject.put("xgdBianhao", ICCardID.toUpperCase());
            jsonObject.put("xgTime", formatter.format(new Date(System.currentTimeMillis())));
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            RetrofitGenerator.getConnectApi().updata(token, body)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ResponseBody>(this) {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                JSONObject jsonData = new JSONObject(responseBody.string());
                                if (jsonData.getInt("code") == 0) {
                                    tv_info.setText(jsonData.getString("info") + "\n该巡更点号为 " + ICCardID);
                                } else if (jsonData.getInt("code") == 1) {
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
                            daoSession.insert(new ReUploadBean(null, "upData", jsonObject.toString()));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void alarm(String text) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("compId", compId);
            jsonObject.put("bjLeixing", "5");
            jsonObject.put("bjBianhao", config.getString("daid"));
            jsonObject.put("bjTime", formatter.format(new Date(System.currentTimeMillis())));
            jsonObject.put("bjNeirong", text);

        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        RetrofitGenerator.getConnectApi().alarm(token, body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonData = new JSONObject(responseBody.string());
                            if (jsonData.getInt("code") == 0) {
                                tv_info.setText(jsonData.getString("info") + "\n该巡更点号为 " + ICCardID);
                            } else if (jsonData.getInt("code") == 1) {
                                tv_info.setText("巡更成功");
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        daoSession.insert(new ReUploadBean(null, "alarm", jsonObject.toString()));
                        tv_upload.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    private void updataWithPic() {
        fjsList.clear();
        if (FileList.size() == 0) {
            upData();
            return;
        }
        for (File file : FileList) {
            if (!file.exists()) {
                continue;
            }
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

            MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            RetrofitGenerator.getConnectApi().uploadPhotos(token, fileBody)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ResponseBody>(this) {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                JSONObject jsonData = new JSONObject(responseBody.string());
                                if (jsonData.getInt("code") == 1) {
                                    fjsList.add(jsonData.getString("data"));
                                } else {
                                    ToastUtils.showLong(jsonData.getString("info"));
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
                            JSONObject rejson = new JSONObject();
                            try {
                                rejson.put("xgsbBianhao", config.getString("daid"));
                                rejson.put("xgdBianhao", ICCardID.toUpperCase());
                                rejson.put("xgTime", formatter.format(new Date(System.currentTimeMillis())));
                                JSONArray jsonArray = new JSONArray();
                                for (File reFile : FileList) {
                                    JSONObject json = new JSONObject();
                                    json.put("fjName", reFile.getName());
                                    jsonArray.put(json);
                                }
                                rejson.put("fjs", jsonArray);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                return;
                            }
                            daoSession.insert(new ReUploadBean(null, "reUploadBmp", rejson.toString()));
                            tv_upload.setVisibility(View.VISIBLE);
                            return;

                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            if (FileList.size() == fjsList.size()) {
//                                List<SbXungengInfoFj> sblists = new ArrayList<SbXungengInfoFj>();
//                                for (String fjurl : fjsList) {
//                                    SbXungengInfoFj sbXungengInfoFj = new SbXungengInfoFj();
//                                    sbXungengInfoFj.setFjName(file.getName());
//                                    sbXungengInfoFj.setFjDaxiao(file.length());
//                                    sbXungengInfoFj.setFjUrl(fjurl);
//                                    sblists.add(sbXungengInfoFj);
//                                }
//                                SbXungengInfoVo sbXungengInfoVo = new SbXungengInfoVo();
//                                sbXungengInfoVo.setXgdBianhao(ICCardID);
//                                sbXungengInfoVo.setXgsbBianhao(config.getString("daid"));
//                                sbXungengInfoVo.setXgTime(formatter.format(new Date(System.currentTimeMillis())));
//                                sbXungengInfoVo.setFjs(sblists);
//
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("xgsbBianhao", config.getString("daid"));
                                    jsonObject.put("xgdBianhao", ICCardID.toUpperCase());
                                    jsonObject.put("xgTime", formatter.format(new Date(System.currentTimeMillis())));
                                    JSONArray jsonArray = new JSONArray();
                                    for (String fjurl : fjsList) {
                                        JSONObject json = new JSONObject();
                                        json.put("fjName", file.getName());
                                        json.put("fjDaxiao", file.length());
                                        json.put("fjUrl", fjurl);
                                        jsonArray.put(json);
                                    }
                                    jsonObject.put("fjs", jsonArray);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
                                RetrofitGenerator.getConnectApi().updata(token, body)
                                        .subscribeOn(Schedulers.io())
                                        .unsubscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new MyObserver<ResponseBody>(MainActivity3.this) {
                                            @Override
                                            public void onNext(ResponseBody responseBody) {
                                                try {
                                                    JSONObject jsonData = new JSONObject(responseBody.string());
                                                    if (jsonData.getInt("code") == 0) {
                                                        tv_info.setText(jsonData.getString("info") + "\n该巡更点号为 " + ICCardID);
                                                    } else if (jsonData.getInt("code") == 1) {
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
                                                daoSession.insert(new ReUploadBean(null, "upDataFiles", jsonObject.toString()));
                                                tv_upload.setVisibility(View.VISIBLE);

                                            }

                                            @Override
                                            public void onComplete() {
                                                super.onComplete();
                                                for (File file1 : FileList) {
                                                    if (file1.exists()) {
                                                        file1.delete();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }

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
                            if (bean.getMethod().equals("alarm")) {
                                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),  bean.getContent());
                                RetrofitGenerator.getConnectApi().alarm(token, body)
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
                            } else if (bean.getMethod().equals("upData")) {
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
                            } else if (bean.getMethod().equals("upDataFiles")) {
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
                                                String dir_name = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "XunGengPic";
                                                try {
                                                    List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
                                                    if (list.size() == 0) {
                                                        tv_upload.setVisibility(View.GONE);
                                                    }
                                                    JSONObject jsonData = new JSONObject(bean.getContent());
                                                    JSONArray jsonArray = jsonData.getJSONArray("fjs");
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        JSONObject subJSON = jsonArray.getJSONObject(i);
                                                        File file = new File(dir_name + File.separator + subJSON.getString("fjName"));
                                                        if (file.exists()) {
                                                            file.delete();
                                                        }
                                                    }

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        });
                            } else if (bean.getMethod().equals("reUploadBmp")) {
                                List<String> re_fjsList = new ArrayList<String>();
                                String dir_name = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "XunGengPic";
                                try {
                                    JSONObject reUploadBmpJSON = new JSONObject(bean.getContent());
                                    JSONArray jsonArray = reUploadBmpJSON.getJSONArray("fjs");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject subJSON = jsonArray.getJSONObject(i);
                                        File file = new File(dir_name + File.separator + subJSON.getString("fjName"));
                                        if (file.exists()) {
                                            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                            MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                                            RetrofitGenerator.getConnectApi().uploadPhotos(token, fileBody)
                                                    .subscribeOn(Schedulers.io())
                                                    .unsubscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Observer<ResponseBody>() {
                                                        @Override
                                                        public void onSubscribe(Disposable d) {

                                                        }

                                                        @Override
                                                        public void onNext(ResponseBody responseBody) {
                                                            try {
                                                                JSONObject jsonData = new JSONObject(responseBody.string());
                                                                if (jsonData.getInt("code") == 1) {
                                                                    re_fjsList.add(jsonData.getString("data"));
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        @Override
                                                        public void onError(Throwable e) {

                                                        }

                                                        @Override
                                                        public void onComplete() {
                                                            if (jsonArray.length() == re_fjsList.size()) {
                                                                JSONObject jsonObject = new JSONObject();
                                                                try {
                                                                    jsonObject.put("xgsbBianhao", reUploadBmpJSON.getString("xgsbBianhao"));
                                                                    jsonObject.put("xgdBianhao", reUploadBmpJSON.getString("xgdBianhao"));
                                                                    jsonObject.put("xgTime", reUploadBmpJSON.getString("xgTime"));
                                                                    JSONArray jsonArray = new JSONArray();
                                                                    for (String fjurl : re_fjsList) {
                                                                        JSONObject json = new JSONObject();
                                                                        json.put("fjName", file.getName());
                                                                        json.put("fjDaxiao", file.length());
                                                                        json.put("fjUrl", fjurl);
                                                                        jsonArray.put(json);
                                                                    }
                                                                    jsonObject.put("fjs", jsonArray);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
                                                                RetrofitGenerator.getConnectApi().updata(token, body)
                                                                        .subscribeOn(Schedulers.io())
                                                                        .unsubscribeOn(Schedulers.io())
                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                        .subscribe(new Observer<ResponseBody>() {
                                                                            @Override
                                                                            public void onSubscribe(Disposable d) {

                                                                            }

                                                                            @Override
                                                                            public void onNext(ResponseBody responseBody) {
                                                                                daoSession.delete(bean);
                                                                            }

                                                                            @Override
                                                                            public void onError(Throwable e) {
                                                                                daoSession.insert(new ReUploadBean(null, "upDataFiles", jsonObject.toString()));
                                                                            }

                                                                            @Override
                                                                            public void onComplete() {

                                                                            }
                                                                        });
                                                            }

                                                        }
                                                    });
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ToastUtils.showLong(e.toString());
                                }
                            }
                        }
                    }
                });
    }

    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();  //Android开发网再次提示Bitmap操作完应该显示的释放
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // Android123建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.
            }
        }
        return b;
    }

    private static Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

}

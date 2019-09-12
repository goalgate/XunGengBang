package com.xungengbang.Activity.WJM_Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Gravity;
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
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LocationUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.xungengbang.Activity.BaseActivity;
import com.xungengbang.AppInit;
import com.xungengbang.Bean.ReUploadBean;
import com.xungengbang.Bean.ReUploadComplexBean;
import com.xungengbang.Camera.mvp.presenter.PhotoPresenter;
import com.xungengbang.Camera.mvp.view.IPhotoView;
import com.xungengbang.Connect.RetrofitGenerator;
import com.xungengbang.Download.Download;
import com.xungengbang.Download.DownloadProgressListener;
import com.xungengbang.R;
import com.xungengbang.Tool.FileUtils;
import com.xungengbang.Tool.MyObserver;
import com.xungengbang.Tool.ServerConnectionUtil;
import com.xungengbang.greendao.DaoSession;
import com.xungengbang.greendao.ReUploadBeanDao;
import com.xungengbang.greendao.ReUploadComplexBeanDao;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    String Picdir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "XunGengPic";
    String Apkdir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Apk";


    PhotoPresenter pp = PhotoPresenter.getInstance();

    SPUtils config = SPUtils.getInstance("config");

    String pName;

    String token;

    String compId;

    String compCode;

    boolean light_status = false;

    DaoSession daoSession = AppInit.getInstance().getDaoSession();

    ReUploadBeanDao reUploadBeanDao = daoSession.getReUploadBeanDao();

    ReUploadComplexBeanDao reUploadComplexBeanDao = daoSession.getReUploadComplexBeanDao();

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
        AutoUpdate();
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
            compCode = getIntent().getExtras().getString("compCode");
            tv_user.setText(pName + ",欢迎您！");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int bmpMax = 5;
    int bmpcount = 0;
    List<File> FileList = new ArrayList<File>();
    List<String> fjsList = new ArrayList<String>();
    List<String> reBmpList = new ArrayList<String>();

    @Override
    public void onGetPhoto(Bitmap bmp) {
        Bitmap senceBitmap = rotate(bmp, 90);
        File dir = new File(Picdir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String pic_name = Picdir + File.separator + File_formatter.format(new Date()) + ".png";
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
                    xungengStatus = false;
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
                bmpcount = 0;
                FileList.clear();
                XunGengAlertView.show();
            } else {
                ToastUtils.showLong("您上一次的巡更操作尚未完成");
            }

        }
    }

    AlertView XunGengAlertView;

    AlertView AlarmAlertView;

    private void viewInit() {
        XunGengAlertView = new AlertView("是否需要拍照上传巡更信息", null, "不需要", new String[]{"需要"}, null, MainActivity3.this,
                AlertView.Style.Alert, (o, position) -> {
            try {
                if (position == -1) {
                    upData();
                } else if (position == 0) {
                    ToastUtils.showLong("点击屏幕下方蓝色按钮拍摄照片");
                    layout_fun.setVisibility(View.INVISIBLE);
                    xungengStatus = true;

                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ViewGroup extView2 = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.info_form, null);
        EditText et_devid = (EditText) extView2.findViewById(R.id.info_input);
        AlarmAlertView = new AlertView("巡更预警上传", null, "取消", new String[]{"确定"}, null, MainActivity3.this, AlertView.Style.Alert, new OnItemClickListener() {
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
        }).addExtView(extView2);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 301) {
            if (xungengStatus) {
                pp.getOneShut();
            }
        } else if (keyCode == 4) {
            if (xungengStatus) {
                layout_fun.setVisibility(View.VISIBLE);
                updataWithPic();
                xungengStatus = false;
            }
        } else if (keyCode == 303 || keyCode == 302) {
            if (!AlarmAlertView.isShowing()) {
                AlarmAlertView.show();
            }
        } else if (keyCode == 24) {

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
                .subscribe(new MyObserver<ResponseBody>(this) {

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            JSONObject jsonData = new JSONObject(responseBody.string());
                            if (jsonData.getInt("code") == 0) {
                                tv_info.setText(jsonData.getString("info") + "\n该巡更点号为 " + ICCardID);
                            } else if (jsonData.getInt("code") == 1) {
                                tv_info.setText("预警信息上传成功");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        daoSession.insert(new ReUploadBean(null, "alarm", jsonObject.toString()));
                        tv_upload.setVisibility(View.VISIBLE);

                    }


                });

    }


    private void updataWithPic() {
        fjsList.clear();
        reBmpList.clear();
        if (FileList.size() == 0) {
            upData();
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("xgsbBianhao", config.getString("daid"));
            jsonObject.put("xgdBianhao", ICCardID.toUpperCase());
            jsonObject.put("xgTime", formatter.format(new Date(System.currentTimeMillis())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (File file : FileList) {
            if (!file.exists()) {
                continue;
            }
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

            MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            RetrofitGenerator.getConnectApi().uploadPhotos(token, fileBody)
                    .subscribeOn(Schedulers.single())
                    .unsubscribeOn(Schedulers.single())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<ResponseBody>(this) {
                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                JSONObject jsonData = new JSONObject(responseBody.string());
                                if (jsonData.getInt("code") == 1) {
                                    fjsList.add(jsonData.getString("data"));
                                    file.delete();
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
                            tv_upload.setVisibility(View.VISIBLE);
                            reBmpList.add(file.getName());
                            if (reBmpList.size() + fjsList.size() == FileList.size()) {
                                daoSession.insert(new ReUploadComplexBean(null, "reBmpFile", jsonObject.toString(), FileList.size(), fjsList, reBmpList));
                            }
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            if (FileList.size() == fjsList.size()) {
                                try {
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
                                        .subscribeOn(Schedulers.single())
                                        .unsubscribeOn(Schedulers.single())
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
                            } else if (reBmpList.size() > 0 && reBmpList.size() + fjsList.size() == FileList.size()) {
                                daoSession.insert(new ReUploadComplexBean(null, "reBmpFile", jsonObject.toString(), FileList.size(), fjsList, reBmpList));
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
                                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bean.getContent());
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
                                                List<ReUploadBean> list = reUploadBeanDao.queryBuilder().list();
                                                if (list.size() == 0) {
                                                    tv_upload.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                            }
                        }
                        List<ReUploadComplexBean> reUploadComplexBeanList = reUploadComplexBeanDao.queryBuilder().list();
                        for (ReUploadComplexBean bean : reUploadComplexBeanList) {
                            if (bean.getMethod().equals("reBmpFile")) {
                                List<String> refjsList = new ArrayList<>();
                                for (String fjsname : bean.getFjUrls()) {
                                    refjsList.add(fjsname);
                                }
                                for (String filename : bean.getBmpFileNames()) {
                                    File file = new File(Picdir + File.separator + filename);
                                    if (!file.exists()) {
                                        continue;
                                    }
                                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                                    MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                                    RetrofitGenerator.getConnectApi().uploadPhotos(token, fileBody)
                                            .subscribeOn(Schedulers.single())
                                            .unsubscribeOn(Schedulers.single())
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
                                                            refjsList.add(jsonData.getString("data"));
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
                                                    if (bean.getBmpcount() == refjsList.size()) {
                                                        try {
                                                            JSONObject json = new JSONObject(bean.getContent());
                                                            JSONArray jsonArray = new JSONArray();
                                                            for (String fjurl : refjsList) {
                                                                JSONObject subjson = new JSONObject();
                                                                subjson.put("fjName", file.getName());
                                                                subjson.put("fjDaxiao", file.length());
                                                                subjson.put("fjUrl", fjurl);
                                                                jsonArray.put(subjson);
                                                            }
                                                            json.put("fjs", jsonArray);
                                                            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
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
                                                                            reUploadComplexBeanDao.delete(bean);
                                                                        }

                                                                        @Override
                                                                        public void onError(Throwable e) {

                                                                        }

                                                                        @Override
                                                                        public void onComplete() {
                                                                            List<ReUploadComplexBean> list = reUploadComplexBeanDao.queryBuilder().list();
                                                                            if (list.size() == 0) {
                                                                                tv_upload.setVisibility(View.GONE);
                                                                            }
                                                                            for (String filename : bean.getBmpFileNames()) {
                                                                                File file = new File(Picdir + File.separator + filename);
                                                                                if (file.exists()) {
                                                                                    file.delete();
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    private void AutoUpdate() {
        new ServerConnectionUtil().download("http://sbgl.wxhxp.cn:8050/daServer/updateXG.do?ver=" + AppUtils.getAppVersionName() + "&daid=" + config.getString("daid") + "&url=" + config.getString("ServerId"), new ServerConnectionUtil.Callback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    if (response.equals("true")) {
                        if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
                            Uri apkUri = FileProvider.getUriForFile(MainActivity3.this, "com.xungengbang.fileprovider", new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"));
                            Intent install = new Intent(Intent.ACTION_VIEW);
                            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            startActivity(install);
                        } else {
                            AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + "app-release.apk"), "application/vnd.android.package-archive");
                        }
                    }
                }
            }
        });
//        RetrofitGenerator.getConnectApi().update(token, String.valueOf(3))
//                .subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<ResponseBody>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        try {
//                            JSONObject data = new JSONObject(responseBody.string());
//                            if(data.getInt("code")==1){
//                                JSONObject jsonData = new JSONObject(data.getString("data"));
//                                if (compCode.startsWith(jsonData.getString("fabuArea"))) {
//                                    if (Float.valueOf(AppUtils.getAppVersionName()) < Float.valueOf(jsonData.getString("avCode"))) {
//
//
//                                    } else {
//                                        new AlertView(String.format("最新版本ver%s,是否需要更新",jsonData.getString("avCode")), null, "不需要", new String[]{"需要"}, null, MainActivity3.this,
//                                                AlertView.Style.Alert, (o, position) -> {
//                                            if (position == 0){
//
//                                                ProgressDialog progressDialog = new ProgressDialog(MainActivity3.this);
//                                                progressDialog.setCanceledOnTouchOutside(false);
//                                                progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
//                                                progressDialog.setMessage("数据上传中,请稍候");
//
//                                                DownloadProgressListener listener = (bytesRead,contentLength,done)->{
//                                                    Download download = new Download();
//                                                    download.setTotalFileSize(contentLength);
//                                                    download.setCurrentFileSize(bytesRead);
//                                                    int progress = (int) ((bytesRead * 100) / contentLength);
//                                                    download.setProgress(progress);
//                                                    progressDialog.setMessage("数据上传中,请稍候... "+progress+"%");
//                                                };
//
//
//                                                new RetrofitGenerator().getGlobalApi(listener).DownLoad("http://192.168.11.134:8102/static/down/巡更ver1.4.apk")
//                                                        .subscribeOn(Schedulers.io())
//                                                        .unsubscribeOn(Schedulers.io())
//                                                        .flatMap(new Function<ResponseBody, ObservableSource<InputStream>>() {
//                                                            @Override
//                                                            public ObservableSource<InputStream> apply(ResponseBody responseBody) throws Exception {
//                                                                return Observable.just(responseBody.byteStream());
//                                                            }
//                                                        }).observeOn(Schedulers.computation())
//                                                        .doOnNext(new Consumer<InputStream>() {
//                                                            @Override
//                                                            public void accept(InputStream inputStream) throws Exception {
//                                                                File apkdir = new File(Apkdir);
//                                                                if(!apkdir.exists()){
//                                                                    apkdir.mkdir();
//                                                                }
//                                                                int len = -1;
//                                                                byte[] bs = new byte[2048];
//                                                                File file = new File(Apkdir+File.separator+"release.apk");
//                                                                FileOutputStream os = new FileOutputStream(file);
//                                                                while ((len =inputStream.read(bs)) != -1) {
//                                                                    os.write(bs, 0, len);
//                                                                }
//                                                                os.flush();
//                                                                os.close();
//                                                            }
//                                                        }).observeOn(AndroidSchedulers.mainThread())
//                                                        .subscribe(new Observer<InputStream>() {
//                                                            @Override
//                                                            public void onSubscribe(Disposable d) {
//                                                                progressDialog.show();
//                                                            }
//
//                                                            @Override
//                                                            public void onNext(InputStream inputStream) {
//
//                                                            }
//
//                                                            @Override
//                                                            public void onError(Throwable e) {
//                                                                progressDialog.dismiss();
//                                                            }
//
//                                                            @Override
//                                                            public void onComplete() {
//                                                                progressDialog.dismiss();
//                                                            }
//                                                        });
//
//                                            }
//                                        }).show();
//                                    }
//                                }
//                            }
//
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
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


}

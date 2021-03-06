package com.xungengbang.Activity.WJM_Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.blankj.utilcode.util.ActivityUtils;
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

    Bitmap SenceBitmap;

    boolean situation;

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
        infoViewInit();
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
    }


    @Override
    public void onGetPhoto(Bitmap bmp) {
        if (!situation) {
            SenceBitmap = rotate(bmp, 90);
            iv_photo.setImageBitmap(SenceBitmap);
            iv_photo.setVisibility(View.VISIBLE);
            photoUpload();
            Observable.timer(2, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            iv_photo.setVisibility(View.GONE);
                        }
                    });
        } else {
            SenceBitmap = scaleBitmap(rotate(bmp, 90), (float) 0.05);
            iv_pic.setImageBitmap(SenceBitmap);
        }
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
                upData(jsonObject.toString(),result.toUpperCase());
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public void upData(final String jsonData,String name_IC) {
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
                                tv_info.setText(jsonData.getString("info")+"\n该巡更点号为 "+name_IC);
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

    private void photoUpload() {
        final JSONObject jsonObject = new JSONObject();
        try {
            if (!situation) {
                jsonObject.put("id", "sdasd");
                jsonObject.put("daid", config.getString("daid"));
                jsonObject.put("pID", "441302199308100538");
                jsonObject.put("pName", pName);
                jsonObject.put("infoType", "一键上报");
                jsonObject.put("x", mlocation != null ? mlocation.getLatitude() : 0.00);
                jsonObject.put("y", mlocation != null ? mlocation.getLongitude() : 0.00);
                jsonObject.put("info", "");
                jsonObject.put("photo", FileUtils.bitmapToBase64(SenceBitmap));
            } else {
                jsonObject.put("id", "sdasd"); //单位ID
                jsonObject.put("daid", config.getString("daid")); //设备ID
                jsonObject.put("pID", "441302199308100538");//人员身份证
                jsonObject.put("pName", pName);  //人员姓名
                jsonObject.put("infoType", condition);  //信息类型 正常/异常
                jsonObject.put("x", mlocation != null ? mlocation.getLatitude() : 0.00); //经度
                jsonObject.put("y", mlocation != null ? mlocation.getLongitude() : 0.00); //纬度
                jsonObject.put("info", et_info.getText().toString());  //内容
                jsonObject.put("photo", FileUtils.bitmapToBase64(SenceBitmap)); //现场照片
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        new RetrofitGenerator().getConnectApi("http://124.172.232.89:8050/daServer/")
                .photoUpload("info", jsonObject.toString())
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
                            if (responseBody.string().equals("true")) {
                                //ToastUtils.showLong(responseBody.string());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        daoSession.insert(new ReUploadBean(null, "photoUpload", jsonObject.toString()));
                        tv_upload.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == 27) {
//            pp.getOneShut();
//        }
        situation = false;
        if (keyCode == 19) {
            pp.getOneShut();
        } else if (keyCode == 27) {
            situation = true;
            pp.getOneShut();
            adapter.notifyDataSetChanged();
            et_info.setText(null);
            info_alert.show();
        } else if(keyCode ==20){
            ToastUtils.showLong(config.getString("daid"));
        }
//        ToastUtils.showLong(String.valueOf(keyCode));
        return super.onKeyDown(keyCode, event);
    }


    List<String> infos = new ArrayList<String>();

    public List<String> getData() {
        try {
            String[] contents = SPUtils.getInstance("info").getString("content").split(",");
            infos.add(contents[contents.length - 1]);
            infos.add(contents[contents.length - 2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infos;
    }

    PopupWindow pop;

    ViewGroup extView2;

    EditText et_info;

    ImageView iv_pic;

    Spinner sp_condition;

    AlertView info_alert;

    ImageView iv_drop_down;

    DropdownAdapter adapter;

    ListView listView;

    String condition;

    private void infoViewInit() {
        listView = new ListView(this);
        adapter = new MainActivity.DropdownAdapter(this, getData());
        listView.setAdapter(adapter);
        extView2 = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.input_form_240x320, null);
        et_info = (EditText) extView2.findViewById(R.id.et_info);
        iv_drop_down = (ImageView) extView2.findViewById(R.id.iv_drop_down);
        iv_drop_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pop == null) {
                    pop = new PopupWindow(listView, et_info.getWidth(), 2 * et_info.getHeight());
                    pop.showAsDropDown(et_info);
                } else {
                    if (pop.isShowing()) {
                        pop.dismiss();
                    } else {
                        pop.showAsDropDown(et_info);
                    }
                }
            }
        });
        iv_pic = (ImageView) extView2.findViewById(R.id.iv_pic);
        sp_condition = (Spinner) extView2.findViewById(R.id.sp_condition);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_condition_240x320, new String[]{"正常", "异常"});
        sp_condition.setAdapter(arrayAdapter);
        sp_condition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                String[] conditions = getResources().getStringArray(R.array.condition);
                condition = conditions[pos];

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        info_alert = new AlertView("拍照上传", null, "取消", new String[]{"上传"}, null, MainActivity.this,
                AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                if (pop != null) {
                    pop.dismiss();
                }
                if (position == 0) {
                    if (TextUtils.isEmpty(et_info.getText().toString())) {
                        ToastUtils.showLong("您的输入为空请重试");
                    } else {
                        try {
                            infos.add(0,et_info.getText().toString());
                            if (infos.size() >= 2) {
                                infos.remove(infos.size()-1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        SPUtils.getInstance("info").put("content", SPUtils.getInstance("info").getString("content") + et_info.getText().toString() + ",");
                        photoUpload();
                    }
                }
            }
        }).addExtView(extView2);

    }


    class DropdownAdapter extends BaseAdapter {

        public DropdownAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_row_240x320, null);
            content = (TextView) convertView.findViewById(R.id.text_row);
            final String editContent = list.get(position);
            content.setText(list.get(position).toString());
            content.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    et_info.setText(editContent);
                    pop.dismiss();
                    return false;
                }
            });
            return convertView;
        }

        private Context context;
        private LayoutInflater layoutInflater;
        private List<String> list;
        private TextView content;
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

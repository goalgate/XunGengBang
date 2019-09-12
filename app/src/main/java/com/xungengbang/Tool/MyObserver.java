package com.xungengbang.Tool;


import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;

import com.blankj.utilcode.util.ToastUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MyObserver<T> implements Observer<T>{
    private Context context;

    private ProgressDialog progressDialog;

    public MyObserver(Context context) {
        this.context = context;
        this.progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        progressDialog.setMessage("数据上传中,请稍候");
    }



    @Override
    public void onSubscribe(Disposable d) {
        progressDialog.show();
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable e) {
        progressDialog.dismiss();
        ToastUtils.showLong("无法连接服务器,请检查网络");
//        Alarm.getInstance(context).messageAlarm("无法连接服务器,请检查网络,巡更数据已保存");


    }

    @Override
    public void onComplete() {
        progressDialog.dismiss();
    }
}

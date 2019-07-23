package com.xungengbang.Camera.mvp.presenter;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.xungengbang.Camera.mvp.module.IPhotoModule;
import com.xungengbang.Camera.mvp.module.PhotoModuleImpl;
import com.xungengbang.Camera.mvp.view.IPhotoView;


/**
 * Created by zbsz on 2017/6/9.
 */

public class PhotoPresenter {

    private IPhotoView view;

    private static PhotoPresenter instance=null;
    private PhotoPresenter(){}
    public static PhotoPresenter getInstance() {
        if(instance==null)
            instance=new PhotoPresenter();
        return instance;
    }

    public enum MyOrientation {
        landscape, vertical
    }

    public void PhotoPresenterSetView(IPhotoView view) {
        this.view = view;
    }

    IPhotoModule photoModule = new PhotoModuleImpl();


    public void Init(SurfaceView surfaceView, MyOrientation orientation){
        try {
            photoModule.Init(surfaceView, new IPhotoModule.IOnSetListener() {
                @Override
                public void onBtnText(String msg) {
                    view.onCaremaText(msg);
                }

                @Override
                public void onGetPhoto(Bitmap bmp) {
                    view.onGetPhoto(bmp);
                }
            },orientation);
        }catch (NullPointerException e){
            Log.e("setParameter",e.toString());
        }
    }


    public void setDisplay(SurfaceHolder surfaceHolder){
        try {
            photoModule.setDisplay(surfaceHolder);
        }catch (NullPointerException e){
            Log.e("setDisplay",e.toString());
        }
    }

    public void capture(){
        try {
            photoModule.capture();
        }catch (NullPointerException e){
            Log.e("capture",e.toString());
        }

    }


    public void onActivityDestroy(){
        try {
            photoModule.onActivityDestroy();
        }catch (NullPointerException e){
            Log.e("onActivityDestroy",e.toString());
        }
    }
    public void getOneShut(){
        try {
            photoModule.getOneShut();
        }catch (NullPointerException e){
            Log.e("getOneShut",e.toString());
        }
    }

}
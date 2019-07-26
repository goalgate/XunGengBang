package com.xungengbang.Camera.mvp.module;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.blankj.utilcode.util.ScreenUtils;
import com.xungengbang.Camera.mvp.presenter.PhotoPresenter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zbsz on 2017/5/19.
 */

public class PhotoModuleImpl implements IPhotoModule, Camera.PreviewCallback {
    final static String ApplicationName = PhotoModuleImpl.class.getSimpleName();

    Camera camera;

    SurfaceView mSurfaceView;

    IOnSetListener callback;

    byte[] global_bytes;

    int width;

    int height;

    PhotoPresenter.MyOrientation myOrientation = PhotoPresenter.MyOrientation.landscape;

    private static final String TAG = PhotoModuleImpl.class.getSimpleName();

    @Override
    public void setDisplay(final SurfaceHolder sHolder) {
        try {
            if (camera != null) {
                camera.setPreviewDisplay(sHolder);
                camera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void Init(SurfaceView surfaceView, IOnSetListener listener, PhotoPresenter.MyOrientation orientation) {
        this.callback = listener;
        this.mSurfaceView = surfaceView;
        this.myOrientation = orientation;
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                safeCameraOpen(0);
                setCameraParemeter();
                setDisplay(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCamera();

            }
        });
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }

    }

    private void setCameraParemeter() {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = camera.getParameters().getPreviewSize(); //获取预览大小
        Log.e("width", String.valueOf(width = size.width));
        Log.e("height", String.valueOf(height = size.height));
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.set("jpeg-quality", 100);
        camera.setParameters(parameters);
        switch (myOrientation) {
            case vertical:
                break;
            case landscape:
                camera.setDisplayOrientation(90);
                break;
            default:
                break;
        }
//        camera.setPreviewCallbackWithBuffer(PhotoModuleImpl.this);
//        camera.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
//
        camera.setPreviewCallback(PhotoModuleImpl.this);
    }

//    @Override
//    public void setParameter(SurfaceView surfaceView, TextureView textureView, IOnSetListener listener, final PhotoPresenter.MyOrientation orientation) {
//        this.callback = listener;
//        this.mTextureView = textureView;
//        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format,
//                                       int width, int height) {
//
//            }
//
//            @Override
//            public void surfaceCreated(SurfaceHolder holder) {
//                safeCameraOpen(0);
//                tools.start();
//                Camera.Size size = camera.getParameters().getPreviewSize(); //获取预览大小
//                Log.e("width", String.valueOf(width = size.width));
//                Log.e("height", String.valueOf(height = size.height));
//                myOrientation = orientation;
//                switch (myOrientation) {
//                    case vertical:
//                        break;
//                    case landscape:
//                        camera.setDisplayOrientation(90);
//                        break;
//                    default:
//                        break;
//                }
//                Camera.Parameters parameters = camera.getParameters();
//                // 设置预览照片时每秒显示多少帧的最小值和最大值
//                // 设置图片格式
//                parameters.setPictureFormat(ImageFormat.JPEG);
//                // 设置JPG照片的质量
//                parameters.set("jpeg-quality", 100);
//                camera.setPreviewCallbackWithBuffer(PhotoModuleImpl3.this);
//                camera.addCallbackBuffer(new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
//                camera.setPreviewCallback(PhotoModuleImpl3.this);
//                camera.setParameters(parameters);
//                setDisplay(holder);
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder) {
//                // 如果camera不为null ,释放摄像头
//                if (camera != null) {
//                    holder.removeCallback(this);
//                    camera.setPreviewCallback(null);
//                    camera.stopPreview();
//                    camera.release();
//                    camera = null;
//                    Log.e(ApplicationName, "摄像头被释放");
//                    mTextureView = null;
//                }
//            }
//
//        });
//    }


    @Override
    public void Flash(boolean status) {
        Camera.Parameters parameters = camera.getParameters();
        if(status){
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
            camera.setParameters(parameters);
        }else{
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);//关闭
            camera.setParameters(parameters);
        }
    }

    @Override
    public void onActivityDestroy() {
        mSurfaceView = null;
    }

    @Override
    public void capture() {
        camera.takePicture(new Camera.ShutterCallback() {
            public void onShutter() {
                // 按下快门瞬间会执行此处代码
            }
        }, new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera c) {
                // 此处代码可以决定是否需要保存原始照片信息
            }
        }, myJpegCallback);
    }

    Camera.PictureCallback myJpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            camera.stopPreview();
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            callback.onBtnText("拍照成功");
            callback.onGetPhoto(bmp);
        }
    };

    private void safeCameraOpen(int id) {
        try {
            releaseCameraAndPreview();
            camera = Camera.open(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void releaseCameraAndPreview() {
        if (camera != null) {
            camera.release();
        }
    }

    @Override
    public void getOneShut() {
        Observable.just(global_bytes)
                .subscribeOn(Schedulers.computation())
                .unsubscribeOn(Schedulers.computation())
                .flatMap(new Function<byte[], ObservableSource<Bitmap>>() {
                    @Override
                    public ObservableSource<Bitmap> apply(byte[] bytes) throws Exception {
                        YuvImage image = new YuvImage(global_bytes, ImageFormat.NV21, width, height, null);
                        ByteArrayOutputStream os = new ByteArrayOutputStream(global_bytes.length);
                        if (!image.compressToJpeg(new Rect(0, 0, width, height), 100, os)) {
                            return null;
                        }
                        byte[] tmp = os.toByteArray();
                        Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
                        return Observable.just(bmp);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        callback.onGetPhoto(bitmap);
                    }
                });
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        global_bytes = data;

    }
}







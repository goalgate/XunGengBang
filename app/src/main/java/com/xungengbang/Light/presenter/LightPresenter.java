package com.xungengbang.Light.presenter;


import com.xungengbang.Light.module.ILight;
import com.xungengbang.Light.module.ILightImpl;

public class LightPresenter {
    private static LightPresenter instance = null;

    private LightPresenter() {

    }

    public static LightPresenter getInstance() {
        if (instance == null)
            instance = new LightPresenter();
        return instance;
    }

    ILight light = new ILightImpl();

    public void blue(boolean status){
        light.blue(status);
    }

    public void green(boolean status){
        light.green(status);
    }

    public void grblue(boolean status){
        light.grblue(status);
    }

    public void red(boolean status){
        light.red(status);
    }

    public void yellow(boolean status){
        light.yellow(status);
    }

    public void white(boolean status){
        light.white(status);
    }
    public void Bigwhite(boolean status){
        light.Bigwhite(status);
    }
}

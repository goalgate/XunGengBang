package com.xungengbang.Config;

public class ZheJiang1_Config extends BaseConfig {
    @Override
    public String getServerId() {
        return "http://14.23.69.2:1140/";
    }


//    @Override
//    public String getServerId() {
//        return "http://192.168.11.21:8140";
//    }

    @Override
    public String getChildServerId() {
        return "http://14.23.69.2:1151/";
    }


//    @Override
//    public String getChildServerId() {
//        return "http://192.168.11.212:8102";
//    }

    @Override
    public String getPackage() {
        return ".Activity.ZheJiangActivity";
    }

    @Override
    public boolean stick() {
        return false;
    }

    @Override
    public String getMainActivity() {
        if (stick()) {
            return ".MainActivity";
        } else {
            return ".MainActivity2";
        }
    }
}

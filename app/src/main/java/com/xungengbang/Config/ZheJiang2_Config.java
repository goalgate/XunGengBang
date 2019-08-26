package com.xungengbang.Config;

public class ZheJiang2_Config extends BaseConfig {
    @Override
    public String getServerId() {
        return "http://14.23.69.2:1140/";
    }

    @Override
    public String getPackage() {
        return ".Activity.ZheJiangActivity";
    }

    @Override
    public String getChildServerId() {
        return "http://14.23.69.2:1161/";
    }

    @Override
    public boolean stick() {
        return false;
    }

    @Override
    public String getMainActivity() {
        if (stick()){
            return ".MainActivity";
        }else {
            return ".MainActivity2";
        }
    }
}

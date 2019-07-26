package com.xungengbang.Config;

public class WJM_Config extends BaseConfig {
    @Override
    public String getServerId() {
        return "http://192.168.11.136:8102/";
    }

    @Override
    public String getPackage() {
        return ".Activity.WJM_Activity";
    }

    @Override
    public boolean stick() {
        return false;
    }

    @Override
    public String getMainActivity() {
        if (stick()){
            return ".MainActivity2";
        }else {
            return ".MainActivity";
        }
    }
}

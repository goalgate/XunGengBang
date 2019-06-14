package com.xungengbang.Config;

public class ZJ_Config extends BaseConfig{
    @Override
    public String getServerId() {
        return "http://192.168.11.131:9001/";
    }

    @Override
    public String getPackage() {
        return ".Activity.ZJ_Activity";
    }
}

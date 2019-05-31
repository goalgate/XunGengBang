package com.xungengbang.Config;

public class WJM_Config extends BaseConfig {
    @Override
    public String getServerId() {
        return "http://192.168.11.134:8102/";
    }

    @Override
    public String getPackage() {
        return ".Activity.WJM_Activity";
    }
}

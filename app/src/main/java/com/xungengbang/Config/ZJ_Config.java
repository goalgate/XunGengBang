package com.xungengbang.Config;

public class ZJ_Config extends BaseConfig{
    @Override
    public String getServerId() {
        return "http://211.90.38.12:8081/";
    }

    @Override
    public String getPackage() {
        return ".Activity.ZJ_Activity";
    }

    @Override
    public String getMainActivity() {
        return ".MainActivity";
    }

    @Override
    public boolean stick() {
        return true;
    }

    @Override
    public String getChildServerId() {
        return null;
    }
}

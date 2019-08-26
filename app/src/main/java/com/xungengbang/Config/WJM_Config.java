package com.xungengbang.Config;

public class WJM_Config extends BaseConfig {
    @Override
    public String getServerId() {
        return "http://yzbyun.wxhxp.cn:81/";
    }

    @Override
    public String getChildServerId() {
        return null;
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
            return ".MainActivity";
        }else {
            return ".MainActivity2";
        }
    }
}

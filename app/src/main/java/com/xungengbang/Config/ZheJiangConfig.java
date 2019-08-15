package com.xungengbang.Config;

public class ZheJiangConfig extends BaseConfig{

    @Override
    public String getServerId() {
        return "http://yzbyun.wxhxp.cn:81/";
    }

    @Override
    public String getPackage() {
        return ".Activity.WJM_Activity";
    }

    @Override
    public boolean stick() {
        return true;
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

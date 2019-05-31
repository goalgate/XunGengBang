package com.xungengbang;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.blankj.utilcode.util.Utils;
import com.xungengbang.Config.BaseConfig;
import com.xungengbang.Config.ZJ_Config;
import com.xungengbang.greendao.DaoMaster;
import com.xungengbang.greendao.DaoSession;


public class AppInit extends Application {
    private DaoMaster.DevOpenHelper mHelper;

    private SQLiteDatabase db;

    private DaoMaster mDaoMaster;

    private DaoSession mDaoSession;

    protected static AppInit instance;

    public static AppInit getInstance() {
        return instance;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

    private static BaseConfig config;

    public static BaseConfig getConfig() {
        return config;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        Utils.init(getContext());

        config = new ZJ_Config();

        setDatabase();
    }

    private void setDatabase() {
        mHelper = new DaoMaster.DevOpenHelper(this, "user-db", null);
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}

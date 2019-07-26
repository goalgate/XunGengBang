package com.xungengbang.Connect;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xungengbang.AppInit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitGenerator {


    private static String testUrl = "http://192.168.11.134:8102/";
    private static String testUrl1 = "http://1i713e1305.imwork.net:23728/";
    private static SPUtils config = SPUtils.getInstance("config");
    private static ConnectApi connectApi;
    private static TestApi testApi;

    private static OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();
    private static <S> S createService(Class<S> serviceClass) {
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .baseUrl(config.getString("ServerId")).client(client).build()
                .baseUrl(AppInit.getConfig().getServerId()).client(client).build();
        return retrofit.create(serviceClass);
    }

    private  <S> S createService(Class<S> serviceClass, String url) {
        OkHttpClient client = okHttpClient.connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(url).client(client).build();
        return retrofit.create(serviceClass);
    }

    public  TestApi getTestApi(String url) {
        if (testApi == null) {
            testApi = createService(TestApi.class,url);
        }
        return testApi;
    }
    public static TestApi getTestApi() {
        if (testApi == null) {
            testApi = createService(TestApi.class);
        }
        return testApi;
    }

    public  ConnectApi getConnectApi(String url) {
        if (connectApi == null) {
            connectApi = createService(ConnectApi.class,url);
        }
        return connectApi;
    }
    public static ConnectApi getConnectApi() {
        if (connectApi == null) {
            connectApi = createService(ConnectApi.class);
        }
        return connectApi;
    }
}

package com.xungengbang.Connect;

import com.blankj.utilcode.util.SPUtils;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ConnectApi {

    @POST("appLogin/s/checkLogin")
    Observable<ResponseBody> login(@Header("tercode") String daid, @Body RequestBody body);



    @POST("api/sbXungengInfo/insert")
    Observable<ResponseBody> updata(@Header("authorization-token") String token, @Body RequestBody body);


    @FormUrlEncoded
    @POST("xg_sjjk")
    Observable<ResponseBody> photoUpload(@Field("dataType") String dataType, @Field("jsonData") String jsonData);
}

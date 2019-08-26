package com.xungengbang.Connect;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;


public interface ZheJiangApi {

    @POST("appLogin/s/checkLogin")
    Observable<ResponseBody> login(@Header("tercode") String daid, @Body RequestBody body);



    @POST("xungengInfo/authc/insert")
    Observable<ResponseBody> updata(@Header("authorization-token") String token, @Body RequestBody body);


    @POST("XungengInfo/photoUp")
    Observable<ResponseBody> photoUpload(@Header("authorization-token") String token, @Body RequestBody body);


    @POST("v1/tickets")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<Response<ResponseBody>> login1( @QueryMap Map<String,Object> maps);

    @POST
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<Response<ResponseBody>> login2(@Url String url, @QueryMap Map<String,Object> maps);

    @GET("p3/serviceValidate")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<Response<ResponseBody>> login3(@QueryMap Map<String,Object> maps);

}

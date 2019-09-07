package com.xungengbang.Connect;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.JsonElement;
import com.xungengbang.Bean.SbXungengInfoFj;
import com.xungengbang.Bean.SbXungengInfoVo;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface ConnectApi {

    @POST("appLogin/s/checkLogin")
    Observable<ResponseBody> login(@Header("tercode") String daid, @Body RequestBody body);

    @POST("api/sbXungengInfo/authc/insertBaojing")
    Observable<ResponseBody> alarm(@Header("authorization-token") String token, @Body RequestBody body);


    @POST("api/sbXungengInfo/insert")
    Observable<ResponseBody> updata(@Header("authorization-token") String token, @Body RequestBody body);

    @POST("api/sbXungengInfo/insert")
    Observable<ResponseBody> updataFile(@Header("authorization-token") String token,  @Body SbXungengInfoVo body );

    @Multipart
    @POST("app/fileUpload/authc/directUploadToFastDFS")
    Observable<ResponseBody> uploadPhotos(@Header("authorization-token") String token,
                                          @Part MultipartBody.Part... files);


    @FormUrlEncoded
    @POST("xg_sjjk")
    Observable<ResponseBody> photoUpload(@Field("dataType") String dataType, @Field("jsonData") String jsonData);
}

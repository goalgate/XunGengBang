package com.xungengbang.Connect;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface TestApi {

    @POST("shebeiDataAction_login.do")
    Observable<ResponseBody> login(@Header("tercode") String daid, @Body RequestBody body);



    @POST("shebeiDataAction_xungengShebei.do")
    Observable<ResponseBody> updata(@Header("authorization-token") String token, @Body RequestBody body);

}

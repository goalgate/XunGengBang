package com.xungengbang.Connect;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface GlobalApi {
    @Streaming
    @GET
    Observable<ResponseBody> DownLoad(@Url String Url);

}

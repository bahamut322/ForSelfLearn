package com.sendi.fooddeliveryrobot;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("ASR")
    Call<ResponseBody> uploadFile(@Part("model") RequestBody body, @Part MultipartBody.Part file);

    @Multipart
    @POST("wechatWork/automaticResponse/getVFFileToText")
    Call<ResponseBody> uploadFile2(@Part MultipartBody.Part file);
}

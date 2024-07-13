package com.configlite.network;

import com.configlite.type.NetworkModel;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;


public interface RetrofitApiInterface {

    @GET("{endpoint}")
    Call<NetworkModel> requestGet(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @POST("{endpoint}")
    Call<NetworkModel> requestPost(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @Multipart
    @POST("{endpoint}")
    Call<NetworkModel> requestPost(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options
            , @Part("name") RequestBody requestBody, @Part MultipartBody.Part multipartBody);

    @FormUrlEncoded
    @POST("{endpoint}")
    Call<NetworkModel> requestPostDataForm(@Path("endpoint") String endpoint, @FieldMap Map<String, String> options);

    @Multipart
    @FormUrlEncoded
    @POST("{endpoint}")
    Call<NetworkModel> requestPostDataForm(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options
            , @Part("name") RequestBody requestBody, @Part MultipartBody.Part multipartBody);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl, @QueryMap Map<String, String> options);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl, @QueryMap Map<String, String> options, @Header("Content-Type") String contentType);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl);

    @Streaming
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlAsync(@Url String fileUrl, @Header("Content-Type") String contentType);

    @Headers({"Accept-Encoding: *","Content-Type: application/pdf"})
    @Streaming
    @GET
    Call<ResponseBody> downloadPDFFileWithDynamicUrlAsync(@Url String fileUrl, @QueryMap Map<String, String> options);
}

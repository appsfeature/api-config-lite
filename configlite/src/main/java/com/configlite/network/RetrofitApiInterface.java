package com.configlite.network;

import com.configlite.type.NetworkModel;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


public interface RetrofitApiInterface {

    @POST("{endpoint}")
    Call<NetworkModel> requestPost(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @GET("{endpoint}")
    Call<NetworkModel> requestGet(@Path("endpoint") String endpoint, @QueryMap Map<String, String> options);

    @FormUrlEncoded
    @POST("{endpoint}")
    Call<NetworkModel> requestPostDataForm(@Path("endpoint") String endpoint, @FieldMap Map<String, String> options);

}

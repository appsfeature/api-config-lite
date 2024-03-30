package com.configlite.network;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.configlite.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitBuilder {

    public static Retrofit getClient(String host, String securityCode) {
        return getClient(host, securityCode, ConfigManager.getInstance().isEnableDebugMode());
    }

    public static Retrofit getClient(String host, String securityCode, boolean isDebug) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        return new Retrofit.Builder()
                .baseUrl(host)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getHttpClient(securityCode, isDebug).build())
                .build();
    }

    private static OkHttpClient.Builder getHttpClient(final String securityCode, boolean isDebug) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @NonNull
                    @Override
                    public Response intercept(@NonNull Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder request = original.newBuilder()
                                .method(original.method(), original.body());
                        if (!TextUtils.isEmpty(securityCode)) {
                            request.header("Authorization", securityCode);
                        }
                        if (ConfigManager.getInstance().getHeadersMap().size() > 0) {
                            for (Map.Entry<String, String> entry : ConfigManager.getInstance().getHeadersMap().entrySet()) {
                                String key = entry.getKey();
                                String value = entry.getValue();
                                request.addHeader(key, value);
                            }
                        }
                        return chain.proceed(request.build());
                    }
                });
        if(ConfigManager.getInstance().getTimeout() != null){
            builder.readTimeout(ConfigManager.getInstance().getTimeout().getReadTimeout(), TimeUnit.SECONDS);
            builder.connectTimeout(ConfigManager.getInstance().getTimeout().getConnectTimeout(), TimeUnit.SECONDS);
            builder.writeTimeout(ConfigManager.getInstance().getTimeout().getWriteTimeout(), TimeUnit.SECONDS);
        }
        if (isDebug) {
            builder.addInterceptor(loggingInterceptor);
        }
        return builder;
    }

    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

}

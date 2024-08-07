package com.configlite.network;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.configlite.ConfigManager;
import com.configlite.network.download.DownloadProgressCallback;
import com.configlite.network.download.DownloadProgressInterceptor;
import com.configlite.util.ConfigEncryption;
import com.configlite.util.NetworkLog;
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
        return getClient(host, securityCode, ConfigManager.getInstance().isDebugMode());
    }

    public static Retrofit getClient(String host, String securityCode, boolean isDebug) {
        return getClient(host, securityCode, null, isDebug);
    }

    public static Retrofit getClient(String host, String securityCode, DownloadProgressCallback progressListener, boolean isDebug) {
        try {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            return new Retrofit.Builder()
                    .baseUrl(ConfigEncryption.get(host))
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getHttpClient(securityCode, isDebug, progressListener).build())
                    .build();
        } catch (IllegalArgumentException e) {
            NetworkLog.logIntegration(ConfigManager.TAG_OK_HTTP, NetworkLog.getClassPath(Thread.currentThread().getStackTrace()) ,
                    "\nError : " + e.getMessage(),
                    "\nbaseUrl : BaseUrl not found",
                    "\nbaseUrl : " + host,
                    "\nbaseUrlDec : " + ConfigEncryption.get(host)
            );
            return null;
        }
    }

    private static OkHttpClient.Builder getHttpClient(final String securityCode, boolean isDebug, DownloadProgressCallback progressListener) {
        DownloadProgressInterceptor progressInterceptor = null;
        if(progressListener != null) {
            progressInterceptor =new DownloadProgressInterceptor(progressListener);
        }
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
                        if (!ConfigManager.getInstance().getHeadersMap().isEmpty()) {
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
        if(progressInterceptor != null){
            builder.addInterceptor(progressInterceptor);
        }
        if (isDebug) {
            builder.addInterceptor(loggingInterceptor);
        }
        return builder;
    }

    private static final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

}

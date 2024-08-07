package com.configlite.network.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class DownloadProgressInterceptor implements Interceptor {

    private DownloadProgressCallback listener;

    public DownloadProgressInterceptor(DownloadProgressCallback listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        return originalResponse.newBuilder()
                .body(new DownloadProgressResponseBody(originalResponse.body(), listener))
                .build();
    }
}
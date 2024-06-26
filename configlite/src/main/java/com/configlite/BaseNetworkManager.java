package com.configlite;

import android.content.Context;

import com.configlite.callback.NetworkCallback;
import com.configlite.type.ApiHost;
import com.configlite.type.ApiRequestType;
import com.configlite.type.NetworkModel;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

public class BaseNetworkManager {

    protected final ConfigManager configManager;

    public BaseNetworkManager(Context context, String baseUrl) {
        this(ConfigManager.getInstance()
                .setEnableDebugMode(true)
                .setEnableSecurityCode(context)
                .addHostUrl(ApiHost.HOST_DEFAULT, baseUrl));
    }
    public BaseNetworkManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void getData(Context context, String methodName, NetworkCallback.Callback<Boolean> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("pkg_name", context.getPackageName());
        callback.onProgressUpdate(true);
        configManager.getData(ApiRequestType.GET, methodName, params, new NetworkCallback.Response<NetworkModel>() {
            @Override
            public void onComplete(boolean status, NetworkModel data) {
                callback.onProgressUpdate(false);
                callback.onSuccess(status);
            }

            @Override
            public void onError(int responseCode, String errorMessage, Exception e) {
                callback.onProgressUpdate(false);
                callback.onFailure(e);
            }
        });
    }
}

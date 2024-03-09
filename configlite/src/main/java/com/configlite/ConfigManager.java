package com.configlite;

import android.content.Context;

import androidx.annotation.Nullable;

import com.configlite.callback.NetworkCallback;
import com.configlite.type.NetworkModel;
import com.configlite.network.ResponseCallBack;
import com.configlite.network.RetrofitApiInterface;
import com.configlite.network.RetrofitBuilder;
import com.configlite.type.ApiHost;
import com.configlite.type.ApiRequestType;
import com.configlite.type.ResponseStatusCode;
import com.configlite.util.NetworkLog;
import com.configlite.util.NetworkUtility;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static ConfigManager instance;
    private final HashMap<String, String> apiHostUrlHashMap = new HashMap<>();
    private final HashMap<String, RetrofitApiInterface> apiInterfaceHashMap = new HashMap<>();
    public static final String HOST_DEFAULT = ApiHost.HOST_DEFAULT;
    private boolean isEnableDebugMode = false;
    private String securityCode;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if(instance == null) instance = new ConfigManager();
        return instance;
    }

    public void getData(@ApiRequestType int reqType, String endPoint, Map<String, String> params, NetworkCallback.Response<NetworkModel> callback) {
        getData(reqType, HOST_DEFAULT, endPoint, params, callback);
    }

    public void getData(@ApiRequestType int reqType, String hostName, String endPoint, Map<String, String> params, NetworkCallback.Response<NetworkModel> callback) {
        RetrofitApiInterface apiInterface = getApiInterface(hostName);
        if(apiInterface != null) {
            if (reqType == ApiRequestType.POST) {
                apiInterface.requestPost(endPoint, params)
                        .enqueue(new ResponseCallBack<>(callback));
            } else if (reqType == ApiRequestType.POST_FORM) {
                apiInterface.requestPostDataForm(endPoint, params)
                        .enqueue(new ResponseCallBack<>(callback));
            } else {
                apiInterface.requestGet(endPoint, params)
                        .enqueue(new ResponseCallBack<>(callback));
            }
        }else {
            NetworkLog.logIntegration(TAG_OK_HTTP, NetworkLog.getClassPath(Thread.currentThread().getStackTrace()) ,
                    "\nBaseNetworkManager.apiInterface == null",
                    "\nbaseUrl : BaseUrl not found",
                    "\nhostName : " + hostName,
                    "\nendPoint : " + endPoint
            );
//            callback.onFailure(null, new Exception("Error : Base URL not set yet"));
            callback.onError(ResponseStatusCode.BAD_REQUEST, new Exception("Error : Base URL not set yet"));
        }
    }

    @Nullable
    private RetrofitApiInterface getApiInterface(String hostName) {
        return getHostInterface(getHostBaseUrl(hostName));
    }

    @Nullable
    private String getHostBaseUrl(String hostName) {
        return apiHostUrlHashMap.get(hostName);
    }

    public void addHostUrl(Map<String, String> hostMap) {
        for (Map.Entry<String,String> entry : hostMap.entrySet()) {
          String hostName = entry.getKey();
          String baseUrl = entry.getValue();
          addHostUrl(hostName, baseUrl);
        }
    }

    public ConfigManager addHostUrl(String hostName, String baseUrl) {
        if (apiHostUrlHashMap.get(hostName) == null) {
            apiHostUrlHashMap.put(hostName, baseUrl);
            getHostInterface(baseUrl);
        }
        return this;
    }

    private RetrofitApiInterface getHostInterface(String hostBaseUrl) {
        if(hostBaseUrl == null) return null;
        if (apiInterfaceHashMap.get(hostBaseUrl) != null) {
            return apiInterfaceHashMap.get(hostBaseUrl);
        } else {
            RetrofitApiInterface apiInterface = RetrofitBuilder.getClient(hostBaseUrl, getSecurityCode(), isEnableDebugMode())
                    .create(RetrofitApiInterface.class);
            apiInterfaceHashMap.put(hostBaseUrl, apiInterface);
            return apiInterface;
        }
    }

    public static final String TAG_OK_HTTP = ConfigManager.class.getSimpleName() + "-okhttp-log";

    public boolean isEnableDebugMode() {
        return isEnableDebugMode;
    }

    public ConfigManager setEnableDebugMode(boolean isEnableDebugMode) {
        this.isEnableDebugMode = isEnableDebugMode;
        return this;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public ConfigManager setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
        return this;
    }

    public ConfigManager setEnableSecurityCode(Context context) {
        this.securityCode = NetworkUtility.getSecurityCode(context);
        return this;
    }
}

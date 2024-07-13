package com.configlite;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.configlite.callback.NetworkCallback;
import com.configlite.network.download.ConfigDownloadListener;
import com.configlite.network.download.DownloadProgressCallback;
import com.configlite.type.NetworkModel;
import com.configlite.network.ResponseCallBack;
import com.configlite.network.RetrofitApiInterface;
import com.configlite.network.RetrofitBuilder;
import com.configlite.type.ApiHost;
import com.configlite.type.ApiRequestType;
import com.configlite.type.NetworkTimeOut;
import com.configlite.type.ResponseStatusCode;
import com.configlite.util.NetworkError;
import com.configlite.util.NetworkLog;
import com.configlite.util.NetworkUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigManager {
    private static ConfigManager instance;
    private final HashMap<String, String> apiHostUrlHashMap = new HashMap<>();
    private final HashMap<String, RetrofitApiInterface> apiInterfaceHashMap = new HashMap<>();
    private final HashMap<String, String> headersMap = new HashMap<>();
    private boolean isDebugMode = false;
    private String securityCode;
    private NetworkTimeOut timeout;
    private String encDataKey;

    private ConfigManager() {
    }

    public static ConfigManager getInstance() {
        if(instance == null) instance = new ConfigManager();
        return instance;
    }

    public void getData(@ApiRequestType int reqType, String endPoint, Map<String, String> params, NetworkCallback.Response<NetworkModel> callback) {
        getData(true, reqType, ApiHost.HOST_MAIN, endPoint, params, callback);
    }

    public void getData(@ApiRequestType int reqType, String hostName, String endPoint, Map<String, String> params, NetworkCallback.Response<NetworkModel> callback) {
        getData(true, reqType, hostName, endPoint, params, callback);
    }

    public void getDataBackground(@ApiRequestType int reqType, String endPoint, Map<String, String> params, NetworkCallback.Response<NetworkModel> callback) {
        getData(false, reqType, ApiHost.HOST_MAIN, endPoint, params, callback);
    }

    @WorkerThread
    public void getDataBackground(@ApiRequestType int reqType, String hostName, String endPoint, Map<String, String> params, NetworkCallback.Response<NetworkModel> callback) {
        getData(false, reqType, hostName, endPoint, params, callback);
    }

    public void uploadData(@ApiRequestType int reqType, String hostName, String endPoint, Map<String, String> params, RequestBody requestBody, MultipartBody.Part multipartBody, NetworkCallback.Response<NetworkModel> callback) {
        getData(true, reqType, hostName, endPoint, params, requestBody, multipartBody, callback);
    }

    public void uploadDataBackground(@ApiRequestType int reqType, String hostName, String endPoint, Map<String, String> params, RequestBody requestBody, MultipartBody.Part multipartBody, NetworkCallback.Response<NetworkModel> callback) {
        getData(false, reqType, hostName, endPoint, params, requestBody, multipartBody, callback);
    }

    private void getData(boolean isRunOnMainThread, @ApiRequestType int reqType, String hostName, String endPoint, Map<String, String> params, NetworkCallback.Response<NetworkModel> callback) {
        getData(isRunOnMainThread, reqType, hostName, endPoint, params, null, null, callback);
    }

    private void getData(boolean isRunOnMainThread, @ApiRequestType int reqType, String hostName, String endPoint, Map<String, String> params, RequestBody requestBody, MultipartBody.Part multipartBody, NetworkCallback.Response<NetworkModel> callback) {
        RetrofitApiInterface apiInterface = getApiInterface(hostName);
        if(apiInterface != null) {
            Call<NetworkModel> request;
            if (reqType == ApiRequestType.POST) {
                request = apiInterface.requestPost(endPoint, validateParams(params));
            } else if (reqType == ApiRequestType.POST_FORM) {
                request = apiInterface.requestPostDataForm(endPoint, validateParams(params));
            } else if (reqType == ApiRequestType.POST_MULTIPART) {
                request = apiInterface.requestPost(endPoint, validateParams(params), requestBody, multipartBody);
            } else if (reqType == ApiRequestType.POST_FORM_MULTIPART) {
                request = apiInterface.requestPostDataForm(endPoint, validateParams(params), requestBody, multipartBody);
            } else {
                request = apiInterface.requestGet(endPoint, validateParams(params));
            }
            if(isRunOnMainThread) {
                request.enqueue(new ResponseCallBack<>(callback));
            }else {
                try {
                    Response<NetworkModel> response = request.execute();
                    ResponseCallBack<NetworkModel> responseCallback = new ResponseCallBack<>(callback);
                    if (response.isSuccessful()) {
                        responseCallback.onResponse(request, response);
                    } else {
                        responseCallback.onFailure(request, new Exception(response.message()));
                    }
                } catch (IOException e) {
                    NetworkLog.logIntegration(TAG_OK_HTTP, NetworkLog.getClassPath(Thread.currentThread().getStackTrace()) ,
                            "\nhostName : " + hostName,
                            "\nendPoint : " + endPoint,
                            e.toString()
                    );
                    callback.onError(ResponseStatusCode.ERROR_BASE_URL, NetworkError.BASE_URL_ERROR, new Exception(NetworkError.BASE_URL_ERROR));
                }
            }
        }else {
            NetworkLog.logIntegration(TAG_OK_HTTP, NetworkLog.getClassPath(Thread.currentThread().getStackTrace()) ,
                    "\nBaseNetworkManager.apiInterface == null",
                    "\nbaseUrl : BaseUrl not found",
                    "\nhostName : " + hostName,
                    "\nendPoint : " + endPoint
            );
            callback.onError(ResponseStatusCode.ERROR_BASE_URL, NetworkError.BASE_URL_ERROR, new Exception("Error : Base URL not set yet"));
        }
    }

    public Map<String, String> validateParams(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                entry.setValue("");
            }
        }
        return params;
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
        if(TextUtils.isEmpty(hostBaseUrl)) return null;
        if (apiInterfaceHashMap.get(hostBaseUrl) != null) {
            return apiInterfaceHashMap.get(hostBaseUrl);
        } else {
            RetrofitApiInterface apiInterface = RetrofitBuilder.getClient(hostBaseUrl, getSecurityCode(), isDebugMode())
                    .create(RetrofitApiInterface.class);
            apiInterfaceHashMap.put(hostBaseUrl, apiInterface);
            return apiInterface;
        }
    }

    // used for download file with progress update
    @Nullable
    public RetrofitApiInterface getApiDownloadInterface(String hostName) {
        return getHostDownloadInterface(getHostBaseUrl(hostName));
    }

    public RetrofitApiInterface getHostDownloadInterface(String hostBaseUrl) {
        if(TextUtils.isEmpty(hostBaseUrl)) return null;
        if (apiInterfaceHashMap.get(hostBaseUrl) != null) {
            return apiInterfaceHashMap.get(hostBaseUrl);
        } else {
            RetrofitApiInterface apiInterface = RetrofitBuilder.getClient(hostBaseUrl, getSecurityCode(), mProgressListener, isDebugMode())
                    .create(RetrofitApiInterface.class);
            apiInterfaceHashMap.put(hostBaseUrl, apiInterface);
            return apiInterface;
        }
    }

    private final DownloadProgressCallback mProgressListener = new DownloadProgressCallback() {
        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            try {
                int progressUpdate = (int) ((bytesRead * 100) / contentLength);
                if (mDownloadListener != null) {
                    mDownloadListener.onProgressUpdate(progressUpdate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public ConfigDownloadListener mDownloadListener;

    /**
     * @param downloadListener  add Handler for show data on Main Thread
     * @return Note. getting data in background thread.
     */
    public ConfigManager setDownloadListener(ConfigDownloadListener downloadListener) {
        mDownloadListener = null;
        this.mDownloadListener = downloadListener;
        return this;
    }

    public static final String TAG_OK_HTTP = ConfigManager.class.getSimpleName() + "-okhttp-log";

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public ConfigManager setDebugMode(boolean isEnableDebugMode) {
        this.isDebugMode = isEnableDebugMode;
        return this;
    }

    public HashMap<String, String> getHeadersMap() {
        return headersMap;
    }

    public ConfigManager setHeadersMap(HashMap<String, String> headersMap) {
        this.headersMap.clear();
        this.headersMap.putAll(headersMap);
        return this;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public ConfigManager setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
        return this;
    }

    public NetworkTimeOut getTimeout() {
        return timeout;
    }

    public ConfigManager setTimeout(NetworkTimeOut timeout) {
        this.timeout = timeout;
        return this;
    }

    public ConfigManager setEnableSecurityCode(Context context) {
        this.securityCode = NetworkUtility.getSecurityCode(context);
        return this;
    }

    public String getEncDataKey() {
        return encDataKey;
    }

    public ConfigManager setEncDataKey(String encDataKey) {
        this.encDataKey = encDataKey;
        return this;
    }
}

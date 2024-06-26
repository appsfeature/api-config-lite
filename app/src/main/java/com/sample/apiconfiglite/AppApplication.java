package com.sample.apiconfiglite;

import android.app.Application;
import android.content.Context;

import com.configlite.ConfigManager;
import com.configlite.type.ApiHost;
import com.configlite.type.NetworkTimeOut;

public class AppApplication extends Application {


    private static final String BASE_URL = "http://yourdomain.com/apps/api/v1/database/";

    private static volatile AppApplication instance;

    public static AppApplication getInstance() {
        if (instance == null) {
            synchronized (AppApplication.class) {
                if (instance == null) instance = new AppApplication();
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configManager = getConfigManager();

    }

    private ConfigManager configManager;

    public ConfigManager getConfigManager() {
        if(configManager == null) {
            NetworkTimeOut timeout = new NetworkTimeOut();
            timeout.setReadTimeout(30);
            configManager = ConfigManager.getInstance()
                    .setEnableDebugMode(BuildConfig.DEBUG)
                    .setTimeout(timeout)
                    .setEnableSecurityCode(AppApplication.this)
                    .addHostUrl(ApiHost.HOST_DEFAULT, BASE_URL);
        }
        return configManager;
    }
}

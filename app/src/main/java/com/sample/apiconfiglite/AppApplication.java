package com.sample.apiconfiglite;

import android.app.Application;
import android.util.Log;

import com.configlite.ConfigManager;
import com.configlite.type.ApiHost;
import com.configlite.type.NetworkTimeOut;
import com.configlite.util.ConfigEncryption;

public class AppApplication extends Application {


    private static final String BASE_URL = "NT+m8pLMZKlWWpudyDZh5IXoj1+qZS2ecrIwn9veup2QAuJCxx8jJ25zZ9IoS6ds";

    private static volatile AppApplication instance;

    public static AppApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        configManager = getConfigManager();
    }

    private ConfigManager configManager;

    public ConfigManager getConfigManager() {
        if(configManager == null) {
            NetworkTimeOut timeout = new NetworkTimeOut();
            timeout.setReadTimeout(30);
            configManager = ConfigManager.getInstance()
                    .setDebugMode(BuildConfig.DEBUG)
                    .setEncDataKey("test")
                    .setTimeout(timeout)
                    .setEnableSecurityCode(instance)
                    .addHostUrl(ApiHost.HOST_MAIN, BASE_URL);
        }
        return configManager;
    }
}

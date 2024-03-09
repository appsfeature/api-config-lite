package com.sample.apiconfiglite;

import com.configlite.BaseNetworkManager;
import com.configlite.ConfigManager;

public class NetworkManager extends BaseNetworkManager {

    private static volatile NetworkManager instance;

    public NetworkManager(ConfigManager configManager) {
        super(configManager);
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            synchronized (NetworkManager.class) {
                if (instance == null) instance = new NetworkManager(AppApplication.getInstance().getConfigManager());
            }
        }
        return instance;
    }

}

# api-config-lite

#### Library size is : Kb

## Setup Project

Add this to your project build.gradle
``` gradle
allprojects {
    repositories {
        maven {
            url "https://jitpack.io"
        }
    }
}
```

Add this to your project build.gradle

#### Dependency
[![](https://jitpack.io/v/appsfeature/api-config-lite.svg)](https://jitpack.io/#appsfeature/api-config-lite)
```gradle
dependencies {
        implementation 'com.github.appsfeature:api-config-lite:1.0'
}
```


### Statistics Usage methods
```java
public class AppApplication extends Application {
    private static final String BASE_URL = "http://yourdomain.com/apps/api/v1/database/";

    @Override
    public void onCreate() {
        super.onCreate();
        ...
        configManager = getConfigManager();
    }

    private ConfigManager configManager;

    public ConfigManager getConfigManager() {
        if(configManager == null) {
            configManager = ConfigManager.getInstance()
                    .setEnableDebugMode(BuildConfig.DEBUG)
                    .setEnableSecurityCode(AppApplication.this)
                    .addHostUrl(ApiHost.HOST_DEFAULT, BASE_URL);
        }
        return configManager;
    }

```

```java
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
```

## ChangeLog

#### Version 1.4-alpha01:
* Removed AdsSdkMaster library

#### Version 1.3-beta01:
* Update Tracking class and usage methods.

#### Version 1.0:
* Initial build

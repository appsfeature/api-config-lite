package com.configlite.util;

import android.text.TextUtils;

import com.configlite.ConfigManager;
import com.helper.util.EncryptionHandler;


public class ConfigEncryption {

    public static String get(String encData) {
        if(!TextUtils.isEmpty(ConfigManager.getInstance().getEncDataKey())){
            return EncryptionHandler.decrypt(ConfigManager.getInstance().getEncDataKey(), encData);
        }else {
            return encData;
        }
    }

}

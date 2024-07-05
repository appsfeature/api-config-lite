package com.configlite.util;

import android.util.Log;

import com.configlite.ConfigManager;

public class NetworkLog {

    public static final String TAG = "api-config-lite";

    /**
     * @apiNote throw new IllegalArgumentException();
     */
    public static void logIntegration(String tag, String... s){
        Log.e(tag, ".     |  |");
        Log.e(tag, ".     |  |");
        Log.e(tag, ".     |  |");
        Log.e(tag, ".   \\ |  | /");
        Log.e(tag, ".    \\    /");
        Log.e(tag, ".     \\  /");
        Log.e(tag, ".      \\/");
        Log.e(tag, ".");
        for (String message : s) {
            Log.e(tag, message);
        }
        Log.e(tag, ".");
        Log.e(tag, ".      /\\");
        Log.e(tag, ".     /  \\");
        Log.e(tag, ".    /    \\");
        Log.e(tag, ".   / |  | \\");
        Log.e(tag, ".     |  |");
        Log.e(tag, ".     |  |");
        Log.e(tag, ".");
    }

    /**
     * @param currentThread = Thread.currentThread().getStackTrace()
     * @return Getting the name of the currently executing method
     */
    public static String getClassPath(StackTraceElement[] currentThread) {
        try {
            if(currentThread!=null && currentThread.length>=3){
                if(currentThread[2]!=null){
                    return currentThread[2].toString()+" [Line Number = "+currentThread[2].getLineNumber()+"]";
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    public static void log(String log) {
        if(ConfigManager.getInstance().isDebugMode()){
            Log.d(TAG, log);
        }
    }

    public static void logError(String log) {
        if(ConfigManager.getInstance().isDebugMode()){
            Log.e(TAG, log);
        }
    }
}

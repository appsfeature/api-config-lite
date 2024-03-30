package com.configlite.type;

/**
 * TimeUnit.SECONDS
 * Default time is 10 sec
 */
public class NetworkTimeOut {
    private long readTimeout = 10;
    private long connectTimeout = 10;
    private long writeTimeout = 10;

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }
}

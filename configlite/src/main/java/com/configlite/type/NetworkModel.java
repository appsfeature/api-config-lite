package com.configlite.type;

import androidx.annotation.Nullable;

import com.configlite.util.GsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;


public class NetworkModel {

    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("data")
    @Expose
    private Object data;

    @SerializedName("image_path")
    @Expose
    private String imagePath;

    public void setData(Object data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Nullable
    public String getData() {
        try {
            return GsonParser.getGson().toJson(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param classOfT : ModelName.class
     */
    @Nullable
    public <T> T getData(Class<T> classOfT) {
        try {
            return GsonParser.getGson().fromJson(getData(), classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param typeCast : new TypeToken<ModelName>() {}
     */
    @Nullable
    public <T> T getData(TypeToken<T> typeCast) {
        return GsonParser.fromJson(getData(), typeCast);
    }
}

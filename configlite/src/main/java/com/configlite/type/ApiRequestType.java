package com.configlite.type;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
        ApiRequestType.GET,
        ApiRequestType.POST,
        ApiRequestType.POST_FORM,
        ApiRequestType.POST_MULTIPART,
        ApiRequestType.POST_FORM_MULTIPART,
})
@Retention(RetentionPolicy.SOURCE)
public @interface ApiRequestType {
    int GET = 0;
    int POST = 1;
    int POST_FORM = 3;
    int POST_MULTIPART = 4;
    int POST_FORM_MULTIPART = 5;
}


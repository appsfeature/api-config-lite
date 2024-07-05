package com.sample.apiconfiglite;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.configlite.callback.NetworkCallback;
import com.configlite.type.ApiRequestType;
import com.configlite.type.NetworkModel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        (findViewById(R.id.btn_call_api)).setOnClickListener(v -> {
            AppApplication.getInstance().getConfigManager().getData(ApiRequestType.GET, "login", new HashMap<>(), new NetworkCallback.Response<NetworkModel>() {
                @Override
                public void onComplete(boolean status, NetworkModel response) {

                }

                @Override
                public void onError(int responseCode, String errorMessage, Exception e) {

                }
            });
        });

    }
}

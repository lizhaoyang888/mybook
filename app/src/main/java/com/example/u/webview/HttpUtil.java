package com.example.u.webview;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by U on 2018/3/28.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String adress, okhttp3.Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(adress)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}

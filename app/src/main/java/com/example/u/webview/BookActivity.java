package com.example.u.webview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by U on 2018/5/19.
 */

public class BookActivity extends AppCompatActivity{

    private EditText num;
    private EditText size;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        num = (EditText) findViewById(R.id.num);
        text = (TextView) findViewById(R.id.text);
        size = (EditText)findViewById(R.id.size);
    }
    private void showResponseData(final String responseData){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(responseData);
            }
        });
    }

    public void doClick(View view){
        switch (view.getId()){
            case R.id.teacher_query:
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            parseJSONwithGSON("teacher");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
            case R.id.my_query:
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            parseJSONwithGSON("my");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
        }
    }

    public void parseJSONwithGSON(String param) throws InterruptedException {
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = new FormBody.Builder()
                .add("pageNum",num.getText().toString())
                .add("pageSize",size.getText().toString())
                .build();
        Request request = null;
        if ("teacher".equals(param)) {
            request = new Request.Builder()
                    .url("http://47.92.91.234:17770/android-api/book/queryByPage")//请求的url
                    .post(requestBody)
                    .build();
        }else if ("my".equals(param)){
            request = new Request.Builder()
                    .url(Constant.MYBOOK_QUERY)//请求的url
                    .post(requestBody)
                    .build();
        }
        Call call = okHttpClient.newCall(request);
        //加入队列 异步操作
        call.enqueue(new Callback() {
            //请求错误回调方法
            @Override
            public void onFailure(Call call, IOException e) {
                showResponseData("连接失败");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                showResponseData(responseData);
            }
        });

    }
}

package com.example.u.webview;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by U on 2018/5/18.
 */

public class UserActivity extends AppCompatActivity{

    private TextView text;
    private EditText name;
    private EditText password;
    private EditText age;
    private EditText birthday;

    private int mYear;
    private int mMonth;
    private int mDay;

    private RadioButton male;
    private RadioButton female;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        text = (TextView) findViewById(R.id.text);
        name = (EditText) findViewById(R.id.name);
        password = (EditText)findViewById(R.id.password);
        age = (EditText) findViewById(R.id.age);
        birthday = (EditText)findViewById(R.id.birthday);
        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.femle);

    }
    public User doUser(){
        String user_name = name.getText().toString();
        String user_passord = password.getText().toString();

        String user_sex = (male.isChecked()?1:0) + "";
        String user_age = age.getText().toString();
        String user_birth = birthday.getText().toString();

        User user = new User();
        Date date = DateFormater.getDate(user_birth);
        user.setAge(Integer.parseInt(user_age));
        user.setPassWord(user_passord);
        user.setUserName(user_name);
        user.setSex(Integer.parseInt(user_sex));
        user.setBirthDay(String.valueOf(date.getTime()));

        return user;
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            String days = mYear+"-"+mMonth+"-"+mDay;
            birthday.setText(days);
        }
    };

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
            case R.id.btn2:
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            parseJSONwithGSON("register");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
            case R.id.btn3:
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            parseJSONwithGSON("login");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
            case R.id.birthday:
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(UserActivity.this,onDateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
                break;
        }
    }

    public void parseJSONwithGSON(String param) throws InterruptedException {
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        Request request = null;
        if ("register".equals(param)) {

            User user = doUser();
            Gson gson = new Gson();
            String json = gson.toJson(user);
            showResponseData(json);

            RequestBody requestBody = new FormBody.Builder()
                    .add("data",json)
                    .build();
            request = new Request.Builder()
                    .url("http://47.92.91.234:17770/android-api/user/register")//请求的url
                    .post(requestBody)
                    .build();

        }else if ("login".equals(param)){
            String user_name = name.getText().toString();
            String user_pwd = password.getText().toString();
            showResponseData("name: "+user_name+"\npassword: "+user_pwd);

            RequestBody requestBody = new FormBody.Builder()
                    .add("userName",user_name)
                    .add("passWord",user_pwd)
                    .build();

            request = new Request.Builder()
                    .url("http://47.92.91.234:17770/android-api/user/login")//请求的url
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

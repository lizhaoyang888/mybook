package com.example.u.webview;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.*;


public class MainActivity extends AppCompatActivity {

    private static final int PHOTO_REQUEST = 1;
    private static final int PHOTO_CLIP = 3;
    private static final int UPLOAD_INIT_PROCESS = 4;//上传初始化
    protected static final int UPLOAD_FILE_DONE = 2;//上传中
    private static final int UPLOAD_IN_PROCESS = 5;//上传文件响应
    private final static int OK = 1;
    private final static int FAIL = 2;
    private TextView text;
    private EditText name;
    private EditText password;
    private EditText no;
    private ImageView imageView;
    private Button photo;
    private File filepath;//返回的文件地址
    private ProgressDialog pd;

    private static final String TAG = "MSG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.text);
        name = (EditText) findViewById(R.id.name);
        password = (EditText)findViewById(R.id.password);
        no = (EditText)findViewById(R.id.no);
        imageView = (ImageView)findViewById(R.id.img);
        photo = (Button)findViewById(R.id.photo);
    }
    public void doClick(View view){
        switch (view.getId()){
            case R.id.photo:
                getPhoto();
                break;
            case R.id.btn2:

                break;
            case R.id.btn3:
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            parseJSONwithGSON("query");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
        }
    }

    /**
     * 从相册选择图片来源
     */
    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_REQUEST://从相册取
                if (data != null) {
                    Uri uri = data.getData();
                    //对相册取出照片进行裁剪
                    photoClip(uri);
                }
                break;
            case PHOTO_CLIP:
                //完成
                if (data != null) {

                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        try {
                            //获得图片路径
                            filepath = UploadUtil.saveFile(photo, Environment.getExternalStorageDirectory().toString(), "hand.jpg");
                            //上传照片
                            Log.i("IMG",filepath.getPath());
                            uploadImage(filepath.getPath());
                           // toUploadFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //上传完成将照片写入imageview与用户进行交互
                        imageView.setImageBitmap(photo);
                    }
                }
                break;
        }
    }

    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_CLIP);
    }

    public MyPerson doPerson(){
        String userName = name.getText().toString();
        String passWord = password.getText().toString();
        String userNo = no.getText().toString();
        MyPerson myPerson = new MyPerson(userName,passWord,userNo,null,null);
        return myPerson;
    }

    private void showResponseData(final String responseData){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(responseData);
            }
        });
    }
    public void parseJSONwithGSON(String param) throws InterruptedException {
        OkHttpClient okHttpClient  = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        MyPerson myPerson = doPerson();
        Gson gson = new Gson();
        String json = gson.toJson(myPerson);
        showResponseData(json);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);
        Request request = null;
        if ("register".equals(param)) {
            request = new Request.Builder()
                    .url("http://10.138.106.105:8080/json/registers")//请求的url
                    .post(requestBody)
                    .build();
        }else if ("query".equals(param)){
            request = new Request.Builder()
                    .url(Constant.MYPERSON_QUERY)//请求的url
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
                Gson gson = new Gson();
                MyPerson myPerson1 = gson.fromJson(responseData,MyPerson.class);
                String showdata = "姓名： "+myPerson1.getName()+"\n学号： "+myPerson1.getNo()+"\n注册日期： "+myPerson1.getDate()
                        +"\nmac地址： "+myPerson1.getMac();
                showResponseData(showdata);
            }
        });

    }

    /**
     * 上传图片
     * @param imagePath
     */
    private void uploadImage(String imagePath) {
        new NetworkTask().execute(imagePath);
    }

    /**
     * 访问网络AsyncTask,访问网络在子线程进行并返回主线程通知访问的结果
     */
    class NetworkTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return doPost(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(!"error".equals(result)) {
                Log.i(TAG, "图片地址 " + Constant.BASE_URL + result);
                Glide.with(MainActivity.this)
                        .load(Constant.BASE_URL + result)
                        .into(imageView);
            }
        }
    }

    private String doPost(String imagePath) {
        OkHttpClient mOkHttpClient = new OkHttpClient();

        String result = "error";
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 这里演示添加用户ID
        MyPerson myPerson = doPerson();
        builder.setType(MediaType.parse("multipart/form-data;charset=utf-8"));
        builder.addFormDataPart("name", myPerson.getName());
        builder.addFormDataPart("password", myPerson.getPassword());
        builder.addFormDataPart("no", myPerson.getNo());
        builder.addFormDataPart("image", imagePath,
                RequestBody.create(MediaType.parse("image/jpeg"), new File(imagePath)));

        RequestBody requestBody = builder.build();

        Request.Builder reqBuilder = new Request.Builder();

        Request request = reqBuilder
                .url(Constant.UPLOAD_URL)
                .post(requestBody)
                .build();

        Log.d(TAG, "请求地址 " + Constant.UPLOAD_URL);
        try{
            Response response = mOkHttpClient.newCall(request).execute();
            Log.d(TAG, "响应码 " + response.code());
            if (response.isSuccessful()) {
                String resultValue = response.body().string();
                Gson gson = new Gson();
                MyPerson myPerson1 = gson.fromJson(resultValue,MyPerson.class);
                String showdata = "姓名： "+myPerson1.getName()+"\n学号： "+myPerson1.getNo()+"\n注册日期： "+myPerson1.getDate()
                        +"\nmac地址： "+myPerson1.getMac();
                showResponseData(showdata);
                return myPerson1.getImg();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

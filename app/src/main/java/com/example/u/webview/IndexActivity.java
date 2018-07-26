package com.example.u.webview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by U on 2018/5/19.
 */

public class IndexActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base);
    }
    public void doClick(View view){
        switch (view.getId()){
            case R.id.btn1:
                Intent intent1 = new Intent(IndexActivity.this,UserActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn2:
                Intent intent2 = new Intent(IndexActivity.this,MainActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn3:
                Intent intent3 = new Intent(IndexActivity.this,BookActivity.class);
                startActivity(intent3);
                break;
        }
    }
}

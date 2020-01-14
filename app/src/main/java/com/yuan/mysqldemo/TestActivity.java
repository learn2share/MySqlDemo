package com.yuan.mysqldemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findViewById(R.id.button1).setOnClickListener(this::onClick);
        findViewById(R.id.button2).setOnClickListener(this::onClick);
        findViewById(R.id.button3).setOnClickListener(this::onClick);
        findViewById(R.id.button4).setOnClickListener(this::onClick);
        findViewById(R.id.button5).setOnClickListener(this::onClick);
        findViewById(R.id.button6).setOnClickListener(this::onClick);
        findViewById(R.id.button7).setOnClickListener(this::onClick);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button1:
                startActivity(new Intent(this,LoginActivity.class));
                break;
            case R.id.button2:
                startActivity(new Intent(this,UserActivity.class));
                break;
            case R.id.button3:
                startActivity(new Intent(this,UserInfoManageActivity.class));
                break;
            case R.id.button4:
                startActivity(new Intent(this,AdduserActivity.class));
                break;
            case R.id.button5:
                startActivity(new Intent(this,AssetManagerActivity.class));
                break;
            case R.id.button6:
                startActivity(new Intent(this,AssetInActivity.class));
                break;
            case R.id.button7:
                startActivity(new Intent(this,AssetPandianActivity.class));
                break;
        }
    }
}

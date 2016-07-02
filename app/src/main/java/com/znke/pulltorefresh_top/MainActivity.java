package com.znke.pulltorefresh_top;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button scrollViewBtn = (Button) findViewById(R.id.scrollViewBtn);
        Button pullRefreshScrollViewBtn = (Button) findViewById(R.id.pullToRefreshScrollViewBtn);
        scrollViewBtn.setOnClickListener(this);
        pullRefreshScrollViewBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.scrollViewBtn:
                intent.setClass(this, ScrollViewActivity.class);
                break;
            case R.id.pullToRefreshScrollViewBtn:
                intent.setClass(this, PullToRefreshScrollViewActivity.class);
                break;
        }
        startActivity(intent);
    }
}
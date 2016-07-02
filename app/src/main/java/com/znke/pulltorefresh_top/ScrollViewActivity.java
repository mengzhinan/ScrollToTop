package com.znke.pulltorefresh_top;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.znke.pulltorefresh_top.tool.ToTopImageView;

public class ScrollViewActivity extends AppCompatActivity {
    private ScrollView mScrollView;
    private ToTopImageView imageView_to_top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view);
        imageView_to_top = (ToTopImageView) findViewById(R.id.imageView_to_top);

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        /*
        //方法1，需要自定义ScrollView，暴漏出onScrollChanged方法
        mScrollView.setOnMyScrollListener(new ToTopScrollView.OnMyScrollListener() {
            @Override
            public void onScrollChanged(int x, int y, int oldx, int oldy) {
                imageView_to_top.tellMe(mScrollView);
            }
        });*/
        //方法2，(不需要自定义ScrollView)
        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        imageView_to_top.tellMe(mScrollView);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        imageView_to_top.clearCallBacks();
        super.onDestroy();
    }
}
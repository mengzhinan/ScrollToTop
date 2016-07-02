package com.znke.pulltorefresh_top;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.znke.pulltorefresh_top.tool.ToTopImageView;

public class PullToRefreshScrollViewActivity extends AppCompatActivity {
    private PullToRefreshScrollView mScrollView;
    private ToTopImageView imageView_to_top;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_to_refresh_scroll_view);

        imageView_to_top = (ToTopImageView) findViewById(R.id.imageView_to_top);
        imageView_to_top.setLimitHeight(800);
        mScrollView = (PullToRefreshScrollView) findViewById(R.id.scrollView);
        final ScrollView scrollView = mScrollView.getRefreshableView();
        //mScrollView.setOnTouchListener();  无效
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        imageView_to_top.tellMe(scrollView);
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
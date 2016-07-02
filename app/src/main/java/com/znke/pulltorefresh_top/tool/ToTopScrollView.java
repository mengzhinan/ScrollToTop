package com.znke.pulltorefresh_top.tool;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * @DateTime: 2016-06-30 11:58
 * @Author: duke
 * @Deacription: 解决scrollview不提供监听问题
 */
public class ToTopScrollView extends ScrollView {
    private OnMyScrollListener onMyScrollListener;

    public void setOnMyScrollListener(OnMyScrollListener onMyScrollListener) {
        this.onMyScrollListener = onMyScrollListener;
    }

    public ToTopScrollView(Context context) {
        this(context, null);
    }

    public ToTopScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(onMyScrollListener != null)
            onMyScrollListener.onScrollChanged(l,t,oldl,oldt);
    }

    public interface OnMyScrollListener{
        void onScrollChanged(int x, int y, int oldx, int oldy);
    }
}
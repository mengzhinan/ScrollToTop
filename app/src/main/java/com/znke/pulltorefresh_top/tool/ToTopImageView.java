package com.znke.pulltorefresh_top.tool;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import java.util.Vector;

/**
 * @DateTime: 2016-06-30 10:31
 * @Author: duke
 * @Deacription: 滚动到top按钮功能
 */
public class ToTopImageView extends ImageView {
    //当前view背景资源
    private int backgroundResId;
    private Bitmap backgroundBitmap;

    //当前view需要显示的零界值
    private int limitHeight = 500;

    /**
     * 便于线程拿到先后的值对比，判定scroll是否停止
     */
    private float endScrollX;//记录上一次保留的卷去的值
    private float endScrollY;

    //待滚动的容器
    private View targetView;

    //当前view出现和隐藏的动画
    private ObjectAnimator visibleAnimation;
    private ObjectAnimator goneAnimation;

    //targetView滚动到top时的动画
    private ValueAnimator targetAnimation;

    //当前view是否需要出现和隐藏动画
    private boolean needVisibleAnimation = true;
    private boolean needGoneAnimation = true;

    //当前view显示与否的状态
    private boolean thisStateVisible = false;

    //扫描target滚动停止线程是否正在运行
    private boolean isStarting = false;
    //扫描线程是否需要停止，当Activity销毁时，避免OOM
    private boolean isQuit = false;

    //扫描线程
    private ScanThread scanThread;

    //handler发出的延时任务队列
    private Vector<MyCallback> callbacks;
    private Handler mHandler = new Handler();

    //to top动画的(时间和变化率)
    private int toTopMillisecond = 500;
    private ToTopAnimationRate toTopRate = ToTopAnimationRate.Accelerate_Decelerate_Interpolator;


    public void setNeedGoneAnimation(boolean needGoneAnimation) {
        this.needGoneAnimation = needGoneAnimation;
    }

    public void setNeedVisibleAnimation(boolean needVisibleAnimation) {
        this.needVisibleAnimation = needVisibleAnimation;
    }

    /**
     * 设置极限高度(即，超过那个值后，当前view开始显示)
     *
     * @param limitHeight
     */
    public void setLimitHeight(int limitHeight) {
        if (limitHeight < 0)
            throw new IllegalArgumentException();
        else
            this.limitHeight = limitHeight;
    }

    /**
     * 获取待监控的view对象
     * 实时调起线程，监控是否scroll停止，来判断是否需要显示imageView
     * @param targetView 需要监控的对象
     */
    public void tellMe(View targetView) {
        if (targetView == null)
            throw new IllegalArgumentException("please set targetView who to scrollTo");
        if (this.targetView == null)
            this.targetView = targetView;
        if (!isStarting) {
            new Thread(scanThread).start();
        }
    }

    /**
     * 设置背景图片资源
     * 或调用 setToTopImageBitmap(Bitmap backgroundBitmap)
     *
     * @param backgroundResId
     */
    public void setToTopBackgroundResource(int backgroundResId) {
        this.backgroundResId = backgroundResId;
        if (this.backgroundResId > 0) {
            this.setBackgroundResource(this.backgroundResId);
        }
    }

    /**
     * 设置背景图片bitmap
     * 或调用 setToTopBackgroundResource(int backgroundResId)
     *
     * @param backgroundBitmap
     */
    public void setToTopImageBitmap(Bitmap backgroundBitmap) {
        this.backgroundBitmap = backgroundBitmap;
        if (this.backgroundBitmap != null) {
            this.setImageBitmap(backgroundBitmap);
        }
    }

    public void setToTopAnimation(ToTopAnimationRate rate) {
        this.toTopRate = rate;
    }

    public void setToTopDuration(int millisecond) {
        this.toTopMillisecond = millisecond;
    }

    public ToTopImageView(Context context) {
        this(context, null);
    }

    public ToTopImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //初始化
    private void init() {
        //默认隐藏
        setVisibility(GONE);
        visibleAnimation = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
        visibleAnimation.setDuration(300);
        visibleAnimation.setInterpolator(new AccelerateInterpolator());
        visibleAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(VISIBLE);
                setAlpha(0f);
            }
        });
        goneAnimation = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
        goneAnimation.setDuration(300);
        goneAnimation.setInterpolator(new AccelerateInterpolator());
        goneAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }
        });
        callbacks = new Vector<>();
        scanThread = new ScanThread(callbacks);
    }

    private void visible() {
        if (!needVisibleAnimation) {
            this.setVisibility(VISIBLE);
        } else {
            visibleAnimation.start();
        }
        thisStateVisible = true;
    }

    private void gone() {
        if (!needGoneAnimation) {
            this.setVisibility(GONE);
        } else {
            goneAnimation.start();
        }
        thisStateVisible = false;
    }

    /**
     * 退出，终止扫描线程
     */
    public void clearCallBacks() {
        this.isQuit = true;
    }

    private class MyCallback implements Runnable {
        @Override
        public void run() {
            /**
             * 获取实时的卷动值，不要传递scroll值给我
             */
            endScrollX = targetView.getScrollX();
            int scrollY = targetView.getScrollY();
            if (endScrollY != scrollY) {
                endScrollY = scrollY;
            } else {
                if (endScrollY >= limitHeight) {
                    if (!thisStateVisible)
                        visible();
                } else {
                    if (thisStateVisible)
                        gone();
                }
                /**
                 * 已判定，卷动停止，显示或隐藏当前view已完成
                 * 退出监控scroll线程
                 */
                clearCallBacks();
            }
        }
    }

    private class ScanThread implements Runnable {
        private Vector<MyCallback> callbacks;

        public ScanThread(Vector<MyCallback> callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public void run() {
            isStarting = true;
            while (!isQuit) {
                try {
                    Thread.sleep(100);
                    MyCallback callback = new MyCallback();
                    callbacks.add(callback);
                    mHandler.postDelayed(callback, 100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //线程退出，清空任务
            int size = callbacks.size();
            for (int i = 0; i < size; i++) {
                mHandler.removeCallbacks(callbacks.get(i));
            }
            //恢复默认值
            isQuit = false;
            isStarting = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                //target卷动逻辑
                if (this.toTopRate == ToTopAnimationRate.None) {
                    targetView.scrollTo(0, 0);
                } else {
                    targetAnimation = ValueAnimator.ofFloat(endScrollY, 0);
                    targetAnimation.setDuration(toTopMillisecond);
                    if (this.toTopRate == ToTopAnimationRate.Linear_Interpolator) {
                        targetAnimation.setInterpolator(new LinearInterpolator());
                    } else if (this.toTopRate == ToTopAnimationRate.Accelerate_Decelerate_Interpolator) {
                        targetAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    } else if (this.toTopRate == ToTopAnimationRate.Accelerate_Interpolator) {
                        targetAnimation.setInterpolator(new AccelerateInterpolator());
                    } else if (this.toTopRate == ToTopAnimationRate.Anticipate_Interpolator) {
                        targetAnimation.setInterpolator(new AnticipateInterpolator());
                    } else if (this.toTopRate == ToTopAnimationRate.Anticipate_Overshoot_Interpolator) {
                        targetAnimation.setInterpolator(new AnticipateOvershootInterpolator());
                    } else if (this.toTopRate == ToTopAnimationRate.Bounce_Interpolator) {
                        targetAnimation.setInterpolator(new BounceInterpolator());
                    } else if (this.toTopRate == ToTopAnimationRate.Cycle_Interpolator) {
                        targetAnimation.setInterpolator(new CycleInterpolator(1));
                    } else if (this.toTopRate == ToTopAnimationRate.Decelerate_Interpolator) {
                        targetAnimation.setInterpolator(new DecelerateInterpolator());
                    } else if (this.toTopRate == ToTopAnimationRate.Overshoot_Interpolator) {
                        targetAnimation.setInterpolator(new OvershootInterpolator());
                    }
                    targetAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float val = (float) animation.getAnimatedValue();
                            targetView.setScrollY((int) val);
                            if (val < limitHeight) {
                                //手动隐藏当前view，此时的卷动没有开启线程监控
                                gone();
                            }
                        }
                    });
                    targetAnimation.start();
                }
                break;
        }
        return true;
    }

    public enum ToTopAnimationRate {
        Accelerate_Decelerate_Interpolator, //在动画开始与结束的地方速率改变比较慢，在中间的时候加速
        Accelerate_Interpolator,  //在动画开始的地方速率改变比较慢，然后开始加速
        Anticipate_Interpolator, //开始的时候向后然后向前甩
        Anticipate_Overshoot_Interpolator, //开始的时候向后然后向前甩一定值后返回最后的值
        Bounce_Interpolator, //动画结束的时候弹起
        Cycle_Interpolator, //动画循环播放特定的次数，速率改变沿着正弦曲线
        Decelerate_Interpolator, //在动画开始的地方快然后慢
        Linear_Interpolator,   //以常量速率改变
        Overshoot_Interpolator, //向前甩一定值后再回到原来位置
        None                    //无
    }
}
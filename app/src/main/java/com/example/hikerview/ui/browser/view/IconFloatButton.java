package com.example.hikerview.ui.browser.view;

/**
 * 作者：By 15968
 * 日期：On 2021/6/27
 * 时间：At 21:31
 */


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.example.hikerview.ui.ActivityManager;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.ScreenUtil;


public class IconFloatButton extends CardView {

    /**
     * View的宽高
     */
    private int width;
    private int height;

    /**
     * 触摸点相对于View的坐标
     */
    private float touchX;
    private float touchY;

    /**
     * x,y坐标的纠正值
     * 考虑到一些异性屏和非标准的显示区域
     */
    int xCorrection = 0;
    int yCorrection = 0;

    public void setxCorrection(int xCorrection) {
        this.xCorrection = xCorrection;
    }

    public void setyCorrection(int yCorrection) {
        this.yCorrection = yCorrection;
    }

    /**
     * 屏幕的宽高，默认是1024 * 600的屏幕高宽
     * 在使用该类的时候最好设置该值为正确的显示区域宽高
     */
    private int screenWidth = 1024;
    private int screenHeight = 600;
    private boolean canDrag = false;

    private final static int FADE_OUT = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_OUT:
                    ObjectAnimator.ofFloat(IconFloatButton.this, "alpha",
                            1.0f, ASSIST_TOUCH_VIEW_ALPHA_RATE)
                            .setDuration(ANIMATION_DURATION)
                            .start();
                    break;
                default:
                    break;
            }
        }
    };

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public void show() {
        marginMin = DisplayUtil.dpToPx(getContext(), 8);
        viewWidth = DisplayUtil.dpToPx(getContext(), 32);
        setScreenHeight(ScreenUtil.getScreenHeight(ActivityManager.getInstance().getCurrentActivity()));
        setScreenWidth(ScreenUtil.getScreenWidth(ActivityManager.getInstance().getCurrentActivity()));
        setVisibility(VISIBLE);
        layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        int fr = PreferenceMgr.getInt(getContext(), "fr", marginMin);
        int ft = PreferenceMgr.getInt(getContext(), "ft", marginMin);
        layoutParams.rightMargin = fr;
        layoutParams.topMargin = ft;
        check();
        setLayoutParams(layoutParams);
        ObjectAnimator.ofFloat(IconFloatButton.this, "alpha",
                1.0f, ASSIST_TOUCH_VIEW_ALPHA_RATE)
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    public void hide() {
        setVisibility(GONE);
    }

    public IconFloatButton(Context context) {
        super(context);
    }

    public IconFloatButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
    }

    private float mTouchStartX;
    private float mTouchStartY;
    private static final float TOLERANCE_RANGE = 18.0f;
    private static final float ASSIST_TOUCH_VIEW_ALPHA_RATE = 0.6f;
    private static final int ANIMATION_DURATION = 110;
    private FrameLayout.LayoutParams layoutParams;
    private int marginMin = 0;
    private int viewWidth = 0;
    private long touchTime = 0;

    private void moveMargin(float deltaX, float deltaY) {
        layoutParams.rightMargin = (int) (layoutParams.rightMargin - deltaX);
        layoutParams.topMargin = (int) (layoutParams.topMargin + deltaY);
        check();
        setLayoutParams(layoutParams);
    }

    private void check() {
        int maxW = screenWidth - marginMin - viewWidth;
        if (layoutParams.rightMargin > maxW) {
            layoutParams.rightMargin = maxW;
        }
        if (layoutParams.rightMargin < marginMin) {
            layoutParams.rightMargin = marginMin;
        }

        int maxH = screenHeight - marginMin - viewWidth;
        if (layoutParams.topMargin > maxH) {
            layoutParams.topMargin = maxH;
        }
        if (layoutParams.topMargin < marginMin) {
            layoutParams.topMargin = marginMin;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setAlpha(1.0f);
                clearAnimation();
                mHandler.removeMessages(FADE_OUT);
                touchX = event.getX();
                touchY = event.getY();
                mTouchStartX = event.getRawX();
                mTouchStartY = event.getRawY();
                touchTime = System.currentTimeMillis();
                final long snapshot = touchTime;
                postDelayed(() -> {
                    if (snapshot == touchTime) {
                        //触发长按
                        try {
                            Vibrator vib = (Vibrator) ActivityManager.getInstance().getCurrentActivity().getSystemService(Service.VIBRATOR_SERVICE);
                            if (vib != null) {
                                vib.vibrate(70);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        canDrag = true;
                    }
                }, 500);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (canDrag) {
                    setAlpha(1.0f);
                    float deltaX = event.getX() - touchX;
                    float deltaY = event.getY() - touchY;
                    moveMargin(deltaX, deltaY);
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                touchTime = 0;
                canDrag = false;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                //这里做动画贴边效果
                int halfOfScreenWidth = screenWidth / 2;
                ValueAnimator anim;
                if (layoutParams.rightMargin > halfOfScreenWidth) {
                    anim = ValueAnimator.ofInt(layoutParams.rightMargin, screenWidth - marginMin - viewWidth);
                } else {
                    anim = ValueAnimator.ofInt(layoutParams.rightMargin, marginMin);
                }
                anim.setDuration(250);
                anim.addUpdateListener(animation -> {
                    layoutParams.rightMargin = (int) (Integer) animation.getAnimatedValue();
                    setLayoutParams(layoutParams);
                });
                anim.start();
                mHandler.sendEmptyMessageDelayed(FADE_OUT, 1000);
                touchX = 0;
                touchY = 0;
                float mTouchEndX = event.getRawX();
                float mTouchEndY = event.getRawY();
                if (Math.abs(mTouchEndX - mTouchStartX) < TOLERANCE_RANGE
                        && Math.abs(mTouchEndY - mTouchStartY) < TOLERANCE_RANGE) {
                    performClick();
                    return true;
                }
                postDelayed(() -> {
                    //记忆位置
                    PreferenceMgr.put(getContext(), "fr", layoutParams.rightMargin);
                    PreferenceMgr.put(getContext(), "ft", layoutParams.topMargin);
                }, 300);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
package com.example.hikerview.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * 作者：By hdy
 * 日期：On 2019/6/1
 * 时间：At 17:13
 */
public class RelativeListenLayout extends RelativeLayout {
    public void setOnInterceptTouchEventListener(OnInterceptTouchEventListener onInterceptTouchEventListener) {
        this.onInterceptTouchEventListener = onInterceptTouchEventListener;
    }

    public OnInterceptTouchEventListener getOnInterceptTouchEventListener() {
        return onInterceptTouchEventListener;
    }

    private OnInterceptTouchEventListener onInterceptTouchEventListener;
    private OnTouchEventListener onTouchEventListener;

    public RelativeListenLayout(Context context) {
        super(context);
    }

    public RelativeListenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (onInterceptTouchEventListener != null) {
            return onInterceptTouchEventListener.intercept(ev);
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (onTouchEventListener != null) {
            return onTouchEventListener.touch(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    public void setOnTouchEventListener(OnTouchEventListener onTouchEventListener) {
        this.onTouchEventListener = onTouchEventListener;
    }

    public interface OnInterceptTouchEventListener {
        boolean intercept(MotionEvent event);
    }

    public interface OnTouchEventListener {
        boolean touch(MotionEvent event);
    }
}

package com.example.hikerview.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.example.hikerview.utils.DisplayUtil;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2020/5/4
 * 时间：At 11:48
 */
public class EnhanceViewPager extends ViewPager {
    private float startX, startY;
    private int height;
    private float distanceX;
    private boolean childScrolling = false;

    public EnhanceViewPager(@NonNull Context context) {
        super(context);
    }

    public EnhanceViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        height = DisplayUtil.dpToPx(context, 150);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                startX = ev.getX();
                startY = ev.getY();
                childScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //来到新的坐标
                float endX = ev.getX();
                float endY = ev.getY();
                //计算偏移量
                distanceX = endX - startX;
                float distanceY = endY - startY;
                Timber.d("distanceX:%s", distanceX);
                //判断滑动方向
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    //水平方向滑动
//                   当滑动到ViewPager的第0个页面，并且是从左到右滑动
                    if (getCurrentItem() == 0 && distanceX > 0 && endY > height) {
                        getParent().requestDisallowInterceptTouchEvent(childScrolling);
                    }
//                   ，当滑动到ViewPager的最后一个页面，并且是从右到左滑动
                    else if ((getCurrentItem() == (getAdapter().getCount() - 1)) && distanceX < 0) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
//                    其他,中间部分
                    else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                } else {
                    //竖直方向滑动
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        //避免Android系统原因闪退 pointerIndex out of range
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept && getCurrentItem() == 0) {
            childScrolling = true;
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void dealChildScroll() {
        if (getCurrentItem() != 0) {
            //中间页
            getParent().requestDisallowInterceptTouchEvent(true);
        } else {
            if (distanceX > 0) {
                //从左到右滑动
                getParent().requestDisallowInterceptTouchEvent(false);
            } else {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        try {
//            float endY = ev.getY();
//            boolean sp = super.onInterceptTouchEvent(ev);
//            return endY > height && sp;
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        float endY = ev.getY();
//        return endY > height && super.onTouchEvent(ev);
//    }

    /**
     * 避免Android系统原因闪退
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 避免Android系统原因闪退
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

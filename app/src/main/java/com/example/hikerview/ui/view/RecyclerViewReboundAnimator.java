package com.example.hikerview.ui.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：By 15968
 * 日期：On 2020/2/8
 * 时间：At 14:02
 */
public class RecyclerViewReboundAnimator {
    private static final int INIT_DELAY = 200;

    private int order = 1;
    private int mWidth;
    private boolean mFirstViewInit = true;

    public int getmLastPosition() {
        return mLastPosition;
    }

    private int mLastPosition = -1;
    private int mStartDelay;
    private boolean open = true;

    public RecyclerViewReboundAnimator(RecyclerView recyclerView) {
        mWidth = recyclerView.getResources().getDisplayMetrics().widthPixels / 2;
        mStartDelay = INIT_DELAY;
    }

    public void onCreateViewHolder(View item, int colum) {
        if (mFirstViewInit) {
            slideInBottom(item, mStartDelay);

            if (order % colum == 0) {
                mStartDelay += 30;
                order = 1;
            } else {
                order++;
            }
        }
    }

    public void onBindViewHolder(View item, int position) {
        if (!mFirstViewInit && position > mLastPosition) {
            slideInBottom(item, 0);
            mLastPosition = position;
        }
    }

    private void slideInBottom(final View item,
                               final int delay) {
        if (!open) {
            return;
        }
        item.setTranslationY(mWidth);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mWidth);
        valueAnimator.setInterpolator(new SpringInterpolator());
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFirstViewInit = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.addUpdateListener(animation -> {
            float val = (mWidth - (float) animation.getAnimatedValue());
            item.setTranslationY(val);
        });
        valueAnimator.setDuration(1000);
        valueAnimator.setStartDelay(delay);
        valueAnimator.start();

    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
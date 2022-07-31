package com.example.hikerview.ui.view.animate;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.MotionEvent;
import android.view.View;

/**
 * 作者：By 15968
 * 日期：On 2021/7/29
 * 时间：At 20:22
 */

public class AnimateTogetherUtils {

    private static void animationScale(View v) {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(v, "ScaleX", 1.0F, 0.8F, 1.1F, 1.0F);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(v, "ScaleY", 1.0F, 0.8F, 1.1F, 1.0F);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorX, animatorY);
        animatorSet.setDuration(400);
        animatorSet.start();
    }

    public static class AnimateOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                animationScale(v);
            }
            return false;
        }
    }

    public static void scaleTouch(View view) {
        if (view == null) {
            return;
        }
        view.setOnTouchListener(new AnimateOnTouchListener());
    }

    public static void scaleNow(View view) {
        if (view == null) {
            return;
        }
        animationScale(view);
    }

    public static void alphaNow(View view) {
        alphaNow(view, null);
    }

    public static void alphaNow(View view, Runnable interceptor) {
        if (view == null) {
            return;
        }
        animationAlpha(view, interceptor);
    }

    private static void animationAlpha(View v, Runnable interceptor) {
        if (interceptor == null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", 1.0F, 0.8F, 1.0F);
            animator.setDuration(300);
            animator.start();
        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", 1.0F, 0.8F);
            animator.setDuration(150);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    interceptor.run();
                    ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", 0.8F, 1.0F);
                    animator.setDuration(150);
                    animator.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }
} 
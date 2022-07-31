package com.example.hikerview.ui.browser.util;

import android.animation.Animator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Interpolator;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

/**
 * 作者：By 15968
 * 日期：On 2019/10/13
 * 时间：At 13:44
 */
public class BottomBarAnimateUtil {
    private static boolean isAnimate;

    private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();

    //隐藏时的动画
    public static void hide(final View view, int viewY) {
        if(isAnimate){
            return;
        }
        ViewPropertyAnimator animator = view.animate().translationY(viewY).setInterpolator(INTERPOLATOR).setDuration(400);

        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.GONE);
                isAnimate = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                show(view, viewY);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }

    //显示时的动画
    public static void show(final View view, int viewY) {
        if(isAnimate){
            return;
        }
        ViewPropertyAnimator animator = view.animate().translationY(0).setInterpolator(INTERPOLATOR).setDuration(300);
        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setVisibility(View.VISIBLE);
                isAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimate = false;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                hide(view, viewY);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }
}

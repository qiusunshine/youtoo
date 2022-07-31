package com.example.hikerview.ui.search;

import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.lxj.xpopup.animator.PopupAnimator;
import com.lxj.xpopup.enums.PopupAnimation;

/**
 * 作者：By 15968
 * 日期：On 2021/1/16
 * 时间：At 1:00
 */

public class MyTranslateAnimator extends PopupAnimator {
    public float startTranslationX, startTranslationY;
    public float endTranslationX, endTranslationY;
    public boolean hasInit = false;
    public MyTranslateAnimator(View target, int animationDuration, PopupAnimation popupAnimation) {
        super(target, animationDuration, popupAnimation);
    }

    @Override
    public void initAnimator() {
        if(!hasInit){
            endTranslationX = targetView.getTranslationX();
            endTranslationY = targetView.getTranslationY();
            // 设置起始坐标
            applyTranslation();
            startTranslationX = targetView.getTranslationX();
            startTranslationY = targetView.getTranslationY();
            Log.e("tag", "endTranslationY: " + endTranslationY  + "  startTranslationY: "+startTranslationY
                    + "   duration: " + animationDuration);
        }
    }

    private void applyTranslation() {
        switch (popupAnimation) {
            case TranslateFromLeft:
                targetView.setTranslationX(-targetView.getRight());
                break;
            case TranslateFromTop:
                targetView.setTranslationY(-targetView.getBottom());
                break;
            case TranslateFromRight:
                targetView.setTranslationX(((View) targetView.getParent()).getMeasuredWidth() - targetView.getLeft());
                break;
            case TranslateFromBottom:
                targetView.setTranslationY((((View) targetView.getParent()).getMeasuredHeight() - targetView.getTop()) / 3f);
                break;
        }
    }

    @Override
    public void animateShow() {
        ViewPropertyAnimator animator = null;
        switch (popupAnimation) {
            case TranslateFromLeft:
            case TranslateFromRight:
                animator = targetView.animate().translationX(endTranslationX);
                break;
            case TranslateFromTop:
            case TranslateFromBottom:
                animator = targetView.animate().translationY(endTranslationY);
                break;
        }
        if (animator != null) animator.setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(animationDuration)
                .withLayer()
                .start();
    }

    @Override
    public void animateDismiss() {
        if (animating) return;
        ViewPropertyAnimator animator = null;
        switch (popupAnimation) {
            case TranslateFromLeft:
                startTranslationX = -targetView.getRight();
                animator = targetView.animate().translationX(startTranslationX);
                break;
            case TranslateFromTop:
                startTranslationY = -targetView.getBottom();
                animator = targetView.animate().translationY(startTranslationY);
                break;
            case TranslateFromRight:
                startTranslationX = ((View) targetView.getParent()).getMeasuredWidth() - targetView.getLeft();
                animator = targetView.animate().translationX(startTranslationX);
                break;
            case TranslateFromBottom:
                startTranslationY = ((View) targetView.getParent()).getMeasuredHeight() - targetView.getTop();
                animator = targetView.animate().translationY(startTranslationY);
                break;
        }
        if (animator != null)
            observerAnimator(animator.setInterpolator(new FastOutSlowInInterpolator())
                    .setDuration((long) (animationDuration * .8))
                    .withLayer())
                    .start();
    }
}

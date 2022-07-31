package com.example.hikerview.ui.view.plusmenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.example.hikerview.R;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.ScreenUtil;
import com.ms_square.etsyblur.BlurringView;

public class MoreWindow extends PopupWindow implements OnClickListener {

    private Activity mContext;
    private RelativeLayout layout;
    private ImageView close;
    private View bgView;
    private BlurringView blurringView;
    private OnClickListener onClickListener;
    private int mWidth;
    private int mHeight;
    private Handler mHandler = new Handler();

    public MoreWindow(Activity context) {
        mContext = context;
    }

    /**
     * 初始化
     *
     * @param view 要显示的模糊背景View,一般选择跟布局layout
     */
    public void init(View view, OnClickListener clickListener) {
        layout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.view_more_window, null);
        setContentView(layout);
        mWidth = ScreenUtil.getScreenSize(mContext)[0];
        mHeight = ScreenUtil.getScreenSize(mContext)[1];
        setWidth(mWidth);
        setHeight(mHeight);

        close = layout.findViewById(R.id.iv_close);

        blurringView = layout.findViewById(R.id.blurring_view);

        try {
            blurringView.blurredView(view);//模糊背景
        } catch (Exception e) {
            e.printStackTrace();
        }

        bgView = layout.findViewById(R.id.rel);
        setOutsideTouchable(true);
        setFocusable(true);
        setClippingEnabled(false);//使popupwindow全屏显示

        close.setOnClickListener(v -> {
            if (isShowing()) {
                closeAnimation(null);
            }
        });
        blurringView.setOnClickListener(v -> {
            if (isShowing()) {
                closeAnimation(null);
            }
        });
        this.onClickListener = clickListener;
    }

    /**
     * 显示window动画
     *
     * @param anchor
     */
    public void showMoreWindow(View anchor) {

        showAtLocation(anchor, Gravity.TOP | Gravity.START, 0, 0);
        mHandler.post(() -> {
            try {
                //圆形扩展的动画
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    int x = mWidth - DisplayUtil.dpToPx(mContext, 36);
                    int y = mHeight - DisplayUtil.dpToPx(mContext, 220);
                    Animator animator = ViewAnimationUtils.createCircularReveal(bgView, x,
                            y, 0, bgView.getHeight());
                    animator.setDuration(300);
                    animator.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        showAnimation(layout);

    }

    private void showAnimation(ViewGroup layout) {
        try {
            mHandler.post(() -> {
                //＋ 旋转动画
                close.animate().rotation(90).setDuration(400);
            });
            //菜单项弹出动画
            LinearLayout linearLayout1 = layout.findViewById(R.id.lin1);
            LinearLayout linearLayout2 = layout.findViewById(R.id.lin2);
            showAnimationForParent(linearLayout1);
            showAnimationForParent(linearLayout2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showAnimationForParent(ViewGroup parent) {
        try {
            //菜单项弹出动画
            for (int i = 0; i < parent.getChildCount(); i++) {
                final View child = parent.getChildAt(i);
                child.setOnClickListener(this);
                child.setVisibility(View.INVISIBLE);
                mHandler.postDelayed(() -> {
                    child.setVisibility(View.VISIBLE);
                    ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationX", 200, 0);
                    fadeAnim.setDuration(200);
                    KickBackAnimator kickAnimator = new KickBackAnimator();
                    kickAnimator.setDuration(150);
                    fadeAnim.setEvaluator(kickAnimator);
                    fadeAnim.start();
                }, i * 50 + 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 关闭window动画
     */
    private void closeAnimation(View clickView) {
        mHandler.post(() -> close.animate().rotation(-90).setDuration(400));
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int x = mWidth - DisplayUtil.dpToPx(mContext, 36);
                int y = mHeight - DisplayUtil.dpToPx(mContext, 220);
                Animator animator = ViewAnimationUtils.createCircularReveal(bgView, x,
                        y, bgView.getHeight(), 0);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        try {
                            if (clickView != null) {
                                onClickListener.onClick(clickView);
                            }
                            dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                animator.setDuration(300);
                animator.start();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 点击事件处理
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (isShowing()) {
            closeAnimation(v);
        }
    }

    float fromDpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }
}

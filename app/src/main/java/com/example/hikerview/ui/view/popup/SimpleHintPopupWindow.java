package com.example.hikerview.ui.view.popup;

/**
 * 作者：By 15968
 * 日期：On 2019/11/30
 * 时间：At 16:36
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.R;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.ScreenUtil;
import com.lxj.xpopup.util.XPopupUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhaoshuang on 16/8/29.
 * 弹出动画的popupwindow
 */
public class SimpleHintPopupWindow {

    private Activity activity;
    private boolean isShow;
    private ViewGroup rootView;
    private ViewGroup linearLayout;
    private FullScreenDialog dialog;

    private final int animDuration = 250;//动画执行时间
    private boolean isAniming;//动画是否在执行


    public SimpleHintPopupWindow(Activity activity, String[] contentList, Consumer<String> consumer) {
        List<View.OnClickListener> listeners = new ArrayList<>();
        for (String s : contentList) {
            View.OnClickListener listener = v -> consumer.accept(s);
            listeners.add(listener);
        }

        this.activity = activity;
        initLayout(new ArrayList<>(Arrays.asList(contentList)), listeners);
    }

    /**
     * @param contentList 点击item的内容文字
     * @param clickList   点击item的事件
     *                    文字和click事件的list是对应绑定的
     */
    public SimpleHintPopupWindow(Activity activity, List<String> contentList, List<View.OnClickListener> clickList) {

        this.activity = activity;

        initLayout(contentList, clickList);
    }

    /**
     * @param contentList 点击item内容的文字
     * @param clickList   点击item的事件
     */
    public void initLayout(List<String> contentList, List<View.OnClickListener> clickList) {

        //这是根布局
        rootView = (ViewGroup) View.inflate(activity, R.layout.item_root_hintpopupwindow, null);
        linearLayout = rootView.findViewById(R.id.linearLayout);
        linearLayout.setBackground(XPopupUtils.createDrawable(activity.getResources().getColor(R.color.white), 15F));

        //格式化点击item, 将文字和click事件一一绑定上去
        List<View> list = new ArrayList<>();
        for (int x = 0; x < contentList.size(); x++) {
            View view = View.inflate(activity, R.layout.item_hint_popupwindow, null);
            TextView textView = view.findViewById(R.id.tv_content);
            View v_line = view.findViewById(R.id.v_line);
            textView.setText(contentList.get(x));
            linearLayout.addView(view);
            list.add(view);
            if (x == 0) {
                v_line.setVisibility(View.INVISIBLE);
            } else {
                v_line.setVisibility(View.VISIBLE);
            }
        }
        for (int x = 0; x < list.size(); x++) {
            int finalX = x;
            list.get(x).setOnClickListener(v -> {
                dismissPopupWindow();
                clickList.get(finalX).onClick(v);
            });
        }


        //当点击根布局时, 隐藏
        rootView.setOnClickListener(v -> dismissPopupWindow());

        rootView.setOnKeyListener((v, keyCode, event) -> {
            //如果是显示状态那么隐藏视图
            if (keyCode == KeyEvent.KEYCODE_BACK && isShow) dismissPopupWindow();
            return isShow;
        });
    }

    /**
     * 弹出选项弹窗
     *
     * @param locationView 默认在该view的下方弹出, 和popupWindow类似
     */
    public void showPopupWindow(View locationView) {
        Log.i("Log.i", "showPopupWindow: " + isAniming);
        try {
            //这个步骤是得到该view相对于屏幕的坐标, 注意不是相对于父布局哦!
            int[] arr = new int[2];
            locationView.getLocationOnScreen(arr);
            linearLayout.measure(0, 0);
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);//得到状态栏高度
            float x = arr[0] + locationView.getWidth() - linearLayout.getMeasuredWidth();
            float y = arr[1] - frame.top + locationView.getHeight();
            showPopupWindow(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 弹出选项弹窗
     *
     * @param locationView 默认在该view的下方弹出, 和popupWindow类似
     */
    public void showPopupWindowCenter(View locationView) {
        Log.i("Log.i", "showPopupWindow: " + isAniming);
        try {
            //这个步骤是得到该view相对于屏幕的坐标, 注意不是相对于父布局哦!
            int[] arr = new int[2];
            locationView.getLocationOnScreen(arr);
            linearLayout.measure(0, 0);
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);//得到状态栏高度
            float x = (float) ScreenUtil.getScreenWidth(activity) / 2 - (float) linearLayout.getMeasuredWidth() / 2;
            float y = arr[1] - frame.top + locationView.getHeight();
            showPopupWindow(x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 弹出选项弹窗
     */
    public void showPopupWindowCenter() {
        Log.i("Log.i", "showPopupWindow: " + isAniming);
        float x = (float) ScreenUtil.getScreenWidth(activity) / 2;
        float y = (float) ScreenUtil.getScreenHeight(activity) / 2;
        showPopupWindow(x, y);
    }

    public void showPopupWindow(float x, float y) {
        Log.i("Log.i", "showPopupWindow: " + isAniming);
        if (!isAniming) {
            isAniming = true;
            if (this.dialog == null) {
                this.dialog = (new FullScreenDialog(activity).setContent(rootView));
            }
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    View decorView = activity.getWindow().getDecorView();
                    int vis = decorView.getSystemUiVisibility();
                    vis |= 8192;

                    decorView.setSystemUiVisibility(vis);
                }
                linearLayout.measure(0, 0);
                if (linearLayout.getMeasuredWidth() + x > ScreenUtil.getScreenWidth(activity)) {
                    linearLayout.setX(ScreenUtil.getScreenWidth(activity) - linearLayout.getMeasuredWidth());
                } else {
                    int half = linearLayout.getMeasuredWidth() / 2;
                    if (half <= x) {
                        linearLayout.setX(x - half);
                    } else {
                        linearLayout.setX(0);
                    }
                }
                int dp20 = DisplayUtil.dpToPx(activity, 12);
                if (linearLayout.getMeasuredHeight() + y + dp20 > ScreenUtil.getScreenHeight(activity)) {
                    linearLayout.setY(ScreenUtil.getScreenHeight(activity) - linearLayout.getMeasuredHeight() - dp20);
                } else {
                    linearLayout.setY(y);
                }

                dialog.show();

                //这一步就是有回弹效果的弹出动画, 我用属性动画写的, 很简单
                showAnim(linearLayout, 0, 1, animDuration, true);

                //视图被弹出来时得到焦点, 否则就捕获不到Touch事件
                rootView.setFocusable(true);
                rootView.setFocusableInTouchMode(true);
                rootView.requestFocus();
                rootView.requestFocusFromTouch();
                isShow = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void dismissPopupWindow() {
        Log.i("Log.i", "dismissPopupWindow: " + isAniming);
        if (!isAniming) {
            isAniming = true;
            isShow = false;
            goneAnim(linearLayout, 0.95f, 1, animDuration / 3, true);
        }
    }

    public ViewGroup getLayout() {
        return linearLayout;
    }

    /**
     * popupwindow是否是显示状态
     */
    public boolean isShow() {
        return isShow;
    }

    private void alphaAnim(final View view, int start, int end, int duration) {

        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(duration);
        va.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setAlpha(value);
        });
        va.start();
    }

    private void showAnim(final View view, float start, final float end, int duration, final boolean isWhile) {

        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(duration);
        va.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setPivotX(view.getWidth());
            view.setPivotY(0);
            view.setScaleX(value);
            view.setScaleY(value);
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isWhile) {
                    showAnim(view, end, 0.95f, animDuration / 3, false);
                } else {
                    isAniming = false;
                }
            }
        });
        va.start();
    }

    private void goneAnim(final View view, float start, final float end, int duration, final boolean isWhile) {

        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(duration);
        va.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setPivotX(view.getWidth());
            view.setPivotY(0);
            view.setScaleX(value);
            view.setScaleY(value);
        });
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isWhile) {
                    alphaAnim(rootView, 1, 0, animDuration);
                    goneAnim(view, end, 0f, animDuration, false);
                } else {

                    dialog.dismiss();
                    isAniming = false;
                }
            }
        });
        va.start();
    }
}

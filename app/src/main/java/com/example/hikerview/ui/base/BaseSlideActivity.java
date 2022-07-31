package com.example.hikerview.ui.base;

import android.view.View;

import com.example.hikerview.R;

/**
 * 作者：By 15968
 * 日期：On 2021/1/23
 * 时间：At 22:07
 */

public abstract class BaseSlideActivity extends BaseActivity {

    //    protected MyShadowBgAnimator shadowBgAnimator;
    private boolean isFinished;
    private View bgView;

    protected abstract View getBackgroundView();


    @Override
    protected void initView() {
        bgView = getBackgroundView();
//        shadowBgAnimator = new MyShadowBgAnimator(bgView);
//        shadowBgAnimator.initAnimator();
        initView2();
    }


//    @Override
//    public void startActivity(Intent intent) {
//        try {
//            if (intent.getComponent() != null) {
//                Class c = Class.forName(intent.getComponent().getClassName());
//                if (c.getSuperclass() == BaseSlideActivity.class) {
//                    ActivityOptions compat = ActivityOptions.makeSceneTransitionAnimation(this);
//                    startActivity(new Intent(getContext(), c), compat.toBundle());
//                    return;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        super.startActivity(intent);
//    }

    protected abstract void initView2();


    @Override
    public void finish() {
        if (isFinished) {
            return;
        }
        isFinished = true;
        super.finish();
        overridePendingTransition(0, R.anim.slide_top_to_bottom);
    }
}

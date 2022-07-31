package com.example.hikerview.ui.base;

/**
 * 作者：By 15968
 * 日期：On 2021/1/9
 * 时间：At 22:13
 */

public abstract class BaseTransNavigationActivity extends BaseActivity {

    @Override
    protected void initView() {
        super.setTranslucentNavigation();
        initView2();
    }

    protected abstract void initView2();

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
}

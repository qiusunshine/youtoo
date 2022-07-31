package com.example.hikerview.ui.base;

public abstract class BaseStatusTransNavigationActivity extends BaseStatusActivity {

    @Override
    protected void initView() {
        super.setTranslucentNavigation();
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
}

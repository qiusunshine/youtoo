package com.example.hikerview.ui.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hikerview.R;
import com.example.hikerview.constants.PreferenceConstant;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.umeng.analytics.MobclickAgent;

public abstract class BaseTranslucentActivity extends AppCompatActivity {

    protected Bundle extraDataBundle;
    protected View nightView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initLayout(savedInstanceState));
        MyStatusBarUtil.setColor(this, getResources().getColor(R.color.white));
        if(SettingConfig.isNight){
            nightView = SettingConfig.changeNightMode(getContext(), nightView, SettingConfig.isNight);
        }
        extraDataBundle = getIntent().getBundleExtra("extraDataBundle");
        initView();
        //以下代码用于去除阴影
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(DisplayUtil.dpToPx(getContext(), 1) / 2);
        }
        initData(savedInstanceState);
    }

    protected void setTranslucentNavigation(){
        boolean useNotch = PreferenceMgr.getBoolean(getContext(), PreferenceConstant.KEY_useNotch, true);
        if (!useNotch) {
            return;
        }
        //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == android.R.id.home || super.onOptionsItemSelected(item);
    }

    /**
     * 初始化布局
     */
    protected abstract int initLayout(Bundle savedInstanceState);

    /**
     * 初始化布局以及View控件
     */
    protected abstract void initView();

    /**
     * 处理业务逻辑，状态恢复等操作
     *
     * @param savedInstanceState 鬼知道
     */
    protected abstract void initData(Bundle savedInstanceState);

    /**
     * 查找View
     *
     * @param id   控件的id
     * @param <VT> View类型
     * @return 鬼知道
     */
    protected <VT extends View> VT findView(@IdRes int id) {
        return (VT) findViewById(id);
    }

    protected Context getContext() {
        return this;
    }

    protected Activity getActivity() {
        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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
}

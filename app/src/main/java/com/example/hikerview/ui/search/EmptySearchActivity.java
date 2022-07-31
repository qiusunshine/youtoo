package com.example.hikerview.ui.search;

import android.content.Intent;
import android.os.Bundle;

import com.example.hikerview.R;
import com.example.hikerview.ui.base.BaseTranslucentActivity;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.lxj.xpopup.XPopup;

/**
 * 作者：By hdy
 * 日期：On 2018/11/16
 * 时间：At 10:51
 */

public class EmptySearchActivity extends BaseTranslucentActivity {
    private static final String TAG = "ResolveIntentActivity";

    @Override
    protected void onNewIntent(Intent intent) {
        finish();
        super.onNewIntent(intent);
    }

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activit_intent;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        MyStatusBarUtil.setColorNoTranslucent(this, getIntent().getIntExtra("color", getResources().getColor(R.color.white)));
        new XPopup.Builder(getContext())
                .autoOpenSoftInput(true)
                .moveUpToKeyboard(false)
                .asCustom(new GlobalSearchPopup(getContext()).with(getIntent(), this).setDismissTask(this::finish))
                .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.dd_mask_out_in, R.anim.dd_mask_out);
    }
}
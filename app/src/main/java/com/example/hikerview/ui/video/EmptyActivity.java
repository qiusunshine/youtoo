package com.example.hikerview.ui.video;

import android.content.Intent;
import android.os.Bundle;

import com.example.hikerview.R;
import com.example.hikerview.ui.base.BaseTranslucentActivity;

/**
 * 作者：By hdy
 * 日期：On 2018/11/16
 * 时间：At 10:51
 */

public class EmptyActivity extends BaseTranslucentActivity {
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
        finish();
    }
}
package com.example.hikerview.ui.base;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * 作者：By hdy
 * 日期：On 2017/10/31
 * 时间：At 15:01
 */
public abstract class BaseModel<T> {
    //数据请求参数
    protected Object[] mParams;
    private Context context;

    /**
     * 设置数据请求参数
     *
     * @param args 参数数组
     */
    public BaseModel<T> params(@Nullable Context context, Object... args) {
        this.context = context;
        mParams = args;
        return this;
    }

    public Context getContext() {
        return context;
    }

    // 添加Callback并执行数据请求
    // 具体的数据请求由子类实现
    public abstract void process(String actionType, BaseCallback<T> baseCallback);
}
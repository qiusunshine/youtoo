package com.example.hikerview.ui.browser.model;

import android.content.Context;

import com.example.hikerview.model.ViewCollection;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseModel;

/**
 * 作者：By 15968
 * 日期：On 2019/10/3
 * 时间：At 22:13
 */
public class ViewCollectionModel extends BaseModel<ViewCollection> {
    @Override
    public void process(String actionType, BaseCallback<ViewCollection> baseCallback) {

    }

    public void add(Context context, ViewCollection viewCollection) {
        viewCollection.save();
    }
}

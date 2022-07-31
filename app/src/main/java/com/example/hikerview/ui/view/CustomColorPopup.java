package com.example.hikerview.ui.view;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.lxj.xpopup.core.CenterPopupView;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class CustomColorPopup extends CenterPopupView {
    private  OnColorSelect colorSelect;

    public CustomColorPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_color_pop;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        findViewById(R.id.color1_bg).setOnClickListener(this::select);
        findViewById(R.id.color2_bg).setOnClickListener(this::select);
        findViewById(R.id.color3_bg).setOnClickListener(this::select);
        findViewById(R.id.color4_bg).setOnClickListener(v -> select(v));
        findViewById(R.id.color5_bg).setOnClickListener(v -> select(v));
        findViewById(R.id.color6_bg).setOnClickListener(v -> select(v));
        findViewById(R.id.color7_bg).setOnClickListener(v -> select(v));
        findViewById(R.id.color8_bg).setOnClickListener(v -> select(v));
    }

    private void select(View v){
        colorSelect.select((String) v.getTag());
        dismiss();
    }

    public OnColorSelect getColorSelect() {
        return colorSelect;
    }

    public void setColorSelect(OnColorSelect colorSelect) {
        this.colorSelect = colorSelect;
    }

    public interface OnColorSelect{
        void select(String color);
    }
}

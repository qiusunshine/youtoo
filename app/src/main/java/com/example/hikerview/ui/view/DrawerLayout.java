package com.example.hikerview.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 作者：By 15968
 * 日期：On 2020/5/4
 * 时间：At 14:49
 */
public class DrawerLayout extends androidx.drawerlayout.widget.DrawerLayout {
    public DrawerLayout(@NonNull Context context) {
        super(context);
    }

    public DrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {

    }
}

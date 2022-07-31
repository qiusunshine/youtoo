package com.example.hikerview.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import com.example.hikerview.R;

/**
 * 作者：By 15968
 * 日期：On 2020/3/7
 * 时间：At 15:56
 */
public class WhiteEditText extends AppCompatEditText {
    public WhiteEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public WhiteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(context.getResources().getColor(R.color.white));
    }
}

package com.example.hikerview.ui.view;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class CustomCopyPopup extends BottomPopupView {
    private String title;
    private TextView codeBtn;
    private boolean showCode;
    private TextView textView;

    public CustomCopyPopup with(String title) {
        this.title = title;
        return this;
    }

    public CustomCopyPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_custom_copy_text;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        textView = findViewById(R.id.movie_one_title);
        codeBtn = findViewById(R.id.code);
        codeBtn.setOnClickListener(v -> {
            showCode = !showCode;
            if (showCode) {
                textView.setText(title);
                codeBtn.setTextColor(getContext().getResources().getColor(R.color.greenAction));
            } else {
                textView.setText(Html.fromHtml(title));
                codeBtn.setTextColor(getContext().getResources().getColor(R.color.black_666));
            }
        });
        if (StringUtil.isNotEmpty(title)) {
            textView.setText(Html.fromHtml(title));
        }
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }
}

package com.example.hikerview.ui.view;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.core.BottomPopupView;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class CustomBottomPopup extends BottomPopupView {

    private OnClickListener onClickListener;
    private String title;

    public CustomBottomPopup addOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public CustomBottomPopup(@NonNull Context context) {
        this(context, null);
    }

    public CustomBottomPopup(@NonNull Context context, String title) {
        super(context);
        this.title = title;
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_msg_bottom_pop;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        popupInfo.isClickThrough = true;
        findViewById(R.id.btn_show).setOnClickListener(v -> {
            onClickListener.onClick(v);
            dismiss();
        });
        if (StringUtil.isNotEmpty(title)) {
            ((TextView) findViewById(R.id.msg_text)).setText(title);
        }
        findViewById(R.id.dismiss).setOnClickListener(v -> {
            dismiss();
        });
    }
}

package com.example.hikerview.ui.view.popup;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.core.CenterPopupView;

/**
 * 作者：By 15968
 * 日期：On 2020/10/31
 * 时间：At 22:48
 */

public class ConfirmPopup extends CenterPopupView {

    private EditText titleEdit;
    private String title;
    private String titleHint;
    private OkListener okListener;
    private String okText, cancelText;

    public ConfirmPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_confirm_popup;
    }

    public ConfirmPopup bind(String title, String titleHint, OkListener okListener) {
        this.title = title;
        this.titleHint = titleHint;
        this.okListener = okListener;
        return this;
    }

    public ConfirmPopup setBtn(String okText, String cancelText) {
        this.okText = okText;
        this.cancelText = cancelText;
        return this;
    }


    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        TextView titleView = findViewById(R.id.title);
        titleEdit = findViewById(R.id.edit_title);
        titleView.setText(title);
        titleEdit.setHint(titleHint);

        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            dismiss();
            okListener.cancel();
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> {
            dismiss();
            okListener.ok(titleEdit.getText().toString());
        });

        if (StringUtil.isNotEmpty(okText)) {
            tv_confirm.setText(okText);
        }
        if (StringUtil.isNotEmpty(cancelText)) {
            tv_cancel.setText(cancelText);
        }
    }

    public interface OkListener {
        void ok(String title);

        void cancel();
    }
}

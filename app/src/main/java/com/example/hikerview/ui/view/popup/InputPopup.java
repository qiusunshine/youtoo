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

public class InputPopup extends CenterPopupView {

    private EditText titleEdit, urlEdit;
    private String title;
    private String titleHint, titleDefault;
    private String urlHint, urlDefault;
    private OkListener okListener;
    private CancelListener cancelListener;

    public InputPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_input_popup;
    }

    public InputPopup bind(String title, String titleHint, String urlHint, OkListener okListener) {
        return bind(title, titleHint, null, urlHint, null, okListener);
    }

    public InputPopup bind(String title, String titleHint, String titleDefault, String urlHint, String urlDefault, OkListener okListener) {
        this.title = title;
        this.titleHint = titleHint;
        this.titleDefault = titleDefault;
        this.urlHint = urlHint;
        this.urlDefault = urlDefault;
        this.okListener = okListener;
        return this;
    }


    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        TextView titleView = findViewById(R.id.title);
        titleEdit = findViewById(R.id.edit_title);
        urlEdit = findViewById(R.id.edit_url);
        titleView.setText(title);
        titleEdit.setHint(titleHint);
        urlEdit.setHint(urlHint);
        if (StringUtil.isNotEmpty(titleDefault)) {
            titleEdit.setText(titleDefault);
        }
        if (StringUtil.isNotEmpty(urlDefault)) {
            urlEdit.setText(urlDefault);
        }
        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            dismiss();
            if (cancelListener != null) {
                cancelListener.cancel();
            }
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> {
            dismiss();
            okListener.ok(titleEdit.getText().toString(), urlEdit.getText().toString());
        });
    }

    public CancelListener getCancelListener() {
        return cancelListener;
    }

    public InputPopup setCancelListener(CancelListener cancelListener) {
        this.cancelListener = cancelListener;
        return this;
    }

    public interface OkListener {
        void ok(String title, String url);
    }

    public interface CancelListener {
        void cancel();
    }
}

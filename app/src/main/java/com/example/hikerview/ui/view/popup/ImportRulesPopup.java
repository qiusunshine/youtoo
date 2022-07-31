package com.example.hikerview.ui.view.popup;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.lxj.xpopup.core.CenterPopupView;

/**
 * 作者：By 15968
 * 日期：On 2020/10/31
 * 时间：At 22:48
 */

public class ImportRulesPopup extends CenterPopupView {

    private OkListener okListener;
    private CheckBox checkBox;

    public ImportRulesPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_import_rules;
    }


    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();

        checkBox = findViewById(R.id.checkbox);
        checkBox.setChecked(true);
        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            dismiss();
            okListener.ok(checkBox.isChecked(), true);
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> {
            dismiss();
            okListener.ok(checkBox.isChecked(), false);
        });
    }

    public ImportRulesPopup withListener(OkListener okListener) {
        this.okListener = okListener;
        return this;
    }

    public interface OkListener {
        void ok(boolean backup, boolean delete);
    }
}

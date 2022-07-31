package com.example.hikerview.ui.rules;

import android.content.Context;
import android.widget.CheckBox;
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

public class RuleUpdateCheckPopup extends CenterPopupView {

    private OkListener okListener;
    private CheckBox checkBox;
    private String content;
    private String confirmText;
    private String cancelText;
    public RuleUpdateCheckPopup(@NonNull Context context, String content) {
        this(context, content, null, null);
    }

    public RuleUpdateCheckPopup(@NonNull Context context, String content, String confirmText, String cancelText) {
        this(context);
        this.content = content;
        this.confirmText = confirmText;
        this.cancelText = cancelText;
    }

    public RuleUpdateCheckPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_rule_update_check;
    }


    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();

        checkBox = findViewById(R.id.checkbox);
        checkBox.setChecked(true);
        ((TextView) findViewById(R.id.contentView)).setText(content);
        TextView tv_cancel = findViewById(R.id.tv_cancel);
        if(StringUtil.isNotEmpty(cancelText)){
            tv_cancel.setText(cancelText);
        }
        tv_cancel.setOnClickListener(v -> {
            dismiss();
            okListener.ok(checkBox.isChecked(), false);
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        if(StringUtil.isNotEmpty(confirmText)){
            tv_confirm.setText(confirmText);
        }
        tv_confirm.setOnClickListener(v -> {
            dismiss();
            okListener.ok(checkBox.isChecked(), true);
        });
    }

    public RuleUpdateCheckPopup withListener(OkListener okListener) {
        this.okListener = okListener;
        return this;
    }

    public interface OkListener {
        void ok(boolean checked, boolean ok);
    }
}

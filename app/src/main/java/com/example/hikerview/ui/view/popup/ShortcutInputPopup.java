package com.example.hikerview.ui.view.popup;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.enums.ShortcutTypeEnum;
import com.example.hikerview.ui.browser.model.Shortcut;
import com.example.hikerview.utils.StringUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.CenterPopupView;

/**
 * 作者：By 15968
 * 日期：On 2020/10/31
 * 时间：At 22:48
 */

public class ShortcutInputPopup extends CenterPopupView {

    private EditText titleEdit, urlEdit, iconEdit;
    private TextView typeView;
    private Shortcut shortcut;
    private OkListener okListener;

    public ShortcutInputPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_shortcut_popup;
    }

    public ShortcutInputPopup bind(Shortcut shortcut, OkListener okListener) {
        try {
            this.shortcut = shortcut.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            this.shortcut = shortcut;
        }
        this.okListener = okListener;
        return this;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        titleEdit = findViewById(R.id.edit_title);
        urlEdit = findViewById(R.id.edit_url);
        iconEdit = findViewById(R.id.edit_icon);
        typeView = findViewById(R.id.edit_type);
        titleEdit.setText(shortcut.getName());
        urlEdit.setText(shortcut.getUrl());
        String icon = StringUtil.isEmpty(shortcut.getIcon()) || shortcut.getIcon().startsWith("color") ? "" : shortcut.getIcon();
        iconEdit.setText(icon);
        if (StringUtil.isEmpty(shortcut.getType())) {
            shortcut.setType(ShortcutTypeEnum.DEFAULT.name());
        }
        typeView.setText(ShortcutTypeEnum.Companion.getName(shortcut.getType()));
        typeView.setOnClickListener(v -> {
            new XPopup.Builder(getContext())
                    .asCenterList("选择显示样式", ShortcutTypeEnum.Companion.getNames(), (position, text) -> {
                        typeView.setText(text);
                    })
                    .show();
        });
        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            dismiss();
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> {
            dismiss();
            shortcut.setName(titleEdit.getText().toString());
            shortcut.setUrl(urlEdit.getText().toString());
            shortcut.setIcon(iconEdit.getText().toString());
            shortcut.setType(ShortcutTypeEnum.Companion.getCode(typeView.getText().toString()));
            okListener.ok(shortcut);
        });
    }

    public interface OkListener {
        void ok(Shortcut shortcut);
    }
}

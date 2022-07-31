package com.example.hikerview.ui.view.popup;

import android.content.Context;
import android.widget.CheckBox;
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

public class InputCheckPopup extends CenterPopupView {

    private EditText titleEdit, urlEdit;
    private String title;
    private String titleHint, titleDefault;
    private String urlHint, urlDefault;
    private OkListener okListener;
    private boolean checkedDefault;
    private String checkText;
    private CheckBox checkBox;

    public InputCheckPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_input_check_popup;
    }

    public InputCheckPopup bind(String title, String titleHint, String urlHint, OkListener okListener) {
        return bind(title, titleHint, null, urlHint, null, null, true, okListener);
    }

    public InputCheckPopup bind(String title, String titleHint, String titleDefault, String urlHint, String urlDefault,
                                String checkText, boolean checkedDefault, OkListener okListener) {
        this.title = title;
        this.titleHint = titleHint;
        this.titleDefault = titleDefault;
        this.urlHint = urlHint;
        this.urlDefault = urlDefault;
        this.okListener = okListener;
        this.checkText = checkText;
        this.checkedDefault = checkedDefault;
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

        checkBox = findViewById(R.id.checkbox);
        if(StringUtil.isNotEmpty(checkText)){
            checkBox.setText(checkText);
        }
        checkBox.setChecked(checkedDefault);
        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            dismiss();
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> {
            dismiss();
            okListener.ok(titleEdit.getText().toString(), urlEdit.getText().toString(),checkBox.isChecked());
        });
    }

    public interface OkListener {
        void ok(String title, String url, boolean checked);
    }
}

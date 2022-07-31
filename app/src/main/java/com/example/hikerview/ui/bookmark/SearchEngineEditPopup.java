package com.example.hikerview.ui.bookmark;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.CenterPopupView;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/10/31
 * 时间：At 22:48
 */

public class SearchEngineEditPopup extends CenterPopupView {

    private EditText titleEdit, urlEdit, group_edit_text;
    private String title;
    private String titleDefault;
    private String urlDefault;
    private OkListener okListener;
    private String groupDefault;
    private List<String> groups;
    private boolean mShowShortcut;
    private String titleHint, urlHint;

    public SearchEngineEditPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_input_sengine_add;
    }

    public SearchEngineEditPopup bind(String title, List<String> groups, OkListener okListener) {
        return bind(title, null, null, null, groups, okListener);
    }

    public SearchEngineEditPopup bind(String title, String titleDefault, String urlDefault, String groupPath, List<String> groups, OkListener okListener) {
        this.title = title;
        this.titleDefault = titleDefault;
        this.urlDefault = urlDefault;
        this.okListener = okListener;
        this.groupDefault = groupPath;
        this.groups = groups;
        return this;
    }

    public SearchEngineEditPopup bindHint(String titleHint, String urlHint){
        this.titleHint = titleHint;
        this.urlHint = urlHint;
        return this;
    }

    public SearchEngineEditPopup showShortcut(boolean show){
        mShowShortcut = show;
        return this;
    }


    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        TextView titleView = findViewById(R.id.title);
        titleEdit = findViewById(R.id.edit_title);
        group_edit_text = findViewById(R.id.group_edit_text);
        urlEdit = findViewById(R.id.edit_url);
        if(StringUtil.isNotEmpty(titleHint)){
            titleEdit.setHint(titleHint);
        }
        if(StringUtil.isNotEmpty(urlHint)){
            urlEdit.setHint(urlHint);
        }
        ImageView groupImg = findViewById(R.id.group_img_view);
        titleView.setText(title);
        if (StringUtil.isNotEmpty(titleDefault)) {
            titleEdit.setText(titleDefault);
        }
        if (StringUtil.isNotEmpty(urlDefault)) {
            urlEdit.setText(urlDefault);
        }
        if (StringUtil.isNotEmpty(groupDefault)) {
            group_edit_text.setText(groupDefault);
        }

        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            dismiss();
        });

        if(mShowShortcut){
            findViewById(R.id.checkbox_shortcut).setVisibility(VISIBLE);
        }

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> {
            dismiss();
            CheckBox shortcut = findViewById(R.id.checkbox_shortcut);
            boolean show = shortcut.getVisibility() != VISIBLE ? false : shortcut.isChecked();
            okListener.ok(titleEdit.getText().toString(), urlEdit.getText().toString(), group_edit_text.getText().toString(), show);
        });
        groupImg.setOnClickListener(v -> {
            if (CollectionUtil.isEmpty(groups)) {
                ToastMgr.shortCenter(getContext(), "还没有标签或分组哦，直接在输入框输入一个吧");
            } else {
                String[] s = new String[groups.size()];
                CustomCenterRecyclerViewPopup popup = new CustomCenterRecyclerViewPopup(getContext())
                        .withTitle("选择分组")
                        .with(groups.toArray(s), 2, new CustomCenterRecyclerViewPopup.ClickListener() {
                            @Override
                            public void click(String url, int position) {
                                group_edit_text.setText(url);
                            }

                            @Override
                            public void onLongClick(String url, int position) {

                            }
                        });
                new XPopup.Builder(getContext())
                        .asCustom(popup)
                        .show();
            }
        });
        ClipboardUtil.getText(getContext(), urlEdit, text -> {
            if (StringUtil.isNotEmpty(text) && text.startsWith("http") && StringUtil.isEmpty(urlEdit.getText())) {
                urlEdit.setText(text);
            }
        });
    }

    public interface OkListener {
        void ok(String title, String url, String group, boolean shortcut);
    }

    public void updateUrl(String url) {
        if (urlEdit != null) {
            urlEdit.setText(url);
        }
    }

    public void updateGroups(List<String> groups){
        this.groups = groups;
    }
}

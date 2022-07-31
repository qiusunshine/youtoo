package com.example.hikerview.ui.rules;

import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.service.parser.CommonParser;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.rules.model.ClazzFetchRule;
import com.example.hikerview.ui.setting.model.SettingConfig;
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

public class ClazzFetchPopup extends CenterPopupView {

    private EditText urlEdit, ruleEdit, edit_result;
    private TextView title;

    public ClazzFetchPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_clazz_fetch_popup;
    }


    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        title = findViewById(R.id.title);
        urlEdit = findViewById(R.id.edit_url);
        ruleEdit = findViewById(R.id.edit_rule);
        edit_result = findViewById(R.id.edit_result);
        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            dismiss();
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> fetchClazz());

        TextView tv_copy = findViewById(R.id.tv_copy);
        tv_copy.setOnClickListener(v -> {
            ClipboardUtil.copyToClipboard(getContext(), edit_result.getText().toString(), false);
            ToastMgr.shortCenter(getContext(), "复制成功");
            dismiss();
        });

        findViewById(R.id.clazz_fetch_notice_icon).setOnClickListener(v -> {
            new XPopup.Builder(getContext())
                    .asConfirm("使用说明", "分类名称和分类替换词通常可以解析网页来获取，只需要一个列表加标题的规则即可" +
                            "如body&&.header&&a;title&&Text，网址请填写能够获取分类名称的链接即可", () -> {

                    }).show();
        });

        if (StringUtil.isNotEmpty(SettingConfig.clazzFetchingRule)) {
            ClazzFetchRule fetchRule = JSON.parseObject(SettingConfig.clazzFetchingRule, ClazzFetchRule.class);
            if (fetchRule != null && StringUtil.isNotEmpty(fetchRule.getUrl())) {
                urlEdit.setText(fetchRule.getUrl());
            }
            if (fetchRule != null && StringUtil.isNotEmpty(fetchRule.getRule())) {
                ruleEdit.setText(fetchRule.getRule());
            }
        }
    }

    private void fetchClazz() {
        String url = urlEdit.getText().toString();
        if (StringUtil.isEmpty(url)) {
            ToastMgr.shortCenter(getContext(), "网址不能为空");
            return;
        }
        String rule = ruleEdit.getText().toString();
        if (StringUtil.isEmpty(rule)) {
            ToastMgr.shortCenter(getContext(), "规则不能为空");
            return;
        }
        String[] rules = rule.split(";");
        if (rules.length != 2) {
            ToastMgr.shortCenter(getContext(), "规则有误，格式：列表;标题");
            return;
        }
        title.setText("加载中，请稍候");
        ClazzFetchRule fetchRule = new ClazzFetchRule();
        fetchRule.setUrl(url);
        fetchRule.setRule(rule);
        SettingConfig.clazzFetchingRule = JSON.toJSONString(fetchRule);
        HttpParser.parseSearchUrlForHtml(url, new HttpParser.OnSearchCallBack() {
            @Override
            public void onSuccess(String url, String s) {
                title.setText("快速生成分类");
                try {
                    List<String> result = CommonParser.parseDomForHtmlList(s, rules[0], rules[1]);
                    if (CollectionUtil.isEmpty(result)) {
                        ToastMgr.shortCenter(getContext(), "数据为空");
                        edit_result.setText("");
                        return;
                    }
                    String resultStr = StringUtil.listToString(result, "&");
                    edit_result.setText(resultStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastMgr.longCenter(getContext(), "出错：" + e.getMessage());
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                title.setText("快速生成分类");
                ToastMgr.longCenter(getContext(), "出错：" + msg);
            }
        });
    }
}

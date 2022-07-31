package com.example.hikerview.ui.browser.view;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.hikerview.R;
import com.example.hikerview.service.parser.HttpHelper;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ThreadTool;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.core.CenterPopupView;

import kotlinx.coroutines.Job;

/**
 * 作者：By 15968
 * 日期：On 2020/10/31
 * 时间：At 22:48
 */

public class TranslatePopup extends CenterPopupView {

    private TextView titleEdit, urlEdit;
    private String title;
    private Job job;

    public TranslatePopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_translate_popup;
    }

    public TranslatePopup bind(String title) {
        this.title = title;
        return this;
    }


    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        titleEdit = findViewById(R.id.edit_title);
        urlEdit = findViewById(R.id.edit_url);
        titleEdit.setText((title + "："));
        urlEdit.setText("翻译中...");
        TextView tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(v -> {
            String t = urlEdit.getText().toString();
            if (StringUtil.isEmpty(t) || "翻译中...".equals(t)) {
                return;
            }
            ClipboardUtil.copyToClipboardForce(getContext(), t);
        });

        TextView tv_confirm = findViewById(R.id.tv_confirm);
        tv_confirm.setOnClickListener(v -> {
            dismiss();
        });

        job = HeavyTaskUtil.launch(() -> {
            translate(title);
        });
    }

    private void translate(String text) {
        String api = JSEngine.getInstance().base64Decode("aHR0cHM6Ly9mYW55aS1hcGkuYmFpZHUuY29tL2FwaS90cmFucy92aXAvdHJhbnNsYXRl");
        String id = JSEngine.getInstance().base64Decode("MjAyMjAyMjgwMDExMDE4NDgwMDk=");
        id = id.substring(0, id.length() - 3);
        String key = JSEngine.getInstance().base64Decode("MTJ3SHJVbzFlQjVyUFkwRjdmYnFoMA==");
        key = key.substring(2);
        String salt = StringUtil.genRandomPwd(6);
        String sign = StringUtil.md5(id + text + salt + key);
        //是否中文，是就翻译为英文，否则中文
        boolean zh = StringUtil.containsChinese(text);
        String url = api + "?q=" + text + "&from=auto&appid=" + id + "&salt=" + salt + "&sign=" + sign + "&to=" + (zh ? "en" : "zh");
        String result = HttpHelper.get(url, null);
        if (StringUtil.isNotEmpty(result)) {
            try {
                JSONObject jsonObject = JSON.parseObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("trans_result");
                if (jsonArray != null && jsonArray.size() > 0) {
                    String dst = jsonArray.getJSONObject(0).getString("dst");
                    ThreadTool.INSTANCE.runOnUI(() -> {
                        if (isShow()) {
                            urlEdit.setText(dst);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                ThreadTool.INSTANCE.runOnUI(() -> ToastMgr.shortCenter(getContext(), "出错：" + e.getMessage()));
            }
        }
    }

    @Override
    protected void onDismiss() {
        HeavyTaskUtil.cancel(job);
        super.onDismiss();
    }
}

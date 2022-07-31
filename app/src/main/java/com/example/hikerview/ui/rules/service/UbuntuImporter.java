package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.example.hikerview.service.http.CharsetStringConvert;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.service.parser.HttpHelper;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.request.PostRequest;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/8/5
 * 时间：At 11:49
 */

public class UbuntuImporter implements RuleImporter {

    @Override
    public void share(Activity activity, String paste, String title, @Nullable @org.jetbrains.annotations.Nullable String password, String rulePrefix) {
        HeavyTaskUtil.executeNewTask(() -> {
            try {
                PostRequest<String> request = OkGo.post("https://paste.ubuntu.com/");
                request.client(HttpHelper.getNoRedirectHttpClient());
                request.params("poster", "hkdd")
                        .params("syntax", "text")
                        .params("expiration", "year")
                        .params("content", paste);
                request.retryCount(1);
                Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
                com.lzy.okgo.model.Response<String> response = call.execute();
                Map<String, List<String>> headers = response.headers() == null ? null : response.headers().toMultimap();
                String error = null;
                if (response.getException() != null) {
                    Timber.e(response.getException());
                    error = response.getException().getMessage();
                }
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                if (error != null || headers == null) {
                    String finalError = error;
                    activity.runOnUiThread(() -> {
                        ToastMgr.shortBottomCenter(activity, "提交失败：" + finalError);
                    });
                } else {
                    List<String> urls = headers.get("Location");
                    if (CollectionUtil.isNotEmpty(urls)) {
                        activity.runOnUiThread(() -> {
                            String url = "https://paste.ubuntu.com" + urls.get(0);
                            url = url + "\n\n" + rulePrefix + "：" + title;
                            ClipboardUtil.copyToClipboardForce(activity, url, false);
                            AutoImportHelper.setShareRule(url);
                            ToastMgr.shortBottomCenter(activity, "云剪贴板地址已复制到剪贴板");
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                activity.runOnUiThread(() -> {
                    ToastMgr.shortCenter(activity, "提交云剪贴板失败：" + e.getMessage());
                });
            }
        });
    }

    @Override
    public boolean canSetPwd() {
        return false;
    }

    @Override
    public boolean canParse(String text) {
        return text.startsWith("https://paste.ubuntu.com/p/");
    }

    @Override
    public void parse(Activity activity, String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0];
            CodeUtil.get(url, new CodeUtil.OnCodeGetListener() {
                @Override
                public void onSuccess(String s) {
                    if (activity.isFinishing()) {
                        return;
                    }
                    if (StringUtil.isEmpty(s)) {
                        return;
                    }
                    try {
                        String content = null;
                        try {
                            content = JSEngine.getInstance().parseDomForHtml(s, ".highlight&&pre&&Text");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (StringUtil.isEmpty(content)) {
                            try {
                                content = JSEngine.getInstance().parseDomForHtml(s, ".pastetable&&.code&&pre&&Text");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (StringUtil.isEmpty(content)) {
                            return;
                        }
                        String finalContent = content;
                        activity.runOnUiThread(() -> {
                            try {
                                AutoImportHelper.checkAutoText(activity, finalContent);
                            } catch (Exception ignored) {
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int errorCode, String msg) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canUseSync() {
        return false;
    }

    @Override
    public String shareSync(String paste) {
        return null;
    }

    @Override
    public String parseSync(String url) {
        return null;
    }
}
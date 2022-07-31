package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.example.hikerview.service.http.CharsetStringConvert;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.service.parser.CommonParser;
import com.example.hikerview.service.parser.HttpHelper;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/8/5
 * 时间：At 11:49
 */

public class CmdImporter implements RuleImporter {

    @Override
    public void share(Activity activity, String paste, String title, @Nullable @org.jetbrains.annotations.Nullable String password, String rulePrefix) {
        HeavyTaskUtil.executeNewTask(() -> {
            try {
                PostRequest<String> request = OkGo.post("https://cmd.im/");
                request.client(HttpHelper.getNoRedirectHttpClient());
                request.params("txt", paste)
                        .headers("Host", "cmd.im")
                        .headers("Origin", "https://cmd.im")
                        .headers("Referer", "https://cmd.im/")
                        .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
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
                            String url = "https://cmd.im" + urls.get(0);
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
        return text.startsWith("https://cmd.im/");
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
                    String code = CommonParser.parseDomForUrl(s, ".test_box&&Text", "");
                    activity.runOnUiThread(() -> {
                        try {
                            AutoImportHelper.checkAutoText(activity, code);
                        } catch (Exception ignored) {
                        }
                    });
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
        return true;
    }

    @Override
    public String shareSync(String paste) {
        try {
            PostRequest<String> request = OkGo.post("https://cmd.im/");
            request.client(HttpHelper.getNoRedirectHttpClient());
            request.params("txt", paste)
                    .headers("Host", "cmd.im")
                    .headers("Origin", "https://cmd.im")
                    .headers("Referer", "https://cmd.im/")
                    .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
            request.retryCount(1);
            Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
            com.lzy.okgo.model.Response<String> response = call.execute();
            Map<String, List<String>> headers = response.headers() == null ? null : response.headers().toMultimap();
            String error = null;
            if (response.getException() != null) {
                Timber.e(response.getException());
                error = response.getException().getMessage();
            }
            if (error != null || headers == null) {
                return "error:" + error;
            } else {
                List<String> urls = headers.get("Location");
                if (CollectionUtil.isNotEmpty(urls)) {
                    return "https://cmd.im" + urls.get(0);
                }
            }
            return "error:";
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }

    @Override
    public String parseSync(String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0];
            GetRequest<String> request = OkGo.get(url);
            Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
            com.lzy.okgo.model.Response<String> res = call.execute();
            if (res.getException() != null) {
                Timber.e(res.getException());
                return "error:" + res.getException().getMessage();
            }
            String s = res.body();
            return CommonParser.parseDomForUrl(s, ".test_box&&Text", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }
}
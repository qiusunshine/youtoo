package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.example.hikerview.service.http.CharsetStringConvert;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.service.parser.HttpHelper;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.request.PostRequest;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/8/5
 * 时间：At 11:49
 */

public class PastebinImporter implements RuleImporter {

    @Override
    public void share(Activity activity, String paste, String title, @Nullable @org.jetbrains.annotations.Nullable String password, String rulePrefix) {
        HeavyTaskUtil.executeNewTask(() -> {
            try {
                PostRequest<String> request = OkGo.post("https://pastebin.com/api/api_post.php");
                request.client(HttpHelper.getNoRedirectHttpClient());
                request.params("api_dev_key", "Qs2VjmfmU1Qz-SGdXpUV6fRPquPzR0js")
                        .params("api_paste_code", paste)
                        .params("api_paste_private", "0")
                        .params("api_paste_expire_date", "N")
                        .params("api_paste_name", rulePrefix + "：" + title)
                        .params("api_user_key", "c02545bd6fcfc9dcb82b36cd8ac7be61")
                        .params("api_paste_format", "javascript")
                        .params("api_option", "paste");
                request.retryCount(1);
                Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
                com.lzy.okgo.model.Response<String> response = call.execute();
                String error = null;
                if (response.getException() != null) {
                    Timber.e(response.getException());
                    error = response.getException().getMessage();
                }
                if (activity == null || activity.isFinishing()) {
                    return;
                }
                if (error != null) {
                    String finalError = error;
                    activity.runOnUiThread(() -> {
                        ToastMgr.shortBottomCenter(activity, "提交失败：" + finalError);
                    });
                } else {
                    String url1 = response.body();
                    activity.runOnUiThread(() -> {
                        String url = url1;
                        url = url + "\n\n" + rulePrefix + "：" + title;
                        ClipboardUtil.copyToClipboardForce(activity, url, false);
                        AutoImportHelper.setShareRule(url);
                        ToastMgr.shortBottomCenter(activity, "云剪贴板地址已复制到剪贴板");
                    });
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
        return text.startsWith("https://pastebin.com/");
    }

    @Override
    public void parse(Activity activity, String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0];
            if (url.startsWith("https://pastebin.com/raw/")) {
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
                            activity.runOnUiThread(() -> {
                                try {
                                    AutoImportHelper.checkAutoText(activity, s);
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
            } else {
                HttpParams params = new HttpParams();
                params.put("api_dev_key", "Qs2VjmfmU1Qz-SGdXpUV6fRPquPzR0js");
                params.put("api_user_key", "c02545bd6fcfc9dcb82b36cd8ac7be61");
                params.put("api_paste_key", url.replace("https://pastebin.com/", ""));
                params.put("api_option", "show_paste");
                CodeUtil.post("https://pastebin.com/api/api_raw.php", params, new CodeUtil.OnCodeGetListener() {
                    @Override
                    public void onSuccess(String s) {
                        if (activity.isFinishing()) {
                            return;
                        }
                        if (StringUtil.isEmpty(s)) {
                            return;
                        }
                        try {
                            activity.runOnUiThread(() -> {
                                try {
                                    AutoImportHelper.checkAutoText(activity, s);
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
            }
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
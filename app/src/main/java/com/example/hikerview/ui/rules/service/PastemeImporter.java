package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.service.http.CharsetStringCallback;
import com.example.hikerview.ui.home.model.PastemeResponse;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/8/5
 * 时间：At 11:49
 */

public class PastemeImporter implements RuleImporter {

    @Override
    public void share(Activity activity, String paste, String title, @Nullable @org.jetbrains.annotations.Nullable String password, String rulePrefix) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lang", "plain");
            jsonObject.put("content", paste);
            if (StringUtil.isNotEmpty(password)) {
                jsonObject.put("password", password);
            }
            BasePopupView popupView = new XPopup.Builder(activity)
                    .asLoading("正在提交云剪贴板")
                    .show();
            PostRequest<String> request = OkGo.post("https://pasteme.cn/_api/backend/");
            request.upJson(jsonObject)
                    .headers(":authority", "pasteme.cn")
                    .headers(":method", "POST")
                    .headers(":path", "/_api/backend/")
                    .headers(":scheme", "https")
                    .headers("referer", "https://pasteme.cn/")
                    .headers("cookie", "")
                    .execute(new CharsetStringCallback("UTF-8") {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<String> res) {
                            String s = res.body();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }

                            if (StringUtil.isEmpty(s)) {
                                activity.runOnUiThread(() -> {
                                    if (popupView != null) {
                                        popupView.dismiss();
                                    }
                                });
                                return;
                            }
                            activity.runOnUiThread(() -> {
                                if (popupView != null) {
                                    popupView.dismiss();
                                }
                                try {
                                    Timber.d("shareByPasteme onSuccess: %s", s);
                                    PastemeResponse response = JSON.parseObject(s, PastemeResponse.class);
                                    if (response == null || StringUtil.isEmpty(response.getKey())) {
                                        ToastMgr.shortCenter(activity, "提交云剪贴板失败");
                                    } else {
                                        String url = "https://pasteme.cn/" + response.getKey();
                                        if (StringUtil.isNotEmpty(password)) {
                                            url = url + " " + password;
                                        }
                                        url = url + "\n\n" + rulePrefix + "：" + title;
                                        ClipboardUtil.copyToClipboardForce(activity, url, false);
                                        AutoImportHelper.setShareRule(url);
                                        ToastMgr.shortBottomCenter(activity, "云剪贴板地址已复制到剪贴板");
                                    }
                                } catch (Exception e) {
                                    String msg = e.getMessage();
                                    if (msg != null && msg.length() > 20) {
                                        msg = msg.substring(0, 20);
                                    }
                                    ToastMgr.shortCenter(activity, "提交云剪贴板失败:" + msg);
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void onError(com.lzy.okgo.model.Response<String> response) {
                            super.onError(response);
                            String msg = response.getException().toString();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }
                            activity.runOnUiThread(() -> {
                                if (popupView != null) {
                                    popupView.dismiss();
                                }
                                ToastMgr.shortCenter(activity, "提交云剪贴板失败：" + msg);
                            });
                        }
                    });
        } catch (JSONException e) {
            ToastMgr.shortCenter(activity, "提交云剪贴板失败：" + e.getMessage());
        }
    }

    @Override
    public boolean canSetPwd() {
        return true;
    }

    @Override
    public boolean canParse(String text) {
        return text.startsWith("http://pasteme.cn/") || text.startsWith("https://pasteme.cn/");
    }

    @Override
    public void parse(Activity activity, String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0];
            String[] urls = url.split(" ");
            if (urls.length == 1) {
                url = url.replace("//pasteme.cn/", "//pasteme.cn/_api/backend/") + "?json=true";
            } else {
                url = urls[0].replace("//pasteme.cn/", "//pasteme.cn/_api/backend/") + "," + urls[1] + "?json=true";
            }
            GetRequest<String> request = OkGo.get(url);
            request.headers(":authority", "pasteme.cn")
                    .headers(":method", "POST")
                    .headers(":path", "/_api/backend/")
                    .headers(":scheme", "https")
                    .headers("referer", "https://pasteme.cn/")
                    .headers("cookie", "")
                    .execute(new CharsetStringCallback("UTF-8") {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<String> res) {
                            String s = res.body();
                            if (activity == null || activity.isFinishing()) {
                                return;
                            }
                            if (StringUtil.isEmpty(s)) {
                                return;
                            }
                            activity.runOnUiThread(() -> {
                                Timber.d("checkClipboardByPasteme onSuccess: %s", s);
                                try {
                                    PastemeResponse response = JSON.parseObject(s, PastemeResponse.class);
                                    if (response != null && !StringUtil.isEmpty(response.getContent())) {
                                        try {
                                            AutoImportHelper.checkAutoText(activity, response.getContent());
                                        } catch (Exception ignored) {
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void onError(com.lzy.okgo.model.Response<String> response) {
                            super.onError(response);
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
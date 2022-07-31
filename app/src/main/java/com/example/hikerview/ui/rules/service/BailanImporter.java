package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.service.http.CharsetStringCallback;
import com.example.hikerview.service.http.CharsetStringConvert;
import com.example.hikerview.ui.rules.model.BailanBodyResponse;
import com.example.hikerview.ui.rules.model.BailanResponse;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
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

public class BailanImporter implements RuleImporter {

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
            PostRequest<String> request = OkGo.post("https://pasteme.tyrantg.com/api/create");
            request.upJson(jsonObject)
                    .headers("referer", "https://pasteme.tyrantg.com/")
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
                                    Timber.d("shareByPasteme3 onSuccess: %s", s);
                                    BailanResponse response = JSON.parseObject(s, BailanResponse.class);
                                    if (response == null || response.getData() == null || StringUtil.isEmpty(response.getData().getPath())) {
                                        ToastMgr.shortCenter(activity, "提交云剪贴板失败");
                                    } else {
                                        String url = "https://pasteme.tyrantg.com/xxxxxx/" + response.getData().getPath();
                                        if (StringUtil.isNotEmpty(password)) {
                                            url = url + "@" + password;
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
        return text.startsWith("https://pasteme.tyrantg.com/xxxxxx/");
    }

    @Override
    public void parse(Activity activity, String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0].replace("/xxxxxx/", "/api/getContent/");
            GetRequest<String> request = OkGo.get(url);
            request.headers("referer", "https://pasteme.tyrantg.com/")
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
                                    BailanBodyResponse response = JSON.parseObject(s, BailanBodyResponse.class);
                                    if (response != null && !StringUtil.isEmpty(response.getData())) {
                                        try {
                                            AutoImportHelper.checkAutoText(activity, response.getData());
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
        return true;
    }

    @Override
    public String shareSync(String paste) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lang", "plain");
            jsonObject.put("content", paste);
            PostRequest<String> request = OkGo.post("https://pasteme.tyrantg.com/api/create");
            request.upJson(jsonObject)
                    .headers("referer", "https://pasteme.tyrantg.com/")
                    .headers("cookie", "");
            Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
            com.lzy.okgo.model.Response<String> res = call.execute();
            if (res.getException() != null) {
                Timber.e(res.getException());
                return "error:" + res.getException().getMessage();
            }
            BailanResponse response = JSON.parseObject(res.body(), BailanResponse.class);
            if (response == null || response.getData() == null || StringUtil.isEmpty(response.getData().getPath())) {
                return "error:response.path is empty";
            } else {
                return "https://pasteme.tyrantg.com/xxxxxx/" + response.getData().getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }

    @Override
    public String parseSync(String url) {
        try {
            url = url.split("\n")[0].replace("/xxxxxx/", "/api/getContent/");
            GetRequest<String> request = OkGo.get(url);
            request.headers("referer", "https://pasteme.tyrantg.com/")
                    .headers("cookie", "");
            Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
            com.lzy.okgo.model.Response<String> res = call.execute();
            if (res.getException() != null) {
                Timber.e(res.getException());
                return "error:" + res.getException().getMessage();
            }
            String s = res.body();
            BailanBodyResponse response = JSON.parseObject(s, BailanBodyResponse.class);
            if (response != null && !StringUtil.isEmpty(response.getData())) {
                return response.getData();
            }
            return "error:";
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }
}
package com.example.hikerview.ui.rules.service;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.service.http.CharsetStringCallback;
import com.example.hikerview.service.http.CharsetStringConvert;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.ui.home.model.NetCutResponse;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.TimeUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/8/5
 * 时间：At 11:49
 */

public class NetCutImporter implements RuleImporter {

    @Override
    public void share(Activity activity, String paste, String title, @Nullable @org.jetbrains.annotations.Nullable String password, String rulePrefix) {
        PostRequest<String> request = OkGo.post("https://netcut.cn/api/note/create/");
        String noteName = "cz" + TimeUtil.getSecondTimestamp();
        request.params("note_name", noteName)
                .params("note_content", paste)
                .params("note_pwd", "0")
                .params("expire_time", "31536000")
                .headers("Host", "netcut.cn")
                .headers("Origin", "https://netcut.cn")
                .headers("Referer", "https://netcut.cn/" + noteName)
                .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36")
                .execute(new CharsetStringCallback("UTF-8") {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> res) {
                        String s = res.body();
                        Timber.d("shareByNetCut, response:%s", s);
                        if (activity == null || activity.isFinishing()) {
                            return;
                        }

                        if (StringUtil.isEmpty(s)) {
                            return;
                        }
                        activity.runOnUiThread(() -> {
                            try {
                                NetCutResponse response = JSON.parseObject(s, NetCutResponse.class);
                                if (response == null || response.getData() == null || response.getStatus() != 1) {
                                    ToastMgr.shortCenter(activity, "提交云剪贴板失败:" + (response == null ? "null" : response.getError()));
                                } else {
                                    String url = "https://netcut.cn/p/" + response.getData().getNote_id();
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
                            ToastMgr.shortCenter(activity, "提交云剪贴板失败：" + msg);
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
        return text.startsWith("https://netcut.cn/p/") || text.startsWith("http://netcut.cn/p/");
    }

    @Override
    public void parse(Activity activity, String url) {
        try {
            url = StringUtil.trimBlanks(url);
            url = url.split("\n")[0];
            String[] urls = url.split(" ");
            String[] noteIdUrl = urls[0].split("/p/");
            if (noteIdUrl.length != 2) {
                return;
            }
            String noteId = noteIdUrl[1];
            url = "http://netcut.cn/api/note/data/?note_id=" + noteId;
            CodeUtil.get(url, new CodeUtil.OnCodeGetListener() {
                @Override
                public void onSuccess(String s) {
                    if (activity.isFinishing()) {
                        return;
                    }
                    if (StringUtil.isEmpty(s)) {
                        return;
                    }
                    activity.runOnUiThread(() -> {
                        NetCutResponse response = JSON.parseObject(s, NetCutResponse.class);
                        if (response != null && response.getData() != null && StringUtil.isNotEmpty(response.getData().getNote_content())) {
                            try {
                                AutoImportHelper.checkAutoText(activity, response.getData().getNote_content());
                            } catch (Exception ignored) {
                            }
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
            PostRequest<String> request = OkGo.post("https://netcut.cn/api/note/create/");
            String noteName = "cz" + TimeUtil.getSecondTimestamp();
            request.params("note_name", noteName)
                    .params("note_content", paste)
                    .params("note_pwd", "0")
                    .params("expire_time", "31536000")
                    .headers("Host", "netcut.cn")
                    .headers("Origin", "https://netcut.cn")
                    .headers("Referer", "https://netcut.cn/" + noteName)
                    .headers("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36");
            Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
            com.lzy.okgo.model.Response<String> res = call.execute();
            if (res.getException() != null) {
                Timber.e(res.getException());
                return "error:" + res.getException().getMessage();
            }
            NetCutResponse response = JSON.parseObject(res.body(), NetCutResponse.class);
            if (response == null || response.getData() == null || response.getStatus() != 1) {
                return "error:" + (response == null ? "null" : response.getError());
            } else {
                return "https://netcut.cn/p/" + response.getData().getNote_id();
            }
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
            String[] urls = url.split(" ");
            String[] noteIdUrl = urls[0].split("/p/");
            if (noteIdUrl.length != 2) {
                return url;
            }
            String noteId = noteIdUrl[1];
            url = "http://netcut.cn/api/note/data/?note_id=" + noteId;
            GetRequest<String> request = OkGo.get(url);
            Call<String> call = request.converter(new CharsetStringConvert("UTF-8")).adapt();
            com.lzy.okgo.model.Response<String> res = call.execute();
            if (res.getException() != null) {
                Timber.e(res.getException());
                return "error:" + res.getException().getMessage();
            }
            String s = res.body();
            NetCutResponse response = JSON.parseObject(s, NetCutResponse.class);
            if (response != null && response.getData() != null && StringUtil.isNotEmpty(response.getData().getNote_content())) {
                return response.getData().getNote_content();
            }
            return "error:";
        } catch (Exception e) {
            e.printStackTrace();
            return "error:" + e.getMessage();
        }
    }
}
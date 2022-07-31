package com.example.hikerview.ui.video.remote;

import android.app.Activity;
import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.function.Consumer;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.model.RemotePlaySource;
import com.example.hikerview.ui.video.model.RemotePlaySourceBatchData;
import com.example.hikerview.ui.video.util.ScanLiveTVUtils;
import com.example.hikerview.ui.webdlan.LocalServerParser;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lzy.okgo.model.HttpParams;

import java.util.List;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2021/6/13
 * 时间：At 22:53
 */

public class LivePlayerHelper {

    /**
     * 直接投屏，投屏失败不重新扫描
     *
     * @param context
     * @param loadingPopupView
     * @param title
     * @param url
     */
    public static void playWithLiveTv(Activity context, LoadingPopupView loadingPopupView, String title, String url) {
        playWithLiveTv(context, loadingPopupView, title, url, false);
    }

    /**
     * 投屏
     *
     * @param context
     * @param loadingPopupView
     * @param title
     * @param url
     * @param forceRescan      失败时是否重新扫描
     */
    public static void playWithLiveTv(Activity context, LoadingPopupView loadingPopupView, String title, String url, boolean forceRescan) {
        if (!PreferenceMgr.contains(context, "custom", "liveTvAddress")) {
            new XPopup.Builder(context)
                    .asConfirm("温馨提示", "这是你第一次使用该功能，以下是该功能的提示：该功能主要是配合第三方软件世界直播使用，主要用于将视频投放到电视上，点击即可让世界直播播放手机上播放的地址",
                            () -> {
                                if (StringUtil.isNotEmpty(SettingConfig.liveTvAddress)) {
                                    playWithLiveTvByAddress(context, loadingPopupView, SettingConfig.liveTvAddress, title, url, forceRescan);
                                } else {
                                    rescanLiveTv(context, loadingPopupView, title, url);
                                }
                            }).show();
        } else {
            SettingConfig.loadLiveAddress(context);
            if (StringUtil.isNotEmpty(SettingConfig.liveTvAddress)) {
                playWithLiveTvByAddress(context, loadingPopupView, SettingConfig.liveTvAddress, title, url, forceRescan);
            } else {
                rescanLiveTv(context, loadingPopupView, title, url);
            }
        }
    }

    /**
     * 重新扫描局域网
     *
     * @param context
     * @param loadingPopupView
     * @param title
     * @param url
     */
    public static void rescanLiveTv(Activity context, LoadingPopupView loadingPopupView, String title, String url) {
        scanLiveTvAddress(context, loadingPopupView, url1 -> {
            playWithLiveTvByAddress(context, loadingPopupView, SettingConfig.liveTvAddress, title, url, false);
        });
    }

    public static void scanLiveTvAddress(Activity context, LoadingPopupView loadingPopupView, Consumer<String> consumer) {
        if (ScanLiveTVUtils.isScanning) {
            ToastMgr.shortCenter(context, "正在扫描中，请勿重复操作");
            return;
        }
        if (loadingPopupView == null) {
            loadingPopupView = new XPopup.Builder(context).asLoading();
        }
        loadingPopupView.setTitle("正在扫描远程地址");
        loadingPopupView.show();
        LoadingPopupView finalLoadingPopupView = loadingPopupView;
        new ScanLiveTVUtils().scan(url1 -> {
            context.runOnUiThread(() -> {
                if (context.isFinishing()) {
                    return;
                }
                finalLoadingPopupView.dismiss();
                ToastMgr.shortBottomCenter(context, "扫描到远程地址：" + url1);
                if (StringUtil.isEmpty(url1)) {
                    return;
                }
                SettingConfig.setLiveTvAddress(context, url1);
                consumer.accept(url1);
            });
        }, msg -> {
            context.runOnUiThread(() -> {
                if (context.isFinishing()) {
                    return;
                }
                finalLoadingPopupView.dismiss();
                SettingConfig.setLiveTvAddress(context, "");
                ToastMgr.shortCenter(context, "没有扫描到直播地址，请确认远程软件在同一个局域网内");
            });
        });
    }

    /**
     * 尝试投屏
     *
     * @param context
     * @param loadingPopupView
     * @param address
     * @param title
     * @param url
     * @param forceRescan
     */
    private static void playWithLiveTvByAddress(Activity context, LoadingPopupView loadingPopupView, String address, String title, String url, boolean forceRescan) {
        CodeUtil.get(address + "/api/remote/version",
                new CodeUtil.OnCodeGetListener() {
                    @Override
                    public void onSuccess(String s) {
                        if (StringUtil.isEmpty(s)) {
                            playByLiveTvWithGet(context, address, title, url);
                        } else {
                            try {
                                int version = Integer.parseInt(s);
                                if (version > 0) {
                                    playByLiveTvWithPost(context, address, title, url);
                                } else {
                                    playByLiveTvWithGet(context, address, title, url);
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                playByLiveTvWithGet(context, address, title, url);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        if (forceRescan) {
                            //投屏失败，重新扫描
                            rescanLiveTv(context, loadingPopupView, title, url);
                        } else {
                            ToastMgr.shortBottomCenter(context, "投放失败，请长按重新扫描远程地址：" + msg);
                        }
                    }
                });
    }

    /**
     * 新版投屏
     *
     * @param context
     * @param address
     * @param title
     * @param url
     */
    private static void playByLiveTvWithPost(Context context, String address, String title, String url) {
        Timber.i("playByLiveTvWithPost: %s", address);
        HttpParams params = new HttpParams();
        RemotePlaySource playSource = new RemotePlaySource();
        playSource.setTitle(HttpParser.encodeUrl(title.replace(",", "_")));
        String playUrl = HttpParser.encodeUrl(LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(url)));
        playSource.setUrl(playUrl);
        params.put("JsonBody", JSON.toJSONString(playSource));
        CodeUtil.post(address + "/api/remote/update", params,
                new CodeUtil.OnCodeGetListener() {
                    @Override
                    public void onSuccess(String s) {
                        ToastMgr.shortBottomCenter(context, "已投放到远程地址：" + address);
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        ToastMgr.shortBottomCenter(context, "投放失败，请长按重新扫描远程地址：" + msg);
                    }
                });
    }

    /**
     * 旧版
     *
     * @param context
     * @param address
     * @param title
     * @param url
     */
    private static void playByLiveTvWithGet(Context context, String address, String title, String url) {
        String playUrl = HttpParser.encodeUrl(LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(url)));
        Timber.i("playByLiveTvWithGet: %s", address);
        CodeUtil.get(address + "/api/updateUrl?title=" + title.replace(",", "_") + "&url=" + playUrl,
                new CodeUtil.OnCodeGetListener() {
                    @Override
                    public void onSuccess(String s) {
                        ToastMgr.shortBottomCenter(context, "已投放到远程地址：" + address);
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        ToastMgr.shortBottomCenter(context, "投放失败，请长按重新扫描远程地址：" + msg);
                    }
                });
    }


    public static void finishBatchDlan(Activity activity, LoadingPopupView loadingPopupView, List<RemotePlaySource> playSources) {
        finishBatchDlan(activity, loadingPopupView, playSources, 0);
    }

    /**
     * 结束投屏
     *
     * @param playSources
     */
    private static void finishBatchDlan(Activity activity, LoadingPopupView loadingPopupView, List<RemotePlaySource> playSources, int redirectCount) {
        if (redirectCount > 2 || activity.isFinishing()) {
            return;
        }
        if (StringUtil.isEmpty(SettingConfig.liveTvAddress)) {
            scanLiveTvAddress(activity, loadingPopupView, addr -> {
                if(activity.isFinishing()){
                    return;
                }
                finishBatchDlan(activity, loadingPopupView, playSources, redirectCount + 1);
            });
            return;
        }
        CodeUtil.get(SettingConfig.liveTvAddress + "/api/remote/version",
                new CodeUtil.OnCodeGetListener() {
                    @Override
                    public void onSuccess(String s) {
                        if(activity.isFinishing()){
                            return;
                        }
                        if (!StringUtil.isEmpty(s)) {
                            try {
                                int version = Integer.parseInt(s);
                                if (version > 1) {
                                    HttpParams params = new HttpParams();
                                    RemotePlaySourceBatchData batchData = new RemotePlaySourceBatchData(playSources);
                                    params.put("JsonBody", JSON.toJSONString(batchData));
                                    CodeUtil.post(SettingConfig.liveTvAddress + "/api/remote/batch", params,
                                            new CodeUtil.OnCodeGetListener() {
                                                @Override
                                                public void onSuccess(String s) {

                                                }

                                                @Override
                                                public void onFailure(int errorCode, String msg) {
                                                    ToastMgr.shortCenter(activity, "批量投屏失败：" + msg);
                                                }
                                            });
                                } else {
                                    ToastMgr.shortCenter(activity, "当前世界直播版本太低，不支持批量投屏");
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastMgr.shortCenter(activity, "当前世界直播版本太低，不支持批量投屏");
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        Timber.d(msg);
                        if(activity.isFinishing()){
                            return;
                        }
                        if (redirectCount > 0) {
                            ToastMgr.shortCenter(activity, "连接世界直播失败：" + msg);
                            return;
                        }
                        scanLiveTvAddress(activity, loadingPopupView, addr -> {
                            finishBatchDlan(activity, loadingPopupView, playSources, redirectCount + 1);
                        });
                    }
                });
    }
} 
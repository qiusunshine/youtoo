package com.example.hikerview.ui.video;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.LongSparseArray;
import android.webkit.CookieManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.event.PlaySourceUpdateEvent;
import com.example.hikerview.event.home.LoadingEvent;
import com.example.hikerview.model.ViewCollection;
import com.example.hikerview.model.ViewCollectionExtraData;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.browser.webview.MultiWindowManager;
import com.example.hikerview.ui.browser.webview.WebViewHelper;
import com.example.hikerview.ui.download.DownloadDialogUtil;
import com.example.hikerview.ui.video.model.PlayData;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.ui.webdlan.LocalServerParser;
import com.example.hikerview.ui.webdlan.RemoteServerManager;
import com.example.hikerview.ui.webdlan.model.DlanUrlDTO;
import com.example.hikerview.utils.DataTransferUtils;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.WebUtil;
import com.example.hikerview.utils.view.DialogUtil;
import com.lxj.xpopup.XPopup;
import com.tencent.smtt.sdk.TbsVideo;
import com.yanzhenjie.andserver.Server;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * 作者：By hdy
 * 日期：On 2018/8/27
 * 时间：At 20:31
 */

public class PlayerChooser {
    private static final String TAG = "PlayerChooser";

    /**
     * 播放器界面单例标识
     */
    public static volatile boolean hasPlayer = false;

    public static LongSparseArray<List<VideoChapter>> getChapterMap() {
        return chapterMap;
    }

    public static LongSparseArray<List<VideoChapter>> chapterMap = new LongSparseArray<>();

    public static void startPlayer(Context context, List<VideoChapter> videoChapters) {
        startPlayer(context, videoChapters, null, null, null);
    }

    /**
     * 多选集的情况
     *
     * @param context
     * @param videoChapters   选集
     * @param CUrl
     * @param MTitle
     * @param extraDataBundle 自定义片头片尾、播放器
     */
    public static void startPlayer(Context context, List<VideoChapter> videoChapters, String CUrl, String MTitle, Bundle extraDataBundle) {
        if (context == null) {
            return;
        }
        VideoChapter chapter = null;
        int nowPos = 0;
        if (videoChapters == null || videoChapters.size() <= 0) {
            ToastMgr.shortBottomCenter(context, "选集有误");
            return;
        }
        for (int i = 0; i < videoChapters.size(); i++) {
            VideoChapter videoChapter = videoChapters.get(i);
            if (videoChapter.isUse()) {
                chapter = videoChapter;
                nowPos = i;
                break;
            }
        }
        if (chapter == null) {
            ToastMgr.shortBottomCenter(context, "没有选中的集数");
            return;
        }

        // 判断设置的播放器
        int player = PreferenceMgr.getInt(context, "player", PlayerEnum.PLAYER_TWO.getCode());
        if (StringUtil.isNotEmpty(chapter.getUrl()) && chapter.getUrl().startsWith("x5Play://")) {
            player = PlayerEnum.X5.getCode();
        }
        try {
            if (extraDataBundle != null) {
                ViewCollectionExtraData extraData = ViewCollectionExtraData.extraDataFromJson(extraDataBundle.getString("viewCollectionExtraData"));
                if (extraData.isCustomPlayer()) {
                    player = extraData.getPlayer();
                }
            }
        } catch (Exception ignored) {
        }
        player = reSetPlayer(player, chapter.getUrl());
        if (player != PlayerEnum.PLAYER_TWO.getCode()) {
            Log.d(TAG, "startPlayer: " + player);
            boolean thirdPlayerNotGoDefault = PreferenceMgr.getBoolean(context, "thirdPlayerNotGoDefault", false);
            Log.d(TAG, "thirdPlayerNotGoDefault: " + thirdPlayerNotGoDefault);
            if (!thirdPlayerNotGoDefault) {
                long current = System.currentTimeMillis();
                chapterMap.put(current, videoChapters);
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("title", chapter.getTitle());
                intent.putExtra("videourl", chapter.getUrl());
                intent.putExtra("player", player);
                intent.putExtra("chapters", current);
                intent.putExtra("nowPos", nowPos);
                if (extraDataBundle != null) {
                    checkSize(extraDataBundle);
                    intent.putExtra("extraDataBundle", extraDataBundle);
                }
                if (CUrl != null && MTitle != null) {
                    intent.putExtra("CUrl", CUrl);
                    intent.putExtra("MTitle", MTitle);
                }
                startVideoPlayerActivity(context, intent);
            } else {
                String playerName = PlayerEnum.findName(player);
                if (StringUtil.isNotEmpty(playerName)) {
                    startPlayer(context, playerName, chapter.getTitle(), chapter.getUrl(), null);
                } else {
                    ToastMgr.shortBottomCenter(context, "获取播放器失败：" + player);
                }
            }
        } else {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("title", chapter.getTitle());
            intent.putExtra("videourl", chapter.getUrl());
            long current = System.currentTimeMillis();
            chapterMap.put(current, videoChapters);
            intent.putExtra("chapters", current);
            intent.putExtra("nowPos", nowPos);
            if (CUrl != null && MTitle != null) {
                intent.putExtra("CUrl", CUrl);
                intent.putExtra("MTitle", MTitle);
            }
            if (extraDataBundle != null) {
                checkSize(extraDataBundle);
                intent.putExtra("extraDataBundle", extraDataBundle);
            }
            if (UrlDetector.isMusic(chapter.getUrl())) {
                if (context.getPackageManager().resolveActivity(intent, 0) == null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }
            startVideoPlayerActivity(context, intent);
        }
    }

    private static void checkSize(Bundle extraDataBundle) {
        if (extraDataBundle == null || !extraDataBundle.containsKey("rule")) {
            return;
        }
        String rule = extraDataBundle.getString("rule");
        int byteCount = rule.getBytes().length;
        Timber.d("变量传递, length: %s", byteCount);
        if (byteCount > 1024 * 100) {
            extraDataBundle.remove("rule");
            DataTransferUtils.INSTANCE.putCacheString(rule, "tempVideoRule");
        }
    }

    /**
     * 无选集情况
     *
     * @param context
     * @param title
     * @param movieUrl
     */
    public static void startPlayer(Context context, String title, String movieUrl) {
        int player = PreferenceMgr.getInt(context, "player", PlayerEnum.PLAYER_TWO.getCode());
        if (StringUtil.isNotEmpty(movieUrl) && movieUrl.startsWith("x5Play://")) {
            player = PlayerEnum.X5.getCode();
        }
        player = reSetPlayer(player, movieUrl);
        title = reSetPlayTitle(title, movieUrl);
        if (player != PlayerEnum.PLAYER_TWO.getCode()) {
            Log.d(TAG, "startPlayer: " + player);
            boolean thirdPlayerNotGoDefault = PreferenceMgr.getBoolean(context, "thirdPlayerNotGoDefault", false);
            Log.d(TAG, "thirdPlayerNotGoDefault: " + thirdPlayerNotGoDefault);
            if (!thirdPlayerNotGoDefault) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("videourl", movieUrl);
                intent.putExtra("player", player);
                startVideoPlayerActivity(context, intent);
            } else {
                String playerName = PlayerEnum.findName(player);
                if (StringUtil.isNotEmpty(playerName)) {
                    startPlayer(context, playerName, title, movieUrl, null);
                } else {
                    ToastMgr.shortBottomCenter(context, "获取播放器失败：" + player);
                }
            }
        } else {
            startPlayer(context, PlayerEnum.PLAYER_TWO.getName(), title, movieUrl, null);
        }
    }

    public static String decorateHeader(Activity activity, String referer, String videoUrl) {
        HorizontalWebView webView = MultiWindowManager.instance(activity).getCurrentWebView();
        return decorateHeader(WebViewHelper.getRequestHeaderMap(webView, videoUrl), referer, videoUrl);
    }

    public static String decorateHeader(@Nullable Map<String, String> headers, String referer, String videoUrl) {
        String cookie = "";
        try {
            if (StringUtil.isNotEmpty(referer)) {
                cookie = CookieManager.getInstance().getCookie(referer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decorateHeader(headers, referer, videoUrl, cookie);
    }

    public static String decorateHeader(@Nullable Map<String, String> headers, String referer, String videoUrl, String cookie) {
        if (StringUtil.isEmpty(videoUrl)) {
            return videoUrl;
        }
        Map<String, String> hd = new HashMap<>();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if ("Accept".equalsIgnoreCase(entry.getKey())) {
                    continue;
                }
                if ("Range".equalsIgnoreCase(entry.getKey())) {
                    continue;
                }
                if ("Upgrade-Insecure-Requests".equalsIgnoreCase(entry.getKey())) {
                    continue;
                }
                hd.put(entry.getKey(), entry.getValue());
            }
        }
        if (StringUtil.isNotEmpty(cookie) && !hd.containsKey("Cookie")) {
            hd.put("Cookie", cookie);
        }
        if (hd.isEmpty()) {
            return videoUrl;
        }
        return videoUrl + ";" + HttpParser.getHeadersStr(hd);
    }

    /**
     * 无选集情况
     *
     * @param context
     * @param title
     * @param movieUrl
     * @param extraDataBundle 自定义片头片尾、播放器
     */
    public static void startPlayer(Context context, String title, String movieUrl, Bundle extraDataBundle) {
        // 判断设置的播放器
        int player = PreferenceMgr.getInt(context, "player", PlayerEnum.PLAYER_TWO.getCode());
        if (StringUtil.isNotEmpty(movieUrl) && movieUrl.startsWith("x5Play://")) {
            player = PlayerEnum.X5.getCode();
        }
        player = reSetPlayer(player, movieUrl);
        title = reSetPlayTitle(title, movieUrl);
        try {
            if (extraDataBundle != null) {
                ViewCollectionExtraData extraData = ViewCollectionExtraData.extraDataFromJson(extraDataBundle.getString("viewCollectionExtraData"));
                if (extraData != null && extraData.isCustomPlayer()) {
                    player = extraData.getPlayer();
                }
            }
        } catch (Exception ignored) {
        }
        if (player != PlayerEnum.PLAYER_TWO.getCode()) {
            Log.d(TAG, "startPlayer: " + player);
            boolean thirdPlayerNotGoDefault = PreferenceMgr.getBoolean(context, "thirdPlayerNotGoDefault", false);
            Log.d(TAG, "thirdPlayerNotGoDefault: " + thirdPlayerNotGoDefault);
            if (!thirdPlayerNotGoDefault) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("videourl", movieUrl);
                intent.putExtra("player", player);
                startVideoPlayerActivity(context, intent);
            } else {
                String playerName = PlayerEnum.findName(player);
                if (StringUtil.isNotEmpty(playerName)) {
                    startPlayer(context, playerName, title, movieUrl, null);
                } else {
                    ToastMgr.shortBottomCenter(context, "获取播放器失败：" + player);
                }
            }
        } else {
            startPlayer(context, PlayerEnum.PLAYER_TWO.getName(), title, movieUrl, extraDataBundle);
        }
    }

    private static String reSetPlayTitle(String title, String movieUrl) {
        if (StringUtils.equals(title, movieUrl) || StringUtil.isEmpty(title)) {
            title = DownloadDialogUtil.getFileName(movieUrl);
            if (StringUtil.isNotEmpty(title) && title.contains("%")) {
                //可能是中文URL编码，解码一下
                title = HttpParser.decodeUrl(title, "UTF-8");
            }
        }
        return title;
    }

    private static int reSetPlayer(int player, String movieUrl) {
        if (UrlDetector.isMusic(movieUrl)) {
            player = PlayerEnum.PLAYER_TWO.getCode();
        }
        return player;
    }

    static boolean startPlayer(Context context, String player, String title, String url, Bundle extraDataBundle) {
        PlayData playData = HttpParser.getPlayData(url);
        String originalUrl = url;
        Map<String, String> headers = HttpParser.getHeaders(url, playData, 0);
        if (CollectionUtil.isNotEmpty(playData.getUrls())) {
            url = playData.getUrls().get(0);
        }
        url = UrlDetector.clearTag(url);
        return startPlayer(context, player, title, url, originalUrl, headers, playData.getSubtitle(), extraDataBundle);
    }

    /**
     * 使用第三方播放器
     *
     * @param context
     * @param player
     * @param title
     * @param url
     * @param extraDataBundle
     * @return
     */
    static boolean startPlayer(Context context, String player, String title, String url, String originalUrl,
                               Map<String, String> headers, String subtitle, Bundle extraDataBundle) {
        url = LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(url));
        if (player.equals(PlayerEnum.X5.getName())) {
            startX5(context, title, url);
            return true;
        } else if (player.equals(PlayerEnum.MX_PLAYER.getName())) {
            return startMxPlayer(context, title, url, headers, subtitle);
        } else if (player.equals(PlayerEnum.X_PLAYER.getName())) {
            return startXPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.KM_PLAYER.getName())) {
            return startKMPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.MO_BO_PLAYER.getName())) {
            return startMoboPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.QQ_PLAYER.getName())) {
            return startQQPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.VLC_PLAYER.getName())) {
            return startVLC(context, title, url);
        } else if (player.equals(PlayerEnum.UC_PLAYER.getName())) {
            return startUCPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.UC_INTL_PLAYER.getName())) {
            return startUCIntlPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.DAN_DAN_PLAYER.getName())) {
            return startDDPlayPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.N_PLAYER.getName())) {
            return startNPlayPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.WEB_VIDEO_CASTER.getName())) {
            return startWVCaster(context, title, url);
        } else if (player.equals(PlayerEnum.LUA_PLAYER.getName())) {
            return startLuaPlayer(context, title, url);
        } else if (player.equals(PlayerEnum.KODI.getName())) {
            return startKodi(context, title, url);
        } else if (player.equals(PlayerEnum.REEX.getName())) {
            return startReex(context, title, url, headers, subtitle);
        } else if (player.equals(PlayerEnum.SYSTEM.getName())) {
            return startSystemPlayer(context, title, url, headers, subtitle);
        } else {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("videourl", originalUrl);
            if (extraDataBundle != null) {
                checkSize(extraDataBundle);
                intent.putExtra("extraDataBundle", extraDataBundle);
            }
            startVideoPlayerActivity(context, intent);
            return true;
        }
    }

    /**
     * 实现播放器界面单例
     *
     * @param context
     * @param intent
     */
    private static void startVideoPlayerActivity(Context context, Intent intent) {
        if (hasPlayer) {
            EventBus.getDefault().post(new LoadingEvent("", false));
            EventBus.getDefault().post(new PlaySourceUpdateEvent(context, intent));
            if(VideoPlayerActivity.isMusic && !UrlDetector.isMusic(intent.getStringExtra("videourl"))){
                try {
                    context.startActivity(intent);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                context.startActivity(intent);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static long putChapters(List<VideoChapter> chapters) {
        long current = System.currentTimeMillis();
        chapterMap.put(current, chapters);
        return current;
    }

    public interface PlayerInterceptor {
        boolean intercept(String playerName);
    }

    static void startMultiPlayer(final Context context, final String title, String url, String originalUrl,
                                 Map<String, String> headers, String subtitle, PlayerInterceptor playerInterceptor) {
        final PlayerEnum[] players = PlayerEnum.values();
        final String movieUrl = url;
        String[] names = new String[players.length + 2];
        for (int i = 0; i < players.length; i++) {
            names[i] = players[i].getName();
        }
        names[names.length - 2] = "网页播放";
        names[names.length - 1] = "其它播放器";
        final int otherPos = names.length - 2;
        AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle("选择播放器")
                .setSingleChoiceItems(names, 0, (dialog, which) -> {
                    String url1 = movieUrl;
                    if (otherPos == which) {
                        url1 = getThirdPlaySource(url1);
                        WebUtil.goWeb(context, url1);
                    } else if (otherPos + 1 == which) {
                        url1 = getThirdPlaySource(url1);
                        ShareUtil.findVideoPlayerToDeal(context, url1);
                    } else {
                        if (playerInterceptor != null && playerInterceptor.intercept(players[which].getName())) {
                            dialog.dismiss();
                            return;
                        }
                        startPlayer(context, players[which].getName(), title, url1, originalUrl, headers, subtitle, null);
                    }
                    dialog.dismiss();
                })
                .setOnCancelListener(dialog -> {
                }).setNegativeButton("取消", (dialog, which) -> {
                }).create();
        DialogUtil.INSTANCE.showAsCard(context, alertDialog);
    }

    public static void setDefaultPlayer(Activity context, String pageFrom) {
        setDefaultPlayer(context, pageFrom, null);
    }

    // 设置默认播放器
    public static void setDefaultPlayer(Activity context, String pageFrom, ViewCollection viewCollection) {
        final PlayerEnum[] players = PlayerEnum.values();
        int player = PlayerEnum.PLAYER_TWO.getCode();
        ViewCollectionExtraData extraData = null;

        if (viewCollection == null) {
            // 不是从收藏进入，使用全局
            player = PreferenceMgr.getInt(context, "player", PlayerEnum.PLAYER_TWO.getCode());
        } else {
            try {
                extraData = ViewCollectionExtraData.extraDataFromJson(viewCollection.getExtraData());
                if (extraData == null) {
                    extraData = new ViewCollectionExtraData(viewCollection.getId());
                }
            } catch (Exception e) {
                extraData = new ViewCollectionExtraData(viewCollection.getId());
                e.printStackTrace();
            }
            // 不需要管 extraData 是不是 null，因为 null 时上面会将 isCustomPlayer 初始化为 false
            // 并且可以达到没设置过的就显示全局播放器的效果
            if (extraData.isCustomPlayer()) {
                player = extraData.getPlayer();
            } else {
                player = PreferenceMgr.getInt(context, "player", PlayerEnum.PLAYER_TWO.getCode());
            }
        }

        String[] names = new String[players.length];
        int checked = 0;
        for (int i = 0; i < players.length; i++) {
            names[i] = players[i].getName();
            if (players[i].getCode() == player) {
                checked = i;
            }
        }

        int finalChecked = checked;
        ViewCollectionExtraData finalExtraData = extraData;

        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("选择播放器（请确保已安装相应软件）", names,
                        null, finalChecked,
                        (position, text) -> {
                            String toastStr = "";
                            // tvCustomPlayer.setText(players[position].getName());
                            switch (pageFrom) {
                                case "setting":
                                    PreferenceMgr.put(context, "player", players[position].getCode());
                                    toastStr = "全局播放器已设置为" + players[position].getName();
                                    ToastMgr.shortBottomCenter(context, toastStr);
                                    break;
                                case "collection":
                                    setPlayer(context, viewCollection, finalExtraData, players[position]);
                                    break;
                                case "videoPlayer":
                                    new XPopup.Builder(context).asConfirm("温馨提示",
                                            "当前正在播放器页面，必须退出当前页面才能使设置生效，是否退出当前页面并且设置播放器为" + players[position].getName() + "？", () -> {
                                                setPlayer(context, viewCollection, finalExtraData, players[position]);
                                                context.finish();
                                            }).show();
                                    break;
                            }
                        })
                .show();
    }

    private static void setPlayer(Activity context, ViewCollection viewCollection, ViewCollectionExtraData extraData, PlayerEnum player) {
        String toastStr = "";
        if (viewCollection != null) {
            extraData.setCustomPlayer(true);
            extraData.setPlayer(player.getCode());
            extraData.setCollectionId(viewCollection.getId());
            viewCollection.setExtraData(ViewCollectionExtraData.extraDataToJson(extraData));
            viewCollection.save();
            toastStr = "当前片单播放器已设置为" + player.getName();
        } else {
            PreferenceMgr.put(context, "player", player.getCode());
            toastStr = "全局播放器已设置为" + player.getName();
        }
        ToastMgr.shortBottomCenter(context, toastStr);
    }

    public static String getThirdPlaySource(String url) {
        if (StringUtil.isEmpty(url)) {
            return url;
        }
        //调用第三方播放器只能移除header信息
        return url.split(";")[0];
    }

    private static boolean startLuaPlayer(Context context, String title, String url) {
        url = getThirdPlaySource(url);
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.putExtra("url", title);
        paramBundle.putExtra("externalPlay:extractText", title);
        paramBundle.setDataAndType(FilesInAppUtil.getUri(context, url), "video/*");
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context, "ms.dev.luaplayer_pro")) {
            try {
                paramBundle.setComponent(new ComponentName("ms.dev.luaplayer_pro", "ms.dev.activity.AVExternalActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        ToastMgr.shortBottomCenter(context, "没有安装 Lua Player！" + message);
        return false;
    }

    private static boolean startNPlayPlayer(Context context, String title, String url) {
        url = getThirdPlaySource(url);
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FilesInAppUtil.getUri(context, url));
        paramBundle.putExtra("fileName", title);
        paramBundle.putExtra("file_name", title);
        paramBundle.putExtra("playPath", url);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context, "com.newin.nplayer.pro")) {
            try {
                paramBundle.setComponent(new ComponentName("com.newin.nplayer.pro", "com.newin.nplayer.activities.BridgeActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        ToastMgr.shortBottomCenter(context, "没有安装nPlayer！" + message);
        return false;
    }

    private static boolean startDDPlayPlayer(Context context, String title, String url) {
        url = getThirdPlaySource(url);
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FilesInAppUtil.getUri(context, url));
        paramBundle.putExtra("video_title", title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context, "com.xyoye.dandanplay")) {
            try {
                paramBundle.setComponent(new ComponentName("com.xyoye.dandanplay", "com.xyoye.player_component.ui.activities.player_intent.PlayerIntentActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
            try {
                paramBundle.setComponent(new ComponentName("com.xyoye.dandanplay", "com.xyoye.dandanplay.ui.activities.play.PlayerManagerActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
            try {
                paramBundle.setComponent(new ComponentName("com.xyoye.dandanplay", "com.xyoye.dandanplay.ui.activities.PlayerManagerActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        ToastMgr.shortBottomCenter(context, "没有安装弹弹Play！" + message);
        return false;
    }

    public static void startX5(Context context, String title, String url) {
        if (url.startsWith("http://127.0.0.1")) {
            ToastMgr.shortBottomCenter(context, "X5播放器不支持此视频");
            return;
        }
//        TbsPlayerActivity.start(context, title, url, null, 0);
        url = getThirdPlaySource(url);
        Bundle data = new Bundle();
        data.putInt("screenMode", 102);
        data.putBoolean("supportLiteWnd", true);
        data.putBoolean("standardFullScreen", false);
        TbsVideo.openVideo(context, url, data);
    }

    private static boolean startMxPlayer(Context context, String title, String url, Map<String, String> headers, String subtitle) {
        url = getThirdPlaySource(url);
        Intent paramBundle = new Intent();
        if (headers != null && !headers.isEmpty()) {
            List<String> hd = new ArrayList<>();
            List<String> hdArray = new ArrayList<>();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                hd.add(HttpParser.encodeUrl(entry.getKey()) + "=" + HttpParser.encodeUrl(entry.getValue()));
                hdArray.add(entry.getKey());
                hdArray.add(entry.getValue());
            }
//            url = url + "##|" + CollectionUtil.listToString(hd, "&");
            paramBundle.putExtra("headers", CollectionUtil.toStrArray(hdArray));
        }
        if(StringUtil.isNotEmpty(subtitle)){
            subtitle = initSubtitleUrl(context, title, url, headers, subtitle);
            paramBundle.putExtra("subs", new Parcelable[]{Uri.parse(subtitle)});
            paramBundle.putExtra("subs.name", new String[]{"外挂字幕"});
            paramBundle.putExtra("subs.enable", new Parcelable[]{Uri.parse(subtitle)});
        }
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FilesInAppUtil.getUri(context, url));
        paramBundle.putExtra("title", title);
        paramBundle.putExtra("name", title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context, "com.mxtech.videoplayer.ad")) {
            try {
                paramBundle.setComponent(new ComponentName("com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.ad.ActivityScreen"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        if (appInstalledOrNot(context, "com.mxtech.videoplayer.pro")) {
            try {
                paramBundle.setComponent(new ComponentName("com.mxtech.videoplayer.pro", "com.mxtech.videoplayer.ActivityScreen"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = message + "____" + e.getMessage();
            }
        }
        ToastMgr.shortBottomCenter(context, "没有安装MXPlayer！" + message);
        return false;
    }

    private static boolean startMoboPlayer(Context context, String title, String url) {
        url = getThirdPlaySource(url);
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FilesInAppUtil.getUri(context, url));
        paramBundle.putExtra("title", title);
        paramBundle.putExtra("name", title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context, "com.clov4r.android.nil.noad")) {
            try {
                paramBundle.setComponent(new ComponentName("com.clov4r.android.nil.noad", "com.clov4r.android.nil.ui.activity.MainActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        if (appInstalledOrNot(context, "com.clov4r.android.nil")) {
            try {
                paramBundle.setComponent(new ComponentName("com.clov4r.android.nil", "com.clov4r.android.nil.ui.activity.MainActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = message + "____" + e.getMessage();
            }
        }
        ToastMgr.shortBottomCenter(context, "没有安装MoboPlayer！" + message);
        return false;
    }

    private static boolean startKMPlayer(Context context, String title, String url) {
        url = getThirdPlaySource(url);
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FilesInAppUtil.getUri(context, url));
        paramBundle.putExtra("title", title);
        paramBundle.putExtra("name", title);
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context, "com.kmplayerpro")) {
            try {
                paramBundle.setComponent(new ComponentName("com.kmplayerpro", "com.kmplayer.activity.VideoPlayerActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        if (appInstalledOrNot(context, "com.kmplayer")) {
            try {
                paramBundle.setComponent(new ComponentName("com.kmplayer", "com.kmplayer.MainActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = message + "____" + e.getMessage();
            }
        }
        if (appInstalledOrNot(context, "com.kmplayerpro")) {
            try {
                paramBundle.setComponent(new ComponentName("com.kmplayerpro", "com.kmplayer.MainActivity"));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        ToastMgr.shortBottomCenter(context, "没有安装KMPlayer！" + message);
        return false;
    }

    private static String initSubtitleUrl(Context context, String title, String url, Map<String, String> headers, String subtitle){
        if ((subtitle.startsWith("hiker") || subtitle.startsWith("file"))) {
            if(RemoteServerManager.instance().getUrlDTO() != null && StringUtils.equals(subtitle, RemoteServerManager.instance().getUrlDTO().getSubtitle())){
                //已经开启
            } else {
                //开启AndServer
                DlanUrlDTO urlDTO = new DlanUrlDTO(url, headers,
                        0, 0);
                urlDTO.setTitle(title);
                urlDTO.setSubtitle(subtitle);
                RemoteServerManager.instance().setUrlDTO(urlDTO);
                try {
                    RemoteServerManager.instance().startServer(context, new Server.ServerListener() {
                        @Override
                        public void onStarted() {

                        }

                        @Override
                        public void onStopped() {

                        }

                        @Override
                        public void onException(Exception e) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return RemoteServerManager.instance().getServerUrl(Application.application) + "/file?name=subtitle." + FileUtil.getExtension(subtitle);
        }
        return subtitle;
    }

    private static boolean startSystemPlayer(Context context, String title, String url, Map<String, String> headers, String subtitle) {
        Intent intent = new Intent();
        intent.putExtra("title", title);
        intent.putExtra("name", title);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);//允许临时的读和写
        Uri content_url = FilesInAppUtil.getUri(context, url);
        String type = UrlDetector.isMusic(url) ? "audio/*" : "video/*";
        intent.setDataAndType(content_url, type);
        context.startActivity(intent);
        return true;
    }

    private static boolean startReex(Context context, String title, String url, Map<String, String> headers, String subtitle) {
        Map<String, String> extra = new HashMap<>();
        if (headers != null) {
            extra.put("reex.extra.http_header", JSON.toJSONString(headers));
        }
        if (StringUtil.isNotEmpty(subtitle)) {
            subtitle = initSubtitleUrl(context, title, url, headers, subtitle);
            extra.put("reex.extra.subtitle", subtitle);
        }
        return startPlayerById(context, title, url, "Reex", extra, "xyz.re.player.ex", "xyz.re.player.ex.MainActivity");
    }

    private static boolean startKodi(Context context, String title, String url) {
        return startPlayerById(context, title, url, "Kodi", "org.xbmc.kodi", "org.xbmc.kodi.Splash");
    }


    private static boolean startUCPlayer(Context context, String title, String url) {
        return startPlayerById(context, title, url, "UC浏览器", "com.UCMobile", "com.UCMobile.main.UCMobile");
    }

    private static boolean startUCIntlPlayer(Context context, String title, String url) {
        return startPlayerById(context, title, url, "UC国际版", "com.UCMobile.intl", "com.UCMobile.main.UCMobile");
    }

    private static boolean startXPlayer(Context context, String title, String url) {
        return startPlayerById(context, title, url, "XPlayer", "video.player.videoplayer", "com.inshot.xplayer.activities.PlayerActivity");
    }

    private static boolean startVLC(Context context, String title, String url) {
        return startPlayerById(context, title, url, "VLC", "org.videolan.vlc", "org.videolan.vlc.StartActivity");
    }

    private static boolean startQQPlayer(Context context, String title, String url) {
        return startPlayerById(context, title, url, "QQ浏览器", "com.tencent.mtt", "com.tencent.mtt.browser.video.H5VideoThrdcallActivity");
    }

    private static boolean startWVCaster(Context context, String title, String url) {
        return startPlayerById(context, title, url, "Web Video Caster", "com.instantbits.cast.webvideo", "com.instantbits.cast.webvideo.WebBrowser");
    }


    private static boolean startPlayerById(Context context, String title, String url, String playerName, String packageName, String activityName) {
        return startPlayerById(context, title, url, playerName, null, packageName, activityName);
    }

    private static boolean startPlayerById(Context context, String title, String url, String playerName,
                                           @Nullable Map<String, String> extra, String packageName, String activityName) {
        url = getThirdPlaySource(url);
        Intent paramBundle = new Intent();
        paramBundle.setAction("android.intent.action.VIEW");
        paramBundle.setData(FilesInAppUtil.getUri(context, url));
        paramBundle.putExtra("title", title);
        paramBundle.putExtra("name", title);
        if (extra != null) {
            for (Map.Entry<String, String> entry : extra.entrySet()) {
                paramBundle.putExtra(entry.getKey(), entry.getValue());
            }
        }
        paramBundle.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        String message = "";
        if (appInstalledOrNot(context, packageName)) {
            try {
                paramBundle.setComponent(new ComponentName(packageName, activityName));
                context.startActivity(paramBundle);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                message = e.getMessage();
            }
        }
        ToastMgr.shortBottomCenter(context, "没有安装" + playerName + "！" + message);
        return false;
    }

    public static boolean appInstalledOrNot(Context context, String paramString) {
        try {
            PackageManager packageManager = context.getPackageManager();// 获取packagemanager
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName;
                    if (paramString.equals(pn)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PackageManager pm = context.getPackageManager();
            try {
                pm.getPackageInfo(paramString, PackageManager.GET_ACTIVITIES);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}

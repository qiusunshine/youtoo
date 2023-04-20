package com.example.hikerview.ui.browser;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.hikerview.utils.PreferenceMgr.SETTING_CONFIG;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.request.RequestOptions;
import com.example.hikerview.R;
import com.example.hikerview.constants.CollectionTypeConstant;
import com.example.hikerview.constants.Media;
import com.example.hikerview.constants.MediaType;
import com.example.hikerview.constants.RemotePlayConfig;
import com.example.hikerview.event.FindVideoEvent;
import com.example.hikerview.event.OnUrlChangeEvent;
import com.example.hikerview.event.SearchEvent;
import com.example.hikerview.event.ShowToastMessageEvent;
import com.example.hikerview.event.ToastMessage;
import com.example.hikerview.event.WebViewUrlChangedEvent;
import com.example.hikerview.event.home.LoadingEvent;
import com.example.hikerview.event.home.ToastEvent;
import com.example.hikerview.event.video.BackMainEvent;
import com.example.hikerview.event.web.BlobDownloadEvent;
import com.example.hikerview.event.web.BlobDownloadProgressEvent;
import com.example.hikerview.event.web.DownloadStartEvent;
import com.example.hikerview.event.web.FindMagnetsEvent;
import com.example.hikerview.event.web.FloatVideoChangeEvent;
import com.example.hikerview.event.web.OnBookmarkUpdateEvent;
import com.example.hikerview.event.web.OnCreateWindowEvent;
import com.example.hikerview.event.web.OnFindInfoEvent;
import com.example.hikerview.event.web.OnHideCustomViewEvent;
import com.example.hikerview.event.web.OnImgHrefFindEvent;
import com.example.hikerview.event.web.OnLoadUrlEvent;
import com.example.hikerview.event.web.OnLongClickEvent;
import com.example.hikerview.event.web.OnMenuItemClickEvent;
import com.example.hikerview.event.web.OnOverrideUrlLoadingForOther;
import com.example.hikerview.event.web.OnPageFinishedEvent;
import com.example.hikerview.event.web.OnPageStartEvent;
import com.example.hikerview.event.web.OnProgressChangedEvent;
import com.example.hikerview.event.web.OnSaveAdBlockRuleEvent;
import com.example.hikerview.event.web.OnSetAdBlockEvent;
import com.example.hikerview.event.web.OnSetWebTitleEvent;
import com.example.hikerview.event.web.OnShortcutUpdateEvent;
import com.example.hikerview.event.web.OnShowCustomViewEvent;
import com.example.hikerview.event.web.OnShowFileChooserEvent;
import com.example.hikerview.event.web.ShowSearchEvent;
import com.example.hikerview.event.web.ShowTranslateEvent;
import com.example.hikerview.event.web.UpdateBgEvent;
import com.example.hikerview.model.AdBlockRule;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.model.Bookmark;
import com.example.hikerview.model.DownloadRecord;
import com.example.hikerview.service.auth.AuthBridgeEvent;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.service.subscribe.AdUrlSubscribe;
import com.example.hikerview.ui.ActivityManager;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.bookmark.BookmarkActivity;
import com.example.hikerview.ui.browser.data.DomainConfigKt;
import com.example.hikerview.ui.browser.data.MagnetData;
import com.example.hikerview.ui.browser.data.TabHistory;
import com.example.hikerview.ui.browser.enums.ShortcutTypeEnum;
import com.example.hikerview.ui.browser.model.AdBlockModel;
import com.example.hikerview.ui.browser.model.AdUrlBlocker;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.ui.browser.model.DetectorManager;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.browser.model.Shortcut;
import com.example.hikerview.ui.browser.model.UAModel;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.browser.model.VideoTask;
import com.example.hikerview.ui.browser.service.BrowserProxy;
import com.example.hikerview.ui.browser.service.DomainConfigService;
import com.example.hikerview.ui.browser.service.JSUpdaterKt;
import com.example.hikerview.ui.browser.service.PoetryService;
import com.example.hikerview.ui.browser.service.UpdateEvent;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.browser.util.HomeConfigUtil;
import com.example.hikerview.ui.browser.view.BaseWebViewActivity;
import com.example.hikerview.ui.browser.view.BrowserMenuPopup;
import com.example.hikerview.ui.browser.view.BrowserSubMenuPopup;
import com.example.hikerview.ui.browser.view.DomainConfigPopup;
import com.example.hikerview.ui.browser.view.IconFloatButton;
import com.example.hikerview.ui.browser.view.ImagesViewerPopup;
import com.example.hikerview.ui.browser.view.JSUpdatePopup;
import com.example.hikerview.ui.browser.view.MultiWondowTextPopup;
import com.example.hikerview.ui.browser.view.MyCaptureActivity;
import com.example.hikerview.ui.browser.view.ShortcutAdapter;
import com.example.hikerview.ui.browser.view.TranslatePopup;
import com.example.hikerview.ui.browser.view.VideoContainer;
import com.example.hikerview.ui.browser.webview.MultiWindowManager;
import com.example.hikerview.ui.browser.webview.WebViewHelper;
import com.example.hikerview.ui.download.DownloadChooser;
import com.example.hikerview.ui.download.DownloadDialogUtil;
import com.example.hikerview.ui.download.DownloadManager;
import com.example.hikerview.ui.download.DownloadRecordsActivity;
import com.example.hikerview.ui.home.ArticleListRuleEditActivity;
import com.example.hikerview.ui.home.reader.EpubFile;
import com.example.hikerview.ui.js.AdListActivity;
import com.example.hikerview.ui.js.AdUrlListActivity;
import com.example.hikerview.ui.js.JSListActivity;
import com.example.hikerview.ui.miniprogram.MiniProgramRouter;
import com.example.hikerview.ui.miniprogram.data.RuleDTO;
import com.example.hikerview.ui.miniprogram.service.AutoCacheUtilKt;
import com.example.hikerview.ui.picture.PictureOnlineActivity;
import com.example.hikerview.ui.search.GlobalSearchPopup;
import com.example.hikerview.ui.search.engine.SearchEngineMagActivity;
import com.example.hikerview.ui.search.model.SearchHistroyModel;
import com.example.hikerview.ui.setting.CollectionListActivity;
import com.example.hikerview.ui.setting.HistoryListActivity;
import com.example.hikerview.ui.setting.MoreSettingActivity;
import com.example.hikerview.ui.setting.MoreSettingMenuPopup;
import com.example.hikerview.ui.setting.SettingMenuPopup;
import com.example.hikerview.ui.setting.TextSizeActivity;
import com.example.hikerview.ui.setting.UAListActivity;
import com.example.hikerview.ui.setting.model.SearchModel;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.setting.office.AboutOfficer;
import com.example.hikerview.ui.setting.office.AdblockOfficer;
import com.example.hikerview.ui.setting.office.DownloadOfficer;
import com.example.hikerview.ui.setting.office.MiniProgramOfficer;
import com.example.hikerview.ui.setting.office.MoreSettingOfficer;
import com.example.hikerview.ui.setting.office.MoreSettingOfficerKt;
import com.example.hikerview.ui.setting.office.NormalSettingOfficer;
import com.example.hikerview.ui.setting.office.XiuTanOfficer;
import com.example.hikerview.ui.setting.updaterecords.UpdateRecord;
import com.example.hikerview.ui.setting.updaterecords.UpdateRecordsActivity;
import com.example.hikerview.ui.setting.webdav.WebDavBackupUtil;
import com.example.hikerview.ui.thunder.ThunderManager;
import com.example.hikerview.ui.video.EmptyActivity;
import com.example.hikerview.ui.video.FloatVideoController;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.view.CustomBottomPopup;
import com.example.hikerview.ui.view.CustomColorPopup;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.ui.view.MutiWondowAdapter;
import com.example.hikerview.ui.view.MutiWondowPopup;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.ui.view.RelativeListenLayout;
import com.example.hikerview.ui.view.XiuTanResultPopup;
import com.example.hikerview.ui.view.animate.AnimateTogetherUtils;
import com.example.hikerview.ui.view.colorDialog.ColorDialog;
import com.example.hikerview.ui.view.popup.InputConfirmPopup;
import com.example.hikerview.ui.view.popup.MyXpopup;
import com.example.hikerview.ui.view.popup.ShortcutInputPopup;
import com.example.hikerview.ui.view.popup.SimpleHintPopupWindow;
import com.example.hikerview.utils.AlertNewVersionUtil;
import com.example.hikerview.utils.AndroidBarUtils;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.BackupUtil;
import com.example.hikerview.utils.CleanMessageUtil;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.FilesInAppUtil;
import com.example.hikerview.utils.GlideUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.HttpUtil;
import com.example.hikerview.utils.ImgUtil;
import com.example.hikerview.utils.NotifyManagerUtils;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.RandomUtil;
import com.example.hikerview.utils.ScreenUtil;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StatusBarCompatUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ThreadTool;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.UriUtils;
import com.example.hikerview.utils.WebUtil;
import com.example.hikerview.utils.permission.PermissionConstants;
import com.example.hikerview.utils.permission.XPermission;
import com.example.hikerview.utils.view.DialogUtil;
import com.example.hikerview.utils.view.ImageUtilKt;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;
import com.king.app.updater.constant.Constants;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.lxj.xpopup.util.KeyboardUtils;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.xunlei.downloadlib.parameter.TorrentFileInfo;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.decode.DecodeImgCallback;
import com.yzq.zxinglibrary.decode.DecodeImgThread;

import org.adblockplus.libadblockplus.android.AdblockEngine;
import org.adblockplus.libadblockplus.android.Utils;
import org.adblockplus.libadblockplus.android.settings.AdblockHelper;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chuangyuan.ycj.videolibrary.video.VideoPlayerManager;
import kotlin.Unit;
import me.jingbin.progress.WebProgress;
import timber.log.Timber;

/**
 * 作者：By hdy
 * 日期：On 2018/3/13
 * 时间：At 20:10
 */

public class WebViewActivity extends BaseWebViewActivity implements View.OnClickListener {
    private static final String TAG = "WebViewActivity";
    public static final String KEY_FULL_THEME = "fullTheme1";
    private HorizontalWebView webViewT;
    private boolean isUsePlayer = false;
    private boolean hasDismissXiuTan = false;
    private boolean isOnPause;
    //视频全屏参数
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS
            = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private VideoContainer fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private boolean blockImg;
    private TextView bottomTitleView;
    private RelativeListenLayout bottomBar, webViewBg;
    private TextView bottomBarXiuTan;
    private boolean hasAutoPlay = false;
    private View bottomBarXiuTanBg, bottomHomeIcon;
    private float mDownY, webBgDownX, webBgDownY;
    private int scrollHeightY = 0;
    private int bottomHomeMinMargin;
    private View toastView;
    private boolean isToastShow = false;
    private View bottomBarMenu;
    private BrowserMenuPopup browserMenuPopup;
    private FrameLayout.LayoutParams layoutParams;
    private CoordinatorLayout snackBarBg;
    private ValueCallback<Uri[]> filePathCallback;
    private View leftIcon, rightIcon;
    private ImageView leftIconView, rightIconView;
    private int dp50, dp40;
    private int lastLeftState, lastRightState;
    private View element_bg;
    private View debug_rule_text_bg;
    private View debug_node_text_bg;
    private View search_bg;
    private TextView debug_node_text, debug_rule_text;
    private SimpleHintPopupWindow simpleHintPopupWindow;
    private TextView searchInfo;
    private EditText search_edit;
    private ImageView bottom_bar_muti;
    private boolean fastPlayFromLiked;
    private long mExitTime;
    private BasePopupView settingPopupView;
    private String detectedText = "", detectedUrl = "";
    private String imgHref;
    private RecyclerView gridView;
    private List<Shortcut> shortcuts;
    private boolean homeTag = true;
    private ShortcutAdapter shortcutAdapter;
    private float scrollX, scrollY;
    private String background;
    private WebProgress progress_bar;
    private ImageView shortcutAddView;
    protected int setSystemUiVisibility = -1;
    private ImageView bottom_bar_refresh;
    private static final int REQUEST_CODE_SCAN = 2401;
    private ImageView slogan;
    private boolean appOpenTemp = false;
    private FloatVideoController floatVideoController;
    private String videoPlayingWebUrl;
    private View magnet_bg;
    private TextView magnet_text;
    private XiuTanResultPopup magnetPopup;
    private LoadingPopupView globalLoadingView;
    private List<UpdateEvent> jsUpdateEvents;

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activity_browser;
    }

    private boolean hasBackground() {
        return StringUtil.isNotEmpty(background);
    }

    @Override
    protected void initView() {
        setSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        Application.application.setHomeActivity(this);
        Application.setHasMainActivity(true);
        //初始化WebView
//        initWebView();
        slogan = findView(R.id.slogan);
        showBackground();
        showShortcuts();
        SettingConfig.initNowConfig(getContext());
//        long start = System.currentTimeMillis();
        LitePal.getDatabase().disableWriteAheadLogging();
//        Timber.d("disableWriteAheadLogging used:%s", System.currentTimeMillis() - start);
        Looper.myQueue().addIdleHandler(() -> {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
            WebUtil.setWebActivityExist(true);
            findView(R.id.view_stub_browser_content).setVisibility(VISIBLE);
            webViewBg = findView(R.id.web_view_bg);

            bottomHomeMinMargin = DisplayUtil.dpToPx(getContext(), 80);
            dp50 = DisplayUtil.dpToPx(getContext(), 60);
            dp40 = DisplayUtil.dpToPx(getContext(), 40);
            leftIcon = findView(R.id.left_icon);
            rightIcon = findView(R.id.right_icon);
            leftIconView = findView(R.id.left_icon_view);
            rightIconView = findView(R.id.right_icon_view);

            int bottomBarMode = PreferenceMgr.getInt(getContext(), "bottomBar", 0);
            ViewStub viewStub = findView(R.id.view_stub_browser_bottom);
            if (bottomBarMode != 0) {
                viewStub.setLayoutResource(getBottomBarLayoutId(bottomBarMode));
                viewStub.setVisibility(VISIBLE);
            } else {
                viewStub.setVisibility(VISIBLE);
            }
            initBottomBar();
            boolean fullTheme = PreferenceMgr.getBoolean(getContext(), KEY_FULL_THEME, false);
            if (fullTheme) {
                updateFullTheme(fullTheme);
            }

            //初始化加载参数
            initWebView();
            startLoadUrl();
            SettingConfig.initConfig(getContext());
            SettingConfig.dynamicInitConfig(getContext());
            HeavyTaskUtil.executeNewTask(() -> {
                try {
                    UrlDetector.initVideoRules();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                BrowserProxy.INSTANCE.initProxyRules();
                try {
                    if (!AdblockHelper.get().isInit()) {
                        // init Adblock
                        String basePath = getDir(AdblockEngine.BASE_PATH_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath();
                        AdblockHelper.get()
                                .init(WebViewActivity.this, basePath, true, AdblockHelper.PREFERENCE_NAME)
                                .setDisabledByDefault();
                        AdblockHelper.get().getProvider().retain(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    ThunderManager.INSTANCE.globalInit(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            //延时执行
            webViewBg.postDelayed(() -> {
                if (isFinishing()) {
                    return;
                }
                //初始化可以后台初始化的配置
                HeavyTaskUtil.executeNewTask(DetectorManager::getInstance);
                int nowAppVersion = PreferenceMgr.getInt(getContext(), "version", "hiker", 0);
                if (SettingConfig.AppVersion <= nowAppVersion) {
                    //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
                    QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
                        @Override
                        public void onViewInitFinished(boolean arg0) {
                            //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                            Timber.d(" onViewInitFinished is %s", arg0);
                        }

                        @Override
                        public void onCoreInitFinished() {
                        }
                    };
                    // 在调用TBS初始化、创建WebView之前进行如下配置
                    HashMap<String, Object> map = new HashMap<>();
                    map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
                    map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
                    QbSdk.initTbsSettings(map);
                    //x5内核初始化接口
                    try {
                        QbSdk.initX5Environment(getContext(), cb);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (PreferenceMgr.getInt(getContext(), "version", "hiker", 0) != 0) {
                    showUpdateRecords();
                }
                HeavyTaskUtil.executeNewTask(this::initBackgroundTaskNotImportant);
            }, 1500);
            Looper.myQueue().addIdleHandler(() -> {
                int recoverLastTab = PreferenceMgr.getInt(getContext(), "vip", "recoverLastTab", 0);
                if (recoverLastTab != 0 && StringUtil.isEmpty(getIntent().getStringExtra("url"))) {
                    String tabsStr = PreferenceMgr.getString(getContext(), "vip", "lastTab", "");
                    if (StringUtil.isNotEmpty(tabsStr)) {
                        PreferenceMgr.put(getContext(), "vip", "lastTab", "");
                        List<TabHistory> tabs = JSON.parseArray(tabsStr, TabHistory.class);
                        if (CollectionUtil.isNotEmpty(tabs)) {
                            if (recoverLastTab == 1) {
                                //手动恢复
                                Snackbar.make(getSnackBarBg(), "恢复上次未关闭标签？", Snackbar.LENGTH_LONG)
                                        .setAction("恢复", v -> recoverLastTabNow(tabs))
                                        .show();
                            } else {
                                //自动恢复
                                recoverLastTabNow(tabs);
                            }
                        }
                    }
                } else {
                    PreferenceMgr.put(getContext(), "vip", "lastTab", "");
                }
                return false;
            });
            if (PreferenceMgr.getBoolean(getContext(), "floatVideo", false)) {
                initFloatVideo();
            }
            return false;
        });
//        WebView.setWebContentsDebuggingEnabled(true);
    }

    private void initFloatVideo() {
        floatVideoController = new FloatVideoController(getActivity(), findView(R.id.bg), (pause, force) -> {
            if (pause) {
                String muteJs = JSManager.instance(getContext()).getJsByFileName("mute");
                if (webViewT != null && !TextUtils.isEmpty(muteJs)) {
                    webViewT.evaluateJavascript(muteJs, null);
                }
            }
            if (force && webViewT != null) {
                if (pause) {
                    webViewT.onPause();
                } else {
                    webViewT.onResume();
                }
            }
            return 0;
        }, DetectorManager.getInstance(),
                () -> webViewT == null || webViewT.getWebViewHelper() == null ? null : webViewT.getWebViewHelper().getRequestHeaderMap());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFloatVideoChange(FloatVideoChangeEvent event) {
        boolean floatNow = PreferenceMgr.getBoolean(getContext(), "floatVideo", false);
        if (!floatNow) {
            //关闭
            if (floatVideoController != null) {
                floatVideoController.destroy();
                floatVideoController = null;
            }
        } else {
            //开启
            if (floatVideoController == null) {
                initFloatVideo();
            }
        }
    }

    private void recoverLastTabNow(List<TabHistory> tabs) {
        boolean hasUrl = webViewT != null && StringUtil.isNotEmpty(webViewT.getUrl());
        int pos = -1;
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).isUse()) {
                pos = i;
            }
            addWindow(tabs.get(i).getUrl(), false, null);
        }
        if (pos == -1) {
            pos = tabs.size() - 1;
        }
        if (hasUrl) {
            //已经有链接的情况下，直接换一下位置到最后即可
            HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).getWebViewList().remove(0);
            MultiWindowManager.instance(WebViewActivity.this).getWebViewList().add(webView);
        } else {
            showWebViewByPos(pos + 1);
            removeByPos(0);
        }
        webViewBg.setTag("white");
        webViewBg.setBackgroundColor(getBackgroundColor());
    }

    private int getBottomBarLayoutId(int bottomBar) {
        if (bottomBar == 1) {
            return R.layout.view_stub_browser_bottom2;
        } else if (bottomBar == 2) {
            return R.layout.view_stub_browser_bottom3;
        } else if (bottomBar == 3) {
            return R.layout.view_stub_browser_bottom4;
        } else {
            return R.layout.view_stub_browser_bottom;
        }
    }

    private void initBottomBar() {
        bottomTitleView = findView(R.id.bottom_bar_title);
        bottomBar = findView(R.id.bottom_bar_bg);
        int dp10 = DisplayUtil.dpToPx(getContext(), 10);
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        int width = outMetrics.widthPixels;
        boolean land = height < width;
        if (land) {
            bottomBar.setPadding(width / 6 + dp10, 0, width / 6 + dp10, 0);
        } else {
            bottomBar.setPadding(dp10, 0, dp10, 0);
        }
        bottom_bar_refresh = findView(R.id.bottom_bar_refresh);
        View bottom_bar_home = findView(R.id.bottom_bar_home);
        bottom_bar_muti = findView(R.id.bottom_bar_muti);
        bottomBarMenu = findView(R.id.bottom_bar_menu);
        if (!hasBackground()) {
            updateBottomBarBackground();
        } else {
            bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackground(null);
        }
        bottomHomeIcon = findView(R.id.home);
        bottomTitleView.setOnClickListener(this);
        bottomTitleView.setOnLongClickListener(v -> {
            XPopup.setAnimationDuration(200);
            new XPopup.Builder(getContext())
                    .atView(v)
                    .hasShadowBg(false)
                    .asAttachList(new String[]{"回到主页", "复制链接", "分享链接", "粘贴前往"}, null, (position, text) -> {
                        switch (text) {
                            case "回到主页":
                                backToHomeHtml();
                                break;
                            case "复制链接":
                                if (webViewT == null || StringUtil.isEmpty(webViewT.getUrl())) {
                                    ToastMgr.shortBottomCenter(getContext(), "没有链接可复制，请先访问网站");
                                    break;
                                }
                                ClipboardUtil.copyToClipboardForce(getContext(), webViewT.getUrl());
                                break;
                            case "分享链接":
                                if (webViewT == null || StringUtil.isEmpty(webViewT.getUrl())) {
                                    ToastMgr.shortBottomCenter(getContext(), "没有链接可分享，请先访问网站");
                                    break;
                                }
                                ShareUtil.shareText(getContext(), bottomTitleView.getText() + "\n" + webViewT.getUrl());
                                break;
                            case "粘贴前往":
                                ClipboardUtil.getText(getContext(), v, text1 -> {
                                    SearchEngine engine = SearchModel.getDefaultWebEngine(getContext());
                                    SearchEvent event = new SearchEvent(text1, engine, "web", null, null, false);
                                    onSearch(event);
                                });
                                break;
                        }
                    }).show();
            v.postDelayed(() -> XPopup.setAnimationDuration(300), 200);
            return true;
        });
        bottom_bar_refresh.setOnClickListener(this);
        bottom_bar_refresh.setOnLongClickListener(v -> {
            if (isForward(v)) {
                if (webViewT != null) {
                    webViewT.reload();
                }
                return true;
            }
            if (webViewT != null && webViewT.canGoForward()) {
                webViewT.goForward();
            }
            return true;
        });
        bottom_bar_home.setOnClickListener(this);
        bottom_bar_home.setOnLongClickListener(v -> {
            backToHomeHtml();
            return true;
        });
        bottom_bar_muti.setOnClickListener(this);
        bottom_bar_muti.setOnLongClickListener(v -> {
            XPopup.setAnimationDuration(200);
            new XPopup.Builder(getContext())
                    .atView(v)
                    .hasShadowBg(false)
                    .asAttachList(new String[]{"新建窗口", "清除其它窗口", "清除全部窗口"}, null, (position, text) -> {
                        switch (text) {
                            case "新建窗口":
                                addWindow(null);
                                break;
                            case "清除其它窗口":
                                clearOtherWebView();
                                break;
                            case "清除全部窗口":
                                HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).clear();
                                showNewWebView(webView);
                                ToastMgr.shortBottomCenter(getContext(), "已清除所有窗口");
                                HeavyTaskUtil.saveTabHistory(getActivity());
                                break;
                        }
                    }).show();
            v.postDelayed(() -> XPopup.setAnimationDuration(300), 200);
            return true;
        });
        bottomBarMenu.setOnClickListener(this);
        layoutParams = (FrameLayout.LayoutParams) bottomHomeIcon.getLayoutParams();
        initBottomBarListener();

        bottomBarXiuTan = findView(R.id.bottom_bar_xiu_tan);
        bottomBarXiuTanBg = findView(R.id.bottom_bar_xiu_tan_bg);
        bottomBarXiuTan.setOnClickListener(this);
        bottomBarXiuTan.setOnLongClickListener(v -> {
            final List<DetectedMediaResult> results = DetectorManager.getInstance().getDetectedMediaResults(MediaType.VIDEO_MUSIC);
            if (results.size() < 1) {
                ToastMgr.shortBottomCenter(getContext(), "还没有嗅探到视频，请稍候重试");
            } else {
                ToastMgr.shortBottomCenter(getContext(), "已为您快速播放第一条视频");
                try {
                    DetectedMediaResult mediaResult = results.get(0);
                    mediaResult.setClicked(true);
                    startPlayVideo(mediaResult.getUrl());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        if (webViewT != null && StringUtil.isNotEmpty(webViewT.getTitle())) {
            bottomTitleView.setText(webViewT.getTitle());
        }
    }

    private void refreshBottomBar(int bottomBarMode) {
        final ViewParent viewParent = bottomBar.getParent();
        int mLayoutResource = getBottomBarLayoutId(bottomBarMode);
        if (viewParent instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) viewParent;
            View view = LayoutInflater.from(getContext()).inflate(mLayoutResource, parent, false);
            replaceSelfWithView(view, parent);
            initBottomBar();
        }
    }

    private void replaceSelfWithView(View view, ViewGroup parent) {
        final int index = parent.indexOfChild(bottomBar);
        parent.removeViewInLayout(bottomBar);
        ViewGroup.LayoutParams layoutParams = bottomBar.getLayoutParams();
        if (layoutParams != null) {
            parent.addView(view, index, layoutParams);
        } else {
            parent.addView(view, index);
        }
    }

    private void showBackground() {
        background = PreferenceMgr.getString(getContext(), "home_bg", null);
        if (StringUtil.isNotEmpty(background) && !"random".equals(background)) {
            File file = new File(background);
            if (!file.exists()) {
                //值存在，但是文件不存在
                background = null;
            }
        }
        if (!hasBackground()) {
            return;
        }
        slogan.setVisibility(View.INVISIBLE);
        View bg = findView(R.id.bg);
        ImageView imageView = bg.findViewById(R.id.backgroundImageView);
        bg.setBackground(null);
        imageView.setVisibility(VISIBLE);
        String path = getHomeBackground();
        GlideUtil.loadPicDrawable(getContext(), imageView, path, new RequestOptions());
        boolean home_logo_dark = PreferenceMgr.getBoolean(getContext(), "home_logo_dark", false);
        View shortcut_search = findViewById(R.id.shortcut_search);
        shortcut_search.setBackground(getResources().getDrawable(!home_logo_dark ? R.drawable.check_bg_trans_border_white : R.drawable.check_bg_trans));
        ImageView shortcut_search_scan = findViewById(R.id.shortcut_search_scan);
        shortcut_search_scan.setImageDrawable(getResources().getDrawable(!home_logo_dark ? R.drawable.scan_light_white : R.drawable.scan_light));
        AndroidBarUtils.setTranslucent(this);
        if (home_logo_dark) {
            WindowInsetsControllerCompat wic = getWindowInsetsController();
            if (wic != null) {
                // true表示Light Mode，状态栏字体呈黑色，反之呈白色
                wic.setAppearanceLightStatusBars(true);
            }
        }
        updateNavBarColor(path);
    }

    private void updateNavBarColor(String path) {
        ImageUtilKt.loadDominantColor(getContext(), path, color -> {
            if (getActivity() != null && !getActivity().isFinishing()) {
                ThreadTool.INSTANCE.runOnUI(() -> {
                    getWindow().setNavigationBarColor(color);
                });
            }
            return Unit.INSTANCE;
        });
    }

    private WindowInsetsControllerCompat getWindowInsetsController() {
        if (Build.VERSION.SDK_INT >= 30) {
            //在部分SDK_INT < 30的系统上直接用getWindowInsetsController拿不到
            return ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        }
        return WindowCompat.getInsetsController(getWindow(), slogan);
    }

    private String getHomeBackground() {
        if ("random".equals(background)) {
            File dir = new File(UriUtils.getRootDir(getContext()) + File.separator + "images");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File[] files = dir.listFiles();
            if (files == null || files.length < 1) {
                background = "";
                return "";
            }
            return files[RandomUtil.getRandom(0, files.length)].getAbsolutePath();
        }
        return background;
    }

    private void refreshBackground() {
        boolean home_logo_dark = PreferenceMgr.getBoolean(getContext(), "home_logo_dark", false);
        View bg = findView(R.id.bg);
        ImageView imageView = bg.findViewById(R.id.backgroundImageView);
        bg.setBackground(null);
        imageView.setVisibility(VISIBLE);
        String path = getHomeBackground();
        GlideUtil.loadPicDrawable(getContext(), imageView, path, new RequestOptions());
        View shortcut_search = findViewById(R.id.shortcut_search);
        shortcut_search.setBackground(getResources().getDrawable(!home_logo_dark ? R.drawable.check_bg_trans_border_white : R.drawable.check_bg_trans));
        ImageView shortcut_search_scan = findViewById(R.id.shortcut_search_scan);
        shortcut_search_scan.setImageDrawable(getResources().getDrawable(!home_logo_dark ? R.drawable.scan_light_white : R.drawable.scan_light));
        if (webViewBg != null && webViewBg.getVisibility() != VISIBLE) {
            AndroidBarUtils.setTranslucent(this);
            bottomBar.setBackground(null);
            if (home_logo_dark) {
                WindowInsetsControllerCompat wic = getWindowInsetsController();
                if (wic != null) {
                    // true表示Light Mode，状态栏字体呈黑色，反之呈白色
                    wic.setAppearanceLightStatusBars(true);
                }
            }
        }
        boolean hasBg = true;
        boolean hasBgAll = hasBg && !home_logo_dark;
        for (Shortcut shortcut : shortcuts) {
            if (ShortcutTypeEnum.DEFAULT == ShortcutTypeEnum.Companion.getByCode(shortcut.getType())) {
                shortcut.setHasBackground(hasBgAll);
            } else {
                shortcut.setHasBackground(hasBg);
            }
        }
        shortcutAdapter.notifyDataSetChanged();
        slogan.setVisibility(View.INVISIBLE);
        if (bottomBar != null) {
            bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackground(null);
        }
        updateNavBarColor(path);
    }

    private void clearBackground() {
        background = null;
        updateBottomBarBackground();
        AndroidBarUtils.setTranslucentStatusBar(this, false);
        View bg = findView(R.id.bg);
        bg.setBackgroundColor(getResources().getColor(R.color.white));
        ImageView imageView = bg.findViewById(R.id.backgroundImageView);
        imageView.setImageDrawable(null);
        imageView.setVisibility(GONE);
        View shortcut_search = bg.findViewById(R.id.shortcut_search);
        shortcut_search.setBackground(getResources().getDrawable(R.drawable.check_bg_trans));
        ImageView shortcut_search_scan = findViewById(R.id.shortcut_search_scan);
        shortcut_search_scan.setImageDrawable(getResources().getDrawable(R.drawable.scan_light));
        if (CollectionUtil.isNotEmpty(shortcuts) && shortcuts.get(0).isHasBackground()) {
            for (Shortcut shortcut : shortcuts) {
                shortcut.setHasBackground(false);
            }
            shortcutAdapter.notifyDataSetChanged();
        }
        if (!isLand()) {
            slogan.setVisibility(VISIBLE);
        }
        PreferenceMgr.put(getContext(), "home_bg", "");
        ToastMgr.shortBottomCenter(getContext(), "主页背景图已清除");
        getWindow().setNavigationBarColor(0xffffffff);
    }

    private void getDefaultShortcuts() {
        shortcuts = new ArrayList<>();
        shortcuts.add(new Shortcut("书签", "hiker://bookmark", "hiker://images/icon1"));
        shortcuts.add(new Shortcut("下载", "hiker://download", "hiker://images/home_download"));
        shortcuts.add(new Shortcut("历史", "hiker://history", "hiker://images/icon3"));
        shortcuts.add(new Shortcut("设置", "hiker://setting", "hiker://images/home_setting"));
        shortcuts.add(new Shortcut("小程序", "hiker://mini-program", "hiker://images/icon4"));
        Shortcut poetry = new Shortcut();
        poetry.setType(ShortcutTypeEnum.POETRY.name());
        poetry.setName("长风破浪会有时，直挂云帆济沧海");
        poetry.setUrl("李白");
        shortcuts.add(poetry);
        Shortcut data = new Shortcut();
        data.setType(ShortcutTypeEnum.DATA.name());
        shortcuts.add(data);
    }

    private void updatePoetry(Shortcut shortcut) {
        HeavyTaskUtil.executeNewTask(() -> {
            PoetryService.INSTANCE.getPoetry(shortcut);
            if (!isFinishing()) {
                runOnUiThread(() -> {
//                    shortcutAdapter.notifyItemChanged(finalIndex);
                    BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
                });
            }
        });
    }

    private void updateData(Shortcut shortcut) {
        int index = -1;
        for (int i = 0; i < shortcuts.size(); i++) {
            if (shortcuts.get(i) == shortcut) {
                index = i;
                break;
            }
        }
        int finalIndex = index;
        HeavyTaskUtil.executeNewTask(() -> {
            int count1 = LitePal.where("dir <= 0").count(Bookmark.class);
            int count2 = LitePal.count(DownloadRecord.class);
            int count3 = JSManager.instance(getContext()).listAllJsFileNames().size();
            String url = count1 + "@@" + count2 + "@@" + count3;
            if (url.equals(shortcut.getUrl())) {
                return;
            }
            shortcut.setUrl(url);
            if (!isFinishing()) {
                runOnUiThread(() -> {
                    shortcutAdapter.notifyItemChanged(finalIndex);
                    BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
                });
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showShortcuts() {
//        Timber.d("consume: showShortcuts: activity(%s) 1 %s", getClass().getSimpleName(), (System.currentTimeMillis() - Application.start));
        String cuts = BigTextDO.getShortcuts(getContext());
        if (StringUtil.isNotEmpty(cuts)) {
            shortcuts = Shortcut.toList(cuts);
        } else {
            getDefaultShortcuts();
            HeavyTaskUtil.executeNewTask(() -> BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts)));
        }
        boolean hasBg = hasBackground();
        boolean home_logo_dark = PreferenceMgr.getBoolean(getContext(), "home_logo_dark", false);
        boolean hasBgAll = hasBg && !home_logo_dark;
        List<Shortcut> poetryList = new ArrayList<>();
        List<Shortcut> dataList = new ArrayList<>();
        for (Shortcut shortcut : shortcuts) {
            ShortcutTypeEnum typeEnum = ShortcutTypeEnum.Companion.getByCode(shortcut.getType());
            if (typeEnum == ShortcutTypeEnum.DATA) {
                dataList.add(shortcut);
            } else if (typeEnum == ShortcutTypeEnum.POETRY) {
                poetryList.add(shortcut);
            }
            if (ShortcutTypeEnum.DEFAULT == typeEnum) {
                shortcut.setHasBackground(hasBgAll);
            } else {
                shortcut.setHasBackground(hasBg);
            }
        }
        View shortcut_search = findView(R.id.shortcut_search);
        shortcut_search.setOnClickListener(v -> {
            GlobalSearchPopup.startSearch(WebViewActivity.this, null
                    , getNowUrl(), "web", getResources().getColor(R.color.white), true);
        });
        shortcut_search.setOnLongClickListener(v -> {
            showBackgroundSetting(v);
            return true;
        });

        ImageView shortcut_search_scan = findView(R.id.shortcut_search_scan);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            shortcut_search_scan.setVisibility(VISIBLE);
            shortcut_search_scan.setOnClickListener(v -> {
                XPermission.create(getContext(), PermissionConstants.CAMERA)
                        .callback(new XPermission.SimpleCallback() {
                            @Override
                            public void onGranted() {
                                Intent intent = new Intent(getContext(), MyCaptureActivity.class);
                                startActivityForResult(intent, REQUEST_CODE_SCAN);
                            }

                            @Override
                            public void onDenied() {
                                Toast.makeText(getContext(), "没有相机权限！", Toast.LENGTH_SHORT).show();
                            }
                        }).request();
            });
        }

        slogan.setOnLongClickListener(v -> {
            showBackgroundSetting(v);
            return true;
        });

        gridView = findView(R.id.gridView);
        refreshHomeMargin(shortcut_search, shortcut_search_scan);

        findView(R.id.shortcut_container).setVisibility(VISIBLE);

        shortcutAddView = findView(R.id.shortcut_add);
        shortcutAddView.setOnClickListener(v -> {
            ShortcutInputPopup inputPopup = new ShortcutInputPopup(getContext())
                    .bind(new Shortcut(), shortcut1 -> {
                        if (StringUtil.isEmpty(shortcut1.getName()) &&
                                !ShortcutTypeEnum.POETRY.name().equals(shortcut1.getType())) {
                            ToastMgr.shortBottomCenter(getContext(), "名称不能为空");
                            return;
                        }
                        if (StringUtil.isNotEmpty(shortcut1.getIcon()) && (shortcut1.getIcon().startsWith("http")
                                || shortcut1.getIcon().startsWith("file://") || shortcut1.getIcon().startsWith("/")
                                || shortcut1.getIcon().startsWith("hiker://images/") || shortcut1.getIcon().startsWith("color://"))) {
//                            shortcut1.setIcon(shortcut1.getIcon());
                        } else {
                            String ic = "color://" + BookmarkActivity.colors[RandomUtil.getRandom(0, BookmarkActivity.colors.length)];
                            shortcut1.setIcon(ic);
                        }
                        shortcuts.add(shortcut1);
                        BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
                        shortcutAdapter.notifyItemInserted(shortcuts.size() - 1);
                        if (ShortcutTypeEnum.DATA.name().equals(shortcut1.getType())) {
                            updateData(shortcut1);
                        }
                        if (ShortcutTypeEnum.POETRY.name().equals(shortcut1.getType())) {
                            updatePoetry(shortcut1);
                        }
                    });
            new XPopup.Builder(getContext())
                    .asCustom(inputPopup)
                    .show();
        });
        shortcutAdapter = new ShortcutAdapter(getContext(), shortcuts);
        shortcutAdapter.setOnItemClickListener(new ShortcutAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                try {
                    Shortcut shortcut = shortcuts.get(position);
                    if (shortcut.isDragging()) {
                        ShortcutInputPopup inputPopup = new ShortcutInputPopup(getContext())
                                .bind(shortcut, shortcut1 -> {
                                    if (StringUtil.isEmpty(shortcut1.getName())) {
                                        ToastMgr.shortBottomCenter(getContext(), "名称不能为空");
                                        return;
                                    }
                                    if (StringUtil.isNotEmpty(shortcut1.getIcon()) && (shortcut1.getIcon().startsWith("http")
                                            || shortcut1.getIcon().startsWith("file://") || shortcut1.getIcon().startsWith("/")
                                            || shortcut1.getIcon().startsWith("hiker://images/") || shortcut1.getIcon().startsWith("color://"))) {
                                        shortcut.setIcon(shortcut1.getIcon());
                                    } else if (StringUtil.isEmpty(shortcut1.getIcon()) && StringUtil.isNotEmpty(shortcut.getIcon())
                                            && !shortcut.getIcon().startsWith("color")) {
                                        String ic = "color://" + BookmarkActivity.colors[RandomUtil.getRandom(0, BookmarkActivity.colors.length)];
                                        shortcut.setIcon(ic);
                                    }
                                    shortcut.setName(shortcut1.getName());
                                    shortcut.setType(shortcut1.getType());
                                    shortcut.setUrl(shortcut1.getUrl());
                                    BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
                                    shortcutAdapter.notifyItemChanged(position);
                                });
                        new XPopup.Builder(getContext())
                                .asCustom(inputPopup)
                                .show();
                    } else {
                        ShortcutTypeEnum typeEnum = ShortcutTypeEnum.Companion.getByCode(shortcut.getType());
                        if (typeEnum == ShortcutTypeEnum.POETRY) {
                            TextView textView = v.findViewById(R.id.textView);
                            if (textView != null) {
                                String p = textView.getText().toString();
                                new XPopup.Builder(getContext())
                                        .asConfirm("诗词", p, "取消", "复制", () -> {
                                            ClipboardUtil.copyToClipboardForce(getContext(), p);
                                        }, () -> {

                                        }, false)
                                        .show();
                            }
                        } else if (typeEnum == ShortcutTypeEnum.DATA) {
                            Object tag = v.getTag();
                            if ("bookmark".equals(tag)) {
                                startActivityForResult(new Intent(getContext(), BookmarkActivity.class), 101);
                            } else if ("download".equals(tag)) {
                                Intent intent2 = new Intent(getContext(), DownloadRecordsActivity.class);
                                intent2.putExtra("downloaded", true);
                                startActivity(intent2);
                            } else if ("plugin".equals(tag)) {
                                startActivity(new Intent(getContext(), JSListActivity.class));
                            } else if ("bizhi".equals(tag)) {
                                showBackgroundSetting(v, shortcut);
                            }
                        } else if (!shortcut.getUrl().startsWith("http") && !shortcut.getUrl().startsWith("file")) {
                            overrideUrlLoading2(new OnOverrideUrlLoadingForOther(shortcut.getUrl()));
                        } else {
                            if (webViewT != null) {
                                webViewT.loadUrl(shortcut.getUrl());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDel(View v, int position) {
                shortcuts.remove(position);
                shortcutAdapter.notifyItemRemoved(position);
                BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
            }

            @Override
            public void onLongClick(View v, int position) {
                if (!shortcuts.get(position).isDragging()) {
                    for (Shortcut shortcut : shortcuts) {
                        shortcut.setDragging(true);
                    }
                    //不使用shortcutAdapter.notifyDataSetChanged();不然图片会闪动
                    shortcutAdapter.notifyItemRangeChanged(0, shortcuts.size());
                    shortcutAddView.setVisibility(VISIBLE);
                }
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 20);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return ShortcutTypeEnum.Companion.getByCode(shortcutAdapter.getList().get(position).getType()).getSpanCount();
            }
        });
        gridView.setLayoutManager(gridLayoutManager);
        gridView.setAdapter(shortcutAdapter);
        gridView.addItemDecoration(shortcutAdapter.getDividerItem());
        if (gridView.getItemAnimator() != null) {
            ((SimpleItemAnimator) gridView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
        gridView.setItemAnimator(new DefaultItemAnimator());
        touchHelper.attachToRecyclerView(gridView);
        Looper.myQueue().addIdleHandler(() -> {
            gridView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    scrollX = event.getX();
                    scrollY = event.getY();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (v.getId() != 0 && Math.abs(scrollX - event.getX()) <= 5 && Math.abs(scrollY - event.getY()) <= 5) {
                        //recyclerView空白处点击事件
                        if (!isDragging()) {
                            return false;
                        }
                        for (Shortcut shortcut : shortcuts) {
                            shortcut.setDragging(false);
                        }
                        shortcutAddView.setVisibility(GONE);
                        //不使用shortcutAdapter.notifyDataSetChanged();不然图片会闪动
                        shortcutAdapter.notifyItemRangeChanged(0, shortcuts.size());
                        BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
                    }
                }
                return false;
            });
            if (CollectionUtil.isNotEmpty(poetryList)) {
                for (Shortcut shortcut : poetryList) {
                    updatePoetry(shortcut);
                }
            }
            if (CollectionUtil.isNotEmpty(dataList)) {
                for (Shortcut shortcut : dataList) {
                    updateData(shortcut);
                }
            }
            return false;
        });
        Timber.i("consume: showShortcuts: activity(%s) 3 %s", getClass().getSimpleName(), (System.currentTimeMillis() - Application.start));
    }

    private boolean isLand() {
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        int width = outMetrics.widthPixels;
        return height < width;
    }

    private void refreshHomeMargin(View shortcut_search, ImageView shortcut_search_scan) {
        int dp20 = DisplayUtil.dpToPx(getContext(), 20);
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        int width = outMetrics.widthPixels;
        boolean land = height < width;
        View shortcut_container = findView(R.id.shortcut_container);

        int dp10 = DisplayUtil.dpToPx(getContext(), 10);
        int viewWidth = shortcut_container.getMeasuredWidth();
        Timber.d("refreshHomeMargin land: width=%d, viewWidth=%d", width, viewWidth);
        if (land && viewWidth > 0) {
            if (viewWidth < width) {
                //view宽度比width小，不能当成横屏处理
                land = false;
            }
        }
        if (land) {
            slogan.setVisibility(GONE);
            shortcut_container.setPadding(width / 6, 0, width / 6, 0);
            if (bottomBar != null) {
                bottomBar.setPadding(width / 6 + dp10, 0, width / 6 + dp10, 0);
            }
        } else {
            shortcut_container.setPadding(0, 0, 0, 0);
            if (bottomBar != null) {
                bottomBar.setPadding(dp10, 0, dp10, 0);
            }
            if (!hasBackground()) {
                slogan.setVisibility(VISIBLE);
            }
        }

        int gap = land ? DisplayUtil.dpToPx(getContext(), 128) : height / 2;

        int top = gap - DisplayUtil.dpToPx(getContext(), 78);
        FrameLayout.LayoutParams sl = (FrameLayout.LayoutParams) shortcut_search.getLayoutParams();
        sl.setMargins(dp20, top, dp20, dp20);
        shortcut_search.setLayoutParams(sl);

        FrameLayout.LayoutParams s2 = (FrameLayout.LayoutParams) shortcut_search_scan.getLayoutParams();
        s2.setMargins(dp20, top, dp20 / 2, dp20);
        shortcut_search_scan.setLayoutParams(s2);

        FrameLayout.LayoutParams s3 = (FrameLayout.LayoutParams) slogan.getLayoutParams();
        s3.setMargins(0, sl.topMargin - DisplayUtil.dpToPx(getContext(), 125), 0, 0);
        slogan.setLayoutParams(s3);

        FrameLayout.LayoutParams s4 = (FrameLayout.LayoutParams) gridView.getLayoutParams();
        int dp15 = DisplayUtil.dpToPx(getContext(), 7);
        s4.setMargins(dp15, sl.topMargin + DisplayUtil.dpToPx(getContext(), 68), dp15, 0);
        gridView.setLayoutParams(s4);
    }

    private void showBackgroundSetting(View v) {
        showBackgroundSetting(v, null);
    }

    private void showBackgroundSetting(View v, Shortcut shortcut) {
        String[] ops = shortcut != null ? new String[]{"查看大图", "设置壁纸", "在线壁纸", "壁纸管理", "随机壁纸", "清除壁纸"} :
                new String[]{"设置壁纸", "在线壁纸", "壁纸管理", "随机壁纸", "清除壁纸"};
        new SimpleHintPopupWindow(this,
                ops, s -> {
            switch (s) {
                case "查看大图":
                    new MyXpopup().Builder(getContext())
                            .asImageViewer(null, shortcut.getIcon(), new PopImageLoaderNoView(null))
                            .show();
                    break;
                case "设置壁纸":
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 1);
                    break;
                case "壁纸管理":
                    managePicture();
                    break;
                case "在线壁纸":
                    startActivity(new Intent(getContext(), PictureOnlineActivity.class));
                    break;
                case "随机壁纸":
                    useRandomPicture();
                    break;
                case "清除壁纸":
                    clearBackground();
                    break;
            }
        }).showPopupWindowCenter(v);
    }

    private boolean isDragging() {
        return shortcuts.size() > 0 && shortcuts.get(0).isDragging();
    }

    private ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlag = 0;
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            return makeMovementFlags(dragFlag, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(shortcuts, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(shortcuts, i, i - 1);
                }
            }
            shortcutAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public boolean isLongPressDragEnabled() {
            if (isFinishing()) {
                return false;
            }
            return isDragging();
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                try {
                    if (isFinishing()) {
                        return;
                    }
                    Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                    if (vib != null) {
                        vib.vibrate(70);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
//            shortcutAdapter.notifyDataSetChanged();
        }
    });

    private void initShortcutView() {
        shortcutAdapter.notifyDataSetChanged();
    }

    private void bindToastView() {
        toastView = findView(R.id.toast_bg);
        toastView.findViewById(R.id.toast_btn).setOnClickListener(v -> {
            toastView.animate().scaleY(0).scaleX(0).setDuration(300).start();
            isToastShow = false;
            new Handler().postDelayed(() -> {
                //如果又变成了展示状态，那就不隐藏了
                if (!isToastShow && !isFinishing() && toastView != null) {
                    toastView.setVisibility(View.INVISIBLE);
                }
            }, 270);
            showVideoList();
        });
        toastView.setOnClickListener(v -> {
            toastView.animate().scaleY(0).scaleX(0).setDuration(300).start();
            isToastShow = false;
            new Handler().postDelayed(() -> {
                if (!isToastShow && !isFinishing() && toastView != null) {
                    toastView.setVisibility(View.INVISIBLE);
                }
            }, 270);
        });
    }

    private void initMenuPopup() {
        int jsCount = webViewT != null && webViewT.getWebViewHelper() != null ?
                webViewT.getWebViewHelper().getGreasyForkRules().size() : 0;
        browserMenuPopup = new BrowserMenuPopup(this, tt -> {
            if (tt.startsWith("插件")) {
                if (jsCount > 0) {
                    List<String> greasyForkRules = webViewT.getWebViewHelper().getGreasyForkRules();
                    if (CollectionUtil.isEmpty(greasyForkRules)) {
                        startActivity(new Intent(getContext(), JSListActivity.class));
                    } else {
                        showGreasyForkMenu();
                    }
                } else {
                    startActivity(new Intent(getContext(), JSListActivity.class));
                }
                return;
            }
            switch (tt) {
                case "加书签":
                case "加入书签":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    addBookmark(webViewT.getTitle(), webViewT.getUrl());
                    break;
                case "移除书签":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    List<Bookmark> bookmarks = null;
                    try {
                        bookmarks = LitePal.where("url = ?", webViewT.getUrl()).limit(1).find(Bookmark.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!CollectionUtil.isEmpty(bookmarks)) {
                        bookmarks.get(0).delete();
                    }
                    ToastMgr.shortBottomCenter(getContext(), "已移除");
                    break;
                case "网络日志":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    Intent intent = new Intent(getContext(), MediaListActivity.class);
                    intent.putExtra("url", webViewT.getUrl());
                    intent.putExtra("title", getWebTitle());
                    startActivityForResult(intent, 101);
                    break;
                case "历史":
                    startActivity(new Intent(getContext(), HistoryListActivity.class));
                    break;
                case "工具箱":
                    showSubMenuPopup();
                    break;
                case "视频嗅探":
                    showMenu(bottomBarMenu);
                    break;
                case "全屏显示":
                    toggleFullTheme();
                    break;
                case "刷新":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    if (webViewT != null) {
                        webViewT.reload();
                    }
                    break;
                case "标记广告":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    showDebugView(true);
                    getWebView().evaluateJavascript("(function(){window.setDebugState(true)})();", null);
                    ToastMgr.shortBottomCenter(getContext(), "触摸可选择元素，无需点击");
                    break;
                case "网站配置":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    new XPopup.Builder(getContext())
                            .asCustom(new DomainConfigPopup(getActivity(), StringUtil.getDom(webViewT.getUrl())))
                            .show();
                    break;
                case "无图模式":
                case "关闭无图":
                    blockImg = !blockImg;
                    PreferenceMgr.put(getContext(), "blockImg", blockImg);
                    ToastMgr.shortBottomCenter(getContext(), "已" + (blockImg ? "开启" : "关闭") + "无图模式");
                    webViewT.getSettings().setBlockNetworkImage(blockImg);
                    if (webViewBg.getVisibility() == VISIBLE) {
                        webViewT.reload();
                    }
                    break;
                case "书签":
                    startActivityForResult(new Intent(getContext(), BookmarkActivity.class), 101);
                    break;
                case "设置":
                case "更多设置":
                    showSetting();
                    break;
                case "元素拦截":
                    startActivity(new Intent(getContext(), AdListActivity.class));
                    break;
                case "设置UA":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    changeUA();
                    break;
                case "无痕模式":
                    SettingConfig.noWebHistory = true;
                    PreferenceMgr.put(getContext(), "custom", "noWebHistory", SettingConfig.noWebHistory);
                    ToastMgr.shortCenter(getContext(), "已开启无痕模式");
                    break;
                case "关闭无痕":
                    SettingConfig.noWebHistory = false;
                    PreferenceMgr.put(getContext(), "custom", "noWebHistory", SettingConfig.noWebHistory);
                    ToastMgr.shortCenter(getContext(), "已关闭无痕模式");
                    break;
                case "下载":
                    Intent intent2 = new Intent(getContext(), DownloadRecordsActivity.class);
                    intent2.putExtra("downloaded", true);
                    startActivity(intent2);
                    break;
                case "退出":
                case "退出软件":
                    finish();
                    break;
            }
        }).withGreasyForkMenu(jsCount);
        if (webViewT == null) {
            return;
        }
        String url = webViewT.getUrl();
        HeavyTaskUtil.executeNewTask(() -> {
            List<Bookmark> bookmarks = null;
            try {
                bookmarks = LitePal.where("url = ?", url).limit(1).find(Bookmark.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!CollectionUtil.isEmpty(bookmarks) && !isFinishing()) {
                runOnUiThread(() -> {
                    if (browserMenuPopup != null) {
                        browserMenuPopup.notifyInBookmarkItem();
                    }
                });
            }
        });
    }

    private void showGreasyForkMenu() {
        List<String> greasyForkRules = webViewT.getWebViewHelper().getGreasyForkRules();
        List<String> data = new ArrayList<>();
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < greasyForkRules.size(); i++) {
            String rule = greasyForkRules.get(i);
            data.add(rule);
            List<String> menu = webViewT.getWebViewHelper().getGreasyForkMenus(rule);
            if (CollectionUtil.isNotEmpty(menu)) {
                for (int j = 0; j < menu.size(); j++) {
                    if ("管理插件".equals(menu.get(j))) {
                        continue;
                    }
                    data.add("    " + menu.get(j));
                    map.put(data.size() - 1, rule);
                }
            }
        }
        data.add("管理所有插件");
        new XPopup.Builder(getContext())
                .asBottomList(null, CollectionUtil.toStrArray(data), null, data.size(), (position, text) -> {
                    if ("管理所有插件".equals(text)) {
                        startActivity(new Intent(getContext(), JSListActivity.class));
                    } else if (text.startsWith("    ")) {
                        text = text.substring(4);
                        String rule = map.get(position);
                        webViewT.getWebViewHelper().triggerGreasyForkMenu(rule, text);
                    } else {
                        Intent intent = new Intent(getContext(), JSListActivity.class);
                        intent.putExtra("search", text);
                        startActivity(intent);
                    }
                }).show();
    }

    private void showCode(String url, String userAgent) {
        if (StringUtil.isEmpty(url)) {
            ToastMgr.shortBottomCenter(this, "当前网页不支持该模式查看源码");
            return;
        }
        Intent intent1 = new Intent(this, HtmlSourceActivity.class);
//                                    intent1.putExtra("code", tempCode);
        try {
            String cookie = CookieManager.getInstance().getCookie(url);
            if (StringUtil.isEmpty(cookie)) {
                cookie = "";
            }
            cookie = cookie.replace(";", "；；");
            userAgent = userAgent.replace(";", "；；");
            String header = "{User-Agent@" + userAgent + "&&Cookie@" + cookie + "}";
            intent1.putExtra("url", url + ";get;UTF-8;" + header);
        } catch (Throwable e) {
            e.printStackTrace();
            intent1.putExtra("url", url);
        }
        startActivity(intent1);
    }

    /**
     * 初始化可以后台初始化的配置
     */
    private void initBackgroundTaskNotImportant() {
        boolean memoryPlaySpeed = PreferenceMgr.getBoolean(getContext(), SETTING_CONFIG, "memoryPlaySpeed", true);
        if (memoryPlaySpeed) {
            VideoPlayerManager.PLAY_SPEED = PreferenceMgr.getFloat(getContext(), "ijkplayer", "playSpeed", 1f);
        }
        RemotePlayConfig.playerPath = RemotePlayConfig.D_PLAYER_PATH;
        try {
            DownloadManager.instance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AdUrlSubscribe.checkUpdateAsync(getContext(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            HomeConfigUtil.scanCrashLog(WebViewActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            HomeConfigUtil.deleteApks(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            WebDavBackupUtil.autoSave(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AboutOfficer.INSTANCE.autoCheckUpdate(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            AutoCacheUtilKt.clearCache(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void toast(ToastMessage event) {
        ToastMgr.shortBottomCenter(getContext(), event.getMsg());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void shortcutsUpdate(OnShortcutUpdateEvent event) {
        List<Shortcut> cuts = Shortcut.toList(BigTextDO.getShortcuts(getContext()));
        shortcuts.clear();
        shortcuts.addAll(cuts);
        boolean hasBg = hasBackground();
        for (Shortcut shortcut : shortcuts) {
            shortcut.setHasBackground(hasBg);
        }
        initShortcutView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void bookmarkUpdate(OnBookmarkUpdateEvent event) {
        if (CollectionUtil.isNotEmpty(shortcuts)) {
            for (Shortcut shortcut : shortcuts) {
                if (ShortcutTypeEnum.DATA.name().equals(shortcut.getType())) {
                    updateData(shortcut);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToast(ToastEvent event) {
        if (isOnPause) {
            return;
        }
        DebugUtil.showErrorMsg(this, getContext(), event.getMsg(), event.getMsg(), "500", new Exception(event.getMsg()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToastShow(ShowToastMessageEvent event) {
        ToastMgr.shortBottomCenter(getContext(), event.getTitle());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlLoad(OnUrlChangeEvent event) {
        Timber.d("OnUrlChangeEvent, url=%s", event.getUrl());
        if (StringUtil.isEmpty(event.getUrl())) {
            return;
        }
        if (event.isNewWindow() || event.isUseNotNow()) {
            addWindow(event.getUrl(), !event.isUseNotNow(), webViewT);
            return;
        }
        checkIntentUrl(event.getUrl());
    }

    private void checkIntent() {
        try {
            String data = getIntent().getDataString();
            if (StringUtil.isNotEmpty(data)) {
                checkIntentUrl(data);
                getIntent().setData(null);
            } else {
                String url = getIntent().getStringExtra("url");
                checkIntentUrl(url);
                getIntent().removeExtra("url");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getNowUrl() {
        return webViewT == null ? "" : webViewT.getUrl();
    }

    private void checkIntentUrl(String url) {
        if (StringUtil.isNotEmpty(url)) {
            String url2;
            String fName;
            if (url.startsWith("content://")) {
                fName = UriUtils.getFileName(Uri.parse(url));
                url2 = url + "#name=" + fName;
            } else {
                url2 = url;
                fName = FileUtil.getFileName(url);
            }
            if (url.equals("hiker://search") || url.equals("hiker://webSearch")) {
                GlobalSearchPopup.startSearch(WebViewActivity.this, null
                        , getNowUrl(), "web", getResources().getColor(R.color.white), true);
            } else if (url.equals("hiker://bookmark")) {
                startActivity(new Intent(getContext(), BookmarkActivity.class));
            } else if (url.startsWith("hiker://search?s=")) {
                String[] s = url.split("hiker://search\\?s=");
                if (s.length > 1) {
                    GlobalSearchPopup.startSearch(WebViewActivity.this, null, s[1], "web", getResources().getColor(R.color.white), true);
                }
            } else if (url.startsWith("magnet") || url.startsWith("ed2k") || url.startsWith("ftp")) {
                ThunderManager.INSTANCE.globalInit(getContext());
                if (ThunderManager.INSTANCE.isFTPOrEd2k(url)) {
                    ThunderManager.INSTANCE.startParseFTPOrEd2k(getContext(), url);
                } else {
                    ThunderManager.INSTANCE.startDownloadMagnet(getContext(), url);
                }
            } else if (!url2.startsWith("http") && url2.endsWith(".apk.1")) {
                Uri uri = Uri.parse(url);
                String fileName = fName.replace(".apk.1", ".apk");
                String copyTo = DownloadDialogUtil.getApkDownloadPath(getContext()) + File.separator + fileName;
                FileUtil.makeSureDirExist(copyTo);
                UriUtils.getFilePathFromURI(getContext(), uri, copyTo, new UriUtils.LoadListener() {
                    @Override
                    public void success(String s) {
                        if (!isFinishing()) {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                Uri uriData;
                                String type = "application/vnd.android.package-archive";
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    uriData = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + Constants.DEFAULT_FILE_PROVIDER, new File(s));
                                } else {
                                    uriData = Uri.fromFile(new File(s));
                                }
                                intent.setDataAndType(uriData, type);
                                startActivity(intent);
                            });
                        }
                    }

                    @Override
                    public void failed(String msg) {
                    }
                });
            } else if (!url.startsWith("http") && url2.endsWith(".js")) {
                Uri uri = Uri.parse(url);
                String fileName = fName.replace(".js", "");
                if (fileName.startsWith("global")) {
                    String copyTo = UriUtils.getRootDir(getContext()) + File.separator + "cache" + File.separator + "_fileSelect_" + fName;
                    FileUtil.makeSureDirExist(copyTo);
                    UriUtils.getFilePathFromURI(getContext(), uri, copyTo, new UriUtils.LoadListener() {
                        @Override
                        public void success(String s) {
                            ThreadTool.INSTANCE.runOnUI(() -> {
                                new XPopup.Builder(getContext())
                                        .asConfirm("温馨提示", "确定从外部导入“" + fileName + "”脚本插件？", () -> {
                                            try {
                                                FileUtil.copyFile(copyTo, JSManager.getJsDirPath() + File.separator + fileName + ".js");
                                                JSManager.instance(getContext()).reloadJSFile(fileName);
                                                ToastMgr.shortBottomCenter(getContext(), "导入成功");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }).show();
                            });
                        }

                        @Override
                        public void failed(String msg) {
                            ToastMgr.shortBottomCenter(getContext(), "出错：" + msg);
                        }
                    });
                }
            } else if (!url.startsWith("http") && url2.endsWith(".epub")) {
                String copyTo = UriUtils.getRootDir(getContext()) + File.separator + "cache" + File.separator + "_fileSelect_" + fName;
                FileUtil.makeSureDirExist(copyTo);
                Uri uri = Uri.parse(url);
                UriUtils.getFilePathFromURI(getContext(), uri, copyTo, new UriUtils.LoadListener() {
                    @Override
                    public void success(String s) {
                        ThreadTool.INSTANCE.runOnUI(() -> {
                            new XPopup.Builder(getContext())
                                    .asConfirm("温馨提示", "确定阅读“" + fName + "”文件？注意点击确定按钮后文件会被放到我的下载里面，下次可直接在我的下载或者历史记录里面继续阅读", () -> {
                                        String p = UriUtils.getRootDir(getContext()) + File.separator + "download" + File.separator + fName;
                                        ThreadTool.INSTANCE.async(() -> {
                                            try {
                                                FileUtil.copyFile(copyTo, p);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }, (e) -> {
                                            if (e != null) {
                                                ToastMgr.shortBottomCenter(getContext(), "出错：" + e.getMessage());
                                            } else {
                                                EpubFile.INSTANCE.showEpubView(getContext(), p);
                                            }
                                        });
                                    }).show();
                        });
                    }

                    @Override
                    public void failed(String msg) {
                        ToastMgr.shortBottomCenter(getContext(), "出错：" + msg);
                    }
                });
            } else if (!url2.startsWith("http") && (url2.endsWith(".hiker") || url2.endsWith(".txt") || url2.endsWith(".json"))) {
                if (url2.startsWith("content")) {
                    String copyTo = UriUtils.getRootDir(getContext()) + File.separator + "_cache" + File.separator + fName;
                    FileUtil.makeSureDirExist(copyTo);
                    UriUtils.getFilePathFromURI(getContext(), Uri.parse(url), copyTo, new UriUtils.LoadListener() {
                        @Override
                        public void success(String s) {
                            if (!isFinishing()) {
                                String text = FileUtil.fileToString(s);
                                runOnUiThread(() -> checkAutoTextFromFile(text, "file://" + s));
                            }
                        }

                        @Override
                        public void failed(String msg) {
                        }
                    });
                } else {
                    String path = url.replace("file://", "");
                    if (!new File(path).exists()) {
                        //中文文件名
                        path = HttpParser.decodeUrl(path, "UTF-8");
                    }
                    String text = FileUtil.fileToString(path);
                    checkAutoTextFromFile(text, url);
                }
            } else if (UrlDetector.isVideoOrMusic(url2) || url.startsWith("content://")) {
                if (url.startsWith("content://")) {
                    Uri uri = Uri.parse(url);
                    String copyTo = UriUtils.getRootDir(getContext()) + File.separator + "_cache" + File.separator + fName;
                    FileUtil.makeSureDirExist(copyTo);
                    onLoading(new LoadingEvent("文件解析中，请稍候", true));
                    getIntent().putExtra("contentCache", true);
                    UriUtils.getFilePathFromURI(getContext(), uri, copyTo, new UriUtils.LoadListener() {
                        @Override
                        public void success(String s) {
                            if (!isFinishing()) {
                                runOnUiThread(() -> {
                                    onLoading(new LoadingEvent("", false));
                                    String nUrl = "file://" + s;
                                    if (UrlDetector.isVideoOrMusic(s)) {
                                        PlayerChooser.startPlayer(getContext(), new File(s).getName(), nUrl);
                                    } else {
                                        if (StringUtil.isNotEmpty(webViewT.getUrl())) {
                                            addWindow(nUrl);
                                        } else {
                                            webViewT.loadUrl(nUrl);
                                        }
                                    }
                                });
                            } else {
                                new File(s).delete();
                            }
                        }

                        @Override
                        public void failed(String msg) {
                            if (!isFinishing()) {
                                runOnUiThread(() -> onLoading(new LoadingEvent("", false)));
                            }
                        }
                    });
                    return;
                }
                PlayerChooser.startPlayer(getContext(), url, url);
            } else {
                if (settingPopupView != null && settingPopupView.isShow()) {
                    settingPopupView.dismiss();
                }
                if (StringUtil.isNotEmpty(webViewT.getUrl())) {
                    addWindow(url);
                } else {
                    webViewT.loadUrl(url);
                }
            }
        }
    }

    private void checkAutoTextFromFile(String text, String originalUrl) {
        if (text.startsWith("{") && text.endsWith("}")) {
            AutoImportHelper.importRulesByTextWithDialog(getContext(), text, originalUrl);
        } else if (text.startsWith("[") && text.endsWith("]")) {
            AutoImportHelper.importRulesByTextWithDialog(getContext(), text, originalUrl);
        } else {
            checkAutoText(text);
        }
    }

    private void setBottomMutiWindowIcon() {
        bottom_bar_muti.setImageDrawable(getResources().getDrawable(getMutiCountLineIconId()));
    }

    private int getMutiCountLineIconId() {
        int size = MultiWindowManager.instance(this).getUsedWebViewList().size();
        int id = R.drawable.discory_10;
        switch (size) {
            case 0:
                id = R.drawable.discory;
                break;
            case 1:
                id = R.drawable.discory_1;
                break;
            case 2:
                id = R.drawable.discory_2;
                break;
            case 3:
                id = R.drawable.discory_3;
                break;
            case 4:
                id = R.drawable.discory_4;
                break;
            case 5:
                id = R.drawable.discory_5;
                break;
            case 6:
                id = R.drawable.discory_6;
                break;
            case 7:
                id = R.drawable.discory_7;
                break;
            case 8:
                id = R.drawable.discory_8;
                break;
            case 9:
                id = R.drawable.discory_9;
                break;
        }
        return id;
    }

    private void showSearchView(boolean show) {
        if (search_bg == null) {
            bindSearchView();
        }
        boolean fullTheme = PreferenceMgr.getBoolean(getContext(), KEY_FULL_THEME, false);
        int margin = fullTheme ? 0 : 50;
        updateBottomMargin(search_bg, DisplayUtil.dpToPx(getContext(), margin), false);
        if (show) {
            search_edit.setText("");
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) webViewBg.getLayoutParams();
            layoutParams.bottomMargin = DisplayUtil.dpToPx(getContext(), margin + 44);
            webViewBg.setLayoutParams(layoutParams);
            search_bg.setVisibility(VISIBLE);
            search_edit.requestFocus();
        } else {
            webViewT.clearMatches();
            if (search_bg.getVisibility() == GONE) {
                return;
            }
            search_bg.setVisibility(View.GONE);
            KeyboardUtils.hideSoftInput(search_edit);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) webViewBg.getLayoutParams();
            layoutParams.bottomMargin = DisplayUtil.dpToPx(getContext(), margin);
            webViewBg.setLayoutParams(layoutParams);
        }
    }

    private void bindSearchView() {
        findView(R.id.view_stub_main_search).setVisibility(VISIBLE);
        search_bg = findView(R.id.search_bg);
        View search_close = findView(R.id.search_close);
        View search_forward = findView(R.id.search_forward);
        View search_back = findView(R.id.search_back);
        search_forward.setOnClickListener(v -> webViewT.findNext(true));
        search_back.setOnClickListener(v -> webViewT.findNext(false));
        search_close.setOnClickListener(v -> showSearchView(false));
        searchInfo = findView(R.id.search_count);
        search_edit = findView(R.id.search_edit);

        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(search_edit.getText().toString())) {
                    searchInfo.setVisibility(View.INVISIBLE);
                } else {
                    searchInfo.setVisibility(View.VISIBLE);
                }
                String content = s.toString();
                if (!TextUtils.isEmpty(content)) {
                    webViewT.findAllAsync(content);
                }
            }
        });

    }

    private void bindDebugView() {
        element_bg = findView(R.id.element_bg);
        View debug_rule_btn = findView(R.id.debug_rule_btn);
        View debug_node_btn = findView(R.id.debug_node_btn);
        View debug_parent_btn = findView(R.id.debug_parent_btn);
        View debug_close_btn = findView(R.id.debug_close_btn);
        debug_rule_text_bg = findView(R.id.debug_rule_text_bg);
        debug_node_text_bg = findView(R.id.debug_node_text_bg);
        View debug_save_btn = findView(R.id.debug_save_btn);
        View debug_edit_rule_btn = findView(R.id.debug_edit_rule_btn);
        View debug_preview_btn = findView(R.id.debug_preview_btn);
        View debug_reload_btn = findView(R.id.debug_reload_btn);
        View debug_clear_btn = findView(R.id.debug_clear_btn);
        debug_node_text = findView(R.id.debug_node_text);
        debug_rule_text = findView(R.id.debug_rule_text);

        debug_rule_btn.setOnClickListener(v -> changeDebugView("rule"));
        debug_node_btn.setOnClickListener(v -> changeDebugView("node"));
        debug_parent_btn.setOnClickListener(v -> changeDebugView("parent"));
        findView(R.id.debug_last).setOnClickListener(v -> changeDebugView("last"));
        findView(R.id.debug_next).setOnClickListener(v -> changeDebugView("next"));
        findView(R.id.debug_child).setOnClickListener(v -> changeDebugView("child"));
        debug_close_btn.setOnClickListener(v -> changeDebugView("close"));

        //保存和编辑按钮
        debug_save_btn.setOnClickListener(v -> {
            String adBlockRule = debug_rule_text.getText().toString();
            if (TextUtils.isEmpty(adBlockRule)) {
                ToastMgr.shortBottomCenter(getContext(), "规则为空！");
            } else {
                saveAdBlock(adBlockRule);
            }
        });
        debug_edit_rule_btn.setOnClickListener(v -> {
            String adBlockRule = debug_rule_text.getText().toString();
            editBlockRule(adBlockRule);
        });
        debug_preview_btn.setOnClickListener(v -> {
            String adBlockRule = debug_rule_text.getText().toString();
            if (TextUtils.isEmpty(adBlockRule)) {
                ToastMgr.shortBottomCenter(getContext(), "规则为空！");
            } else {
                String adBlockJs = AdBlockModel.getBlockJsByRule(adBlockRule);
                if (!TextUtils.isEmpty(adBlockJs) && webViewT != null) {
                    webViewT.evaluateJavascript(adBlockJs, null);
                    ToastMgr.shortBottomCenter(getContext(), "已执行");
                }
            }
        });
        debug_reload_btn.setOnClickListener(v -> {
            if (webViewT != null) {
                webViewT.reload();
            }
        });
        debug_clear_btn.setOnClickListener(v -> {
            String url = webViewT == null ? null : webViewT.getUrl();
            if (StringUtil.isEmpty(url)) {
                ToastMgr.shortBottomCenter(getContext(), "网址为空");
            } else {
                String dom = StringUtil.getDom(url);
                new XPopup.Builder(getContext())
                        .asConfirm("温馨提示", "确定要清除" + dom + "下的元素拦截规则吗？注意清除后不能恢复，只能重新标记拦截", () -> {
                            AdBlockRule rule = LitePal.where("dom = ?", dom).findFirst(AdBlockRule.class);
                            AdBlockModel.deleteRule(rule);
                            webViewT.reload();
                            boolean has = AdUrlBlocker.instance().hasBlockRules(url);
                            if (has) {
                                new XPopup.Builder(getContext())
                                        .asConfirm("温馨提示", "已清除本地拦截规则，但远程订阅的无法清除，可以在网站配置中禁用广告拦截，或者删除远程订阅", () -> {

                                        }).show();
                            } else {
                                ToastMgr.shortBottomCenter(getContext(), "已清除");
                            }
                        }).show();
            }
        });
    }

    private void editBlockRule(String rule) {
        InputConfirmPopup inputPopup = new InputConfirmPopup(getContext());
        inputPopup.setDismissWhenConfirm(false);
        inputPopup.setDismissWhenCancel(false);
        inputPopup.bind("编辑拦截规则", "拦截规则", rule, (popup, text) -> {
            if (TextUtils.isEmpty(text)) {
                ToastMgr.shortBottomCenter(getContext(), "规则不能为空");
            } else {
                saveAdBlock(text);
                popup.dismiss();
            }
        }).bindText("保存", "预览");
        inputPopup.setCancelListener((popup, text) -> {
            if (TextUtils.isEmpty(text)) {
                ToastMgr.shortBottomCenter(getContext(), "规则不能为空");
            } else {
                String adBlockJs = AdBlockModel.getBlockJsByRule(text);
                webViewT.evaluateJavascript(adBlockJs, null);
                ToastMgr.shortCenter(getContext(), "已执行");
            }
        });
        new XPopup.Builder(getContext())
                .asCustom(inputPopup)
                .show();
    }

    private void changeDebugView(String rule) {
        switch (rule) {
            case "rule":
                debug_rule_text_bg.setVisibility(VISIBLE);
                debug_node_text_bg.setVisibility(GONE);
                break;
            case "node":
                debug_node_text_bg.setVisibility(VISIBLE);
                debug_rule_text_bg.setVisibility(GONE);
                break;
            case "parent":
                webViewT.evaluateJavascript("(function(){window.touchParent()})();", null);
                break;
            case "last":
                webViewT.evaluateJavascript("(function(){window.touchLast()})();", null);
                break;
            case "next":
                webViewT.evaluateJavascript("(function(){window.touchNext()})();", null);
                break;
            case "child":
                webViewT.evaluateJavascript("(function(){window.touchChild()})();", null);
                break;
            case "close":
                webViewT.evaluateJavascript("(function(){window.setDebugState(false)})();", null);
                showDebugView(false);
                break;
        }
    }

    private void showDebugView(boolean visible) {
        if (visible) {
            if (webViewT != null && webViewT.getWebViewHelper() != null) {
                webViewT.getWebViewHelper().setAdBlockMarking(true);
            }
            if (element_bg == null) {
                findView(R.id.layout_web_view_debug_stub).setVisibility(View.VISIBLE);
                bindDebugView();
            }
            element_bg.setVisibility(VISIBLE);
            int marginTop = ScreenUtil.getScreenSize(this)[1] / 2;
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) webViewBg.getLayoutParams();
            layoutParams.bottomMargin = marginTop;
            webViewBg.setLayoutParams(layoutParams);

            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) element_bg.getLayoutParams();
            layoutParams1.height = marginTop;
            element_bg.setLayoutParams(layoutParams1);
        } else {
            if (webViewT != null && webViewT.getWebViewHelper() != null) {
                webViewT.getWebViewHelper().setAdBlockMarking(false);
            }
            element_bg.setVisibility(GONE);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) webViewBg.getLayoutParams();
            layoutParams.bottomMargin = DisplayUtil.dpToPx(getContext(), 50);
            webViewBg.setLayoutParams(layoutParams);
        }
    }

    private void initWebViewBgListener() {
        webViewBg.setOnInterceptTouchEventListener(event -> {
            if (event.getPointerCount() != 1) {
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                webBgDownX = event.getRawX();
                webBgDownY = event.getRawY();
                scrollHeightY = ScreenUtil.getScreenHeight2(getActivity()) / 4 * 3;
                int h = webViewBg.getMeasuredHeight() / 4 * 3;
                if (h > 0) {
                    scrollHeightY = Math.min(scrollHeightY, h);
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (event.getRawX() - webBgDownX < 0 && !webViewT.canGoForward()) {
                    return false;
                }
                if (webBgDownY < scrollHeightY || event.getRawY() < scrollHeightY) {
                    //不在底部1/4区域
                    return false;
                }
                return Math.abs(event.getRawX() - webBgDownX) > 20 && Math.abs(event.getRawX() - webBgDownX) > Math.abs(event.getRawY() - webBgDownY);
            }
            return false;
        });
        webViewBg.setOnTouchEventListener(event -> {
            Log.d(TAG, "setOnTouchEventListener: " + event);
            if (event.getPointerCount() != 1) {
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    webBgDownX = event.getRawX();
                    webBgDownY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float mY = event.getRawX();
                    moveLeftRightIconMargin((int) (mY - webBgDownX));
                    break;
                case MotionEvent.ACTION_UP:
                    checkLeftRightIconMargin();
                    break;
            }
            return true;
        });
    }

    private void initBottomBarListener() {
        boolean bottomHomeG = PreferenceMgr.getBoolean(getContext(), "bottomHomeG", true);
        if (!bottomHomeG) {
            return;
        }
        bottomBar.setOnInterceptTouchEventListener(event -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mDownY = event.getRawY();
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                return Math.abs(event.getRawY() - mDownY) > 10;
            }
            return false;
        });
        bottomBar.setOnTouchEventListener(event -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float mY = event.getRawY();
                    moveBottomHomeMarginBottom((int) (mDownY - mY));
                    break;
                case MotionEvent.ACTION_UP:
                    checkBottomHomeMarginBottom();
                    break;
            }
            return true;
        });
    }

    private void moveBottomHomeMarginBottom(int distance) {
        if (bottomHomeIcon.getVisibility() == View.INVISIBLE) {
            bottomHomeIcon.setVisibility(VISIBLE);
        }

        if (distance > 0 && distance <= bottomHomeMinMargin) {
            layoutParams.bottomMargin = distance;
        } else if (distance <= 0) {
            layoutParams.bottomMargin = 0;
            if (bottomHomeIcon.getVisibility() == View.VISIBLE) {
                bottomHomeIcon.setVisibility(View.INVISIBLE);
            }
        } else {
            layoutParams.bottomMargin = bottomHomeMinMargin;
        }
        bottomHomeIcon.setLayoutParams(layoutParams);
    }

    private void checkBottomHomeMarginBottom() {
        if (layoutParams.bottomMargin >= bottomHomeMinMargin) {
            backToHomeHtml(false);
        }
        ValueAnimator anim = ValueAnimator.ofInt(layoutParams.bottomMargin, 0);
        anim.setDuration(300);
        anim.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            moveBottomHomeMarginBottom(value);
        });
        anim.start();
    }

    private void moveLeftRightIconMargin(int distance) {
        FrameLayout.LayoutParams leftLayoutParams = (FrameLayout.LayoutParams) leftIcon.getLayoutParams();
        FrameLayout.LayoutParams rightLayoutParams = (FrameLayout.LayoutParams) rightIcon.getLayoutParams();
        if (Math.abs(distance) < dp50 + dp40) {
            leftLayoutParams.leftMargin = distance - dp50;
            rightLayoutParams.rightMargin = -dp50 - distance;
            if (lastLeftState != 0) {
                leftIconView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.left_circle));
            }
            if (lastRightState != 0) {
                rightIconView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.right_circle));
            }
            lastLeftState = lastRightState = 0;
        } else if (distance >= dp50 + dp40) {
            leftLayoutParams.leftMargin = dp40;
            rightLayoutParams.rightMargin = -dp40 - dp50;
            if (lastLeftState != 1) {
                leftIconView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.left_circle_green));
            }
            lastLeftState = 1;
        } else {
            leftLayoutParams.leftMargin = -dp40 - dp50;
            rightLayoutParams.rightMargin = dp40;
            if (lastRightState != 1) {
                rightIconView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.right_circle_green));
            }
            lastRightState = 1;
        }
        leftIcon.setLayoutParams(leftLayoutParams);
        rightIcon.setLayoutParams(rightLayoutParams);
    }

    private void checkLeftRightIconMargin() {
        FrameLayout.LayoutParams leftLayoutParams = (FrameLayout.LayoutParams) leftIcon.getLayoutParams();
        FrameLayout.LayoutParams rightLayoutParams = (FrameLayout.LayoutParams) rightIcon.getLayoutParams();
        if (leftLayoutParams.leftMargin >= dp40) {
            onBackPressed();
        } else if (rightLayoutParams.rightMargin >= dp40) {
            if (webViewT.canGoForward()) {
                webViewT.goForward();
            }
        }

        if (lastLeftState != 0) {
            leftIconView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.left_circle));
        }
        if (lastRightState != 0) {
            rightIconView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.right_circle));
        }
        lastLeftState = lastRightState = 0;
        ValueAnimator anim = ValueAnimator.ofInt(leftLayoutParams.leftMargin, -dp50);
        anim.setDuration(300);
        anim.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) leftIcon.getLayoutParams();
            layoutParams1.leftMargin = value;
            leftIcon.setLayoutParams(layoutParams1);
        });
        anim.start();

        ValueAnimator anim1 = ValueAnimator.ofInt(rightLayoutParams.rightMargin, -dp50);
        anim1.setDuration(300);
        anim1.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) rightIcon.getLayoutParams();
            layoutParams1.rightMargin = value;
            rightIcon.setLayoutParams(layoutParams1);
        });
        anim1.start();
    }


    @Override
    protected void initData(Bundle savedInstanceState) {
        AlertNewVersionUtil.alert(this);
        MiniProgramRouter.INSTANCE.loadConfigBackground(getContext());
        ScreenUtil.setDisplayInNotch(this);
        DomainConfigService.INSTANCE.initConfigs();
    }

    private void showMenu(View view) {
        String[] titles = new String[]{"悬浮嗅探播放", "清除嗅探偏好", "嗅探偏好黑名单", "嗅探弹窗黑名单", "快速播放白名单"};
        if (webViewBg.getVisibility() != VISIBLE) {
            titles = new String[]{"悬浮嗅探播放"};
        }
        //content
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setItems(titles, (dialog1, which) -> {
                    switch (which) {
                        case 0:
                            XiuTanOfficer.INSTANCE.floatVideo(getActivity());
                            break;
                        case 1:
                            //放入无关的来达到清除的目的
                            String dom = StringUtil.getDom(webViewT.getUrl());
                            DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, "www.fy-sys.cn");
                            ToastMgr.shortBottomCenter(getContext(), "已清除该网站嗅探播放的偏好记录");
                            break;
                        case 2:
                            String button = "加入黑名单";
                            if (DetectorManager.getInstance().inXiuTanLikedBlackList(webViewT.getUrl())) {
                                button = "移出黑名单";
                            }
                            new ColorDialog(getContext()).setTheTitle("设置嗅探偏好黑名单")
                                    .setContentText("默认所有网站都会根据上次播放视频的域名来自动播放嗅探到的视频，即默认开启嗅探偏好功能，加入黑名单则不会根据上次播放视频的域名来自动播放嗅探到的视频")
                                    .setPositiveListener(button, dialog4 -> {
                                        String dom5 = StringUtil.getDom(webViewT.getUrl());
                                        if (DetectorManager.getInstance().inXiuTanLikedBlackList(webViewT.getUrl())) {
                                            SettingConfig.removeXiuTanLikedBlackListDom(getContext(), dom5);
                                        } else {
                                            SettingConfig.addXiuTanLikedBlackListDom(getContext(), dom5);
                                        }
                                        ToastMgr.shortBottomCenter(getContext(), "修改成功！");
                                        dialog4.dismiss();
                                    }).setNegativeListener("取消", ColorDialog::dismiss).show();
                            break;
                        case 3:
                            setXiuTanDialog();
                            break;
                        case 4:
                            setXiuTanFastPlayDialog();
                            break;
                    }
                    dialog1.dismiss();
                }).create();
        DialogUtil.INSTANCE.showAsCard(getContext(), dialog, 3);
    }

    private void startPlayVideo(String videoUrl) {
        startPlayVideo(videoUrl, getWebTitle(), true);
    }

    private void startPlayVideo(String videoUrl, String name, boolean historyVideo) {
        WebUtil.setShowingUrl(webViewT.getUrl());
        String muteJs = JSManager.instance(getContext()).getJsByFileName("mute");
//        Log.d(TAG, "startPlayVideo:1 ");
        if (!TextUtils.isEmpty(muteJs)) {
//            Log.d(TAG, "startPlayVideo:2 ");
            webViewT.evaluateJavascript(muteJs, null);
        }
        if (isOnPause) {
            return;
        }
        if (ThunderManager.INSTANCE.isFTPOrEd2k(videoUrl)) {
            startPlayFTP(videoUrl);
            return;
        } else if (ThunderManager.INSTANCE.isMagnetOrTorrent(videoUrl)) {
            startDownloadMagnet(videoUrl);
            return;
        }
        int code = getIntent().getIntExtra("uWho", 0);
        if (code == 304) {
            Intent intent = new Intent();
            intent.putExtra("videourl", videoUrl);
            intent.putExtra("title", name);
            setResult(code, intent);
            finish();
            return;
        }
        String dom = StringUtil.getDom(webViewT.getUrl());
        String url = StringUtil.getDom(videoUrl);
        DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, url);


        videoUrl = PlayerChooser.decorateHeader(WebViewHelper.getRequestHeaderMap(webViewT, videoUrl), webViewT.getUrl(), videoUrl);
        if (historyVideo) {
            HeavyTaskUtil.updateHistoryVideoUrl(webViewT.getUrl(), videoUrl);
        }
        PlayerChooser.startPlayer(getContext(), name, videoUrl, extraDataBundle);
//        webViewT.pauseTimers();
    }

    private String getWebTitle() {
        if (webViewT.getTitle() == null) {
            return "";
        }
        String t = webViewT.getTitle().replace(" ", "");
        if (t.length() > 85) {
            t = t.substring(0, 85);
        }
        return t;
    }

    private void startDownloadVideo(String videoUrl) {
        WebUtil.setShowingUrl(webViewT.getUrl());
        String muteJs = JSManager.instance(getContext()).getJsByFileName("mute");
        if (!TextUtils.isEmpty(muteJs)) {
            webViewT.evaluateJavascript(muteJs, null);
            bottomTitleView.postDelayed(() -> {
                if (webViewT != null && !isFinishing()) {
                    webViewT.evaluateJavascript(muteJs, null);
                }
            }, 5000);
        }
        String dom = StringUtil.getDom(webViewT.getUrl());
        String url = StringUtil.getDom(videoUrl);
        DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, url);
        String t = webViewT.getTitle().replace(" ", "");
        if (t.length() > 85) {
            t = t.substring(0, 85);
        }
        videoUrl = PlayerChooser.decorateHeaderWithReferer(WebViewHelper.getRequestHeaderMap(webViewT, videoUrl), webViewT.getUrl(), videoUrl);
        DownloadDialogUtil.showEditDialog(this, t, videoUrl);
    }

    private int getBottomView2DefaultDrawable() {
        if (isForward(bottom_bar_refresh)) {
            return R.drawable.right;
        } else {
            return R.drawable.refresh_web_6;
        }
    }

    private boolean isForward(View v) {
        return v.getParent() != null && "forward".equals(((View) v.getParent()).getTag());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (isForward(v)) {
            if (webViewT != null && bottom_bar_refresh != null && bottom_bar_refresh.getTag() != null) {
                webViewT.loadUrl((String) bottom_bar_refresh.getTag());
                bottom_bar_refresh.setTag(null);
                return;
            }
            if (webViewT != null && webViewT.canGoForward()) {
                webViewT.goForward();
            } else if (webViewT != null) {
                webViewT.reload();
            }
            return;
        } else if ("back".equals(v.getTag())) {
            onBackPressed();
            return;
        }
        switch (id) {
            case R.id.bottom_bar_refresh:
                if (webViewT != null) {
                    if (bottom_bar_refresh != null && bottom_bar_refresh.getTag() != null) {
                        webViewT.loadUrl((String) bottom_bar_refresh.getTag());
                        bottom_bar_refresh.setImageDrawable(getResources().getDrawable(getBottomView2DefaultDrawable()));
                        bottom_bar_refresh.setTag(null);
                        break;
                    }
                    if (StringUtil.isEmpty(webViewT.getUrl())) {
                        startActivity(new Intent(getContext(), HistoryListActivity.class));
                    } else {
                        webViewT.reload();
                    }
                }
                break;
            case R.id.bottom_bar_home:
                if (webViewT != null && StringUtil.isEmpty(webViewT.getUrl())) {
                    startActivity(new Intent(getContext(), BookmarkActivity.class));
                    break;
                }
                backToHomeHtml();
                break;
            case R.id.bottom_bar_muti:
                showMultiWindowPop(v);
                break;
            case R.id.bottom_bar_title:
                GlobalSearchPopup.startSearch(WebViewActivity.this, null
                        , getNowUrl(), "web", getResources().getColor(R.color.white), true);
                break;
            case R.id.bottom_bar_xiu_tan:
                if (!isUsePlayer && !ArticleListRuleEditActivity.hasBlockDom(StringUtil.getDom(webViewT.getUrl()))) {
                    showVideoList();
                } else {
                    ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                }
                break;
//            case R.id.menu:
//                showBookmarkCollection();
//                break;
            case R.id.bottom_bar_menu:
                initMenuPopup();
                new XPopup.Builder(getContext())
                        .popupPosition(PopupPosition.Right)
//                        .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
                        .asCustom(browserMenuPopup)
                        .show();
                break;
//            case R.id.exist:
//                finish();
//                break;
        }
    }

    private void initLoadListener() {
        MultiWindowManager.instance(this).setOnUrlLoadListener((webView) -> {
            if (webViewBg.getVisibility() != VISIBLE && webView.isUsed()) {
                if (!"white".equals(webViewBg.getTag())) {
                    webViewBg.setTag("white");
                    webViewBg.setBackgroundColor(getBackgroundColor());
                }
                webViewBg.setAlpha(0f);
                webViewBg.setVisibility(VISIBLE);
                webViewBg.animate().alpha(1f).setDuration(300).start();
                View shortcut_container = findView(R.id.shortcut_container);
                shortcut_container.setVisibility(GONE);
                shortcut_container.animate().alpha(0f).setDuration(300).start();
                if (hasBackground()) {
                    updateBottomBarBackground(true);
                    AndroidBarUtils.setTranslucentStatusBar(this, false);
                }
            }
        });
    }

    private int getBackgroundColor() {
        return WebViewHelper.getDefaultThemeColor(getContext());
    }

    private void showMultiWindowPop(View view) {
        showMultiWindowPop(() -> addWindow(null),
                () -> {
                    HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).clear();
                    showNewWebView(webView);
                    ToastMgr.shortBottomCenter(getContext(), "已清除所有窗口");
                    HeavyTaskUtil.saveTabHistory(getActivity());
                },
                new MutiWondowAdapter.OnClickListener() {
                    @Override
                    public void click(View view1, int pos) {
                        showWebViewByPos(pos);
                    }

                    @Override
                    public void remove(int pos) {
                        removeByPos(pos);
                        HeavyTaskUtil.saveTabHistory(getActivity());
                    }
                });
    }

    private void showMultiWindowPop(Runnable addRunnable, Runnable clearRunnable, MutiWondowAdapter.OnClickListener clickListener) {
        webViewT.setDetectedMediaResults(DetectorManager.getInstance().getDetectedMediaResults((Media) null));
        View shortcut_container = findView(R.id.shortcut_container);
        int tabStyle = PreferenceMgr.getInt(getContext(), "tabStyle", 0);
        if (tabStyle == 0) {
            new XPopup.Builder(getContext())
                    .isLightStatusBar(true)
                    .hasShadowBg(false)
                    .asCustom(new MutiWondowPopup(getContext())
                            .home(shortcut_container)
                            .withClearOtherRunnable(this::clearOtherWebView)
                            .with(WebViewActivity.this, MultiWindowManager.instance(WebViewActivity.this).getUsedWebViewList(),
                                    addRunnable,
                                    clearRunnable,
                                    clickListener))
                    .show();
        } else {
            new XPopup.Builder(getContext())
                    .enableDrag(false)
                    .asCustom(new MultiWondowTextPopup(getContext())
                            .withClearOtherRunnable(this::clearOtherWebView)
                            .with(WebViewActivity.this, MultiWindowManager.instance(WebViewActivity.this).getUsedWebViewList(),
                                    addRunnable,
                                    clearRunnable,
                                    clickListener))
                    .show();
        }
    }

    private void removeByPos(int index) {
        webViewBg.removeView(webViewT);
        HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).removeWindow(index);
        showNewWebView(webView);
    }

    private void clearOtherWebView() {
        MultiWindowManager.instance(WebViewActivity.this).clearOtherWebView();
        HeavyTaskUtil.saveTabHistory(getActivity());
        setBottomMutiWindowIcon();
        ToastMgr.shortBottomCenter(getContext(), "已清除其它窗口");
    }

    private void showWebViewByPos(int pos) {
        webViewBg.removeView(webViewT);
        webViewT = MultiWindowManager.instance(WebViewActivity.this).selectWindow(pos);
        webViewBg.addView(webViewT);
        boolean backHome = StringUtil.isEmpty(webViewT.getUrl());
        if (backHome) {
            backToHomeHtml();
        } else if (webViewBg.getVisibility() != VISIBLE) {
            webViewBg.setAlpha(0f);
            webViewBg.setVisibility(VISIBLE);
            webViewBg.animate().alpha(1f).setDuration(300).start();
            View shortcut_container = findView(R.id.shortcut_container);
            shortcut_container.setVisibility(GONE);
            shortcut_container.animate().alpha(0f).setDuration(300).start();
            if (hasBackground()) {
                AndroidBarUtils.setTranslucentStatusBar(getActivity(), false);
                getWindow().setNavigationBarColor(0xffffffff);
            }
            updateBottomBarBackground(true);
        } else {
            updateBottomBarBackground(true);
        }
        DetectorManager.getInstance().startDetect();
        if (!backHome) {
            StatusBarCompatUtil.setStatusBarColor(WebViewActivity.this, webViewT.getStatusBarColor());
        }
        DetectorManager.getInstance().startDetect();
        DetectorManager.getInstance().setDetectedMediaResults(webViewT.getDetectedMediaResults());
        refreshVideoCount();
        showSearchView(false);
        if (StringUtil.isNotEmpty(webViewT.getUrl())) {
            bottomTitleView.setText(webViewT.getTitle());
        }
        HeavyTaskUtil.saveTabHistory(getActivity());
    }

    private void showNewWebView(HorizontalWebView webView) {
        if (webView == null) {
            webViewBg.addView(webViewT);
            setBottomMutiWindowIcon();
            return;
        }
        webViewT = webView;
        boolean backHome = StringUtil.isEmpty(webView.getUrl());
        if (backHome) {
            backToHomeHtml();
        } else if (webViewBg.getVisibility() != VISIBLE) {
            webViewBg.setVisibility(VISIBLE);
            View shortcut_container = findView(R.id.shortcut_container);
            shortcut_container.setVisibility(GONE);
            shortcut_container.animate().alpha(0f).setDuration(300).start();
            if (hasBackground()) {
                AndroidBarUtils.setTranslucentStatusBar(getActivity(), false);
                getWindow().setNavigationBarColor(0xffffffff);
            }
            updateBottomBarBackground(true);
        } else {
            updateBottomBarBackground(true);
        }
        webViewBg.addView(webViewT);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webViewT, true);
        DetectorManager.getInstance().startDetect();
        if (!backHome) {
            StatusBarCompatUtil.setStatusBarColor(WebViewActivity.this, webViewT.getStatusBarColor());
        }
        DetectorManager.getInstance().setDetectedMediaResults(webViewT.getDetectedMediaResults());
        refreshVideoCount();
        showSearchView(false);
        setBottomMutiWindowIcon();
        if (StringUtil.isNotEmpty(webView.getUrl())) {
            bottomTitleView.setText(webViewT.getTitle());
        } else if (webViewBg.getVisibility() != VISIBLE) {
            bottomTitleView.setText("主页");
        }
    }

    private void addWindow(String url) {
        addWindow(url, true, null);
    }

    private void addWindow(String url, boolean showNow, @Nullable WebView fromWho) {
        if (showNow) {
            webViewBg.removeView(webViewT);
        }
        HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).addWebView(url, showNow, fromWho);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        if (showNow) {
            webViewT = webView;
            webViewBg.addView(webViewT);
            DetectorManager.getInstance().startDetect();
            refreshVideoCount();
            showSearchView(false);
        }
        setBottomMutiWindowIcon();
        if (StringUtil.isEmpty(url) && showNow) {
            backToHomeHtml();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCreateWindow(OnCreateWindowEvent event) {
        if (isFinishing()) {
            return;
        }
        webViewBg.removeViewWithAnimator(webViewT);
        webViewT = event.getWebView();
        webViewT.clearHistory();
        webViewBg.addView(webViewT);
        DetectorManager.getInstance().startDetect();
        refreshVideoCount();
        showSearchView(false);
        if (webViewT.getParentWebView() == null) {
            //不是通过返回不重载增加的WebView才更新窗口数和展示动画
            setBottomMutiWindowIcon();
            AnimateTogetherUtils.scaleNow(bottom_bar_muti);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void downloadStart(DownloadStartEvent event) {
        if (isFinishing()) {
            return;
        }
        HorizontalWebView horizontalWebView = event.getHorizontalWebView();
        if (horizontalWebView != null && horizontalWebView.copyBackForwardList().getSize() == 0) {
            webViewBg.removeView(webViewT);
            HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).removeWebView(horizontalWebView);
            showNewWebView(webView);
        }
        String url = event.getUrl();
        final String surl = url;
        VideoTask video = new VideoTask(url, url);
        DetectorManager.getInstance().addTask(video);
        Timber.d("downloadStart: %s, %s, %s", url, event.getMimetype(), event.getContentDisposition());
        //先从header取
        String fileName = DownloadManager.getDispositionFileName(event.getContentDisposition());
        //再从链接名字取
        if (StringUtil.isEmpty(fileName)) {
            fileName = FileUtil.getResourceName(url);
        }
        //实在不行再根据md5生成
        if (StringUtil.isEmpty(fileName)) {
            fileName = StringUtil.md5(url);
        }
        if (UrlDetector.isMusic(url) || !UrlDetector.isVideoOrMusic(url)) {
            //非视频文件，补全后缀
            if (!fileName.contains(".")) {
                String ext = ShareUtil.getExtension(url, event.getMimetype());
                if (StringUtil.isNotEmpty(ext)) {
                    fileName = fileName + "." + ext;
                } else {
                    String end = FileUtil.getExtension(url);
                    if (StringUtil.isNotEmpty(end)) {
                        fileName = fileName + "." + end;
                    }
                }
            }
        } else {
            //视频文件，去掉后缀
            if (fileName.lastIndexOf(".") >= 1) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
        }
        String finalFileName = fileName;
        if (url.startsWith("blob:")) {
            findBlob(url, finalFileName, event.getMimetype());
            return;
        }
        Timber.d("downloadStart: finalFileName: %s", finalFileName);
        String uu = PlayerChooser.decorateHeaderWithReferer(WebViewHelper.getRequestHeaderMap(webViewT, surl), webViewT.getUrl(), surl);
        if (DomainConfigKt.isDownloadNoConfirm(webViewT.getUrl())) {
            DownloadDialogUtil.downloadNow(getActivity(), finalFileName, uu);
            return;
        }
        if (url.contains(".apk") || DownloadDialogUtil.isApk(fileName, event.getMimetype())) {
            if (DomainConfigKt.isDownloadStrongPrompt(webViewT.getUrl())) {
                FileUtil.saveFile(getContext(), () -> DownloadDialogUtil.showEditDialog(getActivity(), finalFileName, uu, event.getMimetype(), event.getContentLength(), null));
            } else {
                Snackbar.make(getSnackBarBg(), "是否允许网页中的下载请求？", Snackbar.LENGTH_LONG)
                        .setAction("允许", v -> {
                            FileUtil.saveFile(getContext(), () -> DownloadDialogUtil.showEditDialog(getActivity(), finalFileName, uu, event.getMimetype(), event.getContentLength(), null));
                        }).show();
            }
        } else {
            new XPopup.Builder(getContext())
                    .asConfirm("温馨提示", "是否允许网页中的下载请求？（点击空白处拒绝操作，点击播放可以将链接作为视频地址直接播放）",
                            "播放", "下载", () -> {
                                FileUtil.saveFile(getContext(), () -> {
                                    DownloadDialogUtil.showEditDialog(getActivity(), finalFileName, uu, event.getMimetype(), event.getContentLength(), null);
                                });
                            }, () -> startPlayVideo(uu), false).show();
        }
    }


    /**
     * 下载、Blob图片
     *
     * @param url
     */
    private void findBlob(String url, String fileName, String mimeType) {
        if (StringUtil.isEmpty(url)) {
            ToastMgr.shortBottomCenter(getContext(), "获取文件格式失败");
            return;
        }
        //告诉js，发起了下载请求，不要立即revokeObjectURL
        webViewT.evaluateJavascript("window['blobDownload'] = 1;", null);
        if (StringUtil.isEmpty(fileName)) {
            fileName = StringUtil.md5(url);
        }
        if (!fileName.contains(".")) {
            String ext = ShareUtil.getExtension(url, mimeType);
            if (StringUtil.isNotEmpty(ext)) {
                fileName = fileName + "." + ext;
            }
        }
        String finalFileName = fileName;
        if (DomainConfigKt.isDownloadNoConfirm(webViewT.getUrl())) {
            new XPopup.Builder(getContext())
                    .asInputConfirm("温馨提示", "以下为从网页下载请求中提取的文件名", finalFileName, "文件名", (text) -> {
                        String name = text;
                        if (StringUtil.isEmpty(text)) {
                            name = finalFileName;
                        }
                        String finalName = name;
                        FileUtil.saveFile(getContext(), () -> downloadBlob(url, finalName));
                    }, null, R.layout.xpopup_confirm_input).show();
            return;
        }
        if (fileName.contains(".apk") || DownloadDialogUtil.isApk(fileName, mimeType)) {
            Runnable runnable = () -> new XPopup.Builder(getContext())
                    .asInputConfirm("温馨提示", "以下为从网页下载请求中提取的文件名", finalFileName, "文件名", (text) -> {
                        String name = text;
                        if (StringUtil.isEmpty(text)) {
                            name = finalFileName;
                        }
                        String finalName = name;
                        FileUtil.saveFile(getContext(), () -> downloadBlob(url, finalName));
                    }, null, R.layout.xpopup_confirm_input).show();
            if (DomainConfigKt.isDownloadStrongPrompt(webViewT.getUrl())) {
                runnable.run();
            } else {
                Snackbar.make(getSnackBarBg(), "是否允许网页中的下载请求？", Snackbar.LENGTH_LONG)
                        .setAction("允许", v -> {
                            runnable.run();
                        }).show();
            }
        } else {
            new XPopup.Builder(getContext())
                    .asInputConfirm("温馨提示", "是否允许网页中的下载请求", finalFileName, "文件名", (text) -> {
                        String name = text;
                        if (StringUtil.isEmpty(text)) {
                            name = finalFileName;
                        }
                        String finalName = name;
                        FileUtil.saveFile(getContext(), () -> downloadBlob(url, finalName));
                    }, null, R.layout.xpopup_confirm_input).show();
        }
    }

    /**
     * 下载、Blob图片
     *
     * @param url
     */
    private void downloadBlob(String url, String fileName) {
        String blob = FilesInAppUtil.getAssetsString(getContext(), "blob.js")
                .replace("${uuu}", url)
                .replace("${fileName}", fileName);
        webViewT.evaluateJavascript(blob, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void downloadBlobProgress(BlobDownloadProgressEvent event) {
        boolean isOk = "100/100".equals(event.getProgress());
        onLoading(new LoadingEvent("下载中..." + event.getProgress(), !isOk));
    }

    @Subscribe
    public void blobDownloadCallback(BlobDownloadEvent event) {
        HeavyTaskUtil.executeNewTask(() -> {
            JSONObject header = JSON.parseObject(event.getHeaderMap());
            String fileName = event.getFileName();
            if (StringUtil.isEmpty(fileName) || !fileName.contains(".")) {
                if (StringUtil.isEmpty(fileName)) {
                    fileName = StringUtil.md5(event.getUrl());
                }
                if (header.containsKey("content-type")) {
                    String mimeType = header.getString("content-type");
                    String ext = ShareUtil.getExtension(fileName, mimeType);
                    if (StringUtil.isNotEmpty(ext)) {
                        fileName = fileName + "." + ext;
                    }
                }
                if (!fileName.contains(".")) {
                    ThreadTool.INSTANCE.runOnUI(() -> ToastMgr.shortBottomCenter(getContext(), "获取文件格式失败"));
                    return;
                }
            }
            String b64 = event.getResult();
            if (b64.contains(",")) {
                b64 = b64.split(",")[1];
            }
            byte[] decode = Base64.decode(b64, Base64.DEFAULT);
            File dir = new File(DownloadChooser.getRootPath(getContext()));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = dir.getAbsolutePath() + File.separator + fileName;
            FileUtil.bytesToFile(filePath, decode);
            ThreadTool.INSTANCE.runOnUI(() -> {
                if (filePath.contains(".apk")) {
                    ShareUtil.findChooserToDeal(getContext(), filePath);
                }
                ToastMgr.shortBottomCenter(getContext(), "下载完成");
            });
        });
        ThreadTool.INSTANCE.runOnUI(() -> {
            onLoading(new LoadingEvent("", false));
        });
    }

    private void refreshVideoCount() {
        if (DetectorManager.getInstance().getVideoCount() > 0) {
            if (bottomBarXiuTanBg.getVisibility() == View.GONE) {
                bottomBarXiuTanBg.setVisibility(VISIBLE);
            }
            bottomBarXiuTan.setText((DetectorManager.getInstance().getVideoCount() + ""));
        } else {
            if (bottomBarXiuTanBg.getVisibility() == View.VISIBLE) {
                bottomBarXiuTanBg.setVisibility(GONE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showSearch(ShowSearchEvent showSearchEvent) {
        GlobalSearchPopup.startSearch(WebViewActivity.this, null
                , showSearchEvent.getText(), "web", getResources().getColor(R.color.white), true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showTranslate(ShowTranslateEvent event) {
        if (StringUtil.isEmpty(event.getText())) {
            return;
        }
        new XPopup.Builder(getContext())
                .asCustom(new TranslatePopup(getContext()).bind(event.getText()))
                .show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImgHrefFindEvent(OnImgHrefFindEvent event) {
        imgHref = event.getUrl();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearch(SearchEvent searchEvent) {
        if (!"web".equals(searchEvent.getTag())) {
            return;
        }
        String text = searchEvent.getText();
        if (text.startsWith("magnet:?")) {
            startDownloadMagnet(text);
            return;
        } else if (ThunderManager.INSTANCE.isFTPOrEd2k(text)) {
            startPlayFTP(text);
            return;
        }
        SearchEngine searchEngine = searchEvent.getSearchEngine();
        bottomBar.post(() -> {
            if (!SettingConfig.noWebHistory) {
                SearchHistroyModel.addHis(getContext(), text);
            }
            if (TextUtils.isEmpty(text)) {
                ToastMgr.shortCenter(getContext(), "不能为空哦");
                return;
            } else if (StringUtil.isWebUrl(text)) {
                if (!text.equals(webViewT.getUrl())) {
                    loadUrl(text, searchEvent.isNewWindow());
                }
                return;
            } else if (StringUtil.isUrl(text)) {
                String url1 = "http://" + text;
                if (!url1.equals(webViewT.getUrl())) {
                    loadUrl(url1, searchEvent.isNewWindow());
                }
                return;
            }
            loadUrl(HttpParser.parseSearchUrl(searchEngine.getSearch_url(), text), searchEvent.isNewWindow());
        });
    }

    private void loadUrl(String url, boolean newWindow) {
        if (webViewT == null) {
            return;
        }
        if (newWindow && StringUtil.isNotEmpty(webViewT.getUrl())) {
            addWindow(url);
        } else {
            webViewT.loadUrl(url);
        }
    }

    private void showUpdateRecords() {
        String text = FilesInAppUtil.getAssetsString(getContext(), "update_records.json");
        if (!TextUtils.isEmpty(text)) {
            UpdateRecord updateRecord = JSON.parseObject(text, UpdateRecord.class);
            if (updateRecord != null) {
                int updateVersion = PreferenceMgr.getInt(getContext(), "updateVersion", 1);
                if (updateRecord.getVersion() > updateVersion) {
                    PreferenceMgr.put(getContext(), "updateVersion", updateRecord.getVersion());
                    startActivity(new Intent(getContext(), UpdateRecordsActivity.class));
                }
            }
        }
    }

    @Override
    public void onPause() {
        try {
            if (webViewT != null) {
                WebUtil.setShowingUrl(webViewT.getUrl());
                webViewT.onPause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (floatVideoController != null) {
            floatVideoController.onPause();
        }
//        try {
//            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
//            listener = focusChange -> {
//
//            };
//            if (audioManager != null) {
//                int result = audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//                Log.d(TAG, "onPause: " + result);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        isOnPause = true;
        if (bottomHomeIcon != null && layoutParams != null && bottomHomeIcon.getVisibility() == View.VISIBLE) {
            layoutParams.bottomMargin = 0;
            bottomHomeIcon.setVisibility(View.INVISIBLE);
            bottomHomeIcon.setLayoutParams(layoutParams);
        }
        SettingConfig.saveAdblockPlusCount(getContext());
        super.onPause();
    }

    @Override
    public void onResume() {
        try {
            if (isOnPause) {
                if (webViewT != null) {
                    webViewT.onResume();
                    webViewT.resumeTimers();
                }
            }
//            if (audioManager != null) {
//                audioManager.abandonAudioFocus(listener);
//                audioManager = null;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (floatVideoController != null) {
            floatVideoController.onResume();
        }
        isOnPause = false;
        Looper.myQueue().addIdleHandler(() -> {
            checkClipboard();
            return false;
        });
        NotifyManagerUtils.Companion.checkNotificationOnResume(getContext());
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        WebUtil.setWebActivityExist(false);
        try {
            webViewT.setDetectedMediaResults(DetectorManager.getInstance().getDetectedMediaResults((Media) null));
            try {
                releaseWebViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
            saveAdBlockPlusCount();
            EventBus.getDefault().unregister(this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Application.setHasMainActivity(false);

        File contentCache = new File(UriUtils.getRootDir(getContext()) + File.separator + "_cache");
        if (contentCache.exists()) {
            FileUtil.deleteDirs(contentCache.getAbsolutePath());
        }
        SettingConfig.saveAdblockPlusCount(getContext());
        MultiWindowManager.instance(this).releaseAllWebview();
        JSEngine.getInstance().onDestroy();
        super.onDestroy();
    }

    private void saveAdBlockPlusCount() {
        try {
            if (SettingConfig.adblockplus_count != -1) {
                long adblockplus_count = PreferenceMgr.getLong(getContext(), "adblockplus_count", 0);
                if (adblockplus_count < SettingConfig.adblockplus_count) {
                    PreferenceMgr.put(getContext(), "adblockplus_count", SettingConfig.adblockplus_count);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private synchronized void releaseWebViews() {
        if (webViewT != null) {
            try {
                if (webViewT.getParent() != null) {
                    ((ViewGroup) webViewT.getParent()).removeView(webViewT);
                }
                webViewT.onPause();
            } catch (IllegalArgumentException ignored) {

            }
        }
    }

    @Override
    public void onBackPressed() {
        /** 回退键 事件处理 优先级:视频播放全屏-网页回退-关闭页面 */
        if (simpleHintPopupWindow != null && simpleHintPopupWindow.isShow()) {
            simpleHintPopupWindow.dismissPopupWindow();
            return;
        }
        if (settingPopupView != null && settingPopupView.isShow()) {
            settingPopupView.dismiss();
            return;
        }
        if (floatVideoController != null && floatVideoController.onBackPressed()) {
            return;
        }
        if (element_bg != null && element_bg.getVisibility() != GONE) {
            changeDebugView("close");
            return;
        }
        if (customView != null) {
            hideCustomView();
        } else if (webViewBg != null && webViewBg.getVisibility() != VISIBLE && isDragging()) {
            //长按拖拽排序状态
            for (Shortcut shortcut : shortcuts) {
                shortcut.setDragging(false);
            }
            shortcutAddView.setVisibility(GONE);
            //不使用shortcutAdapter.notifyDataSetChanged();不然图片会闪动
            shortcutAdapter.notifyItemRangeChanged(0, shortcuts.size());
            BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
        } else if (webViewT != null && webViewT.canGoBack()) {
            try {
                WebBackForwardList list = webViewT.copyBackForwardList();
                WebHistoryItem historyItem = list.getItemAtIndex(list.getCurrentIndex() - 1);
                if (floatVideoController != null && StringUtil.isNotEmpty(historyItem.getUrl())) {
                    floatVideoController.loadUrl(historyItem.getUrl());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            webViewT.goBack();
        } else {
            if (webViewBg != null) {
                if (MultiWindowManager.instance(this).getWebViewList().size() > 1) {
                    int pos = 0;
                    for (int i = 0; i < MultiWindowManager.instance(this).getWebViewList().size(); i++) {
                        if (webViewT == MultiWindowManager.instance(this).getWebViewList().get(i)) {
                            pos = i;
                            break;
                        }
                    }
                    webViewBg.removeView(webViewT);
                    HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).removeWebView(pos);
                    showNewWebView(webView);
                    HeavyTaskUtil.saveTabHistory(getActivity());
                    if (floatVideoController != null) {
                        if (StringUtil.isNotEmpty(webViewT.getUrl())) {
                            floatVideoController.loadUrl(webViewT.getUrl());
                        } else {
                            floatVideoController.destroy();
                        }
                    }
                    return;
                }
                if (webViewBg.getVisibility() == VISIBLE) {
                    backToHomeHtml();
                    if (floatVideoController != null) {
                        floatVideoController.destroy();
                    }
                    return;
                }
            }
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                mExitTime = System.currentTimeMillis();
                ToastMgr.shortBottomCenter(getContext(), "再按一次退出软件");
                return;
            }
            finish();
        }
    }

    private void backToHomeHtml() {
        backToHomeHtml(true);
    }

    private void backToHomeHtml(boolean checkHomeIcon) {
        if (webViewT != null && StringUtil.isNotEmpty(webViewT.getUrl())) {
            String lastUrl = webViewT.getUrl();
            //返回首页时，重建webview，使历史记录清空
            webViewT = MultiWindowManager.instance(this).recreate(webViewT);
            if (bottom_bar_refresh != null) {
                bottom_bar_refresh.setImageDrawable(getResources().getDrawable(R.drawable.right));
                bottom_bar_refresh.setTag(lastUrl);
            }
        }
        getProgress_bar().hide();
        webViewBg.setVisibility(GONE);
        View shortcut_container = findView(R.id.shortcut_container);
        shortcut_container.setAlpha(0f);
        shortcut_container.setVisibility(VISIBLE);
        shortcut_container.animate().alpha(1f).setDuration(300).start();
        bottomTitleView.setText(("主页"));


        boolean fullTheme = PreferenceMgr.getBoolean(getContext(), KEY_FULL_THEME, false);
        if (!fullTheme) {
            StatusBarCompatUtil.setStatusBarColor(WebViewActivity.this, getResources().getColor(R.color.white));
        }
        homeTag = true;
        DetectorManager.getInstance().startDetect();
        refreshVideoCount();

        if (hasBackground()) {
            bottomBar.setBackground(null);
            AndroidBarUtils.setTranslucentStatusBar(this, true);
            bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackground(null);
            if (PreferenceMgr.getBoolean(getContext(), "home_logo_dark", false)) {
                WindowInsetsControllerCompat wic = getWindowInsetsController();
                if (wic != null) {
                    // true表示Light Mode，状态栏字体呈黑色，反之呈白色
                    wic.setAppearanceLightStatusBars(true);
                }
            }
            updateNavBarColor(getHomeBackground());
        } else {
            if (PreferenceMgr.getBoolean(getContext(), "ib1", false)) {
                updateBottomBarBackground();
            }
        }
        if (checkHomeIcon && bottomHomeIcon.getVisibility() == View.VISIBLE) {
            layoutParams.bottomMargin = 0;
            bottomHomeIcon.setVisibility(View.INVISIBLE);
            bottomHomeIcon.setLayoutParams(layoutParams);
        }
        HeavyTaskUtil.saveTabHistory(getActivity());
        hideMagnetIcon();
//        finish();
    }


    private void changeUA() {
        int selectPos = 0;
        String[] uas = UAModel.getUaList();
        String setUa = PreferenceMgr.getString(getContext(), "vip", "ua", "");
        if (UAModel.hasAdjustUa(webViewT.getUrl())) {
            selectPos = uas.length - 1;
        } else if (UAModel.WebUA.PC.getContent().equals(setUa)) {
            selectPos = 1;
        } else if (UAModel.WebUA.IPhone.getContent().equals(setUa)) {
            selectPos = 2;
        } else if (StringUtil.isNotEmpty(UAModel.getUseUa())) {
            selectPos = 3;
        }
        new XPopup.Builder(getContext())
                .asCenterList("设置UA", uas, (int[]) null, selectPos, new OnSelectListener() {
                    @Override
                    public void onSelect(int which, String s) {
                        List<HorizontalWebView> webViews = MultiWindowManager.instance(WebViewActivity.this).getWebViewList();
                        switch (which) {
                            case 0:
                                UAModel.setUseUa(null);
                                PreferenceMgr.put(getContext(), "vip", "ua", "");
                                if (CollectionUtil.isNotEmpty(webViews)) {
                                    for (HorizontalWebView webView : webViews) {
                                        webView.getWebViewHelper().setLastDom("");
                                    }
                                }
                                webViewT.reload();
                                break;
                            case 1:
                                String ua1 = UAModel.WebUA.PC.getContent();
                                UAModel.setUseUa(ua1);
                                PreferenceMgr.put(getContext(), "vip", "ua", ua1);
                                if (CollectionUtil.isNotEmpty(webViews)) {
                                    for (HorizontalWebView webView : webViews) {
                                        webView.getWebViewHelper().setLastDom("");
                                    }
                                }
                                webViewT.reload();
                                break;
                            case 2:
                                String ua2 = UAModel.WebUA.IPhone.getContent();
                                UAModel.setUseUa(ua2);
                                PreferenceMgr.put(getContext(), "vip", "ua", ua2);
                                if (CollectionUtil.isNotEmpty(webViews)) {
                                    for (HorizontalWebView webView : webViews) {
                                        webView.getWebViewHelper().setLastDom("");
                                    }
                                }
                                webViewT.reload();
                                break;
                            case 3:
                                String userUa = (String) PreferenceMgr.get(getContext(), "vip", "userUa", "");
                                final View view1 = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_web_add, null, false);
                                final EditText titleE = view1.findViewById(R.id.web_add_title);
                                view1.findViewById(R.id.web_add_url).setVisibility(GONE);
                                titleE.setHint("请输入UA");
                                titleE.setText(userUa);
                                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                        .setTitle("自定义全局UA")
                                        .setView(view1)
                                        .setCancelable(true)
                                        .setPositiveButton("确定", (dialog2, which2) -> {
                                            String title = titleE.getText().toString();
                                            if (TextUtils.isEmpty(title)) {
                                                ToastMgr.shortBottomCenter(getContext(), "请输入UA");
                                            } else {
                                                title = StringUtil.replaceBlank(title);
                                                dialog2.dismiss();
                                                PreferenceMgr.put(getContext(), "vip", "ua", title);
                                                PreferenceMgr.put(getContext(), "vip", "userUa", title);
                                                UAModel.setUseUa(title);
                                                if (CollectionUtil.isNotEmpty(webViews)) {
                                                    for (HorizontalWebView webView : webViews) {
                                                        webView.getWebViewHelper().setLastDom("");
                                                    }
                                                }
                                                webViewT.reload();
                                            }
                                        }).setNegativeButton("取消", (dialog2, which3) -> dialog2.dismiss())
                                        .create();
                                DialogUtil.INSTANCE.showAsCard(getContext(), alertDialog);
                                break;
                            case 4:
                                UAModel.showUpdateOrAddDialog(getContext(), webViewT.getUrl(), ua -> {
                                    if (CollectionUtil.isNotEmpty(webViews)) {
                                        for (HorizontalWebView webView : webViews) {
                                            webView.getWebViewHelper().setLastDom("");
                                        }
                                    }
                                    webViewT.reload();
                                });
                                break;
                        }
                    }
                }).show();
    }

    private void setXiuTanFastPlayDialog() {
        String title = "会直接快速播放视频链接，是否修改该网站的自动嗅探播放设置？";
        String button = "移除速播";
        if (!SettingConfig.fastPlayDoms.contains(StringUtil.getDom(webViewT.getUrl()))) {
            title = "不" + title;
            button = "快速播放";
        }
        ColorDialog dialog = new ColorDialog(getContext());
        dialog.setTheTitle("设置嗅探快速播放")
                .setContentText("已支持自定义嗅探自动播放，默认所有网站不会快速播放。快速播放就是网站嗅探到一个视频就会自动播放该视频，当前网站已设置为：" + title)
                .setPositiveListener(button, dialog1 -> {
                    DetectorManager.getInstance().addOrDeleteFormXiuTanFastPlayList(getContext(), webViewT.getUrl());
                    ToastMgr.shortBottomCenter(getContext(), "修改成功！");
                    dialog1.dismiss();
                }).setNegativeListener("取消", ColorDialog::dismiss).show();
    }

    private void setXiuTanDialog() {
        String title = "会自动弹窗嗅探结果，是否修改该网站的弹窗设置？";
        String button = "不再弹窗";
        if (DetectorManager.getInstance().inXiuTanDialogBlackList(webViewT.getUrl())) {
            title = "不" + title;
            button = "自动弹窗";
        }
        ColorDialog dialog = new ColorDialog(getContext());
        dialog.setTheTitle("设置嗅探弹窗")
                .setContentText("已支持自定义嗅探到视频是否自动弹窗，默认所有网站均自动弹窗。当前网站：" + StringUtil.getDom(webViewT.getUrl()) + "，" + title)
                .setPositiveListener(button, dialog1 -> {
                    DetectorManager.getInstance().addOrDeleteFormXiuTanDialogList(webViewT.getUrl());
                    ToastMgr.shortBottomCenter(getContext(), "修改成功！");
                    dialog1.dismiss();
                }).setNegativeListener("取消", ColorDialog::dismiss).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlChangeEvent(WebViewUrlChangedEvent event) {
        finish();
    }

    /**
     * 检测到视频
     *
     * @param videoEvent 视频
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFindVideoEvent(FindVideoEvent videoEvent) {
        if (isFinishing() || webViewT == null) {
            return;
        }
        String nowUrl = webViewT.getUrl();
        String dom = StringUtil.getDom(nowUrl);
        if (ArticleListRuleEditActivity.hasBlockDom(dom)) {
            return;
        }
        WebUtil.setShowingUrl(nowUrl);
        if (!StringUtils.equals(nowUrl, videoPlayingWebUrl)) {
            //说明是hash发生了变化，当成新页面处理，重新显示嗅探到视频
            hasDismissXiuTan = false;
            isToastShow = false;
            hasAutoPlay = false;
            videoPlayingWebUrl = nowUrl;
            DetectorManager.getInstance().reset();
        }
        try {
            webViewT.evaluateJavascript("(function(){\n" +
                    "\twindow.videoUrls = \"" + WebViewHelper.getVideoUrls() + "\";\n" +
                    "})();", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bottomBarXiuTanBg.getVisibility() == View.GONE) {
            bottomBarXiuTanBg.setVisibility(VISIBLE);
        }
        bottomBarXiuTan.setText(videoEvent.getTitle());
        if (DomainConfigKt.isDisableXiuTan(dom)) {
            return;
        }
        if (!hasDismissXiuTan) {
            hasDismissXiuTan = true;
            if (!hasAutoPlay) {
                if (!DetectorManager.getInstance().inXiuTanDialogBlackList(webViewT.getUrl()) && !isToastShow) {
                    if (floatVideoController != null && !UrlDetector.isMusic(videoEvent.getMediaResult().getUrl())) {
                        if (DomainConfigKt.isDisableFloatVideo(dom)) {
                            return;
                        }
                        videoEvent.getMediaResult().setClicked(true);
                        floatVideoController.show(videoEvent.getMediaResult().getUrl(), nowUrl, webViewT.getTitle(), true);
                    } else {
                        if (!SettingConfig.xiuTanNotify) {
                            //关闭了嗅探提示，那么快速播放也同时关闭
                            return;
                        }
                        showVideoToast();
                    }
                }
            }
        }
        if (fastPlayFromLiked) {
            String url = StringUtil.getDom(videoEvent.getMediaResult().getUrl());
            if (DetectorManager.getInstance().inXiuTanLiked(getContext(), dom, url)) {
                int code = getIntent().getIntExtra("uWho", 0);
                if ((code == 0 || code == 304) && !isOnPause && !hasAutoPlay) {
                    hasAutoPlay = true;
                    //只有嗅探到的地址跟当前域名不一致才自动播放，因为很多时候当前域名嗅探的同域名下的地址都不是正确的视频地址
                    boolean floatVideo = PreferenceMgr.getBoolean(getContext(), "floatVideo", false);
                    if (!StringUtils.equals(StringUtil.getDom(url), dom) && !floatVideo) {
                        ToastMgr.shortBottomCenter(getContext(), "已自动播放常用的嗅探地址");
                        videoEvent.getMediaResult().setClicked(true);
                        startPlayVideo(videoEvent.getMediaResult().getUrl());
                    }
                }
            }
        }
    }

    private void showVideoToast() {
        if (toastView == null) {
            findView(R.id.view_stub_main_toast).setVisibility(VISIBLE);
            bindToastView();
        }
        toastView.setVisibility(View.VISIBLE);
        isToastShow = true;
        toastView.animate().scaleY(1).scaleX(1).setDuration(300).start();
        new Handler().postDelayed(() -> {
            if (isToastShow && !isFinishing()) {
                toastView.animate().scaleY(0).scaleX(0).setDuration(300).start();
                isToastShow = false;
                new Handler().postDelayed(() -> {
                    if (!isToastShow && !isFinishing()) {
                        toastView.setVisibility(View.INVISIBLE);
                    }
                }, 270);
            }
        }, 4000);
    }

    private void startLoadUrl() {
        blockImg = PreferenceMgr.getBoolean(getContext(), "blockImg", false);
        isUsePlayer = getIntent().getBooleanExtra("isUsePlayer", false);
        fastPlayFromLiked = PreferenceMgr.getBoolean(getContext(), "fastPlayFromLiked", true);
        checkIntent();
//        webViewT.loadUrl(url);
//        DetectorManager.getInstance().createThread();
    }

    private void initWebView() {
        //滑动前进后退
        boolean scrollForwardAndBack = PreferenceMgr.getBoolean(getContext(), "scrollForwardAndBack", true);
        if (scrollForwardAndBack) {
            initWebViewBgListener();
        }
        initLoadListener();
        webViewT = MultiWindowManager.instance(this).initBaseWebview(false);
        webViewBg.addView(webViewT);
        webViewT.postDelayed(() -> {
            try {
                CookieManager.getInstance().setAcceptCookie(true);
                CookieManager.allowFileSchemeCookies();
                CookieManager.getInstance().setAcceptThirdPartyCookies(webViewT, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 500);
    }

    @Override
    public View getSnackBarBg() {
        if (snackBarBg == null) {
            snackBarBg = findView(R.id.snack_bar_bg);
        }
        return snackBarBg;
    }

    @Override
    public boolean isOnPause() {
        return isOnPause;
    }

    private void chooseOperationForUrl(final String url) {
        List<View.OnClickListener> clickList = new ArrayList<>();
        clickList.add(v -> runOnUiThread(() -> addWindow(url, true, webViewT)));
        clickList.add(v -> runOnUiThread(() -> addWindow(url, false, webViewT)));
        clickList.add(v -> ClipboardUtil.copyToClipboard(getContext(), url));
        clickList.add(v -> customCopy(url));
        clickList.add(v -> runOnUiThread(() -> ShareUtil.findChooserToDeal(getContext(), url, "text/html")));
        clickList.add(v -> runOnUiThread(() -> webViewT.evaluateJavascript("(function(){window.getAdRule()})();", null)));
        clickList.add(v -> runOnUiThread(() -> blockDom(url)));
        clickList.add(v -> runOnUiThread(() -> {
            showDebugView(true);
            webViewT.evaluateJavascript("(function(){window.setDebugState(true)})();", null);
        }));

        simpleHintPopupWindow = new SimpleHintPopupWindow(WebViewActivity.this, Arrays.asList("新窗口打开", "后台打开", "复制链接", "复制文本", "外部打开", "拦截网页元素", "拦截过滤网址", "拦截元素(编辑)"), clickList);
        simpleHintPopupWindow.showPopupWindow(webViewT.getFocusX(), webViewT.getFocusY());
    }

    private void customCopy(String url) {
        webViewT.evaluateJavascript("(function(){window.getAText('" + Utils.escapeJavaScriptString(url) + "')})();", null);
        ;
    }

    private void blockDom(String url) {
        if (TextUtils.isEmpty(url)) {
            ToastMgr.shortBottomCenter(getContext(), "链接不能为空！");
            return;
        }
        if (UrlDetector.isImage(url)) {
            blockDomByUrl(url);
        } else {
            webViewT.evaluateJavascript("window.getImgByUrl('" + url + "')", s -> {
//                Log.d(TAG, "blockDom: window.getImgByUrl-->s=" + s);
                if (TextUtils.isEmpty(s) || "null".equals(s)) {
                    blockDomByUrl(url);
                } else {
                    blockDomByUrl(s);
                }
            });
        }
    }

    private void blockDomByUrl(String url) {
        url = url.replace("http://", "").replace("https://", "");
        final View view1 = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_block_url_add, null, false);
        final EditText titleE = view1.findViewById(R.id.block_add_text);
        View block_add_dom = view1.findViewById(R.id.block_add_dom);
        View block_add_url = view1.findViewById(R.id.block_add_url);
        View block_add_dom2 = view1.findViewById(R.id.block_add_dom2);
        View global = view1.findViewById(R.id.block_add_global);
        View domain = view1.findViewById(R.id.block_add_domain);
        String finalUrl = url;
        block_add_dom.setOnClickListener(v -> {
            titleE.setText(StringUtil.getDom(finalUrl).split(":")[0]);
            block_add_url.setBackground(getDrawable(R.drawable.button_layer));
            block_add_dom.setBackground(getDrawable(R.drawable.button_layer_red));
            block_add_dom2.setBackground(getDrawable(R.drawable.button_layer));
        });
        block_add_dom2.setOnClickListener(v -> {
            String dom = StringUtil.getDom(finalUrl).split(":")[0];
            String[] doms = dom.split("\\.");
            titleE.setText(StringUtil.arrayToString(doms, Math.max(0, doms.length - 2), doms.length, "."));
            block_add_url.setBackground(getDrawable(R.drawable.button_layer));
            block_add_dom.setBackground(getDrawable(R.drawable.button_layer));
            block_add_dom2.setBackground(getDrawable(R.drawable.button_layer_red));
        });
        block_add_url.setOnClickListener(v -> {
            titleE.setText(finalUrl);
            block_add_dom.setBackground(getDrawable(R.drawable.button_layer));
            block_add_url.setBackground(getDrawable(R.drawable.button_layer_red));
            block_add_dom2.setBackground(getDrawable(R.drawable.button_layer));
        });
        global.setOnClickListener(v -> {
            titleE.setText(titleE.getText().toString().split("@domain=")[0]);
            domain.setBackground(getDrawable(R.drawable.button_layer));
            global.setBackground(getDrawable(R.drawable.button_layer_red));
        });
        domain.setOnClickListener(v -> {
            String dom = StringUtil.getDom(webViewT.getUrl());
            titleE.setText((titleE.getText().toString().split("@domain=")[0] + "@domain=" + dom));
            global.setBackground(getDrawable(R.drawable.button_layer));
            domain.setBackground(getDrawable(R.drawable.button_layer_red));
        });
        titleE.setHint("请输入要拦截的网址");
        titleE.setText(StringUtil.getDom(url).split(":")[0]);
        new AlertDialog.Builder(getContext())
                .setTitle("新增网址拦截")
                .setView(view1)
                .setCancelable(true)
                .setPositiveButton("拦截", (dialog, which) -> {
                    String title = titleE.getText().toString();
                    if (TextUtils.isEmpty(title)) {
                        ToastMgr.shortBottomCenter(getContext(), "请输入要拦截的网址");
                    } else {
                        AdUrlBlocker.instance().addUrl(title);
                        CleanMessageUtil.clearWebViewCache(getActivity());
                        if (!SettingConfig.shouldBlock()) {
                            ToastMgr.shortBottomCenter(getContext(), "保存成功，但您关闭了广告拦截，因此不会生效");
                        } else {
                            ToastMgr.shortBottomCenter(getContext(), "保存成功");
                        }
                        webViewT.postDelayed(() -> {
                            if (webViewT != null && !isFinishing()) {
                                webViewT.reload();
                            }
                        }, 300);
                    }
                }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
    }

    private void saveAdBlock(String rule) {
        String blockJs = AdBlockModel.saveBlockRule(webViewT.getUrl(), rule);
        if (!TextUtils.isEmpty(blockJs)) {
            webViewT.evaluateJavascript(blockJs, null);
            if (!SettingConfig.shouldBlock()) {
                ToastMgr.shortBottomCenter(getContext(), "保存成功，但您关闭了广告拦截，因此不会生效");
            } else {
                ToastMgr.shortBottomCenter(getContext(), "已保存拦截规则");
            }
        }
    }


    private void goImgHref(String url, boolean showNow) {
        if (StringUtil.isEmpty(imgHref)) {
            webViewT.evaluateJavascript("(function(){window.getImgHref('" + Utils.escapeJavaScriptString(url) + "')})();", null);
            webViewT.postDelayed(() -> {
                if (StringUtil.isEmpty(imgHref)) {
                    ToastMgr.shortBottomCenter(getContext(), "获取图片跳转地址失败");
                } else {
                    addWindow(HttpUtil.getRealUrl(webViewT.getUrl(), imgHref), showNow, webViewT);
                }
            }, 300);
        } else {
            addWindow(HttpUtil.getRealUrl(webViewT.getUrl(), imgHref), showNow, webViewT);
        }
    }

    private void chooseOperationForImageUrl(View view, final String url, boolean anchor) {
        List<View.OnClickListener> clickList = new ArrayList<>();
        if (anchor) {
            imgHref = null;
            webViewT.evaluateJavascript("(function(){window.getImgHref('" + Utils.escapeJavaScriptString(url) + "')})();", null);
            clickList.add(v -> runOnUiThread(() -> goImgHref(url, true)));
            clickList.add(v -> runOnUiThread(() -> goImgHref(url, false)));
        }
        clickList.add(v -> showBigPic(url));
        clickList.add(v -> savePic(url));
        clickList.add(v -> runOnUiThread(() -> webViewT.evaluateJavascript("(function(){window.getImgUrls()})();", null)));
        clickList.add(v -> runOnUiThread(() -> ClipboardUtil.copyToClipboard(getContext(), url)));
        clickList.add(v -> runOnUiThread(() -> decodePicQR(url)));
        clickList.add(v -> runOnUiThread(() -> webViewT.evaluateJavascript("(function(){window.getAdRule()})();", null)));
        clickList.add(v -> runOnUiThread(() -> blockDom(url)));
        clickList.add(v -> runOnUiThread(() -> {
            showDebugView(true);
            webViewT.evaluateJavascript("(function(){window.setDebugState(true)})();", null);
        }));
        String[] names;
        if (anchor) {
            names = new String[]{"新窗口打开", "后台打开", "全屏查看", "保存图片", "看图模式", "复制图片链接", "识别二维码", "拦截网页元素", "拦截过滤网址", "拦截元素(编辑)"};
        } else {
            names = new String[]{"全屏查看", "保存图片", "看图模式", "复制链接", "识别二维码", "拦截网页元素", "拦截过滤网址", "拦截元素(编辑)"};
        }
        simpleHintPopupWindow = new SimpleHintPopupWindow(WebViewActivity.this, Arrays.asList(names), clickList);
        simpleHintPopupWindow.showPopupWindow(webViewT.getFocusX(), webViewT.getFocusY());
    }

    private void decodePicQR(String url) {
        //识别二维码
        String uu = webViewT.getUrl();
        HeavyTaskUtil.executeNewTask(() -> {
            File file = new PopImageLoaderNoView(uu).getImageFile(getContext(), getImageUrl(url));
            if (file == null || !file.exists()) {
                ToastMgr.shortBottomCenter(getContext(), "下载图片失败");
                return;
            }
            new DecodeImgThread(file.getAbsolutePath(), new DecodeImgCallback() {
                @Override
                public void onImageDecodeSuccess(Result result) {
                    dealQRContent(result.getText());
                }

                @Override
                public void onImageDecodeFailed() {
                    ThreadTool.INSTANCE.runOnUI(() -> ToastMgr.shortBottomCenter(getContext(), "识别失败"));
                }
            }).start();
        });
    }

    private void savePic(String url) {
        ImgUtil.savePic2Gallery(getContext(), getImageUrl(url), webViewT.getUrl(), new ImgUtil.OnSaveListener() {
            @Override
            public void success(List<String> paths) {
                runOnUiThread(() -> ToastMgr.shortBottomCenter(getContext(), "保存成功"));
            }

            @Override
            public void failed(String msg) {
                runOnUiThread(() -> ToastMgr.shortBottomCenter(getContext(), msg));
            }
        });
    }

    private String getImageUrl(String url) {
        return GlideUtil.getImageUrl(url, WebViewHelper.getRequestHeaderMap(webViewT, url));
    }

    private void showBigPic(String pic) {
        List<DetectedMediaResult> images = DetectorManager.getInstance().getDetectedMediaResults(MediaType.IMAGE);
        List<Object> imageUrls = new ArrayList<>(images.size());
        int pos = -1;
        for (int i = 0; i < images.size(); i++) {
            DetectedMediaResult result1 = images.get(i);
            imageUrls.add(getImageUrl(result1.getUrl()));
            if (StringUtil.equalsDomUrl(pic, result1.getUrl())) {
                pos = i;
            }
        }
        if (pos != -1) {
            new MyXpopup().Builder(getContext()).asImageViewer(null, pos, imageUrls, null, new PopImageLoaderNoView(webViewT.getUrl()))
                    .show();
        } else {
            new MyXpopup().Builder(getContext())
                    .asImageViewer(null, pic, new PopImageLoaderNoView(webViewT.getUrl()))
                    .show();
        }
    }

    private void chooseOperationForUnknown(View view) {
        webViewT.evaluateJavascript("(function(){\n" +
                        "    return window.getTouchElement()\n" +
                        "})()",
                value -> {
                    if (StringUtil.isNotEmpty(value) && value.length() > 2) {
                        String result = value.substring(1, value.length() - 1);
                        String[] strings = result.split("@//@", -1);
                        if (strings.length < 2) {
                            return;
                        }
                        runOnUiThread(() -> {
                            List<View.OnClickListener> clickList = new ArrayList<>();
                            List<String> names = new ArrayList<>();
                            if (StringUtil.isNotEmpty(strings[1])) {
                                names.add("复制文本");
                                clickList.add(v -> ClipboardUtil.copyToClipboardForce(getContext(), strings[1]));
                            }
                            if (strings.length > 2 && StringUtil.isNotEmpty(strings[2]) && strings[2].startsWith("http")) {
                                names.add(0, "新窗口打开");
                                clickList.add(0, v -> addWindow(strings[2], true, webViewT));
                                names.add("复制链接");
                                clickList.add(v -> ClipboardUtil.copyToClipboardForce(getContext(), strings[2]));
                            }
                            if (strings.length > 3 && StringUtil.isNotEmpty(strings[3]) && strings[3].startsWith("http")) {
                                names.add("全屏查看");
                                String pic = strings[3];
                                clickList.add(v -> showBigPic(pic));
                                names.add("保存图片");
                                clickList.add(v -> savePic(pic));

                                names.add("看图模式");
                                clickList.add(v -> runOnUiThread(() -> webViewT.evaluateJavascript("(function(){window.getImgUrls()})();", null)));
                                names.add("识别二维码");
                                clickList.add(v -> runOnUiThread(() -> decodePicQR(pic)));
                            }
                            names.add("拦截元素");
                            clickList.add(v -> runOnUiThread(() -> {
                                showDebugView(true);
                                webViewT.evaluateJavascript("(function(){window.setDebugState(true)})();", null);
                            }));
                            simpleHintPopupWindow = new SimpleHintPopupWindow(WebViewActivity.this, names, clickList);
                            simpleHintPopupWindow.showPopupWindow(webViewT.getFocusX(), webViewT.getFocusY());
                        });
                    }
                }
        );
    }

    private void setHomeBg(String path) {
        PreferenceMgr.put(getContext(), "home_bg", path);
        background = path;
        refreshBackground();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateBgByFile(UpdateBgEvent event) {
        File a = new File(event.getPath());
        String zipFilePath = UriUtils.getRootDir(getContext()) + File.separator + "images" + File.separator + a.getName();
        File dir = new File(UriUtils.getRootDir(getContext()) + File.separator + "images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileUtil.copy(a, new File(zipFilePath));
        a.delete();
        setHomeBg(zipFilePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 101) {
                if (resultCode == 101) {
                    if (settingPopupView != null && settingPopupView.isShow()) {
                        settingPopupView.dismiss();
                    }
                    webViewT.loadUrl(data.getStringExtra("url"));
                }
                return;
            } else if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    UriUtils.getFilePathFromURI(getContext(), uri, new UriUtils.LoadListener() {
                        @Override
                        public void success(String s) {
                            File a = new File(s);
                            if (!isFinishing() && a.exists()) {
                                runOnUiThread(() -> updateBgByFile(new UpdateBgEvent(s)));
                            }
                        }

                        @Override
                        public void failed(String msg) {
                            if (!isFinishing()) {
                                runOnUiThread(() -> {
                                    ToastMgr.shortBottomCenter(getContext(), "出错：" + msg);
                                });
                            }
                        }
                    });
                }
                return;
            } else if (requestCode == 2) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    UriUtils.getFilePathFromURI(getContext(), uri, new UriUtils.LoadListener() {
                        @Override
                        public void success(String s) {
                            File a = new File(s);
                            if (!isFinishing() && a.exists()) {
                                runOnUiThread(() -> {
                                    try {
                                        String[] as = s.split("/");
                                        //如果有文件格式则判断格式，如果没有文件格式则不管
                                        if (as[as.length - 1].contains(".") && !"zip".equals(FileUtil.getExtension(s))) {
                                            ToastMgr.shortBottomCenter(getContext(), "格式有误，仅支持zip文件：" + s);
                                            return;
                                        }
                                        String zipFilePath = BackupUtil.genBackupZipPath(getContext());
                                        FileUtil.copy(a, new File(zipFilePath));
                                        a.delete();
                                        BackupUtil.recoveryDBAndJsNow(getContext());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }

                        @Override
                        public void failed(String msg) {
                            if (!isFinishing()) {
                                runOnUiThread(() -> {
                                    ToastMgr.shortBottomCenter(getContext(), "出错：" + msg);
                                });
                            }
                        }
                    });
                }
                return;
            } else if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
                if (data != null) {
                    String content = data.getStringExtra(Constant.CODED_CONTENT);
                    if (StringUtil.isNotEmpty(content)) {
                        dealQRContent(content);
                    }
                }
                return;
            }
            if (resultCode == RESULT_OK) {
                if (requestCode == 0) {
                    if (null != filePathCallback) {
                        Uri result = data == null ? null : data.getData();
                        filePathCallback.onReceiveValue(new Uri[]{result});
                        filePathCallback = null;
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (null != filePathCallback) {
                    filePathCallback.onReceiveValue(null);
                    filePathCallback = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DebugUtil.showErrorMsg(this, getContext(), "文件上传回调失败", e.getMessage(), "" + requestCode, e);
        }
    }

    private void dealQRContent(String content) {
        ThreadTool.INSTANCE.runOnUI(() -> {
            if (content.startsWith("http") && webViewT != null) {
                if (StringUtil.isNotEmpty(webViewT.getUrl())) {
                    addWindow(content);
                } else {
                    webViewT.loadUrl(content);
                }
            } else {
                new XPopup.Builder(getContext())
                        .asConfirm("扫描结果", content, "取消", "复制", () -> {
                            ClipboardUtil.copyToClipboardForce(getContext(), content);
                        }, () -> {
                        }, false).show();
            }
        });
    }

    /**
     * 视频播放全屏 函数集合
     **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (floatVideoController != null && floatVideoController.isFullScreen()) {
            callback.onCustomViewHidden();
            return;
        }
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer = new VideoContainer(getActivity(), webViewT);
        fullscreenContainer.addVideoView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        customViewCallback = callback;
        webViewT.setVisibility(View.INVISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setStatusBarVisibility(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 隐藏视频全屏
     */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        boolean fullTheme = PreferenceMgr.getBoolean(getContext(), KEY_FULL_THEME, false);
        if (!fullTheme) {
            setStatusBarVisibility(true);
            StatusBarCompatUtil.setStatusBarColor(this, webViewT.getStatusBarColor());
        }
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer.destroy();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        webViewT.setVisibility(VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public WebView getWebView() {
        return webViewT;
    }

    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    /**
     * 为啥不用setStatusBarVisibility，因为从带图主页开启全屏模式进入网页状态栏没有全屏
     *
     * @param visible
     */
    private void setStatusBarVisibilityFullTheme(boolean visible) {
        int i = Build.VERSION.SDK_INT;
        if (!visible) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            //必须加上这个才能真正全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(setSystemUiVisibility);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /**
     * 为啥不直接用setStatusBarVisibilityFullTheme，因为会出现横屏切换竖屏时屏幕左侧出现和状态栏高度一样的白边
     *
     * @param visible
     */
    private void setStatusBarVisibility(boolean visible) {
        int i = Build.VERSION.SDK_INT;
        Window localWindow = getWindow();
        if (!visible) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            WindowInsetsControllerCompat controllerCompat = getWindowInsetsController();
            if (controllerCompat != null) {
                controllerCompat.hide(WindowInsetsCompat.Type.systemBars());
                return;
            }
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            WindowInsetsControllerCompat controllerCompat = getWindowInsetsController();
            if (controllerCompat != null) {
                controllerCompat.show(WindowInsetsCompat.Type.systemBars());
                return;
            }
        }
        if (!visible) {
            localWindow.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            localWindow.getDecorView().setSystemUiVisibility(setSystemUiVisibility);
        }
    }

    private void showVideoList() {
        List<DetectedMediaResult> results = DetectorManager.getInstance().getDetectedMediaResults(MediaType.VIDEO_MUSIC);
        if (CollectionUtil.isEmpty(results)) {
            new XPopup.Builder(getContext()).asConfirm("温馨提示", "没有嗅探到的视频", null)
                    .show();
            return;
        }
        new XPopup.Builder(getContext())
                .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
                .asCustom(new XiuTanResultPopup(getContext()).with(results, (url, type) -> {
                    if ("悬浮播放".equals(type)) {
                        if (floatVideoController != null && webViewT.getUrl() != null) {
                            floatVideoController.show(url, webViewT.getUrl(), webViewT.getTitle(), false);
                        }
                    } else if ("play".equals(type)) {
                        startPlayVideo(url);
                    } else if ("复制链接".equals(type)) {
                        ClipboardUtil.copyToClipboard(getContext(), url);
                    } else {
                        startDownloadVideo(url);
                    }
                })/*.enableDrag(false)*/)
                .show();
    }

    @Subscribe
    public void setWebTitle(OnSetWebTitleEvent event) {
        runOnUiThread(() -> bottomTitleView.setText(event.getTitle()));
    }

    @Subscribe
    public void setAdBlock(OnSetAdBlockEvent event) {
        runOnUiThread(() -> {
            try {
                debug_rule_text.setText(event.getRule());
                debug_node_text.setText(event.getHtml());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Subscribe
    public void saveAdBlockRule(OnSaveAdBlockRuleEvent event) {
        runOnUiThread(() -> {
            try {
                debug_rule_text.setText(event.getRule());
                String adBlockRule = debug_rule_text.getText().toString();
                if (TextUtils.isEmpty(adBlockRule)) {
                    ToastMgr.shortBottomCenter(getContext(), "获取拦截规则失败");
                } else {
                    saveAdBlock(adBlockRule);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Subscribe
    public void onFindInfo(OnFindInfoEvent event) {
        runOnUiThread(() -> searchInfo.setText(event.getSearchInfo()));
    }

    @Subscribe
    public void onMenuItemClick(OnMenuItemClickEvent event) {
        runOnUiThread(() -> {
            showDebugView(true);
            getWebView().evaluateJavascript("(function(){window.setDebugState(true)})();", null);
        });
    }

    @Subscribe
    public void onWebViewLongClick(OnLongClickEvent event) {
        WebView.HitTestResult result = event.getResult();
        int type = result.getType();
        Log.d(TAG, "initWebView: setOnLongClickListener--type=" + type + ",url=" + result.getExtra());
        if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                || type == WebView.HitTestResult.IMAGE_TYPE) {
            chooseOperationForImageUrl(webViewT, result.getExtra(), type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE);
        } else if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
            if (UrlDetector.isImage(result.getExtra())) {
                chooseOperationForImageUrl(webViewT, result.getExtra(), true);
            } else {
                chooseOperationForUrl(result.getExtra());
            }
        } else if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
            chooseOperationForUnknown(webViewT);
        }
    }

    private WebProgress getProgress_bar() {
        if (progress_bar == null) {
            progress_bar = findView(R.id.progress_bar);
            progress_bar.setColor(getResources().getColor(R.color.progress_blue));
        }
        return progress_bar;
    }

    @Subscribe
    public void onProgressChanged(OnProgressChangedEvent event) {
        getProgress_bar().setWebProgress(event.getProgress());
        if (event.getProgress() != 100) {
//            bottomTitleView.setText(("加载中" + event.getProgress() + "%"));
        } else {
            String title1 = webViewT.getTitle();
            if (!TextUtils.isEmpty(title1)) {
                bottomTitleView.setText(title1);
            }
            finishPageNow(webViewT.getUrl(), title1);
        }
    }

    @Subscribe
    public void onShowCustomView(OnShowCustomViewEvent event) {
        showCustomView(event.getView(), event.getCallback());
    }

    @Subscribe
    public void onHideCustomView(OnHideCustomViewEvent event) {
        hideCustomView();
    }

    @Subscribe
    public void onShowFileChooser(OnShowFileChooserEvent event) {
        filePathCallback = event.getFilePathCallback();
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "Chooser"), 0);
    }

    @Subscribe
    public void onPageStarted(OnPageStartEvent event) {
        if (homeTag) {
            homeTag = false;
            webViewT.clearHistory();
        }
        if (bottom_bar_refresh != null && bottom_bar_refresh.getTag() != null) {
            bottom_bar_refresh.setImageDrawable(getResources().getDrawable(getBottomView2DefaultDrawable()));
            bottom_bar_refresh.setTag(null);
        }
        if (isToastShow && toastView != null) {
            toastView.animate().scaleY(0).scaleX(0).setDuration(300).start();
            isToastShow = false;
            new Handler().postDelayed(() -> {
                if (!isToastShow) {
                    toastView.setVisibility(View.INVISIBLE);
                }
            }, 270);
        }

        bottomBarXiuTanBg.setVisibility(View.GONE);
        bottomBarXiuTan.setText("0");
        hasAutoPlay = false;
        if (webViewT.getSettings().getBlockNetworkImage() != blockImg) {
            webViewT.getSettings().setBlockNetworkImage(blockImg);
        }
        hasDismissXiuTan = false;
        DetectorManager.getInstance().startDetect();
        hideMagnetIcon();
        if (StringUtil.isNotEmpty(event.getUrl()) && event.getUrl().endsWith(".user.js")) {
            JSUpdaterKt.loadGreasyJS(event.getUrl());
        }
    }


    @Subscribe
    public void onPageFinished(OnPageFinishedEvent event) {
        getProgress_bar().hide();
        Log.d(TAG, "onPageFinished: " + event.getTitle());
        if (!TextUtils.isEmpty(event.getTitle())) {
            bottomTitleView.setText(event.getTitle());
        }
        if (hasBackground()) {
            getWindow().setNavigationBarColor(0xffffffff);
        }
        finishPageNow(event.getUrl(), event.getTitle());
        if (element_bg != null && element_bg.getVisibility() == VISIBLE) {
            webViewT.evaluateJavascript("(function(){window.setDebugState(true)})();", null);
        }
    }

    private void updateBottomBarBackground() {
        updateBottomBarBackground(false);
    }

    private void updateBottomBarBackground(boolean revertFromWebView) {
        boolean immerseBottom = PreferenceMgr.getBoolean(getContext(), "ib1", false);
        if (immerseBottom) {
            if (webViewBg != null && webViewBg.getVisibility() != VISIBLE) {
                //主页
                bottomBar.setBackground(null);
                bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackground(null);
            } else {
                //显示WebView，不改变bottomBar，WebView页面加载完成时会自动识别修改
                bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackground(null);
                if (revertFromWebView && webViewT != null) {
                    updateBottomBarBackground(webViewT.getNavigationBarColor());
                }
            }
        } else {
            bottomBar.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.shape_top_border));
            bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackgroundColor(getResources().getColor(R.color.gray_rice));
            getWindow().setNavigationBarColor(0xffffffff);
        }
    }

    private void updateBottomBarBackground(int color) {
        if (color == 0xff666666) {
            color = 0xff343C3E;
        }
        if (color == 0xffffffff) {
            color = 0xFFF7F7F7;
        }
        bottomBar.setBackgroundColor(color);
        getWindow().setNavigationBarColor(color);
    }

    private void finishPageNow(String url, String title) {
        refreshBottomBarAfterFinishPage();
        if (!SettingConfig.noWebHistory) {
            HeavyTaskUtil.saveHistory(getActivity(), CollectionTypeConstant.WEB_VIEW, "", url, title);
        }
        webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "magnet.js"), null);
    }

    /**
     * 沉浸状态栏和底部地址栏
     */
    private void refreshBottomBarAfterFinishPage() {
        if (webViewT != null) {
            ThreadTool.INSTANCE.async(() -> {
                try {
                    //避免下次用时webViewT引用发生变化导致数据不对
                    HorizontalWebView webView = webViewT;
                    if (webView == null) {
                        return;
                    }
                    boolean immerseBottom = PreferenceMgr.getBoolean(getContext(), "ib1", false);
                    int[] colors = webView.loadAppBarColors(immerseBottom);
                    ThreadTool.INSTANCE.runOnUI(() -> {
                        if (webViewT == null) {
                            return;
                        }
                        webView.setStatusBarColor(colors[0]);
                        webView.setNavigationBarColor(colors[1]);
                        if (webView.isUsed()) {
                            StatusBarCompatUtil.setStatusBarColor(getActivity(), colors[0]);
                            if (immerseBottom) {
                                updateBottomBarBackground(colors[1]);
                            }
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void hideMagnetIcon() {
        if (magnet_bg != null) {
            magnet_bg.setVisibility(GONE);
        }
    }

    private void showMagnetIcon(String data) {
        if (magnet_bg == null) {
            findView(R.id.magnet_stub).setVisibility(VISIBLE);
            magnet_bg = findView(R.id.magnet_bg);
            magnet_text = magnet_bg.findViewById(R.id.magnet_text);
            magnet_bg.setOnClickListener(v -> showMagnetList());
        }
        magnet_bg.setTag(data);
        magnet_bg.setVisibility(VISIBLE);
        List<MagnetData> list = JSON.parseArray(data, MagnetData.class);
        magnet_text.setText((list.size() + ""));
        if (magnetPopup != null) {
            List<DetectedMediaResult> results = toDetectedMediaResults(list);
            magnetPopup.updateData(results);
        }
    }

    private List<DetectedMediaResult> toDetectedMediaResults(List<MagnetData> list) {
        List<DetectedMediaResult> results = new ArrayList<>();
        for (MagnetData magnetData : list) {
            DetectedMediaResult mediaResult = new DetectedMediaResult();
            mediaResult.setMediaType(new Media("磁力"));
            mediaResult.setTitle(magnetData.getName().trim() + "\n" + magnetData.getUrl());
            mediaResult.setUrl(magnetData.getUrl());
            results.add(mediaResult);
        }
        return results;
    }

    private void showMagnetList() {
        if (magnet_bg.getTag() == null) {
            return;
        }
        if (webViewT != null) {
            webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "magnet.js"), null);
        }
        String data = (String) magnet_bg.getTag();
        List<MagnetData> list = JSON.parseArray(data, MagnetData.class);
        if (CollectionUtil.isNotEmpty(list)) {
            List<DetectedMediaResult> results = toDetectedMediaResults(list);
            magnetPopup = new XiuTanResultPopup(getContext())
                    .withTitle("嗅探到的磁力地址")
                    .withIcon(v -> new XPopup.Builder(getContext())
                            .asConfirm("使用说明", "点击列表项即可调用磁力下载软件来下载，如果你没有安装磁力下载软件可以点击下面的按钮去安装一个。" +
                                    "推荐使用全能下载器、迅雷", "取消", "下载器", () -> {
                                addWindow(getString(R.string.downloader_recommand));
                                if (magnetPopup != null) {
                                    magnetPopup.dismiss();
                                }
                            }, () -> {
                            }, false)
                            .show()
                    ).with(results, (url, type) -> {
                        if ("悬浮播放".equals(type)) {
                            ToastMgr.shortCenter(getContext(), "当前资源不支持在线播放");
                        } else if ("play".equals(type)) {
                            startDownloadMagnet(url);
                        } else if ("复制链接".equals(type)) {
                            ClipboardUtil.copyToClipboard(getContext(), url);
                        } else {
                            ShareUtil.findChooserToDeal(getContext(), url);
                        }
                    });
            new XPopup.Builder(getContext())
                    .moveUpToKeyboard(false)
                    .asCustom(magnetPopup)
                    .show();
        }
    }

    private void dealThunderLink(String url) {
        String type = url.startsWith("magnet") ? "磁力" : (url.startsWith("ed2k") ? "电驴" : "FTP");
        new XPopup.Builder(getContext())
                .asConfirm("温馨提示", "请选择" + type + "链接处理方式", "下载", "云播", () -> {
                    if (ThunderManager.INSTANCE.isFTPOrEd2k(url)) {
                        startPlayFTP(url);
                    } else {
                        startDownloadMagnet(url);
                    }
                }, () -> ShareUtil.findChooserToDeal(getContext(), url), false)
                .show();
    }

    private void startDownloadMagnet(String url) {
        ThunderManager.INSTANCE.startDownloadMagnet(getContext(), url, (u, name, list) -> {
            if (list.size() < 2) {
                startPlayVideo(u, name, false);
            } else {
                List<VideoChapter> chapters = new ArrayList<>();
                for (TorrentFileInfo torrentFileInfo : list) {
                    VideoChapter chapter = new VideoChapter();
                    chapter.setTorrentFileInfo(torrentFileInfo);
                    chapter.setMemoryTitle(torrentFileInfo.mFileName);
                    chapter.setTitle(torrentFileInfo.mFileName);
                    chapter.setUrl(StringUtils.equals(name, torrentFileInfo.mFileName) ? u : url);
                    chapter.setUse(StringUtils.equals(name, torrentFileInfo.mFileName));
                    chapter.setOriginalUrl(chapter.getUrl());
                    chapters.add(chapter);
                }
                PlayerChooser.startPlayer(getContext(), chapters, url, "", extraDataBundle);
            }
        });
    }

    private void startPlayFTP(String url) {
        ThunderManager.INSTANCE.startParseFTPOrEd2k(getContext(), url, (u, name, list) -> {
            startPlayVideo(u, name, false);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMagnetFind(FindMagnetsEvent event) {
        if (StringUtil.isEmpty(event.getData()) && !"[]".equals(event.getData())) {
            return;
        }
        showMagnetIcon(event.getData());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlLoad(OnLoadUrlEvent event) {
        if (StringUtil.isNotEmpty(event.getUrl()) && event.getUrl().startsWith("javascript")) {
            return;
        }
        getProgress_bar().show();
        if (floatVideoController != null) {
            floatVideoController.loadUrl(event.getUrl());
        }
    }

    @Subscribe
    public void shouldOverrideUrlLoading2(OnOverrideUrlLoadingForOther event) {
        String url = event.getUrl();
        if (StringUtil.isEmpty(url) || url.startsWith("about:blank")) {
            return;
        }
        if (overrideUrlLoading2(event)) {
            return;
        }
        String dom = StringUtil.getDom(webViewT.getUrl());
        int openAppMode = DomainConfigKt.getOpenAppMode(dom);
        if (SettingConfig.openAppNotify || openAppMode != 0) {
            if (openAppMode == 2) {
                ShareUtil.findChooserToDeal(getContext(), url);
                return;
            }
            appOpenTemp = false;
            Integer disallowApp = WebViewHelper.disallowAppSet.get(dom);
            if ((disallowApp != null && disallowApp > 1) || DomainConfigKt.isDisableOpenApp(dom)) {
                if (openAppMode == 0) {
                    Timber.d("shouldOverrideUrlLoading2, disallowSet.contains: %s", url);
                    return;
                }
            }
            Snackbar.make(getSnackBarBg(), "允许网页打开外部应用？", Snackbar.LENGTH_LONG)
                    .setAction("允许", v -> appOpenTemp = true).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            Timber.d("shouldOverrideUrlLoading2, onDismissed: %s, %s", appOpenTemp, url);
                            if (appOpenTemp) {
                                ShareUtil.findChooserToDeal(getContext(), url);
                            } else {
                                WebViewHelper.disallowAppSet.put(dom, disallowApp == null ? 1 : 2);
                            }
                            super.onDismissed(transientBottomBar, event);
                        }
                    }).show();
        }
    }

    public boolean overrideUrlLoading2(OnOverrideUrlLoadingForOther event) {
        String url = event.getUrl();
        if (url.startsWith("hiker://")) {
            String route = url.replace("hiker://", "");
            if (route.startsWith("mini-program@")) {
                String name = route.replace("mini-program@", "");
                if ("奇妙工具箱".equals(name)) {
                    MiniProgramOfficer.INSTANCE.showTools(getActivity());
                    return true;
                }
                RuleDTO ruleDTO = MiniProgramRouter.INSTANCE.findRuleDTO(name);
                if (ruleDTO == null) {
                    ToastMgr.shortBottomCenter(getContext(), "找不到" + name + "小程序");
                } else {
                    MiniProgramRouter.INSTANCE.startRuleHomePage(getContext(), ruleDTO);
                }
                return true;
            } else if (route.startsWith("folder@")) {
                String groupPath = route.replace("folder@", "");
                BookmarkActivity.showBookmarks(getActivity(), groupPath, u -> {
                    if (!isFinishing() && webViewT != null) {
                        webViewT.loadUrl(u);
                    }
                });
                return true;
            }
            switch (route) {
                case "search":
                    onClick(bottomTitleView);
                    break;
                case "download":
                    Intent intent = new Intent(getContext(), DownloadRecordsActivity.class);
                    intent.putExtra("downloaded", true);
                    startActivity(intent);
                    break;
                case "home":
                    finish();
                    break;
                case "bookmark":
                    startActivityForResult(new Intent(getContext(), BookmarkActivity.class), 101);
                    break;
                case "collection":
                    startActivityForResult(new Intent(getContext(), CollectionListActivity.class), 101);
                    break;
                case "history":
                    startActivityForResult(new Intent(getContext(), HistoryListActivity.class), 101);
                    break;
                case "setting":
                    showSetting();
                    break;
                case "adUrl":
                    startActivity(new Intent(getContext(), AdUrlListActivity.class));
                    break;
                case "adRule":
                    startActivity(new Intent(getContext(), AdListActivity.class));
                    break;
                case "mini-program":
                    if (CollectionUtil.isNotEmpty(MiniProgramRouter.INSTANCE.getData())) {
                        MiniProgramOfficer.INSTANCE.showMiniPrograms(getActivity());
                    } else {
                        MiniProgramOfficer.INSTANCE.show(getActivity());
                    }
                    break;
            }
            return true;
        } else if (url.equals("folder://")) {
            startActivityForResult(new Intent(getContext(), BookmarkActivity.class), 101);
            return true;
        } else if (url.equals("history://")) {
            startActivityForResult(new Intent(getContext(), HistoryListActivity.class), 101);
            return true;
        } else if (url.equals("download://")) {
            Intent intent = new Intent(getContext(), DownloadRecordsActivity.class);
            intent.putExtra("downloaded", true);
            startActivity(intent);
            return true;
        } else if (url.startsWith("magnet:?") || ThunderManager.INSTANCE.isFTPOrEd2k(url)) {
            dealThunderLink(url);
            return true;
        }
        return false;
    }

    private void addBookmark(String title, String url) {
        BookmarkActivity.addBookmark(getActivity(), title.replace(" ", "-"), url);
    }

    private void showSetting() {
        saveAdBlockPlusCount();
        List<String> operations;
        if (webViewT == null || StringUtil.isEmpty(webViewT.getUrl())) {
            operations = new ArrayList<>(Arrays.asList("奇妙工具箱", "基本设置", "外观定制", "下载相关", "广告拦截", "播放器",
                    "网页小程序", "搜索引擎管理", "自定义UA", "数据自动备份", "更多设置", "关于"));
        } else {
            operations = new ArrayList<>(Arrays.asList("奇妙工具箱", "基本设置", "外观定制", "下载相关", "广告拦截", "播放器",
                    "网页小程序", "搜索引擎管理", "网站专属配置", "数据自动备份", "更多设置", "关于"));
        }
        MoreSettingMenuPopup menuPopup = new MoreSettingMenuPopup(this, "更多设置", operations, text -> {
            switch (text) {
                case "奇妙工具箱":
                    MiniProgramOfficer.INSTANCE.showTools(getActivity());
                    break;
                case "基本设置":
                    NormalSettingOfficer.INSTANCE.show(getActivity());
                    break;
                case "网页小程序":
                    MiniProgramOfficer.INSTANCE.show(getActivity());
                    break;
                case "广告拦截":
                    AdblockOfficer.INSTANCE.show(getActivity());
                    break;
                case "关于":
                    AboutOfficer.INSTANCE.show(getActivity());
                    break;
                case "下载相关":
                    DownloadOfficer.INSTANCE.show(getActivity());
                    break;
                case "播放器":
                    XiuTanOfficer.INSTANCE.show(getActivity());
                    break;
                case "外观定制":
                    showUI();
                    break;
                case "搜索引擎管理":
                    startActivity(new Intent(getContext(), SearchEngineMagActivity.class));
                    break;
                case "网站专属配置":
                    new XPopup.Builder(getContext())
                            .asCustom(new DomainConfigPopup(getActivity(), StringUtil.getDom(webViewT.getUrl())))
                            .show();
                    break;
                case "自定义UA":
                    startActivity(new Intent(getContext(), UAListActivity.class));
                    break;
                case "更多设置":
                    MoreSettingOfficer.INSTANCE.show(getActivity());
                    break;
                case "数据自动备份":
                    new XPopup.Builder(getContext())
                            .asCenterList(null, new String[]{"备份到本地", "从本地恢复", "远程WebDav"}, (position, t) -> {
                                switch (t) {
                                    case "备份到本地":
                                        BackupUtil.backupDBAndJs(getContext(), false);
                                        break;
                                    case "从本地恢复":
                                        new XPopup.Builder(getContext())
                                                .asCenterList(null, new String[]{"选择备份文件来恢复", "从默认备份位置恢复"}, (i, s) -> {
                                                    if ("选择备份文件来恢复".equals(s)) {
                                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                                        intent.setType("application/zip");
                                                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                                                        startActivityForResult(intent, 2);
                                                    } else {
                                                        BackupUtil.recoveryDBAndJs(getContext());
                                                    }
                                                }).show();
                                        break;
                                    case "远程WebDav":
                                        MoreSettingActivity.webDavSettingShow(this);
                                        break;
                                }
                            }).show();
                    break;
            }
        }).popupHeight(0f);
        settingPopupView = new XPopup.Builder(getContext())
                .asCustom(menuPopup)
                .show();
    }

    private void showSubMenuPopup() {
        new XPopup.Builder(getContext())
                .asCustom(new BrowserSubMenuPopup(getActivity(), tt -> {
                    switch (tt) {
                        case "页内查找":
                            if (webViewBg.getVisibility() != VISIBLE) {
                                ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                                break;
                            }
                            showSearchView(true);
                            break;
                        case "翻译":
                            if (webViewBg.getVisibility() != VISIBLE) {
                                ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                                break;
                            }
                            webViewT.evaluateJavascript(MoreSettingOfficerKt.getTranslateJS(getContext()), null);
                            break;
                        case "更多功能":
                            MoreSettingOfficer.INSTANCE.show(getActivity());
                            break;
                        case "护眼模式":
                            CustomColorPopup popup = new CustomColorPopup(getContext());
                            popup.setColorSelect(color -> {
                                color = color.toUpperCase();
                                if (color.length() == 9) {
                                    color = color.replace("#FF", "#");
                                }
                                if ("#FFFFFF".equals(color)) {
                                    PreferenceMgr.remove(getContext(), "eye");
                                    for (HorizontalWebView webView : MultiWindowManager.instance(this).getWebViewList()) {
                                        WebViewHelper.clearThemeCss(webView);
                                    }
                                    try {
                                        //避免下次用时webViewT引用发生变化导致数据不对
                                        HorizontalWebView webView = webViewT;
                                        if (webView == null) {
                                            return;
                                        }
                                        if (StringUtil.isNotEmpty(webViewT.getUrl())) {
                                            webViewBg.setBackgroundColor(Color.WHITE);
                                        }
                                        if (webView.isUsed()) {
                                            webView.reload();
                                        }
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    PreferenceMgr.put(getContext(), "eye", color);
                                    int color1 = Color.parseColor(color);
                                    for (HorizontalWebView webView : MultiWindowManager.instance(this).getWebViewList()) {
                                        WebViewHelper.clearThemeCss(webView);
                                        WebViewHelper.injectThemeCss(webView);
                                        webView.setStatusBarColor(color1);
                                        webView.setNavigationBarColor(color1);
                                        if (webView.isUsed()) {
                                            StatusBarCompatUtil.setStatusBarColor(getActivity(), color1);
                                            updateBottomBarBackground(color1);
                                        }
                                    }
                                    if (StringUtil.isNotEmpty(webViewT.getUrl())) {
                                        webViewBg.setBackgroundColor(color1);
                                    }
//                                    ToastMgr.shortBottomCenter(getContext(), "建议同时在UI界面自定义中开启强制新窗口打开");
                                }
                            });
                            new XPopup.Builder(getContext())
                                    .asCustom(popup)
                                    .show();
                            break;
                        case "开发调试":
                            if (webViewBg.getVisibility() != VISIBLE) {
                                ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                                break;
                            }
                            if (webViewT.isUseDevMode()) {
                                webViewT.setUseDevMode(false);
                                webViewT.reload();
                                ToastMgr.shortBottomCenter(getContext(), "已关闭开发调试模式");
                                break;
                            }
                            webViewT.setUseDevMode(true);
                            ToastMgr.shortBottomCenter(getContext(), "已开启开发调试模式");
                            webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "vConsole.js"), value -> {
                                webViewT.evaluateJavascript("eruda.show()", null);
                            });
                            break;
                        case "保存网页":
                            if (webViewBg.getVisibility() != VISIBLE) {
                                ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                                break;
                            }
                            String dir = UriUtils.getRootDir(getContext()) + File.separator + "offline_pages";
                            File dirFile = new File(dir);
                            if (!dirFile.exists()) {
                                dirFile.mkdirs();
                            }
                            String t = FileUtil.fileNameFilter(webViewT.getTitle());
                            String path = dir + File.separator + t + "-" + StringUtil.md5(webViewT.getUrl()) + ".mht";
                            webViewT.saveWebArchive(path);
                            Bookmark bookmark = LitePal.where("title = ? and dir = 1", "离线页面").findFirst(Bookmark.class);
                            if (bookmark == null) {
                                bookmark = new Bookmark();
                                bookmark.setDir(true);
                                bookmark.setTitle("离线页面");
                                bookmark.save();
                            }

                            String s = t + "￥file://" + path + "￥离线页面";
                            Intent intent = new Intent(getContext(), BookmarkActivity.class);
                            intent.putExtra("webs", s);
                            startActivity(intent);
                            break;
                        case "离线页面":
                            Intent intent2 = new Intent(getContext(), BookmarkActivity.class);
                            intent2.putExtra("offline_pages", true);
                            startActivity(intent2);
                            break;
                        case "查看源码":
                            if (webViewBg.getVisibility() != VISIBLE) {
                                ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                                break;
                            }
                            if (StringUtil.isEmpty(webViewT.getUrl())) {
                                ToastMgr.shortBottomCenter(getContext(), "页面链接为空");
                                break;
                            }
                            String url = webViewT.getUrl().startsWith("view-source:") ? webViewT.getUrl().replace("view-source:", "") : webViewT.getUrl();
                            new XPopup.Builder(getContext())
                                    .asBottomList("请选择查看方式", new String[]{"网页形式（流畅性能）", "原生界面（格式规整）"}, (position, text) -> {
                                        switch (text) {
                                            case "网页形式（流畅性能）":
                                                addWindow("view-source:" + url, true, webViewT);
                                                break;
                                            case "原生界面（格式规整）":
                                                showCode(url, webViewT.getUaNonNull());
                                                break;
                                        }
                                    }).show();
                            break;
                    }
                })).show();
    }

    private void toggleFullTheme() {
        boolean fullTheme = PreferenceMgr.getBoolean(getContext(), KEY_FULL_THEME, false);
        fullTheme = !fullTheme;
        PreferenceMgr.put(getContext(), KEY_FULL_THEME, fullTheme);
        updateFullTheme(fullTheme);
    }

    private void updateFullTheme(boolean fullTheme) {
        IconFloatButton view_game_close = findView(R.id.view_game_close);
        if (fullTheme) {
            view_game_close.show();
        } else {
            view_game_close.hide();
        }
        int dp50 = DisplayUtil.dpToPx(getContext(), 50);
        if (fullTheme) {
            bottomBar.setVisibility(GONE);
            bottomBar.animate().alpha(0f).setDuration(300).start();
            updateBottomMargin(webViewBg, dp50);
            updateBottomMargin(findView(R.id.search_bg), dp50);
            updateBottomMargin(findView(R.id.shortcut_container), dp50);
            updateBottomMargin(findView(R.id.snack_bar_bg), dp50);
            setStatusBarVisibilityFullTheme(false);
            view_game_close.setOnClickListener(v -> new XPopup.Builder(getContext())
                    .atView(v)
                    .asAttachList(new String[]{"菜单设置", "窗口管理", "回到主页", "退出全屏"}, null,
                            (position, text) -> {
                                switch (text) {
                                    case "菜单设置":
                                        onClick(findView(R.id.bottom_bar_menu));
                                        break;
                                    case "窗口管理":
                                        onClick(findView(R.id.bottom_bar_muti));
                                        break;
                                    case "回到主页":
                                        backToHomeHtml();
                                        break;
                                    case "退出全屏":
                                        toggleFullTheme();
                                        break;
                                }
                            })
                    .show());
        } else {
            bottomBar.setAlpha(0f);
            bottomBar.setVisibility(VISIBLE);
            bottomBar.animate().alpha(1f).setDuration(300).start();
            updateBottomMargin(webViewBg, -dp50);
            updateBottomMargin(findView(R.id.search_bg), -dp50);
            updateBottomMargin(findView(R.id.shortcut_container), -dp50);
            updateBottomMargin(findView(R.id.snack_bar_bg), -dp50);
            setStatusBarVisibilityFullTheme(true);
            if (webViewT != null && StringUtil.isNotEmpty(webViewT.getUrl())) {
                StatusBarCompatUtil.setStatusBarColor(getActivity(), webViewT.getStatusBarColor());
                updateBottomBarBackground(true);
            } else {
                StatusBarCompatUtil.setStatusBarColor(WebViewActivity.this, getResources().getColor(R.color.white));
                if (hasBackground()) {
                    AndroidBarUtils.setTranslucentStatusBar(this, true);
                }
            }
        }
    }

    private void updateBottomMargin(View view, int gap) {
        updateBottomMargin(view, gap, true);
    }

    private void updateBottomMargin(View view, int gap, boolean incremental) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) layoutParams;
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, incremental ? lp.bottomMargin - gap : gap);
            view.setLayoutParams(layoutParams);
        }
    }

    private void showUI() {
        List<String> operations = new ArrayList<>(Arrays.asList("主页背景设置", "主页图标颜色", "主页壁纸管理", "主页随机壁纸", "底部导航定制", "底部地址栏沉浸", "标签栏样式设置",
                "底部上滑手势", "手势前进后退", "网页字体大小", "清除主页背景", "恢复主页配置"));
        SettingMenuPopup menuPopup = new SettingMenuPopup(this, "UI界面自定义", operations, text -> {
            switch (text) {
                case "标签栏样式设置":
                    int tabStyle = PreferenceMgr.getInt(getContext(), "tabStyle", 0);
                    new XPopup.Builder(getContext())
                            .asCenterList("标签栏样式设置", new String[]{"图文样式", "文字样式"}, null, tabStyle, (position, t) -> {
                                PreferenceMgr.put(getContext(), "tabStyle", position);
                                ToastMgr.shortCenter(getContext(), "已设置为" + t);
                            }).show();
                    break;
                case "底部上滑手势":
                    boolean bottomHomeG = PreferenceMgr.getBoolean(getContext(), "bottomHomeG", true);
                    new XPopup.Builder(getContext())
                            .asCenterList("底部上滑回主页手势", new String[]{"开启", "关闭"}, null, bottomHomeG ? 0 : 1, (position, t) -> {
                                PreferenceMgr.put(getContext(), "bottomHomeG", position == 0);
                                ToastMgr.shortCenter(getContext(), "已" + t + "底部上滑回主页手势");
                                if (position == 0) {
                                    //开启
                                    initBottomBarListener();
                                } else {
                                    bottomBar.setOnTouchEventListener(null);
                                }
                            }).show();
                    break;
                case "底部地址栏沉浸":
                    boolean immerseBottom = PreferenceMgr.getBoolean(getContext(), "ib1", false);
                    new XPopup.Builder(getContext())
                            .asCenterList("底部导航地址栏沉浸", new String[]{"关闭", "开启"}, null, immerseBottom ? 1 : 0, (position, t) -> {
                                if (position == 1) {
                                    PreferenceMgr.put(getContext(), "ib1", true);
                                    if (webViewT != null && StringUtil.isNotEmpty(webViewT.getUrl())) {
                                        refreshBottomBarAfterFinishPage();
                                    }
                                } else {
                                    PreferenceMgr.remove(getContext(), "ib1");
                                    updateBottomBarBackground();
                                }
                                ToastMgr.shortCenter(getContext(), "已" + t + "地址栏沉浸");
                            }).show();
                    break;
                case "手势前进后退":
                    boolean scrollForwardAndBack = PreferenceMgr.getBoolean(getContext(), "scrollForwardAndBack", true);
                    new XPopup.Builder(getContext())
                            .asBottomList("滑动前进后退设置，当前：" + (scrollForwardAndBack ? "开启" : "关闭"), new String[]{"开启", "关闭"},
                                    ((p, t) -> {
                                        if (webViewBg.getOnInterceptTouchEventListener() == null) {
                                            initWebViewBgListener();
                                        }
                                        PreferenceMgr.put(getContext(), "scrollForwardAndBack", "开启".equals(t));
                                        ToastMgr.shortBottomCenter(getContext(), "滑动前进后退功能已设置为" + t);
                                    })).show();
                    break;
                case "主页图标颜色":
                    boolean home_logo_dark = PreferenceMgr.getBoolean(getContext(), "home_logo_dark", false);
                    new XPopup.Builder(getContext())
                            .asBottomList("主页图标颜色", new String[]{"深色", "浅色"}, null, home_logo_dark ? 0 : 1,
                                    ((p, t) -> {
                                        PreferenceMgr.put(getContext(), "home_logo_dark", p == 0);
                                        ToastMgr.shortBottomCenter(getContext(), "已设置为" + t);
                                        if (home_logo_dark != PreferenceMgr.getBoolean(getContext(), "home_logo_dark", false)) {
                                            refreshLogoDarkMode();
                                        }
                                    })).show();
                    break;
                case "底部导航定制":
                    int bottomBar = PreferenceMgr.getInt(getContext(), "bottomBar", 0);
                    new XPopup.Builder(getContext())
                            .asCenterList(text, new String[]{"样式一（主页|刷新）", "样式二（主页|前进）", "样式三（后退|前进）", "样式四（窗口|前进）"},
                                    null, bottomBar, (position, text1) -> {
                                        if (position != bottomBar) {
                                            PreferenceMgr.put(getContext(), "bottomBar", position);
                                            refreshBottomBar(position);
                                        }
                                    }).show();
                    break;
                case "恢复主页配置":
                    getDefaultShortcuts();
                    BigTextDO.updateShortcuts(getContext(), Shortcut.toStr(shortcuts));
                    initShortcutView();
                    settingPopupView.dismiss();
                    ToastMgr.shortBottomCenter(getContext(), "已恢复主页配置");
                    break;
                case "网页字体大小":
                    startActivity(new Intent(getContext(), TextSizeActivity.class));
                    break;
                case "主页背景设置":
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 1);
                    break;
                case "主页壁纸管理":
                    managePicture();
                    break;
                case "主页随机壁纸":
                    useRandomPicture();
                    break;
                case "清除主页背景":
                    clearBackground();
                    break;
            }
        }).dismissWhenClick(true);
        new XPopup.Builder(getContext())
                .moveUpToKeyboard(false)
                .asCustom(menuPopup)
                .show();
    }

    private void refreshLogoDarkMode() {
        boolean home_logo_dark = PreferenceMgr.getBoolean(getContext(), "home_logo_dark", false);
        View shortcut_search = findViewById(R.id.shortcut_search);
        shortcut_search.setBackground(getResources().getDrawable(!home_logo_dark ? R.drawable.check_bg_trans_border_white : R.drawable.check_bg_trans));
        ImageView shortcut_search_scan = findViewById(R.id.shortcut_search_scan);
        shortcut_search_scan.setImageDrawable(getResources().getDrawable(!home_logo_dark ? R.drawable.scan_light_white : R.drawable.scan_light));
        boolean hasBg = hasBackground();
        boolean hasBgAll = hasBg && !home_logo_dark;
        for (Shortcut shortcut : shortcuts) {
            if (ShortcutTypeEnum.DEFAULT == ShortcutTypeEnum.Companion.getByCode(shortcut.getType())) {
                shortcut.setHasBackground(hasBgAll);
            } else {
                shortcut.setHasBackground(hasBg);
            }
        }
        if (hasBg) {
            WindowInsetsControllerCompat wic = getWindowInsetsController();
            if (wic != null) {
                // true表示Light Mode，状态栏字体呈黑色，反之呈白色
                wic.setAppearanceLightStatusBars(home_logo_dark);
            }
        }
        shortcutAdapter.notifyDataSetChanged();
    }

    private void useRandomPicture() {
        File dir = new File(UriUtils.getRootDir(getContext()) + File.separator + "images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles();
        if (files == null || files.length < 1) {
            ToastMgr.shortBottomCenter(getContext(), "还没有设置过壁纸，先选择主页背景设置功能设置试试吧！");
            return;
        }
        new XPopup.Builder(getContext())
                .asConfirm("温馨提示", "当前本地壁纸库共有" + files.length + "张壁纸，确定开启随机壁纸模式吗？", () -> {
                    PreferenceMgr.put(getContext(), "home_bg", "random");
                    ToastMgr.shortBottomCenter(getContext(), "设置成功，重启试试效果吧！");
                }).show();
    }

    private void managePicture() {
        File dir1 = new File(UriUtils.getRootDir(getContext()) + File.separator + "images");
        if (!dir1.exists()) {
            dir1.mkdirs();
        }
        File[] files1 = dir1.listFiles();
        if (files1 == null || files1.length < 1) {
            ToastMgr.shortBottomCenter(getContext(), "还没有设置过壁纸，先选择主页背景设置功能设置试试吧！");
            return;
        }
        List<String> paths = new ArrayList<>();
        for (File file : files1) {
            paths.add(file.getAbsolutePath());
        }
        new XPopup.Builder(getContext())
                .hasStatusBar(false)
                .hasStatusBarShadow(true)
                .asCustom(new ImagesViewerPopup(WebViewActivity.this, paths, this::setHomeBg))
                .show();
    }

    private void checkClipboard() {
        if (webViewBg == null) {
            return;
        }
        ClipboardUtil.getText(getContext(), webViewBg, text -> checkAutoText(text));
    }

    private void checkAutoText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        String shareText = text.trim();
        if (!detectedText.equals(shareText) && AutoImportHelper.couldCloudImport(getContext(), shareText)) {
            detectedText = shareText;
            return;
        }
//            Log.d(TAG, "checkClipboard: " + shareText.substring(0, 4));
        if (shareText.length() > 6 && "海阔视界".equals(shareText.substring(1, 5))) {
            shareText = shareText.substring(1);
        } else if (shareText.length() > 7 && "嗅觉浏览器".equals(shareText.substring(1, 6))) {
            shareText = shareText.substring(1);
        }
        if (!TextUtils.isEmpty(shareText) && shareText.startsWith("http") && !shareText.equals(detectedUrl) && !shareText.equals(AutoImportHelper.getShareRule())) {
            detectedUrl = shareText;
            detectedText = shareText;
            AutoImportHelper.checkText(WebViewActivity.this, shareText);
        }
        if (!TextUtils.isEmpty(shareText) && (shareText.startsWith("海阔视界") || shareText.startsWith("方圆") || shareText.startsWith("嗅觉浏览器")) && !shareText.equals(AutoImportHelper.getShareRule())) {
//                Log.d(TAG, "checkClipboard: true");
            if (detectedText.equals(shareText)) {
                return;
            } else {
                detectedUrl = shareText;
                detectedText = shareText;
            }
            try {
                AutoImportHelper.checkAutoText(getContext(), shareText);
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                int nightModeFlags = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
                for (HorizontalWebView webView : MultiWindowManager.instance(this).getWebViewList()) {
                    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                        webView.getSettings().setForceDark(WebSettings.FORCE_DARK_ON);
                    } else {
                        webView.getSettings().setForceDark(WebSettings.FORCE_DARK_OFF);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!"LANDSCAPE".equals(slogan.getTag())) {
                slogan.setTag("LANDSCAPE");
                slogan.postDelayed(() -> refreshHomeMargin(findView(R.id.shortcut_search), findView(R.id.shortcut_search_scan)), 100);
            }
        } else {
            if (!"PORTRAIT".equals(slogan.getTag())) {
                slogan.setTag("PORTRAIT");
                slogan.postDelayed(() -> refreshHomeMargin(findView(R.id.shortcut_search), findView(R.id.shortcut_search_scan)), 100);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void hi(BackMainEvent event) {
        startActivity(new Intent(getContext(), EmptyActivity.class));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoading(LoadingEvent event) {
        if (event.isShow()) {
            if (isOnPause) {
                return;
            }
            if (globalLoadingView != null && globalLoadingView.isShow()) {
                globalLoadingView.setTitle(event.getText());
                return;
            }
            globalLoadingView = new XPopup.Builder(getContext())
                    .asLoading(event.getText());
            globalLoadingView.show();
        } else if (globalLoadingView != null) {
            globalLoadingView.dismiss();
            globalLoadingView = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJSUpdate(UpdateEvent event) {
        if (jsUpdateEvents == null) {
            jsUpdateEvents = new ArrayList<>();
        }
        if (jsUpdateEvents.isEmpty()) {
            jsUpdateEvents.add(event);
            webViewBg.postDelayed(() -> {
                List<UpdateEvent> events = new ArrayList<>(jsUpdateEvents);
                jsUpdateEvents.clear();
                if (!events.isEmpty()) {
                    try {
                        String t = "““检测到有" + events.size() + "个插件更新””";
                        Activity ctx = ActivityManager.getInstance().getCurrentActivity();
                        CustomBottomPopup popup = new CustomBottomPopup(ctx, t)
                                .addOnClickListener(v -> {
                                    Activity c = ActivityManager.getInstance().getCurrentActivity();
                                    new XPopup.Builder(c)
                                            .asCustom(new JSUpdatePopup(c).withTitle("脚本插件更新").with(events))
                                            .show();
                                });
                        new XPopup.Builder(ctx)
                                .hasShadowBg(false)
                                .asCustom(popup)
                                .show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, event.getUrgent() ? 500 : 5000);
        } else {
            jsUpdateEvents.add(event);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthRequest(AuthBridgeEvent event) {
        if (isFinishing()) {
            return;
        }
        Context context = ActivityManager.getInstance().getCurrentActivity();
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("授权提示")
                .setMessage(event.getRule() + "申请" + event.getTitle() + "，此方法非常规情况下不需要使用，确认要使用请点击授权执行")
                .setCancelable(false)
                .setPositiveButton("授权执行", (dialog, which) -> {
                    dialog.dismiss();
                    JSEngine.getInstance().updateAuth(event.getRule(), event.getMethod(), AuthBridgeEvent.AuthResult.AGREE);
                    event.getLock().countDown();
                })
                .setNegativeButton("拒绝执行", (dialog, which) -> {
                    dialog.dismiss();
                    JSEngine.getInstance().updateAuth(event.getRule(), event.getMethod(), AuthBridgeEvent.AuthResult.DISAGREE);
                    event.getLock().countDown();
                })
                .setNeutralButton("暂不授权", ((dialog, which) -> {
                    dialog.dismiss();
                    event.getLock().countDown();
                }))
                .create();
        DialogUtil.INSTANCE.showAsCard(context, alertDialog);
    }
}

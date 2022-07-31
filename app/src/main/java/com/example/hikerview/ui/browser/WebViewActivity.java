package com.example.hikerview.ui.browser;

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
import android.webkit.WebChromeClient;
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
import com.example.hikerview.event.home.ToastEvent;
import com.example.hikerview.event.video.BackMainEvent;
import com.example.hikerview.event.web.BlobDownloadEvent;
import com.example.hikerview.event.web.BlobDownloadProgressEvent;
import com.example.hikerview.event.web.DownloadStartEvent;
import com.example.hikerview.event.web.FindMagnetsEvent;
import com.example.hikerview.event.web.FloatVideoChangeEvent;
import com.example.hikerview.event.web.OnBookmarkUpdateEvent;
import com.example.hikerview.event.web.OnCreateWindowEvent;
import com.example.hikerview.event.web.OnEvalJsEvent;
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
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.model.Bookmark;
import com.example.hikerview.model.DownloadRecord;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.subscribe.AdUrlSubscribe;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.bookmark.BookmarkActivity;
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
import com.example.hikerview.ui.browser.service.PoetryService;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.browser.util.HomeConfigUtil;
import com.example.hikerview.ui.browser.view.BaseWebViewActivity;
import com.example.hikerview.ui.browser.view.BrowserMenuPopup;
import com.example.hikerview.ui.browser.view.BrowserSubMenuPopup;
import com.example.hikerview.ui.browser.view.ImagesViewerPopup;
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
import com.example.hikerview.ui.js.AdListActivity;
import com.example.hikerview.ui.js.AdUrlListActivity;
import com.example.hikerview.ui.js.JSListActivity;
import com.example.hikerview.ui.miniprogram.MiniProgramRouter;
import com.example.hikerview.ui.miniprogram.data.RuleDTO;
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
import com.example.hikerview.ui.setting.X5DebugActivity;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.setting.office.AboutOfficer;
import com.example.hikerview.ui.setting.office.AdblockOfficer;
import com.example.hikerview.ui.setting.office.DownloadOfficer;
import com.example.hikerview.ui.setting.office.MiniProgramOfficer;
import com.example.hikerview.ui.setting.office.XiuTanOfficer;
import com.example.hikerview.ui.setting.updaterecords.UpdateRecord;
import com.example.hikerview.ui.setting.updaterecords.UpdateRecordsActivity;
import com.example.hikerview.ui.setting.webdav.WebDavBackupUtil;
import com.example.hikerview.ui.thunder.ThunderManager;
import com.example.hikerview.ui.video.EmptyActivity;
import com.example.hikerview.ui.video.FloatVideoController;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.view.DialogBuilder;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.ui.view.MutiWondowAdapter;
import com.example.hikerview.ui.view.MutiWondowPopup;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.ui.view.RelativeListenLayout;
import com.example.hikerview.ui.view.XiuTanResultPopup;
import com.example.hikerview.ui.view.animate.AnimateTogetherUtils;
import com.example.hikerview.ui.view.colorDialog.ColorDialog;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
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
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;
import com.king.app.updater.constant.Constants;
import com.king.app.updater.http.HttpManager;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.lxj.xpopup.util.KeyboardUtils;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.xunlei.downloadlib.parameter.TorrentFileInfo;
import com.yzq.zxinglibrary.android.CaptureActivity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.jingbin.progress.WebProgress;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
    private String adBlockRule;
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
    private LoadingPopupView loadingPopupView;
    private FloatVideoController floatVideoController;
    private String videoPlayingWebUrl;
    private View magnet_bg;
    private TextView magnet_text;
    private XiuTanResultPopup magnetPopup;

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
            });
            //延时执行
            webViewBg.postDelayed(() -> {
                if (isFinishing()) {
                    return;
                }
                //初始化可以后台初始化的配置
                HeavyTaskUtil.executeNewTask(DetectorManager::getInstance);
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
        if (tabs.size() == 1) {
            webViewT.loadUrl(tabs.get(0).getUrl());
            return;
        }
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
        showWebViewByPos(pos + 1);
        removeByPos(0);
        webViewBg.setTag("white");
        webViewBg.setBackgroundColor(Color.WHITE);
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
        bottom_bar_refresh = findView(R.id.bottom_bar_refresh);
        View bottom_bar_home = findView(R.id.bottom_bar_home);
        bottom_bar_muti = findView(R.id.bottom_bar_muti);
        bottomBarMenu = findView(R.id.bottom_bar_menu);
        if (!hasBackground()) {
            bottomBar.setBackground(getResources().getDrawable(R.drawable.shape_top_border));
        } else {
            bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackground(null);
        }
        bottomHomeIcon = findView(R.id.home);
        bottomTitleView.setOnClickListener(this);
        bottomTitleView.setOnLongClickListener(view -> {
            backToHomeHtml();
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
            addWindow(null);
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
        GlideUtil.loadPicDrawable(getContext(), imageView, getHomeBackground(), new RequestOptions());
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
        GlideUtil.loadPicDrawable(getContext(), imageView, getHomeBackground(), new RequestOptions());
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
    }

    private void clearBackground() {
        background = null;
        bottomBar.setBackground(getResources().getDrawable(R.drawable.shape_top_border));
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
        bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackgroundColor(getResources().getColor(R.color.gray_rice));
        PreferenceMgr.put(getContext(), "home_bg", "");
        ToastMgr.shortBottomCenter(getContext(), "主页背景图已清除");
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
            int count1 = LitePal.count(Bookmark.class);
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
                                Intent intent = new Intent(getContext(), CaptureActivity.class);
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
                        if (StringUtil.isEmpty(shortcut1.getName())) {
                            ToastMgr.shortBottomCenter(getContext(), "名称不能为空");
                            return;
                        }
                        if (StringUtil.isNotEmpty(shortcut1.getIcon()) && (shortcut1.getIcon().startsWith("http")
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
                    });
            new XPopup.Builder(getContext())
                    .asCustom(inputPopup)
                    .show();
        });
        shortcutAdapter = new ShortcutAdapter(getContext(), shortcuts);
        shortcutAdapter.setOnItemClickListener(new ShortcutAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                Shortcut shortcut = shortcuts.get(position);
                if (shortcut.isDragging()) {
                    ShortcutInputPopup inputPopup = new ShortcutInputPopup(getContext())
                            .bind(shortcut, shortcut1 -> {
                                if (StringUtil.isEmpty(shortcut1.getName())) {
                                    ToastMgr.shortBottomCenter(getContext(), "名称不能为空");
                                    return;
                                }
                                if (StringUtil.isNotEmpty(shortcut1.getIcon()) && (shortcut1.getIcon().startsWith("http")
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
//                        new XPopup.Builder(getContext())
//                                .asConfirm("诗词", shortcut.getName(), () -> {
//                                })
//                                .show();
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
        if (land) {
            slogan.setVisibility(GONE);
            shortcut_container.setPadding(width / 6, 0, width / 6, 0);
        } else {
            shortcut_container.setPadding(0, 0, 0, 0);
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
                    new XPopup.Builder(getContext())
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
        browserMenuPopup = new BrowserMenuPopup(this, iconTitle -> {
            switch (iconTitle.getTitle()) {
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
                case "历史记录":
                    startActivity(new Intent(getContext(), HistoryListActivity.class));
                    break;
                case "工具箱":
                    showSubMenuPopup();
                    break;
                case "视频嗅探":
                    showMenu(bottomBarMenu);
                    break;
                case "分享链接":
                    if (webViewBg.getVisibility() != VISIBLE) {
                        ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                        break;
                    }
                    ShareUtil.shareText(getContext(), bottomTitleView.getText() + "\n" + webViewT.getUrl());
                    break;
                case "插件管理":
                    startActivity(new Intent(getContext(), JSListActivity.class));
                    break;
                case "无图模式":
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
                case "下载管理":
                    Intent intent2 = new Intent(getContext(), DownloadRecordsActivity.class);
                    intent2.putExtra("downloaded", true);
                    startActivity(intent2);
                    break;
                case "退出":
                case "退出软件":
                    finish();
                    break;
            }
        });
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
        RemotePlayConfig.playerPath = RemotePlayConfig.D_PLAYER_PATH;
        try {
            DownloadManager.instance().loadConfig();
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
            } else {
                String url = getIntent().getStringExtra("url");
                checkIntentUrl(url);
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
            } else if (!url.startsWith("http") && url.endsWith(".apk.1")) {
                Uri uri = Uri.parse(url);
                String fileName = UriUtils.getFileName(uri).replace(".apk.1", ".apk");
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
            } else if (UrlDetector.isVideoOrMusic(url) || url.startsWith("content://")) {
                if (url.startsWith("content://")) {
                    Uri uri = Uri.parse(url);
                    String copyTo = UriUtils.getRootDir(getContext()) + File.separator + "_cache" + File.separator + UriUtils.getFileName(uri);
                    FileUtil.makeSureDirExist(copyTo);
                    showLoading("文件解析中，请稍候");
                    getIntent().putExtra("contentCache", true);
                    UriUtils.getFilePathFromURI(getContext(), uri, copyTo, new UriUtils.LoadListener() {
                        @Override
                        public void success(String s) {
                            if (!isFinishing()) {
                                runOnUiThread(() -> {
                                    hideLoading();
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
                                runOnUiThread(() -> hideLoading());
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

    private void setBottomMutiWindowIcon() {
        bottom_bar_muti.setImageDrawable(getResources().getDrawable(getMutiCountLineIconId()));
    }

    private int getMutiCountLineIconId() {
        int size = MultiWindowManager.instance(this).getWebViewList().size();
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
            adBlockRule = debug_rule_text.getText().toString();
            if (TextUtils.isEmpty(adBlockRule)) {
                ToastMgr.shortBottomCenter(getContext(), "规则为空！");
            } else {
                saveAdBlock(adBlockRule);
            }
        });
        debug_edit_rule_btn.setOnClickListener(v -> {
            adBlockRule = debug_rule_text.getText().toString();
            editBlockRule(adBlockRule);
        });
    }

    private void editBlockRule(String rule) {
        DialogBuilder.createInputConfirm(getContext(), "编辑拦截规则", rule, text -> {
            if (TextUtils.isEmpty(text)) {
                ToastMgr.shortBottomCenter(getContext(), "规则不能为空");
            } else {
                saveAdBlock(text);
            }
        }).show();
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
            if (webViewT.canGoBack()) {
                webViewT.goBack();
            } else {
                backToHomeHtml();
            }
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
        DownloadDialogUtil.showEditDialog(this, t, videoUrl);
    }

    private int getBottomView2DefaultDrawable() {
        if (isForward(bottom_bar_refresh)) {
            return R.drawable.right;
        } else {
            return R.drawable.refresh;
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
                showMutiWindowPop(v);
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
                    webViewBg.setBackgroundColor(Color.WHITE);
                }
                webViewBg.setAlpha(0f);
                webViewBg.setVisibility(VISIBLE);
                webViewBg.animate().alpha(1f).setDuration(300).start();
                View shortcut_container = findView(R.id.shortcut_container);
                shortcut_container.setVisibility(GONE);
                shortcut_container.animate().alpha(0f).setDuration(300).start();
                if (hasBackground()) {
                    bottomBar.setBackground(getResources().getDrawable(R.drawable.shape_top_border));
                    bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackgroundColor(getResources().getColor(R.color.gray_rice));
                    AndroidBarUtils.setTranslucentStatusBar(this, false);
                }
            }
        });
    }

    private void showMutiWindowPop(View view) {
        webViewT.setDetectedMediaResults(DetectorManager.getInstance().getDetectedMediaResults((Media) null));
        int[] location = new int[2];
        webViewBg.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        View shortcut_container = findView(R.id.shortcut_container);
        new XPopup.Builder(getContext())
                .asCustom(new MutiWondowPopup(getContext())
                        .home(shortcut_container)
                        .with(WebViewActivity.this, MultiWindowManager.instance(WebViewActivity.this).getWebViewList(),
                                () -> addWindow(null),
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
                                })).show();
    }

    private void removeByPos(int pos) {
        webViewBg.removeView(webViewT);
        HorizontalWebView webView = MultiWindowManager.instance(WebViewActivity.this).removeWebView(pos);
        showNewWebView(webView);
    }

    private void showWebViewByPos(int pos) {
        webViewBg.removeView(webViewT);
        webViewT = MultiWindowManager.instance(WebViewActivity.this).selectWebView(pos);
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
                bottomBar.setBackground(getResources().getDrawable(R.drawable.shape_top_border));
                bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackgroundColor(getResources().getColor(R.color.gray_rice));
                AndroidBarUtils.setTranslucentStatusBar(getActivity(), false);
            }
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
                bottomBar.setBackground(getResources().getDrawable(R.drawable.shape_top_border));
                bottomBar.findViewById(R.id.bottom_bar_text_bg).setBackgroundColor(getResources().getColor(R.color.gray_rice));
                AndroidBarUtils.setTranslucentStatusBar(getActivity(), false);
            }
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
        webViewBg.removeView(webViewT);
        webViewT = event.getWebView();
        webViewT.clearHistory();
        webViewBg.addView(webViewT);
        DetectorManager.getInstance().startDetect();
        refreshVideoCount();
        showSearchView(false);
        setBottomMutiWindowIcon();
        AnimateTogetherUtils.scaleNow(bottom_bar_muti);
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
        String fileName = HttpManager.getDispositionFileName(event.getContentDisposition());
        if (StringUtil.isEmpty(fileName)) {
            fileName = StringUtil.md5(url);
            if (UrlDetector.isMusic(url) || !UrlDetector.isVideoOrMusic(url)) {
                String ext = ShareUtil.getExtension(event.getMimetype());
                if (StringUtil.isNotEmpty(ext)) {
                    fileName = fileName + "." + ext;
                }
            }
        }
        String finalFileName = fileName;
        if (url.startsWith("blob:")) {
            findBlob(url, finalFileName, event.getMimetype());
            return;
        }
        Timber.d("downloadStart: finalFileName: %s", finalFileName);
        String uu = PlayerChooser.decorateHeader(WebViewHelper.getRequestHeaderMap(webViewT, surl), webViewT.getUrl(), surl);
        if (url.contains(".apk") || DownloadDialogUtil.isApk(fileName, event.getMimetype())) {
            Snackbar.make(getSnackBarBg(), "是否允许网页中的下载请求？", Snackbar.LENGTH_LONG)
                    .setAction("允许", v -> {
                        FileUtil.saveFile(getContext(), () -> DownloadDialogUtil.showEditDialog(getActivity(), finalFileName, uu, event.getMimetype()));
                    }).show();
        } else {
            new XPopup.Builder(getContext())
                    .asConfirm("温馨提示", "是否允许网页中的下载请求？（点击空白处拒绝操作，点击播放可以将链接作为视频地址直接播放）",
                            "播放", "下载", () -> {
                                FileUtil.saveFile(getContext(), () -> {
                                    DownloadDialogUtil.showEditDialog(getActivity(), finalFileName, uu, event.getMimetype());
                                });
                            }, () -> startPlayVideo(surl), false).show();
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
        if (StringUtil.isEmpty(fileName)) {
            fileName = StringUtil.md5(url);
        }
        if (!fileName.contains(".")) {
            String ext = ShareUtil.getExtension(mimeType);
            if (StringUtil.isNotEmpty(ext)) {
                fileName = fileName + "." + ext;
            }
        }
        String finalFileName = fileName;
        if (fileName.contains(".apk") || DownloadDialogUtil.isApk(fileName, mimeType)) {
            Snackbar.make(getSnackBarBg(), "是否允许网页中的下载请求？", Snackbar.LENGTH_LONG)
                    .setAction("允许", v -> {
                        new XPopup.Builder(getContext())
                                .asInputConfirm("温馨提示", "以下为从网页下载请求中提取的文件名", finalFileName, "文件名", (text) -> {
                                    String name = text;
                                    if (StringUtil.isEmpty(text)) {
                                        name = finalFileName;
                                    }
                                    String finalName = name;
                                    FileUtil.saveFile(getContext(), () -> downloadBlob(url, finalName));
                                }, null, R.layout.xpopup_confirm_input).show();
                    }).show();
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
        if (loadingPopupView == null) {
            loadingPopupView = new XPopup.Builder(getContext()).asLoading();
        }
        loadingPopupView.setTitle("下载中..." + event.getProgress());
        if (!loadingPopupView.isShow()) {
            loadingPopupView.show();
        }
    }

    private void showLoading(String text) {
        if (loadingPopupView == null) {
            loadingPopupView = new XPopup.Builder(getContext()).asLoading();
        }
        loadingPopupView.setTitle(text);
        if (!loadingPopupView.isShow()) {
            loadingPopupView.show();
        }
    }

    private void hideLoading() {
        if (loadingPopupView != null) {
            loadingPopupView.dismiss();
        }
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
                    String ext = ShareUtil.getExtension(mimeType);
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
            if (loadingPopupView != null && loadingPopupView.isShow()) {
                loadingPopupView.dismiss();
            }
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
                    return;
                }
                if (webViewBg.getVisibility() == VISIBLE) {
                    backToHomeHtml();
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
        if (!hasDismissXiuTan) {
            hasDismissXiuTan = true;
            if (!hasAutoPlay) {
                if (!DetectorManager.getInstance().inXiuTanDialogBlackList(webViewT.getUrl()) && !isToastShow) {
                    if (floatVideoController != null && !UrlDetector.isMusic(videoEvent.getMediaResult().getUrl())) {
                        videoEvent.getMediaResult().setClicked(true);
                        floatVideoController.show(videoEvent.getMediaResult().getUrl(), nowUrl, webViewT.getTitle(), true);
                    } else {
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
        View global = view1.findViewById(R.id.block_add_global);
        View domain = view1.findViewById(R.id.block_add_domain);
        String finalUrl = url;
        block_add_dom.setOnClickListener(v -> {
            titleE.setText(StringUtil.getDom(finalUrl).split(":")[0]);
            block_add_url.setBackground(getDrawable(R.drawable.button_layer));
            block_add_dom.setBackground(getDrawable(R.drawable.button_layer_red));
        });
        block_add_url.setOnClickListener(v -> {
            titleE.setText(finalUrl);
            block_add_dom.setBackground(getDrawable(R.drawable.button_layer));
            block_add_url.setBackground(getDrawable(R.drawable.button_layer_red));
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
                        ToastMgr.shortBottomCenter(getContext(), "保存成功");
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
            ToastMgr.shortBottomCenter(getContext(), "已保存拦截规则");
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
            File file = new PopImageLoaderNoView(uu).getImageFile(getContext(), url);
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
        ImgUtil.savePic2Gallery(getContext(), url, webViewT.getUrl(), new ImgUtil.OnSaveListener() {
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

    private void showBigPic(String pic) {
        List<DetectedMediaResult> images = DetectorManager.getInstance().getDetectedMediaResults(MediaType.IMAGE);
        List<Object> imageUrls = new ArrayList<>(images.size());
        int pos = -1;
        for (int i = 0; i < images.size(); i++) {
            DetectedMediaResult result1 = images.get(i);
            imageUrls.add(result1.getUrl());
            if (StringUtil.equalsDomUrl(pic, result1.getUrl())) {
                pos = i;
            }
        }
        if (pos != -1) {
            new XPopup.Builder(getContext()).asImageViewer(null, pos, imageUrls, null, new PopImageLoaderNoView(webViewT.getUrl()))
                    .show();
        } else {
            new XPopup.Builder(getContext())
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
                adBlockRule = debug_rule_text.getText().toString();
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
    }


    @Subscribe
    public void onPageFinished(OnPageFinishedEvent event) {
        getProgress_bar().hide();
        Log.d(TAG, "onPageFinished: " + event.getTitle());
        if (!TextUtils.isEmpty(event.getTitle())) {
            bottomTitleView.setText(event.getTitle());
        }
        finishPageNow(event.getUrl(), event.getTitle());
    }

    private void finishPageNow(String url, String title) {
        if (!SettingConfig.noWebHistory) {
            HeavyTaskUtil.saveHistory(getActivity(), CollectionTypeConstant.WEB_VIEW, "", url, title);
        }
        webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "magnet.js"), null);
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvalJs(OnEvalJsEvent event) {
        if (webViewT != null) {
            webViewT.evaluateJavascript(event.getJs(), null);
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
        if (SettingConfig.openAppNotify) {
            appOpenTemp = false;
            String dom = StringUtil.getDom(webViewT.getUrl());
            if (WebViewHelper.disallowAppSet.contains(dom)) {
                Timber.d("shouldOverrideUrlLoading2, disallowSet.contains: %s", url);
                return;
            }
            Snackbar.make(getSnackBarBg(), "允许网页打开外部应用？", Snackbar.LENGTH_LONG)
                    .setAction("允许", v -> appOpenTemp = true).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    Timber.d("shouldOverrideUrlLoading2, onDismissed: %s, %s", appOpenTemp, url);
                    if (appOpenTemp) {
                        ShareUtil.findChooserToDeal(getContext(), url);
                    } else {
                        WebViewHelper.disallowAppSet.add(dom);
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
                RuleDTO ruleDTO = MiniProgramRouter.INSTANCE.findRuleDTO(name);
                if (ruleDTO == null) {
                    ToastMgr.shortBottomCenter(getContext(), "找不到" + name + "小程序");
                } else {
                    MiniProgramRouter.INSTANCE.startRuleHomePage(getContext(), ruleDTO);
                }
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
        List<String> operations = new ArrayList<>(Arrays.asList("下载相关设置", "广告拦截订阅", "视频相关设置",
                "UI界面自定义", "网页小程序", "搜索引擎管理", "自定义UA设置", "数据自动备份", "X5内核调试", "清除内部缓存", "关于与帮助"));
        MoreSettingMenuPopup menuPopup = new MoreSettingMenuPopup(this, "更多设置", operations, text -> {
            switch (text) {
                case "网页小程序":
                    MiniProgramOfficer.INSTANCE.show(getActivity());
                    break;
                case "广告拦截订阅":
                    AdblockOfficer.INSTANCE.show(getActivity());
                    break;
                case "关于与帮助":
                    AboutOfficer.INSTANCE.show(getActivity());
                    break;
                case "下载相关设置":
                    DownloadOfficer.INSTANCE.show(getActivity());
                    break;
                case "X5内核调试":
                    startActivity(new Intent(getContext(), X5DebugActivity.class));
                    break;
                case "视频相关设置":
                    XiuTanOfficer.INSTANCE.show(getActivity());
                    break;
                case "UI界面自定义":
                    showUI();
                    break;
                case "搜索引擎管理":
                    startActivity(new Intent(getContext(), SearchEngineMagActivity.class));
                    break;
                case "自定义UA设置":
                    startActivity(new Intent(getContext(), UAListActivity.class));
                    break;
                case "清除内部缓存":
                    String size = CleanMessageUtil.getTotalCacheSize(getContext());
                    if (CleanMessageUtil.clearAllCache(getContext())) {
                        ToastMgr.shortBottomCenter(getContext(), "已清除" + size + "的缓存");
                    }
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
                .moveUpToKeyboard(false)
                .asCustom(menuPopup)
                .show();
    }

    private void showSubMenuPopup() {
        new XPopup.Builder(getContext())
                .asCustom(new BrowserSubMenuPopup(getActivity(), iconTitle -> {
                    switch (iconTitle.getTitle()) {
                        case "页内查找":
                            if (webViewBg.getVisibility() != VISIBLE) {
                                ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                                break;
                            }
                            showSearchView(true);
                            break;
                        case "网页翻译":
                            if (webViewBg.getVisibility() != VISIBLE) {
                                ToastMgr.shortBottomCenter(getContext(), "当前页面不支持此功能");
                                break;
                            }
                            if (webViewT.isUseTranslate()) {
                                webViewT.setUseTranslate(false);
                                webViewT.reload();
                                ToastMgr.shortBottomCenter(getContext(), "已关闭网页翻译模式");
                                break;
                            }
                            webViewT.setUseTranslate(true);
                            webViewT.evaluateJavascript(JSManager.instance(getContext()).getTranslateJs(), null);
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
                            webViewT.evaluateJavascript(FilesInAppUtil.getAssetsString(getContext(), "vConsole.js"), null);
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
                        case "全屏模式":
                            toggleFullTheme();
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
        View view_game_close = findView(R.id.view_game_close);
        view_game_close.setVisibility(fullTheme ? VISIBLE : GONE);
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
        List<String> operations = new ArrayList<>(Arrays.asList("主页背景设置", "主页图标颜色", "主页壁纸管理", "主页随机壁纸", "底部导航定制", "手势前进后退", "网页打开应用", "网页获取位置", "网页字体大小", "历史记录限制",
                "启动时恢复标签", "强制新窗口打开", "跟随系统深色模式", "清除主页背景", "恢复主页配置"));
        SettingMenuPopup menuPopup = new SettingMenuPopup(this, "UI界面自定义", operations, text -> {
            switch (text) {
                case "强制新窗口打开":
                    boolean forceNewWindow = PreferenceMgr.getBoolean(getContext(), "forceNewWindow", false);
                    new XPopup.Builder(getContext())
                            .asBottomList("强制新窗口打开网页", new String[]{"关闭", "开启"}, null, forceNewWindow ? 1 : 0, (position, t) -> {
                                PreferenceMgr.put(getContext(), "forceNewWindow", position == 1);
                                ToastMgr.shortCenter(getContext(), "已" + t + "强制新窗口打开网页");
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
                case "网页打开应用":
                    new PromptDialog(getContext())
                            .setTitleText("网页打开应用提示设置")
                            .setSpannedContentByStr("是否允许提示打开外部应用，默认同一网页允许提示打开对应应用，同一个网页同一时刻只能提醒三次（避免流氓网站一直提示），使用不再提示后有网页想要打开外部应用也不再提示")
                            .setPositiveListener(SettingConfig.openAppNotify ? "不再提示" : "允许提示", dialog -> {
                                dialog.dismiss();
                                SettingConfig.setOpenAppNotify(getContext(), !SettingConfig.openAppNotify);
                                ToastMgr.shortBottomCenter(getContext(), "已设置为" + (SettingConfig.openAppNotify ? "允许提示" : "不再提示"));
                            }).show();
                    break;
                case "网页获取位置":
                    new XPopup.Builder(getContext())
                            .asConfirm("网页获取位置设置", "是否允许网页获取位置信息？", "不允许", "允许", () -> {
                                SettingConfig.setOpenGeoNotify(getContext(), true);
                                ToastMgr.shortBottomCenter(getContext(), "已设置为允许");
                            }, () -> {
                                SettingConfig.setOpenGeoNotify(getContext(), false);
                                ToastMgr.shortBottomCenter(getContext(), "已设置为不允许");
                            }, false)
                            .show();
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
                case "历史记录限制":
                    int historyCount = PreferenceMgr.getInt(Application.getContext(), "historyCount", 300);
                    String[] titles = new String[]{"不记录历史", "保留100条", "保留300条", "保留500条", "保留1000条"};
                    int select = 2;
                    if (historyCount == 0) {
                        select = 0;
                    } else if (historyCount == 100) {
                        select = 1;
                    } else if (historyCount == 300) {
                        select = 2;
                    } else if (historyCount == 500) {
                        select = 3;
                    } else if (historyCount == 1000) {
                        select = 4;
                    }
                    new XPopup.Builder(getContext())
                            .asBottomList("历史记录限制", titles, null, select, (position, t) -> {
                                int count = 300;
                                if (position == 0) {
                                    count = 0;
                                } else if (position == 1) {
                                    count = 100;
                                } else if (position == 2) {
                                    count = 300;
                                } else if (position == 3) {
                                    count = 500;
                                } else if (position == 4) {
                                    count = 1000;
                                }
                                PreferenceMgr.put(Application.getContext(), "historyCount", count);
                                if (count < historyCount) {
                                    ToastMgr.shortCenter(getContext(), "已设置为" + t);
                                } else {
                                    ToastMgr.shortCenter(getContext(), "已设置为" + t + "，旧的数据需要手动删除");
                                }
                            }).show();
                    break;
                case "启动时恢复标签":
                    int recoverLastTab = PreferenceMgr.getInt(getContext(), "vip", "recoverLastTab", 0);
                    new XPopup.Builder(getContext())
                            .asBottomList("启动时恢复未关闭标签", new String[]{"不恢复", "手动恢复", "自动恢复"}, null, recoverLastTab, (position, t) -> {
                                PreferenceMgr.put(getContext(), "vip", "recoverLastTab", position);
                                ToastMgr.shortCenter(getContext(), "已设置为" + t);
                            }).show();
                    break;
                case "跟随系统深色模式":
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        ToastMgr.shortBottomCenter(getContext(), "当前系统版本不支持深色模式");
                        break;
                    }
                    boolean forceDark = PreferenceMgr.getBoolean(getContext(), "forceDark", true);
                    new XPopup.Builder(getContext())
                            .asBottomList("跟随系统深色模式", new String[]{"跟随系统（默认推荐）", "禁用（无法完全禁用，不推荐）"},
                                    null, forceDark ? 0 : 1, ((p, t) -> {
                                        boolean force = p == 0;
                                        PreferenceMgr.put(getContext(), "forceDark", force);
                                        new XPopup.Builder(getContext())
                                                .asConfirm("温馨提示", "设置成功，需要重启软件才能生效，是否立即重启？", () -> {
                                                    android.os.Process.killProcess(android.os.Process.myPid());
                                                    System.exit(0);
                                                }).show();
                                    })).show();
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
        ClipboardUtil.getText(getContext(), webViewBg, text -> {
            if (TextUtils.isEmpty(text)) {
                return;
            }
            String shareText = text.trim();
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
        });
    }

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
}

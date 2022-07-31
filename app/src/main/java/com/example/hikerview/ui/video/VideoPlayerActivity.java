package com.example.hikerview.ui.video;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.RSRuntimeException;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.hikerview.R;
import com.example.hikerview.constants.CollectionTypeConstant;
import com.example.hikerview.constants.MediaType;
import com.example.hikerview.constants.PreferenceConstant;
import com.example.hikerview.event.OnTimeChangedEvent;
import com.example.hikerview.event.PlaySourceUpdateEvent;
import com.example.hikerview.event.video.BackMainEvent;
import com.example.hikerview.event.video.OnDeviceUpdateEvent;
import com.example.hikerview.event.video.PlayChapterEvent;
import com.example.hikerview.event.web.DestroyEvent;
import com.example.hikerview.model.DownloadRecord;
import com.example.hikerview.model.ViewCollection;
import com.example.hikerview.model.ViewCollectionExtraData;
import com.example.hikerview.model.ViewHistory;
import com.example.hikerview.service.http.CodeUtil;
import com.example.hikerview.service.parser.BaseParseCallback;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.service.parser.LazyRuleParser;
import com.example.hikerview.service.parser.WebkitParser;
import com.example.hikerview.service.parser.X5WebViewParser;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.GlideApp;
import com.example.hikerview.ui.GlideRequest;
import com.example.hikerview.ui.base.BaseTranslucentActivity;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.ui.browser.model.DetectorManager;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.detail.DetailUIHelper;
import com.example.hikerview.ui.dlan.DLandataInter;
import com.example.hikerview.ui.dlan.DlanListPop;
import com.example.hikerview.ui.dlan.DlanPlayEvent;
import com.example.hikerview.ui.dlan.MediaPlayActivity;
import com.example.hikerview.ui.download.DownloadDialogUtil;
import com.example.hikerview.ui.download.DownloadRecordsActivity;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.miniprogram.service.HistoryMemoryService;
import com.example.hikerview.ui.music.HeadsetButtonReceiver;
import com.example.hikerview.ui.setting.MoreSettingActivity;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.thunder.ThunderManager;
import com.example.hikerview.ui.video.event.MusicAction;
import com.example.hikerview.ui.video.event.MusicInfo;
import com.example.hikerview.ui.video.model.PlayData;
import com.example.hikerview.ui.video.remote.LivePlayerHelper;
import com.example.hikerview.ui.video.remote.WebPlayerHelper;
import com.example.hikerview.ui.video.util.VideoCacheHolder;
import com.example.hikerview.ui.video.util.VideoUtil;
import com.example.hikerview.ui.view.CenterLayoutManager;
import com.example.hikerview.ui.view.CustomBottomRecyclerViewPopup;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.ui.view.CustomRecyclerViewAdapter;
import com.example.hikerview.ui.view.XiuTanResultPopup;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.ui.webdlan.LocalServerParser;
import com.example.hikerview.ui.webdlan.RemoteServerManager;
import com.example.hikerview.ui.webdlan.WebServerManager;
import com.example.hikerview.ui.webdlan.model.DlanUrlDTO;
import com.example.hikerview.utils.AndroidBarUtils;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.DataTransferUtils;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.DlanListPopUtil;
import com.example.hikerview.utils.GlideUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.example.hikerview.utils.NotifyManagerUtils;
import com.example.hikerview.utils.PiPUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.RandomUtil;
import com.example.hikerview.utils.ScreenSwitchUtils;
import com.example.hikerview.utils.ScreenUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.TaskUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.UriUtils;
import com.example.hikerview.utils.WebUtil;
import com.example.hikerview.utils.view.DialogUtil;
import com.example.viewlibrary.util.BlurUtil;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ui.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Util;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.AttachListPopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.qingfeng.clinglibrary.service.manager.ClingManager;
import com.skydoves.expandablelayout.ExpandableLayout;
import com.smarx.notchlib.NotchScreenManager;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import chuangyuan.ycj.videolibrary.listener.VideoInfoListener;
import chuangyuan.ycj.videolibrary.utils.VideoPlayUtils;
import chuangyuan.ycj.videolibrary.video.GestureVideoPlayer;
import chuangyuan.ycj.videolibrary.video.ManualPlayer;
import chuangyuan.ycj.videolibrary.video.VideoPlayerManager;
import chuangyuan.ycj.videolibrary.widget.ExoDefaultTimeBar;
import chuangyuan.ycj.videolibrary.widget.VideoPlayerView;
import jp.wasabeef.glide.transformations.internal.FastBlur;
import jp.wasabeef.glide.transformations.internal.RSBlur;
import kotlin.Unit;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.END_OF;
import static android.widget.RelativeLayout.START_OF;
import static com.example.hikerview.utils.PreferenceMgr.SETTING_CONFIG;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT;
import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH;

/**
 * 作者：By hdy
 * 日期：On 2017/10/26
 * 时间：At 22:02
 */

public class VideoPlayerActivity extends BaseTranslucentActivity implements View.OnClickListener {
    private static final String TAG = "V4VideoPlayer";
    private static volatile String url;
    private ManualPlayer player;
    private String title = "视频播放";
    private long position = 0;
    private VideoPlayerView videoPlayerView;
    private boolean isShowMenu = false;
    private ScreenSwitchUtils switchUtils;
    private View listCard;
    private TextView timeView, descView, music_duration, music_position;
    private TextView playerChooseTitle;
    private ObjectAnimator objectAnimator;
    public static boolean isMusic = false;
    private ExoDefaultTimeBar musicTimeBar;
    private long initPlayPos;
    private boolean hasErrorNeedDeal = false;
    private TextView tv_show_info;
    private TextView control_back;
    public static List<VideoChapter> chapters = new ArrayList<>();
    private ScrollView listScrollView;
    private TextView video_str_view, audio_str_view, video_address_view;
    private int nowPos = 0;
    private boolean handleForScreenMode = false;
    private int jumpStartDuration, jumpEndDuration;
    private boolean dealAutoJumpEnd = false;
    private boolean webDlanPlaying = false;
    private String memoryTitle = null;
    private View exo_bg_video_top, custom_lock_screen_bg, custom_control_bottom, exo_controller_bottom;
    private ImageView exo_play_pause2;
    private int notchWidth, notchHeight;
    private View exo_pip;
    private VideoPlayerView.Layout layoutNow = VideoPlayerView.Layout.VERTICAL;
    private ScrollView sVController;
    private PiPUtil mPipUtil;
    private DlanListPop dlanListPop;
    private ExpandableLayout expandableVideoInfo;
    private Format mFormat;
    private LoadingPopupView loadingPopupView;
    private View jinyun_bg;
    private ImageView music_play;
    private TextView music_title;
    private String playUrl;
    private int switchIndex = 0;
    private CenterLayoutManager gridLayoutManager;
    private ChapterAdapter chapterAdapter;
    private BasePopupView chapterPopup;
    private ImageView danmuControllView;
    private long pausePosition;
    private long pauseDuration;
    private boolean isOnPause;
    private RecyclerView chapterRecyclerView;
    private int chapterMenuSpan = 3;

    /**
     * 避免预加载覆盖用户手动操作结果
     */
    private boolean isLoading = false;

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
                if (isFinishing()) {
                    this.cancel();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            EventBus.getDefault().post(new OnTimeChangedEvent());
            try {
                if (player == null) {
                    this.cancel();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            if (SettingConfig.autoStopMinutes > 0) {
                long now = System.currentTimeMillis();
                if (now - SettingConfig.autoStopStartTime > SettingConfig.autoStopMinutes * 60 * 1000) {
                    runOnUiThread(() -> {
                        if (player.isPlaying()) {
                            if (isMusic) {
                                pauseMusic();
                            } else {
                                player.setStartOrPause(false);
                            }
                            ToastMgr.shortCenter(getContext(), "已定时停止播放");
                            SettingConfig.autoStopMinutes = 0;
                        }
                    });
                    return;
                }
            }
            try {
                runOnUiThread(() -> {
                    try {
                        if (player != null && player.getPlayer() != null && !isLoading &&
                                player.getDuration() > 300000 && !player.getPlayer().isCurrentWindowLive()) {
                            updateDownloadPlayPos(player.getCurrentPosition(), player.getDuration());
                            HeavyTaskUtil.saveNowPlayerPos(getContext(), getMemoryId(), player.getCurrentPosition());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (!dealAutoJumpEnd) {
                    runOnUiThread(() -> {
                        if (player == null || isFinishing()) {
                            return;
                        }
                        int jumpEnd = jumpEndDuration * 1000;
                        long duration = player.getDuration();
//                        Timber.d("dealAutoJumpEnd, duration=%d, jump=%d， pos=%d", duration, jumpEnd, player.getCurrentPosition());
                        if (jumpEnd > 0 && duration > jumpEnd) {
                            Timber.d("dealAutoJumpEnd, one, last=%d, jumpEnd=%d", duration - player.getCurrentPosition(), jumpEnd);
                            //时长超过跳过片尾的时长，并且到片尾曲的位置了
                            if (duration - player.getCurrentPosition() < jumpEnd) {
                                dealAutoJumpEnd = true;
                                ToastMgr.shortCenter(getContext(), "正在跳过片尾");
                                dealPlayEnd();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //预加载
            runOnUiThread(() -> {
                try {
                    if (isFinishing() || player == null) {
                        return;
                    }
                    MusicForegroundService.position = player.getCurrentPosition();
                    if (MusicForegroundService.info == null || !StringUtils.equals(title, MusicForegroundService.info.getTitle())
                            || player.isPlaying() == MusicForegroundService.info.isPause()) {
                        if (EventBus.getDefault().hasSubscriberForEvent(MusicInfo.class)) {
                            EventBus.getDefault().post(new MusicInfo(title, null, !player.isPlaying()));
                        }
                    }
                    if (!player.isPlaying() || player.getDuration() < 1000 || isLoading) {
                        return;
                    }
                    if (player.getDuration() - player.getCurrentPosition() < 180 * 1000) {
                        //还有三分钟结束的时候预加载
                        preParse();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    };

    private void updateDownloadPlayPos(long currentPosition, long duration) {
        if (CollectionUtil.isNotEmpty(chapters)) {
            for (VideoChapter chapter : chapters) {
                if (chapter.getDownloadRecord() == null) {
                    return;
                }
                if (chapter.isUse()) {
                    int percent = (int) ((float) currentPosition / (float) duration * 100);
                    if (percent == 0) {
                        percent = 1;
                    }
                    String playPos = percent + "%";
                    HeavyTaskUtil.executeNewTask(() -> {
                        DownloadRecord record = LitePal.find(DownloadRecord.class, chapter.getDownloadRecord().getId());
                        if (record != null) {
                            record.setPlayPos(playPos);
                            record.save();
                        }
                    });
                    break;
                }
            }
        }
    }

    private SimpleAnalyticsListener analyticsListener;

    private Timer timer = new Timer();

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        boolean justLand = PreferenceMgr.getBoolean(getContext(), "justLand", ScreenUtil.isTablet(getContext()));
        if (justLand) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        return R.layout.activity_video_play;
    }

    private String getMemoryId() {
        return VideoUtil.INSTANCE.getMemoryId(url, chapters);
    }

    @Override
    protected void initView() {
        chapters = new ArrayList<>();
        MyStatusBarUtil.setColor(this, getResources().getColor(R.color.black));
        boolean useNotch = PreferenceMgr.getBoolean(getContext(), PreferenceConstant.KEY_useNotch, true);
        if (useNotch) {
            NotchScreenManager.getInstance().setDisplayInNotch(this);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);   //设置沉浸式虚拟键，在MIUI系统中，虚拟键背景透明。原生系统中，虚拟键背景半透明。
        PlayerChooser.hasPlayer = true;
        //为啥必须在这里就执行？因为放后面去执行会先竖屏再横屏，直接布局横屏也不行，因为不需要显示
        boolean justLand = PreferenceMgr.getBoolean(getContext(), "justLand", ScreenUtil.isTablet(getContext()));
        if (justLand || isTv()) {
            //TV模式直接全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            handleForScreenMode = false;
        }
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String from = getIntent().getStringExtra("title");
        if (from != null && (!from.equals(""))) {
            title = from;
        }
        url = getIntent().getStringExtra("videourl");
        long chaptersKey = getIntent().getLongExtra("chapters", 0);
        if (chaptersKey > 0) {
            //取出来，然后清掉
            nowPos = getIntent().getIntExtra("nowPos", 0);
            List<VideoChapter> chapterList = PlayerChooser.getChapterMap().get(chaptersKey);
            if (chapterList != null) {
                chapters.addAll(chapterList);
                PlayerChooser.getChapterMap().delete(chaptersKey);
                DataTransferUtils.INSTANCE.putCacheString(JSON.toJSONString(chapters), "tempChapter");
            } else {
                //可能切换了深色模式，或者被后台杀掉了
                String cache = DataTransferUtils.INSTANCE.loadCacheString("tempChapter");
                if (StringUtil.isNotEmpty(cache)) {
                    chapterList = JSON.parseArray(cache, VideoChapter.class);
                    if (CollectionUtil.isNotEmpty(chapterList)) {
                        chapters.addAll(chapterList);
                    }
                }
            }
        }
        isMusic = UrlDetector.isMusic(url);
        url = UrlDetector.clearTag(url);
        jumpStartDuration = PreferenceMgr.getInt(getContext(), "jumpStartDuration", 0);
        jumpEndDuration = PreferenceMgr.getInt(getContext(), "jumpEndDuration", 0);
        try {
            if (extraDataBundle != null && extraDataBundle.containsKey("viewCollectionExtraData")) {
                ViewCollectionExtraData extraData = ViewCollectionExtraData.extraDataFromJson(extraDataBundle.getString("viewCollectionExtraData"));
                jumpStartDuration = extraData.getJumpStartDuration();
                jumpEndDuration = extraData.getJumpEndDuration();
            }
        } catch (Exception ignored) {
        }
        initPlayPos = HeavyTaskUtil.getPlayerPos(getContext(), getMemoryId());
        setPlayPos();
        //实例化播放控制类
        videoPlayerView = findView(R.id.new_video_player);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) videoPlayerView.getLayoutParams();
        layoutParams.height = ScreenUtil.getScreenMin(this) * 9 / 16;
        videoPlayerView.setLayoutParams(layoutParams);
        addLayoutChangeListener();
        musicTimeBar = findView(R.id.music_progress);
        music_duration = findView(R.id.music_duration);
        music_position = findView(R.id.music_position);
        videoPlayerView.findViewById(R.id.custom_mode).setOnClickListener(this);
        videoPlayerView.findViewById(R.id.custom_dlan).setOnClickListener(this);
        videoPlayerView.findViewById(R.id.custom_chapter).setOnClickListener(this);
        videoPlayerView.findViewById(R.id.custom_next).setOnClickListener(this);
        videoPlayerView.findViewById(R.id.custom_last).setOnClickListener(this);
        exo_pip = videoPlayerView.findViewById(R.id.exo_pip);
        exo_pip.setOnClickListener(this);
        exo_bg_video_top = findView(R.id.exo_controller_top);
        custom_lock_screen_bg = findView(R.id.custom_lock_screen_bg);
        custom_control_bottom = findView(R.id.custom_control_bottom);
        exo_controller_bottom = findView(R.id.exo_controller_bottom);
        exo_play_pause2 = findView(R.id.exo_play_pause2);
        danmuControllView = findView(R.id.exo_video_danmu);
        danmuControllView.setOnClickListener(this::toggleDanmuControl);
        videoPlayerView.setBottomAnimateViews(new ArrayList<>(Arrays.asList(
                custom_lock_screen_bg,
                custom_control_bottom,
                findView(R.id.exo_video_fullscreen),
                findView(R.id.exo_video_switch),
                danmuControllView,
                exo_play_pause2
        )));
        videoPlayerView.getExoFullscreen().setOnClickListener(v -> {
            if (mFormat == null) {
                mFormat = Format.createVideoSampleFormat(null, null, null,
                        0, 0, 1920, 1080, 0.0f, new ArrayList<>(),
                        null);
            }
            enterFullScreen(mFormat);
        });
        videoPlayerView.setRightAnimateView(exo_pip);
        findView(R.id.auto_jump).setOnClickListener(this);
        ((View) videoPlayerView.findViewById(R.id.exo_loading_show_text).getParent()).setBackgroundColor(getResources().getColor(R.color.transparent));
        timeView = videoPlayerView.findViewById(R.id.custom_toolbar_time);
        descView = videoPlayerView.findViewById(R.id.custom_toolbar_desc);
        descView.setOnClickListener(this);
        playerChooseTitle = findView(R.id.player_choose_title);
        boolean networkNotify = PreferenceMgr.getBoolean(getContext(), "networkNotify", true);
        if ((CollectionUtil.isNotEmpty(chapters) && chapters.get(0).getDownloadRecord() != null)
                || (url.startsWith("content://"))) {
            //不能根据file://来判断，因为规则可能有这样开头的地址
            networkNotify = false;
        }
        videoPlayerView.setNetworkNotify(networkNotify);
        if (isMusic) {
            videoPlayerView.setNetworkNotifyUseDialog(true);
        }
        tv_show_info = findView(R.id.tv_show_info);
        tv_show_info.setText(("当前播放：" + title));
        control_back = findView(R.id.control_back);
        if (isMusic) {
            ((TextView) (findView(R.id.other_text))).setText("后台播放");
        }

        if (useNotch) {
            NotchScreenManager.getInstance().getNotchInfo(this, notchScreenInfo -> {
                if (notchScreenInfo.hasNotch) {
                    for (Rect rect : notchScreenInfo.notchRects) {
                        Timber.i("notch screen Rect =  %s", rect.toShortString());
                        if (notchWidth < rect.width()) {
                            notchWidth = rect.width();
                        }
                        if (notchHeight < rect.height()) {
                            notchHeight = rect.height();
                        }
                    }
                    Timber.i("notch screen width =  %s height = %s", notchWidth, notchHeight);
                    updateNotchMargin(layoutNow);
                }
//            else {
//                //测试
//                notchWidth = notchHeight = DisplayUtil.dpToPx(getContext(), 20);
//                updateNotchMargin(layoutNow);
//            }
            });
        }
        String realUrl = HttpParser.getRealUrlFilterHeaders(url);
        PlayData playData = HttpParser.getPlayData(realUrl);
        VideoCacheHolder.INSTANCE.initConfig();
        VideoPlayerManager.Builder builder = new VideoPlayerManager.Builder(VideoPlayerManager.TYPE_PLAY_MANUAL, videoPlayerView)
                .setTitle(title)
                .setPosition(position)
                .setUriProxy((uri, streamType) -> {
                    if (!isMusic && !isFromDownload() && VideoCacheHolder.INSTANCE.getUseProxy()) {
                        String u = uri.toString();
                        String realUrl1 = HttpParser.getRealUrlFilterHeaders(url);
                        PlayData playData1 = HttpParser.getPlayData(realUrl1);
                        Map<String, String> headers;
                        if (CollectionUtil.isNotEmpty(playData1.getUrls())) {
                            headers = getHeaders(playData1);
                        } else {
                            headers = HttpParser.getHeaders(url);
                        }
                        return Uri.parse(VideoCacheHolder.INSTANCE.getProxyUrl(getContext(), u, streamType, headers, () -> {
                            if (!isFinishing()) {
                                reStartPlayer(false);
                            }
                            return 0;
                        }));
                    } else {
                        return uri;
                    }
                });
        videoPlayerView.setSeekListener(pos -> {
            if (VideoCacheHolder.INSTANCE.getUseProxy() && player != null) {
                VideoCacheHolder.INSTANCE.seek(pos, player.getDuration());
            }
        });
        if (CollectionUtil.isNotEmpty(playData.getUrls())) {
            videoPlayerView.setShowVideoSwitch(true);
            switchIndex = getSwitchIndexFromHis();
            if (switchIndex >= playData.getUrls().size()) {
                switchIndex = 0;
            }
            if (CollectionUtil.isNotEmpty(playData.getAudioUrls())) {
                builder.setAudioUrls(playData.getAudioUrls());
            }
            builder.setPlayUri(switchIndex, CollectionUtil.toStrArray(playData.getUrls()),
                    CollectionUtil.toStrArray(playData.getNames()), getHeaders(playData), playData.getSubtitle());
            playUrl = playData.getUrls().get(switchIndex);
            videoPlayerView.setSwitchName(playData.getNames(), switchIndex);
            ((TextView) findView(R.id.player_x5_text)).setText("线路");
            loadDanmaku(playData);
            //弹幕设置
            if (StringUtil.isNotEmpty(playData.getDanmu())) {
                int danmuLine = PreferenceMgr.getInt(getContext(), "danmuLine", 5);
                if (danmuLine < 0) {
                    danmuControllView.setImageDrawable(getResources().getDrawable(R.drawable.ic_danmu_hide));
                }
            }
        } else {
            playUrl = realUrl;
            builder.setPlayUri(realUrl, HttpParser.getHeaders(url));
//            loadDanmaku(playData);
        }
        //字幕背景透明化
        SubtitleView subtitleView = videoPlayerView.findViewById(R.id.exo_subtitles);
        if (subtitleView != null) {
            subtitleView.setStyle(new CaptionStyleCompat(
                    Color.WHITE,
                    Color.TRANSPARENT,
                    Color.TRANSPARENT,
                    CaptionStyleCompat.EDGE_TYPE_NONE,
                    Color.WHITE,
                    /* typeface= */ null));
        }
        MusicForegroundService.position = 0;
        player = builder
                .addUpdateProgressListener((position, bufferedPosition, duration) -> {
                    if (position < 0 || duration < 0) {
                        return;
                    }
                    if (initPlayPos > 0 && player.getPlayer().isCurrentWindowLive()) {
                        //直播也记忆了进度，回到0
                        initPlayPos = 0;
                        VideoPlayerActivity.this.position = 0;
                        reStartPlayer(false);
//                        Timber.d("addUpdateProgressListener: %s, %s", player.getCurrentPosition(), player.getPlayer().isCurrentWindowLive());
                        return;
                    }
                    if (pausePosition > 0 && player.isPlaying()) {
                        long seekPos = pausePosition;
                        long seekDuration = pauseDuration;
                        pausePosition = 0;
                        pauseDuration = 0;
                        if (seekDuration > 600000 && seekDuration - duration > seekDuration / 2) {
                            //比缓存时的视频总长缩水了一半，那可能触发了防盗链
                            Timber.d("比缓存时的视频总长缩水了一半，那可能触发了防盗链, %s->%s", seekDuration, duration);
                            VideoPlayerActivity.this.position = seekPos;
                            int nowPos = getNowUsed();
                            playByPos(nowPos, nowPos, false);
                        }
                    }
                    musicTimeBar.setDuration(duration);
                    musicTimeBar.setPosition(position);
//                    musicTimeBar.setBufferedPosition(bufferedPosition);
                    StringBuilder formatBuilder = new StringBuilder();
                    Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
                    music_duration.setText(Util.getStringForTime(formatBuilder, formatter, duration));
                    music_position.setText(Util.getStringForTime(formatBuilder, formatter, position));
                })
                .addVideoInfoListener(new VideoInfoListener() {
                    @Override
                    public void onPlayStart(long currPosition) {
                        if (player != null && initPlayPos > 0 && player.getDuration() - initPlayPos < 10000) {
                            //跳过片头后视频长度不足5分钟，或者上次播放位置已经到最后5分钟
                            initPlayPos = 0;
                            player.seekTo(0);
                            if (!isMusic) {
                                ToastMgr.shortBottomCenter(getContext(), "上次播放剩余时长不足10秒，已重新播放");
                            }
                        }
                        video_address_view.setText(("视频地址：" + playUrl));
                    }

                    @Override
                    public void onLoadingChanged() {

                    }

                    @Override
                    public void onPlayerError(@Nullable ExoPlaybackException e) {
                        //检查是不是在播放下载中的
                        String path = WebServerManager.instance().getRootPath();
                        if (StringUtil.isNotEmpty(playUrl)
                                && (playUrl.contains("://127.0.0.1") || playUrl.contains(LocalServerParser.getIP(getContext())))
                                && StringUtil.isNotEmpty(path) && path.endsWith(".temp")) {
                            path = path.replace(".temp", "");
                            DownloadRecord record = getDownloadRecord();
                            if (record != null && new File(path).exists()) {
                                //说明下载完成了
                                WebServerManager.instance().stopServer();
                                String filePath = LocalServerParser.getFilePath(record);
                                if (new File(filePath).exists()) {
                                    //确认一下mp4文件存在
                                    url = "file://" + filePath;
                                    initPlayPos = player.getCurrentPosition();
                                    position = initPlayPos;
                                    reStartPlayer(false);
                                    return;
                                }
                            }
                        }
                        if (videoPlayerView != null && videoPlayerView.isLock()) {
                            videoPlayerView.getmLockControlView().setLockCheck(false);
                        }
                        if (pausePosition > 0) {
                            VideoPlayerActivity.this.position = pausePosition;
                            pausePosition = 0;
                            pauseDuration = 0;
                            int nowPos = getNowUsed();
                            playByPos(nowPos, nowPos, false);
                            return;
                        }
                        if (VideoCacheHolder.INSTANCE.isProxyError()) {
                            if (!isFinishing()) {
                                reStartPlayer(false);
                            }
                            return;
                        }
                        if (videoPlayerView.isShowVideoSwitch()) {
                            showVideoSwitch(true);
                            return;
                        }
                        if (autoChangeXiuTanVideo()) {
                            return;
                        }
                        hasErrorNeedDeal = true;
                        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            String dom = StringUtil.getDom(WebUtil.getShowingUrl());
                            DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, "www.fy-sys.cn");
                            showVideoList(true);
                        } else {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                    }

                    @Override
                    public void onPlayEnd() {
                        dealPlayEnd();
                    }

                    @Override
                    public void isPlaying(boolean playWhenReady) {
                    }
                }).create();
        player.setSwitchListener(this::switchVideo);
        player.setPlaybackParameters(VideoPlayerManager.PLAY_SPEED, 1f);
        player.setPlayerGestureOnTouch(true);
        player.setOnDoubleTapListener(this::onPlayerDoubleTap);
        expandableVideoInfo = findViewById(R.id.expand_video_info);
        expandableVideoInfo.parentLayout.setOnClickListener(v -> {
            setVideoInfo();
            if (expandableVideoInfo.isExpanded()) {
                expandableVideoInfo.collapse();
            } else {
                expandableVideoInfo.expand();
            }
        });
        expandableVideoInfo.secondLayout.setOnClickListener(v -> {
            expandableVideoInfo.collapse();
        });
        expandableVideoInfo.secondLayout.setOnLongClickListener(v -> {
            new PromptDialog(getContext())
                    .setDialogType(PromptDialog.DIALOG_TYPE_INFO)
                    .setAnimationEnable(true)
                    .setTitleText("完整链接")
                    .setContentText(url)
                    .setPositiveListener("复制到剪切版", dialog -> {
                        ClipboardUtil.copyToClipboard(getContext(), url, true);
                        dialog.dismiss();
                    }).show();
            return true;
        });
        addFormatListener();
        startPlayOrStartThirdPlayer();
        findView(R.id.video_help).setOnClickListener(this);
        findView(R.id.next).setOnClickListener(this);
        findView(R.id.collect).setOnClickListener(this);
        findView(R.id.speed).setOnClickListener(this);
        control_back.setOnClickListener(this);
        //拉伸模式
        findView(R.id.mode).setOnClickListener(this);
        View d = findView(R.id.download);
        d.setOnClickListener(this);
        d.setOnLongClickListener(v -> {
            startActivity(new Intent(getContext(), DownloadRecordsActivity.class));
            return true;
        });
        findView(R.id.player_x5).setOnClickListener(this);
        //网页投屏
        View remotePlay = findView(R.id.remote_play);
        findView(R.id.more_setting).setOnClickListener(v -> showMoreSetting());
        findView(R.id.auto_stop).setOnClickListener(v -> stopWhenTimeOut());
        findView(R.id.remote_live).setOnClickListener(v -> playWithLiveTv());
        findView(R.id.remote_live).setOnLongClickListener(v -> {
            if (isMusic) {
                pauseMusic();
            } else {
                player.setStartOrPause(false);
            }
            LivePlayerHelper.rescanLiveTv(VideoPlayerActivity.this, loadingPopupView, title, playUrl);
            return true;
        });
        findView(R.id.remote_player_redirect).setOnClickListener(v -> {
            startWebDlan(true, true);
        });
        //网页投屏
        remotePlay.setOnClickListener(v -> startWebDlan(true, false));
        //传统投屏
        View dlan = findView(R.id.dlan);
        dlan.setOnClickListener(this);
        dlan.setOnLongClickListener(v -> {
            String p = LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(playUrl));
            if (CollectionUtil.isEmpty(DlanListPopUtil.instance().getDevices())) {
                DlanListPopUtil.instance().reInit();
            }
            if (dlanListPop == null) {
                dlanListPop = new DlanListPop(this, DlanListPopUtil.instance().getDevices());
            }
            dlanListPop.updateTitleAndUrl(p, title, getHeaders(playData));
            new XPopup.Builder(this)
                    .asCustom(dlanListPop)
                    .show();
            return true;
        });
        findView(R.id.chapters).setOnClickListener(this);
        findView(R.id.other).setOnClickListener(this);
        findView(R.id.copy).setOnClickListener(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        ImageButton custom_lock_screen = videoPlayerView.findViewById(R.id.custom_lock_screen);
        custom_lock_screen.setOnClickListener(this);
        boolean isScreenLocked = (boolean) PreferenceMgr.get(getContext(), "ijkplayer", "isScreenLocked", false);
        int playerInt = getIntent().getIntExtra("player", PlayerEnum.PLAYER_TWO.getCode());
        //第三方播放器，那么锁定竖屏
        if (playerInt != PlayerEnum.PLAYER_TWO.getCode()) {
            isScreenLocked = true;
        }
        //重力感应
        switchUtils = new ScreenSwitchUtils(getContext(), isScreenLocked);
        boolean autoLand = PreferenceMgr.getBoolean(getContext(), "autoLand", true);
        if (!autoLand) {
            switchUtils.setTempLocked(true);
        }
        switchUtils.setAutoLand(autoLand);
        custom_lock_screen.setImageDrawable(isScreenLocked ? getResources().getDrawable(R.drawable.screen_lock_rotation) : getResources().getDrawable(R.drawable.screen_rotation));
        timer.schedule(timerTask, 0, 1000 * 2);
        initActionAdapter();
        if (isMusic) {
            findView(R.id.bg).setFitsSystemWindows(false);
            ((TextView) findView(R.id.screen_mode_text)).setText("全屏显示");
            videoPlayerView.setVisibility(View.GONE);
            jinyun_bg = findView(R.id.jinyun_bg);
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            ImageView jinyunView = jinyun_bg.findViewById(R.id.sv_bg);
            music_play = jinyun_bg.findViewById(R.id.music_play);
            music_title = jinyun_bg.findViewById(R.id.music_title);
            music_play.setOnClickListener(v -> findView(R.id.sv_bg).performClick());
            jinyun_bg.findViewById(R.id.music_last).setOnClickListener(v -> {
                nextMovie(true);
            });
            jinyun_bg.findViewById(R.id.music_next).setOnClickListener(v -> {
                nextMovie(false);
            });
            jinyun_bg.findViewById(R.id.music_menu).setOnClickListener(this::showMusicMenu);
            jinyun_bg.findViewById(R.id.music_list).setOnClickListener(this);
            ViewGroup.MarginLayoutParams jinyunLayoutParams = (ViewGroup.MarginLayoutParams) jinyunView.getLayoutParams();
            jinyunLayoutParams.height = ScreenUtil.getScreenMin(this) / 2;
            jinyunLayoutParams.width = jinyunLayoutParams.height;
            jinyunView.setLayoutParams(jinyunLayoutParams);
            verticalFullScreenForMusic(jinyun_bg, true);

            jinyun_bg.setVisibility(View.VISIBLE);
//            timer.cancel();
            musicTimeBar.addListener(new TimeBar.OnScrubListener() {
                @Override
                public void onScrubStart(TimeBar timeBar, long position) {

                }

                @Override
                public void onScrubMove(TimeBar timeBar, long position) {

                }

                @Override
                public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                    player.seekTo(position);
                }
            });
            try {
                switchUtils.stopListen();
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
            Application.application.startMusicForegroundService(getContext());
            initJinyunView();
        }
        sVController = findViewById(R.id.sv_controller);
        // TODO 进入 PiP 后再判定？
//        boolean backToPiP = PreferenceMgr.getBoolean(getContext(), PreferenceConstant.KEY_BACK_TO_PIP, false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
//        boolean backgroundToPiP = PreferenceMgr.getBoolean(getContext(), PreferenceConstant.KEY_BACKGROUND_TO_PIP, false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
//        if (backToPiP || backgroundToPiP) {
//            TaskUtil.setTaskExcludeFromRecents(this, true, new String[]{"hiker.intent.category.player"});
//        }
        video_address_view.setText(("视频地址：" + playUrl));
        exo_play_pause2.setOnClickListener(v -> {

            player.setStartOrPause(!player.isPlaying());
        });
        if (CollectionUtil.isNotEmpty(chapters) && chapters.size() > 1) {
            RecyclerView recyclerView = findView(R.id.recyclerView);
            gridLayoutManager = new CenterLayoutManager(getContext());
            gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
            recyclerView.setLayoutManager(gridLayoutManager);
            chapterAdapter = new ChapterAdapter(getContext(), chapters, (view, pos) -> {
                playByPos(pos);
            });
            recyclerView.setAdapter(chapterAdapter);
            for (int i = 0; i < chapters.size(); i++) {
                if (chapters.get(i).isUse()) {
                    gridLayoutManager.scrollToPosition(i);
                    break;
                }
            }
        }
        HeadsetButtonReceiver.Companion.registerHeadsetReceiver(this);
        if (ThunderManager.INSTANCE.getTaskId() > 0) {
            //正在播放磁力，加个通知栏避免被杀后台
            Application.application.startDlanForegroundService(getContext());
        }
        boolean towFingerTouch = PreferenceMgr.getBoolean(getContext(), "towFingerTouch", true);
        if (!towFingerTouch) {
            videoPlayerView.getPlayerView().setUseTwoFingerTouch(false);
        }
    }

    private boolean containsClass(StackTraceElement[] elements, String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (StackTraceElement element : elements) {
            if (s.equals(element.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isFromDownload() {
        try {
            return CollectionUtil.isNotEmpty(chapters) && chapters.get(0).getDownloadRecord() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private DownloadRecord getDownloadRecord() {
        if (CollectionUtil.isNotEmpty(chapters)) {
            for (VideoChapter chapter : chapters) {
                if (chapter.getDownloadRecord() == null) {
                    return null;
                }
                if (chapter.isUse()) {
                    DownloadRecord record = LitePal.find(DownloadRecord.class, chapter.getDownloadRecord().getId());
                    chapter.setDownloadRecord(record);
                    return record;
                }
            }
        }
        return null;
    }

    private void toggleDanmuControl(View v) {
        int danmuLine = PreferenceMgr.getInt(getContext(), "danmuLine", 5);
        int newLine = danmuLine > 0 ? -1 : 5;
        danmuControllView.setImageDrawable(getResources().getDrawable(newLine > 0 ? R.drawable.ic_danmu_show : R.drawable.ic_danmu_hide));
        PreferenceMgr.put(getContext(), "danmuLine", newLine);
        if (newLine > 0) {
            //关了马上又开不生效，真是坑啊
            loadDanmaku(HttpParser.getPlayData(url));
        } else {
            if (videoPlayerView.isUseDanmuWebView()) {
                videoPlayerView.useWebDanmuku(false, null, -1, getDanmuContainer());
            } else {
                videoPlayerView.useDanmuku(false, null, -1, getDanmuContainer());
            }
        }
    }

    private List<String> switchVideo(List<String> urls, int index) {
        try {
            switchIndex = index;
            PlayData data = HttpParser.getPlayData(url);
            if (CollectionUtil.isEmpty(data.getUrls())) {
                if (StringUtil.isNotEmpty(data.getUrl())) {
                    switchIndex = 0;
                    data.setUrls(new ArrayList<>(Collections.singletonList(data.getUrl())));
                } else {
                    ToastMgr.shortBottomCenter(getContext(), "线路为空");
                    return urls;
                }
            }
            if (data.getUrls().size() <= index) {
                switchIndex = 0;
            }
            playUrl = data.getUrls().get(index);
            memoryLastClickAndSwitchIndex(false);
            return data.getUrls();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        invalidateCache();
        return urls;
    }

    private ViewGroup getDanmuContainer() {
        if (isMusic) {
            return findView(R.id.danmuContainer);
        } else {
            return null;
        }
    }

    private void loadDanmaku(PlayData playData) {
        if (StringUtil.isNotEmpty(playData.getDanmu())) {
//        String url = "https://comment.bilibili.com/441212122.xml";
            String url = playData.getDanmu();
            int danmuLine = PreferenceMgr.getInt(getContext(), "danmuLine", 5);
            if (danmuLine <= 0) {
                videoPlayerView.useDanmuku(false, null, 5, getDanmuContainer());
                return;
            }
            if (url.startsWith("web://")) {
                String u = url.replace("web://", "");
                if (u.startsWith("hiker://files")) {
                    u = "file://" + JSEngine.getFilePath(u);
                }
                videoPlayerView.useWebDanmuku(true, u, danmuLine, getDanmuContainer());
                return;
            }
            Timber.d("loadDanmaku: %s", url);
            HeavyTaskUtil.executeNewTask(() -> {
                boolean isJson = url.contains(".json");
                String path = JSEngine.getFilePath("hiker://files/cache/danmu." + (isJson ? "json" : "xml"));
                CodeUtil.download(url.split(";")[0], path, HttpParser.getHeaders(url), new CodeUtil.OnCodeGetListener() {
                    @Override
                    public void onSuccess(String s) {
                        if (!isFinishing() && videoPlayerView != null) {
                            runOnUiThread(() -> videoPlayerView.useDanmuku(true, new File(s), danmuLine, getDanmuContainer()));
                        }
                    }

                    @Override
                    public void onFailure(int errorCode, String msg) {
                        if (!isFinishing()) {
                            runOnUiThread(() -> ToastMgr.shortBottomCenter(getContext(), msg));
                        }
                        if (!isFinishing() && videoPlayerView != null) {
                            runOnUiThread(() -> videoPlayerView.useDanmuku(false, null, 5, getDanmuContainer()));
                        }
                    }
                });
            });
        } else {
            videoPlayerView.useDanmuku(false, null, 5, getDanmuContainer());
        }
    }

    /**
     * 让预加载的缓存失效
     */
    private void invalidateCache() {
        if (CollectionUtil.isNotEmpty(chapters)) {
            for (VideoChapter chapter : chapters) {
                chapter.setUrlCache(null);
            }
        }
    }

    private void showVideoSwitch(boolean forError) {
        VideoUtil.INSTANCE.showVideoSwitch(this, switchIndex, url, forError, position -> {
            switchIndex = position;
            memoryLastClickAndSwitchIndex(false);
//                        invalidateCache();
            reStartPlayer(true);
            return Unit.INSTANCE;
        });
    }

    private String getPosUrl(String url) {
        PlayData data = HttpParser.getPlayData(url);
        if (CollectionUtil.isNotEmpty(data.getUrls())) {
            return data.getUrls().get(0);
        } else {
            return url;
        }
    }


    private void showMusicMenu(View view) {
        int mode = PreferenceMgr.getInt(getContext(), "ijkplayer", "musicMode", 0);
        String modeName = mode == 0 ? "列表循环" : (mode == 1 ? "随机播放" : "单曲循环");
        new XPopup.Builder(getContext())
                .atView(view)
                .asAttachList(new String[]{"下载资源", modeName, "倍速播放", "后台播放"}, null,
                        (position, text) -> {
                            switch (text) {
                                case "下载资源":
                                    DownloadDialogUtil.showEditDialog(VideoPlayerActivity.this, title, playUrl);
                                    break;
                                case "倍速播放":
                                    showMusicSpeed(view);
                                    break;
                                case "后台播放":
                                    EventBus.getDefault().post(new BackMainEvent());
                                    ToastMgr.shortBottomCenter(getContext(), "已后台播放，通过系统多任务界面可以回到当前窗口");
                                    break;
                                case "列表循环":
                                case "随机播放":
                                case "单曲循环":
                                    showMusicMode(view);
                                    break;
                            }
                        })
                .show();
    }

    private void showMusicMode(View view) {
        new XPopup.Builder(getContext())
                .atView(view)
                .asAttachList(new String[]{"列表循环", "随机播放", "单曲循环"}, null,
                        (p, t) -> {
                            PreferenceMgr.put(getContext(), "ijkplayer", "musicMode", p);
                            ToastMgr.shortBottomCenter(getContext(), t);
                        })
                .show();
    }

    private void showMusicSpeed(View view) {
        new XPopup.Builder(getContext())
                .atView(view)
                .asAttachList(new String[]{" ×1.0 ", " ×1.2 ", " ×1.5 ", " ×2.0 "}, null,
                        (p, t) -> {
                            switch (t) {
                                case " ×1.0 ":
                                    playFromSpeed(1f);
                                    break;
                                case " ×1.2 ":
                                    playFromSpeed(1.2f);
                                    break;
                                case " ×1.5 ":
                                    playFromSpeed(1.5f);
                                    break;
                                case " ×2.0 ":
                                    playFromSpeed(2f);
                                    break;
                            }
                        })
                .show();
    }

    private void verticalFullScreenForMusic(View jinyun_bg, boolean fullScreen) {
        LinearLayout.LayoutParams jinyun_bgLayoutParams = (LinearLayout.LayoutParams) jinyun_bg.getLayoutParams();
        if (fullScreen) {
            ChangeBounds changeBounds = new ChangeBounds();
            //开启延迟动画，在这里会记录当前视图树的状态
            changeBounds.setDuration(400);
            TransitionManager.beginDelayedTransition((ViewGroup) jinyun_bg, changeBounds);

            View music_controller = jinyun_bg.findViewById(R.id.music_controller);
            music_title.setText(title);
            music_title.animate().alpha(1).setDuration(300).start();
            music_title.setVisibility(VISIBLE);
            music_controller.setVisibility(VISIBLE);
            RelativeLayout.LayoutParams cly = (RelativeLayout.LayoutParams) music_controller.getLayoutParams();
            cly.setMargins(0, DisplayUtil.dpToPx(getContext(), 20), 0, DisplayUtil.dpToPx(getContext(), 50));
            cly.height = DisplayUtil.dpToPx(getContext(), 36);
            music_controller.setLayoutParams(cly);

//            VideoPlayUtils.hideActionBar(getContext());
            jinyun_bgLayoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            jinyun_bg.setLayoutParams(jinyun_bgLayoutParams);
            AndroidBarUtils.setTranslucent(this);
        } else {
            VideoPlayUtils.showActionBar(this);
            ChangeBounds changeBounds = new ChangeBounds();
            //开启延迟动画，在这里会记录当前视图树的状态
            changeBounds.setDuration(400);
            TransitionManager.beginDelayedTransition((ViewGroup) jinyun_bg, changeBounds);

            music_title.setVisibility(GONE);
            View music_controller = jinyun_bg.findViewById(R.id.music_controller);
            music_controller.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams cly = (RelativeLayout.LayoutParams) music_controller.getLayoutParams();
            cly.setMargins(0, 0, 0, 0);
            cly.height = 1;
            music_controller.setLayoutParams(cly);

            jinyun_bgLayoutParams.height = ScreenUtil.getScreenMin(this) * 9 / 16 + DisplayUtil.dpToPx(getContext(), 30);
            jinyun_bg.setLayoutParams(jinyun_bgLayoutParams);
            getWindow().getDecorView().setSystemUiVisibility(videoPlayerView.getSystemUiVisibility());
        }
    }

    private void playWithLiveTv() {
        if (isMusic) {
            pauseMusic();
        } else {
            player.setStartOrPause(false);
        }
        LivePlayerHelper.playWithLiveTv(this, loadingPopupView, title, playUrl);
    }

    private void enterFullScreen(Format format) {
        if (!videoPlayerView.isNowVerticalFullScreen() && VideoPlayUtils.getOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏切横屏landscape
            switchUtils.setTempLocked(false);
        }
        if (videoPlayerView.isNowVerticalFullScreen()) {
            //从竖屏全屏切回竖屏，先临时禁止重力感应，因为此时重力感应会导致整个屏幕横屏，播放区域没有全屏
            switchUtils.setTempLocked(true);
            videoPlayerView.postDelayed(() -> {
                try {
                    if (!isFinishing()) {
                        boolean isScreenLocked = (boolean) PreferenceMgr.get(getContext(), "ijkplayer", "isScreenLocked", false);
                        if (!isScreenLocked) {
                            switchUtils.setTempLocked(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 3000);
        }
        if (format.width < format.height) {
            videoPlayerView.verticalFullScreen();
        } else {
            videoPlayerView.enterFullScreen();
        }
        switchUtils.onResume(this);
    }

    private void setVideoInfo() {
        TextView tvVideoInfoFormat = expandableVideoInfo.secondLayout.findViewById(R.id.tv_video_info_format);
        tvVideoInfoFormat.setText(video_str_view.getText());
        TextView tvAudioInfoFormat = expandableVideoInfo.secondLayout.findViewById(R.id.tv_audio_info_format);
        tvAudioInfoFormat.setText(audio_str_view.getText());
        TextView tvVideoInfoAddress = expandableVideoInfo.secondLayout.findViewById(R.id.tv_video_info_address);
        tvVideoInfoAddress.setText(("视频地址：" + playUrl));
    }

    private void updateNotchMargin(VideoPlayerView.Layout layout) {
        if (notchHeight > 0 && notchWidth > 0) {
            FrameLayout lock = ((FrameLayout) videoPlayerView.getmLockControlView());
            View control_bottom_bg = findView(R.id.control_bottom_bg);
            int delta;
            if (layout == VideoPlayerView.Layout.VERTICAL || layout == VideoPlayerView.Layout.VERTICAL_LAND) {
                delta = -notchHeight;
            } else {
                delta = notchHeight;
            }
            initPaddingTag(exo_bg_video_top, lock, control_bottom_bg);
            initMarginTag(exo_pip, videoPlayerView.getExoControlsBack());

            addPaddingLeftRight(delta, exo_bg_video_top, lock, control_bottom_bg);
            addLeftRightMargin(delta, exo_pip, videoPlayerView.getExoControlsBack());

            if (layout == VideoPlayerView.Layout.VERTICAL || layout == VideoPlayerView.Layout.VERTICAL_LAND) {
                addTag(0, exo_bg_video_top, lock, control_bottom_bg, exo_pip, videoPlayerView.getExoControlsBack());
            } else {
                addTag(delta, exo_bg_video_top, lock, control_bottom_bg, exo_pip, videoPlayerView.getExoControlsBack());
            }
        }
    }

    private void initPaddingTag(View... views) {
        for (View view : views) {
            if (view.getTag() != null) {
                continue;
            }
            VideoViewTag videoViewTag = new VideoViewTag(0, view.getPaddingLeft(), view.getPaddingRight());
            videoViewTag.setSourceTop(view.getPaddingTop());
            videoViewTag.setSourceBottom(view.getPaddingBottom());
            view.setTag(videoViewTag);
        }
    }

    private void initMarginTag(View... views) {
        for (View view : views) {
            if (view.getTag() != null) {
                continue;
            }
            if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                VideoViewTag videoViewTag = new VideoViewTag(0, layoutParams.leftMargin, layoutParams.rightMargin);
                view.setTag(videoViewTag);
            } else if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                VideoViewTag videoViewTag = new VideoViewTag(0, layoutParams.leftMargin, layoutParams.rightMargin);
                view.setTag(videoViewTag);
            }
        }
    }

    private void addTag(int delta, View... views) {
        for (View view : views) {
            VideoViewTag videoViewTag = (VideoViewTag) view.getTag();
            videoViewTag.setDelta(delta);
            view.setTag(videoViewTag);
        }
    }

    private void addPaddingLeftRight(int delta, View... views) {
        for (View view : views) {
            VideoViewTag tag = (VideoViewTag) view.getTag();
            if (tag == null || tag.getDelta() == delta) {
                continue;
            }
            if (delta <= 0) {
                view.setPadding(tag.getSourceLeft(), tag.getSourceTop(), tag.getSourceRight(), tag.getSourceBottom());
            } else {
                view.setPadding(delta + view.getPaddingLeft(), tag.getSourceTop(), delta + view.getPaddingRight(), tag.getSourceBottom());
            }
        }
    }

    private void addLeftRightMargin(int delta, View... views) {
        for (View view : views) {
            VideoViewTag tag = (VideoViewTag) view.getTag();
            if (tag == null || tag.getDelta() == delta) {
                continue;
            }
            if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                if (delta <= 0) {
                    layoutParams.leftMargin = tag.getSourceLeft();
                    layoutParams.rightMargin = tag.getSourceRight();
                } else {
                    layoutParams.leftMargin = layoutParams.leftMargin + delta;
                    layoutParams.rightMargin = layoutParams.rightMargin + delta;
                }
                view.setLayoutParams(layoutParams);
            } else if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                if (delta < 0) {
                    layoutParams.leftMargin = tag.getSourceLeft();
                    layoutParams.rightMargin = tag.getSourceRight();
                } else {
                    layoutParams.leftMargin = layoutParams.leftMargin + delta;
                    layoutParams.rightMargin = layoutParams.rightMargin + delta;
                }
                view.setLayoutParams(layoutParams);
            }
        }
    }

    private void addLayoutChangeListener() {
        videoPlayerView.setOnLayoutChangeListener(layout -> {
            Timber.i("notch screen width =  %s height = %s", notchWidth, notchHeight);
            layoutNow = layout;
            updateNotchMargin(layoutNow);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) exo_controller_bottom.getLayoutParams();
            if (layout == VideoPlayerView.Layout.VERTICAL) {
                //非全屏
                exo_bg_video_top.setVisibility(View.INVISIBLE);
                if (listCard.getVisibility() == VISIBLE) {
                    listCard.setVisibility(View.INVISIBLE);
                    videoPlayerView.getPlaybackControlView().setShowTimeoutMs(5000);
                }
                danmuControllView.setVisibility(GONE);
                custom_lock_screen_bg.setVisibility(GONE);
                custom_control_bottom.setVisibility(GONE);
                exo_play_pause2.setVisibility(VISIBLE);
                layoutParams.addRule(END_OF, R.id.exo_play_pause2);
                layoutParams.addRule(START_OF, R.id.exo_video_fullscreen);
                layoutParams.addRule(ALIGN_PARENT_BOTTOM);
                if (gridLayoutManager != null) {
                    for (int i = 0; i < chapters.size(); i++) {
                        if (chapters.get(i).isUse()) {
                            gridLayoutManager.scrollToPosition(i);
                            break;
                        }
                    }
                }
            } else {
                //全屏
                if (hasDanmu()) {
                    danmuControllView.setVisibility(VISIBLE);
                }
                exo_bg_video_top.setVisibility(VISIBLE);
                custom_lock_screen_bg.setVisibility(View.VISIBLE);
                custom_control_bottom.setVisibility(VISIBLE);
                exo_play_pause2.setVisibility(GONE);
                layoutParams.removeRule(END_OF);
                layoutParams.removeRule(START_OF);
                layoutParams.removeRule(ALIGN_PARENT_BOTTOM);
            }
            exo_controller_bottom.setLayoutParams(layoutParams);
            if (chapterPopup != null && chapterPopup.isShow()) {
                chapterPopup.dismiss();
            }
        });
        videoPlayerView.getPlaybackControlView().setPlayPauseListener(isPlaying -> {
            exo_play_pause2.setImageDrawable(getResources().getDrawable(isPlaying ? R.drawable.ic_pause_ : R.drawable.ic_play_));
        });
    }

    private boolean hasDanmu() {
        PlayData playData = HttpParser.getPlayData(url);
        return StringUtil.isNotEmpty(playData.getDanmu());
    }


    private void showFastJumpNotice(int gap) {
        String notice = videoPlayerView.getNotice();
        int finalJump = gap;
        if (StringUtil.isNotEmpty(notice)) {
            if (notice.contains("已快进") && gap < 0) {
                Timber.d("之前快进，现在快退, gap=%s", gap);
            } else if (notice.contains("已快退") && gap > 0) {
                Timber.d("之前快退，现在快进, gap=%s", gap);
            } else {
                String nowJump = notice.replace("已快退", "").replace("秒", "").replace("已快进", "");
                if (StringUtil.isNotEmpty(nowJump)) {
                    try {
                        int jump = Integer.parseInt(nowJump);
                        if (jump != 0) {
                            if (notice.contains("已快退")) {
                                jump = -jump;
                            }
                            finalJump = jump + gap;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (finalJump != 0) {
            videoPlayerView.showNotice((finalJump > 0 ? "已快进" : "已快退") + Math.abs(finalJump) + "秒");
        }
    }

    /**
     * 双击手势处理
     *
     * @param e
     * @param tapArea 双击区域
     */
    private void onPlayerDoubleTap(MotionEvent e, GestureVideoPlayer.DoubleTapArea tapArea) {
        if (listCard.getVisibility() == VISIBLE) {
            return;
        }
        if (tapArea == GestureVideoPlayer.DoubleTapArea.LEFT) {
            fastPositionJump(-10L);
            showFastJumpNotice(-10);
        } else if (tapArea == GestureVideoPlayer.DoubleTapArea.RIGHT) {
            fastPositionJump(10L);
            showFastJumpNotice(10);
        } else {
            player.setStartOrPause(!player.isPlaying());
        }
    }

    private void showMoreSetting() {
        String[] titles = isMusic ? new String[]{"使用其它播放器", "修改默认播放器", "修改视频链接"} :
                new String[]{"修改默认播放器", "修改视频链接", "本地字幕文件"};
        new XPopup.Builder(getContext())
                .asCenterList("更多设置", titles, (position, text) -> {
                    switch (text) {
                        case "使用其它播放器":
                            if (isMusic) {
                                pauseMusic();
                            } else {
                                player.setStartOrPause(false);
                            }
                            PlayData playData = HttpParser.getPlayData(url);
                            PlayerChooser.startMultiPlayer(getContext(), title, playUrl, url, getHeaders(playData), playData.getSubtitle(), playerName -> false);
                            break;
                        case "设置定时停止播放":
                            stopWhenTimeOut();
                            break;
                        case "修改默认播放器":
                            showPlayerSetting();
                            break;
                        case "本地字幕文件":
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(intent, 1);
                            break;
                        case "修改视频链接":
                            new XPopup.Builder(getContext())
                                    .asInputConfirm(text, null, url, "播放地址", s -> {
                                        url = s;
                                        reStartPlayer(true);
                                        ToastMgr.shortCenter(getContext(), "已修改视频链接");
                                    }, null, R.layout.xpopup_confirm_input).show();
                            break;
                    }
                }).show();
    }

    private void showPlayerSetting() {
        try {
            // 设置片单播放器
            // TODO BUG 若先设置为非默认播放器，则播放器界面设置的是全局播放器
            if (extraDataBundle == null || !extraDataBundle.containsKey("viewCollectionExtraData")) {
                PlayerChooser.setDefaultPlayer(this, "videoPlayer");
                return;
            }
            ViewCollectionExtraData extraData = ViewCollectionExtraData.extraDataFromJson(extraDataBundle.getString("viewCollectionExtraData"));
            if (extraData == null) {
                PlayerChooser.setDefaultPlayer(this, "videoPlayer");
                return;
            }
            ViewCollection viewCollection = DetailUIHelper.findViewCollectionById(extraData.getCollectionId());
            PlayerChooser.setDefaultPlayer(this, "videoPlayer", viewCollection);
        } catch (Exception ignored) {
            // 设置全局播放器
            PlayerChooser.setDefaultPlayer(this, "videoPlayer");
        }
    }

    private void stopWhenTimeOut() {
        Map<String, Integer> timeMap = getTimeMap();
        String[] titles = new String[]{"不开启", "1分钟后", "10分钟后", "20分钟后", "30分钟后", "45分钟后", "60分钟后", "一个半小时后", "两个小时后"};
        String now = "不开启";
        for (Map.Entry<String, Integer> entry : timeMap.entrySet()) {
            if (entry.getValue() == SettingConfig.autoStopMinutes) {
                now = entry.getKey();
                break;
            }
        }
        new XPopup.Builder(getContext())
                .asBottomList("定时停止播放，当前：" + now, titles, (position, text) -> {
                    SettingConfig.autoStopMinutes = !timeMap.containsKey(text) ? 0 : timeMap.get(text);
                    SettingConfig.autoStopStartTime = System.currentTimeMillis();
                    if (SettingConfig.autoStopMinutes != 0) {
                        ToastMgr.shortCenter(getContext(), "已设置" + text + "停止播放");
                    } else {
                        ToastMgr.shortCenter(getContext(), "已关闭定时停止播放");
                    }
                }).show();
    }

    private Map<String, Integer> getTimeMap() {
        Map<String, Integer> timeMap = new HashMap<>();
        timeMap.put("不开启", 0);
        timeMap.put("1分钟后", 1);
        timeMap.put("10分钟后", 10);
        timeMap.put("20分钟后", 20);
        timeMap.put("30分钟后", 30);
        timeMap.put("45分钟后", 45);
        timeMap.put("60分钟后", 60);
        timeMap.put("一个半小时后", 90);
        timeMap.put("两个小时后", 120);
        return timeMap;
    }

    private void dealPlayEnd() {
        if (isMusic) {
            if (player.getDuration() > 10000 && player.getDuration() - initPlayPos < 10000) {
                player.seekTo(0);
                initPlayPos = 0;
            } else {
                if (objectAnimator.isRunning()) {
                    objectAnimator.pause();
                }
                nextMovie(false);
            }
        } else {
            if (player.getDuration() > 10000 && player.getDuration() - initPlayPos < 10000) {
                player.seekTo(0);
                initPlayPos = 0;
                ToastMgr.shortBottomCenter(getContext(), "上次播放剩余时长不足10秒，已重新播放");
            } else {
                nextMovie(false);
            }
        }
    }

    private void addFormatListener() {
        boolean directFullScreen = PreferenceMgr.getBoolean(getContext(), PreferenceConstant.KEY_DIRECT_FULLSCREEN, false);
        int directFullScreenMode = PreferenceMgr.getInt(getContext(), PreferenceConstant.KEY_DIRECT_FULLSCREEN_MODE, 0);
        if (analyticsListener == null) {
            analyticsListener = new SimpleAnalyticsListener() {
                @Override
                public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {
                    if (VideoPlayerManager.PLAY_SPEED != 1f && player.getPlayer() != null && player.getPlayer().isCurrentWindowLive()) {
                        playFromSpeed(1f);
                    }
                    if (trackType == C.TRACK_TYPE_VIDEO) {
                        if (!handleForScreenMode && format != null) {
                            mFormat = format;
                            handleForScreenMode = true;
                            if (directFullScreen) {
                                switch (directFullScreenMode) {
                                    case 0:
                                        if (format == null) {
                                            format = Format.createVideoSampleFormat(null, null, null,
                                                    0, 0, 1920, 1080, 0.0f, new ArrayList<>(),
                                                    null);
                                        }
                                        enterFullScreen(format);
                                        break;
                                    case 1:
                                        //拉伸模式
                                        handleForScreenMode = true;
                                        videoPlayerView.verticalFullScreen();
                                        break;
                                }
                            } else {
                                if (format.width < format.height) {
                                    if (!videoPlayerView.isNowVerticalFullScreen()) {
                                        videoPlayerView.verticalFullScreen();
                                    }
                                }
                            }
                            // 读取到之后修改 PiP 窗口大小
                            mPipUtil.setVideoWidthAndHeight(format.width, format.height);
                            if (mPipUtil.isInPictureInPictureMode()) {
                                mPipUtil.updatePiPRatio(format.width, format.height);
                            }
                        }
                        video_str_view.setText(("视频格式：" + SimpleAnalyticsListener.getVideoString(format)));
                    } else if (trackType == C.TRACK_TYPE_AUDIO) {
                        audio_str_view.setText(("音频格式：" + SimpleAnalyticsListener.getAudioString(format)));
                    }
                    setVideoInfo();
                }
            };
        }
        player.getPlayer().removeAnalyticsListener(analyticsListener);
        player.getPlayer().addAnalyticsListener(analyticsListener);
    }

    private void startPlayOrStartThirdPlayer() {
        int playerInt = getIntent().getIntExtra("player", PlayerEnum.PLAYER_TWO.getCode());
        if (playerInt != PlayerEnum.PLAYER_TWO.getCode()) {
            //第三方播放器
            player.bindListener();
            player.startPlayerNoPlay();
            videoPlayerView.showBtnContinueHint(VISIBLE, null);
            String playerName = PlayerEnum.findName(playerInt);
            if (StringUtil.isNotEmpty(playerName)) {
                PlayData playData = HttpParser.getPlayData(url);
                PlayerChooser.startPlayer(getContext(), playerName, title, playUrl, url, getHeaders(playData), playData.getSubtitle(), null);
            } else {
                ToastMgr.shortBottomCenter(getContext(), "获取播放器失败：" + playerInt);
            }
        } else {
            //直接播放
            player.startPlayer();
//            if (player.getDuration() > 5000 && player.getDuration() - initPlayPos < 5000) {
//                player.seekTo(0);
//            }
        }
    }

    /**
     * 网页投屏
     *
     * @param showToast 复制链接时是否显示toast
     */
    private void startWebDlan(boolean showToast, boolean forRedirect) {
        webDlanPlaying = true;
        if (isMusic) {
            pauseMusic();
        } else {
            player.setStartOrPause(false);
        }
        PlayData playData = HttpParser.getPlayData(url);
        WebPlayerHelper.INSTANCE.start(videoPlayerView, playData, getUrlWithHeaders(playData), showToast, forRedirect, playUrl -> {
            DlanUrlDTO urlDTO = new DlanUrlDTO(jumpStartDuration, jumpEndDuration);
            urlDTO.setTitle(title);
            playerChooseTitle.setText(("远程：" + playUrl));
            switchUtils.onPause();
            return urlDTO;
        });
    }

    private String getUrlWithHeaders(PlayData playData) {
        if (CollectionUtil.isNotEmpty(playData.getHeaders()) && playData.getHeaders().size() > switchIndex) {
            String headers = HttpParser.getHeadersStr(playData.getHeaders().get(switchIndex));
            return playUrl + ";" + headers;
        }
        String[] s = url.split(";");
        if (s.length <= 1) {
            return playUrl;
        } else {
            return playUrl + ";" + StringUtil.arrayToString(s, 1, ";");
        }
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        handleForScreenMode = true;
        if (getRequestedOrientation() == requestedOrientation) {
            return;
        }
        if (isMusic || (videoPlayerView != null && videoPlayerView.isNowVerticalFullScreen())) {
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }

    private void initJinyunView() {
        ImageView jinyunView = findView(R.id.sv_bg);
        jinyunView.setOnClickListener(v -> {
            if (player.isPlaying()) {
                pauseMusic();
            } else {
                resumeMusic();
            }
        });
        String image = null;
        if (CollectionUtil.isNotEmpty(chapters)) {
            for (VideoChapter chapter : chapters) {
                if (chapter.isUse()) {
                    image = chapter.getPicUrl();
                    break;
                }
            }
        }
        loadMusicImage(image);
        objectAnimator = ObjectAnimator.ofFloat(jinyunView, "rotation", 0f, 360f);
        objectAnimator.setDuration(20 * 1000);
        objectAnimator.setRepeatMode(ValueAnimator.RESTART);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(-1);
        objectAnimator.start();
    }

    private void pauseMusic() {
        Glide.with(getContext()).load(R.drawable.play_circle).into(music_play);
        player.setStartOrPause(false);
        if (objectAnimator.isRunning()) {
            objectAnimator.pause();
        }
    }

    private void resumeMusic() {
        Glide.with(getContext()).load(R.drawable.time_out).into(music_play);
        if (objectAnimator.isPaused()) {
            objectAnimator.resume();
        }
        player.setStartOrPause(true);
    }


    /**
     * 增加一层渐变，防止浅色图片导致图标看不清
     *
     * @param bm
     * @return
     */
    private Bitmap addShadowForMovieOne(Bitmap bm) {
        Canvas canvas = new Canvas(bm);
        ColorDrawable colorDrawable = new ColorDrawable(0x80000000);
        colorDrawable.setBounds(0, 0, bm.getWidth(), bm.getHeight());
        colorDrawable.draw(canvas);
        return bm;
    }

    private void loadMusicImage(String image) {
        Object src = StringUtil.isEmpty(image) ? R.mipmap.ic_show :
                GlideUtil.getGlideUrl(image, !image.contains("@Referer=") ? image + "@Referer=" : image);
        ImageView jinyunView = findView(R.id.sv_bg);

        GlideRequest<Bitmap> transforms = GlideApp
                .with(getContext())
                .asBitmap()
                .load(R.mipmap.ic_show)
                .circleCrop();
        Glide.with(this).asBitmap().apply(RequestOptions.bitmapTransform(new CircleCrop())).load(src).thumbnail(transforms).into(jinyunView);
        Glide.with(getContext())
                .asBitmap()
                .load(src)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_show))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (isFinishing()) {
                            return;
                        }
                        Timber.d("MusicInfo:%s", EventBus.getDefault().hasSubscriberForEvent(MusicInfo.class));
                        EventBus.getDefault().postSticky(new MusicInfo(title, resource, !player.isPlaying()));
                        //默认缩小为1/4
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(resource,
                                resource.getWidth() / 4,
                                resource.getHeight() / 4,
                                false);
                        Bitmap bitmap;
                        try {
                            bitmap = RSBlur.blur(getContext(), scaledBitmap, 15);
                        } catch (RSRuntimeException e) {
                            bitmap = FastBlur.blur(scaledBitmap, 15, true);
                        }
                        ImageView iv_bg = findView(R.id.iv_bg);
                        iv_bg.setImageBitmap(addShadowForMovieOne(bitmap));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        try {
                            if (placeholder instanceof GifDrawable) {
                                ((GifDrawable) placeholder).stop();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (isFinishing()) {
                            return;
                        }
                        Bitmap error = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_show);
                        Bitmap bitmap = BlurUtil.doBlur(error, 10, 30);
                        EventBus.getDefault().postSticky(new MusicInfo(title, error, !player.isPlaying()));
                        ImageView iv_bg = findView(R.id.iv_bg);
                        iv_bg.setImageBitmap(bitmap);
                        super.onLoadFailed(errorDrawable);
                    }
                });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        //上次拉伸模式
        int resizeMode = (int) PreferenceMgr.get(getContext(), "ijkplayer", "resizeMode", RESIZE_MODE_FIT);
        switch (resizeMode) {
            case RESIZE_MODE_FIT:
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/自适应"));
                break;
            case RESIZE_MODE_FILL:
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/充满"));
                break;
            case RESIZE_MODE_FIXED_WIDTH:
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/宽度"));
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/高度"));
                break;
        }
        videoPlayerView.getPlayerView().setResizeMode(resizeMode);
        mPipUtil = new PiPUtil(this, videoPlayerView, new PiPUtil.OnPipMediaControlListener() {
            @Override
            public boolean isPlaying() {
                return player.isPlaying();
            }

            @Override
            public void onPause() {
                player.onPause();
            }

            @Override
            public void onMediaPlay() {
                player.setStartOrPause(true);
            }

            @Override
            public void onMediaPause() {
                if (isMusic) {
                    pauseMusic();
                } else {
                    player.setStartOrPause(false);
                }
            }

            @Override
            public void onLast() {
                nextMovie(true);
            }

            @Override
            public void onNext() {
                nextMovie(false);
            }
        });
        if (mPipUtil.checkPipPermission()) {
            exo_pip.setVisibility(VISIBLE);
        }
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoPlayerView.doOnConfigurationChanged(Configuration.ORIENTATION_LANDSCAPE);
        }
    }


    private void fastPositionJump(long forward) {
        long newPos = player.getCurrentPosition() + forward * 1000;
        if (player.getDuration() < newPos) {
            position = player.getDuration() - 1000;
        } else if (newPos < 0) {
            if (forward > 0) {
                position = forward * 1000;
            } else {
                position = 0;
            }
        } else {
            position = newPos;
        }
        player.seekTo(position);
        try {
            if (!player.getPlayer().isCurrentWindowLive()) {
                HeavyTaskUtil.saveNowPlayerPos(getContext(), getMemoryId(), position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dealActionViewClick(View v) {
        switch (v.getId()) {
            //拉伸模式
            case R.id.mode_fit:
                videoPlayerView.getPlayerView().setResizeMode(RESIZE_MODE_FIT);
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/自适应"));
                PreferenceMgr.put(getContext(), "ijkplayer", "resizeMode", 0);
                break;
            case R.id.mode_fill:
                videoPlayerView.getPlayerView().setResizeMode(RESIZE_MODE_FILL);
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/充满"));
                PreferenceMgr.put(getContext(), "ijkplayer", "resizeMode", 3);
                break;
            case R.id.mode_fixed_width:
                videoPlayerView.getPlayerView().setResizeMode(RESIZE_MODE_FIXED_WIDTH);
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/宽度"));
                PreferenceMgr.put(getContext(), "ijkplayer", "resizeMode", 1);
                break;
            case R.id.mode_fixed_height:
                videoPlayerView.getPlayerView().setResizeMode(RESIZE_MODE_FIXED_HEIGHT);
                descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/高度"));
                PreferenceMgr.put(getContext(), "ijkplayer", "resizeMode", 2);
                break;
            //快速跳转
            case R.id.jump_10:
            case R.id.jump_30:
            case R.id.jump_60:
            case R.id.jump_120:
            case R.id.jump_10_l:
            case R.id.jump_30_l:
            case R.id.jump_60_l:
                long forward = Long.parseLong((String) v.getTag());
                fastPositionJump(forward);
                break;
            //倍速播放
            case R.id.speed_1:
            case R.id.speed_1_2:
            case R.id.speed_1_5:
            case R.id.speed_2:
            case R.id.speed_p8:
            case R.id.speed_p5:
            case R.id.speed_3:
            case R.id.speed_4:
            case R.id.speed_5:
            case R.id.speed_6:
                float speed = Float.parseFloat((String) v.getTag());
                playFromSpeed(speed);
                break;
        }
    }

    private void initActionAdapter() {
        listCard = videoPlayerView.findViewById(R.id.custom_list_bg);
        View.OnClickListener listener = v -> {
            dealActionViewClick(v);
            reverseListCardVisibility();
        };
        listCard.setOnClickListener(listener);
        listCard.findViewById(R.id.mode_fit).setOnClickListener(listener);
        listCard.findViewById(R.id.mode_fill).setOnClickListener(listener);
        listCard.findViewById(R.id.mode_fixed_width).setOnClickListener(listener);
        listCard.findViewById(R.id.mode_fixed_height).setOnClickListener(listener);
        listCard.findViewById(R.id.jump_10).setOnClickListener(listener);
        listCard.findViewById(R.id.jump_30).setOnClickListener(listener);
        listCard.findViewById(R.id.jump_60).setOnClickListener(listener);
        listCard.findViewById(R.id.jump_120).setOnClickListener(listener);
        listCard.findViewById(R.id.jump_10_l).setOnClickListener(listener);
        listCard.findViewById(R.id.jump_30_l).setOnClickListener(listener);
        listCard.findViewById(R.id.jump_60_l).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_1).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_1_2).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_1_5).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_2).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_p8).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_p5).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_3).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_4).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_5).setOnClickListener(listener);
        listCard.findViewById(R.id.speed_6).setOnClickListener(listener);
        video_str_view = listCard.findViewById(R.id.video_str_view);
        audio_str_view = listCard.findViewById(R.id.audio_str_view);
        video_address_view = listCard.findViewById(R.id.video_address_view);
        listScrollView = findView(R.id.custom_list_scroll_view);
        int dp44 = DisplayUtil.dpToPx(getContext(), 44);
        int dp10 = DisplayUtil.dpToPx(getContext(), 10);
        listScrollView.setPadding(dp10, dp44, dp10, dp44);
    }

    private void refreshListScrollView(boolean open, boolean halfWidth, View listScrollView) {
        int start = 0, end = 0, width = 0;
        if (open) {
            listScrollView.setVisibility(View.VISIBLE);
        }
        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) listScrollView.getLayoutParams();
        if (videoPlayerView.isNowVerticalFullScreen()) {
            width = videoPlayerView.getMeasuredWidth();
            if (halfWidth) {
                start = open ? width : width / 2;
                end = open ? width / 2 : width;
            } else {
                start = open ? width : 0;
                end = open ? 0 : width;
            }
        } else {
            width = videoPlayerView.getMeasuredWidth();
            if (halfWidth) {
                start = open ? width : width / 4 * 3;
                end = open ? width / 4 * 3 : width;
            } else {
                start = open ? width : width / 2;
                end = open ? width / 2 : width;
            }
        }
        ValueAnimator anim = ValueAnimator.ofInt(start, end);
        anim.setDuration(300);
        int finalEnd = end;
        anim.addUpdateListener(animation -> {
            layoutParams1.leftMargin = (Integer) animation.getAnimatedValue();
            listScrollView.setLayoutParams(layoutParams1);
            if (!open && layoutParams1.leftMargin == finalEnd) {
                videoPlayerView.getPlaybackControlView().hide();
                listCard.setVisibility(View.INVISIBLE);
            }
        });
        anim.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateTimeView(OnTimeChangedEvent event) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        timeView.setText(formatter.format(currentTime));
        if (videoPlayerView.getPlayerView().getControllerView().getVisibility() == View.GONE) {
            if (listCard.getVisibility() != View.INVISIBLE) {
                listCard.setVisibility(View.INVISIBLE);
            }
            if (!videoPlayerView.isLock() && videoPlayerView.getLockState() != GONE) {
                videoPlayerView.showLockState(GONE);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDlanDeviceUpdated(OnDeviceUpdateEvent event) {
        if (dlanListPop != null) {
            dlanListPop.notifyDataChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMusicAction(MusicAction action) {
        if (MusicForegroundService.PAUSE.equals(action.getCode())) {
            if (player.isPlaying()) {
                pauseMusic();
            } else {
                resumeMusic();
            }
        } else if (MusicForegroundService.NEXT.equals(action.getCode())) {
            nextMovie(false);
        } else if (MusicForegroundService.PAUSE_NOW.equals(action.getCode())) {
            if (player.isPlaying()) {
                pauseMusic();
            }
        } else if (MusicForegroundService.PREV.equals(action.getCode())) {
            nextMovie(true);
        } else if (MusicForegroundService.CLOSE.equals(action.getCode())) {
            EventBus.getDefault().post(new BackMainEvent());
            super.finishAndRemoveTask();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlaySourceUpdated(PlaySourceUpdateEvent event) {
        Intent intent = event.getIntent();
        if (intent == null) {
            return;
        }
        if (!mPipUtil.isInPIPMode() && !isMusic) {
            finish();
            event.getContext().startActivity(intent);
        }
        String now = intent.getStringExtra("title");
        if (StringUtil.isNotEmpty(now)) {
            if (isMusic && !UrlDetector.isMusic(intent.getStringExtra("videourl"))) {
                finishAndRemoveTask();
                return;
            }
            setIntent(intent);
            title = now;
            updateUrl(intent.getStringExtra("videourl"));
            long chaptersKey = intent.getLongExtra("chapters", 0);
            if (chaptersKey > 0) {
                //取出来，然后清掉
                chapters.clear();
                nowPos = intent.getIntExtra("nowPos", 0);
                List<VideoChapter> chapterList = PlayerChooser.getChapterMap().get(chaptersKey);
                if (chapterList != null) {
                    chapters.addAll(chapterList);
                    PlayerChooser.getChapterMap().delete(chaptersKey);
                    if (isMusic) {
                        music_title.setText(title);
                        loadMusicImage(chapters.get(nowPos).getPicUrl());
                    }
                }
            }
            reStartPlayer(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*
         * 画中画模式锁屏/解锁生命周期：onStop -> onStart
         */
        if (mPipUtil.isInPictureInPictureMode() && player != null) {
            if (objectAnimator != null && objectAnimator.isPaused()) {
                objectAnimator.resume();
            }
            if (!isMusic) {
                player.onResume();
            }
            // switchUtils.onResume(this);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String now = intent.getStringExtra("title");
        String last = getIntent().getStringExtra("title");
        if (StringUtil.isNotEmpty(now) && !now.equals(last)) {
            setIntent(intent);
            title = now;
            updateUrl(intent.getStringExtra("videourl"));
            long chaptersKey = intent.getLongExtra("chapters", 0);
            if (chaptersKey > 0) {
                //取出来，然后清掉
                chapters.clear();
                nowPos = intent.getIntExtra("nowPos", 0);
                List<VideoChapter> chapterList = PlayerChooser.getChapterMap().get(chaptersKey);
                if (chapterList != null) {
                    chapters.addAll(chapterList);
                    PlayerChooser.getChapterMap().delete(chaptersKey);
                }
            }
            reStartPlayer(true);
            return;
        }
        // initView();
        // initData(null);
        /*
         * TODO Pref：因 setIntent 实测仍然无法更新界面，而调用 initView 会在 setOnClickListener 的时候会报 Null，因而暂时暴力重新创建 Activity
         */
        // recreate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (objectAnimator != null && objectAnimator.isPaused() && player.isPlaying()) {
            objectAnimator.resume();
        }
        if (!isMusic) {
            player.onResume();
        }
        switchUtils.onResume(this);
        mPipUtil.onResume();
        videoPlayerView.resumeDanmu();
//        if (!mPipUtil.isInPictureInPictureMode()) {
//            TaskUtil.hideDetailActivityFromRecents(this);
//        }
        isOnPause = false;
        VideoCacheHolder.INSTANCE.resume();
        NotifyManagerUtils.Companion.checkNotificationOnResume(getContext());
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPipUtil.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        isOnPause = true;
        try {
            if (!player.getPlayer().isCurrentWindowLive()) {
                HeavyTaskUtil.saveNowPlayerPos(getContext(), getMemoryId(), player.getCurrentPosition(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (objectAnimator != null && objectAnimator.isRunning()) {
            objectAnimator.pause();
        }
        mPipUtil.onPause();
        pausePosition = player.getCurrentPosition();
        pauseDuration = player.getDuration();
        if (!isMusic && !mPipUtil.isInPictureInPictureMode()) {
            player.onPause();
        }
        if (!mPipUtil.isInPictureInPictureMode()) {
            videoPlayerView.pauseDanmu();
        }
        switchUtils.onPause();
        VideoCacheHolder.INSTANCE.pause();
    }

    @Override
    protected void onDestroy() {
        PlayerChooser.hasPlayer = false;
        try {
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Timber.tag(TAG).w("onDestroy: ");
        try {
            player.onDestroy();
            player = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            videoPlayerView.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        HeadsetButtonReceiver.Companion.unregisterHeadsetReceiver(this);
        switchUtils = null;
        if (isMusic) {
            Application.application.stopMusicForegroundService();
        }
        Application.application.stopDlanForegroundService();
        X5WebViewParser.destroy0();
        WebkitParser.destroy0();
        VideoCacheHolder.INSTANCE.destroy(getContext());
        if(CollectionUtil.isNotEmpty(chapters) && chapters.get(0).getTorrentFileInfo() != null){
            ThunderManager.INSTANCE.release(chapters.get(0).getTorrentFileInfo().torrentPath);
        } else {
            ThunderManager.INSTANCE.release();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        handleForScreenMode = true;
        videoPlayerView.setPipMode(mPipUtil.isInPIPMode());
        player.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            videoPlayerView.getPlayerView().hideController();
        } else {
            if (switchUtils != null) {
                switchUtils.setTempLocked(true);
                videoPlayerView.postDelayed(() -> {
                    try {
                        if (!isFinishing()) {
                            boolean isScreenLocked = (boolean) PreferenceMgr.get(getContext(), "ijkplayer", "isScreenLocked", false);
                            if (!isScreenLocked) {
                                switchUtils.setTempLocked(false);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 3000);
            }
        }
        if (hasErrorNeedDeal && newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            hasErrorNeedDeal = false;
            String dom = StringUtil.getDom(WebUtil.getShowingUrl());
            DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, "www.fy-sys.cn");
            showVideoList(true);
        }
        super.onConfigurationChanged(newConfig);
    }

    private void showVideoList(boolean forError) {
        hasErrorNeedDeal = false;
        final List<DetectedMediaResult> results = DetectorManager.getInstance().getDetectedMediaResults(MediaType.VIDEO_MUSIC);
        if (CollectionUtil.isEmpty(results)) {
            if (!forError) {
                ToastMgr.shortBottomCenter(getContext(), "当前嗅探地址列表为空");
            }
            return;
        }
        if (results.size() == 1 && forError && StringUtils.equals(results.get(0).getUrl(), playUrl)) {
            return;
        }
        new XPopup.Builder(getContext())
                .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
                .asCustom(new XiuTanResultPopup(getContext()).with(results, (url1, type) -> {
                    if ("play".equals(type)) {
                        String[] uu = url.split(";");
                        uu[0] = url1;
                        url1 = StringUtil.arrayToString(uu, 0, ";");
                        updateUrl(url1);
                        HeavyTaskUtil.updateHistoryVideoUrl(WebUtil.getShowingUrl(), playUrl);
                        reStartPlayer(true);
                        String dom = StringUtil.getDom(WebUtil.getShowingUrl());
                        DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, StringUtil.getDom(playUrl));
                    } else if ("复制链接".equals(type)) {
                        ClipboardUtil.copyToClipboard(getContext(), url1);
                    } else if ("深度嗅探".equals(type)) {
                        WebUtil.goWeb(getContext(), url1);
                    } else {
                        String finalUrl = url1;
                        DownloadDialogUtil.showEditDialog(VideoPlayerActivity.this, title, appendHeaders(url, url1), null, downloadTask -> {
                            PlayData playData = HttpParser.getPlayData(finalUrl);
                            if (StringUtil.isNotEmpty(playData.getSubtitle())) {
                                downloadTask.setSubtitle(playData.getSubtitle());
                            }
                        });
                    }
                }).withTitle(forError ? "播放失败，试试换一个链接吧" : "嗅探地址列表（点击播放）")/*.enableDrag(false)*/)
                .show();
    }

    private boolean autoChangeXiuTanVideo() {
        String dom = StringUtil.getDom(WebUtil.getShowingUrl());
        DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, "www.fy-sys.cn");
        List<DetectedMediaResult> results = DetectorManager.getInstance().getDetectedMediaResults(MediaType.VIDEO_MUSIC);
        if (CollectionUtil.isEmpty(results)) {
            return false;
        }
        if (results.size() == 1 && StringUtils.equals(results.get(0).getUrl(), url)) {
            return false;
        }
        for (DetectedMediaResult result : results) {
            if (!result.isClicked() && !StringUtils.equals(result.getUrl(), url)) {
                result.setClicked(true);
                String url1 = PlayerChooser.decorateHeader(
                        getActivity(),
                        WebUtil.getShowingUrl(),
                        result.getUrl()
                );
                updateUrl(url1);
                HeavyTaskUtil.updateHistoryVideoUrl(WebUtil.getShowingUrl(), url);
                reStartPlayer(true);
                DetectorManager.getInstance().putIntoXiuTanLiked(getContext(), dom, StringUtil.getDom(url));
                return true;
            }
        }
        return false;
    }

    private String appendHeaders(String url, String playUrl) {
        if (url.startsWith("{")) {
            PlayData playData = HttpParser.getPlayData(url);
            if (CollectionUtil.isNotEmpty(playData.getHeaders()) && playData.getHeaders().size() > switchIndex) {
                Map<String, String> headers = playData.getHeaders().get(switchIndex);
                return playUrl + ";" + HttpParser.getHeadersStr(headers);
            }
            return playUrl;
        }
        String[] urls = url.split(";");
        if (urls.length > 1) {
            urls[0] = playUrl;
            return StringUtil.arrayToString(urls, 0, ";");
        } else {
            return playUrl;
        }
    }

    private String getFilm() {
        if (extraDataBundle == null || !extraDataBundle.containsKey("film")) {
            return null;
        } else {
            return extraDataBundle.getString("film");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SPACE: //空格键
                videoPlayerView.getPlayerView().hideController();
                player.setStartOrPause(!player.isPlaying());
                break;
            case KeyEvent.KEYCODE_ENTER: //确定键enter
            case KeyEvent.KEYCODE_DPAD_CENTER:
                try {
                    videoPlayerView.getPlayerView().findViewById(chuangyuan.ycj.videolibrary.R.id.sexo_video_fullscreen).performClick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                if (isShowMenu) {
                    finish();
                } else {
                    isShowMenu = true;
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).setTitle("温馨提示")
                            .setMessage("是否退出播放界面？此时点击菜单键会退出播放界面")
                            .setCancelable(true)
                            .setPositiveButton("退出", (dialog, which) -> {
                                finish();
                                dialog.dismiss();
                                isShowMenu = false;
                            })
                            .setNegativeButton("取消", (dialogInterface, i) -> {
                                isShowMenu = false;
                                dialogInterface.dismiss();
                            })
                            .create();
                    DialogUtil.INSTANCE.showAsCard(getContext(), alertDialog);
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                this.onBackPressed();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isTv() {
        try {
            UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
            if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * TODO 在其他应用/多任务进入播放界面再进入 PiP 时，下层 Activity 显示视界的界面；多任务只显示播放界面的 Task
     * 已尝试 task.moveTaskToFront() 无效
     */
    @Override
    public void onBackPressed() {
        if (videoPlayerView != null && videoPlayerView.isLock()) {
            videoPlayerView.showLockState(View.VISIBLE);
            videoPlayerView.getmLockControlView().setLockCheck(false);
            return;
        }
        if (isTv()) {
            //电视直接关闭页面
            finish();
            return;
        }
        if (isMusic && jinyun_bg != null) {
            LinearLayout.LayoutParams jinyun_bgLayoutParams = (LinearLayout.LayoutParams) jinyun_bg.getLayoutParams();
            if (jinyun_bgLayoutParams.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                verticalFullScreenForMusic(jinyun_bg, false);
                return;
            }
        }
        //使用播放返回键监听
        boolean justLand = PreferenceMgr.getBoolean(getContext(), "justLand", ScreenUtil.isTablet(getContext()));
        boolean finishNow = (boolean) PreferenceMgr.get(getContext(), "ijkplayer", "finishNow", false);
        boolean directBack = PreferenceMgr.getBoolean(getContext(), PreferenceConstant.KEY_DIRECT_BACK, false);
        if (finishNow || justLand || directBack) {
            finish();
            return;
        }
        if (videoPlayerView.getExoFullscreen().isChecked()) {
            // 不加这句返回键无响应
            videoPlayerView.exitFullView();
            return;
        }
        boolean backToPiP = PreferenceMgr.getBoolean(getContext(), PreferenceConstant.KEY_BACK_TO_PIP, false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        // 小窗之后再回来这个判断会成立
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && !player.isPlaying()) {
            if (backToPiP) {
                mPipUtil.enterPiPMode();
            } else {
                // 先判断是不是全屏再 finish，否则暂停视频的时候返回就是 finish 了
                finish();
            }
            return;
        }
        try {
            if (videoPlayerView.isNowVerticalFullScreen()) {
                videoPlayerView.verticalFullScreen();
                if (listCard.getVisibility() == VISIBLE) {
                    reverseListCardVisibility();
                }
                return;
            }
            if (player.onBackPressed()) {
                if (backToPiP && player.isPlaying()) {
                    mPipUtil.enterPiPMode();
                } else {
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void startDlan() {
        //投屏
        if (isMusic) {
            pauseMusic();
        } else {
            player.setStartOrPause(false);
        }
        Map<String, String> headers = getHeaders(HttpParser.getPlayData(url));
        String p = LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(playUrl));
        if (CollectionUtil.isEmpty(DlanListPopUtil.instance().getDevices())) {
            DlanListPopUtil.instance().reInit();
        } else {
            if (DlanListPopUtil.instance().getUsedDevice() != null &&
                    DlanListPopUtil.instance().getDevices().contains(DlanListPopUtil.instance().getUsedDevice())
                    && ClingManager.getInstance().getDeviceManager() != null) {
                ClingManager.getInstance().setSelectedDevice(DlanListPopUtil.instance().getUsedDevice());
                Intent intent1 = new Intent(getContext(), MediaPlayActivity.class);
                intent1.putExtra(DLandataInter.Key.PLAY_TITLE, title);
                intent1.putExtra(DLandataInter.Key.PLAYURL, p);
                intent1.putExtra(DLandataInter.Key.PLAY_POS, 1);
                intent1.putExtra(DLandataInter.Key.HEADER, DlanListPop.genHeaderString(headers));
                startActivity(intent1);
                ToastMgr.shortBottomCenter(getContext(), "已使用常用设备投屏，长按投屏按钮切换设备");
                return;
            }
        }

        dlanListPop = new DlanListPop(this, DlanListPopUtil.instance().getDevices());
        dlanListPop.updateTitleAndUrl(p, title, headers);
        new XPopup.Builder(this)
                .hasNavigationBar(false)
                .isRequestFocus(false)
                .asCustom(dlanListPop)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.custom_mode:
            case R.id.custom_toolbar_desc:
                reverseListCardVisibility();
                break;
            case R.id.exo_pip:
                mPipUtil.enterPiPMode();
                if (!player.isPlaying()) {
                    mPipUtil.initPictureInPictureActions();
                }
                break;
            case R.id.control_back:
                finish();
                break;
            case R.id.custom_dlan:
                String[] dlanTypes = {"传统投屏", "投屏直链"};
                AttachListPopupView popupView = new XPopup.Builder(getContext())
                        .hasNavigationBar(false)
                        .isRequestFocus(false)
                        .atView(v)
                        .asAttachList(dlanTypes,
                                null,
                                (position, text) -> {
                                    switch (text) {
                                        case "传统投屏":
                                            startDlan();
                                            break;
                                        case "投屏直链":
                                            startWebDlan(true, true);
                                            break;
                                        default:
                                            break;
                                    }
                                });
                popupView.show();
                break;
            case R.id.music_list:
            case R.id.chapters:
            case R.id.custom_chapter:
                showChapters();
                break;
            case R.id.custom_lock_screen:
                try {
                    if (!switchUtils.isLocked()) {
                        ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.screen_lock_rotation));
                        ToastMgr.shortBottomCenter(getContext(), "已经锁定重力感应旋转屏幕功能");
                    } else {
                        switchUtils.setTempLocked(false);
                        ((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.screen_rotation));
                        ToastMgr.shortBottomCenter(getContext(), "已经开启重力感应旋转屏幕功能");
                    }
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                switchUtils.reverseLocked(this);
                break;
            case R.id.other:
                if (isMusic) {
                    //后台播放
                    EventBus.getDefault().post(new BackMainEvent());
                    ToastMgr.shortBottomCenter(getContext(), "已后台播放，通过系统多任务界面可以回到当前窗口");
                    break;
                }
                //外部播放
                if (isMusic) {
                    pauseMusic();
                } else {
                    player.setStartOrPause(false);
                }
                PlayData playData = HttpParser.getPlayData(url);
                PlayerChooser.startMultiPlayer(getContext(), title, playUrl, url, getHeaders(playData), playData.getSubtitle(), playerName -> false);
                break;
            case R.id.custom_last:
                nextMovie(true);
                break;
            case R.id.auto_jump:
                try {
                    if (extraDataBundle == null || !extraDataBundle.containsKey("viewCollectionExtraData")) {
                        showJumpSet(null);
                        break;
                    }
                    ViewCollectionExtraData extraData = ViewCollectionExtraData.extraDataFromJson(extraDataBundle.getString("viewCollectionExtraData"));
                    showJumpSet(extraData);
                } catch (Exception ignored) {
                    showJumpSet(null);
                }
                break;
            case R.id.custom_next:
            case R.id.next:
                //下一集
                nextMovie(false);
                if (gridLayoutManager != null) {
                    gridLayoutManager.scrollToPosition(nowPos);
                }
                break;
            case R.id.collect:
                collectVideo();
                break;
            case R.id.copy:
                ClipboardUtil.copyToClipboardForce(getContext(), title + "\n" + playUrl);
                break;
            case R.id.player_x5:
                //嗅探地址切换
                if (videoPlayerView.isShowVideoSwitch()) {
                    showVideoSwitch(false);
                    break;
                }
                showVideoList(false);
                break;
            case R.id.download:
                //下载视频
                if (isMusic) {
                    pauseMusic();
                } else {
                    player.setStartOrPause(false);
                }
                Timber.d("onClick: " + title + "--->" + url);
                DownloadDialogUtil.showEditDialog(VideoPlayerActivity.this, title, appendHeaders(url, playUrl), null, downloadTask -> {
                    PlayData playData1 = HttpParser.getPlayData(url);
                    if (StringUtil.isNotEmpty(playData1.getSubtitle())) {
                        downloadTask.setSubtitle(playData1.getSubtitle());
                    }
                });
                break;
            case R.id.dlan:
                startDlan();
                break;
            case R.id.mode:
                //拉伸模式
                if (isMusic) {
                    verticalFullScreenForMusic(jinyun_bg, true);
                    break;
                }
                handleForScreenMode = true;
                videoPlayerView.verticalFullScreen();
                break;
            case R.id.speed:
                //上一集
                nextMovie(true);
                if (gridLayoutManager != null) {
                    gridLayoutManager.scrollToPosition(nowPos);
                }
                break;
            default:
                break;
        }
    }

    private void collectVideo() {
        if (url.contains(":11111")) {
            ToastMgr.shortCenter(getContext(), "当前地址不支持收藏");
            return;
        }
        List<ViewCollection> collections = null;
        try {
            collections = LitePal.where("CUrl = ? and MTitle = ?", url, title).limit(1).find(ViewCollection.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!CollectionUtil.isEmpty(collections)) {
            List<ViewCollection> finalCollections = collections;
            new XPopup.Builder(getContext())
                    .asConfirm("温馨提示", "检测到已经收藏过该地址，是否删除收藏？", () -> {
                        finalCollections.get(0).delete();
                        ToastMgr.shortBottomCenter(getContext(), "已删除");
                    }).show();
        } else {
            ToastMgr.shortBottomCenter(getContext(), "此功能开发中！");
//            ViewCollection viewCollection = new ViewCollection();
//            viewCollection.setMTitle(title);
//            viewCollection.setCUrl(url);
//            viewCollection.setVideoUrl(url);
//            viewCollection.setMITitle(CollectionTypeConstant.VIDEO_URL);
//            viewCollection.save();
//            ToastMgr.shortBottomCenter(getContext(), "已收藏");
        }
    }

    private void showJumpSet(ViewCollectionExtraData extraData) {
        if (extraData != null && extraData.isCustomJump()) {
            MoreSettingActivity.showJumpPosDialog(getContext(), extraData, (dialog, which) -> {
                jumpStartDuration = extraData.getJumpStartDuration();
                jumpEndDuration = extraData.getJumpEndDuration();
                extraDataBundle.putString("viewCollectionExtraData", ViewCollectionExtraData.extraDataToJson(extraData));
            });
        } else {
            MoreSettingActivity.showJumpPosDialog(getContext(), null, (dialog, which) -> {
                jumpStartDuration = PreferenceMgr.getInt(getContext(), "jumpStartDuration", 0);
                jumpEndDuration = PreferenceMgr.getInt(getContext(), "jumpEndDuration", 0);
            });
        }
    }

    public static List<String> getChapters() {
        if (CollectionUtil.isEmpty(chapters)) {
            return new ArrayList<>();
        }
        List<String> g = Stream.of(chapters).map(VideoChapter::getTitle).toList();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) {
            if (StringUtil.isNotEmpty(g.get(i))) {
                String[] titles = g.get(i).split("-");
                if (g.get(i).endsWith("-") || titles.length < 2 || StringUtil.isEmpty(titles[titles.length - 1])) {
                    continue;
                }
                list.add(titles[titles.length - 1]);
            }
        }
        return list;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void finishParse(DestroyEvent event) {
        if (getActivity() == null || getActivity().isFinishing() || isOnPause) {
            return;
        }
        if ("webkit".equals(event.getMode())) {
            WebkitParser.finishParse(getContext(), event.getUrl(), event.getTicket());
        } else {
            X5WebViewParser.finishParse(getContext(), event.getUrl(), event.getTicket());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void playChapter(PlayChapterEvent event) {
        if (CollectionUtil.isEmpty(chapters)) {
            ToastMgr.shortCenter(getContext(), "当前没有视频列表");
            return;
        }
        if (event.getPosition() == -1) {
            nextMovie(false);
            return;
        }
        List<String> g = Stream.of(chapters).map(VideoChapter::getTitle).toList();
        Map<Integer, Integer> posMap = new HashMap<>();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < g.size(); i++) {
            if (StringUtil.isNotEmpty(g.get(i))) {
                String[] titles = g.get(i).split("-");
                if (g.get(i).endsWith("-") || titles.length < 2 || StringUtil.isEmpty(titles[titles.length - 1])) {
                    continue;
                }
                posMap.put(list.size(), i);
                list.add(titles[titles.length - 1]);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if (i == event.getPosition() && StringUtils.equals(event.getTitle(), list.get(i))) {
                Integer pos = posMap.get(i);
                if (pos != null) {
                    playByPos(pos);
                }
                break;
            }
        }
    }

    private void showChapters() {
        if (CollectionUtil.isEmpty(chapters)) {
            ToastMgr.shortCenter(getContext(), "当前没有资源列表");
            return;
        }
        List<String> g = Stream.of(chapters).map(it -> {
            if (StringUtil.isNotEmpty(it.getMemoryTitle())) {
                return it.getMemoryTitle();
            } else {
                return it.getTitle();
            }
        }).toList();
        Map<Integer, Integer> posMap = new HashMap<>();
        List<String> list = new ArrayList<>();
        int checkedPos = 0;
        for (int i = 0; i < g.size(); i++) {
            if (StringUtil.isNotEmpty(g.get(i))) {
                posMap.put(list.size(), i);
                String txt = DetailUIHelper.getTitleText(g.get(i), false);
                if (chapters.get(i).isUse()) {
                    txt = "‘‘" + txt + "’’";
                    checkedPos = list.size();
                }
                list.add(txt);
            }
        }
        int spanCount = getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? 3 : 5;
        if (spanCount == 5) {
            showChapterRecyclerView(posMap, list, checkedPos);
            return;
        }

        CustomCenterRecyclerViewPopup.ClickListener listener = new CustomCenterRecyclerViewPopup.ClickListener() {
            @Override
            public void click(String text, int position) {
                if (!posMap.containsKey(position)) {
                    ToastMgr.shortBottomCenter(getContext(), "数据出错：custom_chapter, no key: " + position);
                    return;
                }
                Integer pos = posMap.get(position);
                if (pos != null) {
                    playByPos(pos);
                    if (chapters.get(pos).isUse() && gridLayoutManager != null) {
                        gridLayoutManager.scrollToPosition(pos);
                    }
                }
            }

            @Override
            public void onLongClick(String url, int position) {
            }
        };

        int finalCheckedPos = checkedPos;
        CustomBottomRecyclerViewPopup popupView = new CustomBottomRecyclerViewPopup(getContext())
                .withTitle("资源列表")
                .with(list, chapterMenuSpan, listener)
                .withOnCreateCallback(recyclerView -> {
                    if (finalCheckedPos > 3) {
                        recyclerView.scrollToPosition(finalCheckedPos);
                    }
                });
        popupView.withMenu(v -> {
            chapterMenuSpan = chapterMenuSpan == 3 ? 1 : 3;
            popupView.changeSpanCount(chapterMenuSpan);
        });
        chapterPopup = new XPopup.Builder(getContext())
                .hasNavigationBar(false)
                .isRequestFocus(spanCount == 3)
                //挖孔屏问题
                .hasShadowBg(spanCount == 3)
                .asCustom(popupView)
                .show();
    }

    private void openCloseChapterRecyclerView(boolean open) {
        if (listCard.getVisibility() == View.INVISIBLE || listCard.getVisibility() == View.GONE) {
            if (!videoPlayerView.isNowVerticalFullScreen() && this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            if (listScrollView != null) {
                listScrollView.setVisibility(GONE);
            }
            refreshListScrollView(true, true, chapterRecyclerView);
            setListCardTextColor();
            listCard.setVisibility(View.VISIBLE);
            videoPlayerView.getPlaybackControlView().setShowTimeoutMs(0);
            listCard.animate().alpha(1).start();
            listCard.setOnClickListener(v -> openCloseChapterRecyclerView(false));
        } else {
            refreshListScrollView(false, true, chapterRecyclerView);
            videoPlayerView.getPlaybackControlView().setShowTimeoutMs(5000);
        }
    }

    private void showChapterRecyclerView(Map<Integer, Integer> posMap, List<String> list, int checkedPos) {
        if (chapterRecyclerView == null) {
            chapterRecyclerView = listCard.findViewById(R.id.chapterRecyclerView);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            chapterRecyclerView.setLayoutManager(layoutManager);
            ChapterFullAdapter adapter = new ChapterFullAdapter(getContext(), list, new CustomRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (!posMap.containsKey(position)) {
                        ToastMgr.shortBottomCenter(getContext(), "数据出错：custom_chapter, no key: " + position);
                        return;
                    }
                    Integer pos = posMap.get(position);
                    if (pos != null) {
                        playByPos(pos);
                        if (chapters.get(pos).isUse() && gridLayoutManager != null) {
                            gridLayoutManager.scrollToPosition(pos);
                        }
                        openCloseChapterRecyclerView(false);
                    }
                }

                @Override
                public void onLongClick(View view, int position) {
                }
            });
            chapterRecyclerView.setAdapter(adapter);
        } else {
            List<String> data = ((ChapterFullAdapter) chapterRecyclerView.getAdapter()).getList();
            data.clear();
            data.addAll(list);
            chapterRecyclerView.getAdapter().notifyDataSetChanged();
        }
        openCloseChapterRecyclerView(true);
        if (checkedPos > 3) {
            chapterRecyclerView.scrollToPosition(checkedPos);
        }
    }

    private void setPlayPos() {
        dealAutoJumpEnd = false;
        if (jumpStartDuration > 0 && initPlayPos < jumpStartDuration * 1000) {
            initPlayPos = jumpStartDuration * 1000;
            ToastMgr.shortBottomCenter(getContext(), "正在跳过片头");
        }
        position = initPlayPos;
    }

    private void updateUrl(String newUrl) {
        url = UrlDetector.clearTag(newUrl);
    }

    private int getNowUsed() {
        int pos = 0;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).isUse()) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private void nextMovie(boolean last) {
        int pos = getNowUsed();
        if (chapters.isEmpty()) {
            String nextStr = last ? "上" : "下";
            boolean playEndAutoFinish = PreferenceMgr.getBoolean(getContext(), "playEndAutoFinish", false);
            if (playEndAutoFinish) {
                ToastMgr.shortBottomCenter(getContext(), String.format("没有%s一个视频地址，已为您自动回上一级页面", nextStr));
                finish();
                return;
            } else {
                ToastMgr.shortBottomCenter(getContext(), String.format("没有%s一个视频地址", nextStr));
                return;
            }
        }
        if (last) {
            if (pos == 0) {
                ToastMgr.shortBottomCenter(getContext(), "没有上一个资源地址啦！");
                return;
            }
            nowPos = pos - 1;
            playByPos(nowPos, pos, true);
        } else {
//            Log.d(TAG, "nextMovie: " + JSON.toJSONString(chapters));
            if (isMusic) {
                //音乐
                int mode = PreferenceMgr.getInt(getContext(), "ijkplayer", "musicMode", 0);
                if (mode == 0) {
                    //列表循环
                    if (chapters.get(chapters.size() - 1).isUse()) {
                        nowPos = 0;
                    } else {
                        nowPos = pos + 1;
                    }
                    playByPos(nowPos, pos, true);
                } else if (mode == 1) {
                    //随机播放
                    nowPos = RandomUtil.getRandom(0, chapters.size() - 1);
                    position = 0;
                    playByPos(nowPos, pos, false);
                } else {
                    //单曲循环
                    position = 0;
                    playByPos(nowPos, pos, false);
                }
                return;
            }
            if (chapters.get(chapters.size() - 1).isUse()) {
                ToastMgr.shortBottomCenter(getContext(), "没有下一个资源地址啦！");
                return;
            }
            nowPos = pos + 1;
            playByPos(nowPos, pos, true);
        }
    }

    private void playByPos(int position) {
        playByPos(position, getNowUsed(), true);
    }

    private void playByPos(int position, int oldPos, boolean getPosFromMemory) {
        if (CollectionUtil.isEmpty(chapters)) {
            return;
        }
        for (VideoChapter chapter : chapters) {
            if (chapter.isUse()) {
                chapter.setUse(false);
            }
        }
        //如果不在这里选中，那就可能无法在加载中下一集，只能等它自己请求结束
        nowPos = position;
        chapters.get(nowPos).setUse(true);
        title = chapters.get(nowPos).getTitle();
        memoryTitle = chapters.get(nowPos).getMemoryTitle();
        if (isMusic) {
            music_title.setText(title);
            loadMusicImage(chapters.get(nowPos).getPicUrl());
        }
        //cache用一次就自动销毁
        String urlCache = chapters.get(nowPos).getCache();
        if (StringUtil.isNotEmpty(urlCache) && StringUtil.isNotEmpty(urlCache.trim())) {
            updateUrl(urlCache);
        } else if (chapters.get(nowPos).getDownloadRecord() != null) {
            String url1 = LocalServerParser.getRealUrl(getContext(), chapters.get(nowPos).getDownloadRecord());
            updateUrl(url1);
        } else if (chapters.get(nowPos).getTorrentFileInfo() != null) {
            ThunderManager.INSTANCE.playTorrentFile(getContext(), chapters.get(nowPos).getTorrentFileInfo(), (u, name) -> {
                updateUrl(u);
                reStartPlayer(true);
            });
            return;
        } else if (X5WebViewParser.canParse(chapters.get(nowPos).getUrl())) {
            boolean start = X5WebViewParser.parse0(this, chapters.get(nowPos).getUrl(), chapters.get(nowPos).getExtra(), u -> {
                if (u.contains("@lazyRule=")) {
                    String[] lazyRule = chapters.get(nowPos).getOriginalUrl().split("@lazyRule=");
                    if (lazyRule.length >= 2) {
                        dealLazyRuleForPlay(position, oldPos, getPosFromMemory, lazyRule);
                    }
                    return;
                }
                updateUrl(u);
                reStartPlayer(getPosFromMemory);
                if (isMusic && objectAnimator.isPaused()) {
                    objectAnimator.resume();
                }
            });
            if (start) {
                isLoading = true;
                ToastMgr.shortBottomCenter(getContext(), "动态解析规则中，请稍候");
            }
            return;
        } else if (WebkitParser.canParse(chapters.get(nowPos).getUrl())) {
            boolean start = WebkitParser.parse0(this, chapters.get(nowPos).getUrl(), chapters.get(nowPos).getExtra(), u -> {
                if (u.contains("@lazyRule=")) {
                    String[] lazyRule = chapters.get(nowPos).getOriginalUrl().split("@lazyRule=");
                    if (lazyRule.length >= 2) {
                        dealLazyRuleForPlay(position, oldPos, getPosFromMemory, lazyRule);
                    }
                    return;
                }
                updateUrl(u);
                reStartPlayer(getPosFromMemory);
                if (isMusic && objectAnimator.isPaused()) {
                    objectAnimator.resume();
                }
            });
            if (start) {
                isLoading = true;
                ToastMgr.shortBottomCenter(getContext(), "动态解析规则中，请稍候");
            }
            return;
        } else if (ThunderManager.INSTANCE.isFTPOrEd2k(chapters.get(nowPos).getUrl())) {
            ThunderManager.INSTANCE.startParseFTPOrEd2k(getContext(), chapters.get(nowPos).getUrl(), (u, name, arrayList) -> {
                updateUrl(u);
                reStartPlayer(getPosFromMemory);
                if (isMusic && objectAnimator.isPaused()) {
                    objectAnimator.resume();
                }
            });
            isLoading = true;
            ToastMgr.shortBottomCenter(getContext(), "链接解析中，请稍候");
            return;
        } else if (StringUtil.isNotEmpty(chapters.get(nowPos).getCodeAndHeader()) && StringUtil.isNotEmpty(chapters.get(nowPos).getOriginalUrl())) {
            Timber.d("nextMovie: isNotEmpty");
            isLoading = true;
            String[] lazyRule = chapters.get(nowPos).getOriginalUrl().split("@lazyRule=");
            if (lazyRule.length != 2) {
                updateUrl(lazyRule[0]);
                reStartPlayer(getPosFromMemory);
            } else {
                dealLazyRuleForPlay(position, oldPos, getPosFromMemory, lazyRule);
            }
            if (isMusic && objectAnimator.isPaused()) {
                objectAnimator.resume();
            }
            return;
        } else {
            updateUrl(chapters.get(nowPos).getUrl());
        }
        reStartPlayer(getPosFromMemory);
        if (isMusic && objectAnimator.isPaused()) {
            objectAnimator.resume();
        }
    }

    private void dealLazyRuleForPlay(int position, int oldPos, boolean getPosFromMemory, String[] lazyRule) {
        dealLazyRule(lazyRule, chapters.get(nowPos).getCodeAndHeader(), url -> {
            if (url.contains("@lazyRule=")) {
                String[] lazyRule2 = url.split("@lazyRule=");
                if (lazyRule2.length >= 2) {
                    dealLazyRuleForPlay(position, oldPos, getPosFromMemory, lazyRule2);
                    return;
                }
            }
            updateUrl(url);
            reStartPlayer(getPosFromMemory);
            if (isMusic && objectAnimator.isPaused()) {
                objectAnimator.resume();
            }
        }, msg -> {
            if (oldPos >= 0 && oldPos < chapters.size() && chapters.get(oldPos).isUse() && chapters.get(position).isUse()) {
                //失败了还原选中
                chapters.get(position).setUse(false);
                if (chapterAdapter != null) {
                    chapterAdapter.notifyDataSetChanged();
                }
            }
        }, false);
    }

    /**
     * 预加载
     */
    private void preParse() {
        if (isFinishing() || CollectionUtil.isEmpty(chapters)) {
            return;
        }
        if (chapters.get(chapters.size() - 1).isUse()) {
            return;
        }
        if (nowPos >= chapters.size() - 1) {
            return;
        }
        VideoChapter videoChapter = chapters.get(nowPos + 1);
        if (StringUtil.isNotEmpty(videoChapter.getUrlCache())) {
            return;
        }
        if (StringUtil.isNotEmpty(videoChapter.getOriginalUrl())) {
            String uu = videoChapter.getOriginalUrl();
            if (!uu.contains("#pre#") && uu.contains("#noPre#")) {
                //强制忽略预加载
                return;
            }
            if (ThunderManager.INSTANCE.isFTPOrEd2k(uu) || ThunderManager.INSTANCE.isMagnetOrTorrent(uu)) {
                //强制忽略预加载
                return;
            }
        }
        if (videoChapter.getDownloadRecord() != null || videoChapter.getTorrentFileInfo() != null) {
            return;
        }
        if (videoChapter.isCacheLoading()) {
            //在加载中
            return;
        }
        preParseNow(videoChapter, true);
    }

    private void preParseNow(VideoChapter videoChapter, boolean shouldRecheck) {
        Timber.d("开始预加载解析");
        if (X5WebViewParser.canParse(videoChapter.getUrl())) {
            boolean start = X5WebViewParser.parse0(this, videoChapter.getUrl(),
                    videoChapter.getExtra(), u -> {
                        String[] lazyRule2 = u.split("@lazyRule=");
                        if (lazyRule2.length >= 2) {
                            dealLazyRuleForPre(videoChapter, shouldRecheck, lazyRule2);
                            return;
                        }
                        finishPreParse(videoChapter, u, shouldRecheck);
                    });
            if (start) {
                videoChapter.setCacheLoading(true);
            }
        } else if (WebkitParser.canParse(videoChapter.getUrl())) {
            boolean start = WebkitParser.parse0(this, videoChapter.getUrl(),
                    videoChapter.getExtra(), u -> {
                        String[] lazyRule2 = u.split("@lazyRule=");
                        if (lazyRule2.length >= 2) {
                            dealLazyRuleForPre(videoChapter, shouldRecheck, lazyRule2);
                            return;
                        }
                        finishPreParse(videoChapter, u, shouldRecheck);
                    });
            if (start) {
                videoChapter.setCacheLoading(true);
            }
        } else if (StringUtil.isNotEmpty(videoChapter.getCodeAndHeader()) && StringUtil.isNotEmpty(videoChapter.getOriginalUrl())) {
            Timber.d("nextMovie: isNotEmpty");
            String[] lazyRule = videoChapter.getOriginalUrl().split("@lazyRule=");
            if (lazyRule.length != 2) {
                finishPreParse(videoChapter, lazyRule[0], false);
            } else {
                videoChapter.setCacheLoading(true);
                dealLazyRuleForPre(videoChapter, shouldRecheck, lazyRule);
            }
        } else {
            finishPreParse(videoChapter, videoChapter.getUrl(), false);
        }
    }

    private void dealLazyRuleForPre(VideoChapter videoChapter, boolean shouldRecheck, String[] lazyRule) {
        dealLazyRule(lazyRule, videoChapter.getCodeAndHeader(), u -> {
            if (u.contains("@lazyRule=")) {
                String[] lazyRule2 = u.split("@lazyRule=");
                if (lazyRule2.length >= 2) {
                    dealLazyRuleForPre(videoChapter, shouldRecheck, lazyRule2);
                    return;
                }
            }
            finishPreParse(videoChapter, u, shouldRecheck);
        }, null, true);
    }

    private void finishPreParse(VideoChapter videoChapter, String url, boolean shouldRecheck) {
        if (videoChapter.isUse()) {
            Timber.d("结束预加载，已经有结果了");
            return;
        }
        String uu = videoChapter.getOriginalUrl();
        if (StringUtil.isNotEmpty(uu) && uu.contains("#pre#")) {
            //强制使用预加载，那么不再两次校验
            videoChapter.setUrlCache(url);
            Timber.d("结束预加载解析：%s", url);
            return;
        }
        if (StringUtil.isNotEmpty(url) && url.contains("#pre#")) {
            //强制使用预加载，那么不再两次校验
            videoChapter.setUrlCache(url);
            Timber.d("结束预加载解析：%s", url);
            return;
        }
        if (url.startsWith("file://")) {
            //写入本地文件的不再两次校验
            videoChapter.setUrlCache(url);
            Timber.d("结束预加载解析：%s", url);
            return;
        }
        if (shouldRecheck) {
            Timber.d("开始第二次预加载解析：%s", url);
            videoChapter.setCache0(url);
            preParseNow(videoChapter, false);
            return;
        }
        if (StringUtil.isNotEmpty(videoChapter.getCache0()) && !StringUtils.equals(url, videoChapter.getCache0())) {
            Timber.d("结束预加载解析，两次结果不一样：%s, %s", videoChapter.getCache0(), url);
            videoChapter.setUrlCache(" ");
        } else {
            videoChapter.setUrlCache(url);
            Timber.d("结束预加载解析：%s", url);
        }
    }

    private Map<String, String> getHeaders(PlayData playData) {
        return HttpParser.getHeaders(url, playData, switchIndex);
    }

    public void playNow() {
        isLoading = false;
        if (StringUtil.isEmpty(url)) {
            return;
        }
        MusicForegroundService.position = 0;
        String p = HttpParser.getRealUrlFilterHeaders(url);
        PlayData playData = HttpParser.getPlayData(p);
        Map<String, String> headers = getHeaders(playData);
        if (CollectionUtil.isNotEmpty(playData.getUrls())) {
            if (switchIndex >= playData.getUrls().size()) {
                switchIndex = 0;
            }
            playUrl = playData.getUrls().get(switchIndex);
            loadDanmaku(playData);
        } else {
            playUrl = p;
//            loadDanmaku(playData);
        }
        if (chapterAdapter != null) {
            chapterAdapter.notifyDataSetChanged();
        }
        if (isMusic) {
            //音乐模式不再提醒，不然下一曲提醒一次
            videoPlayerView.setNetworkNotify(false);
        }
        video_address_view.setText(("视频地址：" + playUrl));
        TextView tvVideoInfoAddress = expandableVideoInfo.secondLayout.findViewById(R.id.tv_video_info_address);
        tvVideoInfoAddress.setText(("视频地址：" + playUrl));
        boolean isDlanPlaying = EventBus.getDefault().hasSubscriberForEvent(DlanPlayEvent.class);
        if (webDlanPlaying) {
            ToastMgr.shortBottomCenter(getContext(), "已刷新网页投屏链接");
            String videoUrl = LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(playUrl));
            DlanUrlDTO urlDTO = new DlanUrlDTO(videoUrl, headers,
                    jumpStartDuration, jumpEndDuration);
            urlDTO.setTitle(title);
            urlDTO.setDanmu(playData.getDanmu());
            urlDTO.setSubtitle(playData.getSubtitle());
            RemoteServerManager.instance().setUrlDTO(urlDTO);
        }
        if (isDlanPlaying) {
            String videoUrl = LocalServerParser.getRealUrlForRemotedPlay(Application.getContext(), PlayerChooser.getThirdPlaySource(playUrl));
            EventBus.getDefault().post(new DlanPlayEvent(title, videoUrl, DlanListPop.genHeaderString(headers)));
        }
        int playerInt = getIntent().getIntExtra("player", PlayerEnum.PLAYER_TWO.getCode());
        if (playerInt != PlayerEnum.PLAYER_TWO.getCode()) {
            //第三方播放器
            videoPlayerView.setTitle(title);
            tv_show_info.setText(("当前播放：" + title));
            player.bindListener();
            player.startPlayerNoPlay();
            videoPlayerView.showBtnContinueHint(VISIBLE, null);
            String playerName = PlayerEnum.findName(playerInt);
            if (StringUtil.isNotEmpty(playerName)) {
                PlayerChooser.startPlayer(getContext(), playerName, title, playUrl, url, headers, playData.getSubtitle(), null);
            } else {
                ToastMgr.shortBottomCenter(getContext(), "获取播放器失败：" + playerInt);
            }
            memoryLastClick();
            return;
        }
        if (player == null) {
            ToastMgr.shortBottomCenter(getContext(), "播放器未初始化");
            return;
        }
        try {
            player.reset();
            if (CollectionUtil.isNotEmpty(playData.getUrls())) {
                if (switchIndex >= playData.getUrls().size()) {
                    switchIndex = 0;
                }
                if (CollectionUtil.isNotEmpty(playData.getAudioUrls())) {
                    player.setAudioUrls(playData.getAudioUrls());
                }
                player.setPlayUri(switchIndex, CollectionUtil.toStrArray(playData.getUrls()),
                        CollectionUtil.toStrArray(playData.getNames()), headers, playData.getSubtitle());
                playUrl = playData.getUrls().get(switchIndex);
                videoPlayerView.setShowVideoSwitch(true);
                videoPlayerView.setSwitchName(playData.getNames(), switchIndex);
            } else {
                player.setPlayUri(playUrl, HttpParser.getHeaders(url));
                videoPlayerView.setShowVideoSwitch(false);
            }
            player.setPosition(player.getPlayer() != null ? player.getPlayer().getCurrentWindowIndex() : 0, position);
            videoPlayerView.setTitle(title);
            tv_show_info.setText(("当前播放：" + title));
            player.startPlayer();
            addFormatListener();
            memoryLastClick();
            if (webDlanPlaying || isDlanPlaying) {
                if (isMusic) {
                    pauseMusic();
                } else {
                    player.setStartOrPause(false);
                }
            } else {
                player.setStartOrPause(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void memoryLastClickAndSwitchIndex(boolean lastClick) {
        String click = null;
        if (lastClick) {
            int realPos = CollectionUtil.isNotEmpty(chapters) && chapters.size() > nowPos ? chapters.get(nowPos).getRealPos() : 0;
            if (StringUtil.isNotEmpty(memoryTitle)) {
                click = memoryTitle;
            } else {
                click = title.split("-")[title.split("-").length - 1];
            }
            Timber.d("playNow: %s", click);

            String CUrl = getIntent().getStringExtra("CUrl");
            String MTitle = getIntent().getStringExtra("MTitle");
            if (StringUtil.isNotEmpty(CUrl) && StringUtil.isNotEmpty(MTitle)) {
                HistoryMemoryService.INSTANCE.memoryClick(CUrl, MTitle, realPos, click);
            }
        }
    }

    private void memoryLastClick() {
        memoryLastClickAndSwitchIndex(true);
    }

    private int getSwitchIndexFromHis() {
        String CUrl = getIntent().getStringExtra("CUrl");
        String MTitle = getIntent().getStringExtra("MTitle");
        return getSwitchIndex(CUrl, MTitle);
    }

    public static int getSwitchIndex(String CUrl, String MTitle) {
        if (StringUtil.isNotEmpty(CUrl) && StringUtil.isNotEmpty(MTitle)) {
            List<ViewCollection> collections = null;
            try {
                collections = LitePal.where("CUrl = ? and MTitle = ?", CUrl, MTitle).limit(1).find(ViewCollection.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!CollectionUtil.isEmpty(collections)) {
                if (StringUtil.isNotEmpty(collections.get(0).getExtraData())) {
                    ViewCollectionExtraData extraData = ViewCollectionExtraData.extraDataFromJson(collections.get(0).getExtraData());
                    if (extraData != null) {
                        return extraData.getSwitchIndex();
                    }
                }
            }

            List<ViewHistory> histories = null;
            try {
                histories = LitePal.where("url = ? and title = ? and type = ?", CUrl, MTitle, CollectionTypeConstant.DETAIL_LIST_VIEW).limit(1).find(ViewHistory.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!CollectionUtil.isEmpty(histories)) {
                if (StringUtil.isNotEmpty(histories.get(0).getExtraData())) {
                    ViewCollectionExtraData extraData = ViewCollectionExtraData.extraDataFromJson(histories.get(0).getExtraData());
                    if (extraData != null) {
                        return extraData.getSwitchIndex();
                    }
                }
            }
        }
        return 0;
    }

    private String getExtraData(String data) {
        ViewCollectionExtraData extraData = null;
        if (StringUtil.isNotEmpty(data)) {
            extraData = ViewCollectionExtraData.extraDataFromJson(data);
        }
        if (extraData == null) {
            extraData = new ViewCollectionExtraData();
        }
        extraData.setSwitchIndex(switchIndex);
        return JSON.toJSONString(extraData);
    }

    /**
     * 处理动态解析
     *
     * @param lazyRule
     * @param codeAndHeader
     */
    private void dealLazyRule(String[] lazyRule, String codeAndHeader, Consumer<String> consumer, Consumer<String> failConsumer, boolean silence) {
        String myUrl = getIntent().getStringExtra("CUrl") == null ? "" : getIntent().getStringExtra("CUrl");
        LazyRuleParser.parse(this, getRule(), lazyRule, codeAndHeader, myUrl, new BaseParseCallback<String>() {
            @Override
            public void start() {
                if (!silence) {
                    ToastMgr.shortBottomCenter(getContext(), "动态解析规则中，请稍候");
                }
            }

            @Override
            public void success(String data) {
                if (X5WebViewParser.canParse(data)) {
                    boolean start = X5WebViewParser.parse0(getActivity(), data, chapters.get(nowPos).getExtra(), consumer::accept);
                    if (start && !silence) {
                        ToastMgr.shortBottomCenter(getContext(), "动态解析规则中，请稍候");
                    }
                    return;
                } else if (WebkitParser.canParse(data)) {
                    boolean start = WebkitParser.parse0(getActivity(), data, chapters.get(nowPos).getExtra(), consumer::accept);
                    if (start && !silence) {
                        ToastMgr.shortBottomCenter(getContext(), "动态解析规则中，请稍候");
                    }
                    return;
                }
                consumer.accept(data);
            }

            @Override
            public void error(String msg) {
                if (failConsumer != null) {
                    failConsumer.accept(msg);
                }
                if (!silence) {
                    ToastMgr.shortBottomCenter(getContext(), "出错：" + msg);
                }
            }
        });
    }


    private void reverseListCardVisibility() {
        if (listCard.getVisibility() == View.INVISIBLE || listCard.getVisibility() == View.GONE) {
            if (!videoPlayerView.isNowVerticalFullScreen() && this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            if (chapterRecyclerView != null) {
                chapterRecyclerView.setVisibility(GONE);
            }
            refreshListScrollView(true, false, listScrollView);
            setListCardTextColor();
            listCard.setVisibility(View.VISIBLE);
            videoPlayerView.getPlaybackControlView().setShowTimeoutMs(0);
            listCard.animate().alpha(1).start();
            listCard.setOnClickListener(v -> reverseListCardVisibility());
        } else {
            refreshListScrollView(false, false, listScrollView);
            videoPlayerView.getPlaybackControlView().setShowTimeoutMs(5000);
        }
    }

    private void setListCardTextColor() {
        int white = getResources().getColor(R.color.white);
        int green = getResources().getColor(R.color.greenAction);
        TextView mode_fit = listCard.findViewById(R.id.mode_fit);
        mode_fit.setTextColor(white);
        TextView mode_fill = listCard.findViewById(R.id.mode_fill);
        mode_fill.setTextColor(white);
        TextView mode_fixed_width = listCard.findViewById(R.id.mode_fixed_width);
        mode_fixed_width.setTextColor(white);
        TextView mode_fixed_height = listCard.findViewById(R.id.mode_fixed_height);
        mode_fixed_height.setTextColor(white);
        TextView speed_1 = listCard.findViewById(R.id.speed_1);
        speed_1.setTextColor(white);
        TextView speed_1_2 = listCard.findViewById(R.id.speed_1_2);
        speed_1_2.setTextColor(white);
        TextView speed_1_5 = listCard.findViewById(R.id.speed_1_5);
        speed_1_5.setTextColor(white);
        TextView speed_2 = listCard.findViewById(R.id.speed_2);
        speed_2.setTextColor(white);
        TextView speed_p8 = listCard.findViewById(R.id.speed_p8);
        speed_p8.setTextColor(white);
        TextView speed_p5 = listCard.findViewById(R.id.speed_p5);
        speed_p5.setTextColor(white);

        TextView speed_3 = listCard.findViewById(R.id.speed_3);
        speed_3.setTextColor(white);
        TextView speed_4 = listCard.findViewById(R.id.speed_4);
        speed_4.setTextColor(white);
        TextView speed_5 = listCard.findViewById(R.id.speed_5);
        speed_5.setTextColor(white);
        TextView speed_6 = listCard.findViewById(R.id.speed_6);
        speed_6.setTextColor(white);
        switch (videoPlayerView.getPlayerView().getResizeMode()) {
            case RESIZE_MODE_FIT:
                mode_fit.setTextColor(green);
                break;
            case RESIZE_MODE_FILL:
                mode_fill.setTextColor(green);
                break;
            case RESIZE_MODE_FIXED_WIDTH:
                mode_fixed_width.setTextColor(green);
                break;
            case RESIZE_MODE_FIXED_HEIGHT:
                mode_fixed_height.setTextColor(green);
                break;
        }
        switch ((int) (VideoPlayerManager.PLAY_SPEED * 10)) {
            case 10:
                speed_1.setTextColor(green);
                break;
            case 12:
                speed_1_2.setTextColor(green);
                break;
            case 15:
                speed_1_5.setTextColor(green);
                break;
            case 20:
                speed_2.setTextColor(green);
                break;
            case 8:
                speed_p8.setTextColor(green);
                break;
            case 5:
                speed_p5.setTextColor(green);
                break;
            case 30:
                speed_3.setTextColor(green);
                break;
            case 40:
                speed_4.setTextColor(green);
                break;
            case 50:
                speed_5.setTextColor(green);
                break;
            case 60:
                speed_6.setTextColor(green);
                break;
        }
    }

    private void reStartPlayer(boolean reGetPos) {
        if (StringUtil.isEmpty(url)) {
            return;
        }
        if (reGetPos) {
            initPlayPos = HeavyTaskUtil.getPlayerPos(getContext(), getMemoryId());
            setPlayPos();
        }
        playNow();
    }

    private void playFromSpeed(float speed) {
        VideoPlayerManager.PLAY_SPEED = speed;
        boolean memoryPlaySpeed = PreferenceMgr.getBoolean(getContext(), SETTING_CONFIG, "memoryPlaySpeed", false);
        if (memoryPlaySpeed) {
            PreferenceMgr.put(getContext(), "ijkplayer", "playSpeed", VideoPlayerManager.PLAY_SPEED);
        }
        player.setPlaybackParameters(speed, 1f);
        descView.setText(("速度×" + VideoPlayerManager.PLAY_SPEED + "/" + descView.getText().toString().split("/")[1]));
    }


    /**
     * 切换应用时进入 PiP
     * <p>
     * TODO Bug: 华为、小米不调用该方法
     * Reason: 实测三大金刚键回桌面会调用，但是全面屏手势回桌面则不调用
     */
    @Override
    public void onUserLeaveHint() {
        mPipUtil.onUserLeaveHint();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        mPipUtil.setInPIPMode(isInPictureInPictureMode);
        videoPlayerView.setPipMode(mPipUtil.isInPIPMode());
        if (isInPictureInPictureMode) {
            videoPlayerView.getPlayerView().hideController();
            videoPlayerView.getPlayerView().setControllerAutoShow(false);
            //MIUI部分情况下可能先调了onConfigurationChanged，然后再触发此方法，因此再次check保证一定全屏播放
            videoPlayerView.toLandLayout();
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            sVController.setVisibility(GONE);
            // Starts receiving events from action items in PiP mode.
            mPipUtil.registerPipReceiver();
        } else {
            TaskUtil.setExcludeFromRecentTasks(getContext(), getClass(), false);
            if (!videoPlayerView.isLand() && videoPlayerView.isLandLayout()) {
                //恢复竖屏
                videoPlayerView.toPortraitLayout();
            }
            // Restore the full-screen UI.
            sVController.setVisibility(VISIBLE);
            videoPlayerView.getPlayerView().setControllerAutoShow(true);
            // We are out of PiP mode. We can stop receiving events from it.
            mPipUtil.unregisterPipReceiver();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 304 && resultCode == 304) {
            title = data.getStringExtra("title");
            updateUrl(data.getStringExtra("videourl"));
            reStartPlayer(true);
            if (!player.isPlaying()) {
                player.setStartOrPause(true);
            }
        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = UriUtils.getRootDir(getContext()) + File.separator + "subtitle" + File.separator + UriUtils.getFileName(uri);
            new File(UriUtils.getRootDir(getContext()) + File.separator + "subtitle").mkdirs();
            UriUtils.getFilePathFromURI(getContext(), uri, path, new UriUtils.LoadListener() {
                @Override
                public void success(String s) {
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            String realUrl = HttpParser.getRealUrlFilterHeaders(url);
                            PlayData playData = HttpParser.getPlayData(realUrl);
                            playData.setSubtitle("file://" + s);
                            if (CollectionUtil.isEmpty(playData.getUrls())) {
                                playData.setUrls(new ArrayList<>(Collections.singletonList(playData.getUrl())));
                            }
                            String[] sk = url.split(";");
                            url = JSON.toJSONString(playData) + StringUtil.arrayToString(sk, 1, ";");
                            reStartPlayer(true);
                            ToastMgr.shortBottomCenter(getContext(), "已加载字幕文件");
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        if (isMusic && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            //是新开的窗口
//            new XPopup.Builder(getContext())
//                    .asConfirm("温馨提示", "当前正在播放音频，请选择停止播放音频还是后台播放", "后台播放", "停止播放", () -> {
//                        EventBus.getDefault().post(new BackMainEvent());
//                        super.finishAndRemoveTask();
//                    }, () -> {
//                        EventBus.getDefault().post(new BackMainEvent());
//                    }, false).show();
            EventBus.getDefault().post(new BackMainEvent());
            super.finishAndRemoveTask();
            return;
        }
        if (mPipUtil != null && !mPipUtil.finished(this)) {
            super.finish();
        }
    }

    private Object getRule() {
        if (extraDataBundle != null && extraDataBundle.containsKey("rule")) {
            String r = extraDataBundle.getString("rule");
            if (StringUtil.isNotEmpty(r)) {
                return JSON.parseObject(r, ArticleListRule.class);
            }
        } else if (extraDataBundle != null) {
            String r = DataTransferUtils.INSTANCE.loadCacheString("tempVideoRule");
            if (StringUtil.isNotEmpty(r)) {
                return JSON.parseObject(r, ArticleListRule.class);
            }
        }
        return new JSONObject();
    }
}

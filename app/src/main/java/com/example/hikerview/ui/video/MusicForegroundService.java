package com.example.hikerview.ui.video;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.hikerview.R;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.music.HeadsetButtonReceiver;
import com.example.hikerview.ui.video.event.MusicAction;
import com.example.hikerview.ui.video.event.MusicInfo;
import com.example.hikerview.utils.BitmapFillet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * 作者：By 15968
 * 日期：On 2019/12/4
 * 时间：At 23:01
 */
public class MusicForegroundService extends Service {
    private static final int ONGOING_NOTIFICATION_ID = 3;
    /**
     * 歌曲播放
     */
    public static final String PLAY = "play";
    /**
     * 歌曲暂停
     */
    public static final String PAUSE = "pause";
    /**
     * 歌曲暂停
     */
    public static final String PAUSE_NOW = "pause_now";
    /**
     * 上一曲
     */
    public static final String PREV = "prev";
    /**
     * 下一曲
     */
    public static final String NEXT = "next";
    /**
     * 关闭通知栏
     */
    public static final String CLOSE = "close";

    /**
     * 用于判断当前滑动歌名改变的通知栏播放状态
     */
    public static final String IS_CHANGE = "isChange";

    /**
     * 通知
     */
    private NotificationCompat.Builder notification;
    /**
     * 通知管理器
     */
    private static NotificationManager manager;
    /**
     * 音乐广播接收器
     */
    private MusicReceiver musicReceiver;
    private String channelId;
    public static MusicInfo info;
    private Bitmap bitmap;
    private MediaSessionCompat mediaSessionCompat;
    public static long position = 0;

    public MusicForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        channelId = "海阔视界播放提示";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        }
        //初始化RemoteViews配置
        RemoteViews remoteViews = initRemoteViews();
        registerMusicReceiver();

        Intent intent = new Intent(this, EmptyActivity.class);
        notification = new NotificationCompat.Builder(Application.application, channelId)
                .setContentTitle("嗅觉浏览器·音乐播放")
//                .setContentText("请勿清理后台，否则会导致音乐播放中断")
                .setSmallIcon(R.drawable.ic_stat_launcher)
                .setCustomContentView(remoteViews)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(false)
                .setContentIntent(PendingIntent.getActivity(this, 100, intent,
                        FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        startForeground(ONGOING_NOTIFICATION_ID, notification.build());

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        initMediaSession();
        upMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING);
    }

    private void upMediaSessionPlaybackState(int state) {
        mediaSessionCompat.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(HeadsetButtonReceiver.MEDIA_SESSION_ACTIONS)
                        .setState(state, position, 1f)
                        .build()
        );
    }

    private void initMediaSession() {
        mediaSessionCompat = new MediaSessionCompat(this, "hiker-readAloud");
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return HeadsetButtonReceiver.Companion.handleIntent(mediaButtonEvent);
            }
        });
        Intent intent = new Intent(this, HeadsetButtonReceiver.class);
        intent.setAction(Intent.ACTION_MEDIA_BUTTON);
        mediaSessionCompat.setMediaButtonReceiver(PendingIntent.getBroadcast(this, 0, intent, FLAG_UPDATE_CURRENT));
        mediaSessionCompat.setActive(true);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(){
        String channelId = "嗅觉浏览器播放提示";
        String channelName = "前台音乐播放通知";
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setImportance(NotificationManager.IMPORTANCE_HIGH);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        if (manager != null) {
            manager.createNotificationChannel(chan);
        }
        return channelId;
    }

    /**
     * 暂停/继续 音乐
     */
    public void pauseOrContinueMusic() {
        EventBus.getDefault().post(new MusicAction(PAUSE));
    }

    /**
     * 关闭音乐
     */
    public void closeMusic() {
        EventBus.getDefault().post(new MusicAction(CLOSE));
    }

    /**
     * 关闭音乐通知栏
     */
    public void closeNotification() {
        manager.cancel(ONGOING_NOTIFICATION_ID);
    }

    /**
     * 下一首
     */
    public void nextMusic() {
        EventBus.getDefault().post(new MusicAction(NEXT));
    }

    /**
     * 上一首
     */
    public void previousMusic() {
        EventBus.getDefault().post(new MusicAction(PREV));
    }

    /**
     * 初始化自定义通知栏 的按钮点击事件
     */
    private RemoteViews initRemoteViews() {
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.notification);

        //通知栏控制器上一首按钮广播操作
        Intent intentPrev = new Intent(PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, intentPrev, 0);
        //为prev控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_previous, prevPendingIntent);

        //通知栏控制器播放暂停按钮广播操作  //用于接收广播时过滤意图信息
        Intent intentPlay = new Intent(PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
        //为play控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_play, playPendingIntent);

        //通知栏控制器下一首按钮广播操作
        Intent intentNext = new Intent(NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, intentNext, 0);
        //为next控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_next, nextPendingIntent);

        //通知栏控制器关闭按钮广播操作
        Intent intentClose = new Intent(CLOSE);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(this, 0, intentClose, 0);
        //为close控件注册事件
        remoteViews.setOnClickPendingIntent(R.id.btn_notification_close, closePendingIntent);
        return remoteViews;
    }

    /**
     * 更改通知的信息和UI
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void updateNotificationShow(MusicInfo musicInfo) {
        info = new MusicInfo(musicInfo.getTitle(), null, musicInfo.isPause());

        RemoteViews remoteViews = initRemoteViews();
        //播放状态判断
        if (!musicInfo.isPause()) {
            upMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            remoteViews.setImageViewResource(R.id.btn_notification_play, R.drawable.ic_stat_pause);
        } else {
            upMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            remoteViews.setImageViewResource(R.id.btn_notification_play, R.drawable.ic_stat_play);
        }
        //封面专辑
        if (musicInfo.getBitmap() != null) {
            Bitmap b = BitmapFillet.centerSquareScaleBitmap(musicInfo.getBitmap());
            int px = b.getWidth() / 8;
            bitmap = BitmapFillet.fillet(b, px, BitmapFillet.CORNER_ALL);
        } else {
            if (bitmap == null) {
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_show);
                int px = b.getWidth() / 8;

                bitmap = BitmapFillet.fillet(b, px, BitmapFillet.CORNER_ALL);
            }
        }
        remoteViews.setImageViewBitmap(R.id.iv_album_cover, bitmap);

        //歌曲名
        remoteViews.setTextViewText(R.id.tv_notification_song_name, musicInfo.getTitle());
        // remoteViews.setTextColor();

        notification.setCustomContentView(remoteViews);

        //发送通知
        manager.notify(ONGOING_NOTIFICATION_ID, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        manager.cancel(ONGOING_NOTIFICATION_ID);
        upMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        super.onDestroy();
        if (musicReceiver != null) {
            //解除动态注册的广播
            unregisterReceiver(musicReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 注册动态广播
     */
    private void registerMusicReceiver() {
        musicReceiver = new MusicReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAY);
        intentFilter.addAction(PREV);
        intentFilter.addAction(NEXT);
        intentFilter.addAction(CLOSE);
        registerReceiver(musicReceiver, intentFilter);
    }

    /**
     * 广播接收器 （内部类）
     */
    public class MusicReceiver extends BroadcastReceiver {

        public static final String TAG = "MusicReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            //UI控制
            switch (intent.getAction()) {
                case PLAY:
                    //暂停或继续
                    pauseOrContinueMusic();
                    break;
                case PREV:
                    previousMusic();
                    break;
                case NEXT:
                    nextMusic();
                    break;
                case CLOSE:
                    closeMusic();
                    break;
                default:
                    break;
            }
        }
    }
}

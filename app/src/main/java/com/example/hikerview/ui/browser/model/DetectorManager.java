package com.example.hikerview.ui.browser.model;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.constants.Media;
import com.example.hikerview.constants.MediaType;
import com.example.hikerview.event.FindVideoEvent;
import com.example.hikerview.model.BigTextDO;
import com.example.hikerview.model.XiuTanFavor;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作者：By hdy
 * 日期：On 2018/8/25
 * 时间：At 22:19
 */

public class DetectorManager implements VideoDetector {
    private static final String TAG = "DetectorManager";
    private volatile static DetectorManager sInstance;
    private Set<String> taskUrlsSet = new HashSet<>();
    private List<String> xiuTanDialogBlackList = Collections.synchronizedList(new ArrayList<>());
    private Map<String, Boolean> xiuTanDialogBlackMap;
    private Map<String, String> xiuTanLiked = null;
    private List<DetectedMediaResult> detectedMediaResults = Collections.synchronizedList(new ArrayList<>());

    public Integer getVideoCount() {
        return videoCount.get();
    }

    private AtomicInteger videoCount = new AtomicInteger(0);
    private AtomicInteger videoLimit = new AtomicInteger(20);

    private DetectorManager() {
        xiuTanDialogBlackList.addAll(initXiuTanList(BigTextDO.xiuTanDialogBlackListPath));
        xiuTanDialogBlackMap = initXiuTanMap(xiuTanDialogBlackList);
        initXiuTanLiked();
    }

    public static DetectorManager getInstance() {
        if (sInstance == null) {
            synchronized (DetectorManager.class) {
                if (sInstance == null) {
                    sInstance = new DetectorManager();
                }
            }
        }
        return sInstance;
    }

    public List<String> getXiuTanDialogBlackList() {
        return xiuTanDialogBlackList;
    }

    public boolean inXiuTanDialogBlackList(String url) {
        String dom = StringUtil.getDom(url);
        return xiuTanDialogBlackMap.containsKey(dom) && xiuTanDialogBlackMap.get(dom);
    }

    public boolean inXiuTanLikedBlackList(String url) {
        String dom = StringUtil.getDom(url);
        return SettingConfig.xiuTanLikedBlackListDoms.contains(dom);
    }

    public synchronized int addXTDialogBlackList(List<String> urls) {
        if (CollectionUtil.isEmpty(urls)) {
            return 0;
        }
        Set<String> blackList = new HashSet<>(xiuTanDialogBlackList);
        int count = 0;
        for (int i = 0; i < urls.size(); i++) {
            String dom = StringUtil.getDom(urls.get(i));
            if (TextUtils.isEmpty(dom)) {
                continue;
            }
            if (!blackList.contains(dom)) {
                count++;
                blackList.add(dom);
            }
            xiuTanDialogBlackMap.put(dom, true);
        }
        xiuTanDialogBlackList.clear();
        xiuTanDialogBlackList.addAll(blackList);
        saveXiuTanDialogBlackList();
        return count;
    }

    private synchronized void saveXiuTanDialogBlackList() {
        BigTextDO bigTextDO = LitePal.where("key = ?", BigTextDO.xiuTanDialogBlackListPath).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(BigTextDO.xiuTanDialogBlackListPath);
        }
        bigTextDO.setValue(JSON.toJSONString(xiuTanDialogBlackList));
        bigTextDO.save();
    }

    public synchronized void addOrDeleteFormXiuTanDialogList(String url) {
        String dom = StringUtil.getDom(url);
        if (TextUtils.isEmpty(dom)) {
            return;
        }
        boolean exist = false;
        for (int i = 0; i < xiuTanDialogBlackList.size(); i++) {
            if (dom.equals(xiuTanDialogBlackList.get(i))) {
                xiuTanDialogBlackList.remove(i);
                xiuTanDialogBlackMap.put(dom, false);
                exist = true;
                break;
            }
        }
        if (!exist) {
            xiuTanDialogBlackList.add(dom);
            xiuTanDialogBlackMap.put(dom, true);
        }
        saveXiuTanDialogBlackList();
    }

    private Map<String, Boolean> initXiuTanMap(List<String> list) {
        Map<String, Boolean> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.put(list.get(i), true);
        }
        return map;
    }

    private List<String> initXiuTanList(String path) {
        BigTextDO bigTextDO = LitePal.where("key = ?", path).findFirst(BigTextDO.class);
        if (bigTextDO != null) {
            return JSON.parseArray(bigTextDO.getValue(), String.class);
        }
        return new ArrayList<>();
    }


    public synchronized int addFastPlayList(Context context, List<String> urls) {
        if (CollectionUtil.isEmpty(urls)) {
            return 0;
        }
        urls = Stream.of(urls).map(StringUtil::getDom).collect(Collectors.toList());
        int count = urls.size();
        SettingConfig.addFastPlayDom(context, urls.toArray(new String[0]));
        return count;
    }

    public synchronized void addOrDeleteFormXiuTanFastPlayList(Context context, String url) {
        String dom = StringUtil.getDom(url);
        if (TextUtils.isEmpty(dom)) {
            return;
        }
        if (SettingConfig.fastPlayDoms.contains(dom)) {
            SettingConfig.removeFastPlayDom(context, dom);
        } else if (SettingConfig.fastPlayDoms.contains(url)) {
            SettingConfig.removeFastPlayDom(context, url);
        } else {
            SettingConfig.addFastPlayDom(context, dom);
        }
    }

    public boolean inXiuTanLiked(Context context, String dom, String url) {
        initXiuTanLiked();
        if (SettingConfig.fastPlayDoms.contains(dom)) {
            return true;
        }
        return !SettingConfig.xiuTanLikedBlackListDoms.contains(dom) && xiuTanLiked.containsKey(dom) && xiuTanLiked.get(dom).equals(url);
    }


    private void initXiuTanLiked() {
        if (xiuTanLiked != null) {
            return;
        }
        xiuTanLiked = new HashMap<>();
        List<XiuTanFavor> liked = XiuTanModel.getXiuTanLiked();
        for (int i = 0; i < liked.size(); i++) {
            xiuTanLiked.put(liked.get(i).getDom(), liked.get(i).getUrl());
        }
    }

    @Override
    public void putIntoXiuTanLiked(Context context, String dom, String url) {
        initXiuTanLiked();
        xiuTanLiked.put(dom, url);
        XiuTanModel.saveXiuTanLiked(context, dom, url);
    }

    @Override
    public void addTask(VideoTask video) {
        if (video == null) {
            return;
        }
        if (taskUrlsSet.contains(video.getUrl())) {
            return;
        }
        String[] urls = video.getUrl().split("url=");
        if (urls.length > 1 && urls[1].startsWith("http")) {
            addTask(new VideoTask(video.getRequestHeaders(), video.getMethod(), video.getTitle(), HttpParser.decodeUrl(urls[1], "UTF-8")));
        }
        taskUrlsSet.add(video.getUrl());
        video.setTimestamp(System.currentTimeMillis());
        HeavyTaskUtil.executeNewTask(new MyRunnable(video));
    }

    @Override
    public synchronized void startDetect() {
        taskUrlsSet.clear();
        detectedMediaResults.clear();
        videoCount.set(0);
        videoLimit.set(20);
    }

    @Override
    public synchronized void reset(){
        videoLimit.getAndAdd(20);
    }

    public synchronized void setDetectedMediaResults(List<DetectedMediaResult> results) {
        detectedMediaResults.clear();
        if (CollectionUtil.isEmpty(results)) {
            videoCount.set(0);
            return;
        }
        detectedMediaResults.addAll(results);
        int count = 0;
        for (DetectedMediaResult result : results) {
            if (result.getMediaType().getName().equals(MediaType.VIDEO.getName())
                    || result.getMediaType().getName().equals(MediaType.MUSIC.getName())) {
                count++;
            }
        }
        videoCount.set(count);
    }

    public synchronized void createThread() {

    }

    public void destroyDetector() {
        synchronized (DetectorManager.this) {
            detectedMediaResults.clear();
            taskUrlsSet.clear();
        }
    }

    public void addMediaResult(DetectedMediaResult mediaResult) {
        detectedMediaResults.add(mediaResult);
    }

    @Override
    public List<DetectedMediaResult> getDetectedMediaResults(MediaType mediaType) {
        return getDetectedMediaResults(new ArrayList<>(detectedMediaResults), new Media(mediaType));
    }

    public List<DetectedMediaResult> getDetectedMediaResults(Media mediaType) {
        return getDetectedMediaResults(new ArrayList<>(detectedMediaResults), mediaType);
    }

    public static List<DetectedMediaResult> getDetectedMediaResults(List<DetectedMediaResult> detectedMediaResults, Media mediaType) {
        List<DetectedMediaResult> results = new ArrayList<>();
        if (mediaType == null) {
            results.addAll(detectedMediaResults);
        } else {
            for (DetectedMediaResult result : detectedMediaResults) {
                if (mediaType.getName().equals(MediaType.VIDEO_MUSIC.getName())) {
                    if (result.getMediaType().getName().equals(MediaType.VIDEO.getName())
                            || result.getMediaType().getName().equals(MediaType.MUSIC.getName())) {
                        results.add(result);
                    }
                } else if (result.getMediaType().getName().equals(mediaType.getName())) {
                    results.add(result);
                }
            }
        }
        Collections.sort(results, (o1, o2) -> (int) (o1.getTimestamp() - o2.getTimestamp()));
        return results;
    }

    class MyRunnable implements Runnable {
        private VideoTask web;

        MyRunnable(VideoTask web) {
            this.web = web;
        }

        @Override
        public void run() {
            if (web != null) {
                try {
                    DetectedMediaResult mediaResult = new DetectedMediaResult(web.getUrl());
                    mediaResult.setMediaType(UrlDetector.getMediaType(web.getUrl(), web.getRequestHeaders(), web.getMethod()));
                    mediaResult.setTimestamp(web.getTimestamp());
                    String mediaName = mediaResult.getMediaType().getName();
                    detectedMediaResults.add(mediaResult);
                    //只有视频或者音乐才发通知，不然可能会阻塞主线程
                    if ((mediaName.equals(MediaType.VIDEO.getName()) || mediaName.equals(MediaType.MUSIC.getName())) && videoCount.get() < videoLimit.get()) {
                        videoCount.addAndGet(1);
                        FindVideoEvent event = new FindVideoEvent(videoCount.get() + "", mediaResult);
                        EventBus.getDefault().post(event);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

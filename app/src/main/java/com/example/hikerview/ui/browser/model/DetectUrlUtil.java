package com.example.hikerview.ui.browser.model;

import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.browser.util.HttpRequestUtil;
import com.example.hikerview.ui.browser.util.M3U8Util;
import com.example.hikerview.ui.browser.util.UUIDUtil;
import com.example.hikerview.ui.browser.util.VideoFormatUtil;
import com.example.hikerview.utils.StringUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 作者：By hdy
 * 日期：On 2018/11/1
 * 时间：At 13:23
 */
public class DetectUrlUtil {
    public static volatile List<String> filters = CollectionUtil.asList(".css", ".html", ".js", ".ttf", ".ico", ".png", ".jpg", ".jpeg", ".cnzz");
    public static volatile List<String> images = CollectionUtil.asList("mp4", "m3u8", ".flv", ".avi", ".3gp", "mpeg", ".wmv", ".mov", "rmvb", ".dat", "qqBFdownload", ".mp3", ".wav", ".ogg", ".flac", ".m4a");

    public static String getNeedCheckUrl(String url) {
        url = url.replace("http://", "").replace("https://", "");
        String[] urls = url.split("/");
        if (urls.length > 1) {
            //去掉域名
            return StringUtil.listToString(Arrays.asList(urls), 1, "/");
        } else if ((urls[0] + "/").equals(url)) {
            return "";
        }
        return url;
    }

    public static VideoInfo detectVideoComplex(String title, String url, Map<String, String> requestHeaderMap) throws IOException {
        HttpRequestUtil.HeadRequestResponse headRequestResponse = HttpRequestUtil.performHeadRequest(url, requestHeaderMap);
        if (headRequestResponse == null) {
            return null;
        }
        url = headRequestResponse.getRealUrl();
        Map<String, List<String>> map = headRequestResponse.getHeaderMap();
        Set<String> keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        Map<String, String> headerMap = new HashMap<>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            headerMap.put(key, map.get(key).toString());
        }
        if (!headerMap.containsKey("Content-Type")) {
            //检测失败，未找到Content-Type
            return null;
        }
        VideoFormat videoFormat = VideoFormatUtil.detectVideoFormat(url, headerMap.get("Content-Type"));
        if (videoFormat == null) {
            //检测成功，不是视频
            return null;
        }
        VideoInfo videoInfo = new VideoInfo();
        if ("player/m3u8".equals(videoFormat.getName())) {
            double duration = M3U8Util.figureM3U8Duration(url);
            if (duration <= 0) {
                //检测成功，不是m3u8的视频
                return null;
            }
            videoInfo.setDetectImageType("m3u8");
            videoInfo.setDuration(duration);
        } else {
            long size = 0;
            if (headerMap.containsKey("Content-Length")) {
                try {
                    size = Long.parseLong(headerMap.get("Content-Length"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            videoInfo.setDetectImageType(size / 1024 / 1024 + "MB");
            videoInfo.setSize(size);
        }
        videoInfo.setUrl(url);
        videoInfo.setFileName(UUIDUtil.genUUID());
        videoInfo.setVideoFormat(videoFormat);
        videoInfo.setSourcePageTitle(title);
        videoInfo.setSourcePageUrl(url);
        return videoInfo;
    }

    public interface DetectListener {
        void onSuccess(VideoInfo videoInfo);
    }
}

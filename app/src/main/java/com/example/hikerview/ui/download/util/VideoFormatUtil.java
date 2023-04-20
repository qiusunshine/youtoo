package com.example.hikerview.ui.download.util;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.hikerview.ui.download.DownloadManager;
import com.example.hikerview.ui.download.VideoFormat;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.HttpUtil;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by xm on 17-8-16.
 */
public class VideoFormatUtil {
    private static final String TAG = "VideoFormatUtil";
    private static final List<String> videoExtensionList = Arrays.asList(
            "player/m3u8", "mp4", "ts", "mp3", "m4a", "flv", "mpeg"
    );

    public static final List<VideoFormat> videoFormatList = Arrays.asList(
            new VideoFormat("player/m3u8", Arrays.asList("application/octet-stream", "application/vnd.apple.mpegurl", "application/mpegurl", "application/x-mpegurl", "audio/mpegurl", "audio/x-mpegurl", "application/x-mpeg")),
            new VideoFormat("mp4", Arrays.asList("video/mp4", "application/mp4", "video/h264")),
            new VideoFormat("ts", Arrays.asList("video/vnd.dlna.mpeg-tts", "video/mpeg")),
            new VideoFormat("flv", Arrays.asList("video/x-flv")),
            new VideoFormat("f4v", Arrays.asList("video/x-f4v")),
            new VideoFormat("mpeg", Arrays.asList("video/vnd.mpegurl"))
    );

    public static boolean containsVideoExtension(String url) {
        for (String videoExtension : videoExtensionList) {
            if (!TextUtils.isEmpty(url)) {
                if (url.contains(videoExtension)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 带常见类型校验
     *
     * @param title
     * @param url
     * @return
     */
    public static VideoFormat getVideoFormat(@Nullable String title, String url) {
        Set<String> types = ShareUtil.getExtensions();
        if (title != null && title.contains(".") && !title.contains(".m3u8")) {
            String ext = FileUtil.getExtension(title);
            if (StringUtil.isNotEmpty(ext) && ext.length() < 10 && types.contains(ext)) {
                return new VideoFormat(ext, Collections.singletonList(ext));
            }
        }
        String fileName = FileUtil.getResourceName(url);
        if (StringUtil.isNotEmpty(fileName) && fileName.contains(".")) {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (StringUtil.isNotEmpty(extension) && types.contains(extension)) {
                return new VideoFormat(extension, Collections.singletonList(extension));
            }
        }
        return null;
    }

    /**
     * 忽略格式校验
     *
     * @param url
     * @return
     */
    public static VideoFormat getVideoFormatAnyway(String url) {
        String fileName = FileUtil.getResourceName(url);
        if (StringUtil.isNotEmpty(fileName) && fileName.contains(".")) {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (StringUtil.isNotEmpty(extension) && extension.length() < 10) {
                try {
                    Integer.parseInt(extension);
                    if (!url.contains(".apk.")) {
                        //纯数字，肯定不是文件后缀，排除微信改apk为apk.1的恶心操作
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new VideoFormat(extension, Collections.singletonList(extension));
            }
        }
        return null;
    }

    private static VideoFormat getVideoFormatByName(Map<String, List<String>> headerMap, @Nullable String title, String url) {
        try {
            String fileName = null;
            if (headerMap != null && headerMap.containsKey("Content-Disposition")) {
                fileName = DownloadManager.getDispositionFileName(headerMap.get("Content-Disposition").get(0));
            }
            //先检查Content-Disposition和title
            VideoFormat format = getVideoFormat(fileName, title);
            if (format != null) {
                return format;
            }
            //最后再用url
            return getVideoFormat(null, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static VideoFormat detectVideoFormat(@Nullable String title, String url, Map<String, List<String>> headerMap) {
        List<String> cts = headerMap.get("Content-Type");
        if (cts == null || cts.isEmpty()) {
            return getVideoFormatByName(headerMap, title, url);
        }
        String mime = cts.toString();
        try {
            String path = new URL(url).getPath();
            String extension = FileUtil.getExtension(path);
            Log.d(TAG, "detectVideoFormat: " + extension);
            if ("mp4".equals(extension)) {
                mime = "video/mp4";
            } else if ("m3u8".equals(extension) || (HttpUtil.isFuckImage(url, mime) && url.contains(".m3u8"))) {
                return videoFormatList.get(0);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return getVideoFormat(title, url);
        }
        mime = mime.toLowerCase().replace("[", "").replace("]", "").split(";")[0];
        Log.d(TAG, "detectVideoFormat: " + mime);
        if (isStream(mime) && !url.contains("m3u8")) {
            //流文件，可能是多种格式，需要用文件名匹配
            VideoFormat format = getVideoFormatByName(headerMap, title, url);
            if (format != null) {
                return format;
            }
        }
        if (!TextUtils.isEmpty(mime)) {
            //用Content-Type匹配
            for (VideoFormat videoFormat : videoFormatList) {
                for (String mimePattern : videoFormat.getMimeList()) {
                    if (mime.contains(mimePattern)) {
                        return videoFormat;
                    }
                }
            }
            String extension = ShareUtil.getExtension(url, mime);
            if (StringUtil.isNotEmpty(extension)) {
                return new VideoFormat(extension, Collections.singletonList(mime));
            }
        }
        //最后再用文件名匹配
        VideoFormat format = getVideoFormatByName(headerMap, title, url);
        if (format != null) {
            return format;
        }
        if (isStream(mime)) {
            long size = 0;
            if (headerMap.containsKey("Content-Length") && headerMap.get("Content-Length").size() > 0) {
                try {
                    size = Long.parseLong(headerMap.get("Content-Length").get(0));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Timber.d(e, "NumberFormatException");
                }
            }
            if (size > 1024 * 1024 * 200) {
                //大于200MB的流文件当成mp4
                return videoFormatList.get(1);
            }
        }
        //实在匹配不到，只能把后缀弄上去，忽略校验
        return getVideoFormatAnyway(url);
    }

    public static boolean isStreamContent(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        String mime = contentType.toLowerCase().replace("[", "").replace("]", "").split(";")[0];
        return isStream(mime);
    }

    public static boolean isStream(String mime) {
        return "application/octet-stream".equals(mime) || "application/oct-stream".equals(mime) || "multipart/form-data".equals(mime);
    }
}

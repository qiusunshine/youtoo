package com.example.hikerview.ui.browser.util;

import android.text.TextUtils;
import com.example.hikerview.ui.browser.model.VideoFormat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xm on 17-8-16.
 */
public class VideoFormatUtil {

    private static final List<String> videoExtensionList = Arrays.asList(
            "player/m3u8", "mp4", "ts", "mp3", "m4a", "flv", "mpeg"
    );

    private static final List<VideoFormat> videoFormatList = Arrays.asList(
            new VideoFormat("player/m3u8", Arrays.asList("application/octet-stream", "application/vnd.apple.mpegurl", "application/mpegurl", "application/x-mpegurl", "audio/mpegurl", "audio/x-mpegurl")),
            new VideoFormat("mp4", Arrays.asList("video/mp4","application/mp4","video/h264")),
            new VideoFormat("flv", Arrays.asList("video/x-flv")),
            new VideoFormat("f4v", Arrays.asList("video/x-f4v")),
            new VideoFormat("mpeg", Arrays.asList("video/vnd.mpegurl"))
            );


    public static boolean containsVideoExtension(String url){
        for(String videoExtension:videoExtensionList){
            if(!TextUtils.isEmpty(url)) {
                if (url.contains(videoExtension)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isLikeVideo(String fullUrl){
        try {
            URL urlObject = new URL(fullUrl);
            String extension = FileUtil.getExtension(urlObject.getPath());
            if(TextUtils.isEmpty(extension)){
                return true;
            }
            return videoExtensionList.contains(extension.toLowerCase());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static VideoFormat detectVideoFormat(String url, String mime){
        try {
            String path = new URL(url).getPath();
            String extension = FileUtil.getExtension(path);
            if("mp4".equals(extension)){
                mime = "video/mp4";
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        mime = mime.toLowerCase();
        for(VideoFormat videoFormat:videoFormatList){
            if(!TextUtils.isEmpty(mime)) {
                for (String mimePattern : videoFormat.getMimeList()) {
                    if (mime.contains(mimePattern)) {
                        return videoFormat;
                    }
                }
            }
        }
        return null;
    }
}

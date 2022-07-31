package com.example.hikerview.utils;

import androidx.annotation.Nullable;

import com.example.hikerview.ui.browser.model.UrlDetector;
import com.google.android.exoplayer2.util.FileTypes;

import java.util.List;
import java.util.Map;

import static com.google.android.exoplayer2.util.MimeTypes.IMAGE_JPEG;

/**
 * 作者：By 15968
 * 日期：On 2020/3/15
 * 时间：At 21:52
 */
public class HttpUtil {
    public static final int M3U8 = -100;
    public static final int HTML = -101;

    public static String getRealUrl(String domUrl, String url) {
        String baseUrl = StringUtil.getBaseUrl(domUrl);
        if (url.startsWith("http")) {
            return url;
        } else if (url.startsWith("//")) {
            return "http:" + url;
        } else if (url.startsWith("magnet") || url.startsWith("thunder") || url.startsWith("ftp") || url.startsWith("ed2k")) {
            return url;
        } else if (url.startsWith("/")) {
            if (baseUrl.endsWith("/")) {
                return baseUrl.replace("/", "") + url;
            } else {
                return baseUrl + url;
            }
        } else if (url.startsWith("./")) {
            String searchUrl = domUrl.split(";")[0];
            String[] c = searchUrl.split("/");
            if (c.length <= 1) {
                return url;
            }
            String sub = searchUrl.replace(c[c.length - 1], "");
            return sub + url.replace("./", "");
        } else if (url.startsWith("?")) {
            return domUrl + url;
        } else {
            return url;
        }
    }

    public static boolean isFuckImage(String url, String mime) {
        if (mime == null) {
            return false;
        }
        return !UrlDetector.isImage(url) && (mime.contains("[image/") || IMAGE_JPEG.equals(mime));
    }

    public static int inferFileTypeFromResponse(String url, Map<String, List<String>> map) {
        @Nullable List<String> contentTypes = map.get("Content-Type");
        @Nullable
        String mimeType = contentTypes == null || contentTypes.isEmpty() ? null : contentTypes.get(0);
        if (isFuckImage(url, mimeType)) {
            return M3U8;
        }
        if ("text/html".equals(mimeType)) {
            return HTML;
        }
        if (mimeType != null && mimeType.contains(";")) {
            contentTypes.add(mimeType.split(";")[0]);
            contentTypes.remove(0);
        }
        return FileTypes.inferFileTypeFromResponseHeaders(map);
    }
}

package com.example.hikerview.ui.download;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.R;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.download.util.UUIDUtil;
import com.example.hikerview.ui.download.util.VideoFormatUtil;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.view.DialogUtil;
import com.king.app.updater.AppUpdater;
import com.lxj.xpopup.XPopup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2019/12/4
 * 时间：At 23:29
 */
public class DownloadDialogUtil {
    private static final String TAG = "DownloadDialogUtil";
    private static final String[] System_DOWNLOAD = new String[]{".epub", ".zip", ".rar", ".txt", ".exe", ".pdf", ".doc", ".wps", ".ttf"
            , ".otf", ".sfnt", ".woff2", ".woff", ".gz", ".7z",
            ".docx", ".ppt", ".pptx", ".ass", ".srt", ".vtt", ".xls", ".xlsx", ".tar", ".hiker", ".json", ".apk", ".msi", ".torrent", ".md"};
    public static final List<AppUpdater> appTasks = Collections.synchronizedList(new ArrayList<>());


    private static boolean cannotDownload(String title, String url) {
        if (StringUtil.isNotEmpty(url)) {
            if (StringUtil.isCannotHandleScheme(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 不能用默认下载器，只能调用Android的downloadManager
     *
     * @param title
     * @param url
     * @return
     */
    public static boolean useSystemDownload(String title, String url) {
        if (StringUtil.isNotEmpty(title)) {
            for (String s : System_DOWNLOAD) {
                if (title.endsWith(s)) {
                    return true;
                }
            }
        }
        if (StringUtil.isNotEmpty(url) && !UrlDetector.isVideoOrMusic(url)) {
            String ext = url.split("/")[url.split("/").length - 1];
            for (String s : System_DOWNLOAD) {
                if (ext.contains(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeAppDownloadTask(AppUpdater appUpdater) {
        synchronized (appTasks) {
            appTasks.remove(appUpdater);
            if (appTasks.size() > 0) {
                appTasks.get(0).start();
            }
        }
    }

    public static void showEditApkDialog(Activity context, @Nullable String mTitle, @Nullable String mUrl, @Nullable String originalUrl) {
        String shortUrl = mUrl;
        if (StringUtil.isNotEmpty(shortUrl) && shortUrl.length() > 70) {
            shortUrl = shortUrl.substring(0, 70) + "...";
        }

        new XPopup.Builder(context)
                .asConfirm("温馨提示", "确定下载安装来自下面这个网址的软件？" + shortUrl + "（注意自行识别软件安全性）", () -> {
                    String rUrl = originalUrl.split("##")[0];
                    String name = "uuid_" + (StringUtil.isEmpty(mTitle) ? UUIDUtil.genUUID() : FileUtil.getSimpleName(mTitle));
                    DownloadTask downloadTask = new DownloadTask(
                            UUIDUtil.genUUID(), null, null, null, rUrl, rUrl, name, 0L);
                    DownloadManager.instance().addTask(downloadTask);
                    ToastMgr.shortBottomCenter(context, "已加入下载队列");
                }).show();
    }

    public static String getFileName(String url) {
        if (StringUtil.isEmpty(url)) {
            return url;
        }
        String[] s = url.split("#");
        url = s[0];
        s = url.split("\\?");
        url = s[0];
        int start = url.lastIndexOf("/");
        if (start != -1 && start < url.length() - 1) {
            return url.substring(start + 1);
        } else {
            //没有/直接取base64
            return new String(Base64.encode(url.getBytes(), Base64.NO_WRAP));
        }
    }

    public static boolean isApk(String fileName, String mimetype) {
        if (mimetype == null) {
            mimetype = "";
        }
        if ("application/vnd.android.package-archive".equals(mimetype)) {
            return true;
        }
        try {
            if (StringUtil.isNotEmpty(fileName) && fileName.endsWith(".apk") && "application/octet-stream".endsWith(mimetype)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void showEditDialog(Activity context, @Nullable String mTitle, @Nullable String mUrl) {
        showEditDialog(context, mTitle, mUrl, null);
    }

    public static void showEditDialog(Activity context, @Nullable String mTitle, @Nullable String mUrl, @Nullable String mimetype) {
        showEditDialog(context, mTitle, mUrl, mimetype, null);
    }

    public static void showEditDialog(Activity context, @Nullable String mTitle, @Nullable String mUrl,
                                      @Nullable String mimetype, @Nullable Consumer<DownloadTask> interceptor) {
        showEditDialog(context, mTitle, mUrl, mimetype, -1, interceptor);
    }

    public static void showEditDialog(Activity context, @Nullable String mTitle, @Nullable String mUrl,
                                      @Nullable String mimetype, long size, @Nullable Consumer<DownloadTask> interceptor) {
        String originalUrl = mUrl;
        if (StringUtil.isNotEmpty(mUrl)) {
            mUrl = DownloadChooser.getCanDownloadUrl(context, mUrl);
            mUrl = HttpParser.getThirdDownloadSource(mUrl);
        }
        if (StringUtil.isNotEmpty(mTitle) && mTitle.equals(mUrl)) {
            //尝试提取一下文件名
            mTitle = getFileName(mTitle);
        }
        if (StringUtil.isNotEmpty(mTitle) && mTitle.startsWith("http")) {
            mTitle = getFileName(mTitle);
        }
        int downloader = PreferenceMgr.getInt(context, "defaultDownloader", 0);
        if (downloader != 0 && StringUtil.isNotEmpty(mUrl)) {
            DownloadChooser.startDownload(context, downloader, mTitle, mUrl, originalUrl);
            return;
        }
        if (StringUtil.isNotEmpty(mUrl) && (isApk(mTitle, mimetype) || mUrl.contains(".apk"))) {
            showEditApkDialog(context, mTitle, mUrl, originalUrl);
            return;
        }
        if (cannotDownload(mTitle, mUrl)) {
            DownloadChooser.startDownloadByThird(context, mTitle, mUrl, originalUrl);
            return;
        }
        final View view1 = LayoutInflater.from(context).inflate(R.layout.view_dialog_download_add, null, false);
        final AppCompatEditText titleE = view1.findViewById(R.id.download_add_title);
        final AppCompatEditText urlE = view1.findViewById(R.id.download_add_url);
        final AppCompatEditText suffixE = view1.findViewById(R.id.download_add_suffix);
        TextView sizeView = view1.findViewById(R.id.download_add_size);
        if (size > 0) {
            sizeView.setVisibility(View.VISIBLE);
            sizeView.setText((FileUtil.getFormatedFileSize(size) + "（" + size + "个字节）"));
        }
        view1.findViewById(R.id.download_add_suffix_gen).setOnClickListener(v -> {
            String title = titleE.getText().toString();
            String url = urlE.getText().toString();
            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(url)) {
                ToastMgr.shortCenter(context, "文件名和地址均为空，无法提取后缀");
            }
            VideoFormat videoFormat = VideoFormatUtil.getVideoFormat(title, url);
            if (videoFormat != null && StringUtil.isNotEmpty(videoFormat.getName())) {
                suffixE.setText(videoFormat.getName());
            } else {
                ToastMgr.shortCenter(context, "提取文件后缀失败");
            }
        });
        if (StringUtil.isNotEmpty(mTitle)) {
            titleE.setText(mTitle);
        }
        if (StringUtil.isNotEmpty(mUrl)) {
            urlE.setText(mUrl);
        }
        String finalMUrl = mUrl;
        titleE.setTag(originalUrl);
        view1.findViewById(R.id.info).setOnClickListener(v -> new XPopup.Builder(context)
                .asInputConfirm("完整链接", null, (String) titleE.getTag(), null, titleE::setTag, null, R.layout.xpopup_confirm_input).show());
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("添加文件下载")
                .setView(view1)
                .setCancelable(true)
                .setPositiveButton("下载", (dialog, which) -> {
                    String title = titleE.getText().toString();
                    String url = urlE.getText().toString();
                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
                        ToastMgr.shortBottomCenter(context, "请输入完整信息");
                    } else {
                        dialog.dismiss();
                        String rUrl = StringUtils.equals(url, finalMUrl) ? (String) titleE.getTag() : url;
                        if (StringUtil.isEmpty(rUrl)) {
                            rUrl = url;
                        }
                        rUrl = rUrl.split("##")[0];
                        DownloadTask downloadTask = new DownloadTask(
                                UUIDUtil.genUUID(), null, null, null, rUrl, rUrl, title, 0L);
                        downloadTask.setSuffix(suffixE.getText().toString());
//                            downloadTask.setFilm(film);
                        if (interceptor != null) {
                            interceptor.accept(downloadTask);
                        }
                        DownloadManager.instance().addTask(downloadTask);
                        ToastMgr.shortBottomCenter(context, "已加入下载队列");
                    }
                }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).create();
        View download_add_others = view1.findViewById(R.id.download_add_others);
        download_add_others.setOnClickListener(v -> {
            String title = titleE.getText().toString();
            String url = urlE.getText().toString();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(url)) {
                ToastMgr.shortBottomCenter(context, "请输入完整信息");
            } else {
                alertDialog.dismiss();
                DownloadChooser.startDownloadByThird(context, title, url, (String) titleE.getTag());
            }
        });
        DialogUtil.INSTANCE.showAsCard(context, alertDialog);
    }

    public static void downloadNow(Activity context, String mTitle, String mUrl) {
        String originalUrl = mUrl;
        if (StringUtil.isNotEmpty(mUrl)) {
            mUrl = DownloadChooser.getCanDownloadUrl(context, mUrl);
            mUrl = HttpParser.getThirdDownloadSource(mUrl);
        }
        if (StringUtil.isNotEmpty(mTitle) && mTitle.equals(mUrl)) {
            //尝试提取一下文件名
            mTitle = getFileName(mTitle);
        }
        if (StringUtil.isNotEmpty(mTitle) && mTitle.startsWith("http")) {
            mTitle = getFileName(mTitle);
        }
        int downloader = PreferenceMgr.getInt(context, "defaultDownloader", 0);
        if (downloader != 0 && StringUtil.isNotEmpty(mUrl)) {
            DownloadChooser.startDownload(context, downloader, mTitle, mUrl, originalUrl);
            return;
        }
        if (cannotDownload(mTitle, mUrl)) {
            DownloadChooser.startDownloadByThird(context, mTitle, mUrl, originalUrl);
            return;
        }
        String rUrl = originalUrl.split("##")[0];
        DownloadTask downloadTask = new DownloadTask(
                UUIDUtil.genUUID(), null, null, null, rUrl, rUrl, mTitle, 0L);
        downloadTask.setSuffix("");
        downloadTask.setFilm(null);
        DownloadManager.instance().addTask(downloadTask);
        ToastMgr.shortBottomCenter(context, "已加入下载队列");
    }

    public static String getApkDownloadPath(Context context) {
        return DownloadChooser.getRootPath(context);
    }
}

package com.example.hikerview.ui.browser.util;

import android.app.Activity;
import android.content.Context;

import com.example.hikerview.ui.download.DownloadDialogUtil;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.UriUtils;
import com.lxj.xpopup.XPopup;

import java.io.File;
import java.io.IOException;

/**
 * 作者：By 15968
 * 日期：On 2021/4/1
 * 时间：At 22:04
 */

public class HomeConfigUtil {

    /**
     * 删除下载的软件
     */
    public static void deleteApks(Context context) {
        try {
            //先删除遗留文件
            File dir20 = new File(UriUtils.getRootDir(context) + File.separator + "magnet");
            if (dir20.isDirectory() && dir20.exists()) {
                File[] files = dir20.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        FileUtil.deleteDirs(file.getAbsolutePath());
                    }
                }
            }
            String dir0 = UriUtils.getRootDir(context) + File.separator + "cache";
            File dir2 = new File(dir0);
            if (dir2.isDirectory() && dir2.exists()) {
                File[] files = dir2.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.getName().startsWith("_fileSelect_")) {
                            file.delete();
                        }
                    }
                }
            }
            boolean apkClean = PreferenceMgr.getBoolean(context, "download", "apkClean", false);
            if (!apkClean) {
                return;
            }
            String filePath = DownloadDialogUtil.getApkDownloadPath(context);
            if (filePath != null && filePath.length() > 0) {
                File dir = new File(filePath);
                if (!dir.exists()) {
                    return;
                }
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isDirectory() || !file.getName().endsWith(".apk")) {
                            continue;
                        }
                        try {
                            file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void scanCrashLog(Activity context) {
        String path = UriUtils.getRootDir(context) + "/logs/";
        File dir = new File(path);
        if (!dir.exists()) {
            return;
        }
        String path2 = UriUtils.getRootDir(context) + "/reports/";
        File dir2 = new File(path2);
        if (!dir2.exists()) {
            dir2.mkdir();
        }
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            File file = files[0];
            for (int i = 1; i < files.length; i++) {
                try {
                    files[i].delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (file.exists()) {
                String filePath = file.getAbsolutePath();
                String reportPath = filePath.replace("/logs/", "/reports/");
                try {
                    FileUtil.copyFile(filePath, reportPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                new File(filePath).delete();
                context.runOnUiThread(() -> new XPopup.Builder(context)
                        .asConfirm("检测到崩溃日志", "检测到上次使用过程中的崩溃日志，建议通过QQ发送给开发者，让开发者可以尽快修复问题，是否调用QQ发送文件？",
                                () -> ShareUtil.findChooserToSend(context, "file://" + reportPath)).show());
            }
        }
    }
}

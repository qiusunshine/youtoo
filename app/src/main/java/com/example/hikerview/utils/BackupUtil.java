package com.example.hikerview.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.Stream;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.model.JSManager;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.lxj.xpopup.XPopup;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2019/12/1
 * 时间：At 14:51
 */
public class BackupUtil {

    private static final String TAG = "BackupUtil";

    public static void scanFilesToPaths(Context context, Collection<String> filePaths) {
        String shared_prefs = UriUtils.getCacheDir(context) + File.separator + "shared_prefs.zip";
        FileUtil.deleteFile(shared_prefs);
        try {
            String prefs = PreferenceMgr.getFilesDir(context);
            File dir = new File(prefs);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    List<File> files1 = Stream.of(files)
                            .filter(it -> it.getName().endsWith(".xml") &&
                                    !it.getName().startsWith("x5_") && !it.getName().startsWith("tbs")
                                    && !it.getName().startsWith("TBS") && !it.getName().startsWith("litepal")
                                    && !it.getName().startsWith("tsui") && !it.getName().startsWith("plugin")
                                    && !it.getName().startsWith("debug") && !it.getName().startsWith("info")
                                    && !it.getName().startsWith("multi_") && !it.getName().startsWith("qb_")
                                    && !it.getName().startsWith("sai") && !it.getName().startsWith("Classics")
                                    && !it.getName().startsWith("umeng") && !it.getName().startsWith("WebView")
                                    && !it.getName().startsWith("turi") && !it.getName().startsWith("Bugly")
                                    && !it.getName().startsWith("umdat") && !it.getName().startsWith("uifa"))
                            .toList();
                    try {
                        String pdir = UriUtils.getCacheDir(context) + File.separator + "shared_prefs";
                        File d = new File(pdir);
                        if (d.exists()) {
                            FileUtil.deleteDirs(pdir);
                        }
                        d.mkdirs();
                        Set<String> paths = new HashSet<>();
                        for (File value : files1) {
                            String name = value.getName();
                            name = name.substring(0, name.lastIndexOf("."));
                            Map<String, ?> all = PreferenceMgr.all(context, name);
                            File file = new File(pdir + File.separator + name + ".json");
                            FileUtil.stringToFile(JSON.toJSONString(all), file.getAbsolutePath());
                            paths.add(file.getAbsolutePath());
                        }
                        if (ZipUtils.zipFiles(paths, shared_prefs)) {
                            filePaths.add(shared_prefs);
                        }
                    } catch (Exception e) {
                        Timber.e(e, "shared_prefsZip: ");
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            ThreadTool.INSTANCE.runOnUI(() -> ToastMgr.shortBottomCenter(context, "获取配置目录失败"));
        }
    }

    public static void recoverPrefs(Context context, String fileDirPath) {
        try {
            File rulesFile = new File(fileDirPath + File.separator + "shared_prefs.zip");
            if (rulesFile.exists()) {
                //新版带规则文件
                String rulesDir = fileDirPath + File.separator + "shared_prefs";
                ZipUtils.unzipFile(rulesFile.getAbsolutePath(), rulesDir);
                String prefs = PreferenceMgr.getFilesDir(context);
                FileUtil.copyDirectiory(prefs, rulesDir);
                File[] files = (new File(rulesDir)).listFiles();
                if (files == null) {
                    return;
                }
                for (File value : files) {
                    if (value.isFile() && value.getName().endsWith(".json")) {
                        String name = value.getName();
                        name = name.substring(0, name.lastIndexOf("."));
                        String text = FileUtil.fileToString(value.getAbsolutePath());
                        Map<String, Object> json = JSON.parseObject(text);
                        Map<String, ?> old = PreferenceMgr.all(context, name);
                        for (Map.Entry<String, ?> entry : json.entrySet()) {
                            PreferenceMgr.put(context, name, entry.getKey(), entry.getValue());
                        }
                        //key不存在的情况也要还原
                        for (Map.Entry<String, ?> entry : old.entrySet()) {
                            if (!json.containsKey(entry.getKey())) {
                                PreferenceMgr.remove(context, name, entry.getKey());
                            }
                        }
                    }
                }
                FileUtil.deleteDirs(rulesDir);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void backupDBAndJs(Context context, boolean silence) {
        BackupUtil.backupDB(context, true);
        List<String> filePaths = new ArrayList<>();
        String dbPath = UriUtils.getRootDir(context) + File.separator + "backup" + File.separator + BackupUtil.getDBFileNameWithVersion();
        filePaths.add(dbPath);
        File jsDir = new File(JSManager.getJsDirPath());
        if (!jsDir.exists()) {
            jsDir.mkdirs();
        }
        File[] jsFiles = jsDir.listFiles();
        if (jsFiles != null) {
            for (File jsFile : jsFiles) {
                filePaths.add(jsFile.getAbsolutePath());
            }
        }
        scanFilesToPaths(context, filePaths);
        String zipFilePath = genBackupZipPath(context);
        try {
            File dir = new File(zipFilePath).getParentFile();
            File[] files = dir != null ? dir.listFiles() : null;
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("youtoo") && file.getName().endsWith(".zip")) {
                        file.delete();
                    }
                }
            }
            if (ZipUtils.zipFiles(filePaths, zipFilePath)) {
                if (!silence) {
                    ToastMgr.shortBottomCenter(context, "备份成功！");
                    ShareUtil.findChooserToSend(context, "file://" + zipFilePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper())
                    .post(() -> Toast
                            .makeText(Application.application.getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                            .show());
        }
    }


    public static String genBackupZipPath(Context context) {
        return UriUtils.getRootDir(context) + File.separator + "backup" + File.separator + "youtoo"
                + "V" + CommonUtil.getVersionName(context) + ".zip";
    }

    public static String findBackupZipPath(Context context) {
        String zipFilePath = genBackupZipPath(context);
        if (new File(zipFilePath).exists()) {
            return zipFilePath;
        } else {
            File dir = new File(zipFilePath).getParentFile();
            File[] files = dir != null ? dir.listFiles() : null;
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith("youtoo") && file.getName().endsWith(".zip")) {
                        return file.getAbsolutePath();
                    }
                }
            }
            return zipFilePath;
        }
    }
    public static void backupDB(Context context, boolean silence) {
        File dbFile = context.getDatabasePath(getDBFileName());
        Log.d(TAG, "backupDB: path==>" + dbFile.getAbsolutePath());
        if (dbFile.exists()) {
            File exportDir = new File(UriUtils.getRootDir(context) + File.separator + "backup");
            if (!exportDir.exists()) {
                boolean ok = exportDir.mkdirs();
                if (!ok) {
                    ToastMgr.shortBottomCenter(context, "创建备份文件夹失败");
                    return;
                }
            }
            File backup = new File(exportDir, getDBFileNameWithVersion());
            try {
                if (backup.exists()) {
                    boolean del = backup.delete();
                    if (!del) {
                        ToastMgr.shortBottomCenter(context, "删除旧数据库文件失败");
                        return;
                    }
                }
                boolean ok = backup.createNewFile();
                if (!ok) {
                    ToastMgr.shortBottomCenter(context, "创建新数据库文件失败");
                    return;
                }
                FileUtil.copyFile(dbFile.getAbsolutePath(), backup.getAbsolutePath());
                if (!silence) {
                    ToastMgr.shortBottomCenter(context, "备份成功！");
                    ShareUtil.findChooserToSend(context, "file://" + backup.getAbsolutePath());
                }
            } catch (Exception e) {
                DebugUtil.showErrorMsg((Activity) context, "数据库备份出错", e);
            }
        } else {
            ToastMgr.shortBottomCenter(context, "数据库文件不存在");
        }
    }

    public static void backupDB(Context context) {
        backupDB(context, false);
    }

    public static void recoveryDBAndJsNow(Context context){
        String zipFilePath = findBackupZipPath(context);
        try {
            String fileDirPath = UriUtils.getRootDir(context) + File.separator + "backup" + File.separator + "youtoo";
            FileUtil.deleteDirs(fileDirPath);
            new File(fileDirPath).mkdirs();
            ZipUtils.unzipFile(zipFilePath, fileDirPath);

            recoverPrefs(context, fileDirPath);

            String dbFilePath = fileDirPath + File.separator + BackupUtil.getDBFileNameWithVersion();
            File dbFile = new File(dbFilePath);
            String dbNewFilePath = UriUtils.getRootDir(context) + File.separator + "backup" + File.separator + BackupUtil.getDBFileNameWithVersion();
            File jsFile = new File(fileDirPath);
            File[] jsFiles = jsFile.listFiles();
            boolean dbExist = dbFile.exists();
            int jsFileNum = 0;
            if (dbExist) {
                FileUtil.copyFile(dbFile.getAbsolutePath(), dbNewFilePath);
            }
            if (jsFiles != null) {
                for (File jsFilePath : jsFiles) {
                    if (!jsFilePath.getName().endsWith(".js")) {
                        jsFilePath.delete();
                    } else {
                        jsFileNum++;
                    }
                }
            }
            FileUtil.deleteDirs(JSManager.getJsDirPath());
            FileUtil.copyDirectiory(JSManager.getJsDirPath(), jsFile.getAbsolutePath());
            int finalJsFileNum = jsFileNum;
            String title = "";
            if (!dbExist) {
                title = "已恢复" + finalJsFileNum + "个JS插件，没有获取到适合当前版本的db文件";
            } else {
                title = "已恢复" + finalJsFileNum + "个JS插件，是否立即恢复db文件（包括首页规则、历史记录、收藏等）？";
            }
            new XPopup.Builder(context)
                    .asConfirm("恢复完成", title, () -> {
                        if (dbExist) {
                            BackupUtil.recoveryDBNow(context);
                        }
                    }).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void recoveryDBAndJs(Context context) {
        String zipFilePath = findBackupZipPath(context);
        new PromptDialog(context)
                .setTitleText("风险提示")
                .setSpannedContentByStr("确定从备份文件（" + zipFilePath + "）恢复数据吗？当前支持的数据库版本为" + LitePal.getDatabase().getVersion() + "，注意“如果备份和恢复时的数据库版本不一致会导致软件启动闪退！”")
                .setPositiveListener("确定恢复", dialog1 -> {
                    dialog1.dismiss();
                    recoveryDBAndJsNow(context);
                }).show();

    }

    public static void recoveryDB(Context context) {
        String filePath = UriUtils.getRootDir(context) + File.separator + "backup" + File.separator + getDBFileNameWithVersion();
        new PromptDialog(context)
                .setTitleText("风险提示")
                .setSpannedContentByStr("确定从备份文件（" + filePath + "）恢复数据吗？当前支持的数据库版本为" + LitePal.getDatabase().getVersion() + "，注意“如果备份和恢复时的数据库版本不一致会导致软件启动闪退！”")
                .setPositiveListener("确定恢复", dialog1 -> {
                    dialog1.dismiss();
                    recoveryDBNow(context);
                }).show();

    }

    public static void recoveryDBNow(Context context) {
        synchronized (LitePal.class) {
            LitePal.getDatabase().close();
            File exportDir = new File(UriUtils.getRootDir(context) + File.separator + "backup");
            if (!exportDir.exists()) {
                boolean ok = exportDir.mkdirs();
                if (!ok) {
                    ToastMgr.shortBottomCenter(context, "创建备份文件夹失败");
                    return;
                }
            }
            File backup = new File(exportDir, getDBFileNameWithVersion());
            if (backup.exists()) {
                File dbFile = context.getDatabasePath(getDBFileName());
                if (dbFile.exists()) {
                    boolean del = dbFile.delete();
                    if (!del) {
                        ToastMgr.shortBottomCenter(context, "删除旧数据库文件失败");
                        return;
                    }
                }
                try {
                    boolean ok = dbFile.createNewFile();
                    if (!ok) {
                        ToastMgr.shortBottomCenter(context, "创建新数据库文件失败");
                        return;
                    }
                    FileUtil.copyFile(backup.getAbsolutePath(), dbFile.getAbsolutePath());
                    new PromptDialog(context)
                            .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                            .setContentText("从备份恢复成功！开始重启软件使生效！")
                            .setTheCancelable(false)
                            .setAnimationEnable(true)
                            .setTitleText("温馨提示")
                            .setPositiveListener("立即重启", dialog -> {
                                dialog.dismiss();
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(0);
                            }).show();
                } catch (Exception e) {
                    DebugUtil.showErrorMsg((Activity) context, "数据库恢复备份出错", e);
                }
            } else {
                ToastMgr.shortBottomCenter(context, backup.getAbsolutePath() + "数据库文件不存在");
            }
        }
    }

    public static String getDBFileNameWithVersion() {
        return "youtoo_" + LitePal.getDatabase().getVersion() + ".db";
    }

    private static String getDBFileName() {
        return "hiker.db";
    }
}

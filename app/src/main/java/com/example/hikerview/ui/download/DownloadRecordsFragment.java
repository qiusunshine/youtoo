package com.example.hikerview.ui.download;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.model.DownloadRecord;
import com.example.hikerview.ui.ActivityManager;
import com.example.hikerview.ui.base.BaseFragment;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.browser.util.UUIDUtil;
import com.example.hikerview.ui.download.exception.DownloadErrorException;
import com.example.hikerview.ui.home.reader.EpubFile;
import com.example.hikerview.ui.thunder.ThunderManager;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.ui.view.XGridLayoutManager;
import com.example.hikerview.ui.view.popup.MyXpopup;
import com.example.hikerview.ui.webdlan.LocalServerParser;
import com.example.hikerview.utils.AutoImportHelper;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.FileUtil;
import com.example.hikerview.utils.FilesUtilsKt;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ThreadTool;
import com.example.hikerview.utils.ToastMgr;
import com.jeffmony.m3u8library.listener.IVideoTransformListener;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 作者：By 15968
 * 日期：On 2019/12/22
 * 时间：At 19:17
 */
public class DownloadRecordsFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private DownloadRecordsAdapter adapter;
    private final Object lock = new Object();
    private List<DownloadRecord> rules = new ArrayList<>();
    private List<DownloadRecord> showList = new ArrayList<>();
    private boolean isSorting = false;
    private boolean isMultiDeleting = false;
    private LoadingPopupView transformLoadingPopup;

    public String getSelectFilm() {
        return selectFilm;
    }

    private String selectFilm = null;
    private boolean opened = false;

    @Override
    protected int initLayout() {
        return R.layout.fragment_download_list;
    }

    @Override
    protected void initView() {
        recyclerView = findView(R.id.download_list_recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new DownloadRecordsAdapter(getContext(), showList);
        adapter.setOnItemClickListener(onItemClickListener);
        XGridLayoutManager gridLayoutManager = new XGridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public List<DownloadRecord> getRules() {
        List<DownloadRecord> records;
        synchronized (lock) {
            records = new ArrayList<>(rules);
        }
        return records;
    }

    public void setMultiDeleting(boolean isMultiDeleting) {
        this.isMultiDeleting = isMultiDeleting;
    }

    public void setRules(List<DownloadRecord> rules, boolean downloaded) {
        if (isSorting || isMultiDeleting) {
            return;
        }
        if (JSON.toJSONString(this.rules).equals(JSON.toJSONString(rules))) {
            return;
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                synchronized (lock) {
                    this.rules.clear();
                    this.rules.addAll(rules);
                }
                updateShowList();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    checkOpenIntent(downloaded);
                } else {
                    recyclerView.postDelayed(() -> {
                        adapter.notifyDataSetChanged();
                        checkOpenIntent(downloaded);
                    }, 500);
                }
            });
        }
    }

    private void checkOpenIntent(boolean downloaded) {
        String openRecord = getActivity() != null ? getActivity().getIntent().getStringExtra("openRecord") : "";
        if (downloaded && !opened && StringUtil.isNotEmpty(openRecord)) {
            opened = true;
            getActivity().getIntent().removeExtra("openRecord");
            for (DownloadRecord record : this.rules) {
                if (openRecord.equals(record.getTaskId())) {
                    if (StringUtil.isNotEmpty(record.getFilm()) && !record.getFilm().equals(selectFilm)) {
                        updateShowList(record.getFilm());
                    }
                    for (int i = 0; i < showList.size(); i++) {
                        if (openRecord.equals(showList.get(i).getTaskId())) {
                            onItemClickListener.onClick(null, i);
                            return;
                        }
                    }
                    return;
                }
            }
        }
    }

    public void backToHome() {
        updateShowList(null);
        adapter.notifyDataSetChanged();
    }

    public void updateShowList(String film) {
        selectFilm = film;
        updateShowList();
    }

    private void updateShowList() {
        synchronized (lock) {
            List<DownloadRecord> records = new ArrayList<>();
            if (StringUtil.isNotEmpty(selectFilm)) {
                for (DownloadRecord rule : rules) {
                    if (selectFilm.equals(rule.getFilm())) {
                        records.add(rule);
                    }
                }
            } else {
                //用来保证顺序
                List<String> dirList = new ArrayList<>();
                Map<String, Integer> countMap = new HashMap<>();
                records.addAll(rules);
                for (DownloadRecord rule : rules) {
                    if (StringUtil.isNotEmpty(rule.getFilm())) {
                        if (!dirList.contains(rule.getFilm())) {
                            dirList.add(rule.getFilm());
                            countMap.put(rule.getFilm(), 1);
                        } else {
                            countMap.put(rule.getFilm(), countMap.get(rule.getFilm()) + 1);
                        }
                    }
                }
                for (Iterator<DownloadRecord> iterator = records.iterator(); iterator.hasNext(); ) {
                    DownloadRecord next = iterator.next();
                    if (StringUtil.isNotEmpty(next.getFilm()) && countMap.containsKey(next.getFilm()) && countMap.get(next.getFilm()) > 1) {
                        iterator.remove();
                    }
                }
                //删除只有一个item的文件夹
                for (Iterator<String> iterator = dirList.iterator(); iterator.hasNext(); ) {
                    String next = iterator.next();
                    if (countMap.containsKey(next) && countMap.get(next) < 2) {
                        iterator.remove();
                    }
                }
                for (int i = dirList.size() - 1; i >= 0; i--) {
                    DownloadRecord record = new DownloadRecord();
                    record.setSourcePageTitle(dirList.get(i));
                    record.setVideoType("dir");
                    record.setSourcePageUrl(String.valueOf(countMap.get(dirList.get(i))));
                    records.add(0, record);
                }
            }
            showList.clear();
            showList.addAll(records);
        }
    }

    private DownloadRecordsAdapter.OnItemClickListener onItemClickListener = new DownloadRecordsAdapter.OnItemClickListener() {
        @Override
        public void onClick(View view, int position) {
            if (position < 0 || position >= showList.size()) {
                return;
            }
            DownloadRecord record = showList.get(position);
            if (isMultiDeleting) {
                record.setSelected(!record.isSelected());
                adapter.notifyItemChanged(position);
                return;
            }
            if ("dir".equals(record.getVideoType())) {
                updateShowList(record.getSourcePageTitle());
                adapter.notifyDataSetChanged();
                return;
            }
            if (!DownloadStatusEnum.SUCCESS.getCode().equals(record.getStatus())) {
                onLongClick(view, position);
                return;
            }
            if (record.getFullName().endsWith(".torrent")) {
                ThunderManager.INSTANCE.startDownloadMagnet(getContext(), getLocalPath(record));
                return;
            }
            if (record.getFullName().endsWith(".epub")) {
                EpubFile.INSTANCE.showEpubView(getContext(), getLocalPath(record));
                return;
            }
            checkDownload(record);
            if (UrlDetector.isImage(record.getFullName())) {
                String path = "file://" + getLocalPath(record);
                new MyXpopup().Builder(getContext())
                        .asImageViewer(null, path, new PopImageLoaderNoView(path))
                        .show();
                return;
            }
            String normalPath = DownloadManager.getNormalFilePath(record);
            if (normalPath != null) {
                File file = new File(normalPath);
                if (file.exists() && !file.isDirectory() && !UrlDetector.isVideoOrMusic(record.getFullName())) {
                    if (getActivity() != null && getActivity().getIntent().getBooleanExtra("subtitle", false)) {
                        if (file.getName().endsWith(".srt")
                                || file.getName().endsWith(".ass")
                                || file.getName().endsWith(".vtt")) {
                            Intent intent = new Intent();
                            intent.putExtra("subtitle", file.getAbsolutePath());
                            getActivity().setResult(Activity.RESULT_OK, intent);
                            getActivity().finish();
                            return;
                        }
                    }
                    ShareUtil.findChooserToDeal(getContext(), file.getAbsolutePath());
                    return;
                }
            }
            play(record, position);
        }

        private void play(DownloadRecord record, int position) {
            String u = LocalServerParser.getRealUrl(getActivity(), record);
            if (u.startsWith("file://") && !new File(u.replace("file://", "")).exists()) {
                ToastMgr.shortBottomCenter(getContext(), "找不到文件");
                return;
            }
            List<VideoChapter> chapters = new ArrayList<>();
            for (int i = 0; i < position; i++) {
                if ("dir".equals(showList.get(i).getVideoType())) {
                    continue;
                }
                VideoChapter videoChapter = new VideoChapter();
                videoChapter.setTitle(showList.get(i).getSourcePageTitle());
                videoChapter.setUse(false);
                videoChapter.setDownloadRecord(showList.get(i));
                chapters.add(videoChapter);
            }
            VideoChapter videoChapter = new VideoChapter();
            videoChapter.setTitle(showList.get(position).getSourcePageTitle());
            videoChapter.setUrl(u);
            videoChapter.setDownloadRecord(showList.get(position));
            videoChapter.setUse(true);
            chapters.add(videoChapter);
            for (int i = position + 1; i < showList.size(); i++) {
                if ("dir".equals(showList.get(i).getVideoType())) {
                    continue;
                }
                VideoChapter chapter = new VideoChapter();
                chapter.setTitle(showList.get(i).getSourcePageTitle());
                chapter.setDownloadRecord(showList.get(i));
                chapter.setUse(false);
                chapters.add(chapter);
            }
            PlayerChooser.startPlayer(getActivity(), chapters);
        }

        @Override
        public void onLongClick(View view, int position) {
            if (getActivity() instanceof DownloadRecordsActivity) {
                //小米系统的bug，有时候触发了长按也不会触发MotionEvent.ACTION_UP
                ((DownloadRecordsActivity) getActivity()).setTouch(false);
            }
            if (isSorting || isMultiDeleting) {
                return;
            }
            if (position < 0 || position >= showList.size()) {
                return;
            }
            DownloadRecord record = showList.get(position);
            String status = showList.get(position).getStatus();
            String[] operations;

            if ("dir".equals(record.getVideoType())) {
                operations = new String[]{"删除下载", "重命名标题", "转存到公开目录"};
            } else if (DownloadStatusEnum.SUCCESS.getCode().equals(status) || DownloadStatusEnum.ERROR.getCode().equals(status)
                    || DownloadStatusEnum.CANCEL.getCode().equals(status) || DownloadStatusEnum.BREAK.getCode().equals(status)) {
                if (DownloadStatusEnum.SUCCESS.getCode().equals(status) && "player/m3u8".equals(record.getVideoType())) {
                    operations = new String[]{"删除下载", "批量删除", "重新下载", "重命名标题", "复制下载链接", "合并为MP4格式"};
                } else if (DownloadStatusEnum.SUCCESS.getCode().equals(status) && "normal".equals(record.getVideoType())) {
                    operations = new String[]{"删除下载", "批量删除", "重新下载", "重命名标题", "复制下载链接", "分享本地文件", "复制文件路径", "转存到公开目录"};
                } else if (DownloadStatusEnum.ERROR.getCode().equals(status)) {
                    operations = new String[]{"继续下载", "删除下载", "批量删除", "重新下载", "重命名标题", "复制下载链接", "更新下载链接", "复制文件路径", "强制忽略错误"};
                } else {
                    operations = new String[]{"继续下载", "删除下载", "批量删除", "重新下载", "重命名标题", "复制下载链接", "更新下载链接", "复制文件路径"};
                }
            } else {
                if ("player/m3u8".equals(record.getVideoType())) {
                    operations = new String[]{"边下边播", "暂停下载", "取消下载", "批量取消"};
                } else {
                    operations = new String[]{"暂停下载", "取消下载", "批量取消"};
                }
            }
            new XPopup.Builder(getContext())
                    .asCustom(new CustomCenterRecyclerViewPopup(getContext())
                            .withTitle("请选择操作")
                            .with(operations, operations.length > 4 ? 2 : 1, new CustomCenterRecyclerViewPopup.ClickListener() {
                                @Override
                                public void click(String text, int option) {
                                    switch (text) {
                                        case "边下边播":
                                            play(record, position);
                                            break;
                                        case "删除下载":
                                            deleteByRecord(record);
                                            break;
                                        case "重命名标题":
                                            new XPopup.Builder(getContext()).asInputConfirm("重命名标题", null,
                                                    record.getSourcePageTitle(), "请输入标题，不能为空",
                                                    text1 -> {
                                                        if (StringUtil.isEmpty(text1)) {
                                                            ToastMgr.shortBottomCenter(getContext(), "不能为空");
                                                            return;
                                                        }
                                                        if ("dir".equals(record.getVideoType())) {
                                                            if ("音乐/音频文件".equals(record.getSourcePageTitle()) || "非音视频格式文件".equals(record.getSourcePageTitle())) {
                                                                ToastMgr.shortBottomCenter(getContext(), "当前文件夹不支持重命名");
                                                                return;
                                                            }
                                                            for (DownloadRecord rule : rules) {
                                                                if (record.getSourcePageTitle().equals(rule.getFilm())) {
                                                                    if (FilesUtilsKt.inDownloadDir(getContext(), rule)) {
                                                                        String name = rule.getFileName();
                                                                        if (name.contains("@@")) {
                                                                            name = text1 + "@@" + rule.getSourcePageTitle();
                                                                        }
                                                                        FilesUtilsKt.renameFileByPath(getContext(), rule.getFailedReason(), name);
                                                                    } else {
                                                                        rule.setFilm(text1);
                                                                        rule.save();
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            record.setSourcePageTitle(text1);
                                                            if (record.getId() > 0) {
                                                                record.save();
                                                            } else {
                                                                if (FilesUtilsKt.inDownloadDir(getContext(), record)) {
                                                                    if (record.getFileName().contains("@@")) {
                                                                        text1 = record.getFileName().split("@@")[0] + "@@" + text1;
                                                                    }
                                                                    FilesUtilsKt.renameFileByPath(getContext(), record.getFailedReason(), text1);
                                                                } else {
                                                                    File file = new File(DownloadChooser.getRootPath(getContext()) + File.separator + record.getFileName());
                                                                    if (file.exists()) {
                                                                        file.renameTo(new File(DownloadChooser.getRootPath(getContext()) + File.separator + text1));
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        updateShowList();
                                                        adapter.notifyDataSetChanged();
                                                    })
                                                    .show();
                                            break;
                                        case "批量删除":
                                        case "批量取消":
                                            batchDelete();
                                            break;
                                        case "复制下载链接":
                                            ClipboardUtil.copyToClipboard(getContext(), record.getSourcePageUrl());
                                            break;
                                        case "强制忽略错误":
                                            try {
                                                if ("normal".equals(record.getVideoType())) {
                                                    HeavyTaskUtil.executeNewTask(() -> {
                                                        String error = DownloadManager.instance().forceMergeNormalFile(record);
                                                        ThreadTool.INSTANCE.runOnUI(() -> {
                                                            if (StringUtil.isNotEmpty(error)) {
                                                                ToastMgr.shortBottomCenter(getContext(), error);
                                                            } else {
                                                                ToastMgr.shortBottomCenter(getContext(), "操昨成功");
                                                            }
                                                        });
                                                    });
                                                    return;
                                                }
                                                DownloadManager.instance().forceIgnoreError(record);
                                                ToastMgr.shortBottomCenter(getContext(), "操作成功");
                                            } catch (DownloadErrorException e) {
                                                e.printStackTrace();
                                                ToastMgr.shortBottomCenter(getContext(), e.getMessage() + "，不支持强制忽略");
                                            }
                                            break;
                                        case "分享本地文件":
                                            ShareUtil.findChooserToSend(getContext(), getLocalPath(record));
                                            break;
                                        case "复制文件路径":
                                            ClipboardUtil.copyToClipboard(getContext(), getLocalPath(record));
                                            break;
                                        case "合并为MP4格式":
                                            transformLoadingPopup = new XPopup.Builder(getContext())
                                                    .asLoading("合并中");
                                            transformLoadingPopup.show();
                                            DownloadManager.transformM3U8ToMp4(getContext(), record, new IVideoTransformListener() {
                                                @Override
                                                public void onTransformProgress(float v) {
                                                    if (getActivity() == null || getActivity().isFinishing() || isDetached()) {
                                                        return;
                                                    }
                                                    if (transformLoadingPopup != null) {
                                                        transformLoadingPopup.setTitle("合并中" + Math.round(v) + "%");
                                                    }
                                                }

                                                @Override
                                                public void onTransformFailed(Exception e) {
                                                    if (getActivity() == null || getActivity().isFinishing() || isDetached()) {
                                                        return;
                                                    }
                                                    if (transformLoadingPopup != null) {
                                                        transformLoadingPopup.dismiss();
                                                    }
                                                }

                                                @Override
                                                public void onTransformFinished() {
                                                    if (getActivity() == null || getActivity().isFinishing() || isDetached()) {
                                                        return;
                                                    }
                                                    if (transformLoadingPopup != null) {
                                                        transformLoadingPopup.dismiss();
                                                    }
                                                }
                                            });
                                            break;
                                        case "重新下载":
                                            reDownload(record);
                                            break;
                                        case "更新下载链接":
                                            if ("player/m3u8".equals(record.getVideoType())) {
                                                //m3u8格式
                                                String m3u8 = DownloadConfig.rootPath + File.separator + record.getFileName() + ".temp"
                                                        + File.separator + "index.m3u8";
                                                if (new File(m3u8).exists()) {
                                                    ToastMgr.shortCenter(getContext(), "暂不支持更新m3u8格式地址");
                                                    return;
                                                }
                                            }
                                            new XPopup.Builder(getContext())
                                                    .asInputConfirm("更新下载地址", "输入新的地址，点击确定后将继续下载", record.getSourcePageUrl(), "", t1 -> {
                                                        if (StringUtil.isNotEmpty(t1)) {
                                                            record.setSourcePageUrl(t1);
                                                            record.save();
                                                            continueDownload(record);
                                                        }
                                                    }, () -> {

                                                    }, R.layout.xpopup_confirm_input).show();
                                            break;
                                        case "继续下载":
                                            continueDownload(record);
                                            break;
                                        case "暂停下载":
                                            DownloadManager.instance().pauseTask(record.getTaskId());
                                            break;
                                        case "取消下载":
                                            ToastMgr.shortBottomCenter(getContext(), "正在取消下载任务");
                                            DownloadManager.instance().cancelTask(record.getTaskId());
                                            break;
                                        case "转存到公开目录":
                                            moveToDownloadDir(record);
                                            break;
                                    }
                                }

                                private void continueDownload(DownloadRecord record) {
                                    if ("player/m3u8".equals(record.getVideoType())) {
                                        //m3u8格式
                                        String m3u8 = DownloadConfig.rootPath + File.separator + record.getFileName() + ".temp"
                                                + File.separator + "index.m3u8";
                                        if (!new File(m3u8).exists()) {
                                            new XPopup.Builder(getContext())
                                                    .asConfirm("温馨提示", "之前下载的文件已经被删除，只能重新下载，确定重新下载？", () -> reDownload(record))
                                                    .show();
                                            return;
                                        } else {
                                            DownloadManager.instance().continueDownload(record);
                                            ToastMgr.shortBottomCenter(getContext(), "已开始继续下载");
                                        }
                                    } else {
                                        //普通文件格式
                                        String temp = DownloadConfig.rootPath + File.separator + record.getFileName() + "." + record.getFileExtension() + ".temp";
                                        File tempDir = new File(temp);
                                        File[] files = tempDir.isDirectory() ? tempDir.listFiles() : null;
                                        if (!tempDir.exists() || files == null || files.length <= 0) {
                                            new XPopup.Builder(getContext())
                                                    .asConfirm("温馨提示", "之前下载的文件已经被删除，只能重新下载，确定重新下载？", () -> reDownload(record))
                                                    .show();
                                            return;
                                        } else {
                                            DownloadManager.instance().continueDownload(record);
                                            ToastMgr.shortBottomCenter(getContext(), "已开始继续下载");
                                        }
                                    }
                                }

                                @Override
                                public void onLongClick(String url, int position) {

                                }
                            })).show();
        }
    };


    public static void checkDownload(DownloadRecord record) {
        if (record == null) {
            return;
        }
        String name = record.getFullName();
        if (StringUtil.isEmpty(name)) {
            return;
        }
        if (name.endsWith(".txt") || name.endsWith(".json") || name.endsWith(".hiker")) {
            if (record.getSize() < 1024 * 1024 * 2) {
                String p = getLocalPath(record);
                HeavyTaskUtil.executeNewTask(() -> {
                    try {
                        String text = FileUtil.fileToString(p);
                        if (StringUtil.isEmpty(text)) {
                            return;
                        }
                        String originalUrl = "file://" + p;
                        ThreadTool.INSTANCE.runOnUI(() -> {
                            Activity activity = ActivityManager.getInstance().getCurrentActivity();
                            if (text.startsWith("{") && text.endsWith("}") && text.contains("\"nextColType\"")) {
                                AutoImportHelper.importRulesByTextWithDialog(activity, text, originalUrl);
                            } else if (text.startsWith("[") && text.endsWith("]") && text.contains("\"nextColType\"")) {
                                AutoImportHelper.importRulesByTextWithDialog(activity, text, originalUrl);
                            } else if (text.startsWith("{") && text.endsWith("}") && text.contains("\"find_rule\"")) {
                                AutoImportHelper.importHikerRulesByTextWithDialog(activity, text, originalUrl);
                            } else if (text.startsWith("[") && text.endsWith("]") && text.contains("\"find_rule\"")) {
                                AutoImportHelper.importHikerRulesByTextWithDialog(activity, text, originalUrl);
                            } else {
                                try {
                                    AutoImportHelper.checkAutoText(activity, text);
                                } catch (Exception ignored) {
                                }
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void moveToDownloadDir(DownloadRecord record) {
        if ("dir".equals(record.getVideoType())) {
            //转存整个目录
            new XPopup.Builder(getContext())
                    .asConfirm("温馨提示", "确定转存整个目录的文件到Download目录？注意该操作完成将删除原文件且无法恢复！", () -> {
                        LoadingPopupView loadingPopupView = new XPopup.Builder(getContext())
                                .asLoading();
                        loadingPopupView.setTitle("转存中，请稍候").show();
                        HeavyTaskUtil.executeNewTask(() -> {
                            List<DownloadRecord> records = new ArrayList<>();
                            for (DownloadRecord rule : rules) {
                                if (record.getSourcePageTitle().equals(rule.getFilm())
                                        && !FilesUtilsKt.inDownloadDir(getContext(), rule)) {
                                    String path = DownloadManager.getNormalFilePath(rule);
                                    if (StringUtil.isNotEmpty(path)) {
                                        records.add(rule);
                                    }
                                }
                            }
                            int count = records.size();
                            int deal = 1;
                            for (DownloadRecord rule : records) {
                                int p = deal;
                                ThreadTool.INSTANCE.runOnUI(() -> loadingPopupView.setTitle("转存中，请稍候" + p + "/" + count));
                                String path = DownloadManager.getNormalFilePath(rule);
                                FilesUtilsKt.copyToDownloadDir(requireContext(), path, rule.getFilm());
                                deleteRecordsSync(requireContext(), Collections.singletonList(rule));
                                deal++;
                            }
                            ThreadTool.INSTANCE.runOnUI(() -> {
                                if (getActivity() != null && !getActivity().isFinishing()) {
                                    loadingPopupView.dismiss();
                                    ToastMgr.shortCenter(getContext(), "已转存完成(" + count + ")");
                                }
                            });
                        });
                    }).show();
            return;
        }
        String normalPath = DownloadManager.getNormalFilePath(record);
        if (normalPath == null || !new File(normalPath).exists()) {
            ToastMgr.shortBottomCenter(getContext(), "当前文件不支持此操作");
            return;
        }
        LoadingPopupView loadingPopupView = new XPopup.Builder(getContext())
                .asLoading();
        loadingPopupView.setTitle("转存中，请稍候").show();
        HeavyTaskUtil.executeNewTask(() -> {
            FilesUtilsKt.copyToDownloadDir(requireContext(), normalPath, record.getFilm());
            ThreadTool.INSTANCE.runOnUI(() -> {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    loadingPopupView.dismiss();
                    new XPopup.Builder(getContext())
                            .asConfirm("转存完成", "文件已转存到下载Download目录，可以在相册等第三方应用查看，是否删除原文件？", () -> {
                                deleteByRecord(record);
                            }).show();
                }
            });
        });
    }

    private void reDownload(DownloadRecord record) {
        if (StringUtil.isEmpty(record.getSourcePageUrl()) || !record.getSourcePageUrl().startsWith("http")) {
            ToastMgr.shortBottomCenter(getContext(), "当前文件不支持重新下载");
            return;
        }
        DownloadManager.instance().cancelTask(record.getTaskId());
        String normalPath = DownloadManager.getNormalFilePath(record);
        if (normalPath != null && new File(normalPath).exists()) {
            new File(normalPath).delete();
        } else {
            String dir = DownloadManager.instance().getM3u8DownloadedDir(record);
            if (!DownloadConfig.rootPath.equals(dir)) {
                FileUtil.deleteDirs(dir);
            }
        }
        DownloadTask downloadTask = new DownloadTask(
                UUIDUtil.genUUID(), null, null, null, record.getSourcePageUrl(),
                record.getSourcePageUrl(), record.getSourcePageTitle(), 0L);
        downloadTask.setFilm(record.getFilm());
        DownloadManager.instance().addTask(downloadTask);
        ToastMgr.shortBottomCenter(getContext(), "已加入下载队列");
    }

    private void deleteByRecord(DownloadRecord record) {
        new XPopup.Builder(getContext())
                .asConfirm("温馨提示", "确认删除该下载内容吗？", () -> {
                    ToastMgr.shortBottomCenter(getContext(), "正在删除下载任务");
                    List<DownloadRecord> list = new ArrayList<>();
                    if ("dir".equals(record.getVideoType())) {
                        for (DownloadRecord rule : rules) {
                            if (record.getSourcePageTitle().equals(rule.getFilm())) {
                                list.add(rule);
                            }
                        }
                    } else {
                        list.add(record);
                    }
                    deleteRecords(getContext(), list);
                }).show();
    }

    public static void deleteRecords(Context context, List<DownloadRecord> list) {
        HeavyTaskUtil.executeNewTask(() -> deleteRecordsSync(context, list));
    }

    public static void deleteRecordsSync(Context context, List<DownloadRecord> list) {
        for (DownloadRecord downloadRecord : list) {
            try {
                if (downloadRecord.getId() > 0) {
                    downloadRecord.delete();
                }
                DownloadManager.instance().cancelTask(downloadRecord.getTaskId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String normalFile = DownloadManager.getNormalFilePath(downloadRecord);
            if (StringUtil.isNotEmpty(normalFile)) {
                try {
                    if (FilesUtilsKt.inDownloadDir(context, downloadRecord)) {
                        FilesUtilsKt.deleteFileByPath(context, downloadRecord.getFailedReason());
                    } else {
                        new File(normalFile).delete();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File file = new File(downloadRecord.getRootPath() + File.separator + downloadRecord.getFileName());
                if (file.exists() && !file.isDirectory()) {
                    FileUtil.deleteFile(file.getAbsolutePath());
                } else {
                    String dir = DownloadManager.instance().getM3u8DownloadedDir(downloadRecord);
                    if (!DownloadConfig.rootPath.equals(dir)) {
                        FileUtil.deleteFile(dir);
                    }
                }
                //temp目录
                String temp = DownloadConfig.rootPath + File.separator + downloadRecord.getFileName() + "." + downloadRecord.getFileExtension() + ".temp";
                FileUtil.deleteDirs(temp);
                String temp2 = DownloadConfig.rootPath + File.separator + downloadRecord.getFileName() + ".temp";
                FileUtil.deleteDirs(temp2);
            }
        }
    }

    public static void cancelRecords(Context context, List<DownloadRecord> list) {
        for (DownloadRecord record : list) {
            DownloadManager.instance().cancelTask(record.getTaskId());
        }
    }


    private static String getLocalPath(DownloadRecord downloadRecord) {
        String normalPath = DownloadManager.getNormalFilePath(downloadRecord);
        if (normalPath != null) {
            return normalPath;
        }
        String dir = DownloadManager.instance().getM3u8DownloadedDir(downloadRecord);
        String path = dir + File.separator + "video." + downloadRecord.getFileExtension();
        if (new File(path).exists()) {
            return path;
        }
        return dir + File.separator + downloadRecord.getFileName() + "." + downloadRecord.getFileExtension();
    }

    public static String getLocalFileName(DownloadRecord record) {
        String normalPath = DownloadManager.getNormalFilePath(record);
        if (normalPath != null) {
            return new File(normalPath).getName();
        }
        return record.getFullName();
    }

    void batchDelete() {
        new XPopup.Builder(getContext())
                .asConfirm("温馨提示", "请点击下载项来勾选要删除/取消的内容", () -> {
                    isMultiDeleting = true;
                    if (getActivity() instanceof DownloadRecordsActivity) {
                        ((DownloadRecordsActivity) getActivity()).showDeleteBtn();
                    } else {
                        ToastMgr.shortBottomCenter(getContext(), "获取父页面失败");
                    }
                }).show();
    }

    void saveOrders() {
        for (int i = 0; i < rules.size(); i++) {
            rules.get(i).setOrder(rules.size() - i);
        }
        LitePal.saveAllAsync(rules).listen(success -> {
            if (getActivity() != null && !getActivity().isFinishing()) {
                ToastMgr.shortBottomCenter(getContext(), "排序结果保存成功");
                isSorting = false;
            }
        });
    }


    private ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlag = 0;
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            return makeMovementFlags(dragFlag, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(rules, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(rules, i, i - 1);
                }
            }
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //侧滑删除可以使用；
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return isSorting && !isMultiDeleting;
        }

        /**
         * 长按选中Item的时候开始调用
         * 长按高亮
         * @param viewHolder
         * @param actionState
         */
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                viewHolder.itemView.setBackgroundColor(getContext().getResources().getColor(R.color.gray_rice));
                //获取系统震动服务//震动70毫秒
                try {
                    Vibrator vib = (Vibrator) requireActivity().getSystemService(Service.VIBRATOR_SERVICE);
                    if (vib != null) {
                        vib.vibrate(70);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        /**
         * 手指松开的时候还原高亮
         * @param recyclerView
         * @param viewHolder
         */
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
//            viewHolder.itemView.setBackgroundColor(0);
            adapter.notifyDataSetChanged();
        }
    });
}

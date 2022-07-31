package com.example.hikerview.ui.browser;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.constants.Media;
import com.example.hikerview.constants.MediaType;
import com.example.hikerview.model.AdBlockUrl;
import com.example.hikerview.model.SharedAdUrl;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseSlideActivity;
import com.example.hikerview.ui.browser.model.AdUrlBlocker;
import com.example.hikerview.ui.browser.model.DetectedMediaResult;
import com.example.hikerview.ui.browser.model.DetectorManager;
import com.example.hikerview.ui.browser.model.MediaListModel;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.download.DownloadChooser;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.view.DialogBuilder;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.utils.CleanMessageUtil;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.MyStatusBarUtil;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.WebUtil;
import com.lxj.xpopup.XPopup;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2019/10/5
 * 时间：At 10:09
 */
public class MediaListActivity extends BaseSlideActivity implements View.OnClickListener, BaseCallback<DetectedMediaResult> {
    private static final String TAG = "MediaListActivity";
    private Button media_list_all, media_list_video, media_list_music, media_list_image, media_list_html, media_list_other, media_list_block;
    private MediaListAdapter adapter;
    private MediaListModel mediaListModel;
    private List<DetectedMediaResult> results = new ArrayList<>();
    private Media mediaType = null;
    private boolean hasDestroy = false;

    @Override
    protected View getBackgroundView() {
        return findView(R.id.media_list_window);
    }

    @Override
    protected int initLayout(Bundle savedInstanceState) {
        return R.layout.activit_media_list;
    }

    @Override
    protected void initView2() {
        RecyclerView recyclerView = findView(R.id.media_list_recycler_view);
        media_list_all = findView(R.id.media_list_all);
        media_list_all.setOnClickListener(this);
        media_list_video = findView(R.id.media_list_video);
        media_list_video.setOnClickListener(this);
        media_list_music = findView(R.id.media_list_music);
        media_list_music.setOnClickListener(this);
        media_list_image = findView(R.id.media_list_image);
        media_list_image.setOnClickListener(this);
        media_list_html = findView(R.id.media_list_html);
        media_list_html.setOnClickListener(this);
        media_list_other = findView(R.id.media_list_other);
        media_list_other.setOnClickListener(this);
        media_list_block = findView(R.id.media_list_block);
        media_list_block.setOnClickListener(this);
        adapter = new MediaListAdapter(getContext(), results, getIntent().getStringExtra("url"));
        adapter.setOnItemClickListener(onItemClickListener);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        mediaListModel = new MediaListModel();
        //初始化高度
        int marginTop = MyStatusBarUtil.getStatusBarHeight(getContext()) + DisplayUtil.dpToPx(getContext(), 86);
        View bg = findView(R.id.media_list_bg);
        findView(R.id.media_list_window).setOnClickListener(view -> finish());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bg.getLayoutParams();
        layoutParams.topMargin = marginTop;
        bg.setLayoutParams(layoutParams);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        loading(true);
        mediaListModel.params(getContext(), mediaType)
                .process("", this);
        HeavyTaskUtil.executeNewTask(() -> {
            while (!hasDestroy) {
                try {
                    Thread.sleep(7000);
                    mediaListModel.params(getContext(), mediaType)
                            .process("", MediaListActivity.this);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    private MediaListAdapter.OnItemClickListener onItemClickListener = new MediaListAdapter.OnItemClickListener() {
        @Override
        public void onClick(View view, int position) {
            try {
                if (position < 0 || position >= results.size()) {
                    ToastMgr.shortBottomCenter(getContext(), "获取数据失败！position cannot be " + position);
                    return;
                }
                DetectedMediaResult mediaResult = results.get(position);
                if (mediaResult.getMediaType().getName().equals(MediaType.VIDEO.getName())) {
                    String videoUrl = PlayerChooser.decorateHeader(getActivity(), getIntent().getStringExtra("url"), mediaResult.getUrl());
                    PlayerChooser.startPlayer(getContext(), getIntent().getStringExtra("title"), videoUrl);
                    HeavyTaskUtil.updateHistoryVideoUrl(getIntent().getStringExtra("url"), videoUrl);
                } else if (mediaResult.getMediaType().getName().equals(MediaType.MUSIC.getName())) {
                    String videoUrl = PlayerChooser.decorateHeader(getActivity(), getIntent().getStringExtra("url"), mediaResult.getUrl());
                    PlayerChooser.startPlayer(getContext(), getIntent().getStringExtra("title"), videoUrl);
                    HeavyTaskUtil.updateHistoryVideoUrl(getIntent().getStringExtra("url"), videoUrl);
                } else if (mediaResult.getMediaType().getName().equals(MediaType.IMAGE.getName())) {
                    String url = results.get(position).getUrl();
                    List<DetectedMediaResult> images = DetectorManager.getInstance().getDetectedMediaResults(new Media(Media.IMAGE));
                    List<Object> imageUrls = new ArrayList<>(images.size());
                    int pos = 0;
                    for (int i = 0; i < images.size(); i++) {
                        DetectedMediaResult result = images.get(i);
                        imageUrls.add(result.getUrl());
                        if (url.equals(result.getUrl())) {
                            pos = i;
                        }
                    }
                    new XPopup.Builder(getContext()).asImageViewer(null, pos, imageUrls, null, new PopImageLoaderNoView(getIntent().getStringExtra("url")))
                            .show();
                } else {
                    WebUtil.goWeb(getContext(), mediaResult.getUrl());
                }
            } catch (Exception e) {
                //位置发生了变化
                e.printStackTrace();
            }
        }

        @Override
        public void onLongClick(View view, int position) {
            DetectedMediaResult mediaResult = results.get(position);
            String[] titles;
            if (Media.BLOCK.equals(mediaResult.getMediaType().getName())) {
                titles = new String[]{"复制链接", "播放资源", "下载资源", "拦截网址", "取消拦截", "外部打开", "查看完整链接", "保存该网站的拦截规则供分享"};
            } else {
                titles = new String[]{"复制链接", "播放资源", "下载资源", "拦截网址", "外部打开", "查看完整链接", "保存该网站的拦截规则供分享"};
            }
            new XPopup.Builder(getContext())
                    .asCenterList("请选择操作", titles, ((option, text) -> {
                        switch (text) {
                            case "保存该网站的拦截规则供分享":
                                saveAdUrlForDom(null);
                                ToastMgr.shortBottomCenter(getContext(), "已保存，在分享首页规则、搜索引擎、书签时可以同时分享该规则");
                                break;
                            case "外部打开":
                                ShareUtil.findChooserToDeal(getContext(), mediaResult.getUrl());
                                break;
                            case "播放资源":
                                HeavyTaskUtil.updateHistoryVideoUrl(getIntent().getStringExtra("url"), mediaResult.getUrl());
                                PlayerChooser.startPlayer(getContext(), getIntent().getStringExtra("title"), mediaResult.getUrl());
                                break;
                            case "下载资源":
                                DownloadChooser.startDownload(MediaListActivity.this, getIntent().getStringExtra("title"), mediaResult.getUrl());
                                break;
                            case "复制链接":
                                try {
                                    ClipboardUtil.copyToClipboard(getContext(), mediaResult.getUrl());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "查看完整链接":
                                DialogBuilder.createInputConfirm(getContext(), "查看完整链接", mediaResult.getUrl(), text1 -> {}).show();
                                break;
                            case "拦截网址":
                                String url = mediaResult.getUrl().replace("http://", "").replace("https://", "");
                                final View view1 = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_block_url_add, null, false);
                                final EditText titleE = view1.findViewById(R.id.block_add_text);
                                View block_add_dom = view1.findViewById(R.id.block_add_dom);
                                View block_add_url = view1.findViewById(R.id.block_add_url);
                                View global = view1.findViewById(R.id.block_add_global);
                                View domain = view1.findViewById(R.id.block_add_domain);
                                block_add_dom.setOnClickListener(v -> {
                                    titleE.setText(StringUtil.getDom(url).split(":")[0]);
                                    block_add_url.setBackground(getDrawable(R.drawable.button_layer));
                                    block_add_dom.setBackground(getDrawable(R.drawable.button_layer_red));
                                });
                                block_add_url.setOnClickListener(v -> {
                                    titleE.setText(url);
                                    block_add_dom.setBackground(getDrawable(R.drawable.button_layer));
                                    block_add_url.setBackground(getDrawable(R.drawable.button_layer_red));
                                });
                                global.setOnClickListener(v -> {
                                    titleE.setText(titleE.getText().toString().split("@domain=")[0]);
                                    domain.setBackground(getDrawable(R.drawable.button_layer));
                                    global.setBackground(getDrawable(R.drawable.button_layer_red));
                                });
                                domain.setOnClickListener(v -> {
                                    String dom = StringUtil.getDom(getIntent().getStringExtra("url"));
                                    titleE.setText((titleE.getText().toString().split("@domain=")[0] + "@domain=" + dom));
                                    global.setBackground(getDrawable(R.drawable.button_layer));
                                    domain.setBackground(getDrawable(R.drawable.button_layer_red));
                                });
                                titleE.setHint("请输入要拦截的网址");
                                titleE.setText(StringUtil.getDom(url).split(":")[0]);
                                new AlertDialog.Builder(getContext())
                                        .setTitle("新增网址拦截")
                                        .setView(view1)
                                        .setCancelable(true)
                                        .setPositiveButton("拦截", (dialog, which) -> {
                                            String title = titleE.getText().toString();
                                            if (TextUtils.isEmpty(title)) {
                                                ToastMgr.shortBottomCenter(getContext(), "请输入要拦截的网址");
                                            } else {
                                                AdUrlBlocker.instance().addUrl(title);
                                                CleanMessageUtil.clearWebViewCache(getActivity());
                                                HeavyTaskUtil.executeNewTask(() -> saveAdUrlForDom(title));
                                                ToastMgr.shortBottomCenter(getContext(), "保存成功");
                                            }
                                        }).setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
                                break;
                            case "取消拦截":
                                try {
                                    long id = Long.parseLong(mediaResult.getMediaType().getType());
                                    if (id == 0) {
                                        ToastMgr.shortBottomCenter(getContext(), "当前链接被远程订阅的规则拦截，无法取消拦截");
                                    } else if (id > 0) {
                                        AdBlockUrl blockUrl = LitePal.find(AdBlockUrl.class, id);
                                        if (blockUrl == null) {
                                            ToastMgr.shortBottomCenter(getContext(), "获取拦截规则失败！");
                                            return;
                                        }
                                        AdUrlBlocker.instance().removeUrl(blockUrl.getUrl());
                                        HeavyTaskUtil.executeNewTask(() -> saveAdUrlForDom(null));
                                        ToastMgr.shortBottomCenter(getContext(), "已删除拦截规则：" + blockUrl.getUrl());
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }))
                    .show();
        }
    };


    @Override
    public void onClick(View view) {
        media_list_all.setBackground(getResources().getDrawable(R.drawable.button_layer));
        media_list_video.setBackground(getResources().getDrawable(R.drawable.button_layer));
        media_list_music.setBackground(getResources().getDrawable(R.drawable.button_layer));
        media_list_image.setBackground(getResources().getDrawable(R.drawable.button_layer));
        media_list_html.setBackground(getResources().getDrawable(R.drawable.button_layer));
        media_list_other.setBackground(getResources().getDrawable(R.drawable.button_layer));
        media_list_block.setBackground(getResources().getDrawable(R.drawable.button_layer));
        view.setBackground(getResources().getDrawable(R.drawable.button_layer_red));
        switch (view.getId()) {
            case R.id.media_list_all:
                mediaType = null;
                break;
            case R.id.media_list_video:
                mediaType = new Media(MediaType.VIDEO);
                break;
            case R.id.media_list_music:
                mediaType = new Media(MediaType.MUSIC);
                break;
            case R.id.media_list_image:
                mediaType = new Media(MediaType.IMAGE);
                break;
            case R.id.media_list_html:
                mediaType = new Media(MediaType.HTML);
                break;
            case R.id.media_list_other:
                mediaType = new Media(MediaType.OTHER);
                break;
            case R.id.media_list_block:
                mediaType = new Media(MediaType.BLOCK);
                break;
        }
        loading(true);
        mediaListModel.params(getContext(), mediaType)
                .process("", this);
    }

    @Override
    public void bindArrayToView(String actionType, List<DetectedMediaResult> data) {
        runOnUiThread(() -> {
            results.clear();
            results.addAll(data);
            adapter.notifyDataSetChanged();
            loading(false);
        });
    }

    @Override
    public void bindObjectToView(String actionType, DetectedMediaResult data) {

    }

    @Override
    public void error(String title, String msg, String code, Exception e) {
        runOnUiThread(() -> {
            loading(false);
            DebugUtil.showErrorMsg(this, getContext(), title, msg, code, e);
        });
    }

    @Override
    public void loading(boolean isLoading) {

    }

    /**
     * 保存该网站关联的拦截规则到数据库
     *
     * @param title 新增加的
     */
    private void saveAdUrlForDom(@Nullable String title) {
        List<DetectedMediaResult> blocks = DetectorManager.getInstance().getDetectedMediaResults(new Media(MediaType.BLOCK));
        String url1 = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(url1)) {
            return;
        }
        String dom = StringUtil.getDom(url1);
        if (TextUtils.isEmpty(dom)) {
            return;
        }
        List<String> blockUrls = new ArrayList<>(blocks.size());
        for (int i = 0; i < blocks.size(); i++) {
            DetectedMediaResult mediaResult1 = blocks.get(i);
            try {
                long id = Long.parseLong(mediaResult1.getMediaType().getType());
                if (id > 0) {
                    AdBlockUrl blockUrl = LitePal.find(AdBlockUrl.class, id);
                    if (blockUrl != null && !TextUtils.isEmpty(blockUrl.getUrl())) {
                        blockUrls.add(blockUrl.getUrl());
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(title)) {
            blockUrls.add(title);
        }
        String rules = CollectionUtil.listToString(blockUrls, "##");
        Log.d(TAG, "saveAdUrlForDom: rules===>" + rules);
        List<SharedAdUrl> sharedAdUrls = LitePal.where("dom = ?", dom).limit(1).find(SharedAdUrl.class);
        if (CollectionUtil.isEmpty(sharedAdUrls)) {
            SharedAdUrl sharedAdUrl = new SharedAdUrl();
            sharedAdUrl.setDom(dom);
            sharedAdUrl.setBlockUrls(rules);
            sharedAdUrl.save();
        } else {
            sharedAdUrls.get(0).setBlockUrls(rules);
            sharedAdUrls.get(0).save();
        }
    }

    @Override
    protected void onDestroy() {
        hasDestroy = true;
        super.onDestroy();
    }
}

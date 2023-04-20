package com.example.hikerview.ui.browser;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.hikerview.R;
import com.example.hikerview.model.ViewCollectionExtraData;
import com.example.hikerview.service.parser.BaseParseCallback;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.service.parser.LazyRuleParser;
import com.example.hikerview.ui.base.BaseStatusTransNavigationActivity;
import com.example.hikerview.ui.browser.model.PicturePageData;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.detail.DetailUIHelper;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.home.model.article.extra.BaseExtra;
import com.example.hikerview.ui.home.model.article.extra.LongClickExtra;
import com.example.hikerview.ui.home.view.ClickArea;
import com.example.hikerview.ui.miniprogram.data.HistoryDTO;
import com.example.hikerview.ui.miniprogram.service.HistoryMemoryService;
import com.example.hikerview.ui.setting.file.FileDetailAdapter;
import com.example.hikerview.ui.setting.file.FileDetailPopup;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.view.CenterLayoutManager;
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup;
import com.example.hikerview.ui.view.PopImageLoader;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.ui.view.SmartRefreshLayout;
import com.example.hikerview.ui.view.popup.MyXpopup;
import com.example.hikerview.ui.view.popup.PictureProgressBottomPopup;
import com.example.hikerview.ui.view.popup.XPopupImageLoader;
import com.example.hikerview.utils.AlertNewVersionUtil;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.DataTransferUtils;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.GlideUtil;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.ImgUtil;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ThreadTool;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.smarx.notchlib.NotchScreenManager;

import org.adblockplus.libadblockplus.android.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 * 作者：By 15968
 * 日期：On 2019/10/5
 * 时间：At 10:09
 */
public class PictureListActivity extends BaseStatusTransNavigationActivity {
    private static final String TAG = "PictureListActivity";
    private RecyclerView recyclerView;
    private List<String> pics = new ArrayList<>();
    private List<VideoChapter> chapters;
    private SmartRefreshLayout smartRefreshLayout;
    private PictureListAdapter adapter;
    private TextView page_indicator;
    private LinearLayoutManager linearLayoutManager;
    private int indexNow = -1;
    private int scrollPos = -1;
    private String CUrl, MTitle;
    private HistoryDTO history;
    private float touchX;
    private float touchY;
    private Map<Integer, PicturePageData> indicatorMap = new HashMap<>();
    private BasePopupView operationDialog;
    private int loadingPage;
    private int maxLoadedPage = 0;
    //线程安全，单生产者多消费者效率更高
    private final ConcurrentLinkedQueue<String> preLoadList = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();
    private FileDetailPopup detailPopup;
    private int count = 0;
    private final AtomicInteger preloading = new AtomicInteger(0);
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            try {
                if (isFinishing()) {
                    this.cancel();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (history != null) {
                HistoryMemoryService.INSTANCE.update(history);
            }
            if (count > 100000000) {
                count = 0;
            }
            count++;
            if (count % 2 == 1) {
                //每次只取4个，因为Glide newSourceExecutor 最大只有4个线程
                for (int i = 0; i < 4; i++) {
                    if (preloading.get() >= 4) {
                        //还有超过4张图片在加载中，等待下一次循环再看
                        break;
                    }
                    String url = preLoadList.poll();
                    if (url == null) {
                        //没了，等待下一次循环
                        break;
                    }
                    if (isFinishing()) {
                        this.cancel();
                        return;
                    }
                    preloading.incrementAndGet();
                    Timber.d("preload glide: %s", url);
                    Glide.with(getContext())
                            .load(GlideUtil.getGlideUrl(getMyUrl(), url))
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Timber.d("预加载失败：%s", e == null ? "" : e.getMessage());
                                    preloading.decrementAndGet();
                                    return true;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Timber.d("预加载成功");
                                    preloading.decrementAndGet();
                                    return true;
                                }
                            }).preload();
                }
            }
        }
    };

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        getIntent().putExtra("horizontal", PreferenceMgr.getBoolean(getContext(), "picsHorizontal", false));
        setContentView(R.layout.activit_pic_list);
    }

    @Override
    protected void initView2() {
        recyclerView = findView(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        page_indicator = findView(R.id.page_indicator);
        //设置沉浸式状态栏，在MIUI系统中，状态栏背景透明。原生系统中，状态栏背景半透明。
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        NotchScreenManager.getInstance().setDisplayInNotch(this);
    }

    private String getMyUrl() {
        return HttpParser.getFirstPageUrl(getIntent().getStringExtra("url"));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CUrl = getIntent().getStringExtra("CUrl");
        MTitle = getIntent().getStringExtra("MTitle");
        loadingPage = getIntent().getIntExtra("nowPage", 0);
        addPics(loadingPage, false, getPics(), getIntent().getStringExtra("title"));
        adapter = new PictureListAdapter(getContext(), pics, getMyUrl(), getIntent().getBooleanExtra("horizontal", false));
        adapter.setOnItemClickListener(onItemClickListener);

        boolean horizontal = getIntent().getBooleanExtra("horizontal", false);
        if (horizontal) {
            linearLayoutManager = new CenterLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        } else {
            linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        }
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        smartRefreshLayout = findView(R.id.refresh_layout);
        smartRefreshLayout.setEnableRefresh(false);

        long chaptersKey = getIntent().getLongExtra("chapters", 0);
        if (chaptersKey > 0) {
            //取出来，然后清掉
            chapters = PlayerChooser.getChapterMap().get(chaptersKey);
            PlayerChooser.getChapterMap().delete(chaptersKey);
        }

        if (chapters != null && chapters.size() > 1) {
            smartRefreshLayout.setOnLoadMoreListener(this::nextPage);
        } else {
            smartRefreshLayout.setEnableAutoLoadMore(false);
            smartRefreshLayout.setEnableLoadMore(false);
        }
        try {
            history = HistoryMemoryService.INSTANCE.getHistory(MTitle, CUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = linearLayoutManager == null ? 0 : linearLayoutManager.findLastVisibleItemPosition() + 1;
                if (position >= pics.size()) {
                    position = pics.size() - 1;
                }
                Timber.d("onScrolled: %s, %s", position, scrollPos);
                if (scrollPos != position && position >= 0) {
                    scrollPos = position;
                    indexNow = position;
                    indexNow++;
                    PicturePageData pageData = indicatorMap.get(position);
                    if (pageData == null) {
                        page_indicator.setText((indexNow + "/" + pics.size()));
                        page_indicator.setTag(null);
                    } else {
                        Date currentTime = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                        int idx = Math.max(indexNow - pageData.getPreSize(), 0);
                        int lastLoadedPage = maxLoadedPage;
                        if (loadingPage < 0 && idx >= Math.floor((pageData.getPics().size() + 0D) / 2) && lastLoadedPage < pageData.getPageNow() + 1) {
                            Timber.d("will preload: lastLoadedPage: " + lastLoadedPage + ", pageNow: " + pageData.getPageNow());
                            preloadNow(maxLoadedPage);
                        }
                        String ind = idx + "/" + pageData.getPics().size() + " " + formatter.format(currentTime);
                        if (StringUtil.isEmpty(pageData.getTitle())) {
                            page_indicator.setText(ind);
                        } else {
                            page_indicator.setText((pageData.getTitle() + " " + ind));
                        }
                        page_indicator.setTag(pageData);
                    }
                    int pageIndex = indexNow - (pageData == null ? 0 : pageData.getPreSize());
                    if (pageIndex < 0) {
                        pageIndex = 0;
                    }
                    Timber.d("onScrolled: pageIndex: %s, %s", pageIndex, indexNow);
                    memoryPageIndex(pageIndex, pageData);
                }
            }
        });
        if (getIntent().getBooleanExtra("fromLastPage", false)) {
            int pos = getScrollPos();
            if (pos > 0) {
                recyclerView.scrollToPosition(pos);
            }
        }
        timer.schedule(timerTask, 0, 1000);
        AlertNewVersionUtil.alert(this);

        page_indicator.setOnClickListener(v -> {
            if (!isTouchEdge() && v.getTag() != null) {
                showProgressPopup();
            }
        });
    }

    private ArrayList<String> getPics() {
        ArrayList<String> p = getIntent().getStringArrayListExtra("pics");
        if (p != null) {
            return p;
        }
        return DataTransferUtils.INSTANCE.getPics();
    }

    private Object getRule() {
        String r = getIntent().getStringExtra("rule");
        if (StringUtil.isNotEmpty(r)) {
            return JSON.parseObject(r, ArticleListRule.class);
        }
        r = DataTransferUtils.INSTANCE.loadCacheString("tempVideoRule");
        if (StringUtil.isNotEmpty(r)) {
            return JSON.parseObject(r, ArticleListRule.class);
        }
        return new JSONObject();
    }

    private void showProgressPopup() {
        PicturePageData pageData = (PicturePageData) page_indicator.getTag();
        int startProgress = Math.max(indexNow - pageData.getPreSize(), 0);
        int startPos = scrollPos;
        new XPopup.Builder(getContext())
                .hasShadowBg(false)
                .enableDrag(false)
                .asCustom(new PictureProgressBottomPopup(getContext())
                        .withProgress(pageData.getPics().size(), startProgress)
                        .withListener(progress -> {
                            int newPos = progress - startProgress + startPos - 2;
                            if (newPos < 0) {
                                newPos = 0;
                            }
                            recyclerView.scrollToPosition(newPos);
                        })).show();
    }

    private int getScrollPos() {
        if (history != null) {
            String extra = history.getExtraData();
            ViewCollectionExtraData extraData = new ViewCollectionExtraData(-1);
            if (StringUtil.isNotEmpty(extra)) {
                extraData = JSON.parseObject(extra, ViewCollectionExtraData.class);
            }
            if (extraData != null && extraData.getPageIndex() > 2 && extraData.getPageIndex() <= pics.size()) {
                return extraData.getPageIndex() - 2;
            }
        }
        return 0;
    }

    private void memoryPageIndex(int pageIndex, PicturePageData pageData) {
        if (history != null) {
            String extra = history.getExtraData();
            ViewCollectionExtraData extraData = null;
            if (StringUtil.isNotEmpty(extra)) {
                extraData = JSON.parseObject(extra, ViewCollectionExtraData.class);
            }
            if (extraData == null) {
                extraData = new ViewCollectionExtraData(-1);
            }
            extraData.setPageIndex(pageIndex);
            history.setExtraData(JSON.toJSONString(extraData));
            memoryPage(pageData == null ? -1 : pageData.getPageNow());
        }
    }

    private void memoryPage(int pos) {
        try {
            if (pos < 0 || CollectionUtil.isEmpty(chapters) || pos > chapters.size() - 1) {
                return;
            }
            String title = chapters.get(pos).getTitle();
            String click = title.split("-")[title.split("-").length - 1];
            if (StringUtil.isNotEmpty(CUrl) && StringUtil.isNotEmpty(MTitle)) {
                if (history != null) {
                    history.setClickPos(pos);
                    history.setClickText(click);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除图片标记，避免图片无法显示
     *
     * @param urls
     */
    private void addPics(int index, boolean preload, List<String> urls, String title) {
        if (CollectionUtil.isEmpty(urls)) {
            return;
        }
        int lastPicsSize = pics.size();
        PicturePageData pageData = new PicturePageData(urls, title);
        pageData.setPageNow(index);
        loadingPage = -1;
        maxLoadedPage = Math.max(maxLoadedPage, index);
        pageData.setPreSize(pics.size());
        pics.addAll(Stream.of(urls).map(UrlDetector::clearTag).collect(Collectors.toList()));
        for (int i = lastPicsSize; i < pics.size(); i++) {
            indicatorMap.put(i, pageData);
        }
        indexNow = linearLayoutManager == null ? 0 : linearLayoutManager.findLastVisibleItemPosition() + 1;
        if (indexNow >= pics.size()) {
            indexNow = pics.size() - 1;
        }
        if (indexNow < 0) {
            indexNow = 0;
        }
        indexNow++;
        if (!preload) {
            Date currentTime = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            int idx = Math.max(indexNow - pageData.getPreSize(), 0);
            String ind = idx + "/" + pageData.getPics().size() + " " + formatter.format(currentTime);
            if (StringUtil.isEmpty(pageData.getTitle())) {
                page_indicator.setText(ind);
            } else {
                page_indicator.setText((pageData.getTitle() + " " + ind));
            }
        }
        //预加载，提前缓存图片到本地，优先保证第一张图的加载，因此延后1秒开始缓存
        recyclerView.postDelayed(() -> {
            if (isFinishing()) {
                return;
            }
            preLoadList.addAll(urls);
        }, 1000);
    }

    private void loadPage(int index, boolean preload) {
        String itemTitle = DetailUIHelper.getTitleText(chapters.get(index).getMemoryTitle());
        if (StringUtil.isNotEmpty(chapters.get(index).getCodeAndHeader()) && StringUtil.isNotEmpty(chapters.get(index).getOriginalUrl())) {
            String[] lazyRule = chapters.get(index).getOriginalUrl().split("@lazyRule=");
            if (lazyRule.length != 2) {
                int oldSize = pics.size();
                addPics(index, preload, new ArrayList<>(Arrays.asList(lazyRule[0].replace("pics://", "").split("&&"))), itemTitle);
                adapter.notifyItemRangeInserted(oldSize, pics.size() - oldSize);
                smartRefreshLayout.finishLoadMore();
            } else {
                dealLazyRule(index, preload, lazyRule, chapters.get(index).getCodeAndHeader(), itemTitle);
            }
        } else {
            String url = chapters.get(index).getUrl();
            if (StringUtil.isEmpty(url) || !url.startsWith("pics://")) {
                ToastMgr.shortBottomCenter(getContext(), "链接格式有误：" + url);
                smartRefreshLayout.finishLoadMore();
                loadingPage = -1;
                return;
            }
            int oldSize = pics.size();
            addPics(index, preload, new ArrayList<>(Arrays.asList(url.replace("pics://", "").split("&&"))), itemTitle);
            adapter.notifyItemRangeInserted(oldSize, pics.size() - oldSize);
            smartRefreshLayout.finishLoadMore();
        }
    }

    private void preloadNow(int index) {
        if (CollectionUtil.isEmpty(chapters) || chapters.size() <= index + 1) {
            return;
        }
        loadingPage = index + 1;
        loadPage(loadingPage, true);
    }

    private void nextPage(RefreshLayout refreshLayout) {
        if (CollectionUtil.isEmpty(chapters) || maxLoadedPage >= chapters.size() - 1) {
//            ToastMgr.shortBottomCenter(getContext(), "没有下一页啦！");
            refreshLayout.finishLoadMore();
            return;
        }
        if (loadingPage >= maxLoadedPage + 1) {
            return;
        }
        int i = maxLoadedPage;
        int nowPage = i + 1;
        loadingPage = nowPage;
        chapters.get(i).setUse(false);
        chapters.get(i + 1).setUse(true);
        //足迹
        memoryPage(nowPage);
        loadPage(nowPage, false);
        refreshLayout.finishLoadMore();
    }

    /**
     * 处理动态解析
     *
     * @param lazyRule
     * @param codeAndHeader
     */
    private void dealLazyRule(int index, boolean preload, String[] lazyRule, String codeAndHeader, String itemTitle) {
        if (lazyRule.length < 2) {
            ToastMgr.shortBottomCenter(getContext(), "动态解析规则有误");
            loadingPage = -1;
            return;
        } else if (lazyRule.length > 2) {
            lazyRule = new String[]{lazyRule[0], StringUtil.arrayToString(lazyRule, 1, "@lazyRule=")};
        }
        LazyRuleParser.parse(getActivity(), getRule(), lazyRule, codeAndHeader, getMyUrl(), new BaseParseCallback<String>() {
            @Override
            public void start() {

            }

            @Override
            public void success(String res) {
//                Log.d(TAG, "dealLazyRule: " + res);
                int oldSize = pics.size();
                addPics(index, preload, new ArrayList<>(Arrays.asList(res.replace("pics://", "").split("&&"))), itemTitle);
                adapter.notifyItemRangeInserted(oldSize, pics.size() - oldSize);
                smartRefreshLayout.finishLoadMore();
            }

            @Override
            public void error(String msg) {
                loadingPage = -1;
            }
        });
    }

    private VideoChapter getChapter(int position) {
        PicturePageData pageData = indicatorMap.get(position);
        if (pageData != null && CollectionUtil.isNotEmpty(chapters) && pageData.getPageNow() < chapters.size()) {
            return chapters.get(pageData.getPageNow());
        }
        return null;
    }

    private List<LongClickExtra> getLongClick(int position) {
        VideoChapter videoChapter = getChapter(position);
        if (videoChapter != null && StringUtil.isNotEmpty(videoChapter.getExtra())) {
            BaseExtra baseExtra = JSON.parseObject(videoChapter.getExtra(), BaseExtra.class);
            if (baseExtra != null && CollectionUtil.isNotEmpty(baseExtra.getLongClick())) {
                //规则自定义的长按操作
                return baseExtra.getLongClick();
            }
        }
        return null;
    }

    private PictureListAdapter.OnItemClickListener onItemClickListener = new PictureListAdapter.OnItemClickListener() {

        @Override
        public void onLongClick(View view, int position) {
            if (isAlmostCenter()) {
                if (operationDialog != null && operationDialog.isShow()) {
                    return;
                }
                String[] ops;
                List<LongClickExtra> longClick = getLongClick(position);
                if (CollectionUtil.isNotEmpty(longClick)) {
                    List<String> clicks = Stream.of(longClick).map(LongClickExtra::getTitle).toList();
                    clicks.add("全屏查看");
                    clicks.add("图片详情");
                    clicks.add("阅读进度");
                    clicks.add("保存图片");
                    clicks.add("刷新图片");
                    clicks.add("保存全部");
                    clicks.add("竖向横向");
                    clicks.add("点击翻页");
                    ops = CollectionUtil.toStrArray(clicks);
                } else {
                    ops = new String[]{"全屏查看", "图片详情", "阅读进度", "保存图片", "刷新图片", "保存全部", "竖向横向", "点击翻页"};
                }
                new XPopup.Builder(getContext())
                        .asCustom(new CustomCenterRecyclerViewPopup(getContext())
                                .withTitle("请选择操作")
                                .with(ops, ops.length > 4 ? 2 : 1, new CustomCenterRecyclerViewPopup.ClickListener() {
                                    @Override
                                    public void click(String text, int option) {
                                        switch (text) {
                                            case "全屏查看":
                                                ImageView imageView = null;
                                                if (view instanceof ImageView) {
                                                    imageView = (ImageView) view;
                                                }
                                                XPopupImageLoader imageLoader;
                                                if (imageView == null) {
                                                    imageLoader = new PopImageLoaderNoView(getMyUrl());
                                                    ((PopImageLoader) imageLoader).setSelectPos(position);
                                                } else {
                                                    imageLoader = new PopImageLoader(imageView, getMyUrl());
                                                }
                                                List<Object> imageUrls = Stream.of(pics).map(p -> (Object) p).toList();
                                                new MyXpopup().Builder(getContext())
                                                        .asImageViewer(imageView, position, imageUrls, false,
                                                                true, getResources().getColor(R.color.gray_rice), -1, -1, true, Color.rgb(32, 36, 46), (popupView, position2) -> {
                                                                    // 作用是当Pager切换了图片，需要更新源View
                                                                    try {
                                                                        popupView.updateSrcView(null);
                                                                        if (position2 < adapter.getList().size() && position2 >= 0) {
                                                                            recyclerView.scrollToPosition(position2);
                                                                        }
                                                                    } catch (Throwable e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }, imageLoader)
                                                        .show();
                                                break;
                                            case "阅读进度":
                                                if (page_indicator.getTag() != null) {
                                                    showProgressPopup();
                                                }
                                                break;
                                            case "竖向横向":
                                                boolean picsHorizontal = PreferenceMgr.getBoolean(getContext(), "picsHorizontal", false);
                                                new XPopup.Builder(getContext())
                                                        .asCenterList("竖向横向显示", new String[]{"竖向显示", "横向显示"}, null, picsHorizontal ? 1 : 0, (p2, t2) -> {
                                                            PreferenceMgr.put(getContext(), "picsHorizontal", p2 == 1);
                                                            new XPopup.Builder(getContext())
                                                                    .asConfirm("温馨提示", "设置成功，因技术原因暂时需要返回前一个页面手动重进", () -> {
                                                                        finish();
                                                                    }).show();
                                                        }).show();
                                                break;
                                            case "点击翻页":
                                                boolean picsClick = PreferenceMgr.getBoolean(getContext(), "picsClick", false);
                                                new XPopup.Builder(getContext())
                                                        .asCenterList("点击翻页", new String[]{"开启", "关闭"}, null, picsClick ? 0 : 1, (p2, t2) -> {
                                                            PreferenceMgr.put(getContext(), "picsClick", p2 == 0);
                                                            ToastMgr.shortBottomCenter(getContext(), "设置成功");
                                                        }).show();
                                                break;
                                            case "保存全部":
                                                List<Object> picUrls = Stream.of(pics).map(i -> (Object) i).toList();
                                                LoadingPopupView loadingPopupView = new XPopup.Builder(getContext())
                                                        .asLoading("保存中，请稍候");
                                                loadingPopupView.show();
                                                ImgUtil.savePic2Gallery(getContext(), picUrls, getMyUrl(), success -> {
                                                    if (isFinishing()) {
                                                        return;
                                                    }
                                                    runOnUiThread(() -> {
                                                        loadingPopupView.dismiss();
                                                        ToastMgr.shortBottomCenter(getContext(), "已批量保存" + success.size() + "张图片");
                                                    });
                                                });
                                                break;
                                            case "图片详情":
                                                String originalSize = "", size = "";
                                                if (view.getTag() instanceof String) {
                                                    String[] sizes = view.getTag().toString().split("/");
                                                    if (sizes.length == 2) {
                                                        originalSize = sizes[0];
                                                        size = sizes[1];
                                                    } else {
                                                        originalSize = sizes[0];
                                                        size = sizes[0];
                                                    }
                                                }
                                                String size1 = "原始大小：" + originalSize;
                                                String size2 = "显示大小：" + size;
                                                detailPopup = new FileDetailPopup(getActivity(), "图片详情", new String[]{
                                                        "图片地址：" + pics.get(position),
                                                        size1, size2
                                                })
                                                        .withClickListener(new FileDetailAdapter.OnClickListener() {
                                                            @Override
                                                            public void click(String text) {
                                                                if (!text.startsWith("图片地址")) {
                                                                    return;
                                                                }
                                                                new XPopup.Builder(getContext())
                                                                        .asInputConfirm("编辑数据", "点击确定后，界面数据会临时修改为传入的数据，刷新后失效",
                                                                                pics.get(position), null, s -> {
                                                                                    pics.remove(position);
                                                                                    pics.add(position, s);
                                                                                    adapter.notifyItemChanged(position);
                                                                                    if (detailPopup != null) {
                                                                                        detailPopup.updateData(new String[]{
                                                                                                "图片地址：" + pics.get(position), size1, size2
                                                                                        });
                                                                                    }
                                                                                }, null, R.layout.xpopup_confirm_input).show();
                                                            }

                                                            @Override
                                                            public void longClick(View view, String text) {
                                                                if (!text.startsWith("图片地址")) {
                                                                    return;
                                                                }
                                                                ClipboardUtil.copyToClipboardForce(getContext(), pics.get(position));
                                                            }
                                                        });
                                                new XPopup.Builder(getContext())
                                                        .asCustom(detailPopup).show();
                                                break;
                                            case "保存图片":
                                                ImgUtil.savePic2Gallery(getContext(), pics.get(position), getMyUrl(), new ImgUtil.OnSaveListener() {
                                                    @Override
                                                    public void success(List<String> paths) {
                                                        runOnUiThread(() -> ToastMgr.shortBottomCenter(getContext(), "已保存到相册"));
                                                    }

                                                    @Override
                                                    public void failed(String msg) {
                                                        runOnUiThread(() -> ToastMgr.shortBottomCenter(getContext(), msg));
                                                    }
                                                });
                                                break;
                                            case "刷新图片":
                                                Object obj = GlideUtil.getGlideUrl(getMyUrl(), pics.get(position));
                                                ThreadTool.INSTANCE.executeNewTask(() -> {
                                                    try {
                                                        File file = Glide.with(getContext()).downloadOnly().load(obj).submit().get();
                                                        if (file.exists()) {
                                                            Timber.d("File exists: %s", file.getAbsolutePath());
                                                            file.delete();
                                                        }
                                                        ThreadTool.INSTANCE.runOnUI(() -> {
                                                            if (isFinishing()) {
                                                                return;
                                                            }
                                                            Glide.get(getContext()).clearMemory();
                                                            if (view instanceof ImageView) {
                                                                ImageView imgView = (ImageView) view;
                                                                try {
                                                                    Glide.with(getContext()).clear(imgView);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                                Object url = GlideUtil.getGlideUrl(getMyUrl(), pics.get(position));
                                                                RequestOptions options = new RequestOptions().placeholder(getResources()
                                                                        .getDrawable(R.mipmap.placeholder)).skipMemoryCache(true)
                                                                        .diskCacheStrategy(DiskCacheStrategy.DATA);
                                                                GlideUtil.loadFullPicDrawable(getContext(), imgView, url, options);
                                                            }
                                                            ToastMgr.shortBottomCenter(getContext(), "已重新加载图片");
                                                        });
                                                    } catch (Throwable e) {
                                                        e.printStackTrace();
                                                    }
                                                });
                                                break;
                                            default:
                                                //自定义的长按事件
                                                List<LongClickExtra> longClick1 = getLongClick(position);
                                                if (CollectionUtil.isNotEmpty(longClick1)) {
                                                    for (LongClickExtra ext : longClick1) {
                                                        if (text.equals(ext.getTitle())) {
                                                            String js = ext.getJs();
                                                            String input = pics.get(position);
                                                            HeavyTaskUtil.executeNewTask(() -> {
                                                                Object rule = getRule();
                                                                String result = JSEngine.getInstance().evalJS(JSEngine.getMyRule(rule)
                                                                        + JSEngine.getInstance().generateMY("MY_URL", Utils.escapeJavaScriptString(getMyUrl()))
                                                                        + js, input);
                                                                if (StringUtil.isNotEmpty(result) && !"undefined".equalsIgnoreCase(result)
                                                                        && getActivity() != null && !getActivity().isFinishing()) {
                                                                    getActivity().runOnUiThread(() -> {
                                                                        DetailUIHelper.dealUrlSimply(getActivity(), rule, null, result, u -> {

                                                                        });
                                                                    });
                                                                }
                                                            });
                                                            return;
                                                        }
                                                    }
                                                }
                                                break;
                                        }
                                    }

                                    @Override
                                    public void onLongClick(String text, int position1) {

                                    }
                                })
                        ).show();
            }
        }


        @Override
        public void onClick(View view, int position) {
            boolean picsClick = PreferenceMgr.getBoolean(getContext(), "picsClick", false);
            boolean picsHorizontal = PreferenceMgr.getBoolean(getContext(), "picsHorizontal", false);
            WindowManager manager = getWindowManager();
            DisplayMetrics outMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(outMetrics);
            int height = outMetrics.heightPixels;
            int width = outMetrics.widthPixels;
            if (picsHorizontal) {
                //横向显示
                if (touchX < (float) width / 3) {
                    if (picsClick) {
                        prePage(true);
                    }
                } else if (touchX > (float) width * 2 / 3) {
                    if (picsClick) {
                        nextPage(true);
                    }
                } else {
                    //中间
                    onLongClick(view, position);
                }
            } else {
                //竖向显示
                if (touchY < (float) height / 3) {
                    if (picsClick) {
                        prePage(false);
                    }
                } else if (touchY > (float) height * 2 / 3) {
                    if (picsClick) {
                        nextPage(false);
                    }
                } else {
                    //中间
                    onLongClick(view, position);
                }
            }
        }
    };

    private void prePage(boolean picsHorizontal) {
        if (picsHorizontal) {
            int width = smartRefreshLayout.getMeasuredWidth();
            recyclerView.smoothScrollBy(-width, 0);
        } else {
            int height = smartRefreshLayout.getMeasuredHeight();
            recyclerView.smoothScrollBy(0, -height);
        }
    }

    private void nextPage(boolean picsHorizontal) {
        if (picsHorizontal) {
            int width = smartRefreshLayout.getMeasuredWidth();
            recyclerView.smoothScrollBy(width, 0);
        } else {
            int height = smartRefreshLayout.getMeasuredHeight();
            recyclerView.smoothScrollBy(0, height);
        }
    }

    private boolean scrollPage(boolean down) {
        if (down) {
            nextPage(PreferenceMgr.getBoolean(getContext(), "picsHorizontal", false));
        } else {
            prePage(PreferenceMgr.getBoolean(getContext(), "picsHorizontal", false));
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return scrollPage(true);
            case KeyEvent.KEYCODE_VOLUME_UP:
                return scrollPage(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        touchX = ev.getRawX();
        touchY = ev.getRawY();
        return super.dispatchTouchEvent(ev);
    }

    public ClickArea getTouchArea() {
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        int width = outMetrics.widthPixels;
        if (touchY < (float) height / 3) {
            return ClickArea.TOP;
        } else if (touchY > (float) height * 2 / 3) {
            return ClickArea.BOTTOM;
        } else {
            if (touchX < (float) width / 4) {
                return ClickArea.CENTER_LEFT;
            } else if (touchX > (float) width * 3 / 4) {
                return ClickArea.CENTER_RIGHT;
            }
            return ClickArea.CENTER;
        }
    }

    public boolean isAlmostCenter() {
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int height = outMetrics.heightPixels;
        int width = outMetrics.widthPixels;
        return touchX > width / 4F && touchX < width * 3 / 4F &&
                touchY > height / 6F && touchY < height * 5 / 6F;
    }

    private boolean isTouchEdge() {
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        int dp25 = DisplayUtil.dpToPx(getContext(), 30);
        dp25 = Math.max(dp25, page_indicator.getMeasuredWidth() / 2);
        int dp50 = DisplayUtil.dpToPx(getContext(), 50);
        if (dp25 > dp50) {
            dp25 = dp50;
        }
        return touchX < dp25 || touchX > width - dp25;
    }

    @Override
    protected void onDestroy() {
        try {
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (history != null) {
                HistoryMemoryService.INSTANCE.update(history);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}

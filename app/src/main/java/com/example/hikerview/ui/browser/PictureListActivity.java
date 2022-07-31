package com.example.hikerview.ui.browser;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.constants.CollectionTypeConstant;
import com.example.hikerview.model.ViewCollection;
import com.example.hikerview.model.ViewCollectionExtraData;
import com.example.hikerview.model.ViewHistory;
import com.example.hikerview.service.parser.BaseParseCallback;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.LazyRuleParser;
import com.example.hikerview.ui.base.BaseStatusTransNavigationActivity;
import com.example.hikerview.ui.browser.model.PicturePageData;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.browser.util.CollectionUtil;
import com.example.hikerview.ui.detail.DetailUIHelper;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.setting.file.FileDetailAdapter;
import com.example.hikerview.ui.setting.file.FileDetailPopup;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.ui.view.SmartRefreshLayout;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.ImgUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.smarx.notchlib.NotchScreenManager;

import org.litepal.LitePal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private int nowPage = 0;
    private PictureListAdapter adapter;
    private TextView page_indicator;
    private LinearLayoutManager linearLayoutManager;
    private int indexNow = -1;
    private int scrollPos = -1;
    private int lastPicsSize = 0;
    private String CUrl, MTitle;
    private ViewHistory history;
    private Map<Integer, PicturePageData> indicatorMap = new HashMap<>();

    @Override
    protected void initLayout(Bundle savedInstanceState) {
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

    @Override
    protected void initData(Bundle savedInstanceState) {
        CUrl = getIntent().getStringExtra("CUrl");
        MTitle = getIntent().getStringExtra("MTitle");
        addPics(getIntent().getStringArrayListExtra("pics"), getIntent().getStringExtra("title"));
        adapter = new PictureListAdapter(getContext(), pics, getIntent().getStringExtra("url"));
        adapter.setOnItemClickListener(onItemClickListener);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        smartRefreshLayout = findView(R.id.refresh_layout);
        smartRefreshLayout.setEnableRefresh(false);

        long chaptersKey = getIntent().getLongExtra("chapters", 0);
        if (chaptersKey > 0) {
            //取出来，然后清掉
            chapters = PlayerChooser.getChapterMap().get(chaptersKey);
            PlayerChooser.getChapterMap().delete(chaptersKey);
            nowPage = getIntent().getIntExtra("nowPage", 0);
        }

        if (chapters != null && chapters.size() > 1) {
            smartRefreshLayout.setOnLoadMoreListener(this::nextPage);
        } else {
            smartRefreshLayout.setEnableAutoLoadMore(false);
            smartRefreshLayout.setEnableLoadMore(false);
        }
        List<ViewHistory> histories;
        try {
            histories = LitePal.where("url = ? and title = ? and type = ?", CUrl, MTitle, CollectionTypeConstant.DETAIL_LIST_VIEW).limit(1).find(ViewHistory.class);
            if (CollectionUtil.isNotEmpty(histories)) {
                history = histories.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = linearLayoutManager == null ? 0 : linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (scrollPos != position && position >= 0) {
                    scrollPos = position;
                    indexNow = position;
                    indexNow++;
                    PicturePageData pageData = indicatorMap.get(position);
                    if (pageData == null) {
                        page_indicator.setText((indexNow + "/" + pics.size()));
                    } else {
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
                    int pageIndex = indexNow - lastPicsSize;
                    if (pageIndex < 0) {
                        pageIndex = 0;
                    }
                    memoryPageIndex(pageIndex);
                }
            }
        });
        if (getIntent().getBooleanExtra("fromLastPage", false) && history != null) {
            String extra = history.getExtraData();
            ViewCollectionExtraData extraData = new ViewCollectionExtraData(-1);
            if (StringUtil.isNotEmpty(extra)) {
                extraData = JSON.parseObject(extra, ViewCollectionExtraData.class);
            }
            if (extraData.getPageIndex() > 2 && extraData.getPageIndex() < pics.size()) {
                recyclerView.scrollToPosition(extraData.getPageIndex() - 2);
            }
        }
    }

    private void memoryPageIndex(int pageIndex) {
        if (history != null) {
            String extra = history.getExtraData();
            ViewCollectionExtraData extraData = new ViewCollectionExtraData(-1);
            if (StringUtil.isNotEmpty(extra)) {
                extraData = JSON.parseObject(extra, ViewCollectionExtraData.class);
            }
            extraData.setPageIndex(pageIndex);
            history.setExtraData(JSON.toJSONString(extraData));
            HeavyTaskUtil.executeNewTask(() -> history.save());
        }
    }

    /**
     * 清除图片标记，避免图片无法显示
     *
     * @param urls
     */
    private void addPics(List<String> urls, String title) {
        if (CollectionUtil.isEmpty(urls)) {
            return;
        }
        lastPicsSize = pics.size();
        PicturePageData pageData = new PicturePageData(urls, title);
        pageData.setPreSize(pics.size());
        pics.addAll(Stream.of(urls).map(UrlDetector::clearTag).collect(Collectors.toList()));
        for (int i = lastPicsSize; i < pics.size(); i++) {
            indicatorMap.put(i, pageData);
        }
        indexNow = linearLayoutManager == null ? 0 : linearLayoutManager.findLastCompletelyVisibleItemPosition();
        if (indexNow < 0) {
            indexNow = 0;
        }
        indexNow++;
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

    private void nextPage(RefreshLayout refreshLayout) {
        if (chapters.get(chapters.size() - 1).isUse()) {
            ToastMgr.shortBottomCenter(getContext(), "没有下一页啦！");
            refreshLayout.finishLoadMore();
            return;
        }
        for (int i = 0; i < chapters.size() - 1; i++) {
            if (chapters.get(i).isUse()) {
                nowPage = i + 1;
                chapters.get(i).setUse(false);
                chapters.get(i + 1).setUse(true);
                //足迹
                String title = chapters.get(i + 1).getTitle();
                String itemTitle = DetailUIHelper.getTitleText(chapters.get(i + 1).getMemoryTitle());
                String click = title.split("-")[title.split("-").length - 1] + "@@" + (i + 1);
                if (StringUtil.isNotEmpty(CUrl) && StringUtil.isNotEmpty(MTitle)) {
                    List<ViewCollection> collections = null;
                    try {
                        collections = LitePal.where("CUrl = ? and MTitle = ?", CUrl, MTitle).limit(1).find(ViewCollection.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!CollectionUtil.isEmpty(collections)) {
                        collections.get(0).setLastClick(click);
                        collections.get(0).save();
                    }

                    if (history != null) {
                        history.setLastClick(click);
                        history.save();
                    }
                }

                if (StringUtil.isNotEmpty(chapters.get(i + 1).getCodeAndHeader()) && StringUtil.isNotEmpty(chapters.get(i + 1).getOriginalUrl())) {
                    String[] lazyRule = chapters.get(i + 1).getOriginalUrl().split("@lazyRule=");
                    if (lazyRule.length != 2) {
                        int oldSize = pics.size();
                        addPics(new ArrayList<>(Arrays.asList(lazyRule[0].replace("pics://", "").split("&&"))), itemTitle);
                        adapter.notifyItemRangeInserted(oldSize, pics.size() - oldSize);
                        smartRefreshLayout.finishLoadMore();
                    } else {
                        dealLazyRule(lazyRule, chapters.get(i + 1).getCodeAndHeader(), itemTitle);
                    }
                    return;
                } else {
                    String url = chapters.get(i + 1).getUrl();
                    if (StringUtil.isEmpty(url) || !url.startsWith("pics://")) {
                        ToastMgr.shortBottomCenter(getContext(), "链接格式有误：" + url);
                        smartRefreshLayout.finishLoadMore();
                        break;
                    }
                    int oldSize = pics.size();
                    addPics(new ArrayList<>(Arrays.asList(url.replace("pics://", "").split("&&"))), itemTitle);
                    adapter.notifyItemRangeInserted(oldSize, pics.size() - oldSize);
                    smartRefreshLayout.finishLoadMore();
                }
                break;
            }
        }
        refreshLayout.finishLoadMore();
    }


    /**
     * 处理动态解析
     *
     * @param lazyRule
     * @param codeAndHeader
     */
    private void dealLazyRule(String[] lazyRule, String codeAndHeader, String itemTitle) {
        if (lazyRule.length != 2) {
            ToastMgr.shortBottomCenter(getContext(), "动态解析规则有误");
            return;
        }
        String r = getIntent().getStringExtra("rule");
        ArticleListRule rule = StringUtil.isEmpty(r) ? null : JSON.parseObject(r, ArticleListRule.class);
        LazyRuleParser.parse(getActivity(), rule, lazyRule, codeAndHeader, getIntent().getStringExtra("url"), new BaseParseCallback<String>() {
            @Override
            public void start() {

            }

            @Override
            public void success(String res) {
                Log.d(TAG, "dealLazyRule: " + res);
                int oldSize = pics.size();
                addPics(new ArrayList<>(Arrays.asList(res.replace("pics://", "").split("&&"))), itemTitle);
                adapter.notifyItemRangeInserted(oldSize, pics.size() - oldSize);
                smartRefreshLayout.finishLoadMore();
            }

            @Override
            public void error(String msg) {

            }
        });
    }

    private String getMyUrl(){
        return HttpParser.getFirstPageUrl(getIntent().getStringExtra("url"));
    }

    private PictureListAdapter.OnItemClickListener onItemClickListener = new PictureListAdapter.OnItemClickListener() {

        @Override
        public void onClick(View view, int position) {
            new XPopup.Builder(getContext())
                    .asImageViewer(null, pics.get(position), new PopImageLoaderNoView(getIntent().getStringExtra("url")))
                    .show();
        }

        @Override
        public void onLongClick(View view, int position) {
            new XPopup.Builder(getContext()).asBottomList("选择操作", new String[]{"全屏查看", "图片详情", "保存图片", "保存全部"}, (position1, text) -> {
                switch (text) {
                    case "全屏查看":
                        new XPopup.Builder(getContext())
                                .asImageViewer(null, pics.get(position), new PopImageLoaderNoView(getIntent().getStringExtra("url")))
                                .show();
                        break;
                    case "保存全部":
                        List<Object> picUrls = Stream.of(pics).filter(u -> {
                            if (StringUtil.isEmpty(u) || !u.toLowerCase().startsWith("http")) {
                                return false;
                            }
                            return UrlDetector.isImage(u);
                        }).map(i -> (Object) i).toList();
                        ImgUtil.savePic2Gallery(getContext(), picUrls, getMyUrl(), success -> {
                            if (isFinishing()) {
                                return;
                            }
                            runOnUiThread(() -> ToastMgr.shortBottomCenter(getContext(), "已批量保存" + success + "张图片"));
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
                        FileDetailPopup detailPopup = new FileDetailPopup(getActivity(), "图片详情", new String[]{
                                "图片地址：" + pics.get(position),
                                "原始大小：" + originalSize,
                                "显示大小：" + size
                        })
                                .withClickListener(new FileDetailAdapter.OnClickListener() {
                                    @Override
                                    public void click(String text) {
                                        longClick(null, text);
                                    }

                                    @Override
                                    public void longClick(View view, String text) {

                                    }
                                });
                        new XPopup.Builder(getContext())
                                .asCustom(detailPopup).show();
                        break;
                    case "保存图片":
                        ImgUtil.savePic2Gallery(getContext(), pics.get(position), getIntent().getStringExtra("url"), new ImgUtil.OnSaveListener() {
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
                }
            }).show();
        }
    };
}

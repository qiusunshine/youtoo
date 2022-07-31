package com.example.hikerview.ui.search;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.R;
import com.example.hikerview.event.OnHomePageChangedEvent;
import com.example.hikerview.event.StatusEvent;
import com.example.hikerview.service.parser.BaseParseCallback;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.LazyRuleParser;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.browser.PictureListActivity;
import com.example.hikerview.ui.browser.WebViewActivity;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.browser.model.UrlDetector;
import com.example.hikerview.ui.browser.util.HttpRequestUtil;
import com.example.hikerview.ui.detail.DetailUIHelper;
import com.example.hikerview.ui.download.DownloadDialogUtil;
import com.example.hikerview.ui.home.FilmListActivity;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.ui.home.model.SearchResult;
import com.example.hikerview.ui.search.model.SearchModel;
import com.example.hikerview.ui.video.PlayerChooser;
import com.example.hikerview.ui.video.VideoChapter;
import com.example.hikerview.ui.view.PopImageLoaderNoView;
import com.example.hikerview.ui.view.ScrollSpeedLinearLayoutManger;
import com.example.hikerview.ui.view.SmartRefreshLayout;
import com.example.hikerview.utils.ClipboardUtil;
import com.example.hikerview.utils.DebugUtil;
import com.example.hikerview.utils.ShareUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.example.hikerview.utils.WebUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.XPopupImageLoader;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * 作者：By hdy
 * 日期：On 2018/12/14
 * 时间：At 20:12
 */
public class SearchFragment extends Fragment implements BaseCallback<SearchResult> {
    private SearchAdapter adapter;
    private String keyWord;
    private SearchEngine searchEngine;
    private int enginePos;
    private List<SearchResult> results = new ArrayList<>();
    private View convertView;
    private SparseArray<View> mViews;
    private AppCompatImageView progressView1;
    private SmartRefreshLayout smartRefreshLayout;
    private String group;
    private int page = 1;
    private boolean isRefresh = true;
    private String firstPageData, lastPageData;
    private String orginalUrl = "";
    private LoadingPopupView loadingPopupView;
    private String myUrl = "";
    private SearchModel searchModel = new SearchModel().withUrlParseCallBack(url -> {
        myUrl = url;
    });

    public SearchFragment newInstance(String keyWord, SearchEngine searchEngine, int enginePos, String group) {
        this.keyWord = keyWord;
        this.searchEngine = searchEngine;
        this.enginePos = enginePos;
        this.group = group;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        convertView = inflater.inflate(initLayout(), container, false);
        mViews = new SparseArray<>();
        progressView1 = findView(R.id.progress_image_view1);
        initView();
        initData();
        return convertView;
    }

    protected int initLayout() {
        return R.layout.fragment_search;
    }

    protected void initView() {
        smartRefreshLayout = findView(R.id.refresh_layout);
        smartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            page = 1;
            isRefresh = true;
            initData();
        });
        orginalUrl = searchEngine.getSearch_url();
        if (searchEngine.getSearch_url().contains("fypage")) {
            smartRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
                page++;
                isRefresh = false;
                initData();
            });
        } else {
            smartRefreshLayout.setEnableAutoLoadMore(false);
            smartRefreshLayout.setEnableLoadMore(false);
        }
        RecyclerView recyclerView = findView(R.id.search_recycleview_v2);
        LinearLayoutManager linearLayoutManager = new ScrollSpeedLinearLayoutManger(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SearchAdapter(getContext(), results, keyWord, searchEngine);
        adapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position, String type) {
                SearchResult result = adapter.getList().get(position);
                if (result.getTitle().equals("没有找到你想要的啦-_-") && result.getDesc().equals("小棉袄")) {
                    return;
                }
                if (result.getTitle().equals("数据获取失败-_-") && result.getDesc().equals("小棉袄")) {
                    return;
                }
                if (StringUtil.isEmpty(result.getUrl())) {
                    return;
                }
                if (result.getUrl().startsWith("hiker://home")) {
                    //回到首页
                    if (result.getUrl().contains("@")) {
                        String[] s = result.getUrl().split("@");
                        if (s.length == 2 && !TextUtils.isEmpty(s[1])) {
                            EventBus.getDefault().post(new OnHomePageChangedEvent(s[1]));
                            try {
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastMgr.shortBottomCenter(getContext(), "首页路由规则有误");
                        }
                    } else {
                        ToastMgr.shortBottomCenter(getContext(), "首页路由规则有误");
                    }
                    return;
                }
                String[] urlRule = result.getUrl().split("@rule=");
                String[] lazyRule = urlRule[0].split("@lazyRule=");
                String codeAndHeader = DetailUIHelper.getCodeAndHeader(orginalUrl, lazyRule);
                if (lazyRule.length > 1) {
                    dealLazyRule(view, position, lazyRule, codeAndHeader);
                    return;
                }
                if (DetailUIHelper.dealUrlSimply(getActivity(), result.getUrl())) {
                    return;
                }
                if (urlRule.length > 1) {
                    String r = StringUtil.arrayToString(urlRule, 1, "@rule=");
                    String nextRule = r.split("==>")[0];
                    String[] rules = nextRule.split(";");
                    String colType = searchEngine.getDetail_col_type();
                    if (rules.length == 6 && !r.startsWith("js:")) {
                        //有col_type，提取，并且修正规则
                        colType = rules[5];
                        nextRule = StringUtil.arrayToString(rules, 0, 5, ";");
                    }
                    String s1 = StringUtil.arrayToString(r.split("==>"), 1, "==>");
                    if (StringUtil.isNotEmpty(s1)) {
                        s1 = "==>" + s1;
                    } else {
                        s1 = "";
                    }
                    String rule = nextRule + s1;
                    dealRule(searchEngine, result,
                            urlRule[0], codeAndHeader,
                            rule, colType,
                            position);
                    return;
                }
                if (StringUtil.isNotEmpty(searchEngine.getDetail_find_rule())) {
                    dealRule(searchEngine, result,
                            result.getUrl(), codeAndHeader,
                            searchEngine.getDetail_find_rule(), searchEngine.getDetail_col_type(),
                            position);
                    return;
                }
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra("is_xiu_tan", false);
                intent.putExtra("url", result.getUrl());
                intent.putExtra("kw", keyWord);
                intent.putExtra("showSearchEngine", false);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position, String type) {
                SearchResult result = adapter.getList().get(position);
                new XPopup.Builder(getContext())
                        .asCenterList("请选择操作", new String[]{"网页查看", "刷新页面", "网页搜索", "复制链接"},
                                ((option, text) -> {
                                    switch (text) {
                                        case "网页查看":
                                            if (result.getTitle().equals("没有找到你想要的啦-_-") && result.getDesc().equals("小棉袄")) {
                                                return;
                                            }
                                            if (result.getTitle().equals("数据获取失败-_-") && result.getDesc().equals("小棉袄")) {
                                                return;
                                            }
                                            if (StringUtil.isEmpty(result.getUrl())) {
                                                return;
                                            }
                                            if (!result.getUrl().startsWith("http")) {
                                                ToastMgr.shortBottomCenter(getContext(), "当前链接不支持网页查看");
                                                break;
                                            }
                                            WebUtil.goWeb(getContext(), HttpParser.getFirstPageUrl(result.getUrl()));
                                            break;
                                        case "复制链接":
                                            if (TextUtils.isEmpty(result.getUrl())) {
                                                ToastMgr.shortBottomCenter(getContext(), "链接为空，请检查规则");
                                            } else {
                                                ClipboardUtil.copyToClipboard(getContext(), result.getUrl());
                                            }
                                            break;
                                        case "刷新页面":
                                            page = 1;
                                            isRefresh = true;
                                            initData();
                                            break;
                                        case "网页搜索":
                                            WebUtil.goWeb(getContext(), HttpParser.parseSearchUrl(searchEngine.getSearch_url().replace("fypage", "1"), keyWord));
                                            break;
                                    }
                                }))
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
    }
    private void dealRule(SearchEngine searchEngine, SearchResult result, String url, String codeAndHeader, String rule, String colType, int position) {
        ArticleListRule articleListRule1 = new ArticleListRule();
        articleListRule1.setUa(searchEngine.getUa());
        if (!url.contains(";")) {
            articleListRule1.setUrl(url + codeAndHeader);
        } else {
            articleListRule1.setUrl(url);
        }
        articleListRule1.setFind_rule(rule);
        articleListRule1.setGroup(searchEngine.getGroup());
        articleListRule1.setCol_type(colType);
        articleListRule1.setPreRule(searchEngine.getPreRule());
        articleListRule1.setTitle(searchEngine.getTitle());
        Intent intent = new Intent(getContext(), FilmListActivity.class);
        intent.putExtra("data", JSON.toJSONString(articleListRule1));
        intent.putExtra("picUrl", result.getImg());
        intent.putExtra("title", adapter.getList().get(position).getTitle());
        startActivity(intent);
    }

    private void dealLazyRule(View view, int position, String[] lazyRule, String codeAndHeader) {
        Timber.d("dealLazyRule: ");
        LazyRuleParser.parse(getActivity(), lazyRule, codeAndHeader, myUrl, new BaseParseCallback<String>() {
            @Override
            public void start() {
                if (loadingPopupView == null) {
                    loadingPopupView = new XPopup.Builder(getContext()).asLoading("动态解析规则中，请稍候");
                }
                loadingPopupView.show();
            }

            @Override
            public void success(String data) {
                dealWithUrl(view, data, position, codeAndHeader);
                if (loadingPopupView != null) {
                    loadingPopupView.dismiss();
                }
            }

            @Override
            public void error(String msg) {
                if (loadingPopupView != null) {
                    loadingPopupView.dismiss();
                }
            }
        });
    }

    private void dealWithUrl(@Nullable View view, String url, int position, @Nullable String codeAndHeader) {
        if (position < 0 || position >= adapter.getList().size()) {
            return;
        }

        if (DetailUIHelper.dealUrlSimply(getActivity(), url)) {
            return;
        }
        //标题处理，将html转成纯文本
        String itemTitle = getItemTitle(position);
        String activityTitle = DetailUIHelper.getActivityTitle(getActivity());
        String intentTitle = activityTitle;
        if (StringUtil.isNotEmpty(intentTitle)) {
            intentTitle = intentTitle + "-";
        }
        String lowUrl = url.toLowerCase();
        if (lowUrl.startsWith("javascript")) {
            //JS
            WebUtil.goWeb(getContext(), url);
            return;
        } else if (lowUrl.startsWith("pics://")) {
            //图片漫画类型
            String realRule = StringUtils.replaceOnceIgnoreCase(url, "pics://", "");
            String[] urls = realRule.split("&&");
            ArrayList<String> imageUrls = new ArrayList<>(Arrays.asList(urls));
            Intent intent = new Intent(getContext(), PictureListActivity.class);
            intent.putStringArrayListExtra("pics", imageUrls);
            intent.putExtra("url", orginalUrl);
            List<VideoChapter> chapters1 = getChapters(url, position, intentTitle, codeAndHeader);
            long current = PlayerChooser.putChapters(chapters1);
            intent.putExtra("chapters", current);
            if (getActivity() instanceof FilmListActivity) {
                intent.putExtra("CUrl", orginalUrl);
                intent.putExtra("MTitle", activityTitle);
            }
            int nowPage = 0;
            for (int i = 0; i < chapters1.size(); i++) {
                if (chapters1.get(i).isUse()) {
                    nowPage = i;
                }
            }
            intent.putExtra("nowPage", nowPage);
            startActivity(intent);
            return;
        }

        String ext = HttpRequestUtil.getFileExtensionFromUrl(url);
        if (UrlDetector.isImage(url)) {
            //检测是否是图片
            XPopupImageLoader imageLoader = new PopImageLoaderNoView(orginalUrl);
            new XPopup.Builder(getContext())
                    .asImageViewer(null, url, imageLoader)
                    .show();
        } else if (UrlDetector.isVideoOrMusic(url)) {
            //音乐或者视频
            if (url.equals(adapter.getList().get(position).getUrl()) || StringUtil.isNotEmpty(codeAndHeader)) {
                List<VideoChapter> chapters = getChapters(url, position, intentTitle, codeAndHeader);
                Bundle extraDataBundle = new Bundle();
                String viewCollectionExtraData = getViewCollectionExtraData();
                if (!StringUtil.isEmpty(viewCollectionExtraData)) {
                    extraDataBundle.putString("viewCollectionExtraData", viewCollectionExtraData);
                }
                if (getActivity() instanceof FilmListActivity) {
                    PlayerChooser.startPlayer(getActivity(), chapters, orginalUrl, activityTitle, extraDataBundle);
                } else {
                    PlayerChooser.startPlayer(getActivity(), chapters);
                }
                return;
            }
            PlayerChooser.startPlayer(getActivity(), itemTitle, url);
        } else if (lowUrl.startsWith("http") && "apk".equalsIgnoreCase(ext)) {
            DownloadDialogUtil.showEditApkDialog(getActivity(), url, url, null);
        } else if (lowUrl.startsWith("http") || lowUrl.startsWith("file://")) {
            //链接
            Bundle extraDataBundle = new Bundle();
            String viewCollectionExtraData = getViewCollectionExtraData();
            if (!StringUtil.isEmpty(viewCollectionExtraData)) {
                extraDataBundle.putString("viewCollectionExtraData", viewCollectionExtraData);
            }
            WebUtil.goWebWithExtraData(getContext(), url, extraDataBundle);
        } else if (lowUrl.startsWith("magnet") || lowUrl.startsWith("thunder") || lowUrl.startsWith("ftp") || lowUrl.startsWith("ed2k")) {
            //常用第三方软件
            ShareUtil.findChooserToDeal(getContext(), url);
        } else {
            ToastMgr.shortBottomCenter(getContext(), "未知链接：" + url);
        }
    }

    private List<VideoChapter> getChapters(String url, int position, String title, String codeAndHeader) {
        List<VideoChapter> chapters = new ArrayList<>();
        for (int i = 0; i < position; i++) {
            if ("header".equals(adapter.getList().get(i).getType())) {
                continue;
            }
            VideoChapter videoChapter = new VideoChapter();
            videoChapter.setMemoryTitle(adapter.getList().get(i).getTitle());
            videoChapter.setTitle(title + adapter.getList().get(i).getTitle());
            videoChapter.setUrl(adapter.getList().get(i).getUrl());
            videoChapter.setUse(false);
            if (StringUtil.isNotEmpty(codeAndHeader)) {
                videoChapter.setCodeAndHeader(codeAndHeader);
                videoChapter.setOriginalUrl(adapter.getList().get(i).getUrl());
            }
            chapters.add(videoChapter);
        }
        VideoChapter videoChapter = new VideoChapter();
        videoChapter.setMemoryTitle(adapter.getList().get(position).getTitle());
        videoChapter.setTitle(title + adapter.getList().get(position).getTitle());
        videoChapter.setUrl(url);
        videoChapter.setUse(true);
        if (StringUtil.isNotEmpty(codeAndHeader)) {
            videoChapter.setCodeAndHeader(codeAndHeader);
            videoChapter.setOriginalUrl(adapter.getList().get(position).getUrl());
        }
        chapters.add(videoChapter);
        for (int i = position + 1; i < adapter.getList().size(); i++) {
            VideoChapter chapter = new VideoChapter();
            chapter.setTitle(title + adapter.getList().get(i).getTitle());
            chapter.setMemoryTitle(adapter.getList().get(i).getTitle());
            chapter.setUrl(adapter.getList().get(i).getUrl());
            chapter.setUse(false);
            if (StringUtil.isNotEmpty(codeAndHeader)) {
                chapter.setCodeAndHeader(codeAndHeader);
                chapter.setOriginalUrl(adapter.getList().get(i).getUrl());
            }
            chapters.add(chapter);
        }
        return chapters;
    }

    private String getViewCollectionExtraData() {
        return DetailUIHelper.getViewCollectionExtraData(getActivity(), orginalUrl);
    }

    private String getItemTitle(int position) {
        return DetailUIHelper.getItemTitle(getActivity(), adapter.getList().get(position).getTitle());
    }

    protected void initData() {
        searchEngine.setStatus(1);
        EventBus.getDefault().post(new StatusEvent(this.enginePos, group));
        loading(true);
        boolean loadPre = getActivity() instanceof SearchActivity || getActivity() instanceof SearchInOneRuleActivity;
        searchModel.params(getContext(), keyWord, searchEngine, page, loadPre).process(SearchModel.SEARCH_BY_RULE, this);
    }

    @Override
    public void bindArrayToView(String actionType, List<SearchResult> data) {
        if (getActivity() != null) {
            String dataStr = JSON.toJSONString(data);
            try {
                getActivity().runOnUiThread(() -> {
                    loading(false);
                    smartRefreshLayout.finishRefresh(true);
                    List<SearchResult> last = new ArrayList<>(results);
                    int size = results.size();
                    if (isRefresh) {
                        results.clear();
                        isRefresh = false;
                    }
                    if (data.size() < 1) {
                        smartRefreshLayout.finishLoadMore();
                    } else if (results.size() > 0 && (dataStr.equals(firstPageData) || dataStr.equals(lastPageData))) {
                        smartRefreshLayout.finishLoadMore();
                    } else {
                        smartRefreshLayout.finishLoadMore();
                        results.addAll(data);
                    }
                    if (results.size() < 1) {
                        results.addAll(last);
                        searchEngine.setStatus(4);
                        EventBus.getDefault().post(new StatusEvent(this.enginePos, group));
                        if (last.size() < 1 || !"没有找到你想要的啦-_-".equals(last.get(0).getTitle())) {
                            results.add(new SearchResult("没有找到你想要的啦-_-", "小棉袄"));
                        }
                    } else {
                        searchEngine.setStatus(2);
                        EventBus.getDefault().post(new StatusEvent(this.enginePos, group));
                    }
                    adapter.notifyDataSetChanged();
                    if (size == 0) {
                        firstPageData = dataStr;
                    } else {
                        lastPageData = dataStr;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void bindObjectToView(String actionType, SearchResult data) {

    }

    @Override
    public void error(String title, String msg, String code, Exception e) {
        if (getActivity() != null) {
            if (!isAdded() || isDetached()) {
                return;
            }
            getActivity().runOnUiThread(() -> {
                if (!isAdded() || isDetached()) {
                    return;
                }
                loading(false);
                if (isRefresh) {
                    smartRefreshLayout.finishRefresh(false);
                } else {
                    smartRefreshLayout.finishLoadMore(false);
                }
                searchEngine.setStatus(3);
                EventBus.getDefault().post(new StatusEvent(this.enginePos, group));
//                results.clear();
                SearchResult result = new SearchResult("数据获取失败-_-", "小棉袄");
                result.setContent(DebugUtil.getErrorMsg(title, msg, code, e));
                results.add(result);
                adapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void loading(boolean isLoading) {
        if (!isAdded() || isDetached()) {
            return;
        }
        try {
            if (isLoading) {
                progressView1.setVisibility(View.VISIBLE);
                ((Animatable) progressView1.getDrawable()).start();
            } else {
                ((Animatable) progressView1.getDrawable()).stop();
                progressView1.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected <E extends View> E findView(int viewId) {
        if (convertView != null) {
            E view = (E) mViews.get(viewId);
            if (view == null) {
                view = (E) convertView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return view;
        }
        return null;
    }
}

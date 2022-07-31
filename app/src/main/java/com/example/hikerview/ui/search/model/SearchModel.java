package com.example.hikerview.ui.search.model;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.service.parser.SearchJsCallBack;
import com.example.hikerview.service.parser.SearchParser;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseModel;
import com.example.hikerview.ui.browser.model.SearchEngine;
import com.example.hikerview.ui.home.model.ArticleListModel;
import com.example.hikerview.ui.home.model.SearchResult;
import com.example.hikerview.utils.HeavyTaskUtil;
import com.example.hikerview.utils.StringUtil;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2019/10/2
 * 时间：At 10:22
 */
public class SearchModel extends BaseModel<SearchResult> {
    private static final String TAG = "SearchModel";
    public static final String SEARCH_BY_RULE = "SEARCH_BY_RULE";
    private ArticleListModel.OnUrlParseCallBack urlParseCallBack;

    public SearchModel withUrlParseCallBack(ArticleListModel.OnUrlParseCallBack urlParseCallBack) {
        this.urlParseCallBack = urlParseCallBack;
        return this;
    }

    @Override
    public void process(String actionType, BaseCallback<SearchResult> baseCallback) {
        HeavyTaskUtil.executeNewTask(() -> {
            String wd = (String) mParams[0];
            SearchEngine engine = (SearchEngine) mParams[1];
            String url = engine.getSearch_url();
            url = HttpParser.getUrlAppendUA(url, engine.getUa());
            int page = (Integer) mParams[2];

            if (page == 1 && mParams.length >= 4 && Boolean.TRUE.toString().equals(JSON.toJSONString(mParams[3]))) {
                if (StringUtil.isNotEmpty(engine.getPreRule())) {
                    //非二级，且有预处理规则
                    try {
                        JSEngine.getInstance().parsePreRule(engine);
                    } catch (Exception e) {
                        baseCallback.error(e.getMessage(), e.getMessage(), e.getMessage(), e);
                        return;
                    }
                }
            }

//        Log.d(TAG, "process: " + page);
            String[] allUrl = url.split(";");
            url = allUrl[0];
            if (url.contains("fypage@")) {
                //fypage@-1@*2@/fyclass
                String[] strings = url.split("fypage@");
                String[] pages = strings[1].split("@");
                for (int i = 0; i < pages.length - 1; i++) {
                    if (pages[i].startsWith("-")) {
                        page = page - Integer.parseInt(pages[i].replace("-", ""));
                    } else if (pages[i].startsWith("+")) {
                        page = page + Integer.parseInt(pages[i].replace("+", ""));
                    } else if (pages[i].startsWith("*")) {
                        page = page * Integer.parseInt(pages[i].replace("*", ""));
                    } else if (pages[i].startsWith("/")) {
                        page = page / Integer.parseInt(pages[i].replace("/", ""));
                    }
                }
                //前缀 + page + 后缀
                url = strings[0] + page + pages[pages.length - 1];
            } else {
                url = url.replace("fypage", page + "");
            }
            StringBuilder builder = new StringBuilder(url);
            if (allUrl.length > 1) {
                for (int i = 1; i < allUrl.length; i++) {
                    builder.append(";").append(allUrl[i]);
                }
            }
//        Log.d(TAG, "process: " + url);
            HttpParser.parseSearchUrlForHtml(wd, builder.toString(), new HttpParser.OnSearchCallBack() {
                @Override
                public void onSuccess(String url, String s) {
                    if (urlParseCallBack != null) {
                        urlParseCallBack.storeUrl(url == null ? "" : url);
                    }
                    HeavyTaskUtil.executeNewTask(() -> {
                        //解析数据源
                        SearchParser.findList(url, (SearchEngine) mParams[1], s, new SearchJsCallBack<List<SearchResult>>() {
                            @Override
                            public void showData(List<SearchResult> data) {
                                baseCallback.bindArrayToView(actionType, data);
                            }

                            @Override
                            public void showErr(String msg) {
                                baseCallback.error(msg, msg, msg, new Exception(msg));
                            }
                        });
                    });
                }

                @Override
                public void onFailure(int errorCode, String msg) {
                    baseCallback.error(msg, TAG + "-HttpRequestError-msg：" + msg, errorCode + "", new Exception(msg));
                }
            });
        });
    }
}

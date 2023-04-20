package com.example.hikerview.ui.miniprogram.service;

import com.alibaba.fastjson.JSON;
import com.example.hikerview.service.parser.CommonParser;
import com.example.hikerview.service.parser.HomeParser;
import com.example.hikerview.service.parser.HttpParser;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.ui.base.BaseCallback;
import com.example.hikerview.ui.base.BaseModel;
import com.example.hikerview.ui.home.model.ArticleList;
import com.example.hikerview.ui.home.model.ArticleListRule;
import com.example.hikerview.utils.StringUtil;

/**
 * 作者：By 15968
 * 日期：On 2019/10/2
 * 时间：At 10:22
 */
public class ArticleListService extends BaseModel<ArticleList> {
    private static final String TAG = "ArticleListModel";
    private OnUrlParseCallBack urlParseCallBack;

    public ArticleListService withUrlParseCallBack(OnUrlParseCallBack urlParseCallBack) {
        this.urlParseCallBack = urlParseCallBack;
        return this;
    }

    @Override
    public void process(String actionType, final BaseCallback<ArticleList> baseCallback) {
        if (mParams == null || mParams.length < 3) {
            baseCallback.error("参数错误", "mParams格式不正确", "404", null);
            return;
        }
        int page = 0;
        boolean newLoad = false;
        ArticleListRule articleListRule = null;
        try {
            page = (int) mParams[0];
            newLoad = (boolean) mParams[1];
            articleListRule = (ArticleListRule) mParams[2];
        } catch (Exception e) {
            baseCallback.error("参数错误", e.getMessage(), "404", e);
            return;
        }
        if (page == 1 && mParams.length >= 4 && Boolean.FALSE.toString().equals(JSON.toJSONString(mParams[3]))) {
            if (StringUtil.isNotEmpty(articleListRule.getPreRule())) {
                //非二级，且有预处理规则
                try {
                    JSEngine.getInstance().parsePreRule(articleListRule);
                } catch (Exception e) {
                    baseCallback.error(e.getMessage(), e.getMessage(), e.getMessage(), e);
                    return;
                }
            }
        }
        parse(actionType, page, articleListRule, baseCallback);
    }

    private void parse(String actionType, int page, ArticleListRule articleListRule, final BaseCallback<ArticleList> baseCallback) {
        String urlWithUa = HttpParser.getUrlAppendUA(articleListRule.getUrl(), articleListRule.getUa());
        try {
            String url = CommonParser.parsePageClassUrl(urlWithUa, page, articleListRule);
            url = JSEngine.proxyUrl(url, articleListRule.getProxy());
            final String rule = articleListRule.getFind_rule();
            final ArticleListRule finalArticleListRule = articleListRule;
            HttpParser.parseSearchUrlForHtml(url, new HttpParser.OnSearchCallBack() {
                @Override
                public void onSuccess(String url, String s) {
                    if (urlParseCallBack != null) {
                        urlParseCallBack.storeUrl(url == null ? "" : url);
                    }
                    try {
                        HomeParser.findList(actionType, url, finalArticleListRule, rule, s, page, baseCallback);
                    } catch (Exception e) {
                        baseCallback.error("", TAG + "-解析失败：" + e.toString(), "", e);
                    }
                }

                @Override
                public void onFailure(int errorCode, String msg) {
                    baseCallback.error("", TAG + "-HttpRequestError-msg：" + msg, "" + errorCode, null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            baseCallback.error("", TAG + "-HttpRequestError-msg：" + e.toString(), "", e);
        }

    }

    public interface OnUrlParseCallBack {
        void storeUrl(String url);
    }
}

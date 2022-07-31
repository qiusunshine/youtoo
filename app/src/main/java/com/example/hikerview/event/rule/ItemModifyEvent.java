package com.example.hikerview.event.rule;

import com.example.hikerview.ui.home.model.ArticleList;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2022/1/3
 * 时间：At 16:05
 */

public class ItemModifyEvent {
    public ItemModifyEvent(ArticleList articleList) {
        this.articleList = articleList;
        this.action = Action.UPDATE;
    }

    public ItemModifyEvent(ArticleList articleList, Action action) {
        this.articleList = articleList;
        this.action = action;
    }

    public ItemModifyEvent(List<ArticleList> lists, Action action) {
        this.list = lists;
        this.action = action;
    }

    private ArticleList articleList;
    private List<ArticleList> list;
    private Action action;
    private String anchorId;
    private String cls;
    private boolean after;

    public ArticleList getArticleList() {
        return articleList;
    }

    public void setArticleList(ArticleList articleList) {
        this.articleList = articleList;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }

    public boolean isAfter() {
        return after;
    }

    public void setAfter(boolean after) {
        this.after = after;
    }

    public List<ArticleList> getList() {
        return list;
    }

    public void setList(List<ArticleList> list) {
        this.list = list;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public enum Action {
        UPDATE, ADD, DELETE
    }
}
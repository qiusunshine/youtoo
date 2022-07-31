package com.example.hikerview.event.rule;

import com.example.hikerview.ui.home.model.ArticleList;

import java.util.concurrent.CountDownLatch;

/**
 * 作者：By 15968
 * 日期：On 2022/4/29
 * 时间：At 16:26
 */

public class ItemFindEvent {
    public ItemFindEvent(String id) {
        this.id = id;
    }

    private String id;

    public ItemFindEvent(String id, CountDownLatch countDownLatch) {
        this.id = id;
        this.countDownLatch = countDownLatch;
    }

    private CountDownLatch countDownLatch;
    private ArticleList articleList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArticleList getArticleList() {
        return articleList;
    }

    public void setArticleList(ArticleList articleList) {
        this.articleList = articleList;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
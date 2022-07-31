package com.example.hikerview.event.rule;

import com.example.hikerview.ui.home.model.ArticleList;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 作者：By 15968
 * 日期：On 2022/4/29
 * 时间：At 16:26
 */

public class ClsItemsFindEvent {
    public ClsItemsFindEvent(String cls) {
        this.cls = cls;
    }

    private String cls;

    public ClsItemsFindEvent(String cls, CountDownLatch countDownLatch) {
        this.cls = cls;
        this.countDownLatch = countDownLatch;
    }

    private CountDownLatch countDownLatch;
    private List<ArticleList> articleLists;

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public List<ArticleList> getArticleLists() {
        return articleLists;
    }

    public void setArticleLists(List<ArticleList> articleLists) {
        this.articleLists = articleLists;
    }
}
package com.example.hikerview.ui.browser.model;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.constants.ArticleColTypeEnum;

/**
 * 作者：By 15968
 * 日期：On 2020/10/17
 * 时间：At 10:17
 */

public class IconTitle {
    private int icon;
    private String title;
    private String desc;
    private ArticleColTypeEnum colTypeEnum;

    private String extraParam;

    private Consumer<String> extraConsumer;

    public IconTitle(int icon, String title, String desc, ArticleColTypeEnum colTypeEnum) {
        this.icon = icon;
        this.title = title;
        this.desc = desc;
        this.colTypeEnum = colTypeEnum;
    }

    public IconTitle(int icon, String title, ArticleColTypeEnum colTypeEnum) {
        this.icon = icon;
        this.title = title;
        this.colTypeEnum = colTypeEnum;
    }

    public IconTitle(String title, ArticleColTypeEnum colTypeEnum) {
        this.title = title;
        this.colTypeEnum = colTypeEnum;
    }

    public IconTitle(int icon, String title) {
        this.icon = icon;
        this.title = title;
        this.colTypeEnum = ArticleColTypeEnum.ICON_2;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public ArticleColTypeEnum getColTypeEnum() {
        return colTypeEnum;
    }

    public void setColTypeEnum(ArticleColTypeEnum colTypeEnum) {
        this.colTypeEnum = colTypeEnum;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getExtraParam() {
        return extraParam;
    }

    public void setExtraParam(String extraParam) {
        this.extraParam = extraParam;
    }

    public Consumer<String> getExtraConsumer() {
        return extraConsumer;
    }

    public void setExtraConsumer(Consumer<String> extraConsumer) {
        this.extraConsumer = extraConsumer;
    }
}

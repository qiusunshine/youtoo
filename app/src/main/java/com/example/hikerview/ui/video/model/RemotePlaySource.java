package com.example.hikerview.ui.video.model;

/**
 * 作者：By 15968
 * 日期：On 2021/6/13
 * 时间：At 20:34
 */

public class RemotePlaySource implements Comparable<RemotePlaySource> {
    private String title;
    private String url;
    private int sort;
    private boolean isParseFinish;

    private String parseResult;

    public RemotePlaySource() {

    }

    public RemotePlaySource(String title, String url, int sort, boolean isParseFinish, String parseResult) {
        this.title = title;
        this.url = url;
        this.sort = sort;
        this.isParseFinish = isParseFinish;
        this.parseResult = parseResult;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public boolean isParseFinish() {
        return isParseFinish;
    }

    public void setParseFinish(boolean parseFinish) {
        isParseFinish = parseFinish;
    }

    public String getParseResult() {
        return parseResult;
    }

    public void setParseResult(String parseResult) {
        this.parseResult = parseResult;
    }

    /**
     * 如何编写出高质量的 equals 和 hashcode 方法？
     * https://juejin.cn/post/6844903954006933517
     *
     * @param o 对比的对象
     * @return 两个对象的对比结果
     */
    @Override
    public boolean equals(Object o) {
        // 1、判断是否等于自身
        if (this == o) return true;
        // 2、判断 o 对象是否为空 或者类型是否为 RemotePlaySource
        if (!(o instanceof RemotePlaySource)) return false;
        // 3、参数类型转换
        RemotePlaySource that = (RemotePlaySource) o;
        // 4、判断两个对象的 url 是否相等
        return url.equals(that.url);
    }

    /**
     * 重写 hashcode 方法，根据 url 返回 hash 值
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public int compareTo(RemotePlaySource o) {
        if (o == null) {
            return -1;
        }
        // SortSet 的比较不走 equals 而是走 compareTo，所以这里要判断一下
        if (equals(o)) {
            return 0;
        }
        return Integer.compare(sort, o.sort);
    }
}
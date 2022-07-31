package com.example.hikerview.ui.search.model;

import androidx.annotation.NonNull;

/**
 * 作者：By 15968
 * 日期：On 2020/2/7
 * 时间：At 22:04
 */
public class SearchGroup  implements Comparable<SearchGroup>{
    private String group;
    private boolean use;

    public SearchGroup(String group, boolean use) {
        this.group = group;
        this.use = use;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    @Override
    public int compareTo(@NonNull SearchGroup o) {
        return this.getGroup().compareTo(o.getGroup());
    }
}

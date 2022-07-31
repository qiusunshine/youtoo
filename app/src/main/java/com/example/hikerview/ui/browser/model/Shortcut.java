package com.example.hikerview.ui.browser.model;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;
import com.example.hikerview.ui.browser.util.CollectionUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2021/8/3
 * 时间：At 11:45
 */

public class Shortcut implements Cloneable {
    private String name;
    private String url;
    private String icon;
    private boolean dragging;
    private boolean hasBackground;
    private String type;

    public Shortcut() {
    }

    public Shortcut(String name, String url, String icon) {
        this.name = name;
        this.url = url;
        this.icon = icon;
    }

    public Shortcut(String name, String url, String icon, String type) {
        this.name = name;
        this.url = url;
        this.icon = icon;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 因为使用fastjson解析在首次初始化时需要耗时30ms左右，因此自己写序列化反序列化
     *
     * @return
     */
    public String toStr() {
        return (this.name == null ? "" : this.name) + "&&&&"
                + (this.url == null ? "" : this.url) + "&&&&"
                + (this.icon == null ? "" : this.icon) + "&&&&"
                + (this.type == null ? "" : this.type);
    }

    private static String toStr(Shortcut shortcut) {
        if (shortcut == null) {
            return "";
        } else {
            return shortcut.toStr();
        }
    }

    public static String toStr(List<Shortcut> shortcuts) {
        if (shortcuts == null || shortcuts.isEmpty()) {
            return "*";
        } else {
            return CollectionUtil.listToString(Stream.of(shortcuts).map(s -> toStr(s)).toList(), "￥￥￥￥");
        }
    }


    private static Shortcut toObj(String s) {
        if (s == null || s.length() <= 0) {
            return null;
        }
        String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, "&&&&");
        if ("视界".equals(ss[0]) && "https://haikuoshijie.cn".equals(ss[1])) {
            ss[0] = "小程序";
            ss[1] = "hiker://mini-program";
        }
        if (ss.length == 3) {
            return new Shortcut(ss[0], ss[1], ss[2]);
        }
        if (ss.length == 4) {
            return new Shortcut(ss[0], ss[1], ss[2], ss[3]);
        }
        return null;
    }

    public static List<Shortcut> toList(String s) {
        if (s == null || s.length() <= 0 || "*".equals(s)) {
            return new ArrayList<>();
        }
        List<Shortcut> shortcuts = new ArrayList<>();
        String[] ss = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, "￥￥￥￥");
        for (String s1 : ss) {
            Shortcut shortcut = toObj(s1);
            if (shortcut == null) {
                continue;
            }
            shortcuts.add(shortcut);
        }
        return shortcuts;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isHasBackground() {
        return hasBackground;
    }

    public void setHasBackground(boolean hasBackground) {
        this.hasBackground = hasBackground;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    @Override
    public Shortcut clone() throws CloneNotSupportedException {
        return (Shortcut) super.clone();
    }
}
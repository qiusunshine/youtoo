package com.example.hikerview.model;

import android.content.Context;

import com.example.hikerview.ui.browser.enums.ShortcutTypeEnum;
import com.example.hikerview.ui.browser.model.Shortcut;
import com.example.hikerview.utils.PreferenceMgr;
import com.example.hikerview.utils.StringUtil;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/1/3
 * 时间：At 21:37
 */
public class BigTextDO extends LitePalSupport {

    public static final String JS_LIST_ORDER_KEY = "js_list_order";
    public static final String ARTICLE_LIST_ORDER_KEY = "article_list_order";
    public static final String BOOKMARK_ORDER_KEY = "bookmark_order";
    public static final String VIDEO_RULES_KEY = "video_rules";
    public static final String xiuTanDialogBlackListPath = "xiuTanDialogBlackList.json";
    public static final String JS_ENABLE_MAP_KEY = "jsEnableMap";
    public static final String SEARCH_LIST_ORDER_KEY = "search_rules_order";
    public static final String PUBLISH_CODE_KEY = "publish_code";
    public static final String PUBLISH_ACCOUNT_KEY = "publish_account_code";
    private static final String HOME_JS_KEY = "home_js";
    private static final String HOME_SUB_KEY = "home_sub";

    private String key;
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static String getHomeJs() {
        BigTextDO bigTextDO = LitePal.where("key = ?", HOME_JS_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            return null;
        }
        return bigTextDO.getValue();
    }

    public static void updateHomeJs(String value) {
        BigTextDO bigTextDO = LitePal.where("key = ?", HOME_JS_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(HOME_JS_KEY);
        }
        bigTextDO.setValue(value);
        bigTextDO.save();
    }


    public static String getShortcuts(Context context) {
        int migrate = PreferenceMgr.getInt(context, "sc_1", 0);
        int should = 2;
        if (migrate < 1) {
            BigTextDO bigTextDO = LitePal.where("key = ?", "shortcuts2").findFirst(BigTextDO.class);
            PreferenceMgr.put(context, "sc_1", should);
            if (bigTextDO == null) {
                return null;
            }
            PreferenceMgr.put(context, "sc_2", bigTextDO.getValue());
            return bigTextDO.getValue();
        } else if (migrate < should) {
            PreferenceMgr.put(context, "sc_1", should);
            String old = PreferenceMgr.getString(context, "sc_2", null);
            if (StringUtil.isEmpty(old)) {
                return old;
            }
            List<Shortcut> shortcuts = Shortcut.toList(old);
            Shortcut poetry = new Shortcut();
            poetry.setType(ShortcutTypeEnum.POETRY.name());
            poetry.setName("长风破浪会有时，直挂云帆济沧海");
            poetry.setUrl("李白");
            shortcuts.add(poetry);
            Shortcut data = new Shortcut();
            data.setType(ShortcutTypeEnum.DATA.name());
            shortcuts.add(data);
            String cuts = Shortcut.toStr(shortcuts);
            BigTextDO.updateShortcuts(context, cuts);
            return cuts;
        } else {
            return PreferenceMgr.getString(context, "sc_2", null);
        }
    }

    public static void updateShortcuts(Context context, String value) {
        PreferenceMgr.put(context, "sc_2", value);
    }

    public static String getHomeSub() {
        BigTextDO bigTextDO = LitePal.where("key = ?", HOME_SUB_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            return null;
        }
        return bigTextDO.getValue();
    }

    public static void updateHomeSub(String value) {
        BigTextDO bigTextDO = LitePal.where("key = ?", HOME_SUB_KEY).findFirst(BigTextDO.class);
        if (bigTextDO == null) {
            bigTextDO = new BigTextDO();
            bigTextDO.setKey(HOME_SUB_KEY);
        }
        bigTextDO.setValue(value);
        bigTextDO.save();
    }
}

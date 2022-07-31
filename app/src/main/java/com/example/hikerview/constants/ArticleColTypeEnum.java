package com.example.hikerview.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/10/7
 * 时间：At 10:35
 */

public enum ArticleColTypeEnum {
    TEXT_1(5, "text_1", 0, 60),
    TEXT_2(6, "text_2", 10, 30),
    TEXT_3(11, "text_3", 10, 20),
    TEXT_4(12, "text_4", 10, 15),
    TEXT_5(44, "text_5", 5, 12),
    TEXT_CENTER_1(18, "text_center_1", 0, 60),
    MOVIE_1(4, "movie_1", 0, 60),
    MOVIE_2(9, "movie_2", 12, 30),
    MOVIE_3(1, "movie_3", 12, 20),
    MOVIE_1_LEFT_PIC(20, "movie_1_left_pic", 0, 60),
    MOVIE_1_VERTICAL_PIC(21, "movie_1_vertical_pic", 0, 60),
    MOVIE_1_VERTICAL_PIC_BLUR(45, "movie_1_vertical_pic_blur", 0, 60),
    MOVIE_3_MARQUEE(27, "movie_3_marquee", 12, 20),
    PIC_1(7, "pic_1", 0, 60),
    PIC_2(8, "pic_2", 12, 30),
    PIC_3(10, "pic_3", 3, 20),
    PIC_1_FULL(19, "pic_1_full", 0, 60),
    PIC_3_SQUARE(30, "pic_3_square", 3, 20),
    PIC_1_CARD(32, "pic_1_card", 0, 60),
    ICON_1_SEARCH(33, "icon_1_search", 0, 60),
    ICON_2(17, "icon_2", 10, 30),
    ICON_2_ROUND(35, "icon_2_round", 10, 30),
    ICON_4(13, "icon_4", 10, 15),
    ICON_4_CARD(36, "icon_4_card", 10, 15),
    ICON_3_SMALL(25, "icon_small_3", 10, 20),
    ICON_4_SMALL(14, "icon_small_4", 10, 15),
    ICON_4_ROUND(15, "icon_round_4", 10, 15),
    ICON_4_ROUND_SMALL(16, "icon_round_small_4", 10, 15),
    ICON_5(49, "icon_5", 10, 12),
    LONG_TEXT(22, "long_text", 0, 60),
    RICH_TEXT(24, "rich_text", 0, 60),
    AVATAR(28, "avatar", 0, 60),
    X5_WEB_VIEW(34, "x5_webview_single", 0, 60),
    HEADER(2, "header", 0, 60),
    FOOTER(3, "footer", 0, 60),
    LINE(23, "line", 0, 60),
    LINE_BLANK(26, "line_blank", 0, 60),
    BLANK_BLOCK(29, "blank_block", 0, 60),
    BIG_BLANK_BLOCK(31, "big_blank_block", 0, 60),
    FLEX_BUTTON(37, "flex_button", 2, 60),
    SCROLL_BUTTON(38, "scroll_button", 2, 60),
    CARD_PIC_2(39, "card_pic_2", 15, 30),
    CARD_PIC_1(42, "card_pic_1", 15, 60),
    CARD_PIC_2_2(40, "card_pic_2_2", 15, 30),
    CARD_PIC_2_2_LEFT(41, "card_pic_2_2_left", 15, 30),
    input(43, "input", 15, 60),
    BIG_BIG_BLANK_BLOCK(46, "big_big_blank_block", 0, 60),
    CARD_MULTI(137, "card_multi", 10, 60),
    CARD_COL_4_2(138, "card_col_4_2", 10, 60),
    CARD_COL_4(139, "card_col_4", 10, 60),
    CARD_COL_3(140, "card_col_3", 10, 60);
    private final static int maxNow = 49;
    public static final int TEMP_TYPE_ICON_3_LIST = -3;

    private final int itemType;
    private final String code;
    private final int leftRight;
    private final int spanCount;

    ArticleColTypeEnum(int itemType, String code, int leftRight, int spanCount) {
        this.itemType = itemType;
        this.code = code;
        this.leftRight = leftRight;
        this.spanCount = spanCount;
    }

    public String getCode() {
        return code;
    }

    public int getItemType() {
        return itemType;
    }

    public static ArticleColTypeEnum getByItemType(int itemType) {
        for (ArticleColTypeEnum value : values()) {
            if (value.getItemType() == itemType) {
                return value;
            }
        }
        if (TEMP_TYPE_ICON_3_LIST == itemType) {
            return ICON_3_SMALL;
        }
        return MOVIE_3;
    }

    public static int getLeftRightByItemType(int itemType) {
        if (TEMP_TYPE_ICON_3_LIST == itemType) {
            return ICON_3_SMALL.getLeftRight();
        }
        for (ArticleColTypeEnum value : values()) {
            if (value.getItemType() == itemType) {
                return value.getLeftRight();
            }
        }
        return 0;
    }

    public static int getSpanCountByItemType(int itemType) {
        for (ArticleColTypeEnum value : values()) {
            if (value.getItemType() == itemType) {
                return value.getSpanCount();
            }
        }
        if (TEMP_TYPE_ICON_3_LIST == itemType) {
            return ICON_3_SMALL.getSpanCount();
        }
        return 60;
    }

    public static ArticleColTypeEnum getByCode(String code) {
        for (ArticleColTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MOVIE_3;
    }

    public static int getItemTypeByCode(String code) {
        for (ArticleColTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value.getItemType();
            }
        }
        return MOVIE_3.getItemType();
    }

    public static String[] getCodeArray() {
        List<String> list = new ArrayList<>();
        for (ArticleColTypeEnum value : values()) {
            if (value != FOOTER && value != HEADER) {
                list.add(value.getCode());
            }
        }
        String[] array = new String[list.size()];
        return list.toArray(array);
    }

    public int getLeftRight() {
        return leftRight;
    }

    public int getSpanCount() {
        return spanCount;
    }
}

package com.example.hikerview.ui.video;

/**
 * 作者：By hdy
 * 日期：On 2019/4/30
 * 时间：At 14:13
 */
public enum PlayerEnum {
    PLAYER_TWO(2, "默认播放器"),
    SYSTEM(29, "系统播放器"),
    X5(0, "内置X5播放器"),
    MX_PLAYER(11, "MxPlayer"),
    X_PLAYER(12, "XPlayer"),
    REEX(28, "Reex"),
    KM_PLAYER(13, "KMPlayer"),
    MO_BO_PLAYER(14, "MoboPlayer"),
    QQ_PLAYER(15, "QQ浏览器"),
    VLC_PLAYER(16, "VLC"),
    UC_PLAYER(17, "UC浏览器"),
    UC_INTL_PLAYER(20, "UC国际版"),
    DAN_DAN_PLAYER(18, "弹弹Play概念版"),
    N_PLAYER(19, "nPlayer"),
    WEB_VIDEO_CASTER(25, "Web Video Caster"),
    LUA_PLAYER(26, "LUA PLAYER Pro"),
    KODI(27, "Kodi");


    private int code;
    private String name;

    PlayerEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String findName(int code) {
        PlayerEnum[] enums = PlayerEnum.values();
        for (PlayerEnum anEnum : enums) {
            if (anEnum.getCode() == code) {
                return anEnum.getName();
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

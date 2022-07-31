package com.example.hikerview.ui.browser.enums

/**
 * 作者：By 15968
 * 日期：On 2021/12/26
 * 时间：At 10:26
 */
enum class ShortcutTypeEnum(
    val desc: String,
    val viewType: Int,
    val spanCount: Int,
    val leftRight: Int
) {
    DEFAULT("默认", 0, 4, 0),
    DATA("统计", 1, 20, 15),
    POETRY("诗词", 2, 20, 15);

    companion object {
        fun getNames(): Array<String> {
            return values().map { it.desc }.toTypedArray()
        }

        fun getName(code: String?): String {
            for (value in values()) {
                if (value.name == code) {
                    return value.desc
                }
            }
            return DEFAULT.desc
        }

        fun getCode(desc: String?): String {
            for (value in values()) {
                if (value.desc == desc) {
                    return value.name
                }
            }
            return DEFAULT.name
        }

        fun getByCode(name: String?): ShortcutTypeEnum {
            for (value in values()) {
                if (value.name == name) {
                    return value
                }
            }
            return DEFAULT
        }

        fun getLRByViewType(vt: Int): Int {
            for (value in values()) {
                if (value.viewType == vt) {
                    return value.leftRight
                }
            }
            return DEFAULT.leftRight
        }
    }
}
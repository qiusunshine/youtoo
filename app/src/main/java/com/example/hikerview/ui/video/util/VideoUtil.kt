package com.example.hikerview.ui.video.util

import android.app.Activity
import android.content.pm.ActivityInfo
import com.alibaba.fastjson.JSONObject
import com.example.hikerview.service.parser.HttpParser
import com.example.hikerview.ui.browser.util.CollectionUtil
import com.example.hikerview.ui.home.model.article.extra.BaseExtra
import com.example.hikerview.ui.video.VideoChapter
import com.example.hikerview.ui.view.CustomCenterRecyclerViewPopup
import com.example.hikerview.utils.StringUtil
import com.example.hikerview.utils.ToastMgr
import com.lxj.xpopup.XPopup

/**
 * 作者：By 15968
 * 日期：On 2022/1/29
 * 时间：At 23:22
 */
object VideoUtil {

    fun getMemoryId(url: String?, chapters: MutableList<VideoChapter>?): String? {
        var id = if (url != null) url.split(";")[0] else url
        if (CollectionUtil.isNotEmpty(chapters)) {
            for (chapter in chapters!!) {
                if (chapter.isUse) {
                    if (StringUtil.isNotEmpty(chapter.extra)) {
                        val extra = JSONObject.parseObject(
                            chapter.extra,
                            BaseExtra::class.java
                        )
                        if (StringUtil.isNotEmpty(extra.id)) {
                            id = extra.id
                        }
                    }
                    break
                }
            }
        }
        return id
    }


    fun showVideoSwitch(
        activity: Activity,
        si: Int,
        url: String?,
        forError: Boolean,
        click: (position: Int) -> Unit
    ) {
        val realUrl = HttpParser.getRealUrlFilterHeaders(url)
        val playData = HttpParser.getPlayData(realUrl)
        if (playData == null || CollectionUtil.isEmpty(playData.urls) || CollectionUtil.isEmpty(
                playData.names
            ) || playData.names.size < 2
        ) {
            return
        }
        val land = activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val names = CollectionUtil.toStrArray(playData.names)
        var switchIndex = si
        if (switchIndex >= names.size) {
            switchIndex = 0
        }
        if (names.isEmpty()) {
            ToastMgr.shortBottomCenter(activity, "数据格式有误")
            return
        }
        names[switchIndex] = "““" + names[switchIndex] + "””"
        val popup = CustomCenterRecyclerViewPopup(activity)
            .withTitle(if (forError) "播放失败，试试换条线路吧" else "线路切换")
            .with(names, 2, object : CustomCenterRecyclerViewPopup.ClickListener {
                override fun click(text: String, position: Int) {
                    click(position)
                }

                override fun onLongClick(url: String, position: Int) {}
            })
        XPopup.Builder(activity)
            .hasNavigationBar(false)
            .isRequestFocus(!land)
            .asCustom(popup)
            .show()
    }
}
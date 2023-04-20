package com.example.hikerview.ui.miniprogram.service

import android.content.Context
import com.alibaba.fastjson.JSON
import com.example.hikerview.constants.CollectionTypeConstant
import com.example.hikerview.model.ViewCollectionExtraData
import com.example.hikerview.model.ViewHistory
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.detail.DetailUIHelper
import com.example.hikerview.ui.miniprogram.MiniProgramRouter
import com.example.hikerview.ui.miniprogram.data.HistoryDTO
import com.example.hikerview.ui.miniprogram.data.RuleDTO
import com.example.hikerview.utils.FileUtil
import com.example.hikerview.utils.HeavyTaskUtil
import com.example.hikerview.utils.PreferenceMgr
import com.example.hikerview.utils.UriUtils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * 作者：By 15968
 * 日期：On 2022/7/12
 * 时间：At 9:56
 */
object HistoryMemoryService {

    fun filePath(): String {
        return UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "mini-program-history.json"
    }

    /**
     * 记忆页面
     */
    fun memoryPage(ruleDTO: RuleDTO, pageTitle: String, picUrl: String?) {
        HeavyTaskUtil.executeNewTask {
            val historyCount = PreferenceMgr.getInt(Application.getContext(), "historyCount", 300)
            if (historyCount <= 0) {
                return@executeNewTask
            }
            val localRuleDTO = findLocalRule(ruleDTO)
            val history = HistoryDTO()
            history.ruleDTO = ruleDTO.copy()
            history.pic = picUrl
            if (localRuleDTO != null) {
                if (localRuleDTO.url == ruleDTO.url) {
                    //主页不记忆
                    return@executeNewTask
                }
                simplyRuleDTO(history.ruleDTO!!, localRuleDTO)
            }
            val exist = loadMemoryList()
            for (ii in exist) {
                if (ruleDTO.url.equals(ii.ruleDTO?.url) && pageTitle == ii.pageTitle) {
                    history.clickPos = ii.clickPos
                    history.clickText = ii.clickText
                    history.extraData = ii.extraData
                    history.top = ii.top
                    exist.remove(ii)
                    break
                }
            }
            if (exist.size >= 150) {
                for (indexedValue in exist.withIndex()) {
                    if (indexedValue.value.top != true) {
                        exist.removeAt(indexedValue.index)
                        break
                    }
                }
            }
            history.pageTitle = pageTitle
            history.showTitle = pageTitle
            history.time = System.currentTimeMillis()
            exist.add(history)
            FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
        }
    }

    /**
     * 更新标题
     */
    fun updatePage(ruleDTO: RuleDTO, pageTitle: String, showTitle: String) {
        HeavyTaskUtil.executeNewTask {
            val exist = loadMemoryList()
            for (historyDTO in exist) {
                if (ruleDTO.url.equals(historyDTO.ruleDTO?.url) && pageTitle == historyDTO.pageTitle) {
                    historyDTO.showTitle = showTitle
                    FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
                    break
                }
            }
        }
    }

    /**
     * 更新图片
     */
    fun updatePic(ruleDTO: RuleDTO, pageTitle: String, picUrl: String) {
        HeavyTaskUtil.executeNewTask {
            val exist = loadMemoryList()
            for (historyDTO in exist) {
                if (ruleDTO.url.equals(historyDTO.ruleDTO?.url) && pageTitle == historyDTO.pageTitle) {
                    historyDTO.pic = picUrl
                    FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
                    break
                }
            }
        }
    }

    /**
     * 更新页面参数
     */
    fun updateParams(ruleDTO: RuleDTO, pageTitle: String, params: String) {
        HeavyTaskUtil.executeNewTask {
            val exist = loadMemoryList()
            for (historyDTO in exist) {
                if (ruleDTO.url.equals(historyDTO.ruleDTO?.url) && pageTitle == historyDTO.pageTitle) {
                    historyDTO.ruleDTO?.params = params
                    FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
                    break
                }
            }
        }
    }

    /**
     * 记忆点击位置
     */
    fun memoryClick(
        pageUrl: String?,
        pageTitle: String,
        clickPos: Int,
        clickText: String,
        pageIndex: Int = -1
    ) {
        if (DetailUIHelper.getTitleText(clickText).length > 25) {
            return
        }
        HeavyTaskUtil.executeNewTask {
            val exist = loadMemoryList()
            for (historyDTO in exist) {
                if (pageUrl == historyDTO.ruleDTO?.url && pageTitle == historyDTO.pageTitle) {
                    historyDTO.clickPos = clickPos
                    historyDTO.clickText = clickText
                    if (pageIndex >= 0) {
                        val extraData = historyDTO.getExtraJson()
                        extraData.pageIndex = pageIndex
                        historyDTO.extraData = extraData.toJson()
                    }
                    FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
                    break
                }
            }
        }
    }

    /**
     * 清空记忆
     */
    fun clearPages() {
        val exist = loadMemoryList()
        exist.removeAll {
            it.top != true
        }
        FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
    }

    fun getHistory(history: ViewHistory): HistoryDTO? {
        val exist = loadMemoryList()
        for (historyDTO in exist) {
            if (history.url == (historyDTO.ruleDTO?.title + "@" + historyDTO.ruleDTO?.url) && history.title == historyDTO.showTitle) {
                return historyDTO
            }
        }
        return null
    }

    fun startPage(context: Context, history: ViewHistory) {
        startPage(context, history, false)
    }

    fun startPage(context: Context, history: ViewHistory, playLast: Boolean = false) {
        val exist = loadMemoryList()
        for (historyDTO in exist) {
            if (history.url == (historyDTO.ruleDTO?.title + "@" + historyDTO.ruleDTO?.url) && history.title == historyDTO.showTitle) {
                historyDTO.ruleDTO?.let {
                    val localRuleDTO = findLocalRule(it)
                    if (localRuleDTO != null) {
                        if (it.rule?.startsWith("localRule@") == true) {
                            it.rule = localRuleDTO.rule
                        }
                        if (it.pages?.startsWith("localRule@") == true) {
                            it.pages = localRuleDTO.pages
                        }
                        if (it.preRule?.startsWith("localRule@") == true) {
                            it.preRule = localRuleDTO.preRule
                        }
                        if (it.nextRule?.startsWith("localRule@") == true) {
                            it.nextRule = localRuleDTO.nextRule
                        }
                    }
                    MiniProgramRouter.startMiniProgram(
                        context,
                        it.url ?: "",
                        historyDTO.pageTitle ?: "",
                        it,
                        true,
                        null,
                        null,
                        playLast,
                        fromHome = false,
                        picUrl = historyDTO.pic
                    )
                }
                return
            }
        }
    }

    fun deleteHistoryDTO(showTitle: String, url: String, ignoreTop: Boolean = true) {
        val exist = loadMemoryList()
        for (historyDTO in exist) {
            if (url == (historyDTO.ruleDTO?.title + "@" + historyDTO.ruleDTO?.url) && showTitle == historyDTO.showTitle) {
                if (historyDTO.top == true && ignoreTop) {
                    break
                }
                exist.remove(historyDTO)
                FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
                break
            }
        }
    }

    fun batchDelete(list: List<ViewHistory>, ignoreTop: Boolean = true) {
        val exist = loadMemoryList()
        val iterator = exist.iterator()
        while (iterator.hasNext()) {
            val historyDTO = iterator.next()
            if (historyDTO.top == true && ignoreTop) {
                continue
            }
            for (viewHistory in list) {
                val url = viewHistory.url
                val showTitle = viewHistory.title
                if (url == (historyDTO.ruleDTO?.title + "@" + historyDTO.ruleDTO?.url) && showTitle == historyDTO.showTitle) {
                    iterator.remove()
                    break
                }
            }
        }
        FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
    }

    fun updateTop(showTitle: String, url: String, top: Boolean) {
        val exist = loadMemoryList()
        for (historyDTO in exist) {
            if (url == (historyDTO.ruleDTO?.title + "@" + historyDTO.ruleDTO?.url) && showTitle == historyDTO.showTitle) {
                historyDTO.top = top
                FileUtil.stringToFile(JSON.toJSONString(exist), filePath())
                break
            }
        }
    }

    fun getHistoryPages(): MutableList<ViewHistory> {
        val list = loadMemoryList()
        return list.map {
            val viewHistory = ViewHistory()
            viewHistory.group = "小程序"
            viewHistory.title = it.showTitle
            viewHistory.url = it.ruleDTO?.title + "@" + it.ruleDTO?.url
            viewHistory.time = Date(it.time)
            viewHistory.type = CollectionTypeConstant.DETAIL_LIST_VIEW
            viewHistory.lastClick = DetailUIHelper.getTitleText(it.clickText)
            viewHistory.params = if (it.top == true) "true" else null
            viewHistory.picUrl = it.pic
            viewHistory
        }.toMutableList()
    }

    fun getHistory(ruleDTO: RuleDTO, pageTitle: String): HistoryDTO? {
        val list = loadMemoryList()
        for (historyDTO in list.reversed()) {
            if (ruleDTO.url.equals(historyDTO.ruleDTO?.url) && pageTitle == historyDTO.pageTitle) {
                return historyDTO
            }
        }
        return null
    }

    fun getHistory(pageTitle: String?, url: String?): HistoryDTO? {
        if (pageTitle == null || url == null) {
            return null
        }
        val list = loadMemoryList()
        for (historyDTO in list.reversed()) {
            if (url == historyDTO.ruleDTO?.url && pageTitle == historyDTO.pageTitle) {
                return historyDTO
            }
        }
        return null
    }

    fun updateExtra(
        pageTitle: String?,
        url: String?,
        viewCollectionExtraData: ViewCollectionExtraData
    ) {
        if (pageTitle == null || url == null) {
            return
        }
        val list = loadMemoryList()
        for (historyDTO in list.reversed()) {
            if (url == historyDTO.ruleDTO?.url && pageTitle == historyDTO.pageTitle) {
                historyDTO.extraData = JSON.toJSONString(viewCollectionExtraData)
                FileUtil.stringToFile(JSON.toJSONString(list), filePath())
                return
            }
        }
    }

    fun update(
        dto: HistoryDTO?
    ) {
        if (dto == null) {
            return
        }
        val list = loadMemoryList()
        for (historyDTO in list.reversed()) {
            if (dto.ruleDTO?.url == historyDTO.ruleDTO?.url && dto.pageTitle == historyDTO.pageTitle) {
                historyDTO.extraData = dto.extraData
                historyDTO.clickText = dto.clickText
                historyDTO.clickPos = dto.clickPos
                FileUtil.stringToFile(JSON.toJSONString(list), filePath())
                return
            }
        }
    }

    private fun loadMemoryList(): MutableList<HistoryDTO> {
        val path = filePath()
        try {
            if (File(path).exists()) {
                return JSON.parseArray(FileUtil.fileToString(path), HistoryDTO::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    private fun simplyRuleDTO(ruleDTO: RuleDTO, localRuleDTO: RuleDTO) {
        if (ruleDTO.rule == localRuleDTO.rule) {
            ruleDTO.rule = "localRule@" + localRuleDTO.title
        }
        if (ruleDTO.pages == localRuleDTO.pages) {
            ruleDTO.pages = "localRule@" + localRuleDTO.title
        }
        if (ruleDTO.preRule == localRuleDTO.preRule) {
            ruleDTO.preRule = "localRule@" + localRuleDTO.title
        }
        if (ruleDTO.nextRule == localRuleDTO.nextRule) {
            ruleDTO.nextRule = "localRule@" + localRuleDTO.title
        }
    }

    private fun findLocalRule(ruleDTO: RuleDTO?): RuleDTO? {
        if (ruleDTO == null) {
            return ruleDTO
        }
        for (dto in MiniProgramRouter.data) {
            if (ruleDTO.title == dto.title) {
                return dto
            }
        }
        return null
    }
}
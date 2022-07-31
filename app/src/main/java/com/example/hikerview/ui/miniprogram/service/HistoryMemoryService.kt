package com.example.hikerview.ui.miniprogram.service

import android.content.Context
import com.alibaba.fastjson.JSON
import com.example.hikerview.constants.CollectionTypeConstant
import com.example.hikerview.model.ViewHistory
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.detail.DetailUIHelper
import com.example.hikerview.ui.miniprogram.MiniProgramRouter
import com.example.hikerview.ui.miniprogram.data.HistoryDTO
import com.example.hikerview.ui.miniprogram.data.RuleDTO
import com.example.hikerview.utils.FileUtil
import com.example.hikerview.utils.HeavyTaskUtil
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
    const val FILE = "mini-program-history.json"

    /**
     * 记忆页面
     */
    fun memoryPage(ruleDTO: RuleDTO, pageTitle: String) {
        HeavyTaskUtil.executeNewTask {
            val localRuleDTO = findLocalRule(ruleDTO)
            val history = HistoryDTO()
            history.ruleDTO = ruleDTO.copy()
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
                    exist.remove(ii)
                    break
                }
            }
            if (exist.size >= 100) {
                exist.removeAt(0)
            }
            history.pageTitle = pageTitle
            history.showTitle = pageTitle
            history.time = System.currentTimeMillis()
            exist.add(history)
            val path = UriUtils.getRootDir(Application.getContext()) + File.separator + FILE
            FileUtil.stringToFile(JSON.toJSONString(exist), path)
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
                    val path = UriUtils.getRootDir(Application.getContext()) + File.separator + FILE
                    FileUtil.stringToFile(JSON.toJSONString(exist), path)
                    break
                }
            }
        }
    }

    /**
     * 记忆点击位置
     */
    fun memoryClick(pageUrl: String?, pageTitle: String, clickPos: Int, clickText: String) {
        if (DetailUIHelper.getTitleText(clickText).length > 25) {
            return
        }
        HeavyTaskUtil.executeNewTask {
            val exist = loadMemoryList()
            for (historyDTO in exist) {
                if (pageUrl == historyDTO.ruleDTO?.url && pageTitle == historyDTO.pageTitle) {
                    historyDTO.clickPos = clickPos
                    historyDTO.clickText = clickText
                    val path = UriUtils.getRootDir(Application.getContext()) + File.separator + FILE
                    FileUtil.stringToFile(JSON.toJSONString(exist), path)
                    break
                }
            }
        }
    }

    /**
     * 清空记忆
     */
    fun clearPages() {
        val path = UriUtils.getRootDir(Application.getContext()) + File.separator + FILE
        if (File(path).exists()) {
            File(path).delete()
        }
    }

    fun startPage(context: Context, history: ViewHistory) {
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
                    }
                    MiniProgramRouter.startMiniProgram(
                        context,
                        it.url ?: "",
                        historyDTO.pageTitle ?: "",
                        it
                    )
                }
                return
            }
        }
    }

    fun deleteHistoryDTO(showTitle: String, url: String) {
        val exist = loadMemoryList()
        for (historyDTO in exist) {
            if (url == (historyDTO.ruleDTO?.title + "@" + historyDTO.ruleDTO?.url) && showTitle == historyDTO.showTitle) {
                exist.remove(historyDTO)
                val path = UriUtils.getRootDir(Application.getContext()) + File.separator + FILE
                FileUtil.stringToFile(JSON.toJSONString(exist), path)
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

    private fun loadMemoryList(): MutableList<HistoryDTO> {
        val path = UriUtils.getRootDir(Application.getContext()) + File.separator + FILE
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
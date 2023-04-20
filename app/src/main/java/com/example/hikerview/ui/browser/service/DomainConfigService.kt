package com.example.hikerview.ui.browser.service

import com.alibaba.fastjson.JSON
import com.example.hikerview.ui.Application
import com.example.hikerview.ui.browser.data.DomainConfig
import com.example.hikerview.utils.FileUtil
import com.example.hikerview.utils.StringUtil
import com.example.hikerview.utils.UriUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * 作者：By 15968
 * 日期：On 2022/8/6
 * 时间：At 11:10
 */
object DomainConfigService {

    var configMap: MutableMap<String, DomainConfig> = HashMap()

    fun getConfig(dom: String?): DomainConfig {
        if (dom == null) {
            return DomainConfig()
        }
        return if (configMap.containsKey(dom)) {
            configMap[dom]!!
        } else DomainConfig()
    }

    fun initConfigs() {
        GlobalScope.launch(Dispatchers.IO) {
            val file = File(getFilePath())
            if (file.exists()) {
                val str: String? = FileUtil.fileToString(file.absolutePath)
                if (StringUtil.isNotEmpty(str)) {
                    val c = JSON.parseArray(str, DomainConfig::class.java)
                    for (domainConfig in c) {
                        domainConfig.let {
                            if (it.dom != null) {
                                configMap[it.dom!!] = domainConfig
                            }
                        }
                    }
                }
            }
        }
    }

    fun restoreConfig() {
        val list = configMap.values
        FileUtil.stringToFile(JSON.toJSONString(list), getFilePath())
    }

    fun allowGetLocation(origin: String) {
        val dom = StringUtil.getDom(origin)
        val domainConfig = getConfig(dom)
        domainConfig.dom = dom
        domainConfig.allowGeo = true
        configMap[dom] = domainConfig
        restoreConfig()
    }

    private fun getFilePath(): String {
        return UriUtils.getRootDir(Application.getContext()) + File.separator + "rules" + File.separator + "_domain_config.json"
    }
}
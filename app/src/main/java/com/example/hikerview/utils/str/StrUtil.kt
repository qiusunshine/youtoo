package com.example.hikerview.utils.str

import android.text.TextUtils
import java.io.File
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.regex.Pattern

/**
 * 作者：By 15968
 * 日期：On 2021/10/19
 * 时间：At 14:14
 */
object StrUtil {

    private val LOWER_CASES = arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
    private val UPPER_CASES = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    private val NUMS_LIST = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    private val SYMBOLS_ARRAY = arrayOf("!", "~", "^", "_", "*")

    fun genRandomPwd(pwd_len: Int): String? {
        return genRandomPwd(pwd_len, false)
    }

    /**
     * 生成随机密码
     *
     * @param pwd_len 密码长度
     * @param simple  简单模式
     * @return 密码的字符串
     */
    fun genRandomPwd(pwd_len: Int, simple: Boolean): String? {
        if (pwd_len < 6 || pwd_len > 20) {
            return ""
        }
        var upper: Int
        var num = 0
        var symbol = 0
        var lower: Int = pwd_len / 2
        if (simple) {
            upper = pwd_len - lower
        } else {
            upper = (pwd_len - lower) / 2
            num = (pwd_len - lower) / 2
            symbol = pwd_len - lower - upper - num
        }
        val pwd = StringBuilder()
        val random = Random()
        var position = 0
        while (lower + upper + num + symbol > 0) {
            if (lower > 0) {
                position = random.nextInt(pwd.length + 1)
                pwd.insert(position, LOWER_CASES[random.nextInt(LOWER_CASES.size)])
                lower--
            }
            if (upper > 0) {
                position = random.nextInt(pwd.length + 1)
                pwd.insert(position, UPPER_CASES[random.nextInt(UPPER_CASES.size)])
                upper--
            }
            if (num > 0) {
                position = random.nextInt(pwd.length + 1)
                pwd.insert(position, NUMS_LIST[random.nextInt(NUMS_LIST.size)])
                num--
            }
            if (symbol > 0) {
                position = random.nextInt(pwd.length + 1)
                pwd.insert(position, SYMBOLS_ARRAY[random.nextInt(SYMBOLS_ARRAY.size)])
                symbol--
            }
            println(pwd.toString())
        }
        return pwd.toString()
    }

    fun arrayToString(list: Array<String?>?, fromIndex: Int, cha: String?): String? {
        return arrayToString(list, fromIndex, list?.size ?: 0, cha)
    }

    fun arrayToString(list: Array<String?>?, fromIndex: Int, endIndex: Int, cha: String?): String? {
        val builder = StringBuilder()
        if (list == null || list.size <= fromIndex) {
            return ""
        } else if (list.size <= 1) {
            return list[0]
        } else {
            builder.append(list[fromIndex])
        }
        var i = 1 + fromIndex
        while (i < list.size && i < endIndex) {
            builder.append(cha).append(list[i])
            i++
        }
        return builder.toString()
    }


    fun listToString(list: List<String?>?, cha: String?): String? {
        val builder = StringBuilder()
        if (list == null || list.isEmpty()) {
            return ""
        } else if (list.size <= 1) {
            return list[0]
        } else {
            builder.append(list[0])
        }
        for (i in 1 until list.size) {
            builder.append(cha).append(list[i])
        }
        return builder.toString()
    }

    fun listToString(list: List<String?>?, fromIndex: Int, cha: String?): String? {
        val builder = StringBuilder()
        if (list == null || list.size <= fromIndex) {
            return ""
        } else if (list.size <= 1) {
            return list[0]
        } else {
            builder.append(list[fromIndex])
        }
        for (i in fromIndex + 1 until list.size) {
            builder.append(cha).append(list[i])
        }
        return builder.toString()
    }

    fun listToString(list: List<String?>?): String? {
        return listToString(list, "&&")
    }

    fun replaceBlank(str: String?): String? {
        return try {
            var dest = ""
            if (str != null) {
                val p = Pattern.compile("\\s*|\t|\r|\n")
                val m = p.matcher(str)
                dest = m.replaceAll("")
            }
            dest
        } catch (e: Exception) {
            str
        }
    }

    fun replaceLineBlank(str: String?): String? {
        return try {
            str?.replace("\n".toRegex(), "")
        } catch (e: Exception) {
            str
        }
    }

    fun trimBlanks(str: String?): String? {
        if (str == null || str.isEmpty()) {
            return str
        }
        str.trim()
        var len = str.length
        var st = 0
        while (st < len && (str[st] == '\n' || str[st] == '\r' || str[st] == '\t')) {
            st++
        }
        while (st < len && (str[len - 1] == '\n' || str[len - 1] == '\r' || str[len - 1] == '\t')) {
            len--
        }
        return if (st > 0 || len < str.length) str.substring(st, len) else str
    }

    fun equalsDomUrl(url1: String?, url2: String?): Boolean {
        if (url1 == null) {
            return url2 == null
        }
        if (url2 == null) {
            return false
        }
        var pUrl: String = url1
        if (pUrl.endsWith("/")) {
            pUrl = pUrl.substring(0, pUrl.length - 1)
        }
        var sUrl: String = url2
        if (sUrl.endsWith("/")) {
            sUrl = sUrl.substring(0, sUrl.length - 1)
        }
        return pUrl == sUrl
    }

    fun getHomeUrl(url: String): String? {
        return if (isEmpty(url)) {
            url
        } else {
            val dom = getDom(url)
            if (url.startsWith("https")) {
                "https://$dom/"
            } else {
                "http://$dom/"
            }
        }
    }


    fun isHexStr(str: String): Boolean {
        var str = str
        var flag = false
        if (TextUtils.isEmpty(str)) {
            return false
        }
        if (!str.startsWith("#")) {
            str = "#$str"
        }
        if (str.length != 7 && str.length != 9) {
            return false
        }
        for (i in 1 until str.length) {
            val cc = str[i]
            if (cc == '0' || cc == '1' || cc == '2' || cc == '3' || cc == '4' || cc == '5' || cc == '6' || cc == '7' || cc == '8' || cc == '9' || cc == 'A' || cc == 'B' || cc == 'C' || cc == 'D' || cc == 'E' || cc == 'F' || cc == 'a' || cc == 'b' || cc == 'c' || cc == 'd' || cc == 'e' || cc == 'f') {
                flag = true
            }
        }
        return flag
    }

    // 判断一个字符是否是中文
    private fun isChinese(c: Char): Boolean {
        return c.toInt() in 0x4E00..0x9FA5 // 根据字节码判断
    }

    // 判断一个字符串是否含有中文
    fun containsChinese(str: String?): Boolean {
        if (str == null) return false
        for (c in str.toCharArray()) {
            if (isChinese(c)) return true
        }
        return false
    }

    fun decodeConflictStr(str: String): String {
        return if (isEmpty(str)) {
            str
        } else str.replace("？？", "?").replace("＆＆", "&").replace("；；", ";")
    }

    fun isUrl(str: String): Boolean {
        if (TextUtils.isEmpty(str)) {
            return false
        }
        return if (isWebUrl(str)) {
            true
        } else !containsChinese(str) && str.contains(".") && !str.contains(" ")
    }

    fun getDom(u: String?): String? {
        if (TextUtils.isEmpty(u)) {
            return u
        }
        var url: String = u!!
        try {
            url = url.replaceFirst("http://".toRegex(), "").replaceFirst("https://".toRegex(), "")
            val urls = url.split("/").toTypedArray()
            if (urls.isNotEmpty()) {
                return urls[0]
            }
        } catch (e: Exception) {
            return null
        }
        return url
    }

    fun removeDom(u: String?): String? {
        if (isEmpty(u)) {
            return u
        }
        var url = u!!
        try {
            url = url.replaceFirst("http://".toRegex(), "").replaceFirst("https://".toRegex(), "")
            val urls: Array<String?> = url.split("/").toTypedArray()
            if (urls.size > 1) {
                return arrayToString(urls, 1, "/")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return url
    }

    fun getSimpleDom(url: String?): String? {
        val dom = getDom(url)
        if (isEmpty(url) || isEmpty(dom) || url == dom) {
            return url
        }
        val s = dom!!.split("\\.").toTypedArray()
        return if (s.size < 3) {
            dom
        } else dom.substring(dom.indexOf(".", s.size - 2) + 1)
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param keyword
     * @return keyword
     */
    fun escapeExprSpecialWord(keyword: String?): String? {
        return if (isEmpty(keyword)) {
            val fbsArr = arrayOf("\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|")
            var k = keyword!!
            for (key in fbsArr) {
                if (k.contains(key)) {
                    k = k.replace(key, "\\" + key)
                }
            }
            k
        } else {
            keyword
        }
    }

    /**
     * 删除正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param keyword
     * @return keyword
     */
    fun removeSpecialWord(keyword: String): String? {
        var keyword = keyword
        if (!TextUtils.isEmpty(keyword)) {
            val fbsArr = arrayOf("\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|")
            for (key in fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "")
                }
            }
        }
        return keyword
    }

    private val FilePattern = Pattern.compile("[\\\\/:*?\"<>|]")

    fun filenameFilter(str: String?): String? {
        return if (str == null) null else FilePattern.matcher(str).replaceAll("_").replace(File.separator, "_")
    }

    fun getBaseUrl(url: String): String {
        if (isEmpty(url)) {
            return url
        }
        val baseUrls = url.replace("http://", "").replace("https://", "")
        val baseUrl2 = baseUrls.split("/").toTypedArray()[0]
        return if (url.startsWith("https")) {
            "https://$baseUrl2"
        } else {
            "http://$baseUrl2"
        }
    }

    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.isEmpty()
    }

    fun isNotEmpty(str: CharSequence?): Boolean {
        return !isEmpty(str)
    }

    fun isUTF8(str: String): Boolean {
        return try {
            str.toByteArray(charset("utf-8"))
            true
        } catch (e: UnsupportedEncodingException) {
            false
        }
    }


    fun convertBlankToTagP(content: String): String? {
        return try {
            if (isEmpty(content)) {
                content
            } else if (!content.contains("\n")) {
                content
            } else {
                content.replace("\n", "<br>")
            }
        } catch (e: Exception) {
            content
        }
    }


    fun simplyGroup(title: String): String {
        return if (isEmpty(title)) {
            title
        } else title.replace("①", "")
                .replace("②", "")
                .replace("③", "")
                .replace("④", "")
                .replace("⑤", "")
                .replace("⑥", "")
                .replace("⑦", "")
                .replace("⑧", "")
                .replace("⑨", "")
                .replace("⑩", "")
    }

    /**
     * 判读是否是emoji
     *
     * @param codePoint
     * @return
     */
    fun getIsEmoji(codePoint: Char): Boolean {
        return !(codePoint.toInt() == 0x0 || codePoint.toInt() == 0x9 || codePoint.toInt() == 0xA
                || codePoint.toInt() == 0xD
                || codePoint.toInt() in 0x20..0xD7FF
                || codePoint.toInt() in 0xE000..0xFFFD
                || codePoint.toInt() in 0x10000..0x10FFFF)
    }


    fun getIsSp(codePoint: Char): Boolean {
        return Character.getType(codePoint) > Character.LETTER_NUMBER
    }

    /**
     * 判断搜索框内容是否包含特殊字符
     *
     * @param str
     * @return
     */
    fun hasSpWord(str: String?): Boolean {
        val limitEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@①#￥%……&*（）——+|{}【】‘；：”“’。，、？]"
        val pattern = Pattern.compile(limitEx)
        val m = pattern.matcher(str)
        return m.find()
    }

    /**
     * 判断是否只含英文数字
     *
     * @param str
     * @return
     */
    fun isLetterDigit(str: String): Boolean {
        val regex = "^[a-z0-9A-Z\\-_]+$"
        return str.matches(Regex(regex))
    }

    fun isWebUrl(str: String): Boolean {
        if (isEmpty(str) || str.contains(" ")) {
            return false
        }
        val url = str.toLowerCase()
        return (url.startsWith("http") || url.startsWith("file://") || url.startsWith("ftp")
                || url.startsWith("rtmp://") || url.startsWith("rtsp://"))
    }


    fun autoFixUrl(b: String?, u: String?): String? {
        if (isEmpty(b) || isEmpty(u)) {
            return u
        }
        var bUrl = b!!
        val url = u!!
        bUrl = bUrl.split(";").toTypedArray()[0]
        val baseUrl = getBaseUrl(bUrl)
        val lowUrl = url.toLowerCase()
        return if (lowUrl.startsWith("http") || lowUrl.startsWith("hiker") || lowUrl.startsWith("pics") || lowUrl.startsWith("code")) {
            url
        } else if (url.startsWith("//")) {
            "http:$url"
        } else if (url.startsWith("magnet") || url.startsWith("thunder") || url.startsWith("ftp") || url.startsWith("ed2k")) {
            url
        } else if (url.startsWith("/")) {
            if (baseUrl.endsWith("/")) {
                baseUrl.substring(0, baseUrl.length - 1) + url
            } else {
                baseUrl + url
            }
        } else if (url.startsWith("./")) {
            val protocolUrl = bUrl.split("://").toTypedArray()
            if (protocolUrl.isEmpty()) {
                return url
            }
            val c = protocolUrl[1].split("/").toTypedArray()
            if (c.size <= 1) {
                return if (baseUrl.endsWith("/")) {
                    baseUrl.substring(0, baseUrl.length - 1) + url.replace("./", "")
                } else {
                    baseUrl + url.replace("./", "")
                }
            }
            val sub = protocolUrl[1].replace(c[c.size - 1], "")
            protocolUrl[0] + "://" + sub + url.replace("./", "")
        } else if (url.startsWith("?")) {
            bUrl + url
        } else {
            url
        }
    }

    fun splitUrlByQuestionMark(url: String): Array<String?>? {
        return if (isEmpty(url)) {
            arrayOf(url)
        } else {
            val urls: Array<String?> = url.split("\\?").toTypedArray()
            if (urls.size <= 1) {
                urls
            } else {
                val res = arrayOfNulls<String>(2)
                res[0] = urls[0]
                res[1] = arrayToString(urls, 1, "?")
                res
            }
        }
    }
}
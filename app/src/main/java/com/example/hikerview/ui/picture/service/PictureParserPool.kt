package com.example.hikerview.ui.picture.service

/**
 * 作者：By 15968
 * 日期：On 2021/12/31
 * 时间：At 22:33
 */
object PictureParserPool {

    private val parsers = ArrayList<IPictureParser>()

    init {
        register(AdeskPictureParser)
        register(UnsplashPictureParser)
    }

    private fun register(parser: IPictureParser) {
        parsers.add(parser)
    }

    fun getByName(name: String?): IPictureParser {
        for (parser in parsers) {
            if (name == parser::class.simpleName) {
                return parser
            }
        }
        return AdeskPictureParser
    }

    fun getById(name: Int?): IPictureParser {
        for (parser in parsers) {
            if (name == parser.id()) {
                return parser
            }
        }
        return AdeskPictureParser
    }
}
package com.example.hikerview.lib.picture

import android.graphics.Bitmap
import android.graphics.Canvas
import com.bumptech.glide.load.EncodeStrategy
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceEncoder
import com.bumptech.glide.load.engine.Resource
import com.caverock.androidsvg.SVG
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 作者：By 15968
 * 日期：On 2022/2/27
 * 时间：At 11:36
 */

class SvgEncoder : ResourceEncoder<SVG> {
    override fun getEncodeStrategy(options: Options): EncodeStrategy {
        return EncodeStrategy.SOURCE
    }

    override fun encode(data: Resource<SVG>, file: File, options: Options): Boolean {
        try {
            val svg = data.get()
            val picture = svg.renderToPicture()
            val bitmap =
                Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.ARGB_8888);
            val canvas = Canvas(bitmap)
            canvas.drawPicture(picture)
            ByteArrayOutputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it);
                it.flush()
                it.writeTo(FileOutputStream(file))
                bitmap.recycle()
            }
//            picture.writeToStream(FileOutputStream(file))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }
}
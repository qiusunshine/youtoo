package com.example.hikerview.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File

/**
 * 作者：By 15968
 * 日期：On 2022/1/19
 * 时间：At 14:24
 */
object UriTool {
    fun uriToFileName(uri: Uri, context: Context): String {
        return when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> File(uri.path!!).name
            ContentResolver.SCHEME_CONTENT -> {
                try {
                    val cursor = context.contentResolver.query(uri, null, null, null, null, null)
                    cursor?.let {
                        it.moveToFirst()
                        val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        cursor.close()
                        displayName
                    } ?: "${System.currentTimeMillis()}.${
                        MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(context.contentResolver.getType(uri))
                    }}"
                } catch (e: Exception) {
                    "${System.currentTimeMillis()}.${
                        MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(context.contentResolver.getType(uri))
                    }}"
                }

            }
            else -> "${System.currentTimeMillis()}.${
                MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(context.contentResolver.getType(uri))
            }}"
        }
    }
}
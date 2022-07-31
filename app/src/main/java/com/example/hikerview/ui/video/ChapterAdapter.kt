package com.example.hikerview.ui.video

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hikerview.R
import com.example.hikerview.ui.detail.DetailUIHelper
import com.example.hikerview.ui.view.util.TextViewUtils
import com.example.hikerview.utils.StringUtil

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */
class ChapterAdapter(
    private val context: Context,
    val list: List<VideoChapter>,
    private val clickListener: OnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnClickListener {
        fun click(view: View?, pos: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TextThreeHolder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.item_chapter, parent, false)
        )
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is TextThreeHolder) {
            var txt = if (StringUtil.isNotEmpty(list[position].memoryTitle)) {
                list[position].memoryTitle
            } else {
                list[position].title
            }
            txt = DetailUIHelper.getTitleText(txt)
            if (list[position].isUse) {
                txt = "‘‘$txt’’"
            }
            TextViewUtils.setSpanText(viewHolder.title, txt)
            viewHolder.resultBg.setOnClickListener { v: View? ->
                if (viewHolder.adapterPosition >= 0 && viewHolder.adapterPosition < list.size) {
                    clickListener.click(v, viewHolder.adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class TextThreeHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.item_chapter_jishu)
        var resultBg: View = itemView.findViewById(R.id.bg)
    }
}
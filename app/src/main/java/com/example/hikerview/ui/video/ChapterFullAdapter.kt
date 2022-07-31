package com.example.hikerview.ui.video

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hikerview.R
import com.example.hikerview.ui.view.CustomRecyclerViewAdapter

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */
class ChapterFullAdapter(
    context: Context?,
    var list: List<String?>?,
    onItemClickListener: OnItemClickListener?
) : CustomRecyclerViewAdapter(context, list, onItemClickListener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ArticleListRuleHolder(
            LayoutInflater.from(context).inflate(R.layout.item_rect_radius_trans, parent, false)
        )
    }

    init {
        reSetTextColor = false
    }
}
package com.example.hikerview.ui.miniprogram.logs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;

import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2017/9/10
 * 时间：At 17:26
 */

public class LogsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    public List<String> getList() {
        return list;
    }

    private List<String> list;

    public LogsAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TitleHolder(LayoutInflater.from(context).inflate(R.layout.item_log_txt, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        TitleHolder holder = (TitleHolder) viewHolder;
        String title = list.get(position);
        holder.title.setText(title);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class TitleHolder extends RecyclerView.ViewHolder {
        TextView title;

        TitleHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_ad_title);
        }
    }
}

package com.example.hikerview.ui.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.hikerview.R;

/**
 * 作者：By hdy
 * 日期：On 2018/7/21
 * 时间：At 9:10
 */
public class DownloadDialog extends AlertDialog {
    private NumberProgressBar numberProgressBar;
    private TextView title;

    public DownloadDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = View.inflate(getContext(), R.layout.view_download_dialog, null);
        setContentView(contentView);
        this.setCancelable(false);
        numberProgressBar = contentView.findViewById(R.id.number_progress_bar);
        title = contentView.findViewById(R.id.number_title);
    }

    public void setProgress(int progress, String msg) {
        numberProgressBar.setProgress(progress);
        title.setText(msg);
    }

}
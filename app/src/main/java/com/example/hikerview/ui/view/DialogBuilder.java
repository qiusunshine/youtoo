package com.example.hikerview.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.example.hikerview.R;
import com.example.hikerview.ui.view.ZLoadingDialog.ZLoadingDialog;
import com.example.hikerview.ui.view.ZLoadingDialog.Z_TYPE;
import com.example.hikerview.utils.StringUtil;

/**
 * 作者：By hdy
 * 日期：On 2018/2/24
 * 时间：At 10:29
 */

public class DialogBuilder {
    public static ZLoadingDialog createLoadingDialog(Context context, boolean cancelable) {
        return new ZLoadingDialog(context)
                .setLoadingBuilder(Z_TYPE.STAR_LOADING)
                .setLoadingColor(Color.parseColor("#ff0f9d58"))
                .setHintText("Loading...")
                .setHintTextColor(Color.GRAY)
                .setCancelable(cancelable);
    }

    public static AlertDialog createInputConfirm(Context context, String title, String content, OnConfirmListener confirmListener) {
        View view1 = LayoutInflater.from(context).inflate(R.layout.view_dialog_edit_3, null, false);
        final EditText titleE = view1.findViewById(R.id.title);
        if (StringUtil.isNotEmpty(content)) {
            titleE.setText(content);
        }
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view1)
                .setCancelable(true)
                .setPositiveButton("确定", (dialog, which) -> {
                    confirmListener.ok(titleE.getText().toString());
                    dialog.dismiss();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).create();
    }



    public static void showInputConfirm(Context context, String title, String content, OnConfirmListener confirmListener) {
        createInputConfirm(context, title, content, confirmListener).show();
    }

    public interface OnConfirmListener {
        void ok(String text);
    }
}

package com.example.hikerview.ui.view.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.hikerview.R;

/**
 * 作者：By hdy
 * 日期：On 2019/3/23
 * 时间：At 0:11
 */
public class KeyboardToolPop extends PopupWindow {

    public KeyboardToolPop(Context context, final OnClickListener onClickListener) {
        super(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.pop_soft_keyboard_top_tool, null);
        this.setContentView(view);

        setTouchable(true);
        setOutsideTouchable(false);
        setFocusable(false);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED); //解决遮盖输入法

        HorizontalScrollView scrollView = getContentView().findViewById(R.id.keyboard_top_view_scroll);
        LinearLayout linearLayout = scrollView.findViewById(R.id.ll_content);
        if(linearLayout!=null)
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            TextView tv = (TextView) linearLayout.getChildAt(i);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        onClickListener.click(((TextView) v).getText().toString());
                    }
                }
            });
        }
    }

    public interface OnClickListener {
        void click(String text);
    }

}

package com.example.hikerview.ui.browser;

/**
 * 作者：By 15968
 * 日期：On 2019/9/29
 * 时间：At 21:12
 */

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.example.hikerview.R;

/**
 * Created by Renny on 2017/12/8.
 */

public class BottomDialogFragment extends DialogFragment implements View.OnClickListener {

    private View.OnClickListener onClickListener;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_web_bottom_fragment, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.bf1).setOnClickListener(this);
        view.findViewById(R.id.bf2).setOnClickListener(this);
        view.findViewById(R.id.bf3).setOnClickListener(this);
        view.findViewById(R.id.bf4).setOnClickListener(this);
        view.findViewById(R.id.bf5).setOnClickListener(this);
        view.findViewById(R.id.bf6).setOnClickListener(this);
        view.findViewById(R.id.bf7).setOnClickListener(this);
        view.findViewById(R.id.bf8).setOnClickListener(this);
        view.findViewById(R.id.bf9).setOnClickListener(this);
        view.findViewById(R.id.bf10).setOnClickListener(this);
        view.findViewById(R.id.bf11).setOnClickListener(this);
        view.findViewById(R.id.bf12).setOnClickListener(this);
        LinearLayout overlyingView = view.findViewById(R.id.view_group);
        overlyingView.setOnClickListener(v -> dismiss());
//        overlyingView.foldLayout();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setWindowAnimations(R.style.animate_dialog);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setCancelable(true);
        }
    }

    @Override
    public void onClick(View v) {
        v.setSelected(!v.isSelected());
        dismiss();
        if (this.onClickListener != null) {
            this.onClickListener.onClick(v);
        }
    }

//    public void foldLayout() {
//        if (overlyingView != null) {
//            overlyingView.foldLayout();
//        }
//    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit();
            super.show(manager, tag);
        } catch (Exception e) {
            //同一实例使用不同的tag会异常,这里捕获一下
            e.printStackTrace();
        }
    }
}

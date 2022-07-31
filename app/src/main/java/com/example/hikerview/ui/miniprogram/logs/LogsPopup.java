package com.example.hikerview.ui.miniprogram.logs;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.hikerview.R;
import com.example.hikerview.service.parser.JSEngine;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.KeyboardUtils;
import com.lxj.xpopup.util.XPopupUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class LogsPopup extends BottomPopupView {

    private List<String> data = new ArrayList<>();
    private LogsAdapter adapter;
    private EditText editText;
    private TextView textView;

    public LogsPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_logs;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        data.addAll(JSEngine.getInstance().getLogs());
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new LogsAdapter(getContext(), data);
        recyclerView.setAdapter(adapter);
        textView.setOnClickListener(view -> {
            textView.setVisibility(GONE);
            editText.setVisibility(VISIBLE);
            editText.requestFocus();
            KeyboardUtils.showSoftInput(editText);
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                data.clear();
                String key = s == null ? null : s.toString().toLowerCase();
                data.addAll(Stream.of(JSEngine.getInstance().getLogs())
                        .filter(log -> StringUtil.isEmpty(key) || log.toLowerCase().contains(key))
                        .collect(Collectors.toList()));
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.clear).setOnClickListener(v -> {
            new XPopup.Builder(getContext())
                    .asConfirm("温馨提示", "是否清空日志？", () -> {
                        JSEngine.getInstance().getLogs().clear();
                        data.clear();
                        adapter.notifyDataSetChanged();
                        ToastMgr.shortBottomCenter(getContext(), "日志已清空");
                    }).show();
        });
    }


    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .75f);
    }

    @Override
    protected void doAfterDismiss() {
        super.doAfterDismiss();
        KeyboardUtils.hideSoftInput(editText);
    }
}

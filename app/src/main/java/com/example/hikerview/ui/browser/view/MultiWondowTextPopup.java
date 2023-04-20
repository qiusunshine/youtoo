package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.example.hikerview.ui.view.HorizontalWebView;
import com.example.hikerview.ui.view.MutiWondowAdapter;
import com.example.hikerview.utils.ScreenUtil;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签
 * {@link com.example.hikerview.ui.view.MutiWondowPopup }
 */
public class MultiWondowTextPopup extends BottomPopupView {
    private static final String TAG = "MutiWondowPopup";
    private Runnable addRunnable, clearRunnable, clearOtherRunnable;
    private MutiWondowAdapter.OnClickListener clickListener;
    private List<HorizontalWebView> webViewList;
    private MultiWondowTextAdapter adapter;
    private Activity activity;

    public MultiWondowTextPopup with(Activity activity, List<HorizontalWebView> webViewList, Runnable addRunnable, Runnable clearRunnable, MutiWondowAdapter.OnClickListener clickListener) {
        this.activity = activity;
        this.webViewList = webViewList;
        this.addRunnable = addRunnable;
        this.clearRunnable = clearRunnable;
        this.clickListener = clickListener;
        return this;
    }

    public MultiWondowTextPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_multi_window_text;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        SwipeRecyclerView recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.add).setOnClickListener(v -> {
            addRunnable.run();
            dismiss();
        });
        findViewById(R.id.back).setOnClickListener(v -> {
            dismiss();
        });
        findViewById(R.id.clear).setOnClickListener(v -> {
            clearRunnable.run();
            dismiss();
        });

        View window_bg = findViewById(R.id.window_bg);
        window_bg.setOnClickListener(v -> dismiss());
        if (ScreenUtil.isOrientationLand(getContext())) {
            ViewGroup.LayoutParams layoutParams = window_bg.getLayoutParams();
            layoutParams.width = ScreenUtil.getScreenWidth3(getContext()) / 2;
            window_bg.setLayoutParams(layoutParams);
        }
        findViewById(R.id.clear).setOnLongClickListener(v -> {
            if (clearOtherRunnable != null) {
                new XPopup.Builder(getContext())
                        .atView(v)
                        .asAttachList(new String[]{"清除其它窗口", "清除全部窗口"}, null, (position, text) -> {
                            if ("清除其它窗口".equals(text)) {
                                dismissWith(clearOtherRunnable);
                            } else {
                                dismissWith(clearRunnable);
                            }
                        }).show();
            }
            return true;
        });
        recyclerView.setItemViewSwipeEnabled(true);
        recyclerView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(RecyclerView.ViewHolder srcHolder, RecyclerView.ViewHolder targetHolder) {
                return false;
            }

            @Override
            public void onItemDismiss(RecyclerView.ViewHolder srcHolder) {
                try {
                    int position = srcHolder.getAdapterPosition();
                    if (adapter.getBitmaps().size() > position) {
                        adapter.getBitmaps().remove(position);
                    }
                    if (webViewList.size() == 1) {
                        adapter.getBitmaps().clear();
                        clickListener.remove(0);
                        dismiss();
                        return;
                    }
                    adapter.getList().remove(position);
                    clickListener.remove(position - 1);
                    adapter.notifyItemRemoved(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        int span = 1;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), span);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? span : 1;
            }
        });
//        gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        List<HorizontalWebView> list = new ArrayList<>(webViewList);
        list.add(0, null);
        adapter = new MultiWondowTextAdapter(activity, list, new MultiWondowTextAdapter.OnClickListener() {
            @Override
            public void click(View view, int pos) {
                clickListener.click(view, pos - 1);
                dismiss();
            }

            @Override
            public void remove(int pos) {
                try {
                    if (adapter.getBitmaps().size() > pos) {
                        adapter.getBitmaps().remove(pos);
                    }
                    if (webViewList.size() == 1) {
                        adapter.getBitmaps().clear();
                        clickListener.remove(0);
                        dismiss();
                        return;
                    }
                    adapter.getList().remove(pos);
                    clickListener.remove(pos - 1);
                    adapter.notifyItemRemoved(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView.setAdapter(adapter);
        for (int i = 0; i < webViewList.size(); i++) {
            if (webViewList.get(i).isUsed()) {
                if (i < 2) {
                    break;
                }
                int dest = i + 1;
                gridLayoutManager.scrollToPosition(dest);
                break;
            }
        }
    }

    public MultiWondowTextPopup withClearOtherRunnable(Runnable clearOtherRunnable) {
        this.clearOtherRunnable = clearOtherRunnable;
        return this;
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * .85f);
    }
}

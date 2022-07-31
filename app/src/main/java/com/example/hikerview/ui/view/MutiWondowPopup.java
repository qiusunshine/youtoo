package com.example.hikerview.ui.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hikerview.R;
import com.lxj.xpopup.animator.PopupAnimator;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.impl.FullScreenPopupView;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;
import com.yanzhenjie.recyclerview.touch.OnItemMoveListener;

import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class MutiWondowPopup extends FullScreenPopupView {
    private static final String TAG = "MutiWondowPopup";
    private Runnable addRunnable, clearRunnable;
    private MutiWondowAdapter.OnClickListener clickListener;
    private List<HorizontalWebView> webViewList;
    private MutiWondowAdapter adapter;
    private MutiWondowThumbnailAdapter thumbnailAdapter;
    private Activity activity;
    private View homeView;

    public MutiWondowPopup with(Activity activity, List<HorizontalWebView> webViewList, Runnable addRunnable, Runnable clearRunnable, MutiWondowAdapter.OnClickListener clickListener) {
        this.activity = activity;
        this.webViewList = webViewList;
        this.addRunnable = addRunnable;
        this.clearRunnable = clearRunnable;
        this.clickListener = clickListener;
        return this;
    }

    public MutiWondowPopup home(View view){
        homeView = view;
        return this;
    }

    public MutiWondowPopup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected PopupAnimator getPopupAnimator() {
        // 移除默认的动画器
        return new MyScaleAlphaAnimator(getPopupContentView(), getAnimationDuration(), PopupAnimation.TranslateFromBottom);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_muti_window;
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
                        clickListener.remove(position);
                        dismiss();
                        return;
                    }
                    clickListener.remove(position);
                    adapter.notifyItemRemoved(position);
                    thumbnailAdapter.notifyItemRemoved(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        CenterLayoutManager gridLayoutManager = new CenterLayoutManager(getContext());
        gridLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MutiWondowAdapter(activity, webViewList, new MutiWondowAdapter.OnClickListener() {
            @Override
            public void click(View view, int pos) {
                clickListener.click(view, pos);
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
                        clickListener.remove(pos);
                        dismiss();
                        return;
                    }
                    clickListener.remove(pos);
                    adapter.notifyItemRemoved(pos);
                    thumbnailAdapter.notifyItemRemoved(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, this.homeView);
        recyclerView.setAdapter(adapter);

        RecyclerView recyclerView2 = findViewById(R.id.recyclerView2);

        thumbnailAdapter = new MutiWondowThumbnailAdapter(getContext(), webViewList, new MutiWondowThumbnailAdapter.OnClickListener() {
            @Override
            public void click(View view, int pos) {
                clickListener.click(view, pos);
                dismiss();
            }

            @Override
            public void remove(int pos) {

            }
        }, adapter.getBitmaps());
        CenterLayoutManager gridLayoutManager2 = new CenterLayoutManager(getContext());
        gridLayoutManager2.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView2.setLayoutManager(gridLayoutManager2);
        recyclerView2.setAdapter(thumbnailAdapter);

        for (int i = 0; i < webViewList.size(); i++) {
            if (webViewList.get(i).isUsed()) {
                gridLayoutManager.scrollToPosition(i);
                break;
            }
        }
    }
}

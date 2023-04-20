package com.example.hikerview.ui.miniprogram;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.function.Consumer;
import com.example.hikerview.R;
import com.example.hikerview.ui.view.CustomRecyclerViewAdapter;
import com.example.hikerview.utils.DisplayUtil;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class MiniProgramListPopup extends BottomPopupView {
    private List<String> data;
    private ClickListener clickListener;
    private String title;
    private int span = 2;
    private float height;
    private boolean dismissAfterClick = true;
    private CustomRecyclerViewAdapter adapter;

    private boolean showIcon = false;
    private int iconDrawable;
    private OnClickListener iconClickListener;

    private boolean dragSort = false;

    private DragSwapConsumer dragSwapConsumer;

    public void setDragSort(boolean dragSort, Consumer<Boolean> consumer) {
        this.dragSort = dragSort;
        if (dragSort) {
            ImageView icon = findViewById(R.id.code);
            icon.setVisibility(VISIBLE);
            icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_save_file));
            icon.setOnClickListener(v -> {
                consumer.accept(true);
                setDragSort(false, null);
                ToastMgr.shortBottomCenter(getContext(), "已保存");
            });
        } else {
            ImageView icon = findViewById(R.id.code);
            icon.setVisibility(showIcon ? VISIBLE : GONE);
            if (showIcon) {
                icon.setImageDrawable(getResources().getDrawable(iconDrawable));
                icon.setOnClickListener(iconClickListener);
            }
        }
    }

    public void setDragSwapConsumer(DragSwapConsumer dragSwapConsumer) {
        this.dragSwapConsumer = dragSwapConsumer;
    }

    public void top(int position) {
        String t = data.remove(position);
        data.add(0, t);
        adapter.notifyDataSetChanged();
    }

    public MiniProgramListPopup with(String[] data, int span, ClickListener clickListener) {
        this.data = new ArrayList<>(Arrays.asList(data));
        this.clickListener = clickListener;
        this.span = span;
        return this;
    }

    public MiniProgramListPopup with(List<String> data, int span, ClickListener clickListener) {
        this.data = data;
        this.clickListener = clickListener;
        this.span = span;
        return this;
    }

    public MiniProgramListPopup withDismissAfterClick(boolean dismissAfterClick) {
        this.dismissAfterClick = dismissAfterClick;
        return this;
    }

    public MiniProgramListPopup withTitle(String title) {
        this.title = title;
        return this;
    }

    public MiniProgramListPopup withIcon(int iconDrawable, OnClickListener listener) {
        this.showIcon = true;
        this.iconDrawable = iconDrawable;
        this.iconClickListener = listener;
        return this;
    }

    public MiniProgramListPopup height(float height) {
        this.height = height;
        return this;
    }

    public MiniProgramListPopup dismissAfterClick(boolean dismissAfterClick) {
        this.dismissAfterClick = dismissAfterClick;
        return this;
    }

    public void updateData(List<String> data) {
        this.data.clear();
        this.data.addAll(data);
        adapter.notifyDataSetChanged();
    }


    public MiniProgramListPopup(@NonNull Context context) {
        super(context);
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.pop_xiu_tan_result;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        if (StringUtil.isNotEmpty(title)) {
            TextView titleView = findViewById(R.id.title);
            titleView.setText(title);
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), span);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new CustomRecyclerViewAdapter(getContext(), data, new CustomRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (dismissAfterClick) {
                    dismissWith(() -> clickListener.click(data.get(position), position));
                } else {
                    clickListener.click(data.get(position), position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (dragSort) {
                    return;
                }
                if (dismissAfterClick) {
                    dismissWith(() -> clickListener.onLongClick(data.get(position), position));
                } else {
                    clickListener.onLongClick(data.get(position), position);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        if (showIcon) {
            ImageView icon = findViewById(R.id.code);
            icon.setVisibility(VISIBLE);
            icon.setImageDrawable(getResources().getDrawable(iconDrawable));
            icon.setOnClickListener(iconClickListener);
            int dp6 = DisplayUtil.dpToPx(getContext(), 6);
            icon.setPadding(dp6, dp6, dp6, dp6);
        }
        if (dragSwapConsumer != null) {
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            touchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    protected int getMaxHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * (height > 0 ? height : .85f));
    }

    @Override
    protected int getPopupHeight() {
        return (int) (XPopupUtils.getScreenHeight(getContext()) * (height > 0 ? height : .75f));
    }

    public void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }

    public interface ClickListener {
        void click(String url, int position);

        void onLongClick(String url, int position);
    }


    public interface DragSwapConsumer {
        void swap(int i, int j);
    }

    private ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlag = 0;
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            return makeMovementFlags(dragFlag, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    if (dragSwapConsumer != null) {
                        dragSwapConsumer.swap(i, i + 1);
                    }
                    Collections.swap(data, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    if (dragSwapConsumer != null) {
                        dragSwapConsumer.swap(i, i - 1);
                    }
                    Collections.swap(data, i, i - 1);
                }
            }
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //侧滑删除可以使用；
        }

        @Override
        public boolean isLongPressDragEnabled() {
            if (getContext() instanceof Activity && ((Activity)getContext()).isFinishing()) {
                return false;
            }
            return dragSort;
        }

        /**
         * 长按选中Item的时候开始调用
         * 长按高亮
         * @param viewHolder
         * @param actionState
         */
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//                viewHolder.itemView.setBackgroundColor(getContext().getResources().getColor(R.color.gray_rice));
                //获取系统震动服务//震动70毫秒
                try {
                    if (getContext() instanceof Activity && ((Activity)getContext()).isFinishing()) {
                        return;
                    }
                    Vibrator vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
                    if (vib != null) {
                        vib.vibrate(70);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        /**
         * 手指松开的时候还原高亮
         * @param recyclerView
         * @param viewHolder
         */
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
//            viewHolder.itemView.setBackgroundColor(0);
            adapter.notifyDataSetChanged();
        }
    });
}

package com.example.hikerview.ui.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：By hdy
 * 日期：On 2019/6/15
 * 时间：At 20:58
 */
public abstract class BaseDividerItem extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();
        final GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        final int childPosition = parent.getChildAdapterPosition(view);
        final int spanCount = layoutManager.getSpanCount();
        int type = layoutManager.getItemViewType(view);
        boolean noDivider = noBorder(type);
        int leftRight = getLeftRight(type);
        if (noDivider) {
            int pos = getPolyPos(type, parent.getChildAdapterPosition(view));
            // 计算这个child 处于第几列
            int column = (pos) % lp.getSpanSize();
            outRect.left = (column * leftRight / lp.getSpanSize());
            outRect.right = leftRight - (column + 1) * leftRight / lp.getSpanSize();
            return;
        }
        if (layoutManager.getOrientation() == RecyclerView.VERTICAL) {
//                //判断是否在第一排
//                if (layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount) == 0) {
//                    //第一排的需要上面
//                    outRect.top = topBottom;
//                }
//                outRect.bottom = topBottom;
            //这里忽略和合并项的问题，只考虑占满和单一的问题
            if (lp.getSpanSize() == spanCount) {
                //占满
                outRect.left = leftRight;
                outRect.right = leftRight;
            } else {
                outRect.left = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * leftRight);
                outRect.right = (int) (((float) leftRight * (spanCount + 1) / spanCount) - outRect.left);
            }
        } else {
            if (layoutManager.getSpanSizeLookup().getSpanGroupIndex(childPosition, spanCount) == 0) {
                //第一排的需要left
                outRect.left = leftRight;
            }
            outRect.right = leftRight;
            //这里忽略和合并项的问题，只考虑占满和单一的问题
//                if (lp.getSpanSize() == spanCount) {
//                    //占满
//                    outRect.top = topBottom;
//                    outRect.bottom = topBottom;
//                } else {
//                    outRect.top = (int) (((float) (spanCount - lp.getSpanIndex())) / spanCount * topBottom);
//                    outRect.bottom = (int) (((float) topBottom * (spanCount + 1) / spanCount) - outRect.top);
//                }
        }
    }

    /**
     * 网格左右两边间距和列之间的间距控制
     *
     * @param itemViewType itemViewType
     * @return 间距
     */
    public abstract int getLeftRight(int itemViewType);

    protected boolean noBorder(int itemViewType) {
        return false;
    }

    protected int getPolyPos(int itemViewType, int pos) {
        return pos;
    }
}

package com.example.hikerview.ui.view;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 作者：By 15968
 * 日期：On 2019/11/17
 * 时间：At 9:25
 */
public class MySnapHelper extends LinearSnapHelper {

    // 左对齐
    public static final int TYPE_SNAP_START = 2;

    // 右对齐
    public static final int TYPE_SNAP_END = 3;

    // default
    private int type = TYPE_SNAP_START;

    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;

    public MySnapHelper(int type) {
        this.type = type;
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        if (type == TYPE_SNAP_START) {
            return calculateDisOnStart(layoutManager, targetView);
        } else if (type == TYPE_SNAP_END) {
            return calculateDisOnEnd(layoutManager, targetView);
        } else {
            return super.calculateDistanceToFinalSnap(layoutManager, targetView);
        }
    }

    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (type == TYPE_SNAP_START) {
            return findStartSnapView(layoutManager);
        } else if (type == TYPE_SNAP_END) {
            return findEndSnapView(layoutManager);
        } else {
            return super.findSnapView(layoutManager);
        }
    }

    /**
     * TYPE_SNAP_START
     *
     * @param layoutManager
     * @param targetView
     * @return
     */
    private int[] calculateDisOnStart(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(layoutManager, targetView,
                    getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(layoutManager, targetView,
                    getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        return out;
    }


    /**
     * TYPE_SNAP_END
     *
     * @param layoutManager
     * @param targetView
     * @return
     */
    private int[] calculateDisOnEnd(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToEnd(layoutManager, targetView,
                    getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToEnd(layoutManager, targetView,
                    getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        return out;
    }

    /**
     * calculate distance to start
     *
     * @param layoutManager
     * @param targetView
     * @param helper
     * @return
     */
    private int distanceToStart(@NonNull RecyclerView.LayoutManager layoutManager,
                                @NonNull View targetView, OrientationHelper helper) {
        return helper.getDecoratedStart(targetView) - helper.getStartAfterPadding();
    }


    /**
     * calculate distance to end
     *
     * @param layoutManager
     * @param targetView
     * @param helper
     * @return
     */
    private int distanceToEnd(@NonNull RecyclerView.LayoutManager layoutManager,
                              @NonNull View targetView, OrientationHelper helper) {
        return helper.getDecoratedEnd(targetView) - helper.getEndAfterPadding();
    }

    /**
     * find the start view
     *
     * @param layoutManager
     * @return
     */
    private View findStartSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findStartView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findStartView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }


    /**
     * 注意判断最后一个item时，应通过判断距离右侧的位置
     *
     * @param layoutManager
     * @param helper
     * @return
     */
    private View findStartView(RecyclerView.LayoutManager layoutManager, OrientationHelper helper) {
        if (!(layoutManager instanceof LinearLayoutManager)) { // only for LinearLayoutManager
            return null;
        }
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        final int start = helper.getStartAfterPadding();

        int absClosest = Integer.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childStart = helper.getDecoratedStart(child);
            int absDistance = Math.abs(childStart - start);

            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }

        View firstVisibleChild = layoutManager.getChildAt(0);

        if (firstVisibleChild != closestChild) {
            return closestChild;
        }

        int firstChildStart = helper.getDecoratedStart(firstVisibleChild);

        int lastChildPos = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        View lastChild = layoutManager.getChildAt(childCount - 1);
        int lastChildCenter = helper.getDecoratedStart(lastChild) + (helper.getDecoratedMeasurement(lastChild) / 2);
        boolean isEndItem = lastChildPos == layoutManager.getItemCount() - 1;
        if (isEndItem && firstChildStart < 0 && lastChildCenter < helper.getEnd()) {
            return lastChild;
        }

        return closestChild;
    }

    /**
     * find the end view
     *
     * @param layoutManager
     * @return
     */
    private View findEndSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findEndView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findEndView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    private View findEndView(RecyclerView.LayoutManager layoutManager, OrientationHelper helper) {
        if (!(layoutManager instanceof LinearLayoutManager)) { // only for LinearLayoutManager
            return null;
        }
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        if (((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition() == 0) {
            return null;
        }

        View closestChild = null;
        final int end = helper.getEndAfterPadding();

        int absClosest = Integer.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childStart = helper.getDecoratedEnd(child);
            int absDistance = Math.abs(childStart - end);

            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }

        View lastVisibleChild = layoutManager.getChildAt(childCount - 1);

        if (lastVisibleChild != closestChild) {
            return closestChild;
        }

        if (layoutManager.getPosition(closestChild) == ((LinearLayoutManager) layoutManager).findLastCompletelyVisibleItemPosition()) {
            return closestChild;
        }

        View firstChild = layoutManager.getChildAt(0);
        int firstChildStart = helper.getDecoratedStart(firstChild);

        int firstChildPos = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        boolean isFirstItem = firstChildPos == 0;


        int firstChildCenter = helper.getDecoratedStart(firstChild) + (helper.getDecoratedMeasurement(firstChild) / 2);
        if (isFirstItem && firstChildStart < 0 && firstChildCenter > helper.getStartAfterPadding()) {
            return firstChild;
        }

        return closestChild;
    }


    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }
}
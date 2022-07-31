package com.example.hikerview.ui.browser.view;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;

import com.example.hikerview.R;

import java.util.ArrayList;
import java.util.Collections;

public class DragGridView extends ViewGroup implements
        View.OnTouchListener, View.OnClickListener, View.OnLongClickListener {
    private AttributeSet attributeSet;
    private int draggedIndex = -1, lastX = -1, lastY = -1, lastTargetIndex = -1;
    private int xPadding, yPadding;//the x-axis and y-axis padding of the item
    private int itemWidth, itemHeight, colCount;
    private ArrayList<Integer> newPositions = new ArrayList<Integer>();
    private static int ANIM_DURATION = 150;

    private AdapterView.OnItemClickListener onItemClickListener;
    private OnRearrangeListener onRearrangeListener;
    private OnLongClickListener onLongClickListener;
    private OnClickListener onClickListener;
    private boolean stateTag;
    private long touchStart;
    private boolean draggable;

    public DragGridView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.attributeSet = attributeSet;
        init();
    }

    private void init() {
        initAttributes();
        initData();
        initEventListener();
    }

    private void initAttributes() {
        TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.DragGridView);
        try {
            itemWidth = (int) typedArray.getDimension(R.styleable.DragGridView_itemWidth, 0);
            itemHeight = (int) typedArray.getDimension(R.styleable.DragGridView_itemHeight, 0);
            colCount = typedArray.getInteger(R.styleable.DragGridView_colCount, 0);
            yPadding = (int) typedArray.getDimension(R.styleable.DragGridView_yPadding, 20);
        } finally {
            typedArray.recycle();
        }
    }

    private void initData() {
        setChildrenDrawingOrderEnabled(true);
    }

    private void initEventListener() {
        setOnTouchListener(this);
        super.setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        newPositions.add(-1);
    }

    @Override
    public void removeViewAt(int index) {
        super.removeViewAt(index);
        newPositions.remove(index);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        xPadding = ((r - l) - (itemWidth * colCount)) / (colCount + 1);
        for (int i = 0; i < getChildCount(); i++) {
            if (i != draggedIndex) {
                Point xy = getCoorFromIndex(i);
                getChildAt(i).layout(xy.x, xy.y, xy.x + itemWidth, xy.y + itemHeight);
            }
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        //将正在移动的item放在最后一个绘制 防止出现正在移动的item被遮住的问题
        int order = i;
        if (draggedIndex == -1) {
            order = i;
        } else if (i == childCount - 1) {
            order = draggedIndex;
        } else if (i >= draggedIndex) {
            order = i + 1;
        }
        return order >= childCount || order < 0 ? i : order;
    }

    /**
     * get index from coordinate
     *
     * @param x
     * @param y
     * @return
     */
    private int getIndexFromCoor(int x, int y) {
        int col = getColFromCoor(x);
        int row = getRowFromCoor(y);
        if (col == -1 || row == -1) {
            return -1;
        }
        int index = row * colCount + col;
        if (index >= getChildCount()) {
            return -1;
        }
        return index;
    }

    private int getColFromCoor(int coor) {
        coor -= xPadding;
        for (int i = 0; coor > 0; i++) {
            if (coor < itemWidth)
                return i;
            coor -= (itemWidth + xPadding);
        }
        return -1;
    }

    private int getRowFromCoor(int coor) {
        coor -= yPadding;
        for (int i = 0; coor > 0; i++) {
            if (coor < itemHeight)
                return i;
            coor -= (itemHeight + yPadding);
        }
        return -1;
    }

    /**
     * 判断当前移动到的位置 当当前位置在另一个item区域时交换
     *
     * @param x
     * @param y
     * @return
     */
    private int getTargetFromCoor(int x, int y) {
        if (getRowFromCoor(y) == -1) {
            //touch is between rows
            return -1;
        }
        int target = getIndexFromCoor(x, y);
        //将item移动到最后的item之后
        if (target == getChildCount()) {
            target = getChildCount() - 1;
        }
        return target;
    }

    private Point getCoorFromIndex(int index) {
        int col = index % colCount;
        int row = index / colCount;
        return new Point(xPadding + (itemWidth + xPadding) * col,
                yPadding + (itemHeight + yPadding) * row);
    }

    @Override
    public void onClick(View view) {
        if (onItemClickListener != null && getIndex() != -1) {
            onItemClickListener.onItemClick(null, getChildAt(getIndex()), getIndex(), getIndex() / colCount);
        } else {
            if (onClickListener != null) {
                onClickListener.onClick(view);
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int index = getIndex();
        if (index != -1) {
            //如果长按的位置在
            draggedIndex = index;
            animateActionDown();
            draggable = true;
            try {
                Vibrator vib = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
                if (vib != null) {
                    vib.vibrate(70);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (onLongClickListener != null) {
                onLongClickListener.onLongClick(getChildAt(index));
            }
        }
        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                touchStart = System.currentTimeMillis();
                draggable = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getX() - lastX;
                int deltaY = (int) event.getY() - lastY;
                if (draggedIndex != -1 && draggable) {
                    int x = (int) event.getX(), y = (int) event.getY();
                    View draggedView = getChildAt(draggedIndex);
                    int itemLeft = draggedView.getLeft(), itemTop = draggedView.getTop();
                    draggedView.layout(itemLeft + deltaX, itemTop + deltaY, itemLeft + deltaX + itemWidth, itemTop + deltaY + itemHeight);
                    //得到当前点击位置所在的item的index
                    int targetIndex = getTargetFromCoor(x, y);
                    if (lastTargetIndex != targetIndex && targetIndex != -1) {
                        animateGap(targetIndex);
                        lastTargetIndex = targetIndex;
                    }
                }
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (draggedIndex != -1) {
                    //如果存在item交换 则重新排列子view
                    if (lastTargetIndex != -1) {
                        reorderChildren();
                    }
                    animateActionUp();
                    lastTargetIndex = -1;
                    draggedIndex = -1;
                }
                touchStart = -1;
                draggable = false;
                break;
        }
        //如果存在拖动item 则消费掉该事件
        if (draggedIndex != -1) {
            return true;
        }
        return false;
    }

    /**
     * actionDown动画
     */
    private void animateActionDown() {
        View v = getChildAt(draggedIndex);
        AnimationSet animSet = new AnimationSet(true);
        AlphaAnimation alpha = new AlphaAnimation(1, .5f);
        alpha.setDuration(ANIM_DURATION);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animSet);
    }

    /**
     * actionUp动画
     */
    private void animateActionUp() {
        View v = getChildAt(draggedIndex);
        AlphaAnimation alpha = new AlphaAnimation(.5f, 1);
        alpha.setDuration(ANIM_DURATION);
        AnimationSet animSet = new AnimationSet(true);
        animSet.addAnimation(alpha);
        animSet.setFillEnabled(true);
        animSet.setFillAfter(true);
        v.clearAnimation();
        v.startAnimation(animSet);
    }

    /**
     * 拖动某个item时其他item的移动动画
     * animate the other item when the dragged item moving
     *
     * @param targetIndex
     */
    private void animateGap(int targetIndex) {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (i == draggedIndex)
                continue;
            int newPos = i;
            if (draggedIndex < targetIndex && i >= draggedIndex + 1 && i <= targetIndex)
                newPos--;
            else if (targetIndex < draggedIndex && i >= targetIndex && i < draggedIndex)
                newPos++;

            //animate
            int oldPos = i;
            if (newPositions.get(i) != -1)
                oldPos = newPositions.get(i);
            if (oldPos == newPos)
                continue;

            Point oldXY = getCoorFromIndex(oldPos);
            Point newXY = getCoorFromIndex(newPos);
            Point oldOffset = new Point(oldXY.x - v.getLeft(), oldXY.y - v.getTop());
            Point newOffset = new Point(newXY.x - v.getLeft(), newXY.y - v.getTop());

            TranslateAnimation translate = new TranslateAnimation(Animation.ABSOLUTE, oldOffset.x,
                    Animation.ABSOLUTE, newOffset.x,
                    Animation.ABSOLUTE, oldOffset.y,
                    Animation.ABSOLUTE, newOffset.y);
            translate.setDuration(ANIM_DURATION);
            translate.setFillEnabled(true);
            translate.setFillAfter(true);
            v.clearAnimation();
            v.startAnimation(translate);

            newPositions.set(i, newPos);
        }
    }

    @SuppressLint("WrongCall")
    private void reorderChildren() {
        try {
            //FIGURE OUT HOW TO REORDER CHILDREN WITHOUT REMOVING THEM ALL AND RECONSTRUCTING THE LIST!!!
            if (onRearrangeListener != null) {
                onRearrangeListener.onRearrange(draggedIndex, lastTargetIndex);
            }
            ArrayList<View> children = new ArrayList<View>();
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).clearAnimation();
                children.add(getChildAt(i));
            }
            removeAllViews();
            while (draggedIndex != lastTargetIndex)
                if (lastTargetIndex == children.size()) {
                    // dragged and dropped to the right of the last element
                    children.add(children.remove(draggedIndex));
                    draggedIndex = lastTargetIndex;
                } else if (draggedIndex < lastTargetIndex) {
                    // shift to the right
                    Collections.swap(children, draggedIndex, draggedIndex + 1);
                    draggedIndex++;
                } else if (draggedIndex > lastTargetIndex) {
                    // shift to the left
                    Collections.swap(children, draggedIndex, draggedIndex - 1);
                    draggedIndex--;
                }
            for (int i = 0; i < children.size(); i++) {
                newPositions.set(i, -1);
                addView(children.get(i));
            }
            onLayout(true, getLeft(), getTop(), getRight(), getBottom());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get the index of dragging item
     *
     * @return
     */
    private int getIndex() {
        return getIndexFromCoor(lastX, lastY);
    }

    public void setOnRearrangeListener(OnRearrangeListener onRearrangeListener) {
        this.onRearrangeListener = onRearrangeListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setMyLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setEmptyClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void performEmptyClick() {
        if (onClickListener != null) {
            onClickListener.onClick(this);
        }
    }

    public boolean isStateTag() {
        return stateTag;
    }

    public void setStateTag(boolean stateTag) {
        this.stateTag = stateTag;
    }

    public interface OnRearrangeListener {

        void onRearrange(int oldIndex, int newIndex);
    }
}


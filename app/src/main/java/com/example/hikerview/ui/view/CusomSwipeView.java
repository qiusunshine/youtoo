package com.example.hikerview.ui.view;

/**
 * 作者：By 15968
 * 日期：On 2020/4/9
 * 时间：At 20:06
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Scroller;

import androidx.recyclerview.widget.RecyclerView;

/**
 * http://blog.csdn.net/xufeifandj www.github.com/xufeifandj
 *
 * @ferris 459821731@qq.com
 *
 */
public class CusomSwipeView extends RecyclerView {
    private Orientation orientation = Orientation.HORIZONTAL;
    /**
     * 当前滑动的ListView　position
     */
    private int slidePosition;
    /**
     * 手指按下X的坐标
     */
    private int downY;
    /**
     * 手指按下Y的坐标
     */
    private int downX;
    /**
     * 屏幕宽度
     */
    private int screenWidth;
    /**
     * ListView的item
     */
    private View itemView;
    /**
     * 滑动类
     */
    private Scroller scroller;
    private static final int SNAP_VELOCITY = 600;
    /**
     * 速度追踪对象
     */
    private VelocityTracker velocityTracker;
    /**
     * 是否响应滑动，默认为不响应
     */
    private boolean isSlide = false;
    /**
     * 认为是用户滑动的最小距离
     */
    private int mTouchSlop;
    /**
     * 移除item后的回调接口
     */
    private RemoveListener mRemoveListener;
    /**
     * 用来指示item滑出屏幕的方向,向左或者向右,用一个枚举值来标记
     */
    private RemoveDirection removeDirection;

    // 滑动删除方向的枚举值
    public enum RemoveDirection {
        RIGHT, LEFT;
    }

    public CusomSwipeView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public CusomSwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public CusomSwipeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public void init(Context context) {

        if (orientation == Orientation.VERTICAL) {
            screenWidth = ((WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getWidth();
        } else {
            screenWidth = ((WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getHeight();
        }
        scroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    /**
     * 设置滑动删除的回调接口
     *
     * @param removeListener
     */
    public void setRemoveListener(RemoveListener removeListener) {
        this.mRemoveListener = removeListener;
    }

    /**
     * 分发事件，主要做的是判断点击的是那个item, 以及通过postDelayed来设置响应左右滑动事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                addVelocityTracker(event);

                // 假如scroller滚动还没有结束，我们直接返回
                if (!scroller.isFinished()) {
                    return super.dispatchTouchEvent(event);
                }
                downX = (int) event.getX();
                downY = (int) event.getY();

                itemView = findChildViewUnder(downX, downY);
                if (itemView == null) {
                    return super.dispatchTouchEvent(event);
                }

                slidePosition = getChildPosition(itemView);
                // 无效的position, 不做任何处理
                if (slidePosition == AdapterView.INVALID_POSITION) {
                    return super.dispatchTouchEvent(event);
                }

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (orientation == Orientation.VERTICAL) {// 如果
                    // 左右滑动的距离大于最大的滑动距离，并且没有上下滑动，就代表
                    // 右滑删除或者左滑删除
                    if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY
                            || (Math.abs(event.getX() - downX) > mTouchSlop && Math
                            .abs(event.getY() - downY) < mTouchSlop)) {
                        isSlide = true;

                    }
                } else {
                    // 如果 上下滑动的距离大于最大的滑动距离，并且没有左右滑动，就代表上滑删除或者下滑删除
                    if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY
                            || (Math.abs(event.getY() - downY) > mTouchSlop && Math
                            .abs(event.getX() - downX) < mTouchSlop)) {
                        isSlide = true;

                    }

                }
                break;
            }
            case MotionEvent.ACTION_UP:
                recycleVelocityTracker();
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * 往右滑动，getScrollX()返回的是左边缘的距离，就是以View左边缘为原点到开始滑动的距离，所以向右边滑动为负值
     */
    private void scrollRight() {
        if (orientation == Orientation.VERTICAL) {// 往右滑动
            removeDirection = RemoveDirection.RIGHT;
            final int delta = (screenWidth + itemView.getScrollX());
            // 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
            scroller.startScroll(itemView.getScrollX(), 0, -delta, 0,
                    Math.abs(delta));
            postInvalidate(); // 刷新itemView
        } else {// 往上滑动
            removeDirection = RemoveDirection.RIGHT;
            final int delta = (screenWidth + itemView.getScrollY());
            // 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
            scroller.startScroll(0, itemView.getScrollY(), -delta, 0,
                    Math.abs(delta));
            postInvalidate(); // 刷新itemView
        }
    }

    /**
     * 向左滑动，根据上面我们知道向左滑动为正值
     */
    private void scrollLeft() {
        if (orientation == Orientation.VERTICAL) {// 往左滑动
            removeDirection = RemoveDirection.LEFT;
            final int delta = (screenWidth - itemView.getScrollX());
            // 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
            scroller.startScroll(itemView.getScrollX(), 0, delta, 0,
                    Math.abs(delta));
            postInvalidate(); // 刷新itemView
        } else {
            removeDirection = RemoveDirection.LEFT;
            final int delta = (screenWidth - itemView.getScrollY());
            // 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
            scroller.startScroll(0, itemView.getScrollY(), delta, 0,
                    Math.abs(delta));
            postInvalidate(); // 刷新itemView
        }
    }

    /**
     * 根据手指滚动itemView的距离来判断是滚动到开始位置还是向左或者向右滚动
     */
    private void scrollByDistanceX() {
        // 如果向左滚动的距离大于屏幕的二分之一，就让其删除

        if (orientation == Orientation.VERTICAL) {
            if (itemView.getScrollX() >= screenWidth / 2) {
                scrollLeft();
            } else if (itemView.getScrollX() <= -screenWidth / 2) {
                scrollRight();
            } else {
                // 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动
                itemView.scrollTo(0, 0);
            }
        } else {
            if (itemView.getScrollY() >= screenWidth / 2) {
                scrollLeft();
            } else if (itemView.getScrollY() <= -screenWidth / 2) {
                scrollRight();
            } else {
                // 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动
                itemView.scrollTo(0, 0);
            }
        }

    }

    /**
     * 处理我们拖动ListView item的逻辑
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSlide && slidePosition != AdapterView.INVALID_POSITION
                && itemView != null) {
            requestDisallowInterceptTouchEvent(true);
            addVelocityTracker(ev);
            final int action = ev.getAction();
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:

                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent
                            .setAction(MotionEvent.ACTION_CANCEL
                                    | (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    onTouchEvent(cancelEvent);

                    if (orientation == Orientation.VERTICAL) {
                        int deltaX = downX - x;
                        downX = x;
                        // 手指拖动itemView滚动, deltaX大于0向左滚动，小于0向右滚
                        itemView.scrollBy(deltaX, 0);
                    } else {
                        int deltaY = downY - y;
                        downY = y;
                        itemView.scrollBy(0, deltaY);
                    }

                    return true; // 拖动的时候ListView不滚动
                case MotionEvent.ACTION_UP:
                    int velocityX = getScrollVelocity();
                    if (velocityX > SNAP_VELOCITY) {
                        scrollRight();
                    } else if (velocityX < -SNAP_VELOCITY) {
                        scrollLeft();
                    } else {
                        scrollByDistanceX();
                    }

                    recycleVelocityTracker();
                    // 手指离开的时候就不响应左右滚动
                    isSlide = false;
                    break;
            }
        }

        // 否则直接交给ListView来处理onTouchEvent事件
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        // 调用startScroll的时候scroller.computeScrollOffset()返回true，
        if (scroller.computeScrollOffset()) {
            // 让ListView item根据当前的滚动偏移量进行滚动
            itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());

            postInvalidate();

            // 滚动动画结束的时候调用回调接口
            if (scroller.isFinished()) {
                if (mRemoveListener == null) {
                    throw new NullPointerException(
                            "RemoveListener is null, we should called setRemoveListener()");
                }

                itemView.scrollTo(0, 0);
                mRemoveListener.removeItem(removeDirection, slidePosition);
            }
        }
    }

    /**
     * 添加用户的速度跟踪器
     *
     * @param event
     */
    private void addVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }

        velocityTracker.addMovement(event);
    }

    /**
     * 移除用户速度跟踪器
     */
    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    /**
     * 获取X方向的滑动速度,大于0向右滑动，反之向左
     *
     * @return
     */
    private int getScrollVelocity() {
        if (orientation == Orientation.VERTICAL) {
            velocityTracker.computeCurrentVelocity(1000);
            int velocity = (int) velocityTracker.getXVelocity();
            return velocity;
        }else{
            velocityTracker.computeCurrentVelocity(1000);
            int velocity = (int) velocityTracker.getYVelocity();
            return velocity;
        }

    }

    /**
     *
     * 当ListView item滑出屏幕，回调这个接口 我们需要在回调方法removeItem()中移除该Item,然后刷新ListView
     *
     *
     */
    public interface RemoveListener {
        public void removeItem(RemoveDirection direction, int position);
    }

    public static enum Orientation {
        HORIZONTAL(0), VERTICAL(1);

        private int value;

        private Orientation(int i) {
            value = i;
        }

        public int value() {
            return value;
        }

        public static Orientation valueOf(int i) {
            switch (i) {
                case 0:
                    return HORIZONTAL;
                case 1:
                    return VERTICAL;
                default:
                    throw new RuntimeException("[0->HORIZONTAL, 1->VERTICAL]");
            }
        }
    }
}

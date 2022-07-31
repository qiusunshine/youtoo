package com.example.hikerview.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

import com.example.hikerview.R;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.util.TypedValue.COMPLEX_UNIT_PX;
import static android.util.TypedValue.COMPLEX_UNIT_SP;

/**
 * 对 support Design 包中的TabLayout包装
 * 主要实现功能：更改indicator 的长度
 * Created by zhouwei on 2018/5/18.
 */

public class EnhanceTabLayout extends FrameLayout {
    private TabLayout mTabLayout;
    private List<String> mTabList = new ArrayList<>();
    private List<View> mCustomViewList;
    private int mSelectIndicatorColor;
    private int mSelectTextColor;
    private int tabRippleColor;
    private int mUnSelectTextColor;
    private int mIndicatorHeight;
    private int mIndicatorWidth;
    private int mTabMode;
    private int mTabTextSize;
    private int tabSelectedTextSize;
    private OnLongClickListener onLongClickListener;
    private float startX, startY;

    public EnhanceTabLayout(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public EnhanceTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EnhanceTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EnhanceTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                startX = ev.getX();
                startY = ev.getY();
                break;
//            case MotionEvent.ACTION_MOVE:
//                //来到新的坐标
//                float endX = ev.getX();
//                float endY = ev.getY();
//                //计算偏移量
//                float distanceX = endX - startX;
//                float distanceY = endY - startY;
//                //判断滑动方向
//                if (Math.abs(distanceX) > Math.abs(distanceY)) {
//                    //水平方向滑动
////                   当滑动到ViewPager的第0个页面，并且是从左到右滑动
//                    if (getTabLayout().getSelectedTabPosition() == 0 && distanceX > 0) {
//                        getParent().requestDisallowInterceptTouchEvent(false);
//                    }
////                   ，当滑动到ViewPager的最后一个页面，并且是从右到左滑动
//                    else if ((getTabLayout().getSelectedTabPosition() == (getTabLayout().getTabCount() - 1)) && distanceX < 0) {
//                        getParent().requestDisallowInterceptTouchEvent(true);
//                    }
////                    其他,中间部分
//                    else {
//                        getParent().requestDisallowInterceptTouchEvent(true);
//                    }
//                } else {
//                    //竖直方向滑动
//                    getParent().requestDisallowInterceptTouchEvent(true);
//                }
//                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void readAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EnhanceTabLayout);
        mSelectIndicatorColor = typedArray.getColor(R.styleable.EnhanceTabLayout_tabIndicatorColor, context.getResources().getColor(R.color.colorAccent));
        mUnSelectTextColor = typedArray.getColor(R.styleable.EnhanceTabLayout_tabTextColor, Color.parseColor("#666666"));
        mSelectTextColor = typedArray.getColor(R.styleable.EnhanceTabLayout_tabSelectTextColor, context.getResources().getColor(R.color.colorAccent));
        tabRippleColor = typedArray.getColor(R.styleable.EnhanceTabLayout_tabRippleColor, context.getResources().getColor(R.color.grey_black));
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.EnhanceTabLayout_tabIndicatorHeight, 1);
        mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.EnhanceTabLayout_tabIndicatorWidth, 0);
        float sp15 = TypedValue.applyDimension(COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics());
        mTabTextSize = typedArray.getDimensionPixelSize(R.styleable.EnhanceTabLayout_tabTextSize, (int) sp15);
        tabSelectedTextSize = typedArray.getDimensionPixelSize(R.styleable.EnhanceTabLayout_tabSelectedTextSize, mTabTextSize);
        mTabMode = typedArray.getInt(R.styleable.EnhanceTabLayout_tab_Mode, 2);
        typedArray.recycle();
    }

    private void init(Context context, AttributeSet attrs) {
        readAttr(context, attrs);

        mTabList = new ArrayList<>();
        mCustomViewList = new ArrayList<>();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.enhance_tab_layout, this, true);
        mTabLayout = view.findViewById(R.id.enhance_tab_view);

        // 添加属性
        mTabLayout.setTabMode(mTabMode == 1 ? TabLayout.MODE_FIXED : TabLayout.MODE_SCROLLABLE);
        mTabLayout.setTabRippleColor(ColorStateList.valueOf(tabRippleColor));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // onTabItemSelected(tab.getPosition());
                // Tab 选中之后，改变各个Tab的状态
                for (int i = 0; i < mTabLayout.getTabCount(); i++) {
                    View view = mTabLayout.getTabAt(i).getCustomView();
                    if (view == null) {
                        return;
                    }
                    TextView text = view.findViewById(R.id.tab_item_text);
                    TextPaint tp = text.getPaint();
                    View indicator = view.findViewById(R.id.tab_item_indicator);
                    if (i == tab.getPosition()) { // 选中状态
                        text.setTextColor(mSelectTextColor);
                        indicator.setBackgroundColor(mSelectIndicatorColor);
                        indicator.setVisibility(View.VISIBLE);
                        tp.setFakeBoldText(true);
                    } else {// 未选中状态
                        text.setTextColor(mUnSelectTextColor);
                        indicator.setVisibility(View.INVISIBLE);
                        tp.setFakeBoldText(false);
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public List<View> getCustomViewList() {
        return mCustomViewList;
    }

    public void addOnTabSelectedListener(TabLayout.OnTabSelectedListener onTabSelectedListener) {
        mTabLayout.addOnTabSelectedListener(onTabSelectedListener);
    }

    /**
     * 与TabLayout 联动
     *
     * @param viewPager
     */
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        mTabLayout.addOnTabSelectedListener(new ViewPagerOnTabSelectedListener(viewPager, this));
    }


    /**
     * retrive TabLayout Instance
     *
     * @return
     */
    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    /**
     * 清空tab
     */
    public void clearAllTabs() {
        mTabLayout.removeAllTabs();
        mTabList.clear();
        mCustomViewList.clear();
    }

    /**
     * 添加tab
     *
     * @param tab
     */
    public void addTab(String tab) {
        mTabList.add(tab);
        View customView = getTabView(getContext(), tab, mIndicatorWidth, mIndicatorHeight, mTabTextSize);
        mCustomViewList.add(customView);
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(customView));
    }

    public OnLongClickListener getOnLongClickListener() {
        return onLongClickListener;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {

        private final ViewPager mViewPager;
        private final WeakReference<EnhanceTabLayout> mTabLayoutRef;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager, EnhanceTabLayout enhanceTabLayout) {
            mViewPager = viewPager;
            mTabLayoutRef = new WeakReference<>(enhanceTabLayout);
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mViewPager.setCurrentItem(tab.getPosition());
            EnhanceTabLayout mTabLayout = mTabLayoutRef.get();
            if (mTabLayout != null) {
                List<View> customViewList = mTabLayout.getCustomViewList();
                if (customViewList == null || customViewList.size() == 0) {
                    return;
                }
                for (int i = 0; i < customViewList.size(); i++) {
                    View view = customViewList.get(i);
                    if (view == null) {
                        return;
                    }
                    TextView text = view.findViewById(R.id.tab_item_text);
                    View indicator = view.findViewById(R.id.tab_item_indicator);
                    TextPaint tp = text.getPaint();
                    if (i == tab.getPosition()) { // 选中状态
                        text.setTextSize(COMPLEX_UNIT_PX, mTabLayout.tabSelectedTextSize);
                        text.setTextColor(mTabLayout.mSelectTextColor);
                        indicator.setBackgroundColor(mTabLayout.mSelectIndicatorColor);
                        indicator.setVisibility(View.VISIBLE);
                        tp.setFakeBoldText(true);
                    } else {// 未选中状态
                        text.setTextSize(COMPLEX_UNIT_PX, mTabLayout.mTabTextSize);
                        text.setTextColor(mTabLayout.mUnSelectTextColor);
                        indicator.setVisibility(View.INVISIBLE);
                        tp.setFakeBoldText(false);
                    }
                }
            }

        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            // No-op
        }
    }

    /**
     * 获取Tab 显示的内容
     *
     * @param context
     * @param
     * @return
     */
    private View getTabView(Context context, String text, int indicatorWidth, int indicatorHeight, int textSize) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_item_layout, null);
        TextView tabText = view.findViewById(R.id.tab_item_text);
        if (onLongClickListener != null) {
            tabText.setTag(text);
            tabText.setOnLongClickListener(onLongClickListener);
            tabText.setOnClickListener(v -> {
                for (int i = 0; i < mTabList.size(); i++) {
                    if (mTabList.get(i).equals(v.getTag())) {
                        mTabLayout.selectTab(mTabLayout.getTabAt(i));
                        return;
                    }
                }
            });
        }
        if (indicatorWidth > 0) {
            View indicator = view.findViewById(R.id.tab_item_indicator);
            ViewGroup.LayoutParams layoutParams = indicator.getLayoutParams();
            layoutParams.width = indicatorWidth;
            layoutParams.height = indicatorHeight;
            indicator.setLayoutParams(layoutParams);
        }
        tabText.setTextSize(COMPLEX_UNIT_PX, textSize);
        tabText.setText(text);
        return view;
    }
}

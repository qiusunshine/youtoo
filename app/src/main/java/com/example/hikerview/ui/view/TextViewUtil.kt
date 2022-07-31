package com.example.hikerview.ui.view

import android.text.Layout
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import kotlin.math.min

/**
 * Created by suanai on 2020/7/8.
 */
object TextViewUtil {

    private lateinit var mLayoutParams: ViewGroup.LayoutParams
    private var mLayout: Layout? = null


    private var mViewHeight_PX = 0

    private var mContentTotalHeight_PX = 0

    private var mMaxLines = 0
    private var mPaddingTop_PX = 0
    private var mPaddingBottom_PX = 0
    private var mMaxAdjustSpace_PX = 0

    fun isNeedAdjust(textView: TextView?): Boolean {
        if (textView == null) {
            return false
        }
        if (textView.maxLines <= 0 || textView.maxLines == Int.MAX_VALUE) {
            return false
        }

        mLayoutParams = textView.layoutParams
        mViewHeight_PX = mLayoutParams.height
        //不处理wrapContent和matchParent的情况
        if (mViewHeight_PX <= 0) {
            return false
        }

        mPaddingTop_PX = textView.paddingTop
        mPaddingBottom_PX = textView.paddingBottom

        mLayout = textView.layout
        if (mLayout == null) {
            return false
        }
        mMaxLines = textView.maxLines

        //如果文本行数小于最大行数，getLineTop不正确需要重新计算高度
        if (textView.lineCount < textView.maxLines) {
            mContentTotalHeight_PX = simulateContentHeight(mLayout!!, textView)
        } else {
            mContentTotalHeight_PX = mLayout!!.getLineTop(textView.maxLines)
            if (mContentTotalHeight_PX <= 0) {
                return false
            }
        }

//        logline(textView)


        if (mViewHeight_PX - mPaddingTop_PX - mPaddingBottom_PX < mContentTotalHeight_PX) {
            return true
        }

        return false
    }

    private fun logline(textView: TextView) {

        for (i in 0 until textView.maxLines){
            Log.d("MainActivity", "layout.getLineDescent(${i})${mLayout!!.getLineDescent(i)}" +
                    "---- mLayout!!.getLineAscent(0)${mLayout!!.getLineAscent(i)}")
        }
        for (i in 0 until textView.maxLines){
            Log.d("MainActivity", "mLayout!!.getLineBaseline($i)${mLayout!!.getLineBaseline(i)}")
        }
        for (i in 0 until textView.maxLines){
            Log.d(
                "MainActivity", "mLayout!!.getLineTop($i)${mLayout!!.getLineTop(i)}" +
                        "---- mLayout!!.getLineBottom($i)${mLayout!!.getLineBottom(i)}"
            )
        }
    }

    private fun simulateContentHeight(layout: Layout, textView: TextView): Int {
        val nowContentHeight = layout.getLineTop(textView.lineCount)
        return nowContentHeight + (textView.maxLines - textView.lineCount) * textView.lineHeight
    }

    //调整TexView的行间距，如果无法实现则返回false，如果没必要也返回false
    fun adjustTextViewLineSpace(textView: TextView?, lineSpaceBase: Int?): Boolean {

        if (isNeedAdjust(textView)) {
            //以最小Descent作为最大可调整行间距
            mMaxAdjustSpace_PX = getMinDescent(textView, mLayout)
            if (mMaxAdjustSpace_PX <= 0) {
                return false
            }

            //最窄内容
            val textContentMinHeight_PX =
                mContentTotalHeight_PX - mMaxAdjustSpace_PX * (textView!!.maxLines - 1)

            if (textContentMinHeight_PX > mViewHeight_PX - mPaddingBottom_PX - mPaddingTop_PX) {
                return false
            }

            val reduceSpace: Float
            //使内容放得下的最大行间距
            val remainLineSpace_PX =
                (mViewHeight_PX - mPaddingBottom_PX - mPaddingTop_PX - textContentMinHeight_PX) / (textView.maxLines - 1)
//            Log.d("MainActivity","mViewHeight_PX:"+mViewHeight_PX+"----textContentMinHeight_PX:"+textContentMinHeight_PX+"----textView.maxLines:"+textView.maxLines+"---remainLineSpace_PX:"+remainLineSpace_PX+"----mMaxAdjustSpace_PX:"+mMaxAdjustSpace_PX)
            if (remainLineSpace_PX <= 0) {
                return false
            }

            if (lineSpaceBase != null && lineSpaceBase >= 0 && remainLineSpace_PX >= lineSpaceBase) {
                reduceSpace = (lineSpaceBase - mMaxAdjustSpace_PX).toFloat()
            } else {
                reduceSpace = (remainLineSpace_PX - mMaxAdjustSpace_PX).toFloat()
            }
            Log.d("MainActivity", "reduceSpace:" + reduceSpace)
            textView.setLineSpacing(reduceSpace, 1.0f)

//            logline(textView)

            return true
        }

        return false
    }

    private fun getMinDescent(textView: TextView?, layout: Layout?): Int {
        var minDescent = 0
        if (layout == null || textView == null) {
            return minDescent
        }
        minDescent = layout.getLineDescent(0)
        for (i in 0 until textView.lineCount) {
            minDescent = min(minDescent, layout.getLineDescent(i))
        }
        return minDescent
    }
}
package com.example.hikerview.ui.view

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.example.hikerview.R
import com.example.hikerview.ui.view.TextViewUtil.adjustTextViewLineSpace
import com.example.hikerview.ui.view.TextViewUtil.isNeedAdjust

/**
 * Created by suannai on 2020/7/9.
 * 调整行高/行间距来自适应展示TextView的内容
 */

const val TEXTVIEW_CHANGE_LINES = 1
const val TEXTVIEW_CHANGE_LINEHEIGHT = 2
const val TEXTVIEW_CHANGE_LINES_AND_LINEHEIGHT = 3

class AdaptiveTextView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    AppCompatTextView(context, attrs) {

    private var mLineSpaceLowerLimit_PX = -1//行间距下限
    private var mMaxLineNumLowerLimit = 1//行数下限
    private var mOperateType = TEXTVIEW_CHANGE_LINES//执行的操作

    //forDiff
    private var mViewHeight_PX = 0
    private var mLayout: Layout? = null
    private var mMaxLines = 0
    private var mPaddingTop_PX = 0
    private var mPaddingBottom_PX = 0
    private var mTextFontHeight_PX = 0
    private var mContentTotalHeight = 0
    //forDiff

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.AdaptiveTextView)
        mOperateType = array.getInt(R.styleable.AdaptiveTextView_operateType, TEXTVIEW_CHANGE_LINES)
        mLineSpaceLowerLimit_PX =
            array.getDimension(R.styleable.AdaptiveTextView_lineSpaceLowerLimit, -1f).toInt()
//        Log.d("MainActivity","mLineSpaceLowerLimit_PX:$mLineSpaceLowerLimit_PX")
        mMaxLineNumLowerLimit = array.getInt(R.styleable.AdaptiveTextView_lineNumLowerLimit, 1)
        array.recycle()

    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            doAdjustTextView()
        }
    }

    private fun doAdjustTextView() {
        if (this.visibility != View.VISIBLE) {
            return
        }

        if (!isDiff(this)) {
            return
        }

        initAttr(this)

        when (mOperateType) {
            TEXTVIEW_CHANGE_LINES -> {
                if (this.maxLines <= mMaxLineNumLowerLimit) {
                    return
                }
                if (!isNeedAdjust(this)) {
                    return
                }
                this.maxLines--
                if (this.maxLines <= mMaxLineNumLowerLimit) {
                    return
                }
                doAdjustTextView()
            }
            TEXTVIEW_CHANGE_LINEHEIGHT -> {
                adjustTextViewLineSpace(this, mLineSpaceLowerLimit_PX)
            }
            TEXTVIEW_CHANGE_LINES_AND_LINEHEIGHT -> {
                if (!isNeedAdjust(this)){
                    return
                }
                var boolean = adjustTextViewLineSpace(this, mLineSpaceLowerLimit_PX)
//                Log.d("MainActivity","boolean:$boolean")
                if (boolean) {
                    //修改后初始化可以防止isDiff的错乱，使用post因为此时layout为null
                    this.post(Runnable { initAttr(this) })
                    return
                }
                if (this.maxLines <= mMaxLineNumLowerLimit) {
                    return
                }

                this.maxLines--
                if (isNeedAdjust(this)) {
                    doAdjustTextView()
                } else {
                    this.post { initAttr(this) }
                }
            }
        }
    }

    private fun initAttr(adaptiveTextView: AdaptiveTextView) {
        mViewHeight_PX = adaptiveTextView.measuredHeight
        mLayout = adaptiveTextView.layout
        mMaxLines = adaptiveTextView.maxLines
        mPaddingTop_PX = adaptiveTextView.paddingTop
        mPaddingBottom_PX = adaptiveTextView.paddingBottom
        mTextFontHeight_PX = adaptiveTextView.textSize.toInt()
        try {
            mContentTotalHeight = adaptiveTextView.layout?.getLineTop(adaptiveTextView.maxLines) ?: 0
        } catch (e: Exception) {
        }
    }

    //是否控件与高度相关的的属性被改变了
    private fun isDiff(adaptiveTextView: AdaptiveTextView): Boolean {
        if (mViewHeight_PX == 0 || mLayout == null || mMaxLines == 0
            || mTextFontHeight_PX == 0 || mContentTotalHeight == 0
        ) {
            return true
        }

        try {
            if (mViewHeight_PX == adaptiveTextView.measuredHeight && mLayout == adaptiveTextView.layout
                && mMaxLines == adaptiveTextView.maxLines && mPaddingTop_PX == adaptiveTextView.paddingTop
                && mPaddingBottom_PX == adaptiveTextView.paddingBottom
                && mContentTotalHeight == adaptiveTextView.layout?.getLineTop(adaptiveTextView.maxLines)
            ) {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }


}
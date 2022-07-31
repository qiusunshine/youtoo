package com.example.hikerview.ui.view;

import android.widget.EditText;

/**
 * 作者：By 15968
 * 日期：On 2019/11/2
 * 时间：At 23:33
 */
public class ZoomEditTextView extends ZoomView<EditText>
{
    /**最小字体*/
    public static final float MIN_TEXT_SIZE = 0.2f;

    /**最大子图*/
    public static final float MAX_TEXT_SIZE = 100.0f;

    /** 缩放比例 */
    float scale;

    /** 设置字体大小 */
    float textSize;

    public ZoomEditTextView(EditText view, float scale)
    {
        super(view);
        this.scale = scale;
        textSize = view.getTextSize();
    }

    /**
     * 放大
     */
    protected void zoomOut()
    {
        textSize += scale;
        if (textSize > MAX_TEXT_SIZE)
        {
            textSize = MAX_TEXT_SIZE;
        }
        view.setTextSize(textSize);
    }

    /**
     * 缩小
     */
    protected void zoomIn()
    {
        textSize -= scale;
        if (textSize < MIN_TEXT_SIZE)
        {
            textSize = MIN_TEXT_SIZE;
        }
        view.setTextSize(textSize);
    }

}

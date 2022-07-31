package com.example.hikerview.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.hikerview.R;
import com.example.hikerview.utils.DisplayUtil;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

/**
 * 作者：By 15968
 * 日期：On 2020/4/18
 * 时间：At 16:38
 */
public class DrawableTextView extends FrameLayout {

    private TextView textView;
    private ImageView imageView;

    public static final String TAG = "DrawableTextView";

    public DrawableTextView(Context context) {
        super(context);
        init(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
        Drawable src = array.getDrawable(R.styleable.DrawableTextView_drawableSrc);
        int drawableWidth = array.getDimensionPixelSize(R.styleable.DrawableTextView_drawableWidth, 0);
        int textTop = array.getDimensionPixelSize(R.styleable.DrawableTextView_textTop, DisplayUtil.dpToPx(getContext(), 10));
        int textSize = array.getDimensionPixelSize(R.styleable.DrawableTextView_textSize, 0);
        String text = array.getString(R.styleable.DrawableTextView_text);
        array.recycle();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_img_text_view, this, true);
        imageView = view.findViewById(R.id.item_reult_img);
        textView = view.findViewById(R.id.textView);
        RelativeLayout.LayoutParams textLay = (RelativeLayout.LayoutParams) textView.getLayoutParams();
        textLay.topMargin = textTop;
        textView.setLayoutParams(textLay);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.width = drawableWidth;
        layoutParams.height = drawableWidth;
        imageView.setLayoutParams(layoutParams);

        if (text != null) {
            textView.setText(text);
        }
        if (src != null) {
            imageView.setImageDrawable(src);
        }
        if(textSize > 0){
            textView.setTextSize(COMPLEX_UNIT_PX, textSize);
        }

//        setBackground(context.getResources().getDrawable(R.drawable.ripple_gray_setting));
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public TextView getTextView(){
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }
}

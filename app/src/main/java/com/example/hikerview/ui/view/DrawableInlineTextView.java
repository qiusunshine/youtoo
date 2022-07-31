package com.example.hikerview.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hikerview.R;

/**
 * 作者：By 15968
 * 日期：On 2020/4/18
 * 时间：At 16:38
 */
public class DrawableInlineTextView extends FrameLayout {

    private TextView textView;
    private ImageView imageView;

    public static final String TAG = "DrawableTextView";

    public DrawableInlineTextView(Context context) {
        super(context);
        init(context, null);
    }

    public DrawableInlineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public DrawableInlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
        Drawable src = array.getDrawable(R.styleable.DrawableTextView_drawableSrc);
        String text = array.getString(R.styleable.DrawableTextView_text);
        int center = array.getInteger(R.styleable.DrawableTextView_center, 0);
        array.recycle();
        int layout = center == 1 ? R.layout.view_img_text_view_inline : R.layout.view_img_text_view_inline2;
        View view = LayoutInflater.from(getContext()).inflate(layout, this, true);
        imageView = view.findViewById(R.id.item_reult_img);
        textView = view.findViewById(R.id.textView);

        if (text != null) {
            textView.setText(text);
        }
        if (src != null) {
            imageView.setImageDrawable(src);
        }

//        setBackground(context.getResources().getDrawable(R.drawable.ripple_gray_setting));
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setText(Spanned text) {
        textView.setText(text);
    }

    public ImageView getImageView() {
        return imageView;
    }
}

package com.tonicartos.widget.stickygridheaders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class HeaderFillerView extends FrameLayout {
    private int headerWidth;

    public HeaderFillerView(Context context) {
        super(context);
    }

    public HeaderFillerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("NewApi")
    public HeaderFillerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View v = (View) getTag();
        if (v.getMeasuredHeight() == 0) {
            v.measure(MeasureSpec.makeMeasureSpec(headerWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), v.getMeasuredHeight());
    }

    public void setHeaderWidth(int width) {
        this.headerWidth = width;
    }
}

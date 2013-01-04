package com.tonicartos.widget.stickygridheaders;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class HeaderFillerView extends FrameLayout {
    private StickyGridHeadersGridView stickyGridView;
    private int forcedWidth;

    public HeaderFillerView(Context context) {
        super(context);
    }

    public HeaderFillerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderFillerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View v = (View) getTag();
        v.measure(MeasureSpec.makeMeasureSpec(forcedWidth, MeasureSpec.AT_MOST), heightMeasureSpec);
        setMeasuredDimension(v.getWidth(), v.getHeight());
    }
    
    public void bindHeader(StickyGridHeadersGridView view) {
        stickyGridView = view;
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        stickyGridView.requestDraw(this);
    }

    public void forceWidth(int width) {
        this.forcedWidth = width;
    }
}

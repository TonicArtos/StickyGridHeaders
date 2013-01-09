/*
 Copyright 2013 Tonic Artos

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.tonicartos.widget.stickygridheaders;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * A view to hold the section header and measure the header row height correctly.
 * 
 * @author Tonic Artos
 */
public class HeaderFillerView extends FrameLayout {
    private int headerWidth;

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

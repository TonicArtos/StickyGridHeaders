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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * View to wrap adapter supplied views and ensure the row height is correctly
 * measured for the contents of all cells in the row.
 * <p>
 * Some mickymousing is required with the adapter wrapper.
 * <p>
 * Adopted and modified from code first detailed at <a
 * href="http://stackoverflow.com/a/13994344">http://stackoverflow.com/a/13994344</a>
 * 
 * @author Anton Spaans, Tonic Artos
 */
public class ReferenceView extends FrameLayout {
    private int numColumns;
    private int position;
    private View[] rowSiblings;
    private boolean forceMeasureDisabled;

    public ReferenceView(Context context) {
        super(context);
    }

    public ReferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public ReferenceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("NewApi")
    public void setRowSiblings(View[] rowSiblings) {
        this.rowSiblings = rowSiblings;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public Object getTag() {
        return getChildAt(0).getTag();
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void setTag(Object tag) {
        getChildAt(0).setTag(tag);
    }

    @Override
    public Object getTag(int key) {
        return getChildAt(0).getTag(key);
    }

    @Override
    public void setTag(int key, Object tag) {
        getChildAt(0).setTag(key, tag);
    }

    public View getView() {
        return getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (position % numColumns == 0) {
            forceRowMeasurement(widthMeasureSpec, heightMeasureSpec);
        }

        if (rowSiblings == null) {
            return;
        }

        int measuredHeight = getMeasuredHeight();
        int maxHeight = measuredHeight;
        for (View rowSibling : rowSiblings) {
            if (rowSibling != null) {
                maxHeight = Math.max(maxHeight, rowSibling.getMeasuredHeight());
            }
        }

        if (maxHeight == measuredHeight) {
            return;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Forces measurement of entire row.
     * 
     * Used to fix the case where the first item in a row has been measured
     * determining the row height for scrolling upwards before any row siblings
     * has been measured.
     * 
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    private void forceRowMeasurement(int widthMeasureSpec, int heightMeasureSpec) {
        if (forceMeasureDisabled) {
            return;
        }

        forceMeasureDisabled = true;
        for (View rowSibling : rowSiblings) {
            rowSibling.measure(widthMeasureSpec, heightMeasureSpec);
        }
        forceMeasureDisabled = false;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }
}
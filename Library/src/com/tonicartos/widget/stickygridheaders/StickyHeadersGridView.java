
package com.tonicartos.widget.stickygridheaders;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.GridView;

public class StickyHeadersGridView extends GridView {
    private static final String ERROR_PLATFORM = "Error supporting platform "
            + Build.VERSION.SDK_INT + ".";

    public StickyHeadersGridView(Context context) {
        this(context, null);
    }

    public StickyHeadersGridView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.gridViewStyle);
    }

    public StickyHeadersGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void layoutChildren() {
        // TODO: Override to provide new layout behaviour.
    }

    class RuntimePlatformSupportException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public RuntimePlatformSupportException(Exception e) {
            super(ERROR_PLATFORM, e);
        }
    }
}

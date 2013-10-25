
package com.tonicartos.widget.stickygridheaders;

import android.widget.ListAdapter;

public interface StickyHeadersListAdapter extends ListAdapter {
    /**
     * Get the id of the header associated with this position.
     * 
     * @param position Position to get header id for.
     * @return Associated header id for position.
     */
    long getHeaderId(int position);

    /**
     * Check whether this position is a header. This controls the display of
     * this item to span the row and stick to the top of the screen on
     * scrolling.
     * 
     * @param position Position to check.
     * @return True if position is a header item.
     */
    boolean isHeader(int position);
}

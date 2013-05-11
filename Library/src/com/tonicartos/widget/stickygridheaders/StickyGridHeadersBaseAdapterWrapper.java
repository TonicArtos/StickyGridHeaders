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
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter wrapper to insert extra views and otherwise hack around GridView to
 * add sections and headers.
 * 
 * @author Tonic Artos
 */
public class StickyGridHeadersBaseAdapterWrapper extends BaseAdapter {
    private static boolean sCurrentlySizingRow;
    private static final int sNumViewTypes = 2;
    protected static final int ID_FILLER = -0x02;
    protected static final int ID_HEADER = -0x01;
    protected static final int POSITION_FILLER = -0x01;
    protected static final int POSITION_HEADER = -0x02;
    protected static final int VIEW_TYPE_FILLER = 0x00;
    protected static final int VIEW_TYPE_HEADER = 0x01;

    private final StickyGridHeadersBaseAdapter mDelegate;

    private final Context mContext;
    private int mCount;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateCount();
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mHeaderCache.clear();
            notifyDataSetInvalidated();
        }
    };

    private StickyGridHeadersGridView mGridView;

    private final List<View> mHeaderCache = new ArrayList<View>();
    private int mNumColumns = 1;
    private View[] mRowSiblings;

    public StickyGridHeadersBaseAdapterWrapper(Context context,
            StickyGridHeadersGridView gridView,
            StickyGridHeadersBaseAdapter delegate) {
        mContext = context;
        mDelegate = delegate;
        mGridView = gridView;
        delegate.registerDataSetObserver(mDataSetObserver);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        mCount = 0;
        int numHeaders = mDelegate.getNumHeaders();
        if (numHeaders == 0) {
            return mDelegate.getCount();
        }

        for (int i = 0; i < numHeaders; i++) {
            // Pad count with space for header and trailing filler in header
            // group.
            mCount += mDelegate.getCountForHeader(i)
                    + unFilledSpacesInHeaderGroup(i) + mNumColumns;
        }
        return mCount;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     * <p>
     * Since this wrapper inserts fake entries to fill out items grouped by
     * header and also spaces to insert headers into some positions will return
     * null.
     * </p>
     * 
     * @param position Position of the item whose data we want within the
     *            adapter's data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) throws ArrayIndexOutOfBoundsException {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.mPosition == POSITION_FILLER
                || adapterPosition.mPosition == POSITION_HEADER) {
            // Fake entry in view.
            return null;
        }

        return mDelegate.getItem(adapterPosition.mPosition);
    }

    @Override
    public long getItemId(int position) {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.mPosition == POSITION_HEADER) {
            return ID_HEADER;
        }
        if (adapterPosition.mPosition == POSITION_FILLER) {
            return ID_FILLER;
        }
        return mDelegate.getItemId(adapterPosition.mPosition);
    }

    @Override
    public int getItemViewType(int position) {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.mPosition == POSITION_HEADER) {
            return VIEW_TYPE_HEADER;
        }
        if (adapterPosition.mPosition == POSITION_FILLER) {
            return VIEW_TYPE_FILLER;
        }
        int itemViewType = mDelegate.getItemViewType(adapterPosition.mPosition);
        if (itemViewType == IGNORE_ITEM_VIEW_TYPE) {
            return itemViewType;
        }
        return itemViewType + sNumViewTypes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReferenceView container = null;
        if (convertView instanceof ReferenceView) {
            // Unpack from reference view;
            container = (ReferenceView)convertView;
            convertView = container.getChildAt(0);
        }

        Position adapterPosition = translatePosition(position);
        if (adapterPosition.mPosition == POSITION_HEADER) {
            View v = getHeaderFillerView(adapterPosition.mHeader, convertView,
                    parent);
            ((HeaderFillerView)v).setHeaderId(adapterPosition.mHeader);
            convertView = (View)v.getTag();
            View header = mDelegate.getHeaderView(adapterPosition.mHeader,
                    convertView, parent);
            v.setTag(header);
            convertView = v;
        } else if (adapterPosition.mPosition == POSITION_FILLER) {
            convertView = getFillerView(convertView, parent);
        } else {
            convertView = mDelegate.getView(adapterPosition.mPosition,
                    convertView, parent);
        }

        // Wrap in reference view if not already.
        if (container == null) {
            container = new ReferenceView(mContext);
        }
        container.removeAllViews();
        container.addView(convertView);

        container.setPosition(position);
        container.setNumColumns(mNumColumns);

        mRowSiblings[position % mNumColumns] = container;
        if (position % mNumColumns == 0) {
            sCurrentlySizingRow = true;
            for (int i = 1; i < mRowSiblings.length; i++) {
                mRowSiblings[i] = getView(position + i, null, parent);
            }
            sCurrentlySizingRow = false;
        }

        container.setRowSiblings(mRowSiblings);
        if (!sCurrentlySizingRow
                && (position % mNumColumns == (mNumColumns - 1) || position == getCount() - 1)) {
            // End of row or items.
            initRowSiblings(mNumColumns);
        }
        return container;
    }

    /**
     * @return the adapter wrapped by this adapter.
     */
    public StickyGridHeadersBaseAdapter getWrappedAdapter() {
        return mDelegate;
    }

    @Override
    public int getViewTypeCount() {
        return mDelegate.getViewTypeCount() + sNumViewTypes;
    }

    @Override
    public boolean hasStableIds() {
        return mDelegate.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mDelegate.isEmpty();
    }

    @Override
    public boolean isEnabled(int position) {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.mPosition == POSITION_FILLER
                || adapterPosition.mPosition == POSITION_HEADER) {
            return false;
        }

        return mDelegate.isEnabled(adapterPosition.mPosition);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDelegate.registerDataSetObserver(observer);
    }

    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
        initRowSiblings(numColumns);
        // notifyDataSetChanged();
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDelegate.unregisterDataSetObserver(observer);
    }

    private FillerView getFillerView(View convertView, ViewGroup parent) {
        FillerView fillerView = (FillerView)convertView;
        if (fillerView == null) {
            fillerView = new FillerView(mContext);
        }

        return fillerView;
    }

    private View getHeaderFillerView(int headerPosition, View convertView,
            ViewGroup parent) {
        HeaderFillerView headerFillerView = (HeaderFillerView)convertView;
        headerFillerView = new HeaderFillerView(mContext);
        headerFillerView.setHeaderWidth(mGridView.getWidth());

        return headerFillerView;
    }

    private void initRowSiblings(int numColumns) {
        mRowSiblings = new View[numColumns];
        Arrays.fill(mRowSiblings, null);
    }

    /**
     * Counts the number of items that would be need to fill out the last row in
     * the group of items with the given header.
     * 
     * @param header Header set of items are grouped by.
     * @return The count of unfilled spaces in the last row.
     */
    private int unFilledSpacesInHeaderGroup(int header) {
        int remainder = mDelegate.getCountForHeader(header) % mNumColumns;
        return remainder == 0 ? 0 : mNumColumns - remainder;
    }

    protected long getHeaderId(int position) {
        return translatePosition(position).mHeader;
    }

    protected View getHeaderView(int position, View convertView,
            ViewGroup parent) {
        if (mDelegate.getNumHeaders() == 0) {
            return null;
        }

        return mDelegate.getHeaderView(translatePosition(position).mHeader,
                convertView, parent);
    }

    protected Position translatePosition(int position) {
        int numHeaders = mDelegate.getNumHeaders();
        if (numHeaders == 0) {
            if (position >= mDelegate.getCount()) {
                return new Position(POSITION_FILLER, 0);
            }
            return new Position(position, 0);
        }

        // Translate GridView position to Adapter position.
        int adapterPosition = position;
        int place = position;
        int i;

        for (i = 0; i < numHeaders; i++) {
            int sectionCount = mDelegate.getCountForHeader(i);

            // Skip past fake items making space for header in front of
            // sections.
            if (place == 0) {
                // Position is first column where header will be.
                return new Position(POSITION_HEADER, i);
            }
            place -= mNumColumns;
            if (place < 0) {
                // Position is a fake so return null.
                return new Position(POSITION_FILLER, i);
            }
            adapterPosition -= mNumColumns;

            if (place < sectionCount) {
                return new Position(adapterPosition, i);
            }

            // Skip past section end of section row filler;
            int filler = unFilledSpacesInHeaderGroup(i);
            adapterPosition -= filler;
            place -= sectionCount + filler;
        }

        // Position is a fake.
        return new Position(POSITION_FILLER, i);
    }

    protected void updateCount() {
        mCount = 0;
        int numHeaders = mDelegate.getNumHeaders();
        if (numHeaders == 0) {
            mCount = mDelegate.getCount();
            return;
        }

        for (int i = 0; i < numHeaders; i++) {
            mCount += mDelegate.getCountForHeader(i) + mNumColumns;
        }
    }

    /**
     * Simple view to fill space in grid view.
     * 
     * @author Tonic Artos
     */
    protected class FillerView extends View {
        public FillerView(Context context) {
            super(context);
        }

        public FillerView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public FillerView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
    }

    /**
     * A view to hold the section header and measure the header row height
     * correctly.
     * 
     * @author Tonic Artos
     */
    protected class HeaderFillerView extends FrameLayout {
        private int mHeaderId;
        private int mHeaderWidth;

        public HeaderFillerView(Context context) {
            super(context);
        }

        public HeaderFillerView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public HeaderFillerView(Context context, AttributeSet attrs,
                int defStyle) {
            super(context, attrs, defStyle);
        }
        
        public int getHeaderId() {
            return mHeaderId;
        }

        /**
         * Set the adapter id for this header so we can easily pull it later.
         */
        public void setHeaderId(int headerId) {
            mHeaderId = headerId;
        }

        public void setHeaderWidth(int width) {
            mHeaderWidth = width;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            View v = (View)getTag();
            android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
            if (params == null) {
                v.setLayoutParams(generateDefaultLayoutParams());
            }
            if (v.getVisibility() != View.GONE) {
                if (v.getMeasuredHeight() == 0) {
                    v.measure(MeasureSpec.makeMeasureSpec(mHeaderWidth,
                            MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
                            0, MeasureSpec.UNSPECIFIED));
                }
            }
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    v.getMeasuredHeight());
        }

        @Override
        protected LayoutParams generateDefaultLayoutParams() {
            return new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }
    }

    protected class HeaderHolder {
        protected View mHeaderView;
    }

    protected class Position {
        protected int mHeader;
        protected int mPosition;

        protected Position(int position, int header) {
            mPosition = position;
            mHeader = header;
        }
    }

    /**
     * View to wrap adapter supplied views and ensure the row height is
     * correctly measured for the contents of all cells in the row.
     * <p>
     * Some mickymousing is required with the adapter wrapper.
     * <p>
     * Adopted and modified from code first detailed at <a
     * href="http://stackoverflow.com/a/13994344"
     * >http://stackoverflow.com/a/13994344</a>
     * 
     * @author Anton Spaans, Tonic Artos
     */
    protected class ReferenceView extends FrameLayout {
        private boolean mForceMeasureDisabled;
        private int mNumColumns;
        private int mPosition;
        private View[] mRowSiblings;

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

        @Override
        public Object getTag() {
            return getChildAt(0).getTag();
        }

        @Override
        public Object getTag(int key) {
            return getChildAt(0).getTag(key);
        }

        public View getView() {
            return getChildAt(0);
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public void setPosition(int position) {
            mPosition = position;
        }

        @SuppressLint("NewApi")
        public void setRowSiblings(View[] rowSiblings) {
            mRowSiblings = rowSiblings;
        }

        @Override
        public void setTag(int key, Object tag) {
            getChildAt(0).setTag(key, tag);
        }

        @Override
        public void setTag(Object tag) {
            getChildAt(0).setTag(tag);
        }

        /**
         * Forces measurement of entire row. Used to fix the case where the
         * first item in a row has been measured determining the row height for
         * scrolling upwards before any row siblings has been measured.
         * 
         * @param widthMeasureSpec
         * @param heightMeasureSpec
         */
        private void forceRowMeasurement(int widthMeasureSpec,
                int heightMeasureSpec) {
            if (mForceMeasureDisabled) {
                return;
            }

            mForceMeasureDisabled = true;
            for (View rowSibling : mRowSiblings) {
                rowSibling.measure(widthMeasureSpec, heightMeasureSpec);
            }
            mForceMeasureDisabled = false;
        }

        @Override
        protected LayoutParams generateDefaultLayoutParams() {
            return new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (mNumColumns == 1
                    || StickyGridHeadersBaseAdapterWrapper.this.mRowSiblings == null) {
                return;
            }

            if (mPosition % mNumColumns == 0) {
                forceRowMeasurement(widthMeasureSpec, heightMeasureSpec);
            }

            int measuredHeight = getMeasuredHeight();
            int maxHeight = measuredHeight;
            for (View rowSibling : mRowSiblings) {
                if (rowSibling != null) {
                    maxHeight = Math.max(maxHeight,
                            rowSibling.getMeasuredHeight());
                }
            }

            if (maxHeight == measuredHeight) {
                return;
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight,
                    MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapter wrapper to insert extra views and otherwise hack around GridView to
 * add sections and headers.
 * 
 * @author Tonic Artos
 */
public class StickyGridHeadersAdapterWrapper extends BaseAdapter {
    private static final int NUM_VIEW_TYPES = 2;
    protected static final int POSITION_FAKE = -0x01;
    protected static final int POSITION_HEADER = -0x02;
    protected static final int ID_FAKE = -0x01;
    protected static final int ID_HEADER = -0x02;
    protected static final int VIEW_TYPE_FILLER = 0x00;
    protected static final int VIEW_TYPE_HEADER = 0x01;

    public final StickyGridHeadersAdapter delegate;

    private final Context context;

    private int count;
    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            updateCount();
        }

        @Override
        public void onInvalidated() {
            headerCache.clear();
        }
    };
    private StickyGridHeadersGridView gridView;

    private final List<View> headerCache = new ArrayList<View>();

    private int numColumns;
    private View[] rowSiblings;
    static private boolean prepopulation;

    public StickyGridHeadersAdapterWrapper(Context context, StickyGridHeadersGridView gridView, StickyGridHeadersAdapter delegate, int numColumns) {
        this.context = context;
        this.delegate = delegate;
        this.numColumns = numColumns;
        this.gridView = gridView;
        initRowSiblings(numColumns);
        delegate.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        count = 0;
        for (int i = 0; i < delegate.getNumHeaders(); i++) {
            // Pad count with space for header and trailing filler in header
            // group.
            count += delegate.getCountForHeader(i) + unFilledSpacesInHeaderGroup(i) + numColumns;
        }
        return count;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     * <p>
     * Since this wrapper inserts fake entries to fill out items grouped by
     * header and also spaces to insert headers into some positions will return
     * null.
     * </p>
     * 
     * @param position
     *            Position of the item whose data we want within the adapter's
     *            data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) throws ArrayIndexOutOfBoundsException {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.position == POSITION_FAKE || adapterPosition.position == POSITION_HEADER) {
            // Fake entry in view.
            return null;
        }

        return delegate.getItem(adapterPosition.position);
    }

    @Override
    public long getItemId(int position) {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.position == POSITION_HEADER) {
            return ID_HEADER;
        }
        if (adapterPosition.position == POSITION_FAKE) {
            return ID_FAKE;
        }
        return delegate.getItemId(adapterPosition.position);
    }

    @Override
    public int getItemViewType(int position) {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.position == POSITION_HEADER) {
            return VIEW_TYPE_HEADER;
        }
        if (adapterPosition.position == POSITION_FAKE) {
            return VIEW_TYPE_FILLER;
        }
        return delegate.getItemViewType(adapterPosition.position) + NUM_VIEW_TYPES;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ReferenceView container = null;
        if (convertView instanceof ReferenceView) {
            // Unpack from reference view;
            container = (ReferenceView) convertView;
            convertView = container.getChildAt(0);
        }

        Position adapterPosition = translatePosition(position);
        if (adapterPosition.position == POSITION_HEADER) {
            View v = getHeaderFillerView(adapterPosition.header, convertView, parent);
            ((HeaderFillerView) v).setId(position);
            convertView = (View) v.getTag();
            View header = delegate.getHeaderView(adapterPosition.header, convertView, parent);
            v.setTag(header);
            convertView = v;
        } else if (adapterPosition.position == POSITION_FAKE) {
            convertView = getFillerView(convertView, parent);
        } else {
            convertView = delegate.getView(adapterPosition.position, convertView, parent);
        }

        // Wrap in reference view if not already.
        if (container == null) {
            container = new ReferenceView(context);
        }
        container.removeAllViews();
        container.addView(convertView);

        container.setPosition(position);
        container.setNumColumns(numColumns);

        rowSiblings[position % numColumns] = container;
        if (position % numColumns == 0) {
            prepopulation = true;
            for (int i = 1; i < rowSiblings.length; i++) {
                rowSiblings[i] = getView(position + i, null, parent);
            }
            prepopulation = false;
        }

        container.setRowSiblings(rowSiblings);
        if (!prepopulation && (position % numColumns == (numColumns - 1) || position == getCount() - 1)) {
            // End of row or items.
            initRowSiblings(numColumns);
        }
        return container;
    }

    private void initRowSiblings(int numColumns) {
        rowSiblings = new View[numColumns];
        Arrays.fill(rowSiblings, null);
    }

    @Override
    public int getViewTypeCount() {
        return delegate.getViewTypeCount() + NUM_VIEW_TYPES;
    }

    @Override
    public boolean hasStableIds() {
        return delegate.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean isEnabled(int position) {
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.position == POSITION_FAKE || adapterPosition.position == POSITION_HEADER) {
            return false;
        }

        return delegate.isEnabled(adapterPosition.position);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        delegate.registerDataSetObserver(observer);
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        initRowSiblings(numColumns);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        delegate.unregisterDataSetObserver(observer);
    }

    private FillerView getFillerView(View convertView, ViewGroup parent) {
        FillerView fillerView = (FillerView) convertView;
        if (fillerView == null) {
            fillerView = new FillerView(context);
        }

        return fillerView;
    }

    private View getHeaderFillerView(int headerPosition, View convertView, ViewGroup parent) {
        HeaderFillerView headerFillerView = (HeaderFillerView) convertView;
        headerFillerView = new HeaderFillerView(context);
        headerFillerView.setHeaderWidth(gridView.getWidth());

        return headerFillerView;
    }

    /**
     * Counts the number of items that would be need to fill out the last row in
     * the group of items with the given header.
     * 
     * @param header
     *            Header set of items are grouped by.
     * @return The count of unfilled spaces in the last row.
     */
    private int unFilledSpacesInHeaderGroup(int header) {
        int remainder = delegate.getCountForHeader(header) % numColumns;
        return remainder == 0 ? 0 : numColumns - remainder;
    }

    protected Position translatePosition(int position) {
        // Translate GridView position to Adapter position.
        int adapterPosition = position;
        int place = position;
        int i;
        for (i = 0; i < delegate.getNumHeaders(); i++) {
            int sectionCount = delegate.getCountForHeader(i);

            // Skip past fake items making space for header in front of
            // sections.
            if (place == 0) {
                // Position is first column where header will be.
                return new Position(POSITION_HEADER, i);
            }
            place -= numColumns;
            if (place < 0) {
                // Position is a fake so return null.
                return new Position(POSITION_FAKE, i);
            }
            adapterPosition -= numColumns;

            if (place < sectionCount) {
                return new Position(adapterPosition, i);
            }

            // Skip past section end of section row filler;
            int filler = unFilledSpacesInHeaderGroup(i);
            adapterPosition -= filler;
            place -= sectionCount + filler;
        }

        // Position is a fake.
        return new Position(POSITION_FAKE, i);
    }

    protected void updateCount() {
        count = 0;
        for (int i = 0; i < delegate.getNumHeaders(); i++) {
            count += delegate.getCountForHeader(i) + numColumns;
        }
    }

    protected class HeaderHolder {
        protected View headerView;
    }

    protected class Position {
        protected int header;
        protected int position;

        protected Position(int position, int header) {
            this.position = position;
            this.header = header;
        }
    }

    protected long getHeaderId(int position) {
        return translatePosition(position).header;
    }

    protected View getHeaderView(int position, View convertView, ViewGroup parent) {
        return delegate.getHeaderView(translatePosition(position).header, convertView, parent);
    }
}

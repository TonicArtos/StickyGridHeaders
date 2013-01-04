package com.tonicartos.widget.stickygridheaders;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StickyGridHeadersAdapterWrapper extends BaseAdapter {
    private static final int NUM_VIEW_TYPES = 2;
    private static final int POSITION_FAKE = -0x01;
    private static final int POSITION_HEADER = -0x02;
    private static final int VIEW_TYPE_FILLER = 0x00;
    private static final int VIEW_TYPE_HEADER = 0x01;

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
    private final List<View> headerCache = new ArrayList<View>();

    private LayoutInflater inflater;

    private int numColumns;
    private StickyGridHeadersGridView gridView;

    public StickyGridHeadersAdapterWrapper(Context context, StickyGridHeadersGridView gridView, StickyGridHeadersAdapter delegate, int numColumns) {
        this.context = context;
        this.delegate = delegate;
        this.numColumns = numColumns;
        this.gridView = gridView;
        delegate.registerDataSetObserver(dataSetObserver);
        inflater = LayoutInflater.from(context);
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
            return POSITION_HEADER;
        }
        if (adapterPosition.position == POSITION_FAKE) {
            return POSITION_FAKE;
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
        Position adapterPosition = translatePosition(position);
        if (adapterPosition.position == POSITION_HEADER) {
            View v = getFillerView(convertView, parent);
            convertView = (View) v.getTag();
            View header = delegate.getHeaderView(adapterPosition.header, convertView, parent);
            v.setTag(header);
            gridView.requestDraw(header);
            return v;
        }
        if (adapterPosition.position == POSITION_FAKE) {
            return getFillerView(convertView, parent);
        }
        return delegate.getView(adapterPosition.position, convertView, parent);
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
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        delegate.unregisterDataSetObserver(observer);
    }

    private View getFillerView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        return convertView;
    }

    private View getHeaderFillerView(int headerPosition, View convertView, ViewGroup parent) {
        if (convertView == null) {
            HeaderFillerView headerView = new HeaderFillerView(context);
            headerView.forceWidth(gridView.getWidth());
            convertView = headerView;
        }
        return convertView;
    }

    protected class HeaderHolder {
        protected View headerView;
    }

    protected Position translatePosition(int position) {
        // Translate GridView position to Adapter position.
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
            position -= numColumns;

            if (place < sectionCount) {
                return new Position(position, i);
            }

            // Skip past section end of section row filler;
            int filler = unFilledSpacesInHeaderGroup(i);
            position -= filler;
            place -= sectionCount + filler;
        }

        // Position is a fake (or beyond end of data set) so return null.
        return new Position(POSITION_FAKE, i);
    }

    protected class Position {
        protected int position;
        protected int header;

        protected Position(int position, int header) {
            this.position = position;
            this.header = header;
        }
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

    protected void updateCount() {
        count = 0;
        for (int i = 0; i < delegate.getNumHeaders(); i++) {
            count += delegate.getCountForHeader(i) + numColumns;
        }
    }
}

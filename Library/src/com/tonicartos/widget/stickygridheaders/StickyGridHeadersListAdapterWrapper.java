package com.tonicartos.widget.stickygridheaders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public class StickyGridHeadersListAdapterWrapper extends BaseAdapter implements StickyGridHeadersBaseAdapter {
    private ListAdapter mDelegate;

    public StickyGridHeadersListAdapterWrapper(ListAdapter adapter) {
        mDelegate = adapter;
    }

    @Override
    public int getCount() {
        return mDelegate.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mDelegate.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return mDelegate.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mDelegate.getView(position, convertView, parent);
    }

    @Override
    public int getCountForHeader(int header) {
        return 0;
    }

    @Override
    public int getNumHeaders() {
        return 0;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        return null;
    }

}

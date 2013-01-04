package com.tonicartos.widget.stickygridheaders;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ArrayStickyGridHeadersAdapter<T> extends BaseAdapter implements StickyGridHeadersAdapter {
    private int headerResId;
    private List<ArrayStickyGridHeaderData> headers;
    private LayoutInflater inflater;
    private int itemResId;
    private List<T> items;

    public ArrayStickyGridHeadersAdapter(Context context, ArrayStickyGridHeaderData[] headers, T[] items, int headerResId, int itemResId) {
        init(context, Arrays.asList(headers), Arrays.asList(items), headerResId, itemResId);
    }

    public ArrayStickyGridHeadersAdapter(Context context, List<ArrayStickyGridHeaderData> headers, List<T> items, int headerResId, int itemResId) {
        init(context, headers, items, headerResId, itemResId);
    }

    private void init(Context context, List<ArrayStickyGridHeaderData> headers, List<T> items, int headerResId, int itemResId) {
        this.headers = headers;
        this.items = items;
        this.headerResId = headerResId;
        this.itemResId = itemResId;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public int getCountForHeader(int header) {
        return headers.get(header).count;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(headerResId, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }

        ((HeaderViewHolder) convertView.getTag()).textView.setText(headers.get(position).header);
        return convertView;
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getNumHeaders() {
        return headers.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(itemResId, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        T item = getItem(position);
        if (item instanceof CharSequence) {
            holder.textView.setText((CharSequence) item);
        } else {
            holder.textView.setText(item.toString());
        }

        return convertView;
    }

    protected class HeaderViewHolder {
        public TextView textView;
    }

    protected class ViewHolder {
        public TextView textView;
    }
}

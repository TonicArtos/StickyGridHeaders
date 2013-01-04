package com.tonicartos.widgets.stickygridheaders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SimpleArrayStickyGridHeadersAdapter extends BaseAdapter implements StickyGridHeadersAdapter {
    private int headerResId;
    private SimpleStickyGridHeaderData[] headers;
    private LayoutInflater inflater;
    private int itemResId;
    private String[] items;

    public SimpleArrayStickyGridHeadersAdapter(Context context, SimpleStickyGridHeaderData[] headers, String[] items, int headerResId, int itemResId) {
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
        return items.length;
    }

    @Override
    public int getCountForHeader(int header) {
        return headers[header].count;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(headerResId, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder();
            holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }

        ((HeaderViewHolder) convertView.getTag()).textView.setText(headers[position].header);
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getNumHeaders() {
        return headers.length;
    }

    @Override
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

        holder.textView.setText(items[position]);
        return convertView;
    }

    protected class HeaderViewHolder {
        public TextView textView;
    }

    protected class ViewHolder {
        public TextView textView;
    }
}

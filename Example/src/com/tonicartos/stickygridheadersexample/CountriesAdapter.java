
package com.tonicartos.stickygridheadersexample;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tonicartos.widget.stickygridheaders.StickyHeadersListAdapter;

public class CountriesAdapter extends BaseAdapter implements StickyHeadersListAdapter {
    private static final int VIEW_TYPE_COUNTRY = 0x01;

    private static final int VIEW_TYPE_COUNTRY_HEADER = 0x02;

    private List<Item> mItems;

    private LayoutInflater mInflater;

    public CountriesAdapter(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        String[] countries = context.getResources().getStringArray(R.array.countries);
        mItems = adaptArray(countries);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public long getHeaderId(int position) {
        return mItems.get(position).getHeaderId();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = mItems.get(position);
        switch (item.getViewType()) {
            case VIEW_TYPE_COUNTRY_HEADER:
                return getCountryHeaderView(item, convertView, parent);
            case VIEW_TYPE_COUNTRY:
            default:
                return getCountryView(item, convertView, parent);
        }
    }

    private View getCountryView(Item item, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.setViewValues(item);

        return convertView;
    }

    private View getCountryHeaderView(Item item, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.header, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.setViewValues(item);

        return convertView;
    }

    class ViewHolder {
        TextView textView;

        public ViewHolder(View view) {
            textView = (TextView)view.findViewById(android.R.id.text1);
        }

        public void setViewValues(Item item) {
            textView.setText(item.toString());
        }
    }

    @Override
    public boolean isHeader(int position) {
        return mItems.get(position).isHeader();
    }

    private List<Item> adaptArray(String[] countries) {
        ArrayList<Item> items = new ArrayList<Item>();
        long lastHeaderId = countries[0].charAt(0);
        for (int i = 0; i < countries.length; i++) {
            String country = countries[i];
            char headerId = country.charAt(0);

            if (lastHeaderId != headerId) {
                lastHeaderId = headerId;
                items.add(new Header(lastHeaderId, String.valueOf(headerId)));
            }
            items.add(new Country(lastHeaderId, country));
        }
        return items;
    }

    class Country extends Item {
        private long mHeaderId;

        public Country(long headerId, String name) {
            mValue = name;
            mHeaderId = headerId;
        }

        @Override
        long getHeaderId() {
            return mHeaderId;
        }

        @Override
        int getViewType() {
            return VIEW_TYPE_COUNTRY;
        }

        @Override
        boolean isHeader() {
            return false;
        }
    }

    class Header extends Item {
        private long mId;

        public Header(long id, String title) {
            mValue = title;
            mId = id;
        }

        @Override
        long getHeaderId() {
            return mId;
        }

        @Override
        int getViewType() {
            return VIEW_TYPE_COUNTRY_HEADER;
        }

        @Override
        boolean isHeader() {
            return true;
        }
    }

    abstract class Item {
        String mValue;

        @Override
        public String toString() {
            return mValue;
        }

        abstract long getHeaderId();

        abstract int getViewType();

        abstract boolean isHeader();
    }
}

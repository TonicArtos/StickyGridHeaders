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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class StickyGridHeadersSimpleAdapterWrapper extends BaseAdapter implements StickyGridHeadersBaseAdapter {
    private StickyGridHeadersSimpleAdapter delegate;
    private List<HeaderData> headers;

    public StickyGridHeadersSimpleAdapterWrapper(StickyGridHeadersSimpleAdapter adapter) {
        delegate = adapter;
        adapter.registerDataSetObserver(new DataSetObserverExtension());
        headers = generateHeaderList(adapter);
    }

    @Override
    public int getCount() {
        return delegate.getCount();
    }

    @Override
    public int getCountForHeader(int position) {
        return headers.get(position).getCount();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        return delegate.getHeaderView(headers.get(position).getRefPosition(), convertView, parent);
    }

    @Override
    public Object getItem(int position) {
        return delegate.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return delegate.getItemId(position);
    }

    @Override
    public int getNumHeaders() {
        return headers.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return delegate.getView(position, convertView, parent);
    }

    protected List<HeaderData> generateHeaderList(StickyGridHeadersSimpleAdapter adapter) {
        Map<Long, HeaderData> mapping = new HashMap<Long, HeaderData>();
        List<HeaderData> headers = new ArrayList<HeaderData>();

        for (int i = 0; i < adapter.getCount(); i++) {
            long headerId = adapter.getHeaderId(i);
            HeaderData headerData = mapping.get(headerId);
            if (headerData == null) {
                headerData = new HeaderData(i);
                headers.add(headerData);
            }
            headerData.incrementCount();
            mapping.put(headerId, headerData);
        }

        return headers;
    }

    private final class DataSetObserverExtension extends DataSetObserver {
        @Override
        public void onChanged() {
            headers = generateHeaderList(delegate);
        }

        @Override
        public void onInvalidated() {
            headers.clear();
        }
    }

    private class HeaderData {
        private int count;
        private int refPosition;

        public HeaderData(int refPosition) {
            this.refPosition = refPosition;
            this.count = 0;
        }

        public int getCount() {
            return count;
        }

        public int getRefPosition() {
            return refPosition;
        }

        public void incrementCount() {
            count++;
        }
    }
}

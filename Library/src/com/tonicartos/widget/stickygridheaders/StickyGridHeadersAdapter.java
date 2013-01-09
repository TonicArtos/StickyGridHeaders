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

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/**
 * Adapter interface for StickyGridHeadersGridView.
 * 
 * @author Tonic Artos
 */
public interface StickyGridHeadersAdapter extends ListAdapter {
    /**
     * Get a View that displays the header data at the specified position in the
     * set. You can either create a View manually or inflate it from an XML
     * layout file.
     * 
     * @param position
     *            The position of the item within the adapter's data set of the
     *            item whose header view we want.
     * @param convertView
     *            The old view to reuse, if possible. Note: You should check
     *            that this view is non-null and of an appropriate type before
     *            using. If it is not possible to convert this view to display
     *            the correct data, this method can create a new view.
     * @param parent
     *            The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    View getHeaderView(int position, View convertView, ViewGroup parent);

    /**
     * Get the number of headers in the adapter's data set.
     * 
     * @return Number of headers.
     */
    public int getNumHeaders();

    /**
     * Get the number of items with a given header.
     * 
     * @param section
     *            The header in the adapter's data set.
     * @return The number of items for the specified header.
     */
    public int getCountForHeader(int header);

    @Override
    /**
     * Unneeded for StickyGridHeadersAdapter.
     */
    public int getCount();

    /**
     * Get a view that is used to fill in the empty spaces behind the header.
     * The height of the view should match the header view.
     * <p>
     * Tags on this view will be discarded.
     * </p>
     * 
     * @param convertView
     *            View to reuse for the header filler.
     * @param parent
     * @return Header filler view.
     */
    // public View getHeaderFillerView(View convertView, ViewGroup parent);
}

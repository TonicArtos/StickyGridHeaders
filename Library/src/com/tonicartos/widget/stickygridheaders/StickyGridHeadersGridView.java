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
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapterWrapper.HeaderFillerView;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapterWrapper.ReferenceView;

/**
 * GridView that displays items in sections with headers that stick to the top
 * of the view.
 *
 * @author Tonic Artos
 */
public class StickyGridHeadersGridView extends GridView implements OnScrollListener, OnItemClickListener, OnItemSelectedListener, OnItemLongClickListener {
    private StickyGridHeadersBaseAdapterWrapper adapter;
    private boolean areHeadersSticky = true;
    private final Rect clippingRect = new Rect();
    private boolean clippingToPadding;
    private boolean clipToPaddingHasBeenSet;
    private DataSetObserver dataSetChangedObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            reset();
        }

        @Override
        public void onInvalidated() {
            reset();
        }
    };
    private View stickiedHeader;
    private int headerBottomPosition;
    private int numColumns;

    private OnScrollListener scrollListener;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemSelectedListener onItemSelectedListener;
    private long currentHeaderId = -1;

    public StickyGridHeadersGridView(Context context) {
        this(context, null);
    }

    public StickyGridHeadersGridView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.gridViewStyle);
    }

    public StickyGridHeadersGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);
        setVerticalFadingEdgeEnabled(false);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (scrollListener != null) {
            scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            scrollChanged(firstVisibleItem);
        }
    }

    private void measureHeader() {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
        int heightMeasureSpec = 0;

        ViewGroup.LayoutParams params = stickiedHeader.getLayoutParams();
        if (params != null && params.height > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        stickiedHeader.measure(widthMeasureSpec, heightMeasureSpec);
        stickiedHeader.layout(getLeft() + getPaddingLeft(), 0, getRight() - getPaddingRight(), stickiedHeader.getMeasuredHeight());
    }

    private void scrollChanged(int firstVisibleItem) {
        if (adapter == null || adapter.getCount() == 0 || !areHeadersSticky) {
            return;
        }

        ReferenceView firstItem = (ReferenceView) getChildAt(0);
        if (firstItem == null) {
            return;
        }
        // if (firstItem.getView() instanceof HeaderFillerView) {
        // stickiedHeader = (View) firstItem.getTag();
        // }

        long newHeaderId = adapter.getHeaderId(firstVisibleItem);
        if (currentHeaderId != newHeaderId) {
            stickiedHeader = adapter.getHeaderView(firstVisibleItem, stickiedHeader, this);
            measureHeader();
        }
        currentHeaderId = newHeaderId;

        final int childCount = getChildCount();
        if (childCount != 0) {
            View viewToWatch = null;
            int watchingChildDistance = 99999;

            // Find the next header after the stickied one.
            for (int i = 0; i < childCount; i += numColumns) {
                ReferenceView child = (ReferenceView) super.getChildAt(i);

                int childDistance;
                if (clippingToPadding) {
                    childDistance = child.getTop() - getPaddingTop();
                } else {
                    childDistance = child.getTop();
                }

                if (childDistance < 0) {
                    continue;
                }

                if (child.getView() instanceof HeaderFillerView && childDistance < watchingChildDistance) {
                    viewToWatch = child;
                    watchingChildDistance = childDistance;
                }
            }

            int headerHeight = getHeaderHeight();

            // Work out where to draw stickied header using synchronised
            // scrolling.
            if (viewToWatch != null) {
                if (firstVisibleItem == 0 && super.getChildAt(0).getTop() > 0 && !clippingToPadding) {
                    headerBottomPosition = 0;
                } else {
                    if (clippingToPadding) {
                        headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight + getPaddingTop());
                        headerBottomPosition = headerBottomPosition < getPaddingTop() ? headerHeight + getPaddingTop() : headerBottomPosition;
                    } else {
                        headerBottomPosition = Math.min(viewToWatch.getTop(), headerHeight);
                        headerBottomPosition = headerBottomPosition < 0 ? headerHeight : headerBottomPosition;
                    }
                }
            } else {
                headerBottomPosition = headerHeight;
                if (clippingToPadding) {
                    headerBottomPosition += getPaddingTop();
                }
            }
        }
    }

    private int getHeaderHeight() {
        if (stickiedHeader != null) {
            return stickiedHeader.getMeasuredHeight();
        }
        return 0;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollListener != null) {
            scrollListener.onScrollStateChanged(view, scrollState);
        }

    }

    @Override
    public void setOnItemClickListener(android.widget.AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
        super.setOnItemClickListener(this);
    }

    @Override
    public void setOnItemLongClickListener(android.widget.AdapterView.OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
        super.setOnItemLongClickListener(this);
    }

    @Override
    public void setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
        super.setOnItemSelectedListener(this);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!clipToPaddingHasBeenSet) {
            clippingToPadding = true;
        }
        if (!(adapter instanceof StickyGridHeadersBaseAdapter || adapter instanceof StickyGridHeadersSimpleAdapter)) {
            throw new IllegalArgumentException("Adapter must implement either StickyGridHeadersSimpleAdapter or StickyGridHeadersBaseAdapter");
        }
        // NOTE: There may be a problem with getNumColumns(), it could give -1
        // which isn't useful.
        if (adapter instanceof StickyGridHeadersSimpleAdapter) {
            adapter = new StickyGridHeadersSimpleAdapterWrapper((StickyGridHeadersSimpleAdapter) adapter);
        }
        this.adapter = new StickyGridHeadersBaseAdapterWrapper(getContext(), this, (StickyGridHeadersBaseAdapter) adapter, numColumns);
        this.adapter.registerDataSetObserver(dataSetChangedObserver);
        reset();
        super.setAdapter(this.adapter);
    }

    @Override
    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        clippingToPadding = clipToPadding;
        clipToPaddingHasBeenSet = true;
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        this.numColumns = numColumns;

        if (adapter != null) {
            adapter.setNumColumns(numColumns);
        }
    }

    private void reset() {
        headerBottomPosition = 0;
        stickiedHeader = null;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            scrollChanged(getFirstVisiblePosition());
        }

        // Mask the region where we will draw the header later...
        int headerHeight = getHeaderHeight();
        int top = headerBottomPosition - headerHeight;

        clippingRect.left = getPaddingLeft();
        clippingRect.right = getWidth() - getPaddingRight();
        if (clippingToPadding) {
            clippingRect.top = getPaddingTop() + headerBottomPosition;
            clippingRect.bottom = getHeight() - getPaddingBottom();
        } else {
            clippingRect.top = headerBottomPosition;
            clippingRect.bottom = getHeight();
        }

        canvas.save();
        canvas.clipRect(clippingRect);

        // ...and draw the grid view.
        super.dispatchDraw(canvas);

        // Find headers.
        List<Integer> headerPositions = new ArrayList<Integer>();
        int vi = 0;
        for (int i = getFirstVisiblePosition(); i <= getLastVisiblePosition();) {
            long id = getItemIdAtPosition(i);
            if (id == StickyGridHeadersBaseAdapterWrapper.ID_HEADER) {
                headerPositions.add(vi);
            }
            i += numColumns;
            vi += numColumns;
        }

        // Draw headers in list.
        for (int i = 0; i < headerPositions.size(); i++) {
            View frame = getChildAt(headerPositions.get(i));
            View header = (View) frame.getTag();

            int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
            int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            header.measure(widthMeasureSpec, heightMeasureSpec);
            header.layout(getLeft() + getPaddingLeft(), 0, getRight() - getPaddingRight(), frame.getHeight());

            clippingRect.left = getPaddingLeft();
            clippingRect.right = getWidth() - getPaddingRight();
            clippingRect.bottom = frame.getBottom();
            clippingRect.top = frame.getTop();
            canvas.save();
            canvas.clipRect(clippingRect);
            canvas.translate(getPaddingLeft(), frame.getTop());
            header.draw(canvas);
            canvas.restore();
        }
        canvas.restore();

        if (stickiedHeader == null || !areHeadersSticky) {
            return;
        }

        // Draw stickied header.
        clippingRect.left = getPaddingLeft();
        clippingRect.right = getWidth() - getPaddingRight();
        clippingRect.bottom = top + headerHeight;
        if (clippingToPadding) {
            clippingRect.top = getPaddingTop();
        } else {
            clippingRect.top = 0;
        }

        canvas.save();
        canvas.clipRect(clippingRect);
        canvas.translate(getPaddingLeft(), top);
        stickiedHeader.draw(canvas);
        canvas.restore();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onItemClickListener.onItemClick(parent, view, adapter.translatePosition(position).position, id);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return onItemLongClickListener.onItemLongClick(parent, view, adapter.translatePosition(position).position, id);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onItemSelectedListener.onItemSelected(parent, view, adapter.translatePosition(position).position, id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        onItemSelectedListener.onNothingSelected(parent);
    }
}

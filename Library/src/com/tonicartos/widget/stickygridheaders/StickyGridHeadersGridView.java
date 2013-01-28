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
 * @author Tonic Artos, Emil Sjölander
 */
public class StickyGridHeadersGridView extends GridView implements OnScrollListener, OnItemClickListener, OnItemSelectedListener, OnItemLongClickListener {
    private StickyGridHeadersBaseAdapterWrapper mAdapter;
    private boolean mAreHeadersSticky = true;
    private final Rect mClippingRect = new Rect();
    private boolean mClippingToPadding;
    private boolean mClipToPaddingHasBeenSet;
    private long mCurrentHeaderId = -1;
    private DataSetObserver mDataSetChangedObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            reset();
        }

        @Override
        public void onInvalidated() {
            reset();
        }
    };
    private int mHeaderBottomPosition;
    private int mNumColumns;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemSelectedListener mOnItemSelectedListener;
    private OnScrollListener mScrollListener;
    private View mStickiedHeader;

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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mOnItemClickListener.onItemClick(parent, view, mAdapter.translatePosition(position).mPosition, id);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return mOnItemLongClickListener.onItemLongClick(parent, view, mAdapter.translatePosition(position).mPosition, id);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mOnItemSelectedListener.onItemSelected(parent, view, mAdapter.translatePosition(position).mPosition, id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mOnItemSelectedListener.onNothingSelected(parent);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            scrollChanged(firstVisibleItem);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }

    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!mClipToPaddingHasBeenSet) {
            mClippingToPadding = true;
        }
        if (!(adapter instanceof StickyGridHeadersBaseAdapter || adapter instanceof StickyGridHeadersSimpleAdapter)) {
            throw new IllegalArgumentException("Adapter must implement either StickyGridHeadersSimpleAdapter or StickyGridHeadersBaseAdapter");
        }
        // NOTE: There may be a problem with getNumColumns(), it could give -1
        // which isn't useful.
        if (adapter instanceof StickyGridHeadersSimpleAdapter) {
            adapter = new StickyGridHeadersSimpleAdapterWrapper((StickyGridHeadersSimpleAdapter) adapter);
        }
        this.mAdapter = new StickyGridHeadersBaseAdapterWrapper(getContext(), this, (StickyGridHeadersBaseAdapter) adapter, mNumColumns);
        this.mAdapter.registerDataSetObserver(mDataSetChangedObserver);
        reset();
        super.setAdapter(this.mAdapter);
    }

    @Override
    public void setClipToPadding(boolean clipToPadding) {
        super.setClipToPadding(clipToPadding);
        mClippingToPadding = clipToPadding;
        mClipToPaddingHasBeenSet = true;
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        this.mNumColumns = numColumns;

        if (mAdapter != null) {
            mAdapter.setNumColumns(numColumns);
        }
    }

    @Override
    public void setOnItemClickListener(android.widget.AdapterView.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
        super.setOnItemClickListener(this);
    }

    @Override
    public void setOnItemLongClickListener(android.widget.AdapterView.OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
        super.setOnItemLongClickListener(this);
    }

    @Override
    public void setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
        super.setOnItemSelectedListener(this);
    }

    private int getHeaderHeight() {
        if (mStickiedHeader != null) {
            return mStickiedHeader.getMeasuredHeight();
        }
        return 0;
    }

    private void measureHeader() {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
        int heightMeasureSpec = 0;

        ViewGroup.LayoutParams params = mStickiedHeader.getLayoutParams();
        if (params != null && params.height > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        mStickiedHeader.measure(widthMeasureSpec, heightMeasureSpec);
        mStickiedHeader.layout(getLeft() + getPaddingLeft(), 0, getRight() - getPaddingRight(), mStickiedHeader.getMeasuredHeight());
    }

    private void reset() {
        mHeaderBottomPosition = 0;
        mStickiedHeader = null;
    }

    private void scrollChanged(int firstVisibleItem) {
        if (mAdapter == null || mAdapter.getCount() == 0 || !mAreHeadersSticky) {
            return;
        }

        ReferenceView firstItem = (ReferenceView) getChildAt(0);
        if (firstItem == null) {
            return;
        }

        long newHeaderId = mAdapter.getHeaderId(firstVisibleItem);
        if (mCurrentHeaderId != newHeaderId) {
            mStickiedHeader = mAdapter.getHeaderView(firstVisibleItem, mStickiedHeader, this);
            measureHeader();
        }
        mCurrentHeaderId = newHeaderId;

        final int childCount = getChildCount();
        if (childCount != 0) {
            View viewToWatch = null;
            int watchingChildDistance = 99999;

            // Find the next header after the stickied one.
            for (int i = 0; i < childCount; i += mNumColumns) {
                ReferenceView child = (ReferenceView) super.getChildAt(i);

                int childDistance;
                if (mClippingToPadding) {
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
                if (firstVisibleItem == 0 && super.getChildAt(0).getTop() > 0 && !mClippingToPadding) {
                    mHeaderBottomPosition = 0;
                } else {
                    if (mClippingToPadding) {
                        mHeaderBottomPosition = Math.min(viewToWatch.getTop(), headerHeight + getPaddingTop());
                        mHeaderBottomPosition = mHeaderBottomPosition < getPaddingTop() ? headerHeight + getPaddingTop() : mHeaderBottomPosition;
                    } else {
                        mHeaderBottomPosition = Math.min(viewToWatch.getTop(), headerHeight);
                        mHeaderBottomPosition = mHeaderBottomPosition < 0 ? headerHeight : mHeaderBottomPosition;
                    }
                }
            } else {
                mHeaderBottomPosition = headerHeight;
                if (mClippingToPadding) {
                    mHeaderBottomPosition += getPaddingTop();
                }
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            scrollChanged(getFirstVisiblePosition());
        }

        // Mask the region where we will draw the header later...
        int headerHeight = getHeaderHeight();
        int top = mHeaderBottomPosition - headerHeight;

        mClippingRect.left = getPaddingLeft();
        mClippingRect.right = getWidth() - getPaddingRight();
        if (mClippingToPadding) {
            mClippingRect.top = getPaddingTop() + mHeaderBottomPosition;
            mClippingRect.bottom = getHeight() - getPaddingBottom();
        } else {
            mClippingRect.top = mHeaderBottomPosition;
            mClippingRect.bottom = getHeight();
        }

        canvas.save();
        canvas.clipRect(mClippingRect);

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
            i += mNumColumns;
            vi += mNumColumns;
        }

        // Draw headers in list.
        for (int i = 0; i < headerPositions.size(); i++) {
            View frame = getChildAt(headerPositions.get(i));
            View header = (View) frame.getTag();

            int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY);
            int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            header.measure(widthMeasureSpec, heightMeasureSpec);
            header.layout(getLeft() + getPaddingLeft(), 0, getRight() - getPaddingRight(), frame.getHeight());

            mClippingRect.left = getPaddingLeft();
            mClippingRect.right = getWidth() - getPaddingRight();
            mClippingRect.bottom = frame.getBottom();
            mClippingRect.top = frame.getTop();
            canvas.save();
            canvas.clipRect(mClippingRect);
            canvas.translate(getPaddingLeft(), frame.getTop());
            header.draw(canvas);
            canvas.restore();
        }
        canvas.restore();

        if (mStickiedHeader == null || !mAreHeadersSticky) {
            return;
        }

        // Draw stickied header.
        mClippingRect.left = getPaddingLeft();
        mClippingRect.right = getWidth() - getPaddingRight();
        mClippingRect.bottom = top + headerHeight;
        if (mClippingToPadding) {
            mClippingRect.top = getPaddingTop();
        } else {
            mClippingRect.top = 0;
        }

        canvas.save();
        canvas.clipRect(mClippingRect);
        canvas.translate(getPaddingLeft(), top);
        mStickiedHeader.draw(canvas);
        canvas.restore();
    }
}

package com.tonicartos.widget.stickygridheaders;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ListAdapter;

public class StickyGridHeadersGridView extends GridView implements OnScrollListener, android.widget.AdapterView.OnItemClickListener, android.widget.AdapterView.OnItemSelectedListener, android.widget.AdapterView.OnItemLongClickListener {

    private StickyGridHeadersAdapterWrapper adapter;
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
    private View header;
    private int headerBottomPosition;
    private boolean headerHasChanged = true;
    private int headerHeight = -1;
    private int numColumns;

    private Long oldHeaderId = null;

    private OnScrollListener scrollListener;
    private List<View> headerRequests = new ArrayList<View>();
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemSelectedListener onItemSelectedListener;

    public StickyGridHeadersGridView(Context context) {
        super(context);
    }

    public StickyGridHeadersGridView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.gridViewStyle);
    }

    public StickyGridHeadersGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnScrollListener(this);
        setVerticalFadingEdgeEnabled(false);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
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
        if (!(adapter instanceof StickyGridHeadersAdapter)) {
            throw new IllegalArgumentException("Adapter must implement StickyGridHeadersAdapter");
        }
        // NOTE: There may be a problem with getNumColumns(), it could give -1
        // which isn't useful.
        this.adapter = new StickyGridHeadersAdapterWrapper(getContext(), this, (StickyGridHeadersAdapter) adapter, numColumns);
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
        headerHeight = -1;
        header = null;
        oldHeaderId = null;
        headerHasChanged = true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // HeaderView headerFrame;
        // for (int i = 0; i < headerRequests.size(); i++) {
        // headerFrame = headerRequests.get(i);
        // headerFrame.forceWidth(getWidth());
        // }
        super.dispatchDraw(canvas);

        List<Integer> vis = new ArrayList<Integer>();
        int vi = 0;
        for (int i = getFirstVisiblePosition(); i <= getLastVisiblePosition(); i++) {
            long id = getItemIdAtPosition(i);
            if (id == -2) {
                vis.add(vi);
            }
            vi++;
        }

        for (int i = 0; i < vis.size(); i++) {
            View frame = getChildAt(vis.get(i));
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
        //
        // View header;
        // for (int i = 0; i < headerRequests.size(); i++) {
        // headerFrame = headerRequests.get(i);
        // header = headerFrame.getChildAt(0);
        // int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth(),
        // MeasureSpec.EXACTLY);
        // int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0,
        // MeasureSpec.UNSPECIFIED);
        // header.measure(widthMeasureSpec, heightMeasureSpec);
        // clippingRect.left = getPaddingLeft();
        // clippingRect.right = getWidth() - getPaddingRight();
        // clippingRect.bottom = headerFrame.getBottom();
        // clippingRect.top = headerFrame.getTop();
        // }
        //
        // canvas.save();
        // canvas.clipRect(clippingRect);
        // canvas.translate(getPaddingLeft(), top);
        // header.draw(canvas);
        // canvas.restore();
    }

    public void requestDraw(View headerView) {
        headerRequests.add(headerView);
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

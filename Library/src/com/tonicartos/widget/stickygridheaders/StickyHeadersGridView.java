
package com.tonicartos.widget.stickygridheaders;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.GridView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StickyHeadersGridView extends AdapterView<StickyHeadersListAdapter> {
    /**
     * The list allows multiple choices
     */
    public static final int CHOICE_MODE_MULTIPLE = 2;

    /**
     * The list allows multiple choices in a modal selection mode
     */
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 3;

    /**
     * Normal list that does not indicate choices
     */
    public static final int CHOICE_MODE_NONE = 0;

    /**
     * The list allows up to one choice
     */
    public static final int CHOICE_MODE_SINGLE = 1;

    public static final int INVALID_POSITION = -1;

    private static final int NO_MATCHED_CHILD = INVALID_POSITION;

    protected static final int TOUCH_MODE_AT_REST = -0x01;

    /**
     * Indicates we have waited for everything we can wait for, but the user's
     * finger is still down
     */
    protected static final int TOUCH_MODE_DONE_WAITING = 0x02;

    /**
     * Indicates we just received the touch event and we are waiting to see if
     * the it is a tap or a scroll gesture.
     */
    protected static final int TOUCH_MODE_DOWN = 0x00;

    protected static final int TOUCH_MODE_FINISHED_LONG_PRESS = -0x02;

    /**
     * Indicates the touch has been recognized as a tap and we are now waiting
     * to see if the touch is a longpress
     */
    protected static final int TOUCH_MODE_TAP = 0x01;

    /**
     * Show the last item
     */
    static final int LAYOUT_FORCE_BOTTOM = 3;

    /**
     * Show the first item
     */
    static final int LAYOUT_FORCE_TOP = 1;

    /**
     * Layout as a result of using the navigation keys
     */
    static final int LAYOUT_MOVE_SELECTION = 6;

    /**
     * Regular layout - usually an unsolicited layout from the view system
     */
    static final int LAYOUT_NORMAL = 0;

    /**
     * Force the selected item to be on somewhere on the screen
     */
    static final int LAYOUT_SET_SELECTION = 2;

    /**
     * Make a mSelectedItem appear in a specific location and build the rest of
     * the views from there. The top is specified by mSpecificTop.
     */
    static final int LAYOUT_SPECIFIC = 4;

    /**
     * Layout to sync as a result of a data change. Restore mSyncPosition to
     * have its top at mSpecificTop
     */
    static final int LAYOUT_SYNC = 5;

    /**
     * Indicates the view is in the process of being flung
     */
    static final int TOUCH_MODE_FLING = 4;

    /**
     * Indicates the view is being flung outside of normal content bounds and
     * will spring back.
     */
    static final int TOUCH_MODE_OVERFLING = 6;

    /**
     * Indicates the touch gesture is an overscroll - a scroll beyond the
     * beginning or end.
     */
    static final int TOUCH_MODE_OVERSCROLL = 5;

    /**
     * Indicates that we are not in the middle of a touch gesture
     */
    static final int TOUCH_MODE_REST = -1;

    /**
     * Indicates the touch gesture is a scroll
     */
    static final int TOUCH_MODE_SCROLL = 3;

    static View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
        int size = scrapViews.size();
        if (size > 0) {
            // See if we still have a view for this position.
            for (int i = 0; i < size; i++) {
                View view = scrapViews.get(i);
                if (((LayoutParams)view.getLayoutParams()).scrappedFromPosition == position) {
                    scrapViews.remove(i);
                    return view;
                }
            }
            return scrapViews.remove(size - 1);
        } else {
            return null;
        }
    }

    public StickyHeadersListAdapter mAdapter;

    public boolean mDataChanged;

    public int mItemCount;

    public int mOldItemCount;

    private boolean mAdapterHasStableIds;

    /**
     * Indicates that this list is always drawn on top of a solid, single-color,
     * opaque background
     */
    private int mCacheColorHint;

    private LongSparseArray<Integer> mCheckedIdStates;

    private int mChoiceMode;

    private DataSetObserver mDataSetObserver;

    private Method mDispatchFinishTemporaryDetach;

    private Method mDispatchStartTemporaryDetach;

    private CheckForLongPress mPendingCheckForLongPress;

    private CheckForTap mPendingCheckForTap;

    private PerformClick mPerformPropClick;

    private Drawable mSelector;

    private Rect mSelectorRect = new Rect();

    private Runnable mTouchModeReset;

    protected int mMotionPosition;

    protected int mMotionX;

    protected int mMotionY;

    protected int mTouchMode;

    protected int mTouchSlop;

    /**
     * The data set used to store unused views that should be reused during the
     * next layout to avoid creating new ones
     */
    final RecycleBin mRecycler = new RecycleBin();

    public StickyHeadersGridView(Context context) {
        super(context);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        initHiddenMethods();
    }

    public StickyHeadersGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        initHiddenMethods();
    }

    public StickyHeadersGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        initHiddenMethods();
    }

    public long childViewPositionToId(int clickMotionPosition) {
        // TODO Auto-generated method stub
        return INVALID_POSITION;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    public StickyHeadersListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public View getSelectedView() {
        throw new RuntimeException("Unsupport method: View setSelectedView()");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN: {
                mTouchMode = TOUCH_MODE_DOWN;
                mMotionPosition = INVALID_POSITION;
                updateSelectorState();
                invalidate();
                break;
            }
            case MotionEvent.ACTION_DOWN: {
                if (mPendingCheckForTap == null) {
                    mPendingCheckForTap = new CheckForTap();
                }
                postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());

                final int y = (int)event.getY();
                final int x = (int)event.getX();
                mMotionY = y;
                mMotionX = x;
                mMotionPosition = findMotionChildPosition(x, y);

                mTouchMode = TOUCH_MODE_DOWN;
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mMotionPosition != NO_MATCHED_CHILD
                        && (Math.abs(event.getY() - mMotionY) > mTouchSlop || Math.abs(event.getX()
                                - mMotionX) > mTouchSlop)) {
                    // Too much movement to be a tap event.
                    mTouchMode = TOUCH_MODE_AT_REST;
                    final View child = getChildAt(mMotionPosition);
                    if (child != null) {
                        child.setPressed(false);
                    }
                    setPressed(false);
                    final Handler handler = getHandler();
                    if (handler != null) {
                        handler.removeCallbacks(mPendingCheckForLongPress);
                    }
                    mMotionPosition = NO_MATCHED_CHILD;
                    updateSelectorState();
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mTouchMode == TOUCH_MODE_FINISHED_LONG_PRESS) {
                    return true;
                }

                if (mTouchMode == TOUCH_MODE_AT_REST) {
                    break;
                }

                // Handle touch on child.
                if (mMotionPosition == NO_MATCHED_CHILD) {
                    break;
                }

                final View child = getChildAt(mMotionPosition);
                if (child != null && !child.hasFocusable()) {
                    if (mTouchMode != TOUCH_MODE_DOWN) {
                        child.setPressed(false);
                    }

                    if (mPerformPropClick == null) {
                        mPerformPropClick = new PerformClick();
                    }

                    final PerformClick performPropClick = mPerformPropClick;
                    performPropClick.mClickMotionPosition = mMotionPosition;
                    performPropClick.rememberWindowAttachCount();

                    if (mTouchMode != TOUCH_MODE_DOWN || mTouchMode != TOUCH_MODE_TAP) {
                        final Handler handler = getHandler();
                        if (handler != null) {
                            handler.removeCallbacks(mTouchMode == TOUCH_MODE_DOWN ? mPendingCheckForTap
                                    : mPendingCheckForLongPress);
                        }

                        if (!mDataChanged) {
                            // Got here so must be a tap. The long press
                            // would
                            // have triggered inside the delayed runnable.
                            mTouchMode = TOUCH_MODE_TAP;
                            child.setPressed(true);
                            positionSelector(mMotionPosition, child);
                            setPressed(true);
                            updateSelectorState();
                            invalidate();

                            if (mSelector != null) {
                                Drawable d = mSelector.getCurrent();
                                if (d != null && d instanceof TransitionDrawable) {
                                    ((TransitionDrawable)d).resetTransition();
                                }
                            }

                            if (mTouchModeReset != null) {
                                removeCallbacks(mTouchModeReset);
                            }
                            mTouchModeReset = new Runnable() {
                                @Override
                                public void run() {
                                    mTouchMode = TOUCH_MODE_AT_REST;
                                    child.setPressed(false);
                                    setPressed(false);
                                    updateSelectorState();
                                    invalidate();
                                    if (!mDataChanged) {
                                        performPropClick.run();
                                    }
                                }
                            };
                            postDelayed(mTouchModeReset,
                                    ViewConfiguration.getPressedStateDuration());
                        } else {
                            mTouchMode = TOUCH_MODE_AT_REST;
                            updateSelectorState();
                            invalidate();
                        }
                    } else if (!mDataChanged) {
                        performPropClick.run();
                    }
                }
                return true;
            }
        }
        return true;
    }

    public boolean performLongPress(View view, int position, long id) {
        OnItemLongClickListener listener = getOnItemLongClickListener();
        if (listener != null) {
            return doLongPressFeedback(listener.onItemLongClick(this, view, position, id), view);
        }
        return false;
    }

    @Override
    public void setAdapter(StickyHeadersListAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        resetList();

        mAdapter = adapter;

        if (mAdapter != null) {
            mAdapterHasStableIds = mAdapter.hasStableIds();
            if (mChoiceMode != CHOICE_MODE_NONE && mAdapterHasStableIds && mCheckedIdStates == null) {
                mCheckedIdStates = new LongSparseArray<Integer>();
            }
            mItemCount = mAdapter.getCount();

            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }

        requestLayout();
    }

    @Override
    public void setSelection(int position) {
        throw new RuntimeException("Unsupport method: setSelection(int)");
    }

    public void setSelector(Drawable s) {
        if (mSelector != null) {
            mSelector.setCallback(null);
            unscheduleDrawable(mSelector);
        }
        mSelector = s;
        s.setCallback(this);
        updateSelectorState();
    }

    private void dispatchFinishTemporaryDetach(View v) {
        try {
            mDispatchFinishTemporaryDetach.invoke(v);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void dispatchStartTemporaryDetach(View v) {
        try {
            mDispatchStartTemporaryDetach.invoke(v);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send any events and feedback that the long press action has taken place.
     * 
     * @param handled True if the long press has taken place.
     * @param view The view the long press was on.
     * @return Pass through of 'handled' parameter.
     */
    private boolean doLongPressFeedback(final boolean handled, final View view) {
        if (handled) {
            if (view != null) {
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
            }
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        return handled;
    }

    private void drawSelector(Canvas canvas) {
        if (!mSelectorRect.isEmpty()) {
            final Drawable selector = mSelector;
            selector.setBounds(mSelectorRect);
            selector.draw(canvas);
        }
    }

    private int findMotionChildPosition(int x, int y) {
        for (int i = 0; i < getChildCount(); i++) {
            Rect hitRect = new Rect();
            getChildAt(i).getHitRect(hitRect);
            if (hitRect.contains(x, y)) {
                return i;
            }
        }
        return NO_MATCHED_CHILD;
    }

    private void initHiddenMethods() {
        try {
            mDispatchStartTemporaryDetach = View.class.getMethod("dispatchStartTemporaryDetach");
            mDispatchFinishTemporaryDetach = View.class.getMethod("dispatchFinishTemporaryDetach");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void layoutAdapterChildren() {
        int numChildren = mAdapter.getCount();
        for (int i = 0; i < numChildren; i++) {
            // OutBoolean viewAdded = new OutBoolean();
            // View child = obtainView(i, viewAdded);
            // LayoutParams params = (LayoutParams)child.getLayoutParams();
            // int left = params.xPosition;
            // int top = params.yPosition;

            // int childWidthSpec = MeasureSpec.makeMeasureSpec(params.width,
            // MeasureSpec.EXACTLY);
            // int childHeightSpec = MeasureSpec.makeMeasureSpec(params.height,
            // MeasureSpec.EXACTLY);
            // child.measure(childWidthSpec, childHeightSpec);
            //
            // child.layout(left, top, left + child.getMeasuredWidth(),
            // top + child.getMeasuredHeight());

            // if (!viewAdded.value) {
            // addViewInLayout(child, i, params, true);
            // }
        }

        mDataChanged = false;
    }

    private void positionSelector(int l, int t, int r, int b) {
        mSelectorRect.set(l, t, r, b);
    }

    private void positionSelector(int position, View sel) {
        final Rect selectorRect = mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        positionSelector(selectorRect.left, selectorRect.top, selectorRect.right,
                selectorRect.bottom);
        refreshDrawableState();
    }

    private void resetList() {
        removeAllViewsInLayout();
        mDataChanged = false;
        invalidate();
    }

    private boolean shouldShowSelector() {
        return (hasFocus() && !isInTouchMode()) || touchModeDrawsInPressedState();
    }

    private boolean touchModeDrawsInPressedState() {
        switch (mTouchMode) {
            case TOUCH_MODE_TAP:
            case TOUCH_MODE_DONE_WAITING:
                return true;
            default:
                return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateSelectorState() {
        if (mSelector != null) {
            if (shouldShowSelector()) {
                mSelector.setState(getDrawableState());
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mSelector.setState(StateSet.NOTHING);
                } else {
                    mSelector.setState(new int[] {
                        0
                    });
                }
            }
        }
    }

    private void useDefaultSelector() {
        // FIXME: Replace with call to super when sub-classing grid view.
        GridView gv = new GridView(getContext());
        setSelector(gv.getSelector());
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        drawSelector(canvas);

        super.dispatchDraw(canvas);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateSelectorState();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
        }

        layoutAdapterChildren();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mSelector == null) {
            useDefaultSelector();
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    /**
     * Get a view and have it show the data associated with the specified
     * position. This is called when we have already discovered that the view is
     * not available for reuse in the recycle bin. The only choices left are
     * converting an old view or making a new one.
     * 
     * @param position The position to display
     * @param isScrap Array of at least 1 boolean, the first entry will become
     *            true if the returned view was taken from the scrap heap, false
     *            if otherwise.
     * @return A view displaying the data associated with the specified position
     */
    View obtainView(int position, boolean[] isScrap) {
        isScrap[0] = false;
        View scrapView;

        scrapView = mRecycler.getTransientStateView(position);
        if (scrapView != null) {
            return scrapView;
        }

        scrapView = mRecycler.getScrapView(position);

        View child;
        if (scrapView != null) {
            child = mAdapter.getView(position, scrapView, this);

            if (ViewCompat.getImportantForAccessibility(child) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                ViewCompat.setImportantForAccessibility(child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }

            if (child != scrapView) {
                mRecycler.addScrapView(scrapView, position);
                if (mCacheColorHint != 0) {
                    child.setDrawingCacheBackgroundColor(mCacheColorHint);
                }
            } else {
                isScrap[0] = true;
                dispatchFinishTemporaryDetach(child);
            }
        } else {
            child = mAdapter.getView(position, null, this);

            if (ViewCompat.getImportantForAccessibility(child) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                ViewCompat.setImportantForAccessibility(child,
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }

            if (mCacheColorHint != 0) {
                child.setDrawingCacheBackgroundColor(mCacheColorHint);
            }
        }

        if (mAdapterHasStableIds) {
            final ViewGroup.LayoutParams vlp = child.getLayoutParams();
            LayoutParams lp;
            if (vlp == null) {
                lp = (LayoutParams)generateDefaultLayoutParams();
            } else if (!checkLayoutParams(vlp)) {
                lp = (LayoutParams)generateLayoutParams(vlp);
            } else {
                lp = (LayoutParams)vlp;
            }
            lp.itemId = mAdapter.getItemId(position);
            child.setLayoutParams(lp);
        }

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        // AccessibilityManager accessibilityManager =
        // (AccessibilityManager)getContext()
        // .getSystemService(Context.ACCESSIBILITY_SERVICE);
        // if (accessibilityManager.isEnabled()) {
        // if (mAccessibilityDelegate == null) {
        // mAccessibilityDelegate = new ListItemAccessibilityDelegate();
        // }
        // if (child.getAccessibilityDelegate() == null) {
        // child.setAccessibilityDelegate(mAccessibilityDelegate);
        // }
        // }
        // }

        return child;
    }

    // FIXME: Unnecessary when sub-classing GridView.
    public static class LayoutParams extends AdapterView.LayoutParams {
        /**
         * When an AbsListView is measured with an AT_MOST measure spec, it
         * needs to obtain children views to measure itself. When doing so, the
         * children are not attached to the window, but put in the recycler
         * which assumes they've been attached before. Setting this flag will
         * force the reused view to be attached to the window rather than just
         * attached to the parent.
         */
        boolean forceAdd;

        /**
         * The ID the view represents
         */
        long itemId;

        /**
         * When this boolean is set, the view has been added to the AbsListView
         * at least once. It is used to know whether headers/footers have
         * already been added to the list view and whether they should be
         * treated as recycled views or not.
         */
        boolean recycledHeaderFooter;

        /**
         * The position the view was removed from when pulled out of the scrap
         * heap.
         * 
         * @hide
         */
        int scrappedFromPosition;

        /**
         * View type for this view, as returned by
         * {@link android.widget.Adapter#getItemViewType(int) }
         */
        int viewType;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);

            this.viewType = viewType;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataChanged = true;
            mOldItemCount = mItemCount;
            mItemCount = getAdapter().getCount();

            requestLayout();
        }

        @Override
        public void onInvalidated() {
            mDataChanged = true;

            // Data is invalid so we should reset our state
            mOldItemCount = mItemCount;
            mItemCount = 0;

            requestLayout();
        }
    }

    private class CheckForLongPress extends WindowRunnable {
        @Override
        public void run() {
            final View child = getChildAt(mMotionPosition);
            if (child != null) {
                final long longPressId = childViewPositionToId(mMotionPosition);

                boolean handled = false;
                if (sameWindow() && !mDataChanged) {
                    handled = performLongPress(child, mMotionPosition, longPressId);
                }

                if (handled) {
                    mTouchMode = TOUCH_MODE_FINISHED_LONG_PRESS;
                    setPressed(false);
                    child.setPressed(false);
                } else {
                    mTouchMode = TOUCH_MODE_DONE_WAITING;
                }
            }
        }
    }

    protected class CheckForTap extends WindowRunnable {
        @Override
        public void run() {
            if (mTouchMode == TOUCH_MODE_DOWN) {
                mTouchMode = TOUCH_MODE_TAP;

                final View child = getChildAt(mMotionPosition);

                setPressed(true);
                if (child != null && !child.hasFocusable()) {
                    child.setPressed(true);
                    positionSelector(mMotionPosition, child);
                }

                refreshDrawableState();

                final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                final boolean longClickable = isLongClickable();

                if (mSelector != null) {
                    Drawable d = mSelector.getCurrent();
                    if (d != null && d instanceof TransitionDrawable) {
                        if (longClickable) {
                            ((TransitionDrawable)d).startTransition(longPressTimeout);
                        } else {
                            ((TransitionDrawable)d).resetTransition();
                        }
                    }
                }

                if (longClickable) {
                    if (mPendingCheckForLongPress == null) {
                        mPendingCheckForLongPress = new CheckForLongPress();
                    }
                    mPendingCheckForLongPress.rememberWindowAttachCount();
                    postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout());
                } else {
                    mTouchMode = TOUCH_MODE_DONE_WAITING;
                }

                invalidate();
            }
        }
    }

    protected class PerformClick extends WindowRunnable {
        int mClickMotionPosition;

        @Override
        public void run() {
            if (mDataChanged) {
                return;
            }

            final StickyHeadersListAdapter adapter = mAdapter;
            final int motionPosition = mClickMotionPosition;
            if (adapter != null && adapter.getCount() > 0 && motionPosition != INVALID_POSITION
                    && motionPosition < adapter.getCount() && sameWindow()) {
                final View view = getChildAt(motionPosition);
                if (view != null) {
                    performItemClick(view, motionPosition, childViewPositionToId(motionPosition));
                }
            }
        }
    }

    protected abstract class WindowRunnable implements Runnable {
        private int mOriginalAttachCount;

        public void rememberWindowAttachCount() {
            mOriginalAttachCount = getWindowAttachCount();
        }

        public boolean sameWindow() {
            return hasWindowFocus() && getWindowAttachCount() == mOriginalAttachCount;
        }
    }

    /**
     * The RecycleBin facilitates reuse of views across layouts. The RecycleBin
     * has two levels of storage: ActiveViews and ScrapViews. ActiveViews are
     * those views which were onscreen at the start of a layout. By
     * construction, they are displaying current information. At the end of
     * layout, all views in ActiveViews are demoted to ScrapViews. ScrapViews
     * are old views that could potentially be used by the adapter to avoid
     * allocating views unnecessarily.
     * 
     * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
     * @see android.widget.AbsListView.RecyclerListener
     */
    class RecycleBin {
        /**
         * Views that were on screen at the start of layout. This array is
         * populated at the start of layout, and at the end of layout all view
         * in mActiveViews are moved to mScrapViews. Views in mActiveViews
         * represent a contiguous range of Views, with position of the first
         * view store in mFirstActivePosition.
         */
        private View[] mActiveViews = new View[0];

        private ArrayList<View> mCurrentScrap;

        /**
         * The position of the first view stored in mActiveViews.
         */
        private int mFirstActivePosition;

        private RecyclerListener mRecyclerListener;

        /**
         * Unsorted views that can be used by the adapter as a convert view.
         */
        private ArrayList<View>[] mScrapViews;

        private ArrayList<View> mSkippedScrap;

        private SparseArrayCompat<View> mTransientStateViews;

        private LongSparseArray<View> mTransientStateViewsById;

        private int mViewTypeCount;

        public void markChildrenDirty() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).forceLayout();
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(j).forceLayout();
                    }
                }
            }
            if (mTransientStateViews != null) {
                final int count = mTransientStateViews.size();
                for (int i = 0; i < count; i++) {
                    mTransientStateViews.valueAt(i).forceLayout();
                }
            }
            if (mTransientStateViewsById != null) {
                final int count = mTransientStateViewsById.size();
                for (int i = 0; i < count; i++) {
                    mTransientStateViewsById.valueAt(i).forceLayout();
                }
            }
        }

        @SuppressWarnings("unchecked")
        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can't have a viewTypeCount < 1");
            }
            // no inspection unchecked
            ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
            for (int i = 0; i < viewTypeCount; i++) {
                scrapViews[i] = new ArrayList<View>();
            }
            mViewTypeCount = viewTypeCount;
            mCurrentScrap = scrapViews[0];
            mScrapViews = scrapViews;
        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        /**
         * Makes sure that the size of mScrapViews does not exceed the size of
         * mActiveViews. (This can happen if an adapter does not recycle its
         * views).
         */
        private void pruneScrapViews() {
            final int maxViews = mActiveViews.length;
            final int viewTypeCount = mViewTypeCount;
            final ArrayList<View>[] scrapViews = mScrapViews;
            for (int i = 0; i < viewTypeCount; ++i) {
                final ArrayList<View> scrapPile = scrapViews[i];
                int size = scrapPile.size();
                final int extras = size - maxViews;
                size--;
                for (int j = 0; j < extras; j++) {
                    removeDetachedView(scrapPile.remove(size--), false);
                }
            }

            if (mTransientStateViews != null) {
                for (int i = 0; i < mTransientStateViews.size(); i++) {
                    final View v = mTransientStateViews.valueAt(i);
                    if (!ViewCompat.hasTransientState(v)) {
                        mTransientStateViews.removeAt(i);
                        i--;
                    }
                }
            }
            if (mTransientStateViewsById != null) {
                for (int i = 0; i < mTransientStateViewsById.size(); i++) {
                    final View v = mTransientStateViewsById.valueAt(i);
                    if (!ViewCompat.hasTransientState(v)) {
                        mTransientStateViewsById.removeAt(i);
                        i--;
                    }
                }
            }
        }

        /**
         * Put a view into the ScrapViews list. These views are unordered.
         * 
         * @param scrap The view to add
         */
        void addScrapView(View scrap, int position) {
            LayoutParams lp = (LayoutParams)scrap.getLayoutParams();
            if (lp == null) {
                return;
            }

            lp.scrappedFromPosition = position;

            // Don't put header or footer views or views that should be ignored
            // into the scrap heap
            int viewType = lp.viewType;
            final boolean scrapHasTransientState = ViewCompat.hasTransientState(scrap);
            if (!shouldRecycleViewType(viewType) || scrapHasTransientState) {
                if (viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER && scrapHasTransientState) {
                    if (mSkippedScrap == null) {
                        mSkippedScrap = new ArrayList<View>();
                    }
                    mSkippedScrap.add(scrap);
                }
                if (scrapHasTransientState) {
                    dispatchStartTemporaryDetach(scrap);
                    if (mAdapter != null && mAdapterHasStableIds) {
                        if (mTransientStateViewsById == null) {
                            mTransientStateViewsById = new LongSparseArray<View>();
                        }
                        mTransientStateViewsById.put(lp.itemId, scrap);
                    } else if (!mDataChanged) {
                        // avoid putting views on transient state list during a
                        // data change;
                        // the layout positions may be out of sync with the
                        // adapter positions
                        if (mTransientStateViews == null) {
                            mTransientStateViews = new SparseArrayCompat<View>();
                        }
                        mTransientStateViews.put(position, scrap);
                    }
                }
                return;
            }

            dispatchStartTemporaryDetach(scrap);
            if (mViewTypeCount == 1) {
                mCurrentScrap.add(scrap);
            } else {
                mScrapViews[viewType].add(scrap);
            }

            ViewCompat.setAccessibilityDelegate(scrap, null);
            if (mRecyclerListener != null) {
                mRecyclerListener.onMovedToScrapHeap(scrap);
            }
        }

        /**
         * Clears the scrap heap.
         */
        void clear() {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        removeDetachedView(scrap.remove(scrapCount - 1 - j), false);
                    }
                }
            }
            if (mTransientStateViews != null) {
                mTransientStateViews.clear();
            }
            if (mTransientStateViewsById != null) {
                mTransientStateViewsById.clear();
            }
        }

        /**
         * Dump any currently saved views with transient state.
         */
        void clearTransientStateViews() {
            if (mTransientStateViews != null) {
                mTransientStateViews.clear();
            }
            if (mTransientStateViewsById != null) {
                mTransientStateViewsById.clear();
            }
        }

        /**
         * Fill ActiveViews with all of the children of the AbsListView.
         * 
         * @param childCount The minimum number of views mActiveViews should
         *            hold
         * @param firstActivePosition The position of the first view that will
         *            be stored in mActiveViews
         */
        void fillActiveViews(int childCount, int firstActivePosition) {
            if (mActiveViews.length < childCount) {
                mActiveViews = new View[childCount];
            }
            mFirstActivePosition = firstActivePosition;

            final View[] activeViews = mActiveViews;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams)child.getLayoutParams();
                // Don't put header or footer views into the scrap heap
                if (lp != null && lp.viewType != ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
                    // Note: We do place AdapterView.ITEM_VIEW_TYPE_IGNORE in
                    // active views.
                    // However, we will NOT place them into scrap views.
                    activeViews[i] = child;
                }
            }
        }

        /**
         * Get the view corresponding to the specified position. The view will
         * be removed from mActiveViews if it is found.
         * 
         * @param position The position to look up in mActiveViews
         * @return The view if it is found, null otherwise
         */
        View getActiveView(int position) {
            int index = position - mFirstActivePosition;
            final View[] activeViews = mActiveViews;
            if (index >= 0 && index < activeViews.length) {
                final View match = activeViews[index];
                activeViews[index] = null;
                return match;
            }
            return null;
        }

        /**
         * @return A view from the ScrapViews collection. These are unordered.
         */
        View getScrapView(int position) {
            if (mViewTypeCount == 1) {
                return retrieveFromScrap(mCurrentScrap, position);
            } else {
                int whichScrap = mAdapter.getItemViewType(position);
                if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
                    return retrieveFromScrap(mScrapViews[whichScrap], position);
                }
            }
            return null;
        }

        View getTransientStateView(int position) {
            if (mAdapter != null && mAdapterHasStableIds && mTransientStateViewsById != null) {
                long id = mAdapter.getItemId(position);
                View result = mTransientStateViewsById.get(id);
                mTransientStateViewsById.remove(id);
                return result;
            }
            if (mTransientStateViews != null) {
                final int index = mTransientStateViews.indexOfKey(position);
                if (index >= 0) {
                    View result = mTransientStateViews.valueAt(index);
                    mTransientStateViews.removeAt(index);
                    return result;
                }
            }
            return null;
        }

        /**
         * Puts all views in the scrap heap into the supplied list.
         */
        void reclaimScrapViews(List<View> views) {
            if (mViewTypeCount == 1) {
                views.addAll(mCurrentScrap);
            } else {
                final int viewTypeCount = mViewTypeCount;
                final ArrayList<View>[] scrapViews = mScrapViews;
                for (int i = 0; i < viewTypeCount; ++i) {
                    final ArrayList<View> scrapPile = scrapViews[i];
                    views.addAll(scrapPile);
                }
            }
        }

        /**
         * Finish the removal of any views that skipped the scrap heap.
         */
        void removeSkippedScrap() {
            if (mSkippedScrap == null) {
                return;
            }
            final int count = mSkippedScrap.size();
            for (int i = 0; i < count; i++) {
                removeDetachedView(mSkippedScrap.get(i), false);
            }
            mSkippedScrap.clear();
        }

        /**
         * Move all views remaining in mActiveViews to mScrapViews.
         */
        void scrapActiveViews() {
            final View[] activeViews = mActiveViews;
            final boolean hasListener = mRecyclerListener != null;
            final boolean multipleScraps = mViewTypeCount > 1;

            ArrayList<View> scrapViews = mCurrentScrap;
            final int count = activeViews.length;
            for (int i = count - 1; i >= 0; i--) {
                final View victim = activeViews[i];
                if (victim != null) {
                    final LayoutParams lp = (LayoutParams)victim.getLayoutParams();
                    int whichScrap = lp.viewType;

                    activeViews[i] = null;

                    final boolean scrapHasTransientState = ViewCompat.hasTransientState(victim);
                    if (!shouldRecycleViewType(whichScrap) || scrapHasTransientState) {
                        // Do not move views that should be ignored
                        if (whichScrap != ITEM_VIEW_TYPE_HEADER_OR_FOOTER && scrapHasTransientState) {
                            removeDetachedView(victim, false);
                        }
                        if (scrapHasTransientState) {
                            if (mAdapter != null && mAdapterHasStableIds) {
                                if (mTransientStateViewsById == null) {
                                    mTransientStateViewsById = new LongSparseArray<View>();
                                }
                                long id = mAdapter.getItemId(mFirstActivePosition + i);
                                mTransientStateViewsById.put(id, victim);
                            } else {
                                if (mTransientStateViews == null) {
                                    mTransientStateViews = new SparseArrayCompat<View>();
                                }
                                mTransientStateViews.put(mFirstActivePosition + i, victim);
                            }
                        }
                        continue;
                    }

                    if (multipleScraps) {
                        scrapViews = mScrapViews[whichScrap];
                    }

                    dispatchStartTemporaryDetach(victim);
                    lp.scrappedFromPosition = mFirstActivePosition + i;
                    scrapViews.add(victim);

                    ViewCompat.setAccessibilityDelegate(victim, null);
                    if (hasListener) {
                        mRecyclerListener.onMovedToScrapHeap(victim);
                    }
                }
            }

            pruneScrapViews();
        }

        /**
         * Updates the cache color hint of all known views.
         * 
         * @param color The new cache color hint.
         */
        void setCacheColorHint(int color) {
            if (mViewTypeCount == 1) {
                final ArrayList<View> scrap = mCurrentScrap;
                final int scrapCount = scrap.size();
                for (int i = 0; i < scrapCount; i++) {
                    scrap.get(i).setDrawingCacheBackgroundColor(color);
                }
            } else {
                final int typeCount = mViewTypeCount;
                for (int i = 0; i < typeCount; i++) {
                    final ArrayList<View> scrap = mScrapViews[i];
                    final int scrapCount = scrap.size();
                    for (int j = 0; j < scrapCount; j++) {
                        scrap.get(j).setDrawingCacheBackgroundColor(color);
                    }
                }
            }
            // Just in case this is called during a layout pass
            final View[] activeViews = mActiveViews;
            final int count = activeViews.length;
            for (int i = 0; i < count; ++i) {
                final View victim = activeViews[i];
                if (victim != null) {
                    victim.setDrawingCacheBackgroundColor(color);
                }
            }
        }
    }
}

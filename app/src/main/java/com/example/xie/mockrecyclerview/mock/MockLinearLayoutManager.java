package com.example.xie.mockrecyclerview.mock;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MockLinearLayoutManager extends MockRecyclerView.MockLayoutManager {
    private LayoutState mLayoutState;
    private final LayoutChunkResult mLayoutChunkResult = new LayoutChunkResult();
    MockOrientationHelper mOrientationHelper;
    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int VERTICAL = RecyclerView.VERTICAL;
    int mOrientation = MockRecyclerView.DEFAULT_ORIENTATION;
    /**
     * Re-used variable to keep anchor information on re-layout.
     * Anchor position and coordinate defines the reference point for LLM while doing a layout.
     */
    final AnchorInfo mAnchorInfo = new AnchorInfo();

    public MockLinearLayoutManager(Context context) {
        this(context, MockRecyclerView.DEFAULT_ORIENTATION, false);
    }

    public MockLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        setOrientation(orientation);
    }

    public void onLayoutChildren(MockRecyclerView.Recycler recycler, MockRecyclerView.State state) {
        ensureLayoutState();
        mLayoutState.mItemDirection = 1;
        //先将view撤下来
        detachAndScrapAttachedViews(recycler);
        updateLayoutStateToFillEnd(mAnchorInfo);
        this.fill(recycler, this.mLayoutState, state, false);
    }

    private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
        updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
        mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
//        mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mCurrentPosition = itemPosition;
        mLayoutState.mOffset = offset;
//        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
    }

    static class AnchorInfo {
        int mPosition;
        int mCoordinate;
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    private void ensureLayoutState() {
        if (mLayoutState == null) {
            mLayoutState = createLayoutState();
        }
    }

    LayoutState createLayoutState() {
        return new LayoutState();
    }

    int fill(MockRecyclerView.Recycler recycler, LayoutState layoutState, MockRecyclerView.State state, boolean stopOnFocusable) {
        final int start = layoutState.mAvailable;
        int remainingSpace = layoutState.mAvailable + layoutState.mExtra;
        LayoutChunkResult layoutChunkResult = this.mLayoutChunkResult;
        while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
            layoutChunkResult.resetInternal();
            this.layoutChunk(recycler, state, layoutState, layoutChunkResult);
            if (layoutChunkResult.mFinished) {
                break;
            }

            layoutState.mOffset += layoutChunkResult.mConsumed * 1;
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            remainingSpace -= layoutChunkResult.mConsumed;
            layoutState.mScrollingOffset += layoutChunkResult.mConsumed;
        }
        return start - layoutState.mAvailable;
    }

    private void updateLayoutState(int layoutDirection, int requiredSpace,
                                   boolean canUseExistingSpace, MockRecyclerView.State state) {

        int scrollingOffset = 0;
        if (layoutDirection == LayoutState.LAYOUT_END) {
            final View child = getChildClosestToEnd();
            scrollingOffset = mOrientationHelper.getDecoratedEnd(child)
                    - mOrientationHelper.getEndAfterPadding();// 121 = 1353 - 1232  // 118 = 1350 - 1232
        } else {
            final View child = getChildClosestToStart();
            scrollingOffset = -mOrientationHelper.getDecoratedStart(child)
                    + mOrientationHelper.getStartAfterPadding(); //
        }
        mLayoutState.mScrollingOffset = scrollingOffset;
    }

    private View getChildClosestToStart() {
        return getChildAt(0);
    }

    private View getChildClosestToEnd() {
        return getChildAt(getChildCount() - 1);
    }

    void layoutChunk(MockRecyclerView.Recycler recycler, MockRecyclerView.State state, LayoutState layoutState, LayoutChunkResult result) {
        View view = layoutState.next(recycler);
        if (view == null) {
            result.mFinished = true;
        } else {
            MockRecyclerView.LayoutParams params = (MockRecyclerView.LayoutParams) view.getLayoutParams();
            addView(view);
            measureChildWithMargins(view, 0, 0);
            result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
            int left, top, right, bottom;
            left = 0;
            right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
            top = layoutState.mOffset;
            bottom = layoutState.mOffset + result.mConsumed;
            layoutDecoratedWithMargins(view, left, top, right, bottom);
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, MockRecyclerView.Recycler recycler, MockRecyclerView.State state) {
        if (mOrientation == HORIZONTAL) {
            return 0;
        }

        return scrollBy(dy, recycler, state);
    }

    @Override
    public MockRecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new MockRecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    int scrollBy(int dy, MockRecyclerView.Recycler recycler, MockRecyclerView.State state) {
        final int layoutDirection = dy > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        final int absDy = Math.abs(dy);
        updateLayoutState(layoutDirection, absDy, true, state);
        final int consumed = mLayoutState.mScrollingOffset //0 +0 = 0;
                //+ fill(recycler, mLayoutState, state, false)
                ;
        if (consumed < 0) {
            return 0;
        }
        final int scrolled = absDy > consumed ? layoutDirection * consumed : dy;
        mOrientationHelper.offsetChildren(-scrolled);
        return scrolled;
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    public void setOrientation(int orientation) {
        mOrientationHelper =
                MockOrientationHelper.createOrientationHelper(this, orientation);
        mOrientation = orientation;
    }

    protected static class LayoutChunkResult {
        public boolean mFinished;
        public int mConsumed;

        protected LayoutChunkResult() {
        }

        void resetInternal() {
            this.mFinished = false;
        }
    }

    static class LayoutState {
        int mCurrentPosition;
        int mItemDirection;
        public boolean mInfinite = false;
        public int mAvailable;
        int mExtra = 0;
        int mOffset;
        static final int LAYOUT_START = -1;
        static final int LAYOUT_END = 1;
        int mScrollingOffset;
        static final int ITEM_DIRECTION_TAIL = 1;
        static final int SCROLLING_OFFSET_NaN = Integer.MIN_VALUE;

        boolean hasMore(MockRecyclerView.State state) {
            return this.mCurrentPosition >= 0 && this.mCurrentPosition < state.getItemCount();
        }

        View next(MockRecyclerView.Recycler recycler) {
            View view = recycler.getViewForPosition(this.mCurrentPosition);
            this.mCurrentPosition += this.mItemDirection;
            return view;

        }
    }
}

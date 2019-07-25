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
    boolean mShouldReverseLayout = false;
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
        Log.e("xie", "onLayoutChildren");
        this.fill(recycler, this.mLayoutState, state, false);
    }

    private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
        updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
        mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
        mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mCurrentPosition = itemPosition;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
        mLayoutState.mOffset = offset;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
    }

    public int getPaddingBottom() {
        return mRecyclerView != null ? mRecyclerView.getPaddingBottom() : 0;
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
        if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
            // TODO ugly bug fix. should not happen
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState);
        }
        int remainingSpace = layoutState.mAvailable + layoutState.mExtra;
        Log.e("xie", "layoutState.mAvailable:" + layoutState.mAvailable);
        Log.e("xie", "layoutState.mExtra:" + layoutState.mExtra);
        LayoutChunkResult layoutChunkResult = this.mLayoutChunkResult;
        Log.e("xie", "layoutChunk-----------start: " + remainingSpace);
        while ((layoutState.mInfinite || remainingSpace > 0) && layoutState.hasMore(state)) {
            layoutChunkResult.resetInternal();
            this.layoutChunk(recycler, state, layoutState, layoutChunkResult);
            if (layoutChunkResult.mFinished) {
                break;
            }

            layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
            layoutState.mAvailable -= layoutChunkResult.mConsumed;
            remainingSpace -= layoutChunkResult.mConsumed;

            if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
                layoutState.mScrollingOffset += layoutChunkResult.mConsumed;
                if (layoutState.mAvailable < 0) {
                    layoutState.mScrollingOffset += layoutState.mAvailable;
                }
                recycleByLayoutState(recycler, layoutState);
            }
        }
        Log.e("xie", "layoutChunk-----------end");
        return start - layoutState.mAvailable;
    }

    private void recycleByLayoutState(MockRecyclerView.Recycler recycler, LayoutState layoutState) {
        if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
            recycleViewsFromEnd(recycler, layoutState.mScrollingOffset);
        } else {
            recycleViewsFromStart(recycler, layoutState.mScrollingOffset);
        }
    }

    private void recycleViewsFromEnd(MockRecyclerView.Recycler recycler, int dt) {
        final int childCount = getChildCount();
        final int limit = mOrientationHelper.getEnd() - dt;
//        if (mShouldReverseLayout) {
//            for (int i = 0; i < childCount; i++) {
//                View child = getChildAt(i);
//                if (mOrientationHelper.getDecoratedStart(child) < limit) {
//                    // stop here
//                    recycleChildren(recycler, 0, i);
//                    return;
//                }
//            }
//        } else {
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (mOrientationHelper.getDecoratedStart(child) < limit) {
                // stop here
                Log.e("xie", "End recycleChildren:" + (childCount - 1) + "-" + i);
                recycleChildren(recycler, childCount - 1, i);
                return;
            }
        }
//        }
    }

    private void recycleViewsFromStart(MockRecyclerView.Recycler recycler, int dt) {
        final int limit = dt;
        final int childCount = getChildCount();
//        if (mShouldReverseLayout) {
//            for (int i = childCount - 1; i >= 0; i--) {
//                View child = getChildAt(i);
//                if (mOrientationHelper.getDecoratedEnd(child) > limit) {
//                    // stop here
//                    recycleChildren(recycler, childCount - 1, i);
//                    return;
//                }
//            }
//        } else {
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (mOrientationHelper.getDecoratedEnd(child) > limit) {
                // stop here  Start recycleChildren:0-9
                Log.e("xie", "Start recycleChildren:" + 0 + "-" + i);
                recycleChildren(recycler, 0, i);
                return;
            }
        }
//        }
    }

    private void recycleChildren(MockRecyclerView.Recycler recycler, int startIndex, int endIndex) {
        if (startIndex == endIndex) {
            return;
        }
        if (endIndex > startIndex) {
            for (int i = endIndex - 1; i >= startIndex; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
        } else {
            for (int i = startIndex; i > endIndex; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
        }
    }

    public void removeAndRecycleViewAt(int index, MockRecyclerView.Recycler recycler) {
        final View view = getChildAt(index);
        removeViewAt(index);
        recycler.recycleView(view);
    }

    private void updateLayoutState(int layoutDirection, int requiredSpace,
                                   boolean canUseExistingSpace, MockRecyclerView.State state) {
        mLayoutState.mInfinite = resolveIsInfinite();
        mLayoutState.mExtra = getExtraLayoutSpace(state);
        mLayoutState.mLayoutDirection = layoutDirection;
        int scrollingOffset;
        if (layoutDirection == LayoutState.LAYOUT_END) {
            mLayoutState.mExtra += mOrientationHelper.getEndPadding();
            final View child = getChildClosestToEnd();
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
            mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
            mLayoutState.mOffset = mOrientationHelper.getDecoratedEnd(child);
            scrollingOffset = mOrientationHelper.getDecoratedEnd(child)
                    - mOrientationHelper.getEndAfterPadding();// 121 = 1353 - 1232  // 118 = 1350 - 1232
        } else {
            final View child = getChildClosestToStart();
            mLayoutState.mExtra += mOrientationHelper.getStartAfterPadding();
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_HEAD;
            mLayoutState.mCurrentPosition = getPosition(child) + mLayoutState.mItemDirection;
            scrollingOffset = -mOrientationHelper.getDecoratedStart(child)
                    + mOrientationHelper.getStartAfterPadding(); //
            Log.e("xie", "para 1:" + mOrientationHelper.getDecoratedStart(child));
            Log.e("xie", "para 2:" + mOrientationHelper.getStartAfterPadding());

            for (int i = 0; i < getChildCount(); i++) {
                Log.e("xie", "tag:" + getChildAt(i).getTag());
                Log.e("xie", i + " _ decoratedStart:" +
                        mOrientationHelper.getDecoratedStart(getChildAt(i)));
            }
        }
        mLayoutState.mAvailable = requiredSpace; // 17
        if (canUseExistingSpace) {
            mLayoutState.mAvailable -= scrollingOffset;
        }
        Log.e("xie", "scrollingOffset:" + scrollingOffset);
        mLayoutState.mScrollingOffset = scrollingOffset; // 0
    }

    public int getPosition(@NonNull View view) {
        return ((MockRecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
    }

    protected int getExtraLayoutSpace(MockRecyclerView.State state) {
        if (state.hasTargetScrollPosition()) {
            return mOrientationHelper.getTotalSpace();
        } else {
            return 0;
        }
    }

    private boolean resolveIsInfinite() {
        return mOrientationHelper.getEnd() == 0;
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
            if (mShouldReverseLayout == (layoutState.mLayoutDirection
                    == LayoutState.LAYOUT_START)) {
                addView(view);
            } else {
                addView(view, 0);
            }
            measureChildWithMargins(view, 0, 0);
            result.mConsumed = mOrientationHelper.getDecoratedMeasurement(view);
            int left, top, right, bottom;
            if (layoutState.mLayoutDirection == LayoutState.LAYOUT_START) {
                bottom = layoutState.mOffset;
                top = layoutState.mOffset - result.mConsumed;
            } else {
                top = layoutState.mOffset;
                bottom = layoutState.mOffset + result.mConsumed;
            }
            Log.e("xie", "top:" + top);
            left = 0;
            right = left + mOrientationHelper.getDecoratedMeasurementInOther(view);
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
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        mLayoutState.mRecycle = true;
        ensureLayoutState();
        final int layoutDirection = dy > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        final int absDy = Math.abs(dy);
        updateLayoutState(layoutDirection, absDy, true, state);
        final int consumed = mLayoutState.mScrollingOffset //0 +0 = 0;
                + fill(recycler, mLayoutState, state, false);//consumed:0 mLayoutState.mScrollingOffset:0 dy:17 scrolled:0
        //consumed:-986 mLayoutState.mScrollingOffset:-986 dy:-30
        //consumed:43 mLayoutState.mScrollingOffset:43 dy:2 scrolled:2
        Log.e("xie", "consumed:" + consumed);
        Log.e("xie", "mLayoutState.mScrollingOffset:" + mLayoutState.mScrollingOffset);
        Log.e("xie", "dy:" + dy);
        if (consumed < 0) {
            return 0;
        }
        final int scrolled = absDy > consumed ? layoutDirection * consumed : dy;
        Log.e("xie", "scrolled:" + scrolled);

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
        public boolean mRecycle;
        int mCurrentPosition;
        int mItemDirection;
        public boolean mInfinite = false;
        public int mAvailable;
        int mExtra = 0;
        int mOffset;
        static final int LAYOUT_START = -1;
        static final int LAYOUT_END = 1;
        int mScrollingOffset;
        static final int ITEM_DIRECTION_HEAD = -1;

        static final int ITEM_DIRECTION_TAIL = 1;

        static final int SCROLLING_OFFSET_NaN = Integer.MIN_VALUE;
        int mLayoutDirection;

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

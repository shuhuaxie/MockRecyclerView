package com.example.xie.mockrecyclerview.mock;

import android.content.Context;
import android.database.Observable;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static androidx.core.view.ViewCompat.TYPE_TOUCH;

public class MockRecyclerView extends ViewGroup {
    private MockLayoutManager mLayout;
    private Adapter mAdapter;
    private MockChildHelper mChildHelper;
    private Recycler mRecycler = new Recycler();
    private State mState = new State();
    public static final int VERTICAL = LinearLayout.VERTICAL;
    static final boolean ALLOW_SIZE_IN_UNSPECIFIED_SPEC = Build.VERSION.SDK_INT >= 23;
    static final int DEFAULT_ORIENTATION = VERTICAL;
    final Rect mTempRect = new Rect();
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastTouchX;
    private int mLastTouchY;
    private final int[] mScrollStepConsumed = new int[2];
    private NestedScrollingChildHelper mScrollingChildHelper;
    private final int[] mScrollOffset = new int[2];
    final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];
    private int mScrollState = SCROLL_STATE_IDLE;

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int NO_POSITION = -1;
    private int mTouchSlop;
    private int mInterceptRequestLayoutDepth = 0;

    public MockRecyclerView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MockRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MockRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initChildrenHelper();
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.dispatchLayout();
    }

    private final RecyclerViewDataObserver mObserver = new RecyclerViewDataObserver();

    @Override
    public void requestLayout() {
        if (mInterceptRequestLayoutDepth == 0 ) {
            super.requestLayout();
        } else {
//            mLayoutWasDefered = true;
        }
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (mLayout.isAutoMeasureEnabled()) {
            final int widthMode = MeasureSpec.getMode(widthSpec);
            final int heightMode = MeasureSpec.getMode(heightSpec);
            mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);

            final boolean measureSpecModeIsExactly =
                    widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY;
            dispatchLayoutStep1();
            mLayout.setMeasureSpecs(widthSpec, heightSpec);
            dispatchLayoutStep2();
            mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        onTouchEvent(e);
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final boolean canScrollVertically = mLayout.canScrollVertically();
        final int action = e.getActionMasked();
        final MotionEvent vtev = MotionEvent.obtain(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollState = SCROLL_STATE_IDLE;
                mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);
            }
            case MotionEvent.ACTION_MOVE: {
                final int x = (int) (e.getX() + 0.5f);
                final int y = (int) (e.getY() + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;

                mLastTouchX = x - mScrollOffset[0];
                mLastTouchY = y - mScrollOffset[1];
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;
                    if (canScrollVertically && Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (startScroll) {
                        mScrollState = SCROLL_STATE_DRAGGING;
                    }
                }
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    if (scrollByInternal(
                            0,
                            canScrollVertically ? dy : 0,
                            vtev)) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:


        }
        return true;
    }


    boolean scrollByInternal(int x, int y, MotionEvent ev) {
        int consumedX = 0, consumedY = 0;
        if (mAdapter != null) {
            scrollStep(x, y, mScrollStepConsumed);
            consumedX = mScrollStepConsumed[0];
            consumedY = mScrollStepConsumed[1];
        }
        return consumedX != 0 || consumedY != 0;
    }

    void scrollStep(int dx, int dy, @Nullable int[] consumed) {
        startInterceptRequestLayout();
        int consumedX = 0;
        int consumedY = 0;
        if (dy != 0) {
            consumedY = mLayout.scrollVerticallyBy(dy, mRecycler, mState);
        }
        stopInterceptRequestLayout(false);
        if (consumed != null) {
            consumed[0] = consumedX;
            consumed[1] = consumedY;
        }
    }

    private void stopInterceptRequestLayout(boolean performLayoutChildren) {
        mInterceptRequestLayoutDepth--;
    }

    void startInterceptRequestLayout() {
        mInterceptRequestLayoutDepth++;
//        if (mInterceptRequestLayoutDepth == 1 && !mLayoutFrozen) {
//            mLayoutWasDefered = false;
//        }
    }
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow,
                                           int type) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow,
                type);
    }

    private void dispatchLayoutStep1() {
        mState.mItemCount = mAdapter.getItemCount();
    }

    void defaultOnMeasure(int widthSpec, int heightSpec) {
        // calling LayoutManager here is not pretty but that API is already public and it is better
        // than creating another method since this is internal.
        final int width = MockLayoutManager.chooseSize(widthSpec,
                getPaddingLeft() + getPaddingRight(),
                ViewCompat.getMinimumWidth(this));
        final int height = MockLayoutManager.chooseSize(heightSpec,
                getPaddingTop() + getPaddingBottom(),
                ViewCompat.getMinimumHeight(this));

        setMeasuredDimension(width, height);
    }

    private void dispatchLayout() {
        dispatchLayoutStep2();
    }

    private void dispatchLayoutStep2() {
        this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void setAdapter(Adapter adapter) {
//        mAdapter = adapter;
        setAdapterInternal(adapter, false, true);
        requestLayout();
    }

    private void setAdapterInternal(@Nullable MockRecyclerView.Adapter adapter, boolean compatibleWithPrevious,
                                    boolean removeAndRecycleViews) {
        mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
        }
    }

    public void setLayoutManager(MockLinearLayoutManager layoutManager) {
        mLayout = layoutManager;
        this.mLayout.setRecyclerView(this);
    }

    Rect getItemDecorInsetsForChild(View child) {
//        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        final Rect insets = new Rect();//lp.mDecorInsets;
        insets.set(0, 0, 0, 0);
        return insets;
    }

    private void initChildrenHelper() {
        this.mChildHelper = new MockChildHelper(new MockChildHelper.Callback() {
            @Override
            public int getChildCount() {
                return MockRecyclerView.this.getChildCount();
            }

            @Override
            public void addView(View child, int index) {
                MockRecyclerView.this.addView(child, index);
            }

            @Override
            public int indexOfChild(View view) {
                return MockRecyclerView.this.indexOfChild(view);
            }


            @Override
            public void removeViewAt(int index) {
//                final View child = MockRecyclerView.this.getChildAt(index);
                MockRecyclerView.this.removeViewAt(index);
            }

            @Override
            public View getChildAt(int offset) {
                return MockRecyclerView.this.getChildAt(offset);
            }

            @Override
            public void removeAllViews() {

            }
        });
    }

    private void offsetChildrenVertical(int dy) {
        final int childCount = mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            mChildHelper.getChildAt(i).offsetTopAndBottom(dy);
        }
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public abstract static class Adapter<VH extends MockRecyclerView.ViewHolder> {
        private final MockRecyclerView.AdapterDataObservable mObservable =
                new MockRecyclerView.AdapterDataObservable();

        @NonNull
        public abstract VH onCreateViewHolder(@NonNull ViewGroup var1, int var2);

        @NonNull
        public final VH createViewHolder(@NonNull ViewGroup parent, int viewType) {

            try {
                VH holder = this.onCreateViewHolder(parent, viewType);

                return holder;
            } finally {
            }
        }

        public abstract void onBindViewHolder(@NonNull VH var1, int var2);


        public int getItemViewType(int position) {
            return 0;
        }

        public abstract int getItemCount();

        public void bindViewHolder(VH holder, int position) {
            holder.mPosition = position;
            onBindViewHolder(holder, position);
        }

        public final void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }

        public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
            mObservable.registerObserver(observer);
        }
    }

    public static class State {
        int mItemCount = 0;
        boolean mInPreLayout = false;
        /**
         * Owned by SmoothScroller
         */
        private int mTargetPosition = RecyclerView.NO_POSITION;

        public int getItemCount() {
            return mItemCount;
        }

        public boolean isPreLayout() {
            return mInPreLayout;
        }

        public boolean hasTargetScrollPosition() {
            return mTargetPosition != RecyclerView.NO_POSITION;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        if (mLayout == null) {
            throw new IllegalStateException("RecyclerView has no LayoutManager" + exceptionLabel());
        }
        return mLayout.generateDefaultLayoutParams();
    }

    public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;

        final ArrayList<ViewHolder> mCachedViews = new ArrayList<>();

        @NonNull
        public View getViewForPosition(int position) {
            return this.getViewForPosition(position, false);
        }

        View getViewForPosition(int position, boolean dryRun) {
            return this.tryGetViewHolderForPositionByDeadline(position, dryRun, 9223372036854775807L).itemView;
        }

        @Nullable
        ViewHolder tryGetViewHolderForPositionByDeadline(int position, boolean dryRun, long deadlineNs) {
            if (position >= 0 && position < MockRecyclerView.this.mState.getItemCount()) {
                int type;
                int offsetPosition = 0;
                ViewHolder holder = null;
                // 0) If there is a changed scrap, try to find from there
                // 动画预加载View,暂时忽略
                if (mState.isPreLayout()) {
//                    holder = getChangedScrapViewForPosition(position);
//                    fromScrapOrHiddenOrCache = holder != null;
                }
                if (holder == null) {
                    holder = getScrapOrHiddenOrCachedHolderForPosition(position, dryRun);
                    if (holder != null) {

                    }
                }

                if (holder == null) {
                    type = MockRecyclerView.this.mAdapter.getItemViewType(offsetPosition);
                    holder = MockRecyclerView.this.mAdapter.createViewHolder(MockRecyclerView.this, type);
                }
                if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
                    tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
                }

                final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                final LayoutParams rvLayoutParams;
                if (lp == null) {
                    rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
                    holder.itemView.setLayoutParams(rvLayoutParams);
                } else if (!checkLayoutParams(lp)) {
                    rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
                    holder.itemView.setLayoutParams(rvLayoutParams);
                } else {
                    rvLayoutParams = (LayoutParams) lp;
                }
                rvLayoutParams.mViewHolder = holder;
                return holder;
            } else {
                throw new IndexOutOfBoundsException("Invalid item position " + position + "(" + position + "). Item count:" +
                        MockRecyclerView.this.mState.getItemCount() + MockRecyclerView.this.exceptionLabel());
            }

        }

        ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int position, boolean dryRun) {
            final int scrapCount = mAttachedScrap.size();

            // Try first for an exact, non-invalid match from scrap.
            for (int i = 0; i < scrapCount; i++) {
                final ViewHolder holder = mAttachedScrap.get(i);
                if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position
                        && !holder.isInvalid() && (mState.mInPreLayout || !holder.isRemoved())) {
                    holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                    return holder;
                }
            }
            final int cacheSize = mCachedViews.size();
            for (int i = 0; i < cacheSize; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                // invalid view holders may be in cache if adapter has stable ids as they can be
                // retrieved via getScrapOrCachedViewForId
                if (!holder.isInvalid() && holder.getLayoutPosition() == position) {
                    if (!dryRun) {
                        mCachedViews.remove(i);
                    }
                    return holder;
                }
            }
            return null;
        }

        private void tryBindViewHolderByDeadline(ViewHolder holder, int offsetPosition, int position, long deadlineNs) {
            mAdapter.bindViewHolder(holder, position);
        }

        public void unscrapView(ViewHolder holder) {
            if (holder.mInChangeScrap) {
                mChangedScrap.remove(holder);
            } else {
                mAttachedScrap.remove(holder);
            }
            holder.mScrapContainer = null;
            holder.mInChangeScrap = false;
            holder.clearReturnedFromScrapFlag();
        }

        public void scrapView(View view) {
            final ViewHolder holder = getChildViewHolderInt(view);
            if (holder.hasAnyOfTheFlags(ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_INVALID)
                    || !holder.isUpdated()) {
                if (holder.isInvalid() && !holder.isRemoved()) {
                    throw new IllegalArgumentException("Called scrap view with an invalid view."
                            + " Invalid views cannot be reused from scrap, they should rebound from"
                            + " recycler pool." + exceptionLabel());
                }
                holder.setScrapContainer(this, false);
                mAttachedScrap.add(holder);
            } else {
                if (mChangedScrap == null) {
                    mChangedScrap = new ArrayList<ViewHolder>();
                }
                holder.setScrapContainer(this, true);
                mChangedScrap.add(holder);
            }
        }

        void recycleViewHolderInternal(ViewHolder holder) {
            int cachedViewSize = mCachedViews.size();
            int targetCacheIndex = cachedViewSize;
            mCachedViews.add(targetCacheIndex, holder);
        }

        public void recycleView(@NonNull View view) {
            // This public recycle method tries to make view recycle-able since layout manager
            // intended to recycle this view (e.g. even if it is in scrap or change cache)
            ViewHolder holder = getChildViewHolderInt(view);
            if (holder.isScrap()) {
                holder.unScrap();
            } else if (holder.wasReturnedFromScrap()) {
                holder.clearReturnedFromScrapFlag();
            }
            recycleViewHolderInternal(holder);
        }

        public void markKnownViewsInvalid() {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder != null) {
                    holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
//                    holder.addChangePayload(null);
                }
            }

//            if (mAdapter == null) {
//                // we cannot re-use cached views in this case. Recycle them all
//                recycleAndClearCachedViews();
//            }
        }
    }

    static ViewHolder getChildViewHolderInt(View child) {
        if (child == null) {
            return null;
        }
        return ((LayoutParams) child.getLayoutParams()).mViewHolder;
    }

    String exceptionLabel() {
        return " " + super.toString() + ", adapter:" + this.mAdapter + ", layout:" + this.mLayout + ", context:" + this.getContext();
    }

    public abstract static class ViewHolder {
        static final int FLAG_RETURNED_FROM_SCRAP = 1 << 5;
        @NonNull
        public final View itemView;
        Recycler mScrapContainer = null;
        int mFlags;
        boolean mInChangeScrap = false;
        static final int FLAG_BOUND = 1 << 0;
        static final int FLAG_UPDATE = 1 << 1;
        static final int FLAG_INVALID = 1 << 2;
        static final int FLAG_REMOVED = 1 << 3;
        int mPosition = NO_POSITION;

        boolean isScrap() {
            return mScrapContainer != null;
        }

        void unScrap() {
            mScrapContainer.unscrapView(this);
        }

        boolean wasReturnedFromScrap() {
            return (mFlags & FLAG_RETURNED_FROM_SCRAP) != 0;
        }

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }

        void clearReturnedFromScrapFlag() {
            mFlags = mFlags & ~FLAG_RETURNED_FROM_SCRAP;
        }

        void setScrapContainer(Recycler recycler, boolean isChangeScrap) {
            mScrapContainer = recycler;
            mInChangeScrap = isChangeScrap;
        }

        public boolean isInvalid() {
            return (mFlags & FLAG_INVALID) != 0;
        }

        boolean needsUpdate() {
            return (mFlags & FLAG_UPDATE) != 0;
        }

        boolean isBound() {
            return (mFlags & FLAG_BOUND) != 0;
        }

        boolean isRemoved() {
            return (mFlags & FLAG_REMOVED) != 0;
        }

        boolean hasAnyOfTheFlags(int flags) {
            return (mFlags & flags) != 0;
        }

        boolean isUpdated() {
            return (mFlags & FLAG_UPDATE) != 0;
        }

        public int getLayoutPosition() {
            return mPosition;
        }

        void addFlags(int flags) {
            mFlags |= flags;
        }
    }

    public abstract static class MockLayoutManager {
        MockChildHelper mChildHelper;
        MockRecyclerView mRecyclerView;
        private int mWidthMode, mHeightMode;
        private int mWidth, mHeight;
        boolean mAutoMeasure = false;

        public void addView(View child) {
            addView(child, -1);
        }

        public void addView(View child, int index) {
            addViewInt(child, index, false);
        }

        private void addViewInt(View child, int index, boolean disappearing) {
            final ViewHolder holder = getChildViewHolderInt(child);
            if (holder.wasReturnedFromScrap() || holder.isScrap()) {
                if (holder.isScrap()) {
                    holder.unScrap();
                }
            } else if (child.getParent() == mRecyclerView) { // it was not a scrap but a valid child
                // ensure in correct position
//                int currentIndex = mChildHelper.indexOfChild(child);
//                if (index == -1) {
//                    index = mChildHelper.getChildCount();
//                }
//                if (currentIndex == -1) {
//                    throw new IllegalStateException("Added View has RecyclerView as parent but"
//                            + " view is not a real child. Unfiltered index:"
//                            + mRecyclerView.indexOfChild(child) + mRecyclerView.exceptionLabel());
//                }
//                if (currentIndex != index) {
//                    mRecyclerView.mLayout.moveView(currentIndex, index);
//                }
            } else {
                mChildHelper.addView(child, index, false);
            }
        }

        static ViewHolder getChildViewHolderInt(View child) {
            if (child == null) {
                return null;
            }
            return ((LayoutParams) child.getLayoutParams()).mViewHolder;
        }

        public void measureChildWithMargins(@NonNull View child, int widthUsed, int heightUsed) {
            final ViewGroup.LayoutParams lp = child.getLayoutParams();

            final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            widthUsed += insets.left + insets.right;
            heightUsed += insets.top + insets.bottom;
            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(),
//                    getPaddingLeft() + getPaddingRight()
//                    +lp.leftMargin + lp.rightMargin +
                    widthUsed, lp.width,
                    canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(),
//                    getPaddingTop() + getPaddingBottom()
//                    +lp.topMargin + lp.bottomMargin
                    +heightUsed, lp.height,
                    canScrollVertically());
            if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
                child.measure(widthSpec, heightSpec);
            }
        }

        boolean shouldMeasureChild(View child, int widthSpec, int heightSpec, ViewGroup.LayoutParams lp) {
            return true;
        }

        public static int getChildMeasureSpec(int parentSize, int parentMode, int padding,
                                              int childDimension, boolean canScroll) {
            int size = Math.max(0, parentSize - padding);
            int resultSize = 0;
            int resultMode = 0;
            if (canScroll) {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    switch (parentMode) {
                        case MeasureSpec.AT_MOST:
                        case MeasureSpec.EXACTLY:
                            resultSize = size;
                            resultMode = parentMode;
                            break;
                        case MeasureSpec.UNSPECIFIED:
                            resultSize = 0;
                            resultMode = MeasureSpec.UNSPECIFIED;
                            break;
                    }
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
            } else {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    resultMode = parentMode;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    if (parentMode == MeasureSpec.AT_MOST || parentMode == MeasureSpec.EXACTLY) {
                        resultMode = MeasureSpec.AT_MOST;
                    } else {
                        resultMode = MeasureSpec.UNSPECIFIED;
                    }

                }
            }
            //noinspection WrongConstant
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }

        public boolean canScrollHorizontally() {
            return false;
        }

        /**
         * Query if vertical scrolling is currently supported. The default implementation
         * returns false.
         *
         * @return True if this LayoutManager can scroll the current contents vertically
         */
        public boolean canScrollVertically() {
            return false;
        }

        public int getWidthMode() {
            return mWidthMode;
        }

        public int getHeightMode() {
            return mHeightMode;
        }

        @Px
        public int getWidth() {
            return mWidth;
        }

        @Px
        public int getHeight() {
            return mHeight;
        }

        public void onMeasure(@NonNull Recycler recycler, @NonNull State state, int widthSpec,
                              int heightSpec) {
            mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
        }

        void setRecyclerView(MockRecyclerView recyclerView) {
            this.mRecyclerView = recyclerView;
            mChildHelper = recyclerView.mChildHelper;
            mWidth = recyclerView.getWidth();
            mHeight = recyclerView.getHeight();
            mWidthMode = MeasureSpec.EXACTLY;
            mHeightMode = MeasureSpec.EXACTLY;
        }

        public void onLayoutChildren(MockRecyclerView.Recycler recycler, MockRecyclerView.State state) {
            Log.e("RecyclerView", "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        public int getDecoratedMeasuredWidth(@NonNull View child) {
            final Rect insets = new Rect(); //((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return child.getMeasuredWidth() + insets.left + insets.right;
        }

        public int getDecoratedMeasuredHeight(@NonNull View child) {
            final Rect insets = new Rect(); //((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return child.getMeasuredHeight() + insets.top + insets.bottom;
        }

        public void layoutDecoratedWithMargins(@NonNull View child, int left, int top, int right,
                                               int bottom) {
            final ViewGroup.LayoutParams lp = child.getLayoutParams();
            final Rect insets = new Rect(); //lp.mDecorInsets;
            child.layout(left + insets.left
//                            + lp.leftMargin
                    , top + insets.top
//                            + lp.topMargin
                    ,
                    right - insets.right
//                            - lp.rightMargin
                    ,
                    bottom - insets.bottom
//                            - lp.bottomMargin
            );
        }

        public boolean isAutoMeasureEnabled() {
            return mAutoMeasure;
        }

        void setMeasuredDimensionFromChildren(int widthSpec, int heightSpec) {
            final int count = getChildCount();
            if (count == 0) {
                mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
                return;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                final Rect bounds = mRecyclerView.mTempRect;
                getDecoratedBoundsWithMargins(child, bounds);
                if (bounds.left < minX) {
                    minX = bounds.left;
                }
                if (bounds.right > maxX) {
                    maxX = bounds.right;
                }
                if (bounds.top < minY) {
                    minY = bounds.top;
                }
                if (bounds.bottom > maxY) {
                    maxY = bounds.bottom;
                }
            }
            mRecyclerView.mTempRect.set(minX, minY, maxX, maxY);
            setMeasuredDimension(mRecyclerView.mTempRect, widthSpec, heightSpec);
        }

        public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
            int usedWidth = childrenBounds.width() + 0 + 0;
            int usedHeight = childrenBounds.height() + 0 + 0;
            int width = chooseSize(wSpec, usedWidth, 0);
            int height = chooseSize(hSpec, usedHeight, 0);
            setMeasuredDimension(width, height);
        }

        public void setMeasuredDimension(int widthSize, int heightSize) {
            mRecyclerView.setMeasuredDimension(widthSize, heightSize);
        }

        public static int chooseSize(int spec, int desired, int min) {
            final int mode = View.MeasureSpec.getMode(spec);
            final int size = View.MeasureSpec.getSize(spec);
            switch (mode) {
                case View.MeasureSpec.EXACTLY:
                    return size;
                case View.MeasureSpec.AT_MOST:
                    return Math.min(size, Math.max(desired, min));
                case View.MeasureSpec.UNSPECIFIED:
                default:
                    return Math.max(desired, min);
            }
        }

        public int getChildCount() {
            return mChildHelper != null ? mChildHelper.getChildCount() : 0;
        }

        public View getChildAt(int index) {
            return mChildHelper != null ? mChildHelper.getChildAt(index) : null;
        }

        public void getDecoratedBoundsWithMargins(@NonNull View view, @NonNull Rect outBounds) {
            MockRecyclerView.getDecoratedBoundsWithMarginsInt(view, outBounds);
        }

        void setMeasureSpecs(int wSpec, int hSpec) {
            mWidth = MeasureSpec.getSize(wSpec);
            mWidthMode = MeasureSpec.getMode(wSpec);
            if (mWidthMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mWidth = 0;
            }

            mHeight = MeasureSpec.getSize(hSpec);
            mHeightMode = MeasureSpec.getMode(hSpec);
            if (mHeightMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mHeight = 0;
            }
        }

        public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
            return 0;
        }

        public void offsetChildrenVertical(@Px int dy) {
            if (mRecyclerView != null) {
                mRecyclerView.offsetChildrenVertical(dy);
            }
        }

        public int getDecoratedBottom(@NonNull View child) {
            return child.getBottom();// + getBottomDecorationHeight(child);
        }

        public int getDecoratedTop(@NonNull View child) {
            return child.getTop();// - getTopDecorationHeight(child);
        }

        public int getPaddingTop() {
            return mRecyclerView != null ? mRecyclerView.getPaddingTop() : 0;
        }

        public void detachAndScrapAttachedViews(@NonNull Recycler recycler) {
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View v = getChildAt(i);
                scrapOrRecycleView(recycler, i, v);
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int index, View view) {
            final ViewHolder viewHolder = getChildViewHolderInt(view);

            if (viewHolder.isInvalid() && !viewHolder.isRemoved()) {
                removeViewAt(index);
                recycler.recycleViewHolderInternal(viewHolder);
            } else {
                recycler.scrapView(view);
            }
        }

        public void removeViewAt(int index) {
            final View child = getChildAt(index);
            if (child != null) {
                mChildHelper.removeViewAt(index);
            }
        }

        public abstract LayoutParams generateDefaultLayoutParams();
    }


    private static void getDecoratedBoundsWithMarginsInt(View view, Rect outBounds) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        final Rect insets = new Rect();//lp.mDecorInsets;
        outBounds.set(view.getLeft() - insets.left
//                        - lp.leftMargin
                ,
                view.getTop() - insets.top
//                        - lp.topMargin
                ,
                view.getRight() + insets.right
//                        + lp.rightMargin
                ,
                view.getBottom() + insets.bottom
//                        + lp.bottomMargin
        )
        ;
    }

    public static class LayoutParams extends android.view.ViewGroup.MarginLayoutParams {
        final Rect mDecorInsets = new Rect();
        public ViewHolder mViewHolder;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public int getViewLayoutPosition() {
            return mViewHolder.getLayoutPosition();
        }
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mScrollingChildHelper == null) {
            mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mScrollingChildHelper;
    }

    static class AdapterDataObservable extends Observable<RecyclerView.AdapterDataObserver> {

        public void notifyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }
    }

    private class RecyclerViewDataObserver extends RecyclerView.AdapterDataObserver {
        RecyclerViewDataObserver() {
        }

        @Override
        public void onChanged() {
            processDataSetCompletelyChanged(true);
            requestLayout();
        }
        void processDataSetCompletelyChanged(boolean dispatchItemsChanged) {
            markKnownViewsInvalid();
        }
        void markKnownViewsInvalid() {
            final int childCount = mChildHelper.getUnfilteredChildCount();
            for (int i = 0; i < childCount; i++) {
                final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
                if (holder != null ) {
                    holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
                }
            }
//            markItemDecorInsetsDirty();
            mRecycler.markKnownViewsInvalid();
        }
    }
}

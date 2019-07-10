package com.example.xie.mockrecyclerview.mock;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

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

    public MockRecyclerView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MockRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MockRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initChildrenHelper();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        this.dispatchLayout();
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
    public boolean onTouchEvent(MotionEvent e) {
        final boolean canScrollVertically = mLayout.canScrollVertically();
        final int action = e.getActionMasked();
        final MotionEvent vtev = MotionEvent.obtain(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
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

                if (scrollByInternal(
                        0,
                        canScrollVertically ? dy : 0,
                        vtev)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
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
        int consumedX = 0;
        int consumedY = 0;
        if (dy != 0) {
            consumedY = mLayout.scrollVerticallyBy(dy, mRecycler, mState);
        }
        if (consumed != null) {
            consumed[0] = consumedX;
            consumed[1] = consumedY;
        }
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
        mAdapter = adapter;
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

    public abstract static class Adapter<VH extends MockRecyclerView.ViewHolder> {
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
            onBindViewHolder(holder, position);
        }
    }

    public static class State {
        int mItemCount = 0;

        public int getItemCount() {
            return mItemCount;
        }
    }

    public final class Recycler {

        @NonNull
        public View getViewForPosition(int position) {
            return this.getViewForPosition(position, false);
        }

        View getViewForPosition(int position, boolean dryRun) {
            return this.tryGetViewHolderForPositionByDeadline(position, dryRun, 9223372036854775807L).itemView;
        }

        @Nullable
        MockRecyclerView.ViewHolder tryGetViewHolderForPositionByDeadline(int position, boolean dryRun, long deadlineNs) {
            if (position >= 0 && position < MockRecyclerView.this.mState.getItemCount()) {
                int type;
                int offsetPosition = 0;
                MockRecyclerView.ViewHolder holder = null;
                type = MockRecyclerView.this.mAdapter.getItemViewType(offsetPosition);
                holder = MockRecyclerView.this.mAdapter.createViewHolder(MockRecyclerView.this, type);
                tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
                return holder;
            } else {
                throw new IndexOutOfBoundsException("Invalid item position " + position + "(" + position + "). Item count:" +
                        MockRecyclerView.this.mState.getItemCount() + MockRecyclerView.this.exceptionLabel());
            }

        }

        private void tryBindViewHolderByDeadline(ViewHolder holder, int offsetPosition, int position, long deadlineNs) {
            mAdapter.bindViewHolder(holder, position);
        }

    }


    String exceptionLabel() {
        return " " + super.toString() + ", adapter:" + this.mAdapter + ", layout:" + this.mLayout + ", context:" + this.getContext();
    }

    public abstract static class ViewHolder {
        @NonNull
        public final View itemView;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
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

        private void addView(View child, int index) {
            addViewInt(child, index, false);
        }

        protected void addViewInt(View child, int index, boolean b) {
            mChildHelper.addView(child, index, false);
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
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mScrollingChildHelper == null) {
            mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mScrollingChildHelper;
    }
}

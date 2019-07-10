package com.example.xie.mockrecyclerview.mock;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

public abstract class MockOrientationHelper {
    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;

    public static final int VERTICAL = RecyclerView.VERTICAL;
    private final MockLinearLayoutManager mLayoutManager;

    public MockOrientationHelper(MockLinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    public static MockOrientationHelper createOrientationHelper(
            MockLinearLayoutManager layoutManager, int orientation) {
        switch (orientation) {
            case HORIZONTAL:
                return createHorizontalHelper(layoutManager);
            case VERTICAL:
                return createVerticalHelper(layoutManager);
        }
        throw new IllegalArgumentException("invalid orientation");
    }

    private static MockOrientationHelper createVerticalHelper(
            final MockLinearLayoutManager layoutManager) {
        return new MockOrientationHelper(layoutManager) {
            @Override
            public int getDecoratedMeasurementInOther(View view) {
                final ViewGroup.LayoutParams params =
                        view.getLayoutParams();
                return layoutManager.getDecoratedMeasuredWidth(view)
//                return 720
//                        + params.leftMargin
//                        + params.rightMargin
                        ;
            }

            @Override public int getDecoratedMeasurement(View view) {
                final ViewGroup.LayoutParams params = view.getLayoutParams();
                return layoutManager.getDecoratedMeasuredHeight(view)
//                        + params.topMargin
//                        + params.bottomMargin
                        ;
            }
            @Override
            public void offsetChildren(int amount) {
                layoutManager.offsetChildrenVertical(amount);
            }

            @Override
            public int getDecoratedEnd(View view) {
                final ViewGroup.LayoutParams params =
                        view.getLayoutParams();
                return layoutManager.getDecoratedBottom(view) ;//+ params.bottomMargin
            }

            @Override
            public int getEndAfterPadding() {
                return layoutManager.getHeight(); //- layoutManager.getPaddingBottom();
            }

            @Override
            public int getDecoratedStart(View view) {
                final ViewGroup.LayoutParams params =
                        view.getLayoutParams();
                return layoutManager.getDecoratedTop(view);// - params.topMargin;
            }

            @Override
            public int getStartAfterPadding() {
                return layoutManager.getPaddingTop();
            }

        };
    }

    private static MockOrientationHelper createHorizontalHelper(
            MockLinearLayoutManager layoutManager) {
        return null;
    }

    public abstract int getDecoratedMeasurementInOther(View view);

    public abstract int getDecoratedMeasurement(View view);

    public abstract void offsetChildren(int i);

    public abstract int getDecoratedEnd(View child);

    public abstract int getEndAfterPadding();

    public abstract int getDecoratedStart(View child);

    public abstract int getStartAfterPadding();
}

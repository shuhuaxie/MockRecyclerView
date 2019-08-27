package com.example.xie.mockrecyclerview.mock;

import android.util.Log;
import android.view.View;

public class MockChildHelper {
    private final Callback mCallback;

    public MockChildHelper(Callback callback) {
            mCallback = callback;
    }
    void addView(View child, int index, boolean hidden) {
        final int offset;
        if (index < 0) {
            offset = mCallback.getChildCount();
        } else {
            offset = getOffset(index);
        }
        mCallback.addView(child, offset);
    }

    private int getOffset(int index) {
        return index;
    }

    public int getChildCount() {
        return mCallback.getChildCount();
    }

    View getChildAt(int index) {
        final int offset = getOffset(index);
        return mCallback.getChildAt(offset);
    }
    void removeViewAt(int index) {
        final int offset = getOffset(index);
        final View view = mCallback.getChildAt(offset);
        mCallback.removeViewAt(offset);
    }

    public int getUnfilteredChildCount() {
        return mCallback.getChildCount();
    }

    public View getUnfilteredChildAt(int i) {
        return mCallback.getChildAt(i);
    }

    interface Callback {
        int getChildCount();

        void addView(View child, int index);

        int indexOfChild(View view);

        void removeViewAt(int index);

        View getChildAt(int offset);

        void removeAllViews();

    }
}

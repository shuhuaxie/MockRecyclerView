package com.example.xie.mockrecyclerview.mock;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.example.xie.mockrecyclerview.SimplyAdapter;

import java.util.List;

public class MockRecycleView extends ViewGroup {
    private MockLinearLayoutManager mLayoutManager;
    private Adapter mAdapter;

    public MockRecycleView(Context context) {
        super(context);
    }

    public MockRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MockRecycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    public void setLayoutManager(MockLinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    public abstract static class Adapter<VH extends MockRecycleView.ViewHolder> {
        @NonNull
        public abstract VH onCreateViewHolder(@NonNull ViewGroup var1, int var2);

        public abstract void onBindViewHolder(@NonNull VH var1, int var2);

        public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
            this.onBindViewHolder(holder, position);
        }
        public abstract int getItemCount();

    }

    public abstract static class ViewHolder {

        public ViewHolder(View itemView) {

        }
    }

    public abstract static class MockLayoutManager {

    }
}

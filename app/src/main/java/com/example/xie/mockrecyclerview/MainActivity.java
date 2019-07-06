package com.example.xie.mockrecyclerview;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.xie.mockrecyclerview.mock.MockAdapter;
import com.example.xie.mockrecyclerview.mock.MockLinearLayoutManager;
import com.example.xie.mockrecyclerview.mock.MockRecycleView;

import java.util.ArrayList;

public class MainActivity extends Activity {
    RecyclerView mReal;
    MockRecycleView mMock;
    ArrayList<Info> mInfos = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Info info = new Info();
        info.setName("good");
        mInfos.add(info);
        info = new Info();
        info.setName("night");
        mInfos.add(info);

        mReal = findViewById(R.id.real_recycler_view);
        mMock = findViewById(R.id.mock_recycler_view);
        mReal.setLayoutManager(new LinearLayoutManager(this));
        mReal.setAdapter(new SimplyAdapter(mInfos,this));

        mMock.setLayoutManager(new MockLinearLayoutManager());
        mMock.setAdapter(new MockAdapter(mInfos,this));
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder{
        TextView mTextView;
        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv_item);
        }

        public TextView getTextView() {
            return mTextView;
        }

        public void setTextView(TextView textView) {
            mTextView = textView;
        }
    }
    public static class MockViewHolder extends MockRecycleView.ViewHolder{
        TextView mTextView;
        public MockViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv_item);
        }

        public TextView getTextView() {
            return mTextView;
        }

        public void setTextView(TextView textView) {
            mTextView = textView;
        }
    }
}

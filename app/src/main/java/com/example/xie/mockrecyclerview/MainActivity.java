package com.example.xie.mockrecyclerview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.widget.Toast;
import com.example.xie.mockrecyclerview.mock.MockAdapter;
import com.example.xie.mockrecyclerview.mock.MockLinearLayoutManager;
import com.example.xie.mockrecyclerview.mock.MockRecyclerView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {
    RecyclerView mReal;
    MockRecyclerView mMock;
    ArrayList<Info> mInfos = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Info info = new Info();
        info.setName("Happiness ");
        mInfos.add(info);
        info = new Info();
        info.setName("is");
        mInfos.add(info);
        info = new Info();
        info.setName("a");
        mInfos.add(info);
        info = new Info();
        info.setName("way ");
        mInfos.add(info);
        info = new Info();
        info.setName("station");
        mInfos.add(info);
        info = new Info();
        info.setName("between ");
        mInfos.add(info);
        info = new Info();
        info.setName("too ");
        mInfos.add(info);
        info = new Info();
        info.setName("much ");
        mInfos.add(info);
        info = new Info();
        info.setName("and ");
        mInfos.add(info);
        info = new Info();
        info.setName("too ");
        mInfos.add(info);
        info = new Info();
        info.setName("little ");
        mInfos.add(info);
        info = new Info();
        info.setName(". ");
        mInfos.add(info);
        info = new Info();
        info.setName("Happiness  ");
        mInfos.add(info);
        info = new Info();
        info.setName("is ");
        mInfos.add(info);
        info = new Info();
        info.setName("very");
        mInfos.add(info);
        info = new Info();
        info.setName("different");
        mInfos.add(info);
        info = new Info();
        info.setName("from");
        mInfos.add(info);
        info = new Info();
        info.setName("person");
        mInfos.add(info);
        info = new Info();
        info.setName("to");
        mInfos.add(info);
        info = new Info();
        info.setName("person");
        mInfos.add(info);
        info = new Info();
        info.setName(",");
        mInfos.add(info);
        info = new Info();
        info.setName("and ");
        mInfos.add(info);
        info = new Info();
        info.setName("yet");
        mInfos.add(info);
        info = new Info();
        info.setName("seems");
        mInfos.add(info);
        info = new Info();
        info.setName("so");
        mInfos.add(info);
        info = new Info();
        info.setName("common");
        mInfos.add(info);
        info = new Info();
        info.setName(".");
        mInfos.add(info);

        mReal = findViewById(R.id.real_recycler_view);
        mMock = findViewById(R.id.mock_recycler_view);
        mReal.setLayoutManager(new LinearLayoutManager(this));
        mReal.setAdapter(new SimplyAdapter(mInfos,this));

        mMock.setLayoutManager(new MockLinearLayoutManager(this));
        mMock.setAdapter(new MockAdapter(mInfos,this));

        findViewById(R.id.ll_top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfos.get(0).setName(" *Happiness* ");
                mInfos.get(12).setName(" *Happiness* ");
                mReal.getAdapter().notifyDataSetChanged();
                mMock.getAdapter().notifyDataSetChanged();
                Toast.makeText(MainActivity.this,"notifyDataSetChanged", Toast.LENGTH_SHORT).show();
            }
        });
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

    }
    public static class MockViewHolder extends MockRecyclerView.ViewHolder{
        TextView mTextView;
        public MockViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.tv_item);
        }

        public TextView getTextView() {
            return mTextView;
        }

    }
}

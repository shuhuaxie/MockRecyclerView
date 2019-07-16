package com.example.xie.mockrecyclerview.mock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.xie.mockrecyclerview.Info;
import com.example.xie.mockrecyclerview.MainActivity;
import com.example.xie.mockrecyclerview.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class MockAdapter extends MockRecyclerView.Adapter<MainActivity.MockViewHolder> {
    ArrayList<Info> mInfos;
    Context mContext;

    public MockAdapter(ArrayList<Info> infos, Context context) {
        this.mInfos = infos;
        this.mContext = context;
    }

    @NonNull @Override
    public MainActivity.MockViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MainActivity.MockViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.item_view, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivity.MockViewHolder viewHolder,final int i) {
        viewHolder.getTextView().setText(mInfos.get(i).getName());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(mContext, mInfos.get(i).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mInfos.size();
    }


}

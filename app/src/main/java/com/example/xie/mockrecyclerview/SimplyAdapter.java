package com.example.xie.mockrecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

public class SimplyAdapter extends RecyclerView.Adapter<MainActivity.SimpleViewHolder> {
    ArrayList<Info> mInfos;
    Context mContext;

    public SimplyAdapter(ArrayList<Info> infos, Context context) {
        this.mInfos = infos;
        this.mContext = context;
    }

    @NonNull @Override
    public MainActivity.SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MainActivity.SimpleViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.item_view, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MainActivity.SimpleViewHolder viewHolder, int i) {
        viewHolder.getTextView().setText(mInfos.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return mInfos.size();
    }


}

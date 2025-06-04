package com.harry.uhf_c.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harry.uhf_c.R;
import com.harry.uhf_c.entity.TagInfo;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<TagInfo> tagList;

    public TagAdapter(List<TagInfo> tagList) {
        this.tagList = tagList;
    }

    public void updateList(List<TagInfo> newList) {
        this.tagList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        TagInfo tag = tagList.get(position);
        holder.tvIndex.setText(String.valueOf(position + 1)); // Show index starting from 1
        holder.tvEpc.setText(tag.getEpc());
        holder.tvAnt.setText(String.valueOf(tag.getAnt()));
        holder.tvFirstTime.setText(tag.getFirstTime());
    }

    @Override
    public int getItemCount() {
        return tagList != null ? tagList.size() : 0;
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex, tvEpc, tvAnt, tvFirstTime;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvIndex);
            tvEpc = itemView.findViewById(R.id.tvEpc);
            tvAnt = itemView.findViewById(R.id.tvAnt);
            tvFirstTime = itemView.findViewById(R.id.tvFirstTime);
        }
    }
}

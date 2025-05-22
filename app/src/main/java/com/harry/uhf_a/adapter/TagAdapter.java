package com.harry.uhf_a.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harry.uhf_a.R;
import com.harry.uhf_a.entity.TagInfo;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<TagInfo> tagList;

    public TagAdapter(List<TagInfo> tagList) {
        this.tagList = tagList;
    }

    public void updateList(List<TagInfo> newList) {
        this.tagList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_header, parent, false);
            return new TagViewHolder(view, true);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_tag, parent, false);
            return new TagViewHolder(view, false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            TagInfo tag = tagList.get(position - 1); // minus 1 because header is at position 0
            holder.tvEpc.setText(tag.getEpc());
            holder.tvAnt.setText(String.valueOf(tag.getAnt()));
            holder.tvFirstTime.setText(tag.getFirstTime());
        }
    }

    @Override
    public int getItemCount() {
        return tagList == null ? 1 : tagList.size() + 1; // +1 for header
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tvEpc, tvAnt, tvFirstTime;

        public TagViewHolder(@NonNull View itemView, boolean isHeader) {
            super(itemView);
            if (!isHeader) {
                tvEpc = itemView.findViewById(R.id.tvEpc);
                tvAnt = itemView.findViewById(R.id.tvAnt);
                tvFirstTime = itemView.findViewById(R.id.tvFirstTime);
            }
        }
    }
}

package com.harry.uhf_c.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harry.uhf_c.R;

import java.io.File;
import java.util.List;

public class LogFileAdapter extends RecyclerView.Adapter<LogFileAdapter.LogViewHolder> {

    private final List<File> fileList;
    private final OnLogFileActionListener listener;

    public interface OnLogFileActionListener {
        void onView(File file);
        void onDelete(File file);
        void onShare(File file);
    }

    public LogFileAdapter(List<File> fileList, OnLogFileActionListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log_file, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        File file = fileList.get(position);
        holder.tvFileName.setText(file.getName());

        holder.tvFileName.setOnClickListener(v -> listener.onView(file));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(file));
        holder.btnShare.setOnClickListener(v -> listener.onShare(file));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        ImageButton btnDelete, btnShare;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}

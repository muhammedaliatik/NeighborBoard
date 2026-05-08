package com.muhammedaliatik.neighborboard.ui.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muhammedaliatik.neighborboard.databinding.ItemCommentBinding;
import com.muhammedaliatik.neighborboard.model.Comment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comment> list;

    public CommentsAdapter(List<Comment> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = list.get(position);
        holder.binding.tvCommentUser.setText(comment.getUserName());
        holder.binding.tvCommentText.setText(comment.getText());

        String date = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
                .format(new Date(comment.getTimestamp()));
        holder.binding.tvCommentTime.setText(date);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;

        public ViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
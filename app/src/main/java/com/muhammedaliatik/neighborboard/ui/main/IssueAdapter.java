package com.muhammedaliatik.neighborboard.ui.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muhammedaliatik.neighborboard.databinding.ItemIssueBinding;
import com.muhammedaliatik.neighborboard.model.Issue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {

    private List<Issue> list;

    public IssueAdapter(List<Issue> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIssueBinding binding = ItemIssueBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Issue issue = list.get(position);
        holder.binding.tvIssueTitle.setText(issue.getTitle());
        holder.binding.tvIssueDescription.setText(issue.getDescription());
        holder.binding.tvIssueLocation.setText("Konum: " + issue.getLocation());
        holder.binding.tvIssueSender.setText(issue.getSenderName());
        holder.binding.tvIssueStatus.setText(issue.getStatus());

        if (issue.getStatus().equals("açık")) {
            holder.binding.tvIssueStatus.setTextColor(0xFFE53935);
        } else {
            holder.binding.tvIssueStatus.setTextColor(0xFF43A047);
        }

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(issue.getTimestamp()));
        holder.binding.tvIssueDate.setText(date);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemIssueBinding binding;

        public ViewHolder(ItemIssueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
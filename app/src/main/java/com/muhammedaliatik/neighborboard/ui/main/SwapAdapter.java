package com.muhammedaliatik.neighborboard.ui.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muhammedaliatik.neighborboard.databinding.ItemSwapBinding;
import com.muhammedaliatik.neighborboard.model.SwapItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SwapAdapter extends RecyclerView.Adapter<SwapAdapter.ViewHolder> {

    private List<SwapItem> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onRequestClick(SwapItem item);
    }

    public SwapAdapter(List<SwapItem> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSwapBinding binding = ItemSwapBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SwapItem item = list.get(position);
        holder.binding.tvSwapName.setText(item.getName());
        holder.binding.tvSwapDescription.setText(item.getDescription());
        holder.binding.tvSwapOwner.setText(item.getOwnerName());

        if (item.isAvailable()) {
            holder.binding.tvSwapStatus.setText("Müsait");
            holder.binding.tvSwapStatus.setTextColor(0xFF43A047);
        } else {
            holder.binding.tvSwapStatus.setText("Verildi");
            holder.binding.tvSwapStatus.setTextColor(0xFFE53935);
        }

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(item.getTimestamp()));
        holder.binding.tvSwapDate.setText(date);

        // Karta tıklayınca talep et
        holder.itemView.setOnClickListener(v -> {
            if (item.isAvailable()) {
                listener.onRequestClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemSwapBinding binding;

        public ViewHolder(ItemSwapBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
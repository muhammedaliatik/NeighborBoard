package com.muhammedaliatik.neighborboard.ui.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muhammedaliatik.neighborboard.R;
import com.muhammedaliatik.neighborboard.databinding.ItemSwapBinding;
import com.muhammedaliatik.neighborboard.model.SwapItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SwapAdapter extends RecyclerView.Adapter<SwapAdapter.ViewHolder> {

    private List<SwapItem> list;

    public SwapAdapter(List<SwapItem> list) {
        this.list = list;
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
            holder.binding.tvSwapStatus.setText("Talep edildi");
            holder.binding.tvSwapStatus.setTextColor(0xFFE53935);
        }
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(item.getTimestamp()));
        holder.binding.tvSwapDate.setText(date);

        holder.itemView.setOnClickListener(v -> {
            if (item.isAvailable()) {
                showSwapRequestDialog(holder.itemView.getContext(), item);
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

    private void showSwapRequestDialog(android.content.Context context, SwapItem item) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(context);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_swap_request, null);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

        android.widget.TextView tvItemName = dialogView.findViewById(R.id.tvItemName);
        android.widget.TextView tvOwnerName = dialogView.findViewById(R.id.tvOwnerName);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        android.widget.Button btnRequest = dialogView.findViewById(R.id.btnRequest);

        tvItemName.setText(item.getName());
        tvOwnerName.setText(item.getOwnerName());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnRequest.setOnClickListener(v -> {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            dbRef.child("swapItems")
                    .child(item.getApartmentCode())
                    .child(item.getId())
                    .child("available")
                    .setValue(false)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Talep gönderildi", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}
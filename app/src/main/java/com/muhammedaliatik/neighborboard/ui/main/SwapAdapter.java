package com.muhammedaliatik.neighborboard.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammedaliatik.neighborboard.R;
import com.muhammedaliatik.neighborboard.databinding.ItemSwapBinding;
import com.muhammedaliatik.neighborboard.model.Comment;
import com.muhammedaliatik.neighborboard.model.SwapItem;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SwapAdapter extends RecyclerView.Adapter<SwapAdapter.ViewHolder> {

    private List<SwapItem> list;
    private DatabaseReference dbRef;

    public SwapAdapter(List<SwapItem> list) {
        this.list = list;
        this.dbRef = FirebaseDatabase.getInstance().getReference();
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

        if (item.getOwnerUid() != null && !item.getOwnerUid().isEmpty()) {
            loadPhotoByUid(item.getOwnerUid(), holder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (item.isAvailable()) {
                showSwapRequestDialog(holder.itemView.getContext(), item);
            } else {
                showCommentsDialog(holder.itemView.getContext(), item.getId(), item.getApartmentCode(), "swap");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void loadPhotoByUid(String uid, ViewHolder holder) {
        dbRef.child("users").child(uid).child("profilePhoto")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String base64 = snapshot.getValue(String.class);
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            Glide.with(holder.itemView.getContext())
                                    .load(bitmap)
                                    .transform(new CircleCrop())
                                    .into(holder.binding.ivAvatar);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
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

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        android.widget.TextView tvItemName = dialogView.findViewById(R.id.tvItemName);
        android.widget.TextView tvOwnerName = dialogView.findViewById(R.id.tvOwnerName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnRequest = dialogView.findViewById(R.id.btnRequest);

        tvItemName.setText(item.getName());
        tvOwnerName.setText(item.getOwnerName());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnRequest.setOnClickListener(v -> {
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

    private void showCommentsDialog(android.content.Context context, String itemId, String aptCode, String type) {
        android.view.View dialogView = android.view.LayoutInflater.from(context)
                .inflate(R.layout.dialog_comments, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rvComments = dialogView.findViewById(R.id.rvComments);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSend = dialogView.findViewById(R.id.btnSendComment);

        List<Comment> commentList = new ArrayList<>();
        CommentsAdapter adapter = new CommentsAdapter(commentList);
        rvComments.setLayoutManager(new LinearLayoutManager(context));
        rvComments.setAdapter(adapter);

        DatabaseReference commentsRef = dbRef.child(type + "Comments")
                .child(aptCode).child(itemId);

        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Comment comment = ds.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        btnSend.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(context, "Yorum boş bırakılamaz", Toast.LENGTH_SHORT).show();
                return;
            }

            String userName = new SessionManager(context).getDisplayName();
            String uid = new SessionManager(context).getUid();
            String commentKey = commentsRef.push().getKey();
            Comment comment = new Comment(commentKey, text, userName, uid, System.currentTimeMillis());

            commentsRef.child(commentKey).setValue(comment)
                    .addOnSuccessListener(unused -> {
                        etComment.setText("");
                        Toast.makeText(context, "Yorum eklendi", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}
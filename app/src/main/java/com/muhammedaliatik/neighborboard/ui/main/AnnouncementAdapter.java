package com.muhammedaliatik.neighborboard.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammedaliatik.neighborboard.databinding.ItemAnnouncementBinding;
import com.muhammedaliatik.neighborboard.model.Announcement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.muhammedaliatik.neighborboard.R;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Announcement> list;
    private DatabaseReference dbRef;

    public AnnouncementAdapter(List<Announcement> list) {
        this.list = list;
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAnnouncementBinding binding = ItemAnnouncementBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement a = list.get(position);
        holder.binding.tvTitle.setText(a.getTitle());
        holder.binding.tvContent.setText(a.getContent());
        holder.binding.tvSender.setText(a.getSenderName());

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(a.getTimestamp()));
        holder.binding.tvDate.setText(date);

        // Profil fotoğrafını yükle
        // Profil fotoğrafını yükle
        if (a.getSenderUid() != null && !a.getSenderUid().isEmpty()) {
            // Yeni duyurular — uid ile direkt çek
            loadPhotoByUid(a.getSenderUid(), holder);
            // Yorum sayısını yükle
        } else if (a.getSenderName() != null && !a.getSenderName().isEmpty()) {
            // Eski duyurular — isimden uid bul
            dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String name = userSnap.child("displayName").getValue(String.class);
                        if (a.getSenderName().equals(name)) {
                            String uid = userSnap.getKey();
                            loadPhotoByUid(uid, holder);
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        holder.itemView.setOnClickListener(v ->
                showCommentsDialog(holder.itemView.getContext(), a.getId(), a.getApartmentCode(), "announcement")
        );
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
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAnnouncementBinding binding;

        public ViewHolder(ItemAnnouncementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private void showCommentsDialog(android.content.Context context, String itemId, String aptCode, String type) {
        android.view.View dialogView = android.view.LayoutInflater.from(context)
                .inflate(R.layout.dialog_comments, null);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        androidx.recyclerview.widget.RecyclerView rvComments = dialogView.findViewById(R.id.rvComments);
        android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);
        android.widget.Button btnSend = dialogView.findViewById(R.id.btnSendComment);

        java.util.List<com.muhammedaliatik.neighborboard.model.Comment> commentList = new java.util.ArrayList<>();
        CommentsAdapter adapter = new CommentsAdapter(commentList);
        rvComments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
        rvComments.setAdapter(adapter);

        com.google.firebase.database.DatabaseReference commentsRef = dbRef.child(type + "Comments")
                .child(aptCode).child(itemId);

        commentsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                commentList.clear();
                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                    com.muhammedaliatik.neighborboard.model.Comment comment = ds.getValue(com.muhammedaliatik.neighborboard.model.Comment.class);
                    if (comment != null) {
                        commentList.add(comment);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
            }
        });

        btnSend.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (text.isEmpty()) {
                android.widget.Toast.makeText(context, "Yorum boş bırakılamaz", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            String userName = new com.muhammedaliatik.neighborboard.utils.SessionManager(context).getDisplayName();
            String uid = new com.muhammedaliatik.neighborboard.utils.SessionManager(context).getUid();
            String commentKey = commentsRef.push().getKey();
            com.muhammedaliatik.neighborboard.model.Comment comment = new com.muhammedaliatik.neighborboard.model.Comment(
                    commentKey, text, userName, uid, System.currentTimeMillis());

            commentsRef.child(commentKey).setValue(comment)
                    .addOnSuccessListener(unused -> {
                        etComment.setText("");
                        android.widget.Toast.makeText(context, "Yorum eklendi", android.widget.Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            android.widget.Toast.makeText(context, "Hata: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}

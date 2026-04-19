package com.muhammedaliatik.neighborboard.ui.main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

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
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
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
        ItemAnnouncementBinding binding;

        public ViewHolder(ItemAnnouncementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
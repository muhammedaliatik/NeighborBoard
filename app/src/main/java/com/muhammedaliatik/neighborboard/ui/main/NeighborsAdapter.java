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
import com.muhammedaliatik.neighborboard.databinding.ItemNeighborBinding;
import com.muhammedaliatik.neighborboard.model.User;

import java.util.List;

public class NeighborsAdapter extends RecyclerView.Adapter<NeighborsAdapter.ViewHolder> {

    private List<User> list;
    private DatabaseReference dbRef;

    public NeighborsAdapter(List<User> list) {
        this.list = list;
        this.dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNeighborBinding binding = ItemNeighborBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        holder.binding.tvNeighborName.setText(user.getDisplayName());
        holder.binding.tvNeighborApt.setText("Daire sakini");

        if (user.getUid() != null) {
            dbRef.child("users").child(user.getUid()).child("profilePhoto")
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
                                        .skipMemoryCache(true)
                                        .into(holder.binding.ivNeighborPhoto);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemNeighborBinding binding;

        public ViewHolder(ItemNeighborBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
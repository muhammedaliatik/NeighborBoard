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
import com.muhammedaliatik.neighborboard.databinding.ItemIssueBinding;
import com.muhammedaliatik.neighborboard.model.Comment;
import com.muhammedaliatik.neighborboard.model.Issue;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {

    private List<Issue> list;
    private DatabaseReference dbRef;

    public IssueAdapter(List<Issue> list) {
        this.list = list;
        this.dbRef = FirebaseDatabase.getInstance().getReference();
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
        holder.binding.tvIssueLocation.setText("📍 " + issue.getLocation());
        holder.binding.tvIssueSender.setText(issue.getSenderName());

        if ("açık".equals(issue.getStatus())) {
            holder.binding.tvIssueStatus.setText("Açık");
            holder.binding.tvIssueStatus.setTextColor(0xFFE53935);
        } else {
            holder.binding.tvIssueStatus.setText("Kapalı");
            holder.binding.tvIssueStatus.setTextColor(0xFF43A047);
        }

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(issue.getTimestamp()));
        holder.binding.tvIssueDate.setText(date);

        if (issue.getSenderUid() != null && !issue.getSenderUid().isEmpty()) {
            loadPhotoByUid(issue.getSenderUid(), holder);
        } else if (issue.getSenderName() != null && !issue.getSenderName().isEmpty()) {
            dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String name = userSnap.child("displayName").getValue(String.class);
                        if (issue.getSenderName().equals(name)) {
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

        holder.itemView.setOnClickListener(v ->
                showCommentsDialog(holder.itemView.getContext(), issue.getId(), issue.getApartmentCode(), "issue")
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
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemIssueBinding binding;

        public ViewHolder(ItemIssueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
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
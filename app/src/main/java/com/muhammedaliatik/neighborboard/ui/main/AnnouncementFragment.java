package com.muhammedaliatik.neighborboard.ui.main;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammedaliatik.neighborboard.databinding.FragmentAnnouncementBinding;
import com.muhammedaliatik.neighborboard.model.Announcement;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;

import com.muhammedaliatik.neighborboard.R;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;

public class AnnouncementFragment extends Fragment {

    private FragmentAnnouncementBinding binding;
    private DatabaseReference dbRef;
    private SessionManager sessionManager;
    private List<Announcement> announcementList;
    private AnnouncementAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAnnouncementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        dbRef = FirebaseDatabase.getInstance().getReference();
        announcementList = new ArrayList<>();

        adapter = new AnnouncementAdapter(announcementList);
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAnnouncements.setAdapter(adapter);

        loadAnnouncements();

        binding.fabAddAnnouncement.setOnClickListener(v -> showAddAnnouncementDialog());
    }

    private void loadAnnouncements() {
        String aptCode = sessionManager.getApartmentCode();
        dbRef.child("announcements").child(aptCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        announcementList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Announcement a = ds.getValue(Announcement.class);
                            if (a != null) {
                                a.setId(ds.getKey());
                                announcementList.add(a);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.tvAnnouncementCount.setText(String.valueOf(announcementList.size()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "Veri yüklenemedi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddAnnouncementDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_announcement, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }

            String aptCode = sessionManager.getApartmentCode();
            String senderName = sessionManager.getDisplayName();
            String uid = sessionManager.getUid();
            String key = dbRef.child("announcements").child(aptCode).push().getKey();
            Announcement announcement = new Announcement(key, title, content, senderName, uid, aptCode, System.currentTimeMillis());

            dbRef.child("announcements").child(aptCode).child(key).setValue(announcement)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), "Duyuru paylaşıldı", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
    private void sendLocalNotification(String title, String body) {
        String channelId = "neighborboard_channel";
        NotificationManager manager = (NotificationManager) requireContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                channelId, "NeighborBoard Bildirimleri", NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
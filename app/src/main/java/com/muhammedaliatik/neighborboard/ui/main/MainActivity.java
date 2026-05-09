package com.muhammedaliatik.neighborboard.ui.main;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.messaging.FirebaseMessaging;
import com.muhammedaliatik.neighborboard.R;
import com.muhammedaliatik.neighborboard.databinding.ActivityMainBinding;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. ADIM: Android 13+ için bildirim izni isteme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // --- BİLDİRİM KANALI OLUŞTURMA (Android 8.0 ve üzeri için ŞART) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "duyuru_kanali"; // Node.js'de yazdığımızla BİREBİR aynı olmalı
            CharSequence name = "Apartman Duyuruları";
            String description = "Apartmanınıza ait yeni duyurular";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // --- 2. ADIM: DİNAMİK BİLDİRİM GRUBUNA ABONE OLMA ---
        SessionManager sessionManager = new SessionManager(this);
        String currentAptCode = sessionManager.getApartmentCode();

        if (currentAptCode != null && !currentAptCode.trim().isEmpty()) {
            // Firebase'deki topic formatıyla aynı yapıyoruz (boşluksuz, küçük harf)
            String topicName = "apartman_" + currentAptCode.trim().toLowerCase();

            FirebaseMessaging.getInstance().subscribeToTopic(topicName)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.e("FCM", topicName + " grubuna abone olunamadı!");
                        } else {
                            Log.d("FCM", topicName + " grubuna başarıyla abone olundu!");
                        }
                    });
        } else {
            Log.e("FCM", "Apartman kodu bulunamadı, abonelik yapılamadı.");
        }

        loadFragment(new AnnouncementFragment());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();

            if (id == R.id.nav_announcements) {
                fragment = new AnnouncementFragment();
            } else if (id == R.id.nav_issues) {
                fragment = new IssueFragment();
            } else if (id == R.id.nav_swap) {
                fragment = new SwapFragment();
            } else if (id == R.id.nav_neighbors) {
                fragment = new NeighborsFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else {
                return false;
            }

            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
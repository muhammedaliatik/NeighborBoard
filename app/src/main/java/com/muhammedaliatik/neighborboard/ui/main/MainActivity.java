package com.muhammedaliatik.neighborboard.ui.main;

import android.Manifest;
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

        // 2. ADIM: Bildirim grubuna abone olma (Topic Subscription)
        // (Bunu daha sonra apartman numarasına göre dinamik yapabilirsin)
        FirebaseMessaging.getInstance().subscribeToTopic("mahalle_bildirimleri")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("FCM", "Bildirim grubuna abone olunamadı!");
                    } else {
                        Log.d("FCM", "Bildirim grubuna başarıyla abone olundu!");
                    }
                });

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
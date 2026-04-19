package com.muhammedaliatik.neighborboard.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.muhammedaliatik.neighborboard.R;
import com.muhammedaliatik.neighborboard.databinding.ActivityMainBinding;

import com.muhammedaliatik.neighborboard.ui.main.NeighborsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
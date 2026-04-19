package com.muhammedaliatik.neighborboard.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.muhammedaliatik.neighborboard.databinding.FragmentNeighborsBinding;
import com.muhammedaliatik.neighborboard.model.User;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class NeighborsFragment extends Fragment {

    private FragmentNeighborsBinding binding;
    private DatabaseReference dbRef;
    private SessionManager sessionManager;
    private List<User> neighborList;
    private NeighborsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNeighborsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        dbRef = FirebaseDatabase.getInstance().getReference();
        neighborList = new ArrayList<>();

        adapter = new NeighborsAdapter(neighborList);
        binding.rvNeighbors.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNeighbors.setAdapter(adapter);

        loadNeighbors();
    }

    private void loadNeighbors() {
        String myAptCode = sessionManager.getApartmentCode();
        String myUid = sessionManager.getUid();

        dbRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                neighborList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null && myAptCode.equals(user.getApartmentCode())
                            && !ds.getKey().equals(myUid)) {
                        user.setUid(ds.getKey());
                        neighborList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Veri yüklenemedi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.muhammedaliatik.neighborboard.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammedaliatik.neighborboard.R;
import com.muhammedaliatik.neighborboard.databinding.FragmentSwapBinding;
import com.muhammedaliatik.neighborboard.model.SwapItem;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SwapFragment extends Fragment {

    private FragmentSwapBinding binding;
    private DatabaseReference dbRef;
    private SessionManager sessionManager;
    private List<SwapItem> swapList;
    private SwapAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSwapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        dbRef = FirebaseDatabase.getInstance().getReference();
        swapList = new ArrayList<>();

        adapter = new SwapAdapter(swapList);
        binding.rvSwapItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSwapItems.setAdapter(adapter);

        loadSwapItems();

        binding.fabAddSwapItem.setOnClickListener(v -> showAddSwapDialog());
    }

    private void loadSwapItems() {
        String aptCode = sessionManager.getApartmentCode();
        dbRef.child("swapItems").child(aptCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        swapList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            SwapItem item = ds.getValue(SwapItem.class);
                            if (item != null) {
                                item.setId(ds.getKey());
                                swapList.add(item);
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

    private void showAddSwapDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_swap, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }

            String aptCode = sessionManager.getApartmentCode();
            String ownerName = sessionManager.getDisplayName();
            String uid = sessionManager.getUid();
            String key = dbRef.child("swapItems").child(aptCode).push().getKey();
            SwapItem item = new SwapItem(key, name, description, ownerName, uid, aptCode, true, System.currentTimeMillis());

            dbRef.child("swapItems").child(aptCode).child(key).setValue(item)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), "İlan paylaşıldı", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
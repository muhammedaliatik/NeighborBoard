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
import com.muhammedaliatik.neighborboard.databinding.FragmentIssueBinding;
import com.muhammedaliatik.neighborboard.model.Issue;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import com.muhammedaliatik.neighborboard.R;
import android.view.View;
import android.widget.Button;
public class IssueFragment extends Fragment {

    private FragmentIssueBinding binding;
    private DatabaseReference dbRef;
    private SessionManager sessionManager;
    private List<Issue> issueList;
    private IssueAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIssueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        dbRef = FirebaseDatabase.getInstance().getReference();
        issueList = new ArrayList<>();

        adapter = new IssueAdapter(issueList);
        binding.rvIssues.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIssues.setAdapter(adapter);

        loadIssues();

        binding.fabAddIssue.setOnClickListener(v -> showAddIssueDialog());
    }

    private void loadIssues() {
        String aptCode = sessionManager.getApartmentCode();
        dbRef.child("issues").child(aptCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        issueList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Issue issue = ds.getValue(Issue.class);
                            if (issue != null) {
                                issue.setId(ds.getKey());
                                issueList.add(issue);
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

    private void showAddIssueDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_issue, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }

            String aptCode = sessionManager.getApartmentCode();
            String senderName = sessionManager.getDisplayName();
            String uid = sessionManager.getUid();
            String key = dbRef.child("issues").child(aptCode).push().getKey();
            Issue issue = new Issue(key, title, description, location, "açık", senderName, uid, aptCode, System.currentTimeMillis());

            dbRef.child("issues").child(aptCode).child(key).setValue(issue)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), "Arıza bildirildi", Toast.LENGTH_SHORT).show();
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
package com.muhammedaliatik.neighborboard.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muhammedaliatik.neighborboard.databinding.ActivityRegisterBinding;
import com.muhammedaliatik.neighborboard.model.User;
import com.muhammedaliatik.neighborboard.utils.SessionManager;
import com.muhammedaliatik.neighborboard.ui.main.MainActivity;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(this);

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String apartmentCode = binding.etApartmentCode.getText().toString().trim().toUpperCase();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || apartmentCode.isEmpty()) {
            Toast.makeText(this, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User user = new User(uid, email, name, apartmentCode);

                    dbRef.child("users").child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                sessionManager.saveSession(uid, name, apartmentCode);
                                Toast.makeText(this, "Kayıt başarılı, hoş geldin " + name, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Kullanıcı kaydedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Kayıt başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
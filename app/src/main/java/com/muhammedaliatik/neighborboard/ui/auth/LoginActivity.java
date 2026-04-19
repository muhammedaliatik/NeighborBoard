package com.muhammedaliatik.neighborboard.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammedaliatik.neighborboard.databinding.ActivityLoginBinding;
import com.muhammedaliatik.neighborboard.model.User;
import com.muhammedaliatik.neighborboard.ui.main.MainActivity;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        // Google Sign In yapılandırması
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(com.muhammedaliatik.neighborboard.R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(this, "Google girişi başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        binding.btnForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Lütfen e-posta adresinizi girin", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Şifre sıfırlama maili gönderildi", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "E-posta ve şifre boş bırakılamaz", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    dbRef.child("users").child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    String displayName = snapshot.child("displayName").getValue(String.class);
                                    String apartmentCode = snapshot.child("apartmentCode").getValue(String.class);
                                    sessionManager.saveSession(uid, displayName, apartmentCode);
                                    Toast.makeText(LoginActivity.this, "Hoş geldin, " + displayName, Toast.LENGTH_SHORT).show();
                                    goToMain();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Toast.makeText(LoginActivity.this, "Veri okunamadı: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Giriş başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    String email = authResult.getUser().getEmail();
                    String displayName = authResult.getUser().getDisplayName();

                    dbRef.child("users").child(uid).get()
                            .addOnSuccessListener(snapshot -> {
                                if (!snapshot.exists()) {
                                    showApartmentCodeDialog(uid, email, displayName);
                                } else {
                                    String aptCode = snapshot.child("apartmentCode").getValue(String.class);
                                    sessionManager.saveSession(uid, displayName, aptCode);
                                    Toast.makeText(this, "Hoş geldin, " + displayName, Toast.LENGTH_SHORT).show();
                                    goToMain();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showApartmentCodeDialog(String uid, String email, String displayName) {
        android.widget.EditText etCode = new android.widget.EditText(this);
        etCode.setHint("Apartman Kodu (örn: APT001)");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Apartman Kodunuz")
                .setMessage("Yöneticinizden aldığınız kodu girin")
                .setView(etCode)
                .setPositiveButton("Tamam", (dialog, which) -> {
                    String aptCode = etCode.getText().toString().trim().toUpperCase();
                    if (aptCode.isEmpty()) {
                        Toast.makeText(this, "Kod boş bırakılamaz", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    User user = new User(uid, email, displayName, aptCode);
                    dbRef.child("users").child(uid).setValue(user)
                            .addOnSuccessListener(unused -> {
                                sessionManager.saveSession(uid, displayName, aptCode);
                                Toast.makeText(this, "Hoş geldin, " + displayName, Toast.LENGTH_SHORT).show();
                                goToMain();
                            });
                })
                .setCancelable(false)
                .show();
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
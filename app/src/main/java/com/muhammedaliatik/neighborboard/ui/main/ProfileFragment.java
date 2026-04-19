package com.muhammedaliatik.neighborboard.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammedaliatik.neighborboard.R;
import com.muhammedaliatik.neighborboard.databinding.FragmentProfileBinding;
import com.muhammedaliatik.neighborboard.ui.auth.LoginActivity;
import com.muhammedaliatik.neighborboard.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private DatabaseReference dbRef;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        dbRef = FirebaseDatabase.getInstance().getReference();

        String displayName = sessionManager.getDisplayName();
        String apartmentCode = sessionManager.getApartmentCode();
        String uid = sessionManager.getUid();

        binding.tvDisplayName.setText(displayName != null ? displayName : "Kullanıcı");
        binding.tvApartmentCode.setText("Apartman Kodu: " + (apartmentCode != null ? apartmentCode : "-"));

        // Profil fotoğrafını yükle
        loadProfilePhoto(uid);

        // Galeri launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        uploadProfilePhoto(imageUri, uid);
                    }
                });

        // İzin launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        openGallery();
                    } else {
                        Toast.makeText(requireContext(), "Galeri izni gerekli", Toast.LENGTH_SHORT).show();
                    }
                });

        binding.tvChangePhoto.setOnClickListener(v -> checkPermissionAndOpenGallery());
        binding.ivProfilePhoto.setOnClickListener(v -> checkPermissionAndOpenGallery());

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            sessionManager.clearSession();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void checkPermissionAndOpenGallery() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadProfilePhoto(Uri imageUri, String uid) {
        try {
            Toast.makeText(requireContext(), "Yükleniyor...", Toast.LENGTH_SHORT).show();

            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Küçült — DB'ye sığsın
            bitmap = rotateBitmapIfNeeded(imageUri, bitmap);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true);            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            dbRef.child("users").child(uid).child("profilePhoto").setValue(base64Image)
                    .addOnSuccessListener(unused -> {
                        // Göster
                        Glide.with(this)
                                .load(imageUri)
                                .transform(new CircleCrop())
                                .into(binding.ivProfilePhoto);
                        Toast.makeText(requireContext(), "Fotoğraf güncellendi", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Fotoğraf okunamadı", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProfilePhoto(String uid) {
        if (uid == null) return;
        dbRef.child("users").child(uid).child("profilePhoto")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String base64 = snapshot.getValue(String.class);
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            Glide.with(ProfileFragment.this)
                                    .load(bitmap)
                                    .transform(new CircleCrop())
                                    .into(binding.ivProfilePhoto);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private Bitmap rotateBitmapIfNeeded(Uri imageUri, Bitmap bitmap) {
        try {
            androidx.exifinterface.media.ExifInterface exif = new androidx.exifinterface.media.ExifInterface(
                    requireContext().getContentResolver().openInputStream(imageUri));
            int orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);

            android.graphics.Matrix matrix = new android.graphics.Matrix();
            switch (orientation) {
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            return bitmap;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
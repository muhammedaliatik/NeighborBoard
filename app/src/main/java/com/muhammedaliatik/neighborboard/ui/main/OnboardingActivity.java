package com.muhammedaliatik.neighborboard.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.muhammedaliatik.neighborboard.R;
import com.muhammedaliatik.neighborboard.ui.auth.LoginActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SharedPreferences ile kullanıcının ilk girişi olup olmadığını kontrol ediyoruz
        SharedPreferences sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        boolean isFirstTime = sharedPref.getBoolean("isFirstTime", true);

        // Eğer ilk giriş değilse, bu ekranı atlayıp direkt Login ekranına geçiyoruz
        if (!isFirstTime) {
            Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Kullanıcı geri tuşuna basarsa onboarding'e dönmesin diye bu activity'i kapatıyoruz
            return;
        }

        // Eğer ilk girişse layout'u gösteriyoruz
        setContentView(R.layout.activity_onboarding);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Butona tıklandığında artık "ilk giriş" durumunu false yapıp kaydediyoruz
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isFirstTime", false);
                editor.apply();

                // Login ekranına yönlendiriyoruz
                Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
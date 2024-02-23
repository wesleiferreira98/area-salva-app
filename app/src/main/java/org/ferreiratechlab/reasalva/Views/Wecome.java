package org.ferreiratechlab.reasalva.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;

import org.ferreiratechlab.reasalva.MainActivity;
import org.ferreiratechlab.reasalva.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;
public class Wecome extends AppCompatActivity {

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private static final int REQUEST_CODE_PERMISSION_NOTIFICATIONS = 1001;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_wecome);

        Button authenticateButton = findViewById(R.id.authenticate_button);
        authenticateButton.setOnClickListener(v -> {
            biometricPrompt.authenticate(promptInfo);
        });

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, authenticationCallback);
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticação Biométrica")
                .setSubtitle("Autentique-se para acessar os textos")
                .setNegativeButtonText("Cancelar")
                .build();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Fecha a atividade quando ela não está mais visível
        finish();
    }

    private BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            startActivity(new Intent(Wecome.this, MainActivity.class));
            finish();
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast.makeText(Wecome.this, "Falha na autenticação biométrica", Toast.LENGTH_SHORT).show();
        }
    };
}
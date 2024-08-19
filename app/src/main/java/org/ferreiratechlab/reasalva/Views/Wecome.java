package org.ferreiratechlab.reasalva.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.KeyguardManager;
import android.os.Bundle;

import org.ferreiratechlab.reasalva.MainActivity;
import org.ferreiratechlab.reasalva.R;
import org.ferreiratechlab.reasalva.Security.KeyManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;
public class Wecome extends AppCompatActivity {

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private static final int REQUEST_CODE_PERMISSION_NOTIFICATIONS = 1001;

    private static final int REQUEST_CODE_DEVICE_CREDENTIAL = 1;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_wecome);


        Button authenticateButton = findViewById(R.id.authenticate_button);
        authenticateButton.setOnClickListener(v -> {
            initAuthentication();
        });

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, authenticationCallback);
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticação Biométrica")
                .setSubtitle("Autentique-se para acessar os textos")
                .setNegativeButtonText("Cancelar")
                .build();

        initAuthentication();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Fecha a atividade quando ela não está mais visível
        finish();
    }
    private void initAuthentication() {
        if (isBiometricAvailable()) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            authenticateWithDeviceCredentials();
        }
    }

    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void authenticateWithDeviceCredentials() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Autenticação Requerida", "Autentique-se para acessar os PDFs");
            if (intent != null) {
                startActivityForResult(intent, REQUEST_CODE_DEVICE_CREDENTIAL);
            }
        } else {
            Toast.makeText(this, "Configuração de segurança do dispositivo necessária. Vá para Configurações.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DEVICE_CREDENTIAL) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(Wecome.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Falha na autenticação com credenciais do dispositivo", Toast.LENGTH_SHORT).show();
            }
        }
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
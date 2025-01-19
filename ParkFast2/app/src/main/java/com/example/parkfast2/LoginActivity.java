package com.example.parkfast2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private EditText contraseñaEditText;
    private Button botonLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Asegúrate de que este es el nombre correcto de tu archivo XML

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        contraseñaEditText = findViewById(R.id.editTextTextPassword);
        botonLogin = findViewById(R.id.botonRegister); // Cambia el ID si es necesario

        botonLogin.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = contraseñaEditText.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                loginUser(email, password);
            } else {
                Toast.makeText(LoginActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
        if (mAuth.getCurrentUser() != null) {
            // Si el usuario está autenticado, obtenemos el token de FCM y lo guardamos
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult();
                        // Guardar el token en Firestore
                        String userId = mAuth.getCurrentUser().getUid();
                        db.collection("usuarios").document(userId).update("deviceToken", token)
                                .addOnSuccessListener(aVoid -> {
                                    // Token guardado correctamente
                                })
                                .addOnFailureListener(e -> {
                                    // Maneja errores
                                });
                    });
        }

    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso
                        Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                        // Redirigir a la actividad del mapa
                        Intent intent = new Intent(LoginActivity.this, MapaActivity.class);
                        startActivity(intent);
                        finish(); // Finaliza la actividad de inicio de sesión para evitar que el usuario regrese a ella
                    } else {
                        // Si falla el inicio de sesión, analiza el error
                        String errorMessage = "Error al iniciar sesión. Inténtalo de nuevo.";
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            errorMessage = "El usuario no existe o está deshabilitado.";
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Credenciales incorrectas. Por favor verifica tu correo y contraseña.";
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "El correo ya está en uso por otra cuenta.";
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

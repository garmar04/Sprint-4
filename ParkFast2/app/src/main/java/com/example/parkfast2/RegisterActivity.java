package com.example.parkfast2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText nombreEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText contraseñaEditText;
    private Button botonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Inicializa FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Enlaza los elementos de la interfaz
        nombreEditText = findViewById(R.id.nombre);
        emailEditText = findViewById(R.id.email);
        contraseñaEditText = findViewById(R.id.contraseña);
        botonRegister = findViewById(R.id.botonRegister);

        // Configura el botón de registro
        botonRegister.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String nombre = nombreEditText.getText().toString();
            String contraseña = contraseñaEditText.getText().toString();

            // Validar los campos
            if (!email.isEmpty() && !contraseña.isEmpty() && !nombre.isEmpty()) {
                registerUser(email, nombre, contraseña);
            } else {
                Toast.makeText(RegisterActivity.this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para registrar un usuario en Firebase
    private void registerUser(String email, String nombre, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Agregar usuario a Firestore
                            agregarUsuarioAFirestore(user, nombre, email);
                        }
                    } else {
                        // Si falla el registro, muestra el error
                        Toast.makeText(RegisterActivity.this, "Error al registrar el usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para agregar el usuario a Firestore
    private void agregarUsuarioAFirestore(FirebaseUser user, String nombre, String email) {
        // Crear un mapa con los datos del usuario
        Map<String, Object> usuarioData = new HashMap<>();
        usuarioData.put("correo", email);
        usuarioData.put("metodoAutenticacion", "correo electrónico/contraseña");
        usuarioData.put("nombre", nombre);
        usuarioData.put("tarjetaActiva", false);

        // Guardar los datos en Firestore
        db.collection("usuarios").document(user.getUid())
                .set(usuarioData)
                .addOnSuccessListener(aVoid -> {
                    // Datos guardados con éxito
                    Toast.makeText(RegisterActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();

                    // Redirigir al usuario a LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Termina la actividad de registro
                })
                .addOnFailureListener(e -> {
                    // Manejar errores al guardar en Firestore
                    Toast.makeText(RegisterActivity.this, "Error al registrar usuario en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

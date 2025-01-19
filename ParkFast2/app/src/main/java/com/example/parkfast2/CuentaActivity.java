package com.example.parkfast2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CuentaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;

    private Button buttonEliminarCuenta, buttonCambiarContrasena, buttonCerrarSesion;
    private TextView tvEmail, tvNombre;
    private TextView estadoTarjeta;

    // Añadir los ImageView para la navegación
    private ImageView toMapa, toReservas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cuenta);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (mUser == null) {
            Toast.makeText(this, "Error: usuario no logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Inicializar vistas
        tvEmail = findViewById(R.id.tvEmail);
        tvNombre = findViewById(R.id.tvNombre);
        estadoTarjeta = findViewById(R.id.estadoTarjeta);

        buttonEliminarCuenta = findViewById(R.id.buttonEliminarCuenta);
        buttonCambiarContrasena = findViewById(R.id.buttonCambiarContrasena);
        buttonCerrarSesion = findViewById(R.id.button);

        // Inicializar los ImageView
        toMapa = findViewById(R.id.toMapa);
        toReservas = findViewById(R.id.toReservas);

        // Configurar los listeners para los ImageView
        toMapa.setOnClickListener(v -> openMapaActivity());
        toReservas.setOnClickListener(v -> openReservasActivity());

        // Cargar los datos desde Firestore
        cargarDatosUsuario();
        cargarEstadoTarjeta();

        // Configurar botones
        buttonEliminarCuenta.setOnClickListener(v -> showDeleteAccountDialog());
        buttonCambiarContrasena.setOnClickListener(v -> showChangePasswordDialog());
        buttonCerrarSesion.setOnClickListener(v -> logoutUser());
    }

    private void cargarEstadoTarjeta() {
        String userId = mUser.getUid();
        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean tarjetaActiva = documentSnapshot.getBoolean("tarjetaActiva");
                        if (tarjetaActiva != null) {
                            estadoTarjeta.setText(tarjetaActiva ? "Estado tarjeta: Activada" : "Estado tarjeta: Desactivada");
                        } else {
                            estadoTarjeta.setText("Desconocido");
                        }
                    } else {
                        Toast.makeText(CuentaActivity.this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CuentaActivity.this, "Error al cargar estado de la tarjeta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    estadoTarjeta.setText("Error");
                });
    }

    private void cargarDatosUsuario() {
        String userId = mUser.getUid();

        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("correo");
                        String nombre = documentSnapshot.getString("nombre");

                        tvEmail.setText(email != null ? email : "Sin correo");
                        tvNombre.setText(nombre != null ? nombre : "Sin nombre");
                    } else {
                        Toast.makeText(CuentaActivity.this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(CuentaActivity.this, "Error al cargar los datos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openMapaActivity() {
        Intent intent = new Intent(CuentaActivity.this, MapaActivity.class);
        startActivity(intent);
    }

    private void openReservasActivity() {
        Intent intent = new Intent(CuentaActivity.this, ReservasActivity.class);
        startActivity(intent);
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showDeleteAccountDialog() {
        EditText editTextPassword = new EditText(this);
        editTextPassword.setHint("Introduce tu contraseña");

        new AlertDialog.Builder(this)
                .setTitle("Eliminar cuenta")
                .setMessage("Por favor, ingresa tu contraseña para eliminar la cuenta.")
                .setView(editTextPassword)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String password = editTextPassword.getText().toString().trim();
                    if (!password.isEmpty()) {
                        deleteAccount(password);
                    } else {
                        Toast.makeText(CuentaActivity.this, "La contraseña es obligatoria.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteAccount(String password) {
        if (mUser.getEmail() != null) {
            mUser.reauthenticate(EmailAuthProvider.getCredential(mUser.getEmail(), password))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mUser.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(CuentaActivity.this, "Cuenta eliminada con éxito.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(CuentaActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(CuentaActivity.this, "Error al eliminar la cuenta.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(CuentaActivity.this, "Contraseña incorrecta.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(CuentaActivity.this, "No se pudo obtener el email del usuario.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        final EditText inputOldPassword = new EditText(this);
        inputOldPassword.setHint("Contraseña antigua");
        layout.addView(inputOldPassword);

        final EditText inputNewPassword = new EditText(this);
        inputNewPassword.setHint("Contraseña nueva");
        layout.addView(inputNewPassword);

        new AlertDialog.Builder(this)
                .setTitle("Cambiar contraseña")
                .setMessage("Introduce tu contraseña antigua y la nueva.")
                .setView(layout)
                .setPositiveButton("Cambiar", (dialog, which) -> {
                    String oldPassword = inputOldPassword.getText().toString().trim();
                    String newPassword = inputNewPassword.getText().toString().trim();

                    if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(CuentaActivity.this, "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
                    } else {
                        changePassword(oldPassword, newPassword);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void changePassword(String oldPassword, String newPassword) {
        if (mUser.getEmail() != null) {
            mUser.reauthenticate(EmailAuthProvider.getCredential(mUser.getEmail(), oldPassword))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mUser.updatePassword(newPassword).addOnCompleteListener(passwordTask -> {
                                if (passwordTask.isSuccessful()) {
                                    Toast.makeText(CuentaActivity.this, "Contraseña cambiada correctamente.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(CuentaActivity.this, "Error al cambiar la contraseña.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(CuentaActivity.this, "Contraseña antigua incorrecta.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

package com.example.parkfast2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001; // Código de solicitud para Google Sign-In
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configuración de Google Sign-In
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de usar el ID de cliente adecuado desde Firebase
                .requestEmail()
                .build());

        Button botonLogin = findViewById(R.id.botonLogin);
        Button botonRegister = findViewById(R.id.botonRegister);

        // Configurar el listener para el clic de Login
        botonLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Configurar el listener para el clic de Register
        botonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Configurar el listener para el clic del botón de Google Sign-In
        findViewById(R.id.buttonGoogleLogin).setOnClickListener(v -> signInWithGoogle());

    }
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado de Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); // Autenticación con Firebase usando el token de Google
            } catch (ApiException e) {
                Toast.makeText(MainActivity.this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Inicio de sesión exitoso
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            agregarUsuarioAFirestore(user); // Agregar usuario a Firestore
                        }
                    } else {
                        // Si ocurre un error al autenticar
                        Toast.makeText(MainActivity.this, "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void agregarUsuarioAFirestore(FirebaseUser user) {
        // Crear una referencia al documento del usuario en Firestore
        DocumentReference userRef = db.collection("usuarios").document(user.getUid());
        String nombreUsuario = user.getDisplayName();


        // Comprobar si el documento del usuario ya existe
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                // Si el documento no existe, significa que es un nuevo usuario
                // Crear un mapa con los datos del usuario
                Map<String, Object> usuarioData = new HashMap<>();
                usuarioData.put("correo", user.getEmail());
                usuarioData.put("metodoAutenticacion", "Google");
                usuarioData.put("nombre", user.getDisplayName());
                usuarioData.put("tarjetaActiva", false);  // Establecer tarjetaActiva a false si es nuevo

                // Guardar los datos en Firestore
                userRef.set(usuarioData)
                        .addOnSuccessListener(aVoid -> {
                            // Datos guardados con éxito
                            Toast.makeText(MainActivity.this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();

                            // Redirigir al usuario a la pantalla principal (MapaActivity)
                            Intent intent = new Intent(MainActivity.this, MapaActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Manejar errores al guardar en Firestore
                            Toast.makeText(MainActivity.this, "Error al registrar usuario: ", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Si el documento ya existe, no hacemos nada y simplemente mostramos un mensaje
                Toast.makeText(MainActivity.this, "Bienvenido de nuevo, " + nombreUsuario, Toast.LENGTH_SHORT).show();

                // Redirigir al usuario a la pantalla principal (MapaActivity)
                Intent intent = new Intent(MainActivity.this, MapaActivity.class);
                startActivity(intent);
                finish();
            }
        }).addOnFailureListener(e -> {
            // Manejar errores al verificar si el usuario ya existe
            Toast.makeText(MainActivity.this, "Error al verificar usuario en Firestore: ", Toast.LENGTH_SHORT).show();
        });
    }
}

package com.example.parkfast2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReservasActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button btnSinTarjeta;
    private TextView textoSinTarjeta;
    private RecyclerView recyclerView;
    private ReservasAdapter adapter;
    private List<Reserva> listaReservas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservas);

        // Inicializa Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializa elementos de la interfaz
        btnSinTarjeta = findViewById(R.id.btnSinTarjeta);
        textoSinTarjeta = findViewById(R.id.textoSinTarjeta);
        recyclerView = findViewById(R.id.recyclerViewReservas);
        ImageView lupaImageView = findViewById(R.id.lupa);
        ImageView cuentaImageView = findViewById(R.id.cuenta);

        // Configura el RecyclerView
        listaReservas = new ArrayList<>();
        adapter = new ReservasAdapter(listaReservas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar navegación con los iconos inferiores
        lupaImageView.setOnClickListener(v -> {
            Intent intent = new Intent(ReservasActivity.this, MapaActivity.class);
            startActivity(intent);
        });

        cuentaImageView.setOnClickListener(v -> {
            Intent intent = new Intent(ReservasActivity.this, CuentaActivity.class);
            startActivity(intent);
        });

        // Obtén el usuario actual
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean tarjetaActiva = documentSnapshot.getBoolean("tarjetaActiva");

                if (tarjetaActiva != null && !tarjetaActiva) {
                    // Mostrar mensaje para activar la tarjeta
                    btnSinTarjeta.setVisibility(View.VISIBLE);
                    textoSinTarjeta.setVisibility(View.VISIBLE);
                    btnSinTarjeta.setOnClickListener(v -> activarTarjeta(userId));
                } else {
                    // Ocultar mensaje y cargar reservas
                    btnSinTarjeta.setVisibility(View.GONE);
                    textoSinTarjeta.setVisibility(View.GONE);
                    cargarReservas(userId);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ReservasActivity.this, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show();
        });
    }

    private void activarTarjeta(String userId) {
        db.collection("usuarios").document(userId).update("tarjetaActiva", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "¡Tarjeta activada con éxito!", Toast.LENGTH_SHORT).show();
                    // Crear campos adicionales
                    db.collection("usuarios").document(userId).update("IDTarjeta", "4884722EE6480", "reservaActiva", false)
                            .addOnSuccessListener(aVoid2 -> {
                                // Ocultar el botón y mensaje
                                btnSinTarjeta.setVisibility(View.GONE);
                                textoSinTarjeta.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al crear campos adicionales", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al activar la tarjeta", Toast.LENGTH_SHORT).show());
    }

    private void cargarReservas(String userId) {
        db.collection("usuarios").document(userId).collection("reservas")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        listaReservas.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Reserva reserva = doc.toObject(Reserva.class);
                            listaReservas.add(reserva);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

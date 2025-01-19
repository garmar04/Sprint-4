package com.example.parkfast2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ParkingManager {

    private FirebaseFirestore db;

    public ParkingManager() {
        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
    }

    public void verificarReserva(String idTarjeta, Callback callback) {
        db.collection("usuarios")
                .whereEqualTo("IDTarjeta", idTarjeta)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        boolean reservaActiva = document.getBoolean("reservaActiva");
                        if (reservaActiva) {
                            // Reserva activa
                            callback.onResultado("reservaActiva:true");
                        } else {
                            // Reserva no activa
                            callback.onResultado("reservaActiva:false");
                        }
                    } else {
                        // ID no encontrado en la base de datos
                        callback.onResultado("ID no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    // Error en la conexión
                    callback.onResultado("Error: " + e.getMessage());
                });
    }

    public interface Callback {
        void onResultado(String mensaje);
    }
}

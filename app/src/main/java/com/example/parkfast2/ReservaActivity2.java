package com.example.parkfast2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReservaActivity2 extends AppCompatActivity {

    private DatePicker datePickerLlegada, datePickerSalida;
    private TimePicker timePickerLlegada, timePickerSalida;
    private Button btnConfirmarReserva;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar la localización al español (España)
        Locale locale = new Locale("es", "ES");
        Locale.setDefault(locale);
        getResources().getConfiguration().setLocale(locale);

        setContentView(R.layout.activity_reserva2);

        // Inicializar los componentes
        datePickerLlegada = findViewById(R.id.datePickerLlegada2);
        timePickerLlegada = findViewById(R.id.timePickerLlegada2);
        datePickerSalida = findViewById(R.id.datePickerSalida2);
        timePickerSalida = findViewById(R.id.timePickerSalida2);
        btnConfirmarReserva = findViewById(R.id.btnConfirmarReserva2);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configurar el formato de 24 horas para los TimePickers
        timePickerLlegada.setIs24HourView(true);
        timePickerSalida.setIs24HourView(true);

        // Configurar el botón de confirmación
        btnConfirmarReserva.setOnClickListener(v -> {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("usuarios").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                boolean tarjetaActiva = document.getBoolean("tarjetaActiva");
                                if (!tarjetaActiva) {
                                    Toast.makeText(ReservaActivity2.this, "Necesitas comprar nuestra tarjeta para poder realizar reservas", Toast.LENGTH_LONG).show();
                                } else {
                                    confirmarReserva(userId);
                                }
                            }
                        }
                    });
        });
    }

    private void confirmarReserva(String userId) {
        int yearLlegada = datePickerLlegada.getYear();
        int monthLlegada = datePickerLlegada.getMonth();
        int dayOfMonthLlegada = datePickerLlegada.getDayOfMonth();
        int hourLlegada = timePickerLlegada.getHour();
        int minuteLlegada = timePickerLlegada.getMinute();

        int yearSalida = datePickerSalida.getYear();
        int monthSalida = datePickerSalida.getMonth();
        int dayOfMonthSalida = datePickerSalida.getDayOfMonth();
        int hourSalida = timePickerSalida.getHour();
        int minuteSalida = timePickerSalida.getMinute();

        if (isValidReservation(yearLlegada, monthLlegada, dayOfMonthLlegada, hourLlegada, minuteLlegada,
                yearSalida, monthSalida, dayOfMonthSalida, hourSalida, minuteSalida)) {

            Calendar llegada = Calendar.getInstance();
            llegada.set(yearLlegada, monthLlegada, dayOfMonthLlegada, hourLlegada, minuteLlegada);

            Calendar salida = Calendar.getInstance();
            salida.set(yearSalida, monthSalida, dayOfMonthSalida, hourSalida, minuteSalida);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String fechaHoraLlegada = sdf.format(llegada.getTime());
            String fechaHoraSalida = sdf.format(salida.getTime());

            Reserva reserva = new Reserva("pendiente", fechaHoraLlegada, fechaHoraSalida, "Parking 2");

            db.collection("usuarios").document(userId).collection("reservas").add(reserva)
                    .addOnSuccessListener(documentReference -> {
                        db.collection("usuarios").document(userId).update("reservaActiva", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ReservaActivity2.this, "Reserva realizada con éxito", Toast.LENGTH_LONG).show();
                                    // Enviar la notificación
                                    sendNotification(userId, "Parking 2", fechaHoraLlegada, fechaHoraSalida);
                                    Intent intent = new Intent(ReservaActivity2.this, ReservasActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(ReservaActivity2.this, "Error al actualizar el estado de la reserva.", Toast.LENGTH_LONG).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(ReservaActivity2.this, "Error al realizar la reserva: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            Toast.makeText(ReservaActivity2.this, "La fecha de salida no puede ser antes de la fecha de llegada.", Toast.LENGTH_LONG).show();
        }
    }
    private void sendNotification(String userId, String parking, String fechaHoraLlegada, String fechaHoraSalida) {
        // Obtener el token del dispositivo del usuario
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    String token = task.getResult();

                    // Construir el mensaje para enviar
                    String message = "Has reservado el " + parking + " desde " + fechaHoraLlegada + " hasta " + fechaHoraSalida;

                    // Enviar la notificación a través de FCM
                    if (token != null) {
                        RemoteMessage messageData = new RemoteMessage.Builder(token)
                                .setMessageId("2")
                                .addData("title", "Reserva Confirmada")
                                .addData("body", message)
                                .build();

                        FirebaseMessaging.getInstance().send(messageData);
                    }
                });
    }

    private boolean isValidReservation(int yearLlegada, int monthLlegada, int dayOfMonthLlegada, int hourLlegada, int minuteLlegada,
                                       int yearSalida, int monthSalida, int dayOfMonthSalida, int hourSalida, int minuteSalida) {
        long llegadaMillis = new java.util.GregorianCalendar(yearLlegada, monthLlegada, dayOfMonthLlegada, hourLlegada, minuteLlegada).getTimeInMillis();
        long salidaMillis = new java.util.GregorianCalendar(yearSalida, monthSalida, dayOfMonthSalida, hourSalida, minuteSalida).getTimeInMillis();
        return llegadaMillis < salidaMillis;
    }
}

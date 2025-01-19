package com.example.parkfast2;

import com.google.firebase.firestore.PropertyName;

public class Reserva {
    private String estado;
    private String fechaHoraLlegada;
    private String fechaHoraSalida;
    private String parking;

    // Constructor sin argumentos requerido por Firestore
    public Reserva() {
    }

    // Constructor con todos los campos
    public Reserva(String estado, String fechaHoraLlegada, String fechaHoraSalida, String parking) {
        this.estado = estado;
        this.fechaHoraLlegada = fechaHoraLlegada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.parking = parking;
    }

    // Getters y setters
    @PropertyName("estado")
    public String getEstado() {
        return estado;
    }

    @PropertyName("estado")
    public void setEstado(String estado) {
        this.estado = estado;
    }

    @PropertyName("fechaHoraLlegada")
    public String getFechaHoraLlegada() {
        return fechaHoraLlegada;
    }

    @PropertyName("fechaHoraLlegada")
    public void setFechaHoraLlegada(String fechaHoraLlegada) {
        this.fechaHoraLlegada = fechaHoraLlegada;
    }

    @PropertyName("fechaHoraSalida")
    public String getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    @PropertyName("fechaHoraSalida")
    public void setFechaHoraSalida(String fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    @PropertyName("parking")
    public String getParking() {
        return parking;
    }

    @PropertyName("parking")
    public void setParking(String parking) {
        this.parking = parking;
    }
}
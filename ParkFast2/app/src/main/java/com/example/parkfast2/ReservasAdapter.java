package com.example.parkfast2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReservasAdapter extends RecyclerView.Adapter<ReservasAdapter.ReservaViewHolder> {

    private List<Reserva> reservas;

    public ReservasAdapter(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.tvEstado.setText("Estado: " + reserva.getEstado());
        holder.tvFechaHoraLlegada.setText("Llegada: " + reserva.getFechaHoraLlegada());
        holder.tvFechaHoraSalida.setText("Salida: " + reserva.getFechaHoraSalida());
        holder.tvParking.setText("Parking: " + reserva.getParking());
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView tvEstado, tvFechaHoraLlegada, tvFechaHoraSalida, tvParking;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvFechaHoraLlegada = itemView.findViewById(R.id.tvFechaHoraLlegada);
            tvFechaHoraSalida = itemView.findViewById(R.id.tvFechaHoraSalida);
            tvParking = itemView.findViewById(R.id.tvParking);
        }
    }
}

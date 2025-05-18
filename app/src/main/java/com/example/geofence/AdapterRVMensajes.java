package com.example.geofence;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterRVMensajes extends RecyclerView.Adapter<AdapterRVMensajes.MensajeHolder> {

    private List<MensajeVO> lstMensajes;
    private String miNombre;

    public AdapterRVMensajes(List<MensajeVO> lstMensajes, String miNombre) {
        this.lstMensajes = lstMensajes;
        this.miNombre = miNombre;
    }

    @NonNull
    @Override
    public MensajeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_mensaje, viewGroup, false);
        return new MensajeHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeHolder holder, int position) {
        MensajeVO mensaje = lstMensajes.get(position);
        holder.mensaje.setText(mensaje.getMensaje());
        holder.nombre.setText(mensaje.getOrigen());

        // Ajustar alineación
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.burbuja.getLayoutParams();

        if (mensaje.getOrigen().equals(miNombre)) {
            holder.contenedor.setGravity(Gravity.END);
            params.setMargins(50, 5, 0, 5);
        } else {
            holder.contenedor.setGravity(Gravity.START);
            params.setMargins(0, 5, 50, 5);
        }

        holder.burbuja.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return lstMensajes.size();
    }

    class MensajeHolder extends RecyclerView.ViewHolder {

        private final TextView mensaje;
        private final TextView nombre;
        private final LinearLayout burbuja;
        private final LinearLayout contenedor;

        public MensajeHolder(@NonNull View itemView) {
            super(itemView);
            mensaje = itemView.findViewById(R.id.mensaje);
            nombre = itemView.findViewById(R.id.nombre);
            burbuja = itemView.findViewById(R.id.burbuja);
            contenedor = itemView.findViewById(R.id.mensajeContainer);
        }
    }
}

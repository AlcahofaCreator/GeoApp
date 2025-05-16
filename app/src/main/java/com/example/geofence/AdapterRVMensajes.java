package com.example.geofence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterRVMensajes extends RecyclerView.Adapter<AdapterRVMensajes.MensajeHolder> {

    private List<MensajeVO> lstMensajes;

    public AdapterRVMensajes(List<MensajeVO> lstMensajes) {
        this.lstMensajes = lstMensajes;
    }

    @NonNull
    @Override
    public MensajeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_mensaje, viewGroup, false);
        return new MensajeHolder(mView);

    }

    @Override
    public void onBindViewHolder(@NonNull MensajeHolder mensajeHolder, int position) {
        mensajeHolder.mensaje.setText(lstMensajes.get(position).getMensaje());
        mensajeHolder.nombre.setText(lstMensajes.get(position).getNombre());
    }

    @Override
    public int getItemCount() {
        return lstMensajes.size();
    }

    class MensajeHolder extends RecyclerView.ViewHolder {

        private TextView mensaje;
        private TextView nombre;
        public MensajeHolder(@NonNull View itemView) {
            super(itemView);
            mensaje = itemView.findViewById(R.id.mensaje);
            nombre = itemView.findViewById(R.id.nombre);
        }
    }
}

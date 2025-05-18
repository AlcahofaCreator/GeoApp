package com.example.geofence;

public class MensajeVO {

    private String mensaje;
    private String origen;
    private String destino;
    private int numeroMensaje;

    // Getter y setter
    public int getNumeroMensaje() {
        return numeroMensaje;
    }

    public void setNumeroMensaje(int numeroMensaje) {
        this.numeroMensaje = numeroMensaje;
    }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public MensajeVO() {}

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

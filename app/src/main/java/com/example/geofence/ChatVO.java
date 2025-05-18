package com.example.geofence;

public class ChatVO {
    private String nombre;
    private String id; // <--- esta será la UID de Firebase
    private boolean mostrar = false;

    public ChatVO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isMostrar() {
        return mostrar;
    }

    public void setMostrar(boolean mostrar) {
        this.mostrar = mostrar;
    }
}


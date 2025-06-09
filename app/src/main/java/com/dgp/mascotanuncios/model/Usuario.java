package com.dgp.mascotanuncios.model;

public class Usuario {
    private String id;
    private String nombre;
    private String telefono;
    private String id_criadero;

    public Usuario() {}

    public Usuario(String id, String nombre, String telefono, String id_criadero) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.id_criadero = id_criadero;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getId_criadero() {
        return id_criadero;
    }
}

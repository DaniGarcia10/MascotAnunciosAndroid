package com.dgp.mascotanuncios.model;

public class Criadero {
    private String id;
    private String nombre;
    private String nucleo_zoologico;
    private String foto_perfil;
    private String ubicacion;
    private String fecha_registro;
    private Boolean verificado;

    // Constructor vacío requerido por Firebase
    public Criadero() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNucleo_zoologico() { return nucleo_zoologico; }
    public void setNucleo_zoologico(String nucleo_zoologico) { this.nucleo_zoologico = nucleo_zoologico; }

    public String getFoto_perfil() { return foto_perfil; }
    public void setFoto_perfil(String foto_perfil) { this.foto_perfil = foto_perfil; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getFecha_registro() { return fecha_registro; }
    public void setFecha_registro(String fecha_registro) { this.fecha_registro = fecha_registro; }

    public Boolean getVerificado() { return verificado; }
    public void setVerificado(Boolean verificado) { this.verificado = verificado; }
}

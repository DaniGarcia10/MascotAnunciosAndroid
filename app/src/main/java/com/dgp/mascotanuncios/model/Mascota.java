package com.dgp.mascotanuncios.model;

import java.util.List;

public class Mascota {
    private String id;
    private String nombre;
    private Boolean perro;
    private String raza;
    private String color;
    private String sexo; // "Macho" o "Hembra"
    private String descripcion;
    private List<String> imagenes;
    private String id_padre;
    private String id_madre;
    private String id_usuario;

    // Constructor vacío requerido por Firebase
    public Mascota() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Boolean getPerro() { return perro; }
    public void setPerro(Boolean perro) { this.perro = perro; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }

    public String getId_padre() { return id_padre; }
    public void setId_padre(String id_padre) { this.id_padre = id_padre; }

    public String getId_madre() { return id_madre; }
    public void setId_madre(String id_madre) { this.id_madre = id_madre; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }
}

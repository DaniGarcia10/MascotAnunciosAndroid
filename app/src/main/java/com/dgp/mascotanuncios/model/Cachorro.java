package com.dgp.mascotanuncios.model;

import java.util.List;

public class Cachorro {
    private String id;
    private String id_anuncio;
    private String color;
    private String sexo; // "Macho" o "Hembra"
    private Double precio;
    private Boolean disponible;
    private List<String> imagenes;

    // Constructor vacío requerido por Firebase
    public Cachorro() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getId_anuncio() { return id_anuncio; }
    public void setId_anuncio(String id_anuncio) { this.id_anuncio = id_anuncio; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }
}

package com.dgp.mascotanuncios.model;

import java.util.Date;
import java.util.List;

public class Anuncio {
    private String id;
    private String raza;
    private Boolean perro;
    private String titulo;
    private String descripcion;
    private Date fecha_publicacion;
    private String id_padre;
    private String id_madre;
    private String edad;
    private String id_usuario;
    private Boolean activo;
    private String ubicacion;
    private List<String> imagenes;
    private Boolean especificar_cachorros;
    private Double precio;
    private Boolean destacado;
    private String telefono;

    // Constructor vacío requerido por Firebase
    public Anuncio() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public Boolean getPerro() { return perro; }
    public void setPerro(Boolean perro) { this.perro = perro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFecha_publicacion() { return fecha_publicacion; }
    public void setFecha_publicacion(Date fecha_publicacion) { this.fecha_publicacion = fecha_publicacion; }

    public String getId_padre() { return id_padre; }
    public void setId_padre(String id_padre) { this.id_padre = id_padre; }

    public String getId_madre() { return id_madre; }
    public void setId_madre(String id_madre) { this.id_madre = id_madre; }

    public String getEdad() { return edad; }
    public void setEdad(String edad) { this.edad = edad; }

    public String getId_usuario() { return id_usuario; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public List<String> getImagenes() { return imagenes; }
    public void setImagenes(List<String> imagenes) { this.imagenes = imagenes; }

    public Boolean getEspecificar_cachorros() { return especificar_cachorros; }
    public void setEspecificar_cachorros(Boolean especificar_cachorros) { this.especificar_cachorros = especificar_cachorros; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Boolean getDestacado() { return destacado; }
    public void setDestacado(Boolean destacado) { this.destacado = destacado; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

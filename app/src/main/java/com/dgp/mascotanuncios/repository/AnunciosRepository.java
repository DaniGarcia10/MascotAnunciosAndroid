package com.dgp.mascotanuncios.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dgp.mascotanuncios.model.Anuncio;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnunciosRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference anunciosRef = db.collection("anuncios");

    public interface AnunciosCallback {
        void onSuccess(List<Anuncio> anuncios);
        void onError(Exception e);
    }

    public void obtenerAnuncios(final AnunciosCallback callback) {
        anunciosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Anuncio> lista = new ArrayList<>();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    try {
                        Anuncio anuncio = new Anuncio();
                        anuncio.setId(doc.getId());
                        anuncio.setTitulo(doc.getString("titulo"));
                        anuncio.setDescripcion(doc.getString("descripcion"));
                        anuncio.setUbicacion(doc.getString("ubicacion"));
                        anuncio.setPerro(doc.getBoolean("perro"));
                        anuncio.setRaza(doc.getString("raza"));
                        anuncio.setEdad(doc.getString("edad"));
                        anuncio.setId_padre(doc.getString("id_padre"));
                        anuncio.setId_madre(doc.getString("id_madre"));
                        anuncio.setId_usuario(doc.getString("id_usuario"));
                        anuncio.setActivo(doc.getBoolean("activo"));
                        anuncio.setEspecificar_cachorros(doc.getBoolean("especificar_cachorros"));
                        anuncio.setDestacado(doc.getBoolean("destacado"));
                        anuncio.setTelefono(doc.getString("telefono"));
                        anuncio.setPrecio(doc.getDouble("precio"));

                        // Fecha: Timestamp o String
                        Object fecha = doc.get("fecha_publicacion");
                        if (fecha instanceof Timestamp) {
                            anuncio.setFecha_publicacion(((Timestamp) fecha).toDate());
                        } else if (fecha instanceof String) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                                anuncio.setFecha_publicacion(sdf.parse((String) fecha));
                            } catch (ParseException e) {
                                Log.e("Fecha", "Formato inválido en fecha_publicacion", e);
                                anuncio.setFecha_publicacion(null);
                            }
                        }

                        // Imágenes (guardar directamente los nombres tal como están en Firestore)
                        List<String> imagenes = (List<String>) doc.get("imagenes");
                        if (imagenes != null) {
                            anuncio.setImagenes(imagenes);
                        }

                        lista.add(anuncio);
                    } catch (Exception e) {
                        Log.e("Firestore", "Error al convertir anuncio", e);
                    }
                }
                callback.onSuccess(lista);
            } else {
                callback.onError(task.getException());
            }
        });
    }
}

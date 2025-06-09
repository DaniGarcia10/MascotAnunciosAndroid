package com.dgp.mascotanuncios.service;

import com.dgp.mascotanuncios.model.Anuncio;
import com.dgp.mascotanuncios.model.Cachorro;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

// Puedes definir una interfaz de callback para notificar cuando los datos estén listos
public class AnunciosService {

    public interface AnunciosCallback {
        void onAnunciosCargados(List<Anuncio> anuncios);
        void onError(Exception e);
    }

    public void cargarAnuncios(AnunciosCallback callback) {
        // Aquí cargas los anuncios y los enriqueces con los datos de criadero/usuario
        // Ejemplo básico:
        FirebaseFirestore.getInstance().collection("anuncios")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Anuncio> anuncios = new ArrayList<>();
                List<String> anuncioIds = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    Anuncio anuncio = doc.toObject(Anuncio.class);
                    anuncio.setId(doc.getId());
                    anuncios.add(anuncio);
                    anuncioIds.add(doc.getId());
                }
                // Cargar cachorros para todos los anuncios
                cargarCachorrosParaAnuncios(anuncios, callback);
            })
            .addOnFailureListener(callback::onError);
    }

    private void cargarCachorrosParaAnuncios(List<Anuncio> anuncios, AnunciosCallback callback) {
        if (anuncios.isEmpty()) {
            callback.onAnunciosCargados(anuncios);
            return;
        }
        FirebaseFirestore.getInstance().collection("cachorros")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                // Agrupar cachorros por id_anuncio
                java.util.Map<String, List<Cachorro>> cachorrosPorAnuncio = new java.util.HashMap<>();
                for (var doc : querySnapshot.getDocuments()) {
                    Cachorro cachorro = doc.toObject(Cachorro.class);
                    String idAnuncio = cachorro.getId_anuncio();
                    if (idAnuncio != null) {
                        List<Cachorro> lista = cachorrosPorAnuncio.get(idAnuncio);
                        if (lista == null) {
                            lista = new ArrayList<>();
                            cachorrosPorAnuncio.put(idAnuncio, lista);
                        }
                        lista.add(cachorro);
                    }
                }
                // Asignar cachorros a cada anuncio
                for (Anuncio anuncio : anuncios) {
                    List<Cachorro> cachorros = cachorrosPorAnuncio.get(anuncio.getId());
                    anuncio.setCachorros(cachorros != null ? cachorros : new ArrayList<Cachorro>());
                }
                callback.onAnunciosCargados(anuncios);
            })
            .addOnFailureListener(callback::onError);
    }
}
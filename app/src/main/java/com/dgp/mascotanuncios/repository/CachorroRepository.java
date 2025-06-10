package com.dgp.mascotanuncios.repository;

import com.dgp.mascotanuncios.model.Cachorro;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CachorroRepository {

    public interface CachorrosCallback {
        void onSuccess(List<Cachorro> cachorros);
        void onError(Exception e);
    }

    public void obtenerCachorrosPorAnuncio(String idAnuncio, CachorrosCallback callback) {
        FirebaseFirestore.getInstance().collection("cachorros")
                .whereEqualTo("id_anuncio", idAnuncio)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Cachorro> cachorros = new ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        Cachorro cachorro = doc.toObject(Cachorro.class);
                        cachorro.setId(doc.getId());
                        // Solo guardar los nombres de las imágenes, no URLs
                        List<String> imagenes = (List<String>) doc.get("imagenes");
                        if (imagenes != null) {
                            cachorro.setImagenes(imagenes);
                        }
                        cachorros.add(cachorro);
                    }
                    callback.onSuccess(cachorros);
                })
                .addOnFailureListener(callback::onError);
    }
}

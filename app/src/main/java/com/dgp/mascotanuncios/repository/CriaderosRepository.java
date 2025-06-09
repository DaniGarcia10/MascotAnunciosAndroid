package com.dgp.mascotanuncios.repository;

import com.dgp.mascotanuncios.model.Criadero;
import com.google.firebase.firestore.FirebaseFirestore;

public class CriaderosRepository {
    // En CriaderosRepository.java
    public void obtenerCriaderoPorId(String id, final CriaderoCallback callback) {
        FirebaseFirestore.getInstance().collection("criaderos").document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Criadero criadero = documentSnapshot.toObject(Criadero.class);
                    if (criadero != null) {
                        android.util.Log.d("criadero", "Documento encontrado y convertido: " + criadero.getNombre());
                    } else {
                        android.util.Log.w("criadero", "Documento existe pero criadero es null para id=" + id);
                    }
                    callback.onSuccess(criadero);
                } else {
                    android.util.Log.w("criadero", "Documento no existe para id=" + id);
                    callback.onSuccess(null);
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("criadero", "Error al obtener documento para id=" + id, e);
                callback.onError(e);
            });
    }

    public interface CriaderoCallback {
        void onSuccess(Criadero criadero);
        void onError(Exception e);
    }
}
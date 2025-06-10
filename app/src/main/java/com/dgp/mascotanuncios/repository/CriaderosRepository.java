package com.dgp.mascotanuncios.repository;

import com.dgp.mascotanuncios.model.Criadero;
import com.google.firebase.firestore.FirebaseFirestore;

public class CriaderosRepository {

    public interface CriaderoCallback {
        void onSuccess(Criadero criadero);
        void onError(Exception e);
    }

    public void obtenerCriaderoPorId(String id, CriaderoCallback callback) {
        FirebaseFirestore.getInstance().collection("criaderos")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Criadero criadero = documentSnapshot.toObject(Criadero.class);
                        if (criadero != null) {
                            criadero.setId(documentSnapshot.getId());
                        }
                        // Log extra para depuración
                        String fotoPerfil = documentSnapshot.getString("foto_perfil");
                        android.util.Log.d("IMAGENCRIADERO", "Firestore criadero id: " + documentSnapshot.getId() + " | foto_perfil: " + fotoPerfil);
                        callback.onSuccess(criadero);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onError);
    }
}
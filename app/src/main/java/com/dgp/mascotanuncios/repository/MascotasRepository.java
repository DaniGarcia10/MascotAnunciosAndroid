package com.dgp.mascotanuncios.repository;

import com.dgp.mascotanuncios.model.Mascota;
import com.google.firebase.firestore.FirebaseFirestore;

public class MascotasRepository {

    public interface MascotaCallback {
        void onSuccess(Mascota mascota);
        void onError(Exception e);
    }

    public void obtenerMascotaPorId(String id, MascotaCallback callback) {
        FirebaseFirestore.getInstance().collection("mascotas").document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Mascota mascota = documentSnapshot.toObject(Mascota.class);
                    if (mascota != null) mascota.setId(documentSnapshot.getId());
                    callback.onSuccess(mascota);
                } else {
                    callback.onSuccess(null);
                }
            })
            .addOnFailureListener(callback::onError);
    }
}

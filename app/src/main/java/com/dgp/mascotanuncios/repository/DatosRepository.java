package com.dgp.mascotanuncios.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatosRepository {

    public interface RazasCallback {
        void onRazasObtenidas(List<String> razas);
    }

    public void obtenerRazas(String tipo, final RazasCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("datos").document("razas").get()
            .addOnSuccessListener(documentSnapshot -> {
                List<String> lista = new ArrayList<>();
                if (documentSnapshot.exists()) {
                    if ("perro".equals(tipo)) {
                        lista = (List<String>) documentSnapshot.get("perro");
                    } else if ("gato".equals(tipo)) {
                        lista = (List<String>) documentSnapshot.get("gato");
                    }
                }
                if (lista == null) lista = Collections.emptyList();
                callback.onRazasObtenidas(lista);
            })
            .addOnFailureListener(e -> {
                Log.e("DatosRepository", "Error al obtener razas de " + tipo, e);
                callback.onRazasObtenidas(Collections.emptyList());
            });
    }

    public interface ProvinciasCallback {
        void onProvinciasObtenidas(List<String> provincias);
    }

    public void obtenerProvincias(final ProvinciasCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("datos").document("provincias").get()
            .addOnSuccessListener(documentSnapshot -> {
                List<String> lista = new ArrayList<>();
                if (documentSnapshot.exists()) {
                    lista = (List<String>) documentSnapshot.get("lista");
                }
                if (lista == null) lista = Collections.emptyList();
                callback.onProvinciasObtenidas(lista);
            })
            .addOnFailureListener(e -> {
                Log.e("DatosRepository", "Error al obtener provincias", e);
                callback.onProvinciasObtenidas(Collections.emptyList());
            });
    }
}
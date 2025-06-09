package com.dgp.mascotanuncios.service;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ImagenService {

    private final FirebaseStorage storage;

    public ImagenService() {
        storage = FirebaseStorage.getInstance();
    }

    // Obtener una URL de imagen
    public void obtenerUrlImagen(String rutaStorage, OnSuccessListener<Uri> onSuccess, OnFailureListener onFailure) {
        Log.d("IMAGEN - ImagenService", "📤 Solicitando URL de: " + rutaStorage);
        StorageReference imageRef = storage.getReference().child(rutaStorage);
        imageRef.getDownloadUrl()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(e -> {
                    Log.e("IMAGEN - ImagenService", "❌ Error al obtener URL para: " + rutaStorage, e);
                    onFailure.onFailure(e);
                });
    }


    // Cargar varias URLs de imagen
    public void cargarImagenes(List<String> rutas, OnCompleteListener<List<Uri>> onComplete) {
        List<Task<Uri>> tareas = new ArrayList<>();

        for (String ruta : rutas) {
            StorageReference ref = storage.getReference().child(ruta);
            tareas.add(ref.getDownloadUrl());
        }

        Tasks.whenAllSuccess(tareas)
                .addOnSuccessListener(resultados -> {
                    List<Uri> urls = new ArrayList<>();
                    for (Object resultado : resultados) {
                        urls.add((Uri) resultado);
                    }
                    onComplete.onComplete(Tasks.forResult(urls));
                })
                .addOnFailureListener(e -> onComplete.onComplete(Tasks.forException(e)));
    }

    // Cargar una imagen directamente en un ImageView
    public void cargarEnImageView(Context context, String rutaStorage, ImageView imageView) {
        Log.d("IMAGEN - ImagenService", "Intentando cargar ruta: " + rutaStorage);
        obtenerUrlImagen(rutaStorage,
                uri -> {
                    Log.d("IMAGEN - ImagenService", "URL final obtenida: " + uri.toString());
                    Glide.with(context).load(uri).into(imageView); // Comenta temporalmente
                },
                error -> Log.e("IMAGEN - ImagenService", "Error al cargar imagen: " + rutaStorage, error));
    }


}

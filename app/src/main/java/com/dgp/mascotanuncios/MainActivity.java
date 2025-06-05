package com.dgp.mascotanuncios;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgp.mascotanuncios.adapter.AnuncioAdapter;
import com.dgp.mascotanuncios.model.Anuncio;
import com.dgp.mascotanuncios.repository.AnunciosRepository;
import com.dgp.mascotanuncios.adapter.AnuncioAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnuncioAdapter adapter;
    private final List<Anuncio> listaAnuncios = new ArrayList<>();
    private final AnunciosRepository repo = new AnunciosRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerAnuncios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnuncioAdapter(listaAnuncios);
        recyclerView.setAdapter(adapter);

        cargarAnuncios();
    }

    private void cargarAnuncios() {
        repo.obtenerAnuncios(new AnunciosRepository.AnunciosCallback() {
            @Override
            public void onSuccess(List<Anuncio> anuncios) {
                listaAnuncios.clear();
                listaAnuncios.addAll(anuncios);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firestore", "Error al cargar anuncios", e);
            }
        });
    }
}

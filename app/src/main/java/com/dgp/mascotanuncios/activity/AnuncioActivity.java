package com.dgp.mascotanuncios.activity;

import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgp.mascotanuncios.R;
import com.dgp.mascotanuncios.adapter.AnuncioAdapter;
import com.dgp.mascotanuncios.model.Anuncio;
import com.dgp.mascotanuncios.repository.AnunciosRepository;

import java.util.ArrayList;
import java.util.List;

public class AnuncioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnuncioAdapter adapter;
    private final List<Anuncio> listaAnuncios = new ArrayList<>();
    private final AnunciosRepository repo = new AnunciosRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncio);

        // Ir a MainActivity al pulsar el logo
        ImageView logoApp = findViewById(R.id.logoApp);
        logoApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AnuncioActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

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

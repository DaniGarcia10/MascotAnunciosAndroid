package com.dgp.mascotanuncios.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dgp.mascotanuncios.R;
import com.dgp.mascotanuncios.model.Anuncio;
import com.dgp.mascotanuncios.model.Cachorro;
import com.dgp.mascotanuncios.model.Criadero;
import com.dgp.mascotanuncios.repository.CachorroRepository;
import com.dgp.mascotanuncios.repository.CriaderosRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AnuncioDetailActivity extends AppCompatActivity {
    private String anuncioId;
    private Anuncio anuncio;
    private Criadero criadero;
    private LinearLayout layoutCachorros; // Añadido

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncio_detail);

        anuncioId = getIntent().getStringExtra("anuncio_id");
        if (anuncioId == null) {
            Toast.makeText(this, "Anuncio no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutCachorros = findViewById(R.id.layoutCachorros); // Inicializa el contenedor

        cargarAnuncio();
    }

    // En AnuncioDetailActivity.java, método cargarAnuncio()
    private void cargarAnuncio() {
        FirebaseFirestore.getInstance().collection("anuncios").document(anuncioId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Anuncio anuncio = new Anuncio();
                        anuncio.setId(documentSnapshot.getId());
                        anuncio.setTitulo(documentSnapshot.getString("titulo"));
                        anuncio.setDescripcion(documentSnapshot.getString("descripcion"));
                        anuncio.setUbicacion(documentSnapshot.getString("ubicacion"));
                        anuncio.setPerro(documentSnapshot.getBoolean("perro"));
                        anuncio.setRaza(documentSnapshot.getString("raza"));
                        anuncio.setEdad(documentSnapshot.getString("edad"));
                        anuncio.setId_padre(documentSnapshot.getString("id_padre"));
                        anuncio.setId_madre(documentSnapshot.getString("id_madre"));
                        anuncio.setId_usuario(documentSnapshot.getString("id_usuario"));
                        anuncio.setActivo(documentSnapshot.getBoolean("activo"));
                        anuncio.setEspecificar_cachorros(documentSnapshot.getBoolean("especificar_cachorros"));
                        anuncio.setDestacado(documentSnapshot.getBoolean("destacado"));
                        anuncio.setTelefono(documentSnapshot.getString("telefono"));
                        anuncio.setPrecio(documentSnapshot.getDouble("precio"));

                        // Fecha: Timestamp o String
                        Object fecha = documentSnapshot.get("fecha_publicacion");
                        if (fecha instanceof com.google.firebase.Timestamp) {
                            anuncio.setFecha_publicacion(((com.google.firebase.Timestamp) fecha).toDate());
                        } else if (fecha instanceof String) {
                            try {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault());
                                anuncio.setFecha_publicacion(sdf.parse((String) fecha));
                            } catch (java.text.ParseException e) {
                                anuncio.setFecha_publicacion(null);
                            }
                        }

                        // Imágenes
                        java.util.List<String> imagenes = (java.util.List<String>) documentSnapshot.get("imagenes");
                        if (imagenes != null) {
                            anuncio.setImagenes(imagenes);
                        }

                        // --- Usar CachorroRepository para cargar cachorros ---
                        new CachorroRepository().obtenerCachorrosPorAnuncio(anuncio.getId(), new CachorroRepository.CachorrosCallback() {
                            @Override
                            public void onSuccess(java.util.List<Cachorro> cachorros) {
                                anuncio.setCachorros(cachorros);
                                AnuncioDetailActivity.this.anuncio = anuncio;
                                mostrarDatos();
                            }
                            @Override
                            public void onError(Exception e) {
                                AnuncioDetailActivity.this.anuncio = anuncio;
                                mostrarDatos();
                            }
                        });

                    } else {
                        Toast.makeText(this, "Anuncio no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar anuncio", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void mostrarDatos() {
        // Título, fecha, raza, ubicación, edad, descripción
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        TextView tvFecha = findViewById(R.id.tvFecha);
        TextView tvRaza = findViewById(R.id.tvRaza);
        TextView tvUbicacion = findViewById(R.id.tvUbicacion);
        TextView tvEdad = findViewById(R.id.tvEdad);
        TextView tvDescripcion = findViewById(R.id.tvDescripcion);

        tvTitulo.setText(anuncio.getTitulo());
        tvFecha.setText(getFechaRelativa(anuncio.getFecha_publicacion()));
        tvRaza.setText(anuncio.getRaza());
        tvUbicacion.setText(anuncio.getUbicacion());
        tvEdad.setText(anuncio.getEdad());
        tvDescripcion.setText(anuncio.getDescripcion());

        // Galería de imágenes (solo la principal, puedes expandirlo a ViewPager)
        ImageView ivPrincipal = findViewById(R.id.ivPrincipal);
        if (anuncio.getImagenes() != null && !anuncio.getImagenes().isEmpty()) {
            Glide.with(this)
                .load(anuncio.getImagenes().get(0))
                .placeholder(R.drawable.placeholder)
                .into(ivPrincipal);
        }

        // Precio
        TextView tvPrecio = findViewById(R.id.tvPrecio);
        if (anuncio.getPrecio() != null) {
            int precioInt = anuncio.getPrecio().intValue();
            tvPrecio.setText(precioInt + "€");
        }

        // Teléfono
        Button btnTelefono = findViewById(R.id.btnTelefono);
        btnTelefono.setOnClickListener(v -> {
            Toast.makeText(this, "Teléfono: " + anuncio.getTelefono(), Toast.LENGTH_SHORT).show();
        });

        // Cargar datos del criadero
        if (anuncio.getId_usuario() != null) {
            FirebaseFirestore.getInstance().collection("usuarios").document(anuncio.getId_usuario())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String idCriadero = documentSnapshot.getString("id_criadero");
                    if (idCriadero != null) {
                        new CriaderosRepository().obtenerCriaderoPorId(idCriadero, new CriaderosRepository.CriaderoCallback() {
                            @Override
                            public void onSuccess(Criadero c) {
                                criadero = c;
                                mostrarCriadero();
                            }
                            @Override
                            public void onError(Exception e) {}
                        });
                    }
                });
        }
        // Puedes cargar padre/madre/cachorros aquí

        // Cargar cachorros si existen
        layoutCachorros.removeAllViews();
        if (anuncio.getCachorros() != null && !anuncio.getCachorros().isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (Cachorro cachorro : anuncio.getCachorros()) {
                // Mostrar solo si está disponible
                if (cachorro.getDisponible() == null || !cachorro.getDisponible()) continue;

                View card = inflater.inflate(R.layout.item_cachorro, layoutCachorros, false);

                ImageView ivCachorro = card.findViewById(R.id.ivCachorro);
                TextView tvPrecioCachorro = card.findViewById(R.id.tvPrecioCachorro);
                TextView tvSexoCachorro = card.findViewById(R.id.tvSexoCachorro);
                TextView tvColorCachorro = card.findViewById(R.id.tvColorCachorro);
                ImageView ivSexoIcon = card.findViewById(R.id.ivSexoIcon); // <-- Añadido

                // Imagen del cachorro (si hay imágenes)
                if (cachorro.getImagenes() != null && !cachorro.getImagenes().isEmpty()) {
                    Glide.with(this)
                        .load(cachorro.getImagenes().get(0))
                        .placeholder(R.drawable.placeholder)
                        .into(ivCachorro);
                } else {
                    ivCachorro.setImageResource(R.drawable.placeholder);
                }

                // Precio del cachorro
                if (cachorro.getPrecio() != null) {
                    int precioInt = cachorro.getPrecio().intValue();
                    tvPrecioCachorro.setText(precioInt + "€");
                } else {
                    tvPrecioCachorro.setText("Precio no disponible");
                }

                // Sexo y color
                String sexo = (cachorro.getSexo() != null ? cachorro.getSexo() : "-");
                tvSexoCachorro.setText(sexo);
                tvColorCachorro.setText((cachorro.getColor() != null ? cachorro.getColor() : "-"));

                // Mostrar icono de sexo
                if (sexo.equalsIgnoreCase("macho")) {
                    ivSexoIcon.setImageResource(R.drawable.ic_male);
                    ivSexoIcon.setVisibility(View.VISIBLE);
                } else if (sexo.equalsIgnoreCase("hembra")) {
                    ivSexoIcon.setImageResource(R.drawable.ic_female);
                    ivSexoIcon.setVisibility(View.VISIBLE);
                } else {
                    ivSexoIcon.setVisibility(View.GONE);
                }

                layoutCachorros.addView(card);
            }
        }
    }

    private void mostrarCriadero() {
        if (criadero == null) return;
        TextView tvCriaderoNombre = findViewById(R.id.tvCriaderoNombre);
        TextView tvCriaderoUbicacion = findViewById(R.id.tvCriaderoUbicacion);
        TextView tvCriaderoNucleo = findViewById(R.id.tvCriaderoNucleo);
        TextView tvCriaderoFecha = findViewById(R.id.tvCriaderoFecha);
        ImageView ivCriadero = findViewById(R.id.ivCriadero);
        TextView tvVerificado = findViewById(R.id.verificado);

        tvCriaderoNombre.setText(criadero.getNombre());
        tvCriaderoUbicacion.setText(criadero.getUbicacion());
        tvCriaderoNucleo.setText("Nucleo zoologico: " + criadero.getNucleo_zoologico());
        // Formatear la fecha como "Se unió el dd/mm/aaaa"
        String fechaRegistro = criadero.getFecha_registro();
        if (fechaRegistro != null && !fechaRegistro.isEmpty()) {
            try {
                // Intentar parsear la fecha si viene en formato ISO o yyyy-MM-dd
                java.text.SimpleDateFormat sdfInput;
                if (fechaRegistro.contains("T")) {
                    sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                } else if (fechaRegistro.contains("-")) {
                    sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                } else {
                    sdfInput = null;
                }
                String fechaFormateada = fechaRegistro;
                if (sdfInput != null) {
                    java.util.Date date = sdfInput.parse(fechaRegistro);
                    java.text.SimpleDateFormat sdfOutput = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    fechaFormateada = sdfOutput.format(date);
                }
                tvCriaderoFecha.setText("Se unió el " + fechaFormateada);
            } catch (Exception e) {
                tvCriaderoFecha.setText("Se unió el " + fechaRegistro);
            }
        } else {
            tvCriaderoFecha.setText("");
        }
        if (criadero.getFoto_perfil() != null) {
            Glide.with(this).load(criadero.getFoto_perfil()).placeholder(R.drawable.placeholder).into(ivCriadero);
        }
        // Mostrar el verificado siempre (puedes poner lógica si solo algunos criaderos son verificados)
        tvVerificado.setVisibility(View.VISIBLE);
    }

    // Copiado de AnuncioAdapter para unificar el formato de fecha relativa
    private String getFechaRelativa(java.util.Date fechaPub) {
        if (fechaPub == null) return "";
        long diffMillis = System.currentTimeMillis() - fechaPub.getTime();
        long diffHoras = diffMillis / (1000 * 60 * 60);
        long diffDias = diffMillis / (1000 * 60 * 60 * 24);

        if (diffDias >= 1) {
            return "Hace " + diffDias + (diffDias == 1 ? " día" : " días");
        } else if (diffHoras >= 1) {
            return "Hace " + diffHoras + (diffHoras == 1 ? " hora" : " horas");
        } else {
            return "Hace menos de 1 hora";
        }
    }
}
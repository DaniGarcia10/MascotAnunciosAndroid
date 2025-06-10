package com.dgp.mascotanuncios.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.dgp.mascotanuncios.R;
import com.dgp.mascotanuncios.model.Anuncio;
import com.dgp.mascotanuncios.model.Cachorro;
import com.dgp.mascotanuncios.model.Criadero;
import com.dgp.mascotanuncios.model.Mascota;
import com.dgp.mascotanuncios.repository.CachorroRepository;
import com.dgp.mascotanuncios.repository.CriaderosRepository;
import com.dgp.mascotanuncios.repository.MascotasRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.dgp.mascotanuncios.service.ImagenService;

import java.util.ArrayList;
import java.util.Map;

public class AnuncioDetailActivity extends AppCompatActivity {
    private String anuncioId;
    private Anuncio anuncio;
    private Criadero criadero;
    private LinearLayout layoutCachorros; // Añadido
    private ImageView ivPadre, ivMadre;   // Añadido para fotos de padres
    private ViewPager2 viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private ImageView btnPrev, btnNext, btnFullscreen;
    private static final String TAG_IMG = "IMAGEN - AnuncioDetail";
    private final ImagenService storageHelper = new ImagenService();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncio_detail);

        // Ir a MainActivity al pulsar el logo
        ImageView logoApp = findViewById(R.id.logoApp);
        logoApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AnuncioDetailActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        anuncioId = getIntent().getStringExtra("anuncio_id");
        if (anuncioId == null) {
            Toast.makeText(this, "Anuncio no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutCachorros = findViewById(R.id.layoutCachorros); // Inicializa el contenedor

        // Inicializa los ImageView de los padres
        ivPadre = findViewById(R.id.ivPadre);
        ivMadre = findViewById(R.id.ivMadre);

        // Añadir listeners para mostrar el modal de padre/madre
        ivPadre.setOnClickListener(v -> mostrarModalPadreOMadre("padre"));
        ivMadre.setOnClickListener(v -> mostrarModalPadreOMadre("madre"));

        // Inicializa el ViewPager2 y las flechas
        viewPager = findViewById(R.id.viewPagerAnuncio);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnFullscreen = findViewById(R.id.btnFullscreen);

        btnPrev.setOnClickListener(v -> {
            int prev = viewPager.getCurrentItem() - 1;
            if (prev >= 0) viewPager.setCurrentItem(prev, true);
        });
        btnNext.setOnClickListener(v -> {
            int next = viewPager.getCurrentItem() + 1;
            if (imagePagerAdapter != null && next < imagePagerAdapter.getItemCount())
                viewPager.setCurrentItem(next, true);
        });

        btnFullscreen.setOnClickListener(v -> mostrarImagenFullscreen());

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

        // Galería de imágenes con ViewPager2 (anuncio principal)
        if (anuncio.getImagenes() != null && !anuncio.getImagenes().isEmpty()) {
            imagePagerAdapter = new ImagePagerAdapter(anuncio.getImagenes(), anuncio.getId(), "anuncios", storageHelper, this);
            viewPager.setAdapter(imagePagerAdapter);
            viewPager.setVisibility(View.VISIBLE);
            btnFullscreen.setVisibility(View.VISIBLE);

            // Mostrar/ocultar flechas según el número de imágenes
            if (anuncio.getImagenes().size() > 1) {
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
            } else {
                btnPrev.setVisibility(View.GONE);
                btnNext.setVisibility(View.GONE);
            }
        } else {
            viewPager.setVisibility(View.GONE);
            btnPrev.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            btnFullscreen.setVisibility(View.GONE);
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

        // Cargar cachorros si existen y mostrar título si corresponde
        layoutCachorros.removeAllViews();
        boolean mostrarTituloCachorros = false;
        int cachorrosDisponibles = 0;

        if (anuncio.getCachorros() != null && !anuncio.getCachorros().isEmpty()) {
            // Contar cachorros disponibles
            for (Cachorro cachorro : anuncio.getCachorros()) {
                if (cachorro.getDisponible() != null && cachorro.getDisponible()) {
                    cachorrosDisponibles++;
                }
            }
            // Mostrar título solo si especificar_cachorros es true y hay al menos 1 disponible
            if (anuncio.getEspecificar_cachorros() != null && anuncio.getEspecificar_cachorros() && cachorrosDisponibles > 0) {
                mostrarTituloCachorros = true;
            }
        }

        if (mostrarTituloCachorros) {
            TextView tvTituloCachorros = new TextView(this);
            tvTituloCachorros.setText("Cachorros disponibles");
            tvTituloCachorros.setTextSize(18);
            tvTituloCachorros.setTypeface(tvTituloCachorros.getTypeface(), android.graphics.Typeface.BOLD); // Corregido aquí
            tvTituloCachorros.setPadding(0, 12, 0, 8);
            layoutCachorros.addView(tvTituloCachorros);
        }

        if (anuncio.getCachorros() != null && !anuncio.getCachorros().isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (Cachorro cachorro : anuncio.getCachorros()) {
                // Mostrar solo si está disponible
                if (cachorro.getDisponible() == null || !cachorro.getDisponible()) continue;

                View card = inflater.inflate(R.layout.item_cachorro, layoutCachorros, false);

                // --- Carrusel de imágenes para el cachorro ---
                ViewPager2 viewPagerCachorro = card.findViewById(R.id.viewPagerCachorro);
                ImageView btnPrevCachorro = card.findViewById(R.id.btnPrevCachorro);
                ImageView btnNextCachorro = card.findViewById(R.id.btnNextCachorro);
                ImageView btnFullscreenCachorro = card.findViewById(R.id.btnFullscreenCachorro);

                if (cachorro.getImagenes() != null && !cachorro.getImagenes().isEmpty()) {
                    ImagePagerAdapter adapterCachorro = new ImagePagerAdapter(
                        cachorro.getImagenes(),
                        cachorro.getId_anuncio(),
                        "cachorros",
                        storageHelper,
                        this
                    );
                    viewPagerCachorro.setAdapter(adapterCachorro);
                    viewPagerCachorro.setVisibility(View.VISIBLE);
                    btnFullscreenCachorro.setVisibility(View.VISIBLE);

                    // Mostrar/ocultar flechas según el número de imágenes
                    if (cachorro.getImagenes().size() > 1) {
                        btnPrevCachorro.setVisibility(View.VISIBLE);
                        btnNextCachorro.setVisibility(View.VISIBLE);
                    } else {
                        btnPrevCachorro.setVisibility(View.GONE);
                        btnNextCachorro.setVisibility(View.GONE);
                    }

                    btnPrevCachorro.setOnClickListener(v -> {
                        int prev = viewPagerCachorro.getCurrentItem() - 1;
                        if (prev >= 0) viewPagerCachorro.setCurrentItem(prev, true);
                    });
                    btnNextCachorro.setOnClickListener(v -> {
                        int next = viewPagerCachorro.getCurrentItem() + 1;
                        if (adapterCachorro != null && next < adapterCachorro.getItemCount())
                            viewPagerCachorro.setCurrentItem(next, true);
                    });
                    btnFullscreenCachorro.setOnClickListener(v -> {
                        int pos = viewPagerCachorro.getCurrentItem();
                        String nombreImagen = cachorro.getImagenes().get(pos);
                        String rutaStorage = "cachorros/" + cachorro.getId_anuncio() + "/" + nombreImagen;

                        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                        android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(this);

                        ImageView imageView = new ImageView(this);
                        imageView.setBackgroundColor(android.graphics.Color.BLACK);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                        ));

                        ImageView btnCerrar = new ImageView(this);
                        btnCerrar.setImageResource(R.drawable.ic_x_square);
                        int size = (int) getResources().getDimension(R.dimen.fullscreen_close_size);
                        android.widget.FrameLayout.LayoutParams closeParams = new android.widget.FrameLayout.LayoutParams(size, size);
                        closeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
                        closeParams.topMargin = (int) getResources().getDimension(R.dimen.fullscreen_close_margin);
                        closeParams.rightMargin = (int) getResources().getDimension(R.dimen.fullscreen_close_margin);
                        btnCerrar.setLayoutParams(closeParams);
                        btnCerrar.setPadding(12, 12, 12, 12);
                        btnCerrar.setOnClickListener(v2 -> dialog.dismiss());

                        storageHelper.obtenerUrlImagen(rutaStorage,
                            uri -> Glide.with(this)
                                    .load(uri)
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.placeholder)
                                    .into(imageView),
                            error -> imageView.setImageResource(R.drawable.placeholder)
                        );

                        frameLayout.addView(imageView);
                        frameLayout.addView(btnCerrar);

                        dialog.setContentView(frameLayout);
                        dialog.show();
                    });
                } else {
                    viewPagerCachorro.setVisibility(View.GONE);
                    btnPrevCachorro.setVisibility(View.GONE);
                    btnNextCachorro.setVisibility(View.GONE);
                    btnFullscreenCachorro.setVisibility(View.GONE);
                }
                // --- FIN Carrusel de imágenes para el cachorro ---

                ImageView ivCachorro = card.findViewById(R.id.ivCachorro);
                TextView tvPrecioCachorro = card.findViewById(R.id.tvPrecioCachorro);
                TextView tvSexoCachorro = card.findViewById(R.id.tvSexoCachorro);
                TextView tvColorCachorro = card.findViewById(R.id.tvColorCachorro);
                ImageView ivSexoIcon = card.findViewById(R.id.ivSexoIcon); // <-- Añadido

                // Imagen del cachorro (si hay imágenes)
                if (cachorro.getImagenes() != null && !cachorro.getImagenes().isEmpty()) {
                    String nombreImagenCachorro = cachorro.getImagenes().get(0);
                    // CAMBIO: Usar id_anuncio en la ruta
                    String rutaStorageCachorro = "cachorros/" + cachorro.getId_anuncio() + "/" + nombreImagenCachorro;
                    Log.d(TAG_IMG, "Intentando cargar imagen cachorro: " + nombreImagenCachorro + " | Ruta: " + rutaStorageCachorro);
                    storageHelper.obtenerUrlImagen(rutaStorageCachorro,
                        uri -> {
                            Log.d(TAG_IMG, "URL obtenida para imagen cachorro: " + uri);
                            Glide.with(this)
                                .load(uri)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(ivCachorro);
                        },
                        error -> {
                            Log.e(TAG_IMG, "Error al obtener URL de imagen cachorro: " + nombreImagenCachorro, error);
                            ivCachorro.setImageResource(R.drawable.placeholder);
                            Log.w(TAG_IMG, "Mostrando placeholder para cachorro sin imagen: " + cachorro.getId());
                        }
                    );
                } else {
                    Log.w(TAG_IMG, "Cachorro sin imágenes, usando placeholder. ID: " + cachorro.getId());
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

        // Cargar fotos de los padres debajo de los cachorros
        cargarPadres();
    }

    private void cargarPadres() {
        MascotasRepository mascotasRepository = new MascotasRepository();

        // Obtener referencias a los layouts de las cards de padre y madre
        View padreCard = findViewById(R.id.cardPadre);
        View madreCard = findViewById(R.id.cardMadre);

        // Padre
        if (anuncio.getId_padre() != null && anuncio.getId_padre().isEmpty()) { // <-- Corregido aquí
            Log.d("IMAGENPADRES", "Buscando datos del padre con id: " + anuncio.getId_padre());
            mascotasRepository.obtenerMascotaPorId(anuncio.getId_padre(), new MascotasRepository.MascotaCallback() {
                @Override
                public void onSuccess(Mascota padre) {
                    if (padre != null && padre.getImagenes() != null && !padre.getImagenes().isEmpty()) {
                        String nombreImagenPadre = padre.getImagenes().get(0);
                        String rutaStoragePadre = "mascotas/" + anuncio.getId_usuario() + "/" + nombreImagenPadre;
                        Log.d("IMAGENPADRES", "Intentando cargar imagen del padre: " + nombreImagenPadre + " | Ruta: " + rutaStoragePadre);
                        storageHelper.obtenerUrlImagen(rutaStoragePadre,
                            uri -> {
                                Log.d("IMAGENPADRES", "URL obtenida para imagen del padre: " + uri);
                                Glide.with(AnuncioDetailActivity.this)
                                        .load(uri)
                                        .placeholder(R.drawable.placeholder)
                                        .error(R.drawable.placeholder)
                                        .into(ivPadre);
                            },
                            error -> {
                                Log.e("IMAGENPADRES", "Error al obtener URL de imagen del padre: " + nombreImagenPadre, error);
                                ivPadre.setImageResource(R.drawable.placeholder);
                            }
                        );
                    } else {
                        Log.w("IMAGENPADRES", "El padre no tiene imágenes, usando placeholder.");
                        ivPadre.setImageResource(R.drawable.placeholder);
                    }
                }
                @Override
                public void onError(Exception e) {
                    Log.e("IMAGENPADRES", "Error al obtener datos del padre", e);
                    ivPadre.setImageResource(R.drawable.placeholder);
                }
            });
        } else {
            Log.w("IMAGENPADRES", "No hay id_padre en el anuncio, ocultando card de padre.");
            // Oculta la card del padre
            if (padreCard instanceof View) {
                ((View) padreCard).setVisibility(View.GONE);
            } else {
                ivPadre.setVisibility(View.GONE);
                TextView tvPadre = findViewById(R.id.tvPadre);
                if (tvPadre != null) tvPadre.setVisibility(View.GONE);
            }
        }

        // Madre
        if (anuncio.getId_madre() != null && anuncio.getId_madre().isEmpty()) {
            Log.d("IMAGENPADRES", "Buscando datos de la madre con id: " + anuncio.getId_madre());
            mascotasRepository.obtenerMascotaPorId(anuncio.getId_madre(), new MascotasRepository.MascotaCallback() {
                @Override
                public void onSuccess(Mascota madre) {
                    if (madre != null && madre.getImagenes() != null && !madre.getImagenes().isEmpty()) {
                        String nombreImagenMadre = madre.getImagenes().get(0);
                        String rutaStorageMadre = "mascotas/" + anuncio.getId_usuario() + "/" + nombreImagenMadre;
                        Log.d("IMAGENPADRES", "Intentando cargar imagen de la madre: " + nombreImagenMadre + " | Ruta: " + rutaStorageMadre);
                        storageHelper.obtenerUrlImagen(rutaStorageMadre,
                            uri -> {
                                Log.d("IMAGENPADRES", "URL obtenida para imagen de la madre: " + uri);
                                Glide.with(AnuncioDetailActivity.this)
                                        .load(uri)
                                        .placeholder(R.drawable.placeholder)
                                        .error(R.drawable.placeholder)
                                        .into(ivMadre);
                            },
                            error -> {
                                Log.e("IMAGENPADRES", "Error al obtener URL de imagen de la madre: " + nombreImagenMadre, error);
                                ivMadre.setImageResource(R.drawable.placeholder);
                            }
                        );
                    } else {
                        Log.w("IMAGENPADRES", "La madre no tiene imágenes, usando placeholder.");
                        ivMadre.setImageResource(R.drawable.placeholder);
                    }
                }
                @Override
                public void onError(Exception e) {
                    Log.e("IMAGENPADRES", "Error al obtener datos de la madre", e);
                    ivMadre.setImageResource(R.drawable.placeholder);
                }
            });
        } else {
            Log.w("IMAGENPADRES", "No hay id_madre en el anuncio, ocultando card de madre.");
            // Oculta la card de la madre
            if (madreCard instanceof View) {
                ((View) madreCard).setVisibility(View.GONE);
            } else {
                ivMadre.setVisibility(View.GONE);
                TextView tvMadre = findViewById(R.id.tvMadre);
                if (tvMadre != null) tvMadre.setVisibility(View.GONE);
            }
        }
    }

    private void mostrarCriadero() {
        if (criadero == null) {
            Log.w("IMAGENCRIADERO", "No hay criadero para mostrar");
            return;
        }
        TextView tvCriaderoNombre = findViewById(R.id.tvCriaderoNombre);
        TextView tvCriaderoUbicacion = findViewById(R.id.tvCriaderoUbicacion);
        TextView tvCriaderoFecha = findViewById(R.id.tvCriaderoFecha);
        ImageView ivCriadero = findViewById(R.id.ivCriadero);
        TextView tvVerificado = findViewById(R.id.verificado);

        tvCriaderoNombre.setText(criadero.getNombre());
        tvCriaderoUbicacion.setText(criadero.getUbicacion());
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
            // LOG extra para depuración
            String nombreImagenCriadero = criadero.getFoto_perfil();
            String rutaStorageCriadero = "criaderos/" + nombreImagenCriadero;
            storageHelper.obtenerUrlImagen(rutaStorageCriadero,
                uri -> {
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(ivCriadero);
                },
                error -> {
                    ivCriadero.setImageResource(R.drawable.placeholder);
                }
            );
        } else {
            Log.w("IMAGENCRIADERO", "El criadero no tiene foto_perfil");
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

    // Adapter para ViewPager2
    private static class ImagePagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private final java.util.List<String> imagenes;
        private final String id;
        private final String tipoCarpeta;
        private final ImagenService storageHelper;
        private final android.content.Context context;

        // Añadido tipoCarpeta para distinguir entre "anuncios" y "cachorros"
        public ImagePagerAdapter(java.util.List<String> imagenes, String id, String tipoCarpeta, ImagenService storageHelper, android.content.Context context) {
            this.imagenes = imagenes != null ? imagenes : new ArrayList<>();
            this.id = id;
            this.tipoCarpeta = tipoCarpeta;
            this.storageHelper = storageHelper;
            this.context = context;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            String nombreImagen = imagenes.get(position);
            String rutaStorage = tipoCarpeta + "/" + id + "/" + nombreImagen;
            storageHelper.obtenerUrlImagen(rutaStorage,
                uri -> Glide.with(context)
                        .load(uri)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imageView),
                error -> holder.imageView.setImageResource(R.drawable.placeholder)
            );
        }

        @Override
        public int getItemCount() {
            return imagenes.size();
        }

        static class ImageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            ImageView imageView;
            public ImageViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }

    private void mostrarImagenFullscreen() {
        if (anuncio == null || anuncio.getImagenes() == null || anuncio.getImagenes().isEmpty()) return;
        int pos = viewPager.getCurrentItem();
        String nombreImagen = anuncio.getImagenes().get(pos);
        String rutaStorage = "anuncios/" + anuncio.getId() + "/" + nombreImagen;

        android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        // Crear un FrameLayout para superponer el botón de cerrar
        android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(this);

        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(android.graphics.Color.BLACK);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ));

        // Botón de cerrar
        ImageView btnCerrar = new ImageView(this);
        btnCerrar.setImageResource(R.drawable.ic_x_square);
        int size = (int) getResources().getDimension(R.dimen.fullscreen_close_size);
        android.widget.FrameLayout.LayoutParams closeParams = new android.widget.FrameLayout.LayoutParams(size, size);
        closeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        closeParams.topMargin = (int) getResources().getDimension(R.dimen.fullscreen_close_margin);
        closeParams.rightMargin = (int) getResources().getDimension(R.dimen.fullscreen_close_margin);
        btnCerrar.setLayoutParams(closeParams);
        btnCerrar.setPadding(12, 12, 12, 12);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        storageHelper.obtenerUrlImagen(rutaStorage,
            uri -> Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imageView),
            error -> imageView.setImageResource(R.drawable.placeholder)
        );

        frameLayout.addView(imageView);
        frameLayout.addView(btnCerrar);

        dialog.setContentView(frameLayout);
        dialog.show();
    }

    // Nuevo método para mostrar el modal de padre o madre
    private void mostrarModalPadreOMadre(String tipo) {
        MascotasRepository mascotasRepository = new MascotasRepository();
        String idMascota = tipo.equals("padre") ? (anuncio != null ? anuncio.getId_padre() : null) : (anuncio != null ? anuncio.getId_madre() : null);
        if (idMascota == null || idMascota.isEmpty()) return;

        mascotasRepository.obtenerMascotaPorId(idMascota, new MascotasRepository.MascotaCallback() {
            @Override
            public void onSuccess(Mascota mascota) {
                android.app.Dialog dialog = new android.app.Dialog(AnuncioDetailActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                android.widget.ScrollView scrollView = new android.widget.ScrollView(AnuncioDetailActivity.this);
                android.widget.LinearLayout layout = new android.widget.LinearLayout(AnuncioDetailActivity.this);
                layout.setOrientation(android.widget.LinearLayout.VERTICAL);
                layout.setPadding(40, 40, 40, 40);
                layout.setBackgroundColor(android.graphics.Color.BLACK);

                ImageView imageView = new ImageView(AnuncioDetailActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                // Limitar el tamaño máximo de la imagen (ejemplo: 280dp de alto)
                int maxHeightPx = (int) (280 * getResources().getDisplayMetrics().density);
                android.widget.LinearLayout.LayoutParams imgParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    maxHeightPx
                );
                imgParams.bottomMargin = 24;
                imageView.setLayoutParams(imgParams);

                // Cargar imagen
                if (mascota.getImagenes() != null && !mascota.getImagenes().isEmpty()) {
                    String nombreImagen = mascota.getImagenes().get(0);
                    String rutaStorage = "mascotas/" + mascota.getId_usuario() + "/" + nombreImagen;
                    storageHelper.obtenerUrlImagen(rutaStorage,
                        uri -> Glide.with(AnuncioDetailActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(imageView),
                        error -> imageView.setImageResource(R.drawable.placeholder)
                    );
                } else {
                    imageView.setImageResource(R.drawable.placeholder);
                }

                // Nombre
                TextView tvNombre = new TextView(AnuncioDetailActivity.this);
                tvNombre.setText(mascota.getNombre());
                tvNombre.setTextColor(android.graphics.Color.WHITE);
                tvNombre.setTextSize(22);
                tvNombre.setTypeface(tvNombre.getTypeface(), android.graphics.Typeface.BOLD);
                tvNombre.setMaxLines(1);
                tvNombre.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvNombre.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                // Raza, color, descripción
                TextView tvRaza = new TextView(AnuncioDetailActivity.this);
                tvRaza.setText("Raza: " + (mascota.getRaza() != null ? mascota.getRaza() : "-"));
                tvRaza.setTextColor(android.graphics.Color.WHITE);
                tvRaza.setMaxLines(1);
                tvRaza.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvRaza.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                TextView tvColor = new TextView(AnuncioDetailActivity.this);
                tvColor.setText("Color: " + (mascota.getColor() != null ? mascota.getColor() : "-"));
                tvColor.setTextColor(android.graphics.Color.WHITE);
                tvColor.setMaxLines(1);
                tvColor.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvColor.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                TextView tvDescripcion = new TextView(AnuncioDetailActivity.this);
                tvDescripcion.setText(mascota.getDescripcion() != null ? mascota.getDescripcion() : "");
                tvDescripcion.setTextColor(android.graphics.Color.WHITE);
                tvDescripcion.setPadding(0, 20, 0, 0);
                tvDescripcion.setMaxLines(5);
                tvDescripcion.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvDescripcion.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                // Botón cerrar
                ImageView btnCerrar = new ImageView(AnuncioDetailActivity.this);
                btnCerrar.setImageResource(R.drawable.ic_x_square);
                int size = (int) getResources().getDimension(R.dimen.fullscreen_close_size);
                android.widget.LinearLayout.LayoutParams closeParams = new android.widget.LinearLayout.LayoutParams(size, size);
                closeParams.gravity = android.view.Gravity.END;
                btnCerrar.setLayoutParams(closeParams);
                btnCerrar.setPadding(12, 12, 12, 12);
                btnCerrar.setOnClickListener(v -> dialog.dismiss());

                layout.addView(btnCerrar);
                layout.addView(imageView);
                layout.addView(tvNombre);
                layout.addView(tvRaza);
                layout.addView(tvColor);
                layout.addView(tvDescripcion);

                scrollView.addView(layout);
                dialog.setContentView(scrollView);
                dialog.show();
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(AnuncioDetailActivity.this, "No se pudo cargar la información", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.dgp.mascotanuncios.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgp.mascotanuncios.R;
import com.dgp.mascotanuncios.adapter.AnuncioAdapter;
import com.dgp.mascotanuncios.model.Anuncio;
import com.dgp.mascotanuncios.repository.AnunciosRepository;
import com.dgp.mascotanuncios.repository.DatosRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnuncioActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnuncioAdapter adapter;
    private final List<Anuncio> listaAnuncios = new ArrayList<>();
    private final AnunciosRepository repo = new AnunciosRepository();
    private boolean filtroVisible = false;
    private TextView btnFiltros;

    // Filtros
    private String filtroTipoAnimal = "";
    private String filtroRaza = "";
    private String filtroProvincia = "";
    private Double filtroPrecioMin = null;
    private Double filtroPrecioMax = null;

    // Para razas dinámicas
    private ArrayAdapter<String> razaAdapter;

    // Elimina arrays hardcodeados de razas y provincias
    // private List<String> razasPerro = new ArrayList<>();
    // private List<String> razasGato = new ArrayList<>();
    // private final String[] provincias = new String[]{ ... };

    private final DatosRepository datosRepository = new DatosRepository();
    private List<String> razasPerro = new ArrayList<>();
    private List<String> razasGato = new ArrayList<>();
    private List<String> provinciasList = new ArrayList<>();

    // Nueva referencia para la card de filtros
    private View filtroCardView;
    private FrameLayout filtrosContainer;

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

        // Leer filtros desde el intent (si vienen de MainActivity)
        Intent intent = getIntent();
        if (intent != null) {
            String tipoAnimal = intent.getStringExtra("filtroTipoAnimal");
            String raza = intent.getStringExtra("filtroRaza");
            if (tipoAnimal != null) filtroTipoAnimal = tipoAnimal;
            if (raza != null) filtroRaza = raza;
        }

        recyclerView = findViewById(R.id.recyclerAnuncios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnuncioAdapter(listaAnuncios);
        recyclerView.setAdapter(adapter);

        // Si hay filtros, aplicar directamente; si no, cargar todos
        if (!filtroTipoAnimal.isEmpty() || !filtroRaza.isEmpty()) {
            aplicarFiltros();
        } else {
            cargarAnuncios();
        }

        // Configurar Spinner de ordenar
        Spinner spinnerOrdenar = findViewById(R.id.spinnerOrdenar);
        ArrayAdapter<String> ordenarAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_bold_small, // Usa el layout personalizado
                new String[]{"Ordenar por...", "Precio: Menor a mayor", "Precio: Mayor a menor", "Más reciente", "Más antiguo"});
        ordenarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrdenar.setAdapter(ordenarAdapter);

        spinnerOrdenar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Solo ordenar si no es la opción "Ordenar por..."
                if (position > 0) {
                    ordenarAnuncios(position - 1);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Cambia Button por TextView
        btnFiltros = findViewById(R.id.btnFiltros);
        btnFiltros.setOnClickListener(v -> toggleFiltro());

        filtrosContainer = findViewById(R.id.filtrosContainer);

        cargarDatosFiltros();
    }

    private void cargarAnuncios() {
        repo.obtenerAnuncios(new AnunciosRepository.AnunciosCallback() {
            @Override
            public void onSuccess(List<Anuncio> anuncios) {
                listaAnuncios.clear();
                listaAnuncios.addAll(anuncios);
                ordenarDestacadosPrimero(listaAnuncios);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e("Firestore", "Error al cargar anuncios", e);
            }
        });
    }

    private void ordenarAnuncios(int position) {
        // 0: Precio menor a mayor, 1: Precio mayor a menor, 2: Más reciente, 3: Más antiguo
        switch (position) {
            case 0:
                Collections.sort(listaAnuncios, new Comparator<Anuncio>() {
                    @Override
                    public int compare(Anuncio a1, Anuncio a2) {
                        Double p1 = a1.getPrecio() != null ? a1.getPrecio() : 0.0;
                        Double p2 = a2.getPrecio() != null ? a2.getPrecio() : 0.0;
                        return Double.compare(p1, p2);
                    }
                });
                break;
            case 1:
                Collections.sort(listaAnuncios, new Comparator<Anuncio>() {
                    @Override
                    public int compare(Anuncio a1, Anuncio a2) {
                        Double p1 = a1.getPrecio() != null ? a1.getPrecio() : 0.0;
                        Double p2 = a2.getPrecio() != null ? a2.getPrecio() : 0.0;
                        return Double.compare(p2, p1);
                    }
                });
                break;
            case 2:
                Collections.sort(listaAnuncios, new Comparator<Anuncio>() {
                    @Override
                    public int compare(Anuncio a1, Anuncio a2) {
                        if (a1.getFecha_publicacion() == null && a2.getFecha_publicacion() == null) return 0;
                        if (a1.getFecha_publicacion() == null) return 1;
                        if (a2.getFecha_publicacion() == null) return -1;
                        return a2.getFecha_publicacion().compareTo(a1.getFecha_publicacion());
                    }
                });
                break;
            case 3:
                Collections.sort(listaAnuncios, new Comparator<Anuncio>() {
                    @Override
                    public int compare(Anuncio a1, Anuncio a2) {
                        if (a1.getFecha_publicacion() == null && a2.getFecha_publicacion() == null) return 0;
                        if (a1.getFecha_publicacion() == null) return 1;
                        if (a2.getFecha_publicacion() == null) return -1;
                        return a1.getFecha_publicacion().compareTo(a2.getFecha_publicacion());
                    }
                });
                break;
        }
        ordenarDestacadosPrimero(listaAnuncios);
        adapter.notifyDataSetChanged();
    }

    private void toggleFiltro() {
        filtroVisible = !filtroVisible;
        if (filtroVisible) {
            btnFiltros.setText("Filtros ▲");
            mostrarCardFiltros();
        } else {
            btnFiltros.setText("Filtros ▼");
            ocultarCardFiltros();
        }
    }

    private void mostrarCardFiltros() {
        if (filtroCardView == null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            filtroCardView = inflater.inflate(R.layout.dialog_filtros, filtrosContainer, false);

            // Cambia fondo a CardView si quieres (opcional)
            CardView card = new CardView(this);
            card.setCardElevation(8f);
            card.setRadius(24f);
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(getResources().getColor(android.R.color.white));
            card.addView(filtroCardView);
            filtrosContainer.removeAllViews();
            filtrosContainer.addView(card);

            // Tipo animal
            Spinner spinnerTipoAnimal = filtroCardView.findViewById(R.id.spinnerTipoAnimal);
            ArrayAdapter<String> tipoAnimalAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"Todos", "Perro", "Gato"});
            tipoAnimalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTipoAnimal.setAdapter(tipoAnimalAdapter);

            // Raza
            Spinner spinnerRaza = filtroCardView.findViewById(R.id.spinnerRaza);
            razaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
            razaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRaza.setAdapter(razaAdapter);

            // Provincia
            Spinner spinnerProvincia = filtroCardView.findViewById(R.id.spinnerProvincia);
            ArrayAdapter<String> provinciaAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, provinciasList);
            provinciaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProvincia.setAdapter(provinciaAdapter);

            // Precio min/max
            EditText editPrecioMin = filtroCardView.findViewById(R.id.editPrecioMin);
            EditText editPrecioMax = filtroCardView.findViewById(R.id.editPrecioMax);

            // Inicializa valores actuales (rellena los filtros con lo recibido del intent)
            spinnerTipoAnimal.setSelection(getTipoAnimalIndex(filtroTipoAnimal));
            // Cargar razas según tipo animal seleccionado
            spinnerTipoAnimal.post(() -> {
                actualizarRazasSpinner(spinnerTipoAnimal, spinnerRaza);
                spinnerRaza.setSelection(getRazaIndex(filtroRaza));
            });
            spinnerProvincia.setSelection(getProvinciaIndex(filtroProvincia));
            editPrecioMin.setText(filtroPrecioMin != null ? String.valueOf(filtroPrecioMin.intValue()) : "");
            editPrecioMax.setText(filtroPrecioMax != null ? String.valueOf(filtroPrecioMax.intValue()) : "");

            spinnerTipoAnimal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    actualizarRazasSpinner(spinnerTipoAnimal, spinnerRaza);
                    // Si el filtro de raza está seleccionado, volver a seleccionarlo tras actualizar razas
                    if (!filtroRaza.isEmpty()) {
                        spinnerRaza.setSelection(getRazaIndex(filtroRaza));
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });

            Button btnAplicar = filtroCardView.findViewById(R.id.btnAplicarFiltros);
            btnAplicar.setOnClickListener(v -> {
                filtroTipoAnimal = spinnerTipoAnimal.getSelectedItemPosition() == 0 ? "" : spinnerTipoAnimal.getSelectedItem().toString().toLowerCase();
                filtroRaza = spinnerRaza.getSelectedItemPosition() == 0 ? "" : spinnerRaza.getSelectedItem().toString();
                filtroProvincia = spinnerProvincia.getSelectedItemPosition() == 0 ? "" : spinnerProvincia.getSelectedItem().toString();
                try {
                    filtroPrecioMin = editPrecioMin.getText().toString().isEmpty() ? null : Double.parseDouble(editPrecioMin.getText().toString());
                } catch (Exception e) { filtroPrecioMin = null; }
                try {
                    filtroPrecioMax = editPrecioMax.getText().toString().isEmpty() ? null : Double.parseDouble(editPrecioMax.getText().toString());
                } catch (Exception e) { filtroPrecioMax = null; }
                aplicarFiltros();
                ocultarCardFiltros();
                btnFiltros.setText("Filtros ▼");
                filtroVisible = false;
            });

            // Lógica para limpiar filtros
            Button btnLimpiar = filtroCardView.findViewById(R.id.btnLimpiarFiltros);
            btnLimpiar.setOnClickListener(v -> {
                spinnerTipoAnimal.setSelection(0);
                actualizarRazasSpinner(spinnerTipoAnimal, spinnerRaza);
                spinnerRaza.setSelection(0);
                spinnerProvincia.setSelection(0);
                editPrecioMin.setText("");
                editPrecioMax.setText("");
                filtroTipoAnimal = "";
                filtroRaza = "";
                filtroProvincia = "";
                filtroPrecioMin = null;
                filtroPrecioMax = null;
            });
        }
        filtrosContainer.setVisibility(View.VISIBLE);
    }

    private void ocultarCardFiltros() {
        if (filtrosContainer != null) {
            filtrosContainer.removeAllViews();
            filtrosContainer.setVisibility(View.GONE);
            filtroCardView = null;
        }
    }

    private void cargarDatosFiltros() {
        // Cargar razas de perro
        datosRepository.obtenerRazas("perro", razas -> {
            razasPerro.clear();
            razasPerro.add("Todas");
            if (razas != null) razasPerro.addAll(razas);
        });
        // Cargar razas de gato
        datosRepository.obtenerRazas("gato", razas -> {
            razasGato.clear();
            razasGato.add("Todas");
            if (razas != null) razasGato.addAll(razas);
        });
        // Cargar provincias
        datosRepository.obtenerProvincias(provs -> {
            provinciasList.clear();
            provinciasList.add("Todas");
            if (provs != null) provinciasList.addAll(provs);
        });
    }

    private void inicializarRazas() {
        // Ya no es necesario, se carga desde Firestore
    }

    private void actualizarRazasSpinner(Spinner spinnerTipoAnimal, Spinner spinnerRaza) {
        String tipo = spinnerTipoAnimal.getSelectedItem().toString().toLowerCase();
        List<String> razas;
        if (tipo.equals("perro")) {
            razas = razasPerro;
        } else if (tipo.equals("gato")) {
            razas = razasGato;
        } else {
            razas = new ArrayList<>();
            razas.add("Todas");
        }
        razaAdapter.clear();
        razaAdapter.addAll(razas);
        razaAdapter.notifyDataSetChanged();
        spinnerRaza.setSelection(0);
    }

    private int getTipoAnimalIndex(String tipo) {
        if (tipo == null || tipo.isEmpty()) return 0;
        if (tipo.equals("perro")) return 1;
        if (tipo.equals("gato")) return 2;
        return 0;
    }
    private int getRazaIndex(String raza) {
        if (raza == null || raza.isEmpty()) return 0;
        for (int i = 0; i < razaAdapter.getCount(); i++) {
            if (razaAdapter.getItem(i).equalsIgnoreCase(raza)) return i;
        }
        return 0;
    }
    private int getProvinciaIndex(String provincia) {
        if (provincia == null || provincia.isEmpty()) return 0;
        for (int i = 0; i < provinciasList.size(); i++) {
            if (provinciasList.get(i).equalsIgnoreCase(provincia)) return i;
        }
        return 0;
    }

    private void aplicarFiltros() {
        repo.obtenerAnuncios(new AnunciosRepository.AnunciosCallback() {
            @Override
            public void onSuccess(List<Anuncio> anuncios) {
                listaAnuncios.clear();
                for (Anuncio a : anuncios) {
                    // Corregido filtro tipo animal
                    if (!filtroTipoAnimal.isEmpty()) {
                        if (filtroTipoAnimal.equals("perro") && (a.getPerro() == null || !a.getPerro()))
                            continue;
                        if (filtroTipoAnimal.equals("gato") && (a.getPerro() == null || a.getPerro()))
                            continue;
                    }
                    if (!filtroRaza.isEmpty() && (a.getRaza() == null || !a.getRaza().equalsIgnoreCase(filtroRaza)))
                        continue;
                    if (!filtroProvincia.isEmpty() && (a.getUbicacion() == null || !a.getUbicacion().equalsIgnoreCase(filtroProvincia)))
                        continue;
                    if (filtroPrecioMin != null && (a.getPrecio() == null || a.getPrecio() < filtroPrecioMin))
                        continue;
                    if (filtroPrecioMax != null && (a.getPrecio() == null || a.getPrecio() > filtroPrecioMax))
                        continue;
                    listaAnuncios.add(a);
                }
                ordenarDestacadosPrimero(listaAnuncios);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(Exception e) {
                Log.e("Firestore", "Error al filtrar anuncios", e);
            }
        });
    }

    /**
     * Ordena la lista para que los anuncios destacados estén siempre arriba.
     */
    private void ordenarDestacadosPrimero(List<Anuncio> anuncios) {
        Collections.sort(anuncios, new Comparator<Anuncio>() {
            @Override
            public int compare(Anuncio a1, Anuncio a2) {
                boolean d1 = Boolean.TRUE.equals(a1.getDestacado());
                boolean d2 = Boolean.TRUE.equals(a2.getDestacado());
                if (d1 == d2) return 0;
                return d1 ? -1 : 1;
            }
        });
    }

}
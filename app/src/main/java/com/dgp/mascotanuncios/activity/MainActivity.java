package com.dgp.mascotanuncios.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dgp.mascotanuncios.R;
import com.dgp.mascotanuncios.service.ImagenService;
import com.dgp.mascotanuncios.repository.DatosRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Variables para guardar selección de filtros
    private String filtroTipoAnimal = "";
    private String filtroRaza = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View lytTexto = findViewById(R.id.lytTexto);
            lytTexto.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cargar imagen superior desde storage /
        ImageView bgImage = findViewById(R.id.bgImage);
        new ImagenService().cargarEnImageView(this, "inicio.webp", bgImage);

        // Referencias a los botones
        MaterialButton btnPerros = findViewById(R.id.btnPerros);
        MaterialButton btnGatos = findViewById(R.id.btnGatos);
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroupTipo);

        // Referencia al spinner de razas
        Spinner spinnerRazas = findViewById(R.id.spinnerRazas);

        // Repositorio de datos
        DatosRepository datosRepository = new DatosRepository();

        // Inicialmente, spinner con placeholder en blanco
        spinnerRazas.setAdapter(new WhiteHintAdapter(this, new String[]{"Elige la raza"}));

        // Permitir deseleccionar ambos botones
        toggleGroup.clearChecked();

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (checkedId == R.id.btnPerros) {
                if (isChecked) {
                    btnPerros.setTextColor(getResources().getColor(android.R.color.black));
                    btnGatos.setTextColor(getResources().getColor(android.R.color.white));
                    filtroTipoAnimal = "perro";
                    cargarRazasEnSpinner(spinnerRazas, datosRepository, "perro");
                } else {
                    btnPerros.setTextColor(getResources().getColor(android.R.color.white));
                    filtroTipoAnimal = "";
                    limpiarSpinner(spinnerRazas);
                }
            } else if (checkedId == R.id.btnGatos) {
                if (isChecked) {
                    btnGatos.setTextColor(getResources().getColor(android.R.color.black));
                    btnPerros.setTextColor(getResources().getColor(android.R.color.white));
                    filtroTipoAnimal = "gato";
                    cargarRazasEnSpinner(spinnerRazas, datosRepository, "gato");
                } else {
                    btnGatos.setTextColor(getResources().getColor(android.R.color.white));
                    filtroTipoAnimal = "";
                    limpiarSpinner(spinnerRazas);
                }
            }
        });

        // Guardar la raza seleccionada
        spinnerRazas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String seleccion = (String) parent.getItemAtPosition(position);
                if (position == 0 || seleccion.equalsIgnoreCase("Elige la raza")) {
                    filtroRaza = "";
                } else {
                    filtroRaza = seleccion;
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                filtroRaza = "";
            }
        });

        // Lógica para el botón de continuar
        Button btnContinuar = findViewById(R.id.btnBuscar);
        btnContinuar.setOnClickListener(v -> {
            // Ir a AnunciosActivity con filtros si están seleccionados
            Intent intent = new Intent(this, AnuncioActivity.class);
            intent.putExtra("filtroTipoAnimal", filtroTipoAnimal); // Siempre enviar, aunque esté vacío
            intent.putExtra("filtroRaza", filtroRaza); // Siempre enviar, aunque esté vacío
            startActivity(intent);
        });
    }

    private void cargarRazasEnSpinner(Spinner spinner, DatosRepository repo, String tipo) {
        repo.obtenerRazas(tipo, new DatosRepository.RazasCallback() {
            @Override
            public void onRazasObtenidas(List<String> razas) {
                razas.add(0, "Elige la raza");
                spinner.setAdapter(new WhiteHintAdapter(MainActivity.this, razas.toArray(new String[0])));
            }
        });
    }

    private void limpiarSpinner(Spinner spinner) {
        spinner.setAdapter(new WhiteHintAdapter(this, new String[]{"Elige la raza"}));
    }

    // Adaptador personalizado para mostrar el placeholder en blanco
    private static class WhiteHintAdapter extends ArrayAdapter<String> {
        public WhiteHintAdapter(android.content.Context context, String[] items) {
            super(context, android.R.layout.simple_spinner_item, items);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            ((TextView) v).setTextColor(0xFFFFFFFF); // Siempre blanco
            return v;
        }
    }
}
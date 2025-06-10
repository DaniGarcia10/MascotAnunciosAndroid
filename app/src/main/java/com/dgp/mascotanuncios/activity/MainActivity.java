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

        // Lógica para el botón de continuar
        Button btnContinuar = findViewById(R.id.btnBuscar);
        btnContinuar.setOnClickListener(v -> {
            // Ir a AnunciosActivity
            startActivity(new Intent(this, AnuncioActivity.class));

        });

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

        // Listener para cambiar el color del texto al seleccionar y cargar razas
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (checkedId == R.id.btnPerros && isChecked) {
                btnPerros.setTextColor(getResources().getColor(android.R.color.black));
                btnGatos.setTextColor(getResources().getColor(android.R.color.white));
                // Cargar razas de perro desde Firestore
                cargarRazasEnSpinner(spinnerRazas, datosRepository, "perro");
            } else if (checkedId == R.id.btnGatos && isChecked) {
                btnGatos.setTextColor(getResources().getColor(android.R.color.black));
                btnPerros.setTextColor(getResources().getColor(android.R.color.white));
                // Cargar razas de gato desde Firestore
                cargarRazasEnSpinner(spinnerRazas, datosRepository, "gato");
            } else if (!isChecked) {
                // Si se deselecciona, limpiar el spinner
                limpiarSpinner(spinnerRazas);
            }
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
package com.dgp.mascotanuncios.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dgp.mascotanuncios.R;
import com.dgp.mascotanuncios.model.Anuncio;
import com.dgp.mascotanuncios.service.ImagenService;
import com.dgp.mascotanuncios.activity.AnuncioDetailActivity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder> {

    private static final String TAG = "AnuncioAdapter";
    private final List<Anuncio> lista;
    private final ImagenService storageHelper;

    public AnuncioAdapter(List<Anuncio> lista) {
        this.lista = lista;
        this.storageHelper = new ImagenService();
    }

    @NonNull
    @Override
    public AnuncioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anuncio, parent, false);
        return new AnuncioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AnuncioViewHolder holder, int position) {
        Anuncio anuncio = lista.get(position);

        Log.d(TAG, "Mostrando anuncio en posición: " + position + ", Título: " + anuncio.getTitulo());

        // --- FECHA RELATIVA ---
        Date fechaPub = anuncio.getFecha_publicacion();
        if (fechaPub != null) {
            long diffMillis = new Date().getTime() - fechaPub.getTime();
            long diffHoras = TimeUnit.MILLISECONDS.toHours(diffMillis);
            long diffDias = TimeUnit.MILLISECONDS.toDays(diffMillis);

            String textoFecha;
            if (diffDias >= 1) {
                textoFecha = "Hace " + diffDias + (diffDias == 1 ? " día" : " días");
            } else if (diffHoras >= 1) {
                textoFecha = "Hace " + diffHoras + (diffHoras == 1 ? " hora" : " horas");
            } else {
                textoFecha = "Hace menos de 1 hora";
            }
            holder.fecha.setText(textoFecha);
            holder.fecha.setVisibility(View.VISIBLE);
        } else {
            holder.fecha.setVisibility(View.GONE);
        }
        // --- FIN FECHA RELATIVA ---

        holder.titulo.setText(anuncio.getTitulo());
        holder.raza.setText(anuncio.getRaza());
        holder.descripcion.setText(anuncio.getDescripcion());
        holder.precio.setText(anuncio.getPrecio() != null ? anuncio.getPrecio() + "€" : "Precio no disponible");
        holder.edad.setText(anuncio.getEdad());
        holder.ubicacion.setText("📍 " + (anuncio.getUbicacion() != null ? anuncio.getUbicacion() : "Sin ubicación"));

        if (Boolean.TRUE.equals(anuncio.getDestacado())) {
            holder.cintaDestacado.setVisibility(View.VISIBLE);
            // Poner borde naranja
            CardView card = (CardView) holder.itemView.findViewById(R.id.card_anuncio);
            card.setBackgroundResource(R.drawable.bg_card_destacado);
        } else {
            holder.cintaDestacado.setVisibility(View.GONE);
            // Fondo normal (blanco sin borde)
            CardView card = (CardView) holder.itemView.findViewById(R.id.card_anuncio);
            card.setBackgroundColor(holder.itemView.getResources().getColor(android.R.color.white));
        }

        if (anuncio.getImagenes() != null && !anuncio.getImagenes().isEmpty()) {
            String rutaFirebase = "anuncios/" + anuncio.getId() + "/" + anuncio.getImagenes().get(0);
            Log.d("DEBUG_RUTA", "📁 Ruta a cargar: " + rutaFirebase);

            storageHelper.obtenerUrlImagen(rutaFirebase,
                    uri -> {
                        Log.d("FIREBASE_IMG", "✅ URL obtenida: " + uri.toString());
                        Glide.with(holder.itemView.getContext())
                                .load(uri)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.placeholder)
                                .into(holder.imagen);
                    },
                    error -> {
                        Log.e("FIREBASE_IMG", "❌ Error al obtener URL", error);
                        holder.imagen.setImageResource(R.drawable.placeholder);
                    }
            );

            int total = anuncio.getImagenes().size();
            if (total > 1) {
                holder.contadorFotos.setText("+" + (total - 1) + " fotos");
                holder.contadorFotos.setVisibility(View.VISIBLE);
            } else {
                holder.contadorFotos.setVisibility(View.GONE);
            }
        } else {
            Log.w(TAG, "Anuncio sin imágenes: " + anuncio.getTitulo());
            holder.imagen.setImageResource(R.drawable.placeholder);
            holder.contadorFotos.setVisibility(View.GONE);
        }

        // Siempre abrir detalle al pulsar el anuncio
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AnuncioDetailActivity.class);
            intent.putExtra("anuncio_id", anuncio.getId());
            v.getContext().startActivity(intent);
        });

        // --- Eliminar la lógica de carga de datos del criadero ---
        // Ahora se asume que los datos de criadero ya están en el objeto anuncio o en un modelo extendido.
        // Por ejemplo, si usas un modelo AnuncioConCriadero:
        // holder.nombreCriadero.setText(anuncio.getNombreCriadero());
        // holder.nucleoZoologico.setText(anuncio.getNucleoZoologico());
        // holder.verificado.setVisibility(anuncio.isCriaderoVerificado() ? View.VISIBLE : View.GONE);
        // Glide.with(holder.itemView.getContext()).load(anuncio.getFotoPerfilCriadero())...
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class AnuncioViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, descripcion, raza, precio, edad, ubicacion, cintaDestacado, contadorFotos;
        TextView fecha; // Añadido
        ImageView imagen;

        public AnuncioViewHolder(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            raza = itemView.findViewById(R.id.raza);
            precio = itemView.findViewById(R.id.precio);
            edad = itemView.findViewById(R.id.edad);
            ubicacion = itemView.findViewById(R.id.ubicacion);
            cintaDestacado = itemView.findViewById(R.id.cinta_destacado);
            imagen = itemView.findViewById(R.id.imagenAnuncio);
            contadorFotos = itemView.findViewById(R.id.contadorFotos);
            fecha = itemView.findViewById(R.id.fecha);
        }
    }
}
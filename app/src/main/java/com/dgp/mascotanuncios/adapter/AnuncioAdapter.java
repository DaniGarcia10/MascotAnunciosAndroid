package com.dgp.mascotanuncios.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dgp.mascotanuncios.R;
import com.dgp.mascotanuncios.model.Anuncio;

import java.util.List;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioAdapter.AnuncioViewHolder> {

    private final List<Anuncio> lista;

    public AnuncioAdapter(List<Anuncio> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public AnuncioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anuncio, parent, false);
        return new AnuncioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull AnuncioViewHolder holder, int position) {
        Anuncio a = lista.get(position);
        holder.titulo.setText(a.getTitulo());
        holder.descripcion.setText(a.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class AnuncioViewHolder extends RecyclerView.ViewHolder {
        TextView titulo, descripcion;

        public AnuncioViewHolder(View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.tituloAnuncio);
            descripcion = itemView.findViewById(R.id.descripcionAnuncio);
        }
    }
}

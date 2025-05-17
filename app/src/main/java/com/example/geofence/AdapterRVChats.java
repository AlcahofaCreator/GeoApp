package com.example.geofence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterRVChats extends RecyclerView.Adapter<AdapterRVChats.ChatsHolder> {

    private List<ChatVO> lstChats;
    private View.OnClickListener clickListener;

    public AdapterRVChats(List<ChatVO> lstChats) {
        this.lstChats = lstChats;
    }

    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ChatsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View mView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat, viewGroup, false);
        mView.setOnClickListener(clickListener);
        return new ChatsHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsHolder chatsHolder, int position) {
        chatsHolder.nombre.setText(lstChats.get(position).getNombre());
    }

    @Override
    public int getItemCount() {
        return lstChats.size();
    }

    class ChatsHolder extends RecyclerView.ViewHolder {
        private final TextView nombre;
        public ChatsHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombrechats);
        }
    }
}

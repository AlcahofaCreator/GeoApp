package com.example.geofence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListaChats extends AppCompatActivity {

    ImageView location;

    private RecyclerView rVchats;
    private TextView nombre;
    private List<ChatVO> lstChats = new ArrayList<>();

    private AdapterRVChats mAdapterRVChats = new AdapterRVChats(lstChats);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_chats);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        location = findViewById(R.id.location);
        nombre = findViewById(R.id.nombre);
        rVchats = findViewById(R.id.rvChats);

        rVchats.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rVchats.setAdapter(mAdapterRVChats);
        rVchats.setHasFixedSize(true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = currentUser != null ? currentUser.getUid() : "";
        final String[] miNombre = {""};

        mAdapterRVChats.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = rVchats.getChildAdapterPosition(v);
                if (position != RecyclerView.NO_POSITION) {
                    ChatVO chatSeleccionado = lstChats.get(position);
                    String nombreChat = chatSeleccionado.getNombre();

                    Intent intent = new Intent(ListaChats.this, Segunda.class);
                    intent.putExtra("nombreChat", nombreChat);
                    intent.putExtra("miNombre", miNombre[0]);
                    startActivity(intent);
                }
            }
        });

        FirebaseFirestore.getInstance().collection("ubicaciones")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            Log.e("Firestore error", error.getMessage());
                            return;
                        }

                        if (queryDocumentSnapshots == null) {
                            Log.d("Firestore", "No hay documentos");
                            return;
                        }

                        for (DocumentChange mDocumentChange : queryDocumentSnapshots.getDocumentChanges()) {
                            if (mDocumentChange.getType() == DocumentChange.Type.ADDED) {
                                ChatVO chat = mDocumentChange.getDocument().toObject(ChatVO.class);
                                String docId = mDocumentChange.getDocument().getId(); // UID del usuario
                                chat.setId(docId);

                                if (docId.equals(myUid)) {
                                    miNombre[0] = chat.getNombre(); // guardar tu propio nombre
                                    continue;
                                }

                                // ✅ Solo mostrar si está dentro de la geovalla
                                if (MainActivity.usuariosDentroDeGeovalla.contains(docId)) {
                                    lstChats.add(chat);
                                    mAdapterRVChats.notifyDataSetChanged();
                                    rVchats.smoothScrollToPosition(lstChats.size());
                                }
                            }
                        }
                    }
                });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaChats.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
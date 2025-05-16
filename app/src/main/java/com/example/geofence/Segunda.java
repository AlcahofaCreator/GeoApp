package com.example.geofence;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Segunda extends AppCompatActivity {

    ImageView location;

    private RecyclerView rVmensajes;
    private EditText mensaje;
    private ImageButton send;
    private TextView nombre;
    private List<MensajeVO> lstMensajes = new ArrayList<>();

    private AdapterRVMensajes mAdapterRVMensajes = new AdapterRVMensajes(lstMensajes);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_segunda);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        location = findViewById(R.id.location);

        nombre = findViewById(R.id.nombre);
        mensaje = findViewById(R.id.mensaje);
        send = findViewById(R.id.send);

        rVmensajes.findViewById(R.id.rvMensajes);

        rVmensajes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rVmensajes.setAdapter(mAdapterRVMensajes);
        rVmensajes.setHasFixedSize(true);

        FirebaseFirestore.getInstance().collection("chat").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange mDocumentChange : queryDocumentSnapshots.getDocumentChanges()){
                    if (mDocumentChange.getType() == DocumentChange.Type.ADDED){
                        lstMensajes.add(mDocumentChange.getDocument().toObject(MensajeVO.class));
                        mAdapterRVMensajes.notifyDataSetChanged();
                        rVmensajes.smoothScrollToPosition(lstMensajes.size());
                    }
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mensaje.length()== 0)
                    return;
                MensajeVO mMensajeVO = new MensajeVO();
                mMensajeVO.setMensaje(mensaje.getText().toString());
                mMensajeVO.setNombre(nombre.getText().toString());
                FirebaseFirestore.getInstance().collection("chat").add(mMensajeVO);
                mensaje.setText("");
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Segunda.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
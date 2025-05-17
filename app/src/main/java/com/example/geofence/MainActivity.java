package com.example.geofence;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GeofencingClient geofencingClient;
    private FusedLocationProviderClient fusedLocationClient;
    private Circle currentGeofenceCircle;
    private GoogleMap map;
    public int radio = 30000;
    private List<Geofence> geofenceList = new ArrayList<>();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView chats;
    private Location miUbicacionActual;
    private final List<Circle> circulosVisibles = new ArrayList<>();
    private Circle miCirculoPersonal;
    private final Map<String, Marker> marcadoresPorUsuario = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chats = findViewById(R.id.chats);
        chats.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Segunda.class)));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        requestPermissions();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                miUbicacionActual = location;

                // 🔄 Guardar en Firestore inmediatamente al iniciar
                if (user != null) {
                    db.collection("ubicaciones").document(user.getUid())
                            .set(new HashMap<String, Object>() {{
                                put("lat", location.getLatitude());
                                put("long", location.getLongitude());
                            }}, SetOptions.merge());
                }

            } else {
                requestNewLocationData();
                Toast.makeText(this, "Ubicación no disponible. Activa el GPS.", Toast.LENGTH_SHORT).show();
            }
        });

        // 🔁 Llamar actualizaciones constantes
        Handler handler = new Handler();
        Runnable actualizar = new Runnable() {
            @Override
            public void run() {
                actualizarLocalizaciones(); // también actualiza ubicación del usuario y su círculo
                handler.postDelayed(this, 60000); // puedes subir a 60000 (1 min) para producción
            }
        };
        handler.postDelayed(actualizar, 60000);
    }


    private void actualizarLocalizaciones() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = currentUser != null ? currentUser.getUid() : "";

        // 🟢 Actualizar ubicación propia y círculo
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    miUbicacionActual = location;
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    // 🔁 Subir a Firestore
                    if (user != null) {
                        db.collection("ubicaciones").document(user.getUid())
                                .set(new HashMap<String, Object>() {{
                                    put("lat", lat);
                                    put("long", lng);
                                }}, SetOptions.merge());
                    }

                    // 🔵 Dibujar o mover tu círculo
                    LatLng pos = new LatLng(lat, lng);
                    if (miCirculoPersonal != null) {
                        miCirculoPersonal.setCenter(pos);
                    } else {
                        miCirculoPersonal = map.addCircle(new CircleOptions()
                                .center(pos)
                                .radius(radio)
                                .strokeColor(Color.argb(255, 0, 100, 255))
                                .fillColor(Color.argb(64, 0, 100, 255))
                                .strokeWidth(4f));
                    }
                }
            });
        }

        // 🔁 Leer todos los usuarios
        db.collection("ubicaciones").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> usuariosActuales = new HashSet<>();
            geofenceList.clear();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Log.d("Geofence", "📄 Documento leído: " + doc.getId() + " => " + doc.getData());
                String userId = doc.getId();
                if (userId.equals(myUid)) continue;

                Double lat = doc.getDouble("lat");
                Double lng = doc.getDouble("long");

                if (lat != null && lng != null) {
                    Log.d("Geofence", "📌 Coordenadas válidas para " + userId + ": " + lat + ", " + lng);
                    usuariosActuales.add(userId);
                    LatLng pos = new LatLng(lat, lng);

                    if (miUbicacionActual != null) {
                        float[] distancia = new float[1];
                        Location.distanceBetween(
                                lat, lng,
                                miUbicacionActual.getLatitude(), miUbicacionActual.getLongitude(),
                                distancia
                        );

                        if (distancia[0] <= radio) {
                            // 📍 Mostrar o mover marcador
                            if (marcadoresPorUsuario.containsKey(userId)) {
                                marcadoresPorUsuario.get(userId).setPosition(pos);
                            } else {
                                Marker nuevoMarker = map.addMarker(new MarkerOptions()
                                        .position(pos)
                                        .title("Usuario: " + doc.getString("nombre")));
                                marcadoresPorUsuario.put(userId, nuevoMarker);
                            }
                        } else {
                            // ❌ Eliminar marcador si se sale
                            if (marcadoresPorUsuario.containsKey(userId)) {
                                marcadoresPorUsuario.get(userId).remove();
                                marcadoresPorUsuario.remove(userId);
                            }
                        }
                    }

                    // 📡 Agregar geovalla de ese usuario
                    Geofence valla = new Geofence.Builder()
                            .setRequestId("user_" + userId)
                            .setCircularRegion(lat, lng, radio)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setLoiteringDelay(10000) // 10 segundos para activar DWELL
                            .setTransitionTypes(
                                    Geofence.GEOFENCE_TRANSITION_ENTER |
                                            Geofence.GEOFENCE_TRANSITION_EXIT |
                                            Geofence.GEOFENCE_TRANSITION_DWELL
                            )
                            .build();

                    geofenceList.add(valla); // 👈 AGREGA ESTO

                }
            }

            // 🧼 Eliminar marcadores de usuarios que ya no están
            Iterator<Map.Entry<String, Marker>> markerIterator = marcadoresPorUsuario.entrySet().iterator();
            while (markerIterator.hasNext()) {
                Map.Entry<String, Marker> entry = markerIterator.next();
                if (!usuariosActuales.contains(entry.getKey())) {
                    entry.getValue().remove();
                    markerIterator.remove();
                }
            }

            // 🔁 Eliminar geovallas anteriores antes de agregar nuevas
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                geofencingClient.removeGeofences(getGeofencePendingIntent())
                        .addOnSuccessListener(unused -> {
                            Log.d("Geofence", "Geovallas anteriores eliminadas");

                            if (!geofenceList.isEmpty()) {
                                geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Geofence", "Nuevas geovallas registradas");

                                            // 🔁 Simular evento DWELL si ya estás dentro de alguna
                                            new Handler().postDelayed(() -> {
                                                for (Geofence geo : geofenceList) {
                                                    float[] distancia = new float[1];
                                                    Location.distanceBetween(
                                                            miUbicacionActual.getLatitude(), miUbicacionActual.getLongitude(),
                                                            geo.getLatitude(), geo.getLongitude(),
                                                            distancia
                                                    );
                                                    if (distancia[0] <= geo.getRadius()) {
                                                        Log.d("Geofence", "🔁 Simulando DWELL en: " + geo.getRequestId());

                                                        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
                                                        intent.setAction("com.example.geofence.SIMULATED_DWELL");
                                                        sendBroadcast(intent);
                                                    }
                                                }
                                            }, 11000); // loiteringDelay + margen
                                        })
                                        .addOnFailureListener(e -> Log.e("Geofence", "Error al registrar nuevas geovallas: " + e.getMessage()));
                            } else {
                                Log.w("Geofence", "⚠️ No se agregaron geovallas porque la lista está vacía");
                            }

                        })
                        .addOnFailureListener(e ->
                                Log.e("Geofence", "Error al eliminar geovallas: " + e.getMessage()));
            }
        });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_DWELL
        );
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class); // ya no es interna
        return PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void requestPermissions() {
        ActivityResultLauncher<String[]> permissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            Boolean notificationGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? result.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) : true;

            if (!fineLocationGranted && !coarseLocationGranted) {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
            if (!notificationGranted) {
                Toast.makeText(this, "Permiso de notificaciones denegado.", Toast.LENGTH_SHORT).show();
            }
        });

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        permissionRequest.launch(permissions.toArray(new String[0]));
    }

    private void requestNewLocationData() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    miUbicacionActual = locationResult.getLastLocation();
                }
            }
        }, getMainLooper());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        enableMyLocation();
        getDeviceLocationAndMoveCamera();
        actualizarLocalizaciones();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
    }

    private void getDeviceLocationAndMoveCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    miUbicacionActual = location;
                    LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                }
            });
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Ubicación actual:\n" + location, Toast.LENGTH_LONG).show();
    }
}

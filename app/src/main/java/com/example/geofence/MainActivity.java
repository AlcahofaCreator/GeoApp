package com.example.geofence;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.widget.Toast;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GeofencingClient geofencingClient;
    private FusedLocationProviderClient fusedLocationClient;



    private List<Geofence> geofenceList = new ArrayList<>();

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);




        requestPermissions();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            float radius = 80;

                            // Construir geovalla
                            Geofence valla = new Geofence.Builder()
                                    .setRequestId("Valla_Principal")
                                    .setCircularRegion(latitude, longitude, radius)
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(
                                            Geofence.GEOFENCE_TRANSITION_ENTER |
                                                    Geofence.GEOFENCE_TRANSITION_EXIT
                                    )
                                    .build();

                            geofenceList.add(valla);

                            Toast.makeText(
                                    MainActivity.this,
                                    "Geovalla creada en: " + latitude + ", " + longitude,
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Registrar la geovalla con PendingIntent
                            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(MainActivity.this,
                                                        "Geovalla registrada con éxito!",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MainActivity.this,
                                                    "Error al registrar geovalla: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {

                            Toast.makeText(
                                    MainActivity.this,
                                    "Obteniendo ubicación actual...",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Forzar una nueva actualización de ubicación
                            requestNewLocationData();

                            Toast.makeText(
                                    MainActivity.this,
                                    "Ubicación no disponible. Activa el GPS.",
                                    Toast.LENGTH_SHORT
                         ).show();
                        }
                    }
                });
    }



    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }



    private void requestPermissions() {
        ActivityResultLauncher<String[]> permissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    Boolean notificationGranted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            ? result.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false)
                            : true; // No necesario para versiones anteriores

                    if (fineLocationGranted) {
                        Toast.makeText(this, "Precise location access granted.", Toast.LENGTH_SHORT).show();
                    } else if (coarseLocationGranted) {
                        Toast.makeText(this, "Only approximate location access granted.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Location access denied.", Toast.LENGTH_SHORT).show();
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



    // BroadcastReceiver
    public static class GeofenceBroadcastReceiver extends BroadcastReceiver {
        private static final String CHANNEL_ID = "geofence_channel";

        @Override
        public void onReceive(Context context, Intent intent) {
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                sendNotification(context, "Error en geovalla: " + geofencingEvent.getErrorCode());
                return;
            }

            int transitionType = geofencingEvent.getGeofenceTransition();
            String message = "";

            switch (transitionType) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    message = "¡Entraste en la zona!";
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    message = "Saliste de la zona";
                    break;
            }

            sendNotification(context, message);
        }

        private void sendNotification(Context context, String message) {
            createNotificationChannel(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_map) // Puedes cambiar el ícono
                    .setContentTitle("Geovalla")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify((int) System.currentTimeMillis(), builder.build()); // ID único
        }

        private void createNotificationChannel(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Canal de Geovalla";
                String description = "Notificaciones de entrada y salida de zonas geográficas";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void requestNewLocationData() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(10000)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationResult == null) {
                            return;
                        }

                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            // Detener las actualizaciones para ahorrar batería
                            fusedLocationClient.removeLocationUpdates(this);

                            // Procesar la nueva ubicación
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            float radius = 80;

                            // Construir geovalla
                            Geofence valla = new Geofence.Builder()
                                    .setRequestId("Valla_Principal")
                                    .setCircularRegion(latitude, longitude, radius)
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(
                                            Geofence.GEOFENCE_TRANSITION_ENTER |
                                                    Geofence.GEOFENCE_TRANSITION_EXIT
                                    )
                                    .build();

                            geofenceList.add(valla);

                            Toast.makeText(
                                    MainActivity.this,
                                    "Geovalla creada con ubicación actualizada: " + latitude + ", " + longitude,
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Registrar la geovalla
                            if (ActivityCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                                        .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this,
                                                "Geovalla registrada con éxito!",
                                                Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this,
                                                "Error al registrar geovalla: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show());
                            }
                        }
                    }
                },
                getMainLooper()
        );
    }

    //ON MAP READY
    @Override
    public void onMapReady(GoogleMap googleMap){


        LatLng ubicacionInicial = new LatLng(28.5, -103);

        googleMap.addMarker(new MarkerOptions()
                .position(ubicacionInicial)
                .title("Marker"));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 15));
    }
}
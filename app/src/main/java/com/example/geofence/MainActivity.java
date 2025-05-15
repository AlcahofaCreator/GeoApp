package com.example.geofence;

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
import android.util.Log;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GeofencingClient geofencingClient;
    private FusedLocationProviderClient fusedLocationClient;
    private Circle currentGeofenceCircle;
    private GoogleMap map;

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
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;
        map.setOnMyLocationButtonClickListener((GoogleMap.OnMyLocationButtonClickListener) this);
        map.setOnMyLocationClickListener((GoogleMap.OnMyLocationClickListener) this);


        enableMyLocation();
        getDeviceLocationAndMoveCamera();

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }


    }

    private void getDeviceLocationAndMoveCamera() {
        /*
         * Obtiene la mejor y más reciente ubicación del dispositivo, que puede ser nula en raras
         * ocasiones cuando la ubicación no está disponible.
         */
        try {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this.getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Establece la posición de la cámara del mapa en la ubicación actual del dispositivo.
                        Location lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng, 15));

                            CircleOptions circleOptions = new CircleOptions()
                                    .center(currentLatLng)
                                    .radius(80) // Radio en metros
                                    .strokeColor(Color.argb(255, 0, 100, 255))
                                    .fillColor(Color.argb(64, 0, 100, 255))
                                    .strokeWidth(4f);

                            map.addCircle(circleOptions);

                        } else {

                            Log.d("MapDebug", "No se obtuvo la ubicación actual.");
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.5, -103), 5));

                            Toast.makeText(this, "No se pudo obtener la ubicación actual. Mostrando ubicación predeterminada.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("MapDebug", "Current location is null. Using defaults.");
                        Log.e("MapDebug", "Exception: %s", task.getException());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.5, -103), 5)); // Ubicación predeterminada
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                        Toast.makeText(this, "Error al obtener la ubicación.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Permiso de ubicación no concedido. Mostrando ubicación predeterminada.", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.5, -103), 5));
        }
    }
}

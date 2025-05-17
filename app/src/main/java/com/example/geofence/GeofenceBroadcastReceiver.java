package com.example.geofence;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "geofence_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("GeofenceReceiver", "✅ Intent recibido: " + intent);

        if ("com.example.geofence.SIMULATED_DWELL".equals(intent.getAction())) {
            Log.d("GeofenceReceiver", "🔁 Procesando DWELL simulado");
            return;
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "⚠️ GeofencingEvent es null");
            return;
        }

        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e("GeofenceReceiver", "❌ Error en evento de geovalla. Código: " + errorCode);
            return;
        }

        int transitionType = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        Log.d("GeofenceReceiver", "🟢 Tipo de transición: " + transitionType);
        Log.d("GeofenceReceiver", "📍 Geofences activadas: " + triggeringGeofences.size());

        for (Geofence geofence : triggeringGeofences) {
            String requestId = geofence.getRequestId();
            String userId = requestId.replace("user_", "");

            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                sendNotification(context, "📍 Entraste en la geovalla de " + userId);
            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                sendNotification(context, "🚪 Saliste de la geovalla de " + userId);
            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
                sendNotification(context, "🕒 Permaneces en la geovalla de " + userId);
            } else {
                Log.w("GeofenceReceiver", "⚠️ Tipo de transición desconocido: " + transitionType);
            }
        }
    }

    private void sendNotification(Context context, String message) {
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_map)
                .setContentTitle("Geovalla")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Geofence Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

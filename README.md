# 🗺️ Geofencing App 

Aplicación Android de chat con amigos, utilizando los servicios de ubicación de Google Play y que detecta cuando el dispositivo entra/sale de zonas geográficas definidas (geovallas).

## 📋 Requisitos
- Android SDK 24+
- Google Play Services 21+
- Permisos de ubicación (en primer y segundo plano)

## 🌟 Features

### Implementadas
- ✅ Creación de geovallas circulares  
- ✅ Notificaciones locales en tiempo real  
- ✅ Deteccion de geovallas cercanas

### En desarrollo (Roadmap)
- 🛠 Integración con Google Maps  
- 🛠 Histórico de eventos en la nube  
- 🛠 Modo de bajo consumo energético  

### Futuras ideas
- 🔍 Geovallas con formas personalizadas   
- 👥 Geofencing colaborativo  

## Tech Stack

**Client:** Java, XML, Android SDK 

**Server:** Firebase Auth, GooglePlay Services 


## Screenshots

![App Screenshot](https://via.placeholder.com/468x300?text=App+Screenshot+Here)

# Geofencing App - API Reference

## Client-Side (Android)

### 1. Location Permissions

```
  Runtime Permission Request
```


| Permission | Description | Required For |
|------------|-------------|--------------|
| `ACCESS_FINE_LOCATION` | Precise GPS location | Geofence creation |
| `ACCESS_COARSE_LOCATION` | Approximate network location | Fallback option |
| `ACCESS_BACKGROUND_LOCATION` | Background location access (Android 10+) | Persistent geofencing |

### 2. Geofence Creation

```java
  new Geofence.Builder()
      .setRequestId("geofence_id")
      .setCircularRegion(latitude, longitude, radius)
      .setExpirationDuration(Geofence.NEVER_EXPIRE)
      .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
      .build();
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `requestId` | `String` | Unique identifier for the geofence |
| `latitude` | `double` | Center point latitude |
| `longitude` | `double` | Center point longitude |
| `radius` | `float` | Radius in meters (min 100m recommended) |

### 3. Geofencing Client

```java
  geofencingClient.addGeofences(geofencingRequest, pendingIntent);
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `geofencingRequest` | `GeofencingRequest` | Configured geofence request |
| `pendingIntent` | `PendingIntent` | Intent to trigger on geofence events |

### 4. Event Handling

```java
  public class GeofenceBroadcastReceiver extends BroadcastReceiver {
      @Override
      public void onReceive(Context context, Intent intent) {
          GeofencingEvent event = GeofencingEvent.fromIntent(intent);
          // Handle enter/exit events
      }
  }
```

| Transition Type | Description |
|-----------------|-------------|
| `GEOFENCE_TRANSITION_ENTER` | Triggered when device enters geofence |
| `GEOFENCE_TRANSITION_EXIT` | Triggered when device exits geofence |

## Server-Side (Optional)

### 1. Geofence Event Endpoint

```
  POST /api/geofence/events
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `device_id` | `string` | **Required**. Device identifier |
| `geofence_id` | `string` | **Required**. Geofence identifier |
| `event_type` | `string` | "ENTER" or "EXIT" |
| `timestamp` | `datetime` | Event occurrence time |

### 2. Notification Service

```
  POST /api/notifications
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `device_token` | `string` | **Required**. FCM registration token |
| `message` | `string` | Notification content |
| `geofence_data` | `object` | Related geofence information |

## Utility Methods

### getCurrentLocation()

Returns the device's last known location.

```java
  fusedLocationClient.getLastLocation()
      .addOnSuccessListener(location -> {
          // Use location data
      });
```

### createGeofencePendingIntent()

Creates a PendingIntent for geofence transitions.

```java
  private PendingIntent getGeofencePendingIntent() {
      Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
      return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
  }
```

## Dependencies

```gradle
implementation 'com.google.android.gms:play-services-location:21.0.1'
implementation 'androidx.core:core-ktx:1.12.0'
```
## Authors

- [@AlcachofaK](https://github.com/AlcahofaCreator)


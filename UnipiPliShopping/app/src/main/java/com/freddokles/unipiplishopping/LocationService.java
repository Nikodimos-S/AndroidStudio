package com.freddokles.unipiplishopping;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();
    private Runnable locationRunnable;
    private static final int INTERVAL = 30000; //in ms
    private static final double RADIUS_METERS = 1000000;//in m

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRunnable = new Runnable() {
            @Override
            public void run() {
                checkLocation();
                handler.postDelayed(this, INTERVAL);
            }
        };

        handler.post(locationRunnable);
    }

    private void checkLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        checkProximity(location);
                    }
                });
    }

    private void checkProximity(Location userLocation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("stores").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double minDistance=2^64-1;
                    String minStore="";
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GeoPoint storeLocation = document.getGeoPoint("location");
                        String storeName = document.getString("name");

                        if (storeLocation != null && storeName != null) {
                            double distance = haversine(userLocation.getLatitude(), userLocation.getLongitude(),
                                    storeLocation.getLatitude(), storeLocation.getLongitude());

                            if (distance <= minDistance) {
                                minDistance = distance;
                                minStore = storeName;
                            }
                        }


                    }
                    if (minDistance<=RADIUS_METERS){
                        sendNotification(minStore);
                    }
                });
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; //radius of the FUCKING EARTH in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private void sendNotification(String storeName) {
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.product1)
                .setContentTitle("Nearby Store")
                .setContentText("You are close to " + storeName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.notify(1, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

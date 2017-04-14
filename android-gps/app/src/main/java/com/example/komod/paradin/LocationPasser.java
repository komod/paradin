package com.example.komod.paradin;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by komod on 2017/4/15.
 */

public class LocationPasser extends Service implements LocationListener {
    private static final String TAG = "LocationPasser";

    private LocationManager mLocationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        int val = super.onStartCommand(intent, flags, startId);
        if (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Register the listener with the Location Manager to receive location updates
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        }
        return val;
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mLocationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.
        Log.i(TAG, "Longitude " + String.valueOf(location.getLongitude()));
        Log.i(TAG, "Latitude " + String.valueOf(location.getLatitude()));
        Log.i(TAG, "Accuracy " + String.valueOf(location.getAccuracy()));
        Log.i(TAG, "Time " + String.valueOf(location.getTime()));
        Log.i(TAG, "Provider " + location.getProvider());
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }
}

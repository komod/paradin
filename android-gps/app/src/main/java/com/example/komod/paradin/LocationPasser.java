package com.example.komod.paradin;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by komod on 2017/4/15.
 */

public class LocationPasser extends Service implements LocationListener {
    private static final String TAG = "LocationPasser";

    private LocationManager mLocationManager;
    private final Location mLastLocation = new Location("MyLocation");
    // private GPSMock mMock = new GPSMock(this);

    public class MyBinder extends Binder {
        LocationPasser getService() {
            return LocationPasser.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        int val = super.onStartCommand(intent, flags, startId);
        if (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Register the listener with the Location Manager to receive location updates
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            // mMock.start();
        }
        return val;
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new MyBinder();
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
        // mMock.stop();
    }

    public void onLocationChanged(Location location) {
        // Called when a new location is found by the network location provider.
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.i(TAG, "Latitude " + String.valueOf(latitude));
        Log.i(TAG, "Longitude " + String.valueOf(longitude));
        Log.i(TAG, "Accuracy " + String.valueOf(location.getAccuracy()));
        Log.i(TAG, "Time " + String.valueOf(location.getTime()));
        Log.i(TAG, "Provider " + location.getProvider());
        mLastLocation.setLatitude(latitude);
        mLastLocation.setLongitude(longitude);
        mLastLocation.setAccuracy(location.getAccuracy());
        mLastLocation.setTime(location.getTime());
        mLastLocation.setProvider(location.getProvider());

        try {
            JSONObject anchorInfo = new JSONObject();
            anchorInfo.put("latitude", latitude);
            anchorInfo.put("longitude", longitude);
            new PassTask().execute(anchorInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    public Location getLocation() {
        return mLastLocation;
    }
}

class PassTask extends AsyncTask< JSONObject, Integer, Long> {
    private static final String TAG = "PassTask";
    private static final String SERVER_URL = "http://paradin-me.appspot.com";
    protected Long doInBackground(JSONObject... param) {
        Log.d(TAG, "Post Anchor");
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(SERVER_URL + "/route/api/v1.0/anchor");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
            osw.write(param[0].toString());
            osw.flush();
            osw.close();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "OK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return 0L;
    }
}

class GPSMock {
    private static final String TAG = "GPSMock";
    private final Location mLocation = new Location(TAG);
    private final LocationListener mReceiver;
    private final Handler mHandler = new Handler();
    private final Runnable notifyGPS = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Run update location");
            // Repeat this the same runnable code block again another 2 seconds
            if (mReceiver != null) {
                mLocation.setLongitude(counter);
                mLocation.setLatitude(counter);
                mReceiver.onLocationChanged(mLocation);
            }
            ++counter;
            if (!mAbort) {
                mHandler.postDelayed(notifyGPS, 20000);
            }
        }
    };

    private int counter = 0;
    private boolean mAbort = false;

    GPSMock(LocationListener listener) {
        mReceiver = listener;
    }

    public void start() {
        Log.d(TAG, "start");
        mHandler.postDelayed(notifyGPS, 1000);
    }
    public void stop() {
        Log.d(TAG, "stop");
        mAbort = true;
        mHandler.removeCallbacks(notifyGPS);
    }
}

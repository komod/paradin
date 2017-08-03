package com.example.komod.paradin;

import android.content.Intent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Intent mServiceIntent;

    private LocationPasser mLocationPasser;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationPasser.MyBinder binder = (LocationPasser.MyBinder) service;
            mLocationPasser = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationPasser = null;
        }
    };

    private final Handler mHandler = new Handler();
    private final Runnable updateLocation = new Runnable() {
        @Override
        public void run() {
            if (mLocationPasser != null) {
                Location location = mLocationPasser.getLocation();
                ((TextView) findViewById(R.id.latitude_text_view)).setText(String.valueOf(location.getLatitude()));
                ((TextView) findViewById(R.id.longitude_text_view)).setText(String.valueOf(location.getLongitude()));
                ((TextView) findViewById(R.id.accuracy_text_view)).setText(String.valueOf(location.getAccuracy()));
                ((TextView) findViewById(R.id.source_text_view)).setText(location.getProvider() + ": " + String.valueOf(location.getTime()));
            }
            mHandler.postDelayed(updateLocation, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        mServiceIntent = new Intent(MainActivity.this, LocationPasser.class);
        startService(mServiceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mHandler.removeCallbacks(updateLocation);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mHandler.post(updateLocation);
        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        stopService(mServiceIntent);
    }
}

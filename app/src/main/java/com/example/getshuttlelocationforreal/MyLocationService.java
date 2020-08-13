/**
 * This class is a service that runs in the background and collects the devices location and
 * send the longitude, latitude and status of the shuttle to the firebase db.
 */
package com.example.getshuttlelocationforreal;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static java.lang.Math.pow;

public class MyLocationService extends Service {
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = db.getReference("shuttles");
    private String numOfShuttle;
    private Boolean status = false;

    //the minimum distance for locationListener
    private double minDis = 5;
    // the location coordinates for the first and last shuttle stations
    private static final double FIRST_STOP_LAT = 32.0734411;
    private static final double FIRST_STOP_LON = 34.8483981;
    private static final double LAST_STOP_LAT = 32.0727493;
    private static final double LAST_STOP_LON = 34.849301;
    //the chosen radios by the power of 2 (to identify if the shuttle is in the stations proximity)
    private static final double SQUARED_RADIOS = 0.000000104329;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            /* sent location to server */
        }
    };
    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            //send to firebase
            writeToDB(latitude, longitude);

        //for debugging use
         /* String msg = "New Latitude: " + latitude + " New Longitude: " + longitude;
            //System.out.println(msg);

            Context context = getApplicationContext();
            CharSequence text = msg;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();*/
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /**
     *
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // open firebase database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        if (user != null) {
            String emailOfShuttle = user.getEmail();
            //get users email address to find user in db
            numOfShuttle = emailOfShuttle.split("@")[0];
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, (float) minDis, mLocationListener);
                handler.postDelayed(runnable, 60 * 60 * 1000);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, (float) minDis, mLocationListener);
                handler.postDelayed(runnable, 60 * 60 * 1000);
            }
        } catch (SecurityException se) {
            // do nothing
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Write to database the latitude, longitude and the status of the shuttle.
     * @param latitude  of the device's location
     * @param longitude of the device's location
     */
    public void writeToDB(double latitude, double longitude) {
        updateStatus(latitude, longitude);
        myRef.child(numOfShuttle).setValue(latitude + "," + longitude + "," + status);
    }

    /**
     * Updating the status of the shuttle.
     * Using d^2 < r^2 to determine if the shuttle is in the surrounding of the station.
     * d is the calculated distance between the shuttle and the station, and r is the radius
     * defined.
     * The status of the shuttle can be True or False, while True means that the shuttle is active and
     * False means that the shuttle isn't active and has arrived at the last station.
     * @param latitude  of the device's location
     * @param longitude of the device's location
     */
    public void updateStatus(double latitude, double longitude) {
        //checking if the shuttle came in close proximity of the last station
        if (this.status && (pow(latitude - LAST_STOP_LAT, 2) + pow(longitude - LAST_STOP_LON, 2) <= SQUARED_RADIOS)) {
            this.status = false;
        }
        //checking if the shuttle left the proximity of the first station
        else if (!this.status && (pow(latitude - FIRST_STOP_LAT, 2) + pow(longitude - FIRST_STOP_LON, 2) <= SQUARED_RADIOS)) {
            this.status = true;
        }
    }
}
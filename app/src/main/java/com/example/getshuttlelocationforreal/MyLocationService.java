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
    private FirebaseDatabase db=FirebaseDatabase.getInstance();
    private DatabaseReference myRef=db.getReference("shuttles");
    private String numOfShuttle;
    private Boolean status = false;

    private double minDis = 5;


    // todo - change first and last station coordinates
    //private static final double FIRST_STOP_LAT = 32.0727493;
    //private static final double FIRST_STOP_LON = 34.849301;
    //private static final double LAST_STOP_LAT = 32.072267499999995;
    //private static final double LAST_STOP_LON = 34.8480397;

    //31.786266,35.297893
    // "31.786342667022506, 35.29741737647691" - laurens house
    // 31.78626115987331, 35.29813167304606 - by the compyter in my room
    // 31.786476,35.298171 - tree trunk - station 0
    // 31.786442,35.297228 - krak - statiom 1

    // jennie's house
//    private static final double FIRST_STOP_LAT = 31.786442;
//    private static final double FIRST_STOP_LON = 35.297228 ;
//    private static final double LAST_STOP_LAT = 31.786476;
//    private static final double LAST_STOP_LON = 35.298171;
//    private static final double SQUARED_RADIOS = 0.000000000673;
//    private Boolean status = false;

//    // noam's house
//    private static final double FIRST_STOP_LAT = 32.0782038529416;
//    private static final double FIRST_STOP_LON = 34.84960211717887;
//    private static final double LAST_STOP_LAT = 32.07831045222022;
//    private static final double LAST_STOP_LON = 34.84849725022398;
//    private static final double SQUARED_RADIOS = 0.000000104329;

    // hapardes and end hatzmaut
    private static final double FIRST_STOP_LAT = 32.076458;
    private static final double FIRST_STOP_LON = 34.874041;
    private static final double LAST_STOP_LAT = 32.074879;
    private static final double LAST_STOP_LON = 34.868378;
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

            writeToDB(latitude, longitude);

            String msg = "New Latitude2: " + latitude + " New Longitude2: " + longitude;
            System.out.println(msg);

            Context context = getApplicationContext();
            CharSequence text = msg;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        if (user != null) {
            String emailOfShuttle = user.getEmail();
            numOfShuttle = emailOfShuttle.split("@")[0];
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, (float)minDis, mLocationListener);
                handler.postDelayed(runnable, 60 * 60 * 1000);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, (float)minDis, mLocationListener);
                handler.postDelayed(runnable, 60 * 60 * 1000);
            }
        } catch (SecurityException se) {
            // do nothing
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void writeToDB(double latitude, double longitude) {
        updateStatus(latitude,longitude);
        myRef.child(numOfShuttle).setValue(latitude + ", " + longitude + ", " + status);

//        updateStatus(latitude,longitude);
//        myRef = db.getReference(numOfShuttle);
//        myRef.setValue(latitude + ", " + longitude + ", " + status);
    }

    /**
     *  updating the status of the shuttle.
     *  checking weather the shuttle came in close proximity of the last stop
     *  or if the shuttle left the proximity of the first stop
     *  the radios that was chosen was - 5m (hopefully that's correct)
     *
     *  inCircle =  pow(latitude - FIRST_STOP_LAT,2) + pow(longitude - FIRST_STOP_LON,2) <= SQUARED_RADIOS;
     *  using d^2 < r^2 to determine if the shuttle is in the surrounding of the station
     * @param latitude
     * @param longitude
     */
    public void updateStatus(double latitude, double longitude) {
        //came to last stop
        if (this.status && (pow(latitude - LAST_STOP_LAT, 2) + pow(longitude - LAST_STOP_LON, 2) <= SQUARED_RADIOS)) {
            this.status = false;
        }
        // leaving first stop
        else if (!this.status && (pow(latitude - FIRST_STOP_LAT, 2) + pow(longitude - FIRST_STOP_LON, 2) <= SQUARED_RADIOS)) {
            this.status = true;
        }
    }
}
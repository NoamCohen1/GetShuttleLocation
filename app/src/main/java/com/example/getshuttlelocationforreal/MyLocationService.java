package com.example.getshuttlelocationforreal;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static java.lang.Math.pow;
//import android.support.v4.app.ActivityCompat;

/**
 * Created by Hadas Bar on 1/23/2017.
 */

public class MyLocationService extends Service {
    DatabaseReference myRef;
    String numOfShuttle;
    private Boolean status = false;
    private static final double FIRST_STOP_LAT = 32.0782038529416;
    private static final double FIRST_STOP_LON = 34.84960211717887;
    private static final double LAST_STOP_LAT = 32.07831045222022;
    private static final double LAST_STOP_LON = 34.84849725022398;
    private static final double SQUARED_RADIOS = 0.000000104329;
    FirebaseDatabase db;
    double minDis = 0.5;



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
        } else {
// No user is signed in.
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        //        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        //    return super.onStartCommand(intent, flags, startId);
        //}
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, (float)minDis, mLocationListener);
                writeGuidToSet("GPS_PROVIDER");
                handler.postDelayed(runnable, 60 * 60 * 1000);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, (float)minDis, mLocationListener);
                writeGuidToSet("NETWORK_PROVIDER");
                handler.postDelayed(runnable, 60 * 60 * 1000);
            } else {
                writeGuidToSet("no GPS");
            }
        } catch (SecurityException se) {
            // do nothing
        }
        return super.onStartCommand(intent, flags, startId);
    }

    void writeGuidToSet(String gpsStatus) {

    }


    public void writeToDB(double latitude, double longitude) {
        updateStatus(latitude,longitude);
        myRef = db.getReference(numOfShuttle);
        myRef.setValue(Double.toString(latitude) + ", " + Double.toString(longitude) + ", " + Boolean.toString(status));
    }

    /* updating the status of the shuttle.
checking weather the shuttle came in close proximity of the last stop
or if the shuttle left the proximity of the first stop
the radios that was chosen was - 5m (hopefully that's correct)
*/
    public void updateStatus(double latitude, double longitude) {
        // using d^2 < r^2 to determine if the shuttle is in the surrounding of the station
        //Boolean inCircle = ;
        // came to last stop
        if (this.status && (pow(latitude - LAST_STOP_LAT, 2) + pow(longitude - LAST_STOP_LON, 2) <= SQUARED_RADIOS)) {
            this.status = false;
            return;
        }
        //inCircle =  pow(latitude - FIRST_STOP_LAT,2) + pow(longitude - FIRST_STOP_LON,2) <= SQUARED_RADIOS;
        // leaving first stop
        else if (!this.status && (pow(latitude - FIRST_STOP_LAT, 2) + pow(longitude - FIRST_STOP_LON, 2) <= SQUARED_RADIOS)) {
            this.status = true;
        }
    }
}

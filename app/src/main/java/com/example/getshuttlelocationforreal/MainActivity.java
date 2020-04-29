package com.example.getshuttlelocationforreal;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import android.view.View;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;

//////////
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
///////////

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.getshuttlelocationforreal.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;

import java.lang.Math;

import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity {
    LocationRequest locationRequest;
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        //locationRequest.setInterval(10000);
        //locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    String numOfShuttle;

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

    // noam's house
    private static final double FIRST_STOP_LAT = 32.078324;
    private static final double FIRST_STOP_LON = 34.848392 ;
    private static final double LAST_STOP_LAT = 32.078230;
    private static final double LAST_STOP_LON = 34.849451;
    private static final double SQUARED_RADIOS = 0.000000004038;
    private Boolean status = false;

    DatabaseReference myRef;
    //private FusedLocationProviderClient client;

    private void checkPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        //client = LocationServices.getFusedLocationProviderClient(this);

        //EditText mFullName = findViewById(R.id.et_name);

        ViewPager viewPager = findViewById(R.id.viewPager);

        TextView text = (TextView)findViewById(R.id.text);

//        Intent intent = getIntent();
//        numOfShuttle = intent.getStringExtra("NumOfShuttle");
//        text.setText(numOfShuttle);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String emailOfShuttle = user.getEmail();
            numOfShuttle = emailOfShuttle.split("@")[0];
        } else {
// No user is signed in.
        }
//        AuthenticationPagerAdapter pagerAdapter = new AuthenticationPagerAdapter(getSupportFragmentManager());
//        pagerAdapter.addFragmet(new LoginFragment());
//        pagerAdapter.addFragmet(new RegisterFragment());
//        viewPager.setAdapter(pagerAdapter);


        final LocationListener locationListener = new LocationListener() {
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

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        float f = (float) 0.5;

        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                //locationSettingsResponse.getLocationSettingsStates().
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        // REQUEST_CHECK_SETTINGS = 1
                        resolvable.startResolutionForResult(MainActivity.this, 1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, f, locationListener);
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
        if (this.status && (pow(latitude - LAST_STOP_LAT,2) + pow(longitude - LAST_STOP_LON,2) <= SQUARED_RADIOS)) {
            this.status = false;
            return;
        }
        //inCircle =  pow(latitude - FIRST_STOP_LAT,2) + pow(longitude - FIRST_STOP_LON,2) <= SQUARED_RADIOS;
        // leaving first stop
        else if (!this.status && (pow(latitude - FIRST_STOP_LAT,2) + pow(longitude - FIRST_STOP_LON,2) <= SQUARED_RADIOS)) {
            this.status = true;
        }
    }

    public void writeToDB(double latitude, double longitude) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();

        myRef = db.getReference(numOfShuttle);
        myRef.setValue(Double.toString(latitude) + ", " + Double.toString(longitude) +", " + Boolean.toString(status));
    }

   /* public boolean active(double latitude, double longitude) {

    }*/

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }
}
/**
 * This class sets up location setting on device and calls MyLocationService service.
 */

package com.example.getshuttlelocationforreal;

import android.Manifest;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //checking location permissions and requesting high accuracy for location
        checkPermission();
        createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        //checking location setting
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied.
            }
        });
        //asks user permission to turn on location if the location is off
        task.addOnFailureListener(this, new OnFailureListener() {
           @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, 1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        //ignore the error.
                    }
                }
            }
        });
        //calls the service that gets the device's location
        startService(new Intent(getApplicationContext(), MyLocationService.class));
    }

    /**
     * Checks if application has permission to use location from the device.
     */
    private void checkPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    /**
     * Adding 'PRIORITY_HIGH_ACCURACY' to LocationRequest member.
     */
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Application returns to login activity after the user logs out.
     */
    public void logout(View view) {
        //log out of firebase
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}
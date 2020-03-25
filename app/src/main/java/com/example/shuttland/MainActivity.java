package com.example.shuttland;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // try
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.nearStation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityNearStation();
            }
        });
        button = (Button) findViewById(R.id.navigation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityNavigation();
            }
        });
    }

    public void openActivityNearStation() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void openActivityNavigation() {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

}

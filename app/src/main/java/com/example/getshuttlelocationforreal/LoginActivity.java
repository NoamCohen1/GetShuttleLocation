/**
 * This class logs in user or directs to register activity.
 */

package com.example.getshuttlelocationforreal;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText mFullName,mPassword;
    Button mLoginBtn;
    TextView mCreateBtn;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getting user info from login activity
        mFullName = findViewById(R.id.et_name);
        mPassword = findViewById(R.id.et_password);

        fAuth = FirebaseAuth.getInstance();
        //get instances of objects in the login screen
        mLoginBtn = findViewById(R.id.btn_login);
        mCreateBtn = findViewById(R.id.createText);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numOfShuttle = mFullName.getText().toString().trim();
                String fullName = numOfShuttle + "@email.com";
                String password = mPassword.getText().toString().trim() + "11111";

                //check if all required fields were filled out by user
                if(TextUtils.isEmpty(fullName)){
                    mFullName.setError("Full name is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required.");
                    return;
                }
                //authenticate the users info exists
                fAuth.signInWithEmailAndPassword(fullName,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else {
                            Toast.makeText(LoginActivity.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
        // go to register activity screen
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });

    }
}

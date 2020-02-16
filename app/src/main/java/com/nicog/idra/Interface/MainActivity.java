package com.nicog.idra.Interface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nicog.idra.Interface.tutorial.Tutorial;
import com.nicog.idra.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth;
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user == null){
                    startTutorial();
                }else{
                    checkPermissions();
                }
            }
        });

    }

    private void startTutorial(){
        Intent i = new Intent(this, Tutorial.class);
        startActivity(i);
        this.finish();
    }

    private void checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            openApp();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
    }

    private void openApp(){
        Intent i = new Intent(this, Map.class);
        startActivity(i);
        finish();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 123){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openApp();
            }else{
                Toast.makeText(this, R.string.no_location, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

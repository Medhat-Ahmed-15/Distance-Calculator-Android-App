package com.example.distancecoveredcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN=5000;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


   new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    NextActiviy();

                    //Call this when your activity is done and should be closed.
                    // The ActivityResult is propagated back to whoever launched you via onActivityResult().
                    finish();

                }
            },SPLASH_SCREEN);
        }







    public void NextActiviy()

                      {
                    Intent intent = new Intent(getApplicationContext(), MainPage.class);
                    startActivity(intent);
                      }




    }

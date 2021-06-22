package com.example.distancecoveredcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainPage extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener {


    //private WifiManager wifiManager;

    private static final String TAG = "MapActivity";
    private volatile boolean stopThread = false;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mStartFusedLocationProviderClient;
    private FusedLocationProviderClient mEndFusedLocationProviderClient;


    public ConstraintLayout layout;
    public Button start_button;
    public Button calculate_button;
    public Button stop_button;
    public TextView distance_covered_text;
    public TextView total_fees_text;
    public EditText input_fees;


    public LatLng currentStartLatLng;
    public LatLng currentEndtLatLng;


    public double fees;
    double latitude;
    double longitude;

    public int startButtonFlag = 0;
    public int endButtonFlag = 0;
    private Boolean wifiConnected = false;
    private Boolean mobileConnected = false;

    double prevLatitude;
    double prevLongitude;
    double totalDistance;
    double distanceBetween;

    String security;
    FirebaseDatabase database;
    DatabaseReference myRef;




    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


         database = FirebaseDatabase.getInstance();
         myRef = database.getReference("Security");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                security = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read
                Toast.makeText(MainPage.this, "Failed to read value", Toast.LENGTH_LONG).show();
            }
        });




        getLocationPermission();


        if (!CheckNetworkConnection()) {
            Toast.makeText(MainPage.this, "No Internet Connection Please Connect to a wifi or mobile network❌❌❌", Toast.LENGTH_LONG).show();

        }

        if (!CheckLocationServices()) {
            Toast.makeText(MainPage.this, "Location Services Is Not Enabled❌❌❌", Toast.LENGTH_SHORT).show();
            buildAlertMessageNoGps();

        }


        layout = (ConstraintLayout) findViewById(R.id.layout);

        start_button = (Button) findViewById(R.id.start_button);
        calculate_button = (Button) findViewById(R.id.calculate_button);
        stop_button = (Button) findViewById(R.id.stop_button);
        distance_covered_text = (TextView) findViewById(R.id.distance_covered_text);
        total_fees_text = (TextView) findViewById(R.id.total_fees_text);
        input_fees = (EditText) findViewById(R.id.input_fees);
        //wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {





                if (security.equals("allow")) {

                if (CheckNetworkConnection()) {

                    if (CheckLocationServices()) {
                        if (input_fees.getText().toString().equals("")) {

                            Toast.makeText(MainPage.this, "Please Enter an Amount Of Fees 💲💲💲", Toast.LENGTH_LONG).show();

                        } else {


                            if (mLocationPermissionsGranted) {

                                startButtonFlag = 1;


                                getDeviceStartLocation();
                                getCurrentLocation();


                                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(),
                                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    return;
                                }
                                mMap.setMyLocationEnabled(true);
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                            } else {
                                Toast.makeText(MainPage.this, "❌Location Not Granted❌", Toast.LENGTH_LONG).show();

                            }

                        }
                    } else {
                        Toast.makeText(MainPage.this, "Location Services must be enabled first🛰", Toast.LENGTH_LONG).show();
                        buildAlertMessageNoGps();


                    }


                } else {
                    Toast.makeText(MainPage.this, "No Internet Connection Please Connect to a wifi or mobile network❌❌❌", Toast.LENGTH_LONG).show();

                }

            }
                else {
                    Toast.makeText(MainPage.this, "Sorry This application was blocked by the admin❗❗❗", Toast.LENGTH_LONG).show();

                }
            }
        });


        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (startButtonFlag == 0) {

                    Toast.makeText(MainPage.this, "Starting destination is required first😵😵", Toast.LENGTH_SHORT).show();
                } else {
                    endButtonFlag = 1;

                    if (mLocationPermissionsGranted) {


                        getDeviceEndLocation();
                        stopNotificationService();
                        stopGettingCurrentLocation();
                        stopThread=true;

                        Toast.makeText(MainPage.this, "Stopped", Toast.LENGTH_LONG).show();
                        distance_covered_text.setText("press calculate to show results");
                        distance_covered_text.setTextColor(Color.BLACK);



                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);

                    }


                }
            }
        });


        calculate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (startButtonFlag == 1 && endButtonFlag == 1) {
                    if (input_fees.getText().toString().equals("")) {

                        Toast.makeText(MainPage.this, "Please enter an amount of fees first💲💲💲", Toast.LENGTH_LONG).show();
                        input_fees.setHint("input fees required");

                    } else {
                        getDistanceCalculated();
                        getFeesCalculated();

                    }
                } else {
                    Toast.makeText(MainPage.this, "Please Make Sure that you've started and ended the destination 🥵🥵", Toast.LENGTH_LONG).show();
                }

            }
        });


        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

    }


    private void getDeviceStartLocation() {



        mStartFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mStartFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location=task.getResult();
                if(location!=null)
                {
                    currentStartLatLng= new LatLng(location.getLatitude(), location.getLongitude());

                    startNotificationService();
                    distance_covered_text.setText("Estimating Distance....");
                    distance_covered_text.setTextColor(Color.GREEN);

                    moveCamera(currentStartLatLng, DEFAULT_ZOOM);

                    prevLatitude=location.getLatitude();
                    prevLongitude=location.getLongitude();



                }

            }
        });



    }




    private void getDeviceEndLocation(){


        mEndFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mEndFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                Location location=task.getResult();
                if(location!=null)
                {
                    currentEndtLatLng= new LatLng(location.getLatitude(), location.getLongitude());

                    moveCamera(currentEndtLatLng, DEFAULT_ZOOM);

                }

            }
        });



    }





    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MainPage.this);
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

            {

                mLocationPermissionsGranted = true;
                initMap();
            }
            else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }

        else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;

                    //initialize our map
                    initMap();
                }
            }
        }
    }


    private void getDistanceCalculated()
    {

        distance_covered_text.setTextColor(Color.BLACK);



        /*float results[]=new float[10];
        Location.distanceBetween(startLatitude,startLongitude,endLatitude,endLongitude,results);
        distance = results[0]/1000;*/

        distance_covered_text.setText("Distance Covered:     " + String.format("%.2f", distanceBetween )+" "+ "Km");


    }



    private void getFeesCalculated()
    {
        try {
            fees =distanceBetween* Double.parseDouble(input_fees.getText().toString());
            total_fees_text.setText("Total fees :     " + String.format("%.3f", fees));
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(MainPage.this, "Invalid Input😫😫", Toast.LENGTH_SHORT).show();

        }

    }





    private Boolean CheckNetworkConnection()
    {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();

        if(networkInfo!=null&&networkInfo.isConnected())
        {
            wifiConnected=networkInfo.getType()==ConnectivityManager.TYPE_WIFI;
            mobileConnected=networkInfo.getType()==ConnectivityManager.TYPE_MOBILE;

            /*if (wifiConnected)
            {
                Toast.makeText(MainActivity.this, "Wifi Connected", Toast.LENGTH_SHORT).show();
                return true;

            }

            if (mobileConnected)
            {
                Toast.makeText(MainActivity.this, "Mobile Network Connected", Toast.LENGTH_SHORT).show();
                return true;

            }*/

            return  true;

        }
        else
        {
            //Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            return  false;


        }
    }



    private Boolean CheckLocationServices()
    {
        LocationManager lm = (LocationManager)getBaseContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user

            return  false;

        }
        else
        {
            return true;
        }

    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker)
    {

    }

    private boolean isNotificationServiceRunning()
    {
        ActivityManager activityManager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager!=null)
        {
            for(ActivityManager.RunningServiceInfo service:activityManager.getRunningServices(Integer.MAX_VALUE))
            {
                if(NotificationService.class.getName().equals(service.service.getClassName()))
                {
                    if(service.foreground)
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startNotificationService()
    {
        if(!isNotificationServiceRunning())
        {
            Intent intent=new Intent(getApplicationContext(), NotificationService.class);
            intent.setAction(Constants.ACTION_START_NOTIFICATION_SERVICE);
            startService(intent);


        }
    }


    private void stopNotificationService()
    {
        if(isNotificationServiceRunning())
        {
            Intent intent=new Intent(getApplicationContext(), NotificationService.class);
            intent.setAction(Constants.ACTION_STOP_NOTIFICATION_SERVICE);
            startService(intent);


        }
    }




    private void getCurrentLocation()
    {
        totalDistance=0.0;
        distanceBetween=0.0;
        prevLatitude=0.0;
        prevLongitude=0.0;
        latitude=0.0;
        longitude=0.0;

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    latitude = locationResult.getLastLocation().getLatitude();
                    longitude = locationResult.getLastLocation().getLongitude();



                    currentStartLatLng= new LatLng(latitude, longitude);
                    moveCamera(currentStartLatLng, DEFAULT_ZOOM);




                    float results[]=new float[10];
                    Location.distanceBetween(prevLatitude,prevLongitude,latitude,longitude,results);
                    distanceBetween = results[0]/1000;

                    distanceBetween=distanceBetween+totalDistance;

                    Log.d("LOCATION_UPDATE", latitude + "  " + longitude+ "   "+"PREVIOUS_LOCATION_UPDATE"+"   "+ prevLatitude + "  " + prevLongitude+"   "+distanceBetween);


                    prevLongitude=longitude;
                    prevLatitude=latitude;

                    totalDistance=distanceBetween;


                }
            }
        };



        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);//set the interval in which you want to get locations
        locationRequest.setFastestInterval(2000);//if a location is available sooner you can get it early
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationRequest.setSmallestDisplacement(10f);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());

    }


    private void stopGettingCurrentLocation()
    {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
    }


    @Override
    protected void onPause() {

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                security = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read
                Toast.makeText(MainPage.this, "Failed to read value", Toast.LENGTH_LONG).show();
            }
        });
        super.onPause();
    }
}
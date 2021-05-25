package com.example.distancecoveredcalculator;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class NotificationService extends Service {
    double prevLatitude;
    double prevLongitude;
    double totalDistance;
    double distanceBetween;


    @Nullable
    @Override
    /**
     IBinder: To create a bound service, you must define the interface that
     specifies how a client can communicate with the service.
     This interface between the service and a client must be
     an implementation of IBinder and is what your service must return from the onBind() callback method**/

    /**
     To provide binding for a service, you must implement the onBind() callback method.
     This method returns an IBinder object that defines the programming interface that
     clients can use to interact with the service.**/

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationService() {


        String channelId = "loaction_notification_channnel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();

        /**
         A PendingIntent is a token that you give to a foreign application (e.g. NotificationManager, AlarmManager, Home Screen AppWidgetManager,
         or other 3rd party applications), which allows the foreign application to use your
         application's permissions to execute a predefined piece of code.
         If you give the foreign application an Intent, it will execute your
         Intent with its own permissions. But if you give the foreign application a PendingIntent,
         that application will execute your Intent using your application's permission.**/
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Distance Calculator");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);//Set the default notification options that will be used
        builder.setContentText("Start Driving🚗");
        builder.setContentIntent(pendingIntent);//Setting this flag will make it so the notification is automatically canceled when the user clicks it in the panel. The PendingIntent set with setDeleteIntent will be broadcast when the notification is canceled.
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId, "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }


        startForeground(Constants.LOCATION_SERVICE_ID,builder.build());
    }


    private void stopLocationService()
    {
        stopForeground(true);
        stopSelf();//Stop the service, if it was previously started. This is the same as calling


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent!=null)
        {
            String action=intent.getAction();
            if(action!=null)
            {
                if(action.equals(Constants.ACTION_START_NOTIFICATION_SERVICE))
                {
                    startLocationService();
                }

                else if(action.equals(Constants.ACTION_STOP_NOTIFICATION_SERVICE))
                {
                    stopLocationService();
                }
            }
        }



        return super.onStartCommand(intent, flags, startId);
    }
}


package id.orchys.app.wakemeapp.service;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import id.orchys.app.wakemeapp.MapsActivity;
import id.orchys.app.wakemeapp.R;

/**
 * Created by Dhanar J Kusuma on 20/05/2017.
 */

public class GpsService extends Service {
    public static final String INTENT_NOTIF_SWIPE = "intent_notif_swipe";
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Location pinnedLocation;
    private static final long updatePerSecondTime = 3000;
    private boolean isRunning = false;
    public static final String INTENT_DISTANCE_UPDATE_NAME = "intent_distant_update_name";
    public static final String INTENT_DISTANCE_UPDATE = "intent_distant_update";
    public static final String INTENT_LOCATION_UPDATE = "intent_location_update";
    public static final String INTENT_RINGTONE_IS_RINGING_NAME = "intent_ringtone_name";
    public static final String INTENT_RINGTONE_IS_RINGING = "intent_ringtone";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v("STARTED_SERVICE", "STARTING SERVICE.");
        this.locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        //noinspection MissingPermission
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        Bundle bundle = intent.getExtras();
        if(bundle!=null) {
            pinnedLocation = bundle.getParcelable(INTENT_LOCATION_UPDATE);
            Log.e("test", String.valueOf(pinnedLocation.getLatitude()) + " " + String.valueOf(pinnedLocation.getLongitude()));
        }

        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onCreate() {
        this.locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(pinnedLocation != null){
                    float[] dist = new float[1];
                    Location.distanceBetween(
                            location.getLatitude(),
                            location.getLongitude(),
                            pinnedLocation.getLatitude(),
                            pinnedLocation.getLongitude(),
                            dist);


                    if(dist[0] < 20){
                        if(locationManager != null){
                           locationManager.removeUpdates(locationListener);
                        }
                        Intent ringtoneIntent = new Intent(INTENT_DISTANCE_UPDATE_NAME);
                        ringtoneIntent.putExtra(INTENT_RINGTONE_IS_RINGING, true);
                        sendBroadcast(ringtoneIntent);
                        sendNotification();
                    }

                    Intent broadCastIntent = new Intent(INTENT_DISTANCE_UPDATE_NAME);
                    broadCastIntent.putExtra(INTENT_DISTANCE_UPDATE, dist[0]);
                    sendBroadcast(broadCastIntent);

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent settingPanel = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                settingPanel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(settingPanel);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.locationManager != null){
            this.locationManager.removeUpdates(locationListener);
        }
        //unregisterReceiver(this.broadcastReceiver);
    }

    public void sendNotification(){
        // Create a PendingIntent to be fired upon deletion of a Notification.
        Intent deleteIntent = new Intent(INTENT_DISTANCE_UPDATE_NAME);
        deleteIntent.putExtra(INTENT_NOTIF_SWIPE, true);
        PendingIntent mDeletePendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                2323 /* requestCode */, deleteIntent, 0);

        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{ 1000, 1000, 1000, 1000, 1000 })
                    .setContentTitle("You have arrived to ur destination.")
                    .setContentText("Tap this notif, to turn off the alert.")
                    .setDeleteIntent(mDeletePendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }

}

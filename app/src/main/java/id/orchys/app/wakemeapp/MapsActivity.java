package id.orchys.app.wakemeapp;

import android.*;
import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.orchys.app.wakemeapp.service.GpsService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker mDestLocationMarker;
    LocationRequest mLocationRequest;
    private GpsService gpsService;
    private Intent gpsServiceIntent;
    private Button startService;
    private Location pinnedLocation;
    private BroadcastReceiver broadcastReceiver;

    private Uri notification;
    private Ringtone r;
    private MediaPlayer player;

    private View parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startService = (Button) findViewById(R.id.startService);
        gpsService = new GpsService();
        gpsServiceIntent = new Intent(this, GpsService.class);


        parentLayout = findViewById(R.id.parentLayout);

        //notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        player = MediaPlayer.create(this, notification);
        player.setLooping(true);


        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isServiceRunning(GpsService.class)){
                    stopService(gpsServiceIntent);
                    buildGoogleApiClient();
                    displaySnackBar("Service stopped...");
                    startService.setText("Start Service");

                }else{
                    if(pinnedLocation!=null){
                        gpsServiceIntent.putExtra(GpsService.INTENT_LOCATION_UPDATE, pinnedLocation);
                        startService(gpsServiceIntent);
                        displaySnackBar("Service started...");
                        startService.setText("End Service");
                    }else{
                        displaySnackBar("No Destination Location.");
                    }

                }
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Float aFloat = intent.getExtras().getFloat(GpsService.INTENT_DISTANCE_UPDATE);

                if(intent.getExtras().getBoolean(GpsService.INTENT_RINGTONE_IS_RINGING, false)){
                    if(!player.isPlaying()){
                        player.start();
                    }
                    stopService(gpsServiceIntent);
                    buildGoogleApiClient();


                }

                if(intent.getExtras().getBoolean(GpsService.INTENT_NOTIF_SWIPE, false)){
                    if(player.isPlaying()){
                        player.stop();
                    }

                }
            }
        };
        registerReceiver(this.broadcastReceiver, new IntentFilter(GpsService.INTENT_DISTANCE_UPDATE_NAME));
    }

    private void displayToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displaySnackBar(String message){
        Snackbar snack = Snackbar.make(parentLayout, message, Snackbar.LENGTH_LONG);
        snack.show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mDestLocationMarker != null){
                    mDestLocationMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Destination Point");


                mDestLocationMarker = mMap.addMarker(markerOptions);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                pinnedLocation = location;
            }
        });
        buildGoogleApiClient();
        //noinspection MissingPermission
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        stopLocationUpdate();

    }

    private void stopLocationUpdate(){
        if(mGoogleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void startLocationUpdate(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean isServiceRunning(Class<?> serviceClass){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        // Loop through the running services
        for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                // If the service is running then return true
                return true;
            }
        }
        return false;
    }
}

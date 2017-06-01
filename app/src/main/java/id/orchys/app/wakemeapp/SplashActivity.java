package id.orchys.app.wakemeapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSION = 100;
    private final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        runtimePermission();


    }

    private void runtimePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isPermissionGranted()){
            requestPermissions(PERMISSIONS, REQUEST_PERMISSION);
        }else{
            goToMainActivity();
        }
    }

    private boolean isPermissionGranted() {
        boolean isGranted = true;
        //Find permission that not granted, then break
        for (String permission: PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                isGranted = false;
                break;
            }
        }
        return isGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGivenPermisson = true;

        if(requestCode == REQUEST_PERMISSION){
            for(int grant : grantResults){
                if(grant != PackageManager.PERMISSION_GRANTED){
                    isGivenPermisson = false;
                    break;
                }
            }
        }

        if(!isGivenPermisson){
            new AlertDialog.Builder(this)
                    .setMessage(R.string.ask_permission)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            runtimePermission();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    })
                    .create()
                    .show();
        }else{
            goToMainActivity();
        }
    }

    //Move to Main Activity.
    public void goToMainActivity(){
        new CountDownTimer(2000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                //Event when count down time tick.
            }

            @Override
            public void onFinish() {
                //Open Home Activity and destroy this activity.
                Intent mainPage = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(mainPage);
                finish();
            }
        }.start();

    }
}

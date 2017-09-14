package com.oohyugi.exampleheremap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MyLocationCallBack {

    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final long FIVE_MINUTES = 5000;

    // map embedded in the map fragment
    private Map map = null;

    // map fragment embedded in this activity
    private MapFragment mapFragment = null;
    MyLocationService myLocationService;
    GeoPosition mGeoPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        myLocationService = MyLocationService.instance();
//        myLocationService.setCurrentLocation(MainActivity.this);



    }

  
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }

    private void initialize() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    startService(new Intent(MainActivity.this,MyLocationService.class));
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Log.e( "run: ","Timer IN" );
//            }
//        }, 1, FIVE_MINUTES);
        myLocationService = MyLocationService.instance();
        myLocationService.setCurrentLocation(this);

    }

    private void setMarker() {
        map.setCenter(mGeoPosition.getCoordinate(), Map.Animation.BOW);
        mapFragment.getPositionIndicator().setVisible(true);
        map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 1.2);
    }

    @Override
    public void initGeoPositon(GeoPosition geoPosition) {
        this.mGeoPosition = geoPosition;
        setMarker();

    }

}

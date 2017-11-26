package com.perix.fusedlocationsample;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private TextView txtOutput;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private String LOG_TAG = "MainActivityPerry";

    private String permissionsAsk[];
    private int KEY_PERMISSION = 0;
    private PermissionResults permissionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate() Called");
        setContentView(R.layout.activity_main);

        askPermissionsForGPS();

        txtOutput = (TextView) findViewById(R.id.txtOutput);
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart() Called");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop() Called");
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "onConnected() called");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(500);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(LOG_TAG, "onLocationChanged() called");
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());
        txtOutput.setText(lat + "," + lon);
    }



    public MainActivity askPermissions(String permissions[]) {
        this.permissionsAsk = permissions;
        return MainActivity.this;
    }

    public void askCompactPermission(String permission, PermissionResults permissionResult) {
        KEY_PERMISSION = 200;
        permissionsAsk = new String[]{permission};
        this.permissionResult = permissionResult;
        internalRequestPermission(permissionsAsk);
    }

    public void askCompactPermissions(String permissions[], PermissionResults permissionResult) {
        KEY_PERMISSION = 200;
        permissionsAsk = permissions;
        this.permissionResult = permissionResult;
        internalRequestPermission(permissionsAsk);
    }

    private void internalRequestPermission(String[] permissionAsk) {
        String arrayPermissionNotGranted[];
        ArrayList<String> permissionsNotGranted = new ArrayList<>();

        for (int i = 0; i < permissionAsk.length; i++) {
            if (!isPermissionGranted(MainActivity.this, permissionAsk[i])) {
                permissionsNotGranted.add(permissionAsk[i]);
            }
        }


        if (permissionsNotGranted.isEmpty()) {

            if (permissionResult != null) {
                permissionResult.permissionGranted();
            }
        } else {

            arrayPermissionNotGranted = new String[permissionsNotGranted.size()];
            arrayPermissionNotGranted = permissionsNotGranted.toArray(arrayPermissionNotGranted);
            ActivityCompat.requestPermissions(MainActivity.this, arrayPermissionNotGranted, KEY_PERMISSION);
        }


    }

    public boolean isPermissionGranted(Context context, String permission) {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) || (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == KEY_PERMISSION) {
            boolean granted = true;

            for (int grantResult : grantResults) {
                if (!(grantResults.length > 0 && grantResult == PackageManager.PERMISSION_GRANTED))
                    granted = false;
            }
            if (permissionResult != null) {
                if (granted) {
                    permissionResult.permissionGranted();
                } else {
                    permissionResult.permissionDenied();
                }
            }
        } else {
            Log.e(LOG_TAG, "permissionResult callback was null");
        }
    }

    private void setupLocationSettingsBuilder() {
        Log.d(LOG_TAG, "setupLocationSettingsBuilder() called");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();
        mGoogleApiClient.connect();
//        SettingsClient client = LocationServices.getSettingsClient(this);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(LOG_TAG, "SUCCESS ");

                    Log.d(LOG_TAG, "isBlePresent " + result.getLocationSettingsStates().isBlePresent());
                    Log.d(LOG_TAG, "isBleUsable " + result.getLocationSettingsStates().isBleUsable());
                    Log.d(LOG_TAG, "isGpsPresent " + result.getLocationSettingsStates().isGpsPresent());
                    Log.d(LOG_TAG, "isGpsUsable " + result.getLocationSettingsStates().isGpsUsable());
                    Log.d(LOG_TAG, "isLocationPresent " + result.getLocationSettingsStates().isLocationPresent());
                    Log.d(LOG_TAG, "isLocationUsable " + result.getLocationSettingsStates().isLocationUsable());
                    Log.d(LOG_TAG, "isNetworkLocationPresent " + result.getLocationSettingsStates().isNetworkLocationPresent());
                    Log.d(LOG_TAG, "isNetworkLocationUsable " + result.getLocationSettingsStates().isNetworkLocationUsable());

                    if(!result.getLocationSettingsStates().isLocationUsable()) {
                        try {
                            status.startResolutionForResult(MainActivity.this, 10);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.d(LOG_TAG, "RESOLUTION_REQUIRED ");
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    10);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(LOG_TAG, "SETTINGS_CHANGE_UNAVAILABLE ");
                        break;
                }
            }
        });
//        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
//            @Override
//            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
//                Log.d(LOG_TAG, "onComplete() called");
//                try {
//                    LocationSettingsResponse response = task.getResult(ApiException.class);
//
//                    mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
//                        .addApi(LocationServices.API)
//                        .addConnectionCallbacks(MainActivity.this)
//                        .addOnConnectionFailedListener(MainActivity.this)
//                        .build();
//                    mGoogleApiClient.connect();
//
//                    Log.d(LOG_TAG, "isBlePresent " + response.getLocationSettingsStates().isBlePresent());
//                    Log.d(LOG_TAG, "isBleUsable " + response.getLocationSettingsStates().isBleUsable());
//                    Log.d(LOG_TAG, "isGpsPresent " + response.getLocationSettingsStates().isGpsPresent());
//                    Log.d(LOG_TAG, "isGpsUsable " + response.getLocationSettingsStates().isGpsUsable());
//                    Log.d(LOG_TAG, "isLocationPresent " + response.getLocationSettingsStates().isLocationPresent());
//                    Log.d(LOG_TAG, "isLocationUsable " + response.getLocationSettingsStates().isLocationUsable());
//                    Log.d(LOG_TAG, "isNetworkLocationPresent " + response.getLocationSettingsStates().isNetworkLocationPresent());
//                    Log.d(LOG_TAG, "isNetworkLocationUsable " + response.getLocationSettingsStates().isNetworkLocationUsable());
//                    // All location settings are satisfied. The client can initialize location
//                    // requests here.
//
//                    if(!response.getLocationSettingsStates().isLocationUsable()) {
//
//                    }
////             ...
//                } catch (ApiException exception) {
//                    switch (exception.getStatusCode()) {
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            // Location settings are not satisfied. But could be fixed by showing the
//                            // user a dialog.
//                            Log.d(LOG_TAG, "RESOLUTION_REQUIRED called");
//                            try {
//                                // Cast to a resolvable exception.
//                                ResolvableApiException resolvable = (ResolvableApiException) exception;
//                                // Show the dialog by calling startResolutionForResult(),
//                                // and check the result in onActivityResult().
//                                resolvable.startResolutionForResult(
//                                        MainActivity.this,
//                                        10);
//                            } catch (IntentSender.SendIntentException e) {
//                                // Ignore the error.
//                            } catch (ClassCastException e) {
//                                // Ignore, should be an impossible error.
//                            }
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                            // Location settings are not satisfied. However, we have no way to fix the
//                            // settings so we won't show the dialog.
//                            Log.d(LOG_TAG, "SETTINGS_CHANGE_UNAVAILABLE called");
////                     ...
//                            break;
//                    }
//                }
//            }
//        });
//        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                Log.d(LOG_TAG, "addOnSuccessListener() called");
//                mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
//                        .addApi(LocationServices.API)
//                        .addConnectionCallbacks(MainActivity.this)
//                        .addOnConnectionFailedListener(MainActivity.this)
//                        .build();
//                mGoogleApiClient.connect();
//            }
//        });
//
//        task.addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d(LOG_TAG, "addOnFailureListener() called");
//                int statusCode = ((ApiException) e).getStatusCode();
//                switch (statusCode) {
//                    case CommonStatusCodes.RESOLUTION_REQUIRED:
//                        // Location settings are not satisfied, but this can be fixed
//                        // by showing the user a dialog.
//                        Log.d(LOG_TAG, "RESOLUTION_REQUIRED called");
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            ResolvableApiException resolvable = (ResolvableApiException) e;
//                            resolvable.startResolutionForResult(MainActivity.this,
//                                    10);
//                        } catch (IntentSender.SendIntentException sendEx) {
//                            // Ignore the error.
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        // Location settings are not satisfied. However, we have no way
//                        // to fix the settings so we won't show the dialog.
//                        Log.d(LOG_TAG, "SETTINGS_CHANGE_UNAVAILABLE called");
//                        break;
//                }
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, requestCode + " " + resultCode, Toast.LENGTH_LONG);
        Log.d(LOG_TAG, "onActivityResult called" + requestCode + "," + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void askPermissionsForGPS() {
        Log.d(LOG_TAG, "askPermissionsForGPS() Called");
        if (isPermissionGranted(MainActivity.this, PermissionUtils.Manifest_ACCESS_FINE_LOCATION)) {
            showToast("Permission available");
            Log.d(LOG_TAG, "Permission available");
            setupLocationSettingsBuilder();
        } else {
            askCompactPermissions(new String[]{PermissionUtils.Manifest_ACCESS_FINE_LOCATION}, new PermissionResults() {
                @Override
                public void permissionGranted() {
                    showToast("Permission granted");
                    Log.d(LOG_TAG, "Permission granted");
                    setupLocationSettingsBuilder();
                }

                @Override
                public void permissionDenied() {
                    showToast("Permission denied");
                    Log.i(LOG_TAG, "User denied permission");
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy() called");
        super.onDestroy();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}

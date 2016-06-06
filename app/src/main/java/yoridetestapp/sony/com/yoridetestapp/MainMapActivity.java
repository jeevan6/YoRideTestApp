package yoridetestapp.sony.com.yoridetestapp;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Location mCurrentLocation;
    Location mFinalLocation;
    LatLng mFinalLatLng;
    Time mLastUpdateTime;
    Context context;
    Marker initMarker;
    Marker currMarker;
    Marker FinalMarker;
    String FinalPlace;
    View sView;
    DownloadTask downloadTask;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    final static int REQUEST_CHECK_SETTINGS = 2;
    boolean mRequestingLocation=true;
    LocationRequest mLocationRequest;
    Bitmap mDotMarkerBitmap;
    ArrayList<LatLng> markerPoints;
    ArrayList<LatLng> currPoints;
    PolylineOptions currPolyLine;
    LatLngBounds.Builder builder;
    Utility util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main_map);
        markerPoints = new ArrayList<>();
        currPoints = new ArrayList<>();
        util = new Utility();
        buildGoogleApiClient();
        connectGoogleApiClient();
        checkAndConnectLocationService();
        createLocationRequest();
        mRequestingLocation =false;
        sView = (View) findViewById(R.id.search);
        sView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPlacesActivity();
            }
        });
        setUpMapIfNeeded();

    }

    private void checkAndConnectLocationService() {
        if(isLocationServiceEnabled()){
            Log.d("XXXX","LocationEnabled");
            Toast.makeText(this,"LocationEnabled",Toast.LENGTH_SHORT).show();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        else{
            Toast.makeText(this,"LocationNotEnabled",Toast.LENGTH_SHORT).show();
            LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, locationSettingsRequestBuilder.build());
            result.setResultCallback(mResultCallbackFromSettings);

        }
    }

    private void connectGoogleApiClient() {
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
            Log.d("XXXX","Status"+mGoogleApiClient.isConnected());
        }
        else
            Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();
    }


    private void callPlacesActivity() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d("XXXX","Building Api Client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(LocationServices.API)
                .build();
        if(mGoogleApiClient!=null){
            Log.d("XXXX","Built Api Client");
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkAndConnectLocationService();
        if (mGoogleApiClient.isConnected() && mRequestingLocation) {

            startLocationUpdates();
        }
        setUpMapIfNeeded();
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null){
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
        if(mLastLocation==null){
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation!=null){
               addInitalMarker();
            }
        }
    }
    private void setUpMap() {
        if(mLastLocation!=null){
            Log.d("XXXX","adding marker");
        addInitalMarker();
        }
    }

    private void addInitalMarker() {
        LatLng mLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        initMarker = mMap.addMarker(new MarkerOptions().position(mLatLng).title("Marker"));
        BitmapGenerator();
        //initMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_xl));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 10));
        currPolyLine = new PolylineOptions();

    }

    private void UpdateCurrentMarker(LatLng CurrLatLng,int mode){
        LatLng prevLatLng;

        //Toast.makeText(this, "Adding current", Toast.LENGTH_SHORT).show();
        //Log.d("XXXX", "Change" + CurrLatLng.latitude + " " + CurrLatLng.longitude);
        if(currMarker!=null){
            prevLatLng = currMarker.getPosition();
            currMarker.remove();
        }
        else{
            prevLatLng = initMarker.getPosition();
        }
        double Rot = util.angleFromCoordinate(initMarker.getPosition().latitude,initMarker.getPosition().longitude,prevLatLng.latitude,prevLatLng.longitude);
        currMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(util.getMarkerIcon(this))).position(CurrLatLng));
        currMarker.setFlat(true);
        currMarker.setRotation((float) Rot);
        //currMarker.setAnchor(15,22);
        currPoints.add(CurrLatLng);
        currPolyLine.add(CurrLatLng);
        currPolyLine.width(6);
        currPolyLine.color(Color.BLUE);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrLatLng, 13));
        UpdateCurrPolyline();

    }

    private void UpdateCurrPolyline() {
        mMap.addPolyline(currPolyLine);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        //Log.d("XXXX","connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null)
        //Log.d("XXXX",mLastLocation.getLatitude()+" "+mLastLocation.getLongitude());
        setUpMap();
        if (mRequestingLocation) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        Log.d("XXXX","location Update statrted");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //mRequestingLocation =false;
        stopLocationUpdates();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //mRequestingLocation =false;
        stopLocationUpdates();
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();
    }
    public boolean isLocationServiceEnabled(){
        LocationManager locationManager = null;
        boolean gps_enabled= false,network_enabled = false;

        if(locationManager ==null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }


    private void AddFinalMarker() {
        if(FinalMarker!=null){
            FinalMarker.remove();
            downloadTask.resetPolyline();
        }
        FinalMarker = mMap.addMarker(new MarkerOptions().position(mFinalLatLng));
        builder = new LatLngBounds.Builder();
        builder.include(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
        builder.include(mFinalLatLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),48));
        mRequestingLocation = true;
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mLastLocation==null){
            mLastLocation=location;
            setUpMap();
        }

       if(mFinalLocation!=null&&location.distanceTo(mFinalLocation)<=3000){
           stopLocationUpdates();
           PendingIntent resultPendingIntent = SendPendingIntentResult();
           NotificationCompat.Builder mBuilder = CreateNotification() ;
           mBuilder.setContentIntent(resultPendingIntent);
           // Sets an ID for the notification
           int mNotificationId = 001;
// Gets an instance of the NotificationManager service
           NotificationManager mNotifyMgr =
                   (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
           mNotifyMgr.notify(mNotificationId, mBuilder.build());
       }
        mCurrentLocation = location;
        //Log.d("XXXX","LocationChanged");
        //Toast.makeText(this, "Location Changed", Toast.LENGTH_SHORT).show();
        UpdateCurrentMarker(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()),1);
    }
    public void BitmapGenerator(){
        int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
        mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        Drawable shape = getResources().getDrawable(R.drawable.map_dot_red);
        shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        shape.draw(canvas);
    }

    // The callback for the management of the user settings regarding location
    private ResultCallback<LocationSettingsResult> mResultCallbackFromSettings = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            //final LocationSettingsStates locationSettingsStates = result.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                MainMapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e("XXXX", "Settings change unavailable. We have no way to fix the settings so we won't show the dialog.");
                    break;
            }
        }
    };
    /**
     * Used to check the result of the check of the user location settings
     *
     * @param requestCode code of the request made
     * @param resultCode code of the result of that request
     * @param intent intent with further information
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, intent);
                //Log.i("XXXX", "Place: " + place.getName());
                FinalPlace = place.getName().toString();
                mFinalLatLng = place.getLatLng();
                mFinalLocation = new Location("");
                mFinalLocation.setLatitude(mFinalLatLng.latitude);
                mFinalLocation.setLongitude(mFinalLatLng.longitude);

                AddFinalMarker();
                LatLng origin = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                String url = util.getDirectionsUrl(origin, mFinalLatLng);

                downloadTask = new DownloadTask();
                downloadTask.setMap(mMap);
                downloadTask.execute(url);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this,intent);
                // TODO: Handle the error.
                Log.i("XXXX", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(intent);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        if (mGoogleApiClient.isConnected() && initMarker == null) {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            if(mLastLocation!=null)
                                addInitalMarker();
                            startLocationUpdates();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }
    public  NotificationCompat.Builder CreateNotification(){
     NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon_xl)
                    .setContentTitle("Trip Complete")
                    .setContentText("Reached: "+FinalPlace);

    return mBuilder;}
    public PendingIntent SendPendingIntentResult(){
        Intent resultIntent = new Intent(this, MainMapActivity.class);

    // Because clicking the notification opens a new ("special") activity, there's
    // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

    return resultPendingIntent;
    }

}

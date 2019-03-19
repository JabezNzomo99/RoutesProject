package com.gmail.seinkenaiyan;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.gmail.seinkenaiyan.models.Stage;
import com.gmail.seinkenaiyan.models.StageLocation;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

//declaring objects used in this file
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrentLocationMarker;
    int PROXIMITY_RADIUS= 1500;
    public static final int REQUEST_LOCATION_CODE = 99;
    private OkHttpClient client;
    private HttpLoggingInterceptor httpLoggingInterceptor;
    private Button btnSearch;
    private Handler handler;
    private ProgressBar progressBar;


    @Override
    //this class describes what happens when the map fragment is first created and displayed
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpLoggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();
        handler = new Handler(Looper.getMainLooper());
        //set to display layout in the xml file-activity_maps which contains the map fragment
        setContentView(R.layout.activity_maps);
        //checking android version to avoid unsupported versions that may make the app crash
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnSearch = (Button)findViewById(R.id.B_search);
        progressBar = (ProgressBar)findViewById(R.id.progress_horizontal);
        btnSearch.setOnClickListener(v -> {
            EditText edtLocation = (EditText)findViewById(R.id.tf_location);
            if(!TextUtils.isEmpty(edtLocation.getText())){
                if(progressBar.getVisibility() == View.GONE){
                    progressBar.setVisibility(View.VISIBLE);
                }
                mMap.clear();
                getStages(edtLocation.getText().toString().trim());
                progressBar.setVisibility(View.GONE);

            }else {
                Toast.makeText(MapsActivity.this,"Enter Location",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Method for handling permission request results
    //override to check if permission is granted or not
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //permission is granted
                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (mGoogleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else// permission denied
                {
                    Toast.makeText(this, "Permission Denied!", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }
    private void getStages(String locationText){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = new ArrayList<Address>();
        try{
            addressList = geocoder.getFromLocationName(locationText,1);
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
        if(!addressList.isEmpty()) {
            for (Address address : addressList) {
                Log.d("Address", address.getAdminArea());
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                Request request = new Request.Builder()
                        .url(getUrl(latitude, longitude, "bus_station"))
                        .get()
                        .addHeader("cache-control", "no-cache")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Response", "Failed");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        if (response.isSuccessful()) {
                            ArrayList<Stage> stageArrayList = new ArrayList<>();
                            MarkerOptions markerOptions = new MarkerOptions();
                            String jsonData = response.body().string();
                            try {
                                JSONObject jsonObject = new JSONObject(jsonData);
                                JSONArray jsonArray = jsonObject.getJSONArray("results");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Stage stage = new Stage();
                                    stage.setStageName(object.getString("name"));
                                    stage.setStageLocation(new StageLocation(object.getJSONObject("geometry").getJSONObject("location").getDouble("lat"),
                                            object.getJSONObject("geometry").getJSONObject("location").getDouble("lng")));
                                    stage.setIconURL(object.getString("icon"));
                                    stage.setVicinity(object.getString("vicinity"));
                                    stageArrayList.add(stage);
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (stageArrayList.size() > 0) {
                                            for (Stage stage : stageArrayList) {
                                                LatLng latlng = new LatLng(stage.getStageLocation().getLatitude(), stage.getStageLocation().getLongitude());
                                                markerOptions.position(latlng);    //sets position
                                                markerOptions.title(stage.getStageName());
                                                mMap.addMarker(markerOptions);     //adds marker to the map
                                                //makes marker focus on the last position given
                                                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                                            }
                                        }

                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
            }
        }else{
            Toast.makeText(this,"Location not found", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * This function builds the url to be used to send the request to google places API
     * @param latitude
     * @param longitude
     * @param nearbyPlace
     * @return URL : String
     */
    private String getUrl(double latitude, double longitude, String nearbyPlace)
    {//string builder class creates modifiable string
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&key="+"AIzaSyBS8mzDVBrttyB_N_YvIQKMJo-kyOtkP1I");
        return googlePlaceUrl.toString();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize google play services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else{
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }
//Creating a mGoogleApiClient
    protected  synchronized  void buildGoogleApiClient()
    {
         mGoogleApiClient = new GoogleApiClient.Builder(this)
                 .addConnectionCallbacks(this)
                 .addOnConnectionFailedListener(this)
                 .addApi(LocationServices.API)
                 .build();
         mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        //removes any set location marker
        if (mCurrentLocationMarker != null)
        {
            mCurrentLocationMarker.remove();
        }
        //Obtaining current position
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Setting current position to the marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

        mCurrentLocationMarker = mMap.addMarker(markerOptions);

        //moving the camera to the new location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomBy(10));

        //Stopping location updates after setting current location..
        //if statement checks whether there is no location set currently
       if (mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //checks whether permission is granted
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //if permission was requested before and denied
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else
            return true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     *
     * @param context
     * @param vectorResId
     * @return
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}

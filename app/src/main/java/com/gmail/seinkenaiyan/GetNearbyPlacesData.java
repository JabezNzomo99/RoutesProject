package com.gmail.seinkenaiyan;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

   String googlePlaceData;
   GoogleMap mMap;
   String url;


    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlaceData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String, String>> nearbyPlaceList = null;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);

    }

    private void  showNearbyPlaces(List<HashMap<String,String>>nearbyPlaceList)
    {
        for (int i = 0; i<nearbyPlaceList.size();i++)
        {
            //Shows all the places in the list
            MarkerOptions markerOptions = new MarkerOptions();
            //fetching the i'th element and stores in google place
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            //googlePlace parses double values for coordinates
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));

            LatLng latLng = new LatLng(lat, lng);
            //set position and title for marker options
            markerOptions.position(latLng);
            markerOptions.title(placeName +":"+ vicinity);
            //adding the marker to the map
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

            mMap.addMarker(markerOptions);
            //moving camera to the location
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            //animating the camera
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
    }
}

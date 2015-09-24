package com.emamontov.stormy;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by MamontovEU on 21.07.2015.
 */
public class CurrentLocation {
    private double mLatitude = 55.8346;
    private double mLongitude = 37.3234;

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {

        return mLatitude;
    }

    public void setLatitude(double latitude) {

        mLatitude = latitude;
    }

//    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//    LocationListener ll = new mylocationlistener();
//    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
}

class mylocationlistener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.d("LOCATION CHANGED", location.getLatitude() + "");
            Log.d("LOCATION CHANGED", location.getLongitude() + "");
 //           Toast.makeText(MainActivity.this,
 //                   location.getLatitude() + "" + location.getLongitude(),
 //                   Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}

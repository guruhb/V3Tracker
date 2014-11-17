package com.vts.v3tracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vts.vtsUtils.LiveVehicleData;
import com.vts.vtsUtils.VehicleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by guruhb@gmail.com on 16-11-2014.
 */
public class MapViewHelper implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener{

    private static final String TAG = "MapViewHelper";
    LocationClient mLocationClient;
    Location mCurrentLocation;
    LocationRequest mLocationRequest;
    LatLng mDeviceLocation;
    LatLng mVehicleLocation;
    Context mContext;
    ArrayList<String> mSelectedVehicle = new ArrayList<String>();
    private GoogleMap mMap;
    //private Marker mVehicleMarker;
    private Marker mCurrentLocationMarker;

    private HashMap<String, MarkerOptions> mVehicleMarkers;
    private boolean updateMarker;
    private boolean mUserZoomed;

    MapViewHelper(Context context, GoogleMap map) {
        mContext = context;
        mMap = map;
        mUserZoomed = false;
        mLocationClient = new LocationClient(mContext, this, this);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mLocationRequest.setInterval(1000 * 10);

        mLocationRequest.setFastestInterval(5000 * 1);
        mDeviceLocation = new LatLng(13.064838, 77.583793);
        mVehicleMarkers = new HashMap<String, MarkerOptions>();
        getMyPreferedVehicles(false);

    }

    private void createMarkersForVehicles() {
        for (String v : mSelectedVehicle) {
            //list of vehicles string
        }
    }

    public void onStart() {
        mLocationClient.connect();
    }

    public void onResume() {
        //place all vehicle marker
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.v(TAG, "onCameraChange");
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.v(TAG, "onMarkerClick ");
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.v(TAG, "onMapClick  ");
            }
        });

    }

    public void onPause() {
        //remove or hide vehicle marker
    }

    public void onStop() {
        mLocationClient.disconnect();
    }

    public void updateVehiclesOnMap(boolean bound) {
        for(String vehicleName : mSelectedVehicle) {
            if(!(mVehicleMarkers.containsKey(vehicleName))) {
                //get vehicle location and create marker
                VehicleData vd = LiveVehicleData.getInstance().getVehicleDataFor(vehicleName);
                if(vd != null && vd.positionData.length > 0) {
                    MarkerOptions mo = new MarkerOptions()
                            .position(new LatLng(vd.positionData[0].lat, vd.positionData[0].lng))
                            .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.schoolbus, vehicleName)));
                    mMap.addMarker(mo);
                    mVehicleMarkers.put(vehicleName, mo);
                }
            } else {
                //update only marker position
                MarkerOptions mo = mVehicleMarkers.get(vehicleName);
                VehicleData vd = LiveVehicleData.getInstance().getVehicleDataFor(vehicleName);
                if(vd != null && vd.positionData.length > 0) {
                    mo.position(new LatLng(vd.positionData[0].lat, vd.positionData[0].lng));
                }
            }
        }
    }

    public void getMyPreferedVehicles(boolean updateMarker) {
        String[] myVehicles;
        SharedPreferences myPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file), Context.MODE_PRIVATE);
        String s = myPref.getString(mContext.getString(R.string.v3VehiclePreference), "");
        myVehicles = s.split(",");
        mSelectedVehicle = new ArrayList<String>(Arrays.asList(myVehicles));
        if(updateMarker == true) {
            mVehicleMarkers.clear();
            mMap.clear();
            mCurrentLocationMarker = null; //force to re-create
            updateDevicePositionOnMap();
            updateVehiclesOnMap(true);
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        if(mLocationClient != null)
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();

        if(mLocationClient != null) {
            mCurrentLocation = mLocationClient.getLastLocation();
            try {
                mDeviceLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                updateDevicePositionOnMap();
            } catch (NullPointerException e) {
                Toast.makeText(mContext, "Failed to Connect Location service", Toast.LENGTH_SHORT).show();
                // switch on location service intent
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(mContext, "Disconnected.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(mContext, "Connection Failed ... ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mLocationClient.isConnected()) {
            mCurrentLocation = mLocationClient.getLastLocation();
            mDeviceLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            Toast.makeText(mContext, "Location changed. " + mDeviceLocation.toString(), Toast.LENGTH_SHORT).show();
            updateDevicePositionOnMap();
        }
    }


    private void updateDevicePositionOnMap() {
        if(mMap == null) return;

        if(mCurrentLocationMarker == null) {
            mCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(mDeviceLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker()));
        } else {
            mCurrentLocationMarker.setPosition(mDeviceLocation);
        }
        boundVehicleAndDevicePositionOnMap();
    }
    public void updateVehiclePositionOnMap(VehicleData vd) {
        if(mMap == null) return;

        if(vd.positionData.length < 1) {
            Log.v(TAG, "updateVehiclePositionOnMap no position data for this device ");
            return; //FIXME : no position data !
        }

        if(vd.positionData[0].lat == 0 || vd.positionData[0].lng == 0) {
            mVehicleLocation = new LatLng(vd.positionData[1].lat, vd.positionData[1].lng);//FIXME : not tested
        }else {
            mVehicleLocation = new LatLng(vd.positionData[0].lat, vd.positionData[0].lng);
        }
    }

    private void boundVehicleAndDevicePositionOnMap() {
        if(mUserZoomed == false) {
            try {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                if(mDeviceLocation != null)
                    builder.include(mDeviceLocation);


                for(String vehicle : mVehicleMarkers.keySet()) {
                    builder.include(mVehicleMarkers.get(vehicle).getPosition());
                }
                LatLngBounds bounds = builder.build();

                int padding = 100; // offset from edges of the map in pixels FIXME : make this literal instead of hard coded value
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);
                Log.v(TAG, "boundVehicleAndDevicePositionOnMap moveCamera");

            } catch (IllegalStateException e) {
                Log.v("MAPS", "Move camera excpetion");
            }

        }
    }
    private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(mContext.getApplicationContext(), 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(mContext.getApplicationContext(), 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }
    public static float convertToPixels(Context context, int dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }


}

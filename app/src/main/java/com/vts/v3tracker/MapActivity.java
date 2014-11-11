package com.vts.v3tracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import android.provider.Settings;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vts.vtsUtils.Http;
import com.vts.vtsUtils.LiveVehicleData;
import com.vts.vtsUtils.RouteJSONParser;
import com.vts.vtsUtils.Vehicle;
import com.vts.vtsUtils.VehicleData;
import com.vts.vtsUtils.VehicleListAdaptor;
import com.vts.vtsUtils.VtsIntentService;
import com.vts.vtsUtils.VtsService;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.vts.vtsUtils.LocationUpdate;


/**
 * Created by ghb on 21-10-2014.
 */
public class MapActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "MapActivity";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    private String allVehicleData = "";
    private JSONObject allVehicleDataJsonData;

    private static final String TAG_OWNER = "owner";
    private static final String TAG_DEVICE_ID = "imei";
    private static final String TAG_NUMBER = "deviceName";

    ListView customerListView;
    ArrayList<Vehicle> arrayList = new ArrayList<Vehicle>();
    VehicleListAdaptor adaptor;
    private boolean loadingMore = false;
    Handler handler = new Handler();
    private Handler mPeriodicUpdateHandler;

    LocationClient mLocationClient;
    Location mCurrentLocation;
    LocationRequest mLocationRequest;
    LatLng mDeviceLocation;
    LatLng mVehicleLocation;
    private Marker mVehicleMarker;
    private Marker mCurrentLocationMarker;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //set up periodic handler
        mPeriodicUpdateHandler = new Handler();

        mLocationClient = new LocationClient(this, this, this);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mLocationRequest.setInterval(1000 * 10);

        mLocationRequest.setFastestInterval(5000 * 1);
        mDeviceLocation = new LatLng(13.064838, 77.583793);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if(mMap != null) {
            Log.v("MapActivity", "Map Fragment failed to inflate");
        }

        /*Button button = new Button(this);
        button.setText("Click me");
        addContentView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));*/

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(intentReceiver, new IntentFilter(VtsIntentService.VTSINTENTSERVICERESPONSE));
        startPeriodicTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPeriodicTimer();
        unregisterReceiver(intentReceiver);
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.v("MainActivity", "onNavigationDrawerItemSelected position : " + position);


        switch(position) {
            case 0 : {
                // update the main content by replacing fragments
               /* FragmentManager fragmentManager = getFragmentManager();
                //FIXME : different actions on map activity
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();*/

            }
            break;
            case 1 : { //calculate route and plot route map
                //SERVICE_CALCULATE_ROUTE
                if(!(mCurrentLocation != null && mVehicleLocation != null )) {
                    Toast.makeText(this, "Current and Vehicle location unknown" + mDeviceLocation.toString() , Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(MapActivity.this, VtsIntentService.class);
                i.putExtra(VtsIntentService.SERVICE_TYPE, VtsIntentService.ServiceType.SERVICE_CALCULATE_ROUTE);
                Bundle bundle = new Bundle();

                bundle.putDoubleArray("src", new double[] {mDeviceLocation.latitude, mDeviceLocation.longitude});
                bundle.putDoubleArray("dst", new double[] {mVehicleLocation.latitude, mVehicleLocation.longitude});
                i.putExtra("location", bundle);
                // add info for the service which file to download and where to store
                startService(i);
            }
            break;
            default : {
                // update the main content by replacing fragments
               /* FragmentManager fragmentManager = getFragmentManager();
                //FIXME : different actions on map activity
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();*/
            }
        }
    }


    public void onSectionAttached(int number) {
        Log.v("MainActivity", "onSectionAttached number : " + number );
        switch (number) {
            case 1:
                mTitle = getString(R.string.nav_pref_location_update_frequency);
                break;
            case 2:
                mTitle = getString(R.string.nav_pref_select_vehicle);
                break;
            case 3:
                mTitle = getString(R.string.nav_pref_mode);
                break;
        }
    }

    public void restoreActionBar() {
        Log.v("MainActivity", "restoreActionBar ");
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v("MainActivity", "onCreateOptionsMenu menu : ");
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            Log.v("MainActivity", "isDrawerOpen not open");
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.my, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v("MainActivity", "onOptionsItemSelected itemId : " + item.getItemId());
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings : {
                return true;
            }
            case R.id.setting_updatefrequency : {
                //showUpdateLocationPopUp();
                SettingsUpdateFrequency suf = new SettingsUpdateFrequency(MapActivity.this);
                suf.launchSettingsUpdateFrequency();
                return true;
            }
            case R.id.setting_vehicle : {
                FragmentManager mgr = getFragmentManager();
                VehicleListSelectFragment dlg = new VehicleListSelectFragment();
                dlg.show(mgr, "VehicleSelectListFragment");
                return true;
            }
            case R.id.setting_mode : {
                FragmentManager mgr = getFragmentManager();
                ModeSelectFragment dlg = new ModeSelectFragment();
                dlg.show(mgr, "ModeSelectFragment");
                return true;
            }
            default :  {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        if(mLocationClient != null)
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        if(mLocationClient != null) {
            mCurrentLocation = mLocationClient.getLastLocation();
            try {
                mDeviceLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                updateDevicePositionOnMap();
            } catch (NullPointerException e) {
                Toast.makeText(this, "Failed to Connect Location service", Toast.LENGTH_SHORT).show();
                // switch on location service intent
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed ... ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mLocationClient.isConnected()) {
            mCurrentLocation = mLocationClient.getLastLocation();
            mDeviceLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            Toast.makeText(this, "Location changed. " + mDeviceLocation.toString(), Toast.LENGTH_SHORT).show();
            updateDevicePositionOnMap();
        }
    }

    /**
     * A placeholder fragment for replacing container with other view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            Log.v("MainActivity", "newInstance sectionNumber : " + sectionNumber);
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
            Log.v("MainActivity", "PlaceholderFragment cTor ");
        }
        private static View fView;
        private static MapFragment mMapFragment;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.v("MainActivity", "PlaceholderFragment onCreateView ");

            if(fView != null) {
                ViewGroup parent = (ViewGroup) fView.getParent();
                if(parent != null)
                    parent.removeView(fView);
            }
            try {
                fView = inflater.inflate(R.layout.fragment_my, container, false);
            } catch (InflateException e) {
                Log.v("MapActivity", "fragment exception " );
                e.printStackTrace();
            }


            return fView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            Log.v("MainActivity", "PlaceholderFragment onAttach ");
            ((MapActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private int mLocationUpdateFreq = 30;

    public void showUpdateLocationPopUp() {

        Dialog localFreqDialog = new Dialog(this);
        localFreqDialog.setContentView(R.layout.updateseekbar);
        localFreqDialog.setTitle("Location update ... ");
        SeekBar mySeek = (SeekBar)localFreqDialog.findViewById(R.id.location_update_frequency);
        if(mySeek != null) {
            //set seekbar observer
            Log.v("MapActivity", "Set seek bar observer");
            mySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mLocationUpdateFreq = progress;
                    Log.v("MapActivity", "Seek bar onProgressChanged");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Log.v("MapActivity", "Seek bar onStartTrackingTouch");
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.v("MapActivity", "Seek bar onStopTrackingTouch");
                }
            });

            /*
            onCancel is called first and then onDismiss
             */
            localFreqDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.v("MapActivity", "Location update dialog cancelled");
                }
            });

            localFreqDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.v("MapActivity", "Location OnDismissListener");
                }
            });

        }

        localFreqDialog.show();
    }

    /** Messenger for communicating with the service. */
    private VtsService mHttpService = null;
    private boolean mHttpBound = false;

    @Override
    protected void onStart() {
        super.onStart();
        //Bind to VtsService
        Intent intent = new Intent(this, VtsService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mHttpBound) {
            unbindService(mConnection);
        }
        mLocationClient.disconnect();
    }


    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mHttpService = null;
            mHttpBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            VtsService.HttpBinder httpBinder = (VtsService.HttpBinder) service;
            mHttpService = httpBinder.getService();
            mHttpBound = true;
        }
    };

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("MapActivity", "VtsIntentService BroadcastReceiver Service onReceive");
            Bundle bundle = intent.getExtras();
            if(bundle != null) {
                VtsIntentService.ServiceType st = (VtsIntentService.ServiceType) bundle.getSerializable(VtsIntentService.SERVICE_TYPE);
                if(st == null) return;
                switch(st) {
                    case SERVICE_LOGIN :
                    {
                        String status = bundle.getBoolean(VtsIntentService.SERVICE_RESPONSE) ? "true" : "false";
                        Log.v("MapActivity", "Http service login status : " + status);

                        Toast.makeText(MapActivity.this,
                                "User Login status :  " + status,
                                Toast.LENGTH_LONG).show();


                        if(bundle.getBoolean(VtsIntentService.SERVICE_RESPONSE)) {
                            Intent i = new Intent(MapActivity.this, VtsIntentService.class);
                            i.putExtra(VtsIntentService.SERVICE_TYPE, VtsIntentService.ServiceType.SERVICE_GETLIVEDATA);
                            // add infos for the service which file to download and where to store
                            startService(i);
                            Log.v("MapActivity", "Http get live data Request started");
                        }
                    }
                    break;
                    case SERVICE_GETLIVEDATA :
                    {
                        String status = bundle.getBoolean(VtsIntentService.SERVICE_RESPONSE) ? "true" : "false";
                        if(status == "true") {
                            SharedPreferences myPref = getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
                            String myVehicle = myPref.getString(getString(R.string.key_my_vehicle), "");
                            VehicleData vd = LiveVehicleData.getInstance().getVehicleDataFor(myVehicle);
                            if(vd != null) {
                                Toast.makeText(MapActivity.this,
                                        "get Live data :  " + vd.deviceName,
                                        Toast.LENGTH_LONG).show();

                                updateVehiclePositionOnMap(vd);

                            }
                        }
                        Log.v("MapActivity", "Http service getlivedata status : " + status);
                    }
                    break;
                    case SERVICE_CALCULATE_ROUTE : {
                        String status = bundle.getBoolean(VtsIntentService.SERVICE_RESPONSE) ? "true" : "false";
                        if(status == "true") {
                            //render route on the map
                            ArrayList<LatLng> points = null;
                            PolylineOptions lineOptions = null;
                            MarkerOptions markerOptions = new MarkerOptions();
                            String distance = "";
                            String duration = "";

                            List<List<HashMap<String, String>>> result = RouteJSONParser.getInstance().getRoutes();
                            if (result.size() < 1)
                            {
                                Toast.makeText(MapActivity.this.getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Traversing through all the routes
                            for (int i = 0; i < result.size(); i++)
                            {
                                points = new ArrayList<LatLng>();
                                lineOptions = new PolylineOptions();

                                // Fetching i-th route
                                List<HashMap<String, String>> path = result.get(i);

                                // Fetching all the points in i-th route
                                for (int j = 0; j < path.size(); j++)
                                {
                                    HashMap<String, String> point = path.get(j);

                                    if (j == 0)
                                    { // Get distance from the list
                                        distance = point.get("distance");
                                        continue;
                                    } else if (j == 1)
                                    { // Get duration from the list
                                        duration = point.get("duration");
                                        continue;
                                    }
                                    double lat = Double.parseDouble(point.get("lat"));
                                    double lng = Double.parseDouble(point.get("lng"));
                                    LatLng position = new LatLng(lat, lng);
                                    points.add(position);
                                }

                                // Adding all the points in the route to LineOptions
                                lineOptions.addAll(points);
                                lineOptions.width(2);
                                lineOptions.color(Color.RED);
                            }
                            mMap.addPolyline(lineOptions);
                        }

                    }
                     break;
                    default : {
                        Log.v("MapActivity", "Unknown Service responded ");

                        Toast.makeText(MapActivity.this,
                                "Unknown vtsIntentService resonded !!!",
                                Toast.LENGTH_LONG).show();
                    }
                }


            }

        }
    };
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
    private void updateVehiclePositionOnMap(VehicleData vd) {
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

        if(mVehicleMarker == null) {
            mVehicleMarker = mMap.addMarker(new MarkerOptions()
                    .position(mVehicleLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.schoolbus, "Vehicle number, time, etc"))));
        }
        else {
            mVehicleMarker.setPosition(mVehicleLocation);
        }
        boundVehicleAndDevicePositionOnMap();

    }

    private void boundVehicleAndDevicePositionOnMap() {
        if(mDeviceLocation != null && mVehicleLocation != null) {
            try {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                builder.include(mDeviceLocation);
                builder.include(mVehicleLocation);
                LatLngBounds bounds = builder.build();

                int padding = 100; // offset from edges of the map in pixels FIXME : make this literal instead of hard coded value
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.moveCamera(cu);

            } catch (IllegalStateException e) {
                Log.v("MAPS", "Move camera excpetion");
            }
        }

    }
    private void getLivedataAndGPS() {
        Log.i("MapActivity", "getLiveDataAndGPS -->");

        Intent i = new Intent(MapActivity.this, VtsIntentService.class);
        i.putExtra(VtsIntentService.SERVICE_TYPE, VtsIntentService.ServiceType.SERVICE_GETLIVEDATA);
        // add infos for the service which file to download and where to store
        startService(i);
    }
    private void startPeriodicTimer() {
        Log.i("MapActivity", "startPeriodicTimer()");
        mPeriodicStatus.run();
    }
    private void stopPeriodicTimer() {
        Log.i("MapActivity", "stopPeriodicTimer()");
        mPeriodicUpdateHandler.removeCallbacks(mPeriodicStatus);
    }

    private int minLocationUpdateTime = 30000; //in Milli seconds
    private Runnable mPeriodicStatus = new Runnable() {
        @Override
        public void run() {
            Log.v("MapActivity", "PeriodicRunnable Run()");
            getLivedataAndGPS();
            SharedPreferences myPref = getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
            int locupdatetime = myPref.getInt(getString(R.string.key_location_update), minLocationUpdateTime) + minLocationUpdateTime;
            if(locupdatetime < minLocationUpdateTime)
                locupdatetime = minLocationUpdateTime;
            mPeriodicUpdateHandler.postDelayed(mPeriodicStatus, locupdatetime);
        }
    };
    public static float convertToPixels(Context context, int dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(convertToPixels(getApplicationContext(), 11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(convertToPixels(getApplicationContext(), 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }

}

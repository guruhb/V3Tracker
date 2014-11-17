package com.vts.v3tracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.vts.vtsUtils.RouteJSONParser;
import com.vts.vtsUtils.VehicleListAdaptor;
import com.vts.vtsUtils.VtsIntentService;
import com.vts.vtsUtils.VtsService;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by ghb on 21-10-2014.
 */
public class MapActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        VehicleListSelectFragment.PreferenceUpdate

         {

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

    VehicleListAdaptor adaptor;
    private boolean loadingMore = false;
    Handler handler = new Handler();
    private Handler mPeriodicUpdateHandler;

    private GoogleMap mMap;
    private MapViewHelper mMapViewHelper;


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

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if(mMap == null) {
            Log.v("MapActivity", "Map Fragment failed to inflate");
        }
        mMapViewHelper = new MapViewHelper(this, mMap);

        Button button = new Button(this);
        button.setText("Click me");
        addContentView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));*/

    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(intentReceiver, new IntentFilter(VtsIntentService.VTS_INTENT_SERVICE_RESPONSE));
        startPeriodicTimer();
        mMapViewHelper.onResume();
   }

    @Override
    protected void onPause() {
        super.onPause();
        stopPeriodicTimer();
        unregisterReceiver(intentReceiver);
        mMapViewHelper.onPause();
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
               /*
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
                startService(i);*/
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
                dlg.setPreferenceChange(this);
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
    public void onPreferenceChanged() {
         Log.v(TAG, "onPreferenceChanged()");
         mMapViewHelper.getMyPreferedVehicles(true);
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
        mMapViewHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mHttpBound) {
            unbindService(mConnection);
        }
        mMapViewHelper.onStop();
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
                            i.putExtra(VtsIntentService.SERVICE_TYPE, VtsIntentService.ServiceType.SERVICE_GET_LIVE_DATA);
                            // add infos for the service which file to download and where to store
                            startService(i);
                            Log.v("MapActivity", "Http get live data Request started");
                        }
                    }
                    break;
                    case SERVICE_GET_LIVE_DATA :
                    {
                        String status = bundle.getBoolean(VtsIntentService.SERVICE_RESPONSE) ? "true" : "false";
                        if(status == "true") {
                            mMapViewHelper.updateVehiclesOnMap(false);
                           /* SharedPreferences myPref = getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
                            String myVehicle = myPref.getString(getString(R.string.key_my_vehicle), "");
                            VehicleData vd = LiveVehicleData.getInstance().getVehicleDataFor(myVehicle);
                            if(vd != null) {
                                Toast.makeText(MapActivity.this,
                                        "get Live data :  " + vd.deviceName,
                                        Toast.LENGTH_LONG).show();

                                mMapViewHelper.updateVehiclePositionOnMap(vd);

                            }*/
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

    private void getLivedataAndGPS() {
        Log.i("MapActivity", "getLiveDataAndGPS -->");

        Intent i = new Intent(MapActivity.this, VtsIntentService.class);
        i.putExtra(VtsIntentService.SERVICE_TYPE, VtsIntentService.ServiceType.SERVICE_GET_LIVE_DATA);
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

}

package com.vts.vtsUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by guruhb@gmail.com on 22-10-2014.
 * 1. Get location from GPS
 * 2. Get live data from server
 * 3. parser the live data from server and udpate vtsdata
 */
public class LocationUpdate extends Service {
    private int mUpdateTime;
    private Timer mPeriodicTimer;
    private Context mContext;

    public LocationUpdate(int time, Context ctx) {
        //cTor
        mUpdateTime = time;
        mContext = ctx;
    }

    public void startLocationUpdateTimer() {
       /* if(mPeriodicTimer == null) {
            mPeriodicTimer = new Timer();
            mPeriodicTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mContext.runOnUiThread( new Runnable() {
                        public void run () {
                            getGPSLocation();
                        }
                    });

                }
            });
        }*/
    }
    private void getGPSLocation() {

    }
    public void stopLocationUpdateTimer() {

    }




    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private LocationUpdateCallback mCallbacks;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static interface LocationUpdateCallback {
        /**
         * Called when an new data is fetched from network.
         */
        void onLocationUpdate(boolean status);
    }
}

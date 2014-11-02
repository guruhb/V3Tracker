package com.vts.v3tracker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.SeekBar;


/**
 * Created by guruhb@gmail.com on 19-10-2014.
 */
public class SettingsUpdateFrequency {
    final private int mSeekBarOffset = 30;
    private Context mContext;
    private int mLocationUpdateFreq = mSeekBarOffset;

    SettingsUpdateFrequency(Context ctx) {
        mContext = ctx;
    }

    void launchSettingsUpdateFrequency() {
        final Dialog localFreqDialog = new Dialog(mContext); //FIXME do i need to make this final ?
        //localFreqDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        localFreqDialog.setContentView(R.layout.updateseekbar);
        mLocationUpdateFreq = getUpdateFrequency();
        updateTitle(localFreqDialog, mLocationUpdateFreq);

        final SeekBar mySeek = (SeekBar)localFreqDialog.findViewById(R.id.location_update_frequency);
        mySeek.setProgress(mLocationUpdateFreq - mSeekBarOffset);
        if(mySeek != null) {
            //set seekbar observer
            Log.v("MyActivity", "Set seek bar observer");
            mySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mLocationUpdateFreq = progress;
                    updateTitle(localFreqDialog, mLocationUpdateFreq);
                    Log.v("MyActivity", "Seek bar onProgressChanged");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    Log.v("MyActivity", "Seek bar onStartTrackingTouch");
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.v("MyActivity", "Seek bar onStopTrackingTouch");
                }
            });

            /*
            onCancel is called first and then onDismiss
             */
            localFreqDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.v("MyActivity", "Location update dialog cancelled mLocationUpdateFreq : " + mLocationUpdateFreq);
                    if(setUpdateFrequency(mLocationUpdateFreq)) {
                        Log.v("MyActivity", "Location update dialog commit failed" + mLocationUpdateFreq);
                    }
                }
            });

            localFreqDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.v("MyActivity", "Location OnDismissListener mLocationUpdateFreq : " + mLocationUpdateFreq);
                }
            });

        }
        localFreqDialog.show();
    }

    private void updateTitle(Dialog dlg, int freq) {
        dlg.setTitle(mContext.getString(R.string.nav_pref_location_update_frequency_in_sec) +  String.valueOf(freq + mSeekBarOffset) );
    }
    private int getUpdateFrequency() {
        SharedPreferences myPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file), Context.MODE_PRIVATE);
        return (myPref.getInt(mContext.getString(R.string.key_location_update), mSeekBarOffset) + mSeekBarOffset);
    }

    private boolean setUpdateFrequency(int frequency) {
        SharedPreferences myPref = mContext.getSharedPreferences(mContext.getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor myPrefEdit = myPref.edit();
        //remove the offset before storing
        myPrefEdit.putInt(mContext.getString(R.string.key_location_update), (frequency - mSeekBarOffset));
        if(!myPrefEdit.commit()) {
            Log.v("MyActivity", "setUpdateFrequency failed" + mLocationUpdateFreq);
            return false;
        }
        return true;
    }
}

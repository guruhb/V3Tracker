package com.vts.v3tracker;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.vts.vtsUtils.LiveVehicleData;
import com.vts.vtsUtils.VehicleData;

/**
 * Created by guruhb@gmail.com on 21-10-2014.
 * 1. List box with icon
 * 2. List box with current selected vehicle focused
 * 3. info message on vehicle selected
 */
public class VehicleListSelectFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private static final String TAG="VehicleListSelectFragment";
    private String[] mVehicleListItems;

    ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View v = View.inflate(R.layout.)
        View view = inflater.inflate(R.layout.vehicleselectfragment, null, false);
        mListView = (ListView) view.findViewById(R.id.vehicle_select_list);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        if(mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.v(TAG, "onItemClick position : " + position + " id : " + id);
                }
            });
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int vehicles = LiveVehicleData.getInstance().getTotalVehicleCount();

        mVehicleListItems = new String[vehicles];

        int index = 0;
        for(String customer: LiveVehicleData.getInstance().getLiveData().keySet()) {
            //Log.v("HM", "key : " + customer + " " + liveVehicleData.get(customer));
            for(String vehicalNum : LiveVehicleData.getInstance().getLiveData().get(customer).keySet()) {
                mVehicleListItems[index++] = vehicalNum;
                /*VehicleData vd = LiveVehicleData.getInstance().getLiveData().get(customer).get(vehicalNum);
                Log.v("LiveVehicleData", " Customer : " + customer + " vehicle : " + vehicalNum + " Id: " + vd.deviceId + " type : " + vd.type + " Lat/lng : "
                        + vd.positionData[0].lat + "\t" + vd.positionData[0].lng);*/
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, mVehicleListItems);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "onItemClick() position : " + mVehicleListItems[position]);
        setSelectedVehicle(mVehicleListItems[position]);
        dismiss();
    }


    private boolean setSelectedVehicle(String vehicalNum) {
        SharedPreferences myPref = getActivity().getSharedPreferences(getActivity().getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor myPrefEdit = myPref.edit();
        //remove the offset before storing
        myPrefEdit.putString(getActivity().getString(R.string.key_my_vehicle), vehicalNum);
        if(!myPrefEdit.commit()) {
            Log.v(TAG, "setSelectedVehicle failed" + vehicalNum);
            return false;
        }
        return true;
    }
}

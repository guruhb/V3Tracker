package com.vts.v3tracker;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.vts.vtsUtils.LiveVehicleData;
import com.vts.vtsUtils.VehicleData;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by guruhb@gmail.com on 21-10-2014.
 * 1. List box with icon
 * 2. List box with current selected vehicle focused
 * 3. info message on vehicle selected
 */
public class VehicleListSelectFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private static final String TAG="VehicleListSelectFragment";
    private String[] mVehicleListItems;
    ListAdapter mVehicleListAdapter;
    ListView mListView;
    List<VehicleData> mList = new ArrayList<VehicleData>();

    private PreferenceUpdate mPreferenceUpdate;
    public interface PreferenceUpdate {
        public void onPreferenceChanged();
    }

    public void setPreferenceChange(PreferenceUpdate preferenceChange) {
        mPreferenceUpdate = preferenceChange;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View v = View.inflate(R.layout.)
        View view = inflater.inflate(R.layout.vehicleselectfragment, null, false);
        mListView = (ListView) view.findViewById(R.id.vehicle_select_list);
        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        Button mPositiveButton;
        Button mNegativeButton;
        mPositiveButton = (Button)view.findViewById(R.id.vehicle_select_list_ok);
        if(mPositiveButton != null) {
            mPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "List selection done ");

                    StringBuilder sv = new StringBuilder();
                    for(int i = 0; i < mList.size(); i++ ) {
                        VehicleData vd = mList.get(i);
                        if(vd.selected == true) {
                            sv.append(vd.deviceName).append(",");
                        }
                    }
                    setSelectedVehicle(sv);
                    if(mPreferenceUpdate != null) {
                        mPreferenceUpdate.onPreferenceChanged();
                    }

                    dismiss();
                    //store the sv to preference;
                    Log.v(TAG, "List selection done item: " + mListView.getCheckedItemPosition());
                }
            });
        }
        getDialog().setTitle("Select vehicles");

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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mPreferenceUpdate = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int vehicles = LiveVehicleData.getInstance().getTotalVehicleCount();

        mVehicleListItems = new String[vehicles];
        ArrayList<String> al = getSelectedVehicle();

        int index = 0;
        for(String customer: LiveVehicleData.getInstance().getLiveData().keySet()) {
            //Log.v("HM", "key : " + customer + " " + liveVehicleData.get(customer));
            for(String vehicalNum : LiveVehicleData.getInstance().getLiveData().get(customer).keySet()) {
                mVehicleListItems[index++] = vehicalNum;
                VehicleData vd = LiveVehicleData.getInstance().getLiveData().get(customer).get(vehicalNum);
                if(al.contains(vd.deviceName)) {
                    vd.selected = true;
                }
                mList.add(vd);
            }
        }

        mVehicleListAdapter = new VehicleListAdapter(getActivity(), mList);
        mListView.setAdapter(mVehicleListAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v(TAG, "onItemClick() position : " + mVehicleListItems[position]);

        dismiss();
    }

    private ArrayList<String> getSelectedVehicle() {
        String[] myVehicles;
        SharedPreferences myPref = getActivity().getSharedPreferences(getActivity().getString(R.string.preference_file), Context.MODE_PRIVATE);
        String s = myPref.getString(getActivity().getString(R.string.v3VehiclePreference), "");
        myVehicles = s.split(",");
        ArrayList<String> al = new ArrayList<String>(Arrays.asList(myVehicles));
        return al;
    }

    private boolean setSelectedVehicle(StringBuilder sv) {
        Log.v(TAG, "setSelectedVehicle" + sv.toString());
        SharedPreferences myPref = getActivity().getSharedPreferences(getActivity().getString(R.string.preference_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor myPrefEdit = myPref.edit();
        //remove the offset before storing
        myPrefEdit.putString(getActivity().getString(R.string.v3VehiclePreference), sv.toString());
        if(!myPrefEdit.commit()) {
            Log.v(TAG, "setSelectedVehicle failed" + sv.toString());
            return false;
        }
        return true;
    }


}

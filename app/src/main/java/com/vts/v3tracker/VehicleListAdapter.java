package com.vts.v3tracker;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.vts.v3tracker.util.VehicleListModel;
import com.vts.vtsUtils.LiveVehicleData;
import com.vts.vtsUtils.Vehicle;
import com.vts.vtsUtils.VehicleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by guruhb@gmail.com on 16-11-2014.
 */
public class VehicleListAdapter extends ArrayAdapter<VehicleData> {
    private static final String TAG = "VehicleListAdapter";
    private final Context mContext;
    private SparseBooleanArray mCheckStates;
    private ArrayList<VehicleData> mVehicles;


    private List<VehicleData> mList;

    public VehicleListAdapter(Context context, List<VehicleData> list) {
        super(context, R.layout.vehiclelist, list);
        this.mContext = context;
        mList = list;
    }
    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.vehiclelist, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.vehicle_list_text);
            viewHolder.checkbox = (CheckBox) view.findViewById(R.id.vehicle_list_checkbox);
            viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    VehicleData vd = (VehicleData) viewHolder.checkbox.getTag();
                    vd.selected = buttonView.isChecked();
                }
            });
            view.setTag(viewHolder);
            viewHolder.checkbox.setTag(mList.get(position));
        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).checkbox.setTag(mList.get(position));
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(mList.get(position).deviceName);
        holder.checkbox.setChecked(mList.get(position).selected);
        return view;
    }
}

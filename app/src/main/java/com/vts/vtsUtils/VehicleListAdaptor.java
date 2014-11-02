package com.vts.vtsUtils;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vts.v3tracker.R;

public class VehicleListAdaptor extends ArrayAdapter<Vehicle> {

    Context context;
    int layoutResourceId;
    static String datesort;

    Vehicle currentMRB;
    ArrayList<Vehicle> data;

    /**
     * Called when the activity is first created.
     */
    // TODO Auto-generated constructor stub
    public VehicleListAdaptor(Context context, int layoutResourceId, ArrayList<Vehicle> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MyStringReaderHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new MyStringReaderHolder();
            holder.owner = (TextView) row.findViewById(R.id.owner);
            holder.number = (TextView) row.findViewById(R.id.number);

            holder.vehicleIcon = (ImageView) row.findViewById(R.id.vehicleIcon);


            row.setTag(holder);
        } else {
            holder = (MyStringReaderHolder) row.getTag();
        }

        Vehicle mrb = data.get(position);
        System.out.println("Position=" + position);

        holder.owner.setText(mrb.getOwner());

        holder.number.setText(mrb.getNumber());
        holder.vehicleIcon.setImageResource(R.drawable.ic_launcher);
        return row;
    }

    static class MyStringReaderHolder {
        TextView owner, number, vehicleId;
        ImageView vehicleIcon;
    }
}
package com.vts.v3tracker.util;

/**
 * Created by guruhb@gmail.com on 16-11-2014.
 */
public class VehicleListModel {
    private String mName;
    private boolean mSelected;

    public VehicleListModel(String name) {
        mName = name;
        mSelected = false;
    }
    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }
    public boolean ismSelected() {
        return mSelected;
    }
    public void setSelected(boolean selected) {
        mSelected = selected;
    }
}

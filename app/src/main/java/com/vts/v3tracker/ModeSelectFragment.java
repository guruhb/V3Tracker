package com.vts.v3tracker;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by guruhb@gmail.com on 21-10-2014.
 */
public class ModeSelectFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private String[] mModeSelect = {"School Bus", "Car", "Truck", "Tipper"};

    ListView mListView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View v = View.inflate(R.layout.)
        View view = inflater.inflate(R.layout.modeselectfragment, null, false);
        mListView = (ListView) view.findViewById(R.id.mode_select_list);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mModeSelect);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.v("ModeSelectFragment", "onItemClick() position : " + position);
        dismiss();
    }
}

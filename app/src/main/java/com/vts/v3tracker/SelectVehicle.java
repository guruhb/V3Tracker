package com.vts.v3tracker;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vts.vtsUtils.Http;
import com.vts.vtsUtils.LiveVehicleData;
import com.vts.vtsUtils.Vehicle;
import com.vts.vtsUtils.VehicleData;
import com.vts.vtsUtils.VehicleListAdaptor;

import android.content.res.Resources;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Build;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;

import android.widget.AbsListView;


import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import android.content.Context;

public class SelectVehicle extends Activity {

	private String allVehicleData = "";
	private JSONObject allVehicleDataJsonData;
	
	private static final String TAG_OWNER = "owner";
    private static final String TAG_DEVICEID = "imei";
    private static final String TAG_NUMBER = "deviceName";
	
	ListView customerListView;
	ArrayList<Vehicle> arrayList = new ArrayList<Vehicle>();
	VehicleListAdaptor adaptor;
	private boolean loadingMore = false;
	Handler  handler = new Handler();	
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("SelectVehicle", "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_vehicle);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
		Log.v("SelectVehicle", "onCreate data : " + allVehicleData);
	}

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setUpListView();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void setUpListView() throws MalformedURLException {
		customerListView = (ListView) findViewById(R.id.customerList);
		adaptor = new VehicleListAdaptor(SelectVehicle.this, R.layout.listitem, arrayList);
		customerListView.setAdapter(adaptor);

        URL url = null;

        try {
            url = new URL(getResources().getString(R.string.v3prdserver));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        //allVehicleData = Http.getInstance().vtsGetAllVehicleData();
        Http.HttpData resp =  Http.getInstance().vtsGetAllVehicleData(url);
        if(resp.status == HttpStatus.SC_OK) {
            allVehicleData = resp.data;
        }
        boolean status = LiveVehicleData.getInstance().parseLiveData(allVehicleData);
		if(status == true) {
			HashMap<String, HashMap<String, VehicleData>> vdata = LiveVehicleData.getInstance().getLiveData();
			
			for(String customer: vdata.keySet()) {
				//Log.v("HM", "key : " + customer + " " + liveVehicleData.get(customer));
				for(String vehicalNum : vdata.get(customer).keySet()) {
					VehicleData vd = vdata.get(customer).get(vehicalNum);
					Log.v("LiveVehicleData", " Customer : " + customer + " vehicle : " + vehicalNum + " Id: " + vd.deviceId + " type : " + vd.type + " Lat/lng : "
							+ vd.positionData[0].lat + "\t" + vd.positionData[0].lng);
					
					arrayList.add(new Vehicle(vehicalNum, customer, vd.deviceId));
				}
			}
			
			adaptor.notifyDataSetChanged();
		}
		
		customerListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
				
				/*// what is the bottom iten that is visible
	            int lastInScreen = firstVisibleItem + visibleItemCount;
	            if((lastInScreen == totalItemCount) && !(loadingMore)) {
	            	//Run background thread
	            	Thread tt = new loadBackgroung(lastInScreen, adaptor);
	            	tt.start();
	            }*/
			}
		});
		
		customerListView.setOnItemClickListener(new OnItemClickListener(){


			/*public void onItemClick(AdapterView<?>adapter,View v, int position){

			ItemClicked item = ((Menu) adapter).getItem(position);

			Intent intent = new Intent(Activity.this,destinationActivity.class);
			//based on item add info to intent
			startActivity(intent);

			}*/

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Log.v("MainActiviy ", "onItemClick" + position); 
				 Vehicle selectedVehicle = arrayList.get(position);
				 //Log.v("MainActiviy ", "selectedVehicle" + selectedVehicle.owner); 
			}


			});
		
		
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v("SelectVehicle", "onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_vehicle, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v("SelectVehicle", "onOptionsItemSelected");
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }


}

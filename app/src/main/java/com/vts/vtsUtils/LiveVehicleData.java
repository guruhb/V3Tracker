package com.vts.vtsUtils;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.util.Log;


public class LiveVehicleData {
	
	
	private HashMap<String, HashMap<String, VehicleData>> liveVehicleData;
	
	protected LiveVehicleData() { //cTor protected 
		
	}
	private static LiveVehicleData instance = null;
	
	public static LiveVehicleData getInstance() {
		if(instance == null) {
			Log.v("LiveVehicleData ", "LiveVehicleData getInstance");
			instance = new LiveVehicleData(); 
			instance.liveVehicleData = new HashMap<String, HashMap<String, VehicleData>>();
		}
		return instance;
	}
	
	public HashMap<String, HashMap<String, VehicleData>> getLiveData() {
		return liveVehicleData;
	}
	

	/* Input : JSON data 
	 * Output : true / false 
	 * State : Stores the live data in hashmap with Customer mapped to Vehicle number and Vehicle data
	 * 
	 */
	public boolean parseLiveData(String data) {
		/*Parse JSON allVechicleData and store it in HashMap
		 * 
		 */
		JSONObject jobject;
		try {
			jobject = new JSONObject(data);
			
			
			//parsing customer name
			for(int i = 0; i< jobject.names().length(); i++){
				String key = jobject.names().getString(i);
				if (!liveVehicleData.containsKey(key)) {
				    liveVehicleData.put(key, new HashMap<String, VehicleData>());
				}
				
				JSONArray list = jobject.getJSONArray(key);
				if(list != null){
			        for(int j = 0; j < list.length();j++) {
			            JSONObject elem = list.getJSONObject(j);
			            if(elem != null){
			            	JSONArray pA = elem.getJSONArray("positionData");
			            	PositionData positionData[] = null;
			            	if(pA != null){
			            		int pLen = pA.length();
			            		positionData = new PositionData[pA.length()]; //FIXME parse the value here
			            		for(int l = 0; l < pA.length(); l++) {
			            			JSONObject pObj = pA.getJSONObject(l);
			            			
			            			positionData[l] = new PositionData(	
							            					pObj.getDouble("latitude"),
							            					pObj.getDouble("longitude"),
							            					pObj.getDouble("speed"),
							            					pObj.getDouble("fuelLevel"),
							            					pObj.getDouble("batteryLevel"),
							            					pObj.getDouble("heading"),
							            					//new Date(pObj.getLong("time")),
                                                            getLocalDate(pObj.getLong("time")),
				        									pObj.getInt("satellites"),
							            					pObj.getBoolean("gpsFix"),
							            					pObj.getBoolean("batteryDisconnect"),
							            					pObj.getBoolean("ignition"),
							            					pObj.getBoolean("tampering"),
							            					pObj.getBoolean("panic"),
							            					pObj.getBoolean("ac")
	            									);
			            		}
			            		Log.v("s", "pa : " + pA.toString() + pLen);
			            	}
			            	
			            	String devName = elem.get("deviceName").toString();
			            	
			            	if(devName.length() > 0) {
			            		
			            		VehicleData vd = new VehicleData(elem.get("type").toString(),
			            				elem.get("deviceName").toString(),
			            				elem.get("driverName").toString(),
			            				Integer.parseInt(elem.get("maxSpeed").toString()),
			            				elem.get("imei").toString(),
			            				positionData);
			            		
			            		//FIXME : validate the delta in data and then update the has map
			            		if(liveVehicleData.get(key).containsKey(elem.get("deviceName").toString())) {
			            			//old date contained in hash map, remove it update the new one
			            			liveVehicleData.get(key).remove(liveVehicleData.get(key).get(elem.get("deviceName").toString()));
			            		}
			            		liveVehicleData.get(key).put(elem.get("deviceName").toString(), vd);
			            		Log.v("s", "devname : " + devName + vd.deviceName);
			            	}
			            }
			        }
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

    public VehicleData getVehicleDataFor(String vehicleNum) {
        for(String customer: liveVehicleData.keySet()) {
            //Log.v("HM", "key : " + customer + " " + liveVehicleData.get(customer));
            for(String vNum : liveVehicleData.get(customer).keySet()) {
                if(vehicleNum.equalsIgnoreCase(vNum)) {
                    return liveVehicleData.get(customer).get(vNum);
                }
            }
        }
        return null;
    }


    public int getTotalVehicleCount() {
        int count = 0;
        for(String customer: liveVehicleData.keySet()) {
            //Log.v("HM", "key : " + customer + " " + liveVehicleData.get(customer));
            for(String vehicalNum : liveVehicleData.get(customer).keySet()) {
                count++;
            }
        }
        return count;
    }
	public void dumpDataToConsole() {
		/*Reading 3d Hashmap
		 * HashMap<String, HashMap<String, VehicleData>>();
		 */
		for(String customer: liveVehicleData.keySet()) {
			//Log.v("HM", "key : " + customer + " " + liveVehicleData.get(customer));
			for(String vehicalNum : liveVehicleData.get(customer).keySet()) {
				VehicleData vd = liveVehicleData.get(customer).get(vehicalNum);
				Log.v("LiveVehicleData", " Customer : " + customer + " vehicle : " + vehicalNum + " Id: " + vd.deviceId + " type : " + vd.type + " Lat/lng : "
						+ vd.positionData[0].lat + "\t" + vd.positionData[0].lng);
			}
		}
	}

    private Date getLocalDate(long timeInMillisecond) {
       // long currentTime = System.currentTimeMillis();
        /*TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());*/

        //timeInMillisecond += offsetInMillis;
        Date date = new Date(timeInMillisecond);
        //Log.v("LiveVehicleData", "GMT to Local time : " + date.toString());

        return date;
    }
}

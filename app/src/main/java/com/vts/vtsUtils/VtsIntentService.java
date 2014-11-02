package com.vts.vtsUtils;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.vts.v3tracker.R;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

//FIXME : http://www.vogella.com/tutorials/AndroidServices/article.html
public class VtsIntentService extends IntentService {
	public VtsIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public static final String VTSINTENTSERVICERESPONSE = "com.vts.vtsUtils.result";

	public static final String INTENT_ACTION = "VtsINTENT_ACTION";
	public static final int INTENT_NOT_SPECIFIED = 0; 
	public static final int HTTP_USER_LOGIN = 1; 

	public static final String SERVICE_TYPE = "SERVICE_TYPE";
	public static final String SERVICE_RESPONSE = "SERVICE_RESPOSE";

	private static final String SERVICE_NONE = "SERVICE_NONE";

    public enum ServiceType {SERVICE_LOGIN, SERVICE_GETLIVEDATA, SERVICE_CALCULATE_ROUTE, SERVICE_NONE};
	
	
	public VtsIntentService() {
		super("VtsIntentService");
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		
		ServiceType servicetype = (ServiceType) intent.getSerializableExtra(SERVICE_TYPE); 
		Intent i = new Intent(VTSINTENTSERVICERESPONSE);
		i.putExtra(SERVICE_TYPE, servicetype);

		Bundle req = intent.getExtras();
		if(req != null) {
			switch(servicetype) {
			case SERVICE_NONE :
				Log.v("VtsIntentService", "Intent not specefied ");
				//perform some long running task !!! 
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			    i.putExtra("KEY", "keyvalue");
			    i.putExtra("VALUE", "resultvalue");
			    sendBroadcast(i);
				break; 
			case SERVICE_LOGIN : {
                    String uname = req.getString("uname");
                    String passwd = req.getString("passwd");
                URI uri = null;
                try {
                    uri = new URI(getResources().getString(R.string.v3prdserver));
                } catch (URISyntaxException e) {
                    Log.v("LoginActivity", "Getting url server from resource failed");
                    e.printStackTrace();
                }
                    Http.HttpData resp = Http.getInstance().vtsLogin(uname, passwd, uri);
                    if (resp.status == HttpStatus.SC_OK) {
                        i.putExtra(SERVICE_RESPONSE, true);
                    } else {
                        i.putExtra(SERVICE_RESPONSE, false); //FIXME : handle error case
                    }
                    sendBroadcast(i);
                }
				break;
			case SERVICE_GETLIVEDATA : 
				{
                    URL url = null;

                    try {
                        url = new URL(getResources().getString(R.string.v3prdserver));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }
                    Http.HttpData resp = Http.getInstance().vtsGetAllVehicleData(url);
                    if(resp.status == HttpStatus.SC_OK) {
                        if(resp.data.length() > 0 && (LiveVehicleData.getInstance().parseLiveData(resp.data) == true)) {
                            i.putExtra(SERVICE_RESPONSE, true);
                        }
                        else {
                            i.putExtra(SERVICE_RESPONSE, false);//FIXME : handle error case
                        }
                    }
                    sendBroadcast(i);
				}
				break;
            case SERVICE_CALCULATE_ROUTE :
                {
                Bundle locDetail = req.getBundle("location");
                    if(locDetail != null) {
                        double[] src = locDetail.getDoubleArray("src");
                        double[] dst = locDetail.getDoubleArray("dst");
                        LatLng srcLatLng = new LatLng(src[0], src[1]);
                        LatLng dstLatLng = new LatLng(dst[0], dst[1]);

                        String routeUrl = RouteJSONParser.getInstance().getRouteUrl(srcLatLng, dstLatLng );
                        String data = Http.getInstance().downloadUrl(routeUrl);
                        if(data != null) {
                            JSONObject jsonObject;
                            try{
                                jsonObject = new JSONObject(data);
                                boolean status = RouteJSONParser.getInstance().parse(jsonObject);
                                if(status) {
                                    Log.v("VtsIntentService", "RouteJSON parser parsed true");
                                    i.putExtra(SERVICE_RESPONSE, true);
                                }
                                else {
                                    Log.v("VtsIntentService", "RouteJSON parser parsed false");
                                    i.putExtra(SERVICE_RESPONSE, false);
                                }
                            }catch (JSONException e) {
                                Log.v("VtsIntentService", "JSONexception ");
                                i.putExtra(SERVICE_RESPONSE, false);
                            }


                        }
                    }
                    sendBroadcast(i);
                }
            break;
			}

		}

	}



}

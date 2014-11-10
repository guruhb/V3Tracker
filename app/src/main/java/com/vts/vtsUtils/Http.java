package com.vts.vtsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.support.v7.appcompat.R.bool;
import android.util.Log;
import java.net.CookieHandler;
import java.net.CookieManager;
import com.vts.v3tracker.R;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpStatus;

import static org.apache.http.HttpStatus.*;


//NOTE: 1. user login async to activity 
//2. get data in view activity
//3. FIXME : implement network status callback
//4. FIXME : handle each and every network related errors

public class Http {

	public boolean userLogin = false;
    private final String USER_AGENT = "Mozilla/5.0";

    private HttpClient mHttpClient = null;
	private HttpContext mHttpContext = null;
    // Create a local mInstance of cookie store
    private CookieStore mCookieStore = new BasicCookieStore();



	private static Http mInstance = null;
	
	protected Http() {
		//to avoid default mInstance 
	}


    public class HttpData {
        public int status;
        public String data;
    };


	public static Http getInstance() {
		if(mInstance == null) {
			Log.v("Http ", "Http getInstance");
			mInstance = new Http(); 

			mInstance.mHttpContext = new BasicHttpContext();
            HttpParams httpParameters = new BasicHttpParams();
            mInstance.mCookieStore = new BasicCookieStore();
            /*HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 30000);
            mInstance.mHttpClient = new DefaultmHttpClient(httpParameters);*/ //FIXME : in emulator there was always timeout
            mInstance.mHttpClient = new DefaultHttpClient();
           // mInstance.mHttpClient.getParams().setParameter(mHttpClientParams.CONNECTION_MANAGER_TIMEOUT, new Long(5000))
            mInstance.mHttpContext.setAttribute(ClientContext.COOKIE_STORE, mInstance.mCookieStore);
            mInstance.mHttpContext.setAttribute(ClientContext.USER_TOKEN, mInstance.mCookieStore);
            mInstance.mHttpContext.setAttribute(ClientContext.COOKIE_ORIGIN, mInstance.mCookieStore);
           mInstance.mHttpContext.setAttribute(ClientContext.COOKIE_SPEC, mInstance.mCookieStore);
 		}
		return mInstance;
	}

    public String downloadUrl(String url) {
        HttpData response = new HttpData();
        HttpGet get = new HttpGet(url);
        StringBuilder builder = new StringBuilder();
        try{
            HttpResponse httpResponse = mHttpClient.execute(get);
            if(httpResponse.getStatusLine().getStatusCode() == SC_OK ) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                response.status = SC_OK;
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                response.data = builder.toString();
                httpResponse.getEntity().consumeContent();
            }
            else  {
                Log.d("Http", "Http live data failed with status : " +  httpResponse.getStatusLine().getStatusCode());
                return null;
            }
        }catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
        return response.data;
    }
	
	public HttpData vtsGetAllVehicleData(URL url) {
		Log.v("Http ", "vtsGetAllVehicleData url :  " + url.toString());
        HttpData response = new HttpData();
		String SetServerString = "";
		String content = "";

		try {
    	 HttpGet httpget = new HttpGet(url.toString());

            HttpResponse httpResponse = mHttpClient.execute(httpget, mHttpContext);
            if(httpResponse.getStatusLine().getStatusCode() == SC_OK ) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                response.status = SC_OK;
                response.data = reader.readLine();
                httpResponse.getEntity().consumeContent();
            }
            else  {
                Log.d("Http", "Http live data failed with status : " +  httpResponse.getStatusLine().getStatusCode());
            }
		} catch (ClientProtocolException e) {
            // TODO Auto-generated catch block

        	Log.v("Http ", "vtsGetAllVehicleData ClientProtocol Exception: msg : " + e.getMessage());

            response.status = 0;
            response.data = "";
            return response;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.v("Http ", "vtsGetAllVehicleData ClientProtocol Exception auto generate: msg : " + e.getMessage());

            response.status = SC_REQUEST_TIMEOUT;
            response.data = "";
        	return response;
        }
		Log.v("Http ", "vtsGetAllVehicleData <--");
		return response;
	}
	
	public HttpData vtsLogin(String username, String password, URI uri) {
        HttpData response = new HttpData();
        // Perform action on click
    	Log.v("Http", "vtsLogin url : " + uri + " user name :  " + username);

        
    	if(userLogin == true) {
    		Log.v("Http", "user already logged in");
            response.status = 0;//FIXME : check for proper condition
    		return response;
    	}

        HttpPost httppost = new HttpPost(uri.toString());
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            
            nameValuePairs.add(new BasicNameValuePair("name", "loginForm"));
            nameValuePairs.add(new BasicNameValuePair("action", "home"));
            
            nameValuePairs.add(new BasicNameValuePair("user", username));
            nameValuePairs.add(new BasicNameValuePair("p", password));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            Log.v("Http ", "mHttpClient execute post ");
            mHttpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            // Execute HTTP Post Request

            HttpResponse httpResponse = mHttpClient.execute(httppost, mHttpContext);

            int status = httpResponse.getStatusLine().getStatusCode();
            if(status == 200) {
            	userLogin = true;
                response.status = SC_OK;
                response.data = "";

            } else {
                //FIXME : handle error case here
            }
            httpResponse.getEntity().consumeContent();
            Log.v("Http ", "Http status : " + status);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        	Log.v("LoginScreen ", "ClientProtocol Exception: msg : " + e.getMessage());
            response.status = 0; //FIXME : check for proper condition
            response.data = "";
            return response;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            String msg = e.getMessage();
            Log.v("Http ", "ClientProtocol Exception auto generate: msg : " + msg);
            response.status = SC_REQUEST_TIMEOUT;
            response.data = "";
            return response;
        }
		return response;
		
	}
}

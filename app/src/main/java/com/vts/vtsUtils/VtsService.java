package com.vts.vtsUtils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Random;

public class VtsService extends Service {

	public static final int MSG_SAY_HELLO = 1;
	private final IBinder mBinder = new HttpBinder();
	private final Random mGenerator = new Random();
	
	
	public class HttpBinder extends Binder {
		public VtsService getService() {
			return VtsService.this;
		}
	}
	
	public int getRandomNumber() {
		return mGenerator.nextInt(10);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
}

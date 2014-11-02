package com.vts.vtsUtils;

import java.util.Date;

public class PositionData {
	public double lat;
	public double lng;
	public double speed;
	public double fuelLevel;
	public double batteryLevel;
	public double heading;
	public Date time;
	public int satellites;
	public boolean gpsFix;
	public boolean batteryDisconnect;
	public boolean ignition;
	public boolean tampering;
	public boolean panic;
	public boolean ac;
	PositionData() {
		this.lng = (double) 0.0;//FIXME : fill all the default values here 
		this.lat = (double) 0.0;
	}
	PositionData(double lat, 
			double lng, 
			double speed,  
			double fuelLevel,
			double batteryLevel,
			double heading,
			Date time,
			int satellites,
			boolean gpsFix,
			boolean batteryDisconnect,
			boolean ignition,
			boolean tampering,
			boolean panic,
			boolean ac) {
		this.lat = lat;
		this.lng = lng;
		this.speed = speed;
		this.fuelLevel = fuelLevel;
		this.batteryLevel = batteryLevel;
		this.heading = heading;
		this.time = time;
		this.satellites = satellites;
		this.gpsFix = gpsFix;
		this.batteryDisconnect = batteryDisconnect;
		this.ignition = ignition;
		this.tampering = tampering;
		this.panic = panic;
		this.ac = ac;
	}
};

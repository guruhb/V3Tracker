package com.vts.vtsUtils;

import java.util.Date;


/*

"positionData": [{
	"fuelLevel": 18.7,
	"batteryDisconnect": false,
	"gpsFix": true,
	"speed": 0,
	"ignition": false,
	"tampering": false,
	"time": 1410254923000,
	"panic": false,
	"batteryLevel": 18.19,
	"ac": true,
	"longitude": 77.602448,
	"latitude": 13.10648,
	"satellites": 9,
	"heading": 0
}],
"routePlan": [{
	"boundary": []
},
{
	"trip": []
}],
"type": "Bus",
"deviceName": "KA43-1912",
"driverName": "Route No 4 ( RAJU)",
"maxSpeed": "70",
"imei": "869988017289832"
*/



public class VehicleData {
	
	public String type;
	public String deviceName;
	public String driverName;
	public int maxSpeed;
	public String deviceId;
	public PositionData[] positionData;
	
	VehicleData(String type, String deviceName, String drivername, int maxSpeed, String deviceId, PositionData[] p) {
		this.type = type;
		this.deviceName = deviceName;
		this.driverName = drivername;
		this.maxSpeed = maxSpeed;
		this.deviceId = deviceId;
		this.positionData = p;
	}
};

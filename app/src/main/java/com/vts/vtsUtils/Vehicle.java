package com.vts.vtsUtils;

public class Vehicle {
	 String number,owner,imei;  
	 	public Vehicle(String number,String owner,String imei)
	    {
	        this.number = number;
	        this.owner = owner;
	        this.imei = imei;
	    }
		public String getNumber() {
			return number;
		}
		public void setNumber(String number) {
			this.number = number;
		}
		public String getOwner() {
			return owner;
		}
		public void setOwner(String owner) {
			this.owner = owner;
		}
		public String getImei() {
			return imei;
		}
		public void setImei(String imei) {
			this.imei = imei;
		}
	    
}

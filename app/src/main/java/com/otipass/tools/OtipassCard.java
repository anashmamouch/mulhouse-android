/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: OtipassCard.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.tools;

import java.io.Serializable;
import java.util.Calendar;

import models.Constants;

public class OtipassCard implements Serializable {
	public static final int cOtipassCard = 0;
	public static final int cNotOtipassCard = 1;

	private int numotipass;
	private String serial;
	private String client;
	private int status;
	private Calendar expiry;
	// type of the card normal,free,short duration, old pass
	private int type;
	
	public OtipassCard(int numotipass, String serial, String client, int status) {
		this.numotipass = numotipass;
		this.serial = serial;
		this.client = client;
		this.status = status;
		this.expiry = null;
	}


	public OtipassCard(int numotipass) {
		this.type = Constants.PASS_INITIAL;
		this.numotipass = numotipass;
		this.serial = "";
		this.client = "";
		this.status = 0;
		this.expiry = null;
	}

	public OtipassCard(String serial) {
		this.type = Constants.PASS_INITIAL;
		this.numotipass = 0;
		this.serial = serial;
		this.client = "";
		this.status = 0;
		this.expiry = null;
	}

	public OtipassCard(int type, int numotipass) {
		this.type = type;
		this.numotipass = numotipass;
		this.serial = "";
		this.client = "";
		this.status = 0;
		this.expiry = null;
	}

	public OtipassCard(int numotipass, String client) {
		this.type = Constants.PASS_INITIAL;
		this.numotipass = numotipass;
		this.serial = "";
		this.client = client;
		this.status = 0;
		this.expiry = null;
	}
	
	public OtipassCard(int type, int numotipass, int status) {
		this.type = type;
		this.numotipass = numotipass;
		this.serial = "";
		this.client = "";
		this.status = status;
		this.expiry = null;
	}
	
	public OtipassCard(int numotipass, int status, int type, String serial, Calendar expiry) {
		this.type = type;
		this.numotipass = numotipass;
		this.serial = serial;
		this.client = "";
		this.status = status;
		this.expiry = expiry;
	}
	
	public OtipassCard(int type, Calendar expiry) {
		this.type = type;
		this.numotipass = 0;
		this.serial = "";
		this.client = "";
		this.status = 0;
		this.expiry = expiry;
	}
	
	
	public int getNumotipass() {
		return numotipass;
	}
	
	public void setNumotipass(int numotipass) {
		this.numotipass = numotipass;
	}
	
	public int getStatus() {
		return status;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getSerial() {
		return serial;
	}
	
	public String getClient() {
		return client;
	}


	public Calendar getExpiry() {
		return expiry;
	}
	public void setExpiry(Calendar expiry) {
		this.expiry = expiry;
	}

	
	public static boolean checkLuhnOK(int numOtipass)	{
		boolean ok = false;
	    int vL = 9, vR, num = numOtipass / 10, i;
	    do {
	        vR = num % 100;
	        vL += (int)(vR / 10);
	        vR %= 10;
	        i = (vR > 4) ? 1 : 0;
	        vL += (vR + vR + i);
	        num /= 100;
	        num = (int)num;
	        }
	    while(num != 0);
	    if (9-(vL % 10) == (numOtipass % 10)) {
	    	ok = true;
	    }
	    return ok;
	}
	
}

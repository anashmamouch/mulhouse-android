/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Coupon.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;


public class Coupon {
	
	private long id;
	private short service;
	private String date;
	private String serial;
	private short nb;
	private short status;
	private boolean uploaded;
	

	public Coupon() {
		
	}
	
	public Coupon(long id, short service, String date, String serial, short nb, short status) {
		this.id = id;
		this.service = service;
		this.date = date;
		this.serial = serial;
		this.nb = nb;
		this.status = status;
		this.uploaded = false;
	}
	
	public Coupon(short service, String date, String serial, short nb, short status) {
		this.service = service;
		this.date = date;
		this.serial = serial;
		this.nb = nb;
		this.status = status;
		this.uploaded = false;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}


	public short getService() {
		return service;
	}

	public void setService(short service) {
		this.service = service;
	}


	public short getNb() {
		return nb;
	}

	public void setNb(short nb) {
		this.nb = nb;
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
}

/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: Otipass.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */
package com.otipass.sql;

import models.Constants;

public class Otipass implements Cloneable{
	private long numOtipass;
	private String serial;
	private short status;
	private String expiry;
	private short type;
	private int pid;
	private String service;
	private String use_day;

	public Otipass() {
		
	}
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	public Otipass(long numOtipass, String serial, short status, String expiry, short type, int pid, String service) {
		this.numOtipass = numOtipass;
		this.serial = serial;
		this.status = status;
		this.expiry = expiry;
		this.type = type;
		this.pid = pid;
		this.service = service;
	}
	public Otipass(long numOtipass, String serial, short status, String expiry, short type, int pid, String service, String use_day) {
		this.numOtipass = numOtipass;
		this.serial = serial;
		this.status = status;
		this.expiry = expiry;
		this.type = type;
		this.pid = pid;
		this.service = service;
		this.use_day = use_day;
	}

	public Otipass(long numOtipass) {
		this.numOtipass = numOtipass;
		this.status = Constants.PASS_CREATED;
		this.type = Constants.PASS_INITIAL;
	}
	
	public long getNumOtipass() {
		return this.numOtipass;
	}

	public void setNumOtipass(long numOtipass) {
		this.numOtipass = numOtipass;
	}

	public String getSerial() {
		return this.serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public short getStatus() {
		return this.status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public String getExpiry() {
		return this.expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public short getType() {
		return this.type;
	}

	public void setType(short type) {
		this.type = type;
	}
	
	public void setPid(int pid){
		this.pid = pid;
	}
	
	public int getPid(){
		return pid;
	}
	
	public void setService(String service){
		this.service = service;
	}
	
	public String getService(){
		return service;
	}

	public void setUseDay(String use_day){
		this.use_day = use_day;
	}
	
	public String getUseDay(){
		return use_day;
	}
}

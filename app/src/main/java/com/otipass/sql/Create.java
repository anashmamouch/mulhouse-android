/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Create.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */

package com.otipass.sql;


public class Create {
	private long id;
	private int numotipass;
	private String serial;
	private short type;
	private short status;
	private int pid;
	private String srv;
	private String expiry;

	public Create() {

	}

	public Create(int numotipass, short type, String serial, short status, int pid, String srv, String expiry) {
		this.numotipass = numotipass;
		this.type       = type;
		this.serial     = serial;
		this.status     = status;
		this.pid        = pid;
		this.srv        = srv;
		this.expiry     = expiry;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public int getNumotipass() {
		return numotipass;
	}

	public void setNumotipass(int numotipass) {
		this.numotipass = numotipass;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	public void setPid(int pid){
		this.pid = pid;
	}
	
	public int getPid(){
		return pid;
	}
	
	public void setService(String srv){
		this.srv = srv;
	}
	
	public String getService(){
		return srv;
	}
	
	public void setExpiry(String expiry){
		this.expiry = expiry;
	}
	
	public String getExpiry(){
		return expiry;
	}
}

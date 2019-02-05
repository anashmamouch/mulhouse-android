/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Partial.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;


public class Partial {
	private long id;
	private int numotipass;
	private short status;
	private String expiry;
	private int pid;

	public Partial() {
		
	}
	
	public Partial(int numotipass, short status, String expiry, int pid) {
		this.numotipass = numotipass;
		this.status = status;
		this.expiry = expiry;
		this.pid    = pid;
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

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}
	
	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}
	
	public void setPid(int pid){
		this.pid = pid;
	}
	
	public int getPid(){
		return pid;
	}
}

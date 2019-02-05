/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Entry.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;


public class Entry {
	public static final short cNormalEntry = 1;
	public static final short cForcedEntry = 2;
	
	private long id;
	private String date;
	private int numotipass;
	private short nb;
	private short event;
	private boolean uploaded;
	private int service;

	
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

	public int getNumotipass() {
		return numotipass;
	}

	public void setNumotipass(int numotipass) {
		this.numotipass = numotipass;
	}

	public int getService() {
		return service;
	}

	public void setService(int service) {
		this.service = service;
	}

	public short getNb() {
		return nb;
	}

	public void setNb(short nb) {
		this.nb = nb;
	}

	public short getEvent() {
		return event;
	}

	public void setEvent(short event) {
		this.event = event;
	}

	public boolean getUploaded() {
		return uploaded;
	}

	public void setUploaded(boolean uploaded) {
		this.uploaded = uploaded;
	}
}

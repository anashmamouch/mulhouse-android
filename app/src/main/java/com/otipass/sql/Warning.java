/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Warning.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Warning {
	public static final short cInvalidStatus = 1;
	
	private long id;
	private String date;
	private String serial;
	private short event;

	public Warning(String date, String serial, short event) {
		this.date = date;
		this.serial = serial;
		this.event = event;
	}
	
	public Warning() {
		
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


	public short getEvent() {
		return event;
	}

	public void setEvent(short event) {
		this.event = event;
	}

}

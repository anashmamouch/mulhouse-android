/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Param.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Param {
	private long id;
	private String name;
	private String call;
	private String softwareVersion;
	private int debug;
	private int category;
	
	public Param() {
		
	}

	public Param(String name, String call,  String softwareVersion, int debug, int category) {
		this.name = name;
		this.call = call;
		this.softwareVersion = softwareVersion;
		this.debug = debug;
		this.category = category;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getCall() {
		return call;
	}

	public void setCall(String call) {
		this.call = call;
	}


	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public int getDebug() {
		return debug;
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}
	
	public int getCategory(){
		return category;
	}
	
	public void setCategory(int category){
		this.category = category;
	}
}

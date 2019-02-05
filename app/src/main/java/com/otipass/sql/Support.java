/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6355 $
 $Id: Support.java 6355 2016-06-12 16:09:44Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Support {
	private String msg;
	private int type;
	private int id;
	private int hidden;
	private String date;
	private String query;
	private int parent;
	private int event;

	
	public Support() {
		
	}
	

	public Support(String msg, int type, int id, String date, int parent, int event, String query) {
		this.msg = msg;
		this.type = type;
		this.id = id;
		this.hidden = 0;
		this.date = date;
		this.query = query;
		this.parent = parent;
		this.event = event;
	}

	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public void setHidden(int hidden) {
		this.hidden = hidden;
	}
	public int getHidden() {return hidden;}

	public void setParent(int parent) {
		this.parent = parent;
	}
	public int getParent() {return parent;}

	public void setEvent(int event) {
		this.event = event;
	}
	public int getEvent() {return event;}

	public String getDate() {return date;}
	public void setDate(String date) {
		this.date = date;
	}

	public String getQuery() {return query;}
	public void setQuery(String query) {
		this.query = query;
	}
}
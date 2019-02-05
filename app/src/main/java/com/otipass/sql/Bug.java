/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6355 $
 $Id: SQLiteHelper.java 6355 2016-06-12 16:09:44Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Bug {

	private int id;
	private String text;
	private String date;


	public Bug(String text, String date){

		this.text = text;
		this.date = date;
	}

	public Bug(){

	}


	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setText(String text){
		this.text = text;
	}

	public String getText(){
		return text;
	}

	public void setDate(String date){
		this.date = date;
	}

	public String getDate(){
		return date;
	}

}

/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Usage.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Usage {

	int numotipass;
	String date;

	public Usage(){

	}

	public Usage(int numotipass, String date){
		this.numotipass = numotipass;
		this.date = date;
	}

	public void setNumOtipass(int numotipass){
		this.numotipass = numotipass;
	}

	public int getNumOtipass(){
		return numotipass;
	}

	public void setDate(String date){
		this.date = date;
	}

	public String getDate(){
		return date;
	}

}

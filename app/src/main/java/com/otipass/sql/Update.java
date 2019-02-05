/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Update.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Update {
	private long id;
	private String date;
	private int type;
	private int numotipass;
	private int pid;
	private String name;
	private String fname;
	private String email;
	private String postalCode;
	private String country;
	private int newsletter;
	private int twin;

	public Update() {

	}

	public Update(String date, int type, int numotipass, int pid, String name, String fname, String email, String country, String postalCode, int newsletter, int twin) {
		this.date = date;
		this.numotipass = numotipass;
		this.type = type;
		this.pid = pid;
		this.name = name;
		this.fname = fname;
		this.email = email;
		this.postalCode = postalCode;
		this.country = country;
		this.newsletter = newsletter;
		this.twin = twin;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getNumotipass() {
		return numotipass;
	}

	public void setNumotipass(int numotipass) {
		this.numotipass = numotipass;
	}

	public void setPid(int pid){
		this.pid = pid;
	}

	public int getPid(){
		return pid;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setFname(String fname){
		this.fname = fname;
	}

	public String getFname(){
		return fname;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	public void setCountry(String country){
		this.country = country;
	}

	public String getCountry(){
		return country;
	}

	public void setPostalCode(String postal_code){
		this.postalCode = postal_code;
	}

	public String getPostalCode(){
		return postalCode;
	}

	public void setNewsletter(int newsletter){
		this.newsletter = newsletter;
	}

	public int getNewsletter(){
		return newsletter;
	}
	
	public void setTwin(int twin){
		this.twin = twin;
	}
	
	public int getTwin(){
		return twin;
	}
}

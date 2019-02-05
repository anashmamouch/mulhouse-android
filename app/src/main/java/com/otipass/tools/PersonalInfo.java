/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: PersonalInfo.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.tools;

import java.io.Serializable;

public class PersonalInfo implements Serializable {

	private String firstName;
	private String name;
	private String email;
	private String postal_code;
	private String country;
	private boolean newsletter;

	public PersonalInfo(){

	}

	public PersonalInfo(String firstName, String name, String email, String postal_code, String country, boolean newsletter){

		this.firstName   = firstName;
		this.name        = name;
		this.email       = email;
		this.postal_code = postal_code;
		this.country     = country;
		this.newsletter  = newsletter;
	}


	public void setFirstName(String firstName){
		this.firstName = firstName;
	}

	public String getFirstName(){
		return firstName;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return email;
	}

	public String getPostalCode(){
		return postal_code;
	}

	public void setPostalCode(String postal_code){
		this.postal_code = postal_code;
	}

	public String getCountry(){
		return country;
	}

	public void setCountry(String country){
		this.country = country;
	}

	public void setNewsletter(boolean newsletter){
		this.newsletter = newsletter;
	}

	public boolean getNewsletter(){
		return newsletter;
	}

}

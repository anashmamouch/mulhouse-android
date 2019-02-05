/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: ProviderService.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class ProviderService {

	int package_id;
	String service; 

	public ProviderService(){

	}

	public ProviderService(int package_id, String service){
		this.package_id = package_id;
		this.service = service;
	}

	public void setPackageId(int package_id){
		this.package_id = package_id;
	}

	public int getPackageId(){
		return package_id;
	}

	public void setService(String service){
		this.service = service;
	}

	public String getService(){
		return service;
	}

}

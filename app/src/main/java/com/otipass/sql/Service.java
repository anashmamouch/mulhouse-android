/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6450 $
 $Id: Service.java 6450 2016-07-06 09:43:10Z ede $

 ================================================================================
 */
package com.otipass.sql;

import java.util.List;

public class Service {
	private long id;
	private String name;
	private int type;
	
	public Service() {
		
	}

	public Service(long id, String name) {
		this.id = id;
		this.name = name;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public static String getServiceName(List<Service> list, int service) {
		String name = "";
		for (int i = 0; i<list.size(); i++) {
			Service srv = list.get(i);
			if (srv.getId() == service) {
				name = srv.getName();
				break;
			}
		}
		return name;
	}
	
	public static Service getCurrentService(List<Service> list) {
		Service service = null;
		if (list.size() > 0) {
			service = list.get(0);
		}
		return service;
	}

}

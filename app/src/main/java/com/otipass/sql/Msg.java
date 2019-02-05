/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6355 $
 $Id: Msg.java 6355 2016-06-12 16:09:44Z ede $

 ================================================================================
 */
package com.otipass.sql;

import android.text.SpannableString;
import android.text.Spanned;

public class Msg {
	private String msg;
	private Spanned spanned;
	private String btnMsg;
	private int type;
	private int minProfile;
	private int id;
	private String startDate;
	private String endDate;
	private String lang;
	private int hidden;

	
	public Msg() {
		
	}
	
	public Msg(String msg, int type, int id, int minProfile) {
		this.msg = msg;
		this.type = type;
		this.id = id;
		this.minProfile = minProfile;
		this.lang = "fr";
		this.hidden = 0;
	}
	public Msg(String msg, int type, int id, int minProfile, String btnMsg) {
		this.msg = msg;
		this.btnMsg = btnMsg;
		this.type = type;
		this.id = id;
		this.minProfile = minProfile;
		this.lang = "fr";
		this.hidden = 0;
	}

	public Msg(String msg, int id, int type, String startDate, String endDate, String lang) {
		this.msg = msg;
		this.type = type;
		this.id = id;
		this.minProfile = minProfile;
		this.startDate = startDate;
		this.endDate = endDate;
		this.lang = "fr";
		this.hidden = 0;
		this.btnMsg = btnMsg;
	}
	public Msg(String msg, int type, int id, int minProfile, String startDate, String endDate, String btnMsg) {
		this.msg = msg;
		this.type = type;
		this.id = id;
		this.minProfile = minProfile;
		this.startDate = startDate;
		this.endDate = endDate;
		this.lang = "fr";
		this.hidden = 0;
		this.btnMsg = btnMsg;
	}

	public Msg(Spanned msg, int type, int id, int minProfile, String startDate, String endDate, String btnMsg) {
		this.spanned = msg;
		this.type = type;
		this.id = id;
		this.minProfile = minProfile;
		this.startDate = startDate;
		this.endDate = endDate;
		this.lang = "fr";
		this.hidden = 0;
		this.btnMsg = btnMsg;
	}

	public Msg(Spanned msg, int type, int id, int minProfile, String btnMsg) {
		this.msg = "";
		this.spanned = msg;
		this.type = type;
		this.id = id;
		this.minProfile = minProfile;
		this.btnMsg = btnMsg;
		this.startDate = "";
		this.endDate = "";
		this.lang = "fr";
		this.hidden = 0;
	}

	public String getMsg() {
		return msg;
	}
	public String getBtnMsg() {
		return btnMsg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	public void setBtnMsg(String msg) {
		this.btnMsg = msg;
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

	public int getMinProfile() {
		return minProfile;
	}

	public void setMinProfile(int profile) {
		this.minProfile = minProfile;
	}

	public String getStartDate() {
		return startDate;
	}
	
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}
	
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getLang() {
		return lang;
	}
	
	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setHidden(int hidden) {
		this.hidden = hidden;
	}
	public int getHidden() {return hidden;}

	public Spanned getSpanned() {return spanned;}
}
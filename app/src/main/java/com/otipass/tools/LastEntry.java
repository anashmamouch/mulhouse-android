/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: LastEntry.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */
package com.otipass.tools;


import java.util.Calendar;

import models.Constants;

import com.otipass.sql.DbAdapter;
import com.otipass.sql.Entry;
import com.otipass.sql.Otipass;

import android.content.Context;
import android.util.Log;


public class LastEntry {
	private Context context = null;

	public static final int cOK = 0;
	public static final int cCancelTooLate = 1;
	public static final int cNoEntryToCancel = 2;
	public static final int cSaveFailed = 3;
	
    private long id;
	private Calendar date;
    private Otipass otipass; // this is the otipass image before entry
	private boolean entry2Cancel = false;
	private String serial2Show;

	// use to make a singleton, only one instance class
	private static LastEntry mInstance = null;
	
	// use this function to call an instance 
	public static LastEntry getInstance(Context context) {
		if (mInstance == null) {      
			mInstance = new LastEntry(context);
		}    
		return mInstance;  
	}
	
	public LastEntry(Context context) {
		this.context = context;
	}

	public void setEntry2Cancel(boolean val) {
		entry2Cancel = val;
	}

	public boolean getEntry2Cancel() {
		return entry2Cancel;
	}
	public Calendar getDate() {return date;}
    public long getid() {return id;}
    public Otipass getOtipass() {return otipass;}
	public String getSerial2Show() {return serial2Show;}

	public void recordLastEntry(long id, Otipass otipass, Calendar date, String s) {
        this.id = id;
		this.otipass = otipass;
		this.date = date;
		entry2Cancel = true;
		serial2Show = s;
	}

    public long getLastEntry(Calendar now) {
    	long lastentryId = 0;
    	if (entry2Cancel) {
    		try {
	        	Calendar tmpDate = (Calendar) date.clone();
		    	// sale can be cancelled maximum 15 minutes after it occured
	    		tmpDate.add(Calendar.MINUTE, 15);
		    	if (date.before(tmpDate)) {
                    lastentryId = id;
		    	}
        	} catch (Exception ex) {
        		Log.e(Constants.TAG, LastEntry.class.getName() + " - getLastEntry - " + ex.getMessage());
        	}
    	} 
    	return lastentryId;
    }
	
    public int cancelLastEntry() {
    	int error = cOK;
    	DbAdapter dbAdapter = new DbAdapter(context);
    	dbAdapter.open();
    	try {
    		error = dbAdapter.cancelEntry(this);
    	} catch (Exception ex) {
    		Log.e(Constants.TAG, LastEntry.class.getName() + " - cancelLastEntry - " + ex.getMessage());
    		error = cSaveFailed;
    	}
    	finally {
            entry2Cancel = false;
    	}
    	return error;
    }
	
}


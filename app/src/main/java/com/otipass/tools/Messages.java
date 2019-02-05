/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6358 $
 $Id: Messages.java 6358 2016-06-13 09:54:21Z ede $

 ================================================================================
 */

package com.otipass.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Constants;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

import com.otipass.mulhouse.R;
import com.otipass.sql.Msg;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.Support;
import com.otipass.sql.Tablet;
import com.otipass.sql.Warning;
import com.otipass.sql.Wl;
import com.otipass.swdownload.SwDownload;
import com.otipass.mulhouse.Footer;

public class Messages {
	public static final int cMsgGeneral = 1; 
	public static final int cMsgStock = 2;
	public static final int cMsgSWversion = 3;
	public static final int cMsgSynchro = 4;
	public static final int cMsgDB = 5;
	public static final int cMsgSupport = 6;

    private static final int cDummyId1 = 100000;
    private static final int cDummyId2 = 100001;
    private static final int cDummyId3 = 100003;

	// use mInstance to make a singleton
	private static Messages mInstance = null;
	
	private List<Msg> messagesList;

	// use this function to call an instance 
	public static Messages getInstance() {          
		if (mInstance == null) {      
			mInstance = new Messages();    
		}    
		return mInstance;  
	}

	// private constructor to use only getInstance() when creating an instance
	private Messages() {
		messagesList = new ArrayList<Msg>();
	}

	private int getMaxId() {
		int id = 0;
		for (Msg msg : messagesList) {
		    if(msg.getId() > id){
		    	id = msg.getId();
		    }
		}		
		return id;
	}
	
	public void addMessage(String msg, int type, int id, int minProfile, String startDate, String endDate, String btnMsg) {
		Msg message = new Msg(Html.fromHtml(msg), type, id, minProfile, startDate,endDate,  btnMsg);
		messagesList.add(message);
	}
	public void addSpannedMessage(Spanned msg, int id, int type, String btnMsg) {
		Msg message = new Msg(msg, type, id, Constants.USR_CONTROLLER,  btnMsg);
		messagesList.add(message);
	}


	public void clearMessages() {
		messagesList.clear();
	}

	public void clearMessage(int id) {
		int pos = 0;
		for (Msg msg : messagesList) {
		    if(msg.getId() == id){
		    	messagesList.remove(pos);
		    	break;
		    } else {
		    	pos++;
		    }
		}		
		
	}
	
	public List<Msg> getMessages(int userProfile) {
		List<Msg> list = new ArrayList<Msg>();
		// return only messages according to profile
		if (messagesList.size() > 0) {
			for (Msg msg : messagesList) {
				if (msg.getMinProfile() <= userProfile) {
					list.add(msg);
				}
			}			
		}
		return list;
	}

    public int checkMessages(Context context, DbAdapter dbAdapter, int userProfile) {
    	String message;
    	int nb = 0, nbCards = 0, wlStatus;
		clearMessages();
		Footer footer = Footer.getInstance();
		try {
			boolean write = false, partialBDD = false;
			// integrity check of the database
			Wl wl = dbAdapter.getWl(1L);
			if (wl != null) {
				wlStatus = wl.getStatus();
				switch (wlStatus) {
				case Constants.WL_DONE:
					break;
				default:
					partialBDD = true;
					break;
				}
				if (partialBDD) {
	    			// The database is not complete, request for FULL synchronisation
					message = context.getString(R.string.partial_BDD) + "\n";
	    			if (footer.isOnline()) {
	    				message += context.getString(R.string.vous_devez_synchroniser);
	    			} else {
	    				message += context.getString(R.string.msg_pas_connecte);
	    				message += "\n";
	    				message += context.getString(R.string.vous_devez_connecter_et_synchroniser);
	    			}
					addMessage(message, Messages.cMsgDB, cDummyId1, Constants.USR_CONTROLLER,  "", "", context.getString(R.string.synchroniser));
				}
			}
			
			
    	} catch (Exception ex) {
    		Log.e(Constants.TAG, "Messages.checkMessages() - DB " + ex.getMessage());
    	}
    	try {
    		// check synchronization
    		Tablet tablet = dbAdapter.getTablet(1L);
    		String time = tablet.getDownloadTime();
    		Calendar now = Calendar.getInstance();
    		Calendar download = tools.setCalendar(time);
//    		now.add(Calendar.DAY_OF_MONTH, 1);
    		now.set(Calendar.HOUR_OF_DAY, 0);
    		now.set(Calendar.MINUTE, 0);
    		if (now.after(download)) {
    			// no night synchronization today, set a synchronization message
				message = context.getString(R.string.msg_pas_de_synchro_nuit) + "\n";
    			if (footer.isOnline()) {
    				message += context.getString(R.string.vous_devez_synchroniser);
    			} else {
    				message += context.getString(R.string.msg_pas_connecte);
    				message += "\n";
    				message += context.getString(R.string.vous_devez_connecter_et_synchroniser);
    			}
    			addMessage(message, Messages.cMsgSynchro, cDummyId2, Constants.USR_CONTROLLER,  "", "", context.getString(R.string.synchroniser));
    			nb++;
    		}
    	} catch (Exception ex) {
    		Log.e(Constants.TAG, "HomeActivity.checkMessages() - synchro " + ex.getMessage());
    	}
    	try {
    		// check new software download
    		boolean newVersion = SwDownload.detectNewSoftwareVersion(context, dbAdapter);
    		//newVersion = true;
    		if (newVersion) {
				message = context.getString(R.string.nouvelle_version_dispo) + "\n";
    			if (footer.isOnline()) {
    				message += context.getString(R.string.vous_devez_telechrager);
    			} else {
    				message += context.getString(R.string.msg_pas_connecte);
    				message += "\n";
    				message += context.getString(R.string.vous_devez_connecter_et_telecharger);
    			}
    			addMessage(message, Messages.cMsgSWversion, cDummyId3, Constants.USR_CONTROLLER, "", "", context.getString(R.string.telecharger));
    			nb++;
    		}
    	} catch (Exception ex) {
    		Log.e(Constants.TAG, "Messages.checkMessages() - download" + ex.getMessage());
    	}
		try {
			// Support messages
			List<Support> list = dbAdapter.getSupportList();
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					Support msg = list.get(i);
					if (msg.getHidden() == 0) {
						String req = "<b>&#149;  " + context.getString(R.string.support_response) + "</b><br/>";
						req += "<i>" + msg.getQuery() + "</i>";
						req += "<br/><br/><b>&#149;  " + context.getString(R.string.here_is_response) + "</b><br/>";
						req += msg.getMsg();
						Spanned text = Html.fromHtml(req);
						addSpannedMessage(text, msg.getId(), Messages.cMsgSupport, context.getString(R.string.access_tickets));
						nb++;
					}
				}
			}

		} catch (Exception ex) {
			Log.e(Constants.TAG, "Messages.checkMessages() - support message" + ex.getMessage());
		}

    	return nb;
    }
	
	

}

/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6439 $
 $Id: tools.java 6439 2016-06-30 15:44:59Z ede $

 ================================================================================
 */

package com.otipass.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import models.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.otipass.sql.DbAdapter;
import com.otipass.sql.Param;
import com.otipass.sql.Warning;
import com.otipass.sql.Wl;
import com.otipass.mulhouse.R;

public class tools {
	public static final int cOK = 1;
	public static final int cWarning = 2;
	public static final int cError = 3;
	
	public static final int cIdle = 0;
	public static final int cSingleCall = 1;
	public static final int cPeriodicCall = 2;
	public static final int cNightCall = 3;
	public static final int cCommmunicationPending = 4;

    public static final int cNoNFC = 0;
    public static final int cNativeNFC = 1;
    public static final int cExternalReaderNFC = 2;

	private static boolean debug = false;

	public static void debug(Context context, String text) {
		if (debug) {
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static boolean checkMD5(String clearPwd, String salt, String cipheredPwd) {
		String pwd;
		boolean checked = false;
		try {

			pwd = clearPwd + salt;
            MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(pwd.getBytes());
		    final byte[] resultByte = digest.digest();
		    StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < resultByte.length; ++i) {
	          sb.append(Integer.toHexString((resultByte[i] & 0xFF) | 0x100).substring(1,3));
	        }
	        final String result = sb.toString();
	        if (result.equals(cipheredPwd)) {
	        	checked = true;
	        }
		} catch (Exception e) {
			
		}
		return checked;
	}
	
	
	/**
	 * Transform a Uk Date in Fr Format
	 * @param String givenDate
	 * @return String
	 */
	public static String transformDate(String givenDate) {
		String Str_result = givenDate;
		try {
			Date result = new SimpleDateFormat("yyyy-MM-dd").parse(givenDate);
			Str_result = new SimpleDateFormat("dd/MM/yyyy").format(result);
		}catch (java.text.ParseException e) {
			
		}
		return Str_result;
	}
	
    public static String byteArrayToString(byte[] input) {
    	String result = "";    	
        
    	if (input == null) {
    		result = null;
    	} else {
    		for (byte b : input) {
    			result += String.format("%02x", b);
    		}
    		result = result.toUpperCase();
    	}
    	
    	return result;
    }
	

	public static void ExportLogcat(File file) throws InterruptedException, IOException {		
		Process process = null;
		try {
			ProcessBuilder pb = new ProcessBuilder("logcat", "-d", "-v", "time");
			pb.redirectErrorStream(true);
			process = pb.start();
			BufferedReader bufferedReader = new BufferedReader(
												new InputStreamReader(process.getInputStream()), 1024
											);
			
			StringBuilder log = new StringBuilder();
			String line = "";
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line+"\n");
			}
			String data = log.toString();
			FileOutputStream fOut = null;
			OutputStreamWriter osw = null;
	        try{
	        	  fOut = new FileOutputStream(file);
		          osw = new OutputStreamWriter(fOut);

		          osw.write(data);
		          osw.flush();
	         }
	         catch (Exception e) {      
	        	  e.printStackTrace();
	         }
	         finally {
        	 	fOut.close();
                osw.close();
	         }
	        // this erases the logcat
	        process = Runtime.getRuntime().exec("logcat -c");
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static boolean isTablet() {
		String model = android.os.Build.MODEL;
		if (model.equals(Constants.NEXUS_7) || model.equals(Constants.SHIELD)) {
			return true;
		}
		return false;
	}


	public static void showAlert(Context context, String message, int type) {
		Drawable icon;
		String title;
		switch (type) {
		case cWarning:
			icon = ResourcesCompat.getDrawable(context.getResources(), R.mipmap.ic_attention, null);
			title = context.getString(R.string.warning_message_title);
			break;
		case cError:
			icon = ResourcesCompat.getDrawable(context.getResources(), R.mipmap.ic_ko, null);
			title = context.getString(R.string.error_message_title);
			break;
		default:
			icon = ResourcesCompat.getDrawable(context.getResources(), R.mipmap.ic_launcher, null);
			title = context.getString(R.string.Global_information);
			break;
		}
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setIcon(icon);
		alertDialog.setMessage(message);
		alertDialog.setButton(context.getString(R.string.Global_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			} }); 
		alertDialog.show();
	}
	
	public static AlertDialog showWait(Context context) {
		AlertDialog waitDialog = new AlertDialog.Builder(context)
				.setMessage(context.getString(R.string.Global_veuillez_patienter))
				.setCancelable(false)
				.setIcon(R.mipmap.ic_launcher)
				.setTitle(context.getString(R.string.Global_information))
				.show();
		return waitDialog;
	}

	public static void showErrDialog(Context context, String message) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_err);
        TextView msg = (TextView) dialog.findViewById(R.id.dialog_text);
        msg.setText(message);
        TextView title = (TextView) dialog.findViewById(R.id.dialog_title);
        title.setText(R.string.error_message_title);
        Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
	}

	public static String formatNow(String pattern) {
		String now = "";
    	Calendar calendar = Calendar.getInstance(); 
    	try {
	    	SimpleDateFormat format = new SimpleDateFormat(pattern);
	    	now = format.format(calendar.getTime());
    	} catch (Exception e) {
    		//Log.e(Constants.TAG, tools.class.getName() + " - formatNow - " + e.getMessage());
    	}
		return now;
	}

	
	public static String formatSQLDate(Calendar calendar) {
		String date = "";
    	try {
    		date = new SimpleDateFormat(Constants.SQL_FULL_DATE_FORMAT).format(calendar.getTime());	
    	} catch (Exception e) {
    		//Log.e(Constants.TAG, tools.class.getName() + " - formatSQLDate - " + e.getMessage());
    	}
		return date;
	}

	public static String formatSQLShortDate(Calendar calendar) {
		String date = "";
    	try {
    		date = new SimpleDateFormat(Constants.SQL_SHORT_DATE_FORMAT).format(calendar.getTime());	
    	} catch (Exception e) {
    		//Log.e(Constants.TAG, tools.class.getName() + " - formatSQLDate - " + e.getMessage());
    	}
		return date;
	}

	public static String formatShortDate(Calendar calendar) {
		String lang = Locale.getDefault().getLanguage();
		String date = "", format;
    	try {
   			format = Constants.FR_DATE_FORMAT;
    		date = new SimpleDateFormat(format).format(calendar.getTime());	
    	} catch (Exception e) {
    		//Log.e(Constants.TAG, tools.class.getName() + " - formatShortDate - " + e.getMessage());
    	}
		return date;
	}

	public static String formatDate(Calendar calendar) {
		String lang = Locale.getDefault().getLanguage();
		String date = "", format;
		try {
			format = Constants.FULL_DATE_FORMAT_FR;
			date = new SimpleDateFormat(format).format(calendar.getTime());
			if (!date.isEmpty()) {
                // get rid of seconds
                String t[] = date.split(":");
                date = t[0] + ":" + t[1];
            }
		} catch (Exception e) {
			//Log.e(Constants.TAG, tools.class.getName() + " - formatShortDate - " + e.getMessage());
		}
		return date;
	}

	public static String formatDateA(Calendar calendar) {
		String lang = Locale.getDefault().getLanguage();
		String date = "", format;
		try {
			format = Constants.FULL_DATE_FORMAT_FR2;
			date = new SimpleDateFormat(format).format(calendar.getTime());
		} catch (Exception e) {
			//Log.e(Constants.TAG, tools.class.getName() + " - formatShortDate - " + e.getMessage());
		}
		return date;
	}

	public static Calendar setCalendar(String date) {
		Calendar calendar = null;
    	try {
    		calendar = Calendar.getInstance();
    		SimpleDateFormat format = new SimpleDateFormat(Constants.SQL_FULL_DATE_FORMAT); 
    		calendar.setTime(format.parse(date));
    	} catch (Exception e) {
    		//Log.e(Constants.TAG, tools.class.getName() + " - setCalendar - " + e.getMessage());
    	}
		return calendar;
	}
	

	// this gives a reference when defining if a card is expired
	// it returns the current date with time set to 00:00:00
	public static Calendar getNow00() {
		Calendar calendar = null;
    	try {
    		calendar = Calendar.getInstance();
    		calendar.set(Calendar.HOUR_OF_DAY, 1);
    		calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
    		
    	} catch (Exception e) {
    		//Log.e(Constants.TAG, tools.class.getName() + " - setCalendar - " + e.getMessage());
    	}
		return calendar;
	}
	
	// returns the number of steps (x 100 cards) stored in the DB
	// the unused warning table is used to store WL variables
	public static int getNbSteps(DbAdapter dbAdapter) {
		int nbSteps = 0;
		Wl wlState = dbAdapter.getWl(1L);
		if (wlState != null) {
			nbSteps = wlState.getStatus();
		}
		return nbSteps;
	}
	
	public static void setServiceState(Context context, int state) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edt = pref.edit();
		edt.putInt(Constants.SERVICE_KEY, state);
		edt.commit();
	}

	public static int getServiceState(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getInt(Constants.SERVICE_KEY, cIdle); 
	}
	public static void setPeriodicCall(Context context, boolean on) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edt = pref.edit();
		edt.putBoolean(Constants.PERIODIC_CALL, on);
		edt.commit();
	}

	public static boolean getPeriodicCall(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(Constants.PERIODIC_CALL, false); 
	}

	public static void setUserLogged(Context context, int idUser) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edt = pref.edit();
		edt.putInt(Constants.USER_KEY, idUser);
		edt.commit();
	}

	public static int getUserLogged(Context context) {
		int iduser = 0;
		try {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			iduser = pref.getInt(Constants.USER_KEY, 0);
		} catch (Exception ex) {Log.e(Constants.TAG, tools.class.getName() + " - getUserLogged - " + ex.getMessage());}
		return iduser;
	}

    /*
     * Tablet model detection to know NFC behavior
     * Famoco and Nexus do have internal NFC
     * Nvidia SHield has no internal NFC
     */
	public static int getNFCManagement() {
        int nfc = cNativeNFC;
        if (Build.MODEL.equals(Constants.SHIELD)) {
            nfc = cExternalReaderNFC;
        }
        return nfc;
	}
	/*
     * Tablet UID detection
     * Famoco and Nexus do have internal NFC
     * Nvidia SHield has no internal NFC
     */
	public static String getDeviceUID(Context context) {
		String UID = "unknown";
        try {
            if (Build.MODEL.equals(Constants.FAMOCO)) {
                TelephonyManager m_telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                UID = m_telephonyManager.getDeviceId();
            } else {
                UID = android.os.Build.SERIAL;
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, tools.class.getName() + " - getDeviceUID - " + ex.getMessage());
        }
		return UID;
	}

	/*
     * Tablet UID detection
     * Famoco and Nexus do have internal NFC
     * Nvidia SHield has no internal NFC
     */
	public static int getDeviceId(Context context) {
		int id = 0;
		try {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			id = pref.getInt(Constants.DEVICE_ID_KEY, 0);
		} catch (Exception ex) {
			Log.e(Constants.TAG, tools.class.getName() + " - getDeviceId - " + ex.getMessage());
		}
		return id;
	}
	public static void setDeviceId(Context context, int id) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edt = pref.edit();
		edt.putInt(Constants.DEVICE_ID_KEY, id);
		edt.commit();
	}

    public static String formatCipherSerial(String serialDecimal) {
        String cipheredSerial = "";
        try {
            MessageDigest digest = MessageDigest.getInstance(Constants.CIPHER_ALGO);
            digest.update(serialDecimal.getBytes());
            StringBuffer sb = new StringBuffer();
            final byte[] resultByte = digest.digest();
            for (int i = 0; i < resultByte.length; ++i) {
                sb.append(Integer.toHexString((resultByte[i] & 0xFF) | 0x100).substring(1, 3));
            }
			cipheredSerial = sb.toString();
        } catch (Exception ex) {
            Log.e(Constants.TAG, tools.class.getName() + " - formatCipherSerial -" + ex.getMessage());
        }
        return cipheredSerial;
    }

	// format the serial number as it appears on the card
	public static String formatSerial(String serial) {
		String fSerial = "";
        String format = Constants.SERIAL_FORMAT;
		int countd = format.length() - format.replace("d", "").length();
		if (countd == serial.length()) {
			for(int i=0, j=0; i<format.length(); i++, j++) {
				if (format.charAt(i) == ' ') {
                    j--;
                    fSerial += ' ';
                } else {
                    fSerial += serial.charAt(j);
                }
			}
		} else {
			fSerial = serial;
		}

		return fSerial;
	}
    private static final int sizeOfIntInHalfBytes = 8;
    private static final int numberOfBitsInAHalfByte = 4;
    private static final int halfByte = 0x0F;
    private static final char[] hexDigits = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String decToHex(long dec) {
        StringBuilder hexBuilder = new StringBuilder(sizeOfIntInHalfBytes);
        hexBuilder.setLength(sizeOfIntInHalfBytes);
        for (int i = sizeOfIntInHalfBytes - 1; i >= 0; --i)
        {
            int j = (int) (dec & halfByte);
            hexBuilder.setCharAt(i, hexDigits[j]);
            dec >>= numberOfBitsInAHalfByte;
        }
        return hexBuilder.toString();
    }
	/*
     * RFID Lock
     * keep track of a started transaction
     */
	public static boolean getRFIDLock(Context context) {
		boolean locked = false;
		try {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			locked = pref.getBoolean(Constants.RFID_LOCK_KEY, false);
		} catch (Exception ex) {
			Log.e(Constants.TAG, tools.class.getName() + " - getRFIDLock - " + ex.getMessage());
		}
		return locked;
	}
	public static void setRFIDLock(Context context, boolean val) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor edt = pref.edit();
		edt.putBoolean(Constants.RFID_LOCK_KEY, val);
		edt.commit();
	}

}

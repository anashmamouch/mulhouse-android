/**
================================================================================

    OTIPASS
    sql package

    @author ED ($Author: ede $)

    @version $Rev: 6450 $
    $Id: DbAdapter.java 6450 2016-07-06 09:43:10Z ede $

================================================================================
 */
package com.otipass.sql;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Constants;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.otipass.tools.LastEntry;
import com.otipass.tools.PersonalInfo;
import com.otipass.tools.tools;

public class DbAdapter {
	private static final String TAG = Constants.TAG;
	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allOtipassColumns = { 
			SQLiteHelper.OTI_COL_NUMOTIPASS,
			SQLiteHelper.OTI_COL_SERIAL,
			SQLiteHelper.OTI_COL_STATUS,
			SQLiteHelper.OTI_COL_EXPIRY,
			SQLiteHelper.OTI_COL_TYPE,
			SQLiteHelper.OTI_COL_SERVICE,
			SQLiteHelper.OTI_COL_PID
	};
	private String[] allUserColumns = { 
			SQLiteHelper.USER_COL_ID,
			SQLiteHelper.USER_COL_USERID,
			SQLiteHelper.USER_COL_PWD,
			SQLiteHelper.USER_COL_SALT,
			SQLiteHelper.USER_COL_PROFILE,
	};
	private String[] allEntryColumns = { 
			SQLiteHelper.ENTRY_COL_ID,
			SQLiteHelper.ENTRY_COL_DATE,
			SQLiteHelper.ENTRY_COL_OTIPASS,
			SQLiteHelper.ENTRY_COL_NB,
			SQLiteHelper.ENTRY_COL_EVENT,
			SQLiteHelper.ENTRY_COL_UPLOADED,
			SQLiteHelper.ENTRY_COL_SERVICE,
	};
	private String[] allParamColumns = { 
			SQLiteHelper.PARAM_COL_ID,
			SQLiteHelper.PARAM_COL_NAME,
			SQLiteHelper.PARAM_COL_CALL,
			SQLiteHelper.PARAM_COL_SOFT,
			SQLiteHelper.PARAM_COL_CATEGORY
	};
	private String[] allWarningColumns = { 
			SQLiteHelper.WARNING_COL_ID,
			SQLiteHelper.WARNING_COL_DATE,
			SQLiteHelper.WARNING_COL_SERIAL,
			SQLiteHelper.WARNING_COL_EVENT,
	};
	private String[] allUpdateColumns = { 
			SQLiteHelper.UPDATE_COL_ID,
			SQLiteHelper.UPDATE_COL_DATE,
			SQLiteHelper.UPDATE_COL_TYPE,
			SQLiteHelper.UPDATE_COL_NUMOTIPASS,
			SQLiteHelper.UPDATE_COL_PID,
			SQLiteHelper.UPDATE_COL_NAME,
			SQLiteHelper.UPDATE_COL_FNAME,
			SQLiteHelper.UPDATE_COL_COUNTRY,
			SQLiteHelper.UPDATE_COL_EMAIL,
			SQLiteHelper.UPDATE_COL_POSTAL_CODE,
			SQLiteHelper.UPDATE_COL_NEWSLETTER,
			SQLiteHelper.UPDATE_COL_UPLOADED,
	};
	private String[] allTabletColumns = { 
			SQLiteHelper.TABLET_COL_ID,
			SQLiteHelper.TABLET_COL_NUMSEQUENCE,
			SQLiteHelper.TABLET_COL_UPLOAD_TIME,
			SQLiteHelper.TABLET_COL_DOWNLOAD_TIME,
	};

	private String[] allStockColumns = { 
			SQLiteHelper.STOCK_COL_ID,
			SQLiteHelper.STOCK_COL_PROVIDER_ID,
			SQLiteHelper.STOCK_COL_NB_CARDS,
			SQLiteHelper.STOCK_COL_THRESHOLD,
			SQLiteHelper.STOCK_COL_ALERT,
	};

	private String[] allMessageColumns = { 
			SQLiteHelper.MSG_COL_ID,
			SQLiteHelper.MSG_COL_TEXT,
			SQLiteHelper.MSG_COL_LANG,
			SQLiteHelper.MSG_COL_START_DATE,
			SQLiteHelper.MSG_COL_END_DATE,
			SQLiteHelper.MSG_COL_HIDDEN,
	};

	private String[] allSupportColumns = {
			SQLiteHelper.SUPPORT_COL_ID,
			SQLiteHelper.SUPPORT_COL_TEXT,
			SQLiteHelper.SUPPORT_COL_DATE,
			SQLiteHelper.SUPPORT_COL_EVENT,
			SQLiteHelper.SUPPORT_COL_PARENT,
			SQLiteHelper.SUPPORT_COL_QUERY,
			SQLiteHelper.SUPPORT_COL_HIDDEN,
	};

	private String[] allPackageColumns = { 
			SQLiteHelper.PACKAGE_COL_ID,
			SQLiteHelper.PACKAGE_COL_NAME,
			SQLiteHelper.PACKAGE_COL_DURATION,
			SQLiteHelper.PACKAGE_COL_PERIOD,
			SQLiteHelper.PACKAGE_COL_PRICE,
			SQLiteHelper.PACKAGE_COL_REF,
	};
	
	private String[] allWlColumns = { 
			SQLiteHelper.WL_COL_ID,
			SQLiteHelper.WL_COL_DATE,
			SQLiteHelper.WL_COL_NBSTEPS,
			SQLiteHelper.WL_COL_NBCARDS,
			SQLiteHelper.WL_COL_NUMSEQUENCE,
			SQLiteHelper.WL_COL_STATUS,
	};

	private String[] allBugColumns = {
			SQLiteHelper.BUG_REPORT_COL_ID,
			SQLiteHelper.BUG_REPORT_COL_TEXT,
			SQLiteHelper.BUG_REPORT_COL_DATE,
	};

	private String[] allServiceColumns = {
			SQLiteHelper.SERVICE_COL_ID,
			SQLiteHelper.SERVICE_COL_TYPE,
			SQLiteHelper.SERVICE_COL_NAME,
	};

	public static final int cSaveOK = 0;
	public static final int cSaveKO = 1;
	public static final int cOtipassKO = 2;
	public static final int cUpdateOtipassFailed = 3;

	public DbAdapter(Context context) {
		dbHelper = SQLiteHelper.getInstance(context);
	}


	public void open() throws SQLException {
		try {
			database = dbHelper.getWritableDatabase();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - open - " + e.getMessage());
		}
	}

	public void close() {
		dbHelper.close();
	}

	public void deleteDB(Context context) throws SQLException {
		context.deleteDatabase(SQLiteHelper.DATABASE_NAME);
	}

	public String getDBPath(Context context) {
		File dbFile = context.getDatabasePath(SQLiteHelper.DATABASE_NAME);
		String s = dbFile.getAbsolutePath();
		return s;
	}

	public Otipass insertOtipass(long numOtipass, String serial, short status, String expiry, short type) {
		Otipass otipass = null;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.OTI_COL_NUMOTIPASS, numOtipass);
			values.put(SQLiteHelper.OTI_COL_SERIAL, serial);
			values.put(SQLiteHelper.OTI_COL_STATUS, status);
			values.put(SQLiteHelper.OTI_COL_EXPIRY, expiry);
			values.put(SQLiteHelper.OTI_COL_TYPE, type);
			long id = database.insert(SQLiteHelper.OTIPASS_TABLE, null, values);
			if (id > 0) {
				otipass =  getOtipass(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertOtipass - " + e.getMessage());
		}
		return otipass;
	}

	public Otipass insertOtipassObject(Otipass otipass) {
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.OTI_COL_NUMOTIPASS, otipass.getNumOtipass());
			values.put(SQLiteHelper.OTI_COL_SERIAL, otipass.getSerial());
			values.put(SQLiteHelper.OTI_COL_STATUS, otipass.getStatus());
			values.put(SQLiteHelper.OTI_COL_EXPIRY, otipass.getExpiry());
			values.put(SQLiteHelper.OTI_COL_TYPE, otipass.getType());
			values.put(SQLiteHelper.OTI_COL_PID, otipass.getPid());
			values.put(SQLiteHelper.OTI_COL_SERVICE, otipass.getService());
			long id = database.insert(SQLiteHelper.OTIPASS_TABLE, null, values);
			if (id > 0) {
				otipass =  getOtipass(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertOtipassObject - " + e.getMessage());
		}
		return otipass;
	}

	/**
	 * insert usage 
	 * @param usage
	 * @return
	 */
	public long insertUsage(Usage usage) {
		long id = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.USE_PASS_COL_USAGE_OTIPASS, usage.getNumOtipass());
			values.put(SQLiteHelper.USE_PASS_COL_USAGE_DATE, usage.getDate());
			id = database.insert(SQLiteHelper.USE_PASS_TABLE, null, values);

		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertUsage - " + e.getMessage());
		}
		return id;
	}
	
	public int insertUsageList(List<Usage> usageList) {
		Usage usage = null;
		long id;
		int i = 0;
		database.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<usageList.size(); i++) {
				usage = usageList.get(i);
				values.clear();
				values.put(SQLiteHelper.USE_PASS_COL_USAGE_OTIPASS, usage.getNumOtipass());
				values.put(SQLiteHelper.USE_PASS_COL_USAGE_DATE, usage.getDate());
				id = database.insert(SQLiteHelper.USE_PASS_TABLE, null, values);
				if (id < 1L) {
					Log.e(TAG, DbAdapter.class.getName() + " - insertUsageList - Cannot insert Otipass " + usage.getNumOtipass());
					break;
				}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertUsageList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}
	

	/**
	 * get last day of pass'usage
	 * @param numotipass
	 * @return
	 */
	public String getLastDayUsage(int numotipass){

		String date = null;
		Cursor dCursor = null;
		

		final String MY_QUERY = "SELECT * FROM " + SQLiteHelper.USE_PASS_TABLE + " WHERE " + SQLiteHelper.USE_PASS_COL_USAGE_OTIPASS + " = " + numotipass + " ORDER BY " + SQLiteHelper.USE_PASS_COL_USAGE_ID + " DESC LIMIT 1";

		dCursor = database.rawQuery(MY_QUERY, null);

		if (dCursor != null) {
			if (dCursor.moveToFirst()) {
				do {
					date = dCursor.getString(dCursor.getColumnIndex(SQLiteHelper.USE_PASS_COL_USAGE_DATE));

				} while (dCursor.moveToNext());
			}
		}
		if (dCursor != null) {
			dCursor.close();
		}
		return date;

	}

	public int insertOtipassList(List<Otipass> otipassList) {
		Otipass otipass = null;
		long id;
		int i = 0;
		database.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<otipassList.size(); i++) {
				otipass = otipassList.get(i);
				values.clear();
				values.put(SQLiteHelper.OTI_COL_NUMOTIPASS, otipass.getNumOtipass());
				values.put(SQLiteHelper.OTI_COL_SERIAL, otipass.getSerial());
				values.put(SQLiteHelper.OTI_COL_STATUS, otipass.getStatus());
				values.put(SQLiteHelper.OTI_COL_EXPIRY, otipass.getExpiry());
				values.put(SQLiteHelper.OTI_COL_TYPE, otipass.getType());
				values.put(SQLiteHelper.OTI_COL_PID, otipass.getPid());
				values.put(SQLiteHelper.OTI_COL_SERVICE, otipass.getService());
				id = database.insert(SQLiteHelper.OTIPASS_TABLE, null, values);
				if (id < 1L) {
					Log.e(TAG, DbAdapter.class.getName() + " - insertOtipassList - Cannot insert Otipass " + otipass.getNumOtipass());
					break;
				}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertOtipassList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}

	public int createOtipassList(List<Create> createList) {
		Create create;
		long id;
		int i = 0;
		Otipass otipass;
		database.beginTransaction();
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<createList.size(); i++) {
				create = createList.get(i);
				otipass = getOtipass(create.getNumotipass());
				if (otipass  == null) {
					// normal Pass insertion 
					values.clear();
					values.put(SQLiteHelper.OTI_COL_NUMOTIPASS, create.getNumotipass());
					values.put(SQLiteHelper.OTI_COL_SERIAL, create.getSerial());
					values.put(SQLiteHelper.OTI_COL_STATUS, create.getStatus());
					values.put(SQLiteHelper.OTI_COL_TYPE, create.getType());
					values.put(SQLiteHelper.OTI_COL_PID, create.getPid());
					values.put(SQLiteHelper.OTI_COL_SERVICE, create.getService());
					values.put(SQLiteHelper.OTI_COL_EXPIRY, create.getExpiry());
					id = database.insert(SQLiteHelper.OTIPASS_TABLE, null, values);
					if (id < 1L) {
						Log.e(TAG, DbAdapter.class.getName() + " - createOtipassList - Cannot insert Otipass " + create.getNumotipass());
					}
				} else {
					// this otipass already exists, update it
					otipass.setStatus(create.getStatus());
					otipass.setType(create.getType());
					int nb = updateOtipass(otipass);
					if (nb != 1) {
						Log.d(TAG, DbAdapter.class.getName() + " - createOtipassList - cannot update Otipass:" + create.getNumotipass());
					}
				}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - createOtipassList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}


	public int deleteOtipassList(List<Otipass> otipassList) {
		Otipass otipass = null;
		int i = 0;
		try {
			for (i=0; i<otipassList.size(); i++) {
				otipass = otipassList.get(i);
				database.delete(SQLiteHelper.OTIPASS_TABLE, SQLiteHelper.OTI_COL_NUMOTIPASS + " = ?", new String[] { String.valueOf(otipass.getNumOtipass()) });
			}
		} catch (Exception e) {
		}
		return i;
	}


	public int updateOtipassList(List<Partial> updateList) {
		Partial update = null;
		Otipass otipass;
		int i = 0;
		database.beginTransaction();
		try {
			for (i=0; i<updateList.size(); i++) {
				update = updateList.get(i);
				otipass = getOtipass(update.getNumotipass());
				if (otipass != null) {
					otipass.setStatus((short)update.getStatus());
					if (update.getExpiry() != "") {
						otipass.setExpiry(update.getExpiry());
					}
					if (update.getPid() != -1) {
						otipass.setPid(update.getPid());
					}
					
					if (updateOtipass(otipass) != 1) {
						Log.d(TAG, DbAdapter.class.getName() + " - updateOtipassList - cannot update Otipass:" + update.getNumotipass());
					}
				} else {
					Log.d(TAG, DbAdapter.class.getName() + " - updateOtipassList - cannot get Otipass:" + update.getNumotipass());
				}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateOtipassList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}

	/**
	 * update otipass service
	 * @param updateList
	 * @return
	 */

	public int updateOtipassSrvList(List<PartialServiceCpt> updateList) {
		PartialServiceCpt update = null;
		Otipass otipass;
		String service = "";
		int i = 0;
		database.beginTransaction();
		try {
			for (i=0; i<updateList.size(); i++) {
				service = "";
				update = updateList.get(i);
				otipass = getOtipass(update.getNumOtipass());
				if (otipass != null) {
					String [] srv = update.getSrv().split(";");
					if (srv.length > 1) {
						otipass.setService(update.getSrv());
					}else {
						String [] srv_cpt = srv[0].split(":");
						String [] srv_ot = otipass.getService().split(";");

						for (int j = 0; j < srv_ot.length ; j++) {
							String [] serviceToUpdate = srv_ot[j].split(":");
							if (serviceToUpdate[0].equals(srv_cpt[0])) {
								serviceToUpdate[1] = srv_cpt[1];
							}
							service = service.concat(serviceToUpdate[0].toString().concat(":" + serviceToUpdate[1].toString() + ";"));
						}
						otipass.setService(service);
						Log.i(Constants.TAG, "set service:"+service);
					}

					if (updateOtipass(otipass) != 1) {
						Log.d(TAG, DbAdapter.class.getName() + " - updateOtipassSrvList - cannot update Otipass:" + update.getNumOtipass());
					}
				} else {
					Log.d(TAG, DbAdapter.class.getName() + " - updateOtipassSrvList - cannot get Otipass:" + update.getNumOtipass());
				}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateOtipassSrvList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}

	public int deleteOtipass(long numOtipass) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.OTIPASS_TABLE, SQLiteHelper.OTI_COL_NUMOTIPASS + " = ?", new String[]{String.valueOf(numOtipass)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteOtipass - " + e.getMessage());
		}
		return nbRows;
	}

	public int updateOtipass(Otipass otipass) {
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.OTI_COL_NUMOTIPASS, otipass.getNumOtipass());
			values.put(SQLiteHelper.OTI_COL_SERIAL, otipass.getSerial());
			values.put(SQLiteHelper.OTI_COL_STATUS, otipass.getStatus());
			values.put(SQLiteHelper.OTI_COL_EXPIRY, otipass.getExpiry());
			values.put(SQLiteHelper.OTI_COL_PID, otipass.getPid());
			values.put(SQLiteHelper.OTI_COL_TYPE, otipass.getType());
			Log.d("service", otipass.getService());
			values.put(SQLiteHelper.OTI_COL_SERVICE, otipass.getService());
			nbRows = database.update(SQLiteHelper.OTIPASS_TABLE, values, SQLiteHelper.OTI_COL_NUMOTIPASS + " = ?", new String[]{String.valueOf(otipass.getNumOtipass())});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateOtipass - " + e.getMessage());
		}
		return nbRows;
	}

	public int updateOtipassStatus(int status, int numotipass){
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.OTI_COL_STATUS, status);
			nbRows = database.update(SQLiteHelper.OTIPASS_TABLE, values, SQLiteHelper.OTI_COL_NUMOTIPASS + " = ?", new String[]{String.valueOf(numotipass)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateOtipassStatus - " + e.getMessage());
		}
		return nbRows;
	}

	/**
	 * updates otipass service
	 * @param service
	 * @param numotipass
	 * @return
	 */
	public int updateOtipassService(String service, int numotipass){
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.OTI_COL_SERVICE, service);
			nbRows = database.update(SQLiteHelper.OTIPASS_TABLE, values, SQLiteHelper.OTI_COL_NUMOTIPASS + " = ?", new String[] { String.valueOf(numotipass) });
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateOtipassService - " + e.getMessage());
		}
		return nbRows;
	}

	/**
	 * updates otipass when the sale is done
	 * @param service
	 * @param status
	 * @param pid
	 * @param numotipass
	 * @return
	 */
	public int updateOtipassAfterSale(String service, int status, int pid, int numotipass){
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.OTI_COL_PID, pid);
			values.put(SQLiteHelper.OTI_COL_STATUS, status);
			values.put(SQLiteHelper.OTI_COL_SERVICE, service);
			nbRows = database.update(SQLiteHelper.OTIPASS_TABLE, values, SQLiteHelper.OTI_COL_NUMOTIPASS + " = ?", new String[] { String.valueOf(numotipass) });
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateOtipassAfterSale - " + e.getMessage());
		}
		return nbRows;
	}

	/**
	 * updates expriy date
	 * @param date
	 * @param numotipass
	 * @return
	 */
	public int updateOtipassExpiryDate(String date, int numotipass){
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.OTI_COL_EXPIRY, date);
			nbRows = database.update(SQLiteHelper.OTIPASS_TABLE, values, SQLiteHelper.OTI_COL_NUMOTIPASS + " = ?", new String[] { String.valueOf(numotipass) });
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateOtipassExpiryDate - " + e.getMessage());
		}
		return nbRows;
	}

	public Otipass getOtipass(long numOtipass) {
		Otipass otipass = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.OTIPASS_TABLE,
					allOtipassColumns, SQLiteHelper.OTI_COL_NUMOTIPASS + " = " + numOtipass, null, null, null, null);
			if (cursor.moveToFirst()) {
				otipass = new Otipass();
				otipass.setNumOtipass(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.OTI_COL_NUMOTIPASS)));
				otipass.setSerial(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERIAL)));
				otipass.setStatus(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.OTI_COL_STATUS)));
				otipass.setExpiry(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_EXPIRY)));
				otipass.setType(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.OTI_COL_TYPE)));
				otipass.setPid(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.OTI_COL_PID)));
				otipass.setService(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERVICE)));
			}
			if (cursor != null) { 
				cursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getOtipass - " + e.getMessage());
		}
		return otipass;
	}

	public Otipass getOtipassBySerial(String serial) {
		Otipass otipass = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.OTIPASS_TABLE,
					allOtipassColumns, SQLiteHelper.OTI_COL_SERIAL + " = '" + serial + "'", null, null, null, null);
			if (cursor.moveToFirst()) {
				otipass = new Otipass();
                otipass.setNumOtipass(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.OTI_COL_NUMOTIPASS)));
                otipass.setSerial(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERIAL)));
                otipass.setStatus(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.OTI_COL_STATUS)));
                otipass.setExpiry(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_EXPIRY)));
                otipass.setType(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.OTI_COL_TYPE)));
                otipass.setPid(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.OTI_COL_PID)));
                otipass.setService(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERVICE)));
            }
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getOtipassBySerial - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return otipass;
	}

	/**
	 * returns package by package's id
	 * @param idPackage
	 * @return
	 */
	public PackageObject getPackageById(int idPackage){
		PackageObject po = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.PACKAGE_TABLE,
					allPackageColumns, SQLiteHelper.PACKAGE_COL_ID + " = '" + idPackage + "'", null, null, null, null);
			if (cursor.moveToFirst()) { 
				do { 
					po = new PackageObject();
					po.setId(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_ID)));
					po.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_NAME)));
					po.setDuration(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_DURATION)));
					po.setPeriod(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PERIOD)));
					po.setPrice(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PRICE)));

				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getPackageById - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return po;
	}

	/**
	 * returns package by package's name
	 * @param package_name
	 * @return
	 */
	public PackageObject getPackageByName(String package_name){
		PackageObject po = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.PACKAGE_TABLE,
					allPackageColumns, SQLiteHelper.PACKAGE_COL_NAME + " = '" + package_name + "'", null, null, null, null);
			if (cursor.moveToFirst()) { 
				do { 
					po = new PackageObject();
					po.setId(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_ID)));
					po.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_NAME)));
					po.setDuration(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_DURATION)));
					po.setPeriod(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PERIOD)));
					po.setPrice(cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PRICE)));

				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getPackageByName - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return po;
	}

	/**
	 * returns package's name by package's id
	 * @param package_id
	 * @return
	 */
	public String getPackageNameById(int package_id){
		String package_name = null;
		Cursor cursor = null;
		
		try {
			String [] columns = {SQLiteHelper.PACKAGE_COL_NAME};
			cursor = database.query(SQLiteHelper.PACKAGE_TABLE,
                    columns, SQLiteHelper.PACKAGE_COL_ID + " = '" + package_id + "'", null, null, null, null);
			if (cursor.moveToFirst()) { 
				do { 
					package_name = cursor.getString(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_NAME));
				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getPackageNameById - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return package_name;
	}

	/**
	 * returns service's id by service's name
	 * @param service_name
	 * @return
	 */
	public int getServiceIdByName(String service_name){
		int idservice = 0;
		Cursor dCursor = null;
		
		try {

			dCursor = database.rawQuery(
					"SELECT " + SQLiteHelper.SERVICE_COL_ID + " FROM " + SQLiteHelper.SERVICE_TABLE + " WHERE " + SQLiteHelper.SERVICE_COL_NAME + " = ? "
					, new String[]{service_name});
			if (dCursor != null) {
				if (dCursor.moveToFirst()) {
					do {
						idservice = dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.SERVICE_COL_ID));

					} while (dCursor.moveToNext());
				}
				dCursor.close();
			}

		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getServiceIdByName - " + e.getMessage());
		}
		return idservice;
	}

	/**
	 * get service name by service id
	 * @param idservice
	 * @return
	 */
	public String getServiceNameById(int idservice){
		String service_name = null;
		Cursor cursor = null;
		
		try {
			String [] columns = {SQLiteHelper.SERVICE_COL_NAME};
			cursor = database.query(SQLiteHelper.SERVICE_TABLE,
					columns, SQLiteHelper.SERVICE_COL_ID + " = '" + idservice + "'", null, null, null, null);
			if (cursor.moveToFirst()) { 
				do { 
					service_name = cursor.getString(cursor.getColumnIndex(SQLiteHelper.SERVICE_COL_NAME));
				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getServiceNameById - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return service_name;
	}

	/**
	 * get package's price by idpackage
	 * @param idpackage
	 * @return
	 */
	public double getPriceByIdPackage(int idpackage){
		double price = 0;
		Cursor cursor = null;
		
		try {
			String [] columns = {SQLiteHelper.PACKAGE_COL_PRICE};
			cursor = database.query(SQLiteHelper.PACKAGE_TABLE,
                    columns, SQLiteHelper.PACKAGE_COL_ID + " = '" + idpackage + "'", null, null, null, null);
			if (cursor.moveToFirst()) { 
				do { 
					price = cursor.getDouble(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PRICE));
				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getServiceNameById - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return price;
	}

	public User getUser(long id) {
		User user = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.USER_TABLE,
					allUserColumns, SQLiteHelper.USER_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				user = new User();
				user.setId(cursor.getLong(0));
				user.setUserid(cursor.getString(1));
				user.setPassword(cursor.getString(2));
				user.setSalt(cursor.getString(3));
				user.setProfile(cursor.getShort(4));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getUser - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return user;
	}

	public User getUserByLogin(String login) {
		User user = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.USER_TABLE,
					allUserColumns, SQLiteHelper.USER_COL_USERID + " = '" + login + "'", null, null, null, null);
			if (cursor.moveToFirst()) {
				user = new User();
				user.setId(cursor.getLong(0));
				user.setUserid(cursor.getString(1));
				user.setPassword(cursor.getString(2));
				user.setSalt(cursor.getString(3));
				user.setProfile(cursor.getShort(4));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getUserByLogin - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return user;
	}

	public User insertUser(int idUser, String userid, String password, String salt, short profile) {
		User user = null;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.USER_COL_ID, idUser);
			values.put(SQLiteHelper.USER_COL_USERID, userid);
			values.put(SQLiteHelper.USER_COL_PWD, password);
			values.put(SQLiteHelper.USER_COL_SALT, salt);
			values.put(SQLiteHelper.USER_COL_PROFILE, profile);
			long id = database.insert(SQLiteHelper.USER_TABLE, null, values);
			if (id > 0) {
				return getUser(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertUser - " + e.getMessage());
		}
		return user;
	}

	public int insertUserList(List<User> userList) {
		User user = null;
		long id;
		int i = 0;
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<userList.size(); i++) {
				user = userList.get(i);
				values.clear();
				values.put(SQLiteHelper.USER_COL_ID, user.getId());
				values.put(SQLiteHelper.USER_COL_USERID, user.getUserid());
				values.put(SQLiteHelper.USER_COL_PWD, user.getPassword());
				values.put(SQLiteHelper.USER_COL_SALT, user.getSalt());
				values.put(SQLiteHelper.USER_COL_PROFILE, user.getProfile());
				id = database.insert(SQLiteHelper.USER_TABLE, null, values);
				if (id < 1L) {
					Log.e(TAG, DbAdapter.class.getName() + " - insertUserList - Cannot insert user" + user.getUserid());
					break;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertUserList - " + e.getMessage());
		}
		return i;
	}

	// Getting all the entries which have the flag uploaded = false;
	public List<Entry> getUnloadedEntries() { 
		List<Entry> entryList = new ArrayList<Entry>(); 
		Cursor cursor = null;
		
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.ENTRY_TABLE + " WHERE " + SQLiteHelper.ENTRY_COL_UPLOADED + " = 0"; 
			cursor = database.rawQuery(selectQuery, null); 
			if (cursor.moveToFirst()) { 
				do { 
					Entry entry = new Entry(); 
					entry.setId(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_ID)));
					entry.setDate(cursor.getString(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_DATE)));
					entry.setNumotipass((int)cursor.getLong(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_OTIPASS)));
					entry.setNb(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_NB)));
					entry.setEvent(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_EVENT)));
					entry.setUploaded(false);
					entry.setService(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_SERVICE)));
					entryList.add(entry);
				} while (cursor.moveToNext()); 
			} 
			cursor.close();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getUnloadedEntries - " + e.getMessage());
		}
		return entryList; 
	} 

	public Entry getEntry(long id) {
		Entry entry = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.ENTRY_TABLE,
                    allEntryColumns, SQLiteHelper.ENTRY_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				entry = new Entry();
				entry.setId(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_ID)));
				entry.setDate(cursor.getString(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_DATE)));
				entry.setNumotipass((int)cursor.getLong(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_OTIPASS)));
				entry.setNb(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_NB)));
				entry.setEvent(cursor.getShort(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_EVENT)));
				boolean uploaded = (cursor.getShort(5) == 1) ? true : false;
				entry.setUploaded(uploaded);
				entry.setService(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.ENTRY_COL_SERVICE)));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getEntry - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return entry;
	}

	/**
	 * get entries for pass when date is today
	 * @param numotipass
	 * @param today
	 * @return
	 */
	public int getEntryByOtipassAndDay(int numotipass, String today)
	{
		String[] args = {String.valueOf(numotipass) , '%' + today + '%'};
		Cursor cursor = null;
		int nb = 0;
		try
		{
			cursor = database.query(
                    SQLiteHelper.ENTRY_TABLE,
                    allEntryColumns,
                    SQLiteHelper.ENTRY_COL_OTIPASS + " = ? AND " + SQLiteHelper.ENTRY_COL_DATE + " LIKE ?",
                    args,
                    null,
                    null,
                    null
            );
			nb =  cursor.getCount();
		}
		catch (Exception e)
		{
			Log.e(TAG, DbAdapter.class.getName() + " - getEntryByOtipassAndDay - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}

		return nb;
	}

	/**
	 * check whether service exists
	 * @param idService
	 * @return
	 */
	public int isServiceExists(int idService){
		Cursor cursor = null;
		int exist = 0;
		try{
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.SERVICE_TABLE + " WHERE " + SQLiteHelper.SERVICE_COL_ID + " = " + idService;
			cursor = database.rawQuery(selectQuery, null); 
			exist =  cursor.getCount();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - isServiceExists - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return exist;
	}

	/**
	 * check whether otipass exists in use_pass table
	 * @param idotipass
	 * @return
	 */
	public int isOtipassExists(int numotipass){
		Cursor cursor = null;
		int exist = 0;

		try{
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.USE_PASS_TABLE + " WHERE " + SQLiteHelper.USE_PASS_COL_USAGE_OTIPASS + " = " + numotipass;
			cursor = database.rawQuery(selectQuery, null); 
			exist = cursor.getCount();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - isOtipassExists - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return exist;
	}

	/**
	 * check whether provider has services
	 * @return
	 */
	public int providerHasServices(){
		Cursor cursor = null;
		int nb = 0;

		try{
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.PROVIDER_SERVICE_TABLE;
			cursor = database.rawQuery(selectQuery, null); 
			nb =  cursor.getCount();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - providerHasServices - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return nb;
	}

	/**
	 * check whether package service exists
	 * @param idPackageService
	 * @param idService
	 * @return
	 */
	public int isPackageServiceExists(int idpackage, int idService){
		Cursor cursor = null;
		int exist = 0;

		try{
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.PACKAGE_SERVICE_TABLE + " WHERE " + SQLiteHelper.PACKAGE_SERVICE_COL_SERVICE_ID + " = " + idService + "  AND " + SQLiteHelper.PACKAGE_SERVICE_COL_PACKAGE_ID + " = " + idpackage;
			cursor = database.rawQuery(selectQuery, null); 
			exist =  cursor.getCount();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - isPackageServiceExists - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return exist;
	}

	/**
	 * check whether use pass exists in use_pass table
	 * @param numotipass
	 * @param date
	 * @return
	 */
	public int isUsePassExists(int numotipass, String date){
		Cursor cursor = null;
		int exist = 0;

		try{
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.USE_PASS_TABLE + " WHERE " + SQLiteHelper.USE_PASS_COL_USAGE_OTIPASS + " = " + numotipass + "  AND " + SQLiteHelper.USE_PASS_COL_USAGE_DATE + " = " + date;
			cursor = database.rawQuery(selectQuery, null); 
			exist =  cursor.getCount();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - isUsePassExists - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return exist;
	}

	public int getEntryByOtipassAndEvent(int numotipass, int event, String today)
	{
		Cursor cursor = null;
		int exist = 0;
		
		String[] args = {String.valueOf(numotipass) , String.valueOf(event), '%' + today + '%'};
		try
		{
			cursor = database.query(
					SQLiteHelper.ENTRY_TABLE,
					allEntryColumns,
					SQLiteHelper.ENTRY_COL_OTIPASS + " = ? AND " + SQLiteHelper.ENTRY_COL_EVENT + " = ? AND " + SQLiteHelper.ENTRY_COL_DATE + " LIKE ?",
					args,
					null,
					null,
					null
			);
			exist =  cursor.getCount();
		}
		catch (Exception e)
		{
			Log.e(TAG, DbAdapter.class.getName() + " - getEntryByOtipassAndEvent - " + e.getMessage());
		}
		if (cursor != null) { 
			cursor.close();
		}
		return exist;

	}

	public int deleteOldEntries(String today) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.ENTRY_TABLE, SQLiteHelper.ENTRY_COL_UPLOADED + " = ? AND " + SQLiteHelper.ENTRY_COL_DATE + " NOT LIKE ?", new String[]{"1", '%' + today + '%'});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteOldEntries - " + e.getMessage());
		}
		return nbRows;
	}


	public int updateEntry(Entry entry) {
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.ENTRY_COL_DATE, entry.getDate());
			values.put(SQLiteHelper.ENTRY_COL_OTIPASS, entry.getNumotipass());
			values.put(SQLiteHelper.ENTRY_COL_NB, entry.getNb());
			values.put(SQLiteHelper.ENTRY_COL_EVENT, entry.getEvent());
			values.put(SQLiteHelper.ENTRY_COL_UPLOADED, entry.getUploaded());
			values.put(SQLiteHelper.ENTRY_COL_SERVICE, entry.getService());
			nbRows = database.update(SQLiteHelper.ENTRY_TABLE, values, SQLiteHelper.ENTRY_COL_ID + " = ?", new String[]{String.valueOf(entry.getId())});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateEntry - " + e.getMessage());
		}
		return nbRows;
	}

	/**
	 * get services from service table where package_id passed as parameter 
	 * @param idpackage
	 * @return
	 */
	public ArrayList<ServicePass> getServicesByPackageId(int idpackage) {

		ArrayList<ServicePass> serviceList = new ArrayList<ServicePass>();
		ServicePass sp ; 

		final String MY_QUERY = "SELECT s." + SQLiteHelper.SERVICE_COL_NAME + ", s." + SQLiteHelper.SERVICE_COL_ID + ", s." + SQLiteHelper.SERVICE_COL_TYPE 
				+ " FROM " + SQLiteHelper.SERVICE_TABLE 
				+ " s INNER JOIN " + SQLiteHelper.PACKAGE_SERVICE_TABLE 
				+ " ps ON s." + SQLiteHelper.SERVICE_COL_ID + " = ps." + SQLiteHelper.PACKAGE_SERVICE_COL_SERVICE_ID 
				+ " INNER JOIN " + SQLiteHelper.PACKAGE_TABLE + " p ON p." + SQLiteHelper.PACKAGE_COL_ID + " = ps." + SQLiteHelper.PACKAGE_SERVICE_COL_PACKAGE_ID
				+ " WHERE p. " + SQLiteHelper.PACKAGE_COL_ID + "=?";

		Cursor dCursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(idpackage)});

		if (dCursor != null) {
			if (dCursor.moveToFirst()) {
				do {
					sp = new ServicePass();
					sp.setId(dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.SERVICE_COL_ID)));
					sp.setName(dCursor.getString(dCursor.getColumnIndex(SQLiteHelper.SERVICE_COL_NAME)));
					sp.setType(dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.SERVICE_COL_TYPE)));
					serviceList.add(sp);
				} while (dCursor.moveToNext());
			}
			dCursor.close();
		}
		return serviceList;
	}


	/**
	 * get service name from service table
	 * @param idservice
	 * @return
	 */
	public String getServiceNameByServiceId(int idservice) {
		Cursor dCursor = null;
		String service_name = null;
		try {
			final String MY_QUERY = "SELECT " + SQLiteHelper.SERVICE_COL_NAME  
					+ " FROM " + SQLiteHelper.SERVICE_TABLE 
					+ " WHERE " + SQLiteHelper.SERVICE_COL_ID + "=?";

			dCursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(idservice)});

			if (dCursor != null) {
				if (dCursor.moveToFirst()) {
					do {
						service_name = dCursor.getString(dCursor.getColumnIndex(SQLiteHelper.SERVICE_COL_NAME));
					} while (dCursor.moveToNext());
				}
				dCursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getServiceNameByServiceId - " + e.getMessage());
		}
		return service_name;
	}

	public Service getService(long id) {
		Service service = null;
		Cursor cursor = null;

		try {
			cursor = database.query(SQLiteHelper.SERVICE_TABLE,
					allServiceColumns, SQLiteHelper.SERVICE_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				service = new Service();
				service.setType(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.SERVICE_COL_TYPE)));
				service.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.SERVICE_COL_NAME)));
			}
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getService - " + e.getMessage());
		}
		return service;
	}

	/**
	 * returns service from provider service table where package_id is passed as parameter
	 * @param idpackage
	 * @return
	 */
	public String getServicebyPackageId(int idpackage) {
		Cursor cursor = null;
		String service = null;
		try {
			final String MY_QUERY = "SELECT " + SQLiteHelper.PROVIDER_SERVICE_COL_SERVICE  
					+ " FROM " + SQLiteHelper.PROVIDER_SERVICE_TABLE
					+ " WHERE " + SQLiteHelper.PROVIDER_SERVICE_COL_PACKAGE_ID + "=?";

			cursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(idpackage)});
			if (cursor.moveToFirst()) {
				service = cursor.getString(cursor.getColumnIndex(SQLiteHelper.PROVIDER_SERVICE_COL_SERVICE));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getServicebyPackageId - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return service;
	}

	/**
	 * get pass duration by numotipass
	 * @param numotipass
	 * @return
	 */
	public int getDurationByNumOtipass(int numotipass) {

		int duration = 0;

		final String MY_QUERY = "SELECT p." + SQLiteHelper.PACKAGE_COL_DURATION
				+ " FROM " + SQLiteHelper.PACKAGE_TABLE 
				+ " p INNER JOIN " + SQLiteHelper.OTIPASS_TABLE
				+ " o ON p." + SQLiteHelper.PACKAGE_COL_ID + " = o." + SQLiteHelper.OTI_COL_PID
				+ " WHERE o. " + SQLiteHelper.OTI_COL_NUMOTIPASS + "=?";

		Cursor dCursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(numotipass)});

		if (dCursor != null) {
			if (dCursor.moveToFirst()) {
				do {
					duration = dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_DURATION));
				} while (dCursor.moveToNext());
			}
			dCursor.close();
		}
		return duration;
	}

	/**
	 * get period by numotipass
	 * @param numotipass
	 * @return
	 */
	public int getPeriodByNumOtipass(int numotipass) {

		int period = 0;

		final String MY_QUERY = "SELECT p." + SQLiteHelper.PACKAGE_COL_PERIOD
				+ " FROM " + SQLiteHelper.PACKAGE_TABLE 
				+ " p INNER JOIN " + SQLiteHelper.OTIPASS_TABLE
				+ " o ON p." + SQLiteHelper.PACKAGE_COL_ID + " = o." + SQLiteHelper.OTI_COL_PID
				+ " WHERE o. " + SQLiteHelper.OTI_COL_NUMOTIPASS + "=?";

		Cursor dCursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(numotipass)});

		if (dCursor != null) {
			if (dCursor.moveToFirst()) {
				do {
					period = dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PERIOD));
				} while (dCursor.moveToNext());
			}
			dCursor.close();
		}
		return period;
	}

	/**
	 * returns service number from service_package table
	 * @param idservice
	 * @param idpackage
	 * @return
	 */
	public int getServiceNumber(int idservice, int idpackage) {
		Cursor cursor = null;
		int number = 0;
		try {
			final String MY_QUERY = "SELECT " + SQLiteHelper.PACKAGE_SERVICE_COL_NUMBER  
					+ " FROM " + SQLiteHelper.PACKAGE_SERVICE_TABLE
					+ " WHERE " + SQLiteHelper.PACKAGE_SERVICE_COL_SERVICE_ID + "=? AND " + SQLiteHelper.PACKAGE_SERVICE_COL_PACKAGE_ID + "=?";

			cursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(idservice), String.valueOf(idpackage)});
			if (cursor.moveToFirst()) {
				number = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_SERVICE_COL_NUMBER));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getServiceNumber - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return number;
	}

	/**
	 * get service from otipass table
	 * @param numotipass
	 * @return
	 */
	public String getServicesByNumOtipass(int numotipass) {
		Cursor cursor = null;
		String service = null;
		try {
			final String MY_QUERY = "SELECT " + SQLiteHelper.OTI_COL_SERVICE
					+ " FROM " + SQLiteHelper.OTIPASS_TABLE 
					+ " WHERE " + SQLiteHelper.OTI_COL_NUMOTIPASS + "=?";

			cursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(numotipass)});
			if (cursor.moveToFirst()) {
				service = cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERVICE));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getServicesByNumOtipass - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return service;
	}

	/**
	 * returns the number of times that the pass has used
	 * @param numotipass
	 * @return
	 */
	public int getNbUsePass(int numotipass) {
		Cursor cursor = null;
		int nbUse = 0;
		try {
			final String MY_QUERY = "SELECT * " 
					+ " FROM " + SQLiteHelper.USE_PASS_TABLE
					+ " WHERE " + SQLiteHelper.USE_PASS_COL_USAGE_OTIPASS + "=?";

			cursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(numotipass)});
			nbUse = cursor.getCount();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getNbUsePass - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return nbUse;
	}

	/**
	 * returns the id of entry
	 * @param numotipass
	 * @param date
	 * @return
	 */
	public int getEntryId(int numotipass, String date) {
		Cursor dCursor = null;
		int identry = 0;
		try {
			final String MY_QUERY = "SELECT " + SQLiteHelper.ENTRY_COL_ID
					+ " FROM " + SQLiteHelper.ENTRY_TABLE
					+ " WHERE " + SQLiteHelper.ENTRY_COL_OTIPASS + "=?"
					+ "  AND "  + SQLiteHelper.ENTRY_COL_DATE + "=?";

			dCursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(numotipass), date});
			if (dCursor != null) {
				if (dCursor.moveToFirst()) {
					do {
						identry = dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.ENTRY_COL_ID));
					} while (dCursor.moveToNext());
				}
				dCursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getEntryId - " + e.getMessage());
		}

		return identry;
	}

	/**
	 * returns the id of update where numotipass and date are parameters
	 * @param numotipass
	 * @param date
	 * @return
	 */
	public int getUpdateId(int numotipass, String date) {
		Cursor dCursor = null;
		int idupdate = 0;
		try {
			final String MY_QUERY = "SELECT " + SQLiteHelper.UPDATE_COL_ID
					+ " FROM " + SQLiteHelper.UPDATE_TABLE
					+ " WHERE " + SQLiteHelper.UPDATE_COL_NUMOTIPASS + "=?"
					+ "  AND "  + SQLiteHelper.UPDATE_COL_DATE + "=?";

			dCursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(numotipass), date});
			if (dCursor != null) {
				if (dCursor.moveToFirst()) {
					idupdate = dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.UPDATE_COL_ID));
				}
				dCursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getUpdateId - " + e.getMessage());
		}

		return idupdate;
	}
	
	/**
	 * returns the id of use_pass where numotipass is a parameter
	 * @param numotipass
	 * @return
	 */
	public int getUsePassId(int numotipass) {
		Cursor dCursor = null;
		int idUsePass = 0;
		try {
			final String MY_QUERY = "SELECT " + SQLiteHelper.USE_PASS_COL_USAGE_ID
					+ " FROM " + SQLiteHelper.USE_PASS_TABLE
					+ " WHERE " + SQLiteHelper.USE_PASS_COL_USAGE_OTIPASS + "=?";
			
			dCursor = database.rawQuery(MY_QUERY, new String[]{String.valueOf(numotipass)});
			if (dCursor != null) {
				if (dCursor.moveToFirst()) {
					idUsePass = dCursor.getInt(dCursor.getColumnIndex(SQLiteHelper.USE_PASS_COL_USAGE_ID));
				}
				dCursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getUsePassId - " + e.getMessage());
		}
		
		return idUsePass;
	}


	/**
	 * get provider's services
	 * @return
	 */
	public ArrayList<ProviderService> getAllProviderService() {
		Cursor cursor = null;
		ArrayList<ProviderService> psList = new ArrayList<ProviderService>();
		ProviderService ps;
		try {
			final String MY_QUERY = "SELECT * FROM " + SQLiteHelper.PROVIDER_SERVICE_TABLE;

			cursor = database.rawQuery(MY_QUERY, null);

			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						ps = new ProviderService();
						ps.setPackageId(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PROVIDER_SERVICE_COL_PACKAGE_ID)));
						ps.setService(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PROVIDER_SERVICE_COL_SERVICE)));
						psList.add(ps);
					} while (cursor.moveToNext());
				}
				cursor.close();
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getAllProviderService - " + e.getMessage());
		}
		return psList;
	}

	public Entry insertEntry(String date, int numotipass, short nb, short event, boolean uploaded, int service) {
		Entry entry = null;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.ENTRY_COL_DATE, date);
			values.put(SQLiteHelper.ENTRY_COL_OTIPASS, numotipass);
			values.put(SQLiteHelper.ENTRY_COL_NB, nb);
			values.put(SQLiteHelper.ENTRY_COL_EVENT, event);
			values.put(SQLiteHelper.ENTRY_COL_UPLOADED, uploaded);
			values.put(SQLiteHelper.ENTRY_COL_SERVICE, service);
			long id = database.insert(SQLiteHelper.ENTRY_TABLE, null, values);
			if (id > 0) {
				return getEntry(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertEntry - " + e.getMessage());
		}
		return entry;
	}

	public Param getParam(long id) {
		Param param = null;
		try {
			Cursor cursor = database.query(SQLiteHelper.PARAM_TABLE,
					allParamColumns, SQLiteHelper.PARAM_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				param = new Param();
				param.setId(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.PARAM_COL_ID)));
				param.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PARAM_COL_NAME)));
				param.setCall(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PARAM_COL_CALL)));
				param.setSoftwareVersion(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PARAM_COL_SOFT)));
				param.setCategory(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PARAM_COL_CATEGORY)));
			}
			cursor.close();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getParam - " + e.getMessage());
		}
		return param;
	}

	public Param insertParam(String name, String time,  String softwareVersion, String currency, String country) {
		Param param = null;
		try {
			ContentValues values = new ContentValues();
			// only one param record
			values.put(SQLiteHelper.PARAM_COL_ID, 1L);
			values.put(SQLiteHelper.PARAM_COL_NAME, name);
			values.put(SQLiteHelper.PARAM_COL_CALL, time);
			values.put(SQLiteHelper.PARAM_COL_SOFT, softwareVersion);
			long id = database.insert(SQLiteHelper.PARAM_TABLE, null, values);
			if (id > 0) {
				return getParam(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertParam - " + e.getMessage());
		}
		return param;
	}

	public int updateParam(Param param) {
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.PARAM_COL_ID, 1L);
			values.put(SQLiteHelper.PARAM_COL_NAME, param.getName());
			values.put(SQLiteHelper.PARAM_COL_CALL, param.getCall());
			values.put(SQLiteHelper.PARAM_COL_SOFT, param.getSoftwareVersion());
			nbRows = database.update(SQLiteHelper.PARAM_TABLE, values, SQLiteHelper.PARAM_COL_ID + " = ?", new String[] { String.valueOf(1) });
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateParam - " + e.getMessage());
		}
		return nbRows;
	}

	public Param insertParamObject(Param param) {
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.PARAM_COL_ID, 1L);
			values.put(SQLiteHelper.PARAM_COL_NAME, param.getName());
			values.put(SQLiteHelper.PARAM_COL_CALL, param.getCall());
			values.put(SQLiteHelper.PARAM_COL_SOFT, param.getSoftwareVersion());
			values.put(SQLiteHelper.PARAM_COL_CATEGORY, param.getCategory());
			long id = database.insert(SQLiteHelper.PARAM_TABLE, null, values);
			if (id > 0) {
				return getParam(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertParamObject - " + e.getMessage());
		}
		return param;
	}

	public Warning getWarning(long id) {
		Warning warning = null;
		Cursor cursor = null;
		
		try {
			cursor = database.query(SQLiteHelper.WARNING_TABLE,
                    allWarningColumns, SQLiteHelper.WARNING_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				warning = new Warning();
				warning.setId(cursor.getLong(0));
				warning.setDate(cursor.getString(1));
				warning.setSerial(cursor.getString(2));
				warning.setEvent(cursor.getShort(3));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getWarning - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return warning;
	}

	public List<Warning> getWarningList() { 
		List<Warning> warningList = new ArrayList<Warning>(); 
		Warning warning;
		Cursor cursor = null;
		
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.WARNING_TABLE ; 
			cursor = database.rawQuery(selectQuery, null); 
			if (cursor.moveToFirst()) { 
				do { 
					warning = new Warning();
					warning.setId(cursor.getLong(0));
					warning.setDate(cursor.getString(1));
					warning.setSerial(cursor.getString(2));
					warning.setEvent(cursor.getShort(3));
					warningList.add(warning);
				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getWarningList - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return warningList; 
	} 

	/**
	 * get all otipass list in db
	 * @return
	 */
	public List<Otipass> getOtipassList(){
		List<Otipass> otipassList = new ArrayList<Otipass>();
		Otipass otipass;
		Cursor cursor = null;
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.OTIPASS_TABLE ; 
			cursor = database.rawQuery(selectQuery, null); 
			if (cursor.moveToFirst()) { 
				do { 
					otipass = new Otipass();
					otipass.setNumOtipass(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.OTI_COL_NUMOTIPASS)));
					otipass.setExpiry(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_EXPIRY)));
					otipass.setPid(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.OTI_COL_PID)));
					otipass.setSerial(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERIAL)));
					otipass.setService(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERVICE)));
					otipassList.add(otipass);
				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getOtipassList - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return otipassList;
	}

	/**
	 * get all otipass list in db
	 * @return
	 */
	public List<String> getNumotipassList(){
		List<String> serialList = new ArrayList<String>();
		Cursor cursor = null;
		try {
			String selectQuery = "SELECT  numotipass FROM " + SQLiteHelper.OTIPASS_TABLE + " ORDER BY numotipass ASC";
			cursor = database.rawQuery(selectQuery, null);
			if (cursor.moveToFirst()) {
				do {
					serialList.add(String.valueOf(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.OTI_COL_NUMOTIPASS))));
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getNumotipassList - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return serialList;
	}

    /**
     * get all otipass list in db
     * @return
     */
    public List<String> getOtipassSerialList(){
        List<String> serialList = new ArrayList<String>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT  serial FROM " + SQLiteHelper.OTIPASS_TABLE + " ORDER BY serial ASC";
            cursor = database.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    serialList.add(cursor.getString(cursor.getColumnIndex(SQLiteHelper.OTI_COL_SERIAL)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, DbAdapter.class.getName() + " - getOtipassSerialList - " + e.getMessage());
        }
        if (cursor != null) {
            cursor.close();
        }
        return serialList;
    }

	public int deleteWarning(long id) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.WARNING_TABLE, SQLiteHelper.WARNING_COL_ID + " = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteWarning - " + e.getMessage());
		}
		return nbRows;
	}

	public Warning insertWarning(String date, String serial, short event) {
		Warning warning = null;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.WARNING_COL_ID, 1L);
			values.put(SQLiteHelper.WARNING_COL_DATE, date);
			values.put(SQLiteHelper.WARNING_COL_SERIAL, serial);
			values.put(SQLiteHelper.WARNING_COL_EVENT, event);
			long id = database.insert(SQLiteHelper.WARNING_TABLE, null, values);
			if (id > 0) {
				return getWarning(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertWarning - " + e.getMessage());
		}
		return warning;
	}

	public Update getUpdate(long id) {
		Update update = null;
		Cursor cursor = null;
		try {
			cursor = database.query(SQLiteHelper.UPDATE_TABLE,
                    allUpdateColumns, SQLiteHelper.UPDATE_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				update = new Update();
				update.setId(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_ID)));
				update.setDate(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_DATE)));
				update.setType(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_TYPE)));
				update.setNumotipass(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_NUMOTIPASS)));
				update.setPid(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_PID)));
				update.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_NAME)));
				update.setFname(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_FNAME)));
				update.setEmail(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_EMAIL)));
				update.setCountry(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_COUNTRY)));
				update.setPostalCode(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_POSTAL_CODE)));
				update.setNewsletter(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_NEWSLETTER)));
				update.setTwin(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_TWIN)));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getUpdate - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return update;
	}

	public List<Update> getUpdateList() { 
		List<Update> updateList = new ArrayList<Update>(); 
		Update update;
		Cursor cursor = null;
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.UPDATE_TABLE ; 
			cursor = database.rawQuery(selectQuery, null); 
			if (cursor.moveToFirst()) { 
				do { 
					update = new Update();
					update.setId(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_ID)));
					update.setDate(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_DATE)));
					update.setType(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_TYPE)));
					update.setNumotipass(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_NUMOTIPASS)));
					update.setPid(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_PID)));
					update.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_NAME)));
					update.setFname(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_FNAME)));
					update.setEmail(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_EMAIL)));
					update.setCountry(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_COUNTRY)));
					update.setPostalCode(cursor.getString(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_POSTAL_CODE)));
					update.setNewsletter(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_NEWSLETTER)));
					update.setTwin(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPDATE_COL_TWIN)));

					updateList.add(update);
				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getUpdateList - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return updateList; 
	} 



	public long insertUpdate(String date, int type, int numotipass, int pid, PersonalInfo persoInfo, int twin) {
		long id = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.UPDATE_COL_DATE, date);
			values.put(SQLiteHelper.UPDATE_COL_TYPE, type);
			values.put(SQLiteHelper.UPDATE_COL_NUMOTIPASS, numotipass);
			values.put(SQLiteHelper.UPDATE_COL_PID, pid);
			if (persoInfo != null) {
				values.put(SQLiteHelper.UPDATE_COL_NAME, persoInfo.getName());
				values.put(SQLiteHelper.UPDATE_COL_FNAME, persoInfo.getFirstName());
				values.put(SQLiteHelper.UPDATE_COL_EMAIL, persoInfo.getEmail());
				values.put(SQLiteHelper.UPDATE_COL_COUNTRY, persoInfo.getCountry());
				values.put(SQLiteHelper.UPDATE_COL_POSTAL_CODE, persoInfo.getPostalCode());
				values.put(SQLiteHelper.UPDATE_COL_NEWSLETTER, persoInfo.getNewsletter());
			}

			values.put(SQLiteHelper.UPDATE_COL_TWIN, twin);
			id = database.insert(SQLiteHelper.UPDATE_TABLE, null, values);

		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertUpdate - " + e.getMessage());
		}
		return id;
	}

	public int deleteUpdate(long id) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.UPDATE_TABLE, SQLiteHelper.UPDATE_COL_ID + " = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteUpdate - " + e.getMessage());
		}
		return nbRows;
	}

	/**
	 * deletes entry where id is a parameter
	 * @param id
	 * @return
	 */
	public int deleteEntry(long id) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.ENTRY_TABLE, SQLiteHelper.ENTRY_COL_ID + " = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteEntry - " + e.getMessage());
		}
		return nbRows;
	}
	
	/**
	 * deletes a row of use_pass where id is a parameter
	 * @param id
	 * @return
	 */
	public int deleteUsePass(long id) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.USE_PASS_TABLE, SQLiteHelper.USE_PASS_COL_USAGE_ID + " = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteUsePass - " + e.getMessage());
		}
		return nbRows;
	}


	public Tablet getTablet(long id) {
		Tablet tablet = null;
		Cursor cursor = null;
		try {
			cursor = database.query(SQLiteHelper.TABLET_TABLE,
					allTabletColumns, SQLiteHelper.TABLET_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				tablet = new Tablet();
				tablet.setId(cursor.getLong(0));
				tablet.setNumSequence(cursor.getInt(1));
				tablet.setUploadTime(cursor.getString(2));
				tablet.setDownloadTime(cursor.getString(3));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getTablet - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return tablet;
	}

	public Tablet insertTablet(int numSequence, String uploadTime, String downloadTime) {
		Tablet tablet = null;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.TABLET_COL_NUMSEQUENCE, numSequence);
			values.put(SQLiteHelper.TABLET_COL_UPLOAD_TIME, uploadTime);
			values.put(SQLiteHelper.TABLET_COL_DOWNLOAD_TIME, downloadTime);
			long id = database.insert(SQLiteHelper.TABLET_TABLE, null, values);
			if (id > 0) {
				tablet =  getTablet(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertTablet - " + e.getMessage());
		}
		return tablet;
	}

	public int updateTablet(Tablet tablet) {
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.TABLET_COL_NUMSEQUENCE, tablet.getNumSequence());
			values.put(SQLiteHelper.TABLET_COL_UPLOAD_TIME, tablet.getDownloadTime());
			values.put(SQLiteHelper.TABLET_COL_DOWNLOAD_TIME, tablet.getDownloadTime());
			nbRows = database.update(SQLiteHelper.TABLET_TABLE, values, SQLiteHelper.TABLET_COL_ID + " = ?", new String[] { String.valueOf(tablet.getId()) });
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateTablet - " + e.getMessage());
		}
		return nbRows;
	}

	public int updateWarning(Warning warning) {
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.WARNING_COL_DATE, warning.getDate());
			values.put(SQLiteHelper.WARNING_COL_SERIAL, warning.getSerial());
			values.put(SQLiteHelper.WARNING_COL_EVENT, warning.getEvent());
			nbRows = database.update(SQLiteHelper.WARNING_TABLE, values, SQLiteHelper.WARNING_COL_ID + " = ?", new String[] { String.valueOf(warning.getId()) });
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateWarning - " + e.getMessage());
		}
		return nbRows;
	}

	public void flushTable(String table) {
		try {
			database.execSQL(SQLiteHelper.FLUSH_TABLE + table);
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - FlushTable - " + e.getMessage());
		}
	}

	public Stock getStock(long id) {
		Stock stock = null;
		Cursor cursor = null;
		try {
			cursor = database.query(SQLiteHelper.STOCK_TABLE,
					allStockColumns, SQLiteHelper.STOCK_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				stock = new Stock();
				stock.setId(cursor.getLong(cursor.getColumnIndex(SQLiteHelper.STOCK_COL_ID)));
				stock.setProviderId(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.STOCK_COL_PROVIDER_ID)));
				stock.setNbCards(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.STOCK_COL_NB_CARDS)));
				stock.setThreshold(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.STOCK_COL_THRESHOLD)));
				stock.setAlert(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.STOCK_COL_ALERT)));
			}
			cursor.close();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getStock - " + e.getMessage());
		}
		return stock;
	}

	public Stock insertStockObject(Stock stock) {
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.STOCK_COL_ID, 1L);
			values.put(SQLiteHelper.STOCK_COL_PROVIDER_ID, stock.getProviderId());
			values.put(SQLiteHelper.STOCK_COL_NB_CARDS, stock.getNbCards());
			values.put(SQLiteHelper.STOCK_COL_THRESHOLD, stock.getThreshold());
			values.put(SQLiteHelper.STOCK_COL_ALERT, stock.getAlert());
			long id = database.insert(SQLiteHelper.STOCK_TABLE, null, values);
			if (id > 0) {
				stock =  getStock(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertStock - " + e.getMessage());
		}
		return stock;
	}

	public int getNbCards() {
		int nbCards = 0;
		Cursor cursor = null;
		cursor = database.query(SQLiteHelper.OTIPASS_TABLE,
				allOtipassColumns, null, null, null, null, null);
		if (cursor.getCount() > 0) {
			nbCards = cursor.getCount();
		}
		if (cursor != null) {
			cursor.close();
		}
		
		return nbCards;
	}

	public Msg getMessage(long id) {
		Msg message = null;
		Cursor cursor = null;
		try {
			cursor = database.query(SQLiteHelper.MSG_TABLE,
                    allMessageColumns, SQLiteHelper.MSG_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				message = new Msg();
				message.setId((int)cursor.getLong(0));
				message.setMsg(cursor.getString(1));
				message.setLang(cursor.getString(2));
				message.setStartDate(cursor.getString(3));
				message.setEndDate(cursor.getString(4));
				message.setHidden(cursor.getInt(5));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getMessage - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return message;
	}

	public List<Msg> getMessageList() { 
		List<Msg> messageList = new ArrayList<Msg>(); 
		Msg message;
		Cursor cursor = null;
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.MSG_TABLE ; 
			cursor = database.rawQuery(selectQuery, null); 
			if (cursor.moveToFirst()) { 
				do { 
					message = new Msg();
					message.setId((int)cursor.getLong(0));
					message.setMsg(cursor.getString(1));
					message.setLang(cursor.getString(2));
					message.setStartDate(cursor.getString(3));
					message.setEndDate(cursor.getString(4));
					message.setHidden(cursor.getInt(5));
					messageList.add(message);
				} while (cursor.moveToNext()); 
			} 
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getMessageList - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return messageList; 
	}

	public Support getSupport(long id) {
		Support message = null;
		Cursor cursor = null;
		try {
			cursor = database.query(SQLiteHelper.SUPPORT_TABLE,
					allSupportColumns, SQLiteHelper.SUPPORT_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				message = new Support();
				message.setId((int)cursor.getLong(0));
				message.setMsg(cursor.getString(1));
				message.setHidden(cursor.getInt(2));
				message.setDate(cursor.getString(3));
				message.setParent(cursor.getInt(4));
				message.setEvent(cursor.getInt(5));
				message.setQuery(cursor.getString(6));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getSupport - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return message;
	}

	public List<Support> getSupportList() {
		List<Support> messageList = new ArrayList<Support>();
		Support message;
		Cursor cursor = null;
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.SUPPORT_TABLE ;
			cursor = database.rawQuery(selectQuery, null);
			if (cursor.moveToFirst()) {
				do {
					message = new Support();
					message.setId((int)cursor.getLong(0));
					message.setMsg(cursor.getString(1));
					message.setHidden(cursor.getInt(2));
					message.setDate(cursor.getString(3));
					message.setParent(cursor.getInt(4));
					message.setEvent(cursor.getInt(5));
					message.setQuery(cursor.getString(6));
					messageList.add(message);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getSupportList - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}
		return messageList;
	}

	/**
	 * deletes all messages with hidden parameter = 0
	 *
	 * @return
	 */
	public int deleteMessages() {
		int nbRows = 0;
		try {
            Calendar cal = Calendar.getInstance();
            String now =  tools.formatSQLDate(cal);
			nbRows = database.delete(SQLiteHelper.MSG_TABLE, SQLiteHelper.MSG_COL_HIDDEN + " = ? OR (" + SQLiteHelper.MSG_COL_HIDDEN + " = ? AND " + SQLiteHelper.MSG_COL_END_DATE + " < ?)", new String[]{"0", "1", now});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteMessages - " + e.getMessage());
		}
		return nbRows;
	}

	public ArrayList<PackageObject> getPackage(){

		ArrayList<PackageObject> listPackages = new ArrayList<PackageObject>();
		PackageObject po;
		Cursor cursor = null;
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.PACKAGE_TABLE ; 
			cursor = database.rawQuery(selectQuery, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					do {
						po = new PackageObject();
						po.setId(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_ID)));
						po.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_NAME)));
						po.setDuration(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_DURATION)));
						po.setPeriod(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PERIOD)));
						po.setPrice(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PRICE)));
						po.setRef(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_REF)));
						listPackages.add(po);
					} while (cursor.moveToNext());

				}
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getPackage - " + e.getMessage());
		}

		if (cursor != null) {
			cursor.close();
		}
		return listPackages;
	}

	public PackageObject getMinDurationPackage(){
		PackageObject po = null;
		Cursor cursor = null;
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.PACKAGE_TABLE + " ORDER BY " + SQLiteHelper.PACKAGE_COL_DURATION + " ASC";
			cursor = database.rawQuery(selectQuery, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					po = new PackageObject();
					po.setId(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_ID)));
					po.setName(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_NAME)));
					po.setDuration(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_DURATION)));
					po.setPeriod(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PERIOD)));
					po.setPrice(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_PRICE)));
					po.setRef(cursor.getString(cursor.getColumnIndex(SQLiteHelper.PACKAGE_COL_REF)));
				}
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getMinDurationPackage - " + e.getMessage());
		}

		if (cursor != null) {
			cursor.close();
		}
		return po;
	}

	public Msg insertMessageObject(Msg message) {
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.MSG_COL_ID, message.getId());
			values.put(SQLiteHelper.MSG_COL_TEXT, message.getMsg());
			values.put(SQLiteHelper.MSG_COL_TEXT, message.getMsg());
			values.put(SQLiteHelper.MSG_COL_LANG, message.getLang());
			values.put(SQLiteHelper.MSG_COL_START_DATE, message.getStartDate());
			values.put(SQLiteHelper.MSG_COL_END_DATE, message.getEndDate());
            values.put(SQLiteHelper.MSG_COL_HIDDEN, 0);
			long id = database.insert(SQLiteHelper.MSG_TABLE, null, values);
			if (id > 0) {
				message =  getMessage(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertMessageObject - " + e.getMessage());
		}
		return message;
	}

	public int insertMessageList(List<Msg> messageList) {
		Msg message = null;
		long id;
		int i = 0;
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<messageList.size(); i++) {
				message = messageList.get(i);
				values.clear();
				values.put(SQLiteHelper.MSG_COL_ID, message.getId());
				values.put(SQLiteHelper.MSG_COL_TEXT, message.getMsg());
				values.put(SQLiteHelper.MSG_COL_LANG, message.getLang());
				values.put(SQLiteHelper.MSG_COL_START_DATE, message.getStartDate());
				values.put(SQLiteHelper.MSG_COL_END_DATE, message.getEndDate());
                values.put(SQLiteHelper.MSG_COL_HIDDEN, 0);
				id = database.insert(SQLiteHelper.MSG_TABLE, null, values);
				if (id < 1L) {
					Log.e(TAG, DbAdapter.class.getName() + " - insertUserList - Cannot insert Message list");
					break;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertMessageList - " + e.getMessage());
		}
		return i;
	}

	public int insertSupportList(List<Support> messageList) {
		Support message = null;
		long id;
		int i = 0;
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<messageList.size(); i++) {
				message = messageList.get(i);
				values.clear();
				values.put(SQLiteHelper.SUPPORT_COL_ID, message.getId());
				values.put(SQLiteHelper.SUPPORT_COL_TEXT, message.getMsg());
				values.put(SQLiteHelper.SUPPORT_COL_DATE, message.getDate());
				values.put(SQLiteHelper.SUPPORT_COL_EVENT, message.getEvent());
				values.put(SQLiteHelper.SUPPORT_COL_PARENT, message.getParent());
				values.put(SQLiteHelper.SUPPORT_COL_QUERY, message.getQuery());
				values.put(SQLiteHelper.SUPPORT_COL_HIDDEN, 0);
				id = database.insert(SQLiteHelper.SUPPORT_TABLE, null, values);
				if (id < 1L) {
					Log.e(TAG, DbAdapter.class.getName() + " - insertSupportList - Cannot insert Support list");
					break;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertSupportList - " + e.getMessage());
		}
		return i;
	}

	public Support insertSupportObject(Support message) {
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.SUPPORT_COL_ID, message.getId());
			values.put(SQLiteHelper.SUPPORT_COL_TEXT, message.getMsg());
			values.put(SQLiteHelper.SUPPORT_COL_DATE, message.getDate());
			values.put(SQLiteHelper.SUPPORT_COL_EVENT, message.getEvent());
			values.put(SQLiteHelper.SUPPORT_COL_PARENT, message.getParent());
			values.put(SQLiteHelper.SUPPORT_COL_QUERY, message.getQuery());
			values.put(SQLiteHelper.SUPPORT_COL_HIDDEN, 0);
			long id = database.insert(SQLiteHelper.SUPPORT_TABLE, null, values);
			if (id > 0) {
				message =  getSupport(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertSupportObject - " + e.getMessage());
		}
		return message;
	}

    public int updateMessage(Msg msg) {
        int nbRows = 0;
        try {
            ContentValues values = new ContentValues();
            values.clear();
            values.put(SQLiteHelper.MSG_COL_TEXT, msg.getMsg());
            values.put(SQLiteHelper.MSG_COL_LANG, msg.getLang());
            values.put(SQLiteHelper.MSG_COL_START_DATE, msg.getStartDate());
            values.put(SQLiteHelper.MSG_COL_END_DATE, msg.getEndDate());
            values.put(SQLiteHelper.MSG_COL_HIDDEN, msg.getHidden());
            nbRows = database.update(SQLiteHelper.MSG_TABLE, values, SQLiteHelper.MSG_COL_ID + " = ?", new String[]{String.valueOf(msg.getId())});
        } catch (Exception e) {
            Log.e(TAG, DbAdapter.class.getName() + " - updateMessage - " + e.getMessage());
        }
        return nbRows;
    }

	public int updateSupportfHidden(int id, int hidden) {
		int nbRows = 0;
		Support msg;
		try {
			msg =  getSupport(id);
			ContentValues values = new ContentValues();
			values.clear();
			values.put(SQLiteHelper.SUPPORT_COL_HIDDEN, hidden);
			nbRows = database.update(SQLiteHelper.SUPPORT_TABLE, values, SQLiteHelper.SUPPORT_COL_ID + " = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateSupport - " + e.getMessage());
		}
		return nbRows;
	}

    /**
     * deletes message where id is a parameter
     * @param id
     * @return
     */
    public int deleteMessage(long id) {
        int nbRows = 0;
        try {
            nbRows = database.delete(SQLiteHelper.MSG_TABLE, SQLiteHelper.MSG_COL_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e(TAG, DbAdapter.class.getName() + " - deleteMessage - " + e.getMessage());
        }
        return nbRows;
    }

	public long insertService(ServicePass service){
		long id = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.SERVICE_COL_ID, service.getId());
			values.put(SQLiteHelper.SERVICE_COL_TYPE, service.getType());
			values.put(SQLiteHelper.SERVICE_COL_NAME, service.getName());
			id = database.insert(SQLiteHelper.SERVICE_TABLE, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, DbAdapter.class.getName() + " - insertService - " + e.getMessage());
		}

		return id;
	}

	public long insertServiceList(List<ServicePass> serviceList){
		long id = 0;
		int i=0;
		ServicePass service;
		database.beginTransaction();
		
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<serviceList.size(); i++) {
				service = serviceList.get(i);
				try { // instead of IsServiceExist()
					values.put(SQLiteHelper.SERVICE_COL_ID, service.getId());
					values.put(SQLiteHelper.SERVICE_COL_TYPE, service.getType());
					values.put(SQLiteHelper.SERVICE_COL_NAME, service.getName());
					id = database.insert(SQLiteHelper.SERVICE_TABLE, null, values);
				} catch (Exception e) {}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertServiceList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}
	
	
	public long insertPackage(PackageObject packageObject){
		long id = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.PACKAGE_COL_ID, packageObject.getId());
			values.put(SQLiteHelper.PACKAGE_COL_NAME, packageObject.getName());
			values.put(SQLiteHelper.PACKAGE_COL_DURATION, packageObject.getDuration());
			values.put(SQLiteHelper.PACKAGE_COL_PERIOD, packageObject.getPeriod());
			values.put(SQLiteHelper.PACKAGE_COL_PRICE, packageObject.getPrice());
			values.put(SQLiteHelper.PACKAGE_COL_REF, packageObject.getRef());

			id = database.insert(SQLiteHelper.PACKAGE_TABLE, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, DbAdapter.class.getName() + " - insertPackage - " + e.getMessage());
		}

		return id;
	}

	public long insertPackageList(List<PackageObject> packageList){
		long id = 0;
		int i=0;
		PackageObject packageObject;
		database.beginTransaction();
		
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<packageList.size(); i++) {
				packageObject = packageList.get(i);
				values.put(SQLiteHelper.PACKAGE_COL_ID, packageObject.getId());
				values.put(SQLiteHelper.PACKAGE_COL_NAME, packageObject.getName());
				values.put(SQLiteHelper.PACKAGE_COL_DURATION, packageObject.getDuration());
				values.put(SQLiteHelper.PACKAGE_COL_PERIOD, packageObject.getPeriod());
				values.put(SQLiteHelper.PACKAGE_COL_PRICE, packageObject.getPrice());
				values.put(SQLiteHelper.PACKAGE_COL_REF, packageObject.getRef());
				id = database.insert(SQLiteHelper.PACKAGE_TABLE, null, values);
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertPackageList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}
	
	
	public long insertPackageService(PackageService ps){
		long id = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.PACKAGE_SERVICE_COL_PACKAGE_ID, ps.getPackageId());
			values.put(SQLiteHelper.PACKAGE_SERVICE_COL_SERVICE_ID, ps.getServiceId());
			values.put(SQLiteHelper.PACKAGE_SERVICE_COL_NUMBER, ps.getNumber());
			id = database.insert(SQLiteHelper.PACKAGE_SERVICE_TABLE, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, DbAdapter.class.getName() + " - insertPackage - " + e.getMessage());
		}

		return id;
	}

	public long insertPackageServiceList(List<PackageService> packageServiceList){
		long id = 0;
		int i=0;
		PackageService ps;
		database.beginTransaction();
		
		try {
			ContentValues values = new ContentValues();
			for (i=0; i<packageServiceList.size(); i++) {
				try {
					ps = packageServiceList.get(i);
					values.put(SQLiteHelper.PACKAGE_SERVICE_COL_PACKAGE_ID, ps.getPackageId());
					values.put(SQLiteHelper.PACKAGE_SERVICE_COL_SERVICE_ID, ps.getServiceId());
					values.put(SQLiteHelper.PACKAGE_SERVICE_COL_NUMBER, ps.getNumber());
					id = database.insert(SQLiteHelper.PACKAGE_SERVICE_TABLE, null, values);
				} catch (Exception e) {}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertPackageServiceList - " + e.getMessage());
		}
		database.endTransaction();
		return i;
	}
	

	
	public long insertProviderService(ProviderService ps){
		long id = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.PROVIDER_SERVICE_COL_PACKAGE_ID, ps.getPackageId());
			values.put(SQLiteHelper.PROVIDER_SERVICE_COL_SERVICE, ps.getService());
			id = database.insert(SQLiteHelper.PROVIDER_SERVICE_TABLE, null, values);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, DbAdapter.class.getName() + " - insertProviderService - " + e.getMessage());
		}

		return id;
	}


	public void StartTransaction() {
		database.beginTransaction();
	}

	public void endTransaction(boolean success) {
		if (success) {
			database.setTransactionSuccessful();
		}
		database.endTransaction();
	}

	public void databaseBackupSDCard() {
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				String currentDBPath = "//data//"
						+ "com.otipass."+ Constants.PROJECT + "//databases//"
						+ SQLiteHelper.DATABASE_NAME;
                new File(sd + "/clientDB/").mkdirs();
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd + "/clientDB/" + SQLiteHelper.DATABASE_NAME);
				if (currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB)
					.getChannel();
					FileChannel dst = new FileOutputStream(backupDB)
					.getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				}
			}

        } catch (Exception e) {
		    Log.e(Constants.TAG, e.getMessage());
		}
	}

	public int cancelEntry(LastEntry entry) {
		int result = cSaveOK, nb;
		long nb_update;
		try {
            StartTransaction();
            Calendar cal = entry.getDate();
            String date = tools.formatSQLDate(cal);
			long idEntry = entry.getid();
			int numotipass = (int)entry.getOtipass().getNumOtipass();
			nb_update = insertUpdate(date, Constants.UPD_CANCEL_ENTRY, numotipass, -1, null, 0);
			if (nb_update < 1) {
				result = cSaveKO;
				Log.e(Constants.TAG, DbAdapter.class.getName() + " - cancelEntry failed");
			}else {
				if (idEntry > 0) {
					deleteEntry((int)idEntry);
				}
                // update otipass
                Otipass otipass = entry.getOtipass();
                if (otipass != null) {
                    nb = updateOtipass(otipass);
                    if (nb < 1) {
                        result = cSaveKO;
                    }
                }

			}
		} catch (Exception e) {
			result = cSaveKO;
			Log.e(TAG, DbAdapter.class.getName() + " - cancelEntry - " + e.getMessage());
		}
        finally {
            endTransaction(result == cSaveOK);
        }
        return result;
	}
	public Wl insertWl(Wl w) {
		Wl wl = null;
		try {
			ContentValues values = new ContentValues();

			values.put(SQLiteHelper.WL_COL_ID, w.getId());
			values.put(SQLiteHelper.WL_COL_DATE, w.getDate());
			values.put(SQLiteHelper.WL_COL_NBSTEPS, w.getNbsteps());
			values.put(SQLiteHelper.WL_COL_NBCARDS, w.getNbcards());
			values.put(SQLiteHelper.WL_COL_NUMSEQUENCE, w.getNumsequence());
			values.put(SQLiteHelper.WL_COL_STATUS, w.getStatus());
			long id = database.insert(SQLiteHelper.WL_TABLE, null, values);
			if (id > 0) {
				return getWl(id);
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertWl - " + e.getMessage());
		}
		return wl;
	}


	public Wl getWl(long id) {
		Wl wl = null;
		Cursor cursor = null;

		try {
			cursor = database.query(SQLiteHelper.WL_TABLE,
					allWlColumns, SQLiteHelper.WL_COL_ID + " = " + id, null, null, null, null);
			if (cursor.moveToFirst()) {
				wl = new Wl();
				wl.setId(cursor.getLong(0));
				wl.setDate(cursor.getString(1));
				wl.setNbsteps(cursor.getInt(2));
				wl.setNbcards(cursor.getInt(3));
				wl.setNumsequence(cursor.getInt(4));
				wl.setStatus(cursor.getInt(5));
			}
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getWl - " + e.getMessage());
		}
		if (cursor != null) {
			cursor.close();
		}

		return wl;
	}

	public int deleteWl(long id) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.WL_TABLE, SQLiteHelper.WL_COL_ID + " = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteWl - " + e.getMessage());
		}
		return nbRows;
	}

	public int updateWl(Wl wl) {
		int nbRows = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.WL_COL_DATE, wl.getDate());
			values.put(SQLiteHelper.WL_COL_NBSTEPS, wl.getNbsteps());
			values.put(SQLiteHelper.WL_COL_NBCARDS, wl.getNbcards());
			values.put(SQLiteHelper.WL_COL_NUMSEQUENCE, wl.getNumsequence());
			values.put(SQLiteHelper.WL_COL_STATUS, wl.getStatus());
			nbRows = database.update(SQLiteHelper.WL_TABLE, values, SQLiteHelper.WL_COL_ID + " = ?", new String[]{String.valueOf(wl.getId())});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - updateWl - " + e.getMessage());
		}
		return nbRows;
	}

	public long insertBug(Bug bug) {
		long id = 0;
		try {
			ContentValues values = new ContentValues();
			values.put(SQLiteHelper.BUG_REPORT_COL_TEXT, bug.getText());
			values.put(SQLiteHelper.BUG_REPORT_COL_DATE, bug.getDate());

			id = database.insert(SQLiteHelper.BUG_REPORT_TABLE, null, values);

		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - insertBug - " + e.getMessage());
		}
		return id;
	}
	public int deleteBug(long id) {
		int nbRows = 0;
		try {
			nbRows = database.delete(SQLiteHelper.BUG_REPORT_TABLE, SQLiteHelper.BUG_REPORT_COL_ID + " = ?", new String[]{String.valueOf(id)});
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - deleteBug - " + e.getMessage());
		}
		return nbRows;
	}
	public List<Bug> getBugList() {
		List<Bug> bugList = new ArrayList<Bug>();
		Bug bug;
		try {
			String selectQuery = "SELECT  * FROM " + SQLiteHelper.BUG_REPORT_TABLE ;
			Cursor cursor = database.rawQuery(selectQuery, null);
			if (cursor.moveToFirst()) {
				do {
					bug = new Bug();
					bug.setId(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.BUG_REPORT_COL_ID)));
					bug.setDate(cursor.getString(cursor.getColumnIndex(SQLiteHelper.BUG_REPORT_COL_DATE)));
					bug.setText(cursor.getString(cursor.getColumnIndex(SQLiteHelper.BUG_REPORT_COL_TEXT)));

					bugList.add(bug);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} catch (Exception e) {
			Log.e(TAG, DbAdapter.class.getName() + " - getBugList - " + e.getMessage());
		}
		return bugList;
	}


}

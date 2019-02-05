/**
 ================================================================================

 OTIPASS
 synchronization package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: SynchronizationService.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */
package com.otipass.synchronization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import models.Constants;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.otipass.http.XmlTools;
import com.otipass.sql.Bug;
import com.otipass.sql.Create;
import com.otipass.sql.Entry;
import com.otipass.sql.Msg;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.SQLiteHelper;
import com.otipass.sql.Otipass;
import com.otipass.sql.PackageObject;
import com.otipass.sql.PackageService;
import com.otipass.sql.Param;
import com.otipass.sql.Partial;
import com.otipass.sql.PartialServiceCpt;
import com.otipass.sql.ProviderService;
import com.otipass.sql.ServicePass;
import com.otipass.sql.Stock;
import com.otipass.sql.Support;
import com.otipass.sql.Tablet;
import com.otipass.sql.Update;
import com.otipass.sql.Usage;
import com.otipass.sql.User;
import com.otipass.sql.Warning;
import com.otipass.sql.Wl;
import com.otipass.tools.OtipassCard;
import com.otipass.tools.admin;
import com.otipass.tools.tools;


public class SynchronizationService extends Service {
	private static final int cConnectAction = 1;
	private static final int cExchangeAction = 2;
	private static final int cDisconnectAction = 3;

	private Timer timer;
	private ProgressDialog pDialog;

	private Handler communicationHandler;
	private XmlTools xmlTools;
	private DbAdapter dbAdapter;
	private User user;
	private int providerId;
	private int wlType;
	private String xmlUpload;
	private int statusCom;
	private String callingTime;
	private boolean newParamsReceived = false;
	private String dbPath; 
	private String logcatPath; 
	private int synchronizationType; 
	private Context context;
	private int deviceType;
	private int communicationType;
	private boolean messageWithUpload = false;

	private List<Entry> entryList;
	private List<Update> updateList;
	private List<Warning> warningList;
	private String softwareVersion="";
	private String language;

	private int wlNbCards;
	private int wlNbSteps;

	private OtipassCard card;

	private HttpsURLConnection conn;
	Message msg = null;
	int step = cConnectionStep;
	private boolean check;
	private ScheduledExecutorService scheduler;
	private Handler mHandler;
	private Runnable SEND_ENTRYTask;
	private List<ServicePass> serviceList;
	private List<PackageObject> packageList;
	private List<PackageService> packageServiceList;
	private List<ProviderService> providerServiceList;
	private Otipass otipass;
	private int category;
	private Intent intent;
	private Param param;
	private static Timer periodicCallTimer;
	private int numotipass;
    private String serial;
	private List<Bug> bugList;



	public void setMessageWithUpload(boolean messageWithUpload) {
		this.messageWithUpload = messageWithUpload;
	}

	public void setWLType(int wlType) {
		this.wlType = wlType;
	}


	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public void setCommunicationType(int communicationType) {
		this.communicationType = communicationType;
	}

	public void setSynchronizationType(int synchronizationType) {
		this.synchronizationType = synchronizationType;
	}

    public void setCommunicationHandler(Handler communicationHandler) {
        this.communicationHandler = communicationHandler;
    }
	public int getSynchronizationType() {
		return this.synchronizationType;
	}

	public int getWlNbCards() {
		return this.wlNbCards;
	}

	public int getWlNbSteps() {
		return this.wlNbSteps;
	}

	public void setXmlUpload(String xmlUpload) {
		this.xmlUpload = xmlUpload;
	}


	public void setCallingTime(String callingTime) {
		this.callingTime = callingTime;
	}

	public String getCallingTime() {
		return this.callingTime;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public boolean getNewParamsReceived() {
		return this.newParamsReceived;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public void setLogcatPath(String logcatPath) {
		this.logcatPath = logcatPath;
	}

	// synchronisation types
	public static final int cUserSynchronization = 1;
	public static final int cUploadSynchronization = 2;
	public static final int cNightUploadSynchronization = 3;
	public static final int cNightDownloadSynchronization = 4;
	public static final int cInitSynchronization = 5;
	public static final int cStockSynchronization = 6;
	public static final int cMessageSynchronization = 7;

	// communication types
	public static final int cGetTotalWL = 1;
	public static final int cGetPartialWL = 2; 
	public static final int cSynchronize = 3; 
	public static final int cInit = 4; 
	public static final int cUpload = 5; 

	// communication errors
	public static final int cComPending = 1;
	public static final int cComNotConnected = 2;
	public static final int cComOK = 200;
	public static final int cComServerError = 500;
	public static final int cComClientBadRequest = 400;
	public static final int cComClientAccessDenied = 401;
	public static final int cComClientForbidden = 403;
	public static final int cComClientMethodNotAllowed = 405;
	public static final int cComClientMethodFailure = 424;
	public static final int cComClientDecodeFailure = 425;
	public static final int cComClientExceptionFailure = 426;
	public static final int cComClientSQLiteFailure = 427;
	public static final int cComRequestTimeout = 408;
	public static final int cComNoMsgFound = 404;
	

	// server urls
	private static final String urlServer = "https://" + Constants.plateform + ".otipass.net/mobile/" + Constants.PROJECT + "/";
	private static final String urlLogin = "login/login";
	private static final String urlLogout = "login/logout";
	private static final String urlWL = "whitelist/index";
	private static final String urlinitWL = "whitelist/initwl";
	private static final String urlgetWL = "whitelist/getwl";
	private static final String urlinitPartialWL = "whitelist/initpartialwl";
	private static final String urlParam = "param/index";
	private static final String urlUser = "user/index";
	private static final String urlInit = "init/index";
	private static final String urlUpload = "update/index";
	private static final String urlUploadFile = "upload/index";
	private static final String urlEntry = "entry/index";
	private static final String urlStock = "stock/index";
	private static final String urlMessage = "message/index";
	private static final String urlCheck = "check/numotipass";
    private static final String urlCheckSerial = "check/serial";
	private static final String urlCloseSupport = "message/closesupport";
	private static final String urlBug = "exception/index";
	private static final String urlSupport = "message/getsupport";


	// server exchange states
	// what states
	public static final int cProgressMsg = 1;
	public static final int cEndMsg = 2;
	// arg1 states
	public static final int cConnectionStep = 1;
	public static final int cgetWLStep = 2;
	public static final int cInsertWLStep = 3;
	public static final int cgetUserStep = 4;
	public static final int cInsertUserStep = 5;
	public static final int cgetParamStep = 6;
	public static final int cInsertParamStep = 7;
	public static final int cDeconnectionStep = 8;
	public static final int cInitStep = 9;
	public static final int cUploadStep = 10;
	public static final int cUploadLogCatStep = 11;
	public static final int cUploadDBStep = 12;
	public static final int cInitWLStep1 = 13;
	public static final int cInitWLStep2 = 14;
	public static final int cgetTotalWLStep = 15;
	public static final int cEntryStep = 16;
	public static final int cStockStep = 17;
	public static final int cMessageStep = 18;
	public static final int cInitPartialWLStep1 = 19;
	public static final int cgetPartialWLStep = 20;
	public static final int cInitWLStep0 = 21;
	public static final int cInitPartialWLStep2 = 22;
    public static final int cBugStep = 23;
	public static final int cSupportStep = 24;

	public static final int cgetParamStep1 = 30;
	public static final int cgetParamStep2 = 31;
	public static final int cgetParamStep3 = 32;

	public void  setProviderId(int providerId) {
		this.providerId = providerId;
	}

	public int getProviderId() {
		return providerId;
	}
	
	private void initContext() {
		try {
			dbAdapter = new DbAdapter(context);
			dbAdapter.open();
			language = Locale.getDefault().getLanguage();
			dbPath = dbAdapter.getDBPath(context);
			softwareVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public SynchronizationService() {
	}
	
	public SynchronizationService(Context context) {
		this.context = context;
		communicationHandler = null;
		initContext();
	}
	
	/**
	 * adds params to url in httpUrlConnection
	 * @param params
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getQuery(Map<String,Object> params)
	{
		StringBuilder postData = new StringBuilder();
		try {
			for (Map.Entry<String,Object> param : params.entrySet()) {
				if (postData.length() != 0) {
					postData.append('&');
				}
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
		} catch (Exception ex) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - getQuery -" + ex.getMessage());
		}

		return postData.toString();
	}

	private InputStream serverExchange(URL url, Map<String,Object> params, int action) {
		InputStream in = null;
		OutputStream out = null;
		BufferedWriter writer = null;
		try {
			if (action == cConnectAction) {
				CookieManager cookieManager = new CookieManager();
				CookieHandler.setDefault(cookieManager);
			}
			conn = (HttpsURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(30000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setUseCaches(false);
			out = conn.getOutputStream();
			if (params != null) {
				String data = getQuery(params);
				writer = new BufferedWriter(
						new OutputStreamWriter(out, "UTF-8"), data.length());
				writer.write(data);
				writer.flush();
				writer.close();
			}
			in = conn.getInputStream();
			if (action == cDisconnectAction) {
				conn.disconnect();
				out.close();
				in.close();
			}
		} catch (Exception ex) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - serverExchange -" + ex.getMessage());
		}
		return in;
	}

	private String exportLogcat() {
		File path = context.getExternalFilesDir(null);
		String file_name = "logcat";
		File file = new File(path, file_name);
		try {
			tools.ExportLogcat( file );
		} catch (Exception e) {

		}
		return (path + File.separator + file_name);
	}


	@SuppressLint("NewApi")
	private int connect() {
		int status = cComRequestTimeout;
		try {
			user = dbAdapter.getUserByLogin(tools.getDeviceUID(context));
			Wl wl = dbAdapter.getWl(1L);

			if (user != null) {
				Map<String,Object> params = new LinkedHashMap<>();
				params.put("userid", user.getUserid());
				params.put("password", user.getPassword());
				params.put("SWversion", softwareVersion);
				params.put("lang", "fr");
				if (wl != null) {
					params.put("wlSteps", wl.getStatus());
				}
				URL url = new URL(urlServer + urlLogin);
				InputStream inputStream = serverExchange(url, params, cConnectAction);
				if (inputStream != null) {
					xmlTools = new XmlTools();
					status = xmlTools.decodeResponse(inputStream);
				}
			}
		}
		catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - connect -" + e.getMessage());
		}
		return status;
	}

	private int adminConnect() {
		int status = cComClientMethodFailure;
		try {
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("userid", admin.getLogin());
			params.put("password", admin.getPwd());

			URL url = new URL(urlServer + urlLogin);
			InputStream inputStream = serverExchange(url, params, cConnectAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - adminConnect -" + e.getMessage());
		}
		return status;
	}

	private int disconnect() {
		int status = cComClientMethodFailure;
		try {
			URL url = new URL(urlServer + urlLogout);
			InputStream inputStream = serverExchange(url, null, cDisconnectAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - disconnect -" + e.getMessage());
		}
		return status;
	}
	
	@SuppressLint("NewApi")
	private int init() {
		int status = cComClientMethodFailure;
		try {
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("idprovider", String.valueOf(getProviderId()));
			params.put("type", String.valueOf(deviceType));
			params.put("connection", String.valueOf(communicationType));

			URL url = new URL(urlServer + urlInit);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - init -" + e.getMessage());
		}
		return status;
	}


	@SuppressLint("NewApi")
	private int initTotalWhiteList() {
		int status = cComClientMethodFailure;
		int numSequence = 1; // does not matter

		try {
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("numsequence", String.valueOf(numSequence));
			URL url = new URL(urlServer + urlinitWL);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeWLResponse(inputStream);
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - initTotalWhiteList -" + e.getMessage());
		}
		return status;
	}

	@SuppressLint("NewApi")
	private int initPartialWhiteList() {
		int status = cComClientMethodFailure;
		int numSequence;

		Tablet tablet = dbAdapter.getTablet(1L);
		if (tablet == null) {
			numSequence = 1; 
		} else {
			numSequence = tablet.getNumSequence();
		}
		try {

            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("numsequence", String.valueOf(numSequence));
			URL url = new URL(urlServer + urlinitPartialWL);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodePartialWLResponse(inputStream);
				}
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - initPartialWhiteList -" + e.getMessage());
		}
		return status;
	}
	
	@SuppressLint("NewApi")
	private int getTotalWhiteList(int step) {
		int status = cComClientMethodFailure;
		try {

            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("numsequence", String.valueOf(step));
			URL url = new URL(urlServer + urlgetWL);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeWLTotal();
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - getTotalWhiteList -" + e.getMessage());
			status = cComClientExceptionFailure;
		}
		return status;
	}
	
	
	@SuppressLint("NewApi")
	private int getPartialWhiteList(int step) {
		int status = cComClientMethodFailure;
		try {
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("numsequence", String.valueOf(step));
			URL url = new URL(urlServer + urlgetWL);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeWLPartial();
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - getPartialWhiteList -" + e.getMessage());
			statusCom = cComClientExceptionFailure;
		}
		return status;
	}

	@SuppressLint("NewApi")
	private int getParam() {
		int status = cComClientMethodFailure;
		try {
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			URL url = new URL(urlServer + urlParam);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeParam();
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - getParam -" + e.getMessage());
		}
		return status;
	}
	
	@SuppressLint("NewApi")
	private int getUser() {
		int status = cComClientMethodFailure;
		try {
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			URL url = new URL(urlServer + urlUser);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeUser();
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - getUser -" + e.getMessage());
		}
		return status;
	}

	@SuppressLint({ "InlinedApi", "NewApi" })
	private int downloadMsgs() {
		int status = cComClientMethodFailure;
		try {
			String lang = Locale.getDefault().getLanguage();
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("language", lang);
			URL url = new URL(urlServer + urlMessage);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeMessages();
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - downloadMsgs -" + e.getMessage());
		}
		return status;
	}

	private int downloadSupport() {
		int status = cComClientMethodFailure;
		try {
			String lang = Locale.getDefault().getLanguage();
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("iduser", tools.getUserLogged(context));
			URL url = new URL(urlServer + urlSupport);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeSupport();
				} else if (status == cComNoMsgFound) {
					status = cComOK;
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - downloadMsgs -" + e.getMessage());
		}
		return status;
	}

	@SuppressLint("NewApi")
	private int downloadStock() {
		int status = cComClientMethodFailure;
		try {

            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			URL url = new URL(urlServer + urlStock);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					status = xmlTools.decodeStock();
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - downloadStock -" + e.getMessage());
		}
		return status;
	}
	
	private int checkNumotipass() {
		int status = cComClientMethodFailure;
		try {
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("numotipass", String.valueOf(numotipass));
			URL url = new URL(urlServer + urlCheck);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
				if (status == cComOK) {
					otipass = xmlTools.decodeCheckNumOtipass();
				}
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - checkNumotipass -" + e.getMessage());
		}
		return status;
		
	}

    private int checkSerial() {
        int status = cComClientMethodFailure;
        try {
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("serial", String.valueOf(serial));
            URL url = new URL(urlServer + urlCheckSerial);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
            if (inputStream != null) {
                xmlTools = new XmlTools();
                status = xmlTools.decodeResponse(inputStream);
                if (status == cComOK) {
                    otipass = xmlTools.decodeCheckNumOtipass();
                }
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, SynchronizationService.class.getName() + " - checkNumotipass -" + e.getMessage());
        }
        return status;

    }

    private int closeSupport(){
		int status = cComClientMethodFailure;
		try {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			int idrequest           = prefs.getInt(Constants.ID_REQUEST_KEY, 0);
			int idparent            = prefs.getInt(Constants.ID_PARENT_KEY, 0);
			int idUser              = prefs.getInt(Constants.USER_KEY, 0);

			URL url = new URL(urlServer + urlCloseSupport);
            Map<String,Object> params = new LinkedHashMap<>();
			params.put("serial", tools.getDeviceUID(context));
			params.put("idrequest", String.valueOf(idrequest));
			params.put("idparent", String.valueOf(idparent));
			params.put("iduser", String.valueOf(idUser));
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			if (inputStream != null) {
				xmlTools = new XmlTools();
				status = xmlTools.decodeResponse(inputStream);
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - closeSupport -" + e.getMessage());
		}
		return status;
	}

	private int upload() {

		int status = cComClientMethodFailure;
		try {
			URL url = new URL(urlServer + urlUpload);
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("list", xmlUpload);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			status = xmlTools.decodeResponse(inputStream);
			if (status == cComOK) {
				status = xmlTools.decodeUpdate();
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - upload -" + e.getMessage());
		}
		return status;
	}

	private int uploadEntry() {
		int status = cComClientMethodFailure;

		try {
			URL url = new URL(urlServer + urlEntry);
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("list", xmlUpload);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			status = xmlTools.decodeResponse(inputStream);
			if (status == cComOK) {
				status = xmlTools.decodeEntry();
			}

		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - uploadEntry -" + e.getMessage());
		}
		return status;

	}

	private int uploadBug() {
		int status = cComClientMethodFailure;

		try {
			URL url = new URL(urlServer + urlBug);
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("list", xmlUpload);
			InputStream inputStream = serverExchange(url, params, cExchangeAction);
			status = xmlTools.decodeResponse(inputStream);
			if (status == cComOK) {
				status = xmlTools.decodeBug();
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - uploadBug -" + e.getMessage());
		}
		return status;

	}
	public int deleteUploadedBug() {
		int status = SynchronizationService.cComOK;
		long id;
		List<Integer> list = xmlTools.getIdBugList();
		Iterator<Integer> it = list.iterator();
		while(it.hasNext()) {
			id = (long)it.next();
			dbAdapter.deleteBug(id);
		}
		return status;
	}

	private void buildBug() {
		bugList = dbAdapter.getBugList();
		XmlTools xmlTools = new XmlTools();
		xmlUpload = xmlTools.buildBugXml(bugList, context);
	}

	private int uploadBugStep(){
		int statusCom = cComOK;

		buildBug();
		if (!xmlUpload.isEmpty()) {
			statusCom = uploadBug();
			// delete all processed ids
			deleteUploadedBug();
		}
		return statusCom;
	}

	private int sendFile(String filePath, String iFileName ){
		int status = cComClientMethodFailure;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		DataOutputStream dataOutputStream;
		int bytesRead, bytesAvailable, bufferSize;
		int maxBufferSize = 1 * 1024 * 1024;
		byte[] buffer;

		try
		{
			FileInputStream fileInputStream = new FileInputStream(filePath);
			URL url = new URL(urlServer + urlUploadFile);
			conn = (HttpsURLConnection) url.openConnection();

			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploaded_file", filePath);
			//creating new dataoutputstream
			dataOutputStream = new DataOutputStream(conn.getOutputStream());

			//writing bytes to data outputstream
			dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
			dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
					+ filePath + "\"" + lineEnd);

			dataOutputStream.writeBytes(lineEnd);

			//returns no. of bytes present in fileInputStream
			bytesAvailable = fileInputStream.available();
			//selecting the buffer size as minimum of available bytes or 1 MB
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			//setting the buffer as byte array of size of bufferSize
			buffer = new byte[bufferSize];

			//reads bytes from FileInputStream(from 0th index of buffer to buffersize)
			bytesRead = fileInputStream.read(buffer,0,bufferSize);

			//loop repeats till bytesRead = -1, i.e., no bytes are left to read
			while (bytesRead > 0){
				//write the bytes read from inputstream
				dataOutputStream.write(buffer,0,bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable,maxBufferSize);
				bytesRead = fileInputStream.read(buffer,0,bufferSize);
			}

			dataOutputStream.writeBytes(lineEnd);
			dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				status = xmlTools.decodeResponse(conn.getInputStream());
			}

			//closing the input and output streams
			fileInputStream.close();
			dataOutputStream.flush();
			dataOutputStream.close();		}
		catch (Exception ex)
		{
			Log.e(Constants.TAG, "URL error: " + ex.getMessage(), ex);
		}
		return status;
	}

	private void buildUpload() {
		updateList = dbAdapter.getUpdateList();
		XmlTools xmlTools = new XmlTools();
		xmlUpload = xmlTools.buildUploadXml(updateList, context);
	}

	private void buildEntry() {
		entryList = dbAdapter.getUnloadedEntries();
		XmlTools xmlTools = new XmlTools();
		xmlUpload = xmlTools.buildEntryXml(entryList, context);
	}

	private void deleteUploadedData() {
		Entry entry;
		Update update;
		Warning warning;
		if (synchronizationType == SynchronizationService.cNightUploadSynchronization) {
			// during night synchronization, delete all entries 
			dbAdapter.flushTable(SQLiteHelper.ENTRY_TABLE);
		} else {
			// else set the uploaded flag to detect today entries
			if (entryList.size() > 0) {
				for (int i=0; i<entryList.size(); i++) {
					entry = entryList.get(i);
					entry.setUploaded(true);
					dbAdapter.updateEntry(entry);
				}
			}
		}
		if (updateList.size() > 0) {
			for (int i=0; i<updateList.size(); i++) {
				update = updateList.get(i);
				dbAdapter.deleteUpdate(update.getId());
			}
		}
		if (warningList.size() > 0) {
			for (int i=0; i<warningList.size(); i++) {
				warning = warningList.get(i);
				dbAdapter.deleteWarning(warning.getId());
			}
		}

	}

	public int setUploadedEntry() {
		int status = SynchronizationService.cComOK;
		long id;
		Entry entry;
		List<Integer> list = xmlTools.getIdEntryList();
		Iterator<Integer> it = list.iterator();
		while(it.hasNext()) {
			id = (long)it.next();
			entry = dbAdapter.getEntry(id);
			entry.setUploaded(true);
			dbAdapter.updateEntry(entry);
		}
		return status;

	}

	public int deleteUploadedUpdate() {
		int status = SynchronizationService.cComOK;
		long id;
		List<Integer> list = xmlTools.getIdUpdateList();
		Iterator<Integer> it = list.iterator();
		while(it.hasNext()) {
			id = (long)it.next();
			dbAdapter.deleteUpdate(id);
		}
		return status;
	}

	@SuppressLint("NewApi")
	private int uploadStep() {
		int statusCom = cComOK;
		int step = cUploadStep;
		// first upload the data to the server
		getCommHandlerMessage(step);
		buildUpload();
		if (!xmlUpload.isEmpty()) {
			statusCom = upload();
			// delete all processed ids
			deleteUploadedUpdate();
		}
		step = cEntryStep;
		// second upload entry to the server
		getCommHandlerMessage(step);
		buildEntry();
		if (!xmlUpload.isEmpty()) {
			statusCom = uploadEntry();
			// set these entries as uploaded
			setUploadedEntry();
		}
		return statusCom;
	}

	private void getCommHandlerMessage(int step) {
		if (communicationHandler != null) {
			Message msg = communicationHandler.obtainMessage(cProgressMsg, step, 0);
			communicationHandler.sendMessage(msg);
		}
	}

	private void getCommHandlerMessage2(int step, int nb) {
		if (communicationHandler != null) {
			Message msg = communicationHandler.obtainMessage(cProgressMsg, step, nb);
			communicationHandler.sendMessage(msg);
		}
	}

	private Wl getWLState() {
		Wl wl = null;
		Calendar now = Calendar.getInstance(), dateWL;
		try {
			wl = dbAdapter.getWl(1L);
			if (wl != null) {
				dateWL = tools.setCalendar(wl.getDate());
				dateWL.add(Calendar.MINUTE, 5);
				if (dateWL.before(now)) {
					wl.setStatus(0);
				}
			} else {
				wl = new Wl(tools.formatSQLDate(now), 0, 0, 0, 0);
			}
		} catch (Exception e) {
		}

		return wl;
	}

	private void initWLState(int nbSteps, int nbCards, int step) {
		// at the beginning of the WP download, save the number of steps and cards, the numsequence
		String date = tools.formatNow(Constants.SQL_FULL_DATE_FORMAT);
		try {
			dbAdapter.deleteWl(1L);
			dbAdapter.insertWl(new Wl(1, date, nbSteps, nbCards, xmlTools.getNumSequence(), step));
		} catch (Exception e) {
		}
	}

	private void updateWLState(int step) {
		// after each successful download step, save the step
		String date = tools.formatNow(Constants.SQL_FULL_DATE_FORMAT);
		Wl wl = dbAdapter.getWl(1L);
		wl.setDate(date);
		wl.setStatus(step);
		try {
			dbAdapter.updateWl(wl);
		} catch (Exception e) {
		}
	}

	private int getWLTotalStep() {
		int statusCom = cComOK;
		int step, nb, wlStep, numSequence = 0;
		Message msg;
		PowerManager.WakeLock wlock = null;
		wlNbSteps =  wlNbCards = 0;
		List<Otipass> list = null;
		List<Usage> listUsage = null;
		Wl wlState;

		try {

			if (context == null) {
				context = getApplicationContext();
			}

			wlState = getWLState();
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wlock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, Constants.TAG);
			wlock.acquire();
			long startTime = System.currentTimeMillis();

			step = cInitWLStep0;
			if (communicationHandler != null) {
				msg = communicationHandler.obtainMessage(cProgressMsg, step, 0);
				communicationHandler.sendMessage(msg);
			}

			if ((wlState.getStatus() == 0) || (wlState.getStatus() == Constants.WL_DONE)) {
				// start a new download from scratch
				statusCom = initTotalWhiteList();
				if (statusCom == cComOK) {
					wlNbSteps = xmlTools.getWlNbSteps();
					wlNbCards = xmlTools.getWlNbCards();
					initWLState(wlNbSteps, wlNbCards, 0);
					// flush the Otipass table
					dbAdapter.flushTable(SQLiteHelper.OTIPASS_TABLE);
					// flush the use_pass table
					dbAdapter.flushTable(SQLiteHelper.USE_PASS_TABLE);
					numSequence = xmlTools.getNumSequence();
				}
				wlStep = 1;
			} else {
				// start the download from where it stopped
				wlNbSteps = wlState.getNbsteps();
				wlNbCards = wlState.getNbcards();
				numSequence = wlState.getNumsequence();
				wlStep = wlState.getStatus();
			}

			if (statusCom == cComOK) {
				//wlNbSteps = xmlTools.getWlNbSteps();
				//wlNbCards = xmlTools.getWlNbCards();
				if (wlNbSteps > 0) {
					if (communicationHandler != null) {
						step = cInitWLStep1;
						msg = communicationHandler.obtainMessage(cProgressMsg, step, wlNbCards);
						communicationHandler.sendMessage(msg);
						step = cInitWLStep2;
						msg = communicationHandler.obtainMessage(cProgressMsg, step, wlNbSteps);
						communicationHandler.sendMessage(msg);
					}


					// the white list is sent in several parts
					boolean end = false;
					int cptError = 0;
					while (!end) {
						if (communicationHandler != null) {
							step = cgetTotalWLStep;
							msg = communicationHandler.obtainMessage(cProgressMsg, step, wlStep);
							communicationHandler.sendMessage(msg);
						}

						// get the White list part
						statusCom = getTotalWhiteList(wlStep);
						if (statusCom == cComOK) {
							list = xmlTools.getOtipassList();
							if (list.size() > 0) {
								// store the wl part in the database
								nb = dbAdapter.insertOtipassList(list);
								if (nb != list.size()) {
									statusCom = cComClientSQLiteFailure;
								}
							}
						}
						if (statusCom == cComOK) {
							// no error
							cptError = 0;
							if (wlStep < wlNbSteps) {
								// save the step
								updateWLState(wlStep);
								wlStep++;
							} else {
								end = true;
								// it's over, reset the WL state
								updateWLState(Constants.WL_DONE);
							}
						} else  {
							// start again
							if (++cptError > 10 ) {
								end = true;
							} else {
								dbAdapter.deleteOtipassList(list);
								Thread.sleep(1000);
							}
						}
					}
				}

				if (statusCom == cComOK) {
					Tablet tablet;
					if ((tablet = dbAdapter.getTablet(1L)) == null) {
						tablet = dbAdapter.insertTablet(numSequence, "", "");
					} else {
						tablet.setNumSequence(numSequence);
						dbAdapter.updateTablet(tablet);
					}
				}
			}
			long stopTime = System.currentTimeMillis();
			Log.i(Constants.TAG, "DWNL WL:" + String.valueOf(stopTime - startTime));
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - getWLTotalStep -" + e.getMessage());
			statusCom = cComClientExceptionFailure;
		}
		finally {
			if (wlock != null) {
				wlock.release();
			}
		}
		return statusCom;
	}
	private int getWLPartialStep() {
		int statusCom = cComOK;
		int nb, wlStep;
		PowerManager pm;
		PowerManager.WakeLock wl = null;
		wlNbSteps =  wlNbCards = 0;
		try {
			if (context != null) {
				pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			}else{
				pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
			}

			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, Constants.TAG);
			wl.acquire();
			long startTime = System.currentTimeMillis();
			getCommHandlerMessage2(cInitPartialWLStep1, 0);
			statusCom = initPartialWhiteList();

			if (statusCom == cComOK) {
				wlNbSteps = xmlTools.getWlNbSteps();
				getCommHandlerMessage2(cInitPartialWLStep2, wlNbSteps);
				if (wlNbSteps > 0) {

					// the white list is sent in several parts
					int cptError = 0;
					boolean end = false;
					wlStep = 1;
					while (!end) {
						getCommHandlerMessage2(cgetPartialWLStep, wlStep);
						// get the White list part
						statusCom = getPartialWhiteList(wlStep);
						if (statusCom == cComOK) {
							// otipass creations
							List<Create> listC = xmlTools.getCreateList();
							if (listC.size() > 0) {
								nb = dbAdapter.createOtipassList(listC);
								if (nb != listC.size()) {
									statusCom = cComClientSQLiteFailure;
								}
							}	
						} 
						if (statusCom == cComOK) {
							// otipass updates
							List<Partial> list = xmlTools.getPartialList();
							if (list.size() > 0) {
								nb = dbAdapter.updateOtipassList(list);
								if (nb != list.size()) {
									statusCom = cComClientSQLiteFailure;
								}
							}	
						} 
						if (statusCom == cComOK) {
							// otipass srv
							List<PartialServiceCpt> listSrv = xmlTools.getPartialSrvCptList();
							if (listSrv.size() > 0) {
								nb = dbAdapter.updateOtipassSrvList(listSrv);
								if (nb != listSrv.size()) {
									statusCom = cComClientSQLiteFailure;
								}
							}	
						} 
						if (statusCom == cComOK) {
							// no error
							cptError = 0;
							if (wlStep < wlNbSteps) {
								wlStep++;
							} else {
								end = true;
							}
						} else  {
							// start again
							if (++cptError > 10 ) {
								end = true;
							} else {
								Thread.sleep(1000);
							}
						}
					}
				}

				if (statusCom == cComOK) {
					Tablet tablet;
					int numSequence = xmlTools.getNumSequence();
					tablet = dbAdapter.getTablet(1L);
					if (tablet == null) {
						tablet = dbAdapter.insertTablet(numSequence, "", "");
					} else if (numSequence > tablet.getNumSequence()) {
						tablet.setNumSequence(numSequence);
						dbAdapter.updateTablet(tablet);
					}
				}
			}
			long stopTime = System.currentTimeMillis();
			Log.i(Constants.TAG, "DWNL WL:" + String.valueOf(stopTime - startTime));			
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - getWLPartialStep -" + e.getMessage());
			statusCom = cComClientExceptionFailure;
		}
		finally {
			if (wl != null) {
				wl.release();
			}
		}
		return statusCom;
	}


	private int getUserStep() {
		int statusCom = cComOK;
		int step = cgetUserStep, nb;
		getCommHandlerMessage(step);
		statusCom = getUser();
		if (statusCom == cComOK) {
			step = cInsertUserStep;
			getCommHandlerMessage(step);
			List<User> list = xmlTools.getUserList();
			if (list.size() > 0) {
				dbAdapter.flushTable(SQLiteHelper.USER_TABLE);
				nb = dbAdapter.insertUserList(list);
				if (nb != list.size()) {
					statusCom = cComClientMethodFailure;
				}
			}
		}
		return statusCom;
	}

	private int getParamStep() {
		int statusCom = cComOK;
		int step = cgetParamStep;
		getCommHandlerMessage(step);
		statusCom = getParam();
		if (statusCom == cComOK) {
			step = cInsertParamStep;
			getCommHandlerMessage(step);
			Param param = xmlTools.getParam();
			if (param != null) {
				SynchroAlarm.setAlarm(context, param.getCall());
				try {
					String[] t = param.getName().split("-");
					if (t.length > 2) {
						int iddevice = Integer.valueOf(t[2].trim());
						tools.setDeviceId(context, iddevice);
					}
				} catch (Exception e) {}
				newParamsReceived = true;
				dbAdapter.flushTable(SQLiteHelper.PARAM_TABLE);
				dbAdapter.insertParamObject(param);
				serviceList = xmlTools.getServiceList();
				packageList = xmlTools.getPackageList();
				packageServiceList = xmlTools.getPackageServiceList();
				providerServiceList = xmlTools.getProviderServiceList();
				dbAdapter.flushTable(SQLiteHelper.SERVICE_TABLE);
				dbAdapter.flushTable(SQLiteHelper.PACKAGE_TABLE);
				dbAdapter.flushTable(SQLiteHelper.PACKAGE_SERVICE_TABLE);
				dbAdapter.flushTable(SQLiteHelper.PROVIDER_SERVICE_TABLE);
				dbAdapter.insertServiceList(serviceList);

				dbAdapter.insertPackageList(packageList);
				
				step = cgetParamStep1;
				getCommHandlerMessage(step);
				dbAdapter.insertPackageServiceList(packageServiceList);

				for (int i = 0; i < providerServiceList.size(); i++) {
					ProviderService ps = new ProviderService();
					ps = providerServiceList.get(i);
					dbAdapter.insertProviderService(ps);
				}

				if (synchronizationType != cInitSynchronization) {
					if (param.getDebug() == 1) {
						step = cUploadDBStep;
						getCommHandlerMessage(step);
						File file = new File(dbPath);
						if (file.exists()) {
							sendFile(dbPath, SQLiteHelper.DATABASE_NAME);
						}
						step = cUploadLogCatStep;
						getCommHandlerMessage(step);
						logcatPath = exportLogcat();
						file = new File(logcatPath);
						if (file.exists()) {
							sendFile(logcatPath, SQLiteHelper.DATABASE_NAME);
						}
					}
				}
			}
			Tablet tablet = dbAdapter.getTablet(1L);
			tablet.setDownloadTime(tools.formatNow(Constants.SQL_FULL_DATE_FORMAT));
			dbAdapter.updateTablet(tablet);

		}
		return statusCom;
	}

	private int getStockStep() {
		int statusCom = cComOK;
		int step = cStockStep, nb;
		getCommHandlerMessage(step);
		statusCom = downloadStock();
		if (statusCom == cComOK) {
			Stock stock = xmlTools.getStock();
			dbAdapter.flushTable(SQLiteHelper.STOCK_TABLE);
			if (stock != null) {
				dbAdapter.insertStockObject(stock);
			}
		}
		return statusCom;
	}

	private int getMessageStep() {
		int statusCom = cComOK;
		int step = cMessageStep, nb;
        boolean insertMessage;
		getCommHandlerMessage(step);
		statusCom = downloadMsgs();
		if (statusCom == cComOK) {
            // just to debug
            List<Msg> listDB = dbAdapter.getMessageList();
            // end just to debug
			// delete all non hidden and actual messages
			dbAdapter.deleteMessages();
			// get remaining hidden messages from DB
			listDB = dbAdapter.getMessageList();
			// get messages from server
			List<Msg> list = xmlTools.getMessageList();
			if (list.size() > 0) {
				if (listDB.size() == 0) {
					dbAdapter.insertMessageList(list);
				} else {
                    List<Integer> lRemove = new ArrayList<Integer>();
                    int index = 0;
                    for (Msg msg : list) {
                        insertMessage = true;
                        for (Msg msgDB : listDB) {
                            if (msgDB.getId() == msg.getId()) {
                                insertMessage = false;
                                Calendar calDB = Calendar.getInstance(), cal = Calendar.getInstance();
                                if (tools.setCalendar(msgDB.getEndDate()).before(tools.setCalendar(msg.getEndDate()))) {
                                    // show the message again if end date has been changed
                                    msg.setHidden(0);
                                    dbAdapter.updateMessage(msg);
                                } else {
                                    // delete the message if too old
									if (tools.setCalendar(msgDB.getEndDate()).before(cal)) {
										dbAdapter.deleteMessage(msg.getId());
									}
                                }
                                break;
                            }
                        }
                        if (insertMessage) {
                            dbAdapter.insertMessageObject(msg);
                        }
                        index++;
                    }
				}
			}
		}
		return statusCom;
	}

	private int getSupportStep() {
		int statusCom = cComOK;
		int step = cSupportStep, nb;
		boolean insertMessage;
		getCommHandlerMessage(step);
		statusCom = downloadSupport();
		if (statusCom == cComOK) {
			// get remaining hidden messages from DB
			List<Support> listDB = dbAdapter.getSupportList();
			// get messages from server
			List<Support> list = xmlTools.getSupportList();
			if (list.size() > 0) {
				if (listDB.size() == 0) {
					dbAdapter.insertSupportList(list);
				} else {
					List<Integer> lRemove = new ArrayList<Integer>();
					int index = 0;
					for (Support msg : list) {
						insertMessage = true;
						for (Support msgDB : listDB) {
							if (msgDB.getId() == msg.getId()) {
								insertMessage = false;
							}
						}
						if (insertMessage) {
							dbAdapter.insertSupportObject(msg);
						}
						index++;
					}
				}
			}
		}
		return statusCom;
	}


	public OtipassCard getCard() {
		return card;
	}

	public Otipass getCheckedCard(){
		return otipass;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}




	public int getStatus(){
		return statusCom;
	}

	
	@SuppressLint("NewApi")
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub

		super.onStart(intent, startId);
		this.intent = intent;
		context = SynchronizationService.this;
		initContext();
		startTask();
	}
	
	private void endComm() {
		if (communicationHandler != null) {
			if (statusCom == cComOK) {
				msg = communicationHandler.obtainMessage(cEndMsg, statusCom , 0);
			} else {
				msg = communicationHandler.obtainMessage(cEndMsg, statusCom , step);
			}
			communicationHandler.sendMessage(msg);
		}
		
	}
	private void endWait() {
		if (communicationHandler != null) {
			msg = communicationHandler.obtainMessage(cEndMsg, statusCom , 0);
			communicationHandler.sendMessage(msg);
		}

	}

	// main communication process steps
	@SuppressLint("NewApi")
	public void SynchroHandler(String action){
		try {
			tools.setServiceState(context, tools.cSingleCall);
			switch(action){
	
			case (Constants.DO_INIT):{
				statusCom = adminConnect();
				if (statusCom == cComOK) {
					step = cInitStep;
					msg = communicationHandler.obtainMessage(cProgressMsg, step, 0);
					communicationHandler.sendMessage(msg);
					statusCom = init();
				}
				if (statusCom == cComOK) {
					statusCom = getWLTotalStep();
				}
				if (statusCom == cComOK) {
					step = cgetUserStep;
					statusCom = getUserStep();
				}
				if (statusCom == cComOK) {
					step = cgetParamStep;
					statusCom = getParamStep();
				}
	
				disconnect();
				endComm();
			}
			break;
	
			case (Constants.DO_PARTIAL_SYNCHRO):{
				System.out.println("Partial synchro launched");
				statusCom = connect();
				if (statusCom == cComOK) {
					step = cUploadStep;
					statusCom = uploadStep();
				}
				if (statusCom == cComOK) {
					statusCom = getWLPartialStep();
				}
				if (statusCom == cComOK) {
                    step = cgetUserStep;
					statusCom = getUserStep();
				}
				if (statusCom == cComOK) {
                    step = cgetParamStep;
					statusCom = getParamStep();
				}
                if (statusCom == cComOK) {
                    step = cBugStep;
                    statusCom = uploadBugStep();
                }
				if (statusCom == cComOK) {
					step = cSupportStep;
					statusCom = getSupportStep();
				}
				disconnect();
	
				endComm();
				System.out.println("Partial synchro done");
			}
			break;
	
			case (Constants.DO_TOTAL_SYNCHRO):{
				System.out.println("DO_TOTAL_SYNCHRO is called");
				statusCom = connect();
				if (statusCom == cComOK) {
					step = cUploadStep;
					statusCom = uploadStep();
	
				}
				if (statusCom == cComOK) {
					statusCom = getWLTotalStep();
				}
				if (statusCom == cComOK) {
					step = cgetUserStep;
					statusCom = getUserStep();
				}
				if (statusCom == cComOK) {
					step = cgetParamStep;
					statusCom = getParamStep();
				}
                if (statusCom == cComOK) {
                    step = cBugStep;
                    statusCom = uploadBugStep();
                }
				if (statusCom == cComOK) {
					step = cSupportStep;
					statusCom = getSupportStep();
				}
				disconnect();
	
				endComm();
				System.out.println("DO_TOTAL_SYNCHRO is done");
				
			}
			break;
	
			case (Constants.DO_NIGHT_DOWNLOAD):{
				System.out.println("DO_NIGHT_DOWNLOAD is called");
				tools.setServiceState(context, tools.cNightCall);
				statusCom = connect();
				if (statusCom == cComOK) {
					statusCom = getWLPartialStep();
				}
				if (statusCom == cComOK) {
					step = cgetUserStep;
					statusCom = getUserStep();
				}
				if (statusCom == cComOK) {
					step = cgetParamStep;
					statusCom = getParamStep();
				}
				if (statusCom == cComOK) {
					step = cSupportStep;
					statusCom = getSupportStep();
				}

				disconnect();
				System.out.println("DO_NIGHT_DOWNLOAD is done");
			}
			break;
	
			case (Constants.DO_NIGHT_UPLOAD):{
				System.out.println("DO_NIGHT_UPLOAD is called");
				tools.setServiceState(context, tools.cNightCall);
				statusCom = connect();
				if (statusCom == cComOK) {
					step = cUploadStep;
					statusCom = uploadStep();
				}
                if (statusCom == cComOK) {
                    step = cBugStep;
                    statusCom = uploadBugStep();
                }

				disconnect();
				System.out.println("DO_NIGHT_UPLOAD is done");
				Tablet tablet = dbAdapter.getTablet(1L);
				tablet.setUploadTime(tools.formatNow(Constants.SQL_FULL_DATE_FORMAT));
				dbAdapter.updateTablet(tablet);

			}
			break;
	
			case (Constants.DO_PERIODIC_SYNCHRO):{
				// communication result will be displayed in the status bar
				System.out.println("DO_PERIODIC_SYNCHRO is called");
				tools.setServiceState(context, tools.cPeriodicCall);
	
				statusCom = connect();
	
				if (statusCom == cComOK) {
					statusCom = getWLPartialStep();
				}
	
				disconnect();
				System.out.println("DO_PERIODIC_SYNCHRO is done");
				msg = communicationHandler.obtainMessage(cEndMsg, statusCom , 0);
				communicationHandler.sendMessage(msg);
			}
			break;
	
			case (Constants.GET_MESSAGES):{
				statusCom = connect();
				if (statusCom == cComOK) {
					step = cMessageStep;
					statusCom = getSupportStep();
				}
				if (statusCom == cComOK) {
					step = cgetUserStep;
					statusCom = getUserStep();
				}
				if (statusCom == cComOK) {
					step = cgetParamStep;
					statusCom = getParamStep();
				}
				disconnect();
				endWait();
			}
			break;
	
			case (Constants.SEND_UPDATES):{
				statusCom = connect();
				if (statusCom == cComOK) {
					step = cUploadStep;
					statusCom = uploadStep();
					Log.d("sending updates", "ok");
				}
				if (statusCom == cComOK) {
					step = cgetWLStep;
					Log.d("getting partial list", "ok");
					statusCom = getWLPartialStep();
				}
                if (statusCom == cComOK) {
                    step = cBugStep;
                    statusCom = uploadBugStep();
                }
				if (statusCom == cComOK) {
					step = cSupportStep;
					statusCom = getSupportStep();
				}
				disconnect();
				endComm();
				break;
			}
			case (Constants.CHECK_OTIPASS):{
				statusCom = connect();
				if (statusCom == cComOK) {
					statusCom = getWLPartialStep();
					statusCom = checkNumotipass();
				}
				disconnect();
			}
			break;

            case (Constants.CHECK_SERIAL):{
                statusCom = connect();
				if (statusCom == cComOK) {
					statusCom = getWLPartialStep();
					statusCom = checkSerial();
				}
                disconnect();
            }
            break;
            case (Constants.DO_BUG_UPLOAD):{
                System.out.println("DO_BUG_UPLOAD is called");
                tools.setServiceState(context, tools.cSingleCall);
                statusCom = connect();
                if (statusCom == cComOK) {
                    step = cUploadStep;
                    statusCom = uploadStep();
                }
                if (statusCom == cComOK) {
                    step = cBugStep;
                    statusCom = uploadBugStep();
                }

                disconnect();
                System.out.println("DO_BUG_UPLOAD is done");

            }
            break;

            }
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - synchroHandler -" + e.getMessage());			
		}
		finally {
			tools.setServiceState(context, tools.cIdle);
		}
	}
	
	@SuppressLint("NewApi")
	private void startTask() {
		Task task = new Task();
		task.execute();
	}

	public void start(String action, Handler handler) {
		communicationHandler = handler;
		tools.setServiceState(context, tools.cCommmunicationPending);		
		Task task = new Task();
		task.execute(action);
	}

    public void start(Context context, String action) {
        communicationHandler = null;
        tools.setServiceState(context, tools.cCommmunicationPending);
        Task task = new Task();
        task.execute(action);
    }

	public void start(String action, int numotipass) {
		this.numotipass = numotipass;
		tools.setServiceState(context, tools.cCommmunicationPending);		
		Task task = new Task();
		task.execute(action);
	}

    public void start(String action, String serial) {
        this.serial = serial;
        tools.setServiceState(context, tools.cCommmunicationPending);
        Task task = new Task();
        task.execute(action);
    }

    /**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection}
     *
     */
    public static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // the communication service is built on an AsyncTask running all exchanges with the server
	private class Task extends AsyncTask<String, Void, Void> {

		public Task() {

		}
		
		@Override
		protected void onPreExecute() {
		}

		protected Void doInBackground(String... params) {
			if (params.length > 0) {
				SynchroHandler(params[0]);
			} 
			return null;
		}
	}


}


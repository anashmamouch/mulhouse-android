/**
 ================================================================================

 OTIPASS
 models package

 @author ED ($Author: ede $)

 @version $Rev: 6455 $
 $Id: Constants.java 6455 2016-07-08 11:18:45Z ede $

 ================================================================================
 */
package models;

public class Constants {
	public static final String TAG = "Mulhouse";
	public static final String PROJECT = "mulhouse";
    // this defines if the pupi only is read or if NDEF record is read from NFC
    public static final boolean NDEF_ENABLED = false;
	// this defines if the otipass number is shown
	public static final boolean SHOW_OTIPASS_NUM = true;
	// this defines the number of digits pupi should hold
	public static final String PUPI_FORMAT = "";

	// this defines if the serial is ciphered
	public static final boolean SERIAL_CIPHERED = false;
	public static final String CIPHER_ALGO = "SHA-256";
	public static final String SERIAL_FORMAT = "d ddd ddd ddd";

	// tablet models
	public static final String NEXUS_7 = "Nexus 7";
	public static final String SHIELD = "SHIELD Tablet K1";
	public static final String FAMOCO = "FX100,7";

    // this value is returned by external card reader if pupi is not known
    public static final String UNKNOWN_PASS = "inconnu";

    public static final int PASS_CREATED = 1;
	public static final int PASS_INACTIVE = 2;
	public static final int PASS_ACTIVE = 3;
	public static final int PASS_EXPIRED = 4;
	public static final int PASS_INVALID = 5;
	public static final int PASS_UNDEFINED = 6;

	public static final int PASS_TEST = 0;
	public static final int PASS_INITIAL = 1;

	// user profiles
	public static final int USR_OTIPASS_SUPERADMIN = 12;
	public static final int USR_OTIPASS_ADMIN = 11;
	public static final int USR_ISSUER_ADMIN = 10;
	public static final int USR_ISSUER_OP = 9;
	public static final int USR_SITES_MANAGER = 8;
	public static final int USR_MANAGER = 7;
	public static final int USR_STOCK_MANAGER = 6;
	public static final int USR_CASHIER = 5;
	public static final int USR_CONTROLLER = 4; 
	public static final int USR_PROVIDER = 3;
	public static final int USR_CLIENT = 2;
	public static final int USR_BUYER = 1;
	public static final int USR_GUEST = 0;

	// sites categories
	public static final int SITE_PROVIDER = 3;
	public static final int SITE_POS = 4;
	public static final int SITE_PROVIDER_POS = 5;

	// date formats
	public static final String SQL_SHORT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String SQL_FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String FULL_DATE_FORMAT_FR = "dd/MM/yyyy HH:mm:ss";
	public static final String EN_DATE_FORMAT = "yyyy-MM-dd";
	public static final String FR_DATE_FORMAT = "dd/MM/yyyy";
	public static final String DE_DATE_FORMAT = "dd.MM.yyyy";
	public static final String EXPIRY_DATE = " 23:59:59";
	public static final String DAY_DATE ="dd";
	public static final String TIME_FORMAT = "HH:mm:ss";
	public static final String FULL_DATE_FORMAT_FR2 = "dd/MM/yyyy à HH:mm:ss";

	// card event types
	public static final int NOT_CITY_PASS = 1;
	public static final int CITY_PASS = 2;
	
	// functions
	public static final int FUNC_SCAN_PASS = 1;
	public static final int FUNC_SYNCHRO = 2;
	public static final int FUNC_SCAN_MANUALLY = 3;

	// WebView Actions
	public static final String REQUEST_WV = "request";
	public static final String STATS_ENTRY_WV = "stats_entry";
	public static final String STATS_SALE_WV = "stats_sale";
	public static final String STOCK_WV = "stock_command";

	// for the alarm service
	public static final String WAKE_UP_STR = "WAKE_UP";
	public static final String ALARM_UPLOAD_STR = "UPLOAD";
	public static final String ALARM_DOWNLOAD_STR = "DOWNLOAD";
	public static final String ALARM_SYNCHRO_PERIOD = "alarm_synchro_period";
	public static final String ALARM_UP_PERIOD = "alarm_up_period";
	public static final int SYNCHRO_PERIOD        = 2;

	// cancel delay
	public static final int CANCEL_DELAY          = 15;
	
	// action (entry or sale) type
	public static final int ENTRY_TYPE 			  = 1;
	public static final int SALE_TYPE  			  = 2;

	// action (entry or sale) type
	public static final int COUNTER_SERVICE 	  = 1;
	public static final int ILLIMITED_SERVICE	  = 2;

	// flag for complete download
	public static final int WL_DONE = 10000;
	
	// updates type
	public static final int UPD_CANCEL            = 8;
	public static final int UPD_CANCEL_ENTRY      = 10;
	
	// shared prefs
	public static final String PARAMS_PREFS = "PARAMS PREFS";
	public static final String PARAMS_DURATION = "duration";

	// Sharedpreferences key
	public static final String USER_KEY			  = "user_key";
	public static final String SERVICE_KEY        = "service_key";
	public static final String PERIODIC_CALL      = "periodic_call";
	public static final String ID_PARENT_KEY      = "idparent_key";
	public static final String ID_REQUEST_KEY     = "id_request_key";
	public static final String BUG_ACTIVITY_KEY    = "bug_activity_key";
	public static final String DEVICE_ID_KEY    = "device_id_key";
	public static final String EXTERN_CALL_KEY    = "extern";
	public static final String RFID_LOCK_KEY    = "rfid_lock_key";



	// fragment tags
	public static final String ADMIN_TAG = "admin_fragment";
	public static final String USER_TAG = "user_fragment";
	public static final String HOME_TAG = "home_fragment";
	public static final String WEBVIEW_TAG = "webview_fragment";
	public static final String SCAN_TAG = "scan_fragment";
	public static final String SHOWCARD_TAG = "showcard_fragment";
	public static final String CANCEL_TAG = "cancel_fragment";
    public static final String DRAWER_TAG = "drawer_activity";

	// SYNCHRONIZATION SERVICE'S ACTIONS
	public static final String SEND_ENTRY         = "send_entry";
	public static final String SEND_UPDATES       = "send_updates";
	public static final String GET_MESSAGES       = "get_messages";
	public static final String GET_SOFTWARE       = "get_software";
	public static final String GET_PARTIAL_WL     = "get_partial_wl";
	public static final String DO_PARTIAL_SYNCHRO = "do_partial_synchro";
	public static final String DO_TOTAL_SYNCHRO   = "do_total_synchro";
	public static final String DO_INIT            = "do_init";
	public static final String CHECK_OTIPASS      = "check_otipass";
	public static final String CHECK_SERIAL      = "check_serial";
	public static final String DO_NIGHT_DOWNLOAD  = "do_night_download";
	public static final String DO_NIGHT_UPLOAD    = "do_night_upload";
	public static final String DO_PERIODIC_SYNCHRO= "do_periodic_synchro";
	public static final String CLOSE_SUPPORT      = "close_support";
	public static final String DO_BUG_UPLOAD      = "do_bug_upload";

	// action constants for returnListener
	public static final int ACTION_IDLE = 0;
	public static final int ACTION_SAV = 1;

	// url web access
	public static final String URL_WEB_ACCESS   = ".otipass.net/ot/console/" + PROJECT + "/auth/viamobile/lang";

 	public static final String plateform = "www";
//	public static final String plateform = "test";
}

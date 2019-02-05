/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: DrawerActivity.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.neowave.android.usb.Card;
import com.neowave.android.usb.CcidConstants;
import com.neowave.android.usb.CcidController;
import com.neowave.android.usb.CcidDevice;
import com.otipass.nfc.Nfc;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.Param;
import com.otipass.sql.ProviderService;
import com.otipass.sql.User;
import com.otipass.synchronization.SynchroAlarm;
import com.otipass.synchronization.SynchronizationService;
import com.otipass.tools.Callback;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.OtipassCard;
import com.otipass.tools.Services;
import com.otipass.tools.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Constants;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Callback.OnReturnListener, Callback.OnUserIdentificationListener, Callback.OnInputListener,
        Callback.OnScanRequestListener, Callback.OnCardEventListener, ServiceSelection.NoticeDialogListener {
    private static final int cIdle = 0;
    private static final int cInitialized = 1;
    private static final int cConnected = 2;

    // Activities
    private static final int cHomeActivity = 1;
    private static final int cAdminActivity = 2;
    private static final int cIdentificationActivity = 3;
    private static final int cScanActivity = 4;
    private static final int cShowCardActivity = 5;
    private static final int cStatsActivity = 6;
    private static final int cSynchroActivity = 7;
    private static final int cFullSynchroActivity = 8;
    private static final int cScanManuallyActivity = 9;
    private static final int cCancelActivity = 10;
    private static final int cSAVActivity = 11;

    // NFC process
    private static final int cNFCidle = 0;
    private static final int cNFCactive = 1;

    private Toolbar toolbar;
    private Footer footer;
    private DbAdapter dbAdapter;
    private int idUser;
    private int currentActivity;
    private int state = cIdle;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawer;
    private NfcAdapter nfcAdapter = null;
    private PendingIntent pendingIntent;
    private int nfcState = cNFCidle;
    private OtipassCard card = null;
    private NavigationView navigationView;
    private static boolean nfcIntentCalled;
    private CcidDevice reader;
    private UsbManager mUsbManager;
    private Context context;
    private static HashMap<Integer, String> providerServicesList;
    private static int nbProviderServices;
    private static int currentServiceId = -1;
    private String serviceName;
    private boolean selectService = false;
    private boolean checkDone = false;
    private boolean mpm_installed = false;
    private boolean pa_installed = false;

    private Menu toolbarMenu;

    /* ----------------------------------- Fragment callback functions ---------------------------*/
    /*
     * callback management functions to return from sub activities
     */
    public void onReturn(int action) {
        switch (currentActivity) {
            case cAdminActivity:
                checkApplication();
                break;
            case cScanActivity:
                goScanManually();
                break;
            default:
                if (action == Constants.ACTION_SAV) {
                    goSAV();
                } else {
                    goHome();
                }
                break;
        }

    }

    /*
     * callback from UserFragment
     */
    public void onUserIdentificationReturn(int id) {
        idUser = id;
        tools.setUserLogged(this, id);
        checkApplication();
    }

    /*
     * callback from InputFragment
     */
    public void onInput(String serial) {
        if (Constants.SHOW_OTIPASS_NUM) {
            int numotipass = Integer.valueOf(serial);
            card = new OtipassCard(numotipass);
        } else {
            card = new OtipassCard(serial);
        }
        goShowCard();
    }

    /*
     * callback from HomeFragment, clic on Pass image
    */
    public void onScanRequest() {
        goScan();
    }

    /*
     * callback from HomeFragment, clic on Pass image
    */
    public void onCardEvent(String serial) {
        card = new OtipassCard(serial);
        goShowCard();
    }

    private void detectOtipassPackages() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.otipass.passmuseum", PackageManager.GET_ACTIVITIES);
            mpm_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
        }
        try {
            pm.getPackageInfo("com.otipass.adt67", PackageManager.GET_ACTIVITIES);
            pa_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
        }

    }

    /* -------------------------------- View creation and UI setup functions ---------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handle uncaught exception : there will be redirected to BugMsgActivity
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        context = getApplicationContext();
        dbAdapter = new DbAdapter(this);
        dbAdapter.open();

        Intent intent = getIntent();
        if (intent != null ) {
            boolean calledFromExternPackage = intent.getBooleanExtra(Constants.EXTERN_CALL_KEY, false);
            if (calledFromExternPackage) {
                idUser = tools.getUserLogged(this);
            } else {
                // user should not be connected when launching from scratch
                idUser = 0;
                tools.setUserLogged(this, idUser);
            }
        }
        nfcIntentCalled = false;
        setContentView(R.layout.activity_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toolbar Back button pressed
                if ((currentActivity == cStatsActivity) ||
                    (currentActivity == cSAVActivity) ||
                    (currentActivity == cCancelActivity) ||
                    (currentActivity == cScanActivity)) {
                    goHome();
                } else if ((currentActivity == cShowCardActivity) || (currentActivity == cScanManuallyActivity)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(drawer.getWindowToken(), 0);
                    goScan();
                }
            }
        });
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (tools.getNFCManagement() == tools.cNativeNFC) {
            activateNFC();
        }
        nbProviderServices = 0;
        currentServiceId = -1;
        providerServicesList = null;
        detectOtipassPackages();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (((currentActivity == cScanActivity) || (currentActivity == cShowCardActivity)) && nfcIntentCalled) {
            // this is a return from scan or showcard fragment, due to the activity intent management - jump to showcard fragment
            detectMultipleServices();
            checkDone = false;
            goShowCard();
        } else {
            setBackButton(false);
            initFooter();
            checkApplication();
        }
        if (tools.getNFCManagement() == tools.cNativeNFC) {
            try {
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                IntentFilter[] intentFiltersArray = new IntentFilter[] {ndef};

                String[][] techListsArray = new String[][] {new String[] {Ndef.class.getName()}};
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
            } catch (Exception ex) {
                Log.e(Constants.TAG, "DrawerActivity onResume" + ex.getMessage());
            }

        }

    }

     /*
     * Enable/disable the drawer menu
     */
    public void setDrawerState(boolean isEnabled) {
        if ( isEnabled ) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            drawerToggle.onDrawerStateChanged(DrawerLayout.STATE_DRAGGING);
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.syncState();
        }
        else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            drawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE);
            drawerToggle.setDrawerIndicatorEnabled(false);
            drawerToggle.syncState();
        }
    }

    /*
    * Enable/disable the Toolbar back button (it takes place of the drawer menu button)
    */
    private void setBackButton(boolean isEnabled) {
        if (isEnabled) {
            setDrawerState(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            switch (currentActivity) {
                case cScanManuallyActivity:
                case cShowCardActivity:
                    getSupportActionBar().setTitle(R.string.scan);
                    break;

                default:
                    getSupportActionBar().setTitle(R.string.home);
                    break;
            }
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            switch (currentActivity) {
                case cAdminActivity:
                case cIdentificationActivity:
                    setDrawerState(false);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    break;
                default:
                    setDrawerState(true);
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    getSupportActionBar().setTitle(R.string.home);
                    break;
            }
        }
    }

    /*
    * Drawer main menu
    */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        nfcState = cNFCidle;
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Home and message view
            goHome();
        } else if (id == R.id.nav_entry) {
            // scan the card
            goScan();
        } else if (id == R.id.nav_stats) {
            // entry stats
            goStats();
        } else if (id == R.id.nav_tickets) {
            // entry stats
            goSAV();
        } else if (id == R.id.nav_synchro) {
            // simple synchronization
            goSynchro();
        } else if (id == R.id.nav_init) {
            // data initialization
            goInitData();
        } else if (id == R.id.nav_cancel) {
            // data initialization
            goCancel();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

     /* -------------------------------- main functions entry points -----------------------------*/
     /*
    * Home activity, displays messages
    */
     private void goHome() {
        currentActivity = cHomeActivity;
        onPrepareOptionsMenu(toolbarMenu);
        navigationView.getMenu().getItem(0).setChecked(true);
        setBackButton(false);
        nfcState = cNFCidle;
        Bundle args = new Bundle();
        args.putInt("user", tools.getUserLogged(this));
        FragmentManager fragmentManager = getFragmentManager();
        HomeFragment homeActivity = new HomeFragment();
        homeActivity.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, homeActivity).commit();
    }

    /*
   * Initialisation activity, when DataBase is not initialized
   */
    private void goAdmin() {
        nfcState = cNFCidle;
        currentActivity = cAdminActivity;
        setBackButton(false);
        onPrepareOptionsMenu(toolbarMenu);
        AdminFragment adminActivity = new AdminFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, adminActivity).commit();
    }

    /*
   * Identification activity, when no user connected
   */
    private void goIdentification() {
        nfcState = cNFCidle;
        currentActivity = cIdentificationActivity;
        setBackButton(false);
        onPrepareOptionsMenu(toolbarMenu);
        UserFragment userActivity = new UserFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, userActivity).commitAllowingStateLoss();
    }


    /*
   * Scan activity
   */
    private void launchScanActivity() {
        setBackButton(true);
        if (tools.getNFCManagement() == tools.cExternalReaderNFC) {
            reader = checkReader();
            if (reader != null) {
                reader.registerCardInserted(hReader);
            }

        }
        FragmentManager fragmentManager = getFragmentManager();
        Bundle args = new Bundle();
        serviceName = "";
        if ((currentServiceId > -1) && (nbProviderServices == 1)){
            serviceName = providerServicesList.get(currentServiceId);
        }
        args.putString("name", serviceName);
        ScanFragment scanActivity = new ScanFragment();
        scanActivity.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, scanActivity).commit();
        nfcState = cNFCactive;
    }

    private void goScan() {
        checkDone = false;
        tools.setRFIDLock(this, false);
        currentActivity = cScanActivity;
        onPrepareOptionsMenu(toolbarMenu);
        // do a synchro to get updates
        footer.silentSynchronize();
        nfcIntentCalled = false;
        // check number of services
        if ((providerServicesList != null) && nbProviderServices > 0) {
            // select 1st service
            Map.Entry elt = providerServicesList.entrySet().iterator().next();
            currentServiceId = (int) elt.getKey();
            detectMultipleServices();
            launchScanActivity();
        } else {
            currentServiceId = -1;
            tools.showErrDialog(this, getString(R.string.provider_has_no_service));
        }
    }

    /*
   * Scan manually activity
   */
    private void goScanManually() {
        checkDone = false;
        currentActivity = cScanManuallyActivity;
        onPrepareOptionsMenu(toolbarMenu);
        setBackButton(true);
        nfcIntentCalled = false;
        if ((providerServicesList != null) && nbProviderServices > 0) {
            // select 1st service
            Map.Entry elt = providerServicesList.entrySet().iterator().next();
            currentServiceId = (int) elt.getKey();
            detectMultipleServices();
            FragmentManager fragmentManager = getFragmentManager();
            InputFragment inputActivity = new InputFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_frame, inputActivity).commit();
        } else {
            currentServiceId = -1;
            tools.showErrDialog(this, getString(R.string.provider_has_no_service));
        }
    }
    /*
   * Entry stats activity
   */
    private void goStats() {
        onPrepareOptionsMenu(toolbarMenu);
        currentActivity = cStatsActivity;
        setBackButton(true);
        Bundle args = new Bundle();
        args.putInt("user", tools.getUserLogged(this));
        args.putString("action", Constants.STATS_ENTRY_WV);
        FragmentManager fragmentManager = getFragmentManager();
        WebviewFragment webActivity = new WebviewFragment();
        webActivity.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, webActivity).commit();
    }

    /*
    * Callback from service selection dialog
    */
    public void onDialogPositiveClick(int idservice) {
        currentServiceId = idservice;
        selectService = false;
        checkDone = true;
        // RFID is already locked
        tools.setRFIDLock(this, false);
        goShowCard();
    }

    /*
   * Show card activity, when a card is scanned
   */
    private void goShowCard() {
        if (!tools.getRFIDLock(this)) {
            tools.setRFIDLock(this, true);

            currentActivity = cShowCardActivity;
            onPrepareOptionsMenu(toolbarMenu);
            setBackButton(true);
            nfcIntentCalled = false;

            Bundle args = new Bundle();
            if (card != null) {
                args.putString("serial", card.getSerial());
                args.putInt("numotipass", card.getNumotipass());
                args.putInt("idservice", currentServiceId);
                args.putBoolean("selectService", selectService);
                args.putBoolean("checkDone", checkDone);
                args.putSerializable("servicesList", providerServicesList);
            }
            FragmentManager fragmentManager = getFragmentManager();
            ShowcardFragment showcardActivity = new ShowcardFragment();
            showcardActivity.setArguments(args);
            fragmentManager.beginTransaction().replace(R.id.fragment_frame, showcardActivity).commit();
        }
    }

    /*
   * Server Synchronization activity
   * This activity does not launch a fragment, it is processed by the Footer fragment
   */
    private void goSynchro() {
        // goSynchro does not change the fragment activity
        if (footer.isOnline()) {
            footer.synchronise(SynchronizationService.cGetPartialWL);
            WaitSynchro synchro = new WaitSynchro();
            synchro.execute();
        } else {
            tools.showAlert(this, getString(R.string.aucune_connexion_disponible), tools.cWarning);
        }
    }

    /*
   * Cancel last entry
   */
    private void goCancel() {
        currentActivity = cCancelActivity;
        onPrepareOptionsMenu(toolbarMenu);
        setBackButton(true);
        FragmentManager fragmentManager = getFragmentManager();
        CancelFragment cancelActivity = new CancelFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, cancelActivity).commit();
    }

    /*
   * Server Full Synchronization activity (database initialisation)
   * This activity does not launch a fragment, it is processed by the Footer fragment
   */
    private void goInitData() {
        // goInitData does not change the fragment activity
        if (footer.isOnline()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.confirmation_action));

            alertDialog.setIcon(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_question, null));
            alertDialog.setMessage(getString(R.string.sequence_initialisation_longue) + '\n' + getString(R.string.voulez_vous_continuer));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Global_oui), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    footer.synchronise(SynchronizationService.cGetTotalWL);
                    WaitSynchro synchro = new WaitSynchro();
                    synchro.execute();
                } });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.Global_non), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();


        } else {
            tools.showAlert(this, getString(R.string.aucune_connexion_disponible), tools.cWarning);
        }
    }
    /*
   * SAV Tickets
   */
    private void goSAV() {
        onPrepareOptionsMenu(toolbarMenu);
        currentActivity = cSAVActivity;
        setBackButton(true);
        Bundle args = new Bundle();
        args.putInt("user", tools.getUserLogged(this));
        args.putString("action", Constants.REQUEST_WV);
        FragmentManager fragmentManager = getFragmentManager();
        WebviewFragment webActivity = new WebviewFragment();
        webActivity.setArguments(args);
        fragmentManager.beginTransaction().replace(R.id.fragment_frame, webActivity).commit();
    }

    /* -------------------------------- toolbar menu ---------------------------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                showAbout(state, tools.getUserLogged(this));
                return true;
            case R.id.cancel:
                goCancel();
                return true;
            case R.id.keyboard:
                goScanManually();
                return true;
            case R.id.message:
                if (footer.isOnline()) {
                    footer.getMessages();
                    WaitSynchro synchro = new WaitSynchro();
                    synchro.execute();
                } else {
                    tools.showAlert(this, getString(R.string.aucune_connexion_disponible), tools.cWarning);
                }
                return true;
            case R.id.mpm:
                if (mpm_installed) {
                    Intent intent1 = getPackageManager().getLaunchIntentForPackage("com.otipass.passmuseum");
                    intent1.putExtra(Constants.EXTERN_CALL_KEY, true);
                    startActivity(intent1);
                }
                return true;
            case R.id.alsace:
                if (pa_installed) {
                    Intent intent2 = getPackageManager().getLaunchIntentForPackage("com.otipass.adt67");
                    intent2.putExtra(Constants.EXTERN_CALL_KEY, true);
                    startActivity(intent2);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            MenuItem keyboardItem = menu.findItem(R.id.keyboard);
            MenuItem cancelItem = menu.findItem(R.id.cancel);
            MenuItem messageItem = menu.findItem(R.id.message);
            if (!mpm_installed) {
                menu.findItem(R.id.mpm).setVisible(false);
            }
            if (!pa_installed) {
                menu.findItem(R.id.alsace).setVisible(false);
            }

            switch (currentActivity) {
                case cScanActivity:
                    keyboardItem.setVisible(true);
                    cancelItem.setVisible(false);
                    messageItem.setVisible(false);
                    break;
                case cShowCardActivity:
                    keyboardItem.setVisible(true);
                    cancelItem.setVisible(true);
                    messageItem.setVisible(false);
                    break;
                case cHomeActivity:
                    cancelItem.setVisible(false);
                    keyboardItem.setVisible(false);
                    messageItem.setVisible(true);
                    break;
                default:
                    cancelItem.setVisible(false);
                    keyboardItem.setVisible(false);
                    messageItem.setVisible(false);
                    break;
            }
        }catch (Exception ex) {
            Log.e(Constants.DRAWER_TAG, "DrawerActivity onPrepareOptionsMenu" + ex.getMessage());
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        toolbarMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*
   * Help function accessed from toolbar
   */
    private void showAbout(int state, int idUser) {
        String deviceName = "";
        String message = "", profile="", category="";
        try {
            message = getString(R.string.app_name) +  " v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + " - " + Constants.plateform +  "\n\n";
            Param param = dbAdapter.getParam(1L);
            User user = dbAdapter.getUser(idUser);
            if (param != null) {
                deviceName = param.getName();
                message += getString(R.string.appareil) + " : " + deviceName +  "\n\n";
                if ((param.getCategory() - Constants.SITE_PROVIDER) >= 0 ) {
                    category = getResources().getStringArray(R.array.categories)[param.getCategory() - Constants.SITE_PROVIDER];
                } else {
                    category = getString(R.string.unknown);
                }
            }
            if (user != null) {
                getResources().getStringArray(R.array.user_profiles);
                if ((user.getProfile() - Constants.USR_CONTROLLER) >= 0 ) {
                    profile = getResources().getStringArray(R.array.user_profiles)[user.getProfile() - Constants.USR_CONTROLLER];
                } else {
                    profile = getString(R.string.unknown);
                }
                message += getString(R.string.connected_as) + " " + category + " - " + profile;
            }

            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getString(R.string.about));
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.setMessage(message);
            alertDialog.setButton(getString(R.string.Global_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                } });
            alertDialog.show();
        } catch (Exception ex) {
            Log.e(Constants.DRAWER_TAG, "DrawerActivity showAbout" + ex.getMessage());
        }
    }

    /* -------------------------------- Local functions ------------------------------------------*/
    private void detectMultipleServices() {
        selectService =  (providerServicesList != null) && (nbProviderServices > 1);
    }

    /*
   * Get all services of the provider
   * returns hashmap <idservice, service name>
   */
    private HashMap<Integer, String> getProviderServices() {
        Services srvs = new Services();
        List<Integer> idList;
        nbProviderServices = 0;
        try {
            ArrayList<ProviderService> psList = dbAdapter.getAllProviderService();
            if (psList != null) {
                for (ProviderService ps : psList) {
                    srvs = srvs.concatService(ps.getService());
                }
                idList = srvs.getIds();
                providerServicesList = new HashMap<>();
                for (int id : idList) {
                    providerServicesList.put(id, dbAdapter.getServiceNameById(id));
                    nbProviderServices++;
                }
            }
        } catch(Exception ex) {
            Log.e(Constants.TAG, "DrawerActivity getProviderServices()" + ex.getMessage());
        }
        return providerServicesList;
    }


    /*
   * Check the application state
   * cIdle: database not initialized
   * cInitialized : database initialized, no user connected
   * cConnected: database initialized and user connected
   */
    private void checkApplication() {
        // check DB
        Param param = dbAdapter.getParam(1L);
        if (param != null) {
            if (param.getSoftwareVersion() != "") {
                state = cInitialized;
            }
        }
        if (state == cInitialized) {
            // check user
            if (tools.getUserLogged(this) > 0) {
                state = cConnected;
            }
        }
        switch (state) {
            case cIdle:
                setDrawerState(false);
                idUser = 0;
                tools.setUserLogged(this, idUser);
                goAdmin();
                break;
            case cInitialized:
                setDrawerState(false);
                nbProviderServices = 0;
                currentServiceId = -1;
                providerServicesList = getProviderServices();
                goIdentification();
                break;
            case cConnected:
                setDrawerState(true);
                goHome();
                break;
        }
    }

    /*
   * Footer is a fragment that resides at the screen bottom
   * It takes care of communication process and displays messages in the footer bar
   */
    private void initFooter() {
        footer = Footer.getInstance();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.footer, footer).commit();

    }


    /*
   * Phone Back button management
   * It is not used by the navigation
   * It may disconnect user if asked
   */
    private void  exitApplication() {
        new AlertDialog.Builder(this)
                .setMessage(
                        getString(
                                R.string.Confirmation_quitter)
                                .toString())
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(
                        getString(R.string.Global_information)
                                .toString())
                .setPositiveButton(getString(R.string.Global_oui),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                //dbAdapter.close();
                                idUser = 0;
                                tools.setUserLogged(getApplicationContext(), idUser);
                                finish();
                            }
                        })
                .setNegativeButton(getString(R.string.Global_non),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }
                        }).show();
    }

    @Override
    public void onBackPressed() {
        exitApplication();
    }

    // this class is intended to wait for the end of the synchronization
    private class WaitSynchro extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values){
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            boolean end = false;
            do {
                end = (tools.getServiceState(context) != tools.cCommmunicationPending);
            } while (!end);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Param param = dbAdapter.getParam(1L);
            if (param != null) {
                providerServicesList = getProviderServices();
            }
            goHome();
        }
    }


    /* -------------------------------- NFC functions --------------------------------------------*/
    /*
   * NFC management
   * Detects if NFC is active
   */
    private void activateNFC() {
        boolean nfcEnabled = false;
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC) ) {
            nfcEnabled = nfcAdapter.isEnabled();
        }
        if (!nfcEnabled) {
            new AlertDialog.Builder(this).setIcon(R.drawable.ic_attention)
                    .setTitle(getString(R.string.Global_information))
                    .setMessage(getString(R.string.NFC_not_active))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.Global_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }
    /*
   * NFC intent management
   * Intent management is not declared in the application manifest
   * It is declared when calling nfcAdapter.enableForegroundDispatch() in the onResume() task
   * Only this application catches NFC intents
   */
    @Override
    public void onNewIntent(Intent intent)
    {
        if (intent.hasExtra(Constants.EXTERN_CALL_KEY)) {
            checkApplication();
        } else {
            setIntent(intent);
            if (nfcState != cNFCidle) {
                String action = intent.getAction();
                if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
                    nfcIntentCalled = true;
                    Bundle extras = intent.getExtras();
                    card = Nfc.readCard(intent, extras);
                }
            }
        }
    }

    /* -------------------------------- External card reader--------------------------------------*/
    public CcidDevice getReader() {
        return reader;
    }

    private CcidDevice checkReader() {
        CcidDevice reader = null;
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        CcidController cc = CcidController.getInstance();
        List<CcidDevice> devices = cc.getDevicesList(mUsbManager);
        if (devices.size() > 0) {
            reader = devices.get(0);
        } else {
            tools.showAlert(this, getString(R.string.reader_ko), tools.cError);
        }
        return reader;
    }

    public void closeReader() {
        if (reader != null) {
            reader.unregisterCardInserted(hReader);
        }
    }

    private Handler hReader = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int type;
            String serial = "";

            try {
                if (msg.what == CcidConstants.HANDLER_WHAT_CARD) {
                    String readerATR;
                    if (msg.obj instanceof Card) {
                        Card c = (Card) msg.obj;
                        String atr = tools.byteArrayToString(c.getATR());
                        Log.i(Constants.DRAWER_TAG, "ATR: " + atr);
                        if (c.getUid() == null) {
                            Log.i(Constants.DRAWER_TAG, "HANDLER c.getUid() = null - ATR=" + atr);
                            serial = Constants.UNKNOWN_PASS;
                        } else {
                            Log.i(Constants.DRAWER_TAG, "HANDLER: UID=" + tools.byteArrayToString(c.getUid()));
                            serial = tools.byteArrayToString(c.getUid());
                            if (!Constants.PUPI_FORMAT.isEmpty()) {
                                serial = String.format(Constants.PUPI_FORMAT, serial).replace(' ', '0');
                            }
                        }
                        if (nfcState != cNFCidle) {
                            card = new OtipassCard(serial);
                            detectMultipleServices();
                            goShowCard();
                        }
                    } else {

                    }
                } else if (msg.what == CcidConstants.HANDLER_WHAT_READER_DISCONNECTED) {
                    Log.i(Constants.DRAWER_TAG, "Reader disconnected!!");
                } else {
                    Log.i(Constants.DRAWER_TAG, "Reader error!!");
                }
            } catch (Exception e) {
                Log.e(Constants.DRAWER_TAG, "Unexpected exception in handler",e);
            }
        };
    };


}

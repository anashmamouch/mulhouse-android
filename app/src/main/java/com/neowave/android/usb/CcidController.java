/**
 ================================================================================

 OTIPASS
 neowave package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: CcidController.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.neowave.android.usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is the controller class to use to access CCID USB devices.
 * 
 * @author Neowave - WENEO TEAM
 */
public class CcidController {

	/**
	 * Instance of the class.
	 */
	protected static CcidController instance = null;
	
	/**
	 * Map of devices
	 */
	protected Map<UsbDevice, CcidDevice> devices = new HashMap<UsbDevice, CcidDevice>();
	
	/**
	 * Get the instance of this controller.
	 * 
	 * @return The instance
	 */
	public static CcidController getInstance() {
		if (instance == null) {
			instance = new CcidController();
		}
		return instance;
	}
	
	protected CcidController() {		
	}
	
	
	/**
	 * This method finds the USB devices available that are CCID compliant.
	 * You must have the permission to access the USbDevice (UsbManager.hasPermission(...))
	 * 
	 * @param mUsbManager The Android USB Manager
	 * @return A list of CcidDevice objects. If no device, the list returned is empty.
	 */
	public final List<CcidDevice> getDevicesList(UsbManager mUsbManager) {
		List<CcidDevice> ccidDevices = new ArrayList<CcidDevice>();
		
		Collection<UsbDevice> devices = mUsbManager.getDeviceList().values();
		String ccidNames = "";
		String otherNames = "";
		
		for (UsbDevice u : devices)  {
			CcidDevice d = null;
			try {
				Log.d(CcidConstants.LOG_TAG, "Permission for device "+u.getDeviceName()+ ": "+mUsbManager.hasPermission(u));
				d = getCcidDevice(mUsbManager, u);
			} catch (SecurityException e) {
				Log.i(CcidConstants.LOG_TAG, "No permission to access the USB device "+u.getDeviceName());
			}
			if (d!=null) {
				ccidDevices.add(d);
				ccidNames += u.getDeviceName() + " ";
			} else {
				otherNames += u.getDeviceName() + " ";
			}
		}
		Log.d(CcidConstants.LOG_TAG, "Ccid devices:"+ccidNames+"\nOther devices:"+otherNames);
		
		return ccidDevices;
	}
	
	/**
	 * Get the CcidDevice object from the USB device.
	 * If the device is not CCID compliant, null will be returned.
	 * 
	 * @param mUsbManager The Android USB Manager
	 * @param usb The Android USD device object
	 * @return The CcidDevice object, or null if the device is not CCID compliant.
	 */
	public final CcidDevice getCcidDevice(UsbManager mUsbManager, UsbDevice usb) {
		CcidDevice result = devices.get(usb);
		if (result == null)  {
			result = createDevice(mUsbManager, usb);
			devices.put(usb, result);
		}
		
		return result;
	}
	
	/**
	 * Create the CcidDevice object in case of new usb device
	 * 
	 * @param mUsbManager The Android USB Manager
	 * @param usb The Android USD device object
	 * @return The CcidDevice object, or null if the device is not CCID compliant.
	 */
	protected CcidDevice createDevice(UsbManager mUsbManager, UsbDevice usb) {
		try {
			return new CcidDevice(mUsbManager, usb);
		} catch (CcidException e) {
			Log.w(CcidConstants.LOG_TAG, "Cannot use the USB device for CCID",e);
			return null;
		}
	}
}

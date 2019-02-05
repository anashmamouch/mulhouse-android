/**
 ================================================================================

 OTIPASS
 neowave package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: CcidException.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.neowave.android.usb;

/**
 * Exception class for the CCID devices
 * 
 * @author Neowave - WENEO TEAM
 */
public class CcidException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -330918879701556155L;

	public CcidException(String message) {
		super(message);
	}

	public CcidException(String message, Exception parentException) {
		super(message, parentException);
	}
}

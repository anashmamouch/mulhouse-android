/**
 ================================================================================

 OTIPASS
 neowave package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Card.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.neowave.android.usb;

/**
 * This class represents a card detected by a CCID reader 
 * 
 * @author Neowave - WENEO TEAM
 */
public class Card {

	// Attributes
	private CcidDevice device;
	private byte[] atr;
	private byte[] uid;
	
	/**
	 * Constructor
	 * 
	 * @param atr The atr of the card
	 * @param uid The UID of the card
	 * @param device The CcidDevice object that detected the card
	 */
	public Card(byte[] atr, byte[] uid, CcidDevice device) {
		// Null test
		if (atr == null) throw new NullPointerException("atr cannot be null");
		// if (uid == null) throw new NullPointerException("uid cannot be null"); // SIM card for instance
		if (device == null) throw new NullPointerException("device cannot be null");
		
		this.atr = atr;
		this.uid = uid;
		this.device = device;
	}

	/**
	 * 
	 * @return The ATR
	 */
	public byte[] getATR() {
		return atr;
	}
	
	/**
	 * 
	 * @return The UID
	 */
	public byte[] getUid() {
		return uid;
	}
	
	/**
	 * Send an APDU command to the card.
	 * 
	 * @param apdu The APDU command written as a byte array.
	 * @return The APDU response.
	 */
	public byte[] sendAPDU(byte[] apdu) {
		return device.sendApdu(apdu);
	}
}

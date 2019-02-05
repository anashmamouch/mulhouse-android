/**
 ================================================================================

 OTIPASS
 nfc package

 @author ED ($Author: ede $)

 @version $Rev: 6455 $
 $Id: Nfc.java 6455 2016-07-08 11:18:45Z ede $

 ================================================================================
 */
package com.otipass.nfc;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.otipass.tools.OtipassCard;

import models.Constants;

public class Nfc {


	/*
   * Read Pseudo Unique PICC Identifier ( = serial number)
   *  Bundle extras: tag information read by NFC controller
   *  returns PUPI as hexadecimal string without space
   */
	static public String readPupi(Bundle extras) {
		String pupi = "";
		try {
			final Tag tag = (Tag) extras.getParcelable("android.nfc.extra.TAG");
			byte[] data = tag.getId();
			String c;
			for (int i = 0; i < data.length; i++) {
				c = Integer.toHexString(data[i] & 0xff);
				c = String.format("%2s", c).replace(' ', '0'); 
				pupi += c;
			}
		} catch (Exception e) {

		}
		return pupi.toUpperCase();
	}

	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}


    /*
   * Read NDEF message stored in PICC and decrypt it
   *  The NDEF message is DES ciphered
   *  Intent intent: the intent sent by the android NFC layer
   *  returns NDEF message as a string
   */
	public static String readNDEFMessage(Intent intent) {
		String s = "";
		try {
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			NdefMessage msg = (NdefMessage) rawMsgs[0];
			byte[] content = null;
			content = msg.getRecords()[0].getPayload();
			s = new String(content);
			s = s.substring(3);
			byte[] myKey = new byte[] {-82, 34, 66, 48, -89, -1, 49, -29};
			byte[] encrypted = new byte[8];
			encrypted = hexStringToByteArray(s); 
			Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
			SecretKeySpec key = new SecretKeySpec(myKey, "DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] decrypted = cipher.doFinal(encrypted);
			s = new String(decrypted, "UTF8");
			s = s.trim();
		}
		catch (Exception e){ 
			Log.e(Constants.TAG, Nfc.class.getName() + " - readNDEFMessage -" + e.getMessage());
		}

		return s;
	}

    /*
   * Read NFC tag
   * If Constants.NDEF_ENABLED = true, reads PUPI + NDEF message
   * else read only PUPI
   *  Bundle extras: tag information read by NFC controller
   *  Intent intent: the intent sent by the android NFC layer
   *  returns OtipassCard
   */
	public static OtipassCard readCard(Intent intent, Bundle extras) {
		String tags[], client;
		int status = OtipassCard.cNotOtipassCard, numOtipass;
		OtipassCard card = null;
		try {
			String pupi = readPupi(extras);
			if (!pupi.isEmpty()) {
                if (Constants.NDEF_ENABLED) {
                    String message = readNDEFMessage(intent);
                    if ((message != null) && !message.isEmpty()) {
                        tags = message.split("-");
                        if (tags.length == 2) {
                            client = tags[0];
                            numOtipass = Integer.valueOf(tags[1]);
                            if (OtipassCard.checkLuhnOK(numOtipass)) {
                                status = OtipassCard.cOtipassCard;
                            }
                            card = new OtipassCard(numOtipass, pupi, client, status);
                        }
                    }
                } else {
                    card = new OtipassCard(pupi);
                }
			}
		} catch (Exception e) {

		}
		return card;
	}


}

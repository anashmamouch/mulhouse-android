/**
 ================================================================================

 OTIPASS
 neowave package

 @author ED ($Author: ede $)

 @version $Rev: 6363 $
 $Id: CcidDevice.java 6363 2016-06-13 16:39:57Z ede $

 ================================================================================
 */
package com.neowave.android.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/**
 * This class represents a CCID USB device.
 * 
 * @author Neowave - WENEO TEAM
 */
public class CcidDevice implements Runnable {
	// Constants
	private final static String LOG_TAG = CcidConstants.LOG_TAG;	
    private final static byte SLOT_NUMBER = 0x00;
    
    // APDU commands
    protected final static byte[] APDU_GET_UID = new byte[]{(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};

   
    // Variables
	private List<Handler> handlers = new ArrayList<Handler>();
	private Thread interruptThread = null;
	
	// Usb variables
	private UsbDevice usbDevice;
	private UsbDeviceConnection usbConnection;
	private UsbInterface usbInterface;
	private UsbEndpoint endpointIntr;
	private UsbEndpoint endpointBulkOut;
	private UsbEndpoint endpointBulkIn;
	
	private byte sequenceNumber = 0x00; // The sequence number is increased for each command sent
		
	/**
	 * Create a CCIDReader from a USB device
	 * 
	 * @param usbManager The Android USB Manager
	 * @param device A USB device
	 * @throws CcidReaderException Thrown if the reader is not a correct CCID reader
	 */
	protected CcidDevice(UsbManager usbManager, UsbDevice device) throws CcidException {
		// Debug infos
		//Log.d(LOG_TAG, "Create CCID device");
		//Log.v(LOG_TAG, "descriptor: " + Integer.toHexString(device.describeContents()));
		//Log.v(LOG_TAG, "device name: " + device.getDeviceName());
		//Log.v(LOG_TAG, "interface number: " + device.getInterfaceCount());

		this.usbDevice = device;
		
		// Get the first interface with at least 3 endpoints
		int interfaceCount = device.getInterfaceCount();
		if (interfaceCount < 1) {
			Log.e(LOG_TAG, "could not find an USB interface");
			throw new CcidException("No USB interface available.");
		}
		int j=0;
		do {
			usbInterface = device.getInterface(j++);
		} while (usbInterface.getEndpointCount() < 3 && j < interfaceCount);
		
		if (usbInterface.getEndpointCount() < 3) {
			throw new CcidException("No USB interface has enought endpoints.");
		}

		// Find the endpoints needed
		try {
			int nEndpoints = usbInterface.getEndpointCount();
			for (int i = 0; i < nEndpoints; i++) {
				UsbEndpoint ep = usbInterface.getEndpoint(i);

				// Test for interrupt endpoint
				if (endpointIntr == null
						&& ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {
					endpointIntr = ep;
				}
				// Test for bulk-out endpoint
				else if (endpointBulkOut == null
						&& (ep.getType() & UsbConstants.USB_ENDPOINT_XFERTYPE_MASK) == UsbConstants.USB_ENDPOINT_XFER_BULK
						&& (ep.getDirection() & UsbConstants.USB_ENDPOINT_DIR_MASK) == UsbConstants.USB_DIR_OUT) {
					endpointBulkOut = ep;
				}
				// Test for bulk-in endpoint
				else if (endpointBulkIn == null
						&& (ep.getType() & UsbConstants.USB_ENDPOINT_XFERTYPE_MASK) == UsbConstants.USB_ENDPOINT_XFER_BULK
						&& (ep.getDirection() & UsbConstants.USB_ENDPOINT_DIR_MASK) == UsbConstants.USB_DIR_IN) {
					endpointBulkIn = ep;
				}
			}

		} catch (Exception e) {
			Log.w(LOG_TAG, e.getMessage(), e);
		}

		// Test that we have the endpoints needed
		if (endpointIntr==null) throw new CcidException("No Interrupt endpoint on this USB device.");
		else if (endpointBulkOut==null) throw new CcidException("No bulk-out endpoint on this USB device.");
		else if (endpointBulkIn==null) throw new CcidException("No bulk-in endpoint on this USB device.");
		else Log.d(LOG_TAG,"Endpoints created successfully.");
		
		// Open the connection
		usbConnection = usbManager.openDevice(device);		
        if (usbConnection != null) {
        	if (usbConnection.claimInterface(usbInterface, true)) {
                Log.i(LOG_TAG, "Connection to USB interface OPENED");
        	} else {
                Log.w(LOG_TAG, "Unable to claim access to USB device");
    			throw new CcidException("Unable to claim access to USB device.");
        	}
        } else {
            Log.i(LOG_TAG, "Connection to USB interface FAILED");
			throw new CcidException("Unable to open connection with the interface.");
        }
	}
	

    /**
     * Send a command on the bulk-out endpoint, and reads the response on the bulk-in endpoint
     * This is synchronous, the method returns only when the response is available.
     * 
     * @param command The full CCID command. Length must be at least 7 bytes.
     * @return The result obtained on the bulk-in endpoint
     * @throws CcidReaderException
     */
    protected final byte[] sendCommand(byte[] command) throws CcidException {
    	byte[] result = new byte[0];
        byte[] bufferDataIn = new byte[128];
        byte seqNumber;
    	int TIMEOUT = 0;
    	
    	try {    	
	    	//Log.v(LOG_TAG, "sendCommand");

	        seqNumber = command[6];
	    	
	    	 // To do in another thread ?
	    	int dataSent = usbConnection.bulkTransfer(endpointBulkOut, command, command.length, TIMEOUT);	    	
	        //Log.v(LOG_TAG, "  Data sent OUT ("+dataSent+") : "+Arrays.toString(command));
	        
	        int loopCount = 0;
	        int lengthDataReceived;
	        do {
		        lengthDataReceived = usbConnection.bulkTransfer(endpointBulkIn, bufferDataIn, bufferDataIn.length, TIMEOUT);	    	
		        //Log.v(LOG_TAG, "  Data received ("+lengthDataReceived+") : "+Arrays.toString(bufferDataIn));
	        	
	        	// The response must have the same sequence number than the request
	        	// A time extension can be sent in the response, in that case we need to try
	        	// to get the response again
	        } while (((bufferDataIn.length>6 && bufferDataIn[6] != seqNumber) // Same sequenceNumber
	        		|| (bufferDataIn[7] & (byte)0x80) != 0x00) // Check Time Extension error
	        		&& loopCount++ < 5 // Don't loop forever
	        		);
	        	
        	result = Arrays.copyOf(bufferDataIn, lengthDataReceived);
        	
        
    	} catch (Exception e) {
			throw new CcidException("  Send command failed.",e);
    	}
	        
    	return result;
    }
	
    /**
     * This method blocks until a card is available
     * 
     * @return The card detected, or null if an error happened
     */
    private Card waitForCard() {
    	Card result = null;
        //Log.v(LOG_TAG, "waitForCard()");
        
        ByteBuffer buffer = ByteBuffer.allocate(10);
        UsbRequest request = new UsbRequest();
        request.initialize(usbConnection, endpointIntr);
        boolean waitForCard = true;
        
        try {
			while (waitForCard) {        	
			    // queue a request on the interrupt endpoint
			    request.queue(buffer, 10);
			    // wait for status event
			    if (usbConnection.requestWait() == request) {
//			    	Log.d(LOG_TAG, "USB interrupt(" + buffer.capacity() +"): "+Arrays.toString(buffer.array()));
			        byte command = buffer.get(0);
			        
			        if (command == 0x50) {
			        	// Test if slot 0 has a card inserted
			        	if ((buffer.get(1)&0x03) == 0x03) {
			        		// There is a card, create the object
				        	result = createCard(getAtr(), getUid());
				        	waitForCard = false;
			        	}
			        }
			        
//			        // Tempo
//			        try {
//			            Thread.sleep(100);
//			        } catch (InterruptedException e) {
//			        }
			    } else {
			        Log.e(LOG_TAG, "requestWait failed, exiting");
			        waitForCard = false;
			        break;
			    }
			}
		} catch (Exception e) {
	        Log.e(LOG_TAG, "waitForCard() Exception", e);
		} finally {
			if (request != null) {
				request.close();
			}
		}
        
        return result;
    }
    
    /**
     * This method creates the object card.
     * This can be overwritten in order to customize the card object.
     * 
     * @param atr The ATR
     * @param uid The UID of the card
     * @return The Card object
     */
    protected Card createCard(byte[] atr, byte[] uid) {
    	return new Card(atr,uid,this);
    }
    
    /**
	 * Get the Answer To Reset of the card
	 * 
	 * @return The ATR
	 */
	protected byte[] getAtr() {
    	//Log.v(LOG_TAG, "getAtr()");
    	byte[] atr = null;
    	
    	// IccPowerOn command
    	byte[] getAtr = new byte[]{0x62,
        		0x00,0x00,0x00,0x00,
        		SLOT_NUMBER,sequenceNumber++,
        		0x02,0x00,0x00};
    	
    	try {
			atr = sendCommand(getAtr);
			atr = Arrays.copyOfRange(atr, 10, atr[1]+10);
		} catch (CcidException e ) {
            Log.e(LOG_TAG, "Unable to get ATR", e);
		} catch (Exception e) {
            Log.e(LOG_TAG, "Unexpected exception", e);
		}
    	
    	return atr;
    }
    
	/**
	 * Get the UID of the card.
	 * 
	 * @return The UID
	 */
	protected byte[] getUid() {
    	//Log.v(LOG_TAG, "getUID()");
    	byte[] uid = null;
    	
		byte[] result = sendApdu(APDU_GET_UID);
		//Log.v(LOG_TAG, "UID command result: "+Arrays.toString(result));
		
		if (result == null) {
			Log.v(LOG_TAG, "Get UID failed");
		} else if (result.length <= 2) {
			Log.v(LOG_TAG, "Unable to get UID. Response="+Arrays.toString(result));
		} else {
			uid = Arrays.copyOfRange(result, 0, result.length-2);
		}
    	
    	return uid;
    }
	
    /**
     * Send an APDU command to the reader.
     * 
     * @param apdu The APDU command
     * @return The APDU response. Null if an error occured.
     */
    protected byte[] sendApdu(byte[] apdu) {
    	byte[] adpuResult = null;
    	
    	byte apduLength = (byte) apdu.length;
    	
    	byte[] ccidHeader = new byte[]{0x6F,
    			apduLength,0x00,0x00,0x00,
        		SLOT_NUMBER,sequenceNumber++,
    			0x00,0x00,0x00};
    	
    	byte[] ccidCommand = new byte[ccidHeader.length + apdu.length];
    	
    	try {
    		System.arraycopy(ccidHeader, 0, ccidCommand, 0, ccidHeader.length);
    		System.arraycopy(apdu, 0, ccidCommand, ccidHeader.length, apdu.length);
    		
			byte[] result = sendCommand(ccidCommand);
			//Log.v(LOG_TAG, "Send APDU command result: "+Arrays.toString(result));
			
			if (result[0] == (byte)0x80) {
				int length = result[1];
				//Log.v(LOG_TAG, "Apdu response length="+length);
				if (length>0) {
					adpuResult = Arrays.copyOfRange(result, 10, 10+length);
				}
			}
		} catch (CcidException e) {
            Log.e(LOG_TAG, "Failed to send APDU command", e);
		} catch (Exception e) {
            Log.e(LOG_TAG, "Failed to send APDU command, unknown error.", e);
		}
    	
    	return adpuResult;
    }
	
    /**
     * Run the Thread for this reader. It listens to card insertion events.
     * CcidConstants defines the value for the field 'what' of handlers
     */
    public void run() {
    	boolean loop = true;
    	while (!handlers.isEmpty() && loop) {
    		Card c = waitForCard();

    		Message msg = Message.obtain();
    		
    		// Check if there was an error
    		if (c == null) {
    			loop = false;
    			//TODO find a better test
    			if (usbConnection == null || !usbConnection.claimInterface(usbInterface, false)) {
    				// Connection lost
            		msg.what = CcidConstants.HANDLER_WHAT_READER_DISCONNECTED;
            		msg.obj = this;
            		Log.d(LOG_TAG, "UsbDevice Connection lost");
    			} else {
            		msg.what = CcidConstants.HANDLER_WHAT_ERROR;
            		msg.obj = this;
            		Log.d(LOG_TAG, "UsbDevice Connection error");
    			}
    		} else {
        		msg.what = CcidConstants.HANDLER_WHAT_CARD;
        		msg.obj = c;
    		}
    		    		
    		// Notify handlers
    		// Iterate in reverse order from the last handler added
    		ListIterator<Handler> it = handlers.listIterator(handlers.size());
    		while (it.hasPrevious()) {
    			try {
    				Handler h = it.previous();
    				h.sendMessage(msg);
    				// Duplicate message for next handler
    				msg = Message.obtain(msg);
    			} catch (Exception e) {
    				it.remove();
    				Log.w(LOG_TAG, "Error on handler => handler removed. Cause: "+e.getClass().getName(),e);
    			}
    		}
    	}
    }    
	    
    
    
    //////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\
    //////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\
    //                PUBLIC METHODS                  \\
    //////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\
    //////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\\
    
    /**
     * Tell if a card is present in the reader
     * 
     * @return True if there is a card, false otherwise
     */
	public boolean isCardPresent() {
		boolean result = false;
		
		byte[] getSlotStatus = new byte[]{0x65,
        		0x00,0x00,0x00,0x00,
        		SLOT_NUMBER,sequenceNumber++,
        		0x00,0x00,0x00};
		
    	try {
			byte[] answer = sendCommand(getSlotStatus);
			if (answer[0] == (byte)0x81) {
				result = (answer[7] == 0x00);
			}
		} catch (CcidException e ) {
            Log.e(LOG_TAG, "Unable to send slot status command", e);
		}
		
		Log.i(LOG_TAG,"Card present: "+result);
		
		return result;
	}
	
	/**
	 * Register a handler that will be notified if a card is inserted.
	 * When a card is detected, the Message object sent to the handler is as follows:
	 * <br>- what = CcidConstants.HANDLER_WHAT_CARD
	 * <br>- obj = the card object
	 * <br>See CcidConstants for more values
	 * 
	 * @param h The handler to notify when a card is detected
	 */
	public void registerCardInserted(Handler h) {
		if (!handlers.contains(h)) {
			handlers.add(h);
			if (interruptThread == null || !interruptThread.isAlive()) {
				interruptThread = new Thread(this);
				interruptThread.start();
			}
		}
	}
	
	/**
	 * Close the connection to the device
	 */
	public void close() {
		try {
			if (usbConnection != null)  {
				usbConnection.releaseInterface(usbInterface);
				usbConnection.close();
				usbConnection = null;
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error in close()",e);
		}
	}
	
	/**
	 * Unregister an handler for the card detection.
	 * Has no effect if the handler was not registered.
	 * 
	 * @param h The Handler
	 */
	public void unregisterCardInserted(Handler h) {
		if (handlers.contains(h)) {
			handlers.remove(h);
		}
	}
	
	/**
	 * Get the USB device object
	 * @return The USB device object
	 */
	public UsbDevice getUsbDevice() {
		return usbDevice;
	}

	/**
	 * Two CcidDevice objects are equals if the USBDevice they represent are equals.
	 */
	public boolean equals(Object o) {
		if (o instanceof CcidDevice && usbDevice != null)  {
			CcidDevice device = (CcidDevice) o;
			return usbDevice.equals(device.usbDevice);
		} else {
			return false;
		}
	}
}

/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6363 $
 $Id: Callback.java 6363 2016-06-13 16:39:57Z ede $

 ================================================================================
 */
package com.otipass.tools;


public class Callback {
    // callback interface for RETURN button
    public interface OnReturnListener {
        public void onReturn(int action);
    }
    public interface OnScanRequestListener {
        public void onScanRequest();
    }
    public interface OnInputListener {
        public void onInput(String serial);
    }
    public interface OnUserIdentificationListener {
        public void onUserIdentificationReturn(int idUser);
    }
    // callback interface for card detection events
    public interface OnCardEventListener {
        public void onCardEvent(String serial);
    }    
	
}

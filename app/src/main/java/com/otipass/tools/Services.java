/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: Services.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */

package com.otipass.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Constants;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.otipass.sql.Coupon;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.Otipass;

public class Services {
	private String srvStr;
	private Map<Integer, Integer> list;
	public static final int cOK = 0;
	public static final int cError = 1;

    public Services() {
        srvStr = "";
        list = new HashMap<>();
    }

    /*
   * Transform a string of services like 1:1;2:1;3:2... into a hashmap list key::value
   *
   */
    public Services(String srvs) {
        srvStr = srvs;
        list = new HashMap<>();
        try {
            String[] t1 = srvs.split(";");
            for(String elt : t1){
                if (!elt.isEmpty()) {
                    String[] t2 = elt.split(":");
                    if (t2.length == 2) {
                        list.put(Integer.parseInt(t2[0]),Integer.parseInt(t2[1]));
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, tools.class.getName() + " - Services() - " + ex.getMessage());
        }
    }

    /*
   * Get the counter part of the hashmap element
   *  int key: the key to look for
   */
    public int getCounter(int key) {
        int nb = 0;
        if (list.containsKey(key)) {
            nb = list.get(key);
        }
        return nb;
    }

    public int getFirstServiceId() {
        int id = 0;
        try {
            Map.Entry elt = list.entrySet().iterator().next();
            id = (int)elt.getKey();
        }catch (Exception ex) {
            Log.e(Constants.TAG, tools.class.getName() + " - getFirstServiceId() - " + ex.getMessage());
        }
        return id;
    }

    /*
   * Get the number of key elements of the source list matching the pattern list
   *
   */
    public int hasServices(Services srcList) {
        int nb = 0;
        Iterator it = srcList.list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry)it.next();
            if (list.containsKey(elt.getKey())) {
                nb = list.get(elt.getKey());
                break;
            }
        }
        return nb;
    }

    /*
   * Get the number of key elements of the source list matching the pattern list
   *
   */
    public List<Integer> getActiveServices(Services srcList) {
        List<Integer> srvList =  new ArrayList<Integer>();
        int nb = 0;
        Iterator it = srcList.list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry)it.next();
            if (list.containsKey(elt.getKey()) && (list.get(elt.getKey()) > 0)) {
                srvList.add((int)elt.getKey());
            }
        }
        return srvList;
    }

    /*
   * Get services remaining for one otipass to display service selection
   * providerServices holds the services of the provider
   */
    public HashMap<Integer, String> getServices2Select(HashMap<Integer, String> providerServices) {
        HashMap<Integer, String> pList = (HashMap<Integer, String>) providerServices.clone();
        Iterator it = providerServices.entrySet().iterator();
        boolean keep;
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry) it.next();
            keep = list.containsKey(elt.getKey()) && (list.get(elt.getKey()) > 0);
            if (!keep) {
                pList.remove(elt.getKey());
            }
        }
        return pList;
    }


    /*
   * Decrease the counter part of the hashmap element matching key
   *    int key: the key to match
   */
    public String decService(int key) {
        String str = "";
        int nb, val=0;
        Iterator it = list.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry)it.next();
            if ((int)elt.getKey() == key) {
                val =  (int)elt.getValue() > 0 ? (int)elt.getValue() - 1 : 0;
                // accept negative values for created cards
                //val =  (int)elt.getValue() - 1;
            } else {
                val =  (int)elt.getValue();
            }
            str = str + String.valueOf(elt.getKey()) + ":" + String.valueOf(val) + ";";
            list.put((int)elt.getKey(), val);
        }
        return str;
    }

    /*
   * Update an Otipass  services string with server values
   *    Services updateList: the string of services coming from the server
   *    int idservice: the current service id being processed
   */
    public String updateService(Services updateList, int idservice) {
        String str = "";
        int valUpdate, key;
        Iterator it = list.entrySet().iterator();
        // iterate through Otipass service string in DB
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry)it.next();
            key = (int)elt.getKey(); // the otipass service
            valUpdate = (int)elt.getValue(); // the value in DB
            if (updateList.list.containsKey(key)) {
                // the update string has a value for this key
                valUpdate = updateList.list.get(key);
                if (key == idservice) {
                    // the service to take into account is otipassDB
                    valUpdate = (int)elt.getValue();
                }
            }
            str = str + String.valueOf(key) + ":" + String.valueOf(valUpdate) + ";";
            list.put(key, valUpdate);
        }
        return str;
    }

    /*
    * concat Services
    *    String srvs: the services String to add to list
    */
    public Services concatService(String srvs) {
        String str = "";
        int val, key;
        Services updateList = new Services(srvs);
        Iterator it = updateList.list.entrySet().iterator();
        // iterate through update list
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry)it.next();
            key = (int)elt.getKey();
            val = (int)elt.getValue();
            if (!list.containsKey(key)) {
                list.put(key, val);
                srvStr = srvStr + String.valueOf(key) + ":" + String.valueOf(val) + ";";
            }
        }
        return this;
    }

    /*
    * return the services id as a list
    *
    */
    public List<Integer> getIds() {
        List<Integer> idList = new ArrayList<Integer>();
        Iterator it = list.entrySet().iterator();
        int key;
        // iterate through list
        while (it.hasNext()) {
            Map.Entry elt = (Map.Entry)it.next();
            key = (int)elt.getKey();
            idList.add(key);
        }
        return idList;
    }

}

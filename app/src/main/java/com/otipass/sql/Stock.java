/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Stock.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;


public class Stock {
	private long id;
	private int nbCards;
	private int threshold;
	private int alert;
	private int provider_id;

	public Stock() {
		
	}
	
	public Stock(int provider_id, int nbCards, int threshold, int alert) {
		this.provider_id = provider_id;
		this.nbCards = nbCards;
		this.threshold = threshold;
		this.alert = alert;
	}
	
	public int getProviderId(){
		return provider_id;
	}
	
	public void setProviderId(int provider_id){
		this.provider_id = provider_id;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getNbCards() {
		return nbCards;
	}

	public void setNbCards(int nbCards) {
		this.nbCards = nbCards;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getAlert() {
		return alert;
	}

	public void setAlert(int alert) {
		this.alert = alert;
	}




}

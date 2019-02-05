/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Wl.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Wl {

	private long id;
	private String date;
	private int nbsteps;
	private int nbcards;
	private int numsequence;
	private int status;


	public Wl() {

	}
	
	public Wl(int id, String date, int nbsteps, int nbcards,  int numsequence, int status) {
		this.id = id;
		this.date = date;
		this.nbsteps = nbsteps;
		this.nbcards = nbcards;
		this.numsequence = numsequence;
		this.status = status;
	}

	public Wl(String date, int nbsteps, int nbcards,  int numsequence, int status) {
		this.date = date;
		this.nbsteps = nbsteps;
		this.nbcards = nbcards;
		this.numsequence = numsequence;
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}


	public int getNbsteps() {
		return nbsteps;
	}

	public void setNbsteps(int nbsteps) {
		this.nbsteps = nbsteps;
	}

	public int getNbcards() {
		return nbcards;
	}

	public void setNbcards(int nbcards) {
		this.nbcards = nbcards;
	}

	public int getNumsequence() {
		return numsequence;
	}

	public void setNumsequence(int numsequence) {
		this.numsequence = numsequence;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}

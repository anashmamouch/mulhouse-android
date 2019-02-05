/**
 ================================================================================

 OTIPASS
 sql package

 @author ED ($Author: ede $)

 @version $Rev: 6346 $
 $Id: Tablet.java 6346 2016-06-08 16:25:56Z ede $

 ================================================================================
 */
package com.otipass.sql;

public class Tablet {
	private long id;
	private int numSequence;
	private String downloadTime;
	private String uploadTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getNumSequence() {
		return numSequence;
	}

	public void setNumSequence(int numSequence) {
		this.numSequence = numSequence;
	}

	public String getDownloadTime() {
		return downloadTime;
	}

	public void setDownloadTime(String time) {
		this.downloadTime = time;
	}

	public String getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(String time) {
		this.uploadTime = time;
	}

}

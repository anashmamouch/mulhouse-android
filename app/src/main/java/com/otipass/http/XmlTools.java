/**
 ================================================================================

 OTIPASS
 http package

 @author ED ($Author: ede $)

 @version $Rev: 6419 $
 $Id: XmlTools.java 6419 2016-06-27 14:08:26Z ede $

 ================================================================================
 */
package com.otipass.http;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;

 import javax.crypto.spec.IvParameterSpec;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;

 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xmlpull.v1.XmlSerializer;

 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.net.Uri;
 import android.util.Log;
 import android.util.Xml;

 import com.otipass.sql.*;
 import com.otipass.synchronization.SynchronizationService;
 import com.otipass.tools.Messages;
 import com.otipass.tools.OtipassCard;
 import com.otipass.tools.tools;

 public class XmlTools {
	 private Document dom;
	 // lists used upon data download
	 private List<User> userList;
	 private List<Otipass> otipassList;
	 private List<Partial> partialList;
	 private List<Usage> useList;
	 private List<PartialServiceCpt> partialServiceCptList;
	 private List<Integer> useToDelete;
	 private List<Create> createList = null;
	 private List<Update> updateList;
	 private List<Integer> idUpdateList;
	 private List<Integer> idEntryList;
	 private List<Msg> messageList;
	 private List<Support> supportList;
	 private Param param = null;
	 private Stock stock = null;
	 private int wlNbCards;
	 private int wlNbSteps;
	 private List<ServicePass> serviceList;
	 private List<PackageObject> packageList;
	 private List<PackageService> packageServiceList;
	 private List<ProviderService> providerServiceList;
     private List<Integer> idBugList;

	 private int numSequence;
	 private static final String TAG = "adt67";

	 private static final String TAG_STATUS = "st";
	 private static final String TAG_CARD = "ca";
	 private static final String TAG_SERIAL = "sn";
	 private static final String TAG_NUMOTIPASS = "ot";
	 private static final String TAG_EXPIRY = "ex";
	 private static final String TAG_TARIF = "tarif";
	 private static final String TAG_TWIN = "tw";
	 private static final String TAG_TYPE = "ty";

	 private static final String TAG_CARDS = "cas";
	 private static final String TAG_STEPS = "steps";
	 private static final String TAG_RESPONSE = "response";
	 private static final String TAG_REQUEST = "request";
	 private static final String TAG_HEADER = "header";
	 private static final String TAG_VERSION = "version";
	 private static final String TAG_NUMSEQUENCE = "numsequence";
	 private static final String TAG_PARAM = "param";
	 private static final String TAG_NAME = "name";
	 private static final String TAG_LANG = "lang";
	 private static final String TAG_POLLING = "polling";
	 private static final String TAG_CALL = "call";
	 private static final String TAG_SOFT = "soft";
	 private static final String TAG_USER = "user";
	 private static final String TAG_LOGIN = "login";
	 private static final String TAG_PASSWORD = "password";
	 private static final String TAG_SALT = "salt";
	 private static final String TAG_PROFILE = "profile";
	 private static final String TAG_UPDATES = "upds";
	 private static final String TAG_UPDATE = "upd";
	 private static final String TAG_DATE = "date";
	 private static final String TAG_ACTION = "action";
	 private static final String TAG_ENTRY = "entry";
	 private static final String TAG_ENTRYS = "entrys";
	 private static final String TAG_WARNING = "warning";
	 private static final String TAG_WARNINGS = "warnings";
	 private static final String TAG_NB = "nb";
	 private static final String TAG_EVENT = "event";
	 private static final String TAG_DEBUG = "debug";
	 private static final String TAG_CURRENCY = "currency";
	 private static final String TAG_SERVICE_TARIF = "tarif";
	 private static final String TAG_SERVICE_TARIFS = "tarifs";
	 private static final String TAG_SERVICE = "service";
	 private static final String TAG_PRICE = "price";
	 private static final String TAG_NAME_FR = "fr";
	 private static final String TAG_NAME_DE = "de";
	 private static final String TAG_PROVIDER_CATEGORY = "provider_category";
	 private static final String TAG_ID = "id";
	 private static final String TAG_COUNTRY = "country";
	 private static final String TAG_STOCK = "stock";
	 private static final String TAG_ALERT = "alert";
	 private static final String TAG_THRESHOLD = "threshold";
	 private static final String TAG_NB_CARDS = "nb_cards";
	 private static final String TAG_MESSAGE = "message";
	 private static final String TAG_MESSAGES = "messages";
	 private static final String TAG_START_DATE = "start_date";
	 private static final String TAG_END_DATE = "end_date";
	 private static final String TAG_TEXT = "text";
	 private static final String TAG_CREATES = "crs";
	 private static final String TAG_CREATE = "cr";
	 private static final String TAG_DISCOUNT = "discount";
	 private static final String TAG_AMOUNT = "amount";
	 private static final String TAG_TYPE_SERVICE = "type";
	 private static final String TAG_SRV = "srv";
	 private static final String TAG_PID = "pid";
	 private static final String TAG_PACKAGES = "packages";
	 private static final String TAG_PACKAGE = "package";
	 private static final String TAG_NUMBER = "number";
	 private static final String TAG_CHILD = "child";
	 private static final String TAG_DURATION = "duration";
	 private static final String TAG_PERIOD = "period";
	 private static final String TAG_CATEGORY = "category";
	 private static final String TAG_FNAME = "fname";
	 private static final String TAG_TITLE = "title";
	 private static final String TAG_EMAIL = "email";
	 private static final String TAG_NEWSLETTER = "newsletter";
	 private static final String TAG_TWIN_PASS = "twin";
	 private static final String TAG_ACCOMODATION = "accomodation";
	 private static final String TAG_NB_STAY = "nbstay";
	 private static final String TAG_FIRST_STAY = "firststay";
	 private static final String TAG_SPACK = "spack";
	 private static final String TAG_POSTAL_CODE = "cpostal";
	 private static final String TAG_PROVIDER_ID = "provider_id";
	 private static final String TAG_CPT = "cpt";
	 private static final String TAG_DAY = "day";
	 private static final String TAG_REFERENCE = "ref";
	 private static final String TAG_EXCEPTIONS = "exceptions";
	 private static final String TAG_EXCEPTION = "exception";
	 private static final String TAG_MSG = "msg";
	 private static final String TAG_PARENT = "parent";
	 private static final String TAG_QUERY = "query";

	 public int getNumSequence() {
		 return numSequence;
	 }

	 public int getWlNbCards() {
		 return wlNbCards;
	 }

	 public int getWlNbSteps() {
		 return wlNbSteps;
	 }

	 public void setNumSequence(int numSequence) {
		 this.numSequence = numSequence;
	 }

	 private static String ParseElement(Element elt, String tagName) {
		 String content = "";
		 try {
			 NodeList nameList = elt.getElementsByTagName(tagName);
			 Element nameElement = (Element) nameList.item(0);
			 nameList = nameElement.getChildNodes();
			 if (nameList.getLength() != 0) {
				 content =  ((Node) nameList.item(0)).getNodeValue();
			 } 
		 } catch (Exception ex) {

		 }
		 return content;
	 }	



	 public  int decodeResponse(InputStream inputStream) {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 try {
			 DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			 DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
             dom = documentBuilder.parse(inputStream);
			 NodeList nodes = dom.getElementsByTagName(TAG_RESPONSE);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
                 status = Integer.valueOf(ParseElement((Element)node, TAG_STATUS));
			 }

		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeResponse - " + e.getMessage());
		 }
		 return status;
	 }

	 public  int decodeWLResponse(InputStream inputStream) {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 int version;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_HEADER);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
				 version = Integer.valueOf(ParseElement((Element)node, TAG_VERSION));
				 numSequence = Integer.valueOf(ParseElement((Element)node, TAG_NUMSEQUENCE));
				 if ((version > 0) && (numSequence > 0) ) {
					 status = SynchronizationService.cComOK;
				 }
			 } 
			 if (status == SynchronizationService.cComOK){
				 nodes = dom.getElementsByTagName(TAG_CARDS);
				 Node node = nodes.item(0);
				 wlNbCards = Integer.valueOf(ParseElement((Element)node, TAG_NB));
				 wlNbSteps = Integer.valueOf(ParseElement((Element)node, TAG_STEPS));
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeWLResponse - " + e.getMessage());
			 status = SynchronizationService.cComClientDecodeFailure;
		 }
		 return status;
	 }

	 public  int decodePartialWLResponse(InputStream inputStream) {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 int version;
		 try {

			 NodeList nodes = dom.getElementsByTagName(TAG_HEADER);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
				 version = Integer.valueOf(ParseElement((Element)node, TAG_VERSION));
				 numSequence = Integer.valueOf(ParseElement((Element)node, TAG_NUMSEQUENCE));
				 if ((version > 0) && (numSequence > 0) ) {
					 status = SynchronizationService.cComOK;
				 }
			 } 
			 if (status == SynchronizationService.cComOK){
				 nodes = dom.getElementsByTagName(TAG_CARDS);
				 Node node = nodes.item(0);
				 wlNbCards = Integer.valueOf(ParseElement((Element)node, TAG_NB));
				 wlNbSteps = Integer.valueOf(ParseElement((Element)node, TAG_STEPS));
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodePartialWLResponse - " + e.getMessage());
			 status = SynchronizationService.cComClientDecodeFailure;
		 }
		 return status;
	 }

	 public  int decodeUpdate() {
		 int status = SynchronizationService.cComOK, id;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_ID);
			 Node node;
			 if (nodes.getLength() > 0) {
				 idUpdateList = new ArrayList<Integer>();
				 for (int i=0; i<nodes.getLength(); i++) {
					 try {
						 node = nodes.item(i);
						 id = Integer.valueOf(node.getFirstChild().getNodeValue());
						 idUpdateList.add(id);
					 } catch (Exception e) {}
				 }
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeUpdate - " + e.getMessage());
			 status = SynchronizationService.cComClientDecodeFailure;
		 }
		 return status;
	 }

	 public  int decodeEntry() {
		 int status = SynchronizationService.cComOK, id;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_ID);
			 Node node;
			 if (nodes.getLength() > 0) {
				 idEntryList = new ArrayList<Integer>();
				 for (int i=0; i<nodes.getLength(); i++) {
					 try {
						 node = nodes.item(i);
						 id = Integer.valueOf(node.getFirstChild().getNodeValue());
						 idEntryList.add(id);
					 } catch (Exception e) {}
				 }
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeEntry - " + e.getMessage());
			 status = SynchronizationService.cComClientDecodeFailure;
		 }
		 return status;
	 }


	 public  int decodeWLTotal() {
		 int status = SynchronizationService.cComOK;
		 int version, pid;
		 short cardStatus, type;
		 String serial, expiry, service, day;
		 long numOtipass;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_CARD);
			 Node node;
			 if (nodes.getLength() > 0) {
				 otipassList = new ArrayList<Otipass>();
				 useList = new ArrayList<Usage>();
				 for (int i=0; i<nodes.getLength(); i++) {
					 node = nodes.item(i);
					 serial = ParseElement((Element)node, TAG_SERIAL);
					 numOtipass = Long.valueOf(ParseElement((Element)node, TAG_NUMOTIPASS));
					 cardStatus = Short.valueOf(ParseElement((Element)node, TAG_STATUS));
					 type = Short.valueOf(ParseElement((Element)node, TAG_TYPE));
					 pid = Integer.valueOf(ParseElement((Element)node, TAG_PID));
					 service = ParseElement((Element)node, TAG_SRV);
					 day = ParseElement((Element)node, TAG_DAY);
					 expiry = ParseElement((Element)node, TAG_EXPIRY);
					 otipassList.add(new Otipass(numOtipass, serial, cardStatus, expiry, type, pid, service));
					 if (day.length() > 0) {
						 String [] use_day = day.split(";");
						 for (int j = 0; j < use_day.length; j++) {
							 useList.add(new Usage((int)numOtipass, String.valueOf(Integer.valueOf(use_day[j].toString()))));
						 }
					 }
				 }
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeWLTotal - " + e.getMessage());
			 status = SynchronizationService.cComClientDecodeFailure;
		 }
		 return status;
	 }

	 public  int decodeWLPartial() {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 int version, numotipass, type, day = -1, pid = 1;
		 short cardStatus;
		 String serial, expiry, srv;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_HEADER);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
				 version = Integer.valueOf(ParseElement((Element)node, TAG_VERSION));
				 numSequence = Integer.valueOf(ParseElement((Element)node, TAG_NUMSEQUENCE));
				 if ((version > 0) && (numSequence > 0) ) {
					 status = SynchronizationService.cComOK;
				 }
			 } 
			 if (status == SynchronizationService.cComOK){
				 nodes = dom.getElementsByTagName(TAG_UPDATE);
				 Node node;
				 if (nodes.getLength() > 0) {
					 partialList = new ArrayList<Partial>();
					 for (int i=0; i<nodes.getLength(); i++) {
						 node = nodes.item(i);
						 numotipass = Integer.valueOf(ParseElement((Element)node, TAG_NUMOTIPASS));
						 cardStatus = Short.valueOf(ParseElement((Element)node, TAG_STATUS));
						 expiry = ParseElement((Element)node, TAG_EXPIRY);
						 if (ParseElement((Element)node, TAG_PID).length() > 0) {
							 pid = Integer.valueOf(ParseElement((Element)node, TAG_PID));
							 partialList.add(new Partial(numotipass, cardStatus, expiry, pid));
						}else {
							partialList.add(new Partial(numotipass, cardStatus, expiry, -1));
						}
						 
						 Log.d("PARTIAL", "partialList.add " + ParseElement((Element)node, TAG_NUMOTIPASS) + " - " + ParseElement((Element)node, TAG_STATUS) + " - " + ParseElement((Element)node, TAG_EXPIRY) + " - " + ParseElement((Element)node, TAG_PID));
					 }
				 }
			 }
			 if (status == SynchronizationService.cComOK){
				 nodes = dom.getElementsByTagName(TAG_CPT);
				 Node node;
				 if (nodes.getLength() > 0) {
					 partialServiceCptList = new ArrayList<PartialServiceCpt>();
					 useList = new ArrayList<Usage>();
					 useToDelete = new ArrayList<Integer>();
					 for (int i=0; i < nodes.getLength(); i++) {
						 node = nodes.item(i);
						 numotipass = Integer.valueOf(ParseElement((Element)node, TAG_NUMOTIPASS));
						 srv = ParseElement((Element)node, TAG_SRV);
						 String [] srv_ = srv.split(";");
						 if (ParseElement((Element)node, TAG_DAY).length() > 0) {
							 day = Integer.valueOf(ParseElement((Element)node, TAG_DAY));
							 Log.d("PARTIAL", "TAG_CPT with day " + ParseElement((Element)node, TAG_DAY));
						 }

						 partialServiceCptList.add(new PartialServiceCpt(numotipass, srv));
						 Log.d("PARTIAL", "partialServiceCptList.add " + ParseElement((Element)node, TAG_NUMOTIPASS) + " - " + ParseElement((Element)node, TAG_SRV));
						 if (day > 0  && srv_.length == 1) {
							 Log.d("PARTIAL", "useList.add " + ParseElement((Element)node, TAG_DAY));
							 useList.add(new Usage(numotipass, String.valueOf(day)));
						 }
						 if (day == 0) {
							 Log.d("PARTIAL", "useToDelete.add " + ParseElement((Element)node, TAG_NUMOTIPASS));
							 useToDelete.add(numotipass);
						 }
					 }
				 }
			 }
			 if (status == SynchronizationService.cComOK){
				 nodes = dom.getElementsByTagName(TAG_CREATE);
				 Node node;
				 if (nodes.getLength() > 0) {
					 createList = new ArrayList<Create>();
					 for (int i=0; i < nodes.getLength(); i++) {
						 node = nodes.item(i);
						 numotipass = Integer.valueOf(ParseElement((Element)node, TAG_NUMOTIPASS));
						 serial = ParseElement((Element)node, TAG_SERIAL);
						 type = Integer.valueOf(ParseElement((Element)node, TAG_TYPE));
						 cardStatus = Short.valueOf(ParseElement((Element)node, TAG_STATUS));
						 pid = Integer.valueOf(ParseElement((Element)node, TAG_PID));
						 srv = ParseElement((Element)node, TAG_SRV);
						 createList.add(new Create(numotipass, (short)type, serial, cardStatus, pid, srv, ""));
					 }
				 }
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeWLPartial - " + e.getMessage());
			 status = SynchronizationService.cComClientDecodeFailure;
		 }
		 return status;
	 }


	 public  int decodeParam() {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 int debug, idpackage, idservice, type, number, category, nbChild, duration, period;
		 String name, call, softVersion, package_name, service, ref;
		 double price;
		 PackageObject ps;

		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_PARAM);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
				 name = ParseElement((Element)node, TAG_NAME);
				 call = ParseElement((Element)node, TAG_CALL);
				 softVersion = ParseElement((Element)node, TAG_SOFT);
				 debug = Integer.valueOf(ParseElement((Element)node, TAG_DEBUG));
				 category = Integer.valueOf(ParseElement((Element)node, TAG_CATEGORY));
				 if ((name.length() > 0) && (call.length() > 0) && (softVersion.length() > 0) && (category > 0)) {
					 param = new Param(name, call, softVersion, debug, category);
				 }
			 } 

			 packageList = new ArrayList<PackageObject>();
			 serviceList = new ArrayList<ServicePass>();
			 packageServiceList = new ArrayList<PackageService>();
			 ArrayList<Integer> sIdList = new ArrayList<Integer>();
			 sIdList.clear();
			 NodeList nodesPackage = dom.getElementsByTagName(TAG_PACKAGE);
			 if (nodesPackage.getLength() > 0) {
				 for (int i=0; i<nodesPackage.getLength(); i++) {
					 Node node = nodesPackage.item(i);
					 idpackage = Integer.valueOf(ParseElement((Element)node, TAG_ID));
					 package_name = ParseElement((Element)node, TAG_NAME); 
					 ref = ParseElement((Element)node, TAG_REFERENCE); 
					 duration = Integer.valueOf(ParseElement((Element)node, TAG_DURATION));
					 period = Integer.valueOf(ParseElement((Element)node, TAG_PERIOD));
					 price = Double.valueOf(ParseElement((Element)node, TAG_PRICE));
					 packageList.add(new PackageObject(idpackage, package_name, duration, period, price, ref));
					 nodes = node.getChildNodes();
					 if (nodes.getLength() > 0) {
						 for (int j=0; j<nodes.getLength(); j++) {
							 if (nodes.item(j).getNodeName().equals(TAG_SERVICE)) {
								 Node nodeService = nodes.item(j);
								 idservice = Integer.valueOf(ParseElement((Element)nodeService, TAG_ID));
								 type = Integer.valueOf(ParseElement((Element)nodeService, TAG_TYPE_SERVICE));
								 name = ParseElement((Element)nodeService, TAG_NAME);
								 number = Integer.valueOf(ParseElement((Element)nodeService, TAG_NUMBER));
								 if ((idservice > 0) && (name.length() > 0) && (type > 0) && (number > 0)) {
									 status = SynchronizationService.cComOK;
									 if (!sIdList.contains(idservice))  {
										 sIdList.add(idservice);
										 serviceList.add(new ServicePass(idservice, type, name));
									 }
								 }
								 packageServiceList.add(new PackageService(idpackage, idservice, number));
							 }
						 }
					 }
				 } 
			 }
			 providerServiceList = new ArrayList<ProviderService>();
			 nodes = dom.getElementsByTagName(TAG_SPACK);
			 if (nodes.getLength() > 0) {
				 for (int j=0; j<nodes.getLength(); j++) {
					 Node node = nodes.item(j);
					 idpackage = Integer.valueOf(ParseElement((Element)node, TAG_ID));
					 service = ParseElement((Element)node, TAG_SRV);
					 if ((idpackage > 0) && (service.length() > 0)) {
						 providerServiceList.add(new ProviderService(idpackage, service));
					 }
				 }
			 } 
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeParam - " + e.getMessage());
		 }
		 return status;
	 }

	 public  int decodeUser() {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 short profile;
		 String login, password, salt;
		 int id;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_USER);
			 if (nodes.getLength() > 0) {
				 Node node;
				 status = SynchronizationService.cComOK;
				 userList = new ArrayList<User>();
				 for (int i=0; i<nodes.getLength(); i++) {
					 node = nodes.item(i);
					 id = Integer.valueOf(ParseElement((Element)node, TAG_ID));
					 login = ParseElement((Element)node, TAG_LOGIN);
					 password = ParseElement((Element)node, TAG_PASSWORD);
					 salt = ParseElement((Element)node, TAG_SALT);
					 profile = Short.valueOf(ParseElement((Element)node, TAG_PROFILE));
					 userList.add(new User(id, login, password, salt, profile));
				 }
			 } 
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeUser - " + e.getMessage());
		 }
		 return status;
	 }

	 public  int decodeStock() {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 int nbCards = 0, threshold = 0, alert = 0, provider_id = 0;
		 stock = null;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_STOCK);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
				 provider_id = Integer.valueOf(ParseElement((Element)node, TAG_PROVIDER_ID));
				 nbCards = Integer.valueOf(ParseElement((Element)node, TAG_NB_CARDS));
				 threshold = Integer.valueOf(ParseElement((Element)node, TAG_THRESHOLD));
				 alert = Integer.valueOf(ParseElement((Element)node, TAG_ALERT));
				 stock = new Stock(provider_id, nbCards, threshold, alert);
				 status = SynchronizationService.cComOK;
			 } 
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeStock - " + e.getMessage());
		 }
		 return status;
	 }

     public  int decodeBug() {
         int status = SynchronizationService.cComOK, id;
         try {
             NodeList nodes = dom.getElementsByTagName(TAG_ID);
             Node node;
             if (nodes.getLength() > 0) {
                 idBugList = new ArrayList<Integer>();
                 for (int i=0; i < nodes.getLength(); i++) {
                     try {
                         node = nodes.item(i);
                         id = Integer.valueOf(node.getFirstChild().getNodeValue());
                         idBugList.add(id);
                     } catch (Exception e) {}
                 }
             }
         } catch (Exception e) {
             Log.e(TAG, XmlTools.class.getName() + " - decodeBug - " + e.getMessage());
             status = SynchronizationService.cComClientDecodeFailure;
         }
         return status;
     }

	 public  Otipass decodeCheckNumOtipass() {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 int idotipass = 0, pid = 1;
		 String expiryDate = null, service = null, day = "", serial="";
		 long numotipass = 0;
		 short st = 0, type = 0;
		 Otipass otipassChecked = null;

		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_CARD);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
				 if (node.getFirstChild() != null) {

					 if (ParseElement((Element)node, TAG_ID) != null) {
						 idotipass = Integer.valueOf(ParseElement((Element)node, TAG_ID));
					 }
					 if (ParseElement((Element)node, TAG_NUMOTIPASS) != null) {
						 numotipass = Long.valueOf(ParseElement((Element)node, TAG_NUMOTIPASS));
					 }
					 if (ParseElement((Element)node, TAG_SERIAL) != null) {
						 serial = ParseElement((Element)node, TAG_SERIAL);
					 }
					 if (ParseElement((Element)node, TAG_TYPE) != null) {
						 type = Short.valueOf(ParseElement((Element)node, TAG_TYPE));
					 }
					 if (ParseElement((Element)node, TAG_STATUS) != null) {
						 st = Short.valueOf(ParseElement((Element)node, TAG_STATUS));
					 }
					 if (ParseElement((Element)node, TAG_PID) != null) {
						 pid = Integer.valueOf(ParseElement((Element)node, TAG_PID));
					 }
					 if (ParseElement((Element)node, TAG_EXPIRY) != null) {
						 expiryDate = ParseElement((Element)node, TAG_EXPIRY);
					 }
					 if (ParseElement((Element)node, TAG_SRV) != null) {
						 service = ParseElement((Element)node, TAG_SRV);
					 }
					 if (ParseElement((Element)node, TAG_DAY) != null) {
						 day = ParseElement((Element)node, TAG_DAY);	
					}

					 Log.d("CHECK", "decodeCheckNumOtipass " + ParseElement((Element)node, TAG_EXPIRY) + " - " + ParseElement((Element)node, TAG_SRV) + " - " + ParseElement((Element)node, TAG_PID));
					 if (expiryDate != null && service != null) {
						 otipassChecked = new Otipass(numotipass, serial, st, expiryDate, type, pid, service, day);
					 }else if (expiryDate != null && service == null) {
						 otipassChecked = new Otipass(numotipass, serial, st, expiryDate, type, pid, "", day);
					 }else if (expiryDate == null && service != null) {
						 otipassChecked = new Otipass(numotipass, serial, st, "", type, pid, service, day);
					 }
				 }
				 status = SynchronizationService.cComOK;
			 } 
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeCheckNumOtipass - " + e.getMessage());
		 }
		 return otipassChecked;
	 }

	 public int decodeMessages() {
		 int status = SynchronizationService.cComClientDecodeFailure, id;
		 String text, startDate, endDate, lang;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_MESSAGE);
			 if (nodes.getLength() > 0) {
				 Node node;
				 messageList = new ArrayList<Msg>();
				 for (int i=0; i<nodes.getLength(); i++) {
					 node = nodes.item(i);
					 id = Integer.valueOf(ParseElement((Element)node, TAG_ID));
					 text = ParseElement((Element) node, TAG_TEXT);
					 startDate = ParseElement((Element)node, TAG_START_DATE);
					 endDate = ParseElement((Element)node, TAG_END_DATE);
					 lang = ParseElement((Element)node, TAG_LANG);
					 messageList.add(new Msg(text, id, Messages.cMsgGeneral, startDate, endDate, lang));
				 }
				 status = SynchronizationService.cComOK;
			 } else {
				 status = SynchronizationService.cComOK;
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeMessages - " + e.getMessage());
		 }

		 return status;
	 }

	 public int decodeSupport() {
		 int status = SynchronizationService.cComClientDecodeFailure, idmsg, event, parent;
		 String text, date, query;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_MESSAGE);
			 if (nodes.getLength() > 0) {
				 Node node;
				 supportList = new ArrayList<Support>();
				 for (int i = 0; i < nodes.getLength(); i++) {
					 node     = nodes.item(i);
					 idmsg    = Integer.valueOf(ParseElement((Element)node, TAG_ID));
					 text     = ParseElement((Element)node, TAG_TEXT);
					 date     = ParseElement((Element)node, TAG_DATE);
					 query    = ParseElement((Element)node, TAG_QUERY);
					 event  = Integer.valueOf(ParseElement((Element)node, TAG_EVENT));
					 parent = Integer.valueOf(ParseElement((Element)node, TAG_PARENT));
					 supportList.add(new Support(text, Messages.cMsgSupport, idmsg, date, parent, event, query));
				 }
				 status = SynchronizationService.cComOK;
			 } else {
				 status = SynchronizationService.cComOK;
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeSupport - " + e.getMessage());
		 }

		 return status;
	 }


	 @SuppressLint("NewApi")
	 public OtipassCard decodeCheck() {
		 int status = SynchronizationService.cComClientDecodeFailure;
		 String expiry;
		 int pStatus, type, numotipass;
		 OtipassCard card = null;
		 try {
			 NodeList nodes = dom.getElementsByTagName(TAG_CARD);
			 if (nodes.getLength() > 0) {
				 Node node = nodes.item(0);
				 numotipass = Integer.valueOf(ParseElement((Element)node, TAG_NUMOTIPASS));
				 pStatus = Integer.valueOf(ParseElement((Element)node, TAG_STATUS));
				 type = Integer.valueOf(ParseElement((Element)node, TAG_TYPE));
				 expiry = ParseElement((Element)node, TAG_EXPIRY);
				 card = new 	OtipassCard(type, numotipass, pStatus);
				 if (!expiry.isEmpty()) {
					 card.setExpiry(tools.setCalendar(expiry));
				 }

				 status = SynchronizationService.cComOK;
			 } 
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - decodeCheck - " + e.getMessage());
		 }

		 return card;
	 }

	 @SuppressLint("NewApi")
	 public String  buildUploadXml(List<Update> updateList, Context context) {
		 Update update;
		 String xmlString = "", nameSpace = "";
		 XmlSerializer serializer = Xml.newSerializer();
		 StringWriter writer = new StringWriter();
		 boolean mustUpload = false;
		 try {
			 serializer.setOutput(writer);
			 serializer.startDocument("UTF-8", true);
			 serializer.startTag(nameSpace, TAG_REQUEST);
			 serializer.startTag(nameSpace, TAG_HEADER);
			 serializer.startTag(nameSpace, TAG_SERIAL);
			 serializer.text(tools.getDeviceUID(context));
			 serializer.endTag(nameSpace, TAG_SERIAL);
			 serializer.endTag(nameSpace, TAG_HEADER);
			 serializer.startTag(nameSpace, TAG_ACTION);
			 if (updateList.size() > 0) {
				 mustUpload = true;
				 serializer.startTag(nameSpace, TAG_UPDATES);
				 for (int i=0; i<updateList.size(); i++) {
					 update = updateList.get(i);
					 serializer.startTag(nameSpace, TAG_UPDATE);
					 serializer.startTag(nameSpace, TAG_ID);
					 serializer.text(String.valueOf(update.getId()));
					 serializer.endTag(nameSpace, TAG_ID);
					 serializer.startTag(nameSpace, TAG_DATE);
					 serializer.text(update.getDate());
					 serializer.endTag(nameSpace, TAG_DATE);
					 serializer.startTag(nameSpace, TAG_TYPE);
					 serializer.text(String.valueOf(update.getType()));
					 serializer.endTag(nameSpace, TAG_TYPE);
					 serializer.startTag(nameSpace, TAG_NUMOTIPASS);
					 serializer.text(String.valueOf(update.getNumotipass()));
					 serializer.endTag(nameSpace, TAG_NUMOTIPASS);
					 serializer.startTag(nameSpace, TAG_PID);
					 serializer.text(String.valueOf(update.getPid()));
					 serializer.endTag(nameSpace, TAG_PID);
					 if (update.getName() != null) {
						 serializer.startTag(nameSpace, TAG_NAME);
						 serializer.text(String.valueOf(update.getName()));
						 serializer.endTag(nameSpace, TAG_NAME);
					 }
					 if (update.getFname() != null) {
						 serializer.startTag(nameSpace, TAG_FNAME);
						 serializer.text(update.getFname());
						 serializer.endTag(nameSpace, TAG_FNAME);
					 }
					 if (update.getEmail() != null) {
						 serializer.startTag(nameSpace, TAG_EMAIL);
						 serializer.text(update.getEmail());
						 serializer.endTag(nameSpace, TAG_EMAIL);
					 } 
					 if (update.getCountry() != null) {
						 serializer.startTag(nameSpace, TAG_COUNTRY);
						 serializer.text(String.valueOf(update.getCountry()));
						 serializer.endTag(nameSpace, TAG_COUNTRY);
					 }
					 if (update.getPostalCode() != null) {
						 serializer.startTag(nameSpace, TAG_POSTAL_CODE);
						 serializer.text(update.getPostalCode());
						 serializer.endTag(nameSpace, TAG_POSTAL_CODE);
					 }
					 if (update.getNewsletter() != 0) {
						 serializer.startTag(nameSpace, TAG_NEWSLETTER);
						 serializer.text(String.valueOf(update.getNewsletter()));
						 serializer.endTag(nameSpace, TAG_NEWSLETTER);
					 }
					 if (update.getTwin()!= 0) {
						 serializer.startTag(nameSpace, TAG_TWIN_PASS);
						 serializer.text(String.valueOf(update.getTwin()));
						 serializer.endTag(nameSpace, TAG_TWIN_PASS);
					 }


					 serializer.endTag(nameSpace, TAG_UPDATE);
				 }
				 serializer.endTag(nameSpace, TAG_UPDATES);
			 }
			 serializer.endTag(nameSpace, TAG_ACTION);
			 serializer.endTag(nameSpace, TAG_REQUEST);
			 serializer.endDocument();
			 if (mustUpload) {
				 xmlString =  writer.toString();
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - buildUploadXml - " + e.getMessage());
		 } 
		 return xmlString;
	 }

	 @SuppressLint("NewApi")
	 public String  buildEntryXml(List<Entry> entryList, Context context) {
		 Entry entry;
		 String xmlString = "", nameSpace = "";
		 XmlSerializer serializer = Xml.newSerializer();
		 StringWriter writer = new StringWriter();
		 boolean mustUpload = false;
         try {
			 serializer.setOutput(writer);
             serializer.startDocument("UTF-8", true);
			 serializer.startTag(nameSpace, TAG_REQUEST);
             serializer.startTag(nameSpace, TAG_HEADER);
             serializer.startTag(nameSpace, TAG_SERIAL);
             serializer.text(tools.getDeviceUID(context));
             serializer.endTag(nameSpace, TAG_SERIAL);
			 serializer.endTag(nameSpace, TAG_HEADER);
			 if (entryList.size() > 0) {
				 mustUpload = true;
				 serializer.startTag(nameSpace, TAG_ENTRYS);
				 for (int i=0; i<entryList.size(); i++) {
					 entry = entryList.get(i);
					 serializer.startTag(nameSpace, TAG_ENTRY);
					 serializer.startTag(nameSpace, TAG_ID);
					 serializer.text(String.valueOf(entry.getId()));
					 serializer.endTag(nameSpace, TAG_ID);
					 serializer.startTag(nameSpace, TAG_NUMOTIPASS);
					 serializer.text(String.valueOf(entry.getNumotipass()));
					 serializer.endTag(nameSpace, TAG_NUMOTIPASS);
					 serializer.startTag(nameSpace, TAG_DATE);
					 serializer.text(entry.getDate());
					 serializer.endTag(nameSpace, TAG_DATE);
					 serializer.startTag(nameSpace, TAG_EVENT);
					 serializer.text(String.valueOf(entry.getEvent()));
					 serializer.endTag(nameSpace, TAG_EVENT);
					 serializer.startTag(nameSpace, TAG_NB);
					 serializer.text(String.valueOf(entry.getNb()));
					 serializer.endTag(nameSpace, TAG_NB);
					 serializer.startTag(nameSpace, TAG_SERVICE);
					 serializer.text(String.valueOf(entry.getService()));
					 serializer.endTag(nameSpace, TAG_SERVICE);
					 serializer.endTag(nameSpace, TAG_ENTRY);
				 }
				 serializer.endTag(nameSpace, TAG_ENTRYS);
			 }
			 serializer.endTag(nameSpace, TAG_REQUEST);
			 serializer.endDocument();
			 if (mustUpload) {
				 xmlString =  writer.toString();
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - buildEntryXml - " + e.getMessage());
		 } 
		 return xmlString;
	 }

	 public String  buildBugXml(List<Bug> bugList, Context context) {
		 Bug bug;
		 String xmlString = "", nameSpace = "";
		 XmlSerializer serializer = Xml.newSerializer();
		 StringWriter writer = new StringWriter();
		 boolean mustUpload = false;
		 try {
			 serializer.setOutput(writer);
			 serializer.startDocument("UTF-8", true);
			 serializer.startTag(nameSpace, TAG_REQUEST);
			 serializer.startTag(nameSpace, TAG_HEADER);
			 serializer.startTag(nameSpace, TAG_SERIAL);
			 serializer.text(tools.getDeviceUID(context));
			 serializer.endTag(nameSpace, TAG_SERIAL);
			 serializer.endTag(nameSpace, TAG_HEADER);
			 if (bugList.size() > 0) {
				 mustUpload = true;
				 serializer.startTag(nameSpace, TAG_EXCEPTIONS);
				 for (int i=0; i < bugList.size(); i++) {
					 bug = bugList.get(i);
                     serializer.startTag(nameSpace, TAG_EXCEPTION);
					 serializer.startTag(nameSpace, TAG_ID);
					 serializer.text(String.valueOf(bug.getId()));
					 serializer.endTag(nameSpace, TAG_ID);
					 serializer.startTag(nameSpace, TAG_DATE);
                     serializer.text(bug.getDate());
					 serializer.endTag(nameSpace, TAG_DATE);
					 serializer.startTag(nameSpace, TAG_MSG);
                     serializer.text(bug.getText());
					 serializer.endTag(nameSpace, TAG_MSG);
					 serializer.endTag(nameSpace, TAG_EXCEPTION);
				 }
				 serializer.endTag(nameSpace, TAG_EXCEPTIONS);
			 }
			 serializer.endTag(nameSpace, TAG_REQUEST);
			 serializer.endDocument();
			 if (mustUpload) {
				 xmlString =  writer.toString();
			 }
		 } catch (Exception e) {
			 Log.e(TAG, XmlTools.class.getName() + " - buildBugXml - " + e.getMessage());
		 }
		 return xmlString;
	 }


	 public List<User> getUserList() {
		 if (userList == null) {
			 userList = new ArrayList<User>();
		 }
		 return userList;
	 }

	 public List<Msg> getMessageList() {
		 if (messageList == null) {
			 messageList = new ArrayList<Msg>();
		 }
		 return messageList;
	 }

	 public List<Support> getSupportList() {
		 if (supportList == null) {
			 supportList = new ArrayList<Support>();
		 }
		 return supportList;
	 }

	 public List<Otipass> getOtipassList() {
		 if (otipassList == null) {
			 otipassList = new ArrayList<Otipass>();
		 }
		 return otipassList;
	 }

	 public Param getParam() {
		 return param;
	 }

	 public Stock getStock() {
		 return stock;
	 }


	 public List<Update> getUpdateList() {
		 if (updateList == null) {
			 updateList = new ArrayList<Update>();
		 }
		 return updateList;
	 }

	 public List<Partial> getPartialList() {
		 if (partialList == null) {
			 partialList = new ArrayList<Partial>();
		 }
		 return partialList;
	 }

	 public List<PartialServiceCpt> getPartialSrvCptList() {
		 if (partialServiceCptList == null) {
			 partialServiceCptList = new ArrayList<PartialServiceCpt>();
		 }
		 return partialServiceCptList;
	 }

	 public List<Usage> getUseList() {
		 if (useList == null) {
			 useList = new ArrayList<Usage>();
		 }
		 return useList;
	 }

	 public List<Integer> getuseToDeleteList(){
		 if (useToDelete == null) {
			 useToDelete = new ArrayList<Integer>();
		 }
		 return useToDelete;
	 }

	 public List<Create> getCreateList() {
		 if (createList == null) {
			 createList = new ArrayList<Create>();
		 }
		 return createList;
	 }


	 public List<Integer> getIdUpdateList() {
		 if (idUpdateList == null) {
			 idUpdateList = new ArrayList<Integer>();
		 }
		 return idUpdateList;
	 }

	 public List<Integer> getIdEntryList() {
		 if (idEntryList == null) {
			 idEntryList = new ArrayList<Integer>();
		 }
		 return idEntryList;
	 }

	 public List<PackageObject> getPackageList(){

		 return packageList;
	 }

	 public List<ServicePass> getServiceList(){
		 return serviceList;
	 }

	 public List<PackageService> getPackageServiceList(){
		 return packageServiceList;
	 }

	 public List<ProviderService> getProviderServiceList(){
		 return providerServiceList;
	 }

     public List<Integer> getIdBugList() {
         if (idBugList == null) {
             idBugList = new ArrayList<Integer>();
         }
         return idBugList;
     }

 }

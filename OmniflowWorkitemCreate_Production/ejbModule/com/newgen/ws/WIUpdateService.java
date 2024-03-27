/***************************************************************************
 *       Product         : OmniFlow
 *       Application     : WISearchService
 *       File            : WISearchService.java
 *       Author          : Sahiba Matta
 *       Created on      : December 3, 2015
 *       Purpose         : 
 *
 * Change History  :
 *********************************************************************************************************************
 * Date			Problem No           description                                             changed by
 *---------		---------	-----------------------                                     -------------
 ***********************************************************************************************************/

package com.newgen.ws;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import com.newgen.custom.CreateWorkitem;
import com.newgen.ws.exception.WICreateException;
import com.newgen.ws.request.Attribute;
import com.newgen.ws.request.Attributes;
import com.newgen.ws.request.WICreateRequest;
import com.newgen.ws.response.WICreateResponse;
import com.newgen.ws.util.XMLParser;

import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;
import adminclient.OSASecurity;

public class WIUpdateService extends CreateWorkitem
{
	
	
	//CONSTANTS
	final private String SUCCESS_STATUS="0";
	//TABLE NAME CONSTANTS
	final private String WSR_PROCESS="USR_0_WSR_UPDATE_PROCESS";
	final private String WSR_ATTRDETAILS="USR_0_WSR_UPDATE_ATTRDETAILS";
	final private String WSR_POSSIBLEQUEUES="USR_0_WSR_UPDATE_POSSIBLEQUEUES";
	
	// For RMT
	final private String RMT_WIHISTORY = "USR_0_RMT_WIHISTORY";
	//For RAOP
	final private String RAOP_WIHISTORY = "USR_0_RAOP_WIHISTORY";
	//For BSR
	final private String BSR_WIHISTORY = "USR_0_BSR_WIHISTORY";
	
	//For CDOB Biometric status update
	final private String DOB_EXTTABLE="NG_DOB_EXTTABLE";
	

	private String sProcessName;
	private String sSubProcess;
	private String sMsgFormat;
	private String sMsgVersion;
	private String sRequestorChannelId;
	private String sRequestorUserId;
	private String sRequestorLanguage;
	private String sRequestorSecurityInfo;
	private String sMessageId;
	private String sExtra1;
	private String sExtra2;
	private EE_EAI_HEADER headerObjRequest;
	private Attributes attributesObj;
	private Attribute attributeObj[];
	 String InputMessage = "";
	 
	 String docName="";
	 String docType="";
	 String base64="";
	 
	 String lstrDocFileSize="";
	 
//	 String sDocumentName;
//	 String sDocumentType;
//	 String sDocBase64;
	 
	String sTotalRetrieved = "";
	String sDumpLocation = "";
	
	JPISIsIndex NewIsIndex;
	JPDBRecoverDocData dataJPDB;
	
	
	//Configuration Parameters
	private String sCabinetName = "";
	private String sJtsIp = "";
	private int iJtsPort;
	private String sUsername = "";
	private String sPassword = "";
	private String sTempLoc = "";
	private int iVolId;
	//Response Parameters
	String eCode = "";
	String eDesc = ""; 
  	//Other vriables/objects
	WICreateResponse response = new WICreateResponse();	
	EE_EAI_HEADER headerObjResponse;
	private ResourceBundle pCodes;
	private String sSessionID = "";
	HashMap<String, String> hm = new HashMap(); 
	String sInputXML = "";
	String sOutputXML = "";
	XMLParser xmlobj;
	private String attributeTag = "";
	private String extColNames = "";
	private String extColValues = "";
	String sFilePath = "";
	boolean sessionFlag = false;
	private String processID = "";
	private String trTableColumn = "";
	private String trTableValue = "";
	private String sWhereClause ="";
	private String wiName = "";
	private String externalTableName = "";
	private String transactionTableName = "";
	private String processDefID = "";
	private String sDate = "";
	
	//for RMT process
	private String Dec_RemittanceChecker= "";
	
	private String WINumber = "";
	private String ActivityName="";
	private String PossibleQueues="";
	private String sUID_UPDATE="";
	private String sWorkitemid = "";
	//for RAOP process
	private String sCUST_AUTHENTICATION="";
	private String sRemarksAddInfo="";
	private String FircoUpdateActionDCC="";
		 
	public WICreateResponse wiUpdate(WICreateRequest request,String attributeList[]) throws WICreateException { 
		
		try
			{
			WriteLog("inside wiUpdate 1");
			headerObjResponse=new EE_EAI_HEADER();
			//initialize Logger
			//initializeLogger();	
			
			//Load file configuration
			loadConfiguration();
			
			//Load ResourceBundle
			loadResourceBundle();
			
			//Fetching request parameters 
			fetchRequestParameters(request);
			
			//Validating Input Parameters and Check Attribute Name from USR_0_WSR_UPDATE_ATTRDETAILS
			validateRequestParameters(attributeList);
			
			//Checking existing session
			checkExistingSession();
			
			// inserting update data in transaction table 
			//insertIntoTransationTableforUpdate(attributeList);
			
        	if ("RMT".equalsIgnoreCase(sProcessName))
        	{
        		updateInDataInExtTableRMT(); // all data updated in External table
        		
        		if (!ActivityName.equalsIgnoreCase("Attach_Cust_Doc"))
        		{
        			updateValidTillInWorklistTable(); // updating ValidTill in worklist table for expiring workitem
        			updateValidTillInWorkInProcessTable(); // updating ValidTill in workinprocess table for expiring workitem if workitem if locked
        		}
        		
        		insertIntoHistoryRMT(); // inserting in history table
        		updateHistoryRMT(); // updating history of existing record
        	}
        	
            
            //Added by Kamran 06062022 - WI Update from Intellect	
			if ("BSR".equalsIgnoreCase(sProcessName)) {
				
				//To check the current Activity Name
				WINumber=hm.get("WI_NAME");
				String getTableNameQry="select ACTIVITYNAME  from WFINSTRUMENTTABLE with (nolock) where PROCESSINSTANCEID='"+WINumber+"'";
				sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
				WriteLog("Input XML to Get Activity Name: "+sInputXML);
				sOutputXML=executeAPI(sInputXML);
				WriteLog("Output XML to Get Activity Name: "+sOutputXML);
				xmlobj=new XMLParser(sOutputXML);
				checkCallsMainCode(xmlobj);
				ActivityName=getTagValues(sOutputXML, "ACTIVITYNAME");
				WriteLog("ActivityName from QueueView: "+ActivityName);
				
				//Check to validate the current work step
				if (ActivityName.equals("Intellect_Hold")) {
					updateFieldsForIntellectToBSR();
				} else {
					throw new WICreateException("9001", pCodes.getString("9001") + ":" + sProcessName);
				}
			}
            
			// Hritik 2.6.22 - WI update call 
        	if("DigitalAO".equalsIgnoreCase(sProcessName) && !"Dispatch".equals(sSubProcess))
        	{
        		WriteLog("DigitalAO - process : DigitalAO - subprocess : Start");
        		
        		String is_stp = getSTPflag();
        		WriteLog("is_stp flag : "+is_stp);
        		if(is_stp.equalsIgnoreCase("Y") || is_stp.equalsIgnoreCase("Yes"))
        		{
        			// for stp cases -  checking mandate tags, update in 2nd table then done WI using utility.
        			checkConditionMandatoryForDataUpdate_DAO_stp();
        			insert_to_ng_dao_wi_update();
        			updateInDataInExtTableDAO();
        		}
        		else if(is_stp.equalsIgnoreCase("N") || is_stp.equalsIgnoreCase("No"))
        		{
        			/* by Hritik - 22.7.2022 [ On basis of event - checking mandate tags and if tag comes in call update in WI ] */
        			updatefieldsFor_DAO_attributes(); 
        			if(hm.get("Event").equalsIgnoreCase("Additional details provided"))
        			{
        				upadteRepetitive_DAO_attribute(); // updating rep tags if received in xml.
        				doneworkitem();
        				insertIntoHistoryDAO(); // done wi if addns details received.
        			}
        			if(hm.get("Event").equalsIgnoreCase("Account Created"))
        			{
        				insert_to_ng_dao_wi_update(); // updating ecrn,chqbk for wi in 2nd table.
        			}
        		}
        		else{
        			WriteLog("is_stp flag is null or invalid:"+is_stp);
        		}
        		
        		WriteLog("DigitalAO : DigitalAO");
        	}
        	
        	if("DigitalAO".equalsIgnoreCase(sProcessName) && "Dispatch".equals(sSubProcess))
        	{
        		WriteLog("DigitalAO - process : Dispatch - subprocess : Start");
        		//vinayak changed not to done wi for delivry status= picked
        		String Delivery_Status_value=hm.get("Delivery_Status");
        		String Status_Code_value=hm.get("Status_Code");
        		WriteLog("DigitalAO - process : Dispatch - subprocess : Delivery_Status "+Delivery_Status_value);
        		WriteLog("DigitalAO - process : Dispatch - subprocess : Status_Code_value "+Status_Code_value);
        		if("CHK".equalsIgnoreCase(Status_Code_value)){
        			updateInDataInExtTableDAO_dipatch();
        			WriteLog("DigitalAO - process : Dispatch - subprocess : courier picked update done success");
        		}
        		else{
	        		updateRepetitive_DAO_attribute_dispatch();
	        		updatefieldsFor_DAO_Dispatch_attributes();
	        		updateInDataIn_NG_Digital_AWB_Status();    		
	        		doneworkitem();           	
	        		insertIntoHistoryDAO_dispatch();
        		}
        		WriteLog("DigitalAO - process : Dispatch - subprocess : doneworkitem");
        	}

        	//Added by Sajan for FALCON Update WI
        	if("CDOB".equalsIgnoreCase(sProcessName)){
        		/*if("Customer_Hold".equalsIgnoreCase(ActivityName) && "DataUpdate".equalsIgnoreCase(hm.get("OperationName"))){
        			updateFieldsForFalconCustomerHold();   //Updating the fields for customer Hold WS
        		}*/
        		if(ActivityName.toUpperCase().contains("CUSTOMER_HOLD") && "DataUpdate".equalsIgnoreCase(hm.get("OperationName"))){
        			updateFieldsForFalconCustomerHold();   //Updating the fields for customer Hold WS
        		}
        		else if("Disbursal_Hold".equalsIgnoreCase(ActivityName) && "BiometricUpdate".equalsIgnoreCase(hm.get("OperationName"))){
        			updateBiometricStatus();	//Updating only biometric status at Disbursal hold WS
        		}
        		else{
        			throw new WICreateException("6014",pCodes.getString("6014")+":"+sProcessName);
        		}
        	}
        	
        	if ("RAOP".equalsIgnoreCase(sProcessName))
        	{
        		if(processID.equalsIgnoreCase("RAOP_YAP"))
        			updateInDataInExtTableRAOP(); // all data updated in External table for 2nd call from YAP
        		if(processID.equalsIgnoreCase("RAOP_YAP_AddInfo"))
        			updateInDataInExtTableRAOPAddInfo(); // all data updated in External table for Add Info from YAP
        		insertIntoHistoryRAOP(); // inserting in history table
        		doneworkitem();
        	}
        	
        	/**Ravindra Kumar -- 15-07-2022**/
        	if("Digital_CC".equalsIgnoreCase(sProcessName))
        	{
        		// Put all the sub process here with conditions.
        		WriteLog("DigitalCC - process");
        		String decisionInHistory = "";
        		String remark = "";
				if ("FIRCO_DOC_RECEIVED".equalsIgnoreCase(sSubProcess)){
        			upadteRepetitiveAttributeDCC();
        			trTableColumn=extColNames+"FircoUpdateAction, IsFTSDocProvided";
        			trTableValue=extColValues+"'Actioned','Y'";
        			decisionInHistory = "Firco Doc Received";
        			remark=sRemarksAddInfo;
            	}
        		
        		if ("FIRCO_NO_ACTION".equalsIgnoreCase(sSubProcess)){
        			trTableColumn=extColNames+"IsFTSDocProvided, FircoUpdateAction, FIRCO_Status";
        			trTableValue=extColValues+"'N','Decline','N'";
        			decisionInHistory = "Firco Not Actioned - to be Decline";
        			remark=sRemarksAddInfo;
        		}
        		
        		if ("STATEMENT_ANALYZED".equalsIgnoreCase(sSubProcess))
        		{
        			WriteLog("WI update for STATEMENT_ANALYZED- extColNames: "+extColNames);
        			WriteLog("WI update for STATEMENT_ANALYZED- extColValues: "+extColValues);
        			
        			trTableColumn=extColNames+"IsFTSDocProvided, FTS_Ack_flg, Decision_FTS, Decision"; // Decision_FTS added HRITIK 12/11/23
        			trTableValue=extColValues+"'Y','A','Approve','Approve'";
        			
        			WriteLog("WI update for STATEMENT_ANALYZED- trTableColumn: "+trTableColumn);
        			WriteLog("WI update for STATEMENT_ANALYZED- trTableValue: "+trTableValue);
        			
        			decisionInHistory = "FTS Details Received";
        			remark="FTS Details Updated";
        		}
        		
        		if ("NO_BANK_STATEMENT_ANALYSIS".equalsIgnoreCase(sSubProcess))
        		{
        			String IsFTSDocProvided = "";
        			String FTS_Ack_flg = "";
    				if ("Y".equalsIgnoreCase(hm.get("UW_reqd"))) {
    					IsFTSDocProvided="Y";
    					FTS_Ack_flg="NA";
    					hm.put("UW_reqd","NACK");//changed as per discussion with simi and deepak sir by om 20/10/22
    				} else if ("N".equalsIgnoreCase(hm.get("UW_reqd"))){
    					IsFTSDocProvided="N";
    					FTS_Ack_flg="NA";
    					//FTS_Ack_flg="D";changed as per discussion with simi and deepak sir by om 20/10/22
    				}
    				trTableColumn=extColNames+"FTS_Ack_flg, Decision_FTS,IsFTSDocProvided,Decision";
        			trTableValue=extColValues+"'"+FTS_Ack_flg+"','Approve','" +IsFTSDocProvided+"','Approve'"; // Decision_FTS added HRITIK 12/11/23
        			decisionInHistory = "No bank statement analysis statement Received";
        			remark="FTS Details Updated";
        		}

        		if ("BS_NO_ACTION".equalsIgnoreCase(sSubProcess))
        		{
    				trTableColumn=extColNames+"FTS_Ack_flg,Decision_FTS,IsFTSDocProvided,Decision";
        			trTableValue=extColValues+"'D','Approve','N','Approve'";
        			decisionInHistory = "Bank statement no action statement Received"; // Decision_FTS added HRITIK 12/11/23
        			remark="FTS Details Updated";
        		}

        		//vinayak prime 4 changes starts
        		if ("Update_Card_Serno".equalsIgnoreCase(sSubProcess))
        		{        			
        		    String Card_Serno_flag=hm.get("Card_Serno");
        		    
        			trTableColumn=extColNames;
        			trTableValue=extColValues;
        			decisionInHistory = "Card_Serno Received";
        			remark="Card_Serno Details Updated";
        			insertIntoExtTableDCC();
        		}
        		//vinayak prime 4 cahnges ends
        		if ("END_COOLING".equalsIgnoreCase(sSubProcess))
        		{
        			checkConditionMandatoryDCC();
        			String Decision = "";
    				if ("Y".equalsIgnoreCase(hm.get("Cancel_in_cooling_period")))
    					Decision="Decline";
    				else if ("N".equalsIgnoreCase(hm.get("Cancel_in_cooling_period")))
    					Decision="Approve";
        			trTableColumn=extColNames+"Decision";
        			trTableValue=extColValues+"'"+Decision+"'";
        			decisionInHistory = "Cooling period " + Decision;
        			remark="END_COOLING";
        		}
/*        		//Hritik - 26/06/2023 repeated code
        		if("UPDATE_CIF_CARD_DETAILS".equalsIgnoreCase(sSubProcess))
        		{
        			String Decision="Success";
        			trTableColumn=extColNames+"Decision";
        			trTableValue=extColValues+"'"+Decision+"'";
        			decisionInHistory = "CIF CARD DETAILS " + Decision;
        			remark="UPDATE CIF CARD DETAILS";
        		}*/
        		if ("CARD_DEL_DOC_REC".equalsIgnoreCase(sSubProcess) && ("Delivered".equalsIgnoreCase(hm.get("Delivery_Status")) || "Undelivered".equalsIgnoreCase(hm.get("Delivery_Status")) || "RTO".equalsIgnoreCase(hm.get("Delivery_Status"))))
        		{
        			updateRepetitiveDCCAttributesAndEIDData();
        			//Status_Code
        			String Decision = "";
        			/*if ("Delivered".equalsIgnoreCase(hm.get("Delivery_Status")))
    					Decision="Approve";
        			else if ("Undelivered".equalsIgnoreCase(hm.get("Delivery_Status")))
    					Decision="Reject";*/
        			//Changed by om on 25/10/22
        			String Wi_completeFlag = "N";
        			if ("DD".equalsIgnoreCase(hm.get("Status_Code"))){
        				Decision="Approve";
        				Wi_completeFlag = "Y";
        			}
					
    				else if ("RTO".equalsIgnoreCase(hm.get("Status_Code"))){
    					Decision="Reject";
    					Wi_completeFlag = "Y";
    				}
					
        			if("Y".equalsIgnoreCase(Wi_completeFlag)){
        				trTableColumn=extColNames+"Decision";
            			trTableValue=extColValues+"'"+Decision+"'";
            			
            			remark = "Card delivery details and docs received with " + Decision;//Changed by om on 25/10/22
            			
            			decisionInHistory=Decision + " and updated";//Changed by om on 25/10/22
            			
            			insertIntoExtTableDCC();
    					insertIntoHistoryDCC(remark, decisionInHistory);
    					updateInDataIn_NG_Digital_AWB_Status();
    					getWorkItemID();
    					doneworkitemDCC();
        			}
        			else{
        				WriteLog("Invalid request for CARD_DEL_DOC_REC || Status_Code: "+ hm.get("Status_Code") + "-- Delivery_Status: "+ hm.get("Delivery_Status"));
        			}
        			
        		}
				
				if("UPDATE_CIF_CARD_DETAILS".equalsIgnoreCase(sSubProcess)){
        			
        			String Decision="Success";
        			sInputXML=getAPSelectWithColumnNamesXML("SELECT Wi_name,OutputAlternateCard from NG_DCC_EXTTABLE with(nolock) WHERE wi_name='"+hm.get("WINUMBER")+"'");
        			WriteLog("APSelectWithColumnNames Input: "+sInputXML);
        			sOutputXML=executeAPI(sInputXML);
        			WriteLog("APSelectWithColumnNames Output: "+sOutputXML);
        	    	xmlobj=new XMLParser(sOutputXML);
        	    
        			checkCallsMainCode(xmlobj); 
        			String Wi_name=getTagValues(sOutputXML, "Wi_name");
        			String OutputAlternateCard=getTagValues(sOutputXML, "OutputAlternateCard");
        			WriteLog("Wi_name "+Wi_name);
        			WriteLog("card_change "+OutputAlternateCard);
        			if("Y".equalsIgnoreCase(OutputAlternateCard)){
        				checkConditionMandatoryForDCC_UPDATE_CIF_CARD_DETAILS(); // hritik 08112023 -  CM of tags for Alternate card.
        			}
        			
        			trTableColumn=extColNames+"Decision";
    				trTableValue=extColValues+"'"+Decision+"'";
    				decisionInHistory = "CIF CARD DETAILS " + Decision;
    				remark="UPDATE CIF CARD DETAILS";

        		}
        		
        		// CARD_DETAILS_NO_ACTN - 28.9.23 HRITIK
           		if("CARD_DETAILS_NO_ACTN".equalsIgnoreCase(sSubProcess)){
        			
        			String Decision="Reject";
        			trTableColumn=extColNames+"Decision";
        			trTableValue=extColValues+"'"+Decision+"'";
        			decisionInHistory = "Response "+ hm.get("Card_Change_No_Action") + " Workitem "+Decision;
        			remark="Response "+ hm.get("Card_Change_No_Action");
        		}
        		
        		//2803 End
        		//2803 --Kamran Updated Salary Bank for EXT Tabel update	
				if ("FIRCO_DOC_RECEIVED".equalsIgnoreCase(sSubProcess) || "FIRCO_NO_ACTION".equalsIgnoreCase(sSubProcess) || "STATEMENT_ANALYZED".equalsIgnoreCase(sSubProcess)
				|| "NO_BANK_STATEMENT_ANALYSIS".equalsIgnoreCase(sSubProcess) || "BS_NO_ACTION".equalsIgnoreCase(sSubProcess) || "END_COOLING".equalsIgnoreCase(sSubProcess)
				|| "SALARY_DOC_RECEIVED".equalsIgnoreCase(sSubProcess) || "SALARY_DOC_NO_ACTION".equalsIgnoreCase(sSubProcess) || "UPDATE_CIF_CARD_DETAILS".equalsIgnoreCase(sSubProcess)
				|| "CARD_DETAILS_NO_ACTN".equalsIgnoreCase(sSubProcess)) 
				{
					insertIntoExtTableDCC();
					insertIntoHistoryDCC(remark, decisionInHistory);
					getWorkItemID();
					doneworkitemDCC();
				}
				// Hritik - 09/07/23 - Refer Q Changes.
				if("STATEMENT_ANALYZED".equalsIgnoreCase(sSubProcess))
				{
					//Insert Net Salary in the table - NG_DCC_GR_NetSalaryDetails.
					Date d= new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sDate = dateFormat.format(d);
					
	    			WriteLog("Insert Net Salary in the table - NG_DCC_GR_NetSalaryDetails.");
	    			String net1 = ""; 
	    			String net2 = ""; 	
	    			String net3 = ""; 
	    			String WI="";
	    			
	    			net1 =  hm.get("Stmt_Salary_1");
	    			net2 =  hm.get("Stmt_salary_2");
	    			net3 =  hm.get("Stmt_salary_3");
	    			WI =  hm.get("WINUMBER");
	    			
	    			String NetSal_Col="Net_Salary1,Net_Salary2,Net_Salary3,Wi_Name,Workstep,insertion_date_time";
	    			String NetSal_val = "'"+net1+"','"+net2+"','"+net3+"','"+WI+"','Sys_FTS_WI_Update','"+sDate+"'";
	        		
	    			sInputXML=getInsert_NG_DCC_GR_NetSalaryDetails(NetSal_Col,NetSal_val);
					WriteLog("The input XML getInsert_NG_DCC_GR_NetSalaryDetails "+sInputXML);
					sOutputXML = executeAPI(sInputXML);
					WriteLog("APInsert Output for transaction update table getInsert_NG_DCC_GR_NetSalaryDetails: " + sOutputXML);
					xmlobj = new XMLParser(sOutputXML);
					checkCallsMainCode(xmlobj);
					WriteLog("Insert Net Salary in the table - NG_DCC_GR_NetSalaryDetails  END.");
				}
        		WriteLog("Digital CC : doneworkitem");
        	}
        		// returning success response
        		returnResponse();              		
          	}
			catch(WICreateException e)
			{
				WriteLog("WICreateException caught:Code- "+e.getErrorCode());
				WriteLog("WICreateException caught:Description- "+e.getErrorDesc());
				WriteLog("WICreateException: "+e.toString());
				setFailureParamResponse();
				response.setErrorCode(e.getErrorCode());
				response.setErrorDescription(e.getErrorDesc());
				return response;
			}
			catch(IOException e)
			{
				setFailureParamResponse();
				response.setErrorCode("1009");
        		response.setErrorDescription(pCodes.getString("1009")+": "+e.getMessage());

        		return response;
			}
			catch(Exception e)
			{
				setFailureParamResponse();
				response.setErrorCode("1010");
        		response.setErrorDescription(pCodes.getString("1010")+": "+e.getMessage());
        		return response;
			}
			finally
			{	
				WriteLog("Inside finally method to dispose off conenction objects and hash maps");
				try
				{
					// Commented below code for Invalid Session Issue.
					/*if(sessionFlag==true)
						deleteConnection();*/
				}
				catch (Exception e) 
				{
					WriteLog("Exception: "+e.getMessage());					
				}
				hm.clear();

			}
		return response;
		
	}
	
	private void checkConditionMandatoryForDCC_UPDATE_CIF_CARD_DETAILS()throws WICreateException,Exception{ // hritik 08112023 -  CM of tags for Alternate card.
		WriteLog("Inside Digital CC opening conditional mandatory for OutputAlternateCard - Wi Update - Subprocess UPDATE_CIF_CARD_DETAILS");
		
		if(!hm.containsKey("Product_Desc") || "".equals(hm.get("Product_Desc"))){
			throw new WICreateException("10016",pCodes.getString("10016")+" "+sProcessName);
		}
		if(!hm.containsKey("Product") || "".equals(hm.get("Product"))){
			throw new WICreateException("10017",pCodes.getString("10017")+" "+sProcessName);
		}
		if(!hm.containsKey("Updated_CardType_Desc") ||  "".equals(hm.get("Updated_CardType_Desc"))){
			throw new WICreateException("10018",pCodes.getString("10018")+" "+sProcessName);
		}
		if(!hm.containsKey("Updated_CardType_Code")  ||  "".equals(hm.get("Updated_CardType_Code"))){
			throw new WICreateException("10019",pCodes.getString("10019")+" "+sProcessName);
		}
	}
	
	private void returnResponse() throws WICreateException,Exception{
		wiName = WINumber;
		setSuccessParamResponse();
		response.setWorkitemNumber(wiName);
		
	}
	private void setSuccessParamResponse()
	{
		headerObjResponse.setExtra1(sExtra1);
		headerObjResponse.setExtra2(sExtra2);
		headerObjResponse.setMessageId(sMessageId);
		headerObjResponse.setMsgFormat(sMsgFormat);
		headerObjResponse.setMsgVersion(sMsgVersion);
		headerObjResponse.setRequestorChannelId(sRequestorChannelId);
		headerObjResponse.setRequestorLanguage(sRequestorLanguage);
		headerObjResponse.setRequestorSecurityInfo(sRequestorSecurityInfo);
		headerObjResponse.setRequestorUserId(sRequestorUserId);
		headerObjResponse.setReturnCode("0");
		headerObjResponse.setReturnDesc("Success");
		response.setEE_EAI_HEADER(headerObjResponse);
	}
	private void setFailureParamResponse()
	{
		headerObjResponse.setExtra1(sExtra1);
		headerObjResponse.setExtra2(sExtra2);
		headerObjResponse.setMessageId(sMessageId);
		headerObjResponse.setMsgFormat(sMsgFormat);
		headerObjResponse.setMsgVersion(sMsgVersion);
		headerObjResponse.setRequestorChannelId(sRequestorChannelId);
		headerObjResponse.setRequestorLanguage(sRequestorLanguage);
		headerObjResponse.setRequestorSecurityInfo(sRequestorSecurityInfo);
		headerObjResponse.setRequestorUserId(sRequestorUserId);
		headerObjResponse.setReturnCode("-1");
		headerObjResponse.setReturnDesc("Failure");
		response.setEE_EAI_HEADER(headerObjResponse);
	}
	private void updatefieldsFor_DAO_attributes() throws WICreateException,Exception
	{
		WriteLog("updatefieldsFor_DAO_attributes:");
		checkConditionMandatoryForDataUpdate_DAO();
		WriteLog("updatefieldsFor_DAO_attributes: After  checkConditionMandatoryForDataUpdate_DAO");
		updateInDataInExtTableDAO();
		WriteLog("updatefieldsFor_DAO_attributes: After  updateInDataInExtTableDAO");
	}
	
	private void updatefieldsFor_DAO_Dispatch_attributes() throws WICreateException,Exception
	{
		WriteLog("updatefieldsFor_DAO_Dispatch_attributes:");
		checkConditionMandatoryForDataUpdate_DAO_dsipatch();
		WriteLog("updatefieldsFor_DAO_Dispatch_attributes: After  checkConditionMandatoryForDataUpdate_DAO");
		updateInDataInExtTableDAO_dipatch();
		WriteLog("updatefieldsFor_DAO_Dispatch_attributes: After  updateInDataInExtTableDAO");
	}
	
	private void checkConditionMandatoryForDataUpdate_DAO_dsipatch()throws WICreateException,Exception
	{
		if(!hm.containsKey("WINUMBER") || "".equals(hm.get("WINUMBER"))){
			throw new WICreateException("8001",pCodes.getString("8001")+":"+sProcessName);
		}
		if(!hm.containsKey("Delivery_Status") || "".equals(hm.get("Delivery_Status"))){
			throw new WICreateException("8011",pCodes.getString("8011")+":"+sProcessName);
		}
		if(!hm.containsKey("Status_Code") || "".equals(hm.get("Status_Code"))){
			throw new WICreateException("8012",pCodes.getString("8012")+":"+sProcessName);
		}
	}
	
	private void updateInDataIn_NG_Digital_AWB_Status() throws WICreateException, Exception {
		WriteLog("updateInDataIn_NG_Digital_AWB_Status:");
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
		
		WriteLog("updateInDataIn_NG_Digital_AWB_Status: sDate "+sDate);
		
		String WINumber = hm.get("WINUMBER"); 
		WriteLog("updateInDataIn_NG_Digital_AWB_Status: WINUMBER "+WINumber);
		
		trTableColumn="";
		trTableValue="";
		
		WriteLog("updateInDataIn_NG_Digital_AWB_Status: Delivery_Status ");
		if(hm.containsKey("Delivery_Status"))
		{
			trTableColumn="delivery_status";
			trTableValue="'"+hm.get("Delivery_Status")+"'";
			WriteLog("updateInDataIn_NG_Digital_AWB_Status: Delivery_Status: "+hm.get("Prospect_id"));
			WriteLog("updateInDataIn_NG_Digital_AWB_Status: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("Status_Code"))
		{
			trTableColumn=trTableColumn+", Delivery_Status_Code";
			trTableValue=trTableValue+", '"+hm.get("Status_Code")+"'";
			WriteLog("updateInDataIn_NG_Digital_AWB_Status: Status_Code: "+hm.get("Status_Code"));
			WriteLog("updateInDataIn_NG_Digital_AWB_Status: trTableValue: "+trTableValue);
		}
		
		trTableColumn=trTableColumn+",delivery_date";
		trTableValue=trTableValue+",'"+sDate+"'";
		
		WriteLog("Final trTableColumn: updateInDataIn_NG_Digital_AWB_Status "+trTableColumn);
		WriteLog("Final trTableValue: updateInDataIn_NG_Digital_AWB_Status "+trTableValue);
		
		sInputXML=getDBInsert_DAO_Dispatch_NG_Digital_AWB_Status(); // added on 23.8.22 - to update the status in table after del done.
		WriteLog("The input XML getDBInsert_DAO_Dispatch_NG_Digital_AWB_Status "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table getDBInsert_DAO_Dispatch_NG_Digital_AWB_Status: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
	}
	
	private void updateInDataInExtTableDAO_dipatch() throws WICreateException, Exception {
			
		WriteLog("updateInDataInExtTableDAO_dipatch:");
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
		
		WriteLog("updateInDataInExtTableDAO_dipatch: sDate "+sDate);
		
		String WINumber = hm.get("WINUMBER"); 
		WriteLog("updateInDataInExtTableDAO_dipatch: WINUMBER "+WINumber);
		
		trTableColumn="";
		trTableValue="";
		String Delivery_Status_value="";//vinayak
		String Status_Code_value="";
		WriteLog("updateInDataInExtTableDAO_dipatch: Delivery_Status ");
		if(hm.containsKey("Delivery_Status"))
		{
			trTableColumn="Delivery_Status";
			trTableValue="'"+hm.get("Delivery_Status")+"'";
			Delivery_Status_value=hm.get("Delivery_Status");
			WriteLog("updateInDataInExtTableDAO: Delivery_Status: "+hm.get("Delivery_Status"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("Status_Code"))
		{
			trTableColumn=trTableColumn+", Status_Code";
			trTableValue=trTableValue+", '"+hm.get("Status_Code")+"'";
			Status_Code_value=hm.get("Status_Code");
			WriteLog("updateInDataInExtTableDAO: Status_Code: "+hm.get("Status_Code"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if("CHK".equalsIgnoreCase(Status_Code_value)){
		//vinayak 18-08-23 for saving courier picked update value
		trTableColumn=trTableColumn+", Courier_status";
		trTableValue=trTableValue+", 'Courier picked'";
		}
		
		trTableColumn=trTableColumn+",Decision";
		trTableValue=trTableValue+",'Approve'";
		
		WriteLog("Final trTableColumn: getDBInsert_DAO_External "+trTableColumn);
		WriteLog("Final trTableValue: getDBInsert_DAO_External "+trTableValue);
		
		sInputXML=getDBInsert_DAO_External_for_dispatchWIupd(); // 17.8.22 - Again changes to WI from AWBno
		WriteLog("The input XML getDBInsert_DAO_External_for_dispatchWIupd "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table getDBInsert_DAO_External_for_dispatchWIupd: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}

	private void updateRepetitive_DAO_attribute_dispatch() throws WICreateException,Exception
	{
		WriteLog("upadteRepetitive_DAO_attribute_dispatch:" + InputMessage);
		String AWB = getAWB_fromWIDAO();
		WriteLog("upadteRepetitive_DAO_attribute_dispatch : Wi_name " + AWB);
		if(InputMessage.contains("<Emirates_ID_PING_data>"))
		{
			String Emirates_ID_PING_data = getTagValues(InputMessage, "Emirates_ID_PING_data");
			String Emirates_ID_PING_data_arr [] =Emirates_ID_PING_data.split("`");
			String emirates_id="";
			String expiry_date="";
			String card_number="";
			String Date_of_birth="",gender="",Nationality="",name="";
			
			for (int Ping_arr_cout = 0;Ping_arr_cout<Emirates_ID_PING_data_arr.length;Ping_arr_cout++ )
			{
				String rep_attributeList[] = getTagValues(Emirates_ID_PING_data_arr[Ping_arr_cout], "Attribute").split("`");
				Attribute rep_attributeObj[]=new Attribute[rep_attributeList.length];
		    	WriteLog("Attribute Object Initialized");
		    	for(int i=0;i<rep_attributeList.length;i++)
		    	{
		    		rep_attributeObj[i]=new Attribute();
		    		String str_attrName=getTagValues(rep_attributeList[i],"Name");
		    		String str_attrValue=getTagValues(rep_attributeList[i],"Value");
					
		    		if("emirates_id".equalsIgnoreCase(str_attrName)){
		    			emirates_id=str_attrValue;
		    			if(emirates_id.equalsIgnoreCase("") || emirates_id==null){
		    				throw new WICreateException("8013",pCodes.getString("8013")+":"+sProcessName);
		    			}
		    		}
		    		else if("expiry_date".equalsIgnoreCase(str_attrName)){
		    			expiry_date=str_attrValue;
		    		}
		    		else if("card_number".equalsIgnoreCase(str_attrName)){
		    			card_number=str_attrValue;
		    		}
		    		else if("Date_of_birth".equalsIgnoreCase(str_attrName)){
		    			Date_of_birth=str_attrValue;
		    		}
		    		else if("gender".equalsIgnoreCase(str_attrName)){
		    			gender=str_attrValue;
		    		}
		    		else if("name".equalsIgnoreCase(str_attrName)){
		    			name=str_attrValue;
		    			if(name.equalsIgnoreCase("") || name==null){
		    				throw new WICreateException("8014",pCodes.getString("8014")+":"+sProcessName);
		    			}
		    		}
		    		else if("Nationality".equalsIgnoreCase(str_attrName)){
		    			//Nationality=str_attrValue.replace("'"," ");
						Nationality=str_attrValue;
		    		}
		    		else{
		    			WriteLog("new tag in upadteRepetitive_DAO_attribute_dispatch EID_PING_DATA:" + str_attrName + str_attrValue);
		    		}
		    	}
		    	trTableColumn="emirates_id,card_number,expiry_date,Date_of_birth,gender,name,Nationality,WI_Name";
				trTableValue="'"+emirates_id+"','"+card_number+"','"+expiry_date+"','"+Date_of_birth+"','"+gender+"','"+name+"','"+Nationality+"','"+hm.get("WINUMBER")+"'";
				
				WriteLog("EID_PING_DATA: trTableColumn: "+trTableColumn);
				WriteLog("EID_PING_DATA: trTableValue: "+trTableValue);
				// NG_DAO_UPDATEWI_PINGDATA_DISPATCH
				sInputXML=getDBInsert_DAO_PINGDATA_DISPATCH();
				WriteLog("The input XML getDBInsert_DAO_PINGDATA_DISPATCH "+sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output for transaction update table getDBInsert_DAO_PINGDATA_DISPATCH: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				
				checkCallsMainCode(xmlobj);
			}
		}
	}
	
	private void updateRepetitiveDCCAttributesAndEIDData() throws WICreateException,Exception
	{
		WriteLog("INSERT DETAILS FROM CARD DELIVERY:" + InputMessage);
		String AWB_Number = getAWBNoForDCC();
		WriteLog("AWB_Number DCC " + AWB_Number);
		if(InputMessage.contains("<Emirates_ID_PING_data>"))
		{
			String Emirates_ID_PING_data = getTagValues(InputMessage, "Emirates_ID_PING_data");
			String Emirates_ID_PING_data_arr [] =Emirates_ID_PING_data.split("`");
			String emirates_id="";
			String expiry_date="";
			String card_number="";
			String Date_of_birth="",gender="",Nationality="",name="";
			
			for (int Ping_arr_cout = 0;Ping_arr_cout<Emirates_ID_PING_data_arr.length;Ping_arr_cout++ )
			{
				String rep_attributeList[] = getTagValues(Emirates_ID_PING_data_arr[Ping_arr_cout], "Attribute").split("`");
				Attribute rep_attributeObj[]=new Attribute[rep_attributeList.length];
		    	WriteLog("Attribute Object Initialized");
		    	for(int i=0;i<rep_attributeList.length;i++)
		    	{
		    		rep_attributeObj[i]=new Attribute();
		    		String str_attrName=getTagValues(rep_attributeList[i],"Name");
		    		String str_attrValue=getTagValues(rep_attributeList[i],"Value");
					
		    		if("emirates_id".equalsIgnoreCase(str_attrName)){
		    			emirates_id=str_attrValue;
		    			if(emirates_id.equalsIgnoreCase("") || emirates_id==null)
		    			{
		    				throw new WICreateException("8013",pCodes.getString("8013")+":"+sProcessName);
		    			}
		    		}
		    		else if("expiry_date".equalsIgnoreCase(str_attrName)){
		    			expiry_date=str_attrValue;
		    		}
		    		else if("card_number".equalsIgnoreCase(str_attrName)){
		    			card_number=str_attrValue;
		    		}
		    		else if("Date_of_birth".equalsIgnoreCase(str_attrName)){
		    			Date_of_birth=str_attrValue;
		    		}
		    		else if("gender".equalsIgnoreCase(str_attrName)){
		    			gender=str_attrValue;
		    		}
		    		else if("name".equalsIgnoreCase(str_attrName)){
		    			name=str_attrValue;
		    			if(name.equalsIgnoreCase("") || name==null)
		    			{
		    				throw new WICreateException("8014",pCodes.getString("8014")+":"+sProcessName);
		    			}
		    		}
		    		else if("Nationality".equalsIgnoreCase(str_attrName)){
		    		//	Nationality=str_attrValue.replace("'", " ");
						Nationality=str_attrValue;
		    		}
		    		else{
		    			WriteLog("Attribute names :" + str_attrName);
		    			WriteLog("Attribute values :" + str_attrValue);
		    		}
		    	}
		    	trTableColumn="emirates_id,card_number,expiry_date,Date_of_birth,gender,name,Nationality,WI_Name";
				trTableValue="'"+emirates_id+"','"+card_number+"','"+expiry_date+"','"+Date_of_birth+"','"+gender+"','"+name+"','"+Nationality+"','"+hm.get("WINUMBER")+"'";
				
				WriteLog("EID_PING_DATA: trTableColumn: "+trTableColumn);
				WriteLog("EID_PING_DATA: trTableValue: "+trTableValue);
				
				sInputXML=getDBInsertInputXml("NG_DCC_PINGDATA");
				WriteLog("The input XML dcc Ping date is :  "+sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output dcc PINGDATA : " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				// Check Main Code
				checkCallsMainCode(xmlobj);
			}
		}
		
		WriteLog("upadte Repetitive DCC attributes : Documents");
		
		if(InputMessage.contains("<Document>"))
		{
			String Documents_Data = getTagValues(InputMessage, "Document");
			String Documents_Data_arr [] =Documents_Data.split("`");
			WriteLog("Document Object Initialized");
			for (int arr_cout = 0;arr_cout<Documents_Data_arr.length;arr_cout++ )
			{
				String rep_attributeList[] = getTagValues(Documents_Data_arr[arr_cout], "Attribute").split("`");
				Attribute rep_attributeObj[]=new Attribute[rep_attributeList.length];
		    	WriteLog("Attribute Object Initialized");
		    	
		    	for(int i=0;i<rep_attributeList.length;i++)
		    	{
		    		rep_attributeObj[i]=new Attribute();
		    		String str_attrName=getTagValues(rep_attributeList[i],"Name");
		    		String str_attrValue=getTagValues(rep_attributeList[i],"Value");
		    		
		    		if("DocumentName".equalsIgnoreCase(str_attrName)){
		    			docName=str_attrValue;
		    		}
		    		else if("Document_type".equalsIgnoreCase(str_attrName)){
		    			docType=str_attrValue;
		    		}
		    		else if("Document_url".equalsIgnoreCase(str_attrName)){
		    			base64=str_attrValue;
		    		}
		    	}
		    	// Insert the data into new table : NG_DAO_SINGLEPAGER_DOCS
		    	trTableColumn="DocumentName,Document_type,Document_url,WI_name,AWB_Number";
				trTableValue="'"+docName+"','"+docType+"','"+base64+"','"+hm.get("WINUMBER")+"','"+AWB_Number+"'";
				
				WriteLog("NG_DCC_SINGLEPAGER_DOCS: trTableColumn: "+trTableColumn);
				WriteLog("NG_DCC_SINGLEPAGER_DOCS: trTableValue: "+trTableValue);
				
				sInputXML=getDBInsertInputXml("NG_DCC_SINGLEPAGER_DOCS");
				WriteLog("The input XML NG_DCC_SINGLEPAGER_DOCS "+sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output for transaction update table NG_DCC_SINGLEPAGER_DOCS: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				
				checkCallsMainCode(xmlobj);
			}
		}
	}
	
	private void downloadDocument( String sDocumentName , String sDocumentType, String sDocBase64) throws WICreateException, Exception {
		
		WriteLog("inside downloadDocument");
		WriteLog("Doc Name: "+sDocumentName);
		WriteLog("Doc Type: "+sDocumentType);

		if(sDocumentType.equalsIgnoreCase("") && !sDocumentName.equalsIgnoreCase(""))
		{
			WriteLog("No Value for Document Type");
			throw new WICreateException("1012",pCodes.getString("1012")+": "+sDocumentName);
		}
		if(sDocBase64.equalsIgnoreCase("") && !sDocumentName.equalsIgnoreCase(""))
		{
			WriteLog("No Value for DocBase64");
			throw new WICreateException("1008",pCodes.getString("1008")+": "+sDocumentName);
		}
		if(!sDocumentType.equalsIgnoreCase("") && !sDocumentName.equalsIgnoreCase("") && !sDocBase64.equalsIgnoreCase(""))
		{
			// validateDocumentNameType(docName,docType);
			dumpFileOnServer(docName,docType,base64);
			addToSMS(docName,docType); 
			deleteDumpedFileOnServer();
		}
    }
	
	private void validateDocumentNameType(String docNameTable, String docTypeTable) throws WICreateException {
		
		//Fetch from USR_0_WSR_DOCTYPEDETAILS
		sInputXML=getAPSelectWithColumnNamesXML("select DOCUMENTNAME, DOCTYPE, ISMANDATORY  from USR_0_WSR_DOCTYPEDETAILS with(nolock) where PROCESSID='Digital_AO' and DOCUMENTNAME='"+docNameTable+"' and DOCTYPE='"+docTypeTable+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
		WriteLog("APSelectWithColumnNames Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("APSelectWithColumnNames Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    
		checkCallsMainCode(xmlobj); 
		docNameTable=getTagValues(sOutputXML, "DOCUMENTNAME");
		docTypeTable=getTagValues(sOutputXML, "DOCTYPE");
		//String isMand=getTagValues(sOutputXML, "ISMANDATORY");
		if(docNameTable.equalsIgnoreCase(""))
		{
			WriteLog("No Value mapped for DocName");
			throw new WICreateException("1018",pCodes.getString("1018")+": "+docNameTable+" and "+docTypeTable);
		}
	}
	
	private void dumpFileOnServer(String sDocumentName, String sDocumentType , String sDocBase64  ) throws WICreateException,IOException,Exception {
		
		try {
			
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
			String strDate = sdf.format(cal.getTime());
			strDate= strDate.replace("/","");
		    strDate=strDate.replace(":","");
		    strDate=strDate.replace(".","");
		    strDate=strDate.replace(" ","");
		    sDumpLocation=sTempLoc+"/BinaryDump_DAO/"+strDate+"/";
			File fDumpFolder=new File(sDumpLocation);
			if(!fDumpFolder.exists())
				fDumpFolder.mkdirs();
			sFilePath=new StringBuilder().append(sDumpLocation).append(sDocumentName).append(".").append(sDocumentType).toString();
			WriteLog("Dump File Path: "+sFilePath);
			byte[] base64Bytes =  sDocBase64.getBytes(Charset.forName("UTF-8"));
			WriteLog("Size of base64 encoded bytes: "+base64Bytes.length);
			byte binaryBytes[] = Base64.decodeBase64(base64Bytes);
			File fDumpFile=new File(sFilePath);
			FileOutputStream fileoutputstream = new FileOutputStream(fDumpFile);			
			fileoutputstream.write(binaryBytes);
			fileoutputstream.close();
			
			long lLngFileSize = 0L;
			lLngFileSize=fDumpFile.length();
			lstrDocFileSize = Long.toString(lLngFileSize);
			
			WriteLog("lstrDocFileSize: "+lstrDocFileSize);
		}
		catch (IOException e) {
			
			WriteLog("IOException: "+e);
			throw new WICreateException("1004",pCodes.getString("1004")+" : "+e.getMessage());
		}
		catch (Exception e) {
			
			WriteLog("Exception: "+e);
			throw new WICreateException("1004",pCodes.getString("1004")+" : "+e.getMessage());
		}
	}

	private void addToSMS(String strDocumentName,String strExtension) throws WICreateException,IOException, Exception {
		
		WriteLog("SMS Addition starts for: "+sFilePath);
		NewIsIndex = new JPISIsIndex();
		dataJPDB =  new JPDBRecoverDocData();
		try {
			CPISDocumentTxn.AddDocument_MT(null, sJtsIp, (short)iJtsPort,sCabinetName, (short)iVolId, sFilePath, dataJPDB, null, NewIsIndex);

			String isIndex=(new StringBuilder()).append(NewIsIndex.m_nDocIndex).append("#").append(NewIsIndex.m_sVolumeId).append("#").toString();
			WriteLog("Data returned from SMS: "+isIndex);
			
			WriteLog("workItemName: "+hm.get("WINUMBER")+" sISIndex: "+isIndex);
			String sMappedInputXml="";
			String sOutputXml="";
			
			String Wi_name=hm.get("WINUMBER");
			String getTableNameQry="select itemindex from NG_DAO_EXTTABLE where WI_name='"+Wi_name+"'";
			sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
			WriteLog("Input XML to Get itemindex Name: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML to Get itemindex Name: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			String itemindex = getTagValues(sOutputXML, "itemindex");
			WriteLog("itemindex for DAO doc upload to WI: "+itemindex);
			
			String DocumentType="";
			
			if(strExtension.equalsIgnoreCase("JPG") || strExtension.equalsIgnoreCase("TIF") || strExtension.equalsIgnoreCase("JPEG") || strExtension.equalsIgnoreCase("TIFF"))
			{
				DocumentType = "I";
			}
			else
			{
				DocumentType = "N";
			}
			
			sMappedInputXml = getNGOAddDocument(itemindex,strDocumentName,DocumentType,strExtension,isIndex,lstrDocFileSize,iVolId,sCabinetName,sSessionID);
			WriteLog("sMappedInputXml: getNGOAddDocument:  "+sMappedInputXml);
			sOutputXml=executeAPI(sMappedInputXml);
			WriteLog("sOutputXml: getNGOAddDocument : "+sOutputXml);
			
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			String status=getTagValues(sOutputXML, "Status");
			WriteLog("status for DAO doc upload to WI: "+status);			
		}
		catch (NumberFormatException e) {
			WriteLog("NumberFormatException addToSMS:"+e.getMessage());
			e.printStackTrace();
		}
		catch(JPISException e) {
			WriteLog("JPISException addToSMS: "+e);
			throw new WICreateException("1000",pCodes.getString("1000")+" : "+e.getMessage());
		}
		catch (Exception e) {
			WriteLog("Exception in addToSMS :"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void deleteDumpedFileOnServer() {
		
		WriteLog("inside deleteDumpedFileOnServer");
		try {
			
			File fDumpFolder=new File(sDumpLocation);
			FileUtils.deleteDirectory(fDumpFolder);
			WriteLog("Folder Deleted");
		}
		catch (Exception e) {
			WriteLog("Exception: "+e.getMessage());
		}
	}

	private void upadteRepetitive_DAO_attribute() throws WICreateException,Exception {
		
		WriteLog("upadteRepetive_DAO_attribute:" + InputMessage);
		if(InputMessage.contains("<Additional_Document_Req>")) {
			
			String Additional_Document_Req = getTagValues(InputMessage, "Additional_Document_Req");
			String Additional_Document_arr [] =Additional_Document_Req.split("`");
			String Document_name="";
			String Document_Status="";
			String Document_Remarks="";
			
			for (int Document_arr_cout = 0;Document_arr_cout<Additional_Document_arr.length;Document_arr_cout++) {
				String rep_attributeList[] = getTagValues(Additional_Document_arr[Document_arr_cout], "Attribute").split("`");
				Attribute rep_attributeObj[]=new Attribute[rep_attributeList.length];
		    	WriteLog("Attribute Object Initialized");
		    	for(int i=0;i<rep_attributeList.length;i++)
		    	{
		    		rep_attributeObj[i]=new Attribute();
		    		String str_attrName=getTagValues(rep_attributeList[i],"Name");
		    		String str_attrValue=getTagValues(rep_attributeList[i],"Value");
					
		    		if("Document_name".equalsIgnoreCase(str_attrName)){
		    			Document_name=str_attrValue;
		    		}
		    		else if("Status".equalsIgnoreCase(str_attrName)){
		    			Document_Status=str_attrValue;
		    		}
		    		else if("Remarks".equalsIgnoreCase(str_attrName)){
		    			Document_Remarks=str_attrValue;
		    		}
		    		else{
		    			WriteLog("new tag in upadteRepetitive_DAO_attribute Additional_Document_Req:" + str_attrName + str_attrValue);
		    		}
		    	}
		    	WriteLog("Additional_Document_Req: Document_name: "+Document_name+" Document_Status: "+ Document_Status+" Document_Remarks: "+Document_Remarks );
		    	trTableColumn="document_name,document_status,document_remarks";
				trTableValue="'"+Document_name+"','"+Document_Status+"','"+Document_Remarks+"'";
		
				WriteLog("Additional_Document_Req: trTableValue: "+trTableValue);
				
				sInputXML=getDBInsert_DAO_Additional_Doc(Document_name);
				WriteLog("The input XML getDBInsert_DAO_Additional_Doc "+sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output for transaction update table getDBInsert_DAO_Additional_Doc: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				// Check Main Code
				checkCallsMainCode(xmlobj);
			}
			
			// update the AddnalDocsReqd flag to false after doc updated.
			WriteLog("update the AddnalDocsReqd flag to false after doc updated.");
			
			String AddnalDocsReqd="False";
			trTableColumn="AddnalDocsReqd";
			trTableValue="'"+AddnalDocsReqd+"'";
			sInputXML=getDBUpdate_AddnalDocsReqd_flag();
			WriteLog("The input XML getDBUpdate_AddnalDocsReqd_flag "+sInputXML);
			sOutputXML = executeAPI(sInputXML);
			WriteLog("APInsert Output for transaction update table getDBUpdate_AddnalDocsReqd_flag: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj); 
		}
		
		// Digital AO - Code for rep tag 13.6.22
		if(InputMessage.contains("<Background_info_employer>")) {
				
				String Background_info_employer = getTagValues(InputMessage, "Background_info_employer");
				String Background_info_arr [] =Background_info_employer.split("`");
				String Employer_Name="";
				String other_employer="";
				String Position_held="";
				String country ="";
				String dateofemployemnt="";
				String not_in_employment="";
 				for (int Background_info_cout = 0;Background_info_cout<Background_info_arr.length;Background_info_cout++ )
				{
					String rep_attribute_List[] = getTagValues(Background_info_arr[Background_info_cout], "Attribute").split("`");
					Attribute rep_attribute_Obj[]=new Attribute[rep_attribute_List.length];
			    	WriteLog("Attribute Object Initialized");
			    	for(int i=0;i<rep_attribute_List.length;i++)
			    	{
			    		rep_attribute_Obj[i]=new Attribute();
			    		String str_attrName=getTagValues(rep_attribute_List[i],"Name");
			    		String str_attrValue=getTagValues(rep_attribute_List[i],"Value");
						
			    		if("Employer_Name".equalsIgnoreCase(str_attrName)){
			    			Employer_Name=str_attrValue;
			    		}
			    		else if("not_in_employment".equalsIgnoreCase(str_attrName)){
			    			not_in_employment=str_attrValue;
			    			if(not_in_employment.equalsIgnoreCase("") || not_in_employment==null)
			    			{
			    				throw new WICreateException("8010",pCodes.getString("8010")+":"+sProcessName);
			    			}
			    		}
			    		else if("other_employer".equalsIgnoreCase(str_attrName)){
			    			other_employer=str_attrValue;
			    		}
			    		else if("Position_held".equalsIgnoreCase(str_attrName)){
			    			Position_held=str_attrValue;
			    		}
			    		else if("country".equalsIgnoreCase(str_attrName)){
			    			country=str_attrValue;
			    		}
			    		else if("dateofemployemnt".equalsIgnoreCase(str_attrName)){
			    			dateofemployemnt=str_attrValue;
			    		}
			    		else
			    		{
			    			WriteLog("new tag in upadteRepetitive_DAO_attribute Bckground_info:" + str_attrName + str_attrValue);
			    		}					
				}
			    WriteLog("Bckground_info: Employer_Name: "+Employer_Name+" other_employer: "+ other_employer+" Position_held: "+Position_held+" country: "+country+"dateofemployemnt: "+dateofemployemnt+"not_in_employment"+not_in_employment );
			    
			    trTableColumn="Employer_Name,other_employer,Position_held,country,dateofemployemnt,not_in_emp,WI_name";
				trTableValue="'"+Employer_Name+"','"+other_employer+"','"+Position_held+"','"+country+"','"+dateofemployemnt+"','"+not_in_employment+"','"+hm.get("WINUMBER")+"'";
				
				WriteLog("trTableValue Bckground info: "+trTableValue);
				
				sInputXML=getDBInsert_BackgroundInfo();
				WriteLog("The input XML getDBInsert_BackgroundInfo "+sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output for transaction update table getDBInsert_BackgroundInfo: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				// Check Main Code
				checkCallsMainCode(xmlobj);
				
			}
		}
		
		WriteLog("upadteRepetive_DAO_attribute: After  updateInDataInExtTableDAO");
	}
	
	
	private void upadteRepetitiveAttributeDCC() throws WICreateException,Exception
	{
		String Documents_Provided = hm.get("Document_Name_List");
		WriteLog("DCC Documents_Provided: trTableValue: " + Documents_Provided);
		String documents_arr[] = Documents_Provided.split(",");
		for (int i = 0; i < documents_arr.length; i++) {
			String Document_name = documents_arr[i];

			WriteLog("Documents_Provided: Document_name: " + Document_name);
			trTableColumn = "document_status";
			trTableValue = "'Received'";

			WriteLog("Documents_Provided: trTableValue: " + trTableValue);

			sInputXML = getDBUpdate_DCC_Additional_Doc(Document_name);
			WriteLog("The input XML update DCC Firco Additional_Doc " + sInputXML);
			sOutputXML = executeAPI(sInputXML);
			WriteLog("APInsert Output for transaction update table update DCC Additional_Doc: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj);
		}
		
		
		
		/*WriteLog("upadteRepetitiveAttributeDCC:" + InputMessage);
		if(InputMessage.contains("<Documents_Provided>"))
		{
			
			String Documents_Provided = getTagValues(InputMessage, "Document_Name_List");
			String documents_arr [] =Documents_Provided.split(",");
			
			
			for (int Document_arr_cout = 0; Document_arr_cout < documents_arr.length; Document_arr_cout++)
			{
				String Document_name="";
				String Document_Status="";
				String Document_Remarks="";
				String rep_attributeList[] = getTagValues(documents_arr[Document_arr_cout], "Attribute").split("`");
				Attribute rep_attributeObj[]=new Attribute[rep_attributeList.length];
		    	WriteLog("Attribute Object Initialized");
		    	for(int i=0;i<rep_attributeList.length;i++)
		    	{
		    		rep_attributeObj[i]=new Attribute();
		    		String str_attrName=getTagValues(rep_attributeList[i],"Name");
		    		String str_attrValue=getTagValues(rep_attributeList[i],"Value");
					
		    		if("Document_name".equalsIgnoreCase(str_attrName)){
		    			Document_name=str_attrValue;
		    		}
		    		else if("Status".equalsIgnoreCase(str_attrName)){
		    			Document_Status=str_attrValue;
		    		}
		    		else if("Remarks".equalsIgnoreCase(str_attrName)){
		    			Document_Remarks=str_attrValue;
		    		}
		    		else{
		    			WriteLog("new tag in upadteRepetitiveAttributeDCC Documents_Provided:" + str_attrName + str_attrValue);
		    		}
		    	}
		    	WriteLog("Documents_Provided: Document_name: "+Document_name+" Document_Status: "+ Document_Status+" Document_Remarks: "+Document_Remarks );
		    	trTableColumn="document_name,document_status,document_remarks";
				trTableValue="'"+Document_name+"','"+Document_Status+"','"+Document_Remarks+"'";
		
				WriteLog("Documents_Provided: trTableValue: "+trTableValue);
				
				sInputXML=getDBUpdate_DCC_Additional_Doc(Document_name);
				WriteLog("The input XML update DCC Firco Additional_Doc "+sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output for transaction update table update DCC Additional_Doc: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				// Check Main Code
				checkCallsMainCode(xmlobj);
			}
			
			// update the AddnalDocsReqd flag to false after doc updated.
			WriteLog("update the ADDITIONAL_DOCUMENT_REQUIRED flag to false after doc updated.");
			
			String AddnalDocsReqd="False";
			trTableColumn=extColNames+"ADDITIONAL_DOCUMENT_REQUIRED, Decision";
			trTableValue=extColValues+"'"+AddnalDocsReqd+"','Approve'";
		}*/
		
		WriteLog("upadteRepetive_DAO_attribute: After  updateInDataInExtTableDAO");
	}
	
	private void insertIntoExtTableDCC() throws WICreateException {
		sInputXML=getDBUpdateDocsReqdFlagDCC();
		WriteLog("The input XML getDBUpdateDocsReqdFlagDCC "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table getDBUpdateDocsReqdFlagDCC: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
	}
	
	private void checkConditionMandatoryForDataUpdate_DAO_stp()throws WICreateException,Exception
	{
		WriteLog("Inside Digital Account opening conditional mandatory for PEP check - Wi Update - Subprocess DAO");
		
		if(!hm.containsKey("WINUMBER") || "".equals(hm.get("WINUMBER")))
		{
			throw new WICreateException("8001",pCodes.getString("8001")+":"+sProcessName);
		}
		if(!hm.containsKey("Prospect_id") || "".equals(hm.get("Prospect_id")))
		{
			throw new WICreateException("8002",pCodes.getString("8002")+":"+sProcessName);
		}
		if(!hm.containsKey("Event") ||  "".equals(hm.get("Event")))
		{
			throw new WICreateException("8003",pCodes.getString("8003")+":"+sProcessName);
		}
		if(!hm.containsKey("Additional_document_received")  ||  "".equals(hm.get("Additional_document_received")))
		{
			throw new WICreateException("8006",pCodes.getString("8006")+":"+sProcessName);
		}
		
		String is_prime_req=is_prime_req();
		WriteLog("is_prime_req "+is_prime_req);
		String is_cbs_req=is_cbs_req();
		WriteLog("is_cbs_req "+is_cbs_req);
		String is_ntb=is_ntb();
		WriteLog("is_ntb "+is_ntb);
		//Vinayak changes done to remove NTB from condition 
		if((is_prime_req.equalsIgnoreCase("Y")|| is_prime_req.equalsIgnoreCase("Yes")))
		{
			if(!hm.containsKey("ECRN") ||  "".equals(hm.get("ECRN")))
			{
				throw new WICreateException("8023",pCodes.getString("8023")+":"+sProcessName);
			}
		}
		if(is_cbs_req.equalsIgnoreCase("Y")|| is_cbs_req.equalsIgnoreCase("Yes"))
		{
			if(!hm.containsKey("ChequeBk_ref") ||  "".equals(hm.get("ChequeBk_ref")))
			{
				throw new WICreateException("8024",pCodes.getString("8024")+":"+sProcessName);
			}
		}
	}
	
	private void checkConditionMandatoryForDataUpdate_DAO()throws WICreateException,Exception
	{
		WriteLog("Inside Digital Account opening conditional mandatory for PEP check - Wi Update - Subprocess DAO");
		// always mandate for update wi - start
		if(!hm.containsKey("WINUMBER") || "".equals(hm.get("WINUMBER")))
		{
			throw new WICreateException("8001",pCodes.getString("8001")+":"+sProcessName);
		}
		if(!hm.containsKey("Prospect_id") || "".equals(hm.get("Prospect_id")))
		{
			throw new WICreateException("8002",pCodes.getString("8002")+":"+sProcessName);
		}
		if(!hm.containsKey("Event") ||  "".equals(hm.get("Event")))
		{
			throw new WICreateException("8003",pCodes.getString("8003")+":"+sProcessName);
		}
		
		if(!hm.containsKey("Additional_document_received")  ||  "".equals(hm.get("Additional_document_received")))
		{
			throw new WICreateException("8006",pCodes.getString("8006")+":"+sProcessName);
		}
		// always mandate for update wi - end
		
		// Added on 13.7.22 - hritik -  start
		/*	if( (!hm.containsKey("PEP") ||  "".equals(hm.get("PEP"))) && (hm.get("Event").equalsIgnoreCase("Additional details provided")) )
		{
			throw new WICreateException("8015",pCodes.getString("8015")+":"+sProcessName);
		}
		
		if("Y".equalsIgnoreCase(hm.get("PEP")) || "YES".equalsIgnoreCase(hm.get("PEP")) )
		{
			WriteLog("Inside Digital Account opening conditional mandatory for PEP check");
			
			if("".equals(hm.get("PEP_Category"))|| !hm.containsKey("PEP_Category"))
			{
				throw new WICreateException("8016",pCodes.getString("8016")+" for process id: "+processID);
			}
			if("Self".equalsIgnoreCase(hm.get("PEP_Category")))
			{
				if("".equals(hm.get("Previous_Designation")) || !hm.containsKey("Previous_Designation"))
				{
					throw new WICreateException("8017",pCodes.getString("8017")+" for process id: "+processID);
				}
			}
			else if("Association".equalsIgnoreCase(hm.get("PEP_Category")))
			{
				if("".equals(hm.get("Relation_Detail_w_PEP"))|| !hm.containsKey("Relation_Detail_w_PEP"))
				{
					throw new WICreateException("8018",pCodes.getString("8018")+" for process id: "+processID);
				}
				else if ("".equals(hm.get("Name_of_PEP"))|| !hm.containsKey("Name_of_PEP"))
				{
					throw new WICreateException("8019",pCodes.getString("8019")+" for process id: "+processID);
				}
				 else if("".equals(hm.get("country_pep_hold_status"))|| !hm.containsKey("country_pep_hold_status"))
				 {
					 throw new WICreateException("8020",pCodes.getString("8020")+" for process id: "+processID);
				 }
				 else if(("".equals(hm.get("Emirate_pep_hold_status")) || !hm.containsKey("Emirate_pep_hold_status")) && hm.get("country_pep_hold_status").equalsIgnoreCase("AE"))
				 {
					throw new WICreateException("8021",pCodes.getString("8021")+" for process id: "+processID);
				 }
			}
		} commented by hritik 22.7.22 - after discuss with team. */
		// Added on 13.7.22 - hritik - end
		
		if(hm.get("Event").equalsIgnoreCase("Account Created"))
		{
			if(!hm.containsKey("CIF") || hm.get("CIF").equalsIgnoreCase(""))
			{
				throw new WICreateException("8004",pCodes.getString("8004")+":"+sProcessName);
			}
			if(!hm.containsKey("Account_Number") || hm.get("Account_Number").equalsIgnoreCase(""))
			{
				throw new WICreateException("8005",pCodes.getString("8005")+":"+sProcessName);
			}
			if(!hm.containsKey("IBAN") || hm.get("IBAN").equalsIgnoreCase(""))
			{
				throw new WICreateException("8025",pCodes.getString("8025")+":"+sProcessName);
			}
			
			String is_prime_req=is_prime_req();
			WriteLog("is_prime_req "+is_prime_req);
			String is_cbs_req=is_cbs_req();
			WriteLog("is_cbs_req "+is_cbs_req);
			String is_ntb=is_ntb();
			WriteLog("is_ntb "+is_ntb);
					//Vinayak changes done to remove NTB from condition 
			if((is_prime_req.equalsIgnoreCase("Y")|| is_prime_req.equalsIgnoreCase("Yes")))
			{
				if(!hm.containsKey("ECRN") ||  "".equals(hm.get("ECRN")))
				{
					throw new WICreateException("8023",pCodes.getString("8023")+":"+sProcessName);
				}
			}
			if(is_cbs_req.equalsIgnoreCase("Y")|| is_cbs_req.equalsIgnoreCase("Yes"))
			{
				if(!hm.containsKey("ChequeBk_ref") ||  "".equals(hm.get("ChequeBk_ref")))
				{
					throw new WICreateException("8024",pCodes.getString("8024")+":"+sProcessName);
				}
			}
		}
		
		/* commented on 13.7.22 - hritik 
		if("Y".equals(hm.get("investment_portfolio_including_virtual_asset")) || "Yes".equals(hm.get("investment_portfolio_including_virtual_asset")))
		{
			if(!hm.containsKey("income_generated")  ||  "".equals(hm.get("income_generated")))
			{
				throw new WICreateException("8007",pCodes.getString("8007")+":"+sProcessName);
			}
		}
		
		if("Y".equals(hm.get("real_Est_owned")) || "Yes".equals(hm.get("real_Est_owned")) )
		{
			if(!hm.containsKey("rental_income")  ||  "".equals(hm.get("rental_income")))
			{
				throw new WICreateException("8008",pCodes.getString("8008")+":"+sProcessName);
			}
		}
		
		if("Y".equals(hm.get("other_Source_of_income"))|| "Yes".equals(hm.get("other_Source_of_income")))
		{
			if(!hm.containsKey("Net_monthly_income")  ||  "".equals(hm.get("Net_monthly_income")))
			{
				throw new WICreateException("8009",pCodes.getString("8009")+":"+sProcessName);
			}
		}
		
		if("Y".equals(hm.get("Inheritance"))|| "Yes".equals(hm.get("Inheritance")))
		{
			if(!hm.containsKey("Inheritance_income")  ||  "".equals(hm.get("Inheritance_income")))
			{
				throw new WICreateException("8022",pCodes.getString("8022")+":"+sProcessName);
			}
		} */
	}
	
	private void checkConditionMandatoryDCC() throws WICreateException,Exception
	{
		WriteLog("Check Conditional mandatory fields");
		if ("END_COOLING".equalsIgnoreCase(sSubProcess))
		{
			if("Y".equals(hm.get("Cooling_period_expiry")) && (hm.get("Cooling_period_expiry_date") == null || "".equals(hm.get("Cooling_period_expiry_date"))))
			{
				throw new WICreateException("11001",pCodes.getString("11001")+":"+sProcessName);
			}
		}
	}
	
	
	
	private void insert_to_ng_dao_wi_update() throws WICreateException, Exception
	{
			WriteLog("_insert_to_ng_dao_wi_update DAO:");
			Date d= new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sDate = dateFormat.format(d);
			
			WriteLog("_insert_to_ng_dao_wi_update: sDate "+sDate);
			
			String WINumber = hm.get("WINUMBER");
			WriteLog("insert_to_ng_dao_wi_update: WINUMBER "+WINumber);
			
			trTableColumn="";
			trTableValue="";
			WriteLog("insert_to_ng_dao_wi_update: ");
			
			if(hm.containsKey("ECRN") || hm.containsKey("ChequeBk_ref")){
				
				WriteLog("Inside contains key ECRN/ChequeBk_ref: ");
				
				if(hm.containsKey("WINUMBER"))
				{
					trTableColumn=trTableColumn+"WI_name";
					trTableValue=trTableValue+"'"+hm.get("WINUMBER")+"'";
					WriteLog("insert_to_ng_dao_wi_update: WINUMBER: "+hm.get("WINUMBER"));
					WriteLog("insert_to_ng_dao_wi_updateinsert_to_ng_dao_wi_update: WINUMBER : trTableValue: "+trTableValue);
				}
				
				if(hm.containsKey("ECRN"))
				{
					trTableColumn=trTableColumn+",ECRN";
					trTableValue=trTableValue+",'"+hm.get("ECRN")+"'";
					WriteLog("insert_to_ng_dao_wi_update: ECRN: "+hm.get("ECRN"));
					WriteLog("insert_to_ng_dao_wi_update: ECRN : trTableValue: "+trTableValue);
				}
				WriteLog("insert_to_ng_dao_wi_update: ChequeBk_ref ");
				if(hm.containsKey("ChequeBk_ref"))
				{
					trTableColumn=trTableColumn+",ChequeBk_ref";
					trTableValue=trTableValue+",'"+hm.get("ChequeBk_ref")+"'";
					WriteLog("insert_to_ng_dao_wi_update: ChequeBk_ref: "+hm.get("ChequeBk_ref"));
					WriteLog("insert_to_ng_dao_wi_update: ChequeBk_ref : trTableValue: "+trTableValue);
				}
			
			
				trTableColumn = trTableColumn+",Status";
				trTableValue =trTableValue+",'R'";
				
				WriteLog("Final trTableColumn: ng_dao_wi_update "+trTableColumn);
				WriteLog("Final trTableValue: ng_dao_wi_update "+trTableValue);
				
				sInputXML=ng_dao_wi_update();
				WriteLog("The input XML ng_dao_wi_update "+sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output for transaction update table ng_dao_wi_update: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				// Check Main Code
				checkCallsMainCode(xmlobj);
			}
			else
			{
				WriteLog("Else condition of contains key ECRN/ChequeBk_ref: ");
			}
	}
	
	private void updateInDataInExtTableDAO() throws WICreateException, Exception {
		
		WriteLog("updateInDataInExtTableDAO:");
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
		
		WriteLog("updateInDataInExtTableDAO: sDate "+sDate);
		
		String WINumber = hm.get("WINUMBER");
		WriteLog("updateInDataInExtTableDAO: WINUMBER "+WINumber);
		
		trTableColumn="";
		trTableValue="";
		
		WriteLog("updateInDataInExtTableDAO: Prospect_id ");
		if(hm.containsKey("Prospect_id"))
		{
			trTableColumn="prospect_id";
			trTableValue="'"+hm.get("Prospect_id")+"',";
			WriteLog("updateInDataInExtTableDAO: Prospect_id: "+hm.get("Prospect_id"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("Sol_Id"))
		{
			trTableColumn=trTableColumn+",Sol_id";
			trTableValue=trTableValue+",'"+hm.get("Sol_Id")+"'";
			WriteLog("updateInDataInExtTableDAO: Sol_id: "+hm.get("Sol_id"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		// Added on 13.7.22 - hritik -  start
		if(hm.containsKey("PEP"))
		{
			trTableColumn=trTableColumn+",PEP";
			trTableValue=trTableValue+",'"+hm.get("PEP")+"'";
			WriteLog("updateInDataInExtTableDAO: PEP: "+hm.get("PEP"));
			WriteLog("updateInDataInExtTableDAO: trTableValue PEP: "+trTableValue);
		}
		
		if(hm.containsKey("PEP_Category"))
		{
			trTableColumn=trTableColumn+",PEP_Category";
			trTableValue=trTableValue+",'"+hm.get("PEP_Category")+"'";
			WriteLog("updateInDataInExtTableDAO: PEP_Category: "+hm.get("PEP_Category"));
			WriteLog("updateInDataInExtTableDAO: trTableValue PEP_Category: "+trTableValue);
		}
		// Added on 13.7.22 - hritik -  end
		if(hm.containsKey("Event"))
		{
			trTableColumn=trTableColumn+",Event";
			trTableValue=trTableValue+",'"+hm.get("Event")+"'";
			WriteLog("updateInDataInExtTableDAO: Event: "+hm.get("Event"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("Previous_Designation"))
		{
			trTableColumn=trTableColumn+",Previous_Designation";
			trTableValue=trTableValue+",'"+hm.get("Previous_Designation")+"'";
			WriteLog("updateInDataInExtTableDAO: Previous_Designation: "+hm.get("Previous_Designation"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("Relation_Detail_w_PEP"))
		{
			trTableColumn=trTableColumn+",Relation_Detail_w_PEP";
			trTableValue=trTableValue+",'"+hm.get("Relation_Detail_w_PEP")+"'";
			WriteLog("updateInDataInExtTableDAO: Relation_Detail_w_PEP: "+hm.get("Relation_Detail_w_PEP"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("Name_of_PEP"))
		{
			trTableColumn=trTableColumn+",name_of_pep";
			trTableValue=trTableValue+",'"+hm.get("Name_of_PEP")+"'";
			WriteLog("updateInDataInExtTableDAO: Name_of_PEP: "+hm.get("Name_of_PEP"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("country_pep_hold_status"))
		{
			trTableColumn=trTableColumn+",country_pep_hold_status";
			trTableValue=trTableValue+",'"+hm.get("country_pep_hold_status")+"'";
			WriteLog("updateInDataInExtTableDAO: country_pep_hold_status: "+hm.get("country_pep_hold_status"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("Emirate_pep_hold_status"))
		{
			trTableColumn=trTableColumn+",Emirate_pep_hold_status";
			trTableValue=trTableValue+",'"+hm.get("Emirate_pep_hold_status")+"'";
			WriteLog("updateInDataInExtTableDAO: Emirate_pep_hold_status: "+hm.get("Emirate_pep_hold_status"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("CIF"))
		{
			trTableColumn=trTableColumn+",CIF";
			trTableValue=trTableValue+",'"+hm.get("CIF")+"'";
			WriteLog("updateInDataInExtTableDAO: CIF: "+hm.get("Emirate_pep_hold_status"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("Account_Number"))
		{
			trTableColumn=trTableColumn+",account_no";
			trTableValue=trTableValue+",'"+hm.get("Account_Number")+"'";
			WriteLog("updateInDataInExtTableDAO: Account_Number: "+hm.get("Account_Number"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("investment_portfolio_including_virtual_asset"))
		{
			trTableColumn=trTableColumn+",investment_portfolio_including_virtual_asset";
			trTableValue=trTableValue+",'"+hm.get("investment_portfolio_including_virtual_asset")+"'";
			WriteLog("updateInDataInExtTableDAO: investment_portfolio_including_virtual_asset: "+hm.get("investment_portfolio_including_virtual_asset"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("income_generated"))
		{
			trTableColumn=trTableColumn+",income_generated";
			trTableValue=trTableValue+",'"+hm.get("income_generated")+"'";
			WriteLog("updateInDataInExtTableDAO: income_generated: "+hm.get("income_generated"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("real_Est_owned"))
		{
			trTableColumn=trTableColumn+",real_Est_owned";
			trTableValue=trTableValue+",'"+hm.get("real_Est_owned")+"'";
			WriteLog("updateInDataInExtTableDAO: real_Est_owned: "+hm.get("real_Est_owned"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("rental_income"))
		{
			trTableColumn=trTableColumn+",rental_income";
			trTableValue=trTableValue+",'"+hm.get("rental_income")+"'";
			WriteLog("updateInDataInExtTableDAO: rental_income: "+hm.get("rental_income"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("title_deed_attached"))
		{
			trTableColumn=trTableColumn+",title_deed_attached";
			trTableValue=trTableValue+",'"+hm.get("title_deed_attached")+"'";
			WriteLog("updateInDataInExtTableDAO: title_deed_attached: "+hm.get("title_deed_attached"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("other_Source_of_income"))
		{
			trTableColumn=trTableColumn+",other_Source_of_income";
			trTableValue=trTableValue+",'"+hm.get("other_Source_of_income")+"'";
			WriteLog("updateInDataInExtTableDAO: title_deed_attached: "+hm.get("other_Source_of_income"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("Desc_spec_prod_serv_your_cmpny_deals"))
		{
			trTableColumn=trTableColumn+",Description_of_product_and_services_company_deals_";
			trTableValue=trTableValue+",'"+hm.get("Desc_spec_prod_serv_your_cmpny_deals")+"'";
			WriteLog("updateInDataInExtTableDAO: Desc_spec_prod_serv_your_cmpny_deals: "+hm.get("Desc_spec_prod_serv_your_cmpny_deals"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("virtual_Card_issued"))
		{
			trTableColumn=trTableColumn+",virtual_Card_issued";
			trTableValue=trTableValue+",'"+hm.get("virtual_Card_issued")+"'";
			WriteLog("updateInDataInExtTableDAO: virtual_Card_issued: "+hm.get("virtual_Card_issued"));
			WriteLog("updateInDataInExtTableDAO: trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("Net_monthly_income"))
		{
			trTableColumn=trTableColumn+",Net_monthly_income";
			trTableValue=trTableValue+",'"+hm.get("Net_monthly_income")+"'";
			WriteLog("updateInDataInExtTableDAO: Net_monthly_income: "+hm.get("Net_monthly_income"));
			WriteLog("updateInDataInExtTableDAO: Net_monthly_income : trTableValue: "+trTableValue);
		}
		
		// Added on 13.7.22 - hritik -  start
		if(hm.containsKey("Inheritance"))
		{
			trTableColumn=trTableColumn+",Inheritance";
			trTableValue=trTableValue+",'"+hm.get("Inheritance")+"'";
			WriteLog("updateInDataInExtTableDAO: Inheritance: "+hm.get("Inheritance"));
			WriteLog("updateInDataInExtTableDAO: Inheritance : trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("Inheritance_income"))
		{
			trTableColumn=trTableColumn+",Inheritance_income";
			trTableValue=trTableValue+",'"+hm.get("Inheritance_income")+"'";
			WriteLog("updateInDataInExtTableDAO: Inheritance_income: "+hm.get("Inheritance_income"));
			WriteLog("updateInDataInExtTableDAO: Inheritance_income : trTableValue: "+trTableValue);
		}
		// Added on 13.7.22 - hritik -  End
		
		if(hm.containsKey("Additional_document_received"))
		{
			trTableColumn=trTableColumn+",Additional_document_received";
			trTableValue=trTableValue+",'"+hm.get("Additional_document_received")+"'";
			WriteLog("updateInDataInExtTableDAO: Additional_document_received: "+hm.get("Additional_document_received"));
			WriteLog("updateInDataInExtTableDAO: Additional_document_received : trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("IBAN"))
		{
			trTableColumn=trTableColumn+",IBAN";
			trTableValue=trTableValue+",'"+hm.get("IBAN")+"'";
			WriteLog("updateInDataInExtTableDAO: Additional_document_received: "+hm.get("IBAN"));
			WriteLog("updateInDataInExtTableDAO: Additional_document_received : trTableValue: "+trTableValue);
		}
		//vinayak added jira 4087
		
		if(hm.containsKey("Payroll_account"))
		{
			trTableColumn=trTableColumn+",Payroll_account";
			trTableValue=trTableValue+",'"+hm.get("Payroll_account")+"'";
			WriteLog("updateInDataInExtTableDAO: Additional_document_received: "+hm.get("Payroll_account"));
			WriteLog("updateInDataInExtTableDAO: Additional_document_received : trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("Scheme_Code"))
		{
			trTableColumn=trTableColumn+",Scheme_Code";
			trTableValue=trTableValue+",'"+hm.get("Scheme_Code")+"'";
			WriteLog("updateInDataInExtTableDAO: Additional_document_received: "+hm.get("Scheme_Code"));
			WriteLog("updateInDataInExtTableDAO: Additional_document_received : trTableValue: "+trTableValue);
		}
		
		if(hm.containsKey("customer_subsegment"))
		{
			trTableColumn=trTableColumn+",Customer_Subsegment";
			trTableValue=trTableValue+",'"+hm.get("customer_subsegment")+"'";
			WriteLog("updateInDataInExtTableDAO: Additional_document_received: "+hm.get("customer_subsegment"));
			WriteLog("updateInDataInExtTableDAO: Additional_document_received : trTableValue: "+trTableValue);
		}
		//vinayak chnages end
		
		
		/*
		if(hm.containsKey("ECRN"))
		{
			trTableColumn=trTableColumn+",ECRN";
			trTableValue=trTableValue+",'"+hm.get("ECRN")+"'";
			WriteLog("updateInDataInExtTableDAO: ECRN: "+hm.get("ECRN"));
			WriteLog("updateInDataInExtTableDAO: ECRN : trTableValue: "+trTableValue);
		}
		if(hm.containsKey("ChequeBk_ref"))
		{
			trTableColumn=trTableColumn+",ChequeBk_ref";
			trTableValue=trTableValue+",'"+hm.get("ChequeBk_ref")+"'";
			WriteLog("updateInDataInExtTableDAO: ChequeBk_ref: "+hm.get("ChequeBk_ref"));
			WriteLog("updateInDataInExtTableDAO: ChequeBk_ref : trTableValue: "+trTableValue);
		} */
		
		// Hritik -- 02.08.23 
		if(hm.containsKey("Account_creation_date"))
		{
			trTableColumn=trTableColumn+",Account_creation_date";
			trTableValue=trTableValue+",'"+hm.get("Account_creation_date")+"'";
			WriteLog("updateInDataInExtTableDAO: Account_creation_date: "+hm.get("Account_creation_date"));
			WriteLog("updateInDataInExtTableDAO: Account_creation_date : trTableValue: "+trTableValue);
		}
		
		WriteLog("Final trTableColumn: getDBInsert_DAO_External "+trTableColumn);
		WriteLog("Final trTableValue: getDBInsert_DAO_External "+trTableValue);
		
		sInputXML=getDBInsert_DAO_External();
		WriteLog("The input XML getDBInsert_DAO_External "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table getDBInsert_DAO_External: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);

	}
	//Kamran to update fields from Intellect to BSR 
	private void updateFieldsForIntellectToBSR()throws WICreateException,Exception{
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
		
		//checkConditionMandatoryForDataUpdate();
		trTableColumn="";
		trTableValue="";
		WINumber=hm.get("WI_NAME");
		WriteLog("updateFieldsForIntellectToBSR: WINUMBER "+WINumber);
		
		if(hm.containsKey("WI_NAME"))
		{
			trTableColumn=trTableColumn+"WI_NAME";
			trTableValue=trTableValue+"'"+hm.get("WI_NAME")+"',";
			WriteLog("updateFieldsForIntellectToBSR: WI_NAME: "+hm.get("WI_NAME"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("ENTRY_DATE_TIME"))
		{
			trTableColumn=trTableColumn+",ENTRY_DATE_TIME";
			trTableValue=trTableValue+"'"+hm.get("ENTRY_DATE_TIME")+"',";
			WriteLog("updateFieldsForIntellectToBSR: ENTRY_DATE_TIME: "+hm.get("ENTRY_DATE_TIME"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("WS_NAME"))
		{
			trTableColumn=trTableColumn+",WS_NAME";
			trTableValue=trTableValue+"'"+hm.get("WS_NAME")+"',";
			WriteLog("updateFieldsForIntellectToBSR: WS_NAME: "+hm.get("WS_NAME"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("USER_NAME"))
		{
			trTableColumn=trTableColumn+",USER_NAME";
			trTableValue=trTableValue+"'"+hm.get("USER_NAME")+"',";
			WriteLog("updateFieldsForIntellectToBSR: USER_NAME: "+hm.get("USER_NAME"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("DECISION"))
		{
			trTableColumn=trTableColumn+",DECISION";
			trTableValue=trTableValue+"'"+hm.get("DECISION")+"',";
			WriteLog("updateFieldsForIntellectToBSR: DECISION: "+hm.get("DECISION"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("INITIATOR_TEAM"))
		{
			trTableColumn=trTableColumn+",INITIATOR_TEAM";
			trTableValue=trTableValue+"'"+hm.get("INITIATOR_TEAM")+"',";
			WriteLog("updateFieldsForIntellectToBSR: INITIATOR_TEAM: "+hm.get("INITIATOR_TEAM"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("TEAM"))
		{
			trTableColumn=trTableColumn+",TEAM";
			trTableValue=trTableValue+"'"+hm.get("TEAM")+"',";
			WriteLog("updateFieldsForIntellectToBSR: TEAM: "+hm.get("TEAM"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("REMARKS"))
		{
			trTableColumn=trTableColumn+",REMARKS";
			trTableValue=trTableValue+"'"+hm.get("REMARKS")+"',";
			WriteLog("updateFieldsForIntellectToBSR: REMARKS: "+hm.get("REMARKS"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("REJECT_REASONS"))
		{
			trTableColumn=trTableColumn+",REJECT_REASONS";
			trTableValue=trTableValue+"'"+hm.get("REJECT_REASONS")+"',";
			WriteLog("updateFieldsForIntellectToBSR: REJECT_REASONS: "+hm.get("REJECT_REASONS"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("HOLD_TILL_DATE"))
		{
			trTableColumn=trTableColumn+",HOLD_TILL_DATE";
			trTableValue=trTableValue+"'"+hm.get("HOLD_TILL_DATE")+"',";
			WriteLog("updateFieldsForIntellectToBSR: HOLD_TILL_DATE: "+hm.get("HOLD_TILL_DATE"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		if(hm.containsKey("HOLD_REASON"))
		{
			trTableColumn=trTableColumn+",HOLD_REASON";
			trTableValue=trTableValue+"'"+hm.get("HOLD_REASON")+"',";
			WriteLog("updateFieldsForIntellectToBSR: HOLD_REASON: "+hm.get("HOLD_REASON"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		trTableColumn=trTableColumn+",ACTION_DATE_TIME";
		trTableValue=trTableValue+"'"+sDate+"'";
		//WriteLog("updateFieldsForIntellectToBSR: ACTION_DATE_TIME: "+sDate);
		WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		WriteLog("Final trTableColumn: "+trTableColumn);
		WriteLog("Final trTableValue: "+trTableValue);
		sInputXML=getDBInsertInputHistoryBSR();
		WriteLog("The input XML getDBInsertInputHistoryBSR "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table getDBInsertInputHistoryBSR: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		
		//Updating External Table
		trTableColumn="";
		trTableValue="";
		WINumber=hm.get("WI_NAME");
		if(hm.containsKey("DECISION"))
		{
			trTableColumn=trTableColumn+"DECISION";
			trTableValue=trTableValue+"'"+hm.get("DECISION")+"'";
			WriteLog("updateFieldsForIntellectToBSR: DECISION: "+hm.get("DECISION"));
			WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		}
		trTableColumn=trTableColumn+",CURRENT_WS";
		trTableValue=trTableValue+",'"+hm.get("WS_NAME")+"',";
		WriteLog("updateFieldsForIntellectToBSR: CURRENT_WS: "+hm.get("WS_NAME"));
		WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		
		trTableColumn=trTableColumn+",PREV_WS";
		trTableValue=trTableValue+"'INTELLECT_HOLD'";
	//	System.out.println("eee"+trTableColumn);
	//	System.out.println("eee3333"+trTableValue);
		WriteLog("updateFieldsForIntellectToBSR: PREV_WS: INTELLECT_HOLD");
		WriteLog("updateFieldsForIntellectToBSR: trTableValue: "+trTableValue);
		
		
		WriteLog("Uppdating external table for BSR for the case "+WINumber);
		WriteLog("updateFieldsForIntellectToBSR: WINUMBER "+WINumber);
		sInputXML=getDBInsert_BSR_External();
		WriteLog("The input XML getDBInsert_BSR_External "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table getDBInsert_BSR_External: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		doneworkitemForBSR();
		
		//
	}
	//Method added by Sajan to update fields when the WI is at Customer Hold
	private void updateFieldsForFalconCustomerHold()throws WICreateException,Exception{
		checkConditionMandatoryForDataUpdate();
		trTableColumn="";
		trTableValue="";
		WINumber=hm.get("WINumber");
		if(hm.containsKey("BankName")){
			trTableColumn="Bank_Name";
			trTableValue="'"+getBankDescFromCode().replace(",", " ")+"'";
		}
		if(hm.containsKey("Iban_number")){
			if("".equals(trTableColumn)){
				trTableColumn="iban_Number";
				trTableValue="'"+hm.get("Iban_number")+"'";
			}
			else{
				trTableColumn=trTableColumn+",iban_Number";
				trTableValue=trTableValue+",'"+hm.get("Iban_number")+"'";
			}
		}
		if(hm.containsKey("HR_email_ID")){
			sInputXML=updateCustomTable("NG_RLOS_OffVerification","hremailid","'"+hm.get("HR_email_ID")+"'","wi_name = '"+WINumber+"'");
			WriteLog("The input XML for Bank Name update is "+sInputXML);
			sOutputXML = executeAPI(sInputXML);
			WriteLog("APInsert Output for BankName table: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
		}
		//trTableValue="'"+hm.get("BankName")+"'";
		WriteLog("The columns for Bank Name update is "+trTableColumn);
		//WINumber=hm.get("WINumber");
		sInputXML=updateBankNameInGrid();
		WriteLog("The input XML for Bank Name update is "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for BankName table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		trTableColumn="IF_DOCS_CUST_HOLD,CUST_HOLD_UPDATE";
		trTableValue="'"+hm.get("IsDocsPresent")+"','Y'";
		WriteLog("The columns for Biometric update is "+trTableColumn);
		sInputXML=getDBInsertExternal();
		WriteLog("The input XML for biometric update is "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		String wi_Id =getWorkitemID();
		sInputXML=getWorkItemInput_withWorkitemID(wi_Id);
		WriteLog("Input XML for get work item is "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML for get Work item is "+sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String strInputXML=completeWorkItemInput_withWorkitemID(wi_Id);
		WriteLog("Input XML for complete WI is "+strInputXML);
		String strOutputXMLCompleteWI=executeAPI(strInputXML);
		WriteLog("Output XML for complete WI is "+strOutputXMLCompleteWI);
		xmlobj = new XMLParser(strOutputXMLCompleteWI);
		checkCallsMainCode(xmlobj);
		
	}
	
	//Method added by Sajan to check conditional mandatory attributes for FALCON Data update
	private void checkConditionMandatoryForDataUpdate()throws WICreateException,Exception{
		if(!hm.containsKey("PortalReferenceNumber")){
			throw new WICreateException("6012",pCodes.getString("6012")+":"+sProcessName);
		}
	}
	
	//Method added By Sajan  to update Biometric status
	private void updateBiometricStatus() throws Exception{
		trTableColumn="BiometricStatus,BiometricAttempts";
		trTableValue="'"+hm.get("BiometricStatus")+"','"+hm.get("NoOfAttempts")+"'";
		if(hm.containsKey("Remarks")){
			trTableColumn=trTableColumn+",Biometric_Remarks";
			trTableValue=trTableValue+",'"+hm.get("Remarks")+"'";
		}
		WriteLog("The columns for Biometric update is "+trTableColumn);
		sInputXML=getDBInsertExternal();
		WriteLog("The input XML for biometric update is "+sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		
		sInputXML=getWorkItemInput();
		WriteLog("Input XML for get work item is "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML for get Work item is "+sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String strInputXML=completeWorkItemInput();
		WriteLog("Input XML for complete WI is "+strInputXML);
		String strOutputXMLCompleteWI=executeAPI(strInputXML);
		WriteLog("Output XML for complete WI is "+strOutputXMLCompleteWI);
		xmlobj = new XMLParser(strOutputXMLCompleteWI);
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoTransationTableforUpdate(String attributeList[]) throws WICreateException, Exception {

		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
		
		String attrName="";
	    String attrValue="";
	    String AttrNames = "";
	    String AttrValues ="";
		for(int i=0;i<attributeList.length;i++)
    	{
    		attrName=getTagValues(attributeList[i],"Name");
    		attrValue=getTagValues(attributeList[i],"Value");
    		if (!attrName.equalsIgnoreCase("WINUMBER"))
    		{
    			AttrNames=AttrNames+attrName+"~|~";
    			AttrValues=AttrValues+attrValue+"~|~";
    		}
    	}
		AttrNames=AttrNames.replace("'", " ");
		AttrValues=AttrValues.replace("'", "");
		trTableColumn="WINAME,ActivityName,ColumnNames,ColumnValues,InsertedDateTime";
		trTableValue="'"+WINumber+"','"+ActivityName+"','"+AttrNames+"','"+AttrValues+"','"+sDate+"'";
		WriteLog("trTableColumn for transaction table update" + trTableColumn);
		WriteLog("trTableValue for transaction table update" + trTableValue);
		sInputXML = getDBInsertInputTransationUpdate();
		WriteLog("APInsert Input in transaction update table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output for transaction update table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryRMT() throws WICreateException, Exception {

		trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		WriteLog("trTableColumn" + trTableColumn);
		String Remarks = "Update Request Recevied for UID: "+sUID_UPDATE;
		trTableValue = "'"+ WINumber + "','"+ActivityName+"','Update Request Received','" + sDate + "','"+Remarks+"','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryRMT();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	
	//Kamran 06062022
	
	private void insertIntoHistoryBSR() throws WICreateException, Exception {
		trTableColumn = "wi_name,ws_name,user_name,entry_date_time,action_date_time,team,team_category,decision,reject_reasons,remarks,initiator_team," +
				"hold_reason,hold_till_date";
		WriteLog("trTableColumn" + trTableColumn);
		String Remarks = "Update Request Recevied for UID: "+sUID_UPDATE;
		trTableValue = "'"+ WINumber + "','"+ActivityName+"','Update Request Received','" + sDate + "','"+Remarks+"','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryBSR();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryRAOP() throws WICreateException, Exception {

		trTableColumn = "wi_name,workstep,decision,action_date_time,remarks,user_name,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		String Remarks = "Update Request Recevied for customer authentication: "+sCUST_AUTHENTICATION;
		if(processID.equalsIgnoreCase("RAOP_YAP_AddInfo"))
			Remarks = "Additional Information from YAP is: "+sRemarksAddInfo;
		trTableValue = "'"+ WINumber + "','"+ActivityName+"','Update Request Received','" + sDate + "','"+Remarks+"','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryRAOP();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryDAO() throws WICreateException, Exception {
		trTableColumn = "wi_name, workstep, Decision, decision_date_time, Remarks, user_name, dec_date,reject_reason,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		String Remarks = hm.get("Remarks_dec");
		String reject_Reason = hm.get("Reject_reason");
		String event = hm.get("Event");
		trTableValue = "'"+ WINumber + "','"+ActivityName+"','"+event+"','" + sDate + "','"+Remarks+"','" + sUsername + "','" +sDate+"','"+reject_Reason+"','"+sDate+"'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryDAO();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}

	private void insertIntoHistoryDCC(String remarks, String decisionInHistory) throws WICreateException, Exception {
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		sDate = dateFormat.format(d);
		
		trTableColumn = "wi_name, workstep, Decision, decision_date_time, Remarks, user_name, dec_date";
		WriteLog("trTableColumn" + trTableColumn);
		
		trTableValue = "'"+ WINumber + "','"+PossibleQueues+"','"+decisionInHistory+"','" + sDate + "','"+remarks+"','System','" +sDate+"'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputXml("NG_DCC_GR_DECISION_HISTORY");
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
	}
	
	
	private void insertIntoHistoryDAO_dispatch() throws WICreateException, Exception {

		trTableColumn = "wi_name, workstep, Decision, decision_date_time, Remarks, user_name, dec_date,reject_reason,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		String EntryDatetime = getEntryDateTime_DAOdispatch();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(EntryDatetime);
		String entrydatetime_format = dateFormat.format(d1);
		
		WriteLog("insertIntoHistoryDAO_dispatch : entrydatetime_format :" + entrydatetime_format);
		
		trTableValue = "'"+ WINumber + "','"+ActivityName+"','Approve','" + sDate + "','','" + sUsername + "','" +sDate+"','','"+entrydatetime_format+"'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryDAO();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	
	private void updateHistoryRMT() throws WICreateException, Exception {
		
		extColNames = "decision,actiondatetime,remarks,username";
		WriteLog("extColNames" + extColNames);
		extColValues = "'Update Request Received','"+sDate+"','Updated Recevied Data','"+sUsername+"'";
		WriteLog("extColValues" + extColValues);
		sWhereClause = "WINAME='"+WINumber+"' and wsname='"+ActivityName+"' and actiondatetime is null";
		sInputXML = getDBUpdateInputHistoryTable();
		WriteLog("APUpdate in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate in External Table: " + sOutputXML);
	}
	
	//Kamran 06062022
	private void updateHistoryBSR() throws WICreateException, Exception {
		
		extColNames = "decision,actiondatetime,remarks,username";
		WriteLog("extColNames" + extColNames);
		extColValues = "'Update Request Received','"+sDate+"','Updated Recevied Data','"+sUsername+"'";
		WriteLog("extColValues" + extColValues);
		sWhereClause = "WINAME='"+WINumber+"' and wsname='"+ActivityName+"' and actiondatetime is null";
		sInputXML = getDBUpdateInputHistoryTable();
		WriteLog("APUpdate in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate in External Table: " + sOutputXML);
	}
	
	private void updateInDataInExtTableRMT() throws WICreateException, Exception {
		
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);

		extColNames = extColNames+"UPDATERECEIVED";
		WriteLog("extColNames" + extColNames);
		extColValues = extColValues+"'Y'";
		WriteLog("extColValues" + extColValues);
		sWhereClause = "WINAME='"+WINumber+"'";
		sInputXML = getDBUpdateInputExternalTable();
		WriteLog("APUpdate in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate in External Table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
		//Kamran 06062022
	
	private void updateInDataInExtTableBSR() throws WICreateException, Exception {
		
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);

		extColNames = extColNames+"UPDATERECEIVED";
		WriteLog("extColNames" + extColNames);
		extColValues = extColValues+"'Y'";
		WriteLog("extColValues" + extColValues);
		sWhereClause = "WINAME='"+WINumber+"'";
		sInputXML = getDBUpdateInputExternalTable();
		WriteLog("APUpdate in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate in External Table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void updateInDataInExtTableRAOP() throws WICreateException, Exception {
			
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
	
		extColNames = extColNames+"UPDATERECEIVED,DECISION";
		WriteLog("extColNames for RAOP" + extColNames);
		extColValues = extColValues+"'Y','Submit'";
		WriteLog("extColValues for RAOP" + extColValues);
		sWhereClause = "WINAME='"+WINumber+"'";
		sInputXML = getDBUpdateInputExternalTable();
		WriteLog("APUpdate for RAOP in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate for RAOP in External Table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void updateInDataInExtTableRAOPAddInfo() throws WICreateException, Exception {
		
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
	
		extColNames = extColNames+"UPDATERECEIVED,DECISION";
		WriteLog("extColNames for RAOP" + extColNames);
		extColValues = extColValues+"'N','Submit'";
		WriteLog("extColValues for RAOP" + extColValues);
		sWhereClause = "WINAME='"+WINumber+"'";
		sInputXML = getDBUpdateInputExternalTable();
		WriteLog("APUpdate for RAOP in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate for RAOP in External Table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void updateValidTillInWorklistTable() throws WICreateException, Exception {

		extColNames = "ValidTill";
		WriteLog("extColNames" + extColNames);
		extColValues = "'"+sDate+"'";
		WriteLog("trTableValue" + trTableValue);
		sWhereClause = "ProcessInstanceId='"+WINumber+"'";
		sInputXML = getDBUpdateInputWorklistTable();
		WriteLog("APUpdate in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate in External Table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void updateValidTillInWorkInProcessTable() throws WICreateException, Exception {

		extColNames = "ValidTill";
		WriteLog("extColNames" + extColNames);
		extColValues = "'"+sDate+"'";
		WriteLog("trTableValue" + trTableValue);
		sWhereClause = "ProcessInstanceId='"+WINumber+"'";
		sInputXML = getDBUpdateInputWorkInProcessTable();
		WriteLog("APUpdate in External Table: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APUpdate in External Table: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	
	private String getDBInsertInputTransationUpdate() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + transactionTableName
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	private String getDBInsertInputHistoryRMT() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + RMT_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	//Kamran 06062022
	private String getDBInsertInputHistoryBSR() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + BSR_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	public String getSTPflag()throws WICreateException,Exception
	{
		WriteLog("getSTPflag:");
		String sQuery="select is_stp from NG_DAO_EXTTABLE with (nolock) where WI_name='"+hm.get("WINUMBER")+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("getSTPflag:"+xmlobj);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		String is_stp = getTagValues(sOutputXML, "is_stp");
		WriteLog("is_stp : "+is_stp);
		return is_stp;
	}
	
	
	public String getEntryDateTime_DAOdispatch()throws WICreateException,Exception
	{
		WriteLog("getEntryDateTime_dispatch:");
		String Wi_name=hm.get("WINUMBER");
		String sQuery="select EntryDATETIME from WFINSTRUMENTTABLE  with(nolock) where ProcessInstanceID ='"+ Wi_name +"' and ActivityName = 'Hold_courier_update'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("getSTPflag:"+xmlobj);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		String EntryDATETIME = getTagValues(sOutputXML, "EntryDATETIME");
		WriteLog("getEntryDateTime_dispatch : "+EntryDATETIME);
		return EntryDATETIME;
	}
	
	public String getAWB_fromWIDAO()throws WICreateException,Exception
	{
		WriteLog("getAWB_fromWIDAO:");
		String sQuery="select AWB_Number from NG_DAO_EXTTABLE with (nolock) where WI_name='"+hm.get("WINUMBER")+"'"; // map will get awb number in the tag
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("WI_name:"+xmlobj);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		String getAWB_fromWIDAO = getTagValues(sOutputXML, "AWB_Number");
		WriteLog("getAWB_fromWIDAO : "+getAWB_fromWIDAO);
		return getAWB_fromWIDAO;
	}

	public String getAWBNoForDCC()throws WICreateException,Exception
	{
		WriteLog("GET AWB NO FOR DCC:");
		String sQuery="select AWB_Number from NG_DCC_EXTTABLE with (nolock) where WI_name='"+hm.get("WINUMBER")+"'"; // map will get awb number in the tag
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("WI_name:"+xmlobj);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		String AWB_Number = getTagValues(sOutputXML, "AWB_Number");
		WriteLog("AWB Number DCC : "+AWB_Number);
		return AWB_Number;
	}

	public String is_prime_req()throws WICreateException,Exception
	{
		WriteLog("is_prime_req:");
		String sQuery="select is_prime_req from NG_DAO_EXTTABLE with (nolock) where WI_name='"+hm.get("WINUMBER")+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("getSTPflag:"+xmlobj);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		String is_prime_req = getTagValues(sOutputXML, "is_prime_req");
		WriteLog("is_prime_req : "+is_prime_req);
		return is_prime_req;
	}
	
	public String is_cbs_req()throws WICreateException,Exception
	{
		WriteLog("is_cbs_req:");
		String sQuery="select is_cbs_req from NG_DAO_EXTTABLE with (nolock) where WI_name='"+hm.get("WINUMBER")+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("getSTPflag:"+xmlobj);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		String is_cbs_req = getTagValues(sOutputXML, "is_cbs_req");
		WriteLog(" is_cbs_req: "+is_cbs_req);
		return is_cbs_req;
	}
	
	public String is_ntb()throws WICreateException,Exception
	{
		WriteLog("is_ntb:");
		String sQuery="select is_Ntb from NG_DAO_EXTTABLE with (nolock) where WI_name='"+hm.get("WINUMBER")+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		WriteLog("getSTPflag:"+xmlobj);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		String is_ntb = getTagValues(sOutputXML, "is_Ntb");
		WriteLog(" is_ntb: "+is_ntb);
		return is_ntb;
	}
	
	
	private String getDBInsertExternal() {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>"+DOB_EXTTABLE
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>CC_Wi_Name='"+hm.get("WINumber")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	private String getDBInsert_DAO_External()
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>NG_DAO_EXTTABLE"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WINUMBER")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	private String getDBInsert_DAO_Dispatch_NG_Digital_AWB_Status()
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>NG_Digital_AWB_Status"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WINUMBER")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	
	
	private String getDBInsert_DAO_External_for_dispatchWIupd() //added by hritik 1.8.22 for update in ext table on basis of awb number received via teamX
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>NG_DAO_EXTTABLE" // 17.8.22 - Again changes to WI from AWBno
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WINUMBER")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	// Hritik 22.7.22 - Insert the stp case data to other table.
	private String ng_dao_wi_update() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>ng_dao_wi_update"
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
		//Kamran 07062022
	private String getDBInsert_BSR_External() {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>RB_BSR_EXTTABLE"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WI_NAME")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	//hritik 13.6.22 - Update the doc status in WI
	private String getDBInsert_DAO_Additional_Doc(String Doc_name)
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>NG_DAO_GR_ADDIDTIONAL_DOCUMENT"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WINUMBER")+"' and document_name='"+Doc_name+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}

	private String getInsert_NG_DCC_GR_NetSalaryDetails(String NetSal_Col, String NetSal_val)
	{
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>NG_DCC_GR_NetSalaryDetails"
				+ "</TableName>" + "<ColName>" + NetSal_Col + "</ColName>"
				+ "<Values>" + NetSal_val + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBUpdate_DCC_Additional_Doc(String Doc_name)
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>NG_DCC_GR_ADDITIONAL_DOCUMENT"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WINUMBER")+"' and document_name='"+Doc_name+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	// hritik 13.6.22 
	private String getDBUpdate_AddnalDocsReqd_flag()
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>NG_DAO_EXTTABLE"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WINUMBER")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	// ravindra 7.8.22 
	private String getDBUpdateDocsReqdFlagDCC()
	{
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>NG_DCC_EXTTABLE"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>WI_name='"+hm.get("WINUMBER")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	// Hritik 13.6.22 - Insert Back ground Info to the grid in case of WI Update.
	private String getDBInsert_BackgroundInfo() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>NG_DAO_GR_BACKGROUND_INFORMATI"
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	//hritik 29.6.22 -Insert in [[NG_DAO_UPDATEWI_PINGDATA_DISPATCH]]
	private String getDBInsert_DAO_PINGDATA_DISPATCH()
	{
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
		+ "<Option>APInsert</Option>" + "<TableName>NG_DAO_UPDATEWI_PINGDATA_DISPATCH"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APInsert_Input>";
	}
	
	
	private String getDBInsert_NG_DAO_SINGLEPAGER_DOCS()
	{
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
		+ "<Option>APInsert</Option>" + "<TableName>NG_DAO_SINGLEPAGER_DOCS"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String updateBankNameInGrid() {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>ng_RLOS_Liability_New"
		+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
		+ "<Values>" + trTableValue + "</Values>" + "<WhereClause>wi_name='"+hm.get("WINumber")+"'</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	private String updateCustomTable(String tableName,String ColumnName,String ColumnValue,String WhereClause) {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
		+ "<Option>APUpdate</Option>" + "<TableName>"+tableName
		+ "</TableName>" + "<ColName>" + ColumnName + "</ColName>"
		+ "<Values>" + ColumnValue + "</Values>" + "<WhereClause>"+WhereClause+"</WhereClause>"
		+ "<EngineName>"
		+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
		+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	private String getDBInsertInputHistoryRAOP() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + RAOP_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryDAO() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName> NG_DAO_GR_DECISION_HISTORY"
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputXml(String tableName) {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" +tableName
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	
	private String getDBUpdateInputExternalTable() {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
				+ "<Option>APUpdate</Option>" + "<TableName>" + externalTableName
				+ "</TableName>" + "<ColName>" + extColNames + "</ColName>"
				+ "<Values>" + extColValues + "</Values>" + "<WhereClause>" + sWhereClause + "</WhereClause>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	private String getDBUpdateInputHistoryTable() {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
				+ "<Option>APUpdate</Option>" + "<TableName>" + transactionTableName
				+ "</TableName>" + "<ColName>" + extColNames + "</ColName>"
				+ "<Values>" + extColValues + "</Values>" + "<WhereClause>" + sWhereClause + "</WhereClause>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	private String getDBUpdateInputWorklistTable() {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
				+ "<Option>APUpdate</Option>" + "<TableName>" + "WORKLISTTABLE"
				+ "</TableName>" + "<ColName>" + extColNames + "</ColName>"
				+ "<Values>" + extColValues + "</Values>" + "<WhereClause>" + sWhereClause + "</WhereClause>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	private String getDBUpdateInputWorkInProcessTable() {
		return "<?xml version=\"1.0\"?>" + "<APUpdate_Input>"
				+ "<Option>APUpdate</Option>" + "<TableName>" + "WORKINPROCESSTABLE"
				+ "</TableName>" + "<ColName>" + extColNames + "</ColName>"
				+ "<Values>" + extColValues + "</Values>" + "<WhereClause>" + sWhereClause + "</WhereClause>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APUpdate_Input>";
	}
	
	public  String getWorkItemInput()
	{
		return "<?xml version=\"1.0\"?>" + "<WMGetWorkItem_Input>"
				+ "<Option>WMGetWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + "1" + "</WorkItemId>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMGetWorkItem_Input>";
	}		
	public  String getWorkItemInputDCC()
	{
		return "<?xml version=\"1.0\"?>" + "<WMGetWorkItem_Input>"
				+ "<Option>WMGetWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + sWorkitemid + "</WorkItemId>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMGetWorkItem_Input>";
	}	
	
	public  String getWorkItemInput_withWorkitemID(String Workitem_ID)
	{
		return "<?xml version=\"1.0\"?>" + "<WMGetWorkItem_Input>"
				+ "<Option>WMGetWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + Workitem_ID + "</WorkItemId>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMGetWorkItem_Input>";
	}	
	
	
	public  String completeWorkItemInput()
	{
		return "<?xml version=\"1.0\"?>" + "<WMCompleteWorkItem_Input>"
				+ "<Option>WMCompleteWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + "1" + "</WorkItemId>"
				+ "<AuditStatus></AuditStatus>"
				+ "<Comments></Comments>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMCompleteWorkItem_Input>";
	}
	
	public  String completeWorkItemInputDCC()
	{
		return "<?xml version=\"1.0\"?>" + "<WMCompleteWorkItem_Input>"
				+ "<Option>WMCompleteWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + sWorkitemid + "</WorkItemId>"
				+ "<AuditStatus></AuditStatus>"
				+ "<Comments></Comments>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMCompleteWorkItem_Input>";
	}
	public  String completeWorkItemInput_withWorkitemID(String Workitem_ID)
	{
		return "<?xml version=\"1.0\"?>" + "<WMCompleteWorkItem_Input>"
				+ "<Option>WMCompleteWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
				+ "</ProcessInstanceId>" + "<WorkItemId>" + Workitem_ID + "</WorkItemId>"
				+ "<AuditStatus></AuditStatus>"
				+ "<Comments></Comments>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMCompleteWorkItem_Input>";
	}	

	private void getProcessDefID() throws WICreateException, Exception
	{
		sInputXML=getAPSelectWithColumnNamesXML("select a.PROCESSDEFID, b.PROCESSID from processdeftable a with(nolock),"+WSR_PROCESS+" b with(nolock) where a.processname='"+sProcessName+"' and b.SUBPROCESSNAME='"+sSubProcess+"' and a.processname=b.processname and b.isactive='Y'");
		if("CDOB".equalsIgnoreCase(sProcessName))
			sInputXML=getAPSelectWithColumnNamesXML("select a.PROCESSDEFID, b.PROCESSID from processdeftable a with(nolock),"+WSR_PROCESS+" b with(nolock) where a.processname='DigitalOnBoarding' and b.SUBPROCESSNAME='"+sSubProcess+"' and a.processname='DigitalOnBoarding' and isactive='Y'");
		WriteLog("APSelectWithColumnNames Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("APSelectWithColumnNames Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj); 
		processDefID=getTagValues(sOutputXML, "PROCESSDEFID");
		WriteLog("processDefID: "+processDefID);
		processID=getTagValues(sOutputXML, "PROCESSID");
		WriteLog("processID: "+processID);
		if(processID.equalsIgnoreCase(""))
			throw new WICreateException("1019",pCodes.getString("1019")+":"+sProcessName+"/"+sSubProcess);
		
	}
	
	
	private void checkExistingSession() throws Exception, WICreateException
	{
		WriteLog("inside checkExistingSession");
		//String getSessionQry="select top(1) RANDOMNUMBER from pdbconnection";
		String getSessionQry="select randomnumber from pdbconnection where userindex in (select userindex from pdbuser where username='"+sUsername+"')";
		sInputXML=getAPSelectWithColumnNamesXML(getSessionQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		//Check Main Code
		checkCallsMainCode(xmlobj);
		sSessionID=getTagValues(sOutputXML,"randomnumber");
		WriteLog("SessionID: "+sSessionID);
		if(sSessionID.equalsIgnoreCase(""))
		{
			sInputXML=getWMConnectXML();
			//WriteLog("WM Connect Input: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("WM Connect Output: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			//Check Main Code
			checkCallsMainCode(xmlobj);
        	sSessionID=getTagValues(sOutputXML,"SessionId");
        	sessionFlag=true;
		}
		response.setSessionId(sSessionID);
	}
	private void checkCallsMainCode(XMLParser obj) throws WICreateException
	{
		WriteLog("Inside checkCallsMainCode");
		if(!xmlobj.getValueOf("MainCode").trim().equalsIgnoreCase(SUCCESS_STATUS))
 		{
			if (!xmlobj.getValueOf("SubErrorCode").equalsIgnoreCase(""))
			{
				eCode=xmlobj.getValueOf("SubErrorCode");
				eDesc=xmlobj.getValueOf("Description");
			}
			else if(!xmlobj.getValueOf("Output").equalsIgnoreCase(""))
			{
				eCode=xmlobj.getValueOf("MainCode");
				eDesc=xmlobj.getValueOf("Output");
			}
			else 
			{
				eCode=xmlobj.getValueOf("Status");
				eDesc=xmlobj.getValueOf("Error");
			}
    		throw new WICreateException(eCode,eDesc);
       	}
	}
	
	private void loadConfiguration() throws IOException, Exception
	{
		//load file configuration
		WriteLog("Loading Configuration file");
		Properties p = new Properties();
		String sConfigFile=new StringBuilder().append(System.getProperty("user.dir"))
				.append(System.getProperty("file.separator")).append("BPMCustomWebservicesConf")
				.append(System.getProperty("file.separator")).append("config.properties").toString();
		
		p.load(new FileInputStream(sConfigFile));
	    sCabinetName=p.getProperty("CabinetName");
	    sJtsIp=p.getProperty("JtsIp");
	    iJtsPort=Integer.parseInt(p.getProperty("JtsPort"));
	    sUsername=p.getProperty("username");
		try
		{
			//WriteLog("sPassword::"+sPassword);
			sPassword=decryptPassword(p.getProperty("password"));	
			//WriteLog("after decrption::"+sPassword);
		}
		catch(Exception e)
		{
			WriteLog("Inside exception of log decryption");
			throw new WICreateException("3001",pCodes.getString("3001"));
		}
	    sTempLoc=p.getProperty("TempDocumentLoc");
	    iVolId=Integer.parseInt(p.getProperty("volid"));
	    WriteLog("CabinetName: "+sCabinetName+", JtsIp: "+sJtsIp+", JtsPort: "+iJtsPort+" ,Username: "+sUsername+", Password: "+p.getProperty("password")+" ,VolumeID: "+iVolId);
	    WriteLog("Configuration file loaded successfuly");
	    response.setCabinetName(sCabinetName);
	    response.setJtsIp(sJtsIp);
	    response.setJtsPort(iJtsPort);
	    response.setUsername(sUsername);
	}
	/*
	private String decryptPassword(String pass) throws Exception
	{
		return AESEncryption.decrypt(pass);
	}
	*/
	
	
	private String executeAPI(String sInputXML) throws WICreateException
	{
		String sOutputXML="";
		try
		{
			 Socket sock = null;
			 sock = new Socket(sJtsIp, iJtsPort);
			 DataOutputStream oOut = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
			 DataInputStream oIn = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
			 byte[] SendStream = sInputXML.getBytes("8859_1");
			 int strLen = SendStream.length;
	         oOut.writeInt(strLen);
	    	 oOut.write(SendStream, 0, strLen);
			 oOut.flush();
			 int length = 0;
			 length = oIn.readInt();
			 byte[] readStream = new byte[length];
			 oIn.readFully(readStream);
			 sOutputXML= new String(readStream, "8859_1");
			 sock.close(); 
		}
		catch (Exception e)
		{
			WriteLog("Exception: "+e.getMessage());
			throw new WICreateException("1006",pCodes.getString("1006")+" : "+e.getMessage());
		}
		return sOutputXML;
     	
	}

	private void fetchRequestParameters(WICreateRequest req) throws WICreateException
	{		
		try
		{
			WriteLog("Inside fetchRequestParameters");
			sProcessName=(req.getProcessName()==null)? "" : req.getProcessName().trim();
			sSubProcess=(req.getSubProcess()==null)? "" : req.getSubProcess().trim();
			headerObjRequest=req.getEE_EAI_HEADER();
			
			
			sMsgFormat=(headerObjRequest.getMsgFormat()==null)? "" : headerObjRequest.getMsgFormat().trim();
			sMsgVersion=(headerObjRequest.getMsgVersion()==null)? "" : headerObjRequest.getMsgVersion().trim();
			sMsgFormat=(headerObjRequest.getMsgFormat()==null)? "" : headerObjRequest.getMsgFormat().trim();
			sRequestorChannelId=(headerObjRequest.getRequestorChannelId()==null)? "" : headerObjRequest.getRequestorChannelId().trim();
			sRequestorUserId=(headerObjRequest.getRequestorUserId()==null)? "" : headerObjRequest.getRequestorUserId().trim();
			sRequestorLanguage=(headerObjRequest.getRequestorLanguage()==null)? "" : headerObjRequest.getRequestorLanguage().trim();
			sRequestorSecurityInfo=(headerObjRequest.getRequestorSecurityInfo()==null)? "" : headerObjRequest.getRequestorSecurityInfo().trim();
			sExtra2=(headerObjRequest.getExtra2()==null)? "" : headerObjRequest.getExtra2().trim();
			sExtra1=(headerObjRequest.getExtra1()==null)? "" : headerObjRequest.getExtra1().trim();
			sMessageId=(headerObjRequest.getMessageId()==null)? "" : headerObjRequest.getMessageId().trim();
			
			//Deepak Code change to get InputMessage to  utilise Repetitive tags
			InputMessage=(req.getInputMessage()==null)? "" : req.getInputMessage().trim();
			
			attributesObj=req.getAttributes();
			attributeObj=attributesObj.getAttribute();
			WriteLog("attributeObj.length"+attributeObj.length);
			//Fetch Name Value Attributes in Hash Map
			for(int i=0;i<attributeObj.length;i++)
			{
				
				hm.put(attributeObj[i].getName(), attributeObj[i].getValue());
			}
						
			WriteLog("sProcessName: "+sProcessName+", sSubProcess: "+sSubProcess);
		}
		catch(Exception e)
		{
			WriteLog("Exception: "+e.getMessage());
			throw new WICreateException("1007",pCodes.getString("1007")+" : "+e.getMessage());
		}
	}
	private void checkAttributeTable(String attributeName, String attributeValue) throws WICreateException, Exception
	{
		WriteLog("inside checkAttributeTable");
		String getExtTransQry="";
		if(sProcessName.equalsIgnoreCase("RAOP")) // for iBPS Processes
		{
			getExtTransQry="select ATTRIBUTENAME,EXTERNALTABLECOLNAME, TRANSACTIONTABLECOLNAME, ISMANDATORY,ATTRIBUTE_FORMAT,ATTRIBUTE_TYPE from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME='"+attributeName+"' and PROCESSID='"+processID+"'";
		}
		else // for BPM Processes
		{
			getExtTransQry="select ATTRIBUTENAME,EXTERNALTABLECOLNAME, TRANSACTIONTABLECOLNAME, ISMANDATORY from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME='"+attributeName+"' and PROCESSID='"+processID+"'";
		}	
		sInputXML=getAPSelectWithColumnNamesXML(getExtTransQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String attNameFromTable=getTagValues(sOutputXML, "ATTRIBUTENAME");
		String extFlag=getTagValues(sOutputXML, "EXTERNALTABLECOLNAME");
		String transFlag=getTagValues(sOutputXML, "TRANSACTIONTABLECOLNAME");
		String isMandatory=getTagValues(sOutputXML, "ISMANDATORY");
		
		WriteLog("EXTERNALTABLECOLNAME: "+extFlag+ "TRANSACTIONTABLECOLNAME: "+transFlag+" PROCESSID: "+processID+" ISMANDATORY: "+isMandatory+" attNameFromTable:"+attNameFromTable);
			
		//Added by Nikita for the checking length of the attribute value at 14062019
		
				if(sProcessName.equalsIgnoreCase("RAOP"))
				{
					
					String strattributesize=getTagValues(sOutputXML, "ATTRIBUTE_FORMAT");
					String attType = getTagValues(sOutputXML, "ATTRIBUTE_TYPE");
					attributeValue=attributeValue.trim();
					if(!(attributeValue.equalsIgnoreCase("")))
					{
						int attributelength = attributeValue.length();
						WriteLog("attributelength--"+attributelength);
						WriteLog("attNameFromTable--"+attNameFromTable);
						if(!attType.equalsIgnoreCase(""))
						{
							if(attType.equalsIgnoreCase("DATE"))
							{
								Date date = null;
								SimpleDateFormat sdf = new SimpleDateFormat(strattributesize);
							    date = sdf.parse(attributeValue);
							    if (!attributeValue.equals(sdf.format(date))) 
							    {
							    	throw new WICreateException("1116",pCodes.getString("1116")+" :"+attributeName);
							    }
							}
							else if(attType.equalsIgnoreCase("AMOUNT"))
							{
								try
								{
									if(!attributeValue.contains("."))
										attributeValue=attributeValue+".00";
									Double.parseDouble(attributeValue);
									
								}
								catch(Exception ex)
								{
									throw new WICreateException("1117",pCodes.getString("1117")+" :"+attributeName);
								}
								
							}
							else
							{
								//WriteLog("inside 991--"+attNameFromTable);	
								int attributesize = Integer.parseInt(strattributesize);
								if(attributelength>attributesize)
								{
									throw new WICreateException("1111",pCodes.getString("1111")+" :"+attributeName);
								}
								
								String patternMatch = "";
													
								if(attType.equalsIgnoreCase("NUMERIC"))
								{	
									patternMatch="[0-9]+";
									if(!Pattern.matches(patternMatch, attributeValue))
									{
										WriteLog("inside 995");
										throw new WICreateException("1112",pCodes.getString("1112")+" :"+attributeName);
									}
									
									
								}
								
								if(attType.equalsIgnoreCase("ALPHANUMERIC"))
								{
									patternMatch="^[a-zA-Z0-9 ]*$";
									if(!Pattern.matches(patternMatch, attributeValue))
									{
										throw new WICreateException("1113",pCodes.getString("1113")+" :"+attributeName);
									}
								}
								
								if(attType.equalsIgnoreCase("ALPHAONLY"))
								{
									patternMatch="^[a-zA-Z ]*$";
									if(!Pattern.matches(patternMatch, attributeValue))
									{
										throw new WICreateException("1114",pCodes.getString("1114")+" :"+attributeName);
									}
								}
								
								if(attType.equalsIgnoreCase("APLPHANUMERICWITHSPACE"))
								{
									patternMatch="^[a-zA-Z0-9 ]*$";
									if(!Pattern.matches(patternMatch, attributeValue))
									{
										throw new WICreateException("1115",pCodes.getString("1115")+" :"+attributeName);
									}
								}
								
								if(attType.equalsIgnoreCase("APLPHANUMERICWITHSPECIALCHAR"))
								{
									patternMatch="^[a-zA-Z0-9-#_!.@()+/%&\\s|~ ]*$";
									if(!Pattern.matches(patternMatch, attributeValue))
									{
										throw new WICreateException("1118",pCodes.getString("1118")+" :"+attributeName);
										
									}
								}
							}
						}
					}
					
				}	
				//Code added by Nikita ends on 18062019
		
		if(!attNameFromTable.equalsIgnoreCase(""))
		{
			WriteLog("Attribute Name: "+attributeName);
			if(((isMandatory.equalsIgnoreCase("Y") || isMandatory.equalsIgnoreCase("N") || isMandatory.equalsIgnoreCase("C")) && !attributeValue.trim().equalsIgnoreCase("")) 
					|| ((isMandatory.equalsIgnoreCase("N") || isMandatory.equalsIgnoreCase("C")) && attributeValue.trim().equalsIgnoreCase("")))
			{
				if (!extFlag.equalsIgnoreCase("") && transFlag.equalsIgnoreCase(""))
				{
					if (attributeValue.contains("'"))
						attributeValue = attributeValue.replace("'", "");
					
					if(sProcessName.equalsIgnoreCase("RMT"))
					{
						if (extFlag.equalsIgnoreCase("UID_UPDATE")) // for RMT Process
							sUID_UPDATE = attributeValue;
					}
					
					//added for RAOP Process for 2nd call
					if("RAOP_YAP".equalsIgnoreCase(processID))
					{	
						if (extFlag.equalsIgnoreCase("CUSTOMERAUTHENTICATED"))
						{
							sCUST_AUTHENTICATION = attributeValue;
							WriteLog("sCUST_AUTHENTICATION 1---"+attributeValue);
						}
						else if(extFlag.equalsIgnoreCase("YAP_CUST_AUTHENTICATED"))
						{	sCUST_AUTHENTICATION = attributeValue;
							WriteLog("sCUST_AUTHENTICATION 11---"+sCUST_AUTHENTICATION);
						}	
					}
					
					//added for RAOP Process for Additional Info
					if("RAOP_YAP_AddInfo".equalsIgnoreCase(processID))
					{	
						if(extFlag.equalsIgnoreCase("REMARKS"))
						{	sRemarksAddInfo = attributeValue;
							WriteLog("sRemarksAddInfo 11---"+sRemarksAddInfo);
						}	
					}
					
					attributeTag=attributeTag+extFlag+(char)21+attributeValue.trim()+(char)25;
					extColNames=extColNames+extFlag+",";
					extColValues=extColValues+"'"+attributeValue.trim()+"'"+",";
				}
				else
					 throw new WICreateException("1015",pCodes.getString("1015")+" :"+attributeName);
			}
			else if(isMandatory.equalsIgnoreCase("Y") && attributeValue.equalsIgnoreCase(""))
			{
				 throw new WICreateException("1016",pCodes.getString("1016")+" :"+attributeName);
			}
		}
		else
		{	
			WriteLog("No Value Mapped for name");
			throw new WICreateException("1017",pCodes.getString("1017")+" :"+attributeName);
		}
		
	}
	
	private void checkAttributeTable_DCC(String attributeName, String attributeValue) throws WICreateException, Exception
	{
		WriteLog("inside checkAttributeTable_DCC");
		String getExtTransQry="";
		String IsIncludedInUpdate = "Y";
		if(sProcessName.equalsIgnoreCase("Digital_CC")) // for iBPS Processes
		{
			getExtTransQry="select ATTRIBUTENAME,EXTERNALTABLECOLNAME, TRANSACTIONTABLECOLNAME, ISMANDATORY,ATTRIBUTE_FORMAT,ATTRIBUTE_TYPE from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME='"+attributeName+"' and PROCESSID='"+processID+"'";
		}
		
		sInputXML=getAPSelectWithColumnNamesXML(getExtTransQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String attNameFromTable=getTagValues(sOutputXML, "ATTRIBUTENAME");
		String extFlag=getTagValues(sOutputXML, "EXTERNALTABLECOLNAME");
		String transFlag=getTagValues(sOutputXML, "TRANSACTIONTABLECOLNAME");
		String isMandatory=getTagValues(sOutputXML, "ISMANDATORY");
		String ExtCol="";
		String ExtVal="";
		
		// Hritik 09.10.23
		if(processID.equalsIgnoreCase("DCC_UPDATE_CIF_CARD_DETAILS")
			&& attributeName.equalsIgnoreCase("Updated_CardType_Desc")){
			if(isMandatory.contains("`")){
				String[] mand_arr = isMandatory.split("`");
				isMandatory= mand_arr[0];
			}
			if(transFlag.contains("`")){
				transFlag="";
			}
		}
		
		WriteLog("EXTERNALTABLECOLNAME: "+extFlag+ "TRANSACTIONTABLECOLNAME: "+transFlag+" PROCESSID: "+processID+" ISMANDATORY: "+isMandatory+" attNameFromTable:"+attNameFromTable);
		
		if(!attNameFromTable.equalsIgnoreCase(""))
		{
			WriteLog("Attribute Name: "+attributeName);
			if(((isMandatory.equalsIgnoreCase("Y") || isMandatory.equalsIgnoreCase("N") || isMandatory.equalsIgnoreCase("C")) && !attributeValue.trim().equalsIgnoreCase("")) 
					|| ((isMandatory.equalsIgnoreCase("N") || isMandatory.equalsIgnoreCase("C")) && attributeValue.trim().equalsIgnoreCase("")))
			{
				if (!extFlag.equalsIgnoreCase("") && transFlag.equalsIgnoreCase(""))
				{
					if (attributeValue.contains("'"))
						attributeValue = attributeValue.replace("'", "");
					
					if (attributeValue.contains(","))
						attributeValue = attributeValue.replace(",", " ");
					
					if("DCC_FIRCO_ADD_DOCS".equalsIgnoreCase(processID))
					{	
						if(attNameFromTable.equalsIgnoreCase("FircoAdditionalRemarks"))
						{	sRemarksAddInfo = attributeValue;
							WriteLog("sRemarksAddInfo 11 DCC Firco---"+sRemarksAddInfo);
							IsIncludedInUpdate="N";
						}
						if(attNameFromTable.equalsIgnoreCase("FircoUpdateAction"))
						{	FircoUpdateActionDCC = attributeValue;
							WriteLog("FircoUpdateAction 11 DCC Firco---"+FircoUpdateActionDCC);
						}
						if(attNameFromTable.equalsIgnoreCase("WINUMBER"))
						{	
							IsIncludedInUpdate="N";
						}
					}
					
					if(	processID.equalsIgnoreCase("DCC_FIRCO_ADD_DOCS") || processID.equalsIgnoreCase("DCC_FIRCO_NO_ACTION") || 
						processID.equalsIgnoreCase("DCC_FTS_DETAILS") || processID.equalsIgnoreCase("DCC_NO_BANK_STMT_ANALYSIS") || 
						processID.equalsIgnoreCase("DCC_BS_NO_ACTION") || processID.equalsIgnoreCase("DCC_END_COOLING") || 
						processID.equalsIgnoreCase("DCC_CARD_DEL_DOC_REC") || processID.equalsIgnoreCase("DCC_SALARY_NO_ACTION")||
						processID.equalsIgnoreCase("DCC_UPDATE_CIF_CARD_DETAILS") || processID.equalsIgnoreCase("DCC_CARD_DETAILS_NO_ACTN"))
					{
						if(attNameFromTable.equalsIgnoreCase("WINUMBER") || attNameFromTable.equalsIgnoreCase("Prospect_id"))
						{
							IsIncludedInUpdate="N";
						}
					}
					
					if (processID.equalsIgnoreCase("DCC_FIRCO_ADD_DOCS") && attNameFromTable.equalsIgnoreCase("Document_Name_List")) {
						IsIncludedInUpdate = "N";
					}
					
					if (processID.equalsIgnoreCase("DCC_FIRCO_NO_ACTION")) {
						IsIncludedInUpdate = "N";
					}
					
					if (processID.equalsIgnoreCase("DCC_BS_NO_ACTION")) {
						if (attNameFromTable.equalsIgnoreCase("Initaited_Date") || attNameFromTable.equalsIgnoreCase("Expiry_Date"))
							IsIncludedInUpdate = "N";
					}
					// Hritik 09.10.23
					if(processID.equalsIgnoreCase("DCC_UPDATE_CIF_CARD_DETAILS") && attributeName.equalsIgnoreCase("Updated_CardType_Desc")){
						String[] ExtCol_arr=extFlag.split("`");

						for (int i = 0; i < ExtCol_arr.length; i++){
							if(ExtCol.equals("")){
								ExtCol=ExtCol_arr[i];
								ExtVal="'"+attributeValue+"'"+",'";
							}
							else{
								ExtCol+=","+ExtCol_arr[i];
								ExtVal+=attributeValue+"'"+",";
							}
							WriteLog("element"+ExtCol);
						}
						extFlag=ExtCol;
	
					}
				
					if(IsIncludedInUpdate.equalsIgnoreCase("Y"))
					{
						attributeTag=attributeTag+extFlag+(char)21+attributeValue.trim()+(char)25;
						extColNames=extColNames+extFlag+",";
						if(processID.equalsIgnoreCase("DCC_UPDATE_CIF_CARD_DETAILS") && attributeName.equalsIgnoreCase("Updated_CardType_Desc")){ // Hritik 09.10.23
							extColValues=extColValues+ExtVal;
						}else
							extColValues=extColValues+"'"+attributeValue.trim()+"'"+",";
					}
				}
				else
					 throw new WICreateException("1015",pCodes.getString("1015")+" :"+attributeName);
			}
			else if(isMandatory.equalsIgnoreCase("Y") && attributeValue.equalsIgnoreCase(""))
			{
				 throw new WICreateException("1016",pCodes.getString("1016")+" :"+attributeName);
			}
		}
		else
		{	
			WriteLog("No Value Mapped for name");
			throw new WICreateException("1017",pCodes.getString("1017")+" :"+attributeName);
		}
		
	}
	
	private String getWorkitemID() throws WICreateException, Exception
	{
		String WorkitemId="";
		String strActivity_name="";
		try{
			if (ActivityName.toUpperCase().contains("CUSTOMER_HOLD")){
				strActivity_name = "CUSTOMER_HOLD";
			}
			String getWorkitemIDQry="select workitemid  from QUEUEVIEW with (nolock) where PROCESSINSTANCEID='"+WINumber+"' and ActivityName='"+strActivity_name+"'";
			sInputXML=getAPSelectWithColumnNamesXML(getWorkitemIDQry);
			WriteLog("Input XML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			WorkitemId=getTagValues(sOutputXML, "workitemid");
			WriteLog("WorkitemId: "+WorkitemId);
		}
		catch(Exception e){
			WriteLog("Exception occured while retriving workitem ID: "+e.getMessage());
			WorkitemId="1";
		}
		//from tables here
		
		return WorkitemId;
	}
	
	private void getWorkItemID() throws WICreateException, Exception {
		try {
			String getWorkitemIDQry = "select workitemid  from QUEUEVIEW with (nolock) where PROCESSINSTANCEID='" + WINumber + "' and ActivityName='" + PossibleQueues + "'";
			sInputXML = getAPSelectWithColumnNamesXML(getWorkitemIDQry);
			WriteLog("Input XML: " + sInputXML);
			sOutputXML = executeAPI(sInputXML);
			WriteLog("Output XML: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			sWorkitemid = getTagValues(sOutputXML, "workitemid");
			WriteLog("WorkitemId: " + sWorkitemid);
		} catch (Exception e) {
			WriteLog("Exception occured while retriving workitem ID: " + e.getMessage());
			sWorkitemid = "1";
		}
		// from tables here

	}
	
	private void getTableName() throws WICreateException, Exception
	{
		//from tables here
		String getTableNameQry="select EXTERNALTABLE ,HISTORYTABLE  from "+WSR_PROCESS+" where PROCESSID='"+processID+"'";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		externalTableName=getTagValues(sOutputXML, "EXTERNALTABLE");
		WriteLog("External table Name: "+externalTableName);
		transactionTableName=getTagValues(sOutputXML, "HISTORYTABLE");
		WriteLog("Transaction table Name: "+transactionTableName);
	}
	private void getWorkitemStage(String attributeList[]) throws WICreateException, Exception
	{
		// from tables here
		String attrName = "";
		String attrValue = "";
		String strOperationName = "";
		for (int i = 0; i < attributeList.length; i++) {
			attrName = getTagValues(attributeList[i], "Name");
			attrValue = getTagValues(attributeList[i], "Value");
			if (attrName.equalsIgnoreCase("WINUMBER")) {
				WINumber = "";
				WINumber = attrValue;
			}
			// Kamran - BSR 07062022
			if (attrName.equalsIgnoreCase("WI_NAME")) {
				WINumber = "";
				WINumber = attrValue;
			}
			if ("OperationName".equalsIgnoreCase(attrName)) {
				strOperationName = attrValue;
			}
		}
		String getTableNameQry="select ACTIVITYNAME  from QUEUEVIEW with (nolock) where PROCESSINSTANCEID='"+WINumber+"'";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get Activity Name: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML to Get Activity Name: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		ActivityName=getTagValues(sOutputXML, "ACTIVITYNAME");
		WriteLog("ActivityName from QueueView: "+ActivityName);
		response.setWorkitemNumber(WINumber);
		if("CDOB".equalsIgnoreCase(sProcessName)){
			if("".equals(ActivityName))
				throw new WICreateException("5052",pCodes.getString("5052") +"  Process Name "+sProcessName);
			if(!"DataUpdate".equalsIgnoreCase(strOperationName) && !"BiometricUpdate".equalsIgnoreCase(strOperationName))
				throw new WICreateException("6006",pCodes.getString("6006"));
			/*if("DataUpdate".equalsIgnoreCase(strOperationName) && !"Customer_Hold".equalsIgnoreCase(ActivityName))  commented Test
				throw new WICreateException("6007",pCodes.getString("6007"));
			else if("BiometricUpdate".equalsIgnoreCase(strOperationName) && !"Disbursal_Hold".equalsIgnoreCase(ActivityName))
				throw new WICreateException("6008",pCodes.getString("6008"));*/
			
			if("BiometricUpdate".equalsIgnoreCase(strOperationName)){ 
				//&& "FAILED".equalsIgnoreCase(hm.get("")) !hm.containsKey("Remarks")
				if(!hm.containsKey("BiometricStatus"))
					throw new WICreateException("6010",pCodes.getString("6010"));
				
				if("False".equalsIgnoreCase(hm.get("BiometricStatus")) && (!hm.containsKey("Remarks") || "".equals(hm.get("REMARKS"))))
					throw new WICreateException("6009",pCodes.getString("6009"));
			}
		}
	}
	
	
	private void getPossibleUpdateQueues() throws WICreateException, Exception
	{
		//from tables here
		String getTableNameQry="select QUEUENAME from "+WSR_POSSIBLEQUEUES+" with (nolock) where ProcessName='"+sProcessName+"'";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get Possible Update Queues: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML to Get Possible Update Queues: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		PossibleQueues=getTagValues(sOutputXML, "QUEUENAME");
		WriteLog("PossibleQueues from Master: "+PossibleQueues);
	}
	

	
	private void getPossibleUpdateQueuesDCC() throws WICreateException, Exception
	{
		//from tables here
		String getTableNameQry="select QUEUENAME from "+WSR_POSSIBLEQUEUES+" with (nolock) where ProcessName='"+sProcessName+"' AND SUBPROCESSNAME ='" + sSubProcess+ "'" ;
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get Possible Update Queues: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML to Get Possible Update Queues: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		PossibleQueues=getTagValues(sOutputXML, "QUEUENAME");
		WriteLog("PossibleQueues from Master: "+PossibleQueues);
	}
	
	private String getRAOPNationality() throws WICreateException, Exception
	{
		//from tables here
		String getTableNameQry="select NATIONALITY from RB_RAOP_EXTTABLE with(nolock) where WINAME ='"+WINumber+"'";
		String sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get RAOP Nationality: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML to Get RAOP Nationality: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String Nationality=getTagValues(sOutputXML, "NATIONALITY");
		WriteLog("Nationality is: "+Nationality);
		return Nationality;
	}
	
	private String getRemittanceCheckerDecision() throws WICreateException,Exception
	{
		String getTableNameQry="select Remittance_Checker_Decision from rb_rmt_exttable with (nolock) where WINAME='"+WINumber+"'";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get Remittance Checker decision: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML to Get Remittance Checker decision: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		Dec_RemittanceChecker=getTagValues(sOutputXML, "Remittance_Checker_Decision");
		WriteLog("Remittance Checker decision from External Table: "+Dec_RemittanceChecker);
		return Dec_RemittanceChecker;
	
	}
	
	private String getRAOPAddInfoStatus() throws WICreateException,Exception
	{
		String getTableNameQry="select PREVWS_InfoReqCust from RB_RAOP_EXTTABLE with(nolock) where WINAME='"+WINumber+"'";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get RAOP PREVWS_InfoReqCust Status: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML to Get RAOP PREVWS_InfoReqCust Status: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String PREVWS_InfoReqCust =getTagValues(sOutputXML, "PREVWS_InfoReqCust");
		WriteLog("RAOP PREVWS_InfoReqCust Status from External Table: "+PREVWS_InfoReqCust);
		return PREVWS_InfoReqCust;
	
	}
	
	private void validateRequestParameters(String attributeList[]) throws WICreateException,Exception
	{
		WriteLog("Inside validateRequestParameters");
		if(sProcessName.equalsIgnoreCase("?") || sProcessName.equalsIgnoreCase(""))
			throw new WICreateException("1001",pCodes.getString("1001"));
		
		if(("?".equalsIgnoreCase(sSubProcess) || "".equalsIgnoreCase(sSubProcess)) && (!"RMT".equalsIgnoreCase(sSubProcess) && !"CDOB".equalsIgnoreCase(sProcessName) &&  !"DigitalAO".equalsIgnoreCase(sProcessName)))
			throw new WICreateException("1002",pCodes.getString("1002"));
		
		if(sRequestorChannelId.equalsIgnoreCase("?") || sRequestorChannelId.equalsIgnoreCase(""))
			throw new WICreateException("1011",pCodes.getString("1011"));
		//Fetch ProcessDefID
		getProcessDefID();	
		
		// Checking update is possible for workitem or not		
		getWorkitemStage(attributeList);
		if (ActivityName.equalsIgnoreCase("")) // Activityname blank means, workitem doesn't exists in flow
		{
			throw new WICreateException("5052",pCodes.getString("5052")+": "+WINumber);
		}	
				
		//If User has taken the decision at Remittance Checker as Approve then Update will not be possible
		
		//Modified by Nikita on 06062019 for RAOP Update
		if(processID.equalsIgnoreCase("RAOP_YAP") || processID.equalsIgnoreCase("RAOP_YAP_AddInfo"))
		{
			getPossibleUpdateQueues();
		}
		
		if(processID.equalsIgnoreCase("RMT"))
		{
			String RemittanceCheckerDecision= getRemittanceCheckerDecision();
			if(RemittanceCheckerDecision.equalsIgnoreCase("Approve"))
			{
				throw new WICreateException("5051",pCodes.getString("5051")+": "+ActivityName);
			}
			else
			{
				getPossibleUpdateQueues();
			}
		}
		
		if("RMT".equalsIgnoreCase(processID) || "RAOP_YAP".equalsIgnoreCase(processID) || "RAOP_YAP_AddInfo".equalsIgnoreCase(processID))
		{		
			if (PossibleQueues.contains(ActivityName))
			{
				WriteLog("Update request is possible at this queue: "+ActivityName);
				
				if("RAOP_YAP".equalsIgnoreCase(processID))
				{
					String RAOPAddInfoStatus = getRAOPAddInfoStatus().trim();
					if("".equalsIgnoreCase(RAOPAddInfoStatus) || "NULL".equalsIgnoreCase(RAOPAddInfoStatus))
					{
						// Nothing to do - if this value is blank means workitem is waiting for 2nd call from YAP
					} else {
						WriteLog("RAOP_YAP Update request is not possible as Add Info from Customer was asked: "+RAOPAddInfoStatus);
						throw new WICreateException("5050E",pCodes.getString("5050E"));
					}
				}
				else if("RAOP_YAP_AddInfo".equalsIgnoreCase(processID))
				{
					String RAOPAddInfoStatus = getRAOPAddInfoStatus().trim();
					if("".equalsIgnoreCase(RAOPAddInfoStatus) || "NULL".equalsIgnoreCase(RAOPAddInfoStatus))
					{
						WriteLog("RAOP_YAP_AddInfo Update request is not possible as WI is waiting for the second call: "+RAOPAddInfoStatus);
						throw new WICreateException("5050D",pCodes.getString("5050D"));
					} else {
						// Nothing to do - if this value is not blank means workitem is waiting for Additional Info from YAP
					}
				}
			}
			else if("RMT".equalsIgnoreCase(processID))
			{
				WriteLog("Update request is not possible at this queue: "+ActivityName);	
				throw new WICreateException("5050",pCodes.getString("5050"));
			}
			else if("RAOP_YAP".equalsIgnoreCase(processID) || "RAOP_YAP_AddInfo".equalsIgnoreCase(processID))
			{
				WriteLog("Update request is not possible at this queue: "+ActivityName);
				//throw new WICreateException("5050",pCodes.getString("5050"));
				if("Discard".equalsIgnoreCase(ActivityName.trim()))
					throw new WICreateException("5050A",pCodes.getString("5050A"));
				else if("Exit".equalsIgnoreCase(ActivityName.trim()))
					throw new WICreateException("5050B",pCodes.getString("5050B"));
				else
					throw new WICreateException("5050C",pCodes.getString("5050C"));
				
			}
		}
		
		/*if(processID.equalsIgnoreCase("DCC_FIRCO_ADD_DOCS") || processID.equalsIgnoreCase("DCC_FTS_DETAILS") || 
				processID.equalsIgnoreCase("DCC_BS_NO_ACTN") || processID.equalsIgnoreCase("DCC_CARD_DEL_DOC_REC") || 
				processID.equalsIgnoreCase("DCC_COOL_PRD_END") || processID.equalsIgnoreCase("DCC_FIRCO_NO_ACTION") || 
				processID.equalsIgnoreCase("DCC_FTS_STMT_FAIL") || processID.equalsIgnoreCase("DCC_NO_BANK_STMT_ANALYSIS"))*/
		if(processID.equalsIgnoreCase("DCC_FIRCO_ADD_DOCS") || processID.equalsIgnoreCase("DCC_FIRCO_NO_ACTION") || 
			processID.equalsIgnoreCase("DCC_FTS_DETAILS") || processID.equalsIgnoreCase("DCC_NO_BANK_STMT_ANALYSIS") || 
			processID.equalsIgnoreCase("DCC_BS_NO_ACTION")  || processID.equalsIgnoreCase("DCC_END_COOLING") ||
			processID.equalsIgnoreCase("DCC_CARD_DEL_DOC_REC") || processID.equalsIgnoreCase("DCC_SALARY_DOC_REQ") ||
			processID.equalsIgnoreCase("DCC_SALARY_NO_ACTION") || processID.equalsIgnoreCase("DCC_UPDATE_CIF_CARD_DETAILS") ||
			processID.equalsIgnoreCase("DCC_CARD_DETAILS_NO_ACTN"))
		{
			getPossibleUpdateQueuesDCC();
			if (ActivityName.contains(PossibleQueues))
			{
				WriteLog("Update request is possible at this queue: "+ActivityName);
			}
			else 
			{
				WriteLog("Update request is not possible at this queue: "+ActivityName);	
				throw new WICreateException("5050",pCodes.getString("5050"));
			}
		}
		
		//Check if all Mandatory attributes present in USR_0_WSR_UPDATE_ATTRDETAILS have come
		checkMandatoryAttribute();
		
		//Start - Checking conditional Mandatory when customer is authenticated added by Angad on 04122019
		if("RAOP_YAP".equalsIgnoreCase(processID))
		{
			checkConditionalMandatoryAttributeRAOP();
		}
		if(!"CDOB".equalsIgnoreCase(sProcessName) && !"DigitalAO".equalsIgnoreCase(sProcessName)  && !"Digital_CC".equalsIgnoreCase(sProcessName)){
			for(int i=0;i<attributeObj.length;i++)
			{
				checkAttributeTable(attributeObj[i].getName(),attributeObj[i].getValue());
			}
		}
		//********************************************************
		if ("Digital_CC".equalsIgnoreCase(sProcessName)){
			String Attributes = getTagValues(InputMessage, "Attributes");
			String AttributesTmp [] = getTagValues(Attributes, "Attribute").split("`");
			for(int i=0;i<AttributesTmp.length;i++)
			{
				checkAttributeTable_DCC(getTagValues(attributeList[i],"Name"),getTagValues(attributeList[i],"Value"));
			}
		}
		getTableName();
		
		// validating date field format in received Request
		validateInputValuesFunction(attributeList);
		
	}
	private void checkMandatoryAttribute() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryAttribute");
		sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from "+WSR_ATTRDETAILS+" where PROCESSID='"+processID+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String attributeList []=getTagValues(sOutputXML,"ATTRIBUTENAME").split("`");
		if(attributeList.length>0)
		{
			for(int i=0;i<attributeList.length;i++)
			{
				String flag="N";
				//Iterate through the Hash Map
				Set<?> set = hm.entrySet();
			    // Get an iterator
			    Iterator<?> j = set.iterator();
			    // Display elements
			    while(j.hasNext())
			    {
			        Map.Entry me = (Map.Entry)j.next();
			        if(me.getKey().toString().equalsIgnoreCase(attributeList[i]))
			        	flag="Y";
			   
			    }
			    //WriteLog("Value of flag: "+flag);
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+attributeList[i]);
			}
		}
		else
		{
			throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
		}
			
	}
	
	private void checkConditionalMandatoryAttributeRAOP() throws WICreateException, Exception
	{
		WriteLog("inside checkConditionalMandatoryAttributeRAOP");
		sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from "+WSR_ATTRDETAILS+" where PROCESSID='"+processID+"' and ISMANDATORY='C' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String attributeList []=getTagValues(sOutputXML,"ATTRIBUTENAME").split("`");
		
		if (hm.containsKey("CUSTOMERAUTHENTICATED"))
			sCUST_AUTHENTICATION=hm.get("CUSTOMERAUTHENTICATED");
		
		if(sCUST_AUTHENTICATION.trim().equalsIgnoreCase("Y"))
		{
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					String flag="N";
					//Iterate through the Hash Map
					Set<?> set = hm.entrySet();
				    // Get an iterator
				    Iterator<?> j = set.iterator();
				    // Display elements
				    while(j.hasNext())
				    {
				        Map.Entry me = (Map.Entry)j.next();
				        if(me.getKey().toString().equalsIgnoreCase(attributeList[i])){
				        	flag="Y";
				        	break;
				        }
				    }
				    //WriteLog("Value of flag: "+flag);
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+attributeList[i]);
				    
				    String tempValue = "";
				    if (hm.containsKey(attributeList[i]))
						tempValue=hm.get(attributeList[i]);
					
					if (tempValue.trim().equalsIgnoreCase(""))
					{
						throw new WICreateException("1016",pCodes.getString("1016")+": "+attributeList[i]);
					}
					
				}
			}
			
			// Start - Visa validation for GCC Nationalities added on 14022021 by Angad
			String sVISAEXPDATE = "";
			String sVISAFILENUMBER = "";
			String sVISAISSUEDATE = "";
			String sVISAUIDNUMBER = "";
			if (hm.containsKey("VISAEXPDATE"))
				sVISAEXPDATE=hm.get("VISAEXPDATE");
			if (hm.containsKey("VISAFILENUMBER"))
				sVISAFILENUMBER=hm.get("VISAFILENUMBER");
			if (hm.containsKey("VISAISSUEDATE"))
				sVISAISSUEDATE=hm.get("VISAISSUEDATE");
			if (hm.containsKey("VISAUIDNUMBER"))
				sVISAUIDNUMBER=hm.get("VISAUIDNUMBER");
			
			if("".equalsIgnoreCase(sVISAFILENUMBER) || "".equalsIgnoreCase(sVISAUIDNUMBER) || "".equalsIgnoreCase(sVISAISSUEDATE) || "".equalsIgnoreCase(sVISAEXPDATE))
			{	
				String Nationality = getRAOPNationality().trim();
				if(!"".equalsIgnoreCase(Nationality) && !"null".equalsIgnoreCase(Nationality) && Nationality != null
						&& !"AE".equalsIgnoreCase(Nationality) && !"BH".equalsIgnoreCase(Nationality) && !"KW".equalsIgnoreCase(Nationality) && !"OM".equalsIgnoreCase(Nationality) && !"QA".equalsIgnoreCase(Nationality) && !"SA".equalsIgnoreCase(Nationality))
				{
					if("".equalsIgnoreCase(sVISAFILENUMBER))
						throw new WICreateException("5053A",pCodes.getString("5053A"));
					else if("".equalsIgnoreCase(sVISAUIDNUMBER))
						throw new WICreateException("5053B",pCodes.getString("5053B"));	
					else if("".equalsIgnoreCase(sVISAISSUEDATE))
						throw new WICreateException("5053C",pCodes.getString("5053C"));	
					else if("".equalsIgnoreCase(sVISAEXPDATE))
						throw new WICreateException("5053D",pCodes.getString("5053D"));
				}
			}
			// End - Visa validation for GCC Nationalities added on 14022021 by Angad
			
			//Start - Industry SubSegment mandatory for employment type �Self employed� or �A�
			String sEMPLOYMENTTYPE = "";
			if (hm.containsKey("EMPLOYMENTTYPE"))
				sEMPLOYMENTTYPE=hm.get("EMPLOYMENTTYPE");
			if("Self employed".equalsIgnoreCase(sEMPLOYMENTTYPE.trim()) || "A".equalsIgnoreCase(sEMPLOYMENTTYPE.trim()))
			{	
				String sINDUSTRYSUBSEGMENT = "";
				if (hm.containsKey("INDUSTRYSUBSEGMENT"))
					sINDUSTRYSUBSEGMENT=hm.get("INDUSTRYSUBSEGMENT");
				if("".equalsIgnoreCase(sINDUSTRYSUBSEGMENT.trim()))
					throw new WICreateException("5053E",pCodes.getString("5053E"));
			}
			//End - Industry SubSegment mandatory for employment type �Self employed� or �A� 
		}
	}
	
	private void loadResourceBundle()
	{
		WriteLog("inside loadResourceBundle");
		pCodes= PropertyResourceBundle.getBundle("com.newgen.ws.config.StatusCodes");
		if(pCodes==null)
			WriteLog("Error in loading status codes");
		else
			WriteLog("Status Codes loaded successfully");

	}
	 
	private String getAPSelectWithColumnNamesXML(String sQuery)
	{
		
		return 	 "<?xml version='1.0'?>" +
				 "<APSelectWithColumnNames_Input>" +
				 "<Option>APSelectWithColumnNames</Option>" +
				 "<Query>" + sQuery + "</Query>" +
				 "<EngineName>"+sCabinetName+"</EngineName>" +
				 "</APSelectWithColumnNames_Input>";
		
	}
	public static String completeWorkItemInput(String cabName, String sessionID, String workItemName, String WorkItemID){

		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<WMCompleteWorkItem_Input>\n");
		ipXMLBuffer.append("<Option>WMCompleteWorkItem</Option>");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("<ProcessInstanceId>");
		ipXMLBuffer.append(workItemName);
		ipXMLBuffer.append("</ProcessInstanceId>\n");
		ipXMLBuffer.append("<WorkItemId>");
		ipXMLBuffer.append(WorkItemID);
		ipXMLBuffer.append("</WorkItemId>\n");
		ipXMLBuffer.append("<AuditStatus></AuditStatus>\n");
		ipXMLBuffer.append("<Comments></Comments>\n");
		ipXMLBuffer.append("</WMCompleteWorkItem_Input>");

		return ipXMLBuffer.toString();
	}
	
	private String getWMConnectXML()
	{
		
		return "<?xml version=\"1.0\"?>"+
	    "<WMConnect_Input>"+
		"<Option>WMConnect</Option>"+
		"<EngineName>"+sCabinetName+"</EngineName>"+
		"<Participant>"+
		"<Name>"+sUsername+"</Name>"+
		"<Password>"+sPassword+"</Password>"+
		"</Participant>"+
		"</WMConnect_Input>";		
			
	}
	
 
private static String getTagValues (String sXML, String sTagName) 
{  
		String sTagValues = "";
		String sStartTag = "<" + sTagName + ">";
		String sEndTag = "</" + sTagName + ">";
		String tempXML = sXML;
    try	{
	  
		for(int i=0;i<sXML.split(sEndTag).length;i++) 
		{
			if(tempXML.indexOf(sStartTag) != -1) 
			{
				sTagValues += tempXML.substring(tempXML.indexOf(sStartTag) + sStartTag.length(), tempXML.indexOf(sEndTag));
				//System.//out.println("sTagValues"+sTagValues);
				tempXML=tempXML.substring(tempXML.indexOf(sEndTag) + sEndTag.length(), tempXML.length());
	        }
			if(tempXML.indexOf(sStartTag) != -1) 
			{    
				sTagValues +="`";
				//System.//out.println("sTagValues"+sTagValues);
				
			}
			//System.//out.println("sTagValues"+sTagValues);
		}
		//System.//out.println(" Final sTagValues"+sTagValues);
	}

	catch(Exception e) 
	{   
	}
		return sTagValues;
}

private String decryptPassword(String pass)
{
	int len = pass.length();
	byte[] data = new byte[len / 2];
	for (int i = 0; i < len; i += 2) {
		data[i / 2] = (byte) ((Character.digit(pass.charAt(i), 16) << 4)
				+ Character.digit(pass.charAt(i+1), 16));
	}
	String password=OSASecurity.decode(data,"UTF-8");
	return password;
}


private void validateInputValuesFunction(String attributeList[]) throws WICreateException
{
	if (sProcessName.equalsIgnoreCase("RMT"))
	{
		String attrName="";
	    String attrValue="";
	    String find="";
		for(int i=0;i<attributeList.length;i++)
		{
			attrName=getTagValues(attributeList[i],"Name");
			attrValue=getTagValues(attributeList[i],"Value");
			find=validateInputValues(attrName,attrValue);
			if(find.equalsIgnoreCase("false"))
			{
				break;
			}
		}
		//System.out.println("outside "+find+" attrName "+attrName);
		if(find.equalsIgnoreCase("false"))
		{
			
			if(attrName.equalsIgnoreCase("PASSPORTEXPIRYDATE")||
			attrName.equalsIgnoreCase("VISAISSUEDATE")||
			attrName.equalsIgnoreCase("VISAEXPIRYDATE"))
			{
				throw new WICreateException("1005", "Invalid date format for field:" +attrName+" Format should be YYYY-MM-DD format");
			}
		}
	}
}


private String validateInputValues(String attrName,String attrValues)
{
	String found="true";
	
	if((attrName.equalsIgnoreCase("PASSPORTEXPIRYDATE") && !(attrValues.trim().length()==0))
	||(attrName.equalsIgnoreCase("VISAISSUEDATE") && !(attrValues.trim().length()==0))
	||(attrName.equalsIgnoreCase("VISAEXPIRYDATE") && !(attrValues.trim().length()==0)))
	{
		return validateDateFormat(attrValues);
	}
	
	return found;
}

//This function validate the date format in YYYY-MM-DD format
private static String validateDateFormat(String input) 
{
	Date date = null;
	try 
	{
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    date = sdf.parse(input);
	    if (!input.equals(sdf.format(date))) 
	    {
	        date = null;
	    }
	} 
	catch (ParseException ex) 
	{
     return "false";
	}
	if (date == null) 
	{
		return "false";
	} 
	else 
	{
		return "true";
	}	
}

//Added by Sajan for FALCON CDOB
private String getBankDescFromCode(){
	String strBankDesc="";
	try{
	
	sInputXML = getAPSelectWithColumnNamesXML("select top 1 Description from NG_MASTER_BankName with (nolock) where Code='"+hm.get("BankName")+"'");
	WriteLog("Get Bank desc xml: " + sInputXML);
	sOutputXML = executeAPI(sInputXML);
	WriteLog("Get Bank desc Output xml: " + sOutputXML);
	xmlobj = new XMLParser(sOutputXML);
	// Check Main Code
	checkCallsMainCode(xmlobj);
	strBankDesc = getTagValues(sOutputXML, "Description");
	WriteLog("Bank Name is : " + strBankDesc);
	
	}
	catch(WICreateException ex){
		WriteLog("WICreate exception thrown in getBankCodeFromDesc "+ex.getMessage());
	}
	catch(Exception e){
		WriteLog("Generic exception in getBankDescFromCode "+e.getMessage());
	}
	return strBankDesc;
}

	private void doneworkitem() throws WICreateException {
		try {
			// Lock Workitem.
			String getWorkItemInputXML = getWorkItemInput();
			String getWorkItemOutputXml = executeAPI(getWorkItemInputXML);
			WriteLog("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			WriteLog("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0")) {
				WriteLog("WMgetWorkItemCall Successful: " + getWorkItemMainCode);

				// Move Workitem to next Workstep
				String completeWorkItemInputXML = completeWorkItemInput();
				WriteLog("Input XML for wmcompleteWorkItem: " + completeWorkItemInputXML);

				String completeWorkItemOutputXML = executeAPI(completeWorkItemInputXML);
				WriteLog("Output XML for wmcompleteWorkItem: " + completeWorkItemOutputXML);

				XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
				String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
				WriteLog("Status of wmcompleteWorkItem  " + completeWorkitemMaincode);

				if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) {
					WriteLog("WmCompleteWorkItem successful: " + completeWorkitemMaincode);
					System.out.println(WINumber + "Complete Sussesfully with status ");
					WriteLog("WorkItem moved to next Workstep.");

				} else {
					completeWorkitemMaincode = "";
					WriteLog("WMCompleteWorkItem failed: " + completeWorkitemMaincode);
				}

			} else {
				getWorkItemMainCode = "";
				WriteLog("WmgetWorkItem failed: " + getWorkItemMainCode);
			}

		} catch (Exception e) {
			WriteLog("Exception: " + e.getMessage());
			throw new WICreateException("1007", pCodes.getString("1007") + " : " + e.getMessage());
		}
	}



	private void doneworkitemDCC() throws WICreateException {
		try {
			// Lock Workitem.
			String getWorkItemInputXML = getWorkItemInputDCC();
			String getWorkItemOutputXml = executeAPI(getWorkItemInputXML);
			WriteLog("Output XML For WmgetWorkItemCall: " + getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			WriteLog("WmgetWorkItemCall Maincode:  " + getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0")) {
				WriteLog("WMgetWorkItemCall Successful: " + getWorkItemMainCode);

				// Move Workitem to next Workstep
				String completeWorkItemInputXML = completeWorkItemInputDCC();
				WriteLog("Input XML for wmcompleteWorkItem: " + completeWorkItemInputXML);

				String completeWorkItemOutputXML = executeAPI(completeWorkItemInputXML);
				WriteLog("Output XML for wmcompleteWorkItem: " + completeWorkItemOutputXML);

				XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
				String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
				WriteLog("Status of wmcompleteWorkItem  " + completeWorkitemMaincode);

				if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) {
					WriteLog("WmCompleteWorkItem successful: " + completeWorkitemMaincode);
					System.out.println(WINumber + "Complete Sussesfully with status ");
					WriteLog("WorkItem moved to next Workstep.");

				} else {
					completeWorkitemMaincode = "";
					WriteLog("WMCompleteWorkItem failed: " + completeWorkitemMaincode);
				}

			} else {
				getWorkItemMainCode = "";
				WriteLog("WmgetWorkItem failed: " + getWorkItemMainCode);
			}

		} catch (Exception e) {
			WriteLog("Exception: " + e.getMessage());
			throw new WICreateException("1007", pCodes.getString("1007") + " : " + e.getMessage());
		}
	}


//Kamran 09062022 - For Done Workitem for BSR

private void doneworkitemForBSR()throws WICreateException
{
	
	try
	{
	//Lock Workitem.
	String getWorkItemInputXML = getWorkItemInputforBSR();
	
//	WriteLog("ooooooooooooooooooooooooo"+getWorkItemInputXML);
	String getWorkItemOutputXml = executeAPI(getWorkItemInputXML);
	WriteLog("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

	XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
	String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
	WriteLog("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

	if (getWorkItemMainCode.trim().equals("0")) 
	{
		WriteLog("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
							
			//Move Workitem to next Workstep 
			String completeWorkItemInputXML = completeWorkItemInputForBSR();
			WriteLog("Input XML for wmcompleteWorkItem: "+ completeWorkItemInputXML);

			String completeWorkItemOutputXML = executeAPI(completeWorkItemInputXML);
			WriteLog("Output XML for wmcompleteWorkItem: "+ completeWorkItemOutputXML);

			XMLParser xmlParserCompleteWorkitem = new XMLParser(completeWorkItemOutputXML);
			String completeWorkitemMaincode = xmlParserCompleteWorkitem.getValueOf("MainCode");
			WriteLog("Status of wmcompleteWorkItem  "+ completeWorkitemMaincode);

			if (completeWorkitemMaincode.trim().equalsIgnoreCase("0")) 
			{
				WriteLog("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
				System.out.println(WINumber + "Complete Sussesfully with status ");
				WriteLog("WorkItem moved to next Workstep.");

			}
			else 
			{
				completeWorkitemMaincode="";
				WriteLog("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
			}
		
		
	}
	else
	{
		getWorkItemMainCode="";
		WriteLog("WmgetWorkItem failed: "+getWorkItemMainCode);
	}
	
	}
	catch(Exception e)
	{
		WriteLog("Exception: "+e.getMessage());
		throw new WICreateException("1007",pCodes.getString("1007")+" : "+e.getMessage());
	}
}

	public static String getNGOAddDocument(String parentFolderIndex, String strDocumentName,String DocumentType,String strExtension,
			String sISIndex,String lstrDocFileSize, int iVolId2, String cabinetName, String sessionId)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();
	
		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<NGOAddDocument_Input>\n");
		ipXMLBuffer.append("<Option>NGOAddDocument</Option>");
		ipXMLBuffer.append("<CabinetName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</CabinetName>\n");
		ipXMLBuffer.append("<UserDBId>");
		ipXMLBuffer.append(sessionId);
		ipXMLBuffer.append("</UserDBId>\n");
		ipXMLBuffer.append("<GroupIndex>0</GroupIndex>\n");
		ipXMLBuffer.append("<Document>\n");
		ipXMLBuffer.append("<VersionFlag>Y</VersionFlag>\n");
		ipXMLBuffer.append("<ParentFolderIndex>");
		ipXMLBuffer.append(parentFolderIndex);
		ipXMLBuffer.append("</ParentFolderIndex>\n");
		ipXMLBuffer.append("<DocumentName>");
		ipXMLBuffer.append(strDocumentName);
		ipXMLBuffer.append("</DocumentName>\n");
		ipXMLBuffer.append("<VolumeIndex>");
		ipXMLBuffer.append(iVolId2);
		ipXMLBuffer.append("</VolumeIndex>\n");
		ipXMLBuffer.append("<ISIndex>");
		ipXMLBuffer.append(sISIndex);
		ipXMLBuffer.append("</ISIndex>\n");
		ipXMLBuffer.append("<NoOfPages>1</NoOfPages>\n");
		ipXMLBuffer.append("<DocumentType>");
		ipXMLBuffer.append(DocumentType);
		ipXMLBuffer.append("</DocumentType>\n");
		ipXMLBuffer.append("<DocumentSize>");
		ipXMLBuffer.append(lstrDocFileSize);
		ipXMLBuffer.append("</DocumentSize>\n");
		ipXMLBuffer.append("<CreatedByAppName>");
		ipXMLBuffer.append(strExtension);
		ipXMLBuffer.append("</CreatedByAppName>\n");
		ipXMLBuffer.append("</Document>\n");
		ipXMLBuffer.append("</NGOAddDocument_Input>\n");
		return ipXMLBuffer.toString();
	}
public  String getWorkItemInputforBSR()
{
	WINumber=hm.get("WI_NAME");
	return "<?xml version=\"1.0\"?>" + "<WMGetWorkItem_Input>"
			+ "<Option>WMGetWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
			+ "</ProcessInstanceId>" + "<WorkItemId>" + "1" + "</WorkItemId>"
			+ "<EngineName>"
			+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
			+ "</SessionId>" + "</WMGetWorkItem_Input>";
}	

public  String completeWorkItemInputForBSR()
{
	WINumber=hm.get("WI_NAME");
	return "<?xml version=\"1.0\"?>" + "<WMCompleteWorkItem_Input>"
			+ "<Option>WMCompleteWorkItem</Option>" + "<ProcessInstanceId>" + WINumber
			+ "</ProcessInstanceId>" + "<WorkItemId>" + "1" + "</WorkItemId>"
			+ "<AuditStatus></AuditStatus>"
			+ "<Comments></Comments>"
			+ "<EngineName>"
			+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
			+ "</SessionId>" + "</WMCompleteWorkItem_Input>";
}
  
}

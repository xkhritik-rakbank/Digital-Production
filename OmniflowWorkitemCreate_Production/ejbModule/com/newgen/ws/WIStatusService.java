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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.lang.String;

 
import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;


import adminclient.OSASecurity;

import com.newgen.custom.CreateWorkitem;
import com.newgen.ws.exception.WICreateException;
import com.newgen.ws.request.Attribute;
import com.newgen.ws.request.Attributes;
import com.newgen.ws.request.Document;
import com.newgen.ws.request.Documents;
import com.newgen.ws.request.WICreateRequest;

import com.newgen.ws.response.WICreateResponse;
import com.newgen.ws.srvr.AesUtil;
 
import com.newgen.ws.util.XMLParser;
//import com.sun.xml.internal.ws.util.StringUtils;

import java.util.HashMap;
import java.util.regex.Pattern;


public class WIStatusService extends CreateWorkitem
{
	
	
	//CONSTANTS
	final private String SUCCESS_STATUS="0";
	//TABLE NAME CONSTANTS
	final private String WSR_PROCESS="USR_0_WSR_STATUS_PROCESS";
	final private String WSR_ATTRDETAILS="USR_0_WSR_STATUS_ATTRDETAILS";
	final private String WSR_QUEUESTATUSDETAILS="USR_0_WSR_STATUS_QUEUESTATUSDETAILS";
	
	// For RMT
	final private String RMT_WIHISTORY = "USR_0_RMT_WIHISTORY";
	//For RAOP
	final private String RAOP_WIHISTORY = "USR_0_RAOP_WIHISTORY";
	
	//For CDOB
	final private String CC_EXTTABLE="NG_CC_EXTTABLE";
	final private String Customer_Fragment_Table="ng_rlos_Customer";
	private String strCodes;
	

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
	
	//Configuration Parameters
	private String sCabinetName = "";
	private String sJtsIp = "";
	private int iJtsPort;
	private String sUsername = "";
	private String sPassword = "";
	private String sTempLoc = "";
	private int iVolId;
	private String sCabinetNameOF = "";
	private String sJtsIpOF = "";
	private int iJtsPortOF;
	private String sUsernameOF = "";
	private String sPasswordOF = "";
	private String sTempLocOF = "";
	private int iVolIdOF;
	//Response Parameters
	String eCode = "";
	String eDesc = ""; 
  	//Other vriables/objects
	WICreateResponse response = new WICreateResponse();	
	EE_EAI_HEADER headerObjResponse;
	private ResourceBundle pCodes;
	private String sSessionID = "";
	HashMap<String, String> hm = new HashMap();
	HashMap<String, String> hmQueueStatusCode = new HashMap();
	HashMap<String, String> hmQueueStatusDesc = new HashMap();
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
	private String DecisionReasonCode="";
	
	private String WINumber = "";
	private String ActivityName="";
	private String WorkitemStatusDetails="";
			 
	public WICreateResponse wiStatus(WICreateRequest request,String attributeList[]) throws WICreateException { 
		
			try
			{
				WriteLog("inside wiStatus 1");
				headerObjResponse=new EE_EAI_HEADER();
				//initialize Logger
				//initializeLogger();	
				
				//Load file configuration
				loadConfiguration();
				
				//Load ResourceBundle
				loadResourceBundle();
				
				//Fetching request parameters 
				fetchRequestParameters(request);
									
				//Validating Input Parameters and Check Attribute Name from USR_0_WSR_STATUS_ATTRDETAILS
				validateRequestParameters(attributeList);
				
				//Function for dedupe in FALCON CDOB //Deepak new condition added for Digital_CC process in April 2022
				if("CDOB".equalsIgnoreCase(sProcessName)||"Digital_CC".equalsIgnoreCase(sProcessName)){
					ifCustomerExistsInSystem();
				}
					
				//Checking existing session
				checkExistingSession();
				
				// returning success response
            	returnResponse();
		 			                		
          	}
			catch(WICreateException e)
			{
				WriteLog("WICreateException caught:Code- "+e.getErrorCode());
				WriteLog("WICreateException caught:Description "+e.getErrorDesc());
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
				hmQueueStatusCode.clear();
				hmQueueStatusDesc.clear();
			}
			return response;
		
	}
	private void returnResponse() throws WICreateException,Exception
	{
		wiName = WINumber;
		setSuccessParamResponse();
		response.setWorkitemNumber(wiName);
		response.setWorkitemStatusDetails(WorkitemStatusDetails);	
		
	}
	
	//Function added by Sajan for FALCON CDOB dedupe check
	private void ifCustomerExistsInSystem() throws WICreateException,Exception{
		if("Case_Dedupe".equalsIgnoreCase(sSubProcess) && "CDOB".equalsIgnoreCase(sProcessName)){
			//checkMandatoryAttributesForDedupe();
			
			String sQuery="select count(a.wi_name) as Count from (select wi_name from ng_RLOS_Customer with (nolock) where EmirateID='"+hm.get("EmiratesID")+"' or " +
					"(Nationality='"+hm.get("Nationality")+"' and cast(dob as date)='"+hm.get("dob")+"' and FirstName like '"+hm.get("FirstName").charAt(0)+"%' and LAstName='"+hm.get("LastName")+"')) a " +
					"INNER JOIN" +
					"(select CC_Wi_Name from NG_CC_EXTTABLE with(nolock) union all select CC_Wi_Name from NG_DOB_EXTTABLE with (nolock)) ext " +
					"ON a.wi_name=ext.CC_Wi_Name " +
					"INNER JOIN" +
					"(select ProcessInstanceID from WFINSTRUMENTTABLE with (nolock) where ActivityName!='Exit' and ActivityName!='Rejected_Application') wf " +
					"on a.wi_name=wf.ProcessInstanceID";
				
			sInputXML=getAPSelectWithColumnNamesXML(sQuery);
			WriteLog("Input: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			//Check Main Code
			checkCallsMainCode(xmlobj); 
			String strCount=xmlobj.getValueOf("Count");
			//WriteLog("Workitem number: "+WINAME);
			WriteLog("Message id of the request"+sMessageId);
			
			if(Integer.parseInt(strCount)==0){
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				WorkitemStatusDetails="~"+sdf.format(cal.getTime())+"~~6001~No Workitem Found";
			}
			else{
				sQuery="select a.wi_name as CC_Wi_Name,cast(wf.Createddatetime as date) as Created_Date,wf.ActivityName as CURR_WSNAME  from (select wi_name from ng_RLOS_Customer with (nolock) where EmirateID='"+hm.get("EmiratesID")+"' or " +
					"(Nationality='"+hm.get("Nationality")+"' and cast(dob as date)='"+hm.get("dob")+"' and FirstName like '"+hm.get("FirstName").charAt(0)+"%' and LAstName='"+hm.get("LastName")+"')) a " +
					"INNER JOIN " +
					"(select CC_Wi_Name from NG_CC_EXTTABLE with(nolock) union all select CC_Wi_Name from NG_DOB_EXTTABLE with (nolock)) ext" +
					" ON a.wi_name=ext.CC_Wi_Name " +
					"INNER JOIN" +
					"(select ProcessInstanceID,Createddatetime,ActivityName from WFINSTRUMENTTABLE with (nolock) where ActivityName!='Exit' " +
					"and ActivityName!='Rejected_Application') wf on a.wi_name=wf.ProcessInstanceID";
			
				sInputXML=getAPSelectWithColumnNamesXML(sQuery);
				WriteLog("Input: "+sInputXML);
				sOutputXML=executeAPI(sInputXML);
				WriteLog("Output: "+sOutputXML);
				xmlobj=new XMLParser(sOutputXML);
				//Check Main Code
				checkCallsMainCode(xmlobj); 
				/*String strWiName=xmlobj.getValueOf("CC_Wi_Name");
				String strCurrWS=xmlobj.getValueOf("CURR_WSNAME");
				String strCreatedDate=xmlobj.getValueOf("Created_Date");
				WorkitemStatusDetails=strWiName+"~"+strCreatedDate+"~"+strCurrWS+"~ ~ ~";*/
				
				int TotalRetrieved=Integer.parseInt(getTagValues(sOutputXML, "TotalRetrieved"));
				
				if (TotalRetrieved > 0) 
				{
					for (int i = 0; i < TotalRetrieved; i++)
					{
						String subXML= xmlobj.getNextValueOf("Record");
						XMLParser objXmlParser = new XMLParser(subXML);
						if("".equals(WorkitemStatusDetails))
							WorkitemStatusDetails=objXmlParser.getValueOf("CC_Wi_Name")+"~"+objXmlParser.getValueOf("Created_Date")+"~"+objXmlParser.getValueOf("CURR_WSNAME")+"~ ~ ~";
						else
							WorkitemStatusDetails=WorkitemStatusDetails+"~,~"+objXmlParser.getValueOf("CC_Wi_Name")+"~"+objXmlParser.getValueOf("Created_Date")+"~"+objXmlParser.getValueOf("CURR_WSNAME")+"~ ~ ~";
					}
				}
			}
		}
		//new condition added by Deepak for Digital_CC process case Dedupe April 2022
		else if("Case_Dedupe".equalsIgnoreCase(sSubProcess) && "Digital_CC".equalsIgnoreCase(sProcessName)){
			//checkMandatoryAttributesForDedupe();
			
			String sQuery="select count(a.wi_name) as Count from (select wi_name from ng_RLOS_Customer with (nolock) where EmirateID='"+hm.get("EmiratesID")+"') a " +
					"INNER JOIN" +
					"(select CC_Wi_Name from NG_CC_EXTTABLE with(nolock) union all select CC_Wi_Name from NG_DOB_EXTTABLE with (nolock) union all select WIname as CC_Wi_Name from NG_RLOS_EXTTABLE with (nolock) where Decision is null) ext " +
					"ON a.wi_name=ext.CC_Wi_Name " +
					"INNER JOIN" +
					"(select ProcessInstanceID from WFINSTRUMENTTABLE with (nolock) where ActivityName!='Exit' and ActivityName!='Rejected_Application' and ActivityName!='Rejected_queue') wf " +
					"on a.wi_name=wf.ProcessInstanceID";
				
			sInputXML=getAPSelectWithColumnNamesXML(sQuery);
			WriteLog("Input: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			//Check Main Code
			checkCallsMainCode(xmlobj); 
			String strCount=xmlobj.getValueOf("Count");
			//WriteLog("Workitem number: "+WINAME);
			WriteLog("Message id of the request"+sMessageId);
			
			if(Integer.parseInt(strCount)==0){
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				WorkitemStatusDetails="~"+sdf.format(cal.getTime())+"~~6001~No Workitem Found";
			}
			else{
				sQuery="select a.wi_name as CC_Wi_Name,cast(wf.Createddatetime as date) as Created_Date,wf.ActivityName as CURR_WSNAME  from (select wi_name from ng_RLOS_Customer with (nolock) where EmirateID='"+hm.get("EmiratesID")+"') a " +
					"INNER JOIN " +
					"(select CC_Wi_Name from NG_CC_EXTTABLE with(nolock) union all select CC_Wi_Name from NG_DOB_EXTTABLE with (nolock) union all select WIname as CC_Wi_Name from NG_RLOS_EXTTABLE with (nolock) where Decision is null) ext" +
					" ON a.wi_name=ext.CC_Wi_Name " +
					"INNER JOIN" +
					"(select ProcessInstanceID,Createddatetime,ActivityName from WFINSTRUMENTTABLE with (nolock) where ActivityName!='Exit' " +
					"and ActivityName!='Rejected_Application' and ActivityName!='Rejected_queue') wf on a.wi_name=wf.ProcessInstanceID";
			
				sInputXML=getAPSelectWithColumnNamesXML(sQuery);
				WriteLog("Input: "+sInputXML);
				sOutputXML=executeAPI(sInputXML);
				WriteLog("Output: "+sOutputXML);
				xmlobj=new XMLParser(sOutputXML);
				//Check Main Code
				checkCallsMainCode(xmlobj); 
				/*String strWiName=xmlobj.getValueOf("CC_Wi_Name");
				String strCurrWS=xmlobj.getValueOf("CURR_WSNAME");
				String strCreatedDate=xmlobj.getValueOf("Created_Date");
				WorkitemStatusDetails=strWiName+"~"+strCreatedDate+"~"+strCurrWS+"~ ~ ~";*/
				
				int TotalRetrieved=Integer.parseInt(getTagValues(sOutputXML, "TotalRetrieved"));
				
				if (TotalRetrieved > 0) 
				{
					for (int i = 0; i < TotalRetrieved; i++)
					{
						String subXML= xmlobj.getNextValueOf("Record");
						XMLParser objXmlParser = new XMLParser(subXML);
						if("".equals(WorkitemStatusDetails))
							WorkitemStatusDetails=objXmlParser.getValueOf("CC_Wi_Name")+"~"+objXmlParser.getValueOf("Created_Date")+"~"+objXmlParser.getValueOf("CURR_WSNAME")+"~ ~ ~";
						else
							WorkitemStatusDetails=WorkitemStatusDetails+"~,~"+objXmlParser.getValueOf("CC_Wi_Name")+"~"+objXmlParser.getValueOf("Created_Date")+"~"+objXmlParser.getValueOf("CURR_WSNAME")+"~ ~ ~";
					//temp change
						/*if("".equals(WorkitemStatusDetails))
							WorkitemStatusDetails=objXmlParser.getValueOf("CC_Wi_Name")+"~"+objXmlParser.getValueOf("Created_Date")+"~"+objXmlParser.getValueOf("CURR_WSNAME")+"~ ~ ~";
						else
							WorkitemStatusDetails=WorkitemStatusDetails+"~,~"+objXmlParser.getValueOf("CC_Wi_Name")+"~"+objXmlParser.getValueOf("Created_Date")+"~"+objXmlParser.getValueOf("CURR_WSNAME")+"~ ~ ~";
						*/
					}
				}
			}
		}
	}
	
	//Added by Sajan for FALCON CDOB
	private void checkMandatoryAttributesForDedupe() throws WICreateException,Exception{
		
		String[] arrMandatoryForDedupe={"FirstName","LastName","dob","Nationality","PAssportNo","EmiratesID"};
		for(int i=0;i<arrMandatoryForDedupe.length;i++){
			if(!hm.containsKey(arrMandatoryForDedupe[i]) || "".equals(hm.get(arrMandatoryForDedupe[i]))){
				throw new WICreateException("6011",pCodes.getString("6011"));
			}
		}
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
	
	private String getDBInsertInputHistoryRAOP() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + RAOP_WIHISTORY
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
		String sConfigFile = new StringBuilder().append(System.getProperty("user.dir"))
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
	// Added by nikhil to accomodate inquiry in ibos and OF 19-08-2022
	
	private void loadConfiguration_OF() throws IOException, Exception
	{
		//load file configuration
		WriteLog("Loading Configuration omniflow file");
		Properties p = new Properties();
		String sConfigFile = new StringBuilder().append(System.getProperty("user.dir"))
				.append(System.getProperty("file.separator")).append("BPMCustomWebservicesConf")
				.append(System.getProperty("file.separator")).append("configOF.properties").toString();
			
		p.load(new FileInputStream(sConfigFile));
	    sCabinetNameOF=p.getProperty("CabinetName");
	    sJtsIpOF=p.getProperty("JtsIp");
	    iJtsPortOF=Integer.parseInt(p.getProperty("JtsPort"));
	    sUsernameOF=p.getProperty("username");
		try
		{
			//WriteLog("sPassword::"+sPassword);
			sPasswordOF=decryptPassword(p.getProperty("password"));	
			//WriteLog("after decrption::"+sPassword);
		}
		catch(Exception e)
		{
			WriteLog("Inside exception of log omniflow decryption");
			throw new WICreateException("3001",pCodes.getString("3001"));
		}
	    sTempLocOF=p.getProperty("TempDocumentLoc");
	    iVolIdOF=Integer.parseInt(p.getProperty("volid"));
	    WriteLog("CabinetNameOF: "+sCabinetNameOF+", JtsIpOF: "+sJtsIpOF+", JtsPortOF: "+iJtsPortOF+" ,UsernameOF: "+sUsernameOF+", PasswordOF: "+p.getProperty("password")+" ,VolumeIDOF: "+iVolIdOF);
	    WriteLog("Configuration OF file loaded successfuly");
	    
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
	private String executeAPI_in_OF(String sInputXML) throws WICreateException
	{
		String sOutputXML="";
		try
		{
			 Socket sock = null;
			 sock = new Socket(sJtsIpOF, iJtsPortOF);
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
		getExtTransQry="select ATTRIBUTENAME,EXTERNALTABLECOLNAME, TRANSACTIONTABLECOLNAME, ISMANDATORY from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME='"+attributeName+"' and PROCESSID='"+processID+"'";
			
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
		
		if(!attNameFromTable.equalsIgnoreCase(""))
		{
			WriteLog("Attribute Name: "+attributeName);
			if(((isMandatory.equalsIgnoreCase("Y") || isMandatory.equalsIgnoreCase("N")) && !attributeValue.equalsIgnoreCase("")) || (isMandatory.equalsIgnoreCase("N") && attributeValue.equalsIgnoreCase("")))
			{
				if (!extFlag.equalsIgnoreCase("") && transFlag.equalsIgnoreCase(""))
				{
					if (attributeValue.contains("'"))
						attributeValue = attributeValue.replace("'", "");
					
					/*if(sProcessName.equalsIgnoreCase("RMT"))
					{
						if (extFlag.equalsIgnoreCase("UID_UPDATE")) // for RMT Process
							sUID_UPDATE = attributeValue;
					}*/
					
					//added for RAOP Process
					/*if(sProcessName.equalsIgnoreCase("RAOP"))
					{	
						WriteLog("sCUST_AUTHENTICATION---"+attributeValue);
						if (extFlag.equalsIgnoreCase("CUSTOMERAUTHENTICATED")) // For RAOP Process
							sCUST_AUTHENTICATION = attributeValue;
						else if(extFlag.equalsIgnoreCase("YAP_CUST_AUTHENTICATED"))
							sCUST_AUTHENTICATION = attributeValue;
							WriteLog("sCUST_AUTHENTICATION---"+sCUST_AUTHENTICATION);
					}*/
					
					attributeTag=attributeTag+extFlag+(char)21+attributeValue+(char)25;
					extColNames=extColNames+extFlag+",";
					extColValues=extColValues+"'"+attributeValue+"'"+",";
				}
				/*else
					 throw new WICreateException("1015",pCodes.getString("1015")+" :"+attributeName);*/
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
	private void getStatusCodeDesc() throws WICreateException,Exception{
		String getTableNameQry="select top 1 DecisionReasonCode from NG_RLOS_GR_DECISION with (nolock) where dec_wi_name='"+hm.get("WINumber")+"' order by dateLastChanged desc";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		DecisionReasonCode=getTagValues(sOutputXML, "DecisionReasonCode");
		WriteLog("Decision reason code is : "+DecisionReasonCode);
		//return DecisionReasonCode;
		DecisionReasonCode=DecisionReasonCode.replace(";", ",");
		String[] decisionReasons=DecisionReasonCode.split(",");
		strCodes="";
		for(int i=0;i<decisionReasons.length;i++){
			getTableNameQry="select STATUS_CODE from USR_0_WSR_STATUS_ENQUIRE_CODES with (nolock) where status_desc='"+decisionReasons[i]+"' and isActive='Y'";
			sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
			WriteLog("Input XML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			if(strCodes!="")
				strCodes=strCodes+","+getTagValues(sOutputXML, "STATUS_CODE");
			else
				strCodes=getTagValues(sOutputXML, "STATUS_CODE");
			WriteLog("Decision reason code is : "+DecisionReasonCode);
		}
		
	}
	private void EnquireReject_app() throws WICreateException,Exception{
		String getTableNameQry="select top 1 Decision from NG_RLOS_GR_DECISION with (nolock) where dec_wi_name='"+hm.get("WINumber")+"' order by dateLastChanged desc";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		if("Reject".equalsIgnoreCase(getTagValues(sOutputXML, "Decision"))){
			DecisionReasonCode="Reject";
			strCodes="Reject";
		}
		else{
			DecisionReasonCode="Exit";
			strCodes="Exit";
		}
	}
	private void getWorkitemDataBasedOnWINumber(String attributeList[]) throws WICreateException, Exception
	{
		//from tables here
		String attrName="";
	    String attrValue="";
		for(int i=0;i<attributeList.length;i++)
    	{
    		attrName=getTagValues(attributeList[i],"Name");
    		attrValue=getTagValues(attributeList[i],"Value");
    		if (attrName.equalsIgnoreCase("WorkitemNumber") || "WINumber".equalsIgnoreCase(attrName))
    		{
    			WINumber="";
    			WINumber = attrValue;
    		}
    	}
		response.setWorkitemNumber(WINumber);
		if(!WINumber.equalsIgnoreCase(""))
		{	
			String getTableNameQry="select TOP 1 ACTIVITYNAME, convert(varchar(10),IntroductionDateTime,121) as IntroductionDateTime from QUEUEVIEW with (nolock) where PROCESSINSTANCEID='"+WINumber+"' and activityname != 'Distribute' and activityname != 'Sys_CPF_Response' order by EntryDateTime desc";
			sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
			WriteLog("Input XML to get workitem details based on workitem number: "+sInputXML);
			String sOutputXML1=executeAPI(sInputXML);
			WriteLog("Output XML to Get workitem details based on workitem number: "+sOutputXML1);
			xmlobj=new XMLParser(sOutputXML1);
			checkCallsMainCode(xmlobj);
			int TotalRetrieved=Integer.parseInt(getTagValues(sOutputXML1, "TotalRetrieved"));
			if (TotalRetrieved > 0) 
			{
				for (int i = 0; i < TotalRetrieved; i++)
				{
					if(WorkitemStatusDetails.equalsIgnoreCase("")){
						if("CDOB".equalsIgnoreCase(sProcessName) && "Customer_Hold".equalsIgnoreCase(getTagValues(sOutputXML1, "ACTIVITYNAME"))){
							getStatusCodeDesc();
							WorkitemStatusDetails=WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~cust_hold~"+strCodes+"~End";
						}/* //Not available in the base code received from angad.
						else if("CDOB".equalsIgnoreCase(sProcessName) && "EXIT".equalsIgnoreCase(getTagValues(sOutputXML1, "ACTIVITYNAME"))){
							EnquireReject_app();
							WorkitemStatusDetails=WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~"+DecisionReasonCode+"~"+strCodes+"~End";
						}*/
						else{
							WorkitemStatusDetails=WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+getTagValues(sOutputXML1, "ACTIVITYNAME")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~"+hmQueueStatusDesc.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~End";
						}
						
					}
					else
						WorkitemStatusDetails=WorkitemStatusDetails+"~,~"+WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+getTagValues(sOutputXML1, "ACTIVITYNAME")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~"+hmQueueStatusDesc.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~End";
				}
			}
		}
	}
	//added by nikhil to accomodate inquiry in both ibps and Of 19-08-22
	private void getWorkitemDataBasedOnWINumberOF(String attributeList[]) throws WICreateException, Exception
	{
		//from tables here
		String attrName="";
	    String attrValue="";
		for(int i=0;i<attributeList.length;i++)
    	{
    		attrName=getTagValues(attributeList[i],"Name");
    		attrValue=getTagValues(attributeList[i],"Value");
    		if (attrName.equalsIgnoreCase("WorkitemNumber") || "WINumber".equalsIgnoreCase(attrName))
    		{
    			WINumber="";
    			WINumber = attrValue;
    		}
    	}
		response.setWorkitemNumber(WINumber);
		if(!WINumber.equalsIgnoreCase(""))
		{	
			String getTableNameQry="select TOP 1 ACTIVITYNAME, convert(varchar(10),IntroductionDateTime,121) as IntroductionDateTime from QUEUEVIEW with (nolock) where PROCESSINSTANCEID='"+WINumber+"' and activityname != 'Distribute' and activityname != 'Sys_CPF_Response' order by EntryDateTime desc";
			sInputXML=getAPSelectWithColumnNamesXMLOF(getTableNameQry);
			WriteLog("Input XML to get workitem details based on workitem number: "+sInputXML);
			String sOutputXML1=executeAPI_in_OF(sInputXML);
			WriteLog("Output XML to Get workitem details based on workitem number: "+sOutputXML1);
			xmlobj=new XMLParser(sOutputXML1);
			checkCallsMainCode(xmlobj);
			int TotalRetrieved=Integer.parseInt(getTagValues(sOutputXML1, "TotalRetrieved"));
			if (TotalRetrieved > 0) 
			{
				for (int i = 0; i < TotalRetrieved; i++)
				{
					if(WorkitemStatusDetails.equalsIgnoreCase("")){
						if("CDOB".equalsIgnoreCase(sProcessName) && "Customer_Hold".equalsIgnoreCase(getTagValues(sOutputXML1, "ACTIVITYNAME"))){
							getStatusCodeDesc();
							WorkitemStatusDetails=WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~cust_hold~"+strCodes+"~End";
						}/* //Not available in the base code received from angad.
						else if("CDOB".equalsIgnoreCase(sProcessName) && "EXIT".equalsIgnoreCase(getTagValues(sOutputXML1, "ACTIVITYNAME"))){
							EnquireReject_app();
							WorkitemStatusDetails=WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~"+DecisionReasonCode+"~"+strCodes+"~End";
						}*/
						else{
							WorkitemStatusDetails=WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+getTagValues(sOutputXML1, "ACTIVITYNAME")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~"+hmQueueStatusDesc.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~End";
						}
						
					}
					else
						WorkitemStatusDetails=WorkitemStatusDetails+"~,~"+WINumber+"~"+getTagValues(sOutputXML1, "IntroductionDateTime")+"~"+getTagValues(sOutputXML1, "ACTIVITYNAME")+"~"+hmQueueStatusCode.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~"+hmQueueStatusDesc.get(getTagValues(sOutputXML1, "ACTIVITYNAME"))+"~End";
				}
			}
		}
	}
	
	private void getWorkitemDataBasedOnDateRange(String attributeList[]) throws WICreateException, Exception
	{
		//from tables here
		String attrName="";
	    String attrValue="";
	    String fromDate = "";
	    String toDate = "";
		for(int i=0;i<attributeList.length;i++)
    	{
    		attrName=getTagValues(attributeList[i],"Name");
    		attrValue=getTagValues(attributeList[i],"Value");
    		if (attrName.equalsIgnoreCase("FromDate"))
    			fromDate = attrValue;
    		
    		if (attrName.equalsIgnoreCase("ToDate"))
    			toDate = attrValue;
    		
    	}
		
		if(!fromDate.equalsIgnoreCase("") && !toDate.equalsIgnoreCase(""))
		{	
			//Getting list of workitem introduced between date range.
			String getTableNameQry="select ACTIVITYNAME, convert(varchar(10),IntroductionDateTime,121) as IntroductionDateTime, PROCESSINSTANCEID from QUEUEVIEW with (nolock) where PROCESSINSTANCEID in (select distinct processinstanceid from QUEUEVIEW with (nolock) where ProcessName = '"+sProcessName+"' and CONVERT(DATE, IntroductionDateTime, 102)  between CONVERT(DATE, '"+fromDate+"' , 102) and CONVERT(DATE, '"+toDate+"' , 102)) and activityname != 'Distribute' order by PROCESSINSTANCEID asc, EntryDateTime desc";
			sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
			WriteLog("Input XML to Get Workitem Count: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML to Get Workitem Count: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			int TotalRetrieved=Integer.parseInt(getTagValues(sOutputXML, "TotalRetrieved"));
			WriteLog("Workitem Count: list of workitem introduced between date range: "+TotalRetrieved);
			//*************************************************
			
			if (TotalRetrieved > 0) 
			{
				for (int i = 0; i < TotalRetrieved; i++)
				{
					String subXML= xmlobj.getNextValueOf("Record");
					XMLParser objXmlParser = new XMLParser(subXML);
					
					if(WorkitemStatusDetails.equalsIgnoreCase(""))
						WorkitemStatusDetails=objXmlParser.getValueOf("PROCESSINSTANCEID")+"~"+objXmlParser.getValueOf("IntroductionDateTime")+"~"+objXmlParser.getValueOf("ACTIVITYNAME")+"~"+hmQueueStatusCode.get(objXmlParser.getValueOf("ACTIVITYNAME"))+"~"+hmQueueStatusDesc.get(objXmlParser.getValueOf("ACTIVITYNAME"))+"~End";
					else
						WorkitemStatusDetails=WorkitemStatusDetails+"~,~"+objXmlParser.getValueOf("PROCESSINSTANCEID")+"~"+objXmlParser.getValueOf("IntroductionDateTime")+"~"+objXmlParser.getValueOf("ACTIVITYNAME")+"~"+hmQueueStatusCode.get(objXmlParser.getValueOf("ACTIVITYNAME"))+"~"+hmQueueStatusDesc.get(objXmlParser.getValueOf("ACTIVITYNAME"))+"~End";
					//WriteLog("WorkitemStatusDetails: "+WorkitemStatusDetails);
				}
			}
		}
	}
	
	private void getQueueStatusCodeAndDesc() throws WICreateException
	{
		String getTableNameQry="SELECT QUEUENAME, STATUSCODE, STATUSDESC from "+WSR_QUEUESTATUSDETAILS+" WITH(NOLOCK) where PROCESSNAME='"+processID+"' AND ISACTIVE='Y'";
		String InputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML to Get Activity Name: "+InputXML);
		String OutputXML=executeAPI(InputXML);
		WriteLog("Output XML to Get Activity Name: "+OutputXML);
		XMLParser xmlobj1=new XMLParser(OutputXML);
		checkCallsMainCode(xmlobj1);
		int TotalRetrieved=Integer.parseInt(getTagValues(OutputXML, "TotalRetrieved"));
		
		if (TotalRetrieved > 0) 
		{
			for (int i = 0; i < TotalRetrieved; i++)
			{
				String subXML= xmlobj1.getNextValueOf("Record");
				XMLParser objXmlParser = new XMLParser(subXML);
				hmQueueStatusCode.put(objXmlParser.getValueOf("QUEUENAME"), objXmlParser.getValueOf("STATUSCODE"));
				hmQueueStatusDesc.put(objXmlParser.getValueOf("QUEUENAME"), objXmlParser.getValueOf("STATUSDESC"));
			}
		}
	}
	
	private void validateRequestParameters(String attributeList[]) throws WICreateException,Exception
	{
		WriteLog("Inside validateRequestParameters");
		if(sProcessName.equalsIgnoreCase("?") || sProcessName.equalsIgnoreCase(""))
			throw new WICreateException("1001",pCodes.getString("1001"));
		
		if(sRequestorChannelId.equalsIgnoreCase("?") || sRequestorChannelId.equalsIgnoreCase(""))
			throw new WICreateException("1011",pCodes.getString("1011"));
		//Fetch ProcessDefID
		getProcessDefID();	
		
		
		//Check if all Mandatory attributes present in USR_0_WSR_STATUS_ATTRDETAILS 
		checkMandatoryAttribute();
		
		for(int i=0;i<attributeObj.length;i++)
		{
			checkAttributeTable(attributeObj[i].getName(),attributeObj[i].getValue());
		}
		
		getQueueStatusCodeAndDesc();
		//getting details workitem number wise
		if(!"Case_Dedupe".equalsIgnoreCase(sSubProcess)){    //condition for dedupe as no workitem attribute is resent 
			getWorkitemDataBasedOnWINumber(attributeList);
			//getting details using date range
			//getWorkitemDataBasedOnDateRange(attributeList);
			//modified  by nikhil 19-08-22
			if (WorkitemStatusDetails.equalsIgnoreCase("")) // WorkitemStatusDetails blank means, workitem doesn't exists in flow
			{
				if("BAIS".equalsIgnoreCase(sProcessName))
				{
					loadConfiguration_OF();
					getWorkitemDataBasedOnWINumberOF(attributeList);
				
					if (WorkitemStatusDetails.equalsIgnoreCase("")) // WorkitemStatusDetails blank means, workitem doesn't exists in flow
					{
						throw new WICreateException("5052",pCodes.getString("5052")+": "+WINumber);
					}
				}
				else
				{
					throw new WICreateException("5052",pCodes.getString("5052")+": "+WINumber);
				}
			}
		}
		
		getTableName();
		
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
			   // WriteLog("Value of flag: "+flag);
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+attributeList[i]);
			}
		}
		else
		{
			throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
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
	//added by nikhil to accomodate inquiry in both ibps and of 19-0822
	
	private String getAPSelectWithColumnNamesXMLOF(String sQuery)
	{
		
		return 	 "<?xml version='1.0'?>" +
				 "<APSelectWithColumnNames_Input>" +
				 "<Option>APSelectWithColumnNames</Option>" +
				 "<Query>" + sQuery + "</Query>" +
				 "<EngineName>"+sCabinetNameOF+"</EngineName>" +
				 "</APSelectWithColumnNames_Input>";
		
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

private void doneworkitem()throws WICreateException
{
	try
	{
	//Lock Workitem.
	String getWorkItemInputXML = getWorkItemInput();
	String getWorkItemOutputXml = executeAPI(getWorkItemInputXML);
	WriteLog("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

	XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
	String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
	WriteLog("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

	if (getWorkItemMainCode.trim().equals("0")) 
	{
		WriteLog("WMgetWorkItemCall Successful: "+getWorkItemMainCode);
							
			//Move Workitem to next Workstep 
			String completeWorkItemInputXML = completeWorkItemInput();
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

  
}

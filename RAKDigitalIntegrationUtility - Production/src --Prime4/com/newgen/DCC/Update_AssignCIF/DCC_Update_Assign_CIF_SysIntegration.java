package com.newgen.DCC.Update_AssignCIF;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.newgen.DCC.CAMGenCIFUpdate.Digital_CCLog;
import com.newgen.DCC.DECTECHIntegration.DECTECHSystemIntegrationLog;
import com.newgen.DCC.SystemIntegration.DCCSystemIntegrationLog;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.omni.wf.util.app.NGEjbClient;
import com.newgen.wfdesktop.xmlapi.WFCallBroker;

public class DCC_Update_Assign_CIF_SysIntegration implements Runnable {



	private static NGEjbClient ngEjbClientCIFVer;

	static Map<String, String> ConfigParamMap= new HashMap<String, String>();
	
	public static int sessionCheckInt=0;
	public static int waitLoop=50;
	public static int loopCount=50;
	private static  String cabinetName;
	private static  String jtsIP;
	private static  String jtsPort;
	private static String ActivityType;
	private static String ProcessDefId;
	private static String ActivityName;
	private static String ActivityID;
	private String sessionID = "";
	private static String queueID = "";
	private int socketConnectionTimeout=0;
	private int integrationWaitTime=0;
	private int sleepIntervalInMin=0;

	@Override
	public void run()
	{
		
		

		try
		{
			DCC_UpdateAssignCIFLog.setLogger();
			ngEjbClientCIFVer = NGEjbClient.getSharedInstance();

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Connecting to Cabinet.");

			int configReadStatus = readConfig();

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("configReadStatus "+configReadStatus);
			if(configReadStatus !=0)
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Could not Read Config Properties [RAOPStatus]");
				return;
			}

			cabinetName = CommonConnection.getCabinetName();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Cabinet Name: " + cabinetName);

			jtsIP = CommonConnection.getJTSIP();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("JTSIP: " + jtsIP);

			jtsPort = CommonConnection.getJTSPort();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("JTSPORT: " + jtsPort);

			queueID = ConfigParamMap.get("queueID");
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("QueueID: " + queueID);

			socketConnectionTimeout=Integer.parseInt(ConfigParamMap.get("MQ_SOCKET_CONNECTION_TIMEOUT"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketConnectionTimeOut: "+socketConnectionTimeout);

			integrationWaitTime=Integer.parseInt(ConfigParamMap.get("INTEGRATION_WAIT_TIME"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("IntegrationWaitTime: "+integrationWaitTime);

			sleepIntervalInMin=Integer.parseInt(ConfigParamMap.get("SleepIntervalInMin"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SleepIntervalInMin: "+sleepIntervalInMin);


			sessionID = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);

			if(sessionID.trim().equalsIgnoreCase(""))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Could Not Connect to Server!");
			}
			else
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Session ID found: " + sessionID);
				HashMap<String, String> socketDetailsMap= socketConnectionDetails(cabinetName, jtsIP, jtsPort,
						sessionID);
				while(true)
				{
					DCC_UpdateAssignCIFLog.setLogger();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("DCC CIF Upadte and Assign CIF...123.");
					startDCCUpdateAssignCIFUtility(cabinetName, jtsIP, jtsPort,sessionID,
					queueID,socketDetailsMap);
					System.out.println("No More workitems to Process, Sleeping!");
					Thread.sleep(sleepIntervalInMin*60*1000);
				}
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Exception Occurred in DCC CIF Verification : "+e);
			final Writer result = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(result);
			e.printStackTrace(printWriter);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Exception Occurred in DCC CIF Verification : "+result);
		}
	}

	private int readConfig()
	{
		Properties p = null;
		try {

			p = new Properties();
			p.load(new FileInputStream(new File(System.getProperty("user.dir")+ File.separator + "ConfigFiles"+ File.separator+ "DCC_UpdateAndAssignCIF_Config.properties")));

			Enumeration<?> names = p.propertyNames();

			while (names.hasMoreElements())
			  {
			    String name = (String) names.nextElement();
			    ConfigParamMap.put(name, p.getProperty(name));
			  }
		    }
		catch (Exception e)
		{
			return -1 ;
		}
		return 0;
	}


	private void startDCCUpdateAssignCIFUtility(String cabinetName,String sJtsIp,String iJtsPort,String sessionId,
			String queueID,
			HashMap<String, String> socketDetailsMap)
	{
		final String ws_name=ConfigParamMap.get("WS_NAME");
		final String Queuename=ConfigParamMap.get("QueueName");

		try
		{
			//Validate Session ID
			sessionId  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
			sessionID = sessionId;
			if(sessionId==null || sessionId.equalsIgnoreCase("") || sessionId.equalsIgnoreCase("null"))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.error("Could Not Get Session ID "+sessionId);
				return;
			}

			//Fetch all Work-Items on given queueID.
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Fetching all Workitems on "+Queuename+" queue");
			System.out.println("Fetching all Workitems on "+Queuename+" queue");
			String fetchWorkitemListInputXML=CommonMethods.fetchWorkItemsInput(cabinetName, sessionId, queueID);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("InputXML for fetchWorkList Call: "+fetchWorkitemListInputXML);

			String fetchWorkitemListOutputXML= WFNGExecute(fetchWorkitemListInputXML,sJtsIp,iJtsPort,1);

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WMFetchWorkList OutputXML: "+fetchWorkitemListOutputXML);

			XMLParser xmlParserFetchWorkItemlist = new XMLParser(fetchWorkitemListOutputXML);

			String fetchWorkItemListMainCode = xmlParserFetchWorkItemlist.getValueOf("MainCode");
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FetchWorkItemListMainCode: "+fetchWorkItemListMainCode);

			int fetchWorkitemListCount = Integer.parseInt(xmlParserFetchWorkItemlist.getValueOf("RetrievedCount"));
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("RetrievedCount for WMFetchWorkList Call: "+fetchWorkitemListCount);

			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Number of workitems retrieved on CIF_Update_Initial: "+fetchWorkitemListCount);

			System.out.println("Number of workitems retrieved on CIF_Update_Initial: "+fetchWorkitemListCount);

			if (fetchWorkItemListMainCode.trim().equals("0") && fetchWorkitemListCount > 0)
			{
				for(int i=0; i<fetchWorkitemListCount; i++)
				{
					String fetchWorkItemlistData=xmlParserFetchWorkItemlist.getNextValueOf("Instrument");
					fetchWorkItemlistData =fetchWorkItemlistData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Parsing <Instrument> in WMFetchWorkList OutputXML: "+fetchWorkItemlistData);
					XMLParser xmlParserfetchWorkItemData = new XMLParser(fetchWorkItemlistData);

					String processInstanceID = xmlParserfetchWorkItemData.getValueOf("ProcessInstanceId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Current ProcessInstanceID: "+processInstanceID);

					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Processing Workitem: "+processInstanceID);
					System.out.println("\nProcessing Workitem: "+processInstanceID);

					String WorkItemID= xmlParserfetchWorkItemData.getValueOf("WorkItemId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Current WorkItemID: "+WorkItemID);

					String entryDateTime=xmlParserfetchWorkItemData.getValueOf("EntryDateTime");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Current EntryDateTime: "+entryDateTime);

					ActivityName=xmlParserfetchWorkItemData.getValueOf("ActivityName");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ActivityName: "+ActivityName);
					
					ActivityID = xmlParserfetchWorkItemData.getValueOf("WorkStageId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ActivityID: "+ActivityID);
					ActivityType = xmlParserfetchWorkItemData.getValueOf("ActivityType");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ActivityType: "+ActivityType);
					ProcessDefId = xmlParserfetchWorkItemData.getValueOf("RouteId");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ProcessDefId: "+ProcessDefId);
					
					String DB_Query = "SELECT Is_CIF_ASSIGNED,CardOps_Reschedule,Is_CIF_UPDATED,FIRCO_Flag,EFMS_Status,FTS_Ack_flg,is_stp,Dectech_Decision,FircoUpdateAction,UW_Decision,Product,Preferred_Language,Nationality,FATCA_Tin_Number FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
			        
			        String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DB_Query, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false));
			        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataIPXML: " + extTabDataINPXML);
			        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
			        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("extTabDataOPXML: " + extTabDataOUPXML);	
					
			        XMLParser xmlParserData = new XMLParser(extTabDataOUPXML);
			        int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
			        
			        String decisionValue="";
					String attributesTag="";
					String updateCIFIntegrationStatus="";
					String ErrDesc = "";
					
			        if (!xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec == 0)
			        {
						decisionValue = "Failed";
						ErrDesc="apselect for Fetching WI details failed" ;
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("apselect for Fetching WI details failed");
						doneWI(processInstanceID, WorkItemID, decisionValue, entryDateTime, ErrDesc, attributesTag, sessionId);
						continue;
					}
			        
			        String Is_CIF_UPDATED = xmlParserData.getValueOf("Is_CIF_UPDATED");
			        String Is_CIF_ASSIGNED = xmlParserData.getValueOf("Is_CIF_ASSIGNED");
			        String CardOps_Reschedule = xmlParserData.getValueOf("CardOps_Reschedule");
					
			        if (!"Y".equalsIgnoreCase(CardOps_Reschedule) && !"Y".equalsIgnoreCase(Is_CIF_UPDATED)) {
			        	DCC_CIFUpdate objUpadteCIF = new DCC_CIFUpdate(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
						updateCIFIntegrationStatus = objUpadteCIF.customIntegration(cabinetName,sessionId, sJtsIp,
								iJtsPort,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeout,  socketDetailsMap);
						if (!"Success".equalsIgnoreCase(updateCIFIntegrationStatus)) {
							ErrDesc = updateCIFIntegrationStatus.replace("~", ",").replace("|", "\n");
							decisionValue = "Failed";
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
							doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
							continue;
						} else {
							/**Update Is_CIF_UPDATED value in exttable to Y**/
							if (updateFlagInExtTable("Y", "Is_CIF_UPDATED", processInstanceID, entryDateTime, sessionId)){
								Is_CIF_UPDATED = "Y";
							} else
								continue;
						}
			        }
			        
			        if (!"Y".equalsIgnoreCase(CardOps_Reschedule) && "Y".equalsIgnoreCase(Is_CIF_UPDATED) && !"Y".equalsIgnoreCase(Is_CIF_ASSIGNED)) {
			        	
						DCC_Assign_CIF objAssignCIF= new DCC_Assign_CIF(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
						
			        	String firco=xmlParserData.getValueOf("FIRCO_Flag");
			        	String fircoAction=xmlParserData.getValueOf("FircoUpdateAction");
			        	String efms=xmlParserData.getValueOf("EFMS_Status");
			        	String fts =xmlParserData.getValueOf("FTS_Ack_flg");
			        	String stp =xmlParserData.getValueOf("is_stp");
			        	String DectechDecision =xmlParserData.getValueOf("Dectech_Decision");
			        	String UWDecision=xmlParserData.getValueOf("UW_Decision");
			        	
			        	if("Reject".equalsIgnoreCase(UWDecision) || "D".equalsIgnoreCase(DectechDecision) || "CB".equalsIgnoreCase(firco) || "Confirmed Fraud".equalsIgnoreCase(efms)||("Y".equalsIgnoreCase(stp)&&"R".equalsIgnoreCase(DectechDecision)) || "D".equalsIgnoreCase(fts)|| "Decline".equalsIgnoreCase(fircoAction))
			        	{
			        		String NOTIFY_DEH_IDENTIFIER="Decline_Prospect";
			        		if("D".equalsIgnoreCase(fts)|| "Decline".equalsIgnoreCase(fircoAction))
			        			NOTIFY_DEH_IDENTIFIER="Expire_Prospect";
			        		decisionValue="Reject";
							attributesTag="<Decision>"+decisionValue+"</Decision>"+
										"<NOTIFY_DEH_IDENTIFIER>"+NOTIFY_DEH_IDENTIFIER+"</NOTIFY_DEH_IDENTIFIER>";
							ErrDesc = "CIF Update Done Successfully";
							doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
							continue;
			        	}
				        
						String assignCIFIntegrationStatus=objAssignCIF.DCC_Assign_CIF_Integration(cabinetName,jtsIP,jtsPort,sessionId,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeout,socketDetailsMap);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("assignCIFIntegrationStatus" +assignCIFIntegrationStatus);
						String statuses [] = null;
						
						if (assignCIFIntegrationStatus != null)
							statuses = assignCIFIntegrationStatus.split("~");
							
						if (statuses != null && statuses.length > 0 && statuses[0].equalsIgnoreCase("0000"))
						{
							/**Update Is_CIF_ASSIGNED value in exttable to Y**/
							if (updateFlagInExtTable("Y", "Is_CIF_ASSIGNED", processInstanceID, entryDateTime, sessionId)){
								Is_CIF_ASSIGNED = "Y";
							} else
								continue;
									        	
						} else {
							if (statuses != null && statuses.length > 0) {
								ErrDesc = "Assign CIF Failed " + statuses[0] + ":";
								if (statuses.length > 1)
									ErrDesc =ErrDesc +statuses[1];
							} else {
								ErrDesc = "Assign CIF Failed ";
							}
							decisionValue = "Failed";
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
							doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
							continue;
						}
			        }
			        
			        if ( "Y".equalsIgnoreCase(Is_CIF_ASSIGNED)  && !"Y".equalsIgnoreCase(CardOps_Reschedule)) {
			      //vinayak changes starts
			        	
			        	//is_service_main
		        	DCC_Service_Maintenance objservice= new DCC_Service_Maintenance(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);

					String serviceRequestIntegrationStatus=objservice.DCC_Service_Maintenance_Integration(cabinetName,jtsIP,jtsPort,sessionId,processInstanceID,ws_name,integrationWaitTime,socketConnectionTimeout,socketDetailsMap);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("serviceRequestIntegrationStatus" +serviceRequestIntegrationStatus);
					String statuses [] = null;
					
					if (serviceRequestIntegrationStatus != null)
						statuses = serviceRequestIntegrationStatus.split("~");
						
					if (statuses != null && statuses.length > 0 && statuses[0].equalsIgnoreCase("0000"))
					{
						/**Update Is_CIF_ASSIGNED value in exttable to Y**/
						if (updateFlagInExtTable("Y", "is_service_main", processInstanceID, entryDateTime, sessionId)){
							String is_service_main = "Y";
						} else
							continue;
								        	
					} else {
						if (statuses != null && statuses.length > 0) {
							ErrDesc = "Service Maintenance Failed " + statuses[0] + ":";
							if (statuses.length > 1)
								ErrDesc =ErrDesc +statuses[1];
						} else {
							ErrDesc = "Service Maintenance Failed ";
						}
						decisionValue = "Failed";
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
						continue;
					}
					
					//vinayak changes ends
				}
			        
			        //Updated 14122022
			        if ("Y".equalsIgnoreCase(Is_CIF_UPDATED) && "Y".equalsIgnoreCase(Is_CIF_ASSIGNED)  && !"Y".equalsIgnoreCase(CardOps_Reschedule)) {
			        	
			        	
			        	
						String Product=xmlParserData.getValueOf("Product");
						String nationality=xmlParserData.getValueOf("Nationality");
			        	String Preferred_Language=xmlParserData.getValueOf("Preferred_Language");
			        	String TIN=xmlParserData.getValueOf("FATCA_Tin_Number");
			        	
						DCC_DocumentGeneration objDocGen = new DCC_DocumentGeneration(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
						String docToBeGen="";
						if("ISL".equalsIgnoreCase(Product))
						{
							docToBeGen="MRBH_Agency_Agreement";
							if("AR".equalsIgnoreCase(Preferred_Language))
								docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-Arabic";
							else
								docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-English";
						}
						else
						{
							if ("AR".equalsIgnoreCase(Preferred_Language)){
								docToBeGen = "Customer_Consent_Form_Conv-Arabic";
							}
							else{
								docToBeGen = "Customer_Consent_Form_Conv-English";
							}
						}
						
						if("US".equalsIgnoreCase(nationality))
						{
							docToBeGen=docToBeGen+"~W-9_Form";
						}
						else if(!"US".equalsIgnoreCase(nationality) && TIN!=null && !"".equalsIgnoreCase(TIN))
						{
							docToBeGen=docToBeGen+"~W-8_Form";
						}
						docToBeGen=docToBeGen+"~Security_Cheque";
					
						String docGenStatus=objDocGen.generate_Document_Customer_Consent(docToBeGen,processInstanceID, sessionId);
					
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGenStatus:--" +docGenStatus);
						if (docGenStatus == null || docGenStatus.contains("Error"))
						{
							decisionValue = "Failed";
							ErrDesc = "Doc Genration Failed";
							String err[] = docGenStatus.split("~");
							if(err.length>1)
								ErrDesc = "Doc Genration Failed for document "+err[1];
						}
						else if(docGenStatus.contains("Success"))
						{
							decisionValue = "Success";
						}
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
						continue;
			        }
			        
			        
			        if ("Y".equalsIgnoreCase(Is_CIF_UPDATED) && "Y".equalsIgnoreCase(Is_CIF_ASSIGNED) && "Y".equalsIgnoreCase(CardOps_Reschedule)) {
			        	DCC_DocumentGeneration objDocGen = new DCC_DocumentGeneration(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
			        	String Preferred_Language=xmlParserData.getValueOf("Preferred_Language");
			        	String Product=xmlParserData.getValueOf("Product");
						String nationality=xmlParserData.getValueOf("Nationality");
			        	String TIN=xmlParserData.getValueOf("FATCA_Tin_Number");
			        	
						String doc_DB_Query = "select Doc_Name from NG_DCC_GR_DOCUMENT_NAME with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
				        
				        String docGRDataINPXML = CommonMethods.apSelectWithColumnNames(doc_DB_Query, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false));
				        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGRDataINPXML: " + docGRDataINPXML);
				        String docGRDataOUPXML = CommonMethods.WFNGExecute(docGRDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
				        DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGRDataOUPXML: " + docGRDataOUPXML);	
						
				        XMLParser xmlParserData1 = new XMLParser(docGRDataOUPXML);
				        int iTotalrec1 = Integer.parseInt(xmlParserData1.getValueOf("TotalRetrieved"));
				        
				        String decisionValue1="";
						String attributesTag1="";
						String ErrDesc1 = "";
						String docToBeGen1 = "";
						
				        if (!xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 == 0)
				        {
							decisionValue1 = "Failed";
							ErrDesc1="apselect for Fetching WI details failed" ;
							attributesTag1="<Decision>"+decisionValue1+"</Decision>";
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("apselect for Fetching WI doc name failed");
							doneWI(processInstanceID, WorkItemID, decisionValue, entryDateTime, ErrDesc1, attributesTag1, sessionId);
							continue;
						}
						
						
						if(xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 > 0){
						
						for(int k = 0;k < iTotalrec1;k++){
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("----Reschedule flag document Name -- "+ k);
							String doc_name_record =xmlParserData1.getNextValueOf("Record");
							doc_name_record = doc_name_record.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");
							XMLParser docxmlParser = new XMLParser(doc_name_record);
							String doc_name = docxmlParser.getValueOf("doc_name");
							
							if(doc_name.equalsIgnoreCase("MRBH_Agency_Agreement")){
								docToBeGen1=docToBeGen1+"~MRBH_Agency_Agreement";
							}
							
							if(doc_name.equalsIgnoreCase("Customer_Consent_Form")){
								if("ISL".equalsIgnoreCase(Product))
								{
									if("AR".equalsIgnoreCase(Preferred_Language)){
									docToBeGen1=docToBeGen1+"~Customer_Consent_Form_Islamic-Arabic";
									
								}	
									else{
										docToBeGen1=docToBeGen1+"~Customer_Consent_Form_Islamic-English";
									}
										
								}
								else
								{
									if ("AR".equalsIgnoreCase(Preferred_Language)){
										docToBeGen1 = "Customer_Consent_Form_Conv-Arabic";
									}
									else{
										docToBeGen1 = "Customer_Consent_Form_Conv-English";
									}
								}
							}
							
							if(doc_name.equalsIgnoreCase("US") || doc_name.equalsIgnoreCase("W8/W9")){
								
								if("US".equalsIgnoreCase(nationality))
								{
									docToBeGen1=docToBeGen1+"~W-9_Form";
								}
								else if(!"US".equalsIgnoreCase(nationality) && TIN!=null && !"".equalsIgnoreCase(TIN))
								{
									docToBeGen1=docToBeGen1+"~W-8_Form";
								}
							}
							
							if(doc_name.equalsIgnoreCase("W-9_Form") || doc_name.equalsIgnoreCase("W-9") || doc_name.equalsIgnoreCase("W9")){
								
									docToBeGen1=docToBeGen1+"~W-9_Form";
								
							}
							if(doc_name.equalsIgnoreCase("W-8_Form") || doc_name.equalsIgnoreCase("W-8") || doc_name.equalsIgnoreCase("W8")){
						
								docToBeGen1=docToBeGen1+"~W-8_Form";
							
							}
							
							if(doc_name.equalsIgnoreCase("Security_Cheque")){
								
								docToBeGen1=docToBeGen1+"~Security_Cheque";
							}
							
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("----Reschedule flag document Name -- Final Doc to be generated 123 "+docToBeGen1);	
							
						}
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("----Reschedule flag document Name -- Final Doc to be generated "+docToBeGen1);
						String docGenStatus=objDocGen.generate_Document_Customer_Consent(docToBeGen1,processInstanceID, sessionId);
						
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGenStatus:--" +docGenStatus);
						if (docGenStatus == null || docGenStatus.contains("Error"))
						{
							decisionValue = "Failed";
							ErrDesc = "Doc Genration Failed";
							String err[] = docGenStatus.split("~");
							if(err.length>1)
								ErrDesc = "Doc Genration Failed for document "+err[1];
						}
						else if(docGenStatus.contains("Success"))
						{
							decisionValue = "Success";
						}
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
						attributesTag="<Decision>"+decisionValue+"</Decision>";
						doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
						continue;
							
							
						}
						
						if(xmlParserData1.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec1 ==0){
							 Product=xmlParserData.getValueOf("Product");
							 nationality=xmlParserData.getValueOf("Nationality");
				        	 Preferred_Language=xmlParserData.getValueOf("Preferred_Language");
				        	 TIN=xmlParserData.getValueOf("FATCA_Tin_Number");
				        	
							 objDocGen = new DCC_DocumentGeneration(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger);
							String docToBeGen="";
							if("ISL".equalsIgnoreCase(Product))
							{
								docToBeGen="MRBH_Agency_Agreement";
								if("AR".equalsIgnoreCase(Preferred_Language))
									docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-Arabic";
								else
									docToBeGen=docToBeGen+"~Customer_Consent_Form_Islamic-English";
							}
							else
							{
								if ("AR".equalsIgnoreCase(Preferred_Language)){
									docToBeGen = "Customer_Consent_Form_Conv-Arabic";
								}
								else{
									docToBeGen = "Customer_Consent_Form_Conv-English";
								}
							}
							
							if("US".equalsIgnoreCase(nationality))
							{
								docToBeGen=docToBeGen+"~W-9_Form";
							}
							else if(!"US".equalsIgnoreCase(nationality) && TIN!=null && !"".equalsIgnoreCase(TIN))
							{
								docToBeGen=docToBeGen+"~W-8_Form";
							}
							docToBeGen=docToBeGen+"~Security_Cheque";
						
							String docGenStatus=objDocGen.generate_Document_Customer_Consent(docToBeGen,processInstanceID, sessionId);
						
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("docGenStatus:--" +docGenStatus);
							if (docGenStatus == null || docGenStatus.contains("Error"))
							{
								decisionValue = "Failed";
								ErrDesc = "Doc Genration Failed";
								String err[] = docGenStatus.split("~");
								if(err.length>1)
									ErrDesc = "Doc Genration Failed for document "+err[1];
							}
							else if(docGenStatus.contains("Success"))
							{
								decisionValue = "Success";
							}
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Decision" +decisionValue);
							attributesTag="<Decision>"+decisionValue+"</Decision>";
							doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime,ErrDesc,attributesTag,sessionId);
							continue;
						}
					}
					//
			        }
			        
			}
		}
		catch (Exception e)
		{
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Exception: "+e.getMessage());
		}
	}

	private boolean updateFlagInExtTable(String columnValue, String columnNames, String processInstanceID, String entryDateTime, String sessionId) {
		columnValue = "'" + columnValue + "'";
		DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("column Names for AP udpate call : " +columnNames);
		
		String sWhereClause = "WI_NAME='" + processInstanceID + "'";
		String tableName = "NG_DCC_EXTTABLE";
		try {
		    String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false), 
		    		tableName, columnNames, columnValue, sWhereClause);
		    DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
		    String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
		
		    DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
		    XMLParser sXMLParserChild = new XMLParser(outputXml);
		    String StrMainCode = sXMLParserChild.getValueOf("MainCode");
		    if (StrMainCode.equals("0")) {
		    	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML for apUpdateInput for : Success" );
		    	return true;
		    } 
		    else 
		    {
		    	DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML for apUpdateInput for : Failed" );
		    	String decisionValue = "Failed";
		    	String ErrDesc="apupdate failed : " +  columnNames;
		    	String attributesTag="<Decision>"+decisionValue+"</Decision>";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("apupdate for : " + columnNames);
				doneWI(processInstanceID, processInstanceID, decisionValue, entryDateTime, ErrDesc, attributesTag, sessionId);
				return false;
		    }
		} catch (Exception e) {
			String decisionValue = "Failed";
	    	String ErrDesc="Some Exception occured while updating : " +  e.getMessage();
	    	String attributesTag="<Decision>"+decisionValue+"</Decision>";
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("some error occured while ");
			doneWI(processInstanceID, processInstanceID, decisionValue, entryDateTime, ErrDesc, attributesTag, sessionId);
			return false;
		}
		
	}

	private void doneWI(String processInstanceID,String WorkItemID,String decisionValue,String entryDateTime ,String ErrDesc,String attributesTag,String sessionId)
	{
		try
		{
			//Lock Workitem.
			String getWorkItemInputXML = CommonMethods.getWorkItemInput(cabinetName, sessionId, processInstanceID,WorkItemID);
			String getWorkItemOutputXml = WFNGExecute(getWorkItemInputXML,jtsIP,jtsPort,1);
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);

			XMLParser xmlParserGetWorkItem = new XMLParser(getWorkItemOutputXml);
			String getWorkItemMainCode = xmlParserGetWorkItem.getValueOf("MainCode");
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WmgetWorkItemCall Maincode:  "+ getWorkItemMainCode);

			if (getWorkItemMainCode.trim().equals("0"))
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WMgetWorkItemCall Successful: "+getWorkItemMainCode);

				//String assignWorkitemAttributeInputXML=CommonMethods.assignWorkitemAttributeInput(cabinetName, sessionId,processInstanceID,WorkItemID,attributesTag);
				
				String assignWorkitemAttributeInputXML = "<?xml version=\"1.0\"?><WMAssignWorkItemAttributes_Input>"
						+ "<Option>WMAssignWorkItemAttributes</Option>"
						+ "<EngineName>"+cabinetName+"</EngineName>"
						+ "<SessionId>"+sessionId+"</SessionId>"
						+ "<ProcessInstanceId>"+processInstanceID+"</ProcessInstanceId>"
						+ "<WorkItemId>"+WorkItemID+"</WorkItemId>"
						+ "<ActivityId>"+ActivityID+"</ActivityId>"
						+ "<ProcessDefId>"+ProcessDefId+"</ProcessDefId>"
						+ "<LastModifiedTime></LastModifiedTime>"
						+ "<ActivityType>"+ActivityType+"</ActivityType>"
						+ "<complete>D</complete>"
						+ "<AuditStatus></AuditStatus>"
						+ "<Comments></Comments>"
						+ "<UserDefVarFlag>Y</UserDefVarFlag>"
						+ "<Attributes>"+attributesTag+"</Attributes>"
						+ "</WMAssignWorkItemAttributes_Input>";
				
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("InputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeInputXML);

				String assignWorkitemAttributeOutputXML=WFNGExecute(assignWorkitemAttributeInputXML,jtsIP, jtsPort,1);

				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("OutputXML for assignWorkitemAttribute Call: "+assignWorkitemAttributeOutputXML);

				XMLParser xmlParserWorkitemAttribute = new XMLParser(assignWorkitemAttributeOutputXML);
				String assignWorkitemAttributeMainCode = xmlParserWorkitemAttribute.getValueOf("MainCode");
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute MainCode: "+assignWorkitemAttributeMainCode);

				if(assignWorkitemAttributeMainCode.trim().equalsIgnoreCase("0"))
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute Successful: "+assignWorkitemAttributeMainCode);	
					if ("0".trim().equalsIgnoreCase("0"))
					{
						//DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WmCompleteWorkItem successful: "+completeWorkitemMaincode);
						System.out.println(processInstanceID + "Complete Succesfully with status "+decisionValue);

						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WorkItem moved to next Workstep.");

						SimpleDateFormat inputDateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
						SimpleDateFormat outputDateFormat=new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");

						Date entryDatetimeFormat = inputDateformat.parse(entryDateTime);
						String formattedEntryDatetime=outputDateFormat.format(entryDatetimeFormat);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FormattedEntryDatetime: "+formattedEntryDatetime);

						Date actionDateTime= new Date();
						String formattedActionDateTime=outputDateFormat.format(actionDateTime);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("FormattedActionDateTime: "+formattedActionDateTime);

						//Insert in WIHistory Table.
						String columnNames="WI_NAME,dec_date,WORKSTEP,USER_NAME,DECISION,ENTRY_DATE_TIME,REMARKS";
						String columnValues="'"+processInstanceID+"','"+formattedActionDateTime+"','"+ActivityName+"','"
						+CommonConnection.getUsername()+"','"+decisionValue+"','"+formattedEntryDatetime+"','"+ErrDesc+"'";

						String apInsertInputXML=CommonMethods.apInsert(cabinetName, sessionId, columnNames, columnValues,"NG_DCC_GR_DECISION_HISTORY");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertInputXML: "+apInsertInputXML);

						String apInsertOutputXML = WFNGExecute(apInsertInputXML,jtsIP,jtsPort,1);
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("APInsertOutputXML: "+ apInsertInputXML);

						XMLParser xmlParserAPInsert = new XMLParser(apInsertOutputXML);
						String apInsertMaincode = xmlParserAPInsert.getValueOf("MainCode");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Status of apInsertMaincode  "+ apInsertMaincode);

						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Completed On "+ ActivityName);


						if(apInsertMaincode.equalsIgnoreCase("0"))
						{
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert successful: "+apInsertMaincode);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inserted in WiHistory table successfully.");
						}
						else
						{
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("ApInsert failed: "+apInsertMaincode);
						}
					}
					else
					{
						//completeWorkitemMaincode="";
						//DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WMCompleteWorkItem failed: "+completeWorkitemMaincode);
					}
				}
				else if("11".equalsIgnoreCase(assignWorkitemAttributeMainCode)){
					
					sessionID = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
					doneWI(processInstanceID,WorkItemID,decisionValue,entryDateTime ,ErrDesc,attributesTag,sessionID);
				}
				else
				{
					assignWorkitemAttributeMainCode="";
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("AssignWorkitemAttribute failed: "+assignWorkitemAttributeMainCode);
				}
			}
			else
			{
				getWorkItemMainCode="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("WmgetWorkItem failed: "+getWorkItemMainCode);
			}
		}
		
		catch (Exception e)
		{
			DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("DoneWI Exception: "+e.toString());
		}
	}


			private HashMap<String,String> socketConnectionDetails(String cabinetName, String sJtsIp, String iJtsPort,
					String sessionID)
			{
				HashMap<String, String> socketDetailsMap = new HashMap<String, String>();

				try
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Fetching Socket Connection Details.");
					System.out.println("Fetching Socket Connection Details.");

					String socketDetailsQuery = "SELECT SocketServerIP,SocketServerPort FROM NG_BPM_MQ_TABLE with (nolock) where ProcessName = 'DCC' and CallingSource = 'Utility'";

					String socketDetailsInputXML =CommonMethods.apSelectWithColumnNames(socketDetailsQuery, cabinetName, sessionID);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Socket Details APSelect InputXML: "+socketDetailsInputXML);

					String socketDetailsOutputXML=WFNGExecute(socketDetailsInputXML,sJtsIp,iJtsPort,1);
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Socket Details APSelect OutputXML: "+socketDetailsOutputXML);

					XMLParser xmlParserSocketDetails= new XMLParser(socketDetailsOutputXML);
					String socketDetailsMainCode = xmlParserSocketDetails.getValueOf("MainCode");
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketDetailsMainCode: "+socketDetailsMainCode);

					int socketDetailsTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketDetailsTotalRecords: "+socketDetailsTotalRecords);

					if(socketDetailsMainCode.equalsIgnoreCase("0")&& socketDetailsTotalRecords>0)
					{
						String xmlDataSocketDetails=xmlParserSocketDetails.getNextValueOf("Record");
						xmlDataSocketDetails =xmlDataSocketDetails.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

						XMLParser xmlParserSocketDetailsRecord = new XMLParser(xmlDataSocketDetails);

						String socketServerIP=xmlParserSocketDetailsRecord.getValueOf("SocketServerIP");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketServerIP: "+socketServerIP);
						socketDetailsMap.put("SocketServerIP", socketServerIP);

						String socketServerPort=xmlParserSocketDetailsRecord.getValueOf("SocketServerPort");
						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketServerPort " + socketServerPort);
						socketDetailsMap.put("SocketServerPort", socketServerPort);

						DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("SocketServer Details found.");
						System.out.println("SocketServer Details found.");

					}
				}
				catch (Exception e)
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Exception in getting Socket Connection Details: "+e.getMessage());
					System.out.println("Exception in getting Socket Connection Details: "+e.getMessage());
				}

				return socketDetailsMap;
			}
			protected static String WFNGExecute(String ipXML, String jtsServerIP, String serverPort,
					int flag) throws IOException, Exception
			{
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("In WF NG Execute : " + serverPort);
				try
				{
					if (serverPort.startsWith("33"))
						return WFCallBroker.execute(ipXML, jtsServerIP,
								Integer.parseInt(serverPort), 1);
					else
						return ngEjbClientCIFVer.makeCall(jtsServerIP, serverPort,
								"WebSphere", ipXML);
				}
				catch (Exception e)
				{
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Exception Occured in WF NG Execute : "+ e.getMessage());
					e.printStackTrace();
					return "Error";
				}
			}
			
			private  String getPreviousWorkStep( String sWorkItemName, String sWorkitemId )
			{
				String prevWS="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Start of function getPreviousWorkStep ");
				String outputXML=null;
				String mainCode=null;
				try
				{

					sessionCheckInt=0;
					while(sessionCheckInt<loopCount)
					{
						try 
						{
							XMLParser objXMLParser = new XMLParser();
							String sqlQuery = "select PreviousStage from WFINSTRUMENTTABLE with(nolock) where ProcessInstanceID = '"+sWorkItemName+"' and WorkItemId='"+sWorkitemId+"'";
							String InputXML = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionID);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Getting PreviousWorkStep from instrument table "+InputXML);
							outputXML = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("OutputXML for getting PreviousWorkStep from external table "+outputXML);
							objXMLParser.setInputXML(outputXML);
							mainCode=objXMLParser.getValueOf("MainCode");
							if (mainCode.equalsIgnoreCase("0")) 
							{
								prevWS = CommonMethods.getTagValues(outputXML, "PreviousStage");
							}
						} 
						catch (Exception e) 
						{
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
						if(mainCode.equalsIgnoreCase("0")){
							break;
						}
						else if (mainCode.equalsIgnoreCase("11"))
						{
							sessionID  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
						}
						else
						{
							sessionCheckInt++;
							break;
						}
					}


				}
				catch(Exception e)
				{
					e.printStackTrace();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inside catch of getPreviousWorkStep function with exception.."+e);
				}
				return prevWS;
			}
			private  String getUWDecision( String sWorkItemName, String sWorkitemId )
			{
				String decision="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Start of function getUWDecision ");
				String outputXML=null;
				String mainCode=null;
				try
				{

					sessionCheckInt=0;
					while(sessionCheckInt<loopCount)
					{
						try 
						{
							XMLParser objXMLParser = new XMLParser();
							String sqlQuery = "select UW_Decision from NG_DCC_EXTTABLE with(nolock) where Wi_Name = '"+sWorkItemName+"'";
							String InputXML = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionID);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Getting UW_Decision from NG_DCC_EXTTABLE table "+InputXML);
							outputXML = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("OutputXML for getting UW_Decision from external table "+outputXML);
							objXMLParser.setInputXML(outputXML);
							mainCode=objXMLParser.getValueOf("MainCode");
							if (mainCode.equalsIgnoreCase("0")) 
							{
								decision = CommonMethods.getTagValues(outputXML, "UW_Decision");
								if(decision==null)
									decision="";
							}
						} 
						catch (Exception e) 
						{
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
						if(mainCode.equalsIgnoreCase("0")){
							break;
						}
						else if (mainCode.equalsIgnoreCase("11"))
						{
							sessionID  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
						}
						else
						{
							sessionCheckInt++;
							break;
						}
					}

					
				}
				catch(Exception e)
				{
					e.printStackTrace();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inside catch of getUWDecision function with exception.."+e);
				}
				return decision;
			}
			private  String getDocName( String sWorkItemName)
			{
				String decision="";
				DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Start of function getDocName ");
				String outputXML=null;
				String mainCode=null;
				try
				{

					sessionCheckInt=0;
					while(sessionCheckInt<loopCount)
					{
						try 
						{
							XMLParser objXMLParser = new XMLParser();
							String sqlQuery = "select Product,Preferred_Language from NG_DCC_EXTTABLE with(nolock) where Wi_Name = '"+sWorkItemName+"'";
							String InputXML = CommonMethods.apSelectWithColumnNames(sqlQuery,cabinetName, sessionID);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("Getting getDocName from NG_DCC_EXTTABLE table "+InputXML);
							outputXML = WFNGExecute(InputXML, jtsIP, jtsPort, 1);
							DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.info("OutputXML for getting getDocName from external table "+outputXML);
							objXMLParser.setInputXML(outputXML);
							mainCode=objXMLParser.getValueOf("MainCode");
							if (mainCode.equalsIgnoreCase("0")) 
							{
								decision = CommonMethods.getTagValues(outputXML, "UW_Decision");
								if(decision==null)
									decision="";
							}
						} 
						catch (Exception e) 
						{
							sessionCheckInt++;
							waiteloopExecute(waitLoop);
							continue;
						}
						if(mainCode.equalsIgnoreCase("0")){
							break;
						}
						else if (mainCode.equalsIgnoreCase("11"))
						{
							sessionID  = CommonConnection.getSessionID(DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger, false);
						}
						else
						{
							sessionCheckInt++;
							break;
						}
					}

					
				}
				catch(Exception e)
				{
					e.printStackTrace();
					DCC_UpdateAssignCIFLog.DCC_Update_And_Assign_CIF_Logger.debug("Inside catch of getUWDecision function with exception.."+e);
				}
				return decision;
			}

			public static void waiteloopExecute(long wtime) {
				try {
					for (int i = 0; i < 10; i++) {
						Thread.yield();
						Thread.sleep(wtime / 10);
					}
				} catch (InterruptedException e) {
				}
			}

}

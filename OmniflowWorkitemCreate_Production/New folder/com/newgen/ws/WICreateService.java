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
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;

 
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





public class WICreateService extends CreateWorkitem
{
	
	
	//CONSTANTS 
	final private String SUCCESS_STATUS="0";
	//TABLE NAME CONSTANTS
	final private String SRM_WIHISTORY="USR_0_SRM_WIHISTORY";
	final private String WSR_PROCESS="USR_0_WSR_PROCESS";
	final private String WSR_ATTRDETAILS="USR_0_WSR_ATTRDETAILS";
	final private String WSR_DOCTYPEDETAILS="USR_0_WSR_DOCTYPEDETAILS";
	final private String SRM_SUBCATEGORY="USR_0_SRM_SUBCATEGORY";
	final private String SRM_CATEGORY="USR_0_SRM_CATEGORY";
	final private String SRM_CARDS_BIN="USR_0_SRM_CARDS_BIN";
	// CAC Tables
	final private String CAC_WIHISTORY = "USR_0_CAC_WIHISTORY";
	final private String CAC_MAILINGADDRESS = "USR_0_CAC_MAILINGADDRESS";
	
	// For RMT
	final private String RMT_WIHISTORY = "USR_0_RMT_WIHISTORY";
	final private String RMT_OECDGRID = "USR_0_RMT_OECD_DTLS_GRID";
	final private String RMT_EXTERNALTABLE = "RB_RMT_EXTTABLE";
	final private String RMT_DUPLICATE = "USR_0_WSR_DUPLICATEWORKITEM";
	
	//For OECD
	final private String OECD_WIHISTORY = "USR_0_OECD_WIHISTORY";
	final private String OECD_OECDGRID = "USR_0_OECD_DTLS_GRID";
	
	//For RAOP
	final private String RAOP_WIHISTORY = "USR_0_RAOP_WIHISTORY";
	final private String RAOP_OECDGRID = "USR_0_RAOP_TAX_DTLS";
	
	//For CMP
	final private String CMP_WIHISTORY = "USR_0_CMP_WIHISTORY";
	
	//For BAIS
	final private String BAIS_WIHISTORY = "USR_0_BAIS_WIHISTORY";
	
	//For CBP
	final private String CBP_WIHISTORY = "USR_0_CBP_WIHISTORY";
	
	//For DAC
	final private String DAC_WIHISTORY = "USR_0_DAC_WIHISTORY";
	
	//For ACP
	final private String ACP_WIHISTORY = "USR_0_ACP_WIHISTORY";
	
	//For TT
	final private String TT_WIHISTORY = "USR_0_TT_WIHISTORY";
	
	// for iRBL
	final private String iRBL_WIHISTORY = "USR_0_iRBL_WIHISTORY";
	
	// for TWC
		final private String TWC_WIHISTORY = "USR_0_TWC_WIHISTORY";
	
	//For CDOB
	final private String CDOB_EXTTABLE="NG_CC_EXTTABLE";
	final private String Customer_Fragment_Table="ng_rlos_Customer";
	final private String CARD_PRODUCT="ng_rlos_IGR_Eligibility_CardProduct";
	final private String CARD_LIMIT="ng_rlos_IGR_Eligibility_CardLimit";
	private String CardProductForDesc="";
	
	
	//For FIRCO in Digital AO Process
	final private String GRID_DTLS ="NG_DAO_GR_UID";
	final private String FircoTable = "NG_RLOS_FIRCO";
	final private String DAO_DECISION_HISTORY = "NG_DAO_GR_DECISION_HISTORY";
	
	
	//for digital_cc process
	final private String DCC_DECISION_HISTORY = "NG_DCC_GR_DECISION_HISTORY";
	final private String DCC_GR_UID ="NG_DCC_GR_UID";
	
	//Reddy 7.6.22 BSR Process
	final private String BSR_DECISION_HISTORY = "USR_0_BSR_WIHISTORY";
	
	
//Possibe vlaues of input parameter***********************
	/* public enum IsPrimaryExisiting_Enum {Y,N}
	 public enum IsDebitCardReq_Enum {Y,N}
	 public enum ApplicationType_Enum {Single,Joint}
	 public enum AddressType_Enum {Residence,Office,Home}
	 public enum JointDocType_Enum {Passport,Khulasat_QAID_number}
	 public enum PrimaryDocType_Enum {Passport,Khulasat_QAID_number}
	 //public enum PrimaryEmpType_Enum {Salaried,Self_Employed,Pensioner,Others}
	 
	 public enum IsChequeBReq_Enum {Y,N}
	 public enum PrimaryTitle_Enum  {Mr,Mrs,Ms}
	 public enum PrimaryGender_Enum {Male,Female}
	 public enum PrimaryMaritalStatus_Enum {Single,Married,Others}
     public enum JointMaritalStatus_Enum {Single,Married,Others}
	 public enum ISUSCitizenPrimary_Enum {Y,N}
	 public enum IsUSTAXPayerPrimary_Enum {Y,N}
	 public enum InterestsHawalaPrimary_Enum {Y,N}
	 public enum IsDualNationalityPrimary_Enum {Y,N}
	 public enum IsUSCitizenJoint_Enum {Y,N}
	 public enum IsUSTAXPayerJoint_Enum {Y,N}
	 public enum InterestsHawalaJoint_Enum {Y,N}
	 public enum IsDualNationalityJoint_Enum {Y,N}
	 public enum JointGender_Enum {Male,Female}
	 String failedInputField=""; */
	//********************************************************
	//Request Parameters
	private String sProcessName;
	private String sSubProcess;
	private String sInitiateAlso;
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
	private Documents documentsObj;
	private Document documentObj[];
	private String sDocumentName;
	private String sDocumentType;
	private String sDocBase64;
	private String strCardProduct="";
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
	String sFilePath = "";
	JPISIsIndex NewIsIndex;
	JPDBRecoverDocData dataJPDB;
	String documentTag = "";
	String sDumpLocation = "";
	boolean sessionFlag = false;
	String processID = "";
	String trTableColumn = "";
	String trTableValue = "";
	String wiName = "";
	String externalTableName = "";
	String transactionTableName = "";
	String processDefID = "";
	String categoryId = "";
	String subCategoryId = "";
	String categoryName = "";
	String sTotalRetrieved = "";
	String sDate = "";
	String wsname = "";
	String cardST1 = "";
	String strAttributeTagForDOB="";
	//Others
	//private Logger log = Logger.getLogger(WICreateService.class);
	
	//CBR 
	String cardNo = "";
	String unmaskedCardNo = "";
	String cardST = "";
	String cashBackEA = "";
	String cashBackEAFromTable = "";
	String sCBRQuery = "";
	String cardStatus = "";
	String cardType = "";
	String cardHolderType = "";
	String IS_PRIME_UP = "";
	String reqCBAmount = "";
	String cashBackMinLimit = "";
	String ISSTP = "";
	String temp_winame = "";
	String amountHist = "";
	String encryptCardNo = "";
	//BT
	String btAmountReq = "";
	String typeOfBT = "";
	String btEligibleAmount = "";
	String otherBankCardNo = "";
	String otherBankCardType = "";
	String otherBankName = "";
	String BTQuery = "";
	String typeOfBT1 = "";
	String errorCodeDesc = "";
    String errorDesc = "";	
	//FundBlock Call Output
	String sAuthCode = "";
	String sCardType = "";
	String sSubRefNo = "";
	String sSubRefNoAuth = "";
	String sTranReqUID = "";
	String sApprovalCd = "";
	String sReturnCode = "";
	String sStatus = "";
	String typeOfError = "";
	String sMerchant = "";

	
	//CCC
	String cifId = "";
	String customerName = "";
	String mobileNo = "";
	String cardCRNNo = "";
	String salary = "";
	String agentNetworkId = "";
	String salesAgentId = "";
	String cardProduct = "";
	String cccAmount = "";
	String purpose = "";
	String paymentBy = "";
	String remarks = "";
	String availableBalance = "";
	String cashAvailableBalance = "";
	String branchName = "";
	String cardEligibility = "";
	String cardHolderName = "";
	String deliveryChannel = "";
	String cccEligibleAmount = "";
	String cardExpiryDate = "";
	String beneficiaryName = "";
	String marketingCode = "";
	String overdueamount = "";
	String purposeShortCode = "";
	String ccc_amount = "";
	
	// CAC Process
	String ApplicationType = "";
	String CustomerType = "";
	String IsPrimaryExisiting = "";
	String IsJointExisiting = "";
	String PrimaryCIFID = "";
	String JointCIFID = "";
	String PrimaryExistingCardNo = "";
	String MailingAddress = "";
	String POBOX = "";
	String PrimaryTitle = "";
	String PrimaryMMN = "";
	String PrimaryGender = "";
	String PrimaryResCountry = "";
	String PrimaryProfession = "";
	String PrimaryMonthInc = "";
	String PrimaryDocType = "";
	String PrimaryDocExpDate = "";
	String PrimaryDocNumber = "";
	String PrimaryMaritalStatus = "";
	String PrimaryEmpType = "";
	String JointTitle = "";
	String JointFirstName = "";
	String JointLastName = "";
	String JointMMN = "";
	String JointGender = "";
	String JointNationality = "";
	String JointDOB = "";
	String JointResCountry = "";
	String JointMobNoCCode = "";
	String JointMobNo = "";
	String JointProfession = "";
	String JointMonthInc = "";
	String JointDocType = "";
	String JointDocExpDate = "";
	String JointDocNumber = "";
	String JointMaritalStatus = "";
	String JointEmpType = "";
	String JointEmiratesID = "";
	String JointEmiratesIDExpDate = "";
	String SigningAuthority="";
	
	//for RMT process
	String OECDTag="";
	
	String WINAME="";
	String WI_create_date="";
	String prospect_id="";
	
	//for TT process
	String sInitiateFromActivityId = "";
    String sInitiateFromActivityName = "";
    String TT_ActivityName = "OPS_Initiate";
    String RepetitiveMainTags = "";
    
    String InputMessage = "";
    //for Firco Data in Digital AO Process
    String FircoAlertDetails = "";
    LinkedHashMap<String, HashMap<String, String>> hmMain = new LinkedHashMap();	
    
    static HashMap<String, String> hmExtMandIRBL = new HashMap();
    static HashMap<String, String> hmRptProcessIdIRBL = new HashMap();
    static HashMap<String, String> hmRptTransTableIRBL = new HashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndColIRBL = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndMandIRBL = new LinkedHashMap();
    
    //Hritik 5.4.22 - map for DAO
    String InputMessage1 = "";
    LinkedHashMap<String, HashMap<String, String>> hmMainDAO = new LinkedHashMap();	
    
    static HashMap<String, String> hmExtMandDAO = new HashMap();
    static HashMap<String, String> hmRptProcessIdDAO = new HashMap();
    static HashMap<String, String> hmRptTransTableDAO = new HashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndColDAO = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndMandDAO = new LinkedHashMap();
    
    LinkedHashMap<String, HashMap<String, String>> hmMainBAIS = new LinkedHashMap();	
    
    static HashMap<String, String> hmExtMandBAIS = new HashMap();
    static HashMap<String, String> hmRptProcessIdBAIS = new HashMap();
    static HashMap<String, String> hmRptTransTableBAIS = new HashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndColBAIS = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndMandBAIS = new LinkedHashMap();
    
    
    LinkedHashMap<String, HashMap<String, String>> hmMainDCC = new LinkedHashMap();	
    
    static HashMap<String, String> hmExtMandDCC = new HashMap();
    static HashMap<String, String> hmRptProcessIdDCC = new HashMap();
    static HashMap<String, String> hmRptTransTableDCC = new HashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndColDCC = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndMandDCC = new LinkedHashMap();
    
    //Reddy 7.6.22 BSR Process
    LinkedHashMap<String, HashMap<String, String>> hmMainBSR = new LinkedHashMap();	
    static HashMap<String, String> hmExtMandBSR = new HashMap();
    static HashMap<String, String> hmRptProcessIdBSR = new HashMap();
    static HashMap<String, String> hmRptTransTableBSR = new HashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndColBSR = new LinkedHashMap();
    static LinkedHashMap<String, HashMap<String, String>> hmRptAttrAndMandBSR = new LinkedHashMap();
    
	public WICreateResponse wiCreate(WICreateRequest request,String attributeList[]) throws WICreateException { 
		
			try
			{
				WriteLog("inside wiCreate 1");
				headerObjResponse=new EE_EAI_HEADER();
				//initialize Logger
				//initializeLogger();					
				
				//Load file configuration
				loadConfiguration();
				
				//Load ResourceBundle
				loadResourceBundle();
				
				//Fetching request parameters 
				fetchRequestParameters(request);
				
				//Validating Input Parameters and Check Attribute Name from USR_0_WSR_ATTRDETAILS
				validateRequestParameters(attributeList);
				
				// Validating Repetitive Tags
				if("OECD".equalsIgnoreCase(sProcessName))
					validateRepetitiveRequestParameters();
				//Added by Sowmya BAIS
				if("BAIS".equalsIgnoreCase(sProcessName))
				{
					validateRepetitiveRequestParametersBAIS();
				}
					
				
				if("iRBL".equalsIgnoreCase(sProcessName))
					validateRepetitiveRequestParametersIRBL();
				
				if("DigitalAO".equalsIgnoreCase(sProcessName))
				{
					WriteLog("DigitalAO: Prospect_id: "+hm.get("Prospect_id"));
					getworkitemfromduplicatetable_DAO(hm.get("Prospect_id"));
					
					if(WINAME.trim().equalsIgnoreCase("") || WINAME.trim().equalsIgnoreCase("null"))
					{
						WriteLog("DigitalAO: high_risk/validate_rep");
						check_high_risk();
						validateRepetitiveRequestParametersDAO();
						validateConditionalRequestParametersForDAO();
						String high_risk_flag = hm.get("risk_score_flag");
						
						// for income percentage
						String GrossMonthSalary = hm.get("Gross_Monthly_Salary_income");
						String MonthlyTurnoverCash = hm.get("Monthly_Expected_Turnover_Cash");
						String MonthylyTurnoverNonCash = hm.get("Monthly_Expected_Turnover_Non_Cash");
						
						double gMonthSalary = Double.parseDouble(GrossMonthSalary);
						double MonthSalaryCashDouble = Double.parseDouble(MonthlyTurnoverCash);
						double MonthSalaryNonCashDouble = Double.parseDouble(MonthylyTurnoverNonCash);
							
						double PercentageMonthlyTurnoverCash =(MonthSalaryCashDouble/gMonthSalary)*100;
						double PercentageMonthlyTurnoverNonCash = (MonthSalaryNonCashDouble/gMonthSalary)*100;
						
						WriteLog("PercentageMonthlyTurnoverCash :" + PercentageMonthlyTurnoverCash);
						WriteLog("PercentageMonthlyTurnoverNonCash :" + PercentageMonthlyTurnoverNonCash);
						
						double HighRangeValue =  (0.15*gMonthSalary)+ gMonthSalary;
						double LowRangeValue = gMonthSalary - (0.15*gMonthSalary) ;
						
						WriteLog("HighRangeValue :" + HighRangeValue);
						WriteLog("LowRangeValue :" + LowRangeValue);
						
						// for percentage
						attributeTag = attributeTag + "PercentageMonthlyExpectedTurnoverCash" + (char)21 +PercentageMonthlyTurnoverCash+(char)25;
						attributeTag = attributeTag + "PercentageMonthlyExpectedTurnoverNonCash" + (char)21 +PercentageMonthlyTurnoverNonCash+(char)25;
						// for high and low value
						attributeTag = attributeTag + "high_range_value" + (char)21 +HighRangeValue+(char)25;
						attributeTag = attributeTag + "low_range_value" + (char)21 +LowRangeValue+(char)25;
						
						WriteLog("high_risk_flag:"+high_risk_flag);
						attributeTag=attributeTag+"high_risk"+(char)21+high_risk_flag+(char)25;
						WriteLog("high_risk_flag + attributeTag:"+attributeTag);
						
						 // bckgrnd info tag mandatory is high risk
						if(high_risk_flag.equalsIgnoreCase("Y") && ("SALARIED".equalsIgnoreCase(hm.get("employer_type")) && !InputMessage.contains("<Background_info_employer>")))
						{
							throw new WICreateException("7027",pCodes.getString("7027")+" for process id: "+processID);
						}
						
						
						String firco_hit_flag="N";
						if(InputMessage.contains("Suspect detected #1"))
						{
							firco_hit_flag="Y";
						}
						attributeTag=attributeTag+"firco_hit"+(char)21+firco_hit_flag+(char)25;
						WriteLog("firco_hit + attributeTag:"+attributeTag);
						
						/*
						if(hm.containsKey("free_field_1") && hm.get("free_field_1") != null && !"".equalsIgnoreCase(hm.get("free_field_1"))) // ECRN Number
						{
							attributeTag=attributeTag+"is_prime_req"+(char)21+"Y"+(char)25;
						}
						else
						{
							attributeTag=attributeTag+"is_prime_req"+(char)21+"N"+(char)25;
						}
						WriteLog("is_prime_req + attributeTag:"+attributeTag);
						*/
						
						if(hm.containsKey("ChequeBk_Req") && hm.get("ChequeBk_Req") != null && ("Y".equalsIgnoreCase(hm.get("ChequeBk_Req")) || "Yes".equalsIgnoreCase(hm.get("ChequeBk_Req"))) ) // Cheque book ref Number
						{
							attributeTag=attributeTag+"is_cbs_req"+(char)21+"Y"+(char)25;
						}
						else
						{
							attributeTag=attributeTag+"is_cbs_req"+(char)21+"N"+(char)25;
						}
						WriteLog("only is_cbs_req is getting set from ChequeBk_Req + "+attributeTag);
						
	    				}
					else
					{
						WriteLog("returnCode: "+3335+" returnDesc: "+pCodes.getString("3335"));
    					setFailureParamResponse();
    					response.setWorkitemNumber(WINAME);
    					response.setErrorCode("3335");
    					response.setErrorDescription(pCodes.getString("3335"));
    					return response;
					}
					
				} //Hritik 5.4.22 Digital AO Process
				
				/** Ravindra Kumar -- Validate parameters  for dcc **/
				if("Digital_CC".equalsIgnoreCase(sProcessName))
				{
					//					changes to validate duplicate workitem based on Prospect_id
					WriteLog("DigitalCC: Prospect_id: "+hm.get("Prospect_id"));
					getworkitemfromduplicatetable_DAO(hm.get("Prospect_id"));

					if(WINAME.trim().equalsIgnoreCase("") || WINAME.trim().equalsIgnoreCase("null"))
					{
						validateRepetitiveRequestParametersDCC();
						validateConditionalRequestParametersForDCC();
						if(hm.containsKey("Statement_Analyser_Flag") && hm.get("Statement_Analyser_Flag") != null && "F".equalsIgnoreCase(hm.get("Statement_Analyser_Flag"))) // Cheque book ref Number
						{
							attributeTag=attributeTag+"FTS_Ack_flg"+(char)21+"Y"+(char)25;
						}
						else
						{
							attributeTag=attributeTag+"FTS_Ack_flg"+(char)21+"N"+(char)25;
						}

						WriteLog(" Set FTS_Ack_flg according to FTS_FLAG :- " +attributeTag);
					}
					else
					{
						WriteLog("returnCode: "+3335+" returnDesc: "+pCodes.getString("3335"));
						setFailureParamResponse();
						response.setWorkitemNumber(WINAME);
						response.setErrorCode("3335");
						response.setErrorDescription(pCodes.getString("3335"));
						return response;
					}

				}
				//Reddy 07.06.22 BSR Process
				if("BSR".equalsIgnoreCase(sProcessName))
				{
					validateRepetitiveRequestParametersBSR();
					//validateConditionalRequestParametersForBSR();
				}
				//Checking existing session
				checkExistingSession();
				
				//Check for sub process name
            	if("Cash Back Redemption".equalsIgnoreCase(sSubProcess))
            		subProcCBR();
            	
            	if("Balance Transfer".equalsIgnoreCase(sSubProcess))
            		subProcBT();
            	
            	if("Credit Card Cheque".equalsIgnoreCase(sSubProcess))
            		subProcCCC();
            	
            	if ("SRM".equalsIgnoreCase(sProcessName))
            	{	
	            	//Check if all Mandatory Document attributes present in USR_0_WSR_DOCTYPEDETAILS have come 
	            	checkMandatoryDocDetails();
	            	                	
	            	//Download Input Documents at Server and add them to SMS
	            	downloadDocument();
            	}
            	
            	if("DAC".equalsIgnoreCase(sProcessName))
            	{
            		attributeTag=attributeTag+"WI_ORIGIN"+(char)21+"Third_Party"+(char)25;
            	}
            	
            	//Checking duplicate based on message id -For RMT Only
            	if ("RMT".equalsIgnoreCase(sProcessName))
				{
            		getworkitemfromduplicatetable();
            		if(WINAME.trim().equalsIgnoreCase("") || WINAME.trim().equalsIgnoreCase("null"))
    				{		
    					//it will go for workitem creation
            			runWICall();
    				}
    				else
    				{
    					WriteLog("returnCode: "+3333+" returnDesc: "+pCodes.getString("3333"));
    					setFailureParamResponse();
    					response.setWorkitemNumber(WINAME);
    					response.setErrorCode("3333");
    					response.setErrorDescription(pCodes.getString("3333"));
    					return response;
    				}
				}
            	//********************************************************
            	//Checking duplicate based on IBAN Number For RAOP Only added on 31052020
            	else if ("RAOP".equalsIgnoreCase(sProcessName))
				{
            		String existingDetails = getRAOPWIStageBasedOnIBANNumber(hm.get("IBANNUMBER"));
            		String sCurrentWS = "";
            		if(!"".equalsIgnoreCase(existingDetails.trim()))
            		{
            			String arrExistingDetails[] = existingDetails.split("~");
            			WINAME=arrExistingDetails[0];
            			sCurrentWS=arrExistingDetails[1];
            		}
            		
            		if("Discard".equalsIgnoreCase(sCurrentWS.trim()) || "".equalsIgnoreCase(sCurrentWS.trim()))
    				{		
    					//If existing workitem is rejected or new case then only it will go for workitem creation
            			runWICall();
    				}
					else
    				{
    					WriteLog("returnCode: "+3334+" returnDesc: "+pCodes.getString("3334"));
    					setFailureParamResponse();
    					response.setWorkitemNumber(WINAME);
    					response.setErrorCode("3334");
    					response.setErrorDescription(pCodes.getString("3334"));
    					return response;
    				}
            		//runWICall();
				}            	
            	//********************************************************
            	else {
	            	//Run WFUploadWorkItem Call and put data in external and transaction table 
            		if("CDOB".equalsIgnoreCase(sProcessName)){
            			validateConditionalRequestParametersForDOB();
            			sInitiateAlso = "N";
            			attributeTag="";
            		}
					//Inclusion of TT Process from YAP
					if("TT".equalsIgnoreCase(sProcessName))
					{
						getActivityID(sProcessName,TT_ActivityName);
						attributeTag=attributeTag+"CREATE_WI_STATUS"+(char)21+"Y"+(char)25;
					}
	            	runWICall();
					
	            	if("CDOB".equalsIgnoreCase(sProcessName)){
	            		WriteLog("The workitem number created for CDOB is "+response.getWorkitemNumber());
	            		getAttributeTagForDOB();
	            		appendAdditionalFields();
            			appendConditionalRequestParametersForDOB();
            			saveWorkItemForCDOB();
	            	}
            	}
            	
				if ("SRM".equalsIgnoreCase(sProcessName))
				{
            		//Entry into usr_0_srm_wihistory table
            		insertIntoHistory();
            	}
				
            	if ("CAC".equalsIgnoreCase(sProcessName))
            	{
            		//Entry into usr_0_cac_wihistory table
            		if (!"".equalsIgnoreCase(MailingAddress)) {
            			insertForMailingAddress();
            		}
            		insertIntoHistoryCAC();
            	} 
            	if ("RMT".equalsIgnoreCase(sProcessName))
            	{
            		//Inserting in Duplicate Workitem Table
            		insertIntoDuplicateTableRMT();
            		//Entry into usr_0_rmt_wihistory table
            		insertIntoHistoryRMT();
            		insertIntoOECDGridRMT();
            	} 
				if ("OECD".equalsIgnoreCase(sProcessName))
            	{
            		//Entry into usr_0_oecd_wihistory table
            		
            		insertIntoHistoryOECD();
            		insertIntoOECDGridOECD();
            		InsertRecordsForRepetitiveTags();
       
            	}
				if ("BAIS".equalsIgnoreCase(sProcessName))
            	{
					insertIntoHistoryBAIS(); //Entry into history table
					InsertRecordsForRepetitiveTagsBAIS();
					
            	} 
				if ("TWC".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryTWC(); //Entry into history table
            	}
				if ("RAOP".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryRAOP(); //Entry into history table
            		insertIntoTaxGridRAOP(); //Entry into OECD grid
            	} 
				if ("CMP".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryCMP(); //Entry into history table       
            	} 
				if ("CBP".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryCBP(); //Entry into history table       
            	}
				if ("DAC".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryDAC(); //Entry into history table       
            	}
				if ("ACP".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryACP(); //Entry into history table       
            	}
				if ("TT".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryTT(); //Entry into history table       
            	}
				if ("iRBL".equalsIgnoreCase(sProcessName))
            	{
            		insertIntoHistoryIRBL(); //Entry into history table   
            		InsertRecordsForRepetitiveTagsIBPS();
            	}
				
				if("DigitalAO".equalsIgnoreCase(sProcessName))
				{
					insertIntoHistoryDAO();
					InsertRecordsForRepetitiveTagsDAO();
				} //Hritik 5.4.22 Digital AO Process
				
				if("Digital_CC".equalsIgnoreCase(sProcessName))
				{
					//NG_RLOS_FIRCO
					insertIntoFircoTable();
					insertIntoHistoryDCC();
					InsertRecordsForRepetitiveTagsDCC();
				}
				//Reddy 7.6.22 BSR Process
				if("BSR".equalsIgnoreCase(sProcessName))
				{
					insertIntoHistoryBSR();
					InsertRecordsForRepetitiveTagsBSR();
				}

          	}
			catch(WICreateException e)
			{
				WriteLog("WICreateException caught:Message- "+e.getMessage());
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
				hmMain.clear();
				//hmRptAttrAndColIRBL.clear();
				//hmRptAttrAndMandIRBL.clear();
				//hmExtMandIRBL.clear();
				hmRptProcessIdIRBL.clear();
				hmRptTransTableIRBL.clear();
			}
			return response;
		
	}
	
	//Added By Sajan for FALCON CDOB
	public void ifCustomerExistInCAS()throws WICreateException,Exception{
		String sQuery="select count(ext.CC_Wi_Name) as Count from "+CDOB_EXTTABLE+" ext with(nolock),"+Customer_Fragment_Table+" cust with(nolock) where cust.FirstName='"+hm.get("FirstName")+"' and cust.LAstName='"+hm.get("LastName")+"' and cust.MobileNo='"+hm.get("MobileNo_pri")+"' and cust.Email_ID='"+hm.get("Email_ID")+"' and cust.wi_name=ext.CC_Wi_Name";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		//Check Main Code
		checkCallsMainCode(xmlobj); 
		String strCount=xmlobj.getValueOf("Count");
		WriteLog("Workitem number: "+WINAME);
		WriteLog("Message id of the request"+sMessageId);
		
		if(Integer.parseInt(strCount)>0)
			throw new WICreateException("6011",pCodes.getString("6011"));
	}
	
	
	//Added by Sajan for FALCON DOB
	private void getAttributeTagForDOB()throws Exception{
		String fileLocation=new StringBuffer().append(System.getProperty("user.dir"))
		.append(System.getProperty("file.separator")).append("BPMCustomWebservicesConf")
		.append(System.getProperty("file.separator")).append("WFUploadDOB.txt").toString();
		BufferedReader sbf=new BufferedReader(new FileReader(fileLocation));
		
		StringBuilder sb=new StringBuilder();
		String line=sbf.readLine();
		while(line!=null){
			sb.append(line);
			sb.append(System.lineSeparator());
			line=sbf.readLine();
		}
		strAttributeTagForDOB=sb.toString();
		for(Map.Entry<String, String> entry:hm.entrySet()){
			WriteLog("The key is "+entry.getKey());
			WriteLog("The value is "+entry.getValue());
			if(!"Product_Type".equalsIgnoreCase(entry.getKey()) && !"Emp_Type".equalsIgnoreCase(entry.getKey()))
				strAttributeTagForDOB=strAttributeTagForDOB.replace(">"+entry.getKey()+"<", ">"+entry.getValue()+"<");
			else{
				if("Product_Type".equalsIgnoreCase(entry.getKey())){
				String strprodType=entry.getValue().trim().charAt(0)+"";
				strCardProduct=entry.getValue().substring(entry.getValue().indexOf("-")+1);
				strAttributeTagForDOB=strAttributeTagForDOB.replace(">Card_Product<",">"+getCardProducrFromMaster()+"<");
				if("I".equalsIgnoreCase(strprodType)){
					
					strAttributeTagForDOB=strAttributeTagForDOB.replace(">Product_Type<", ">Islamic<");
					strAttributeTagForDOB=strAttributeTagForDOB.replace(">loantype<", ">Islamic<");
				}
				else
					strAttributeTagForDOB=strAttributeTagForDOB.replace(">Product_Type<", ">Conventional<");
					strAttributeTagForDOB=strAttributeTagForDOB.replace(">loantype<", ">Conventional<");
			}
				else if("Emp_Type".equalsIgnoreCase(entry.getKey())){
					if("Salaried".equalsIgnoreCase(entry.getValue())){
						strAttributeTagForDOB=strAttributeTagForDOB.replace(">Emp_Type<",">S<");
					}
					else{
						strAttributeTagForDOB=strAttributeTagForDOB.replace(">"+entry.getKey()+"<", ">"+entry.getValue()+"<");
					}
				}
			}
		}
		DateFormat for3 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date curr_date = new Date();
		strAttributeTagForDOB=strAttributeTagForDOB.replace("WI_Creatio_Date",for3.format(curr_date));
		strAttributeTagForDOB=strAttributeTagForDOB.replace("WorkItemNumber", response.getWorkitemNumber());
	}
	
	
	private void getworkitemfromduplicatetable() throws Exception
	{
		String sQuery="SELECT WINAME FROM "+RMT_DUPLICATE+" with (nolock) where messageid ='"+sMessageId+"' and processname='"+sProcessName+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		//Check Main Code
		checkCallsMainCode(xmlobj); 
		WINAME=xmlobj.getValueOf("WINAME");
		WriteLog("Workitem number: "+WINAME);
		WriteLog("Message id of the request"+sMessageId);
		
	}
	
	// select WI_name,WI_create_date,prospect_id from NG_DAO_EXTTABLE with (nolock) where prospect_id=''

	private void getworkitemfromduplicatetable_DAO(String prospect) throws Exception
	{
		String sQuery="select WI_name,WI_create_date,prospect_id from NG_DAO_EXTTABLE with (nolock) where prospect_id='"+prospect+"' union select WI_name,entry_date_time,prospect_id from NG_DCC_EXTTABLE with (nolock) where prospect_id='"+prospect+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		//Check Main Code
		checkCallsMainCode(xmlobj); 
		WINAME=xmlobj.getValueOf("WI_name");
		WI_create_date=xmlobj.getValueOf("WI_create_date");
		prospect_id=xmlobj.getValueOf("prospect_id");
		WriteLog("Workitem number: "+WINAME);
		WriteLog("WI_create_date of the request"+WI_create_date);
		WriteLog("prospect_id of the request"+prospect_id);
	}
	

	
	
	private String getRAOPWIStageBasedOnIBANNumber(String IBANNumber) throws WICreateException,Exception
	{
		String sQuery="select top 1 WINAME,CURRENT_WS from RB_RAOP_EXTTABLE with (nolock) where IBANNO='"+IBANNumber+"' order by CREATED_AT desc";
		String InputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input XML to Get RAOP WI Stage based on IBAN Number: "+InputXML);
		String OutputXML=executeAPI(InputXML);
		WriteLog("Output XML to Get RAOP WI Stage based on IBAN Number: "+OutputXML);
		xmlobj=new XMLParser(OutputXML);
		checkCallsMainCode(xmlobj);
		int iTotalRetrieved = Integer.parseInt(getTagValues(OutputXML, "TotalRetrieved"));
		if (iTotalRetrieved == 1)
		{	
			String sWINAME=getTagValues(OutputXML, "WINAME");
			String sCurrentWS=getTagValues(OutputXML, "CURRENT_WS");
			WriteLog("RAOP WI Stage based on IBAN Number from External Table: "+sWINAME+"~"+sCurrentWS);
			return sWINAME+"~"+sCurrentWS;
		}
		return "";
	}
	
	//Added by Sajan for FALCON CDOB
	public void saveWorkItemForCDOB()throws WICreateException,Exception{
		
		
		sInputXML=getWorkItemInput(response.getWorkitemNumber());
		WriteLog("Inpout xml for get WI is "+sInputXML);
		String getWorkItemOutputXml = executeAPI(sInputXML);
		WriteLog("Output XML For WmgetWorkItemCall: "+ getWorkItemOutputXml);
		xmlobj=new XMLParser(getWorkItemOutputXml);
		checkCallsMainCode(xmlobj);
		
		//sInputXML=getAssignAttributeInputXML();
		sInputXML=getAssignAttributeInputXML().replace("&", "&amp;");
		WriteLog("input XML for set attribute is "+sInputXML);
		String outputXMLforAssignAttribute=executeAPI(sInputXML);
		WriteLog(outputXMLforAssignAttribute);
		xmlobj=new XMLParser(outputXMLforAssignAttribute);
		checkCallsMainCode(xmlobj);
		
		/*sInputXML=getUnlockWorkitemInputXML();
		String outputXMLforUnlockWorkItem=executeAPI(sInputXML);
		xmlobj=new XMLParser(outputXMLforUnlockWorkItem);
		checkCallsMainCode(xmlobj);*/
		
		insertEligibleCardProductData();
		insertDefaultCardProduct();
	}
	
	//Added by Sajan for FALCON CDOB
	private void insertEligibleCardProductData()throws WICreateException,Exception{
		String strCardProduct=hm.get("EligibleCardProduct");
		String[] arrCardProduct=strCardProduct.split("~");
		trTableColumn="Product,Card_Product,CASCalculatedlimit,Eligible_Card,Final_limit,Decision,Declined_Reason,Deviation_Code_Refer,Delegation_Authorithy,Score_Grade,wi_name,Child_Wi";   //Changes needed Sajan
		for(int p=0;p<arrCardProduct.length;p++){
			String[] strProduct=arrCardProduct[p].split("\\|");
			trTableValue="'"+strProduct[0]+"','"+strProduct[1]+"','"+strProduct[2]+"','"+strProduct[3]+"','"+strProduct[4]+"','"+strProduct[5]+"','"+strProduct[6]+"','"+strProduct[7]+"','"+strProduct[8]+"','"+strProduct[9]+"','"+response.getWorkitemNumber()+"','"+response.getWorkitemNumber()+"'";
			String inputXML=getInputXMLInsert(CARD_PRODUCT);
			WriteLog("APInsert Input History: "+inputXML);
			sOutputXML=executeAPI(inputXML);
			WriteLog("APInsert Output History: "+sOutputXML);
	    	xmlobj=new XMLParser(sOutputXML);
	    	//Check Main Code
			checkCallsMainCode(xmlobj);  
			
		}
	}
	
	private void insertDefaultCardProduct() throws WICreateException,Exception{
		//String strCardProduct=hm.get("Product_Type").substring(hm.get("Product_Type").indexOf("-")+1);
		//For Credit Shield change
		trTableColumn="Card_Product,Eligible_Limit,Final_Limit,wi_name,Flag,Child_Wi,combined_limit,CC_Waiver,Cardproductselect,CardShield";
		String strCardProduct=getCardProducrFromMaster();
			//For Credit Shield change
		String CardShield_val = "false";
		if(hm.containsKey("Credit_Shield") && "true".equalsIgnoreCase(hm.get("Credit_Shield"))){
			CardShield_val = "true";
		}
		trTableValue="'"+strCardProduct+"','"+hm.get("ApprovedLimit")+"','','"+response.getWorkitemNumber()+"','N','"+response.getWorkitemNumber()+"','1','false','true','"+CardShield_val+"'";
		String inputXML=getInputXMLInsert(CARD_LIMIT);
		WriteLog("APInsert Input History: "+inputXML);
		sOutputXML=executeAPI(inputXML);
		WriteLog("APInsert Output History: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj);  
	}
	
	//Added by Sajan to get card product from master
	private String getCardProducrFromMaster()throws WICreateException,Exception{
		String strCardProduct="";
		String strQuery="";
		if("AE".equalsIgnoreCase(hm.get("Nationality")))
			strQuery="select CARD_PRODUCT from CDOB_CARDPROUDCT with(nolock) where CDOB_PRODUCT='"+hm.get("Product_Type")+"' and NATIONALITY='AE'";
		else
			strQuery="select CARD_PRODUCT from CDOB_CARDPROUDCT with(nolock) where CDOB_PRODUCT='"+hm.get("Product_Type")+"' and NATIONALITY='All'";
		sInputXML = getAPSelectWithColumnNamesXML(strQuery);
		WriteLog("Get Card Product Input xml: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("Get Card Product Output xml: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		strCardProduct = getTagValues(sOutputXML, "CARD_PRODUCT");
		WriteLog("CardProduct: " + strCardProduct);
		CardProductForDesc=strCardProduct;
		return strCardProduct;
	}
	
	
	private String getCardDescFromProduct() throws WICreateException,Exception{
		String strCardProductDesc="";
		String strQuery="";
		strQuery="select top 1 DESCRIPTION from ng_master_cardproduct with(nolock) where CODE='"+CardProductForDesc+"'";
		sInputXML = getAPSelectWithColumnNamesXML(strQuery);
		WriteLog("Get Card Product Description  Input xml: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("Get Card Product Description Output xml: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		strCardProductDesc = getTagValues(sOutputXML, "DESCRIPTION");
		WriteLog("CardProductDescription : " + strCardProductDesc);
		//cardProduct=strCardProduct;
		return strCardProductDesc;
	}
	
	private String getInputXMLInsert(String tableName)
	{
		 return "<?xml version=\"1.0\"?>" +
	            "<APInsert_Input>" +
				"<Option>APInsert</Option>" +
				"<TableName>" + tableName + "</TableName>" +
				"<ColName>" + trTableColumn + "</ColName>" +
				"<Values>" + trTableValue + "</Values>" +
				"<EngineName>" + sCabinetName + "</EngineName>" +
				"<SessionId>" + sSessionID + "</SessionId>" +
	            "</APInsert_Input>";
	}
	//Added by Sajan for FALCON CDOB
	private String getUnlockWorkitemInputXML(){
		String inputXML="<?xml version=\"1.0\"?>"+"<WMUnlockWorkItem_Input><Option>WMUnlockWorkItem</Option>" +
				"<EngineName>"+sCabinetName+"</EngineName><SessionId>"+sSessionID+"</SessionId>" +
				"<ProcessInstanceID>"+response.getWorkitemNumber()+"</ProcessInstanceID><WorkItemID>1</WorkItemID>" +
				"<UnlockOption>W</UnlockOption></WMUnlockWorkItem_Input>";
		return inputXML;
	}
	
	//Added by Sajan for FALCON CDOB
	private String getAssignAttributeInputXML(){
		String inputXML="<?xml version=\"1.0\"?>"+"<WMAssignWorkItemAttributes_Input><Option>WMAssignWorkItemAttributes</Option>" +
				"<EngineName>"+sCabinetName+"</EngineName><SessionId>"+sSessionID+"</SessionId>" +
				"<ProcessInstanceId>"+response.getWorkitemNumber()+"</ProcessInstanceId><WorkitemId>1</WorkitemId>" +
				"<ActivityId>17</ActivityId><ProcessDefId>"+processDefID+"</ProcessDefId><ActivityType>1</ActivityType>" +
				"<UserDefVarFlag>Y</UserDefVarFlag><LastModifiedTime></LastModifiedTime><complete>I</complete>" +
				"<Attributes>"+strAttributeTagForDOB+"</Attributes></WMAssignWorkItemAttributes_Input>";
		return inputXML;
	}
	
	public  String getWorkItemInput(String workitemNumber){
		return "<?xml version=\"1.0\"?>" + "<WMGetWorkItem_Input>"
				+ "<Option>WMGetWorkItem</Option>" + "<ProcessInstanceId>" + workitemNumber
				+ "</ProcessInstanceId>" + "<WorkItemId>1</WorkItemId>"
				+ "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</WMGetWorkItem_Input>";
	}
	
	private void insertIntoDuplicateTableRMT()throws WICreateException,Exception {
		WriteLog("Inside insertIntoDuplicateTable");
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sCurrDate = dateFormat.format(d);
	
		trTableColumn = "processname,winame,currentdatetime,messageid";
		WriteLog("duplicatecolumn" + trTableColumn);
		//trTableValue = ""+wiName+","+ element + "";
		
		trTableValue = "'"+sProcessName+"','"+response.getWorkitemNumber()+"','"+sCurrDate+"','"+sMessageId+"'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDuplicateInsertRMT();
		WriteLog("APInsert Input RMT insert for duplicate request: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output RMT insert for duplicate request: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	
	}
	
	private void appendAdditionalFields() throws WICreateException,Exception{
		String strGender="";
		StringBuffer sbf=new StringBuffer(strAttributeTagForDOB);
		sbf.insert(sbf.indexOf("<q_Customer>")+"<q_Customer>".length(), "<Shortname>"+hm.get("FirstName").charAt(0)+" "+hm.get("LastName").charAt(0)+"</Shortname>");
		if("MR.".equalsIgnoreCase(hm.get("Title")))
			strGender="M";
		else if("MRS.".equalsIgnoreCase(hm.get("Title")))
			strGender="F";
		else
			strGender="F";
		
		String strBankDesc=getBankDescFromCode();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = sdf.format(cal.getTime());
		WriteLog("The Current Date is "+strDate);
		cal.add(Calendar.MONTH,-4);
		String strFTSToDate=sdf.format(cal.getTime());
		WriteLog("The FTS to date is "+strFTSToDate);
		
		sbf.insert(sbf.indexOf("<q_Customer>")+"<q_Customer>".length(),"<gender>"+strGender+"</gender>");
		sbf.insert(sbf.indexOf("<q_Customer>")+"<q_Customer>".length(),"<RESIDENTNONRESIDENT>Y</RESIDENTNONRESIDENT>");
		sbf.insert(sbf.indexOf("<q_Customer>")+"<q_Customer>".length(),"<COUNTRYOFRESIDENCE>AE</COUNTRYOFRESIDENCE>");
		//sbf.insert(sbf.indexOf("<q_Liabilities>")+"<q_Liabilities>".length(),"<Bank_Name>"+getBankDescFromCode()+"</Bank_Name>");
		sbf.insert(sbf.indexOf("<q_Liabilities>")+"<q_Liabilities>".length(),"<ConsentDate>"+strDate+"</ConsentDate><FTSFromDate>"+strFTSToDate+"</FTSFromDate><FTSToDate>"+strDate+"</FTSToDate><Bank_Name>"+strBankDesc+"</Bank_Name>");
		sbf.insert(sbf.indexOf("<cmplx_GR_FatcaDetails>")+"<cmplx_GR_FatcaDetails>".length(), "<customerType>P-"+hm.get("FirstName").toUpperCase()+" "+hm.get("LastName").toUpperCase()+"</customerType>");
		sbf.insert(sbf.indexOf("<cmplx_KYCGrid>")+"<cmplx_KYCGrid>".length(), "<CustomerType>P-"+hm.get("FirstName").toUpperCase()+" "+hm.get("LastName").toUpperCase()+"</CustomerType>");
		//Deepak Changes to save customer type in OECD
		sbf.insert(sbf.indexOf("<cmplx_GR_OecdDetails>")+"<cmplx_GR_OecdDetails>".length(), "<CustomerType>P-"+hm.get("FirstName").toUpperCase()+" "+hm.get("LastName").toUpperCase()+"</CustomerType>");
		sbf.insert(sbf.indexOf("<cmplx_AddressGrid>")+"<cmplx_AddressGrid>".length(), "<CustomerType>P-"+hm.get("FirstName").toUpperCase()+" "+hm.get("LastName").toUpperCase()+"</CustomerType>");
		sbf.insert(sbf.indexOf("<AddrType>OFFICE</AddrType>")+"<AddrType>OFFICE</AddrType>".length(), "<CustomerType>P-"+hm.get("FirstName").toUpperCase()+" "+hm.get("LastName").toUpperCase()+"</CustomerType>");
		sbf.insert(sbf.indexOf("<MultipleApplicantsGrid>")+"<MultipleApplicantsGrid>".length(),"<ApplicantName>"+hm.get("FirstName").toUpperCase()+" "+hm.get("LastName").toUpperCase()+"</ApplicantName>");
		if(hm.containsKey("MiddleName") && !"".equals(hm.get("MiddleName"))){
			sbf.insert(sbf.indexOf("</PORTAL_REF_NUMBER>")+"</PORTAL_REF_NUMBER>".length(), "<CUSTOMERNAME>"+hm.get("FirstName")+" "+hm.get("MiddleName")+" "+hm.get("LastName")+"</CUSTOMERNAME>");
		}
		else{
			sbf.insert(sbf.indexOf("</PORTAL_REF_NUMBER>")+"</PORTAL_REF_NUMBER>".length(), "<CUSTOMERNAME>"+hm.get("FirstName")+" "+hm.get("LastName")+"</CUSTOMERNAME>");
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal1=Calendar.getInstance();
		WriteLog("The current date is "+cal1.getTime());
		
		sbf.insert(sbf.indexOf("</PORTAL_REF_NUMBER>")+"</PORTAL_REF_NUMBER>".length(),"<Card_Product>"+getCardDescFromProduct()+"</Card_Product><PassportNo>"+hm.get("PAssportNo")+"</PassportNo><MobileNo>"+hm.get("MobileNo_pri")+"</MobileNo><Employer_Name>"+hm.get("EmpName")+"</Employer_Name><Introduction_Date>"+dateFormat.format(cal1.getTime())+"</Introduction_Date>");
		strAttributeTagForDOB=sbf.toString().replace(">EmpType<", ">"+hm.get("Emp_Type")+"<");
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
	
	private String getDuplicateInsertRMT()
	{
		 return "<?xml version=\"1.0\"?>" +
	            "<APInsert_Input>" +
				"<Option>APInsert</Option>" +
				"<TableName>" + RMT_DUPLICATE + "</TableName>" +
				"<ColName>" + trTableColumn + "</ColName>" +
				"<Values>" + trTableValue + "</Values>" +
				"<EngineName>" + sCabinetName + "</EngineName>" +
				"<SessionId>" + sSessionID + "</SessionId>" +
	            "</APInsert_Input>";
	}
	
	private void runWICall() throws WICreateException,Exception
	{
		sInputXML=getWFUploadWorkItemXML();
		WriteLog("WFUploadWorkItemXML Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("WFUploadWorkItemXML Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj);  
		wiName=getTagValues(sOutputXML,"ProcessInstanceId");
		WriteLog("WINAME: "+wiName);
		//Update associated trans table
		if(sSubProcess.equalsIgnoreCase("Cash Back Redemption"))
		{
			WriteLog("runwicall::sSubprocess--"+sSubProcess);
			insertIntoTransTableCBR();
		}
		else if(sSubProcess.equalsIgnoreCase("Balance Transfer"))
		{
			WriteLog("runwicall::sSubprocess--"+sSubProcess);
			insertIntoTransTableBT();
		}
		else if(sSubProcess.equalsIgnoreCase("Credit Card Cheque"))
		{
			WriteLog("runwicall::sSubprocess--"+sSubProcess);
			insertIntoTransTableCCC();
		}
		setSuccessParamResponse();
		response.setWorkitemNumber(wiName);
		
	}
	//Method to get branch name from RB_BRANCH_MASTER by branch code
	private String getBranchNameByBranchCode(String branchCode) throws WICreateException  {
		String branchName="";
		sInputXML = getAPSelectWithColumnNamesXML("SELECT BRANCH_NAME FROM RB_BRANCH_MASTER with(nolock) WHERE SOLEID='"+branchCode+"'");
		WriteLog("Get Branch Name Input xml: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("Get Branch Name Output xml: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
		branchName = getTagValues(sOutputXML, "BRANCH_NAME");
		WriteLog("BranchName: " + branchName);
		return branchName;
	}//************************************************************	
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
	private void insertIntoHistory() throws WICreateException, Exception
	{
		
		trTableColumn="catIndex,subCatIndex,winame,wsname,actual_wsname,decision,actiondatetime,remarks,username";
		WriteLog("trTableColumn"+trTableColumn);
		trTableValue="'"+categoryId+"','"+subCategoryId+"','"+wiName+"','Introduction','PBO','Introduce','"+sDate+"','Workitem created through webservice','"+sUsername+"'";
		WriteLog("trTableValue"+trTableValue);
		sInputXML=getDBInsertInputHistory();
		WriteLog("APInsert Input History: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("APInsert Output History: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj);  
	}
	private void insertIntoHistoryCAC() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime,checklistdata";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Introduction','Introduce','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "',NULL";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryCAC();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	private void insertIntoHistoryRMT() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Introduction','SUBMIT','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryRMT();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryOECD() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Introduction','SUBMIT','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryOECD();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryBAIS() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Introduction','SUBMIT','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryBAIS();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryTWC() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','SUBMIT','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryTWC();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryRAOP() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		//trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		trTableColumn = "wi_name,workstep,decision,action_date_time,remarks,user_name,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryRAOP();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoOECDGridRMT() throws WICreateException, IOException, Exception {
		WriteLog("Inside insertIntoOECDGridRMT");
		
		
		// Fetch CardNo, CategoryID, SubCategoryID
		WriteLog("REPORTCOUNTRYDETAILS exists check: "+hm.containsKey("REPORTCOUNTRYDETAILS"));
		if (hm.containsKey("REPORTCOUNTRYDETAILS"))
		{
			OECDTag = hm.get("REPORTCOUNTRYDETAILS");
			WriteLog("REPORTCOUNTRYDETAILS exists");
		}
		 
		if (!OECDTag.trim().equalsIgnoreCase(""))
		{
			String[] oecd = OECDTag.split("\\|");
			for(int k =0;k<oecd.length;k++) 
			{
				String oecddetail[] = oecd[k].split("~",-1);
				WriteLog("notinreason"+oecddetail[2]);
				String element="";
				for (int i = 0; i < oecddetail.length; i++) {
					WriteLog("column1 "+oecddetail[0]);
					WriteLog("column3 "+oecddetail[2]);
					if(element.equals(""))
						element="'"+oecddetail[i]+"'";
					else
						element+=",'"+oecddetail[i]+"'";
					WriteLog("element"+element);
				}
				
				trTableColumn = "WINAME,COUNTRY_OF_TAX_RESIDENCE,TAX_PAYER_IDENTIFICATION_NO,NO_TIN_REASON,REMARKS";
				WriteLog("trTableColumn" + trTableColumn);
				//trTableValue = ""+wiName+","+ element + "";
				
				trTableValue = "'"+wiName+"',"+ element + "";
				WriteLog("trTableValue" + trTableValue);
				sInputXML = getDBInsertOECDDetailRMT();
				WriteLog("APInsert Input RMT insertForOECDGrid: " + sInputXML);
				sOutputXML = executeAPI(sInputXML);
				WriteLog("APInsert Output RMT insertForOECDD: " + sOutputXML);
				xmlobj = new XMLParser(sOutputXML);
				// Check Main Code
				checkCallsMainCode(xmlobj);
			}
		}
	}
	private void insertIntoOECDGridOECD() throws WICreateException, IOException, Exception {
		WriteLog("Inside insertIntoOECDGridOECD");
		
		
		// Fetch CardNo, CategoryID, SubCategoryID
		if (hm.containsKey("REPORTCOUNTRYDETAILS"))
		{
			OECDTag = hm.get("REPORTCOUNTRYDETAILS");
		}
		
		if (!OECDTag.trim().equalsIgnoreCase(""))
		{
		String[] oecd = OECDTag.split("\\|");
		for(int k =0;k<oecd.length;k++) 
		{
			String oecddetail[] = oecd[k].split("~",-1);
			WriteLog("notinreason"+oecddetail[2]);
			String element="";
			for (int i = 0; i < oecddetail.length; i++) {
				WriteLog("column1 "+oecddetail[0]);
				WriteLog("column3 "+oecddetail[2]);
				if(element.equals(""))
					element="'"+oecddetail[i]+"'";
				else
					element+=",'"+oecddetail[i]+"'";
				WriteLog("element"+element);
				}
				
			trTableColumn = "WINAME,COUNTRY_OF_TAX_RESIDENCE,TAX_PAYER_IDENTIFICATION_NUMBER,NO_TIN_REASON,REASONB_REMARKS";
			WriteLog("trTableColumn" + trTableColumn);
			//trTableValue = ""+wiName+","+ element + "";
			
			trTableValue = "'"+wiName+"',"+ element + "";
			WriteLog("trTableValue" + trTableValue);
			sInputXML = getDBInsertOECDDetailOECD();
			WriteLog("APInsert Input insertForOECDD: " + sInputXML);
			sOutputXML = executeAPI(sInputXML);
			WriteLog("APInsert Output insertForOECDD: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj);
		}
	}
	
}
	
	private void insertIntoTaxGridRAOP() throws WICreateException, IOException, Exception {
		WriteLog("Inside insertIntoOECDGridRAOP");
		
		
		// Fetch CardNo, CategoryID, SubCategoryID
		if (hm.containsKey("REPORTCOUNTRYDETAILS"))
		{
			OECDTag = hm.get("REPORTCOUNTRYDETAILS");
		}
		
		if (!OECDTag.trim().equalsIgnoreCase(""))
		{
		String[] oecd = OECDTag.split("\\|");
		for(int k =0;k<oecd.length;k++) 
		{
			String oecddetail[] = oecd[k].split("~",-1);
			WriteLog("notinreason"+oecddetail[2]);
			String element="";
			for (int i = 0; i < oecddetail.length; i++) {
				WriteLog("column1 "+oecddetail[0]);
				WriteLog("column3 "+oecddetail[2]);
				if(element.equals(""))
					element="'"+oecddetail[i]+"'";
				else
					element+=",'"+oecddetail[i]+"'";
				WriteLog("element"+element);
				}
				
			trTableColumn = "WI_NAME,COUNTRY_OF_TAX_RESIDENCE,TAX_PAYER_IDENTIFICATION_NO,NO_TIN_REASON,REMARKS";
			WriteLog("trTableColumn" + trTableColumn);
			//trTableValue = ""+wiName+","+ element + "";
			
			trTableValue = "'"+wiName+"',"+ element + "";
			WriteLog("trTableValue" + trTableValue);
			sInputXML = getDBInsertOECDDetailRAOP();
			WriteLog("APInsert Input insertForOECDD: " + sInputXML);
			sOutputXML = executeAPI(sInputXML);
			WriteLog("APInsert Output insertForOECDD: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj);
		}
	}
	
}  
	private void insertIntoHistoryCMP() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		//trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		trTableColumn = "wi_name,workstep,decision,action_date_time,remarks,user_name,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryCMP();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryCBP() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		//trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		trTableColumn = "wi_name,ws_name,decision,action_date_time,remarks,user_name,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryCBP();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	private void insertIntoHistoryDAC() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		//trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		trTableColumn = "WI_NAME,WORKSTEP,DECISION,ACTION_DATE_TIME,REMARKS,USER_NAME,ENTRY_DATE_TIME";
		WriteLog("trTableColumn dor DAC" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue for DAC" + trTableValue);
		sInputXML = getDBInsertInputHistoryDAC();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryIRBL() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		//trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		trTableColumn = "WI_NAME,WORKSTEP,DECISION,ACTION_DATE_TIME,REMARKS,USER_NAME,ENTRY_DATE_TIME";
		WriteLog("trTableColumn dor DAC" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue for iRBL" + trTableValue);
		sInputXML = getDBInsertInputHistoryIRBL();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryACP() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		//trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		trTableColumn = "wi_name,workstep,decision,action_date_time,remarks,user_name,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryACP();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryTT() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "winame,wsname,decision,actiondatetime,remarks,username,entrydatetime";
		//trTableColumn = "winame,workstep,decision,action_date_time,remarks,user_name,entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','"+TT_ActivityName+"','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryTT();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryDAO() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "wi_name, workstep, Decision, decision_date_time, Remarks, user_name, dec_date, entry_date_time";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "','"+ sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryDAO();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private void insertIntoHistoryDCC() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "wi_name, workstep, Decision, decision_date_time, Remarks, user_name, dec_date";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "','" + sDate + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryDCC();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	//Reddy 7.6.22 BSR Process
	private void insertIntoHistoryBSR() throws WICreateException, Exception {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		//WriteLog(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		trTableColumn = "WI_NAME, WS_NAME, DECISION, ACTION_DATE_TIME, REMARKS, USER_NAME";
	//	WriteLog("trTableColumn" + trTableColumn);
		trTableValue = "'"+ wiName + "','Initiation','Submit','" + sDate + "','Workitem created through webservice','" + sUsername + "'";
	//	WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertInputHistoryBSR();
		WriteLog("APInsert Input History: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output History: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private String getDBInsertInputHistoryCAC() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + CAC_WIHISTORY
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
	
	private String getDBInsertInputHistoryOECD() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + OECD_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	private String getDBInsertInputHistoryBAIS() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + BAIS_WIHISTORY
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
		
	private String getDBInsertOECDDetailRMT() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + RMT_OECDGRID
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertOECDDetailOECD() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + OECD_OECDGRID
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertOECDDetailRAOP() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + RAOP_OECDGRID
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}


	private String getDBInsertMailingAddressCAC() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + CAC_MAILINGADDRESS
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	private String getDBInsertInputHistoryCMP() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + CMP_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	private String getDBInsertInputHistoryCBP() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + CBP_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryDAC() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + DAC_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryIRBL() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + iRBL_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryACP() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + ACP_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryTT() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + TT_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryTWC() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + TWC_WIHISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryDAO() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + DAO_DECISION_HISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	private String getDBInsertInputHistoryDCC() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + DCC_DECISION_HISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	//Reddy 7.6.22 BSR Process
	private String getDBInsertInputHistoryBSR() {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + BSR_DECISION_HISTORY
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	
	
	private String getDBInsertInputHistory()
	{
		 return "<?xml version=\"1.0\"?>" +
	            "<APInsert_Input>" +
				"<Option>APInsert</Option>" +
				"<TableName>" + SRM_WIHISTORY + "</TableName>" +
				"<ColName>" + trTableColumn + "</ColName>" +
				"<Values>" + trTableValue + "</Values>" +
				"<EngineName>" + sCabinetName + "</EngineName>" +
				"<SessionId>" + sSessionID + "</SessionId>" +
	            "</APInsert_Input>";
	}
	
	
	
	private String getDBInsertInputFIRCOTable()
	{
		 return "<?xml version=\"1.0\"?>" +
	            "<APInsert_Input>" +
				"<Option>APInsert</Option>" +
				"<TableName>" + FircoTable + "</TableName>" +
				"<ColName>" + trTableColumn + "</ColName>" +
				"<Values>" + trTableValue + "</Values>" +
				"<EngineName>" + sCabinetName + "</EngineName>" +
				"<SessionId>" + sSessionID + "</SessionId>" +
	            "</APInsert_Input>";
	}
	
	private void downloadDocument() throws WICreateException, Exception
	{
		WriteLog("inside downloadDocument");
		for(int i=0;i<documentObj.length;i++)
    	{
			sDocumentName=documentObj[i].getDocumentName();
    		sDocumentType=documentObj[i].getDocumentType();
    		sDocBase64=documentObj[i].getBase64String();
    		WriteLog("Doc Name: "+sDocumentName);
    		WriteLog("Doc Type: "+sDocumentType);
    		//Initial check for document type and base64 
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
    			validateDocumentNameType();
    			dumpFileOnServer();
    			addToSMS(); 
    			deleteDumpedFileOnServer();
    		}
    	}
	}
	private void getProcessDefID() throws WICreateException, Exception
	{
		sInputXML=getAPSelectWithColumnNamesXML("select a.PROCESSDEFID, b.PROCESSID from processdeftable a with(nolock),"+WSR_PROCESS+" b with(nolock) where a.processname='"+sProcessName+"' and b.SUBPROCESSNAME='"+sSubProcess+"' and a.processname=b.processname and isactive='Y'");
		if("CDOB".equalsIgnoreCase(sProcessName))
			sInputXML=getAPSelectWithColumnNamesXML("select a.PROCESSDEFID, b.PROCESSID from processdeftable a with(nolock),"+WSR_PROCESS+" b with(nolock) where a.processname='DigitalOnBoarding' and b.SUBPROCESSNAME='"+sSubProcess+"' and a.RegPrefix=b.processname and isactive='Y'");
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
		
		if ("SRM".equalsIgnoreCase(sProcessName.trim())){
			String sQuery="select a.parentcategoryindex,a.subcategoryindex,b.categoryname from "+SRM_SUBCATEGORY+" a with(nolock),"+SRM_CATEGORY+" b with(nolock) where a.subcategoryname='"+sSubProcess+"' and a.parentcategoryindex=b.categoryindex";
			sInputXML=getAPSelectWithColumnNamesXML(sQuery);
			WriteLog("Input: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			//Check Main Code
			checkCallsMainCode(xmlobj); 
			categoryId=xmlobj.getValueOf("PARENTCATEGORYINDEX");
			subCategoryId=xmlobj.getValueOf("SUBCATEGORYINDEX");
			categoryName=xmlobj.getValueOf("CATEGORYNAME");
			WriteLog("Category ID: "+categoryId+" Sub Cat ID: "+subCategoryId+" Categ Name: "+categoryName);
		}
		
	}
	private void checkMandatoryDocDetails() throws WICreateException, Exception
	{

		WriteLog("inside checkMandatoryDocDetails");
		sInputXML=getAPSelectWithColumnNamesXML("select DOCUMENTNAME from "+WSR_DOCTYPEDETAILS+" with(nolock) where PROCESSID='"+processID+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String documentList []=getTagValues(sOutputXML,"DOCUMENTNAME").split("`");
		WriteLog("Doc Length: "+documentList.length);
		sTotalRetrieved=getTagValues(sOutputXML,"TotalRetrieved");
		if(!sTotalRetrieved.equalsIgnoreCase("0"))
		{
			for(int i=0;i<documentList.length;i++)
			{
				String flag="N";
				for(int j=0;j<documentObj.length;j++)
				{
					if(documentList[i].equalsIgnoreCase(documentObj[j].getDocumentName()))
						flag="Y";
				}
			    //WriteLog("Value of flag: "+flag);
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1023",pCodes.getString("1023")+": "+documentList[i]);
			}
		}
		/*
		else
		{
			throw new WICreateException("1022",pCodes.getString("1022")+" for process id: "+processID);
		}
		*/
		
	}
	private void insertIntoTransTableCCC() throws UnsupportedEncodingException,WICreateException
	{
		String ccc_amt_req = hm.get("CCCAmount");
		String source_code = hm.get("SalesAgentID");
		WriteLog("ccc_amt_req value: "+ccc_amt_req);
		WriteLog("source_code value: "+source_code);
		trTableColumn="WI_NAME,"+"KEYID,"+"verification_details,"+"salary,"+"confirm_with_customer,"+"eligibility,"+"ccc_required,"+"card_number,"+"auth_code,"+"card_no,"+"card_no_1,"+"card_type,"+"Purpose,"+"rakbank_eligible_card_no,"+"status,"+"sub_ref_no,"+"sub_ref_no_auth,"+"tran_req_uid,"+"Approval_cd,"+"Temp_WI_NAME,"+"Cardno_Masked,"+"card_no_1_masked,"+"card_no_masked,"+"card_number_masked,"+"rak_elig_card_masked,"+"c_crn_no,"+"IsError,"+"ccc_amt_req,"+"source_code,"+trTableColumn.substring(0,trTableColumn.length()-1);
		//System.out.println("trTableColumn"+trTableColumn);
		if(typeOfError.equalsIgnoreCase("A"))
		trTableValue="'"+URLEncoder.encode(wiName,"UTF-8")+"'"+","+"'"+encryptCardNo+"'"+","+"'MANUAL'"+","+"'0'"+","+"'Yes'"+","+"'Yes'"+","+"'Yes'"+","+"'"+encryptCardNo+"'"+","+"'"+sAuthCode+"'"+","+"'"+encryptCardNo+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(cardType,"UTF-8")+"'"+","+"'"+URLEncoder.encode(purpose,"UTF-8")+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(sStatus,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sSubRefNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sSubRefNoAuth,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sTranReqUID,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sApprovalCd,"UTF-8")+"'"+","+"'"+URLEncoder.encode(temp_winame, "UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardCRNNo,"UTF-8")+"'"+","+"'N'"+","+"'"+ccc_amt_req+"'"+","+"'"+source_code+"'"+","+trTableValue.substring(0,trTableValue.length()-1);
		else if(typeOfError.equalsIgnoreCase("T") || typeOfError.equalsIgnoreCase("")) //Technical error or no error
		trTableValue="'"+URLEncoder.encode(wiName,"UTF-8")+"'"+","+"'"+encryptCardNo+"'"+","+"'MANUAL'"+","+"'0'"+","+"'Yes'"+","+"'Yes'"+","+"'Yes'"+","+"'"+encryptCardNo+"'"+","+"''"+","+"'"+encryptCardNo+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(cardType,"UTF-8")+"'"+","+"'"+URLEncoder.encode(purpose,"UTF-8")+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(sStatus,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sSubRefNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sSubRefNoAuth,"UTF-8")+"'"+","+"''"+","+"''"+","+"'"+URLEncoder.encode(temp_winame, "UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardCRNNo,"UTF-8")+"'"+","+"'T'"+","+"'"+ccc_amt_req+"'"+","+"'"+source_code+"'"+","+trTableValue.substring(0,trTableValue.length()-1);	
			
		//else if(typeOfError.equalsIgnoreCase("T")) //Technical error
		//trTableValue=trTableValue="'"+cardST+"'"+","+"'Manual'"+","+"'0'"+","+"'"+encryptCardNo+"'"+","+"'"+sAuthCode+"'"+","+"'"+encryptCardNo+"'"+","+"'Yes'"+","+"'"+encryptCardNo+"'"+","+"'"+Icard_type+"'"+","+"'Yes'"+","+"'"+encryptCardNo+"'"+","+"'"+sStatus+"'"+","+"'"+sSubRefNo+"'"+","+"'Yes'"+","+"'true'"+","+"'"+sSubRefNoAuth+"'"+","+"'"+sTranReqUID+"'"+","+"'"+sApprovalCd+"'"+","+"'"+URLEncoder.encode(temp_winame, "UTF-8")+"'"+","+"'"+cardNo+"'"+","+"'"+cardNo+"'"+","+"'"+cardNo+"'"+","+"'"+cardNo+"'"+","+"'"+cardNo+"'";
		WriteLog("trTableValue"+trTableValue);
		sInputXML=getDBInsertInput();
		WriteLog("APInsert Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("APInsert Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj);  
		
	}
	private void insertIntoTransTableBT() throws UnsupportedEncodingException, WICreateException
	{
		WriteLog("subrefno: "+sSubRefNo+"Auth: "+sSubRefNoAuth);
		
		trTableColumn="WI_NAME,"+"verification_details,"+"salary,"+"rak_card_no,"+"auth_code,"+"KEYID,"+"card_number,"+"card_type,"+"eligibility,"+"rakbank_eligible_card_no,"+"status,"+"sub_ref_no,"+"bt_required,"+"bt_required_confirm,"+"sub_ref_no_auth,"+"tran_req_uid,"+"Approval_cd,"+"Temp_WI_NAME,"+"Cardno_Masked,"+"rak_card_no_masked,"+"card_no_masked,"+"card_number_masked,"+"rak_elig_card_masked,"+"card_no,"+"bt_amt_req,"+"type_of_bt,"+"IsError,"+"cardholder_type,"+"name_on_card,"+"Source,"+"c_crn_no,"+trTableColumn.substring(0,trTableColumn.length()-1);
		//System.out.println("trTableColumn"+trTableColumn);
		if(typeOfError.equalsIgnoreCase("A")) //Success
		trTableValue="'"+URLEncoder.encode(wiName,"UTF-8")+"'"+","+"'MANUAL'"+","+"'0'"+","+"'"+encryptCardNo+"'"+","+"'"+sAuthCode+"'"+","+"'"+encryptCardNo+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(cardType,"UTF-8")+"'"+","+"'Yes'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(sStatus,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sSubRefNo,"UTF-8")+"'"+","+"'Yes'"+","+"'true'"+","+"'"+URLEncoder.encode(sSubRefNoAuth,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sTranReqUID,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sApprovalCd,"UTF-8")+"'"+","+"'"+URLEncoder.encode(temp_winame, "UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(btAmountReq,"UTF-8")+"'"+","+"'"+URLEncoder.encode(typeOfBT1,"UTF-8")+"'"+","+"'N'"+","+"'"+URLEncoder.encode(cardHolderType,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardHolderName,"UTF-8")+"'"+","+"'"+URLEncoder.encode(salesAgentId,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardCRNNo,"UTF-8")+"'"+","+trTableValue.substring(0,trTableValue.length()-1);
		else if(typeOfError.equalsIgnoreCase("T") || typeOfError.equalsIgnoreCase("")) //Technical error
		trTableValue="'"+URLEncoder.encode(wiName,"UTF-8")+"'"+","+"'MANUAL'"+","+"'0'"+","+"'"+encryptCardNo+"'"+","+"''"+","+"'"+encryptCardNo+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(cardType,"UTF-8")+"'"+","+"'Yes'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(sStatus,"UTF-8")+"'"+","+"'"+URLEncoder.encode(sSubRefNo,"UTF-8")+"'"+","+"'Yes'"+","+"'true'"+","+"'"+URLEncoder.encode(sSubRefNoAuth,"UTF-8")+"'"+","+"''"+","+"''"+","+"'"+URLEncoder.encode(temp_winame, "UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardNo,"UTF-8")+"'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(btAmountReq,"UTF-8")+"'"+","+"'"+URLEncoder.encode(typeOfBT1,"UTF-8")+"'"+","+"'T'"+","+"'"+URLEncoder.encode(cardHolderType,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardHolderName,"UTF-8")+"'"+","+"'"+URLEncoder.encode(salesAgentId,"UTF-8")+"'"+","+"'"+URLEncoder.encode(cardCRNNo,"UTF-8")+"'"+","+trTableValue.substring(0,trTableValue.length()-1);
		
		
	
		//System.out.println("trTableValue"+trTableValue);
		sInputXML=getDBInsertInput();
		WriteLog("APInsert Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("APInsert Output: "+sOutputXML);   
		xmlobj=new XMLParser(sOutputXML);
		//Check Main Code
		checkCallsMainCode(xmlobj);  
			
	}
	
	private void insertIntoTransTableCBR() throws WICreateException, Exception
	{
	
		if(cardST1.equalsIgnoreCase(""))
		{
		trTableColumn="CCI_CST,"+"IsPrimeUpdated,"+"Approved_Cash_Back_Amount,"+"Cash_Back_Eligibility,"+"KEYID,"+"cardno_masked,"+"Amount_History,"+"Verification_Details,"+"IsError,"+"Temp_WI_NAME,"+"ISSTP,"+"IS_PRIME_UP,"+"WI_NAME,"+trTableColumn.substring(0,trTableColumn.length()-1);
		WriteLog("trTableColumn"+trTableColumn);
		trTableValue="'"+cardST+"'"+","+"'N'"+","+"'"+URLEncoder.encode(reqCBAmount,"UTF-8")+"'"+","+"'Y'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(cardNo, "UTF-8")+"'"+","+"'"+URLEncoder.encode(amountHist, "UTF-8")+"'"+","+"'Manual'"+","+"'N'"+","+"'"+URLEncoder.encode(temp_winame, "UTF-8")+"'"+","+"'"+ISSTP+"'"+","+"'"+IS_PRIME_UP+"'"+","+"'"+URLEncoder.encode(wiName, "UTF-8")+"'"+","+trTableValue.substring(0,trTableValue.length()-1);
		}
		else
		{
		trTableColumn="IsPrimeUpdated,"+"Approved_Cash_Back_Amount,"+"Cash_Back_Eligibility,"+"KEYID,"+"cardno_masked,"+"Amount_History,"+"Verification_Details,"+"IsError,"+"Temp_WI_NAME,"+"ISSTP,"+"IS_PRIME_UP,"+"WI_NAME,"+trTableColumn.substring(0,trTableColumn.length()-1);
        trTableValue="'N'"+","+"'"+URLEncoder.encode(reqCBAmount,"UTF-8")+"'"+","+"'Y'"+","+"'"+encryptCardNo+"'"+","+"'"+URLEncoder.encode(cardNo, "UTF-8")+"'"+","+"'"+URLEncoder.encode(amountHist, "UTF-8")+"'"+","+"'Manual'"+","+"'N'"+","+"'"+URLEncoder.encode(temp_winame, "UTF-8")+"'"+","+"'"+ISSTP+"'"+","+"'"+IS_PRIME_UP+"'"+","+"'"+URLEncoder.encode(wiName, "UTF-8")+"'"+","+trTableValue.substring(0,trTableValue.length()-1);
	
		}
		//System.out.println("trTableValue"+trTableValue);
		sInputXML=getDBInsertInput();
		WriteLog("APInsert Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("APInsert Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj);  
		
	}
	private void subProcCCC() throws WICreateException
	{
		WriteLog("inside subProcCCC");
		cardNo=hm.get("CardNumber");
		cifId=hm.get("CIFID");
		cardCRNNo=hm.get("CardCRNNo");
		cccAmount=hm.get("CCCAmount");
		deliveryChannel=hm.get("DeliveryChannel");
		branchName=hm.get("BranchName");
		//eligiblebtamt set to null
		cardExpiryDate=hm.get("CardExpiryDate");
		if(cardNo.length()<16)
			throw new WICreateException("2005",pCodes.getString("2005"));
		
		getCardType();
		//Fetch marketing code
		getMarketingCode();
		
		//Retrieve Temp_WI_NAME value
		sCBRQuery="select convert(varchar,getdate(),112)+RIGHT(REPLICATE('0', 6) + cast(next value for dbo.usr_0_srm_TEMP_WI as varchar),6) as TEMP";
		sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);
		WriteLog("cashbacklimitQuery sInputXML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		temp_winame=getTagValues(sOutputXML, "TEMP");
		
		//Encrypt Card Number
		
		encryptCardNo=AesUtil.Encrypt("cardNo");
		
		sInputXML="<?xml version='1.0'?><APEncryptString_Input><Option>APEncryptString</Option><EngineName>"+sCabinetName +"</EngineName><StringValue>"+cardNo+"</StringValue></APEncryptString_Input>";
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		
		encryptCardNo =getTagValues(sOutputXML, "Output");
		
		//encryptCardNo="12345";
		WriteLog("encryptCardNo: "+encryptCardNo);
		
		//Masking Card Number
		unmaskedCardNo=cardNo;
		cardNo=cardNo.substring(0,4)+"-"+cardNo.substring(4,6)+"XX-XXXX-"+cardNo.substring(12,16);
		WriteLog("cardNo: "+cardNo);
		
		//Inserting data into external table
		attributeTag+="IntegrationStatus"+(char)21+"false"+(char)25+"encryptedkeyid"+(char)21+encryptCardNo+(char)25+"CifId"+(char)21+cifId+(char)25+"Card_Number"+(char)21+cardNo+(char)25+"Temp_WI_NAME"+(char)21+temp_winame+(char)25;
		WriteLog("attributeTag--"+attributeTag);
	    
		sInputXML=getSRM_APMQPutGetMessage_CCC();
		WriteLog("sInputXML::"+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("soutputXML::"+sOutputXML);
		sReturnCode=getTagValues(sOutputXML,"ReturnCode");
		WriteLog("sReturnCode::"+sReturnCode);
		
		/*if(!deliveryChannel.equalsIgnoreCase("Branch"))
		{
			WriteLog("not branch----");
		throw new WICreateException("3000", pCodes.getString("3000"));
		}*/
		if("Branch".equalsIgnoreCase(deliveryChannel) && branchName==null)
			throw new WICreateException("3000", pCodes.getString("3000"));
		
		getTypeOfError();
		if(typeOfError.equalsIgnoreCase("A"))
		{
			WriteLog("Success if:::::");
			sAuthCode=getTagValues(sOutputXML,"ApprovalId");
			sTranReqUID=getTagValues(sOutputXML,"TrnRqUID");
			sApprovalCd=getTagValues(sOutputXML,"DebitAuthId");
			sStatus="Successful";
			sSubRefNo="0001";
			sSubRefNoAuth="0001";
			WriteLog("sAuthcode--"+sAuthCode+"--stranReqUID--"+sTranReqUID+"--sApprovalCd--"+sApprovalCd+"--sStatus--"+sStatus+"--sSubrefNo--"+sSubRefNo+"--sSubRefNoAuth--"+sSubRefNoAuth);
			
		}
		
		else if(typeOfError.equalsIgnoreCase("T"))
		{
			WriteLog("technical error::");
			//errorCodeDesc=typeOfError+":"+errorDesc;
			sSubRefNo="0001";
			sSubRefNoAuth="0001";
			sStatus="Failure";
			WriteLog("sSubRefNo:"+sSubRefNo);
		}
		else if(typeOfError.equalsIgnoreCase("B"))
		{
			WriteLog("business error::");
			//errorCodeDesc=typeOfError+":"+errorDesc;
			WriteLog("errorDesc:"+errorDesc);
			throw new WICreateException("9"+sReturnCode,errorDesc);
		}

	}
	private void getMarketingCode() throws WICreateException
	{
		//Fetch Card status
		sCBRQuery="select top 1 marketingcode,purposedesc from usr_0_srm_ccc_purpose_master with(nolock) where purposecodeshort='"+purposeShortCode+"'";//"Select CARD_PRODUCT,CASH_BACK_LIMIT from "+SRM_CARDS_BIN+"  where parameter='"+cardNo.substring(0,6)+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);
		WriteLog("MarketingCode sInputXML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("MarketingCode Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		marketingCode=getTagValues(sOutputXML, "marketingcode");
		purpose=getTagValues(sOutputXML, "purposedesc");
		
	}
	private String getDBInsertInput()
	{
		 return "<?xml version=\"1.0\"?>" +
	            "<APInsert_Input>" +
				"<Option>APInsert</Option>" +
				"<TableName>" + transactionTableName + "</TableName>" +
				"<ColName>" + trTableColumn + "</ColName>" +
				"<Values>" + trTableValue + "</Values>" +
				"<EngineName>" + sCabinetName + "</EngineName>" +
				"<SessionId>" + sSessionID + "</SessionId>" +
	            "</APInsert_Input>";
	}
	private void checkExistingSession() throws Exception, WICreateException
	{
		WriteLog("inside checkExistingSession");
		//String getSessionQry="select top(1) RANDOMNUMBER from pdbconnection";
		String getSessionQry="select randomnumber from pdbconnection with(nolock) where userindex in (select userindex from pdbuser with(nolock) where username='"+sUsername+"')";
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
	private void deleteConnection() throws Exception
	{
		sInputXML=getWMDisConnectXML();
		WriteLog("WMDisconnect Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("WMDisconnect Output: "+sOutputXML);
	}
	private void validateDocumentNameType() throws WICreateException
	{
		
		 
		//Fetch from USR_0_WSR_DOCTYPEDETAILS
		sInputXML=getAPSelectWithColumnNamesXML("select DOCUMENTNAME, DOCTYPE, ISMANDATORY  from "+WSR_DOCTYPEDETAILS+" with(nolock) where PROCESSID='"+processID+"' and DOCUMENTNAME='"+sDocumentName+"' and DOCTYPE='"+sDocumentType+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
		WriteLog("APSelectWithColumnNames Input: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("APSelectWithColumnNames Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj); 
		String docNameTable=getTagValues(sOutputXML, "DOCUMENTNAME");
		String docTypeTable=getTagValues(sOutputXML, "DOCTYPE");
		//String isMand=getTagValues(sOutputXML, "ISMANDATORY");
		if(docNameTable.equalsIgnoreCase(""))
		{
			WriteLog("No Value mapped for DocName");
			throw new WICreateException("1018",pCodes.getString("1018")+": "+sDocumentName+" and "+sDocumentType);
		}
		 
		
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
	private void addToSMS() throws WICreateException,IOException, Exception
	{
		//Adding Document to SMS
		WriteLog("SMS Addition starts for: "+sFilePath);
		NewIsIndex = new JPISIsIndex();
		dataJPDB =  new JPDBRecoverDocData();
		try
		{
			CPISDocumentTxn.AddDocument_MT(null, sJtsIp, (short)iJtsPort,
					sCabinetName, (short)iVolId, sFilePath, dataJPDB, null, NewIsIndex);
		
		
		String isIndex=(new StringBuilder()).
				append(NewIsIndex.m_nDocIndex).append("#").append(NewIsIndex.m_sVolumeId).append("#").toString();
		WriteLog("Data returned from SMS: "+isIndex);
		documentTag=documentTag+sDocumentName+" "+isIndex+" ";
		}
		catch(JPISException e)
		{
			WriteLog("JPISException: "+e);
			throw new WICreateException("1000",pCodes.getString("1000")+" : "+e.getMessage());
		}
		WriteLog("Document Tag: "+documentTag);
		
	}
	private void dumpFileOnServer() throws WICreateException,IOException,Exception
	{
		try
		{
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
			String strDate = sdf.format(cal.getTime());
			strDate= strDate.replace("/","");
		    strDate=strDate.replace(":","");
		    strDate=strDate.replace(".","");
		    strDate=strDate.replace(" ","");
		    sDumpLocation=sTempLoc+"/BinaryDump/"+strDate+"/";
			File fDumpFolder=new File(sDumpLocation);
			if(!fDumpFolder.exists())
				fDumpFolder.mkdirs();
			sFilePath=new StringBuilder().append(sDumpLocation).append(sDocumentName).append(".").append(sDocumentType).toString();
			WriteLog("Dump File Path: "+sFilePath);
			byte[] base64Bytes = sDocBase64.getBytes(Charset.forName("UTF-8"));
			WriteLog("Size of base64 encoded bytes: "+base64Bytes.length);
			byte binaryBytes[] = Base64.decodeBase64(base64Bytes);
			File fDumpFile=new File(sFilePath);
			FileOutputStream fileoutputstream = new FileOutputStream(fDumpFile);
			fileoutputstream.write(binaryBytes);
			fileoutputstream.close();
		}
		catch (IOException e)
		{
			WriteLog("IOException: "+e);
			throw new WICreateException("1004",pCodes.getString("1004")+" : "+e.getMessage());
		
		}
		catch (Exception e)
		{
			WriteLog("Exception: "+e);
			throw new WICreateException("1004",pCodes.getString("1004")+" : "+e.getMessage());
		}
	}
	
	private void deleteDumpedFileOnServer() 
	{
		WriteLog("inside deleteDumpedFileOnServer");
		try
		{
			File fDumpFolder=new File(sDumpLocation);
			FileUtils.deleteDirectory(fDumpFolder);
			WriteLog("Folder Deleted");
		}
		catch (Exception e)
		{
			WriteLog("Exception: "+e.getMessage());
		}
	}
	private void getTypeOfError() throws WICreateException
	{
		//sReturnCode="1121";
		String sQuery="select typeoferror,errordescription from usr_0_wsr_errordetails with(nolock) where errorcode='"+sReturnCode+"'";
		String sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("sinputXML::"+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("soutputXML:"+sOutputXML);
		typeOfError=getTagValues(sOutputXML,"typeoferror");
		errorDesc=getTagValues(sOutputXML,"errordescription");
		WriteLog("typeoferror::"+typeOfError);
		WriteLog("errordesc:"+errorDesc);
	}
	
	
	private void getCardType() throws WICreateException

    {
		  //Fetch Card type
           BTQuery="Select Merchant from "+SRM_CARDS_BIN+" with(nolock) where parameter='"+cardNo.substring(0,6)+"'";
           sInputXML=getAPSelectWithColumnNamesXML(BTQuery);
           WriteLog("Card sInputXML: "+sInputXML);
           sOutputXML=executeAPI(sInputXML);
           WriteLog("Output XML: "+sOutputXML);
           xmlobj=new XMLParser(sOutputXML);
           checkCallsMainCode(xmlobj);

           cardType=getTagValues(sOutputXML,"Merchant");
    }

	private void getTypeOfBT() throws WICreateException
	{
		WriteLog("inside get type of BT");
		BTQuery="select top 1 typeofbt from usr_0_srm_bt_marketingcodes with(nolock) where marketingcode='"+typeOfBT+"' and isactive='Y'";
		WriteLog("BTQUERY::"+BTQuery);
		String sInputXML=getAPSelectWithColumnNamesXML(BTQuery);
		WriteLog("sinputXML::"+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("sOUTPUTXML::"+sOutputXML);
		typeOfBT1=getTagValues(sOutputXML,"typeofbt");
		WriteLog("typeofBT:::"+typeOfBT1);

	}
	
	private void subProcBT() throws WICreateException
	{
		WriteLog("inside subProcBT");
		cardNo=hm.get("CardNumber");
		cifId=hm.get("CIFID");
		customerName=hm.get("CustomerName");
		cardCRNNo=hm.get("CardCRNNo");
		//salary default 0
		mobileNo=hm.get("MobileNumber");
		cardHolderType=hm.get("CardHolderType");
		agentNetworkId=hm.get("AgentNetworkId");
		salesAgentId=hm.get("SalesAgentID");
		cardStatus=hm.get("card_status");
		overdueamount=hm.get("OverDueAmount");
		cardProduct=hm.get("CardProduct");
		btAmountReq=hm.get("BTAmount");
		typeOfBT=hm.get("TypeOfBT");
		paymentBy=hm.get("PaymentBy");
		remarks=hm.get("Remarks");
		availableBalance=hm.get("AvailableBalance");
		cashAvailableBalance=hm.get("CashAvailableBalance");
		branchName=hm.get("BranchName");
		//cardeligibity default yes
		cardHolderName=hm.get("CardHolderName");
		//cardtype see
		deliveryChannel=hm.get("DeliveryChannel");
		//eligiblebtamt set to null
		cardExpiryDate=hm.get("CardExpiryDate");
		otherBankCardNo=hm.get("OtherBankCardNo");
		otherBankCardType=hm.get("OtherBankCardType");
		otherBankName=hm.get("OtherBankName");
		WriteLog("cardHolderType--"+cardHolderType+"cardno--"+cardNo+"--cifid--"+cifId+"--custname--"+customerName+"--cardCrnno--"+cardCRNNo+"--mobileno--"+mobileNo+"--agentnwid--"+agentNetworkId+"--salesagentid--"+salesAgentId+"--cardstatus--"+cardStatus+"--overdueamt--"+overdueamount+"--cardproduct--"+cardProduct+"--btamtreq--"+btAmountReq+"--paymentby--"+paymentBy+"--remarks--"+remarks+"--availablebalance--"+availableBalance+"--cashavailableBalance--"+cashAvailableBalance+"--branchname--"+branchName+"--cardEligibilty--"+cardEligibility+"--cardHolderName--"+cardHolderName+"--cardType--"+cardType+"--deliverychannel--"+deliveryChannel+"--btEligibleamt--"+btEligibleAmount+"--cardexpdate--"+cardExpiryDate+"--otherbankcardno--"+otherBankCardNo+"--otherbankcardtype--"+otherBankCardType+"--otherbankname--"+otherBankName);
		if(cardNo.length()<16)
			throw new WICreateException("2005",pCodes.getString("2005"));
		
		
		 getCardType();
		
		//Retrieve Temp_WI_NAME value
		sCBRQuery="select convert(varchar,getdate(),112)+RIGHT(REPLICATE('0', 6) + cast(next value for dbo.usr_0_srm_TEMP_WI as varchar),6) as TEMP";
		sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);
		WriteLog("cashbacklimitQuery sInputXML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		temp_winame=getTagValues(sOutputXML, "TEMP");
	 
		 
		 //Encrypt Card Number
		
		encryptCardNo=AesUtil.Encrypt("cardNo");
		sInputXML="<?xml version='1.0'?><APEncryptString_Input><Option>APEncryptString</Option><EngineName>"+sCabinetName +"</EngineName><StringValue>"+cardNo+"</StringValue></APEncryptString_Input>";
		//WriteLog("sInputXML::"+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		encryptCardNo =getTagValues(sOutputXML, "Output");
		
		
		//encryptCardNo="4NrdXHmIsnWmyqPk/+F5dM9y2x9UoAmCc7q51FEnewk=";
		WriteLog("encryptCardNo: "+encryptCardNo);
		
		
		
		//Masking Card Number
		unmaskedCardNo=cardNo;
		cardNo=cardNo.substring(0,4)+"-"+cardNo.substring(4,6)+"XX-XXXX-"+cardNo.substring(12,16);
		WriteLog("cardNo: "+cardNo);
		
		//Inserting data into external table
		attributeTag+="IntegrationStatus"+(char)21+"false"+(char)25+"encryptedkeyid"+(char)21+encryptCardNo+(char)25+"CifId"+(char)21+cifId+(char)25+"Card_Number"+(char)21+cardNo+(char)25+"Temp_WI_NAME"+(char)21+temp_winame+(char)25;
		WriteLog("attributeTag--"+attributeTag);
	    
		sInputXML=getSRM_APMQPutGetMessage_BT();
		WriteLog("sInputXML::"+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("soutputXML::"+sOutputXML);
		sReturnCode=getTagValues(sOutputXML,"ReturnCode");
		WriteLog("sReturnCode::"+sReturnCode);
		
		if("Branch".equalsIgnoreCase(deliveryChannel) && branchName==null)
			throw new WICreateException("3000", pCodes.getString("3000"));
		
		/*if(!deliveryChannel.equalsIgnoreCase("Branch"))
			throw new WICreateException("3000", pCodes.getString("3000"));*/
		
		getTypeOfError();
		getTypeOfBT();
		
		if(typeOfError.equalsIgnoreCase("A"))
		{
			WriteLog("Success if:::::");
			sAuthCode=getTagValues(sOutputXML,"ApprovalId");
			sTranReqUID=getTagValues(sOutputXML,"TrnRqUID");
			sApprovalCd=getTagValues(sOutputXML,"DebitAuthId");
			sStatus="Successful";
			sSubRefNo="0001";
			sSubRefNoAuth="0001";
			WriteLog("sAuthcode--"+sAuthCode+"--stranReqUID--"+sTranReqUID+"--sApprovalCd--"+sApprovalCd+"--sStatus--"+sStatus+"--sSubrefNo--"+sSubRefNo+"--sSubRefNoAuth--"+sSubRefNoAuth);
			
		}
		
		else if(typeOfError.equalsIgnoreCase("T"))
		{
			WriteLog("technical error::");
			//errorDesc=typeOfError+":"+errorDesc;
			sStatus="Failure";
			sSubRefNo="0001";
			sSubRefNoAuth="0001";
			WriteLog("sSubRefNo:"+sSubRefNo);
			
		}
		else if(typeOfError.equalsIgnoreCase("B"))
		{
			WriteLog("business error::");
			//errorCodeDesc=typeOfError+":"+errorDesc;
			WriteLog("errorDesc:"+errorDesc);
			throw new WICreateException("9"+sReturnCode,errorDesc);
		}

	}
	
	private void subProcCBR() throws WICreateException, IOException, Exception
	{
			WriteLog("Inside subProcCBR");
			//Fetch CardNo, CategoryID, SubCategoryID
			cardNo=hm.get("CardNumber");
			cardST=hm.get("CardSubType");
			cardST=(hm.get("CardSubType")==null)? "" : hm.get("CardSubType").trim();
			cashBackEA=hm.get("RewardPoints");
			cardType=hm.get("CardType");
			cardHolderType=hm.get("CardHolderType");
			reqCBAmount=hm.get("RedemptionAmount");
			
			//Only check here for 16 digit card number
			if(cardNo.length()<16)
				throw new WICreateException("2005",pCodes.getString("2005"));
			
			//Fetch Card status
			sCBRQuery="Select CARD_PRODUCT,CASH_BACK_LIMIT from "+SRM_CARDS_BIN+" with(nolock) where parameter='"+cardNo.substring(0,6)+"'";
			sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);
			WriteLog("Card sInputXML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
		
			cardST1=cardST;
			if(cardST.equalsIgnoreCase(""))
			{
				System.out.println("inside iff--");
			cardST=getTagValues(sOutputXML, "CARD_PRODUCT");
			}
			
			System.out.println("cardST1 is::"+cardST);
			cashBackMinLimit=getTagValues(sOutputXML, "CASH_BACK_LIMIT");
		
			cardStatus=hm.get("CardStatus");
			
			//Fetch value of IS_PRIME_UP
			String primeCheckQuery = "Select FieldValue from usr_0_srm_DataCenterFlagMaster with(nolock) where fieldname = 'IS_PRIME_UP' and catindex = 1 and subcatindex = 1";
			sInputXML=getAPSelectWithColumnNamesXML(primeCheckQuery);
			WriteLog("FieldValue sInputXML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			IS_PRIME_UP=getTagValues(sOutputXML, "FieldValue");
			
			//Fetch Eligible Cash back amount from table
			String cashbacklimitQuery = "Select FieldValue from usr_0_srm_dynamic_variable_master with(nolock) where fieldname = 'CASHBACKAMOUNT'";
			sInputXML=getAPSelectWithColumnNamesXML(cashbacklimitQuery);
			WriteLog("cashbacklimitQuery sInputXML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			cashBackEAFromTable=getTagValues(sOutputXML, "FieldValue");
			if(Integer.parseInt(reqCBAmount)>Integer.parseInt(cashBackEAFromTable))
				ISSTP="N";
			else
				ISSTP="Y";
			
			//Retrieve Temp_WI_NAME value
			sCBRQuery="select convert(varchar,getdate(),112)+RIGHT(REPLICATE('0', 6) + cast(next value for dbo.usr_0_srm_TEMP_WI as varchar),6) as TEMP";
			sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);
			WriteLog("cashbacklimitQuery sInputXML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			temp_winame=getTagValues(sOutputXML, "TEMP");
			
			
			//Amount History
			amountHist=sRequestorChannelId+":"+reqCBAmount+"/";
			WriteLog("Amount History: "+amountHist);
			
			//Encrypt Card Number
			//encryptCardNo=AesUtil.Encrypt("cardNo");
			sInputXML="<?xml version='1.0'?><APEncryptString_Input><Option>APEncryptString</Option><EngineName>"+sCabinetName +"</EngineName><StringValue>"+cardNo+"</StringValue></APEncryptString_Input>";
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			encryptCardNo =getTagValues(sOutputXML, "Output");
			WriteLog("encryptCardNo: "+encryptCardNo);
			
			//Masking Card Number
			cardNo=cardNo.substring(0,4)+"-"+cardNo.substring(4,6)+"XX-XXXX-"+cardNo.substring(12,16);
			WriteLog("cardNo: "+cardNo);
			
			//Inserting data into external table
			attributeTag=attributeTag+"encryptedkeyid"+(char)21+encryptCardNo+(char)25+"Temp_WI_NAME"+(char)21+temp_winame+(char)25+"Card_Number"+(char)21+cardNo+(char)25;
			
			//Validate non-technical CBR errors
			validateCBR();
			
			//attributeTag="Category"+(char)21+categoryName+(char)25+"SubCategory"+(char)21+sSubProcess+(char)25+"WS_NAME"+(char)21+"PBO"+(char)25+"ProcessName"+(char)21+sProcessName+(char)25+"IntroducedBy"+(char)21+sUsername+(char)25+"Channel"+(char)21+"Phone Banking"+(char)25+"IntroductionDateTime"+(char)21+sDate+(char)25;
			WriteLog("Attribute Tag: "+attributeTag);
		 
		
	}
	
	private void insertForMailingAddress() throws WICreateException, IOException, Exception {
		WriteLog("Inside insertForMailingAddress");
		String element="";
		
		// Fetch CardNo, CategoryID, SubCategoryID
		MailingAddress = hm.get("MailingAddress");

		String[] splittedMailingAddress = MailingAddress.split("\\|");
		element="";
		for (int i = 0; i < splittedMailingAddress.length; i++) {
			String arrayVal=splittedMailingAddress[i];
			
			if(element.equals(""))
				element="'"+arrayVal+"'";
			else
				element+=",'"+arrayVal+"'";
				
		}
		
		trTableColumn = "addresstype,street,noname,addressline3,state,country,pobox,city,WINAME";
		WriteLog("trTableColumn" + trTableColumn);
		trTableValue = ""+element+",'"+ wiName + "'";
		WriteLog("trTableValue" + trTableValue);
		sInputXML = getDBInsertMailingAddressCAC();
		WriteLog("APInsert Input insertForMailingAddress: " + sInputXML);
		sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output insertForMailingAddress: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);

	}
	
	private void validateCBR() throws WICreateException,IOException, Exception
	{
		WriteLog("inside validateCBR");
		
		if(Integer.parseInt(cashBackEA)<Integer.parseInt(cashBackMinLimit) && !cardST.equalsIgnoreCase("Red Card"))
			throw new WICreateException("2000",pCodes.getString("2000"));
		sCBRQuery="select CARD_STATUS_DESCRIPTION from usr_0_srm_cbr_invalidstatus with(nolock) where ISActive='Y' and CARD_STATUS_CODE='"+cardStatus+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);		
		WriteLog("Card sInputXML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String checkStatus=getTagValues(sOutputXML, "CARD_STATUS_DESCRIPTION");
		if(!checkStatus.equalsIgnoreCase(""))
			throw new WICreateException("2001",pCodes.getString("2001")+" "+cardStatus);
		if(Integer.parseInt(cashBackEA)<0)
			throw new WICreateException("2002",pCodes.getString("2002"));
		/*		 	
		if(Integer.parseInt(reqCBAmount)>Integer.parseInt(cashBackEA))
			throw new WICreateException("2006",pCodes.getString("2006"));
		*/		
		
		sCBRQuery="select CARD_PRODUCT from usr_0_srm_cbr_invalidproducts with(nolock) where ISActive='Y'and CARD_PRODUCT='"+cardST+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);
		WriteLog("CARD_PRODUCT sInputXML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String checkCardSTStatus=getTagValues(sOutputXML, "CARD_PRODUCT");
		if(!checkCardSTStatus.equalsIgnoreCase(""))
			throw new WICreateException("2003",pCodes.getString("2003")+" "+checkCardSTStatus);
		if(!cardHolderType.equalsIgnoreCase("Primary") && cardType.equalsIgnoreCase("Credit"))
				throw new WICreateException("2004",pCodes.getString("2004")+" "+checkCardSTStatus);
		
		//Duplicate request check start
		sInputXML = "<?xml version='1.0'?><APProcedure2_Input><Option>APProcedure2</Option><ProcName>SRM_RESTRICT_DUPLICATE</ProcName><Params>'', '"+cardNo+"', '"+reqCBAmount+"'</Params><NoOfCols>3</NoOfCols><SessionID>"+sSessionID+"</SessionID><EngineName>"+sCabinetName+"</EngineName></APProcedure2_Input>";
		WriteLog("input for duplicate restrict "+sInputXML);	
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String mainCodeProc = xmlobj.getValueOf("MainCode");
		String result = xmlobj.getValueOf("Results");
		boolean continueExecution = true;
		String tempamount = "";
		int recordcount2=0;
		String subXML="";
		XMLParser objXmlParser=null;
		int Approved_Cash_Back_Amount=0;
		if(mainCodeProc.equals("0") && result.startsWith("1"))
		{	 
			continueExecution = false;
			throw new WICreateException("2007",pCodes.getString("2007"));
		}
		else if (!mainCodeProc.equals("0"))
		{
			continueExecution = false;
			WriteLog("Error in running SRM_RESTRICT_DUPLICATE");
		}
		else if (continueExecution)
		{
			//sCBRQuery="select tr.Approved_Cash_Back_Amount from usr_0_srm_tr_cbr tr, rb_new_srm_exttable ext where tr.KEYID='"+cardNo+"'  AND  ext.wi_name = tr.wi_name and tr.IsPrimeUpdated='N' AND  tr.IsError='N' and (charindex('Reject', tr.Decision)<=0 or tr.decision is null) and ext.isrejected is null and (ext.current_workstep is not null and ext.current_workstep !='Not Introduced')";
			sCBRQuery="select tr.Approved_Cash_Back_Amount from "+transactionTableName+" tr with(nolock), "+externalTableName+" ext with(nolock) where tr.KEYID='"+cardNo+"'  AND  ext.wi_name = tr.wi_name and tr.IsPrimeUpdated='N' AND  tr.IsError='N' and (charindex('Reject', tr.Decision)<=0 or tr.decision is null) and ext.isrejected is null and (ext.current_workstep is not null and ext.current_workstep !='Not Introduced')";
			sInputXML=getAPSelectWithColumnNamesXML(sCBRQuery);
			WriteLog("input for duplicate restrict "+sInputXML);	
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			String mainCodeValue2 = xmlobj.getValueOf("MainCode");
			if(mainCodeValue2.equals("0"))
			{	
				recordcount2 = Integer.parseInt(xmlobj.getValueOf("TotalRetrieved"));
				WriteLog("Number of records are "+recordcount2);
				if(recordcount2!=0)
				{
					for(int k=0; k<recordcount2; k++)
					{	
						subXML = xmlobj.getNextValueOf("Record");
						objXmlParser = new XMLParser(subXML);
						tempamount = objXmlParser.getValueOf("Approved_Cash_Back_Amount");
						if (tempamount.equalsIgnoreCase("NULL") || tempamount.equals(""))
							continue;
						if(tempamount.indexOf(".")!=-1)
							tempamount=tempamount.substring(0,tempamount.indexOf("."));
						Approved_Cash_Back_Amount+= Integer.parseInt(tempamount);
					}
					throw new WICreateException("2008",recordcount2+pCodes.getString("2008")+": "+Approved_Cash_Back_Amount);
				}
				else
					WriteLog("No duplicate records found.WICreation will continue");
			}	
			else
			{ 
				WriteLog("Duplicate check failed with maincode="+mainCodeValue2);
				continueExecution=false;
			}
		}
		////Duplicate request check end
		
	}
	private void loadConfiguration() throws IOException, Exception
	{
		//load file configuration
		WriteLog("Loading Configuration file 123");
		try
		{
			
			Properties p = new Properties();
			String sConfigFile=new StringBuilder().append(System.getProperty("user.dir"))
					.append(System.getProperty("file.separator")).append("BPMCustomWebservicesConf")
					.append(System.getProperty("file.separator")).append("config.properties").toString();
			
			WriteLog("config file path is "+sConfigFile);
			
			p.load(new FileInputStream(sConfigFile));
			WriteLog("After p load");
		    sCabinetName=p.getProperty("CabinetName");
		    sJtsIp=p.getProperty("JtsIp");
		    iJtsPort=Integer.parseInt(p.getProperty("JtsPort"));
		    sUsername=p.getProperty("username");
		
			//WriteLog("sPassword::"+sPassword);
			sPassword=decryptPassword(p.getProperty("password"));	
			//WriteLog("after decrption::"+sPassword);
			sTempLoc=p.getProperty("TempDocumentLoc");
		    iVolId=Integer.parseInt(p.getProperty("volid"));
		    WriteLog("CabinetName: "+sCabinetName+", JtsIp: "+sJtsIp+", JtsPort: "+iJtsPort+" ,Username: "+sUsername+", Password: "+p.getProperty("password")+" ,VolumeID: "+iVolId);
		    WriteLog("Configuration file loaded successfuly");
		    response.setCabinetName(sCabinetName);
		    response.setJtsIp(sJtsIp);
		    response.setJtsPort(iJtsPort);
		    response.setUsername(sUsername);
		}
		catch(Exception e)
		{
			WriteLog("Inside exception of log decryption");
			throw new WICreateException("3001",pCodes.getString("3001"));
		}  
	}
	/*
	private String decryptPassword(String pass) throws Exception
	{
		return AESEncryption.decrypt(pass);
	}
	*/
	private void initializeLogger() throws IOException
	{
		Properties p = new Properties();
		String log4JPropertyFile=new StringBuilder().append(System.getProperty("user.dir"))
				.append(System.getProperty("file.separator")).append("BPMCustomWebservicesConf")
				.append(System.getProperty("file.separator")).append("log4j.properties").toString();
		//System.out.println("Path of log4j file: "+log4JPropertyFile);
		
		p.load(new FileInputStream(log4JPropertyFile));
		PropertyConfigurator.configure(p);
		WriteLog("Logger is configured successfully");
		
	}
	
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
			sInitiateAlso=(req.getInitiateAlso()==null)? "" : req.getInitiateAlso().trim();
			headerObjRequest=req.getEE_EAI_HEADER();
			
			InputMessage=(req.getInputMessage()==null)? "" : req.getInputMessage().trim();
			
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
				//checkAttributeTable(attributeObj[i].getName(),attributeObj[i].getValue());
			}
			//Fetch Document base64 String 
			documentsObj=req.getDocuments();
			documentObj=documentsObj.getDocument();
			
			WriteLog("sProcessName: "+sProcessName+", sSubProcess: "+sSubProcess
					+" sInitiateAlso: "+sInitiateAlso);
		}
		catch(Exception e)
		{
			WriteLog("Exception: "+e.getMessage());
			throw new WICreateException("1007",pCodes.getString("1007")+" : "+e.getMessage());
		}
	}
	private void checkAttributeTable(String attributeName, String attributeValue) throws WICreateException, Exception
	{
		//WriteLog("inside checkAttributeTable");
		String getExtTransQry= "";
		if(sProcessName.equalsIgnoreCase("RAOP") || sProcessName.equalsIgnoreCase("CMP") || sProcessName.equalsIgnoreCase("CBP") || sProcessName.equalsIgnoreCase("DAC") || sProcessName.equalsIgnoreCase("ACP") ) // for iBPS Processes
		{
			getExtTransQry="select ATTRIBUTENAME,EXTERNALTABLECOLNAME, TRANSACTIONTABLECOLNAME, ISMANDATORY,ATTRIBUTE_FORMAT,ATTRIBUTE_TYPE from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME='"+attributeName+"' and PROCESSID='"+processID+"'";
		}
		else if (sProcessName.equalsIgnoreCase("Digital_CC"))
		{
			getExtTransQry="select ATTRIBUTENAME,EXTERNALTABLECOLNAME, TRANSACTIONTABLECOLNAME, ISMANDATORY from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME='"+attributeName+"' and PROCESSID='"+processID+"' AND ISACTIVE='Y'";
		}else // for BPM Processes
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
		
		if((sProcessName.equalsIgnoreCase("OECD")) && (attributeName.equalsIgnoreCase("CIFTYPE")) &&  !(attributeValue.equalsIgnoreCase("")))
		{
		//WriteLog("CIFtype------" +attributeValue);	
			if(attributeValue.equalsIgnoreCase("R"))
			{
				attributeValue = "Retail";	
			}
			else
				attributeValue = "Corporate";	
		}
		
		//Added by Nikita for the checking length of the attribute value at 14062019
			
		if(sProcessName.equalsIgnoreCase("RAOP") || sProcessName.equalsIgnoreCase("CMP") || sProcessName.equalsIgnoreCase("CBP") || sProcessName.equalsIgnoreCase("DAC")  || sProcessName.equalsIgnoreCase("ACP"))
		{
			String strattributesize=getTagValues(sOutputXML, "ATTRIBUTE_FORMAT");
			String attType = getTagValues(sOutputXML, "ATTRIBUTE_TYPE");
			attributeValue=attributeValue.trim();
			if(!(attributeValue.equalsIgnoreCase("")))
			{
				int attributelength = attributeValue.length();
				//WriteLog("attributelength--"+attributelength);
				//WriteLog("attNameFromTable--"+attNameFromTable);
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
								//WriteLog("inside 995");
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

		//Below code added by amitabh on 12-8-2016 to get branch code from branchname for CCC process
		if((sSubProcess.equalsIgnoreCase("Credit Card Cheque") || sSubProcess.equalsIgnoreCase("Balance Transfer")) && attributeName.equalsIgnoreCase("BranchName") 
		&& !(attributeValue.equalsIgnoreCase("")))
		{
			WriteLog("Branch Code "+attributeValue);					
			attributeValue=getBranchNameByBranchCode(attributeValue);
			WriteLog("Branch Name "+attributeValue);
			if(attributeValue.equalsIgnoreCase(""))
			throw new WICreateException("1010", "Branch mapping not found in branch master for provided branch code.");
		}
		//********************************************************************************************
		//Below code added to replace &amp; to & in OtherBankNames
		if((sSubProcess.equalsIgnoreCase("Credit Card Cheque") || sSubProcess.equalsIgnoreCase("Balance Transfer")) && attributeName.equalsIgnoreCase("OtherBankName") 
		&& !(attributeValue.equalsIgnoreCase("")))
		{
			WriteLog("OtherBankNames before replace"+attributeValue);					
			attributeValue=attributeValue.replaceAll("&amp;", "&");
			WriteLog("OtherBankNames after replace"+attributeValue);
		}
		//********************************************************************************************
		
		//Changes by Sajan for FALCON DOB
		if(!attNameFromTable.equalsIgnoreCase(""))
		{
			WriteLog("Attribute Name: "+attributeName);
			if(((isMandatory.equalsIgnoreCase("Y") || isMandatory.equalsIgnoreCase("N")) && !attributeValue.equalsIgnoreCase("")) || (isMandatory.equalsIgnoreCase("N") && attributeValue.equalsIgnoreCase("")))
			{
				if (!extFlag.equalsIgnoreCase("") && transFlag.equalsIgnoreCase(""))
				{
					attributeTag=attributeTag+extFlag+(char)21+attributeValue+(char)25;
				}
				else if(extFlag.equalsIgnoreCase("") && !transFlag.equalsIgnoreCase(""))
				{
					trTableColumn=trTableColumn+transFlag+",";
					trTableValue=trTableValue+"'"+URLEncoder.encode(attributeValue, "UTF-8")+"'"+",";
				}
				else if(!extFlag.equalsIgnoreCase("") && !transFlag.equalsIgnoreCase(""))
				{
					attributeTag=attributeTag+extFlag+(char)21+attributeValue+(char)25;
					trTableColumn=trTableColumn+transFlag+",";
					trTableValue=trTableValue+"'"+URLEncoder.encode(attributeValue, "UTF-8")+"'"+",";
				}
				else if(extFlag.equalsIgnoreCase("") && transFlag.equalsIgnoreCase(""))
				{
					WriteLog("Both Flag N");
					if(attributeName.equalsIgnoreCase("PurposeShortCode"))
							purposeShortCode=attributeValue;
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
	
	private void checkExtTableAttrIBPS(HashMap<String, String> hmt) throws WICreateException, Exception
	{
		//WriteLog("inside checkAttributeTable");
		String attributeNames = "";
		for (String name: hmt.keySet()){
			if(attributeNames.equalsIgnoreCase(""))
				attributeNames = "'"+name.toString()+"'";
			else 
				attributeNames = attributeNames+",'"+name.toString()+"'";
		}
		String getExtTransQry= "";
		
		getExtTransQry="select ATTRIBUTENAME, EXTERNALTABLECOLNAME, ISMANDATORY from "+WSR_ATTRDETAILS+" with(nolock) where ATTRIBUTENAME in ("+attributeNames+") and PROCESSID='"+processID+"'";
		
		sInputXML=getAPSelectWithColumnNamesXML(getExtTransQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String attNameFromTable=getTagValues(sOutputXML, "ATTRIBUTENAME");
		String extFlag=getTagValues(sOutputXML, "EXTERNALTABLECOLNAME");
		String isMandatory=getTagValues(sOutputXML, "ISMANDATORY");
		
		String AttrTagsName []=attNameFromTable.split("`");
		String ExtColsName []=extFlag.split("`");
		String MandateList []=isMandatory.split("`");
		
		for (String name: hmt.keySet()){
			String attrtag = name.toString();
			String flg = "N";
			//WriteLog("Attr:"+attNameFromTable);
			for(int i=0;i<AttrTagsName.length;i++)
			{	//WriteLog("Attr111:"+AttrTagsName[i]);
				if(AttrTagsName[i].equalsIgnoreCase(attrtag))
				{	
					flg = "Y";
					break;
				}
			}
			if(flg.equalsIgnoreCase("N"))
			{
				WriteLog("No Value Mapped for name");
				throw new WICreateException("1017",pCodes.getString("1017")+" :"+attrtag);
			}
		}
		
		for(int i=0;i<AttrTagsName.length;i++)
		{
			//WriteLog("AttrTagsName[i]:"+AttrTagsName[i]);	
			//WriteLog("ExtColsName[i]:"+ExtColsName[i]);	
			//WriteLog("MandateList[i]:"+MandateList[i]);	
			String attributeValue = hmt.get(AttrTagsName[i]);
			if(((MandateList[i].equalsIgnoreCase("Y") || MandateList[i].equalsIgnoreCase("N")) 
					&& !attributeValue.equalsIgnoreCase("")) || (MandateList[i].equalsIgnoreCase("N") && attributeValue.equalsIgnoreCase("")))
			{
				if (!ExtColsName[i].equalsIgnoreCase(""))
				{
					attributeTag=attributeTag+ExtColsName[i]+(char)21+attributeValue+(char)25;
				}
				else
					 throw new WICreateException("1015",pCodes.getString("1015")+" :"+AttrTagsName[i]);
			}
			else if(MandateList[i].equalsIgnoreCase("Y") && attributeValue.equalsIgnoreCase(""))
			{
				 throw new WICreateException("1016",pCodes.getString("1016")+" :"+AttrTagsName[i]);
			}
		}
				
	}
	
	private void getTableName() throws WICreateException, Exception
	{
		//from tables here
		String getTableNameQry="select EXTERNALTABLE ,TRANSACTIONTABLE  from "+WSR_PROCESS+" with(nolock) where PROCESSID='"+processID+"'";
		sInputXML=getAPSelectWithColumnNamesXML(getTableNameQry);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		externalTableName=getTagValues(sOutputXML, "EXTERNALTABLE");
		WriteLog("External table Name: "+externalTableName);
		transactionTableName=getTagValues(sOutputXML, "TRANSACTIONTABLE");
		WriteLog("Transaction table Name: "+transactionTableName);
	}
	private void validateRequestParameters(String attributeList[]) throws WICreateException,Exception
	{
		WriteLog("Inside validateRequestParameters");
		if(sProcessName.equalsIgnoreCase("?") || sProcessName.equalsIgnoreCase(""))
			throw new WICreateException("1001",pCodes.getString("1001"));
		if((sSubProcess.equalsIgnoreCase("?") || sSubProcess.equalsIgnoreCase("")) && (!sProcessName.trim().equalsIgnoreCase("CAC")) && (!sProcessName.trim().equalsIgnoreCase("RMT")) && (!sProcessName.trim().equalsIgnoreCase("OECD")))
			throw new WICreateException("1002",pCodes.getString("1002"));
		if(!(sInitiateAlso.equalsIgnoreCase("Y") || sInitiateAlso.equalsIgnoreCase("N")))
			throw new WICreateException("1003",pCodes.getString("1003"));
		if(sRequestorChannelId.equalsIgnoreCase("?") || sRequestorChannelId.equalsIgnoreCase(""))
			throw new WICreateException("1011",pCodes.getString("1011"));
		//Fetch ProcessDefID
		getProcessDefID();	
		
		//Check if all Mandatory attributes present in USR_0_WSR_ATTRDETAILS have come
		if("iRBL".equalsIgnoreCase(sProcessName)){
			checkMandatoryAttributeIRBL();
		}
		//For Digital AO Process
		else if("DigitalAO".equalsIgnoreCase(sProcessName))
		{
			checkMandatoryAttributeDAO();
		}
		else if("Digital_CC".equalsIgnoreCase(sProcessName))
		{
			checkMandatoryAttributeDCC();
		}
		else 
			checkMandatoryAttribute();
		
		Date d= new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sDate = dateFormat.format(d);
		wsname="PBO";
		if("iRBL".equalsIgnoreCase(sProcessName)){
			// nothing to do
		}else 
			attributeTag="ws_name"+(char)21+wsname+(char)25+"Introduction_DateTime"+(char)21+sDate+(char)25+"IntoducedBy"+(char)21+sUsername+(char)25+"SubCategory"+(char)21+sSubProcess+(char)25+"ProcessName"+(char)21+sProcessName+(char)25+"Category"+(char)21+categoryName+(char)25+"Channel"+(char)21+sRequestorChannelId+(char)25;
		
		if(!"CDOB".equalsIgnoreCase(sProcessName) && !"iRBL".equalsIgnoreCase(sProcessName)){
			for(int i=0;i<attributeObj.length;i++)
			{
				if(attributeObj[i].getName().equalsIgnoreCase("CardSubType") &&  attributeObj[i].getValue().equalsIgnoreCase(""))
				{
					//System.out.println("over here no value of Card SubType specified::::::");
				}
				else	
				checkAttributeTable(attributeObj[i].getName(),attributeObj[i].getValue());
			}
		}
		
		if("iRBL".equalsIgnoreCase(sProcessName)){
			checkExtTableAttrIBPS(hm);
		}
		
		getTableName();
		
		//Validating request parameters for CAC process on the basis of Customer Type
		if(sProcessName.equalsIgnoreCase("CAC"))
		{
			validateInputValuesFunction(attributeList);
			validateConditionalRequestParameters(sProcessName);
		}
		//Validating request parameters for CAC process on the basis of Customer Type
		else if(sProcessName.equalsIgnoreCase("RMT"))
		{
			validateConditionalRequestParameters(sProcessName);
		}
		else if(sProcessName.equalsIgnoreCase("OECD"))
		{
			validateConditionalRequestParameters(sProcessName);
		}
		//*****************Validating request parameters for ACP process on the basis of Payment Type
		else if(sProcessName.equalsIgnoreCase("ACP"))
		{
			validateConditionalRequestParameters(sProcessName);
		}		
		//**********************************
		
		
	}
	private void checkMandatoryAttribute() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryAttribute");
		sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from "+WSR_ATTRDETAILS+" with(nolock) where PROCESSID='"+processID+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
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
			        //WriteLog("The key is "+me.getKey());
			        if(me.getKey().toString().equalsIgnoreCase(attributeList[i])){
			        	flag="Y";
			        	break;
			        }
			   
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
	
	private void checkMandatoryAttributeIRBL() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryAttributeIRBL");
		String attributeList [];
		if(hmExtMandIRBL.size() == 0)
		{
			sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from "+WSR_ATTRDETAILS+" with(nolock) where PROCESSID='"+processID+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
			WriteLog("Input XML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			attributeList=getTagValues(sOutputXML,"ATTRIBUTENAME").split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					hmExtMandIRBL.put(attributeList[i], "");
				}
			}	
		}
		else 
		{
			String AttrTagName = "";
			for (String name: hmExtMandIRBL.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
			}
			attributeList=AttrTagName.split("`");
		}
		
		
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
			        //WriteLog("The key is "+me.getKey());
			        if(me.getKey().toString().equalsIgnoreCase(attributeList[i])){
			        	flag="Y";
			        	break;
			        }
			   
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
	
	private void checkMandatoryAttributeDAO() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryAttributeDAO");
		String attributeList [];
		if(hmExtMandDAO.size() == 0)
		{
			sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from "+WSR_ATTRDETAILS+" with(nolock) where PROCESSID='"+processID+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
			WriteLog("Input XML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			attributeList=getTagValues(sOutputXML,"ATTRIBUTENAME").split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					hmExtMandDAO.put(attributeList[i], "");
				}
			}	
		}
		else 
		{
			String AttrTagName = "";
			for (String name: hmExtMandDAO.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
			}
			attributeList=AttrTagName.split("`");
		}
		
		
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
			        //WriteLog("The key is "+me.getKey());
			        if(me.getKey().toString().equalsIgnoreCase(attributeList[i])){
			        	flag="Y";
			        	break;
			        }
			   
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
			
	} //Hritik 5.4.22
	
	

	private void checkMandatoryAttributeDCC() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryAttributeDCC");
		String attributeList [];
		if(hmExtMandDCC.size() == 0)
		{
			sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME from "+WSR_ATTRDETAILS+" with(nolock) where PROCESSID='"+processID+"' and ISMANDATORY='Y' and ISACTIVE='Y'");
			WriteLog("Input XML: "+sInputXML);
			sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			attributeList=getTagValues(sOutputXML,"ATTRIBUTENAME").split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					hmExtMandDCC.put(attributeList[i], "");
				}
			}	
		}
		else 
		{
			String AttrTagName = "";
			for (String name: hmExtMandDCC.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
			}
			attributeList=AttrTagName.split("`");
		}
		
		
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
			        //WriteLog("The key is "+me.getKey());
			        if(me.getKey().toString().equalsIgnoreCase(attributeList[i])){
			        	flag="Y";
			        	break;
			        }
			   
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
			
	} //Hritik 5.4.22
	
	
	
	
	private void validateConditionalRequestParameters(String ProcessName) throws WICreateException, Exception
	{
		if("CAC".equalsIgnoreCase(ProcessName))
		{
		WriteLog("inside validateConditionalRequestParameters");
		
		ApplicationType=hm.get("ApplicationType");
		//CustomerType=hm.get("CustomerType");
		IsPrimaryExisiting=hm.get("IsPrimaryExisiting");
		PrimaryCIFID=hm.get("PrimaryCIFID");
		//PrimaryExistingCardNo=hm.get("PrimaryExistingCardNo");
		//MailingAddress=hm.get("MailingAddress");
		//POBOX=hm.get("POBOX");
		//PrimaryTitle=hm.get("PrimaryTitle");
		//PrimaryMMN=hm.get("PrimaryMMN");
		//PrimaryGender=hm.get("PrimaryGender");
		//PrimaryResCountry=hm.get("PrimaryResCountry");
		//PrimaryProfession=hm.get("PrimaryProfession");
		//PrimaryMonthInc=hm.get("PrimaryMonthInc");
		//PrimaryDocType=hm.get("PrimaryDocType");
		//PrimaryDocExpDate=hm.get("PrimaryDocExpDate");
		//PrimaryDocNumber=hm.get("PrimaryDocNumber");
		//PrimaryMaritalStatus=hm.get("PrimaryMaritalStatus");
		//PrimaryEmpType=hm.get("PrimaryEmpType");
		WriteLog("ApplicationType: "+ApplicationType);
		/*if(ApplicationType.trim().equalsIgnoreCase("JOINT")) {
			IsJointExisiting=hm.get("IsJointExisiting");
			WriteLog("IsJointExisiting: "+IsJointExisiting);
			JointCIFID=hm.get("JointCIFID");
			//JointTitle=hm.get("JointTitle");
			JointFirstName=hm.get("JointFirstName");
			JointLastName=hm.get("JointLastName");
			JointMMN=hm.get("JointMMN");
			//JointGender=hm.get("JointGender");
			JointNationality=hm.get("JointNationality");
			JointDOB=hm.get("JointDOB");
			//JointResCountry=hm.get("JointResCountry");
			JointMobNoCCode=hm.get("JointMobNoCCode");
			JointMobNo=hm.get("JointMobNo");
			//JointProfession=hm.get("JointProfession");
			//JointMonthInc=hm.get("JointMonthInc");
			JointDocType=hm.get("JointDocType");
			//JointDocExpDate=hm.get("JointDocExpDate");
			JointDocNumber=hm.get("JointDocNumber");
			//JointMaritalStatus=hm.get("JointMaritalStatus");
			//JointEmpType=hm.get("JointEmpType");
			//JointEmiratesID=hm.get("JointEmiratesID");
			//JointEmiratesIDExpDate=hm.get("JointEmiratesIDExpDate");
			SigningAuthority = hm.get("SigningAuthority");
		} */
		/*if(ApplicationType.trim().equalsIgnoreCase("JOINT"))
		{
			if(!(SigningAuthority.trim().length()==0))
			{
				if(!(SigningAuthority.trim().equalsIgnoreCase("Any Of Us")))
				{
					throw new WICreateException("8001"," SigningAuthority values should have �Any of Us� in case " +
							"application type is joint for process id: "
							+ processID);
				}
			}
		} */
		//if (!"".equalsIgnoreCase(MailingAddress)) {
		if (ApplicationType.trim().equalsIgnoreCase("SINGLE")){
			if (IsJointExisiting.trim().equalsIgnoreCase("Y")){
				throw new WICreateException("4000",pCodes.getString("4000")+" for process id: "+processID);
			}
			if (IsPrimaryExisiting.trim().equalsIgnoreCase("N")){
				/*if (PrimaryExistingCardNo.trim().length() == 0 || MailingAddress.trim().length() == 0 || POBOX.trim().length() == 0
						|| PrimaryTitle.trim().length() == 0 || PrimaryMMN.trim().length() == 0 || PrimaryGender.trim().length() == 0
						|| PrimaryResCountry.trim().length() == 0 || PrimaryProfession.trim().length() == 0 || PrimaryMonthInc.trim().length() == 0
						|| PrimaryDocType.trim().length() == 0 || PrimaryDocExpDate.trim().length() == 0 || PrimaryDocNumber.trim().length() == 0
						|| PrimaryMaritalStatus.trim().length() == 0 || PrimaryEmpType.trim().length() == 0){
					throw new WICreateException("4003",pCodes.getString("4003")+" for process id: "+processID);
				}*/
			}
			if (PrimaryCIFID.trim().equalsIgnoreCase("") || PrimaryCIFID.trim().length() < 7){
				throw new WICreateException("4002",pCodes.getString("4002")+" for process id: "+processID);
			}
		} if (ApplicationType.trim().equalsIgnoreCase("JOINT")){
			if (PrimaryCIFID.trim().equalsIgnoreCase("")
					|| JointCIFID.trim().equalsIgnoreCase("")) 
				{
					String fieldsFail="JointCIFID";
					throw new WICreateException("4007",
					fieldsFail+" fields is blank for Joint Customer for process id: "
					+ processID);
			    }
			   if (PrimaryCIFID.trim().equalsIgnoreCase("")
						|| JointCIFID.trim().length() < 7) 
			   {
					throw new WICreateException("4002",
							pCodes.getString("4002") + " for process id: "
									+ processID);
			    }
			if (IsPrimaryExisiting.trim().equalsIgnoreCase("Y")){
				//System.out.println("Joint PrimaryExisting Y"+pCodes.getString("4001"));
				throw new WICreateException("4001",pCodes.getString("4001")+" for process id: "+processID);
			} else if (IsPrimaryExisiting.trim().equalsIgnoreCase("N")){
				/*if (PrimaryExistingCardNo.trim().length() == 0 || MailingAddress.trim().length() == 0 || POBOX.trim().length() == 0
						|| PrimaryTitle.trim().length() == 0 || PrimaryMMN.trim().length() == 0 || PrimaryGender.trim().length() == 0
						|| PrimaryResCountry.trim().length() == 0 || PrimaryProfession.trim().length() == 0 || PrimaryMonthInc.trim().length() == 0
						|| PrimaryDocType.trim().length() == 0 || PrimaryDocExpDate.trim().length() == 0 || PrimaryDocNumber.trim().length() == 0
						|| PrimaryMaritalStatus.trim().length() == 0 || PrimaryEmpType.trim().length() == 0){
					throw new WICreateException("4003",pCodes.getString("4003")+" for process id: "+processID);
				}*/
			} else {
				throw new WICreateException("4005",pCodes.getString("4005")+" for process id: "+processID);
			}
			//System.out.println("IsJointExisiting: "+IsJointExisiting);
			if (IsJointExisiting.trim().equalsIgnoreCase("Y") || IsJointExisiting.trim().equalsIgnoreCase("N")){
				String fieldsFail="";
					if (JointCIFID.trim().length() == 0)
						fieldsFail="JointCIFID";
					else if (JointFirstName.trim().length() == 0)
				    	fieldsFail="JointFirstName";
					else if (JointLastName.trim().length() == 0)
				    	fieldsFail="JointLastName";
					else if (JointMMN.trim().length() == 0)
						fieldsFail="JointMMN";
					else if (JointNationality.trim().length() == 0)
						fieldsFail="JointNationality";
					else if (JointDOB.trim().length() == 0)
						fieldsFail="JointDOB";
					else if (JointMobNoCCode.trim().length() == 0)
						fieldsFail="JointMobNoCCode";
					else if(JointMobNo.trim().length() == 0)
						fieldsFail="JointMobNo";
					else if(JointDocType.trim().length() == 0)
						fieldsFail="JointDocType";
					else if(JointDocNumber.trim().length() == 0) 
						fieldsFail="JointDocNumber";
					
					if(fieldsFail.trim().length()!=0)
					{
						throw new WICreateException("4007",
								fieldsFail+" fields is blank for Joint Customer for process id: "
										+ processID);
					}
				
			} else {
				throw new WICreateException("4006",pCodes.getString("4006")+" for process id: "+processID);
			}
			}
		}
		else if("RMT".equalsIgnoreCase(ProcessName)){
			
			WriteLog("inside validation for RMT");
			String FacialScore = hm.get("FACIALSCORE");
			int FacialScoreValue = Integer.parseInt(FacialScore);
			String CIFNumber ="";
			String EMPLOYMENTTYPE = "";
			String INDUSTRY = "";
			String POLITICALLYEXPOSEDPERSON ="";
			String DEMOGRAPHIC = "";
			String ACCOUNTNUMBER = "";
			String PRODUCTTYPE = "";
			String PRODUCTCURRENCY = "";
			String CRSUNDOCUMENTEDFLAG = "";
			// below check will be mandatory only when facial score is greater than or equal to 4
			if (FacialScoreValue >= 4)
			{
				try {	
					if (hm.containsKey("CIFNUMBER"))
						CIFNumber=hm.get("CIFNUMBER");
				} catch (Exception e)
				{
					throw new WICreateException("5000",pCodes.getString("5000")+" for process id: "+processID);
				}
				if (CIFNumber.trim().equalsIgnoreCase("")){
					throw new WICreateException("5000",pCodes.getString("5000")+" for process id: "+processID);
				}
				else
				{ 
					if (CIFNumber.trim().length() != 7)
					{
						throw new WICreateException("5001",pCodes.getString("5001")+" for process id: "+processID);
					}
				}
				
				try {	
					if (hm.containsKey("EMPLOYMENTTYPE"))
						EMPLOYMENTTYPE = hm.get("EMPLOYMENTTYPE");
				} catch (Exception e)
				{
					throw new WICreateException("5002",pCodes.getString("5002")+" for process id: "+processID);
				}
				if (EMPLOYMENTTYPE.trim().equalsIgnoreCase("")){
					throw new WICreateException("5003",pCodes.getString("5003")+" for process id: "+processID);
				}
				
				try {	
					if (hm.containsKey("INDUSTRY"))
						INDUSTRY = hm.get("INDUSTRY");
				} catch (Exception e)
				{
					throw new WICreateException("5004",pCodes.getString("5004")+" for process id: "+processID);
				}
				if (INDUSTRY.trim().equalsIgnoreCase("")){
					throw new WICreateException("5005",pCodes.getString("5005")+" for process id: "+processID);
				}
				
				try {	
					if (hm.containsKey("POLITICALLYEXPOSEDPERSON"))
						POLITICALLYEXPOSEDPERSON = hm.get("POLITICALLYEXPOSEDPERSON");
				} catch (Exception e)
				{
					throw new WICreateException("5006",pCodes.getString("5006")+" for process id: "+processID);
				}
				if (POLITICALLYEXPOSEDPERSON.trim().equalsIgnoreCase("")){
					throw new WICreateException("5007",pCodes.getString("5007")+" for process id: "+processID);
				}
				
				try {	
					if (hm.containsKey("DEMOGRAPHIC"))
						DEMOGRAPHIC = hm.get("DEMOGRAPHIC");
				} catch (Exception e)
				{
					throw new WICreateException("5008",pCodes.getString("5008")+" for process id: "+processID);
				}
				if (DEMOGRAPHIC.trim().equalsIgnoreCase("")){
					throw new WICreateException("5009",pCodes.getString("5009")+" for process id: "+processID);
				}
				
				// Removed AccountNumber Mandatory validation as asked by Projects 30/08/2018
				/*try {
					if (hm.containsKey("ACCOUNTNUMBER"))
						ACCOUNTNUMBER = hm.get(s"ACCOUNTNUMBER");
				} catch (Exception e)
				{
					throw new WICreateException("5010",pCodes.getString("5010")+" for process id: "+processID);
				}
				if (ACCOUNTNUMBER.trim().equalsIgnoreCase("")){
					throw new WICreateException("5011",pCodes.getString("5011")+" for process id: "+processID);
				}*/
				
				
				try {	
					if (hm.containsKey("PRODUCTTYPE"))
						PRODUCTTYPE = hm.get("PRODUCTTYPE");
				} catch (Exception e)
				{
					throw new WICreateException("5012",pCodes.getString("5012")+" for process id: "+processID);
				}
				if (PRODUCTTYPE.trim().equalsIgnoreCase("")){
					throw new WICreateException("5013",pCodes.getString("5013")+" for process id: "+processID);
				}
				
				try {	
					if (hm.containsKey("PRODUCTCURRENCY"))
						PRODUCTCURRENCY = hm.get("PRODUCTCURRENCY");
				} catch (Exception e)
				{
					throw new WICreateException("5014",pCodes.getString("5014")+" for process id: "+processID);
				}
				if (PRODUCTCURRENCY.trim().equalsIgnoreCase("")){
					throw new WICreateException("5015",pCodes.getString("5015")+" for process id: "+processID);
				}
				
				try {	
					if (hm.containsKey("CRSUNDOCUMENTEDFLAG"))
						CRSUNDOCUMENTEDFLAG = hm.get("CRSUNDOCUMENTEDFLAG");
				} catch (Exception e)
				{
					throw new WICreateException("5016",pCodes.getString("5016")+" for process id: "+processID);
				}
				if (CRSUNDOCUMENTEDFLAG.trim().equalsIgnoreCase("")){
					throw new WICreateException("5017",pCodes.getString("5017")+" for process id: "+processID);
				}
			}
		}
		
		else if("OECD".equalsIgnoreCase(ProcessName)){
			String CIFNumber ="";
			WriteLog("inside validation for OECD");
			if (hm.containsKey("CIFNUMBER"))
				CIFNumber=hm.get("CIFNUMBER");
			
			if (!(CIFNumber.trim().equalsIgnoreCase("")) && !(CIFNumber.trim().length() == 7)){
				throw new WICreateException("5001",pCodes.getString("5001")+" for process id: "+processID);
			}
			
			//Added as part of CR on 30102020**************
			String SUBMISSIONMODE ="";
			String RMCODE ="";
			
			WriteLog("SUBMISSIONMODE: "+SUBMISSIONMODE);
			if(hm.containsKey("SUBMISSIONMODE"))
				SUBMISSIONMODE=hm.get("SUBMISSIONMODE");
			if(SUBMISSIONMODE.trim().equalsIgnoreCase("Offline"))
			{
				if (hm.containsKey("RMCODE"))
					RMCODE=hm.get("RMCODE");
				
				WriteLog("RMCODE :"+RMCODE);					
				if(RMCODE.trim().equalsIgnoreCase(""))
				{
					throw new WICreateException("4051",pCodes.getString("4051")+" :");
				}
			}
			//Added as part of CR on 30102020***************
		}
		else if("ACP".equalsIgnoreCase(ProcessName)){			
			//*****************
			WriteLog("before PAYMENTTYPE :"+ProcessName);
			String PAYMENTTYPE="";
			String RAKBANKACCNUMBER="";
			if (hm.containsKey("PAYMENTTYPE"))
				PAYMENTTYPE=hm.get("PAYMENTTYPE");
			
			WriteLog("PAYMENTTYPE :"+PAYMENTTYPE);
			if(PAYMENTTYPE.equalsIgnoreCase("FUND TRANSFER RAK"))
			{
				if (hm.containsKey("RAKBANKACCNUMBER"))
					RAKBANKACCNUMBER=hm.get("RAKBANKACCNUMBER");
				
				WriteLog("RAKBANKACCNUMBER :"+RAKBANKACCNUMBER);					
				if(RAKBANKACCNUMBER.equalsIgnoreCase(""))
				{
					throw new WICreateException("5018",pCodes.getString("5018")+" :");
				}
			}	
			String OTHERBANKNAME="";
			if(PAYMENTTYPE.equalsIgnoreCase("FUND TRANSFER OTHER BANK"))
			{
				if (hm.containsKey("OTHERBANKNAME"))
					OTHERBANKNAME=hm.get("OTHERBANKNAME");
				
				WriteLog("OTHERBANKNAME :"+OTHERBANKNAME);					
				if(OTHERBANKNAME.equalsIgnoreCase(""))
				{
					throw new WICreateException("5019",pCodes.getString("5019")+" :");
				}
			}
			
			String OTHERBANKIBANNUMBER="";
			if(PAYMENTTYPE.equalsIgnoreCase("FUND TRANSFER OTHER BANK"))
			{
				if (hm.containsKey("OTHERBANKIBANNUMBER"))
					OTHERBANKIBANNUMBER=hm.get("OTHERBANKIBANNUMBER");
				
				WriteLog("OTHERBANKIBANNUMBER :"+OTHERBANKIBANNUMBER);					
				if(OTHERBANKIBANNUMBER.equalsIgnoreCase(""))
				{
					throw new WICreateException("5020",pCodes.getString("5020")+" :");
				}
			}
			//**************
		}
		
	}
	
	// Start - adding methods for repetitive tags added on 26/10/2020
	private void validateRepetitiveRequestParameters() throws WICreateException, Exception
	{
		String repetitiveListMain[];
		String repetitiveList[];
		
		RepetitiveMainTags = checkMandatoryRepetitiveTags();
		if(!"".equalsIgnoreCase(RepetitiveMainTags))
		{
			String attributeList []=RepetitiveMainTags.split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					if(InputMessage.contains(attributeList[i])) 
					{
						repetitiveListMain=getTagValues(InputMessage, attributeList[i]).split("`");
			        	//HashMap<String, HashMap<String, String>> hmMain = new HashMap();
			        	for(int j=0;j<repetitiveListMain.length;j++)
			        	{	
			        		HashMap<String, String> hm1 = new HashMap(); 
			            	repetitiveList=getTagValues(repetitiveListMain[j], "Attribute").split("`");
			        		for(int k=0;k<repetitiveList.length;k++)
			            	{
			        			String attrName=getTagValues(repetitiveList[k],"Name");
				        		String attrValue=getTagValues(repetitiveList[k],"Value");
				    			
				        		hm1.put(attrName, attrValue);
			        		}
			        		if(hm1.size() != 0)
			        		{
			        			String RepetitiveProcessID = getRepetitiveProcessIdOrTransactionTableName(attributeList[i],"PROCESSID");
				        		checkMandatoryRepetitiveAttribute(RepetitiveProcessID,"Y","ATTRIBUTENAME",hm1);
				        		hmMain.put(attributeList[i]+"-"+Integer.toString(j), hm1);
				        	}
			        	}
					}
				}
			}
		}
			
		
		
	}
	
	private void validateRepetitiveRequestParametersIRBL() throws WICreateException, Exception
	{
		String repetitiveListMain[];
		String repetitiveList[];
		
		RepetitiveMainTags = checkMandatoryRepetitiveTagsIRBL();
		if(!"".equalsIgnoreCase(RepetitiveMainTags))
		{
			String attributeList []=RepetitiveMainTags.split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					if(InputMessage.contains(attributeList[i])) 
					{
						repetitiveListMain=getTagValues(InputMessage, attributeList[i]).split("`");
			        	for(int j=0;j<repetitiveListMain.length;j++)
			        	{	
			        		HashMap<String, String> hm1 = new HashMap(); 
			            	repetitiveList=getTagValues(repetitiveListMain[j], "Attribute").split("`");
			        		for(int k=0;k<repetitiveList.length;k++)
			            	{
			        			String attrName=getTagValues(repetitiveList[k],"Name");
				        		String attrValue=getTagValues(repetitiveList[k],"Value");
				    			
				        		hm1.put(attrName, attrValue);
			        		}
			        		if(hm1.size() != 0)
			        		{
			        			String RepetitiveProcessID = hmRptProcessIdIRBL.get(attributeList[i]);
			        			checkMandatoryRepetitiveAttributeIRBL(RepetitiveProcessID,"Y",hm1);
				        		hmMain.put(attributeList[i]+"-"+Integer.toString(j), hm1);
				        	}
			        	}
					}
				}
			}
		}
			
		
		
	}
	private void validateRepetitiveRequestParametersDAO() throws WICreateException, Exception
	{
		String repetitiveListMain[];
		String repetitiveList[];
		
		RepetitiveMainTags = checkMandatoryRepetitiveTagsDAO();
		if(!"".equalsIgnoreCase(RepetitiveMainTags))
		{
			String attributeList []=RepetitiveMainTags.split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					if(InputMessage.contains(attributeList[i])) 
					{
						repetitiveListMain=getTagValues(InputMessage, attributeList[i]).split("`");
			        	for(int j=0;j<repetitiveListMain.length;j++)
			        	{	
			        		HashMap<String, String> hm1 = new HashMap<String, String>(); 
			        		repetitiveList=getTagValues(repetitiveListMain[j], "Attribute").split("`");			            	
			        		for(int k=0;k<repetitiveList.length;k++)
			            	{
			        			String attrName=getTagValues(repetitiveList[k],"Name");
				        		String attrValue=getTagValues(repetitiveList[k],"Value");
				    			
				        		hm1.put(attrName, attrValue);
			        		}
			        		if(hm1.size() != 0)
			        		{
			        			String RepetitiveProcessID = hmRptProcessIdDAO.get(attributeList[i]);
			        			checkMandatoryRepetitiveAttributeDAO(RepetitiveProcessID,"Y",hm1);
				        		hmMainDAO.put(attributeList[i]+"-"+Integer.toString(j), hm1);
				        	}
			        	}
					}
				}
			}
		}
	}
	
	private void validateRepetitiveRequestParametersBAIS() throws WICreateException, Exception
	{
		String repetitiveListMain[];
		String repetitiveList[];
		WriteLog("Testing-->"+!"".equalsIgnoreCase(RepetitiveMainTags));
		WriteLog("RepetitiveMainTags-->"+RepetitiveMainTags);
		RepetitiveMainTags = checkMandatoryRepetitiveTagsBAIS();
		if(!"".equalsIgnoreCase(RepetitiveMainTags))
		{
			String attributeList []=RepetitiveMainTags.split("`");
			WriteLog("Length-->"+attributeList.length);
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					if(InputMessage.contains(attributeList[i])) 
					{
						repetitiveListMain=getTagValues(InputMessage, attributeList[i]).split("`");
			        	for(int j=0;j<repetitiveListMain.length;j++)
			        	{	
			        		HashMap<String, String> hm1 = new HashMap();
			        		repetitiveList=getTagValues(repetitiveListMain[j], "Attribute").split("`");			            	
			        		for(int k=0;k<repetitiveList.length;k++)
			            	{
			        			String attrName=getTagValues(repetitiveList[k],"Name");
				        		String attrValue=getTagValues(repetitiveList[k],"Value");
				    			WriteLog("attrName-->"+attrName);
				    			WriteLog("attrValue--->"+attrValue);
				        		hm1.put(attrName, attrValue);
			        		}
			        		if(hm1.size() != 0)
			        		{
			        			String RepetitiveProcessID = hmRptProcessIdBAIS.get(attributeList[i]);
			        			checkMandatoryRepetitiveAttributeBAIS(RepetitiveProcessID,"Y",hm1);
				        		hmMainBAIS.put(attributeList[i]+"-"+Integer.toString(j), hm1);
				        	}
			        	}
					}
				}
			}
		}
	}
	
	/** Ravindra Kumar -- Validate repetitive parameters  for dcc **/
	private void validateRepetitiveRequestParametersDCC() throws WICreateException, Exception
	{
		String repetitiveListMain[];
		String repetitiveList[];
		
		RepetitiveMainTags = checkMandatoryRepetitiveTagsDCC();
		if(!"".equalsIgnoreCase(RepetitiveMainTags))
		{
			String attributeList []=RepetitiveMainTags.split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					if(InputMessage.contains(attributeList[i])) 
					{
						repetitiveListMain=getTagValues(InputMessage, attributeList[i]).split("`");
			        	for(int j=0;j<repetitiveListMain.length;j++)
			        	{	
			        		HashMap<String, String> hm1 = new HashMap(); 
			            	repetitiveList=getTagValues(repetitiveListMain[j], "Attribute").split("`");
			        		for(int k=0;k<repetitiveList.length;k++)
			            	{
			        			String attrName=getTagValues(repetitiveList[k],"Name");
				        		String attrValue=getTagValues(repetitiveList[k],"Value");
				    			
				        		hm1.put(attrName, attrValue);
			        		}
			        		if(hm1.size() != 0)
			        		{
			        			String RepetitiveProcessID = hmRptProcessIdDCC.get(attributeList[i]);
			        			checkMandatoryRepetitiveAttributeDCC(RepetitiveProcessID,"Y",hm1);
				        		hmMainDCC.put(attributeList[i]+"-"+Integer.toString(j), hm1);
				        	}
			        	}
					}
				}
			}
		}
	} 
	
	//Reddy 7.6.22 BSR Process
	private void validateRepetitiveRequestParametersBSR() throws WICreateException, Exception
	{
		String repetitiveListMain[];
		String repetitiveList[];
		
		RepetitiveMainTags = checkMandatoryRepetitiveTagsBSR();
		if(!"".equalsIgnoreCase(RepetitiveMainTags))
		{
			String attributeList []=RepetitiveMainTags.split("`");
			if(attributeList.length>0)
			{
				for(int i=0;i<attributeList.length;i++)
				{
					if(InputMessage.contains(attributeList[i])) 
					{
						repetitiveListMain=getTagValues(InputMessage, attributeList[i]).split("`");
			        	for(int j=0;j<repetitiveListMain.length;j++)
			        	{	
			        		HashMap<String, String> hm1 = new HashMap(); 
			            	repetitiveList=getTagValues(repetitiveListMain[j], "Attribute").split("`");
			        		for(int k=0;k<repetitiveList.length;k++)
			            	{
			        			String attrName=getTagValues(repetitiveList[k],"Name");
				        		String attrValue=getTagValues(repetitiveList[k],"Value");
				    			
				        		hm1.put(attrName, attrValue);
			        		}
			        		if(hm1.size() != 0)
			        		{
			        			String RepetitiveProcessID = hmRptProcessIdBSR.get(attributeList[i]);
			        			checkMandatoryRepetitiveAttributeBSR(RepetitiveProcessID,"Y",hm1);
				        		hmMainBSR.put(attributeList[i]+"-"+Integer.toString(j), hm1);
				        	}
			        	}
					}
				}
			}
		}
	} 
	
	
	private String checkMandatoryRepetitiveTags() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveTags");
		String sInputXML=getAPSelectWithColumnNamesXML("select REPETITIVETAGNAME,ISMANDATORY from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and SUBPROCESSNAME='"+sSubProcess+"' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = getTagValues(sOutputXML,"REPETITIVETAGNAME");
		String isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		String RepetitiveList []=RepetitiveMainTags.split("`");
		String MandateList []=isMandatory.split("`");
		if(MandateList.length>0)
		{
			for(int i=0;i<MandateList.length;i++)
			{
				String flag="Y";
				
				if("Y".equalsIgnoreCase(MandateList[i]))
				{
				    if(InputMessage.contains(RepetitiveList[i]))
				    {
				    	// nothing to do
				    }	
				    else 
				    	flag = "N";
				}
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+RepetitiveList[i]);
			}
		}
		return RepetitiveMainTags;
	}
	
	private String checkMandatoryRepetitiveTagsIRBL() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveTagsIRBL");
		String sInputXML=getAPSelectWithColumnNamesXML("select REPETITIVETAGNAME,PROCESSID,TRANSACTIONTABLE,ISMANDATORY from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and SUBPROCESSNAME='"+sSubProcess+"' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = getTagValues(sOutputXML,"REPETITIVETAGNAME");
		String RepProcessId[] = getTagValues(sOutputXML,"PROCESSID").split("`");
		String RepTransTable[] = getTagValues(sOutputXML,"TRANSACTIONTABLE").split("`");
		String isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		String RepetitiveList []=RepetitiveMainTags.split("`");
		String MandateList []=isMandatory.split("`");
		if(MandateList.length>0)
		{
			for(int i=0;i<MandateList.length;i++)
			{
				String flag="Y";
				
				if("Y".equalsIgnoreCase(MandateList[i]))
				{
				    if(InputMessage.contains(RepetitiveList[i]))
				    {
				    	// nothing to do
				    }	
				    else 
				    	flag = "N";
				}
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+RepetitiveList[i]);
			
			    hmRptProcessIdIRBL.put(RepetitiveList[i], RepProcessId[i]);
			    hmRptTransTableIRBL.put(RepProcessId[i], RepTransTable[i]);
			}
		}
		return RepetitiveMainTags;
	}
	
	
	private String checkMandatoryRepetitiveTagsDAO() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveTagsDAO");
		String sInputXML=getAPSelectWithColumnNamesXML("select REPETITIVETAGNAME,PROCESSID,TRANSACTIONTABLE,ISMANDATORY from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and (SUBPROCESSNAME='"+hm.get("employer_type")+"' or SUBPROCESSNAME='ALL') and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = getTagValues(sOutputXML,"REPETITIVETAGNAME");
		String RepProcessId[] = getTagValues(sOutputXML,"PROCESSID").split("`");
		String RepTransTable[] = getTagValues(sOutputXML,"TRANSACTIONTABLE").split("`");
		String isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		String RepetitiveList []=RepetitiveMainTags.split("`");
		String MandateList []=isMandatory.split("`");
		if(MandateList.length>0)
		{
			for(int i=0;i<MandateList.length;i++)
			{
				String flag="Y";
				
				if("Y".equalsIgnoreCase(MandateList[i]))
				{
				    if(InputMessage.contains(RepetitiveList[i]))
				    {
				    	// nothing to do
				    }	
				    else 
				    	flag = "N";
				}
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+RepetitiveList[i]);
			
			    hmRptProcessIdDAO.put(RepetitiveList[i], RepProcessId[i]);
			    hmRptTransTableDAO.put(RepProcessId[i], RepTransTable[i]);
			}
		}
		return RepetitiveMainTags;
	} //Hritik 5.4.22 
	//By Sowmya
	private String checkMandatoryRepetitiveTagsBAIS() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveTagsDAO");
		String sInputXML=getAPSelectWithColumnNamesXML("select REPETITIVETAGNAME,PROCESSID,TRANSACTIONTABLE,ISMANDATORY from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and SUBPROCESSNAME='"+sSubProcess+"' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = getTagValues(sOutputXML,"REPETITIVETAGNAME");
		String RepProcessId[] = getTagValues(sOutputXML,"PROCESSID").split("`");
		String RepTransTable[] = getTagValues(sOutputXML,"TRANSACTIONTABLE").split("`");
		String isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		String RepetitiveList []=RepetitiveMainTags.split("`");
		String MandateList []=isMandatory.split("`");
		if(MandateList.length>0)
		{
			for(int i=0;i<MandateList.length;i++)
			{
				String flag="Y";
				
				if("Y".equalsIgnoreCase(MandateList[i]))
				{
				    if(InputMessage.contains(RepetitiveList[i]))
				    {
				    	// nothing to do
				    }	
				    else 
				    	flag = "N";
				}
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+RepetitiveList[i]);
			
			    hmRptProcessIdBAIS.put(RepetitiveList[i], RepProcessId[i]);
			    hmRptTransTableBAIS.put(RepProcessId[i], RepTransTable[i]);
			}
		}
		return RepetitiveMainTags;
	} 
	

	private String checkMandatoryRepetitiveTagsDCC() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveTagsDCC");
		String sInputXML=getAPSelectWithColumnNamesXML("select REPETITIVETAGNAME,PROCESSID,TRANSACTIONTABLE,ISMANDATORY from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and SUBPROCESSNAME='ALL' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = getTagValues(sOutputXML,"REPETITIVETAGNAME");
		String RepProcessId[] = getTagValues(sOutputXML,"PROCESSID").split("`");
		String RepTransTable[] = getTagValues(sOutputXML,"TRANSACTIONTABLE").split("`");
		String isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		String RepetitiveList []=RepetitiveMainTags.split("`");
		String MandateList []=isMandatory.split("`");
		if("Y".equalsIgnoreCase(hm.get("risk_score_flag"))){
			int Background_info_index = Arrays.asList(RepetitiveList).indexOf("Background_info_employer");
			MandateList[Background_info_index]="Y";
		}
		if(MandateList.length>0)
		{
			for(int i=0;i<MandateList.length;i++)
			{
				String flag="Y";
				
				if("Y".equalsIgnoreCase(MandateList[i]))
				{
				    if(InputMessage.contains(RepetitiveList[i]))
				    {
				    	// nothing to do
				    }	
				    else 
				    	flag = "N";
				}
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+RepetitiveList[i]);
			
			    hmRptProcessIdDCC.put(RepetitiveList[i], RepProcessId[i]);
			    hmRptTransTableDCC.put(RepProcessId[i], RepTransTable[i]);
			}
		}
		return RepetitiveMainTags;
	} //Hritik 5.4.22 
	
	//Reddy 7.6.22 BSR Process 
	private String checkMandatoryRepetitiveTagsBSR() throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveTagsBSR");
		String sInputXML=getAPSelectWithColumnNamesXML("select REPETITIVETAGNAME,PROCESSID,TRANSACTIONTABLE,ISMANDATORY from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and SUBPROCESSNAME='"+sSubProcess+"' and ISACTIVE='Y'");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String RepetitiveMainTags = getTagValues(sOutputXML,"REPETITIVETAGNAME");
		String RepProcessId[] = getTagValues(sOutputXML,"PROCESSID").split("`");
		String RepTransTable[] = getTagValues(sOutputXML,"TRANSACTIONTABLE").split("`");
		String isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		String RepetitiveList []=RepetitiveMainTags.split("`");
		String MandateList []=isMandatory.split("`");
		if(MandateList.length>0)
		{
			for(int i=0;i<MandateList.length;i++)
			{
				String flag="Y";
				
				if("Y".equalsIgnoreCase(MandateList[i]))
				{
				    if(InputMessage.contains(RepetitiveList[i]))
				    {
				    	// nothing to do
				    }	
				    else 
				    	flag = "N";
				}
			    if(flag.equalsIgnoreCase("N"))
					throw new WICreateException("1020",pCodes.getString("1020")+": "+RepetitiveList[i]);
			
			    hmRptProcessIdBSR.put(RepetitiveList[i], RepProcessId[i]);
			    hmRptTransTableBSR.put(RepProcessId[i], RepTransTable[i]);
			}
		}
		return RepetitiveMainTags;
	} 
	
	
	
	private String checkMandatoryRepetitiveAttribute(String RepetitiveProcessId,String isValidatingMandate,String ColName, HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveAttribute");
		
		String sInputXML=getAPSelectWithColumnNamesXML("select "+ColName+",ISMANDATORY from USR_0_WSR_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"+RepetitiveProcessId+"' and ISACTIVE='Y' order by AttributeName");
		WriteLog("Input XML: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("Output XML: "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String AttrTagName = getTagValues(sOutputXML,ColName);
		String isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		
		if(isValidatingMandate.equalsIgnoreCase("Y"))
		{	
			String AttrTagsName []=AttrTagName.split("`");
			String MandateList []=isMandatory.split("`");
			if(MandateList.length>0)
			{
				for(int i=0;i<MandateList.length;i++)
				{
					String flag="Y";
					
					if("Y".equalsIgnoreCase(MandateList[i]))
					{
						if(RepetitiveReqAttr.containsKey(AttrTagsName[i]))
						{	
						    if(RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
						    	flag = "N";
						    else {
						    	// nothing to do
						    }
						}
						else
							flag = "N";	
					}
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+AttrTagsName[i]+" for "+ RepetitiveProcessId);
				}
			}
			else
			{
				throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
			}
			
			if("OECD_RelPartyDet".equalsIgnoreCase(RepetitiveProcessId))
			{
				String flag="Y";
				if(RepetitiveReqAttr.containsKey("CIFID") && !RepetitiveReqAttr.get("CIFID").trim().equalsIgnoreCase(""))
				{	
					flag = "N";
					String patternMatch="[0-9]+";
					if(!Pattern.matches(patternMatch, RepetitiveReqAttr.get("CIFID").trim()))
						throw new WICreateException("1112",pCodes.getString("1112")+" :CIFID");
					else if (RepetitiveReqAttr.get("CIFID").trim().length() != 7)
						throw new WICreateException("5001",pCodes.getString("5001")+" for Related CIF");
				}	
				
				if(RepetitiveReqAttr.containsKey("FINANCIALDETAILID") && !RepetitiveReqAttr.get("FINANCIALDETAILID").trim().equalsIgnoreCase(""))
					flag = "N";
				
				if(flag.equalsIgnoreCase("Y"))
					throw new WICreateException("4050",pCodes.getString("4050"));
			}	
		}
		return AttrTagName;
	}
	
	private String checkMandatoryRepetitiveAttributeIRBL(String RepetitiveProcessId,String isValidatingMandate, HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveAttributeIBPS");
		String AttrTagName = "";
		String TransColName = "";
		String isMandatory = "";
		if(!hmRptAttrAndColIRBL.containsKey(RepetitiveProcessId))
		{
			String sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME,TRANSACTIONTABLECOLNAME,ISMANDATORY from USR_0_WSR_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"+RepetitiveProcessId+"' and ISACTIVE='Y' order by AttributeName");
			WriteLog("Input XML: "+sInputXML);
			String sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			AttrTagName = getTagValues(sOutputXML,"ATTRIBUTENAME");
			TransColName = getTagValues(sOutputXML,"TRANSACTIONTABLECOLNAME");
			isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		} 
		else
		{
			HashMap<String, String> hmAttr = new HashMap();
			hmAttr = hmRptAttrAndColIRBL.get(RepetitiveProcessId);
			for (String name: hmAttr.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
				
				if(TransColName.equalsIgnoreCase(""))
					TransColName = hmAttr.get(name).toString(); 
				else
					TransColName = TransColName+"`"+hmAttr.get(name).toString();
			}
			
			HashMap<String, String> hmMand = new HashMap();
			hmMand = hmRptAttrAndMandIRBL.get(RepetitiveProcessId);
			for (String name1: hmMand.keySet()){
				if(isMandatory.equalsIgnoreCase(""))
					isMandatory = hmMand.get(name1).toString(); 
				else
					isMandatory = isMandatory+"`"+hmMand.get(name1).toString();
			}
		}
		
		if(isValidatingMandate.equalsIgnoreCase("Y"))
		{	
			String AttrTagsName []=AttrTagName.split("`");
			String TransColsName []=TransColName.split("`");
			String MandateList []=isMandatory.split("`");
			if(MandateList.length>0)
			{
				for(int i=0;i<MandateList.length;i++)
				{
					String flag="Y";
					
					if("Y".equalsIgnoreCase(MandateList[i]))
					{
						if(RepetitiveReqAttr.containsKey(AttrTagsName[i]))
						{	
						    if(RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
						    	flag = "N";
						    else {
						    	// nothing to do
						    }
						}
						else
							flag = "N";	
					}
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+AttrTagsName[i]+" for "+ RepetitiveProcessId);
				}
			}
			else
			{
				throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
			}
			
			
			if(!hmRptAttrAndColIRBL.containsKey(RepetitiveProcessId))
			{
				if(AttrTagsName.length>0)
				{
					HashMap<String, String> hmtmp = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp.put(AttrTagsName[i], TransColsName[i]);
					}
					hmRptAttrAndColIRBL.put(RepetitiveProcessId, hmtmp);
					
					HashMap<String, String> hmtmp1 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp1.put(AttrTagsName[i], MandateList[i]);
					}
					hmRptAttrAndMandIRBL.put(RepetitiveProcessId, hmtmp1);
				}	
			}
			
			
			// Start - Validating conditional mandatory parameters added on 11/07/2021 by Angad
			/*if("iRBLWBA_CondHistRelPartDtls".equalsIgnoreCase(RepetitiveProcessId.trim()))
			{
				String RelationshiType = "";
				if(RepetitiveReqAttr.containsKey("RELATIONSHIPTYPE"))
				{	
					RelationshiType = RepetitiveReqAttr.get("RELATIONSHIPTYPE").trim();
				}
				
				if("Guarantor".equalsIgnoreCase(RelationshiType))
				{
					if(RepetitiveReqAttr.containsKey("GUARANTORYCATEGORY"))
					{	
					    if("".equalsIgnoreCase(RepetitiveReqAttr.get("GUARANTORYCATEGORY").trim()))
					    	throw new WICreateException("1020",pCodes.getString("1020")+": "+"GUARANTORYCATEGORY"+" for "+ RepetitiveProcessId);
					    else {
					    	// nothing to do
					    }
					}
					else
						throw new WICreateException("1020",pCodes.getString("1020")+": "+"GUARANTORYCATEGORY"+" for "+ RepetitiveProcessId);
				}
				
				String CompanyFlag = "";
				if(RepetitiveReqAttr.containsKey("COMPANYFLAG"))
				{	
				    CompanyFlag = RepetitiveReqAttr.get("COMPANYFLAG").trim();
				}
				
				// Start - conditional mandatory on the basis of Company Flag
				if("Y".equalsIgnoreCase(CompanyFlag))
				{
					String [] MandAttr = {"COMPANYCATEGORY","YEAROFINCORPORATION","ISTLVALID","COUNTRY","EMIRATE","ISSISCOCOBORROWERGUARANTOR","NAMEOFSISTERCOMPANYOWNER","COBORROWERGUARANTORSTATUS","SHARESBANKACCOUNT"};
					
					for(int i=0; i < MandAttr.length; i++)
					{
						if(RepetitiveReqAttr.containsKey(MandAttr[i]))
						{	
						    if("".equalsIgnoreCase(RepetitiveReqAttr.get(MandAttr[i]).trim()))
						    	throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
						    else {
						    	// nothing to do
						    }
						}
						else
							throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
					}
				}
				
				if("N".equalsIgnoreCase(CompanyFlag))
				{
					String [] MandAttr = {"FIRSTNAME","LASTNAME","DATEOFBIRTH","EMIRATESID","ISGOVERNMENTRELATION","SIGNATORYFLAG","TYPEOFOWNERSHIP","TYPEOFPROOFPROVIDEDFORLOB","INVOLVEDINBUSINESS","SHAREHOLDINGPERCENTAGE","AUTHORITYTYPE","NATIONALITY","VISASPONSOR","PASSPORTNOVISASPONSOR"};
					
					for(int i=0; i < MandAttr.length; i++)
					{
						if(RepetitiveReqAttr.containsKey(MandAttr[i]))
						{	
						    if("".equalsIgnoreCase(RepetitiveReqAttr.get(MandAttr[i]).trim()))
						    	throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
						    else 
						    {
						    	if("SIGNATORYFLAG".equalsIgnoreCase(MandAttr[i]))
						    	{
						    		String SignFlag = RepetitiveReqAttr.get(MandAttr[i]).trim();
						    		if("Y".equalsIgnoreCase(SignFlag))
						    		{
						    			if(RepetitiveReqAttr.containsKey("SIGNATORYPOWERHELDSINCEDATE"))
										{	
										    if("".equalsIgnoreCase(RepetitiveReqAttr.get("SIGNATORYPOWERHELDSINCEDATE").trim()))
										    	throw new WICreateException("1020",pCodes.getString("1020")+": "+"SIGNATORYPOWERHELDSINCEDATE"+" for "+ RepetitiveProcessId);
										    else {
										    	// nothing to do
										    }
										}
										else
											throw new WICreateException("1020",pCodes.getString("1020")+": "+"SIGNATORYPOWERHELDSINCEDATE"+" for "+ RepetitiveProcessId);
						    		}
						    	}
						    }
						}
						else
							throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
					}
				}
				// End - conditional mandatory on the basis of Company Flag
				
				String GuarantorCategory = "";
				if(RepetitiveReqAttr.containsKey("GUARANTORYCATEGORY"))
				{	
					GuarantorCategory = RepetitiveReqAttr.get("GUARANTORYCATEGORY").trim();
				}
				
				// Start - conditional mandatory on the basis of Guarantor Category
				if("Company".equalsIgnoreCase(GuarantorCategory))
				{
					String [] MandAttr = {"DATEOFINCORPORATION","LINEOFBUSINESS","ISSUINGEMIRATE"};
					
					for(int i=0; i < MandAttr.length; i++)
					{
						if(RepetitiveReqAttr.containsKey(MandAttr[i]))
						{	
						    if("".equalsIgnoreCase(RepetitiveReqAttr.get(MandAttr[i]).trim()))
						    	throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
						    else {
						    	// nothing to do
						    }
						}
						else
							throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
					}
				}
				
				if("Individual".equalsIgnoreCase(GuarantorCategory))
				{
					String [] MandAttr = {"GENDER","COUNTRYOFRESIDENCE","PASSPORTNUMBER","PASSPORTEXPIRYDATE","PASSPORTISSUINGCOUNTRY","PREVIOUSPASSPORTNUMBER","VISANUMBER","COUNTRYOFBIRTH","BACKGROUND","FATHERNAME","MOTHERNAME","SPOUSENAME"};
					
					for(int i=0; i < MandAttr.length; i++)
					{
						if(RepetitiveReqAttr.containsKey(MandAttr[i]))
						{	
						    if("".equalsIgnoreCase(RepetitiveReqAttr.get(MandAttr[i]).trim()))
						    	throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
						    else {
						    	// nothing to do
						    }
						}
						else
							throw new WICreateException("1020",pCodes.getString("1020")+": "+MandAttr[i]+" for "+ RepetitiveProcessId);
					}
				}
				// End - conditional mandatory on the basis of Guarantor Category
			}*/
			// End - Validating conditional mandatory parameters added on 11/07/2021 by Angad
			
		}
		return AttrTagName;
	}
	
	
	private String checkMandatoryRepetitiveAttributeDAO(String RepetitiveProcessId,String isValidatingMandate, HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveAttributeIBPS - DAO");
		String AttrTagName = "";
		String TransColName = "";
		String isMandatory = "";
		if(!hmRptAttrAndColDAO.containsKey(RepetitiveProcessId))
		{
			String sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME,TRANSACTIONTABLECOLNAME,ISMANDATORY from USR_0_WSR_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"+RepetitiveProcessId+"' and ISACTIVE='Y' order by AttributeName");
			WriteLog("Input XML: "+sInputXML);
			String sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			AttrTagName = getTagValues(sOutputXML,"ATTRIBUTENAME");
			TransColName = getTagValues(sOutputXML,"TRANSACTIONTABLECOLNAME");
			isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		} 
		else
		{
			HashMap<String, String> hmAttr = new HashMap();
			hmAttr = hmRptAttrAndColDAO.get(RepetitiveProcessId);
			for (String name: hmAttr.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
				
				if(TransColName.equalsIgnoreCase(""))
					TransColName = hmAttr.get(name).toString(); 
				else
					TransColName = TransColName+"`"+hmAttr.get(name).toString();
			}
			
			HashMap<String, String> hmMand = new HashMap();
			hmMand = hmRptAttrAndMandDAO.get(RepetitiveProcessId);
			for (String name1: hmMand.keySet()){
				if(isMandatory.equalsIgnoreCase(""))
					isMandatory = hmMand.get(name1).toString(); 
				else
					isMandatory = isMandatory+"`"+hmMand.get(name1).toString();
			}
		}
		
		if(isValidatingMandate.equalsIgnoreCase("Y"))
		{	
			String AttrTagsName []=AttrTagName.split("`");
			String TransColsName []=TransColName.split("`");
			String MandateList []=isMandatory.split("`");
			if(MandateList.length>0)
			{
				for(int i=0;i<MandateList.length;i++)
				{
					String flag="Y";
					
					if("Y".equalsIgnoreCase(MandateList[i]))
					{
						if(RepetitiveReqAttr.containsKey(AttrTagsName[i]))
						{	
						    if(RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
						    	flag = "N";
						    else {
						    	// nothing to do
						    }
						}
						else
							flag = "N";	
					}
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+AttrTagsName[i]+" for "+ RepetitiveProcessId);
				}
			}
			else
			{
				throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
			}
			
			
			if(!hmRptAttrAndColDAO.containsKey(RepetitiveProcessId))
			{
				if(AttrTagsName.length>0)
				{
					HashMap<String, String> hmtmp = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp.put(AttrTagsName[i], TransColsName[i]);
					}
					hmRptAttrAndColDAO.put(RepetitiveProcessId, hmtmp);
					
					HashMap<String, String> hmtmp1 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp1.put(AttrTagsName[i], MandateList[i]);
					}
					hmRptAttrAndMandDAO.put(RepetitiveProcessId, hmtmp1);
				}	
			}
		}
		return AttrTagName;
	}
	private String checkMandatoryRepetitiveAttributeBAIS(String RepetitiveProcessId,String isValidatingMandate, HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveAttributeIBPS - BAIS");
		String AttrTagName = "";
		String TransColName = "";
		String isMandatory = "";
		if(!hmRptAttrAndColBAIS.containsKey(RepetitiveProcessId))
		{
			String sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME,TRANSACTIONTABLECOLNAME,ISMANDATORY from USR_0_WSR_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"+RepetitiveProcessId+"' and ISACTIVE='Y' order by AttributeName");
			WriteLog("Input XML: "+sInputXML);
			String sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			AttrTagName = getTagValues(sOutputXML,"ATTRIBUTENAME");
			TransColName = getTagValues(sOutputXML,"TRANSACTIONTABLECOLNAME");
			isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		} 
		else
		{
			HashMap<String, String> hmAttr = new HashMap();
			hmAttr = hmRptAttrAndColBAIS.get(RepetitiveProcessId);
			for (String name: hmAttr.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
				
				if(TransColName.equalsIgnoreCase(""))
					TransColName = hmAttr.get(name).toString(); 
				else
					TransColName = TransColName+"`"+hmAttr.get(name).toString();
			}
			
			HashMap<String, String> hmMand = new HashMap();
			hmMand = hmRptAttrAndMandBAIS.get(RepetitiveProcessId);
			for (String name1: hmMand.keySet()){
				if(isMandatory.equalsIgnoreCase(""))
					isMandatory = hmMand.get(name1).toString(); 
				else
					isMandatory = isMandatory+"`"+hmMand.get(name1).toString();
			}
		}
		
		if(isValidatingMandate.equalsIgnoreCase("Y"))
		{	
			String AttrTagsName []=AttrTagName.split("`");
			String TransColsName []=TransColName.split("`");
			String MandateList []=isMandatory.split("`");
			if(MandateList.length>0)
			{
				for(int i=0;i<MandateList.length;i++)
				{
					String flag="Y";
					
					if("Y".equalsIgnoreCase(MandateList[i]))
					{
						if(RepetitiveReqAttr.containsKey(AttrTagsName[i]))
						{	
						    if(RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
						    	flag = "N";
						    else {
						    	// nothing to do
						    }
						}
						else
							flag = "N";	
					}
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+AttrTagsName[i]+" for "+ RepetitiveProcessId);
				}
			}
			else
			{
				throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
			}
			
			
			if(!hmRptAttrAndColBAIS.containsKey(RepetitiveProcessId))
			{
				if(AttrTagsName.length>0)
				{
					HashMap<String, String> hmtmp = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp.put(AttrTagsName[i], TransColsName[i]);
					}
					hmRptAttrAndColBAIS.put(RepetitiveProcessId, hmtmp);
					
					HashMap<String, String> hmtmp1 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp1.put(AttrTagsName[i], MandateList[i]);
					}
					hmRptAttrAndMandBAIS.put(RepetitiveProcessId, hmtmp1);
				}	
			}
		}
		return AttrTagName;
	}
	
	private String checkMandatoryRepetitiveAttributeDCC(String RepetitiveProcessId,String isValidatingMandate, HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveAttributeIBPS - DCC");
		String AttrTagName = "";
		String TransColName = "";
		String isMandatory = "";
		if(!hmRptAttrAndColDCC.containsKey(RepetitiveProcessId))
		{
			String sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME,TRANSACTIONTABLECOLNAME,ISMANDATORY from USR_0_WSR_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"+RepetitiveProcessId+"' and ISACTIVE='Y' order by AttributeName");
			WriteLog("Input XML: "+sInputXML);
			String sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			AttrTagName = getTagValues(sOutputXML,"ATTRIBUTENAME");
			TransColName = getTagValues(sOutputXML,"TRANSACTIONTABLECOLNAME");
			isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		} 
		else
		{
			HashMap<String, String> hmAttr = new HashMap();
			hmAttr = hmRptAttrAndColDCC.get(RepetitiveProcessId);
			for (String name: hmAttr.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
				
				if(TransColName.equalsIgnoreCase(""))
					TransColName = hmAttr.get(name).toString(); 
				else
					TransColName = TransColName+"`"+hmAttr.get(name).toString();
			}
			
			HashMap<String, String> hmMand = new HashMap();
			hmMand = hmRptAttrAndMandDCC.get(RepetitiveProcessId);
			for (String name1: hmMand.keySet()){
				if(isMandatory.equalsIgnoreCase(""))
					isMandatory = hmMand.get(name1).toString(); 
				else
					isMandatory = isMandatory+"`"+hmMand.get(name1).toString();
			}
		}
		
		if(isValidatingMandate.equalsIgnoreCase("Y"))
		{	
			String AttrTagsName []=AttrTagName.split("`");
			String TransColsName []=TransColName.split("`");
			String MandateList []=isMandatory.split("`");
			if(MandateList.length>0)
			{
				for(int i=0;i<MandateList.length;i++)
				{
					String flag="Y";
					
					if("Y".equalsIgnoreCase(MandateList[i]))
					{
						if(RepetitiveReqAttr.containsKey(AttrTagsName[i]))
						{	
						    if(RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
						    	flag = "N";
						    else {
						    	// nothing to do
						    }
						}
						else
							flag = "N";	
					}
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+AttrTagsName[i]+" for "+ RepetitiveProcessId);
				}
			}
			else
			{
				throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
			}
			
			
			if(!hmRptAttrAndColDCC.containsKey(RepetitiveProcessId))
			{
				if(AttrTagsName.length>0)
				{
					HashMap<String, String> hmtmp = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp.put(AttrTagsName[i], TransColsName[i]);
					}
					hmRptAttrAndColDCC.put(RepetitiveProcessId, hmtmp);
					
					HashMap<String, String> hmtmp1 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp1.put(AttrTagsName[i], MandateList[i]);
					}
					hmRptAttrAndMandDCC.put(RepetitiveProcessId, hmtmp1);
				}	
			}
			
			if("DIGITAL_CC_GR_SUPP_CARD_DETAILS".equalsIgnoreCase(RepetitiveProcessId.trim()))
			{
				if(!RepetitiveReqAttr.containsKey("Card_For_Relative") || "".equals(RepetitiveReqAttr.get("Card_For_Relative"))) {	
					throw new WICreateException("10008 ",pCodes.getString("10008")+": "+" Supp_Card_Details"+" for "+ RepetitiveProcessId);
				} else {
					if(RepetitiveReqAttr.get("Card_For_Relative").startsWith("Y") && (!RepetitiveReqAttr.containsKey("Relationship_type") || "".equals(RepetitiveReqAttr.get("Relationship_type")))) {
						throw new WICreateException("10009",pCodes.getString("10009")+": "+"Supp_Card_Details"+" for "+ RepetitiveProcessId);
					}
				}
				
				if(RepetitiveReqAttr.get("UAE_Residency").startsWith("Y")) {
					if(!RepetitiveReqAttr.containsKey("EID") || "".equals(RepetitiveReqAttr.get("EID"))) {
						throw new WICreateException("10010",pCodes.getString("10010")+": "+"Supp_Card_Details"+" for "+ RepetitiveProcessId);
					}
					if(!RepetitiveReqAttr.containsKey("EID_Expiry") || "".equals(RepetitiveReqAttr.get("EID_Expiry"))) {
						throw new WICreateException("10011",pCodes.getString("10011")+": "+"Supp_Card_Details"+" for "+ RepetitiveProcessId);
					}
					if(!RepetitiveReqAttr.containsKey("Visa_Number") || "".equals(RepetitiveReqAttr.get("Visa_Number"))) {
						throw new WICreateException("10012",pCodes.getString("10012")+": "+"Supp_Card_Details"+" for "+ RepetitiveProcessId);
					}
					if(!RepetitiveReqAttr.containsKey("Visa_Expiry") || "".equals(RepetitiveReqAttr.get("Visa_Expiry"))) {
						throw new WICreateException("10013",pCodes.getString("10013")+": "+"Supp_Card_Details"+" for "+ RepetitiveProcessId);
					}
				}
			}
		}
		return AttrTagName;
	}
	
	//Reddy 7.6.22 BSR Process
	private String checkMandatoryRepetitiveAttributeBSR(String RepetitiveProcessId,String isValidatingMandate, HashMap<String, String> RepetitiveReqAttr) throws WICreateException, Exception
	{
		WriteLog("inside checkMandatoryRepetitiveAttributeIBPS - BSR");
		String AttrTagName = "";
		String TransColName = "";
		String isMandatory = "";
		if(!hmRptAttrAndColBSR.containsKey(RepetitiveProcessId))
		{
			String sInputXML=getAPSelectWithColumnNamesXML("select ATTRIBUTENAME,TRANSACTIONTABLECOLNAME,ISMANDATORY from USR_0_WSR_ATTRDETAILS_REPETITIVE with(nolock) where PROCESSID='"+RepetitiveProcessId+"' and ISACTIVE='Y' order by AttributeName");
			WriteLog("Input XML: "+sInputXML);
			String sOutputXML=executeAPI(sInputXML);
			WriteLog("Output XML: "+sOutputXML);
			xmlobj=new XMLParser(sOutputXML);
			checkCallsMainCode(xmlobj);
			AttrTagName = getTagValues(sOutputXML,"ATTRIBUTENAME");
			TransColName = getTagValues(sOutputXML,"TRANSACTIONTABLECOLNAME");
			isMandatory = getTagValues(sOutputXML,"ISMANDATORY");
		} 
		else
		{
			HashMap<String, String> hmAttr = new HashMap();
			hmAttr = hmRptAttrAndColBSR.get(RepetitiveProcessId);
			for (String name: hmAttr.keySet()){
				if(AttrTagName.equalsIgnoreCase(""))
					AttrTagName = name.toString();
				else 
					AttrTagName = AttrTagName+"`"+name.toString();
				
				if(TransColName.equalsIgnoreCase(""))
					TransColName = hmAttr.get(name).toString(); 
				else
					TransColName = TransColName+"`"+hmAttr.get(name).toString();
			}
			
			HashMap<String, String> hmMand = new HashMap();
			hmMand = hmRptAttrAndMandBSR.get(RepetitiveProcessId);
			for (String name1: hmMand.keySet()){
				if(isMandatory.equalsIgnoreCase(""))
					isMandatory = hmMand.get(name1).toString(); 
				else
					isMandatory = isMandatory+"`"+hmMand.get(name1).toString();
			}
		}
		
		if(isValidatingMandate.equalsIgnoreCase("Y"))
		{	
			String AttrTagsName []=AttrTagName.split("`");
			String TransColsName []=TransColName.split("`");
			String MandateList []=isMandatory.split("`");
			if(MandateList.length>0)
			{
				for(int i=0;i<MandateList.length;i++)
				{
					String flag="Y";
					
					if("Y".equalsIgnoreCase(MandateList[i]))
					{
						if(RepetitiveReqAttr.containsKey(AttrTagsName[i]))
						{	
						    if(RepetitiveReqAttr.get(AttrTagsName[i]).trim().equalsIgnoreCase(""))
						    	flag = "N";
						    else {
						    	// nothing to do
						    }
						}
						else
							flag = "N";	
					}
				    if(flag.equalsIgnoreCase("N"))
						throw new WICreateException("1020",pCodes.getString("1020")+": "+AttrTagsName[i]+" for "+ RepetitiveProcessId);
				}
			}
			else
			{
				throw new WICreateException("1021",pCodes.getString("1021")+" for process id: "+processID);
			}
			
			
			if(!hmRptAttrAndColBSR.containsKey(RepetitiveProcessId))
			{
				if(AttrTagsName.length>0)
				{
					HashMap<String, String> hmtmp = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp.put(AttrTagsName[i], TransColsName[i]);
					}
					hmRptAttrAndColBSR.put(RepetitiveProcessId, hmtmp);
					
					HashMap<String, String> hmtmp1 = new HashMap();
					for(int i=0;i<AttrTagsName.length;i++)
					{
						hmtmp1.put(AttrTagsName[i], MandateList[i]);
					}
					hmRptAttrAndMandBSR.put(RepetitiveProcessId, hmtmp1);
				}	
			}
		}
		return AttrTagName;
	}
	
	
	
	
	private String getRepetitiveProcessIdOrTransactionTableName(String RepetitiveTagName, String ColName) throws WICreateException, Exception
	{
		String sInputXML=getAPSelectWithColumnNamesXML("select "+ColName+" from USR_0_WSR_PROCESS_REPETITIVE with(nolock) where ProcessName='"+sProcessName+"' and SUBPROCESSNAME='"+sSubProcess+"' and REPETITIVETAGNAME = '"+RepetitiveTagName+"' and isactive='Y'");
		WriteLog("APSelectWithColumnNames Input: "+sInputXML);
		String sOutputXML=executeAPI(sInputXML);
		WriteLog("APSelectWithColumnNames Output: "+sOutputXML);
    	xmlobj=new XMLParser(sOutputXML);
    	//Check Main Code
		checkCallsMainCode(xmlobj); 
		
		if(ColName.equalsIgnoreCase("PROCESSID"))
		{
			String RepetitiveProcessID=getTagValues(sOutputXML, "PROCESSID");
			WriteLog("RepetitiveProcessID: "+RepetitiveProcessID);
			if(RepetitiveProcessID.equalsIgnoreCase(""))
				throw new WICreateException("1025",pCodes.getString("1025")+":"+sProcessName+"/"+sSubProcess+"/"+RepetitiveTagName);
		
			return RepetitiveProcessID;
		}
		
		if(ColName.equalsIgnoreCase("TRANSACTIONTABLE"))
		{
			String RepetitiveTagTableName=getTagValues(sOutputXML, "TRANSACTIONTABLE");
			WriteLog("RepetitiveTagTableName: "+RepetitiveTagTableName);
			if(RepetitiveTagTableName.equalsIgnoreCase(""))
				throw new WICreateException("1024",pCodes.getString("1024")+":"+sProcessName+"/"+sSubProcess+"/"+RepetitiveTagName);
			
			return RepetitiveTagTableName;
		}
		
		return "";
	}
	
	private void InsertRecordsForRepetitiveTags() throws WICreateException, Exception
	{
		if(hmMain.size()!= 0)
		{
			WriteLog("Inside InsertRecordsForRepetitiveTags");
			for (String name: hmMain.keySet()){
	            String key = name.toString();
	            String keyvalue = hmMain.get(name).toString();  
	            WriteLog("hmMain: "+key + " " + keyvalue);  
	            String RepetitiveTags [] = key.split("-");
				String RepetitiveTagTableName=getRepetitiveProcessIdOrTransactionTableName(RepetitiveTags[0],"TRANSACTIONTABLE");
				String RepetitiveProcessID = getRepetitiveProcessIdOrTransactionTableName(RepetitiveTags[0],"PROCESSID");
				HashMap<String, String> hm1 = new HashMap(); 
				hm1 = hmMain.get(key);
				WriteLog(key+" hm1 size: "+hm1.size());
				String AttrList = checkMandatoryRepetitiveAttribute(RepetitiveProcessID,"N","ATTRIBUTENAME",hm1);
				String TransColList = checkMandatoryRepetitiveAttribute(RepetitiveProcessID,"N","TRANSACTIONTABLECOLNAME",hm1);
				if(!"".equalsIgnoreCase(AttrList.trim()))
				{
					String AttrListArr [] = AttrList.split("`");
					String TransColListArr [] = TransColList.split("`");
					String insertColumns = "";
					String insertValues = "";
					for(int j=0;j<AttrListArr.length;j++)
					{
						if(hm1.containsKey(AttrListArr[j]))
						{
							String value = hm1.get(AttrListArr[j]);
							if(!value.trim().equalsIgnoreCase("")) 
							{
								value = value.replace("'", "");
							}
							if(!value.trim().equalsIgnoreCase("")) 
							{
								if("".equalsIgnoreCase(insertColumns))
									insertColumns = TransColListArr[j];
								else 
									insertColumns = insertColumns+","+TransColListArr[j];
								
								if("".equalsIgnoreCase(insertValues))
									insertValues = "'"+value+"'";
								else 
									insertValues = insertValues+","+"'"+value+"'";
							}
						}
					}
					insertIntoRepetitiveGridTable(RepetitiveTagTableName, insertColumns, insertValues);
				}
				
			}
				
				
		}
	}
	
	private void InsertRecordsForRepetitiveTagsIBPS() throws WICreateException, Exception
	{
		if(hmMain.size()!= 0)
		{
			WriteLog("Inside InsertRecordsForRepetitiveTagsIBPS");
			for (String name: hmMain.keySet()){
	            String key = name.toString();
	            String keyvalue = hmMain.get(name).toString();  
	            //WriteLog("hmMain: "+key + " " + keyvalue);  
	            String RepetitiveTags [] = key.split("-");
	            String RepetitiveProcessID = hmRptProcessIdIRBL.get(RepetitiveTags[0]);
				String RepetitiveTagTableName=hmRptTransTableIRBL.get(RepetitiveProcessID);
				//WriteLog("RepetitiveProcessID:"+RepetitiveProcessID);
				//WriteLog("RepetitiveTagTableName:"+RepetitiveTagTableName);
				HashMap<String, String> hm1 = new HashMap(); 
				hm1 = hmMain.get(key);
				//WriteLog(key+" hm1 size: "+hm1.size());
				
				HashMap<String, String> hmtmp = new HashMap();
				hmtmp = hmRptAttrAndColIRBL.get(RepetitiveProcessID);
				String AttrList = "";
				String TransColList = "";
				//WriteLog("hmRptAttrAndCol size:"+hmRptAttrAndCol.size());
				//WriteLog("hmtmp size:"+hmtmp.size());
				for (String name1: hmtmp.keySet()){
					if(AttrList.equalsIgnoreCase(""))
						AttrList = name1.toString();
					else 
						AttrList = AttrList+"`"+name1.toString();
					
					if(TransColList.equalsIgnoreCase(""))
						TransColList = hmtmp.get(name1).toString(); 
					else
						TransColList = TransColList+"`"+hmtmp.get(name1).toString();
				}
				//WriteLog("AttrList:"+AttrList);
				//WriteLog("TransColList:"+TransColList);
				if(!"".equalsIgnoreCase(AttrList.trim()))
				{
					String AttrListArr [] = AttrList.split("`");
					String TransColListArr [] = TransColList.split("`");
					String insertColumns = "";
					String insertValues = "";
					for(int j=0;j<AttrListArr.length;j++)
					{
						if(hm1.containsKey(AttrListArr[j]))
						{
							String value = hm1.get(AttrListArr[j]);
							if(!value.trim().equalsIgnoreCase("")) 
							{
								value = value.replace("'", "");
							}
							if(!value.trim().equalsIgnoreCase("")) 
							{
								if("".equalsIgnoreCase(insertColumns))
									insertColumns = TransColListArr[j];
								else 
									insertColumns = insertColumns+","+TransColListArr[j];
								
								if("".equalsIgnoreCase(insertValues))
									insertValues = "'"+value+"'";
								else 
									insertValues = insertValues+","+"'"+value+"'";
							}
						}
					}
					insertIntoRepetitiveGridTable(RepetitiveTagTableName, insertColumns, insertValues);
				}
				
			}	
				
		}
	}
	
	
	private void InsertRecordsForRepetitiveTagsDAO() throws WICreateException, Exception
	{
		if(hmMainDAO.size()!= 0)
		{
			WriteLog("Inside InsertRecordsForRepetitiveTagsIBPS");
			for (String name: hmMainDAO.keySet()){
	            String key = name.toString();
	            String keyvalue = hmMainDAO.get(name).toString();  
	            //WriteLog("hmMain: "+key + " " + keyvalue);  
	            String RepetitiveTags [] = key.split("-");
	            String RepetitiveProcessID = hmRptProcessIdDAO.get(RepetitiveTags[0]);
				String RepetitiveTagTableName=hmRptTransTableDAO.get(RepetitiveProcessID);
				//WriteLog("RepetitiveProcessID:"+RepetitiveProcessID);
				//WriteLog("RepetitiveTagTableName:"+RepetitiveTagTableName);
				HashMap<String, String> hm1 = new HashMap(); 
				hm1 = hmMainDAO.get(key);
				//WriteLog(key+" hm1 size: "+hm1.size());
				
				HashMap<String, String> hmtmp = new HashMap();
				hmtmp = hmRptAttrAndColDAO.get(RepetitiveProcessID);
				String AttrList = "";
				String TransColList = "";
				//WriteLog("hmRptAttrAndCol size:"+hmRptAttrAndCol.size());
				//WriteLog("hmtmp size:"+hmtmp.size());
				if(key.toUpperCase().contains("UIDDetials".toUpperCase())){
					insertCustomerFircoData(keyvalue);
					//sProcessName
					//wiName
				}
				else{
					for (String name1: hmtmp.keySet()){
						if(AttrList.equalsIgnoreCase(""))
							AttrList = name1.toString();
						else 
							AttrList = AttrList+"`"+name1.toString();
						
						if(TransColList.equalsIgnoreCase(""))
							TransColList = hmtmp.get(name1).toString(); 
						else
							TransColList = TransColList+"`"+hmtmp.get(name1).toString();
					}
					//WriteLog("AttrList:"+AttrList);
					//WriteLog("TransColList:"+TransColList);
					if(!"".equalsIgnoreCase(AttrList.trim()))
					{
						String AttrListArr [] = AttrList.split("`");
						String TransColListArr [] = TransColList.split("`");
						String insertColumns = "";
						String insertValues = "";
						for(int j=0;j<AttrListArr.length;j++)
						{
							if(hm1.containsKey(AttrListArr[j]))
							{
								String value = hm1.get(AttrListArr[j]);
								if(!value.trim().equalsIgnoreCase("")) 
								{
									value = value.replace("'", "");
								}
								if(!value.trim().equalsIgnoreCase("")) 
								{
									if("".equalsIgnoreCase(insertColumns))
										insertColumns = TransColListArr[j];
									else 
										insertColumns = insertColumns+","+TransColListArr[j];
									
									if("".equalsIgnoreCase(insertValues))
										insertValues = "'"+value+"'";
									else 
										insertValues = insertValues+","+"'"+value+"'";
								}
							}
						}
						insertIntoRepetitiveGridTable(RepetitiveTagTableName, insertColumns, insertValues);
					}
					
				}
				
			}
		}
	}
	
	
	private void InsertRecordsForRepetitiveTagsBAIS() throws WICreateException, Exception
	{
		if(hmMainBAIS.size()!= 0)
		{
			WriteLog("Inside InsertRecordsForRepetitiveTagsIBPS");
			for (String name: hmMainBAIS.keySet()){
	            String key = name.toString();
	            String keyvalue = hmMainBAIS.get(name).toString();  
	            //WriteLog("hmMain: "+key + " " + keyvalue);  
	            String RepetitiveTags [] = key.split("-");
	            String RepetitiveProcessID = hmRptProcessIdBAIS.get(RepetitiveTags[0]);
				String RepetitiveTagTableName=hmRptTransTableBAIS.get(RepetitiveProcessID);
				//WriteLog("RepetitiveProcessID:"+RepetitiveProcessID);
				//WriteLog("RepetitiveTagTableName:"+RepetitiveTagTableName);
				HashMap<String, String> hm1 = new HashMap(); 
				hm1 = hmMainBAIS.get(key);
				//WriteLog(key+" hm1 size: "+hm1.size());
				
				HashMap<String, String> hmtmp = new HashMap();
				hmtmp = hmRptAttrAndColBAIS.get(RepetitiveProcessID);
				String AttrList = "";
				String TransColList = "";
				//WriteLog("hmRptAttrAndCol size:"+hmRptAttrAndCol.size());
				//WriteLog("hmtmp size:"+hmtmp.size());
				for (String name1: hmtmp.keySet()){
					if(AttrList.equalsIgnoreCase(""))
						AttrList = name1.toString();
					else 
						AttrList = AttrList+"`"+name1.toString();
					
					if(TransColList.equalsIgnoreCase(""))
						TransColList = hmtmp.get(name1).toString(); 
					else
						TransColList = TransColList+"`"+hmtmp.get(name1).toString();
				}
				//WriteLog("AttrList:"+AttrList);
				//WriteLog("TransColList:"+TransColList);
				if(!"".equalsIgnoreCase(AttrList.trim()))
				{
					String AttrListArr [] = AttrList.split("`");
					String TransColListArr [] = TransColList.split("`");
					String insertColumns = "";
					String insertValues = "";
					for(int j=0;j<AttrListArr.length;j++)
					{
						if(hm1.containsKey(AttrListArr[j]))
						{
							String value = hm1.get(AttrListArr[j]);
							if(!value.trim().equalsIgnoreCase("")) 
							{
								value = value.replace("'", "");
							}
							if(!value.trim().equalsIgnoreCase("")) 
							{
								if("".equalsIgnoreCase(insertColumns))
									insertColumns = TransColListArr[j];
								else 
									insertColumns = insertColumns+","+TransColListArr[j];
								
								if("".equalsIgnoreCase(insertValues))
									insertValues = "'"+value+"'";
								else 
									insertValues = insertValues+","+"'"+value+"'";
							}
						}
					}
					insertIntoRepetitiveGridTable(RepetitiveTagTableName, insertColumns, insertValues);
				}
				
			}	
				
		}
	}
	
	private void InsertRecordsForRepetitiveTagsDCC() throws WICreateException, Exception
	{
		if(hmMainDCC.size()!= 0)
		{
			WriteLog("Inside InsertRecordsForRepetitiveTagsDCC");
			for (String name: hmMainDCC.keySet()){
	            String key = name.toString();
	            String keyvalue = hmMainDCC.get(name).toString();  
	            //WriteLog("hmMain: "+key + " " + keyvalue);  
	            String RepetitiveTags [] = key.split("-");
	            String RepetitiveProcessID = hmRptProcessIdDCC.get(RepetitiveTags[0]);
				String RepetitiveTagTableName=hmRptTransTableDCC.get(RepetitiveProcessID);
				//WriteLog("RepetitiveProcessID:"+RepetitiveProcessID);
				//WriteLog("RepetitiveTagTableName:"+RepetitiveTagTableName);
				HashMap<String, String> hm1 = new HashMap(); 
				hm1 = hmMainDCC.get(key);
				//WriteLog(key+" hm1 size: "+hm1.size());
				
				HashMap<String, String> hmtmp = new HashMap();
				hmtmp = hmRptAttrAndColDCC.get(RepetitiveProcessID);
				String AttrList = "";
				String TransColList = "";
				//WriteLog("hmRptAttrAndCol size:"+hmRptAttrAndCol.size());
				//WriteLog("hmtmp size:"+hmtmp.size());
				if(key.toUpperCase().contains("UIDDetials".toUpperCase())){
					insertCustomerFircoDataDCC(keyvalue);
				}
				else{
					for (String name1: hmtmp.keySet()){
						if(AttrList.equalsIgnoreCase(""))
							AttrList = name1.toString();
						else 
							AttrList = AttrList+"`"+name1.toString();
						
						if(TransColList.equalsIgnoreCase(""))
							TransColList = hmtmp.get(name1).toString(); 
						else
							TransColList = TransColList+"`"+hmtmp.get(name1).toString();
					}
					//WriteLog("AttrList:"+AttrList);
					//WriteLog("TransColList:"+TransColList);
					if(!"".equalsIgnoreCase(AttrList.trim()))
					{
						String AttrListArr [] = AttrList.split("`");
						String TransColListArr [] = TransColList.split("`");
						String insertColumns = "";
						String insertValues = "";
						for(int j=0;j<AttrListArr.length;j++)
						{
							if(hm1.containsKey(AttrListArr[j]))
							{
								String value = hm1.get(AttrListArr[j]);
								if(!value.trim().equalsIgnoreCase("")) 
								{
									value = value.replace("'", "");
								}
								if(!value.trim().equalsIgnoreCase("")) 
								{
									if("".equalsIgnoreCase(insertColumns))
										insertColumns = TransColListArr[j];
									else 
										insertColumns = insertColumns+","+TransColListArr[j];
									
									if("".equalsIgnoreCase(insertValues))
										insertValues = "'"+value+"'";
									else 
										insertValues = insertValues+","+"'"+value+"'";
								}
							}
						}
						insertIntoRepetitiveGridTable(RepetitiveTagTableName, insertColumns, insertValues);
					}
					
				}
				
			}
				
				
		}
	}
	
		//Reddy 7.6.22 BSR Process
	private void InsertRecordsForRepetitiveTagsBSR() throws WICreateException, Exception
	{
		if(hmMainBSR.size()!= 0)
		{
			WriteLog("Inside InsertRecordsForRepetitiveTagsIBPS");
			for (String name: hmMainBSR.keySet()){
	            String key = name.toString();
	            String keyvalue = hmMainBSR.get(name).toString();  
	            //WriteLog("hmMain: "+key + " " + keyvalue);  
	            String RepetitiveTags [] = key.split("-");
	            String RepetitiveProcessID = hmRptProcessIdBSR.get(RepetitiveTags[0]);
				String RepetitiveTagTableName=hmRptTransTableBSR.get(RepetitiveProcessID);
				//WriteLog("RepetitiveProcessID:"+RepetitiveProcessID);
				//WriteLog("RepetitiveTagTableName:"+RepetitiveTagTableName);
				HashMap<String, String> hm1 = new HashMap(); 
				hm1 = hmMainBSR.get(key);
				//WriteLog(key+" hm1 size: "+hm1.size());
				
				HashMap<String, String> hmtmp = new HashMap();
				hmtmp = hmRptAttrAndColBSR.get(RepetitiveProcessID);
				String AttrList = "";
				String TransColList = "";
				//WriteLog("hmRptAttrAndCol size:"+hmRptAttrAndCol.size());
				//WriteLog("hmtmp size:"+hmtmp.size());
				if(key.toUpperCase().contains("UIDDetials".toUpperCase())){
					
				}
				else{
					for (String name1: hmtmp.keySet()){
						if(AttrList.equalsIgnoreCase(""))
							AttrList = name1.toString();
						else 
							AttrList = AttrList+"`"+name1.toString();
						
						if(TransColList.equalsIgnoreCase(""))
							TransColList = hmtmp.get(name1).toString(); 
						else
							TransColList = TransColList+"`"+hmtmp.get(name1).toString();
					}
					//WriteLog("AttrList:"+AttrList);
					//WriteLog("TransColList:"+TransColList);
					if(!"".equalsIgnoreCase(AttrList.trim()))
					{
						String AttrListArr [] = AttrList.split("`");
						String TransColListArr [] = TransColList.split("`");
						String insertColumns = "";
						String insertValues = "";
						for(int j=0;j<AttrListArr.length;j++)
						{
							if(hm1.containsKey(AttrListArr[j]))
							{
								String value = hm1.get(AttrListArr[j]);
								if(!value.trim().equalsIgnoreCase("")) 
								{
									value = value.replace("'", "");
								}
								if(!value.trim().equalsIgnoreCase("")) 
								{
									if("".equalsIgnoreCase(insertColumns))
										insertColumns = TransColListArr[j];
									else 
										insertColumns = insertColumns+","+TransColListArr[j];
									
									if("".equalsIgnoreCase(insertValues))
										insertValues = "'"+value+"'";
									else 
										insertValues = insertValues+","+"'"+value+"'";
								}
							}
						}
						insertIntoRepetitiveGridTable(RepetitiveTagTableName, insertColumns, insertValues);
					}
					
				}
				
			}
				
				
		}
	}
	
	private void insertIntoRepetitiveGridTable(String trTableName, String trTableColumn, String trTableValue) throws WICreateException, Exception {

		if ("OECD".equalsIgnoreCase(sProcessName))
			trTableColumn = "WINAME,"+trTableColumn;
		if ("iRBL".equalsIgnoreCase(sProcessName))
			trTableColumn = "WI_NAME,"+trTableColumn;
		if ("DigitalAO".equalsIgnoreCase(sProcessName))
			trTableColumn = "WI_NAME,"+trTableColumn;  //hritik 5.4.22  
		if ("Digital_CC".equalsIgnoreCase(sProcessName))
			trTableColumn = "WI_NAME,"+trTableColumn;
		if ("BSR".equalsIgnoreCase(sProcessName))
			trTableColumn = "WI_NAME,"+trTableColumn; //Reddy 7.6.22 BSR Process
		if ("BAIS".equalsIgnoreCase(sProcessName))
			trTableColumn = "WINAME,"+trTableColumn; //SOUMYA 
		
		WriteLog("trTableColumn final: " + trTableColumn);
		trTableValue = "'"+ wiName + "'," + trTableValue;
		WriteLog("trTableValue final: " + trTableValue);
		String sInputXML = getDBInsertInputRepetitiveGridTable(trTableName, trTableColumn, trTableValue);
		WriteLog("APInsert Input RepetitiveGridTable: " + sInputXML);
		String sOutputXML = executeAPI(sInputXML);
		WriteLog("APInsert Output RepetitiveGridTable: " + sOutputXML);
		xmlobj = new XMLParser(sOutputXML);
		// Check Main Code
		checkCallsMainCode(xmlobj);
	}
	
	private String getDBInsertInputRepetitiveGridTable(String trTableName, String trTableColumn, String trTableValue) {
		return "<?xml version=\"1.0\"?>" + "<APInsert_Input>"
				+ "<Option>APInsert</Option>" + "<TableName>" + trTableName
				+ "</TableName>" + "<ColName>" + trTableColumn + "</ColName>"
				+ "<Values>" + trTableValue + "</Values>" + "<EngineName>"
				+ sCabinetName + "</EngineName>" + "<SessionId>" + sSessionID
				+ "</SessionId>" + "</APInsert_Input>";
	}
	// End - adding methods for repetitive tags added on 26/10/2020
	
	
	//Added by Sajan For FALCON Digital onboarding
		
	
	private void validateConditionalRequestParametersForDOB() throws WICreateException, Exception
	{
		WriteLog("Inside Digital On boarding Consitional mandatory");
		//Changes for emp validation removal 
		/*if(!hm.containsKey("EmpCode") || "".equals(hm.get("EmpCode"))){
			String empCode=checkEmployerCode(hm.get("EmpName"));
			if(!"".equals(empCode) && !"`".equalsIgnoreCase(empCode)){
				throw new WICreateException("6001",pCodes.getString("6001")+" for process id: "+processID);
			}
		}*/
		if(!hm.containsKey("IndusSeg") || "".equals(hm.get("IndusSeg"))){
			String strIndusMicro=checkIndustrySegment(hm.get("EmpName"));
			if(!"".equals(strIndusMicro)){
				throw new WICreateException("6002",pCodes.getString("6002")+" for process id: "+processID);
			}
		}
		if(!"AE".equals(hm.get("Nationality"))){
			if(!hm.containsKey("HomeCountryHouseNo") || "".equals(hm.get("HomeCountryHouseNo")) 
				|| !hm.containsKey("HomeCountryBuildingName") || "".equals(hm.get("HomeCountryBuildingName"))
				|| !hm.containsKey("HomeCountryStreetName") || "".equals(hm.get("HomeCountryStreetName"))
				|| !hm.containsKey("HomeCountryNo") || "".equals(hm.get("HomeCountryNo"))){
				throw new WICreateException("6003",pCodes.getString("6003")+" for process id: "+processID);
			}
		}
		if("True".equalsIgnoreCase(hm.get("isTinAvailable"))){
			if(!hm.containsKey("TINNumber") || "".equals(hm.get("TINNumber"))){
				throw new WICreateException("6004",pCodes.getString("6004")+" for process id: "+processID);
			}
			
		}
		
		else{
			if(!hm.containsKey("TinReason") || "".equals(hm.get("TinReason"))){
				throw new WICreateException("6005",pCodes.getString("6005")+" for process id: "+processID);
			}
		}
	}
	private void validateConditionalRequestParametersForDAO() throws WICreateException, Exception
	{
		WriteLog("Inside Digital Account opening conditional mandatory");
		
		if((hm.get("employer_type").toUpperCase()).contains("SELF"))
		{
			WriteLog("Inside Digital Account opening conditional mandatory for self employed check");
		/*	if("".equals(hm.get("Monthly_Expected_Turnover_Cash"))|| !hm.containsKey("Monthly_Expected_Turnover_Cash"))
			{
				throw new WICreateException("7001",pCodes.getString("7001")+" for process id: "+processID);
			} else if("".equals(hm.get("Monthly_Expected_Turnover_Non_Cash"))|| !hm.containsKey("Monthly_Expected_Turnover_Non_Cash")){
				throw new WICreateException("7002",pCodes.getString("7002")+" for process id: "+processID);
			} */ // removed on 31.5.22 as discussed 
		}
			// hritik 24.5.22 : 11:27 PM
		if("Y".equalsIgnoreCase(hm.get("STP_NonSTP")) || "Yes".equalsIgnoreCase(hm.get("STP_NonSTP")))
		{
			if("".equals(hm.get("CIF"))|| !hm.containsKey("CIF"))
				{
					throw new WICreateException("7015",pCodes.getString("7015")+" for process id: "+processID);
				}
			else if("".equals(hm.get("Account_Number"))|| !hm.containsKey("Account_Number"))
				{
					throw new WICreateException("7016",pCodes.getString("7016")+" for process id: "+processID);
				}
		}
		
		if("Y".equalsIgnoreCase(hm.get("ChequeBk_Req")) || "Yes".equalsIgnoreCase(hm.get("ChequeBk_Req")))
		{
			if("".equals(hm.get("Name_on_ChequeBk")) || !hm.containsKey("Name_on_ChequeBk"))
			{
				throw new WICreateException("7029",pCodes.getString("7029")+" for process id: "+processID);
			}
		}
		
		// hritik 24.5.22 : 11:27 PM
		if("Y".equalsIgnoreCase(hm.get("US_Citizenship_Residency")) || "Yes".equalsIgnoreCase(hm.get("US_Citizenship_Residency")))
		{
			if(("".equals(hm.get("W8Form"))|| !hm.containsKey("W8Form")) && ("".equals(hm.get("W9Form"))|| !hm.containsKey("W9Form"))){
				throw new WICreateException("7028",pCodes.getString("7028")+" for process id: "+processID);
			}
			/*
			 if("".equals(hm.get("W8Form"))|| !hm.containsKey("W8Form"))
			{
				throw new WICreateException("7017",pCodes.getString("7017")+" for process id: "+processID);
			}
			 else  if("".equals(hm.get("W9Form"))|| !hm.containsKey("W9Form"))
				{
					throw new WICreateException("7018",pCodes.getString("7018")+" for process id: "+processID);
				}
			*/	
		}
		if("SALARIED".equalsIgnoreCase(hm.get("employer_type")))
		{
			WriteLog("Inside Digital Account opening conditional mandatory for salaries employed check");
			if("".equals(hm.get("Employer_Code"))|| !hm.containsKey("Employer_Code"))
			{
				throw new WICreateException("7003",pCodes.getString("7003")+" for process id: "+processID);
			} else if("".equals(hm.get("Company_Employer_Name"))|| !hm.containsKey("Company_Employer_Name")){
				throw new WICreateException("7004",pCodes.getString("7004")+" for process id: "+processID);
			} else if("".equals(hm.get("Designation"))|| !hm.containsKey("Designation")){
				throw new WICreateException("7005",pCodes.getString("7005")+" for process id: "+processID);
			} else if("".equals(hm.get("PO_Box_emp"))|| !hm.containsKey("PO_Box_emp")){
				throw new WICreateException("7006",pCodes.getString("7006")+" for process id: "+processID);
			} else if("".equals(hm.get("Gross_Monthly_Salary_income"))|| !hm.containsKey("Gross_Monthly_Salary_income")){
				throw new WICreateException("7007",pCodes.getString("7007")+" for process id: "+processID);
			}else if("".equals(hm.get("Occupation"))|| !hm.containsKey("Occupation")){
				throw new WICreateException("7019",pCodes.getString("7019")+" for process id: "+processID);
			}
		}
		//commented for production
		/*if("Y".equalsIgnoreCase(hm.get("Is_ntb")))
		{
			if("".equals(hm.get("Country_Of_Birth")) || !hm.containsKey("Country_Of_Birth"))
			{
				throw new WICreateException("7034",pCodes.getString("7034")+" for process id: "+processID);
			}
			if("".equals(hm.get("Mothers_Maiden_Name")) || !hm.containsKey("Mothers_Maiden_Name"))
			{
				throw new WICreateException("7035",pCodes.getString("7035")+" for process id: "+processID);
			} 
			if("".equals(hm.get("PO_Box_address")) || !hm.containsKey("PO_Box_address"))
			{
				throw new WICreateException("7036",pCodes.getString("7036")+" for process id: "+processID);
			}
		}*/
	
		
		 /*
		 investment_portfolio_including_virtual_asset - income_generated
		 real_Est_owned - rental_income
		 other_Source_of_income - Net_monthly_income
		 */
		 
		
		// CM changes on 14.6.22 - Hritik
		if("Y".equalsIgnoreCase(hm.get("investment_portfolio_including_virtual_asset")) || "Yes".equalsIgnoreCase(hm.get("investment_portfolio_including_virtual_asset")))
		{
			if("".equalsIgnoreCase(hm.get("income_generated"))|| !hm.containsKey("income_generated"))
			{
				throw new WICreateException("7021",pCodes.getString("7021")+" for process id: "+processID);
			}
		}
		if("Y".equalsIgnoreCase(hm.get("real_Est_owned"))|| "Yes".equalsIgnoreCase(hm.get("real_Est_owned")))
		{
			if("".equalsIgnoreCase(hm.get("rental_income"))|| !hm.containsKey("rental_income"))
			{
				throw new WICreateException("7023",pCodes.getString("7023")+" for process id: "+processID);
			}
		}
		if("Y".equalsIgnoreCase(hm.get("Inheritance"))|| "Yes".equalsIgnoreCase(hm.get("Inheritance")))
		{
			if("".equalsIgnoreCase(hm.get("Inheritance_income"))|| !hm.containsKey("Inheritance_income"))
			{
				throw new WICreateException("7033",pCodes.getString("7033")+" for process id: "+processID);
			}
		}
		
		if("Y".equalsIgnoreCase(hm.get("other_Source_of_income"))|| "Yes".equalsIgnoreCase(hm.get("other_Source_of_income")))
		{
			if("".equalsIgnoreCase(hm.get("Net_monthly_income"))|| !hm.containsKey("Net_monthly_income"))
			{
				throw new WICreateException("7030",pCodes.getString("7030")+" for process id: "+processID);
			}
		}
		
		if("N".equalsIgnoreCase(hm.get("Is_ntb"))|| "False".equalsIgnoreCase(hm.get("Is_ntb")) || "No".equalsIgnoreCase(hm.get("Is_ntb")))
		{
			if("".equals(hm.get("CIF"))|| !hm.containsKey("CIF"))
			{
				throw new WICreateException("7031",pCodes.getString("7031")+" for process id: "+processID);
			}
		}
		if("Y".equalsIgnoreCase(hm.get("PEP")) || "YES".equalsIgnoreCase(hm.get("PEP")) )
		{
			WriteLog("Inside Digital Account opening conditional mandatory for PEP check");
			
			if("".equals(hm.get("PEP_Category"))|| !hm.containsKey("PEP_Category"))
			{
				throw new WICreateException("7010",pCodes.getString("7010")+" for process id: "+processID);
			}
			// hritik 4.7.22 - pep cat changes 
			if("Self".equalsIgnoreCase(hm.get("PEP_Category")))
			{
				if("".equals(hm.get("Previous_Designation")) || !hm.containsKey("Previous_Designation"))
				{
					throw new WICreateException("7008",pCodes.getString("7008")+" for process id: "+processID);
				}
			}
			else if("Association".equalsIgnoreCase(hm.get("PEP_Category")))
			{
				if("".equals(hm.get("Relation_Detail_w_PEP"))|| !hm.containsKey("Relation_Detail_w_PEP"))
				{
					throw new WICreateException("7009",pCodes.getString("7009")+" for process id: "+processID);
				}
				else if ("".equals(hm.get("Name_of_PEP"))|| !hm.containsKey("Name_of_PEP"))
				{
					throw new WICreateException("7011",pCodes.getString("7011")+" for process id: "+processID);
				}
				 else if("".equals(hm.get("country_pep_hold_status"))|| !hm.containsKey("country_pep_hold_status"))
				 {
					 throw new WICreateException("7012",pCodes.getString("7012")+" for process id: "+processID);
				 }
				 else if("".equals(hm.get("Emirate_pep_hold_status"))|| !hm.containsKey("Emirate_pep_hold_status")&& hm.get("country_pep_hold_status").equalsIgnoreCase("AE"))
				 {
					throw new WICreateException("7013",pCodes.getString("7013")+" for process id: "+processID);
				 }
			}
		}
	}
	
	public void check_high_risk()throws WICreateException, Exception
	{ //try catch 
		WriteLog("Inside DigitalAO high_risk");
		String high_risk="N";
		try
		{
			String score = hm.get("Risk_Score");
			Double risk_src = Double.parseDouble(score);
			if(risk_src>=4)
			{
				high_risk="Y";
			}
			else
			{
				high_risk="N";
			}
		}
		catch(Exception e){
			WriteLog("Exception: "+e.getMessage());
			throw new WICreateException("7032",pCodes.getString("7032")+" for process id: "+processID);
		}
		// how to put in WFUploadWorkItemXML?
		hm.put("risk_score_flag",high_risk);
	}
	
	private void validateConditionalRequestParametersForDCC() throws WICreateException, Exception
	{ 
		WriteLog("Inside Digital Credit Card conditional mandatory");
		
		if(!hm.containsKey("employercode") || "".equals(hm.get("employercode"))){
			String empCode=checkEmployerCode(hm.get("employercode"));
			if(!"".equals(empCode) && !"`".equalsIgnoreCase(empCode)){
				throw new WICreateException("10001",pCodes.getString("10001")+" for process id: "+processID);
			}
		}
		if (!hm.containsKey("IndusSeg") || "".equals(hm.get("IndusSeg"))) {
			String strIndusMicro = checkIndustrySegment(hm.get("employercode"));
			if (!"".equals(strIndusMicro)) {
				throw new WICreateException("10002", pCodes.getString("10002") + " for process id: " + processID);
			}
		}
		if (hm.get("Supp_Card_Required") != null && hm.get("Supp_Card_Required").startsWith("Y")) {
			if (!hm.containsKey("Self_Supp_Card_Required") || "".equals(hm.get("Self_Supp_Card_Required"))) {
				throw new WICreateException("10005", pCodes.getString("10005") + " for process id: " + processID);
			} else {
				if (hm.get("Self_Supp_Card_Required").startsWith("Y")) {
					if (!hm.containsKey("Self_Supp_Card_Embossing_Name") || "".equals(hm.get("Self_Supp_Card_Embossing_Name"))) {
						throw new WICreateException("10006", pCodes.getString("10002") + " for process id: " + processID);
					}
					if (!hm.containsKey("Self_Supp_Card_Limit") || "".equals(hm.get("Self_Supp_Card_Limit"))) {
						throw new WICreateException("10007", pCodes.getString("10002") + " for process id: " + processID);
					}
				}
			}
		}
	}
	
		//Reddy 7.6.22 BSR Process
	private void validateConditionalRequestParametersForBSR() throws WICreateException, Exception
	{ 
		WriteLog("Inside Business Service Request conditional mandatory");
		
		/*if(hm.get("employer_type").contains("SELF"))
		{
			if("".equals(hm.get("Monthly_Expected_Turnover_Cash")))
			{
				throw new WICreateException("7001",pCodes.getString("7001")+" for process id: "+processID);
			} else if("".equals(hm.get("Monthly_Expected_Turnover_Non_Cash"))){
				throw new WICreateException("7002",pCodes.getString("7002")+" for process id: "+processID);
			}
		}
		
		if("SALARIED".equalsIgnoreCase(hm.get("employer_type")))
		{
			if("".equals(hm.get("Employer_Code")))
			{
				throw new WICreateException("7003",pCodes.getString("7003")+" for process id: "+processID);
			} else if("".equals(hm.get("Company_Employer_Name"))){
				throw new WICreateException("7004",pCodes.getString("7004")+" for process id: "+processID);
			} else if("".equals(hm.get("Designation"))){
				throw new WICreateException("7005",pCodes.getString("7005")+" for process id: "+processID);
			} else if("".equals(hm.get("PO_Box_emp"))){
				throw new WICreateException("7006",pCodes.getString("7006")+" for process id: "+processID);
			} else if("".equals(hm.get("Gross_Monthly_Salary_income"))){
				throw new WICreateException("7007",pCodes.getString("7007")+" for process id: "+processID);
			}
		}
		
		if("Y".equalsIgnoreCase(hm.get("PEP")) || "YES".equalsIgnoreCase(hm.get("PEP")) )
		{
			if("".equals(hm.get("Previous_Designation")))
			{
				throw new WICreateException("7008",pCodes.getString("7008")+" for process id: "+processID);
			} else if("".equals(hm.get("Relation_Detail_w_PEP"))){
				throw new WICreateException("7009",pCodes.getString("7009")+" for process id: "+processID);
			} else if("".equals(hm.get("PEP_Category"))){
				throw new WICreateException("7010",pCodes.getString("7010")+" for process id: "+processID);
			} else if("".equals(hm.get("Name_of_PEP"))){
				throw new WICreateException("7011",pCodes.getString("7011")+" for process id: "+processID);
			} else if("".equals(hm.get("country_pep_hold_status"))){
				throw new WICreateException("7012",pCodes.getString("7012")+" for process id: "+processID);
			} else if("".equals(hm.get("Emirate_pep_hold_status"))){
				throw new WICreateException("7013",pCodes.getString("7013")+" for process id: "+processID);
			} else if("".equals(hm.get("Year_of_Incorporation"))){
				throw new WICreateException("7014",pCodes.getString("7014")+" for process id: "+processID);
			}
		}*/
	}
	private void appendConditionalRequestParametersForDOB() throws WICreateException, Exception
	{
		StringBuffer sbf=new StringBuffer(strAttributeTagForDOB);
		if(hm.containsKey("MiddleName"))
			sbf.insert(sbf.indexOf("<q_Customer>")+"<q_Customer>".length(), "<MiddleName>"+hm.get("MiddleName")+"</MiddleName>");
		WriteLog("Inside Digital On boarding Consitional mandatory Appending");
		if(hm.containsKey("EmpCode")){
			sbf.insert(sbf.indexOf("<q_EmpDetails>")+"<q_EmpDetails>".length(),"<EMpCode>"+hm.get("EmpCode")+"</EMpCode>");
		}
		if(hm.containsKey("EmpCode")){
			Map<String,String> hMapForALOC=getDetailsFromALOC(hm.get("EmpCode"));
			//			/WriteLog("test 111 "+strALOCEmpDetails);
			String InCC = hMapForALOC.get("INCLUDED_IN_CC_ALOC").equalsIgnoreCase("Y")?"true":"false";
			String inPL=hMapForALOC.get("INCLUDED_IN_PL_ALOC").equalsIgnoreCase("Y")?"true":"false";
			String newFields="<Indus_Micro>"+hm.get("IndusSeg")+"</Indus_Micro>" ;
			
			if("".equalsIgnoreCase(hMapForALOC.get("INDUSTRY_MACRO"))){
				newFields=newFields+"<Indus_Macro>"+getMacroFromMicro()+"</Indus_Macro>";
			}
			else{
				newFields=newFields+"<Indus_Macro>"+hMapForALOC.get("INDUSTRY_MACRO")+"</Indus_Macro>";
			}

			newFields=newFields+"<EmpStatusCC>"+hMapForALOC.get("COMPANY_STATUS_CC")+"</EmpStatusCC>" +
					"<EmpStatusPL>"+hMapForALOC.get("COMPANY_STATUS_PL")+"</EmpStatusPL>" +
					"<CurrEmployer>"+hMapForALOC.get("EMPLOYER_CATEGORY_PL")+"</CurrEmployer>" +
					"<EmpIndusSector>"+hMapForALOC.get("INDUSTRY_SECTOR")+"</EmpIndusSector>" +
					"<NOB>"+hMapForALOC.get("NATURE_OF_BUSINESS")+"</NOB>" +
					"<Status_PLExpact>"+hMapForALOC.get("EMPLOYER_CATEGORY_PL_EXPAT")+"</Status_PLExpact>" +
					"<Status_PLNational>"+hMapForALOC.get("EMPLOYER_CATEGORY_PL_NATIONAL")+"</Status_PLNational>" +
					"<IncInCC>"+InCC+"</IncInCC>" +
					"<IncInPL>"+inPL+"</IncInPL>" +
					"<dateinPL>"+hMapForALOC.get("DATE_OF_INCLUSION_IN_PL_ALOC")+"</dateinPL>" +
					"<dateinCC>"+hMapForALOC.get("DATE_OF_INCLUSION_IN_CC_ALOC")+"</dateinCC>" +
					"<Aloc_RemarksPL>"+hMapForALOC.get("ALOC_REMARKS_PL")+"</Aloc_RemarksPL>";

			WriteLog("New Fields test 111 "+newFields);

			sbf.insert(sbf.indexOf("<q_EmpDetails>")+"<q_EmpDetails>".length(), newFields);
		}
		else{
			String strMacro=getMacroFromMicro();
			String newFields="<Indus_Micro>"+hm.get("IndusSeg")+"</Indus_Micro>" +
			"<Indus_Macro>"+strMacro+"</Indus_Macro>" +
			"<EmpStatusCC></EmpStatusCC>" +
			"<EmpStatusPL></EmpStatusPL>" +
			"<CurrEmployer></CurrEmployer>" +
			"<EmpIndusSector></EmpIndusSector>" +
			"<NOB></NOB>" +
			"<Status_PLExpact></Status_PLExpact>" +
			"<Status_PLNational></Status_PLNational>" +
			"<IncInCC></IncInCC>" +
			"<IncInPL></IncInPL>" +
			"<dateinPL></dateinPL>" +
			"<dateinCC></dateinCC>" +
			"<Aloc_RemarksPL></Aloc_RemarksPL>";
	
			WriteLog("New Fields test 222 "+newFields);
			
			sbf.insert(sbf.indexOf("<q_EmpDetails>")+"<q_EmpDetails>".length(), newFields);
		}
		
		if(hm.containsKey("Skyward_Number")){
			sbf.insert(sbf.indexOf("<q_AltContactDet>")+"<q_AltContactDet>".length(),"<SkywardNumber>"+hm.get("Skyward_Number")+"</SkywardNumber>");
		}
		if(!"AE".equals(hm.get("Nationality"))){
			sbf.insert(sbf.indexOf("<q_AddressDetails>")+"<q_AddressDetails>".length(),"<cmplx_AddressGrid>");
			sbf.insert(sbf.indexOf("<cmplx_AddressGrid>")+"<cmplx_AddressGrid>".length(),"<AddrType>Home</AddrType>" +
					"<YearsAtCurrAddr>1</YearsAtCurrAddr>" +
					"<HouseNo>"+hm.get("HomeCountryHouseNo")+"</HouseNo>" +
					"<Addr_wi_name>"+response.getWorkitemNumber()+"</Addr_wi_name>"+
					"<BuildingName>"+hm.get("HomeCountryBuildingName")+"</BuildingName>" +
					"<CustomerType>P-"+hm.get("FirstName").toUpperCase()+" "+hm.get("LastName").toUpperCase()+"</CustomerType>" +
					"<City>"+hm.get("HomeCountryStreetName")+"</City>" +
					"<State>"+hm.get("HomeCountryStreetName")+"</State>" +
					"<POBox>-</POBox>" +
					"<StreetName>-</StreetName>" +
					"<Country>"+hm.get("Nationality")+"</Country>" +
					"<prefaddr>false</prefaddr>" +
					"<HashId>0</HashId>" +
					"</cmplx_AddressGrid>");
		}
		else{
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = sdf.format(cal.getTime());
			WriteLog("The Current Date is "+strDate);
			cal.add(Calendar.YEAR,3);
			String strEIDExpiry=sdf.format(cal.getTime());
			WriteLog("The EID expiry date for Nationals "+strEIDExpiry);
			sbf.insert(sbf.indexOf("<q_Customer>")+"<q_Customer>".length(), "<IdIssueDate>"+strDate+"</IdIssueDate><EmirateIDExpiry>"+strEIDExpiry+"</EmirateIDExpiry>");
		}
		
		if("BH".equals(hm.get("Nationality")) || "IQ".equals(hm.get("Nationality")) || "KW".equals(hm.get("Nationality")) || "OM".equals(hm.get("Nationality")) || "QA".equals(hm.get("Nationality")) || "SA".equals(hm.get("Nationality"))){
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = sdf.format(cal.getTime());
			WriteLog("The Current Date is "+strDate);
			cal.add(Calendar.YEAR,3);
			String strEIDExpiry=sdf.format(cal.getTime());
			WriteLog("The EID expiry date for GCC Nationals "+strEIDExpiry);
			sbf.insert(sbf.indexOf("<q_Customer>")+"<q_Customer>".length(), "<IdIssueDate>"+strDate+"</IdIssueDate><EmirateIDExpiry>"+strEIDExpiry+"</EmirateIDExpiry>");
		}
		
		if("True".equalsIgnoreCase(hm.get("isTinAvailable"))){
			sbf.insert(sbf.indexOf("<cmplx_GR_OecdDetails>")+"<cmplx_GR_OecdDetails>".length(),"<TinNumber>"+hm.get("TINNumber")+"</TinNumber>");
		}
		
		else{
			sbf.insert(sbf.indexOf("<cmplx_GR_OecdDetails>")+"<cmplx_GR_OecdDetails>".length(),"<TinReason>"+hm.get("TinReason")+"</TinReason>");
		}
		
		strAttributeTagForDOB=sbf.toString();
	}
	
	
	private Map<String,String> getDetailsFromALOC(String empCode)throws WICreateException,Exception{
		String sQuery="select top 1 INDUSTRY_MACRO,COMPANY_STATUS_CC,COMPANY_STATUS_PL,EMPLOYER_CATEGORY_PL,INDUSTRY_SECTOR,NATURE_OF_BUSINESS," +
				"EMPLOYER_CATEGORY_PL_EXPAT,EMPLOYER_CATEGORY_PL_NATIONAL,INCLUDED_IN_CC_ALOC,INCLUDED_IN_PL_ALOC,cast(DATE_OF_INCLUSION_IN_CC_ALOC as date) as DATE_OF_INCLUSION_IN_CC_ALOC," +
				"cast(DATE_OF_INCLUSION_IN_PL_ALOC as date) as DATE_OF_INCLUSION_IN_PL_ALOC,ALOC_REMARKS_PL from NG_RLOS_ALOC_OFFLINE_DATA with (nolock) where EMPLOYER_CODE='"+empCode+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Sajan test 222 "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		Map<String,String> hMapForAlocDate=new HashMap<String, String>();
		hMapForAlocDate.put("INDUSTRY_MACRO", getTagValues(sOutputXML, "INDUSTRY_MACRO"));
		hMapForAlocDate.put("COMPANY_STATUS_CC", getTagValues(sOutputXML, "COMPANY_STATUS_CC"));
		hMapForAlocDate.put("COMPANY_STATUS_PL", getTagValues(sOutputXML, "COMPANY_STATUS_PL"));
		hMapForAlocDate.put("EMPLOYER_CATEGORY_PL", getTagValues(sOutputXML, "EMPLOYER_CATEGORY_PL"));
		hMapForAlocDate.put("INDUSTRY_SECTOR", getTagValues(sOutputXML, "INDUSTRY_SECTOR"));
		hMapForAlocDate.put("NATURE_OF_BUSINESS", getTagValues(sOutputXML, "NATURE_OF_BUSINESS"));
		hMapForAlocDate.put("EMPLOYER_CATEGORY_PL_EXPAT", getTagValues(sOutputXML, "EMPLOYER_CATEGORY_PL_EXPAT"));
		hMapForAlocDate.put("EMPLOYER_CATEGORY_PL_NATIONAL", getTagValues(sOutputXML, "EMPLOYER_CATEGORY_PL_NATIONAL"));
		hMapForAlocDate.put("INCLUDED_IN_CC_ALOC", getTagValues(sOutputXML, "INCLUDED_IN_CC_ALOC"));
		hMapForAlocDate.put("INCLUDED_IN_PL_ALOC", getTagValues(sOutputXML, "INCLUDED_IN_PL_ALOC"));
		hMapForAlocDate.put("DATE_OF_INCLUSION_IN_CC_ALOC", getTagValues(sOutputXML, "DATE_OF_INCLUSION_IN_CC_ALOC"));
		hMapForAlocDate.put("DATE_OF_INCLUSION_IN_PL_ALOC", getTagValues(sOutputXML, "DATE_OF_INCLUSION_IN_PL_ALOC"));
		hMapForAlocDate.put("ALOC_REMARKS_PL", getTagValues(sOutputXML, "ALOC_REMARKS_PL").replace("<NEXT LINE>", ""));
		//String strIndusMacro=getTagValues(sOutputXML, "INDUSTRY_MACRO");
		//WriteLog("Indu is "+strEmployerCode);
		return hMapForAlocDate;
		
	}
	
	
	private String getMacroFromMicro() throws WICreateException,Exception{
		String sQuery="select top 1  MACRO from NG_MASTER_EmpIndusMacroAndMicro with (nolock) where MICRO='"+hm.get("IndusSeg")+"'  and IndustrySector='SERVICES'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		WriteLog("Sajan test 222 "+sOutputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String strMacro=xmlobj.getValueOf("MACRO");
		return strMacro;
	}
	private String checkEmployerCode(String employerName)throws Exception{
		//Changes for emp validation removal 
		//String sQuery="select EmployerCode from NG_RLOS_ALOC_OFFLINE_DATA where EMPR_NAME='"+employerName+"'";
		String sQuery="select top 1 EMPLOYER_CODE from ( select distinct main_Employer_code as EMPLOYER_CODE,EMPR_NAME from NG_RLOS_ALOC_OFFLINE_DATA with(nolock) where EMPR_NAME='"+employerName+"' union all select Employer_code,EMPR_NAME from NG_RLOS_ALOC_OFFLINE_DATA with(nolock) where EMPR_NAME='"+employerName+"' ) as ALOC_dump";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String strEmployerCode=getTagValues(sOutputXML, "EMPLOYER_CODE");
		WriteLog("the employer code is "+strEmployerCode);
		return strEmployerCode;
	}

	
	private String checkIndustrySegment(String employerName) throws Exception{
		
		String sQuery="select INDUSTRY_MICRO from NG_RLOS_ALOC_OFFLINE_DATA with(nolock) where EMPR_NAME='"+employerName+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		String strIndusMicro=getTagValues(sOutputXML, "INDUSTRY_MICRO");
		return strIndusMicro;
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
	
	private String getSRM_APMQPutGetMessage_BT()
	{
		
		return 	 "<?xml version=\"1.0\"?>\n"	+ "<APAPMQPutGetMessage>\n"	+ 
				"<Option>SRM_APMQPutGetMessage</Option>\n"+
				"<UserID>"+ sUsername+ "</UserID>\n"+
				"<CustId>"+ cifId+ "</CustId>\n"+ 
				"<CardCRNNumber>"+cardCRNNo+"</CardCRNNumber>\n"+
				"<CardNumber>"+unmaskedCardNo+"</CardNumber>\n"+ 
				"<TrnType>Posting</TrnType>\n"+
				"<Amount>"+btAmountReq.replaceAll(",","")+"</Amount>\n"+
				"<Currency>AED</Currency>\n"+
				"<CardExpiryDate>"+cardExpiryDate+"</CardExpiryDate>\n"+
				"<Remarks></Remarks>\n"+
				"<MerchantCode> </MerchantCode>\n"+
				"<ProcessingCode>000000</ProcessingCode>\n"+
				"<FreeField1> </FreeField1>\n"+
				"<RequestType>FUNDBLOCK</RequestType>\n"+ 
				"<SessionId>"+sSessionID+ "</SessionId>\n"+
				"<EngineName>"+sCabinetName+"</EngineName>\n"+
				"</APAPMQPutGetMessage>\n";
		
	}
	
	private String getSRM_APMQPutGetMessage_CCC()
	{
		return 	 "<?xml version=\"1.0\"?>\n"	+ "<APAPMQPutGetMessage>\n"	+ 
				"<Option>SRM_APMQPutGetMessage</Option>\n"+
				"<UserID>"+ sUsername+ "</UserID>\n"+
				"<CustId>"+ cifId+ "</CustId>\n"+ 
				"<CardCRNNumber>"+cardCRNNo+"</CardCRNNumber>\n"+
				"<CardNumber>"+unmaskedCardNo+"</CardNumber>\n"+ 
				"<TrnType>Posting</TrnType>\n"+
				"<Amount>"+cccAmount.replaceAll(",","")+"</Amount>\n"+
				"<Currency>AED</Currency>\n"+
				"<CardExpiryDate>"+cardExpiryDate+"</CardExpiryDate>\n"+
				"<Remarks></Remarks>\n"+
				"<MerchantCode> </MerchantCode>\n"+
				"<ProcessingCode>000000</ProcessingCode>\n"+
				"<FreeField1> </FreeField1>\n"+
				"<RequestType>FUNDBLOCK</RequestType>\n"+ 
				"<SessionId>"+sSessionID+ "</SessionId>\n"+
				"<EngineName>"+sCabinetName+"</EngineName>\n"+
				"</APAPMQPutGetMessage>\n";
		
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
	private String getWMDisConnectXML()
	{
		
		return "<?xml version=\"1.0\"?>"+
	    "<WMDisConnect_Input>"+
		"<Option>WMDisConnect</Option>"+
		"<EngineName>"+sCabinetName+"</EngineName>"+
		"<SessionID>"+sSessionID+"</SessionID>"+
		"</WMDisConnect_Input>";	
	
	}
	private String getWFUploadWorkItemXML()
	{
		if("TT".equalsIgnoreCase(sProcessName))
		{			
			return  "<?xml version=\"1.0\"?>"+
					"<WFUploadWorkItem_Input>"+
					"<Option>WFUploadWorkItem</Option>"+
					"<EngineName>"+sCabinetName+"</EngineName>"+
					"<SessionId>"+sSessionID+"</SessionId>"+
					"<ValidationRequired><ValidationRequired>"+
					"<ProcessDefId>"+processDefID+"</ProcessDefId>"+
					"<InitiateFromActivityId>"+sInitiateFromActivityId+"</InitiateFromActivityId>" +
					"<InitiateFromActivityName>"+sInitiateFromActivityName+"</InitiateFromActivityName>" +
					"<DataDefName></DataDefName>"+
					"<Fields></Fields>"+
					"<InitiateAlso>"+sInitiateAlso+"</InitiateAlso>"+
					"<Documents>"+documentTag+"</Documents>"+
					"<Attributes>"+attributeTag+"</Attributes>"+
					"</WFUploadWorkItem_Input>";
		}                
        else
		{		
			return  "<?xml version=\"1.0\"?>"+
					"<WFUploadWorkItem_Input>"+
					"<Option>WFUploadWorkItem</Option>"+
					"<EngineName>"+sCabinetName+"</EngineName>"+
					"<SessionId>"+sSessionID+"</SessionId>"+
					"<ValidationRequired><ValidationRequired>"+
					"<ProcessDefId>"+processDefID+"</ProcessDefId>"+
					"<DataDefName></DataDefName>"+
					"<Fields></Fields>"+
					"<InitiateAlso>"+sInitiateAlso+"</InitiateAlso>"+
					"<Documents>"+documentTag+"</Documents>"+
					"<Attributes>"+attributeTag+"</Attributes>"+
					"</WFUploadWorkItem_Input>";
		}
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
		
		// taking values which is mandatory if ApplicaionType is Joint
		if(attrName.equalsIgnoreCase("IsJointExisiting") && !(attrValues.trim().length()==0)){
			IsJointExisiting=hm.get("IsJointExisiting");
			WriteLog("Values of IsJointExisiting: "+IsJointExisiting);
		} else if(attrName.equalsIgnoreCase("JointCIFID") && !(attrValues.trim().length()==0)){
			JointCIFID=hm.get("JointCIFID");
			WriteLog("Values of JointCIFID: "+JointCIFID);
		} else if(attrName.equalsIgnoreCase("JointFirstName") && !(attrValues.trim().length()==0)){
			JointFirstName=hm.get("JointFirstName");
			WriteLog("Values of JointFirstName: "+JointFirstName);
		} else if(attrName.equalsIgnoreCase("JointLastName") && !(attrValues.trim().length()==0)){
			JointLastName=hm.get("JointLastName");
			WriteLog("Values of JointLastName: "+JointLastName);
		} else if(attrName.equalsIgnoreCase("JointMMN") && !(attrValues.trim().length()==0)){
			JointMMN=hm.get("JointMMN");
			WriteLog("Values of JointMMN: "+JointMMN);
		} else if(attrName.equalsIgnoreCase("JointNationality") && !(attrValues.trim().length()==0)){
			JointNationality=hm.get("JointNationality");
			WriteLog("Values of JointNationality: "+JointNationality);
		} else if(attrName.equalsIgnoreCase("JointDOB") && !(attrValues.trim().length()==0)){
			JointDOB=hm.get("JointDOB");
			WriteLog("Values of JointDOB: "+JointDOB);
		} else if(attrName.equalsIgnoreCase("JointMobNo") && !(attrValues.trim().length()==0)){
			JointMobNo=hm.get("JointMobNo");
			WriteLog("Values of JointMobNo: "+JointMobNo);
		} else if(attrName.equalsIgnoreCase("JointMobNoCCode") && !(attrValues.trim().length()==0)){
			JointMobNoCCode=hm.get("JointMobNoCCode");
			WriteLog("Values of JointMobNoCCode: "+JointMobNoCCode);
		} else if(attrName.equalsIgnoreCase("JointDocType") && !(attrValues.trim().length()==0)){
			JointDocType=hm.get("JointDocType");
			WriteLog("Values of JointDocType: "+JointDocType);
		} else if(attrName.equalsIgnoreCase("JointDocNumber") && !(attrValues.trim().length()==0)){
			JointDocNumber=hm.get("JointDocNumber");
			WriteLog("Values of JointDocNumber: "+JointDocNumber);
		} else if(attrName.equalsIgnoreCase("SigningAuthority") && !(attrValues.trim().length()==0)){
			SigningAuthority = hm.get("SigningAuthority");
			WriteLog("Values of SigningAuthority: "+SigningAuthority);
		} else if(attrName.equalsIgnoreCase("MailingAddress") && !(attrValues.trim().length()==0)){
			MailingAddress=hm.get("MailingAddress");
			WriteLog("Values of MailingAddress: "+MailingAddress);
		}
		// ******************************************************************************************
	
		if(attrName.equalsIgnoreCase("PrimaryEmailID") && !(attrValues.trim().length()==0))
		{
			return validateEmail(attrValues);
		}
		else if(attrName.equalsIgnoreCase("JointEmailID") && !(attrValues.trim().length()==0))
		{
			return validateEmail(attrValues);
		}
		else if((attrName.equalsIgnoreCase("PrimaryDocExpDate") && !(attrValues.trim().length()==0))
		||(attrName.equalsIgnoreCase("PrimaryEmiratesIDExpDate") && !(attrValues.trim().length()==0))
		||(attrName.equalsIgnoreCase("JointDOB") && !(attrValues.trim().length()==0))
		||(attrName.equalsIgnoreCase("JointDocExpDate") && !(attrValues.trim().length()==0))
		||(attrName.equalsIgnoreCase("JointEmiratesIDExpDate") && !(attrValues.trim().length()==0))
		||(attrName.equalsIgnoreCase("PrimaryDOB") && !(attrValues.trim().length()==0)))
		{
			return validateDateFormat(attrValues);
		}
		
		/*else if(attrName.equalsIgnoreCase("JointGender") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrName "+attrName+ " attrValues "+attrValues);
			for (JointGender_Enum s : JointGender_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("IsPrimaryExisiting") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrName "+attrName+ " attrValues "+attrValues);
			for (IsPrimaryExisiting_Enum s : IsPrimaryExisiting_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("IsDebitCardReq") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues IsDebitCardReq "+attrValues);
			for (IsDebitCardReq_Enum s : IsDebitCardReq_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("ApplicationType") && !(attrValues.trim().length()==0))
		{
			////System.out.println(" attrValues ApplicationType "+attrValues);
			for (ApplicationType_Enum s : ApplicationType_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("AddressType") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType amitabh  "+attrValues.replace(" ","_"));
			for (AddressType_Enum s : AddressType_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues.replace(" ","_"))) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			//System.out.println(" found AddressType  "+found);
			return found;
		}
		else if(attrName.equalsIgnoreCase("JointDocType") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType amitabh  "+attrValues.replace(" ","_"));
			for (JointDocType_Enum s : JointDocType_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues.replace(" ","_"))) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			//System.out.println(" found AddressType  "+found);
			return found;
		}
		else if(attrName.equalsIgnoreCase("PrimaryDocType") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType amitabh  "+attrValues.replace(" ","_"));
			for (PrimaryDocType_Enum s : PrimaryDocType_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues.replace(" ","_"))) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			//System.out.println(" found AddressType  "+found);
			return found;
		}*/
		/*else if(attrName.equalsIgnoreCase("PrimaryEmpType") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType amitabh  "+attrValues.replace(" ","_"));
			for (PrimaryEmpType_Enum s : PrimaryEmpType_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues.replace(" ","_"))) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			//System.out.println(" found AddressType  "+found);
			return found;
		}*/
		/*else if(attrName.equalsIgnoreCase("IsChequeBReq") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (IsChequeBReq_Enum s : IsChequeBReq_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("PrimaryTitle") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (PrimaryTitle_Enum s : PrimaryTitle_Enum.values())
			{
				String str = s.name()+"."; 
				if (str.equalsIgnoreCase(attrValues)) {
					found="true";
					str = "";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("PrimaryGender") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (PrimaryGender_Enum s : PrimaryGender_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("PrimaryMaritalStatus") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (PrimaryMaritalStatus_Enum s : PrimaryMaritalStatus_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("JointMaritalStatus") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (JointMaritalStatus_Enum s : JointMaritalStatus_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("ISUSCitizenPrimary") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (ISUSCitizenPrimary_Enum s : ISUSCitizenPrimary_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("IsUSTAXPayerPrimary") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (IsUSTAXPayerPrimary_Enum s : IsUSTAXPayerPrimary_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("InterestsHawalaPrimary") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (InterestsHawalaPrimary_Enum s : InterestsHawalaPrimary_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("IsDualNationalityPrimary") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (IsDualNationalityPrimary_Enum s : IsDualNationalityPrimary_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("IsUSCitizenJoint") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (IsUSCitizenJoint_Enum s : IsUSCitizenJoint_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("IsUSTAXPayerJoint") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (IsUSTAXPayerJoint_Enum s : IsUSTAXPayerJoint_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("InterestsHawalaJoint") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (InterestsHawalaJoint_Enum s : InterestsHawalaJoint_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}
		else if(attrName.equalsIgnoreCase("IsDualNationalityJoint") && !(attrValues.trim().length()==0))
		{
			//System.out.println(" attrValues AddressType "+attrValues);
			for (IsDualNationalityJoint_Enum s : IsDualNationalityJoint_Enum.values())
			{
				if (s.name().equalsIgnoreCase(attrValues)) {
					found="true";
					break;
		        }
				else 
					found="false";	
			}
			return found;
		}*/
						
		return found;
	}
	private void validateInputValuesFunction(String attributeList[]) throws WICreateException
	{
		//Added by amitabh after mandatory check. we are calling to validate input values
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
		
		if(attrName.equalsIgnoreCase("PrimaryDocExpDate")||
		attrName.equalsIgnoreCase("PrimaryEmiratesIDExpDate")||
		attrName.equalsIgnoreCase("JointDOB")||attrName.equalsIgnoreCase("JointDocExpDate")
		||attrName.equalsIgnoreCase("JointEmiratesIDExpDate")||
		attrName.equalsIgnoreCase("PrimaryDOB") ||
		attrName.equalsIgnoreCase("PrimaryVisaExpDate") ||
		attrName.equalsIgnoreCase("JointVisaExpDate"))
		{
			throw new WICreateException("1005", "Invalid date format for field:" +attrName+" Format should be YYYY-MM-DD format");
		}
		/*else if(attrName.equalsIgnoreCase("PrimaryEmpType"))
		{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
				    "PrimaryEmpType\n"+" Allowded values are [Salaried,Self Employed,Pensioner,Others]");
		}
		else if(attrName.equalsIgnoreCase("PrimaryDocType"))
		{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
				    "PrimaryDocType\n"+" Allowded values are [Khulasat QAID Number,Passport]");
		}*/
		else if(attrName.equalsIgnoreCase("PrimaryEmailID"))
		{
			throw new WICreateException("1005", "Not a valid emailid for field:" +
				    "PrimaryEmailID\n");
		}
		else if(attrName.equalsIgnoreCase("JointEmailID"))
		{
			throw new WICreateException("1005", "Not a valid emailid for field:" +
				    " JointEmailID\n");
		}
		/*else if(attrName.equalsIgnoreCase("IsPrimaryExisiting"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsPrimaryExisiting\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsPrimaryExisiting_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("IsDebitCardReq"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsDebitCardReq\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsDebitCardReq_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("ApplicationType"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "ApplicationType\n" +
		    " Allowded values are "+java.util.Arrays.asList(ApplicationType_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("AddressType"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "AddressType\n" +
		    " Allowded values are "+java.util.Arrays.asList(AddressType_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("JointDocType"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "JointDocType\n" +
		    " Allowded values are [Passport,Khulasat QAID number]");
    	}
		else if(attrName.equalsIgnoreCase("IsChequeBReq"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsChequeBReq\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsChequeBReq_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("PrimaryTitle"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "PrimaryTitle\n" +
		    " Allowded values are [Mr., Mrs., Ms.]");
    	}
		else if(attrName.equalsIgnoreCase("PrimaryGender"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "PrimaryGender\n" +
		    " Allowded values are "+java.util.Arrays.asList(PrimaryGender_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("PrimaryMaritalStatus"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "PrimaryMaritalStatus\n" +
		    " Allowded values are "+java.util.Arrays.asList(PrimaryMaritalStatus_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("JointMaritalStatus"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "JointMaritalStatus\n" +
		    " Allowded values are "+java.util.Arrays.asList(JointMaritalStatus_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("ISUSCitizenPrimary"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "ISUSCitizenPrimary\n" +
		    " Allowded values are "+java.util.Arrays.asList(ISUSCitizenPrimary_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("IsUSTAXPayerPrimary"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsUSTAXPayerPrimary\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsUSTAXPayerPrimary_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("InterestsHawalaPrimary"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "InterestsHawalaPrimary\n" +
		    " Allowded values are "+java.util.Arrays.asList(InterestsHawalaPrimary_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("IsDualNationalityPrimary"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsDualNationalityPrimary\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsDualNationalityPrimary_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("IsUSCitizenJoint"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsUSCitizenJoint\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsUSCitizenJoint_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("IsUSTAXPayerJoint"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsUSTAXPayerJoint\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsUSTAXPayerJoint_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("InterestsHawalaJoint"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "InterestsHawalaJoint\n" +
		    " Allowded values are "+java.util.Arrays.asList(InterestsHawalaJoint_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("IsDualNationalityJoint"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "IsDualNationalityJoint\n" +
		    " Allowded values are "+java.util.Arrays.asList(IsDualNationalityJoint_Enum.values()));
    	}
		else if(attrName.equalsIgnoreCase("JointGender"))
    	{
			throw new WICreateException("1005", "Passed Input values are not allowed for field:" +
		    "JointGender\n" +
		    " Allowded values are "+java.util.Arrays.asList(JointGender_Enum.values()));
    	}*/		
	}
//##############################################################################################
}
	//This function validate emailid
	private String validateEmail(String strValue)
	{
		String regex ="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(strValue);
		if(matcher.matches())
		return "true";
		else
		return "false";
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
	
	private void getActivityID(String strProcessName, String strActivityName) throws WICreateException
    {
		String sQuery="SELECT ActivityId FROM ACTIVITYTABLE with(nolock) WHERE ProcessDefId IN (SELECT ProcessDefId FROM ProcessDefTable with(nolock) WHERE ProcessName='"+strProcessName+"') AND ActivityName='"+strActivityName+"'";
		sInputXML=getAPSelectWithColumnNamesXML(sQuery);
		WriteLog("Input XML: "+sInputXML);
		sOutputXML=executeAPI(sInputXML);
		xmlobj=new XMLParser(sOutputXML);
		checkCallsMainCode(xmlobj);
		sInitiateFromActivityId=getTagValues(sOutputXML, "ActivityId");
		WriteLog("ActivityId:"+sInitiateFromActivityId);
        sInitiateFromActivityName=strActivityName;
		WriteLog("ActivityName:"+sInitiateFromActivityName);
    }
	
	private void insertCustomerFircoData(String AlertText) throws WICreateException {
		
		String[] arrOfStr1 = AlertText.split("=============================");
		 if(arrOfStr1.length==1)
		 {
			 WriteLog("RLOS value of ReturnCodeFIRCO");
		 }
		 else if(arrOfStr1.length>1)
		 {
			// String rlos_firco_update ="update NG_RLOS_FIRCO set call_valid = 'N' where Workitem_no='"+processInstanceID+"' and Call_type='FIRCO'";
			 try {
				
				
				 Map<String,String> Columnvalues = new HashMap<String,String>(); 
				 for(int j=1;j<arrOfStr1.length;j++)
				 {
					 String sRecords=arrOfStr1[j].replace(": \n", ":"); 
					 sRecords=sRecords.replace(":\n", ":");
					 sRecords=sRecords.replace("'", "''"); // added to handle the PCUG 99 - firco impacted cases.
					 
					 if (sRecords.contains("Suspect detected")) {
						 BufferedReader bufReader = new BufferedReader(new StringReader(sRecords));
						 String line=null;
						 while((line=bufReader.readLine()) != null )
						 {
							 String[] PDFColumns = {"OFAC ID", "NAME", "MATCHINGTEXT", "ORIGIN", "DESIGNATION", "DATE OF BIRTH", "USER DATA 1", "NATIONALITY", "PASSPORT", "ADDITIONAL INFOS"};
							 for(int k=0;k<PDFColumns.length;k++)
							 {
								 if(line.contains(PDFColumns[k]+":"))
								 {
									 String colData = "";
									 String [] tmp = line.split(":");
									 //iRBLSysCheckIntegrationLog.iRBLSysCheckIntegrationLogger.debug("tmp.length : "+tmp.length+", line : "+line);
	
									 //********below loop added for handling hardcoded Fircosoft XML in offshore dev server
									 if(tmp.length == 1)
										 colData="";
									 else if(tmp[1].trim().equalsIgnoreCase("Synonyms") || tmp[1].trim().equalsIgnoreCase("none") || tmp[1].trim().equalsIgnoreCase(""))
										 colData="";
									 else
									 {
										 //colData=tmp[1].trim();
										 for(int m=1; m<tmp.length; m++)
										 {
											 colData=colData+" "+tmp[m].trim();
										 }
									 }
	
									 if("DATE OF BIRTH".equalsIgnoreCase(PDFColumns[k].trim()))
									 {
										 colData=colData.trim();
										 if(colData.length()==4)
											 colData="01-01-"+colData;
										 else if(colData.length()>10)
											 colData = colData.substring(0,10);
									 }	
	
									 Columnvalues.put(PDFColumns[k],colData);
									 WriteLog("Columnvalues " + Columnvalues);
									 WriteLog("colData " + colData);
									 WriteLog("PDFColumns " + PDFColumns[k]);
								 }
							 }
						 }
						String addinfo = Columnvalues.get("ADDITIONAL INFOS");
						addinfo=addinfo.replaceAll("'", "''");
						WriteLog("addinfo " + addinfo);
						
						String Name = Columnvalues.get("NAME");
						Name=Name.replaceAll("'", "''");
						WriteLog("Name " + Name);
						
						trTableColumn = "WI_name,U_ID,Additiona_info,Name,Date_of_birth,Designation,Matchingtext,Origin,Nationality,Passport,user_data_1";
						WriteLog("trTableColumn" + trTableColumn);
	
						trTableValue = "'" + wiName + "','" + Columnvalues.get("OFAC ID").toString().trim() + "'," + "'"
								+ addinfo + "','" + Name + "','" + Columnvalues.get("DATE OF BIRTH") + "'," + "'" + Columnvalues.get("DESIGNATION")
								+ "','" + Columnvalues.get("MATCHINGTEXT") + "','" + Columnvalues.get("ORIGIN") + "'," + "'" + Columnvalues.get("NATIONALITY") + "','" + Columnvalues.get("PASSPORT") + "','"
								+ Columnvalues.get("USER DATA 1")+"'" ;
				
						WriteLog("trTableValue " + trTableValue);
						String inputXML = getInputXMLInsert(GRID_DTLS);
						WriteLog("APInsert Input History: " + inputXML);
						sOutputXML = executeAPI(inputXML);
						WriteLog("APInsert Output History: " + sOutputXML);
						xmlobj = new XMLParser(sOutputXML);
						
						checkCallsMainCode(xmlobj);
						
					//	 String rlos_firco_query="INSERT INTO NG_RLOS_FIRCO(Process_name,Workitem_no,Firco_ID,Request_datatime,Workstep_name,Newgen_status,StatusBehavior,StatusName,AlertDetails,Updated_Datetime,Call_type,call_valid,passport) VALUES('"+formObject.getWFProcessName()+"','"+ formObject.getWFWorkitemName()+"','"+ReferenceNo+"','"+requestDate+"','"+formObject.getWFActivityName()+"','Pending','"+StatusBehavior+"','"+StatusName+"','"+AlertDetails+"','"+UpdatedDateAndTime+"',"+"'FIRCO','Y','"+Columnvalues.get("PASSPORT")+"')";
							
						Columnvalues.clear();
					 }
				 }

			 } catch(Exception e){
				 e.printStackTrace();
				 WriteLog(" Exceptio After ng_FIRCO_InsertData: "+e.getMessage());
			 }
		 }
	 
	}
	
	private void insertCustomerFircoDataDCC(String AlertText) throws WICreateException {

		//String ReferenceNo = "";

		String[] arrOfStr1 = AlertText.split("=============================");
		if (arrOfStr1.length == 1) {
			WriteLog("DCC value of ReturnCodeFIRCO");
		} else if (arrOfStr1.length > 1) {

			try {

				//int FIRCOSOFTGridsize = 0;
				Map<String, String> Columnvalues = new HashMap<String, String>();
				for (int j = 1; j < arrOfStr1.length; j++) {
					String sRecords = arrOfStr1[j].replace(": \n", ":");
					sRecords = sRecords.replace(":\n", ":");
					if (sRecords.contains("Suspect detected")) {
						BufferedReader bufReader = new BufferedReader(new StringReader(sRecords));
						String line = null;
						while ((line = bufReader.readLine()) != null) {
							String[] PDFColumns = { "OFAC ID", "NAME", "MATCHINGTEXT", "ORIGIN", "DESIGNATION",
									"DATE OF BIRTH", "USER DATA 1", "NATIONALITY", "PASSPORT", "ADDITIONAL INFOS" };
							for (int k = 0; k < PDFColumns.length; k++) {
								if (line.contains(PDFColumns[k] + ":")) {
									String colData = "";
									String[] tmp = line.split(":");
	
									// below loop added for handling hardcoded Fircosoft XML in offshore dev server
									if (tmp.length == 1)
										colData = "";// ***************************
									else if (tmp[1].trim().equalsIgnoreCase("Synonyms")
											|| tmp[1].trim().equalsIgnoreCase("none") || tmp[1].trim().equalsIgnoreCase(""))
										colData = "";
									else {
										// colData=tmp[1].trim();
										for (int m = 1; m < tmp.length; m++) {
											colData = colData + " " + tmp[m].trim();
										}
									}
	
									if ("DATE OF BIRTH".equalsIgnoreCase(PDFColumns[k].trim())) {
										colData = colData.trim();
										if (colData.length() == 4)
											colData = "01-01-" + colData;
										else if (colData.length() > 10)
											colData = colData.substring(0, 10);
									}
	
									Columnvalues.put(PDFColumns[k], colData);
	
								}
							}
						}
	
						trTableColumn = "WI_name,U_ID,Additiona_info,Name,Date_of_birth,Designation,Matchingtext,Origin,Nationality,Passport,user_data_1";
						WriteLog("trTableColumn" + trTableColumn);
	
						trTableValue = "'" + wiName + "','" + Columnvalues.get("OFAC ID").toString().trim() + "'," + "'"
								+ Columnvalues.get("ADDITIONAL INFOS") + "','" + Columnvalues.get("NAME") + "','"
								+ Columnvalues.get("DATE OF BIRTH") + "'," + "'" + Columnvalues.get("DESIGNATION") + "','"
								+ Columnvalues.get("MATCHINGTEXT") + "','" + Columnvalues.get("ORIGIN") + "'," + "'"
								+ Columnvalues.get("NATIONALITY") + "','" + Columnvalues.get("PASSPORT") + "','"
								+ Columnvalues.get("USER DATA 1") + "'";
						String inputXML = getInputXMLInsert(DCC_GR_UID);
						WriteLog("APInsert Input History: " + inputXML);
						sOutputXML = executeAPI(inputXML);
						WriteLog("APInsert Output History: " + sOutputXML);
						xmlobj = new XMLParser(sOutputXML);
						// Check Main Code
						checkCallsMainCode(xmlobj);
						Columnvalues.clear();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				WriteLog(" Exceptio After ng_FIRCO_InsertData: " + e.getMessage());
			}
		}
	}
	
	private void insertIntoFircoTable() throws WICreateException, Exception
	{
		if(hm.get("FIRCO_Flag").toUpperCase().startsWith("Y"))
		{
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"); //2022-06-03 10:42:54.000
			String Alertdetails = "";
			for (String name: hmMainDCC.keySet()){
				String key = name.toString();
				//String keyvalue = hmMainDCC.get(name).toString();  
				HashMap<String, String> hm1 = new HashMap<String, String>(); 
				if (key.toUpperCase().contains("UIDDetials".toUpperCase())) {
					hm1 = hmMainDCC.get(key);
					Alertdetails = "<" + hm1.get("Alertdetails") + ">";
				}
			}
			
			//String UIDDetials =getTagValues("");
			WriteLog("Inside insertIntoFircoTable");
			trTableColumn = "Process_name, Workitem_no, Firco_ID, Request_datatime,Workstep_name,Newgen_status,StatusBehavior,StatusName,AlertDetails,Remarks,Updated_Datetime,Call_type,call_valid,passport";
			WriteLog("trTableColumn" + trTableColumn);
			trTableValue = "'Digital_CC','" + wiName + "','" + hm.get("Prospect_id") + "','" + dateFormat1.format(new Date()) + "','Initiation','Pending','0','New','"
					+ Alertdetails + "','Workitem created through webservice','" + sDate + "','Primary','Y','" + hm.get("PassportNo") + "'";

			WriteLog("trTableValue" + trTableValue);
			sInputXML = getDBInsertInputFIRCOTable();
			WriteLog("APInsert Input FIRCO: " + sInputXML);
			sOutputXML = executeAPI(sInputXML);
			WriteLog("APInsert Output FIRCO: " + sOutputXML);
			xmlobj = new XMLParser(sOutputXML);
			// Check Main Code
			checkCallsMainCode(xmlobj);
			
		}
	}
	
	public static String apUpdateInput(String cabinetName,String sessionID, String tableName, String columnName,
			 String strValues,String sWhereClause)
	{
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<APUpdate_Input>\n");
		ipXMLBuffer.append("<Option>APUpdate</Option>\n");
		ipXMLBuffer.append("<TableName>");
		ipXMLBuffer.append(tableName);
		ipXMLBuffer.append("</TableName>\n");
		ipXMLBuffer.append("<ColName>");
		ipXMLBuffer.append(columnName);
		ipXMLBuffer.append("</ColName>\n");
		ipXMLBuffer.append("<Values>");
		ipXMLBuffer.append(strValues);
		ipXMLBuffer.append("</Values>\n");
		ipXMLBuffer.append("<WhereClause>");
		ipXMLBuffer.append(sWhereClause);
		ipXMLBuffer.append("</WhereClause>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("</APUpdate_Input>");

		return ipXMLBuffer.toString();
	 }
	
	public static String apDeleteInput(String cabinetName,String sessionID, String tableName, String sWhere)
	{ 	
		StringBuffer ipXMLBuffer=new StringBuffer();

		ipXMLBuffer.append("<?xml version=\"1.0\"?>\n");
		ipXMLBuffer.append("<APDelete_Input>\n");
		ipXMLBuffer.append("<Option>APDelete</Option>\n");
		ipXMLBuffer.append("<TableName>");
		ipXMLBuffer.append(tableName);
		ipXMLBuffer.append("</TableName>\n");
		ipXMLBuffer.append("<WhereClause>");
		ipXMLBuffer.append(sWhere);
		ipXMLBuffer.append("</WhereClause>\n");
		ipXMLBuffer.append("<EngineName>");
		ipXMLBuffer.append(cabinetName);
		ipXMLBuffer.append("</EngineName>\n");
		ipXMLBuffer.append("<SessionId>");
		ipXMLBuffer.append(sessionID);
		ipXMLBuffer.append("</SessionId>\n");
		ipXMLBuffer.append("</APDelete_Input>");

		return ipXMLBuffer.toString();
	}
}

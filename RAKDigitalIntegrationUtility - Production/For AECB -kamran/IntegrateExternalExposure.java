package com.newgen.DCC.SystemIntegration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.newgen.DCC.CAMGenCIFUpdate.Digital_CC_CAMTemplate;
import com.newgen.DCC.DECTECHIntegration.DCC_DECTECH_Integration_Input;
import com.newgen.common.CommonConnection;
import com.newgen.common.CommonMethods;
import com.newgen.omni.jts.cmgr.NGXmlList;
import com.newgen.omni.jts.cmgr.XMLParser;

public class IntegrateExternalExposure
  {
	
	public static void main(String[] args){
		String flag = "";
		String responseXML = "";
		

		String abc = "<EE_EAI_MESSAGE> <EE_EAI_HEADER><MsgFormat>CUSTOMER_EXPOSURE</MsgFormat><MsgVersion>0001</MsgVersion><RequestorChannelId>CAS</RequestorChannelId><RequestorUserId>RAKUSER</RequestorUserId><RequestorLanguage>E</RequestorLanguage><RequestorSecurityInfo>secure</RequestorSecurityInfo><ReturnCode>0000</ReturnCode><ReturnDesc>Successful</ReturnDesc><MessageId>CAS16656523049763</MessageId><Extra1>REP||SHELL.JOHN</Extra1><Extra2>2022-10-13T01:11:49.288+04:00</Extra2></EE_EAI_HEADER><CustomerExposureResponse><RequestType>ExternalExposure</RequestType><ReferenceNumber>425986</ReferenceNumber><EnquiryDate>2022-10-01</EnquiryDate><ReportUrl>https://ant2a2aapps01.rakbanktst.ae:446/GetPdf.aspx?refno=KyGp6%2fcGA%2b0%3d</ReportUrl><IsDirect>N</IsDirect><CustInfo><FullNm>NASIMA BANU</FullNm><BirthDt>1995-06-29</BirthDt><Nationality>INDIA</Nationality><Gender>Female</Gender><Activity>0</Activity><TotalOutstanding>200.00</TotalOutstanding><TotalOverdue>0.00</TotalOverdue><NoOfContracts>0</NoOfContracts><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>NameENInfo</InfoType><CustName>MASIMA BANU SYED ALI</CustName><CustNameType>FullENName</CustNameType><ActualFlag>false</ActualFlag><ProviderNo>B01</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-10-01</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>NameENInfo</InfoType><CustName>NASIMA BANU</CustName><CustNameType>FullENName</CustNameType><ActualFlag>true</ActualFlag><ProviderNo>B01</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-09-19</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>NameENInfo</InfoType><CustName>NASIMA BANU</CustName><CustNameType>FullENName</CustNameType><ActualFlag>true</ActualFlag><ProviderNo>C02</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-09-05</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>NameENInfo</InfoType><CustName>NASIMA BANU SYED ALI</CustName><CustNameType>FullENName</CustNameType><ActualFlag>false</ActualFlag><ProviderNo>B01</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-09-06</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>GenderHistoryInfolst</InfoType><Gender>F</Gender><ActualFlag>true</ActualFlag><ProviderNo>B01</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-10-01</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>GenderHistoryInfolst</InfoType><Gender>F</Gender><ActualFlag>true</ActualFlag><ProviderNo>C02</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-09-05</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>GenderHistoryInfolst</InfoType><Gender>M</Gender><ActualFlag>false</ActualFlag><ProviderNo>B01</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-09-19</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>DOBHistoryInfolst</InfoType><DOB>1995-06-29</DOB><ActualFlag>true</ActualFlag><ProviderNo>B01</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-10-01</DateOfUpdate></CustInfoListDet><CustInfoListDet><ReferenceNumber>425986</ReferenceNumber><InfoType>DOBHistoryInfolst</InfoType><DOB>1995-06-29</DOB><ActualFlag>true</ActualFlag><ProviderNo>C02</ProviderNo><CreatedOn>2022-10-01</CreatedOn><DateOfUpdate>2022-09-05</DateOfUpdate></CustInfoListDet><PhoneInfo><ReportedDate>2022-09-06</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2022-09-06</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2022-10-01</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2022-10-01</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2022-09-19</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2022-09-19</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2022-09-06</ReportedDate></PhoneInfo><PhoneInfo><ReportedDate>2022-09-06</ReportedDate></PhoneInfo><InquiryInfo><ContractCategory>C</ContractCategory></InquiryInfo><InquiryInfo><ContractCategory>C</ContractCategory></InquiryInfo><InquiryInfo><ContractCategory>C</ContractCategory></InquiryInfo><InquiryInfo><ContractCategory>C</ContractCategory></InquiryInfo></CustInfo><ScoreInfo><Value>747</Value><Range>K</Range></ScoreInfo><AddrInfo><EnrichedThroughEnquiry>1</EnrichedThroughEnquiry></AddrInfo><RecordDestributions><RecordDestribution><ContractType>TotalSummary</ContractType><Contract_Role_Type></Contract_Role_Type><TotalNo>5</TotalNo><DataProvidersNo>2</DataProvidersNo><RequestNo>4</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>1</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>Installments</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>0</TotalNo><DataProvidersNo>0</DataProvidersNo><RequestNo>0</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>0</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>NotInstallments</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>0</TotalNo><DataProvidersNo>0</DataProvidersNo><RequestNo>0</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>0</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>CreditCards</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>5</TotalNo><DataProvidersNo>1</DataProvidersNo><RequestNo>4</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>1</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>Services</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>0</TotalNo><DataProvidersNo>0</DataProvidersNo><RequestNo>0</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>0</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution><RecordDestribution><ContractType>Utility</ContractType><Contract_Role_Type>Holder</Contract_Role_Type><TotalNo>0</TotalNo><DataProvidersNo>0</DataProvidersNo><RequestNo>0</RequestNo><DeclinedNo>0</DeclinedNo><RejectedNo>0</RejectedNo><NotTakenUpNo>0</NotTakenUpNo><ActiveNo>0</ActiveNo><ClosedNo>0</ClosedNo></RecordDestribution></RecordDestributions><Derived><Total_Exposure>10000</Total_Exposure><Oldest_Contract_Start_Date>25-10-2021</Oldest_Contract_Start_Date><WorstCurrentPaymentDelay>0</WorstCurrentPaymentDelay><Worst_PaymentDelay_Last24Months>0</Worst_PaymentDelay_Last24Months><Worst_Status_Last24Months>U</Worst_Status_Last24Months><Nof_Records>1</Nof_Records><NoOf_Cheque_Return_Last3>0</NoOf_Cheque_Return_Last3><Nof_DDES_Return_Last3Months>0</Nof_DDES_Return_Last3Months><Nof_DDES_Return_Last6Months>0</Nof_DDES_Return_Last6Months><Nof_Cheque_Return_Last6>0</Nof_Cheque_Return_Last6><Nof_Enq_Last90Days>0</Nof_Enq_Last90Days><Nof_Enq_Last60Days>0</Nof_Enq_Last60Days><Nof_Enq_Last30Days>0</Nof_Enq_Last30Days><TotOverDue_GuarteContrct>0</TotOverDue_GuarteContrct></Derived><ProductExposureDetails><CardDetails><CardEmbossNum>505516967</CardEmbossNum><CardStatus>Active</CardStatus><CardType>01</CardType><CardTypeDesc>01</CardTypeDesc><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>C02</ProviderNo><KeyDt><KeyDtType>StartDate</KeyDtType><KeyDtValue>2021-10-25</KeyDtValue></KeyDt><KeyDt><KeyDtType>MaxOverDueAmountDate</KeyDtType><KeyDtValue>2022-06-30</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>CurrentBalance</AmtType><Amt>200</Amt></AmountDtls><AmountDtls><AmtType>TotalAmount</AmtType><Amt>10000</Amt></AmountDtls><AmountDtls><AmtType>PaymentsAmount</AmtType><Amt>0</Amt></AmountDtls><AmountDtls><AmtType>CashLimit</AmtType><Amt>10000</Amt></AmountDtls><AmountDtls><AmtType>OverdueAmount</AmtType><Amt>0</Amt></AmountDtls><WriteoffStat>U</WriteoffStat><WriteoffStatDt>2022-06-30</WriteoffStatDt><NofDaysPmtDelay>0</NofDaysPmtDelay><MaxDaysPmtDelay>0</MaxDaysPmtDelay><MaxDaysPmtDelayDt>2022-06-30</MaxDaysPmtDelayDt><MonthsOnBook>11.00</MonthsOnBook><LastRepmtDt>2022-06-01</LastRepmtDt><IsDuplicate>0</IsDuplicate><NonActivePmt>0</NonActivePmt><IsCurrent>1</IsCurrent><CurUtilRate>2</CurUtilRate><AECBHistMonthCnt>8</AECBHistMonthCnt><DPD5_Last3Months>0</DPD5_Last3Months><DPD30_Last6Months>0</DPD30_Last6Months><DPD60_Last18Months>0</DPD60_Last18Months><DPD60Plus_Last12Months>0</DPD60Plus_Last12Months><DPD5_Last12Months>0</DPD5_Last12Months><MaximumOverDueAmount>0</MaximumOverDueAmount><Utilizations24Months><Month>06-2022</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>200</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>05-2022</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>300</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>04-2022</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>120</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>03-2022</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>100</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>02-2022</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>420</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>01-2022</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>450</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>12-2021</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>350</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>11-2021</Month><CreditLimit>10000</CreditLimit><OutstandingBalance>200</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>10-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>09-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>08-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>07-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>06-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>05-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>04-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>03-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>02-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>01-2021</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>12-2020</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>11-2020</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>10-2020</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>09-2020</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>08-2020</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><Utilizations24Months><Month>07-2020</Month><CreditLimit></CreditLimit><OutstandingBalance>0</OutstandingBalance></Utilizations24Months><History><Key>06-2022</Key><Status>OK</Status></History><History><Key>05-2022</Key><Status>OK</Status></History><History><Key>04-2022</Key><Status>OK</Status></History><History><Key>03-2022</Key><Status>OK</Status></History><History><Key>02-2022</Key><Status>OK</Status></History><History><Key>01-2022</Key><Status>OK</Status></History><History><Key>12-2021</Key><Status>OK</Status></History><History><Key>11-2021</Key><Status>OK</Status></History><History><Key>10-2021</Key><Status>N/A</Status></History><History><Key>09-2021</Key><Status>N/A</Status></History><History><Key>08-2021</Key><Status>N/A</Status></History><History><Key>07-2021</Key><Status>N/A</Status></History><History><Key>06-2021</Key><Status>N/A</Status></History><History><Key>05-2021</Key><Status>N/A</Status></History><History><Key>04-2021</Key><Status>N/A</Status></History><History><Key>03-2021</Key><Status>N/A</Status></History><History><Key>02-2021</Key><Status>N/A</Status></History><History><Key>01-2021</Key><Status>N/A</Status></History><History><Key>12-2020</Key><Status>N/A</Status></History><History><Key>11-2020</Key><Status>N/A</Status></History><History><Key>10-2020</Key><Status>N/A</Status></History><History><Key>09-2020</Key><Status>N/A</Status></History><History><Key>08-2020</Key><Status>N/A</Status></History><History><Key>07-2020</Key><Status>N/A</Status></History><AdditionalAccountInfo><MaxOverDueAmount>0</MaxOverDueAmount><MaxOverDueAmountDate>30/06/2022</MaxOverDueAmountDate></AdditionalAccountInfo></CardDetails><CardDetails><CardEmbossNum>I05520274</CardEmbossNum><CardStatus>Pipeline</CardStatus><CardType>01</CardType><CardTypeDesc>Requested</CardTypeDesc><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>B01</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2022-10-01</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>TotalAmount</AmtType><Amt>90000</Amt></AmountDtls><AmountDtls><AmtType>CashLimit</AmtType><Amt>90000</Amt></AmountDtls><DelinquencyInfo><BucketType>NoOfInstallments</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>NoOfDaysInPipeLine</BucketType><BucketValue>0</BucketValue></DelinquencyInfo></CardDetails><CardDetails><CardEmbossNum>J05520280</CardEmbossNum><CardStatus>Pipeline</CardStatus><CardType>00</CardType><CardTypeDesc>Requested</CardTypeDesc><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>B01</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2022-09-06</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>TotalAmount</AmtType><Amt>5000</Amt></AmountDtls><AmountDtls><AmtType>CashLimit</AmtType><Amt>5000</Amt></AmountDtls><DelinquencyInfo><BucketType>NoOfInstallments</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>NoOfDaysInPipeLine</BucketType><BucketValue>25</BucketValue></DelinquencyInfo></CardDetails><CardDetails><CardEmbossNum>L05520109</CardEmbossNum><CardStatus>Pipeline</CardStatus><CardType>00</CardType><CardTypeDesc>Requested</CardTypeDesc><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>B01</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2022-09-19</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>TotalAmount</AmtType><Amt>5000</Amt></AmountDtls><AmountDtls><AmtType>CashLimit</AmtType><Amt>5000</Amt></AmountDtls><DelinquencyInfo><BucketType>NoOfInstallments</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>NoOfDaysInPipeLine</BucketType><BucketValue>12</BucketValue></DelinquencyInfo></CardDetails><CardDetails><CardEmbossNum>P05520293</CardEmbossNum><CardStatus>Pipeline</CardStatus><CardType>01</CardType><CardTypeDesc>Requested</CardTypeDesc><CustRoleType>Main Contract Holder</CustRoleType><ProviderNo>B01</ProviderNo><KeyDt><KeyDtType>LastUpdateDate</KeyDtType><KeyDtValue>2022-09-06</KeyDtValue></KeyDt><CurCode></CurCode><AmountDtls><AmtType>TotalAmount</AmtType><Amt>9000</Amt></AmountDtls><AmountDtls><AmtType>CashLimit</AmtType><Amt>9000</Amt></AmountDtls><DelinquencyInfo><BucketType>NoOfInstallments</BucketType><BucketValue>0</BucketValue></DelinquencyInfo><DelinquencyInfo><BucketType>NoOfDaysInPipeLine</BucketType><BucketValue>25</BucketValue></DelinquencyInfo></CardDetails></ProductExposureDetails><Ratios><ExpenseToSalaryRatio>0</ExpenseToSalaryRatio></Ratios><SalaryCreditDetails><AccountType>D</AccountType><Phase>A</Phase><IBAN>AE54545445454541298</IBAN><CBAccountNo>C00057232</CBAccountNo><DPAccountNo>6362_10</DPAccountNo><ProviderNo>B13</ProviderNo><StartDate>2020-08-01</StartDate><DateOfLastUpdate>2022-10-13</DateOfLastUpdate><SalaryCreditHistory><ReferenceDate>2022-09</ReferenceDate><TotalSalaryAmount>20000</TotalSalaryAmount><NumberOfSalariesTransferred>1</NumberOfSalariesTransferred></SalaryCreditHistory><SalaryCreditHistory><ReferenceDate>2022-10</ReferenceDate><TotalSalaryAmount>15000</TotalSalaryAmount><NumberOfSalariesTransferred>1</NumberOfSalariesTransferred></SalaryCreditHistory><SalaryCreditHistory><ReferenceDate>2022-08</ReferenceDate><TotalSalaryAmount>23000</TotalSalaryAmount><NumberOfSalariesTransferred>2</NumberOfSalariesTransferred></SalaryCreditHistory></SalaryCreditDetails><SalaryCreditDetails><AccountType>D</AccountType><Phase>A</Phase><IBAN>AE54545569854214004</IBAN><CBAccountNo>C00014885</CBAccountNo><DPAccountNo>6362_10</DPAccountNo><ProviderNo>B13</ProviderNo><StartDate>2020-08-01</StartDate><DateOfLastUpdate>2022-10-13</DateOfLastUpdate><SalaryCreditHistory><ReferenceDate>2022-12</ReferenceDate><TotalSalaryAmount>21500</TotalSalaryAmount><NumberOfSalariesTransferred>1</NumberOfSalariesTransferred></SalaryCreditHistory><SalaryCreditHistory><ReferenceDate>2023-01</ReferenceDate><TotalSalaryAmount>19000</TotalSalaryAmount><NumberOfSalariesTransferred>1</NumberOfSalariesTransferred></SalaryCreditHistory><SalaryCreditHistory><ReferenceDate>2023-02</ReferenceDate><TotalSalaryAmount>19500</TotalSalaryAmount><NumberOfSalariesTransferred>2</NumberOfSalariesTransferred></SalaryCreditHistory></SalaryCreditDetails></CustomerExposureResponse></EE_EAI_MESSAGE>";
		
		 flag = ResponseParser.getOutputXMLValues(abc, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
		          CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false), CommonConnection.getCabinetName(), 
		          "DCC-TEST", "", "", "","");
	}

    private static String CheckGridTable = "NG_DCC_EXTTABLE";
    
    public static String IntegratewithMW(String processInstanceID, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap,String WorkItemID,String ActivityID,String ActivityType,String ProcessDefId) throws IOException, Exception
      {
    	
    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Inside external exposure Integration " );
    	String MainStatusFlag = "Success";
    	String DBQuery = "SELECT Wi_Name, CIF_ID, PassportNo, EmirateID, MobileNo,requested_limit as Final_Limit, Title, FirstName, MiddleName, LastName, dob, Nationality, Designation, Cust_Decl_Salary, "
				+ "Prospect_id, FinalDBR, Passport_expiry, Gender_Code, IndusSeg, EXTERNAL_EXPOSURE_STATUS,Dectech_Flag,Is_CAM_generated FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
        
        String extTabDataIPXML = CommonMethods.apSelectWithColumnNames(DBQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false));
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("extTabDataIPXML: " + extTabDataIPXML);
        String extTabDataOPXML = CommonMethods.WFNGExecute(extTabDataIPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOPXML);

        XMLParser xmlParserData = new XMLParser(extTabDataOPXML);
        int iTotalrec = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        String Is_CAM_Generated = xmlParserData.getValueOf("Is_CAM_generated");

        if (xmlParserData.getValueOf("MainCode").equalsIgnoreCase("0") && iTotalrec > 0)
          {
            String xmlDataExtTab = xmlParserData.getNextValueOf("Record");
            xmlDataExtTab = xmlDataExtTab.replaceAll("[ ]+>", ">").replaceAll("<[ ]+", "<");

            NGXmlList objWorkList = xmlParserData.createList("Records", "Record");

            HashMap<String, String> CheckGridDataMap = new HashMap<String, String>();

            for (; objWorkList.hasMoreElements(true); objWorkList.skip(true))
              {
            	String gender=objWorkList.getVal("Gender_Code");
                if ("F".equalsIgnoreCase(gender)) {
                    gender = "1";
                } else if ("M".equalsIgnoreCase(gender)) {
                    gender = "2";
                } else {
                    gender = "3";
                }
            	CheckGridDataMap.put("Wi_Name", validateValue(objWorkList.getVal("Wi_Name")));
                CheckGridDataMap.put("Final_Limit", validateValue(objWorkList.getVal("Final_Limit")));
                CheckGridDataMap.put("FirstNm", validateValue(objWorkList.getVal("FirstName")));
                CheckGridDataMap.put("LastNm", validateValue(objWorkList.getVal("LastName")));
                CheckGridDataMap.put("CUSTOMERNAME", validateValue(objWorkList.getVal("CUSTOMERNAME")));
                CheckGridDataMap.put("Gender", validateValue(gender));
                CheckGridDataMap.put("EXTERNAL_EXPOSURE_STATUS", validateValue(objWorkList.getVal("EXTERNAL_EXPOSURE_STATUS")));
                CheckGridDataMap.put("MobileNo", validateValue(objWorkList.getVal("MobileNo")));
                CheckGridDataMap.put("EmirateID", validateValue(objWorkList.getVal("EmirateID")));
                CheckGridDataMap.put("PassportNo", validateValue(objWorkList.getVal("PassportNo")));
                CheckGridDataMap.put("BirthDt", validateValue(objWorkList.getVal("dob")));
                CheckGridDataMap.put("Dectech_Flag", validateValue(objWorkList.getVal("Dectech_Flag")));
                CheckGridDataMap.put("Nationality", validateValue(objWorkList.getVal("Nationality")));
                
                String flag = "Y";
                if (!(CheckGridDataMap.get("EXTERNAL_EXPOSURE_STATUS").equalsIgnoreCase("Y")))//To Do append the second condition..
                {
                	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Inside AECB Call for WI Number: " + processInstanceID);
                	
                	flag = callExternalExposure(processInstanceID, CheckGridDataMap, integrationWaitTime, socketConnectionTimeOut, socketDetailsMap);
                	
                	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("AECB Flag" + processInstanceID);
                	
                	if("N".equalsIgnoreCase(flag))
                    {
                    	
                    	MainStatusFlag = "Failure~AECB call failed";
                    	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("callExternalExposure status : Failure");
                    }
                	else if("Y".equalsIgnoreCase(flag)){
                		
                		String aecbDataQuery= "select AECB_Score,Range,ReferenceNo from ng_dcc_cust_extexpo_Derived with(nolock) where Wi_Name='"+processInstanceID+"' and Request_Type= 'ExternalExposure'";
                		String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(aecbDataQuery, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false));
        		        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("extTabDataIPXML: " + extTabDataINPXML);
        		        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        		        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOUPXML);	
        				
        		        XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);
        		        
        		        String mainCode=xmlParserDataDB.getValueOf("MainCode");
        		        int iTotalreccam = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
        		        if("0".equalsIgnoreCase(mainCode) && iTotalreccam>0)
        		        {
        		        	String AECB_Score = xmlParserDataDB.getValueOf("AECB_Score");
        			        String range = xmlParserDataDB.getValueOf("Range");
        			        String ReferenceNo = xmlParserDataDB.getValueOf("ReferenceNo");
        			        if(range!=null && !"".equalsIgnoreCase(range))
        			        {
        			        	/*String numRegex="[0-9]+";
        			        	Pattern p = Pattern.compile(numRegex);
        			        	Matcher m = p.matcher(range);*/
        			        	if(range.matches("^[a-zA-Z]*$"))
            			        	range="Consumer Score "+range;
        			        	else if(range.matches("[0-9]+"))
            			        	range="No hit score "+range;
            			        else if(range.matches("^[a-zA-Z0-9]*$"))
            			        	range="Alternate score "+range;
            			        else
            			        	range="";
        			        }
        			        

    		        		String columnNames="AECB_Score,Score_range,bureau_reference_number";
    		        		String columnValues="'"+AECB_Score+"','"+range+"','"+ReferenceNo+"'";
    		        		String sWhereClause = "WI_NAME='" + processInstanceID + "'";
    		    	    	String tableName = "NG_DCC_EXTTABLE";
    		    	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false), 
    		    	        		tableName, columnNames, columnValues, sWhereClause);
    		    	        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
    		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
    		    	        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
    		    	        XMLParser sXMLParserChild = new XMLParser(outputXml);
    		    	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
    		    	        if(!"0".equals(StrMainCode))
    		    	        	MainStatusFlag = "Failure~Error in AECB Fields Update.";
    		        	
        		        }
        		        else
        		        	MainStatusFlag = "Failure~Error in AECB Fields Update.";
                	}
                }
                
                if ("Y".equalsIgnoreCase(flag)) 
                {
            		String Proc_name = "ng_dcc_insert_external_exposure";
            		DCCSystemIntegrationLog.DCCSystemIntegrationLogger.info("Inside process method where procedure need to be triggered");
            		String param = "'" +processInstanceID+ "'";
                    String Proc_inputXMl = CommonMethods.ExecuteQuery_APProcedure(Proc_name, param, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false));
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.info("Input xml for procedure"+Proc_inputXMl);

                    String outXml = CommonMethods.WFNGExecute(Proc_inputXMl,CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),1);
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.info("Output xml for procedure "+outXml);
                    XMLParser xmlParserDataDB = new XMLParser(outXml);
    		        
                    String Proc_main_Code =  xmlParserDataDB.getValueOf("MainCode");
                    DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Maincode for procedure : " + Proc_main_Code);
                	
                }
                
                if ("Y".equalsIgnoreCase(flag) && !(CheckGridDataMap.get("Dectech_Flag").equalsIgnoreCase("Y"))) 
                {
                	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("Inside DECTECH Call for WI Number: " + processInstanceID);
                	
                	String output=DCC_DECTECH_Integration_Input.GenerateXML(processInstanceID, ActivityID, ActivityType, ProcessDefId,WorkItemID);
                	if(!"Success".equalsIgnoreCase(output))
                	{
                		MainStatusFlag = "Failure~DECTECH call failed";
                	}
                	else{
                		CheckGridDataMap.put("Dectech_Flag", "Y");
                	}
                }
                //Cam generation ...
                
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("before CAM generation Is_CAM_Generated: " + Is_CAM_Generated);
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("before CAM generation Dectech_Flag: " + CheckGridDataMap.get("Dectech_Flag"));
                DCCSystemIntegrationLog.DCCSystemIntegrationLogger.error("before CAM generation AECB flag: " + flag);
                
                if("Y".equalsIgnoreCase(flag) && CheckGridDataMap.get("Dectech_Flag").equalsIgnoreCase("Y") && !"Y".equalsIgnoreCase(Is_CAM_Generated))
		        {
                	
                	String DBQuery1 = "SELECT CIF,Is_STP,Dectech_Decision,FIRCO_Flag,EFMS_Status,FTS_Ack_flg,FircoUpdateAction FROM NG_DCC_EXTTABLE with(nolock) WHERE WI_NAME='" + processInstanceID + "'";
    		        
    		        String extTabDataINPXML = CommonMethods.apSelectWithColumnNames(DBQuery1, CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false));
    		        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("extTabDataIPXML: " + extTabDataINPXML);
    		        String extTabDataOUPXML = CommonMethods.WFNGExecute(extTabDataINPXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
    		        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("extTabDataOPXML: " + extTabDataOUPXML);	
    				
    		        XMLParser xmlParserDataDB = new XMLParser(extTabDataOUPXML);
    		        
    		        String mainCode=xmlParserDataDB.getValueOf("MainCode");
    		        int iTotalreccam = Integer.parseInt(xmlParserData.getValueOf("TotalRetrieved"));
    		        if("0".equalsIgnoreCase(mainCode) && iTotalreccam>0)
    		        {
    		        	
    			        String Cif_Id = xmlParserDataDB.getValueOf("CIF");
    			        String Is_STP = xmlParserDataDB.getValueOf("Is_STP");
    			        String Dectech_Decision =xmlParserDataDB.getValueOf("Dectech_Decision");
    			        String firco=xmlParserDataDB.getValueOf("FIRCO_Flag");
    			        String efms=xmlParserDataDB.getValueOf("EFMS_Status");
    			        String fts =xmlParserDataDB.getValueOf("FTS_Ack_flg");
    			        String fircoAction =xmlParserDataDB.getValueOf("FircoUpdateAction");
    			        String pdfName = "";
    			        if ("Y".equalsIgnoreCase(Is_STP) || "D".equalsIgnoreCase(Dectech_Decision) || "A".equalsIgnoreCase(Dectech_Decision)||"CB".equalsIgnoreCase(firco)||"Confirmed Fraud".equalsIgnoreCase(efms)||"D".equalsIgnoreCase(fts)||"Decline".equalsIgnoreCase(fircoAction))
    		        	{
    		        		 pdfName = "STP_CAM_Report";
    		        		 
    		        		 	Digital_CC_CAMTemplate obj = new Digital_CC_CAMTemplate(DCCSystemIntegrationLog.DCCSystemIntegrationLogger);
    	    		        	String output = obj.generate_CAM_ReportT(pdfName,Cif_Id,processInstanceID,CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false));
    	    		        	if(output==null || !output.contains("Success~"))
    	    		        	 {
    	    		        		 MainStatusFlag = "Failure~Error in Cam Genration.";
    	    		        	 }
    	    		        	else
    	    		        	{
    	    		        		String columnNames="Is_CAM_generated";
    	    		        		String columnValues="'Y'";
    	    		        		String sWhereClause = "WI_NAME='" + processInstanceID + "'";
    	    		    	    	String tableName = "NG_DCC_EXTTABLE";
    	    		    	        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false), 
    	    		    	        		tableName, columnNames, columnValues, sWhereClause);
    	    		    	        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Input XML for apUpdateInput for " + tableName + " Table : " + inputXML);
    	    		    	        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
    	    		    	        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Output XML for apUpdateInput for " + tableName + " Table : " + outputXml);
    	    		    	        XMLParser sXMLParserChild = new XMLParser(outputXml);
    	    		    	        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
    	    		    	        if(!"0".equals(StrMainCode))
    	    		    	        	MainStatusFlag = "Failure~Error in Cam Flag Update.";
    	    		        	}
    			        }
    			        else if(!"N".equalsIgnoreCase(Is_STP))
    			        {
    			        	MainStatusFlag = "Failure~Error in Cam Generation(STP flag is not valid).";
    			        	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Error in Cam Generation(STP flag is not valid): " + Is_STP);
    			        }
    		        	/*else
    		        	{
    		        		 pdfName = "NON_STP_CAM_Report";
    		        	}*/
    		        	
    			        
    		        }
    		        else
    		        {
    		        	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Error in cam generation..ApSelect for Is_CAM_Generated flag..");
    		        	MainStatusFlag = "Failure~Error in cam Generation";
    		        }
		        	
				}
		        else
		        {
		        	DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Cam Report Is Already Generated");
				}
			 }
          }
        else
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("WmgetWorkItem status: " + xmlParserData.getValueOf("MainCode"));
          }
        	return MainStatusFlag;
    }

    private static String callExternalExposure(String wiName, HashMap<String, String> CheckGridDataMap, int integrationWaitTime, int socketConnectionTimeOut, HashMap<String, String> socketDetailsMap)
      throws IOException, Exception
      {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmm");
		String DateExtra2 = sdf1.format(new Date())+"+04:00";
		
		String flag = "";
        StringBuilder sInputXML = new StringBuilder();
        sInputXML = sInputXml(wiName,  DateExtra2, CheckGridDataMap);
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Request XML for ExternalExposure  " + sInputXML);
        
        String responseXML = socketConnection(CommonConnection.getCabinetName(), CommonConnection.getUsername(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false), 
        		CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), wiName, "ExternalExposure", socketConnectionTimeOut, integrationWaitTime, socketDetailsMap, sInputXML);
        
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Request XML for responseXML  " + responseXML);
        
        flag = ResponseParser.getOutputXMLValues(responseXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(),
          CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false), CommonConnection.getCabinetName(), 
          wiName, CheckGridDataMap.get("Product"), CheckGridDataMap.get("SubProduct"), CheckGridDataMap.get("CIF"), 
          CheckGridDataMap.get("CUSTOMER_TYPE"));
        flag = flag == "true" ? "Y" : "N";

    	String columnNames = "EXTERNAL_EXPOSURE_STATUS";
    	String columnValues = "'" + flag + "'";
    	String sWhereClause = "WI_NAME='" + wiName + "'";
        String inputXML = CommonMethods.apUpdateInput(CommonConnection.getCabinetName(), CommonConnection.getSessionID(DCCSystemIntegrationLog.DCCSystemIntegrationLogger, false), 
        		CheckGridTable, columnNames, columnValues, sWhereClause);
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Input XML for apUpdateInput for " + CheckGridTable + " Table : " + inputXML);
        String outputXml = CommonMethods.WFNGExecute(inputXML, CommonConnection.getJTSIP(), CommonConnection.getJTSPort(), 1);
        DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Output XML for apUpdateInput for " + CheckGridTable + " Table : " + outputXml);
        XMLParser sXMLParserChild = new XMLParser(outputXml);
        String StrMainCode = sXMLParserChild.getValueOf("MainCode");
        //String RetStatus = null;
        if (StrMainCode.equals("0"))
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Successful in apUpdateInput the record in : " + CheckGridTable);
            //RetStatus = "Success in apUpdateInput the record";
          }
        else
          {
            DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Error in Executing apUpdateInput sOutputXML : " + outputXml);
            //RetStatus = "Error in Executing apUpdateInput";
          }
        return flag;
      }

	private static StringBuilder sInputXml(String wiName, String DateExtra2, HashMap<String, String> CheckGridDataMap) {
		String emi_id = CheckGridDataMap.get("EmirateID");
		emi_id = emi_id.substring(0, 3) + "-" + emi_id.substring(3, 7) + "-" + emi_id.substring(7, 14) + "-"+ emi_id.substring(14, 15);
		StringBuilder sb = new StringBuilder(
				"<EE_EAI_MESSAGE>" + "\n"
                +"<EE_EAI_HEADER>" + "\n"
                +"<MsgFormat>CUSTOMER_EXPOSURE</MsgFormat>" + "\n"
                +"<MsgVersion>0001</MsgVersion>" + "\n"
                +"<RequestorChannelId>CAS</RequestorChannelId>" + "\n"
                +"<RequestorUserId>RAKUSER</RequestorUserId>" + "\n"
                +"<RequestorLanguage>E</RequestorLanguage>" + "\n"
                +"<RequestorSecurityInfo>secure</RequestorSecurityInfo>" + "\n"
                +"<ReturnCode>911</ReturnCode>" + "\n"
                +"<ReturnDesc>IssuerTimedOut</ReturnDesc>" + "\n"
                +"<MessageId>CUSTOMER_EXPOSUER_0V27</MessageId>" + "\n"
                +"<Extra1>REQ||SHELL.JOHN</Extra1>" + "\n"
                +"<Extra2>" + DateExtra2+"</Extra2>" + "\n"
			+"</EE_EAI_HEADER>" + "\n"
			+"<CustomerExposureRequest>" + "\n"
                +"<BankId>RAK</BankId>" + "\n"
                +"<BranchId>RAK123</BranchId>" + "\n"
                +"<RequestType>ExternalExposure</RequestType>" + "\n"
                +"<CustType>1</CustType>" + "\n"
                +"<UserId>deepak</UserId>" + "\n"
                +"<AcctId></AcctId>" + "\n"
                +"<TxnAmount>" + CheckGridDataMap.get("Final_Limit") + "</TxnAmount>" + "\n"
                +"<NoOfInstallments></NoOfInstallments>" + "\n"
                +"<DurationOfAgreement></DurationOfAgreement>" + "\n"
                +"<FirstNm>"+ CheckGridDataMap.get("FirstNm") +"</FirstNm>" + "\n"
                +"<LastNm>"+ CheckGridDataMap.get("LastNm") +"</LastNm>" + "\n"
                +"<FullNm>"+ CheckGridDataMap.get("FirstNm") +" "+ CheckGridDataMap.get("LastNm") +"</FullNm>" + "\n" 
                +"<BirthDt>"+ CheckGridDataMap.get("BirthDt") +"</BirthDt>" + "\n"
                +"<Gender>"+ CheckGridDataMap.get("Gender") +"</Gender>" + "\n"
                +"<Nationality>"+ CheckGridDataMap.get("Nationality") +"</Nationality>" + "\n"
                +"<InquiryPurpose>2</InquiryPurpose>" + "\n" 
                +"<ProviderApplNo>" + wiName.split("-")[1] + (new Date()).getTime() + "</ProviderApplNo>" + "\n"
                +"<CBApplNo></CBApplNo>" + "\n"
                +"<IsCoApplicant>0</IsCoApplicant>" + "\n"
                +"<LosIndicator>1</LosIndicator>" + "\n"
                +"<ContractType>1</ContractType>" + "\n"
                +"<OverridePeriod>0</OverridePeriod>" + "\n"
                +"<PrimaryMobileNo>" + CheckGridDataMap.get("MobileNo") +"</PrimaryMobileNo>" + "\n"
                +"<ConsentFlag>1</ConsentFlag>" + "\n"
                +"<BureauCategory>Retail</BureauCategory>" + "\n"
                +"<BureauId>10</BureauId>" + "\n"
                +"<CallType>Synchronous</CallType>" + "\n"
                +"<ScoreType>0</ScoreType>" + "\n"
                +"<LegalDocInfo>" + "\n"
                                +"<DocType>Emirates id</DocType>" + "\n"
                                +"<DocNum>"+ emi_id +"</DocNum>" + "\n"
                +"</LegalDocInfo>" + "\n"
                +"<LegalDocInfo>" + "\n"
                                +"<DocType>Passport Number</DocType>" + "\n"
                                +"<DocNum>"+CheckGridDataMap.get("PassportNo")+"</DocNum>" + "\n"
                +"</LegalDocInfo>" + "\n"
		+"</CustomerExposureRequest>" + "\n"
		+"</EE_EAI_MESSAGE>"
		);
		return  sb;
	}

	static String socketConnection(String cabinetName, String username, String sessionId, String sJtsIp,
			String iJtsPort, String processInstanceID, String ws_name,
			int connection_timeout, int integrationWaitTime,HashMap<String, String> socketDetailsMap, StringBuilder sInputXML)
	{

		String socketServerIP;
		int socketServerPort;
		Socket socket = null;
		OutputStream out = null;
		InputStream socketInputStream = null;
		DataOutputStream dout = null;
		DataInputStream din = null;
		String outputResponse = null;
		String inputRequest = null;
		String inputMessageID = null;

		try
		{
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("userName "+ username);
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("SessionId "+ sessionId);

			socketServerIP=socketDetailsMap.get("SocketServerIP");
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("SocketServerIP "+ socketServerIP);
			socketServerPort=Integer.parseInt(socketDetailsMap.get("SocketServerPort"));
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("SocketServerPort "+ socketServerPort);

	   		if (!("".equalsIgnoreCase(socketServerIP) && socketServerIP == null && socketServerPort==0))
	   		{

    			socket = new Socket(socketServerIP, socketServerPort);
    			socket.setSoTimeout(connection_timeout*1000);
    			out = socket.getOutputStream();
    			socketInputStream = socket.getInputStream();
    			dout = new DataOutputStream(out);
    			din = new DataInputStream(socketInputStream);
    			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Dout " + dout);
    			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Din " + din);

    			outputResponse = "";

    			inputRequest = getRequestXML( cabinetName,sessionId ,processInstanceID, ws_name, username, sInputXML);

    			if (inputRequest != null && inputRequest.length() > 0)
    			{
    				int inputRequestLen = inputRequest.getBytes("UTF-16LE").length;
    				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("RequestLen: "+inputRequestLen + "");
    				inputRequest = inputRequestLen + "##8##;" + inputRequest;
    				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("InputRequest"+"Input Request Bytes : "+ inputRequest.getBytes("UTF-16LE"));
    				dout.write(inputRequest.getBytes("UTF-16LE"));
    				dout.flush();
    			}
    			byte[] readBuffer = new byte[1000];
    			int num = din.read(readBuffer);
    			if (num > 0)
				{

					byte[] arrayBytes = new byte[num];
					System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
					outputResponse = outputResponse + new String(arrayBytes, "UTF-16LE");
					inputMessageID = outputResponse;
					DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("OutputResponse: " + outputResponse);

					if (!"".equalsIgnoreCase(outputResponse))

						outputResponse = getResponseXML(cabinetName, sJtsIp, iJtsPort, sessionId, processInstanceID,
								outputResponse, integrationWaitTime);

					if (outputResponse.contains("&lt;")) {
						outputResponse = outputResponse.replaceAll("&lt;", "<");
						outputResponse = outputResponse.replaceAll("&gt;", ">");
					}
				}
    			socket.close();

				outputResponse = outputResponse.replaceAll("</MessageId>","</MessageId><InputMessageId>"+inputMessageID+"</InputMessageId>");

				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("outputResponse "+outputResponse);
				return outputResponse;

    	 	}
    		else
    		{
    			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("SocketServerIp and SocketServerPort is not maintained "+"");
    			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("SocketServerIp is not maintained "+	socketServerIP);
    			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug(" SocketServerPort is not maintained "+	socketServerPort);
    			return "Socket Details not maintained";
    		}
		}
		catch (Exception e)
		{
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception Occured Mq_connection_CC"+e.getStackTrace());
			return "";
		}
		finally
		{
			try
			{
				if(out != null)
				{
					out.close();
					out=null;
				}
				if(socketInputStream != null)
				{

					socketInputStream.close();
					socketInputStream=null;
				}
				if(dout != null)
				{

					dout.close();
					dout=null;
				}
				if(din != null)
				{

					din.close();
					din=null;
				}
				if(socket != null)
				{
					if(!socket.isClosed())
						socket.close();
					socket=null;
				}

			}

			catch(Exception e)
			{
				DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Final Exception Occured Mq_connection_CC"+e.getStackTrace());
			}
		}
	}
	

	private static String getRequestXML(String cabinetName, String sessionId,
			String processInstanceID, String ws_name, String userName, StringBuilder sInputXML)
	{
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<APMQPUTGET_Input>");
		strBuff.append("<SessionId>" + sessionId + "</SessionId>");
		strBuff.append("<EngineName>" + cabinetName + "</EngineName>");
		strBuff.append("<XMLHISTORY_TABLENAME>NG_DCC_XMLLOG_HISTORY</XMLHISTORY_TABLENAME>");
		strBuff.append("<WI_NAME>" + processInstanceID + "</WI_NAME>");
		strBuff.append("<WS_NAME>" + ws_name + "</WS_NAME>");
		strBuff.append("<USER_NAME>" + userName + "</USER_NAME>");
		strBuff.append("<MQ_REQUEST_XML>");
		strBuff.append(sInputXML);
		strBuff.append("</MQ_REQUEST_XML>");
		strBuff.append("</APMQPUTGET_Input>");
		DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("GetRequestXML: "+ strBuff.toString());
		return strBuff.toString();
	}

	private static String getResponseXML(String cabinetName,String sJtsIp,String iJtsPort, String
			sessionId, String processInstanceID,String message_ID, int integrationWaitTime)
	{

		String outputResponseXML="";
		try
		{
			String QueryString = "select OUTPUT_XML from NG_DCC_XMLLOG_HISTORY with (nolock) where MESSAGE_ID ='"+message_ID+"' and WI_NAME = '"+processInstanceID+"'";
			String responseInputXML = CommonMethods.apSelectWithColumnNames(QueryString, cabinetName, sessionId);
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Response APSelect InputXML: "+responseInputXML);

			int Loop_count=0;
			do
			{
				String responseOutputXML=CommonMethods.WFNGExecute(responseInputXML,sJtsIp,iJtsPort,1);
				//DCCExternalExposureLog.DCCExternalExposureLogger.debug("Response APSelect OutputXML: "+responseOutputXML);

			    XMLParser xmlParserSocketDetails= new XMLParser(responseOutputXML);
			    String responseMainCode = xmlParserSocketDetails.getValueOf("MainCode");
			    //DCCExternalExposureLog.DCCExternalExposureLogger.debug("ResponseMainCode: "+responseMainCode);



			    int responseTotalRecords = Integer.parseInt(xmlParserSocketDetails.getValueOf("TotalRetrieved"));
			    //DCCExternalExposureLog.DCCExternalExposureLogger.debug("ResponseTotalRecords: "+responseTotalRecords);

			    if (responseMainCode.equals("0") && responseTotalRecords > 0)
				{

					String responseXMLData=xmlParserSocketDetails.getNextValueOf("Record");
					responseXMLData =responseXMLData.replaceAll("[ ]+>",">").replaceAll("<[ ]+", "<");

	        		XMLParser xmlParserResponseXMLData = new XMLParser(responseXMLData);
	        		//DCCExternalExposureLog.DCCExternalExposureLogger.debug("ResponseXMLData: "+responseXMLData);

	        		outputResponseXML=xmlParserResponseXMLData.getValueOf("OUTPUT_XML");
	        		//DCCExternalExposureLog.DCCExternalExposureLogger.debug("OutputResponseXML: "+outputResponseXML);
	        		
	        		if(outputResponseXML.contains("<MQ_RESPONSE_XML>"))
	        		{
	        			XMLParser xmlParserResponseXMLData1 = new XMLParser(outputResponseXML);
	        			outputResponseXML = xmlParserResponseXMLData1.getValueOf("MQ_RESPONSE_XML");
	        		}
	        		if("".equalsIgnoreCase(outputResponseXML)){
	        			outputResponseXML="Error";
	    			}
	        		break;
				}
			    Loop_count++;
			    Thread.sleep(1000);
			}
			while(Loop_count<integrationWaitTime);
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("integrationWaitTime: "+integrationWaitTime);

		}
		catch(Exception e)
		{
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occurred in outputResponseXML" + e.getMessage());
			DCCSystemIntegrationLog.DCCSystemIntegrationLogger.debug("Exception occurred in outputResponseXML" + e.getStackTrace());
			outputResponseXML="Error";
		}

		return outputResponseXML;

	}
	
	private static String validateValue(String value) {
		if (value != null && ! value.equals("") && !value.equalsIgnoreCase("null")) {
			return value.toString();
		}
		return "";
	}
}

package com.idev4.reports.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idev4.reports.domain.*;
import com.idev4.reports.dto.*;
import com.idev4.reports.repository.*;
import com.idev4.reports.util.Constant;
import com.idev4.reports.util.EnglishNumberToWords;
import com.idev4.reports.util.Queries;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.servlet.ServletContext;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ReportsService {

    private static final String FDARTA = "FDARTA.jrxml";
    private static final String TOPSHEET = "TOPSHEET.jrxml";
    private static final String PDCDETAIL = "PDCDETAIL.jrxml";
    private static final String RETENTIONRATE = "RETENTIONRATE.jrxml";
    private static final String PROJECTED_CLIENTS_LOANS_COM = "PROJECTED_CLIENTS_LOANS_COM.jrxml";
    private static final String PAR_BRANCHWISE = "PAR_BRANCHWISE.jrxml";
    private static final String ADC_MONTH_WISE_REPORT = "ADC_MONTH_WISE_REPORT.jrxml";
    private static final String ANML_INSURANCECLAIM = "ANML_INSURANCECLAIM.jrxml";
    private static final String PORTFOLIO_CONCENTRATION = "PORTFOLIO_CONCENTRATION.jrxml";
    private static final String PENDINGCLIENTS = "PENDINGCLIENTS.jrxml";
    private static final String TAGGEDCLIENTSCLAIM = "TAGGEDCLIENTSCLAIM.jrxml";
    private static final String PRODUCTWISEREPORTADDITION = "PRODUCTWISEREPORTADDITION.jrxml";
    private static final String AGENCIES_TRAGET_TRACKING = "AGENCIES_TRAGET_TRACKING.jrxml";
    private static final String TRANSFERRED_CLIENTS = "TRANSFERRED_CLIENTS.jrxml";
    private static final String ACTIVE_STATUS = "0005";
    private static final String DISBIRST_STATUS = "0009";
    private static final String DISGARD_STATUS = "0008";
    private static final String DEFERRED_STATUS = "1285";
    private static final String ADVANCE_STATUS = "1305";
    private static final String DISBURSEMENT = "Disbursement";
    private static final String ISLAMIC_TYPE = "0002";
    private static final String APPLICATIONS_STATUS = "0106";
    private static final String APROVED_STATUS = "0004";
    private static final String SUBMITTED_STATUS = "0002";
    private static final String TURNAROUNDTIME = "TURNAROUNDTIME.jrxml";
    private static final String FEMALEPARTICIPATION = "FEMALEPARTICIPATION.jrxml";
    private static final String DUES = "DUES.jrxml";
    private static final String PORTFOLIO_SEG = "PORTFOLIO_SEG.jrxml";
    private static final String PORTFOLIO_AT_RISK = "PORTFOLIO_AT_RISK.jrxml";
    private static final String RISKFLAG = "RISKFLAG.jrxml";
    private static final String PORTFOLIO_STAT_REP = "PORTFOLIO_STAT_REP.jrxml";
    private static final String PORTFOLIO_STAT_FROM = "PORTFOLIO_STAT_FROM.jrxml";
    private static final String CONSOLIDATED_LOAN = "CONSOLIDATED_LOAN.jrxml";
    private static final String ROR = "ROR.jrxml";
    private static final String LOAN_UTI_FORM = "LOAN_UTI_FORM.jrxml";
    private static final String MONTHLY_STATUS = "MONTHLY_STATUS.jrxml";
    private static final String PORTFOLIO_RISK = "PORTFOLIO_RISK.jrxml";
    private static final String FEMALE_PARTICIPATION_RATIO = "FEMALE_PARTICIPATION_RATIO.jrxml";
    private static final String BRANCH_TARGET_MANG = "BRANCH_TARGET_MANG.jrxml";
    private static final String MCB_CHEQUE = "MCB_CHECQUE.jrxml";
    private static final String MLABAF = "MLABAF.jrxml";
    private static final String BOP_INFO = "BOP_CHECQUE.jrxml";
    private static final String REM_REPORT = "REM_RATIO_REP.jrxml";
    private static final String FUND_TRANSFER_REPORT = "FUND_REPORT.jrxml";
    private static final String DISBURSEMENT_REPORT = "DISBURSEMENT_REPORT.jrxml";
    private static final String RECOVERY_REPORT = "RECOVERY_REPORT.jrxml";
    private static final String REM_RATIO_REP = "REM_RATIO_REP.jrxml";
    private static final String ORG_TAGGING_REPORT = "ORG_TAGGING_REPORT.jrxml";
    private static final String PRODUCT_WISE_DISBURSEMENT = "PRODUCT_WISE_DISBURSEMENT.jrxml";
    private static final String ORG_TAG_TIME_REPORT = "ORG_TAGGING_TIME_REPORT.jrxml";
    private static final String ORG_TAGGING_TIME_REPORT = "ORG_TAGGING_TIME_REPORT.jrxml";
    private static final String REVRSAL_ENTERIES_REPORT = "REVRSAL_ENTERIES.jrxml";
    private static final String TRAIL_BALANCE_REPORT = "Trail_balance_report.jrxml";
    private static final String BRANCH_RANKING = "BRANCH_RANKING.jrxml";
    private static final String CPC_REPORT = "CPC_REPORT.jrxml";
    private static final String AICG_TRAINING = "AICG-TRAINING.jrxml";
    private static final String MONTHLY_RANKING = "MONTHLY_RANKING.jrxml";
    private static final String GESA = "GESA.jrxml";
    private static final String PAR_REPORT = "PAR_REPORT.jrxml";
    private static final String ADVANCE_RECOVERY_REPORT = "ADV_REC_REPORT.jrxml";
    private static final String ADVANCE_CLIENT_REPORT = "ADV_CLNT_REPORT.jrxml";
    private static final String ADVANCE_MATURITY_REPORT = "ADV_MATURITY_REPORT.jrxml";
    private static final String WEEKLY_TARGET_REPORT = "WEEKLY_TRGT_REPORT.jrxml";
    private static final String AREA_DISBURSEMENT_REPORT = "AREA_DIS_REPORT.jrxml";
    private static final String LEAVE_AND_ATTENDANCE_MONITORING_REPORT = "LEAVE_AND_ATTENDANCE_MONITORING_REPORT.jrxml";
    private static final String CNIC_EXPIRY_DETAIL = "CNIC_EXPIRY_DETAIL.jrxml";
    private static final String CLIENT_LOAN_MATRITY = "Clients_Loans_Maturity_Report.jrxml";
    private static final String MOBILE_WALLET_DUE = "Mobile-Wallet_Due.jrxml";
    private static final String MOBILE_WALLET = "Mobile-Wallet.jrxml";
    private static final String CLIENT_LOAN_MATRITY_EXCEL = "Clients_Loans_Maturity_Excel.jrxml";
    private static final String MOBILE_WALLET_DUE_EXCEL = "Mobile-Wallet_Due_Excel.jrxml";
    private static final String MOBILE_WALLET_EXCEL = "Mobile_Wallet_Excel.jrxml";
    private static final String TAGGING_REPORT = "Tagging_Report.jrxml";
    private static final String TAGGING_REPORT_EXCEL = "Tagging_Report_Excel.jrxml";
    private static final String Mfcib_Report = "Mfcib_Report.jrxml";
    private static final String BM_CLIENT_LOAN_MATRITY = "BM_Clients_Loans_Maturity_Report.jrxml";
    private static final String BM_MOBILE_WALLET_DUE = "BM_Mobile-Wallet_Due.jrxml";
    private static final String MB_MOBILE_WALLET = "BM_Mobile-Wallet.jrxml";
    // Added By Naveed - Date - 31-12-2021
    // Animal picture report
    private static final String ANIMAL_PICTURE = "Animal_Pics.jrxml";
    // Added by Areeba - Date - 23-01-2022
    // SCR - Mobile Wallet Control
    private static final String MOB_WAL_LOANS = "MOB_WAL_LOANS.jrxml";
    // SCR - Portfolio transfer
    private static final String TRANSFER_CLIENTS_DETAILS = "TRANSFER_CLIENTS_DETAILS.jrxml";
    private static final String TOP_SHEET_TRANSFER_CLIENTS = "TOP_SHEET_TRANSFER_CLIENTS.jrxml";
    // Added by Naveed - Date 27-01-2022
    // SCR - RM Reports
    private static final String RISK_FLAGGING_REGION_REPORT = "RISK_FLAGGING_REGION_REPORT.jrxml";
    private static final String REGIONAL_DISBURSEMENT = "REGIONAL_DISBURSEMENT.jrxml";
    // Added by Areeba - Date - 15-02-2022
    // Accounts Reports
    private static final String PREMIUM_DATA = "PREMIUM_DATA.jrxml";
    private static final String PREMIUM_DATA_KM = "PREMIUM_DATA_KM.jrxml";
    private static final String WO_CLIENT_DATA = "WO_CLIENT_DATA.jrxml";
    private static final String WO_RECOVERY = "WO_RECOVERY.jrxml";
    private static final String ACC_RECOVERY = "RECOVERY.jrxml";
    private static final String CLIENT_DATA = "CLIENT_DATA.jrxml";
    private static final String KSZB_CLIENT_DATA = "KSZB_CLIENT_DATA.jrxml";
    // Added by Naveed - Date 07-03-2022
    // SCR - RM Reports
    private static final String REGIONAL_RECOVERY_TREND = "REGIONAL_RECOVERY_TREND.jrxml";
    private final String UDERTAKING_REPORT = "UDERTAKING_REPORT.jrxml";
    private final String HEALTHCARD = "HEALTHCARD.jrxml";
    private final String LABAF = "LABAF.jrxml";
    private final String SLABAF = "SLABAF.jrxml";
    private final String LAF = "LAF.jrxml";
    private final String MABAF = "MABAF.jrxml";
    private final String REPAYMENT = "REPAYMENT.jrxml";
    private final String OVERDUELOANS = "OVERDUELOANS.jrxml";
    private final String MONITORING = "MONITORING.jrxml";
    private final String POSTING = "POSTING.jrxml";
    private final String FUNDSTATEMENT = "FUNDSTATEMENT.jrxml";
    private final String INSURANCECLAIM = "INSURANCECLAIM.jrxml";
    private final String ACCOUNTSLEDGER = "ACCOUNTSLEDGER.jrxml";
    private final String KCR = "KCR.jrxml";
    private final String BOOKDETAILS = "BOOKDETAILS.jrxml";
    private final String DUERECOVERY = "DUERECOVERY.jrxml";
    private final String KSK_LPD = "KSK_LPD.jrxml";
    private final String WOMENPARTICIPATION = "WOMENPARTICIPATION.jrxml";
    private final String CLIENT_HEALTH_BENEFICIARY = "CLIENT_HEALTH_BENEFICIARY.jrxml";
    private final String INSURANCE_CLAIM = "INSURANCE_CLAIM_REPORT.jrxml";
    private final String BTAP = "BTAP.jrxml";
    private final String KSS_TRAINING = "KSS_TRAINING.jrxml";
    private final String CLIENT_RECOVERY_STATUS = "Clients_Recovery_Status_Branch_wise.jrxml";
    private final String DUE_VS_RECOVERY = "Pre-Covid_Due_vs_Recovery.jrxml";
    private final String MANAGEMENT_DASHBOARD = "Management_Dashboard.jrxml";
    private final String PORTFOLIO_RESCHEDULING = "Portfolio_Rescheduling.jrxml";
    private final String OLD_PORTFOLIO = "Old_ Portfolio.jrxml";
    private final String SALE_2_PENDING = "SALE_2_PENDING.jrxml";
    private final String AML_MATCHES = "AML_Matches.jrxml";
    private final String ACTIVE_CLIENTS = "Active_Clients_Report.jrxml";
    private final String MONITORING_NEW = "MONITORING_NEW.jrxml";
    private final String MCB_FUNDS = "MCB_Remittance_Disbursement_funds.jrxml";
    private final String MCB_FUNDS_EXCEL = "MCB_Remittance_Disbursement_funds_Excel.jrxml";
    private final String MCB_LOADER = "MCBRemittanceDisbursmentfundsLoader.jrxml";
    private final String MCB_LOADER_EXCEL = "MCBRemittanceDisbursmentfundsLoaderExcel.jrxml";
    private final String MCB_LETTER = "MCBRemittanceDisbursmentLetter.jrxml";
    private final String MCB_LETTER_EXCEL = "MCBRemittanceDisbursmentLetterExcel.jrxml";
    private final String CHEQUE_DISBURSMENT = "Cheques_Disbursment_Funds.jrxml";
    private final String CHEQUE_DISBURSMENT_EXCEL = "Cheques_Disbursment_Funds_Excel.jrxml";
    private final String EASYPAISA_FUNDS = "EasyPaisa_Mobile_Wallet_funds.jrxml";
    private final String EASYPAISA_FUNDS_EXCEL = "EasyPaisa_Mobile_Wallet_funds_Excel.jrxml";
    private final String EASYPAISA_LOADER = "EasyPaisa_Mobile_Wallet_funds_Loader.jrxml";
    private final String EASYPAISA_LOADER_EXCEL = "EasyPaisa_funds_Loader_Excel.jrxml";
    private final String EASYPAISA_LETTER = "EasyPaisa_Mobile_Wallet_funds_Letter.jrxml";
    private final String EASYPAISA_LETTER_EXCEL = "EasyPaisa_Mobile_Wallet_Letter_Excel.jrxml";
    private final String JAZZ_CASH_INFO_DUES = "Jazz_Cash-info_dues.jrxml";
    private final String JAZZ_CASH_INFO_DUES_EXCEL = "Jazz_Cash-info_dues_Excel.jrxml";
    private final String JAZZ_CASH_UPLOAD_DUES = "Jazz_Cash_dues_upload.jrxml";
    private final String JAZZ_CASH_UPLOAD_DUES_EXCEL = "Jazz_Cash_dues_upload_Excel.jrxml";
    private final String EASY_PAISA_DUES = "EasyPaisaDues.jrxml";
    private final String EASY_PAISA_DUES_EXCEL = "EasyPaisaDues_Excel.jrxml";
    private final String UBL_OMNI_DUES = "UBLOmniDues.jrxml";
    private final String UBL_OMNI_DUES_EXCEL = "UBLOmniDues_Excel.jrxml";
    private final String HBL_CONNECT_DUES = "HBLConnectDues.jrxml";
    private final String HBL_CONNECT_DUES_EXCEL = "HBLConnectDues_Excel.jrxml";
    private final String ABL_DUES = "ABLDues.jrxml";
    private final String ABL_DUES_EXCEL = "ABLDues_Excel.jrxml";
    private final String MCB_COLLECT_DUES = "MCBCollectDues.jrxml";
    private final String MCB_COLLECT_DUES_EXCEL = "MCBCollectDues_Excel.jrxml";
    private final String NADRA_DUES = "NadraDues.jrxml";
    private final String NADRA_DUES_EXCEL = "NadraDues_Excel.jrxml";
    private final String NACTA_MANAGMENT = "NACTA_MANAGMENT.jrxml";
    private final String NACTA_MATCH = "NACTA_MATCH.jrxml";
    private final String VERISYS_REPORT = "VERISYS_REPORT.jrxml";
    private final String DATE_WISE_DISB = "Date_Wise_Disbursment.jrxml";
    private final String CHANNEL_WISE_DISB = "Channel_Wise_Disbursment.jrxml";
    private final String BRANCH_WISE_DISB = "Branch_Wise_Disbursment.jrxml";
    private final String PRODUCT_WISE_DISB = "Product_Wise_Disbursment.jrxml";
    private final String BRANCH_WISE_REVERSAL = "Branch_Wise_Reversal.jrxml";
    private final String DATE_WISE_RECOVERY = "Date_Wise_Recovery.jrxml";
    private final String CHANNEL_WISE_RECOVERY = "Channel_Wise_Recovery.jrxml";
    private final String BRANCH_WISE_RECOVERY = "Branch_Wise_Recovery.jrxml";
    private final String ADCs_POSTED_RECOVERY = "Daily_ADCs_Recovery_Posted.jrxml";
    private final String MCB_REMI_MAPPED_BRANCHES = "MCB_Remi_Mapped_Brnch.jrxml";
    private final String BANK_BRANCHES_MAPPED = "Bank_Brnchs_Mapped.jrxml";
    private final String UBL_OMNI_MAPPED = "Ubl_Omni_Mapped_Brnch.jrxml";
    private final String MOBILE_WALLET_MAPPED = "Mobile-Wallet-Mapped.jrxml";
    private final String ANIMAL_MISSING_TAG = "Animal_Missing_Tag.jrxml";
    private final String DATE_WISE_DISB_EXCEL = "Date_Wise_Disbursment_Excel.jrxml";
    private final String CHANNEL_WISE_DISB_EXCEL = "Channel_Wise_Disbursment_Excel.jrxml";
    private final String BRANCH_WISE_DISB_EXCEL = "Branch_Wise_Disbursment_Excel.jrxml";
    private final String PRODUCT_WISE_DISB_EXCEL = "Product_Wise_Disbursment_Excel.jrxml";
    private final String BRANCH_WISE_REVERSAL_EXCEL = "Branch_Wise_Reversal_Excel.jrxml";
    private final String DATE_WISE_RECOVERY_EXCEL = "Date_Wise_Recovery_Excel.jrxml";
    private final String CHANNEL_WISE_RECOVERY_EXCEL = "Channel_Wise_Recovery_Excel.jrxml";
    private final String BRANCH_WISE_RECOVERY_EXCEL = "Branch_Wise_Recovery_Excel.jrxml";
    private final String ADCs_POSTED_RECOVERY_EXCEL = "Daily_ADCs_Recovery_Posted_Excel.jrxml";
    private final String MCB_REMI_MAPPED_BRANCHES_EXCEL = "MCB_Remi_Mapped_Brnch_Excel.jrxml";
    private final String BANK_BRANCHES_MAPPED_EXCEL = "Bank_Brnchs_Mapped_Excel.jrxml";
    private final String UBL_OMNI_MAPPED_EXCEL = "Ubl_Omni_Mapped_Brnch_Excel.jrxml";
    private final String MOBILE_WALLET_MAPPED_EXCEL = "Mobile-Wallet-Mapped_Excel.jrxml";
    private final String MONITORING_KM = "MONITORING_KM.jrxml";
    private final String RECOVERY_TREND_ANALYSIS = "Recovery_Trend_Analysis_Report.jrxml";
    private final String VERISYS_REPORT_EXCEL = "VERISYS_REPORT_EXCEL.jrxml";
    // Added By Naveed - Date - 13-10-2021
    // Telenor Collection Report - SCR-EasyPaisa Integration
    private final String TELENOR_COLLECTION = "TELENOR_BANK_MICRO.jrxml";
    private final String TELENOR_COLLECTION_EXCEL = "TELENOR_BANK_MICRO_EXCEL.jrxml";
    // Added By Naveed - Date - 29-10-2021
    // TASDEEQ REPORT
    private final String TASDEEQ_REPORT = "TASDEEQ_REPORT.jrxml";
    // Added By Naveed - Date - 01-11-2021
    // Inquiries Telenor Collection Report - SCR-EasyPaisa Integration
    private final String INQUIRY_TELENOR_COLLECTION = "INQUIRY_TELENOR_BANK_MICRO.jrxml";
    private final String INQUIRY_TELENOR_COLLECTION_EXCEL = "INQUIRY_TELENOR_BANK_MICRO_EXCEL.jrxml";
    private final String SPACE = " ";
    // Ended By Naveed - Date - 04-11-2021
    // Added By Naveed - Date - 23-01-2022
    // SCR- Munsalik Integration
    // Online Collection Channel
    private final String EASY_PAISA = "0001";
    private final String MUNSALIK = "0004";
    private final String UBL_OMNI = "0005";
    private final String HBL = "0007";
    // Ended by Areeba
    // Added and modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // add parameter Mobile Wallet Type
    private final String WALLET_EASY_PAISA = "16228";
    private final String WALLET_JAZZ_CASH = "16229";
    private final String WALLET_UPAISA = "0019";
    private final String WALLET_HBL_KONNECT = "0021";
    private final String ALL_MOBILE_WALLET = "-1";
    // Ended By Naveed - Date - 23-01-2022
    private final String MOBILE_WALLET_FUNDS = "Mobile_Wallet_funds.jrxml";
    private final String MOBILE_WALLET_FUNDS_EXCEL = "Mobile_Wallet_funds_Excel.jrxml";
    // Ended By Naveed - Date - 27-01-2022
    private final String MOBILE_WALLET_LOADER = "Mobile_Wallet_funds_Loader.jrxml";
    private final String MOBILE_WALLET_LOADER_EXCEL = "Mobile_Wallet_funds_Loader_Excel.jrxml";
    private final String MOBILE_WALLET_LETTER = "Mobile_Wallet_funds_Letter.jrxml";
    private final String MOBILE_WALLET_LETTER_EXCEL = "Mobile_Wallet_funds_Letter_Excel.jrxml";
    private final String MOBILE_WALLET_TREND = "MOBILE_WALLET_TREND.jrxml";
    private final String MOBILE_WALLET_TREND_EXCEL = "MOBILE_WALLET_TREND_EXCEL.jrxml";
    private final String UPaisaDues = "UPaisaDues.jrxml";
    private final String UPaisaDues_EXCEL = "UPaisaDues_EXCEL.jrxml";
    private final String ATM_CARDS_MANAGEMENT = "ATM_CARDS_MANAGEMENT.jrxml";
    private final String ATM_CARDS_MANAGEMENT_EXCEL = "ATM_CARDS_MANAGEMENT_EXCEL.jrxml";
    // SCR One Pager For Both (TASDEEQ and DATACHECK)
    private final String ONEPAGER_MFCIB = "ONEPAGER_MFCIB.jrxml";
    /*
     * Added by Naveed - Date - 10-05-2022
     * SCR - MCB Disbursement
     * */
    private final String MCB_DISBURSEMENT_FUNDS_REPORTS = "MCB_DISBURSEMENT_FUNDS_REPORTS.jrxml";
    private final String MCB_DISBURSEMENT_FUNDS_REPORTS_EXCEL = "MCB_DISBURSEMENT_FUNDS_REPORTS_EXCEL.jrxml";
    private final String MCB_DISBURSEMENT_REVERSAL_REQUEST = "MCB_DISBURSEMENT_REVERSAL_REQUEST.jrxml";
    private final String MCB_DISBURSEMENT_REVERSAL_REQUEST_EXCEL = "MCB_DISBURSEMENT_REVERSAL_REQUEST_EXCEL.jrxml";
    private final String NADRA_VERISYS_STATUS = "NADRA_VERISYS_STATUS.jrxml";
    /**
     * @Added, Naveed
     * @Date, 14-06-2022
     * @Description, SCR - systemization Funds Request
     */
    private final String IBFT_IFT_FUNDS_TRANSFER_EXCEL = "IBFT_IFT_FUNDS_TRANSFER_EXCEL.jrxml";
    // End by Naveed
    private final String IBFT_IFT_FUNDS_TRANSFER = "IBFT_IFT_FUNDS_TRANSFER.jrxml";
    private final String CONSOLIDATED_BANK_FUNDS_EXCEL = "CONSOLIDATED_BANK_FUNDS_EXCEL.jrxml";
    private final String CONSOLIDATED_BANK_FUNDS = "CONSOLIDATED_BANK_FUNDS.jrxml";
    private final String BANK_FUNDS_REQUEST_DATA_LOADER_EXCEL = "BANK_FUNDS_REQUEST_DATA_LOADER_EXCEL.jrxml";
    private final String BANK_FUNDS_REQUEST_DATA_LOADER = "BANK_FUNDS_REQUEST_DATA_LOADER.jrxml";
    private final String KHUSHALI_FUNDS_LETTER_EXCEL = "KHUSHALI_FUNDS_LETTER_EXCEL.jrxml";
    private final String KHUSHALI_FUNDS_LETTER = "KHUSHALI_FUNDS_LETTER.jrxml";
    // Ended by Areeba
    private final String SECTOR_WISE_PORTFOLIO = "SECTOR_WISE_PORTFOLIO.jrxml";
    private final String GENDER_WISE_PORTFOLIO = "GENDER_WISE_PORTFOLIO.jrxml";
    //Added by Areeba - 30-09-2022 - Finance Portfolio Reports
    private final String SECTOR_WISE_PORTFOLIO_AND_PAR = "SECTOR_WISE_PORTFOLIO_AND_PAR.jrxml";
    private final String GENDER_AND_AGE_WISE_PORTFOLIO = "GENDER_AND_AGE_WISE_PORTFOLIO.jrxml";
    private final String BRANCH_WISE_PORTFOLIO = "BRANCH_WISE_PORTFOLIO.jrxml";
    private final String BRANCH_WISE_PAR = "BRANCH_WISE_PAR.jrxml";
    private final String PRODUCT_WISE_PORTFOLIO = "PRODUCT_WISE_PORTFOLIO.jrxml";
    private final String PRODUCT_WISE_PAR = "PRODUCT_WISE_PAR.jrxml";
    private final String PROVINCE_WISE_OST_PORTFOLIO = "PROVINCE_WISE_OST_PORTFOLIO.jrxml";
    private final String LOAN_CYCLE_WISE_PORTFOLIO = "LOAN_CYCLE_WISE_PORTFOLIO.jrxml";
    //Added by Areeba - 07-11-2022 - Audit Report
    private final String CLNT_AND_BRNCH_AUDIT_SAMPLING = "CLNT_AND_BRNCH_AUDIT_SAMPLING.jrxml";
    private final File file = null;
    @Autowired
    MwHlthInsrMembRepository mwHlthInsrMembRepository;
    @Autowired
    ReportComponent reportComponent;
    @Autowired
    MwMfcibOthOutsdLoanRepository mwMfcibOthOutsdLoanRepository;
    @Autowired
    MwClntRelRepository mwClntRelRepository;
    @Autowired
    LoanApplicationRepository loanApplicationRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    MwBizAprslRepository mwBizAprslRepository;
    @Autowired
    EntityManager em;
    @Autowired
    ServletContext context;
    @Autowired
    MwRefCdValRepository mwRefCdValRepository;
    @Autowired
    MwLedgerHeadsRepository mwLedgerHeadsRepository;
    Logger logger = LoggerFactory.getLogger(ReportsService.class);
    @Autowired
    private MwStpCnfigValRepository mwStpCnfigValRepository;
//	private final String KMWK_CLIENT_INCOME = "KMWK_CLIENT_INCOME.jrxml";
//	private final String MONTHLY_DUE = "MONTHLY_DUE.jrxml";
//	private final String PORTFOLIO_MATURITY = "PORTFOLIO_MATURITY.jrxml";
    //Ended by Areeba
    @Autowired
    private MwFndsLodrRepository mwFndsLodrRepository;
    //Ended by Areeba
    // Added By Naveed - Date - 13-10-2021
    // Telenor Collection Report - SCR-EasyPaisa Integration
    @Autowired
    private ClientRepository clntRepository;
    @Autowired
    private EnglishNumberToWords englishNumberToWords;

    public static Date getFirstDateOfMonth(String date) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(new SimpleDateFormat("dd-MM-yyyy").parse(date));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static Date getLastDateOfMonth(String date) {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(new SimpleDateFormat("dd-MM-yyyy").parse(date));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public byte[] getRepaymentReport(long loanAppSeq, String currUser) throws IOException {
        MwRefCdVal type = mwRefCdValRepository.findProductTypeByLoanApp(loanAppSeq);
        MwRefCdVal sts = mwRefCdValRepository.findAppTypeByLoanApp(loanAppSeq);

        int vchrTyp = 0;
        if (type.getRefCd().equals(ISLAMIC_TYPE) && sts.getRefCd().equals(APROVED_STATUS)) {
            vchrTyp = 1;
        }

        LoanApplication app = loanApplicationRepository.findByLoanAppSeq(loanAppSeq);

        Query qt = em.createNativeQuery(Queries.TOTAL_RECEIVEABLE_AMOUNT)
                .setParameter("loanAppSeq", loanAppSeq);
        Object[] qtObj = (Object[]) qt.getSingleResult();
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Map<String, Object> params = new HashMap<>();
        params.put("TOTAL_AMT",
                ((qtObj[0] == null) ? 0L : new BigDecimal(qtObj[0].toString()).longValue())
                        + ((qtObj[1] == null) ? 0L : new BigDecimal(qtObj[1].toString()).longValue())
                        + ((qtObj[2] == null) ? 0L : new BigDecimal(qtObj[2].toString()).longValue()));
        params.put("TOT_CHRG_DUE", qtObj[1] == null ? 0L : new BigDecimal(qtObj[1].toString()).longValue());
        params.put("other_chrgs", qtObj[2] == null ? 0L : new BigDecimal(qtObj[2].toString()).longValue());

        String ql;

        ql = readFile(Charset.defaultCharset(), "RepaymentReport.txt");
        Query rs = em.createNativeQuery(ql).setParameter("loanAppSeq", loanAppSeq).setParameter("vtyp", vchrTyp);

        Object[] obj = (Object[]) rs.getSingleResult();
        params.put("PRD_TYP", type.getRefCd());
        params.put("BRNCH_NM", obj[0].toString());
        params.put("bdo", obj[1] == null ? "" : obj[1].toString());
        params.put("PRD_NM", obj[2].toString());
        params.put("CLNT_SEQ", (obj[4] == null) ? "" : obj[4].toString());
        params.put("CLNT_NM", obj[5] == null ? "" : obj[5].toString());
        params.put("FTHR_NM", obj[6] == null ? "" : obj[6].toString());

        params.put("CNIC_NUM", obj[7].toString());

        params.put("APRVD_LOAN_AMT", (obj[8] == null) ? 0L : new BigDecimal(obj[8].toString()).longValue());

        params.put("TOT_DOC", obj[9] == null ? 0L : new BigDecimal(obj[9].toString()).longValue());

        params.put("CHRG_VAL", (double) (new BigDecimal(qtObj[1].toString()).doubleValue()
                / new BigDecimal(qtObj[0].toString()).doubleValue()) * 100);

        params.put("LOAN_CYCL_NUM", (obj[11] == null) ? 0 : new BigDecimal(obj[11].toString()).longValue());

        params.put("disbu_dt", obj[12].toString());
        loanAppSeq = app.getPrntLoanAppSeq();
        Query q = em.createNativeQuery(Queries.REPAYMENT).setParameter("loanAppSeq", loanAppSeq)
                .setParameter("clntseq", obj[13].toString());
        List<Object[]> payments = q.getResultList();

        List<Map<String, ?>> paymentsList = new ArrayList();
        Long kszb = 0L;
        long inst = 0;
        String oldDate = "";
        for (Object[] r : payments) {
            if (!r[0].toString().equals(oldDate)) {
                inst++;
            }
            Map<String, Object> map = new HashMap();
            map.put("inst_num", inst);

            try {
                map.put("due_dt", new SimpleDateFormat("dd-MM-yyyy").format(inputFormat.parse(r[0].toString())));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            map.put("ppal_amt_due", r[1] == null ? 0 : new BigDecimal(r[1].toString()).longValue());
            map.put("tot_chrg_due", r[2] == null ? 0 : new BigDecimal(r[2].toString()).longValue());
            if (r[3] != null) {
                map.put("type", r[3] == null ? "" : r[3].toString());
                map.put("amt", r[4] == null ? 0 : getLongValue(r[4].toString()));
            }
            map.put("out_std", r[5] == null ? 0 : getLongValue(r[5].toString()));

            oldDate = r[0].toString();
            paymentsList.add(map);

        }

        params.put("irr_val", obj[14] == null ? 0d : (new BigDecimal(obj[14].toString()).doubleValue() * 12) * 100);

        params.put("kszb", kszb);
        params.put("dataset", getJRDataSource(paymentsList));
        params.put("dataset1", getJRDataSource(paymentsList));
        params.put("curr_user", currUser);
        return reportComponent.generateReport(REPAYMENT, params, null);
    }

    public byte[] getUndertakingReport(long loanAppSeq, String currUser) throws IOException {

        String q;

        q = readFile(Charset.defaultCharset(), "UNDERTAKING_INFO_QUERY.txt");
        Query rs = em.createNativeQuery(q).setParameter("loanAppSeq", loanAppSeq);

        // Query q = em.createNativeQuery(
        // com.idev4.rds.util.Queries.UNDERTAKING_INFO_QUERY ).setParameter(
        // "loanAppSeq", loanAppSeq );
        Object[] obj = (Object[]) rs.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", currUser);
        params.put("CLNT_NM",
                (obj[1] != null ? obj[1].toString() : "") + SPACE + (obj[2] != null ? obj[2].toString() : ""));
        params.put("FTHR_NM",
                (obj[3] != null ? obj[3].toString() : "") + SPACE + (obj[4] != null ? obj[4].toString() : ""));
        params.put("CLNT_ID", obj[5].toString());
        params.put("CNIC_NUM", obj[6]);
        params.put("BRNCH_NM", obj[8]);
        params.put("AREA_NM", obj[9]);
        params.put("REG_NM", obj[10]);

        String qt;

        qt = readFile(Charset.defaultCharset(), "TOTAL_RECEIVEABLE_AMOUNT.txt");
        Query rset = em.createNativeQuery(qt).setParameter("loanAppSeq", loanAppSeq);

        // Query qt = em.createNativeQuery(
        // com.idev4.rds.util.Queries.TOTAL_RECEIVEABLE_AMOUNT ).setParameter(
        // "loanAppSeq", loanAppSeq );
        Object[] qtObj = (Object[]) rset.getSingleResult();

        MwMfcibOthOutsdLoan loan = new MwMfcibOthOutsdLoan();
        loan.setInstnNm("Kashf Foundation");
        loan.setLoanPrps(
                (obj[12] == null ? "" : obj[12].toString()) + " " + (obj[13] == null ? "" : obj[13].toString()));
        if (new BigDecimal(obj[11].toString()).longValue() == 5153L
                || new BigDecimal(obj[11].toString()).longValue() == 4513L) {
            loan.setLoanPrps("SCHOOL");
        }
        loan.crntOutsdAmt(((qtObj[0] == null) ? 0L : new BigDecimal(qtObj[0].toString()).longValue())
                + ((qtObj[1] == null) ? 0L : new BigDecimal(qtObj[1].toString()).longValue())
                + ((qtObj[2] == null) ? 0L : new BigDecimal(qtObj[2].toString()).longValue()));

        List<MwMfcibOthOutsdLoan> mfcib = new ArrayList();
        mfcib.add(0, loan);
        mfcib.addAll(mwMfcibOthOutsdLoanRepository.findAllByLoanAppSeq(loanAppSeq));

        return reportComponent.generateReport(UDERTAKING_REPORT, params, getJRDataSource(mfcib));
    }

    public byte[] getHealthCardReport(long loanAppSeq, String currUser) throws IOException {
        String q;

        q = readFile(Charset.defaultCharset(), "HLTH_INSR_CARD_QUERY.txt");
        Query rs = em.createNativeQuery(q).setParameter("loanAppSeq", loanAppSeq);

        // Query q = em.createNativeQuery(
        // com.idev4.rds.util.Queries.HLTH_INSR_CARD_QUERY ).setParameter( "loanAppSeq",
        // loanAppSeq );
        Object[] obj = (Object[]) rs.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("CARD_NO", obj[0] == null ? "" : obj[0].toString());
        params.put("CERT_NUM",
                obj[0] == null ? "" : "00" + obj[0].toString().substring(obj[0].toString().length() - 5));
        params.put("PLCY_NUM", obj[0] == null ? "" : obj[0].toString().substring(2, obj[0].toString().length() - 5));
        params.put("CARD_EXPIRY_DT", obj[14]);
        params.put("CUST_NO", obj[2].toString());
        params.put("CLNT_NM",
                (obj[3] == null ? "" : obj[3].toString()) + SPACE + (obj[4] == null ? "" : obj[4].toString()));
        params.put("DOB", obj[5]);
        params.put("CNIC_NUM", obj[9]);
        params.put("BRNCH_NM", obj[11]);
        params.put("MEMB_OF", "KASHF FOUNDATION");
        params.put("MAX_PLCY_AMT", obj[12]);
        if (new BigDecimal(obj[15].toString()).longValue() <= 2019) {
            params.put("ROOM_LIMT",
                    "General Ward" + (obj[13].toString().equals("0001") ? "C-Section Limit : 20,000" : ""));
        } else {
            params.put("ROOM_LIMT",
                    "General Ward" + (obj[13].toString().equals("0001") ? "C-Section Limit : 25,000" : ""));
        }
        List<MwHlthInsrMemb> mem = mwHlthInsrMembRepository.findByLoanAppSeq(loanAppSeq);
        params.put("DATASET", getJRDataSource(mem));
        return reportComponent.generateReport(HEALTHCARD, params, null);
    }

    public byte[] getClientInfoReport(long loanAppSeq, String currUser) throws IOException {
        MwRefCdVal type = mwRefCdValRepository.findProductTypeByLoanApp(loanAppSeq);
        MwRefCdVal sts = mwRefCdValRepository.findAppTypeByLoanApp(loanAppSeq);

        int vchrTyp = 0;
        if (type.getRefCd().equals(ISLAMIC_TYPE) && sts.getRefCd().equals(APROVED_STATUS)) {
            vchrTyp = 1;
        }

        LoanApplication app = loanApplicationRepository.findByLoanAppSeq(loanAppSeq);
        loanAppSeq = app.getPrntLoanAppSeq();

        File file = null;
        String REPORTS_BASEPATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator
                + "reports" + file.separator;
        // String REPORTS_BASEPATH = "D:\\Report\\";
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

        String q;
        q = readFile(Charset.defaultCharset(), "ClientInfoReport.txt");
        Query rs = em.createNativeQuery(q).setParameter("loanAppSeq", loanAppSeq).setParameter("vchrTyp", vchrTyp);

        List<Object[]> results = rs.getResultList();

        Map<String, Object> params = new HashMap<String, Object>();

        params.put("loan_id", results.get(0)[0].toString());
        params.put("prd_seq", results.get(0)[1].toString());
        params.put("prd_nm", results.get(0)[2].toString());
        params.put("loan_cycl_num", results.get(0)[3].toString());
        params.put("rqstd_loan_amt", new BigDecimal(results.get(0)[4].toString()).longValue());
        params.put("aprvd_loan_amt",
                results.get(0)[5] == null ? 0L : new BigDecimal(results.get(0)[5].toString()).longValue());
        params.put("mrtl", results.get(0)[6].toString());
        params.put("occ", results.get(0)[7].toString());
        params.put("res", results.get(0)[8].toString());
        params.put("yrs_res", results.get(0)[9].toString());
        params.put("num_of_dpnd", new BigDecimal(results.get(0)[10].toString()).longValue());
        params.put("num_of_chldrn", new BigDecimal(results.get(0)[11].toString()).longValue());
        params.put("num_of_erng_memb", new BigDecimal(results.get(0)[12].toString()).longValue());
        params.put("frst_nm", results.get(0)[13] == null ? "" : results.get(0)[13].toString());
        params.put("last_nm", results.get(0)[14] == null ? "" : results.get(0)[14].toString());
        params.put("clnt_id", results.get(0)[15] == null ? "" : results.get(0)[15].toString());
        params.put("cnic_num", results.get(0)[16].toString());
        params.put("port_nm", results.get(0)[24].toString());
        params.put("brnch_cd", results.get(0)[18].toString());
        params.put("brnch_nm", results.get(0)[19].toString());
        params.put("area_nm", results.get(0)[20].toString());
        params.put("reg_nm", results.get(0)[21].toString());
        params.put("bdo", results.get(0)[22].toString());
        params.put("mnth_res", results.get(0)[26] == null ? "0" : results.get(0)[26].toString());
        params.put("scrn_flg",
                results.get(0)[27] != null && new BigDecimal(results.get(0)[27].toString()).longValue() == 1L ? "Field"
                        : "Table");
        params.put("biz_dtl", results.get(0)[28] == null ? "" : results.get(0)[28].toString());
        params.put("apprv_dt", results.get(0)[23].toString());
        params.put("disbu_dt", results.get(0)[29].toString());
        params.put("crdt_rsk_ctgry", results.get(0)[30] == null ? "" : results.get(0)[30].toString());
        params.put("cs_flg",
                results.get(0)[31] == null ? 0 : new BigDecimal(results.get(0)[31].toString()).longValue());
        params.put("prev_loan_amt", 0L);
        if (new BigDecimal(results.get(0)[3].toString()).longValue() >= 2) {
            LoanApplication preapp = loanApplicationRepository.findOneByClntSeqAndLoanCyclNum(
                    new BigDecimal(results.get(0)[25].toString()).longValue(),
                    new BigDecimal(results.get(0)[3].toString()).longValue() - 1);
            params.put("prev_loan_amt",
                    (preapp == null) ? 0 : ((preapp.getAprvdLoanAmt() == null) ? 0 : preapp.getAprvdLoanAmt()));
        }
        String qt;
        qt = readFile(Charset.defaultCharset(), "TOTAL_RECEIVEABLE_AMOUNT.txt");
        Query rs1 = em.createNativeQuery(qt).setParameter("loanAppSeq", loanAppSeq);

        // Query qt = em.createNativeQuery(
        // com.idev4.rds.util.Queries.TOTAL_RECEIVEABLE_AMOUNT ).setParameter(
        // "loanAppSeq", loanAppSeq );
        Object[] qtObj = (Object[]) rs1.getSingleResult();

        params.put("li_ttl",
                ((qtObj[0] == null) ? 0L : new BigDecimal(qtObj[0].toString()).longValue())
                        + ((qtObj[1] == null) ? 0L : new BigDecimal(qtObj[1].toString()).longValue())
                        + ((qtObj[2] == null) ? 0L : new BigDecimal(qtObj[2].toString()).longValue()));
        params.put("li_servc", (qtObj[1] == null) ? 0L : new BigDecimal(qtObj[1].toString()).longValue());

        String instQry;
        instQry = readFile(Charset.defaultCharset(), "clientInfoInstQuery.txt");
        Query rs2 = em.createNativeQuery(instQry).setParameter("loanAppSeq", loanAppSeq);

        try {
            List<Object[]> isntList = rs2.getResultList();
            if (isntList != null && isntList.size() > 0) {
                params.put("li_inst_no", isntList.size());
                params.put("li_inst_amt", new BigDecimal(isntList.get(1)[1].toString()).longValue()
                        + new BigDecimal(isntList.get(1)[2].toString()).longValue()
                        + (isntList.get(1)[3] == null ? 0 : new BigDecimal(isntList.get(1)[3].toString()).longValue()));
                try {
                    params.put("li_com_dt", new SimpleDateFormat("dd-MM-yyyy")
                            .format(inputFormat.parse(isntList.get(isntList.size() - 1)[0].toString())));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String kszbQry;
        kszbQry = readFile(Charset.defaultCharset(), "clientInfoKSZBQuery.txt");
        Query rs3 = em.createNativeQuery(kszbQry).setParameter("loanAppSeq", loanAppSeq);

        Object[] kszb = null;
        try {
            kszb = (Object[]) rs3.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.put("li_kszb", kszb == null ? "" : kszb[1].toString());

        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 10L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 11L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 12L) {

            String takfulQry;
            takfulQry = readFile(Charset.defaultCharset(), "ClientInfoTkafulQuery.txt");
            Query rs4 = em.createNativeQuery(takfulQry).setParameter("loanAppSeq", loanAppSeq);

            Object takfulObj = rs4.getSingleResult();
            params.put("takaful", (takfulObj == null) ? 0L : new BigDecimal(takfulObj.toString()).longValue());
        }

        String bizApprQury;
        bizApprQury = readFile(Charset.defaultCharset(), "ClientInfobizApprQury.txt");
        Query rs4 = em.createNativeQuery(bizApprQury).setParameter("loanAppSeq", loanAppSeq);

        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 5153L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 4513L) {

            // String bizApprQury;
            bizApprQury = readFile(Charset.defaultCharset(), "ClientInfobizApprQury2.txt");
            rs4 = em.createNativeQuery(bizApprQury).setParameter("loanAppSeq", loanAppSeq);

        }

        List<Object[]> bizApprList = rs4.getResultList();
        if (bizApprList.size() > 0) {
            params.put("biz_aprsl_seq", bizApprList.get(0)[0].toString());
            params.put("yrs_in_biz", bizApprList.get(0)[1].toString());
            params.put("mnth_in_biz", bizApprList.get(0)[2] == null ? "" : bizApprList.get(0)[2].toString());
            params.put("biz", bizApprList.get(0)[3].toString());
            params.put("prsn", bizApprList.get(0)[4].toString());
            params.put("sect", bizApprList.get(0)[5] == null ? "" : bizApprList.get(0)[5].toString());
            params.put("acty", bizApprList.get(0)[6] == null ? "" : bizApprList.get(0)[6].toString());
            params.put("biz_addr", bizApprList.get(0)[7].toString());
        }

        String bizApprSummry;
        bizApprSummry = readFile(Charset.defaultCharset(), "ClientInfobizAppSummaryQuery.txt");
        rs4 = em.createNativeQuery(bizApprSummry).setParameter("loanAppSeq", loanAppSeq);
        /*
         * Query bizApprSummry = em .createNativeQuery(
         * " select biz_inc,biz_exp,prm_incm hfs_incm,sec_incm,hsld_exp,ndi from vw_loan_app where loan_app_seq=:loanAppSeq"
         * ) .setParameter( "loanAppSeq", loanAppSeq );
         */
        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 5153L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 4513L) {
            /*
             * bizApprSummry = em.createNativeQuery(
             * "select tot_fee,sch_biz_exp,sch_prm_incm,sch_sec_incm,sch_hsld_exp,sch_profit+sch_prm_incm+sch_sec_incm - sch_hsld_exp ndi\r\n"
             * + "from vw_loan_app where loan_app_seq =:loanAppSeq" ) .setParameter(
             * "loanAppSeq", loanAppSeq );
             */
            bizApprSummry = readFile(Charset.defaultCharset(), "ClientInfobizAppSummaryQuery2.txt");
            rs4 = em.createNativeQuery(bizApprSummry).setParameter("loanAppSeq", loanAppSeq);
        }

        List<Object[]> bizApprSummryList = rs4.getResultList();
        if (bizApprSummryList != null && bizApprSummryList.get(0)[0] != null) {
            long prfit = new BigDecimal(bizApprSummryList.get(0)[0].toString()).longValue()
                    - new BigDecimal(bizApprSummryList.get(0)[1].toString()).longValue();

            params.put("busi_incm", new BigDecimal(bizApprSummryList.get(0)[0].toString()).longValue());
            params.put("busi_exp", new BigDecimal(bizApprSummryList.get(0)[1].toString()).longValue());
            params.put("busi_prft", prfit);
            params.put("hfs_incm", new BigDecimal(bizApprSummryList.get(0)[2].toString()).longValue());
            params.put("ttl_p_incm", new BigDecimal(bizApprSummryList.get(0)[2].toString()).longValue() + prfit);
            params.put("sec_incm", new BigDecimal(bizApprSummryList.get(0)[3].toString()).longValue());
            params.put("ttl_incm", new BigDecimal(bizApprSummryList.get(0)[3].toString()).longValue()
                    + new BigDecimal(bizApprSummryList.get(0)[2].toString()).longValue() + prfit);
            params.put("hsld_exp", new BigDecimal(bizApprSummryList.get(0)[4].toString()).longValue());
            params.put("ndi", new BigDecimal(bizApprSummryList.get(0)[5].toString()).longValue());
        }

        String bizBreakupQury;
        bizBreakupQury = readFile(Charset.defaultCharset(), "ClientInfobizBreakupQury.txt");
        rs4 = em.createNativeQuery(bizBreakupQury).setParameter("loanAppSeq", loanAppSeq);

        List<Object[]> bizBreakupList = rs4.getResultList();

        List<Map<String, ?>> bizDtlList = new ArrayList();
        bizBreakupList.forEach(d -> {
            Map<String, Object> map = new HashMap();
            map.put("incm_ctgry_key", d[0] == null ? "" : d[0].toString());
            map.put("ref_cd_dscr", d[1] == null ? "" : d[1].toString());
            map.put("incm_amt", d[2] == null ? 0 : new BigDecimal(d[2].toString()).longValue());
            bizDtlList.add(map);
        });
        params.put("bizdtlbreakup", getJRDataSource(bizDtlList));

        params.put("curr_user", currUser);

        // relations

        String clntRelsQury;
        clntRelsQury = readFile(Charset.defaultCharset(), "ClientInfoclntRelsQury.txt");
        rs4 = em.createNativeQuery(clntRelsQury).setParameter("loanAppSeq", loanAppSeq);

        List<Object[]> clntRelsList = rs4.getResultList();

        List<Map<String, ?>> clntRels = new ArrayList();
        boolean san = false;
        boolean spf = false;
        for (Object[] r : clntRelsList) {
            Map<String, String> map = new HashMap();
            map.put("seq", r[0].toString());
            map.put("cnic_num", r[1].toString());
            try {
                map.put("cnic_expry_dt", new SimpleDateFormat("dd-MM-yyyy").format(inputFormat.parse(r[2].toString())));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            map.put("frst_nm", r[3] == null ? "" : r[3].toString());
            map.put("last_nm", r[4] == null ? "" : r[4].toString());
            map.put("ph_num", r[5] == null ? "" : r[5].toString());
            try {
                map.put("dob", new SimpleDateFormat("dd-MM-yyyy").format(inputFormat.parse(r[6].toString())));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            map.put("gndr", r[7].toString());
            map.put("rel_typ", r[8].toString());
            map.put("rel_typ_flg", r[9].toString());
            map.put("rel", r[10].toString());
            map.put("addr", r[11] == null ? "" : r[11].toString());
            map.put("rel_fathr", r[12] == null ? "" : r[12].toString());
            boolean cobAddrSAC = r[15] != null && new BigDecimal(r[15].toString()).longValue() == 1L ? true : false;
            if (!san) {
                san = r[13] != null && new BigDecimal(r[13].toString()).longValue() == 1L ? true : false;
            }
            if (!spf) {
                spf = r[14] != null && new BigDecimal(r[14].toString()).longValue() == 1L ? true : false;
            }
            if (cobAddrSAC || (new BigDecimal(r[9].toString()).longValue() == 1L)) {
                map.put("addr", "Same As Client");
            }
            clntRels.add(map);
        }
        if (san) {
            Map<String, String> map = new HashMap();
            for (Map r : clntRels) {
                if (r.get("rel_typ").equals("Nominee")) {
                    map.put("seq", r.get("seq").toString());
                    map.put("cnic_num", r.get("cnic_num").toString());
                    map.put("cnic_expry_dt", r.get("cnic_expry_dt").toString());
                    map.put("frst_nm", r.get("frst_nm").toString());
                    map.put("last_nm", r.get("last_nm").toString());
                    map.put("ph_num", r.get("ph_num").toString());
                    map.put("dob", r.get("dob").toString());
                    map.put("gndr", r.get("gndr").toString());
                    map.put("rel_typ_flg", "3");
                    map.put("rel", r.get("rel").toString());
                    map.put("addr", "Same as Client");
                    map.put("rel_fathr", r.get("rel_fathr").toString());
                    map.put("rel_typ", "Co-Borrower");
                }
            }
            clntRels.add(map);
        }
        if (spf) {
            Map<String, String> map = new HashMap();
            for (Map r : clntRels) {
                if (r.get("rel_typ").equals("Client")) {
                    map.put("seq", r.get("seq").toString());
                    map.put("cnic_num", r.get("cnic_num").toString());
                    map.put("cnic_expry_dt", r.get("cnic_expry_dt").toString());
                    map.put("frst_nm", r.get("frst_nm").toString());
                    map.put("last_nm", r.get("last_nm").toString());
                    map.put("ph_num", r.get("ph_num").toString());
                    map.put("dob", r.get("dob").toString());
                    map.put("gndr", r.get("gndr").toString());
                    map.put("rel_typ_flg", "0");
                    map.put("rel", r.get("rel").toString());
                    map.put("addr", "Same as Client");
                    map.put("rel_fathr", r.get("rel_fathr").toString());
                    map.put("rel_typ", "Co-Borrower");
                }
            }
            clntRels.add(map);
        }

        String picQry;
        picQry = readFile(Charset.defaultCharset(), "ClientInfopicQry.txt");
        rs4 = em.createNativeQuery(picQry).setParameter("loanAppSeq", loanAppSeq);

        Clob clob = null;

        Clob clob2 = null;
        try {
            List<Object[]> picsObj = rs4.getResultList();
            if (picsObj != null && picsObj.size() > 0) {
                clob = (Clob) (picsObj.size() >= 0 ? picsObj.get(0)[1] : null);
                clob2 = (Clob) (picsObj.size() == 2 ? picsObj.get(1)[1] : null);
            }

        } catch (

                Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        params.put("nominee_img",

                getStringClob(clob2));
        params.put("client_img", getStringClob(clob));

        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 2625L) {
            params.put("charter_img", REPORTS_BASEPATH + "labaf_1.jpg");
            params.put("signature_img", REPORTS_BASEPATH + "labaf_2.jpg");
            return reportComponent.generateReport(LABAF, params, getJRDataSource(clntRels));
        }
        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 10L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 11L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 12L) {
            params.put("charter_img", REPORTS_BASEPATH + "mabaf_1.jpg");
            params.put("signature_img", REPORTS_BASEPATH + "mabaf_2.jpg");

            return reportComponent.generateReport(MABAF, params, getJRDataSource(clntRels));
        }

        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 5153L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 4513L) {
            params.put("charter_img", REPORTS_BASEPATH + "slabaf_1.jpg");
            params.put("signature_img", REPORTS_BASEPATH + "slabaf_2.jpg");
            return reportComponent.generateReport(SLABAF, params, getJRDataSource(clntRels));
        }

        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 2657L
                || new BigDecimal(params.get("prd_seq").toString()).longValue() == 2657L) {
            params.put("charter_img", REPORTS_BASEPATH + "laf_1.jpg");
            params.put("signature_img", REPORTS_BASEPATH + "laf_2.jpg");
            return reportComponent.generateReport(LAF, params, getJRDataSource(clntRels));
        }
        if (new BigDecimal(params.get("prd_seq").toString()).longValue() == 16L) {
            params.put("charter_img", REPORTS_BASEPATH + "labaf_1.jpg");
            params.put("signature_img", REPORTS_BASEPATH + "labaf_2.jpg");
            return reportComponent.generateReport(MLABAF, params, getJRDataSource(clntRels));
        }

        return null;

    }

    private String getStringClob(Clob clob) {
        byte[] imgByte = null;
        try {
            imgByte = clob == null ? null : IOUtils.toByteArray(clob.getAsciiStream());
        } catch (IOException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String noImg = "/9j/4AAQSkZJRgABAgAAZABkAAD/2wBDAAYEBAQFBAYFBQYJBgUGCQsIBgYICwwKCgsKCgwQDAwMDAwMEAwODxAPDgwTExQUExMcGxsbHB8fHx8fHx8fHx//2wBDAQcHBw0MDRgQEBgaFREVGh8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx8fHx//wgARCADIAMgDAREAAhEBAxEB/8QAFwABAQEBAAAAAAAAAAAAAAAAAAECB//EABUBAQEAAAAAAAAAAAAAAAAAAAAB/9oADAMBAAIQAxAAAAHqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABCgAAAAhQCFAAAAAAAAAIUAAAQFAAAABCgAAAAAAAAAAAAEKAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEKCFAAAAAAAAAAAAAAMmgAAAAAQoAAAAAAAIUAAAAAAAAAAAAAAAAhQAAADBsAAAAAAAAAAAEKAAACFAAIUAAAAAAAAAAAAQoQoAAIUAAAEIaBCgAAAAAAAAEKACFBItAAAAAAAAQoAAAAAAAAAAAAAAAAAAAAAAEKAAAAAhQAAQFAAAAAAAAAABCgAAQoQoAAAABItAACFAAAIUAH/8QAGRABAAMBAQAAAAAAAAAAAAAAARFgcAAw/9oACAEBAAEFArxOBjJYHyWqvA5r/8QAFBEBAAAAAAAAAAAAAAAAAAAAkP/aAAgBAwEBPwEcf//EABQRAQAAAAAAAAAAAAAAAAAAAJD/2gAIAQIBAT8BHH//xAAYEAACAwAAAAAAAAAAAAAAAAABESFwgP/aAAgBAQAGPwLXkl1r/8QAJBAAAgAGAQMFAAAAAAAAAAAAAREQICEwQEExAFBhUWBxgcH/2gAIAQEAAT8hkL7lvAahvIAXGQUK9eLZd4gHnHeA4iucOxAARweOwpG7v5t6jpxeCUKmQ3WVLCglaZVckgFMNVFkkDnc1X+3axq/F0Tmy2uhByI7xKrzKWognfsJwrZPTmJAHX3j+JzDWIDd/9oADAMBAAIAAwAAABASQCQSSQSSSCSSSSSSSSSCSQSSSQSSCSSQSQQSSSCCSQSSCSSSSSQSCQCQCCQSSCTKCSSSRSSCSSSSSSSSSSRSSSCSSSQCCSSSSSSQSSSSCSACQSSCSSSSACSSSSSQSSSSSSSSSSSSSQSSSSSCSSCSSSSSQCCSSQSSSSSACSCQQAASSKSQQCQCQAASSAASCSSCSQBSSAQQSSQACQCCSSBSSCSTAQCSKSQSQSSQCQSQQSCAaSCSJSSSSSSAACSSQCSSSQSSCSSCQCCQBACCCASCQASSSQCQQACSSQQCSRAQSCCQCYASCSQASSSf/8QAFBEBAAAAAAAAAAAAAAAAAAAAkP/aAAgBAwEBPxAcf//EABgRAAMBAQAAAAAAAAAAAAAAAAERYBCA/9oACAECAQE/EOZnZDRKOC//xAAoEAACAQQBBAICAwEBAAAAAAABEQAQITFBUSBhcYGhwTCRsdHx4fD/2gAIAQEAAT8QRZPMc0oH6nB78UzLRcVLVsy9DqaqYSAYf8FO34Lum6gu/QhMS7FvdDL+qK//ALcJAl5/HTe524q7qgLUL176sjiphAGcRQkoES98ddqO63N/dFCV42ZrOYpe/wAQEqaiaepqgoCwLFk+aCgdhnk0+qGCd4MmpANlaLDusVLVsxnGDR3xUITBYElorU30EA56L0JLC/HSCxxXETz0oEaLHYiAbpjqvqrt34guI+k0YmRAxI4gLFXUdaFE+5dvXFCAMFg4I61TdMUVGTjpMVHHaBK0ccN7OF51uYVG6OqL7UBuQkundLv+IQ/6gAAWhFiEOhDH4iUCTYDcJGxMu0tQFhi4OPFczUYdC9VY/BtdCHrpIhsLUCADOST5vDW8AA6kW3biAK0JAAb+lN/gOIOXuAS0eSyDXiKHtGCe4KgsOt1+CxYeIja+4TCId63gE2mlRfNEkscTMKgWtw9FlmWMlWd4+bVGKK75oQNiGO94X6gnkgQgSTJOB5oORgwD4xQwMC5vMk9v1Br5g3LG+o6HFNlZnb9zeIn4mLUWuKCER+ARXRgjYhf9y9TFzeZEUMDAEvZhosVBtEMKBiEBfcHxD2zC9ZhKl91JUYt+47oZy9QlCsy6vNUF70u74109p8TNDiAgt0z0WUZW4gwCkTVWUYBWzXzLKzeTDYOiS0kDqhMZgXi3NWniKDE3L1L1HV0d+9HbEOjwcTR90LlyO8HBnmAgMe3BGEHgiGeJqB7gofmWxuDEHfqzQl+Dmi3F+poKihBVs6hPzT5ivQUAC+JYQFhwGrvO9MQD/Ohx7obOmqHFXehDExEP3QkAM24lxfUJs0YAIcGBDgAu74oAiZuNk9pgOdxGd4oJICJCLKVx7iMjJGqq7hAmo8E5sPMIJIWGe/VuBPvQkMTvS0OlASry66iQr4oELMC0IBTDgH/YaK7oBItmEGJAOPqXwDLq2YOY4v3QvVBiZ+oXbzc0ZiFz8TeYBzdbhamrQi2YwnkTdLxx3o4QyOBQm6r/AP/Z";

        return imgByte == null ? noImg : new String(imgByte);
    }

    public byte[] getPostedReport(String date, String userId, long branch) throws IOException {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());

        String openBalQury;
        openBalQury = readFile(Charset.defaultCharset(), "PostedReportopenBalQury.txt");
        Query rs = em.createNativeQuery(openBalQury).setParameter("date", date).setParameter("branch",
                obj[4].toString());

        Object[] openBal = (Object[]) rs.getSingleResult();

        Long ttlOpnBacnk = new BigDecimal(openBal[1].toString()).longValue();

        Long totlOpnCash = new BigDecimal(openBal[0].toString()).longValue();

        String closeBalQury;
        closeBalQury = readFile(Charset.defaultCharset(), "PostedReportcloseBalQury.txt");
        Query rs1 = em.createNativeQuery(closeBalQury).setParameter("date", date).setParameter("branch",
                obj[4].toString());

        Object[] closeBal = (Object[]) rs1.getSingleResult();

        Long ttlClsBank = new BigDecimal(closeBal[1].toString()).longValue();

        Long ttlClsCash = new BigDecimal(closeBal[0].toString()).longValue();

        params.put("curr_user", userId);
        try {
            Date date1 = new SimpleDateFormat("MM-dd-yyyy").parse(date);
            params.put("date", new SimpleDateFormat("dd-MM-yyyy").format(date1));
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        params.put("open_bank", ttlOpnBacnk);
        params.put("open_cash", totlOpnCash);
        params.put("close_bank", ttlClsBank);
        params.put("close_cash", ttlClsCash);

        String qt;
        qt = readFile(Charset.defaultCharset(), "PostedReportqt.txt");
        Query rs2 = em.createNativeQuery(qt).setParameter("userId", userId).setParameter("reportdate", date)
                .setParameter("branch", branch);

        List<Object[]> recvrys = rs2.getResultList();
        List<Map<String, ?>> recvrysList = new ArrayList();
        recvrys.forEach(r -> {
            Map<String, Object> map = new HashMap();
            map.put("rcvry_trx_seq", r[0] == null ? 0 : new BigDecimal(r[0].toString()).longValue());
            map.put("clnt_id", r[1].toString());
            map.put("name", r[2].toString());
            map.put("instr_num", r[3] == null ? "" : r[3].toString());
            map.put("pymt_amt", r[4] == null ? 0 : new BigDecimal(r[4].toString()).longValue());
            map.put("typ_str", r[5].toString());
            map.put("prd_nm", r[6].toString());
            map.put("ref_cd", r[7].toString());
            map.put("emp_nm", r[8] == null ? "" : r[8].toString());
            map.put("upd_dt", r[9].toString());
            map.put("due_dt", r[10].toString());
            recvrysList.add(map);

        });

        // params.put( "dataset", getJRDataSource( recvrysList ) );

        String des;
        des = readFile(Charset.defaultCharset(), "PostedReportdes.txt");
        Query rs3 = em.createNativeQuery(des).setParameter("brnch", obj[3].toString()).setParameter("reportdate", date);

        List<Object[]> disbursments = rs3.getResultList();
        List<Map<String, ?>> disbList = new ArrayList();
        disbursments.forEach(d -> {
            Map<String, Object> map = new HashMap();
            map.put("prd_nm", d[0].toString());
            map.put("clnt_id", d[1].toString());
            map.put("name", d[2].toString());
            map.put("spz", (d[3] == null ? "" : d[3].toString()) + " " + (d[4] == null ? "" : d[4].toString()));
            map.put("aprvd_loan_amt", d[5] == null ? 0 : new BigDecimal(d[5].toString()).longValue());
            map.put("emp_nm", d[6].toString());
            map.put("typ_str", d[7].toString());
            map.put("ref_cd", d[8].toString());
            map.put("dsbmt_dt", d[9].toString());
            map.put("instr_num", d[10] == null ? "" : d[10].toString());
            disbList.add(map);

        });

        params.put("datasetdis", getJRDataSource(disbList));

        String expQry;
        expQry = readFile(Charset.defaultCharset(), "PostedReportexpQry.txt");
        Query rs4 = em.createNativeQuery(expQry).setParameter("branch", branch).setParameter("reportdate", date);

        List<Object[]> expenses = new ArrayList();
        try {
            expenses = rs4.getResultList();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        List<Map<String, ?>> expList = new ArrayList();
        expenses.forEach(d -> {
            Map<String, Object> map = new HashMap();
            map.put("PYMT_MOD", d[0].toString());
            map.put("CLNT_ID", d[1] == null ? "" : d[1].toString());
            map.put("LGR_ACTY", d[2] == null ? "" : d[2].toString());
            map.put("EXPNS_DSCR", d[3] == null ? "" : d[3].toString());
            map.put("INSTR_NUM", d[4] == null ? "" : d[4].toString());
            map.put("PYMTS", d[5] == null ? 0 : new BigDecimal(d[5].toString()).longValue());
            map.put("RCPTS", d[6] == null ? 0 : new BigDecimal(d[6].toString()).longValue());
            map.put("EXP_SEQ", d[7] == null ? 0 : new BigDecimal(d[7].toString()).longValue());
            expList.add(map);
        });
        params.put("expdataset", getJRDataSource(expList));

        String adjQry;
        adjQry = readFile(Charset.defaultCharset(), "PostedReportadjQry.txt");
        Query rs5 = em.createNativeQuery(adjQry).setParameter("userId", userId).setParameter("reportdate", date)
                .setParameter("branch", branch);

        List<Object[]> adjRec = new ArrayList();
        try {
            adjRec = rs5.getResultList();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        List<Map<String, ?>> adjList = new ArrayList();
        adjRec.forEach(r -> {
            Map<String, Object> map = new HashMap();
            map.put("RCVRY_TRX_SEQ", r[0] == null ? 0 : new BigDecimal(r[0].toString()).longValue());
            map.put("CLNT_ID", r[1].toString());
            map.put("NAME", r[2].toString());
            map.put("INSTR_NUM", r[3] == null ? "" : r[3].toString());
            map.put("PYMT_AMT", r[4] == null ? 0 : new BigDecimal(r[4].toString()).longValue());
            map.put("TYP_STR", r[5].toString());
            map.put("PRD_CMNT", r[6].toString());
            map.put("EMP_NM", r[7].toString());
            map.put("UPD_DT", r[8].toString());
            map.put("JV_HDR_SEQ", r[9].toString());
            adjList.add(map);
        });
        params.put("adjdataset", getJRDataSource(adjList));

        File file = null;
        String REPORTS_BASEPATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator
                + "reports" + file.separator;
        params.put("POSTING_SUB", REPORTS_BASEPATH + "POSTING_SUB.jasper");
        return reportComponent.generateReport(POSTING, params, getJRDataSource(recvrysList));
    }

    public byte[] getOverdueLoansReport(long prdSeq, String asDt, String userId, long branch) throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        Query prdQ = em.createNativeQuery("select PRD_GRP_NM from mw_prd_grp grp where grp.PRD_GRP_SEQ=:prdSeq")
                .setParameter("prdSeq", prdSeq);
        String prd = (String) prdQ.getSingleResult();

        params.put("prd", prd);
        params.put("date", asDt);

        String q;
        q = readFile(Charset.defaultCharset(), "OVER_DUE_LOANS.txt");
        Query rs5 = em.createNativeQuery(q).setParameter("brnch", obj[4].toString()).setParameter("prdSeq", prdSeq)
                .setParameter("aDt", asDt);

        List<Object[]> overdues = rs5.getResultList();

        List<Map<String, ?>> paymentsList = new ArrayList();
        for (Object[] r : overdues) {
            Map<String, Object> map = new HashMap();
            map.put("PORT_NM", r[1].toString());
            map.put("CLNT_ID", r[2].toString());
            map.put("FRST_NM", r[3].toString());
            map.put("FS_FRST_NM", r[4] == null ? "" : r[4].toString());
            map.put("PH_NUM", r[5] == null ? "" : r[5].toString());
            map.put("HSE_NUM", r[6] == null ? "" : r[6].toString());
            map.put("DSBMT_DT", r[7] == null ? "" : getFormaedDate(r[7].toString()));
            map.put("APRVD_LOAN_AMT", r[8] == null ? 0 : new BigDecimal(r[8].toString()).longValue());
            map.put("OST_INST", r[9] == null ? 0 : new BigDecimal(r[9].toString()).longValue());
            map.put("OST_AMT", r[10] == null ? 0 : new BigDecimal(r[10].toString()).longValue());
            map.put("OST_CHRG_AMT", r[11] == null ? 0 : new BigDecimal(r[11].toString()).longValue());
            map.put("OD_INST", r[12] == null ? 0 : new BigDecimal(r[12].toString()).longValue());
            map.put("OD_AMT", r[13] == null ? 0 : new BigDecimal(r[13].toString()).longValue());
            map.put("OD_DAYS", r[14] == null ? 0 : new BigDecimal(r[14].toString()).longValue());
            map.put("LST_REC_DT", r[15] == null ? "" : getFormaedDate(r[15].toString()));
            map.put("RSN", r[16] == null ? "" : r[16].toString());
            map.put("CMNT", r[17] == null ? "" : r[17].toString());
            paymentsList.add(map);
        }

        return reportComponent.generateReport(OVERDUELOANS, params, getJRDataSource(paymentsList));
    }

    public byte[] getPortfolioReport(String fromDt, String toDt, String userId, long branch) throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", fromDt);
        params.put("to_dt", toDt);

        Date frmDt = null;
        Date tooDt = null;
        try {
            frmDt = new SimpleDateFormat("dd-MM-yyyy").parse(fromDt);
            tooDt = new SimpleDateFormat("dd-MM-yyyy").parse(toDt);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fromDt = new SimpleDateFormat("dd-MMM-yyyy").format(frmDt);
        toDt = new SimpleDateFormat("dd-MMM-yyyy").format(tooDt);
        String q;
        q = readFile(Charset.defaultCharset(), "PORTFOLIO_MONITORING.txt");
        Query rs5 = em.createNativeQuery(q).setParameter("frmdt", fromDt).setParameter("todt", toDt)
                .setParameter("branch", new BigDecimal(obj[3].toString()).longValue());
        List<Object[]> overdues = rs5.getResultList();

        // Query q = em.createNativeQuery(
        // com.idev4.rds.util.Queries.PORTFOLIO_MONITORING ).setParameter( "frmdt",
        // fromDt )
        // .setParameter( "todt", toDt ).setParameter( "branch", new BigDecimal( obj[ 3
        // ].toString() ).longValue() );
        // List< Object[] > overdues = rs5.getResultList();

        List<Map<String, ?>> paymentsList = new ArrayList();
        for (Object[] r : overdues) {
            Map<String, Object> map = new HashMap();
            if (r[4] != null && new BigDecimal(r[4].toString()).longValue() > 0) {
                map.put("PRD_NM", r[0].toString());
                map.put("PORT_NM", r[1].toString());
                map.put("PORT_HANDEL_DT", getFormaedDate(r[2].toString()));
                map.put("OST_CLNTS", r[3] == null ? 0 : new BigDecimal(r[3].toString()).longValue());
                map.put("OST_AMT", r[4] == null ? 0 : new BigDecimal(r[4].toString()).longValue());
                map.put("PERIOD_DUE_CLNTS", r[5] == null ? 0 : new BigDecimal(r[5].toString()).longValue());
                map.put("PERIOD_DUE_AMT", r[6] == null ? 0 : new BigDecimal(r[6].toString()).longValue());
                map.put("PERIOD_PAID_CLNTS", r[7] == null ? 0 : new BigDecimal(r[7].toString()).longValue());
                map.put("PERIOD_PAID_AMT", r[8] == null ? 0 : new BigDecimal(r[8].toString()).longValue());
                map.put("TRGT_CLNTS", r[9] == null ? 0 : new BigDecimal(r[9].toString()).longValue());
                map.put("ACHIV_CLNTS", r[10] == null ? 0 : new BigDecimal(r[10].toString()).longValue());
                map.put("TRGT_AMT", r[11] == null ? 0 : new BigDecimal(r[11].toString()).longValue());
                map.put("ACHIV_AMT", r[12] == null ? 0 : new BigDecimal(r[12].toString()).longValue());
                map.put("OD_LOANS", r[13] == null ? 0 : new BigDecimal(r[13].toString()).longValue());
                map.put("OD_AMT", r[14] == null ? 0 : new BigDecimal(r[14].toString()).longValue());
                map.put("DAY_1_OD_CLNTS", r[15] == null ? 0 : new BigDecimal(r[15].toString()).longValue());
                map.put("DAY_1_OD_AMT", r[16] == null ? 0 : new BigDecimal(r[16].toString()).longValue());
                map.put("DAY_30_OD_CLNTS", r[17] == null ? 0 : new BigDecimal(r[17].toString()).longValue());
                map.put("DAY_30_OD_AMT", r[18] == null ? 0 : new BigDecimal(r[18].toString()).longValue());
                map.put("CMPLTD_CLNTS", r[19] == null ? 0 : new BigDecimal(r[19].toString()).longValue());
                map.put("BP_OD_CLNTS", r[20] == null ? 0 : new BigDecimal(r[20].toString()).longValue());
                map.put("BP_OD_AMT", r[21] == null ? 0 : new BigDecimal(r[21].toString()).longValue());

                paymentsList.add(map);
            }
        }

        return reportComponent.generateReport(MONITORING, params, getJRDataSource(paymentsList));
    }

    public byte[] getFundStatmentReport(String fromDt, String toDt, long brnchSeq, String userId) throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);

        String loanProtfolio;
        loanProtfolio = readFile(Charset.defaultCharset(), "FUND_STATMENT_HEADER.txt");
        Query rs5 = em.createNativeQuery(loanProtfolio).setParameter("frmdt", fromDt).setParameter("todt", toDt)
                .setParameter("branch", obj[4].toString());
        // Query loanProtfolio = em.createNativeQuery(
        // com.idev4.rds.util.Queries.FUND_STATMENT_HEADER ).setParameter( "frmdt",
        // fromDt )
        // .setParameter( "todt", toDt ).setParameter( "branch", obj[ 4 ].toString() );
        List<Object[]> loanPrts = rs5.getResultList();
        List<Map<String, ?>> loanPrtss = new ArrayList();
        loanPrts.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
            map.put("DSB_LOANS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DSB_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("REC", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("DIP", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("IP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DDOC", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("DOC", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("DTF", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("TF", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("DKSZB", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("KSZB", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("FC", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("LA", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            loanPrtss.add(map);
        });
        params.put("dataset", getJRDataSource(loanPrtss));
        try {
            params.put("from_dt",
                    new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("dd-MMM-yyyy").parse(fromDt)));
            params.put("to_dt",
                    new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("dd-MMM-yyyy").parse(toDt)));

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String fundStatHead1;
        fundStatHead1 = readFile(Charset.defaultCharset(), "FUND_STATMENT_HEADER1.txt");
        Query resultset = em.createNativeQuery(fundStatHead1).setParameter("frmdt", fromDt).setParameter("todt", toDt)
                .setParameter("branch", obj[4].toString());
        // Query loanProtfolio = em.createNativeQuery(
        // com.idev4.rds.util.Queries.FUND_STATMENT_HEADER ).setParameter( "frmdt",
        // fromDt )
        // .setParameter( "todt", toDt ).setParameter( "branch", obj[ 4 ].toString() );
        List<Object[]> fundStat = resultset.getResultList();
        List<Map<String, ?>> fundStats = new ArrayList();
        fundStat.forEach(l -> {
            Map<String, Object> mapp = new HashMap();
            mapp.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
            mapp.put("DSB_LOANS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            mapp.put("DSB_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            mapp.put("REC", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            mapp.put("DIP", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            mapp.put("IP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            mapp.put("DDOC", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            mapp.put("DOC", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            mapp.put("DTF", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            mapp.put("TF", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            mapp.put("DKSZB", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            mapp.put("KSZB", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            mapp.put("FC", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            mapp.put("LA", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            fundStats.add(mapp);
        });
        params.put("datasett", getJRDataSource(fundStats));
        try {
            params.put("from_dt",
                    new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("dd-MMM-yyyy").parse(fromDt)));
            params.put("to_dt",
                    new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("dd-MMM-yyyy").parse(toDt)));

        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String expnsQuery;
        expnsQuery = readFile(Charset.defaultCharset(), "FUND_STATMENT_REPORT.txt");
        Query rs = em.createNativeQuery(expnsQuery).setParameter("frmDt", fromDt).setParameter("toDt", toDt)
                .setParameter("brnch", obj[4].toString());

        // Query expnsQuery = em.createNativeQuery(
        // com.idev4.rds.util.Queries.FUND_STATMENT_REPORT ).setParameter( "frmDt",
        // fromDt )
        // .setParameter( "toDt", toDt ).setParameter( "brnch", obj[ 4 ].toString() );
        List<Object[]> expnsObj = rs.getResultList();
        List<Map<String, ?>> expns = new ArrayList();
        expnsObj.forEach(e -> {
            Map<String, Object> map = new HashMap();
            map.put("GRP", e[0].toString());
            map.put("PERD_KEY", e[1] == null ? 0 : new BigDecimal(e[1].toString()).longValue());
            map.put("PERD_STRT_DT", e[2].toString());
            map.put("PERD_END_DT", e[3].toString());
            map.put("TYP_STR", e[5].toString());
            map.put("BDGT_AMT", e[6] == null ? 0 : new BigDecimal(e[6].toString()).longValue());
            map.put("CASH_DB_AMT", e[7] == null ? 0 : new BigDecimal(e[7].toString()).longValue());
            map.put("CASH_CR_AMT", e[8] == null ? 0 : new BigDecimal(e[8].toString()).longValue());
            map.put("BNK_DB_AMT", e[9] == null ? 0 : new BigDecimal(e[9].toString()).longValue());
            map.put("BNK_CR_AMT", e[10] == null ? 0 : new BigDecimal(e[10].toString()).longValue());
            expns.add(map);
        });
        return reportComponent.generateReport(FUNDSTATEMENT, params, getJRDataSource(expns));
    }

    public byte[] getInsuClmFrm(long clntSeq, String userId) throws IOException {

        Clients clnt = clientRepository.findOneByClntSeq(clntSeq);

        Query bi = em.createNativeQuery(Queries.USER_BRANCH_INFO).setParameter("portKey",
                clnt.getPortKey());
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);

        String clntinfoQry;
        clntinfoQry = readFile(Charset.defaultCharset(), "InsuClmFrmclntinfoQry.txt");
        Query rs = em.createNativeQuery(clntinfoQry).setParameter("clntSeq", clntSeq);

        Object[] clntObj = (Object[]) rs.getSingleResult();

        params.put("clnt_id", clntObj[0].toString());
        params.put("clnt_nm", clntObj[1].toString());
        params.put("rel_nm", clntObj[7] == null ? "" : clntObj[7].toString());
        params.put("dt_of_dth", getFormaedDate(clntObj[4].toString()));
        params.put("cause_of_dth", clntObj[5].toString());
        int flg = new BigDecimal(clntObj[6].toString()).intValue();
        params.put("des_nm", flg == 0 ? clntObj[1].toString() : clntObj[7].toString());
        String desAg = flg == 0 ? (clntObj[3] == null ? "" : clntObj[3].toString())
                : (clntObj[9] == null ? "" : clntObj[9].toString());
        params.put("des_ag", calculateAge(getLocalDate(desAg), LocalDate.now()));
        params.put("des_cnic", flg == 0 ? (clntObj[2] == null ? "" : clntObj[2].toString())
                : clntObj[8] == null ? "" : clntObj[8].toString());
        params.put("inst_no", clntObj[10] == null ? "" : clntObj[10].toString());
        params.put("pay_dt", getFormaedDate(clntObj[11].toString()));
        params.put("form_title_main", clntObj[13].toString().equals("0010") ? "" : "Credit for Life");
        params.put("form_title", clntObj[13].toString().equals("0010") ? "Takaful Claim Form" : "Insurance Claim Form");
        String loanInfoQry;
        loanInfoQry = readFile(Charset.defaultCharset(), "InsuClmFrmloanInfoQry.txt");
        Query rs1 = em.createNativeQuery(loanInfoQry).setParameter("clntSeq", clntSeq);

        List<Object[]> loanInfo = rs1.getResultList();
        List<Map> loanInfoList = new ArrayList();
        loanInfo.forEach(l -> {
            Map loan = new HashMap();
            loan.put("prd_name", l[0].toString());
            loan.put("dis_dt", getFormaedDate(l[1].toString()));
            loan.put("dis_amt", getLongValue(l[2].toString()));
            loan.put("ag_dt", l[3] == null ? "" : getFormaedDate(l[3].toString()));
            loan.put("ad_amt", l[4] == null ? 0 : getLongValue(l[4].toString()));
            loan.put("rec_amt",
                    (l[2] == null ? 0 : getLongValue(l[2].toString()))
                            + (l[5] == null ? 0 : getLongValue(l[5].toString()))
                            - (l[6] == null ? 0 : getLongValue(l[6].toString())));
            loanInfoList.add(loan);
        });
        params.put("dataset", getJRDataSource(loanInfoList));

        /*
         * params.put( "prd_name", loanInfo.get( 0 )[ 0 ].toString() ); params.put(
         * "dis_dt", getFormaedDate( loanInfo.get( 0 )[ 1 ].toString() ) ); params.put(
         * "dis_amt", getLongValue( loanInfo.get( 0 )[ 2 ].toString() ) );
         */
        String paymentsQry;
        paymentsQry = readFile(Charset.defaultCharset(), "InsuClmFrmpaymentsQry.txt");
        Query rs2 = em.createNativeQuery(paymentsQry).setParameter("clntSeq", clntSeq);

        List<Object[]> pymtsObj = rs2.getResultList();
        List<Map<String, ?>> pymts = new ArrayList();
        Long outSts = 0L;
        long inst = 0;
        String oldDate = "";
        for (Object[] l : pymtsObj) {
            Map<String, Object> map = new HashMap();
            if (!l[0].toString().equals(oldDate)) {
                inst++;
            }
            map.put("INST_NUM", inst);
            map.put("DUE_DT", getFormaedDate(l[0].toString()));
            map.put("PPAL_AMT_DUE", l[1] == null ? 0 : getLongValue(l[1].toString()));
            map.put("TOT_CHRG_DUE", l[2] == null ? 0 : getLongValue(l[2].toString()));
            map.put("AMT", l[3] == null ? 0 : getLongValue(l[3].toString()));
            map.put("PR_REC", l[4] == null ? 0 : getLongValue(l[4].toString()));
            map.put("SC_REC", l[5] == null ? 0 : getLongValue(l[5].toString()));
            map.put("PYMT_DT", l[6] == null ? "" : getFormaedDate(l[6].toString()));

            oldDate = l[0].toString();
            pymts.add(map);

        }

        return reportComponent.generateReport(INSURANCECLAIM, params, getJRDataSource(pymts));
    }

    public byte[] getAccountLedger(AccountLedger accountLedger, String userId) throws IOException {

        Map<String, Object> params = new HashMap<>();
        if (accountLedger.branch != null) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH)
                    .setParameter("brnchSeq", accountLedger.branch);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        }
        MwLedgerHeads account = mwLedgerHeadsRepository.findOneByCustSegments(accountLedger.account);

        params.put("acct_det", account.getCustAccDesc());
        params.put("curr_user", userId);
        params.put("from_dt", new SimpleDateFormat("dd-MM-yyyy").format(accountLedger.frmDt));
        params.put("to_dt", new SimpleDateFormat("dd-MM-yyyy").format(accountLedger.toDt));

        Query q = em.createNativeQuery("select op_blnc_ho(to_date(:FROM_DATE,'MM/dd/yyyy'),:GL_ACC_NUM) bal from dual")
                .setParameter("FROM_DATE", formateTimeZoneDate(accountLedger.frmDt.toString()))
                .setParameter("GL_ACC_NUM", accountLedger.account);

        if (accountLedger.branch != null) {
            q = em.createNativeQuery(
                            "select op_blnc(to_date(:FROM_DATE,'MM/dd/yyyy'),:BRNCH_SEQ,:GL_ACC_NUM) bal from dual")
                    .setParameter("BRNCH_SEQ", accountLedger.branch)
                    .setParameter("FROM_DATE", formateTimeZoneDate(accountLedger.frmDt.toString()))
                    .setParameter("GL_ACC_NUM", accountLedger.account);
        }
        BigDecimal openBal = null;
        try {
            openBal = (BigDecimal) q.getSingleResult();
        } catch (Exception e) {
        }
        long balnce = getLongValue(openBal == null ? "0" : openBal.toString());

        params.put("open_bal", balnce);

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "AccountLedgerdetailQry.txt");
        Query rs2 = em.createNativeQuery(detailQry)
                .setParameter("FROM_DATE", formateTimeZoneDate(accountLedger.frmDt.toString()))
                .setParameter("TO_DATE", formateTimeZoneDate(accountLedger.toDt.toString()))
                .setParameter("GL_ACC_NUM", accountLedger.account);

        if (accountLedger.branch != null) {

            detailQry = readFile(Charset.defaultCharset(), "AccountLedgerdetailQry_2.txt");
            rs2 = em.createNativeQuery(detailQry).setParameter("brnchseq", accountLedger.branch)
                    .setParameter("frmdt", formateTimeZoneDate(accountLedger.frmDt.toString()))
                    .setParameter("todt", formateTimeZoneDate(accountLedger.toDt.toString()))
                    .setParameter("gl_acc_num", accountLedger.account);

        }

        List<Object[]> dtlObj = rs2.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();

        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("VOUCHER_DATE", getFormaedDateShortYear(l[0].toString()));
            map.put("VOUCHER_TYPE", l[1] == null ? "" : l[1].toString());
            map.put("JV_HDR_SEQ", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("ENTY_SEQ", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("DSCR", l[4] == null ? "" : l[4].toString());
            map.put("INSTR_NO", l[5] == null ? "" : l[5].toString());
            map.put("CLNT_INFO_SEQ", l[6] == null ? "" : l[6].toString());
            map.put("CLNT_INFO_NAME", l[7] == null ? "" : l[7].toString());
            map.put("DEBIT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("CREDIT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("CLS_BLNC", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            pymts.add(map);
        });

        String headerQuery;
        headerQuery = readFile(Charset.defaultCharset(), "AccountLedgerHeaderQry.txt");
        Query header = em.createNativeQuery(headerQuery).setParameter("brnchseq", accountLedger.branch)
                .setParameter("frmdt", formateTimeZoneDate(accountLedger.frmDt.toString()))
                .setParameter("todt", formateTimeZoneDate(accountLedger.toDt.toString()))
                .setParameter("gl_acc_num", accountLedger.account);

        List<Object[]> hdrObj = header.getResultList();

        List<Map<String, ?>> hdrRes = new ArrayList();

        hdrObj.forEach(l -> {
            Map<String, Object> hrdmap = new HashMap();
            hrdmap.put("LEDGER_TYPE", l[0] == null ? "" : l[0].toString());
            hrdmap.put("DEBIT", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            hrdmap.put("CREDIT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            hdrRes.add(hrdmap);
        });
        params.put("dataset", getJRDataSource(hdrRes));

        return reportComponent.generateReport(ACCOUNTSLEDGER, params,

                getJRDataSource(pymts));
    }

    public byte[] getKcrReport(long trxSeq, String userId, int type) throws IOException {

        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO).setParameter("userId", userId);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("brnch_nm", obj[2].toString());
        params.put("curr_user", userId);

        String qury;
        qury = readFile(Charset.defaultCharset(), "KcrReportqury.txt");
        // Query rs2 = em.createNativeQuery( qury );

        if (type == 1) {

            qury = readFile(Charset.defaultCharset(), "KcrReportqury_2.txt");

        }

        Query q = em.createNativeQuery(qury).setParameter("TRX_SEQ", trxSeq);

        try {
            List<Object[]> kcrObj = q.getResultList();
            if (kcrObj.size() == 0) {
                return null;
            }
            long amt = 0;
            params.put("JV_HDR_SEQ", kcrObj.get(0)[0].toString());
            params.put("ENTY_TYP", kcrObj.get(0)[1].toString());
            params.put("JV_DSCR", kcrObj.get(0)[2].toString());
            params.put("JV_DT", getFormaedDate(kcrObj.get(0)[3].toString()));
            params.put("INSTR_NUM", kcrObj.get(0)[4] == null ? "" : kcrObj.get(0)[4].toString());
            params.put("CLNT_SEQ", kcrObj.get(0)[5] == null ? "" : kcrObj.get(0)[5].toString());
            params.put("NAME", kcrObj.get(0)[6] == null ? "" : kcrObj.get(0)[6].toString());
            amt = kcrObj.get(0)[7] == null ? 0 : new BigDecimal(kcrObj.get(0)[7].toString()).longValue();
            if (kcrObj.get(0)[8].toString().equals("0424")) {
                amt = 5000 - new BigDecimal(kcrObj.get(0)[7].toString()).longValue();
                params.put("ENTY_TYP", "RECOVERY");
                params.put("JV_DSCR", "RECOVERY");
            }

            params.put("AMT", amt);

            return reportComponent.generateReport(KCR, params, null);

        } catch (Exception e) {

        }
        return null;

    }

    public byte[] getDueRecovery(BookDetailsDTO bookDetailsDTO, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();
        if (bookDetailsDTO.branch != null) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH)
                    .setParameter("brnchSeq", bookDetailsDTO.branch);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        }
        params.put("curr_user", userId);
        params.put("from_dt", new SimpleDateFormat("dd-MM-yyyy").format(bookDetailsDTO.frmDt));
        params.put("to_dt", new SimpleDateFormat("dd-MM-yyyy").format(bookDetailsDTO.toDt));

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "DueRecoverydetailQry.txt");
        Query rs = em.createNativeQuery(detailQry).setParameter("BRNCH_SEQ", bookDetailsDTO.branch)
                .setParameter("BRNCH_SEQ", bookDetailsDTO.branch)
                .setParameter("frmdt", formateTimeZoneDate(bookDetailsDTO.frmDt.toString()))
                .setParameter("todt", formateTimeZoneDate(bookDetailsDTO.toDt.toString()));

        List<Object[]> dtlObj = rs.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();
        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("EMP_NM", l[1] == null ? "" : l[1].toString());
            map.put("CLNT_ID", l[2] == null ? "" : l[2].toString());
            map.put("NAME", l[3] == null ? "" : l[3].toString());
            map.put("SPZ", l[4] == null ? "" : l[4].toString());
            map.put("PH_NUM", l[5] == null ? "" : l[5].toString());
            map.put("ADDR", l[6] == null ? "" : l[6].toString());
            map.put("DUE_DT", getFormaedDate(l[7].toString()));
            map.put("PRD_NM", l[8] == null ? "" : l[8].toString());
            map.put("INST_NUM", l[9] == null ? "" : l[9].toString());
            map.put("AMT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("OD", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            pymts.add(map);
        });
        return reportComponent.generateReport(DUERECOVERY, params,

                getJRDataSource(pymts));
    }

    public byte[] getBookDetails(BookDetailsDTO bookDetailsDTO, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                bookDetailsDTO.branch);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());

        String openBalQury;
        openBalQury = readFile(Charset.defaultCharset(), "BookDetailsopenBalQury.txt");
        Query rs = em.createNativeQuery(openBalQury)
                .setParameter("date", formateTimeZoneDate(bookDetailsDTO.frmDt.toString()))
                .setParameter("branch", obj[4].toString());
        ;

        Object[] openBal = (Object[]) rs.getSingleResult();

        Long opnBnk = new BigDecimal(openBal[1].toString()).longValue();

        Long opnCsh = new BigDecimal(openBal[0].toString()).longValue();

        params.put("type", bookDetailsDTO.type == 1 ? "Bank" : "Cash");
        params.put("curr_user", userId);
        params.put("from_dt", new SimpleDateFormat("dd-MM-yyyy").format(bookDetailsDTO.frmDt));
        params.put("to_dt", new SimpleDateFormat("dd-MM-yyyy").format(bookDetailsDTO.toDt));
        long branchAcc = 0;
        // List< Long > ids = Arrays.asList( 3L, 4L );
        String typId = "0001";
        if (bookDetailsDTO.type == 1) {
            // ids = Arrays.asList( 3L, 4L, 5L );
            typId = "0008";
            branchAcc = bookDetailsDTO.branch;
        }

        // BigDecimal openBal = ( BigDecimal ) q.getSingleResult();

        // long balnce = getLongValue( openBal.toString() );
        params.put("open_bal", opnCsh);
        if (bookDetailsDTO.type == 1) {
            params.put("open_bal", opnBnk);
        }

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "BookDetailsdetailQry.txt");
        Query rs1 = em.createNativeQuery(detailQry).setParameter("typId", typId)
                .setParameter("FROM_DATE", formateTimeZoneDate(bookDetailsDTO.frmDt.toString()))
                .setParameter("TO_DATE", formateTimeZoneDate(bookDetailsDTO.toDt.toString()));

        if (bookDetailsDTO.branch != null) {

            detailQry = readFile(Charset.defaultCharset(), "BookDetailsdetailQry_2.txt");
            rs1 = em.createNativeQuery(detailQry).setParameter("typId", typId).setParameter("typId", typId)
                    .setParameter("branchAcc", branchAcc).setParameter("BRNCH_SEQ", bookDetailsDTO.branch)
                    .setParameter("frmdt", formateTimeZoneDate(bookDetailsDTO.frmDt.toString()))
                    .setParameter("todt", formateTimeZoneDate(bookDetailsDTO.toDt.toString()));
        }

        List<Object[]> dtlObj = rs1.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();
        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("CRTD_DT", getFormaedDate(l[0].toString()));
            map.put("SR_NO", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("CLNT_SEQ", l[2] == null ? "" : l[2].toString());
            map.put("REFERENCE_COL", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("LEDGER_HEAD", l[4] == null ? "" : l[4].toString());
            map.put("INSTR_NUM", l[5] == null ? "" : l[5].toString());
            map.put("NAR_NO", l[6] == null ? "" : l[6].toString());
            map.put("RECIEPT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("PAYMENT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());

            pymts.add(map);
        });

        return reportComponent.generateReport(BOOKDETAILS, params,

                getJRDataSource(pymts));
    }

    public byte[] getKSKLPD(long loanAppSeq, String currUser) throws IOException {

        String ql;
        ql = readFile(Charset.defaultCharset(), "KSKLPDql.txt");
        Query rs = em.createNativeQuery(ql).setParameter("loanAppSeq", loanAppSeq);

        Object[] obj = (Object[]) rs.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("clnt_id", obj[0] == null ? "" : obj[0].toString());
        params.put("clnt_nm",
                ((obj[1] == null) ? "" : obj[1].toString()) + SPACE + ((obj[2] == null) ? "" : obj[2].toString()));
        params.put("spz_nm",
                (obj[3] == null ? "" : obj[3].toString()) + SPACE + (obj[4] == null ? "" : obj[4].toString()));
        params.put("req_amt", obj[13] == null ? 0 : getLongValue(obj[13].toString()));
        params.put("aprv_amt", obj[14] == null ? 0 : getLongValue(obj[14].toString()));
        params.put("area", obj[9] == null ? "" : obj[9].toString());
        params.put("branch", obj[8] == null ? "" : obj[8].toString());
        params.put("bdo", obj[15] == null ? "" : obj[15].toString());
        params.put("curr_user", currUser);
        params.put("title", obj[1].toString().equals("0010") ? "MPD" : "LPD");
        return reportComponent.generateReport(KSK_LPD, params, null);
    }

    public byte[] getWomenParticipation(String date, long branch, String currUser) throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branch);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());

        String ql;
        ql = readFile(Charset.defaultCharset(), "WomenParticipationql.txt");
        Query rs = em.createNativeQuery(ql);

        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REF_CD_DSCR", w[0].toString());
            parm.put("REF_CD", w[1].toString());
            parm.put("CRNT_CLNT_CNT", getLongValue(w[2].toString()));
            parm.put("CRNT_WMN_CNT", getLongValue(w[3].toString()));
            parm.put("PRV_CLNT_CNT", getLongValue(w[4].toString()));
            parm.put("PRV_WMN_CNT", getLongValue(w[5].toString()));
            parm.put("PRT_CLNT_CNT", getLongValue(w[6].toString()));
            parm.put("PRT_WMN_CNT", getLongValue(w[7].toString()));
            expList.add(parm);
        });

        params.put("curr_user", currUser);
        return reportComponent.generateReport(WOMENPARTICIPATION, params, getJRDataSource(expList));
    }

    private JRDataSource getJRDataSource(List<?> list) {
        return new JRBeanCollectionDataSource(list);
    }

    private String getFormaedDate(String input) {
        String date = "";
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try {
            date = new SimpleDateFormat("dd-MM-yyyy").format(inputFormat.parse(input));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    private String getFormaedDateShortYear(String input) {
        String date = "";
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try {
            date = new SimpleDateFormat("dd-MM-yy").format(inputFormat.parse(input));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    private String formateTimeZoneDate(String input) {
        String date = "";
        DateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        try {
            date = new SimpleDateFormat("MM/dd/yyyy").format(inputFormat.parse(input));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;
    }

    private LocalDate getLocalDate(String date) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        return LocalDate.parse(date, formatter);
    }

    public Long calculateAge(LocalDate birthDate, LocalDate currentDate) {
        // validate inputs ...
        return (long) Period.between(birthDate, currentDate).getYears();
    }

    private Long getLongValue(String value) {
        return new BigDecimal(value).longValue();
    }
    ////////// Health Beneficiary report

    public byte[] getClientHealthBeneficiaryReport(String fromDt, String toDt, long brnchSeq, String userId)
            throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);

        String clientBeneficiary;
        clientBeneficiary = readFile(Charset.defaultCharset(), "CLT_HEALTH_BENEFICIARY.txt");
        Query rs = em.createNativeQuery(clientBeneficiary).setParameter("from_dt", fromDt).setParameter("to_dt", toDt)
                .setParameter("brnch_cd", obj[4].toString());

        /*
         * Query clientBeneficiary = em.createNativeQuery(
         * com.idev4.rds.util.Queries.CLT_HEALTH_BENEFICIARY ) .setParameter( "from_dt",
         * fromDt ).setParameter( "to_dt", toDt ).setParameter( "brnch_cd", obj[ 4
         * ].toString() );
         */
        List<Object[]> clientBeneficiaryList = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        clientBeneficiaryList.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("CLNT_SEQ", l[0] == null ? "" : l[0].toString());
            map.put("CLIENT_NAME", l[1] == null ? "" : l[1].toString());
            map.put("HUSBAND_NAME", l[2] == null ? "" : l[2].toString());
            map.put("FATHER_NAME", l[3] == null ? "" : l[3].toString());
            map.put("CNIC_NUM", l[4] == null ? "" : l[4].toString());
            map.put("PLAN", l[5] == null ? "" : l[5].toString());
            map.put("ANL_PREM_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("HLTH_INSR_MEMB_SEQ", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("MEMBER_NM", l[8] == null ? "" : l[8].toString());
            map.put("AGE_AT_INSURACE_TIME", l[9] == null ? "" : l[9].toString());
            map.put("MARITAL_STATUS", l[10] == null ? "" : l[10].toString());
            map.put("RELATION", l[11] == null ? "" : l[11].toString());
            map.put("GENDER", l[12] == null ? "" : l[12].toString());
            map.put("ENROLLMENT_DATE", l[13] == null ? "" : getFormaedDate(l[13].toString()));
            map.put("INSURANCE_DATE", l[14] == null ? "" : getFormaedDate(l[14].toString()));
            map.put("MATURITY_DATE", l[15] == null ? "" : getFormaedDate(l[15].toString()));
            map.put("CARD_NUM", l[16] == null ? "" : l[16].toString());
            map.put("DEATH_DATE", l[17] == null ? "" : getFormaedDate(l[17].toString()));
            reportParams.add(map);
        });
        params.put("dataset", getJRDataSource(reportParams));

        params.put("from_dt", fromDt);

        params.put("to_dt", toDt);
        return reportComponent.generateReport(CLIENT_HEALTH_BENEFICIARY, params, getJRDataSource(reportParams));
    }

    /// Insurance Claim Report

    public byte[] getInsuranceClaimReport(String fromDt, String toDt, long brnchSeq, String userId) throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);

        String clientBeneficiary;
        clientBeneficiary = readFile(Charset.defaultCharset(), "INSURANCE_CLAIM.txt");
        Query rs = em.createNativeQuery(clientBeneficiary).setParameter("from_dt", fromDt).setParameter("to_dt", toDt)
                .setParameter("brnch_cd", obj[4].toString());

        // Query clientBeneficiary = em.createNativeQuery(
        // com.idev4.rds.util.Queries.INSURANCE_CLAIM ).setParameter( "from_dt", fromDt
        // )
        // .setParameter( "to_dt", toDt ).setParameter( "brnch_cd", obj[ 4 ].toString()
        // );
        List<Object[]> clientBeneficiaryList = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        clientBeneficiaryList.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PORT_NM", l[0] == null ? "" : l[0].toString());
            map.put("CLNT_ID", l[1] == null ? "" : l[1].toString());
            map.put("CLNT_NM", l[2] == null ? "" : l[2].toString());
            map.put("NOM_NM", l[3] == null ? "" : l[3].toString());
            map.put("DT_OF_DTH", l[4] == null ? "" : getFormaedDate(l[4].toString()));
            map.put("DECEASED_PERSON", l[5] == null ? "" : l[5].toString());
            map.put("ADJ_DT", l[6] == null ? "" : getFormaedDate(l[6].toString()));
            map.put("PRD_CMNT", l[7] == null ? "" : l[7].toString());
            map.put("DSBMT_DT", l[8] == null ? "" : getFormaedDate(l[8].toString()));
            map.put("DSBMT_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("ADJ_PRNCPL", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("ADJ_SRVC_CHRG", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("INS_CLM_DT", l[12] == null ? "" : getFormaedDate(l[12].toString()));
            map.put("FNRL_CHRG", l[13] == null ? "" : new BigDecimal(l[13].toString()).longValue());
            reportParams.add(map);
        });
        params.put("dataset", getJRDataSource(reportParams));

        try {
            params.put("from_dt",
                    new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("dd-MMM-yyyy").parse(fromDt)));
            params.put("to_dt",
                    new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("dd-MMM-yyyy").parse(toDt)));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return reportComponent.generateReport(INSURANCE_CLAIM, params, getJRDataSource(reportParams));

    }

    /// PAR Branch Wise Report

    public byte[] getParBranchWiseReport(String fromDt, String toDt, long brnchSeq, String userId) throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", fromDt);
        params.put("to_dt", toDt);

        String clientBeneficiary;
        clientBeneficiary = readFile(Charset.defaultCharset(), "PAR_BRNACH_WISE.txt");
        Query rs = em.createNativeQuery(clientBeneficiary).setParameter("fromDt", fromDt).setParameter("toDt", toDt)
                .setParameter("brnch", obj[4].toString());
        // Query clientBeneficiary = em.createNativeQuery(
        // com.idev4.rds.util.Queries.PAR_BRNACH_WISE ).setParameter( "fromDt", fromDt )
        // .setParameter( "toDt", toDt ).setParameter( "brnch", obj[ 4 ].toString() );
        List<Object[]> clientBeneficiaryList = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        clientBeneficiaryList.forEach(l -> {

            Map<String, Object> map = new HashMap();
            map.put("PRD_NM", l[0] == null ? "" : l[0].toString());
            map.put("EMP_NM", l[1] == null ? "" : l[1].toString());
            map.put("PORT_SEQ", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("LOAN_APP_SEQ", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("LOAN_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OST_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("OD_AMT_OP", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("ADDITION", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("RECOVERED", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OD_AMT_CL", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("OD_LOANS_1", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("OST_AMT_1", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("OD_LOANS_30", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OST_AMT_30", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("OD_LOANS_1_5", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("OST_AMT_1_5", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OD_LOANS_6_10", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("OST_AMT_6_10", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("OD_LOANS_11_15", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("OST_AMT_11_15", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("OD_LOANS_16_30", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
            map.put("OST_AMT_16_30", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("OD_LOANS_31_90", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("OST_AMT_31_90", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());
            map.put("OD_LOANS_90_180", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("OST_AMT_91_180", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("OD_LOANS_181_365", l[26] == null ? 0 : new BigDecimal(l[26].toString()).longValue());
            map.put("OST_AMT_181_365", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("OD_LOANS_365", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
            map.put("OST_AMT_365", l[29] == null ? 0 : new BigDecimal(l[29].toString()).longValue());
            map.put("OD_DYS", l[30] == null ? 0 : new BigDecimal(l[30].toString()).longValue());
            reportParams.add(map);
        });
        params.put("dataset", getJRDataSource(reportParams));

        return reportComponent.generateReport(PAR_BRANCHWISE, params, getJRDataSource(reportParams));
    }

    /// Branch Performance Report

    public byte[] getBranchPerformanceReport(String fromDt, String toDt, long brnchSeq, String userId) {

        // Query bi = em.createNativeQuery(
        // com.idev4.rds.util.Queries.BRANCH_INFO_BY_BRANCH ).setParameter( "brnchSeq",
        // brnchSeq );
        // Object[] obj = ( Object[] ) bi.getSingleResult();
        //
        // Map< String, Object > params = new HashMap<>();
        // params.put( "reg_nm", obj[ 0 ].toString() );
        // params.put( "area_nm", obj[ 1 ].toString() );
        // params.put( "brnch_nm", obj[ 2 ].toString() );
        // params.put( "brnch_cd", obj[ 3 ].toString() );
        // params.put( "curr_user", userId );
        //
        // Query clientBeneficiary = em.createNativeQuery(
        // com.idev4.rds.util.Queries.INSURANCE_CLAIM ).setParameter( "from_dt", fromDt
        // )
        // .setParameter( "to_dt", toDt ).setParameter( "brnch_cd", obj[ 4 ].toString()
        // );
        // List< Object[] > clientBeneficiaryList = clientBeneficiary.getResultList();
        // List< Map< String, ? > > reportParams = new ArrayList();
        // clientBeneficiaryList.forEach( l -> {
        // Map< String, Object > map = new HashMap();
        // map.put( "CLNT_SEQ", l[ 0 ] == null ? "" : l[ 0 ].toString() );
        // map.put( "CLIENT_NAME", l[ 1 ] == null ? "" : l[ 1 ].toString() );
        // map.put( "HUSBAND_NAME", l[ 2 ] == null ? "" : l[ 2 ].toString() );
        // map.put( "FATHER_NAME", l[ 3 ] == null ? "" : l[ 3 ].toString() );
        // map.put( "CNIC_NUM", l[ 4 ] == null ? "" : l[ 4 ].toString() );
        // map.put( "PLAN", l[ 5 ] == null ? "" : l[ 5 ].toString() );
        // map.put( "ANL_PREM_AMT", l[ 6 ] == null ? 0 : new BigDecimal( l[ 6
        // ].toString() ).longValue() );
        // map.put( "HLTH_INSR_MEMB_SEQ", l[ 7 ] == null ? 0 : new BigDecimal( l[ 7
        // ].toString() ).longValue() );
        // map.put( "MEMBER_NM", l[ 8 ] == null ? "" : l[ 8 ].toString() );
        // map.put( "AGE_AT_INSURACE_TIME", l[ 9 ] == null ? "" : l[ 9 ].toString() );
        // map.put( "MARITAL_STATUS", l[ 10 ] == null ? "" : l[ 10 ].toString() );
        // map.put( "RELATION", l[ 11 ] == null ? "" : l[ 11 ].toString() );
        // map.put( "GENDER", l[ 12 ] == null ? "" : l[ 12 ].toString() );
        // map.put( "ENROLLMENT_DATE", l[ 13 ] == null ? "" : getFormaedDate( l[ 13
        // ].toString() ) );
        // map.put( "INSURANCE_DATE", l[ 14 ] == null ? "" : getFormaedDate( l[ 14
        // ].toString() ) );
        // map.put( "MATURITY_DATE", l[ 15 ] == null ? "" : getFormaedDate( l[ 15
        // ].toString() ) );
        // map.put( "CARD_NUM", l[ 16 ] == null ? "" : l[ 16 ].toString() );
        // map.put( "DEATH_DATE", l[ 17 ] == null ? "" : getFormaedDate( l[ 17
        // ].toString() ) );
        // reportParams.add( map );
        // } );
        // params.put( "dataset", getJRDataSource( reportParams ) );
        //
        // try {
        // params.put( "from_dt", new SimpleDateFormat( "MM-yyyy" ).format( new
        // SimpleDateFormat( "dd-MMM-yyyy" ).parse( fromDt ) ) );
        //
        // params.put( "to_dt", new SimpleDateFormat( "MM-yyyy" ).format( new
        // SimpleDateFormat( "dd-MMM-yyyy" ).parse( toDt ) ) );
        // } catch ( ParseException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // return reportComponent.generateReport( INSURANCE_CLAIM, params,
        // getJRDataSource( reportParams ) );
        return null;
    }

    public byte[] getBranchTurnoverAnalysisAndPlaningReport(String date, long branch, String currUser)
            throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branch);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("date", date);

        Date frstDt = getFirstDateOfMonth(date);
        Date lastDt = getLastDateOfMonth(date);
        String frstDt1 = new SimpleDateFormat("dd-MMM-yyyy").format(frstDt);
        String lastDT1 = new SimpleDateFormat("dd-MMM-yyyy").format(lastDt);

        String turnObverQury;
        turnObverQury = readFile(Charset.defaultCharset(), "turnObverQury.txt");
        Query rs = em.createNativeQuery(turnObverQury).setParameter("asDt", date).setParameter("branch", branch);

        List<Object[]> turnObverObject = rs.getResultList();

        List<Map<String, ?>> turnObverList = new ArrayList();
        turnObverObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0].toString());
            parm.put("PRD_NM", w[1].toString());
            parm.put("SAMEDAY", getLongValue(w[2].toString()));
            parm.put("DAYS_1_4", getLongValue(w[3].toString()));
            parm.put("DAYS_5_15", getLongValue(w[4].toString()));
            parm.put("DAYS_16_30", getLongValue(w[5].toString()));
            parm.put("ABV30", getLongValue(w[6].toString()));
            turnObverList.add(parm);
        });

        String pndgSummryQury;
        pndgSummryQury = readFile(Charset.defaultCharset(), "pndgSummryQury.txt");
        Query rs1 = em.createNativeQuery(pndgSummryQury).setParameter("asDt", date).setParameter("branch", branch);

        List<Object[]> pndgSummryObject = rs1.getResultList();

        List<Map<String, ?>> pndgSummryList = new ArrayList();
        pndgSummryObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0].toString());
            parm.put("PRD_CMNT", w[1].toString());
            parm.put("DAYS1_7", getLongValue(w[2].toString()));
            parm.put("DAYS8_15", getLongValue(w[3].toString()));
            parm.put("DAYS16_30", getLongValue(w[4].toString()));
            parm.put("DAYS31_60", getLongValue(w[5].toString()));
            pndgSummryList.add(parm);
        });

        String clntLoanMatuProjQury;
        clntLoanMatuProjQury = readFile(Charset.defaultCharset(), "clntLoanMatuProjQury.txt");
        Query rs2 = em.createNativeQuery(clntLoanMatuProjQury).setParameter("asDt", date).setParameter("branch",
                branch);

        List<Object[]> clntLoanMatuProjObject = rs2.getResultList();

        List<Map<String, ?>> clntLoanMatuProjList = new ArrayList();
        clntLoanMatuProjObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0].toString());
            parm.put("PRD_CMNT", w[1].toString());
            parm.put("DAYS1_7", getLongValue(w[2].toString()));
            parm.put("DAYS8_15", getLongValue(w[3].toString()));
            parm.put("DAYS16_30", getLongValue(w[4].toString()));
            parm.put("DAYS31_60", getLongValue(w[5].toString()));
            clntLoanMatuProjList.add(parm);
        });
        params.put("pndgSummryList", getJRDataSource(pndgSummryList));
        params.put("clntLoanMatuProjList", getJRDataSource(clntLoanMatuProjList));
        params.put("curr_user", currUser);
        return reportComponent.generateReport(BTAP, params, getJRDataSource(turnObverList));
    }

    public byte[] getFiveDaysAdvanceRecoveryTrends(String fromDt, String toDt, long brnchSeq, String userId)
            throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("fromDt", fromDt);
        params.put("toDt", toDt);
        params.put("curr_user", userId);

        String trendQury;
        trendQury = readFile(Charset.defaultCharset(), "FIVE_DAYS_ADVANCE_RECOVERY_TRENDS.txt");
        Query rs = em.createNativeQuery(trendQury).setParameter("fromDt", fromDt).setParameter("toDt", toDt)
                .setParameter("brnchSeq", brnchSeq);

        List<Object[]> trendObject = rs.getResultList();

        List<Map<String, ?>> trendsList = new ArrayList();
        trendObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0].toString());
            parm.put("CLNT_ID", w[1].toString());
            parm.put("CLNT_NM", w[2].toString());
            parm.put("DUE_DT", getFormaedDate(w[3].toString()));
            parm.put("PYMT_DT", getFormaedDate(w[4].toString()));
            parm.put("ADV_DAYS", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("PH_NUM", w[6] == null ? "" : w[6].toString());
            trendsList.add(parm);
        });

        return reportComponent.generateReport(FDARTA, params, getJRDataSource(trendsList));

    }

    public byte[] getTopSheet(String fromDt, String toDt, long brnchSeq, long prd, int flg, String userId)
            throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("fromDt", fromDt);
        params.put("toDt", toDt);
        params.put("curr_user", userId);
        params.put("title", flg == 0 ? "Top Sheet Report" : "Tag Top Sheet Report");

        String trendQury;
        trendQury = (flg == 0 ? readFile(Charset.defaultCharset(), "TOP_SHEET.txt")
                : readFile(Charset.defaultCharset(), "TAG_TOPSHEET.txt"));

        Query rs = em.createNativeQuery(trendQury).setParameter("frmdt", fromDt).setParameter("todt", toDt)
                .setParameter("prdseq", prd).setParameter("brnchseq", brnchSeq);

        List<Object[]> trendObject = rs.getResultList();
        List<Map<String, ?>> trendsList = new ArrayList();
        trendObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("PRD_GRP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("EMP_NM", w[1] == null ? "" : w[1].toString());
            parm.put("OPN_CLNT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("OPN_PRN_AMT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("OPN_SVC_AMT", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("DSBMT_CNT", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("DSBMT_PRN_AMT", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            parm.put("DSBMT_SVC_AMT", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("RCVRD_CLNT", w[8] == null ? 0 : new BigDecimal(w[8].toString()).longValue());
            parm.put("RCVRD_PRN_AMT", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("RCVRD_SVC_AMT", w[10] == null ? 0 : new BigDecimal(w[10].toString()).longValue());
            parm.put("ADJ_CLNT", w[11] == null ? 0 : new BigDecimal(w[11].toString()).longValue());
            parm.put("ADJ_PRN_AMT", w[12] == null ? 0 : new BigDecimal(w[12].toString()).longValue());
            parm.put("ADJ_SVC_AMT", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());
            parm.put("CLSNG_CLNT", w[14] == null ? 0 : new BigDecimal(w[14].toString()).longValue());
            parm.put("CLSNG_PRN_AMT", w[15] == null ? 0 : new BigDecimal(w[15].toString()).longValue());
            parm.put("CLSNG_SVC_AMT", w[16] == null ? 0 : new BigDecimal(w[16].toString()).longValue());
            parm.put("CMPLTD_LOANS", w[17] == null ? 0 : new BigDecimal(w[17].toString()).longValue());
            //Added by Areeba - Dated - 25-3-2022
            parm.put("TRF_IN_CLNT", w[18] == null ? 0 : new BigDecimal(w[18].toString()).longValue());
            parm.put("TRF_IN_PRN_AMT", w[19] == null ? 0 : new BigDecimal(w[19].toString()).longValue());
            parm.put("TRF_IN_SVC_AMT", w[20] == null ? 0 : new BigDecimal(w[20].toString()).longValue());
            parm.put("TRF_OUT_CLNT", w[21] == null ? 0 : new BigDecimal(w[21].toString()).longValue());
            parm.put("TRF_OUT_PRN_AMT", w[22] == null ? 0 : new BigDecimal(w[22].toString()).longValue());
            parm.put("TRF_OUT_SVC_AMT", w[23] == null ? 0 : new BigDecimal(w[23].toString()).longValue());
            //Ended by Areeba
            trendsList.add(parm);
        });

        String reversalQuery;

        reversalQuery = (flg == 0 ? readFile(Charset.defaultCharset(), "TopSheetReversal.txt")
                : readFile(Charset.defaultCharset(), "Tag_TopsheetReversal.txt"));
        // reversalQuery = readFile( Charset.defaultCharset(), "TopSheetReversal.txt" );
        Query header = em.createNativeQuery(reversalQuery).setParameter("frmdt", fromDt).setParameter("todt", toDt)
                .setParameter("prdseq", prd).setParameter("brnchseq", brnchSeq);

        List<Object[]> reversalObj = header.getResultList();

        List<Map<String, ?>> rev = new ArrayList();

        reversalObj.forEach(l -> {
            Map<String, Object> hrdmap = new HashMap();
            hrdmap.put("EMP_NM", l[1] == null ? "" : l[1].toString());
            hrdmap.put("DSBMT_CNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            hrdmap.put("DSBMT_PRN_AMT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            hrdmap.put("DSBMT_SVC_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            hrdmap.put("RCVRD_CLNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            hrdmap.put("RCVRD_PRN_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            hrdmap.put("RCVRD_SVC_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            hrdmap.put("ADJ_CLNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            hrdmap.put("ADJ_PRN_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            hrdmap.put("ADJ_SVC_AMT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            rev.add(hrdmap);
        });
        params.put("dataset", getJRDataSource(rev));

        return reportComponent.generateReport(TOPSHEET, params, getJRDataSource(trendsList));

    }

    public byte[] getPDCDetailReport(String fromDt, String toDt, long brnchSeq, String userId) throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("from_dt", fromDt);
        params.put("to_dt", toDt);
        params.put("curr_user", userId);
        String trendQury;
        trendQury = readFile(Charset.defaultCharset(), "PDC_DETAILS.txt");

        Query rs = em.createNativeQuery(trendQury).setParameter("frmdt", fromDt).setParameter("todt", toDt)
                .setParameter("brnchSeq", brnchSeq);

        List<Object[]> trendObject = rs.getResultList();
        List<Map<String, ?>> trendsList = new ArrayList();
        trendObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("CLNT_SEQ", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_NM", w[1] == null ? "" : w[1].toString());
            parm.put("LOAN_APP_SEQ", w[2] == null ? "" : w[2].toString());
            parm.put("PDC_PROVIDER_NM", w[3] == null ? "" : w[3].toString());
            parm.put("BANK_NM", w[4] == null ? "" : w[4].toString());
            parm.put("ACCT_NUM", w[5] == null ? "" : w[5].toString());
            parm.put("PDC1", w[6] == null ? "" : w[6].toString());
            parm.put("PDC2", w[7] == null ? "" : w[7].toString());
            parm.put("PDC3", w[8] == null ? "" : w[8].toString());
            parm.put("PDC4", w[9] == null ? "" : w[9].toString());

            trendsList.add(parm);
        });

        return reportComponent.generateReport(PDCDETAIL, params, getJRDataSource(trendsList));

    }

    public byte[] getRateOfRetentionReport(long brnchSeq, String userId) throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("curr_user", userId);

        String rateOfRetObjQury;
        rateOfRetObjQury = readFile(Charset.defaultCharset(), "rateOfRetObjQury.txt");

        Query rs = em.createNativeQuery(rateOfRetObjQury).setParameter("brnchSeq", brnchSeq);

        List<Object[]> rateOfRetObj = rs.getResultList();
        List<Map<String, ?>> rateOfRetObjList = new ArrayList();
        int c = 1;
        long comRepttd = 0L;
        long comComltd = 0L;
        DecimalFormat df = new DecimalFormat("#.##");
        for (Object[] w : rateOfRetObj) {
            if (c == 1) {
                params.put("from_dt", w[0].toString());
            }
            params.put("to_dt", w[0].toString());
            params.put("MONTH" + (c < 10 ? "0" : "") + c, w[0].toString());
            Map<String, Object> parm = new HashMap<>();
            parm.put("CMPLTD_MNTH", w[0].toString());
            parm.put("MNTH_ORDR", w[1].toString());
            parm.put("CMPLTD_LOANS", new BigDecimal(w[2].toString()).longValue());
            parm.put("MONTH01", new BigDecimal(w[3].toString()).longValue());
            parm.put("MONTH02", new BigDecimal(w[4].toString()).longValue());
            parm.put("MONTH03", new BigDecimal(w[5].toString()).longValue());
            parm.put("MONTH04", new BigDecimal(w[6].toString()).longValue());
            parm.put("MONTH05", new BigDecimal(w[7].toString()).longValue());
            parm.put("MONTH06", new BigDecimal(w[8].toString()).longValue());
            parm.put("MONTH07", new BigDecimal(w[9].toString()).longValue());
            parm.put("MONTH08", new BigDecimal(w[10].toString()).longValue());
            parm.put("MONTH09", new BigDecimal(w[11].toString()).longValue());
            parm.put("MONTH10", new BigDecimal(w[12].toString()).longValue());
            parm.put("MONTH11", new BigDecimal(w[13].toString()).longValue());
            parm.put("MONTH12", new BigDecimal(w[14].toString()).longValue());
            long rptTtl = new BigDecimal(w[3].toString()).longValue() + new BigDecimal(w[4].toString()).longValue()
                    + new BigDecimal(w[5].toString()).longValue() + new BigDecimal(w[6].toString()).longValue()
                    + new BigDecimal(w[7].toString()).longValue() + new BigDecimal(w[8].toString()).longValue()
                    + new BigDecimal(w[9].toString()).longValue() + new BigDecimal(w[10].toString()).longValue()
                    + new BigDecimal(w[11].toString()).longValue() + new BigDecimal(w[12].toString()).longValue()
                    + new BigDecimal(w[13].toString()).longValue() + new BigDecimal(w[14].toString()).longValue();
            comRepttd = comRepttd + rptTtl;
            comComltd = comComltd + new BigDecimal(w[2].toString()).longValue();
            double comRetnRat = ((double) comRepttd / comComltd) * 100;
            parm.put("REPTED_TTL", rptTtl);
            parm.put("COM_REPTD", comRepttd);
            parm.put("COM_COMLTD", comComltd);
            parm.put("COM_RENTNRAT", df.format(comRetnRat));
            rateOfRetObjList.add(parm);
            c++;
        }

        return reportComponent.generateReport(RETENTIONRATE, params, getJRDataSource(rateOfRetObjList));
    }

    public byte[] getProjectedClientLoanCompletion(String fromDt, String toDt, long brnchSeq, String userId)
            throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("curr_user", userId);
        params.put("from_dt", fromDt);
        params.put("to_dt", toDt);

        String projClntLoanQuery;
        projClntLoanQuery = readFile(Charset.defaultCharset(), "PROJECTED_CLIENTS_LOANS_COMPLE.txt");

        Query rs = em.createNativeQuery(projClntLoanQuery).setParameter("frmDt", fromDt).setParameter("toDt", toDt)
                .setParameter("brnch", brnchSeq);

        // Query projClntLoanQuery = em.createNativeQuery(
        // com.idev4.rds.util.Queries.PROJECTED_CLIENTS_LOANS_COMPLE )
        // .setParameter( "frmDt", fromDt ).setParameter( "toDt", toDt ).setParameter(
        // "brnch", brnchSeq );
        List<Object[]> projClntObj = rs.getResultList();
        List<Map<String, ?>> projClntList = new ArrayList();
        projClntObj.forEach(c -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("BDO_NM", c[0] == null ? "" : c[0].toString());
            parm.put("CLNT_ID", c[1] == null ? "" : c[1].toString());
            parm.put("CLNT_NM", c[2] == null ? "" : c[2].toString());
            parm.put("FTHR_SPZ_NM", c[3] == null ? "" : c[3].toString());
            parm.put("CNTCT_NUM", c[4] == null ? "" : c[4].toString());
            parm.put("ADDR", c[5] == null ? "" : c[5].toString());
            parm.put("LOAN_USR", c[6] == null ? "" : c[6].toString());
            parm.put("LOAN_CYCL_NUM", c[7] == null ? "" : c[7].toString());
            parm.put("APRVD_LOAN_AMT", c[8] == null ? 0L : new BigDecimal(c[8].toString()).longValue());
            parm.put("CMP_DT", c[9] == null ? "" : getFormaedDate(c[9].toString()));
            parm.put("PRD", c[10] == null ? "" : c[10].toString());
            projClntList.add(parm);
        });

        return reportComponent.generateReport(PROJECTED_CLIENTS_LOANS_COM, params, getJRDataSource(projClntList));
    }

    public byte[] getRegionMonthWiseADCReport(String fromDt, String toDt, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("fromDt", fromDt);
        params.put("toDt", toDt);
        params.put("curr_user", userId);

        String query;
        query = readFile(Charset.defaultCharset(), "RegionMonthWiseADCReport.txt");

        Query rs = em.createNativeQuery(query).setParameter("frmDt", fromDt).setParameter("toDt", toDt);

        List<Object[]> queryResult = rs.getResultList();
        List<Map<String, ?>> regionAdcList = new ArrayList();
        queryResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REG_NM", w[0] == null ? "" : w[0].toString());
            parm.put("AREA_NM", w[1] == null ? "" : w[1].toString());
            parm.put("BRNCH_NM", w[2] == null ? "" : w[2].toString());
            parm.put("CHANNEL", w[3] == null ? "" : w[3].toString());
            parm.put("TRX_CNT", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("RCVRD_AMT", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("ACTV_CLNTS_LAST_MNTH", w[8] == null ? 0 : new BigDecimal(w[8].toString()).longValue());
            parm.put("DUE_LAST_MNTH", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("ADC_TRX_PER", w[10] == null ? 0.0 : new BigDecimal(w[10].toString()).floatValue());
            parm.put("ADC_AMT_PER", w[11] == null ? 0.0 : new BigDecimal(w[11].toString()).floatValue());
            parm.put("ADC_AMT", w[12] == null ? 0 : new BigDecimal(w[12].toString()).longValue());
            parm.put("CASH_TRX", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());
            parm.put("CASH_AMT", w[14] == null ? 0 : new BigDecimal(w[14].toString()).longValue());
            parm.put("CASH_PER", w[15] == null ? 0.0 : new BigDecimal(w[15].toString()).floatValue());
            parm.put("CASH_AMT_PER", w[16] == null ? 0.0 : new BigDecimal(w[16].toString()).floatValue());

            regionAdcList.add(parm);
        });
        params.put("data_set", getJRDataSource(regionAdcList));
        return reportComponent.generateReport(ADC_MONTH_WISE_REPORT, params, null);
    }

    public byte[] getAnmlInsuClmFrm(long anmlRgstrSeq, String userId) throws IOException {

        Map<String, Object> params = new HashMap<>();
        String clntinfoQry;
        clntinfoQry = readFile(Charset.defaultCharset(), "AnmlInsuClmFrmclntinfoQry.txt");

        Query rs = em.createNativeQuery(clntinfoQry).setParameter("anmlRgstrSeq", anmlRgstrSeq);

        Object[] clntObj = (Object[]) rs.getSingleResult();
        params.put("CLNT_ID", clntObj[0].toString());
        params.put("CLNT_NM", clntObj[1].toString());
        params.put("CNIC_NUM", clntObj[2] == null ? "" : clntObj[2].toString());
        params.put("PH_NUM", clntObj[3] == null ? "" : clntObj[3].toString());
        params.put("DT_OF_DTH", getFormaedDate(clntObj[4].toString()));
        params.put("CAUSE_OF_DTH", clntObj[5] == null ? "" : clntObj[5].toString());
        params.put("CLNT_NOM_FLG", clntObj[6] == null ? "" : clntObj[6].toString());
        params.put("APRVD_LOAN_AMT", clntObj[7] == null ? 0 : new BigDecimal(clntObj[7].toString()).longValue());
        params.put("LOAN_APP_STS_DT", getFormaedDate(clntObj[8].toString()));
        params.put("REL_NM", clntObj[9] == null ? "" : clntObj[9].toString());
        params.put("LOAN_APP_SEQ", clntObj[10] == null ? 0 : new BigDecimal(clntObj[10].toString()).longValue());
        params.put("ANML_TYP", clntObj[12] == null ? "" : clntObj[12].toString());
        params.put("TAG_NUM", clntObj[13] == null ? "" : clntObj[13].toString());
        params.put("PRCH_AMT", clntObj[14] == null ? "" : clntObj[14].toString());
        params.put("AGE_MNTH", clntObj[15] == null ? "" : clntObj[15].toString());
        params.put("AGE_YR", clntObj[16] == null ? "" : clntObj[16].toString());
        params.put("ADDR", clntObj[17] == null ? "" : clntObj[17].toString());
        Clob anml = null;
        Clob tag = null;

        try {
            anml = (Clob) (clntObj[18] == null ? null : clntObj[18]);

            tag = (Clob) (clntObj[19] == null ? null : clntObj[19]);
        } catch (Exception e) {

        }

        params.put("ANML_IMG", getStringClob(anml));
        params.put("TAG_IMG", getStringClob(tag));

        Clients clnt = clientRepository.findOneByClntSeq(new BigDecimal(clntObj[20].toString()).longValue());

        Query bi = em.createNativeQuery(Queries.USER_BRANCH_INFO).setParameter("portKey",
                clnt.getPortKey());
        Object[] obj = (Object[]) bi.getSingleResult();

        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);

        String bizApprQury;
        bizApprQury = readFile(Charset.defaultCharset(), "AnmlInsuClmFrmbizApprQury.txt");

        Query rset = em.createNativeQuery(bizApprQury).setParameter("loanAppSeq", clntObj[10].toString());

        List<Object[]> bizApprList = rset.getResultList();
        if (bizApprList.size() > 0) {
            params.put("BIZ_ADDR", bizApprList.get(0)[7].toString());
        }

        String paymentsQry;
        paymentsQry = readFile(Charset.defaultCharset(), "AnmlInsuClmFrmpaymentsQry.txt");

        Query resset = em.createNativeQuery(paymentsQry).setParameter("loanAppSeq", clntObj[10].toString());

        List<Object[]> pymtsObj = resset.getResultList();
        List<Map<String, ?>> pymts = new ArrayList();
        Long outSts = 0L;
        pymtsObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("INST_NUM", l[0].toString());
            map.put("DUE_DT", getFormaedDate(l[1].toString()));
            map.put("PPAL_AMT_DUE", l[2] == null ? 0 : getLongValue(l[2].toString()));
            map.put("TOT_CHRG_DUE", l[3] == null ? 0 : getLongValue(l[3].toString()));
            map.put("AMT", l[4] == null ? 0 : getLongValue(l[4].toString()));
            map.put("PR_REC", l[5] == null ? 0 : getLongValue(l[5].toString()));
            map.put("SC_REC", l[6] == null ? 0 : getLongValue(l[6].toString()));
            map.put("PYMT_DT", l[7] == null ? "" : getFormaedDate(l[7].toString()));
            pymts.add(map);
        });

        return reportComponent.generateReport(ANML_INSURANCECLAIM, params, getJRDataSource(pymts));
    }

    public byte[] getPortfolioConcentrationReport(long prdSeq, long brnch, String asDt, String userId)
            throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO).setParameter("userId", userId);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("asDt", asDt);

        Query prdQ = em.createNativeQuery("select prd_cmnt \r\n" + "from mw_prd p\r\n"
                + "join mw_prd_grp pg on pg.prd_grp_seq = p.prd_grp_seq  \r\n" + "where pg.prd_grp_seq=:prdSeq \r\n"
                + "group by prd_cmnt").setParameter("prdSeq", prdSeq);
        String prd = (String) prdQ.getSingleResult();

        params.put("prd", prd);

        String bussQry;
        bussQry = readFile(Charset.defaultCharset(), "PortfolioConcentrationbussQry.txt");

        Query resset = em.createNativeQuery(bussQry).setParameter("prdSeq", prdSeq).setParameter("brnch", brnch)
                .setParameter("asdt", asDt);

        List<Object[]> bussResult = resset.getResultList();
        List<Map<String, ?>> bussList = new ArrayList();
        bussResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("BIZ_SECT_NM", w[0] == null ? "" : w[0].toString());
            parm.put("ACTV_LNS", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("BSBMT_AMT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("OST_AMT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("OD_AMT_CL", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("OD_LOANS", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("OST_PAR_AMT", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            parm.put("OD_LOANS_30", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("OST_PAR_AMT_30", w[8] == null ? 0 : new BigDecimal(w[8].toString()).longValue());
            parm.put("OD_DYS", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            bussList.add(parm);
        });
        params.put("buss_dataset", getJRDataSource(bussList));

        String rangQry;
        bussQry = readFile(Charset.defaultCharset(), "PortfolioConcentrationbussQry.txt");

        Query rest = em.createNativeQuery(bussQry).setParameter("prdSeq", prdSeq).setParameter("brnch", brnch)
                .setParameter("asdt", asDt);

        List<Object[]> rangResult = rest.getResultList();
        List<Map<String, ?>> rangList = new ArrayList();
        rangResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("LOAN_SIZE", w[0] == null ? "" : w[0].toString());
            parm.put("ACTV_LNS", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("BSBMT_AMT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("OST_AMT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("OD_AMT_CL", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("OD_LOANS", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("OST_PAR_AMT", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            parm.put("OD_LOANS_30", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("OST_PAR_AMT_30", w[8] == null ? 0 : new BigDecimal(w[8].toString()).longValue());
            parm.put("OD_DYS", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            rangList.add(parm);
        });
        params.put("rang_dataset", getJRDataSource(rangList));
        String cycleQry;
        cycleQry = readFile(Charset.defaultCharset(), "PortfolioConcentrationrangQry.txt");

        Query rs = em.createNativeQuery(bussQry).setParameter("prdSeq", prdSeq).setParameter("brnch", brnch)
                .setParameter("adt", asDt);

        List<Object[]> cycleResult = rs.getResultList();
        List<Map<String, ?>> cycleList = new ArrayList();
        cycleResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("LOAN_CYCL_NUM", w[0] == null ? "" : w[0].toString());
            parm.put("ACTV_LOANS", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("LOAN_AMT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("OST_AMT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("OD_AMT_CL", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("OD_LOANS", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("OST_PAR_AMT", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            parm.put("OD_LOANS_30", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("OST_PAR_AMT_30", w[8] == null ? 0 : new BigDecimal(w[8].toString()).longValue());
            parm.put("OD_DYS", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            cycleList.add(parm);
        });
        params.put("cycle_dataset", getJRDataSource(cycleList));
        return reportComponent.generateReport(PORTFOLIO_CONCENTRATION, params, null);
    }

    public byte[] getPendingClientsReport(int typ, long prdSeq, long brnch, String asDt, String userId)
            throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("asDt", asDt);

        Query prdQ = em.createNativeQuery("select PRD_GRP_NM from mw_prd_grp grp where grp.PRD_GRP_SEQ=:prdSeq")
                .setParameter("prdSeq", prdSeq);
        String prd = (String) prdQ.getSingleResult();

        params.put("prd", prd);
        params.put("typ", typ);

        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new SimpleDateFormat("dd-MM-yyyy").parse(asDt));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        c.add(Calendar.DAY_OF_MONTH, -30);

        String fromDt = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime());
        String toDt = asDt;

        if (typ == 1) {
            toDt = fromDt;
            c.add(Calendar.DAY_OF_MONTH, -335);
            fromDt = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime());
        }

        String bussQry;
        bussQry = readFile(Charset.defaultCharset(), "PendingClientsReportbussQry.txt");

        Query rs = em.createNativeQuery(bussQry).setParameter("prd", prdSeq).setParameter("brnch", brnch)
                .setParameter("asDt", asDt).setParameter("fromDt", fromDt).setParameter("toDt", toDt);

        List<Object[]> bussResult = rs.getResultList();
        List<Map<String, ?>> bussList = new ArrayList();
        bussResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("PORT_NM", w[0] == null ? "" : w[0].toString());
            parm.put("BDO", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_ID", w[2] == null ? "" : w[2].toString());
            parm.put("CLNT_NM", w[3] == null ? "" : w[3].toString());
            parm.put("FTHR_NM", w[4] == null ? "" : w[4].toString());
            parm.put("SPZ_NM", w[5] == null ? "" : w[5].toString());
            parm.put("PH_NUM", w[6] == null ? "" : w[6].toString());
            parm.put("LOANUSER", w[7] == null ? "" : w[7].toString());
            parm.put("ADDR", w[8] == null ? "" : w[8].toString());
            parm.put("LOAN_CYCL_NUM", w[9] == null ? "" : w[9].toString());
            parm.put("DSBMT_AMT", w[10] == null ? 0 : new BigDecimal(w[10].toString()).longValue());
            parm.put("CMP_DT", w[11] == null ? "" : getFormaedDate(w[11].toString()));
            parm.put("OD_INST", w[12] == null ? "" : w[12].toString());
            parm.put("PND_DAYS", w[14] == null ? "" : w[14].toString());

            bussList.add(parm);
        });
        return reportComponent.generateReport(PENDINGCLIENTS, params, getJRDataSource(bussList));
    }

    public byte[] getTaggedClientCliamReport(long prdSeq, long brnch, String asDt, String userId) throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("asDt", asDt);

        Query prdQ = em.createNativeQuery("select p.PRD_GRP_NM from mw_prd_grp p\n" +
                "    where p.prd_grp_seq=:prdSeq \n" +
                "    group by p.PRD_GRP_NM").setParameter("prdSeq", prdSeq);
        String prd = (String) prdQ.getSingleResult();

        params.put("prd", prd);
        String bussQry;

        bussQry = readFile(Charset.defaultCharset(), "TAGGED_CLIENT_CLAIM.txt");

        Query rs = em.createNativeQuery(bussQry).setParameter("prdSeq", prdSeq).setParameter("brnchSeq", brnch)
                .setParameter("aDt", asDt);

        List<Object[]> bussResult = rs.getResultList();
        List<Map<String, ?>> bussList = new ArrayList();
        bussResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();

            parm.put("EMP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLSNG_CLNT", w[1] == null ? "" : w[1].toString());
            parm.put("CLSNG_CLNT_NAME", w[2] == null ? "" : w[2].toString());
            parm.put("TAG_DT", w[3] == null ? "" : getFormaedDate(w[3].toString()));
            parm.put("PH_NUM", w[4] == null ? "" : w[4].toString());
            parm.put("ADDRESS", w[5] == null ? "" : w[5].toString());
            parm.put("DSBMT_DT", w[6] == null ? "" : getFormaedDate(w[6].toString()));
            parm.put("DISB_AMT", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("CLSNG_REM_INST", w[8] == null ? "" : w[8].toString());
            parm.put("CLSNG_OST_AMT", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("CLSNG_OD_REM_INST", w[10] == null ? 0 : new BigDecimal(w[10].toString()).longValue());
            parm.put("CLSNG_OD_AMT", w[11] == null ? 0 : new BigDecimal(w[11].toString()).longValue());
            parm.put("OD_DAYS", w[12] == null ? 0 : new BigDecimal(w[12].toString()).longValue());
            parm.put("OD_DAYS_WHEN_TAGED", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());

            bussList.add(parm);
        });
        return reportComponent.generateReport(TAGGEDCLIENTSCLAIM, params, getJRDataSource(bussList));
    }

    public byte[] getProductWiseReprotAddition(String frmDt, String toDt, long brnch, String userId)
            throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("frmDt", frmDt);
        params.put("toDt", toDt);

        String bussQry;
        bussQry = readFile(Charset.defaultCharset(), "ProductWiseReprotAdditionbussQry.txt");

        Query rs = em.createNativeQuery(bussQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("brnch", brnch);

        List<Object[]> bussResult = rs.getResultList();
        List<Map<String, ?>> bussList = new ArrayList();
        bussResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("MONTHS", w[0] == null ? "" : w[0].toString());
            parm.put("PRD_CMNT", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_CNT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("DSBMT_AMT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            bussList.add(parm);
        });

        params.put("data_cash", getJRDataSource(bussList));

        String easyPayQry;
        easyPayQry = readFile(Charset.defaultCharset(), "ProductWiseReprotAdditioneasyPayQry.txt");

        Query res = em.createNativeQuery(easyPayQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("brnch", brnch);

        List<Object[]> easypayRes = res.getResultList();
        List<Map<String, ?>> esaypayList = new ArrayList();
        easypayRes.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("MONTHS", w[0] == null ? "" : w[0].toString());
            parm.put("PRD_CMNT", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_CNT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("DSBMT_AMT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            esaypayList.add(parm);
        });
        params.put("data_easypay", getJRDataSource(esaypayList));
        String allQry;
        allQry = readFile(Charset.defaultCharset(), "ProductWiseReprotAdditionallQry.txt");

        Query result = em.createNativeQuery(easyPayQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("brnch", brnch);

        List<Object[]> allRes = result.getResultList();
        List<Map<String, ?>> allList = new ArrayList();
        allRes.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("MONTHS", w[0] == null ? "" : w[0].toString());
            parm.put("PRD_CMNT", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_CNT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("DSBMT_AMT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            allList.add(parm);
        });
        params.put("data_all", getJRDataSource(allList));
        return reportComponent.generateReport(PRODUCTWISEREPORTADDITION, params, null);
    }

    public byte[] getAgencyTrackingReport(long brnchSeq, String userId) throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);

        String rateOfRetObjQury;
        rateOfRetObjQury = readFile(Charset.defaultCharset(), "rateOfRetObjQury.txt");

        Query result = em.createNativeQuery(rateOfRetObjQury).setParameter("brnchSeq", brnchSeq);

        List<Object[]> rateOfRetObj = result.getResultList();
        List<Map<String, ?>> rateOfRetObjList = new ArrayList();
        int c = 1;

        DecimalFormat df = new DecimalFormat("#.##");
        for (Object[] w : rateOfRetObj) {
            params.put("MONTH" + (c < 10 ? "0" : "") + c, w[0].toString());
            Map<String, Object> parm = new HashMap<>();
            parm.put("CMPLTD_MNTH", w[0].toString());
            parm.put("MNTH_ORDR", w[1].toString());
            parm.put("TRGT_CLNTS", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("AGENCIES", new BigDecimal(w[3].toString()).longValue());
            parm.put("MONTH01", new BigDecimal(w[4].toString()).longValue());
            parm.put("MONTH02", new BigDecimal(w[5].toString()).longValue());
            parm.put("MONTH03", new BigDecimal(w[6].toString()).longValue());
            parm.put("MONTH04", new BigDecimal(w[7].toString()).longValue());
            parm.put("MONTH05", new BigDecimal(w[8].toString()).longValue());
            parm.put("MONTH06", new BigDecimal(w[9].toString()).longValue());
            parm.put("MONTH07", new BigDecimal(w[10].toString()).longValue());
            parm.put("MONTH08", new BigDecimal(w[11].toString()).longValue());
            parm.put("MONTH09", new BigDecimal(w[12].toString()).longValue());
            parm.put("MONTH10", new BigDecimal(w[13].toString()).longValue());
            parm.put("MONTH11", new BigDecimal(w[14].toString()).longValue());
            parm.put("MONTH12", new BigDecimal(w[15].toString()).longValue());
            long achivTtl = new BigDecimal(w[15].toString()).longValue() + new BigDecimal(w[4].toString()).longValue()
                    + new BigDecimal(w[5].toString()).longValue() + new BigDecimal(w[6].toString()).longValue()
                    + new BigDecimal(w[7].toString()).longValue() + new BigDecimal(w[8].toString()).longValue()
                    + new BigDecimal(w[9].toString()).longValue() + new BigDecimal(w[10].toString()).longValue()
                    + new BigDecimal(w[11].toString()).longValue() + new BigDecimal(w[12].toString()).longValue()
                    + new BigDecimal(w[13].toString()).longValue() + new BigDecimal(w[14].toString()).longValue();

            double achvRat = (achivTtl == 0 || w[2] == null) ? 0
                    : ((double) achivTtl / new BigDecimal(w[2].toString()).longValue()) * 100;
            parm.put("ACHIV_TTL", achivTtl);
            parm.put("ACHIV_PER", df.format(achvRat));
            parm.put("VARI", achivTtl - (w[2] == null ? 0L : new BigDecimal(w[2].toString()).longValue()));
            rateOfRetObjList.add(parm);
            c++;
        }

        return reportComponent.generateReport(AGENCIES_TRAGET_TRACKING, params, getJRDataSource(rateOfRetObjList));
    }

    public byte[] getTransferredClients(String frmDt, String toDt, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("frm_dt", frmDt);
        params.put("curr_user", userId);
        params.put("to_dt", toDt);

        String rateOfRetObjQury;
        rateOfRetObjQury = readFile(Charset.defaultCharset(), "TRANSFERRED_CLIENTS.txt");

        Query result = em.createNativeQuery(rateOfRetObjQury).setParameter("frmDt", frmDt).setParameter("toDt", toDt);

        List<Object[]> bussResult = result.getResultList();
        List<Map<String, ?>> bussList = new ArrayList();
        bussResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("PRD_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_ID", w[1] == null ? "" : w[1].toString());
            parm.put("LOAN_APP_SEQ", w[2] == null ? "" : w[2].toString());
            parm.put("CLNT_NM", w[3] == null ? "" : w[3].toString());
            parm.put("FRM_BDO", w[4] == null ? "" : w[4].toString());
            parm.put("TO_BDO", w[5] == null ? "" : w[5].toString());
            parm.put("FRM_BRNCH", w[6] == null ? "" : w[6].toString());
            parm.put("TO_BRNCH", w[7] == null ? "" : w[7].toString());
            parm.put("LOAN_APP_STS_DT", w[8] == null ? "" : getFormaedDate(w[8].toString()));
            parm.put("APRVD_LOAN_AMT", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("INST_NUM", w[10] == null ? 0 : new BigDecimal(w[10].toString()).longValue());
            parm.put("OST_PR", w[11] == null ? 0 : new BigDecimal(w[11].toString()).longValue());
            parm.put("OST_SC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).longValue());
            parm.put("KSZB_OST", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());
            parm.put("DOC_FEE_OST", w[14] == null ? 0 : new BigDecimal(w[14].toString()).longValue());
            parm.put("TRN_OST", w[15] == null ? 0 : new BigDecimal(w[15].toString()).longValue());
            parm.put("INS_OST", w[16] == null ? 0 : new BigDecimal(w[16].toString()).longValue());
            parm.put("EX_RCVRY", w[17] == null ? 0 : new BigDecimal(w[17].toString()).longValue());
            bussList.add(parm);
        });
        return reportComponent.generateReport(TRANSFERRED_CLIENTS, params, getJRDataSource(bussList));
    }

    public byte[] getTurnAroundTimeReport(String frmDt, String toDt, String userId, long rpt_flg, long areaSeq,
                                          long regSeq, long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();

        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em
                    .createNativeQuery("select rg.reg_nm from mw_reg rg where rg.reg_seq=:regSeq and rg.crnt_rec_flg=1")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        String rateOfRetObjQury;
        rateOfRetObjQury = readFile(Charset.defaultCharset(), "TurnAroundTimeReportbdoQry.txt");

        Query result = em.createNativeQuery(rateOfRetObjQury).setParameter("brnch_seq", brnchSeq)
                .setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> bdoObj = result.getResultList();

        List<Map<String, ?>> bdos = new ArrayList();
        bdoObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_NM", l[0] == null ? "" : l[0].toString());
            map.put("DAYS_1_5", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DAYS_6_10", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("DAYS_11_15", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("DAYS_1_15", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OVR_15", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("TOT_CLNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("NO_OF_DYS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            map.put("TOT_DAYS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            bdos.add(map);
        });
        params.put("bdos", getJRDataSource(bdos));

        String prdQry;
        prdQry = readFile(Charset.defaultCharset(), "TurnAroundTimeReportprdQry.txt");

        Query res = em.createNativeQuery(prdQry).setParameter("brnch_seq", brnchSeq).setParameter("frmdt", frmDt)
                .setParameter("todt", toDt);

        List<Object[]> prdObj = res.getResultList();

        List<Map<String, ?>> prds = new ArrayList();
        prdObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
            map.put("DAYS_1_5", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DAYS_6_10", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("DAYS_11_15", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("DAYS_1_15", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OVR_15", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("TOT_CLNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("NO_OF_DYS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            map.put("TOT_DAYS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            prds.add(map);
        });
        params.put("prds", getJRDataSource(prds));
        return reportComponent.generateReport(TURNAROUNDTIME, params, null);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getFemaleParticipationAll(String toDt, String userId, String roleType, long brnchSeq, long areaSeq, long regSeq) {
        String allQry;
        allQry = readFile(Charset.defaultCharset(), "FemaleParticipationReportallQry.txt");

        Query res = em.createNativeQuery(allQry).setParameter("todt", toDt).setParameter("p_user", userId)
                .setParameter("brnchSeq", brnchSeq).setParameter("role_type", roleType);
        List<Object[]> allObj = res.getResultList();

        List<Map<String, ?>> all = new ArrayList();
        allObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_NM", l[0] == null ? "" : l[0].toString());
            map.put("FEMALE", l[1] == null ? "" : new BigDecimal(l[1].toString()).longValue());
            map.put("MALE", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("JOINT_USER", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            all.add(map);
        });
        return CompletableFuture.completedFuture(all);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getFemaleParticipationLoans(String toDt, String userId, String roleType, long brnchSeq, long areaSeq, long regSeq) {
        String loanQry;
        loanQry = readFile(Charset.defaultCharset(), "FemaleParticipationReportloanQry.txt");

        Query rs = em.createNativeQuery(loanQry).setParameter("todt", toDt).setParameter("p_user", userId)
                .setParameter("brnchSeq", brnchSeq);
        List<Object[]> loanObj = rs.getResultList();

        List<Map<String, ?>> loans = new ArrayList();
        loanObj.forEach(l -> {
            Map<String, Object> map = new HashMap();

            map.put("MALE_REP", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("FEMALE_REP", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("JOINT_USER_REP", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());

            map.put("MALE_NEW", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("FEMALE_NEW", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("JOINT_USER_NEW", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());

            loans.add(map);
        });

        return CompletableFuture.completedFuture(loans);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getFemaleParticipationPrds(String toDt, String userId, String roleType, long brnchSeq, long areaSeq, long regSeq) {

        String prdQry;
        prdQry = readFile(Charset.defaultCharset(), "FemaleParticipationReportprdQry.txt");

        Query result = em.createNativeQuery(prdQry).setParameter("todt", toDt).setParameter("p_user", userId)
                .setParameter("brnchSeq", brnchSeq);
        List<Object[]> prdObj = result.getResultList();

        List<Map<String, ?>> prds = new ArrayList();
        prdObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
            map.put("FEMALE", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("MALE", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("JOINT_USER", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            prds.add(map);
        });
        return CompletableFuture.completedFuture(prds);
    }

    public byte[] getFemaleParticipationReport(String toDt, String userId, String roleType, long brnchSeq, long areaSeq, long regSeq, List<Map<String, ?>> all, List<Map<String, ?>> loans, List<Map<String, ?>> prds) throws IOException {
        Map<String, Object> params = new HashMap<>();

        Query bi;
        if (brnchSeq != -1) {
            bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                    brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
            params.put("area_cd", obj[5].toString());
        } else if (areaSeq != -1) {
            bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", obj[2].toString());
        } else if (regSeq != -1) {
            bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq",
                    regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[1].toString());
            params.put("area_nm", "ALL");
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", "");
        }
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("roleType", roleType);
        params.put("all", getJRDataSource(all));
        params.put("prds", getJRDataSource(prds));
        params.put("loans", getJRDataSource(loans));

        return reportComponent.generateReport(FEMALEPARTICIPATION, params, null);
    }

    public byte[] getDuesReport(String frmDt, String toDt, String userId, long branch) throws IOException {
        Map<String, Object> params = new HashMap<>();

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branch);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "DuesReportdetailQry.txt");

        Query result = em.createNativeQuery(detailQry).setParameter("brnch_seq", branch).setParameter("frm_dt", frmDt)
                .setParameter("to_dt", toDt).setParameter("user_id", userId);

        List<Object[]> dtlObj = result.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();
        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PORT_TYP", l[0] == null ? "" : l[0].toString());
            map.put("EMP_SEQ", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("EMP_NM", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_ID", l[3] == null ? "" : l[3].toString());
            map.put("NAME", l[4] == null ? "" : l[4].toString());
            map.put("NOM_NM", l[5] == null ? "" : l[5].toString());
            map.put("PH_NUM", l[6] == null ? "" : l[6].toString());
            map.put("ADDR", l[7] == null ? "" : l[7].toString());
            map.put("INST_NUM", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("LOAN_CYCL_NUM", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("PRD_CMNT", l[10] == null ? "" : l[10].toString());
            map.put("INST_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("DUE_DT", l[12] == null ? "" : l[12].toString());
            map.put("OD_INST", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("OD_DAYS", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("PDC_HLDR_NM", l[15] == null ? "" : l[15].toString());
            map.put("PDC_HLDR_PHN", l[16] == null ? "" : l[16].toString());
            pymts.add(map);
        });
        return reportComponent.generateReport(DUES, params,

                getJRDataSource(pymts));
    }

    public byte[] getPortfolioSegReport(String toDt, String userId, String roleType, long brnchSeq, long areaSeq, long regSeq)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi;
        if (brnchSeq != -1) {
            bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                    brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
            params.put("area_cd", obj[5].toString());
        } else if (areaSeq != -1) {
            bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", obj[2].toString());
        } else if (regSeq != -1) {
            bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq",
                    regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[1].toString());
            params.put("area_nm", "ALL");
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", "");
        }
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("roleType", roleType);

        String secQry;
        secQry = readFile(Charset.defaultCharset(), "portfolioSegmentationScript.txt");
        Query result = em.createNativeQuery(secQry).setParameter("todt", toDt).setParameter("p_user", userId)
                .setParameter("brnchSeq", brnchSeq).setParameter("role_type", roleType);

        List<Object[]> secObj = result.getResultList();

        List<Map<String, ?>> sectors = new ArrayList();
        secObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : l[0].toString());
            map.put("SECT", l[1] == null ? "" : l[1].toString());
            map.put("LOAN_CNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("APRVD_LOAN_AMT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("OST_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            sectors.add(map);
        });

        return reportComponent.generateReport(PORTFOLIO_SEG, params, getJRDataSource(sectors));
    }

    @Async
    public CompletableFuture<Map<String, Object>> getPortfolioAtRiskTimeSeriseOne(String asDt, long brnchSeq) {
        System.out.println("01-Start");
        long start = System.currentTimeMillis();

        Map<String, Object> mapList = getPortfolioatRiskComputeGraph(asDt, brnchSeq, "portfolioAtRiskTimeSerise_1.txt");

        long end = System.currentTimeMillis();
        System.out.println("01-Seconds " + (end - start) / 1000.0);

        return CompletableFuture.completedFuture(mapList);
    }

    @Async
    public CompletableFuture<Map<String, Object>> getPortfolioAtRiskTimeSeriseTwo(String asDt, long brnchSeq) {
        System.out.println("02-Start");

        long start = System.currentTimeMillis();
        Map<String, Object> mapList = getPortfolioatRiskComputeGraph(asDt, brnchSeq, "portfolioAtRiskTimeSerise_2.txt");

        long end = System.currentTimeMillis();
        System.out.println("02-Seconds " + (end - start) / 1000.0);

        return CompletableFuture.completedFuture(mapList);
    }

    @Async
    public CompletableFuture<Map<String, Object>> getPortfolioAtRiskTimeSeriseThree(String asDt, long brnchSeq) {
        System.out.println("03-Start");

        long start = System.currentTimeMillis();
        Map<String, Object> mapList = getPortfolioatRiskComputeGraph(asDt, brnchSeq, "portfolioAtRiskTimeSerise_3.txt");

        long end = System.currentTimeMillis();
        System.out.println("03-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapList);
    }

    @Async
    public CompletableFuture<Map<String, Object>> getPortfolioAtRiskTimeSeriseFour(String asDt, long brnchSeq) {
        System.out.println("04-Start");
        long start = System.currentTimeMillis();
        Map<String, Object> mapList = getPortfolioatRiskComputeGraph(asDt, brnchSeq, "portfolioAtRiskTimeSerise_4.txt");

        long end = System.currentTimeMillis();
        System.out.println("04-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapList);
    }

    @Async
    public CompletableFuture<Map<String, Object>> getPortfolioAtRiskTimeSeriseFive(String asDt, long brnchSeq) {
        System.out.println("05-Start");
        long start = System.currentTimeMillis();
        Map<String, Object> mapList = getPortfolioatRiskComputeGraph(asDt, brnchSeq, "portfolioAtRiskTimeSerise_5.txt");

        long end = System.currentTimeMillis();
        System.out.println("05-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapList);
    }

    @Async
    public CompletableFuture<Map<String, Object>> getPortfolioAtRiskTimeSeriseSix(String asDt, long brnchSeq) {
        System.out.println("06-Start");
        long start = System.currentTimeMillis();
        Map<String, Object> mapLists = getPortfolioatRiskComputeGraph(asDt, brnchSeq, "portfolioAtRiskTimeSerise_6.txt");
        long end = System.currentTimeMillis();
        System.out.println("06-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeriesSeven(String asDt, long brnchSeq) {
        System.out.println("07-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_7.txt");

        long end = System.currentTimeMillis();
        System.out.println("07-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeriesEight(String asDt, long brnchSeq) {
        System.out.println("08-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_8.txt");

        long end = System.currentTimeMillis();
        System.out.println("O8-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeriesNine(String asDt, long brnchSeq) {
        System.out.println("09-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_9.txt");

        long end = System.currentTimeMillis();
        System.out.println("09-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeriesTen(String asDt, long brnchSeq) {
        System.out.println("10-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_10.txt");
        long end = System.currentTimeMillis();
        System.out.println("10-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeriesEle(String asDt, long brnchSeq) {
        System.out.println("11-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_11.txt");

        long end = System.currentTimeMillis();
        System.out.println("11-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeriesTwe(String asDt, long brnchSeq) {
        System.out.println("12-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_12.txt");

        long end = System.currentTimeMillis();
        System.out.println("12-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeries13(String asDt, long brnchSeq) {
        System.out.println("13-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_13.txt");

        long end = System.currentTimeMillis();
        System.out.println("13-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeries14(String asDt, long brnchSeq) {
        System.out.println("14-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_14.txt");

        long end = System.currentTimeMillis();
        System.out.println("14-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeries15(String asDt, long brnchSeq) {
        System.out.println("15-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_15.txt");

        long end = System.currentTimeMillis();
        System.out.println("15-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeries16(String asDt, long brnchSeq) {
        System.out.println("16-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_16.txt");

        long end = System.currentTimeMillis();
        System.out.println("16-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeries17(String asDt, long brnchSeq) {
        System.out.println("17-Start");
        long start = System.currentTimeMillis();
        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_17.txt");

        long end = System.currentTimeMillis();
        System.out.println("17-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    @Async
    public CompletableFuture<List<Map<String, ?>>> getPortfolioAtRiskTimeSeries18(String asDt, long brnchSeq) {
        System.out.println("18-Start");
        long start = System.currentTimeMillis();

        List<Map<String, ?>> mapLists = getPortfolioatRiskComputeTable(asDt, brnchSeq, "portfolioAtRiskTimeSerise_18.txt");

        long end = System.currentTimeMillis();
        System.out.println("18-Seconds " + (end - start) / 1000.0);
        return CompletableFuture.completedFuture(mapLists);
    }

    public Map<String, Object> getPortfolioatRiskComputeGraph(String asDt, long brnchSeq, String scriptName) {

        asDt = getPortfolioAtRiskAsOfDate(asDt);
        String graphQry1 = readFile(Charset.defaultCharset(), scriptName);
        Query rs1 = em.createNativeQuery(graphQry1).setParameter("todt", asDt).setParameter("brnch_seq", brnchSeq);
        List<Object[]> graphObj = rs1.getResultList();

        return getPortfolioAtRiskMapSet(graphObj);
    }

    public List<Map<String, ?>> getPortfolioatRiskComputeTable(String asDt, long brnchSeq, String scriptName) {

        asDt = getPortfolioAtRiskAsOfDate(asDt);
        String graphQry1 = readFile(Charset.defaultCharset(), scriptName);
        Query rs1 = em.createNativeQuery(graphQry1).setParameter("todt", asDt).setParameter("brnch_seq", brnchSeq);
        List<Object[]> graphObj = rs1.getResultList();

        return getPortfolioAtRiskMapSetPrd(graphObj);
    }


    public String getPortfolioAtRiskAsOfDate(String asDt) {
        DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date = new Date();
        try {
            date = inputFormat.parse(asDt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int asOfDateMonth = cal.get(Calendar.MONTH) + 1;
        int asOfDateYear = cal.get(Calendar.YEAR);

        if ((asOfDateMonth >= currentMonth) && (asOfDateYear >= currentYear)) {
            // Convert Date to Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, -1);

            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            asDt = inputFormat.format(calendar.getTime());
        }
        return asDt;
    }

    public Map<String, Object> getPortfolioAtRiskMapSet(List<Object[]> obj) {
        Map<String, Object> map = new HashMap();
        obj.forEach(l -> {
            map.put("PAR_ORDR", l[0] == null ? "" : l[0].toString());
            map.put("PAR_MONTH", l[1] == null ? "" : l[1].toString());
            map.put("PAR_PERC", l[2] == null ? 0 : new BigDecimal(l[2].toString()).doubleValue());
            map.put("PAR_PERC1", l[3] == null ? 0 : new BigDecimal(l[3].toString()).doubleValue());
        });
        return map;
    }

    public List<Map<String, ?>> getPortfolioAtRiskMapSetPrd(List<Object[]> obj) {

        List<Map<String, ?>> maps = new ArrayList<>();
        obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : l[0].toString());
            map.put("PAR_ORDR", l[1] == null ? "" : l[1].toString());
            map.put("PAR_MONTH", l[2] == null ? "" : l[2].toString());
            map.put("PRD_GRP_NM", l[3] == null ? "" : l[3].toString());
            map.put("TOT_CLNT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OST_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("PAR_OVER_30_DAY_CNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("PAR_OVER_30_DAY_OD_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("PAR_OVER_30_AMT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("PAR_PERC", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());
            map.put("PAR_OVER_1_DAY_CNT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PAR_OVER_1_DAY_OD_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("PAR_OVER_1_AMT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("PAR_PERC1", l[13] == null ? 0 : new BigDecimal(l[13].toString()).doubleValue());

            maps.add(map);
        });

        return maps;
    }

    public byte[] getPortfolioAtRiskReport(String asDt, String userId, long rpt_flg, long areaSeq, long regSeq, long brnchSeq,
                                           List<Map<String, ?>> maps, List<Map<String, ?>> prds, List<Map<String, ?>> bdos) {
        Map<String, Object> params = new HashMap<>();

        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ).setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em.createNativeQuery("select rg.reg_nm,rg.reg_seq from mw_reg rg where rg.reg_seq=:regSeq and rg.crnt_rec_flg=1")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }

        params.put("curr_user", userId);
        params.put("as_dt", asDt);

        params.put("prds", getJRDataSource(prds));
        params.put("bdos", getJRDataSource(bdos));

        return reportComponent.generateReport(PORTFOLIO_RISK, params, getJRDataSource(maps));
    }

    public byte[] getRiskFlaggingReport(String frmDt, String toDt, String userId, long rpt_flg, long areaSeq,
                                        long regSeq, long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();
        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em
                    .createNativeQuery(
                            "select rg.reg_nm,rg.reg_seq from mw_reg rg where rg.reg_seq=:regSeq and rg.crnt_rec_flg=1")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String jumpsQry;
        jumpsQry = readFile(Charset.defaultCharset(), "RiskFlaggingReportJumpsQryy.txt");

        Query reSet1 = em.createNativeQuery(jumpsQry).setParameter("brnch_seq", brnchSeq).setParameter("frmdt", frmDt)
                .setParameter("todt", toDt);
        List<Object[]> jumpsObj = reSet1.getResultList();

        List<Map<String, ?>> jumps = new ArrayList();
        jumpsObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("NAME", l[0] == null ? "" : l[0].toString());
            map.put("CLNT_ID", l[1] == null ? "" : l[1].toString());
            map.put("LOAN_CYCL_NUM", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("PRD_GRP_NM", l[3] == null ? "" : l[3].toString());
            map.put("APRVD_LOAN_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("LOAN_JUMP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("EMP_NM", l[6] == null ? "" : l[6].toString());
            map.put("OD_INST", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            jumps.add(map);
        });

        params.put("jumps", getJRDataSource(jumps));

        String disbQry;
        disbQry = readFile(Charset.defaultCharset(), "RiskFlaggingReportDisbQry.txt");

        Query rs = em.createNativeQuery(disbQry).setParameter("brnch_seq", brnchSeq).setParameter("frmdt", frmDt)
                .setParameter("todt", toDt);
        List<Object[]> disbObj = rs.getResultList();

        List<Map<String, ?>> disb = new ArrayList();
        disbObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("NAME", l[0] == null ? "" : l[0].toString());
            map.put("CLNT_ID", l[1] == null ? "" : l[1].toString());
            map.put("LOAN_CYCL_NUM", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("PRD_GRP_NM", l[3] == null ? "" : l[3].toString());
            map.put("APRVD_LOAN_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("LOAN_JUMP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("EMP_NM", l[6] == null ? "" : l[6].toString());
            map.put("OD_INST", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            disb.add(map);
        });
        params.put("disb", getJRDataSource(disb));

        String mfisQry;
        mfisQry = readFile(Charset.defaultCharset(), "RiskFlaggingReportmfisQry.txt");

        Query rset = em.createNativeQuery(mfisQry).setParameter("user_id", userId).setParameter("frmdt", frmDt)
                .setParameter("todt", toDt).setParameter("rpt_flg", rpt_flg).setParameter("areaSeq", areaSeq)
                .setParameter("regSeq", regSeq).setParameter("brnch_seq", brnchSeq);
        List<Object[]> mfisObj = rset.getResultList();

        List<Map<String, ?>> mfis = new ArrayList();
        mfisObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("NAME", l[0] == null ? "" : l[0].toString());
            map.put("CLNT_ID", l[1] == null ? "" : l[1].toString());
            map.put("LOAN_CYCL_NUM", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("PRD_CMNT", l[3] == null ? "" : l[3].toString());
            map.put("APRVD_LOAN_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("LOAN_JUMP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("EMP_NM", l[6] == null ? "" : l[6].toString());
            map.put("OD_INST", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("OTH_LOANS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            mfis.add(map);
        });
        params.put("mfis", getJRDataSource(mfis));

        String rentQry;
        rentQry = readFile(Charset.defaultCharset(), "RiskFlaggingReportRentQry.txt");

        Query resset = em.createNativeQuery(rentQry).setParameter("brnch_seq", brnchSeq).setParameter("todt", toDt);
        List<Object[]> rentObj = resset.getResultList();

        List<Map<String, ?>> rented = new ArrayList();
        rentObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REF_CD_DSCR", l[0] == null ? "" : l[0].toString());
            map.put("LOANS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            rented.add(map);
        });

        String jum30pieScript;
        jum30pieScript = readFile(Charset.defaultCharset(), "RiskFlaggingReportJump30Qry.txt");
        Query jum30 = em.createNativeQuery(jum30pieScript).setParameter("brnch_seq", brnchSeq)
                .setParameter("frmdt", frmDt).setParameter("todt", toDt);
        List<Object[]> jumobj = jum30.getResultList();

        List<Map<String, ?>> jum30param = new ArrayList();
        jumobj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSCR", l[0] == null ? "" : l[0].toString());
            map.put("LOANS_30", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            jum30param.add(map);
        });

        params.put("jum30pie", getJRDataSource(jum30param));

        String jum80pieScript;
        jum80pieScript = readFile(Charset.defaultCharset(), "RiskFlaggingReport80Qry.txt");
        Query jum80 = em.createNativeQuery(jum80pieScript).setParameter("brnch_seq", brnchSeq)
                .setParameter("frmdt", frmDt).setParameter("todt", toDt);
        List<Object[]> jum80obj = jum80.getResultList();

        List<Map<String, ?>> jum80param = new ArrayList();
        jum80obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSCR", l[0] == null ? "" : l[0].toString());
            map.put("LOANS_80", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            jum80param.add(map);
        });

        params.put("jum80pie", getJRDataSource(jum80param));

        return reportComponent.generateReport(RISKFLAG, params, getJRDataSource(rented));
    }

    public byte[] getPortfolioStatusActiveReport(String toDt, String userId, String roleType, long areaSeq, long regSeq,
                                                 long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi;
        if (brnchSeq != -1) {
            bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                    brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
            params.put("area_cd", obj[5].toString());
        } else if (areaSeq != -1) {
            bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", obj[2].toString());
        } else if (regSeq != -1) {
            bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq",
                    regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[1].toString());
            params.put("area_nm", "ALL");
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", "");
        }
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("roleType", roleType);

        String activePortObjQry;
        activePortObjQry = readFile(Charset.defaultCharset(), "portfolioStatusScript.txt");

        Query resSet = em.createNativeQuery(activePortObjQry).setParameter("brnchSeq", brnchSeq).setParameter("to_dt", toDt)
                .setParameter("p_user", userId).setParameter("role_type", roleType);
        List<Object[]> activePortObj = resSet.getResultList();

        List<Map<String, ?>> activePorts = new ArrayList();
        activePortObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : l[0].toString());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("TOT_CLNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("TOT_CLNT_ADS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("TOT_CLNT_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("NEW_CLNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("NEW_CLNT_ADS", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("NEW_CLNT_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("RPT_CLNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("RPT_CLNT_ADS", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("RPT_CLNT_AMT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("OST_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("PAR_1_DY_AMT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("PAR_1__29_DAY_AMT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("PAR_OVER_30_AMT", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("OD_CLNTS", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OD_CLNTS_30", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("RNW_CLNT_MONTH", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("CMPLTD_MNTH", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("REP_MNTH", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("DSBMT_MNTH", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
            map.put("FML_PRT", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("FML_TTOL", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());

            activePorts.add(map);
        });

        return reportComponent.generateReport(PORTFOLIO_STAT_REP, params, getJRDataSource(activePorts));
    }

    public byte[] getConsolidatedLoanReport(String frmDt, String toDt, String userId, long rpt_flg, long areaSeq,
                                            long regSeq, long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();

        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em
                    .createNativeQuery(
                            "select rg.reg_nm,rg.reg_seq from mw_reg rg where rg.reg_seq=:regSeq and rg.crnt_rec_flg=1")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String activePortObjQry;
        activePortObjQry = readFile(Charset.defaultCharset(), "ConsolidatedLoanReportActivePortObjQry.txt");

        Query rSet = em.createNativeQuery(activePortObjQry).setParameter("brnch_seq", brnchSeq)
                .setParameter("frm_dt", frmDt).setParameter("to_dt", toDt);

        List<Object[]> activePortObj = rSet.getResultList();

        List<Map<String, ?>> activePorts = new ArrayList();
        activePortObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("EMP_NM", l[1] == null ? "" : l[1].toString());
            map.put("NAME", l[2] == null ? "" : l[2].toString());
            map.put("NOM_NM", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_ID", l[4] == null ? "" : l[4].toString());
            map.put("LOAN_CYCL_NUM", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("PRD_CMNT", l[6] == null ? "" : l[6].toString());
            map.put("PH_NUM", l[7] == null ? "" : l[7].toString());
            map.put("ADDR", l[8] == null ? "" : l[8].toString());
            map.put("LST_LOAN_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("CMP_DT", l[10] == null ? "" : l[10].toString());
            map.put("LOAN_APP_STS_DT", l[11] == null ? "" : l[11].toString());
            map.put("DAYS_CMP", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OD_INST", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("TAG_STS", l[14] == null ? "" : l[14].toString());
            activePorts.add(map);
        });

        return reportComponent.generateReport(CONSOLIDATED_LOAN, params, getJRDataSource(activePorts));
    }

    public byte[] getRORReport(String asDt, String userId, long rpt_flg, long areaSeq, long regSeq, long brnchSeq)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em
                    .createNativeQuery(
                            "select rg.reg_nm,rg.reg_seq from mw_reg rg where rg.reg_seq=:regSeq and rg.crnt_rec_flg=1")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }

        DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date = new Date();
        try {
            date = inputFormat.parse(asDt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int asOfDateMonth = cal.get(Calendar.MONTH) + 1;
        int asOfDateYear = cal.get(Calendar.YEAR);

        if ((asOfDateMonth >= currentMonth) && (asOfDateYear >= currentYear)) {
            // Convert Date to Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, -1);

            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            asDt = inputFormat.format(calendar.getTime());
        }

        params.put("curr_user", userId);
        params.put("as_dt", asDt);

        String graphQry;
        graphQry = readFile(Charset.defaultCharset(), "RORReportGraphQry.txt");

        Query rSet = em.createNativeQuery(graphQry).setParameter("toDt", asDt).setParameter("brnch_seq", brnchSeq);

        List<Object[]> graphObj = rSet.getResultList();

        List<Map<String, ?>> graph = new ArrayList();
        graphObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : new BigDecimal(l[0].toString()).longValue());
            map.put("ROR_MONTH", l[1] == null ? "" : l[1].toString());
            map.put("COMP", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("REP", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("ROR_PERC", l[4] == null ? 0 : new BigDecimal(l[4].toString()).doubleValue());
            graph.add(map);
        });

        String prdQry;
        prdQry = readFile(Charset.defaultCharset(), "RORReportPrdQry.txt");

        Query rs = em.createNativeQuery(prdQry).setParameter("toDt", asDt).setParameter("brnch_seq", brnchSeq);

        List<Object[]> prdObj = rs.getResultList();

        List<Map<String, ?>> prds = new ArrayList();
        prdObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : new BigDecimal(l[0].toString()).longValue());
            map.put("ROR_MONTH", l[1] == null ? "" : l[1].toString());
            map.put("PRD_GRP_NM", l[2] == null ? "" : l[2].toString());
            map.put("COMP", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("REP", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("ROR_PERC", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());
            prds.add(map);
        });
        params.put("prds", getJRDataSource(prds));

        String bdoQry;
        bdoQry = readFile(Charset.defaultCharset(), "RORReportBdoQry.txt");

        Query rs1 = em.createNativeQuery(bdoQry).setParameter("toDt", asDt).setParameter("brnch_seq", brnchSeq);

        List<Object[]> bdoObj = rs1.getResultList();

        List<Map<String, ?>> bdos = new ArrayList();
        bdoObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : new BigDecimal(l[0].toString()).longValue());
            map.put("ROR_MONTH", l[1] == null ? "" : l[1].toString());
            map.put("EMP_NM", l[2] == null ? "" : l[2].toString());
            map.put("COMP", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("REP", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("ROR_PERC", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());
            bdos.add(map);
        });
        params.put("bdos", getJRDataSource(bdos));

        return reportComponent.generateReport(ROR, params, getJRDataSource(graph));
    }

    public byte[] getLoanUtilizationReport(String frmDt, String toDt, String userId, long rpt_flg, long areaSeq,
                                           long regSeq, long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();

        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em
                    .createNativeQuery(
                            "select rg.reg_nm,rg.reg_seq from mw_reg rg where rg.reg_seq=:regSeq and rg.crnt_rec_flg=1")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String graphQry;
        graphQry = readFile(Charset.defaultCharset(), "LoanUtilizationReportGraphQry.txt");

        Query rs1 = em.createNativeQuery(graphQry).setParameter("user_id", userId).setParameter("frm_dt", frmDt)
                .setParameter("to_dt", toDt).setParameter("brnchSeq", brnchSeq);
        List<Object[]> graphObj = rs1.getResultList();

        List<Map<String, ?>> graph = new ArrayList();
        graphObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("GRP_NM", l[0] == null ? "" : l[0].toString());
            map.put("DSCR", l[1] == null ? "" : l[1].toString());
            map.put("CLNT_CNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            graph.add(map);
        });

        params.put("loanuti", getJRDataSource(graph));

        String bdoQry;
        bdoQry = readFile(Charset.defaultCharset(), "LoanUtilizationReportQry.txt");

        Query rs2 = em.createNativeQuery(bdoQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("brnch_seq", brnchSeq);
        List<Object[]> bdoObj = rs2.getResultList();

        List<Map<String, ?>> bdosList = new ArrayList();
        bdoObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_NM", l[0] == null ? "" : l[0].toString());
            map.put("CLNT_SEQ", l[1] == null ? "" : l[1].toString());
            map.put("CLNT_NM", l[2] == null ? "" : l[2].toString());
            map.put("PH_NUM", l[3] == null ? "" : l[3].toString());
            map.put("NOMINEE_NM", l[4] == null ? "" : l[4].toString());
            map.put("NOMINEE_PH_NUM", l[5] == null ? "" : l[5].toString());
            map.put("LOAN_CYCL_NUM", l[6] == null ? "" : l[6].toString());
            map.put("PRD_GRP_NM", l[7] == null ? "" : l[7].toString());
            map.put("DAYS_MISSED", l[8] == null ? "" : l[8].toString());

            bdosList.add(map);
        });

        params.put("bdos", getJRDataSource(bdosList));

        return reportComponent.generateReport(LOAN_UTI_FORM, params, null);
    }

    public byte[] getMonthlyStatusReport(String frmDt, String toDt, String userId, long rpt_flg) throws IOException {
        Map<String, Object> params = new HashMap<>();
        if (rpt_flg == 1) {

            Query bi = em.createNativeQuery(Queries.BRANCH_INFO).setParameter("userId", userId);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (rpt_flg == 2) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO).setParameter("userId", userId);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());

        }

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String graphQry;
        graphQry = readFile(Charset.defaultCharset(), "MonthlyStatusReportGraphQry.txt");

        Query rs1 = em.createNativeQuery(graphQry).setParameter("user_id", userId).setParameter("frm_dt", frmDt)
                .setParameter("to_dt", toDt).setParameter("rpt_flg", rpt_flg);

        List<Object[]> graphObj = rs1.getResultList();

        List<Map<String, ?>> graph = new ArrayList();
        graphObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_NM", l[0] == null ? "" : l[0].toString());
            map.put("ACH_CLNT", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("ACH_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("TRGT_CLNTS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("TRGT_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("ROR", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("FP", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("ADS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("ADV_MAT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            graph.add(map);
        });
        return reportComponent.generateReport(MONTHLY_STATUS, params, getJRDataSource(graph));
    }

    public byte[] getPortfolioAtRiskTimeSerise(String asDt, String userId, long rpt_flg, long areaSeq, long regSeq, long brnchSeq)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ).setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em.createNativeQuery("select rg.reg_nm,rg.reg_seq from mw_reg rg where rg.reg_seq=:regSeq and rg.crnt_rec_flg=1")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }

        DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date = new Date();
        try {
            date = inputFormat.parse(asDt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int asOfDateMonth = cal.get(Calendar.MONTH) + 1;
        int asOfDateYear = cal.get(Calendar.YEAR);

        if ((asOfDateMonth >= currentMonth) && (asOfDateYear >= currentYear)) {
            // Convert Date to Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, -1);

            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            asDt = inputFormat.format(calendar.getTime());
        }

        params.put("curr_user", userId);
        params.put("as_dt", asDt);

        String graphQry;
        graphQry = readFile(Charset.defaultCharset(), "PortfolioAtRiskTimeSeriseGraphQry.txt");

        Query rs1 = em.createNativeQuery(graphQry).setParameter("todt", asDt)
                .setParameter("brnch_seq", brnchSeq);

        List<Object[]> graphObj = rs1.getResultList();
        List<Map<String, ?>> graph = new ArrayList();
        graphObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PAR_ORDR", l[0] == null ? "" : l[0].toString());
            map.put("PAR_MONTH", l[1] == null ? "" : l[1].toString());
            map.put("PAR_PERC", l[2] == null ? 0 : new BigDecimal(l[2].toString()).doubleValue());
            map.put("PAR_PERC1", l[3] == null ? 0 : new BigDecimal(l[3].toString()).doubleValue());
            graph.add(map);
        });


        String prdQry;
        prdQry = readFile(Charset.defaultCharset(), "PortfolioAtRiskTimeSerisePrdQry.txt");

        Query rs = em.createNativeQuery(prdQry).setParameter("todt", asDt)
                .setParameter("brnch_seq", brnchSeq);

        List<Object[]> prdObj = rs.getResultList();
        List<Map<String, ?>> prds = new ArrayList();
        List<Map<String, ?>> bdos = new ArrayList();

        prdObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : l[0].toString());
            map.put("PAR_ORDR", l[1] == null ? "" : l[1].toString());
            map.put("PAR_MONTH", l[2] == null ? "" : l[2].toString());
            map.put("PRD_GRP_NM", l[3] == null ? "" : l[3].toString());
            map.put("TOT_CLNT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OST_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("PAR_OVER_30_DAY_CNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("PAR_OVER_30_DAY_OD_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("PAR_OVER_30_AMT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("PAR_PERC", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());

            map.put("PAR_OVER_1_DAY_CNT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PAR_OVER_1_DAY_OD_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("PAR_OVER_1_AMT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("PAR_PERC1", l[13] == null ? 0 : new BigDecimal(l[13].toString()).doubleValue());

            if (map.get("ORD").equals("1")) {
                prds.add(map);
            } else {
                bdos.add(map);
            }
        });
        params.put("prds", getJRDataSource(prds));
        params.put("bdos", getJRDataSource(bdos));

        return reportComponent.generateReport(PORTFOLIO_RISK, params, getJRDataSource(graph));
    }

    public byte[] getFemaleParticipationRatio(String asDt, String userId, long rpt_flg, long areaSeq, long regSeq,
                                              long brnchSeq) throws IOException {

        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());

        DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date = new Date();
        try {
            date = inputFormat.parse(asDt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int asOfDateMonth = cal.get(Calendar.MONTH) + 1;
        int asOfDateYear = cal.get(Calendar.YEAR);

        if ((asOfDateMonth >= currentMonth) && (asOfDateYear >= currentYear)) {
            // Convert Date to Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, -1);

            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            asDt = inputFormat.format(calendar.getTime());
        }

        params.put("curr_user", userId);
        params.put("as_dt", asDt);

        String graphQry;
        graphQry = readFile(Charset.defaultCharset(), "FemaleParticipationRatioGraphQry.txt");
        Query resSet = em.createNativeQuery(graphQry).setParameter("todt", asDt).setParameter("brnch_seq", brnchSeq);

        List<Object[]> graphObj = resSet.getResultList();

        List<Map<String, ?>> graph = new ArrayList();
        graphObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("SORTORD", l[0] == null ? "" : new BigDecimal(l[0].toString()).longValue());
            map.put("RPT_MONTH", l[1] == null ? 0 : l[1].toString());
            map.put("TOTAL", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("NEW", l[3] == null ? 0 : new BigDecimal(l[3].toString()).doubleValue());
            map.put("REPEAT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).doubleValue());
            graph.add(map);

        });

        String userQry;
        userQry = readFile(Charset.defaultCharset(), "FemaleParticipationRatioUserQry.txt");
        Query ret = em.createNativeQuery(userQry).setParameter("todt", asDt).setParameter("brnch_seq", brnchSeq);

        List<Object[]> userObj = ret.getResultList();

        List<Map<String, ?>> users = new ArrayList();
        userObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("SORTORD", l[0] == null ? "" : new BigDecimal(l[0].toString()).longValue());
            map.put("RPT_MONTH", l[1] == null ? "" : l[1].toString());
            map.put("MALE_NEW", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("FEMALE_NEW", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("JOINT_USER_NEW", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("MALE_REP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("FEMALE_REP", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("JOINT_USER_REP", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());

            users.add(map);
        });
        params.put("users", getJRDataSource(users));

        return reportComponent.generateReport(FEMALE_PARTICIPATION_RATIO, params, getJRDataSource(graph));
    }

    public byte[] getBranchTargetManagementTool(String asDt, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO).setParameter("userId", userId);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("as_dt", asDt);

        String targQry;
        targQry = readFile(Charset.defaultCharset(), "BranchTargetManagementTargQry.txt");
        Query resSet = em.createNativeQuery(targQry).setParameter("user_id", userId).setParameter("as_dt", asDt);

        List<Object[]> targObj = resSet.getResultList();

        List<Map<String, ?>> targets = new ArrayList();
        targObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_NM", l[0] == null ? "" : l[0].toString());
            map.put("TRGT_CLNTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("TRGT_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            targets.add(map);
        });

        return reportComponent.generateReport(BRANCH_TARGET_MANG, params, getJRDataSource(targets));
    }

    public byte[] getPortfolioStatusActiveFromDateReport(String frmDt, String toDt, String userId, long rpt_flg,
                                                         long areaSeq, long regSeq, long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO).setParameter("userId", userId);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("frm_dt", frmDt);

        String activePortObjQry;
        activePortObjQry = readFile(Charset.defaultCharset(), "PortfolioStatusActiveDurationActivePortObjQry.txt");
        Query resSet = em.createNativeQuery(activePortObjQry).setParameter("user_id", userId)
                .setParameter("to_dt", toDt).setParameter("frm_dt", frmDt).setParameter("rpt_flg", rpt_flg)
                .setParameter("areaSeq", areaSeq).setParameter("regSeq", regSeq).setParameter("brnchSeq", brnchSeq);

        List<Object[]> activePortObj = resSet.getResultList();

        List<Map<String, ?>> activePorts = new ArrayList();
        activePortObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
            map.put("TOT_CLNT", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("TOT_CLNT_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("NEW_CLNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("NEW_CLNT_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("RPT_CLNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("RPT_CLNT_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("RNW_CLNT_MONTH", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("CMPLTD_MNTH", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("DSBMT_MNTH", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            activePorts.add(map);
        });

        String bdoQry;
        bdoQry = readFile(Charset.defaultCharset(), "PortfolioStatusActiveDurationActivePortBdoQry.txt");
        Query rSet = em.createNativeQuery(bdoQry).setParameter("user_id", userId).setParameter("to_dt", toDt)
                .setParameter("frm_dt", frmDt).setParameter("rpt_flg", rpt_flg).setParameter("areaSeq", areaSeq)
                .setParameter("regSeq", regSeq).setParameter("brnchSeq", brnchSeq);
        ;

        List<Object[]> bdoObj = rSet.getResultList();

        List<Map<String, ?>> bdos = new ArrayList();
        bdoObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_NM", l[0] == null ? "" : l[0].toString());
            map.put("TOT_CLNT", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("TOT_CLNT_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("NEW_CLNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("NEW_CLNT_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("RPT_CLNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("RPT_CLNT_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("RNW_CLNT_MONTH", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("CMPLTD_MNTH", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("DSBMT_MNTH", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            bdos.add(map);
        });
        params.put("bdos", getJRDataSource(bdos));
        return reportComponent.generateReport(PORTFOLIO_STAT_FROM, params, getJRDataSource(activePorts));
    }

    public byte[] getMcbChequeInfoReport(String frmDt, String toDt) throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("to_dt", toDt);
        params.put("frm_dt", frmDt);

        String targQry;
        targQry = readFile(Charset.defaultCharset(), "McbChequeInfoReport.txt");
        Query set = em.createNativeQuery(targQry).setParameter("to_dt", toDt).setParameter("frm_dt", frmDt);

        List<Object[]> targObj = set.getResultList();

        List<Map<String, ?>> targets = new ArrayList();
        targObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("VDATE", l[0] == null ? "" : l[0].toString());
            map.put("LOAN_APP_SEQ", l[1] == null ? "" : l[1].toString());
            map.put("CNIC_NUM", l[2] == null ? "" : l[2].toString());
            map.put("CNIC_EXPRY_DT", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_SEQ", l[4] == null ? "" : l[4].toString());
            map.put("BR_NAME", l[5] == null ? "" : l[5].toString());
            map.put("NAME", l[6] == null ? "" : l[6].toString());
            map.put("DISB_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("BANK", l[8] == null ? "" : l[8].toString());
            map.put("BANK_CODE", l[9] == null ? "" : l[9].toString());
            map.put("BANK_BRNCH", l[10] == null ? "" : l[10].toString());
            targets.add(map);
        });

        return reportComponent.generateReport(MCB_CHEQUE, params, getJRDataSource(targets));
    }

    public byte[] getBOPInfoReport(String frmDt, String toDt) throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("to_dt", toDt);
        params.put("frm_dt", frmDt);

        String targQry;
        targQry = readFile(Charset.defaultCharset(), "BOPInfoReport.txt");
        Query set = em.createNativeQuery(targQry).setParameter("to_dt", toDt).setParameter("frm_dt", frmDt);

        List<Object[]> targObj = set.getResultList();

        List<Map<String, ?>> targets = new ArrayList();
        targObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("VDATE", l[0] == null ? "" : l[0].toString());
            map.put("LOAN_APP_SEQ", l[1] == null ? "" : l[1].toString());
            map.put("CNIC_NUM", l[2] == null ? "" : l[2].toString());
            map.put("CNIC_EXPRY_DT", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_SEQ", l[4] == null ? "" : l[4].toString());
            map.put("BR_NAME", l[5] == null ? "" : l[5].toString());
            map.put("NAME", l[6] == null ? "" : l[6].toString());
            map.put("DISB_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("BANK", l[7] == null ? "" : l[7].toString());
            map.put("BANK_CODE", l[8] == null ? "" : l[8].toString());
            map.put("BANK_BRNCH", l[9] == null ? "" : l[9].toString());
            targets.add(map);
        });

        return reportComponent.generateReport(BOP_INFO, params, getJRDataSource(targets));
    }

    public byte[] getEPAndRemReport(String frmDt, String toDt, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO).setParameter("userId", userId);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String remQry;
        remQry = readFile(Charset.defaultCharset(), "EPAndRemReport.txt");
        Query set = em.createNativeQuery(remQry).setParameter("userid", userId).setParameter("frmdt", frmDt)
                .setParameter("todt", toDt);

        List<Object[]> graphObj = set.getResultList();

        List<Map<String, ?>> remittance = new ArrayList();
        graphObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("MON", l[0] == null ? "" : l[0].toString());
            map.put("BNK_CLNT_CNT", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("BNK_CLNT_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("EP_CNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("EP_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            remittance.add(map);
        });
        return reportComponent.generateReport(REM_REPORT, params, getJRDataSource(remittance));
    }

    public byte[] getFundsReport(String frmDt, String toDt, String userId, long brnch, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String fundTraQry;
        fundTraQry = readFile(Charset.defaultCharset(), "FundsReport.txt");
        Query set = em.createNativeQuery(fundTraQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("brnch", brnch);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> funds = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("EXP_DATE", l[3] == null ? "" : l[3].toString());
            map.put("EXP_TYP", l[4] == null ? "" : l[4].toString());
            map.put("EXP_SEQ", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("EXP_REF", l[6] == null ? "" : l[6].toString());
            map.put("NARRATION", l[7] == null ? "" : l[7].toString());
            map.put("INSTR_NUM", l[8] == null ? "" : l[8].toString());
            map.put("PYMT_TYP", l[9] == null ? "" : l[9].toString());
            map.put("RECPT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PYMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            funds.add(map);
        });
        return reportComponent.generateReport(FUND_TRANSFER_REPORT, params, getJRDataSource(funds), isXls);
    }

    public byte[] getDisbursementReport(String frmDt, String toDt, String userId, long brnch, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "DisbursementReport.txt");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("brnch", brnch);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("TYP_STR", l[3] == null ? "" : l[3].toString());
            map.put("MON", l[4] == null ? "" : l[4].toString());
            map.put("BIZ_SECT", l[5] == null ? "" : l[5].toString());
            map.put("PRD_GRP_NM", l[6] == null ? "" : l[6].toString());
            map.put("NO_OF_LOANS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("NO_OF_CLNTS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("DISB_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            disbursements.add(map);
        });
        return reportComponent.generateReport(DISBURSEMENT_REPORT, params, getJRDataSource(disbursements), isXls);
    }

    public byte[] getRecoveryReport(String frmDt, String toDt, String userId, boolean isXls) throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String recoveryQry;
        recoveryQry = readFile(Charset.defaultCharset(), "RecoveryReport.txt");
        Query set = em.createNativeQuery(recoveryQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> recovery = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("TYP_STR", l[3] == null ? "" : l[3].toString());
            map.put("PRD_GRP_NM", l[4] == null ? "" : l[4].toString());
            map.put("PYMT_DT", l[5] == null ? "" : l[5].toString());
            map.put("CHRG_TYP", l[5] == null ? "" : l[5].toString());
            map.put("RCVRD_CLNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("RCVRD_AMT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            recovery.add(map);
        });
        return reportComponent.generateReport(RECOVERY_REPORT, params, getJRDataSource(recovery), isXls);
    }

    public byte[] getFundManagmentReport(String frmDt, String toDt, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String recoveryQry;
        recoveryQry = readFile(Charset.defaultCharset(), "FundManagmentRecoveryQry.txt");
        Query set = em.createNativeQuery(recoveryQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> recovery = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSBMT_DT", l[0] == null ? "" : l[0].toString());
            map.put("TYP_STR", l[1] == null ? "" : l[1].toString());
            map.put("MON", l[2] == null ? "" : l[2].toString());
            map.put("NO_OF_LOANS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("NO_OF_CLNTS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("DISB_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            recovery.add(map);
        });
        String cashlessQry;
        cashlessQry = readFile(Charset.defaultCharset(), "FundManagmentRecoveryQry.txt");
        Query rs = em.createNativeQuery(cashlessQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> cashLessObj = rs.getResultList();

        List<Map<String, ?>> cashLess = new ArrayList();
        cashLessObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSBMT_DT", l[0] == null ? "" : l[0].toString());
            map.put("TYP_STR", l[1] == null ? "" : l[1].toString());
            map.put("MON", l[2] == null ? "" : l[2].toString());
            map.put("NO_OF_LOANS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("NO_OF_CLNTS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("DISB_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            cashLess.add(map);
        });
        params.put("CASH_LESS", getJRDataSource(cashLess));
        return reportComponent.generateReport(REM_RATIO_REP, params, getJRDataSource(recovery));
    }

    public byte[] getOrganizationTaggingReport(String frmDt, String toDt, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String recoveryQry;
        recoveryQry = readFile(Charset.defaultCharset(), "OrganizationTaggingReport.txt");
        Query rs = em.createNativeQuery(recoveryQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> Obj = rs.getResultList();

        List<Map<String, ?>> recovery = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_SEQ", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_NAME", l[4] == null ? "" : l[4].toString());
            map.put("LOAN_APP_SEQ", l[5] == null ? "" : l[5].toString());
            map.put("BIZ_SECT", l[6] == null ? "" : l[6].toString());
            map.put("PRD_GRP_NM", l[7] == null ? "" : l[7].toString());
            map.put("LOAN_CYCL_NUM", l[8] == null ? "" : l[8].toString());
            map.put("TYP_STR", l[9] == null ? "" : l[9].toString());
            map.put("DISB_DT", l[10] == null ? "" : l[10].toString());
            map.put("DISB_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("DOB", l[12] == null ? "" : l[12].toString());
            map.put("GNDR", l[13] == null ? "" : l[13].toString());
            recovery.add(map);
        });
        return reportComponent.generateReport(ORG_TAGGING_REPORT, params, getJRDataSource(recovery));
    }

    public byte[] getProductWiseDisbursementReport(String frmDt, String toDt, Long prd, String userId)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        String prdString = "";
        if (prd == 0) {
            prdString = " and grp.PRD_GRP_SEQ=:prd\r\n";
        }

        String query;
        query = (readFile(Charset.defaultCharset(), "ProductWiseDisbursementReport.txt")).replace("prdString",
                prdString);
        // query.replaceAll( "prdString", prdString );
        Query recoveryQry = em.createNativeQuery(query).setParameter("frmdt", frmDt).setParameter("todt", toDt);
        if (prd == 0) {
            recoveryQry.setParameter("prd", prd);
        }

        List<Object[]> Obj = recoveryQry.getResultList();

        List<Map<String, ?>> recovery = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSBMT_DT", l[0] == null ? "" : l[0].toString());
            map.put("MON", l[1] == null ? "" : l[1].toString());
            map.put("TYP_STR", l[2] == null ? "" : l[2].toString());
            map.put("PRD_GRP_NM", l[3] == null ? "" : l[3].toString());
            map.put("NO_OF_LOANS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("NO_OF_CLNTS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DISB_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            recovery.add(map);
        });
        return reportComponent.generateReport(PRODUCT_WISE_DISBURSEMENT, params, getJRDataSource(recovery));
    }

    public byte[] getOrganizationTagTimeReport(String frmDt, String toDt, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String recoveryQry;

        recoveryQry = readFile(Charset.defaultCharset(), "OrganizationTagTimeReport.txt");
        Query rs = em.createNativeQuery(recoveryQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("userid", userId);

        List<Object[]> Obj = rs.getResultList();

        List<Map<String, ?>> recovery = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("DISB_DT", l[3] == null ? "" : l[3].toString());
            map.put("PRD_GRP_NM", l[4] == null ? "" : l[4].toString());
            map.put("TYP_STR", l[5] == null ? "" : l[5].toString());
            map.put("CLNTS", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("DISB_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            recovery.add(map);
        });
        return reportComponent.generateReport(ORG_TAGGING_TIME_REPORT, params, getJRDataSource(recovery));
    }

    public byte[] getReversalEnteriesReport(String frmdt, String todt, long brnch, String user) throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);
        String ql;
        ql = readFile(Charset.defaultCharset(), "ReversalEnteries.txt");
        Query rs = em.createNativeQuery(ql).setParameter("frmdt", frmdt).setParameter("todt", todt)
                .setParameter("brnch", brnch);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("GL_ACCT_NUM", w[0] == null ? "" : w[0].toString());
            parm.put("TYP_STR", w[1] == null ? "" : w[1].toString());
            parm.put("JV_TYPE", w[2] == null ? "" : w[2].toString());
            parm.put("CLIENT_ID", w[3] == null ? "" : w[3].toString());
            // parm.put( "CLIENT_ID", w[ 3 ] == null ? 0 : new BigDecimal( w[ 3 ].toString()
            // ).longValue() );
            parm.put("INSTR_NO", w[4] == null ? "" : w[4].toString());
            parm.put("ENTY_TYP", w[5] == null ? "" : w[5].toString());
            parm.put("JV_DSCR", w[6] == null ? "" : w[6].toString());
            parm.put("ACTUAL_JV_HDR_SEQ", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("ACTUAL_REF", w[8] == null ? 0 : new BigDecimal(w[8].toString()).longValue());
            parm.put("ACTUAL_VALUE_DT", w[9] == null ? "" : w[9].toString());
            parm.put("ACTUAL_DEBIT", w[10] == null ? 0 : new BigDecimal(w[10].toString()).longValue());
            parm.put("ACTUAL_CREDIT", w[11] == null ? 0 : new BigDecimal(w[11].toString()).longValue());
            parm.put("JV_HDR_SEQ", w[12] == null ? 0 : new BigDecimal(w[12].toString()).longValue());
            parm.put("REV_REF_KEY", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());
            parm.put("REVERSAL_DT", w[14] == null ? "" : w[14].toString());
            parm.put("REV_DEBIT", w[15] == null ? 0 : new BigDecimal(w[15].toString()).longValue());
            parm.put("REV_CREDIT", w[16] == null ? 0 : new BigDecimal(w[16].toString()).longValue());
            expList.add(parm);
        });

        return reportComponent.generateReport(REVRSAL_ENTERIES_REPORT, params, getJRDataSource(expList));
    }

    public byte[] getTrailBalance(String frmdt, String todt, long brnch, String user) throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);
        String ql;
        ql = readFile(Charset.defaultCharset(), "Trail_Balance_Report.txt");
        Query rs = em.createNativeQuery(ql).setParameter("frmdt", frmdt).setParameter("todt", todt)
                .setParameter("brnch", brnch);

        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("ACCOUNT_CODE", w[0] == null ? "" : w[0].toString());
            parm.put("LAGACY_CODE", w[1] == null ? "" : w[1].toString());
            parm.put("LAGACY_DESC", w[2] == null ? "" : w[2].toString());
            parm.put("DEBIT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("CREDI", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            expList.add(parm);
        });

        return reportComponent.generateReport(TRAIL_BALANCE_REPORT, params, getJRDataSource(expList));
    }

    public byte[] getBrnchRnkngReport(long vstseq, String user) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        String ql;
        ql = readFile(Charset.defaultCharset(), "BranchRanking.txt");
        Query rs = em.createNativeQuery(ql).setParameter("vstseq", vstseq);

        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REF_CD_DSCR", w[0] == null ? "" : w[0].toString());
            parm.put("CTGRY_NM", w[1] == null ? "" : w[1].toString());
            parm.put("CTGRY_CMNT", w[2] == null ? "" : w[2].toString());
            parm.put("CTGRY_SCR", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("VST_SCR", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("COUNTKEYS", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("ACHVD", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            parm.put("ISU_NM", w[7] == null ? "" : w[7].toString());
            parm.put("ISU_STS", w[8] == null ? "" : w[8].toString());
            parm.put("ISU_CMNT", w[9] == null ? "" : w[9].toString());

            expList.add(parm);
        });

        return reportComponent.generateReport(BRANCH_RANKING, params, getJRDataSource(expList));
    }

    public byte[] getCpcReport(long vstseq, String user) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        String ql;
        ql = readFile(Charset.defaultCharset(), "CpcReport.txt");
        Query rs = em.createNativeQuery(ql).setParameter("vstseq", vstseq);

        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("CLNT_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_ID", w[1] == null ? "" : w[1].toString());
            parm.put("BDO_NM", w[2] == null ? "" : w[2].toString());
            parm.put("CLNT_CTGRY", w[3] == null ? "" : w[3].toString());
            parm.put("DT_OF_VST", w[4] == null ? "" : w[4].toString());
            parm.put("QST_STR", w[5] == null ? "" : w[5].toString());
            parm.put("ANSWR_STR", w[6] == null ? "" : w[6].toString());
            parm.put("CMNT", w[7] == null ? "" : w[7].toString());
            expList.add(parm);
        });

        return reportComponent.generateReport(CPC_REPORT, params, getJRDataSource(expList));
    }

    public byte[] getMonthlyRanking(long vst_seq, String user) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        String ql;
        ql = readFile(Charset.defaultCharset(), "MonthyRankReport.txt");
        Query rs = em.createNativeQuery(ql).setParameter("vst_seq", vst_seq);

        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REG_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_ID", w[1] == null ? "" : w[1].toString());
            parm.put("VSTD_CLNTS", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("REF_CD_DSCR", w[3] == null ? "" : w[3].toString());
            parm.put("SCR", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("REF_CD_DSCR", w[5] == null ? "" : w[5].toString());
            expList.add(parm);
        });

        return reportComponent.generateReport(MONTHLY_RANKING, params, getJRDataSource(expList));
    }

    public byte[] getacigReport(long trng_seq, String user) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        String ql;
        ql = readFile(Charset.defaultCharset(), "ACIGREPORT.txt");
        Query rs = em.createNativeQuery(ql).setParameter("trng_seq", trng_seq);

        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("TRN_DT", w[0] == null ? "" : w[0].toString());
            parm.put("REG_NM", w[1] == null ? "" : w[1].toString());
            parm.put("AREA_NM", w[2] == null ? "" : w[2].toString());
            parm.put("TRNR_NM", w[3] == null ? "" : w[3].toString());
            parm.put("BRNCH_NM", w[4] == null ? "" : w[4].toString());
            parm.put("PRTCPNT_ID", w[5] == null ? "" : w[5].toString());
            parm.put("PRTCPNT_CNIC_NUM", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            parm.put("GROUP1", w[7] == null ? "" : w[7].toString());
            parm.put("PRTCPNT_NM", w[8] == null ? "" : w[8].toString());
            parm.put("RELATION", w[9] == null ? "" : w[9].toString());
            parm.put("trng_STS_KEY", w[10] == null ? "" : w[10].toString().equals("1") ? "InActive" : "Active");

            expList.add(parm);
        });

        return reportComponent.generateReport(AICG_TRAINING, params, getJRDataSource(expList));
    }

    public byte[] getGesaSFE(long trng_seq, String user) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        String ql;
        ql = readFile(Charset.defaultCharset(), "GESA.txt");
        Query rs = em.createNativeQuery(ql).setParameter("trng_seq", trng_seq);

        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("TRNR_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_ID", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_NM", w[2] == null ? "" : w[2].toString());
            parm.put("TRN_DT", w[3] == null ? "" : w[3].toString());

            expList.add(parm);
        });

        return reportComponent.generateReport(GESA, params, getJRDataSource(expList));
    }

    // Read Query files
    private String readFile(Charset encoding, String fileName) {

        String QUERY_FILE_PATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator
                + "reports" + file.separator + "queries" + file.separator;
        // String QUERY_FILE_PATH = file.separator + "opt" + file.separator + "reports"
        // + file.separator + "queries" + file.separator;

        //String QUERY_FILE_PATH = "D:\\GIT-MWX\\mw_rptsrvs\\src\\main\\resources\\reports\\queries\\";
        byte[] encoded = null;
        try {
            encoded = Files.readAllBytes(Paths.get(QUERY_FILE_PATH + fileName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new String(encoded, encoding);
    }

    public byte[] getPARMD(String toDt, String user) {
        Map<String, Object> params = new HashMap<>();

        // params.put( "frm_dt", frmDt );
        params.put("to_dt", toDt);

        // MD Report PAR Region wise
        Query regionWiseParQuery = em.createNativeQuery(readFile(Charset.defaultCharset(), "RegionWiseParQuery.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> regionWiseParRs = regionWiseParQuery.getResultList();

        List<Map<String, ?>> regionWiseParData = new ArrayList();
        regionWiseParRs.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            // parm.put( "REGION_NAME", w[ 0 ] == null ? "" : w[ 0 ].toString() );
            if (w[0].equals("AAOverAll")) {
                parm.put("REGION_NAME", w[0] == null ? "" : w[0].toString().substring(2));
            } else {
                parm.put("REGION_NAME", w[0] == null ? "" : w[0].toString());
            }
            parm.put("PAR_1_PERC", w[1] == null ? 0 : new BigDecimal(w[1].toString()).doubleValue());
            parm.put("PAR_11_PERC", w[2] == null ? 0 : new BigDecimal(w[2].toString()).doubleValue());
            parm.put("PAR_5_PERC", w[3] == null ? 0 : new BigDecimal(w[3].toString()).doubleValue());
            parm.put("PAR_51_PERC", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
            parm.put("PAR_15_PERC", w[5] == null ? 0 : new BigDecimal(w[5].toString()).doubleValue());
            parm.put("PAR_151_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
            parm.put("PAR_30_PERC", w[7] == null ? 0 : new BigDecimal(w[7].toString()).doubleValue());
            parm.put("PAR_301_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
            parm.put("PAR_45_PERC", w[9] == null ? 0 : new BigDecimal(w[9].toString()).doubleValue());
            parm.put("PAR_451_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
            parm.put("PAR_60_PERC", w[11] == null ? 0 : new BigDecimal(w[11].toString()).doubleValue());
            parm.put("PAR_601_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
            parm.put("PAR_90_PERC", w[13] == null ? 0 : new BigDecimal(w[13].toString()).doubleValue());
            parm.put("PAR_901_PERC", w[14] == null ? 0 : new BigDecimal(w[14].toString()).doubleValue());
            parm.put("PAR_91_PERC", w[15] == null ? 0 : new BigDecimal(w[15].toString()).doubleValue());
            parm.put("PAR_911_PERC", w[16] == null ? 0 : new BigDecimal(w[16].toString()).doubleValue());
            regionWiseParData.add(parm);
        });

        params.put("reg_data", getJRDataSource(regionWiseParData));

        // MD Report PAR Region wise
        Query prdWiseParQuery = em.createNativeQuery(readFile(Charset.defaultCharset(), "PrdWiseParQuery.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> prdWiseParRs = prdWiseParQuery.getResultList();

        List<Map<String, ?>> prdWiseParData = new ArrayList();
        prdWiseParRs.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            if (w[0].equals("AAOverall")) {
                parm.put("PRODUCT_DESC", w[0] == null ? "" : w[0].toString().substring(2));
            } else {
                parm.put("PRODUCT_DESC", w[0] == null ? "" : w[0].toString());
            }
            // parm.put( "PRODUCT_DESC", w[ 0 ] == null ? "" : w[ 0 ].toString() );
            parm.put("PAR_1_PERC", w[1] == null ? 0 : new BigDecimal(w[1].toString()).doubleValue());
            parm.put("PAR_11_PERC", w[2] == null ? 0 : new BigDecimal(w[2].toString()).doubleValue());
            parm.put("PAR_5_PERC", w[3] == null ? 0 : new BigDecimal(w[3].toString()).doubleValue());
            parm.put("PAR_51_PERC", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
            parm.put("PAR_15_PERC", w[5] == null ? 0 : new BigDecimal(w[5].toString()).doubleValue());
            parm.put("PAR_151_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
            parm.put("PAR_30_PERC", w[7] == null ? 0 : new BigDecimal(w[7].toString()).doubleValue());
            parm.put("PAR_301_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
            parm.put("PAR_45_PERC", w[9] == null ? 0 : new BigDecimal(w[9].toString()).doubleValue());
            parm.put("PAR_451_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
            parm.put("PAR_60_PERC", w[11] == null ? 0 : new BigDecimal(w[11].toString()).doubleValue());
            parm.put("PAR_601_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
            parm.put("PAR_90_PERC", w[13] == null ? 0 : new BigDecimal(w[13].toString()).doubleValue());
            parm.put("PAR_901_PERC", w[14] == null ? 0 : new BigDecimal(w[14].toString()).doubleValue());
            parm.put("PAR_91_PERC", w[15] == null ? 0 : new BigDecimal(w[15].toString()).doubleValue());
            parm.put("PAR_911_PERC", w[16] == null ? 0 : new BigDecimal(w[16].toString()).doubleValue());
            prdWiseParData.add(parm);
        });

        params.put("prd_data", getJRDataSource(prdWiseParData));

        // MD Report PAR Loan Cycle wise
        Query loanCycleParQuery = em.createNativeQuery(readFile(Charset.defaultCharset(), "LoanCycleParQuery.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> loanCycleParRs = loanCycleParQuery.getResultList();

        List<Map<String, ?>> loanCycleParData = new ArrayList();
        loanCycleParRs.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            if (w[0].toString().equals("0")) {
                parm.put("LOAN_CYCLE_CD", "Overall");
            } else if (w[0].toString().equals("99")) {
                parm.put("LOAN_CYCLE_CD", "Above 10");
            } else {
                parm.put("LOAN_CYCLE_CD", w[0] == null ? "" : w[0].toString());
            }
            // parm.put( "LOAN_CYCLE_CD", w[ 0 ] == null ? "" : w[ 0 ].toString() );
            parm.put("OD_P1", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("PAR_1_PERC", w[2] == null ? 0 : new BigDecimal(w[2].toString()).doubleValue());
            parm.put("OD_P5", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("PAR_5_PERC", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
            parm.put("OD_P15", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("PAR_15_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
            parm.put("OD_P30", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("PAR_30_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
            parm.put("OD_P45", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("PAR_45_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
            parm.put("OD_P60", w[11] == null ? 0 : new BigDecimal(w[11].toString()).longValue());
            parm.put("PAR_60_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
            parm.put("OD_P90", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());
            parm.put("PAR_90_PERC", w[14] == null ? 0 : new BigDecimal(w[14].toString()).doubleValue());
            parm.put("OD_P91", w[15] == null ? 0 : new BigDecimal(w[15].toString()).longValue());
            parm.put("PAR_91_PERC", w[16] == null ? 0 : new BigDecimal(w[16].toString()).doubleValue());
            loanCycleParData.add(parm);
        });

        params.put("lc_data", getJRDataSource(loanCycleParData));

        // MD Report PAR Amount wise
        Query amtWiseParQuery = em.createNativeQuery(readFile(Charset.defaultCharset(), "AmtWiseParQuery.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> amtWiseParRs = amtWiseParQuery.getResultList();

        List<Map<String, ?>> amtWiseParData = new ArrayList();
        amtWiseParRs.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            if (w[0].equals("AAOverall")) {
                parm.put("DISBURSED_AMOUNT", "Overall");
            } else if (w[0].equals("disb_4k")) {
                parm.put("DISBURSED_AMOUNT", "20,000 - 40,000");
            } else if (w[0].equals("disb_6k")) {
                parm.put("DISBURSED_AMOUNT", "41,000 - 60,000");
            } else if (w[0].equals("disb_8k")) {
                parm.put("DISBURSED_AMOUNT", "60,001 - 80,000");
            } else if (w[0].equals("disb_8kk")) {
                parm.put("DISBURSED_AMOUNT", "80,001 - 100,000");
            } else if (w[0].equals("disb_91k")) {
                parm.put("DISBURSED_AMOUNT", "Above 100,000");
            }
            parm.put("OD_P1", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("PAR_1_PERC", w[2] == null ? 0 : new BigDecimal(w[2].toString()).doubleValue());
            parm.put("OD_P5", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("PAR_5_PERC", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
            parm.put("OD_P15", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("PAR_15_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
            parm.put("OD_P30", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("PAR_30_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
            parm.put("OD_P45", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("PAR_45_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
            parm.put("OD_P60", w[11] == null ? 0 : new BigDecimal(w[11].toString()).longValue());
            parm.put("PAR_60_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
            parm.put("OD_P90", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());
            parm.put("PAR_90_PERC", w[14] == null ? 0 : new BigDecimal(w[14].toString()).doubleValue());
            parm.put("OD_P91", w[15] == null ? 0 : new BigDecimal(w[15].toString()).longValue());
            parm.put("PAR_91_PERC", w[16] == null ? 0 : new BigDecimal(w[16].toString()).doubleValue());
            amtWiseParData.add(parm);
        });

        params.put("amt_data", getJRDataSource(amtWiseParData));

        // MD Report PAR Loan USer wise
        Query loanUsrParQuery = em.createNativeQuery(readFile(Charset.defaultCharset(), "LoanUsrParQuery.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> loanUsrParRs = loanUsrParQuery.getResultList();

        List<Map<String, ?>> loanUsrParData = new ArrayList();
        loanUsrParRs.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("LOAN_USER", w[0] == null ? "" : w[0].toString());
            parm.put("OD_P1", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("PAR_1_PERC", w[2] == null ? 0 : new BigDecimal(w[2].toString()).doubleValue());
            parm.put("OD_P5", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("PAR_5_PERC", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
            parm.put("OD_P15", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("PAR_15_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
            parm.put("OD_P30", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("PAR_30_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
            parm.put("OD_P45", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("PAR_45_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
            parm.put("OD_P60", w[11] == null ? 0 : new BigDecimal(w[11].toString()).longValue());
            parm.put("PAR_60_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
            parm.put("OD_P90", w[13] == null ? 0 : new BigDecimal(w[13].toString()).longValue());
            parm.put("PAR_90_PERC", w[14] == null ? 0 : new BigDecimal(w[14].toString()).doubleValue());
            parm.put("OD_P91", w[15] == null ? 0 : new BigDecimal(w[15].toString()).longValue());
            parm.put("PAR_91_PERC", w[16] == null ? 0 : new BigDecimal(w[16].toString()).doubleValue());
            loanUsrParData.add(parm);
        });

        params.put("user_data", getJRDataSource(loanUsrParData));

        // MD Report PAR Reagion wise Od
        Query regOdParQuery = em.createNativeQuery(readFile(Charset.defaultCharset(), "RegOdParQuery.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> regOdParRs = regOdParQuery.getResultList();

        List<Map<String, ?>> regOdParData = new ArrayList();
        regOdParRs.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION_NAME", w[0] == null ? "" : w[0].toString());
            parm.put("ACTIVE_CLIENTS", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("REGULAR_OD_CLIENTS", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("OUTSTANDING", w[3] == null ? 0 : new BigDecimal(w[3].toString()).doubleValue());
            parm.put("REGULAR_OD_AMOUNT", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
            parm.put("PAR_1_PERC", w[5] == null ? 0 : new BigDecimal(w[5].toString()).doubleValue());
            parm.put("PAR_5_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
            parm.put("PAR_15_PERC", w[7] == null ? 0 : new BigDecimal(w[7].toString()).doubleValue());
            parm.put("PAR_30_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
            parm.put("PAR_45_PERC", w[9] == null ? 0 : new BigDecimal(w[9].toString()).doubleValue());
            parm.put("PAR_60_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
            parm.put("PAR_90_PERC", w[11] == null ? 0 : new BigDecimal(w[11].toString()).doubleValue());
            parm.put("PAR_91_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
            regOdParData.add(parm);
        });

        params.put("reg_od_data", getJRDataSource(regOdParData));

        // MD Report PAR Reagion wise branch
        Query regbrnchOdParQuery = em.createNativeQuery(readFile(Charset.defaultCharset(), "RegbrnchOdParQuery.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> regbrnchOdParRs = regbrnchOdParQuery.getResultList();

        List<Map<String, ?>> regbrnchOdParData = new ArrayList();
        regbrnchOdParRs.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION_NAME", w[0] == null ? "" : w[0].toString());
            parm.put("P_5", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("P_2", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("P_3", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("P_5_1", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("P_6", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            regbrnchOdParData.add(parm);
        });

        params.put("reg_od_brnch_data", getJRDataSource(regbrnchOdParData));

        // MD Report PAR Reagion wise branch 2
        Query regbrnchOdParQuery2 = em.createNativeQuery(readFile(Charset.defaultCharset(), "RegbrnchOdParQuery2.txt"))
                .setParameter("to_dt", toDt);

        List<Object[]> regbrnchOdParRs2 = regbrnchOdParQuery2.getResultList();

        List<Map<String, ?>> regbrnchOdParData2 = new ArrayList();
        regbrnchOdParRs2.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION_NAME", w[0] == null ? "" : w[0].toString());
            parm.put("P_1", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("P_2", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("P_3", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("P_4", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            regbrnchOdParData2.add(parm);
        });

        params.put("reg_od_brnch_data2", getJRDataSource(regbrnchOdParData2));

        return reportComponent.generateReport(PAR_REPORT, params, null);
    }

    public byte[] getAdvanceRecoveryReport(String frmdt, String todt, String user, long rpt_flg, long areaSeq,
                                           long regSeq, long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();
        /*
         * Query bi = em.createNativeQuery( com.idev4.rds.util.Queries.BRANCH_INFO
         * ).setParameter( "userId", user ); Object[] obj = ( Object[] )
         * bi.getSingleResult(); params.put( "reg_nm", obj[ 0 ].toString() );
         * params.put( "area_nm", obj[ 1 ].toString() ); params.put( "brnch_nm", obj[ 2
         * ].toString() ); params.put( "brnch_cd", obj[ 3 ].toString() );
         */
        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em.createNativeQuery("select rg.reg_nm from mw_reg rg where rg.reg_seq=:regSeq ")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);
        String ql;
        ql = readFile(Charset.defaultCharset(), "AdvanceRecovery.txt");
        Query rs = em.createNativeQuery(ql).setParameter("frmdt", frmdt).setParameter("todt", todt)
                .setParameter("user", user).setParameter("rpt_flg", rpt_flg).setParameter("areaSeq", areaSeq)
                .setParameter("regSeq", regSeq).setParameter("brnchSeq", brnchSeq);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("'ZZOVERALL'", w[0] == null ? "" : w[0].toString());
            parm.put("'ZZOVERALL_1'", w[1] == null ? "" : w[1].toString());
            parm.put("SM_DAY_CNT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("ADV_1_3", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("ADV_4_6", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("ADV_7_10", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("ADV_OVR_10", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            recList.add(parm);
        });

        return reportComponent.generateReport(ADVANCE_RECOVERY_REPORT, params, getJRDataSource(recList));
    }

    public byte[] getAdvanceClientReport(String frmdt, String todt, String user, long rpt_flg) throws IOException {
        Map<String, Object> params = new HashMap<>();
        /*
         * Query bi = em.createNativeQuery( com.idev4.rds.util.Queries.BRANCH_INFO
         * ).setParameter( "userId", user ); Object[] obj = ( Object[] )
         * bi.getSingleResult(); params.put( "reg_nm", obj[ 0 ].toString() );
         * params.put( "area_nm", obj[ 1 ].toString() ); params.put( "brnch_nm", obj[ 2
         * ].toString() ); params.put( "brnch_cd", obj[ 3 ].toString() );
         */
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);
        String ql;
        ql = readFile(Charset.defaultCharset(), "AdvClientRecovery.txt");
        Query rs = em.createNativeQuery(ql).setParameter("frmdt", frmdt).setParameter("todt", todt).setParameter("user",
                user);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("BRNCH_NM", w[0] == null ? "" : w[0].toString());
            parm.put("AREA_NM", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_NM", w[2] == null ? "" : w[2].toString());
            parm.put("CLNT_ID", w[3] == null ? "" : w[3].toString());
            parm.put("CLNT_CNTCT", w[4] == null ? "" : w[4].toString());
            parm.put("PRD_NM", w[5] == null ? "" : w[5].toString());
            parm.put("LOAN_CYCL_NUM", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            parm.put("APRVD_LOAN_AMT", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("INST_NUM", w[8] == null ? 0 : new BigDecimal(w[8].toString()).longValue());
            parm.put("OD_INS", w[9] == null ? 0 : new BigDecimal(w[9].toString()).longValue());
            parm.put("OST_AMT", w[10] == null ? 0 : new BigDecimal(w[10].toString()).longValue());
            recList.add(parm);
        });

        return reportComponent.generateReport(ADVANCE_CLIENT_REPORT, params, getJRDataSource(recList));
    }

    public byte[] getAdvanceMaturityReport(String as_dt, String user, long rpt_flg, long areaSeq, long regSeq,
                                           long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();
        /*
         * Query bi = em.createNativeQuery( com.idev4.rds.util.Queries.BRANCH_INFO
         * ).setParameter( "userId", user ); Object[] obj = ( Object[] )
         * bi.getSingleResult(); params.put( "reg_nm", obj[ 0 ].toString() );
         * params.put( "area_nm", obj[ 1 ].toString() ); params.put( "brnch_nm", obj[ 2
         * ].toString() ); params.put( "brnch_cd", obj[ 3 ].toString() );
         */
        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em.createNativeQuery("select rg.reg_nm from mw_reg rg where rg.reg_seq=:regSeq ")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }
        params.put("as_dt", as_dt);
        params.put("curr_user", user);
        String ql;
        ql = readFile(Charset.defaultCharset(), "AdvanceMaturity.txt");
        Query rs = em.createNativeQuery(ql).setParameter("as_dt", as_dt).setParameter("user", user)
                .setParameter("rpt_flg", rpt_flg).setParameter("areaSeq", areaSeq).setParameter("regSeq", regSeq)
                .setParameter("brnchSeq", brnchSeq);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();

            if (w[0].equals("zzzOverall")) {
                parm.put("GRP_NM", w[0] == null ? "" : w[0].toString().substring(3));
            } else {
                parm.put("GRP_NM", w[0] == null ? "" : w[0].toString());
            }

            parm.put("MAT_15_30", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("MAT_31_60", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("MAT_61_90", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("MAT_61_90_1", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            recList.add(parm);
        });

        return reportComponent.generateReport(ADVANCE_MATURITY_REPORT, params, getJRDataSource(recList));
    }

    public byte[] getWeeklyTargetReport(String as_dt, String userid, long rpt_flg, long areaSeq, long regSeq,
                                        long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();
        /*
         * Query bi = em.createNativeQuery( com.idev4.rds.util.Queries.BRANCH_INFO
         * ).setParameter( "userId", userid ); Object[] obj = ( Object[] )
         * bi.getSingleResult(); params.put( "reg_nm", obj[ 0 ].toString() );
         * params.put( "area_nm", obj[ 1 ].toString() ); params.put( "brnch_nm", obj[ 2
         * ].toString() ); params.put( "brnch_cd", obj[ 3 ].toString() );
         */

        if (brnchSeq != 0) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != 0) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ)
                    .setParameter("areaSeq", areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
        } else if (regSeq != 0) {
            Query bi = em.createNativeQuery("select rg.reg_nm from mw_reg rg where rg.reg_seq=:regSeq ")
                    .setParameter("regSeq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
        }

        params.put("as_dt", as_dt);
        params.put("curr_user", userid);
        String ql;
        ql = readFile(Charset.defaultCharset(), "WeeklyTarget.txt");
        Query rs = em.createNativeQuery(ql).setParameter("as_dt", as_dt).setParameter("userid", userid)
                .setParameter("rpt_flg", rpt_flg).setParameter("areaSeq", areaSeq).setParameter("regSeq", regSeq)
                .setParameter("brnchSeq", brnchSeq);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("AREA_NM", w[0] == null ? "" : w[0].toString());
            parm.put("BRNCH_NM", w[1] == null ? "" : w[1].toString());
            parm.put("WEEKS", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("PRD_GRP_NM", w[3] == null ? "" : w[3].toString());
            parm.put("ACHVD_CLNT", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("ACHVD_AMT", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            recList.add(parm);
        });

        return reportComponent.generateReport(WEEKLY_TARGET_REPORT, params, getJRDataSource(recList));
    }

    public byte[] getAreaDisbursReport(String as_dt, String userid, long rpt_flg, long areaSeq, long regSeq,
                                       long brnchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();
        /*
         * Query bi = em.createNativeQuery( com.idev4.rds.util.Queries.BRANCH_INFO
         * ).setParameter( "userId", userid ); Object[] obj = ( Object[] )
         * bi.getSingleResult(); params.put( "reg_nm", obj[ 0 ].toString() );
         * params.put( "area_nm", obj[ 1 ].toString() ); params.put( "brnch_nm", obj[ 2
         * ].toString() ); params.put( "brnch_cd", obj[ 3 ].toString() );
         */
        params.put("as_dt", as_dt);
        params.put("curr_user", userid);
        String ql;
        ql = readFile(Charset.defaultCharset(), "AreaDisbReport.txt");
        Query rs = em.createNativeQuery(ql).setParameter("as_dt", as_dt).setParameter("userid", userid)
                .setParameter("rpt_flg", rpt_flg).setParameter("areaSeq", areaSeq).setParameter("regSeq", regSeq)
                .setParameter("brnchSeq", brnchSeq);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("AREA_NM", w[0] == null ? "" : w[0].toString());
            parm.put("BRNCH_NM", w[1] == null ? "" : w[1].toString());
            parm.put("PRD_GRP_NM", w[2] == null ? "" : w[2].toString());
            parm.put("TRGT_CLNTS", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("ACHVD_CLNT", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("TRGT_AMT", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("ACHVD_AMT", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            recList.add(parm);
        });

        return reportComponent.generateReport(AREA_DISBURSEMENT_REPORT, params, getJRDataSource(recList));
    }

    public byte[] getKSSTraining(long asDt, String user) throws IOException {

        String ql;
        ql = readFile(Charset.defaultCharset(), "KSSTraining.txt");
        Query rs = em.createNativeQuery(ql).setParameter("rptMnth", asDt);

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        List<Object[]> result = rs.getResultList();
        List<Map<String, ?>> expList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("TRNR_NM", w[0] == null ? "" : w[0].toString());
            parm.put("REG_NM", w[1] == null ? "" : w[1].toString());
            parm.put("MNTH", w[2] == null ? "" : w[2].toString());
            parm.put("TRGT", w[3] == null ? "" : new BigDecimal(w[3].toString()).longValue());
            parm.put("ACHIV", w[4] == null ? "" : new BigDecimal(w[4].toString()).longValue());
            parm.put("RPT_SCH", w[5] == null ? "" : new BigDecimal(w[5].toString()).longValue());
            parm.put("NW_SCH", w[6] == null ? "" : new BigDecimal(w[6].toString()).longValue());
            parm.put("RFSD", w[7] == null ? "" : new BigDecimal(w[7].toString()).longValue());
            parm.put("BCK_LOG", w[8] == null ? "" : new BigDecimal(w[8].toString()).longValue());
            parm.put("TCHR_TRND", w[9] == null ? "" : new BigDecimal(w[9].toString()).longValue());
            parm.put("OWNR_TRND", w[10] == null ? "" : new BigDecimal(w[10].toString()).longValue());
            parm.put("PRCPL_TRND", w[11] == null ? "" : new BigDecimal(w[11].toString()).longValue());
            parm.put("STDNT_TRND", w[12] == null ? "" : new BigDecimal(w[12].toString()).longValue());

            expList.add(parm);
        });

        return reportComponent.generateReport(KSS_TRAINING, params, getJRDataSource(expList));
    }

    public byte[] getClientRecoveryStatus(String toDt, String userId, long brnch) throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("to_dt", toDt);

        String clntRecvryQry;
        clntRecvryQry = readFile(Charset.defaultCharset(), "ClientRecoveryStatusQuery.txt");
        Query set = em.createNativeQuery(clntRecvryQry).setParameter("to_dt", toDt).setParameter("brn_seq", brnch);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> rcvry = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue()); // big decimal
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("EMP_NM", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_SEQ", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue()); // big decimal
            map.put("CLNT_NM", l[5] == null ? "" : l[5].toString());
            map.put("DSB_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue()); // big decimal
            map.put("DSBMT_DT", l[7] == null ? "" : l[7].toString());
            map.put("GRACE_PERD", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue()); // big decimal
            map.put("LOAN_CYCL_NUM", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue()); // big decimal
            map.put("OUTS_APR20", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue()); // big decimal
            map.put("REC_CUM", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());// big decimal
            map.put("OD_AMT_CRNT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());// big decimal
            map.put("OD_DYS_CRNT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());// big decimal

            rcvry.add(map);
        });
        return reportComponent.generateReport(CLIENT_RECOVERY_STATUS, params, getJRDataSource(rcvry));
    }

    public byte[] getDueVsRecovery(String userId) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "DueVsRecoveryDtlQry.txt");
        Query rs2 = em.createNativeQuery(detailQry);

        List<Object[]> dtlObj = rs2.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();

        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
            map.put("REG_NM", l[1] == null ? "" : l[1].toString());
            map.put("AREA_NM", l[2] == null ? "" : l[2].toString());
            map.put("BRNCH_NM", l[3] == null ? "" : l[3].toString());
            map.put("LOANS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OUTS_APR20", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("RSCH_LOANS", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("RSCH_OUTS_APR20", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("DUE_CRNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("REC_CRNT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("REC_CRNT_PRCNT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).doubleValue());
            map.put("DUE_CUM", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("REC_CUM", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("REC_CUM_PRCNT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).doubleValue());
            map.put("COMPL_LOANS_CRNT", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("LOANS_CRNT", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OUTS_CRNT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());

            pymts.add(map);
        });

        String headerQuery;
        headerQuery = readFile(Charset.defaultCharset(), "DueVsRecoveryHeaderQry.txt");
        Query header = em.createNativeQuery(headerQuery);

        List<Object[]> hdrObj = header.getResultList();

        List<Map<String, ?>> hdrRes = new ArrayList();

        hdrObj.forEach(l -> {
            Map<String, Object> hrdmap = new HashMap();
            hrdmap.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
            hrdmap.put("REG_NM", l[1] == null ? "" : l[1].toString());
            hrdmap.put("LOANS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            hrdmap.put("OUTS_APR20", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            hrdmap.put("RSCH_LOANS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            hrdmap.put("RSCH_OUTS_APR20", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            hrdmap.put("DUE_CRNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            hrdmap.put("REC_CRNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            hrdmap.put("REC_CRNT_PRCNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).doubleValue());
            hrdmap.put("DUE_CUM", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            hrdmap.put("REC_CUM", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            hrdmap.put("REC_CUM_PRCNT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            hrdmap.put("COMPL_LOANS_CRNT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            hrdmap.put("LOANS_CRNT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            hrdmap.put("OUTS_CRNT", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());

            hdrRes.add(hrdmap);
        });
        params.put("dataset", getJRDataSource(hdrRes));

        return reportComponent.generateReport(DUE_VS_RECOVERY, params, getJRDataSource(pymts));
    }

    public byte[] getManagementDashboard(String userId) throws IOException {

        Map<String, Object> params = new HashMap<>();

        Object asOfDateScript = em.createNativeQuery(
                        "select to_date \n" + "from CON_MON_PROCESS_PARAM@link_to_testmwc cc\n" + "where cc.REPORT_NUMBER=1")
                .getSingleResult();

        params.put("curr_user", userId);
        params.put("TO_DATE", asOfDateScript);

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "ManagementDashBoradQry.txt");

        Query rs2 = em.createNativeQuery(detailQry);

        List<Object[]> dtlObj = rs2.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();

        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("LOANS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("CLNTS_UNRECH", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("CLNTS_RECH", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("CLNTS_NEW", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("DSB_AMT_NEW", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("OUTS_CRNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("OUTS_UNRECH", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("OUTS_RECH", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OUTS_NEW", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("P_1_RECH_GRC1_PAR", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("P_1_RECH_GRC2_PAR", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("P_1_RECH_GRC3_PAR", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("P_1_RECH_GRC4_PAR", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());

            map.put("P_3_RECH_GRC1_PAR", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("P_3_RECH_GRC2_PAR", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("P_3_RECH_GRC3_PAR", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("P_3_RECH_GRC4_PAR", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());

            map.put("P_1_OUTS", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("P_1_UNRECH_OUTS", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("P_1_RECH_OUTS", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
            map.put("P_1_NEW_OUTS", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());

            map.put("P_3_OUTS", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("P_3_UNRECH_OUTS", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());
            map.put("P_3_RECH_OUTS", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("P_3_NEW_OUTS", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());

            map.put("PAR_P1_PERC", l[26] == null ? 0 : new BigDecimal(l[26].toString()).doubleValue());
            map.put("PAR_P1_PERC_UNRECH", l[27] == null ? 0 : new BigDecimal(l[27].toString()).doubleValue());
            map.put("PAR_P1_PERC_RECH", l[28] == null ? 0 : new BigDecimal(l[28].toString()).doubleValue());
            map.put("PAR_P1_PERC_NEW", l[29] == null ? 0 : new BigDecimal(l[29].toString()).doubleValue());

            map.put("PAR_P3_PERC", l[30] == null ? 0 : new BigDecimal(l[30].toString()).doubleValue());
            map.put("PAR_P3_PERC_UNRECH", l[31] == null ? 0 : new BigDecimal(l[31].toString()).doubleValue());
            map.put("PAR_P3_PERC_RECH", l[32] == null ? 0 : new BigDecimal(l[32].toString()).doubleValue());
            map.put("PAR_P3_PERC_NEW", l[33] == null ? 0 : new BigDecimal(l[33].toString()).doubleValue());

            map.put("OUTS_RECH_GRC1", l[34] == null ? 0 : new BigDecimal(l[34].toString()).longValue());
            map.put("OUTS_RECH_GRC2", l[35] == null ? 0 : new BigDecimal(l[35].toString()).longValue());
            map.put("OUTS_RECH_GRC3", l[36] == null ? 0 : new BigDecimal(l[36].toString()).longValue());
            map.put("OUTS_RECH_GRC4", l[37] == null ? 0 : new BigDecimal(l[37].toString()).longValue());

            map.put("P_1_RECH_GRC1", l[38] == null ? 0 : new BigDecimal(l[38].toString()).doubleValue());
            map.put("P_1_RECH_GRC2", l[39] == null ? 0 : new BigDecimal(l[39].toString()).doubleValue());
            map.put("P_1_RECH_GRC3", l[40] == null ? 0 : new BigDecimal(l[40].toString()).doubleValue());
            map.put("P_1_RECH_GRC4", l[41] == null ? 0 : new BigDecimal(l[41].toString()).doubleValue());

            map.put("P_3_RECH_GRC1", l[42] == null ? 0 : new BigDecimal(l[42].toString()).doubleValue());
            map.put("P_3_RECH_GRC2", l[43] == null ? 0 : new BigDecimal(l[43].toString()).doubleValue());
            map.put("P_3_RECH_GRC3", l[44] == null ? 0 : new BigDecimal(l[44].toString()).doubleValue());
            map.put("P_3_RECH_GRC4", l[45] == null ? 0 : new BigDecimal(l[45].toString()).doubleValue());

            map.put("P_1_RECH_GRC1_LOANS", l[46] == null ? 0 : new BigDecimal(l[46].toString()).longValue());
            map.put("P_1_RECH_GRC2_LOANS", l[47] == null ? 0 : new BigDecimal(l[47].toString()).longValue());
            map.put("P_1_RECH_GRC3_LOANS", l[48] == null ? 0 : new BigDecimal(l[48].toString()).longValue());
            map.put("P_1_RECH_GRC4_LOANS", l[49] == null ? 0 : new BigDecimal(l[49].toString()).longValue());

            map.put("P_1_RECH_GRC1_#", l[50] == null ? 0 : new BigDecimal(l[50].toString()).longValue());
            map.put("P_1_RECH_GRC2_#", l[51] == null ? 0 : new BigDecimal(l[51].toString()).longValue());
            map.put("P_1_RECH_GRC3_#", l[52] == null ? 0 : new BigDecimal(l[52].toString()).longValue());
            map.put("P_1_RECH_GRC4_#", l[53] == null ? 0 : new BigDecimal(l[53].toString()).longValue());

            map.put("P_3_RECH_GRC1_LOANS", l[54] == null ? 0 : new BigDecimal(l[54].toString()).longValue());
            map.put("P_3_RECH_GRC2_LOANS", l[55] == null ? 0 : new BigDecimal(l[55].toString()).longValue());
            map.put("P_3_RECH_GRC3_LOANS", l[56] == null ? 0 : new BigDecimal(l[56].toString()).longValue());
            map.put("P_3_RECH_GRC4_LOANS", l[57] == null ? 0 : new BigDecimal(l[57].toString()).longValue());

            map.put("P_3_RECH_GRC1_#", l[58] == null ? 0 : new BigDecimal(l[58].toString()).longValue());
            map.put("P_3_RECH_GRC2_#", l[59] == null ? 0 : new BigDecimal(l[59].toString()).longValue());
            map.put("P_3_RECH_GRC3_#", l[60] == null ? 0 : new BigDecimal(l[60].toString()).longValue());
            map.put("P_3_RECH_GRC4_#", l[61] == null ? 0 : new BigDecimal(l[61].toString()).longValue());

            map.put("OUTS_RECH_GRC1_OUTS", l[62] == null ? 0 : new BigDecimal(l[62].toString()).longValue());
            map.put("OUTS_RECH_GRC2_OUTS", l[63] == null ? 0 : new BigDecimal(l[63].toString()).longValue());
            map.put("OUTS_RECH_GRC3_OUTS", l[64] == null ? 0 : new BigDecimal(l[64].toString()).longValue());
            map.put("OUTS_RECH_GRC4_OUTS", l[65] == null ? 0 : new BigDecimal(l[65].toString()).longValue());

            pymts.add(map);
        });

        return reportComponent.generateReport(MANAGEMENT_DASHBOARD, params, getJRDataSource(pymts));
    }

    public byte[] getReschedulingPortfolio(String userId) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "ReschedulingPortfolioDtlQry.txt");
        Query rs2 = em.createNativeQuery(detailQry);

        List<Object[]> dtlObj = rs2.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();

        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("REG_NM", l[2] == null ? "" : l[2].toString());
            map.put("AREA_NM", l[3] == null ? "" : l[3].toString());
            map.put("BRNCH_NM", l[4] == null ? "" : l[4].toString());
            map.put("LOANS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DSB_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("OUTS_CRNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("LOANS_P1", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OD_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("OUTS_P1", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PAR_P1_PERC", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            map.put("LOANS_P2", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OUTS_P2", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("PAR_P2_PERC", l[14] == null ? 0 : new BigDecimal(l[14].toString()).doubleValue());
            map.put("LOANS_P3", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OUTS_P3", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("PAR_P3_PERC", l[17] == null ? 0 : new BigDecimal(l[17].toString()).doubleValue());

            map.put("LOANS_P4", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("OUTS_P4", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("PAR_P4_PERC", l[20] == null ? 0 : new BigDecimal(l[20].toString()).doubleValue());

            map.put("LOANS_P5", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("OUTS_P5", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("PAR_P5_PERC", l[23] == null ? 0 : new BigDecimal(l[23].toString()).doubleValue());

            map.put("LOANS_P6", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("OUTS_P6", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("PAR_P6_PERC", l[26] == null ? 0 : new BigDecimal(l[26].toString()).doubleValue());

            map.put("LOANS_P7", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("OUTS_P7", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
            map.put("PAR_P7_PERC", l[29] == null ? 0 : new BigDecimal(l[29].toString()).doubleValue());

            map.put("LOANS_P8", l[30] == null ? 0 : new BigDecimal(l[30].toString()).longValue());
            map.put("OUTS_P8", l[31] == null ? 0 : new BigDecimal(l[31].toString()).longValue());
            map.put("PAR_P8_PERC", l[32] == null ? 0 : new BigDecimal(l[32].toString()).doubleValue());

            pymts.add(map);
        });

        String headerQuery;
        headerQuery = readFile(Charset.defaultCharset(), "ReschedulingPortfolioHeaderQry.txt");
        Query header = em.createNativeQuery(headerQuery);

        List<Object[]> hdrObj = header.getResultList();

        List<Map<String, ?>> hdrRes = new ArrayList();

        hdrObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("REG_NM", l[2] == null ? "" : l[2].toString());
            map.put("AREA_NM", l[3] == null ? "" : l[3].toString());
            map.put("BRNCH_NM", l[4] == null ? "" : l[4].toString());
            map.put("LOANS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DSB_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("OUTS_CRNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("LOANS_P1", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OD_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("OUTS_P1", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PAR_P1_PERC", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            map.put("LOANS_P2", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OUTS_P2", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("PAR_P2_PERC", l[14] == null ? 0 : new BigDecimal(l[14].toString()).doubleValue());
            map.put("LOANS_P3", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OUTS_P3", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("PAR_P3_PERC", l[17] == null ? 0 : new BigDecimal(l[17].toString()).doubleValue());

            map.put("LOANS_P4", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("OUTS_P4", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("PAR_P4_PERC", l[20] == null ? 0 : new BigDecimal(l[20].toString()).doubleValue());

            map.put("LOANS_P5", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("OUTS_P5", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("PAR_P5_PERC", l[23] == null ? 0 : new BigDecimal(l[23].toString()).doubleValue());

            map.put("LOANS_P6", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("OUTS_P6", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("PAR_P6_PERC", l[26] == null ? 0 : new BigDecimal(l[26].toString()).doubleValue());

            map.put("LOANS_P7", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("OUTS_P7", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
            map.put("PAR_P7_PERC", l[29] == null ? 0 : new BigDecimal(l[29].toString()).doubleValue());

            map.put("LOANS_P8", l[30] == null ? 0 : new BigDecimal(l[30].toString()).longValue());
            map.put("OUTS_P8", l[31] == null ? 0 : new BigDecimal(l[31].toString()).longValue());
            map.put("PAR_P8_PERC", l[32] == null ? 0 : new BigDecimal(l[32].toString()).doubleValue());
            hdrRes.add(map);
        });
        params.put("dataset", getJRDataSource(hdrRes));

        return reportComponent.generateReport(PORTFOLIO_RESCHEDULING, params, getJRDataSource(pymts));
    }

    public byte[] getPortfolioQualityOldPortfolio(String userId) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "oldPortfolioDtlQuery.txt");
        Query rs2 = em.createNativeQuery(detailQry);

        List<Object[]> dtlObj = rs2.getResultList();

        List<Map<String, ?>> pymts = new ArrayList();

        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("REG_NM", l[2] == null ? "" : l[2].toString());
            map.put("AREA_NM", l[3] == null ? "" : l[3].toString());
            map.put("BRNCH_NM", l[4] == null ? "" : l[4].toString());
            map.put("LOANS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DSB_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("OUTS_CRNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("LOANS_P1", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OD_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("OUTS_P1", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PAR_P1_PERC", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            map.put("LOANS_P2", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OUTS_P2", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("PAR_P2_PERC", l[14] == null ? 0 : new BigDecimal(l[14].toString()).doubleValue());
            map.put("LOANS_P3", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OUTS_P3", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("PAR_P3_PERC", l[17] == null ? 0 : new BigDecimal(l[17].toString()).doubleValue());

            map.put("LOANS_P4", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("OUTS_P4", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("PAR_P4_PERC", l[20] == null ? 0 : new BigDecimal(l[20].toString()).doubleValue());

            map.put("LOANS_P5", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("OUTS_P5", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("PAR_P5_PERC", l[23] == null ? 0 : new BigDecimal(l[23].toString()).doubleValue());

            map.put("LOANS_P6", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("OUTS_P6", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("PAR_P6_PERC", l[26] == null ? 0 : new BigDecimal(l[26].toString()).doubleValue());

            map.put("LOANS_P7", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("OUTS_P7", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
            map.put("PAR_P7_PERC", l[29] == null ? 0 : new BigDecimal(l[29].toString()).doubleValue());

            map.put("LOANS_P8", l[30] == null ? 0 : new BigDecimal(l[30].toString()).longValue());
            map.put("OUTS_P8", l[31] == null ? 0 : new BigDecimal(l[31].toString()).longValue());
            map.put("PAR_P8_PERC", l[32] == null ? 0 : new BigDecimal(l[32].toString()).doubleValue());

            pymts.add(map);
        });

        String headerQuery;
        headerQuery = readFile(Charset.defaultCharset(), "oldPortfolioHdrQuery.txt");
        Query header = em.createNativeQuery(headerQuery);

        List<Object[]> hdrObj = header.getResultList();

        List<Map<String, ?>> hdrRes = new ArrayList();

        hdrObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRD_GRP_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("REG_NM", l[2] == null ? "" : l[2].toString());
            map.put("AREA_NM", l[3] == null ? "" : l[3].toString());
            map.put("BRNCH_NM", l[4] == null ? "" : l[4].toString());
            map.put("LOANS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DSB_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("OUTS_CRNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("LOANS_P1", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OD_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("OUTS_P1", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PAR_P1_PERC", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            map.put("LOANS_P2", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OUTS_P2", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("PAR_P2_PERC", l[14] == null ? 0 : new BigDecimal(l[14].toString()).doubleValue());
            map.put("LOANS_P3", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OUTS_P3", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("PAR_P3_PERC", l[17] == null ? 0 : new BigDecimal(l[17].toString()).doubleValue());

            map.put("LOANS_P4", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("OUTS_P4", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("PAR_P4_PERC", l[20] == null ? 0 : new BigDecimal(l[20].toString()).doubleValue());

            map.put("LOANS_P5", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("OUTS_P5", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("PAR_P5_PERC", l[23] == null ? 0 : new BigDecimal(l[23].toString()).doubleValue());

            map.put("LOANS_P6", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("OUTS_P6", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("PAR_P6_PERC", l[26] == null ? 0 : new BigDecimal(l[26].toString()).doubleValue());

            map.put("LOANS_P7", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("OUTS_P7", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
            map.put("PAR_P7_PERC", l[29] == null ? 0 : new BigDecimal(l[29].toString()).doubleValue());

            map.put("LOANS_P8", l[30] == null ? 0 : new BigDecimal(l[30].toString()).longValue());
            map.put("OUTS_P8", l[31] == null ? 0 : new BigDecimal(l[31].toString()).longValue());
            map.put("PAR_P8_PERC", l[32] == null ? 0 : new BigDecimal(l[32].toString()).doubleValue());
            hdrRes.add(map);
        });
        params.put("dataset", getJRDataSource(hdrRes));

        return reportComponent.generateReport(OLD_PORTFOLIO, params, getJRDataSource(pymts));
    }

    public byte[] getSale2PendingReport(String fromDt, String toDt, long brnchSeq, String userId) throws IOException {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", fromDt);
        params.put("to_dt", toDt);

        String salePending;
        salePending = readFile(Charset.defaultCharset(), "SALE2PENDING.txt");
        Query rs = em.createNativeQuery(salePending).setParameter("from_dt", fromDt).setParameter("to_dt", toDt)
                .setParameter("brnch_cd", obj[4].toString()).setParameter("usrid", userId);

        List<Object[]> sale2PendingList = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        sale2PendingList.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("EMP_NM", l[0] == null ? "" : l[0].toString());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("CLNT_ID", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_NM", l[3] == null ? "" : l[3].toString());
            map.put("HSBND_NM", l[4] == null ? "" : l[4].toString());
            map.put("APRVD_LOAN_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DSB_DT", l[6] == null ? "" : l[6].toString());
            map.put("PNDNG_DYS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());

            reportParams.add(map);
        });
        params.put("dataset", getJRDataSource(reportParams));

        return reportComponent.generateReport(SALE_2_PENDING, params, getJRDataSource(reportParams));
    }

    public byte[] getLeaveAndAttendanceMonitoringReport(String frmDt, String toDt, String userId, long rpt_flg)
            throws IOException {

		/*
		 	UPDATED BY YOUSAF AS ATTANDANCE REPORT IS AVAILABALE IN HARMONY
		 */

		/*Map<String, Object> params = new HashMap<>();

		// if (rpt_flg == 1) {

		Query headerQuery = em.createNativeQuery("select e.EMPLOYEE_ID,e.FIRST_NAME,\r\n"
				+ "       dl.DESCRIPTION detail_location, \r\n" + "       ml.DESCRIPTION master_location,\r\n"
				+ "       r.REGION_NAME\r\n" + "from \r\n" + "    mw_emp emp, \r\n"
				+ "	employee@DBLINK_NEWMW_TO_H0DB1 e,\r\n" + "	detail_Location@DBLINK_NEWMW_TO_H0DB1 dl,\r\n"
				+ "	master_location@DBLINK_NEWMW_TO_H0DB1 ml,\r\n" + "	region@DBLINK_NEWMW_TO_H0DB1 r\r\n" + "	\r\n"
				+ "where\r\n" + "emp.hrid=e.employee_id \r\n" + "and e.DETAIL_LOCATION_ID = dl.DETAIL_LOCATION_ID\r\n"
				+ "and e.MASTER_LOCATION_ID = ml.MASTER_LOCATION_ID\r\n" + "and ml.REGION_CD = r.REGION_CD\r\n"
				+ "and dl.ACTIVE = 'Y'\r\n" + "and ml.ACTIVE = 'Y'\r\n" + "and emp.emp_lan_id=:emplnId")
				.setParameter("emplnId", userId);

		// Query bi = em.createNativeQuery( com.idev4.rds.util.Queries.BRANCH_INFO
		// ).setParameter( "userId", userId );
		Object[] obj = (Object[]) headerQuery.getSingleResult();
		params.put("emp_id", obj[0].toString());
		params.put("emp_nm", obj[1].toString());
		params.put("brnch_nm", obj[2].toString());
		params.put("area_nm", obj[3].toString());
		params.put("reg_nm", obj[4].toString());
		// }
		params.put("curr_user", userId);
		params.put("from_dt", frmDt);
		params.put("to_dt", toDt);

		Query brnchDetil = em
				.createNativeQuery("Select A.detail_location_id, B.master_location_id , A.region_cd \r\n"
						+ "from EMPLOYEE@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK A\r\n"
						+ "join detail_location@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK B \r\n"
						+ "ON A.detail_location_id=B.detail_location_id\r\n" + "join mw_emp C \r\n"
						+ "on c.hrid=A.employee_id\r\n" + "and C.emp_lan_id=:userId\r\n" + "and B.active='Y'")
				.setParameter("userId", userId);

		Object[] brnchObjet = (Object[]) brnchDetil.getSingleResult();
		String detLoc = brnchObjet[0].toString();
		String mstLoc = brnchObjet[1].toString();
		String region = brnchObjet[2].toString();

		Query bodyQuery = em.createNativeQuery("select \r\n" + "' '||B.BRANCH AS BRANCH,\r\n"
				+ "' '||b.AREA AS AREA,\r\n" + "' '||B.REGION AS REGION,\r\n" + "B.HRID,\r\n"
				+ "' '||B.NAME AS NAME,\r\n" + "' '||B.DESIGNATION AS DESIGNATION,\r\n" + "B.NDATE DATES,\r\n"
				+ "B.OPBAL_AL,\r\n" + "B.OPBAL_ML,\r\n" + "B.OPBAL_CL,\r\n"
				+ "nvl(B.opbal_al,0)-nvl(B.avail_al,0) - nvl((SELECT ld.no_of_leave_deduct\r\n"
				+ "                                        FROM leave_deduction@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK ld\r\n"
				+ "                                        WHERE ld.POST = 'Y'\r\n"
				+ "                                        AND LD.LEAVE_ID=1\r\n"
				+ "                                        AND LD.employee_id = B.HRID\r\n"
				+ "                                        AND ld.from_date Between to_date(:from_date,'dd-MM-yyyy') AND to_date(:to_date,'dd-MM-yyyy')\r\n"
				+ "                                        ),0) clbal_al,\r\n"
				+ "nvl(B.opbal_ml,0)-nvl(B.avail_ml,0) clbal_ml,\r\n"
				+ "nvl(B.opbal_cl,0)-nvl(B.avail_cl,0) clbal_cl,\r\n" + "ev.SHORT_DESC status,\r\n"
				+ "ev.TIME_IN TIMEIN,\r\n" + "ev.TIMEOUT TIME_OUT,\r\n" + "TO_CHAR(B.NDATE,'YYMMDD') ADATE,\r\n"
				+ "b.HRIDS\r\n" + "from \r\n" + "(select \r\n" + "dl.DETAIL_LOCATION_ID,\r\n"
				+ "dl.MASTER_LOCATION_ID,\r\n" + "initcap(dl.DESCRIPTION) branch,\r\n"
				+ "initcap(ml.DESCRIPTION) area,\r\n" + "initcap(r.Region_name) Region,\r\n" + "e.EMPLOYEE_ID HRID,\r\n"
				+ "INITCAP (          e.first_name\r\n" + "                   || ' '\r\n"
				+ "                   || e.middle_name\r\n" + "                   || ' '\r\n"
				+ "                   || e.last_name\r\n" + "                  ) AS name,\r\n"
				+ "p.SHORT_DESC Designation,\r\n" + "subquery_name.ndate,\r\n" + "((SELECT nvl(LB.LOADEDBAL,0)\r\n"
				+ "--into OP_BAL \r\n" + "  FROM LEAVE_BALANCE@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LB\r\n"
				+ "  where LB.employee_id = e.EMPLOYEE_ID\r\n" + "  AND LB.ACTIVE  = 'Y'\r\n"
				+ "  AND LB.POST = 'Y'\r\n" + "AND LB.LEAVE_ID = 1)-\r\n" + "(select NVL(SUM(LQ.TOTAL_DAYS),0) \r\n"
				+ "FROM LEAVE_REQUEST@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LQ\r\n"
				+ "    where Lq.employee_id = e.EMPLOYEE_ID\r\n" + "    AND  LQ.LEAVE_ID = 1\r\n"
				+ "    AND (LQ.APPLICATION_DATE >= (SELECT LB.TRANS_DATE\r\n"
				+ "                                                        FROM LEAVE_BALANCE@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LB\r\n"
				+ "                                                        where LB.employee_id = e.EMPLOYEE_ID\r\n"
				+ "                                                        AND LB.ACTIVE  = 'Y'\r\n"
				+ "                                                        AND LB.POST = 'Y'\r\n"
				+ "                                                        AND LB.LEAVE_ID = 1\r\n"
				+ "                      )\r\n"
				+ "    and LQ.APPLICATION_DATE < to_date(:from_date,'dd-MM-yyyy')))) opbal_al,\r\n"
				+ "    ((SELECT nvl(LB.LOADEDBAL,0)\r\n" + "--into OP_BAL \r\n"
				+ "  FROM LEAVE_BALANCE@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LB\r\n"
				+ "  where LB.employee_id = e.EMPLOYEE_ID\r\n" + "  AND LB.ACTIVE  = 'Y'\r\n"
				+ "  AND LB.POST = 'Y'\r\n" + "AND LB.LEAVE_ID = 2)-\r\n" + "(select NVL(SUM(LQ.TOTAL_DAYS),0) \r\n"
				+ "FROM LEAVE_REQUEST@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LQ\r\n"
				+ "    where Lq.employee_id = e.EMPLOYEE_ID\r\n" + "    AND  LQ.LEAVE_ID = 2\r\n"
				+ "    AND (LQ.APPLICATION_DATE >= (SELECT LB.TRANS_DATE\r\n"
				+ "                                                        FROM LEAVE_BALANCE@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LB\r\n"
				+ "                                                        where LB.employee_id = e.EMPLOYEE_ID\r\n"
				+ "                                                        AND LB.ACTIVE  = 'Y'\r\n"
				+ "                                                        AND LB.POST = 'Y'\r\n"
				+ "                                                        AND LB.LEAVE_ID = 2\r\n"
				+ "                      )\r\n"
				+ "    and LQ.APPLICATION_DATE < to_date(:from_date,'dd-MM-yyyy')))) opbal_ml,\r\n"
				+ "     ((SELECT nvl(LB.LOADEDBAL,0)\r\n" + "--into OP_BAL \r\n"
				+ "  FROM LEAVE_BALANCE@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LB\r\n"
				+ "  where LB.employee_id = e.EMPLOYEE_ID\r\n" + "  AND LB.ACTIVE  = 'Y'\r\n"
				+ "  AND LB.POST = 'Y'\r\n" + "AND LB.LEAVE_ID = 3)-\r\n" + "(select NVL(SUM(LQ.TOTAL_DAYS),0) \r\n"
				+ "FROM LEAVE_REQUEST@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LQ\r\n"
				+ "    where Lq.employee_id = e.EMPLOYEE_ID\r\n" + "    AND  LQ.LEAVE_ID = 3\r\n"
				+ "    AND (LQ.APPLICATION_DATE >= (SELECT LB.TRANS_DATE\r\n"
				+ "                                                        FROM LEAVE_BALANCE@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LB\r\n"
				+ "                                                        where LB.employee_id = e.EMPLOYEE_ID\r\n"
				+ "                                                        AND LB.ACTIVE  = 'Y'\r\n"
				+ "                                                        AND LB.POST = 'Y'\r\n"
				+ "                                                        AND LB.LEAVE_ID = 3\r\n"
				+ "                      )\r\n"
				+ "    and LQ.APPLICATION_DATE < to_date(:from_date,'dd-MM-yyyy')))) opbal_cl,\r\n"
				+ "  (select NVL(SUM(LQ.TOTAL_DAYS),0) \r\n"
				+ "    FROM LEAVE_REQUEST@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LQ\r\n"
				+ "    where Lq.employee_id = e.EMPLOYEE_ID\r\n" + "    AND  LQ.LEAVE_ID = 1\r\n"
				+ "    AND (LQ.APPLICATION_DATE >= to_date(:from_date,'dd-MM-yyyy') and LQ.APPLICATION_DATE <= to_date(:to_date,'dd-MM-yyyy')))avail_al,\r\n"
				+ "    (select NVL(SUM(LQ.TOTAL_DAYS),0) \r\n"
				+ "    FROM LEAVE_REQUEST@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LQ\r\n"
				+ "    where Lq.employee_id = e.EMPLOYEE_ID\r\n" + "    AND  LQ.LEAVE_ID = 2\r\n"
				+ "    AND (LQ.APPLICATION_DATE >= to_date(:from_date,'dd-MM-yyyy') and LQ.APPLICATION_DATE <= to_date(:to_date,'dd-MM-yyyy')))avail_ml,\r\n"
				+ "     (select NVL(SUM(LQ.TOTAL_DAYS),0) \r\n"
				+ "    FROM LEAVE_REQUEST@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK LQ\r\n"
				+ "    where Lq.employee_id = e.EMPLOYEE_ID\r\n" + "    AND  LQ.LEAVE_ID = 3\r\n"
				+ "    AND (LQ.APPLICATION_DATE >= to_date(:from_date,'dd-MM-yyyy') and LQ.APPLICATION_DATE <= to_date(:to_date,'dd-MM-yyyy')))avail_cl,\r\n"
				+ "TO_NUMBER(e.EMPLOYEE_ID) HRIDS\r\n" + "from \r\n"
				+ "region@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK r,\r\n"
				+ "employee@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK e,\r\n"
				+ "position@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK p,\r\n"
				+ "master_location@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK ml,\r\n"
				+ "detail_location@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK dl,\r\n" + "(          \r\n" + " SELECT NDATE\r\n"
				+ "FROM (\r\n" + "SELECT ROWNUM - 1 + to_date (to_date(:from_date,'dd-MM-yyyy'), 'dd-mon-yy') ndate\r\n"
				+ "  FROM all_tab_columns\r\n" + "   WHERE ROWNUM <= (               \r\n"
				+ "   SELECT TRIM (to_date(:to_date,'dd-MM-yyyy') - to_date(:from_date,'dd-MM-yyyy')) + 1\r\n"
				+ "  FROM DUAL                                          \r\n" + "  ))\r\n"
				+ "  WHERE LTRIM (RTRIM (TO_CHAR(NDATE,'DAY'))) NOT IN ('SATURDAY', 'SUNDAY')\r\n"
				+ ") subquery_name\r\n" + "where ml.REGION_CD=r.REGION_CD\r\n"
				+ "and dl.MASTER_LOCATION_ID=ml.MASTER_LOCATION_ID\r\n"
				+ "and e.MASTER_LOCATION_ID=ml.MASTER_LOCATION_ID\r\n"
				+ "and e.DETAIL_LOCATION_ID=dl.DETAIL_LOCATION_ID\r\n" + "and e.POSITION_ID=p.POSITION_ID\r\n"
				+ "and ml.ACTIVE='Y'\r\n" + "and dl.ACTIVE='Y'\r\n" + "and e.ACTIVE='Y'\r\n"
				+ "and LTRIM(UPPER(DL.DETAIL_LOCATION_ID))=(CASE when:det_loc='001' THEN  LTRIM(UPPER(DL.DETAIL_LOCATION_ID)) ELSE REPLACE(UPPER(:det_loc),'_',' ') END)\r\n"
				+ "and LTRIM(UPPER(ML.MASTER_LOCATION_ID))=(case when :mst_loc='001' THEN   LTRIM(UPPER(ML.MASTER_LOCATION_ID)) ELSE REPLACE(UPPER(:mst_loc),'_',' ')   END)\r\n"
				+ "AND LTRIM(UPPER(R.REGION_CD))=(CASE WHEN :regions='999' THEN LTRIM(UPPER(R.REGION_CD)) ELSE REPLACE(upper(:regions),'_',' ') END)\r\n"
				+ "--and e.EMPLOYEE_ID='04758'\r\n" + "--and initcap(dl.DESCRIPTION)='Qanchi'\r\n" + "and exists\r\n"
				+ "(select hr_branch_cd from hr_mw6_branch_maps@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK hmbm\r\n"
				+ "where hmbm.mw6_branch_cd is not null\r\n" + "and hmbm.hr_branch_cd=dl.DETAIL_LOCATION_ID))b,\r\n"
				+ "EMPLOYEE_ATTENDANCE_V@DBLINK_NEWMW_TO_H0DB1.KASHF.ORG.PK ev\r\n"
				+ "where b.HRID=ev.EMPLOYEE_ID(+)\r\n" + "--and   b.detail_location_id=ev.DETAIL_LOCATION_ID(+)\r\n"
				+ "and   b.NDATE=ev.ATT_DATE(+)").setParameter("from_date", frmDt).setParameter("to_date", toDt)
				.setParameter("regions", region).setParameter("mst_loc", mstLoc).setParameter("det_loc", detLoc);

		List<Object[]> qryObj = bodyQuery.getResultList();
		List<Map<String, ?>> result = new ArrayList();

		qryObj.forEach(data -> {
			Map<String, Object> mapList = new HashMap<>();

			mapList.put("HRID", data[3] == null ? "" : data[3].toString());
			mapList.put("NAME", data[4] == null ? "" : data[4].toString());
			mapList.put("DESIGNATION", data[5] == null ? "" : data[5].toString());
			mapList.put("DATES", data[6]);
			mapList.put("OPBAL_AL", data[7] == null ? 0 : new BigDecimal(data[7].toString()).longValue());
			mapList.put("OPBAL_ML", data[8] == null ? 0 : new BigDecimal(data[8].toString()).longValue());
			mapList.put("OPBAL_CL", data[9] == null ? 0 : new BigDecimal(data[9].toString()).doubleValue());

			mapList.put("CLBAL_AL", data[10] == null ? 0 : new BigDecimal(data[10].toString()).longValue());
			mapList.put("CLBAL_ML", data[11] == null ? 0 : new BigDecimal(data[11].toString()).longValue());
			mapList.put("CLBAL_CL", data[12] == null ? 0 : new BigDecimal(data[12].toString()).doubleValue());

			mapList.put("STATUS", data[13] == null ? "" : data[13].toString());
			mapList.put("TIMEIN", data[14] == null ? null : data[14].toString());
			mapList.put("TIME_OUT", data[15] == null ? "" : data[15].toString());

			result.add(mapList);
		});

		return reportComponent.generateReport(LEAVE_AND_ATTENDANCE_MONITORING_REPORT, params, getJRDataSource(result));*/
        return null;
    }

    public byte[] getAMLMatchesRport(String fromDt, String toDt, Long brnchSeq, String userId, boolean isXlx)
            throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);

        String amlMatchesScript;
        amlMatchesScript = readFile(Charset.defaultCharset(), "AMLMatchesScript.txt");
        Query rs = em.createNativeQuery(amlMatchesScript).setParameter("frmDt", fromDt).setParameter("toDt", toDt)
                .setParameter("brnchSeq", brnchSeq);

        List<Object[]> amlMatchLists = rs.getResultList();

        List<Map<String, ?>> reportDetail = new ArrayList();
        amlMatchLists.forEach(aml -> {
            Map<String, Object> map = new HashMap();

            map.put("REG_NM", aml[0] == null ? "" : aml[0].toString());
            map.put("AREA_NM", aml[1] == null ? "" : aml[1].toString());
            map.put("BRNCH_NM", aml[2] == null ? "" : aml[2].toString());
            map.put("BDONM", aml[3] == null ? "" : aml[3].toString());
            map.put("CRDT_DT", aml[4]);
            map.put("CNIC_NUM", aml[5] == null ? "" : aml[5].toString());
            map.put("NM", aml[6] == null ? "" : aml[6].toString());
            map.put("TYPOFMATCH", aml[7] == null ? "" : aml[7].toString());

            reportDetail.add(map);
        });
        return reportComponent.generateReport(AML_MATCHES, params, getJRDataSource(reportDetail), isXlx);
    }

    public byte[] getActiveClientsReport(String user) {

        Map<String, Object> params = new HashMap<>();

        String asOfScript;
        asOfScript = readFile(Charset.defaultCharset(), "AsOfScript.txt");
        Query rs = em.createNativeQuery(asOfScript);

        Object asOfDateScript = rs.getSingleResult();

        params.put("curr_user", user);
        params.put("TO_DATE", asOfDateScript);

        String activeClientsScript = readFile(Charset.defaultCharset(), "ActiveClientsScript.txt");

        List<Object[]> listOfClients = em.createNativeQuery(activeClientsScript).getResultList();

        List<Map<String, ?>> clients = new ArrayList<>();

        listOfClients.forEach(clnt -> {
            Map<String, Object> map = new HashMap<>();

            map.put("REG_SEQ", (clnt[0] == null ? "" : new BigDecimal(clnt[0].toString()).longValue()));
            map.put("REG_NM", (clnt[1] == null ? "" : clnt[1].toString()));
            map.put("CLNTS", (clnt[2] == null ? "" : new BigDecimal(clnt[2].toString()).longValue()));

            clients.add(map);
        });
        return reportComponent.generateReport(ACTIVE_CLIENTS, params, getJRDataSource(clients));
    }

    public byte[] getCnicExpDetailReport(String user) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String expryDtl;
        expryDtl = readFile(Charset.defaultCharset(), "CnicExpiryDetail.txt");
        Query rs = em.createNativeQuery(expryDtl).setParameter("userId", user);

        List<Object[]> cnicRecords = rs.getResultList();

        List<Map<String, ?>> result = new ArrayList();

        cnicRecords.forEach(record -> {
            Map<String, Object> mapList = new HashMap<>();

            mapList.put("BRNCH_NM", record[0] == null ? "" : record[0].toString());
            mapList.put("EMP_NM", record[1] == null ? "" : record[1].toString());
            mapList.put("PRD_GRP_NM", record[2] == null ? "" : record[2].toString());
            mapList.put("CLNT_SEQ", record[3] == null ? "" : record[3].toString());
            mapList.put("LOAN_APP_SEQ", record[4] == null ? "" : record[4].toString());
            mapList.put("CLNT_NM", record[5] == null ? "" : record[5].toString());
            mapList.put("DSBMT_DT", record[6] == null ? "" : record[6]);
            mapList.put("CNIC_EXPRY_DT", record[7] == null ? "" : record[7]);
            mapList.put("IDENTIFICATION", record[8] == null ? "" : record[8].toString());

            result.add(mapList);
        });

        return reportComponent.generateReport(CNIC_EXPIRY_DETAIL, params, getJRDataSource(result));
    }

    public byte[] getClientLoanMaturityReport(String userId, String toDt, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("to_dt", toDt);

        String ql;
        ql = readFile(Charset.defaultCharset(), "ClientsLoansMaturityReportScript.txt");
        Query rs = em.createNativeQuery(ql).setParameter("todt", toDt);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_SEQ", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_NM", w[2] == null ? "" : w[2].toString());
            parm.put("PH_NM", w[3] == null ? "" : w[3].toString());
            parm.put("LOAN_APP_SEQ", w[4] == null ? "" : w[4].toString());
            parm.put("DSBMT_DT", w[5] == null ? "" : w[5].toString());
            parm.put("CLNT_CNIC", w[6] == null ? "" : w[6].toString());
            parm.put("DSB_AMT", w[7] == null ? "" : w[7].toString());
            parm.put("OUTS", w[8] == null ? "" : w[8].toString());
            parm.put("OD_AMT", w[9] == null ? "" : w[9].toString());
            parm.put("OD_DYS", w[10] == null ? "" : w[10].toString());
            parm.put("REMITANCE_ACC_#", w[11] == null ? "" : w[11].toString());
            parm.put("REMITTANCE_TYP", w[12] == null ? "" : w[12].toString());
            parm.put("LOAN_MTURTY_DT", w[13] == null ? "" : w[13].toString());
            parm.put("BRNCH_NM", w[14] == null ? "" : w[14].toString());
            recList.add(parm);
        });

        if (isXls) {
            return reportComponent.generateReport(CLIENT_LOAN_MATRITY_EXCEL, params, getJRDataSource(recList), isXls);
        } else {
            return reportComponent.generateReport(CLIENT_LOAN_MATRITY, params, getJRDataSource(recList));
        }
    }

    public byte[] getMobileWalletDueReport(String userId, String frmDt, String toDt, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String ql;
        ql = readFile(Charset.defaultCharset(), "MobileWalletDisbursementDueScript.txt");
        Query rs = em.createNativeQuery(ql).setParameter("todt", toDt).setParameter("frmdt", frmDt);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_SEQ", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_NM", w[2] == null ? "" : w[2].toString());
            parm.put("DSBMT_DT", w[3] == null ? "" : w[3].toString());
            parm.put("DUE_DT", w[4] == null ? "" : w[4].toString());
            parm.put("TOT_DUE", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("REMITANCE_ACC_#", w[6] == null ? "" : w[6].toString());
            parm.put("REMITTANCE_TYP", w[7] == null ? "" : w[7].toString());
            parm.put("BRNCH_NM", w[8] == null ? "" : w[8].toString());

            recList.add(parm);
        });

        if (isXls) {
            return reportComponent.generateReport(MOBILE_WALLET_DUE_EXCEL, params, getJRDataSource(recList), isXls);
        } else {
            return reportComponent.generateReport(MOBILE_WALLET_DUE, params, getJRDataSource(recList));
        }
    }

    public byte[] getMobileWalletReport(String userId, String frmDt, String toDt, boolean isXls) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String ql;
        ql = readFile(Charset.defaultCharset(), "MobileWalletDisbursementScript.txt");
        Query rs = em.createNativeQuery(ql).setParameter("todt", toDt).setParameter("frmdt", frmDt);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("DSBMT_DT", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_SEQ", w[2] == null ? "" : w[2].toString());
            parm.put("CLNT_NM", w[3] == null ? "" : w[3].toString());
            parm.put("LOAN_APP_SEQ", w[4] == null ? "" : w[4].toString());
            parm.put("CLNT_CNIC", w[5] == null ? "" : w[5].toString());
            parm.put("REMITANCE_ACC_#", w[6] == null ? "" : w[6].toString());
            parm.put("AMT", w[7] == null ? 0 : new BigDecimal(w[7].toString()).longValue());
            parm.put("REMITTANCE_TYP", w[8] == null ? "" : w[8].toString());
            parm.put("DT", w[9] == null ? "" : w[9].toString());
            parm.put("BRNCH_NM", w[10] == null ? "" : w[10].toString());
            parm.put("LOAN_STS", w[11] == null ? "" : w[11].toString());

            recList.add(parm);
        });

        if (isXls) {
            return reportComponent.generateReport(MOBILE_WALLET_EXCEL, params, getJRDataSource(recList), isXls);
        } else {
            return reportComponent.generateReport(MOBILE_WALLET, params, getJRDataSource(recList));
        }
    }

    public byte[] getMcbRemmitanceDisbursementFundsReport(String frmDt, String toDt, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "MCBRemittanceDisbursementFundsScript.txt");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("VDATE", l[0] == null ? "" : l[0].toString());
            map.put("LOAN_APP_SEQ", l[1] == null ? "" : l[1].toString());
            map.put("CNIC_NUM", l[2] == null ? "" : l[2].toString());
            map.put("CNIC_EXPRY_DT", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_SEQ", l[4] == null ? "" : l[4].toString());
            map.put("NAME", l[5] == null ? "" : l[5].toString());
            map.put("BRNCH_NM", l[6] == null ? "" : l[6].toString());
            map.put("DISB_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("BANK", l[8] == null ? "" : l[8].toString());
            map.put("BANK_CODE", l[9] == null ? "" : l[9].toString());
            map.put("BANK_BRNCH", l[10] == null ? "" : l[10].toString());
            map.put("PRD_NM", l[11] == null ? "" : l[11].toString());
            map.put("TYP_STR", l[12] == null ? "" : l[12].toString());
            map.put("CONTACT", l[13] == null ? "" : l[13].toString());
            map.put("EMAIL", l[14] == null ? "" : l[14].toString());
            map.put("REFFER", l[15] == null ? "" : l[15].toString());


            disbursements.add(map);
        });

        if (isXls) {
            return reportComponent.generateReport(MCB_FUNDS_EXCEL, params, getJRDataSource(disbursements), isXls);
        } else {
            return reportComponent.generateReport(MCB_FUNDS, params, getJRDataSource(disbursements));
        }
    }

    public byte[] getMcbRemmitanceDisbursementLoaderReport(String frmDt, String toDt, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "MCBRemittanceDisbursmentfundsLoaderScript.txt");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("BANK", l[1] == null ? "" : l[1].toString());
            map.put("AMOUNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            disbursements.add(map);
        });

        if (isXls) {
            return reportComponent.generateReport(MCB_LOADER_EXCEL, params, getJRDataSource(disbursements), isXls);
        } else {
            return reportComponent.generateReport(MCB_LOADER, params, getJRDataSource(disbursements));
        }
    }

    public byte[] getMcbRemmitanceDisbursementLetterReport(String frmDt, String toDt, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        String refDt = "";
        DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            Date date = format.parse(toDt);
            DateFormat frmt = new SimpleDateFormat("yyyyMMdd");
            refDt = frmt.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        params.put("ref_dt", refDt);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "MCBRemittanceDisbursmentfundsLetterScript.txt");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        Object Obj = set.getSingleResult();
        params.put("AMOUNT", Obj == null ? 0L : new BigDecimal(Obj.toString()).longValue());

        if (isXls) {
            return reportComponent.generateReport(MCB_LETTER_EXCEL, params, null, isXls);
        } else {
            return reportComponent.generateReport(MCB_LETTER, params, null);
        }
    }

    public byte[] getCheckDisbursementFundsReport(String frmDt, String toDt, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "ChequesDisbursmentFunds.txt");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSBMT_HDR_SEQ", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("REG_NM", l[1] == null ? "" : l[1].toString());
            map.put("AREA_NM", l[2] == null ? "" : l[2].toString());
            map.put("BRNCH_NM", l[3] == null ? "" : l[3].toString());
            map.put("ACCT_NUM", l[4] == null ? "" : l[4].toString());
            map.put("BANK", l[5] == null ? "" : l[5].toString());
            map.put("STATUS", l[6] == null ? "" : l[6].toString());
            map.put("BANK_BRNCH", l[7] == null ? "" : l[7].toString());
            map.put("BANK_CODE", l[8] == null ? "" : l[8].toString());
            map.put("DISB_AMOUNT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("CLIENTS", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PRD_NM", l[11] == null ? "" : l[11].toString());
            map.put("TYP_STR", l[12] == null ? "" : l[12].toString());
            disbursements.add(map);
        });

        if (isXls) {
            return reportComponent.generateReport(CHEQUE_DISBURSMENT_EXCEL, params, getJRDataSource(disbursements), isXls);
        } else {
            return reportComponent.generateReport(CHEQUE_DISBURSMENT, params, getJRDataSource(disbursements));
        }
    }

    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // add parameter Mobile Wallet Type
    public byte[] getMobileWalletFundsReport(String frmDt, String toDt, String typId, String userId, boolean isXls)
            throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        params.put("typyId", typId);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "mobileWalletDisburseFundScript.txt");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("typId", typId);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSBMT_DT", l[0] == null ? "" : l[0]);
            map.put("BRNCH_NM", l[1] == null ? "" : l[1].toString());
            map.put("LOAN_ID", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_SEQ_1", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_SEQ", l[4] == null ? "" : l[4].toString());
            map.put("CLNT_NM", l[5] == null ? "" : l[5].toString());
            map.put("CLNT_CNIC", l[6] == null ? "" : l[6].toString());
            map.put("REMITANCE_ACC_#", l[7] == null ? "" : l[7].toString());
            map.put("DISB_AMOUNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("REMITTANCE_TYP", l[9] == null ? "" : l[9].toString());
            map.put("BRNCH_ADDR", l[10] == null ? "" : l[10].toString());
            disbursements.add(map);
        });

        if (WALLET_EASY_PAISA.equals(typId)) {
            params.put("account", "EP");
            params.put("title", "EasyPaisa Mobile Wallet Disbursement Funds Report");
        } else if (WALLET_JAZZ_CASH.equals(typId)) {
            params.put("account", "JazzCash");
            params.put("title", "JazzCash Mobile Wallet Disbursement Funds Report");
        } else if (WALLET_HBL_KONNECT.equals(typId)) {
            params.put("account", "HBL Konnect");
            params.put("title", "HBL Konnect Mobile Wallet Disbursement Funds Report");
        } else if (WALLET_UPAISA.equals(typId)) {
            params.put("account", "Upaisa");
            params.put("title", "Upaisa Mobile Wallet Disbursement Funds Report");
        } else if (ALL_MOBILE_WALLET.equals(typId)) {
            params.put("account", "Wallet");
            params.put("title", "Mobile Wallet Disbursement Funds Report");
        }

        if (isXls) {
            return reportComponent.generateReport(MOBILE_WALLET_FUNDS_EXCEL, params, getJRDataSource(disbursements), isXls);
        } else {
            return reportComponent.generateReport(MOBILE_WALLET_FUNDS, params, getJRDataSource(disbursements));
        }
    } // end by Naveed

    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // add parameter Mobile Wallet Type
    public byte[] getMobileWalletLoaderReport(String frmDt, String toDt, String typId, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        params.put("typyId", typId);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "mobileWalletDisburseLoaderScript.txt");
        disQry = disQry.replaceAll(":typId", "'" + typId + "'");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("DISB_AMOUNT", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("BANK", l[2] == null ? "" : l[2].toString());
            disbursements.add(map);
        });

        if (WALLET_EASY_PAISA.equals(typId)) {
            params.put("account", "Telenor Microfinance Bank Limited");
            params.put("account_no", "10.0101001.000.000.401202.10351.0000");
            params.put("title", "Easy Paisa Disbursement Funds Loader");
        } else if (WALLET_JAZZ_CASH.equals(typId)) {
            params.put("account", "JazzCash Main Account");
            params.put("account_no", "10.0101001.000.000.401301.10356.0000");
            params.put("title", "JazzCash Disbursement Funds Loader");
        } else if (WALLET_HBL_KONNECT.equals(typId)) {
            params.put("account", "HBL Konnect Main Account");
            params.put("account_no", "10.0101001.000.000.401202.10204.0000");
            params.put("title", "HBL Konnect Disbursement Funds Loader");
        } else if (WALLET_UPAISA.equals(typId)) {
            params.put("account", "Upaisa Main Account");
            params.put("account_no", "10.0101001.000.000.401301.10369.0000");
            params.put("title", "Upaisa Disbursement Funds Loader");
        } else if (ALL_MOBILE_WALLET.equals(typId)) {
            params.put("account", "Main Account");
            params.put("account_no", "00.000000.000.000.000000.00000.0000");
            params.put("title", "Mobile Wallet Disbursement Funds Loader");
        }

        if (isXls) {
            return reportComponent.generateReport(MOBILE_WALLET_LOADER_EXCEL, params, getJRDataSource(disbursements), isXls);
        } else {
            return reportComponent.generateReport(MOBILE_WALLET_LOADER, params, getJRDataSource(disbursements));
        }
    } // End By Naveed

    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // add parameter Mobile Wallet Type
    public byte[] getMobileWalletLetterReport(String frmDt, String toDt, String typId, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        params.put("typyId", typId);

        String refDt = "";
        DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            Date date = format.parse(toDt);
            DateFormat frmt = new SimpleDateFormat("yyyyMMdd");
            refDt = frmt.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        params.put("ref_dt", refDt);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "mobileWalletDisburseLetterScript.txt");
        Query set = em.createNativeQuery(disQry).setParameter("frmdt", frmDt).setParameter("todt", toDt)
                .setParameter("typId", typId);

        Object Obj = set.getSingleResult();
        params.put("AMOUNT", Obj == null ? 0L : new BigDecimal(Obj.toString()).longValue());

        if (WALLET_EASY_PAISA.equals(typId)) {
            params.put("account", "Telenor Microfinance Bank Limited");
            params.put("account_no", "151016036889001");
            params.put("addr", "S94-R55, Near Chappar Stop, Multan Road, Lahore.");
            params.put("bankCd", "151016");
        } else if (WALLET_JAZZ_CASH.equals(typId)) {
            params.put("account", "Mobilink Microfinance Bank Limited");
            params.put("account_no", "101143953");
            params.put("addr", "37-A, XX Khayaban-e-Iqbal,DHA,  Lahore");
            params.put("bankCd", "2000");
        } else if (WALLET_HBL_KONNECT.equals(typId)) {
            params.put("account", "Habib Bank Limited,");
            params.put("account1", "Corporate Center Branch The Mall,");
            params.put("account_no", "12427900022803");
            params.put("addr", "Corporate Center 102 103 Upper Mall, Lahore.");
            params.put("bankCd", "1242");
        } else if (WALLET_UPAISA.equals(typId)) {
            params.put("account", "U Microfinance Bank Limited");
            params.put("account_no", "0423-0096989-0259");
            params.put("addr", "117-A, Chowk Yateem Khana Multan Road Lahore.");
            params.put("bankCd", "0423");
        } else if (ALL_MOBILE_WALLET.equals(typId)) {
            params.put("account", "Microfinance Bank Limited");
            params.put("account_no", "000000000000000");
            params.put("addr", "Address");
        }

        if (isXls) {
            return reportComponent.generateReport(MOBILE_WALLET_LETTER_EXCEL, params, null, isXls);
        } else {
            return reportComponent.generateReport(MOBILE_WALLET_LETTER, params, null);
        }
    } // end by Naveed

    public byte[] getClntInfoJazzDueReport(String user, boolean isxls) {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "ClientInfoJazzDues.txt");
        Query set = em.createNativeQuery(query);

        List<Object[]> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REGION_NAME", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NAME", l[1] == null ? "" : l[1].toString());
            map.put("Transaction_Number", l[2] == null ? "" : l[2].toString());
            map.put("BRANCH_DESC", l[3] == null ? "" : l[3].toString());
            map.put("CLIENT_PARTY_ID", l[4] == null ? "" : l[4].toString());
            map.put("NAME", l[5] == null ? "" : l[5].toString());
            map.put("CNIC", l[6] == null ? "" : l[6].toString());
            map.put("DUE_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("SVG_AMOUNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("TOTAL_AMOUNT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("TRANSACTION", l[10] == null ? "" : l[10].toString());
            map.put("AGENT", l[11] == null ? "" : l[11].toString());

            lists.add(map);
        });

        if (isxls) {
            return reportComponent.generateReport(JAZZ_CASH_INFO_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            return reportComponent.generateReport(JAZZ_CASH_INFO_DUES, params, getJRDataSource(lists));
        }

    }

    public byte[] getClientUpldDuesReport(String user, boolean isxls) {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "ClientUploadingJazzDues.txt");
        Query set = em.createNativeQuery(query);

        List<Object[]> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REGION_NAME", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NAME", l[1] == null ? "" : l[1].toString());
            map.put("CLIENT_PARTY_ID", l[2] == null ? "" : l[2].toString());
            map.put("NAME", l[3] == null ? "" : l[3].toString());
            map.put("BIL_MNT", l[4] == null ? "" : l[4].toString());
            map.put("BFR_DT", l[5] == null ? "" : l[5].toString());
            map.put("AFTR_DT", l[6] == null ? "" : l[6].toString());
            map.put("DF", l[7] == null ? "" : l[7].toString());
            map.put("CLNT", l[8] == null ? "" : l[8].toString());

            lists.add(map);
        });

        if (isxls) {
            return reportComponent.generateReport(JAZZ_CASH_UPLOAD_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            return reportComponent.generateReport(JAZZ_CASH_UPLOAD_DUES, params, getJRDataSource(lists));
        }
    }

    public byte[] getEasyPaisaDuesReport(String user, boolean isxls) {

        byte[] reportData = null;
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "easyPaisaDues.txt");
        Query set = em.createNativeQuery(query);

        List<String> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DATA", l.toString());
            lists.add(map);
        });

        if (isxls) {
            reportData = reportComponent.generateReport(EASY_PAISA_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            reportData = reportComponent.generateReport(EASY_PAISA_DUES, params, getJRDataSource(lists));
        }

        lists.clear();
        Objs.clear();
        System.gc();

        return reportData;
    }

    public byte[] getUblOmniDuesReport(String user, boolean isxls) {

        byte[] reportDataBytes = null;
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "ublOmniDues.txt");
        Query set = em.createNativeQuery(query);

        List<Object[]> Objs = set.getResultList();
        List<Map<String, ?>> lists = new ArrayList();

        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REGION_NAME", l[0] == null ? "" : l[0].toString());
            map.put("BRANCH_DESC", l[1] == null ? "" : l[1].toString());
            map.put("CLIENT_PARTY_ID", l[2] == null ? "" : l[2].toString());
            map.put("NAME", l[3] == null ? "" : l[3].toString());
            map.put("CNIC", l[4] == null ? "" : l[4].toString());
            map.put("HUSBAND", l[5] == null ? "" : l[5].toString());
            map.put("DUE_AMOUNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("SAVING_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("DISB_DT", l[8] == null ? "" : l[8].toString());
            map.put("END_DT", l[9] == null ? "" : l[9].toString());
            map.put("BRNCH_PH_NUM", l[10] == null ? "" : l[10].toString());
            lists.add(map);
        });

        if (isxls) {
            reportDataBytes = reportComponent.generateReport(UBL_OMNI_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            reportDataBytes = reportComponent.generateReport(UBL_OMNI_DUES, params, getJRDataSource(lists));
        }
        lists.clear();
        Objs.clear();
        System.gc();

        return reportDataBytes;
    }

    public byte[] getHblConnectReport(String user, String todt, boolean isxls) {

        byte[] reportData = null;
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "hblConnectDues.txt");
        Query set = em.createNativeQuery(query).setParameter("todt", todt);

        List<Object[]> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("CLIENT_PARTY_ID", l[0] == null ? "" : l[0].toString());
            map.put("INVOICE_ID", l[1] == null ? "" : l[1].toString());
            map.put("NAME", l[2] == null ? "" : l[2].toString());
            map.put("BRANCH_DESC", l[3] == null ? "" : l[3].toString());
            map.put("BILLING_MONTH", l[4] == null ? "" : l[4].toString());
            map.put("DUE_AMOUNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("SAVING_AMOUNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("TOTAL_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("PAYABLE_AMOUNT_AFT_DUE_DATE", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("DUE_DATE", l[9] == null ? "" : l[9].toString());

            lists.add(map);
        });

        if (isxls) {
            reportData = reportComponent.generateReport(HBL_CONNECT_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            reportData = reportComponent.generateReport(HBL_CONNECT_DUES, params, getJRDataSource(lists));
        }

        lists.clear();
        Objs.clear();
        System.gc();
        return reportData;
    }


    public byte[] getAblLoanDuesReport(String user, boolean isxls) {

        byte[] reportData = null;
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "AblRecoveryDues.txt");
        Query set = em.createNativeQuery(query);

        List<String> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DATA", l.toString());
            lists.add(map);
        });

        if (isxls) {
            reportData = reportComponent.generateReport(ABL_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            reportData = reportComponent.generateReport(ABL_DUES, params, getJRDataSource(lists));
        }

        lists.clear();
        Objs.clear();
        System.gc();

        return reportData;
    }

    public byte[] getMcbCollectReport(String user, boolean isxls) {

        byte[] reportData = null;
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "McbCollectDues.txt");
        Query set = em.createNativeQuery(query);

        List<Object[]> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("TRANSACTION_NUMBER", l[0] == null ? "" : l[0].toString());
            map.put("CLIENT_ID", l[1] == null ? "" : l[1].toString());
            map.put("CLIENT_NAME", l[2] == null ? "" : l[2].toString());
            map.put("SPOUSE_NAME", l[3] == null ? "" : l[3].toString());
            map.put("CNIC_NO", l[4] == null ? "" : l[4].toString());
            map.put("KASHF_BRANCH_NAME", l[5] == null ? "" : l[5].toString());
            map.put("RECOVERY_AMOUNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("SAVING_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("TOTAL_AMOUNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("TRANSACTION_DATE", l[9] == null ? "" : l[9].toString());
            map.put("MCB_CODE", l[10] == null ? "" : l[10].toString());
            map.put("MCB_BRANCH_NAME", l[11] == null ? "" : l[11].toString());

            lists.add(map);
        });

        if (isxls) {
            reportData = reportComponent.generateReport(MCB_COLLECT_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            reportData = reportComponent.generateReport(MCB_COLLECT_DUES, params, getJRDataSource(lists));
        }

        lists.clear();
        Objs.clear();
        System.gc();

        return reportData;
    }

    public byte[] getNadraDuesReport(String user, boolean isxls) {

        byte[] reportData = null;
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String query;
        query = readFile(Charset.defaultCharset(), "NadraDues.txt");
        Query set = em.createNativeQuery(query);

        List<String> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DATA", l.toString());
            lists.add(map);
        });

        if (isxls) {
            reportData = reportComponent.generateReport(NADRA_DUES_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            reportData = reportComponent.generateReport(NADRA_DUES, params, getJRDataSource(lists));
        }

        lists.clear();
        Objs.clear();
        System.gc();

        return reportData;
    }

    public byte[] getNactaManagementReport() {

        Map<String, Object> params = new HashMap<>();

        String expryDtl = "SELECT LI.* FROM MW_NACTA_LIST LI ORDER BY 1";
        Query rs = em.createNativeQuery(expryDtl);

        List<Object[]> cnicRecords = rs.getResultList();

        List<Map<String, ?>> result = new ArrayList();

        cnicRecords.forEach(record -> {
            Map<String, Object> mapList = new HashMap<>();

            mapList.put("NACTA_LIST_SEQ", record[0] == null ? "" : record[0].toString());
            mapList.put("CNIC_NUM", record[1] == null ? "" : record[1].toString());
            mapList.put("DIST", record[2] == null ? "" : record[2].toString());
            mapList.put("FTHR_NM", record[3] == null ? "" : record[3].toString());
            mapList.put("CLNT_NM", record[4] == null ? "" : record[4].toString());
            mapList.put("PRVNC", record[5] == null ? "" : record[5].toString());

            result.add(mapList);
        });
        return reportComponent.generateReport(NACTA_MANAGMENT, params, getJRDataSource(result), true);
    }

    public byte[] getNactaManagementMatchReport() {

        Map<String, Object> params = new HashMap<>();

        String query = readFile(Charset.defaultCharset(), "nactaMatchingClientScript.txt");
        Query rs = em.createNativeQuery(query);

        List<Object[]> cnicRecords = rs.getResultList();
        List<Map<String, ?>> result = new ArrayList();

        cnicRecords.forEach(record -> {
            Map<String, Object> mapList = new HashMap<>();

            mapList.put("BRANCH_NAME", record[0] == null ? "" : record[0].toString());
            mapList.put("CNIC_NUM", record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue());
            mapList.put("NAME", record[2] == null ? "" : record[2].toString());
            mapList.put("CLIENT_ID", record[3] == null ? 0 : new BigDecimal(record[3].toString()).longValue());
            mapList.put("PARTY_TYPE", record[4] == null ? "" : record[4].toString());


            result.add(mapList);
        });
        return reportComponent.generateReport(NACTA_MATCH, params, getJRDataSource(result), true);
    }

    public byte[] getDonnerTaggingReport(String userId, String toDt, String frmDt, long donner, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);

        String taggingscript;
        taggingscript = readFile(Charset.defaultCharset(), "taggingReportScript.txt");
        Query rs = em.createNativeQuery(taggingscript).setParameter("P_TODT", toDt)
                .setParameter("P_FRMDT", frmDt).setParameter("P_DNRSEQ", donner);

        List<Object[]> tagginglists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        tagginglists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("CLNT_SEQ", l[0] == null ? "" : l[0].toString());
            map.put("CLNT_NM", l[1] == null ? "" : l[1].toString());
            map.put("LOAN_APP_SEQ", l[2] == null ? "" : l[2].toString());
            map.put("CNIC", l[3] == null ? "" : l[3].toString());
            map.put("GENDER", l[4] == null ? "" : l[4].toString());
            map.put("BANK", l[5] == null ? "" : l[5].toString());
            map.put("BRNCH_LOC", l[6] == null ? "" : l[6].toString());
            map.put("DISTRICT", l[6] == null ? "" : l[6].toString().split(" - ")[1]);
            map.put("RURAL_URBAN", l[7] == null ? "" : l[7].toString());
            map.put("ACTIVITY", l[8] == null ? "" : l[8].toString());
            map.put("SECTOR", l[9] == null ? "" : l[9].toString());
            map.put("PRD_CMNT", l[10] == null ? "" : l[10].toString());
            map.put("LOAN_CYCL_NUM", l[11] == null ? "" : l[11].toString());
            map.put("DONOR_NM", l[12] == null ? "" : l[12].toString());
            map.put("SANC_DT", l[13] == null ? "" : l[13].toString());
            map.put("DSBMT_DT", l[14] == null ? "" : l[14].toString());
            map.put("APRVD_LOAN_AMT", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OST_AMT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("REC_AMT", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("LOAN_STS", l[18] == null ? "" : l[18].toString());
            map.put("MARKUP_RATE", l[19] == null ? "" : l[19].toString());
            map.put("APR", l[20] == null ? 0 : new BigDecimal(l[20].toString()).doubleValue());
            map.put("COLLATERAL", l[21] == null ? "" : l[21].toString());
            map.put("SATTLEMENT_DT", l[22] == null ? "" : l[22].toString());
            map.put("OD_DYS", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());
            map.put("DPD", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());

            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(TAGGING_REPORT_EXCEL, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(TAGGING_REPORT, params, getJRDataSource(reportParams));
        }
    }

    public byte[] getVerisysReport(String userId, String frmDt, String toDt, long brnchSeq, long areaSeq, long regSeq, long type, boolean isxls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi;

        if (brnchSeq != -1) {
            bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                    brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        } else if (areaSeq != -1) {
            bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
        } else if (regSeq != -1) {
            bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq",
                    regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[1].toString());
            params.put("area_nm", "ALL");
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
        }


        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);


        String sumaryScript;
        sumaryScript = readFile(Charset.defaultCharset(), "verisysReportSumaryScript.txt");
        Query sumaryQuery = em.createNativeQuery(sumaryScript).setParameter("todt", toDt).setParameter("frmdt", frmDt)
                .setParameter("brnchSeq", brnchSeq).setParameter("regSeq", regSeq)
                .setParameter("areaSeq", areaSeq);

        List<Object[]> sumaryResult = sumaryQuery.getResultList();
        List<Map<String, ?>> smryList = new ArrayList();

        sumaryResult.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("TRANS_DT", w[0] == null ? "" : w[0].toString());
            parm.put("PENDING_COUNT", w[1] == null ? 0 : new BigDecimal(w[1].toString()).longValue());
            parm.put("NADRA_VERIFIED_COUNT", w[2] == null ? 0 : new BigDecimal(w[2].toString()).longValue());
            parm.put("NADRA_ERROR_COUNT", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("KASHF_VERIFIED_COUNT", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("KASHF_ERROR_COUNT", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("TOTAL_COUNT", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());
            smryList.add(parm);
        });

        params.put("datasetsum", getJRDataSource(smryList));

        String ql;
        ql = readFile(Charset.defaultCharset(), "verisysReportScript.txt");
        Query rs = em.createNativeQuery(ql).setParameter("todt", toDt).setParameter("frmdt", frmDt)
                .setParameter("brnchSeq", brnchSeq).setParameter("typ", type).setParameter("regSeq", regSeq)
                .setParameter("areaSeq", areaSeq);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("CLNT_ID", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_NM", w[1] == null ? "" : w[1].toString());
            parm.put("VER_SEQ", w[2] == null ? "" : w[2].toString());
            parm.put("LOAN_APP_SEQ", w[3] == null ? "" : w[3].toString());
            parm.put("CNIC", w[4] == null ? "" : w[4].toString());
            parm.put("TRANS_DT", w[5] == null ? "" : w[5].toString());
            parm.put("CNIC_ISSUE_DT", w[6] == null ? "" : w[6].toString());
            parm.put("CNIC_CATEGORY", w[7] == null ? "" : w[7].toString());
            parm.put("STATUS", w[8] == null ? "" : w[8].toString());
            parm.put("VERISYS_RESP", w[9] == null ? "" : w[9].toString());
            parm.put("NOM_NM", w[10] == null ? "" : w[10].toString());
            parm.put("REG_NM", w[11] == null ? "" : w[11].toString());
            parm.put("AREA_NM", w[12] == null ? "" : w[12].toString());
            parm.put("BRNCH_NM", w[13] == null ? "" : w[13].toString());
            parm.put("KASHF_STATUS", w[14] == null ? "" : w[14].toString());
            recList.add(parm);
        });
        params.put("dataset", getJRDataSource(recList));

        if (isxls) {
            return reportComponent.generateReport(VERISYS_REPORT_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(VERISYS_REPORT, params, null);
        }
    }

    public byte[] getMfcib(Clob clob) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonObject = "";
        Map<String, Map<String, Object>> map = new HashMap<>();

        try {
            jsonObject = clob == null ? null : clob.getSubString(1, (int) clob.length());
            map = objectMapper.readValue(jsonObject, Map.class);
        } catch (Exception exp) {
            throw new Exception(exp);
        }

        Map<String, Object> params = new HashMap<>();

        Map<String, Object> root = new HashMap<>();
        if (map.containsKey("report")) {
            root = (Map<String, Object>) map.get("report").get("ROOT");
        } else if (map.containsKey("ROOT")) {
            root = (Map<String, Object>) map.get("ROOT");
        } else {
            return new byte[0];
        }

        List<Map<String, Object>> personDetail = new ArrayList<>();
        List<Map<String, Object>> addrDetail = new ArrayList<>();
        List<Map<String, Object>> creditApp = new ArrayList<>();
        List<Map<String, Object>> defalt = new ArrayList<>();
        List<Map<String, Object>> fileNotes = new ArrayList<>();
        List<Map<String, Object>> employeeInfo = new ArrayList<>();
        List<Map<String, Object>> collateral = new ArrayList<>();
        List<Map<String, Object>> association = new ArrayList<>();
        List<Map<String, Object>> gurantyDetail = new ArrayList<>();
        List<Map<String, Object>> coborrowerDetail = new ArrayList<>();
        List<Map<String, Object>> bankRuptcyDetail = new ArrayList<>();
        List<Map<String, Object>> reviewDetail = new ArrayList<>();
        List<Map<String, Object>> reportMessage = new ArrayList<>();

        if (root.containsKey("INDIVIDUAL_DETAIL")) {
            if (isList(root.get("INDIVIDUAL_DETAIL"))) {
                personDetail = getMapList((List<Map<String, Object>>) root.get("INDIVIDUAL_DETAIL"));
            } else {
                personDetail = getMapList((Map<String, Object>) root.get("INDIVIDUAL_DETAIL"));
            }
        }
        if (root.containsKey("HOME_INFORMATION")) {
            if (isList(root.get("HOME_INFORMATION"))) {
                addrDetail = getMapList((List<Map<String, Object>>) root.get("HOME_INFORMATION"));
            } else {
                addrDetail = getMapList((Map<String, Object>) root.get("HOME_INFORMATION"));
            }
        }

        if (root.containsKey("ENQUIRIES")) {
            if (isList(root.get("ENQUIRIES"))) {
                creditApp = getMapList((List<Map<String, Object>>) root.get("ENQUIRIES"));
            } else {
                creditApp = getMapList((Map<String, Object>) root.get("ENQUIRIES"));
            }
        }

        if (root.containsKey("DEFAULTS")) {
            if (isList(root.get("DEFAULTS"))) {
                defalt = getMapList((List<Map<String, Object>>) root.get("DEFAULTS"));
            } else {
                defalt = getMapList((Map<String, Object>) root.get("DEFAULTS"));
            }
        }

        if (root.containsKey("FILE_NOTES")) {
            if (isList(root.get("FILE_NOTES"))) {
                fileNotes = getMapList((List<Map<String, Object>>) root.get("FILE_NOTES"));
            } else {
                fileNotes = getMapList((Map<String, Object>) root.get("FILE_NOTES"));
            }
        }

        if (root.containsKey("EMPLOYER_INFORMATION")) {
            if (isList(root.get("EMPLOYER_INFORMATION"))) {
                employeeInfo = getMapList((List<Map<String, Object>>) root.get("EMPLOYER_INFORMATION"));
            } else {
                employeeInfo = getMapList((Map<String, Object>) root.get("EMPLOYER_INFORMATION"));
            }
        }

        if (root.containsKey("COLLATERAL")) {
            if (isList(root.get("COLLATERAL"))) {
                collateral = getMapList((List<Map<String, Object>>) root.get("COLLATERAL"));
            } else {
                collateral = getMapList((Map<String, Object>) root.get("COLLATERAL"));
            }
        }

        if (root.containsKey("ASSOCIATION")) {
            if (isList(root.get("ASSOCIATION"))) {
                association = getMapList((List<Map<String, Object>>) root.get("ASSOCIATION"));
            } else {
                association = getMapList((Map<String, Object>) root.get("ASSOCIATION"));
            }
        }

        if (root.containsKey("GUARANTEES_DETAILS")) {
            if (isList(root.get("GUARANTEES_DETAILS"))) {
                gurantyDetail = getMapList((List<Map<String, Object>>) root.get("GUARANTEES_DETAILS"));
            } else {
                gurantyDetail = getMapList((Map<String, Object>) root.get("GUARANTEES_DETAILS"));
            }
        }

        if (root.containsKey("COBORROWER_DETAILS")) {
            if (isList(root.get("COBORROWER_DETAILS"))) {
                coborrowerDetail = getMapList((List<Map<String, Object>>) root.get("COBORROWER_DETAILS"));
            } else {
                coborrowerDetail = getMapList((Map<String, Object>) root.get("COBORROWER_DETAILS"));
            }
        }

        if (root.containsKey("BANKRUPTCY_DETAILS")) {
            if (isList(root.get("BANKRUPTCY_DETAILS"))) {
                bankRuptcyDetail = getMapList((List<Map<String, Object>>) root.get("BANKRUPTCY_DETAILS"));
            } else {
                bankRuptcyDetail = getMapList((Map<String, Object>) root.get("BANKRUPTCY_DETAILS"));
            }
        }

        if (root.containsKey("REVIEW")) {
            if (isList(root.get("REVIEW"))) {
                reviewDetail = getMapList((List<Map<String, Object>>) root.get("REVIEW"));
            } else {
                reviewDetail = getMapList((Map<String, Object>) root.get("REVIEW"));
            }
        }

        if (root.containsKey("REPORT_MESSAGE")) {
            if (isList(root.get("REPORT_MESSAGE"))) {
                reportMessage = getMapList((List<Map<String, Object>>) root.get("REPORT_MESSAGE"));
            } else {
                reportMessage = getMapList((Map<String, Object>) root.get("REPORT_MESSAGE"));
            }
        }

        List<Map<String, Object>> delinq = getCcpMaster(root, true, false);
        List<Map<String, Object>> ccpDetail = getCcpMaster(root, true, true);

        addrDetail.forEach(addr -> {
            if (addr.get("SEQ_NO").toString().equals("Latest")) {
                params.put("crntAddr", getJRDataSource(getLists(addr)));
            } else if (addr.get("SEQ_NO").toString().equals("Previous")) {
                params.put("prmAddr", getJRDataSource(getLists(addr)));
            }
        });

        employeeInfo.forEach(addr -> {
            if (addr.get("SEQ_NO").toString().equals("Latest")) {
                params.put("crntEmp", getJRDataSource(getLists(addr)));
            } else if (addr.get("SEQ_NO").toString().equals("Previous")) {
                params.put("prevEmp", getJRDataSource(getLists(addr)));
            }
        });

        params.put("fileIdentity", getJRDataSource(personDetail));
        params.put("personInfo", getJRDataSource(personDetail));
        params.put("sumry", getJRDataSource(delinq));
        params.put("deflt", getJRDataSource(defalt));
        params.put("credit", getJRDataSource(creditApp));
        params.put("ccpDetail", getJRDataSource(ccpDetail));
        params.put("file", getJRDataSource(fileNotes));
        params.put("review", getJRDataSource(reviewDetail));
        params.put("collateral", getJRDataSource(collateral));
        params.put("association", getJRDataSource(association));
        params.put("gurantyDetail", getJRDataSource(gurantyDetail));
        params.put("coborrowerDetail", getJRDataSource(coborrowerDetail));
        params.put("bankRuptcyDetail", getJRDataSource(bankRuptcyDetail));
        params.put("reportMessage", getJRDataSource(reportMessage));
        params.put("employeeInfo", getJRDataSource(employeeInfo));

        String[] accTyp = new String[5];
        creditApp.forEach(r -> {
            if (r.containsKey("ACCT_TY") || r.containsKey("MAPPED_ACCT_TY")) {
                if (r.get("ACCT_TY").toString().equals("IN") || r.get("MAPPED_ACCT_TY").toString().equals("57")) {
                    accTyp[0] = "IN-57:INCOME GENERATION";
                } else if (r.get("ACCT_TY").toString().equals("MFE") || r.get("MAPPED_ACCT_TY").toString().equals("28")) {
                    accTyp[1] = "MFE-28:MICRO CREDIT- ENTERPRISE";
                } else if (r.get("ACCT_TY").toString().equals("MFA") || r.get("MAPPED_ACCT_TY").toString().equals("24")) {
                    accTyp[2] = "MFA-24:MICRO CREDIT -AGRI INPUTS";
                } else if (r.get("ACCT_TY").toString().equals("PC") || r.get("MAPPED_ACCT_TY").toString().equals("38")) {
                    accTyp[3] = "PC-38:PERSONAL/CONSUMPTION";
                }
            }
        });

        ccpDetail.forEach(r -> {
            if (r.containsKey("ACCT_TY") || r.containsKey("MAPPED_ACCT_TY")) {
                if (r.get("ACCT_TY").toString().equals("IN") || r.get("MAPPED_ACCT_TY").toString().equals("57")) {
                    accTyp[0] = "IN-57:INCOME GENERATION";
                } else if (r.get("ACCT_TY").toString().equals("MFE") || r.get("MAPPED_ACCT_TY").toString().equals("28")) {
                    accTyp[1] = "MFE-28:MICRO CREDIT- ENTERPRISE";
                } else if (r.get("ACCT_TY").toString().equals("MFA") || r.get("MAPPED_ACCT_TY").toString().equals("24")) {
                    accTyp[2] = "MFA-24:MICRO CREDIT -AGRI INPUTS";
                } else if (r.get("ACCT_TY").toString().equals("PC") || r.get("MAPPED_ACCT_TY").toString().equals("38")) {
                    accTyp[3] = "PC-38:PERSONAL/CONSUMPTION";
                }
            }
        });

        params.put("report_type", delinq.size() != 0 ? "Strong Match" : defalt.size() != 0 ? "Strong Match" : "New File Created");
        params.put("typIN", accTyp[0]);
        params.put("typMFE", accTyp[1]);
        params.put("typMFA", accTyp[2]);
        params.put("typPC", accTyp[3]);

        return reportComponent.generateReport(Mfcib_Report, params, null);
    }

    private List<Map<String, Object>> getCcpMaster(Map<String, Object> objs, final boolean isSummery, final boolean isCppDetail) {

        List<Map<String, Object>> ccpMaster = new ArrayList<>();
        List<Map<String, Object>> ccpSummary = new ArrayList<>();
        List<Map<String, Object>> ccpDetail = new ArrayList<>();

        if (objs.containsKey("CCP_MASTER")) {
            if (isList(objs.get("CCP_MASTER"))) {
                ccpMaster = (List<Map<String, Object>>) objs.get("CCP_MASTER");
            } else {
                ccpMaster.add((Map<String, Object>) objs.get("CCP_MASTER"));
            }
        }

        if (objs.containsKey("CCP_SUMMARY")) {
            if (isList(objs.get("CCP_SUMMARY"))) {
                ccpSummary = (List<Map<String, Object>>) objs.get("CCP_SUMMARY");
            } else {
                ccpSummary.add((Map<String, Object>) objs.get("CCP_SUMMARY"));
            }
        }
        if (objs.containsKey("CCP_DETAIL")) {
            if (isList(objs.get("CCP_DETAIL"))) {
                ccpDetail = (List<Map<String, Object>>) objs.get("CCP_DETAIL");
            } else {
                ccpDetail.add((Map<String, Object>) objs.get("CCP_DETAIL"));
            }
        }

        List<Map<String, Object>> maps = new ArrayList<>();

        if (ccpMaster.get(0).get("FILE_NO").toString().isEmpty()) {
            return maps;
        }

        List<Map<String, Object>> finalCcpSummary = ccpSummary;
        List<Map<String, Object>> finalCcpDetail = ccpDetail;
        ccpMaster.forEach(ccp -> {
            Map<String, Object> map = new HashMap<>();
            map.putAll(ccp);
            if (isSummery) {
                finalCcpSummary.forEach(sm -> {
                    if (sm.get("SEQ_NO").toString().equals(ccp.get("SEQ_NO").toString())) {
                        map.putAll(sm);
                    }
                });
            }
            if (isCppDetail) {
                int count = 1;
                for (int i = 0; i < finalCcpDetail.size(); i++) {
                    Map<String, Object> detail = finalCcpDetail.get(i);
                    if (detail.get("SEQ_NO").toString().equals(ccp.get("SEQ_NO").toString())) {

                        int date = 0;
                        if (detail.get("STATUS_MONTH").toString().length() == 6) {
                            date = Integer.parseInt(detail.get("STATUS_MONTH").toString().substring(0, 2));
                            if (date == 0) {
                                date = Integer.parseInt(detail.get("STATUS_MONTH").toString().substring(1, 2));
                            }
                        } else if (detail.get("STATUS_MONTH").toString().length() == 5) {
                            date = Integer.parseInt(detail.get("STATUS_MONTH").toString().substring(0, 1));
                        }
                        StringBuffer buffer = new StringBuffer("");
                        if (date == 10 || date == 11 || date == 12) {
                            map.put("STATUS_MONTH_" + count, date);
                        } else if (date == 1) {
                            buffer = new StringBuffer(detail.get("STATUS_MONTH").toString());
                            if (buffer.length() == 6) {
                                buffer.insert(2, '/');
                            } else if (buffer.length() == 5) {
                                buffer.insert(0, "0").insert(2, '/');
                            }
                            map.put("STATUS_MONTH_" + count, buffer.toString());
                        } else {
                            map.put("STATUS_MONTH_" + count, date);
                        }

                        map.put("PAYMENT_STATUS_" + count, detail.get("PAYMENT_STATUS"));
                        map.put("OVERDUEAMOUNT_" + count, detail.get("OVERDUEAMOUNT"));
                        count++;
                    }
                }
            }
            maps.add(stringifyValues(map));
        });
        return maps;
    }

    private List<Map<String, Object>> getMapList(List<Map<String, Object>> mapObject) {
        List<Map<String, Object>> maps = new ArrayList<>();

        if (mapObject.get(0) != null) {
            if (mapObject.get(0).containsKey("FILE_NO")) {
                if (mapObject.get(0).get("FILE_NO").toString().isEmpty()) {
                    return maps;
                }
            }
        }

        mapObject.forEach(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.putAll(obj);
            maps.add(stringifyValues(map));
        });
        return maps;
    }

    private List<Map<String, Object>> getMapList(Map<String, Object> mapObject) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> maps = new ArrayList<>();
        if (mapObject.containsKey("FILE_NO")) {
            if (mapObject.get("FILE_NO").toString().isEmpty()) {
                return maps;
            }
        }
        map.putAll(mapObject);
        maps.add(stringifyValues(map));
        return maps;
    }

    public List<Map<String, Object>> getLists(Map<String, Object> map) {
        List<Map<String, Object>> maps = new ArrayList<>();
        maps.add(map);
        return maps;
    }

    private Map<String, Object> stringifyValues(Map<String, Object> variables) {
        return variables.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
//         .filter(m -> m.getKey() != null && m.getValue() !=null)
    }

    private boolean isList(Object object) {
        return object instanceof ArrayList;
    }

    public byte[] getDateWiseDisbFunds(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String crossTabScript;
        crossTabScript = readFile(Charset.defaultCharset(), "dateWiseDisbScript.txt");
        Query corssTabQuery = em.createNativeQuery(crossTabScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> crossTabResult = corssTabQuery.getResultList();
        List<Map<String, ?>> crosstTabMap = new ArrayList();

        crossTabResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DSBMT_DT", l[0] == null ? "" : l[0].toString());
            map.put("REMITTANCE_TYP", l[1] == null ? "" : l[1].toString());
            map.put("CLNTS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("DISB_AMOUNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());

            crosstTabMap.add(map);
        });

        params.put("dataset", getJRDataSource(crosstTabMap));

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "dateWiseDisbSmaryScript.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> tableRsult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableRsult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REMITTANCE_TYP", l[0] == null ? "" : l[0].toString());
            map.put("CLNTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DISB_AMOUNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(DATE_WISE_DISB_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(DATE_WISE_DISB, params, null);
        }
    }

    public byte[] getChannelWiseDisbFunds(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "channelWiseDisbScript.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REMITTANCE_TYP", l[0] == null ? "" : l[0].toString());
            map.put("CLNTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DISB_AMOUNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(CHANNEL_WISE_DISB_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(CHANNEL_WISE_DISB, params, null);
        }
    }

    public byte[] getBranchWiseDisbFunds(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String crossTabScript;
        crossTabScript = readFile(Charset.defaultCharset(), "branchWiseDisbScript.txt");
        Query corssTabQuery = em.createNativeQuery(crossTabScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> crossTabResult = corssTabQuery.getResultList();
        List<Map<String, ?>> crosstTabMap = new ArrayList();

        crossTabResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("REMITTANCE_TYP", l[1] == null ? "" : l[1].toString());
            map.put("CLNTS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("DISB_AMOUNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());

            crosstTabMap.add(map);
        });

        params.put("dataset", getJRDataSource(crosstTabMap));

        if (isxls) {
            return reportComponent.generateReport(BRANCH_WISE_DISB_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(BRANCH_WISE_DISB, params, null);
        }
    }

    public byte[] getProductWiseDisbFunds(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String crossTabScript;
        crossTabScript = readFile(Charset.defaultCharset(), "productWiseDisbScript.txt");
        Query corssTabQuery = em.createNativeQuery(crossTabScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> crossTabResult = corssTabQuery.getResultList();
        List<Map<String, ?>> crosstTabMap = new ArrayList();

        crossTabResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("PRD_NM", l[1] == null ? "" : l[1].toString());
            map.put("REMITTANCE_TYP", l[2] == null ? "" : l[2].toString());
            map.put("CLNTS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("DISB_AMOUNT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());

            crosstTabMap.add(map);
        });

        params.put("dataset", getJRDataSource(crosstTabMap));

        if (isxls) {
            return reportComponent.generateReport(PRODUCT_WISE_DISB_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(PRODUCT_WISE_DISB, params, null);
        }
    }

    public byte[] getBranchWiseReversal(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "brnchWiseReversalScript.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("CRTD_BY", l[1] == null ? "" : l[1].toString());
            map.put("GL_ACCT_NUM", l[2] == null ? "" : l[2].toString());
            map.put("TYP_STR", l[3] == null ? "" : l[3].toString());
            map.put("CLIENT_ID", l[4] == null ? "" : l[4].toString());
            map.put("INSTR_NO", l[5] == null ? "" : l[5].toString());
            map.put("JV_TYPE", l[6] == null ? "" : l[6].toString());
            map.put("JV_DSCR", l[7] == null ? "" : l[7].toString());
            map.put("ENTY_TYP", l[8] == null ? "" : l[8].toString());
            map.put("ACTUAL_JV_HDR_SEQ", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("ACTUAL_REF", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("ACTUAL_VALUE_DT", l[11] == null ? "" : l[11].toString());
            map.put("ACTUAL_DEBIT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("ACTUAL_CREDIT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("JV_HDR_SEQ", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("REV_REF_KEY", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("REVERSAL_DT", l[16] == null ? "" : l[16].toString());
            map.put("REV_DEBIT", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("REV_CREDIT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());

            tableMap.add(map);
        });

        params.put("dataset", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(BRANCH_WISE_REVERSAL_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(BRANCH_WISE_REVERSAL, params, null);
        }
    }


    public byte[] getDateWiseRecovery(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String crossTabScript;
        crossTabScript = readFile(Charset.defaultCharset(), "dateWiseRecoveryScript.txt");
        Query corssTabQuery = em.createNativeQuery(crossTabScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> crossTabResult = corssTabQuery.getResultList();
        List<Map<String, ?>> crosstTabMap = new ArrayList();

        crossTabResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PYMT_DT", l[0] == null ? "" : l[0].toString());
            map.put("TYP_STR", l[1] == null ? "" : l[1].toString());
            map.put("CLNTS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("AMT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());

            crosstTabMap.add(map);
        });

        params.put("dataset", getJRDataSource(crosstTabMap));

        if (isxls) {
            return reportComponent.generateReport(DATE_WISE_RECOVERY_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(DATE_WISE_RECOVERY, params, null);
        }
    }

    public byte[] getChannelWiseRecovery(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "channelWiseRecoveryScript.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("TYP_STR", l[0] == null ? "" : l[0].toString());
            map.put("TRANS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("REC_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(CHANNEL_WISE_RECOVERY_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(CHANNEL_WISE_RECOVERY, params, null);
        }
    }

    public byte[] getBranchWiseRecovery(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String crossTabScript;
        crossTabScript = readFile(Charset.defaultCharset(), "brnchWiseRecoveryScript.txt");
        Query corssTabQuery = em.createNativeQuery(crossTabScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> crossTabResult = corssTabQuery.getResultList();
        List<Map<String, ?>> crosstTabMap = new ArrayList();

        crossTabResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("TYP_STR", l[1] == null ? "" : l[1].toString());
            map.put("CLNTS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("REC_AMT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());

            crosstTabMap.add(map);
        });

        params.put("dataset", getJRDataSource(crosstTabMap));

        if (isxls) {
            return reportComponent.generateReport(BRANCH_WISE_RECOVERY_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(BRANCH_WISE_RECOVERY, params, null);
        }
    }

    public byte[] getADCsPostedRecovery(String user, String frmdt, String todt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "DailyADCsRecoveryPostedScript.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("frmdt", frmdt).setParameter("todt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_SEQ", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("PYMT_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("PYMT_DT", l[5] == null ? "" : l[5]);
            map.put("CRTD_DT", l[6] == null ? "" : l[6]);
            map.put("RCVRY_TRX_SEQ", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("TYP_STR", l[8] == null ? "" : l[8].toString());
            map.put("NARRATION", l[9] == null ? "" : l[9].toString());
            map.put("AUTO_ADC_RECOVERY", l[10] == null ? "" : l[10].toString());
            map.put("CRTD_BY", l[11] == null ? "" : l[11].toString());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(ADCs_POSTED_RECOVERY_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(ADCs_POSTED_RECOVERY, params, null);
        }
    }

    public byte[] getMcbRemiMappedBranch(String user, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "MCBRemiMappedBrnch.txt");
        Query tableQuery = em.createNativeQuery(tableScript);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("TYP_STR", l[1] == null ? "" : l[1].toString());
            map.put("BANK_CODE", l[2] == null ? "" : l[2].toString());
            map.put("BANK_ADDRESS", l[3] == null ? "" : l[3].toString());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(MCB_REMI_MAPPED_BRANCHES_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(MCB_REMI_MAPPED_BRANCHES, params, null);
        }
    }

    public byte[] getBankBranchMapped(String user, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "bankBrnchesMapedScript.txt");
        Query tableQuery = em.createNativeQuery(tableScript);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("BANK", l[1] == null ? "" : l[1].toString());
            map.put("IBAN", l[2] == null ? "" : l[2].toString());
            map.put("ACCT_NUM", l[3] == null ? "" : l[3].toString());
            map.put("BANK_BRNCH", l[4] == null ? "" : l[4].toString());
            map.put("BANK_CODE", l[5] == null ? "" : l[5].toString());
            map.put("IBFT_BANK_CODE", l[6] == null ? "" : l[6].toString());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(BANK_BRANCHES_MAPPED_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(BANK_BRANCHES_MAPPED, params, null);
        }
    }

    public byte[] getUblOmniMapped(String user, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "ublOmniMappedBrnchScript.txt");
        Query tableQuery = em.createNativeQuery(tableScript);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("BRNCH_PH_NUM", l[1] == null ? "" : l[1].toString());
            map.put("EMAIL", l[2] == null ? "" : l[2].toString());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        if (isxls) {
            return reportComponent.generateReport(UBL_OMNI_MAPPED_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(UBL_OMNI_MAPPED, params, null);
        }
    }

    public byte[] getMobileWalletMapped(String user, String typId, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String crossTabScript;
        crossTabScript = readFile(Charset.defaultCharset(), "mobileWalletMappedScript.txt");
        Query corssTabQuery = em.createNativeQuery(crossTabScript).setParameter("typId", typId);

        List<Object[]> crossTabResult = corssTabQuery.getResultList();
        List<Map<String, ?>> crosstTabMap = new ArrayList();

        crossTabResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("M_WALLET", l[1] == null ? "" : l[1].toString());

            crosstTabMap.add(map);
        });

        params.put("dataset", getJRDataSource(crosstTabMap));
        if (isxls) {
            return reportComponent.generateReport(MOBILE_WALLET_MAPPED_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(MOBILE_WALLET_MAPPED, params, null);
        }
    }

    public byte[] getAnimalTagReport(String toDt, long brnchSeq, String userId) throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("to_dt", toDt);
        params.put("curr_user", userId);
        String trendQury;
        trendQury = readFile(Charset.defaultCharset(), "animalMissingTagScript.txt");

        Query rs = em.createNativeQuery(trendQury).setParameter("todt", toDt)
                .setParameter("brnchSeq", brnchSeq);

        List<Object[]> trendObject = rs.getResultList();
        List<Map<String, ?>> trendsList = new ArrayList();
        trendObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_SEQ", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_NM", w[2] == null ? "" : w[2].toString());
            parm.put("DSBMT_DT", w[3] == null ? "" : w[3].toString());
            parm.put("CLNT_PH_NUM", w[4] == null ? "" : w[4].toString());
            parm.put("CLNT_ADDR", w[5] == null ? "" : w[5].toString());
            parm.put("NOM_NM", w[6] == null ? "" : w[6].toString());
            parm.put("NOM_PH_NUM", w[7] == null ? "" : w[7].toString());
            parm.put("PENDING_DYS", w[8] == null ? "" : w[8].toString());

            trendsList.add(parm);
        });
        return reportComponent.generateReport(ANIMAL_MISSING_TAG, params, getJRDataSource(trendsList));
    }

    public byte[] getPortfolioMonitoringKMReport(String fromDt, String toDt, String userId, long branch)
            throws IOException {

        Date frmDt = null;
        Date tooDt = null;
        try {
            frmDt = new SimpleDateFormat("dd-MM-yyyy").parse(fromDt);
            tooDt = new SimpleDateFormat("dd-MM-yyyy").parse(toDt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        fromDt = new SimpleDateFormat("dd-MMM-yyyy").format(frmDt);
        toDt = new SimpleDateFormat("dd-MMM-yyyy").format(tooDt);

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branch);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", fromDt);
        params.put("to_dt", toDt);

        String q;
        q = readFile(Charset.defaultCharset(), "PORTFOLIO_MONITORING_KM.txt");
        Query rs5 = em.createNativeQuery(q).setParameter("frmdt", fromDt).setParameter("todt", toDt)
                .setParameter("branchSeq", branch);
        List<Object[]> overdues = rs5.getResultList();

        List<Map<String, ?>> paymentsList = new ArrayList();
        for (Object[] r : overdues) {
            Map<String, Object> map = new HashMap();
//			if (r[4] != null && new BigDecimal(r[4].toString()).longValue() > 0) {
            map.put("PRD_GRP_NM", r[0].toString());
            map.put("EMP_NM", r[1].toString());
            map.put("EFF_START_DT", r[2] == null ? "" : r[2].toString());
            map.put("OST_CLNT", r[3] == null ? 0 : new BigDecimal(r[3].toString()).longValue());
            map.put("OST_AMT", r[4] == null ? 0 : new BigDecimal(r[4].toString()).longValue());
            map.put("DUE_PERD_CLNT", r[5] == null ? 0 : new BigDecimal(r[5].toString()).longValue());
            map.put("DUE_PERD_AMT", r[6] == null ? 0 : new BigDecimal(r[6].toString()).longValue());
            map.put("RCVRD_CLNT", r[7] == null ? 0 : new BigDecimal(r[7].toString()).longValue());
            map.put("RCVRD_AMT", r[8] == null ? 0 : new BigDecimal(r[8].toString()).longValue());
            map.put("TRGT_CLNT", r[9] == null ? 0 : new BigDecimal(r[9].toString()).longValue());
            map.put("ACHVD_IN_PERD", r[10] == null ? 0 : new BigDecimal(r[10].toString()).longValue());
            map.put("TRGT_AMT", r[11] == null ? 0 : new BigDecimal(r[11].toString()).longValue());
            map.put("ACHVD_IN_PERD_AMT", r[12] == null ? 0 : new BigDecimal(r[12].toString()).longValue());
            map.put("OD_CLNT", r[13] == null ? 0 : new BigDecimal(r[13].toString()).longValue());
            map.put("OD_AMT", r[14] == null ? 0 : new BigDecimal(r[14].toString()).longValue());
            map.put("PAR_1_DY_CNT", r[15] == null ? 0 : new BigDecimal(r[15].toString()).longValue());
            map.put("PAR_1_DY_AMT", r[16] == null ? 0 : new BigDecimal(r[16].toString()).longValue());
            map.put("PAR_30_DAY_CNT", r[17] == null ? 0 : new BigDecimal(r[17].toString()).longValue());
            map.put("PAR_30_DAY_AMT", r[18] == null ? 0 : new BigDecimal(r[18].toString()).longValue());
            map.put("CMPLTD_LOANS", r[19] == null ? 0 : new BigDecimal(r[19].toString()).longValue());
            map.put("OD_BP_CLNT", r[20] == null ? 0 : new BigDecimal(r[20].toString()).longValue());
            map.put("OD_BP_AMT0", r[21] == null ? 0 : new BigDecimal(r[21].toString()).longValue());
            map.put("ACHVD_IN_PERD_AGN", r[22] == null ? 0 : new BigDecimal(r[22].toString()).longValue());
            map.put("ACHVD_IN_PERD_AGN_AMT", r[23] == null ? 0 : new BigDecimal(r[23].toString()).longValue());
            map.put("ACHVD_IN_PERD_ACHVD_NEW", r[24] == null ? 0 : new BigDecimal(r[24].toString()).longValue());
            map.put("ACHVD_IN_PERD_AMT_ACHVD_NEW", r[25] == null ? 0 : new BigDecimal(r[25].toString()).longValue());
            map.put("ACHVD_IN_PERD_ACHVD_REP", r[26] == null ? 0 : new BigDecimal(r[26].toString()).longValue());
            map.put("ACHVD_IN_PERD_AMT_ACHVD_REP", r[27] == null ? 0 : new BigDecimal(r[27].toString()).longValue());

            paymentsList.add(map);
        }
//		}

        return reportComponent.generateReport(MONITORING_KM, params, getJRDataSource(paymentsList));
    }

    /**
     * @Update: Naveed
     * @SCR: Rescheduled Branch Reports (Operation)
     * @Date: 02-11-2022
     */
    public byte[] getRecoveryTrendAnalysis(String toDt, String frmdt, String userId, String roleType, long brnchSeq, long areaSeq, long regSeq, String type) throws IOException {
        Map<String, Object> params = new HashMap<>();

        Query bi;
        if (brnchSeq != -1) {
            bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                    brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
            params.put("area_cd", obj[5].toString());
        } else if (areaSeq != -1) {
            bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", obj[2].toString());
        } else if (regSeq != -1) {
            bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq",
                    regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[1].toString());
            params.put("area_nm", "ALL");
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", "");
        }
        params.put("from_dt", frmdt);
        params.put("to_dt", toDt);
        params.put("roleType", roleType);
        params.put("curr_user", userId);
        params.put("type", type);

        String trendQury = readFile(Charset.defaultCharset(), "recoveryTrendAnalysisPrdScript.txt");
        String prdDues = readFile(Charset.defaultCharset(), "recoveryTrendAnalysisPrdScriptNoOfDues.txt");

        Query prd = em.createNativeQuery(trendQury).setParameter("todt", toDt).setParameter("frmdt", frmdt)
                .setParameter("brnchSeq", brnchSeq).setParameter("p_user", userId).setParameter("P_TYPE", type);

        Query dues = em.createNativeQuery(prdDues).setParameter("todt", toDt).setParameter("frmdt", frmdt)
                .setParameter("brnchSeq", brnchSeq).setParameter("p_user", userId).setParameter("P_TYPE", type);

        List<Object[]> trendObject = prd.getResultList();
        List<Object[]> prdDuesObject = dues.getResultList();

        List<Map<String, ?>> trendsList = new ArrayList();
        trendObject.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("PORTFOLIO_TYPE", l[0] == null ? "" : l[0].toString());
            parm.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            parm.put("REC_TYP_CASH", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("REC_TYP_OTH", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            parm.put("REC_TYP_CASH_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("REC_TYP_OTH_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL_AMT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("OD_1_30_DYS", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            parm.put("OD_ABOVE_30_DYS", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("SAME_DY", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            parm.put("ADC_1_3_DY", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("ADC_4_7_DY", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            parm.put("ADC_8_15_DY", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            parm.put("ADC_16_30_DY", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            parm.put("ADC_ABOVE_30_DY", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());

            Object[] d = prdDuesObject.stream().filter(due -> l[17].toString().equals(due[1].toString())).findAny().orElse(null);
            if (d != null) {
                parm.put("TOT_DUE", d[3] == null ? 0 : new BigDecimal(d[3].toString()).longValue());
                parm.put("TOT_RCV", d[4] == null ? 0 : new BigDecimal(d[4].toString()).longValue());
            }
            trendsList.add(parm);
        });

        params.put("RCVRY_PRD", getJRDataSource(trendsList));
        params.put("RCVRY_PRD_1", getJRDataSource(trendsList));


        String bdoScript = readFile(Charset.defaultCharset(), "recoveryTrendAnalysisBdoScript.txt");
        String duesBdoScript = readFile(Charset.defaultCharset(), "recoveryTrendAnalysisBdoScriptNoOfDues.txt");

        Query bdoQuery = em.createNativeQuery(bdoScript).setParameter("todt", toDt).setParameter("frmdt", frmdt)
                .setParameter("brnchSeq", brnchSeq).setParameter("p_user", userId).setParameter("role_type", roleType).setParameter("P_TYPE", type);

        Query dueBdoQuery = em.createNativeQuery(duesBdoScript).setParameter("todt", toDt).setParameter("frmdt", frmdt)
                .setParameter("brnchSeq", brnchSeq).setParameter("p_user", userId).setParameter("role_type", roleType).setParameter("P_TYPE", type);

        List<Object[]> bdoObject = bdoQuery.getResultList();
        List<Object[]> dueBdoObject = dueBdoQuery.getResultList();

        List<Map<String, ?>> bdoList = new ArrayList();
        bdoObject.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("PORTFOLIO_TYPE", l[0] == null ? "" : l[0].toString());
            parm.put("REG_NM", l[1] == null ? "" : l[1].toString());
            parm.put("AREA_NM", l[2] == null ? "" : l[2].toString());
            parm.put("BRNCH_NM", l[3] == null ? "" : l[3].toString());
            parm.put("EMP_NM", l[4] == null ? "" : l[4].toString());
            parm.put("REC_TYP_CASH", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("REC_TYP_OTH", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("REC_TYP_CASH_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            parm.put("REC_TYP_OTH_AMT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            parm.put("OD_1_30_DYS", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("OD_ABOVE_30_DYS", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            parm.put("SAME_DY", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            parm.put("ADC_1_3_DY", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            parm.put("ADC_4_7_DY", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            parm.put("ADC_8_15_DY", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            parm.put("ADC_16_30_DY", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            parm.put("ADC_ABOVE_30_DY", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
//			parm.put("REC_TYP_TOT", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
//			parm.put("REC_TYP_TOT_AMT", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());

            Object[] d = dueBdoObject.stream().filter(due -> l[20].toString().equals(due[1].toString())).findAny().orElse(null);
            if (d != null) {
                parm.put("TOT_DUE", d[2] == null ? 0 : new BigDecimal(d[2].toString()).longValue());
                parm.put("TOT_RCV", d[3] == null ? 0 : new BigDecimal(d[3].toString()).longValue());
            }

            bdoList.add(parm);
        });

        params.put("RCVRY_BDO", getJRDataSource(bdoList));
        params.put("RCVRY_BDO_1", getJRDataSource(bdoList));

        return reportComponent.generateReport(RECOVERY_TREND_ANALYSIS, params, null);
    }

    public byte[] getPortfolioAtRisk(String asDt, String userId, String roleType, long brnchSeq, long areaSeq, long regSeq)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi;
        if (brnchSeq != -1) {
            bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                    brnchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
            params.put("area_cd", obj[5].toString());
        } else if (areaSeq != -1) {
            bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", obj[2].toString());
        } else if (regSeq != -1) {
            bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq",
                    regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[1].toString());
            params.put("area_nm", "ALL");
            params.put("brnch_nm", "ALL");
            params.put("brnch_cd", "");
            params.put("area_cd", "");
        }

        params.put("curr_user", userId);
        params.put("as_dt", asDt);
        params.put("roleType", roleType);

        String secQry;
        secQry = readFile(Charset.defaultCharset(), "portfolioAtRiskScript.txt");

        Query reulSet = em.createNativeQuery(secQry).setParameter("todt", asDt).setParameter("brnchSeq", brnchSeq)
                .setParameter("p_user", userId).setParameter("role_type", roleType);
        ;

        List<Object[]> secObj = reulSet.getResultList();

        List<Map<String, ?>> details = new ArrayList();

        secObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ORD", l[0] == null ? "" : l[0].toString());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("TOT_CLNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("APRVD_LOAN_AMT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("OST_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("PAR_1_DY_CNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("PAR_1_DAY_OD_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("PAR_1_DY_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("PAR_1_4_DAY_CNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("PAR_1_4_DAY_OD_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("PAR_1_4_DAY_AMT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("PAR_5_15_DAY_CNT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("PAR_5_15_DAY_OD_AMT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("PAR_5_15_DAY_AMT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("PAR_16_30_DAY_CNT", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("PAR_16_30_DAY_OD_AMT", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("PAR_16_30_DAY_AMT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("PAR_OVER_30_DAY_CNT", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("PAR_OVER_30_DAY_OD_AMT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("PAR_OVER_30_AMT", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());

            details.add(map);
        });

        String parQry;
        parQry = readFile(Charset.defaultCharset(), "PortfolioAtRiskReportParQry.txt");

        Query reSet1 = em.createNativeQuery(parQry).setParameter("todt", asDt).setParameter("brnchSeq", brnchSeq)
                .setParameter("p_user", userId).setParameter("role_type", roleType);
        ;

        List<Object[]> parObj = reSet1.getResultList();

        List<Map<String, ?>> par = new ArrayList();
        parObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BDO_NM", l[0] == null ? "" : l[0].toString());
            map.put("NAME", l[1] == null ? "" : l[1].toString());
            map.put("CLNT_ID", l[2] == null ? "" : l[2].toString());
            map.put("PH_NUM", l[3] == null ? "" : l[3].toString());
            map.put("ADDR", l[4] == null ? "" : l[4].toString());
            map.put("LOAN_CYCL_NUM", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("PRD_GRP_NM", l[6] == null ? "" : l[6].toString());
            map.put("DIS_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("OD_AMT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OD_DAYS", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("OD_INST", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("OST_BAL", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("NOM_NM", l[12] == null ? "" : l[12].toString());
            map.put("CMP_DT", l[13] == null ? "" : l[13].toString());
            map.put("PAID_INST", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            par.add(map);
        });
        params.put("par", getJRDataSource(par));

        return reportComponent.generateReport(PORTFOLIO_AT_RISK, params, getJRDataSource(details));
    }

    public byte[] getBmClientLoanMaturityReport(String userId, String toDt, long branchSeq)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("to_dt", toDt);

        String ql;
        ql = readFile(Charset.defaultCharset(), "BmClientsLoansMaturityReportScript.txt");
        Query rs = em.createNativeQuery(ql).setParameter("todt", toDt)
                .setParameter("brnch_seq", branchSeq);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("CLNT_SEQ", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_NM", w[2] == null ? "" : w[2].toString());
            parm.put("PH_NM", w[3] == null ? "" : w[3].toString());
            parm.put("LOAN_APP_SEQ", w[4] == null ? "" : w[4].toString());
            parm.put("DSBMT_DT", w[5] == null ? "" : w[5].toString());
            parm.put("CLNT_CNIC", w[6] == null ? "" : w[6].toString());
            parm.put("DSB_AMT", w[7] == null ? "" : w[7].toString());
            parm.put("OUTS", w[8] == null ? "" : w[8].toString());
            parm.put("OD_AMT", w[9] == null ? "" : w[9].toString());
            parm.put("OD_DYS", w[10] == null ? "" : w[10].toString());
            parm.put("REMITANCE_ACC_#", w[11] == null ? "" : w[11].toString());
            parm.put("REMITTANCE_TYP", w[12] == null ? "" : w[12].toString());
            parm.put("LOAN_MTURTY_DT", w[13] == null ? "" : w[13].toString());
            recList.add(parm);
        });

        return reportComponent.generateReport(BM_CLIENT_LOAN_MATRITY, params, getJRDataSource(recList));
    }

    public byte[] getBmMobileWalletDueReport(String userId, String frmDt, String toDt, long branchSeq)
            throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String detailQry;
        detailQry = readFile(Charset.defaultCharset(), "BmMobileWalletDisbursementDueScript.txt");

        Query result = em.createNativeQuery(detailQry).setParameter("frmdt", frmDt)
                .setParameter("todt", toDt).setParameter("brnch_seq", branchSeq);

        List<Object[]> dtlObj = result.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        dtlObj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PORT_TYP", l[0] == null ? "" : l[0].toString());
            map.put("EMP_SEQ", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("EMP_NM", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_ID", l[3] == null ? "" : l[3].toString());
            map.put("NAME", l[4] == null ? "" : l[4].toString());
            map.put("NOM_NM", l[5] == null ? "" : l[5].toString());
            map.put("PH_NUM", l[6] == null ? "" : l[6].toString());
            map.put("ADDR", l[7] == null ? "" : l[7].toString());
            map.put("INST_NUM", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("LOAN_CYCL_NUM", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("PRD_CMNT", l[10] == null ? "" : l[10].toString());
            map.put("INST_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("DUE_DT", l[12] == null ? "" : l[12].toString());
            map.put("OD_INST", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("OD_DAYS", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("PDC_HLDR_NM", l[15] == null ? "" : l[15].toString());
            map.put("PDC_HLDR_PHN", l[16] == null ? "" : l[16].toString());
            recList.add(map);
        });

        return reportComponent.generateReport(BM_MOBILE_WALLET_DUE, params, getJRDataSource(recList));
    }

    public byte[] getBmMobileWalletReport(String userId, String frmDt, String toDt, long branchSeq) throws IOException {
        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                branchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String ql;
        ql = readFile(Charset.defaultCharset(), "BmMobileWalletDisbursementScript.txt");
        Query rs = em.createNativeQuery(ql).setParameter("todt", toDt).setParameter("frmdt", frmDt)
                .setParameter("brnch_seq", branchSeq);
        List<Object[]> result = rs.getResultList();

        List<Map<String, ?>> recList = new ArrayList();
        result.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("EMP_NM", w[0] == null ? "" : w[0].toString());
            parm.put("DSBMT_DT", w[1] == null ? "" : w[1].toString());
            parm.put("CLNT_SEQ", w[2] == null ? "" : w[2].toString());
            parm.put("CLNT_NM", w[3] == null ? "" : w[3].toString());
            parm.put("LOAN_APP_SEQ", w[4] == null ? "" : w[4].toString());
            parm.put("CLNT_CNIC", w[5] == null ? "" : w[5].toString());
            parm.put("REMITANCE_ACC_#", w[6] == null ? "" : w[6].toString());
            parm.put("AMOUNT", w[7] == null ? "" : w[7].toString());
            parm.put("REMITTANCE_TYP", w[8] == null ? "" : w[8].toString());

            recList.add(parm);
        });

        return reportComponent.generateReport(MB_MOBILE_WALLET, params, getJRDataSource(recList));
    }

    /*
     * Modified by Zohaib Asim - Dated 29-07-2021 - CR: Sanction List
     * In-Valid CNIC List (Either Nulls, Not equal to 13 in Length)
     */
    public byte[] getSancInValidClientsReport(String fileType, boolean inValidCnic) {
        Map<String, Object> params = new HashMap<>();
        String whereSancType = "";
        if (fileType.toUpperCase().equals("NACTA")) {
            whereSancType = "NACTA";
        } else if (fileType.toUpperCase().equals("SANCTION")) {
            whereSancType = "S-";
        }

        // In DB isValidCNIC is 1, if it meet the length and country
        // If inValidCnic is true, so system should fetch with 0
        String query = readFile(Charset.defaultCharset(), "SANC_INVALID_CNIC_LIST.txt");
        Query rs = em.createNativeQuery(query).setParameter("P_CRNT_REC_FLG", 1L)
                .setParameter("P_IS_VALID_CNIC", inValidCnic == true ? 0 : 1)
                .setParameter("P_SANC_TYP", whereSancType);
        List<Object[]> cnicRecords = rs.getResultList();

        List<Map<String, ?>> result = new ArrayList();

        cnicRecords.forEach(record -> {
            Map<String, Object> mapList = new HashMap<>();

            mapList.put("SANC_SEQ", record[0] == null ? "" : Long.parseLong(record[0].toString()));
            mapList.put("NATIONAL_ID", record[1] == null ? "" : record[1].toString());
            mapList.put("CNIC_NUM", record[2] == null ? "" : record[2].toString());
            mapList.put("FIRST_NM", record[3] == null ? "" : record[3].toString());
            mapList.put("LAST_NM", record[4] == null ? "" : record[4].toString());
            mapList.put("FATHER_NM", record[5] == null ? "" : record[5].toString());
            mapList.put("CNTRY", record[6] == null ? "" : record[6].toString());
            mapList.put("PRVNCE", record[7] == null ? "" : record[7].toString());
            mapList.put("DSTRCT", record[8] == null ? "" : record[8].toString());
            mapList.put("DOB", record[9] == null ? null : Timestamp.valueOf(record[9].toString()));
            mapList.put("REF_NO", record[10] == null ? "" : record[10].toString());
            mapList.put("SANC_TYPE", record[11] == null ? "" : record[11].toString());

            result.add(mapList);
        });
        params.put("NACTA_SANC", getJRDataSource(result));
        return reportComponent.generateReport(NACTA_MANAGMENT, params, getJRDataSource(result), true);
    }

    /*
     * Modified by Zohaib Asim - Dated 29-07-2021 - CR: Sanction List
     * By Using File Type Parameter, it will fetch Matched Clients)
     */
    public byte[] getSancMatchReport(String fileType, boolean isMtchFound) {

        Map<String, Object> params = new HashMap<>();
        List<Map<String, ?>> result = new ArrayList();

        if (fileType.toUpperCase().equals("NACTA") || fileType.toUpperCase().equals("SANCTION")) {
            String whereSancType = "";
            if (fileType.toUpperCase().equals("NACTA")) {
                whereSancType = "NACTA";
            } else if (fileType.toUpperCase().equals("SANCTION")) {
                whereSancType = "S-";
            }

            String query = readFile(Charset.defaultCharset(), "SANC_MATCH_LIST.txt");
            Query rs = em.createNativeQuery(query).setParameter("P_CRNT_REC_FLG", 1L)
                    .setParameter("P_IS_MTCH_FOUND", isMtchFound == true ? 1 : 0)
                    .setParameter("P_SANC_TYP", whereSancType);

            List<Object[]> cnicRecords = rs.getResultList();

            cnicRecords.forEach(record -> {
                Map<String, Object> mapList = new HashMap<>();

                mapList.put("SANC_SEQ", record[0] == null ? "" : Long.parseLong(record[0].toString()));
                mapList.put("NATIONAL_ID", record[1] == null ? "" : record[1].toString());
                mapList.put("CNIC_NUM", record[2] == null ? "" : record[2].toString());
                mapList.put("FIRST_NM", record[3] == null ? "" : record[3].toString());
                mapList.put("LAST_NM", record[4] == null ? "" : record[4].toString());
                mapList.put("FATHER_NM", record[5] == null ? "" : record[5].toString());
                mapList.put("CNTRY", record[6] == null ? "" : record[6].toString());
                mapList.put("PRVNCE", record[7] == null ? "" : record[7].toString());
                mapList.put("DSTRCT", record[8] == null ? "" : record[8].toString());
                mapList.put("DOB", record[9] == null ? null : Timestamp.valueOf(record[9].toString()));
                mapList.put("REF_NO", record[10] == null ? "" : record[10].toString());
                mapList.put("SANC_TYPE", record[11] == null ? "" : record[11].toString());

                result.add(mapList);
            });
            params.put("NACTA_SANC", getJRDataSource(result));

        } else if (fileType.toUpperCase().equals("TAG") || fileType.toUpperCase().equals("UNTAG")) {
            String whereTagType = "";
            if (fileType.toUpperCase().equals("TAG")) {
                whereTagType = " CTL.CRNT_REC_FLG = 1 ";
            } else if (fileType.toUpperCase().equals("UNTAG")) {
                whereTagType = " CTL.CRNT_REC_FLG = 0 AND CTL.DEL_FLG = 1 ";
            }

            String query = readFile(Charset.defaultCharset(), "CLNT_TAG_MATCH_LIST.txt");
            query = query.replaceAll(":whereTagType", whereTagType);
            Query rs = em.createNativeQuery(query);

            List<Object[]> cnicRecords = rs.getResultList();

            cnicRecords.forEach(record -> {
                Map<String, Object> mapList = new HashMap<>();

                mapList.put("LOAN_APP_SEQ", record[0] == null ? "" : record[0].toString());
                mapList.put("CNIC_NUM", record[1] == null ? "" : record[1].toString());
                mapList.put("RMKS", record[2] == null ? "" : record[2].toString());
                mapList.put("EFF_START_DT", record[3] == null ? "" : record[3].toString());
                mapList.put("TAG_TO_DT", record[4] == null ? "" : record[4].toString());
                mapList.put("TAG_FROM_DT", record[5] == null ? "" : record[5].toString());
                mapList.put("LAST_UPD_BY", record[6] == null ? "" : record[6].toString());
                mapList.put("LAST_UPD_DT", record[7] == null ? "" : record[7].toString());

                result.add(mapList);
            });
            params.put("TAG_UNTAG", getJRDataSource(result));
        }
        return reportComponent.generateReport(NACTA_MANAGMENT, params, getJRDataSource(result), true);
    }


    // Added By Naveed - Date - 13-10-2021
    // Telenor Collection Report - SCR-EasyPaisa Integration
    // Modified By Naveed - Date - 23-01-2022
    // SCR- Munsalik Integration
    public byte[] getTelenorCollectionReport(String user, String frmdt, String todt, long brnchSeq, String channelSeq, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String channelScript = readFile(Charset.defaultCharset(), "telenorCollectionScript.txt");
        Query channelQuery = em.createNativeQuery(channelScript)
                .setParameter("frmdt", frmdt).setParameter("todt", todt).setParameter("brnchSeq", brnchSeq)
                .setParameter("adcSeq", channelSeq);

        List<Object[]> channelResult = channelQuery.getResultList();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        List<Map<String, ?>> channelMap = new ArrayList();

        channelResult.forEach(json -> {
            ChannelCollectionDto channelDto = new ChannelCollectionDto();
            Map<String, Object> map = new HashMap();
            try {
                Clob clob = (Clob) json[0];
                channelDto.execDate = json[1] == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(json[1].toString());
                channelDto.branch = json[2] == null ? "" : json[2].toString();
                channelDto.adc_nm = json[3] == null ? "" : json[3].toString();
                channelDto.agent = "-";


                if (EASY_PAISA.equals(channelSeq)) {
                    TelenorCollectionDTO telenorDto = objectMapper.readValue(clob.getSubString(1, (int) clob.length()), TelenorCollectionDTO.class);
                    Clients client = clntRepository.findByClntSeqAndCrntRecFlg(new BigDecimal(telenorDto.consumer_number).longValue(), true);

                    channelDto.tranDate = telenorDto.tran_Date == null ? null : new SimpleDateFormat("yyyyMMdd").parse(telenorDto.tran_Date);
                    channelDto.tranTime = telenorDto.tran_Time == null ? null : new SimpleDateFormat("HHmmss").parse(telenorDto.tran_Time);
                    channelDto.clientId = telenorDto.consumer_number == null ? "" : telenorDto.consumer_number;
                    channelDto.clientName = client == null ? "" : client.getFrstNm() + " " + client.getLastNm();
                    channelDto.tranNum = telenorDto.tran_Auth_Id == null ? "" : telenorDto.tran_Auth_Id;
                    channelDto.recovery = telenorDto.transaction_Amount == null ? 0 : new BigDecimal(telenorDto.transaction_Amount).longValue();
                    channelDto.diff = hmsFromDate(json[1].toString(), telenorDto.tran_Date + " " + telenorDto.tran_Time);

                } else if (MUNSALIK.equals(channelSeq)) {
                    MunsalikCollectionDto munsalikdto = objectMapper.readValue(clob.getSubString(1, (int) clob.length()), MunsalikCollectionDto.class);
                    Clients client = clntRepository.findByClntSeqAndCrntRecFlg(new BigDecimal(munsalikdto.utilityConsumerNumber.substring(3)).longValue(), true);

                    channelDto.tranDate = munsalikdto.transmissionDate == null ? null : new SimpleDateFormat("yyyyMMdd").parse(munsalikdto.transmissionDate);
                    channelDto.tranTime = munsalikdto.transmissionTime == null ? null : new SimpleDateFormat("HHmmss").parse(munsalikdto.transmissionTime);
                    channelDto.clientId = client == null ? "" : client.getClntId();
                    channelDto.clientName = client == null ? "" : client.getFrstNm() + " " + client.getLastNm();
                    channelDto.tranNum = munsalikdto.rrn == null ? "" : munsalikdto.rrn;
                    channelDto.recovery = munsalikdto.transactionAmount == null ? 0 : new BigDecimal(munsalikdto.transactionAmount).longValue();
                    channelDto.diff = hmsFromDate(json[1].toString(), munsalikdto.transmissionDate + " " + munsalikdto.transmissionTime);
                } else if (UBL_OMNI.equals(channelSeq)) {
                    UBLCollectionDto ublDto = objectMapper.readValue(clob.getSubString(1, (int) clob.length()), UBLCollectionDto.class);
                    Clients client = clntRepository.findByClntSeqAndCrntRecFlg(new BigDecimal(ublDto.utilityConsumerNumber).longValue(), true);

                    channelDto.tranDate = ublDto.transmissionDate == null ? null : new SimpleDateFormat("yyyyMMdd").parse(ublDto.transmissionDate);
                    channelDto.tranTime = ublDto.transmissionTime == null ? null : new SimpleDateFormat("HHmmss").parse(ublDto.transmissionTime);
                    channelDto.clientId = client == null ? "" : client.getClntId();
                    channelDto.clientName = client == null ? "" : client.getFrstNm() + " " + client.getLastNm();
                    channelDto.tranNum = ublDto.rrn == null ? "" : ublDto.rrn;
                    channelDto.recovery = ublDto.transactionAmount == null ? 0 : new BigDecimal(ublDto.transactionAmount).longValue();
                    channelDto.diff = hmsFromDate(json[1].toString(), ublDto.transmissionDate + " " + ublDto.transmissionTime);
                } else if (HBL.equals(channelSeq)) {
                    HBLCollectionDto hblDto = objectMapper.readValue(clob.getSubString(1, (int) clob.length()), HBLCollectionDto.class);
                    Clients client = clntRepository.findByClntSeqAndCrntRecFlg(new BigDecimal(hblDto.utilityConsumerNumber).longValue(), true);

                    channelDto.tranDate = hblDto.transmissionDate == null ? null : new SimpleDateFormat("yyyyMMdd").parse(hblDto.transmissionDate);
                    channelDto.tranTime = hblDto.transmissionTime == null ? null : new SimpleDateFormat("HHmmss").parse(hblDto.transmissionTime);
                    channelDto.clientId = client == null ? "" : client.getClntId();
                    channelDto.clientName = client == null ? "" : client.getFrstNm() + " " + client.getLastNm();
                    channelDto.tranNum = hblDto.stan == null ? "" : StringUtils.stripStart(hblDto.stan, "0");
                    channelDto.recovery = hblDto.transactionAmount == null ? 0 : new BigDecimal(hblDto.transactionAmount).longValue();
                    channelDto.diff = hmsFromDate(json[1].toString(), hblDto.transmissionDate + " " + hblDto.transmissionTime);
                }

                map.put("EXEC_DATE", channelDto.execDate);
                map.put("TRAN_DATE", channelDto.tranDate);
                map.put("TRAN_TIME", channelDto.tranTime);
                map.put("CLIENT_ID", channelDto.clientId);
                map.put("CLIENT_NAME", channelDto.clientName);
                map.put("AGENT", channelDto.agent);
                map.put("TRAN_NUM", channelDto.tranNum);
                map.put("RECOVRY", channelDto.recovery);
                map.put("DIFF", channelDto.diff);
                map.put("BRANCH", channelDto.branch);
                map.put("ADC_NM", channelDto.adc_nm);

                channelMap.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        params.put("dataset", getJRDataSource(channelMap));

        if (isxls) {
            return reportComponent.generateReport(TELENOR_COLLECTION_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(TELENOR_COLLECTION, params, null);
        }
    }

    private String hmsFromDate(String exec, String trans) throws ParseException {
        Date execDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(exec);
        Date transDate = new SimpleDateFormat("yyyyMMdd HHmmss").parse(trans);

        long millis = (execDate.getTime() - transDate.getTime());
        return String.format("%02d:%02d:%02d", Math.abs(TimeUnit.MILLISECONDS.toHours(millis)),
                Math.abs(TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))),
                Math.abs(TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));

    }
    // Ended By Naveed - Date - 13-10-2021

    // Added By Naveed - Date - 20-10-2021
    // Tasdeeq Report - Integration
    public byte[] getMfcibReport(long loanApp, long clientCnic, long type) throws Exception {

        // Modified By Zohaib
        //
        Clob clob = null;
        String script = "SELECT DOC.DOC_IMG, DOC.COMPANY_NM \n" +
                "FROM  MW_LOAN_APP_DOC DOC \n" +
                "WHERE DOC.CRNT_REC_FLG =1 AND DOC.DOC_SEQ = :type AND DOC.LOAN_APP_SEQ = :loanApp AND DOC.CNIC_NUM = :P_CNIC_NUM\n" +
                "ORDER BY DOC.CRTD_DT DESC";
        Query query = em.createNativeQuery(script).setParameter("loanApp", loanApp).setParameter("type", type).setParameter("P_CNIC_NUM", clientCnic);
        List<Object[]> result = query.getResultList();

        if (result.size() < 1) {
            return new byte[0];
        }

        clob = result.get(0)[0] == null ? null : (Clob) result.get(0)[0];
        String companyName = result.get(0)[1] == null ? "" : (String) result.get(0)[1];

        if (clob == null || companyName.isEmpty()) {
            return new byte[0];
        }

        // Modified By Naveed - Date 28-03-2022
        // common one pager for Both
        if (companyName.equals("DATACHECK")) {
//			return getMfcib(clob);
            return getDataCheck(clob);
        } else if (companyName.equals("TASDEEQ")) {
//			return getTessdeq(clob);
            return getTASDEEQ(clob);
        } else {
            return new byte[0];
        }
    }

    public byte[] getTessdeq(Clob clob) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonObject = "";
        Map<String, Map<String, Object>> map = new HashMap<>();

        try {
            jsonObject = clob == null ? null : clob.getSubString(1, (int) clob.length());
            map = objectMapper.readValue(jsonObject, Map.class);
        } catch (Exception exp) {
            throw new Exception(exp);
        }

        Map<String, Object> params = new HashMap<>();
        Map<String, Object> root = new HashMap<>();

        if (map.containsKey("statusCode") && map.containsKey("messageCode")) {
            Object statusCode = map.get("statusCode");
            Object messageCode = map.get("messageCode");
            if ((statusCode.toString().equals("111")) && (messageCode.toString().equals("00922001"))) {
                root = (Map<String, Object>) map.get("data");

            }
        } else {
            return new byte[0];
        }

        List<Map<String, Object>> persInfo = new ArrayList<>();
        List<Map<String, Object>> sumryInfo = new ArrayList<>();
        List<Map<String, Object>> summaryOverdue = new ArrayList<>();
        List<Map<String, Object>> detailsOfStatusCreditApp = new ArrayList<>();
        List<Map<String, Object>> detailsOfLoansSettl = new ArrayList<>();
        List<Map<String, Object>> personalGuarantees = new ArrayList<>();
        List<Map<String, Object>> coborrowerDetail = new ArrayList<>();
        List<Map<String, Object>> detailsOfBankruptcyCases = new ArrayList<>();
        List<Map<String, Object>> creditEnquiry = new ArrayList<>();
        List<Map<String, Object>> loanDetails = new ArrayList<>();

        if (root.containsKey("personalInformation")) {
            persInfo = getList((Map<String, Object>) root.get("personalInformation"));
        }

        if (root.containsKey("summaryOverdue_24M")) {
            summaryOverdue = getList((List<Map<String, Object>>) root.get("summaryOverdue_24M"));
        }

        if (root.containsKey("detailsOfStatusCreditApplication")) {
            detailsOfStatusCreditApp = getList((List<Map<String, Object>>) root.get("detailsOfStatusCreditApplication"));
        }

        if (root.containsKey("detailsOfLoansSettlement")) {
            detailsOfLoansSettl = getList((List<Map<String, Object>>) root.get("detailsOfLoansSettlement"));
        }

        if (root.containsKey("personalGuarantees")) {
            personalGuarantees = getList((List<Map<String, Object>>) root.get("personalGuarantees"));
        }

        if (root.containsKey("coborrowerDetail")) {
            coborrowerDetail = getList((List<Map<String, Object>>) root.get("coborrowerDetail"));
        }

        if (root.containsKey("detailsOfBankruptcyCases")) {
            detailsOfBankruptcyCases = getList((List<Map<String, Object>>) root.get("detailsOfBankruptcyCases"));
        }

        if (root.containsKey("creditEnquiry")) {
            creditEnquiry = getList((List<Map<String, Object>>) root.get("creditEnquiry"));
        }

        if (root.containsKey("reportDate")) {
            params.put("reportDate", root.get("reportDate").toString());
        }

        if (root.containsKey("reportTime")) {
            params.put("reportTime", root.get("reportTime").toString());
        }

        if (root.containsKey("refNo")) {
            params.put("refNo", root.get("refNo").toString());
        }

        if (root.containsKey("noOfCreditEnquiry")) {
            params.put("noOfCreditEnquiry", root.get("noOfCreditEnquiry").toString());
        }

        if (root.containsKey("noOfActiveAccounts")) {
            params.put("noOfActiveAccounts", root.get("noOfActiveAccounts").toString());
        }

        if (root.containsKey("totalOutstandingBalance")) {
            params.put("totalOutstandingBalance", root.get("totalOutstandingBalance").toString());
        }

        if (root.containsKey("disclaimerText")) {
            params.put("disclaimerText", root.get("disclaimerText").toString());
        }

        if (root.containsKey("remarks")) {
            params.put("remarks", root.get("remarks").toString());
        }

        loanDetails = loanDetailDataSet(root);

        params.put("persInfo", getJRDataSource(persInfo));
        params.put("sumryInfo", getJRDataSource(coborrowerDetail));
        params.put("summaryOverdue", getJRDataSource(summaryOverdue));
        params.put("detailsOfStatusCreditApp", getJRDataSource(detailsOfStatusCreditApp));
        params.put("detailsOfLoansSettl", getJRDataSource(detailsOfLoansSettl));
        params.put("personalGuarantees", getJRDataSource(personalGuarantees));
        params.put("coborrowerDetail", getJRDataSource(coborrowerDetail));
        params.put("detailsOfBankruptcyCases", getJRDataSource(detailsOfBankruptcyCases));
        params.put("creditEnquiry", getJRDataSource(creditEnquiry));
        params.put("loanDetails", getJRDataSource(loanDetails));

        byte[] bytes = reportComponent.generateReport(TASDEEQ_REPORT, params, null);
        return bytes;
    }

    private List<Map<String, Object>> getList(List<Map<String, Object>> mapObject) {
        List<Map<String, Object>> maps = new ArrayList<>();
        mapObject.forEach(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.putAll(obj);
            maps.add(stringifyValues(map));
        });
        return maps;
    }

    private List<Map<String, Object>> getList(Map<String, Object> mapObject) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> maps = new ArrayList<>();
        map.putAll(mapObject);
        maps.add(stringifyValues(map));
        return maps;
    }

    private List<Map<String, Object>> loanDetailDataSet(Map<String, Object> root) {

        List<Map<String, Object>> loanDetails = new ArrayList<>();
        List<Map<String, Object>> loanDetailsMapList = new ArrayList<>();
        List<Map<String, Object>> loanHistory = new ArrayList<>();

        if (root.containsKey("loanDetails")) {
            loanDetails = (List<Map<String, Object>>) root.get("loanDetails");

            if (root.containsKey("creditHistory")) {
                loanHistory = (List<Map<String, Object>>) root.get("creditHistory");
            }

            for (Map<String, Object> loanDetail : loanDetails) {
                Map<String, Object> loanDetailsMap = new HashMap<>();
                if (loanDetail.containsKey("LOAN_SERIAL_NUMBER")) {
                    int i = 1;
                    String loanNumber = loanDetail.get("LOAN_SERIAL_NUMBER").toString();
                    loanDetailsMap.putAll(loanDetail);
                    for (Map<String, Object> hist : loanHistory) {
                        if (hist.get("LOAN_SERIAL_NUMBER").toString().equals(loanNumber)) {
                            loanDetailsMap.put("MONTH_NAME_" + i, hist.get("MONTH_NAME"));
                            loanDetailsMap.put("PLUS_30_" + i, hist.get("PLUS_30"));
                            loanDetailsMap.put("PLUS_60_" + i, hist.get("PLUS_60"));
                            loanDetailsMap.put("PLUS_90_" + i, hist.get("PLUS_90"));
                            loanDetailsMap.put("PLUS_120_" + i, hist.get("PLUS_120"));
                            loanDetailsMap.put("PLUS_150_" + i, hist.get("PLUS_150"));
                            loanDetailsMap.put("PLUS_180_" + i, hist.get("PLUS_180"));
                            loanDetailsMap.put("MFI_DEFAULT_" + i, hist.get("MFI_DEFAULT"));
                            loanDetailsMap.put("LATE_PMT_DAYS_" + i, hist.get("LATE_PMT_DAYS"));
                            i++;
                        }
                    }
                }
                loanDetailsMapList.add(stringifyValues(loanDetailsMap));
            }
        }
        return loanDetailsMapList;
    }
    //Ended BY Naveed - Dated - 20-10-2021

    // Added By Naveed - Date - 04-11-2021
    // Inquiries Telenor Collection Report - SCR-EasyPaisa Integration
    // Modifiled By Naveed - Date - 23-01-2022
    // SCR- Munsalik Integration
    public byte[] getInquiriesTelenorCollectionReport(String user, String frmdt, String todt, long brnchSeq, String channelSeq, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", user);
        params.put("from_dt", frmdt);
        params.put("to_dt", todt);

        String telenorscript;
        telenorscript = readFile(Charset.defaultCharset(), "InquiriesTelenorCollectionScript.txt");
        Query telenorQuery = em.createNativeQuery(telenorscript)
                .setParameter("frmdt", frmdt).setParameter("todt", todt).setParameter("brnchSeq", brnchSeq)
                .setParameter("adcSeq", channelSeq);
        List<Object[]> telenorResult = telenorQuery.getResultList();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        List<Map<String, ?>> listsMap = new ArrayList();

        telenorResult.forEach(record -> {
            Map<String, Object> map = new HashMap();
            ChannelCollectionDto channelDto = new ChannelCollectionDto();
            try {
                Clob clob = (Clob) record[0];
                channelDto.inquirySeq = record[1] == null ? "" : record[1].toString();
                channelDto.crtdDate = record[2] == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(record[2].toString());
                channelDto.branch = record[3] == null ? "" : record[3].toString();
                channelDto.bdoName = record[4] == null ? "" : record[4].toString();
                channelDto.clientSeq = record[5] == null ? "" : record[5].toString();
                channelDto.inquiryStatus = record[6] == null ? "" : record[6].toString();
                channelDto.loanAppSeq = record[7] == null ? "" : record[7].toString();
                channelDto.crtdBy = record[8] == null ? "" : record[8].toString();
                channelDto.remarks = record[9] == null ? "" : record[9].toString();
                channelDto.noOfCount = record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue();
                channelDto.channelName = record[11] == null ? "" : record[11].toString();

                String jsonObject = getClobString((Clob) record[0]).replaceAll("([a-zA-Z0-9-_]+)=([a-zA-Z0-9-+ ]+|)", "$1:\"$2\"");

                if (EASY_PAISA.equals(channelSeq)) {
                    TelenorCollectionDTO dto = objectMapper.readValue(jsonObject, TelenorCollectionDTO.class);
                    channelDto.paidAmount = dto.amount_Paid == null || dto.amount_Paid.isEmpty() ? 0 : new BigDecimal(dto.amount_Paid).longValue();
                    channelDto.withinAmount = dto.amount_Within_DueDate == null || dto.amount_Within_DueDate.isEmpty() ? 0 : new BigDecimal(dto.amount_Within_DueDate).longValue();
                    channelDto.afterAmount = dto.amount_After_DueDate == null || dto.amount_After_DueDate.isEmpty() ? 0 : new BigDecimal(dto.amount_After_DueDate).longValue();
                    channelDto.amount = dto.amount_Paid == null || dto.amount_Paid.isEmpty() ? 0 : new BigDecimal(dto.amount_Paid).longValue();
                    channelDto.dueDate = dto.due_Date == null || dto.due_Date.isEmpty() ? null : new SimpleDateFormat("yyyyMMdd").parse(dto.due_Date);
                } else if (MUNSALIK.equals(channelSeq)) {
                    MunsalikCollectionDto dto = objectMapper.readValue(jsonObject, MunsalikCollectionDto.class);
                    channelDto.paidAmount = dto.amountPaid == null || dto.amountPaid.isEmpty() ? 0 : new BigDecimal(dto.amountPaid).longValue();
                    channelDto.withinAmount = dto.amountDueDate == null || dto.amountDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountDueDate).longValue();
                    channelDto.afterAmount = dto.amountAfterDueDate == null || dto.amountAfterDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountAfterDueDate).longValue();
                    channelDto.amount = dto.amountPaid == null || dto.amountPaid.isEmpty() ? 0 : new BigDecimal(dto.amountPaid).longValue();
                    channelDto.dueDate = dto.dueDate == null || dto.dueDate.isEmpty() ? null : new SimpleDateFormat("yyyyMMdd").parse(dto.dueDate);
                } else if (UBL_OMNI.equals(channelSeq)) {
                    UBLCollectionDto dto = objectMapper.readValue(jsonObject, UBLCollectionDto.class);
                    channelDto.paidAmount = dto.amountDueDate == null || dto.amountDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountDueDate).longValue();
                    channelDto.withinAmount = dto.amountDueDate == null || dto.amountDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountDueDate).longValue();
                    channelDto.afterAmount = dto.amountAfterDueDate == null || dto.amountAfterDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountAfterDueDate).longValue();
                    channelDto.amount = dto.amountDueDate == null || dto.amountDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountDueDate).longValue();
                    channelDto.dueDate = dto.dueDate == null || dto.dueDate.isEmpty() ? null : new SimpleDateFormat("yyyyMMdd").parse(dto.dueDate);
                } else if (HBL.equals(channelSeq)) {
                    HBLCollectionDto dto = objectMapper.readValue(jsonObject, HBLCollectionDto.class);
                    channelDto.paidAmount = dto.amountDueDate == null || dto.amountDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountDueDate).longValue();
                    channelDto.withinAmount = dto.amountDueDate == null || dto.amountDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountDueDate).longValue();
                    channelDto.afterAmount = dto.amountAfterDueDate == null || dto.amountAfterDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountAfterDueDate).longValue();
                    channelDto.amount = dto.amountDueDate == null || dto.amountDueDate.isEmpty() ? 0 : new BigDecimal(dto.amountDueDate).longValue();
                    channelDto.dueDate = dto.dueDate == null || dto.dueDate.isEmpty() ? null : new SimpleDateFormat("yyyyMMdd").parse(dto.dueDate);
                }

                map.put("INQUIRY_SEQ", channelDto.inquirySeq);
                map.put("CRTD_DATE", channelDto.crtdDate);
                map.put("BRANCH", channelDto.branch);
                map.put("BDO_NAME", channelDto.bdoName);
                map.put("CLIENT_SEQ", channelDto.clientSeq);
                map.put("INQUIRY_STATUS", channelDto.inquiryStatus);
                map.put("LOAN_APP_SEQ", channelDto.loanAppSeq);
                map.put("CRTD_BY", channelDto.crtdBy);
                map.put("REMARKS", channelDto.remarks);
                map.put("NO_COUNT", channelDto.noOfCount);
                map.put("ADC_NM", channelDto.channelName);
                map.put("PAID_AMOUNT", channelDto.paidAmount);
                map.put("WITHIN_AMOUNT", channelDto.withinAmount);
                map.put("AFTER_AMOUNT", channelDto.afterAmount);
                map.put("AMOUNT", channelDto.amount);
                map.put("DUE_DATE", channelDto.dueDate);

                listsMap.add(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        params.put("dataset", getJRDataSource(listsMap));
        if (isxls) {
            return reportComponent.generateReport(INQUIRY_TELENOR_COLLECTION_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(INQUIRY_TELENOR_COLLECTION, params, null);
        }
    }
    // Added By Naveed - Date - 13-10-2021

    private String getClobString(Clob clob) throws SQLException,
            IOException {
        BufferedReader stringReader = new BufferedReader(
                clob.getCharacterStream());
        String singleLine = null;
        StringBuffer strBuff = new StringBuffer();
        while ((singleLine = stringReader.readLine()) != null) {
            strBuff.append(singleLine);
        }
        return strBuff.toString();
    }
    // Ended By Naveed - Date - 04-11-2021


    // Added By Naveed - Date - 31-12-2021
    // Animal picture report
    public byte[] getAnimalPictureReport(long loanAppSeq, String userId) throws IOException {

        String getBrnchByLoanApp = "SELECT BRNCH.BRNCH_SEQ FROM MW_LOAN_APP MLA\n" +
                "    JOIN MW_PORT PORT ON PORT.PORT_SEQ = MLA.PORT_SEQ AND PORT.CRNT_REC_FLG = MLA.CRNT_REC_FLG\n" +
                "    JOIN MW_BRNCH BRNCH ON BRNCH.BRNCH_SEQ = PORT.BRNCH_SEQ AND BRNCH.CRNT_REC_FLG = PORT.CRNT_REC_FLG\n" +
                "    WHERE MLA.CRNT_REC_FLG = 1 AND MLA.LOAN_APP_SEQ = :P_LOAN_APP_SEQ";

        List<Object> brnch = em.createNativeQuery(getBrnchByLoanApp).setParameter("P_LOAN_APP_SEQ", loanAppSeq).getResultList();
        long brnchSeq = 0l;

        if (brnch.size() > 0 && brnch != null) {
            brnchSeq = brnch.get(0) == null ? 0 : new BigDecimal(brnch.get(0).toString()).longValue();
        }

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", userId);

        String picQry;
        picQry = readFile(Charset.defaultCharset(), "animalPictureScript.txt");
        Query rs4 = em.createNativeQuery(picQry).setParameter("P_LOAN_APP_SEQ", loanAppSeq);

        Clob clob = null;
        Clob clob2 = null;
        try {
            List<Object[]> picsObj = rs4.getResultList();
            if (picsObj != null && picsObj.size() > 0) {
                params.put("clnt_id", picsObj.get(0)[0] == null ? "" : picsObj.get(0)[0].toString());
                params.put("clnt_nm", picsObj.get(0)[1] == null ? "" : picsObj.get(0)[1].toString());
                params.put("disbmt_dt", picsObj.get(0)[2] == null ? "" : picsObj.get(0)[2].toString());
                params.put("disbmt_amt", picsObj.get(0)[3] == null ? "" : picsObj.get(0)[3].toString());
                params.put("animal_tag_num", picsObj.get(0)[4] == null ? "" : picsObj.get(0)[4].toString());
                params.put("tag_dt", picsObj.get(0)[5] == null ? "" : picsObj.get(0)[5].toString());

                clob = picsObj.get(0)[6] == null ? null : (Clob) picsObj.get(0)[6];
                clob2 = picsObj.get(0)[7] == null ? null : (Clob) picsObj.get(0)[7];

                params.put("animal_img_1", getStringClob(clob2));
                params.put("animal_img_2", getStringClob(clob));

                return reportComponent.generateReport(ANIMAL_PICTURE, params, null);
            } else {
                return new byte[0];
            }
        } catch (
                Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new byte[0];
    }
    // Ended By Naveed - Date - 31-12-2021


    // Added By Naveed - Date - 23-01-2022
    // SCR- Munsalik Integration
    // Get All Online Collection Channel modes
    public List<Map<String, String>> getIntegratedChannelCompanyList() {

        List<Object[]> resultList = em.createNativeQuery(readFile(Charset.defaultCharset(), "IntegrationChannelCompany.txt"))
                .getResultList();

        List<Map<String, String>> mapList = new ArrayList();

        resultList.forEach(l -> {
            Map<String, String> map = new HashMap();
            map.put("channel_seq", l[0] == null ? "" : l[0].toString());
            map.put("channel_nm", l[1] == null ? "" : l[1].toString());
            mapList.add(map);
        });
        return mapList;
    }
    // Ended By Naveed - Date - 23-01-2022

    // Added by Areeba - Date - 23-01-2022
    // SCR - Mobile Wallet Control
    public byte[] getMobWalLoansReport(String userId, String toDt, String frmDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);

        String mobwalloans;
        mobwalloans = readFile(Charset.defaultCharset(), "MobWalLoans.txt");
        Query rs = em.createNativeQuery(mobwalloans).setParameter("to_dt", toDt).setParameter("from_dt", frmDt);

        List<Object[]> mobwalloanslists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        mobwalloanslists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_ID", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_NM", l[4] == null ? "" : l[4].toString());
            map.put("MOB_WAL_NO", l[5] == null ? "" : l[5].toString());
            map.put("MOB_WAL_CRTD_DT", l[6]);
            map.put("DSBMT_DT", l[7]);
            map.put("MOB_WAL_CHNL_TYP", l[8] == null ? "" : l[8].toString());
            map.put("DSBMT_MODE", l[9] == null ? "" : l[9].toString());
            map.put("DSBMT_AMT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("LOAN_APP_STS", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(MOB_WAL_LOANS, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(MOB_WAL_LOANS, params, getJRDataSource(reportParams));
        }
    }
    // Ended by Areeba

    // Added by Areeba - Date - 23-01-2022
    // SCR - Portfolio transfer
    public byte[] getTransferClientsDetailsReport(String user, String frmDt, String toDt, long brnch, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", user);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        //params.put("brnch", brnch);

        String transferclientsdetails;
        transferclientsdetails = readFile(Charset.defaultCharset(), "TransferClientsDetails.txt");
        Query rs = em.createNativeQuery(transferclientsdetails).setParameter("to_dt", toDt).setParameter("from_dt", frmDt).setParameter("brnch", brnch);

        List<Object[]> transferclientsdetailslists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        transferclientsdetailslists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("CLNT_SEQ", l[0] == null ? "" : l[0].toString());
            map.put("PRD_NM", l[1] == null ? "" : l[1].toString());
            map.put("LOAN_APP_SEQ", l[2] == null ? "" : l[2].toString());
            map.put("CLNT_NM", l[3] == null ? "" : l[3].toString());
            map.put("DSBMT_DT", l[4]);
            map.put("TRF_BY", l[5] == null ? "" : l[5].toString());
            map.put("TRF_DT", l[6]);
            map.put("BRNCH_NM", l[7] == null ? "" : l[7].toString());
            map.put("PPAL", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("SC", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("KSZB", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("KST", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("KC", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("DOCUMENTATION_FEE", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("LIFE_INSURANCE_PREMIUM", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("TAKAFUL_CONRTIBUTION", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("TRAINING_CHARGES", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("INSURANCE_PREMIUM_LIVE_STOCK", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("EXCESS_RCVRY", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("INS_CLAIMS", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("BRNCH_STS", l[20] == null ? "" : l[20].toString());

            reportParams.add(map);

            params.put("Dataset1", getJRDataSource(reportParams));
        });
        if (isxls) {
            return reportComponent.generateReport(TRANSFER_CLIENTS_DETAILS, params, null, isxls);
        } else {
            return reportComponent.generateReport(TRANSFER_CLIENTS_DETAILS, params, null);
        }
    }
    // Ended by Areeba
    // Ended by Areeba

    // Added by Areeba - Date - 25-1-2022
    // SCR - Portfolio transfer
    // Top Sheet Transfer Clients Report
    public byte[] getTopSheetTransferClientsReport(String user, String frmDt, String toDt, long brnch, boolean isxls) throws IOException {


        Map<String, Object> params = new HashMap<>();
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnch);
        Object[] obj = (Object[]) bi.getSingleResult();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("curr_user", user);
        params.put("frmdt", frmDt);
        params.put("todt", toDt);

        //params.put("brnch", brnch);

        String topsheettransferclients;
        topsheettransferclients = readFile(Charset.defaultCharset(), "TopSheetTransferClients.txt");
        Query rs = em.createNativeQuery(topsheettransferclients).setParameter("todt", toDt).setParameter("frmdt", frmDt).setParameter("brnch", brnch);

        List<Object[]> topsheettransferclientslists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        topsheettransferclientslists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BDO", l[0] == null ? "" : l[0].toString());
            map.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            map.put("PFIN_CLNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("PFIN_AMT_PR", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("PFIN_AMT_SC", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("KSZB_IN", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("KST_IN", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("KC_IN", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("DOCUMENTATION_FEE_IN", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("LIFE_INSURANCE_PREMIUM_IN", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("TAKAFUL_CONRTIBUTION_IN", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("TRAINING_CHARGES_IN", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("INSURANCE_PREMIUM_LIVE_STOCK_IN", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("EXCESS_RCVRY_IN", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("INS_CLAIMS_IN", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("PFOUT_CLNT", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("PFOUT_AMT_PR", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("PFOUT_AMT_SC", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("KSZB_OUT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("KST_OUT", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("KC_OUT", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
            map.put("DOCUMENTATION_FEE_OUT", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("LIFE_INSURANCE_PREMIUM_OUT", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("TAKAFUL_CONRTIBUTION_OUT", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());
            map.put("TRAINING_CHARGES_OUT", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("INSURANCE_PREMIUM_LIVE_STOCK_OUT", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("EXCESS_RCVRY_OUT", l[26] == null ? 0 : new BigDecimal(l[26].toString()).longValue());
            map.put("INS_CLAIMS_OUT", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());

            reportParams.add(map);

            params.put("Dataset1", getJRDataSource(reportParams));
        });
        if (isxls) {
            return reportComponent.generateReport(TOP_SHEET_TRANSFER_CLIENTS, params, null, isxls);
        } else {
            return reportComponent.generateReport(TOP_SHEET_TRANSFER_CLIENTS, params, null);
        }
    }
    // Ended by Areeba

    // Added by Naveed - Date - 27-01-2022
    // SCR - RM Reports
    // Regional Risk Flagging Report
    // Modified BY Naveed - Date - 10-02-2022
    // Data Rectification
    public byte[] getRegionalRiskFlaggingReport(String frmDt, String toDt, long regSeq, long areaSeq, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        params.put("area_cd", "-1");

        if (regSeq != -1 && areaSeq == -1) {
            Query bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            if (obj != null && obj.length > 0) {
                params.put("reg_seq", obj[0].toString());
                params.put("reg_nm", obj[1].toString());
            }
        } else if (areaSeq != -1) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("area_cd", obj[2].toString());
        }

        String loanJumpBrnchScript;
        loanJumpBrnchScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingLoanJumpBrnch.txt");
        Query loanJumpBrnchQuery = em.createNativeQuery(loanJumpBrnchScript).setParameter("todt", toDt).setParameter("fromdt", frmDt)
                .setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);
        List<Object[]> loanJumpBrnchResult = loanJumpBrnchQuery.getResultList();

        List<Map<String, ?>> loanJumpBrnchList = new ArrayList();
        loanJumpBrnchResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("BRANCH", l[2] == null ? "" : l[2].toString());
            parm.put("TOTAL_CLNTS_DISB", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("REDUCED", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("NO_JUMP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            parm.put("5K-20K", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("21K-35K", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            parm.put("36K-50K", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("51K-65K", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            parm.put("66K-90K", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("91K-105K", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            parm.put("Above 105K", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());

            loanJumpBrnchList.add(parm);
        });
        params.put("LOAN_JUMP_BRANCH", getJRDataSource(loanJumpBrnchList));

        String loanJumpPrdScript;
        loanJumpPrdScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingLoanJumpPrd.txt");
        Query loanJumpPrdQuery = em.createNativeQuery(loanJumpPrdScript).setParameter("todt", toDt).setParameter("fromdt", frmDt)
                .setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);

        List<Object[]> loanJumpPrdResult = loanJumpPrdQuery.getResultList();

        List<Map<String, ?>> loanJumpPrdList = new ArrayList();
        loanJumpPrdResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("PRODUCT", l[2] == null ? "" : l[2].toString());
            parm.put("TOTAL_CLNTS_DISB", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("REDUCED", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("NO_JUMP", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            parm.put("5K-20K", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("21K-35K", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            parm.put("36K-50K", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("51K-65K", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            parm.put("66K-90K", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("91K-105K", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            parm.put("Above 105K", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());

            loanJumpPrdList.add(parm);
        });
        params.put("LOAN_JUMP_PRD", getJRDataSource(loanJumpPrdList));

        String loanAbove18KScript;
        loanAbove18KScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingLoanLoanAbove18K.txt");
        Query loanAbove18KQuery = em.createNativeQuery(loanAbove18KScript).setParameter("todt", toDt).setParameter("fromdt", frmDt)
                .setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);

        List<Object[]> loanAbove18KResult = loanAbove18KQuery.getResultList();

        List<Map<String, ?>> loanAbove18KList = new ArrayList();
        loanAbove18KResult.forEach(l -> {

            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("BRANCH", l[2] == null ? "" : l[2].toString());
            parm.put("TOTAL_CLNTS_DISB", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("80K-100K", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("80K-100K_%", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());
            parm.put("101K-120K", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("101K-120K_%", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            parm.put("121K-140K", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("121K-140K_%", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());
            parm.put("141K-150K", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("141K-150K_%", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            parm.put("151K-200K", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("151K-200K_%", l[13] == null ? 0 : new BigDecimal(l[13].toString()).doubleValue());
            parm.put("Above 200K", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            parm.put("Above_200K_%", l[15] == null ? 0 : new BigDecimal(l[15].toString()).doubleValue());
            parm.put("80K-100K_CLNT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            parm.put("101K-120K_CLNT", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            parm.put("121K-140K_CLNT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            parm.put("141K-150K_CLNT", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            parm.put("151K-200K_CLNT", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
            parm.put("Above 200K_CLNT", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());

            loanAbove18KList.add(parm);
        });
        params.put("LOANS_ABOVE_80K", getJRDataSource(loanAbove18KList));


        String otherMFPsBrnchScript;
        otherMFPsBrnchScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingOtherMFPsBrnch.txt");
        Query otherMFPsBrnchQuery = em.createNativeQuery(otherMFPsBrnchScript).setParameter("todt", toDt).setParameter("fromdt", frmDt)
                .setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);

        List<Object[]> otherMFPsBrnchResult = otherMFPsBrnchQuery.getResultList();

        List<Map<String, ?>> otherMFPsBrnchList = new ArrayList();
        otherMFPsBrnchResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("BRANCH", l[2] == null ? "" : l[2].toString());
            parm.put("TOT_CLNTS_DISB", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("ZERO_LOANS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("ZERO_LOANS_%", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());
            parm.put("1_LOANS", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("1_LOANS_%", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            parm.put("2_LOANS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("2_LOANS_%", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());
            parm.put("3_LOANS_AND_ABOVE", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("3_LOANS_AND_ABOVE_%", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());

            otherMFPsBrnchList.add(parm);
        });
        params.put("OTHER_MFP_BRNCH", getJRDataSource(otherMFPsBrnchList));

        String otherMFPsPrdScript;
        otherMFPsPrdScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingOtherMFPsPrd.txt");
        Query otherMFPsPrdQuery = em.createNativeQuery(otherMFPsPrdScript).setParameter("todt", toDt).setParameter("fromdt", frmDt)
                .setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);

        List<Object[]> otherMFPsPrdResult = otherMFPsPrdQuery.getResultList();

        List<Map<String, ?>> otherMFPsPrdList = new ArrayList();
        otherMFPsPrdResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("PRODUCT", l[2] == null ? "" : l[2].toString());
            parm.put("TOT_CLNTS_DISB", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("ZERO_LOANS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("ZERO_LOANS_%", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());
            parm.put("1_LOANS", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("1_LOANS_%", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            parm.put("2_LOANS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("2_LOANS_%", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());
            parm.put("3_LOANS_AND_ABOVE", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("3_LOANS_AND_ABOVE_%", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());

            otherMFPsPrdList.add(parm);
        });
        params.put("OTHER_MFP_PRD", getJRDataSource(otherMFPsPrdList));

        String earlyMaturityBrnchScript;
        earlyMaturityBrnchScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingEarlyMaturityBrnch.txt");
        Query earlyMaturityBrnchQuery = em.createNativeQuery(earlyMaturityBrnchScript).setParameter("todt", toDt).setParameter("fromdt", frmDt)
                .setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);

        List<Object[]> earlyMaturityBrnchResult = earlyMaturityBrnchQuery.getResultList();

        List<Map<String, ?>> earlyMaturityBrnchList = new ArrayList();
        earlyMaturityBrnchResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("BRANCH", l[2] == null ? "" : l[2].toString());
            parm.put("TOTAL_CLNTS_DISB", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("TOTAL_CLNTS_MATURED_EARLY", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("total_clnts_matured_early_%", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());
            parm.put("1-2 installments", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("1-2 installments_%", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            parm.put("3-4 installments", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("3-4 installments_%", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());
            parm.put("5-6 installments", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("5-6 installments_%", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            parm.put("More than 6 installments", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("More than 6 installments_%", l[13] == null ? 0 : new BigDecimal(l[13].toString()).doubleValue());

            earlyMaturityBrnchList.add(parm);
        });
        params.put("EARLY_MATURITY_BRNCH", getJRDataSource(earlyMaturityBrnchList));

        String earlyMaturityPrdScript;
        earlyMaturityPrdScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingEarlyMaturityPrd.txt");
        Query earlyMaturityPrdQuery = em.createNativeQuery(earlyMaturityPrdScript).setParameter("todt", toDt).setParameter("fromdt", frmDt)
                .setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);

        List<Object[]> earlyMaturityPrdResult = earlyMaturityPrdQuery.getResultList();

        List<Map<String, ?>> earlyMaturityPrdList = new ArrayList();
        earlyMaturityPrdResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("PRODUCT", l[2] == null ? "" : l[2].toString());
            parm.put("TOTAL_CLNTS_DISB", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("TOTAL_CLNTS_MATURED_EARLY", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("total_clnts_matured_early_%", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());
            parm.put("1-2 installments", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("1-2 installments_%", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            parm.put("3-4 installments", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("3-4 installments_%", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());
            parm.put("5-6 installments", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("5-6 installments_%", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
            parm.put("More than 6 installments", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("More than 6 installments_%", l[13] == null ? 0 : new BigDecimal(l[13].toString()).doubleValue());

            earlyMaturityPrdList.add(parm);
        });
        params.put("EARLY_MATURITY_PRD", getJRDataSource(earlyMaturityPrdList));

        String rentedClientRatioScript;
        rentedClientRatioScript = readFile(Charset.defaultCharset(), "regionalRiskFlaggingRentedClientsRatio.txt")
                .replaceAll(":REG_SEQ", String.valueOf(regSeq)).replaceAll(":AREA_SEQ", String.valueOf(areaSeq));

        Query rentedClientRatioQuery = em.createNativeQuery(rentedClientRatioScript).setParameter("todt", toDt).setParameter("fromdt", frmDt);
        //.setParameter("REG_SEQ", regSeq).setParameter("AREA_SEQ", areaSeq);

        List<Object[]> rentedClientRatioResult = rentedClientRatioQuery.getResultList();

        List<Map<String, ?>> rentedClientRatioList = new ArrayList();
        rentedClientRatioResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA", l[1] == null ? "" : l[1].toString());
            parm.put("TOTAL_CLNTS_DISB", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            parm.put("RENTED", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("RENTED_%", l[4] == null ? 0 : new BigDecimal(l[4].toString()).doubleValue());

            rentedClientRatioList.add(parm);
        });
        params.put("RENTED_RATIO", getJRDataSource(rentedClientRatioList));

        return reportComponent.generateReport(RISK_FLAGGING_REGION_REPORT, params, null);
    }
    // Ended by Naveed - Date - 27-01-2022

    // Added by Naveed - Date 27-01-2022
    // SCR - RM Reports
    // Regional Disbursement Report
    // Modified BY Naveed - Date - 10-02-2022
    // Data Rectification
    public byte[] getRegionalDisbursementReport(String frmDt, String toDt, long regSeq, long areaSeq, String userId) throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        params.put("area_cd", "-1");

        if (regSeq != -1 && areaSeq == -1) {
            Query bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            if (obj != null && obj.length > 0) {
                params.put("reg_seq", obj[0].toString());
                params.put("reg_nm", obj[1].toString());
            }
        } else if (areaSeq != -1) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("area_cd", obj[2].toString());
        }

        long totalPortfolio = 0;

        String disbursementPrdScript;
        disbursementPrdScript = readFile(Charset.defaultCharset(), "regionalDisbursementPrd.txt")
                .replaceAll(":REG_SEQ", String.valueOf(regSeq)).replaceAll(":AREA_SEQ", String.valueOf(areaSeq));
        Query disbursementPrdQuery = em.createNativeQuery(disbursementPrdScript).setParameter("todt", toDt).setParameter("frmdt", frmDt);

        List<Object[]> disbursementPrdResult = disbursementPrdQuery.getResultList();

        List<Map<String, ?>> disbursementPrdList = new ArrayList();
        disbursementPrdResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("PRODUCT", l[1] == null ? "" : l[1].toString());
            parm.put("ACTIVE_CLNTS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            parm.put("OLP", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("TOT_DSB_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("PORTFOLIO_COUNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            parm.put("BDO_CASELOAD", l[6] == null ? 0 : new BigDecimal(l[6].toString()).doubleValue());
            parm.put("ADS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).doubleValue());
            parm.put("TRGT_CLIENTS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("ACHVD_CLNTS", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            parm.put("TRGT_AMT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("ACHVD_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            parm.put("TOT_LOAN_SIZE", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("NEW_CLNT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            parm.put("RPT_CLNT", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            parm.put("NEW_CLNT_AMT", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            parm.put("RPT_CLNT_AMT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());

            disbursementPrdList.add(parm);
        });
        params.put("DISB_PRD", getJRDataSource(disbursementPrdList));

        String disbursementAreaScriptActive;
        disbursementAreaScriptActive = readFile(Charset.defaultCharset(), "regionalDisbursementAreaActive.txt")
                .replaceAll(":REG_SEQ", String.valueOf(regSeq)).replaceAll(":AREA_SEQ", String.valueOf(areaSeq));
        Query disbursementAreaActiveQuery = em.createNativeQuery(disbursementAreaScriptActive).setParameter("todt", toDt);
        //.setParameter("reg_seq", regSeq);

        String disbursementAreaDsbScript;
        disbursementAreaDsbScript = readFile(Charset.defaultCharset(), "regionalDisbursementAreaDsb.txt")
                .replaceAll(":REG_SEQ", String.valueOf(regSeq)).replaceAll(":AREA_SEQ", String.valueOf(areaSeq));
        Query disbursementAreaDsbQuery = em.createNativeQuery(disbursementAreaDsbScript).setParameter("todt", toDt).setParameter("frmdt", frmDt);
//				.setParameter("reg_seq", regSeq);

        String disbursementAreaTargetScript;
        disbursementAreaTargetScript = readFile(Charset.defaultCharset(), "regionalDisbursementAreaTarget.txt")
                .replaceAll(":REG_SEQ", String.valueOf(regSeq)).replaceAll(":AREA_SEQ", String.valueOf(areaSeq));
        Query disbursementAreaTargetQuery = em.createNativeQuery(disbursementAreaTargetScript).setParameter("todt", toDt).setParameter("frmdt", frmDt);
//				.setParameter("reg_seq", regSeq);

        List<Object[]> disbursementAreaActiveResult = disbursementAreaActiveQuery.getResultList();
        List<Object[]> disbursementAreaDsbResult = disbursementAreaDsbQuery.getResultList();
        List<Object[]> disbursementAreaTargetResult = disbursementAreaTargetQuery.getResultList();

        List<Map<String, Object>> disbursementAreaList = new ArrayList();
        disbursementAreaActiveResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", l[0] == null ? "" : l[0].toString());
            parm.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            parm.put("ACTIVE_CLNTS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            parm.put("OLP", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("TOT_DSB_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("PORTFOLIO_COUNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            //	parm.put("BDO_CASELOAD", l[6] == null ? 0 : new BigDecimal(l[6].toString()).doubleValue());
            //	parm.put("ADS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).doubleValue());

            Object[] active = disbursementAreaDsbResult.stream().filter(atv -> l[6].toString().equals(atv[8].toString())).findAny().orElse(null);
            if (active != null) {
                parm.put("NEW_CLNT", active[2] == null ? 0 : new BigDecimal(active[2].toString()).longValue());
                parm.put("RPT_CLNT", active[3] == null ? 0 : new BigDecimal(active[3].toString()).longValue());
                parm.put("NEW_CLNT_AMT", active[4] == null ? 0 : new BigDecimal(active[4].toString()).longValue());
                parm.put("RPT_CLNT_AMT", active[5] == null ? 0 : new BigDecimal(active[5].toString()).longValue());
                parm.put("NEW_CLNT_1", active[6] == null ? 0 : new BigDecimal(active[6].toString()).longValue());
                parm.put("RPT_CLNT_1", active[7] == null ? 0 : new BigDecimal(active[7].toString()).longValue());
            } else {
                parm.put("NEW_CLNT", 0L);
                parm.put("RPT_CLNT", 0L);
                parm.put("NEW_CLNT_AMT", 0L);
                parm.put("RPT_CLNT_AMT", 0L);
                parm.put("NEW_CLNT_1", 0L);
                parm.put("RPT_CLNT_1", 0L);
            }

            Object[] target = disbursementAreaTargetResult.stream().filter(tar -> l[6].toString().equals(tar[4].toString())).findAny().orElse(null);
            if (target != null) {
                parm.put("TRGT_CLIENTS", target[2] == null ? 0 : new BigDecimal(target[2].toString()).longValue());
                parm.put("TRGT_AMT", target[3] == null ? 0 : new BigDecimal(target[3].toString()).longValue());
            } else {
                parm.put("TRGT_CLIENTS", 0L);
                parm.put("TRGT_AMT", 0L);
            }
            disbursementAreaList.add(parm);
        });

        for (Map<String, Object> map : disbursementAreaList) {
            if (map.containsKey("PORTFOLIO_COUNT")) {
                totalPortfolio += Long.parseLong(map.get("PORTFOLIO_COUNT").toString());
            }
        }
        params.put("TOTAL_PORT", totalPortfolio);
        params.put("DISB_AREA", getJRDataSource(disbursementAreaList));

        return reportComponent.generateReport(REGIONAL_DISBURSEMENT, params, null);
    }
    // Ended by Naveed - Date - 27-01-2022


    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // Mobile Disbursement Trend Report
    public byte[] getMobileWalletTrend(String frmDt, String toDt, long brnchSeq, long type, boolean isxls, String userId) throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);
        params.put("curr_user", userId);

        String trendQury;
        trendQury = readFile(Charset.defaultCharset(), "mobileWalletTrendScript.txt");

        Query rs = em.createNativeQuery(trendQury).setParameter("todt", toDt)
                .setParameter("frmDt", frmDt).setParameter("brnchSeq", brnchSeq);

        List<Object[]> trendObject = rs.getResultList();
        List<Map<String, ?>> trendsList = new ArrayList();
        trendObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REGION", w[0] == null ? "" : w[0].toString());
            parm.put("AREA", w[1] == null ? "" : w[1].toString());
            parm.put("BRANCH", w[2] == null ? "" : w[2].toString());
            parm.put("BANK", w[3] == null ? 0 : new BigDecimal(w[3].toString()).longValue());
            parm.put("CASH", w[4] == null ? 0 : new BigDecimal(w[4].toString()).longValue());
            parm.put("MOBILE_WALLET", w[5] == null ? 0 : new BigDecimal(w[5].toString()).longValue());
            parm.put("MCB", w[6] == null ? 0 : new BigDecimal(w[6].toString()).longValue());

            trendsList.add(parm);
        });

        if (type == 0) {
            params.put("WALLET_TREND", getJRDataSource(trendsList));
        } else if (type == 1) {
            params.put("WALLET_TREND_SUMRY", getJRDataSource(trendsList));
        }
        if (isxls) {
            return reportComponent.generateReport(MOBILE_WALLET_TREND_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(MOBILE_WALLET_TREND, params, getJRDataSource(trendsList));
        }
    } // End By naveed

    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // ATM Cards Management Report
    public byte[] getATMCard(String frmDt, String toDt, long brnchSeq, String userId, boolean isxls) throws IOException {
        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();
        Map<String, Object> params = new HashMap<>();
        params.put("reg_nm", obj[0].toString());
        params.put("area_nm", obj[1].toString());
        params.put("brnch_nm", obj[2].toString());
        params.put("brnch_cd", obj[3].toString());
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);
        params.put("curr_user", userId);

        String trendQury;
        trendQury = readFile(Charset.defaultCharset(), "ATMCardsScript.txt");

        Query rs = em.createNativeQuery(trendQury).setParameter("frmDt", frmDt).setParameter("toDt", toDt).setParameter("brnchSeq", brnchSeq);

        List<Object[]> trendObject = rs.getResultList();
        List<Map<String, ?>> trendsList = new ArrayList();
        trendObject.forEach(w -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("BRANCH", w[0] == null ? "" : w[0].toString());
            parm.put("EMP_NM", w[1] == null ? "" : w[1].toString());
            parm.put("CLIENT_ID", w[2] == null ? "" : w[2].toString());
            parm.put("CLIENT_NM", w[3] == null ? "" : w[3].toString());
            parm.put("CNIC", w[4] == null ? "" : w[4].toString());
            parm.put("MOB_NUM", w[5] == null ? "" : w[5].toString());
            parm.put("ATM_CARD", w[6] == null ? "" : w[6].toString());
            parm.put("RECEIVING_DT", w[7] == null ? "" : w[7].toString());
            parm.put("DELIVERED_DT", w[8] == null ? "" : w[8].toString());
            parm.put("CLIENT_ADDR", w[9] == null ? "" : w[9].toString());

            trendsList.add(parm);
        });
        params.put("AMT_CARDS", getJRDataSource(trendsList));
        if (isxls) {
            return reportComponent.generateReport(ATM_CARDS_MANAGEMENT_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(ATM_CARDS_MANAGEMENT, params, null);
        }
    } // End By Naveed

    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // UPaisa Mobile Wallet Report
    public byte[] getUpaisaDuesReport(String user, long type, boolean isxls) {

        byte[] reportData = null;
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);

        String query;
        if (type == 0L) {
            query = readFile(Charset.defaultCharset(), "UPaisaDuesUploader.txt");
        } else {
            query = readFile(Charset.defaultCharset(), "UPaisaDuesMIS.txt");
        }
        Query set = em.createNativeQuery(query);

        List<String> Objs = set.getResultList();

        List<Map<String, ?>> lists = new ArrayList();
        Objs.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("DATA", l.toString());
            lists.add(map);
        });

        if (isxls) {
            reportData = reportComponent.generateReport(UPaisaDues_EXCEL, params, getJRDataSource(lists), isxls);
        } else {
            reportData = reportComponent.generateReport(UPaisaDues, params, getJRDataSource(lists));
        }

        lists.clear();
        Objs.clear();
        System.gc();

        return reportData;
    } // Ended By Naveed

    // Added by Areeba - Date - 15-02-2022
    // Accounts Reports
    //Premium Data Report
    public byte[] getPremiumDataReport(String user, String toDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);
        params.put("toDt", toDt);

        String premiumdata;
        premiumdata = readFile(Charset.defaultCharset(), "PremiumData.txt");
        Query rs = em.createNativeQuery(premiumdata).setParameter("toDt", toDt);

        List<Object[]> premiumdatalists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        premiumdatalists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("PRD_CMNT", l[3] == null ? "" : l[3].toString());
            map.put("CUSTOMER_ID", l[4] == null ? "" : l[4].toString());
            map.put("CUSTOMER_NAME", l[5] == null ? "" : l[5].toString());
            map.put("DATE_OB_BIRTH", l[6]);
            map.put("CLIENT_CNIC_NO", l[7] == null ? "" : l[7].toString());
            map.put("NOMINEE_NAME", l[8] == null ? "" : l[8].toString());
            map.put("NOMINEE_CNIC_NO", l[9] == null ? "" : l[9].toString());
            map.put("DISBURSED_DATE", l[10]);
            map.put("DISB_AMOUNT_PR", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("OUTS_AMOUNT_SC", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OUTS_AMOUNT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("OVERDUE_DAYS", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("PREMIUM_AMT", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("NO_OF_INSTALLMENTS", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("ANIMAL_PRODUCT_TYPE", l[17] == null ? "" : l[17].toString());
            map.put("ANIMAL_BREED", l[18] == null ? "" : l[18].toString());
            map.put("PROCESS_DATE", l[19]);
            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(PREMIUM_DATA, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(PREMIUM_DATA, params, getJRDataSource(reportParams));
        }
    }

    //Premium Data KM Report
    public byte[] getPremiumDataKMReport(String user, String toDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);
        params.put("toDt", toDt);

        String premiumdatakm;
        premiumdatakm = readFile(Charset.defaultCharset(), "PremiumDataKM.txt");
        Query rs = em.createNativeQuery(premiumdatakm).setParameter("toDt", toDt);

        List<Object[]> premiumdatakmlists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        premiumdatakmlists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("PRD_CMNT", l[3] == null ? "" : l[3].toString());
            map.put("CUSTOMER_ID", l[4] == null ? "" : l[4].toString());
            map.put("CUSTOMER_NAME", l[5] == null ? "" : l[5].toString());
            map.put("DATE_OB_BIRTH", l[6]);
            map.put("CLIENT_CNIC_NO", l[7] == null ? "" : l[7].toString());
            map.put("NOMINEE_NAME", l[8] == null ? "" : l[8].toString());
            map.put("NOMINEE_CNIC_NO", l[9] == null ? "" : l[9].toString());
            map.put("NOMINEE_DOB", l[10]);
            map.put("DISBURSED_DATE", l[11]);
            map.put("AGENCY_DATE", l[12]);
            map.put("AGENCY_AMOUNT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("DISB_AMOUNT_PR", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("OUTS_AMOUNT_SC", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OUTS_AMOUNT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("OVERDUE_DAYS", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("PREMIUM_AMT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("TAKAFUL", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("PROCESS_DATE", l[20]);
            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(PREMIUM_DATA_KM, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(PREMIUM_DATA_KM, params, getJRDataSource(reportParams));
        }
    }

    //Write off Client Data Report
    public byte[] getWOClientDataReport(String user, String toDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);
        params.put("toDt", toDt);

        String woclientdata;
        woclientdata = readFile(Charset.defaultCharset(), "WOClientData.txt");
        Query rs = em.createNativeQuery(woclientdata).setParameter("toDt", toDt);

        List<Object[]> woclientdatalists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        woclientdatalists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("BDO_NAME", l[3] == null ? "" : l[3].toString());
            map.put("CENTER_ID", l[4] == null ? "" : l[4].toString());
            map.put("PRODUCT_ID", l[5] == null ? "" : l[5].toString());
            map.put("CUSTOMER_ID", l[6] == null ? "" : l[6].toString());
            map.put("CUSTOMER_NAME", l[7] == null ? "" : l[7].toString());
            map.put("CYCLE_NO", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("DISB_DATE", l[9]);
            map.put("DISB_PR", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("DISB_SC", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("OUTSTANDING_PRINCIPAL", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OUTSTANDING_SC", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("OVERDUE_PR", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("OVERDUE_AMOUNT_SC", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OVERDUE_DAYS", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("WRITE_OFF_DATE", l[17]);
            map.put("SECTOR_DESC", l[18] == null ? "" : l[18].toString());
            map.put("ACTIVITY", l[19] == null ? "" : l[19].toString());
            map.put("MATURITY_DATE", l[20] == null ? "" : l[20].toString());
            map.put("Total Installments", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("RESCHEDULE_DATE", l[22]);
            map.put("Remaining Installments", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());
            map.put("2ND CYCLE CLIENT", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("OS_PR", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("OS_SC", l[26] == null ? 0 : new BigDecimal(l[26].toString()).longValue());
            map.put("CURR_ODDAYS", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("SECOND_CYCLE_PR", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
            map.put("SECOND_CYCLE_SC", l[29] == null ? 0 : new BigDecimal(l[29].toString()).longValue());
            map.put("DONOR", l[30] == null ? "" : l[30].toString());
            map.put("LOAN_CODE", l[31] == null ? "" : l[31].toString());
            map.put("CNIC", l[32] == null ? "" : l[32].toString());
            map.put("TEHSIL", l[33] == null ? "" : l[33].toString());
            map.put("DISTRICT", l[34] == null ? "" : l[34].toString());
            map.put("UNION_COUNCEL", l[35] == null ? "" : l[35].toString());
            map.put("GENDER", l[36] == null ? "" : l[36].toString());
            map.put("BRANCH_TYPE", l[37] == null ? "" : l[37].toString());
            map.put("PROCESS_DATE", l[38]);

            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(WO_CLIENT_DATA, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(WO_CLIENT_DATA, params, getJRDataSource(reportParams));
        }
    }

    //Write off Recovery Report
    public byte[] getWORecoveryReport(String user, String toDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);
        params.put("toDt", toDt);

        String worecovery;
        worecovery = readFile(Charset.defaultCharset(), "WORecovery.txt");
        Query rs = em.createNativeQuery(worecovery).setParameter("toDt", toDt);

        List<Object[]> worecoverylists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        worecoverylists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("LOAN_APP_SEQ", l[1] == null ? "" : l[1].toString());
            map.put("PRD_SEQ", l[2] == null ? "" : l[2].toString());
            map.put("PRD_GRP_NM", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_SEQ", l[4] == null ? "" : l[4].toString());
            map.put("TAG_FROM_DT", l[5]);
            map.put("RCVRD_PRN_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("RCVRD_SC_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("PYMT_DT", l[8]);
            map.put("PROCESS_DATE", l[9]);

            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(WO_RECOVERY, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(WO_RECOVERY, params, getJRDataSource(reportParams));
        }
    }

    //Recovery Report
    public byte[] getAccRecoveryReport(String user, String toDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);
        params.put("toDt", toDt);

        String accrecovery;
        accrecovery = readFile(Charset.defaultCharset(), "Recovery.txt");
        Query rs = em.createNativeQuery(accrecovery).setParameter("toDt", toDt);

        List<Object[]> accrecoverylists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        accrecoverylists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("PRD_GRP_NM", l[3] == null ? "" : l[3].toString());
            map.put("CLNT_SEQ", l[4] == null ? "" : l[4].toString());
            map.put("DSBMT_DT", l[5]);
            map.put("LOAN_APP_SEQ", l[6] == null ? "" : l[6].toString());
            map.put("PAID_DT", l[7]);
            map.put("TOT_REC", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("PR_REC", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("SC_REC", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("REC_TYP", l[11] == null ? "" : l[11].toString());
            map.put("INSTR_NUM", l[12] == null ? "" : l[12].toString());
            map.put("DUE_DT", l[13]);
            map.put("PR_DUE", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("SC_DUE", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("INST_NUM", l[16] == null ? "" : l[16].toString());
            map.put("RSCH_LOANS", l[17] == null ? "" : l[17].toString());
            map.put("RSCH_DT", l[18]);
            map.put("DATA_EXT_DT", l[19]);
            //map.put("PROCESS_DATE", l[20]);

            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(ACC_RECOVERY, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(ACC_RECOVERY, params, getJRDataSource(reportParams));
        }
    }

    //Client Data Report
    public byte[] getClientDataReport(String user, String toDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);
        params.put("toDt", toDt);

        String clientdata;
        clientdata = readFile(Charset.defaultCharset(), "ClientData.txt");
        Query rs = em.createNativeQuery(clientdata).setParameter("toDt", toDt);

        List<Object[]> clientdatalists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        clientdatalists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("Region", l[0] == null ? "" : l[0].toString());
            map.put("Area", l[1] == null ? "" : l[1].toString());
            map.put("Branch", l[2] == null ? "" : l[2].toString());
            map.put("BDO_NAME", l[3] == null ? "" : l[3].toString());
            map.put("CENTER_ID", l[4] == null ? "" : l[4].toString());
            map.put("PRODUCT_ID", l[5] == null ? "" : l[5].toString());
            map.put("CUSTOMER_ID", l[6] == null ? "" : l[6].toString());
            map.put("CUSTOMER_NAME", l[7] == null ? "" : l[7].toString());
            map.put("CYCLE_NO", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("DISB_DATE", l[9] == null ? "" : l[9].toString());
            map.put("DISB_PR", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("DISB_SC", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("OUTSTANDING_PRINCIPAL", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("OUTSTANDING_SC", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("OVERDUE_PR", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("OVERDUE_AMOUNT_SC", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("OVERDUE_DAYS", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("WRITE_OFF_DATE", l[17]);
            map.put("SECTOR_DESC", l[18] == null ? "" : l[18].toString());
            map.put("ACTIVITY", l[19] == null ? "" : l[19].toString());
            map.put("MATURITY_DATE", l[20] == null ? "" : l[20].toString());
            map.put("Total Installments", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("RESCHEDULE_DATE", l[22]);
            map.put("Remaining Installments", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());
            map.put("2ND CYCLE CLIENT", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
            map.put("OS_PR", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
            map.put("OS_SC", l[26] == null ? 0 : new BigDecimal(l[26].toString()).longValue());
            map.put("CURR_ODDAYS", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("SECOND_CYCLE_PR", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
            map.put("SECOND_CYCLE_SC", l[29] == null ? 0 : new BigDecimal(l[29].toString()).longValue());
            map.put("DONOR", l[30] == null ? "" : l[30].toString());
            map.put("LOAN_CODE", l[31] == null ? "" : l[31].toString());
            map.put("CNIC", l[32] == null ? "" : l[32].toString());
            map.put("TEHSIL", l[33] == null ? "" : l[33].toString());
            map.put("DISTRICT", l[34] == null ? "" : l[34].toString());
            map.put("UNION_COUNCEL", l[35] == null ? "" : l[35].toString());
            map.put("GENDER", l[36] == null ? "" : l[36].toString());
            map.put("BRANCH_TYPE", l[37] == null ? "" : l[37].toString());
            map.put("NOMINEE_NAME", l[38] == null ? "" : l[38].toString());
            map.put("NOMINEE_CNIC", l[39] == null ? "" : l[39].toString());
            map.put("PROCESS_DATE", l[40]);
            map.put("BRNCH_SEQ", l[41] == null ? "" : l[41].toString());
            map.put("CLIENT_DOB", l[42]);
            map.put("NOMINEE_DOB", l[43]);

            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(CLIENT_DATA, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(CLIENT_DATA, params, getJRDataSource(reportParams));
        }
    }

    //KSZB Client Data Report
    public byte[] getKSZBClientDataReport(String user, String toDt, boolean isxls) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", user);
        params.put("toDt", toDt);

        String kszbclientdata;
        kszbclientdata = readFile(Charset.defaultCharset(), "KSZBClientData.txt");
        Query rs = em.createNativeQuery(kszbclientdata).setParameter("toDt", toDt);

        List<Object[]> kszbclientdatalists = rs.getResultList();
        List<Map<String, ?>> reportParams = new ArrayList();
        kszbclientdatalists.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_SEQ", l[0] == null ? "" : l[0].toString());
            map.put("BRNCH_NM", l[1] == null ? "" : l[1].toString());
            map.put("VOUCHER_DATE", l[2]);
            map.put("CLIENT_PARTY_ID", l[3] == null ? "" : l[3].toString());
            map.put("LOAN_CODE", l[4] == null ? "" : l[4].toString());
            map.put("DSBMT_DT", l[5]);
            map.put("CARD_NUM", l[6] == null ? "" : l[6].toString());
            map.put("VOUCHER_TYPE", l[7] == null ? "" : l[7].toString());
            map.put("VOUCHER_CODE", l[8] == null ? "" : l[8].toString());
            map.put("ENTY_SEQ", l[9] == null ? "" : l[9].toString());
            map.put("PRNT_VCHR_REF", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("KBK_REC", l[11] == null ? "" : l[11].toString());
            map.put("JV_DSCR", l[12] == null ? "" : l[12].toString());
            map.put("DEBIT", l[13] == null ? "" : l[13].toString());
            map.put("CREDIT", l[14] == null ? "" : l[14].toString());
            map.put("TRNS_FLAG", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("INSTRUMENT_NO", l[16] == null ? "" : l[16].toString());
            map.put("RESCHEDULE_TYPE", l[17] == null ? "" : l[17].toString());
            map.put("RESCHEDULE_PREMIUM", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("MATURITY_DATE", l[19]);
            map.put("PRODUCT_CODE", l[20] == null ? "" : l[20].toString());
            map.put("PRODUCT_NAME", l[21] == null ? "" : l[21].toString());
            map.put("PROCESS_DATE", l[22]);

            reportParams.add(map);
        });
        if (isxls) {
            return reportComponent.generateReport(KSZB_CLIENT_DATA, params, getJRDataSource(reportParams), isxls);
        } else {
            return reportComponent.generateReport(KSZB_CLIENT_DATA, params, getJRDataSource(reportParams));
        }
    }

    // Added by Naveed - Date 07-03-2022
    // SCR - RM Reports
    // Regional Recovery Trend Report
    public byte[] getRegionalRecoveryTrendReport(String frmDt, String toDt, long regSeq, long areaSeq, String userId) throws IOException {

        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        params.put("area_cd", "-1");

        if (regSeq != -1 && areaSeq == -1) {
            Query bi = em.createNativeQuery(Queries.REGION_INFO_BY_REGION_SEQ).setParameter("regseq", regSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            if (obj != null && obj.length > 0) {
                params.put("reg_seq", obj[0].toString());
                params.put("reg_nm", obj[1].toString());
            }
        } else if (areaSeq != -1) {
            Query bi = em.createNativeQuery(Queries.AREA_INFO_BY_AREA_SEQ).setParameter("areaSeq",
                    areaSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("area_cd", obj[2].toString());
        }

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String recoveryTrendAreaScript;
        recoveryTrendAreaScript = readFile(Charset.defaultCharset(), "regionalRecoveryTrendArea.txt")
                .replaceAll(":REG_SEQ", String.valueOf(regSeq)).replaceAll(":AREA_SEQ", String.valueOf(areaSeq));
        Query recoveryTrendAreaQuery = em.createNativeQuery(recoveryTrendAreaScript).setParameter("todt", toDt).setParameter("frmdt", frmDt);
        List<Object[]> recoveryTrendAreaResult = recoveryTrendAreaQuery.getResultList();
        List<Map<String, ?>> recoveryTrendAreaList = new ArrayList();

        recoveryTrendAreaResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REG_NM", l[0] == null ? "" : l[0].toString());
            parm.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            parm.put("TOT_DUE", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            parm.put("TOT_RCV", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("REC_TYP_CASH", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("REC_TYP_CASH_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            parm.put("REC_TYP_OTH", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("REC_TYP_OTH_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            parm.put("OD_1_30_DYS", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("OD_ABOVE_30_DYS", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            parm.put("SAME_DY", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("ADC_1_3_DY", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            parm.put("ADC_4_7_DY", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            parm.put("ADC_8_15_DY", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            parm.put("ADC_16_30_DY", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            parm.put("ADC_ABOVE_30_DY", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            parm.put("REC_TYP_TOT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            parm.put("REC_TYP_TOT_AMT", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());

            recoveryTrendAreaList.add(parm);
        });
        params.put("RCVRY_AREA", getJRDataSource(recoveryTrendAreaList));
        params.put("RCVRY_AREA_1", getJRDataSource(recoveryTrendAreaList));

        String recoveryTrendPrdScript;
        recoveryTrendPrdScript = readFile(Charset.defaultCharset(), "regionalRecoveryTrendPrd.txt")
                .replaceAll(":REG_SEQ", String.valueOf(regSeq)).replaceAll(":AREA_SEQ", String.valueOf(areaSeq));
        Query recoveryTrendPrdQuery = em.createNativeQuery(recoveryTrendPrdScript).setParameter("todt", toDt).setParameter("frmdt", frmDt);
        List<Object[]> recoveryTrendPrdResult = recoveryTrendPrdQuery.getResultList();

        List<Map<String, ?>> recoveryTrendPrdList = new ArrayList();

        recoveryTrendPrdResult.forEach(l -> {
            Map<String, Object> parm = new HashMap<>();
            parm.put("REG_NM", l[0] == null ? "" : l[0].toString());
            parm.put("PRD_GRP_NM", l[1] == null ? "" : l[1].toString());
            parm.put("TOT_DUE", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            parm.put("TOT_RCV", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            parm.put("REC_TYP_CASH", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            parm.put("REC_TYP_CASH_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            parm.put("REC_TYP_MOB_WAL_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            parm.put("REC_TYP_OTH", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            parm.put("REC_TYP_OTH_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            parm.put("OD_1_30_DYS", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            parm.put("OD_ABOVE_30_DYS", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            parm.put("SAME_DY", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            parm.put("ADC_1_3_DY", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            parm.put("ADC_4_7_DY", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            parm.put("ADC_8_15_DY", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            parm.put("ADC_16_30_DY", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            parm.put("ADC_ABOVE_30_DY", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            parm.put("REC_TYP_TOT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            parm.put("REC_TYP_TOT_AMT", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());

            recoveryTrendPrdList.add(parm);
        });
        params.put("RCVRY_PRD", getJRDataSource(recoveryTrendPrdList));
        params.put("RCVRY_PRD_1", getJRDataSource(recoveryTrendPrdList));

        return reportComponent.generateReport(REGIONAL_RECOVERY_TREND, params, null);
    }

    // Modified by Areeba - 2-11-2022
    public byte[] getAccountsMonthlyReport(String toDate, String fileName, long type) {
        byte[] bytes = null;
        List<MwStpCnfigVal> path = mwStpCnfigValRepository.findAllByStpGrpCdAndCrntRecFlgOrderBySortOrderAsc("0007", true);
        String csvFile = path.get(0).getRefCdValDscr() + fileName + ".csv";

        try {
            InputStream inputStream;
            if (checkFileExist(csvFile)) {
                logger.info("File Found " + fileName);
                inputStream = new FileInputStream(csvFile);
                bytes = IOUtils.toByteArray(inputStream);
                inputStream.close();
//				bytes = Files.readAllBytes(Paths.get(csvFile));
            } else {
                String paramOutputProcedure = "";
                if (type == 11 || type == 12) {
                    String dateScript = "select TO_DATE (TRUNC (TO_DATE (TRUNC (to_date(:toDt), 'Month')), 'Month')) from dual";
                    Object dateQry = em.createNativeQuery(dateScript).setParameter("toDt", toDate).getSingleResult();
                    String fromDt = "";
                    try {
                        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yyyy");
                        fromDt = sdf1.format(dateQry);
                    } catch (Exception ex) {
                        logger.error(ex.toString());
                    }

                    StoredProcedureQuery storedProcedure;
                    if (type == 11)
                        storedProcedure = em.createStoredProcedureQuery("MAIL.PKG_ACCOUNT_REPORTS.POP_CLIENT_DUES@MAILDB.KASHF.ORG.PK");
                    else
                        storedProcedure = em.createStoredProcedureQuery("MAIL.PKG_ACCOUNT_REPORTS.POP_CLIENT_RECOVERIES@MAILDB.KASHF.ORG.PK");

                    storedProcedure.registerStoredProcedureParameter("P_FROM_DATE", String.class, ParameterMode.IN);
                    storedProcedure.registerStoredProcedureParameter("P_TO_DATE", String.class, ParameterMode.IN);
                    storedProcedure.registerStoredProcedureParameter("P_ERROR_MSG", String.class, ParameterMode.OUT);
                    storedProcedure.setParameter("P_FROM_DATE", fromDt);
                    storedProcedure.setParameter("P_TO_DATE", toDate);
                    storedProcedure.execute();
                    paramOutputProcedure = storedProcedure.getOutputParameterValue("P_ERROR_MSG").toString();
                    logger.info("P_ERROR_MSG : " + paramOutputProcedure);
                } else {
                    StoredProcedureQuery storedProcedure = em.createStoredProcedureQuery("CSV_FILE@MAILDB.KASHF.ORG.PK");
                    storedProcedure.registerStoredProcedureParameter("p_to_dt", String.class, ParameterMode.IN);
                    storedProcedure.registerStoredProcedureParameter("rpt_number", Long.class, ParameterMode.IN);
                    storedProcedure.registerStoredProcedureParameter("p_msg", String.class, ParameterMode.OUT);
                    storedProcedure.setParameter("p_to_dt", toDate);
                    storedProcedure.setParameter("rpt_number", type);
                    storedProcedure.execute();
                    paramOutputProcedure = storedProcedure.getOutputParameterValue("p_msg").toString();
                    logger.info("p_msg : " + paramOutputProcedure);
                }
                if (paramOutputProcedure.equals("sucess") || paramOutputProcedure.equals("success") || paramOutputProcedure.equals("Completed")) {
//					bytes = Files.readAllBytes(Paths.get(csvFile));
                    inputStream = new FileInputStream(csvFile);
                    bytes = IOUtils.toByteArray(inputStream);
                    inputStream.close();
                } else {
                    bytes = new byte[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            bytes = new byte[0];
        } finally {
            System.gc();
        }
        return bytes;
    }
    // Ended by Areeba

    public boolean checkFileExist(String path) {
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    // Added By Naveed - Date 28-03-2022
    // SCR One Pager For Both (TASDEEQ and DATACHECK)
    public byte[] getDataCheck(Clob clob) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonObject = "";
        Map<String, Map<String, Object>> map = new HashMap<>();

        try {
            jsonObject = clob == null ? null : clob.getSubString(1, (int) clob.length());
            map = objectMapper.readValue(jsonObject, Map.class);
        } catch (Exception exp) {
            throw new Exception(exp);
        }

        Map<String, Object> root = new HashMap<>();
        if (map.containsKey("report")) {
            root = (Map<String, Object>) map.get("report").get("ROOT");
        } else if (map.containsKey("ROOT")) {
            root = (Map<String, Object>) map.get("ROOT");
        } else {
            return new byte[0];
        }

        MfcibCommonDto mfcibCommonDto = new MfcibCommonDto();
        personalInfoDetailDATACHEK(mfcibCommonDto, root);
        creditScore(mfcibCommonDto, root);

        summaryOfInformation(mfcibCommonDto, root);
        summaryOfOpenLoans(mfcibCommonDto, root);

        if (root.containsKey("INDIVIDUAL_DETAIL")) {
            getCreditFileDetailDATACHECK(mfcibCommonDto, (Map<String, Object>) root.get("INDIVIDUAL_DETAIL"));
        }
        return getOnePageReport(mfcibCommonDto);
    }

    public byte[] getTASDEEQ(Clob clob) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonObject = "";
        Map<String, Map<String, Object>> map = new HashMap<>();

        try {
            jsonObject = clob == null ? null : clob.getSubString(1, (int) clob.length());
            map = objectMapper.readValue(jsonObject, Map.class);
        } catch (Exception exp) {
            throw new Exception(exp);
        }

        Map<String, Object> root = new HashMap<>();
        MfcibCommonDto mfcibCommonDto = new MfcibCommonDto();

        if (map.containsKey("statusCode") && map.containsKey("messageCode")) {
            Object statusCode = map.get("statusCode");
            Object messageCode = map.get("messageCode");
            if ((statusCode.toString().equals("111")) && (messageCode.toString().equals("00922001"))) {
                root = (Map<String, Object>) map.get("data");
                if (root.containsKey("refNo")) {
                    mfcibCommonDto.reportRef = root.get("refNo").toString();
                }
                if (root.containsKey("reportDate")) {
                    mfcibCommonDto.reportGenDate = root.get("reportDate").toString();
                }
                mfcibCommonDto.creditBureau = "TD";
                mfcibCommonDto.creditBureauType = "Type A";
            }
        } else {
            return new byte[0];
        }

        if (root.containsKey("loanDetails")) {
            List<Map<String, Object>> loanDetails = (List<Map<String, Object>>) root.get("loanDetails");
            loanDetails.forEach(loan -> {
                if (loan.containsKey("LOAN_ACCOUNT_STATUS")) {
                    if (loan.get("LOAN_ACCOUNT_STATUS").toString().equalsIgnoreCase("open")) {
                        mfcibCommonDto.openLoansSummary.add(summaryOfOpenLoansTasdeeq(loan));
                    } else {
                        if (loan.containsKey("OUTSTANDING_BALANCE")) {
                            if (!(loan.get("OUTSTANDING_BALANCE").toString().isEmpty() || loan.get("OUTSTANDING_BALANCE").toString().equals("*"))) {
                                if (new BigDecimal(Double.parseDouble(loan.get("OUTSTANDING_BALANCE").toString())).longValue() > 0) {
                                    mfcibCommonDto.openLoansSummary.add(summaryOfOpenLoansTasdeeq(loan));
                                }
                            }
                        }
                    }
                } else if (loan.containsKey("OUTSTANDING_BALANCE")) {
                    if (!(loan.get("OUTSTANDING_BALANCE").toString().isEmpty() || loan.get("OUTSTANDING_BALANCE").toString().equals("*"))) {
                        if (new BigDecimal(Double.parseDouble(loan.get("OUTSTANDING_BALANCE").toString())).longValue() > 0) {
                            mfcibCommonDto.openLoansSummary.add(summaryOfOpenLoansTasdeeq(loan));
                        }
                    }
                }
            });
        }
        SummaryInfoDto summaryInfoDto = new SummaryInfoDto();
        if (root.containsKey("noOfCreditEnquiry")) {
            if (!root.get("noOfCreditEnquiry").toString().equals("*")) {
                BigDecimal bigDecimal = new BigDecimal(Double.parseDouble(root.get("noOfCreditEnquiry").toString()));
                summaryInfoDto.totalCreditInquires = String.valueOf(bigDecimal.longValue());
            }
        }
        if (root.containsKey("noOfActiveAccounts")) {
            if (!root.get("noOfActiveAccounts").toString().equals("*")) {
                BigDecimal bigDecimal = new BigDecimal(Double.parseDouble(root.get("noOfActiveAccounts").toString()));
                summaryInfoDto.noOfActiveLoans = String.valueOf(bigDecimal.longValue());
            }
        }
        if (root.containsKey("totalOutstandingBalance")) {
            if (!root.get("totalOutstandingBalance").toString().equals("*")) {
                BigDecimal bigDecimal = new BigDecimal(Double.parseDouble(root.get("totalOutstandingBalance").toString()));
                summaryInfoDto.activeLoansOuts = String.valueOf(bigDecimal.longValue());
            }
        }

        int nanoLoans = 0;
        int woNo = 0;
        long woAmt = 0;
        for (OpenLoansSummaryDto loan : mfcibCommonDto.openLoansSummary) {
            try {
                if (!(loan.approvedLimit.isEmpty() || loan.approvedLimit.equals("*"))) {
                    nanoLoans += Long.parseLong(loan.approvedLimit) <= 10000 ? 1 : 0;
                }
                if (!(loan.woAmount.isEmpty() || loan.woAmount.equals("*"))) {
                    if (Long.parseLong(loan.woAmount) > 0) {
                        woAmt += Long.parseLong(loan.woAmount);
                        woNo += 1;
                    }
                }
            } catch (NumberFormatException exception) {
                exception.printStackTrace();
            }
        }
        summaryInfoDto.noOfWoAmount = String.valueOf(woAmt);
        summaryInfoDto.noOfWoLoans = String.valueOf(woNo);
        summaryInfoDto.noOfNanoLoans = String.valueOf(nanoLoans);
        mfcibCommonDto.summaryInfo.add(summaryInfoDto);


        personalInfoDetailTasdeeq(mfcibCommonDto, root);
//		summaryOfInformationTasdeeqPrimaryInfo(mfcibCommonDto, root);
//		summaryOfInformationTasdeeqAdditinalInfo(mfcibCommonDto, root);

        return getOnePageReport(mfcibCommonDto);
    }

    public byte[] getOnePageReport(MfcibCommonDto mfcibCommonDto) throws Exception {

        Map<String, Object> params = new HashMap<>();
        String additionalCnic = "";

        List<Map<String, ?>> personalInfoList = new ArrayList();
        for (PersonalInfoDto personalInfoDto : mfcibCommonDto.personalInfo) {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("NAME", personalInfoDto.name == null ? "" : personalInfoDto.name);
            hashMap.put("GENDER", personalInfoDto.gender == null ? "" : personalInfoDto.gender);
            hashMap.put("DOB", personalInfoDto.dob == null ? "" : personalInfoDto.dob);
            hashMap.put("FATHER_OR_HUSBAND_NAME", personalInfoDto.fatherOrHusbandName == null ? "" : personalInfoDto.fatherOrHusbandName);
            hashMap.put("CNIC", personalInfoDto.cnic == null ? "" : personalInfoDto.cnic);
            hashMap.put("MARTIAL_STATUS", personalInfoDto.martialStatus == null ? "" : personalInfoDto.martialStatus);
            hashMap.put("CURRENT_RESIDENTIAL_ADDRESS", personalInfoDto.currentResidentialAddress == null ? "" : personalInfoDto.currentResidentialAddress);
            hashMap.put("PERMANENT_ADDRESS", personalInfoDto.permanentAddress == null ? "" : personalInfoDto.permanentAddress);
            hashMap.put("EMPLOYER_OR_BUSINESS_ADDRESS", personalInfoDto.employerOrBusinessAddress == null ? "" : personalInfoDto.employerOrBusinessAddress);
            hashMap.put("PREVIOUS_RESIDENTIAL_ADDRESS", personalInfoDto.previousResidentialAddress == null ? "" : personalInfoDto.previousResidentialAddress);
            hashMap.put("NOMINEE_NM", personalInfoDto.nomineeName == null ? "" : personalInfoDto.nomineeName);
            hashMap.put("TYPE", "Client");
            if (personalInfoDto.nomineeName != null && !personalInfoDto.nomineeName.isEmpty()) {
                mfcibCommonDto.creditBureauType = "Type B";
                additionalCnic += personalInfoDto.nomineeName;
            }
            personalInfoList.add(hashMap);
        }

        List<Map<String, ?>> summaryInfo = new ArrayList();
        mfcibCommonDto.summaryInfo.forEach(info -> {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("TOT_CREDT", info.totalCreditInquires == null ? "" : info.totalCreditInquires);
            hashMap.put("ACTIVE_LOANS", info.noOfActiveLoans == null ? "" : info.noOfActiveLoans);
            hashMap.put("OUTS", info.activeLoansOuts == null ? "" : info.activeLoansOuts);
            hashMap.put("NANO_LOANS", info.noOfNanoLoans == null ? "" : info.noOfNanoLoans);
            hashMap.put("LOAN_ABOVE_10K", info.loanAbove10K == null ? "" : info.loanAbove10K);
            hashMap.put("OD_ABOVE_60", info.noOfCurrentAbove60Days == null ? "" : info.noOfCurrentAbove60Days);
            hashMap.put("NO_WO_LOANS", info.noOfWoLoans == null ? "" : info.noOfWoLoans);
            hashMap.put("AMT_WO_LOANS", info.noOfWoAmount == null ? "" : info.noOfWoAmount);
            hashMap.put("PAYMT_ABOVE_30", info.paymentLast24Month == null ? "" : info.paymentLast24Month);
            hashMap.put("COMPLT_IN_MATURITY", info.completedInMaturity == null ? "" : info.completedInMaturity);
            hashMap.put("COMPLT_AFTR_MATURITY", info.completedAfterMaturity == null ? "" : info.completedAfterMaturity);
            hashMap.put("TYPE", info.type == null ? "" : info.type);

            summaryInfo.add(hashMap);
        });

        List<Map<String, ?>> summaryOpenLoans = new ArrayList();
        mfcibCommonDto.openLoansSummary.forEach(info -> {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("LOAN", info.loan == null ? "" : info.loan);
            hashMap.put("MEM", info.mem == null ? "" : info.mem);
            hashMap.put("LOANS_STATUS", info.loansStatus == null ? "" : info.loansStatus);
            hashMap.put("APPROVED_LIMIT", info.approvedLimit == null ? "" : info.approvedLimit);
            hashMap.put("CURRENT_OS", info.currentOs == null ? "" : info.currentOs);
            hashMap.put("AMOUNT_OD", info.amountOd == null ? "" : info.amountOd);
            hashMap.put("ONTIME", info.onTime == null ? "" : info.onTime);
            hashMap.put("BTW_15_29", info.btw15To29 == null ? "" : info.btw15To29);
            hashMap.put("PLUS_30", info.plus30 == null ? "" : info.plus30);
            hashMap.put("PLUS_60", info.plus60 == null ? "" : info.plus60);
            hashMap.put("PLUS_90", info.plus90 == null ? "" : info.plus90);
            hashMap.put("PLUS_120", info.plus120 == null ? "" : info.plus120);
            hashMap.put("PLUS_150", info.plus150 == null ? "" : info.plus150);
            hashMap.put("PLUS_180", info.plus180 == null ? "" : info.plus180);
            hashMap.put("LOSS", info.loss == null ? "" : info.loss);
            hashMap.put("LOAN_ID", info.loanId == null ? "" : info.loanId);
            hashMap.put("SECURE_UNSECURE", info.secureUnsecure == null ? "" : info.secureUnsecure);
            hashMap.put("GROUP_ID", info.groupId == null ? "" : info.groupId);
            hashMap.put("REPAYMENT_FREQ", info.repaymentFrequency == null ? "" : info.repaymentFrequency);
            hashMap.put("INSTALLMENT_AMOUNT", info.installmentAmount == null ? "" : info.installmentAmount);
            hashMap.put("DISB_DATE", info.disbursementDate == null ? "" : info.disbursementDate);
            hashMap.put("SECURITY_COLLATERAL", info.securityCollateral == null ? "" : info.securityCollateral);
            hashMap.put("MATURITY_DATE", info.maturityDate == null ? "" : info.maturityDate);
            hashMap.put("WO_AMOUNT", info.woAmount == null ? "" : info.woAmount);
            hashMap.put("LAST_PAYMENT", info.lastPayment == null ? "" : info.lastPayment);
            hashMap.put("POSITION_OF", info.positionAsOf == null ? "" : info.positionAsOf);
            hashMap.put("RECH_DATE", info.rescheduleDate == null ? "" : info.rescheduleDate);
            hashMap.put("LAST_PAYMENT_DATE", info.lastPaymentDate == null ? "" : info.lastPaymentDate);
            hashMap.put("LOAN_TYPE", info.loanType == null ? "" : info.loanType);

            summaryOpenLoans.add(hashMap);
        });

        List<Map<String, ?>> creditScore = new ArrayList();
        mfcibCommonDto.creditScore.forEach(score -> {
            Map<String, String> hashMap = new HashMap<>();
            hashMap.put("SCORE", score.score == null ? "" : score.score);
            hashMap.put("ODDS", score.odds == null ? "" : score.odds);
            hashMap.put("PROB_OF_DEFALUT", score.probOfDefault == null ? "" : score.probOfDefault);
            hashMap.put("PERCENTILE_RISK", score.percentileRisk == null ? "" : score.percentileRisk);
            hashMap.put("SBP_RISK_LEVEL", score.sbpRiskLevel == null ? "" : score.sbpRiskLevel);
            creditScore.add(hashMap);
        });

        List<Map<String, ?>> additionalCnicList = new ArrayList();
        if (!additionalCnic.isEmpty()) {
            String array[] = additionalCnic.split(";");
            for (String s : array) {
                Map<String, String> hashMap = new HashMap<>();
                hashMap.put("NOMINEE_NM", s);
                additionalCnicList.add(hashMap);
            }
        }

//		params.put("curr_user", user);
        params.put("FILE_REF", mfcibCommonDto.reportRef);
        params.put("FILE_GEN_DT", mfcibCommonDto.reportGenDate);
        params.put("CREDIT_BUREAU", mfcibCommonDto.creditBureau);
        params.put("CREDIT_BUREAU_TYPE", mfcibCommonDto.creditBureauType);
        params.put("PERSONAL_INFO", getJRDataSource(personalInfoList));
        params.put("SUMMMARY_INFO", getJRDataSource(summaryInfo));
        params.put("OPEN_LOANS", getJRDataSource(summaryOpenLoans));
        params.put("CREDIT_DETAILS", getJRDataSource(summaryOpenLoans));
        params.put("ADDITIONAL_CNIC", getJRDataSource(additionalCnicList));
        params.put("CREDIT_SCORE", getJRDataSource(creditScore));

        return reportComponent.generateReport(ONEPAGER_MFCIB, params, null);
    }

    private void getCreditFileDetailDATACHECK(MfcibCommonDto mfcibCommonDto, Map<String, Object> individualDetail) {
        if (individualDetail.containsKey("FILE_NO")) {
            if (!individualDetail.get("FILE_NO").toString().isEmpty()) {
                mfcibCommonDto.reportRef = individualDetail.get("TRANX_NO").toString();
                mfcibCommonDto.reportGenDate = individualDetail.get("TRANX_DATE").toString();
                mfcibCommonDto.creditBureau = "DC";
                mfcibCommonDto.creditBureauType = "Type A";
            }
        }
    }

    private void personalInfoDetailDATACHEK(MfcibCommonDto mfcibCommonDto, Map<String, Object> root) {
        PersonalInfoDto dto = new PersonalInfoDto();
        if (root.containsKey("INDIVIDUAL_DETAIL")) {
            if (isList(root.get("INDIVIDUAL_DETAIL"))) {
                personalInfo((List<Map<String, Object>>) root.get("INDIVIDUAL_DETAIL"), dto);
            } else {
                personalInfoDatacheck((Map<String, Object>) root.get("INDIVIDUAL_DETAIL"), dto);
            }
        }

        if (root.containsKey("HOME_INFORMATION")) {
            if (isList(root.get("HOME_INFORMATION"))) {
                homeInformation((List<Map<String, Object>>) root.get("HOME_INFORMATION"), dto);
            } else {
                homeInformation((Map<String, Object>) root.get("HOME_INFORMATION"), dto);
            }
        }
        if (root.containsKey("EMPLOYER_INFORMATION")) {
            if (isList(root.get("EMPLOYER_INFORMATION"))) {
                employeeInformation((List<Map<String, Object>>) root.get("EMPLOYER_INFORMATION"), dto);
            } else {
                employeeInformation((Map<String, Object>) root.get("EMPLOYER_INFORMATION"), dto);
            }
        }
        if (root.containsKey("CREDIT_SUMMARY")) {
            if (isList(root.get("CREDIT_SUMMARY"))) {
                additionalCnic((List<Map<String, Object>>) root.get("CREDIT_SUMMARY"), dto);
            } else {
                additionalCnic((Map<String, Object>) root.get("CREDIT_SUMMARY"), dto);
            }
        }
        mfcibCommonDto.personalInfo.add(dto);
    }

    private void personalInfoDetailTasdeeq(MfcibCommonDto mfcibCommonDto, Map<String, Object> root) {
        PersonalInfoDto dto = new PersonalInfoDto();
        if (root.containsKey("personalInformation")) {
            personalInfoTasdeeq((Map<String, Object>) root.get("personalInformation"), dto);
        }
        mfcibCommonDto.personalInfo.add(dto);
    }

    private void summaryOfInformationTasdeeqPrimaryInfo(MfcibCommonDto mfcibCommonDto, Map<String, Object> root) {
        if (root.containsKey("summaryPrimaryConsolidatedLoansExtended")) {
            if (isList(root.get("summaryPrimaryConsolidatedLoansExtended"))) {
                ((List<Map<String, Object>>) root.get("summaryPrimaryConsolidatedLoansExtended")).forEach(summary -> {
                    mfcibCommonDto.summaryInfo.add(tasdeeqPrimaryInfo(summary));
                });
            } else {
                mfcibCommonDto.summaryInfo.add(tasdeeqPrimaryInfo((Map<String, Object>) root.get("summaryPrimaryConsolidatedLoansExtended")));
            }
        }
    }

    private SummaryInfoDto tasdeeqPrimaryInfo(Map<String, Object> summary) {
        SummaryInfoDto dto = new SummaryInfoDto();

        if (summary.containsKey("primary_Updated_enquiries_2_Years_Count")) {
            if (!(summary.get("primary_Updated_enquiries_2_Years_Count").toString().isEmpty() || summary.get("primary_Updated_enquiries_2_Years_Count").toString().equals("*"))) {
                dto.totalCreditInquires = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Updated_enquiries_2_Years_Count").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Updated_Active_Accounts")) {
            if (!(summary.get("primary_Updated_Active_Accounts").toString().isEmpty() || summary.get("primary_Updated_Active_Accounts").toString().equals("*"))) {
                dto.noOfActiveLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Updated_Active_Accounts").toString())).longValue());
                dto.loanAbove10K = dto.noOfActiveLoans;
            }
        }
        if (summary.containsKey("primary_Updated_Active_Balance")) {
            if (!(summary.get("primary_Updated_Active_Balance").toString().isEmpty() || summary.get("primary_Updated_Active_Balance").toString().equals("*"))) {
                dto.activeLoansOuts = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Updated_Active_Balance").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Nano_Loan_Count")) {
            if (!(summary.get("primary_Nano_Loan_Count").toString().isEmpty() || summary.get("primary_Nano_Loan_Count").toString().equals("*"))) {
                dto.noOfNanoLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Nano_Loan_Count").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Count_Overdue_Loans_60")) {
            if (!(summary.get("primary_Count_Overdue_Loans_60").toString().isEmpty() || summary.get("primary_Count_Overdue_Loans_60").toString().equals("*"))) {
                dto.noOfCurrentAbove60Days = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Count_Overdue_Loans_60").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Count_Default_Loans")) {
            if (!(summary.get("primary_Count_Default_Loans").toString().isEmpty() || summary.get("primary_Count_Default_Loans").toString().equals("*"))) {
                dto.noOfWoLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Count_Default_Loans").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Amount_Default_Loans")) {
            if (!(summary.get("primary_Amount_Default_Loans").toString().isEmpty() || summary.get("primary_Amount_Default_Loans").toString().equals("*"))) {
                dto.noOfWoAmount = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Amount_Default_Loans").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Payments_PLUS_30")) {
            if (!(summary.get("primary_Payments_PLUS_30").toString().isEmpty() || summary.get("primary_Payments_PLUS_30").toString().equals("*"))) {
                dto.paymentLast24Month = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Payments_PLUS_30").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Count_Completed_Within_Maturity")) {
            if (!(summary.get("primary_Count_Completed_Within_Maturity").toString().isEmpty() || summary.get("primary_Count_Completed_Within_Maturity").toString().equals("*"))) {
                dto.completedInMaturity = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Count_Completed_Within_Maturity").toString())).longValue());
            }
        }
        if (summary.containsKey("primary_Count_Completed_After_Maturity")) {
            if (!(summary.get("primary_Count_Completed_After_Maturity").toString().isEmpty() || summary.get("primary_Count_Completed_After_Maturity").toString().equals("*"))) {
                dto.completedAfterMaturity = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("primary_Count_Completed_After_Maturity").toString())).longValue());
            }
        }
        dto.type = "Client";

        return dto;
    }

    private void summaryOfInformationTasdeeqAdditinalInfo(MfcibCommonDto mfcibCommonDto, Map<String, Object> root) {
        if (root.containsKey("additionalInformation")) {
            if (isList(root.get("additionalInformation"))) {
                ((List<Map<String, Object>>) root.get("additionalInformation")).forEach(summary -> {
                    SummaryInfoDto dto = new SummaryInfoDto();

                    if (summary.containsKey("nominee1_Updated_Enquiries_2_Years_Count")) {
                        if (!(summary.get("nominee1_Updated_Enquiries_2_Years_Count").toString().isEmpty() || summary.get("nominee1_Updated_Enquiries_2_Years_Count").toString().equals("*"))) {
                            dto.totalCreditInquires = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Updated_Enquiries_2_Years_Count").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Updated_Active_Accounts")) {
                        if (!(summary.get("nominee1_Updated_Active_Accounts").toString().isEmpty() || summary.get("nominee1_Updated_Active_Accounts").toString().equals("*"))) {
                            dto.noOfActiveLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Updated_Active_Accounts").toString())).longValue());
                            dto.loanAbove10K = dto.noOfActiveLoans;
                        }
                    }
                    if (summary.containsKey("nominee1_Updated_Active_Balance")) {
                        if (!(summary.get("nominee1_Updated_Active_Balance").toString().isEmpty() || summary.get("nominee1_Updated_Active_Balance").toString().equals("*"))) {
                            dto.activeLoansOuts = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Updated_Active_Balance").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Nano_Loan_Count")) {
                        if (!(summary.get("nominee1_Nano_Loan_Count").toString().isEmpty() || summary.get("nominee1_Nano_Loan_Count").toString().equals("*"))) {
                            dto.noOfNanoLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Nano_Loan_Count").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Count_Overdue_Loans_60")) {
                        if (!(summary.get("nominee1_Count_Overdue_Loans_60").toString().isEmpty() || summary.get("nominee1_Count_Overdue_Loans_60").toString().equals("*"))) {
                            dto.noOfCurrentAbove60Days = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Count_Overdue_Loans_60").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Count_Default_Loans")) {
                        if (!(summary.get("nominee1_Count_Default_Loans").toString().isEmpty() || summary.get("nominee1_Count_Default_Loans").toString().equals("*"))) {
                            dto.noOfWoLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Count_Default_Loans").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Amount_Default_Loans")) {
                        if (!(summary.get("nominee1_Amount_Default_Loans").toString().isEmpty() || summary.get("nominee1_Amount_Default_Loans").toString().equals("*"))) {
                            dto.noOfWoAmount = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Amount_Default_Loans").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Payments_PLUS_30")) {
                        if (!(summary.get("nominee1_Payments_PLUS_30").toString().isEmpty() || summary.get("nominee1_Payments_PLUS_30").toString().equals("*"))) {
                            dto.paymentLast24Month = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Payments_PLUS_30").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Count_Completed_Within_Maturity")) {
                        if (!(summary.get("nominee1_Count_Completed_Within_Maturity").toString().isEmpty() || summary.get("nominee1_Count_Completed_Within_Maturity").toString().equals("*"))) {
                            dto.completedInMaturity = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Count_Completed_Within_Maturity").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_Count_Completed_After_Maturity")) {
                        if (!(summary.get("nominee1_Count_Completed_After_Maturity").toString().isEmpty() || summary.get("nominee1_Count_Completed_After_Maturity").toString().equals("*"))) {
                            dto.completedAfterMaturity = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("nominee1_Count_Completed_After_Maturity").toString())).longValue());
                        }
                    }
                    if (summary.containsKey("nominee1_NAME")) {
                        String nominee = mfcibCommonDto.personalInfo.get(0).nomineeName;
                        if (nominee.isEmpty()) {
                            mfcibCommonDto.personalInfo.get(0).nomineeName = summary.get("nominee1_NAME").toString().concat(" ");
                        } else {
                            mfcibCommonDto.personalInfo.get(0).nomineeName = nominee.concat(";") + summary.get("nominee1_NAME").toString();
                        }
                    }
                    dto.type = "Nominee";
                    mfcibCommonDto.summaryInfo.add(dto);
                });
            } else {
                Map<String, Object> summary = (Map<String, Object>) root.get("additionalInformation");
                SummaryInfoDto dto = new SummaryInfoDto();
                if (summary.containsKey("Updated_Enquiries_2_Years_Count")) {
                    if (!(summary.get("Updated_Enquiries_2_Years_Count").toString().isEmpty() || summary.get("Updated_Enquiries_2_Years_Count").toString().equals("*"))) {
                        dto.totalCreditInquires = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Updated_Enquiries_2_Years_Count").toString())).longValue());
                    }
                }
                if (summary.containsKey("Updated_Active_Accounts")) {
                    if (!(summary.get("Updated_Active_Accounts").toString().isEmpty() || summary.get("Updated_Active_Accounts").toString().equals("*"))) {
                        dto.noOfActiveLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Updated_Active_Accounts").toString())).longValue());
                    }
                }
                if (summary.containsKey("Updated_Active_Balance")) {
                    if (!(summary.get("Updated_Active_Balance").toString().isEmpty() || summary.get("Updated_Active_Balance").toString().equals("*"))) {
                        dto.activeLoansOuts = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Updated_Active_Balance").toString())).longValue());
                    }
                }
                if (summary.containsKey("Nano_Loan_Count")) {
                    if (!(summary.get("Nano_Loan_Count").toString().isEmpty() || summary.get("Nano_Loan_Count").toString().equals("*"))) {
                        dto.noOfNanoLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Nano_Loan_Count").toString())).longValue());
                    }
                }
                if (summary.containsKey("Count_Overdue_Loans_60")) {
                    if (!(summary.get("Count_Overdue_Loans_60").toString().isEmpty() || summary.get("Count_Overdue_Loans_60").toString().equals("*"))) {
                        dto.noOfCurrentAbove60Days = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Count_Overdue_Loans_60").toString())).longValue());
                    }
                }
                if (summary.containsKey("Count_Default_Loans")) {
                    if (!(summary.get("Count_Default_Loans").toString().isEmpty() || summary.get("Count_Default_Loans").toString().equals("*"))) {
                        dto.noOfWoLoans = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Count_Default_Loans").toString())).longValue());
                    }
                }
                if (summary.containsKey("Amount_Default_Loans")) {
                    if (!(summary.get("Amount_Default_Loans").toString().isEmpty() || summary.get("Amount_Default_Loans").toString().equals("*"))) {
                        dto.noOfWoAmount = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Amount_Default_Loans").toString())).longValue());
                    }
                }
                if (summary.containsKey("Payments_PLUS_30")) {
                    if (!(summary.get("Payments_PLUS_30").toString().isEmpty() || summary.get("Payments_PLUS_30").toString().equals("*"))) {
                        dto.paymentLast24Month = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Payments_PLUS_30").toString())).longValue());
                    }
                }
                if (summary.containsKey("Count_Completed_Within_Maturity")) {
                    if (!(summary.get("Count_Completed_Within_Maturity").toString().isEmpty() || summary.get("Count_Completed_Within_Maturity").toString().equals("*"))) {
                        dto.completedInMaturity = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Count_Completed_Within_Maturity").toString())).longValue());
                    }
                }
                if (summary.containsKey("Count_Completed_After_Maturity")) {
                    if (!(summary.get("Count_Completed_After_Maturity").toString().isEmpty() || summary.get("Count_Completed_After_Maturity").toString().equals("*"))) {
                        dto.completedAfterMaturity = String.valueOf(new BigDecimal(Double.parseDouble(summary.get("Count_Completed_After_Maturity").toString())).longValue());
                    }
                }
                if (summary.containsKey("Name")) {
                    String nominee = mfcibCommonDto.personalInfo.get(0).nomineeName;
                    if (nominee.isEmpty()) {
                        mfcibCommonDto.personalInfo.get(0).nomineeName = summary.get("Name").toString().concat(" ");
                    } else {
                        mfcibCommonDto.personalInfo.get(0).nomineeName = nominee.concat(";") + summary.get("Name").toString();
                    }
                }
                dto.type = "Nominee";
                mfcibCommonDto.summaryInfo.add(dto);
            }
        }
    }

    private void summaryOfOpenLoans(MfcibCommonDto mfcibCommonDto, Map<String, Object> root) {
        if (root.containsKey("CCP_MASTER")) {
            if (isList(root.get("CCP_MASTER"))) {
                ((List<Map<String, Object>>) root.get("CCP_MASTER")).forEach(openLoan -> {
                    if (openLoan.containsKey("ACCT_STATUS")) {
                        if (openLoan.get("ACCT_STATUS").toString().equals("OPEN")) {
                            OpenLoansSummaryDto loans = openLoansSummary(openLoan);
                            if (loans != null) {
                                if (isList(root.get("CCP_SUMMARY"))) {
                                    ((List<Map<String, Object>>) root.get("CCP_SUMMARY")).forEach(history -> {
                                        if (history.containsKey("LOAN_NO")) {
                                            if (history.get("LOAN_NO").toString().equals(loans.loan)) {
                                                repaymentHistory(history, loans);
                                            }
                                        }
                                    });
                                } else {
                                    if (((Map<String, Object>) root.get("CCP_SUMMARY")).containsKey("LOAN_NO")) {
                                        if (((Map<String, Object>) root.get("CCP_SUMMARY")).get("LOAN_NO").equals(loans.loan)) {
                                            repaymentHistory((Map<String, Object>) root.get("CCP_SUMMARY"), loans);
                                        }
                                    }
                                }
                                mfcibCommonDto.openLoansSummary.add(loans);
                            }
                        }
                    }
                });
            } else {
                if (((Map<String, Object>) root.get("CCP_MASTER")).containsKey("ACCT_STATUS")) {
                    if (((Map<String, Object>) root.get("CCP_MASTER")).get("ACCT_STATUS").toString().equals("OPEN")) {
                        OpenLoansSummaryDto loans = openLoansSummary((Map<String, Object>) root.get("CCP_MASTER"));
                        if (loans != null) {
                            if (isList(root.get("CCP_SUMMARY"))) {
                                ((List<Map<String, Object>>) root.get("CCP_SUMMARY")).forEach(history -> {
                                    if (history.containsKey("LOAN_NO")) {
                                        if (history.get("LOAN_NO").toString().equals(loans.loan)) {
                                            repaymentHistory(history, loans);
                                        }
                                    }
                                });
                            } else {
                                if (((Map<String, Object>) root.get("CCP_SUMMARY")).containsKey("LOAN_NO")) {
                                    if (((Map<String, Object>) root.get("CCP_SUMMARY")).get("LOAN_NO").equals(loans.loan)) {
                                        repaymentHistory((Map<String, Object>) root.get("CCP_SUMMARY"), loans);
                                    }
                                }
                            }
                            mfcibCommonDto.openLoansSummary.add(loans);
                        }
                    }
                }
            }
        }

        if (root.containsKey("DEFAULTS")) {
            if (isList(root.get("DEFAULTS"))) {
                ((List<Map<String, Object>>) root.get("DEFAULTS")).forEach(def -> {
                    if (def.containsKey("UPD_STATUS") || def.containsKey("ORG_STATUS")) {
                        if (def.get("UPD_STATUS").toString().equalsIgnoreCase("C") || def.get("ORG_STATUS").toString().equalsIgnoreCase("C")) {
                            OpenLoansSummaryDto loans = openLoansSummaryDefault(def);
                            if (loans != null) {
                                if (isList(root.get("CCP_SUMMARY"))) {
                                    ((List<Map<String, Object>>) root.get("CCP_SUMMARY")).forEach(history -> {
                                        if (history.containsKey("LOAN_NO")) {
                                            if (history.get("LOAN_NO").toString().equals(loans.loan)) {
                                                repaymentHistory(history, loans);
                                            }
                                        }
                                    });
                                } else {
                                    if (((Map<String, Object>) root.get("CCP_SUMMARY")).containsKey("LOAN_NO")) {
                                        if (((Map<String, Object>) root.get("CCP_SUMMARY")).get("LOAN_NO").equals(loans.loan)) {
                                            repaymentHistory((Map<String, Object>) root.get("CCP_SUMMARY"), loans);
                                        }
                                    }
                                }
                                mfcibCommonDto.openLoansSummary.add(loans);
                            }
                        }
                    }
                });
            } else {
                if (((Map<String, Object>) root.get("DEFAULTS")).containsKey("UPD_STATUS")) {
                    if (((Map<String, Object>) root.get("DEFAULTS")).get("UPD_STATUS").toString().equalsIgnoreCase("C") ||
                            ((Map<String, Object>) root.get("DEFAULTS")).get("ORG_STATUS").toString().equalsIgnoreCase("C")) {
                        OpenLoansSummaryDto loans = openLoansSummaryDefault((Map<String, Object>) root.get("DEFAULTS"));
                        if (loans != null) {
                            if (isList(root.get("CCP_SUMMARY"))) {
                                ((List<Map<String, Object>>) root.get("CCP_SUMMARY")).forEach(history -> {
                                    if (history.containsKey("LOAN_NO")) {
                                        if (history.get("LOAN_NO").toString().equals(loans.loan)) {
                                            repaymentHistory(history, loans);
                                        }
                                    }
                                });
                            } else {
                                if (((Map<String, Object>) root.get("CCP_SUMMARY")).containsKey("LOAN_NO")) {
                                    if (((Map<String, Object>) root.get("CCP_SUMMARY")).get("LOAN_NO").equals(loans.loan)) {
                                        repaymentHistory((Map<String, Object>) root.get("CCP_SUMMARY"), loans);
                                    }
                                }
                            }
                            mfcibCommonDto.openLoansSummary.add(loans);
                        }
                    }
                }
            }
        }
    }

    private void personalInfo(List<Map<String, Object>> personalInfo, PersonalInfoDto dto) {
        personalInfoDatacheck(personalInfo.get(0), dto);
    }

    private void personalInfoDatacheck(Map<String, Object> personalInfo, PersonalInfoDto dto) {

        if (personalInfo.containsKey("FIRST_NAME")) {
            dto.name = personalInfo.get("FIRST_NAME").toString();
        }
        if (personalInfo.containsKey("LAST_NAME")) {
            dto.name = dto.name.concat(" ") + personalInfo.get("LAST_NAME").toString();
        }
        if (personalInfo.containsKey("GENDER")) {
            dto.gender = personalInfo.get("GENDER").toString();
        }
        if (personalInfo.containsKey("DOB")) {
            dto.dob = personalInfo.get("DOB").toString();
        }
        if (personalInfo.containsKey("FATHER_HUSBAND_FNAME")) {
            dto.fatherOrHusbandName = personalInfo.get("FATHER_HUSBAND_FNAME").toString();
        }
        if (personalInfo.containsKey("FATHER_HUSBAND_LNAME")) {
            dto.fatherOrHusbandName = dto.fatherOrHusbandName.concat(" ") + personalInfo.get("FATHER_HUSBAND_LNAME").toString();
        }
        if (personalInfo.containsKey("CNIC")) {
            dto.cnic = personalInfo.get("CNIC").toString();
        }
        if (personalInfo.containsKey("MARITIAL_STATUS")) {
            dto.martialStatus = personalInfo.get("MARITIAL_STATUS").toString();
        }
    }

    private void personalInfoTasdeeq(Map<String, Object> personalInfo, PersonalInfoDto dto) {

        if (personalInfo.containsKey("NAME")) {
            dto.name = personalInfo.get("NAME").toString();
        }
        if (personalInfo.containsKey("GENDER")) {
            dto.gender = personalInfo.get("GENDER").toString();
        }
        if (personalInfo.containsKey("DOB")) {
            dto.dob = personalInfo.get("DOB").toString();
        }
        if (personalInfo.containsKey("FATHER_OR_HUSBAND_NAME")) {
            dto.fatherOrHusbandName = personalInfo.get("FATHER_OR_HUSBAND_NAME").toString();
        }
        if (personalInfo.containsKey("CNIC")) {
            dto.cnic = personalInfo.get("CNIC").toString();
        }
        if (personalInfo.containsKey("CURRENT_RESIDENTIAL_ADDRESS")) {
            dto.currentResidentialAddress = personalInfo.get("CURRENT_RESIDENTIAL_ADDRESS").toString();
        }
        if (personalInfo.containsKey("PREVIOUS_RESIDENTIAL_ADDRESS")) {
            dto.previousResidentialAddress = personalInfo.get("PREVIOUS_RESIDENTIAL_ADDRESS").toString();
        }
        if (personalInfo.containsKey("EMPLOYER_OR_BUSINESS")) {
            dto.employerOrBusinessAddress = personalInfo.get("EMPLOYER_OR_BUSINESS").toString();
        }
    }

    private void homeInformation(List<Map<String, Object>> homeInformation, PersonalInfoDto dto) {
        homeInformation.forEach(home -> {
            homeInformation(home, dto);
        });
    }

    private void homeInformation(Map<String, Object> homeInformation, PersonalInfoDto dto) {
        if (homeInformation.get("SEQ_NO").toString().equals("Latest")) {
            if (homeInformation.containsKey("ADDRESS")) {
                dto.currentResidentialAddress = homeInformation.get("ADDRESS").toString();
            }
            if (homeInformation.containsKey("CITY")) {
                dto.currentResidentialAddress = dto.currentResidentialAddress.concat(", ") + homeInformation.get("CITY").toString();
            }
            if (homeInformation.containsKey("PERMANENT_ADDRESS")) {
                dto.permanentAddress = homeInformation.get("PERMANENT_ADDRESS").toString();
            }
            if (homeInformation.containsKey("PERMANENT_CITY")) {
                dto.permanentAddress = dto.permanentAddress.concat(", ") + homeInformation.get("PERMANENT_CITY").toString();
            }
        } else if (homeInformation.get("SEQ_NO").toString().equals("Previous")) {
            if (homeInformation.containsKey("ADDRESS")) {
                dto.previousResidentialAddress = homeInformation.get("ADDRESS").toString();
            }
            if (homeInformation.containsKey("CITY")) {
                dto.previousResidentialAddress = dto.previousResidentialAddress.concat(", ") + homeInformation.get("CITY").toString();
            }
        }
    }

    private void employeeInformation(List<Map<String, Object>> employeeInformation, PersonalInfoDto dto) {
        employeeInformation.forEach(employee -> {
            employeeInformation(employee, dto);
        });
    }

    private void employeeInformation(Map<String, Object> employeeInformation, PersonalInfoDto dto) {
        if (employeeInformation.get("SEQ_NO").toString().equals("Latest")) {
            if (employeeInformation.containsKey("ADDRESS")) {
                dto.employerOrBusinessAddress = employeeInformation.get("ADDRESS").toString();
            }
            if (employeeInformation.containsKey("CITY")) {
                dto.employerOrBusinessAddress = dto.employerOrBusinessAddress.concat(", ") + employeeInformation.get("CITY").toString();
            }
        }
    }

    private void additionalCnic(List<Map<String, Object>> additionalCnic, PersonalInfoDto dto) {
        additionalCnic.forEach(cnic -> {
            additionalCnic(cnic, dto);
        });
    }

    private void additionalCnic(Map<String, Object> additionalCnic, PersonalInfoDto dto) {
        if (additionalCnic.get("CATEGORY").toString().equals("Additional CNIC")) {
            if (additionalCnic.containsKey("NAME")) {
                if (dto.nomineeName.isEmpty()) {
                    dto.nomineeName = additionalCnic.get("NAME").toString().concat(" ");
                } else {
                    dto.nomineeName = dto.nomineeName.concat(";") + additionalCnic.get("NAME").toString();
                }
            }
        }
    }

    private void summaryOfInformation(MfcibCommonDto mfcibCommonDto, Map<String, Object> root) {
        if (root.containsKey("CREDIT_SUMMARY")) {
            if (isList(root.get("CREDIT_SUMMARY"))) {
                ((List<Map<String, Object>>) root.get("CREDIT_SUMMARY")).forEach(summary -> {
                    mfcibCommonDto.summaryInfo.add(summaryInfo(summary));
                });
            } else {
                mfcibCommonDto.summaryInfo.add(summaryInfo((Map<String, Object>) root.get("CREDIT_SUMMARY")));
            }
        }
    }

    private SummaryInfoDto summaryInfo(Map<String, Object> summary) {
        SummaryInfoDto dto = new SummaryInfoDto();
        if (summary.containsKey("ENQUIRY_COUNT")) {
            dto.totalCreditInquires = summary.get("ENQUIRY_COUNT").toString();
        }
        if (summary.containsKey("LOAN_COUNT")) {
            dto.noOfActiveLoans = summary.get("LOAN_COUNT").toString();
        }
        if (summary.containsKey("LOAN_OS")) {
            dto.activeLoansOuts = summary.get("LOAN_OS").toString();
        }
        if (summary.containsKey("LOAN_LESS_10K")) {
            dto.noOfNanoLoans = summary.get("LOAN_LESS_10K").toString();
        }
        if (summary.containsKey("LOAN_ABOVE_10K")) {
            dto.loanAbove10K = summary.get("LOAN_ABOVE_10K").toString();
        }
        if (summary.containsKey("CURRENT_30PLUS")) {
            dto.noOfCurrentAbove60Days = summary.get("CURRENT_30PLUS").toString();
        }
        if (summary.containsKey("DEFAULT_COUNT")) {
            dto.noOfWoLoans = summary.get("DEFAULT_COUNT").toString();
        }
        if (summary.containsKey("DEFAULT_OS")) {
            dto.noOfWoAmount = summary.get("DEFAULT_OS").toString();
        }
        if (summary.containsKey("MONTH24_30PLUS")) {
            dto.paymentLast24Month = summary.get("MONTH24_30PLUS").toString();
        }
        if (summary.containsKey("CLOSE_WITHIN_MATURITY")) {
            dto.completedInMaturity = summary.get("CLOSE_WITHIN_MATURITY").toString();
        }
        if (summary.containsKey("CLOSE_AFTER_MATURITY")) {
            dto.completedAfterMaturity = summary.get("CLOSE_AFTER_MATURITY").toString();
        }
        if (summary.get("CATEGORY").toString().equals("Applicant")) {
            dto.type = "Client";
        } else if (summary.get("CATEGORY").toString().equals("Additional CNIC")) {
            dto.type = "Nominee";
        }
        return dto;
    }

    private OpenLoansSummaryDto openLoansSummary(Map<String, Object> summary) {
        OpenLoansSummaryDto dto = new OpenLoansSummaryDto();
        if (summary.containsKey("LOAN_NO")) {
            dto.loan = summary.get("LOAN_NO").toString();
        }
        if (summary.containsKey("MEM_NAME")) {
            dto.mem = summary.get("MEM_NAME").toString();
        }
        if (summary.containsKey("ACCT_STATUS")) {
            dto.loansStatus = summary.get("ACCT_STATUS").toString();
        }
        if (summary.containsKey("LIMIT")) {
            dto.approvedLimit = summary.get("LIMIT").toString();
        }
        if (summary.containsKey("BALANCE")) {
            dto.currentOs = summary.get("BALANCE").toString();
        }
        if (summary.containsKey("OVERDUEAMOUNT")) {
            dto.amountOd = summary.get("OVERDUEAMOUNT").toString();
        }
        if (summary.containsKey("ACCT_NO")) {
            dto.loanId = summary.get("ACCT_NO").toString();
        }
        if (summary.containsKey("SECURE")) {
            dto.secureUnsecure = summary.get("SECURE").toString();
        }
        if (summary.containsKey("GROUP_ID")) {
            dto.groupId = summary.get("GROUP_ID").toString();
        }
        if (summary.containsKey("REPAYMENT_FREQ")) {
            dto.repaymentFrequency = summary.get("REPAYMENT_FREQ").toString();
        }
        if (summary.containsKey("OPEN_DATE")) {
            dto.disbursementDate = summary.get("OPEN_DATE").toString();
        }
        if (summary.containsKey("MATURITY_DATE")) {
            dto.maturityDate = summary.get("MATURITY_DATE").toString();
        }
        if (summary.containsKey("OVERDUEAMOUNT")) {
            dto.woAmount = summary.get("OVERDUEAMOUNT").toString();
        }
        if (summary.containsKey("RESCHEDULE_DT")) {
            dto.rescheduleDate = summary.get("RESCHEDULE_DT").toString();
        }
        if (summary.containsKey("LAST_PAYMENT")) {
            dto.lastPayment = summary.get("LAST_PAYMENT").toString();
        }
        if (summary.containsKey("ACCT_TY")) {
            dto.loanType = summary.get("ACCT_TY").toString();
        }
        if (summary.containsKey("STATUS_DATE")) {
            dto.positionAsOf = summary.get("STATUS_DATE").toString();
        }
        if (summary.containsKey("STATUS_DATE")) {
            dto.lastPaymentDate = summary.get("STATUS_DATE").toString();
        }
        return dto;
    }

    private OpenLoansSummaryDto openLoansSummaryDefault(Map<String, Object> summary) {

        OpenLoansSummaryDto dto = new OpenLoansSummaryDto();

        if (summary.get("UPD_STATUS").toString().equalsIgnoreCase("C")) {
            if (summary.containsKey("UPD_AMOUNT")) {
                dto.currentOs = summary.get("UPD_AMOUNT").toString();
            }
            if (summary.containsKey("UPD_ACCT_NO")) {
                dto.loanId = summary.get("UPD_ACCT_NO").toString();
            }
            if (summary.containsKey("UPD_STATUS_DATE")) {
                dto.positionAsOf = summary.get("UPD_STATUS_DATE").toString();
            }
            if (summary.containsKey("UPD_STATUS_DATE")) {
                dto.lastPaymentDate = summary.get("UPD_STATUS_DATE").toString();
            }
            if (summary.containsKey("UPD_ACCT_TY")) {
                dto.loanType = summary.get("UPD_ACCT_TY").toString();
            }
        } else {
            if (summary.containsKey("ORG_AMOUNT")) {
                dto.currentOs = summary.get("ORG_AMOUNT").toString();
            }
            if (summary.containsKey("ORG_ACCT_NO")) {
                dto.loanId = summary.get("ORG_ACCT_NO").toString();
            }
            if (summary.containsKey("ORG_STATUS_DATE")) {
                dto.positionAsOf = summary.get("ORG_STATUS_DATE").toString();
            }
            if (summary.containsKey("ORG_STATUS_DATE")) {
                dto.lastPaymentDate = summary.get("ORG_STATUS_DATE").toString();
            }
            if (summary.containsKey("ORG_ACCT_TY")) {
                dto.loanType = summary.get("ORG_ACCT_TY").toString();
            }
        }

        if (summary.containsKey("LOAN_NO")) {
            dto.loan = summary.get("LOAN_NO").toString();
        }
        if (summary.containsKey("MEM_NAME")) {
            dto.mem = summary.get("MEM_NAME").toString();
        }
        if (summary.containsKey("SECURE")) {
            dto.secureUnsecure = summary.get("SECURE").toString();
        }
        if (summary.containsKey("GROUP_ID")) {
            dto.groupId = summary.get("GROUP_ID").toString();
        }
        if (summary.containsKey("ORG_AMOUNT")) {
            dto.woAmount = summary.get("ORG_AMOUNT").toString();
        }
        dto.loansStatus = "OPEN";
        return dto;
    }


    private void repaymentHistory(Map<String, Object> history, OpenLoansSummaryDto dto) {
        if (history.containsKey("OK")) {
            dto.onTime = history.get("OK").toString();
        }
        if (history.containsKey("X")) {
            dto.btw15To29 = history.get("X").toString();
        }
        if (history.containsKey("P30")) {
            dto.plus30 = history.get("P30").toString();
        }
        if (history.containsKey("P60")) {
            dto.plus60 = history.get("P60").toString();
        }
        if (history.containsKey("P90")) {
            dto.plus90 = history.get("P90").toString();
        }
        if (history.containsKey("P120")) {
            dto.plus120 = history.get("P120").toString();
        }
        if (history.containsKey("P150")) {
            dto.plus150 = history.get("P150").toString();
        }
        if (history.containsKey("P180")) {
            dto.plus180 = history.get("P180").toString();
        }
        if (history.containsKey("LOSS")) {
            dto.loss = history.get("LOSS").toString();
        }
    }

    private OpenLoansSummaryDto summaryOfOpenLoansTasdeeq(Map<String, Object> root) {
        OpenLoansSummaryDto dto = new OpenLoansSummaryDto();

        if (root.containsKey("LOAN_SERIAL_NUMBER")) {
            dto.loan = root.get("LOAN_SERIAL_NUMBER").toString();
        }
        if (root.containsKey("BANK_NAME")) {
            dto.mem = root.get("BANK_NAME").toString();
        }
        if (root.containsKey("LOAN_ACCOUNT_STATUS")) {
            dto.loansStatus = root.get("LOAN_ACCOUNT_STATUS").toString();
        }
        if (root.containsKey("LOAN_LIMIT")) {
            dto.approvedLimit = root.get("LOAN_LIMIT").toString();
        }
        if (root.containsKey("OUTSTANDING_BALANCE")) {
            if (!(root.get("OUTSTANDING_BALANCE").toString().isEmpty() || root.get("OUTSTANDING_BALANCE").toString().equals("*"))) {
                dto.currentOs = String.valueOf(new BigDecimal(Double.parseDouble(root.get("OUTSTANDING_BALANCE").toString())).longValue());
            }
        }
        if (root.containsKey("MINIMUM_AMOUNT_DUE")) {
            if (!(root.get("MINIMUM_AMOUNT_DUE").toString().isEmpty() || root.get("MINIMUM_AMOUNT_DUE").toString().equals("*"))) {
                dto.amountOd = String.valueOf(new BigDecimal(Double.parseDouble(root.get("MINIMUM_AMOUNT_DUE").toString())).longValue());
            }
        }
        if (root.containsKey("PLUS_30")) {
            dto.plus30 = root.get("PLUS_30").toString();
        }
        if (root.containsKey("PLUS_60")) {
            dto.plus60 = root.get("PLUS_60").toString();
        }
        if (root.containsKey("PLUS_90")) {
            dto.plus90 = root.get("PLUS_90").toString();
        }
        if (root.containsKey("PLUS_120")) {
            dto.plus120 = root.get("PLUS_120").toString();
        }
        if (root.containsKey("PLUS_150")) {
            dto.plus150 = root.get("PLUS_150").toString();
        }
        if (root.containsKey("PLUS_180")) {
            dto.plus180 = root.get("PLUS_180").toString();
        }
        if (root.containsKey("MFI_DEFAULT")) {
            dto.loss = root.get("MFI_DEFAULT").toString();
        }

        if (root.containsKey("LOAN_ID")) {
            dto.loanId = root.get("LOAN_ID").toString();
        }
        if (root.containsKey("SECURED_UNSECURED")) {
            dto.secureUnsecure = root.get("SECURED_UNSECURED").toString();
        }
        if (root.containsKey("REPAYMENT_FREQUENCY")) {
            dto.repaymentFrequency = root.get("REPAYMENT_FREQUENCY").toString();
        }
        if (root.containsKey("FACILITY_DATE")) {
            dto.disbursementDate = root.get("FACILITY_DATE").toString();
        }
        if (root.containsKey("SECURITY_COLLATERAL")) {
            dto.securityCollateral = root.get("SECURITY_COLLATERAL").toString();
        }
        if (root.containsKey("MATURITY_DATE")) {
            dto.maturityDate = root.get("MATURITY_DATE").toString();
        }
        if (root.containsKey("WRITE_OFF_AMOUNT")) {
            if (!(root.get("WRITE_OFF_AMOUNT").toString().isEmpty() || root.get("WRITE_OFF_AMOUNT").toString().equals("*"))) {
                dto.woAmount = String.valueOf(new BigDecimal(Double.parseDouble(root.get("WRITE_OFF_AMOUNT").toString())).longValue());
            }
        }
        if (root.containsKey("LOAN_LAST_PAYMENT_AMOUNT")) {
            dto.lastPayment = root.get("LOAN_LAST_PAYMENT_AMOUNT").toString();
        }
        if (root.containsKey("POSITION_AS_OF")) {
            dto.positionAsOf = root.get("POSITION_AS_OF").toString();
        }
        if (root.containsKey("DATE_OF_LAST_PAYMENT_MADE")) {
            dto.lastPaymentDate = root.get("DATE_OF_LAST_PAYMENT_MADE").toString();
        }
        if (root.containsKey("LOAN_TYPE")) {
            dto.loanType = root.get("LOAN_TYPE").toString();
        }
        if (root.containsKey("LATE_PAYMENT_30")) {
            dto.overdue60DaysOd = root.get("LATE_PAYMENT_30").toString();
        }
        if (root.containsKey("RESTRUCTURING_DATE")) {
            dto.rescheduleDate = root.get("RESTRUCTURING_DATE").toString();
        }
        return dto;
    }

    private void creditScore(MfcibCommonDto mfcibCommonDto, Map<String, Object> root) {
        if (root.containsKey("CREDIT_SCORE")) {
            if (isList(root.get("CREDIT_SCORE"))) {
                ((List<Map<String, Object>>) root.get("CREDIT_SCORE")).forEach(credit -> {
                    mfcibCommonDto.creditScore.add(creditScore(credit));
                });
            } else {
                mfcibCommonDto.creditScore.add(creditScore((Map<String, Object>) root.get("CREDIT_SCORE")));
            }
        }
    }

    private CreditScoreDto creditScore(Map<String, Object> creditScore) {
        CreditScoreDto dto = new CreditScoreDto();

        if (creditScore.containsKey("SCORE")) {
            dto.score = creditScore.get("SCORE").toString();
        }
        if (creditScore.containsKey("ODDS")) {
            dto.odds = creditScore.get("ODDS").toString();
        }
        if (creditScore.containsKey("PROB_OF_DEFALUT")) {
            dto.probOfDefault = creditScore.get("PROB_OF_DEFALUT").toString();
        }
        if (creditScore.containsKey("PERCENTILE_RISK")) {
            dto.percentileRisk = creditScore.get("PERCENTILE_RISK").toString();
        }
        if (creditScore.containsKey("SBP_RISK_LEVEL")) {
            dto.sbpRiskLevel = creditScore.get("SBP_RISK_LEVEL").toString();
        }
        return dto;
    }

    // Ended By Naveed - Date 28-03-2022

    // Added By Areeba
    // Dated 30-03-2022
    // Audit Reports SCR
    public byte[] getAuditReport(String frmDate, String toDate, String fileName, long type) throws IOException {

        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        String auditAnalysis;
        if (type == 0)
            auditAnalysis = readFile(Charset.defaultCharset(), "AnalysisOfCoBorrowerDataPunching.txt");
        else
            auditAnalysis = readFile(Charset.defaultCharset(), "AnalysisOfOdReasons.txt");

        Query rs = em.createNativeQuery(auditAnalysis).setParameter("frmdt", frmDate).setParameter("todt", toDate);

        List<Object[]> auditAnalysisList = rs.getResultList();

        byte[] bytes = null;
        String csvFile = fileName + ".csv";
        try {
            InputStream inputStream;

            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(csvFile));
            if (type == 0) {
                fileWriter.write("REGION,AREA,BRANCH,CLIENT ID,CLIENT NAME,PRODUCT NAME,DISBURSED AMOUNT," +
                        "DISBURSEMENT DATE,OUTSTANDING LOAN (PR + SC),PDC PROVIDER NAME,PDC PROVIDER CNIC,ACCOUNT NUMBER,BANK NAME," +
                        "CLIENT HOME ADDRESS,RESIDENTIAL STATUS,CLIENT BUSINESS ADDRESS");
            } else {
                fileWriter.write("REGION,AREA,BRANCH,CLIENT ID,CLIENT NAME,PRODUCT NAME,DISBURSED AMOUNT," +
                        "DISBURSEMENT DATE,OVERDUE AMOUNT,OUTSTANDING,CLIENT CONTACT NUMBER," +
                        "CLIENT HOME ADDRESS,RESIDENTIAL STATUS,OD DAYS,OD REASONS,OD COMMENTS");
            }
            logger.info("LIST : " + auditAnalysisList);
            auditAnalysisList.forEach(l -> {
                if (type == 0) {
                    String REG_NM = l[0] == null ? "" : l[0].toString();
                    String AREA_NM = l[1] == null ? "" : l[1].toString();
                    String BRNCH_NM = l[2] == null ? "" : l[2].toString();
                    String CUSTOMER_ID = l[3] == null ? "" : l[3].toString();
                    String CUSTOMER_NAME = l[4] == null ? "" : l[4].toString();
                    String PRD_GRP_NM = l[5] == null ? "" : l[5].toString();
                    Long DISBURSED_AMOUNT = l[6] == null ? 0 : Long.valueOf(l[6].toString());
                    //java.sql.Timestamp DISBURSED_DATE = (java.sql.Timestamp) l[7];
                    String DISBURSED_DATE = null;
                    try {
                        DISBURSED_DATE = new SimpleDateFormat("dd-MM-yyyy").format(inputFormat.parse(l[7].toString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Long outs = l[8] == null ? 0 : Long.valueOf(l[8].toString());
                    String pdc_provider_nm = l[9] == null ? "" : l[9].toString();
                    String pdc_provider_cnic = l[10] == null ? "" : l[10].toString();
                    String ACCT_NUM = l[11] == null ? "" : l[11].toString();
                    String bank_nm = l[12] == null ? "" : l[12].toString();
                    String clnt_home_address = l[13] == null ? "" : l[13].toString();
                    String resident_status = l[14] == null ? "" : l[14].toString();
                    String clnt_business_address = l[15] == null ? "" : l[15].toString();

                    String line = String.format("\"%s\",%s,%s,%s,%s,%s,%d,%s,%d,%s,%s,%s,%s,%s,%s,%s",
                            REG_NM, AREA_NM, BRNCH_NM, CUSTOMER_ID, CUSTOMER_NAME, PRD_GRP_NM, DISBURSED_AMOUNT,
                            DISBURSED_DATE, outs, pdc_provider_nm, pdc_provider_cnic, ACCT_NUM, bank_nm,
                            clnt_home_address, resident_status, clnt_business_address);

                    try {
                        fileWriter.newLine();
                        fileWriter.write(line);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    String REG_NM = l[0] == null ? "" : l[0].toString();
                    String AREA_NM = l[1] == null ? "" : l[1].toString();
                    String BRNCH_NM = l[2] == null ? "" : l[2].toString();
                    String CUSTOMER_ID = l[3] == null ? "" : l[3].toString();
                    String CUSTOMER_NAME = l[4] == null ? "" : l[4].toString();
                    String PRD_GRP_NM = l[5] == null ? "" : l[5].toString();
                    Long DISBURSED_AMOUNT = l[6] == null ? 0 : Long.valueOf(l[6].toString());
                    //java.sql.Timestamp DISBURSED_DATE = (java.sql.Timestamp) l[7];
                    String DISBURSED_DATE = null;
                    try {
                        DISBURSED_DATE = new SimpleDateFormat("dd-MM-yyyy").format(inputFormat.parse(l[7].toString()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Long overdue = l[8] == null ? 0 : Long.valueOf(l[8].toString());
                    Long outs = l[9] == null ? 0 : Long.valueOf(l[9].toString());
                    String clnt_cntct_number = l[10] == null ? "" : l[10].toString();
                    String clnt_home_address = l[11] == null ? "" : l[11].toString();
                    String resident_status = l[12] == null ? "" : l[12].toString();
                    Long OVERDUE_DAYS = l[13] == null ? 0 : Long.valueOf(l[13].toString());
                    String od_reason = l[14] == null ? "" : l[14].toString();
                    String od_cmnts = l[15] == null ? "" : l[15].toString();

                    String line = String.format("\"%s\",%s,%s,%s,%s,%s,%d,%s,%d,%d,%s,%s,%s,%d,%s,%s",
                            REG_NM, AREA_NM, BRNCH_NM, CUSTOMER_ID, CUSTOMER_NAME, PRD_GRP_NM, DISBURSED_AMOUNT,
                            DISBURSED_DATE, overdue, outs, clnt_cntct_number,
                            clnt_home_address, resident_status, OVERDUE_DAYS, od_reason, od_cmnts);

                    try {
                        fileWriter.newLine();
                        fileWriter.write(line);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            fileWriter.close();

            inputStream = new FileInputStream(csvFile);
            bytes = IOUtils.toByteArray(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            bytes = new byte[0];
        } finally {
            System.gc();
        }
        return bytes;
    }
    // Ended by Areeba

    // Added by Naveed - Date - 10-05-2022
    // SCR - MCB Disbursement
    public byte[] getMcbDisburseFunds(String frmDt, String toDt, long branchSeq, String reportType, String userId, boolean isXls) throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);
        params.put("brnch_cd", String.valueOf(branchSeq));
        params.put("TYP", reportType);

        if (branchSeq != -1) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", branchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        }

        String mcbDisburseFundsStatusScript;
        mcbDisburseFundsStatusScript = readFile(Charset.defaultCharset(), "mcbDisburseFundsStatusReportsScript.txt");
        Query scriptQuery = em.createNativeQuery(mcbDisburseFundsStatusScript).setParameter("P_DATE_FROM", frmDt).setParameter("P_DATE_TO", toDt)
                .setParameter("P_BRNCH_SEQ", branchSeq);

        List<Object[]> resultSet = scriptQuery.getResultList();

        List<Map<String, ?>> reportDataSet = new ArrayList();

        resultSet.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("REG_SEQ", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("BRNCH_SEQ", l[3] == null ? "" : l[3].toString());
            map.put("AREA_NM", l[4] == null ? "" : l[4].toString());
            map.put("AREA_SEQ", l[5] == null ? "" : l[5].toString());
            map.put("DISURSEMENT_DATE", l[6]);
            map.put("CLIENT_ID", l[7] == null ? "" : l[7].toString());
            map.put("CLIENT_NAME", l[8] == null ? "" : l[8].toString());
            map.put("WALLET_NUM", l[9] == null ? "" : l[9].toString());
            map.put("XPIN_NUMBER", l[10] == null ? "" : l[10].toString());
            map.put("CNIC", l[11] == null ? "" : l[11].toString());
            map.put("AMOUNT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("CONTACT_NUM", l[13] == null ? "" : l[13].toString());
            map.put("FUND_TRANSFER_DATE", l[14]);
            map.put("TRANS_STATUS", l[15] == null ? "" : l[15].toString());
            map.put("TRANS_DATE", l[16]);
            map.put("PRODUCT", l[17] == null ? "" : l[17].toString());
            map.put("MODE_OF_DISBURSEMENT", l[18] == null ? "" : l[18].toString());
            map.put("REASON", l[19] == null ? "" : l[19].toString());

            reportDataSet.add(map);
        });

        if (reportType.equals("1")) {
            if (branchSeq != -1) {
                params.put("MCB_FUNDS_BRANCH", getJRDataSource(reportDataSet));
            } else {
                params.put("MCB_FUNDS_HO", getJRDataSource(reportDataSet));
            }
        } else {
            params.put("MCB_FUNDS_SUMRY", getJRDataSource(reportDataSet));
        }

        if (isXls) {
            return reportComponent.generateReport(MCB_DISBURSEMENT_FUNDS_REPORTS_EXCEL, params, null, isXls);
        } else {
            return reportComponent.generateReport(MCB_DISBURSEMENT_FUNDS_REPORTS, params, null);
        }
    }

    public byte[] getMcbDisburseReversalFunds(String frmDt, String toDt, long branchSeq, String reportType, String userId, boolean isXls) throws IOException {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);
        params.put("brnch_cd", String.valueOf(branchSeq));
        params.put("TYP", reportType);

        if (branchSeq != -1) {
            Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BRNCH_SEQ)
                    .setParameter("brnchSeq", branchSeq);
            Object[] obj = (Object[]) bi.getSingleResult();
            params.put("reg_nm", obj[0].toString());
            params.put("area_nm", obj[1].toString());
            params.put("brnch_nm", obj[2].toString());
            params.put("brnch_cd", obj[3].toString());
        }

        String mcbDisburseFundsStatusScript;
        mcbDisburseFundsStatusScript = readFile(Charset.defaultCharset(), "mcbDisburseFundsRerversalReportsScript.txt");
        Query scriptQuery = em.createNativeQuery(mcbDisburseFundsStatusScript).setParameter("P_DATE_FROM", frmDt).setParameter("P_DATE_TO", toDt)
                .setParameter("P_BRNCH_SEQ", branchSeq).setParameter("TYP", reportType);

        List<Object[]> resultSet = scriptQuery.getResultList();

        List<Map<String, ?>> reportDataSet = new ArrayList();

        resultSet.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("REG_SEQ", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("BRNCH_SEQ", l[3] == null ? "" : l[3].toString());
            map.put("AREA_NM", l[4] == null ? "" : l[4].toString());
            map.put("AREA_SEQ", l[5] == null ? "" : l[5].toString());
            map.put("DISURSEMENT_DATE", l[6]);
            map.put("CLIENT_ID", l[7] == null ? "" : l[7].toString());
            map.put("CLIENT_NAME", l[8] == null ? "" : l[8].toString());
            map.put("WALLET_NUM", l[9] == null ? "" : l[9].toString());
            map.put("XPIN_NUMBER", l[10] == null ? "" : l[10].toString());
            map.put("CNIC", l[11] == null ? "" : l[11].toString());
            map.put("AMOUNT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("CONTACT_NUM", l[13] == null ? "" : l[13].toString());
            map.put("FUND_TRANSFER_DATE", l[14]);
            map.put("TRANS_STATUS", l[15] == null ? "" : l[15].toString());
            map.put("TRANS_DATE", l[16]);
            map.put("PRODUCT", l[17] == null ? "" : l[17].toString());
            map.put("MODE_OF_DISBURSEMENT", l[18] == null ? "" : l[18].toString());
            map.put("REASON", l[19] == null ? "" : l[19].toString());
            map.put("REVERSAL_LOG_DATE", l[20]);
            map.put("REQUEST_CLOSE_DATE", l[21]);

            reportDataSet.add(map);
        });
        params.put("DISB_RVSL_RQST", getJRDataSource(reportDataSet));
        if (isXls) {
            return reportComponent.generateReport(MCB_DISBURSEMENT_REVERSAL_REQUEST_EXCEL, params, null, isXls);
        } else {
            return reportComponent.generateReport(MCB_DISBURSEMENT_REVERSAL_REQUEST, params, null);
        }
    }
    // Ended by Naveed

    /**
     * @Added Naveed
     * @Date, 08-06-2022
     * @Description, NADRA Verisys Status Report
     */
    public byte[] getNadraVerisysReport(String user, String frmDt, String toDt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);

        String nadraStatusScript;
        nadraStatusScript = readFile(Charset.defaultCharset(), "NADRA_VERISYS_ERROR_STATUS_REPORT.txt");
        Query nadraStatusQuery = em.createNativeQuery(nadraStatusScript).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

        List<Object[]> nadraStatusResult = nadraStatusQuery.getResultList();
        List<Map<String, ?>> resultSet = new ArrayList();

        nadraStatusResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("TOTAL_REQUESTS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("TOTAL_ERRORS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("NADRA_NEVER_ISSUE_ERR", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("TOTAL_ERROR_PERCENT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).doubleValue());

            resultSet.add(map);
        });

        params.put("NADRA_VERISYS", getJRDataSource(resultSet));

        return reportComponent.generateReport(NADRA_VERISYS_STATUS, params, getJRDataSource(resultSet));
    }

    /**
     * @Added, Naveed
     * @Date, 14-06-2022
     * @Description, SCR - systemization Funds Request
     */
    public byte[] getIBFTAndIFTReport(String user, String frmDt, String toDt, String type, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("TYP", type);

        String IbftAndIftScript;
        IbftAndIftScript = readFile(Charset.defaultCharset(), "IBFT_IFT_SCRIPT.txt");

        IbftAndIftScript = type.equals("1") ? IbftAndIftScript.replaceAll("%%", "NOT IN ('Khushhali Microfinance bank Limited', 'HBL')") : IbftAndIftScript.replaceAll("%%", "IN ('HBL')");

        Query IbftAndIftQuery = em.createNativeQuery(IbftAndIftScript).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

        List<Object[]> IbftAndIftResult = IbftAndIftQuery.getResultList();
        List<Map<String, ?>> resultSet = new ArrayList();

        IbftAndIftResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BENEF_NM", l[0] == null ? "" : l[0].toString());
            map.put("BENEF_ACCT_NUM", l[1] == null ? "" : l[1].toString());
            map.put("CUST_REF_NUM", l[2] == null ? "" : l[2].toString());
            map.put("BENEF_BANK_NM", l[3] == null ? "" : l[3].toString());
            map.put("CONTACT_NUM", l[4] == null ? "" : l[4].toString());
            map.put("BENEF_BANK_CD", l[5] == null ? "" : l[5].toString());
            map.put("EMAIL_ADDR", l[6] == null ? "" : l[6].toString());
            map.put("TRANS_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            resultSet.add(map);
        });

        if (type.equals("1")) {
            params.put("IBFT_FUNDS", getJRDataSource(resultSet));
        } else {
            params.put("IFT_FUNDS", getJRDataSource(resultSet));
        }

        if (isxls) {
            return reportComponent.generateReport(IBFT_IFT_FUNDS_TRANSFER_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(IBFT_IFT_FUNDS_TRANSFER, params, null);
        }
    }

    /**
     * @Added, Naveed
     * @Date, 14-06-2022
     * @Description, SCR - systemization Funds Request (Consolidated Report of Bank Funds)
     */
    public byte[] getConsolidatedBankFundsReport(String user, String frmDt, String toDt, boolean isxls) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);

        String consolidatedFundsScript;
        consolidatedFundsScript = readFile(Charset.defaultCharset(), "CONSOLIDATED_BANK-FUNDS_SCRIPT.txt");
        Query consolidatedFundsQuery = em.createNativeQuery(consolidatedFundsScript).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

        List<Object[]> consolidatedFundsResult = consolidatedFundsQuery.getResultList();
        List<Map<String, ?>> resultSet = new ArrayList();

        consolidatedFundsResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("BRNCH_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_SEQ", l[2] == null ? "" : l[2].toString());
            map.put("ACCT_NUM", l[3] == null ? "" : l[3].toString());
            map.put("CHEQUE_DISB_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("KSZB_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("BRNCH_EXP_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("USER", l[7] == null ? "" : l[7].toString());
            map.put("PURPOSE", l[8] == null ? "" : l[8].toString());
            map.put("BANK_NM", l[9] == null ? "" : l[9].toString());

            resultSet.add(map);
        });

        params.put("BANK_FUNDS", getJRDataSource(resultSet));

        if (isxls) {
            return reportComponent.generateReport(CONSOLIDATED_BANK_FUNDS_EXCEL, params, null, isxls);
        } else {
            return reportComponent.generateReport(CONSOLIDATED_BANK_FUNDS, params, null);
        }
    }

    @Transactional
    public List<MwFndsLodr> consolidatedBankFundsLoader(String user, String frmDt, String toDt) {

        Instant currIns = Instant.now();
        List<MwFndsLodr> resultSet = new ArrayList();

        List<Object[]> funds = em.createNativeQuery("SELECT * FROM MW_FNDS_LODR FD WHERE TRUNC(FD.CRTD_DT) = TRUNC(SYSDATE)").getResultList();

        if (funds != null && funds.size() > 0) {
            return null;
        }

        em.createNativeQuery("truncate table MW_FNDS_LODR").executeUpdate();

        String consolidatedFundsScript;
        consolidatedFundsScript = readFile(Charset.defaultCharset(), "CONSOLIDATED_BANK-FUNDS_SCRIPT.txt");
        Query consolidatedFundsQuery = em.createNativeQuery(consolidatedFundsScript).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

        List<Object[]> consolidatedFundsResult = consolidatedFundsQuery.getResultList();

        consolidatedFundsResult.forEach(l -> {
            if (l[1] != null) {
                MwFndsLodr mwFndsLodr = new MwFndsLodr();
                mwFndsLodr.setBranchSeq(l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
                mwFndsLodr.setFunds((l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue()) +
                        (l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue()) +
                        (l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue()));
                mwFndsLodr.setCrtdBy(user);
                mwFndsLodr.setCrtdDt(currIns);
                resultSet.add(mwFndsLodr);
            }
        });

        return mwFndsLodrRepository.save(resultSet);
    }

    @Transactional
    public Map<String, String> consolidatedBankFundsPost() {
        Map<String, String> response = new HashMap<>();

        StoredProcedureQuery storedProcedure = em.createStoredProcedureQuery("ho_funds");
        boolean flag = storedProcedure.execute();

        response.put("success", "Funds uploaded Successfully");
        return response;
    }

    /**
     * @Added, Naveed
     * @Date, 14-06-2022
     * @Description, SCR - systemization Funds Request (Bank Funds Request Data Loader)
     */
    public byte[] getBankFundsRequestDataLoader(String frmDt, String toDt, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "BANK_FUNDS_REQUEST_LOADER_SCRIPT.txt");
        Query set = em.createNativeQuery(disQry).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("BANK_NM", l[1] == null ? "" : l[1].toString());
            map.put("GL_ACCT_NUM", l[2] == null ? "" : l[2].toString());
            map.put("AMOUNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            if (l[4] != null && l[4].toString().equals("REG")) {
                map.put("GL_ACCT_NUM", Constant.getRegionInterunitCode(l[5] == null ? 0 : new BigDecimal(l[5].toString()).intValue()));
            }
            disbursements.add(map);
        });

        if (isXls) {
            return reportComponent.generateReport(BANK_FUNDS_REQUEST_DATA_LOADER_EXCEL, params, getJRDataSource(disbursements), isXls);
        } else {
            return reportComponent.generateReport(BANK_FUNDS_REQUEST_DATA_LOADER, params, getJRDataSource(disbursements));
        }
    }

    /**
     * @Added, Naveed
     * @Date, 24-06-2022
     * @Description, SCR - systemization Funds Request (Khushali Funds Letter)
     */
    public byte[] getKhushaliFundsLetter(String frmDt, String toDt, String userId, boolean isXls)
            throws IOException {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        String refDt = "";
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = format.parse(toDt);
            DateFormat frmt = new SimpleDateFormat("yyyyMMdd");
            refDt = frmt.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        params.put("ref_dt", refDt);
        long totalAmount = 0;

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "KHUSHALI_FUNDS_LETTER.txt");
        Query set = em.createNativeQuery(disQry).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, Object>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("ACCT_NM", l[0] == null ? "" : l[0].toString());
            map.put("ACCT_NUM", l[1] == null ? "" : l[1].toString());
            map.put("BANK_NM", l[2] == null ? "" : l[2].toString());
            map.put("STATUS", l[3] == null ? "" : l[3].toString());
            map.put("BRNCH_ADDR", l[4] == null ? "" : l[4].toString());
            map.put("BANK_CODE", l[5] == null ? "" : l[5].toString());
            map.put("AMOUNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            disbursements.add(map);
        });

        for (Map<String, Object> map : disbursements) {
            if (map.containsKey("AMOUNT")) {
                totalAmount += Long.parseLong(map.get("AMOUNT").toString());
            }
        }
        params.put("TOTAL_AMT", totalAmount);
        params.put("AMT_IN_WORDS", englishNumberToWords.convert(totalAmount));
        if (isXls) {
            return reportComponent.generateReport(KHUSHALI_FUNDS_LETTER_EXCEL, params, getJRDataSource(disbursements), isXls);
        } else {
            return reportComponent.generateReport(KHUSHALI_FUNDS_LETTER, params, getJRDataSource(disbursements));
        }
    }

    /**
     * @Added, Naveed
     * @Date, 29-06-2022
     * @Description, Export Data Utility
     */

    public List<Object[]> getCsvExport(String fromDate, String toDate, String typ) {

        System.out.println("01-Start");
        long start = System.currentTimeMillis();
        List<Object[]> resultSet = new ArrayList();

        if (typ.equals("1")) {
            resultSet = exportBankBookCsv(fromDate, toDate);
        } else if (typ.equals("2")) {
            resultSet = exportCashBookCsv(fromDate, toDate);
        } else if (typ.equals("3")) {
            resultSet = exportHoCurrentAccountCsv(fromDate, toDate);
        }

        long end = System.currentTimeMillis();
        System.out.println("01-Mintus " + (end - start) / 60000.0);

        return resultSet;
    }

    public List<Object[]> exportBankBookCsv(String fromDate, String toDate) {

        List<Object[]> resultSet = new ArrayList();
        Object[] head = new Object[14];
        head[0] = "REG_NM";
        head[1] = "AREA_NM";
        head[2] = "BRNCH_NM";
        head[3] = "JV_DATE";
        head[4] = "LEDGER_HEAD";
        head[5] = "CLNT_SEQ";
        head[6] = "JV_HEADER_SEQ";
        head[7] = "JV_REF_SEQ";
        head[8] = "GL_ACCT_NUM";
        head[9] = "INSTRUMENT_NO";
        head[10] = "RECIEPT";
        head[11] = "PAYMENT";
        head[12] = "JV_TYPE";
        head[13] = "JV_NARRATION";

        resultSet.add(head);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "EXPORT_DATA_BANK_BOOK.txt");
        Query set = em.createNativeQuery(disQry).setParameter("P_DATE_FROM", fromDate).setParameter("P_DATE_TO", toDate);

        resultSet.addAll(set.getResultList());

        return resultSet;
    }


    public List<Object[]> exportCashBookCsv(String fromDate, String toDate) {

        List<Object[]> resultSet = new ArrayList();
        Object[] head = new Object[14];
        head[0] = "REG_NM";
        head[1] = "AREA_NM";
        head[2] = "BRNCH_NM";
        head[3] = "JV_DATE";
        head[4] = "LEDGER_HEAD";
        head[5] = "CLNT_SEQ";
        head[6] = "JV_HEADER_SEQ";
        head[7] = "JV_REF_SEQ";
        head[8] = "INSTRUMENT_NO";
        head[9] = "RECIEPT";
        head[10] = "PAYMENT";
        head[11] = "JV_TYPE";
        head[12] = "JV_NARRATION";

        resultSet.add(head);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "EXPORT_DATA_CASH_BOOK.txt");
        Query set = em.createNativeQuery(disQry).setParameter("P_DATE_FROM", fromDate).setParameter("P_DATE_TO", toDate);

        resultSet.addAll(set.getResultList());

        return resultSet;
    }

    public List<Object[]> exportHoCurrentAccountCsv(String fromDate, String toDate) {

        List<Object[]> resultSet = new ArrayList();
        Object[] head = new Object[14];
        head[0] = "REG_NM";
        head[1] = "AREA_NM";
        head[2] = "BRNCH_NM";
        head[3] = "CLNT_SEQ";
        head[4] = "JV_DATE";
        head[5] = "LEDGER_HEAD";
        head[6] = "JV_TYPE";
        head[7] = "JV_HEADER_SEQ";
        head[8] = "JV_REF_SEQ";
        head[9] = "DEBIT";
        head[10] = "CREDIT";
        head[11] = "INSTRUMENT_NO";
        head[12] = "JV_NARRATION";
        head[13] = "JV_TRANS_LOCATION";

        resultSet.add(head);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "EXPORT_DATA_HO_CURRENT_ACCOUNT.txt");
        Query set = em.createNativeQuery(disQry).setParameter("P_DATE_FROM", fromDate).setParameter("P_DATE_TO", toDate);

        resultSet.addAll(set.getResultList());

        return resultSet;
    }

    /**
     * @Ended, Naveed
     * @Date, 29-06-2022
     * @Description, Export Data Utility
     */

    // Added by Areeba - SCR - HR Travelling
    public List<Object[]> exportTrvlngDtls(String monthDt) {

        List<Object[]> resultSet = new ArrayList();
        Object[] head = new Object[28];
        head[0] = "TRVLNG_MNTH";
        head[1] = "REG_SEQ";
        head[2] = "REG_NM";
        head[3] = "AREA_SEQ";
        head[4] = "AREA_NM";
        head[5] = "BRNCH_SEQ";
        head[6] = "BRNCH_NM";
        head[7] = "PORT_SEQ";
        head[8] = "PORT_NM";
        head[9] = "HRID";
        head[10] = "EMP_NM";
        head[11] = "WRKNG_STRT_DT";
        head[12] = "WRKNG_END_DT";
        head[13] = "WRKNG_DYS";
        head[14] = "TRVLNG_RATE";
        head[15] = "PER_DAY_VALUE";
        head[16] = "DISB_CLNTS";
        head[17] = "DISB_AMT";
        head[18] = "FIELD_TYPE_SEQ";
        head[19] = "FIELD_TYPE_DSCR";
        head[20] = "TRVL_AMT";
        head[21] = "TRVL_GRAND_TOTAL";
        head[22] = "TRNS_DATE";
        head[23] = "MW_HR_TRVLNG_DTL_SEQ";
        head[24] = "REF_CD_TRVLNG_ROL";
        head[25] = "REF_CD_TRVLNG_ROL_DSCR";
        head[26] = "REF_CD_CALC_TYP";
        head[27] = "REF_CD_CALC_TYP_DSCR";

        resultSet.add(head);

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "HrTrvlngDtl.txt");
        Query set = em.createNativeQuery(disQry).setParameter("monthDt", monthDt);

        resultSet.addAll(set.getResultList());

        return resultSet;
    }

    public List<Object[]> exportTrvlngHarmony(String monthDt) {

        List<Object[]> resultSet = new ArrayList();
        Object[] head = new Object[2];
        head[0] = "Employee ID";
        head[1] = "Allowance";

        resultSet.add(head);

        String disQry = " SELECT TO_CHAR(HTD.HRID), NVL(SUM(HTD.INCTVE),0) FROM MW_HR_TRVLNG_DTL HTD " +
                " WHERE HTD.TRVLNG_MNTH = TO_DATE( :monthDt ) " +
                " GROUP BY HTD.HRID ";
        Query set = em.createNativeQuery(disQry).setParameter("monthDt", monthDt);

        resultSet.addAll(set.getResultList());

        return resultSet;
    }
    // Ended by Areeba

    /**
     * @Added, Naveed
     * @Modified, Areeba
     * @Date, 26-08-2022
     * @Description, Finance Reports
     */
    public byte[] getSectorWisePortfolio(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "SECTOR_WISE_PORTFOLIO_AND_PAR.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BIZ_SECT_NM", l[0] == null ? "" : l[0].toString());
            map.put("DSB_CLNTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DSB_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("OST_LOANS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("OST_CLIENTS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OST_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("P_0_CLIENTS", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("P_0_AMOUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("P_1_CLIENTS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("P_1_AMOUNT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("P_29_CLIENTS", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("P_29_AMOUNT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("P_59_CLIENTS", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("P_59_AMOUNT", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("P_89_CLIENTS", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("P_89_AMOUNT", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("P_179_CLIENTS", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("P_179_AMOUNT", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("P_365_CLIENTS", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("P_365_AMOUNT", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("P_365ABV_CLIENTS", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
            map.put("P_365ABV_AMOUNT", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("P_29ABV_CLIENTS", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());
            map.put("P_29ABV_AMOUNT", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(SECTOR_WISE_PORTFOLIO_AND_PAR, params, null);
    }

    public byte[] getGenderAndAgeWisePortfolio(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "GENDER_AND_AGE_WISE_PORTFOLIO.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("GENDER", l[0] == null ? "" : l[0].toString());
            map.put("DSB_CLNTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DSB_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("OST_CLIENTS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("OST_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("BEL35_OST_CLIENTS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("BEL35_OST_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("ABV35_OST_CLIENTS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("ABV35_OST_AMT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(GENDER_AND_AGE_WISE_PORTFOLIO, params, null);
    }

    //Added by Areeba
    public byte[] getBranchWisePortfolio(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "BRANCH_WISE_PORTFOLIO.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRANCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("DISTRICT_NM", l[1] == null ? "" : l[1].toString());
            map.put("TEHSIL_NM", l[2] == null ? "" : l[2].toString());
            map.put("TYPE", l[3] == null ? "" : l[3].toString());
            map.put("PROVINCE", l[4] == null ? "" : l[4].toString());
            map.put("DSB_CLNTS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("DSB_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("OST_LOANS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("OST_CLIENTS", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("OST_AMT", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("OD_CLIENTS", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("OD_AMT", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(BRANCH_WISE_PORTFOLIO, params, null);
    }

    public byte[] getBranchWisePar(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "BRANCH_WISE_PAR.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRANCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("DISTRICT_NM", l[1] == null ? "" : l[1].toString());
            map.put("TEHSIL_NM", l[2] == null ? "" : l[2].toString());
            map.put("TYPE", l[3] == null ? "" : l[3].toString());
            map.put("PROVINCE", l[4] == null ? "" : l[4].toString());
            map.put("P_0_CLIENTS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("P_0_AMOUNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("P_1_CLIENTS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("P_1_AMOUNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("P_29_CLIENTS", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("P_29_AMOUNT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("P_59_CLIENTS", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("P_59_AMOUNT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("P_89_CLIENTS", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("P_89_AMOUNT", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("P_179_CLIENTS", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("P_179_AMOUNT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("P_365_CLIENTS", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("P_365_AMOUNT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
            map.put("P_365ABV_CLIENTS", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
            map.put("P_365ABV_AMOUNT", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());
            map.put("P_29ABV_CLIENTS", l[21] == null ? 0 : new BigDecimal(l[21].toString()).longValue());
            map.put("P_29ABV_AMOUNT", l[22] == null ? 0 : new BigDecimal(l[22].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(BRANCH_WISE_PAR, params, null);
    }

    public byte[] getProductWisePortfolio(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "PRODUCT_WISE_PORTFOLIO.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRODUCT_NM", l[0] == null ? "" : l[0].toString());
            map.put("DSB_CLNTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("DSB_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("NEW_DSB_CLNTS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("NEW_DSB_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("OST_CLIENTS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("OST_AMT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(PRODUCT_WISE_PORTFOLIO, params, null);
    }

    public byte[] getProductWisePar(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "PRODUCT_WISE_PAR.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PRODUCT_NM", l[0] == null ? "" : l[0].toString());
            map.put("P_0_CLIENTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("P_0_AMOUNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
            map.put("P_1_CLIENTS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            map.put("P_1_AMOUNT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
            map.put("P_29_CLIENTS", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
            map.put("P_29_AMOUNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
            map.put("P_59_CLIENTS", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
            map.put("P_59_AMOUNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
            map.put("P_89_CLIENTS", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
            map.put("P_89_AMOUNT", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
            map.put("P_179_CLIENTS", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());
            map.put("P_179_AMOUNT", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("P_365_CLIENTS", l[13] == null ? 0 : new BigDecimal(l[13].toString()).longValue());
            map.put("P_365_AMOUNT", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("P_365ABV_CLIENTS", l[15] == null ? 0 : new BigDecimal(l[15].toString()).longValue());
            map.put("P_365ABV_AMOUNT", l[16] == null ? 0 : new BigDecimal(l[16].toString()).longValue());
            map.put("P_29ABV_CLIENTS", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
            map.put("P_29ABV_AMOUNT", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(PRODUCT_WISE_PAR, params, null);
    }

    public byte[] getProvinceWiseOutstandingPortfolio(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "PROVINCE_WISE_OST_PORTFOLIO.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("PROV_NM", l[0] == null ? "" : l[0].toString());
            map.put("OST_CLIENTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("OST_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(PROVINCE_WISE_OST_PORTFOLIO, params, null);
    }

    public byte[] getLoanCycleWisePortfolio(String user, String todt) {

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", todt);

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "LOAN_CYCLE_WISE_PORTFOLIO.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("to_dt", todt);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("CYCLE_NO", l[0] == null ? 0 : new BigDecimal(l[0].toString()).longValue());
            map.put("OST_CLIENTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
            map.put("OST_AMT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());

            tableMap.add(map);
        });

        params.put("datasetsmm", getJRDataSource(tableMap));

        return reportComponent.generateReport(LOAN_CYCLE_WISE_PORTFOLIO, params, null);
    }

    //Ended by Areeba

    /**
     * @Ended, Naveed
     * @Date, 26-08-2022
     * @Description, Finance Reports
     */

    //Added by Areeba - Dated 9-9-2022 - Adc Failed Transaction Report
    public List<Object[]> getAdcFailedTransactions(String logDt) {

        List<Object[]> resultSet = new ArrayList();
        Object[] head = new Object[6];
        head[0] = "TRX_ID";
        head[1] = "CLNT_SEQ";
        head[2] = "TRX_DT";
        head[3] = "AMT";
        head[4] = "AGENT_ID";
        head[5] = "ERR_MSG";

        resultSet.add(head);

        String tableScript;
        tableScript = "select TRX_ID, CLNT_SEQ,\n" +
                "    TRX_DT, AMT, AGENT_ID, ERR_MSG\n" +
                " from mw_rcvry_load_log lg\n" +
                " where trunc(lg.LOAD_DT) = to_date(:logDt, 'dd-mm-rrrr')";
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("logDt", logDt);

        resultSet.addAll(tableQuery.getResultList());

        return resultSet;
    }
    // Ended by Areeba

    //Added by Areeba - 07-11-2022
    public byte[] getSamplingAudit(String asofDt, Long brnchSeq, long type, String user) {

        Query bi = em.createNativeQuery(Queries.BRANCH_INFO_BY_BRANCH).setParameter("brnchSeq",
                brnchSeq);
        Object[] obj = (Object[]) bi.getSingleResult();

        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("asofDt", asofDt);
        params.put("brnchCd", obj[2].toString());
        params.put("brnchNm", obj[3].toString());

        String tableScript;
        tableScript = readFile(Charset.defaultCharset(), "CLNT_AND_BRNCH_AUDIT_SAMPLING.txt");
        Query tableQuery = em.createNativeQuery(tableScript).setParameter("p_asof_dt", asofDt)
                .setParameter("p_brnch_seq", brnchSeq);

        List<Object[]> tableResult = tableQuery.getResultList();
        List<Map<String, ?>> tableMap = new ArrayList();

        tableResult.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("BRANCH_TYPE", l[3] == null ? "" : l[3].toString());
            map.put("BDO_NAME", l[4] == null ? "" : l[4].toString());
            map.put("PORTFOLIO_HANDLING_DATE", l[5]);
            map.put("PRODUCT_NM", l[6] == null ? "" : l[6].toString());
            map.put("CUSTOMER_ID", l[7] == null ? "" : l[7].toString());
            map.put("CUSTOMER_NAME", l[8] == null ? "" : l[8].toString());
            map.put("GENDER", l[9] == null ? "" : l[9].toString());
            map.put("ADDRESS", l[10] == null ? "" : l[10].toString());
            map.put("CLNT_PH_NUM", l[11] == null ? "" : l[11].toString());
            map.put("CYCLE_NO", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("LOAN_USER", l[13] == null ? "" : l[13].toString());
            map.put("LOAN_IN_KASHF", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
            map.put("NOMINEE_NAME", l[15] == null ? "" : l[15].toString());
            map.put("NOMINEE_CNIC", l[16] == null ? "" : l[16].toString());
            map.put("NOMINEE_RELATION", l[17] == null ? "" : l[17].toString());
            map.put("NOM_PH_NUM", l[18] == null ? "" : l[18].toString());
            map.put("PDC_NM", l[19] == null ? "" : l[19].toString());
            map.put("PDC_PH_NUM", l[20] == null ? "" : l[20].toString());
            map.put("PDC_CNIC_NUM", l[21] == null ? "" : l[21].toString());
            map.put("PDC_RELATION_WITH_CLIENT", l[22] == null ? "" : l[22].toString());
            map.put("NOK_NM", l[23] == null ? "" : l[23].toString());
            map.put("NOK_PH_NUM", l[24] == null ? "" : l[24].toString());
            map.put("NOK_CNIC_NUM", l[25] == null ? "" : l[25].toString());
            map.put("NOK_RELATION_WITH_CLIENT", l[26] == null ? "" : l[26].toString());
            map.put("LOAN_JUMP", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
            map.put("SECTOR", l[28] == null ? "" : l[28].toString());
            map.put("ACTIVITY", l[29] == null ? "" : l[29].toString());
            map.put("DISB_DATE", l[30]);
            map.put("DISB_PR", l[31] == null ? 0 : new BigDecimal(l[31].toString()).longValue());
            map.put("DISB_SC", l[32] == null ? 0 : new BigDecimal(l[32].toString()).longValue());
            map.put("OUTSTANDING_PRINCIPAL", l[33] == null ? 0 : new BigDecimal(l[33].toString()).longValue());
            map.put("OUTSTANDING_SC", l[34] == null ? 0 : new BigDecimal(l[34].toString()).longValue());
            map.put("OST_TOT", l[35] == null ? 0 : new BigDecimal(l[35].toString()).longValue());
            map.put("OVERDUE_PR", l[36] == null ? 0 : new BigDecimal(l[36].toString()).longValue());
            map.put("OVERDUE_AMOUNT_SC", l[37] == null ? 0 : new BigDecimal(l[37].toString()).longValue());
            map.put("OD_TOT", l[38] == null ? 0 : new BigDecimal(l[38].toString()).longValue());
            map.put("OVERDUE_DAYS", l[39] == null ? 0 : new BigDecimal(l[39].toString()).longValue());
            map.put("WRITE_OFF_DATE", l[40]);
            map.put("TTL_INSTALLMENTS", l[41] == null ? 0 : new BigDecimal(l[41].toString()).longValue());
            map.put("RESCHEDULE_DATE", l[42]);
            map.put("REM_INSTALLMENTS", l[43] == null ? 0 : new BigDecimal(l[43].toString()).longValue());
            map.put("CLNT_CNIC", l[44] == null ? "" : l[44].toString());
            map.put("NDI", l[45] == null ? 0 : new BigDecimal(l[45].toString()).longValue());
            map.put("NUM_OF_DPND", l[46] == null ? 0 : new BigDecimal(l[46].toString()).longValue());
            map.put("HOUSE_STATUS", l[47] == null ? "" : l[47].toString());
            map.put("FORM_FILLING_DATE", l[48]);
            map.put("ADV", l[49] == null ? 0 : new BigDecimal(l[49].toString()).longValue());
            map.put("SAME_DAY", l[50] == null ? 0 : new BigDecimal(l[50].toString()).longValue());
            map.put("BUSINESS_EXP", l[51] == null ? 0 : new BigDecimal(l[51].toString()).longValue());
            map.put("HOUSE_EXP", l[52] == null ? 0 : new BigDecimal(l[52].toString()).longValue());
            map.put("BUSINESS_INCM", l[53] == null ? 0 : new BigDecimal(l[53].toString()).longValue());
            map.put("OTHR_INCM", l[54] == null ? 0 : new BigDecimal(l[54].toString()).longValue());
            map.put("BM_SCREENING_DT", l[55]);
            map.put("BM_APRVL_DT", l[56]);
            map.put("OTHR_MFI", l[57] == null ? 0 : new BigDecimal(l[57].toString()).longValue());
            map.put("CUR_RCV_CASH", l[58] == null ? 0 : new BigDecimal(l[58].toString()).longValue());
            map.put("CUR_RCV_BANK", l[59] == null ? 0 : new BigDecimal(l[59].toString()).longValue());
            map.put("DEL_1_30", l[60] == null ? 0 : new BigDecimal(l[60].toString()).longValue());
            map.put("DEL_30", l[61] == null ? 0 : new BigDecimal(l[61].toString()).longValue());
            map.put("PREV_LOAN_PRD", l[62] == null ? "" : l[62].toString());
            map.put("PREV_LOAN_DSB_AMT", l[63] == null ? 0 : new BigDecimal(l[63].toString()).longValue());
            map.put("PREV_LOAN_CMPL_DT", l[64]);
            map.put("PEV_LOAN_ADV", l[65] == null ? 0 : new BigDecimal(l[65].toString()).longValue());
            map.put("PEV_LOAN_SAME_DAY", l[66] == null ? 0 : new BigDecimal(l[66].toString()).longValue());
            map.put("PREV_LOAN_DELI", l[67] == null ? 0 : new BigDecimal(l[67].toString()).longValue());
            map.put("PREV_LOAN_SECTOR", l[68] == null ? "" : l[68].toString());
            map.put("PREV_LOAN_ACTIVITY", l[69] == null ? "" : l[69].toString());
            map.put("PREV_RCV_CASH", l[70] == null ? 0 : new BigDecimal(l[70].toString()).longValue());
            map.put("PREV_RCV_BANK", l[71] == null ? 0 : new BigDecimal(l[71].toString()).longValue());
            map.put("ANIMAL_TAG", l[72] == null ? "" : l[72].toString());
            map.put("DONOR_NM", l[73] == null ? "" : l[73].toString());
            tableMap.add(map);
        });

        params.put("Dataset1", getJRDataSource(tableMap));
        return reportComponent.generateReport(CLNT_AND_BRNCH_AUDIT_SAMPLING, params, null, true);
    }
    //Ended by Areeba
}

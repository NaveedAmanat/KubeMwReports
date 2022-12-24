package com.idev4.reports.service;

import com.idev4.reports.domain.MwAutoListStatus;
import com.idev4.reports.repository.MwAutoStatusRepository;
import com.idev4.reports.util.SequenceFinder;
import com.idev4.reports.util.Sequences;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

//@Service
//@EnableScheduling
@RestController
@RequestMapping("/api")
public class AutoEmailScheduler {
    private static final String ACTIVE_CLIENTS = "Active_Clients_Report.jrxml";
    private static final String DUE_VS_RECOVERY = "Pre-Covid_Due_vs_Recovery.jrxml";
    private static final String MANAGEMENT_DASHBOARD = "Management_Dashboard.jrxml";
    private static final String PORTFOLIO_RESCHEDULING = "Portfolio_Rescheduling.jrxml";

//    @Autowired
//    private AutoMessageRepository smsMessageRepository;
    private static final String OLD_PORTFOLIO = "Old_Portfolio.jrxml";
    private static final String PAR_REPORT = "PAR_REPORT.jrxml";
    private static final String PORTFOLIO_QUALITY = "Portfolio_Quality_Report.jrxml";
    private static final String TARGET_ACHIEVEMENTS = "Target_Achievements_Report.jrxml";
    private static final String AMOUNT_TARGET_ACHIEVEMENTS = "Amount_Target_Achievements_Report.jrxml";
    @Value("${cron.expression.restart-policy}")
    private final int RESTART_POLICY = 3;
    @Value("${cron.expression.back-off-delay}")
    private final long BACK_OFF_DELAY = 180000;
    private final File file = null;
    @Autowired
    ServletContext context;
    Logger logger = LoggerFactory.getLogger(AutoEmailScheduler.class);
    String REPORTS_BASEPATH = "G:\\Management_Scripts_Updated\\";
    String QUERY_FILE_PATH = REPORTS_BASEPATH + "\\queries\\";
    String PDF_REPORTS = QUERY_FILE_PATH + "\\automation\\";
    @Autowired
    private Environment environment;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MwAutoStatusRepository mwAutoStatusRepository;
    private int pdfAttachcount = 0;
    private Multipart multipart;
    private ArrayList<String> notGeneratedReports = new ArrayList<>();
    private ArrayList<String> generatedReports = new ArrayList<>();

//    String QUERY_FILE_PATH = "";
//    String REPORTS_BASEPATH = "";
//    String PDF_REPORTS = "";

    //    @Scheduled(cron = "${cron.expression.generate-report-timeline}")
    @GetMapping("/auto-report")
    void generatePdfFile() {
        String asOfDateScript = "";
        try {
//            QUERY_FILE_PATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
//                    + file.separator + "queries" + file.separator;
//
//            REPORTS_BASEPATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
//                    + file.separator;
//
//            PDF_REPORTS = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
//                    + file.separator + "queries" + file.separator + "automation" + file.separator;

            notGeneratedReports.clear();
            generatedReports.clear();

            FileUtils.cleanDirectory(new File(PDF_REPORTS));

            String user = environment.getProperty("cron.user-name");
            Map<String, Object> params = new HashMap<>();

            asOfDateScript = readFile(Charset.defaultCharset(), "asOfDateScript.txt");
            Object asOfDate = entityManager.createNativeQuery(asOfDateScript).getSingleResult();
            String todayDate = "";
            String asOfDateString = "";
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
                todayDate = sdf.format(asOfDate);
                SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yyyy");
                asOfDateString = sdf1.format(asOfDate);
            } catch (Exception ex) {
                logger.error(ex.toString());
            }

            params.put("curr_user", user);
            params.put("TO_DATE", asOfDate);

//            getPortfolioQuality(user, params, asOfDateString, todayDate, "1");
//            getPortfolioQuality(user, params, asOfDateString, todayDate, "2");
//            getPortfolioQuality(user, params, asOfDateString, todayDate, "3");
//            getTargetVsAchievements(user, params, asOfDateString, todayDate);
//            getAmountTargetVsAchievements(user, params, asOfDateString, todayDate);
//            getPARMD(user, asOfDate, todayDate);
//            getManagementDashboard(params, todayDate, asOfDateString);
//            getReschedulingPortfolio(params, todayDate, asOfDateString);
            getDueVsRecovery(params, todayDate, asOfDateString);
//            getActiveClientsReport(params, todayDate, asOfDateString);
//            getPortfolioQualityOldPortfolio(params, todayDate, asOfDateString);

        } catch (Exception exp) {
            //smsAlert(ExceptionUtils.getStackTrace(exp).substring(0, 700));
            SendErrorEmail(asOfDateScript, "Main Report Section", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "Main Report Section");
        }
    }

    private void getActiveClientsReport(Map<String, Object> params, String todayDate, String asOfDate) {
        try {
            String activeClientsScript;
            activeClientsScript = readFile(Charset.defaultCharset(), "activeClientsScripts.txt");
            List<Object[]> listOfClients = entityManager.createNativeQuery(activeClientsScript)
                    .setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> clients = new ArrayList<>();

            listOfClients.forEach(clnt -> {
                Map<String, Object> map = new HashMap<>();
                map.put("REG_SEQ", (clnt[0] == null ? 0 : new BigDecimal(clnt[0].toString()).longValue()));
                map.put("REG_NM", (clnt[1] == null ? "" : clnt[1].toString()));
                map.put("CLNTS", (clnt[2] == null ? 0 : new BigDecimal(clnt[2].toString()).longValue()));

                clients.add(map);
            });

            if (clients.size() > 0) {
                generateMwxReport(ACTIVE_CLIENTS, params, getJRDataSource(clients), "9-Active_Clients_Report-" + todayDate);
                generatedReports.add("9-Active_Clients_Report-" + todayDate);
            } else {
                SendErrorEmail("", "9-Active_Clients_Report-", "No Data Found : " + activeClientsScript);
            }

        } catch (Exception exp) {
            SendErrorEmail("", "9-Active_Clients_Report-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "9-Active_Clients_Report-" + todayDate);
        }
    }

    private void getManagementDashboard(Map<String, Object> params, String todayDate, String asOfDate) {
        try {
            String detailQry;
            detailQry = readFile(Charset.defaultCharset(), "ManagementDashBoradQry.txt");

            Query rs2 = entityManager.createNativeQuery(detailQry).setParameter("asOfDate", asOfDate);
            List<Object[]> dtlObj = rs2.getResultList();
            List<Map<String, ?>> pymts = new ArrayList();

            dtlObj.forEach(l -> {
                Map<String, Object> map = new HashMap();
                map.put("REG_NM", l[0] == null ? "" : l[0].toString());
                map.put("ACTIVE_CLNTS", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
                map.put("ACTIVE_LOANS", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
                map.put("DSB_AMT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());

                map.put("OUTS_CRNT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
                map.put("OUTS_PR_CRNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
                map.put("OUTS_SC_CRNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());

                map.put("BRNCH_COUNT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
                map.put("PORT_COUNT", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());

                map.put("PAR_P1_PERC", l[9] == null ? 0 : new BigDecimal(l[9].toString()).doubleValue());
                map.put("P_1_OUTS", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
                map.put("P1_BRNCHS_ABV_2", l[11] == null ? 0 : new BigDecimal(l[11].toString()).doubleValue());
                map.put("P1_PORTS_ABV_25", l[12] == null ? 0 : new BigDecimal(l[12].toString()).doubleValue());

                map.put("PAR_P3_PERC", l[13] == null ? 0 : new BigDecimal(l[13].toString()).doubleValue());
                map.put("P_3_OUTS", l[14] == null ? 0 : new BigDecimal(l[14].toString()).longValue());
                map.put("P3_BRNCHS_ABV_1", l[15] == null ? 0 : new BigDecimal(l[15].toString()).doubleValue());
                map.put("P3_PORTS_ABV_15", l[16] == null ? 0 : new BigDecimal(l[16].toString()).doubleValue());

                map.put("LOANS_MFI_0", l[17] == null ? 0 : new BigDecimal(l[17].toString()).longValue());
                map.put("LOANS_MFI_1", l[18] == null ? 0 : new BigDecimal(l[18].toString()).longValue());
                map.put("LOANS_MFI_2", l[19] == null ? 0 : new BigDecimal(l[19].toString()).longValue());
                map.put("LOANS_MFI_3_ABV", l[20] == null ? 0 : new BigDecimal(l[20].toString()).longValue());

                map.put("ADV_60_DAYS", l[21] == null ? 0 : new BigDecimal(l[21].toString()).doubleValue());
                map.put("ABV_60_DAYS_RE", l[22] == null ? 0 : new BigDecimal(l[22].toString()).doubleValue());

                map.put("LZ_1", l[23] == null ? 0 : new BigDecimal(l[23].toString()).longValue());
                map.put("LZ_2", l[24] == null ? 0 : new BigDecimal(l[24].toString()).longValue());
                map.put("LZ_3", l[25] == null ? 0 : new BigDecimal(l[25].toString()).longValue());
                map.put("LZ_4", l[26] == null ? 0 : new BigDecimal(l[26].toString()).longValue());
                map.put("LZ_5", l[27] == null ? 0 : new BigDecimal(l[27].toString()).longValue());
                map.put("LZ_6", l[28] == null ? 0 : new BigDecimal(l[28].toString()).longValue());
                map.put("LZ_7", l[29] == null ? 0 : new BigDecimal(l[29].toString()).longValue());
                map.put("LZ_8", l[30] == null ? 0 : new BigDecimal(l[30].toString()).longValue());

                map.put("P_1_OUTS_P1", l[31] == null ? 0 : new BigDecimal(l[31].toString()).longValue());
                map.put("P_3_OUTS_P1", l[32] == null ? 0 : new BigDecimal(l[32].toString()).longValue());

                map.put("DSB_AMT_NEW", l[33] == null ? 0 : new BigDecimal(l[33].toString()).longValue());
                map.put("LOANS_NEW", l[34] == null ? 0 : new BigDecimal(l[34].toString()).longValue());
                map.put("DSB_AMT_NEW_KKK", l[35] == null ? 0 : new BigDecimal(l[35].toString()).longValue());
                map.put("LOANS_NEW_KKK", l[36] == null ? 0 : new BigDecimal(l[36].toString()).longValue());
                map.put("DSB_AMT_NEW_KM", l[37] == null ? 0 : new BigDecimal(l[37].toString()).longValue());
                map.put("LOANS_NEW_KM", l[38] == null ? 0 : new BigDecimal(l[38].toString()).longValue());
                map.put("DSB_AMT_NEW_KMWK", l[39] == null ? 0 : new BigDecimal(l[39].toString()).longValue());
                map.put("LOANS_NEW_KMWK", l[40] == null ? 0 : new BigDecimal(l[40].toString()).longValue());
                map.put("DSB_AMT_NEW_KSS", l[41] == null ? 0 : new BigDecimal(l[41].toString()).longValue());
                map.put("LOANS_NEW_KSS", l[42] == null ? 0 : new BigDecimal(l[42].toString()).longValue());
                map.put("DSB_AMT_NEW_KEL", l[43] == null ? 0 : new BigDecimal(l[43].toString()).longValue());
                map.put("LOANS_NEW_KEL", l[44] == null ? 0 : new BigDecimal(l[44].toString()).longValue());

                map.put("DSB_AMT_REP", l[45] == null ? 0 : new BigDecimal(l[45].toString()).longValue());
                map.put("LOANS_REP", l[46] == null ? 0 : new BigDecimal(l[46].toString()).longValue());
                map.put("DSB_AMT_REP_KKK", l[47] == null ? 0 : new BigDecimal(l[47].toString()).longValue());
                map.put("LOANS_REP_KKK", l[48] == null ? 0 : new BigDecimal(l[48].toString()).longValue());
                map.put("DSB_AMT_REP_KM", l[49] == null ? 0 : new BigDecimal(l[49].toString()).longValue());
                map.put("LOANS_REP_KM", l[50] == null ? 0 : new BigDecimal(l[50].toString()).longValue());
                map.put("DSB_AMT_REP_KMWK", l[51] == null ? 0 : new BigDecimal(l[51].toString()).longValue());
                map.put("LOANS_REP_KMWK", l[52] == null ? 0 : new BigDecimal(l[52].toString()).longValue());
                map.put("DSB_AMT_REP_KSS", l[53] == null ? 0 : new BigDecimal(l[53].toString()).longValue());
                map.put("LOANS_REP_KSS", l[54] == null ? 0 : new BigDecimal(l[54].toString()).longValue());
                map.put("DSB_AMT_REP_KEL", l[55] == null ? 0 : new BigDecimal(l[55].toString()).longValue());
                map.put("LOANS_REP_KEL", l[56] == null ? 0 : new BigDecimal(l[56].toString()).longValue());

                map.put("F_CLNTS", l[57] == null ? 0 : new BigDecimal(l[57].toString()).longValue());
                map.put("LOANS_REP_ROR", l[58] == null ? 0 : new BigDecimal(l[58].toString()).longValue());
                map.put("LOANS_CUR_ROR", l[59] == null ? 0 : new BigDecimal(l[59].toString()).longValue());

                map.put("DSB_AMT_NEW_KBK", l[60] == null ? 0 : new BigDecimal(l[60].toString()).longValue());
                map.put("LOANS_NEW_KBK", l[61] == null ? 0 : new BigDecimal(l[61].toString()).longValue());
                map.put("DSB_AMT_REP_KBK", l[62] == null ? 0 : new BigDecimal(l[62].toString()).longValue());
                map.put("LOANS_REP_KBK", l[63] == null ? 0 : new BigDecimal(l[63].toString()).longValue());

                map.put("LOANS_NEW_FP", l[64] == null ? 0 : new BigDecimal(l[64].toString()).longValue());
                map.put("LOANS_REP_FP", l[65] == null ? 0 : new BigDecimal(l[65].toString()).longValue());

                pymts.add(map);
            });

            if (pymts.size() > 0) {
                generateMwxReport(MANAGEMENT_DASHBOARD, params, getJRDataSource(pymts), "7-Management_Dashboard-" + todayDate);
                generatedReports.add("7-Management_Dashboard-" + todayDate);
            } else {
                SendErrorEmail("", "7-Management_Dashboard-", "No Data Found : " + detailQry);
            }

        } catch (Exception exp) {
            SendErrorEmail("", "7-Management_Dashboard-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "7-Management_Dashboard-" + todayDate);
        }
    }

    private void getDueVsRecovery(Map<String, Object> params, String todayDate, String asOfDate) {
        try {
            boolean noDataProceed = false;
            String detailQry;
            detailQry = readFile(Charset.defaultCharset(), "DueVsRecoveryDtlQry.txt");
            Query rs2 = entityManager.createNativeQuery(detailQry).setParameter("asOfDate", asOfDate);

            String detailQryWrtOf;
            detailQryWrtOf = readFile(Charset.defaultCharset(), "DueVsRecoveryDtlQry_wrtOf.txt");
            Query resultDetailWrtOf = entityManager.createNativeQuery(detailQryWrtOf);

            List<Object[]> dtlObj = rs2.getResultList();

            List<Object[]> detailWrtOf = resultDetailWrtOf.getResultList();

            List<Map<String, ?>> pymts = new ArrayList();

            dtlObj.forEach(l -> {

                Map<String, Object> map = new HashMap();
                map.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
                map.put("REG_NM", l[1] == null ? "" : l[1].toString());
                map.put("AREA_NM", l[2] == null ? "" : l[2].toString());
                map.put("BRNCH_NM", l[3] == null ? "" : l[3].toString());
                map.put("LOANS_CRNT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
                map.put("OUTS_CRNT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
                map.put("RCV_LOANS", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
                map.put("RCV_AMT", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
                map.put("PRD_GRP_SEQ", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
                map.put("REG_SEQ", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());
                map.put("AREA_SEQ", l[10] == null ? 0 : new BigDecimal(l[10].toString()).longValue());
                map.put("BRNCH_SEQ", l[11] == null ? 0 : new BigDecimal(l[11].toString()).longValue());

                Object[] d = detailWrtOf.stream().filter(

                        due -> (l[8].toString().equals(due[8].toString()) && l[11].toString().equals(due[11].toString()))

                ).findAny().orElse(null);
                if (d != null) {
                    map.put("WO_LOAN", d[4] == null ? 0 : new BigDecimal(d[4].toString()).longValue());
                    map.put("WO_AMT", d[5] == null ? 0 : new BigDecimal(d[5].toString()).longValue());
                    map.put("WO_RCV_AMT", d[7] == null ? 0 : new BigDecimal(d[7].toString()).longValue());
                    map.put("WO_RCV_LN", d[6] == null ? 0 : new BigDecimal(d[6].toString()).longValue());
                } else {
                    map.put("WO_LOAN", 0l);
                    map.put("WO_AMT", 0l);
                    map.put("WO_RCV_AMT", 0l);
                    map.put("WO_RCV_LN", 0l);
                }

                pymts.add(map);
            });

//            if(pymts.isEmpty()){
//                noDataProceed = true;
//                SendErrorEmail("", "Recovery_Monitoring_Report - DueVsRecoveryDtlQry" , "No Data Found : "+detailQry);
//            }


            String headerQuery;
            headerQuery = readFile(Charset.defaultCharset(), "DueVsRecoveryHeaderQry.txt");
            Query header = entityManager.createNativeQuery(headerQuery).setParameter("asOfDate", asOfDate);

            List<Object[]> hdrObj = header.getResultList();

            List<Map<String, ?>> hdrRes = new ArrayList();

            String headerQueryWrtOf;
            headerQueryWrtOf = readFile(Charset.defaultCharset(), "DueVsRecoveryHeaderQry_wrtOf.txt");
            Query headerWrtOf = entityManager.createNativeQuery(headerQueryWrtOf);

            List<Object[]> hdrObjWrtOf = headerWrtOf.getResultList();

            hdrObj.forEach(l -> {
                Map<String, Object> hrdmap = new HashMap();
                hrdmap.put("PRD_GRP_NM", l[0] == null ? "" : l[0].toString());
                hrdmap.put("REG_NM", l[1] == null ? "" : l[1].toString());
                hrdmap.put("LOANS_CRNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
                hrdmap.put("OUTS_CRNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
                hrdmap.put("RCV_LOANS", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
                hrdmap.put("RCV_AMT", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
                hrdmap.put("PRD_GRP_SEQ", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
                hrdmap.put("REG_SEQ", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());
//                hrdmap.put("PRD_GRP_SEQ", l[8] == null ? 0 : new BigDecimal(l[8].toString()).longValue());
//                hrdmap.put("REG_SEQ", l[9] == null ? 0 : new BigDecimal(l[9].toString()).longValue());

                Object[] d = hdrObjWrtOf.stream().filter(

                        due -> l[6].toString().equals(due[6].toString()) && l[7].toString().equals(due[7].toString())

                ).findAny().orElse(null);
                if (d != null) {
                    hrdmap.put("WO_LOAN", d[2] == null ? 0 : new BigDecimal(d[2].toString()).longValue());
                    hrdmap.put("WO_AMT", d[3] == null ? 0 : new BigDecimal(d[3].toString()).longValue());
                    hrdmap.put("WO_RCV_AMT", d[4] == null ? 0 : new BigDecimal(d[4].toString()).longValue());
                    hrdmap.put("WO_RCV_LN", d[5] == null ? 0 : new BigDecimal(d[5].toString()).longValue());
                } else {
                    hrdmap.put("WO_LOAN", 0l);
                    hrdmap.put("WO_AMT", 0l);
                    hrdmap.put("WO_RCV_AMT", 0l);
                    hrdmap.put("WO_RCV_LN", 0l);
                }
                hdrRes.add(hrdmap);
            });

//            if(!hdrRes.isEmpty()){
            params.put("dataset1", getJRDataSource(hdrRes));
//            }else{
//                noDataProceed = true;
//                SendErrorEmail("", "Recovery_Monitoring_Report - DueVsRecoveryHeaderQry" , "No Data Found : "+headerQuery);
//            }

            String regionWiseQuery;
            regionWiseQuery = readFile(Charset.defaultCharset(), "DueVsRecoveryHdrQry.txt");
            Query regoionHdr = entityManager.createNativeQuery(regionWiseQuery).setParameter("asOfDate", asOfDate);

            List<Object[]> regionObj = regoionHdr.getResultList();

            List<Map<String, ?>> regionHdrRes = new ArrayList();

            String regionQueryWrtOf;
            regionQueryWrtOf = readFile(Charset.defaultCharset(), "DueVsRecoveryHdrQry_wrtOf.txt");
            Query regionWrtOf = entityManager.createNativeQuery(regionQueryWrtOf);

            List<Object[]> regionObjWrtOf = regionWrtOf.getResultList();

            regionObj.forEach(l -> {
                Map<String, Object> hrdmap = new HashMap();
                hrdmap.put("REG_NM", l[0] == null ? "" : l[0].toString());
                hrdmap.put("LOANS_CRNT", l[1] == null ? 0 : new BigDecimal(l[1].toString()).longValue());
                hrdmap.put("OUTS_CRNT", l[2] == null ? 0 : new BigDecimal(l[2].toString()).longValue());
                hrdmap.put("RCV_LOANS", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
                hrdmap.put("RCV_AMT", l[4] == null ? 0 : new BigDecimal(l[4].toString()).longValue());
                hrdmap.put("REG_SEQ", l[5] == null ? 0 : new BigDecimal(l[5].toString()).longValue());
//                hrdmap.put("OUTS_CRNT", l[6] == null ? 0 : new BigDecimal(l[6].toString()).longValue());
//                hrdmap.put("REG_SEQ", l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue());

                Object[] d = regionObjWrtOf.stream().filter(

                        due -> l[5].toString().equals(due[5].toString())

                ).findAny().orElse(null);
                if (d != null) {
                    hrdmap.put("WO_LOAN", d[1] == null ? 0 : new BigDecimal(d[1].toString()).longValue());
                    hrdmap.put("WO_AMT", d[2] == null ? 0 : new BigDecimal(d[2].toString()).longValue());
                    hrdmap.put("WO_RCV_AMT", d[3] == null ? 0 : new BigDecimal(d[3].toString()).longValue());
                    hrdmap.put("WO_RCV_LN", d[4] == null ? 0 : new BigDecimal(d[4].toString()).longValue());
                } else {
                    hrdmap.put("WO_LOAN", 0l);
                    hrdmap.put("WO_AMT", 0l);
                    hrdmap.put("WO_RCV_AMT", 0l);
                    hrdmap.put("WO_RCV_LN", 0l);
                }

                regionHdrRes.add(hrdmap);
            });


//            if(!regionHdrRes.isEmpty()){
            params.put("dataset2", getJRDataSource(regionHdrRes));
//            }else{
//                noDataProceed = true;
//                SendErrorEmail("", "Recovery_Monitoring_Report - DueVsRecoveryHdrQry" , "No Data Found : "+regoionHdr);
//            }

            if (!noDataProceed) {
                generateMwxReport(DUE_VS_RECOVERY, params, getJRDataSource(pymts), "8-Recovery_Monitoring_Report-" + todayDate);
                generatedReports.add("8-Recovery_Monitoring_Report-" + todayDate);
            }
        } catch (Exception exp) {
            SendErrorEmail("", "8-Recovery_Monitoring_Report-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "8-Recovery_Monitoring_Report-" + todayDate);
        }
    }

    private void getReschedulingPortfolio(Map<String, Object> params, String todayDate, String asOfDate) {
        try {

            String detailQry;
            detailQry = readFile(Charset.defaultCharset(), "ReschedulingPortfolioDtlQry.txt");
            Query rs2 = entityManager.createNativeQuery(detailQry).setParameter("asOfDate", asOfDate);

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
            Query header = entityManager.createNativeQuery(headerQuery).setParameter("asOfDate", asOfDate);

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

            if (hdrRes.size() > 0 && pymts.size() > 0) {
                params.put("dataset1", getJRDataSource(hdrRes));
                generateMwxReport(PORTFOLIO_RESCHEDULING, params, getJRDataSource(pymts), "6-Rescheduling_Portfolio_Quality-" + todayDate);
                generatedReports.add("6-Rescheduling_Portfolio_Quality-" + todayDate);
            } else {
                SendErrorEmail("", "6-Rescheduling_Portfolio_Quality-" + todayDate, "No Data Found : " + headerQuery);
            }

        } catch (Exception exp) {
            SendErrorEmail("", "6-Rescheduling_Portfolio_Quality-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "6-Rescheduling_Portfolio_Quality-" + todayDate);
        }
    }

    private void getPortfolioQualityOldPortfolio(Map<String, Object> params, String todayDate, String asOfDate) {
        try {

            String detailQry;
            detailQry = readFile(Charset.defaultCharset(), "oldPortfolioDtlQuery.txt");
            Query rs2 = entityManager.createNativeQuery(detailQry).setParameter("asOfDate", asOfDate);

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
            Query header = entityManager.createNativeQuery(headerQuery).setParameter("asOfDate", asOfDate);

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

            if (hdrRes.size() > 0 && pymts.size() > 0) {
                params.put("dataset1", getJRDataSource(hdrRes));
                generateMwxReport(OLD_PORTFOLIO, params, getJRDataSource(pymts), "9-New_Portfolio_Quality-" + todayDate);
                generatedReports.add("9-New_Portfolio_Quality-" + todayDate);
            } else {
                SendErrorEmail("", "9-New_Portfolio_Quality-" + todayDate, "No Data Found : " + headerQuery);
            }

        } catch (Exception exp) {
            SendErrorEmail("", "9-New_Portfolio_Quality-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "9-New_Portfolio_Quality-" + todayDate);
        }
    }

    private void getPARMD(String user, Object asOfDate, String todayDate) {

        try {
            boolean noDataProceed = false;
            Map<String, Object> params = new HashMap<>();
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            asOfDate = new SimpleDateFormat("dd-MM-yyyy").format(inputFormat.parse(asOfDate.toString()));
            params.put("to_dt", asOfDate);
            // MD Report PAR Region wise

            String RegionWiseParQuery;
            RegionWiseParQuery = readFile(Charset.defaultCharset(), "RegionWiseParQuery.txt");
            Query regionWiseParQuery = entityManager.createNativeQuery(RegionWiseParQuery)
                    .setParameter("to_dt", asOfDate);
            List<Object[]> regionWiseParRs = regionWiseParQuery.getResultList();

            List<Map<String, ?>> regionWiseParData = new ArrayList();
            regionWiseParRs.forEach(w -> {
                Map<String, Object> parm = new HashMap<>();
                // parm.put( "REGION_NAME", w[ 0 ] == null ? 0 : w[ 0 ].toString() );
                if (w[0].equals("AAOverAll")) {
                    parm.put("REGION_NAME", w[0] == null ? "" : w[0].toString().substring(2));
                } else {
                    parm.put("REGION_NAME", w[0] == null ? "" : w[0].toString());
                }
                parm.put("PAR_1_PERC", w[1] == null ? 0 : new BigDecimal(w[1].toString()).doubleValue());
                parm.put("PAR_5_PERC", w[2] == null ? 0 : new BigDecimal(w[2].toString()).doubleValue());
                parm.put("PAR_15_PERC", w[3] == null ? 0 : new BigDecimal(w[3].toString()).doubleValue());
                parm.put("PAR_30_PERC", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
                parm.put("PAR_45_PERC", w[5] == null ? 0 : new BigDecimal(w[5].toString()).doubleValue());
                parm.put("PAR_60_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
                parm.put("PAR_90_PERC", w[7] == null ? 0 : new BigDecimal(w[7].toString()).doubleValue());
                parm.put("PAR_91_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
                parm.put("PAR_30_AND_ABV_PERC", w[9] == null ? 0 : new BigDecimal(w[9].toString()).doubleValue());

                parm.put("PAR_11_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
                parm.put("PAR_51_PERC", w[11] == null ? 0 : new BigDecimal(w[11].toString()).doubleValue());
                parm.put("PAR_151_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
                parm.put("PAR_301_PERC", w[13] == null ? 0 : new BigDecimal(w[13].toString()).doubleValue());
                parm.put("PAR_451_PERC", w[14] == null ? 0 : new BigDecimal(w[14].toString()).doubleValue());
                parm.put("PAR_601_PERC", w[15] == null ? 0 : new BigDecimal(w[15].toString()).doubleValue());
                parm.put("PAR_901_PERC", w[16] == null ? 0 : new BigDecimal(w[16].toString()).doubleValue());
                parm.put("PAR_911_PERC", w[17] == null ? 0 : new BigDecimal(w[17].toString()).doubleValue());
                parm.put("PAR_301_AND_ABV_PERC", w[18] == null ? 0 : new BigDecimal(w[18].toString()).doubleValue());
                regionWiseParData.add(parm);
            });

            if (regionWiseParData.size() > 0) {
                params.put("reg_data", getJRDataSource(regionWiseParData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - RegionWiseParQuery", "No Data Found : " + RegionWiseParQuery);
            }

            // MD Report PAR Region wise
            String PrdWiseParQuery;
            PrdWiseParQuery = readFile(Charset.defaultCharset(), "PrdWiseParQuery.txt");
            Query prdWiseParQuery = entityManager.createNativeQuery(PrdWiseParQuery).setParameter("to_dt",
                    asOfDate);
            List<Object[]> prdWiseParRs = prdWiseParQuery.getResultList();

            List<Map<String, ?>> prdWiseParData = new ArrayList();
            prdWiseParRs.forEach(w -> {
                Map<String, Object> parm = new HashMap<>();
                if (w[0].equals("AAOverall")) {
                    parm.put("PRODUCT_DESC", w[0] == null ? "" : w[0].toString().substring(2));
                } else {
                    parm.put("PRODUCT_DESC", w[0] == null ? "" : w[0].toString());
                }
                parm.put("PAR_1_PERC", w[1] == null ? 0 : new BigDecimal(w[1].toString()).doubleValue());
                parm.put("PAR_5_PERC", w[2] == null ? 0 : new BigDecimal(w[2].toString()).doubleValue());
                parm.put("PAR_15_PERC", w[3] == null ? 0 : new BigDecimal(w[3].toString()).doubleValue());
                parm.put("PAR_30_PERC", w[4] == null ? 0 : new BigDecimal(w[4].toString()).doubleValue());
                parm.put("PAR_45_PERC", w[5] == null ? 0 : new BigDecimal(w[5].toString()).doubleValue());
                parm.put("PAR_60_PERC", w[6] == null ? 0 : new BigDecimal(w[6].toString()).doubleValue());
                parm.put("PAR_90_PERC", w[7] == null ? 0 : new BigDecimal(w[7].toString()).doubleValue());
                parm.put("PAR_91_PERC", w[8] == null ? 0 : new BigDecimal(w[8].toString()).doubleValue());
                parm.put("PAR_30_AND_ABV_PERC", w[9] == null ? 0 : new BigDecimal(w[9].toString()).doubleValue());

                parm.put("PAR_11_PERC", w[10] == null ? 0 : new BigDecimal(w[10].toString()).doubleValue());
                parm.put("PAR_51_PERC", w[11] == null ? 0 : new BigDecimal(w[11].toString()).doubleValue());
                parm.put("PAR_151_PERC", w[12] == null ? 0 : new BigDecimal(w[12].toString()).doubleValue());
                parm.put("PAR_301_PERC", w[13] == null ? 0 : new BigDecimal(w[13].toString()).doubleValue());
                parm.put("PAR_451_PERC", w[14] == null ? 0 : new BigDecimal(w[14].toString()).doubleValue());
                parm.put("PAR_601_PERC", w[15] == null ? 0 : new BigDecimal(w[15].toString()).doubleValue());
                parm.put("PAR_901_PERC", w[16] == null ? 0 : new BigDecimal(w[16].toString()).doubleValue());
                parm.put("PAR_911_PERC", w[17] == null ? 0 : new BigDecimal(w[17].toString()).doubleValue());
                parm.put("PAR_301_AND_ABV_PERC", w[18] == null ? 0 : new BigDecimal(w[18].toString()).doubleValue());
                prdWiseParData.add(parm);
            });

            if (prdWiseParData.size() > 0) {
                params.put("prd_data", getJRDataSource(prdWiseParData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - prdWiseParData", "No Data Found : " + PrdWiseParQuery);
            }

            // MD Report PAR Loan Cycle wise
            String LoanCycleParQuery;
            LoanCycleParQuery = readFile(Charset.defaultCharset(), "LoanCycleParQuery.txt");
            Query loanCycleParQuery = entityManager.createNativeQuery(LoanCycleParQuery)
                    .setParameter("to_dt", asOfDate);

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
                // parm.put( "LOAN_CYCLE_CD", w[ 0 ] == null ? 0 : w[ 0 ].toString() );
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
                parm.put("OD_P30ABV", w[18] == null ? 0 : new BigDecimal(w[18].toString()).longValue());
                parm.put("PAR_30_AND_ABV_PERC", w[19] == null ? 0 : new BigDecimal(w[19].toString()).doubleValue());
                loanCycleParData.add(parm);
            });

            if (loanCycleParData.size() > 0) {
                params.put("lc_data", getJRDataSource(loanCycleParData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - loanCycleParData", "No Data Found : " + LoanCycleParQuery);
            }

            // MD Report PAR Amount wise
            String AmtWiseParQuery;
            AmtWiseParQuery = readFile(Charset.defaultCharset(), "AmtWiseParQuery.txt");
            Query amtWiseParQuery = entityManager.createNativeQuery(AmtWiseParQuery).setParameter("to_dt",
                    asOfDate);

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
                parm.put("OD_P30ABV", w[17] == null ? 0 : new BigDecimal(w[17].toString()).longValue());
                parm.put("PAR_30_AND_ABV_PERC", w[18] == null ? 0 : new BigDecimal(w[18].toString()).doubleValue());
                amtWiseParData.add(parm);
            });

            if (amtWiseParData.size() > 0) {
                params.put("amt_data", getJRDataSource(amtWiseParData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - amtWiseParData", "No Data Found : " + AmtWiseParQuery);
            }

            // MD Report PAR Loan USer wise
            String LoanUsrParQuery;
            LoanUsrParQuery = readFile(Charset.defaultCharset(), "LoanUsrParQuery.txt");
            Query loanUsrParQuery = entityManager.createNativeQuery(LoanUsrParQuery).setParameter("to_dt",
                    asOfDate);

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
                parm.put("OD_P30ABV", w[17] == null ? 0 : new BigDecimal(w[17].toString()).longValue());
                parm.put("PAR_30_AND_ABV_PERC", w[18] == null ? 0 : new BigDecimal(w[18].toString()).doubleValue());
                loanUsrParData.add(parm);
            });

            if (loanUsrParData.size() > 0) {
                params.put("user_data", getJRDataSource(loanUsrParData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - loanUsrParData", "No Data Found : " + LoanUsrParQuery);
            }

            // MD Report PAR Reagion wise Od
            String RegOdParQuery;
            RegOdParQuery = readFile(Charset.defaultCharset(), "RegOdParQuery.txt");
            Query regOdParQuery = entityManager.createNativeQuery(RegOdParQuery).setParameter("to_dt",
                    asOfDate);

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
                parm.put("PAR_30_AND_ABV_PERC", w[13] == null ? 0 : new BigDecimal(w[13].toString()).doubleValue());
                regOdParData.add(parm);
            });

            if (regOdParData.size() > 0) {
                params.put("reg_od_data", getJRDataSource(regOdParData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - regOdParData", "No Data Found : " + RegOdParQuery);
            }

            // MD Report PAR Reagion wise branch
            String RegbrnchOdParQuery;
            RegbrnchOdParQuery = readFile(Charset.defaultCharset(), "RegbrnchOdParQuery.txt");
            Query regbrnchOdParQuery = entityManager.createNativeQuery(RegbrnchOdParQuery)
                    .setParameter("to_dt", asOfDate);

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

            if (regbrnchOdParData.size() > 0) {
                params.put("reg_od_brnch_data", getJRDataSource(regbrnchOdParData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - regbrnchOdParData", "No Data Found : " + RegbrnchOdParQuery);
            }

            // MD Report PAR Reagion wise branch 2
            String RegbrnchOdParQuery2;
            RegbrnchOdParQuery2 = readFile(Charset.defaultCharset(), "RegbrnchOdParQuery2.txt");
            Query regbrnchOdParQuery2 = entityManager.createNativeQuery(RegbrnchOdParQuery2)
                    .setParameter("to_dt", asOfDate);

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

            if (regbrnchOdParData2.size() > 0) {
                params.put("reg_od_brnch_data2", getJRDataSource(regbrnchOdParData2));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Get PAR Mgmt Dashboard - regbrnchOdParData2", "No Data Found : " + RegbrnchOdParQuery2);
            }

            if (!noDataProceed) {
                generateMwxReport(PAR_REPORT, params, null, "6-PAR_Breakdown-" + todayDate);
                generatedReports.add("6-PAR_Breakdown-" + todayDate);
            }

        } catch (Exception exp) {
            SendErrorEmail("", "6-PAR_Breakdown-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "6-PAR_Breakdown-" + todayDate);
        }
    }

    private void getPortfolioQuality(String user, Map<String, Object> params, String asOfDate, String todayDate, String type) {

        String reportName = "Portfolio_Quality";
        params.put("type", reportName);

        if (type.equals("1")) {
            reportName = "Portfolio Quality Report";
        } else if (type.equals("2")) {
            reportName = "Rescheduled Portfolio Quality Report";
        } else if (type.equals("3")) {
            reportName = "Regular Client Portfolio Quality Report";
        }
        params.put("type", reportName);

        try {

            boolean noDataProceed = false;

            String portQltySummeryScript;
            portQltySummeryScript = readFile(Charset.defaultCharset(), "portfolioQualitySummery.txt");
            List<Object[]> sumLists = entityManager.createNativeQuery(portQltySummeryScript).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> parStatusData = new ArrayList<>();
            sumLists.forEach(pars -> {
                Map<String, Object> map = new HashMap<>();

                map.put("SR_NO", (pars[0] == null ? 0 : new BigDecimal(pars[0].toString()).longValue()));
                map.put("BUCKT", (pars[1] == null ? "" : pars[1].toString()));

                map.put("OD_CUST_KKK", (pars[2] == null ? 0 : new BigDecimal(pars[2].toString()).longValue()));
                map.put("OD_PAR_KKK", (pars[3] == null ? 0 : new BigDecimal(pars[3].toString()).longValue()));
                map.put("OUTSTANDING_KKK", (pars[4] == null ? 0 : new BigDecimal(pars[4].toString()).longValue()));
                map.put("PERCENT_KKK", (pars[5] == null ? 0 : new BigDecimal(pars[5].toString()).doubleValue()));

                map.put("OD_CUST_KEL", (pars[6] == null ? 0 : new BigDecimal(pars[6].toString()).longValue()));
                map.put("OD_PAR_KEL", (pars[7] == null ? 0 : new BigDecimal(pars[7].toString()).longValue()));
                map.put("OUTSTANDING_KEL", (pars[8] == null ? 0 : new BigDecimal(pars[8].toString()).longValue()));
                map.put("PERCENT_KEL", (pars[9] == null ? 0 : new BigDecimal(pars[9].toString()).doubleValue()));

                map.put("OD_CUST_KSK", (pars[10] == null ? 0 : new BigDecimal(pars[10].toString()).longValue()));
                map.put("OD_PAR_KSK", (pars[11] == null ? 0 : new BigDecimal(pars[11].toString()).longValue()));
                map.put("OUTSTANDING_KSK", (pars[12] == null ? 0 : new BigDecimal(pars[12].toString()).longValue()));
                map.put("PERCENT_KSK", (pars[13] == null ? 0 : new BigDecimal(pars[13].toString()).doubleValue()));

                map.put("OD_CUST_KM", (pars[14] == null ? 0 : new BigDecimal(pars[14].toString()).longValue()));
                map.put("OD_PAR_KM", (pars[15] == null ? 0 : new BigDecimal(pars[15].toString()).longValue()));
                map.put("OUTSTANDING_KM", (pars[16] == null ? 0 : new BigDecimal(pars[16].toString()).longValue()));
                map.put("PERCENT_KM", (pars[17] == null ? 0 : new BigDecimal(pars[17].toString()).doubleValue()));

                map.put("OD_CUST_KSS", (pars[18] == null ? 0 : new BigDecimal(pars[18].toString()).longValue()));
                map.put("OD_PAR_KSS", (pars[19] == null ? 0 : new BigDecimal(pars[19].toString()).longValue()));
                map.put("OUTSTANDING_KSS", (pars[20] == null ? 0 : new BigDecimal(pars[20].toString()).longValue()));
                map.put("PERCENT_KSS", (pars[21] == null ? 0 : new BigDecimal(pars[21].toString()).doubleValue()));

                map.put("OD_CUST_KAKSIL", (pars[22] == null ? 0 : new BigDecimal(pars[22].toString()).longValue()));
                map.put("OD_PAR_KAKSIL", (pars[23] == null ? 0 : new BigDecimal(pars[23].toString()).longValue()));
                map.put("OUTSTANDING_KAKSIL", (pars[24] == null ? 0 : new BigDecimal(pars[24].toString()).longValue()));
                map.put("PERCENT_KAKSIL", (pars[25] == null ? 0 : new BigDecimal(pars[25].toString()).doubleValue()));

                map.put("OD_CUST_KBK", (pars[26] == null ? 0 : new BigDecimal(pars[26].toString()).longValue()));
                map.put("OD_PAR_KBK", (pars[27] == null ? 0 : new BigDecimal(pars[27].toString()).longValue()));
                map.put("OUTSTANDING_KBK", (pars[28] == null ? 0 : new BigDecimal(pars[28].toString()).longValue()));
                map.put("PERCENT_KBK", (pars[29] == null ? 0 : new BigDecimal(pars[29].toString()).doubleValue()));

                map.put("TOTAL_OD_CUST", (pars[30] == null ? 0 : new BigDecimal(pars[30].toString()).longValue()));
                map.put("TOTAL_OD_PAR", (pars[31] == null ? 0 : new BigDecimal(pars[31].toString()).longValue()));
                map.put("TOTAL_OUTSTANDING", (pars[32] == null ? 0 : new BigDecimal(pars[32].toString()).longValue()));
                map.put("TOTAL_PERCENT", (pars[33] == null ? 0 : new BigDecimal(pars[33].toString()).doubleValue()));

                map.put("OD_CUST_HIL", (pars[34] == null ? 0 : new BigDecimal(pars[34].toString()).longValue()));
                map.put("OD_PAR_HIL", (pars[35] == null ? 0 : new BigDecimal(pars[35].toString()).longValue()));
                map.put("OUTSTANDING_HIL", (pars[36] == null ? 0 : new BigDecimal(pars[36].toString()).longValue()));
                map.put("PERCENT_HIL", (pars[37] == null ? 0 : new BigDecimal(pars[37].toString()).doubleValue()));

                map.put("OD_CUST_KSWK", (pars[38] == null ? 0 : new BigDecimal(pars[38].toString()).longValue()));
                map.put("OD_PAR_KSWK", (pars[39] == null ? 0 : new BigDecimal(pars[39].toString()).longValue()));
                map.put("OUTSTANDING_KSWK", (pars[40] == null ? 0 : new BigDecimal(pars[40].toString()).longValue()));
                map.put("PERCENT_KSWK", (pars[41] == null ? 0 : new BigDecimal(pars[41].toString()).doubleValue()));

                map.put("OD_CUST_KFK", (pars[42] == null ? 0 : new BigDecimal(pars[42].toString()).longValue()));
                map.put("OD_PAR_KFK", (pars[43] == null ? 0 : new BigDecimal(pars[43].toString()).longValue()));
                map.put("OUTSTANDING_KFK", (pars[44] == null ? 0 : new BigDecimal(pars[44].toString()).longValue()));
                map.put("PERCENT_KFK", (pars[45] == null ? 0 : new BigDecimal(pars[45].toString()).doubleValue()));

                parStatusData.add(map);
            });

            if (parStatusData.size() > 0) {
                params.put("summery", getJRDataSource(parStatusData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Portfolio Quality Report - Summary section", "No Data Found : \n\n\n" + portQltySummeryScript);
            }

//            String portfolioQltySummeryScript;
//            portfolioQltySummeryScript = readFile(Charset.defaultCharset(), "portfolioQualitySummeryHdr.txt");
//            List<Object[]> portfolioSummary = entityManager.createNativeQuery(portfolioQltySummeryScript).setParameter("p_type",type).getResultList();
//
//            List<Map<String, ?>> portfolioSummaryData = new ArrayList<>();
//            portfolioSummary.forEach(pars -> {
//                Map<String, Object> map = new HashMap<>();
//
//                map.put("PRODUCT_ID", (pars[0] == null ? 0 : new BigDecimal(pars[0].toString()).longValue()));
//                map.put("PRD_GRP_NM", (pars[1] == null ? "" : pars[1].toString()));
//
//                map.put("PAR_1_4", (pars[2] == null ? 0 : new BigDecimal(pars[2].toString()).longValue()));
//                map.put("PAR_5_15", (pars[3] == null ? 0 : new BigDecimal(pars[3].toString()).longValue()));
//                map.put("PAR_16_29", (pars[4] == null ? 0 : new BigDecimal(pars[4].toString()).longValue()));
//                map.put("PAR_30_89", (pars[5] == null ? 0 : new BigDecimal(pars[5].toString()).longValue()));
//
//                map.put("PAR_90_179", (pars[6] == null ? 0 : new BigDecimal(pars[6].toString()).longValue()));
//                map.put("PAR_364", (pars[7] == null ? 0 : new BigDecimal(pars[7].toString()).longValue()));
//                map.put("PAR_365", (pars[8] == null ? 0 : new BigDecimal(pars[8].toString()).longValue()));
//                map.put("OUTSTANDING", (pars[9] == null ? 0 : new BigDecimal(pars[9].toString()).longValue()));
//
//                map.put("OD_CUST_1_4", (pars[10] == null ? 0 : new BigDecimal(pars[10].toString()).longValue()));
//                map.put("OD_CUST_5_15", (pars[11] == null ? 0 : new BigDecimal(pars[11].toString()).longValue()));
//                map.put("OD_CUST_16_29", (pars[12] == null ? 0 : new BigDecimal(pars[12].toString()).longValue()));
//                map.put("OD_CUST_30_89", (pars[13] == null ? 0 : new BigDecimal(pars[13].toString()).longValue()));
//
//                map.put("OD_CUST_90_179", (pars[14] == null ? 0 : new BigDecimal(pars[14].toString()).longValue()));
//                map.put("OD_CUST_364", (pars[15] == null ? 0 : new BigDecimal(pars[15].toString()).longValue()));
//                map.put("OD_CUST_365", (pars[16] == null ? 0 : new BigDecimal(pars[16].toString()).longValue()));
//                map.put("OD_TOTAL", (pars[17] == null ? 0 : new BigDecimal(pars[17].toString()).longValue()));
//
//                portfolioSummaryData.add(map);
//            });
//
//            if(portfolioSummaryData.size()>0) {
//            	params.put("datasetsummary", getJRDataSource(portfolioSummaryData));
//            }else {
//            	noDataProceed = true;
//            	SendErrorEmail("", "Portfolio Quality Report - Summary section" , "No Data Found : \n\n\n" + portfolioQltySummeryScript);
//            }

            String portQltyHdrScript;
            portQltyHdrScript = readFile(Charset.defaultCharset(), "portfolioQualityHdr.txt");
            List<Object[]> portHdrLists = entityManager.createNativeQuery(portQltyHdrScript).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> portHdrData = new ArrayList<>();

            portHdrLists.forEach(par -> {
                Map<String, Object> map = new HashMap<>();

                map.put("R_REGION_NAME", (par[0] == null ? "" : par[0].toString()));
                map.put("R_SR_NO", (par[1] == null ? 0 : new BigDecimal(par[1].toString()).longValue()));
                map.put("R_PRODUCT_ID", (par[2] == null ? "" : new BigDecimal(par[2].toString())));
                map.put("R_PRODUCT", (par[3] == null ? "" : par[3].toString()));

                map.put("R_ACTIVE_CLIENTS", (par[4] == null ? 0 : new BigDecimal(par[4].toString()).longValue()));
                map.put("R_DISB_AMOUNT", (par[5] == null ? 0 : new BigDecimal(par[5].toString()).longValue()));
                map.put("R_OS_AMOUNT", (par[6] == null ? 0 : new BigDecimal(par[6].toString()).longValue()));

                map.put("R_NO_OF_OD_LOANS", (par[7] == null ? 0 : new BigDecimal(par[7].toString()).longValue()));
                map.put("R_OVERDUE_PRINCIPAL_CLOSING", (par[8] == null ? 0 : new BigDecimal(par[8].toString()).longValue()));

                map.put("R_PAR_ABOVE_1", (par[9] == null ? 0 : new BigDecimal(par[9].toString()).longValue()));
//                map.put("R_PAR_ABOVE_1_PERC", (par[10] == null ? 0 : new BigDecimal(par[10].toString()).doubleValue()));

                map.put("R_OD_CUST_1_4", (par[10] == null ? 0 : new BigDecimal(par[10].toString()).longValue()));
                map.put("R_PAR_1_4", (par[11] == null ? 0 : new BigDecimal(par[11].toString()).longValue()));
//                map.put("R_PAR_ABOVE_1_4_PERC", (par[13] == null ? 0 : new BigDecimal(par[13].toString()).doubleValue()));

                map.put("R_OD_CUST_5_15", (par[12] == null ? 0 : new BigDecimal(par[12].toString()).longValue()));
                map.put("R_PAR_5_15", (par[13] == null ? 0 : new BigDecimal(par[13].toString()).longValue()));
//                map.put("R_PAR_ABOVE_5_15_PERC", (par[16] == null ? 0 : new BigDecimal(par[16].toString()).doubleValue()));

                map.put("R_OD_CUST_16_29", (par[14] == null ? 0 : new BigDecimal(par[14].toString()).longValue()));
                map.put("R_PAR_16_29", (par[15] == null ? 0 : new BigDecimal(par[15].toString()).longValue()));
//                map.put("R_PAR_ABOVE_16_29_PERC", (par[19] == null ? 0 : new BigDecimal(par[19].toString()).doubleValue()));

                map.put("R_OD_CUST_30_89", (par[16] == null ? 0 : new BigDecimal(par[16].toString()).longValue()));
                map.put("R_PAR_30_89", (par[17] == null ? 0 : new BigDecimal(par[17].toString()).longValue()));
//                map.put("R_PAR_ABOVE_30_89_PERC", (par[22] == null ? 0 : new BigDecimal(par[22].toString()).doubleValue()));

                map.put("R_OD_CUST_90_179", (par[18] == null ? 0 : new BigDecimal(par[18].toString()).longValue()));
                map.put("R_PAR_90_179", (par[19] == null ? 0 : new BigDecimal(par[19].toString()).longValue()));
//                map.put("R_PAR_ABOVE_90_179_PERC", (par[25] == null ? 0 : new BigDecimal(par[25].toString()).doubleValue()));

                map.put("R_OD_CUST_364", (par[20] == null ? 0 : new BigDecimal(par[20].toString()).longValue()));
                map.put("R_PAR_364", (par[21] == null ? 0 : new BigDecimal(par[21].toString()).longValue()));
//                map.put("R_PAR_364_PERC", (par[28] == null ? 0 : new BigDecimal(par[28].toString()).doubleValue()));

                map.put("R_OD_CUST_365", (par[22] == null ? 0 : new BigDecimal(par[22].toString()).longValue()));
                map.put("R_PAR_365", (par[23] == null ? 0 : new BigDecimal(par[23].toString()).longValue()));
//                map.put("R_PAR_ABOVE_365_PERC", (par[31] == null ? 0 : new BigDecimal(par[31].toString()).doubleValue()));

                portHdrData.add(map);
            });

            if (portHdrData.size() > 0) {
                params.put("datasethdr", getJRDataSource(portHdrData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "Portfolio Quality Report - Header section", "No Data Found : \n\n\n" + portQltyHdrScript);
            }


            String portQltyRegScript;
            portQltyRegScript = readFile(Charset.defaultCharset(), "portfolioQualityReg.txt");
            List<Object[]> portRegLists = entityManager.createNativeQuery(portQltyRegScript).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> portRegData = new ArrayList<>();

            portRegLists.forEach(par -> {
                Map<String, Object> map = new HashMap<>();

                map.put("R_REGION_NAME", (par[0] == null ? "" : par[0].toString()));
                map.put("R_ACTIVE_CLIENTS", (par[1] == null ? 0 : new BigDecimal(par[1].toString()).longValue()));
                map.put("R_DISB_AMOUNT", (par[2] == null ? 0 : new BigDecimal(par[2].toString()).longValue()));
                map.put("R_OS_AMOUNT", (par[3] == null ? 0 : new BigDecimal(par[3].toString()).longValue()));

                map.put("R_NO_OF_OD_LOANS", (par[4] == null ? 0 : new BigDecimal(par[4].toString()).longValue()));
                map.put("R_OVERDUE_PRINCIPAL_CLOSING", (par[5] == null ? 0 : new BigDecimal(par[5].toString()).longValue()));

                map.put("R_PAR_ABOVE_1", (par[6] == null ? 0 : new BigDecimal(par[6].toString()).longValue()));

                map.put("R_OD_CUST_1_4", (par[7] == null ? 0 : new BigDecimal(par[7].toString()).longValue()));
                map.put("R_PAR_1_4", (par[8] == null ? 0 : new BigDecimal(par[8].toString()).longValue()));

                map.put("R_OD_CUST_5_15", (par[9] == null ? 0 : new BigDecimal(par[9].toString()).longValue()));
                map.put("R_PAR_5_15", (par[10] == null ? 0 : new BigDecimal(par[10].toString()).longValue()));

                map.put("R_OD_CUST_16_29", (par[11] == null ? 0 : new BigDecimal(par[11].toString()).longValue()));
                map.put("R_PAR_16_29", (par[12] == null ? 0 : new BigDecimal(par[12].toString()).longValue()));

                map.put("R_OD_CUST_30_89", (par[13] == null ? 0 : new BigDecimal(par[13].toString()).longValue()));
                map.put("R_PAR_30_89", (par[14] == null ? 0 : new BigDecimal(par[14].toString()).longValue()));

                map.put("R_OD_CUST_90_179", (par[15] == null ? 0 : new BigDecimal(par[15].toString()).longValue()));
                map.put("R_PAR_90_179", (par[16] == null ? 0 : new BigDecimal(par[16].toString()).longValue()));

                map.put("R_OD_CUST_364", (par[17] == null ? 0 : new BigDecimal(par[17].toString()).longValue()));
                map.put("R_PAR_364", (par[18] == null ? 0 : new BigDecimal(par[18].toString()).longValue()));

                map.put("R_OD_CUST_365", (par[19] == null ? 0 : new BigDecimal(par[19].toString()).longValue()));
                map.put("R_PAR_365", (par[20] == null ? 0 : new BigDecimal(par[20].toString()).longValue()));

                portRegData.add(map);
            });

            if (portRegData.size() > 0) {
                params.put("datasetReg", getJRDataSource(portRegData));
            } else {
                noDataProceed = true;
                //  SendErrorEmail("", "Portfolio Quality Report - Region section" , "No Data Found : \n\n\n"+portQltyHdrScript);
            }


            ///KKK
            String kkkChartScript;
            kkkChartScript = readFile(Charset.defaultCharset(), "portfolioQualitykkkchart.txt");
            List<Object[]> kkkChart = entityManager.createNativeQuery(kkkChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kkkChartData = new ArrayList<>();

            kkkChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kkkChartData.add(map);
                params.put("MIN_KKK", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KKK", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());

            });

            if (kkkChartData.size() > 0) {
                params.put("kkkchart", getJRDataSource(kkkChartData));
            } else {
                //	SendErrorEmail("", "Portfolio Quality Report - kkkChartData" , "No Data Found : \n\n\n"+kkkChartScript);
            }


            ///KSS
            String kssChartScript;
            kssChartScript = readFile(Charset.defaultCharset(), "portfolioQualityksschart.txt");
            List<Object[]> kssChart = entityManager.createNativeQuery(kssChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kssChartData = new ArrayList<>();

            kssChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kssChartData.add(map);
                params.put("MIN_KSS", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KSS", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kssChartData.size() > 0) {
                params.put("ksschart", getJRDataSource(kssChartData));
            } else {
                //SendErrorEmail("", "Portfolio Quality Report - kssChartData" , "No Data Found : \n\n\n"+kssChartScript);
            }

            ///KM
            String kmChartScript;
            kmChartScript = readFile(Charset.defaultCharset(), "portfolioQualitykmchart.txt");
            List<Object[]> kmChart = entityManager.createNativeQuery(kmChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kmChartData = new ArrayList<>();

            kmChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kmChartData.add(map);
                params.put("MIN_KM", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KM", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kmChartData.size() > 0) {
                params.put("kmchart", getJRDataSource(kmChartData));
            } else {
                //	SendErrorEmail("", "Portfolio Quality Report - kmChartData" , "No Data Found : \n\n\n"+kmChartScript);
            }

            //KEL
            String kelChartScript;
            kelChartScript = readFile(Charset.defaultCharset(), "portfolioQualitykelchart.txt");
            List<Object[]> kelChart = entityManager.createNativeQuery(kelChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kelChartData = new ArrayList<>();

            kelChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kelChartData.add(map);
                params.put("MIN_KEL", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KEL", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kelChartData.size() > 0) {
                params.put("kelchart", getJRDataSource(kelChartData));
            } else {
                //	SendErrorEmail("", "Portfolio Quality Report - kelChartData" , "No Data Found : \n\n\n"+kelChartScript);
            }

            //KSK
            String kskChartScript;
            kskChartScript = readFile(Charset.defaultCharset(), "portfolioQualitykskchart.txt");
            List<Object[]> kskChart = entityManager.createNativeQuery(kskChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kskChartData = new ArrayList<>();

            kskChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kskChartData.add(map);
                params.put("MIN_KSK", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KSK", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kskChartData.size() > 0) {
                params.put("kskchart", getJRDataSource(kskChartData));
            } else {
                //	SendErrorEmail("", "Portfolio Quality Report - kskChartData" , "No Data Found : \n\n\n"+kskChartScript);
            }

            //KMWK
            String kmwChartScript;
            kmwChartScript = readFile(Charset.defaultCharset(), "portfolioQualitykmwchart.txt");
            List<Object[]> kmwChart = entityManager.createNativeQuery(kmwChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kmwChartData = new ArrayList<>();

            kmwChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kmwChartData.add(map);
                params.put("MIN_KMW", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KMW", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kmwChartData.size() > 0) {
                params.put("kmwchart", getJRDataSource(kmwChartData));
            } else {
                //SendErrorEmail("", "Portfolio Quality Report - kmwChartData" , "No Data Found : \n\n\n"+kmwChartScript);
            }

            //KBK
            String kbkChartScript;
            kbkChartScript = readFile(Charset.defaultCharset(), "portfolioQualitykbkchart.txt");
            List<Object[]> kbkChart = entityManager.createNativeQuery(kbkChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kbkChartData = new ArrayList<>();

            kbkChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kbkChartData.add(map);
                params.put("MIN_KBK", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KBK", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kbkChartData.size() > 0) {
                params.put("kbkchart", getJRDataSource(kbkChartData));
            } else {
                //	SendErrorEmail("", "Portfolio Quality Report - kbkChartData" , "No Data Found : \n\n\n"+kbkChartScript);
            }


            //HIL
            String hilScript;
            hilScript = readFile(Charset.defaultCharset(), "portfolioQualityhilchart.txt");
            List<Object[]> hilChart = entityManager.createNativeQuery(hilScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> hilChartData = new ArrayList<>();

            hilChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                hilChartData.add(map);
                params.put("MIN_HIL", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_HIL", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (hilChartData.size() > 0) {
                params.put("hilchart", getJRDataSource(hilChartData));
            } else {
                //   SendErrorEmail("", "Portfolio Quality Report - HilChartData" , "No Data Found : \n\n\n"+kbkChartScript);
            }

            //KSWK
            String kswkScript;
            kswkScript = readFile(Charset.defaultCharset(), "portfolioQualitykswkchart.txt");
            List<Object[]> kswkChart = entityManager.createNativeQuery(kswkScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kswkChartData = new ArrayList<>();

            kswkChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kswkChartData.add(map);
                params.put("MIN_KSWK", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KSWK", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kswkChartData.size() > 0) {
                params.put("kswkchart", getJRDataSource(kswkChartData));
            } else {
                //   SendErrorEmail("", "Portfolio Quality Report - KswkChartData" , "No Data Found : \n\n\n"+kbkChartScript);
            }

            //KSWK
            String kfkScript;
            kfkScript = readFile(Charset.defaultCharset(), "portfolioQualitykfkchart.txt");
            List<Object[]> kfkChart = entityManager.createNativeQuery(kfkScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> kfkChartData = new ArrayList<>();

            kfkChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("PRODUCT_ID", (data[2] == null ? 0 : new BigDecimal(data[2].toString()).longValue()));

                kfkChartData.add(map);
                params.put("MIN_KSWK", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
                params.put("MAX_KSWK", data[4] == null ? 0 : new BigDecimal(data[4].toString()).doubleValue());
            });

            if (kfkChartData.size() > 0) {
                params.put("kfkchart", getJRDataSource(kfkChartData));
            } else {
                //   SendErrorEmail("", "Portfolio Quality Report - KswkChartData" , "No Data Found : \n\n\n"+kbkChartScript);
            }

            //all-chart
            String allChartScript;
            allChartScript = readFile(Charset.defaultCharset(), "portfolioQualityallchart.txt");
            List<Object[]> allChart = entityManager.createNativeQuery(allChartScript).setParameter("asOfDate", asOfDate).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> allChartData = new ArrayList<>();

            allChart.forEach(data -> {
                Map<String, Object> map = new HashMap<>();
                map.put("PAR", (data[0] == null ? 0 : new BigDecimal(data[0].toString()).doubleValue()));
                map.put("PAR_DATE", (data[1] == null ? "" : data[1].toString()));
                map.put("ACTIVE_STATUS", "1");

                allChartData.add(map);
                params.put("MIN_ALL", data[2] == null ? 0 : new BigDecimal(data[2].toString()).doubleValue());
                params.put("MAX_ALL", data[3] == null ? 0 : new BigDecimal(data[3].toString()).doubleValue());
            });

            if (allChartData.size() > 0) {
                params.put("allchart", getJRDataSource(allChartData));
            } else {
                SendErrorEmail("", "Portfolio Quality Report - allChartData", "No Data Found : \n\n\n" + allChartScript);
            }

            String portQltyDtlScript;
            portQltyDtlScript = readFile(Charset.defaultCharset(), "portfolioQualityDtl.txt");
            List<Object[]> portDtlLists = entityManager.createNativeQuery(portQltyDtlScript).setParameter("p_type", type).getResultList();

            List<Map<String, ?>> portDtlData = new ArrayList<>();

            portDtlLists.forEach(par -> {
                Map<String, Object> map = new HashMap<>();

                map.put("REGION_NAME", (par[0] == null ? "" : par[0].toString()));
                map.put("AREA_NAME", (par[1] == null ? "" : par[1].toString()));
                map.put("BRANCH", (par[2] == null ? "" : par[2].toString()));
                map.put("PRODUCT_ID", (par[3] == null ? 0 : new BigDecimal(par[3].toString()).longValue()));
                map.put("PRODUCT", (par[4] == null ? "" : par[4].toString()));

                map.put("ACTIVE_CLIENTS", (par[5] == null ? 0 : new BigDecimal(par[5].toString()).longValue()));
                map.put("DISB_AMOUNT", (par[6] == null ? 0 : new BigDecimal(par[6].toString()).longValue()));
                map.put("OS_AMOUNT", (par[7] == null ? 0 : new BigDecimal(par[7].toString()).longValue()));
                map.put("NO_OF_OD_LOANS", (par[8] == null ? 0 : new BigDecimal(par[8].toString()).longValue()));
                map.put("OVERDUE_PRINCIPAL_CLOSING", (par[9] == null ? 0 : new BigDecimal(par[9].toString()).longValue()));
                map.put("PAR_ABOVE_1", (par[10] == null ? 0 : new BigDecimal(par[10].toString()).longValue()));
                map.put("OD_CUST_1_4", (par[11] == null ? 0 : new BigDecimal(par[11].toString()).longValue()));
                map.put("PAR_1_4", (par[12] == null ? 0 : new BigDecimal(par[12].toString()).longValue()));
                map.put("OD_CUST_5_15", (par[13] == null ? 0 : new BigDecimal(par[13].toString()).longValue()));
                map.put("PAR_5_15", (par[14] == null ? 0 : new BigDecimal(par[14].toString()).longValue()));
                map.put("OD_CUST_16_29", (par[15] == null ? 0 : new BigDecimal(par[15].toString()).longValue()));
                map.put("PAR_16_29", (par[16] == null ? 0 : new BigDecimal(par[16].toString()).longValue()));
                map.put("OD_CUST_30_89", (par[17] == null ? 0 : new BigDecimal(par[17].toString()).longValue()));
                map.put("PAR_30_89", (par[18] == null ? 0 : new BigDecimal(par[18].toString()).longValue()));
                map.put("OD_CUST_90_179", (par[19] == null ? 0 : new BigDecimal(par[19].toString()).longValue()));
                map.put("PAR_90_179", (par[20] == null ? 0 : new BigDecimal(par[20].toString()).longValue()));
                map.put("OD_CUST_364", (par[21] == null ? 0 : new BigDecimal(par[21].toString()).longValue()));
                map.put("PAR_364", (par[22] == null ? 0 : new BigDecimal(par[22].toString()).longValue()));
                map.put("OD_CUST_365", (par[23] == null ? 0 : new BigDecimal(par[23].toString()).longValue()));
                map.put("PAR_365", (par[24] == null ? 0 : new BigDecimal(par[24].toString()).longValue()));

                map.put("PAR_1_4_PERC", (par[25] == null ? 0 : new BigDecimal(par[25].toString()).doubleValue()));
                map.put("PAR_5_15_PERC", (par[26] == null ? 0 : new BigDecimal(par[26].toString()).doubleValue()));
                map.put("PAR_16_29_PERC", (par[27] == null ? 0 : new BigDecimal(par[27].toString()).doubleValue()));
                map.put("PAR_30_89_PERC", (par[28] == null ? 0 : new BigDecimal(par[28].toString()).doubleValue()));
                map.put("PAR_90_179_PERC", (par[29] == null ? 0 : new BigDecimal(par[29].toString()).doubleValue()));
                map.put("PAR_364_PERC", (par[30] == null ? 0 : new BigDecimal(par[30].toString()).doubleValue()));
                map.put("PAR_365_PERC", (par[31] == null ? 0 : new BigDecimal(par[31].toString()).doubleValue()));

                portDtlData.add(map);

            });

            if (portDtlData.size() <= 0) {
                noDataProceed = true;
                SendErrorEmail("", "Portfolio Quality Report - portDtlData", "No Data Found : \n\n\n" + portQltyDtlScript);
            }

            if (!noDataProceed) {
                generateMwxReport(PORTFOLIO_QUALITY, params, getJRDataSource(portDtlData), type + "-" + reportName + todayDate);
                generatedReports.add(type + "" + reportName + todayDate);
            }

        } catch (Exception exp) {
            SendErrorEmail("", type + "-" + reportName + todayDate, ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, type + "" + reportName + todayDate);
        }
    }

    private void getTargetVsAchievements(String user, Map<String, Object> params, String asOfDate, String todayDate) {

        try {

            boolean noDataProceed = false;

            String targetSummeryScript;
            targetSummeryScript = readFile(Charset.defaultCharset(), "targetVsAchievementsSummery.txt");
            List<Object[]> summeryLists = entityManager.createNativeQuery(targetSummeryScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> summeryListsData1 = new ArrayList<>();
            List<Map<String, ?>> summeryListsData2 = new ArrayList<>();

            summeryLists.forEach(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("PRODUCT_F", (record[0] == null ? "" : record[0].toString()));
                map.put("PRODUCT_TYPE", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("TARGET_F", (record[2] == null ? 0 : new BigDecimal(record[2].toString()).longValue()));
                map.put("ACHIEVEMENT_F", (record[3] == null ? 0 : new BigDecimal(record[3].toString()).longValue()));
                map.put("P_TAG_F", (record[4] == null ? 0 : new BigDecimal(record[4].toString()).doubleValue()));
                map.put("VAR_F", (record[5] == null ? 0 : new BigDecimal(record[5].toString()).longValue()));
                map.put("PRODUCT_CODE_SUMMARY", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_F", (record[7] == null ? 0 : new BigDecimal(record[7].toString()).longValue()));
                map.put("PRODUCT_CODE_F", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));

                if (record[1].toString().equals("2"))
                    summeryListsData2.add(map);
                else
                    summeryListsData1.add(map);
            });

            if (summeryListsData1.size() > 0 && summeryListsData2.size() > 0) {
                params.put("prdYrSmry1", getJRDataSource(summeryListsData1));
                params.put("prdYrSmry2", getJRDataSource(summeryListsData2));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getTargetVsAchievements-" + todayDate, "No Data Found : " + targetSummeryScript);
            }

            String targetRegionOutreachScript;
            targetRegionOutreachScript = readFile(Charset.defaultCharset(), "targetVsAchievementsRegionOutreach.txt");
            List<Object[]> regionOutreach = entityManager.createNativeQuery(targetRegionOutreachScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> regionOutreachData = new ArrayList<>();

            regionOutreach.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("REGION_NAME_AMT", (record[0] == null ? "" : record[0].toString()));
                map.put("OPENING", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("TARGETS", (record[2] == null ? 0 : new BigDecimal(record[2].toString()).longValue()));
                map.put("MATURING_LOANS", (record[3] == null ? 0 : new BigDecimal(record[3].toString()).longValue()));
                map.put("CLOSING", (record[4] == null ? 0 : new BigDecimal(record[4].toString()).longValue()));
                map.put("ACHIEVMENT_NO", (record[5] == null ? 0 : new BigDecimal(record[5].toString()).longValue()));
                map.put("DIFF", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));

                regionOutreachData.add(map);
            });

            if (regionOutreachData.size() > 0) {
                params.put("rgOutrch", getJRDataSource(regionOutreachData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getTargetVsAchievements- regionOutreachData", "No Data Found : " + targetRegionOutreachScript);
            }

            String targetQuaterlyScript;
            targetQuaterlyScript = readFile(Charset.defaultCharset(), "targetVsAchievementsQuarterlySummary.txt");
            List<Object[]> targetQuaterList = entityManager.createNativeQuery(targetQuaterlyScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetQuaterData1 = new ArrayList<>();
            List<Map<String, ?>> targetQuaterData2 = new ArrayList<>();

            targetQuaterList.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PRODUCT_P", (record[0] == null ? "" : record[0].toString()));
                map.put("PRODUCT_CODE_REGION_P", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));

                map.put("PAR_DATE_REGION_P1", (record[2] == null ? "" : record[2].toString()));
                map.put("PAR_DATE_REGION_P2", (record[3] == null ? "" : record[3].toString()));
                map.put("PAR_DATE_REGION_P3", (record[4] == null ? "" : record[4].toString()));

                map.put("TARGET_REGION_P1", (record[5] == null ? 0 : new BigDecimal(record[5].toString()).longValue()));
                map.put("TARGET_REGION_P2", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));
                map.put("TARGET_REGION_P3", (record[7] == null ? 0 : new BigDecimal(record[7].toString()).longValue()));

                map.put("ACHIEVEMENT_REGION_P1", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));
                map.put("ACHIEVEMENT_REGION_P2", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));
                map.put("ACHIEVEMENT_REGION_P3", (record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS_REGION1", (record[11] == null ? 0 : new BigDecimal(record[11].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION2", (record[12] == null ? 0 : new BigDecimal(record[12].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION3", (record[13] == null ? 0 : new BigDecimal(record[13].toString()).longValue()));

                if (record[1].toString().equals("19"))
                    targetQuaterData2.add(map);
                else
                    targetQuaterData1.add(map);
            });

            if (targetQuaterData1.size() > 0 && targetQuaterData2.size() > 0) {
                params.put("prdQtrSmry1", getJRDataSource(targetQuaterData1));
                params.put("prdQtrSmry2", getJRDataSource(targetQuaterData2));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getTargetVsAchievements- targetQuaterData1", "No Data Found : " + targetQuaterlyScript);
            }

            String targetHdrScript;
            targetHdrScript = readFile(Charset.defaultCharset(), "targetVsAchievementsHeaderSummary.txt");
            List<Object[]> targetHdrlist = entityManager.createNativeQuery(targetHdrScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetHdrData = new ArrayList<>();

            targetHdrlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PRODUCT", (record[0] == null ? "" : record[0].toString()));
                map.put("REGION", (record[1] == null ? "" : record[1].toString()));
                map.put("PRODUCT_CODE_REGION", (record[2] == null ? 0 : new BigDecimal(record[2].toString()).longValue()));

                map.put("PAR_DATE_REGION1", (record[3] == null ? "" : record[3].toString()));
                map.put("PAR_DATE_REGION2", (record[4] == null ? "" : record[4].toString()));
                map.put("PAR_DATE_REGION3", (record[5] == null ? "" : record[5].toString()));

                map.put("TARGET_REGION1", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));
                map.put("TARGET_REGION2", (record[7] == null ? 0 : new BigDecimal(record[7].toString()).longValue()));
                map.put("TARGET_REGION3", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));

                map.put("ACHIEVEMENT_REGION1", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));
                map.put("ACHIEVEMENT_REGION2", (record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue()));
                map.put("ACHIEVEMENT_REGION3", (record[11] == null ? 0 : new BigDecimal(record[11].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS_REGION1", (record[12] == null ? 0 : new BigDecimal(record[12].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION2", (record[13] == null ? 0 : new BigDecimal(record[13].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION3", (record[14] == null ? 0 : new BigDecimal(record[14].toString()).longValue()));

                targetHdrData.add(map);
            });


            if (targetHdrData.size() > 0) {
                params.put("rgPrdHdrSmry", getJRDataSource(targetHdrData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getTargetVsAchievements- targetHdrData", "No Data Found : \n\n\n" + targetHdrScript);
            }

            String targetDetailScript;
            targetDetailScript = readFile(Charset.defaultCharset(), "targetVsAchievementsDetail.txt");
            List<Object[]> targetDetaillist = entityManager.createNativeQuery(targetDetailScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetDetailData = new ArrayList<>();

            targetDetaillist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PRD_GRP_NM", (record[0] == null ? "" : record[0].toString()));
                map.put("REGION_NAME", (record[1] == null ? "" : record[1].toString()));
                map.put("AREA_NAME", (record[2] == null ? "" : record[2].toString()));
                map.put("BRANCH_NAME", (record[3] == null ? "" : record[3].toString()));
                map.put("PRODUCT_CODE", (record[4] == null ? 0 : new BigDecimal(record[4].toString()).longValue()));

                map.put("PAR_DATE1", (record[5] == null ? "" : record[5].toString()));
                map.put("PAR_DATE2", (record[6] == null ? "" : record[6].toString()));
                map.put("PAR_DATE3", (record[7] == null ? "" : record[7].toString()));

                map.put("TARGET1", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));
                map.put("TARGET2", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));
                map.put("TARGET3", (record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue()));

                map.put("ACHIEVEMENT1", (record[11] == null ? 0 : new BigDecimal(record[11].toString()).longValue()));
                map.put("ACHIEVEMENT2", (record[12] == null ? 0 : new BigDecimal(record[12].toString()).longValue()));
                map.put("ACHIEVEMENT3", (record[13] == null ? 0 : new BigDecimal(record[13].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS1", (record[14] == null ? 0 : new BigDecimal(record[14].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS2", (record[15] == null ? 0 : new BigDecimal(record[15].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS3", (record[16] == null ? 0 : new BigDecimal(record[16].toString()).longValue()));

                targetDetailData.add(map);
            });

            if (targetDetailData.size() > 0) {
                params.put("rgPrdDtlSmry", getJRDataSource(targetDetailData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getTargetVsAchievements- targetDetailData", "No Data Found : \n\n\n" + targetDetailScript);
            }

            String consoGrphScript;
            consoGrphScript = readFile(Charset.defaultCharset(), "targetVsAchievementsConsolidatedGrph.txt");
            List<Object[]> conGrphlist = entityManager.createNativeQuery(consoGrphScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> conGrphData = new ArrayList<>();

            conGrphlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("TARGET_REGION_GR1_", (record[0] == null ? 0 : new BigDecimal(record[0].toString()).longValue()));
                map.put("FLAG_GR1_", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("MONTH_GR1_", (record[2] == null ? "" : record[2].toString()));

                conGrphData.add(map);
            });

            if (conGrphData.size() > 0) {
                params.put("grpcon", getJRDataSource(conGrphData));
            } else {
                SendErrorEmail("", "getTargetVsAchievements- conGrphData", "No Data Found : \n\n\n" + consoGrphScript);
            }

            if (!conGrphData.isEmpty()) {
                String consoGrphScriptMax;
                consoGrphScriptMax = readFile(Charset.defaultCharset(), "targetVsAchievementsConsolidatedGrphMAX.txt");
                Object conGrphlistMax = entityManager.createNativeQuery(consoGrphScriptMax).setParameter("asOfDate", asOfDate).getSingleResult();

                if (conGrphlistMax != null) {
                    params.put("MAX_VAL_CONSO", new BigDecimal(conGrphlistMax.toString()).intValue());
                } else {
                    params.put("MAX_VAL_CONSO", 0);
                }
            }

            String prdGrphScript;
            prdGrphScript = readFile(Charset.defaultCharset(), "targetVsAchievementsProductGrph.txt");
            List<Object[]> prdGrphlist = entityManager.createNativeQuery(prdGrphScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> prdGrphData = new ArrayList<>();

            prdGrphlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("TARGET_PROD", (record[0] == null ? 0 : new BigDecimal(record[0].toString()).longValue()));
                map.put("FLAG_PROD", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("MON_PROD", (record[2] == null ? "" : record[2].toString()));
                map.put("PROD", (record[3] == null ? "" : record[3].toString()));

                prdGrphData.add(map);
            });

            if (prdGrphData.size() > 0) {
                params.put("grpprd", getJRDataSource(prdGrphData));
            } else {
                SendErrorEmail("", "getTargetVsAchievements- prdGrphData", "No Data Found : \n\n\n" + prdGrphScript);
            }

            if (!prdGrphData.isEmpty()) {
                String prdGrphScriptMax;
                prdGrphScriptMax = readFile(Charset.defaultCharset(), "targetVsAchievementsProductGrphMAX.txt");
                Object prdGrphlistMax = entityManager.createNativeQuery(prdGrphScriptMax).setParameter("asOfDate", asOfDate).getSingleResult();

                if (prdGrphlistMax != null) {
                    params.put("MAX_VAL_PRD", new BigDecimal(prdGrphlistMax.toString()).intValue());
                } else {
                    params.put("MAX_VAL_PRD", 0);
                }
            }

            String targetRegionScript;
            targetRegionScript = readFile(Charset.defaultCharset(), "targetVsAchievementsRegionSummery.txt");
            List<Object[]> targetRegionlist = entityManager.createNativeQuery(targetRegionScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetRegionData = new ArrayList<>();

            targetRegionlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("REGION", (record[0] == null ? "" : record[0].toString()));

                map.put("PAR_DATE_REGION1", (record[1] == null ? "" : record[1].toString()));
                map.put("PAR_DATE_REGION2", (record[2] == null ? "" : record[2].toString()));
                map.put("PAR_DATE_REGION3", (record[3] == null ? "" : record[3].toString()));

                map.put("TARGET_REGION1", (record[4] == null ? 0 : new BigDecimal(record[4].toString()).longValue()));
                map.put("TARGET_REGION2", (record[5] == null ? 0 : new BigDecimal(record[5].toString()).longValue()));
                map.put("TARGET_REGION3", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));

                map.put("ACHIEVEMENT_REGION1", (record[7] == null ? 0 : new BigDecimal(record[7].toString()).longValue()));
                map.put("ACHIEVEMENT_REGION2", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));
                map.put("ACHIEVEMENT_REGION3", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS_REGION1", (record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION2", (record[11] == null ? 0 : new BigDecimal(record[11].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION3", (record[12] == null ? 0 : new BigDecimal(record[12].toString()).longValue()));

                targetRegionData.add(map);
            });


            if (targetRegionData.size() > 0) {
                params.put("rgPrdRegionSmry", getJRDataSource(targetRegionData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getTargetVsAchievements- rgPrdRegionSmry", "No Data Found : \n\n\n" + targetHdrScript);
            }

            if (!noDataProceed) {
                generateMwxReport(TARGET_ACHIEVEMENTS, params, null, "4-Consolidated_TargetAchievements-" + todayDate);
                generatedReports.add("4-Consolidated_TargetAchievements-" + todayDate);
            }

        } catch (Exception exp) {
            SendErrorEmail("", "4-Consolidated_TargetAchievements-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "4-Consolidated_TargetAchievements-" + todayDate);
        }
    }

    private void getAmountTargetVsAchievements(String user, Map<String, Object> params, Object asOfDate, String todayDate) {

        try {

            boolean noDataProceed = false;

            String targetSummeryScript;
            targetSummeryScript = readFile(Charset.defaultCharset(), "amountAchievementsSummery.txt");
            List<Object[]> summeryLists = entityManager.createNativeQuery(targetSummeryScript).getResultList();

            List<Map<String, ?>> summeryListsData1 = new ArrayList<>();
            List<Map<String, ?>> summeryListsData2 = new ArrayList<>();

            summeryLists.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PRODUCT_F", (record[0] == null ? "" : record[0].toString()));
                map.put("TARGET_CLTS_F", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_F", (record[2] == null ? 0 : new BigDecimal(record[2].toString()).longValue()));
                map.put("P_TAG_CLTS_F", (record[3] == null ? 0 : new BigDecimal(record[3].toString()).doubleValue()));
                map.put("VAR_CLTS_F", (record[4] == null ? 0 : new BigDecimal(record[4].toString()).longValue()));
                map.put("PRODUCT_CODE_F", (record[5] == null ? 0 : new BigDecimal(record[5].toString()).longValue()));

                if (record[5].toString().equals("19"))
                    summeryListsData2.add(map);
                else
                    summeryListsData1.add(map);

            });

            if (summeryListsData1.size() > 0 && summeryListsData2.size() > 0) {
                params.put("prdYrSmry1", getJRDataSource(summeryListsData1));
                params.put("prdYrSmry2", getJRDataSource(summeryListsData2));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getAmountTargetVsAchievements- targetSummeryScript", "No Data Found : " + targetSummeryScript);
            }

            String targetRegionOutreachScript;
            targetRegionOutreachScript = readFile(Charset.defaultCharset(), "amountAchievementsRegionOutreach.txt");
            List<Object[]> regionOutreach = entityManager.createNativeQuery(targetRegionOutreachScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> regionOutreachData = new ArrayList<>();

            regionOutreach.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("REGION_NAME_AMT", (record[0] == null ? "" : record[0].toString()));
                map.put("ACHIEVMENT_NO", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("ACHIEVMENT_AMT", (record[2] == null ? 0 : new BigDecimal(record[2].toString()).longValue()));

                regionOutreachData.add(map);
            });

            if (regionOutreachData.size() > 0) {
                params.put("rgOutrch", getJRDataSource(regionOutreachData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getAmountTargetVsAchievements- regionOutreachData", "No Data Found : " + targetRegionOutreachScript);
            }

            String targetQuaterlyScript;
            targetQuaterlyScript = readFile(Charset.defaultCharset(), "amountAchievementsQuarterlySummary.txt");
            List<Object[]> targetQuaterList = entityManager.createNativeQuery(targetQuaterlyScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetQuaterData1 = new ArrayList<>();
            List<Map<String, ?>> targetQuaterData2 = new ArrayList<>();

            targetQuaterList.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PRODUCT_P", (record[0] == null ? "" : record[0].toString()));
                map.put("PRODUCT_CODE_REGION_P", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));

                map.put("PAR_DATE_REGION_P1", (record[2] == null ? "" : record[2].toString()));
                map.put("PAR_DATE_REGION_P2", (record[3] == null ? "" : record[3].toString()));
                map.put("PAR_DATE_REGION_P3", (record[4] == null ? "" : record[4].toString()));

                map.put("TARGET_CLTS_REGION_P_P1", (record[5] == null ? 0 : new BigDecimal(record[5].toString()).longValue()));
                map.put("TARGET_CLTS_REGION_P_P2", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));
                map.put("TARGET_CLTS_REGION_P_P3", (record[7] == null ? 0 : new BigDecimal(record[7].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS_REGION1", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION2", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION3", (record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue()));

                if (record[1].toString().equals("19"))
                    targetQuaterData2.add(map);
                else
                    targetQuaterData1.add(map);
            });

            if (targetQuaterData1.size() > 0 && targetQuaterData2.size() > 0) {
                params.put("prdQtrSmry1", getJRDataSource(targetQuaterData1));
                params.put("prdQtrSmry2", getJRDataSource(targetQuaterData2));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getAmountTargetVsAchievements- targetQuaterlyScript", "No Data Found : " + targetQuaterlyScript);
            }


            String targetHdrScript;
            targetHdrScript = readFile(Charset.defaultCharset(), "amountAchievementsHeaderSummary.txt");
            List<Object[]> targetHdrlist = entityManager.createNativeQuery(targetHdrScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetHdrData = new ArrayList<>();

            targetHdrlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PRODUCT", (record[0] == null ? "" : record[0].toString()));
                map.put("REGION", (record[1] == null ? "" : record[1].toString()));
                map.put("PRODUCT_CODE_REGION", (record[2] == null ? 0 : new BigDecimal(record[2].toString()).longValue()));

                map.put("PAR_DATE_REGION1", (record[3] == null ? "" : record[3].toString()));
                map.put("PAR_DATE_REGION2", (record[4] == null ? "" : record[4].toString()));
                map.put("PAR_DATE_REGION3", (record[5] == null ? "" : record[5].toString()));

                map.put("TARGET_CLTS_REGION1", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));
                map.put("TARGET_CLTS_REGION2", (record[7] == null ? 0 : new BigDecimal(record[7].toString()).longValue()));
                map.put("TARGET_CLTS_REGION3", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS_REGION1", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION2", (record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION3", (record[11] == null ? 0 : new BigDecimal(record[11].toString()).longValue()));

                targetHdrData.add(map);
            });

            if (targetHdrData.size() > 0) {
                params.put("rgPrdHdrSmry", getJRDataSource(targetHdrData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getAmountTargetVsAchievements- targetHdrData", "No Data Found : " + targetHdrScript);
            }

            String targetDetailScript;
            targetDetailScript = readFile(Charset.defaultCharset(), "amountAchievementsDetail.txt");
            List<Object[]> targetDetaillist = entityManager.createNativeQuery(targetDetailScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetDetailData = new ArrayList<>();

            targetDetaillist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("PRD_GRP_NM", (record[0] == null ? "" : record[0].toString()));
                map.put("REGION_NAME", (record[1] == null ? "" : record[1].toString()));
                map.put("AREA_NAME", (record[2] == null ? "" : record[2].toString()));
                map.put("BRANCH_NAME", (record[3] == null ? "" : record[3].toString()));
                map.put("PRODUCT_CODE", (record[4] == null ? 0 : new BigDecimal(record[4].toString()).longValue()));

                map.put("PAR_DATE1", (record[5] == null ? "" : record[5].toString()));
                map.put("PAR_DATE2", (record[6] == null ? "" : record[6].toString()));
                map.put("PAR_DATE3", (record[7] == null ? "" : record[7].toString()));

                map.put("TARGET_CLT1", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));
                map.put("TARGET_CLT2", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));
                map.put("TARGET_CLT3", (record[10] == null ? 0 : new BigDecimal(record[10].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS1", (record[11] == null ? 0 : new BigDecimal(record[11].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS2", (record[12] == null ? 0 : new BigDecimal(record[12].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS3", (record[13] == null ? 0 : new BigDecimal(record[13].toString()).longValue()));

                targetDetailData.add(map);
            });

            if (targetDetailData.size() > 0) {
                params.put("rgPrdDtlSmry", getJRDataSource(targetDetailData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getAmountTargetVsAchievements- targetDetailData", "No Data Found : " + targetDetailScript);
            }

            String consoGrphScript;
            consoGrphScript = readFile(Charset.defaultCharset(), "amountAchievementsConsolidatedGrph.txt");
            List<Object[]> conGrphlist = entityManager.createNativeQuery(consoGrphScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> conGrphData = new ArrayList<>();

            conGrphlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("TARGET_REGION_GR1_", (record[0] == null ? 0 : new BigDecimal(record[0].toString()).longValue()));
                map.put("FLAG_GR1_", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("MONTH_GR1_", (record[2] == null ? "" : record[2].toString()));

                conGrphData.add(map);
            });

            if (conGrphData.size() > 0) {
                params.put("grpcon", getJRDataSource(conGrphData));
            } else {
                SendErrorEmail("", "getAmountTargetVsAchievements- conGrphData", "No Data Found : " + consoGrphScript);
            }

            if (!conGrphData.isEmpty()) {
                String consoGrphScriptMax;
                consoGrphScriptMax = readFile(Charset.defaultCharset(), "amountAchievementsConsolidatedGrphMAX.txt");
                Object conGrphlistMax = entityManager.createNativeQuery(consoGrphScriptMax).setParameter("asOfDate", asOfDate).getSingleResult();

                if (conGrphlistMax != null) {
                    params.put("MAX_VAL_CONSO", new BigDecimal(conGrphlistMax.toString()).intValue());
                } else {
                    params.put("MAX_VAL_CONSO", 0);
                }
            }

            String prdGrphScript;
            prdGrphScript = readFile(Charset.defaultCharset(), "amountAchievementsProductGrph.txt");
            List<Object[]> prdGrphlist = entityManager.createNativeQuery(prdGrphScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> prdGrphData = new ArrayList<>();

            prdGrphlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("TARGET_PROD", (record[0] == null ? 0 : new BigDecimal(record[0].toString()).longValue()));
                map.put("FLAG_PROD", (record[1] == null ? 0 : new BigDecimal(record[1].toString()).longValue()));
                map.put("MON_PROD", (record[2] == null ? "" : record[2].toString()));
                map.put("PROD", (record[3] == null ? "" : record[3].toString()));

                prdGrphData.add(map);
            });

            if (prdGrphData.size() > 0) {
                params.put("grpprd", getJRDataSource(prdGrphData));
            } else {
                SendErrorEmail("", "getAmountTargetVsAchievements- prdGrphData", "No Data Found : " + prdGrphScript);
            }

            if (!prdGrphData.isEmpty()) {
                String prdGrphScriptMax;
                prdGrphScriptMax = readFile(Charset.defaultCharset(), "amountAchievementsProductGrphMAX.txt");
                Object prdGrphlistMax = entityManager.createNativeQuery(prdGrphScriptMax).setParameter("asOfDate", asOfDate).getSingleResult();

                if (prdGrphlistMax != null) {
                    params.put("MAX_VAL_PRD", new BigDecimal(prdGrphlistMax.toString()).intValue());
                } else {
                    params.put("MAX_VAL_PRD", 0);
                }
            }


            String targetRegionScript;
            targetRegionScript = readFile(Charset.defaultCharset(), "amountAchievementsRegionSummery.txt");
            List<Object[]> targetRegionlist = entityManager.createNativeQuery(targetRegionScript).setParameter("asOfDate", asOfDate).getResultList();

            List<Map<String, ?>> targetRegionData = new ArrayList<>();

            targetRegionlist.forEach(record -> {
                Map<String, Object> map = new HashMap<>();

                map.put("REGION", (record[0] == null ? "" : record[0].toString()));

                map.put("PAR_DATE_REGION1", (record[1] == null ? "" : record[1].toString()));
                map.put("PAR_DATE_REGION2", (record[2] == null ? "" : record[2].toString()));
                map.put("PAR_DATE_REGION3", (record[3] == null ? "" : record[3].toString()));

                map.put("TARGET_CLTS_REGION1", (record[4] == null ? 0 : new BigDecimal(record[4].toString()).longValue()));
                map.put("TARGET_CLTS_REGION2", (record[5] == null ? 0 : new BigDecimal(record[5].toString()).longValue()));
                map.put("TARGET_CLTS_REGION3", (record[6] == null ? 0 : new BigDecimal(record[6].toString()).longValue()));

                map.put("ACHIEVEMENT_CLTS_REGION1", (record[7] == null ? 0 : new BigDecimal(record[7].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION2", (record[8] == null ? 0 : new BigDecimal(record[8].toString()).longValue()));
                map.put("ACHIEVEMENT_CLTS_REGION3", (record[9] == null ? 0 : new BigDecimal(record[9].toString()).longValue()));

                targetRegionData.add(map);
            });

            if (targetRegionData.size() > 0) {
                params.put("rgPrdRegionSmry", getJRDataSource(targetRegionData));
            } else {
                noDataProceed = true;
                SendErrorEmail("", "getAmountTargetVsAchievements- targetRegionScript", "No Data Found : " + targetHdrScript);
            }

            if (!noDataProceed) {
                generateMwxReport(AMOUNT_TARGET_ACHIEVEMENTS, params, null, "5-Amount_Based_TargetAchievements-" + todayDate);
                generatedReports.add("5-Amount_Based_TargetAchievements-" + todayDate);
            }

        } catch (Exception exp) {
            SendErrorEmail("", "5-Amount_Based_TargetAchievements-", ExceptionUtils.getStackTrace(exp));
            ExceptionLogger(exp, "5-Amount_Based_TargetAchievements-" + todayDate);
        }
    }

    private JRDataSource getJRDataSource(List<?> list) {
        return new JRBeanCollectionDataSource(list);
    }

    private String readFile(Charset encoding, String fileName) throws IOException {
        byte[] encoded = null;
        encoded = Files.readAllBytes(Paths.get(QUERY_FILE_PATH + fileName));
        return new String(encoded, encoding);
    }

    @Retryable(value = {Exception.class}, maxAttempts = RESTART_POLICY, backoff = @Backoff(delay = BACK_OFF_DELAY))
    @Scheduled(cron = "${cron.expression.email-send-timeline}")
    public void sendingEmail() {
        String todayDate = "";
        String todayDate1 = "";

        try {

            InternetAddress[] addresses = getEmailToReceipt(0);

            String asOfDateScript;
            asOfDateScript = readFile(Charset.defaultCharset(), "asOfDateScript.txt");
            Object asOfDate = entityManager.createNativeQuery(asOfDateScript).getSingleResult();

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyyyy");
                SimpleDateFormat sdf1 = new SimpleDateFormat("dd MMMM yyyy");
                todayDate = sdf.format(asOfDate);
                todayDate1 = sdf1.format(asOfDate);
            } catch (Exception ex) {
                logger.error(ex.toString());
            }

            final int todayMonday = cal.get(Calendar.DAY_OF_WEEK);

            multipart = new MimeMultipart();

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(environment.getProperty("cron.email.send-from"), "MWX Reports"));
            message.setSubject("Management Reports " + todayDate1.substring(0, 3) + " " + todayDate1.substring(3));

            String brunchDayEndScript = "";
            if (todayMonday != 2) {
                brunchDayEndScript = "\n" +
                        "  SELECT r.REG_NM             \"Region\",\n" +
                        "         a.AREA_NM            \"Area\",\n" +
                        "         B.BRNCH_NM           \"Branch\",\n" +
                        "         CPM.CLOSING_DATE     \"ClosingDate\"\n" +
                        "    FROM REPORT_USERS_CPM_VU cpm,\n" +
                        "         mw_brnch           b,\n" +
                        "         mw_area            a,\n" +
                        "         mw_reg             r\n" +
                        "   WHERE     cpm.BRANCH_CD = b.BRNCH_CD\n" +
                        "         AND b.area_seq = a.area_seq\n" +
                        "         AND a.reg_seq = r.reg_Seq\n" +
                        "         AND CPM.CLOSING_DATE != TO_DATE ( :asOfDate, 'MM/dd/yyyy')\n" +
                        "         AND B.BRNCH_cd NOT IN ('0258', '0259')\n" +
                        "ORDER BY 1, 2, 3";
            } else {
                brunchDayEndScript = "SELECT r.REG_NM             \"Region\",\n" +
                        "         a.AREA_NM            \"Area\",\n" +
                        "         B.BRNCH_NM           \"Branch\",\n" +
                        "         CPM.CLOSING_DATE     \"ClosingDate\"\n" +
                        "    FROM REPORT_USERS_CPM_VU cpm,\n" +
                        "         mw_brnch           b,\n" +
                        "         mw_area            a,\n" +
                        "         mw_reg             r\n" +
                        "   WHERE     cpm.BRANCH_CD = b.BRNCH_CD\n" +
                        "         AND b.area_seq = a.area_seq\n" +
                        "         AND a.reg_seq = r.reg_Seq\n" +
                        "         AND TO_DATE (CPM.CLOSING_DATE) NOT IN\n" +
                        "                 (TO_DATE ( :asOfDate, 'MM/dd/yyyy'),\n" +
                        "                  TO_DATE ( :asOfDate, 'MM/dd/yyyy') - 1,\n" +
                        "                  TO_DATE ( :asOfDate, 'MM/dd/yyyy') - 2)\n" +
                        "         AND B.BRNCH_cd NOT IN ('0258', '0259')\n" +
                        "ORDER BY 1, 2, 3";
            }


            List<Object[]> brnchDayEndList = entityManager.createNativeQuery(brunchDayEndScript).setParameter("asOfDate", sdf.format(asOfDate)).getResultList();
            StringBuilder body = new StringBuilder();
            String str = "";

            if (brnchDayEndList.size() > 0 && brnchDayEndList != null) {
                for (int i = 0; i < brnchDayEndList.size(); i++) {
                    Object[] obj = brnchDayEndList.get(i);

                    String color = "#e7d2d2";
                    if (i % 2 == 0) {
                        color = "#e2abab";
                    }
                    body.append("<tr style=\"background-color: " + color + "\">" +
                            "<td>" + (i + 1) + "</td>" +
                            "<td>" + obj[0] + "</td>" +
                            "<td>" + obj[1] + "</td>" +
                            "<td>" + obj[2] + "</td>" +
                            "</tr>");
                }
            } else {
                str = "Please note today status, All branches have day closed successfully.";
            }

            String msg = "<html>\n" +
                    "<style>\n" +
                    "\n" +
                    "    body{\n" +
                    "    font-family: Calibri, 'Trebuchet MS', sans-serif\n" +
                    "    }\n" +
                    "    table.GeneratedTable {\n" +
                    "        width: 70%;\n" +
                    "        background-color: #ffffff;\n" +
                    "        border-collapse: collapse;\n" +
                    "        border-width: 2px;\n" +
                    "        border-color: #000000;\n" +
                    "        border-style: solid;\n" +
                    "        color: #000000;\n" +
                    "        font-size: 12px;\n" +
                    "    }\n" +
                    "\n" +
                    "    table.GeneratedTable td,\n" +
                    "    table.GeneratedTable th {\n" +
                    "        border-width: 1px;\n" +
                    "        border-color: #000000;\n" +
                    "        border-style: solid;\n" +
                    "        padding: 3px;\n" +
                    "    }\n" +
                    "\n" +
                    "    table.GeneratedTable thead {\n" +
                    "        background-color: #d15454;\n" +
                    "    }\n" +
                    "\n" +
                    "    table.GeneratedTable tbody tr:nth-child(even) {\n" +
                    "        background-color: #e2abab;\n" +
                    "    }\n" +
                    "\n" +
                    "    table.GeneratedTable tbody tr:nth-child(odd) {\n" +
                    "        background-color: #e7d2d2;\n" +
                    "    }\n" +
                    "</style>\n" +
                    "\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div class=\"body\">\n" +
                    "      Dear <strong>Management Team</strong>, <br/><br/>Please find enclosed the following reports for " + todayDate1.substring(0, 3) + "<sup>th</sup> " + todayDate1.substring(3) + "." +
                    "      <ol>\n" +
                    "        <li>Portfolio Quality.</li>\n" +
                    "        <li>Rescheduled Portfolio Quality.</li>\n" +
                    "        <li>Regular Client Portfolio Quality.</li>\n" +
                    "        <li>Targets v/s Achievements.</li>\n" +
                    "        <li>Amount Based Targets v/s Achievements.</li>\n" +
                    "        <li>PAR Breakdown.</li>\n" +
                    "        <li>Management Dashboard.</li>\n" +
//                    "        <li>Rescheduling Portfolio Quality.</li>\n" +
                    "        <li>Recovery Monitoring Report.</li>\n" +
                    "        <li>Active Clients Report.</li>\n" +
//                    "        <li>New Portfolio Quality.</li>\n" +
                    "      </ol>\n" +
                    "\n" +
                    "        <b>The list of branches with pending day end closing is as follows:<br/><br/>\n" +
                    "    </div>\n" +
                    "\n" +
                    "    <table class=GeneratedTable>\n" +
                    "        <thead>\n" +
                    "            <tr style=\"color: #ffffff; font-size: 14px;\">\n" +
                    "                <th> Sr. #</th>\n" +
                    "                <th>Region</th>\n" +
                    "                <th>Area</th>\n" +
                    "                <th>Branch</th>\n" +
                    "            </tr>\n" +
                    "        </thead>\n" +
                    "        <tbody>\n" +
                    body.toString() +
                    "        </tbody>\n" +
                    "    </table>\n" +
                    "</br><p>" + str + "</p>\n" +
                    "</br></br><p>" + "------------ System Generated Report ------------" + "</p>\n" +
                    "</body>\n" +
                    "\n" +
                    "</html>";

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html");

            String createdDate = sdf.format(new Date());
            pdfAttachcount = 0;

            attachPdfFile(PDF_REPORTS, createdDate, sdf);
            multipart.addBodyPart(mimeBodyPart);

            if (pdfAttachcount == 9 && generatedReports.size() == 9) {
                message.setRecipients(Message.RecipientType.TO, addresses);
                message.setContent(multipart);
                logger.error("Email Sending please wait...");
//                Transport.send(message);
                logger.error("Email Send Successfully...");
                updateStatus("Email Send Successfully");
            } else {
                StringBuffer buffer = new StringBuffer("");
                notGeneratedReports.forEach(report -> {
                    buffer.append(report + " \n");
                });
                if (buffer.toString().isEmpty()) {
                    buffer.append("We Could Not Found Any File While Sending Email");
                }
                SendErrorEmail(todayDate, buffer.toString(), "");
                //smsAlert(buffer.toString());
            }
        } catch (Exception exception) {
            logger.error("Error in sending email...");
            exception.printStackTrace();
            updateStatus(ExceptionUtils.getStackTrace(exception).substring(0, 495));
            //smsAlert(ExceptionUtils.getStackTrace(exception).substring(0, 700));
            SendErrorEmail(todayDate, ExceptionUtils.getStackTrace(exception).substring(0, 1000), "");
        }
    }

    public void attachPdfFile(String filePath, String createdDate, SimpleDateFormat sdf) throws Exception {
        File mwxFilePath = new File(filePath);

        File[] mwxFiles = mwxFilePath.listFiles();
        Arrays.sort(mwxFiles, Comparator.comparingLong(File::lastModified));

        for (int i = 0; i < mwxFiles.length; i++) {
            if ((sdf.format(mwxFiles[i].lastModified()).equals(createdDate)) && (mwxFiles[i].getName().endsWith(".pdf"))) {
                MimeBodyPart mimeBodyPartfile = new MimeBodyPart();
                try {
                    mimeBodyPartfile.attachFile(filePath + mwxFiles[i].getName(), "application/pdf", "base64");
                    multipart.addBodyPart(mimeBodyPartfile);
                    logger.error(mwxFiles[i].getName() + " File Found");
                    pdfAttachcount++;
                } catch (Exception exception) {
                    logger.error(mwxFiles[i].getName() + " File Not Found " + exception.getMessage());
                    throw exception;
                }
            }
        }
    }

    @Recover
    public void recover(Exception e, String str) {
        logger.error("Auto Email Service recovering");
    }

    public void SendErrorEmail(String todayDate, String reportName, String errDetail) {

        multipart = new MimeMultipart();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(environment.getProperty("cron.email.send-from"), "MWX Reports"));
            InternetAddress[] addresses = getEmailToReceipt(1);

            message.setRecipients(Message.RecipientType.TO, addresses);
            message.setSubject("Error Alert : Management Report Email Service");

            String msg = "<html>\n" +
                    "<body>\n" +
                    "\n" +
                    "    <div>\n" +
                    "      Following Reports Could Not Be Generated. Please See Logs For More Detail." + todayDate +
                    "      <ol>\n" +
                    reportName +
                    "      </ol>\n" +
                    "      <ol>Error Details: \n" +
                    errDetail +
                    "      </ol>\n" +
                    "    </div>\n" +
                    "\n" +
                    "</body>\n" +
                    "\n" +
                    "</html>";

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html");

            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);

            logger.error("Alert Email Sending please wait...");
//            Transport.send(message);
            logger.error("Alert Email Send Successfully...");

        } catch (Exception exception) {
            updateStatus(ExceptionUtils.getStackTrace(exception).substring(0, 495));
        }
    }

    public void generateMwxReport(String inputFileName, Map<String, Object> params, JRDataSource dataSource, String reportName) throws JRException {

        params.put("LOGO_IMG", REPORTS_BASEPATH + "logo.jpg");
        byte[] bytes = null;
        String jrxml = REPORTS_BASEPATH + inputFileName;
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxml);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
        JasperExportManager.exportReportToPdfFile(jasperPrint, REPORTS_BASEPATH + "queries" + File.separator + "automation" + File.separator + reportName + ".pdf");
        updateStatus(reportName + " Created Successfully");
    }

    public void updateStatus(String status) {
        long seq = SequenceFinder.findNextVal(Sequences.AUTO_LIST_STATUS_SEQ);
        MwAutoListStatus autoListStatus = new MwAutoListStatus();
        autoListStatus.setAutoListStatusSeq(seq);
        autoListStatus.setCrntDate(Instant.now());
        autoListStatus.setStatus(status);
        autoListStatus.setType("email");
        mwAutoStatusRepository.save(autoListStatus);
    }

    private InternetAddress[] getEmailToReceipt(int errorCode) throws Exception {

        // Modified By Naveed - Date - 11-02-2022
        // systemized email address table
        String emailListsScript = "";
        if (errorCode == 0) {
            emailListsScript = "SELECT LI.EMAIL_ADDR FROM KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LI WHERE LI.CRNT_REC_FLG =1 AND LI.EMAIL_CNTCTS_RCPNTS_TYP = 1";
        } else {
            emailListsScript = "SELECT LI.EMAIL_ADDR FROM KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LI WHERE LI.SEND_ERROR = 1 AND LI.EMAIL_CNTCTS_RCPNTS_TYP = 1";
        }
        // End By Naveed
        List<String> emailLists = entityManager.createNativeQuery(emailListsScript).getResultList();
        InternetAddress[] address = new InternetAddress[emailLists.size()];
        for (int i = 0; i < emailLists.size(); i++) {
            address[i] = new InternetAddress(emailLists.get(i));
        }
        return address;
    }

//    public void smsAlert(String message) {
//        try {
//            String contactScript = "SELECT LI.CNTCT_NO FROM KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LI WHERE LI.CRNT_REC_FLG =1 AND LI.SEND_ERROR = 1";
//            List<String> contactLists = entityManager.createNativeQuery(contactScript).getResultList();
//
//            List<AutoMessage> messageArrayList = new ArrayList<>();
//
//            for (int i = 0; i < contactLists.size(); i++) {
//                long seq = SequenceFinder.findNextVal(Sequences.AUTO_LIST_STATUS_SEQ);
//                messageArrayList.add (new AutoMessage(
//                        seq, contactLists.get(i),
//                        Instant.now(),
//                        "Following Management Reports Could Not Be Generated.  \n" + message ,
//                        Instant.now(),
//                        Instant.now(),
//                        1,
//                        ""));
//            }
//            smsMessageRepository.save(messageArrayList);
//        }catch (Exception exception) {
//            updateStatus(ExceptionUtils.getStackTrace(exception).substring(0, 495));
//        }
//    }

    private void ExceptionLogger(Exception exp, String name) {

        if (!name.isEmpty()) {
            notGeneratedReports.add(name);
        }
        updateStatus(ExceptionUtils.getStackTrace(exp).substring(0, 495));
        exp.printStackTrace();
    }

    private Session getSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", true);
        // Modified by Naveed - Date 11-02-2022
        // send email to valid email recipients except invalid recipients
        prop.put("mail.smtp.sendpartial", true);
        prop.put("mail.smtp.host", environment.getProperty("spring.mail.host"));
        prop.put("mail.smtp.port", new Integer(environment.getProperty("spring.mail.port")));

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(environment.getProperty("spring.mail.username"), environment.getProperty("spring.mail.password"));
            }
        });
        return session;
    }
}

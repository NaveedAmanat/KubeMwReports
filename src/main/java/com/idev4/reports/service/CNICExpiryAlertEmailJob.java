package com.idev4.reports.service;

import com.idev4.reports.domain.MwAutoListStatus;
import com.idev4.reports.dto.EmailDto;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CNICExpiryAlertEmailJob {

    private final String CNIC_EXPIRY_ALERT_REPORT = "CNIC_EXPIRY_ALERT_REPORT.jrxml";
    private final String FILE_NAME = "CLIENT_CNIC_EXPIRY_ALERT_REPORT_";
    private final String BODY = " and make sure there is no such client/nominee/co-borrower/next of kin etc with expired CNIC exits. \n";
    private final File file = null;
    String REPORTS_BASEPATH = "";
    String QUERY_FILE_PATH = "";
    String PDF_REPORTS = "";
    @Autowired
    ServletContext context;
    Logger logger = LoggerFactory.getLogger(CNICExpiryAlertEmailJob.class);
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MwAutoStatusRepository mwAutoStatusRepository;
    @Autowired
    private Email emailService;

    @Scheduled(cron = "${cron.cnic_alert.scheduled}")
//    @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{${cron.cnic_alert.restart-policy}}",
//            backoff = @Backoff(delayExpression = "#{${cron.cnic_alert.back-off-delay}}"))
    public void CNICExpiryAlert() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String todayDate = sdf.format(new Date());
        EmailDto emailDto = new EmailDto();
        emailDto.subject = "Expired CNIC Details: " + todayDate;
        emailDto.emailCredentialCd = "0002";
        emailDto.receiptType = 7;
        emailDto.dateFormat = "dd-MM-yyyy";
        emailDto.fileName = "Expired CNIC Alert";
        emailDto.body = "";
        emailDto.error = 0;
        emailDto.pathFile = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
                + file.separator + "queries" + file.separator + "cnic" + file.separator;
        ;

        try {
            QUERY_FILE_PATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
                    + file.separator + "queries" + file.separator;

            REPORTS_BASEPATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
                    + file.separator;

            PDF_REPORTS = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
                    + file.separator + "queries" + file.separator + "cnic" + file.separator;

            FileUtils.cleanDirectory(new File(PDF_REPORTS));
            getCnicExpiryReport(emailDto, todayDate);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                emailDto.body = ExceptionUtils.getStackTrace(e);
                emailDto.to = getReceipt(1, "7", 1);
                emailDto.error = 1;
                emailService.email(emailDto);
                updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void getCnicExpiryReport(EmailDto emailDto, String todayDate) throws Exception {
        Calendar calendar = Calendar.getInstance();

        Map<String, Object> params = new HashMap<>();

        String script = readFile(Charset.defaultCharset(), "CNIC_EXPIRY_ALERT_REPORT_SCRIPT.txt");
        Query set = entityManager.createNativeQuery(script);

        List<Object[]> resultList = set.getResultList();

        List<Map<String, ?>> dataset = new ArrayList();

        resultList.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("REG_NM", l[0] == null ? "" : l[0].toString());
            map.put("AREA_NM", l[1] == null ? "" : l[1].toString());
            map.put("BRNCH_NM", l[2] == null ? "" : l[2].toString());
            map.put("EMP_NM", l[3] == null ? "" : l[3].toString());
            map.put("PRD_GRP_NM", l[4] == null ? "" : l[4].toString());
            map.put("CLNT_SEQ", l[5] == null ? "" : l[5].toString());
            map.put("LOAN_APP_SEQ", l[6] == null ? "" : l[6].toString());
            map.put("CLNT_NM", l[7] == null ? "" : l[7].toString());
            map.put("DSBMT_DT", l[8] == null ? "" : l[8]);
            map.put("CNIC_EXPRY_DT", l[9] == null ? "" : l[9]);
            map.put("IDENTIFICATION", l[10] == null ? "" : l[10].toString());
            map.put("PH_NUM", l[11] == null ? "" : l[11].toString());
            map.put("DYS", l[12] == null ? 0 : new BigDecimal(l[12].toString()).longValue());
            map.put("EXPIRED", l[13] == null ? "" : l[13].toString());

            dataset.add(map);
        });

        String region = "SELECT REG.REG_SEQ, REG.REG_NM, LST.EMAIL_ADDR FROM MW_REG REG \n" +
                "     LEFT OUTER JOIN KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LST ON LST.CNTCT_NO = REG.REG_SEQ AND LST.EMAIL_CNTCTS_RCPNTS_TYP = 6\n" +
                "     WHERE REG.CRNT_REC_FLG = 1 AND REG.REG_SEQ <> -1";
        List<Object[]> regionList = entityManager.createNativeQuery(region).getResultList();

        regionList.forEach(reg -> {
            List<Map<String, ?>> regionDateset = dataset.stream().filter(rg -> rg.get("REG_NM").equals(reg[1].toString())).collect(Collectors.toList());

            String area = "SELECT AREA.AREA_SEQ, AREA.AREA_NM, LST.EMAIL_ADDR FROM MW_AREA AREA\n" +
                    "   LEFT OUTER JOIN KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LST ON LST.CNTCT_NO = AREA.AREA_SEQ AND LST.EMAIL_CNTCTS_RCPNTS_TYP = 5\n" +
                    "   WHERE AREA.CRNT_REC_FLG = 1 AND AREA.AREA_SEQ <> -1 AND AREA.REG_SEQ = :REGION";
            List<Object[]> areaList = entityManager.createNativeQuery(area).setParameter("REGION", reg[0].toString()).getResultList();

            areaList.forEach(ar -> {
                List<Map<String, ?>> areaDataSet = regionDateset.stream().filter(p -> p.get("AREA_NM").equals(ar[1].toString())).collect(Collectors.toList());

                String branch = "SELECT BR.BRNCH_SEQ, BR.BRNCH_NM, LST.EMAIL_ADDR FROM MW_BRNCH BR \n" +
                        "    LEFT OUTER JOIN KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LST ON LST.CNTCT_NO = BR.BRNCH_SEQ AND LST.EMAIL_CNTCTS_RCPNTS_TYP = 4\n" +
                        "    WHERE BR.CRNT_REC_FLG = 1 AND BR.BRNCH_SEQ <> -1 AND BR.AREA_SEQ = :AREA";

                List<Object[]> branchList = entityManager.createNativeQuery(branch).setParameter("AREA", ar[0].toString()).getResultList();

                branchList.forEach(br -> {
                    List<Map<String, ?>> branchDataset = areaDataSet.stream().filter(p -> p.get("BRNCH_NM").equals(br[1].toString())).collect(Collectors.toList());
                    if (!branchDataset.isEmpty()) {
                        String fileName = FILE_NAME + "BRANCH_" + br[1].toString().replaceAll("\\/", "_") + "-" + todayDate;

                        params.put("CNIC_EXPIRY_BM_DETAIL", getJRDataSource(branchDataset));
                        params.put("CNIC_EXPIRY_BM_SUMMARY", getJRDataSource(branchDataset));
                        generateMwxReport(CNIC_EXPIRY_ALERT_REPORT, params, null, fileName);
                        logger.info("REGION " + reg[1].toString() + " AREA " + ar[1].toString() + " BRANCH " + br[1].toString() + " CNIC Expiry Report Created");
                        try {
                            if (br[2] != null) {
                                emailDto.body = "Please find expired CNIC details of your branch " + BODY;
                                emailDto.fileName = fileName;
                                emailDto.to = getReceipt(-1, br[2].toString(), 0);
                                emailService.email(emailDto);
                                updateStatus("Email Send Successfully " + emailDto.fileName);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                emailDto.body = ExceptionUtils.getStackTrace(e);
                                emailDto.to = getReceipt(1, "7", 1);
                                emailDto.error = 1;
                                emailService.email(emailDto);
                                updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                if (!areaDataSet.isEmpty()) {
                    String fileName = FILE_NAME + "AREA_" + ar[1].toString().replaceAll("\\/", "_") + "-" + todayDate;
                    params.put("CNIC_EXPIRY_AREA_SUMMARY", getJRDataSource(areaDataSet));
                    generateMwxReport(CNIC_EXPIRY_ALERT_REPORT, params, null, fileName);
                    logger.info("REGION " + reg[1].toString() + " AREA " + ar[1].toString() + " CNIC Expiry Report Created");
                    try {
                        if (ar[2] != null) {
                            emailDto.body = "Please find expired CNIC count of your area " + BODY;
                            emailDto.fileName = fileName;
                            emailDto.to = getReceipt(-1, ar[2].toString(), 0);
                            emailService.email(emailDto);
                            updateStatus("Email Send Successfully " + emailDto.fileName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            emailDto.body = ExceptionUtils.getStackTrace(e);
                            emailDto.to = getReceipt(1, "7", 1);
                            emailDto.error = 1;
                            emailService.email(emailDto);
                            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            if (!regionDateset.isEmpty()) {
                String fileName = FILE_NAME + "REGION_" + reg[1].toString().replaceAll("\\/", "_") + "-" + todayDate;
                params.put("CNIC_EXPIRY_RM_DETAIL", getJRDataSource(regionDateset));
                generateMwxReport(CNIC_EXPIRY_ALERT_REPORT, params, null, fileName);
                logger.info("REGION " + reg[1].toString() + " CNIC Expiry Report Created");
                try {
                    if (reg[2] != null) {
                        emailDto.body = "Please find expired CNIC count of your region " + BODY;
                        emailDto.fileName = fileName;
                        emailDto.to = getReceipt(-1, reg[2].toString(), 0);
                        emailService.email(emailDto);
                        updateStatus("Email Send Successfully " + emailDto.fileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        emailDto.body = ExceptionUtils.getStackTrace(e);
                        emailDto.to = getReceipt(1, "7", 1);
                        emailDto.error = 1;
                        emailService.email(emailDto);
                        updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        if (!dataset.isEmpty()) {
            String fileName = FILE_NAME + "HO_" + todayDate;
            params.put("CNIC_EXPIRY_HO_DETAIL", getJRDataSource(dataset));
            generateMwxReport(CNIC_EXPIRY_ALERT_REPORT, params, null, fileName);
            if (isFirstMondayOfTheMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)) == calendar.get(Calendar.DATE)) {
                emailDto.body = "HO";
                emailDto.fileName = fileName;
                emailDto.to = getReceipt(1, "7", 0);
                emailService.email(emailDto);
                updateStatus("Email Send Successfully " + emailDto.fileName);
            }
        }
    }

    private JRDataSource getJRDataSource(List<?> list) {
        return new JRBeanCollectionDataSource(list);
    }

    private String readFile(Charset encoding, String fileName) throws Exception {
        byte[] encoded = null;
        try {
            encoded = Files.readAllBytes(Paths.get(QUERY_FILE_PATH + fileName));
        } catch (IOException e) {
            throw new Exception(e);
        }
        return new String(encoded, encoding);
    }

    public void generateMwxReport(String inputFileName, Map<String, Object> params, JRDataSource dataSource, String reportName) {

        params.put("LOGO_IMG", REPORTS_BASEPATH + "logo.jpg");
        byte[] bytes = null;
        String jrxml = REPORTS_BASEPATH + inputFileName;
        JasperReport jasperReport = null;
        try {
            jasperReport = JasperCompileManager.compileReport(jrxml);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, PDF_REPORTS + reportName + ".pdf");
        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    public int isFirstMondayOfTheMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        return cal.get(Calendar.DATE);
    }

    private InternetAddress[] getReceipt(int typ, String receipt, int errorCode) throws Exception {
        if (typ == -1) {
            InternetAddress[] address = new InternetAddress[1];
            address[0] = new InternetAddress(receipt);
            return address;
        }
        String emailListsScript = "";
        if (errorCode == 0) {
            emailListsScript = "SELECT LI.EMAIL_ADDR FROM KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LI WHERE LI.CRNT_REC_FLG =1 AND LI.EMAIL_CNTCTS_RCPNTS_TYP = :receiptType";
        } else {
            emailListsScript = "SELECT LI.EMAIL_ADDR FROM KASHF_REPORTING.MW_EMAIL_CNTCTS_RCPNTS_LST LI WHERE LI.SEND_ERROR = 1 AND LI.EMAIL_CNTCTS_RCPNTS_TYP = :receiptType";
        }

        List emailLists = entityManager.createNativeQuery(emailListsScript).setParameter("receiptType", receipt).getResultList();
        InternetAddress[] address = new InternetAddress[emailLists.size()];
        for (int i = 0; i < emailLists.size(); i++) {
            address[i] = new InternetAddress(emailLists.get(i).toString());
        }
        return address;
    }

    public void updateStatus(String status) {
        long seq = SequenceFinder.findNextVal(Sequences.AUTO_LIST_STATUS_SEQ);
        MwAutoListStatus autoListStatus = new MwAutoListStatus();
        autoListStatus.setAutoListStatusSeq(seq);
        autoListStatus.setCrntDate(Instant.now());
        autoListStatus.setStatus(status);
        autoListStatus.setType("cnic");
        mwAutoStatusRepository.save(autoListStatus);
    }
}

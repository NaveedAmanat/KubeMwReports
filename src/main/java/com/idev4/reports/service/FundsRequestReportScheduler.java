package com.idev4.reports.service;

import com.idev4.reports.domain.MwAutoListStatus;
import com.idev4.reports.repository.MwAutoStatusRepository;
import com.idev4.reports.util.SequenceFinder;
import com.idev4.reports.util.Sequences;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * @Added, Naveed
 * @Date, 14-06-2022
 * @Description, SCR - systemization Funds Request
 */

@Service
@EnableScheduling
public class FundsRequestReportScheduler {

    private final File file = null;
    private final String IBFT_IFT_FUNDS_TRANSFER_EXCEL = "IBFT_IFT_FUNDS_TRANSFER_EXCEL.jrxml";
    private final String CONSOLIDATED_BANK_FUNDS_EXCEL = "CONSOLIDATED_BANK_FUNDS_EXCEL.jrxml";
    private final String BANK_FUNDS_REQUEST_DATA_LOADER_EXCEL = "BANK_FUNDS_REQUEST_DATA_LOADER_EXCEL.jrxml";
    private final String KHUSHALI_FUNDS_LETTER_EXCEL = "KHUSHALI_FUNDS_LETTER_EXCEL.jrxml";
    @Autowired
    ServletContext context;
    Logger logger = LoggerFactory.getLogger(AutoEmailScheduler.class);
    String REPORTS_BASEPATH = "E:\\Generate\\Management_Reports\\";
    String QUERY_FILE_PATH = REPORTS_BASEPATH + "\\queries\\";
    String PDF_REPORTS = QUERY_FILE_PATH + "\\automation\\";
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MwAutoStatusRepository mwAutoStatusRepository;

//    @Scheduled(cron = "${cron.fund_request_reports.scheduled}")
//    void generatePdfFile() {
//        try {
//            QUERY_FILE_PATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
//                    + file.separator + "queries" + file.separator;
//
//            REPORTS_BASEPATH = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
//                    + file.separator;
//
//            PDF_REPORTS = context.getRealPath("") + "WEB-INF" + file.separator + "classes" + file.separator + "reports"
//                    + file.separator + "queries" + file.separator + "fundsRequestReports" + file.separator;
//
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(new Date());
//
//            Calendar fromDate = Calendar.getInstance();
//            fromDate.setTime(new Date());
//
//            Calendar toDate = Calendar.getInstance();
//            toDate.setTime(new Date());
//
//            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
//                fromDate.add(Calendar.DATE, -3);
//            } else {
//                fromDate.add(Calendar.DATE, -1);
//            }
//            toDate.add(Calendar.DATE, -1);
//
//            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
//            String from = sdf.format(fromDate.getTime());
//            String to = sdf.format(toDate.getTime());
//            logger.info("Funds Request Reports" + " : FROM " + from + " TO " + to);
//
//            getIBFTAndIFTReport("", from, to, "0");
//            getIBFTAndIFTReport("", from, to, "1");
//            getConsolidatedBankFundsReport("", from, to);
//            getBankFundsRequestDataLoader("", from, to);
//            getKhushaliFundsLetter("", from, to);
//
//        } catch (Exception exp) {
//            updateStatus(ExceptionUtils.getStackTrace(exp).substring(0, 495));
//        }
//    }

    public void getIBFTAndIFTReport(String user, String frmDt, String toDt, String type) throws Exception {
        Map<String, Object> params = new HashMap<>();
        String fileName = "";
        params.put("curr_user", user);
        params.put("TYP", type);

        String IbftAndIftScript;
        IbftAndIftScript = readFile(Charset.defaultCharset(), "IBFT_IFT_SCRIPT.txt");

        IbftAndIftScript = type.equals("1") ? IbftAndIftScript.replaceAll("%%", "NOT IN ('Khushhali Microfinance bank Limited', 'HBL')") : IbftAndIftScript.replaceAll("%%", "IN ('HBL')");

        Query IbftAndIftQuery = entityManager.createNativeQuery(IbftAndIftScript).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

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
            fileName = "IBFT_FUNDS_REQUEST_";
            params.put("IBFT_FUNDS", getJRDataSource(resultSet));
        } else {
            fileName = "IFT_FUNDS_REQUEST_";
            params.put("IFT_FUNDS", getJRDataSource(resultSet));
        }

        if (resultSet.size() > 0) {
            generateMwxReport(IBFT_IFT_FUNDS_TRANSFER_EXCEL, params, null, fileName + toDt);
            logger.info("File Name: " + PDF_REPORTS + fileName + toDt + ".xls");
            updateStatus(fileName + "Created Successfully " + toDt);
        } else {
            updateStatus(fileName + toDt + " No Data Found");
        }
    }

    public void getConsolidatedBankFundsReport(String user, String frmDt, String toDt) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", user);
        params.put("to_dt", toDt);
        params.put("from_dt", frmDt);
        String fileName = "CONSOLIDATED_BANK_FUNDS_";

        String consolidatedFundsScript;
        consolidatedFundsScript = readFile(Charset.defaultCharset(), "CONSOLIDATED_BANK-FUNDS_SCRIPT.txt");
        Query consolidatedFundsQuery = entityManager.createNativeQuery(consolidatedFundsScript).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

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

            resultSet.add(map);
        });

        params.put("BANK_FUNDS", getJRDataSource(resultSet));

        if (resultSet.size() > 0) {
            generateMwxReport(CONSOLIDATED_BANK_FUNDS_EXCEL, params, null, fileName + toDt);
            logger.info("File Name: " + PDF_REPORTS + fileName + toDt + ".xls");
            updateStatus(fileName + "Created Successfully " + toDt);
        } else {
            updateStatus(fileName + toDt + " No Data Found");
        }
    }

    public void getBankFundsRequestDataLoader(String frmDt, String toDt, String userId)
            throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        String fileName = "BANK_FUNDS_REQUEST_DATA_LOADER_";

        String disQry;
        disQry = readFile(Charset.defaultCharset(), "BANK_FUNDS_REQUEST_LOADER_SCRIPT.txt");
        Query set = entityManager.createNativeQuery(disQry).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

        List<Object[]> Obj = set.getResultList();

        List<Map<String, ?>> disbursements = new ArrayList();
        Obj.forEach(l -> {
            Map<String, Object> map = new HashMap();
            map.put("BRNCH_NM", l[0] == null ? "" : l[0].toString());
            map.put("BANK_NM", l[1] == null ? "" : l[1].toString());
            map.put("GL_ACCT_NUM", l[2] == null ? "" : l[2].toString());
            map.put("AMOUNT", l[3] == null ? 0 : new BigDecimal(l[3].toString()).longValue());
            disbursements.add(map);
        });

        if (disbursements.size() > 0) {
            generateMwxReport(BANK_FUNDS_REQUEST_DATA_LOADER_EXCEL, params, getJRDataSource(disbursements), fileName + toDt);
            logger.info("File Name: " + PDF_REPORTS + fileName + toDt + ".xls");
            updateStatus(fileName + "Created Successfully " + toDt);
        } else {
            updateStatus(fileName + toDt + " No Data Found");
        }
    }

    public void getKhushaliFundsLetter(String frmDt, String toDt, String userId)
            throws Exception {
        Map<String, Object> params = new HashMap<>();

        params.put("curr_user", userId);
        params.put("from_dt", frmDt);
        params.put("to_dt", toDt);
        String fileName = "KHUSHALI_FUNDS_LETTER_";
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
        Query set = entityManager.createNativeQuery(disQry).setParameter("P_FROM_DATE", frmDt).setParameter("P_TO_DATE", toDt);

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
        if (disbursements.size() > 0) {
            generateMwxReport(KHUSHALI_FUNDS_LETTER_EXCEL, params, getJRDataSource(disbursements), fileName + toDt);
            logger.info("File Name: " + PDF_REPORTS + fileName + toDt + ".xls");
            updateStatus(fileName + "Created Successfully " + toDt);
        } else {
            updateStatus(fileName + toDt + " No Data Found");
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
            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            throw new Exception(e);
        }
        return new String(encoded, encoding);
    }

    public void generateMwxReport(String inputFileName, Map<String, Object> params, JRDataSource dataSource, String reportName) throws Exception {

        String jrxml = REPORTS_BASEPATH + inputFileName;
        JasperReport jasperReport = null;
        try {
            jasperReport = JasperCompileManager.compileReport(jrxml);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
            getReportXlsx(jasperPrint, PDF_REPORTS + File.separator + reportName + ".xls");
        } catch (JRException e) {
            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            throw new Exception(e);
        }
    }

    private byte[] getReportXlsx(final JasperPrint jp, String filePath) throws JRException {
        JRXlsxExporter xlsxExporter = new JRXlsxExporter();

        ByteArrayOutputStream xlsReport = new ByteArrayOutputStream();
        xlsxExporter.setExporterInput(new SimpleExporterInput(jp));
        xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(filePath));
        SimpleXlsxReportConfiguration xlsxreportConfig = new SimpleXlsxReportConfiguration();
        xlsxreportConfig.setDetectCellType(true);
        xlsxreportConfig.setCollapseRowSpan(true);
        xlsxreportConfig.setRemoveEmptySpaceBetweenRows(true);
        xlsxreportConfig.setForcePageBreaks(false);
        xlsxreportConfig.setWrapText(false);
        xlsxreportConfig.setCollapseRowSpan(true);
        xlsxExporter.setConfiguration(xlsxreportConfig);
        xlsxExporter.exportReport();

        return xlsReport.toByteArray();

    }

    public void updateStatus(String status) {
        long seq = SequenceFinder.findNextVal(Sequences.AUTO_LIST_STATUS_SEQ);
        MwAutoListStatus autoListStatus = new MwAutoListStatus();
        autoListStatus.setAutoListStatusSeq(seq);
        autoListStatus.setCrntDate(Instant.now());
        autoListStatus.setStatus(status);
        autoListStatus.setType("fund_req");
        mwAutoStatusRepository.save(autoListStatus);
    }
}

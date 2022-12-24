package com.idev4.reports.service;

import com.idev4.reports.domain.MwAutoListStatus;
import com.idev4.reports.domain.MwStpCnfigVal;
import com.idev4.reports.repository.MwAutoStatusRepository;
import com.idev4.reports.repository.MwStpCnfigValRepository;
import com.idev4.reports.util.SequenceFinder;
import com.idev4.reports.util.Sequences;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
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
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.Recover;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Added By Naveed Date - 23-01-2022
 * SCR - Mobile Wallet Control
 * file transfer or auto park over MCB file server using SFTP
 */
@Service
@EnableScheduling
public class AutoPlacementJob {

    private final String FILE_NAME = "MCB_PayDirect_File_Payment_File_";
    private final String MCB_FUNDS = "MCB_Remittance_Disbursement_funds.jrxml";
    private final String MCB_FUNDS_EXCEL = "MCB_Remittance_Disbursement_funds_Excel.jrxml";
    @Autowired
    ServletContext context;
    Logger logger = LoggerFactory.getLogger(AutoEmailScheduler.class);
    String REPORTS_BASEPATH = "";
    String QUERY_FILE_PATH = "";
    String PDF_REPORTS = "";
    @Autowired
    private Environment environment;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MwStpCnfigValRepository mwStpCnfigValRepository;
    @Autowired
    private MwAutoStatusRepository mwAutoStatusRepository;
    private Multipart multipart;
    private String Today = "";

    private String exceptionString = "";

    private long totalRecord = 0;
    private long totalAmount = 0;

    /* Commented by Zohaib Asim - Dated 07/06/2022 - MCB Disbursement Integration - File Transfer Not required

    @Scheduled(cron = "${cron.mcb.mcb-remittance-scheduled}")
    @Retryable(value = {Exception.class}, maxAttemptsExpression = "#{${cron.mcb.restart-policy}}",
                backoff = @Backoff(delayExpression = "#{${cron.mcb.back-off-delay}}"))
    public void generatePdfFile() throws Exception {
        try {
            QUERY_FILE_PATH = context.getRealPath("") + "WEB-INF" + File.separator + "classes" + File.separator + "reports"
                    + File.separator + "queries" + File.separator;

            REPORTS_BASEPATH = context.getRealPath("") + "WEB-INF" + File.separator + "classes" + File.separator + "reports"
                    + File.separator;

            PDF_REPORTS = context.getRealPath("") + "WEB-INF" + File.separator + "classes" + File.separator + "reports"
                    + File.separator + "queries" + File.separator + "mcb_remittance" + File.separator;

            logger.info("Count " + RetrySynchronizationManager.getContext().getRetryCount());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            Today = sdf.format(new Date());
            FileUtils.cleanDirectory(new File(PDF_REPORTS));
            getMcbRemittance();
        } catch (Exception e) {
            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            exceptionString = exceptionString.isEmpty() ? ExceptionUtils.getStackTrace(e) : exceptionString;
            if (RetrySynchronizationManager.getContext().getRetryCount()
                    == (Integer.parseInt(environment.getProperty("cron.mcb.restart-policy")) - 1)) {
                email(exceptionString);
            }else{
                throw new Exception(e);
            }
        }
    }*/

    private void getMcbRemittance() throws Exception {
        try {
            totalAmount = 0;
            totalRecord = 0;
            Map<String, Object> params = new HashMap<>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());

            Calendar fromDate = Calendar.getInstance();
            fromDate.setTime(new Date());

            Calendar toDate = Calendar.getInstance();
            toDate.setTime(new Date());

            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                fromDate.add(Calendar.DATE, -3);
            } else {
                fromDate.add(Calendar.DATE, -1);
            }
            toDate.add(Calendar.DATE, -1);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            String from = sdf.format(fromDate.getTime());
            String to = sdf.format(toDate.getTime());

            logger.info(FILE_NAME + Today + " : FROM " + from + " TO " + to);

            String disQry;
            disQry = readFile(Charset.defaultCharset(), "MCBRemittanceDisbursementFundsScript.txt");
            Query set = entityManager.createNativeQuery(disQry).setParameter("frmdt", from).setParameter("todt", to);

            List<Object[]> resultList = set.getResultList();
            totalRecord = resultList.size();

            List<Map<String, ?>> disbursements = new ArrayList();
            resultList.forEach(l -> {
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
                totalAmount += l[7] == null ? 0 : new BigDecimal(l[7].toString()).longValue();
            });
            if (resultList.size() > 0) {
                generateMwxReport(MCB_FUNDS_EXCEL, params, getJRDataSource(disbursements), FILE_NAME + Today);
                logger.info("File Name: " + PDF_REPORTS + FILE_NAME + Today + ".xls");
                send(PDF_REPORTS + FILE_NAME + Today + ".xls");
                updateStatus("File Placed Successfully At MCB");
            } else {
                exceptionString = "No Data Found";
                email("No Data Found");
                updateStatus("No Data Found");
            }
        } catch (Exception e) {
            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            exceptionString = ExceptionUtils.getStackTrace(e);
            throw new Exception(e);
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
            exceptionString = ExceptionUtils.getStackTrace(e);
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
            exceptionString = ExceptionUtils.getStackTrace(e);
            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            throw new Exception(e);
        }
    }

    private Session getSession(String host, int port, String user, String password) {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", true);
        prop.put("mail.smtp.sendpartial", true);
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });
        return session;
    }

    public void email(String msg) throws Exception {
        List<MwStpCnfigVal> stpCnfigValList = mwStpCnfigValRepository.findAllByStpGrpCdAndCrntRecFlgOrderBySortOrderAsc("0002", true);

        if (exceptionString.isEmpty()) {
            msg = FILE_NAME + Today + "\t" + msg + "<br> Total Record: <b>" + totalRecord + "</b> <br> Total Amount: <b>" + totalAmount + "</b>";
        } else {
            msg = FILE_NAME + Today + msg;
        }

        String EMAIL_HOST = "";
        int EMAIL_PORT = 0;
        String EMAIL_USER = "";
        String EMAIL_PASSWORD = "";
        try {

            for (MwStpCnfigVal stp : stpCnfigValList) {
                if (stp.getStpValCd().equals("0001")) {
                    EMAIL_USER = stp.getRefCdValDscr().split(":")[0];
                } else if (stp.getStpValCd().equals("0002")) {
                    EMAIL_PASSWORD = stp.getRefCdValDscr();
                } else if (stp.getStpValCd().equals("0003")) {
                    EMAIL_HOST = stp.getRefCdValDscr();
                } else if (stp.getStpValCd().equals("0004")) {
                    EMAIL_PORT = Integer.parseInt(stp.getRefCdValDscr());
                }
            }

            InternetAddress[] addresses = getEmailToReceipt();
            multipart = new MimeMultipart();
            Message message = new MimeMessage(getSession(EMAIL_HOST, EMAIL_PORT, EMAIL_USER, EMAIL_PASSWORD));
            message.setFrom(new InternetAddress(EMAIL_USER, "MCB_REMITTANCE"));

            message.setRecipients(Message.RecipientType.TO, addresses);
            message.setSubject("MCB_REMITTANCE");

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(msg, "text/html");

            multipart.addBodyPart(mimeBodyPart);
            message.setContent(multipart);

            logger.info("MCB REMITTANCE Email Sending please wait...");
            Transport.send(message);
            logger.info("MCB REMITTANCE Email Send Successfully...");
        } catch (Exception e) {
            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            exceptionString = ExceptionUtils.getStackTrace(e);
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

    private byte[] getReportPdf(final JasperPrint jp) throws JRException {
        return JasperExportManager.exportReportToPdf(jp);
    }

    public void send(String fileName) throws Exception {

        List<MwStpCnfigVal> stpCnfigVal = mwStpCnfigValRepository.findAllByStpGrpCdAndCrntRecFlgOrderBySortOrderAsc("0001", true);

        String SFTP_HOST = "";
        int SFTP_PORT = 0;
        String SFTP_USER = "";
        String SFTP_PASSWORD = "";
        String REMOTE_PATH = "";

        for (MwStpCnfigVal stp : stpCnfigVal) {
            if (stp.getStpValCd().equals("0001")) {
                SFTP_HOST = stp.getRefCdValDscr().split(":")[0];
                SFTP_PORT = Integer.parseInt(stp.getRefCdValDscr().split(":")[1]);
            } else if (stp.getStpValCd().equals("0002")) {
                SFTP_USER = stp.getRefCdValDscr();
            } else if (stp.getStpValCd().equals("0003")) {
                SFTP_PASSWORD = stp.getRefCdValDscr();
            } else if (stp.getStpValCd().equals("0004")) {
                REMOTE_PATH = stp.getRefCdValDscr();
            }
        }

        com.jcraft.jsch.Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;
        FileInputStream inputStream = null;
        logger.info("preparing the host information for sftp.");

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            logger.info("Host connected.");
            channel = session.openChannel("sftp");
            channel.connect();
            logger.info("sftp channel opened and connected.");
            channelSftp = (ChannelSftp) channel;
            File f = new File(fileName);
            inputStream = new FileInputStream(f);
            channelSftp.cd(REMOTE_PATH);
            channelSftp.put(new FileInputStream(f), f.getName());
            logger.info("File transferred successfully to host.");

            email("Placed at MCB SFTP Server");

        } catch (Exception e) {
            updateStatus(ExceptionUtils.getStackTrace(e).substring(0, 495));
            exceptionString = ExceptionUtils.getStackTrace(e);
            throw new Exception(e);
        } finally {
            channelSftp.exit();
            logger.info("sftp Channel exited.");
            channel.disconnect();
            logger.info("Channel disconnected.");
            session.disconnect();
            logger.info("Host Session disconnected.");
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("inputStream disconnected.");
        }
    }

    @Recover
    public void recover(Exception e, String str) {
        logger.error("MCB Remittance Funds SFTP Service recovering ");
    }

    public void updateStatus(String status) {
        long seq = SequenceFinder.findNextVal(Sequences.AUTO_LIST_STATUS_SEQ);
        MwAutoListStatus autoListStatus = new MwAutoListStatus();
        autoListStatus.setAutoListStatusSeq(seq);
        autoListStatus.setCrntDate(Instant.now());
        autoListStatus.setStatus(status);
        autoListStatus.setType("mcb_remit");
        mwAutoStatusRepository.save(autoListStatus);
    }

    private InternetAddress[] getEmailToReceipt() throws Exception {

        List<MwStpCnfigVal> mwStpCnfigVals = mwStpCnfigValRepository.findAllByStpGrpCdAndCrntRecFlgOrderBySortOrderAsc("0003", true);
        InternetAddress[] address = new InternetAddress[mwStpCnfigVals.size()];
        for (int i = 0; i < mwStpCnfigVals.size(); i++) {
            address[i] = new InternetAddress(mwStpCnfigVals.get(i).getRefCdValDscr());
        }
        return address;
    }
}

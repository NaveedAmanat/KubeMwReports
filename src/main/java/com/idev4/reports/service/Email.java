package com.idev4.reports.service;

import com.idev4.reports.domain.MwStpCnfigVal;
import com.idev4.reports.dto.EmailDto;
import com.idev4.reports.repository.MwStpCnfigValRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Email {

    Logger logger = LoggerFactory.getLogger(Email.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MwStpCnfigValRepository mwStpCnfigValRepository;

    public void email(EmailDto emailDto) throws Exception {

        Multipart multipart = new MimeMultipart();

        SimpleDateFormat sdf = new SimpleDateFormat(emailDto.dateFormat);
        String createdDate = sdf.format(new Date());

        if (emailDto.error == 0) {
            multipart = attachPdfFile(emailDto.pathFile, emailDto.fileName, createdDate, sdf, multipart);
        }

        emailDto = emailCredential(emailDto.emailCredentialCd, emailDto);

        Message message = new MimeMessage(getSession(emailDto));
        message.setFrom(new InternetAddress(emailDto.user));

        message.setRecipients(Message.RecipientType.TO, emailDto.to);
        message.setSubject(emailDto.subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(emailDto.body, "text/html");

        multipart.addBodyPart(mimeBodyPart);
        message.setContent(multipart);

        logger.info(emailDto.fileName + " Email Sending please wait...");
        Transport.send(message);
        logger.info(emailDto.fileName + " Email Send Successfully...");
    }

    private Session getSession(EmailDto emailDto) {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", true);
        prop.put("mail.smtp.sendpartial", true);
        prop.put("mail.smtp.host", emailDto.host);
        prop.put("mail.smtp.port", emailDto.port);

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailDto.user, emailDto.password);
            }
        });
        return session;
    }

    private EmailDto emailCredential(String groupCode, EmailDto emailDto) {
        List<MwStpCnfigVal> lists = mwStpCnfigValRepository.findAllByStpGrpCdAndCrntRecFlgOrderBySortOrderAsc(groupCode, true);

        for (MwStpCnfigVal stp : lists) {
            if (stp.getStpValCd().equals("0001")) {
                emailDto.user = stp.getRefCdValDscr().split(":")[0];
            } else if (stp.getStpValCd().equals("0002")) {
                emailDto.password = stp.getRefCdValDscr();
            } else if (stp.getStpValCd().equals("0003")) {
                emailDto.host = stp.getRefCdValDscr();
            } else if (stp.getStpValCd().equals("0004")) {
                emailDto.port = Integer.parseInt(stp.getRefCdValDscr());
            }
        }
        return emailDto;
    }

    public Multipart attachPdfFile(String filePath, String fileName, String createdDate, SimpleDateFormat sdf, Multipart multipart) throws Exception {
        File mwxFilePath = new File(filePath);
        File[] files = mwxFilePath.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));

            for (File file : files) {
                if ((sdf.format(file.lastModified()).equals(createdDate)) && (file.getName().endsWith(".pdf"))) {
                    MimeBodyPart mimeBodyPartFile = new MimeBodyPart();
                    try {
                        if (file.getName().equals(fileName + ".pdf")) {
                            mimeBodyPartFile.attachFile(filePath + file.getName(), "application/pdf", "base64");
                            multipart.addBodyPart(mimeBodyPartFile);
                            logger.info(file.getName() + " File Found");
                        }
                    } catch (Exception exception) {
                        logger.info(file.getName() + " File Not Found " + exception.getMessage());
                        throw exception;
                    }
                }
            }
        }
        return multipart;
    }
}

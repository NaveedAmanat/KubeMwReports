package com.idev4.reports.dto;

import javax.mail.internet.InternetAddress;

public class EmailDto {
    public String host;
    public String user;
    public String password;
    public int port;
    public String subject;
    public String body;
    public int receiptType;
    public String emailCredentialCd;
    public String pathFile;
    public String fileName;
    public String dateFormat;
    public InternetAddress[] to;
    public int error;
}

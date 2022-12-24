package com.idev4.reports.dto;

import java.util.Date;

// Added By Naveed - Date - 23-01-2022
// SCR- Munsalik Integration
// Common Dataset for Online Collection Channel report
public class ChannelCollectionDto {
    public Date execDate;
    public Date tranDate;
    public Date tranTime;
    public String clientId;
    public String clientName;
    public String agent;
    public String tranNum;
    public long recovery;
    public String diff;
    public String branch;
    public String adc_nm;

    public String inquirySeq;
    public Date crtdDate;
    public String bdoName;
    public String clientSeq;
    public String inquiryStatus;
    public String loanAppSeq;
    public String crtdBy;
    public String remarks;
    public long noOfCount;
    public String channelName;
    public long paidAmount;
    public long withinAmount;
    public long afterAmount;
    public long amount;
    public Date dueDate;
}
package com.idev4.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Added By Naveed - Date - 13-05-2022
 * SCR - HBL Integration
 * */
public class HBLCollectionDto {
    /*
     * HBL Inquiry Response
     * */
    @JsonProperty("consumerDetail")
    public String consumerDetail;

    @JsonProperty("reserved")
    public String reserved;

    @JsonProperty("dueDate")
    public String dueDate;

    @JsonProperty("loanStatus")
    public String loanStatus;

    @JsonProperty("amountDueDate")
    public String amountDueDate;

    @JsonProperty("amountAfterDueDate")
    public String amountAfterDueDate;

    @JsonProperty("responseCode")
    public String responseCode;

    @JsonProperty("branchName")
    public String branchName;

    /*
     * HBL Payment Request
     * */

    @JsonProperty("transmissionDate")
    public String transmissionDate;

    @JsonProperty("transmissionTime")
    public String transmissionTime;

    @JsonProperty("stan")
    public String stan;

    @JsonProperty("rrn")
    public String rrn;

    @JsonProperty("dateLocalTran")
    public String dateLocalTran;

    @JsonProperty("timeLocalTran")
    public String timeLocalTran;

    @JsonProperty("acqInstCode")
    public String acqInstCode;

    @JsonProperty("utilityCompanyId")
    public String utilityCompanyId;

    @JsonProperty("utilityConsumerNumber")
    public String utilityConsumerNumber;

    @JsonProperty("transactionAmount")
    public String transactionAmount;

    @JsonProperty("transactionCurrency")
    public String transactionCurrency;
}

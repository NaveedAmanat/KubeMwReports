package com.idev4.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Added By Naveed - Date - 23-01-2022
// SCR- Munsalik Integration
public class MunsalikCollectionDto {

    @JsonProperty("relationshipId")
    public String relationshipId;

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

    @JsonProperty("pinData")
    public String pinData;

    @JsonProperty("utilityCompanyId")
    public String utilityCompanyId;

    @JsonProperty("utilityConsumerNumber")
    public String utilityConsumerNumber;

    @JsonProperty("transactionAmount")
    public String transactionAmount;

    @JsonProperty("transactionCurrency")
    public String transactionCurrency;

    @JsonProperty("reserved")
    public String reserved;

    @JsonProperty("remainingInstallments")
    public String remainingInstallments;

    @JsonProperty("consumerDetail")
    public String consumerDetail;

    @JsonProperty("remainingInstallmentAmount")
    public String remainingInstallmentAmount;

    @JsonProperty("dueDate")
    public String dueDate;

    @JsonProperty("loanStatus")
    public String loanStatus;

    @JsonProperty("amountPaid")
    public String amountPaid;

    @JsonProperty("amountDueDate")
    public String amountDueDate;

    @JsonProperty("amountAfterDueDate")
    public String amountAfterDueDate;

    @JsonProperty("nextPaymentDueDate")
    public String nextPaymentDueDate;

    @JsonProperty("installmentNo")
    public String installmentNo;

    @JsonProperty("responseCode")
    public String responseCode;
}

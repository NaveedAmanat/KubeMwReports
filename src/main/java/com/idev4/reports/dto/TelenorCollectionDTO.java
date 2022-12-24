package com.idev4.reports.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelenorCollectionDTO {

    public String username;
    public String password;

    @JsonProperty("Bank_Mnemonic")
    public String bank_Mnemonic;

    @JsonProperty("Consumer_number")
    public String consumer_number;

    @JsonProperty("Tran_Auth_Id")
    public String tran_Auth_Id;

    @JsonProperty("Transaction_Amount")
    public String transaction_Amount;

    @JsonProperty("Tran_Date")
    public String tran_Date;

    @JsonProperty("Tran_Time")
    public String tran_Time;

    @JsonProperty("Reserved")
    public String reserved;


    @JsonProperty("Billing_Month")
    public String billing_Month;

    @JsonProperty("Date_Paid")
    public String date_Paid;

    @JsonProperty("Response_code")
    public String response_code;

    @JsonProperty("Amount_Within_DueDate")
    public String amount_Within_DueDate;

    @JsonProperty("Due_Date")
    public String due_Date;

    @JsonProperty("Amount_After_DueDate")
    public String amount_After_DueDate;

    @JsonProperty("Consumer_Detail")
    public String consumer_Detail;

    @JsonProperty("Bill_Status")
    public String bill_Status;

    @JsonProperty("Amount_Paid")
    public String amount_Paid;
}

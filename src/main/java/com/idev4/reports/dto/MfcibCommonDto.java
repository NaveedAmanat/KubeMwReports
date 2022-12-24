package com.idev4.reports.dto;

import java.util.ArrayList;
import java.util.List;

public class MfcibCommonDto {

    public String reportRef;
    public String reportGenDate;

    public String creditBureau;
    public String creditBureauType;

    public List<PersonalInfoDto> personalInfo = new ArrayList();
    public List<SummaryInfoDto> summaryInfo = new ArrayList<>();
    public List<OpenLoansSummaryDto> openLoansSummary = new ArrayList<>();
    public List<CreditScoreDto> creditScore = new ArrayList<>();
}

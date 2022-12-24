package com.idev4.reports.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.idev4.reports.domain.MwFndsLodr;
import com.idev4.reports.dto.MwPrdDTO;
import com.idev4.reports.service.GenericService;
import com.idev4.reports.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
public class ReportController {

    @Autowired
    ReportsService reportsService;

    @Autowired
    GenericService genericService;

//    @Autowired
//    AdcAutoDataSharing adcAutoDataSharing;

    @GetMapping("/brnch-prds/{userId}")
    @Timed
    public ResponseEntity<List<MwPrdDTO>> getBranchProducts(@PathVariable String userId) {
        return new ResponseEntity<List<MwPrdDTO>>(genericService.getBranchPrds(userId), HttpStatus.OK);
    }

    // @GetMapping ( "/print-overdue-loans/{prdSeq}/{asDt}/{branch}" )
    // @Timed
    // public HttpEntity< byte[] > printOverdueLoans( HttpServletResponse response, @PathVariable long prdSeq, @PathVariable String asDt,
    // @PathVariable long branch ) throws IOException {
    // String user = SecurityContextHolder.getContext().getAuthentication().getName();
    // byte[] bytes = reportsService.getOverdueLoansReport( prdSeq, asDt, user, branch );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Overdue Loans", bytes.length ) );
    // }

    // @GetMapping ( "/print-portfolio/{fromDt}/{toDt}/{branch}" )
    // @Timed
    // public HttpEntity< byte[] > printPortfolio( HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
    // @PathVariable long branch ) throws IOException {
    // String user = SecurityContextHolder.getContext().getAuthentication().getName();
    // byte[] bytes = reportsService.getPortfolioReport( fromDt, toDt, user, branch );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Portfolio", bytes.length ) );
    // }

    // @GetMapping ( "/print-fund-statment/{fromDt}/{toDt}/{branch}/{userId}" )
    // @Timed
    // public HttpEntity< byte[] > printFundStatment( HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
    // @PathVariable long branch, @PathVariable String userId ) throws IOException {
    // byte[] bytes = reportsService.getFundStatmentReport( fromDt, toDt, branch, userId );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Fund Statment", bytes.length ) );
    // }

    @GetMapping("/print-insu-clm-frm/{clntSeq}/{userId}")
    @Timed
    public HttpEntity<byte[]> printInsuClmFrm(HttpServletResponse response, @PathVariable long clntSeq, @PathVariable String userId)
            throws IOException {
        byte[] bytes = reportsService.getInsuClmFrm(clntSeq, userId);
        return new HttpEntity<byte[]>(bytes, getHeader("Insurance Claim Form", bytes.length));
    }

    // @PostMapping ( "/print-account-ledger/{userId}" )
    // @Timed
    // public HttpEntity< byte[] > printAccountLedger( HttpServletResponse response, @PathVariable String userId,
    // @RequestBody AccountLedger accountLedger ) throws IOException {
    // byte[] bytes = reportsService.getAccountLedger( accountLedger, userId );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Account Ledger", bytes.length ) );
    // }

    @GetMapping("/print-kcr-report/{trxSeq}/{userId}/{type}")
    @Timed
    public HttpEntity<byte[]> printKcrReport(HttpServletResponse response, @PathVariable long trxSeq, @PathVariable String userId,
                                             @PathVariable int type) throws IOException {
        byte[] bytes = reportsService.getKcrReport(trxSeq, userId, type);
        if (bytes == null)
            return null;
        return new HttpEntity<byte[]>(bytes, getHeader("Account Ledger", bytes.length));
    }

    // @PostMapping ( "/print-book-details/{userId}" )
    // @Timed
    // public HttpEntity< byte[] > printBookDetails( HttpServletResponse response, @PathVariable String userId,
    // @RequestBody BookDetailsDTO bookDetailsDTO ) throws IOException {
    // byte[] bytes = reportsService.getBookDetails( bookDetailsDTO, userId );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Account Ledger", bytes.length ) );
    // }

    // @PostMapping ( "/print-due-recovery/{userId}" )
    // @Timed
    // public HttpEntity< byte[] > printDueRecovery( HttpServletResponse response, @PathVariable String userId,
    // @RequestBody BookDetailsDTO bookDetailsDTO ) throws IOException {
    // byte[] bytes = reportsService.getDueRecovery( bookDetailsDTO, userId );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Account Ledger", bytes.length ) );
    // }

    @GetMapping("/print-women-participation/{date}/{branch}")
    @Timed
    public HttpEntity<byte[]> printWomenParticipation(HttpServletResponse response, @PathVariable String date,
                                                      @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getWomenParticipation(date, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Account Ledger", bytes.length));
    }

    // @GetMapping ( "/print-clt-health-beneficiary/{fromDt}/{toDt}/{branch}/{userId}" )
    // @Timed
    // public HttpEntity< byte[] > printClientHealthBeneficiary( HttpServletResponse response, @PathVariable String fromDt,
    // @PathVariable String toDt, @PathVariable long branch, @PathVariable String userId ) throws IOException {
    // byte[] bytes = reportsService.getClientHealthBeneficiaryReport( fromDt, toDt, branch, userId );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Client Health Beneficiary", bytes.length ) );
    // }

    // @GetMapping ( "/print-insurance-claim/{fromDt}/{toDt}/{branch}/{userId}" )
    // @Timed
    // public HttpEntity< byte[] > printInsuranceClaim( HttpServletResponse response, @PathVariable String fromDt, @PathVariable String
    // toDt,
    // @PathVariable long branch, @PathVariable String userId ) throws IOException {
    // byte[] bytes = reportsService.getInsuranceClaimReport( fromDt, toDt, branch, userId );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Insurance Claim", bytes.length ) );
    // }

    @GetMapping("/print-par-branch-wise/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printPARBranchWise(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                                 @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getParBranchWiseReport(fromDt, toDt, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Client Health Beneficiary", bytes.length));
    }

    @GetMapping("/print-branch-performance-review/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printBranchPerformanceReview(HttpServletResponse response, @PathVariable String fromDt,
                                                           @PathVariable String toDt, @PathVariable long branch) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getBranchPerformanceReport(fromDt, toDt, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Client Health Beneficiary", bytes.length));
    }

    private HttpHeaders getHeader(String name, int lenght) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_PDF);
        header.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + name + ".pdf");
        header.setContentLength(lenght);
        return header;
    }

    @GetMapping("/print-brnch-turnover-anlysis/{date}/{branch}")
    @Timed
    public HttpEntity<byte[]> printBranchTurnoverAnalysisAndPlaning(HttpServletResponse response, @PathVariable String date,
                                                                    @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getBranchTurnoverAnalysisAndPlaningReport(date, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Client Health Beneficiary", bytes.length));
    }

    @GetMapping("/print-five-days-advance-recovery-trends/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printFiveDaysAdvanceRecoveryTrends(HttpServletResponse response, @PathVariable String fromDt,
                                                                 @PathVariable String toDt, @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getFiveDaysAdvanceRecoveryTrends(fromDt, toDt, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Client Health Beneficiary", bytes.length));
    }

    @GetMapping("/print-top-sheet/{fromDt}/{toDt}/{branch}/{prd}/{flg}")
    @Timed
    public HttpEntity<byte[]> printTopSheet(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                            @PathVariable long branch, @PathVariable long prd, @PathVariable int flg) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getTopSheet(fromDt, toDt, branch, prd, flg, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Client Health Beneficiary", bytes.length));
    }

    @GetMapping("/print-pdc-detail/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printPDCDetails(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                              @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPDCDetailReport(fromDt, toDt, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Client Health Beneficiary", bytes.length));
    }

    @GetMapping("/rate-of-retention/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printRateOfRetention(HttpServletResponse response, @PathVariable Long brnchSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getRateOfRetentionReport(brnchSeq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Rate Of Retention", bytes.length));
    }

    // @GetMapping ( "/print-projected-clients-loan-completetion/{fromDt}/{toDt}/{branch}" )
    // @Timed
    // public HttpEntity< byte[] > printProjectedClientsLoanCompletetion( HttpServletResponse response, @PathVariable String fromDt,
    // @PathVariable String toDt, @PathVariable long branch ) throws IOException {
    // String user = SecurityContextHolder.getContext().getAuthentication().getName();
    // byte[] bytes = reportsService.getProjectedClientLoanCompletion( fromDt, toDt, branch, user );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Client Health Beneficiary", bytes.length ) );
    // }

    @GetMapping("/print-region-wise-adc/{fromDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printRegionWiseAdc(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getRegionMonthWiseADCReport(fromDt, toDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-anml-insur-claim-form/{anmlRgstrSeq}")
    @Timed
    public HttpEntity<byte[]> printAnmlInsuClmFrm(HttpServletResponse response, @PathVariable long anmlRgstrSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAnmlInsuClmFrm(anmlRgstrSeq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-portfolio-concentration/{prd}/{brnch}/{asDt}")
    @Timed
    public HttpEntity<byte[]> printAnmlInsuClmFrm(HttpServletResponse response, @PathVariable long prd, @PathVariable long brnch,
                                                  @PathVariable String asDt) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPortfolioConcentrationReport(prd, brnch, asDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Portfolio Concentration", bytes.length));
    }

    // @GetMapping ( "/print-pending-client/{prd}/{brnch}/{asDt}/{typ}" )
    // @Timed
    // public HttpEntity< byte[] > printPendingClient( HttpServletResponse response, @PathVariable long prd, @PathVariable long brnch,
    // @PathVariable String asDt, @PathVariable int typ ) throws IOException {
    // String user = SecurityContextHolder.getContext().getAuthentication().getName();
    // byte[] bytes = reportsService.getPendingClientsReport( typ, prd, brnch, asDt, user );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Portfolio Concentration", bytes.length ) );
    // }

    @GetMapping("/print-tagged-client-claim/{prd}/{brnch}/{asDt}")
    @Timed
    public HttpEntity<byte[]> printTaggedClientClaim(HttpServletResponse response, @PathVariable long prd, @PathVariable long brnch,
                                                     @PathVariable String asDt) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getTaggedClientCliamReport(prd, brnch, asDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Portfolio Concentration", bytes.length));
    }

    @GetMapping("/print-product-wise-addition/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printProductWiseAddition(HttpServletResponse response, @PathVariable String fromDt,
                                                       @PathVariable String toDt, @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getProductWiseReprotAddition(fromDt, toDt, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/agencies-target-tracking/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printAgenciesTargetTracking(HttpServletResponse response, @PathVariable Long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAgencyTrackingReport(brnchSeq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Rate Of Retention", bytes.length));
    }

    @GetMapping("/print-transferred-clients/{fromDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printTransferredClients(HttpServletResponse response, @PathVariable String fromDt,
                                                      @PathVariable String toDt) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getTransferredClients(fromDt, toDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-turn-around-time/{fromDt}/{toDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printTurnAroundTime(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                                  @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getTurnAroundTimeReport(fromDt, toDt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-female-participation-time/{toDt}/{roleType}/{regSeq}/{areaSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printFemalearticipationTime(HttpServletResponse response,
                                                          @PathVariable String toDt, @PathVariable String roleType,
                                                          @PathVariable long brnchSeq, @PathVariable long areaSeq, @PathVariable long regSeq) throws IOException, ExecutionException, InterruptedException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Map<String, ?>> all = new ArrayList();
        List<Map<String, ?>> prds = new ArrayList();
        List<Map<String, ?>> loans = new ArrayList();
        CompletableFuture<List<Map<String, ?>>> compFutureAll = reportsService.getFemaleParticipationAll(toDt, user, roleType, brnchSeq, areaSeq, regSeq);
        CompletableFuture<List<Map<String, ?>>> compFutureLoans = reportsService.getFemaleParticipationLoans(toDt, user, roleType, brnchSeq, areaSeq, regSeq);
        CompletableFuture<List<Map<String, ?>>> compFuturePrds = reportsService.getFemaleParticipationPrds(toDt, user, roleType, brnchSeq, areaSeq, regSeq);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(compFutureAll, compFutureLoans, compFuturePrds);
        allOf.get();

        if (allOf.isDone()) {
            all = compFutureAll.get();
            loans = compFutureLoans.get();
            prds = compFuturePrds.get();
        }
        byte[] bytes = reportsService.getFemaleParticipationReport(toDt, user, roleType, brnchSeq, areaSeq, regSeq, all, loans, prds);
        return new HttpEntity<byte[]>(bytes, getHeader("Female Participation Report", bytes.length));
    }

    // @GetMapping ( "/print-dues/{fromDt}/{toDt}/{branch}" )
    // @Timed
    // public HttpEntity< byte[] > printDuesReport( HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
    // @PathVariable long branch ) throws IOException {
    // String user = SecurityContextHolder.getContext().getAuthentication().getName();
    // byte[] bytes = reportsService.getDuesReport( fromDt, toDt, user, branch );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Region Wise ADC", bytes.length ) );
    // }

    @GetMapping("/print-portfolio-segmentation/{toDt}/{roleType}/{brnchSeq}/{areaSeq}/{regSeq}")
    @Timed
    public HttpEntity<byte[]> printPortfolioSegmentation(HttpServletResponse response,
                                                         @PathVariable String toDt, @PathVariable String roleType,
                                                         @PathVariable long brnchSeq, @PathVariable long areaSeq, @PathVariable long regSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPortfolioSegReport(toDt, user, roleType, brnchSeq, areaSeq, regSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-portfolio-at-risk/{toDt}/{roleType}/{brnchSeq}/{areaSeq}/{regSeq}")
    @Timed
    public HttpEntity<byte[]> printPortfolioAtRisk(HttpServletResponse response, @PathVariable String toDt, @PathVariable String roleType,
                                                   @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPortfolioAtRisk(toDt, user, roleType, brnchSeq, areaSeq, regSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Portfolio At Risk Report", bytes.length));
    }

    @GetMapping("/print-portfolio-at-risk-time/{asDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printPortfolioAtRiskTimeSeriseAsync(HttpServletResponse response, @PathVariable String asDt,
                                                                  @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException, ExecutionException, InterruptedException {

        List<Map<String, ?>> maps = new ArrayList();

        List<Map<String, ?>> prds = new ArrayList();
        List<Map<String, ?>> bdos = new ArrayList();

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        long start = System.currentTimeMillis();

        CompletableFuture<Map<String, Object>> one = reportsService.getPortfolioAtRiskTimeSeriseOne(asDt, brnchSeq);
        CompletableFuture<Map<String, Object>> two = reportsService.getPortfolioAtRiskTimeSeriseTwo(asDt, brnchSeq);
        CompletableFuture<Map<String, Object>> three = reportsService.getPortfolioAtRiskTimeSeriseThree(asDt, brnchSeq);
        CompletableFuture<Map<String, Object>> four = reportsService.getPortfolioAtRiskTimeSeriseFour(asDt, brnchSeq);
        CompletableFuture<Map<String, Object>> five = reportsService.getPortfolioAtRiskTimeSeriseFive(asDt, brnchSeq);
        CompletableFuture<Map<String, Object>> six = reportsService.getPortfolioAtRiskTimeSeriseSix(asDt, brnchSeq);

        CompletableFuture<List<Map<String, ?>>> seven = reportsService.getPortfolioAtRiskTimeSeriesSeven(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> eight = reportsService.getPortfolioAtRiskTimeSeriesEight(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> nine = reportsService.getPortfolioAtRiskTimeSeriesNine(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> ten = reportsService.getPortfolioAtRiskTimeSeriesTen(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> ele = reportsService.getPortfolioAtRiskTimeSeriesEle(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> twe = reportsService.getPortfolioAtRiskTimeSeriesTwe(asDt, brnchSeq);

        CompletableFuture<List<Map<String, ?>>> thirt = reportsService.getPortfolioAtRiskTimeSeries13(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> forty = reportsService.getPortfolioAtRiskTimeSeries14(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> fifty = reportsService.getPortfolioAtRiskTimeSeries15(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> sixty = reportsService.getPortfolioAtRiskTimeSeries16(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> seventy = reportsService.getPortfolioAtRiskTimeSeries17(asDt, brnchSeq);
        CompletableFuture<List<Map<String, ?>>> eighty = reportsService.getPortfolioAtRiskTimeSeries18(asDt, brnchSeq);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(one, two, three, four, five, six,
                seven, eight, nine, ten, ele, twe, thirt, forty, fifty, sixty, seventy, eighty);
        allOf.get();

        if (allOf.isDone()) {
            maps.add(one.get());
            maps.add(two.get());
            maps.add(three.get());
            maps.add(four.get());
            maps.add(five.get());
            maps.add(six.get());

            prds.addAll(seven.get());
            bdos.addAll(thirt.get());
            prds.addAll(eight.get());
            bdos.addAll(forty.get());
            prds.addAll(nine.get());
            bdos.addAll(fifty.get());
            prds.addAll(ten.get());
            bdos.addAll(sixty.get());
            prds.addAll(ele.get());
            bdos.addAll(seventy.get());
            prds.addAll(twe.get());
            bdos.addAll(eighty.get());

            byte[] bytes = reportsService.getPortfolioAtRiskReport(asDt, user, rpt_flg, areaSeq, regSeq, brnchSeq, maps, prds, bdos);
            long end = System.currentTimeMillis();

            System.out.println("Overall-Seconds " + (end - start) / 1000.0);

            return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
        } else {
            System.out.println("Futures are not ready");
            return new HttpEntity<byte[]>(null, getHeader("Something Went Wrong", 0));
        }
    }

    @GetMapping("/print-risk-flagging/{toDt}/{fromDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printRiskFlagging(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                                @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getRiskFlaggingReport(fromDt, toDt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-portfolio-status/{toDt}/{roleType}/{brnchSeq}/{areaSeq}/{regSeq}")
    @Timed
    public HttpEntity<byte[]> printPortfolioStatus(HttpServletResponse response, @PathVariable String toDt,
                                                   @PathVariable String roleType, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPortfolioStatusActiveReport(toDt, user, roleType, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-consolidated-loan/{fromDt}/{toDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printConsolidatedLoan(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                                    @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getConsolidatedLoanReport(fromDt, toDt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-ror/{asDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printROR(HttpServletResponse response, @PathVariable String asDt, @PathVariable long rpt_flg,
                                       @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getRORReport(asDt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-loan_utlization/{fromDt}/{toDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printLoanUtlization(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                                  @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getLoanUtilizationReport(fromDt, toDt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-monthly-status/{fromDt}/{toDt}/{rpt_flg}")
    @Timed
    public HttpEntity<byte[]> printMonthlyStatus(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt,
                                                 @PathVariable long rpt_flg) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMonthlyStatusReport(fromDt, toDt, user, rpt_flg);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-female-participation-ratio/{asDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printFemaleParticipationRatio(HttpServletResponse response, @PathVariable String asDt,
                                                            @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getFemaleParticipationRatio(asDt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-barnch-target-management/{asDt}")
    @Timed
    public HttpEntity<byte[]> printBarnchTargetManagement(HttpServletResponse response, @PathVariable String asDt) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getBranchTargetManagementTool(asDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-portfolio-status-fromdt/{frmDt}/{toDt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printPortfolioStatusFromDte(HttpServletResponse response, @PathVariable String frmDt,
                                                          @PathVariable String toDt, @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq,
                                                          @PathVariable long brnchSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPortfolioStatusActiveFromDateReport(frmDt, toDt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-mcb-cheque/{frmDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printMcbChequeReport(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt)
            throws IOException {
        byte[] bytes = reportsService.getMcbChequeInfoReport(frmDt, toDt);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-bop-info/{frmDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printBOPReport(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt)
            throws IOException {
        byte[] bytes = reportsService.getBOPInfoReport(frmDt, toDt);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-remittance-report/{frmDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printRemReport(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getEPAndRemReport(frmDt, toDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-funds-report/{frmDt}/{toDt}/{brnch}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printFundTransferReport(HttpServletResponse response, @PathVariable String frmDt,
                                                      @PathVariable String toDt, @PathVariable boolean isxls, @PathVariable long brnch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getFundsReport(frmDt, toDt, user, brnch, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("Region Wise ADC", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-disbursement-report/{frmDt}/{toDt}/{brnch}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printDisbursementReport(HttpServletResponse response, @PathVariable String frmDt,
                                                      @PathVariable String toDt, @PathVariable long brnch, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getDisbursementReport(frmDt, toDt, user, brnch, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("Region Wise ADC", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-recovery-report/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printRecoveryReport(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt,
                                                  @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getRecoveryReport(frmDt, toDt, user, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("Region Wise ADC", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-fund-management-report/{frmDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printFundManagementReport(HttpServletResponse response, @PathVariable String frmDt,
                                                        @PathVariable String toDt) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getFundManagmentReport(frmDt, toDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    // @GetMapping ( "/print-organization-tagging-report/{frmDt}/{toDt}" )
    // @Timed
    // public HttpEntity< byte[] > printOrganizationTaggingReport( HttpServletResponse response, @PathVariable String frmDt,
    // @PathVariable String toDt ) throws IOException {
    // String user = SecurityContextHolder.getContext().getAuthentication().getName();
    // byte[] bytes = reportsService.getOrganizationTaggingReport( frmDt, toDt, user );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Region Wise ADC", bytes.length ) );
    // }

    @GetMapping("/print-product-wise-disbursement-report/{frmDt}/{toDt}/{prd}")
    @Timed
    public HttpEntity<byte[]> printProductWiseDisbursementReport(HttpServletResponse response, @PathVariable String frmDt,
                                                                 @PathVariable String toDt, @PathVariable Long prd) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getProductWiseDisbursementReport(frmDt, toDt, prd, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));

    }

    private HttpHeaders getHeaderXls(String name, int lenght) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name + ".xlsx");
        header.setContentLength(lenght);
        return header;
    }

    @GetMapping("/print-ogranization-time-report/{frmDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printOrganziationTagTimeReport(HttpServletResponse response, @PathVariable String frmDt,
                                                             @PathVariable String toDt) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getOrganizationTagTimeReport(frmDt, toDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-reversal-enteries-report/{frmdt}/{todt}/{brnch}")
    @Timed
    public HttpEntity<byte[]> printReversalEnteriesReport(HttpServletResponse response, @PathVariable String frmdt,
                                                          @PathVariable String todt, @PathVariable long brnch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getReversalEnteriesReport(frmdt, todt, brnch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-trail-balance-report/{frmdt}/{todt}/{brnch}")
    @Timed
    public HttpEntity<byte[]> printTrailBalancesReport(HttpServletResponse response, @PathVariable String frmdt,
                                                       @PathVariable String todt, @PathVariable long brnch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getTrailBalance(frmdt, todt, brnch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-barch-ranking/{vstseq}")
    @Timed
    public HttpEntity<byte[]> printBrnchRankngReport(HttpServletResponse response, @PathVariable long vstseq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getBrnchRnkngReport(vstseq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-cpc-report/{vstseq}")
    @Timed
    public HttpEntity<byte[]> printCPCReport(HttpServletResponse response, @PathVariable long vstseq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getCpcReport(vstseq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-aicg-report/{trng_seq}")
    @Timed
    public HttpEntity<byte[]> printaicgReport(HttpServletResponse response, @PathVariable long trng_seq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getacigReport(trng_seq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-monthly-ranking-report/{vstseq}")
    @Timed
    public HttpEntity<byte[]> printMonthlyRanking(HttpServletResponse response, @PathVariable long vstseq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMonthlyRanking(vstseq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-gesa-report/{trng_seq}")
    @Timed
    public HttpEntity<byte[]> printGesaSfe(HttpServletResponse response, @PathVariable long trng_seq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getGesaSFE(trng_seq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    // @GetMapping ( "/print-md-par_report/{toDt}" )
    // @Timed
    // public HttpEntity< byte[] > printMdParReport( HttpServletResponse response, @PathVariable String toDt ) {
    // String user = SecurityContextHolder.getContext().getAuthentication().getName();
    // byte[] bytes = reportsService.getPARMD( toDt, user );
    // return new HttpEntity< byte[] >( bytes, getHeader( "Region Wise ADC", bytes.length ) );
    // }

    @GetMapping("/print-advance-recovery-report/{frmdt}/{todt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printAdvanceRecoveryReport(HttpServletResponse response, @PathVariable String frmdt,
                                                         @PathVariable String todt, @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq,
                                                         @PathVariable long brnchSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAdvanceRecoveryReport(frmdt, todt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-advance-client-report/{frmdt}/{todt}/{rpt_flg}")
    @Timed
    public HttpEntity<byte[]> printAdvanceClientReport(HttpServletResponse response, @PathVariable String frmdt,
                                                       @PathVariable String todt, @PathVariable long rpt_flg) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAdvanceClientReport(frmdt, todt, user, rpt_flg);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-advance-maturity-report/{as_dt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printAdvanceMaturityReportWithFlgs(HttpServletResponse response, @PathVariable String as_dt,
                                                                 @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAdvanceMaturityReport(as_dt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-weekly-target-report/{as_dt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printAdvanceMaturityReport(HttpServletResponse response, @PathVariable String as_dt,
                                                         @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getWeeklyTargetReport(as_dt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-area-disb-report/{as_dt}/{rpt_flg}/{areaSeq}/{regSeq}/{brnchSeq}")
    @Timed
    public HttpEntity<byte[]> printAreaDisbursementReport(HttpServletResponse response, @PathVariable String as_dt,
                                                          @PathVariable long rpt_flg, @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable long brnchSeq)
            throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAreaDisbursReport(as_dt, user, rpt_flg, areaSeq, regSeq, brnchSeq);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-kss-training-report/{asDt}")
    @Timed
    public HttpEntity<byte[]> printKssTraining(HttpServletResponse response, @PathVariable long asDt) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getKSSTraining(asDt, user);
        return new HttpEntity<byte[]>(bytes, getHeader("KSS Training", bytes.length));
    }

    @GetMapping("/print-client-recovery-status/{todt}/{brnch}")
    @Timed
    public HttpEntity<byte[]> printClientRecoveryStatus(HttpServletResponse response, @PathVariable String todt,
                                                        @PathVariable long brnch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getClientRecoveryStatus(todt, user, brnch);
        return new HttpEntity<byte[]>(bytes, getHeader("Clients Recovery", bytes.length));
    }

    @GetMapping("/print-due-vs-recovery")
    @Timed
    public HttpEntity<byte[]> printDueVsRecovery(HttpServletResponse response) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getDueVsRecovery(user);
        return new HttpEntity<byte[]>(bytes, getHeader("Due Vs Recovery", bytes.length));
    }

    @GetMapping("/print-management-dashboard")
    @Timed
    public HttpEntity<byte[]> printManagementDashboard(HttpServletResponse response) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getManagementDashboard(user);
        return new HttpEntity<byte[]>(bytes, getHeader("Management Dashboard", bytes.length));
    }

    @GetMapping("/print-rescheduling-portfolio")
    @Timed
    public HttpEntity<byte[]> printReschedulingPortfolio(HttpServletResponse response) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getReschedulingPortfolio(user);
        return new HttpEntity<byte[]>(bytes, getHeader("Rescheduling Portfolio", bytes.length));
    }

    @GetMapping("/print-portfolio-quality-old-portfolio")
    @Timed
    public HttpEntity<byte[]> printPortfolioQualityOldPortfolio(HttpServletResponse response) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPortfolioQualityOldPortfolio(user);
        return new HttpEntity<byte[]>(bytes, getHeader(" Portfolio Quality Old Portfolio", bytes.length));
    }

    @GetMapping("/print-sale-2-pending-report/{fromDt}/{toDt}/{branch}/{userId}")
    @Timed
    public HttpEntity<byte[]> printSale2PendingReport(HttpServletResponse response, @PathVariable String fromDt,
                                                      @PathVariable String toDt, @PathVariable long branch, @PathVariable String userId) throws IOException {
        byte[] bytes = reportsService.getSale2PendingReport(fromDt, toDt, branch, userId);
        return new HttpEntity<byte[]>(bytes, getHeader("Sale 2 Pending", bytes.length));
    }

    @GetMapping("/leave-and-attendance-monitoring-report/{frmdt}/{todt}/{rpt_flg}")
    @Timed
    public HttpEntity<byte[]> printAttendanceMonitoringReport(HttpServletResponse reponse, @PathVariable String frmdt,
                                                              @PathVariable String todt, @PathVariable long rpt_flg) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getLeaveAndAttendanceMonitoringReport(frmdt, todt, user, rpt_flg);
        return new HttpEntity<byte[]>(bytes, getHeader("Region Wise ADC", bytes.length));
    }

    @GetMapping("/print-aml-matches/{fromDt}/{toDt}/{brnchSeq}/{isXlx}")
    @Timed
    public HttpEntity<byte[]> printAgencyInfo(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt, @PathVariable Long brnchSeq, @PathVariable boolean isXlx) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAMLMatchesRport(fromDt, toDt, brnchSeq, user, isXlx);
        return new HttpEntity<byte[]>(bytes, getHeader("AML Matches", bytes.length));
    }

    @GetMapping("/print-active-clients")
    @Timed
    public HttpEntity<byte[]> printActiveClients(HttpServletResponse response) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getActiveClientsReport(user);

        return new HttpEntity<byte[]>(bytes, getHeader("Active Client", bytes.length));
    }

    @GetMapping("/cnic_expiry_detail")
    @Timed
    public HttpEntity<byte[]> printCnicExpDetail(HttpServletResponse reponse) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getCnicExpDetailReport(user);
        return new HttpEntity<byte[]>(bytes, getHeader("CNIC Expiry Detail", bytes.length));
    }

    @GetMapping("/client-loan-maturity/{toDt}/{isXls}")
    @Timed
    public HttpEntity<byte[]> printClientLoanMaturity(HttpServletResponse reponse, @PathVariable String toDt, @PathVariable Boolean isXls) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getClientLoanMaturityReport(user, toDt, isXls);
        return new HttpEntity<byte[]>(bytes, getHeader("client-loan-maturity", bytes.length));
    }

    @GetMapping("/mobile-wallet-due/{fromDt}/{toDt}/{isXls}")
    @Timed
    public HttpEntity<byte[]> printMobileWalletDue(HttpServletResponse reponse, @PathVariable String fromDt, @PathVariable String toDt, @PathVariable Boolean isXls) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMobileWalletDueReport(user, fromDt, toDt, isXls);
        return new HttpEntity<byte[]>(bytes, getHeader("Mobile Wallet Due", bytes.length));
    }

    @GetMapping("/mobile-wallet/{fromDt}/{toDt}/{isXls}")
    @Timed
    public HttpEntity<byte[]> printMobileWallet(HttpServletResponse reponse, @PathVariable String fromDt, @PathVariable String toDt, @PathVariable boolean isXls) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMobileWalletReport(user, fromDt, toDt, isXls);
        return new HttpEntity<byte[]>(bytes, getHeader("Mobile Wallet", bytes.length));
    }

    @GetMapping("/print-mcb-remittance-disbursement-funds/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMcbRemmitanceDisbursementFundsReport(HttpServletResponse response, @PathVariable String frmDt,
                                                                        @PathVariable String toDt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMcbRemmitanceDisbursementFundsReport(frmDt, toDt, user, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("Disbursement funds", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("Disbursement funds", bytes.length));
    }

    @GetMapping("/print-mcb-remittance-disbursement-loader/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMcbRemmitanceDisbursementLoaderReport(HttpServletResponse response, @PathVariable String frmDt,
                                                                         @PathVariable String toDt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMcbRemmitanceDisbursementLoaderReport(frmDt, toDt, user, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("Disbursement loader", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("Disbursement loader", bytes.length));
    }

    @GetMapping("/print-mcb-remittance-disbursement-letter/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMcbRemmitanceDisbursementLetterReport(HttpServletResponse response, @PathVariable String frmDt,
                                                                         @PathVariable String toDt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMcbRemmitanceDisbursementLetterReport(frmDt, toDt, user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("Disbursement Letter", bytes.length));
    }

    @GetMapping("/print-check-disbursement-funds/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printCheckDisbursmentFundsReport(HttpServletResponse response, @PathVariable String frmDt,
                                                               @PathVariable String toDt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getCheckDisbursementFundsReport(frmDt, toDt, user, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("Disbursement cheque", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("Disbursement cheque", bytes.length));
    }


    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // add parameter Mobile Wallet Type
    @GetMapping("/print-mobile-wallet-Disburse-funds/{frmDt}/{toDt}/{typId}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMobileWalletDisburseFundsReport(HttpServletResponse response, @PathVariable String frmDt,
                                                                   @PathVariable String toDt, @PathVariable String typId, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMobileWalletFundsReport(frmDt, toDt, typId, user, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("mobile Wallet Disburse funds", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("mobile Wallet Disburse funds", bytes.length));
    } // end by Naveed

    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // add parameter Mobile Wallet Type
    @GetMapping("/print-mobile-wallet-Disburse-loader/{frmDt}/{toDt}/{typId}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMobileWalletLoaderReport(HttpServletResponse response, @PathVariable String frmDt,
                                                            @PathVariable String toDt, @PathVariable String typId, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMobileWalletLoaderReport(frmDt, toDt, typId, user, isxls);
        if (isxls) {
            return new HttpEntity<byte[]>(bytes, getHeaderXls("EasyPaisa loader", bytes.length));
        }
        return new HttpEntity<byte[]>(bytes, getHeader("mobile Wallet Disburse loader", bytes.length));
    }

    // modified by Naveed - 24-02-2022
    // SCR - Upaisa and HBL Konnect Mobile Wallet payment Mode
    // add parameter Mobile Wallet Type
    @GetMapping("/print-mobile-wallet-Disburse-letter/{frmDt}/{toDt}/{typId}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMobileWalletLetterReport(HttpServletResponse response, @PathVariable String frmDt,
                                                            @PathVariable String toDt, @PathVariable String typId, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMobileWalletLetterReport(frmDt, toDt, typId, user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("mobile Wallet Disburse Letter", bytes.length));
    }
    // End by Naveed

    @GetMapping("/clnt-info-jazz-dues/{isxls}")
    @Timed
    public HttpEntity<byte[]> printClientInfoJazzDuesReport(HttpServletResponse response, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getClntInfoJazzDueReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/clnt-upload-jazz-dues/{isxls}")
    @Timed
    public HttpEntity<byte[]> printClientUpldDuesReport(HttpServletResponse response, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getClientUpldDuesReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/easy-paisa-dues/{isxls}")
    @Timed
    public HttpEntity<byte[]> printEasyPaisaDuesReport(HttpServletResponse response, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getEasyPaisaDuesReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/ubl-omni-dues/{isxls}")
    @Timed
    public HttpEntity<byte[]> printUblOmniDuesReport(HttpServletResponse response, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getUblOmniDuesReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/hbl-connect-dues/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printHblConnectDuesReport(HttpServletResponse response, @PathVariable String todt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getHblConnectReport(user, todt, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/abl-loan-recovery-dues/{isxls}")
    @Timed
    public HttpEntity<byte[]> printAblLoanRecoveryDuesReport(HttpServletResponse response, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getAblLoanDuesReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/mcb-collect-dues/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMcbCollectDuesReport(HttpServletResponse response, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMcbCollectReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/nadra-dues/{isxls}")
    @Timed
    public HttpEntity<byte[]> printNadraDuesReport(HttpServletResponse response, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getNadraDuesReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("EasyPaisa Letter", bytes.length));
    }

    @GetMapping("/print-nacta-client")
    @Timed
    public HttpEntity<byte[]> printNactaClient(HttpServletResponse response) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getNactaManagementReport();

        return new HttpEntity<byte[]>(bytes, getHeader("Nacta Report", bytes.length));
    }

    // CR-Donor Tagging
    // add Filter frmDt for Report
    // Added By Naveed - 20-12-2021
    @GetMapping("/donner-tagging-report/{toDt}/{frmDt}/{donner}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printDonnerTagging(HttpServletResponse response,
                                                 @PathVariable String toDt, @PathVariable String frmDt,
                                                 @PathVariable long donner, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getDonnerTaggingReport(user, toDt, frmDt, donner, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Donner Tagging", bytes.length));
    }
    // Ended By Naveed - 20-12-2021

    @GetMapping("/verisys-report/{fromDt}/{toDt}/{branchSeq}/{areaSeq}/{regSeq}/{type}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printVerisys(HttpServletResponse response,
                                           @PathVariable String fromDt, @PathVariable String toDt,
                                           @PathVariable long branchSeq, @PathVariable long areaSeq,
                                           @PathVariable long regSeq, @PathVariable long type,
                                           @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getVerisysReport(user, fromDt, toDt, branchSeq, areaSeq, regSeq, type, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("verisys report", bytes.length));
    }

    // Added By Naveed - Date - 20-10-2021
    // Tasdeeq Report - Integration
    @GetMapping("/print-mfcib/{loanApp}/{clientCnic}/{type}")
    @Timed
    public HttpEntity<byte[]> printMfcib(HttpServletResponse reponse, @PathVariable long loanApp, @PathVariable long clientCnic, @PathVariable long type) throws Exception {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMfcibReport(loanApp, clientCnic, type);
        return new HttpEntity<byte[]>(bytes, getHeader("Mfcib Report", bytes.length));
    }
    // Ended By Naveed - Date - 20-10-2021


    /* Finnace Report */

    //Date wise Disbursment funds Report
    @GetMapping("/print-date-wise-disb-funds/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printDateWiseDisbFunds(HttpServletResponse response,
                                                     @PathVariable String frmdt,
                                                     @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getDateWiseDisbFunds(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Date wise Disbursment funds Report", bytes.length));
    }

    //Channels wise Disbursement Report
    @GetMapping("/print-channel-wise-disb/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printChannelWiseDisbFunds(HttpServletResponse response,
                                                        @PathVariable String frmdt,
                                                        @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getChannelWiseDisbFunds(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Channels wise Disbursement Report", bytes.length));
    }

    //Branch wise Disbursement Report
    @GetMapping("/print-branch-wise-disb/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printBranchWiseDisbFunds(HttpServletResponse response,
                                                       @PathVariable String frmdt,
                                                       @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBranchWiseDisbFunds(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Branch wise Disbursement Report", bytes.length));
    }

    //Products wise Disbursement Report
    @GetMapping("/print-product-wise-disb/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printProductWiseDisbFunds(HttpServletResponse response,
                                                        @PathVariable String frmdt,
                                                        @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getProductWiseDisbFunds(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Products wise Disbursement Report", bytes.length));
    }

    //Branch wise Reversal Report
    @GetMapping("/print-branch-wise-reversal/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printBranchWiseReversal(HttpServletResponse response,
                                                      @PathVariable String frmdt,
                                                      @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBranchWiseReversal(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Branch wise Reversal Report", bytes.length));
    }


    //Date and Modes wise Recovery report
    @GetMapping("/print-date-wise-recovery/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printDateWiseRecovery(HttpServletResponse response,
                                                    @PathVariable String frmdt,
                                                    @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getDateWiseRecovery(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Date and Modes wise Recovery report", bytes.length));
    }


    //Channels wise Recovery report
    @GetMapping("/print-channel-wise-recovery/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printChannelWiseRecovery(HttpServletResponse response,
                                                       @PathVariable String frmdt,
                                                       @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getChannelWiseRecovery(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Channels wise Recovery report", bytes.length));
    }

    //Branch wise Recovery Report
    @GetMapping("/print-branch-wise-recovery/{frmdt}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printBranchWiseRecovery(HttpServletResponse response,
                                                      @PathVariable String frmdt,
                                                      @PathVariable String todt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBranchWiseRecovery(user, frmdt, todt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Branch wise Recovery Report", bytes.length));
    }

    //print-ADCs-posted-recovery
    @GetMapping("/print-ADCs-posted-recovery/{frmdt}/{todt}/{isXls}")
    @Timed
    public HttpEntity<byte[]> printADCsPostedRecovery(HttpServletResponse response,
                                                      @PathVariable String frmdt, @PathVariable String todt,
                                                      @PathVariable boolean isXls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getADCsPostedRecovery(user, frmdt, todt, isXls);
        return new HttpEntity<byte[]>(bytes, getHeader("Daily ADCs Recovery posted Report", bytes.length));
    }

    //MCB Remittance mapped branches List
    @GetMapping("/print-mcb-remi-mapped-brnch/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMcbRemiMappedBranch(HttpServletResponse response, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMcbRemiMappedBranch(user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("MCB Remittance mapped branches List", bytes.length));
    }

    //Bank branches mapped List
    @GetMapping("/print-mcb-bank-brnch-mapped/{isxls}")
    @Timed
    public HttpEntity<byte[]> printBankBranchMapped(HttpServletResponse response, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBankBranchMapped(user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Daily ADCs Recovery posted Report", bytes.length));
    }

    //Mobile Wallet Mapped branches list
    @GetMapping("/print-mobile-wallet-mapped/{typId}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMobileWalletMapped(HttpServletResponse response, @PathVariable String typId, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMobileWalletMapped(user, typId, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("print-mobile-wallet-mapped", bytes.length));
    }

    //UBL Omni contact no Mapping
    @GetMapping("/print-ubl-omni-branch-mapped/{isxls}")
    @Timed
    public HttpEntity<byte[]> printUblOmniMapped(HttpServletResponse response, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getUblOmniMapped(user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Daily ADCs Recovery posted Report", bytes.length));
    }

    @GetMapping("/print-missing-animal-tag/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printAnimalTagReport(HttpServletResponse response, @PathVariable String toDt, @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getAnimalTagReport(toDt, branch, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Missing Animal Tag Report", bytes.length));
    }

    @GetMapping("/print-portfolio-km/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printPortfolioKM(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt, @PathVariable long branch) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getPortfolioMonitoringKMReport(fromDt, toDt, user, branch);
        return new HttpEntity<byte[]>(bytes, getHeader("Portfolio", bytes.length));
    }


    @GetMapping("/print-recovery-trend-analysis/{fromDt}/{toDt}/{role}/{brnchSeq}/{areaSeq}/{regSeq}/{type}")
    @Timed
    public HttpEntity<byte[]> printRecoveryTrendAnalysis(HttpServletResponse response, @PathVariable String fromDt, @PathVariable String toDt, @PathVariable long brnchSeq,
                                                         @PathVariable long areaSeq, @PathVariable long regSeq, @PathVariable String role, @PathVariable String type) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getRecoveryTrendAnalysis(toDt, fromDt, user, role, brnchSeq, areaSeq, regSeq, type);
        return new HttpEntity<byte[]>(bytes, getHeader("Recovery Trend Analysis", bytes.length));
    }

    @GetMapping("/bm-client-loan-maturity/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printBmClientLoanMaturity(HttpServletResponse reponse, @PathVariable String toDt, @PathVariable long branch) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBmClientLoanMaturityReport(user, toDt, branch);
        return new HttpEntity<byte[]>(bytes, getHeader("client-loan-maturity", bytes.length));
    }

    @GetMapping("/bm-mobile-wallet-due/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printBmMobileBMWalletDue(HttpServletResponse reponse, @PathVariable String fromDt, @PathVariable String toDt, @PathVariable long branch) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBmMobileWalletDueReport(user, fromDt, toDt, branch);
        return new HttpEntity<byte[]>(bytes, getHeader("Mobile Wallet Due", bytes.length));
    }

    @GetMapping("/bm-mobile-wallet/{fromDt}/{toDt}/{branch}")
    @Timed
    public HttpEntity<byte[]> printBmMobileWallet(HttpServletResponse reponse, @PathVariable String fromDt, @PathVariable String toDt, @PathVariable long branch) throws IOException {
        // get Current user
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBmMobileWalletReport(user, fromDt, toDt, branch);
        return new HttpEntity<byte[]>(bytes, getHeader("Mobile Wallet", bytes.length));
    }

    /* Modified by Zohaib Asim - Dated 29-07-2021 CR: Sanction List
     * Function Renamed, Parameters Added
     * */
    @GetMapping("/print-invalid-list")
    @Timed
    public HttpEntity<byte[]> printInValidClient(HttpServletResponse response,
                                                 @RequestParam String fileType, @RequestParam boolean inValidCnic) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getSancInValidClientsReport(fileType, inValidCnic);

        return new HttpEntity<byte[]>(bytes, getHeader("Nacta Report", bytes.length));
    }
    // End by Zohaib Asim

    /* Modified by Zohaib Asim - Dated 29-07-2021 CR: Sanction List
     * Function Renamed, Parameters Added
     * */
    @GetMapping("/print-match-client")
    @Timed
    public HttpEntity<byte[]> printMatchClient(HttpServletResponse response,
                                               @RequestParam String fileType, @RequestParam boolean isMtchFound) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getSancMatchReport(fileType, isMtchFound);

        return new HttpEntity<byte[]>(bytes, getHeader("Nacta/Sanction Report", bytes.length));
    }
    // End by Zohaib Asim


    // Added By Naveed - Date - 13-10-2021
    // Telenor Collection Report - SCR-EasyPaisa Integration
    // Modified By Naveed - Date - 23-01-2022
    // SCR- Munsalik Integration
    @GetMapping("/print-telenor-collection-report/{frmdt}/{toDt}/{brnchSeq}/{channelSeq}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printTelenorCollection(HttpServletResponse response, @PathVariable String frmdt, @PathVariable String toDt,
                                                     @PathVariable long brnchSeq, @PathVariable String channelSeq, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getTelenorCollectionReport(user, frmdt, toDt, brnchSeq, channelSeq, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("print-telenor-collection-report", bytes.length));
    }
    // Ended By Naveed - Date - 13-10-2021

    // Added By Naveed - Date - 04-11-2021
    // Inquiries Telenor Collection Report - SCR-EasyPaisa Integration
    // Modified By Naveed - Date - 23-01-2022
    // SCR- Munsalik Integration
    @GetMapping("/print-Inquiries-telenor-collection-report/{frmdt}/{toDt}/{brnchSeq}/{channelSeq}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printInquiriesTelenorCollection(HttpServletResponse response, @PathVariable String frmdt, @PathVariable String toDt,
                                                              @PathVariable long brnchSeq, @PathVariable String channelSeq, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getInquiriesTelenorCollectionReport(user, frmdt, toDt, brnchSeq, channelSeq, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("print-Inquiries-telenor-collection-report", bytes.length));
    }
    // Ended By Naveed - Date - 04-11-2021


    // Added By Naveed - Date - 31-12-2021
    // Animal picture report
    @GetMapping("/print-animal-picture/{loanAppSeq}")
    @Timed
    public HttpEntity<byte[]> printAnimalPicture(HttpServletResponse response, @PathVariable long loanAppSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getAnimalPictureReport(loanAppSeq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("print-animal-picture", bytes.length));
    }
    // Ended By Naveed - Date - 31-12-2021

    // addeds By Naveed - Date - 23-01-2022
    // SCR- Munsalik Integration
    // all online collection channel
    @GetMapping("/get-channel-cmpny-list")
    @Timed
    public ResponseEntity<List<Map<String, String>>> getMwRegHistory() {
        return ResponseEntity.ok().body(reportsService.getIntegratedChannelCompanyList());
    }
    // Ended By Navee - Date - 23-01-2022

    // Added by Areeba - Date - 23-01-2022
    // SCR - Mobile Wallet Control
    @GetMapping("/mob-wal-loans-report/{toDt}/{frmDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMobWalLoans(HttpServletResponse response,
                                               @PathVariable String toDt, @PathVariable String frmDt,
                                               @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMobWalLoansReport(user, toDt, frmDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Mobile Wallet Loan", bytes.length));
    }
    // Ended by Areeba

    // Added by Areeba - Date - 23-01-2022
    // SCR - Portfolio transfer
    @GetMapping("/transfer-clients-details-report/{frmDt}/{toDt}/{brnch}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printTransferClientsDetailsReport(HttpServletResponse response,
                                                                @PathVariable String frmDt, @PathVariable String toDt,
                                                                @PathVariable long brnch, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getTransferClientsDetailsReport(user, frmDt, toDt, brnch, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Transfer Clients Details", bytes.length));
    }
    // Ended by Areeba

    // Added by Areeba - Date - 25-1-2022
    // SCR - Portfolio transfer
    // Top Sheet Transfer Clients Report
    @GetMapping("/top-sheet-transfer-clients-report/{frmDt}/{toDt}/{brnch}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printTopSheetTransferClientsReport(HttpServletResponse response,
                                                                 @PathVariable String frmDt, @PathVariable String toDt,
                                                                 @PathVariable long brnch, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getTopSheetTransferClientsReport(user, frmDt, toDt, brnch, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Transfer Clients Details", bytes.length));
    }
    // Ended by Areeba

    // Added by Naveed - Date - 27-01-2022
    // SCR - RM Reports
    // Regional Risk Flagging Report
    // update by Naveed - add param for areaSeq
    @GetMapping("/regional-risk-flagging-report/{frmDt}/{toDt}/{regSeq}/{areaSeq}")
    @Timed
    public HttpEntity<byte[]> printRegionalRiskFlaggingReport(HttpServletResponse response,
                                                              @PathVariable String frmDt, @PathVariable String toDt,
                                                              @PathVariable long regSeq, @PathVariable long areaSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getRegionalRiskFlaggingReport(frmDt, toDt, regSeq, areaSeq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Regional Risk Flagging Report", bytes.length));
    }
    // Ended by Naveed - Date - 27-01-2022

    // Added by Naveed - Date 27-01-2022
    // SCR - RM Reports
    // Regional Disbursement Report
    // update by Naveed - add param for areaSeq
    @GetMapping("/regional-disbursement/{frmDt}/{toDt}/{regSeq}/{areaSeq}")
    @Timed
    public HttpEntity<byte[]> printRegionalDisbursementReport(HttpServletResponse response,
                                                              @PathVariable String frmDt, @PathVariable String toDt,
                                                              @PathVariable long regSeq, @PathVariable long areaSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getRegionalDisbursementReport(frmDt, toDt, regSeq, areaSeq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Regional Disbursement Report", bytes.length));
    }
    // Ended by Naveed - Date - 27-01-2022


    // Added by Naveed - Date 24-01-2022
    // SCR - Upaisa and HBL Konnect Payment Mode
    // Mobile Wallet Disbursement Trend Report
    @GetMapping("/mobile-wallet-trend/{frmDt}/{toDt}/{brnchSeq}/{type}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMobileWalletTrend(HttpServletResponse response, @PathVariable String frmDt,
                                                     @PathVariable String toDt, @PathVariable long brnchSeq,
                                                     @PathVariable long type, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getMobileWalletTrend(frmDt, toDt, brnchSeq, type, isxls, user);
        return new HttpEntity<byte[]>(bytes, getHeader("mobile-wallet-trend", bytes.length));
    }
    // Ended by Naveed - Date - 24-02-2022

    // Added by Naveed - Date 24-01-2022
    // SCR - Upaisa and HBL Konnect Payment Mode
    // ATM cards Management Report
    @GetMapping("/atm-cards-report/{frmDt}/{toDt}/{brnchSeq}//{isxls}")
    @Timed
    public HttpEntity<byte[]> printATMCards(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt,
                                            @PathVariable long brnchSeq, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getATMCard(frmDt, toDt, brnchSeq, user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("atm-cards-report", bytes.length));
    }
    // Ended by Naveed - Date - 24-02-2022


    @GetMapping("/upaisa-dues-sharing/{type}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printUPaisaDuesReport(HttpServletResponse response, @PathVariable long type, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getUpaisaDuesReport(user, type, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("upaisa-dues-sharing", bytes.length));
    }

    // Added by Areeba - Date - 15-02-2022
    // Accounts Reports
    @GetMapping("/premium-data-report/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printPremiumDataReport(HttpServletResponse response,
                                                     @PathVariable String toDt,
                                                     @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getPremiumDataReport(user, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Premium Data", bytes.length));
    }

    @GetMapping("/premium-data-km-report/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printPremiumDataKMReport(HttpServletResponse response,
                                                       @PathVariable String toDt,
                                                       @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getPremiumDataKMReport(user, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Premium Data KM", bytes.length));
    }

    @GetMapping("/wo-client-data-report/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printWOClientDataReport(HttpServletResponse response,
                                                      @PathVariable String toDt,
                                                      @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getWOClientDataReport(user, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Write off Clients Data", bytes.length));
    }

    @GetMapping("/wo-recovery-report/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printWORecoveryReport(HttpServletResponse response,
                                                    @PathVariable String toDt,
                                                    @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getWORecoveryReport(user, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Write off Recovery", bytes.length));
    }

    @GetMapping("/acc-recovery-report/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printAccRecoveryReport(HttpServletResponse response,
                                                     @PathVariable String toDt,
                                                     @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getAccRecoveryReport(user, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Recovery", bytes.length));
    }

    @GetMapping("/clnt-data-report/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printClientDataReport(HttpServletResponse response,
                                                    @PathVariable String toDt,
                                                    @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getClientDataReport(user, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Recovery", bytes.length));
    }

    @GetMapping("/kszb-clnt-data-report/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printKSZBClientDataReport(HttpServletResponse response,
                                                        @PathVariable String toDt,
                                                        @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getKSZBClientDataReport(user, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("Recovery", bytes.length));
    }
    // Ended by Areeba

    // Added by Naveed - Date 07-03-2022
    // SCR - RM Reports
    // Regional Recovery Trend
    // update by Naveed - add param for areaSeq
    @GetMapping("/regional-recoveries-trend/{frmDt}/{toDt}/{regSeq}/{areaSeq}")
    @Timed
    public HttpEntity<byte[]> printRegionalRecoveryTrend(HttpServletResponse response,
                                                         @PathVariable String frmDt, @PathVariable String toDt,
                                                         @PathVariable long regSeq, @PathVariable long areaSeq) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getRegionalRecoveryTrendReport(frmDt, toDt, regSeq, areaSeq, user);
        return new HttpEntity<byte[]>(bytes, getHeader("Regional Disbursement Report", bytes.length));
    }
    // Ended by Naveed - Date 07-03-2022

    @GetMapping("/account-monthly-report/{toDt}/{fileName}/{type}")
    @Timed
    public HttpEntity<byte[]> printAccountMonthlyReport(HttpServletResponse response,
                                                        @PathVariable String toDt, @PathVariable String fileName, @PathVariable long type) throws IOException {
        Map<String, String> resp = new HashMap<>();

        byte[] bytes = reportsService.getAccountsMonthlyReport(toDt, fileName, type);
        return new HttpEntity<byte[]>(bytes, getHeader("account-monthly-report", bytes.length));
    }

    // Added by Areeba - Date 30-03-2022
    @GetMapping("/audit-report/{frmDt}/{toDt}/{fileName}/{type}")
    @Timed
    public HttpEntity<byte[]> printAuditReport(HttpServletResponse response, @PathVariable String frmDt,
                                               @PathVariable String toDt, @PathVariable String fileName, @PathVariable long type) throws IOException {
        Map<String, String> resp = new HashMap<>();

        byte[] bytes = reportsService.getAuditReport(frmDt, toDt, fileName, type);
        return new HttpEntity<byte[]>(bytes, getHeader("audit-report", bytes.length));
    }
    //Ended by Areeba

    // Added BY Naveed - Date 10-05-2022
    // SCR - MCB Disbursement
    @GetMapping("/get-mcb-disbursement-funds/{frmDt}/{toDt}/{brnchSeq}/{type}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMcbDisburseFunds(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt, @PathVariable long brnchSeq,
                                                    @PathVariable String type, @PathVariable boolean isxls) throws IOException {
        Map<String, String> resp = new HashMap<>();
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMcbDisburseFunds(frmDt, toDt, brnchSeq, type, user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("print MCB disburse funds Report", bytes.length));
    }

    @GetMapping("/get-mcb-disbursement-reversal/{frmDt}/{toDt}/{brnchSeq}/{type}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printMcbDisburseReversal(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt, @PathVariable long brnchSeq,
                                                       @PathVariable String type, @PathVariable boolean isxls) throws IOException {
        Map<String, String> resp = new HashMap<>();
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getMcbDisburseReversalFunds(frmDt, toDt, brnchSeq, type, user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("print MCB disburse reversal Report", bytes.length));
    }

    /**
     * @Added, Naveed
     * @Date, 08-06-2022
     * @Description, NADRA Verisys Status Report
     */
    @GetMapping("/print-nadra-verisys/{frmDt}/{toDt}")
    @Timed
    public HttpEntity<byte[]> printNadraVerisys(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getNadraVerisysReport(user, frmDt, toDt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-nadra-verisys", bytes.length));
    }

    /**
     * @Added, Naveed
     * @Date, 14-06-2022
     * @Description, SCR - systemization Funds Request (IBFT and IFT funds transfer)
     */
    @GetMapping("/print-ibft-ift-report/{frmDt}/{toDt}/{type}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printIBFTAndIFTReport(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt, @PathVariable String type, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getIBFTAndIFTReport(user, frmDt, toDt, type, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("print-IBFT-and-IFT Report", bytes.length));
    }

    /**
     * @Added, Naveed
     * @Date, 14-06-2022
     * @Description, SCR - systemization Funds Request (Consolidated Report of Bank Funds)
     */
    @GetMapping("/print-consolidated-bank-funds/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printConsolidatedBankFundsReport(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt, @PathVariable boolean isxls) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getConsolidatedBankFundsReport(user, frmDt, toDt, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("print-consolidated-bank-funds", bytes.length));
    }

    @GetMapping("/consolidated-bank-funds-loader/{frmDt}/{toDt}")
    @Timed
    public ResponseEntity<?> consolidatedBankFundsLoader(HttpServletResponse response, @PathVariable String frmDt, @PathVariable String toDt) {
        Map<String, String> resp = new HashMap<>();

        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        List<MwFndsLodr> funds = reportsService.consolidatedBankFundsLoader(user, frmDt, toDt);
        if (funds != null) {
            resp = reportsService.consolidatedBankFundsPost();
        } else {
            resp.put("failed", "Funds already Posted");
        }
        return ResponseEntity.ok(resp);
    }


    /**
     * @Added, Naveed
     * @Date, 14-06-2022
     * @Description, SCR - systemization Funds Request (Bank Funds Request Data Loader)
     */
    @GetMapping("/print-bank-funds-request-data-loader/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printBankFundsRequestDataLoader(HttpServletResponse response, @PathVariable String frmDt,
                                                              @PathVariable String toDt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getBankFundsRequestDataLoader(frmDt, toDt, user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("bank-funds-request-data-loader", bytes.length));
    }

    /**
     * @Added, Naveed
     * @Date, 24-06-2022
     * @Description, SCR - systemization Funds Request (Khushali Funds Letter)
     */
    @GetMapping("/print-khushali-funds-letter/{frmDt}/{toDt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printKhushaliFundsLetter(HttpServletResponse response, @PathVariable String frmDt,
                                                       @PathVariable String toDt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getKhushaliFundsLetter(frmDt, toDt, user, isxls);
        return new HttpEntity<byte[]>(bytes, getHeader("khushali-funds-letter", bytes.length));
    }

    /**
     * @Added, Naveed
     * @Date, 29-06-2022
     * @Description, Export Data Utility
     */
    @GetMapping("/print-export-csv/{frmDt}/{toDt}/{type}")
    @Timed
    public List<Object[]> printExportCsvFile(HttpServletResponse response, @PathVariable String frmDt,
                                             @PathVariable String toDt, @PathVariable String type) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return reportsService.getCsvExport(frmDt, toDt, type);
    }

    // Added by Areeba - SCR - HR Travelling
    @GetMapping("/print-trvlng_dtls/{monthDt}")
    @Timed
    public List<Object[]> printExportTrvlngDtlsCsvFile(HttpServletResponse response, @PathVariable String monthDt
    ) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return reportsService.exportTrvlngDtls(monthDt);
    }

    @GetMapping("/print-trvlng_for_harmony/{monthDt}")
    @Timed
    public List<Object[]> printExportTrvlngHarmonyCsvFile(HttpServletResponse response, @PathVariable String monthDt
    ) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        return reportsService.exportTrvlngHarmony(monthDt);
    }
    // Ended by Areeba


    /**
     * @Added, Naveed
     * @Date, 26-08-2022
     * @Description, Finance Reports
     */
    @GetMapping("/print-sector-wise-portfolio/{todt}")
    @Timed
    public HttpEntity<byte[]> printSectorWisePortfolio(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getSectorWisePortfolio(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-sector-wise-portfolio", bytes.length));
    }

    @GetMapping("/print-gender-wise-portfolio/{todt}")
    @Timed
    public HttpEntity<byte[]> printGenderWisePortfolio(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getGenderAndAgeWisePortfolio(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-gender-wise-portfolio", bytes.length));
    }

    //Added by Areeba - Finance Reports
    @GetMapping("/print-branch-wise-portfolio/{todt}")
    @Timed
    public HttpEntity<byte[]> printBranchWisePortfolio(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBranchWisePortfolio(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-branch-wise-portfolio", bytes.length));
    }

    @GetMapping("/print-branch-wise-par/{todt}")
    @Timed
    public HttpEntity<byte[]> printBranchWisePar(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getBranchWisePar(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-branch-wise-par", bytes.length));
    }

    @GetMapping("/print-product-wise-portfolio/{todt}")
    @Timed
    public HttpEntity<byte[]> printProductWisePortfolio(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getProductWisePortfolio(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-product-wise-portfolio", bytes.length));
    }

    @GetMapping("/print-product-wise-par/{todt}")
    @Timed
    public HttpEntity<byte[]> printProductWisePar(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getProductWisePar(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-product-wise-par", bytes.length));
    }

    @GetMapping("/print-province-wise-ost-portfolio/{todt}")
    @Timed
    public HttpEntity<byte[]> printProvinceWiseOutstandingPortfolio(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getProvinceWiseOutstandingPortfolio(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-province-wise-ost-portfolio", bytes.length));
    }

    @GetMapping("/print-loan-cycle-wise-portfolio/{todt}")
    @Timed
    public HttpEntity<byte[]> printLoanCycleWisePortfolio(HttpServletResponse response, @PathVariable String todt) {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();

        byte[] bytes = reportsService.getLoanCycleWisePortfolio(user, todt);
        return new HttpEntity<byte[]>(bytes, getHeader("print-loan-cycle-wise-portfolio", bytes.length));
    }

    //Ended by Areeba

    /**
     * @Ended, Naveed
     * @Date, 26-08-2022
     * @Description, Finance Reports
     */

    /*Added by Areeba
    Dated 9-9-2022
    Recovery Report
    */
    @GetMapping("/print-adc-failed-transaction/{logDt}")
    @Timed
    public List<Object[]> printAdcFailedTransactions(@PathVariable String logDt) {
        List<Object[]> result = reportsService.getAdcFailedTransactions(logDt);
        return result;
    }

//    @GetMapping("/adc-data-sharing-test")
//    @Timed
//    public String printAdcDataSharingTest() throws Exception {
//        adcAutoDataSharing.generatePdfFile();
//        return "success";
//    }

    // Added by Areeba - Date 07-11-2022
    @GetMapping("/print-sampling-audit/{asofDt}/{brnchCd}/{type}")
    @Timed
    public HttpEntity<byte[]> printSamplingAuditReport(HttpServletResponse response, @PathVariable String asofDt,
                                                       @PathVariable Long brnchCd, @PathVariable long type) throws IOException {
        Map<String, String> resp = new HashMap<>();
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = reportsService.getSamplingAudit(asofDt, brnchCd, type, user);
        return new HttpEntity<byte[]>(bytes, getHeader("audit-report", bytes.length));
    }
    //Ended by Areeba

    // Added by Areeba
    @GetMapping("/adc-data-sharing-reports/{reportNm}/{todt}/{isxls}")
    @Timed
    public HttpEntity<byte[]> printAdcDataSharingReports(HttpServletResponse response, @PathVariable String reportNm, @PathVariable String todt, @PathVariable boolean isxls) throws IOException {
        String user = SecurityContextHolder.getContext().getAuthentication().getName();
        byte[] bytes = null;
        if (reportNm.equals("jazzcash_info_dues_"))
            bytes = reportsService.getClntInfoJazzDueReport(user, isxls);
        else if (reportNm.equals("jazzcash_upload_dues_"))
            bytes = reportsService.getClientUpldDuesReport(user, isxls);
        else if (reportNm.equals("easypaisa_dues_"))
            bytes = reportsService.getEasyPaisaDuesReport(user, isxls);
        else if (reportNm.equals("ubl_omni_dues_"))
            bytes = reportsService.getUblOmniDuesReport(user, isxls);
        else if (reportNm.equals("hbl_konnect_dues_"))
            bytes = reportsService.getHblConnectReport(user, todt, isxls);
        else if (reportNm.equals("abl_loan_recovery_dues_"))
            bytes = reportsService.getAblLoanDuesReport(user, isxls);
        else if (reportNm.equals("mcb_collect_plus_dues_"))
            bytes = reportsService.getMcbCollectReport(user, isxls);
        else if (reportNm.equals("nadra_dues_"))
            bytes = reportsService.getNadraDuesReport(user, isxls);

        return new HttpEntity<byte[]>(bytes, getHeader("ADC Data Sharing", bytes.length));
    }
}




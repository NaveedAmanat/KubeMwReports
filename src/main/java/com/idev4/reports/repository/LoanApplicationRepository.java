package com.idev4.reports.repository;

import com.idev4.reports.domain.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    @Query(value = "SELECT l.LOAN_APP_SEQ, l.LOAN_ID, l.RQSTD_LOAN_AMT, l.APRVD_LOAN_AMT, l.CLNT_SEQ , p.PRD_NM, l.RCMND_LOAN_AMT, (select ref_cd_dscr from mw_ref_cd_val where ref_cd_seq=l.loan_app_sts) as status, c.NATR_OF_DIS_KEY, c.CLNT_STS_KEY, c.RES_TYP_KEY, c.DIS_FLG, c.SLF_PDC_FLG, c.CRNT_ADDR_PERM_FLG, c.PH_NUM, c.FRST_NM, c.LAST_NM "
            + "FROM MW_loan_App l, " + "MW_clnt c, " + "MW_PRD p WHERE " + "l.CLNT_SEQ = c.CLNT_SEQ "
            + "AND l.PRD_SEQ =p.PRD_SEQ " + "AND l.LOAN_APP_STS=455", nativeQuery = true)
    List<Map<String, ?>> getAllApplications();

    @Query(value = "SELECT LOAN_APP_SEQ,LOAN_ID,RQSTD_LOAN_AMT, APRVD_LOAN_AMT ,PR.PRD_ID,PO.PORT_NM, RCMND_LOAN_AMT,LOAN_APP_ID,C.CLNT_ID,C.FRST_NM,LAST_NM,B.BRNCH_NM,PR.PRD_NM "
            + "FROM MW_LOAN_APP L, MW_CLNT C ,MW_PORT PO,MW_BRNCH B,MW_PRD PR " + "WHERE L.CLNT_SEQ=C.CLNT_SEQ AND L.LOAN_APP_STS=455 "
            + "AND L.PORT_SEQ=PO.PORT_SEQ AND PO.BRNCH_SEQ=B.BRNCH_SEQ " + "AND L.PRD_SEQ=PR.PRD_SEQ "
            + "AND LOAN_APP_SEQ=?", nativeQuery = true)
    public Map<String, ?> findOneByLoanAppSeq(long loanAppSeq);

    public LoanApplication findByLoanAppSeq(long loanAppSeq);

    public List<LoanApplication> findAllByClntSeqAndLoanAppSts(long clntSeq, long loanAppSts);

    public List<LoanApplication> findAllByLoanAppSeqInAndLoanAppStsIn(List<Long> loanSeq, List<Long> sts);

    public LoanApplication findOneByClntSeq(long seq);

    @Query(value = "select *\r\n"
            + "from mw_loan_app where loan_app_seq=prnt_loan_app_seq and  clnt_seq = ?  and loan_cycl_num = ?", nativeQuery = true)
    public LoanApplication findOneByClntSeqAndLoanCyclNum(long seq, long loanCyclNum);

    @Query(value = "select distinct mla.*\r\n" + "from mw_rcvry_trx mrt\r\n"
            + "join  mw_rcvry_dtl mrd on mrd.rcvry_trx_seq = mrt.rcvry_trx_seq\r\n"
            + "join mw_pymt_sched_dtl psd on psd.pymt_sched_dtl_seq = mrd.pymt_sched_dtl_seq\r\n"
            + "join MW_PYMT_SCHED_HDR mpsh on mpsh.PYMT_SCHED_HDR_SEQ = psd.PYMT_SCHED_HDR_SEQ\r\n"
            + "join mw_loan_app mla on mla.LOAN_APP_SEQ = mpsh.LOAN_APP_SEQ\r\n"
            + "where mrt.rcvry_trx_seq = :rcvryTrxSeq and mla.LOAN_APP_STS=:loanAppSts", nativeQuery = true)
    public List<LoanApplication> findAllPaidLoanAppByTrx(@Param("rcvryTrxSeq") long rcvryTrxSeq,
                                                         @Param("loanAppSts") long loanAppSts);

    public LoanApplication findTop1ByLoanAppSeqOrderByLastUpdDtDesc(long loanAppSeq);

    @Query(value = "select * from mw_loan_app ap\r\n" + "where ap.clnt_seq=:clntSeq\r\n"
            + "and ap.loan_cycl_num=(select max(loan_cycl_num) from mw_loan_app where clnt_seq=ap.clnt_seq )", nativeQuery = true)
    public List<LoanApplication> findMaxLoanCyclLoansForClient(@Param("clntSeq") long clntSeq);

    @Query(value = "select * from mw_loan_app ap\r\n" + "where ap.clnt_seq=:clntSeq\r\n"
            + "and ap.loan_cycl_num=(select max(loan_cycl_num) from mw_loan_app where clnt_seq=ap.clnt_seq ) and ap.loan_app_seq!=ap.prnt_loan_app_seq", nativeQuery = true)
    public List<LoanApplication> findMaxLoanCyclAssocLoansForClient(@Param("clntSeq") long clntSeq);

    @Query(value = "select * from mw_loan_app ap\r\n" + "where  ap.clnt_seq=:clntSeq\r\n"
            + "and ap.loan_cycl_num=(select max(loan_cycl_num) from mw_loan_app where clnt_seq=ap.clnt_seq ) and ap.loan_app_seq!=ap.prnt_loan_app_seq and ap.loan_app_sts_dt>=to_date(:dthDt,'dd-mm-yyyy'", nativeQuery = true)
    public List<LoanApplication> findMaxLoanCyclAssocLoansForClientCompletedAfterDeathDt(@Param("clntSeq") long clntSeq,
                                                                                         @Param("dthDt") String dthDt);
}

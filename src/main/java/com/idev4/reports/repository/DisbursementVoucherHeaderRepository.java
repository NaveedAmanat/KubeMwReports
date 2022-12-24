package com.idev4.reports.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DisbursementVoucherHeaderRepository extends JpaRepository<DisbursementVoucherHeader, Long> {

    @Query(value = "SELECT DVH.DSBMT_HDR_SEQ, DVH.DSBMT_ID, DVH.DSBMT_DT, DVH.LOAN_APP_SEQ, DVH.DSBMT_STS_KEY,DVD.DSBMT_DTL_KEY, DVD.INSTR_NUM, DVD.AMT, DVD.PYMT_TYPS_SEQ\r\n"
            + ",TYP_SEQ, TYP_ID, TYP_STR, GL_ACCT_NUM, TYP_STS_KEY, TYP_CTGRY_KEY\r\n"
            + "FROM MW_DSBMT_VCHR_HDR DVH,MW_DSBMT_VCHR_DTL DVD,MW_TYPS T\r\n"
            + "WHERE DVH.DSBMT_HDR_SEQ=DVD.DSBMT_HDR_SEQ(+) AND DVD.PYMT_TYPS_SEQ=T.TYP_SEQ\r\n"
            + "AND DVH.LOAN_APP_SEQ=1 AND DVH.DSBMT_VCHR_TYP=2", nativeQuery = true)
    List<Map<String, ?>> getDisbursementVoucherByLoanAppSeq(long paySchedChrgSeq);

    @Query(value = "SELECT DVH.DSBMT_HDR_SEQ, DVH.DSBMT_ID, DVH.DSBMT_DT, DVH.LOAN_APP_SEQ, DVH.DSBMT_STS_KEY,DVD.DSBMT_DTL_KEY, DVD.INSTR_NUM, DVD.AMT, DVD.PYMT_TYPS_SEQ\r\n"
            + ",TYP_SEQ, TYP_ID, TYP_STR, GL_ACCT_NUM, TYP_STS_KEY, TYP_CTGRY_KEY\r\n"
            + "FROM MW_DSBMT_VCHR_HDR DVH,MW_DSBMT_VCHR_DTL DVD,MW_TYPS T\r\n"
            + "WHERE DVH.DSBMT_HDR_SEQ=DVD.DSBMT_HDR_SEQ(+) AND DVD.PYMT_TYPS_SEQ=T.TYP_SEQ\r\n"
            + "AND DVH.LOAN_APP_SEQ=1 AND DVH.DSBMT_VCHR_TYP=1", nativeQuery = true)
    List<Map<String, ?>> getAgencyByLoanAppSeq(long paySchedChrgSeq);

    public DisbursementVoucherHeader findOneByLoanAppSeq(long loanAppSeq);

    public List<DisbursementVoucherHeader> findAllByLoanAppSeq(long loanAppSeq);

    public DisbursementVoucherHeader findOneByLoanAppSeqAndDsbmtVchrTyp(long loanAppSeq, int type);

    @Query(value = "select dvd.amt,t.typ_str,t.typ_id,t.gl_acct_num,t.typ_seq from  mw_dsbmt_vchr_dtl dvd\r\n"
            + "join mw_typs t on t.typ_seq=dvd.pymt_typs_seq \r\n"
            + "join mw_dsbmt_vchr_hdr dvh on dvh.dsbmt_hdr_seq=dvd.dsbmt_hdr_seq \r\n"
            + "where dvh.loan_app_seq=:loanAppSeq and dvh.dsbmt_vchr_typ=:type", nativeQuery = true)
    List<Map<String, ?>> getVouchersbyLoanAppAndType(@Param("loanAppSeq") long loanAppSeq, @Param("type") long type);

    @Query(value = "select hdr.*,app.loan_app_sts,app.LOAN_APP_SEQ \r\n" + "from mw_dsbmt_vchr_hdr hdr \r\n"
            + "join mw_loan_app app on app.loan_app_seq = hdr.loan_app_seq  and app.loan_app_seq=app.prnt_loan_app_seq\r\n"
            + "join mw_clnt clnt on app.clnt_seq=clnt.clnt_seq \r\n"
            + "join mw_prd p on p.prd_seq=app.prd_seq  \r\n"
            + "join mw_ref_cd_val val on val.REF_CD_SEQ=p.PRD_TYP_KEY  and val.ref_cd !='1165'\r\n"
            + "join mw_ref_cd_grp grp on grp.ref_cd_grp_seq = val.ref_cd_grp_key  and grp.ref_cd_grp = '0161'   \r\n"
            + "where clnt.clnt_seq=:clientId  and hdr.DSBMT_VCHR_TYP=0 \r\n"
            + "and app.LOAN_CYCL_NUM=(select max(LOAN_CYCL_NUM) from mw_loan_app where clnt_seq=:clientId)", nativeQuery = true)
    public DisbursementVoucherHeader getDisbursementVoucherHeaderForClientsActiveLoan(@Param("clientId") long clientId);

}

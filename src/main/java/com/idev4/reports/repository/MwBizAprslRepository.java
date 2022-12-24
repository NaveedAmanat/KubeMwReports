package com.idev4.reports.repository;

import com.idev4.reports.domain.MwBizAprsl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Spring Data JPA repository for the MwBizAprsl entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MwBizAprslRepository extends JpaRepository<MwBizAprsl, Long> {

    public List<MwBizAprsl> findAll();

    public MwBizAprsl findOneByMwLoanApp(long loanAppSeq);

    public MwBizAprsl findOneByBizAprslSeq(long loanAppSeq);

    // public MwBizAprsl findOneByMwLoanApp( long loanAppSeq );

    @Query(value = "select ba.biz_aprsl_seq,ba.yrs_in_biz,ba.mnth_in_biz,biz.ref_cd_dscr biz,prsn.ref_cd_dscr prsn,acty.ref_cd_dscr acty\n"
            + "from mw_biz_aprsl  ba\n" + "join mw_ref_cd_val biz on ba.biz_own=biz.ref_cd_seq  and biz.del_flg=0\n"
            + "join mw_ref_cd_val prsn on ba.prsn_run_the_biz=prsn.ref_cd_seq and prsn.del_flg=0\n"
            + "join mw_ref_cd_val acty on ba.acty_key=acty.ref_cd_seq  and acty.del_flg=0\n"
            + "where ba.loan_app_seq=? ", nativeQuery = true)
    public Map<String, Object> findBizAprsByLoanAppSeq(@Param("loanAppSeq") long loanAppSeq);

}

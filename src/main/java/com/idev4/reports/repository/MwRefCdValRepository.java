package com.idev4.reports.repository;

import com.idev4.reports.domain.MwRefCdVal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the MwRefCdVal entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MwRefCdValRepository extends JpaRepository<MwRefCdVal, Long> {

    public MwRefCdVal findOneByRefCd(String refCd);

    @Query(value = "select val.* \n" + "from mw_ref_cd_val val\n"
            + "join mw_ref_cd_grp rcg on rcg.ref_cd_grp_seq=val.ref_cd_grp_key and rcg.ref_cd_grp='0161' \n"
            + "join mw_prd p on p.prd_typ_key=val.ref_cd_seq \n"
            + "join mw_loan_app app on p.prd_seq=app.prd_seq \n"
            + "where app.loan_app_seq=:loanAppSeq", nativeQuery = true)
    public MwRefCdVal findProductTypeByLoanApp(@Param("loanAppSeq") long loanAppSeq);

    @Query(value = "select val.ref_cd_seq, val.eff_start_dt, val.ref_cd_grp_key, val.ref_cd, val.ref_cd_dscr, val.ref_cd_sort_ordr, val.ref_cd_active_flg, val.crtd_by, val.crtd_dt, val.last_upd_by, val.last_upd_dt, val.del_flg, val.eff_end_dt \n"
            + "from mw_ref_cd_val val\n"
            + "join mw_ref_cd_grp rcg on rcg.ref_cd_grp_seq=val.ref_cd_grp_key and rcg.ref_cd_grp='0106' \n"
            + "join mw_loan_app app on app.LOAN_APP_STS=val.REF_CD_SEQ and \n"
            + "where  and app.loan_app_seq=:loanAppSeq ", nativeQuery = true)
    public MwRefCdVal findAppTypeByLoanApp(@Param("loanAppSeq") long loanAppSeq);

    @Query(value = "select val.ref_cd_seq, val.eff_start_dt, val.ref_cd_grp_key, val.ref_cd, val.ref_cd_dscr, val.ref_cd_sort_ordr, val.ref_cd_active_flg, val.crtd_by, val.crtd_dt, val.last_upd_by, val.last_upd_dt, val.del_flg, val.eff_end_dt\n"
            + "from mw_ref_cd_val val\n"
            + "join mw_ref_cd_grp grp on grp.ref_cd_grp_seq = val.ref_cd_grp_key  and grp.ref_cd_grp = :refCdGrp\n"
            + "where  val.ref_cd =:refCd", nativeQuery = true)
    public MwRefCdVal findRefCdByGrpAndVal(@Param("refCdGrp") String refCdGrp, @Param("refCd") String refCd);

    public MwRefCdVal findOneByRefCdSeq(long seq);
}

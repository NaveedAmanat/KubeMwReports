package com.idev4.reports.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

/**
 * Added By Naveed Date - 23-01-2022
 * SCR - Mobile Wallet Control
 */
@Entity
@Table(name = "MW_STP_CNFIG_VAL")
public class MwStpCnfigVal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STP_VAL_SEQ")
    private Long stpValSeq;

    @Column(name = "STP_GRP_CD")
    private String stpGrpCd;

    @Column(name = "STP_VAL_CD")
    private String stpValCd;

    @Column(name = "REF_CD_VAL_DSCR")
    private String refCdValDscr;

    @Column(name = "REF_CD_VAL_SHRT_DSCR")
    private String refCdValShrtDscr;

    @Column(name = "SORT_ORDR")
    private Long sortOrder;

    @Column(name = "CRTD_BY")
    private String crtdBy;

    @Column(name = "CRTD_DT")
    private Instant crtdDt;

    @Column(name = "LAST_UPD_BY")
    private String lastUpdBy;

    @Column(name = "LAST_UPD_DT")
    private Instant lastUpdDt;

    @Column(name = "DEL_FLG")
    private Boolean delFlg;

    @Column(name = "EFF_START_DT")
    private Instant effStartDt;

    @Column(name = "EFF_END_DT")
    private Instant effEndDt;

    @Column(name = "CRNT_REC_FLG")
    private Boolean crntRecFlg;

    public MwStpCnfigVal() {
    }

    public MwStpCnfigVal(Long stpValSeq, String stpGrpCd, String stpValCd, String refCdValDscr, String refCdValShrtDscr, Long sortOrder, String crtdBy, Instant crtdDt, String lastUpdBy, Instant lastUpdDt, Boolean delFlg, Instant effStartDt, Instant effEndDt, Boolean crntRecFlg) {
        this.stpValSeq = stpValSeq;
        this.stpGrpCd = stpGrpCd;
        this.stpValCd = stpValCd;
        this.refCdValDscr = refCdValDscr;
        this.refCdValShrtDscr = refCdValShrtDscr;
        this.sortOrder = sortOrder;
        this.crtdBy = crtdBy;
        this.crtdDt = crtdDt;
        this.lastUpdBy = lastUpdBy;
        this.lastUpdDt = lastUpdDt;
        this.delFlg = delFlg;
        this.effStartDt = effStartDt;
        this.effEndDt = effEndDt;
        this.crntRecFlg = crntRecFlg;
    }

    public Long getStpValSeq() {
        return stpValSeq;
    }

    public void setStpValSeq(Long stpValSeq) {
        this.stpValSeq = stpValSeq;
    }

    public String getStpGrpCd() {
        return stpGrpCd;
    }

    public void setStpGrpCd(String stpGrpCd) {
        this.stpGrpCd = stpGrpCd;
    }

    public String getStpValCd() {
        return stpValCd;
    }

    public void setStpValCd(String stpValCd) {
        this.stpValCd = stpValCd;
    }

    public String getRefCdValDscr() {
        return refCdValDscr;
    }

    public void setRefCdValDscr(String refCdValDscr) {
        this.refCdValDscr = refCdValDscr;
    }

    public String getRefCdValShrtDscr() {
        return refCdValShrtDscr;
    }

    public void setRefCdValShrtDscr(String refCdValShrtDscr) {
        this.refCdValShrtDscr = refCdValShrtDscr;
    }

    public Long getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Long sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCrtdBy() {
        return crtdBy;
    }

    public void setCrtdBy(String crtdBy) {
        this.crtdBy = crtdBy;
    }

    public Instant getCrtdDt() {
        return crtdDt;
    }

    public void setCrtdDt(Instant crtdDt) {
        this.crtdDt = crtdDt;
    }

    public String getLastUpdBy() {
        return lastUpdBy;
    }

    public void setLastUpdBy(String lastUpdBy) {
        this.lastUpdBy = lastUpdBy;
    }

    public Instant getLastUpdDt() {
        return lastUpdDt;
    }

    public void setLastUpdDt(Instant lastUpdDt) {
        this.lastUpdDt = lastUpdDt;
    }

    public Boolean getDelFlg() {
        return delFlg;
    }

    public void setDelFlg(Boolean delFlg) {
        this.delFlg = delFlg;
    }

    public Instant getEffStartDt() {
        return effStartDt;
    }

    public void setEffStartDt(Instant effStartDt) {
        this.effStartDt = effStartDt;
    }

    public Instant getEffEndDt() {
        return effEndDt;
    }

    public void setEffEndDt(Instant effEndDt) {
        this.effEndDt = effEndDt;
    }

    public Boolean getCrntRecFlg() {
        return crntRecFlg;
    }

    public void setCrntRecFlg(Boolean crntRecFlg) {
        this.crntRecFlg = crntRecFlg;
    }
}

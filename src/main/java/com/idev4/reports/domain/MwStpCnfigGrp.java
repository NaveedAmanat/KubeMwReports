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
@Table(name = "MW_STP_CNFIG_GRP")
public class MwStpCnfigGrp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STP_GRP_SEQ")
    private Long stpGrpSeq;

    @Column(name = "STP_GRP_CD")
    private String stpGrpCd;

    @Column(name = "REF_CD_GRP_NM")
    private String refCdGrpNm;

    @Column(name = "REF_CD_GRP_SHRT_NM")
    private String refCdGrpShrtNm;

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

    public MwStpCnfigGrp(Long stpGrpSeq) {
        this.stpGrpSeq = stpGrpSeq;
    }

    public MwStpCnfigGrp(Long stpGrpSeq, String stpGrpCd, String refCdGrpNm, String refCdGrpShrtNm, String crtdBy, Instant crtdDt, String lastUpdBy, Instant lastUpdDt, Boolean delFlg, Instant effStartDt, Instant effEndDt, Boolean crntRecFlg) {
        this.stpGrpSeq = stpGrpSeq;
        this.stpGrpCd = stpGrpCd;
        this.refCdGrpNm = refCdGrpNm;
        this.refCdGrpShrtNm = refCdGrpShrtNm;
        this.crtdBy = crtdBy;
        this.crtdDt = crtdDt;
        this.lastUpdBy = lastUpdBy;
        this.lastUpdDt = lastUpdDt;
        this.delFlg = delFlg;
        this.effStartDt = effStartDt;
        this.effEndDt = effEndDt;
        this.crntRecFlg = crntRecFlg;
    }

    public Long getStpGrpSeq() {
        return stpGrpSeq;
    }

    public void setStpGrpSeq(Long stpGrpSeq) {
        this.stpGrpSeq = stpGrpSeq;
    }

    public String getStpGrpCd() {
        return stpGrpCd;
    }

    public void setStpGrpCd(String stpGrpCd) {
        this.stpGrpCd = stpGrpCd;
    }

    public String getRefCdGrpNm() {
        return refCdGrpNm;
    }

    public void setRefCdGrpNm(String refCdGrpNm) {
        this.refCdGrpNm = refCdGrpNm;
    }

    public String getRefCdGrpShrtNm() {
        return refCdGrpShrtNm;
    }

    public void setRefCdGrpShrtNm(String refCdGrpShrtNm) {
        this.refCdGrpShrtNm = refCdGrpShrtNm;
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

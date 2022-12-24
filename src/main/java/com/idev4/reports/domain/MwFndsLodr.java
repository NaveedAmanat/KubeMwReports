package com.idev4.reports.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "mw_fnds_lodr")
public class MwFndsLodr implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "branch_seq")
    private Long branchSeq;

    @Column(name = "funds")
    private Long funds;

    @Column(name = "crtd_by")
    private String crtdBy;

    @Column(name = "crtd_dt")
    private Instant crtdDt;

    public Long getBranchSeq() {
        return branchSeq;
    }

    public void setBranchSeq(Long branchSeq) {
        this.branchSeq = branchSeq;
    }

    public Long getFunds() {
        return funds;
    }

    public void setFunds(Long funds) {
        this.funds = funds;
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

}
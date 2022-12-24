package com.idev4.reports.ids;

import java.io.Serializable;

public class MwClntRelId implements Serializable {

    //(clnt_rel_SEQ,rel_typ_flg, LOAN_APP_sEQ);
    private static final long serialVersionUID = 1L;

    public Long clntRelSeq;
    public Long relTypFlg;
    public Long loanAppSeq;

}

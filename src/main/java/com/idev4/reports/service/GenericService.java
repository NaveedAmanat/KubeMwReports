package com.idev4.reports.service;

import com.idev4.reports.domain.Clients;
import com.idev4.reports.domain.MwDthRpt;
import com.idev4.reports.dto.MwPrdDTO;
import com.idev4.reports.repository.ClientRepository;
import com.idev4.reports.repository.DisbursementVoucherHeader;
import com.idev4.reports.repository.DisbursementVoucherHeaderRepository;
import com.idev4.reports.repository.MwDthRptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class GenericService {

    @Autowired
    EntityManager em;

    @Autowired
    MwDthRptRepository mwDthRptRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    DisbursementVoucherHeaderRepository disbursementVoucherHeaderRepository;

    public List<MwPrdDTO> getBranchPrds(String userId) {

        Query q = em
                .createNativeQuery("select prd.prd_seq,prd.prd_nm\r\n" + "  from mw_prd prd\r\n"
                        + "  join mw_brnch_prd_rel prel on prel.prd_seq=prd.prd_seq \r\n"
                        + "  join mw_brnch brnch on brnch.brnch_seq=prel.brnch_seq \r\n"
                        + "  join mw_brnch_emp_rel erel on erel.brnch_seq=brnch.brnch_seq  and erel.del_flg=0\r\n"
                        + "  join mw_emp emp on emp.emp_seq=erel.emp_seq\r\n" + "where emp.emp_lan_id=:userId")
                .setParameter("userId", userId);
        List<Object[]> results = q.getResultList();
        List<MwPrdDTO> prds = new ArrayList();
        results.forEach(p -> {
            MwPrdDTO prd = new MwPrdDTO();
            prd.prdId = p[0].toString();
            prd.prdNm = p[1].toString();
            prds.add(prd);
        });

        return prds;
    }

    public MwDthRpt getClntDth(String clientId) {
        Clients clnt = clientRepository.findOneByClntSeq(Long.parseLong(clientId));
        return mwDthRptRepository.findTopByClntSeqOrderByDtOfDthDesc(clnt.getClntSeq());
    }

    public DisbursementVoucherHeader getDisbursementVoucherHeaderForClientsActiveLoan(String clientId) {
        return disbursementVoucherHeaderRepository.getDisbursementVoucherHeaderForClientsActiveLoan(Long.parseLong(clientId));
    }

}

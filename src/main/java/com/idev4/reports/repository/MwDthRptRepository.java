package com.idev4.reports.repository;

import com.idev4.reports.domain.MwDthRpt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MwDthRptRepository extends JpaRepository<MwDthRpt, Long> {

    public MwDthRpt findTopByClntSeqOrderByDtOfDthDesc(long clntSeq);

}

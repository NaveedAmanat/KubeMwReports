package com.idev4.reports.repository;

import com.idev4.reports.domain.MwLedgerHeads;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MwLedgerHeadsRepository extends JpaRepository<MwLedgerHeads, String> {

    public MwLedgerHeads findOneByCustSegments(String custSegments);
}

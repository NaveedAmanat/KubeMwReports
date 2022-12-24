package com.idev4.reports.repository;

import com.idev4.reports.domain.MwStpCnfigVal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Added By Naveed Date - 23-01-2022
 * SCR - Mobile Wallet Control
 */
@Repository
public interface MwStpCnfigValRepository extends JpaRepository<MwStpCnfigVal, Long> {

    List<MwStpCnfigVal> findAllByStpGrpCdAndCrntRecFlgOrderBySortOrderAsc(String grpCd, boolean flag);
}

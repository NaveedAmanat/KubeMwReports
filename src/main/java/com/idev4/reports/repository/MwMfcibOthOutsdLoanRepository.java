package com.idev4.reports.repository;

import com.idev4.reports.domain.MwMfcibOthOutsdLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the MwMfcibOthOutsdLoan entity.
 */

@Repository
public interface MwMfcibOthOutsdLoanRepository extends JpaRepository<MwMfcibOthOutsdLoan, Long> {

    public List<MwMfcibOthOutsdLoan> findAllByLoanAppSeq(long loanAppSeq);

    public MwMfcibOthOutsdLoan findOneByOthOutsdLoanSeq(long seq);
}

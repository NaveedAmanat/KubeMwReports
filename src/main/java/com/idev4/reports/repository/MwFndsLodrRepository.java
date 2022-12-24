package com.idev4.reports.repository;

import com.idev4.reports.domain.MwFndsLodr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MwFndsLodrRepository extends JpaRepository<MwFndsLodr, Long> {

}

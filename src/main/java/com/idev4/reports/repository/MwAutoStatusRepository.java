package com.idev4.reports.repository;

import com.idev4.reports.domain.MwAutoListStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MwAutoStatusRepository extends JpaRepository<MwAutoListStatus, Long> {
}

package com.idev4.reports.repository;

import com.idev4.reports.domain.MwRefCdGrp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the MwRefCdGrp entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MwRefCdGrpRepository extends JpaRepository<MwRefCdGrp, Long> {

    public MwRefCdGrp findOneByRefCdGrpSeq(long seq);

    public MwRefCdGrp findOneByRefCdGrpName(String name);

    public List<MwRefCdGrp> findAll();

}

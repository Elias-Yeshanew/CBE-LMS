package com.cbe.lms.lease.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cbe.lms.lease.Model.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
    // Add any custom query methods if needed
}

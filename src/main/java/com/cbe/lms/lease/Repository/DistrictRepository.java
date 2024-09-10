package com.cbe.lms.lease.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cbe.lms.lease.Model.District;
import com.cbe.lms.lease.Model.Region;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByRegion(Region region);

    long count();

    long countByRegion(Region region); // Add this method
    // Add any other custom query methods if needed
}

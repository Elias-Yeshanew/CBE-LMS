package com.cbe.lms.lease.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cbe.lms.lease.Model.Branch;
import com.cbe.lms.lease.Model.District;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByDistrict(District district);

    Branch findByPoliticalRegion(String politicalRegion);

    // Optional<Branch> findById(Branch branchId);

    long count();

    long countByDistrict(District district); // Add this method

    @Query("SELECT l FROM Branch l WHERE l.district.districtId = :districtId")
    Page<Branch> findBranchbyDistrict(@Param("districtId") Long districtId, PageRequest pageable);

    // Add any other custom query methods if needed
}
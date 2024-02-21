package com.example.demo.lease.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.District;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByDistrict(District district);

    Branch findByPoliticalRegion(String politicalRegion);

    // Optional<Branch> findById(Branch branchId);

    long count();

    long countByDistrict(District district); // Add this method

    // Add any other custom query methods if needed
}
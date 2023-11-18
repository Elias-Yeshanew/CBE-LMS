package com.example.demo.lease.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.District;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByDistrict(District district);

    Branch findByPoliticalRegion(String politicalRegion);

    // Add any other custom query methods if needed
}
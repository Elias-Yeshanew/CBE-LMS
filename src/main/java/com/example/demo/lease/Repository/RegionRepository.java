package com.example.demo.lease.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.lease.Model.Region;

public interface RegionRepository extends JpaRepository<Region, Long> {
    // Add any custom query methods if needed
}

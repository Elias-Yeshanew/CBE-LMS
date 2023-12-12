package com.example.demo.lease.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.lease.Model.PoliticalRegion;

public interface PoliticalRegionRepository extends JpaRepository<PoliticalRegion, Long> {
    // You can add custom query methods here if needed
}
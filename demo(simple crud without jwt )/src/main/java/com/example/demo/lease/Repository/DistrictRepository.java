package com.example.demo.lease.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.lease.Model.District;
import com.example.demo.lease.Model.Region;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByRegion(Region region);
    // Add any other custom query methods if needed
}

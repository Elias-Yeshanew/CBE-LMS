package com.example.demo.lease.Service;

import org.springframework.stereotype.Service;

import com.example.demo.lease.Model.Region;
import com.example.demo.lease.Repository.RegionRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class RegionService {
    private final RegionRepository regionRepository;

    public Region addNewRegion(Region region) throws Exception {
        return regionRepository.save(region);
    }

}

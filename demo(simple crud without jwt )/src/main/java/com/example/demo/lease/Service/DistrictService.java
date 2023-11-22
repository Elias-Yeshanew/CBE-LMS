package com.example.demo.lease.Service;

import org.springframework.stereotype.Service;

import com.example.demo.lease.Repository.DistrictRepository;

@Service
public class DistrictService {

    private final DistrictRepository districtRepository;

    public DistrictService(DistrictRepository districtRepository) {
        this.districtRepository = districtRepository;
    }

    public long getDistrictCount() {
        return districtRepository.count();
    }

}

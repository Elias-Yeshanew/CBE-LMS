package com.example.demo.lease.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.lease.Model.District;
import com.example.demo.lease.Model.Region;
import com.example.demo.lease.Repository.DistrictRepository;
import com.example.demo.lease.Repository.RegionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;

    public long getDistrictCount() {
        return districtRepository.count();
    }

    public List<District> saveAllDistricts(List<District> districts) {
        return districtRepository.saveAll(districts);
    }

    // public District addNewDistrict(District district) throws Exception {
    // return districtRepository.save(district);
    // }

    public District addNewDistrict(District district) throws Exception {
        // Load the Region from the database using its regionId
        Region existingRegion = regionRepository.findById(district.getRegion().getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        // Set the loaded Region in the District entity
        district.setRegion(existingRegion);

        // Save the District entity
        return districtRepository.save(district);
    }
}

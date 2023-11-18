package com.example.demo.lease.Service;

import org.springframework.stereotype.Service;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.District;
// import com.example.demo.lease.Model.Lease;
import com.example.demo.lease.Model.Region;
import com.example.demo.lease.Repository.BranchRepository;
import com.example.demo.lease.Repository.DistrictRepository;
import com.example.demo.lease.Repository.RegionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class HirerachyService {

    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final BranchRepository branchRepository;

    public HirerachyService(RegionRepository regionRepository, DistrictRepository districtRepository,
            BranchRepository branchRepository) {
        this.regionRepository = regionRepository;
        this.districtRepository = districtRepository;
        this.branchRepository = branchRepository;
    }

    public List<Map<String, Object>> getRegionHierarchy() {
        List<Map<String, Object>> data = new ArrayList<>();

        List<Region> regions = regionRepository.findAll();
        for (Region region : regions) {
            Map<String, Object> regionMap = new HashMap<>();
            regionMap.put("regionId", region.getId());
            regionMap.put("region", region.getRegionName());

            List<Map<String, Object>> districtsList = new ArrayList<>();
            List<District> districts = districtRepository.findByRegion(region);
            for (District district : districts) {
                Map<String, Object> districtMap = new HashMap<>();
                districtMap.put("districtId", district.getId());
                districtMap.put("name", district.getDistrictName());

                List<Map<String, Object>> branchesList = new ArrayList<>();
                List<Branch> branches = branchRepository.findByDistrict(district);
                for (Branch branch : branches) {
                    Map<String, Object> branchInfo = new HashMap<>();
                    branchInfo.put("BranchId", branch.getId());
                    branchInfo.put("name", branch.getBranchName());
                    branchInfo.put("branchCode", String.valueOf(branch.getBranchCode()));
                    branchesList.add(branchInfo);
                }
                districtMap.put("branches", branchesList);
                districtsList.add(districtMap);
            }
            regionMap.put("districts", districtsList);
            data.add(regionMap);
        }

        return data;
    }
}

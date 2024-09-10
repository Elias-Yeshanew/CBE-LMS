package com.cbe.lms.lease.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cbe.lms.lease.Model.Branch;
import com.cbe.lms.lease.Model.District;
// import com.cbe.lms.lease.Model.Lease;
import com.cbe.lms.lease.Model.Region;
import com.cbe.lms.lease.Repository.BranchRepository;
import com.cbe.lms.lease.Repository.DistrictRepository;
import com.cbe.lms.lease.Repository.RegionRepository;

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
            regionMap.put("regionId", region.getRegionId());
            regionMap.put("region", region.getRegionName());

            List<Map<String, Object>> districtsList = new ArrayList<>();
            List<District> districts = districtRepository.findByRegion(region);
            for (District district : districts) {
                Map<String, Object> districtMap = new HashMap<>();
                districtMap.put("districtId", district.getDistrictId());
                districtMap.put("name", district.getDistrictName());

                List<Map<String, Object>> branchesList = new ArrayList<>();
                List<Branch> branches = branchRepository.findByDistrict(district);
                for (Branch branch : branches) {
                    Map<String, Object> branchInfo = new HashMap<>();
                    branchInfo.put("BranchId", branch.getBranchId());
                    branchInfo.put("name", branch.getBranchName());
                    branchInfo.put("branchCode", String.valueOf(branch.getBranchCode()));
                    branchInfo.put("costCenter", String.valueOf(branch.getCostCenter()));
                    branchInfo.put("location", String.valueOf(branch.getLocation()));

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

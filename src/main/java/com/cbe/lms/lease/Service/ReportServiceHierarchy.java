package com.cbe.lms.lease.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cbe.lms.lease.Model.Branch;
import com.cbe.lms.lease.Model.District;
import com.cbe.lms.lease.Model.Lease;
import com.cbe.lms.lease.Model.Region;
import com.cbe.lms.lease.Repository.BranchRepository;
import com.cbe.lms.lease.Repository.DistrictRepository;
import com.cbe.lms.lease.Repository.LeaseRepository;
import com.cbe.lms.lease.Repository.RegionRepository;

// @Service
// public class reportServiceHirerachy {

//     private final RegionRepository regionRepository;
//     private final DistrictRepository districtRepository;
//     private final BranchRepository branchRepository;

//     public reportServiceHirerachy(RegionRepository regionRepository, DistrictRepository districtRepository,
//             BranchRepository branchRepository) {
//         this.regionRepository = regionRepository;
//         this.districtRepository = districtRepository;
//         this.branchRepository = branchRepository;
//     }

//     public List<Map<String, Object>> getRegionHierarchy() {
//         List<Map<String, Object>> data = new ArrayList<>();

//         List<Region> regions = regionRepository.findAll();
//         for (Region region : regions) {
//             Map<String, Object> regionMap = new HashMap<>();
//             regionMap.put("region", region.getRegionName());

//             List<Map<String, Object>> districtsList = new ArrayList<>();
//             List<District> districts = districtRepository.findByRegion(region);
//             for (District district : districts) {
//                 Map<String, Object> districtMap = new HashMap<>();
//                 districtMap.put("name", district.getDistrictName());

//                 List<Map<String, Object>> branchesList = new ArrayList<>();
//                 List<Branch> branches = branchRepository.findByDistrict(district);
//                 for (Branch branch : branches) {
//                     Map<String, Object> branchInfo = new HashMap<>();
//                     branchInfo.put("name", branch.getBranchName());
//                     branchInfo.put("branchCode", String.valueOf(branch.getBranchCode()));
//                     List<Map<String, Object>> leasesList = new ArrayList<>();
//                     for (Lease lease : branch.getLeases()) {
//                         Map<String, Object> leaseInfo = new HashMap<>();
//                         leaseInfo.put("contractNumber", lease.getContractNumber());
//                         // Add more lease properties as needed
//                         leasesList.add(leaseInfo);
//                     }
//                     branchInfo.put("leases", leasesList);
//                     branchesList.add(branchInfo);
//                 }
//                 districtMap.put("branches", branchesList);
//                 districtsList.add(districtMap);
//             }
//             regionMap.put("districts", districtsList);
//             data.add(regionMap);
//         }

//         return data;
//     }

// }

@Service
public class ReportServiceHierarchy {

    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final BranchRepository branchRepository;
    private final LeaseRepository leaseRepository;

    public ReportServiceHierarchy(RegionRepository regionRepository, DistrictRepository districtRepository,
            BranchRepository branchRepository, LeaseRepository leaseRepository) {
        this.regionRepository = regionRepository;
        this.districtRepository = districtRepository;
        this.branchRepository = branchRepository;
        this.leaseRepository = leaseRepository;
    }

    public List<Map<String, Object>> getRegionreportHierarchy() {
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
                    branchInfo.put("location", branch.getLocation());
                    branchInfo.put("costCenter", branch.getCostCenter());

                    // Add lease information to the branch
                    List<Map<String, Object>> leasesList = new ArrayList<>();
                    List<Lease> leases = leaseRepository.findByBranch(branch);
                    for (Lease lease : leases) {
                        Map<String, Object> leaseInfo = createLeaseInfo(lease);
                        leasesList.add(leaseInfo);
                    }

                    branchInfo.put("leases", leasesList);

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

    public Map<String, Object> getHierarchyForBranch(Long branchId) {
        Map<String, Object> data = new HashMap<>();

        Optional<Branch> branch = branchRepository.findById(branchId);

        if (branch.isPresent()) {
            Branch targetBranch = branch.get();
            District district = targetBranch.getDistrict();
            Region region = district.getRegion();

            // Add region information
            Map<String, Object> regionMap = new HashMap<>();
            regionMap.put("region", region.getRegionName());

            // Add district information
            Map<String, Object> districtMap = new HashMap<>();
            districtMap.put("name", district.getDistrictName());

            // Add branch information
            Map<String, Object> branchInfo = new HashMap<>();
            branchInfo.put("name", targetBranch.getBranchName());
            branchInfo.put("branchCode", String.valueOf(targetBranch.getBranchCode()));

            List<Map<String, Object>> leasesList = new ArrayList<>();
            List<Lease> leases = leaseRepository.findByBranch(targetBranch);

            for (Lease lease : leases) {
                Map<String, Object> leaseInfo = createLeaseInfo(lease);
                leasesList.add(leaseInfo);
            }

            branchInfo.put("leases", leasesList);
            districtMap.put("branches", Collections.singletonList(branchInfo));
            regionMap.put("districts", Collections.singletonList(districtMap));
            data.put("region_" + region.getRegionId(), regionMap);
        }

        return data;
    }

    public List<Map<String, Object>> getHierarchyForBranches(List<Long> branchIds) {
        List<Map<String, Object>> branchHierarchies = new ArrayList<>();

        for (Long branchId : branchIds) {
            Optional<Branch> branchOptional = branchRepository.findById(branchId);

            if (branchOptional.isPresent()) {
                Branch branch = branchOptional.get();
                Map<String, Object> branchInfo = new HashMap<>();
                branchInfo.put("BranchId", branch.getBranchId());
                branchInfo.put("name", branch.getBranchName());
                branchInfo.put("branchCode", String.valueOf(branch.getBranchCode()));

                // Add lease information to the branch
                List<Map<String, Object>> leasesList = new ArrayList<>();
                List<Lease> leases = leaseRepository.findByBranch(branch);
                for (Lease lease : leases) {
                    Map<String, Object> leaseInfo = createLeaseInfo(lease);
                    leasesList.add(leaseInfo);

                }

                branchInfo.put("leases", leasesList);
                branchHierarchies.add(branchInfo);
            }
        }
        return branchHierarchies;
    }

    // Helper method to create a lease info map
    private Map<String, Object> createLeaseInfo(Lease lease) {
        Map<String, Object> leaseInfo = new HashMap<>();
        leaseInfo.put("ID", lease.getId());
        leaseInfo.put("Total Payment", lease.getTotalPayment());
        leaseInfo.put("Advance Payment", lease.getAdvancePayment());
        leaseInfo.put("Contract Start Date", lease.getContractStartDate());
        leaseInfo.put("Contract End Date", lease.getContractEndDate());
        leaseInfo.put("Discount Rate", lease.getDiscountRate());
        leaseInfo.put("initial Direct Cost", lease.getInitialDirectCost());
        leaseInfo.put("Lease Incentive", lease.getLeaseIncentive());
        leaseInfo.put("contract Type", lease.getContractType());
        leaseInfo.put("contract Reason", lease.getContractReason());
        
        return leaseInfo;
    }

}

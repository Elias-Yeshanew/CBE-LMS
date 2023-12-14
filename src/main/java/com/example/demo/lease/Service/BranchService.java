package com.example.demo.lease.Service;

import org.springframework.stereotype.Service;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.District;
import com.example.demo.lease.Repository.BranchRepository;
import com.example.demo.lease.Repository.DistrictRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final DistrictRepository districtRepository;

    public Branch getBranchByPoliticalRegion(String politicalRegion) {
        return branchRepository.findByPoliticalRegion(politicalRegion);
    }

    public long getBranchCount() {
        return branchRepository.count();
    }

    // public Branch addNewBranch(Branch branch) throws Exception {
    // // Load the District from the database using its districtId
    // District existingDistrict =
    // districtRepository.findById(branch.getDistrict().getDistrictId())
    // .orElseThrow(() -> new IllegalArgumentException("District not found"));

    // // Set the loaded District in the Branch entity
    // // branch.setDistrict(existingDistrict);
    // branch = Branch.builder()
    // .branchName(branch.getBranchName())
    // .branchCode(branch.getBranchCode())
    // .location(branch.getLocation())
    // .costCenter(branch.getCostCenter())
    // .district(branch.getDistrict()) // Set the District
    // .build();

    // // Save the Branch
    // // Save the Branch entity
    // return branchRepository.save(branch);
    // }

    public Branch addNewBranch(Branch branch) throws Exception {
        // Load the District from the database using its districtId
        District existingDistrict = districtRepository.findById(branch.getDistrict().getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found"));

        // Set the loaded District in the Branch entity
        branch.setDistrict(existingDistrict);

        // Save the Branch
        // Save the Branch entity
        branch = branchRepository.save(branch);

        // Fetch the Branch entity again to get the fully initialized object
        branch = branchRepository.findById(branch.getBranchId()).orElse(null);

        return branch;
    }

    public Branch addNewBranchs(Branch branch) throws Exception {
        return branchRepository.save(branch);
    }

}

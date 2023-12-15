package com.example.demo.lease.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    public Map<String, Object> getBranchById(long branchId) {
        Optional<Branch> optionalBranch = branchRepository.findById(branchId);
        Map<String, Object> branchdata = new HashMap<>();
        branchdata.put("branch",
                optionalBranch.map(this::mapBranch));
        return branchdata;
    }

    public Map<String, Object> getAllBranches(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size); // Adjust the page number
        Page<Branch> branchPage = branchRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, branchPage.getTotalElements()));

        response.put("branches",
                branchPage.getContent().stream().map(this::mapBranch).collect(Collectors.toList()));

        return response;
    }

    private Map<String, Object> mapBranch(Branch branch) {
        Map<String, Object> branchData = new HashMap<>();
        branchData.put("branchId", branch.getBranchId());
        branchData.put("branchName", branch.getBranchName());
        branchData.put("branchCode", branch.getBranchCode());
        branchData.put("location", branch.getLocation());
        branchData.put("costCenter", branch.getCostCenter());
        branchData.put("claimAccount", branch.getClaimAccount());
        branchData.put("district", branch.getDistrict());
        branchData.put("politicalRegion", branch.getPoliticalRegion());
        // Include other branch fields

        return branchData;
    }

    public Branch updateBranchById(Long branchId, Branch updatedBranch) throws Exception {
        Branch existingBranch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        // Update fields based on your requirements
        existingBranch.setBranchName(updatedBranch.getBranchName());
        existingBranch.setBranchCode(updatedBranch.getBranchCode());
        existingBranch.setLocation(updatedBranch.getLocation());
        existingBranch.setCostCenter(updatedBranch.getCostCenter());
        existingBranch.setClaimAccount(updatedBranch.getClaimAccount());

        // Update the district if it's provided
        if (updatedBranch.getDistrict() != null) {
            District existingDistrict = districtRepository.findById(updatedBranch.getDistrict().getDistrictId())
                    .orElseThrow(() -> new IllegalArgumentException("District not found"));
            existingBranch.setDistrict(existingDistrict);
        }

        return branchRepository.save(existingBranch);
    }
}

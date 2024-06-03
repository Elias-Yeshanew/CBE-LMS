package com.example.demo.lease.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.District;
import com.example.demo.lease.Repository.BranchRepository;
import com.example.demo.lease.Repository.DistrictRepository;

import jakarta.persistence.EntityNotFoundException;
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
        if (optionalBranch.isPresent()) {

            Map<String, Object> branchdata = new HashMap<>();
            branchdata.put("branch",
                    optionalBranch.map(this::mapBranch));
            return branchdata;
        } else {
            return null;
        }
    }

    public Map<String, Object> getAllBranches(int page, int size, String sortBy,
            String sortOrder) {
        Map<String, Object> response = new HashMap<>();
        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, sortBy));
        Page<Branch> branchPage = branchRepository.findAll(pageable);

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

    // public Branch updateBranchById(Long branchId, Branch updatedBranch) throws
    // Exception {
    // Optional<Branch> optionalBranch = branchRepository.findById(branchId);
    // if (optionalBranch.isPresent()) {
    // Branch existingBranch = optionalBranch.get();
    // // Update fields based on your requirements
    // existingBranch.setBranchName(updatedBranch.getBranchName());
    // existingBranch.setBranchCode(updatedBranch.getBranchCode());
    // existingBranch.setLocation(updatedBranch.getLocation());
    // existingBranch.setCostCenter(updatedBranch.getCostCenter());
    // existingBranch.setClaimAccount(updatedBranch.getClaimAccount());

    // // Update the district if it's provided
    // if (updatedBranch.getDistrict() != null) {
    // Optional<District> optionalDistrict = districtRepository
    // .findById(updatedBranch.getDistrict().getDistrictId());
    // if (optionalDistrict.isPresent()) {
    // District existingDistrict = optionalDistrict.get();
    // existingBranch.setDistrict(existingDistrict);
    // } else {
    // throw new IllegalArgumentException("District not found");
    // }
    // }
    // return branchRepository.save(existingBranch);
    // } else {
    // throw new IllegalArgumentException("Branch not found");
    // }
    // }

    public Branch updateBranchById(Long branchId, Branch updatedBranch) throws Exception {
        Optional<Branch> optionalExistingBranch = branchRepository.findById(branchId);
        if (optionalExistingBranch.isPresent()) {
            Branch existingBranch = optionalExistingBranch.get();
            System.out.println(existingBranch.getBranchName());
            // Update the district if it's provided
            if (updatedBranch.getDistrict() != null) {
                Long districtId = updatedBranch.getDistrict().getDistrictId();
                Optional<District> optionalDistrict = districtRepository.findById(districtId);
                if (optionalDistrict.isPresent()) {
                    District existingDistrict = optionalDistrict.get();
                    existingBranch.setDistrict(existingDistrict);
                } else {
                    throw new IllegalArgumentException("District not found");
                }
            }

            // Update fields based on your requirements
            existingBranch.setBranchName(updatedBranch.getBranchName());
            existingBranch.setBranchCode(updatedBranch.getBranchCode());
            existingBranch.setLocation(updatedBranch.getLocation());
            existingBranch.setCostCenter(updatedBranch.getCostCenter());
            existingBranch.setClaimAccount(updatedBranch.getClaimAccount());

            return branchRepository.save(existingBranch);
        } else {
            throw new IllegalArgumentException("Branch not found");
        }
    }

    public void deleteBranchById(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new EntityNotFoundException("Branch with id " + branchId + " does not exist.");
        }
        branchRepository.deleteById(branchId);
    }

    public Map<String, Object> getBranchsByDistrictId(int page, int size, Long districtId, String sortBy,
            String sortOrder) {
        Map<String, Object> response = new HashMap<>();
        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        if (sortBy == null || sortOrder == null) {
            pageable = PageRequest
                    .of(page - 1, size);
        } else {
            pageable = PageRequest
                    .of(page - 1, size, Sort.by(direction, sortBy));
        }
        pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, sortBy));
        Page<Branch> branchPage = branchRepository.findBranchbyDistrict(districtId, pageable);
        response.put("pagination", PaginationUtil.buildPagination(page, size, branchPage.getTotalElements()));
        response.put("branches",
                branchPage.getContent().stream().map(this::mapBranch).collect(Collectors.toList()));

        return response;
    }
}
